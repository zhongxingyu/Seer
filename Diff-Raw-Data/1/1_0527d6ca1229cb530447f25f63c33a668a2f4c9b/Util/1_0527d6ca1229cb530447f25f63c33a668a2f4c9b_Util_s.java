 package util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import db.HSession;
 import db.data.Article;
 
 
 public class Util {
 	
 	
 	/**
 	 * @param hql
 	 * @param maxNum 
 	 * @return
 	 * @Description: get elements from db, if maxNum <= 0 will not set the maxNum per returned
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> List<T> getElementsFromDB(String hql,int maxNum) {
 		List<T> result = new ArrayList<T>();
 		Session session = new HSession().createSession();
 		Query query = session.createQuery(hql);
 		if(maxNum > 0){
 			query.setMaxResults(maxNum);
 		}		
 		result = query.list();
 		session.close();
 		return result;
 	}
 	
 	/**
 	 * @param id
 	 * @return
 	 * @Description:from id to get article
 	 */
 	@SuppressWarnings("unchecked")
 	public static Article getArticleById(int id){
 		Session session = new HSession().createSession();
 		List<Article> results = new ArrayList<Article>();
 		String hql = "from Article as obj where obj.id=" + String.valueOf(id);
 		Query query = session.createQuery(hql);
 		results = (List<Article>)query.list();
 		Article result = new Article();
 		if(results.size()>0){
 			result = results.get(0);
 		}
 		return result;
 	}
 	
 	public static <T> void updateDB(List<T> scrs) {
 		Session session = new HSession().createSession();
 		Transaction tx = session.beginTransaction();		
 		for(T t : scrs){
 			try{
 				session.merge(t);				
 			}catch(Exception e){
 				System.out.println("update error" );
 				e.printStackTrace();
 			}
 		}
 		//for gc
 		scrs = null;
 		tx.commit();
 		session.flush();	
 		session.clear();
 	}
 	
 	public static String getDateStr(Date date){
 		if(date == null){
 			date = new Date();
 		}
 		String result = "";
 		Calendar calendar = GregorianCalendar.getInstance(); 
 		calendar.setTime(date);		
 		int year = calendar.get(Calendar.YEAR);
 		int month = calendar.get(Calendar.MONTH) + 1;
 		int day = calendar.get(Calendar.DATE);		
 		result += year + "-" +month + "-" + day;
 		return result;
 	}
 	
 	/**
 	 * @param texts
 	 * @return
 	 * @Description:convert list to string
 	 */
 	public static String ListToStr(List<String> texts){
 		String result = "";
 		for(String text:texts ){
 			result+=text;
 		}		
 		return result;
 	}
 	
 	/**
 	 * @param texts
 	 * @return
 	 * @Description:convert list to string
 	 */
 	public static String ListToStrForm(List<String> texts){
 		String result = "";
 		for(String text:texts ){
 			result += "<p>    " + text + "</p>\n";
 		}		
 		return result;
 	}
 	
 	/**
 	 * @param texts
 	 * @return
 	 * @Description:convert list to string
 	 */
 	public static String ListToStr(List<String> texts,String split){
 		String result = "";
 		int num = 0;
 		for(String text:texts ){
 			num++;
 			if(num == 1){
 				result = text;
 			}else{
 				result += split +  text ;	
 			}		
 		}		
 		return result;
 	}
 	
 	/**
 	 * @param texts
 	 * @return
 	 * @Description:convert list to string
 	 */
 	public static String ListToStr(List<String> texts,String split,int n){
 		String result = "";
 		int num = 0;
 		for(String text:texts ){
 			num++;
 			if(num == 1){
 				result = text;
 			}else{
 				result += split +  text ;	
 			}		
 			if(num > n)
 				break;
 		}		
 		return result;
 	}
 	
 	public static String MD5(String str){
 		String result = "";
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("MD5");
 			  byte[] dataBytes = str.getBytes();
 		      md.update(dataBytes);
 		        byte[] mdbytes = md.digest();		 
 		        //convert the byte to hex format method 1
 		        StringBuffer sb = new StringBuffer();
 		        for (int i = 0; i < mdbytes.length; i++) {
 		          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
 		        }
 		        //convert the byte to hex format method 2
 		        StringBuffer hexString = new StringBuffer();
 		    	for (int i=0;i<mdbytes.length;i++) {
 		    		String hex=Integer.toHexString(0xff & mdbytes[i]);
 		   	     	if(hex.length()==1) hexString.append('0');
 		   	     	hexString.append(hex);
 		    	}
 		    	result =  hexString.toString();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;     
 	}
 	
 	public static String MD5OfByte(byte[] dataBytes){
 		String result = "";
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("MD5");
 		      md.update(dataBytes);
 		        byte[] mdbytes = md.digest();		 
 		        //convert the byte to hex format method 1
 		        StringBuffer sb = new StringBuffer();
 		        for (int i = 0; i < mdbytes.length; i++) {
 		          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
 		        }
 		        //convert the byte to hex format method 2
 		        StringBuffer hexString = new StringBuffer();
 		    	for (int i=0;i<mdbytes.length;i++) {
 		    		String hex=Integer.toHexString(0xff & mdbytes[i]);
 		   	     	if(hex.length()==1) hexString.append('0');
 		   	     	hexString.append(hex);
 		    	}
 		    	result =  hexString.toString();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;     
 	}
 	
 	//words string in db convert to map
 	//format is "a,5 b,10" 
 	public static  Map<String,Integer> getDdfMap(String words){
 		Map<String,Integer> results = new HashMap<String,Integer>();
 		String[] wds = words.split(" ");
 		for(String wd : wds){
 			if(wd.length() == 0)
 				continue;
 			String[] score = wd.split(",");
 			results.put(score[0], Integer.valueOf(score[1]));
 		}			
 		return results;
 	}
 	
 	///write map to str format
 	public static String DdfMapToStr(Map<String,Integer> words){
 		StringBuilder result = new StringBuilder();
 		Iterator<String> its = words.keySet().iterator();
 		while(its.hasNext()){
 			String word = its.next();
 			result.append(word + "," + words.get(word) + " ");
 		}
 		return result.toString();
 	}
 
 	public static void ArrayCopy(double a[][] , double b[][], int N, int M){
 		for(int i = 0 ;i<N;i++){
 			for(int j = 0 ;j<M;j++){
 				a[i][j] = b[i][j];
 			}
 		}
 	}
 	
 	public static List<String> FileToLines(String in){
 		List<String> result = new ArrayList<String>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(in));
 			String line = "";
 			while((line = br.readLine())!=null){
 				result.add(line);
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 }
