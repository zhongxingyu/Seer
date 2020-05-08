 package org.hunmr.copywithoutselection;
 
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.editor.SelectionModel;
 import com.intellij.openapi.ui.Messages;
 import com.intellij.openapi.util.TextRange;
 import org.hunmr.common.EmacsIdeasAction;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class CopyWithoutSelectAction extends EmacsIdeasAction {
     private KeyListener _handleCopyKeyListener;
 
     public void actionPerformed(AnActionEvent e) {
         if (super.initAction(e)) {
             _contentComponent.addKeyListener(_handleCopyKeyListener);
         }
     }
 
     @Override
     protected void initMemberVariableForConvenientAccess() {
         super.initMemberVariableForConvenientAccess();
         _handleCopyKeyListener = createHandleCopyWithoutSelectionKeyListener();
     }
 
     private KeyListener createHandleCopyWithoutSelectionKeyListener() {
         return new KeyListener() {
             public void keyTyped(KeyEvent keyEvent) {
                 keyEvent.consume();
                 boolean copyFinished = handleKey(keyEvent.getKeyChar());
                 if (copyFinished) {
                     cleanupSetupsInAndBackToNormalEditingMode();
                 }
             }
 
             public void keyPressed(KeyEvent keyEvent) {
                 if (KeyEvent.VK_ESCAPE == keyEvent.getKeyChar()) {
                     cleanupSetupsInAndBackToNormalEditingMode();
                 }
             }
 
             public void keyReleased(KeyEvent keyEvent) {
             }
         };
     }
 
     private boolean handleKey(char key) {
         SelectionModel selectionModel = _editor.getSelectionModel();
 
         switch (key) {
             case 'w' :
                 selectionModel.selectWordAtCaret(false);
                 selectionModel.copySelectionToClipboard();
                 selectionModel.removeSelection();
                 break;
             case 's' :
                 TextRange tr = getTextRangeOfCurrentString();
                 if (tr != null) {
                     selectionModel.setSelection(tr.getStartOffset(), tr.getEndOffset());
                     selectionModel.copySelectionToClipboard();
                     selectionModel.removeSelection();
                 }
                 break;
             case 'l' :
                 selectionModel.selectLineAtCaret();
                 selectionModel.copySelectionToClipboard();
                 break;
             case 'p' :
                 Messages.showMessageDialog(_editor.getProject(), "copy paragraph action", "Information", Messages.getInformationIcon());
                 break;
             case 'a' :
                 Messages.showMessageDialog(_editor.getProject(), "copy among action", "Information", Messages.getInformationIcon());
                 break;
             default:
                 //show Error message, can not find command
                 break;
         }
 
         return true;
     }
 
     private TextRange getTextRangeOfCurrentString() {
         final int caretOffset = _editor.getCaretModel().getOffset();
         final int lineNumber = _document.getLineNumber(caretOffset);
         final int lineEnd = _document.getLineEndOffset(lineNumber);
         final int lineStart = _document.getLineStartOffset(lineNumber);
         final String lineText = _document.getText(new TextRange(lineStart, lineEnd));
 
        int caretOffsetToLineStart = caretOffset;
         if (isCaretBetweenSpaces()) {
             caretOffsetToLineStart = getNearestStringEndOffset(caretOffset, lineStart, lineText);
         }
 
         int strStart = lineStart;
         int strEnd = lineEnd;
 
         int index = lineText.lastIndexOf(" ", caretOffsetToLineStart - 1);
         if (index != -1) {
             strStart = lineStart + index + 1;
         }
 
         index = lineText.indexOf(" ", caretOffsetToLineStart);
         if (index != -1) {
             strEnd = lineStart + index;
         }
 
         return strEnd > strStart ? new TextRange(strStart, strEnd) : null;
     }
 
     private boolean isCaretBetweenSpaces() {
         int caretOffset = _editor.getCaretModel().getOffset();
         int textLength = _document.getTextLength();
 
         return caretOffset - 1 > 0
                && caretOffset + 1 < textLength
                && Character.isSpaceChar(_document.getText(new TextRange(caretOffset - 1, caretOffset)).charAt(0))
                && Character.isSpaceChar(_document.getText(new TextRange(caretOffset + 1, caretOffset + 2)).charAt(0));
     }
 
     private int getNearestStringEndOffset(int caretOffset, int lineStart, String lineText) {
         int caretOffsetToLineStart = caretOffset - lineStart;
         while (caretOffsetToLineStart > 0 && Character.isSpaceChar(lineText.charAt(caretOffsetToLineStart - 1))) {
             caretOffsetToLineStart--;
         }
         return caretOffsetToLineStart;
     }
 
     public void cleanupSetupsInAndBackToNormalEditingMode() {
         if (_handleCopyKeyListener != null) {
             _contentComponent.removeKeyListener(_handleCopyKeyListener);
             _handleCopyKeyListener = null;
         }
 
         super.cleanupSetupsInAndBackToNormalEditingMode();
     }
 
 }
