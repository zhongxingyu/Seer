 package gui.controller;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Color;
 
 import gui.EditorView;
 import misc.Editor;
 import misc.ExecutionHandler;
 
 public class EditorController implements MouseListener, ModifyListener, VerifyListener, VerifyKeyListener, FocusListener {
         private EditorView editorframe;
         private Editor editor;
         public EditorController(Editor editor, EditorView editorframe) {
                 this.editorframe = editorframe;
                 this.editor = editor;
                 this.editorframe.getTextField().addFocusListener(this);
                 this.editorframe.getTextField().addModifyListener(this);
                 this.editorframe.getTextField().addVerifyKeyListener(this);
                 this.editorframe.getLineNumbers().addMouseListener(this);
                 this.editorframe.getLineNumbers().addFocusListener(this);
         }
         public Editor getEditor() {
                 return this.editor;
         }
         
         public EditorView getEditorView() {
         		return this.editorframe;
         }
         
         @Override
         public void modifyText(ModifyEvent e) {
                 // TODO REST
                 editor.setSource(editorframe.getText());
         }
         @Override
         public void verifyText(VerifyEvent e) {
                 // TODO need?
 //              System.out.println(e.character);
         }
         @Override
         public void verifyKey(VerifyEvent e) {
                 if ((e.stateMask == SWT.CTRL) && (e.character == '\u001A')) {
                         //undo
                         e.doit = false;
                         this.editor.undo();
                 }
         }
         @Override
         public void focusGained(FocusEvent e) {
                 // TODO Auto-generated method stub
                 System.out.println("focus");
                 int test;
                 //TODO nicht vorhandene zeilen
                 //TODO nur doppelklick funktioniert
                 // => ZUM TESTEN: Focus nur auf vorhandene Zeilen, daher erst enter drücken
                 System.out.println(test = editorframe.getLineNumbers().getCaretOffset());
                 System.out.println(test = editorframe.getLineNumbers().getLineAtOffset(test));
                 editorframe.getTextField().setFocus();
                 int pos = 0;
                 for(int i = 0; i < test; i++) {
                         int linelength;
                         if(i < this.editorframe.getTextField().getLineCount()) {
                                 linelength = this.editorframe.getTextField().getLine(i).length();
                         } else {
                                 linelength = 0;
                         }
                         if(linelength == 0) {
                                 pos += 2;
                         } else {
                                 pos += linelength+2;
                         }
                 }
                 if(pos % 2 != 0) {
                         pos++;
                 }
                 //TODO WEG
                 //editorframe.getTextField().setCaretOffset(pos);
                 System.out.println(pos); 
         	
         		if (e.getSource() == this.editorframe.getLineNumbers()) {
         			this.editorframe.getTextField().setFocus();
         		}
         } 
         
 		@Override
 		public void mouseDoubleClick(MouseEvent e) {
 			if (e.getSource() == this.editorframe.getLineNumbers()) {
 				int offset = this.editorframe.getLineNumbers().getCaretOffset();
 				int lineCount = this.editorframe.getTextField().getLineCount();
 				if (offset > lineCount - 1 || this.editorframe.getTextField().getLine(offset) == null 
						|| !this.editorframe.getTextField().getLine(offset).contains(";")) {
 					this.editorframe.getTextField().setFocus();
 					return;
 				} 
 				this.editorframe.getLineNumbers().getLineBullet
     				(this.editorframe.getLineNumbers().getCaretOffset()).style.background 
     				= new Color(this.editorframe.getLineNumbers().getDisplay(), 142, 189, 247);
 				this.editorframe.getLineNumbers().redraw();
 				this.editorframe.getTextField().setFocus();
 				
 				this.editor.addBreakpoint(offset + 1); 
 			}			
 		}
         
         @Override
         public void focusLost(FocusEvent e) {
                 // TODO Auto-generated method stub
 
         }
 		@Override
 		public void mouseDown(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 		@Override
 		public void mouseUp(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 }
