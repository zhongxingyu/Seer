 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.webapp;
 
 import java.io.IOException;
 
 import java.util.List;
 import java.util.Iterator;
 
 import javax.servlet.jsp.tagext.TagSupport;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.JspException;
 
 import org.apache.struts.util.RequestUtils;
 
 /**
  * Abbreviates a list and ends it with ellipses.
  *
  * @jsp:tag name="join"
  */
 public class JoinTag extends TagSupport
 {
     /**
      * Creates a new <code>JoinTag</code> instance.
      */
     public JoinTag()
     {
         super();
         mLimit = Integer.MAX_VALUE;
         mName = null;
         mProperty = null;
         mScope = null;
         mSeparator = ",";
         mEllipsis = "...";
         mNoEllipsis = false;
     }
     
     /**
     * The number to show of the list before the ellipses.
      * Defaults to 0 aka "infinite."
      *
      * @param limit the upper bounds on the number to show, 0 for "infinite"
      *
      * @jsp:attribute required="false" rtexprvalue="false"
      *                description="The upper limit of items to show"
      */
     public void setLimit(int limit)
     {
         if (limit == 0)
         {
             mLimit = Integer.MAX_VALUE;
         }
         else
         {
             mLimit = limit;
         }
     }
 
     /**
      * Set the bean name of the list, or the name the bean the list is a
      * property of.
      *
      * @param name the name of the bean
      *
      * @jsp:attribute required="true" rtexprvalue="false"
      *                description="name of bean"
      */
     public void setName(String name)
     {
         mName = name;
     }
 
     /**
      * Set the name of the property of the bean to use.
      *
      * @param property the name of the property
      *
      * @jsp:attribute required="false" rtexprvalue="false"
      *                description="name of property"
      */
     public void setProperty(String property)
     {
         mProperty = property;
     }
 
     /**
      * Sets the separator.  Default is a comma.
      *
      * @param separator the separator
      *
      * @jsp:attribute required="false" rtexprvalue="false"
      *                description="what is our separator"
      */
     public void setSeparator(String separator)
     {
         mSeparator = separator;
     }
 
     /**
      * Set the scope to seach for the bean in.
      *
      * @param scope the scope to look in
      *
      * @jsp:attribute required="false" rtexprvalue="false"
      *                description="scope to locate bean in"
      */
     public void setScope(String scope)
     {
         mScope = scope;
     }
 
     /**
      * Sets what the ellipsis should be.  Defaults to "..."
      *
      * @param ellipsis the ellipsis
      *
      * @jsp:attribute required="false" rtexprvalue="false"
      *                description="what should the ellipsis be"
      */
     public void setEllipsis(String ellipsis)
     {
         mEllipsis = ellipsis;
     }
 
     /**
      * Do not show the ellipsis.  Defaults to false.
      *
      * @param noEllipsis true or false
      *
      * @jsp:attribute required="false" rtexprvalue="false"
      *                description="do not show ellipsis"
      */
     public void setNoEllipsis(boolean noEllipsis)
     {
         mNoEllipsis = noEllipsis;
     }
 
     /**
      * helper function to locate our list
      *
      * @return the list or null
      *
      * @exception JspException if an error occurs
      */
     private List getList()
         throws JspException
     {
         List ourList = null;
         RequestUtils util = new RequestUtils();
 
         if (mProperty == null)
         {
             ourList = (List) util.lookup(pageContext, mName, mScope);
         }
         else
         {
             ourList =
                 (List) util.lookup(pageContext, mName, mProperty, mScope);
         }
 
         return ourList;
     }
 
     /**
      * Renders the list as asked.
      *
      * @return what to do after tag is started
      * @exception JspException if an error occurs
      */
     public int doStartTag()
         throws JspException
     {
         JspWriter out = pageContext.getOut();
         List list = getList();
         Iterator i = list.iterator();
         boolean firstDone = false;
         try
         {
             for (int count = 0; i.hasNext() && count < mLimit; count++)
             {
                 if (firstDone)
                 {
                     out.print(mSeparator + " ");
                 }
                 else
                 {
                     firstDone = true;
                 }
                 String item = (String) i.next();
                 out.print(item);
             }
 
             if ((list.size() > mLimit) && !mNoEllipsis)
             {
                 out.print(mSeparator + " " + mEllipsis);
             }
         }
         catch (IOException e)
         {
             throw new JspException(e);
         }
 
         return SKIP_BODY;
     }
 
     /** The upper limit to cutoff at */
     private int mLimit;
     /** The name of the bean */
     private String mName;
     /** the property on the bean */
     private String mProperty;
     /** the scope to find the bean in */
     private String mScope;
     /** the separator */
     private String mSeparator;
     /** the ellipsis */
     private String mEllipsis;
     /** do not show the ellipsis */
     private boolean mNoEllipsis;
 }
