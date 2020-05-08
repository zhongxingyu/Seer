 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009-2010 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.ui;
 
 import java.io.IOException;
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.PdfSurface;
 import org.freedesktop.cairo.Surface;
 import org.gnome.gdk.Event;
 import org.gnome.gdk.EventKey;
 import org.gnome.gdk.Keyval;
 import org.gnome.gdk.ModifierType;
 import org.gnome.gdk.WindowState;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.Button;
 import org.gnome.gtk.ButtonsType;
 import org.gnome.gtk.Dialog;
 import org.gnome.gtk.ErrorMessageDialog;
 import org.gnome.gtk.FileChooserDialog;
 import org.gnome.gtk.FileFilter;
 import org.gnome.gtk.HBox;
 import org.gnome.gtk.IconSize;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.InfoMessageDialog;
 import org.gnome.gtk.MessageDialog;
 import org.gnome.gtk.MessageType;
 import org.gnome.gtk.Notebook;
 import org.gnome.gtk.PaperSize;
 import org.gnome.gtk.ResponseType;
 import org.gnome.gtk.Stock;
 import org.gnome.gtk.Unit;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 import org.gnome.gtk.Window;
 import org.gnome.pango.FontDescription;
 
 import parchment.format.Manuscript;
 import parchment.render.RenderEngine;
 import parchment.render.ReportRenderEngine;
 import quill.client.ImproperFilenameException;
 import quill.client.Quill;
 import quill.textbase.Change;
 import quill.textbase.ChangeStack;
 import quill.textbase.Folio;
 import quill.textbase.Origin;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 
 import static org.gnome.gtk.FileChooserAction.OPEN;
 import static org.gnome.gtk.FileChooserAction.SAVE;
 
 /**
  * The main application window, representing a single loaded (or brand new)
  * document.
  * 
  * @author Andrew Cowie
  */
 class PrimaryWindow extends Window
 {
     private Window window;
 
     // convenience referece
     private UserInterface ui;
 
     private VBox top;
 
     private HBox two;
 
     private Notebook left;
 
     private Notebook right;
 
     private ComponentEditorWidget editor;
 
     private HelpWidget help;
 
     private PreviewWidget preview;
 
     private OutlineWidget outline;
 
     /**
      * The document this PrimaryWindow is displaying.
      */
     private Manuscript manuscript;
 
     private ChangeStack stack;
 
     /**
      * The state of the document when last loaded or saved to disk, expressed
      * as the Change object current at that point. The document is unmodified
      * if the current item on the ChangeStack is this object.
      */
     private Change last;
 
     /**
      * The root of the document currently being presented by this
      * PrimaryWindow.
      */
     private Folio folio;
 
     /**
      * The series of Components (the current chapter) currently being
      * represented by this PrimaryWindow.
      */
     private Series series;
 
     PrimaryWindow() {
         super();
         setupWindow();
         setupUndoCapability();
         setupEditorSide();
         setupPreviewSide();
         hookupDefaultKeyhandlers();
         hookupWindowManagement();
         initialPresentation();
     }
 
     /*
      * This code sure has migrated around the application quite a bit. Seems
      * to make sense here now, though; after all, undo and redo are user
      * interface artifacts.
      */
     private void setupUndoCapability() {
         stack = new ChangeStack();
     }
 
     /**
      * Apply a Change to the data layer and then to this user interface.
      */
     public void apply(Change change) {
         stack.apply(change);
         editor.affect(change);
     }
 
     /**
      * Pick the latest Change off the ChangeStack, and then do something with
      * it
      */
     void undo() {
         final Change change;
 
         change = stack.undo();
 
         if (change == null) {
             return;
         }
 
         editor.reverse(change);
     }
 
     /**
      * Grab the Change that was most recently undone, and redo it.
      */
     void redo() {
         final Change change;
 
         change = stack.redo();
 
         if (change == null) {
             return;
         }
 
         editor.affect(change);
     }
 
     private void setupWindow() {
         final FontDescription desc;
 
         ui = Quill.getUserInterface();
 
         window = this;
         window.setMaximize(true);
         window.setTitle("Quill");
 
         desc = new FontDescription("Deja Vu Serif, 11");
         window.modifyFont(desc);
 
         top = new VBox(false, 0);
         window.add(top);
 
         two = new HBox(true, 0);
         top.packStart(two, true, true, 0);
     }
 
     private void setupEditorSide() {
         left = new Notebook();
         left.setShowTabs(false);
         left.setShowBorder(false);
         left.setSizeRequest(640, -1);
 
         editor = new ComponentEditorWidget(this);
         left.insertPage(editor, null, 0);
 
         two.packStart(left, true, true, 0);
     }
 
     private void setupPreviewSide() {
         right = new Notebook();
         right.setShowTabs(false);
         right.setShowBorder(false);
 
         preview = new PreviewWidget(this);
         right.add(preview);
 
         help = new HelpWidget();
         right.add(help);
 
         outline = new OutlineWidget();
         right.add(outline);
 
        two.packStart(right, true, true, 0);
     }
 
     private void initialPresentation() {
         window.showAll();
         window.present();
     }
 
     private void hookupWindowManagement() {
         window.connect(new Window.DeleteEvent() {
             public boolean onDeleteEvent(Widget source, Event event) {
                 try {
                     saveIfModified();
                     ui.shutdown();
                     return false;
                 } catch (SaveCancelledException sce) {
                     return true;
                 }
             }
         });
     }
 
     private void hookupDefaultKeyhandlers() {
         window.connect(new Widget.KeyPressEvent() {
             public boolean onKeyPressEvent(Widget source, EventKey event) {
                 final Keyval key;
                 final ModifierType mod;
 
                 key = event.getKeyval();
                 mod = event.getState();
 
                 if (mod == ModifierType.NONE) {
                     if (key == Keyval.F1) {
                         switchToHelp();
                         return true;
                     } else if (key == Keyval.F2) {
                         switchToPreview();
                         return true;
                     } else if (key == Keyval.F3) {
                         switchToOutline();
                         return true;
                     }
 
                     if ((key == Keyval.F4) || (key == Keyval.F5) || (key == Keyval.F6)
                             || (key == Keyval.F7) || (key == Keyval.F8) || (key == Keyval.F9)
                             || (key == Keyval.F10)) {
                         // nothing yet
                         return true;
                     }
 
                     else if (key == Keyval.F11) {
                         toggleFullscreen();
                         return true;
                     } else if (key == Keyval.F12) {
                         toggleRightSide();
                         return true;
                     }
                 } else if (mod == ModifierType.CONTROL_MASK) {
                     if (key == Keyval.p) {
                         printDocument();
                         return true;
                     }
                     if (key == Keyval.n) {
                         /*
                          * I was about to hook up ui.newDocument() here, but I
                          * think Ctrl+N needs to be saved for something more
                          * common and important. New segment, perhaps (though
                          * that's Ins in EditorTextView at the moment); new
                          * series would be good too.
                          */
                         return true;
                     }
                     if (key == Keyval.o) {
                         try {
                             saveIfModified();
                             openDocument();
                             return true;
                         } catch (SaveCancelledException sce) {
                             return true;
                         }
                     }
                     if (key == Keyval.q) {
                         try {
                             saveIfModified();
                             ui.shutdown();
                             return true;
                         } catch (SaveCancelledException sce) {
                             return true;
                         }
                     }
                     if (key == Keyval.s) {
                         try {
                             saveDocument();
                         } catch (SaveCancelledException e) {
                             // ignore
                         }
                         return true;
                     } else if (key == Keyval.w) {
                         /*
                          * FIXME if we evolve to holding multiple documents
                          * open at once in a single instance of Quill, then
                          * this will need to change to closing the window
                          * (document?) that is currently active instead of
                          * terminating the application.
                          */
                         try {
                             saveIfModified();
                             ui.shutdown();
                             return true;
                         } catch (SaveCancelledException sce) {
                             return true;
                         }
                     } else if (key == Keyval.y) {
                         redo();
                         return true;
                     } else if (key == Keyval.z) {
                         undo();
                         return true;
                     }
                 }
 
                 return false;
             }
         });
     }
 
     /**
      * Change the user interface from full screen to normal and back again.
      * Quill is designed to work maximized, but you get an even better
      * experience if you fullscreen.
      */
     void toggleFullscreen() {
         if (window.getWindow().getState().contains(WindowState.FULLSCREEN)) {
             window.setFullscreen(false);
         } else {
             window.setFullscreen(true);
         }
     }
 
     private boolean showingRightSide = true;
 
     /*
      * Not a documented public feature. This code is here only so we can
      * demonstrate just how hard getting the user experience correct for this
      * is. The correct end result would be keeping the vertical height of
      * previously set, and probably horizontal width as well. What is really
      * bad is that if you return to maximized with the right hand side turned
      * off suddenly the editor is super wide, and that's a horrible
      * experience.
      */
     private void toggleRightSide() {
         final Allocation alloc;
 
         if (showingRightSide) {
             alloc = left.getAllocation();
             right.hide();
             window.setMaximize(false);
             window.resize(alloc.getWidth(), 720);
             showingRightSide = false;
         } else {
             right.show();
             window.setMaximize(true);
             showingRightSide = true;
         }
     }
 
     /**
      * Change the left side to show the chapter editor.
      */
     void switchToEditor() {
         left.setCurrentPage(0);
     }
 
     /**
      * Change the right side to show the help pane.
      */
     void switchToHelp() {
         right.setCurrentPage(1);
     }
 
     /**
      * Change the right side to show the preview display, and ensure it is up
      * to date.
      */
     void switchToPreview() {
         right.setCurrentPage(0);
         preview.queueDraw();
     }
 
     /**
      * Change the right side to show the outline navigator.
      */
     void switchToOutline() {
         right.setCurrentPage(2);
         outline.renderSeries(series);
     }
 
     /**
      * Adjust the left side editor to show the supplied vertical location.
      */
     /*
      * This probably doesn't need to be a UI function; an EditorTextView
      * should know or be able to find out its parent and simply call its
      * parent's ensureVisible() method.
      */
     void scrollEditorToShow(int from, int height) {
         editor.ensureVisible(from, height);
     }
 
     /**
      * Show the nominated Series in this PrimaryWindow
      */
     // FIXME rename?
     void displayDocument(Folio folio) {
         this.manuscript = folio.getManuscript();
 
         if (folio.size() == 0) {
             throw new IllegalStateException();
         }
 
         this.folio = folio;
         this.series = folio.getSeries(0);
 
         editor.initializeSeries(series);
         preview.renderSeries(manuscript, folio);
         outline.renderSeries(series);
         this.updateTitle();
     }
 
     /**
      * Set or reset the Window title based on the text of the first Segment of
      * the currently dipslayed Series (which will be the ComponentSegment
      * leading this chapter with the chapter title).
      */
     /*
      * There is a HIG question here, as to whether the application name should
      * be in the window title. Since Open Office, Firefox, Evolution, Eclipse,
      * and Inkscape all put their titles in, we will too. In always having
      * "Quill" in the title, we do manage to avoid the transient ugliness that
      * occurs when you are just entering the chapter title for the first time
      * and have a window title with only one letter in it as you type.
      */
     void updateTitle() {
         final Segment first;
         final String title;
         final String str;
 
         first = series.get(0);
         title = first.getText().toString();
 
         if ((title == null) || (title.equals(""))) {
             str = "Quill";
         } else {
             str = title + " - Quill";
         }
 
         super.setTitle(str);
     }
 
     public void grabFocus() {
         editor.grabFocus();
     }
 
     Origin getCursor() {
         return editor.getCursor();
     }
 
     /**
      * This WILL set the filename in the Manuscript, unless cancelled.
      */
     /*
      * This is a bit messy. There is confusion of responsibilities here
      * between who owns the filename, who is responsible for carrying out IO,
      * and who drives the process.
      */
     void requestFilename() throws SaveCancelledException {
         final FileChooserDialog dialog;
         String filename;
         ResponseType response;
 
         dialog = new FileChooserDialog("Save As...", window, SAVE);
 
         while (true) {
             response = dialog.run();
             dialog.hide();
 
             if (response != ResponseType.OK) {
                 throw new SaveCancelledException();
             }
 
             filename = dialog.getFilename();
 
             try {
                 manuscript.setFilename(filename);
                 return;
             } catch (ImproperFilenameException ife) {
                 warnUserAboutFilename();
                 // try again
                 continue;
             }
         }
     }
 
     private void warnUserAboutFilename() {
         final Dialog dialog;
 
         dialog = new ErrorMessageDialog(window, "Improper filename",
                 "Parchment filenames must have a <tt>.parchment</tt> extension");
         dialog.showAll();
         dialog.setTitle("Improper filename!");
 
         dialog.run();
         dialog.hide();
     }
 
     /**
      * Cause the document to be saved. Returns false on error or if cancelled.
      */
     void saveDocument() throws SaveCancelledException {
         MessageDialog dialog;
         String filename;
 
         filename = manuscript.getFilename();
 
         if (filename == null) {
             requestFilename(); // throws if user cancels
         }
 
         try {
             manuscript.saveDocument(folio);
             last = stack.getCurrent();
         } catch (IllegalStateException ise) {
             dialog = new ErrorMessageDialog(window, "Save failed",
                     "There is a problem in the structure or data of your document: " + ise.getMessage());
             dialog.run();
             dialog.hide();
         } catch (IOException ioe) {
             dialog = new ErrorMessageDialog(window, "Save failed", ioe.getMessage());
             dialog.setSecondaryUseMarkup(true);
             dialog.run();
             dialog.hide();
         }
     }
 
     /**
      * Has the document been modified since the last save? If undo/redo takes
      * you back to the most recent save point, then indeed this will report
      * false.
      */
     public boolean isModified() {
         if (last == stack.getCurrent()) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Ask the user if they want to save the document before closing the app
      * or discarding it by opening a new one.
      */
     void saveIfModified() throws SaveCancelledException {
         final MessageDialog dialog;
         String filename;
         final ResponseType response;
         final Button discard, cancel, ok;
 
         if (!isModified()) {
             return;
         }
 
         // TODO change to document title?
         filename = manuscript.getFilename();
         if (filename == null) {
             filename = "(untitled)";
         }
 
         dialog = new MessageDialog(window, true, MessageType.WARNING, ButtonsType.NONE, "Save Document?");
         dialog.setSecondaryText("The current document" + "\n<tt>" + filename + "</tt>\n"
                 + "has been modified. Do you want to save it first?", true);
 
         discard = new Button();
         discard.setImage(new Image(Stock.DELETE, IconSize.BUTTON));
         discard.setLabel("Discard changes");
         dialog.addButton(discard, ResponseType.CLOSE);
 
         cancel = new Button();
         cancel.setImage(new Image(Stock.CANCEL, IconSize.BUTTON));
         cancel.setLabel("Return to editor");
         dialog.addButton(cancel, ResponseType.CANCEL);
 
         ok = new Button();
         ok.setImage(new Image(Stock.SAVE, IconSize.BUTTON));
         ok.setLabel("Yes, save");
         dialog.addButton(ok, ResponseType.OK);
 
         dialog.showAll();
         dialog.setTitle("Document modified!");
         ok.grabFocus();
 
         response = dialog.run();
         dialog.hide();
 
         if (response == ResponseType.OK) {
             saveDocument();
         } else if (response == ResponseType.CLOSE) {
             return;
         } else {
             throw new SaveCancelledException();
         }
     }
 
     /**
      * Open a new chapter in the editor, replacing the current one.
      */
     void openDocument() {
         final Manuscript attempt;
         final FileChooserDialog dialog;
         final FileFilter filter;
         final ErrorMessageDialog error;
         String filename;
         ResponseType response;
         final Folio folio;
 
         dialog = new FileChooserDialog("Open file...", window, OPEN);
 
         filter = new FileFilter();
         filter.setName("Quill and Parchment documents");
         filter.addPattern("*.parchment");
         dialog.addFilter(filter);
 
         response = dialog.run();
         dialog.hide();
 
         if (response != ResponseType.OK) {
             return;
         }
 
         filename = dialog.getFilename();
 
         try {
             attempt = new Manuscript(filename);
             folio = attempt.loadDocument();
             manuscript = attempt;
         } catch (Exception e) {
             error = new ErrorMessageDialog(
                     window,
                     "Failed to load document!",
                     "Problem encountered when attempting to load:"
                             + "\n<tt>"
                             + filename
                             + "</tt>\n\n"
                             + "Worse, it wasn't something we were expecting. Here's the internal message, which might help a developer fix the problem:\n\n<tt>"
                             + e.getClass().getSimpleName() + "</tt>:\n<tt>" + e.getMessage() + "</tt>");
             error.setSecondaryUseMarkup(true);
 
             error.run();
             error.hide();
             return;
         }
         this.displayDocument(folio);
     }
 
     void saveAs() throws SaveCancelledException {
         requestFilename();
         saveDocument();
     }
 
     /**
      * Cause the document to be printed.
      */
     /*
      * Passing a target filename in from here is either correct, or should be
      * sourced from the DataLayer. The code driving the renderer probably
      * shouldn't be here. Should it be in RenderEngine instead? Improving this
      * will also have to wait on our establishing a proper abstraction for
      * documents as a whole, containing settings relating to publishing. This
      * code copied from what is presently our command line driven
      * RenderToPrintHarness.
      */
     void printDocument() {
         final String parentdir, fullname, basename, targetname;
         MessageDialog dialog;
         final Context cr;
         final Surface surface;
         final PaperSize paper;
         final RenderEngine engine;
 
         try {
             paper = PaperSize.A4;
 
             fullname = manuscript.getFilename();
             if (fullname == null) {
                 dialog = new InfoMessageDialog(window, "Set filename first",
                         "You can't print the document (to PDF) until you've set the filename of this document. "
                                 + "Choose <b>Save As...</b>, then come back and try again!");
                 dialog.setSecondaryUseMarkup(true);
                 dialog.run();
                 dialog.hide();
                 return;
             }
 
             /*
              * Get the basename of the current [save] filename, then
              * instantiate the Cairo Surface we're going to be drawing to with
              * that basename.pdf as the target.
              */
             parentdir = manuscript.getDirectory();
             basename = manuscript.getBasename();
             targetname = parentdir + "/" + basename + ".pdf";
 
             surface = new PdfSurface(targetname, paper.getWidth(Unit.POINTS),
                     paper.getHeight(Unit.POINTS));
             cr = new Context(surface);
 
             // HARDCODE
             engine = new ReportRenderEngine(paper, manuscript, folio);
             engine.render(cr);
 
             surface.finish();
         } catch (IOException ioe) {
             dialog = new ErrorMessageDialog(window, "Print failed", "There's some kind of I/O problem: "
                     + ioe.getMessage());
             dialog.run();
             dialog.hide();
         }
     }
 
     void emergencySave() {
         manuscript.emergencySave(folio);
     }
 
     /**
      * Get the document being edited.
      */
     Folio getDocument() {
         return folio;
     }
 }
