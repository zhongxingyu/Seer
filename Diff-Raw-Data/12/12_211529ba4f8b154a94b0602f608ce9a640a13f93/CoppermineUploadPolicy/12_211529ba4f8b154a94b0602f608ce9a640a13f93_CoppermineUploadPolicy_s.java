 //
 // $Id: CoppermineUploadPolicy.java 143 2007-05-14 02:07:27 +0000 (lun., 14 mai
 // 2007) felfert $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: 2006-05-07
 // Creator: etienne_sf
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
 
 import wjhk.jupload2.context.JUploadContext;
 import wjhk.jupload2.exception.JUploadException;
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
  * <A NAME="example1"> <H3>Call of the applet from a php script in coppermine</H3>
  * </A> You'll find below an example of how to put the applet into a PHP page: <BR>
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
  * or higher plugin required. </APPLET> </XMP> <A NAME="example1"> <H3>Example
  * 2: albumId set by a javascript call.</H3> </A> <XMP> <script
  * language="javascript" type="text/javascript"> function onAlbumChange() { if
  * (document.form_album.album_id.selectedIndex >= 0) {
  * document.applets['JUpload'].setProperty('albumId',
  * document.form_album.album_id.value); document.form_album.album_name.value =
  * document
  * .form_album.album_id.options[document.form_album.album_id.selectedIndex
  * ].text; document.form_album.album_description.value =
  * description[document.form_album.album_id.value]; } else {
  * document.JUpload.setProperty('albumId', '');
  * document.form_album.album_name.value = '';
  * document.form_album.album_description.value = ''; } } </script> </XMP>
  * 
  * @author etienne_sf
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
      * @param juploadContext Identifier for the current applet. It's necessary, to
      *            read information from the navigator.
      * @throws JUploadException
      */
     public CoppermineUploadPolicy(JUploadContext juploadContext)
             throws JUploadException {
         // Let's call our mother ! :-)
         super(juploadContext);
 
         // Let's read the albumId from the applet parameter. It can be unset,
         // but the user must then choose
         // an album before upload.
         this.albumId = getContext().getParameter(PROP_ALBUM_ID,
                 DEFAULT_ALBUM_ID);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#onFileSelected(wjhk.jupload2.filedata.FileData)
      */
     @Override
     public void onFileSelected(FileData fileData) {
         if (fileData == null) {
             super.onFileSelected(fileData);
         } else if (fileData instanceof PictureFileData) {
             // The selected file is a picture, we let PictureUploadPolicy manage
             // it.
             super.onFileSelected(fileData);
         } else {
             // he selected file is not a picture. We simulate the fact that no
             // more picture is selected, so that the preview picture is cleared.
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
         displayDebug("[CoppermineUploadPolicy] Call of setProperty: " + prop
                 + " => " + value, 30);
 
         // Check if it's a local property.
         if (prop.equals(PROP_ALBUM_ID)) {
             this.albumId = getContext().parseInt(value, 0);
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
         // Within the coppermine PHP script, that contains the call to this
         // applet, the postURL given contains the full URL, without the album
         // id. So we ask for this postURL, and just concatenate the albumId on
         // the fly.
         String postURL = super.getPostURL();
         return postURL + (postURL.contains("?") ? "&" : "?") + "album="
                 + this.albumId;
     }
 
     /**
      * @see UploadPolicy#getUploadName(FileData, int)
      */
     @Override
     public String getUploadName(FileData fileData, int index) {
         return "userpicture";
     }
 
     /**
      * This method checks that an album id has been given, and then stores the
      * number of files that are to be uploaded, before upload, then call its
      * superclass. This number is then used to display to the user the list of
      * pictures he just uploaded.
      * 
     * @see wjhk.jupload2.policies.UploadPolicy#isUploadReady()
      */
     @Override
     public boolean beforeUpload() {
         if (this.albumId <= 0) {
             alert("chooseAlbumFirst");
             return false;
         }
 
         // We note the number of files to upload.
         this.nbPictureInUpload = getContext().getUploadPanel().getFilePanel()
                 .getFilesLength();
 
         // Default : Let's ask the mother.
         return super.beforeUpload();
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String) */
     @Override
     public void afterUpload(Exception e, String serverOutput)
             throws JUploadException {
         int nbPictureAfterUpload = getContext().getUploadPanel().getFilePanel()
                 .getFilesLength();
         if (nbPictureAfterUpload > this.nbPictureInUpload) {
             displayErr("CoppermineUploadPolicy.afterUpload: The number of uploaded files is negative! ("
                     + (this.nbPictureInUpload - nbPictureAfterUpload) + ")");
         } else if (nbPictureAfterUpload == this.nbPictureInUpload) {
             displayWarn("CoppermineUploadPolicy.afterUpload: No file were uploaded! ("
                     + (nbPictureAfterUpload - this.nbPictureInUpload) + ")");
         } else if (getDebugLevel() >= 100) {
             alertStr("No switch to property page, because debug level is "
                     + getDebugLevel() + " (>=100)");
         } else if (e == null) {
             // Let's display an alert box, to explain what to do to the
             // user: he will be redirected to the coppermine page that
             // allow him to associate names and comments to the uploaded
             // pictures.
             alert("coppermineUploadOk");
 
             // Let's change the afterUploadURL value, so we can call the
             // standard afterUpload method (DefaultUploadPolicy).
             setAfterUploadURL(getPostURL().substring(0,
                     getPostURL().lastIndexOf('/'))
                     // + "/editpics.php?album=" + albumId
                     + "/jupload&action=edit_uploaded_pics&album="
                     + this.albumId
                     + "&nb_pictures="
                     + (this.nbPictureInUpload - nbPictureAfterUpload));
             // ... and call the standard behavior.
             super.afterUpload(e, serverOutput);
         }
     }
 
     /** @see DefaultUploadPolicy#displayParameterStatus() */
     @Override
     public void displayParameterStatus() {
         super.displayParameterStatus();
 
         displayDebug("======= Parameters managed by CoppermineUploadPolicy", 30);
         displayDebug(PROP_ALBUM_ID + " : " + this.albumId, 30);
         displayDebug("", 30);
     }
 
 }
