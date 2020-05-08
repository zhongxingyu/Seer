 package at.rovo.parser;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 /**
  * <p>Represents a HTML or XML tag which starts with a '&lt;' character
  * and ends with a '>' character.</p>
  * <p>This class does not provide any methods to retrieve any attribute
  * informations of the element like the href attribute in &lt;a href="...">
  * or the class attribute in &lt;div class="..."> tags.</p>
  * 
  * @author Roman Vottner
  */
 public class Tag extends Token
 {
 	/** Used by TSReC parser. Will contain list of appended tokens that will 
 	 * appear as sub-elements of this tokens. This are not exactly children 
 	 * per se as TSReC will append Words and Tags that do not break the text
 	 * flow. **/
 	private List<Token> appendedTokens = new ArrayList<Token>(); 
 	/** will contain the attributes of this tag **/
 	private Hashtable<String, String> attributes = null;
 	
 	/**
 	 * <p>Initializes a new instance of a HTML tag. The parameter is the full 
 	 * content of this tag. This means if you need to specify f.e. a div element 
 	 * with a class attribute you can instantiate a new tag like follows:</p>
 	 * <code>Tag div = new Tag("&lt;div class=\"...\">");</code>
 	 * <p>or start a tag and append the rest later via invoking 
 	 * {@link #append(String)}</p>
 	 * <code>Tag div = new Tag("&lt;div");
 	 * <br/>div.append(" class=\"...\">");</code>
 	 * 
 	 * @param text Content of the tag
 	 */
 	public Tag(String text)
 	{
 		super(text);
 	}
 	
 	public Tag(int id, String name, int parent, int numSiblings, int level)
 	{
 		super(id, name, null, level, parent, numSiblings);
 	}
 	
 	/**
 	 * <p>Creates a new instance of a tag through deep copying the data of the
 	 * other tag.</p>
 	 * 
 	 * @param node The tag to copy
 	 */
 	public Tag(Token node)
 	{
 		super(node);
 		
 		if (node != null)
 		{
 			// deep copy
 			if (node.matchedNode != null)
 			{
 				if (node.matchedNode instanceof Word)
 					this.matchedNode = new Word((Word)node.matchedNode);
 				else
 					this.matchedNode = new Tag((Tag)node.matchedNode);
 			}
 		}
 	}
 	
 	public Tag(Tag node)
 	{
 		super(node);
 		if (node != null)
 		{
 			if (node.matchedNode != null)
 				this.matchedNode = new Tag((Tag)node.matchedNode);
 		}
 	}
 	
 	/**
 	 * <p>Appends further text to a tag unless it is not closed yet.
 	 * You can test if a tag is already closed by invoking the 
 	 * {@link #isClosed()} method.</p>
 	 * 
 	 * @param text The text which should be added to the tag. Note 
 	 *             that appending occurs inside the '&lt;' and '>'
 	 *             parts of the tag and not outside!
 	 * @return The current content of the tag
 	 */
 	String append(String text)
 	{
 		if (!this.isValid())
 			this.html = this.html+" "+text;
 		return this.html;
 	}
 	
 	/**
 	 * <p>Adds a token as a sub element to this tag.</p>
 	 * 
 	 * @param token The token to add as a sub element to this tag
 	 */
 	void append(Token token)
 	{
 		this.appendedTokens.add(token);
 	}
 	
 	/**
 	 * <p>Returns all sub elements for this tag. Note that this elements are
 	 * only available for a TSReC parser.</p>
 	 * 
 	 * @return The sub elements for this tag
 	 */
 	public List<Token> getSubElements()
 	{
 		return this.appendedTokens;
 	}
 	
 	/**
 	 * <p>Determines if this tag is valid or not.</p>
 	 * <p>A tag is valid if it matches either of the following  patterns:</p>
 	 * <ul>
 	 * <li><code>&lt;!-- ... --></code></li>
 	 * <li><code>&lt;![ ... ]]></code></li>
 	 * <li><code>&lt; ... ></code></li>
 	 * </ul>
 	 * <p>Note that this method does not check for tags inside  of comments!</p>
 	 * <p>If a tag is valid and not a comment, its attributes are extracted 
 	 * automatically.</p>
 	 * 
 	 * @return true if the tag is valid; false otherwise
 	 */
 	boolean isValid()
 	{
 		boolean ret = false;
 		if (this.html.startsWith("<!--") && this.html.endsWith("-->"))
 			ret = true;
 		else if (this.html.startsWith("<![") && this.html.endsWith("]]>"))
 			ret = true;
 		else if (!this.html.startsWith("<!--") && this.html.endsWith(">"))
 			ret = true;
 		
 		if (ret && !this.html.endsWith("-->") && !this.html.endsWith("]]>") && 
 				this.attributes == null)
 		{
			// remove the ending signs of the token
 			String html = this.html;
 			if (html.endsWith("/>"))
 				html = html.substring(0, html.length()-2);
 			else if (html.endsWith(">"))
 				html = html.substring(0, html.length()-1);
 			
 			// split the attributes into tokens
 			String[] tokens = html.split(" ");
 			this.attributes = new Hashtable<String, String>();
 			String currArg = null;
 			// omit the first token as it is the tag-name
 			for (int i=1; i<tokens.length; i++)
 			{
 				// check if the token has an assignment-character
 				if (tokens[i].contains("=\""))
 				{
 					String[] arg = tokens[i].split("=\"");
					this.attributes.put(arg[0], checkToken(arg[1]));
 					currArg = arg[0];
 				}
 				// attribute is split up into multiple tokens, join them as long
 				// as no ending quote is reached
 				else if (currArg != null)
 				{
 					String val = this.attributes.get(currArg)+" "+tokens[i];
 					this.attributes.put(currArg, checkToken(val));
 					
 					if (val.endsWith("\""))
 						currArg = null;
 				}
 			}			
 		}
 		return ret;
 	}
 	
 	/**
 	 * <p>Removes characters that indicate a tag ending.</p>
 	 * 
 	 * @param value The token to check for tag-endings
 	 * @return The corrected token if it contained a tag ending symbol, else the
 	 *         origin token
 	 */
 	private static String checkToken(String value)
 	{
 		if (value.endsWith("/>"))
 			value = value.substring(0, value.length()-2);
 		else if (value.endsWith(">"))
 			value = value.substring(0, value.length()-1);
 		
 		return value;
 	}
 	
 	/**
 	 * <p>Returns all defined attributes and their content for this tag.</p>
 	 * 
 	 * @return All defined attributes including their content
 	 */
 	public Map<String, String> getAttributes()
 	{
 		return this.attributes;
 	}
 	
 	/**
 	 * <p>Returns the content for a certain attribute.</p>
 	 * 
 	 * @param attribute The attribute whose content should be returned
 	 * @return The content of the specified attribute
 	 */
 	public String getAttribute(String attribute)
 	{
 		if (this.attributes.contains(attribute))
 			return this.attributes.get(attribute);
 		return "";
 	}
 	
 	/**
 	 * <p>Adds or replaces an existing attribute with the provided content.</p>
 	 * 
 	 * @param attribute The attribute to add or modify
 	 * @param content The content of the attribute to add or modify
 	 */
 	public void setAttribute(String attribute, String content)
 	{
 		this.attributes.put(attribute, content);
 	}
 	
 	/**
 	 * <p>Determines if the tag includes an inline closing 
 	 * symbol &lt;... />, f.e. &lt;div /> or &lt;br/>.</p>
 	 * 
 	 * @return true if the tag contains an inline closing
 	 *              symbol; false otherwise
 	 */
 	public boolean isInlineCloseingTag()
 	{
 		if (this.html.endsWith("/>"))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * <p>Determines if this tag is a opening tag</p>
 	 * <p>Opening tags do no include any of the following 
 	 * patterns:</p>
 	 * <ul>
 	 * <li><code>&lt;/...></code>, f.e. <code>&lt;/div></code></li>
 	 * <li><code>--></code></li>
 	 * <li><code>]]></code></li>
 	 * </ul>
 	 * 
 	 * @return true if this tag is an opening tag; false
 	 *              otherwise
 	 */
 	public boolean isOpeningTag()
 	{
 		if (this.html.startsWith("</") || this.html.endsWith("-->") || this.html.endsWith("]]>"))
 			return false;
 		return true;
 	}
 	
 	/**
 	 * <p>Determines if this tag is a comment. A comment 
 	 * matches either of the following patterns:</p>
 	 * <ul>
 	 * <li><code>&lt;!-- ... --></code></li>
 	 * <li><code>&lt;![ ... ]]></code></li>
 	 * </ul>
 	 * 
 	 * @return true if this tag is a comment; false
 	 *              otherwise
 	 */
 	public boolean isComment()
 	{
 		if (this.html == null)
 			return false;
 		if (this.html.startsWith("<!--") || this.html.endsWith("-->"))
 			return true;
 		if (this.html.startsWith("<![") || this.html.endsWith("]]>"))
 			return true;
 		return false;
 	}
 
 	/**
 	 * <p>Reflects the type of HTML tag this tag represents</p>
 	 * <p>The type of an HTML tag is the first part of the tag, f.e.
 	 * the type of <code>&lt;div class="..."></code> will be 'div'</p>
 	 * <ul>
 	 * <li><code>&lt;div class="..."></code> return 'div'</li>
 	 * <li><code>&lt;/p></code> returns 'p'</li>
 	 * <li><code>&lt;!-- ... --></code> returns ''</li>
 	 * </ul>
 	 * <p>As from the examples above can be seen, comments to not
 	 * return any short tag!</p>
 	 * 
 	 * @return Returns the type of HTML tag this tag corresponds to
 	 */
 	public String getShortTag()
 	{
 		String shortTag ="";
 		// closing tags
 		if (this.html.startsWith("</"))
 		{
 			shortTag = this.html.substring(2);
 			if (shortTag.endsWith(">"))
 				shortTag = shortTag.substring(0, shortTag.indexOf(">"));
 			if (shortTag.contains(" "))
 				shortTag = shortTag.substring(0, shortTag.indexOf(" "));
 			if (shortTag.contains("."))
 				shortTag = shortTag.substring(0, shortTag.indexOf("."));
 			if (shortTag.contains(":"))
 				shortTag = shortTag.substring(0, shortTag.indexOf(":"));
 			// should not happen that a tag contains " - but unfortunately it does
 			if (shortTag.contains("\""))
 				shortTag = shortTag.substring(0, shortTag.indexOf("\""));
 		}
 		// opening tags
 		else if (this.html.length() > 1 && this.html.startsWith("<") 
 				/*&& this.html.charAt(1) != '!' && this.html.charAt(1) != '['*/)
 		{
 			if (this.html.startsWith("<!--"))
 				return "<!--";
 			// remove leading <
 			shortTag = this.html.substring(1);
 			if (shortTag.contains(" "))
 				shortTag = shortTag.substring(0, shortTag.indexOf(" "));
 			if (shortTag.contains("."))
 				shortTag = shortTag.substring(0, shortTag.indexOf("."));
 			if (shortTag.contains(":"))
 				shortTag = shortTag.substring(0, shortTag.indexOf(":"));
 			if (shortTag.contains(">"))
 				shortTag = shortTag.substring(0, shortTag.indexOf(">"));
 			// should not happen that a tag contains " - but unfortunately it does
 			if (shortTag.contains("\""))
 				shortTag = shortTag.substring(0, shortTag.indexOf("\""));
 			// remove inline closings
 			if (shortTag.endsWith("/"))
 				shortTag = shortTag.substring(0, shortTag.indexOf("/"));
 		}
 		return shortTag;
 	}
 	
 	/**
 	 * <p>Renames a tag to &lt;UNKNOWN> if it is an opening tag or
 	 * to &lt;/UNKNOWN> if it is a closing tag.</p>
 	 */
 	public void setAsUndefined()
 	{
 		if (this.isOpeningTag())
 			this.html = "<unknown>";
 		else
 			this.html = "</unknown>";
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		int result = 17;
 		result = 31 * result + this.html.hashCode();
 		return result;
 	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		Tag tag;
 		if (obj instanceof Tag)
 		{
 			tag = (Tag) obj;
 		} 
 		else 
 		{
 			return false;
 		}
 
 		if (!(this.html.equalsIgnoreCase(tag.html)))
 		{
 			return false;
 		}
 
 		return true;
 	}
 }
