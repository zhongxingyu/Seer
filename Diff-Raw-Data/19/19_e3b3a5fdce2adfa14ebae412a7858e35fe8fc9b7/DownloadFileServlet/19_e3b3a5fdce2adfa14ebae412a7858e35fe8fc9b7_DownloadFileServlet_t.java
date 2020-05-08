 package by.bsu.fpmi.second.dudkoAA.servlet;
 
 
 
 import by.bsu.fpmi.second.dudkoAA.model.File;
 import by.bsu.fpmi.second.dudkoAA.service.FileBC;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 
 public class DownloadFileServlet extends HttpServlet {
 
     private final static int BUFFER_SIZE = 2048;
 
     private String filePath;
 
     private FileBC fileBC = new FileBC();
 
     public void init() {
         filePath = "C:" + java.io.File.separator + "Temp";
     }
 
     @Override
     protected void doGet(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
         File fileFromDB = getFileFromDB(request.getRequestURI());
         if (fileFromDB == null) {
            wrongURIForward(request, response, "No such file in DB");
         }
         else {
             if ("remove".equals(request.getParameter("op"))) {
                 removeFile(request, response, fileFromDB);
             }
             else {
                createFile(response, fileFromDB);
             }
         }
     }
 
    private void createFile(HttpServletResponse response, File fileFromDB) throws IOException {
         java.io.File file = new java.io.File(filePath + java.io.File.separator + fileFromDB.getFileName());
         updateResponseParams(response, file);
         downloadFile(response, file);
     }
 
     private void removeFile(HttpServletRequest request, HttpServletResponse response,
                             File fileFromDB) throws IOException, ServletException {
         String currentFileRemoveCode = fileFromDB.getRemoveCode().toString();
         String requestRemoveCode = request.getParameter("removecode");
         if (currentFileRemoveCode.equals(requestRemoveCode)) {
             fileBC.removeFile(fileFromDB);
             response.getOutputStream().println("<html><body>The file has been removed</body></html>");
         }
         else {
            wrongURIForward(request,response,"Wrong remove code");
         }
     }
 
     private void wrongURIForward(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String errorMessage) throws ServletException, IOException {
        request.setAttribute("message", errorMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
         dispatcher.forward(request, response);
     }
 
     private void downloadFile(HttpServletResponse response, java.io.File file) throws IOException {
         DataInputStream in = new DataInputStream(new FileInputStream(file));
         ServletOutputStream outStream = response.getOutputStream();
         downloadFileContent(in, outStream);
         in.close();
         outStream.close();
     }
 
     private File getFileFromDB(String requestURI) {
         String[] URIParts = requestURI.split("/");
         int i = 0;
         while (!URIParts[i].matches("id[0-9]*") && ++i < URIParts.length) {
 
         }
         if (i == URIParts.length) {
             return null;
         }
         StringBuilder IDPart = new StringBuilder(URIParts[i]);
         int fileID = Integer.parseInt(IDPart.substring(2, IDPart.length()));
         return fileBC.getFile(fileID);
     }
 
     private void updateResponseParams(HttpServletResponse response, java.io.File file) {
         response.setContentType(getMimeType());
         response.setContentLength((int)file.length());
         String fileName = file.getName();
         response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
     }
 
     private String getMimeType() {
         String mimeType = getServletConfig().getServletContext().getMimeType(filePath);
         if (mimeType == null) {
             mimeType = "application/octet-stream";
         }
         return mimeType;
     }
 
     private void downloadFileContent(DataInputStream inputStream, ServletOutputStream outputStream) throws IOException {
         byte[] byteBuffer = new byte[BUFFER_SIZE];
         int symbolsRead;
         while ((inputStream != null) && ((symbolsRead = inputStream.read(byteBuffer)) != -1))
         {
             outputStream.write(byteBuffer, 0, symbolsRead);
         }
         outputStream.flush();
     }
 }
