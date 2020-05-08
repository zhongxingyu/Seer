 package es.hackxcrack.andHxC;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import org.htmlcleaner.*;
 
 import android.util.Log;
 
 public class ForumManager {
 
     private final static String MAIN_FORUM = "http://www.hackxcrack.es/forum/?";
 
     /**
      * Esto busca en la página principal y saca los foros y las ID.
      *  Forma tres grupos, url, id y nombre.
      *
      */
     private final static Pattern FORUM_REGEX = Pattern.compile("<a class=\"subject\" href=\"(http://(www\\.)?hackxcrack.es/forum/index.php[?]board=\\d+).0\" name=\"b(\\d+)\">([^<]+)</a>");
 
     /**
      * Descripción: Busca las ID del tablón en una URL.
      *
      */
     private final static Pattern BOARD_ID_MATCHER = Pattern.compile(
         "(?:\\?|&)board=(\\d+)(\\.\\d+)");
 
 
     /**
      * Descripción: Busca las ID del tema en una URL.
      *
      */
     private final static Pattern TOPIC_ID_MATCHER = Pattern.compile(
         "(?:\\?|&)topic=(\\d+)(\\.\\d+)");
 
 
     /**
      * Descripción: Lee los datos en una url. Usa una cookie si se especifica.
      *
      * @param url String La dirección de donde leer los datos.
      * @param cookie String La cookie que usar al acceder a la dirección.
      *
      * @return String Los datos que devuelve la URL.
      * @throws IOException
      *
      */
     public static String fetchUrl(String url, String cookie) throws IOException{
         String result = null;
 
         HttpClient httpclient = new DefaultHttpClient();
         try {
             HttpGet request = new HttpGet(url);
             if (cookie != null){
                 request.addHeader("Cookie", cookie);
             }
 
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             result = httpclient.execute(request, responseHandler);
 
         } finally {
             // Cerramos la conexión para asegurarnos de no malgastar recursos
             httpclient.getConnectionManager().shutdown();
         }
 
         return result;
     }
 
 
     /**
      * Descripcion: Devuelve la lista de foros como una tupla de (nombre, id).
      *
      * @return List<Pair<String, String>>
      *
      * @note No implementado, por ahora se usará la lista estática.
      */
     public static List<PostInfo> getForumList(){
 
         try {
             System.out.println(fetchUrl(MAIN_FORUM, null));
         } catch (IOException ioException) {}
 
         return null;
     }
 
     /**
      * Descripción: Devuelve la lista de posts de una categoría como una tupla de (nombre, id).
      *
      * @return  List<PostInfo>
      */
     public static List<PostInfo> getItemsFromCategory(int categoryId, int page){
         String url = MAIN_FORUM + "board=" + categoryId + "." + page * 10;
 
         List <PostInfo> postList = new ArrayList<PostInfo>();
         String data;
         try {
             data = fetchUrl(url, null);
         } catch (IOException ioException) {
            //Log.e("andHxC getPostsFromCategory", ioException + "");
             return null;
         }
 
         HtmlCleaner cleaner = new HtmlCleaner();
         TagNode doc = cleaner.clean(data);
 
         // Búsqueda de subforos
         try{
             Object[] subforums = doc.evaluateXPath("//table[@class=\"table_list\"]/tbody/tr");
             for (int i = 0;i < subforums.length; i++){
                 TagNode subforum = (TagNode) subforums[i];
 
                 // Búsqueda del nombre/ID
                 Object[] subjects = subforum.evaluateXPath("//a[@class=\"subject\"]");
                 if (subjects.length != 1){
                     Log.e("andHxC", "Error parsing subforum link");
                     continue;
                 }
                 TagNode subject = (TagNode)subjects[0];
 
                 Matcher idMatch = BOARD_ID_MATCHER.matcher(subject.getAttributeByName("href"));
                 if (!idMatch.find()){
                     Log.e("andHxC", "Board ID not found on url “" + subject.getAttributeByName("href") + "”");
                     continue;
                 }
 
                 int id = Integer.parseInt(idMatch.group(1));
                 String name = subject.getText().toString();
 
                 // Búsqueda del número de respuestas
                 if (subforum.getChildTags().length != 4){
                     Log.e("andHxC", "Parse error looking for subforum response number, found " +
                           subforum.getChildTags().length + " child tags, expected 4");
                     continue;
                 }
 
                 TagNode responseNode = subforum.getChildTags()[2];
 
                 // Toma la fila de respuestas, haz trim() y toma de la segunda linea la primera columna
                 String sResponseNum = responseNode.getText().toString().trim().split("\n")[1].trim().split(" ")[0];
                 int responseNum = Integer.parseInt(sResponseNum);
 
                 postList.add(new PostInfo(name, responseNum, id, null, true));
             }
         }
         catch(XPatherException xpe){
             Log.e("andHxC", xpe.toString());
         }
 
 
         // Búsqueda de hilos
         Object[] threads = null;
         try{
             // Localización de la información
              threads = doc.evaluateXPath("//div[@id=\"messageindex\"]/table/tbody/tr");
         }
         catch(XPatherException xpe){
             Log.e("andHxC", xpe.toString());
         }
         if (threads != null){
             for (int i = 0;i < threads.length; i++){
                 TagNode thread = (TagNode) threads[i];
 
                 // El evaluador de XPath no soporta not :\
                 if (thread.hasAttribute("class")){
                     continue;
                 }
 
                 List<TagNode> titleList = thread.getElementListByAttValue("class", "subject_title", true, true);
 
                 // Extracción del título/ID
                 if((titleList.size() != 1) || (titleList.get(0).getChildTags().length != 1)){
                     Log.e("andHxC", "Parsing error looking for title");
                     continue;
                 }
 
 
                 TagNode title = titleList.get(0);
                 String name = title.getText().toString();
                 TagNode link = title.getChildTags()[0];
 
                 Matcher match = TOPIC_ID_MATCHER.matcher(link.getAttributeByName("href"));
                 if (!match.find()){
                     Log.e("andHxC", "Topic ID not found on url “" + link.getAttributeByName("href") + "”");
                     continue;
                 }
 
                 int id = Integer.parseInt(match.group(1));
 
                 // Extracción de la autoría
                 Object[] authorLink = null;
                 try {
                     authorLink = thread.evaluateXPath("//p/a");
                 }
                 catch(XPatherException xpe){
                     Log.e("andHxC", xpe.toString());
                     continue;
                 }
                 if (authorLink.length != 1){
                     Log.e("andHxC", "Parse error looking for topic author");
                     continue;
                 }
 
                 TagNode authorTag = (TagNode) authorLink[0];
                 String author = authorTag.getText().toString();
 
                 // Extracción del número de respuestas
                 if (thread.getChildTags().length != 4){
                     Log.e("andHxC", "Parse error looking for response number, found " +
                           thread.getChildTags().length + " child tags, expected 4");
                     continue;
                 }
 
                 TagNode responseNode = thread.getChildTags()[2];
                 int responseNum = Integer.parseInt(responseNode.getText().toString().trim().split(" ")[0]);
 
                 postList.add(new PostInfo(name, responseNum, id, author, false));
             }
         }
 
         return postList;
    }
 
 }
