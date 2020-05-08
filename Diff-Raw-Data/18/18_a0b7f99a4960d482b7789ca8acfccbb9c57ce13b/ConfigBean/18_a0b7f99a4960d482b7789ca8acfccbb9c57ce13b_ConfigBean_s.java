 package net.wazari.service.engine;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 import net.wazari.dao.GeolocalisationFacadeLocal;
 import net.wazari.dao.PersonFacadeLocal;
 import net.wazari.dao.TagFacadeLocal;
 import net.wazari.dao.TagPhotoFacadeLocal;
 import net.wazari.dao.TagThemeFacadeLocal;
 import net.wazari.util.system.FilesFinder;
 import net.wazari.dao.ThemeFacadeLocal;
 
 import net.wazari.dao.entity.*;
 
 import net.wazari.service.ConfigLocal;
 import net.wazari.service.exchange.ViewSessionConfig;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.xml.config.XmlConfigDelTag;
 import net.wazari.service.exchange.xml.config.XmlConfigDelTheme;
 import net.wazari.service.exchange.xml.config.XmlConfigImport;
 import net.wazari.service.exchange.xml.config.XmlConfigLinkTag;
 import net.wazari.service.exchange.xml.config.XmlConfigModGeo;
 import net.wazari.service.exchange.xml.config.XmlConfigModPers;
 import net.wazari.service.exchange.xml.config.XmlConfigModTag;
 import net.wazari.service.exchange.xml.config.XmlConfigModVis;
 import net.wazari.service.exchange.xml.config.XmlConfigNewTag;
 
 @Stateless
 public class ConfigBean implements ConfigLocal {
 
     private static final long serialVersionUID = -628341734743684910L;
     @EJB
     private TagFacadeLocal tagDAO;
     @EJB
     private GeolocalisationFacadeLocal geoDAO;
     @EJB
     private PersonFacadeLocal personDAO;
     @EJB
     private TagThemeFacadeLocal tagThemeDAO;
     @EJB
     private TagPhotoFacadeLocal tagPhotoDAO;
     @EJB
     private ThemeFacadeLocal themeDAO;
     @EJB
     private FilesFinder filesFinder;
 
     @Override
     public XmlConfigImport treatIMPORT(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigImport output = new XmlConfigImport();
         String theme = vSession.getImportTheme();
 
         boolean correct = filesFinder.importAuthor(vSession, theme, vSession.getConfiguration());
 
         if (correct) {
             output.message = "Well done !" ;
         } else {
             output.exception = "An error occured ..." ;
         }
 
         return output ;
     }
 
     @Override
     public XmlConfigModTag treatMODTAG(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigModTag output = new XmlConfigModTag ();
 
         Integer tag = vSession.getTag();
 
         if (tag == null || tag == -1) {
             output.exception = "Pas de tag selectionné ..." ;
             return output;
         }
 
         Tag enrTag = tagDAO.find(tag);
         if (tag == null) {
             output.exception = "Le Tag #" + tag + " n'est pas dans la base ...";
             return output;
         }
         
         String newName = vSession.getNouveau();
         String oldName = enrTag.getNom();
         if (newName != null && !newName.equals(oldName)) {
             enrTag.setNom(newName);
             output.newName = newName ;
         }
         
         Boolean isMinor = vSession.getMinor();
         Boolean oldIsMinor = enrTag.isMinor();
         if (oldIsMinor != null && oldIsMinor != isMinor) {
             if (!isMinor)
                 isMinor = null;
             enrTag.setMinor(isMinor);
             output.newMinor = isMinor != null && isMinor;
         }
         
         
         tagDAO.edit(enrTag);
         
 
         return output ;
     }
 
     @Override
     public XmlConfigModGeo treatMODGEO(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigModGeo output = new XmlConfigModGeo();
 
         String lng = vSession.getLng();
         String lat = vSession.getLat();
         Integer tag = vSession.getTag();
 
         if (tag == null || tag == -1) {
             output.exception = "Pas de tag selectionné ..." ;
             return output ;
         }
 
         Tag enrTag = tagDAO.find(tag);
         if (enrTag == null) {
             output.exception = "La localisation " + tag + " ne correspond à aucun tag ..." ;
             return output ;
         }
 
         if (lng == null || lat == null) {
             output.exception = "La geoloc " + lng + "/" + lat + " n'est pas correcte..." ;
             return output;
         }
 
         Geolocalisation enrGeo = enrTag.getGeolocalisation();
         enrGeo.setLongitude(lng);
         enrGeo.setLat(lat);
         geoDAO.edit(enrGeo);
 
         output.newLngLat = lng + "/" + lat ;
 
         return output ;
     }
     
     @Override
     public XmlConfigModPers treatMODPERS(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigModPers output = new XmlConfigModPers();
 
         String birthdate = vSession.getBirthdate();
         String contact = vSession.getContact();
         Integer tag = vSession.getTag();
 
         if (tag == null || tag == -1) {
             output.exception = "Pas de tag selectionné ..." ;
             return output ;
         }
 
         Tag enrTag = tagDAO.find(tag);
         if (enrTag == null) {
             output.exception = "L'id " + tag + " ne correspond à aucun tag ..." ;
             return output ;
         }
         
         if (enrTag.getTagType() != 1) {
             output.exception = "Le " + tag + " ne correspond à une personne ..." ;
             return output ;
         }
 
         Person enrPerson = enrTag.getPerson();
         if (enrPerson == null) {
             enrPerson = personDAO.newPerson(enrTag);
             personDAO.create(enrPerson);
         }
        
         String oldBirthdate = enrPerson.getBirthdate() ;
         if (birthdate != null && !birthdate.equals("") && !birthdate.equals(oldBirthdate)) {
             try {
                 SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
                 parser.parse(birthdate);
             } catch (ParseException e) {
                 output.exception = "La date " + birthdate + " n'est pas correcte..." ;
                 return output;
             }
             
             enrPerson.setBirthdate(birthdate);
             output.newBirthdate = birthdate ;
         } 
         
         String oldContact = enrPerson.getContact();
        log.warn("----->"+contact);
         if (contact != null && !contact.equals("") && !contact.equals(oldContact)) {
             enrPerson.setContact(contact);
             output.newContact = contact ;
        } 
        
         
         personDAO.edit(enrPerson);
         

         return output ;
     }
 
     @Override
     public XmlConfigModVis treatMODVIS(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigModVis output = new XmlConfigModVis();
 
         if (vSession.isRootSession()) {
             output.exception = "Impossible to change visibility on the root session" ;
             return output ;
         }
 
         Integer tag = vSession.getTag();
         Boolean visible = vSession.getVisible();
 
         if (tag == null || tag == -1) {
             output.exception = "Pas de tag selectionné ...";
             return output ;
         }
         TagTheme enrTagTheme = tagThemeDAO.loadByTagTheme(tag, vSession.getTheme().getId());
 
         if (enrTagTheme == null) {
 
             if (tagDAO.find(tag) == null) {
                 output.exception = "Impossible de trouver ce tag (" + tag + ") ..." ;
                 return output;
             }
 
             enrTagTheme = tagThemeDAO.newTagTheme();
             enrTagTheme.setTheme(vSession.getTheme());
             enrTagTheme.setTag(tagDAO.find(tag));
 
             tagThemeDAO.create(enrTagTheme);
         }
         if (visible != null && visible) {
             enrTagTheme.setIsVisible(true);
         } else {
             enrTagTheme.setIsVisible(false);
         }
         tagThemeDAO.edit(enrTagTheme);
         output.message = "Le tag " + tag + " est maintenant : " + (visible ? "visible" : "invisible");
 
         return output ;
     }
 
     @Override
     public XmlConfigNewTag treatNEWTAG(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigNewTag output = new XmlConfigNewTag();
         String nom = vSession.getNom();
         Integer type = vSession.getType();
 
         if (type == null || 0 > type || type > 3) {
             log.warn("Invalid type: ", type);
             output.exception = "Pas de type selectionné ...";
             return output ;
         }
      
         if (nom == null || nom.isEmpty()) {
             output.exception = "Le nom du tag est vide ..." ;
 
             return output ;
         }
         String msg = "";
         String liste = "";
 
         Tag enrTag = tagDAO.loadByName(nom);
         if (enrTag != null) {
             log.warn("The tag '{}' already exists ", nom);
             output.exception = "Le Tag " + nom + " est déjà présent dans la base ..." + enrTag.getId() + " - " + enrTag.getNom();
 
             return output ;
         }
         
         enrTag = tagDAO.newTag();
 
         enrTag.setNom(nom);
         enrTag.setTagType(type);
         tagDAO.create(enrTag);
         output.message = "TAG == " + enrTag.getId() + " ==" ;
         if (type == 3) {
             String longit = vSession.getLng();
             String lat = vSession.getLat();
             msg = " (" + longit + "/" + lat + ")";
             if (longit == null || lat == null) {
                 log.warn("Invalid geoloc: {}", msg) ;
                 output.exception = "La geoloc " + msg + " n'est pas correcte..." ;
                 tagDAO.remove(enrTag);
 
                 return output ;
             }
 
             Geolocalisation geo = geoDAO.newGeolocalisation();
             geo.setTag(enrTag.getId());
             geo.setLongitude(longit);
             geo.setLat(lat);
             geo.setTag1(enrTag);
             geoDAO.create(geo);
         }
 
         switch (type) {
             case 1:
                 liste = "Who";
                 break;
             case 2:
                 liste = "What";
                 break;
             case 3:
                 liste = "Where";
                 break;
         }
         output.message = "Tag '" + nom + msg + "' correctement ajouté "
                 + "à la liste " + liste ;
         
         Integer parentId = vSession.getParentTag();
         Tag enrParentTag = tagDAO.find(parentId);
         if (enrParentTag != null 
                 && isParentalityAllowed(enrParentTag, enrTag)) {
             enrTag.setParent(enrParentTag);
             output.message += " avec '"+enrParentTag.getNom()+"' comme parent." ;
         }
 
                 
 
         return output ;
     }
 
     @Override
     public XmlConfigLinkTag treatLINKTAG(ViewSessionConfig vSession) {
         XmlConfigLinkTag output = new XmlConfigLinkTag();
 
         Integer parentId = vSession.getParentTag();
         Integer[] sonIds = vSession.getSonTags();
 
         if (parentId == null || sonIds == null)
         {
             output.exception = "Pas de tag selectionné ..." ;
             return output ;
         }
 
         Tag enrParentTag = tagDAO.find(parentId);
 
         for (Integer sonId : sonIds) {
             Tag enrSonTag = tagDAO.find(sonId);
             if (enrParentTag == null)
                 enrSonTag.setParent(null);
             else if(isParentalityAllowed(enrParentTag, enrSonTag)) {
                 enrSonTag.setParent(enrParentTag);
             }
         }
         output.message = "Tags correctement affiliés" ;
         return output ;
     }
 
     private static boolean isParentalityAllowed(Tag enrParent, Tag enrSon) {
         if (enrSon.getId().equals(enrParent.getId())) return false ;
         else if (enrParent.getParent() == null) return true ;
         else return isParentalityAllowed(enrParent.getParent(), enrSon) ;
     }
 
 
     @Override
     public XmlConfigDelTag treatDELTAG(ViewSessionConfig vSession)
             throws WebAlbumsServiceException {
         XmlConfigDelTag output = new XmlConfigDelTag();
 
         int tagID = vSession.getTag();
 
         try {
             Tag enrTag = tagDAO.find(tagID);
             //liens Tag->Photos
             List<TagPhoto> lstTP = enrTag.getTagPhotoList();
             int i = 0;
             for (TagPhoto enrTagPhoto : lstTP) {
                 tagPhotoDAO.remove(enrTagPhoto);
                 i++;
             }
             output.message = "Suppression de " + i + " Tags Photo" ;
 
             //liens Tag->Localisation
             i = 0;
             Geolocalisation enrGeo = enrTag.getGeolocalisation();
             if (enrGeo != null) {
                 geoDAO.remove(enrGeo);
                 i = 1;
             }
             output.message += "Suppression de " + i + " Geolocalisation" ;
 
             //liens Tag->Theme
             List<TagTheme> lstTT = enrTag.getTagThemeList();
 
             i = 0;
             for (TagTheme enrTagTheme : lstTT) {
                 tagThemeDAO.remove(enrTagTheme);
                 i++;
             }
             output.message += "Suppression de " + i + " TagThemes" ;
 
             //tag
             i = 0;
             if (enrTag != null) {
                 tagDAO.remove(enrTag);
                 i++;
             }
             output.message += "Suppression de " + i + " Tag" ;
 
             return output ;
         } catch (NumberFormatException e) {
             output.exception = "Aucun tag selectionné ...";
             return output ;
         }
     }
 
     public XmlConfigDelTheme treatDELTHEME(ViewSessionConfig vSession) throws WebAlbumsServiceException {
         XmlConfigDelTheme output = new XmlConfigDelTheme() ;
         try {
             if (vSession.isRootSession()) {
                 output.message = "Impossible de supprimer le theme Root" ;
                 return output ;
             }
 
             themeDAO.remove(vSession.getTheme(), vSession.getConfiguration().wantsProtectDB());
             output.message = "Theme correctement supprimer" ;
             return output ;
         } catch (Exception e) {
             log.warn("error while removing the theme: {}", e);
             output.exception = "error while removing the theme: "+e.getMessage()  ;
             return output ;
         }
     }
 
     private static final Logger log = LoggerFactory.getLogger(ConfigBean.class.getName());
 }
