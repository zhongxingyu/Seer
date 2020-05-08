 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package omnibot;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.CookiePolicy;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.JOptionPane;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  *
  * @author pavel
  */
 public class WikiApi extends DefaultHandler {
     CookieManager cm;
     URL apiURL;
     Main main;
     String value = "";
     Attributes attributes;
     String xml;
 
     String loginToken;
     String cookiePrefix;
     String sessionId;
 
     String loginName;
     String password;
     boolean login = false;
     int loginCounter = 0;
 
     boolean revision = false;
     WikiPage page;
 
     boolean token = false;
     String tokenValue;
 
     boolean edit = false;
 
 
     WikiApi(String url, Main m) {
         cm = new CookieManager();
         cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
         CookieHandler.setDefault(cm);
         main = m;
 
         try {
             apiURL = new URL(url);
         } catch (Exception e) {
             main.showError(e);
         }
     }
     void login(String ln, char[] pswd) {
         loginCounter++;
         if (loginCounter > 2) {
             JOptionPane.showMessageDialog(main, "Nebylo možné se přihlásit!", "Chyba!", JOptionPane.ERROR_MESSAGE);
             return;
         }
         loginName = ln;
         password = new String(pswd);
         login = true;
 
         Parameters p = new Parameters(main);
         p.add(new Parameter("format", "xml"));
         p.add(new Parameter("action", "login"));
         p.add(new Parameter("lgname", loginName));
         p.add(new Parameter("lgpassword", password));
         if (loginToken != null && !loginToken.isEmpty()) {
             p.add(new Parameter("lgtoken", loginToken));
         }
         try {
             xml = getRequest(p);
             parse(xml);
         } catch (Exception e) {
             main.showError(e);
         }
     }
     private void parseLogin(String name, String text, Attributes att) {
         if (name.equals("api")) {
             login = false;
         } else if (name.equals("login")) {
             if (att.getValue("result").equals("NeedToken")) {
                 loginToken = att.getValue("token");
                 cookiePrefix = att.getValue("cookieprefix");
                 sessionId = att.getValue("sessionId");
                 login(loginName, password.toCharArray());
             } else if (att.getValue("result").equals("Success")) {
                 JOptionPane.showMessageDialog(main, "Přihlášení proběhlo úspěšně.", "OK!", JOptionPane.INFORMATION_MESSAGE);
             } else if (att.getValue("result").equals("WrongPass")) {
                 JOptionPane.showMessageDialog(main, "Chybné přihlašovací údaje!", "Chyba!", JOptionPane.ERROR_MESSAGE);
             } else {
                 JOptionPane.showMessageDialog(main, att.getValue("result"), "Chyba!", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
     WikiPage revision(String pageTitle) {
         revision = true;
         page = new WikiPage();
         Parameters p = new Parameters(main);
         p.add(new Parameter("format", "xml"));
         p.add(new Parameter("action", "query"));
         p.add(new Parameter("prop", "revisions"));
         p.add(new Parameter("titles", pageTitle));
         p.add(new Parameter("rvprop", "ids|content|comment|user|userid|timestamp|flags"));
         p.add(new Parameter("rvlimit", "1"));
         try {
             xml = getRequest(p);
             System.out.print(xml);
             parse(xml);
         } catch (Exception e) {
             main.showError(e);
         }
         return page;
     }
     private void parseRevision(String name, String text, Attributes att) {
         if (name.equals("api")) {
             revision = false;
         } else if (name.equals("rev")) {
             page.setPageText(text);
             page.setRevisionComment(att.getValue("comment"));
             page.setRevisionContributor(att.getValue("user"));
             page.setRevisionContributorId(att.getValue("userid"));
             page.setRevisionTimestamp(att.getValue("timestamp"));
             page.setRevisionId(att.getValue("revid"));
             if (doAttributesContains(att, "minor")) {
                 page.setMinor(true);
             }
         }
     }
     private String editToken(String pageTitle) {
         token = true;
         Parameters p = new Parameters(main);
         p.add(new Parameter("format", "xml"));
         p.add(new Parameter("action", "query"));
         p.add(new Parameter("prop", "info"));
         p.add(new Parameter("titles", pageTitle));
         p.add(new Parameter("intoken", "edit"));
         //p.add(new Parameter("action", "tokens")); //newer MediaWiki
         //p.add(new Parameter("type", "edit")); //newer MediaWiki
         try {
             xml = getRequest(p);
             System.out.println(xml);
             parse(xml);
         } catch (Exception e) {
             main.showError(e);
         }
         return tokenValue;
     }
     private void parseToken(String name, String text, Attributes att) {
         if (name.equals("api")) {
             token = false;
         } else if (name.equals("tokens")) {
             tokenValue = att.getValue("edittoken");
         } else if (name.equals("page")) {
             tokenValue = att.getValue("edittoken");
         }
     }
     public void editPage(WikiPage page, String newContent, String comment, boolean minor, boolean bot) {
         String editToken = editToken(page.getTitle());
         edit = true;
         Parameters p = new Parameters(main);
         p.add(new Parameter("format", "xml"));
         p.add(new Parameter("action", "edit"));
         p.add(new Parameter("title", page.getTitle()));
         p.add(new Parameter("token", editToken));
         p.add(new Parameter("basetimestamp", page.getRevisionTimestamp()));
         p.add(new Parameter("text", newContent));
         p.add(new Parameter("summary", comment));
         if (minor) {
             p.add(new Parameter("minor", "1"));
         }
         if (bot) {
             p.add(new Parameter("bot", "1"));
         }
         //p.add(new Parameter("nocreate", "1")); //TODO
         try {
             xml = getRequest(p);
             System.out.print(xml);
             parse(xml);
         } catch (Exception e) {
             main.showError(e);
         }
     }
     private void parseEdit(String name, String text, Attributes att) {
         if (name.equals("api")) {
             edit = false;
         } else if (name.equals("edit")) {
             String result = att.getValue("result");
             if (result.equals("Success")) {
                JOptionPane.showMessageDialog(main, "Stránka " + att.getValue("title") + " byla editována!" , "OK!", JOptionPane.INFORMATION_MESSAGE);
             } else if (result.equals("Failure")) {
                 JOptionPane.showMessageDialog(main, xml, "Nebyla provedena editace!", JOptionPane.ERROR_MESSAGE);
             } else {
                 JOptionPane.showMessageDialog(main, xml, "Pozor!", JOptionPane.WARNING_MESSAGE);
             }
         }
     }
     private String getRequest(Parameters p) throws IOException {
         String parameters = p.exportToHttpRequest();
         HttpURLConnection con = (HttpURLConnection) apiURL.openConnection();
         con.setDoOutput(true);
         con.setDoInput(true);
         con.setInstanceFollowRedirects(false);
         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         con.setRequestProperty("charset", "utf-8");
         con.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
         con.setUseCaches(true);
 
         DataOutputStream writer = new DataOutputStream((con.getOutputStream()));
         writer.writeBytes(parameters);
         writer.flush();
         String line;
         String content = "";
         BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
         while ((line = reader.readLine()) != null) {
             content += line + "\n";
         }
         writer.close();
         reader.close();
         con.disconnect(); //TODO
 
         return content;
 
     }
     boolean doAttributesContains(Attributes att, String name) {
         int length = att.getLength();
         String attName;
         for (int i = 0; i < length; i++) {
             attName = att.getLocalName(i);
             if (attName.equals(name)) {
                 return true;
             }
         }
         return false;
     }
     @Override
     public void startElement(String uri, String name, String qName, Attributes att) {
         attributes = att;
         if (revision && name.equals("page")) {
             page.setTitle(att.getValue("title"));
             page.setPageId(att.getValue("pageid"));
         }
     }
     @Override
     public void endElement(String uri, String name, String qName) {
         if (login) {
             parseLogin(name, value, attributes);
         }
         if (revision) {
             parseRevision(name, value, attributes);
         }
         if (token) {
             parseToken(name, value, attributes);
         }
         if (edit) {
             parseEdit(name, value, attributes);
         }
         value = "";
         attributes = null;
     }
     @Override
     public void characters(char[] ch, int start, int end) {
         value = value + new String(ch, start, end);
     }
 
     public void parse(InputStream inputStream) throws org.xml.sax.SAXException, java.io.IOException {
         XMLReader xr = XMLReaderFactory.createXMLReader();
         xr.setContentHandler(this);
         xr.setErrorHandler(this);
         xr.parse(new InputSource(inputStream));
     }
 
     public void parse(String xml) throws org.xml.sax.SAXException, java.io.IOException {
         parse(new ByteArrayInputStream(xml.getBytes()));
     }
 
 }
 
 class Parameters {
     List<Parameter> parameterList;
     Main main;
 
     Parameters(Main m) {
         parameterList = new ArrayList<Parameter>();
         main = m;
     }
     boolean add(Parameter p) {
         return parameterList.add(p);
     }
     boolean addAll(Collection<Parameter> c) {
         return parameterList.addAll(c);
     }
 
     String exportToHttpRequest() {
         String httpRequestValues = "";
         Parameter p;
 
         for (Iterator<Parameter> it = parameterList.iterator(); it.hasNext(); ) {
             p = it.next();
             try {
                 if (!httpRequestValues.isEmpty()) {
                     httpRequestValues += '&';
                 }
                 httpRequestValues += URLEncoder.encode(p.name, "UTF-8") + '=' + URLEncoder.encode(p.value, "UTF-8");
             } catch (Exception e) {
                 main.showError(e);
             }
         }
         return httpRequestValues;
     }
 }
 
 class Parameter {
     String name;
     String value;
     Parameter(String parameterName, String parameterValue) {
         name = parameterName;
         value = parameterValue;
     }
 }
