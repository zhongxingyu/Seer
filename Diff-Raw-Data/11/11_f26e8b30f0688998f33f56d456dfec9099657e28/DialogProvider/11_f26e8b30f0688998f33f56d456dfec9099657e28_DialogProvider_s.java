 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.injection;
 
import static com.dmdirc.addons.ui_swing.SwingPreconditions.checkOnEDT;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.inject.Provider;
 
 /**
  * Simple provider for {@link StandardDialog} based windows.
  *
  * <p>This provider will cache instances that are created until the windows are closed. Once a
  * window has been closed, the next call to {@link #get()} or {@link #displayOrRequestFocus()}
  * will result in a new instance being created.
  *
  * @param <T> The type of dialog that will be managed.
  */
 public class DialogProvider<T extends StandardDialog> {
 
     /** Provider used to generate new instances. */
     private final Provider<T> provider;
     /** The existing instance being displayed, if any. */
     private T instance;
 
     /**
      * Creates a new instance of {@link DialogProvider}.
      *
      * @param provider The provider to use to generate new instances of the dialog, when required.
      */
     public DialogProvider(final Provider<T> provider) {
         this.provider = provider;
     }
 
     /**
      * Gets an instance of the dialog provided by this class.
      *
      * <p>If there is an existing instance that hasn't been closed, it will be returned. Otherwise
      * a new instance will be created and returned. New instances will not be automatically be
      * displayed to the user - use {@link #displayOrRequestFocus()} to do so.
      *
      * <p>This method <em>must</em> be called on the Event Despatch Thread.
      *
      * @return An instance of the dialog.
      */
     public T get() {
         checkOnEDT();
         if (instance == null) {
             instance = provider.get();
             instance.addWindowListener(new Listener());
         }
         return instance;
     }
 
     /**
      * Ensures the dialog is visible to the user.
      *
      * <p>If no dialog currently exists, a new one will be created and displayed to the user. If
      * a dialog existed prior to this method being invoked, it will request focus to bring it to
      * the user's attention.
      *
      * <p>This method <em>must</em> be called on the Event Despatch Thread.
      */
     public void displayOrRequestFocus() {
         checkOnEDT();
         get().displayOrRequestFocus();
     }
 
     /**
      * Listens to window closing events to remove the cached instance of the dialog.
      */
     private class Listener extends WindowAdapter {
 
         @Override
        public void windowClosing(final WindowEvent e) {
            super.windowClosing(e);
             instance = null;
         }
 
     }
 
 }
