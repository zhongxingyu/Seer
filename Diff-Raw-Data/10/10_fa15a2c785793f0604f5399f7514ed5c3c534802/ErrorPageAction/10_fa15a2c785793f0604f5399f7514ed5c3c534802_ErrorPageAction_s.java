 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 
 package org.infoglue.deliver.applications.actions;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.HttpServletResponse;
 
 import org.infoglue.cms.applications.common.actions.Error;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.util.CharResponseWrapper;
 
 
 /**
  * This is an error page action. Used to send out the right error codes and the right html
  *
  * @author Mattias Bogeblad
  */
 
 public class ErrorPageAction extends InfoGlueAbstractAction 
 {
     private int responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
     
     /**
      * This is the excecute method - it will send the right error codes and also show the right error message.
      */
     
     public String doExecute() throws Exception
     {
         String responseCodeAttribute = (String)this.getRequest().getAttribute("responseCode");
         if(responseCodeAttribute != null)
             responseCode = Integer.parseInt(responseCodeAttribute);
         
         String responseCodeParameter = (String)this.getRequest().getParameter("responseCode");
         if(responseCodeParameter != null)
             responseCode = Integer.parseInt(responseCodeParameter);
 
         Exception e = (Exception)this.getRequest().getAttribute("error");
         if(e != null)
         {
             setError(e, e.getCause());
             //System.out.println("error:" + e.getMessage());
         }
                 
         //System.out.println("responseCode:" + responseCode);
         this.getResponse().setStatus(responseCode);
 
         String errorUrl = CmsPropertyHandler.getProperty("errorUrl");
         if(errorUrl != null && errorUrl.indexOf("@errorUrl@") == -1)
         {
             if(errorUrl.indexOf("http") > -1)
                 this.getResponse().sendRedirect(errorUrl);
             else
             {
                 RequestDispatcher dispatch = this.getRequest().getRequestDispatcher(errorUrl);
                 this.getRequest().setAttribute("error", e);
                 //dispatch.forward(this.getRequest(), this.getResponse());
                 dispatch.include(this.getRequest(), this.getResponse());
             }
             
             return NONE;
         }
         else
             return SUCCESS;
     }
 
     public int getResponseCode()
     {
         return responseCode;
     }
 }
