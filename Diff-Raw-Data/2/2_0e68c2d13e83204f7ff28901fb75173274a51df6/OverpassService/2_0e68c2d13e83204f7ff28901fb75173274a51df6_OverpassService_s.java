 /*
  * This software is provided "AS IS" without a warranty of any kind.
  * You use it on your own risk and responsibility!!!
  *
  * This file is shared under BSD v3 license.
  * See readme.txt and BSD3 file for details.
  *
  */
 
 package kendzi.josm.plugin.tomb.service;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.gui.preferences.server.ProxyPreferencesPanel;
 import org.openstreetmap.josm.gui.preferences.server.ProxyPreferencesPanel.ProxyPolicy;
 import org.xml.sax.SAXException;
 
 public class OverpassService {
     static String OVERPASS_URL = "http://www.overpass-api.de/api/interpreter";
 
     public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
         String q = "<osm-script>"
                 + " <query into=\"_\" type=\"relation\">"
                 + " <has-kv k=\"type\" v=\"person\" />"
                 + " <has-kv k=\"name\" regv=\"[Cc]zernik\" />"
                 + " </query>"
                 + " <print from=\"_\" limit=\"\" mode=\"meta\" order=\"id\"/>"
                 + " </osm-script>";
 
         String xml = (new OverpassService()).findQuery(q);
 
 
         System.out.println(xml);
 
     }
 
     public String findQuery(String query) {
         return findQuery(query, "UTF-8");
     }
 
     public String findQuery(String query, String encoding) {
 
         //http://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientExecuteProxy.java
         HttpHost httpProxy = getHttpProxy();
 
         HttpClient client = new DefaultHttpClient();
 
         StringBuffer sb = new StringBuffer();
         try {
 
             if (httpProxy != null) {
                 client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpProxy);
             }
             HttpPost post = new HttpPost(OVERPASS_URL);
 
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
             nameValuePairs.add(new BasicNameValuePair("data", query));
 
 
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             HttpResponse response = client.execute(post);
             BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), encoding));
             String line = "";
             while ((line = rd.readLine()) != null) {
                 System.out.println(line);
                 sb.append(line);
             }
             return sb.toString();
 
         } catch (IOException e) {
             throw new RuntimeException("error findQuery", e);
         }
     }
 
     private HttpHost getHttpProxy() {
 
 
         //        ProxySelector selector = ProxySelector.getDefault();
         //        if (selector instanceof DefaultProxySelector) {
         //            DefaultProxySelector p = ((DefaultProxySelector)selector);//.initFromPreferences();
         //            p.
         //        }
 
         ProxyPolicy proxyPolicy = ProxyPolicy.NO_PROXY;
 
         String value = Main.pref.get(ProxyPreferencesPanel.PROXY_POLICY);
 
         if (value.length() == 0) {
             proxyPolicy = ProxyPolicy.NO_PROXY;
         } else {
             proxyPolicy= ProxyPolicy.fromName(value);
             if (proxyPolicy == null) {
                 System.err.println(tr("Warning: unexpected value for preference ''{0}'' found. Got ''{1}''. Will use no proxy.", ProxyPreferencesPanel.PROXY_POLICY, value));
                 proxyPolicy = ProxyPolicy.NO_PROXY;
             }
         }
 
         if (!ProxyPolicy.USE_HTTP_PROXY.equals(proxyPolicy)) {
             return null;
         }
 
         String host = Main.pref.get(ProxyPreferencesPanel.PROXY_HTTP_HOST, null);
         int port = parseProxyPortValue(ProxyPreferencesPanel.PROXY_HTTP_PORT, Main.pref.get(ProxyPreferencesPanel.PROXY_HTTP_PORT, null));
         if (host != null && ! host.trim().equals("") && port > 0) {
 
             return new HttpHost(host, port, "http");
 
         } else {
             System.err.println(tr("Warning: Unexpected parameters for HTTP proxy. Got host ''{0}'' and port ''{1}''.", host, port));
             System.err.println(tr("The proxy will not be used."));
         }
         return null;
 
 
 
 
 
 
 
         //        CredentialsAgent cm = CredentialsManager.getInstance();
         //        try {
         //            PasswordAuthentication pa = new PasswordAuthentication(
         //                    tfProxyHttpUser.getText().trim(),
         //                    tfProxyHttpPassword.getPassword()
         //                    );
         //            cm.store(RequestorType.PROXY, tfProxyHttpHost.getText(), pa);
         //        } catch(CredentialsAgentException e) {
         //            e.printStackTrace();
         //        }
         //        return null;
     }
 
     protected int parseProxyPortValue(String property, String value) {
         if (value == null) {
             return 0;
         }
         int port = 0;
         try {
             port = Integer.parseInt(value);
         } catch (NumberFormatException e) {
             System.err.println(tr("Unexpected format for port number in in preference ''{0}''. Got ''{1}''.", property, value));
             System.err.println(tr("The proxy will not be used."));
             return 0;
         }
         if (port <= 0 || port >  65535) {
             System.err.println(tr("Illegal port number in preference ''{0}''. Got {1}.", property, port));
             System.err.println(tr("The proxy will not be used."));
             return 0;
         }
         return port;
     }
 }
