 package LinkFuture.Web;
 
 import LinkFuture.Core.Config;
 import LinkFuture.Core.ConfigurationManager.ConfigurationController;
 import LinkFuture.Core.Extensions.PathExtension;
 import LinkFuture.Core.Extensions.StringExtension;
 import LinkFuture.Core.MemoryManager.MemoryController;
 import LinkFuture.Core.Utility;
 import LinkFuture.Core.WebClient.WebClientResultInfo;
 import LinkFuture.Web.HttpContextManager.HttpContextController;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.*;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URI;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.HashMap;
 
 import static LinkFuture.Web.StaticFileType.*;
 
 /**
  * LinkFuture.
  * User: Cyokin Zhang
  * Date: 10/24/13
  * Time: 2:44 PM
  */
 public class SiteInfo{
     public static String BuildJsFileReference(String outputFileName)
     {
         return  BuildJsFileReference(outputFileName, ConfigurationController.IsDebug, false, true);
     }
     public static String BuildInlineJsFileReference(String outputFileName)
     {
         return  BuildJsFileReference(outputFileName, ConfigurationController.IsDebug, false, false);
     }
    /*
    * Read file from remote if not local, otherwise read from local
    *
    * */
    public static String ReadFile(String fileUrl) throws IOException {
        if(!StringExtension.IsNullOrEmpty(HttpContextController.ApplicationDomain) && fileUrl.startsWith(HttpContextController.ApplicationDomain))
        {
            String localPath = PathExtension.FullPhysicalPath(StringExtension.TrimStart(fileUrl,HttpContextController.ApplicationDomain));
            return Utility.getStringFromFile(localPath);
        }
        else
        {
           return Utility.sendRequest(fileUrl).response;
        }
    }
     public static String BuildUnlockJsFileReference(String loadJSFileName,String outputFileName,String callbackFunction)
     {
         StringWriter sb = new StringWriter();
         sb.append(SiteInfo.BuildInlineJsFileReference(loadJSFileName));
         sb.append("<script language=\"javascript\"> $loadJS(");
         sb.append(BuildJsFileReference(outputFileName, ConfigurationController.IsDebug, true, true));
         sb.append(String.format(",function(){%s});",callbackFunction));
         sb.append("</script>");
         return sb.toString();
     }
     public static String BuildCssFileReference(String outputFileName)
     {
         return  BuildCssFileReference(outputFileName, ConfigurationController.IsDebug, false, true);
     }
     public static String BuildJsFileReference(String outputFileName,boolean isDebug,boolean isUnblocked,boolean isReference){
         try {
             return BuildStaticFileReference(outputFileName,ConfigurationController.DefaultJsCrunchFilePath, isDebug, StaticFileType.JavaScript, isUnblocked, isReference);
         }
         catch (Exception ex)
         {
             throw new RuntimeException(ex);
         }
     }
     public static String BuildCssFileReference(String outputFileName,boolean isDebug,boolean isUnblocked,boolean isReference) {
         try {
             return BuildStaticFileReference(outputFileName, ConfigurationController.DefaultCssCrunchFilePath,isDebug, StaticFileType.Css, isUnblocked, isReference);
         }
         catch (Exception ex)
         {
             throw new RuntimeException(ex);
         }
     }
     private static final HashMap<String,String> StaticOutputCache = new HashMap<String,String>();
     public static String BuildStaticFileReference(String outputFileName,String crunchFilePath,boolean isDebug, StaticFileType type,boolean isUnblocked,boolean isReference) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
 
         StringWriter sw = new StringWriter();
         sw.append(outputFileName);
         sw.append(crunchFilePath);
         sw.append(isDebug?"true":"false");
         sw.append(type.toString());
         sw.append(isUnblocked?"true":"false");
         sw.append(isReference?"true":"false");
         String key = sw.toString().toLowerCase();
         if(!StaticOutputCache.containsKey(key))
         {
             System.out.println("Build " + outputFileName);
             String crunchSetting = Utility.getStringFromFile(crunchFilePath);
             Document doc = Utility.parseXmlDom(crunchSetting);
             doc.getDocumentElement().normalize();
             XPathFactory xPathfactory = XPathFactory.newInstance();
             XPath xpath = xPathfactory.newXPath();
             XPathExpression expr = xpath.compile("root/output[@path='" + outputFileName + "']/node()");
             NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
             StringBuilder staticReference = new StringBuilder();
             if (isUnblocked && type == StaticFileType.JavaScript && isReference)
             {
                 staticReference.append("[");
             }
             for(int i=0;i<nl.getLength();i++)
             {
                 Node item = nl.item(i);
                 if(item.getNodeType() == Node.ELEMENT_NODE){
                     String filePath = item.getAttributes().getNamedItem("path").getNodeValue();
                     if(isDebug)
                     {
                         if(item.getNodeName().equals("input"))
                         {
                             staticReference.append(GetStaticFileOutput(type, filePath, isUnblocked,isReference));
                         }
                     }
                     if(item.getNodeName().equals("remote"))
                     {
                         staticReference.append(GetStaticFileOutput(type, filePath, isUnblocked,isReference));
                     }
                 }
             }
             if (!isDebug)
             {
                 staticReference.append(GetStaticFileOutput(type, outputFileName, isUnblocked, isReference));
             }
             String output = staticReference.toString();
             if (isUnblocked && type == StaticFileType.JavaScript && isReference)
             {
                 output = output.substring(0,output.length() -1) + "]";
             }
             StaticOutputCache.put(key,output);
             return output;
         }
         return StaticOutputCache.get(key);
     }
     private static String GetStaticFileOutput(StaticFileType type, String outputFileName, boolean isUnblocked, boolean isReference)
     {
         switch (type)
         {
             case JavaScript:
                 String jsFilePath = JSUri(outputFileName);
                 if (isUnblocked && isReference)
                 {
                     return String.format("\"%s\",", jsFilePath);
                 }
                 if (!isReference)
                 {
                     try {
                        return String.format("<script type=\"text/javascript\">%s</script>",ReadFile(jsFilePath));
                    } catch (Exception e) {
                         return String.format("<script type=\"text/javascript\">/*download js file failed(%s)*/</script>",jsFilePath);
                     }
                 }
                 return String.format("<script type=\"text/javascript\" src=\"%s\"></script>", jsFilePath);
             case Css:
                 String cssFilePath = CssUri(outputFileName);
                 if (!isReference)
                 {
                     try {
                         return String.format("<style type=\"text/css\">%s</style>",Utility.sendRequest(cssFilePath).response);
                     } catch (IOException e) {
                         return String.format("<style type=\"text/css\">/*download css file failed(%s)*/</style>",cssFilePath);
                     }
                 }
                 return String.format("<link rel=\"Stylesheet\" type=\"text/css\" href=\"%s\" />", cssFilePath);
             default:
                 throw new IllegalArgumentException("type");
         }
     }
     public static String SiteUri(String relativePath)
     {
         if(StringExtension.IsNullOrEmpty(relativePath))
         {
             return HttpContextController.ApplicationDomain;
         }
         relativePath = relativePath.replace("\\","/");
         if(IsWellFormedUriString(relativePath))
         {
             return relativePath;
         }
         if(relativePath.startsWith("/") || relativePath.startsWith("\\"))
         {
             return HttpContextController.ApplicationDomain + relativePath;
         }
         return  relativePath;
     }
     public static Boolean IsWellFormedUriString(String url)
     {
         return url.startsWith("http://") || url.startsWith("https://");
     }
     public static String SiteUri(String configSettingName,String relativePath) {
         if(IsWellFormedUriString(relativePath))
         {
             return relativePath;
         }
         return SiteUri(ConfigurationController.AppSettings(configSettingName) + relativePath);
     }
     public static String JSUri(String relativePath)
     {
           return SiteUri("JsFileFolder",relativePath);
     }
     public static String CssUri(String relativePath)
     {
         return SiteUri("CssFileFolder",relativePath);
     }
 }
