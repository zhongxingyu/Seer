 /*****************************************************************************
  * Copyright  2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * Licensor) hereby grants to any person (the Licensee) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizations name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 
 /**
  * On request mapping:
  *
  *     url rewrite filter will take over first, then we do regular Spring mapping.
  *     RedirectView is discouraged here as it will mess up the current rewrite
  *     rule, use "redirect:" prefix instead, and it is regarded as a better alternative
  *     anyway.
  *
  * For any redirect trouble, please refers to ROOT/urlrewrite.xml
  *
  * @author Feiyi Wang (fwang2@ornl.gov)
  *
  */
 package org.esgf.globusonline;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value="/goformview3")
 public class GOFormView3Controller {
 
 
     private final static Logger LOG = Logger.getLogger(GOFormView3Controller.class);
 
     private final static String GOFORMVIEW_MODEL = "GoFormView_model";
     private final static String GOFORMVIEW_DATASET_NAME = "GoFormView_Dataset_Name";
     private final static String GOFORMVIEW_FILE_URLS = "GoFormView_File_Urls";
     private final static String GOFORMVIEW_FILE_NAMES = "GoFormView_File_Names";
     private final static String GOFORMVIEW_ERROR = "GoFormView_Error";
     private final static String GOFORMVIEW_DEST_TARGET_PATH = "GoFormView_DestTargetPath";
     private final static String GOFORMVIEW_DEST_ENDPOINT_INFO = "GoFormView_DestEndpointInfo";
     private final static String GOFORMVIEW_USER_CERTIFICATE = "GoFormView_UserCertificate";
     private final static String GOFORMVIEW_GO_USERNAME = "GoFormView_GOUsername";
     private final static String GOFORMVIEW_GO_CONNECT = "GoFormView_GOConnect";
     private final static String GOFORMVIEW_SRC_MYPROXY_USER = "GoFormView_SrcMyproxyUser";
     private final static String GOFORMVIEW_SRC_MYPROXY_PASS = "GoFormView_SrcMyproxyPass";
     private final static String GOFORMVIEW_DEST_MYPROXY_SERVER = "GoFormView_DestMyproxyServer";
 
     public GOFormView3Controller() {
     }
 
     @RequestMapping(method=RequestMethod.POST)
     public ModelAndView doPost(final HttpServletRequest request)
     {
         /* get model params here */
         String dataset_name = request.getParameter("id");
         String userCertificate = request.getParameter("usercertificate");
         String[] file_names = request.getParameterValues("child_id");
         String[] file_urls = request.getParameterValues("child_url");
         String[] endpointInfos = request.getParameterValues("endpointinfos");
 
         /* get endpoint info here */
         String goUserName = request.getParameter("gousername");
         String endpoint = request.getParameter("endpointdropdown");
         String target = request.getParameter("target");
         String myProxyUserName = request.getParameter("srcmyproxyuser");
         String myProxyUserPass = request.getParameter("srcmyproxypass");
 
         LOG.debug("GOFormView3Controller: Got GO Username " + goUserName);
         LOG.debug("GOFormView3Controller: Got User Certificate " + userCertificate);
         LOG.debug("GOFormView3Controller: Got selected endpoint " + endpoint);
         LOG.debug("GOFormView3Controller: Got selected target " + target);
         LOG.debug("GOFormView3Controller: Got endpointInfos " + endpointInfos);
         LOG.debug("GOFormView3Controller: Source MyProxy User  " + myProxyUserName);
         LOG.debug("GOFormView3Controller: Source MyProxy Pass *****");
 
         Map<String,Object> model = new HashMap<String,Object>();
 
         // find the endpointInfo line that matches the endpoint the user selected
         int len = endpointInfos.length;
         String endpointInfo = null;
         String searchEndpoint = endpoint + "^^";
         for(int i = 0; i < len; i++)
         {
             if (endpointInfos[i].startsWith(searchEndpoint))
             {
                 endpointInfo = endpointInfos[i];
                 break;
             }
         }
         System.out.println("User selected endpoint that has the info: " + endpointInfo);
         LOG.debug("User selected endpoint that has the info: " + endpointInfo);
 
         String destMyProxyServer = null;
         boolean isGlobusConnect = endpointInfo.endsWith("true");
 
         if (isGlobusConnect == false)
         {
             String[] pieces = endpointInfo.split("\\^\\^");
             destMyProxyServer = pieces[2];
         }
 
         if (request.getParameter(GOFORMVIEW_MODEL)!=null) {
             //shouldn't ever come here
         }
         else
         {
             if (isGlobusConnect == true)
             {
                 LOG.debug("GlobusConnect Endpoint detected");
                 model.put(GOFORMVIEW_GO_CONNECT, "true");
             }
             model.put(GOFORMVIEW_DEST_MYPROXY_SERVER, destMyProxyServer);
             model.put(GOFORMVIEW_FILE_URLS, file_urls);
             model.put(GOFORMVIEW_FILE_NAMES, file_names);
             model.put(GOFORMVIEW_USER_CERTIFICATE, userCertificate);
             model.put(GOFORMVIEW_DATASET_NAME, dataset_name);
             model.put(GOFORMVIEW_DEST_TARGET_PATH, target);
             model.put(GOFORMVIEW_DEST_ENDPOINT_INFO, endpointInfo);
             model.put(GOFORMVIEW_GO_USERNAME, goUserName);
             model.put(GOFORMVIEW_SRC_MYPROXY_USER, myProxyUserName);
             model.put(GOFORMVIEW_SRC_MYPROXY_PASS, myProxyUserPass);
         }
 
         // NOTE: We have the ability to skip this page if target is
         // Globus Connect rather than using the above method that
         // displays text.  This method should override the above
         // safely.
        // NOT READY JUST YET: always go to step 3 for now
         //
         //if myproxy is required then navigate to the myproxy prompt page
//         if (isGlobusConnect == false)
//         {
        return new ModelAndView("goformview3", model);
//         } 
         //otherwise go to the confirmation page
//         else
//         {
//             return new ModelAndView("goformview4", model);
//         }
     }
 }
 
