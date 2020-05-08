 package com.nbcedu.function.teachersignup.core.util.exl.process.writer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.struts2.ServletActionContext;
 
 public class WorkBookWriter {
 	///wether the exl file will save in the local file system
 	private static final Boolean LOCAL_FILE = Boolean.FALSE;
 	/**
 	 * 输出
 	 * @param book
 	 * @author xuechong
 	 */
 	public static void writeBook(Workbook book,String fileName) {
 		OutputStream out = getOutStream(fileName);
 		writeBook(book, fileName, out);
 	}
 	/**
 	 * use the given outputstream to write WorkBook
 	 * @param book
 	 * @param fileName
 	 * @param out
 	 */
 	public static void writeBook(Workbook book,String fileName,OutputStream out){
		if(fileName==null){
			fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
		}
 		if(out==null){
 			throw new NullPointerException("the outputstream is null when try to write workbook");
 		}
		if(fileName.indexOf(".xls")<=0){
			fileName+=".xls";
		}
 		try {
 			book.write(out);
 			out.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}finally{
 			if(out!=null){
 				try {
 					out.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	/**
 	 * get the outputstream to write the exl file
 	 * @return
 	 * @author xuechong
 	 */
 	public static OutputStream getOutStream(String fileName){
 		OutputStream out = null;
 		if (LOCAL_FILE) {//return the local file outstream
 			File parent = new File(File.listRoots()[0].getPath() + "exportedExls");
 			parent.mkdir();
 			File file = new File(parent.getPath() + File.separator + fileName
 					+ ".xls");
 			try {
 				out = new FileOutputStream(file);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		} else {//return the struts2 based out 
 			try {
 
 				String agent = (String) ServletActionContext.getRequest()
 						.getHeader("USER-AGENT");
 				if (agent != null && agent.indexOf("MSIE") == -1) {
 					ServletActionContext.getResponse().setHeader(
 							"Content-Disposition",
 							"attachment; filename=" +  
 							new String(fileName.getBytes("utf-8"),"ISO8859_1"));
 				} else {
 					// IE
 					ServletActionContext.getResponse().setHeader( 
 							"Content-Disposition", 
 							"attachment; filename=" +
 							URLEncoder.encode(fileName, "UTF8"));
 				}
 				
 				ServletActionContext.getResponse().
 				setContentType("application/vnd.ms-excel;charset=uft-8");
 				out = ServletActionContext.getResponse().getOutputStream();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return out;
 	}
 }
