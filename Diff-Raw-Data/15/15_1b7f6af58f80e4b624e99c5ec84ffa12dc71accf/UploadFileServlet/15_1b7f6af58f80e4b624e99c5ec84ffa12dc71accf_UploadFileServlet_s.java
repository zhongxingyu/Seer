 package by.bsu.fpmi.second.dudkoAA.servlet;
 
 import by.bsu.fpmi.second.dudkoAA.model.File;
 import by.bsu.fpmi.second.dudkoAA.service.FileBC;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 @MultipartConfig
 public class UploadFileServlet extends HttpServlet {
     private FileBC fileBC = new FileBC();
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         List<String> messages = getRequestErrors(request);
         if (messages.size() != 0) {
             request.setAttribute("messages",messages);
             RequestDispatcher dispatcher = request.getRequestDispatcher("/addFile.jsp");
             dispatcher.forward(request, response);
         }
 
         Collection<Part> parts = request.getParts();
 
         String fileName = getFileName((Part)parts.toArray()[0]);
         FileWriter fileWriter = new FileWriter(fileName);
         BufferedWriter writer = new BufferedWriter(fileWriter);
 
         for (Part part : parts) {
             String partContent = getPartContent(part);
             writer.write(partContent);
         }
         writer.flush();
         writer.close();
 
         fileBC.addFile(makeFile(request,fileName));
 
         response.getOutputStream().println("<html><body>The file has been uploaded</body></html>");
     }
 
     private String readAsString(InputStream stream) throws IOException {
         String result = "";
         InputStreamReader inputReader = new InputStreamReader(stream);
         BufferedReader reader = new BufferedReader(inputReader);
         while (reader.ready()) {
             result += reader.readLine();
         }
 
         return result;
     }
 
     private String getFileName(Part part) {
         String[] contentAttributes = part.getHeader("content-disposition").split(";");
         for (String attribute : contentAttributes) {
             if (attribute.trim().startsWith("filename")) {
                 String value = attribute.substring(attribute.indexOf('=') + 1).trim();
                 return value.replace("\"", "");
             }
         }
 
         return null;
     }
 
     private String getPartContent(Part part) throws IOException {
         String contentType = part.getContentType();
         System.out.println("contentType = " + contentType);
         System.out.println("file = " + getFileName(part));
         InputStream stream = part.getInputStream();
         String content = readAsString(stream);
         return content;
     }
 
     private File makeFile(HttpServletRequest request, String fileName) {
         File file = new File();
        file.setDescription(request.getParameter("description"));
        file.setAuthor(request.getParameter("author"));
        file.setNotes(request.getParameter("notes"));
         file.setFileName(fileName);
         return file;
     }
 
     private List<String> getRequestErrors(HttpServletRequest request) {
         List<String> errors = new ArrayList<>();
        if ("".equals(request.getParameter("description"))) {
             errors.add("Description field should not be empty");
         }
        if ("".equals(request.getParameter("author"))) {
             errors.add("Author field should not be empty");
         }
        if ("".equals(request.getParameter("notes"))) {
             errors.add("Notes field should not be empty");
         }
         if ("".equals(request.getParameter("file"))) {
             errors.add("File should be chosen");
         }
         return errors;
     }
 }
