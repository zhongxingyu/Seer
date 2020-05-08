 package dk.statsbiblioteket.doms.central.connectors.fedora.methods;
 
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
 import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
 import dk.statsbiblioteket.doms.central.connectors.fedora.Fedora;
 import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Method;
 import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Parameter;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.util.Pair;
import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 9/13/12
  * Time: 10:58 AM
  * To change this template use File | Settings | File Templates.
  */
 public class MethodsImpl implements Methods{
 
 
     Fedora fedora;
     private String thisLocation;
     private Unmarshaller jaxb;
 
 
 
     public MethodsImpl(Fedora fedora, String thisLocation) throws JAXBException {
         this.fedora = fedora;
         this.thisLocation = thisLocation;
         jaxb = JAXBContext.newInstance("dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated").createUnmarshaller();
     }
 
     @Override
     public String invokeMethod(String pid,String methodName,Map<String,List<String>> parameters, Long asOfTime, String logMessage) throws BackendInvalidResourceException, BackendInvalidCredsException, BackendMethodFailedException {
         List<Method> methods = getStaticMethods(pid,asOfTime);
 
         boolean staticMethod = true;
 
         // Find method
         Method chosenMethod = null;
         for (Method method : methods) {
             if (method.getName().equals(methodName)) {
                 chosenMethod = method;
                 break;
             }
         }
         if (chosenMethod == null){
             staticMethod = false;
             methods = getDynamicMethods(pid,asOfTime);
             for (Method method : methods) {
                 if (method.getName().equals(methodName)) {
                     chosenMethod = method;
                     break;
                 }
             }
         }
         if (chosenMethod == null){
             throw new BackendInvalidResourceException("Failed to find specified method");
         }
 
         String command = chosenMethod.getCommand();
 
         // Set default parameters
        parameters.put("domsUser", Arrays.asList(fedora.getUsername()));
        parameters.put("domsPassword", Arrays.asList(fedora.getPassword()));
        parameters.put("domsLocation", Arrays.asList(thisLocation));
         if (!staticMethod){
            parameters.put("domsPid", Arrays.asList(pid));
         }
 
         //replace parameter values
        for (Parameter declaredParameter : chosenMethod.getParameters().getParameter()) {
             String name = declaredParameter.getName();
             List<String> values = parameters.get(declaredParameter.getName());
             //Get defaults
             if (values == null) {
                 parameters.put(declaredParameter.getName(), Arrays.asList(declaredParameter.getDefault()));
             }
 
             String parameterString = "";
             String parameterprefix = declaredParameter.getParameterprefix();
             if (parameterprefix == null) {
                 parameterprefix = "";
             }
             for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
                 String value = iterator.next();
                 //shellescape value and prepend parameter prefix
                 value = parameterprefix + "'" + value.replaceAll("\'", "\'\\\\\'\'") + "'";
                 parameterString = parameterString + value;
                 if (iterator.hasNext()) {
                     parameterString = parameterString + " ";
                 }
             }
             command = command.replaceAll("%%" + name + "%%", parameterString);
         }
 
         // Run command
         List<String> commandList = new ArrayList<String>();
         commandList.add("/bin/bash");
         commandList.add("-c");
         commandList.add(command);
         ProcessBuilder procesBuilder = new ProcessBuilder(commandList);
         try {
             Process process = procesBuilder.start();
             int returnCode = process.waitFor();
             StringWriter writer = new StringWriter();
             IOUtils.copy(process.getInputStream(),writer);
 
             if (returnCode == 0) {
             } else {
                 IOUtils.copy(process.getErrorStream(),writer);
             }
             return writer.toString();
         } catch (Exception e){
             throw new BackendMethodFailedException("Failed to run command " + commandList.toString(), e);
         }
     }
 
     public List<Method> getDynamicMethods(String objpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         ObjectProfile profile = fedora.getObjectProfile(objpid, null);
         List<Method> result = new ArrayList<Method>();
         for (String contentModelPid : profile.getContentModels()) {
             List<Method> methods = getMethods(contentModelPid,asOfTime);
             for (Method method : methods) {
                 if (method.getType().equals("dynamic")){
                     result.add(method);
                 }
             }
         }
         return result;
     }
 
 
     private List<Method> getMethods(String cmpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         String methodsXml = null;
         try {
             //TODO check that the model is in fact a content model??
             methodsXml = fedora.getXMLDatastreamContents(cmpid, "METHODS",asOfTime);
         } catch (BackendInvalidResourceException e) {
             return new ArrayList<Method>();
         }
 
         try {
             JAXBElement<dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Methods> parsed = (JAXBElement<dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Methods>) jaxb.unmarshal(new StringReader(methodsXml));
             return parsed.getValue().getMethod();
         } catch (JAXBException e) {
             throw new BackendMethodFailedException("failed to parse Methods definition",e);
         }
     }
 
 
     @Override
     public List<Method> getStaticMethods(String cmpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         List<Method> result = new ArrayList<Method>();
         List<Method> methods = getMethods(cmpid,asOfTime);
         for (Method method : methods) {
             if (method.getType().equals("static")){
                 result.add(method);
             }
         }
         return result;
 
     }
 
 }
