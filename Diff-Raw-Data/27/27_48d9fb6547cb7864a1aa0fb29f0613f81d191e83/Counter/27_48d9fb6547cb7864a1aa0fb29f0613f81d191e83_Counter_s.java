 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.mssm.pharm.maayanlab.FileUtils;
 
 @WebServlet(urlPatterns = {"/count"}, loadOnStartup=1)
 public class Counter extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -682732829814620653L;
 
	private AtomicInteger count = new AtomicInteger(0);	// Default initial value
 
 	@Override
 	public void init() throws ServletException {
 		// Try to load the initial count from our saved persistent state
 		BufferedReader bufferedReader = null;
 		try {
 			bufferedReader = new BufferedReader(new FileReader("/datasets/count"));
 			count.set(Integer.parseInt(bufferedReader.readLine()));
 		}
 		catch (FileNotFoundException ignored) { }  // no saved state
 		catch (IOException ignored) { }            // problem during read
 		catch (NumberFormatException ignored) { }  // corrupt saved state
 		finally {
 			// Make sure to close the file
 			try {
 				if (bufferedReader != null)
 					bufferedReader.close();
 			}
 			catch (IOException ignored) { }
 		}
 		
 		getServletContext().setAttribute("EnrichrCount", count);
 	}
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/plain");                                
 		response.getWriter().print(count);		
 	}   
 	
 	@Override
 	public void destroy() {                                            
 		super.destroy();  // entirely optional
 
 		// Try to save the accumulated count
 		FileUtils.writeString("/datasets/count", count.toString());
 	}                                                                  
 
 }
