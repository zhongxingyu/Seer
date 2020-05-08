 package gov.nih.nci.cagrid.introduce.servicetasks.deployment.validator;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.common.XMLUtilities;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.jdom.Element;
 
 
 /**
  * DeploymentValidatorTask
  * Validates a deployment by executing the deployment validator classes specified
  * in a service's deployment descriptor.
  * 
  * <note>This class used to deserialize a bean to handle this, however that causes
  * problems when the code is executed out of the caGrid installer under
  * Windows XP sp 3.  For more details, see https://jira.citih.osumc.edu/browse/CAGRID-404</note>
  * 
  * @author David
  */
 public class DeploymentValidatorTask extends Task {
 
     public static final String DEPLOYMENT_VALIDATOR_FILE = "deploymentValidator.xml";
     public static final QName DEPLOYMENT_VALIDATOR_QNAME = new QName(
         "gme://gov.nih.nci.cagrid.introduce/1/DeploymentValidator", "DeploymentValidatorDescriptor");
 
 
     public void execute() throws BuildException {
         super.execute();
         
         try {
             Properties properties = new Properties();
             properties.putAll(this.getProject().getProperties());
             
             String baseDir = this.getProject().getBaseDir().getAbsolutePath();
             
             File deploymentDescriptor = new File(baseDir,
                 "tools" + File.separator + DeploymentValidatorTask.DEPLOYMENT_VALIDATOR_FILE);
             if (!deploymentDescriptor.exists() || !deploymentDescriptor.canRead()) {
                 String message = "Deployment descriptor (" + deploymentDescriptor.getAbsolutePath() 
                 + ") doesn't exist or can't be read!";
                 System.out.println(message);
                 throw new Exception(message + 
                     "  Service does not seem to be an Introduce generated service or file system permissions are preventing access.");
             }
             
             Element deploymentValidatorDescriptorElem = null;
             try {
                 StringBuffer buff = Utils.fileToStringBuffer(deploymentDescriptor);
                 deploymentValidatorDescriptorElem = XMLUtilities.stringToDocument(buff.toString()).getRootElement();
             } catch (Exception e) {
                 throw new Exception("Cannot deserialize deployment validator descriptor: " + deploymentDescriptor, e);
             }
             
            // get the validator elements, regardless of their namespace
             List validatorDescriptorElements = deploymentValidatorDescriptorElem.getChildren(
                "ValidatorDescriptor", null);
             
             if (validatorDescriptorElements != null && validatorDescriptorElements.size() != 0) {
                System.out.println("Found " + validatorDescriptorElements.size() + " validator descriptors");
                 Iterator<Element> validatorDescriptorElemIter = validatorDescriptorElements.iterator();
                 while (validatorDescriptorElemIter.hasNext()) {
                     Element validatorDescriptorElem = validatorDescriptorElemIter.next();
                     String validatorClass = validatorDescriptorElem.getAttributeValue("validationClass");
                     if (validatorClass != null) {
                        System.out.println("Loading validator " + validatorClass);
                         Class clazz = Class.forName(validatorClass);
                         Constructor con = clazz.getConstructor(new Class[]{String.class});
                         DeploymentValidator validator = (DeploymentValidator) con.newInstance(new Object[]{baseDir});
                        System.out.println("\tExecuing validator...");
                         validator.validate();
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new BuildException(e.getMessage(), e);
         }
         System.out.println("Done");
     }
 }
