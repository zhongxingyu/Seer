 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2008-2010 Operational Dynamics Consulting, Pty Ltd
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
 
 /*
  * Adapted from code in Compendium.
  */
 
 import java.io.FileNotFoundException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.freedesktop.enchant.Dictionary;
 import org.freedesktop.enchant.Enchant;
 import org.gnome.gdk.EventOwnerChange;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.gtk.Button;
 import org.gnome.gtk.ButtonsType;
 import org.gnome.gtk.Clipboard;
 import org.gnome.gtk.Dialog;
 import org.gnome.gtk.ErrorMessageDialog;
 import org.gnome.gtk.Gtk;
 import org.gnome.gtk.IconSize;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.MessageDialog;
 import org.gnome.gtk.MessageType;
 import org.gnome.gtk.ResponseType;
 import org.gnome.gtk.Settings;
 import org.gnome.gtk.Stock;
 import org.gnome.pango.FontDescription;
 
 import quill.client.ApplicationException;
 import quill.client.RecoveryFileExistsException;
 import quill.client.SafelyTerminateException;
 import quill.textbase.Extract;
 import quill.textbase.Folio;
 import quill.textbase.Span;
 
 import static quill.textbase.Span.createSpan;
 
 public class UserInterface
 {
     private Set<PrimaryWindow> primaries;
 
     Dictionary dict;
 
     public UserInterface() {
         loadImages();
         loadFonts();
         setupApplication();
         setupWindows();
         hookupExternalClipboard();
         loadDictionary();
     }
 
     private void setupWindows() {
         primaries = new HashSet<PrimaryWindow>(3);
     }
 
     private void loadImages() {
         try {
             images.quill = new Pixbuf("share/pixmaps/quill-and-parchment.png"); // 48x48
             images.graphic = new Pixbuf("share/pixmaps/graphic-16x16.png", -1, 10, true);
         } catch (FileNotFoundException fnfe) {
             System.err.println("Icon file not found: " + fnfe.getMessage());
         }
     }
 
     private void loadFonts() {
         fonts.serif = new FontDescription("Deja Vu Serif, 11");
         fonts.sans = new FontDescription("Deja Vu Sans Condensed, 11");
         fonts.mono = new FontDescription("Deja Vu Sans Mono, 11");
         fonts.heading = new FontDescription("Liberation Sans, Bold 14");
     }
 
     private void setupApplication() {
         final Settings settings;
 
         Gtk.setDefaultIcon(images.quill);
 
         settings = Gtk.getSettings();
         settings.setShowInputMethodMenu(false);
         settings.setShowUnicodeMenu(false);
     }
 
     /**
      * Terminate the user interface (and, by returning to main(), precipiate
      * the application to terminate).
      */
     public void shutdown() {
         clipboard.store();
 
         Gtk.mainQuit();
     }
 
     private Clipboard clipboard;
 
     /**
      * Do we [think] we're the owner of the clipboard?
      */
     private boolean owner;
 
     /**
      * When text is cut or copied out, it will be cached here.
      */
     private Extract stash;
 
     private void hookupExternalClipboard() {
         stash = null;
         clipboard = Clipboard.getDefault();
 
         /*
          * This gets hit whenever the clipboard is written to. In our case, we
          * toggle the owner state variable to true when we know we're about to
          * programatically set the clipboard; this handler gets called once,
          * and we can ignore it. Any other receipt of this event and we have
          * to get the text from the system.
          */
 
         clipboard.connect(new Clipboard.OwnerChange() {
             public void onOwnerChange(Clipboard source, EventOwnerChange event) {
                 final Span span;
                 final String str;
 
                 if (owner) {
                     owner = false;
                 } else {
                     str = clipboard.getText();
                     if (str == null) {
                         /*
                          * If the data in the system clipboard was put there
                          * in a a form other than plain text we get null back.
                          */
                         return;
                     }
                     span = createSpan(str, null);
                     stash = Extract.create(span);
                 }
             }
         });
     }
 
     /**
      * Get the Span(s) that are currently in the clipboard. This will be rich
      * Spans with Markup if the cut/copy operation was done within our
      * application, otherwise it will be a plain text.
      */
     Extract getClipboard() {
         return stash;
     }
 
     /**
      * Put the extracted text into the system clipboard.
      */
     void setClipboard(Extract extract) {
         owner = true;
         stash = extract;
         clipboard.setText(extract.getText());
     }
 
     /**
      * Initiate display of the given document.
      */
     /*
      * For now this starts at the first component, but presumably we want to
      * record somewhere what the last edited point was and start there
      * instead. We also need to instruct the PrimaryWindow about what
      * navigation options it can offer.
      */
     public void displayDocument(Folio folio) {
         final PrimaryWindow primary;
 
         primary = new PrimaryWindow();
         primary.displayDocument(folio);
 
         primaries.add(primary);
     }
 
     // is this necessary?
     PrimaryWindow[] getPrimaries() {
         final PrimaryWindow[] result;
         final int len;
 
         len = primaries.size();
         result = new PrimaryWindow[len];
 
         return primaries.toArray(result);
     }
 
     /**
      * Change the UI to report a (fatal) error condition.
      */
     /*
      * TODO in development, frankly, I just need the stack trace. So we're
      * actually skipping calling this crash dialog for now. Maybe we should
      * attempt an autosave here, though?
      */
     public void error(Exception e) {
         Dialog d;
 
         for (PrimaryWindow primary : primaries) {
             primary.hide();
         }
 
         e.printStackTrace();
 
         d = new ErrorMessageDialog(null, "Problem", e.getMessage());
         d.run();
         d.hide();
 
         System.exit(1);
     }
 
     public void focusEditor() {
         /*
          * Grabbing focus was the previous behaviour, and what we want. Is
          * focus a window by window property, or is it app wide. If the
          * former, then this is correct.
          */
         for (PrimaryWindow primary : primaries) {
             primary.grabFocus();
         }
     }
 
     private void loadDictionary() {
         Enchant.init();
         dict = Enchant.requestDictionary("en_CA");
     }
 
     public void warning(ApplicationException ae) throws SafelyTerminateException {
         final MessageDialog dialog;
         final Button cancel, ok;
         final ResponseType response;
         final String path;
 
         if (ae instanceof RecoveryFileExistsException) {
             dialog = new MessageDialog(null, true, MessageType.WARNING, ButtonsType.NONE,
                     "Crash recovery file exists");
             cancel = new Button();
             cancel.setImage(new Image(Stock.CANCEL, IconSize.BUTTON));
             cancel.setLabel("Cancel loading and Quit");
             dialog.addButton(cancel, ResponseType.CANCEL);
 
             ok = new Button();
             ok.setImage(new Image(Stock.OK, IconSize.BUTTON));
             ok.setLabel("Load original anyway");
             dialog.addButton(ok, ResponseType.OK);
 
             path = ae.getMessage().replace(System.getenv("HOME"), "~");
 
             dialog.setSecondaryText("A recovery file exists:" + "\n\n<tt>" + path + "</tt>\n\n"
                     + "It <i>may</i> contain what you were working on before Quill crashed. "
                     + "You should quit and review it against your actual document. Once you're sure "
                     + "the rescue file doesn't contain anything you need, you can delete it." + "\n\n"
                     + "Be warned that if the program crashes again, we will <i>not</i> overwrite "
                     + "the existing recovery file.", true);
 
             dialog.showAll();
             response = dialog.run();
             dialog.hide();
 
             if (response != ResponseType.OK) {
                 /*
                  * FUTURE This will need to change if we support editing
                  * multiple documents in one Quill process.
                  */
                 throw new SafelyTerminateException();
             }
         }
     }
 
     public void emergencySave() {
         for (PrimaryWindow primary : primaries) {
             primary.emergencySave();
         }
     }
 }
 
 class images
 {
     static Pixbuf quill;
 
     static Pixbuf graphic;
 }
 
 class fonts
 {
     static FontDescription sans;
 
     static FontDescription serif;
 
     static FontDescription mono;
 
     static FontDescription heading;
 }
