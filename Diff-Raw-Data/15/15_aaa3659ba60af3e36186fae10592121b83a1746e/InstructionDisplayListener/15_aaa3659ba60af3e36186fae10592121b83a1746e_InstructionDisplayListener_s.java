 package net.sf.anathema.basics.ui.forms;
 
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Text;
 
public final class InstructionDisplayListener extends MouseAdapter implements FocusListener, SelectionListener {
   private static final String EMPTY_TEXT = ""; //$NON-NLS-1$
 
   private final Text textfield;
   private final String instruction;
 
   private final SelectionListener selectionListener;
 
   public static InstructionDisplayListener Connect(
       Text textfield,
       SelectionListener selectionListener,
       String instruction) {
     InstructionDisplayListener instructionListener = new InstructionDisplayListener(
         textfield,
         selectionListener,
         instruction);
     textfield.addFocusListener(instructionListener);
     textfield.addMouseListener(instructionListener);
     textfield.addSelectionListener(instructionListener);
     return instructionListener;
   }
 
   private InstructionDisplayListener(Text textfield, SelectionListener selectionListener, String instruction) {
     this.textfield = textfield;
     this.selectionListener = selectionListener;
     this.instruction = instruction;
     showInstruction();
   }
 
   @Override
   public void focusGained(FocusEvent e) {
     hideInstruction();
   }
 
   @Override
   public void focusLost(FocusEvent e) {
     showInstruction();
   }
 
   @Override
   public void mouseDown(MouseEvent e) {
     hideInstruction();
   }
 
   private void hideInstruction() {
     if (textfield.getText().equals(instruction)) {
       clearText();
     }
   }
 
   private void clearText() {
     textfield.setText(EMPTY_TEXT);
   }
 
   private void showInstruction() {
     if (textfield.getText().equals(EMPTY_TEXT)) {
       textfield.setText(instruction);
     }
   }
 
   @Override
   public void widgetDefaultSelected(SelectionEvent e) {
     selectionListener.widgetDefaultSelected(e);
     clearText();
   }
 
   @Override
   public void widgetSelected(SelectionEvent e) {
     selectionListener.widgetSelected(e);
     clearText();
   }
 }
