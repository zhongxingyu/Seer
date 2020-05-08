 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import com.google.gson.Gson;
 
 
 public class PhotoServlet implements Servlet {
 	private ArrayList<String> photoMetaList;
 	public PhotoServlet(ArrayList<String> photoMetaList){
 		this.photoMetaList = photoMetaList;		
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public ServletConfig getServletConfig() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getServletInfo() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void init(ServletConfig arg0) throws ServletException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void service(ServletRequest arg0, ServletResponse arg1)
 			throws ServletException, IOException {	
 		arg1.getWriter().flush();
 		// 0 params in request, get set of photos that consumer has populated into queue
 		StringBuilder output = new StringBuilder();
	//	output.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
 		output.append("<response>");
 		synchronized(photoMetaList){
 
 			for(String metaData : photoMetaList) {
				//output.append("\n");
 				output.append(metaData);
 			//	output.append("\n"); 
 			}
 			output.append("</response>");
 			
 			// populate arg1 with XML
 			arg1.getWriter().write(output.toString());
 			arg1.getWriter().flush();
 			
 			// DO I WANT THIS???  THIS CLEARS SO THE OLD DATA DOES NOT STAY ON THE SCREEN WHEN IT"S REFRESHED
			//photoMetaList.clear();
 			
 
 		}
 
 	}
 }
