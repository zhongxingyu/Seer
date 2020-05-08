 import iTests.framework.utils.LogUtils;
 import org.swift.common.soap.confluence.ConfluenceSoapService;
 import org.swift.common.soap.confluence.RemotePage;
 import org.swift.common.soap.confluence.RemoteServerInfo;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * User: nirb
  * Date: 5/21/13
  */
 public class EarlyAccessUpdate {
 
     public static void main(String[] args) throws Exception {
 
         String user = args[0];
         String password = args[1];
         String oldMilestone = args[2];
         String newMilestone = args[3];
         String oldBuildNumber = args[4];
         String newBuildNumber = args[5];
         String buildVersion = args[6];
 
         String cutBuildVersion = buildVersion.substring(0, buildVersion.length()-1);
         String pageTitle = "GigaSpaces " + cutBuildVersion + "X Early Access";
 
         WikiClient wikiClient = new WikiClient("http://wiki.gigaspaces.com/wiki", user, password);
 
         LogUtils.log("Connected ok.");
         ConfluenceSoapService service = wikiClient.getConfluenceSOAPService();
         String token = wikiClient.getToken();
 
         RemoteServerInfo info = service.getServerInfo(token);
         LogUtils.log("Confluence version: " + info.getMajorVersion() + "." + info.getMinorVersion());
         LogUtils.log("Completed.");
 
         RemotePage page = service.getPage(token, "RN", pageTitle);
         String pageContent = page.getContent();
 
         int startIndex = pageContent.indexOf("h2");
         String currentVersionTextBlock = pageContent.substring(startIndex, pageContent.indexOf("h2", startIndex + 1));
 
         Map<String, String> replaceTextMap = new HashMap<String, String>();
         replaceTextMap.put(oldMilestone, newMilestone);
         replaceTextMap.put(oldMilestone.toUpperCase(), newMilestone.toUpperCase());
         replaceTextMap.put(oldBuildNumber, newBuildNumber);
 
         for(Entry<String, String> entry : replaceTextMap.entrySet()){
             pageContent = pageContent.replace(entry.getKey(), entry.getValue());
         }
 
         String deckOpenning = "{deck:id=previous}";
        String cardOpenning = "{card:label=" + buildVersion + " " + oldMilestone.toUpperCase() + "}";
         String cardClose = "{card}";
 
         pageContent = pageContent.replace(deckOpenning, deckOpenning + "\n\n" + cardOpenning + "\n" + currentVersionTextBlock + "\n" + cardClose);
 
         page.setContent(pageContent);
         service.storePage(token, page);
     }
 
 }
