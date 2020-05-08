 package TagSupport;
 
 import javax.servlet.jsp.tagext.TagSupport;
 
 public class InnerTag extends TagSupport {
   private String msg;
 
   public String getMsg() {
     return msg;
   }
 
   public void setMsg(String msg) {
     this.msg = msg;
   }
 
 //  public int doStartTag() throws JspException {
 //    String cid = getConnection();
 //
 //    if (cid != null) {
 //      // there is a connection id, use it
 //      connection = (Connection) pageContext.getAttribute(cid);
 //    } else {
 //      ConnectionTag ancestorTag = (ConnectionTag) findAncestorWithClass(this,
 //          ConnectionTag.class);
 //
 //      if (ancestorTag == null) {
 //        throw new JspTagException(
 //            "A query without a connection attribute must be nested within a connection tag.");
 //      }
 //
 //      connection = ancestorTag.getConnection();
 //    }
 //  }
 
 }
