 package net.wazari.service.engine;
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import net.wazari.common.util.StringUtil;
 import net.wazari.dao.AlbumFacadeLocal;
 import net.wazari.dao.AlbumFacadeLocal.Restriction;
 import net.wazari.dao.AlbumFacadeLocal.TopFirst;
 import net.wazari.dao.CarnetFacadeLocal;
 import net.wazari.dao.PhotoFacadeLocal;
 import net.wazari.dao.UtilisateurFacadeLocal;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Carnet;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.dao.entity.facades.SubsetOf;
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.service.CarnetLocal;
 import net.wazari.service.WebPageLocal;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession.Box;
 import net.wazari.service.exchange.ViewSession.Mode;
 import net.wazari.service.exchange.ViewSessionCarnet;
 import net.wazari.service.exchange.ViewSessionCarnet.ViewSessionCarnetDisplay;
 import net.wazari.service.exchange.ViewSessionCarnet.ViewSessionCarnetEdit;
 import net.wazari.service.exchange.ViewSessionCarnet.ViewSessionCarnetSubmit;
 import net.wazari.service.exchange.xml.carnet.*;
 import net.wazari.service.exchange.xml.common.XmlDetails;
 import net.wazari.service.exchange.xml.common.XmlFrom;
 import net.wazari.service.exchange.xml.common.XmlPhotoAlbumUser;
 import net.wazari.service.exchange.xml.photo.XmlPhotoId;
 import net.wazari.util.system.FilesFinder;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Stateless
 public class CarnetBean implements CarnetLocal {
     private static final Logger log = LoggerFactory.getLogger(CarnetBean.class.getCanonicalName()) ;
     private static final long serialVersionUID = 1L;
 
     @EJB
     private CarnetFacadeLocal carnetDAO;
     @EJB
     private UtilisateurFacadeLocal userDAO;
     @EJB
     private PhotoFacadeLocal photoDAO;
     @EJB
     private WebPageLocal webPageService;
     @EJB private FilesFinder finder;
 
     @Override
     public XmlCarnetEdit treatEDIT(ViewSessionCarnetEdit vSession,
             XmlCarnetSubmit submit)
             throws WebAlbumsServiceException {
 
         XmlCarnetEdit output = new XmlCarnetEdit();
         Integer carnetId = vSession.getCarnet();
         Integer page = vSession.getPage();
         
         if (submit != null) {
             output.submit = submit ;
         }
         
         /*Reuse the Carnet from SUBMIT if any, otherwise lookup this one.  */
         Carnet enrCarnet = (submit != null && submit.carnet != null) ?
                 submit.carnet : carnetDAO.find(carnetId);
         
         if (enrCarnet == null) {
             output.rights = webPageService.displayListDroit(null, null);
             return output ;
         }
         output.rights = webPageService.displayListDroit(enrCarnet.getDroit(), null);
         if (enrCarnet.getPicture() != null) {
             output.picture = enrCarnet.getPicture().getId();
         }
         output.name = enrCarnet.getNom();
         output.page = page;
         output.id = enrCarnet.getId();
         output.description = enrCarnet.getDescription();
         output.text = enrCarnet.getText();
         output.date = enrCarnet.getDate();
 
         return output;
     }
 
     public XmlCarnetsDisplay treatDISPLAY(ViewSessionCarnetDisplay vSession,
             XmlCarnetSubmit submit) throws WebAlbumsServiceException {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
         
         XmlCarnetsDisplay output = new XmlCarnetsDisplay() ;
         XmlFrom thisPage = new XmlFrom();
         thisPage.name = "Carnets" ;
         thisPage.carnetsPage = vSession.getCarnetsPage() ;
         if (thisPage.carnetsPage == null)
             thisPage.carnetsPage = 0;
         Integer page = vSession.getPage();
 
         List<Carnet> carnets = null;
         Integer carnetId = vSession.getCarnet();
         if (carnetId != null) {
             Carnet enrCarnet = carnetDAO.loadIfAllowed(vSession, carnetId);
             if (enrCarnet != null) {
                 carnets = new ArrayList<Carnet>(1);
                 carnets.add(enrCarnet);
             } else {
                 output.message = "Couldn't load carnet #"+carnetId;
                 carnetId = null;
             }
         }
         
         if (carnetId == null) {
             Bornes bornes = webPageService.calculBornes(page, 
                                     vSession.getPhotoAlbumSize());
             carnets = carnetDAO.queryCarnets(vSession, Restriction.THEME_ONLY, 
                                 AlbumFacadeLocal.TopFirst.FIRST, bornes).subset;
             output.page = webPageService.xmlPage(thisPage, bornes);
         }
         
        String oldDate = null ;
         for(Carnet enrCarnet : carnets) {
             XmlCarnet carnet = new XmlCarnet();
 
             carnet.date = webPageService.xmlDate(enrCarnet.getDate());
            oldDate = enrCarnet.getDate() ;
             carnet.id = enrCarnet.getId();
             carnet.carnetsPage = page;
             carnet.name = enrCarnet.getNom();
             if (carnetId != null) {
                 carnet.text = StringUtil.escapeXML(enrCarnet.getText());
             }
             if (enrCarnet.getPhotoList() != null) {
                 carnet.photo = new ArrayList<XmlPhotoId>(enrCarnet.getPhotoList().size());
                 for (Photo p : enrCarnet.getPhotoList()) {
                     XmlPhotoId photo = new XmlPhotoId(p.getId());
                     carnet.photo.add(photo);
                     if (vSession.directFileAccess())
                         photo.path = p.getPath(true);
                 }
             }
             XmlDetails details = new XmlDetails();
 
             if (enrCarnet.getPicture() != null) {
                 details.photoId = new XmlPhotoId(enrCarnet.getPicture().getId());
                 if (vSession.directFileAccess() && details.photoId != null) {
                     details.photoId.path = enrCarnet.getPicture().getPath(true) ;
                 }
             }
             
             details.description = enrCarnet.getDescription();
 
             //tags du carnet
             details.tag_used = webPageService.displayListIBT(Mode.TAG_USED, vSession, enrCarnet, Box.NONE) ;
             //utilisateur ayant le droit à l'album
             //ou a l'une des photos qu'il contient
             if (vSession.isSessionManager()) {
                 details.user = new XmlPhotoAlbumUser(enrCarnet.getDroit().getNom(), null);
             }
             carnet.details = details ;
 
             output.carnet.add(carnet);
         }
 
         
         stopWatch.stop("Service.displayCarnet") ;
         return output ;
     }
 
     @Override
     public XmlCarnetSubmit treatSUBMIT(ViewSessionCarnetSubmit vSession)
             throws WebAlbumsServiceException {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
         XmlCarnetSubmit output = new XmlCarnetSubmit();
         output.valid = false;
         
         Carnet enrCarnet;
         
         Integer carnetId = vSession.getCarnet();
         if (carnetId == null) {
             enrCarnet = carnetDAO.newCarnet();
             enrCarnet.setTheme(vSession.getTheme());
         } else {
             enrCarnet = carnetDAO.find(carnetId);
         }
         
         if (enrCarnet == null) {
             output.exception = "Couldn't find the carnet #"+carnetId;
             return output;
         }
         output.carnet = enrCarnet;
         Boolean supprParam = vSession.getSuppr();
         if (supprParam) {
             if (finder.deleteCarnet(enrCarnet, vSession.getConfiguration())) {
                 output.message = "Carnet correctement  supprimé !";
             } else {
                 output.exception = 
                         "an error occured during the carnet deletion ...";
             }
             return output;
         }
         Integer repr = vSession.getCarnetRepr();
         Integer user = vSession.getUserAllowed();
         String desc = vSession.getDesc();
         String nom = vSession.getNom();
         String date = vSession.getDate();
         String text = vSession.getCarnetText();
         Set<Integer> photos = vSession.getCarnetPhoto();
         
         if (user != null) {
             Utilisateur enrDroit = userDAO.find(user);
             if (enrDroit != null) {
                 enrCarnet.setDroit(enrDroit);
             }
         }
         
         enrCarnet.setText(text);
         enrCarnet.setNom(nom);
         enrCarnet.setDescription(desc);
         try {
             date.substring(0) ; //NullPointerException
             new SimpleDateFormat("yyyy-MM-dd").parse(date); //ParseException
             enrCarnet.setDate(date);
         } catch (Exception ex) {
             log.info("Date incorrect: "+date);
             output.exception = "Date incorrect: '"+date+"', "
                     + "format attendu: yyyy-MM-dd";
             return output;
         }
         
         if (repr != null) {
             try {
                 Photo enrRepr = photoDAO.find(repr);
                 enrCarnet.setPicture(enrRepr);
                 photos.add(repr);
             } catch (Exception e) {
                 log.info("Couldn't find the representative picture: "+repr);
                 output.exception = "Couldn't find the representative picture: "
                                    +repr;
                 return output;
             }
         }
         
         Set<Photo> enrPhotos = new HashSet<Photo>();
         Set<Album> enrAlbums = new HashSet<Album>();
         for (Integer photoId : photos) {
             try {
                 Photo enrPhoto = photoDAO.find(photoId);
                 
                 enrAlbums.add(enrPhoto.getAlbum()); //NullPointerException
                 enrPhotos.add(enrPhoto);
             } catch (Exception e) {
                 log.info("Couldn't find one of the pictures: "+photoId);
                 output.exception = "Couldn't find one of the pictures: "
                                    + photoId;
                 return output;
             }
         }
         
         if (!enrPhotos.isEmpty())
             enrCarnet.setPhotoList(new ArrayList(enrPhotos));
         
         if (!enrAlbums.isEmpty())
             enrCarnet.setAlbumList(new ArrayList(enrAlbums));
         
         try {
             if (carnetId == null)
                 carnetDAO.create(enrCarnet);
 
             carnetDAO.edit(enrCarnet);
             output.message = "Carnet (" + enrCarnet.getId() + ") correctement mise à jour !";
         } catch (Exception e) {
             output.exception = "La mise a jour de (" + enrCarnet.getId() + ") a échouée ... "+e.getMessage();
             return output ;
         }
         output.valid = true;
         stopWatch.stop("Service.treatSUBMIT") ;
         return output ;   
     }
     
     @Override
     public XmlCarnetsTop treatTOP(ViewSessionCarnet vSession) {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
         XmlCarnetsTop top5 = new XmlCarnetsTop();
 
         SubsetOf<Carnet> carnets = carnetDAO.queryCarnets(vSession, 
                Restriction.THEME_ONLY, TopFirst.TOP, new Bornes(AlbumBean.TOP));
         int i = 0;
         for (Carnet enrCarnet : carnets.subset) {
             XmlCarnet carnet = new XmlCarnet();
             carnet.id = enrCarnet.getId();
             carnet.carnetsPage = 0;
             carnet.name = enrCarnet.getNom();
             if (enrCarnet.getPicture() != null) {
                 carnet.picture = new XmlPhotoId(enrCarnet.getPicture().getId());
                 if (vSession.directFileAccess())
                     carnet.picture.path = enrCarnet.getPicture().getPath(true);
             }
             top5.carnet.add(carnet);
             i++ ;
         }
         stopWatch.stop("Service.treatTOP") ;
         return top5;
     }
 }
