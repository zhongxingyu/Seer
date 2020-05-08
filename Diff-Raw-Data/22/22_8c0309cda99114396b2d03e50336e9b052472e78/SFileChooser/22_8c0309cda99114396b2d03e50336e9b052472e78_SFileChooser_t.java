 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
import org.wings.plaf.FileChooserCG;

import javax.servlet.http.HttpUtils;
 import java.io.File;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  * A filechooser shows a textfield with a browse-button to enter a file.
  * The file is uploaded via HTTP and made accessible to the WingS application.
  *
  * <p>The uploaded file is stored temporarily in the filesystem of the
  * server with a unique name, so that uploaded files with the same
  * filename do not clash. You can access this internal name with
  * the {@link #getFileDir()} and {@link #getFileId()} methods. 
  * The user provided filename can be queried with the 
  * {@link #getFileName()} method.
  *
  * Since the file is stored temporarily in the filesystem, you should
  * {@link File#delete()} it, when you are done with it.
  * However, if you don't delete the file yourself, it is eventually 
  * being removed by the Java garbage collector, if you haven't renamed it
  * (see {@link #getFile()}).
  *
  * <p>The form, you add this SFileChooser to, needs to have the encoding type
  * <code>multipart/form-data</code> set 
  * (form.{@link SForm#setEncodingType(String) setEncodingType("multipart/form-data")}).
  *
  * <p>You can limit the size of files to be uploaded, so it is hard to make
  * a denial-of-service (harddisk, bandwidth) attack from outside to your
  * server. You can modify the maximum content length to be posted in
  * {@link org.wings.session.Session#setMaxContentLength(int)}. This is
  * 64 kByte by default, so you might want to change this in your application.
  *
  * <p>
  * The SFileChooser notifies the form if something has gone 
  * wrong with uploading a file. 
  * <p>
  * <b>Szenario</b>
  * Files that are too big to be uploaded are blocked
  * very early in the upload-process (if you are curious: this is done in
  * {@link org.wings.session.MultipartRequest}).
  * At that time, only a partial input is
  * read, the rest is discarded to thwart denial of service attacks. Since we
  * read only part of the input, we cannot make sure, that <em>all</em>
  * parameters are gathered from the input, thus we cannot just deliver the
  * events contained, since they might be incomplete. However, the file
  * chooser needs to be informed, that something went wrong as to present
  * an error message to the user. So in that case, only <em>one</em> event
  * is delivered to the enclosing <em>form</em>, that contains this
  * SFileChooser.
  *
  * <p>Note, that in this case, this will <em>not</em> trigger the action
  * listener that you might have added to the submit-button.
  * This means, that you <em>always</em> should add your action listener
 * to the {@link SForm} ({@link SForm#addActionListener(java.awt.event.ActionListener)}),
  * <em>not</em> the submit button.
  *
  * @author <a href="mailto:HEngels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:H.Zeller@acm.org">Henner Zeller</a>
  * @version $Revision$
  */
 public class SFileChooser
     extends SComponent
     implements LowLevelEventListener
 {
     private static final String cgClassID = "FileChooserCG";
     private final static Logger logger = Logger.getLogger("org.wings");
 
     /**
      * maximum visible amount of characters in the file chooser.
      */
     protected int columns = 16;
 
     protected String fileNameFilter = null;
 
     protected Class filter    = null;
     protected String fileDir  = null;
     protected String fileName = null;
     protected String fileId   = null;
     protected String fileType = null;
 
     /**
      * the temporary file created on upload. This file is automatically
      * removed if and when it is not accessible anymore.
      */
     protected TempFile currentFile = null;
 
     /**
      * the temporary file created on upload. This file is automatically
      * removed if and when it is not accessible anymore.
      */
     protected IOException exception = null;
 
     /**
      * Creates a new FileChooser. 
      */
     public SFileChooser() {}
 
     /**
      * Find the form, this FileChooser is embedded in.
      */
     protected final SForm getParentForm() {
         SComponent parent = getParent();
 
         while ( parent!=null && !(parent instanceof SForm) ) {
             parent = parent.getParent();
         }
 
         return (SForm)parent;
     }
 
     /**
      * notifies the parent form, to fire action performed. This is necessary, 
      * if an exception in parsing a MultiPartRequest occurs, e.g. upload 
      * file is too big.
      */
     protected final void notifyParentForm() {
         SForm form = getParentForm();
 
         if ( form!=null ) {
             SForm.addArmedComponent(form);
         }
     }
 
     /**
      * Set the visible amount of columns in the textfield.
      *
      * @param c columns; '-1' sets the default that is browser dependent.
      */
     public void setColumns(int c) {
         int oldColumns = columns;
         columns = c;
         if (columns != oldColumns)
             reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * returns the number of visible columns.
      *
      * @return number of visible columns.
      */
     public int getColumns() {
         return columns;
     }
 
     /**
      * Unlike the swing filechooser that allows to match certain file-suffices,
      * this sets the <em>mimetype</em> to be accepted. This filter may be fully
      * qualified like <code>text/html</code> or can contain
      * a wildcard in the subtype like <code>text/ *</code>. Some browsers
      * may as well accept a file-suffix wildcard as well. 
      *
      * <p>In any case, you hould check the result, since you cannot assume,
      * that the browser
      * actually does filter. Worse, browsers may not guess the
      * correct type so users cannot upload a file even if it has the correct
      * type. So, bottomline, it is generally a good idea to let the file 
      * name filter untouched, unless you know bugs of the browser at the 
      * other end of the wire...
      *
      * @param mimeFilter the mime type to be filtered.
      */
     public void setFileNameFilter(String mimeFilter) {
         fileNameFilter = mimeFilter;
     }
 
     /**
      * returns the current filename filter. This is a mimetype-filter
      *
      * @see #setFileNameFilter(String)
      * @return the current filename filter or 'null', if no filename filter
      *         is provided.
      */
     public String getFileNameFilter() {
         return fileNameFilter;
     }
 
     /**
      * @deprecated use {@link #getFileName()}
      */
     public String getFilename() throws IOException {
         return getFileName();
     }
 
     /**
      * Returns the filename, that has been given by the user in the
      * upload text-field.
      *
      * @return the filename, given by the user.
      * @exception IOException if something went wrong with the upload (most
      *            likely, the maximum allowed filesize is exceeded, see
      *            {@link org.wings.session.Session#setMaxContentLength(int)})
      */
     public String getFileName() throws IOException {
         if ( exception!=null )
             throw exception;
 
         return fileName;
     }
 
     /**
      * @deprecated use {@link #getFileDir()}
      */
     public String getFiledir() throws IOException {
         return getFileDir();
     }
 
     /**
      * Returns the name of the system directory, the file has been stored 
      * temporarily in. You won't need this, unless you want to access the
      * file directly.  Don't store the value you receive here for use later, 
      * since the SFileChooser does its own garbage collecting of unused files.
      *
      * @return the pathname of the system directory, the file is stored in.
      * @exception IOException if something went wrong with the upload (most
      *            likely, the maximum allowed filesize is exceeded, see
      *            {@link org.wings.session.Session#setMaxContentLength(int)})
      */
     public String getFileDir() throws IOException {
         if ( exception!=null )
             throw exception;
 
         return fileDir;
     }
 
     /**
      * @deprecated use {@link #getFileId()}
      */
     public String getFileid() throws IOException {
         return getFileId();
     }
 
     /**
      * Returns the internal ID of this file, that has been assigned at upload
      * time. This ID is unique to prevent clashes with other uploaded files.
      * You won't need this, unless you want to access the file directly. Don't
      * store the value you receive here for later use, since the SFileChooser
      * does its own garbage collecting of unused files.
      *
      * @return the internal, unique file id given to the uploaded file.
      * @exception IOException if something went wrong with the upload (most
      *            likely, the maximum allowed filesize is exceeded, see
      *            {@link org.wings.session.Session#setMaxContentLength(int)})
 
      */
     public String getFileId() throws IOException {
         if ( exception!=null )
             throw exception;
 
         return fileId;
     }
 
     /**
      * @deprecated use {@link #getFileType()}
      */
     public String getFiletype() throws IOException {
         return getFileType();
     }
 
     /**
      * Returns the mime type of this file, if known.
      *
      * @return the mime type of this file.
      * @exception IOException if something went wrong with the upload (most
      *            likely, the maximum allowed filesize is exceeded, see
      *            {@link org.wings.session.Session#setMaxContentLength(int)})
      */
     public String getFileType() throws IOException {
         if ( exception!=null )
             throw exception;
 
         return fileType;
     }
 
     /**
      * pseudonym for {@link #getFile()} (see there).
      *
      * @return a File to access the content of the uploaded file.
      * @exception IOException if something went wrong with the upload (most
      *            likely, the maximum allowed filesize is exceeded, see
      *            {@link org.wings.session.Session#setMaxContentLength(int)})
      */
     public File getSelectedFile() throws IOException {
         return getFile();
     }
 
     /**
      * resets this FileChooser (no file selected). It does not remove an 
      * upload filter!.
      * reset() will <em>not</em> remove a previously selected file from
      * the local tmp disk space, so as long as you have a reference to
      * such a file, you can still access it. If you don't have a reference
      * to the file, it will automatically be removed when the file object
      * is garbage collected.
      */
     public void reset() {
         currentFile = null;
         fileId  = null;
         fileDir = null;
         fileType = null;
         fileName = null;
         exception = null;
     }
 
     /**
      * returns the file, that has been uploaded. Use this, to open and
      * read from the file uploaded by the user. Don't use this method
      * to query the actual filename given by the user, since this file
      * wraps a system generated file with a different (unique) name.
      * Use {@link #getFileName()} instead.
      *
      * <p>The file returned here
      * will delete itself if you loose the reference to it and it is
      * garbage collected to avoid filling up the filesystem (This doesn't 
      * mean, that you shouldn't be a good programmer and delete the 
      * file yourself, if you don't need it anymore :-).
      * If you rename() the file to use it somewhere else,
      * it is regarded not temporary anymore and thus will <em>not</em> 
      * be removed from the filesystem.
      *
      * @return a File to access the content of the uploaded file.
      * @exception IOException if something went wrong with the upload (most
      *            likely, the maximum allowed filesize is exceeded, see
      *            {@link org.wings.session.Session#setMaxContentLength(int)})
      */
     public File getFile() throws IOException {
         if ( exception!=null )
             throw exception;
 
         return currentFile;
     }
 
     /**
      * An FilterOutputStream, that filters incoming files. You can use 
      * UploadFilters to inspect the stream or rewrite it to some own
      * format.
      * 
      * @param filter the Class that is instanciated to filter incoming
      *               files.
      */
     public void setUploadFilter(Class filter) {
         if (!FilterOutputStream.class.isAssignableFrom(filter))
             throw new IllegalArgumentException(filter.getName() + " is not a FilterOutputStream!");
 
         UploadFilterManager.registerFilter(getLowLevelEventId(), filter);
         this.filter = filter;
     }
 
     /**
      * Returns the upload filter set in {@link #setUploadFilter(Class)}
      *
      * @return
      */
     public Class getUploadFilter() { return filter; }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public FilterOutputStream getUploadFilterInstance() {
         return UploadFilterManager.getFilterInstance(getLowLevelEventId());
     }
 
     //-- Plaf specific methods
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(FileChooserCG cg) {
         super.setCG(cg);
     }
 
     // -- Implementation of LowLevelEventListener
     public void processLowLevelEvent(String action, String[] values) {
         exception = null;
 
         String value = values[0];
 
         if ( "exception".equals(value) ) {
             exception = new IOException(values[1]);
 
             notifyParentForm();
         } else {
             try {
                 Hashtable params = HttpUtils.parseQueryString(value);
                 String[] arr;
                 arr = (String[])params.get("dir");
                 this.fileDir = (arr != null)?arr[0]:null;
                 arr = (String[])params.get("name");
                 this.fileName = (arr != null)?arr[0]:null;
                 arr = (String[])params.get("id");
                 this.fileId = (arr != null)?arr[0]:null;
                 arr = (String[])params.get("type");
                 this.fileType = (arr != null)?arr[0]:null;
                 if (fileDir != null && fileId != null) {
                     currentFile = new TempFile(fileDir, fileId);
                 }
             }
             catch ( Exception e ) {
                 logger.log(Level.SEVERE, null, e);
             }
         }
     }
 
     public void fireIntermediateEvents() {
     }
 
     public void fireFinalEvents() {
     }
 
     public boolean checkEpoch() {
         return true;
     }
 
     /**
      * A temporary file. This file removes its representation in the
      * filesysten, when there are no references to it (i.e. it is garbage
      * collected)
      */
     private static class TempFile extends File {
         private boolean isTemp;
         public TempFile(String parent, String child) {
             super(parent, child);
             isTemp = true;
         }
 
         /**
          * when this file is renamed, then it is not temporary anymore,
          * thus will not be removed on cleanup.
          */
         public boolean renameTo(File newfile) {
             boolean success;
             success = super.renameTo(newfile);
             isTemp &= !success; // we are not temporary anymore on success.
             return success;
         }
 
         /**
          * removes the file in the filesystem, if it is still temporary.
          */
         private void cleanup() {
             if (isTemp) {
                 delete();
             }
         }
         
         /**
          * do a cleanup, if this temporary file is garbage collected.
          */
         protected void finalize() throws Throwable {
             super.finalize();
             if (isTemp) logger.fine("garbage collect file " + getName());
             cleanup();
         }
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
