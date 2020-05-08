 package net.wazari.service.engine;
 
 import net.wazari.service.exchange.ViewSessionPhoto;
 import net.wazari.service.exchange.xml.photo.XmlPhoto;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 import net.wazari.dao.AlbumFacadeLocal;
 import net.wazari.dao.PhotoFacadeLocal;
 import net.wazari.dao.TagFacadeLocal;
 import net.wazari.dao.TagThemeFacadeLocal;
 import net.wazari.dao.ThemeFacadeLocal;
 import net.wazari.dao.UtilisateurFacadeLocal;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Carnet;
 import net.wazari.dao.entity.Gpx;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.TagTheme;
 import net.wazari.dao.entity.Tag;
 import net.wazari.dao.entity.Theme;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.dao.entity.facades.SubsetOf;
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.service.PhotoLocal;
 import net.wazari.service.UserLocal;
 import net.wazari.service.WebPageLocal;
 import net.wazari.service.entity.util.PhotoUtil;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession.Action;
 import net.wazari.service.exchange.ViewSession.Box;
 import net.wazari.service.exchange.ViewSession.Mode;
 import net.wazari.service.exchange.ViewSession.Special;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoDisplay;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoEdit;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoSubmit;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoDisplay.ViewSessionPhotoDisplayMassEdit.Turn;
 import net.wazari.util.system.FilesFinder;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 import net.wazari.service.exchange.ViewSession;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoFastEdit;
 import net.wazari.service.exchange.ViewSessionPhoto.ViewSessionPhotoFastEdit.TagAction;
 import net.wazari.service.exchange.xml.album.XmlAlbum;
 import net.wazari.service.exchange.xml.album.XmlGpx;
 import net.wazari.service.exchange.xml.carnet.XmlCarnet;
 import net.wazari.service.exchange.xml.common.XmlDetails;
 import net.wazari.service.exchange.xml.common.XmlFrom;
 import net.wazari.service.exchange.xml.common.XmlPhotoAlbumUser;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList.XmlWebAlbumsTagWho;
 import net.wazari.service.exchange.xml.photo.XmlPhotoAbout;
 import net.wazari.service.exchange.xml.photo.XmlPhotoDisplay;
 import net.wazari.service.exchange.xml.photo.XmlPhotoEdit;
 import net.wazari.service.exchange.xml.photo.XmlPhotoFastEdit;
 import net.wazari.service.exchange.xml.photo.XmlPhotoId;
 import net.wazari.service.exchange.xml.photo.XmlPhotoList;
 import net.wazari.service.exchange.xml.photo.XmlPhotoMassEdit;
 import net.wazari.service.exchange.xml.photo.XmlPhotoRandom;
 import net.wazari.service.exchange.xml.photo.XmlPhotoSubmit;
 import net.wazari.util.system.SystemTools;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 
 @Stateless
 public class PhotoBean implements PhotoLocal {
 
     private static final Logger log = LoggerFactory.getLogger(PhotoBean.class.toString());
     private static final long serialVersionUID = 1L;
     @EJB
     PhotoUtil photoUtil;
     @EJB
     private PhotoFacadeLocal photoDAO;
     @EJB
     private AlbumFacadeLocal albumDAO;
     @EJB
     private TagThemeFacadeLocal tagThemeDAO;
     @EJB
     private TagFacadeLocal tagDAO;
     @EJB
     private ThemeFacadeLocal themeDAO;
     @EJB
     private UtilisateurFacadeLocal userDAO;
     @EJB
     private WebPageLocal webPageService;
     @EJB
     private FilesFinder finder;
     @EJB
     private SystemTools sysTools;
 
     @Override
     public XmlPhotoSubmit treatPhotoSUBMIT(ViewSessionPhotoSubmit vSession,
             Boolean correct) throws WebAlbumsServiceException {
         StopWatch stopWatch = new Slf4JStopWatch("Service.treatPhotoSUBMIT", log) ;
         XmlPhotoSubmit output = new XmlPhotoSubmit();
         Integer photoID = vSession.getId();
 
         if (photoID == null) {
             output.exception = "Pas de photo demandé ... (id=null)" ;
             return output ;
         }
         Photo enrPhoto = photoDAO.loadIfAllowed(vSession, photoID);
 
         if (enrPhoto == null) {
             output.exception = "Impossible de trouver cette photo "
                     + "(" + photoID + (vSession.isRootSession() ? "" : "/" + vSession.getTheme()) + ")" ;
             return output ;
         }
 
         //supprimer ?
         if (vSession.getSuppr()) {
             if (finder.deletePhoto(enrPhoto, vSession.getConfiguration())) {
                 output.message = "Photo correctement  supprimé !";
             } else {
                 output.exception = "Impossible de supprimer correctement la photo ...";
             }
             return output ;
         }
 
         //mise à jour des tag/description
         String desc = vSession.getDesc();
         enrPhoto.setDescription(StringEscapeUtils.escapeXml(desc));
 
         String user = vSession.getDroit();
         if (user != null) {
             boolean valid = false;
             try {
                 Integer userId = null;
 
                 if ("null".equals(user)) {
                     valid = true;
                 } else if (userDAO.find(Integer.parseInt(user)) != null) {
                     valid = true;
                     userId = Integer.parseInt(user);
                 } else {
                     log.warn( "Unknown userId:{}", user);
                 }
 
                 if (valid) {
                     log.info("Set Droit to:{}", userId);
                     enrPhoto.setDroit(userId);
                 }
 
             } catch (NumberFormatException e) {
                 log.warn( "Cannot parse userId:{}", user);
             }
         }
 
         Integer[] tags = vSession.getNewTag();
         photoUtil.setTags(enrPhoto, tags);
 
         photoDAO.edit(enrPhoto);
 
         //utiliser cette photo comme representante de l'album ?
         Boolean represent = vSession.getRepresent();
         if (represent) {
             Album enrAlbum = enrPhoto.getAlbum();
             if (enrAlbum == null) {
                 output.exception = "Impossible d'acceder l'album à representer "
                         + "(" + enrPhoto.getAlbum() + ")" ;
                 return output ;
             }
 
             enrAlbum.setPicture(enrPhoto);
             albumDAO.edit(enrAlbum);
         }
 
         //utiliser cette photo comme representante de l'album ?
         Boolean themeBackground = vSession.getThemeBackground();
         if (themeBackground) {
             Theme enrTheme = enrPhoto.getAlbum().getTheme();
             log.warn("Assign theme background {}",enrPhoto) ;
             themeDAO.setBackground(enrTheme, enrPhoto);
             vSession.getTheme().setBackground(enrPhoto);
             File backgroundDir = new File(vSession.getConfiguration()
                     .getTempPath()+vSession.getTheme().getNom()) ;
             log.info("Delete and create background dir: {}", backgroundDir) ;
             if (backgroundDir.listFiles() != null) {
                 for (File child : backgroundDir.listFiles()) {
                     if (!child.delete()) {
                         log.warn("Could not remove background file: {}",child);
                     }
                 }
             }
         }
         //utiliser cette photo comme representante de l'album ?
         Boolean themePicture = vSession.getThemePicture();
         if (themePicture) {
             Theme enrTheme = enrPhoto.getAlbum().getTheme();
             log.warn("Assign theme picture {}",enrPhoto) ;
             themeDAO.setPicture(enrTheme, enrPhoto);
             vSession.getTheme().setPicture(enrPhoto);
         }
 
         //utiliser cette photo pour representer le tag de ce theme
         Tag enrTag = tagDAO.find(vSession.getTagPhoto());
         if (enrTag != null) {
             Theme actualTheme = enrPhoto.getAlbum().getTheme();
 
             TagTheme enrTagTh = null;
             for (TagTheme enrTTCurrent : enrTag.getTagThemeList()) {
                 if (enrTTCurrent.getTheme().getId().equals(actualTheme.getId())) {
                     enrTagTh = enrTTCurrent;
                     break;
                 }
             }
 
             if (enrTagTh == null) {
                 log.warn("CREATE TAG");
                 //creer un tagTheme pour cette photo/tag/theme
                 enrTagTh = tagThemeDAO.newTagTheme();
 
                 enrTagTh.setTheme(actualTheme);
                 enrTagTh.setTag(enrTag);
                 //par défaut le tag est visible
                 enrTagTh.setIsVisible(true);
 
                 tagThemeDAO.create(enrTagTh);
             } else {
                 log.warn("EDIT TAG");
             }
             //changer la photo representant ce tag/theme
             enrTagTh.setPhoto(enrPhoto);
 
             log.info( "edit {}", enrTagTh);
             tagThemeDAO.edit(enrTagTh);
         }
 
         output.message = " Photo (" + enrPhoto.getId() + ") "
                 + "correctement mise à jour !" ;
 
         stopWatch.stop() ;
         return output ;
     }
 
     @Override
     public XmlPhotoDisplay treatPhotoDISPLAY(ViewSessionPhotoDisplay vSession, XmlPhotoSubmit submit) throws WebAlbumsServiceException {
         StopWatch stopWatch = new Slf4JStopWatch("Service.treatPhotoDISPLAY", log) ;
 
         XmlPhotoDisplay output = new XmlPhotoDisplay();
         //afficher les photos
         //afficher la liste des albums de cet theme
         Integer page = vSession.getPage();
         Integer albmPage = vSession.getAlbmPage();
         Special special = vSession.getSpecial();
         page = (page == null ? 0 : page);
 
         Integer albumId = vSession.getAlbum();
         if (albumId == null) {
             output.exception = "No album asked ... (id=null)" ;
             return output ;
         }
 
         Album enrAlbum = null;
         enrAlbum = albumDAO.loadIfAllowed(vSession, albumId);
         if (enrAlbum == null) {
             output.exception = "L'album (" + albumId + ") n'existe pas "
                     + "ou n'est pas accessible..." ;
             return output ;
         }
         XmlAlbum album = new XmlAlbum();
         output.album = album ;
 
         album.id = enrAlbum.getId();
         album.title = enrAlbum.getNom();
         album.droit = enrAlbum.getDroit().getNom();
         album.date = webPageService.xmlDate(enrAlbum.getDate());
         
         for (Carnet enrCarnet: enrAlbum.getCarnetList()) {
             if (album.carnet == null)
                 album.carnet = new ArrayList(enrAlbum.getCarnetList().size()) ;
 
             XmlCarnet carnet = new XmlCarnet();
             carnet.date = webPageService.xmlDate(enrCarnet.getDate());
             carnet.id = enrCarnet.getId();
             carnet.name = enrCarnet.getNom();
             if (enrCarnet.getPicture() != null) {
                 carnet.picture = new XmlPhotoId(enrCarnet.getPicture().getId());
                 if (vSession.directFileAccess()) 
                     carnet.picture.path = enrCarnet.getPicture().getPath(true);
             }
 
             album.carnet.add(carnet);
         }
         
         for (Gpx enrGpx : enrAlbum.getGpxList()) {
             if (album.gpx == null)
                 album.gpx = new ArrayList(enrAlbum.getGpxList().size()) ;
             XmlGpx gpx = new XmlGpx();
             gpx.id = enrGpx.getId();
             if (vSession.directFileAccess())
                 gpx.path = enrGpx.getGpxPath();
             gpx.description = enrGpx.getDescription();
 
             album.gpx.add(gpx);
         }
         
         XmlDetails details = new XmlDetails() ;
         details.description = enrAlbum.getDescription() ;
         
         if (enrAlbum.getPicture() != null) {
             details.photoId = new XmlPhotoId(enrAlbum.getPicture().getId()) ;
             if (vSession.directFileAccess())
                 details.photoId.path = enrAlbum.getPicture().getPath(true) ;
         }
         XmlFrom thisPage = new XmlFrom();
         thisPage.name = "Photos";
         thisPage.album = albumId ;
         thisPage.albmPage = albmPage ;
         
         PhotoRequest rq = new PhotoRequest(TypeRequest.PHOTO, enrAlbum);
         if (Special.FULLSCREEN == special) {
             sysTools.fullscreenMultiple(vSession, rq, enrAlbum.getId(), page, "Albums");
         }
         output.photoList = displayPhoto(rq, vSession, submit, thisPage);
 
 
 
         stopWatch.stop() ;
         return output ;
     }
 
     @Override
     public XmlPhotoEdit treatPhotoEDIT(ViewSessionPhotoEdit vSession, XmlPhotoSubmit submit) throws WebAlbumsServiceException {
         XmlPhotoEdit output = new XmlPhotoEdit();
         if (submit != null) {
             output.submit = submit ;
         }
 
         Integer photoID = vSession.getId();
 
         Photo enrPhoto = photoDAO.find(photoID);
 
         if (enrPhoto == null) {
             output.exception = "Impossible de trouver la photo (" + photoID + ")" ;
             return output ;
         }
         Album enrAlbum = enrPhoto.getAlbum();
 
         output.id = enrPhoto.getId() ;
         output.album = enrPhoto.getAlbum().getId() ;
         output.description = enrPhoto.getDescription();
         output.tag_used = webPageService.displayListIBT(Mode.TAG_USED, vSession, enrPhoto,
                 Box.MULTIPLE);
 
         output.tag_nused = webPageService.displayListIBT(Mode.TAG_NUSED, vSession, enrPhoto,
                 Box.MULTIPLE);
 
         output.tag_never = webPageService.displayListIBT(Mode.TAG_NEVER, vSession, enrPhoto,
                 Box.MULTIPLE);
 
         output.tag_used_lst = webPageService.displayListIBT(Mode.TAG_USED, vSession, enrPhoto,
                 Box.LIST);
         Utilisateur enrUtil = userDAO.find(enrPhoto.getDroit());
         output.rights = webPageService.displayListDroit(enrUtil, enrAlbum.getDroit().getId());
 
         return output ;
     }
 
     @Override
     @RolesAllowed(UserLocal.VIEWER_ROLE)
     public XmlPhotoList displayPhoto(PhotoRequest rq,
             ViewSessionPhotoDisplay vSession,
             XmlPhotoSubmit submit,
             XmlFrom thisPage)
             throws WebAlbumsServiceException {
         StopWatch stopWatch = new Slf4JStopWatch("Service.displayPhoto."+rq.type, log) ;
 
         Integer page = vSession.getPage();
         Integer photoId = vSession.getId();
         
         Bornes bornes = null;
         SubsetOf<Photo> lstP = null;
         boolean found = false;
         if (page == null && photoId != null) {
             int ipage = 0;
             while (!found) {
                 bornes = webPageService.calculBornes(ipage, vSession.getPhotoAlbumSize());
         
                 if (rq.type == TypeRequest.PHOTO) {
                     lstP = photoDAO.loadFromAlbum(vSession, rq.albumId, bornes, 
                                                   ListOrder.ASC);
                 } else {
                     lstP = photoDAO.loadByTags(vSession, rq.listTagId, bornes, 
                                                ListOrder.ASC);
                 }
                 for (Photo enrPhoto : lstP.subset) {
                     if (enrPhoto.getId() == photoId) {
                         found = true;
                         break;   
                     }
                 }
                     
                 if (lstP.setSize == 0) {
                     break;
                 }
                 ipage++;
             }
         } 
         if (!found) {
             bornes = webPageService.calculBornes(page, vSession.getPhotoAlbumSize());
         
             if (rq.type == TypeRequest.PHOTO) {
                 lstP = photoDAO.loadFromAlbum(vSession, rq.albumId, bornes, 
                                           ListOrder.ASC);
             } else {
                 lstP = photoDAO.loadByTags(vSession, rq.listTagId, bornes, 
                                            ListOrder.ASC);
             }
         }        
 
         String degrees = "0";
         Integer tag = null;
         int countME = 0;
         boolean massEditParam = false;
         boolean reSelect = false;
         boolean current = false;
 
         XmlPhotoList output = new XmlPhotoList(lstP.subset.size()) ;
         Turn turn = null;
         if (vSession.isSessionManager()) {
             try {
                 Action action = vSession.getAction();
                 if (Action.MASSEDIT == action) {
                     turn = vSession.getMassEdit().getTurn();
                     stopWatch.setTag(stopWatch.getTag()+".MASSEDIT."+turn) ;
                     if (turn == Turn.LEFT) {
                         degrees = "270";
                     } else if (turn == Turn.RIGHT) {
                         degrees = "90";
                     } else if (turn == Turn.TAG || turn == Turn.UNTAG || turn == Turn.MVTAG) {
                         tag = vSession.getMassEdit().getAddTag();
                     } else if (turn == Turn.AUTHOR) {
                         tag = vSession.getMassEdit().getAddTag();
                     }
                     massEditParam = true;
                 }
             } catch (NoSuchElementException e) {
                 output.exception = "NoSuchElementException: "+ tag;
                 reSelect = true;
             }
         }
 
         boolean submitted = false ;
         for (Photo enrPhoto : lstP.subset) {
             XmlPhoto photo = new XmlPhoto();
             boolean reSelectThis = false;
             if (massEditParam) {
                 Boolean chkbox = vSession.getMassEdit().getChk(enrPhoto.getId());
                 if (chkbox) {
                     current = true;
                    if (turn == Turn.LEFT || turn == Turn.RIGHT) {
                        if (!photoUtil.rotate(vSession, enrPhoto, degrees)) {
                            photo.exception = "Erreur dans le ConvertWrapper ...";
                            reSelectThis = true;
                        }
                    
                    } else if (turn != null) {
                         String verb;
                         if (turn == Turn.TAG) {
                             photoUtil.addTags(enrPhoto, new Integer[]{tag});
                             verb = "added";
                         } else if (turn == Turn.UNTAG) {
                             photoUtil.removeTag(enrPhoto, tag);
                             verb = "removed";
                         } else if (turn == Turn.MVTAG) {
                             Integer rmTag = vSession.getMassEdit().getRmTag();
                             photoUtil.removeTag(enrPhoto, rmTag);
                             photoUtil.addTags(enrPhoto, new Integer[]{tag});
                             verb = "added and tag " + rmTag + " removed";
                         } else if (turn == Turn.AUTHOR) {
                             enrPhoto.setTagAuthor(tagDAO.find(tag));
                             verb = "set as author";
                         } else {
                             verb = "nothinged";
                         }
                         photo.message = "Tag " + tag + " " + verb + " from photo #" + enrPhoto.getId();
                     }
                     countME++;
                 }
             }
 
             if (enrPhoto.getId().equals(photoId)) {
                 if (submit != null) {
                     photo.submit = submit ;
                     submitted = true ;
                 }
             }
             if (vSession.isSessionManager()) {
                 if ((reSelect || reSelectThis) && current) {
                     photo.checked = true ;
                 }
             }
             XmlDetails details = new XmlDetails();
             details.photoId = new XmlPhotoId(enrPhoto.getId());
             if (vSession.directFileAccess())
                 details.photoId.path = enrPhoto.getPath(true) ;
             details.description = enrPhoto.getDescription();
             //tags de cette photo
             details.tag_used = webPageService.displayListIBTD(Mode.TAG_USED, vSession, enrPhoto,
                     Box.NONE, enrPhoto.getAlbum().getDate());
             
             details.albumId = enrPhoto.getAlbum().getId();
             if (rq.type == TypeRequest.TAG) {
                 details.albumName = enrPhoto.getAlbum().getNom();
                 details.albumDate = enrPhoto.getAlbum().getDate();
             }
             details.stars = enrPhoto.getStars();
             //liste des utilisateurs pouvant voir cette photo
             if (vSession.isSessionManager()) {
                 String name;
                 boolean outside;
                 Utilisateur enrUser = userDAO.find(enrPhoto.getDroit());
                 if (enrUser != null) {
                     name = enrUser.getNom();
                     outside = false;
                 } else {
                     name = userDAO.loadUserOutside(enrPhoto.getAlbum().getId()).getNom();
                     outside = true;
                 }
                 details.user = new XmlPhotoAlbumUser(name, outside);
             }
             photo.details = details ;
             if (enrPhoto.getTagAuthor() != null) {
                 Tag enrAuthor = enrPhoto.getTagAuthor();
                 photo.author = new XmlWebAlbumsTagWho();
                 photo.author.name = enrAuthor.getNom();
                 photo.author.id = enrAuthor.getId();
                 if (enrAuthor.getPerson() != null)
                     photo.author.contact = enrAuthor.getPerson().getContact();
             }
             photo.exif = photoUtil.getXmlExif(enrPhoto) ;
             
             output.photo.add(photo);
             current = false;
         }
 
         if (!submitted) {
             output.submit = submit ;
         }
 
         if (vSession.isSessionManager()) {
             XmlPhotoMassEdit massEdit = new XmlPhotoMassEdit();
             massEdit.tag_used = webPageService.displayListBN(Mode.TAG_USED, vSession,
                     Box.LIST, "newTag");
             massEdit.tag_never = webPageService.displayListBN(Mode.TAG_NEVER_EVER, vSession,
                     Box.LIST, "newTag");
             if (massEditParam) {
                 String msg;
                 if (countME == 0 || Turn.NOTHING == turn) {
                     msg = "Aucune modification faite ... ?";
                 } else {
                     msg = "" + countME + " photo"
                             + (countME == 1 ? " a été modifiée" : "s ont été modifées");
                 }
                 massEdit.message = msg ;
             }
             output.massEdit = massEdit ;
         }
         output.page = webPageService.xmlPage(thisPage, bornes);
         if (countME == 0) {
             stopWatch.stop() ;
         } else {
             stopWatch.stop(stopWatch.getTag(), ""+countME+" photos modified") ;
 
         }
         return output ;
     }
 
     @Override
     public XmlPhotoRandom treatRANDOM(ViewSession vSession) throws WebAlbumsServiceException {
 
         Photo enrPhoto = photoDAO.loadRandom(vSession);
         if (enrPhoto == null) return null ;
         XmlPhotoRandom output = new XmlPhotoRandom() ;
         
         output.details = transformToDetails(vSession, enrPhoto) ;
         return output ;
     }
 
     private XmlDetails transformToDetails(ViewSession vSession, Photo enrPhoto) throws WebAlbumsServiceException {
         XmlDetails details = new XmlDetails();
         details.photoId = new XmlPhotoId(enrPhoto.getId());
         if (vSession.directFileAccess())
             details.photoId.path = enrPhoto.getPath(true) ;
         details.description = enrPhoto.getDescription();
         details.albumName = enrPhoto.getAlbum().getNom();
         details.albumDate = enrPhoto.getAlbum().getDate();
         //tags de cette photo
         details.tag_used = webPageService.displayListIBTD(Mode.TAG_USED, vSession, 
                              enrPhoto, Box.NONE, enrPhoto.getAlbum().getDate());
         details.albumId = enrPhoto.getAlbum().getId();
         details.stars = enrPhoto.getStars();
         return details ;
     }
 
     public XmlPhotoAbout treatABOUT(ViewSessionPhoto vSession) throws WebAlbumsServiceException {
         Photo enrPhoto = photoDAO.loadIfAllowed(vSession, vSession.getId());
         if(enrPhoto == null) return null ;
 
         XmlPhotoAbout output = new XmlPhotoAbout() ;
         output.details = transformToDetails(vSession, enrPhoto) ;
         
         return output ;
     }
 
     public XmlPhotoFastEdit treatFASTEDIT(ViewSessionPhotoFastEdit vSession) {
         XmlPhotoFastEdit output = new XmlPhotoFastEdit();
         output.desc_status = null;
         output.tag_status = null;
         
         Integer id = vSession.getId();
         Photo enrPhoto = photoDAO.find(id);
         if (enrPhoto == null) {
             output.desc_msg = "No photo found for id="+id;
             output.desc_status = XmlPhotoFastEdit.Status.ERROR;
             output.tag_status = XmlPhotoFastEdit.Status.ERROR;
             return output;
         }
         
         String desc = vSession.getDesc();
         if (desc != null) {
             enrPhoto.setDescription(desc);
             output.desc_status = XmlPhotoFastEdit.Status.OK;
         }
         
         
         Integer tagId = vSession.getTag();
         Tag enrTag = tagDAO.find(tagId);
         if (enrTag != null) {
             try {
                 TagAction action = vSession.getTagAction();
                 if (action == null || action == TagAction.ADD) 
                     photoUtil.addTags(enrPhoto, new Integer []{tagId});
                 else
                     photoUtil.removeTag(enrPhoto, tagId);
                 output.tag_status = XmlPhotoFastEdit.Status.OK;
             } catch (WebAlbumsServiceException ex) {
                 output.tag_msg = ex.getMessage();
                 output.tag_status = XmlPhotoFastEdit.Status.ERROR;
             }
         }
         
         Integer stars = vSession.getStars();
         if (stars != null) {
             enrPhoto.setStars(stars);
             output.stars_status = XmlPhotoFastEdit.Status.OK;
         }
         
         return output;
     }
 }
