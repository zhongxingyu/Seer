 package worker;
 
import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.Story;
 
 /**
  * Encodes the Story generated into HTML
  * so that the browsers can show them to the users
  * @author nickleus
  *
  */
 public class StoryEncoder {
 
	HttpServletRequest Request;
	PrintWriter Out;
 	
	public StoryEncoder(HttpServletRequest request,PrintWriter out){
 		Request=request;
 		Out=out;
 	}
 	
 	public String writeStory(){
 		
 		StoryGenerator storyMaker=new StoryGenerator(10, getConfidence());//Number of templates and Confidence for now
 		Story myStory=storyMaker.getStory();
 		
 		return myStory.getsStory();
 	}
 	
 	public void encodeStory(){
 		StoryGenerator storyMaker=new StoryGenerator(10, getConfidence());//Number of templates and Confidence for now
 		Story myStory=storyMaker.getStory();
 		Request.setAttribute("Story", myStory);
		Out.write(myStory.getsStory());
 	}
 	
 	public String getStoryAttributeName(){return "Story";}
 	public int getConfidence(){ return 60;}
 	
 }
