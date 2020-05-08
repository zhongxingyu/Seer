 package http.response.routeType;
 
 import org.junit.Test;
 
 import java.io.*;
 import java.util.Date;
 import java.util.HashMap;
 
 import static junit.framework.Assert.assertEquals;
 
 public class DirectoryTest {
   private String NEW_LINE = "\r\n";
 
   @Test
   public void build() throws IOException {
     String workingDirectory = System.getProperty("user.dir");
     File publicDirectoryFullPath = new File(workingDirectory, "test/public/");
 
     HashMap request = new HashMap();
     request.put("httpMethod", "GET");
     request.put("url", "/images");
     request.put("httpProtocol", "HTTP/1.1");
     request.put("Host", "localhost:5000");
     request.put("Connection", "keep-alive");
     request.put("Content-Length", "15");
     request.put("Cache-Control", "max-age=0");
     request.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
     request.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");
     request.put("Accept-Encoding", "gzip,deflate,sdch");
     request.put("Accept-Language", "en-US,en;q=0.8");
     request.put("Cookie", "textwrapon=false; wysiwyg=textarea");
 
     File routeFile = new File(workingDirectory, "src/http/templates/file_directory.html");
     Directory directory = new Directory(publicDirectoryFullPath);
     byte[] actualResultInBytes = directory.get(routeFile, request);
     String actualResult = new String(actualResultInBytes);
 
     String expectedHeader = "HTTP/1.1 200 OK\r\n"
         + new Date() + "\r\n"
         + "Server: NinjaServer 1.0" + "\r\n"
         + "Content-type: text/html; charset=UTF-8" + "\r\n"
        + "Content-length: 227\r\n";
     String expectedBody   = "<HTML>\n"
                           + "  <HEAD>\n"
                           + "    <TITLE>\n"
                           + "      File Directory\n"
                           + "    </TITLE>\n"
                           + "  </HEAD>\n"
                           + "  <BODY>\n"
                           + "    <H1>/images Folder</H1>\n"
                           + "    <a href=\"/images/404.png\">/404.png</a><br><a href=\"/images/another_404.png\">/another_404.png</a><br>\n"
                           + "  </BODY>\n"
                           + "</HTML>";
     String expectedResult = expectedHeader + NEW_LINE + expectedBody;
 
     assertEquals(expectedResult, actualResult);
   }
 
   public byte[] toBytes(File routeFile) throws IOException {
     InputStream inputStream = new FileInputStream(routeFile);
     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     int chr;
 
     while ((chr = inputStream.read()) != -1)
       outputStream.write(chr);
 
     return outputStream.toByteArray();
   }
 }
