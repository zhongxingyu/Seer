 package org.ijsberg.iglu.util.http;
 
 import org.ijsberg.iglu.logging.LogEntry;
 import org.ijsberg.iglu.util.io.FileData;
 import org.ijsberg.iglu.util.io.FileSupport;
 
 import javax.servlet.ServletInputStream;
 import javax.servlet.ServletRequest;
 import java.io.*;
 
 /**
  */
 public class MultiPartReader {
 
 	//TODO cancel upload
 
 
 	public static final int BUFFER_SIZE = 1024;
 
 	private ServletRequest request;
 	private String uploadDir;
 	private ServletInputStream input;
 
 
 	private byte[] line = new byte[BUFFER_SIZE];
 	private int bytesReadAsLine;
 
 	private String propertyName = null;
 	private String fullFileName = null;
 	private String contentType = null;
 
 
 	private long contentLength = 0;
 	private long bytesRead = 0;
 
 	private OutputStream partialCopyOutputStream;
 	private File uploadFile;
 
 
 	/**
 	 *
 	 * @param request
 	 * @param uploadDir path to the directory where to store read files
 	 */
 	public MultiPartReader(ServletRequest request, String uploadDir) {
 		this.request = request;
 		this.uploadDir = uploadDir;
 		contentLength = request.getContentLength();
 	}
 
 	public long getBytesRead() {
 		return bytesRead;
 	}
 
 	/**
 	 * Reads uploaded files and form variables from POSTed data.
 	 * Files are stored on disk in case uploadDir is not null.
 	 * Otherwise files are stored as attributes on the http-request in the form of FileObjects, using the name of the html-form-variable as key.
 	 * Plain properties are stored as String attributes on the http-request.
 	 *
 	 * In case a file to be stored on disk already exists, the existing file is preserved
 	 * and renamed by adding a sequence number to its name.
 	 *
 	 * @return total nr of bytes read
 	 * @throws IOException
 	 * @see org.ijsberg.iglu.util.io.FileData
 	 */
 	public long readMultipartUpload() throws IOException
 	{
 		input = request.getInputStream();
 		//the next variable stores all post data and is useful for debug purposes
 
 
 		bytesReadAsLine = input.readLine(line, 0, BUFFER_SIZE);
 		if (bytesReadAsLine <= 2)
 		{
 			return 0;
 		}
 
 		bytesRead = bytesReadAsLine;
 
 
 		//save the multipart boundary string
 		String boundary = new String(line, 0, bytesReadAsLine - 2);
 
 		while ((bytesReadAsLine = input.readLine(line, 0, BUFFER_SIZE)) != -1)
 		{
 
 			readPropertiesUntilEmptyLine();
 
 			bytesRead += bytesReadAsLine;
 			bytesReadAsLine = input.readLine(line, 0, BUFFER_SIZE);
 
 			//before the boundary that indicates the end of the file
 			//there is a line separator which does not belong to the file
 			//it's necessary to filter it out without too much memory overhead
 			//it's not clear where the line separator comes from.
 			//this has to work on Windows and UNIX
 
 			partialCopyOutputStream = getStorageOutputStream();
 
 			readUploadedFile(boundary, partialCopyOutputStream);
 			bytesRead += bytesReadAsLine;
 
 			if (fullFileName != null)
 			{
 				if (uploadDir == null)
 				{
 					FileData file = new FileData(fullFileName, contentType);
 					file.setDescription("Obtained from: " + fullFileName);
 					file.setRawData(((ByteArrayOutputStream) partialCopyOutputStream).toByteArray());
 					//propertyName is specified in HTML form like <INPUT TYPE="FILE" NAME="myPropertyName">
 					request.setAttribute(propertyName, file);
 					System.out.println(new LogEntry("file found in multi-part data: " + file));
 				}
 			}
 			else
 			{
 				String propertyValue = partialCopyOutputStream.toString();
 				request.setAttribute(propertyName, propertyValue);
 				System.out.println(new LogEntry("property found in multi-part data:"  + propertyName + '=' + propertyValue));
 			}
 			partialCopyOutputStream.close();
 
 			fullFileName = null;
 		}
 		System.out.println(new LogEntry("post data retrieved from multi-part data: " + bytesRead + " bytes"));
 
 		return bytesRead;
 	}
 
 	private void readUploadedFile(String boundary, OutputStream partialCopyOutputStream) throws IOException {
 		byte[] line2 = new byte[BUFFER_SIZE];
 		int len2 = 0;
 
 		byte[] line3 = new byte[BUFFER_SIZE];
 		int len3 = 0;
 
 
 		LOOP:
 		for (; bytesReadAsLine != -1; bytesReadAsLine = input.readLine(line, 0, BUFFER_SIZE))
 		{
 			String newLine = new String(line, 0, bytesReadAsLine);
 
 			if (newLine.startsWith(boundary))
 			{
 				if ((len2 == 1) && (len3 > 0))//must be a \n
 				{
 					partialCopyOutputStream.write(line3, 0, len3 - 1);//get rid of the \r
 					bytesRead += len3;
 				}
 				else if (len2 >= 2)//\n found
 				{
 					partialCopyOutputStream.write(line3, 0, len3);
 					partialCopyOutputStream.write(line2, 0, len2 - 2);//get rid of the \n
 					bytesRead += len3 + len2;
 				}
 				break LOOP;
 			}
 			partialCopyOutputStream.write(line3, 0, len3);
 			bytesRead += len3;
 			len3 = len2;
 			line3 = line2;
 			len2 = bytesReadAsLine;
 			line2 = line;
 			line = new byte[BUFFER_SIZE];

        }
 	}
 
 	private OutputStream getStorageOutputStream() throws IOException {
 		OutputStream partialCopyOutputStream = null;
 		if (fullFileName != null && uploadDir != null)
 		{
 			//check if file exists
 			FileData file = new FileData(fullFileName, contentType);
 			uploadFile = new File(uploadDir + '/' + file.getFileName());
 			if(uploadFile.exists())
 			{
 				uploadFile = getFileWithUniqueName(file, uploadFile);
 			}
 			uploadFile = FileSupport.createFile(uploadFile.getPath());
 			partialCopyOutputStream = new FileOutputStream(uploadFile);
 		}
 		else
 		{
 			partialCopyOutputStream = new ByteArrayOutputStream();
 		}
 		return partialCopyOutputStream;
 	}
 
 	private File getFileWithUniqueName(FileData file, File uploadFile) {
 		int i = 0;
 		File existingUploadFile = uploadFile;
 		while (uploadFile.exists())
 		{
 			uploadFile = new File(uploadDir + '/' + file.getFileNameWithoutExtension() + '_' + i++ + '.' + file.getExtension());
 		}
 		existingUploadFile.renameTo(uploadFile);
 		uploadFile = new File(uploadDir + '/' + file.getFileName());
 		return uploadFile;
 	}
 
 	private void readPropertiesUntilEmptyLine() throws IOException {
 		for (; bytesReadAsLine > 2; bytesReadAsLine = input.readLine(line, 0, BUFFER_SIZE))
 		{
 			bytesRead += bytesReadAsLine;
 			String newLine = new String(line, 0, bytesReadAsLine);
 			if (newLine.startsWith("Content-Disposition: form-data;"))
 			{
 				propertyName = newLine.substring(newLine.indexOf("name=") + 6, newLine.indexOf('\"', newLine.indexOf("name=") + 6));
 				if (newLine.indexOf("filename") != -1)
 				{
 					fullFileName = newLine.substring(newLine.indexOf("filename=") + 10, newLine.indexOf('\"', newLine.indexOf("filename=") + 10));
 				}
 			}
 			if (newLine.startsWith("Content-Type: "))
 			{
 				contentType = newLine.substring(newLine.indexOf("Content-Type: ") + 14, newLine.length() - 2);
 			}
 		}
 	}
 
 
 	public long getContentLength() {
 		return contentLength;
 	}
 
 	public void cancel() {
 
 		if(partialCopyOutputStream != null) {
 			try {
 				partialCopyOutputStream.close();
 			} catch (IOException e) {
 				//
 			}
 			if(uploadFile != null) {
 				uploadFile.delete();
 			}
 			bytesRead = 0;
 			contentLength = 0;
 		}
 	}
 }
