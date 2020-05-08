 /**
  * Copyright (c) 2012 keyhom.c@gmail.com.
  *
  * This software is provided 'as-is', without any express or implied warranty.
  * In no event will the authors be held liable for any damages arising from
  * the use of this software.
  *
  * Permission is granted to anyone to use this software for any purpose
  * excluding commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  *
  *     1. The origin of this software must not be misrepresented; you must not
  *     claim that you wrote the original software. If you use this software
  *     in a product, an acknowledgment in the product documentation would be
  *     appreciated but is not required.
  *
  *     2. Altered source versions must be plainly marked as such, and must not
  *     be misrepresented as being the original software.
  *
  *     3. This notice may not be removed or altered from any source
  *     distribution.
  */
 package org.vimide.eclipse.vimplugin.editors;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.net.URI;
 
 import org.apache.commons.exec.OS;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vimide.eclipse.vimplugin.VimInstanceManager;
 import org.vimide.eclipse.vimplugin.VimInstanceManager.VimInstance;
 import org.vimide.eclipse.vimplugin.VimideVimpluginPlugin;
 import org.vimide.eclipse.vimplugin.VimpluginMessage;
 import org.vimide.eclipse.vimplugin.VimpluginSupport;
 import org.vimide.vimplugin.server.VimBufferSession;
 import org.vimide.vimplugin.server.VimEvent;
 import org.vimide.vimplugin.server.VimEventListener;
 
 /**
  * Provides an editor to eclipse which is backed by a vim instance.
  * 
  * @author keyhom (keyhom.c@gmail.com)
  */
 public class VimEditor extends TextEditor {
 
     /**
      * Logger
      */
     static final Logger LOGGER = LoggerFactory.getLogger(VimEditor.class);
 
     /**
      * The field to grab for Windows/Win32.
      */
     static final String win32WID = "handle";
 
     /**
      * The field to grab for GTK2.
      */
     static final String gtkWID = "embededHandle";
 
     /**
      * A shell to open {@link MessageDialog MessageDialogs}.
      */
     private Shell shell;
     private Composite parent;
 
     private boolean documentListen;
 
     private VimInstance vimInstance;
     private int bufferId;
     private IDocument document;
     private VimDocumentProvider documentProvider;
 
     private IFile selectFile;
 
     private Canvas vimCanvas;
 
     /**
      * Creates an new VimEditor instance.
      */
     public VimEditor() {
         super();
         bufferId = -1; // not really necessary but set it to an invalid buffer.
         setDocumentProvider(documentProvider = new VimDocumentProvider());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
      */
     @Override
     public void createPartControl(Composite parent) {
         this.parent = parent;
         this.shell = parent.getShell();
 
         if (!VimpluginSupport.isGvimAvailable()) {
             MessageDialog dialog = new MessageDialog(shell, "Vimplugin", null,
                     VimpluginMessage.gvim_not_found, MessageDialog.ERROR,
                     new String[] { IDialogConstants.OK_LABEL,
                             IDialogConstants.CANCEL_LABEL }, 0) {
 
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.jface.dialogs.MessageDialog#buttonPressed(int)
                  */
                 @Override
                 protected void buttonPressed(int buttonId) {
                     super.buttonPressed(buttonId);
 
                     // show preferences to configure the gvim.
                     if (buttonId == IDialogConstants.OK_ID) {
                         PreferenceDialog dialog = PreferencesUtil
                                 .createPreferenceDialogOn(
                                         shell,
                                         "org.vimide.eclipse.vimplugin.preferences.VimpluginPreference",
                                         null, null);
                         if (null != dialog) {
                             dialog.open();
                         }
                     }
                 }
 
             };
 
             dialog.open();
 
             if (!VimpluginSupport.isGvimAvailable()) {
                 throw new RuntimeException(VimpluginMessage.gvim_not_found);
             }
         }
 
         documentListen = false;
 
         if (!VimpluginSupport.isEmbedSupported()) {
             String message = NLS.bind(VimpluginMessage.gvim_not_supported,
                     VimpluginMessage.gvim_embed_not_supported);
             throw new RuntimeException(message);
         }
 
         if (!VimpluginSupport.isNbSupported()) {
             String message = NLS.bind(VimpluginMessage.gvim_not_supported,
                     VimpluginMessage.gvim_nb_not_enabled);
             throw new RuntimeException(message);
         }
 
         String projectPath = null;
         String filePath = null;
         IEditorInput input = getEditorInput();
 
         if (input instanceof IFileEditorInput) {
             selectFile = ((IFileEditorInput) input).getFile();
             IProject project = selectFile.getProject();
             IPath path = project.getRawLocation();
             if (null == path) {
                 String name = project.getName();
                 path = ResourcesPlugin.getWorkspace().getRoot()
                         .getRawLocation();
                 path = path.append(name);
             }
 
             projectPath = path.toPortableString();
 
             filePath = selectFile.getRawLocation().toPortableString();
 
             if (filePath.toLowerCase().indexOf(projectPath.toLowerCase()) != -1) {
                 filePath = filePath.substring(projectPath.length() + 1);
             }
         } else {
             URI uri = ((IURIEditorInput) input).getURI();
             filePath = uri.toString().substring("file:".length());
             filePath = filePath.replaceFirst("^/([A-Za-z]:)", "$1");
         }
 
         if (null != filePath) {
             vimCanvas = new Canvas(parent, SWT.EMBEDDED);
             createVim(projectPath, filePath, vimCanvas);
         }
     }
 
     /**
      * Creates a vim instance figuring out if it should be external of embed.
      * 
      * @param workingDir the working directory.
      * @param filePath the file path for opening.
      * @param parent the parent composite to embed.
      * @param embedded
      * @param tabbed
      */
     private void createVim(String workingDir, final String filePath,
             Composite parent) {
         long wid = 0;
         
         Field f = null;
         try{
             if (OS.isFamilyWindows()) {
               f = Composite.class.getField("handle");
             } else{
               f = Composite.class.getField("embeddedHandle");
             }
             
             wid = f.getLong(parent);
         }catch(NoSuchFieldException nsfe){
         } catch (IllegalArgumentException e) {
         } catch (IllegalAccessException e) {
         }
         
         final int bufferId = VimideVimpluginPlugin.getDefault()
                 .getNumberOfBuffers().getAndIncrement();
         final Object waitObject = new Object();
         
         // create embedded vim instance.
         vimInstance = VimInstanceManager.getInstance().createVimInstance();
         vimInstance.start(workingDir, wid);
         vimInstance.invokeAtStartup(new VimEventListener() {
 
             @Override
             public void actived(VimEvent event) throws Exception {
                 LOGGER.debug("StartupDone...");
 
                 event.getSession().removeEventListener("startupDone", this);
 
                 final VimBufferSession session = event.getSession();
                 session.commandBuilder().bufferId(bufferId).command("editFile")
                         .stringData(filePath).toCommandData().flush();
                 if (documentListen) {
                     session.commandBuilder().bufferId(bufferId)
                             .command("startDocumentListen").toCommandData()
                             .flush();
                 } else {
                     session.commandBuilder().bufferId(bufferId)
                             .command("stopDocumentListen").toCommandData()
                             .flush();
                 }
 
                 session.commandBuilder().bufferId(bufferId).command("setTitle")
                         .stringData(getTitle()).toCommandData().flush();
 
                 synchronized (waitObject) {
                     waitObject.notify();
                 }
             }
         });
 
         synchronized (waitObject) {
             try {
                 waitObject.wait(5000);
             } catch (final InterruptedException ignore) {
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite,
      *      org.eclipse.ui.IEditorInput)
      */
     @Override
     public void init(IEditorSite site, IEditorInput input)
             throws PartInitException {
         setSite(site);
         setInput(input);
 
         try {
             document = documentProvider.createDocument(input);
         } catch (final Exception e) {
             error(VimpluginMessage.document_create_failed, e);
         }
 
         // set vim title image.
         Image titleImage = new Image(null, this.getClass().getResourceAsStream(
                 "/icons/vim16x16.gif"));
         setTitleImage(titleImage);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#isEditable()
      */
     @Override
     public boolean isEditable() {
         return false;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.ui.texteditor.StatusTextEditor#setFocus()
      */
     @Override
     public void setFocus() {
         if (isEmbedded()) {
             parent.setFocus();
         }
     }
 
     public void setTitleTo(final String path) {
         Display.getDefault().asyncExec(new Runnable() {
 
             @Override
             public void run() {
                 String filename = path.substring(path
                         .lastIndexOf(File.separator) + 1);
                 setPartName(filename);
             }
         });
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#getDocumentProvider()
      */
     @Override
     public IDocumentProvider getDocumentProvider() {
         return documentProvider;
     }
 
     /**
      * Gets the buffer ID.
      * 
      * @return the buffer id.
      */
     public int getBufferId() {
         return bufferId;
     }
 
     /**
      * Determines if this editor is running an embedded gvim instance or not.
      * 
      * @return true if the gvim instance is embedded, false otherwise.
      */
     public boolean isEmbedded() {
         return vimInstance.isEmbedded();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.ui.editors.text.TextEditor#dispose()
      */
     public void dispose() {
         // closing the eclipse tab directly calls dispose, but not close.
         close(true);
 
         document = null;
 
         super.dispose();
     }
 
     @Override
     public void close(boolean save) {
         if (null != vimInstance) {
             vimInstance.dispose();
         }
 
         super.close(save);
     }
 
     private void error(String message, Throwable e) {
         // convert stacktrace to string
         String stacktrace;
         StringWriter sw = null;
         PrintWriter pw = null;
         try {
             sw = new StringWriter();
             pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             stacktrace = sw.toString();
         } finally {
             try {
                 if (pw != null)
                     pw.close();
                 if (sw != null)
                     sw.close();
             } catch (IOException ignore) {
             }
         }
 
         MessageDialog.openError(shell, "Vimplugin", message + stacktrace);
     }
 }
