 package gov.nih.nci.ncicb.cadsr.common.jsp.tag.handler;
 import gov.nih.nci.ncicb.cadsr.common.CaDSRConstants;
 import gov.nih.nci.ncicb.cadsr.common.jsp.bean.PaginationBean;
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.Tag;
 import javax.servlet.jsp.tagext.TagSupport;
 import javax.servlet.jsp.JspWriter;
 import gov.nih.nci.ncicb.cadsr.common.formbuilder.struts.common.FormConstants;
 
 /**
  * This TagHandler is use to display Pagenation for a collection
  * It is used in combination with struts iterate tag
  * Pagenation Tag displays "previous",next and dropdown to select a  page
  * Tag expects the specified instance of the pagenation bean to 
  * be in the session. "formIndex" if for the index of the form with the jsp.
  * "beanId" attribute is the key for the pagenation bean in the session.
  * Struts iterate tag uses the offset and the length specified in the PagenationBean in the session
  *  Ex.
  *  <cde:pagenation name="bottom" textClassName="OraFieldText" selectClassName="LOVField" formIndex="0" pageSize="40" 
  *                    beanId = "aBeanId" 
  *                    actionURL="/cdebrowser/pageAction.do"/>        
  *
  */
 public class Pagination extends TagSupport implements CaDSRConstants,FormConstants
 {
   
   private String listId;
   private String pageSize;
   private String actionURL;
   private String beanId;
   private String formIndex;
   private String name;
   private String selectClassName;
   private String textClassName;
   private String previousOnImage;
   private String previousOffImage;
   private String nextOnImage;
   private String nextOffImage;
   private String urlPrefix="";
   
   public Pagination()
   {   
   }
   
   public int doStartTag() throws javax.servlet.jsp.JspException {
     HttpServletRequest  req;
     JspWriter out;
      try {
         req = ( HttpServletRequest )pageContext.getRequest();
         PaginationBean pageBean = (PaginationBean)pageContext.getSession().getAttribute(this.beanId);  
         
         if(!pageBean.isInitialized())
         {
           pageBean.init(new Integer(pageSize).intValue());
         }
           
         out = pageContext.getOut();
         out.print(getTabeleHeader());
         
         if(pageBean.getPreviousPageIndex()!=-1)
         {
          out.print("<a href=\"javascript:"+name+"previous()\"><img src=\""+urlPrefix+previousOnImage+"\" border=\"0\" alt=\"Previous Page\">&nbsp;Previous&nbsp;"+pageSize+"</a>&nbsp;");
         }
         else
         {
           out.print("<img src=\""+urlPrefix+previousOffImage+"\" border=\"0\" alt=\"Previous Page\">&nbsp;");
         }
                                 
         out.print(generateSelectList(pageBean));   
         int nextSet =pageBean.getPageSize();
         if(pageBean.getOffset()+pageBean.getPageSize()>=pageBean.getListSize())
           nextSet=pageBean.getListSize()-pageBean.getOffset();
           
         if(pageBean.getNextPageIndex()!=-1)
         {
           out.print("<a href=\"javascript:"+name+"next()\">&nbsp;Next&nbsp;"+nextSet+"&nbsp;<img src=\""+urlPrefix+nextOnImage+"\" border=\"0\" alt=\"Next Page\"></a>");
         }
         else
         {
           out.print("<img src=\""+urlPrefix+nextOffImage+"\" border=\"0\" alt=\"Next Page\">&nbsp;");
         }
         
        out.print(getTableFooter());
       } catch( IOException ioe ) {
           throw new JspException( "I/O Error : " + ioe.getMessage() );
       }//end try/catch
       return Tag.SKIP_BODY;
 
     }//end doStartTag()
 
   public String getListId()
   {
     return listId;
   }
 
   public void setListId(String newListId)
   {
     listId = newListId;
   }
 
   public String getPageSize()
   {
     return pageSize;
   }
 
   public void setPageSize(String newPageSize)
   {
     pageSize = newPageSize;
   }
 
   private String generateJavaScript()
   {
     StringBuffer script = new StringBuffer("\n<SCRIPT LANGUAGE=\"JavaScript\"><!--");
     script.append(" \nfunction  "+name+"next() \n") ;
     script.append("\n {");
     script.append("\n document.location.href = \""+this.actionURL+"?method=next\"");
     script.append("\n}");
     script.append("\n function  "+name+"previous() ") ;
     script.append("\n {");
     script.append("\n document.location.href = \""+this.actionURL+"?method=previous\"");
     script.append("\n }");
     script.append("\n function  "+name+"setPageIndex(formindex) ") ;
     script.append("\n {");
     script.append("\n var pgNum = document.forms[formindex]."+name+".options[document.forms[formindex]."+name+".selectedIndex].value");
     script.append("\n document.location.href = \""+this.actionURL+"?method=setPageIndex&"+PAGEINDEX+"=\"+pgNum");
     script.append("\n }");    
     script.append("\n--> </SCRIPT>\n");
     return script.toString();
   }
   
   private String getTabeleHeader()
   {
      StringBuffer htmlStr = new StringBuffer(generateJavaScript());
      //StringBuffer htmlStr = new StringBuffer();
      htmlStr.append("<table width=\"100%\" align=\"center\" cellpadding=\"1\" cellspacing=\"1\" border=\"0\">");
      htmlStr.append("<tr> <td CLASS=\""+textClassName+"\" align=\"right\"> <td align=\"right\">");
     return htmlStr.toString();
   }
   
   private String getTableFooter()
   {
      StringBuffer htmlStr = new StringBuffer("</td> </tr> </table>");
      return htmlStr.toString();
   }
  private String generateSelectList(PaginationBean pageBean)
  {
     StringBuffer htmlStr = new StringBuffer();
     htmlStr.append("<SELECT NAME="+name+" CLASS=\""+selectClassName+"\" onChange = \""+name+"setPageIndex('"+formIndex +"');\">");
     int counted = 0;
     int listSize = pageBean.getListSize();
     int localIndex = 1;
     int recIndex = 0;
     String selectedStr = " ";
     boolean selected = false;
     for(int i=1;(i+pageBean.getPageSize()-1)<listSize;i=i+pageBean.getPageSize())
     { 
        if(pageBean.getCurrentPageIndex()==localIndex)
        {
          selectedStr="SELECTED";
          selected=true;
        }      
       else
        {
         selectedStr=" ";
        }
       recIndex = i+pageBean.getPageSize()-1;
       htmlStr.append("<OPTION "+selectedStr+"  VALUE=\""+localIndex+"\">"+i+" - "+recIndex+" of "+listSize+"</OPTION>");      
       localIndex++;      
     }
     if(!selected)
       selectedStr=" SELECTED ";
     else
       selectedStr=" ";
     recIndex++;
     if(recIndex<= listSize)
       {
         //String len = new Integer(listSize-1).toString();
         htmlStr.append("<OPTION "+selectedStr+"  VALUE=\""+localIndex+"\">"+recIndex+" - "+listSize+" of "+listSize+"</OPTION>");
       }
       
     
     htmlStr.append("</SELECT>&nbsp;");
     
     return htmlStr.toString();
  }
 
   public String getActionURL()
   {
     return actionURL;
   }
 
   public void setActionURL(String newActionURL)
   {
     actionURL = newActionURL;
   }
 
   public String getBeanId()
   {
     return beanId;
   }
 
   public void setBeanId(String newBeanId)
   {
     beanId = newBeanId;
   }
 
   public String getFormIndex()
   {
     return formIndex;
   }
 
   public void setFormIndex(String newFormIndex)
   {
     formIndex = newFormIndex;
   }
 
   public String getName()
   {
     return name;
   }
 
   public void setName(String newName)
   {
     name = newName;
   }
 
   public String getSelectClassName()
   {
     return selectClassName;
   }
 
   public void setSelectClassName(String newSelectClassName)
   {
     selectClassName = newSelectClassName;
   }
 
   public String getTextClassName()
   {
     return textClassName;
   }
 
   public void setTextClassName(String newTextClassName)
   {
     textClassName = newTextClassName;
   }
 
   public String getPreviousOnImage()
   {
     return previousOnImage;
   }
 
   public void setPreviousOnImage(String newPreviousOnImage)
   {
     previousOnImage = newPreviousOnImage;
   }
 
   public String getPreviousOffImage()
   {
     return previousOffImage;
   }
 
   public void setPreviousOffImage(String newPreviousOffImage)
   {
     previousOffImage = newPreviousOffImage;
   }
 
   public String getNextOnImage()
   {
     return nextOnImage;
   }
 
   public void setNextOnImage(String newNextOnImage)
   {
     nextOnImage = newNextOnImage;
   }
 
   public String getNextOffImage()
   {
     return nextOffImage;
   }
 
   public void setNextOffImage(String newNextOffImage)
   {
     nextOffImage = newNextOffImage;
   }
 
   public String getUrlPrefix()
   {
     return urlPrefix;
   }
 
   public void setUrlPrefix(String newUrlPrefix)
   {
     urlPrefix = newUrlPrefix;
   }
 
 }
