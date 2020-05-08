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
 
 package com.dmdirc.addons.ui_swing.components.frames;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
 import com.dmdirc.addons.ui_swing.textpane.TextPane;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.ui.core.components.WindowComponent;
 import com.dmdirc.util.SimpleInjector;
 import com.dmdirc.util.URLBuilder;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JComponent;
 
 /**
  * Utility class to create frame components.
  *
  * @deprecated Not really compatible with dependency injection. Replace.... somehow.
  */
 @Deprecated
 public class ComponentCreator {
 
     /**
      * Initialises the components specified by the given frame.
      *
      * @param frame      Frame to initialise
      * @param controller UI Controller
      * @param eventBus   Global event bus
      * @param urlBuilder URL builder to inject.
      * @param owner      container
      *
      * @return Set of initialised components
      *
      * @deprecated Not really compatible with dependency injection. Replace.... somehow.
      */
     @Deprecated
     public Set<JComponent> initFrameComponents(
             final Object frame,
             final SwingController controller,
             final DMDircMBassador eventBus,
             final URLBuilder urlBuilder,
             final FrameContainer owner) {
         final SimpleInjector injector = new SimpleInjector();
         final Set<String> names = owner.getComponents();
         final Set<JComponent> components = new HashSet<>();
 
         injector.addParameter(eventBus);
         injector.addParameter(frame);
         injector.addParameter(owner);
         injector.addParameter(controller);
         injector.addParameter(controller.getMainFrame());
         injector.addParameter(urlBuilder);
 
         for (String string : names) {
             Object object;
             try {
                 final Class<?> clazz;
                 if (string.equals(WindowComponent.INPUTFIELD.getIdentifier())) {
                     clazz = SwingInputField.class;
                 } else if (string.equals(WindowComponent.TEXTAREA.getIdentifier())) {
                     clazz = TextPane.class;
                 } else {
                     clazz = Class.forName(string);
                 }
                 object = injector.createInstance(clazz);
             } catch (ClassNotFoundException | IllegalArgumentException ex) {
                 object = null;
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.HIGH, ex,
                         "Unable to create component: " + ex.getMessage(), ""));
             }
             if (object instanceof JComponent) {
                 components.add((JComponent) object);
             }
         }
 
         return components;
     }
 
 }
