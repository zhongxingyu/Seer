 /**
 * $Id: HighlightTextTag.java,v 1.5 2006-07-26 14:50:51 pandyas Exp $ 
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/05/10 14:15:52  schroedn
 * New Features - Changes from code review
 *
  * Revision 1.3  2006/05/04 17:30:41  schroedn
  * Edit
  * 
  *  
  */
 
 package gov.nih.nci.camod.webapp.taglib;
 
 import gov.nih.nci.camod.Constants;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.BodyContent;
 import javax.servlet.jsp.tagext.BodyTag;
 import javax.servlet.jsp.tagext.Tag;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 /**
  * Custom tag for highlighting keyword searches
  * 
  * @arthur schroedn
  */
 public class HighlightTextTag implements Tag, BodyTag, Serializable
 {
    static private final Log log = LogFactory.getLog(HighlightTextTag.class);
    
     private static final long serialVersionUID = 5618297483211863400L;
     private PageContext myPageContext = null;
     private BodyContent myBodyContent;
     private Tag myParent = null;
     private String myKeyword;
 
     /**
      * @param inPageContext
      *            The inPageContext to set.
      */
     public void setPageContext(PageContext inPageContext)
     {
         myPageContext = inPageContext;
     }
 
     /**
      * @param inParent
      *            The inParent to set.
      */
     public void setParent(Tag inParent)
     {
         myParent = inParent;
     }
 
     /**
      * @return myParent
      *            Returns the myParent Tag.
      */
     public Tag getParent()
     {
         return myParent;
     }
 
     /**
      * Sets the keyword attribute. This is included in the tld file.
      * 
      * @jsp.attribute description="The keyword attribute is used to determine which words of the text to highlight
      * 
      * required="true"
      * rtexprvalue="false"
      */
     public void setKeyword(String inKeyword)
     {
         try
         {
             myKeyword = inKeyword;
         }
         catch (Exception e)
         {
             // ignore
         }
     }
 
     public void setBodyContent(BodyContent inBodyContent)
     {
         myBodyContent = inBodyContent;
     }
 
     public int doStartTag() throws JspException
     {
         return EVAL_BODY_BUFFERED;
     }
 
     public int doAfterBody() throws JspTagException
     {
 
         try
         {
 
             // Make sure we put anything in the output stream in the
             // body to the output stream of the JSP
 
             JspWriter out = myBodyContent.getEnclosingWriter();
             String theBody = myBodyContent.getString();
 
             myKeyword = (String) myPageContext.getSession().getAttribute(Constants.KEYWORD_HIGHLIGHT);
 
             try
             {
 
                if (myKeyword != null && !myKeyword.equals("") && !myKeyword.equals(" ") )
                 {
                     // Create a pattern to match cat
                     Pattern inPattern = Pattern.compile("(?i)(" + myKeyword + ")");
 
                     // Create a matcher with an input string
                     Matcher inMatcher = inPattern.matcher(theBody);
 
                     StringBuffer inStringBuffer = new StringBuffer();
 
                     boolean result = inMatcher.find();
 
                     // Loop through and create a new String 
                     // with the replacements
                     while (result)
                     {
                         inMatcher.appendReplacement(inStringBuffer, "<font class='keywordHighlight'>$1</font>");
                         result = inMatcher.find();
                     }
 
                     // Add the last segment of input to 
                     // the new String
                     inMatcher.appendTail(inStringBuffer);
 
                     theBody = inStringBuffer.toString();
                 }
 
             }
             catch (Exception e)
             {
                log.error("Error in HighlightTextTag.doAfterBody method", e);
             }
 
             out.println(theBody);
 
         }
         catch (IOException ioe)
         {
             throw new JspTagException("Error in ShortenTextTag tag doAfterBody " + ioe);
         }
 
         return SKIP_BODY;
     }
 
     public int doEndTag() throws JspException
     {
         return EVAL_PAGE;
     }
 
     public void doInitBody()
     {}
 
     public void release()
     {
         myParent = null;
     }
 }
