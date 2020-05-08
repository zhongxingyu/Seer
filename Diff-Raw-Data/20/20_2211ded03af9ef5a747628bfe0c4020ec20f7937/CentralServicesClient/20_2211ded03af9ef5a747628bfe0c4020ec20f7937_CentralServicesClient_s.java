 /*
  * Created on Feb 17, 2009
  */
 package com.delcyon.updater.client;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.Preferences;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.updater.client.LeveledConsoleHandler.Output;
 
 
 
 
 
 /**
  * @author jeremiah
  * @version $Id: $
  */
 
 public class CentralServicesClient 
 {
 
     public static Level LOGGING_LEVEL = Level.INFO;
     public static Logger logger = null;
     private static LeveledConsoleHandler leveledConsoleHandler;
     private static FileHandler fileHandler;
     private static String logFileName = null;   
     
     private Hashtable<String, String> processingPropertiesHashtable = new Hashtable<String, String>();
    
     
     /**
      * @throws Exception 
      * 
      */
     public CentralServicesClient() throws Exception
     {
         logger = Logger.getLogger(this.getClass().getName());
         logger.setLevel(LOGGING_LEVEL);
         if (leveledConsoleHandler == null)
         {
             leveledConsoleHandler = new LeveledConsoleHandler();
             leveledConsoleHandler.setLevel(LOGGING_LEVEL);
             leveledConsoleHandler.setOutputForLevel(Output.STDERR, Level.FINER);
             logger.setUseParentHandlers(false);
             logger.addHandler(leveledConsoleHandler);           
         }
         logger.log(Level.INFO, "Starting Installer ");
 
         if (fileHandler == null)
         {
             logFileName = "install.log";
             logger.log(Level.FINE, "Opening LogElement File:" + logFileName);
             fileHandler = new FileHandler(logFileName);
             fileHandler.setLevel(LOGGING_LEVEL);
             logger.addHandler(fileHandler);
         }
         
      // load environment
         Set<Entry<String, String>> envEntrySet = System.getenv().entrySet();
         for (Entry<String, String> entry : envEntrySet)
         {
             processingPropertiesHashtable.put(entry.getKey().toString(), entry.getValue().toString());
         }
 
         // load system properties
         Set<Entry<Object, Object>> propEntrySet = System.getProperties().entrySet();
         for (Entry<Object, Object> entry : propEntrySet)
         {
             processingPropertiesHashtable.put(entry.getKey().toString(), entry.getValue().toString());
         }
     }
     
     /* (non-Javadoc)
      * @see com.delcyon.cs.application.Application#getApplicationName()
      */
     public String getApplicationName()
     {
         return "Central Services Client";
     }
     
     
     
 
     
     public void runStatusCheck() throws Exception
     {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document statusDocument = documentBuilder.parse(new ByteArrayInputStream(new byte[]{}));
         
         
         processStatusDocument(statusDocument);
         CentralServicesClient.logger.log(Level.INFO, "Finished Run. Exiting...");
     }
 
     /**
      * @param statusDocument
      * @throws Exception 
      */
     void processStatusDocument(Document statusDocument) throws Exception
     {
         NodeList controlElementList = statusDocument.getDocumentElement().getChildNodes();
         for (int index = 0; index < controlElementList.getLength(); index++)
         {
             if (controlElementList.item(index).getNodeType() != Node.ELEMENT_NODE)
             {
                 continue;
             }
             Element controlElement = (Element) controlElementList.item(index);
             processControlElement(controlElement);
         }
         
     }
 
     
 
     /**
      * @param controlElement
      * @throws Exception 
      */
    @SuppressWarnings("unchecked")
     private void processControlElement(Element controlElement) throws Exception
     {
         CentralServicesClient.logger.log(Level.FINE, "Processing Control Element "+controlElement.getAttribute("name"));
 
 
         String controlType = controlElement.getNodeName();
 
 
         if(controlType.equals("copy"))
         {
 
             processCopyElement(controlElement);
         }        
         else if(controlType.equals("ask"))
         {
             processAskElement(controlElement);                    
         }   
         else if(controlType.equals("shellcommand"))
         {
             processShellCommand(controlElement);                  
         }   
         else if(controlType.equals("pref"))
         {
 
             processPrefElement(controlElement);
         }
         else
         {
 
             //load attributes if we don't know what kind of element this is
             NamedNodeMap atrributeList = controlElement.getAttributes();
             for (int attributeIndexindex = 0; attributeIndexindex < atrributeList.getLength() ; attributeIndexindex++)
             {                        
                 Attr attribute = (Attr) atrributeList.item(attributeIndexindex);
                 setVar(attribute.getName(), attribute.getValue());                      
             }
 
         }
 
     }
 
     /**
      * @param childElement
      */
     private void processPrefElement(Element childElement) throws Exception
     {
         String path = processVariablesInString(childElement.getAttribute("path"),true);
         Preferences preferences =  Preferences.systemRoot().node(path);
         
         if (childElement.hasAttribute("set"))
         {
             String preferenceName =  processVariablesInString(childElement.getAttribute("set"),true);
             String varName = processVariablesInString(childElement.getAttribute("var"),true);
             if (childElement.hasAttribute("var") == false)
             {
                 varName = preferenceName;
             }
             preferences.put(preferenceName, getVar(varName));
             preferences.sync();
         }
        
         if (childElement.hasAttribute("get"))
         {
             String preferenceName =  processVariablesInString(childElement.getAttribute("get"),true);
             String property = processVariablesInString(childElement.getAttribute("var"),true);
             if (childElement.hasAttribute("var") == false)
             {
                 property = preferenceName;
             }
             setVar(property, preferences.get(preferenceName, ""));
         }
     }
 
     /**
      * @param childElement
      * @throws Exception 
      */
     private void processAskElement(Element childElement) throws Exception
     {
       
           if (childElement.hasAttribute("if"))
           {
               String xpath = processVariablesInString( childElement.getAttribute("if"),true);
               CentralServicesClient.logger.log(Level.FINE, "evaluating "+xpath);
               boolean result = XMLUtils.evaluateXPath(childElement, xpath);
               if (result == false)
               {
                   return;                    
               }
           }
           System.out.println(childElement.getAttribute("message"));
           if(childElement.hasAttribute("default"))
           {
               String defaultValue = processVariablesInString(childElement.getAttribute("default"),false);
               if (defaultValue.isEmpty() == false)
               {
                   setVar(childElement.getAttribute("var"), defaultValue);
                   System.out.println("Default ["+defaultValue+"]");
               }
               
           }
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String value = br.readLine();
             if (value.isEmpty() == false)
             {
                 setVar(childElement.getAttribute("var"), value);
             }
             
       
       
     }
 
     private void processShellCommand(Element childElement) throws Exception {
         String ifProperty = childElement.getAttribute("if");
         if (childElement.hasAttribute("if"))
         {
             if(processingPropertiesHashtable.containsKey(ifProperty))
             {
                 if (processingPropertiesHashtable.get(ifProperty).equals("true") == false)
                 {
                     if (childElement.hasAttribute("onSkip"))
                     {
                         processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                     }
                     CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
                     return ;
                 }
             }
             else
             {
                 CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
                 if (childElement.hasAttribute("onSkip"))
                 {
                     processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
                 }
                 return ;
             }
         }
         File workingDir = null;
         if (childElement.hasAttribute("workingDir") == true)
         {
             workingDir = new File(childElement.getAttribute("workingDir"));
         }
         
         runCommand(childElement.getAttribute("exec"), workingDir);
         if (childElement.hasAttribute("onAction") == true)
         {
             processingPropertiesHashtable.put(childElement.getAttribute("onAction"),"true");
         }
     }
 
     
         
     
     
     
     private String runCommand(String command, File workingDir)throws Exception
     {
         String output = null;
         CentralServicesClient.logger.log(Level.INFO, "Running "+command);
         String[] commandArray = {"/bin/sh","-c",command};
         Process process = Runtime.getRuntime().exec(commandArray,null,workingDir);
         
         ByteArrayOutputStream errorByteArrayOutputStream = new ByteArrayOutputStream();
         ByteArrayOutputStream outputByteArrayOutputStream = new ByteArrayOutputStream();
         
         int errorValue = 0;
         int outputValue = 0;
         
         InputStream errorInputStream = process.getErrorStream();
         InputStream outputInputStream = process.getInputStream();
         
         while (outputValue >= 0 || errorValue >= 0)
         {
             
             if (outputValue >= 0)
             {
                 if (outputValue != 0)
                 {
                     outputByteArrayOutputStream.write(outputValue);
                 }
                 outputValue = outputInputStream.read();
             }
             if (errorValue >= 0)
             {
                 if (errorValue != 0)
                 {
                     errorByteArrayOutputStream.write(errorValue);
                 }
                 errorValue = errorInputStream.read();                
             }
         }
         
         String warningMessage = new String(errorByteArrayOutputStream.toByteArray());
         if (warningMessage != null && warningMessage.trim().length() != 0)
         {
             CentralServicesClient.logger.log(Level.WARNING, command+":\n"+warningMessage.trim());
         }
         String infoMessage = new String(outputByteArrayOutputStream.toByteArray());
         if (infoMessage != null && infoMessage.trim().length() != 0)
         {
             CentralServicesClient.logger.log(Level.INFO, infoMessage.trim());
             output = infoMessage.trim();
         }
         
         return output;
     }
     
     /**
      * @param copyElement
      */
     private boolean processCopyElement(Element copyElement)
     {
         try
         {
             String masterFileName = processVariablesInString(copyElement.getAttribute("src"),true);
             String destinationFileName =  processVariablesInString(copyElement.getAttribute("dest"),true);
             //String ifProperty =  processVariablesInString(childElement.getAttribute("if"),true);
 
             if (copyElement.hasAttribute("if"))
             {
                 String xpath = processVariablesInString(copyElement.getAttribute("if"),true);            
                 boolean result = XMLUtils.evaluateXPath(copyElement, xpath);
                 CentralServicesClient.logger.log(Level.FINE, "evaluating "+xpath+" "+result);
                 if (result == false)
                 {
                     return false;                    
                 }
             }
             
             
 //            if (childElement.hasAttribute("if"))
 //            {
 //                if(processingPropertiesHashtable.containsKey(ifProperty))
 //                {
 //                    if (processingPropertiesHashtable.get(ifProperty).equals("true") == false)
 //                    {
 //                        if (childElement.hasAttribute("onSkip"))
 //                        {
 //                            processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
 //                        }
 //                        CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
 //                        return false;
 //                    }
 //                }
 //                else
 //                {
 //                    CentralServicesClient.logger.log(Level.FINE, "Skipping "+ifProperty+" not == true");
 //                    if (childElement.hasAttribute("onSkip"))
 //                    {
 //                        processingPropertiesHashtable.put(childElement.getAttribute("onSkip"),"true");
 //                    }
 //                    return false;    
 //                }
 //            }
             CentralServicesClient.logger.log(Level.INFO, "Processing Copy Element "+masterFileName+" ==> "+destinationFileName);
             File file = new File(destinationFileName);
             if (file.exists() == false)
             {
                 String path = file.getCanonicalPath();
                 path = path.substring(0, path.lastIndexOf(File.separator+file.getName()));
                 File dirs = new File(path);
                 if (dirs.exists() == false)
                 {
                     CentralServicesClient.logger.log(Level.INFO, "Creating Directory: "+dirs.getCanonicalPath());
                     dirs.mkdirs();
                 }
                 file.createNewFile();
                 
             }
             String srcMD5 = null;
             if (copyElement.hasAttribute("md5"))
             {
                 srcMD5 = copyElement.getAttribute("md5");
             }
             else
             {
                 OutputStream nullOutputStream = new NullOutputStream();
                 srcMD5 = readClientVersionStreamIntoOutputStream(masterFileName, nullOutputStream,copyElement);
             }
             String destMd5 = FileUtility.getMD5ForFile(destinationFileName);
             if (destMd5 == null || srcMD5.equals(destMd5) == false)
             {
                 
                 
                 
                 CentralServicesClient.logger.log(Level.INFO, "Copying "+destinationFileName);
                 FileOutputStream fileOutputStream = new FileOutputStream(destinationFileName, false);
                 
                 readClientVersionStreamIntoOutputStream(masterFileName, fileOutputStream, copyElement);
                 
                
                 
                 if (copyElement.hasAttribute("chmod"))
                 {
                     runCommand("/bin/chmod "+copyElement.getAttribute("chmod")+" "+destinationFileName, null);
                 }
                 if (copyElement.hasAttribute("chown"))
                 {
                     runCommand("/bin/chown "+copyElement.getAttribute("chown")+" "+destinationFileName, null);
                 }
                 if (copyElement.hasAttribute("onAction"))
                 {
                     processingPropertiesHashtable.put(copyElement.getAttribute("onAction"),"true");
                 }
                 return true;
             }
             else
             {
                 CentralServicesClient.logger.log(Level.INFO, "File already up to date, skipping.");
                 if (copyElement.hasAttribute("onSkip"))
                 {
                     processingPropertiesHashtable.put(copyElement.getAttribute("onSkip"),"true");
                 }
                 return false;
             }
         }
         catch (Exception e)
         {
             
             e.printStackTrace();
             if (copyElement.hasAttribute("onError"))
             {
                 processingPropertiesHashtable.put(copyElement.getAttribute("onError"),"true");
             }
         }
         return false;
     }
    
     
 
     private void readStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream) throws Exception
     {
         byte[] buffer = new byte[1024];
         int bytesRead = 0;
         while (bytesRead >= 0)
         {
             bytesRead = inputStream.read(buffer);
             if (bytesRead == -1)
             {
                 break;
             }
             outputStream.write(buffer,0,bytesRead);
         }
         outputStream.flush();
     }
 
     public static Document initDocument() throws Exception
     {
         
         
         Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         Element rootElement = document.createElement("agent");
         document.appendChild(rootElement);
 
         // load environment
         Set<Entry<String, String>> envEntrySet = System.getenv().entrySet();
         for (Entry<String, String> entry : envEntrySet)
         {
             Element variableElement = document.createElement("variable");
             variableElement.setAttribute("name", entry.getKey());
             variableElement.setAttribute("value", entry.getValue());
             rootElement.appendChild(variableElement);
             CentralServicesClient.logger.log(Level.FINE, "Storing "+variableElement.getAttribute("name")+" ==> "+variableElement.getAttribute("value"));
         }
 
         // load system properties
         Set<Entry<Object, Object>> propEntrySet = System.getProperties().entrySet();
         for (Entry<Object, Object> entry : propEntrySet)
         {
             Element variableElement = document.createElement("variable");
             variableElement.setAttribute("name", entry.getKey().toString());
             variableElement.setAttribute("value", entry.getValue().toString());
             rootElement.appendChild(variableElement);
             CentralServicesClient.logger.log(Level.FINE, "Storing "+variableElement.getAttribute("name")+" ==> "+variableElement.getAttribute("value"));
         }
         
         
         return document;
     }
 
    
 
     public boolean hasVar(String varName)
     {
         return processingPropertiesHashtable.containsKey(varName);
     }
  
     public void setVar(String key, String value)
     {        
         CentralServicesClient.logger.log(Level.FINE, "Storing '"+key+"' => '"+value+"'");
         processingPropertiesHashtable.put(key, value);
     }
 
     /**
      * @param varName
      * @return
      */
     public String getVar(String varName)
     {
         return getVar(varName,false);
     }
     
     
     public String getVar(String varName,boolean emptyOK)
     {
         //Thread.dumpStack();
         String value = "";
         
          
        
 
         if (value.length() == 0)
         {
             //check for var name replacement
             if (varName.matches(".*\\$\\{.*\\}.*"))
             {
                 varName = processVariablesInString(varName,emptyOK);
             }
 
 
 
             if (processingPropertiesHashtable.containsKey(varName))
             {
                 value = processingPropertiesHashtable.get(varName);
             }
             
         }
         if (value.matches(".*\\$\\{.*\\}.*"))
         {
             value = processVariablesInString(value,emptyOK);
         }
         
         return value;
         
     }
     
     
     
     public String processVariablesInString(String name,boolean emptyOK)
     {
         
         String[] variables = getVariableNames(name);
         for (String variableName : variables)
         {
             String replacement = getVar(variableName,emptyOK);
             if (replacement.length() != 0 || emptyOK)
             {
                 name = name.replaceAll("\\$\\{"+variableName+"\\}", replacement);
             }            
         }
         return name;
     }
     
     private String[] getVariableNames(String variableString)
     {
         String[] variables = {};
         Vector<String> varNameVector = new Vector<String>();
         String[] split = variableString.split("\\$");
         for (String string : split)
         {
             if (string.startsWith("{"))
             {
                 int endIndex = string.indexOf('}');
                 String varName = string.substring(1, endIndex);
                 varNameVector.add(varName);
             }
         }
         return varNameVector.toArray(variables);
     }
 
     /**
      * 
      * @param masterFileName
      * @param outputStream
      * @param copyElement
      * @return md5 of written stream
      * @throws Exception
      */
     public String readClientVersionStreamIntoOutputStream(String masterFileName, OutputStream outputStream,  Element copyElement) throws Exception
     {
         String copysrcDir = "";//Application.getConfiguration().getValue("CENTRAL_SERVICES_COPYSOURCE_DIR") + File.separator;
         if (masterFileName.startsWith(File.separator))
         {
             copysrcDir = "";
         }
        
        InputStream fileInputStream = UpdaterClient.classLoader.getResource(masterFileName).openStream();
         MD5FilterOutputStream md5rootOutputStream = new MD5FilterOutputStream(outputStream);
         OutputStream rootOutputStream = md5rootOutputStream;
 
         NodeList childNodes = copyElement.getChildNodes();
         for(int index = 0; index < childNodes.getLength(); index++)
         {
             if (childNodes.item(index) instanceof Element)
             {
                 Element childElement = (Element) childNodes.item(index);
                 if (childElement.getNodeName().equals("filter"))
                 {
                     CSFilterOutputStream filterOutputStream = new CSFilterOutputStream(processVariablesInString(childElement.getAttribute("trigger"),false),processVariablesInString(childElement.getAttribute("replacement"),false), rootOutputStream);
                     rootOutputStream = filterOutputStream;
                 }
             }
         }
         
         
 
         CentralServicesClient.logger.log(Level.FINE, "Copying " + masterFileName);
         readStreamIntoOutputStream(fileInputStream, rootOutputStream);
         return md5rootOutputStream.getMD5();
     }
 
     public class NullOutputStream extends OutputStream
     {
         @Override 
         public void write(int b) throws IOException{}
     }
     
     
     public class MD5FilterOutputStream extends FilterOutputStream
     {
         private MessageDigest messageDigest;
 
         protected MD5FilterOutputStream(OutputStream out) throws NoSuchAlgorithmException
         {
             super(out);
             messageDigest = MessageDigest.getInstance("MD5");
             messageDigest.reset();
         }
 
         @Override
         public void write(int b) throws IOException
         {
             messageDigest.update((byte) b);
             super.write(b);
         }
 
         /**
          * override write method
          */
         @Override
         public void write(byte[] data, int offset, int length) throws IOException
         {
             for (int i = offset; i < offset + length; i++)
             {
                 this.write(data[i]);
             }
         }
 
         /**
          * override write method
          */
         @Override
         public void write(byte[] b) throws IOException
         {
             write(b, 0, b.length);
         }
 
         public String getMD5()
         {
             return new BigInteger(1, messageDigest.digest()).toString(16);
         }
     }
 }
