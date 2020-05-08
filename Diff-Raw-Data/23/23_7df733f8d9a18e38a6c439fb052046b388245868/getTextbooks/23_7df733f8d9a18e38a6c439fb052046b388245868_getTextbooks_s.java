 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.*;
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 public class getTextbooks {
 	
 	private static String UID;
 	private static String last_name;
 	private static String term_id;
 	
 	
 	public getTextbooks(String id, String lastName, String termID) {
 	    setUID(id);
 	    setLastName(lastName);
 	    setTermID(termID);
 	}
 	
 	private static String getISBN(String title) {
 		
 		int beginIndex = 0;
 		int endIndex = 0;
 		for(int i = 0; i < title.length(); i++) {
 			if(title.charAt(i) == '(')
 				beginIndex = i + 1;
 			else if(title.charAt(i) == ')') {
 				endIndex = i;
 				break;
 			}
 		}
 		
 		return title.substring(beginIndex, endIndex);
 	}
 	private static ArrayList<String> getCourseIDs(String input) {
 		
 		ArrayList<String> idList = new ArrayList<String>();
 		int beginIndex = 0;
 		int endIndex = 0;
		boolean registration_nbr = false;
 
 		for(int i = 0; i < input.length(); i++) {
 			
			if(i + 22 < input.length() && input.substring(i, i + 23).equals("registration_nbr=&quot;")) {
				beginIndex = i + 23;
				registration_nbr = true;
 				i = beginIndex;
 			}
			else if(i + 5 < input.length() && registration_nbr && input.substring(i, i + 6).equals("&quot;")) {
 				endIndex = i;
 				idList.add(input.substring(beginIndex, endIndex));
				registration_nbr = false;
 			} 
 				
 		}
 		
 		return idList;
 	}
 	private ArrayList<String> getISBNList() {
 		
 		ArrayList<String> ISBNList = new ArrayList<String>();
 		
 		try {
 			
 			String output1 = "";
 			String data1 = URLEncoder.encode("student_id", "UTF-8") + "=" + URLEncoder.encode(UID, "UTF-8");
 			data1 += "&" + URLEncoder.encode("term_id", "UTF-8") + "=" + URLEncoder.encode(term_id, "UTF-8");
 			data1 += "&" + URLEncoder.encode("last_name", "UTF-8") + "=" + URLEncoder.encode(last_name, "UTF-8");
 			
 			// Send data
 			URL url1 = new URL("http://www.collegestore.org/textbookstore/student_login_ch.asp");
 			URLConnection conn1 = url1.openConnection();
 			conn1.setDoOutput(true);
 		    OutputStreamWriter wr = new OutputStreamWriter(conn1.getOutputStream());
 		    wr.write(data1);
 		    wr.flush();
 		    
 		    // Get the response
 		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
 		    String line1;
 		    while ((line1 = rd.readLine()) != null) {
 		        // Process line..
 		    	output1 += line1;
 		    }
 		    wr.close();
 		    rd.close();
 		    
 		    // Parse Course ID's from data
 		    Document doc1 = Jsoup.parse(output1);
 		    
 		    ArrayList<String> IDList = getCourseIDs(doc1.getElementsByTag("form").get(0).getElementsByAttributeValue("name","XMLDOC").toString());
 			
 			String output2 = "";
 		    // Construct data
 			String xmlstreamValue = "<?xml version='1.0'?>" + 
 									"<!DOCTYPE express_textbooks SYSTEM" +
 									" 'express_textbooks.dtd'><express_textbooks>";
 			for(String a : IDList) {
 		    	xmlstreamValue += "<Registrar_Item term_code='121' registration_nbr='" + a + "'/>";
 		    }
 			
 			xmlstreamValue +="</express_textbooks>";
 			
 			
 			// Setup data
 		    String data2 = URLEncoder.encode("xmlstream", "UTF-8") + "=" + URLEncoder.encode(xmlstreamValue, "UTF-8");
 		    data2 += "&" + URLEncoder.encode("mode", "UTF-8") + "=" + URLEncoder.encode("3", "UTF-8");
 		    data2 += "&" + URLEncoder.encode("student_id", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
 		    
 		    // Send data
 		    URL url2 = new URL("http://www.uclaestore.com/ucla/textbook_express.asp");
 		    URLConnection conn2 = url2.openConnection();
 		    conn2.setDoOutput(true);
 		    OutputStreamWriter wr2 = new OutputStreamWriter(conn2.getOutputStream());
 		    wr2.write(data2);
 		    wr2.flush();
 		    
 		    // Get the response
 		    BufferedReader rd2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
 		    String line2;
 		    while ((line2 = rd2.readLine()) != null) {
 		        // Process line..
 		    	output2 += line2;
 		    }
 		    wr.close();
 		    rd.close();
 		    
 		    // Parse ISBN numbers from data
 		    Document doc2 = Jsoup.parse(output2);
 		    
 		    Elements courseNames = doc2.getElementsByClass("productTableRegRow");
 		    
 		    ArrayList<String> courseNameList = new ArrayList<String>();
 		    String courseName;
 		    String courseInstructor;
 		    for(int k = 0; k < courseNames.size(); k++) {
 		    	if(courseNames.get(k).getElementsByClass("productTableRegText").size() == 5) {
 		    		courseName = courseNames.get(k).getElementsByClass("productTableRegText").get(1).html();
 		    		courseName += " " + courseNames.get(k).getElementsByClass("productTableRegText").get(2).html();  
 		    		courseInstructor =  courseNames.get(k).getElementsByClass("productTableRegText").get(4).html();
 		    		courseNameList.add("Course: " + courseName + ", Instructor: " + courseInstructor);
 		    	}
 		    }
 		    
 		    Elements odd = doc2.getElementsByClass("productTableOddRow");
 		    Elements even = doc2.getElementsByClass("productTableEvenRow");
 		    
 		    
 		    for(int i = 0; i < odd.size(); i++) {
 
 		    	if(odd.get(i).getElementsByClass("productTableText").size() > 2 &&
 		    	   odd.get(i).getElementsByClass("productTableText").get(2).html().equals("Required")) 
		    		ISBNList.add(courseNameList.get(i) + ", ISBN: " + odd.get(i).getElementsByClass("productTableText").get(0).html());
 		    }
 		 
 		    for(int j = 0; j < even.size(); j++) {
 		    	if(even.get(j).getElementsByClass("productTableText").size() > 2 &&
 		    	   even.get(j).getElementsByClass("productTableText").get(2).html().equals("Required")) 
 		    		ISBNList.add(courseNameList.get(j) + ", ISBN: " + getISBN(even.get(j).getElementsByClass("productTableText").get(0).html()));
 		    } 	    
 		    
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		
 		return ISBNList;
 	}
 
 	public String getLastName() {
 		return last_name;
 	}
 
 	public void setLastName(String lastName) {
 		last_name = lastName;
 	}
 
 	public String getTermID() {
 		return term_id;
 	}
 
 	public void setTermID(String termID) {
 		term_id = termID;
 	}
 
 	public String getUID() {
 		return UID;
 	}
 
 	public void setUID(String uID) {
 		UID = uID;
 	}
 	public static void main(String [] args) {
		getTextbooks isbn = new getTextbooks("603658544", "huynh", "121");
 		ArrayList<String> a = new ArrayList<String>();
 		a = isbn.getISBNList();
 		for (String b : a) 
 			System.out.println(b);
 	}
 }
