 //
 // $Id: PictureUploadPolicy.java 295 2007-06-27 08:43:25 +0000 (mer., 27 juin
 // 2007) etienne_sf $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: 2006-05-06
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
 
 import java.awt.Cursor;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.SystemColor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.ImageObserver;
 import java.io.File;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import wjhk.jupload2.context.JUploadContext;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadExceptionStopAddingFiles;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.filedata.PictureFileData;
 import wjhk.jupload2.filedata.helper.ImageFileConversionInfo;
 import wjhk.jupload2.gui.JUploadFileChooser;
 import wjhk.jupload2.gui.JUploadPanel;
 import wjhk.jupload2.gui.image.JUploadImagePreview;
 import wjhk.jupload2.gui.image.PictureDialog;
 import wjhk.jupload2.gui.image.PicturePanel;
 
 /**
  * This class add handling of pictures to upload. <BR>
  * <BR>
  * <H4>Functionalities:</H4>
  * <UL>
  * <LI>The top panel (upper part of the applet display) is modified, by using
  * UploadPolicy.
  * {@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JUploadPanel)}
  * . It contains a <B>preview</B> picture panel, and two additional buttons to
  * rotate the selected picture in one direction or the other.
  * <LI>Ability to set maximum width or height to a picture (with maxPicWidth and
  * maxPicHeight applet parameters, see the global explanation on the <a
  * href="UploadPolicy.html#parameters">parameters</a> section) of the
  * UploadPolicy API page.
  * <LI>Rotation of pictures, by quarter of turn.
  * <LI><I>(To be implemented)</I> A target picture format can be used, to force
  * all uploaded pictures to be in one picture format, jpeg for instance. All
  * details are in the UploadPolicy <a
  * href="UploadPolicy.html#parameters">parameters</a> section.
  * </UL>
  * <BR>
  * <BR>
  * See an example of HTML that calls this applet, just below. <H4>Parameters</H4>
  * The description for all parameters of all polices has been grouped in the
  * UploadPolicy <a href="UploadPolicy.html#parameters">parameters</a> section. <BR>
  * The parameters implemented in this class are:
  * <UL>
  * <LI>maxPicWidth: Maximum width for the uploaded picture.
  * <LI>maxPicHeight: Maximum height for the uploaded picture.
  * <LI>targetPictureFormat : Define picture format conversions.
  * <LI>keepOriginalFileExtensionForConvertedImages : handling of file extensions
  * for image conversions.
  * </UL>
  * <A NAME="example"> <H4>HTML call example</H4> </A> You'll find below an
  * example of how to put the applet into a PHP page: <BR>
  * <XMP> <APPLET NAME="JUpload" CODE="wjhk.jupload2.JUploadApplet"
  * ARCHIVE="plugins/jupload/wjhk.jupload.jar" <!-- Applet display size, on the
  * navigator page --> WIDTH="500" HEIGHT="700" <!-- The applet call some
  * javascript function, so we must allow it : --> MAYSCRIPT > <!-- First,
  * mandatory parameters --> <PARAM NAME="postURL"
  * VALUE="http://some.host.com/youruploadpage.php"> <PARAM NAME="uploadPolicy"
  * VALUE="PictureUploadPolicy"> <!-- Then, optional parameters --> <PARAM
  * NAME="lang" VALUE="fr"> <PARAM NAME="maxPicHeight" VALUE="768"> <PARAM
  * NAME="maxPicWidth" VALUE="1024"> <PARAM NAME="debugLevel" VALUE="0"> Java 1.4
  * or higher plugin required. </APPLET> </XMP>
  * 
  * @author etienne_sf
  * @version $Revision$
  */
 
 public class PictureUploadPolicy extends DefaultUploadPolicy implements
         ActionListener, ImageObserver {
 
     /**
      * Indicates that a BufferedImage is to be created when the user selects the
      * file. <BR>
      * If true : the Image is loaded once from the hard drive. This consumes
      * memory, but is interesting for big pictures, when they are resized (see
      * {@link #maxWidth} and {@link #maxHeight}). <BR>
      * If false : it is loaded for each display on the applet, then once for the
      * upload. <BR>
      * <BR>
      * Default : false, because the applet, while in the navigator, runs too
      * quickly out of memory.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_STORE_BUFFERED_IMAGE
      */
     private boolean storeBufferedImage;
 
     /**
      * This parameter can contain a list to convert image formats. <br />
      * see class description of {@link UploadPolicy} for details
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_TARGET_PICTURE_FORMAT
      */
     private String targetPictureFormat;
 
     /**
      * see class description of {@link UploadPolicy} for details
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_KEEP_ORIG_EXTENSION
      */
     private boolean keepOrigExtension;
 
     /**
      * the parsed {@link #targetPictureFormat} list
      */
     private ImageFileConversionInfo imageFileConversionInfo = new ImageFileConversionInfo("");
 
     /**
      * Stored value for the fileChooserIconFromFileContent applet property.
      * 
      * @see UploadPolicy#PROP_FILE_CHOOSER_IMAGE_PREVIEW
      */
     private boolean fileChooserImagePreview = UploadPolicy.DEFAULT_FILE_CHOOSER_IMAGE_PREVIEW;
 
     /**
      * Indicates wether or not the preview pictures must be calculated by the
      * BufferedImage.getScaledInstance() method.
      */
     private boolean highQualityPreview;
 
     /**
      * Maximal width for the uploaded picture. If the actual width for the
      * picture is more than maxWidth, the picture is resized. The proportion
      * between widht and height are maintained. Negative if no maximum width (no
      * resizing). <BR>
      * Default: -1.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_WIDTH
      */
     private int maxWidth = -1;
 
     /**
      * Maximal height for the uploaded picture. If the actual height for the
      * picture is more than maxHeight, the picture is resized. The proportion
      * between width and height are maintained. Negative if no maximum height
      * (no resizing). <BR>
      * Default: -1.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_HEIGHT
      */
     private int maxHeight = -1;
 
     /**
      * Used to control the compression of a jpeg written file, after
      * transforming a picture.
      * 
      * @see UploadPolicy#PROP_PICTURE_COMPRESSION_QUALITY
      */
     private float pictureCompressionQuality = UploadPolicy.DEFAULT_PICTURE_COMPRESSION_QUALITY;
 
     /**
      * Used to control whether PictureFileData should add metadata to
      * transformed picture files, before upload (or remove metadata from
      * normally untransformed picture files).
      */
     private boolean pictureTransmitMetadata;
 
     /**
      * @see UploadPolicy
      */
     private int realMaxWidth = -1;
 
     /**
      * @see UploadPolicy
      */
     private int realMaxHeight = -1;
 
     /**
      * Button to allow the user to rotate the picture one quarter
      * counter-clockwise.
      */
     private JButton rotateLeftButton;
 
     /**
      * Button to allow the user to rotate the picture one quarter clockwise.
      */
     private JButton rotateRightButton;
 
     /**
      * The picture panel, where the selected picture is displayed.
      */
     private PicturePanel picturePanel;
 
     /**
      * The standard constructor, which transmit most informations to the
      * super.Constructor().
      * 
      * @param juploadContext Reference to the current applet. Allows access to
      *            javascript functions.
      * @throws JUploadException
      */
     public PictureUploadPolicy(JUploadContext juploadContext)
             throws JUploadException {
         super(juploadContext);
 
         // Creation of the PictureFileDataPolicy, from parameters given to the
         // applet, or from default values.
         setFileChooserImagePreview(juploadContext.getParameter(
                 PROP_FILE_CHOOSER_IMAGE_PREVIEW,
                 DEFAULT_FILE_CHOOSER_IMAGE_PREVIEW));
         setHighQualityPreview(juploadContext.getParameter(
                 PROP_HIGH_QUALITY_PREVIEW, DEFAULT_HIGH_QUALITY_PREVIEW));
         setMaxHeight(juploadContext.getParameter(PROP_MAX_HEIGHT,
                 DEFAULT_MAX_HEIGHT));
         setMaxWidth(juploadContext.getParameter(PROP_MAX_WIDTH,
                 DEFAULT_MAX_WIDTH));
         setPictureCompressionQuality(juploadContext.getParameter(
                 PROP_PICTURE_COMPRESSION_QUALITY,
                 DEFAULT_PICTURE_COMPRESSION_QUALITY));
         setPictureTransmitMetadata(juploadContext.getParameter(
                 PROP_PICTURE_TRANSMIT_METADATA,
                 DEFAULT_PICTURE_TRANSMIT_METADATA));
         setRealMaxHeight(juploadContext.getParameter(PROP_REAL_MAX_HEIGHT,
                 DEFAULT_REAL_MAX_HEIGHT));
         setRealMaxWidth(juploadContext.getParameter(PROP_REAL_MAX_WIDTH,
                 DEFAULT_REAL_MAX_WIDTH));
         setTargetPictureFormat(juploadContext.getParameter(
                 PROP_TARGET_PICTURE_FORMAT, DEFAULT_TARGET_PICTURE_FORMAT));
 
         setKeepOrigExtension(juploadContext.getParameter(
                 PROP_KEEP_ORIG_EXTENSION, DEFAULT_KEEP_ORIG_EXTENSION));
 
         displayDebug("[PictureUploadPolicy] end of constructor", 30);
     }
 
     /**
      * This methods actually returns a {@link PictureFileData} instance. It
      * allows only pictures: if the file is not a picture, this method returns
      * null, thus preventing the file to be added to the list of files to be
      * uploaded.
      * 
      * @param file The file selected by the user (called once for each added
      *            file).
      * @return An instance of {@link PictureFileData} or null if file is not a
      *         picture.
      * @see wjhk.jupload2.policies.UploadPolicy#createFileData(File,File)
      */
     @Override
     public FileData createFileData(File file, File root)
             throws JUploadExceptionStopAddingFiles {
         // Do standard rules accept this file ?
         FileData defaultFileData = super.createFileData(file, root);
 
         if (defaultFileData == null) {
             // The file is not allowed.
             return null;
         } else {
             // Ok, the file is to be accepted. Is it a picture?
             PictureFileData pfd = null;
             try {
                 pfd = new PictureFileData(file, root, this);
             } catch (JUploadIOException e) {
                 displayErr(e);
             }
 
             // If we get a pfd, let' check that it's a picture.
             if (pfd != null && pfd.isPicture()) {
                 return pfd;
             } else if (getAllowedFileExtensions() != null) {
                 // A list of allowed extensions has been given, and, as we got
                 // here, defaultFileData is not null, that is: this files match
                 // the allowedFileEXtensions parameter. We return it.
                 return defaultFileData;
             } else {
                 // We now use the JUploadExceptionStopAddingFiles exception, to
                 // allow the user to stop adding files.
                 String msg = String.format(getString("notAPicture"), file
                         .getName());
 
                 // Alert only once, when several files are not pictures... hum,
                 displayWarn(msg);
                 if (JOptionPane.showConfirmDialog(null, msg, "alert",
                         JOptionPane.OK_CANCEL_OPTION,
                         JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
                     // The user want to stop to add files to the list. For
                     // instance, when he/she added a whole directory, and it
                     // contains a lot of files that don't match the allowed file
                     // extension.
                     throw new JUploadExceptionStopAddingFiles(
                             "Stopped by the user");
                 }
                 return null;
             }
         }
     }
 
     /**
      * This method override the default topPanel, and adds:<BR>
      * <UL>
      * <LI>Two rotation buttons, to rotate the currently selected picture.
      * <LI>A Preview area, to view the selected picture
      * </UL>
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton,
      *      JButton, JUploadPanel)
      */
     @Override
     public JPanel createTopPanel(JButton browse, JButton remove,
             JButton removeAll, JUploadPanel jUploadPanel) {
         // The top panel is verticaly divided in :
         // - On the left, the button bar (buttons one above another)
         // - On the right, the preview PicturePanel.
 
         // Creation of specific buttons
         this.rotateLeftButton = new JButton(getString("buttonRotateLeft"));
         this.rotateLeftButton.setIcon(new ImageIcon(getClass().getResource(
                 "/images/rotateLeft.gif")));
         this.rotateLeftButton.addActionListener(this);
         this.rotateLeftButton.addMouseListener(jUploadPanel);
         this.rotateLeftButton.setEnabled(false);
 
         this.rotateRightButton = new JButton(getString("buttonRotateRight"));
         this.rotateRightButton.setIcon(new ImageIcon(getClass().getResource(
                 "/images/rotateRight.gif")));
         this.rotateRightButton.addActionListener(this);
         this.rotateRightButton.addMouseListener(jUploadPanel);
         this.rotateRightButton.setEnabled(false);
 
         // The button bar
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new GridLayout(5, 1, 5, 5));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
         buttonPanel.add(browse);
         buttonPanel.add(this.rotateLeftButton);
         buttonPanel.add(this.rotateRightButton);
         buttonPanel.add(removeAll);
         buttonPanel.add(remove);
 
         // The preview PicturePanel
         JPanel pPanel = new JPanel();
         pPanel.setLayout(new GridLayout(1, 1));
         pPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
 
         this.picturePanel = new PicturePanel(true, this);
         this.picturePanel.addMouseListener(jUploadPanel);
         pPanel.add(this.picturePanel);
         // Setting specific cursor for this panel, default for other parts of
         // the applet.
         setCursor(null);
 
         // And last but not least ... creation of the top panel:
         JPanel topPanel = new JPanel();
         topPanel.setLayout(new GridLayout(1, 2));
         topPanel.add(buttonPanel);
         topPanel.add(pPanel);
 
         jUploadPanel.setBorder(BorderFactory
                 .createLineBorder(SystemColor.controlDkShadow));
 
         return topPanel;
     }// createTopPanel
 
     /**
      * This method handles the clicks on the rotation buttons. All other actions
      * are managed by the {@link DefaultUploadPolicy}.
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
         displayInfo("Action : " + e.getActionCommand());
         if (e.getActionCommand() == this.rotateLeftButton.getActionCommand()) {
             this.picturePanel.rotate(-1);
         } else if (e.getActionCommand() == this.rotateRightButton
                 .getActionCommand()) {
             this.picturePanel.rotate(1);
         }
     }// actionPerformed
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#onFileSelected(wjhk.jupload2.filedata.FileData)
      */
     @Override
     public void onFileSelected(FileData fileData) {
         if (fileData != null) {
             displayDebug("File selected: " + fileData.getFileName(), 30);
         }
         if (this.picturePanel != null) {
             // If this file is a picture, we display it.
             if (fileData instanceof PictureFileData) {
                 Cursor previousCursor = setWaitCursor();
                 this.picturePanel.setPictureFile((PictureFileData) fileData,
                         this.rotateLeftButton, this.rotateRightButton);
                 setCursor(previousCursor);
             } else {
                 this.picturePanel.setPictureFile(null, this.rotateLeftButton,
                         this.rotateRightButton);
             }
         }
     }
 
     /**
      * Open the 'big' preview dialog box. It allows the user to see a full
      * screen preview of the choosen picture.<BR>
      * This method does nothing if the panel has no selected picture, that is
      * when pictureFileData is null.
      * 
      * @see UploadPolicy#onFileDoubleClicked(FileData)
      */
     @Override
     public void onFileDoubleClicked(FileData pictureFileData) {
         if (pictureFileData == null) {
             // No action
         } else if (!(pictureFileData instanceof PictureFileData)) {
             displayWarn("PictureUploadPolicy: received a non PictureFileData in onFileDoubleClicked");
         } else {
             new PictureDialog(null, (PictureFileData) pictureFileData, this);
         }
     }
 
     /** @see UploadPolicy#beforeUpload() */
     @Override
     public boolean beforeUpload() {
         // We clear the current picture selection. This insures a correct
         // managing of enabling/disabling of
         // buttons, even if the user stops the upload.
         getContext().getUploadPanel().getFilePanel().clearSelection();
         if (this.picturePanel != null) {
             this.picturePanel.setPictureFile(null, this.rotateLeftButton,
                     this.rotateRightButton);
         }
 
         // Then, we call the standard action, if any.
         return super.beforeUpload();
     }
 
     // ////////////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// Getters and Setters
     // ////////////////////////////////////////////////
     // ////////////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Getter for fileChooserImagePreview.
      * 
      * @return Current value for the applet parameter: fileChooserImagePreview
      * @see UploadPolicy#PROP_FILE_CHOOSER_IMAGE_PREVIEW
      */
     public boolean getFileChooserImagePreview() {
         return this.fileChooserImagePreview;
     }
 
     /**
      * Setter for fileChooserIconFromFileContent. Current allowed values are:
      * -1, 0, 1. Default value is 0.
      * 
      * @param fileChooserImagePreview new value to store, for the applet
      *            parameter: fileChooserImagePreview.
      * @see UploadPolicy#PROP_FILE_CHOOSER_IMAGE_PREVIEW
      */
     public void setFileChooserImagePreview(boolean fileChooserImagePreview) {
         this.fileChooserImagePreview = fileChooserImagePreview;
     }
 
     /** @return the applet parameter <I>highQualityPreview</I>. */
     public boolean getHighQualityPreview() {
         return this.highQualityPreview;
     }
 
     /** @param highQualityPreview the highQualityPreview to set */
     void setHighQualityPreview(boolean highQualityPreview) {
         this.highQualityPreview = highQualityPreview;
     }
 
     /**
      * @return Returns the maxHeight, that should be used by pictures non
      *         transformed (rotated...) by the applet.
      */
     public int getMaxHeight() {
         return this.maxHeight;
     }
 
     /** @param maxHeight the maxHeight to set */
     void setMaxHeight(int maxHeight) {
         if (maxHeight <= 0) {
             this.maxHeight = Integer.MAX_VALUE;
             displayWarn("[setMaxHeight] maxHeight switched from " + maxHeight
                     + " to " + this.maxHeight);
         } else {
             this.maxHeight = maxHeight;
         }
     }
 
     /**
      * @return Returns the maxWidth, that should be used by pictures non
      *         transformed (rotated...) by the applet.
      */
     public int getMaxWidth() {
         return this.maxWidth;
     }
 
     /** @param maxWidth the maxWidth to set */
     void setMaxWidth(int maxWidth) {
         if (maxWidth <= 0) {
             this.maxWidth = Integer.MAX_VALUE;
             displayWarn("[setMaxWidth] maxWidth switched from " + maxWidth
                     + " to " + this.maxWidth);
         } else {
             this.maxWidth = maxWidth;
         }
     }
 
     /**
      * @return The current value for picture compression.
      */
     public float getPictureCompressionQuality() {
         return this.pictureCompressionQuality;
     }
 
     /**
      * @see #pictureCompressionQuality
      * @param pictureCompressionQuality The new value for picture compression.
      */
     void setPictureCompressionQuality(float pictureCompressionQuality) {
         this.pictureCompressionQuality = pictureCompressionQuality;
     }
 
     /**
      * @return The current value for transmission (or no transmission) of
      *         picture metadata.
      */
     public boolean getPictureTransmitMetadata() {
         return this.pictureTransmitMetadata;
     }
 
     /**
      * @see #pictureTransmitMetadata
      * @param pictureTransmitMetadata The new value for this attribute.
      */
     void setPictureTransmitMetadata(boolean pictureTransmitMetadata) {
         this.pictureTransmitMetadata = pictureTransmitMetadata;
     }
 
     /**
      * @return Returns the maxHeight, that should be used by pictures that are
      *         transformed (rotated...) by the applet.
      */
     public int getRealMaxHeight() {
         return (this.realMaxHeight == Integer.MAX_VALUE) ? this.maxHeight
                 : this.realMaxHeight;
     }
 
     /** @param realMaxHeight the realMaxHeight to set */
     void setRealMaxHeight(int realMaxHeight) {
         this.realMaxHeight = realMaxHeight;
     }
 
     /**
      * @return Returns the maxWidth, that should be used by pictures that are
      *         transformed (rotated...) by the applet.
      */
     public int getRealMaxWidth() {
         return (this.realMaxWidth == Integer.MAX_VALUE) ? this.maxWidth
                 : this.realMaxWidth;
     }
 
     /** @param realMaxWidth the realMaxWidth to set */
     void setRealMaxWidth(int realMaxWidth) {
         this.realMaxWidth = realMaxWidth;
     }
 
     /** @return Returns the targetPictureFormat. */
     public String getTargetPictureFormat() {
         return this.targetPictureFormat;
     }
 
     public ImageFileConversionInfo getImageFileConversionInfo() {
         return imageFileConversionInfo;
     }
 
     /**
      * we expect e.g. "png,bmp:jpg;gif:png;"
      * 
      * @param targetPictureFormat the targetPictureFormat to set
      * @throws JUploadException if the conversionList is erroneous
      */
     void setTargetPictureFormat(String targetPictureFormat)
             throws JUploadException {
         this.targetPictureFormat = targetPictureFormat;
         imageFileConversionInfo = new ImageFileConversionInfo(targetPictureFormat);
     }
 
     /**
      * @return <ul>
      *         <li><code>true</code>, if the the original file extension should
      *         be kept</li>
      *         <li><code>false</code>, if the the original file extension should
      *         be changed to the target picture format, that the file has been
      *         converted to</li>
      *         </ul>
      */
     public boolean getKeepOrigExtension() {
         return this.keepOrigExtension;
     }
 
     /**
      * @param keepOrigExtension if the original file extension should be kept '
      *            <code>true</code>', or changed '<code>false</code>' (if the
      *            image was converted)
      */
     void setKeepOrigExtension(boolean keepOrigExtension)
             throws JUploadException {
         this.keepOrigExtension = keepOrigExtension;
     }
 
     /**
      * This method manages the applet parameters that are specific to this
      * class. The super.setProperty method is called for other properties.
      * 
      * @param prop The property which value should change
      * @param value The new value for this property. If invalid, the default
      *            value is used.
      * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void setProperty(String prop, String value) throws JUploadException {
         // The, we check the local properties.
         if (prop.equals(PROP_FILE_CHOOSER_IMAGE_PREVIEW)) {
             setFileChooserImagePreview(getContext().parseBoolean(value,
                     getFileChooserImagePreview()));
         } else if (prop.equals(PROP_HIGH_QUALITY_PREVIEW)) {
             setHighQualityPreview(getContext().parseBoolean(value,
                     this.highQualityPreview));
         } else if (prop.equals(PROP_MAX_HEIGHT)) {
             setMaxHeight(getContext().parseInt(value, this.maxHeight));
         } else if (prop.equals(PROP_MAX_WIDTH)) {
             setMaxWidth(getContext().parseInt(value, this.maxWidth));
         } else if (prop.equals(PROP_PICTURE_COMPRESSION_QUALITY)) {
             setPictureCompressionQuality(getContext().parseFloat(value,
                     this.pictureCompressionQuality));
         } else if (prop.equals(PROP_PICTURE_TRANSMIT_METADATA)) {
             setPictureTransmitMetadata(getContext().parseBoolean(value,
                     this.pictureTransmitMetadata));
         } else if (prop.equals(PROP_REAL_MAX_HEIGHT)) {
             setRealMaxHeight(getContext().parseInt(value, this.realMaxHeight));
         } else if (prop.equals(PROP_REAL_MAX_WIDTH)) {
             setRealMaxWidth(getContext().parseInt(value, this.realMaxWidth));
         } else if (prop.equals(PROP_TARGET_PICTURE_FORMAT)) {
             setTargetPictureFormat(value);
         } else if (prop.equals(PROP_KEEP_ORIG_EXTENSION)) {
             setKeepOrigExtension(getContext().parseBoolean(value,
                     this.keepOrigExtension));
         } else {
             // Otherwise, transmission to the mother class.
             super.setProperty(prop, value);
         }
     }
 
     /** @see DefaultUploadPolicy#displayParameterStatus() */
     @Override
     public void displayParameterStatus() {
         super.displayParameterStatus();
 
         displayDebug("======= Parameters managed by PictureUploadPolicy", 30);
         displayDebug(PROP_FILE_CHOOSER_IMAGE_PREVIEW + ": "
                 + getFileChooserImagePreview(), 30);
         displayDebug(PROP_HIGH_QUALITY_PREVIEW + " : "
                 + this.highQualityPreview, 30);
         displayDebug(PROP_PICTURE_COMPRESSION_QUALITY + " : "
                 + getPictureCompressionQuality(), 30);
         displayDebug(PROP_PICTURE_TRANSMIT_METADATA + " : "
                 + getPictureTransmitMetadata(), 30);
         displayDebug(PROP_MAX_WIDTH + " : " + this.maxWidth + ", "
                 + PROP_MAX_HEIGHT + " : " + this.maxHeight, 30);
         displayDebug(PROP_REAL_MAX_WIDTH + " : " + this.realMaxWidth + ", "
                 + PROP_REAL_MAX_HEIGHT + " : " + this.realMaxHeight, 30);
         displayDebug(PROP_STORE_BUFFERED_IMAGE + " : "
                 + this.storeBufferedImage, 30);
         displayDebug(PROP_TARGET_PICTURE_FORMAT + " : "
                 + this.targetPictureFormat, 30);
         displayDebug("Format conversions : " + getImageFileConversionInfo(), 40);
         displayDebug("", 30);
     }
 
     /**
      * Calls the {@link DefaultUploadPolicy#setWaitCursor()} method, then erases
      * the picture panel specific cursor.
      * 
      * @see DefaultUploadPolicy#setCursor(Cursor)
      */
     @Override
     public Cursor setWaitCursor() {
         Cursor previousCursor = super.setWaitCursor();
         this.picturePanel.setCursor(null);
         return previousCursor;
     }
 
     /**
      * Calls the {@link DefaultUploadPolicy#setCursor(Cursor)} method, then set
      * the picture panel specific cursor.
      * 
      * @see DefaultUploadPolicy#setCursor(Cursor)
      */
     @Override
     public Cursor setCursor(Cursor cursor) {
         Cursor oldCursor = super.setCursor(null);
         this.picturePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
         return oldCursor;
     }
 
     /**
      * Creates the file chooser, from the default implementation, then add an
      * accessory to preview pictures.
      * 
      * @see UploadPolicy#createFileChooser()
      */
     @Override
     public JUploadFileChooser createFileChooser() {
         JUploadFileChooser jufc = super.createFileChooser();
         if (getFileChooserImagePreview()) {
             jufc.setAccessory(new JUploadImagePreview(jufc, this));
         }
         return jufc;
     }
 
     /**
      * Returns an icon, calculated from the image content. Currently only
      * pictures managed by ImageIO can be displayed. Once upon a day, extracting
      * the first picture of a video may become reality... ;-) <BR>
      * Note: this method is called in a dedicated thread by the
      * JUploadFileChooser, to avoid to calculate the icon for all pictures, when
      * opening a new folder.
      * 
      * @return The calculated ImageIcon, or null if no picture can be extracted.
      * @see UploadPolicy#fileViewGetIcon(File)
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT
      */
     @Override
     public Icon fileViewGetIcon(File file) {
         return PictureFileData.getImageIcon(file, getFileChooserIconSize(),
                 getFileChooserIconSize());
     }
 
     @Override
     public String getUploadFilename(FileData fileData, int index)
             throws JUploadException {
         String fileName = fileData.getFileName();
         if (!keepOrigExtension) {
             String targetFormatOrNull = imageFileConversionInfo
                     .getTargetFormatOrNull(fileData.getFileExtension());
             if (targetFormatOrNull != null) {
 
                int endIndex = fileName.length()
                        - fileData.getFileExtension().length();
                 StringBuilder newFilename = new StringBuilder(fileName
                         .substring(0, endIndex));
                 newFilename.append(targetFormatOrNull);
                 fileName = newFilename.toString();
             }
         }
 
         return getEncodedFilename(fileName);
     }
 
     /**
      * Implementation of the ImageObserver interface
      * 
      * @param arg0
      * @param arg1
      * @param arg2
      * @param arg3
      * @param arg4
      * @param arg5
      * @return true or false
      */
     public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
             int arg4, int arg5) {
         return true;
     }
 }
