 package eionet.util;
 
 import java.util.StringTokenizer;
 /**
  * A Class class.
  * <P>
 * @author Enriko Ksper
  */
 public class QueryString{
 
   private String queryString=null;
 
   public QueryString(String queryString) {
       this.queryString = queryString;
   }
   public String getValue(){
       return queryString;
   }
   public String changeParam(String param, String value){
       if (hasParam(param))
           change(param, value);
       else
           add(param, value);
 
       return queryString;
   }
   public String addParam(String param, String value){
       return changeParam(param, value);
   }
   public String removeParam(String param){
       remove(param);
       return queryString;
   }
   public boolean equals(QueryString s){
       return equals(s.getValue());
   }
   private boolean hasParam(String param){
 
       if (queryString.indexOf(param + "=")>0)
           return true;
       return false;
   }
 
   private void add(String param, String value){
 
       String s =queryString.indexOf("?")>0 ? "&" : "?";
 
 			queryString += s + param + "=" + value;
   }
 
   private void remove(String param){
 
 		int i=queryString.indexOf(param);
     if (i<1) return;
 
 		int and=queryString.indexOf("&", i);
 
  		if (and>0)
         queryString = queryString.substring(0,i-1) + queryString.substring(and);
     else
        	queryString = queryString.substring(0,i-1);
   }
 
   private void change(String param, String value){
 
 		int i=queryString.indexOf(param);
     if (i<1) return;
 		String begin=queryString.substring(0, i);
 		String str=queryString.substring(i);
 		int j = str.indexOf("&");
     String end = j>0 ? str.substring(j) : "";
 
 		queryString=begin + param + "=" + value + end ;
   }
   public boolean equals(String s){
 
     if (queryString.equals(s)) return true;
 
     int sep = queryString.indexOf("?");
     int sep2 = s.indexOf("?");
     if (sep>0){
         if (sep!=sep2) return false;
         if (!queryString.substring(0,sep).equalsIgnoreCase(s.substring(0,sep))) return false;
 
         String query = queryString.substring(sep+1);
         StringTokenizer tokens = new StringTokenizer(query, "&");
         String query2 = s.substring(sep+1);
         StringTokenizer tokens2 = new StringTokenizer(query2, "&");
 
 
         if (tokens.countTokens()!=tokens2.countTokens()) return false;
         boolean ok = false;
         while (tokens.hasMoreTokens()) {
             String t=tokens.nextToken();
             while (tokens2.hasMoreTokens())
               if (t.equals(tokens2.nextToken())) ok = true;
             if (ok==false) return false;
             ok=false;
             tokens2 = new StringTokenizer(query2, "&");
         }
 
     }
     else{
         return queryString.equalsIgnoreCase(s);
     }
     return true;
   }
   public static void main(String[] args){
 
         //System.out.println(s);
         //System.out.println(qs.changeParam("ds_id","222"));
         //System.out.println(qs.addParam("aaa","j"));
         //System.out.println(qs.removeParam("ctx"));
         //System.out.println(qs.removeParam("mode"));
   }
 }
 
