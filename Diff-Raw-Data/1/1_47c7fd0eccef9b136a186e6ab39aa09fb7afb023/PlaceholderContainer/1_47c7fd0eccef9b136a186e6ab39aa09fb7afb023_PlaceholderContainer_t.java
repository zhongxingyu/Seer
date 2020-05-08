 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.dcc;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.ui.messages.BackBufferFactory;
 
 import java.awt.Dialog.ModalityType;
 import java.awt.Window;
 import java.util.Collections;
 import java.util.Optional;
 
 /**
  * Creates a placeholder DCC Frame.
  */
 public class PlaceholderContainer extends FrameContainer {
 
     /** The plugin which owns this placeholder. */
     private final DCCManager plugin;
     /** Window that will own new dialogs. */
     private final Window parentWindow;
 
     /**
      * Creates a placeholder DCC frame.
      *
      * @param plugin       The plugin which owns this placeholder
      * @param config       Config manager
      * @param parentWindow Window that will parent new dialogs.
      * @param eventBus     The bus to dispatch events on.
      */
     public PlaceholderContainer(
             final DCCManager plugin,
             final AggregateConfigProvider config,
             final BackBufferFactory backBufferFactory,
             final Window parentWindow,
             final DMDircMBassador eventBus) {
         super(null, "dcc", "DCCs", "DCCs", config, backBufferFactory, eventBus,
                 Collections.singletonList("com.dmdirc.addons.dcc.ui.PlaceholderPanel"));
         this.plugin = plugin;
         this.parentWindow = parentWindow;
         initBackBuffer();
     }
 
     @Override
     public void close() {
         int dccs = 0;
         for (FrameContainer window : getChildren()) {
             if (window instanceof TransferContainer
                     && ((TransferContainer) window).getDCC().isActive()
                     || window instanceof ChatContainer
                     && ((ChatContainer) window).getDCC().isActive()) {
                 dccs++;
             }
         }
 
         if (dccs > 0) {
             new StandardQuestionDialog(parentWindow, ModalityType.MODELESS,
                     "Close confirmation",
                     "Closing this window will cause all existing DCCs "
                     + "to terminate, are you sure you want to do this?",
                     () -> { close(); plugin.removeContainer();}).display();
         } else {
             super.close();
             plugin.removeContainer();
         }
     }
 
     @Override
     public Optional<Connection> getConnection() {
         return Optional.empty();
     }
 
     @Override
     public void removeChild(final FrameContainer child) {
         super.removeChild(child);
 
         if (getChildren().isEmpty()) {
             close();
         }
     }
 
 }
