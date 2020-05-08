 package org.mvnsearch.sprockets;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 /**
  * sprockets servlet
  *
  * @author linux_china
  */
 public class SprocketsServlet extends HttpServlet {
     /**
      * current environment
      */
     private String env;
     /**
      * js repository url
      */
     private String repository;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         env = config.getInitParameter("env");
         repository = config.getInitParameter("repository");
     }
 
     /**
      * sprockets request handler
      *
      * @param request  http servlet request
      * @param response http servlet response
      * @throws ServletException servlet exception
      * @throws IOException      io exception
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         response.setContentType("text/javascript");
         PrintWriter out = response.getWriter();
         String jsUri = request.getRequestURI();
         String queryString = request.getQueryString();
         //output js with sprockets
         if (queryString != null && queryString.contains("sprockets")) {
             JsNode jsNode = null;
             //dev env, output js with loader
             if ("dev".equals(env)) {
                 jsNode = parseNode(jsUri, request.getQueryString());
                 List<JsNode> linkedParents = jsNode.getLinkedParents();
                 for (JsNode linkedParent : linkedParents) {
                     out.println("document.write('<script type=\"text/javascript\" src=\"" + linkedParent.getUri() + "\"></script>');");
                 }
                out.println("document.write('<script type=\"text/javascript\" src=\"" + jsUri + "\"></script>');");
             } else { //concat all js and output
                 jsNode = JsDependencyTree.getInstance().findNode(jsUri);
                 if (jsNode == null) {
                     jsNode = parseNode(jsUri, request.getQueryString());
                     if (jsNode != null) {
                         JsDependencyTree.getInstance().addNode(jsNode);
                     }
                 }
                 if (jsNode != null) {
                     for (JsNode node : jsNode.getLinkedParents()) {
                         out.println(node.getContent());
                     }
                 }
             }
         } else { // plain js
             out.print(IOUtils.toString(getServletContext().getResourceAsStream(jsUri)));
         }
         out.flush();
         out.close();
 
     }
 
     /**
      * parse js node
      *
      * @param jsUri       js uri
      * @param queryString query string
      * @return js node
      */
     public JsNode parseNode(String jsUri, String queryString) {
         try {
             JsNode jsNode = new JsNode();
             jsNode.setUri(jsUri);
             jsNode.setQueryString(queryString);
             jsNode.setContent(IOUtils.toString(getServletContext().getResourceAsStream(jsUri)));
             resolveParent(jsNode);
             return jsNode;
         } catch (Exception e) {
             getServletContext().log("Failed to resolve js node: " + jsUri, e);
         }
         return null;
     }
 
     /**
      * resolve parent jsNode
      *
      * @param jsNode js node
      */
    @SuppressWarnings({"ConstantConditions"})
     private void resolveParent(JsNode jsNode) {
         try {
             List<String> requireSentences = jsNode.getRequireSentences();
             if (!requireSentences.isEmpty()) {
                 for (String requireSentence : requireSentences) {
                     String path = requireSentence.substring(requireSentence.indexOf("require") + 7).trim();
                     String jsUri = null;
                     //رļ
                     if (path.startsWith("\"") || path.startsWith("'")) {
                         jsUri = FilenameUtils.getPath(jsNode.getUri()) + trimQuote(path) + ".js";
                     } else if (path.startsWith("<")) { //ؿļ
                         jsUri = repository + "/" + trimQuote(path) + ".js";
                         //ո񣬱ʾָ汾
                         if (path.contains(" ")) {
                             String[] parts = trimQuote(path).split("\\s");
                             jsUri = repository + "/" + parts[0] + "/" + parts[1] + "/" + parts[0] + "-" + parts[1] + ".js";
                         }
                     }
                     if (jsUri != null) {
                         JsNode parent = new JsNode();
                         parent.setUri(jsUri);
                         if (parent.isRepositoryJs()) {
                             //repository js, use cache
                             if (JsDependencyTree.getInstance().findNode(jsUri) != null) {
                                 parent = JsDependencyTree.getInstance().findNode(jsUri);
                             } else {
                                 parent.setContent(getUriText(parent.getUri()));
                             }
                         } else {
                             parent.setContent(IOUtils.toString(getServletContext().getResourceAsStream(parent.getUri())));
                         }
                         jsNode.addParent(parent);
                         JsDependencyTree.getInstance().addNode(parent);
                         resolveParent(parent);
                     }
                 }
             }
         } catch (Exception e) {
             getServletContext().log("Failed to resolve parent Js:  " + jsNode.getUri(), e);
         }
     }
 
     /**
      * trim quote, include " ' < >
      *
      * @param text tet
      * @return new text
      */
     private String trimQuote(String text) {
         StringBuilder builder = new StringBuilder();
         for (char c : text.toCharArray()) {
             if (c != '"' && c != '\'' && c != '<' && c != '>') {
                 builder.append(c);
             }
         }
         return builder.toString().trim();
     }
 
     /**
      * get uri text content
      *
      * @param uri uri
      * @return text content
      */
     private String getUriText(String uri) {
         try {
             GetMethod getMethod = new GetMethod(uri);
             createHttpClient().executeMethod(getMethod);
             return IOUtils.toString(getMethod.getResponseBodyAsStream());
         } catch (Exception e) {
             getServletContext().log("Failed to download text content:" + uri, e);
         }
         return null;
     }
 
     /**
      * create http client instance
      *
      * @return HttpClient object
      */
     private HttpClient createHttpClient() {
         HttpClient clientTemp = new HttpClient();     //HttpClient create
         HttpClientParams clientParams = clientTemp.getParams();
         clientParams.setParameter("http.socket.timeout", 10000); //10 seconds for socket waiting
         clientParams.setParameter("http.connection.timeout", 10000); //10 seconds http connection creation
         clientParams.setParameter("http.connection-manager.timeout", 3000L); //3 seconds waiting to get connection from http connection manager
         clientParams.setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler()); // if failed, try 3
         return clientTemp;
     }
 }
