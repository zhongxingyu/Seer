 package net.wazari.service.engine;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.util.StatusPrinter;
 import java.io.InputStream;
 import java.text.ParseException;
 import net.wazari.service.exchange.xml.common.XmlFrom;
 import net.wazari.service.exchange.xml.common.XmlLoginInfo;
 import java.security.Principal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 import net.wazari.dao.TagFacadeLocal;
 import net.wazari.dao.TagPhotoFacadeLocal;
 import net.wazari.dao.TagThemeFacadeLocal;
 import net.wazari.dao.UtilisateurFacadeLocal;
 import net.wazari.dao.entity.*;
 
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.service.WebPageLocal;
 
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession;
 import net.wazari.service.exchange.ViewSession.Box;
 import net.wazari.service.exchange.ViewSession.EditMode;
 import net.wazari.service.exchange.ViewSession.Mode;
 import net.wazari.service.util.google.GooglePoint;
 import net.wazari.service.util.google.GooglePoint.Point;
 
 import net.wazari.dao.ThemeFacadeLocal;
 import net.wazari.dao.entity.facades.EntityWithId;
 import net.wazari.service.UserLocal;
 import net.wazari.service.exchange.ViewSessionLogin;
 import net.wazari.service.exchange.xml.XmlAffichage;
 import net.wazari.service.exchange.xml.XmlPage;
 import net.wazari.service.exchange.xml.common.XmlDate;
 import net.wazari.service.exchange.xml.common.XmlUser;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList.XmlWebAlbumsTagWhat;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList.XmlWebAlbumsTagWhere;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList.XmlWebAlbumsTagWho;
 import net.wazari.service.exchange.xml.common.XmlUserList;
 import net.wazari.service.exchange.xml.common.XmlWebAlbumsList.ListType;
 import net.wazari.service.exchange.xml.tag.XmlTag;
 import net.wazari.util.system.SystemTools;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 
 @Stateless
 public class WebPageBean implements WebPageLocal {
 
     @EJB
     private TagThemeFacadeLocal tagThemeDAO;
     @EJB
     private UtilisateurFacadeLocal userDAO;
     @EJB
     private TagPhotoFacadeLocal tagPhotoDAO;
     @EJB
     private TagFacadeLocal tagDAO;
     @EJB
     private ThemeFacadeLocal themeDAO;
 
     private static final long serialVersionUID = -8157612278920872716L;
     private static final Logger log = LoggerFactory.getLogger(WebPageBean.class.getName());
 
     static {
         log.warn( "FilesFinder.initialized {}", SystemTools.initate());
         log.warn("Loading WebAlbums3-Service-Beans");
         
         LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
     
         try {
           JoranConfigurator configurator = new JoranConfigurator();
           configurator.setContext(lc);
           // the context was probably already configured by default configuration rules
           lc.reset(); 
           InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logback.xml") ;
           
           configurator.doConfigure(stream);
         } catch (JoranException je) {
            je.printStackTrace();
         }
         StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
     }
 
     @Override
     public EditMode getNextEditionMode(ViewSession vSession) {
         EditMode editionMode = vSession.getEditionMode();
         EditMode next;
         if (editionMode == EditMode.VISITE) {
             next = EditMode.NORMAL;
         } else if (editionMode == EditMode.NORMAL) {
             next = EditMode.EDITION;
         } else if (editionMode == EditMode.EDITION) {
             next = EditMode.VISITE;
         } else {
             next = EditMode.NORMAL;
         }
         return next;
     }
 
     //try to get the 'asked' element
     //or the page 'page' if asked is null
     //go to the first page otherwise
     @Override
     public Bornes calculBornes(Integer page,
                                int taille) {
         Bornes bornes;
         if (page != null) {
             bornes = new Bornes(taille, page);
         } else {
             bornes = new Bornes(taille, 0);
         }
 
         return bornes;
     }
 
     private static final int NB_PAGES_BEF_AFT = 3 ;
     @Override
     public XmlPage xmlPage(XmlFrom from, Bornes bornes) {
         int current = bornes.getCurrentPage() ;
         int last = bornes.getLastPage() ;
         XmlPage page = new XmlPage();
         page.description = "Page 0 .. " +current + " .." + last ;
         page.url = from ;
         int start = Math.max(0, current - NB_PAGES_BEF_AFT);
         int stop = Math.min(current + NB_PAGES_BEF_AFT+1, last);
         if (start >= 2) {
             page.first = 0;
         } else if (start == 1) {
             page.prev.add(0) ;
         }
         for (int i = start; i < stop; i++) {
             if (i == current ||current == -1 && i == 0) {
                 page.current = i;
             } else if (i < current) {
                 page.prev.add(i);
             } else {
                 page.next.add(i);
             }
         }
         if (stop == bornes.getLastPage() - 1) {
             page.next.add(last - 1) ;
         } else if (stop != last) {
             page.last = last - 1 ;
         }
 
         if (current != 0) {
             page.previ = current -1 ;
         }
 
         if (last != 0 && current != last -1) {
             page.nexti = current +1 ;
         }
         
         return page;
     }
 
     @Override
     public XmlLoginInfo xmlLogin(ViewSessionLogin vSession) {
         XmlLoginInfo login = new XmlLoginInfo();
 
         Theme enrTheme = vSession.getTheme();
         Utilisateur enrUtil = vSession.getUser() ;
         Principal principal = vSession.getUserPrincipal() ;
         login.theme = (enrTheme == null ? null : enrTheme.getNom());
         login.themeid = (enrTheme == null ? null : enrTheme.getId());
         if (enrUtil != null) {
             login.role = enrUtil.getNom() ;
         }
         if (principal != null) {
             login.user = principal.getName();
         }
         log.info( "logged as manager? {}", vSession.isSessionManager());
         if (vSession.isSessionManager() && !vSession.getStatic()) {
             login.admin = true ;
         }
         log.info( "logged as admin? {}", vSession.isAdminSession());
         log.info( "logged as root? {}", vSession.isRootSession());
         if (vSession.isRootSession()) {
             login.root = true ;
         }
 
         return login;
     }
 
     @Override
     public XmlAffichage xmlAffichage(ViewSession vSession) {
         XmlAffichage affichage = new XmlAffichage();
 
         if (!vSession.getConfiguration().isPathURL() &&
             (vSession.isRootSession() ||
             vSession.getTheme() != null && vSession.getTheme().getBackground() != null)) {
             affichage.background = true  ;
         }
         if (vSession.isRemoteAccess()) affichage.remote = true ;
         
         affichage.photoAlbumSize = vSession.getPhotoAlbumSize();
         affichage.statik = vSession.getStatic() ? true : null;
         affichage.direct_access = vSession.directFileAccess() ? true : null;
         if (affichage.direct_access != null && affichage.direct_access) {
             affichage.mini_folder = vSession.getConfiguration().getMiniPath(false);
             affichage.photo_folder = vSession.getConfiguration().getImagesPath(false);
         }
         return affichage;
     }
 
     //display a list into STR
     //according to the MODE
     //and the information found in REQUEST.
     //List is made up of BOX items
     //and is named NAME
     //if type is PHOTO, info (MODE) related to the photo #ID are put in the list
     //if type is ALBUM, info (MODE) related to the album #ID are put in the list
     @Override
     public XmlWebAlbumsList displayListIBTN(Mode mode,
             ViewSession vSession,
             EntityWithId entity,
             Box box,
             String name)
             throws WebAlbumsServiceException
     {
 
         ListType type = ListType.UNKNOWN;
         List<TagPhoto> list = null;
         if (entity instanceof Photo) {
             list = ((Photo) entity).getTagPhotoList();
             type = ListType.PHOTO;
         } else if (entity instanceof Album) {
             list = tagPhotoDAO.queryByAlbum((Album) entity);
             type = ListType.ALBUM;
         } else if (entity instanceof Carnet) {
             list = tagPhotoDAO.queryByCarnet((Carnet) entity);
             type = ListType.CARNET;
         }
         List<Tag> tags = new ArrayList<Tag>(list.size());
         for (TagPhoto enrTagPhoto : list) {
             tags.add(enrTagPhoto.getTag());
         }
         XmlWebAlbumsList output = displayListLBN(mode, vSession, tags, box, name) ;
 
         output.box = box ;
         output.type = type ;
         output.id = entity.getId() ;
         output.mode = mode ;
 
         return output ;
     }
 
     //display a list into STR
     //according to the MOXmlBuilderDE
     //and the information found in REQUEST.
     //List is made up of BOX items
     //and is named NAME
     //Only IDS are added to the list
     //Mode specific information can be provide throug info (null otherwise)
     //(used by Mode.MAP for the link to the relevant address)
     @Override
     public XmlWebAlbumsList displayListLBN(Mode mode,
             ViewSession vSession,
             List<Tag> ids,
             Box box,
             String name)
             throws WebAlbumsServiceException 
     {
         StopWatch stopWatch = new Slf4JStopWatch("Service.displayListLBNI", log) ;
         List<Tag> tags = null;
 
         XmlWebAlbumsList output = new XmlWebAlbumsList();
         boolean geoOnly = mode == Mode.TAG_GEO;
         //affichage de la liste des tags où il y aura des photos
         if (!vSession.isSessionManager()) {
             if (mode != Mode.TAG_USED && mode != Mode.TAG_GEO) {
                 throw new RuntimeException("Don't want to process mode " + mode + " when not logged at manager");
             }
             log.info( "Load visible tags (only for geo?{})", geoOnly);
             tags = tagDAO.loadVisibleTags(vSession, geoOnly);
         } else /* current manager*/ {
 
             if (mode == Mode.TAG_USED || mode == Mode.TAG_GEO) {
                 log.info( "Load visible tags (only for geo?{})", geoOnly);
                 tags = tagDAO.loadVisibleTags(vSession, geoOnly);
             } else if (mode == Mode.TAG_ALL) {
                 log.info( "Load all tags");
                 //afficher tous les tags
                 tags = tagDAO.findAll();
             } else if (mode == Mode.TAG_NUSED || mode == Mode.TAG_NEVER) {
                 List<Tag> notWantedTags = null;
                 //select the tags not used [in this theme]
                 if (mode == Mode.TAG_NEVER || vSession.isRootSession()) {
                     //select all the tags used
                     log.info( "Select disting tags");
                     notWantedTags = tagPhotoDAO.selectDistinctTags();
 
                 } else /* TAG_NUSED*/ {
                     //select all the tags used in photo of this theme
                     log.info( "Select not used tags");
                     notWantedTags = tagPhotoDAO.selectUnusedTags(vSession);
                 }
                 log.info( "Select no such tags");
                 tags = tagDAO.getNoSuchTags(vSession, notWantedTags);
 
             } else /* not handled mode*/ {
                 output.exception = "Unknown handled mode :" + mode ;
                 return output ;
             }
         } /* isManagerSession */
 
         output.mode = mode ;
 
         GooglePoint map = null;
         if (box == Box.MAP_SCRIPT) {
             map = new GooglePoint();
         }
 
         log.info( "Mode: {}, Box: {}, list: {}", new Object[]{mode, box, ids});
 
         for (Tag enrTag : tags) {
             XmlTag tag = new XmlTag();
             Tag tagId = null;
             String nom = null;
             Point p = null;
             Integer photoId = null;
             String photoPath = null;
 
             //first, prepare the information (type, id, nom)
             if (box == Box.MAP_SCRIPT) {
                 if (enrTag.getTagType() == 3) {
                     //ensure that this tag is displayed in this theme
                     //(in root theme, diplay all of theme)
                     TagTheme enrTagTh = tagThemeDAO.loadByTagTheme(enrTag.getId(), vSession.getTheme().getId());
                     if (enrTagTh != null && !enrTagTh.getIsVisible()) {
                         //Root session can see all the tags, otherwise restrict
                         if (!vSession.isRootSession()) {
                             continue;
                         }
                     }
 
                     //get its geoloc
                     Geolocalisation enrGeo = enrTag.getGeolocalisation();
                     if (enrGeo != null) {
                         tagId = enrTag;
                         //Get the photo to display, if any
                         if (enrTagTh != null && enrTagTh.getPhoto() != null) {
                             photoId = enrTagTh.getPhoto().getId();
                             photoPath = vSession.getTheme().getNom() + "/"+enrTagTh.getPhoto().getPath();
                         }
                         p = new Point(enrTag.getNom(), enrTag.getId(),
                                 enrGeo.getLat(),
                                 enrGeo.getLongitude(),
                                 photoId, photoPath);
                         nom = enrTag.getNom();
                     }
                 }
             } else if (box == Box.MAP) {
             } else {
                 tagId = enrTag;
                 nom = enrTag.getNom();
 
                 switch (enrTag.getTagType()) {
                     case 1: tag = new XmlWebAlbumsTagWho(); break ;
                     case 2: tag = new XmlWebAlbumsTagWhat(); break;
                     case 3: tag = new XmlWebAlbumsTagWhere(); break ;
                     default: throw new RuntimeException("Unkown tag type "+enrTag.getNom()+"->"+enrTag.getTagType()) ;
                 }
             }
             //display the value [if in ids][select if in ids]
             if (box == Box.MAP_SCRIPT) {
                 if (nom != null && (ids == null || ids.contains(tagId))) {
                     map.addPoint(p);
                 }
             } else if (box == Box.MAP) {
             } else {
                 boolean selected = false;
                 boolean written = true;
                 if (ids != null) {
                     if (box == Box.MULTIPLE) {
                         if (ids.contains(tagId)) {
                             selected = true;
                         }
                     } else if (!ids.contains(tagId)) {
                         written = false;
                     }
                 }
                 if (written) {
                     if (tag instanceof XmlWebAlbumsTagWho && enrTag.getPerson() != null) {
                         if (enrTag.getPerson().getBirthdate() != null && !"".equals(enrTag.getPerson().getBirthdate()))
                             ((XmlWebAlbumsTagWho) tag).birthdate = enrTag.getPerson().getBirthdate();
                     }
                     tag.name = nom ;
                     tag.id = tagId.getId() ;
                    tag.checked = selected ;
                     tag.minor = tagId.isMinor();
                     output.addTag(tag) ;
                 }
             }
         } /* while loop*/
 
         if (box == Box.MAP_SCRIPT) {
             output.blob = map.getJSon();
         }
 
         stopWatch.stop() ;
         return output ;
     }
 
     @Override
     public XmlUserList displayListDroit(Utilisateur right, Integer albmRight)
             throws WebAlbumsServiceException {
 
         StopWatch stopWatch = new Slf4JStopWatch("Service.displayListDroit", log) ;
 
         XmlUserList output = new XmlUserList();
 
         List<Utilisateur> lstUsr = userDAO.findAll();
         for (Utilisateur enrUser : lstUsr) {
 
             String name = enrUser.getNom();
             Integer id = enrUser.getId();
             boolean selected = false;
 
             if (albmRight != null && albmRight.equals(enrUser.getId())) {
                 name = "[" + name + "]";
                 id = null;
 
                 if (right == null) {
                     selected = true;
                 }
             } else if (right != null && right.getId().equals(enrUser.getId())) {
                 selected = true;
             }
 
             XmlUser user = new XmlUser();
             user.name = name ;
             user.id = id ;
             if (selected) {
                 user.selected = true ;
             }
             output.user.add(user);
         }
         stopWatch.stop();
         return output ;
     }
 
     @Override
     public XmlWebAlbumsList displayMapInScript(ViewSession vSession)
             throws WebAlbumsServiceException {
         return displayListLBN(Mode.TAG_USED, vSession, null, Box.MAP_SCRIPT, null);
     }
 
     //display a list into STR
     //according to the MODE
     //and the information found in the REQUEST.
     //List made up of BOX items,
     //and is named NAME
     @Override
     public XmlWebAlbumsList displayListBN(Mode mode,
             ViewSession vSession,
             Box box,
             String name)
             throws WebAlbumsServiceException {
         return displayListLBN(mode, vSession, null, box, name);
     }
     //display a list into STR
     //according to the MODE
     //and the information found in REQUEST.
     //List is made up of BOX items
     //and is named with the default name for this MODE
 
     @Override
     public XmlWebAlbumsList displayListB(Mode mode,
             ViewSession vSession,
             Box box)
             throws WebAlbumsServiceException {
         return displayListLBN(mode, vSession, null, box, "tags");
     }
 
     @Override
     public XmlWebAlbumsList displayListIBTD(Mode mode,
             ViewSession vSession,
             EntityWithId entity,
             Box box, String date)
             throws WebAlbumsServiceException {
         XmlWebAlbumsList lst = displayListIBTN(mode, vSession, entity, box, null);
         SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
         Date ref;
         try {
             ref = parser.parse(date);
         } catch (ParseException ex) {
             log.warn("Invalid date provided: {}", date);
             return lst;
         }
         
         Calendar dob = Calendar.getInstance();
         Calendar day = Calendar.getInstance();  
         day.setTime(ref);
         int age ;
         for (XmlWebAlbumsTagWho person: lst.who) {
             if (person.birthdate != null) {
                 try {
                     Date birth = parser.parse(person.birthdate);
                     dob.setTime(birth);
                     age = day.get(Calendar.YEAR) - dob.get(Calendar.YEAR);  
                     if (day.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))  
                         age--;
                     person.birthdate = Integer.toString(age);
                 } catch (ParseException ex) {
                     log.warn("Invalid birth date for tag {}: {}", person.name, person.birthdate);
                 }
             }
         }
         return lst;
     }
     //display a list into STR
     //according to the MODE
     //and the information found in REQUEST.
     //List is made up of BOX items
     //and is named with the default name for this MODE
     //if type is PHOTO, info (MODE) related to the photo #ID are put in the list
     //if type is ALBUM, info (MODE) related to the album #ID are put in the list
 
     @Override
     public XmlWebAlbumsList displayListIBT(Mode mode,
             ViewSession vSession,
             EntityWithId entity,
             Box box)
             throws WebAlbumsServiceException {
         return displayListIBTN(mode, vSession, entity, box, null);
     }
     
     //display a list into STR
     //according to the MODE
     //and the information found in REQUEST.
     //List is made up of BOX items
     //and is filled with the IDs
     @Override
     public XmlWebAlbumsList displayListLB(Mode mode,
             ViewSession vSession,
             List<Tag> ids,
             Box box)
             throws WebAlbumsServiceException {
         return displayListLBN(mode, vSession, ids, box, "tags");
     }
 
     @Override
     public void populateEntities() {
         log.warn("Database empty, creating Root theme and Users");
         themeDAO.newTheme(ThemeFacadeLocal.THEME_ROOT_ID, ThemeFacadeLocal.THEME_ROOT_NAME) ;
         userDAO.newUser(1, UserLocal.USER_ADMIN);
         userDAO.newUser(2, UserLocal.USER_FAMILLE);
         userDAO.newUser(3, UserLocal.USER_AMIS);
         userDAO.newUser(4, UserLocal.USER_PUBLIC);
     }
 
     private static final SimpleDateFormat annee = new SimpleDateFormat("yyyy");
     private static final SimpleDateFormat mois = new SimpleDateFormat("MMMM");
     private static final SimpleDateFormat jour = new SimpleDateFormat("dd");
 
 
     public XmlDate xmlDate(String strNewDate, String strOldDate) {
         XmlDate temps = new XmlDate();
         try {
             Date newDate = new SimpleDateFormat("yyyy-MM-dd").parse(strNewDate);
 
             if (strOldDate == null) {
                 temps.year = annee.format(newDate);
                 temps.month = mois.format(newDate);
                 temps.day = jour.format(newDate);
             } else {
                 Date oldDate = new SimpleDateFormat("yyyy-MM-dd").parse(strOldDate);
 
                 if (!annee.format(oldDate).equals(annee.format(newDate))) {
                     temps.year = annee.format(newDate);
                     temps.month = mois.format(newDate);
                     temps.day = jour.format(newDate);
                 } else if (!mois.format(oldDate).equals(mois.format(newDate))) {
 
                     temps.month = mois.format(newDate);
                     temps.day = jour.format(newDate);
 
                     // 1 jour = 86 400 secondes
                 } else if (!jour.format(oldDate).equals(jour.format(newDate))) {
                     temps.day = jour.format(newDate);
 
                 } else {
                     //nothing to display
                 }
             }
         } catch (Exception e) {
             temps.year = "x";
             temps.month = "xx";
             temps.day = "xx";
         }
 
         return temps;
     }
     
 }
