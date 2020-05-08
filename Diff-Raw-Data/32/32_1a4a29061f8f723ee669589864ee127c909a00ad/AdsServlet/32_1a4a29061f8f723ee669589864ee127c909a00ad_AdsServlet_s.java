 package ads.controller;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class AdsServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		doPost(request, response);
 	}
 
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		System.out.println("AAAAAAAAAAAAAAAdsServlet doPost is running up.");
 		request.setCharacterEncoding("UTF-8");
 		response.setCharacterEncoding("UTF-8");
 		
 		//取參數
 		String action = request.getParameter("action");
 		
 		//轉給facade
 		AdsFacade facade = new AdsFacade(request, response);
 		
 		if ( action == null ){
 			facade.saveAdsImg();
 		}
 		
 		if ( action != null && action.equals("queryProducts")){
 			String queryProducts = request.getParameter("queryProducts").trim();
 			
 			facade.querySearchProducts(queryProducts);
 		}
 		
 		if ( action != null && action.equals("setAds")){
 			String fileName = request.getParameter("fileName");
 			String productName = request.getParameter("productName");
 			
 			facade.setAds(fileName, productName);
 		}
 		
 		if ( action != null && action.equals("getAll")){
 			System.out.println("*********************Ads GETALL******************");
 			facade.getAll();
 		}
 		
		if ( action != null && action.equals("")){
 			
 		}
 
 	}
 
 }
