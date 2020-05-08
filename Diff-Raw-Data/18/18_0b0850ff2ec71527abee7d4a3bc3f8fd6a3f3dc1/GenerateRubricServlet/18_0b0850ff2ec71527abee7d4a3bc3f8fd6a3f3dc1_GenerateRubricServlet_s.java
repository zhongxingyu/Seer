 package gov.usgs.cida.ncetl.servlet;
 
 import gov.usgs.cida.ncetl.utils.FileHelper;
 import thredds.server.metadata.util.ThreddsTranslatorUtil;
 import gov.usgs.cida.ncetl.utils.NcMLUtil;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import thredds.server.metadata.exception.ThreddsUtilitiesException;
 
 /**
  *
  * @author jwalker
  */
 public class GenerateRubricServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
     private static final Logger LOG = LoggerFactory.getLogger(GenerateRubricServlet.class);
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request,
                                   HttpServletResponse response)
             throws ServletException, IOException {
 
         String _xsltMetadataAssessmentUrl = "http://www.ngdc.noaa.gov/metadata/published/xsl/UnidataDDCount-HTML.xsl";
         //String xslt = GenerateRubricServlet.class.getClassLoader().getResource("UnidataDDCount-HTML1_1.xsl").getPath();
         PrintWriter out = response.getWriter();
 
 
         BufferedReader reader = null;
         try {
             String output = request.getParameter("output");
             if (output == null) {
                 response.setStatus(response.SC_BAD_REQUEST);
                 out.print("Must supply outputType");
                 return;
             }
 
             String file = request.getParameter("file");
             if (file == null) {
                 response.setStatus(response.SC_BAD_REQUEST);
                 out.print("Must supply file to run ncISO on");
                 return;
             }
             
             String filename = FileHelper.getBaseDirectory() + file;
             File checkFile = new File(filename);
             if (!checkFile.exists() || !checkFile.canRead() || !checkFile.isFile()) {
                 response.setStatus(response.SC_BAD_REQUEST);
                 out.print(
                         "Requested file doesn't exist, can't be read, or isn't a file at all");
                 return;
             }
             //NetcdfFileWriteable netCDFfile = NetcdfFileWriteable.openExisting("/home/scratch/ncrfc2000.nc");
             File ncmlFile = NcMLUtil.createNcML(filename);
 
             // always recreate html
             String htmlFile = filename + ".html";
             File html = ThreddsTranslatorUtil.transform(
                     _xsltMetadataAssessmentUrl, ncmlFile.getCanonicalPath(),
                                                         htmlFile);
             if ("ncml".equalsIgnoreCase(output)) {
                 response.setContentType("text/xml;charset=UTF-8");
                 reader = new BufferedReader(new FileReader(ncmlFile));
             }
             else if ("rubric".equalsIgnoreCase(output)) {
                 response.setContentType("text/html;charset=UTF-8");
                 reader = new BufferedReader(new FileReader(html));
             }
             else {
                 response.setStatus(response.SC_NOT_IMPLEMENTED);
                 out.print("This output type is not supported");
                 return;
             }
             String line = null;
             while ((line = reader.readLine()) != null) {
                 out.println(line);
             }
         }
                 catch (ThreddsUtilitiesException tue) {
                     LOG.error(tue.getMessage());
                 }
         finally {
             if (reader != null) reader.close();
             out.close();
         }
     }
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "This servlet runs through the nciso workflow on a given local file."
                + "  This file should be on the server in some staging area that it can be handled through"
                + " all steps of the process.";
     }
 }
