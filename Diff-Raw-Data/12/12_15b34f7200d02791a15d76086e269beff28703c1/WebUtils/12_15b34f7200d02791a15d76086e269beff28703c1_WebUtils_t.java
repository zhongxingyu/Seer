 /*
  *  Copyright 2010 The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.mitre.medj;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.PageContext;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.mitre.medj.jaxb.ContinuityOfCareRecord;
 
 
 /**
  *  Miscellaneous utils for use in JSPs and for other web-based areas of the
  *  code.
  *
  * @author     jchoyt
  * @created    April 22, 2004
  */
 public class WebUtils
 {
 
     public final static String KEY = WebUtils.class.getName();
     public final static Logger log = Logger.getLogger( KEY );
     
     public static String BASE_DIR = "";
     public final static String CCR_DIR = "ccrFiles";
     // static{log.setLevel(Level.FINER);}
     /**
      *  Retrieves a parameter from a passed request. If the parameter is not
      *  found, an NPE is thrown.
      *
      * @param  req                       Request object to look in
      * @param  name                      the name of the field from the html
      *      form
      * @return                           the value from the html form
      *      corresponding to the name parameter
      * @exception  NullPointerException  thrown if the name was not in the html
      *      form
      */
     public static String getRequiredParameter( ServletRequest req, String name )
         throws NullPointerException
     {
         String  ret  = req.getParameter( name );
 
         if ( ret == null )
         {
             throw new NullPointerException( "This form requires a \"" + name + "\" parameter, which was missing from the submitted request." );
         }
         return ret;
     }
 
 
     /**
      *  Retrieves a list of parameters from a passed request in comma delimited
      *  format. If the parameter is not found, an NPE is thrown.
      *
      * @param  req                       Request object to look in
      * @param  name                      the name of the field from the html
      *      form
      * @return                           the value from the html form
      *      corresponding to the name parameter
      * @exception  NullPointerException  thrown if the name was not in the html
      *      form
      */
     public static String getRequiredParameterValues( ServletRequest req, String name )
         throws NullPointerException
     {
         String[]      values  = req.getParameterValues( name );
 
         if ( values == null )
         {
             throw new NullPointerException( "This form requires a \"" + name + "\" parameter, which was missing from the submitted request." );
         }
 
         StringBuffer  ret     = new StringBuffer();
 
         for ( int i = 0; i < values.length; i++ )
         {
             if ( i > 0 )
             {
                 ret.append( "," );
             }
             ret.append( values[i] );
         }
         return ret.toString();
     }
 
 
     /**
      *  Gets the optionalParameter attribute of the WebUtils class
      *
      * @param  req                       The servlet request object to pull it
      *      out of.
      * @param  name                      Name of the Parameter
      * @param  defalt                    The default value (yes, defalt is
      *      spelled incorrectly - default is a keyword
      * @return                           The optionalParameter value
      * @exception  NullPointerException  Description of the Exception
      */
     public static String getOptionalParameter( ServletRequest req, String name, String defalt )
         throws NullPointerException
     {
         String  ret  = req.getParameter( name );
 
         if ( ret == null )
         {
             ret = defalt;
         }
         return ret;
     }
 
   /**
      *  Retrieves a list of parameters from a passed request in comma delimited
      *  format. If the parameter is not found, an NPE is thrown.
      *
      * @param  req                       Request object to look in
      * @param  name                      the name of the field from the html
      *      form
      * @return                           the value from the html form
      *      corresponding to the name parameter
      * @exception  NullPointerException  thrown if the name was not in the html
      *      form
      */
     public static String getOptionalParameterValues( ServletRequest req, String name )
         throws NullPointerException
     {
         String[] values = req.getParameterValues( name );
 
         if ( values == null )
         {
 		return "";
 	}
 
         StringBuffer  ret     = new StringBuffer();
 
         for ( int i = 0; i < values.length; i++ )
         {
             if ( i > 0 )
             {
                 ret.append( "," );
             }
             ret.append( values[i] );
         }
         return ret.toString();
     }
 
     /**
      *  Gets the optionalParameter attribute of the WebUtils class
      *
      * @param  req                       Description of the Parameter
      * @param  name                      Description of the Parameter
      * @return                           The optionalParameter value
      * @exception  NullPointerException  Description of the Exception
      */
     public static String getOptionalParameter( ServletRequest req, String name )
         throws NullPointerException
     {
         return getOptionalParameter( req, name, "" );
     }
 
 
     /**
      *  Gets the requiredAttribute attribute of the WebUtils class
      *
      * @param  page   Description of the Parameter
      * @param  name   Description of the Parameter
      * @param  scope  Description of the Parameter
      * @return        The requiredAttribute value
      */
     public static String getRequiredAttribute( PageContext page, String name, int scope )
     {
         Object  ret  = page.getAttribute( name, scope );
 
         if ( ret == null )
         {
             throw new NullPointerException( "This form requires a \"" + name + "\" parameter, which was missing from the submitted request." );
         }
         return ret.toString();
     }
 
 
     /**
      *  Gets the optionalAttribute attribute of the WebUtils class
      *
      * @param  page   Description of the Parameter
      * @param  name   Description of the Parameter
      * @param  scope  Description of the Parameter
      * @return        The optionalAttribute value
      */
     public static String getOptionalAttribute( PageContext page, String name, int scope )
     {
         Object  ret  = page.getAttribute( name, scope );
 
         if ( ret == null )
         {
             return "";
         }
         return ret.toString();
     }
 
 
     public  static List<FileItem> getFileItems( HttpServletRequest request) throws FileUploadException 
 	{
     	/*
 		 *  parse the request
 		 */
     	FileItemFactory factory = new DiskFileItemFactory();
 		System.out.println("WebUtils uploadFile : got factory ");
 		
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		System.out.println("WebUtils uploadFile : got upload object ");
 		
 		List<FileItem> items;
 	
 		items = upload.parseRequest(request);
 		return items;
 	}
     
     public  static List<ContinuityOfCareRecord> translateFiles( List<FileItem> items, String pathName) 
 	{
 	
     	try {
     		ArrayList<ContinuityOfCareRecord> ccrs = new ArrayList<ContinuityOfCareRecord>();
 			System.out.println("WebUtils uploadFile : got items " + items);			
 			boolean writeToFile = true;
 			System.out.println("WebUtils uploadFile : got items " + items.size());
 			
 			for (FileItem fileSetItem: items)
 			{
 				String itemName = fileSetItem.getFieldName();
 				System.out.println("WebUtils: first file item  " + itemName);
 				
 				String fileName ="";
 				if (!fileSetItem.isFormField()) {
 				    String fieldName = fileSetItem.getFieldName();
 				    fileName = fileSetItem.getName();
 				    
 				    String contentType = fileSetItem.getContentType();
 				    boolean isInMemory = fileSetItem.isInMemory();
 				    long sizeInBytes = fileSetItem.getSize();
 	
 				    ContinuityOfCareRecord ccr = translate(fileSetItem.getString());
 				    String patientId = getPatientId(ccr);
 		        	
 				    uploadFile(fileSetItem, pathName, fileName, patientId);
 				    if (ccr != null)
 				    	ccrs.add(ccr);
 									
 				    
 					
 			    }
 		
 			}
 			return ccrs;
 			
     	} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}	
 
     public static boolean uploadFile(FileItem uploadFileItem, String pathName, String fileName, String patientId) throws Exception
     {
     	File ccrFile =  createFile(pathName,  fileName, patientId);
 		uploadFileItem.write(ccrFile);
 		
 		return true;
     }
     
     public static boolean uploadFileFromCCR(ContinuityOfCareRecord ccr, String pathName, String fileName,String patientId) throws JAXBException, IOException
     {
     	
 		File ccrFile =  createFile(pathName,  fileName, patientId);
     	FileWriter fileWrite = new FileWriter(ccrFile);
 		//This won't work, need to have way to translate to string
     	fileWrite.write(ccr.toString());
 		fileWrite.flush();
 		fileWrite.close();
 		
 		return true;
     }
     
     public static boolean uploadFileFromString(String ccr, String pathName, String fileName, String patientId) throws JAXBException, IOException
     {
     	File ccrFile =  createFile( pathName,  fileName, patientId);
     	FileWriter fileWrite = new FileWriter(ccrFile);
 		fileWrite.write(ccr);
 		fileWrite.flush();
 		fileWrite.close();
 		return true;
     }
     
     public static File createFile( String pathName, String fileName, String patientId) throws JAXBException, IOException
     {
    	patientId = patientId.replace(" ", "");
		
     	File path = new File(pathName + "/" +CCR_DIR + "/" + patientId ); 
 		if (!path.exists())
 		{
 			boolean status = path.mkdirs();
 			if (!status) return null;
 		}
 		File uploadedFile = new File(path + "/" + patientId + ".xml");
 		return uploadedFile;
 		
     }
     
     private static ContinuityOfCareRecord loadCCRFromFile(String fileName) throws FileNotFoundException, JAXBException
     {
     	 JAXBContext jc = JAXBContext.newInstance("org.mitre.medj.jaxb");
          Unmarshaller u = jc.createUnmarshaller();
          ContinuityOfCareRecord ccr = (ContinuityOfCareRecord)u.unmarshal(new StreamSource( new FileReader(fileName)  ) );
          return ccr;
     }
 	
     
     public static ContinuityOfCareRecord convert(HttpServletRequest req, String pathName)
 		 throws  FileUploadException, IOException, Exception
 	{
 		if ( !ServletFileUpload.isMultipartContent(req) )
 		{
 			String sourceXml = WebUtils.getRequiredParameter( req, "source_xml" );
 	         
 		    return translate(sourceXml);
 		}
 		else
 		{
 			System.out.println("WebUtils convert : this is a multipart doc");
 			List<FileItem> items = getFileItems(req);
 			List<ContinuityOfCareRecord> ccrs = translateFiles(items, pathName);
 			
 			if (ccrs != null)
 			{
 				if (ccrs.size() > 0)
 					return ccrs.get(0);
 			}
 				
 			return null;
 			/*if (!success)
 				return "Error in File Upload";
 			else
 				return "File uploaded successfully";*/
 		}
 	
 	}	
 
 
 	public  static ContinuityOfCareRecord translate(String sourceXml) throws JAXBException
 	{
 	
        JAXBContext jc = JAXBContext.newInstance("org.mitre.medj.jaxb");
        Unmarshaller u = jc.createUnmarshaller();
        // URL url = new URL( "simple.ccr.xml" );
        // URLConnection conn = url.openConnection();
        ContinuityOfCareRecord p = (ContinuityOfCareRecord)u.unmarshal(new StreamSource( new StringReader( sourceXml ) ) );
        return p;
       
    
 	}	
 
 	public static String getPatientId(ContinuityOfCareRecord ccr)
 	{
 		String patientId = "";
 		patientId = ccr.getPatient().get(0).getActorID();
 		System.out.println("WebUtils getPatientId : " + patientId);
 		
 		return patientId;
 	}
 	public static JSONObject buildErrorJson(String errorMsg)
     {
         JSONObject ret = new JSONObject();
         try
         {
             JSONObject error = new JSONObject();
             error.put("message", errorMsg);
             error.put("type", "error");
             ret.put("announce", error);
         }
         catch (JSONException e)
         {
             return null;
         }
         return ret;
     }
 	
 	public static ContinuityOfCareRecord loadCCR(String patientId) throws FileNotFoundException, JAXBException
 	{
		patientId = patientId.replace(" ", "");
		
 		return WebUtils.loadCCRFromFile(WebUtils.BASE_DIR  + "/" + CCR_DIR+ "/" + patientId + "/" + patientId + ".xml");
 	}
 	
 	public static ArrayList<String> listPatients(File dir)
 	{
 		  
 		ArrayList<String> patientFiles = new ArrayList<String>();
 		
 		FileFilter fileFilter = new FileFilter() {
 			public boolean accept(File file) 
 			{
 	   	        return file.isDirectory();
 	   	    }
 	   	};
    	
 	   	FilenameFilter filter = new FilenameFilter() {
 	   	    public boolean accept(File dir, String name) {
 	   	        return name.endsWith("xml");
 	   	      
 	   	    }
 	   	};
 	   	
 	   	File[] subDirs = dir.listFiles(fileFilter);
 
 		System.out.println("TextProcesses getCSSFiles no of files " + subDirs.length);
 	   	if (subDirs == null) {
 	   	    // Either dir does not exist or is not a directory
 	   	} 
 	   	else 
 	   	{
    	    
 	   		for (File subDir: subDirs)
 	   	    {
 	   			System.out.println("WebUtils getPatients subDir " + subDir.getName());
 	   		   	
 	   	        // Get filename of file or directory
 	   	    	String[] subFiles = subDir.list(filter);
 	   	    	System.out.println("WebUtils getPatients files # " + subFiles.length);
 	   		   	
 	   	    	for (String subFile: subFiles)
 	   	    	{
 	   	    		System.out.println("WebUtils getPatients files  " + subFile);
 	   	    		String patientId = subFile.substring(0, subFile.lastIndexOf("."));
 	   	    		patientFiles.add(patientId);
 	   	    	}
 	   	    	
 	   	       
 	   	    }
 	   	}
 	   	return patientFiles;
 	}
 }
 
