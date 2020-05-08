 package jp.dev7.enchant.doga.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jp.dev7.enchant.doga.converter.l3c.L3cConverter;
 
 @SuppressWarnings("serial")
 public class ConvertServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.setContentType("application/json");
 		final L3cConverter converter = new L3cConverter();
 		try {
			converter.convertAndWriteJson(new File("test-data/darkhero.l3c"),
					resp.getWriter());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		resp.flushBuffer();
 	}
 }
