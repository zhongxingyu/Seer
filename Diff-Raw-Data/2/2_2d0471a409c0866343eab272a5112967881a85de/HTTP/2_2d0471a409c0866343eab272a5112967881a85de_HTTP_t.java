 package com.opencsi.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.SocketTimeoutException;
 
 import com.opencsi.entity.Message;
 
 public class HTTP
 {
 	private int contentLength;
 	private String header,chunkedResponse,strBody;
 	private boolean bheader,response;
 	private final byte newLine = System.getProperty("line.separator").getBytes()[0];
 	boolean loop;
 	private int port;
 	private int sizeContent=0; 
 	
 	public HTTP(int portSSL)
 	{
 		contentLength = 0;
 		header = "";
 		bheader = loop = true;
 		port = portSSL;
 		response = false;
 		chunkedResponse = null;
 		strBody = "";
 	}
 	
 	protected void getHeader(String line)
 	{
 		header += line;
 		// Empty query, ignore it:
 		if(line.equals("null\n"))
 		{
 			header = null;
 			loop = false;
 			contentLength = 0;
 			return;
 		}
 		// Content-length: ? [<number>\n] or not: chunked content
 		if (line.split(" ")[0].toLowerCase().equals("content-length:"))
 			contentLength = Integer.parseInt(line.split(" ")[1].substring(0,line.split(" ")[1].length()-1));
 		// Need to check for non Content-Length whereas they are data in the response body...
 		if (line.split(" ")[0].toLowerCase().equals("http/1.1") && line.split(" ")[1].toLowerCase().equals("200"))
 			response = true; 
 		// Get type Code HTTP: [4xx or 5xx]
 		if (line.split(" ")[0].toLowerCase().equals("http/1.1") && !response)
 		{
 			String message = Message.getHTTPError(line.split(" ")[1].toLowerCase());
 			if (message != null){
 				System.out.println(message);
 			}
 		}
 		// End of header:
 		if(header.substring(header.length() - 1).getBytes()[0] == newLine && header.substring(header.length() - 2).getBytes()[0] == newLine)
 		{
 			bheader = false;
 			if (contentLength == 0 && !response)
 				loop = false;
 		}
 	}
 	
 	protected void getBody(BufferedReader in)
 	{
 		if (contentLength <= 8720)
 		{
 			char[] cbuf = new char[contentLength];
 			try {
 				in.read(cbuf);
 				strBody = new String(cbuf);
 				strBody = strBody.replaceAll("https://", "http://");
 				strBody = strBody.replaceAll(":"+String.valueOf(port)+"/", "/");
 				sizeContent = strBody.getBytes().length;
 			} catch (IOException e) {
 			}
 		}
 		else
 		{
 			String line="";
 			sizeContent = 0;
 			try {
 				do
 				{
 					line = in.readLine();
 					sizeContent += line.getBytes().length + 1;// +1: new line.
 					///////////////////////////////
 					line = line.replaceAll("https://", "http://");
 					line = line.replaceAll(":"+String.valueOf(port)+"/", "/");
 					///////////////////////////////
 					strBody += line + "\n";
 				}while(sizeContent < contentLength && !line.equals("</html>"));
 			}catch(SocketTimeoutException e){
 				Message.getSSLError("Timeout");
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		loop = false;
 	}
 	
 	protected void getChunkedBody(BufferedReader in)
 	{
 		String line="";
 		chunkedResponse = "";
 		try {
 			while((line = in.readLine()) != null)
 			{
 				line = line.replaceAll("'https://", " 'http://");
 				line = line.replaceAll("\"https://", " \"http://");
 				chunkedResponse += line + "\n";
 				try{
 					if(line.equals("</html>"))
 					{
						chunkedResponse += "\r\n0\r\n";
 						loop = false;
 						break;
 					}
 				}catch(Exception e){
 					//e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected byte[] setToBytes()
 	{
 		if (header == null)
 			return null;
 		int sizeCBody = 0;
 		byte[] _cbody = new byte[1];
 		String cLength = "";
 		if (chunkedResponse != null)
 		{
 			_cbody = chunkedResponse.getBytes();
 			sizeCBody = _cbody.length;
 		}
 		byte[] response = new byte[header.getBytes().length + sizeContent + sizeCBody];
 		byte[] h = header.getBytes();
 		for(int i=0;i<h.length;i++)
 			response[i] = h[i];
 		////////////////////////////////////////////////////
 		if (chunkedResponse == null)
 		{
 			byte[] body = strBody.getBytes();
 			for(int i=0,j=header.length();i<body.length;i++,j++)
 				response[j] = body[i];
 		}
 		////////////////////////////////////////////////////
 		else
 		{
 			for(int i=h.length+cLength.getBytes().length,j=0;j<sizeCBody;j++,i++)
 				response[i] = _cbody[j];
 		}
 		return response;
 	}
 	
 	public byte[] getResponse(InputStream in)
 	{
 		BufferedReader read = new BufferedReader(new InputStreamReader(in));
 		while(loop)
 		{
 			try {
 				if (bheader)
 					this.getHeader(read.readLine() + "\n");
 				else if (contentLength != 0)
 					this.getBody(read);
 				else if (response && contentLength == 0)
 					this.getChunkedBody(read);
 			} catch (IOException e) {
 				e.getStackTrace();
 				loop = false;
 			}
 		}
 		if (header == null)
 			return null;
 		return this.setToBytes();
 	}
 
 }
