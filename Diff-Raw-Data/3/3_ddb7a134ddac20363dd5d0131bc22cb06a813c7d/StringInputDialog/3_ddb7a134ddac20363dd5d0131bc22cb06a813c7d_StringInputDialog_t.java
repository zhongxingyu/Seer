 package edu.ualberta.med.biobank.barcodegenerator.dialogs;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 
 public class StringInputDialog extends Dialog {
 
     private String value;
 
     private String title;
     private String message;
 
     public StringInputDialog(Shell parent) {
         super(parent);
         title = "NumberInputDialog";
         message = "Please enter a valid number:";
     }
 
     public StringInputDialog(Shell parent, int style) {
         super(parent, style);
         title = "NumberInputDialog";
         message = "Please enter a valid number:";
     }
 
     public StringInputDialog(String title, String message, Shell parent,
         int style) {
         super(parent, style);
         this.title = title;
         this.message = message;
     }
 
     public String open(String defaultMessage) {
         Shell parent = getParent();
         final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER
             | SWT.APPLICATION_MODAL);
         shell.setText(title);
 
         shell.setLayout(new RowLayout());
 
         Label label = new Label(shell, SWT.NONE);
         label.setText(message);
 
         final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
 
         text.setFocus();
 
         Button buttonCancel = new Button(shell, SWT.PUSH);
         buttonCancel.setText("Cancel");
 
         final Button buttonOK = new Button(shell, SWT.PUSH);
         buttonOK.setText("Okay");
 
         text.addListener(SWT.Modify, new Listener() {
             public void handleEvent(Event event) {
                 try {
                     value = text.getText();
                     buttonOK.setEnabled(value.length() > 0);
                 } catch (Exception e) {
                     buttonOK.setEnabled(false);
                 }
             }
         });
 
         text.addListener(SWT.Verify, new Listener() {
             public void handleEvent(Event e) {
 
                 if (!e.text.matches("[{0-9, A-Za-z}]*")) {
                     e.doit = false;
                     return;
                 }
             }
         });
         text.addKeyListener(new KeyListener() {
 
             @Override
             public void keyReleased(KeyEvent e) {
 
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.character == SWT.CR) {
                     if (buttonOK.isEnabled()) {
                         shell.dispose();
                     }
                 }
             }
         });
 
         buttonOK.addListener(SWT.Selection, new Listener() {
             public void handleEvent(Event event) {
                 shell.dispose();
             }
         });
 
         buttonCancel.addListener(SWT.Selection, new Listener() {
             public void handleEvent(Event event) {
                 value = null;
                 shell.dispose();
             }
         });
         buttonOK.setEnabled(false);
 
         value = null;
 
         if (defaultMessage == null)
             defaultMessage = "";
         text.setText(defaultMessage);
 
         shell.pack();
         shell.open();
 
         Display display = parent.getDisplay();
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch())
                 display.sleep();
         }
 
         if (value != null && value.length() == 0)
             value = null;
 
         return value;
     }
 
     public static void main(String[] args) {
         Shell shell = new Shell();
         StringInputDialog dialog = new StringInputDialog("Bacon", "Fries",
             shell, SWT.NONE);
         System.out.println(dialog.open(null));
     }
 
 }
