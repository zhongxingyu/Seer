 /*
  *  http://www.jrecruiter.org
  *
  *  Disclaimer of Warranty.
  *
  *  Unless required by applicable law or agreed to in writing, Licensor provides
  *  the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  *  including, without limitation, any warranties or conditions of TITLE,
  *  NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
  *  solely responsible for determining the appropriateness of using or
  *  redistributing the Work and assume any risks associated with Your exercise of
  *  permissions under this License.
  *
  */
 package org.jrecruiter.webtier;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ExceptionHandler;
 import org.apache.struts.config.ExceptionConfig;
 
 /**
  * This struts exception will handle any uncaught exceptions that were
  * bubbling up from the action.
  *
  * @author Gunnar Hillert
  * @version $Id$
  */
 public class GeneralExceptionHandler extends ExceptionHandler {
 
     public static final Logger LOGGER = Logger
             .getLogger(GeneralExceptionHandler.class);
 
     /**
      * Constructor.
      */
     public GeneralExceptionHandler() {
         super();
     }
 
     /**
      * @see org.apache.struts.action.ExceptionHandler#execute(java.lang.Exception, org.apache.struts.config.ExceptionConfig, org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     @Override
     public ActionForward execute(Exception e, ExceptionConfig exceptionConfig,
             ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws ServletException {
 
         LOGGER.error("GeneralExceptionHandler() - caught exception!", e);
 
         return super.execute(e, exceptionConfig, mapping, form, request,
                 response);
 
     }
 
 }
