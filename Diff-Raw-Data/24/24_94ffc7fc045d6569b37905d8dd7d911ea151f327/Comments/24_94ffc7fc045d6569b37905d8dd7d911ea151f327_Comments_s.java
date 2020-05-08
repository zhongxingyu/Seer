 package diploma;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.*;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 class Comment{
 	String expand_url;
 	String ctime;
 	String article;
 	String username;
 	long thread;
 	long parent;
 	 int level;
 	boolean collapsed;
 	
 	public Comment(String username, long thread, long parent, String ctime, String article, int level){
 		this.username = username;
 		this.expand_url = "";
 		this.thread = thread;
 		this.parent = parent;
 		this.ctime = ctime;
 		this.article = article;
 		this.level = level;
 		this.collapsed = false;
 	}
 	
 	public Comment(String threadURL){
 		this.expand_url = threadURL;
 		this.collapsed = true;
 	}
 	
 	public String toString(){
 		String blink = "";
 		if (collapsed) return expand_url;
 		for (int i=0; i*2<level; i++)
 			blink += " ";
 		return blink+article;
 	}
 	
 	public boolean equals(Object Obj){
 		return collapsed;		
 	}
 }
 
 
 public class Comments {
 	
     List<Comment> commentList;
     List<String> keys;
     List<String> linkList;
     int number = 0;
     
     public Comments() throws Exception{
                keys = new ArrayList<String>(Arrays.asList("journal_url,thread,parent,ctime,article,level,leafclass".split(",")));
         commentList = new ArrayList<Comment>();
            linkList = new ArrayList<String>();
     }
    
 //    /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ NEW BLOCK
     void getFirstLevelList(JSONArray initial) throws Exception{
 //    	Принимает набор первоначальных комментариев, открывает все 1е уровни, грузит из них комменты
     	String href = "";
     	List<String> localKeys = null;
     	System.out.println(initial.length());
     	for (int i = 0; i < initial.length(); i++) {
         		JSONObject currentComment = initial.getJSONObject(i);
         		localKeys = new ArrayList<String>(Arrays.asList(JSONObject.getNames(currentComment)));
         		if (!localKeys.contains("actions"))
         			continue;
         		if (currentComment.getInt("level")!=1)
         			continue;
         		href = simpleCheck(currentComment.get("actions"));
         		if (!href.equals("")){
         			getComments(retrieveJson(href));
                 	  Json2File(retrieveJson(href));
         		}
 		}
     }
     String simpleCheck(Object toCheck){
     	if (toCheck.getClass().equals(JSONArray.class)){
     		JSONArray ttt = (JSONArray) toCheck;
     		for (int i=0; i<ttt.length(); i++){
     			JSONObject tmp = ttt.getJSONObject(i);
     			if (JSONObject.valueToString(tmp.get("name")).equals("\"expand\""))
     				return tmp.getString("href");
     		}
     	}
     	return "";
     }
     
     void getComments(JSONArray initial){
     	String journal_url;
     	long thread;
     	long parent;
     	String ctime;
     	String article;
     	int level;
     	for (int i = 0; i < initial.length(); i++) {
 			JSONObject currentComment = initial.getJSONObject(i);
 			if (JSONObject.valueToString(currentComment.get("username")).equals("null"))
 				journal_url = "Anonimous";
 			else {
     			thread 	=   Long.parseLong(JSONObject.valueToString(currentComment.get("thread")));
     			parent 	=   Long.parseLong(JSONObject.valueToString(currentComment.get("parent")));
     			ctime 	=                  JSONObject.valueToString(currentComment.get("ctime"));
     			article = 				   JSONObject.valueToString(currentComment.get("article"));
     			level   = Integer.parseInt(JSONObject.valueToString(currentComment.get("level")));
     			JSONObject username = currentComment.getJSONArray("username").getJSONObject(0);
     			journal_url = JSONObject.valueToString(username.get("journal_url"));
     			commentList.add(new Comment(journal_url, thread, parent, ctime, article, level));
 			}
 		}
     }
 //  \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/ TRash BLOCK
     
     JSONArray retrieveJson(String url) throws Exception{
 //    	Возвращает массив с комментариями из страницы по адресу url
         HttpClient   httpclnt = new DefaultHttpClient();
         HttpGet       httpget = new HttpGet(url);      
         HttpResponse httpresp = httpclnt.execute(httpget);
         HttpEntity     entity = httpresp.getEntity();
         String 		html_text = new String();
         if (entity != null)
             html_text = EntityUtils.toString(entity, "UTF-8");
         httpget.abort();
         
         Document  		  doc = Jsoup.parse(html_text,"UTF-8");       
         Elements 	 comments = doc.getElementsByAttributeValue("id", "comments_json");
         return new JSONArray(comments.html());
     }
 
     
     void makeList(JSONArray input){
     	String expand_url = "";
     	String ctime;
     	String article;
     	String username;
     	long thread;
     	long parent;
     	 int level;
     	
     	List<Comment> tmpFull = new ArrayList<Comment>();
     	List<Comment> tmpLink = new ArrayList<Comment>();
     	for (int i=0; i<input.length(); i++){
     		JSONObject first_level = input.getJSONObject(i);
     		String[] aNames = JSONObject.getNames(first_level);
    		if (Arrays.binarySearch(aNames, "moreusers") < -1) continue;
     		if (tmpFull.isEmpty() & Integer.parseInt(JSONObject.valueToString(first_level.get("collapsed"))) == 1) continue;
 //    		Проверили необходимость сохранения текущего комментария
 			ctime 	=                  JSONObject.valueToString(first_level.get("ctime"));
 			article = 				   JSONObject.valueToString(first_level.get("article"));
 			thread 	=   Long.parseLong(JSONObject.valueToString(first_level.get("thread")));
 			parent 	=   Long.parseLong(JSONObject.valueToString(first_level.get("parent")));
 			level   = Integer.parseInt(JSONObject.valueToString(first_level.get("level")));
 //    		Сохранили все данные из первого уровня
 			JSONObject usernameJson = first_level.getJSONArray("username").getJSONObject(0);
 			username = JSONObject.valueToString(usernameJson.get("journal_url"));
 //			Сохранили название журнала из второго
 			JSONArray actionsArray = first_level.getJSONArray("actions");
     		for (int j=0; j<actionsArray.length(); j++){
     			JSONObject tmp = actionsArray.getJSONObject(j);
     			if (JSONObject.valueToString(tmp.get("name")).equals("\"expand\""))
     				expand_url = tmp.getString("href");
     		}
 //			Извлекли ссылку из внутреннего массива
     		tmpFull.add(new Comment(username, thread, parent, ctime, article, level));
     		tmpLink.add(new Comment(expand_url));
     		System.out.println(tmpFull.size()-1 + " -- tmpFull "+tmpFull.get(tmpFull.size()-1));
     		System.out.print("tmpLink "+tmpLink.get(tmpLink.size()-1));
     		System.out.println(", collapsed: "+JSONObject.valueToString(first_level.get("collapsed")));
     		if (Integer.parseInt(JSONObject.valueToString(first_level.get("collapsed"))) == 1){
 //    			Текущая ветка скрыта, поднимаемся на 2 уровня выше
     			for (int j = 0; j < tmpFull.size()-3; j++)
     				commentList.add(tmpFull.get(j));
     			commentList.add(tmpLink.get(tmpLink.size()-3));
     			tmpFull.clear();
     			tmpLink.clear();
     		} else
     		if (i==input.length()-1)
     			commentList.addAll(tmpFull);
 //    			дошли до конца и не свернуто
     	}
     }   
     
     void Json2File(JSONArray initial) throws Exception{
     	List<String> localKeys = new ArrayList<String>(Arrays.asList("username,article,level,collapsed".split(",")));
 //        File out_file = new File("D:\\aovodov\\tmp\\20130509\\1398900"+number++ +".comments");
    	File out_file = new File("D:\\aovodov\\tmp\\20130509\\1398900_primer.comments");
         PrintWriter out = new PrintWriter(out_file);
 
     	for (int i=0; i<initial.length(); i++){
     		out.println("-----------------------------------------------New Comment #"+(i+1));
     		JSONObject first_level = initial.getJSONObject(i);
     		for(String aName:JSONObject.getNames(first_level)){
     			if (aName.equals("leafclass")) continue;
 //    			if (!localKeys.contains(aName)) continue;
     			Object obj = first_level.get(aName);
     			if (obj.getClass() == JSONArray.class){
     				out.println("\n"+aName+":");
 					JSONArray tmp = first_level.getJSONArray(aName);
 					for (int j = 0; j < tmp.length(); j++) {
 						JSONObject second_level = tmp.getJSONObject(j);
 						for(String bName:JSONObject.getNames(second_level)){
 							out.println("  "+bName+": "+JSONObject.valueToString(second_level.get(bName)));
 						}
 					}
     			} else {
 					out.println(aName+": "+JSONObject.valueToString(first_level.get(aName)));
     			}
     		}
     	}
     	out.close();
     }
     
     public static void main(String[] args) throws Exception {
         Comments t = new Comments();
        t.makeList(t.retrieveJson("http://tema.livejournal.com/1399594.html?&format=light"));
 //        for (int i=1; i<4; i++){
 //        	t.getFirstLevelList(t.retrieveJson("http://tema.livejournal.com/1398900.html?page="+i+"&format=light"));
 //        	System.out.println("http://tema.livejournal.com/1398900.html?page="+i+"&format=light");
 //        	System.out.println(new Date());	
 //        }
    	File out_file = new File("D:\\aovodov\\tmp\\20130509\\tema1399594.comments");
         PrintWriter out = new PrintWriter(out_file);        
     	for (Comment aComment:t.commentList)
     		out.println(aComment);
     	out.close();
     	System.out.println(t.commentList.size());
     	System.out.print(new Date());
     }
 }
