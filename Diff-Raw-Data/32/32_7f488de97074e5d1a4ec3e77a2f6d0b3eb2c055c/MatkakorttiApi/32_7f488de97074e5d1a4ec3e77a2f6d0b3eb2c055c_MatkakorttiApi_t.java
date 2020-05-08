 package fi.iki.dezgeg.matkakorttiwidget.matkakortti;
 
 import com.gistlabs.mechanize.MechanizeAgent;
 import com.gistlabs.mechanize.document.Document;
 import com.gistlabs.mechanize.document.html.HtmlDocument;
 import com.gistlabs.mechanize.document.html.HtmlElement;
 import com.gistlabs.mechanize.document.html.form.Form;
 import com.gistlabs.mechanize.document.html.form.SubmitButton;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.math.BigDecimal;
 import java.security.KeyStore;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import fi.iki.dezgeg.matkakorttiwidget.MatkakorttiException;
 
 import static com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder.byId;
 import static com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder.byTag;
 
 public class MatkakorttiApi
 {
     private String username;
     private String password;
 
    public MatkakorttiApi(String username, String password) {
         this.username = username;
        this.password = password;
     }
 
     public Card getCard() throws Exception
     {
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
             List<HtmlElement> errorElements = validationSummary.get(byTag("ul")).getAll(byTag("li"));
             String errors = "";
             // - 1 since the service will always complain about our browser.
             for (int i = 0; i < errorElements.size() - 1; i++)
                 errors += errorElements.get(i).getText() + "\n";
             throw new MatkakorttiException(errors);
         }
 
         // TODO: Follow redirects
         HtmlDocument response = agent.get("https://omamatkakortti.hsl.fi/Basic/Cards.aspx");
         List<HtmlElement> scripts = response.htmlElements().getAll(byTag("script"));
 
         Pattern jsonPattern = Pattern.compile(".*?parseJSON\\('(.*)'\\).*", Pattern.DOTALL);
 
         for (HtmlElement script : scripts) {
             Matcher matcher = jsonPattern.matcher(script.getInnerHtml());
             if (matcher.matches()) {
                return createCardFromJSON(matcher.group(1));
             }
         }
         throw new MatkakorttiException("No voi vittu");
     }

    private Card createCardFromJSON(String json) throws JSONException {
        JSONArray cards = new JSONArray(json);
        if (cards.length() == 0)
            throw new MatkakorttiException("Tunnuksella ei ole matkakortteja.");

        JSONObject card = cards.getJSONObject(0);
        double moneyAsDouble = card.getDouble("RemainingMoney");

        // Round to BigDecimal cents safely
        BigDecimal money = new BigDecimal((int)Math.round(100 * moneyAsDouble)).divide(new BigDecimal(100));
        return new Card(card.getString("name"), card.getString("id"), money);
    }
 }
