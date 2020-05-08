 package nl.nikhef.xhtmlrenderer.swing;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /** A Document that is based on a template.
  * <p>
  * The constructor receives a template Document, and this object contains the resulting 
  * Document. Each call to {@link #refresh} or {@link #setData} re-parses the
  * template Document. When you call {@link #data()}.{@link Properties#setProperty setProperty}
  * directly, you need to call {@linkplain #refresh} yourself.
  * <p>
  * <strong>Template language</strong>
  * <p>
  * Templates are valid XML (which is required because the template itself is
  * parsed into a Document). There are two extra attributes, <code>if</code> for
  * conditionals, and <code>c</code> for replacement.
  * <p>
  * <strong>Replacement</strong>
  * <p>
  * When the attribute <code>c</code> is present on any Node, the contents of
  * that node is replaced by the value of the attribute. The value can be XHTML
  * itself.<br>
  * Example:
  * <code>&lt;p&gt;Please look at &lt;a href="${url}" c="the url ${url}"/&gt; for details.&lt;/p&gt;</code><br>
  * This will make a link to the URL specified by property <code>url</code> with 
  * the description "<i>the url ...</i>" (with the dots replaced by the actual url).
  * <p>
  * It is also possible extract a portion of a string by square bracket notation. For example,
  * when property <code>os.name</code> is equal to <code>Windows NT</code>, one can retrieve
  * the first three characters by referencing the variable with <code>${os.name[0:3]}</code>.
  * If the first number is omitted, zero is used; if the last number is omitted, the length
  * of the string is used.
  * <p>
  * <strong>Conditionals</strong>
  * <p>
  * When the attribute <code>if</code> is present on any Node, that node is
  * only retained when the expression evaluates to true, according to:<ul>
  *   <li>the literal <code>true</code> evaluates to true
  *   <li>the literal <code>false</code> evaluates to false
  *   <li>the empty string evaluates to false
  *   <li>any other non-empty string evaluates to true
  * </ul>
  * Some basic expressions can be used:
  * <ul>
  *   <li><code>!<i>expression</i></code> for negation
  *   <li><code><i>expression</i> and <i>expression</i></code> for the boolean and operation
  *   <li><code><i>expression</i> or <i>expression</i></code> for the boolean or operation
  *   <li><code>(<i>subexpression</i>)</code> to evaluate subexpressions, as usual
  *   <li><code><i>string1</i> == <i>string2</i></code> to check if two strings are equal (whitespace is trimmed)</li>
  *   <li><code><i>string1</i> != <i>string2</i></code> to check if two strings are unequal (whitespace is trimmed)</li>
  * </ul>
  * <br>
  * Example:
  * <code>&lt;p if="${url} and ${desc}"&gt;Visit &lt;a href="${url}" c="${desc}"/&gt;&lt;/p&gt;</code><br>
  * to only show a link with description if both relevant variables are defined.
  * <p>
  * <strong>Replacement with conditional</strong>
  * <p>
  * It is also possible to use a conditional with a replacement by using
  * <code>${(&lt;expression&gt;)}</code> as a variable in an attribute. This can be
  *  used, for example, to change the class of an element based on whether a variable
  * is set or not: <code>&lt;p class="has-${(${foo})}"&gt;yeah&lt;/p&gt;</code>, which
  * can be styled by css class {@code has-true} when {@code foo} is set, or
  * {@code has-false} when {@code foo} is unset.
  * <p>
  * <strong>Other remarks</strong>
  * <p>
  * When the property <tt><i>something</i>.lock</tt> is defined and an element with the
  * attribute <code>name="<i>something</i>"</code> is present, its readonly attribute
  * will be set automatically. Its use is explained in {@link TemplatePanel}.
  * <p>
 * When a propery cannot be found, it is looked up with {@link System#getProperty}. If
  * that fails, {@code null} is assumed as its value.
  * 
  * @author wvengen
  */
 public class TemplateDocument extends DocumentDelegate {
     
     /** The template from which this document is derived */
     private Document template = null;
     /** Properties for parsing the template */
     private Properties data = null;
     
     /** Create a new document from a template with empty data. */
     public TemplateDocument(Document template) {
 	this(template, new Properties());
     }
     /** Create a new document from a template and directly parse from properties. */
     public TemplateDocument(Document template, Properties data) {
 	super();
 	this.template = template;
 	setData(data);
     }
     
     /** Set the properties to use for the template.
      * 
      * @param p New properties to set 
      */
     public void setData(Properties p) {
 	data = p;
 	refresh();
     }
     
     /** Return the properties. */
     public Properties data() {
 	return data;
     }
     
     /** Rebuild the document from its template and properties */
     public void refresh() {
 	try {
 	    // clone the template document
 	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	    Document newDoc = factory.newDocumentBuilder().newDocument();
 	    newDoc.appendChild(newDoc.importNode(template.getFirstChild(), true));
 	    // copy properties from the template document at first, but copy from
 	    // the previously parsed document on subsequent refreshes. This allows
 	    // the user to set attributes without losing them after a refresh.
 	    Document propsSrc = this;
 	    if (!isValid()) propsSrc = template;
 	    if (propsSrc.getDocumentURI()!=null)
 		newDoc.setDocumentURI(propsSrc.getDocumentURI());
 	    newDoc.setStrictErrorChecking(propsSrc.getStrictErrorChecking());
 	    newDoc.setXmlStandalone(propsSrc.getXmlStandalone());
 	    newDoc.setXmlVersion(propsSrc.getXmlVersion());
 	    // and run the template
 	    setDocument(newDoc);
 	    doTemplate(this);
 	} catch (ParserConfigurationException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
     }
     
     /** Processes an XML Node to apply the template recursively. */
     protected void doTemplate(Node node) {
 	if (node.getNodeType() == Node.ELEMENT_NODE) {
 	    // expand variables in attributes
 	    NamedNodeMap attrs = node.getAttributes();
 	    for (int i=0; i<attrs.getLength(); i++) {
 		attrs.item(i).setNodeValue(parseExpression(attrs.item(i).getNodeValue()));
 	    }
 	    // apply "if" attributes
 	    Node ifNode = attrs.getNamedItem("if");
 	    if (ifNode!=null && !parseConditional(ifNode.getNodeValue())) {
 		node.getParentNode().removeChild(node);		
 		return;
 	    }
 	    // replace contents of "c" attributes
 	    Node cNode = attrs.getNamedItem("c");
 	    if (cNode!=null) {
 		// erase existing contents
 		NodeList nl = node.getChildNodes();
 		for (int i=0; i<nl.getLength(); i++)
 		    node.removeChild(nl.item(i));
 		node.setTextContent(null);
 		/* and add new content; it might have been nice to do
 		 *    node.appendChild(node.getOwnerDocument().createTextNode(cNode.getNodeValue()));
 		 * but that doesn't allow one to put html in variables. So a
 		 * new html document is created from the parsed node, and the
 		 * resulting html is put into the original document.
 		 * 
 		 * to make adoptNode() work, the following workaround is used
 		 *   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4915524
 		 * 
 		 * TODO use DOM implementation from node for parsing */ 
 		try {
 		    byte[] data = ("<root>"+cNode.getNodeValue()+"</root>").getBytes();
 		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		    factory.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);
 		    Document newDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(data));
 		    newDoc.normalize();
 		    NodeList newChildren = newDoc.getFirstChild().getChildNodes();
 		    while(newChildren.getLength()>0)
 			node.appendChild(node.getOwnerDocument().adoptNode(newChildren.item(0)));
 		} catch (SAXException e) {
 		    // TODO Auto-generated catch block
 		    e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 		    // TODO Auto-generated catch block
 		    e.printStackTrace();
 		} catch (IOException e) {
 		    // TODO shouldn't happen
 		    e.printStackTrace();
 		}
 	    }
 	    // readonly not implemented by xhtmlreader itself but disabled is
 	    // also if property lock.<name> is set make it readonly
 	    Node name = attrs.getNamedItem("name");
 	    Node rdNode = attrs.getNamedItem("readonly");
 	    if (rdNode!=null ||
 		    (name!=null && Boolean.valueOf(data().getProperty(name.getNodeValue()+".lock"))) ) {
 		Node attr = node.getOwnerDocument().createAttribute("disabled");
 		attr.setNodeValue("disabled");
 		attrs.setNamedItem(attr);
 	    }
 	}
 	// recursively parse children
 	NodeList nl = node.getChildNodes();
 	for (int i=0; i<nl.getLength(); i++) {
 	    doTemplate(nl.item(i));
 	}
     }
 
     /** returns the result of a boolean expression */
     // TODO more intelligent expression parsing, e.g. using JUEL
     protected boolean parseConditional(String expr) {
 	if (expr==null) return true;
 	expr = expr.trim();
 	// add parentheses to boolean operators
 	//expr = expr.replaceAll("^(.*\\b)(and|or)(\\b.*)$", "($1)$2($3)");
 	// parse subexpressions within parentheses
 	final Pattern paren = Pattern.compile("(\\(.*\\))");
 	Matcher matcher = paren.matcher(expr);
 	StringBuffer newExpr = new StringBuffer();
 	while (matcher.find()) {
 	    String subExpr = matcher.group();
 	    if (subExpr.trim().equals("")) {
 		matcher.appendReplacement(newExpr, "");
 	    } else {
 		subExpr = subExpr.substring(1, subExpr.length()-1);
 		matcher.appendReplacement(newExpr, Boolean.toString(parseConditional(subExpr)));
 	    }
 	}
 	matcher.appendTail(newExpr);
 	expr = newExpr.toString().trim();
 	// parse boolean operations
 	final Pattern ops = Pattern.compile("^(.*?)\\b(and|or)\\b(.*)$");
 	while ( (matcher=ops.matcher(expr)).find() ) {
 	    boolean pre = parseConditional(matcher.group(1));
 	    String op = matcher.group(2);
 	    boolean post = parseConditional(matcher.group(3));
 	    if (op.equals("and"))
 		expr = Boolean.toString(pre && post);
 	    else if (op.equals("or"))
 		expr = Boolean.toString(pre || post);
 	}
 	// parse string comparison
 	final Pattern comp = Pattern.compile("^(.*?)(==|!=)(.*)$");
 	matcher = comp.matcher(expr);
 	if (matcher.find()) {
 	    boolean val = (matcher.group(1).trim().equals(matcher.group(3).trim()));
 	    if (matcher.group(2).equals("!="))
 		val = !val;
 	    expr = Boolean.toString(val);
 	}
 	// handle negations
 	if (expr.startsWith("!"))
 	    return !parseConditional(expr.substring(1));
 	if (expr.equals("true")) return true;
 	if (expr.equals("false")) return false;
 	return !expr.equals("");
     }
     /** evaluates an expression by replacing variables */
     // TODO more intelligent expression parsing, e.g. using JUEL
     protected String parseExpression(String expr) {
 	// parse embedded conditionals
 	StringBuffer dstbufCond = new StringBuffer();
 	final Pattern patCond = Pattern.compile("(\\$\\{\\((.*?)\\)\\})", Pattern.MULTILINE|Pattern.DOTALL);
 	Matcher matchCond = patCond.matcher(expr);
 	while (matchCond.find()) {
 	    String key = matchCond.group(2).trim();
 	    String sub = Boolean.toString(parseConditional(parseExpression(key)));
 	    matchCond.appendReplacement(dstbufCond, sub);
 	}
 	matchCond.appendTail(dstbufCond);
 	
 	// substitute variables
 	StringBuffer dstbuf = new StringBuffer();
 	final Pattern pat = Pattern.compile("(\\$\\{(.*?)(\\[([0-9]*):([0-9]*)\\])?\\})", Pattern.MULTILINE|Pattern.DOTALL);
 	Matcher match = pat.matcher(dstbufCond.toString());
 	while (match.find()) {
 	    String key = match.group(2).trim();
 	    String sub = null;
 	    if (data!=null) sub = data.getProperty(key);
 	    if (sub==null) sub = System.getProperty(key);
 	    if (sub==null) sub="";
 	    // handle substring
 	    if (match.group(3)!=null) {
 		int from = 0;
 		int to = sub.length();
 		try { from = Integer.parseInt(match.group(4)); } catch(Exception e) { }
 		try { to = Integer.parseInt(match.group(5)); } catch(Exception e) { }
 		try { sub = sub.substring(from, to); } catch(Exception e) { }
 	    }
 	    sub = sub.replaceAll("([$\\\\])", "\\\\$1"); // need escaping in replacement string
 	    match.appendReplacement(dstbuf, sub);
 	}
 	match.appendTail(dstbuf);
 	return dstbuf.toString();
     }
 }
