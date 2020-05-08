 package com.epam.lab.intouch.web.servlet;
 
 import static com.epam.lab.intouch.dao.util.PropertiesReader.getProperty;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FilenameUtils;
 
 import com.epam.lab.intouch.controller.member.common.MemberController;
 
 /**
  * Servlet implementation class ShowAvatarServlet
  */
 
 public class ShowAvatarServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ShowAvatarServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			ServletOutputStream out = response.getOutputStream();
 			//response.setContentType("image/jpg");
 			MemberController controller = new MemberController();
 			String name = request.getParameter("login");
 			/*if (name != null) {
 				InputStream in = (memberService.getPhoto(req
 						.getParameter("login")));
 				int count = 0;
 				byte[] buf = new byte[1024];
 				while ((count = in.read(buf)) >= 0) {
 					out.write(buf, 0, count);
 				}
 				
 				in.close();
 			}
 			else{*/
 				
 
 				String savePath = getProperty("avatar_storage");
 						
 				
 				File f = new File( savePath + File.separator+ controller.getById(name).getPhotoLink());
				//System.out.println(f.getPath().toString());//+f.getName().toString());
				//System.out.println(f.getName().toString());
 				String ext = FilenameUtils.getExtension(controller.getById(name).getPhotoLink());
 				
 				BufferedImage bi = ImageIO.read(f);
 				
 				ImageIO.write(bi, ext, out);
 			
 			out.close();
 		} catch (Exception e) {
 
 		}
 	
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
