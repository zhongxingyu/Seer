 package comtech.staxer.plugin;
 
 import comtech.staxer.StaxerUtils;
 import comtech.staxer.domain.WebService;
 import comtech.util.ResourceUtils;
 import comtech.util.xml.XmlConstants;
 import comtech.util.xml.XmlUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 
 /**
  * Ws-client stub generator goal
  *
  * @author Vlad Vinichenko (akerigan@gmail.com)
  * @goal ws-beans
  * @phase generate-sources
  */
 public class WsBeansMojo extends AbstractMojo {
 
     /**
      * Location base dir
      *
      * @parameter expression="${basedir}"
      * @required
      */
     private File baseDir;
 
     /**
      * URL of wsdl file or relative (based on module path) path to wsdl file
      *
      * @parameter
      * @required
      */
     private String wsdlUrl;
 
     /**
      * Charset of wsdl file
      *
      * @parameter default-value="UTF-8"
      */
     private String wsdlCharset;
 
     /**
      * Relative path for stub saving
      *
      * @parameter
      * @required
      */
     private String sourceDir;
 
     /**
      * Relative path for description saving
      *
      * @parameter
      */
     private String definitionPath;
 
     /**
      * Name of generated package
      *
      * @parameter
      * @required
      */
     private String packageName;
 
     /**
      * Http basic auth login
      *
      * @parameter
      */
     private String httpUser;
 
     /**
      * Http basic auth login
      *
      * @parameter
      */
     private String httpPassword;
 
     /**
      * Generate ws client service
      *
      * @parameter
      */
     private boolean createClientService;
 
     /**
      * Generate server operations
      *
      * @parameter
      */
     private boolean createServerService;
 
     public void execute() throws MojoExecutionException {
         try {
             WebService webService;
             if (wsdlUrl.startsWith("http")) {
                 String xml = ResourceUtils.getUrlContentAsString(wsdlUrl, httpUser, httpPassword);
                 if (xml != null) {
                     webService = XmlUtils.readXml(
                             new StringReader(xml), WebService.class,
                             XmlConstants.XML_NAME_WSDL_DEFINITIONS
                     );
                 } else {
                     throw new MojoExecutionException("Url content is empty");
                 }
             } else {
                 File wsdlFile = new File(baseDir, wsdlUrl);
                 InputStream inputStream = new FileInputStream(wsdlFile);
                 webService = XmlUtils.readXml(
                         inputStream, wsdlCharset, WebService.class,
                         XmlConstants.XML_NAME_WSDL_DEFINITIONS
                 );
                 inputStream.close();
             }
             if (webService != null) {
                 StaxerUtils.createJavaWebService(
                        webService, new File(baseDir, sourceDir), packageName, true, createServerService, createClientService
                 );
             } else {
                 throw new MojoExecutionException("Web service is empty");
             }
         } catch (Exception e) {
             throw new MojoExecutionException("Cant generate java ws beans", e);
         }
     }
 }
