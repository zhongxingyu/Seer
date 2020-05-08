 package fi.iki.dezgeg.matkakorttiwidget.matkakortti;
 
 import com.gistlabs.mechanize.MechanizeAgent;
 import com.gistlabs.mechanize.document.Document;
 import com.gistlabs.mechanize.document.html.HtmlDocument;
 import com.gistlabs.mechanize.document.html.HtmlElement;
import com.gistlabs.mechanize.document.html.HtmlNode;
 import com.gistlabs.mechanize.document.html.form.Form;
 import com.gistlabs.mechanize.document.html.form.SubmitButton;
 
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder.byId;
 import static com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder.byTag;
 
 public class MatkakorttiApi
 {
     private String username;
     private String password;
     public static final Pattern CARDS_JSON_PATTERN = Pattern.compile(".*?parseJSON\\('(.*)'\\).*", Pattern.DOTALL);
     public static final Pattern DATE_PATTERN = Pattern.compile("^/Date\\(([0-9]+)\\)/$");
     public static final Pattern SESSION_EXISTS_PATTERN = Pattern.compile(".*on toinen avoin istunto.*");
 
     public MatkakorttiApi(String username, String password) {
         this.username = username;
         this.password = password;
     }
 
     public List<Card> getCards() throws Exception
     {
         MechanizeAgent agent;
         try {
             agent = login();
         } catch (MatkakorttiException me) {
             if (SESSION_EXISTS_PATTERN.matcher(me.getMessage()).matches()) // Try again once
                 agent = login();
             else
                 throw me;
         }
 
         // TODO: Follow redirects
         HtmlDocument response = agent.get("https://omamatkakortti.hsl.fi/Basic/Cards.aspx");
         List<HtmlElement> scripts = response.htmlElements().getAll(byTag("script"));
 
         List<Card> cards = new ArrayList<Card>();
         for (HtmlElement script : scripts) {
             Matcher matcher = CARDS_JSON_PATTERN.matcher(script.getInnerHtml());
             if (matcher.matches()) {
                 JSONArray cardsJson = new JSONArray(matcher.group(1));
                 for (int i = 0; i < cardsJson.length(); i++) {
                     cards.add(createCardFromJSON(cardsJson.getJSONObject(i)));
                 }
                 return cards;
             }
         }
         throw new MatkakorttiException("No voi vittu");
     }
 
     private MechanizeAgent login() throws Exception {
         AbstractHttpClient httpClient = NonverifyingSSLSocketFactory.createNonverifyingHttpClient();
 
         MechanizeAgent agent = new MechanizeAgent(httpClient);
         Document page = agent.get("https://omamatkakortti.hsl.fi/Login.aspx");
 
         Form loginForm = page.forms().get(byId("aspnetForm"));
         String prefix = "Etuile$MainContent$LoginControl$LoginForm$";
         loginForm.get(prefix + "UserName").set(username);
         loginForm.get(prefix + "Password").set(password);
 
         HtmlDocument loginResponse = loginForm.submit((SubmitButton) loginForm.get(prefix + "LoginButton"));
         HtmlElement validationSummary = loginResponse.htmlElements().get(byId("Etuile_mainValidationSummary"));
 
         if (validationSummary != null) {
            List<HtmlNode> errorElements = validationSummary.get(byTag("ul")).getChildren();
             String errors = "";
             // - 1 since the service will always complain about our browser.
            for (int i = 0; i < errorElements.size() - 1; i++) {
                HtmlNode elem = errorElements.get(i);
                if (elem instanceof HtmlElement)
                    errors += ((HtmlElement)elem).getText() + "\n";
            }
             throw new MatkakorttiException(errors);
         }
         return agent;
     }
 
     private Card createCardFromJSON(JSONObject card) throws JSONException {
         double moneyAsDouble = card.getDouble("RemainingMoney");
         // Round to BigDecimal cents safely
         BigDecimal money = new BigDecimal((int)Math.round(100 * moneyAsDouble)).divide(new BigDecimal(100));
 
         String expiryDateStr = card.getJSONObject("PeriodProductState").getString("ExpiringDate");
         Date expiryDate = null;
         if (!expiryDateStr.equals("null")) {
             Matcher expiryDateMatch = DATE_PATTERN.matcher(expiryDateStr);
             expiryDateMatch.matches(); // Must do this.
             expiryDate = new Date(Long.parseLong(expiryDateMatch.group(1)));
         }
 
         return new Card(card.getString("name"), card.getString("id"), money, expiryDate);
     }
 }
