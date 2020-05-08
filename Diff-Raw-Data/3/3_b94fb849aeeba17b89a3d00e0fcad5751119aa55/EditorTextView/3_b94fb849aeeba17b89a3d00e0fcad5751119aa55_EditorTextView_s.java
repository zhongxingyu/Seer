 /*
  * EditorTextView.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.ui;
 
 import org.gnome.gdk.EventKey;
 import org.gnome.gdk.Keyval;
 import org.gnome.gdk.ModifierType;
 import org.gnome.gdk.Rectangle;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.InputMethod;
 import org.gnome.gtk.Menu;
 import org.gnome.gtk.MenuItem;
 import org.gnome.gtk.SimpleInputMethod;
 import org.gnome.gtk.TextBuffer;
 import org.gnome.gtk.TextIter;
 import org.gnome.gtk.TextMark;
 import org.gnome.gtk.TextTag;
 import org.gnome.gtk.TextView;
 import org.gnome.gtk.Widget;
 import org.gnome.gtk.WrapMode;
 
 import quill.textbase.Change;
 import quill.textbase.Common;
 import quill.textbase.DeleteTextualChange;
 import quill.textbase.Extract;
 import quill.textbase.FormatTextualChange;
 import quill.textbase.FullTextualChange;
 import quill.textbase.HeadingSegment;
 import quill.textbase.InsertTextualChange;
 import quill.textbase.Markup;
 import quill.textbase.NormalSegment;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.Span;
 import quill.textbase.SplitStructuralChange;
 import quill.textbase.StructuralChange;
 import quill.textbase.TextChain;
 import quill.textbase.TextualChange;
 
 import static quill.client.Quill.ui;
 import static quill.ui.Format.tagForMarkup;
 
 abstract class EditorTextView extends TextView
 {
     protected final TextView view;
 
     private TextBuffer buffer;
 
     private InputMethod input;
 
     private TextMark selectionBound, insertBound;
 
     private TextChain chain;
 
     /**
      * Cache of the offset into the TextBuffer of the insertBound TextMark.
      */
     private int insertOffset;
 
     private Markup insertMarkup;
 
     /**
      * The model element that we are representing.
      */
     private Segment segment;
 
     EditorTextView(Segment segment) {
         super();
         this.view = this;
         this.segment = segment;
 
         setupTextView();
         setupInsertMenu();
         setupContextMenu();
 
         displaySegment();
 
         hookupKeybindings();
         hookupFormatManagement();
     }
 
     private void setupTextView() {
         buffer = new TextBuffer();
 
         selectionBound = buffer.getSelectionBound();
         insertBound = buffer.getInsert();
 
         view.setBuffer(buffer);
         view.setWrapMode(WrapMode.WORD);
 
         view.setMarginLeft(3);
         view.setBorderWidth(2);
 
         view.setAcceptsTab(true);
 
         if (isSpellChecked()) {
             view.attachSpell();
         }
     }
 
     /**
      * Override this and return true if you want TAB characters to be inserted
      * rather than swollowed.
      */
     protected boolean isCodeBlock() {
         return false;
     }
 
     /**
      * Override this and return false if you want spell checking off
      */
     protected boolean isSpellChecked() {
         return true;
     }
 
     private void hookupKeybindings() {
         input = new SimpleInputMethod();
 
         input.connect(new InputMethod.Commit() {
             public void onCommit(InputMethod source, String text) {
                 insertText(text);
             }
         });
 
         view.connect(new Widget.KeyPressEvent() {
             public boolean onKeyPressEvent(Widget source, EventKey event) {
                 final Keyval key;
                 final ModifierType mod;
 
                 /*
                  * This is magic, actually. Both normal keystrokes and
                  * composed sequences will be delivered to us above in the
                  * InputMethod.Commit handler.
                  */
 
                 if (input.filterKeypress(event)) {
                     return true;
                 }
 
                 /*
                  * Otherwise, we begin the logic to check and see if we're
                  * going to handle it ourselves.
                  */
 
                 key = event.getKeyval();
 
                 /*
                  * Let default keybindings handle cursor movement keys and for
                  * a few other special keys we don't need to handle.
                  */
 
                 if ((key == Keyval.Up) || (key == Keyval.Right)) {
                     if (insertOffset == 0) {
                         // ui.primary.moveCursorBack();
                         return false;
                     }
                 }
 
                 if ((key == Keyval.Down) || (key == Keyval.Left)) {
                     if (insertOffset == chain.length()) {
                         // ui.primary.moveCursorNext();
                         return false;
                     }
                 }
 
                 if ((key == Keyval.Home) || (key == Keyval.End) || (key == Keyval.PageUp)
                         || (key == Keyval.PageDown) || (key == Keyval.Compose)) {
                     return false;
                 }
 
                 /*
                  * Let function keys be handled by PrimaryWindow.
                  */
 
                 if ((key == Keyval.F1) || (key == Keyval.F2) || (key == Keyval.F3) || (key == Keyval.F4)
                         || (key == Keyval.F5) || (key == Keyval.F6) || (key == Keyval.F7)
                         || (key == Keyval.F8) || (key == Keyval.F9) || (key == Keyval.F10)
                         || ((key == Keyval.F11) || (key == Keyval.F12))) {
                     return false;
                 }
 
                 if (key == Keyval.Escape) {
                     // deliberate no-op
                     return true;
                 }
 
                 if (key == Keyval.Insert) {
                     popupInsertMenu();
                     return true;
                 }
 
                 /*
                  * Other special keys that we DO handle. The newline case is
                  * interesting. We let a \n fly but clear any current
                  * formatting first.
                  */
                 if (key == Keyval.Return) {
                     insertMarkup = null;
                    return false;
                 } else if (key == Keyval.Delete) {
                     deleteAt();
                     return true;
                 } else if (key == Keyval.BackSpace) {
                     deleteBack();
                     return true;
                 }
 
                 /*
                  * Context menu!
                  */
 
                 if (key == Keyval.Menu) {
                     // TODO
                     return false;
                 }
 
                 /*
                  * Let modifier keys through; input methods, cursor movement,
                  * and selection seems to depend on this.
                  */
 
                 if ((key == Keyval.ShiftLeft) || (key == Keyval.ShiftRight) || (key == Keyval.AltLeft)
                         || (key == Keyval.AltRight) || (key == Keyval.ControlLeft)
                         || (key == Keyval.ControlRight) || (key == Keyval.SuperLeft)
                         || (key == Keyval.SuperRight)) {
                     return false;
                 }
 
                 /*
                  * Tab is a strange one. At first glance it is tempting to set
                  * the TextView to not accept them and to have Tab change
                  * focus, but there is the case of program code in a
                  * preformatted block which might need indent support. So we
                  * swollow it unless we're in a preformatted code block.
                  */
 
                 if (key == Keyval.Tab) {
                     if (isCodeBlock()) {
                         return false;
                     } else {
                         return true;
                     }
                 }
 
                 /*
                  * Now on to processing normal keystrokes.
                  */
 
                 mod = event.getState();
 
                 if ((mod == ModifierType.NONE) || (mod == ModifierType.SHIFT_MASK)) {
                     /*
                      * Normal keystroke. Chances are we probably don't get
                      * here that often.
                      */
                     return false;
                 }
                 if (mod == ModifierType.CONTROL_MASK) {
                     if (key == Keyval.a) {
                         // select all; pass through
                         return false;
                     } else if (key == Keyval.b) {
                         toggleMarkup(Common.BOLD);
                         return true;
                     } else if (key == Keyval.c) {
                         copyText();
                         return true;
                     } else if (key == Keyval.g) {
                         insertImage();
                         return true;
                     } else if (key == Keyval.i) {
                         toggleMarkup(Common.ITALICS);
                         return true;
                     } else if (key == Keyval.v) {
                         pasteText();
                         return true;
                     } else if (key == Keyval.x) {
                         cutText();
                         return true;
                     } else {
                         /*
                          * No keybinding in the editor, but PrimaryWindow will
                          * handle program wide accelerators.
                          */
                         return false;
                     }
                 } else if (mod.contains(ModifierType.CONTROL_MASK)
                         && mod.contains(ModifierType.SHIFT_MASK)) {
                     if (key == Keyval.Space) {
                         clearFormat();
                         return true;
                     } else if (key == Keyval.A) {
                         toggleMarkup(Common.APPLICATION);
                         return true;
                     } else if (key == Keyval.L) {
                         toggleMarkup(Common.LITERAL);
                         return true;
                     } else if (key == Keyval.F) {
                         toggleMarkup(Common.FILENAME);
                         return true;
                     } else if (key == Keyval.M) {
                         // function or _m_ethod
                         toggleMarkup(Common.FUNCTION);
                         return true;
                     } else if (key == Keyval.O) {
                         toggleMarkup(Common.COMMAND);
                         return true;
                     } else if (key == Keyval.T) {
                         toggleMarkup(Common.TYPE);
                         return true;
                     } else if (key == Keyval.U) {
                         /*
                          * Special to GTK's default input method, so pass
                          * through!
                          */
                         return false;
                     } else {
                         /*
                          * Nothing special, pass through.
                          */
                         return false;
                     }
                 } else if (mod == ModifierType.LOCK_MASK) {
                     /*
                      * No keybinding needed, pass through.
                      */
                     return false;
                 }
 
                 /*
                  * We didn't handle it, and are assuming we're capable of
                  * handing all keyboard input. Boom :(
                  */
 
                 throw new IllegalStateException("\n" + "Unhandled " + key + " with " + mod);
             }
         });
 
         view.connect(new Widget.KeyReleaseEvent() {
             public boolean onKeyReleaseEvent(Widget source, EventKey event) {
                 if (input.filterKeypress(event)) {
                     return true;
                 }
                 return false;
             }
         });
     }
 
     private void insertText(String text) {
         final Extract removed;
         final Span span;
         final TextualChange change;
         final TextIter selection;
         final int selectionOffset, offset, width;
 
         span = new Span(text, insertMarkup);
 
         if (buffer.getHasSelection()) {
             selection = buffer.getIter(selectionBound);
             selectionOffset = selection.getOffset();
 
             offset = normalizeOffset(insertOffset, selectionOffset);
             width = normalizeWidth(insertOffset, selectionOffset);
 
             removed = chain.extractRange(offset, width);
             change = new FullTextualChange(chain, offset, removed, span);
         } else {
             change = new InsertTextualChange(chain, insertOffset, span);
         }
 
         ui.apply(change);
     }
 
     private void pasteText() {
         final Extract stash, removed;
         final TextualChange change;
         final TextIter selection;
         final int selectionOffset, offset, width;
 
         stash = ui.getClipboard();
         if (stash == null) {
             return;
         }
 
         if (buffer.getHasSelection()) {
             selection = buffer.getIter(selectionBound);
             selectionOffset = selection.getOffset();
 
             offset = normalizeOffset(insertOffset, selectionOffset);
             width = normalizeWidth(insertOffset, selectionOffset);
 
             removed = chain.extractRange(offset, width);
             change = new FullTextualChange(chain, offset, removed, stash);
         } else {
             change = new InsertTextualChange(chain, insertOffset, stash);
         }
 
         /*
          * Propegate the change. After this wends its way though the layers,
          * it will result in ComponentEditorWindow calling this.affect().
          */
 
         ui.apply(change);
     }
 
     private void deleteBack() {
         final TextIter start, end;
 
         end = buffer.getIter(insertBound);
 
         if (buffer.getHasSelection()) {
             start = buffer.getIter(selectionBound);
         } else {
             if (end.isStart()) {
                 return;
             }
             start = end.copy();
             start.backwardChar();
         }
 
         deleteRange(start, end);
     }
 
     private void deleteAt() {
         final TextIter start, end;
 
         start = buffer.getIter(insertBound);
 
         if (buffer.getHasSelection()) {
             end = buffer.getIter(selectionBound);
         } else {
             if (start.isEnd()) {
                 return;
             }
             end = start.copy();
             end.forwardChar();
         }
 
         deleteRange(start, end);
     }
 
     /**
      * Effect a deletion from start to end.
      */
     private void deleteRange(TextIter start, TextIter end) {
         int alpha, omega, offset, width;
         final Extract range;
         final TextualChange change;
 
         alpha = start.getOffset();
         omega = end.getOffset();
 
         offset = normalizeOffset(alpha, omega);
         width = normalizeWidth(alpha, omega);
 
         range = chain.extractRange(offset, width);
         change = new DeleteTextualChange(chain, offset, range);
 
         ui.apply(change);
     }
 
     private void toggleMarkup(Markup format) {
         TextIter start, end;
         int alpha, omega, offset, width;
         final TextualChange change;
         final Extract original;
 
         /*
          * If there is a selection then toggle the markup applied there.
          * Otherwise, change the current insertion point formats.
          */
 
         if (buffer.getHasSelection()) {
             start = selectionBound.getIter();
             end = insertBound.getIter();
 
             alpha = start.getOffset();
             omega = end.getOffset();
 
             offset = normalizeOffset(alpha, omega);
             width = normalizeWidth(alpha, omega);
 
             original = chain.extractRange(offset, width);
 
             change = new FormatTextualChange(chain, offset, original, format);
             ui.apply(change);
             this.affect(change);
 
         } else {
             if (insertMarkup == format) {
                 insertMarkup = null; // OR, something more block oriented?
             } else {
                 insertMarkup = format;
             }
         }
     }
 
     private void insertImage() {}
 
     /**
      * Cause the given Change to be reflected in the TextView. The assumption
      * is made that the backing TextBuffer is in a state where applying this
      * Change makes sense.
      */
     void affect(Change change) {
         final StructuralChange structural;
         TextIter start, end;
         int offset;
 
         if (change instanceof StructuralChange) {
             structural = (StructuralChange) change;
 
             offset = structural.getOffset();
             start = buffer.getIter(offset);
             end = buffer.getIterEnd();
             buffer.delete(start, end);
 
         } else if (change instanceof FormatTextualChange) {
             imposeFormatChange((TextualChange) change);
         } else if (change instanceof TextualChange) {
             imposeTextualChange((TextualChange) change);
         } else {
             throw new IllegalStateException("Unknown Change type");
         }
     }
 
     private void imposeTextualChange(TextualChange change) {
         TextIter start, end;
         Extract r;
         int i;
         Span s;
 
         start = buffer.getIter(change.getOffset());
 
         r = change.getRemoved();
         if (r != null) {
             end = buffer.getIter(change.getOffset() + r.getWidth());
             buffer.delete(start, end);
             start = end;
         }
 
         r = change.getAdded();
         if (r != null) {
             for (i = 0; i < r.size(); i++) {
                 s = r.get(i);
                 buffer.insert(start, s.getText(), tagForMarkup(s.getMarkup()));
             }
         }
     }
 
     private void imposeFormatChange(TextualChange change) {
         TextIter start, end;
         Extract r;
         int i, offset;
         Span s;
         TextTag tag;
 
         r = change.getAdded();
         if (r == null) {
             return;
         }
 
         offset = change.getOffset();
 
         for (i = 0; i < r.size(); i++) {
             s = r.get(i);
 
             start = buffer.getIter(offset);
             offset += s.getWidth();
             end = buffer.getIter(offset);
 
             /*
              * FUTURE this is horribly inefficient compared to just adding or
              * removing the tag that has changed. But it is undeniably easy to
              * express. To do this properly we'll have to get the individual
              * Markup and whether it was added or removed from the
              * FormatChange.
              * 
              * FIXME this clears the error underlining from GtkSpell!
              */
 
             buffer.removeAllTags(start, end);
             tag = tagForMarkup(s.getMarkup());
             if (tag == null) {
                 continue;
             }
             buffer.applyTag(tag, start, end);
         }
     }
 
     /**
      * Revert this Change, removing it's affect on the view. A
      * DeleteTextualChange will cause an insertion, etc.
      */
     void reverse(Change obj) {
         final StructuralChange structural;
         final TextualChange textual;
         final TextIter start, end;
         Extract r;
         int i;
         Span s;
 
         if (obj instanceof StructuralChange) {
             structural = (StructuralChange) obj;
 
             throw new UnsupportedOperationException("Not yet implemented " + structural); // FIXME
 
         } else if (obj instanceof TextualChange) {
             textual = (TextualChange) obj;
 
             /*
              * And now do what is necessary to reflect the change in this UI.
              */
 
             start = buffer.getIter(textual.getOffset());
 
             r = textual.getAdded();
             if (r != null) {
                 end = buffer.getIter(textual.getOffset() + r.getWidth());
                 buffer.delete(start, end);
             }
 
             r = textual.getRemoved();
             if (r != null) {
                 for (i = 0; i < r.size(); i++) {
                     s = r.get(i);
                     buffer.insert(start, s.getText(), tagForMarkup(s.getMarkup()));
                 }
             }
         }
     }
 
     private void copyText() {
         extractText(true);
     }
 
     private void cutText() {
         extractText(false);
     }
 
     private void extractText(boolean copy) {
         final TextIter start, end;
         int alpha, omega, offset, width;
         final Extract extract;
         final TextualChange change;
 
         /*
          * If there's no selection, we can't "Copy" or "Cut"
          */
 
         if (!buffer.getHasSelection()) {
             return;
         }
 
         start = buffer.getIter(selectionBound);
         end = buffer.getIter(insertBound);
 
         alpha = start.getOffset();
         omega = end.getOffset();
 
         offset = normalizeOffset(alpha, omega);
         width = normalizeWidth(alpha, omega);
 
         /*
          * Copy the range to clipboard, being the "Copy" behviour.
          */
 
         extract = chain.extractRange(offset, width);
         ui.setClipboard(extract);
 
         if (copy) {
             return;
         }
 
         /*
          * And now delete the selected range, which makes this the "Cut"
          * behaviour.
          */
 
         change = new DeleteTextualChange(chain, offset, ui.getClipboard());
         ui.apply(change);
     }
 
     private static int normalizeOffset(int alpha, int omega) {
         if (omega > alpha) {
             return alpha;
         } else {
             return omega;
         }
     }
 
     private static int normalizeWidth(int alpha, int omega) {
         final int width;
 
         width = omega - alpha;
 
         if (width < 0) {
             return -width;
         } else {
             return width;
         }
     }
 
     /**
      * Hookup signals to aggregate formats to be used on a subsequent
      * insertion. The insertMarkup array starts empty, and builds up as
      * formats are toggled by the user. When the cursor moves, the Set is
      * changed to the formatting applying on character back.
      */
     private void hookupFormatManagement() {
         insertMarkup = null;
 
         buffer.connect(new TextBuffer.NotifyCursorPosition() {
             public void onNotifyCursorPosition(TextBuffer source) {
                 final TextIter pointer;
                 int offset;
                 final Rectangle rect;
                 final Allocation alloc;
 
                 pointer = buffer.getIter(insertBound);
                 offset = pointer.getOffset();
 
                 insertOffset = offset;
                 if (offset != 0) {
                     offset--;
                 }
                 insertMarkup = chain.getMarkupAt(offset);
 
                 rect = view.getLocation(pointer);
                 alloc = view.getAllocation();
 
                 ui.primary.scrollEditorToShow(alloc.getY() + rect.getY(), rect.getHeight() + 5);
             }
         });
     }
 
     private void clearFormat() {
         final TextIter start, end;
         final Extract original;
         final TextualChange change;
         int alpha, omega, offset, width;
 
         /*
          * If there is a selection then clear the markup applied there. This
          * may not be the correct implementation; there could be Markups which
          * are structural and not block or inline.
          */
 
         if (buffer.getHasSelection()) {
             start = selectionBound.getIter();
             end = insertBound.getIter();
 
             alpha = start.getOffset();
             omega = end.getOffset();
 
             offset = normalizeOffset(alpha, omega);
             width = normalizeWidth(alpha, omega);
 
             original = chain.extractRange(offset, width);
             change = new FormatTextualChange(chain, offset, original);
 
             ui.apply(change);
             this.affect(change);
         }
 
         /*
          * Deactivate the any insert formatting.
          */
 
         insertMarkup = null;
     }
 
     /**
      * Make this EditorTextView reflect the contents of the Segment this
      * EditorTextView was initialized with.
      */
     private void displaySegment() {
         final Extract entire;
         TextIter pointer;
         int i;
         Span s;
 
         /*
          * Easy enough to just set the internal TextStack backing this editor
          * to the one belonging to the Segment passed in.
          */
 
         chain = segment.getText();
 
         /*
          * But now we need to cycle over its Spans and place its content into
          * the view.
          */
 
         entire = chain.extractAll();
         if (entire == null) {
             return;
         }
 
         pointer = buffer.getIterStart();
 
         for (i = 0; i < entire.size(); i++) {
             s = entire.get(i);
             buffer.insert(pointer, s.getText(), tagForMarkup(s.getMarkup()));
         }
     }
 
     private Menu split;
 
     private Class<?>[] types;
 
     private String[] texts;
 
     private MenuItem createMenuItem(final String label, final Class<?> type) {
         final MenuItem result;
 
         result = new MenuItem(label, new MenuItem.Activate() {
             public void onActivate(MenuItem source) {
                 try {
                     handleInsertSegment((Segment) type.newInstance());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
 
         return result;
     }
 
     /*
      * Yes it would be "better" to write this such that the two arrays weren't
      * coupled but instead strongly typed objects with two+ fields.
      */
     private void setupInsertMenu() {
         int i;
         MenuItem item;
 
         split = new Menu();
 
         types = new Class<?>[] {
                 NormalSegment.class, PreformatSegment.class, QuoteSegment.class, HeadingSegment.class
         };
 
         texts = new String[] {
                 "Normal _paragraphs", "Preformatted _code block", "Block _quote", "Section _heading"
         };
 
         for (i = 0; i < types.length; i++) {
             item = createMenuItem(texts[i], types[i]);
             split.append(item);
         }
 
         split.showAll();
     }
 
     /*
      * This is a bit hideous.
      */
     private void popupInsertMenu() {
         final Widget[] children;
         int i;
         final Series series;
         final int len;
         final Segment next;
 
         /*
          * Turn off the type that the current Segment is
          */
 
         children = split.getChildren();
 
         for (i = 0; i < types.length; i++) {
             if (types[i].isInstance(segment)) {
                 children[i].setSensitive(false);
             } else {
                 children[i].setSensitive(true);
             }
         }
 
         /*
          * if we're at the last character of an existing Segment, then turn
          * off the type that's already following
          */
 
         if (insertOffset == chain.length()) {
             series = segment.getParent();
             i = series.indexOf(segment);
             len = series.size();
 
             if (i < len - 1) {
                 i++;
                 next = series.get(i);
 
                 for (i = 0; i < types.length; i++) {
                     if (types[i].isInstance(next)) {
                         children[i].setSensitive(false);
                         break;
                     }
                 }
             }
 
         }
         split.popup();
     }
 
     private void setupContextMenu() {
         /*
          * The default context menu created by TextView on a right click popup
          * is annoying in that it contains stuff about input methods and
          * unicode, all entirely unnecessary. The TextView API doesn't give us
          * anything to inhibit this nonsense, but we can dig into the packing
          * hierarchy and, as Widgets, remove them.
          */
 
         view.connect(new TextView.PopulatePopup() {
             public void onPopulatePopup(TextView source, Menu menu) {
                 Widget[] items;
                 int i;
 
                 items = menu.getChildren();
                 i = items.length - 3;
 
                 // "[separator]"
                 menu.remove(items[i++]);
 
                 // "Input Methods"
                 menu.remove(items[i++]);
 
                 // "Insert Unicode Control Character"
                 menu.remove(items[i++]);
             }
         });
     }
 
     /**
      * Take the necessary actions to create a new Segment, which you pass in.
      * If we're at the end of the view we're appending. Jump the logic to the
      * UserInterface facade.
      */
     private void handleInsertSegment(Segment addition) {
         final Change change;
 
         addition.setText(new TextChain());
 
         change = new SplitStructuralChange(segment, insertOffset, addition);
 
         ui.apply(change);
     }
 }
