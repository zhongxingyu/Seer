 package models;
 
 import javax.persistence.*;
 
 import play.db.jpa.*;
 import play.data.validation.*;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.regex.*;
 
 import java.util.Date;
 
 import java.text.SimpleDateFormat;
 
 
 
 @Entity
 public class Document extends Model {
 
     @Transient
     public boolean copy = false;
 
 	@Required
     public String partNumber;
 
 	@Required
     public int page = 1;
 
 	@Required
 	@Min(3)
     public int width = 3; // in inches?
 	
 	@Required
 	@Min(2)
     public int height = 2; // in inches?
 
     @Column(length = 4000)
     public String html;
 
     public boolean landscape = false;
     public boolean accepted = true;
     public String updateDesc = "";
     public Date updated = new Date();
     public boolean printLabel = false;
     public boolean printStencil = true;
 
     public String orderNumber;
 
 	
 	private static Pattern ORDER_PATTERN = Pattern.compile("\\{work\\s?order\\}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 	private static Pattern DATE_PATTERN = Pattern.compile("\\{date\\}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 	private static Pattern TOKEN_PATTERN = Pattern.compile("\\{[A-Za-z0-9]*.\\s?[A-Za-z0-9]*.\\}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 
 	// find all groups of spaces where there are at least to consequetive spaces.
 	private static Pattern SPACE_PATTERN = Pattern.compile( "&nbsp;"  );
 
 	private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yy");
 
 	protected boolean emptyHtml(){
 		return (html==null || html.length()==0 );
 	}
 	
 	public void convertExtraSpaces(){
		html = html.replace( "&nbsp;","&#160;" );		
		//html = html.replace( " ","&#x20;" );
		//html = html.replace("\t","&#160;&#160;&#160;&#160;");
		//System.out.println( html );
 	}
 
 	public void convertDates(){
 		if( emptyHtml() ){return;}
 		Matcher matcher = DATE_PATTERN.matcher( html );
 		String today = DATE_FORMATTER.format( new Date() );
 		if( matcher.find()  ){
 			//String token = matcher.group().toLowerCase();
 			html = matcher.replaceAll( today );			
 		}
 
 	}
 
     public void setOrderNumber(String orderNumber) {
         if ( emptyHtml() || orderNumber == null) { return; }
         Matcher matcher = ORDER_PATTERN.matcher(html);
         html = matcher.replaceAll(orderNumber);
     }
 
     public void setToken(String token, String value) {
 		if( emptyHtml() ){return;}
         token = token.replace("{", "\\{");
         token = token.replace("}", "\\}");
 		Pattern tp = Pattern.compile( token, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
         Matcher matcher = tp.matcher(html);
         html = matcher.replaceAll(value);
 
     }
 
     public Map<String, String> getTokens() {
 
         if (emptyHtml() ) {
             return Collections.<String, String>emptyMap();
         }
 
 		convertDates();
 
         Map<String, String> tokens = new HashMap<String, String>();
 
         Matcher matcher = TOKEN_PATTERN.matcher(html);
         while (matcher.find()) {
             String token = matcher.group().toLowerCase();
             tokens.put(token, "");
         }
 
         return tokens;
 
     }
 
     public static List<Document> findByPartNumber(String partNumber) {
         return find("partNumber = ? and accepted=true", partNumber).fetch();
     }
 
     public static Document findByPartNumberAndPage(String partNumber, int pageNumber) {
         return find("byPartNumberAndPage", partNumber, pageNumber).<Document>first();
     }
 
     public static long getPageCount(String partNumber) {
         return count("byPartNumber", partNumber);
     }
 }
