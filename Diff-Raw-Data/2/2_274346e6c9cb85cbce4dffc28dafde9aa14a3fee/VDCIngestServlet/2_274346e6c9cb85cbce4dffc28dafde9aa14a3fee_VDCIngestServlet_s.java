 /*
  * VDCIngestBean.java
  *
  * Created on October 2, 2007, 3:15 PM
  */
 
 package edu.harvard.hmdc.vdcnet.web.servlet;
 
 import edu.harvard.hmdc.vdcnet.admin.NetworkRoleServiceLocal;
 import edu.harvard.hmdc.vdcnet.admin.VDCUser;
 import edu.harvard.hmdc.vdcnet.dsb.DSBIngestMessage;
 import edu.harvard.hmdc.vdcnet.study.FileCategory;
 import edu.harvard.hmdc.vdcnet.study.Study;
 import edu.harvard.hmdc.vdcnet.study.StudyFile;
 import edu.harvard.hmdc.vdcnet.study.StudyFileEditBean;
 import edu.harvard.hmdc.vdcnet.study.StudyFileEditBean;
 import edu.harvard.hmdc.vdcnet.study.StudyServiceLocal;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.jms.Queue;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.QueueConnection;
 import javax.jms.QueueConnectionFactory;
 import javax.jms.QueueSender;
 import javax.jms.QueueSession;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 /**
  *
  * @author Gustavo
  * @version
  */
 public class VDCIngestServlet extends HttpServlet {
     
     @EJB StudyServiceLocal studyService;
     @Resource(mappedName="jms/DSBIngest") Queue queue;
     @Resource(mappedName="jms/DSBQueueConnectionFactory") QueueConnectionFactory factory;
     
     private boolean isNetworkAdmin(HttpServletRequest req) {
         VDCUser user = null;
         if ( LoginFilter.getLoginBean(req) != null ) {
             user= LoginFilter.getLoginBean(req).getUser();
             if (user.getNetworkRole()!=null && user.getNetworkRole().getName().equals(NetworkRoleServiceLocal.ADMIN) ) {
                 return true;
             }
         }
         
         return false;
     }
     
     
     /** Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doGet(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         
         res.setContentType("text/html;charset=UTF-8");
         PrintWriter out = res.getWriter();
         
         if (isNetworkAdmin(req)) {
             Long studyId = null;
             
             try {
                 studyId = new Long( req.getParameter("studyId") );
                 Study study = studyService.getStudy(studyId);
                 
                 String selectString = "";
                 for (FileCategory cat : study.getFileCategories()) {
                     for (StudyFile file : cat.getStudyFiles()) {
                         selectString += "<option value=" + file.getId()+ ">" + file.getFileName();
                     }
                 }
                 
                 beginPage(out);
                 out.println("<h3>Ingest</h3>");
                 out.println("<form method=POST>");
                 out.println("To make a file subsettable, please select the data file and the control card file below:<br/>");
                 
                 out.print("<input type=hidden name=\"studyId\" value=" + studyId + ">");
 
                 out.println("<br/><br/><table>");
                 
                 out.print("<tr><td>Data file</td>");
                 out.println("<td><select name=\"dataFileId\">");
                 out.println(selectString);
                 out.print("</select></td></tr>");
                 
                 out.print("<td>Control Card file: </td>");
                 out.println("<td><select name=\"controlCardFileId\">");
                 out.println(selectString);
                 out.print("</select></td></tr>");
                 
                 out.print("<td>Email: </td>");
                 out.println("<td><input name=\"emailAddress\" value=\"" +  LoginFilter.getLoginBean(req).getUser().getEmail() + "\" size=30 >");
                 out.print("</td></tr>");
                 
                 out.println("</table><br/><br/>");
                 
                 out.print("<input name=ingest value=\"Make subsettable\" type=submit />");
                 out.print("<hr>");
                 out.println("</form>");
                 endPage(out);
                 
             } catch (NumberFormatException nfe) {
                 displayMessage(out, "Action failed.", "The study id must be of type Long.");
             } catch (Exception e) {
                 e.printStackTrace();
                 displayMessage(out, "Action failed.", "An unknown error occurred trying to get study; id = " + studyId);
             }
             
         } else {
             displayMessage(out, "You are not authorized for this action.");
         }
         
     }
     
     /** Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doPost(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         res.setContentType("text/html;charset=UTF-8");
         PrintWriter out = res.getWriter();
         
         if (isNetworkAdmin(req) ) {
             
             Long studyId = new Long( req.getParameter("studyId") );
             Long dataFileId = new Long( req.getParameter("dataFileId") );
             Long controlCardFileId = new Long( req.getParameter("controlCardFileId") );
             
             StudyFileEditBean fileBean = new StudyFileEditBean( studyService.getStudyFile(dataFileId) );
             fileBean.getStudyFile().getDataTable(); // this is to instantiate lazy relationship, before serializing
             fileBean.setTempSystemFileLocation( fileBean.getStudyFile().getFileSystemLocation() );
             fileBean.setControlCardSystemFileLocation( studyService.getStudyFile(controlCardFileId).getFileSystemLocation() );
             
             List tempList = new ArrayList();
             tempList.add(fileBean);
             
             QueueConnection conn = null;
             QueueSession session = null;
             QueueSender sender = null;
             try {
                 conn = factory.createQueueConnection();
                 session = conn.createQueueSession(false,0);
                 sender = session.createSender(queue);
                 
                 DSBIngestMessage ingestMessage = new DSBIngestMessage();
                 ingestMessage.setFileBeans(tempList);
                 ingestMessage.setIngestEmail(req.getParameter("emailAddress"));
                 ingestMessage.setIngestUserId(LoginFilter.getLoginBean(req).getUser().getId());
                 ingestMessage.setStudyId(studyId);
                 Message message = session.createObjectMessage(ingestMessage);
                 sender.send(message);
                 
                 studyService.addStudyLock(studyId, LoginFilter.getLoginBean(req).getUser().getId(), "VDCIngestServlet call to DSBIngestMessageBean.");
             
             } catch (JMSException ex) {
                 ex.printStackTrace();
             } finally {
                 try {
                     if (sender != null) {sender.close();}
                     if (session != null) {session.close();}
                     if (conn != null) {conn.close();}
                 } catch (JMSException ex) {
                     ex.printStackTrace();
                 }
             }
             
             
             displayMessage(out,"Request in process. You will receive an e-mail when it is complete.", 
                     "(for study id = " + studyId + 
                     "; data file id = " + dataFileId + 
                    "; controCardFileId = " +controlCardFileId + ")");
             
         } else {
             displayMessage(out, "You are not authorized for this action.");
         }
     }
     
     private void beginPage(PrintWriter out) {
         out.println("<html>");
         out.println("<head>");
         out.println("<title>Ingest Servlet</title>");
         out.println("</head>");
         out.println("<body><center>");
     }
     private void endPage(PrintWriter out) {
         out.println("</center></body>");
         out.println("</html>");
         out.close();
     }
     
     private void displayMessage(PrintWriter out, String title) {
         displayMessage(out, title, null);
     }
     
     private void displayMessage(PrintWriter out, String title, String message) {
         beginPage(out);
         out.println("<h3>" + title + "</h3>");
         if (message != null && !message.trim().equals("") ) {
             out.println(message);
         }
         endPage(out);
     }
     
     
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         /* TODO output your page here
         out.println("<html>");
         out.println("<head>");
         out.println("<title>Servlet VDCIngestBean</title>");
         out.println("</head>");
         out.println("<body>");
         out.println("<h1>Servlet VDCIngestBean at " + request.getContextPath () + "</h1>");
         out.println("</body>");
         out.println("</html>");
          */
         out.close();
     }
     
     
     /**
      * Returns a short description of the servlet.
      */
     public String getServletInfo() {
         return "Short description";
     }
     // </editor-fold>
 }
