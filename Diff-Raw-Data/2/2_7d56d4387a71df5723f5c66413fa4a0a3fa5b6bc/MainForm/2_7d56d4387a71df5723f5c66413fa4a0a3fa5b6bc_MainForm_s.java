 package net.milanaleksic.guitransformer.editor;
 
 import com.google.common.base.*;
 import net.milanaleksic.guitransformer.*;
 import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.dnd.*;
 import org.eclipse.swt.widgets.*;
 
 import javax.annotation.Nullable;
 import javax.inject.*;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * User: Milan Aleksic
  * Date: 5/14/12
  * Time: 3:00 PM
  */
 public class MainForm {
 
     @Inject
     private Transformer transformer;
 
     @Named(value = "EditorTransformer")
     @Inject
     private Transformer editorTransformer;
 
     @Inject
     private ErrorDialog errorDialog;
 
     @Inject
     private ResourceBundleProvider resourceBundleProvider;
 
     @EmbeddedComponent
     private StyledText editor;
 
     @EmbeddedComponent
     private Label infoLabel;
 
     @EmbeddedComponent
     private org.eclipse.swt.widgets.List contextWidgets;
 
     /* editing context */
     private Shell currentShell = null;
     private File currentFile = null;
     private boolean modified = false;
     private Exception lastException = null;
 
     /* editor's own context */
     private Shell shell;
     private ResourceBundle resourceBundle;
 
     @EmbeddedEventListener(component = "editorDropTarget", event = DND.Drop)
     private final Listener editorDropTargetDropListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             if (event.data instanceof String[]) {
                 String[] typedData = (String[]) event.data;
                 if (typedData != null && typedData.length > 0) {
                     openFile(new File(typedData[0]));
                 }
             }
         }
     };
 
     @EmbeddedEventListener(component = "infoLabel", event = SWT.MouseDown)
     private final Listener infoLabelMouseDownListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             if (lastException == null)
                 return;
             errorDialog.showMessage(getStackTrace(lastException));
         }
 
         public String getStackTrace(Throwable t) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             t.printStackTrace(pw);
             return sw.toString();
         }
     };
 
     private class RunnableListener implements Listener, Runnable {
 
         private ExecutorService executor = Executors.newSingleThreadExecutor();
 
         @Override
         public void handleEvent(Event event) {
             showInformation("", null);
             String text = editor.getText();
             if (Strings.isNullOrEmpty(text))
                 return;
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     shell.getDisplay().syncExec(RunnableListener.this);
                 }
             });
         }
 
         private void removePreviousShell() {
             if (currentShell == null)
                 return;
             Shell shell = currentShell;
             if (!shell.isDisposed())
                 shell.dispose();
             currentShell = null;
         }
 
         @Override
         public void run() {
             try {
                 TransformationContext nonManagedForm = editorTransformer.createNonManagedForm(editor.getText());
                 Shell newShell = nonManagedForm.getShell();
                 newShell.setLocation(20, 20);
 
                 removePreviousShell();
 
                 currentShell = newShell;
                 currentShell.open();
 
                 updateAvailableWidgets(nonManagedForm);
 
                 editor.setFocus();
 
                 modified = true;
             } catch (TransformerException e) {
                 showInformation(String.format(resourceBundle.getString("mainForm.transformationError"), e.getMessage()), e);
             } catch (Exception e) {
                 showInformation(resourceBundle.getString("mainForm.error"), e);
             }
         }
 
         private void updateAvailableWidgets(TransformationContext nonManagedForm) {
             contextWidgets.setItems(new String[] {});
             for (Map.Entry<String,Object> entry : nonManagedForm.getMappedObjects().entrySet()) {
                 contextWidgets.add(String.format("[%s] - %s", entry.getKey(), entry.getValue().getClass().getName()));
             }
         }
 
         public void shutdown() {
             executor.shutdownNow();
         }
     }
 
     @EmbeddedEventListener(component = "editor", event = SWT.Modify)
     private final RunnableListener editorModifyListener = new RunnableListener();
 
     @EmbeddedEventListener(component = "editor", event = SWT.KeyDown)
     private final Listener editorKeyDown = new Listener() {
         @Override
         public void handleEvent(Event event) {
             if (Character.toLowerCase(event.keyCode) == 'a' && (event.stateMask & SWT.CTRL) == SWT.CTRL) {
                 editor.selectAll();
                 return;
             }
         }
     };
 
     @EmbeddedEventListener(component = "btnNew", event = SWT.Selection)
     private final Listener btnNewSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             setCurrentFile(null);
             editor.setText("");
         }
     };
 
     @EmbeddedEventListener(component = "btnOpen", event = SWT.Selection)
     private final Listener btnOpenSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             FileDialog dlg = new FileDialog(shell, SWT.OPEN);
             dlg.setFilterNames(new String[]{resourceBundle.getString("mainForm.openFilters")});
             dlg.setFilterExtensions(new String[]{"*.gui"}); //NON-NLS
             final String selectedFile = dlg.open();
             if (selectedFile == null)
                 return;
             openFile(new File(selectedFile));
         }
     };
 
     @EmbeddedEventListener(component = "btnSave", event = SWT.Selection)
     private final Listener btnSaveSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             saveCurrentDocument();
         }
     };
 
     @EmbeddedEventListener(component = "btnSaveAs", event = SWT.Selection)
     private final Listener btnSaveAsSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             saveDocumentAs();
         }
     };
 
     @EmbeddedEventListener(component = "btnExit", event = SWT.Selection)
     private final Listener btnExitSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             shell.close();
         }
     };
 
     @EmbeddedEventListener(component = "shell", event = SWT.Close)
     private final Listener shellCloseListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             try {
                 if (!modified)
                     return;
                 int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL;
                 MessageBox messageBox = new MessageBox(shell, style);
                 messageBox.setText(resourceBundle.getString("mainForm.information"));
                 messageBox.setMessage(resourceBundle.getString("mainForm.saveBeforeClosing"));
                 switch (messageBox.open()) {
                     case SWT.CANCEL:
                         event.doit = false;
                         break;
                     case SWT.YES:
                         saveCurrentDocument();
                         event.doit = true;
                         break;
                     case SWT.NO:
                         event.doit = true;
                         break;
                 }
             } finally {
                 if (event.doit)
                     editorModifyListener.shutdown();
             }
         }
     };
 
     private void openFile(File targetFile) {
         if (!targetFile.exists()) {
             showError(String.format(resourceBundle.getString("mainForm.fileDoesNotExist"),
                     targetFile.getAbsolutePath()));
             return;
         }
         try {
             editor.setText(com.google.common.io.Files.toString(targetFile, Charsets.UTF_8));
             setCurrentFile(targetFile);
         } catch (IOException e) {
             e.printStackTrace();
             showError(String.format(resourceBundle.getString("mainForm.ioError.open"), targetFile.getAbsolutePath()));
         }
     }
 
     private void saveCurrentDocument() {
         if (currentFile == null && editor.getText().trim().length() == 0)
             return;
         if (currentFile == null) {
             saveDocumentAs();
             return;
         }
         try {
             com.google.common.io.Files.write(editor.getText(), currentFile, Charsets.UTF_8);
         } catch (IOException e) {
             e.printStackTrace();
             showError(String.format(resourceBundle.getString("mainForm.ioError.save"), currentFile.getAbsolutePath()));
         }
         modified = false;
     }
 
     private void saveDocumentAs() {
         FileDialog dlg = new FileDialog(shell, SWT.SAVE);
         dlg.setFilterNames(new String[]{resourceBundle.getString("mainForm.openFilters")});
         dlg.setFilterExtensions(new String[]{"*.gui"}); //NON-NLS
         final String selectedFile = dlg.open();
         if (selectedFile == null)
             return;
         setCurrentFile(new File(selectedFile));
         saveCurrentDocument();
     }
 
     private void showInformation(String infoText, @Nullable Exception exception) {
         infoLabel.setText(infoText);
         lastException = exception;
     }
 
     private void showError(String errorMessage) {
         MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
         box.setMessage(errorMessage);
         box.setText(resourceBundle.getString("mainForm.error"));
         box.open();
     }
 
     public boolean isDisposed() {
         return shell.isDisposed();
     }
 
     private void setCurrentFile(@Nullable File file) {
         this.currentFile = file;
         shell.setText(String.format("%s - [%s]",  //NON-NLS
                 resourceBundle.getString("mainForm.title"),
                 currentFile == null
                         ? resourceBundle.getString("mainForm.newFile")
                         : currentFile.getName()));
         modified = false;
     }
 
     public void init() {
         try {
             resourceBundle = resourceBundleProvider.getResourceBundle();
             final TransformationContext transformationContext = transformer.fillManagedForm(this);
             this.shell = transformationContext.getShell();
 
             postTransformation(transformationContext);
 
             shell.open();
         } catch (TransformerException e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     private void postTransformation(TransformationContext transformationContext) {
         transformationContext.<DropTarget>getMappedObject("editorDropTarget").get() //NON-NLS
                 .setTransfer(new Transfer[]{FileTransfer.getInstance()});
         editorTransformer.setDoNotCreateModalDialogs(true);
         setCurrentFile(null);
     }
 
     Shell getShell() {
         return shell;
     }
 }
