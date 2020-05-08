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
 
 package org.esgf.globusonline;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Vector;
 
 import org.globusonline.JGOTransfer;
 import org.globusonline.JGOTransferException;
 import org.globusonline.EndpointInfo;
 
 import java.net.URI;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 import org.openid4java.OpenIDException;
 import org.openid4java.util.HttpCache;
 import org.openid4java.discovery.xrds.XrdsServiceEndpoint;
 import org.openid4java.discovery.yadis.YadisException;
 import org.openid4java.discovery.yadis.YadisResolver;
 import org.openid4java.discovery.yadis.YadisResult;
 
 @SuppressWarnings("unchecked")
 public class Utils {
     
     private final static Logger LOG = Logger.getLogger(Utils.class);
     
     public static String getPassword(File file) {
         String passwd = null;
         
         StringBuffer contents = new StringBuffer();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(file));
             int counter = 0;
             String line = null;
             while ((line = reader.readLine()) != null) {
                 if(counter == 0) {
                     passwd = line;
                 }
                 counter++;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         return passwd;
     }
     
     /*
      * Used to extract the openid from the cookie in the header of a request
      */
     public static String getIdFromHeaderCookie(HttpServletRequest request) {
         LOG.debug("------Utils getIdFromHeaderCookie------");
         
         Cookie [] cookies = request.getCookies();
         
         String userId = "";
         
         for(int i=0;i<cookies.length;i++) {
             LOG.debug("CookieArray: " + i + " " + cookies[i].getName());
             if(cookies[i].getName().equals("esgf.idp.cookie")) {
                 userId = cookies[i].getValue();
             }
         }
 
         LOG.debug("------End tils getIdFromHeaderCookie------");
         return userId;
     }
     
     public static void writeXMLContentToFile(Element rootNode,File file) {
         XMLOutputter outputter = new XMLOutputter();
         String xmlContent = outputter.outputString(rootNode);
         
         try {
             Writer output = null;
             output = new BufferedWriter(new FileWriter(file));
             output.write(xmlContent);
             
             output.close();
         } catch(Exception e) {
             System.out.println("Error in writeXMLContentTOFile");
             e.printStackTrace();
         }
         
     }
     
     public static String createGroupId(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt();
         while(groupIdExists(num,file)) {
             num = rand.nextInt();
         }
         String str = "group" + num + "_id";
         return str;
     }
     
     public static boolean groupIdExists(int id,File file) {
         boolean idExists = false;
         SAXBuilder builder = new SAXBuilder();
         String xmlContent = "";
         //File file = GROUPS_FILE;
         
         try{
 
             Document document = (Document) builder.build(file);
             
             Element rootNode = document.getRootElement();
         
             List groups = (List)rootNode.getChildren();
             String intStr = Integer.toString(id);
             for(int i=0;i<groups.size();i++)
             {
                 Element groupEl = (Element)groups.get(i);
                 Element groupIdEl = groupEl.getChild("groupid");
                 String groupId = groupIdEl.getTextNormalize();
                 if(groupId.contains(intStr)) {
                     idExists = true;
                 }
             }
         
         
         }catch(Exception e) {
             System.out.println("Problem in idExists");
             
         }
         return idExists;
     }
     
     
     public static String createUserId(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"id",file)) {
             num = rand.nextInt();
         }
         String str = "user" + num;
         return str;
     }
     
     
     public static boolean idExists(int id,String cat,File file) {
         boolean idExists = false;
         SAXBuilder builder = new SAXBuilder();
         String xmlContent = "";
         try{
             Document users_document = (Document) builder.build(file);
             Element rootNode = users_document.getRootElement();
             List users = (List)rootNode.getChildren();
             String intStr = Integer.toString(id);
             
             for(int i=0;i<users.size();i++)
             {
                 Element userEl = (Element)users.get(i);
                 Element userIdEl = userEl.getChild(cat);
                 String userId = userIdEl.getTextNormalize();
                 
                 if(userId.contains(intStr)) {
                     idExists = true;
                     System.out.println("Id exists");
                 }
             }
             
         }catch(Exception e) {
             e.printStackTrace();
             LOG.debug("Error in getUserIdFromOpenID");
         }
         
         return idExists;
     }
     
     public static String createOpenId(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"openid",file)) {
             num = rand.nextInt();
         }
         String str = "openid" + num;
         return str;
     }
     
     public static String createUserName(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"username",file)) {
             num = rand.nextInt();
         }
         String str = "username" + num;
         return str;
     }
     
     public static String createUserDN(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"dn",file)) {
             num = rand.nextInt();
         }
         String str = "dn" + num;
         return str;
     }
     
     //Used by ManageUsersController to obtain the "type"
     public static String getTypeFromQueryString(HttpServletRequest request) {
         LOG.debug("------Utils getTypeFromQueryString------");
         String type = "";
         Enumeration<String> paramEnum = request.getParameterNames();
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             if(postContent.equalsIgnoreCase("type")) {
                 type = request.getParameter(postContent);
             }
         }
         LOG.debug("------End Utils getTypeFromQueryString------");
         return type;
     }
     
     
     
     
     
     /**
      * headerStringInfo(HttpServletRequest request)
      * Private method that prints out the header contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     public static void headerStringInfo(HttpServletRequest request) {
         LOG.debug("--------Utils Header String Info--------");
         Enumeration headerNames = request.getHeaderNames(); 
         while(headerNames.hasMoreElements()) { 
             String headerName = (String)headerNames.nextElement(); 
             LOG.debug(headerName+"-->"); 
             LOG.debug(request.getHeader(headerName)); 
         }
         LOG.debug("--------End Utils Header String Info--------");
     }
     /**
      * queryStringInfo(HttpServletRequest request)
      * Private method that prints out the contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     public static void queryStringInfo(HttpServletRequest request) {
         LOG.debug("--------Utils Query String Info--------");
         
         
         Enumeration<String> paramEnum = request.getParameterNames();
         
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             LOG.debug(postContent+"-->"); 
             LOG.debug(request.getParameter(postContent));
         }
         /**/
         
         LOG.debug("--------End Utils Query String Info--------");
     }
     
     /*
      * Single level Element nesting debugger
      */
     public static void printElementContents(Element element) {
         LOG.debug("--------Utils printElementContents--------");
         List children = (List)element.getChildren();
         for(int i=0;i<children.size();i++)
         {
             LOG.debug("Element: " + i + " " + children.get(i));
         }
         LOG.debug("--------End Utils printElementContents--------");
     }
     
     public static String resolveMyProxyViaOpenID(String openId)
         throws YadisException, Exception
     {
         String result = null;
         LOG.debug("Attempting to resolve MyProxy Server from OpenID: " + openId);
 
         YadisResolver resolver = new YadisResolver();
         Set<String> serviceTypes = new HashSet<String>();
         // service type for P2P issued OpenIDs
         serviceTypes.add("esg:myproxy-service");
         // service type for Gateway issued OpenIDs
         serviceTypes.add("urn:esg:security:myproxy-service");
 
         YadisResult yadisResult = resolver.discover(openId, 10, new HttpCache(), serviceTypes);
         XrdsServiceEndpoint endpoint = (XrdsServiceEndpoint) yadisResult.getEndpoints().get(0);
 
         // clean up endpoint, as some appear to contain cruft
         // e.g. "socket://hostname.org:7512:7512
         String ep = endpoint.getUri();
         int pos = ep.indexOf("://");
         if (pos != -1)
         {
             ep = ep.substring(pos + 3);
         }
         pos = ep.indexOf(":");
         if (pos != -1)
         {
             pos = ep.indexOf(":", pos+1);
             if (pos != -1)
             {
                 ep = ep.substring(0, pos);
             }
         }
         else
         {
             ep = ep + ":7512";
         }
         result = ep;
 
         return result;
     }
 
     public static Vector<EndpointInfo> bringGCEndpointsToTop(
         String goUserName, Vector<EndpointInfo> endpoints)
     {
         Vector<EndpointInfo> gcUserEps = new Vector<EndpointInfo>();
         Vector<EndpointInfo> tmp = new Vector<EndpointInfo>();
         for(EndpointInfo cur : endpoints)
         {
             if (cur.getEPName().startsWith(goUserName) && cur.isGlobusConnect())
             {
                 gcUserEps.add(cur);
             }
             else
             {
                 tmp.add(cur);
             }
         }
         gcUserEps.addAll(tmp);
         return gcUserEps;
     }
 
     /*
       If there are multiple endpoints with the same GridFTP
       information, this method gives preference to one that starts
       with "esg#" since it's the most likely to be correct.
     */
     public static String lookupGOEPBasedOnGridFTPURL(
         String gftpURL, Vector<EndpointInfo> endpointInfos, boolean forceESG)
     {
         String goEP = null, hosts = null;
         for(EndpointInfo curInfo : endpointInfos)
         {
             hosts = curInfo.getHosts();
             if (hosts.contains(gftpURL))
             {
                 if ((forceESG == true) && (!curInfo.getEPName().startsWith("esg")))
                 {
                     continue;
                 }
                 goEP = curInfo.getEPName();
                 break;
             }
         }
         return goEP;
     }
 
     public static EndpointInfo getLocalEndpointBasedOnGlobalName(
         String goUserName, String endpointName, Vector<EndpointInfo> endpointInfos)
     {
         String epName = endpointName;
         int pos = endpointName.indexOf("#");
         if (pos != -1)
         {
             epName = endpointName.substring(pos+1);
         }
         for(EndpointInfo curInfo : endpointInfos)
         {
             String curEPName = curInfo.getEPName();
             if (curEPName.startsWith(goUserName))
             {
                 if (curEPName.endsWith("#" + epName))
                 {
                     System.out.println("Found matching local Endpoint Info: " + curInfo);
                     return curInfo;
                 }
             }
         }
         return null;
     }
 
     public static String getEndpointInfoFromEndpointStr(String endpoint, String[] endpointInfos)
     {
         String endpointInfo = null;
         String searchEndpoint = endpoint + "^^";
         int len = endpointInfos.length;
         for(int i = 0; i < len; i++)
         {
             if (endpointInfos[i].startsWith(searchEndpoint))
             {
                 endpointInfo = endpointInfos[i];
                 break;
             }
         }
         return endpointInfo;
     }
 
     public static String createGlobusOnlineEndpointFromEndpointInfo(
         JGOTransfer transfer, String goUserName, String info)
         throws JGOTransferException
     {
         Random rand = new Random(); 
         String localEPName = "esg-dyn-ep" + Integer.toString(rand.nextInt());
         System.out.println("Default generated local EP name is: " + localEPName);
         String[] endpointPieces = info.split("\\^\\^");
         String destEPName = endpointPieces[0];
         String gridFTPServer = endpointPieces[1];
         if (gridFTPServer.startsWith("gsiftp://"))
         {
             gridFTPServer = gridFTPServer.substring(9);
         }
         String myproxyServer = endpointPieces[2];
         int pos = destEPName.indexOf("#");
         if (pos != -1)
         {
             localEPName = localEPName + "-" + destEPName.substring(pos+1);
         }
         localEPName = goUserName + "#" + localEPName;
 
         // check if there are multiple GridFTP entries and remove all
         // but the first if so
         pos = gridFTPServer.indexOf(",");
         if (pos != -1)
         {
             gridFTPServer = gridFTPServer.substring(0, pos);
         }
         System.out.println("Adding new Endpoint \"" + localEPName + "\" with GridFTP: "
                            + gridFTPServer + ", and MyProxy: " + myproxyServer);
 
         transfer.addEndpoint(localEPName, gridFTPServer, myproxyServer, false);
         return localEPName;
     }
 }
