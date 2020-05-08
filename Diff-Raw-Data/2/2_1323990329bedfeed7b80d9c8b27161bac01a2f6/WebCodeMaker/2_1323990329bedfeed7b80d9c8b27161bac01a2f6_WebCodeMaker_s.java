 package worker;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspWriter;
 
 /**
  * Used to return Stings or other data types in generating the
  * HTML code output to be interpreted by the web browser
  * @author nickleus
  *
  */
 public class WebCodeMaker {
 
 	private JspWriter out;
 	
 	
 	public WebCodeMaker(JspWriter writer){
 		out=writer;
 	}
 	
 	/*	Methods	<% //LBoardHTMLGenerator generator=new LBoardHTMLGenerator(); %>*/
 	public void writeJsElementReference(String elementName){
 		try {
 			out.write("\""+elementName+"\"");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String giveJsStringParam(String elementName){
 		return "\""+elementName+"\"";
 	}
 	
 	public String getleaderBoardHTMLTable(){
 		String tableCode="";
 		 /*
 			for(int ctr=0;ctr<Users.size();ctr++)
 				tableCode.concat("<tr>"+
 				"<td><img src='"+picURls.get(ctr)+"' class='profPic'/>"+Users.get(ctr).getName()+"</td>"+
 				"<td>15</td>"+
 				"<td>1510</td>"+
 				"</tr>");
 				*/
 		 return "<tr><td>lddd</td></tr>";
 	}
 }
