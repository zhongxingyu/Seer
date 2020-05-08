 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dynamicNetworking.calculator.operationsServer;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 
 /**
  *
  * @author steffen
  */
 public class OperationsServer {
    
     private Properties prop;
     private HTTPClassLoader classLoader;
     
     private HashMap<String, Class> knownClasses;
 
     public OperationsServer() {
         this("OperationsServer.properties");
     }
 
     public OperationsServer(String propFile) {
         prop = new Properties();
         knownClasses = new HashMap<>();
 
         try {
             //load a properties file
            prop.load(this.getClass().getResourceAsStream(propFile));
         } catch (IOException ex) {
             System.err.println("Failed to open properties file.");
         }
         
         //get the property value and save
         String rootDirectory = prop.getProperty("CLASS_ROOT_DIRECTORY");
         System.out.println(rootDirectory);
         String host = prop.getProperty("HOST");
         System.out.println(host);
         String port = prop.getProperty("PORT");
         System.out.println(port);
         
         if (rootDirectory == null ||
                 host == null ||
                 port == null)
         {
             System.err.println("Invalid properties file");
         }
         
         int portNum = Integer.parseInt(port);
         
         classLoader = new HTTPClassLoader(rootDirectory, host, portNum);
     }
 
     public Operation getOperation(String operationString) 
         throws UnknownOperationException 
     {
         Operation op = null;
         try {
             if (knownClasses.get(operationString) != null) {
                 // we already know this operation. Don't ask server for it!
                 op = (Operation) knownClasses.get(operationString).newInstance();
             } else {
                 String opName = prop.getProperty(operationString);
                 
                 if (opName == null) {
                     throw new UnknownOperationException("Operation: " +
                             operationString + " is not known");
                 }
                 
                 Class opClass = classLoader.findClass(opName);
                 op = (Operation) opClass.newInstance();
                 
                 // Stash this class
                 knownClasses.put(operationString, opClass);
             }
         } catch (InstantiationException e) {
             System.err.println("Instantiation Exception!!!");
         } catch (IllegalAccessException e) {
             System.err.println("IllegalAccessException!!!");
         }
         return op;
     }
 }
