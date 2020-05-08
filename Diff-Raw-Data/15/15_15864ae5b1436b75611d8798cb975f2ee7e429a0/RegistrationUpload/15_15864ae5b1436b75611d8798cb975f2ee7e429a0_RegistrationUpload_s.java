 /*******************************************************************************
  *Copyright (c) 2013 IBM Corporation and others.
  *All rights reserved. This program and the accompanying materials
  *are made available under the terms of the Eclipse Public License v1.0
  *which accompanies this distribution, and is available at
  *http://www.eclipse.org/legal/epl-v10.html
  *
  *Contributors:
  *    IBM Corporation - initial API and implementation
  *******************************************************************************/
 package servlets;
 
 import java.io.DataInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class RegistrationUpload
  */
 @WebServlet("/RegistrationUpload")
 public class RegistrationUpload extends BaseServlet {
 
 	@Override
 	public void executeCustomCode(HttpServletRequest request,
 			HttpServletResponse response) {
 		String contentType = request.getContentType();
		String pathPrefix="uploadedFiles/";
 		
 		
 		PrintWriter out=null;
 		if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
 	 		try {
 	 			out = response.getWriter();
 				DataInputStream in = new DataInputStream(request.getInputStream());
 				
 				//we are taking the length of Content type data
 				int formDataLength = request.getContentLength();
 				byte dataBytes[] = new byte[formDataLength];
 				int byteRead = 0;
 				int totalBytesRead = 0;
 				
 				
 				//this loop converting the uploaded file into byte code
 				while (totalBytesRead < formDataLength) {
 					byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
 					totalBytesRead += byteRead;
 					}
 
 				String file = new String(dataBytes);
 				//for saving the file name
 				String saveFile = file.substring(file.indexOf("filename=\"") + 10);
 				
 				//out.print("The save file: "+saveFile+"\n");
 				
 				saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
 				saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1,saveFile.indexOf("\""));
 				int lastIndex = contentType.lastIndexOf("=");
 				String boundary = contentType.substring(lastIndex + 1,contentType.length());
 				int pos;
 				//extracting the index of file 
 				pos = file.indexOf("filename=\"");
 				pos = file.indexOf("\n", pos) + 1;
 				pos = file.indexOf("\n", pos) + 1;
 				pos = file.indexOf("\n", pos) + 1;
 				
 				//out.write("The file: "+file+" *.*\n");
 				
 				int boundaryLocation = file.indexOf(boundary, pos) - 4;
 				int startPos = ((file.substring(0, pos)).getBytes()).length;
 				int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;
 
 				// creating a new file with the same name and writing the content in new file
 				FileOutputStream fileOut = new FileOutputStream(pathPrefix+saveFile);
 				fileOut.write(dataBytes, startPos, (endPos - startPos));
 				
 				fileOut.flush();
 				fileOut.close();
 				
 				out.print(saveFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}//End of method
 
 }
