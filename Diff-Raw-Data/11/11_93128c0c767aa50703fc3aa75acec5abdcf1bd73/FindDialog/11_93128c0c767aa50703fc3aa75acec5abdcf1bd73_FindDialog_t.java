 package net.milanaleksic.guitransformer.editor;
 
 import net.milanaleksic.guitransformer.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.*;
 
 import javax.inject.Inject;
 
 /**
  * User: Milan Aleksic
  * Date: 5/14/12
  * Time: 7:37 PM
  */
 public class FindDialog {
 
     @Inject
     private Transformer transformer;
 
     @Inject
     private MainForm mainForm;
 
     @EmbeddedComponent
     private Text searchText;
 
     private Shell shell;
 
     private String text;
 
     @EmbeddedEventListener(component = "shell", event = SWT.Close)
     private final Listener shellCloseListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             shell.dispose();
         }
     };
 
     @EmbeddedEventListener(component = "btnAccept", event = SWT.Selection)
     private final Listener btnAcceptSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             text = searchText.getText();
            shell.dispose();
         }
     };
 
     @EmbeddedEventListener(component = "btnCancel", event = SWT.Selection)
     private final Listener btnCancelSelectionListener = new Listener() {
         @Override
         public void handleEvent(Event event) {
             shell.close();
         }
     };
 
     public String getSearchString() {
         try {
             final TransformationContext transformationContext = transformer.fillManagedForm(mainForm.getShell(), this);
             this.shell = transformationContext.getShell();
 
             text = null;
 
             shell.open();
 
            Display display = shell.getDisplay();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }

             return text;
         } catch (TransformerException e) {
             e.printStackTrace();
             System.exit(1);
             return null;
         }
     }
 
 }
