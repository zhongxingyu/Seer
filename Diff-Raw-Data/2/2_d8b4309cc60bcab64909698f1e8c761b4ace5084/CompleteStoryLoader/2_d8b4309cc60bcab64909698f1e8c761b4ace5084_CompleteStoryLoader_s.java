 package webEncoder;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 
 import javax.servlet.jsp.JspWriter;
 
 import dao.AcomplishmentDAO;
 import dao.DAOFactory;
 import dao.LikedStoryDAO;
 import dao.UserDAO;
 
 import model.Story;
 import serializableObjects.StoryFileAccess;
 import entity.Acomplishment;
 import entity.User;
 
 /**
  * This loads the stories from the server to be returned to the browser
  * The AJAXscripts must be loaded from the html file for full functionality
  * @author nickleus`
  *
  */
 public class CompleteStoryLoader {
 
 	private User SessionUser;
 	public CompleteStoryLoader(){}
 
 	public CompleteStoryLoader(User u){
 		SessionUser=u;
 	}
 	/*Methods*/
 	public String previewStory(StoryFileAccess StoryF)
     {   
 		Story Story=StoryF.getMyStory();
         String story_preview = Story.getsStory();
         for (int i=0; i<StoryF.getAnswers().size(); i++)
         {
         	/*revised story writer format
         	 * 
         	 */
         story_preview = story_preview.replaceFirst("<input type='text' width='15' name='answer"+(i+1)+
         		"' id='answer"+(i+1)+"'/>.",
         		StoryF.getAnswers().get(i)+" ");
         story_preview = story_preview.replaceFirst("<input type='text' width='15' name='answer"+(i+1)+"' />.",
         		StoryF.getAnswers().get(i)+" ");
         }
         
         return story_preview;
     }
 	
 	/**
 	 * Loads a story given the file entered
 	 */
 	public String loadStory(String fileUrl){
 		FileInputStream fileIn;
 		try {
 			fileIn = new FileInputStream(fileUrl);
 			ObjectInputStream oi = new ObjectInputStream(fileIn);
 			StoryFileAccess storyFile=(StoryFileAccess)oi.readObject();
 			fileIn.close();
 			return previewStory(storyFile);
 		} catch(IOException ioEx){
 			return "File path problems D:";
 		}
 		catch(Exception ex){
 			//out.println("Error in getting the story\n"+ex.getMessage());
 		}
 		return "Error";
 	}
 	
 	/**
 	 * Shows the stories requires the AjaxScripts
 	 * @param out
 	 */
 	public void showStories(JspWriter out){
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		AcomplishmentDAO myAcomDAO=myDAOFactory.createAcomplishmentDAO();
 		ArrayList<Acomplishment> Stories=(ArrayList<Acomplishment>)myAcomDAO.getAllStories();
 		UserDAO myUserDao=myDAOFactory.createUserDAO();
 		User myUser;
 		
 		try{
 		//out.write("<p>");
 		out.write("<table id=\"tableBorderfeed2\" bgcolor=\"white\">");
 		out.write("<tr><td id='1st'></td></tr>");
 		/*loop that displays the stories*/
 		for(int ctr=0;ctr<Stories.size();ctr++){
 			out.write("<tr class=\"storyHead\">");
 			myUser=myUserDao.getUser(Stories.get(ctr).getAccountID());
 			out.write("<th>Title:</th><td> "+Stories.get(ctr).getName()+"</td> <th>Made by </th>" +
 					"<td>"+myUser.getName()+"</td></tr>");
 			
 			if(SessionUser!=null)
 				out.write(generateLikeRow(Stories.get(ctr))+"</tr>");
 			out.write("<tr><td colspan=\"4\">");
 			out.println("<br/>"+loadStory(Stories.get(ctr).getFileURL()));
 			out.write("<hr/></td></tr>");
 		}/*End of Loop*/
 		out.write("</table>");
 		//out.write("</p>");
 		}catch(IOException ie){}
 	}
 	
 	/**This generates an HTML code to be 
 	 * Placed in an HTML table
 	 * The row generated shows the number of people who liked
 	 * the story
 	 */
 	private String generateLikeRow(Acomplishment myStory){
 		//User myUser=(User)request.getSession().getAttribute("user");
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		LikedStoryDAO myLikeDAO=myDAOFactory.createLikeDAO();
 		
 		String storyID="story"+myStory.getID();
 		String val="<tr><th>Likes</th><td id=\""+storyID+"\"" +
 				">"+myLikeDAO.countStoryLikes(myStory.getID())+"</td>";
 		
 		String btElemName="btStory" +storyID,btIDElemName="id=\""+btElemName+"\"";
 		
 		if(myLikeDAO.didUserLike(SessionUser.getAccountID(), myStory.getID())){
 			val=val.concat("<td><button "+btIDElemName+" onclick=\"showNumberOfLikes('"+storyID+"'," +myStory.getID()+","+
 					"'----','"+btElemName+"')\">" +"Unlike</button></td>");
 		}
 		else val=val.concat("<td><button "+btIDElemName+
 				" onclick=\"showNumberOfLikes('"+storyID+"'," +myStory.getID()+","+"'like','"+btElemName+"')\">" +
 				"Like</button></td>");
 		
 		val=val.concat("</tr>");
 		return val;
 	}
 	
 	
 	/**
 	 * Show description of stories made by the user
 	 */
 	public void showUserStoryPreviews(User myUser,JspWriter out){
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		AcomplishmentDAO myAcomDAO=myDAOFactory.createAcomplishmentDAO();
 		ArrayList<Acomplishment> Stories=(ArrayList<Acomplishment>)myAcomDAO.getAllStoriesOfUser(myUser.getAccountID());
 		
 		try{
 			out.write("<table><caption>your stories</caption>");
 			
 			/*Table header*/
 			out.write("<tr>");
 			out.write("<th> Title </th>");
 			out.write("<th> Time Finished </th>");
 			out.write("<td>Link</td>");
 			out.write("</tr>");
 			
 			/*Loop that shows the story Links*/
 			for(int ctr=0;ctr<Stories.size();ctr++){
 				out.write("<tr>");
 				out.write("<td>"+Stories.get(ctr).getName()+"</td>");
 				out.write("<td>"+Stories.get(ctr).getFinishTime()+"</td>");
 				out.write("<td>"+createStoryLink(Stories.get(ctr).getID(), "storyStage")+"</td>");
 				//out.write("<td>See Story</td>");
 				out.write("</tr>");
 			}/*End of Loop*/
 			out.write("</table>");
 		}catch(IOException ie){}
 	}
 	
 	/**
 	 * Show description of stories made by the user
 	 */
 	public void PreviewUserStories(User myUser,JspWriter out){
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		AcomplishmentDAO myAcomDAO=myDAOFactory.createAcomplishmentDAO();
 		LikedStoryDAO myLikeDAO=myDAOFactory.createLikeDAO();
 		ArrayList<Acomplishment> Stories=(ArrayList<Acomplishment>)myAcomDAO.getAllStoriesOfUser(myUser.getAccountID());
 		String stageID;
 		try{
 			
 			
 			/*Loop that shows the story Links*/
 			for(int ctr=0;ctr<Stories.size();ctr++){
 				myLikeDAO.countStoryLikes(Stories.get(ctr).getID());
 				stageID="stage"+Stories.get(ctr).getID();
 				
 				/*Generate HTML code*/
 				out.write("<tr align=\"center\">");
 				out.write("<td>"+Stories.get(ctr).getName()+"</td>");
 				out.write("<td>50</td>");
 				out.write("<td>"+Stories.get(ctr).getFinishTime()+"</td>");
 				out.write("<td>"+myLikeDAO.countStoryLikes(Stories.get(ctr).getID())+"</td>");
 				out.write("<td>"+createStoryLink(Stories.get(ctr).getID(), stageID)+"</td>");
 				
 				out.write("</tr>" +
						"<tr><td class=\"hiddenElem\" id=\""+stageID+"\"></td>");
 				
 				out.write("</tr>");
 			}/*End of Loop*/
 			//out.write("</table>");
 		}catch(IOException ie){}
 	}
 	
 	/**
 	 * Creates a HTML element to Asynchronously get the Story from the server
 	 * @param storyID : The ID of the Accomplishment
 	 * @param stageID : Where the story will be written
 	 * @return
 	 */
 	public String createStoryLink(int storyID,String stageID){
 		String link="<button onclick=\"showStory('"+stageID+"',"+storyID+")\">";
 		return link.concat("See Story</button>");
 	}
 	
 	
 	/*For demo purpose*/
 	public String loadSampleStory(){
 		FileInputStream fileIn;
 		try {
 			fileIn = new FileInputStream("The introduction of Simba522066365Simba.story");
 			
 			ObjectInputStream oi = new ObjectInputStream(fileIn);
 			StoryFileAccess storyFile=(StoryFileAccess)oi.readObject();
 			fileIn.close();
 			return previewStory(storyFile);
 		} catch(IOException ioEx){
 			return "File path problems D:";
 		}
 		catch(Exception ex){
 			//out.println("Error in getting the story\n"+ex.getMessage());
 		}
 		return "Error";
 	}
 }
