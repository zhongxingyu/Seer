 package gov.usgs.cida.watersmart.util;
 
 import gov.usgs.cida.watersmart.csw.CSWTransactionHelper;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip.ModelType;
 import gov.usgs.cida.watersmart.parse.RunMetadata;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URISyntaxException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author isuftin
  */
 public class UpdateRun extends HttpServlet {
 
     private static final Logger LOG = LoggerFactory.getLogger(UpdateRun.class);
 
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
         LOG.debug("Received new update request");
         
         String modelerName = request.getParameter("name");
         String originalModelerName = request.getParameter("originalName");
         String modelId = request.getParameter("modelId");
         String modelType = request.getParameter("modeltype");
         String modelVersion = request.getParameter("version");
         String originalModelVersion = request.getParameter("originalModelVersion");
         String runIdent = request.getParameter("runIdent");
         String originalRunIdent = request.getParameter("originalRunIdent");
         String runDate = request.getParameter("creationDate");
         String originalRunDate = request.getParameter("originalCreationDate");
         String scenario = request.getParameter("scenario");
         String originalScenario = request.getParameter("originalScenario");
         String comments = request.getParameter("comments");
         String originalComments = request.getParameter("originalComments");
         String email = request.getParameter("email");
         String wfsUrl = request.getParameter("wfsUrl");
         String layer = request.getParameter("layer");
         String commonAttr = request.getParameter("commonAttr");
        Boolean updateAsBest = "on".equalsIgnoreCase(request.getParameter("markAsBest")) ? Boolean.TRUE : Boolean.FALSE;
         
         ModelType modelTypeEnum = null;
         if ("prms".equals(modelType.toLowerCase())) modelTypeEnum = ModelType.PRMS;
         if ("afinch".equals(modelType.toLowerCase())) modelTypeEnum = ModelType.AFINCH;
         if ("waters".equals(modelType.toLowerCase())) modelTypeEnum = ModelType.WATERS;
         if ("sye".equals(modelType.toLowerCase())) modelTypeEnum = ModelType.SYE;
         
         RunMetadata metaData = new RunMetadata(
                 modelTypeEnum,
                 modelId,
                 modelerName,
                 modelVersion,
                 runIdent,
                 runDate,
                 scenario,
                 comments,
                 email,
                 wfsUrl,
                 layer,
                 commonAttr,
                 updateAsBest
         );
         
         RunMetadata originalMetaData = new RunMetadata(
                 modelTypeEnum,
                 modelId,
                 originalModelerName,
                 originalModelVersion,
                 originalRunIdent,
                 originalRunDate,
                 originalScenario,
                 originalComments,
                 email, 
                 wfsUrl,
                 layer,
                 commonAttr
         );
         
         String responseText;
         
         CSWTransactionHelper helper = new CSWTransactionHelper(metaData);
         try {
             String results = helper.update(originalMetaData);
             // parse xml, make sure stuff happened alright, if so don't say success
             responseText = "{success: true, msg: 'The record has been updated'}";
         }
         catch (IOException ioe) {
             responseText = "{success: false, msg: '" + ioe.getMessage() + "'}";
         }
         catch (URISyntaxException ex) {
             responseText = "{success: false, msg: '" + ex.getMessage() + "'}";
         }
 
         response.setContentType("application/json");
         response.setCharacterEncoding("utf-8");
 
         try {
             Writer writer = response.getWriter();
             writer.write(responseText);
             writer.close();
         } catch (IOException ex) {
             // LOG
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
 }
