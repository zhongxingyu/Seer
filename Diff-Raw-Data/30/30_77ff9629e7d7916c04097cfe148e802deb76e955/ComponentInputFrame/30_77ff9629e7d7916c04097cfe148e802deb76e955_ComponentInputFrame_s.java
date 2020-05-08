 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.components.frames;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
 import com.dmdirc.commandparser.PopupType;
 
 import javax.swing.JComponent;
 import javax.swing.JPopupMenu;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * A component frame that includes an input field (for use with writable
  * containers).
  */
 public class ComponentInputFrame extends InputTextFrame {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Parent frame container. */
     private final FrameContainer owner;
     /** Parent controller. */
     private final SwingController controller;
 
     /**
      * Creates a new instance of CustomInputFrame.
      *
      * @param owner The frame container that owns this frame
      * @param controller Swing controller
      */
     public ComponentInputFrame(final SwingController controller,
             final WritableFrameContainer owner) {
         super(controller, owner);

        setInputHandler(new SwingInputHandler(getInputField(),
                owner.getCommandParser(), owner));

         this.controller = controller;
         this.owner = owner;
         initComponents();
     }
 
     /**
      * Initialises components in this frame.
      */
     private void initComponents() {
         setLayout(new MigLayout("fill"));
         for (JComponent comp : new ComponentCreator().initFrameComponents(this,
                 controller, owner)) {
             add(comp, "wrap, grow");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public PopupType getNicknamePopupType() {
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public PopupType getChannelPopupType() {
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public PopupType getHyperlinkPopupType() {
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public PopupType getNormalPopupType() {
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public void addCustomPopupItems(final JPopupMenu popupMenu) {
         //Add no custom popup items
     }
 }
