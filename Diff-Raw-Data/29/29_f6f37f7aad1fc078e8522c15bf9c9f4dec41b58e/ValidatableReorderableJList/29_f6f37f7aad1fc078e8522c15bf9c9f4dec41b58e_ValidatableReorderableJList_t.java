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
 
 import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
 import com.dmdirc.util.validators.Validatable;
 import com.dmdirc.util.validators.ValidationResponse;
 
 import java.awt.Color;
 
 import javax.swing.BorderFactory;
 import javax.swing.border.Border;
 
 /**
  * A re-orderable list with a setErorr method to add a visual distinction when there is an error
  * state.
 *
 * @param <T> Type contained in the list
  */
public class ValidatableReorderableJList<T> extends ReorderableJList<T> implements Validatable {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1L;
     /** Normal component border, used for non error state. */
     private final Border passBorder = getBorder();
     /** Error border, used when in an error state. */
     private final Border failBorder = BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(Color.RED, 2), getBorder());
 
     /** {@inheritDoc} */
     @Override
     public void setValidation(final ValidationResponse validation) {
         if (validation.isFailure()) {
             setBorder(failBorder);
             setToolTipText(validation.getFailureReason());
         } else {
             setBorder(passBorder);
             setToolTipText(null);
         }
     }
 
 }
