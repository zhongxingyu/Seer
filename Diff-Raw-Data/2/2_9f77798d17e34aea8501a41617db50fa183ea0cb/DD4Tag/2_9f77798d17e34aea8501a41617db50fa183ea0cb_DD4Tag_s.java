 package com.digitald4.common.tld;
 
 import java.util.StringTokenizer;
 
 import javax.servlet.jsp.tagext.TagSupport;
 
 public abstract class DD4Tag extends TagSupport {
 	/**
 	 * doStartTag is called by the JSP container when the tag is encountered
 	 */
 	public int doStartTag() {
 		try {
 			pageContext.getOut().write(getOutput());
 		} catch (Exception e) {
 			throw new Error(e.getMessage());
 		}
 		// Must return SKIP_BODY because we are not supporting a body for this tag.
 		return SKIP_BODY;
 	}
 	
 	public abstract String getOutput() throws Exception;
 	
 	public String getOutputIndented() throws Exception {
 		String ret = "";
 		String out = getOutput().replaceAll("\n", "").replaceAll("\t", "");
 		int tabs = 0;
 		StringTokenizer st = new StringTokenizer(out,">");
 		while (st.hasMoreTokens()) {
 			String tag = st.nextToken().trim()+">";
 			if (!tag.startsWith("<")) {
 				for (int t=0; t<tabs; t++) {
 					ret += "\t";
 				}
 				ret += tag.substring(0, tag.indexOf('<')) + "\n";
 				tag = tag.substring(tag.indexOf('<'));
 			}
 			if (tag.contains("</")) {
 				tabs--;
 			}
 			for (int t=0; t<tabs; t++) {
 				ret += "\t";
 			}
			if (!tag.contains("/")) {
 				tabs++;
 			}
 			ret += tag + "\n";
 		}
 		return ret;
 	}
 	
 	/**
 	 * doEndTag is called by the JSP container when the tag is closed
 	 */
 	public int doEndTag(){
 		return 0;
 	}
 }
