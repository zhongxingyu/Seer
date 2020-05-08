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
 
 package com.dmdirc.addons.ui_swing.components.validating;
 
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.util.validators.Validator;
 
 import java.awt.Component;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JLayer;
 import javax.swing.JList;
 import javax.swing.text.JTextComponent;
 
 /**
  * Factory to add validation support to UI components.
  */
 public class ValidationFactory {
 
     private ValidationFactory() {
         //Use static methods
     }
 
     /**
      * Retrieves a validating component panel for the given component.
      *
      * @param validation  Component to validate
      * @param validator   Validator to validate against
      * @param iconManager Icon manager to get icons from
      *
      * @return Validating component panel for the component
      */
     public static JComponent getValidatorPanel(final JTextComponent validation,
             final Validator<String> validator, final IconManager iconManager) {
         return getValidatorPanel(validation, validation, validator, iconManager);
     }
 
     /**
      * Retrieves a validating component panel for the given component.
      *
      *
      * @param display     Component to display instead in the panel
      * @param validation  Component to validate
      * @param validator   Validator to validate against
      * @param iconManager Icon manager to get icons from
      *
      * @param <T>         Type of component to wrap in the layer UI
      *
      * @return Validating component panel for the component
      */
     public static <T extends Component> JComponent getValidatorPanel(final T display,
             final JTextComponent validation, final Validator<String> validator,
             final IconManager iconManager) {
         final ComponentValidator<String, JTextComponent> componentValidator
                 = new JTextComponentComponentValidator(validation, validator);
         return getPanel(display, componentValidator, iconManager);
     }
 
     /**
      * Retrieves a validating component panel for the given component.
      *
      * @param validation  Component to validate
      * @param validator   Validator to validate against
      * @param iconManager Icon manager to get icons from
      *
     * @param <T>         Type of component to wrap in the layer UI
     *
      * @return Validating component panel for the component
      */
     public static <T> JComponent getValidatorPanel(
             final JList<T> validation, final Validator<List<T>> validator,
             final IconManager iconManager) {
         return getValidatorPanel(validation, validation, validator, iconManager);
     }
 
     /**
      * Retrieves a validating component panel for the given component.
      *
      * @param display     Component to display instead in the panel
      * @param validation  Component to validate
      * @param validator   Validator to validate against
      * @param iconManager Icon manager to get icons from
      *
      * @param <T>         Type of component to wrap in the layer UI
     * @param <V>         The type of item in the model that will be validated.
      *
      * @return Validating component panel for the component
      */
     public static <T extends Component, V> JComponent getValidatorPanel(final T display,
             final JList<V> validation, final Validator<List<V>> validator,
             final IconManager iconManager) {
         final ComponentValidator<List<V>, JList<V>> componentValidator
                = new JListComponentValidator<>(validation, validator);
         return getPanel(display, componentValidator, iconManager);
     }
 
    private static <T extends Component> JComponent getPanel(
             final T display,
            final ComponentValidator<?, ? extends JComponent> componentValidator,
             final IconManager iconManager) {
        final ValidationLayerUI<T> validationLayer = new ValidationLayerUI<>();
         final ValidationComponentPanel panel = new ValidationComponentPanel(iconManager,
                new JLayer<>(display, validationLayer));
         componentValidator.addHooks();
         componentValidator.addComponentValidatorListener(validationLayer);
         componentValidator.addComponentValidatorListener(panel);
         return panel;
     }
 
 }
