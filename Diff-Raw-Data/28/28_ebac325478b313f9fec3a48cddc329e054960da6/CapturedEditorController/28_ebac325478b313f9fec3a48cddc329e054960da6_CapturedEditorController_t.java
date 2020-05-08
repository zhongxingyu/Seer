 package org.jetbrains.plugins.xml.searchandreplace.ui.controller.replace;
 
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.event.CaretEvent;
 import com.intellij.openapi.editor.event.CaretListener;
 import com.intellij.openapi.editor.event.DocumentEvent;
 import com.intellij.openapi.editor.event.DocumentListener;
 import com.intellij.openapi.editor.ex.FocusChangeListener;
 import com.intellij.openapi.editor.impl.EditorImpl;
 import com.intellij.openapi.editor.markup.EffectType;
 import com.intellij.openapi.editor.markup.HighlighterTargetArea;
 import com.intellij.openapi.editor.markup.RangeHighlighter;
 import com.intellij.openapi.editor.markup.TextAttributes;
 import com.intellij.psi.PsiElement;
 import org.jetbrains.plugins.xml.searchandreplace.replace.CapturePresentation;
 import org.jetbrains.plugins.xml.searchandreplace.search.Node;
 import org.jetbrains.plugins.xml.searchandreplace.ui.controller.captures.Capture;
 import org.jetbrains.plugins.xml.searchandreplace.ui.controller.captures.CapturesListener;
 import org.jetbrains.plugins.xml.searchandreplace.ui.controller.captures.CapturesManager;
 import org.jetbrains.plugins.xml.searchandreplace.ui.controller.search.ConstraintController;
 
 import java.awt.*;
import java.util.*;
 import java.util.List;
 
 import static java.util.Collections.sort;
 
 public class CapturedEditorController implements CaptureDropHandler.CaptureDropHandlerDelegate, DocumentListener, CaretListener, CapturesListener {
 
   @Override
   public void beforeDocumentChange(DocumentEvent event) {}
 
   @Override
   public void documentChanged(DocumentEvent event) {
     updateEntries();
   }
 
   private static class BadCapture extends Capture {
     public BadCapture(String captureId) {
       super(null);
 
       CapturePresentation presentation = new CapturePresentation();
       presentation.setIdentifier(captureId);
       presentation.setTextColor(Color.RED);
 
       setPresentation(presentation);
     }
 
     @Override
     public String value(PsiElement element) {
       return null;
     }
   }
 
   private void updateEntries() {
     clearEntries();
     String text = editor.getDocument().getText();
     for (int i = 0; i < text.length(); ++i) {
       if (text.charAt(i) == '$') {
         String captureId = parseCaptureId(text, i+1);
         if (captureId == null) continue;
         Capture capture = findCaptureWithId(captureId);
         if (capture == null) {
           capture = new BadCapture(captureId);
         }
         insertCaptureEntry(capture, i, captureId);
       }
     }
     highlightCapturesUnderCaret();
   }
 
   private void clearEntries() {
    for (CaptureEntry ce : entries) {
      ConstraintController constraintController = ce.capture.getConstraintController();
      if (constraintController != null) {
        constraintController.highlightCaptures(null);
      }
    }
     editor.getMarkupModel().removeAllHighlighters();
     entries.clear();
   }
 
   private Capture findCaptureWithId(String captureId) {
     return capturesManager.findById(captureId);
   }
 
   private String parseCaptureId(String text, int i) {
     String number = "";
     int j = i;
     while (j < text.length() && Character.isDigit(text.charAt(j))) {
       number += text.charAt(j++);
     }
     return number.isEmpty() ? null : number;
   }
 
   @Override
   public void caretPositionChanged(CaretEvent e) {
     int offset = editor.logicalPositionToOffset(e.getNewPosition());
     highlightCapturesUnderCaret(offset);
   }
 
   private void highlightCapturesUnderCaret(int offset) {
    Set<Capture> highlighted = new HashSet<Capture>();
     for (CaptureEntry ce : entries) {
       boolean inside = ce.range.getStartOffset() <= offset && offset <= ce.range.getEndOffset();
       highlightEntry(ce, inside);

      ConstraintController constraintController = ce.capture.getConstraintController();
      if (constraintController != null && !highlighted.contains(ce.capture)) {
        constraintController.highlightCaptures(inside ? ce.capture : null);
      }
      
      if (inside) {
        highlighted.add(ce.capture);
      }
     }
   }
 
   private void highlightCapturesUnderCaret() {
     highlightCapturesUnderCaret(editor.getComponent().hasFocus());
   }
 
   private void highlightCapturesUnderCaret(boolean hasFocus) {
     highlightCapturesUnderCaret(hasFocus ? editor.getCaretModel().getOffset() : -1);
   }
 
   @Override
   public void captureBecameInvalid(Capture c) {
     updateEntries();
   }
 
   @Override
   public void captureAdded(Capture c) {
     updateEntries();
   }
 
   public boolean validateInput() {
     for (CaptureEntry ce : entries) {
       if (ce.capture.getConstraintController() == null) {
         return false;
       }
     }
     return true;
   }
 
   private List<CaptureEntry> entries = new ArrayList<CaptureEntry>();
 
   void setEditor(final EditorImpl editor) {
     this.editor = editor;
   }
 
   private void highlightEntry(CaptureEntry ce, boolean inside) {
     ConstraintController constraintController = ce.capture.getConstraintController();
 
     if (ce.range.isValid()) {
       TextAttributes textAttributes = ce.range.getTextAttributes();
       assert textAttributes != null;
       if (constraintController == null) {
         textAttributes.setForegroundColor(Color.RED);
       } else {
         textAttributes.setForegroundColor(ce.capture.presentation().getTextColor());
         if (inside && ce.range.getStartOffset() != ce.range.getEndOffset()) {
           textAttributes.setEffectType(EffectType.BOXED);
           textAttributes.setEffectColor(Color.GREEN);
         } else if (!inside) {
           textAttributes.setEffectType(null);
         }
       }
     }
   }
 
   public Editor getEditor() {
     return editor;
   }
 
   private Editor editor;
   private CapturesManager capturesManager;
 
   public CapturedEditorController(final EditorImpl editor, CapturesManager capturesManager) {
     this.editor = editor;
     this.capturesManager = capturesManager;
 
     CaptureDropHandler dropHandler = new CaptureDropHandler(editor);
     dropHandler.setDelegate(this);
     editor.setDropHandler(dropHandler);
 
     editor.getDocument().addDocumentListener(this);
 
     editor.getCaretModel().addCaretListener(this);
 
     capturesManager.addCapturesListener(this);
 
     updateEntries();
     
     editor.addFocusListener(new FocusChangeListener() {
       @Override
       public void focusGained(Editor editor) {
         highlightCapturesUnderCaret(true);
       }
 
       @Override
       public void focusLost(Editor editor) {
         highlightCapturesUnderCaret(false);
       }
     });
   }
 
   public String resolveCaptures(Map<Node, PsiElement> match) {
     String text = editor.getDocument().getText();
     StringBuilder result = new StringBuilder();
     sort(entries, new Comparator<CaptureEntry>() {
       @Override
       public int compare(CaptureEntry captureEntry, CaptureEntry captureEntry1) {
         return captureEntry.range.getStartOffset() - captureEntry1.range.getStartOffset();
       }
     });
     int start = 0;
     for (CaptureEntry entry : entries) {
       if (entry.range.isValid()) {
         result.append(text.substring(start, entry.range.getStartOffset()));
         Capture capture = entry.capture;
         Node key = null;
         for (Map.Entry<Node, PsiElement> e : match.entrySet()) {
           key = e.getKey();
           if (key.getPredicate().flatten().contains(capture.getPredicate())) {
             break;
           }
         }
         String captureResolution = capture.value(match.get(key));
         result.append(captureResolution);
         start = entry.range.getEndOffset();
       }
     }
     result.append(text.substring(start));
     return result.toString();
   }
 
   @Override
   public void insertCapture(Capture capture, int offset) {
     editor.getDocument().insertString(offset, "$" + capture.presentation().getIdentifier());
   }
 
   private CaptureEntry insertCaptureEntry(Capture capture, int offset, String captureId) {
     CapturePresentation presentation = capture.presentation();
 
     RangeHighlighter marker = editor.getMarkupModel().addRangeHighlighter(offset,
             offset + presentation.getIdentifier().length()+1,
             10,
             new TextAttributes(presentation.getTextColor(),
                     presentation.getBackgroundColor(),
                     null, null, 0),
             HighlighterTargetArea.EXACT_RANGE);
     CaptureEntry captureEntry = new CaptureEntry(marker, capture);
     entries.add(captureEntry);
     return captureEntry;
   }
 }
