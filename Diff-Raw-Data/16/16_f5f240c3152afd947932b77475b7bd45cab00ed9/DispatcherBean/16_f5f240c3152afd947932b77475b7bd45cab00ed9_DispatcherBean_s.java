 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.view.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.xml.bind.JAXBException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import net.wazari.service.UserLocal;
 import net.wazari.service.WebPageLocal;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession;
 import net.wazari.service.exchange.ViewSessionLogin;
 import net.wazari.service.exchange.ViewSessionAlbum;
 import net.wazari.service.exchange.ViewSessionConfig;
 import net.wazari.service.exchange.ViewSessionImages;
 import net.wazari.service.exchange.ViewSessionMaint;
 import net.wazari.service.exchange.ViewSessionPhoto;
 import net.wazari.service.exchange.ViewSessionTag;
 import net.wazari.dao.entity.Theme;
 import net.wazari.service.exchange.xml.XmlImage;
 import net.wazari.view.servlet.exchange.ConfigurationXML;
 import net.wazari.view.servlet.exchange.ViewSessionImpl;
 import net.wazari.view.servlet.exchange.xml.XmlWebAlbums;
 import org.apache.commons.lang.time.DurationFormatUtils;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class DispatcherBean {
 
     private static final Logger log = LoggerFactory.getLogger(DispatcherBean.class.getCanonicalName());
 
     static {
         log.warn("WebAlbums3-Servlet is being loaded ... ");
         log.info("RootPath: {}", ConfigurationXML.getConf().getRootPath());
         
     }
 
     public DispatcherBean() {
         log.warn("WebAlbums3-Servlet DispatcherBean created !");
     }
     @EJB
     private Index indexServlet;
     @EJB
     private Users userServlet;
     @EJB
     private Maint maintServlet;
     @EJB
     private Choix choixServlet;
     @EJB
     private Albums albumServlet;
     @EJB
     private Photos photoServlet;
     @EJB
     private Tags tagServlet;
     @EJB
     private Images imageServlet;
     @EJB
     private Config configServlet;
     @EJB
     private WebPageLocal webPageService;
     @EJB
     private UserLocal userService;
 
     public enum Page {
 
         PHOTO, IMAGE, USER, ALBUM, CONFIG, CHOIX, TAGS, VOID, PERIODE, MAINT
     }
 
     public void treat(ServletContext context,
             Page page,
             HttpServletRequest request,
             HttpServletResponse response)
             throws IOException, ServletException {
         Page actualPage = page;
         StopWatch stopWatch = new Slf4JStopWatch(log);
         request.setCharacterEncoding("UTF-8");
         ViewSession vSession = new ViewSessionImpl(request, response, context);
         if (request.getParameter("logout") != null) {
             log.info("Logout and cleanup the session");
             request.logout();
             userService.cleanUpSession((ViewSessionLogin) vSession);
         }
         if (page != Page.USER && page != Page.MAINT) {
             log.info("Authenticated the session");
             request.authenticate(response);
         }
         log.info("============= <{}> =============", page);
 
 
         
         response.setContentType("text/xml");
 
         XmlWebAlbums output = new XmlWebAlbums();
 
         boolean isWritten = false;
         try {
             output.xslFile = "static/Display.xsl";
             if (page == Page.USER) {
                 output.login = userServlet.treatLogin((ViewSessionLogin) vSession, request, response);
                 if (output.login == null) {
                     return;
                 }
             } else if (page == Page.VOID) {
                 output.themes = indexServlet.treatVOID(vSession);
             } else if (page == Page.MAINT) {
                 output.isComplete = true;
                 output.xslFile = "static/Empty.xsl";
                 output.maint = maintServlet.treatMaint((ViewSessionMaint) vSession);
             } else {
                 log.info("============= Login: {} =============", request.getUserPrincipal());
                 String special = request.getParameter("special");
                 if (special != null) {
                     log.info("Special XSL-style ({})", special);
                     output.xslFile = "static/Empty.xsl";
                 }
                 log.debug("XSL-style{}", output.xslFile);
                 //try to logon and set the thlaiestloleme
                 if (vSession.getThemeId() != null) {
                     log.info("Try to logon");
                     boolean ret = userService.logon((ViewSessionLogin) vSession, request);
                     log.debug("Logon result: {}", ret);
                 }
                 //from here on, the theme must be saved
                 if (vSession.getTheme() == null) {
                     actualPage = Page.VOID;
                     if (special == null) {
                         log.debug("Not logged in, not a special page, display VOID page");
                         output.themes = indexServlet.treatVOID(vSession);
 
                     } else {
                         output.isComplete = true;
                         log.debug("Not logged in, special request, nothing to display ...");
                     }
                 } else {
                     if (page == Page.CHOIX) {
                         if (special == null) {
                             log.debug("CHOIX page");
                             output.choix = choixServlet.displayCHX(vSession);
                         } else {
                             log.debug("CHOIX special page");
                             output.blob = choixServlet.displayChxScript(vSession).blob;
                             response.setContentType("text/javascript;charset=UTF-8");
                             output.isBlob = true;
                             output.isComplete = true;
                         }
                     } else if (page == Page.ALBUM) {
                         log.debug("ALBUM page");
                         output.albums = albumServlet.treatALBM((ViewSessionAlbum) vSession);
                     } else if (page == Page.PHOTO) {
                         log.debug("PHOTO page");
                         output.photos = photoServlet.treatPHOTO((ViewSessionPhoto) vSession);
                     } else if (page == Page.CONFIG) {
                         log.debug("CONFIG page");
                         output.config = configServlet.treatCONFIG((ViewSessionConfig) vSession);
                     } else if (page == Page.TAGS) {
                         log.debug("TAGS page");
                         output.tags = tagServlet.treatTAGS((ViewSessionTag) vSession);
                     } else if (page == Page.IMAGE) {
                         log.debug("IMAGE page");
                         XmlImage ret = imageServlet.treatIMG((ViewSessionImages) vSession);
                         if (ret == null) {
                             isWritten = true;
                         } else {
                             output.image = ret;
                         }
                         log.debug("IMAGE written? {}", isWritten);
                     } else {
                         log.debug("VOID page? ({})", page);
                         output.themes = indexServlet.treatVOID(vSession);
                         actualPage = Page.VOID;
                     }
                 }
             }
         } catch (WebAlbumsServiceException e) {
             log.warn("WebAlbumsServiceException", e);
         }
         log.debug("============= Footer (written:{}, complete:{})=============", new Object[]{isWritten, output.isComplete});
         Theme currentTheme = vSession.getTheme();
         stopWatch.stop("View.dispatch." + actualPage + (vSession.getSpecial() != null ? "." + vSession.getSpecial() : "") + (currentTheme != null ? "." + currentTheme.getNom() : "NoTheme"));
         String strTime = DurationFormatUtils.formatDuration(stopWatch.getElapsedTime(), "m'min' s's' S'ms'", false);
         if (!isWritten) {
             preventCaching(request, response);
 
             if (!output.isComplete) {
                 try {
                     output.loginInfo = webPageService.xmlLogin((ViewSessionLogin) vSession);
                 } catch (Exception e) {
                     log.warn("An exception occured during xmlLogin:", e);
                 }
                 try {
                     output.affichage = webPageService.xmlAffichage(vSession);
                 } catch (Exception e) {
                     log.warn("An exception occured during xmlAffichage:", e);
                 }
                 output.time = strTime;
             }
             doWrite(response, output);
         }
 
         log.warn("============= <{}/>: {} =============", new Object[]{page, strTime});
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     private static void doWrite(HttpServletResponse response, XmlWebAlbums output) {
         try {
             PrintWriter sortie = response.getWriter();
 
             if (!output.isBlob) {
                 if (!output.isComplete) {
                     sortie.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                     sortie.println("<!DOCTYPE xsl:stylesheet  ["
                             + "<!ENTITY auml   \"&#228;\" >"
                             + "<!ENTITY ouml   \"&#246;\" >"
                             + "<!ENTITY uuml   \"&#252;\" >"
                             + "<!ENTITY szlig  \"&#223;\" >"
                             + "<!ENTITY Auml   \"&#196;\" >"
                             + "<!ENTITY Ouml   \"&#214;\" >"
                             + "<!ENTITY Uuml   \"&#220;\" >"
                             + "<!ENTITY euml   \"&#235;\" >"
                             + "<!ENTITY ocirc  \"&#244;\" >"
                             + "<!ENTITY nbsp   \"&#160;\" >"
                             + "<!ENTITY Agrave \"&#192;\" >"
                             + "<!ENTITY Egrave \"&#200;\" >"
                             + "<!ENTITY Eacute \"&#201;\" >"
                             + "<!ENTITY Ecirc  \"&#202;\" >"
                             + "<!ENTITY egrave \"&#232;\" >"
                             + "<!ENTITY eacute \"&#233;\" >"
                             + "<!ENTITY ecirc  \"&#234;\" >"
                             + "<!ENTITY agrave \"&#224;\" >"
                             + "<!ENTITY iuml   \"&#239;\" >"
                             + "<!ENTITY ugrave \"&#249;\" >"
                             + "<!ENTITY ucirc  \"&#251;\" >"
                             + "<!ENTITY uuml   \"&#252;\" >"
                             + "<!ENTITY ccedil \"&#231;\" >"
                             + "<!ENTITY AElig  \"&#198;\" >"
                             + "<!ENTITY aelig  \"&#330;\" >"
                             + "<!ENTITY OElig  \"&#338;\" >"
                             + "<!ENTITY oelig  \"&#339;\" >"
                             + "<!ENTITY euro   \"&#8364;\">"
                             + "<!ENTITY laquo  \"&#171;\" >"
                             + "<!ENTITY raquo  \"&#187;\" >"
                             + "]>");
 
                     sortie.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + output.xslFile + "\"?>");
                 }
                 //Create JAXB Context
                 JAXBContext jc = JAXBContext.newInstance(XmlWebAlbums.class);

                 //Create marshaller
                 Marshaller marshaller = jc.createMarshaller();
                 marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                 marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

                marshaller.marshal(output, sortie);
             } else {
                 sortie.println(output.blob);
             }
             sortie.flush();
             sortie.close();
         } catch (IOException e) {
             log.error("IOException: ", e);
         } catch (JAXBException e) {
             log.error("IOException: ", e);
         }
     }
 
     protected static void preventCaching(HttpServletRequest request,
             HttpServletResponse response) {
         // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
         String protocol = request.getProtocol();
         if ("HTTP/1.0".equalsIgnoreCase(protocol)) {
             response.setHeader("Pragma", "no-cache");
         } else if ("HTTP/1.1".equalsIgnoreCase(protocol)) {
             response.setHeader("Cache-Control", "no-cache"); // "no-store" work also
         }
         response.setDateHeader("Expires", 0);
     }// </editor-fold>
 }
