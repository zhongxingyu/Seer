 package sce.finalprojects.sceprojectbackend.managers;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.utils.UrlHelper;
 
 public class CommentsDownloadManager {
 	
 	
 	UrlHelper uh = new UrlHelper();
 	private StringBuilder commentString = new StringBuilder();
 	
 	/**
 	 * this function is the main function that get all the comments by the given URL
 	 */
 	public void getCommentsByUrl(URL url, int numOfComments, int threadId, int lastComment, BuildingTreeDataManager btdm, MaintenanceDataManager mdm) throws FileNotFoundException{	
 		String htmlArr[];
 		PrintWriter out = new PrintWriter("C:\\\\comments" + threadId + "_" + lastComment + ".txt"); ////TODO delete after testing
 		int initialOffset = 0;
 		int beginningComment = 0;
 				
 		if(lastComment == 0)
 			do{
 				htmlArr = getHtmlCommentsFromYahoo(uh.getFixUrl(uh.buildUrl(url), (threadId-1) * 100));			
 			}while(htmlArr == null);
 		else{
 			initialOffset = (lastComment/100) * 100;
 			if(threadId == 1)
 				beginningComment = lastComment - initialOffset;			
 			do{
 				htmlArr = getHtmlCommentsFromYahoo(uh.getFixUrl(uh.buildUrl(url), ((threadId-1) * 100) + initialOffset));	
 			}while(htmlArr == null);
 		}
 		
 		CommentEntityDS result;
 		int htmlArraySize = htmlArr.length;
 		for(int i = beginningComment; i < htmlArraySize && i < numOfComments + beginningComment; i++){
 			try {
 				result = getCommentEntityFromHtml(htmlArr[i], out, threadId, i, initialOffset);
 				if (btdm != null)
 					BuildingTreeDataManager.commentsArray[Integer.parseInt(result.getId()) - 1] = result;
 				else
 					MaintenanceDataManager.commentsArray[Integer.parseInt(result.getId()) - lastComment - 1] = result;
 
 			} catch (FileNotFoundException e) {
 						e.printStackTrace();
 			}	
 		}
 		out.close(); //TODO delete after testing
 	}
 	
 	 /**
      * the function connect to yahoo site, get the ajax object and turn it into an atml array that each cell
      * present an html of specific comment
      */
 	public String[] getHtmlCommentsFromYahoo(URL url){
 		String nextLine;
 	    URLConnection urlConn = null;
 	    InputStreamReader inStream = null;
 	    BufferedReader buff = null;
 	    String result;
 	    String[] htmlComments = null;
 	    try{
 	    	urlConn = url.openConnection();
 	        inStream = new InputStreamReader(urlConn.getInputStream());
 	        buff = new BufferedReader(inStream);
 	        while (true){
 	        	nextLine = buff.readLine(); 
 	            if (nextLine != null){
 	            	result = "" + nextLine;
 	                htmlComments = result.split("js-item comment ");
 	                int htmlCommentsSize = htmlComments.length;
 	                for(int i=1; i<htmlCommentsSize; i++){
 	                	htmlComments[i] = "<li class=\"js-item comment" + htmlComments[i];
                 		String temp[] = htmlComments[i].split("li>");
     	                htmlComments[i] = temp[0] + "li>";
 	                }
 	                String tempHtml[] = new String[htmlCommentsSize-1];
 	                for(int i = 1; i < htmlCommentsSize; i++)
 	                	tempHtml[i-1] = htmlComments[i];
 	                return tempHtml;
 	            }
 	            else{
 	               break;
 	            } 
 	        }
 	        } catch(MalformedURLException e){
 	        	System.out.println("Please check the URL:" + e.toString() );
 	        	} catch(IOException  e1){
 	        		System.out.println("Can't read  from the Internet: "+ e1.toString() ); 
 	        	}
 	        return htmlComments;
 	}
 	
 	 /**the function get html string, separate it into an entity of comment and update the commentString to prepare it
 	  * into text processing
 	  * 
 	  */
     public CommentEntityDS getCommentEntityFromHtml(String html, PrintWriter out, int i, int j, int initialOffset) throws FileNotFoundException{
   	  	CommentEntityDS result = new CommentEntityDS();
   	  	result.setId("" + ((i-1) * 100 + initialOffset + j + 1));//set the id of the comment(Serial number)
 		result.setCommentHTML(html); //set the htmlComment from the array  
 		String clearComment = cleanTheCommentFromTheHtml(html);
 		writeCommentInFile(out, clearComment, i, j, initialOffset);//TODO delete
 		addCommentToString(clearComment);
 	
 		return result;
     }
     
     public String cleanTheCommentFromTheHtml(String html){
    	String[] splitComment = html.split("comment-content");//get and set the clear comment from the html 
 		splitComment = splitComment[1].split("p>");
 		String clearComment = splitComment[0];
 		clearComment = clearComment.replaceAll("[ ]+", " ");
 		clearComment = (clearComment.substring(6, clearComment.length() - 6));
 		clearComment = prepareCommentToTextProcessing(clearComment);
		
 		return clearComment;
     	
     }
     
     public String prepareCommentToTextProcessing(String comment)
 	{
 		  String tempComment = comment;
 		  tempComment = tempComment.replaceAll("(&#39;)", "");
 		  tempComment = tempComment.replaceAll("\\W", " ");
 		  tempComment = tempComment.replaceAll("( quot )", "");
 		  tempComment = tempComment.replaceAll("( n )", "");
 		  tempComment = tempComment.replaceAll("( br )", "");
 		  
 		  StringBuilder temp = new StringBuilder();
 		  int commentSize = tempComment.length();
 		  for(int i = 0; i < commentSize; i++)
 			  if(!(Character.isDigit(tempComment.charAt(i))))
 					  temp.append(tempComment.charAt(i));
 			  else
 				  if(i == commentSize-1)
 					  temp.append("a");
 				  else
 					  if(!(Character.isDigit(tempComment.charAt(i+1)))) {
 						  temp.append(tempComment.charAt(i));
 						  temp.append("a");
 					  }
 					  else
 						  temp.append(tempComment.charAt(i)); 
 		  tempComment = temp.toString();
 		  if(tempComment.matches("[ ]+"))
 			  tempComment = tempComment + "a";
 		  tempComment = tempComment + ".";
 		  tempComment = tempComment.replaceFirst("[ ]+\\.", ".");
 		  tempComment = tempComment.toLowerCase();
		  
 		  return tempComment;
 	}
 	
 	public void addCommentToString(String comment){
 		this.commentString.append(comment);
 	}
 	
 	public String getCommentString(){
 		return this.commentString.toString();
 	}
 
 	//TODO delete after testing
 	public void writeCommentInFile(PrintWriter out , String comment, int i, int j, int initialOffset) throws FileNotFoundException{
     	int id = (i-1) * 100 + initialOffset + j + 1 ;
 		out.println(id + "  " + comment);
     }
 	
 }
