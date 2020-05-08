 package org.wonderly.doclets;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Stack;
 
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.MethodDoc;
 
 /**
  * Replace html/javadoc specific tags with latex commands.
  * Escapes special tex characters.
  * 
  * Supported HTML tags include the following
  * <dl>
  * <dt>&lt;dl&gt;
  * <dd>with the associated &lt;dt&gt;&lt;dd&gt;&lt;/dl&gt; tags
  * <dt>&lt;p&gt;
  * <dd>but not align=center...yet
  * <dt>&lt;br&gt;
  * <dd>but not clear=xxx
  * <dt>&lt;table&gt;
  * <dd>including all the associcated &lt;td&gt;&lt;th&gt;&lt;tr&gt;&lt;/td&gt;&lt;/th&gt;&lt;/tr&gt;
  * <dt>&lt;ol&gt;
  * <dd>ordered lists
  * <dt>&lt;ul&gt;
  * <dd>unordered lists
  * <dt>&lt;font&gt;
  * <dd>font coloring
  * <dt>&lt;pre&gt;
  * <dd>preformatted text
  * <dt>&lt;code&gt;
  * <dd>fixed point fonts
  * <dt>&lt;i&gt;
  * <dd>italized fonts
  * <dt>&lt;b&gt;
  * <dd>bold fonts
  * </dl>
  * 
  */
 public class HTMLToTex {
 
 	public static String convert(String input) {
 		HTMLToTex instance = new HTMLToTex();
 		return instance.convertToTex(input, null);
 	}
 
 	public static String convert(String input, MethodDoc md) {
 		HTMLToTex instance = new HTMLToTex();
 		return instance.convertToTex(input, md);
 	}
 	
 	private HTMLToTex() {
 	}
 
 	private String str;
 	private int pos;
 	private StringBuffer ret;
 	private String block = "";
 	private String refurl = "";
 	private String refimg = "";
 	private boolean collectBlock;
 	private int chapt = 0;
 	private int textdepth = 0;
 	private int verbat = 0;
 	private Stack<TableInfo> tblstk = new Stack<TableInfo>();
 	private Hashtable<String, String> colors = new Hashtable<String, String>(10);
 	private int colIdx = 0;
 	private TableInfo tblinfo = new TableInfo(null, null, "", 0);
 
 	/**
 	 * keeps track of contexts inside the html formatting Saves the apropriate
 	 * string to end the respective context. (i.e. in an
 	 * <p>
 	 * context the string
 	 * </p>
 	 * would be saved)
 	 */
 	private Stack<String> contexts = new Stack<String>();
 	private Stack<String> texContexts = new Stack<String>();
 
 	private void enter(String exitString, String exitTexString) {
 		contexts.push(exitString);
 		texContexts.push(exitTexString);
 	}
 
 	private void leave(String exitString) {
 		/* look for a matching context */
 		int i;
 		if (exitString.equals("")) {
 			i = 0;
 		} else {
 			for (i = contexts.size() - 1; i >= 0; --i) {
 				String context = contexts.get(i);
 				if (context.equals(exitString))
 					break;
 			}
 			if (i < 0) {
 				System.err.println("Warning: No matching opening tag for '"
 						+ exitString + "'");
 				System.err.println("Context: " + str);
 				return;
 			}
 		}
 
 		/* close all surrounding contexts */
 		while (contexts.size() > i) {
 			String tag = contexts.pop();
 			String tex = texContexts.pop();
 			if (i < contexts.size()) {
 				System.err
 						.println("Warning: Missing closing tag '" + tag + "'");
 				System.err.println("Context: " + str);
 			}
 			ret.append(tex);
 		}
 	}
 
 	private boolean startsWith(String needle) {
		if (pos + needle.length() >= str.length())
 			return false;
 		for (int i = 0; i < needle.length(); ++i) {
 			if (Character.toLowerCase(needle.charAt(i)) != Character.toLowerCase(str.charAt(pos + i)))
 				return false;
 		}
 		return true;
 	}
 
 	private boolean match(String needle) {
 		if (!startsWith(needle))
 			return false;
 		pos += needle.length() - 1; /*
 									 * hacky: the -1 compensates the ++i in the
 									 * for loop in convert...
 									 */
 		return true;
 	}
 
 	private void stackTable(Properties p, StringBuffer ret, String txt, int off) {
 		tblstk.push(tblinfo);
 		tblinfo = new TableInfo(p, ret, txt, off);
 	}
 
 	private String makeRefKey(String key) {
 		return key;
 	}
 
 	public void test(ArrayList<String> strings) {
 	}
 
 	private int labno = 0;
 	private Hashtable<String, String> refs = new Hashtable<String, String>();
 
 	private String refName(String key) {
 		String lab;
 		if ((lab = refs.get(key)) == null) {
 			lab = "l" + labno++;
 			refs.put(key, lab);
 		}
 		return lab;
 	}
 
 	private String convertToTex(String input, MethodDoc md) {
 		this.str = input;
 		ret = new StringBuffer();
 
 		++textdepth;
 		for (pos = 0; pos < str.length(); ++pos) {
 			char c = str.charAt(pos);
 			switch (c) {
 			case ' ':
 				if (verbat > 0) {
 					ret.append("\\phantom{ }");
 				} else {
 					ret.append(' ');
 				}
 				break;
 			case '_':
 			case '%':
 			case '$':
 			case '#':
 				ret.append('\\');
 				ret.append((char) c);
 				break;
 			case '^': /* { */
 				ret.append("$\\wedge$");
 				break;
 			case '}':
 				ret.append("$\\}$");
 				break;
 			case '{':
 				ret.append("$\\{$");
 				break;
 			case '<':
 				if (match("<pre>")) {
 					ret.append("\\texttt{");
 					verbat++;
 				} else if (match("</pre>")) {
 					verbat--;
 					ret.append("}\n");
 				} else if (str.length() > pos + 4
 						&& str.substring(pos, pos + 2).equalsIgnoreCase("<h") 
 						&& Character.isDigit(str.substring(pos+2, pos+3).charAt(0))) {
 					String headnum = str.substring(pos+2, pos+3);
 					ret.append(String.format("\\headref{%s}{", headnum));
 					enter(String.format("</h%s>", headnum), "}\n");
 					pos += 3;
 				} else if (str.length() > pos + 5
 						&& str.substring(pos, pos + 3).equalsIgnoreCase("</h") 
 						&& Character.isDigit(str.substring(pos+3, pos+4).charAt(0))) {
 					String headnum = str.substring(pos+3, pos+4);
 					leave(String.format("</h%s>", headnum));
 					pos += 4;
 				} else if (match("<html>")) {
 					enter("</html>", "");
 				} else if (match("</html>")) {
 					leave("</html>");
 					if (chapt > 0) {
 						ret.append("}");
 						--chapt;
 					}
 				} else if (match("<head>")) {
 					enter("<head>", "");
 				} else if (match("</head>")) {
 					leave("</head>");
 				} else if (match("<center>")) {
 					ret.append("\\begin{center}");
 					enter("</center>", "\\end{center}");
 				} else if (match("</center>")) {
 					leave("</center>");
 				} else if (startsWith("<meta")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 				} else if (match("<title>")) {
 					ret.append("\\chapter{");
 				} else if (match("</title>")) {
 					ret.append("}");
 				} else if (startsWith("<form")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 				} else if (match("</form>")) {
 					/* nothing */
 				} else if (startsWith("<input")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 				} else if (match("</input>")) {
 					/* nothing */
 				} else if (startsWith("<body")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 				} else if (match("</body>")) {
 					/* nothing */
 				} else if (match("<code>")) {
 					ret.append("\\texttt{");
 					enter("</code>", "}");
 				} else if (match("</code>")) {
 					leave("</code>");
 				} else if (match("</br>")) {
 					/* nothing */
 				} else if (match("<br>") || match("<br/>")) {
 					ret.append("\\texdocbr{}\n");
 				} else if (match("</p>")) {
 					leave("</p>");
 				} else if (match("<p>")) {
 					ret.append("\\begin{texdocp}");
 					enter("</p>", "\\end{texdocp}");
 				} else if (startsWith("<hr")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					String sz = p.getProperty("size");
 					int size = 1;
 					if (sz != null)
 						size = Integer.parseInt(sz);
 					ret.append("\\newline\\rule[2mm]{\\hsize}{"
 							+ (1 * size * .5) + "mm}\\newline\n");
 					pos = idx;
 				} else if (match("<tt>")) {
 					ret.append("\\texttt{");
 					enter("</tt>", "}");
 				} else if (match("</tt>")) {
 					leave("</tt>");
 				} else if (match("<b>")) {
 					ret.append("\\textbf{");
 					enter("</b>", "}");
 				} else if (match("</b>")) {
 					leave("</b>");
 				} else if (match("<i>")) {
 					ret.append("\\textit{");
 					enter("</i>", "}");
 				} else if (match("</i>")) {
 					leave("</i>");
 				} else if (match("<strong>")) {
 					ret.append("\\textbf{");
 					enter("</strong>", "}");
 				} else if (match("</strong>")) {
 					leave("</strong>");
 				} else if (match("<em>")) {
 					ret.append("\\textit{");
 					enter("</em>", "}");
 				} else if (match("</em>")) {
 					leave("</em>");
 				} else if (match("<i>")) {
 					ret.append("\\textit{");
 					enter("</i>", "}");
 				} else if (match("</i>")) {
 					leave("</i>");					
 				} else if (match("</img>")) {
 					/* nothing */
 				} else if (startsWith("<img")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 4);
 					refimg = p.getProperty("src");
 					ret.append("(see image at " + convert(refimg) + ")");
 					pos = idx;
 				} else if (match("</a>")) {
 					if (refurl != null) {
 						ret.append("} ");
 						if (refurl.charAt(0) == '#')
 							ret.append("\\refdefined{"
 									+ refName(makeRefKey(refurl.substring(1)))
 									+ "}");
 						else
 							ret.append("(at " + convert(refurl) + ")");
 					}
 				} else if (startsWith("<a")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					refurl = p.getProperty("href");
 					String refname = p.getProperty("href");
 					pos = idx;
 					if (refurl != null)
 						ret.append("{\\bf ");
 					else if (refname != null)
 						ret.append("\\label{" + refName(makeRefKey(refname))
 								+ "}");
 				} else if (startsWith("<ol")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 					ret.append("\\begin{enumerate}\n");
 				} else if (startsWith("<dl")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 					ret.append("\\begin{itemize}\n");
 				} else if (match("<li>")) {
 					ret.append("\\item ");
 				} else if (match("</li>")) {
 					/* ignore */
 				} else if (match("<dt>")) {
 					ret.append("\\item[");
 				} else if (match("<dd>")) {
 					ret.append("] ");
 				} else if (match("</dl>")) {
 					ret.append("\n\\end{itemize}\n");
 				} else if (match("</ol>")) {
 					ret.append("}\n\\end{enumerate}");
 				} else if (startsWith("<ul")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 3);
 					pos = idx;
 					ret.append("\\begin{itemize}");
 				} else if (match("</ul>")) {
 					ret.append("\\end{itemize}\n");
 				} else if (match("</table>")) {
 					tblinfo.endTable(ret);
 					tblinfo = tblstk.pop();
 				} else if (match("</th>")) {
 					tblinfo.endCol(ret);
 				} else if (match("</td>")) {
 					tblinfo.endCol(ret);
 				} else if (match("</tr>")) {
 					tblinfo.endRow(ret);
 				} else if (startsWith("<table")) {
 					Properties p = new Properties();
 					int idx = getTagAttrs(str, p, pos + 6);
 					pos = idx;
 					stackTable(p, ret, str, pos);
 				} else if (startsWith("<tr")) {
 					Properties p = new Properties();
 					int idx = getTagAttrs(str, p, pos + 3);
 					pos = idx;
 					tblinfo.startRow(ret, p);
 				} else if (startsWith("<tr")) {
 					Properties p = new Properties();
 					int idx = getTagAttrs(str, p, pos + 3);
 					pos = idx;
 					tblinfo.startCol(ret, p);
 				} else if (startsWith("<th")) {
 					Properties p = new Properties();
 					int idx = getTagAttrs(str, p, pos + 3);
 					pos = idx;
 					tblinfo.startHeadCol(ret, p);
 				} else if (startsWith("<font")) {
 					Properties p = new Properties();
 					int idx = HTMLToTex.getTagAttrs(str, p, pos + 5);
 					pos = idx;
 					String col = p.getProperty("color");
 					ret.append("{");
 					if (col != null) {
 						if ("redgreenbluewhiteyellowblackcyanmagenta"
 								.indexOf(col) != -1)
 							ret.append("\\color{" + col + "}");
 						else {
 							if ("abcdefABCDEF0123456789".indexOf(col.charAt(0)) != -1) {
 								Color cc = new Color((int) Long.parseLong(col,
 										16));
 								String name = colors.get("color" + cc.getRGB());
 								if (name == null) {
 									ret.append("\\definecolor{color" + colIdx
 											+ "}[rgb]{" + (cc.getRed() / 255.0)
 											+ "," + (cc.getBlue() / 255.0)
 											+ "," + (cc.getGreen() / 255.0)
 											+ "}");
 									name = "color" + colIdx;
 									colIdx++;
 									colors.put("color" + cc.getRGB(), name);
 								}
 								ret.append("\\color{" + name + "}");
 								++colIdx;
 							}
 						}
 					}
 
 				} else if (match("</font>")) {
 					ret.append("}");
 				} else {
 					ret.append("\\textless{}");
 				}
 				break;
 			case '\r':
 			case '\n':
 				if (tblstk.size() > 0) {
 					// Swallow new lines while tables are in progress,
 					// <tr> controls new line emission.
 					if (verbat > 0) {
 						ret.append("\\mbox{}\\newline\n");
 					} else
 						ret.append(" ");
 				} else {
 					if ((pos + 1) < str.length() && str.charAt(pos + 1) == 10) {
 						ret.append("\\texdocbr ");
 						++pos;
 					} else {
 						if (verbat > 0)
 							ret.append("\\mbox{}\\newline\n");
 						else
 							ret.append((char) c);
 					}
 				}
 				break;
 			case '/':
 				ret.append("$/$");
 				break;
 			case '&':
 				if (str.length() > pos + 4
 						&& str.substring(pos, pos + 2).equals("&#")) {
 					String it = str.substring(pos + 2);
 					int stp = it.indexOf(';');
 					if (stp > 0) {
 						String v = it.substring(0, stp);
 						int ch = -1;
 						try {
 							ch = Integer.parseInt(v);
 						} catch (NumberFormatException ex) {
 							ch = -1;
 						}
 						if (ch >= 0 && ch < 128) {
 							ret.append("\\verb" + ((char) (ch + 1))
 									+ ((char) ch) + ((char) (ch + 1)));
 						} else {
 							ret.append("\\&\\#" + v);
 						}
 						pos += v.length() + 2;
 					} else {
 						ret.append("\\&\\#");
 						pos++;
 					}
 				} else if (match("&amp;")) {
 					ret.append("\\&");
 				} else if (match("&nbsp;")) {
 					ret.append("\\phantom{ }");
 				} else if (match("&lt;")) {
 					ret.append("\\textless{}");
 				} else if (match("&gt;")) {
 					ret.append("\\textgreater{}");
 				} else {
 					ret.append("\\&");
 				}
 				break;
 			case '>':
 				ret.append("\\textgreater{}");
 				break;
 			case '\\':
 				ret.append("$\\backslash$");
 				break;
 			default:
 				ret.append((char) c);
 				break;
 			}
 		}
 		--textdepth;
 
 		/* leave all contexts */
 		leave("");
 
 		return ret.toString();
 	}
 
 	/**
 	 * This method parses HTML tags to extract the tag attributes and place them
 	 * into a Properties object.
 	 * 
 	 * @param str
 	 *            the string that is the whole HTML tag (at least)
 	 * @param i
 	 *            the offset in the string where the tag starts
 	 */
 	protected static int getTagAttrs(String str, Properties p, int i) {
 		// static Properties getTagAttrs( String str, int i ) {
 		String name = "";
 		String value = "";
 		int state = 0;
 		while (i < str.length()) {
 			switch (str.charAt(i)) {
 			case ' ':
 				if (state == 2) {
 					p.put(name.toLowerCase(), value);
 					state = 1;
 					name = "";
 					value = "";
 				} else if (state == 3) {
 					value += " ";
 				}
 				break;
 			case '=':
 				if (state == 1) {
 					state = 2;
 					value = "";
 				} else if (state > 1) {
 					value += '=';
 				}
 				break;
 			case '"':
 				if (state == 2) {
 					state = 3;
 				} else if (state == 3) {
 					state = 1;
 					p.put(name.toLowerCase(), value);
 					name = "";
 					value = "";
 				}
 				break;
 			case '>':
 				if (state == 1) {
 					p.put(name.toLowerCase(), "");
 				} else if (state == 2) {
 					p.put(name.toLowerCase(), value);
 				}
 				return i;
 			default:
 				if (state == 0)
 					state = 1;
 				if (state == 1) {
 					name = name + str.charAt(i);
 				} else {
 					value = value + str.charAt(i);
 				}
 			}
 			++i;
 		}
 		return i;
 	}
 }
