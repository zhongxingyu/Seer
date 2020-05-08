 package gov.usgs.cida.watersmart.util;
 
 import gov.usgs.cida.config.DynamicReadOnlyProperties;
 import gov.usgs.cida.watersmart.common.JNDISingleton;
 import gov.usgs.cida.watersmart.common.ModelType;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import gov.usgs.cida.watersmart.csw.CSWTransactionHelper;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URISyntaxException;
 import java.util.HashMap;
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
     private static final DynamicReadOnlyProperties props = JNDISingleton.getInstance();
     private static final long serialVersionUID = 1L;
 
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
         // Currently not being used since we don't update the modelVersion 
 //        String modelVersion = request.getParameter("version");
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
         Boolean rerun = Boolean.parseBoolean(request.getParameter("rerun")); // If this is true, we only re-run the R processing 
 
         String responseText;
         RunMetadata newRunMetadata;
 
         ModelType modelTypeEnum = null;
         if ("prms".equals(modelType.toLowerCase())) {
             modelTypeEnum = ModelType.PRMS;
         }
        else if ("afinch".equals(modelType.toLowerCase())) {
             modelTypeEnum = ModelType.AFINCH;
         }
        else if ("waters".equals(modelType.toLowerCase())) {
             modelTypeEnum = ModelType.WATERS;
         }
        else if ("sye".equals(modelType.toLowerCase())) {
             modelTypeEnum = ModelType.SYE;
         }
        else if ("stats".equals(modelType.toLowerCase())) {
            modelTypeEnum = ModelType.STATS;
        }
        else if ("prms2".equals(modelType.toLowerCase())) {
            modelTypeEnum = ModelType.PRMS2;
        }
        else if ("waterfall".equals(modelType.toLowerCase())) {
            modelTypeEnum = ModelType.WATERFALL;
        }
        else {
            throw new IllegalArgumentException("Model Type does not exist!!");
        }
 
         RunMetadata originalRunMetadata = new RunMetadata(
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
                 commonAttr,
                 updateAsBest);
 
         if (rerun) {
            String sosEndpoint = props.getProperty("watersmart.sos.model.repo") + originalRunMetadata.getTypeString() + "/" + originalRunMetadata.getFileName() + ".nc";
             WPSImpl impl = new WPSImpl();
             String implResponse = impl.executeProcess(sosEndpoint, originalRunMetadata);
             Boolean processStarted = implResponse.toLowerCase().equals("ok");
             responseText = "{success: "+processStarted.toString()+", message: '" + implResponse + "'}";
         } else {
             newRunMetadata = new RunMetadata(
                     modelTypeEnum,
                     modelId,
                     modelerName,
                     originalModelVersion,
                     runIdent,
                     runDate,
                     scenario,
                     comments,
                     email,
                     wfsUrl,
                     layer,
                     commonAttr,
                     updateAsBest);
 
             CSWTransactionHelper helper = new CSWTransactionHelper(newRunMetadata, null, new HashMap<String, String>());
             try {
                 String results = helper.updateRunMetadata(originalRunMetadata);
                 // TODO- parse xml, make sure stuff happened alright, if so don't say success
                 responseText = "{success: true, msg: 'The record has been updated'}";
             } catch (IOException ex) {
                 responseText = "{success: false, msg: '" + ex.getMessage() + "'}";
             } catch (URISyntaxException ex) {
                 responseText = "{success: false, msg: '" + ex.getMessage() + "'}";
             }
 
         }
 
         response.setContentType("application/json");
         response.setCharacterEncoding("utf-8");
 
         try {
             Writer writer = response.getWriter();
             writer.write(responseText);
             writer.close();
         } catch (IOException ex) {
             LOG.warn("An error occurred while trying to send response to client. ", ex);
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
