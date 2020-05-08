 package com.aetrion.flickr.photos;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.aetrion.flickr.people.User;
 import com.aetrion.flickr.util.XMLUtilities;
 import com.aetrion.flickr.tags.Tag;
 
 /**
  * Utilitiy-methods to transfer requested XML to Photo-objects.
  *
  * @author till, x-mago
 * @version $Id: PhotoUtils.java,v 1.7 2007/07/19 21:29:24 x-mago Exp $
  */
 public final class PhotoUtils {
 
     private PhotoUtils() {
     }
 
     /**
      * Transfer the Information of a photo from a DOM-object
      * to a Photo-object.
      *
      * @param photoElement
      * @return Photo
      */
     public static final Photo createPhoto(Element photoElement) {
         Photo photo = new Photo();
         photo.setId(photoElement.getAttribute("id"));
         photo.setSecret(photoElement.getAttribute("secret"));
         photo.setServer(photoElement.getAttribute("server"));
         photo.setFarm(photoElement.getAttribute("farm"));
         photo.setFavorite("1".equals(photoElement.getAttribute("isfavorite")));
         photo.setLicense(photoElement.getAttribute("license"));
         photo.setOriginalFormat(photoElement.getAttribute("originalformat"));
         photo.setOriginalSecret(photoElement.getAttribute("originalsecret"));
         photo.setIconServer(photoElement.getAttribute("iconserver"));
         photo.setIconFarm(photoElement.getAttribute("iconfarm"));
         // flickr.groups.pools.getPhotos provides this value!
         photo.setDateAdded(photoElement.getAttribute("dateadded"));
 
         // Searches, or other list may contain orginal_format.
         // If not choosen via extras, set jpg as default.
         if (photo.getOriginalFormat().equals("")) {
             photo.setOriginalFormat("jpg");
         }
 
         Element ownerElement = (Element) photoElement.getElementsByTagName("owner").item(0);
         if (ownerElement == null) {
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             owner.setUsername(photoElement.getAttribute("ownername"));
             photo.setOwner(owner);
             photo.setUrl("http://flickr.com/photos/" + owner.getId() + "/" + photo.getId());
         } else {
             User owner = new User();
             owner.setId(ownerElement.getAttribute("nsid"));
 
             String username = ownerElement.getAttribute("username");
             String ownername = ownerElement.getAttribute("ownername");
             // try to get the username either from the "username" attribute or
             // from the "ownername" attribute
             if (username != null && !"".equals(username)) {
                 owner.setUsername(username);
             } else if (ownername != null && !"".equals(ownername)) {
                 owner.setUsername(ownername);
             }
 
             owner.setUsername(ownerElement.getAttribute("username"));
             owner.setRealName(ownerElement.getAttribute("realname"));
             owner.setLocation(ownerElement.getAttribute("location"));
             photo.setOwner(owner);
             photo.setUrl("http://flickr.com/photos/" + owner.getId() + "/" + photo.getId());
         }
 
         photo.setTitle(XMLUtilities.getChildValue(photoElement, "title"));
         if (photo.getTitle() == null) {
             photo.setTitle(photoElement.getAttribute("title"));
         }
 
         photo.setDescription(XMLUtilities.getChildValue(photoElement, "description"));
 
         try {
        	// here the flags are set, if the photo is read by getInfo().
             Element visibilityElement = (Element) photoElement.getElementsByTagName("visibility").item(0);
             photo.setPublicFlag("1".equals(visibilityElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(visibilityElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(visibilityElement.getAttribute("isfamily")));
         } catch (NullPointerException e) {
            // these flags are set here, if photos read from a list.
            photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
            photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
            photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
         }
 
         // Parse either photo by getInfo, or from list
         try {
             Element datesElement = XMLUtilities.getChild(photoElement, "dates");
             photo.setDatePosted(datesElement.getAttribute("posted"));
             photo.setDateTaken(datesElement.getAttribute("taken"));
             photo.setTakenGranularity(datesElement.getAttribute("takengranularity"));
         } catch (NullPointerException e) {
             photo.setDateTaken(photoElement.getAttribute("datetaken"));
         }
 
         NodeList permissionsNodes = photoElement.getElementsByTagName("permissions");
         if (permissionsNodes.getLength() > 0) {
             Element permissionsElement = (Element) permissionsNodes.item(0);
             Permissions permissions = new Permissions();
             permissions.setComment(permissionsElement.getAttribute("permcomment"));
             permissions.setAddmeta(permissionsElement.getAttribute("permaddmeta"));
         }
 
         try {
             Element editabilityElement = (Element) photoElement.getElementsByTagName("editability").item(0);
             Editability editability = new Editability();
             editability.setComment("1".equals(editabilityElement.getAttribute("cancomment")));
             editability.setAddmeta("1".equals(editabilityElement.getAttribute("canaddmeta")));
         } catch (NullPointerException e) {
             // nop
         }
 
         try {
             Element commentsElement = (Element) photoElement.getElementsByTagName("comments").item(0);
             photo.setComments(((Text) commentsElement.getFirstChild()).getData());
         } catch (NullPointerException e) {
             // nop
         }
 
         try {
             Element notesElement = (Element) photoElement.getElementsByTagName("notes").item(0);
             List notes = new ArrayList();
             NodeList noteNodes = notesElement.getElementsByTagName("note");
             for (int i = 0; i < noteNodes.getLength(); i++) {
                 Element noteElement = (Element) noteNodes.item(i);
                 Note note = new Note();
                 note.setId(noteElement.getAttribute("id"));
                 note.setAuthor(noteElement.getAttribute("author"));
                 note.setAuthorName(noteElement.getAttribute("authorname"));
                 note.setBounds(noteElement.getAttribute("x"), noteElement.getAttribute("y"),
                     noteElement.getAttribute("w"), noteElement.getAttribute("h"));
                 note.setText(noteElement.getTextContent());
                 notes.add(note);
             }
             photo.setNotes(notes);
         } catch (NullPointerException e) {
             // nop
         }
 
         try {
             Element tagsElement = (Element) photoElement.getElementsByTagName("tags").item(0);
             List tags = new ArrayList();
             NodeList tagNodes = tagsElement.getElementsByTagName("tag");
             for (int i = 0; i < tagNodes.getLength(); i++) {
                 Element tagElement = (Element) tagNodes.item(i);
                 Tag tag = new Tag();
                 tag.setId(tagElement.getAttribute("id"));
                 tag.setAuthor(tagElement.getAttribute("author"));
                 tag.setRaw(tagElement.getAttribute("raw"));
                 tag.setValue(((Text) tagElement.getFirstChild()).getData());
                 tags.add(tag);
             }
             photo.setTags(tags);
         } catch (NullPointerException e) {
             // nop
         }
 
         try {
             Element urlsElement = (Element) photoElement.getElementsByTagName("urls").item(0);
             List urls = new ArrayList();
             NodeList urlNodes = urlsElement.getElementsByTagName("url");
             for (int i = 0; i < urlNodes.getLength(); i++) {
                 Element urlElement = (Element) urlNodes.item(i);
                 PhotoUrl photoUrl = new PhotoUrl();
                 photoUrl.setType(urlElement.getAttribute("type"));
                 photoUrl.setUrl(XMLUtilities.getValue(urlElement));
                 if (photoUrl.getType().equals("photopage")) {
                     photo.setUrl(photoUrl.getUrl());
                 }
             }
             photo.setUrls(urls);
         } catch (NullPointerException e) {
             // nop
         }
 
         String longitude = null;
         String latitude = null;
         String accuracy = null;
         try {
             Element geoElement = (Element) photoElement.getElementsByTagName("location").item(0);
             longitude = geoElement.getAttribute("longitude");
             latitude = geoElement.getAttribute("latitude");
             accuracy = geoElement.getAttribute("accuracy");
         } catch (NullPointerException e) {
         	// Geodata may be available as attributes in the photo-tag itself!
             try {
                 longitude = photoElement.getAttribute("longitude");
                 latitude = photoElement.getAttribute("latitude");
                 accuracy = photoElement.getAttribute("accuracy");
             } catch (NullPointerException e2) {
                 // no geodata at all
             }
         }
         if (longitude != null && latitude != null) {
             if(longitude.length() > 0 && latitude.length() > 0
                 && !("0".equals(longitude) && "0".equals(latitude))) {
                 photo.setGeoData(new GeoData(longitude, latitude, accuracy));
             }
         }
 
         return photo;
     }
 
     public static final PhotoList createPhotoList(Element photosElement) {
         PhotoList photos = new PhotoList();
         photos.setPage(photosElement.getAttribute("page"));
         photos.setPages(photosElement.getAttribute("pages"));
         photos.setPerPage(photosElement.getAttribute("perpage"));
         photos.setTotal(photosElement.getAttribute("total"));
 
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             photos.add(PhotoUtils.createPhoto(photoElement));
         }
         return photos;
     }
 
 }
