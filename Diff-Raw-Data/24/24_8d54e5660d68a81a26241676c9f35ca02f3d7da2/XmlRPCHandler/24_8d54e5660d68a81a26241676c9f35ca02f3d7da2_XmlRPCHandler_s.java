 package com.imdevice.pipe2wp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class XmlRPCHandler {
 	String blogid;
 	String xmlrpcurl;
 	String username;
 	String password;
 	String methodname;
 	
 	public XmlRPCHandler(){
 		//xmlrpcurl="http://imdevice.com/wordpress/xmlrpc.php";
 		xmlrpcurl="http://imdevice.com/xmlrpc.php";
 		//xmlrpcurl="http://42.121.1.72/xmlrpc.php";
 		blogid="1";
 		username="lcw";
 		password="Hncwx123";
 		methodname="metaWeblog.newPost";
 	}
 	public static void main(String[] args) {
 		Post post=new Post();
 		post.setTitle("Test callRpc中文");
 		post.setMt_excerpt("Test callRpc excerpt中文");
 		post.setDescription("Test callRpc description CONTENT<br><p style=\"color:red\">中文</p>");
 		post.setDate_created_gmt("20121021T10:00:00");
 		try {
 			new XmlRPCHandler().callRpc(post);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void callRpc(Post post) throws Exception {
 		StringBuffer msg=new StringBuffer();
 		msg.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
 		msg.append("<methodCall>");
 		msg.append("<methodName>"+methodname+"</methodName>");
 		msg.append("<params>");
 		msg.append("<param><value><int>"+blogid+"</int></value></param>");//blog id
 		msg.append("<param><value><string>"+username+"</string></value></param>");//username
 		msg.append("<param><value><string>"+password+"</string></value></param>");//password
 		msg.append("<param><value><struct>");
 		msg.append(post.toXML());
 		/***
 		 * left for test only
 		msg.append("<member>");
 		msg.append("<name>title</name>");
 		msg.append("<value><string>Test callRpc中</string></value>");
 		msg.append("</member>");
 		msg.append("<member>");
 		msg.append("<name>mt_excerpt</name>");
 		msg.append("<value><string><![CDATA[Test callRpc excerpt中文]]></string></value>");
 		msg.append("</member>");
 		msg.append("<member>");
 		msg.append("<name>description</name>");
 		msg.append("<value><string><![CDATA[Test callRpc description CONTENT<br><p style=\"color:red\">中文</p>]]></string></value>");
 		msg.append("</member>");
 		msg.append("<member>");
 		msg.append("<name>date_created_gmt</name>");//<dateTime.iso8601>20110414T09:45:00Z</dateTime.iso8601>
 		msg.append("<value><dateTime.iso8601>20110414T10:05:00</dateTime.iso8601></value>");
 		msg.append("</member>");
 		*/
 		msg.append("</struct></value></param>");
 		msg.append("<param><value><boolean>1</boolean></value></param>");
 		msg.append("</params>");
 		msg.append("</methodCall>");
 //System.out.println(msg.toString());
 //System.setProperty("proxyHost", "proxy.nbbyd.com");  
 //System.setProperty("proxyPort", "3128");  
 			URL url = new URL(xmlrpcurl);
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\""); 
 			connection.setRequestMethod("POST");
 			// Password handling, in my case MyPassword consisted of the string
 			// base64(user:password)
 			/*connection.setRequestProperty("Authorization",
 					"Basic " + Base64.encode("lcw601474:123qwer`12345".getBytes()));*/
 			
 
 			// XMLEncoder x=new
 			// XMLEncoder(url.openConnection().getOutputStream());
 			// x.writeObject("x");
 
 			OutputStreamWriter writer = new OutputStreamWriter(
 					connection.getOutputStream());
 			//String message = URLEncoder.encode(msg.toString(), "UTF-8");
 			writer.write(msg.toString());
 			writer.close();
 			InputStream in=connection.getInputStream();
 			InputSource is = new InputSource(in);
 			try{
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			DefaultHandler myHandler=new DefaultHandler(){
 				StringBuffer s;
 				@Override
 				public void startDocument() throws SAXException {
 					s=new StringBuffer();
 				}
 				@Override
 				public void endElement(String uri, String localName, String qName) throws SAXException { 
 						if(qName.equalsIgnoreCase("string")){
 								System.out.println(s.toString().trim());
 							}
 						s.setLength(0);
 					} 
 				public void characters(char [] buf, int offset, int len) { 
 						s.append(buf,offset,len); 
 					} 
 			};
 			parser.parse(is, myHandler);
 
 			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 				System.out.println("OK");// OK
 			} else {
 				System.out.println("error code"+connection.getResponseMessage());// Server returned HTTP error code.
 			}
 		} catch (MalformedURLException e) {
 			// ...
 		} catch (UnsupportedEncodingException e1) {
 			// ...
 		} catch (IOException e) {
 			// ...
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			in.close();
 		}
 	}
 	
 	public  String  test() {
 		Post post=new Post();
 		post.setTitle("Test callRpc中文");
 		post.setMt_excerpt("Test callRpc excerpt中文");
 		post.setDescription("Test callRpc description CONTENT<br><p style=\"color:red\">中文</p>");
 		post.setDate_created_gmt("20110414T10:05:00");
 		String[] charsets={"UTF-8","GBK","ISO8859-1","ANSI_X3.4-1968"};
 		StringBuffer s=new StringBuffer();
 		try {
 			callRpc1(post);
 			String title=post.title;
 			for(String charset :charsets){
 				
 				post.setTitle(new String((title+":"+charset).getBytes(),charset));
 				callRpc1(post);
 				s.append(post.title);
 				s.append("<hr>");
 				
 				for(String charset1 :charsets){
 					s.append(charset+":"+charset1+"<br>");
 					post.setTitle(new String((title+":"+charset+"-"+charset1).getBytes(charset),charset1));
 					callRpc1(post);
 					s.append(post.title);
 					s.append("<hr>");
 				}
 			}
 			//return s.toString();
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return s.toString();
 	}
 	public void callRpc1(Post post) throws Exception {
 		StringBuffer msg=new StringBuffer();
 		msg.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
 		msg.append("<methodCall>");
 		msg.append("<methodName>"+methodname+"</methodName>");
 		msg.append("<params>");
 		msg.append("<param><value><int>"+blogid+"</int></value></param>");//blog id
 		msg.append("<param><value><string>"+username+"</string></value></param>");//username
 		msg.append("<param><value><string>"+password+"</string></value></param>");//password
 		msg.append("<param><value><struct>");
 		msg.append(post.toXML());
 		msg.append("</struct></value></param>");
 		msg.append("<param><value><boolean>1</boolean></value></param>");
 		msg.append("</params>");
 		msg.append("</methodCall>");
 //System.out.println(msg.toString());
 //System.setProperty("proxyHost", "proxy.nbbyd.com");  
 //System.setProperty("proxyPort", "3128");  
 		URL url = new URL(xmlrpcurl);
 			//URL url = new URL("http://imdevice.com/wordpress/xmlrpc.php");
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\""); 
 			connection.setRequestMethod("POST");
 
 			OutputStreamWriter writer = new OutputStreamWriter(
 					connection.getOutputStream(),"UTF-8");//VERY IMPORTANT!!!,SET the CHARSET
 			//String message = URLEncoder.encode(msg.toString(), "UTF-8");
 			//writer.write(message);
 			writer.write(msg.toString());
 			writer.close();
			/*
			 *** 反馈时间太长，不等了
 			InputStream in=connection.getInputStream();
 			InputSource is = new InputSource(in);
 			try{
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			DefaultHandler myHandler=new DefaultHandler(){
 					StringBuffer s;
 					@Override
 					public void startDocument() throws SAXException {
 						s=new StringBuffer();
 					}
 					@Override
 					public void endElement(String uri, String localName, String qName) throws SAXException { 
 							if(qName.equalsIgnoreCase("string")){
 									System.out.println(s.toString().trim());
 								}
 							s.setLength(0);
 						} 
 					public void characters(char [] buf, int offset, int len) { 
 							s.append(buf,offset,len); 
 						} 
 				};
 				parser.parse(is, myHandler);
 	
 				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 					System.out.println("OK");// OK
 				} else {
 					System.out.println("error code"+connection.getResponseMessage());// Server returned HTTP error code.
 				}
 			} catch (MalformedURLException e) {
 				// ...
 			} catch (UnsupportedEncodingException e1) {
 				// ...
 			} catch (IOException e) {
 				// ...
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}finally{
 				in.close();
 			}
			*/
 	}
 
 }
