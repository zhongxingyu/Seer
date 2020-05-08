 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.bnl.racf.ps.dashboard.servlets.debug;
 
 import gov.bnl.racf.ps.dashboard.db.data_objects.*;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsCloudManipulator;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsHostManipulator;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsMatrixManipulator;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsObjectCreator;
 import gov.bnl.racf.ps.dashboard.db.session_factory_store.PsSessionFactoryStore;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 /**
  *
  * @author tomw
  */
 public class PsCreateUsAtlasServlet extends HttpServlet {
 
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
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         //boilerplate code to open session
         SessionFactory sessionFactory =
                 PsSessionFactoryStore.getSessionFactoryStore().getSessionFactory();
         
         Session session = sessionFactory.openSession();
         session.beginTransaction();
         
         try {
             /*
              * TODO output your page here. You may use following sample code.
              */
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet createUsAtlas</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet createUsAtlas at " + request.getContextPath() + "</h1>");
             
             PsCloud usatlasCloud = PsObjectCreator.createNewCloud(session);
             PsMatrix latencyMatrix = PsObjectCreator.createNewMatrix(session, PsServiceType.LATENCY, "USATLAS Packet Loss");
             PsMatrix throughputMatrix = PsObjectCreator.createNewMatrix(session, PsServiceType.THROUGHPUT, "USATLAS Throughput Loss");
             usatlasCloud.addMatrix(latencyMatrix);
             usatlasCloud.addMatrix(throughputMatrix);
 
             // BNL site
             PsSite bnlSite = PsObjectCreator.createNewSite(session);
             bnlSite.setName("BNL");
             usatlasCloud.addSite(bnlSite);
             
             PsHost lhcmon = PsObjectCreator.createNewHost(session);
             lhcmon.setHostname("lhcmon.bnl.gov");
             lhcmon.setIpv4("192.12.15.23");
             bnlSite.addHost(lhcmon);
             PsHostManipulator.addThroughputServices(session, lhcmon);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix, lhcmon);
             
             
             PsHost lhcperfmon = PsObjectCreator.createNewHost(session);
             lhcperfmon.setHostname("lhcperfmon.bnl.gov");
             lhcperfmon.setIpv4("192.12.15.26");
             PsHostManipulator.addLatencyServices(session, lhcperfmon);
             bnlSite.addHost(lhcperfmon);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix, lhcperfmon);
 
 
 
 
             //AGLT2
             PsSite aglt2 = PsObjectCreator.createNewSite(session);
             aglt2.setName("AGLT2");
             usatlasCloud.addSite(aglt2);
 
             //psmsu01.aglt2.org 192.41.236.31 latency
             PsHost psmsu01 = PsObjectCreator.createNewHost(session);
             psmsu01.setHostname("psmsu01.aglt2.org");
             psmsu01.setIpv4("192.41.236.31");
             PsHostManipulator.addLatencyServices(session, psmsu01);
             aglt2.addHost(psmsu01);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix, psmsu01);
 
 
             //psmsu02.aglt2.org 192.41.236.32 bw
             PsHost psmsu02 = PsObjectCreator.createNewHost(session);
             psmsu02.setHostname("psmsu02.aglt2.org");
             psmsu02.setIpv4("192.41.230.19");
             PsHostManipulator.addThroughputServices(session, psmsu02);
             aglt2.addHost(psmsu02);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix, psmsu02);
 
             //psum01.aglt2.org 192.41.230.19 late
             PsHost psum01 = PsObjectCreator.createNewHost(session);
             psum01.setHostname("psum01.aglt2.org");
             psum01.setIpv4("192.41.230.19");
             PsHostManipulator.addLatencyServices(session, psum01);
             aglt2.addHost(psum01);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix, psum01);
 
 
             //psum02.aglt2.org 192.41.230.20 bw
             PsHost psum02 = PsObjectCreator.createNewHost(session);
             psum02.setHostname("psum02.aglt2.org");
             psum02.setIpv4("192.41.230.20");
             PsHostManipulator.addThroughputServices(session, psum02);
             aglt2.addHost(psum02);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix, psum02);
 
 
             //MWT2
             PsSite mwt2 = PsObjectCreator.createNewSite(session);
             mwt2.setName("MWT2");
             usatlasCloud.addSite(mwt2);
 
             //iut2-net1.iu.edu 149.165.225.223 lat
             PsHost iut2net1 = PsObjectCreator.createNewHost(session);
             iut2net1.setHostname("iut2-net1.iu.edu");
             iut2net1.setIpv4("149.165.225.223");
             PsHostManipulator.addLatencyServices(session,iut2net1);
             mwt2.addHost(iut2net1);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,iut2net1);
             
             
             //iut2-net2.iu.edu 149.165.225.224 bw
             PsHost iut2net2 = PsObjectCreator.createNewHost(session);
             iut2net2.setHostname("iut2-net2.iu.edu");
             iut2net2.setIpv4("149.165.225.224");
             PsHostManipulator.addThroughputServices(session,iut2net2);
             mwt2.addHost(iut2net2);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,iut2net2);
             
             //uct2-net1.uchicago.edu 128.135.158.216 lat
             PsHost uct2net1= PsObjectCreator.createNewHost(session);
             uct2net1.setHostname("uct2-net1.uchicago.edu");
             uct2net1.setIpv4("128.135.158.216");
             PsHostManipulator.addLatencyServices(session,uct2net1);
             mwt2.addHost(uct2net1);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,uct2net1);
             
             //uct2-net2.uchicago.edu 128.135.158.219 bw
             PsHost uct2net2= PsObjectCreator.createNewHost(session);
             uct2net2.setHostname("uct2-net2.uchicago");
             uct2net2.setIpv4("128.135.158.219");
             PsHostManipulator.addThroughputServices(session,uct2net2);
             mwt2.addHost(uct2net2);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,uct2net2);
             
             //mwt2-ps01.campuscluster.illinois.edu 72.36.88.28 lat 
             PsHost mwt2ps01= PsObjectCreator.createNewHost(session);
             mwt2ps01.setHostname("mwt2-ps01.campuscluster.illinois.edu");
             mwt2ps01.setIpv4("72.36.88.28");
             PsHostManipulator.addLatencyServices(session,mwt2ps01);
             mwt2.addHost(mwt2ps01);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,mwt2ps01);
             
             
             //mwt2-ps02.campuscluster.illinois.edu 72.36.88.27 bw
             PsHost mwt2ps02=PsObjectCreator.createNewHost(session);
             mwt2ps02.setHostname("mwt2-ps02.campuscluster.illinois.edu");
             mwt2ps02.setIpv4("72.36.88.27");
             PsHostManipulator.addThroughputServices(session,mwt2ps02);
             mwt2.addHost(mwt2ps02);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,mwt2ps02);
             
 
             //NET2
             PsSite net2 =  PsObjectCreator.createNewSite(session);
             net2.setName("NET2");
             usatlasCloud.addSite(net2);
             
             //atlas-npt1.bu.edu 192.5.207.251  lat 
             PsHost atlasnpt1 = PsObjectCreator.createNewHost(session);
             atlasnpt1.setHostname("atlas-npt1.bu.edu");
             atlasnpt1.setIpv4("192.5.207.251");
             PsHostManipulator.addLatencyServices(session,atlasnpt1);
             net2.addHost(atlasnpt1);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,atlasnpt1);
             
             
             //atlas-npt2.bu.edu 192.5.207.252  bw
             PsHost atlasnpt2=PsObjectCreator.createNewHost(session);
             atlasnpt2.setHostname("atlas-npt2.bu.edu");
             atlasnpt2.setIpv4("192.5.207.252");
             PsHostManipulator.addThroughputServices(session,atlasnpt2);
             net2.addHost(atlasnpt2);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,atlasnpt2);
             
 
             //SWT2
             PsSite swt2 = PsObjectCreator.createNewSite(session);
             swt2.setName("SWT2");
             usatlasCloud.addSite(swt2);
             
             //ps1.ochep.ou.edu 129.15.40.231 lat
             PsHost ps1 = PsObjectCreator.createNewHost(session);
             ps1.setHostname("ps1.ochep.ou.edu");
             ps1.setIpv4("129.15.40.231");
             PsHostManipulator.addLatencyServices(session,ps1);
             swt2.addHost(ps1);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,ps1);
             
             //ps2.ochep.ou.edu 129.15.40.232  bw
             PsHost ps2= PsObjectCreator.createNewHost(session);
             ps2.setHostname("ps2.ochep.ou.edu");
             ps2.setIpv4("129.15.40.232");
             PsHostManipulator.addThroughputServices(session,ps2);
             swt2.addHost(ps2);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,ps2);
             
             //netmon1.atlas-swt2.org 129.107.255.26 lat
             PsHost netmon1=PsObjectCreator.createNewHost(session);
             netmon1.setHostname("netmon1.atlas-swt2.org");
             netmon1.setIpv4("129.107.255.26");
             PsHostManipulator.addLatencyServices(session,netmon1);
             swt2.addHost(netmon1);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,netmon1);
             
             //netmon2.atlas-swt2.org 129.107.255.27  bw
             PsHost netmon2 = PsObjectCreator.createNewHost(session);
             netmon2.setHostname("netmon2.atlas-swt2.org");
             netmon2.setIpv4("129.107.255.27");
             PsHostManipulator.addThroughputServices(session,netmon2);
             swt2.addHost(netmon2);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,netmon2);
 
             //WT2
             PsSite wt2 = PsObjectCreator.createNewSite(session);
             wt2.setName("WT2");
             usatlasCloud.addSite(wt2);
             
             //psnr-lat01.slac.stanford.edu 134.79.104.208   lat 
             PsHost psnrlat01 = PsObjectCreator.createNewHost(session);
             psnrlat01.setHostname("psnr-lat01.slac.stanford.edu");
             psnrlat01.setIpv4("134.79.104.208");
             PsHostManipulator.addLatencyServices(session,psnrlat01);
             wt2.addHost(psnrlat01);
             PsMatrixManipulator.addHostToMatrix(session, latencyMatrix,psnrlat01);
             
             //psnr-bw01.slac.stanford.edu 134.79.104.209 bw
             PsHost psnrbw01  = PsObjectCreator.createNewHost(session);
             psnrbw01.setHostname("psnr-bw01.slac.stanford.edu");
             psnrbw01.setIpv4("134.79.104.209");
             PsHostManipulator.addThroughputServices(session,psnrbw01);
             wt2.addHost(psnrbw01);
             PsMatrixManipulator.addHostToMatrix(session, throughputMatrix,psnrbw01);
 
             // commit transaction and close session
             session.getTransaction().commit();
             
             out.println("</body>");
             out.println("</html>");
         } catch (Exception e) {
             session.getTransaction().rollback();
             System.out.println(new Date() + " Error in " + getClass().getName() + " " + e);
             Logger.getLogger(PsCreateUsAtlasServlet.class).error(e);
             out.println("Error occured in " + getClass().getName() + " plase check the logs " + e);
         } finally {
             session.close();
             out.close();
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
