 package newsrack.archiver;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Hashtable;
 import java.util.Stack;
 import java.util.regex.Pattern;
 
 import newsrack.NewsRack;
 import newsrack.util.IOUtils;
 import newsrack.util.StringUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.htmlparser.Parser;
 import org.htmlparser.PrototypicalNodeFactory;
 import org.htmlparser.Tag;
 import org.htmlparser.Text;
 import org.htmlparser.tags.MetaTag;
 import org.htmlparser.util.ParserException;
 import org.htmlparser.visitors.NodeVisitor;
 
 public class HTMLFilter extends NodeVisitor 
 {
    	// Logging output for this class
    private static Log _log = LogFactory.getLog(HTMLFilter.class);
 
 	public static final String[] IGNORE_ELTS = {
 		"SCRIPT", "NOSCRIPT", "STYLE", "MARQUEE", "APPLET", "MAP", 
 		"SELECT", "A", "LABEL",
 // Note: Even though the following tags below are included here,
 // they NEED NOT BE included because these are standalone tags.
 // They dont come in the form <TAG> ... </TAG>. 
 // They come in the form <TAG ... />
 // So, the only call to the Visitor will be to "visitTag" --
 // there wont be any corresponding call to "visitEndTag"
 // But, including them in this list to avoid confusion
 //		"AREA", "OPTION", "IMG", "INPUT",
 	};
 
 	public static final String[] BLOCK_ELTS = {
 		"HTML", "BODY",
 		"P", "DIV", "PRE", "BLOCKQUOTE", "CENTER", "ADDRESS", "FORM",
 		"UL", "OL", "LI", "DL", "DD", "DT",
 		"H1", "H2", "H3", "H4", "H5", "H6",
 		"TABLE", "THEAD", "TBODY", "TFOOT", "TH", "TD", "TR"
 	};
 
 	public static final Hashtable BLOCK_ELTS_TBL;
 	public static final Hashtable IGNORE_ELTS_TBL;
 
 	static {
 		IGNORE_ELTS_TBL = new Hashtable();
 		for (String element : IGNORE_ELTS)
 			IGNORE_ELTS_TBL.put(element, "");
 
 		BLOCK_ELTS_TBL = new Hashtable();
 		for (String element : BLOCK_ELTS)
 			BLOCK_ELTS_TBL.put(element, "");
 
 		_lineSep = System.getProperty("line.separator");
 
 		org.htmlparser.scanners.ScriptScanner.STRICT = false;	// Turn off strict parsing of cdata
       org.htmlparser.lexer.Lexer.STRICT_REMARKS = false; 	// Turn off strict parsing of HTML comments
 		java.net.HttpURLConnection.setFollowRedirects(false);	// Turn off automatic redirect processing
 	}
 
 	public static void clearCookieJar()
 	{
          // Hack! disabling and enabling cookie processing on the connection manager clears the cookie jar!
       Parser.getConnectionManager().setCookieProcessingEnabled(false);
       Parser.getConnectionManager().setCookieProcessingEnabled(true);
 	}
 
 	static private class DOM_Node_Info {
 		String       tagName;						// Tag for this DOM node
 		int          totalContentSize;			// Total size of all content in the subtree rooted at this dom node
 		StringBuffer currUnfilteredContent;		// Buffer containing current unfiltered content 
 															// (all text from some DOM descendents might have been discarded)
 		StringBuffer currFilteredContent;		// Buffer containing current filtered content 
 															// (all text from some DOM descendents might have been discarded 
 															//   + anchor text from some DOM descendents might have been discarded)
 
 		public DOM_Node_Info(String t) 
 		{
 			tagName = t;
 			totalContentSize = 0;
 			currUnfilteredContent = new StringBuffer();
 			currFilteredContent = new StringBuffer();
 		}
 
 		public void appendUnfilteredContent(String s)
 		{
 			currUnfilteredContent.append(s);
 			totalContentSize += s.length();
 		}
 
 		public void appendContent(String s)
 		{
 			currUnfilteredContent.append(s);
 			currFilteredContent.append(s);
 			totalContentSize += s.length();
 		}
 	
 		public void appendContent(String s1, String s2)
 		{
 			currUnfilteredContent.append(s1);
 			currFilteredContent.append(s2);
 			totalContentSize += s1.length();
 		}
 
 		public void discardContent()
 		{
 			currUnfilteredContent = new StringBuffer();
 			currFilteredContent = new StringBuffer();
 		}
 
 		public boolean shouldKeepBlockContent()
 		{
 			// HEURISTIC:
 			// - If filtered content is less than 50% of the full text content, chuck it!
 			// - If over 50%, check if the content we are considering will lead to at least 5% of all content from this block 
 			//   If not, it might just be that this is just a header for some link block -- so no use retaining it!
 			int b1_len = currUnfilteredContent.length();  
 			int b2_len = currFilteredContent.length();
 			if (_debug) System.out.println("b1_len - " + b1_len + "; b2_len - " + b2_len + "; n1 - " + totalContentSize);
 			return (   (b2_len * 1.0 / b1_len) > 0.5
 					  && (b1_len * 1.0 / (b1_len + totalContentSize) > 0.05));
 		}
 
 		public void swallowChild(DOM_Node_Info child, boolean discardContent)
 		{
 			if (discardContent) {
 				if (_debug) System.out.println("BLK: Discarding " + child.currUnfilteredContent.toString().replaceAll("\n", "|"));
 			}
 			else {
 				String childContent = child.currUnfilteredContent.toString();
 				currUnfilteredContent.append(childContent);
 				currFilteredContent.append(childContent);
 				if (_debug) System.out.println("BLK: Accumulating " + childContent.replaceAll("\n", "|"));
 			}
 
 			totalContentSize += child.totalContentSize;
 		}
 	}
 
 	private static String  _lineSep;	// Line separator
    private static boolean _debug = false;
 
 	private Stack<String> _ignoreFlagStack;
 	private Stack<DOM_Node_Info> _eltContentStack;
 	private String		   _title;
 	private StringBuffer _content;
 	private String       _origHtml;
 	private String       _url;
 	private String       _urlDomain;
 	private String       _file;
 	private PrintWriter  _pw;
 	private OutputStream _os;
 	private boolean      _closeStream;	// Should I close output streams after I am done?
 	private boolean	   _PREtagContent;
 	private boolean	   _isTitleTag;
    private boolean      _outputToFile; // Should the content be output to a file?
 		// Next field is for domain-specific hacks
 	private boolean	   _ignoreEverything;
 		// Next 2 fields for Newkerala.com hack -- May 18, 2006
 	private boolean	   _foundKonaBody;
 	private Stack        _spanTagStack;
 		// Next field is for Mumbai/Bangalore Mirror hack -- June 7, 2009
 	private boolean      _isBMmirror;
 
 	private void initFilter()
 	{
 		_title            = "";
 		_content          = null;
 		_PREtagContent    = false;
 		_isTitleTag       = false;
 		_ignoreFlagStack  = new Stack<String>();
 		_eltContentStack  = new Stack<DOM_Node_Info>();
 		_closeStream      = false;
 		_spanTagStack     = new Stack();
       _outputToFile     = true;     // By default, content is written to file!
 		_ignoreEverything = false;
 		_foundKonaBody    = false;
 		_isBMmirror        = false;
 
 			// Set up some default connection properties!
 		Hashtable headers = new Hashtable();
 		String ua = NewsRack.getProperty("useragent.string");
 		if (ua == null) ua = "NewsRack/1.0 (http://newsrack.in)";
 		headers.put ("User-Agent", ua);
       headers.put ("Accept-Encoding", "gzip, deflate");
 
 		Parser.getConnectionManager().setCookieProcessingEnabled(true);
 		Parser.getConnectionManager().setRedirectionProcessingEnabled(true);
 		Parser.getConnectionManager().setDefaultRequestProperties(headers);
 	}
 
 	private HTMLFilter()
 	{
 		initFilter();
 	}
 
 	private void setUrl(String url)
 	{
 		_url = url;
 		_urlDomain = StringUtils.getDomainForUrl(_url);
 
 			// Mumbai/Bangalore Mirror specific hack -- June 7, 2009
 		_isBMmirror = _urlDomain.equals("bangaloremirror.com") || _urlDomain.equals("mumbaimirror.com");
 	}
 	
 	/**
 	 * @param file File to parse
 	 */
 	public HTMLFilter(String file)
 	{
 		initFilter();
 		_outputToFile = false;
 		_file = file;
 	}
 
 	/**
 	 * @param fileOrUrl  File/URL that has to be filtered
 	 * @param pw         Print Writer to which filtered HTML should be written
 	 * @param isURL      True if 'fileOrUrl' is a URL
 	 **/
 	public HTMLFilter(String fileOrUrl, PrintWriter pw, boolean isURL)
 	{
 		initFilter();
 		_pw = pw;
 		if (isURL)
 			setUrl(fileOrUrl);
 		else
 			_file = fileOrUrl;
 	}
 
 	/**
 	 * @param fileOrUrl  File/URL that has to be filtered
 	 * @param os         Output Stream to which filtered HTML should be written
 	 * @param isURL      True if 'fileOrUrl' is a URL
 	 **/
 	public HTMLFilter(String fileOrUrl, OutputStream os, boolean isURL)
 	{
 		initFilter();
 		_os  = os;
 		if (isURL)
 			setUrl(fileOrUrl);
 		else
 			_file = fileOrUrl;
 	}
 
 	/**
 	 * @param fileOrUrl  File/URL that has to be filtered
 	 * @param outputDir  Directory where the filtered file has to be written
 	 * @param isURL      True if 'fileOrUrl' is a URL
 	 *
 	 * @throws IOException if there is an error trying to create the output file
 	 */
 	public HTMLFilter(String fileOrUrl, String outputDir, boolean isURL) throws java.io.IOException
 	{ 
 		initFilter();
 
 		char sep;
 		if (isURL) {
 			sep = '/';
 			setUrl(fileOrUrl);
 		}
 		else {
 			_file = fileOrUrl;
 			sep = File.separatorChar;
 		}
 		_pw = IOUtils.getUTF8Writer(outputDir + File.separator + fileOrUrl.substring(1 + fileOrUrl.lastIndexOf(sep)));
 			// Since I have opened these streams, I should close them after I am done!
 		_closeStream = true;
 	}
 
 	/**
 	 * @param url        URL of the article -- this url will be added in the filtered file
 	 *                   as a link to the original article
 	 * @param file       File that has to be filtered
 	 * @param outputDir  Directory where the filtered file has to be written
 	 * @throws IOException if there is an error trying to create the output file
 	 */
 	public HTMLFilter(String url, String file, String outputDir) throws java.io.IOException
 	{ 
 		initFilter();
 		setUrl(url);
 		_file = file;
 		_pw = IOUtils.getUTF8Writer(outputDir + File.separator + file.substring(1 + file.lastIndexOf(File.separatorChar)));
 			// Since I have opened these streams, I should close them after I am done!
 		_closeStream = true;
 	}
 
 	public String getOrigHtml() { return _origHtml; }
 
 	public String getUrl() { return _url; }
 
 	public void run() throws Exception
 	{
 		Parser parser;
 		try {
 		   parser = new Parser((_file != null) ? _file : _url);
 		}
 		catch (Exception e) {
 			String msg = e.toString();
 			int    i   = msg.indexOf("no protocol:");
 			if (i > 0 && _url != null) {
 				String urlSuffix = msg.substring(i + 13);
 				_log.info("Got malformed url exception " + msg + "; Retrying with url - " + _urlDomain + urlSuffix);
 				parser = new Parser(_urlDomain + urlSuffix);
 			}
 			else {
 				throw e;
 			}
 		}
 		parseNow(parser, this);
 		_url = parser.getURL();
 		_origHtml = parser.getLexer().getPage().getText();
 	}
 
    @Override
 	public boolean shouldRecurseSelf() { return true; } 
 
    @Override
 	public boolean shouldRecurseChildren() { return true; }
 
    @Override
 	public void beginParsing() { startDocument(); }
 
 	private void startDocument() 
 	{
 		_content = new StringBuffer();
 	}
 
    @Override
 	public void visitTag(Tag tag) 
 	{
 		String tagName = tag.getTagName();
 		if (_debug) System.out.println("ST. TAG - " + tagName + "; name attribute - " + tag.getAttribute("name"));
 
 		if (IGNORE_ELTS_TBL.get(tagName) != null) {
 			if (tagName.equals("A")) {
 				// SPECIAL CASE: Don't ignore non-href anchor tags 
 				// Required so that Hindustan Times article titles don't get stripped out!
 				String href = tag.getAttribute("HREF");
 				if (href == null || href.equals(""))
 					return;
 			}
 			_ignoreFlagStack.push(tagName);
 			if (_debug) System.out.println("--> PUSHED");
 		}
 		else if (tagName.equals("BR")) {
 			// India together articles have some strange html that leads to empty stack here!
 			// Otherwise this check is normally not necessary!
 			if (!_eltContentStack.isEmpty()) {
 				DOM_Node_Info topElt = _eltContentStack.peek();
 				topElt.appendContent("\n");
 			}
 		}
 		else if (tagName.equals("PRE")) {
 			_PREtagContent = true;
 		}
 		else if (tagName.equals("TITLE")) {
 			_isTitleTag = true;
 		}
 		else if (tagName.equals("HTML") || tagName.equals("BODY")) {
 			if (_debug) System.out.println("Pushing new dom-node-info for " + tagName);
 			_eltContentStack.push(new DOM_Node_Info(tagName));
 		}
 		else {
 				// Push a new dom-node-info only for block elements
 			if (BLOCK_ELTS_TBL.get(tagName) != null) {
 				if (_debug) System.out.println("Pushing new dom-node-info for " + tagName);
 				_eltContentStack.push(new DOM_Node_Info(tagName));
 			}
 
 			// Mumbai/Bangalore Mirror hack -- June 7, 2009
 			// Everything after id="tags" is not required.
 			if (tagName.equals("DIV") && _isBMmirror) {
 				String divId = tag.getAttribute("id");
 				if ((divId != null) && divId.equals("tags"))
 					_ignoreEverything = true;
 			}
 
 			if (!_ignoreEverything && (BLOCK_ELTS_TBL.get(tagName) != null)) {
 				DOM_Node_Info topElt = _eltContentStack.peek();
 				topElt.appendContent("\n\n");
 			}
 				// Newkerala.com hack -- May 18, 2006
 			else if (tagName.equals("SPAN")) {
 				String nameAttr = tag.getAttribute("name");
 				if ((nameAttr != null) && nameAttr.equals("KonaBody")) {
 					_foundKonaBody = true;
 					_spanTagStack.push("KONABODY_SPAN");
 				}
 				else {
 					_spanTagStack.push("SPAN");
 				}
 			}
 		}
 	}
 
 	private void processCurrStackElt()
 	{
 		DOM_Node_Info top = _eltContentStack.pop();
 		if (_debug) System.out.println("Popping dom-node-info for " + top.tagName);
 
 		// If we are the end, the unfiltered buffer contains our content
 		if (_eltContentStack.isEmpty()) {
 			_content.append(top.currUnfilteredContent.toString());
 		}
 		else {
 			DOM_Node_Info parent = _eltContentStack.peek();
 				// Rather than treat all siblings at a DOM-level identically, we assume that a block element
 				// effectively introduces an artificial block consisting of all DOM siblings seen so far!
 				// i.e. if [ ... DIV ... ] represents the current tree-nesting level in the DOM tree,
 				// process siblings to the left of DIV as if it were in its own block.
 				//
 				// IMPORTANT: Process left siblings *before* processing the current block DOM element
 			if (!parent.shouldKeepBlockContent()) {
 				if (_debug) System.out.println("BLK: Discarding " + parent.currUnfilteredContent.toString().replaceAll("\n", "|"));
 				parent.discardContent();
 			}
 
 				// Keep everything in child or dump it all!
 			if (top.shouldKeepBlockContent())
 				parent.swallowChild(top, false);
 			else
 				parent.swallowChild(top, true);
 		}
 	}
 
    @Override
 	public void visitEndTag(Tag tag) 
 	{
 		String tagName = tag.getTagName();
 
 		if (_debug) System.out.println("END : " + tagName);
 
 		if (!_eltContentStack.isEmpty()) {
 			DOM_Node_Info topElt = _eltContentStack.peek();
 			if (topElt != null) {
 				if (topElt.tagName.equals(tagName))
 					processCurrStackElt();
 				else
 					if (_debug) System.out.println(" ... Waiting for " + topElt.tagName + "; got " + tagName);
 			}
 		}
 
 		if (!_ignoreFlagStack.isEmpty() && _ignoreFlagStack.peek().equals(tagName)) {
 			if (_debug) System.out.println("--> POPPED");
 			_ignoreFlagStack.pop();
 		}
 
 		if (tagName.equals("PRE")) {
 			_PREtagContent = false;
 		}
 		else if (tagName.equals("TITLE")) {
 			_isTitleTag = false;
 		}
 			// Newkerala.com hack -- May 18, 2006
 		else if (!_ignoreEverything && _foundKonaBody && tagName.equals("SPAN")) {
 			try {
 				Object spanTag = _spanTagStack.pop();
 				if (spanTag.equals("KONABODY_SPAN"))
 					_ignoreEverything = true;
 			}
 			catch (Exception e) {
 				if (_log.isErrorEnabled()) _log.error("popped out all span tags already! .. empty stack!");
 			}
 /**
  * Commented out Jun 5, 2009; extra crud in the text
  *
 			if (_url != null) {
 				if (_url.indexOf("newkerala.com") != -1)
 					_content.append("\nCopyright 2001-2005 newkerala.com");
 				else if (_url.indexOf("indianexpress.com") != -1)
 					_content.append("\n\n&copy; 2006: Indian Express Newspapers (Mumbai) Ltd. All rights reserved throughout the world");
 				else if (_url.indexOf("financialexpress.com") != -1)
 					_content.append("\n\n&copy; 2006: Indian Express Newspapers (Mumbai) Ltd. All rights reserved throughout the world");
 			}
 	**/
 		}
 	}
 
 	public void visitStringNode(Text string) 
 	{
 		String eltContent = string.getText();
 		if (_debug) System.out.println("TAG txt - " + eltContent);
 
 		if (_eltContentStack.isEmpty())
 			return;
 
 		DOM_Node_Info topElt = _eltContentStack.peek();
 
 			// If this text is coming in the context of a ignoreable tag, discard
 		if (!_ignoreFlagStack.isEmpty()) {
 		   if (_debug) System.out.println(" -- IGNORED");
 
 				// Add it to unfiltered buffer
 			String currIgnoreTag = _ignoreFlagStack.peek();
 			if (currIgnoreTag.equals("A") || currIgnoreTag.equals("LABEL"))
 				topElt.appendUnfilteredContent(" " +eltContent.trim());
 
 			return;
 		}
 			// Newkerala.com hack -- May 18, 2006
 		else if (_ignoreEverything) {
 		   if (_debug) System.out.println(" -- IGNORED");
 			return;
 		}
 
 		if (_PREtagContent) {
 			topElt.appendContent(eltContent);
 			if (_debug) System.out.println("PRE: Accumulating " + eltContent);
 		}
 		else if (_isTitleTag) {
 			if (_debug) System.out.println("TITLE: ... " + eltContent);
 			if (_title.equals("")) {
 				_title = eltContent;
 			}
 		}
 		else {
 			eltContent = collapseWhiteSpace(eltContent);
 			if (!isJunk(eltContent)) { // skip spurious content!
 				topElt.appendContent(eltContent);
 				if (_debug) System.out.println("NORMAL: Accumulating " + eltContent);
 			}
 			else {
 				if (_debug) System.out.println("JUNK: Discarding " + eltContent);
 			}
 		}
 	}
 
 	public void finishedParsing()
 	{
 		// We have unbalanced tags!
 		if (!_eltContentStack.isEmpty()) {
 			if (_debug) System.out.println("Malformed HTML? Got an unbalanced content stack!");
 			while (!_eltContentStack.isEmpty())
 				processCurrStackElt();
 		}
 
 		if (_url != null) {
 			String t1 = _title;
 
 				// Normalize white space
 			t1 = _title.replaceAll("(\n|\\s+)", " ").trim();
 
 				// Strip domain from title!
 			t1 = (Pattern.compile("^\\s*" + _urlDomain + "\\s*[|\\-:]?\\s*", Pattern.CASE_INSENSITIVE)).matcher(t1).replaceAll("");
 			t1 = (Pattern.compile("\\s*[|\\-:]?\\s*" + _urlDomain + "\\s*$", Pattern.CASE_INSENSITIVE)).matcher(t1).replaceAll("");
 
 				// If the title hasn't changed, check if we are actually in a subdomain -- retry with the main domain
 				// Ex: dealbook.blogs.nytimes.com --> retry with nytimes.com!
 			if (t1.equals(_title)) {
 				String[] domainParts = _urlDomain.split("\\.");
 				int      n           = domainParts.length;
 				if (n > 2) {
 					String domain = domainParts[n-2] + "." + domainParts[n-1];
 					t1 = (Pattern.compile("^" + domain + "\\s*[|\\-:]?\\s*", Pattern.CASE_INSENSITIVE)).matcher(t1).replaceAll("");
 					t1 = (Pattern.compile("\\s*[|\\-:]?\\s*" + domain + "\\s*[|\\-:]?$", Pattern.CASE_INSENSITIVE)).matcher(t1).replaceAll("");
 				}
 			}
 
 				// New title!
 			_title = t1;
 			if (_debug) System.out.println("ORIG TITLE: " + _title + "\nNEW TITLE: " + _title);
 		}
 
 			// Split the content around matches of the title, if any ... But, check this out!
 			// 1. Replace all space characters with the "\s+" regexp so that variations in number of white space won't trip up the match!
 			// 2. Replace all special characters with "." allowing for the matching to be more lenient 
 			// 3. Replace colon(:), hyphen(-) with a regexp or (|) so that there is a greater chance of
 			//    finding a match of the title despite trailers / leaders in the title!  Since we are looking
 			//    for the smallest match, we are guaranteed that we'll hit the jackpot around the actual title!
 			// 4. Do two REs one where the '-' matches with anything, and another where '-' is converted to an "|"
 			//    Without this fix to the above strategy (3. above), we will have partial replacements
 			//    Ex: With title "Attack-hit women of Bangalore vent ire on the Web", only "Attack" will be
 			//        removed leaving a partial title in the article which is not as good as we can do.
		String  titleRE = _title.replaceAll("\\s+","\\\\s+").replaceAll("(\\$|\\?|\\(|\\)|\\[|\\]|\\|)", ".");
 		titleRE = titleRE.replaceAll("[:\\-]", ".") + "|" + titleRE.replaceAll("[:\\-]+", "|");
 		String[] xs = Pattern.compile(titleRE, Pattern.CASE_INSENSITIVE).split(_content, 2);
 		if ((xs.length > 1) && (xs[0].length() < xs[1].length())) {
 				// We are discarding xs[0] -- but, let us preserve any information about publishing date!
 			String[] linesBeingDiscarded = Pattern.compile("\n").split(xs[0]);
 			Pattern  datePattern         = Pattern.compile("^.*(volume|posted|published|updated).*(\\d+).*$");
 			String   dateLine            = "";
 			for (String line: linesBeingDiscarded) {
 				if (datePattern.matcher(line).matches()) {
 					dateLine = line.trim();
 					break;
 				}
 			}
 
 				// New content!
 			_content = (new StringBuffer(dateLine)).append("\n\n").append(xs[1]);
 			if (_debug) System.out.println("Stripping away " + xs[0].length() + " chars; Leaving " + xs[1].length() + " chars;\n Stripping away: " + xs[0]);
 		}
 		else if (_debug) {
 			System.out.println("Got " + xs.length + " items from splitting around " + titleRE);
 			if (xs.length > 1)
 				System.out.println("xs[0] size: " + xs[0].length() + " chars; xs[1] size: " + xs[1].length() + " chars;\n xs[0]: " + xs[0]);
 		}
 
 /**
  * Not foolproof yet!
 
 			// Remove copyright notices
 		String[] strs = Pattern.compile("((Copyright|\\d+|&copy;)[\\s,;-]*){2,}((\\w+[\\s,;-]+){0,5}?\\s*((rights|reserved)\\s*)+)?").split(_content.toString());
 		_content = new StringBuffer();
 		for (String s: strs)
 			_content.append(s);
 **/
 
 			// Finally, output new content!
       if (_outputToFile)
 		   outputToFile(_content);
 	}
 
 	private static String collapseWhiteSpace(String s)
 	{
 		int          n     = s.length();
 		char[]       cs    = new char[n];
 		StringBuffer sb    = new StringBuffer();
 		boolean      ws    = false;
 		boolean      empty = true;
 
 		s.getChars(0, n, cs, 0);
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
 				ws = true;
 			}
 					// &nbsp; is considered white space
 			else if ((c == '&')
 				   && ((i+5) < n) 
 					&& (cs[i+1] == 'n')
 					&& (cs[i+2] == 'b')
 					&& (cs[i+3] == 's')
 					&& (cs[i+4] == 'p')
 					&& (cs[i+5] == ';'))
 			{
 				i += 5;
 				ws = true;
 			}
 			else {
 				if (ws) {
 					sb.append(' ');
 					ws = false;
 				}
 				else if (empty) {
 					sb.append(' ');	// ensure there is white space before content
 				}
 				empty = false;
 				sb.append(c);
 			}
 		}
 		if (!ws)
 			sb.append(' ');	// ensure there is white space after content
 
 		return sb.toString();
 	}
 
 	private static boolean isJunk(String sb) 
 	{
 		int     n  = sb.length();
 		char[]  cs = new char[n];
 		sb.getChars(0, n, cs, 0);
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if (!Character.isWhitespace(c) && (c != '|') && (c != '-'))
 				return false;
 		}
 
 		return true;
 	}
 
 	private static String prettyPrint(StringBuffer s) 
 	{
 		// NOTE: In the node visitor methods, I am using "\n" and
 		// not _lineSep.  It does not matter, because, in all those
 		// methods, "\n" is used as the generic line separator.
 		// Before the string is output to a file, in the "prettyPrint"
 		// method, "\n"s are being replaced with _lineSep strings.
 
 		int          LINE_WIDTH = 75;
 		int          n          = s.length();
 		char[]       cs         = new char[n];
 		StringBuffer lb         = new StringBuffer();
 		StringBuffer sb         = new StringBuffer();
 		boolean      ws         = false;
 		int          numNLs     = 0;
 		int          numChars   = 0;
 		int          lastWsPosn = 0;
 
 		s.getChars(0, n, cs, 0);
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if (c == '\n') {
 				numNLs++;
 			} 
 			else if (Character.isWhitespace(c)) {
 				if (!ws) {
 					ws = true;
 					lastWsPosn = numChars + 1;
 				}
 			}
 			else {
 				if (numNLs > 0) {
 					lb.append(_lineSep);
 					numChars++;
 						// Replace 2 or more new lines by exactly 2 new lines
 					if (numNLs > 1) {
 						lb.append(_lineSep);
 						numChars++;
 					}
 						// Since the line is not junk,
 						// append the entire line to 'sb' and clear it out
 					sb.append(lb);
 					lb.delete(0, numChars);
 					numChars = 0;
 					lastWsPosn = 0;
 					numNLs = 0;
 				}
 				else if (ws) {
 					lb.append(' ');
 					numChars++;
 				}
 				ws = false;
 
 				lb.append(c);
 				numChars++;
 				if (numChars > LINE_WIDTH) {
 						// If cannot properly break the line, arbitrarily break it!
 					if (lastWsPosn == 0)
 						lastWsPosn = LINE_WIDTH;
 
 						// Get the max full words in this line
 					String line = lb.substring(0, lastWsPosn - 1);
 					sb.append(line);
 					sb.append(_lineSep);
 
 						// Get the rest of the line
 					String rest = lb.substring(lastWsPosn);
 
 						// Delete everything and retain only the 'rest'
 					lb.delete(0, numChars);
 					lb.append(rest);
 					numChars = rest.length();
 					lastWsPosn = 0;
 				}
 			}
 		}
 
 		sb.append(lb);
 		sb.append(_lineSep);
 		return sb.toString();
 	}
 
    /**
     * Returns the HTML tag that signals the beginning of the body text and
     * end of the preamble / header in the body text (see code for outputToFile)
     */
    public static String getHTMLTagSignallingEndOfPreamble() { return "h1"; }
 
 	private void outputToFile(StringBuffer data)
 	{
 		StringBuffer outBuf = new StringBuffer();
 		outBuf.append("<html>" + "\n" + "<head>\n");
 		outBuf.append("<title>" + _title + "</title>\n");
 			// Set output encoding to UTF-8
 		outBuf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
 		outBuf.append("</head>\n");
 		outBuf.append("<body>\n");
 		if (_url != null) {
 			outBuf.append("<h2 style=\"width:600px; font-size:20px; background:#ffeedd; color: red\">\n");
 			outBuf.append("The article was downloaded (and processed) at " + (new java.util.Date()) + " from:<br/>\n");
 			outBuf.append("<a href=\"" + _url + "\">" + _url + "</a></h2>\n");
 		}
 
          // Doing it this way ensures that code in Issue.java (Gen_JFLEX_RegExps) that
          // wants to find the end of the preamble in the output HTML
          // can work even if the tag is changed to something else
       String preambleEndTag = getHTMLTagSignallingEndOfPreamble();
 		outBuf.append("<" + preambleEndTag + ">" + _title + "</" + preambleEndTag + ">\n");
 		outBuf.append("<pre>\n" + prettyPrint(data) + "</pre>\n");
 		outBuf.append("</body>\n</html>\n");
 
 		if (_pw != null) {
 			_pw.println(outBuf);
 			_pw.flush();
 			if (_closeStream)
 				_pw.close();
 		}
 		else if (_os != null) {
 			try {
 				_os.write(outBuf.toString().getBytes("UTF-8"));
 				_os.flush();
 			}
 			catch (Exception e) {
             if (_log.isErrorEnabled()) _log.error("Error outputting data to output stream!");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void ignoreCharSetChanges(Parser p)
 	{
 		PrototypicalNodeFactory factory = new PrototypicalNodeFactory ();
 		factory.unregisterTag(new MetaTag());
 			// Unregister meta tag so that char set changes are ignored!
 		p.setNodeFactory (factory);
 	}
 
 	private static String parseNow(Parser p, HTMLFilter visitor) throws org.htmlparser.util.ParserException
 	{
 		try {
          //if (_log.isInfoEnabled()) _log.info("START encoding is " + p.getEncoding());
 			p.visitAllNodesWith(visitor);
 		}
 		catch (org.htmlparser.util.EncodingChangeException e) {
 			try {
 				if (_log.isInfoEnabled()) _log.info("Caught you! CURRENT encoding is " + p.getEncoding());
 				visitor.initFilter();
 				p.reset();
 				p.visitAllNodesWith(visitor);
 			}
 			catch (org.htmlparser.util.EncodingChangeException e2) {
 				if (_log.isInfoEnabled()) _log.info("CURRENT encoding is " + p.getEncoding());
 				if (_log.isInfoEnabled()) _log.info("--- CAUGHT you yet again! IGNORE meta tags now! ---");
 				visitor.initFilter();
 				p.reset();
 				ignoreCharSetChanges(p);
 				p.visitAllNodesWith(visitor);
 			}
 		}
 		//if (_log.isInfoEnabled()) _log.info("ENCODING IS " + p.getEncoding());
 		return p.getEncoding();
 	}
 
    /**
     * Extract text content from the file and return the content
     * @file  File from which the content needs to be extracted
     */
    public static StringBuffer getFilteredText(String file) throws Exception
    {
       HTMLFilter hf = new HTMLFilter(file);
 		hf.run();
       return hf._content;
    }
 
    /**
     * Extract text content from a string and returns it
     * @htmlString  String from which the content needs to be extracted
     */
    public static StringBuffer getFilteredTextFromString(String htmlString) throws Exception
    {
       HTMLFilter hf = new HTMLFilter();
       hf._outputToFile = false;
 		Parser parser = Parser.createParser(htmlString, "UTF-8");
 		parseNow(parser, hf);
       return hf._content;
    }
 
 	public static void main(String[] args) throws ParserException 
 	{
 		if (args.length == 0) {
 			System.out.println("USAGE: java HTMLFilter [-debug] [-o <output-dir>] [(-urllist <file>) OR (-filelist <file>) OR ((-u <url>) OR ([-url <url>] <file>))*]");
 			return;
 		}
 
 /* Record the output directory, and create it if it doesn't exist */
 		int    argIndex = 0;
 		String nextArg  = args[argIndex];
 		String outDir   = ".";
 		if (nextArg.equals("-debug")) {
 			_debug = true;
 			argIndex++;
 			nextArg = args[argIndex];
 		}
 		if (nextArg.equals("-o")) {
 			outDir = args[argIndex + 1];
 			argIndex += 2;
 			nextArg = args[argIndex];
 			File d = new File(outDir);
 			if (!d.exists())
 				d.mkdir();
 		}
 
 			/* Parse the other arguments .. the list of files/urls to be filtered */
 		if (nextArg.equals("-filelist") || nextArg.equals("-urllist")) {
 			boolean urls = nextArg.equals("-urllist");
 			try {
 				DataInputStream fileNameStream = new DataInputStream(new FileInputStream(args[argIndex+1]));
 				while (true) {
 					String line = fileNameStream.readLine();
 					if (line == null)
 						break;
 					try {
 						if (urls)
 							(new HTMLFilter(line, outDir, true)).run();
 						else
 							(new HTMLFilter(line, outDir, false)).run();
 					}
 					catch (Exception e) {
 						System.err.println("ERROR filtering " + line);
 						System.err.println("Exception: " + e);
 						e.printStackTrace();
 					}
 				}
 			}
 			catch (java.io.IOException e) {
 				System.err.println("IO exception " + e);
 			}
 		}
 		else {
 			for (int i = argIndex; i < args.length; i++) {
 				try {
 					boolean isUrl  = args[i].equals("-u");
 					if (isUrl) {
 						(new HTMLFilter(args[i+1], outDir, true)).run();
 						i++;
 					}
 					else {
 						if (args[i].equals("-url")) {
 //							System.out.println("URL - " + args[i+1] + "; fname - " + args[i+2]);
 							(new HTMLFilter(args[i+1], args[i+2], outDir)).run();
 							i += 2;
 						}
 						else {
 							(new HTMLFilter(args[i], outDir, false)).run();
 						}
 					}
 				}
 				catch (Exception e) {
 					if (args[i].equals("-u") || args[i].equals("-url"))
 						System.err.println("ERROR filtering " + args[i+1]);
 					else
 						System.err.println("ERROR filtering " + args[i]);
 					System.err.println("Exception: " + e);
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
