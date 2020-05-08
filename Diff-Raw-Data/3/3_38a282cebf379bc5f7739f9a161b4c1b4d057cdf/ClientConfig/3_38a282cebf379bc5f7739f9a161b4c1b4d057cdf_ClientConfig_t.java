 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dblike.client.service;
 
 import dblike.api.ServerAPI;
 import dblike.client.ActiveServer;
 import dblike.client.Client;
 import dblike.client.CurrentClient;
 import dblike.server.service.FileListService;
 import dblike.server.service.FileListXMLService;
 import dblike.service.FileInfo;
 import dblike.service.FileInfoService;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Hashtable;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author wenhanwu
  */
 public class ClientConfig {
 
     private static CurrentClient currentClient = new CurrentClient();
     private static Vector<ActiveServer> ServerList = ActiveServerListClient.getActiveServerList();
     private static FileListService myFileList = new FileListService();
     private static int currentServerIndex;
     private static boolean availableFlag = true;
 
     public static int pickupAvailableServer() {
         int availableServer = -1;
         availableFlag = true;
         for (int i = 0; i < ServerList.size(); i++) {
            System.out.println(ServerList.get(i).getServerIP()+":"+ServerList.get(i).getPort());
        }
        for (int i = 0; i < ServerList.size(); i++) {
             Client.setTestFlag(0);
             ActiveServer aServer = ServerList.get(i);
             tryToConect(aServer.getServerIP(), aServer.getPort());
             System.out.println(availableFlag);
             System.out.println(Client.getTestFlag());
             if (availableFlag) {
                 availableServer = i;
                 break;
             }
             availableFlag = true;
         }
         availableFlag = true;
         System.out.println("Pick up server Num is: " + availableServer);
         return availableServer;
     }
 
     public static void tryToConect(String ip, int port) {
         try {
             ServerAPI server;
             Registry registry;
             registry = LocateRegistry.getRegistry(ip, port);
             server = (ServerAPI) registry.lookup("serverUtility");
 //            String bindParam = "clientUtility" + ClientConfig.getCurrentClient().getClientID() + ClientConfig.getCurrentClient().getDeviceID() + ClientConfig.getCurrentClient().getIp() + ClientConfig.getCurrentClient().getPort();
 //            System.out.println(bindParam);
 //            server.actClient(bindParam, ip, port);
         } catch (Exception ex) {
             availableFlag = false;
         }
     }
 
     /**
      * @return the currentServerIndex
      */
     public static int getCurrentServerIndex() {
         return currentServerIndex;
     }
 
     /**
      * @param aCurrentServerIndex the currentServerIndex to set
      */
     public static void setCurrentServerIndex(int aCurrentServerIndex) {
         currentServerIndex = aCurrentServerIndex;
     }
 
     /**
      * @return the currentClient
      */
     public static CurrentClient getCurrentClient() {
         return currentClient;
     }
 
     /**
      * @param aCurrentClient the currentClient to set
      */
     public static void setCurrentClient(CurrentClient aCurrentClient) {
         currentClient = aCurrentClient;
     }
 
     /**
      * @return the ServerList
      */
     public static Vector<ActiveServer> getServerList() {
         return ServerList;
     }
 
     /**
      * @param aServerList the ServerList to set
      */
     public static void setServerList(Vector<ActiveServer> aServerList) {
         ServerList = aServerList;
     }
 
     public static FileListService getMyFileList() {
         return myFileList;
     }
 
     public static void setMyFileList(FileListService myFileList) {
         ClientConfig.myFileList = myFileList;
     }
 
     public static void initCurrentClient(String aClientID, String aDeviceID, String aFolderPath, String aIP, String aPort) {
         getCurrentClient().setClientID(aClientID);
         getCurrentClient().setDeviceID(aDeviceID);
         getCurrentClient().setFolderPath(aFolderPath);
         getCurrentClient().setIp(aIP);
         getCurrentClient().setPort(aPort);
     }
 
     /**
      *
      * @return
      */
     public static boolean loadCurrentUser() throws Exception {
         DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder dombuilder = domfac.newDocumentBuilder();
             System.out.println("ClientCfg/users/" + getCurrentClient().getClientID() + "/user.xml");
             InputStream fis = new FileInputStream("ClientCfg/users/" + getCurrentClient().getClientID() + "/user.xml");
             System.out.println(fis);
             Document doc = dombuilder.parse(fis);
             Element root = doc.getDocumentElement();
             Node user = root.getFirstChild();
             if (user != null) {
                 String username = "";
                 String folderPath = "";
                 String deviceID = "";
                 String ip = "";
                 String port = "";
                 for (Node node = user; node != null; node = node.getNextSibling()) {
                     if (node.getNodeType() == Node.ELEMENT_NODE) {
                         if (node.getNodeName().equals("username")) {
                             username = node.getFirstChild().getNodeValue();
                             System.out.println(username);
                         }
                         if (node.getNodeName().equals("deviceID")) {
                             deviceID = node.getFirstChild() == null ? "" : node.getFirstChild().getNodeValue();
                             if (deviceID.equals("")) {
                                 Scanner scanDID = new Scanner(System.in);
                                 System.out.println("First time login, must input the Device ID:");
                                 deviceID = scanDID.nextLine();
                                 node.appendChild(doc.createTextNode(deviceID));
                                 node.getFirstChild().setNodeValue(deviceID);
                             }
                         }
                         if (node.getNodeName().equals("folderPath")) {
                             folderPath = node.getFirstChild() == null ? "" : node.getFirstChild().getNodeValue();
                             if (folderPath.equals("")) {
                                 Scanner scanFP = new Scanner(System.in);
                                 System.out.println("First time login, must specify a sync Folder:");
                                 folderPath = scanFP.nextLine();
                                 folderPath = FileInfoService.getAbsolutePathName(folderPath) + "/";
                                 node.appendChild(doc.createTextNode(folderPath));
                                 node.getFirstChild().setNodeValue(folderPath);
                             }
                         }
 
                         if (node.getNodeName().equals("IP")) {
                             ip = node.getFirstChild().getNodeValue();
                         }
 
                         if (node.getNodeName().equals("port")) {
                             port = node.getFirstChild().getNodeValue();
                         }
                     }
                 }
 
                 initCurrentClient(username, deviceID, folderPath, ip, port);
 
                 TransformerFactory transformerFactory = TransformerFactory.newInstance();
                 Transformer transformer = null;
                 try {
                     transformer = transformerFactory.newTransformer();
                 } catch (TransformerConfigurationException ex) {
                     Logger.getLogger(ClientConfig.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 DOMSource source = new DOMSource(doc);
                 StreamResult result = new StreamResult(new File("ClientCfg/users/" + getCurrentClient().getClientID() + "/user.xml"));
                 try {
                     transformer.transform(source, result);
                 } catch (TransformerException ex) {
                     Logger.getLogger(ClientConfig.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 return true;
             } else {
                 return false;
             }
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
             return false;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return false;
         } catch (SAXException e) {
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      *
      * @return
      */
     public static boolean loadServerList() {
         DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder dombuilder = domfac.newDocumentBuilder();
             InputStream fis = new FileInputStream("ClientCfg/serverList.xml");
             Document doc = dombuilder.parse(fis);
             Element root = doc.getDocumentElement();
             NodeList users = root.getChildNodes();
             if (users != null) {
                 for (int i = 0; i < users.getLength(); i++) {
                     Node user = users.item(i);
                     if (user.getNodeType() == Node.ELEMENT_NODE) {
                         int checker = 0;
                         String ip = "";
                         String port = "";
                         for (Node node = user.getFirstChild(); node != null; node = node.getNextSibling()) {
                             if (node.getNodeType() == Node.ELEMENT_NODE) {
                                 if (node.getNodeName().equals("IP")) {
                                     ip = node.getFirstChild().getNodeValue();
                                     ++checker;
                                 }
                                 if (node.getNodeName().equals("port")) {
                                     port = node.getFirstChild().getNodeValue();
                                     ++checker;
                                 }
                             }
                             if (checker == 2) {
                                 String serverID = ip;
                                 getServerList().add(new ActiveServer(serverID, ip, Integer.parseInt(port)));
                                 checker = 0;
                             }
                         }
                     }
                 }
                 for (int i = 0; i < getServerList().size(); ++i) {
                     System.out.println(getServerList().get(i).getServerIP() + ":" + getServerList().get(i).getPort());
                 }
                 setCurrentServerIndex((int) (Math.random() * getServerList().size()));
                 return true;
             } else {
                 return false;
             }
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
             return false;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return false;
         } catch (SAXException e) {
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public static void saveFileListToXML() {
         try {
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             Document fileListXML = docBuilder.newDocument();
             Element rootElement = fileListXML.createElement("fileList");
             fileListXML.appendChild(rootElement);
 
             Element pathnameElement = fileListXML.createElement("pathname");
             pathnameElement.appendChild(fileListXML.createTextNode(getMyFileList().getPathname()));
             rootElement.appendChild(pathnameElement);
 
             for (String filename : getMyFileList().getFileHashTable().keySet()) {
                 Element fileInfoElement = fileListXML.createElement("fileInfo");
                 fileInfoElement.setAttribute("filename", filename);
                 rootElement.appendChild(fileInfoElement);
 
                 Element versionElement = fileListXML.createElement("version");
                 versionElement.appendChild(fileListXML.createTextNode(Integer.toString(getMyFileList().getFileHashTable().get(filename).getVersion())));
                 fileInfoElement.appendChild(versionElement);
 
                 Element deviceIDElement = fileListXML.createElement("deviceID");
                 deviceIDElement.appendChild(fileListXML.createTextNode(getMyFileList().getFileHashTable().get(filename).getDeviceID()));
                 fileInfoElement.appendChild(deviceIDElement);
 
                 Element fileNameElement = fileListXML.createElement("fileName");
                 fileNameElement.appendChild(fileListXML.createTextNode(filename));
                 fileInfoElement.appendChild(fileNameElement);
 
                 Element timestampElement = fileListXML.createElement("timestamp");
                 timestampElement.appendChild(fileListXML.createTextNode(getMyFileList().getFileHashTable().get(filename).getTimestamp()));
                 fileInfoElement.appendChild(timestampElement);
 
                 Element fileSizeElement = fileListXML.createElement("fileSize");
                 fileSizeElement.appendChild(fileListXML.createTextNode(Long.toString(getMyFileList().getFileHashTable().get(filename).getFileSize())));
                 fileInfoElement.appendChild(fileSizeElement);
 
                 Element fileHashCodeElement = fileListXML.createElement("fileHashCode");
                 fileInfoElement.appendChild(fileHashCodeElement);
 
                 for (String fileChunkName : getMyFileList().getFileInfoByFileName(filename).getFileHashCode().keySet()) {
                     Element fileChunkHashCodeElement = fileListXML.createElement("fileChunkHashCode");
                     fileChunkHashCodeElement.appendChild(fileListXML.createTextNode(getMyFileList().getFileInfoByFileName(filename).getFileHashCode().get(fileChunkName)));
                     fileChunkHashCodeElement.setAttribute("fileChunkName", fileChunkName);
                     fileHashCodeElement.appendChild(fileChunkHashCodeElement);
                 }
             }
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(fileListXML);
             StreamResult result = new StreamResult(new File("ClientCfg/users/" + getCurrentClient().getClientID() + "/filelist.xml"));
             transformer.transform(source, result);
 
         } catch (ParserConfigurationException pce) {
             pce.printStackTrace();
         } catch (TransformerException tfe) {
             tfe.printStackTrace();
         }
     }
 
     public static FileListService loadFileListFromXML() {
         DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder dombuilder = domfac.newDocumentBuilder();
             InputStream fis = new FileInputStream("ClientCfg/users/" + getCurrentClient().getClientID() + "/filelist.xml");
             Document doc = dombuilder.parse(fis);
             Element root = doc.getDocumentElement();
             NodeList fileListServiceNodeList = root.getChildNodes();
             if (fileListServiceNodeList != null) {
                 Node pathNameNode = fileListServiceNodeList.item(0);
                 if (pathNameNode.getNodeName().equals("pathname")) {
                     getMyFileList().setPathname(pathNameNode.getFirstChild().getNodeValue());
                 }
 
 //                System.out.println(fileInfoNode.getNodeName());
                 for (Node fileInfoNode = pathNameNode.getNextSibling(); fileInfoNode != null; fileInfoNode = fileInfoNode.getNextSibling()) {
                     FileInfo fileinfo = new FileInfo();
                     if (fileInfoNode.hasChildNodes()) {
                         Node versionNode = fileInfoNode.getFirstChild();
                         if (versionNode.getNodeName().equals("version")) {
                             fileinfo.setVersion(Integer.parseInt(versionNode.getFirstChild().getNodeValue()));
                         }
                         Node deviceIDNode = versionNode.getNextSibling();
                         if (deviceIDNode.getNodeName().equals("deviceID")) {
                             fileinfo.setDeviceID(deviceIDNode.getFirstChild().getNodeValue());
                         }
                         Node fileNameNode = deviceIDNode.getNextSibling();
                         if (fileNameNode.getNodeName().equals("fileName")) {
                             fileinfo.setFileName(fileNameNode.getFirstChild().getNodeValue());
                         }
                         Node timestampNode = fileNameNode.getNextSibling();
                         if (timestampNode.getNodeName().equals("timestamp")) {
                             fileinfo.setTimestamp(timestampNode.getFirstChild().getNodeValue());
                         }
                         Node fileSizeNode = timestampNode.getNextSibling();
                         if (fileSizeNode.getNodeName().equals("fileSize")) {
                             fileinfo.setFileSize(Long.parseLong(fileSizeNode.getFirstChild().getNodeValue()));
                         }
                         Node fileHashCodeNode = fileSizeNode.getNextSibling();
                         Hashtable<String, String> fileHashCode = new Hashtable<>();
 
                         for (Node node = fileHashCodeNode.getFirstChild(); node != null; node = node.getNextSibling()) {
                             fileHashCode.put(((Attr) node.getAttributes().item(0)).getValue(), node.getFirstChild().getNodeValue());
                         }
                         fileinfo.setFileHashCode(fileHashCode);
                         getMyFileList().getFileHashTable().put(((Attr) fileInfoNode.getAttributes().item(0)).getValue(), fileinfo);
                     }
                 }
             }
 
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return getMyFileList();
     }
 }
