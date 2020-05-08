 package worker;
 
import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
 
 import model.Story;
 
 /**
  * Encodes the Story generated into HTML
  * so that the browsers can show them to the users
  * @author nickleus
  *
  */
 public class StoryEncoder {
 
	private HttpServletRequest Request;
	private JspWriter Out;
 	
	public StoryEncoder(HttpServletRequest request,JspWriter out){
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
		try {
			Out.write(myStory.getsStory());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	
 	public String getStoryAttributeName(){return "Story";}
 	public int getConfidence(){ return 60;}
 	
 }
