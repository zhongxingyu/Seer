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
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
 import com.dmdirc.addons.ui_swing.components.IconManager;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.ui.core.errors.CoreErrorsDialogModel;
 import com.dmdirc.ui.core.errors.DisplayableError;
 
 import com.google.common.collect.HashMultiset;
 
 import java.awt.Font;
 import java.awt.Window;
 import java.util.Set;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 /**
  * Shows a breakdown of errors that have occurred.
  *
  * @since 0.6.3m1
  */
 public class ErrorPopup extends StatusbarPopupWindow {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1;
     /** Icon manager. */
     private final IconManager iconManager;
     /** Error manager to retrieve errors from. */
    private final CoreErrorsDialogModel model;
 
     /**
      * Creates a new error popup.
      *
      * @param model        The error model to retrieve errors from
      * @param iconManager  The manager to use to retrieve icons.
      * @param parent       Parent panel
      * @param parentWindow Parent window
      */
     public ErrorPopup(
            final CoreErrorsDialogModel model,
             final IconManager iconManager,
             final JPanel parent,
             final Window parentWindow) {
         super(parent, parentWindow);
 
         this.model = model;
         this.iconManager = iconManager;
     }
 
     @Override
     protected void initContent(final JPanel panel) {
         final Set<DisplayableError> errors = model.getErrors();
         final HashMultiset<ErrorLevel> severities = HashMultiset.create();
         final HashMultiset<ErrorReportStatus> statuses = HashMultiset.create();
         errors.stream().map(DisplayableError::getSeverity).forEach(severities::add);
         errors.stream().map(DisplayableError::getReportStatus).forEach(statuses::add);
 
         panel.add(buildLabel("Severity", SwingConstants.LEFT));
         panel.add(buildLabel("#", SwingConstants.RIGHT), "growx, pushx, wrap");
         severities.elementSet().forEach(s -> {
             panel.add(new JLabel(s.toString(), iconManager.getIcon(s.getIcon()), SwingConstants.LEFT));
             panel.add(new JLabel(String.valueOf(severities.count(s)), SwingConstants.RIGHT),
                     "growx, pushx, wrap");
         });
 
         panel.add(new JSeparator(), "span, growx, pushx, wrap");
 
         panel.add(buildLabel("Report status", SwingConstants.LEFT));
         panel.add(buildLabel("#", SwingConstants.RIGHT), "growx, pushx, wrap");
         statuses.elementSet().forEach(s -> {
             panel.add(new JLabel(s.toString(), SwingConstants.LEFT));
             panel.add(new JLabel(String.valueOf(statuses.count(s)), SwingConstants.RIGHT),
                     "growx, pushx, wrap");
         });
     }
 
     private JLabel buildLabel(final String text, final int constant) {
         final JLabel header = new JLabel(text, constant);
         header.setFont(header.getFont().deriveFont(Font.BOLD));
         return header;
     }
 
 }
