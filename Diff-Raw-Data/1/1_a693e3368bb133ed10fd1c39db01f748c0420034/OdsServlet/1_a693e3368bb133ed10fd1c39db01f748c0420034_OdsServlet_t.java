 /*
  * Created on 4.05.2006
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package eionet.meta.exports.ods;
 
 import java.io.*;
 import java.sql.Connection;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import eionet.meta.DDSearchEngine;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.Util;
 import eionet.util.sql.ConnectionUtil;
 
 /**
  * 
  * @author jaanus
  */
 public class OdsServlet extends HttpServlet{
 
 	/*
 	 *  (non-Javadoc)
 	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	protected void service(HttpServletRequest req, HttpServletResponse res)
     												throws ServletException, IOException {
     	
 		Connection conn = null;
 		String workingFolderPath = null;
 		try{
 			// get object id
             String id = req.getParameter("id");
 	        if (Util.voidStr(id)) throw new Exception("Missing request parameter: id");
 
 			// get object type
 			String type = req.getParameter("type");
 			if (Util.voidStr(type)) throw new Exception("Missing request parameter: type");
 			
 	        // get schema URL base
 			String schemaURLBase = Props.getProperty(PropsIF.XLS_SCHEMA_URL);
 			if (Util.voidStr(schemaURLBase))
 				throw new Exception("Missing property: " + PropsIF.XLS_SCHEMA_URL);
 
 			// prepare workinf folder
 			workingFolderPath = prepareWorkingFolder(req.getSession().getId());
 
 			// get db connection
 			conn = ConnectionUtil.getConnection();
                 
 	        // set up the ods object
             Ods ods = null;
             DDSearchEngine searchEngine = new DDSearchEngine(conn);
             if (type.equals("dst")){
             	ods = new DstOds(searchEngine, id);
             }
             else if (type.equals("tbl")){
             	ods = new TblOds(searchEngine, id);
             }
             else
             	throw new Exception("Unknown object type: " + type);
 
 	        ods.setWorkingFolderPath(workingFolderPath);
 			ods.setSchemaURLBase(schemaURLBase);
 	        
 	        // process the ods object
 	        ods.processContent();
 	        ods.processMeta();
 	        
 	        // prepare response
	        res.setContentType("application/vnd.oasis.opendocument.spreadsheet");
 			StringBuffer buf = new StringBuffer("attachment; filename=\"").
 			append(ods.getFinalFileName()).append("\"");
 			res.setHeader("Content-Disposition", buf.toString());
 			
 			// write the ods result file into response
 			writeFileIntoResponse(new File(ods.getWorkingFolderPath() + Ods.ODS_FILE_NAME), res);			
 		}
 	    catch (Exception e){
 	        e.printStackTrace(System.out);
 			throw new ServletException(Util.getStack(e));
 	    }
 		finally{
 			
 			// close DB connection
 			try{
 				if (conn != null) conn.close();				
 			}
 			catch(Exception e){}
 			
 			// clean up the working folder
 			try{
 				if (req.getParameter("keep_working_folder")==null)
 					deleteFolder(workingFolderPath);
 			}
 			catch(Exception e){}
 		}
 	}
 	
 	/**
 	 * 
 	 *
 	 */
 	private String prepareWorkingFolder(String sessionID) throws Exception{
 
 		// get ods-folder path
         String odsFolder = Props.getProperty(PropsIF.OPENDOC_ODS_PATH);
         if (odsFolder==null)
         	throw new Exception("Missing property: " + PropsIF.OPENDOC_ODS_PATH);
         else if (!odsFolder.endsWith(File.separator))
         	odsFolder = odsFolder + File.separator;
         
         // get DD temporary folder
         String tmpFilePath = Props.getProperty(PropsIF.TEMP_FILE_PATH);
         if (tmpFilePath==null)
         	throw new Exception("Missing property: " + PropsIF.TEMP_FILE_PATH);
         else if (!tmpFilePath.endsWith(File.separator))
         	tmpFilePath = tmpFilePath + File.separator;
         
         // build working folder name
         StringBuffer buf = new StringBuffer(tmpFilePath);
         buf.append("ods_");
         buf.append(sessionID);
         buf.append("_");
         buf.append(System.currentTimeMillis());
         
         // create working folder
         File workginFolder = new File(buf.toString());
         if (workginFolder.mkdir()==false)
         	throw new Exception("Failed to create directory: " + buf.toString());
         
         String s = buf.toString() + File.separator;
 
         // copy ods-file into working folder
         File odsFile = new File(odsFolder + Ods.ODS_FILE_NAME);
         File cpyOdsFile = new File(s + Ods.ODS_FILE_NAME);
         copyFile(odsFile, cpyOdsFile);
 
         // copy content-file into working folder
         File contentFile = new File(odsFolder + Ods.CONTENT_FILE_NAME);
         File cpyContentFile = new File(s + Ods.CONTENT_FILE_NAME);
         copyFile(contentFile, cpyContentFile);        
 
         // copy meta-file into working folder
         File metaFile = new File(odsFolder + Ods.META_FILE_NAME);
         File cpyMetaFile = new File(s + Ods.META_FILE_NAME);
         copyFile(metaFile, cpyMetaFile);        
         
         // return the newly created working folder's path
         return buf.toString();
 	}
 	
 	/*
 	 * 
 	 */
 	private void deleteFolder(String folderPath){
 		
 		File folder = new File(folderPath);
 		File[] files = folder.listFiles();
 		for (int i=0; files!=null && i<files.length; i++){
 			files[i].delete();
 		}
 		
 		folder.delete();
 	}
 	
 	/**
 	 * 
 	 * @param in
 	 * @param out
 	 * @throws Exception
 	 */
 	public void copyFile(File in, File out) throws Exception {
 		
 		FileInputStream fis = null;
 		FileOutputStream fos = null;
 		try{
 			fis = new FileInputStream(in);
 		    fos = new FileOutputStream(out);
 		    byte[] buf = new byte[1024];
 		    int i = 0;
 		    while((i=fis.read(buf))!=-1){
 		    	fos.write(buf, 0, i);
 		    }
 		}
 		finally{
 		    if (fis!=null) fis.close();
 		    if (fos!=null) fos.close();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param file
 	 * @param res
 	 * @throws IOException
 	 */
 	private void writeFileIntoResponse(File file, HttpServletResponse res) throws IOException{
 		
 		int i = 0;
 		byte[] buf = new byte[1024];
 		FileInputStream in = null;
 		OutputStream out = null;
 		
 		try{
 			in = new FileInputStream(file);
 			res.setContentLength(in.available());
 			out = res.getOutputStream();
 			while ((i=in.read(buf, 0, buf.length)) != -1){
 				out.write(buf, 0, i);
 			}
 		}
 		finally{
 			if (in!=null) in.close();
 			if (out!=null) out.close();
 		}
 	}
 }
