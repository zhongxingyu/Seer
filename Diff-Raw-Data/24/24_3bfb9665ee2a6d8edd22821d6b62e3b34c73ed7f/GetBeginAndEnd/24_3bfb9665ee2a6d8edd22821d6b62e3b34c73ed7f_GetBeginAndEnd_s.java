 package request_handlers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import data_source_interface.DataSourceException;
 import data_source_interface.Mysql_Datasource;
 import data_source_interface.SSHD_log_vis_datasource;
 
 /**
  * Servlet implementation class GetBeginAndEnd
  */
 public class GetBeginAndEnd extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private SSHD_log_vis_datasource datasource;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public GetBeginAndEnd() {
         super();
         this.datasource = new Mysql_Datasource();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		long[] answer;
 		try {
 			answer = this.datasource.getStartAndEndOfUniverse();
 		} catch (DataSourceException e) {
 			this.getServletContext().log(e.getMessage(), e.getCause());
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return;
 		}
 
 		PrintWriter w = response.getWriter();
 		w.print("{\"start\": " + answer[0] +
				"\"end\":" + answer[1] +
 				"}");
 		w.flush();
 		response.flushBuffer();
 		return;
 	}
 
 }
