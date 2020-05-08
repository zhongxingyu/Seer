 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	private static boolean debug = true;
 	private static URL url;
 	private static URLConnection urlConnection;
 	private static InputStream inputStream;
 	private static String content;
 	private static byte[] contentRaw;
 	private static Pattern pattern;
 	private static Matcher matcher;
 	private static String htmlDocTxt;
 	private static Elements aElements;
 	private static Elements pElements;
 	private static Elements divElements;
 	private static Elements trElements;
 	private static Elements tdElements;
 	private static Elements liElements;
 	
 	public static void main(String[] args) throws Exception {
 		
		String[] inputUrls = new String[] {"http://www.cs.umd.edu/class/"};
 		
 		for(int i = 0; i < inputUrls.length; i ++)
 		{
 			
 			String inputUrl = inputUrls[i];
 			Document htmlDoc = Jsoup.connect(inputUrl).get();
 			
 			aElements = htmlDoc.select("a");
 			pElements = htmlDoc.select("p");
 			divElements = htmlDoc.select("div");
 			trElements = htmlDoc.select("tr");
 			tdElements = htmlDoc.select("td");
 			liElements = htmlDoc.select("li");
 			
 			htmlDocTxt = htmlDoc.toString();
 			
			String regex="(\\b[A-Z]{2,4}\\s[a-zA-Z]?[0-9]{2,4}?[a-zA-Z])";	// Any Single Word Character (Not Whitespace) 1
 			pattern = Pattern.compile(regex);
 			matcher = pattern.matcher(htmlDocTxt);
 			
 			while (matcher.find())
 			{
 				String found=matcher.group();
 				int countFound=matcher.groupCount();
 				Set<Element> eSet = new HashSet<Element>();
 				HashMap<String, String> eMap = new HashMap<String, String>();
 				Elements e = htmlDoc.getElementsContainingOwnText(found);
 				Elements courseLinks = new Elements();
				for(Element element : e)
				{
//					Elements parentElements = e.parents();
//					Main.log("Immediate Parent: " + parentElements.get(0));
					courseLinks.add(element);
					courseLinks.get(0).getElementsByAttribute("href");
				}
 				eSet.add(e.get(0));
 				Main.log(found + "\t" + e.get(0));
 				int endIndex = courseLinks.get(0).toString().indexOf("\">");
//				Main.log(courseLinks.get(0).toString().substring((courseLinks.get(0).toString().indexOf("href"))+6).length());
//				Main.log(endIndex);
				Main.log(courseLinks.get(0).toString().substring((courseLinks.get(0).toString().indexOf("href"))+6));
				Main.log(courseLinks.get(0).toString().substring((courseLinks.get(0).toString().indexOf("href"))+6,endIndex) + "<--");
 			}

 		}
 	}
 	
 	private static void log(Object o)
 	{
 		if(debug)
 			System.err.println(o);
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 /* IN MAIN:
  * FindTagsElement findTagsObj = new FindTagsElement();
         ArrayList<String> divList = findTagsObj.find("http://www.cs.washington.edu/education/courses/","div");
         ArrayList<String> aList = findTagsObj.find("http://psych.nyu.edu/courses/undergraduatecatalog.html#V89.0001","a");
         ArrayList<String> plist=findTagsObj.find("http://psych.nyu.edu/courses/undergraduatecatalog.html#V89.0001","p");
                   
         ArrayList<String> listUsing=divList;
         ArrayList<String> titles=new ArrayList<String>();
         ArrayList<String> descs=new ArrayList<String>();
         if(divList.size()<20)
             listUsing=plist;
         for (String i: listUsing)
         {
             String title="";
             String desc="";
             boolean titleFound=false;
             boolean validCourse=true;
             String[] parts=i.split("<");
             int sizeOfTempDescs=5;
             String[] tempDescs=new String[sizeOfTempDescs];
             int descCount=0;
             
             for( String j: parts)
             {
                if(validCourse)  //if the we consider this is a course
                { String[] segs=j.split(">");
                         
 	               if (segs.length!=1)
 	               {
 	                    //if title is found, look for course description
 	                   if(titleFound){                             
 	                       if(segs[1].length()>60)
 	                       {
 	                           if(descCount<5)
 	                           {desc=segs[1];
 	                           tempDescs[descCount]=desc; 
 	                           descCount++;
 	                           }
 	                           else  //if the the number of description we got is greater than 5, we consider this not a course
 	                           {
 	                              validCourse=false;
 	                           }
 	                       }
 	                   }
 	                                    
 	                   //look for title is its not found yet
 	                   if(!titleFound){
 	                       if(segs[1].length()>10)
 	                       { 
 	                           if(!segs[1].contains("\n")) //if it contains a new line character, we consider it not a title
 	                           {title=segs[1];                        
 	                           titleFound=true;}
 	                       }
 	                   }
 	               }            
                }     
             }
             if(!desc.equals("")&&!titles.contains(title)&&validCourse)                  
             {
             	titles.add(title);
             	System.out.println("Course Title: "+title);
             	for(String k: tempDescs)
             		if(k!=null)
             			System.out.println("Course Description: "+k);
             }
  */
