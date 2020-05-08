 package net.wazari.service.engine;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import net.wazari.common.util.StringUtil;
 import net.wazari.dao.PhotoFacadeLocal;
 import net.wazari.dao.TagFacadeLocal;
 import net.wazari.dao.ThemeFacadeLocal;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Tag;
 import net.wazari.dao.entity.Theme;
 import net.wazari.dao.entity.facades.SubsetOf;
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 import net.wazari.service.ImageLocal;
 import net.wazari.service.entity.util.PhotoUtil;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSessionImages;
 import net.wazari.service.exchange.ViewSessionImages.ImgMode;
 import net.wazari.service.exchange.xml.XmlImage;
 import net.wazari.util.system.SystemTools;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Stateless
 public class ImageBeaned implements ImageLocal {
     private static final Logger log = LoggerFactory.getLogger(ImageBeaned.class.getName());
     private static final long serialVersionUID = 1L;
     @EJB private PhotoFacadeLocal photoDAO ;
     @EJB private TagFacadeLocal tagDAO ;
     @EJB private PhotoUtil photoUtil ;
 
     @EJB private SystemTools sysTools ;
     @EJB private ThemeFacadeLocal themeDAO ;
     
     @Override
     public String treatSHRINK(ViewSessionImages vSession)
             throws WebAlbumsServiceException {
         Photo enrPhoto = photoDAO.loadIfAllowed(vSession, vSession.getId());
         if (enrPhoto == null) {
             return"Cette photo (" + vSession.getId() + ") n'est pas accessible ou n'existe pas ..." ;
         }
         String filepath;
         try {
             Integer width = vSession.getWidth();
             filepath = sysTools.shrink(vSession, enrPhoto, width);
             log.warn("Shrinked filepath: {}", filepath) ;
         } catch (NumberFormatException e) {
             return "Impossible de parser la taille demandee" ;
         }
         
         try {
             Integer borderWidth = vSession.getBorderWidth();
             if (borderWidth != null) {
                 String color = vSession.getBorderColor() ;
                 sysTools.addBorder(vSession, enrPhoto, new Integer(borderWidth), color, filepath);
                 log.warn("Border {}*{} ({}) added to file: {}", new Object[]{borderWidth, borderWidth, color, filepath}) ;
             }
         } catch (NumberFormatException e) {
             return "Impossible de parser le taille de la bordure a ajouter" ;
         }
         return filepath;
     }
     
     @Override
     public XmlImage treatIMG(ViewSessionImages vSession)
             throws WebAlbumsServiceException {
         ImgMode mode = vSession.getImgMode();
         mode = (mode == null ? ImgMode.MINI : mode) ;
 
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
 
         XmlImage output = new XmlImage();
         Integer imgId = vSession.getId();
         Photo enrPhoto;
         String filepath;
         String type;
         Theme enrThemeForBackground = vSession.getTheme() ;
         try {
             if (mode == ImgMode.RANDOM_TAG) {
                 if (tagDAO.find(imgId) != null) {
                     Collection<Tag> tagLst = Arrays.asList(new Tag[]{tagDAO.find(imgId)});
                     SubsetOf<Photo> photos = photoDAO.loadByTags(vSession, tagLst, new Bornes(1), ListOrder.RANDOM);
                     if (!photos.subset.isEmpty()) {
                         imgId = photos.subset.get(0).getId() ;
                         mode = ImgMode.GRAND ;
                     } else {
                         log.warn("No photo in tag: {}",imgId);
                         imgId = null ;
                     }
                 } else {
                     log.warn("No such tag: {}",imgId);
                     imgId = null ;
                 }
             } else if (mode == ImgMode.BACKGROUND) {
                 if (vSession.isRootSession()) {
                     List<Theme> lstThemes = themeDAO.findAll() ;
                     enrThemeForBackground = lstThemes.get(new Random().nextInt(lstThemes.size())) ;
                     log.info("Using Theme[{}] for root background picture", enrThemeForBackground);
 
                 }
                 if (enrThemeForBackground != null && enrThemeForBackground.getBackground() != null) {
                     imgId = enrThemeForBackground.getBackground().getId() ;
                 }
             }
 
 
             if (imgId == null) {
                 output.exception = "No photo asked ... (id=null)" ;
                 return output;
             }
 
             enrPhoto = photoDAO.loadIfAllowed(vSession, imgId);
             if (enrPhoto == null) {
                 output.exception = "Cette photo (" + imgId + ") n'est pas accessible ou n'existe pas ..." ;
                 return output ;
             }
 
             if (enrPhoto.getPath(false) == null) {
                 output.exception = "Cette photo (" + imgId + ") a un path null ..." ;
                 return output ;
             }
 
             type = (mode == ImgMode.MINI || enrPhoto.getType() == null ? "image/png" : enrPhoto.getType());
             if (mode == ImgMode.SHRINK) {
                 try {
                     Integer width = vSession.getWidth();
                     filepath = sysTools.shrink(vSession, enrPhoto, width);
                     log.warn("Shrinked filepath: {}", filepath) ;
                 } catch (NumberFormatException e) {
                     output.exception = "Impossible de parser la taille demandee" ;
                     return output ;
                 }
 
                 try {
                     Integer borderWidth = vSession.getBorderWidth();
                     if (borderWidth != null) {
                         String color = vSession.getBorderColor() ;
                         sysTools.addBorder(vSession, enrPhoto, new Integer(borderWidth), color, filepath);
                         log.warn("Border {}*{} ({}) added to file: {}", new Object[]{borderWidth, borderWidth, color, filepath}) ;
                     }
                 } catch (NumberFormatException e) {
                     output.exception = "Impossible de parser le taille de la bordure a ajouter" ;
                     return output ;
                 }
             } else if (mode == ImgMode.BACKGROUND) {
                 final int SIZE = 1280 ;
 
                 String backgroundpath = vSession.getConfiguration()
                         .getTempPath()+enrThemeForBackground.getNom()+File.separator+SIZE+".jpg" ;
                 if (vSession.getConfiguration().isPathURL()) {
                     filepath = photoUtil.getImagePath(vSession, enrPhoto) ;
                 } else if (!new File (backgroundpath).exists()) {
                     filepath = sysTools.shrink(vSession, enrPhoto, SIZE, backgroundpath);
                 } else {
                     filepath = backgroundpath ;
                 }
 
             } else if (mode == ImgMode.GRAND) {
                 filepath = photoUtil.getImagePath(vSession, enrPhoto) ;
             } else if (mode == ImgMode.GPX) {
                 filepath = photoUtil.getImagePath(vSession, enrPhoto) ;
             } else {
                 filepath = photoUtil.getMiniPath(vSession, enrPhoto);
             }
             
 
             //redirect if the image can be accessed from HTTP
             if (vSession.getConfiguration().isPathURL()) {
                 if (false) {
                     vSession.redirect(filepath) ;
                     stopWatch.stop(stopWatch.getTag()+".redirect") ;
                     return null ;
                 }
             } else {
                 filepath = "file://" + filepath;
             }
 
             stopWatch.lap("Service.treatIMG."+mode) ;
             //null = correct, true = incorrect, but contentType already set
             Boolean correct = sendFile(vSession, filepath, type, output);
             if (correct == null || correct) {
                 output = null;
             }
             stopWatch.stop("Service.treatIMG."+mode+".sendFile") ;
         } catch (Exception e) {
             log.warn ("{}: {} ", e.getClass().getSimpleName(), e) ;
             output.exception = e.getMessage();
         }
         return output;
     }
     
     protected static Boolean sendFile(ViewSessionImages vSession,
                                       String filepath,
                                       String type,
                                       XmlImage output)
     {
         boolean uniq = false;
         try {
             String safeFilepath = filepath ;
             safeFilepath = StringEscapeUtils.unescapeHtml(safeFilepath);
             
             URLConnection conn = new URL(StringUtil.escapeURL(safeFilepath)).openConnection();
             InputStream in = conn.getInputStream();
                     
             int bufferSize = (int) Math.min(conn.getContentLength(), 4 * 1024);
             byte[] buffer = new byte[bufferSize];
             int nbRead;
 
             uniq = true;
            String filename = new File(safeFilepath).getName();
            vSession.setContentDispositionFilename(filename);
            
             vSession.setContentLength(conn.getContentLength());
             vSession.setContentType(type);
             OutputStream out = vSession.getOutputStream();
             while ((nbRead = in.read(buffer)) != -1) {
                 out.write(buffer, 0, nbRead);
             }
             out.flush();
             out.close();
             
             return null;
         } catch (MalformedURLException e) {
             log.warn( "MalformedURLException: {}", e) ;
             output.exception = "MalformedURLException:"+ filepath;
 
         } catch (ConnectException e) {
             log.warn( "ConnectException: {}", e) ;
             output.exception = "ConnectException" + filepath;
         } catch (IOException e) {
             log.warn( "IOException {}({})", new Object[]{filepath, e});
             output.exception = "IOException: "+ filepath + "(" + e.getMessage() + ")" ;
         }
         return uniq;
     }
 }
