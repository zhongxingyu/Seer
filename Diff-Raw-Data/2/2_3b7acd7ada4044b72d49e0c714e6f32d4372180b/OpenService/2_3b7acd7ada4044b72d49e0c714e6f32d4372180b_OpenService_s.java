 package webfs.service;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping(value = "/open")
 public class OpenService {
 	@Autowired
 	private String basePath;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public void open(@RequestParam String wpath, HttpServletResponse response) {
 
 		String path = "";
 		if (wpath != null) {
 			// TODO: Validade input
 			path = wpath;
 		}
 
 		File file = new File(basePath + "/" + path);
 		if (file != null && !file.isDirectory()) {
 			response.setContentType("application/octet-stream");
 			response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
 			try {
				FileInputStream fis = new FileInputStream(new File(path));
 				BufferedInputStream bis = new BufferedInputStream(fis);
 				
 				IOUtils.copy(bis, response.getOutputStream());
 				
 				bis.close();
 				fis.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void setBasePath(String basePath) {
 		this.basePath = basePath;
 	}
 }
