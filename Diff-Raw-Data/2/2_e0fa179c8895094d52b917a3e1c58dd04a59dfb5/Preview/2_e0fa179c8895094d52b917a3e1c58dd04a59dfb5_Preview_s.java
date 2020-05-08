 package plotter.servlet;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import plotter.pdf.PrintJob;
 
 /**
  * Preview image servlet
  */
 public class Preview extends HttpServlet {
 
 	private static final long serialVersionUID = 2386284613636739972L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public Preview() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(true);
 
 		// Get request parameters
 		String key = request.getParameter("key");
 		int num = Integer.valueOf(request.getParameter("num"));
 
 		// Get jobs from session
 		@SuppressWarnings("unchecked")
 		Map<String, PrintJob> jobs = (LinkedHashMap<String, PrintJob>)
 			session.getAttribute(Process.sessionJobs);
 
		if( jobs == null || ! jobs.containsKey(key)) {
 			// Job not found
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 
 			return;
 		}
 
 		PrintJob job = jobs.get(key);
 
 		if(job.getNumberOfPages() <= num) {
 			// Image not found
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 
 			return;
 		}
 
 		File image = job.getThumbnails().get(num);
 
 		// Set content type
 		response.setContentType("image/png");
 
 		InputStream input = new BufferedInputStream(new FileInputStream(image));
 
 		OutputStream output = response.getOutputStream();
 
 	    // Copy the image to the output stream
 	    byte[] buffer = new byte[4*1024];
 	    int length = 0;
 	    while ((length = input.read(buffer)) >= 0) {
 	        output.write(buffer, 0, length);
 	    }
 
 		input.close();
 		output.close();
 	}
 
 }
