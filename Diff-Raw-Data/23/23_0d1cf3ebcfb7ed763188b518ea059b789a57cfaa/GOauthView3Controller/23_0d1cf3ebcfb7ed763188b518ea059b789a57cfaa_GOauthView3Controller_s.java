 /*****************************************************************************
  * Copyright © 2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * ÒLicensorÓ) hereby grants to any person (the ÒLicenseeÓ) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizationÕs name.  If the Software is protected by a proprietary
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
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.Ó
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
  * @author Neill Miller (neillm@mcs.anl.gov), Feiyi Wang (fwang2@ornl.gov),
  * 	   Eric Blau (blau@mcs.anl.gov)
  *
  */
 package org.esgf.globusonline;
 
 import java.util.Vector;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.*;
 
 import org.openid4java.discovery.yadis.YadisException;
 
 import org.globusonline.*;
 import org.json.*;
 
 import esg.common.util.ESGFProperties;
 import esg.node.security.UserInfo;
 import esg.node.security.UserInfoCredentialedDAO;
 import esg.node.security.UserInfoDAO;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value="/goauthview3")
 public class GOauthView3Controller {
 
 
     private final static Logger LOG = Logger.getLogger(GOauthView3Controller.class);
 
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
     private final static String GOFORMVIEW_OPENID = "GoFormView_openId";
 
     // should be configurable?
     private final static String CA_CERTIFICATE_FILE = "/etc/grid-security/certificates/97552d04.0";
     
 
     public GOauthView3Controller()
     {
     }
 
     @SuppressWarnings("unchecked")
     @RequestMapping(method=RequestMethod.POST)
     public ModelAndView doPost(final HttpServletRequest request)
     {
         /* get model params here */
         String userCertificate = null;
 	String goUserName = null;
         String target = request.getParameter("path");
         String folder = request.getParameter("folder[0]");
 	
 	//System.out.println("path from Parameters is: " + target);
 	//System.out.println("folder from Parameters is: " + folder);
 
         String[] endpointInfos = null;
         String endpoint = request.getParameter("endpoint");
         String createdSrcEndpoint = null, createdDestEndpoint = null;
 
         //if this request comes via go form view 3 we can then obtain the request parameters for myproxy username
         String srcMyproxyUserName = request.getParameter("srcmyproxyuser");
         String srcMyproxyUserPass = request.getParameter("myProxyUserPass");
         String myProxyServerStr = request.getParameter("srcmyproxyserver");
 
 
         StringBuffer errorStatus = new StringBuffer("Steps leading up to the error are shown below:<br><br>");
 
         String auth_code = request.getParameter("code");
         String [] file_urls = null;
         String [] file_names;
 
         String myproxyServerStr = null;
 	String myproxyUserName = null;
 	JSONObject goAccessTokenObj = null;
 	String goAccessToken = null;
 	String userCertificateFile = null;
 
         Map<String,Object> model = new HashMap<String,Object>();
 //Get the session, so we can retrieve state.
         HttpSession session = request.getSession(false);
         if (session == null)
         {}else
         {
 	   if (!(endpoint == null)){ session.setAttribute("endpoint", endpoint);}else{
 	endpoint = (String) session.getAttribute("endpoint");
 	}
 	if (!(target == null)){ session.setAttribute("target", target);}else{
 	target = (String) session.getAttribute("target");
 	}
 	if (!(folder == null)){ session.setAttribute("folder", folder);}else{
 	folder = (String) session.getAttribute("folder");
 	}
         file_urls = (String []) session.getAttribute("fileUrls");
         String dataset_id = (String ) session.getAttribute("datasetName");
         //System.out.println("Auth3, session id is:" + session.getId());
         //System.out.println("Your dataset name is: " + dataset_id);
         
         userCertificateFile = (String) session.getAttribute("usercertificatefile");
 	if (userCertificateFile.equals("undefined")){userCertificateFile = null;}
         goUserName = (String) session.getAttribute("gousername");
         goAccessTokenObj = (JSONObject) session.getAttribute("goaccesstoken");
 	try{
         goAccessToken = goAccessTokenObj.getString("access_token");
 	} catch (JSONException e) {
                         //logger.error("Error getting access_token", e);
                         //throw new ValueErrorException();
 	}
 
 	myproxyServerStr = (String) session.getAttribute("myproxyServerStr");
 	myproxyUserName = (String) session.getAttribute("myproxyUserName");
 	//if (!(myproxyUserName == null)){System.out.println("Auth3, myproxyUserName is:" +myproxyUserName);}
 	if (srcMyproxyUserPass == null){srcMyproxyUserPass = (String) session.getAttribute("MyproxyUserPass");}
         }
         String esg_user="";
         String esg_password="";
 
         try
         {
 	    //get the openid here from the cookie
 	    Cookie [] cookies = request.getCookies();
 	    String openId = "";
 	    for(int i = 0; i < cookies.length; i++)
 	    {
 		if (cookies[i].getName().equals("esgf.idp.cookie"))
 		{
 		    openId = cookies[i].getValue();
 		}
 	    }
 
 	    LOG.debug("Got User OpenID: " + openId);
 	    model.put(GOFORMVIEW_OPENID, openId);
             myproxyServerStr = Utils.resolveMyProxyViaOpenID(openId);
             LOG.debug("Using MyProxy Server: " + myproxyServerStr);
             System.out.println("Using MyProxy Server: " + myproxyServerStr);
 
             ESGFProperties esgfProperties = new ESGFProperties();
             UserInfoDAO uid = new UserInfoDAO(esgfProperties);
             UserInfo userInfo = uid.getUserByOpenid(openId);
             myproxyUserName = userInfo.getUserName();
 
             LOG.debug("Got MyProxy Username: " + myproxyUserName);
             System.out.println("Got MyProxy Username: " + myproxyUserName);
 
 	    if (request.getParameter(GOFORMVIEW_MODEL)!=null) {
 		//it should never come here...
 	    }
 	    else {
 		//do we need this at all?  I don't think so
 	    }
         }
         catch(YadisException ye)
         {
             String eMsg = ye.toString();
             if (eMsg.indexOf("0x702") != -1)
             {
                 model.put(GOFORMVIEW_ERROR, "error");
                 model.put(GOFORMVIEW_ERROR_MSG, "Please <a href=\"login\">Login</a>" +
                           " before trying to download data!");
             }
             else
             {
                 String errorMsg = "Failed to resolve OpenID: " + ye;
                 LOG.error("Failed to resolve OpenID: " + ye);
                 model.put(GOFORMVIEW_ERROR, "error");
                 model.put(GOFORMVIEW_ERROR_MSG, errorMsg + "<br><br>Please make sure that you're" +
                           " logged in as a valid user before trying to download data!<br><br>");
             }
         }
         catch(Exception e)
         {
 	    String errorMsg = "Failed to resolve OpenID: " + e;
             LOG.error("Failed to resolve OpenID: " + e);
             model.put(GOFORMVIEW_ERROR, "error");
             model.put(GOFORMVIEW_ERROR_MSG, errorMsg + "<br><br>Please make sure that you're" +
 		      " logged in as a valid user before trying to download data!<br><br>");
         }
 
 	if ((userCertificateFile == null || userCertificateFile.isEmpty())&&(srcMyproxyUserPass == null)){
 	//System.out.println("Auth3, srcMyproxyUserPass is null");
 	return new ModelAndView("goauthview3", model);}
 	else{
 	//System.out.println("Auth3, srcMyproxyUserPass is:"); // +srcMyproxyUserPass);
 	}
         //LOG.debug("GOFORMView4Controller got Certificate " + userCertificate);
         //LOG.debug("GOFORMView4Controller got Target " + target);
         //LOG.debug("GOFORMView4Controller got selected endpoint " + endpoint);
         //LOG.debug("GOFORMView4Controller got Src Myproxy User " + srcMyproxyUserName);
         //LOG.debug("GOFORMView4Controller got Src Myproxy Pass ******");
         //LOG.debug("GOFORMView4Controller got Src Myproxy Server " + myproxyServerStr);
 
         //System.out.println("GOFORMView4Controller got Certificate " + userCertificate);
         //System.out.println("GOFORMView4Controller got Target " + target);
         //System.out.println("GOFORMView4Controller got selected endpoint " + endpoint);
         //System.out.println("GOauthView3Controller got Src Myproxy User " + myproxyUserName);
         //System.out.println("GOauthView3Controller got go User " + goUserName);
         //System.out.println("GOFORMView4Controller got Src Myproxy Pass ******");
 // +srcMyproxyUserPass);
         //System.out.println("GOauthView4Controller got Src Myproxy Server " + myproxyServerStr);
         
 	if ((srcMyproxyUserPass != null)||(userCertificateFile == null || userCertificateFile.isEmpty())){
 	    try{
             JGOTransfer un = new JGOTransfer(
                 goUserName, myproxyServerStr, myproxyUserName,
                 srcMyproxyUserPass, CA_CERTIFICATE_FILE);
   	    un.setVerbose(true);
             un.initialize();
 
             LOG.debug("Globus Online Transfer object Initialize complete");
 
             //errorStatus.append("Globus Online Transfer object Initialize complete<br>");
 
             userCertificateFile = un.getUserCertificateFile();
             LOG.debug("Retrieved user credential file: " + userCertificateFile);
          //   System.out.println("Retrieved user credential file: " + userCertificateFile);
 	    model.put(GOFORMVIEW_USER_CERTIFICATE, userCertificateFile);
 	   }   catch(Exception e)
             {
  LOG.error("Failed to initialize transfer object to get user cert: " + e);
 
 	    //TODO: Iterate back to password page w/ invalid password error
                 model.put(GOFORMVIEW_ERROR, "autherror");
             System.out.println("Failed to initialize transfer object to get user cert: " + e);
 		return new ModelAndView("goauthview3", model);
 	    }
 	}
  //System.out.println("Retrieved user credential file: " + goUserName + goAccessToken + userCertificateFile + CA_CERTIFICATE_FILE);
 
         JGOTransfer transfer = new JGOTransfer(goUserName, goAccessToken, CA_CERTIFICATE_FILE);
         transfer.setVerbose(true);
 	//If we need to set vs. testing:
 	//transfer.setBaseUrl("https://transfer.test.api.globusonline.org/v0.10");
 
         try
         {
             String newURL = null, goEP = null;
             String[] pieces = null;
             Vector<String> fileList = null;
             HashMap<String, String> sourceEpToGFTPMap = new HashMap<String, String>();
             HashMap<String, Vector<String>> sourceMap = new HashMap<String, Vector<String>>();
             transfer.initialize();
 
  	    LOG.debug("About to retrieve available endpoints");
             Vector<EndpointInfo> endpoints = transfer.listEndpoints();
             LOG.debug("We pulled down " + endpoints.size() + " endpoints");
             //System.out.println("We pulled down " + endpoints.size() + " endpoints");
             errorStatus.append("Endpoints retrieved<br>");
             endpointInfos = constructEndpointInfos(endpoints);
             // find the endpointInfo line that matches the endpoint the user selected
             String endpointInfo = Utils.getEndpointInfoFromEndpointStr(endpoint, endpointInfos);
             //System.out.println("User selected endpoint that has the info: " + endpointInfo);
             LOG.debug("User selected endpoint that has the info: " + endpointInfo);
 
             //boolean isGlobusConnect = endpointInfo.endsWith("true");
 
             // FIXME: Cache from previous time we called this?
             // or reconstruct from the other format of them that we have?
             Vector<EndpointInfo> goEndpointInfos = transfer.listEndpoints();
 
             // first pass, find all sources
             // we create a mapping of GO endpoints to Filelists
            
             for(String curURL : file_urls)
             {
	//System.out.println("curURL is:" +curURL);
                 pieces = curURL.split("//");
                 if ((pieces != null) && (pieces.length > 1))
                 {
                     goEP = Utils.lookupGOEPBasedOnGridFTPURL(pieces[1], goEndpointInfos, true);
                     if (goEP == null)
                     {
                         goEP = Utils.lookupGOEPBasedOnGridFTPURL(pieces[1], goEndpointInfos, false);
                     }
 
                     if (!sourceMap.containsKey(goEP))
                     {
                         LOG.debug("Mapped GridFTP Server " + pieces[1] + " to GO EP " + goEP);
                         //System.out.println("Mapped GridFTP Server " + pieces[1] + " to GO EP " + goEP);
                         sourceEpToGFTPMap.put(goEP, pieces[1]);
                         sourceMap.put(goEP, new Vector<String>());
                     }
 
                     fileList = sourceMap.get(goEP);
                     newURL = "//" + pieces[2];
 
                     LOG.debug("Transformed " + curURL + " into " + newURL);
                     //System.out.println("Transformed " + curURL + " into " + newURL);
                     fileList.add(newURL);
                 }
                 else
                 {
                     LOG.debug("Failed to split URL on //: " + curURL);
                     //System.out.println("Failed to split URL on //: " + curURL);
                 }
             }
 
             // For now we always just grab the first endpoint since we
             // can only handle a single source endpoint (per transfer)
             // ... break up into multiple transfers later when we
             // support transfers of multiple data sets at once
             Map.Entry<String, Vector<String>> entry = sourceMap.entrySet().iterator().next();
             String goSourceEndpoint = entry.getKey();
             Map.Entry<String, String> gftpEntry = sourceEpToGFTPMap.entrySet().iterator().next();
             String gftpServer = gftpEntry.getValue();
 
            //System.out.println("Got GO Source EP: " + goSourceEndpoint);
            //System.out.println("Got GFTP Server: " + gftpServer);
             if (goSourceEndpoint != null)
             {
                 fileList = entry.getValue();
             }
             else
             {
                 // create new endpoint using known information
                 String srcEndpointInfo = "D^^" + gftpServer +
                     "^^" + myproxyServerStr + "^^false";
                 goSourceEndpoint = Utils.createGlobusOnlineEndpointFromEndpointInfo(
                     transfer, goUserName, srcEndpointInfo);
                 createdSrcEndpoint = goSourceEndpoint;
             }
 
             LOG.debug("Using GO Source EP: " + goSourceEndpoint);
             //System.out.println("Using GO Source EP: " + goSourceEndpoint);
 
             errorStatus.append("Source endpoint resolved as \"");
             errorStatus.append(goSourceEndpoint);
             errorStatus.append("\".<br>");
 
             // first activate the source endpoint
             LOG.debug("Activating source endpoint " + goSourceEndpoint);
             //System.out.println("Activating source endpoint " + goSourceEndpoint);
             errorStatus.append("Attempting to activate Source Endpoint " + goSourceEndpoint + " ...<br>");
             try
             {
 		// try the activation with the userCertificateFile
                 transfer.activateEndpoint(goSourceEndpoint, userCertificateFile);
             }
             catch(Exception e)
             {
 	    //System.out.println("activation w/ userCert failed because:" + e.toString());
 	    //System.out.println("userCertificateFile:" +userCertificateFile);
 		//
 	        model.put(GOFORMVIEW_ERROR, "crederror");
 	        //model.put(GOFORMVIEW_ERROR_MSG, error);
 	        //System.out.println("Failed to initialize transfer object to get user cert: " + e);
 	        return new ModelAndView("goauthview3", model);
             }
             errorStatus.append("Source Endpoint activated properly!<br>");
             //System.out.println("pSource Endpoint activated properly!<br>");
 
             String[] endpointPieces = endpointInfo.split("\\^\\^");
             String destEPName = endpointPieces[0];
 
 
             // kick off the transfer here!
             errorStatus.append("Attempting to start Globus Online Transfer ...<br>");
 
 	    String destpath = target + folder;
 	    //System.out.println("destpath is" + destpath);
 	    //System.out.println("goSourceEndpoint is" + goSourceEndpoint);
 	    //System.out.println("destEPName is" + destEPName);
 	    //System.out.println("fileList is" + fileList);
             String taskID = transfer.transfer(goSourceEndpoint, destEPName, fileList, destpath);
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
 
             System.out.println("Trying to teardown created source endpoints ...");
             if (createdSrcEndpoint != null)
             {
                 try { transfer.removeEndpoint(createdSrcEndpoint); } catch(Exception e1) {}
             }
             if (createdDestEndpoint != null)
             {
                 try { transfer.removeEndpoint(createdDestEndpoint); } catch(Exception e2) {}
             }
             System.out.println("Attempted endpoint removal complete");
         }
         //make sure we put the cert back in the model so we can reuse it
 	model.put(GOFORMVIEW_USER_CERTIFICATE, userCertificateFile);
         return new ModelAndView("goauth_transfer", model);
     }
 
  private String[] constructEndpointInfos(Vector<EndpointInfo> endpoints)
     {
         int numEndpoints = endpoints.size();
         String [] endPointNames = new String[numEndpoints];
 
         for(int i = 0; i < numEndpoints; i++)
         {
             // we encode this in a string as follows
             // EPNAME^^HOSTS^^MYPROXYSERVER^^ISGLOBUSCONNECT
             endPointNames[i] = endpoints.get(i).getEPName() + "^^" + endpoints.get(i).getHosts() +
                 "^^" + endpoints.get(i).getMyproxyServer() + "^^" + endpoints.get(i).isGlobusConnect();
         }
         return endPointNames;
     }
 }
 
