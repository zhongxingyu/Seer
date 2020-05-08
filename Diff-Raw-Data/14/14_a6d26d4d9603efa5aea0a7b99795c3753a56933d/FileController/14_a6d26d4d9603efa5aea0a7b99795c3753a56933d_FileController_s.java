 package my.odysseymoon.controller;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 import javax.annotation.Resource;
 
 import my.odysseymoon.view.FileView;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
 @RequestMapping("/file")
 public class FileController {
 	
 	@Resource(name="fileView") 
 	private FileView fileView;
 	
 	@RequestMapping("/uploadPage")
 	private String uploadView() {
 		return "upload";
 	}
 	
 	@RequestMapping(value="/upload", method=RequestMethod.POST)
 	private String upload(@RequestParam MultipartFile imageFile, ModelMap modelMap) {
 
 		String fileName = imageFile.getOriginalFilename();
 		
 		byte[] bytes = null;
 		String savePath = "D:\\Temp" + File.separator + fileName;
 		String returnPath = "";
 
 		/* 파일 쓰기 */
 		BufferedOutputStream bos = null;
 		
 		try {
 			bytes = imageFile.getBytes();
 			bos = new BufferedOutputStream(new FileOutputStream(savePath));
 			bos.write(bytes);
 			
 			returnPath = URLEncoder.encode(savePath, "UTF-8");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (bos != null) {
 				try {
 					bos.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 				
 		modelMap.put("savePath", returnPath);
 		
 		return "uploadComplete";
 	}
 	
	@RequestMapping("/download/{filePath}")
	private FileView getImage(@PathVariable String filePath, ModelMap modelMap) {
 		
 		String realPath = "";
 		
 		try {
 			realPath = URLDecoder.decode(filePath, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		modelMap.put("filePath", realPath);
 		
 		return fileView;
 	}
 
 }
