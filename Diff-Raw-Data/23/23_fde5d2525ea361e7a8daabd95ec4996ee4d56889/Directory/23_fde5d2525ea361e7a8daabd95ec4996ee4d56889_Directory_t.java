 package http.response.routeType;
 
 import http.response.code.TwoHundred;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class Directory implements RouteType {
   File publicDirectoryFullPath;
 
   public Directory(File publicDirectoryFullPath) {
     this.publicDirectoryFullPath = publicDirectoryFullPath;
   }
 
   public byte[] get(File routeFile, HashMap request) throws IOException {
     HashMap modifiedRequest = addDirectoryToQueryString(request);
     TwoHundred twoHundred = new TwoHundred();
     return twoHundred.build(routeFile, modifiedRequest);
   }
 
   public HashMap addDirectoryToQueryString(HashMap request) {
     String folderName = (String)request.get("url");
     String fileList = createFileList(new File(publicDirectoryFullPath, folderName), request);
     request.put("queryString", "folder_name=" + folderName + "&file_list=" + fileList);
     return request;
   }
 
   public String createFileList(File directory, HashMap request) {
     File[] fileList = directory.listFiles();
     StringBuilder stringBuilder = new StringBuilder();
 
     for(int i = 0; i < fileList.length; i++) {
       stringBuilder.append("<a href=\"");
       stringBuilder.append(request.get("url"));
      stringBuilder.append("/" + fileList[i].getName());
       stringBuilder.append("\">");
      stringBuilder.append("/" + fileList[i].getName());
       stringBuilder.append("</a><br>");
     }
 
     return stringBuilder.toString();
   }
 }
