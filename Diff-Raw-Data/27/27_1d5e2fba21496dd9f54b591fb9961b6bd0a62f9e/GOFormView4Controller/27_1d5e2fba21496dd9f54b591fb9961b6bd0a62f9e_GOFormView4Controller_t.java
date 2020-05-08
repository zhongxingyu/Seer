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
 
 import java.util.Vector;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.globusonline.*;
 
 import esg.common.util.ESGFProperties;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value="/goformview4")
 public class GOFormView4Controller {
 
 
     private final static Logger LOG = Logger.getLogger(GOFormView4Controller.class);
 
     private final static String GOFORMVIEW_MODEL = "GoFormView_model";
     private final static String GOFORMVIEW_DATASET_NAME = "GoFormView_Dataset_Name";
     private final static String GOFORMVIEW_USER_CERTIFICATE = "GoFormView_UserCertificate";
     private final static String GOFORMVIEW_FILE_URLS = "GoFormView_File_Urls";
     private final static String GOFORMVIEW_FILE_NAMES = "GoFormView_File_Names";
     private final static String GOFORMVIEW_ERROR = "GoFormView_Error";
     private final static String GOFORMVIEW_ERROR_MSG = "GoFormView_ErrorMsg";
     private final static String GOFORMVIEW_TRANSFER_INFO1 = "GoFormView_TransferInfo1";
     private final static String GOFORMVIEW_TRANSFER_INFO2 = "GoFormView_TransferInfo2";
     private final static String GOFORMVIEW_TRANSFER_INFO3 = "GoFormView_TransferInfo3";
     private final static String GOFORMVIEW_TRANSFER_INFO4 = "GoFormView_TransferInfo4";
     private final static String GOFORMVIEW_GO_USERNAME = "GoFormView_GOUsername";
 
     // should be configurable?
     private final static String CA_CERTIFICATE_FILE = "/etc/grid-security/certificates/97552d04.0";
     
 
     public GOFormView4Controller()
     {
     }
 
     @SuppressWarnings("unchecked")
     @RequestMapping(method=RequestMethod.POST)
     public ModelAndView doPost(final HttpServletRequest request)
     {
         /* get model params here */
         String dataset_name = request.getParameter("id");
         String userCertificate = request.getParameter("usercertificate");
         String [] file_names = request.getParameterValues("child_id");
         String [] file_urls = request.getParameterValues("child_url");
         String goUserName = request.getParameter("gousername");
         String target = request.getParameter("target");
         String endpointInfo = request.getParameter("endpointinfo");
 
         //if this request comes via go form view 3, then obtain the request parameters for myproxy username
         String srcMyproxyUserName = request.getParameter("srcmyproxyuser");
         String srcMyproxyUserPass = request.getParameter("srcmyproxypass");
 
         String destMyproxyUserName = null;
         String destMyproxyUserPass = null;
 
         StringBuffer errorStatus = new StringBuffer("Steps leading up to the error are shown below:<br><br>");
         if ((request.getParameter("myproxyUserName") != null) && (request.getParameter("myproxyUserPass") != null))
         {
             destMyproxyUserName = request.getParameter("myproxyUserName");
             destMyproxyUserPass= request.getParameter("myproxyUserPass");
         }
         LOG.debug("GOFORMView4Controller got Certificate " + userCertificate);
         LOG.debug("GOFORMView4Controller got Target " + target);
         LOG.debug("GOFORMView4Controller got endpointInfo " + endpointInfo);
         LOG.debug("GOFORMView4Controller got Src Myproxy User " + srcMyproxyUserName);
         LOG.debug("GOFORMView4Controller got Src Myproxy Pass ******");
         LOG.debug("GOFORMView4Controller got Dest Myproxy User " + destMyproxyUserName);
         LOG.debug("GOFORMView4Controller got Dest Myproxy Pass ******");
 
         Map<String,Object> model = new HashMap<String,Object>();
 
         JGOTransfer transfer = new JGOTransfer(goUserName, userCertificate, userCertificate, CA_CERTIFICATE_FILE);
         //transfer.setVerbose(true);
         try
         {
             transfer.initialize();
 
             ESGFProperties esgfProperties = new ESGFProperties();
             String goSourceEndpoint = esgfProperties.getProperty("gridftp.globusonline.endpoint");
             LOG.debug("GOT SOURCE ENDPOINT: " + goSourceEndpoint);
 
             errorStatus.append("Source endpoint resolved as \"");
             errorStatus.append(goSourceEndpoint);
             errorStatus.append("\".<br>");
 
             // first activate the source endpoint
             LOG.debug("Activating source endpoint " + goSourceEndpoint);
             errorStatus.append("Attempting to activate Source Endpoint " + goSourceEndpoint + " ...<br>");
             transfer.activateEndpoint(goSourceEndpoint, srcMyproxyUserName, srcMyproxyUserPass);
             errorStatus.append("Source Endpoint activated properly!<br>");
 
             // second, activate the target endpoint (if not GlobusConnect)
             String[] endpointPieces = endpointInfo.split("\\^\\^");
             if (endpointInfo.endsWith("true"))
             {
                 LOG.debug("Detected Globus Connect target endpoint");
                 errorStatus.append("Detected Globus Connect target endpoint.<br>");
                 errorStatus.append("Attempting to activate Destination Endpoint " + endpointPieces[0] + " ...<br>");
                 transfer.activateEndpoint(endpointPieces[0]);
                 errorStatus.append("Destination Endpoint activated properly!<br>");
             }
             else
             {
                 LOG.debug("Activating destination endpoint " + endpointPieces[0]);
                 errorStatus.append("Attempting to activate Destination Endpoint " + endpointPieces[0] + " ...<br>");
                 transfer.activateEndpoint(endpointPieces[0], destMyproxyUserName, destMyproxyUserPass);
                 errorStatus.append("Destination Endpoint activated properly!<br>");
             }
 
             // transform all URLs here
             String newURL = null;
             String[] pieces = null;
             Vector<String> fileList = new Vector<String>();
             for(String curURL : file_urls)
             {
                 // ESG URLs are of the form "gsiftp://host//file-part.
                 // we only want the file part in this file list (i.e. //file-part).
                 pieces = curURL.split("//");
                 newURL = "//" + pieces[2];
 
                 //LOG.debug("Transformed " + curURL + " into " + newURL);
                 fileList.add(newURL);
             }
 
             // kick off the transfer here!
             errorStatus.append("Attempting to start Globus Online Transfer ...<br>");
             String taskID = transfer.transfer(goSourceEndpoint, endpointPieces[0], fileList, target);
             if (taskID != null)
             {
                 errorStatus.append("Globus Online Transfer got TaskID " + taskID + ".<br>");
                 String transferInfo1 = "The transfer has been accepted and a task has been " +
                     "created and queued for execution.";
                 String transferInfo2 = "Globus Online TaskID: " + taskID;
                 LOG.debug("Started Globus Online transfer with TaskID: " + taskID);
 
                 if (request.getParameter(GOFORMVIEW_MODEL)!=null) {
                 }
                 else
                 {
                     model.put(GOFORMVIEW_TRANSFER_INFO1, transferInfo1);
                     model.put(GOFORMVIEW_TRANSFER_INFO2, transferInfo2);
                 }
             }
             else
             {
                 String error = errorStatus.toString() + "<br><b>Main Error:</b><br><br>Transfer failed";
                 model.put(GOFORMVIEW_ERROR, "error");
                 model.put(GOFORMVIEW_ERROR_MSG, error);
                 LOG.error("Failed to initiate Globus Online transfer.");
             }
         }
         catch(Exception e)
         {
             String error = errorStatus.toString() + "<br><b>Main Error:</b><br><br>" + e.toString();
             model.put(GOFORMVIEW_ERROR, "error");
             model.put(GOFORMVIEW_ERROR_MSG, error);
             LOG.error("Failed to initialize Globus Online: " + e);
         }
         return new ModelAndView("goformview4", model);
     }
 }
 
