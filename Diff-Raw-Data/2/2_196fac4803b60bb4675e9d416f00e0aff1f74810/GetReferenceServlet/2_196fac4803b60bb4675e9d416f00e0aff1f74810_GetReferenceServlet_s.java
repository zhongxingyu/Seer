 package fi.helsinki.cs.web;
 
 import fi.helsinki.cs.okkopa.database.OkkopaDatabase;
 import fi.helsinki.cs.okkopa.reference.Reference;
 import fi.helsinki.cs.okkopa.shared.Settings;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.glxn.qrgen.QRCode;
 import net.glxn.qrgen.image.ImageType;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.support.SpringBeanAutowiringSupport;
 import org.zeroturnaround.zip.ZipUtil;
 
 public class GetReferenceServlet extends HttpServlet {
 
     private String amount;
     private String size;
     private String letters;
     private Reference reference;
     private PrintWriter writer;
     private Graphics2D g2d;
     private BufferedImage bufferedImage;
     private int height;
     private int width;
     private Font font;
     private FontMetrics fm;
     private File file;
     private BufferedImage img;
     private FileOutputStream outputStream;
     private ByteArrayOutputStream stream;
     private String back;
     private String url;
     private OkkopaDatabase database;
     @Autowired
     Settings settings;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
                 config.getServletContext());
     }
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try {
             // get your file as InputStream
             getAmountSizeLettersBackByForm(request);
 
             if (back.equals("txt")) {
                 writer = new PrintWriter("references.txt", "UTF-8");
             }
 
             if (OkkopaDatabase.isOpen() == false) {
                 database = new OkkopaDatabase(settings);
             }
 
             reference = new Reference(Integer.valueOf(size));
 
             for (int i = 0; i < Integer.valueOf(amount); i++) {
                 String line = getReference();
                 i = addToDBAndFileOrDoAgain(line, i);
             }
 
             if (back.equals("txt")) {
                 writer.close();
             }
 
             OkkopaDatabase.closeConnectionSource();
 
             addFileAsResponse(response);
         } catch (IOException ex) {
             throw new RuntimeException("IOError writing file to output stream");
         } catch (SQLException ex) {
             Logger.getLogger(GetReferenceServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
     private void getAmountSizeLettersBackByForm(HttpServletRequest request) {
         amount = request.getParameter("amount");
         size = request.getParameter("size");
         letters = request.getParameter("letters");
         back = request.getParameter("back");
     }
 
     private void addFileAsResponse(HttpServletResponse response) throws IOException, FileNotFoundException {
         if (back.equals("txt")) {
             InputStream is = new FileInputStream("references.txt");
             response.setContentType("text/plain");
             response.setHeader("Content-Disposition", "attachment; filename=references.txt");
             IOUtils.copy(is, response.getOutputStream());
         } else {
             ZipUtil.pack(new File("temp/"), new File("references.zip"));
             InputStream is = new FileInputStream("references.zip");
             response.setContentType("application/zip");
             response.setHeader("Content-Disposition", "attachment; filename=references.zip");
             IOUtils.copy(is, response.getOutputStream());
             FileUtils.deleteDirectory(new File("temp"));
         }
         response.flushBuffer();
     }
 
     private int addToDBAndFileOrDoAgain(String line, int i) throws SQLException, FileNotFoundException, IOException {
        if (line != null || line.equals("") == false) {
             if (OkkopaDatabase.addQRCode(line) == false) {
                 i--;
                 System.out.println("möö");
             } else {
                 if (back.equals("txt")) {
                     writer.println(line);
                 } else {
                     createFileForZip(line);
                 }
             }
         }
         return i;
     }
 
     private String getReference() {
         String line;
         if (letters.equals("yes")) {
             line = reference.getReference();
         } else {
             line = "" + reference.getReferenceNumber();
         }
         return line;
     }
 
     private void createFileForZip(String line) throws FileNotFoundException, IOException {
         makeQRCodeImage(line);
         makeGraphics2DForRender();
         fillRestWithWhite();
 
         drawUrlToImage();
         drawTextToImage(line);
 
         closeImages();
 
         writeToDirectoryToWaitForZip(line);
     }
 
     private void makeQRCodeImage(String line) throws FileNotFoundException, IOException {
         stream = QRCode.from(line).to(ImageType.PNG).withSize(500, 500).stream();
 
         outputStream = new FileOutputStream("temp.png");
         stream.writeTo(outputStream);
 
         img = ImageIO.read(new File("temp.png"));
     }
 
     private void makeGraphics2DForRender() {
         width = img.getWidth();
         height = img.getHeight();
         bufferedImage = new BufferedImage(width + 300, height - 100, BufferedImage.TYPE_INT_RGB);
         g2d = bufferedImage.createGraphics();
         g2d.drawImage(img, -50, -50, Color.WHITE, null);
     }
 
     private void makeFontSettings(int size, Color c) {
         font = new Font("Serif", Font.BOLD, size);
         g2d.setFont(font);
         g2d.setPaint(c);
         fm = g2d.getFontMetrics();
     }
 
     private void drawUrlToImage() {
         url = "http://cs.helsinki.fi/okkopa";
         makeFontSettings(24, Color.BLACK);
         g2d.drawString(url, width + 200 / 2 - (fm.stringWidth(url) / 2), height / 2 - 10);
     }
 
     private void drawTextToImage(String line) {
         makeFontSettings(70, Color.BLACK);
         g2d.drawString(line, width + 200 / 2 - (fm.stringWidth(line) / 2), height / 2 - 90);
     }
 
     private void closeImages() {
         g2d.dispose();
     }
 
     private void writeToDirectoryToWaitForZip(String line) throws IOException {
         mkDirIfNotExists();
         file = new File("temp/" + line + ".png");
         ImageIO.write(bufferedImage, "png", file);
     }
 
     private void mkDirIfNotExists() {
         File theDir = new File("temp/");
         if (!theDir.exists()) {
             theDir.mkdir();
         }
     }
 
     private void fillRestWithWhite() {
         g2d.setColor(Color.WHITE);
         g2d.fillRect(width - 50, 0, 400, height);
     }
 }
