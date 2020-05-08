 package Global.WebPages;
 
 import Global.WebPage;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dunaev
  * Date: 11/28/13
  * Time: 8:10 PM
  * To change this template use File | Settings | File Templates.
  *
  * Testovaja stranicca karochi
  */
 
 public class FromTheVeryBottomOfMyHearth extends WebPageImp implements WebPage {
 
     // Sostojanija dl'a CaseHandler
     private static final int ENTRY = 0;
 
     // Im'a templaita, s kotorim budet rabotat'
     private static final String TEMPLATE = "test.tml";
 
     // Dopolnitel'nie peremennie
         // Context
     private Map<String, Object> pageVariables;
 
     public FromTheVeryBottomOfMyHearth() {
         super();
         this.pageVariables = new HashMap<>();
     }
 
     @Override
     protected int analyzeRequestGET(HttpServletRequest request) {
         // Poka dl'a demonstracii ostavil odin, kotorij i pojdet v CaseHandler, kotorij opisan nije
         return ENTRY;
     }
 
     @Override
     protected int analyzeRequestPOST(HttpServletRequest request) {
         // Lol, poka bi s GET razobrats'a :D
         return ENTRY;
     }
 
     // Lol, da eto je metki ebanie. Nu ok, eto obrabotchik lubogo (ANY) zaprosa s kodom ENTRY
     @CaseHandler(routine = ENTRY, reqType = RequestType.ANY)
     public String handleEntry() {
         // Nu poka bez proverok s if i prochim, glavnoe otrisovshik
             // Zapolnenie dannimi (pervie 2 sv'azani so strukturoj shablona)
        this.pageVariables = dataToKey(new String[] {"PageTitle", "Location"},
                 "So much bottom of my hearth","So much test page");
        return generatePage(TEMPLATE,pageVariables);
     }
 
 }
