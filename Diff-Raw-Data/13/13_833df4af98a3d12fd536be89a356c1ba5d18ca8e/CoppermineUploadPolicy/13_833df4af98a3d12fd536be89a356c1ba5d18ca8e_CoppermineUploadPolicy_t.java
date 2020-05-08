 //
 // $Id: CoppermineUploadPolicy.java 143 2007-05-14 02:07:27 +0000 (lun., 14 mai
 // 2007) felfert $
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-05-07
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.policies;
 
 import java.io.File;
 import java.net.URL;
 
 import wjhk.jupload2.JUploadApplet;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.filedata.DefaultFileData;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.filedata.PictureFileData;
 
 // TODO cookies handling: desc to be mve to UploadPolicy presentation.
 /**
  * Specific UploadPolicy for the coppermine picture gallery. It is based on the
  * PictureUploadPolicy, and some specific part to add the uploaded pictures to a
  * coppermine existing album. <BR>
  * Specific features for this policy are:
  * <UL>
  * <LI>Album handling : the setProperty("albumId", n) can be called from
  * javascript, when the user selects another album (with n is the numeric id for
  * the selected album). This needs that the MAYSCRIPT HTML parameter is set, in
  * the APPLET tag (see the example below). The upload can not start if the user
  * didn't first select an album.
  * <LI>If an error occurs, the applet asks the user if he wants to send a mail
  * to the webmaster. If he answered yes, the full debug output is submitted to
  * the URL pointed by urlToSendErrorTo. This URL should send a mail to the
  * manager of the Coppermine galery.
  * </UL>
  * <A NAME="example1">
  * <H3>Call of the applet from a php script in coppermine</H3>
  * </A> You'll find below an example of how to put the applet into a PHP page:
  * <BR>
  * <XMP> <?php $URL = $CONFIG['site_url'] . 'xp_publish.php'; $lang =
  * $lang_translation_info['lang_country_code']; $max_upl_width_height =
  * $CONFIG['max_upl_width_height']; ?> <APPLET NAME="JUpload"
  * CODE="wjhk.jupload2.JUploadApplet" ARCHIVE="plugins/jupload/wjhk.jupload.jar"
  * <!-- Applet display size, on the navigator page --> WIDTH="500" HEIGHT="700"
  * <!-- The applet call some javascript function, so we must allow it : -->
  * MAYSCRIPT > <!-- First, mandatory parameters --> <PARAM NAME="postURL"
  * VALUE="$URL"> <PARAM NAME="uploadPolicy" VALUE="CoppermineUploadPolicy"> <!--
  * Then, optional parameters --> <PARAM NAME="lang" VALUE="$lang"> <PARAM
  * NAME="maxPicHeight" VALUE="$max_upl_width_height"> <PARAM NAME="maxPicWidth"
  * VALUE="$max_upl_width_height"> <PARAM NAME="debugLevel" VALUE="0"> Java 1.4
  * or higher plugin required. </APPLET> </XMP> <A NAME="example1">
  * <H3>Example 2: albumId set by a javascript call.</H3>
  * </A> <XMP> <script language="javascript" type="text/javascript"> function
  * onAlbumChange() { if (document.form_album.album_id.selectedIndex >= 0) {
  * document.applets['JUpload'].setProperty('albumId',
  * document.form_album.album_id.value); document.form_album.album_name.value =
  * document.form_album.album_id.options[document.form_album.album_id.selectedIndex].text;
  * document.form_album.album_description.value =
  * description[document.form_album.album_id.value]; } else {
  * document.JUpload.setProperty('albumId', '');
  * document.form_album.album_name.value = '';
  * document.form_album.album_description.value = ''; } } </script> </XMP>
  * 
  * @author Etienne Gauthier
  * @version $Revision$
  */
 public class CoppermineUploadPolicy extends PictureUploadPolicy {
 
     /**
      * The coppermine's album id where picture must be uploaded.
      */
     private int albumId;
 
     /**
      * The number of pictures to download in the current upload. This number is
      * stored in the {@link #isUploadReady()} method, which is called at the
      * beginning of each upload.
      */
     private int nbPictureInUpload = 0;
 
     /**
      * @param theApplet Identifier for the current applet. It's necessary, to
      *            read information from the navigator.
      */
     public CoppermineUploadPolicy(JUploadApplet theApplet)
             throws JUploadException {
         // Let's call our mother ! :-)
         super(theApplet);
         // Use our own default for stringUploadError
         setStringUploadError(UploadPolicyFactory.getParameter(theApplet,
                 PROP_STRING_UPLOAD_ERROR, "ERROR: (.*)", this));
 
         // Let's read the albumId from the applet parameter. It can be unset,
         // but the user must then choose
         // an album before upload.
         this.albumId = UploadPolicyFactory.getParameter(theApplet,
                 PROP_ALBUM_ID, DEFAULT_ALBUM_ID, this);
     }
 
     /**
      * The Coppermine gallery allows files other than pictures. If it's a
      * picture, we manage it as a picture. Otherwise, we currently do nothing.
      * 
      * @see #onFileSelected(FileData)
      */
     @Override
     public FileData createFileData(File file, File root) {
         PictureFileData pfd = new PictureFileData(file, root, this);
         if (pfd.isPicture()) {
             return pfd;
         }
         return new DefaultFileData(file, root, this);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#onFileSelected(wjhk.jupload2.filedata.FileData)
      */
     @Override
     public void onFileSelected(FileData fileData) {
         if (fileData == null) {
             super.onFileSelected(fileData);
         } else if (fileData instanceof PictureFileData) {
             super.onFileSelected(fileData);
         } else {
             super.onFileSelected(null);
         }
     }
 
     /**
      * This method only handles the <I>albumId</I> parameter, which is the only
      * applet parameter that is specific to this class. The super.setProperty
      * method is called for other properties.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void setProperty(String prop, String value) throws JUploadException {
         // The, we check the local properties.
         if (prop.equals(PROP_ALBUM_ID)) {
             this.albumId = UploadPolicyFactory.parseInt(value, 0, this);
             displayDebug("Post URL (modified in CoppermineUploadPolicy) = "
                     + getPostURL(), 10);
         } else {
             // Otherwise, transmission to the mother class.
             super.setProperty(prop, value);
         }
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#getPostURL()
      */
     @Override
     public String getPostURL() {
         // The jupload.phg script gives the upload php script that will receive
         // the uploaded files.
         // It can be xp_publish.php, or (much better) jupload.php.
         // In either case, the postURL given to the applet contains already one
         // paramete: the cmd (for xp_publish) or
         // the action (for jupload). We just add one parameter.
         // Note: if the postURL (given to the applet) doesn't need any
         // parameter, it's necessary to add a dummy one,
         // so that the line below generates a valid URL.
         String postURL = super.getPostURL();
         return postURL + (postURL.contains("?") ? "&" : "?") + "album="
                 + this.albumId;
     }
 
     /**
      * @see UploadPolicy#getUploadName(FileData, int)
      */
     @Override
     public String getUploadName(@SuppressWarnings("unused")
     FileData fileData, @SuppressWarnings("unused")
     int index) {
         return "userpicture";
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#isUploadReady()
      */
     @Override
     public boolean isUploadReady() {
         if (this.albumId <= 0) {
             alert("chooseAlbumFirst");
             return false;
         }
 
         // We note the number of files to upload.
         this.nbPictureInUpload = getApplet().getFilePanel().getFilesLength();
 
         // Default : Let's ask the mother.
         return super.isUploadReady();
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String)
      */
     @Override
     public void afterUpload(Exception e, @SuppressWarnings("unused")
     String serverOutput) {
        int nbPictureAfterUpload = getApplet().getFilePanel().getFilesLength();
        if (nbPictureAfterUpload > this.nbPictureInUpload) {
            displayErr("CoppermineUploadPolicy.afterUpload: The number of uploaded files is negative! ("
                    + (this.nbPictureInUpload - nbPictureAfterUpload) + ")");
        } else if (nbPictureAfterUpload == this.nbPictureInUpload) {
            displayWarn("CoppermineUploadPolicy.afterUpload: No file were uploaded! ("
                    + (nbPictureAfterUpload - this.nbPictureInUpload) + ")");
        } else if (e == null) {
            // Ok, at least one file were uploaded, and there was no error.
             try {
                 // First : construction of the editpic URL :
                 String editpicURL = getPostURL().substring(0,
                         getPostURL().lastIndexOf('/'))
                         // + "/editpics.php?album=" + albumId
                         + "/jupload&action=edit_uploaded_pics&album="
                         + this.albumId
                         + "&nb_pictures="
                        + (this.nbPictureInUpload - nbPictureAfterUpload);
 
                 if (getDebugLevel() >= 100) {
                     alertStr("No switch to property page, because debug level is "
                             + getDebugLevel() + " (>=100)");
                 } else {
                     // Let's display an alert box, to explain what to do to the
                     // user: he will be redirected to the coppermine page that
                     // allow him to associate names and comments to the uploaded
                     // pictures.
                     alert("coppermineUploadOk");
 
                     // Let's change the current URL to edit names and comments,
                     // for the selected album. Ok, let's go and add names and
                     // comments to the newly updated pictures.
                     String target = getAfterUploadTarget();
                     getApplet().getAppletContext().showDocument(
                             new URL(editpicURL),
                             (null == target) ? "_self" : target);
                 }
             } catch (Exception ee) {
                 // Oups, no navigator. We are probably in debug mode, within
                 // eclipse for instance.
                 displayErr(ee);
             }
         }
     }
 
     /** @see DefaultUploadPolicy#displayParameterStatus() */
     @Override
     public void displayParameterStatus() {
         super.displayParameterStatus();
 
         displayDebug("======= Parameters managed by CoppermineUploadPolicy", 20);
         displayDebug(PROP_ALBUM_ID + " : " + this.albumId, 20);
         displayDebug("", 20);
     }
 
 }
