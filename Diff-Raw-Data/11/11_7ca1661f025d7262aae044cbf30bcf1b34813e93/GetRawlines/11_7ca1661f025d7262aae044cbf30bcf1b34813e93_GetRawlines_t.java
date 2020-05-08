 package request_handlers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import JSONtypes.Line;
 import data_source_interface.DataSourceException;
 import data_source_interface.Mysql_Datasource;
 import data_source_interface.SSHD_log_vis_datasource;
 
 /**
  * Servlet implementation class GetRawlines
  */
 public class GetRawlines extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String JSONMimeType = "application/json";
 	private SSHD_log_vis_datasource datasource;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public GetRawlines() {
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
 		List<Line> lines;
 		if (request.getParameter("startTime") == null
 				|| request.getParameter("endTime") == null) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
 		try {
 			lines = datasource.getEntriesFromDataSource(request.getParameter("serverName"), request.getParameter("startTime"), request.getParameter("endTime"));
 		} catch(DataSourceException e) {
 			this.getServletContext().log(e.getMessage(), e.getCause());
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return;
 		}
 
 		if (lines.size() == 0){
 			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 			return;
 		}
 
 		response.setContentType(GetRawlines.JSONMimeType);
 		PrintWriter resp = response.getWriter();
		StringBuilder json = new StringBuilder("[");
 		for (Line l : lines){
			json.append(l.toJSONString());
			json.append(",");
 		}
		json.deleteCharAt(json.length() - 1);
		json.append("]");
		resp.write(json.toString());
 		resp.flush();
 	}
 }
