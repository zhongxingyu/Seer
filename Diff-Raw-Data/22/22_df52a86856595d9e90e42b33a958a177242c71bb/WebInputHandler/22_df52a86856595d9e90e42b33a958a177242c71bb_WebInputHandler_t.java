 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_web.uicomponents;
 
 import com.dmdirc.commandparser.parsers.CommandParser;
 import com.dmdirc.ui.input.InputHandler;
 import com.dmdirc.ui.input.TabCompleter;
 import com.dmdirc.ui.interfaces.InputField;
 
 /**
  *
  * @author chris
  */
 public class WebInputHandler extends InputHandler {
 
     public WebInputHandler(InputField thisTarget,
                            CommandParser thisCommandParser,
                            WebInputWindow thisParentWindow) {
         super(thisTarget, thisCommandParser, thisParentWindow);
     }
 
     public InputField getTarget() {
         return target;
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addUpHandler() {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addDownHandler() {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addTabHandler() {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addKeyHandler() {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addEnterHandler() {
         // Do nothing
     }
 
     public TabCompleter getTabCompleter() {
         return tabCompleter;
     }
 
     /** {@inheritDoc} */
     @Override
    public void doTabCompletion(boolean shiftPressed) {
        super.doTabCompletion(shiftPressed);
     }
 
     /** {@inheritDoc} */
     @Override
     public void doBufferDown() {
         super.doBufferDown();
     }
 
     /** {@inheritDoc} */
     @Override
     public void doBufferUp() {
         super.doBufferUp();
     }
 
     /** {@inheritDoc} */
     @Override
     public void handleKeyPressed(final String line, final int keyCode,
             final boolean shiftPressed, final boolean ctrlPressed) {
         super.handleKeyPressed(line, keyCode, shiftPressed, ctrlPressed);
     }
 
 }
