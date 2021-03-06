 /*
  * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
  * written by Rasto Levrinc.
  *
  * Copyright (C) 2009-2010, LINBIT HA-Solutions GmbH.
  * Copyright (C) 2009-2010, Rasto Levrinc
  *
  * DRBD Management Console is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published
  * by the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * DRBD Management Console is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with drbd; see the file COPYING.  If not, write to
  * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package drbd.gui.resources;
 
 import drbd.gui.Browser;
 import drbd.gui.GuiComboBox;
 import drbd.utilities.ButtonCallback;
 import drbd.utilities.MyButton;
 import drbd.utilities.Tools;
 import drbd.utilities.Unit;
 import drbd.data.CRMXML;
 import drbd.data.ConfigData;
 import drbd.data.AccessMode;
 import drbd.gui.SpringUtilities;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 import javax.swing.SpringLayout;
 import javax.swing.BoxLayout;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import org.apache.commons.collections.map.MultiKeyMap;
 
 /**
  * This class provides textfields, combo boxes etc. for editable info
  * objects.
  */
 public abstract class EditableInfo extends Info {
     /** Hash from parameter to boolean value if the last entered value was
      * correct. */
     private final Map<String, Boolean> paramCorrectValueMap =
                                                 new HashMap<String, Boolean>();
 
     /** Returns section in which is this parameter. */
     protected abstract String getSection(String param);
     /** Returns whether this parameter is required. */
     protected abstract boolean isRequired(String param);
     /** Returns whether this parameter is advanced. */
     protected abstract boolean isAdvanced(String param);
     /** Returns whether this parameter should be enabled. */
     protected abstract boolean isEnabled(String param);
     /** Returns access type of this parameter. */
     protected abstract ConfigData.AccessType getAccessType(String param);
     /** Returns whether this parameter is enabled in advanced mode. */
     protected abstract boolean isEnabledOnlyInAdvancedMode(String param);
     /** Returns whether this parameter is of label type. */
     protected abstract boolean isLabel(String param);
     /** Returns whether this parameter is of the integer type. */
     protected abstract boolean isInteger(String param);
     /** Returns whether this parameter is of the time type. */
     protected abstract boolean isTimeType(String param);
     /** Returns whether this parameter has a unit prefix. */
     protected boolean hasUnitPrefix(final String param) {
         return false;
     }
     /** Returns whether this parameter is of the check box type, like
      * boolean. */
     protected abstract boolean isCheckBox(String param);
     /** Returns type of the field. */
     protected GuiComboBox.Type getFieldType(final String param) {
         return null;
     }
     /** Returns the name of the type. */
     protected abstract String getParamType(final String param);
     /** Returns the regexp of the parameter. */
     protected String getParamRegexp(final String param) {
         // TODO: this should be only for Pacemaker
         if (isInteger(param)) {
             return "^((-?\\d*|(-|\\+)?" + CRMXML.INFINITY_STRING
                    + "|" + CRMXML.DISABLED_STRING
                    + "))|@NOTHING_SELECTED@$";
         }
         return null;
     }
     /** Returns the possible choices for pull down menus if applicable. */
     protected abstract Object[] getParamPossibleChoices(String param);
     /** Returns array of all parameters. */
     public abstract String[] getParametersFromXML(); // TODO: no XML
     /** Old apply button, is used for wizards. */
     private MyButton oldApplyButton = null;
     /** Apply button. */ // TODO: private
     private MyButton applyButton;
     /** Is counted down, first time the info panel is initialized. */
     private final CountDownLatch infoPanelLatch = new CountDownLatch(1);
     /** List of advanced panels. */
     private final List<JPanel> advancedPanelList = new ArrayList<JPanel>();
     /** List of messages if advanced panels are hidden. */
     private final List<JPanel> advancedOnlySectionList =
                                                       new ArrayList<JPanel>();
     /** More options panel. */
     private final JPanel moreOptionsPanel = new JPanel();
 
     /** How much of the info is used. */
     public int getUsed() {
         return -1;
     }
 
     /**
      * Prepares a new <code>EditableInfo</code> object.
      *
      * @param name
      *      name that will be shown to the user.
      */
     public EditableInfo(final String name, final Browser browser) {
         super(name, browser);
     }
 
     /** Inits apply button. */
     public final void initApplyButton(final ButtonCallback buttonCallback) {
         if (oldApplyButton == null) {
             applyButton = new MyButton(
                     Tools.getString("Browser.ApplyResource"),
                     Browser.APPLY_ICON);
             oldApplyButton = applyButton;
         } else {
             applyButton = oldApplyButton;
         }
         applyButton.setEnabled(false);
         if (buttonCallback != null) {
             addMouseOverListener(applyButton, buttonCallback);
         }
     }
 
     /** Creates apply button and adds it to the panel. */
     protected final void addApplyButton(final JPanel panel) {
         panel.add(applyButton, BorderLayout.WEST);
         Tools.getGUIData().getMainFrameRootPane().setDefaultButton(applyButton);
     }
 
     /** Adds jlabel field with tooltip. */
     public final void addLabelField(final JPanel panel,
                                     final String left,
                                     final String right,
                                     final int leftWidth,
                                     final int rightWidth,
                                     final int height) {
         final JLabel leftLabel = new JLabel(left);
         leftLabel.setToolTipText(left);
         final JLabel rightLabel = new JLabel(right);
         rightLabel.setToolTipText(right);
         addField(panel, leftLabel, rightLabel, leftWidth, rightWidth, height);
     }
 
     /**
      * Adds field with left and right component to the panel. Use panel
      * with spring layout for this.
      */
     public final void addField(final JPanel panel,
                                final JComponent left,
                                final JComponent right,
                                final int leftWidth,
                                final int rightWidth,
                                int height) {
         /* right component with fixed width. */
         if (height == 0) {
             height = Tools.getDefaultInt("Browser.FieldHeight");
         }
         Tools.setSize(left,
                       leftWidth,
                       height);
         panel.add(left);
         Tools.setSize(right,
                       rightWidth,
                       height);
         panel.add(right);
         right.setBackground(panel.getBackground());
     }
 
     /**
      * Adds parameters to the panel in a wizard.
      * Returns number of rows.
      */
     public final void addWizardParams(
                               final JPanel optionsPanel,
                               final String[] params,
                               final MyButton wizardApplyButton,
                               final int leftWidth,
                               final int rightWidth,
                               final Map<String, GuiComboBox> sameAsFields) {
         addParams(optionsPanel,
                   "wizard",
                   params,
                   wizardApplyButton,
                   leftWidth,
                   rightWidth,
                   sameAsFields);
     }
 
     /**
      * This class holds a part of the panel within the same section, access
      * type and advanced mode setting.
      */
     private class PanelPart {
         /** Section of this panel part. */
         private final String section;
         /** Access type of this panel part. */
         private final ConfigData.AccessType accessType;
         /** Whether it is an advanced panel part. */
         private final boolean advanced;
 
         /** Creates new panel part object. */
         public PanelPart(final String section,
                          final ConfigData.AccessType accessType,
                          final boolean advanced) {
             this.section = section;
             this.accessType = accessType;
             this.advanced = advanced;
         }
 
         /** Returns a section to which this panel part belongs. */
         public final String getSection() {
             return section;
         }
 
         /** Returns access type of this panel part. */
         public final ConfigData.AccessType getAccessType() {
             return accessType;
         }
 
         /** Whether this panel part has advanced options. */
         public final boolean isAdvanced() {
             return advanced;
         }
     }
 
     /** Adds parameters to the panel. */
     public final void addParams(final JPanel optionsPanel,
                                 final String[] params,
                                 final int leftWidth,
                                 final int rightWidth,
                                 final Map<String, GuiComboBox> sameAsFields) {
         addParams(optionsPanel,
                   null,
                   params,
                   applyButton,
                   leftWidth,
                   rightWidth,
                   sameAsFields);
     }
 
     /** Adds parameters to the panel. */
     private void addParams(final JPanel optionsPanel,
                            final String prefix,
                            final String[] params,
                            final MyButton thisApplyButton,
                            final int leftWidth,
                            final int rightWidth,
                            final Map<String, GuiComboBox> sameAsFields) {
         if (params == null) {
             return;
         }
         final MultiKeyMap panelPartsMap = new MultiKeyMap();
         final List<PanelPart> panelPartsList = new ArrayList<PanelPart>();
         final MultiKeyMap panelPartRowsMap = new MultiKeyMap();
 
         for (final String param : params) {
             final GuiComboBox paramCb = getParamComboBox(param,
                                                          prefix,
                                                          rightWidth);
             /* sub panel */
             final String section = getSection(param);
             JPanel panel;
             final boolean advanced = isAdvanced(param);
             final ConfigData.AccessType accessType = getAccessType(param);
             if (panelPartsMap.containsKey(section, accessType, advanced)) {
                 panel = (JPanel) panelPartsMap.get(section,
                                                    accessType,
                                                    advanced);
                 panelPartRowsMap.put(section, accessType, advanced,
                                  (Integer) panelPartRowsMap.get(section,
                                                                 accessType,
                                                                 advanced) + 1);
             } else {
                 panel = new JPanel(new SpringLayout());
                 panel.setBackground(Browser.PANEL_BACKGROUND);
                 if (advanced) {
                     advancedPanelList.add(panel);
                     final JPanel p = panel;
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             p.setVisible(
                                      Tools.getConfigData().isAdvancedMode());
                         }
                     });
                 }
                 panelPartsMap.put(section, accessType, advanced, panel);
                 panelPartsList.add(new PanelPart(section,
                                                  accessType,
                                                  advanced));
                 panelPartRowsMap.put(section, accessType, advanced, 1);
             }
 
             /* label */
             final JLabel label = new JLabel(getParamShortDesc(param));
             final String longDesc = getParamLongDesc(param);
             paramCb.setLabel(label, longDesc);
 
             /* tool tip */
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     paramCb.setToolTipText(getToolTipText(param));
                     label.setToolTipText(longDesc);
                 }
             });
             int height = 0;
             if (paramCb.getType() == GuiComboBox.Type.LABELFIELD) {
                 height = Tools.getDefaultInt("Browser.LabelFieldHeight");
             }
             addField(panel, label, paramCb, leftWidth, rightWidth, height);
         }
         for (final String param : params) {
             final GuiComboBox paramCb = paramComboBoxGet(param, prefix);
             GuiComboBox rpcb = null;
             if ("wizard".equals(prefix)) {
                 rpcb = paramComboBoxGet(param, null);
                 int height = 0;
                 if (rpcb.getType() == GuiComboBox.Type.LABELFIELD) {
                     height = Tools.getDefaultInt("Browser.LabelFieldHeight");
                 }
                 final GuiComboBox rpcb0 = rpcb;
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         final Object value = paramCb.getStringValue();
                         rpcb0.setValueAndWait(value);
                     }
                 });
             }
             final GuiComboBox realParamCb = rpcb;
             paramCb.addListeners(new ItemListener() {
                 public void itemStateChanged(final ItemEvent e) {
                     if (paramCb.isCheckBox()
                         || e.getStateChange() == ItemEvent.SELECTED) {
                         checkParameterFields(paramCb,
                                              realParamCb,
                                              param,
                                              params,
                                              thisApplyButton);
                     }
                 }
             },
 
             new DocumentListener() {
                 public void insertUpdate(final DocumentEvent e) {
                     checkParameterFields(paramCb,
                                          realParamCb,
                                          param,
                                          params,
                                          thisApplyButton);
                 }
 
                 public void removeUpdate(final DocumentEvent e) {
                     checkParameterFields(paramCb,
                                          realParamCb,
                                          param,
                                          params,
                                          thisApplyButton);
                 }
 
                 public void changedUpdate(final DocumentEvent e) {
                     checkParameterFields(paramCb,
                                          realParamCb,
                                          param,
                                          params,
                                          thisApplyButton);
                 }
             });
         }
 
         /* add sub panels to the option panel */
         final Map<String, JPanel> sectionMap = new HashMap<String, JPanel>();
         final Set<JPanel> notAdvancedSections = new HashSet<JPanel>();
         final Set<JPanel> advancedSections = new HashSet<JPanel>();
         for (final PanelPart panelPart : panelPartsList) {
             final String section = panelPart.getSection();
             final ConfigData.AccessType accessType = panelPart.getAccessType();
             final boolean advanced = panelPart.isAdvanced();
 
             final JPanel panel = (JPanel) panelPartsMap.get(section,
                                                             accessType,
                                                             advanced);
             final int rows = (Integer) panelPartRowsMap.get(section,
                                                             accessType,
                                                             advanced);
             final int columns = 2;
             SpringUtilities.makeCompactGrid(panel, rows, columns,
                                             1, 1,  // initX, initY
                                             1, 1); // xPad, yPad
             JPanel sectionPanel;
             if (sectionMap.containsKey(section)) {
                 sectionPanel = sectionMap.get(section);
             } else {
                 sectionPanel = getParamPanel(section);
                 sectionMap.put(section, sectionPanel);
                 optionsPanel.add(sectionPanel);
                 if (sameAsFields != null) {
                     final GuiComboBox sameAsCombo = sameAsFields.get(section);
                     if (sameAsCombo != null) {
                         final JPanel saPanel = new JPanel(new SpringLayout());
                         saPanel.setBackground(Browser.STATUS_BACKGROUND);
                         final JLabel label = new JLabel("Same As");
                         sameAsCombo.setLabel(label, "");
                         addField(saPanel,
                                  label,
                                  sameAsCombo,
                                  leftWidth,
                                  rightWidth,
                                  0);
                         SpringUtilities.makeCompactGrid(saPanel, 1, 2,
                                                         1, 1,  // initX, initY
                                                         1, 1); // xPad, yPad
                         sectionPanel.add(saPanel);
                     }
                 }
             }
             sectionPanel.add(panel);
             if (!advanced) {
                 notAdvancedSections.add(sectionPanel);
             } else {
                 advancedSections.add(sectionPanel);
             }
         }
         boolean advanced = false;
         for (final JPanel sectionPanel : sectionMap.values()) {
             if (advancedSections.contains(sectionPanel)) {
                 advanced = true;
             }
             if (!notAdvancedSections.contains(sectionPanel)) {
                 advancedOnlySectionList.add(sectionPanel);
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         sectionPanel.setVisible(
                                       Tools.getConfigData().isAdvancedMode());
                     }
                 });
             }
         }
         final boolean a = advanced;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 moreOptionsPanel.setVisible(
                                 a && !Tools.getConfigData().isAdvancedMode());
             }
         });
     }
 
     /** Returns a more panel with "more options are available" message. */
     protected final JPanel getMoreOptionsPanel(final int width) {
         final JLabel l = new JLabel(
                               Tools.getString("EditableInfo.MoreOptions"));
         final Font font = l.getFont();
         final String name = font.getFontName();
         final int style = Font.ITALIC;
         final int size = font.getSize();
         l.setFont(new Font(name, style, size - 3));
 
         moreOptionsPanel.setBackground(Browser.PANEL_BACKGROUND);
         moreOptionsPanel.add(l);
         final Dimension d = moreOptionsPanel.getPreferredSize();
         d.width = width;
         moreOptionsPanel.setMaximumSize(d);
         return moreOptionsPanel;
     }
 
     /** Checks ands sets paramter fields. */
     public void checkParameterFields(final GuiComboBox paramCb,
                                      final GuiComboBox realParamCb,
                                      final String param,
                                      final String[] params,
                                      final MyButton thisApplyButton) {
         final Thread thread = new Thread(new Runnable() {
             public void run() {
                 //SwingUtilities.invokeLater(new Runnable() {
                 //    public void run() {
                 //        paramCb.setEditable();
                 //    }
                 //});
                 boolean c;
                 if (realParamCb != null) {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             final Object value = paramCb.getStringValue();
                             realParamCb.setValueAndWait(value);
                         }
                     });
                     Tools.waitForSwing();
                     c = checkResourceFieldsCorrect(param,
                                                    params);
                 } else {
                     c = checkResourceFields(param, params);
                 }
                 final boolean check = c;
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         thisApplyButton.setEnabled(check);
                         paramCb.setToolTipText(
                                 getToolTipText(param));
                         if (realParamCb != null) {
                             realParamCb.setToolTipText(
                                     getToolTipText(param));
                         }
                     }
                 });
             }
         });
         thread.start();
     }
 
     /** Get stored value in the combo box. */
     public final String getComboBoxValue(final String param) {
         final GuiComboBox cb = paramComboBoxGet(param, null);
         if (cb == null) {
             return null;
         }
         final Object o = cb.getValue();
         String value;
         if (Tools.isStringClass(o)) {
             value = cb.getStringValue();
         } else if (o instanceof Object[]) {
             value = ((Object[]) o)[0].toString();
             if (((Object[]) o)[1] instanceof Unit) {
                 value += ((Unit) ((Object[]) o)[1]).getShortName();
             }
         } else {
             value = ((Info) o).getStringValue();
         }
         return value;
     }
 
     /** Stores values in the combo boxes in the component c. */
     protected final void storeComboBoxValues(final String[] params) {
         for (String param : params) {
             final String value = getComboBoxValue(param);
             getResource().setValue(param, value);
             final GuiComboBox cb = paramComboBoxGet(param, null);
             if (cb != null) {
                cb.setToolTipText(getToolTipText(param));
             }
         }
     }
 
     /** Returns combo box for one parameter. */
     protected GuiComboBox getParamComboBox(final String param,
                                            final String prefix,
                                            final int width) {
         getResource().setPossibleChoices(param, getParamPossibleChoices(param));
         /* set default value */
         final String value = getParamSaved(param);
         String initValue = null;
         if (value == null || "".equals(value)) {
             if (getResource().isNew()) {
                 initValue = getResource().getPreferredValue(param);
                 if (initValue == null) {
                     initValue = getParamPreferred(param);
                 }
             }
             if (initValue == null) {
                 initValue = getParamDefault(param);
                 getResource().setValue(param, initValue);
             }
         } else {
             initValue = value;
         }
         final String regexp = getParamRegexp(param);
         Map<String, String> abbreviations = new HashMap<String, String>();
         if (isInteger(param)) {
             abbreviations = new HashMap<String, String>();
             abbreviations.put("i", CRMXML.INFINITY_STRING);
             abbreviations.put("I", CRMXML.INFINITY_STRING);
             abbreviations.put("+", CRMXML.PLUS_INFINITY_STRING);
             abbreviations.put("d", CRMXML.DISABLED_STRING);
             abbreviations.put("D", CRMXML.DISABLED_STRING);
         }
         GuiComboBox.Type type = getFieldType(param);
         Unit[] units = null;
         if (type == GuiComboBox.Type.TEXTFIELDWITHUNIT) {
             units = getUnits();
         }
         if (isCheckBox(param)) {
             type = GuiComboBox.Type.CHECKBOX;
         } else if (isTimeType(param)) {
             type = GuiComboBox.Type.TEXTFIELDWITHUNIT;
             units = getTimeUnits();
         } else if (isLabel(param)) {
             type = GuiComboBox.Type.LABELFIELD;
         }
         final GuiComboBox paramCb = new GuiComboBox(
                                       initValue,
                                       getPossibleChoices(param),
                                       units,
                                       type,
                                       regexp,
                                       width,
                                       abbreviations,
                                       new AccessMode(
                                         getAccessType(param),
                                         isEnabledOnlyInAdvancedMode(param)));
         paramComboBoxAdd(param, prefix, paramCb);
         paramCb.setEditable(true);
         return paramCb;
     }
 
     /**
      * Checks new value of the parameter if it correct and has changed.
      * Returns false if parameter is invalid or has not not changed from
      * the stored value. This is needed to disable apply button, if some of
      * the values are invalid or none of the parameters have changed.
      */
     protected abstract boolean checkParam(String param, String newValue);
 
     /** Checks whether this value matches the regexp of this field. */
     protected final boolean checkRegexp(final String param,
                                         final String newValue) {
        String regexp = getParamRegexp(param);
        if (regexp == null) {
            final GuiComboBox cb = paramComboBoxGet(param, null);
            if (cb != null) {
                regexp = cb.getRegexp();
            }
        }
         if (regexp != null) {
             final Pattern p = Pattern.compile(regexp);
             final Matcher m = p.matcher(newValue);
             if (m.matches()) {
                 return true;
             }
             return false;
         }
         return true;
     }
 
     /**
      * Checks parameter, but use cached value. This is useful if some other
      * parameter was modified, but not this one.
      */
     protected boolean checkParamCache(final String param) {
         if (!paramCorrectValueMap.containsKey(param)) {
             return false;
         }
         final Boolean ret = paramCorrectValueMap.get(param);
         if (ret == null) {
             return false;
         }
         return ret;
     }
 
     /** Sets the cache for the result of the parameter check. */
     protected final void setCheckParamCache(final String param,
                                             final boolean correctValue) {
         paramCorrectValueMap.put(param, correctValue);
     }
 
     /** Returns default value of a parameter. */
     protected abstract String getParamDefault(String param);
 
     /** Returns saved value of a parameter. */
     protected String getParamSaved(final String param) {
         return getResource().getValue(param);
     }
 
     /** Returns preferred value of a parameter. */
     protected abstract String getParamPreferred(String param);
 
     /** Returns short description of a parameter. */
     protected abstract String getParamShortDesc(String param);
 
     /** Returns long description of a parameter. */
     protected abstract String getParamLongDesc(String param);
 
     /**
      * Returns possible choices in a combo box, if possible choices are
      * null, instead of combo box a text field will be generated.
      */
     protected Object[] getPossibleChoices(final String param) {
         return getResource().getPossibleChoices(param);
     }
 
 
     /**
      * Creates panel with border and title for parameters with default
      * background.
      */
     protected final JPanel getParamPanel(final String title) {
         return getParamPanel(title, Browser.PANEL_BACKGROUND);
     }
 
     /**
      * Creates panel with border and title for parameters with specified
      * background.
      */
     protected final JPanel getParamPanel(final String title,
                                          final Color background) {
         final JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.setBackground(background);
         final TitledBorder titleBorder = Tools.getBorder(title);
         panel.setBorder(titleBorder);
         return panel;
     }
 
     /**
      * Returns on mouse over text for parameter. If value is different
      * from default value, default value will be returned.
      */
     protected final String getToolTipText(final String param) {
         final String defaultValue = getParamDefault(param);
         final StringBuffer ret = new StringBuffer(120);
         final GuiComboBox cb = paramComboBoxGet(param, null);
         if (cb != null) {
             final Object value = cb.getStringValue();
             ret.append("<b>");
             ret.append(value);
             ret.append("</b>");
         }
         if (defaultValue != null && !defaultValue.equals("")) {
             ret.append("<table><tr><td><b>");
             ret.append(Tools.getString("Browser.ParamDefault"));
             ret.append("</b></td><td>");
             ret.append(defaultValue);
             ret.append("</td></tr></table>");
         }
         return ret.toString();
 
     }
 
     /**
      * Can be called from dialog box, where it does not need to check if
      * fields have changed.
      */
     public boolean checkResourceFields(final String param,
                                        final String[] params) {
         final boolean cor = checkResourceFieldsCorrect(param, params);
         if (cor) {
             return checkResourceFieldsChanged(param, params);
         }
         return cor;
     }
 
     /** Checks one parameter. */
     protected final void checkOneParam(final String param) {
         checkResourceFieldsCorrect(param, new String[]{param});
     }
 
     /**
      * Returns whether all the parameters are correct. If param is null,
      * all paremeters will be checked, otherwise only the param, but other
      * parameters will be checked only in the cache. This is good if only
      * one value is changed and we don't want to check everything.
      */
     public boolean checkResourceFieldsCorrect(final String param,
                                               final String[] params) {
         /* check if values are correct */
         boolean correctValue = true;
         if (params != null) {
             for (final String otherParam : params) {
                 final GuiComboBox cb = paramComboBoxGet(otherParam, null);
                 if (cb == null) {
                     continue;
                 }
                 String newValue;
                 final Object o = cb.getValue();
                 if (Tools.isStringClass(o)) {
                     newValue = cb.getStringValue();
                 } else if (o instanceof Object[]) {
                     final Object o0 = ((Object[]) o)[0];
                     final Object o1 = ((Object[]) o)[1];
                     newValue = o0.toString();
                     if (o1 != null) {
                         if (o1 instanceof Unit) {
                             newValue += ((Unit) o1).getShortName();
                         }
                     }
                 } else {
                     newValue = ((Info) o).getStringValue();
                 }
 
                 if (param == null || otherParam.equals(param)
                     || !paramCorrectValueMap.containsKey(param)) {
                     final GuiComboBox wizardCb =
                                     paramComboBoxGet(otherParam, "wizard");
                     final boolean enable = isEnabled(otherParam);
                     if (wizardCb != null) {
                         wizardCb.setEnabled(enable);
                         final Object wo = wizardCb.getValue();
                         if (Tools.isStringClass(wo)) {
                             newValue = wizardCb.getStringValue();
                         } else if (wo instanceof Object[]) {
                             newValue =
                                    ((Object[]) wo)[0].toString()
                                    + ((Unit) ((Object[]) wo)[1]).getShortName();
                         } else {
                             newValue = ((Info) wo).getStringValue();
                         }
                     }
                     cb.setEnabled(enable);
                     final boolean check = checkParam(otherParam, newValue)
                                           && checkRegexp(otherParam, newValue);
                     if (check) {
                         if (isTimeType(otherParam)
                             || hasUnitPrefix(otherParam)) {
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     cb.setBackground(
                                                 Tools.extractUnit(
                                                    getParamDefault(otherParam)),
                                                 Tools.extractUnit(
                                                    getParamSaved(otherParam)),
                                                 isRequired(otherParam));
                                 }
                             });
                             if (wizardCb != null) {
                                 wizardCb.setBackground(
                                     Tools.extractUnit(
                                               getParamDefault(otherParam)),
                                     Tools.extractUnit(
                                         getParamSaved(otherParam)),
                                     isRequired(otherParam));
                             }
                         } else {
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     cb.setBackground(
                                              getParamDefault(otherParam),
                                              getParamSaved(otherParam),
                                              isRequired(otherParam));
                                     if (wizardCb != null) {
                                         wizardCb.setBackground(
                                                     getParamDefault(otherParam),
                                                     getParamSaved(otherParam),
                                                     isRequired(otherParam));
                                     }
                                 }
                             });
                         }
                     } else {
                         cb.wrongValue();
                         if (wizardCb != null) {
                             wizardCb.wrongValue();
                         }
                         correctValue = false;
                     }
                     setCheckParamCache(otherParam, check);
                 } else {
                     correctValue = correctValue && checkParamCache(otherParam);
                 }
             }
         }
         return correctValue;
     }
 
     /**
      * Returns whether the specified parameter or any of the parameters
      * have changed. If param is null, only param will be checked,
      * otherwise all parameters will be checked.
      */
     public boolean checkResourceFieldsChanged(final String param,
                                               final String[] params) {
         /* check if something is different from saved values */
         boolean changedValue = false;
         if (params != null) {
             for (String otherParam : params) {
                 final GuiComboBox cb = paramComboBoxGet(otherParam, null);
                 if (cb == null) {
                     continue;
                 }
                 final Object newValue = cb.getValue();
 
                 /* check if value has changed */
                 Object oldValue = getParamSaved(otherParam);
                 if (oldValue == null) {
                     oldValue = getParamDefault(otherParam);
                 }
                 if (isTimeType(otherParam) || hasUnitPrefix(otherParam)) {
                     oldValue = Tools.extractUnit((String) oldValue);
                 }
                 if (!Tools.areEqual(newValue, oldValue)) {
                     changedValue = true;
                 }
             }
         }
         return changedValue;
     }
 
     /** Return JLabel object for the combobox. */
     protected final JLabel getLabel(final GuiComboBox cb) {
         return cb.getLabel();
     }
 
     /** Removes this editable object and clealrs the parameter hashes. */
     public void removeMyself(final boolean testOnly) {
         super.removeMyself(testOnly);
         //if (applyButton != null) {
         //    for (final ActionListener al : applyButton.getActionListeners()) {
         //        applyButton.removeActionListener(al);
         //    }
         //}
     }
 
     /** Waits till the info panel is done for the first time. */
     public final void waitForInfoPanel() {
         try {
             infoPanelLatch.await();
         } catch (InterruptedException ignored) {
             Thread.currentThread().interrupt();
         }
     }
 
     /** Should be called after info panel is done. */
     public final void infoPanelDone() {
         infoPanelLatch.countDown();
     }
 
     /** Adds a panel to the advanced list. */
     protected final void addToAdvancedList(final JPanel p) {
         advancedPanelList.add(p);
     }
 
     /** Hide/Show advanced panels. */
     public void updateAdvancedPanels() {
         final boolean advancedMode = Tools.getConfigData().isAdvancedMode();
         boolean advanced = false;
         for (final JPanel apl : advancedPanelList) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     apl.setVisible(advancedMode);
                 }
             });
             advanced = true;
         }
         for (final JPanel p : advancedOnlySectionList) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     p.setVisible(advancedMode);
                 }
             });
             advanced = true;
         }
         final boolean a = advanced;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 moreOptionsPanel.setVisible(a && !advancedMode);
             }
         });
     }
 
     /** Returns apply button. */
     public final MyButton getApplyButton() {
         return applyButton;
     }
 
     /** Sets apply button. */
     public final void setApplyButton(final MyButton applyButton) {
         this.applyButton = applyButton;
     }
 }
