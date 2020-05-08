 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.outline.cssdialog.tabs;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.ColorDialog;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.outline.cssdialog.FontFamilyDialog;
 import org.jboss.tools.jst.jsp.outline.cssdialog.ImageSelectionDialog;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.CSSConstants;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Constants;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.ImageCombo;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Util;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.ChangeStyleEvent;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.ManualChangeStyleListener;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.StyleAttributes;
 import org.jboss.tools.jst.jsp.outline.cssdialog.parsers.ColorParser;
 
 /**
  * Class for creating control in Quick edit tab
  *
  * @author Evgeny Zheleznyakov
  */
 public class TabQuickEditControl extends Composite {
 
     private static int FIRST_INDEX = 0;
     private static int SECOND_INDEX = 1;
     private static int GRID_NUM_COLUMNS = 3;
     private static int VALUE_NUMBER = 0;
     private static int EXT_VALUE_NUMBER = 1;
     private Label label = null;
     private HashMap<String, ArrayList<String>> comboMap;
     private StyleAttributes styleAttributes;
 
     private ArrayList<ManualChangeStyleListener> listeners = new ArrayList<ManualChangeStyleListener>();
     private boolean updateDataFromStyleAttributes = false;
 
     /**
      * Constructor for creating controls
      *
      * @param composite The parent composite for tab
      */
     public TabQuickEditControl(Composite sc, HashMap<String, ArrayList<String>> comboMap,
         StyleAttributes styleAttributes) {
         super(sc, SWT.NONE);
 
         this.styleAttributes = styleAttributes;
         this.comboMap = comboMap;
 
         GridLayout gridLayout = new GridLayout();
         gridLayout.numColumns = GRID_NUM_COLUMNS;
         setLayout(gridLayout);
 
         init();
     }
 
     /**
      * Update data method.
      */
     public void updateData() {
         Control[] controls = this.getChildren();
         if (controls != null) {
             for (int i = 0; i < controls.length; i++) {
                 if (!controls[i].isDisposed()) {
                     controls[i].dispose();
                 }
             }
         }
 
         init();
         this.layout();
     }
 
     /**
      * Initialize method.
      */
     private void init() {
         ArrayList<String> listKeys = new ArrayList<String>();
         for (String key : styleAttributes.keySet()) {
             listKeys.add(key);
         }
         Collections.sort(listKeys);
         updateDataFromStyleAttributes = true;
         for (String key : listKeys) {
             label = new Label(this, SWT.LEFT);
             label.setText(format(key));
             label.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
             // each attribute might be represented in two ways
             if (comboMap.keySet().contains(key)) {
                 createCombo(key, styleAttributes.getAttribute(key));
             } else {
                 createText(key, styleAttributes.getAttribute(key));
             }
         }
         updateDataFromStyleAttributes = false;
     }
 
     /**
      * Method for creating combo controls.
      *
      * @param composite The parent composite for tab
      * @param name Name of css element
      * @param value Value of css element
      */
     private void createCombo(final String name, final String value) {
         Button btn = null;
         // if css attribute with color combo
         if (name.indexOf(CSSConstants.COLOR) != Constants.DONT_CONTAIN) {
             Composite tmpComposite = getCompositeElement();
 
             final ImageCombo colorCombo = new ImageCombo(tmpComposite, SWT.BORDER);
             colorCombo.setText(value);
             colorCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 
             btn = new Button(tmpComposite, SWT.NONE);
             btn.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 
             ImageDescriptor imageDes = JspEditorPlugin.getImageDescriptor(Constants.IMAGE_COLOR_FILE_LOCATION);
             Image colorImage = imageDes.createImage();
             btn.setImage(colorImage);
             btn.setToolTipText(JstUIMessages.COLOR_DIALOG_TITLE);
             btn.addDisposeListener(new DisposeListener() {
                     public void widgetDisposed(DisposeEvent e) {
                         Button button = (Button) e.getSource();
                         button.getImage().dispose();
                     }
                 });
             btn.addSelectionListener(new SelectionAdapter() {
                     public void widgetSelected(SelectionEvent event) {
                         RGB startRgb = Util.getColor((colorCombo.getText().toLowerCase().trim()));
                         if (startRgb == null) {
                             startRgb = Constants.RGB_BLACK;
                         }
 
                         ColorDialog colorDialog = new ColorDialog(getShell());
                         colorDialog.setRGB(startRgb);
                         colorDialog.setText(JstUIMessages.COLOR_DIALOG_TITLE);
 
                         RGB rgb = colorDialog.open();
                         if (rgb != null) {
                             String str = Util.createColorString(rgb);
                             if (ColorParser.getInstance().getMap().get(str) != null) {
                                 colorCombo.setText(ColorParser.getInstance().getMap().get(str));
                             } else {
                                 colorCombo.setText(str);
                             }
                         }
                     }
                 });
             HashMap<String, String> colorMap = ColorParser.getInstance().getMap();
             for (String key : colorMap.keySet()) {
                 RGB rgb = Util.getColor(key);
                 colorCombo.add(colorMap.get(key), rgb);
             }
             if (btn == null) {
                 new Label(this, SWT.NONE);
             }
 
             colorCombo.setText(value);
             colorCombo.addModifyListener(new ModifyListener() {
                 String key = name;
                 public void modifyText(ModifyEvent event) {
                     if (!colorCombo.getText().trim().equals(Constants.EMPTY)) {
                         styleAttributes.addAttribute(key, colorCombo.getText().trim());
                     } else {
                         styleAttributes.removeAttribute(key);
                     }
                     if (!updateDataFromStyleAttributes) {
                     	notifyListeners();
                     }
                 }
             });
             // if css attribute contain choose_folder button
         } else if (Util.containFolder(name)) {
             Composite tmpComposite = getCompositeElement();
 
         	final Combo combo = new Combo(tmpComposite, SWT.NONE);
             combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
             btn = new Button(tmpComposite, SWT.NONE);
             btn.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 
             ImageDescriptor imageDes = JspEditorPlugin.getImageDescriptor(Constants.IMAGE_FOLDER_FILE_LOCATION);
             Image chooserImage = imageDes.createImage();
             btn.setImage(chooserImage);
             btn.addDisposeListener(new DisposeListener() {
                     public void widgetDisposed(DisposeEvent e) {
                         Button button = (Button) e.getSource();
                         button.getImage().dispose();
                     }
                 });
             btn.setToolTipText(JstUIMessages.COLOR_DIALOG_TITLE);
 
             ArrayList<String> list = comboMap.get(name);
             for (String str : list) {
                 combo.add(str);
             }
 
             btn.addSelectionListener(new SelectionAdapter() {
                     public void widgetSelected(SelectionEvent event) {
                     	IAdaptable project = Util.getCurrentProject();
                         ImageSelectionDialog dialog = new ImageSelectionDialog(getShell(),
                                 new WorkbenchLabelProvider(), new WorkbenchContentProvider());
                         String title = JstUIMessages.IMAGE_DIALOG_TITLE;
                         dialog.setTitle(title);
                         dialog.setMessage(title);
                         dialog.setEmptyListMessage(JstUIMessages.IMAGE_DIALOG_EMPTY_MESSAGE);
                         dialog.setAllowMultiple(false);
                         dialog.setInput(project);
 
                         if (dialog.open() == ImageSelectionDialog.OK) {
                             IFile file = (IFile) dialog.getFirstResult();
                             String value = file.getFullPath().toString();
                             combo.setText(value);
                         }
                     }
                 });
 
             combo.setText(value);
             combo.addModifyListener(new ModifyListener() {
                     String key = name;
 
                     public void modifyText(ModifyEvent event) {
                         if (!combo.getText().trim().equals(Constants.EMPTY)) {
                             styleAttributes.addAttribute(key, combo.getText().trim());
                         } else {
                             styleAttributes.removeAttribute(key);
                         }
                         if (!updateDataFromStyleAttributes) {
                         	notifyListeners();
                         }
                     }
                 });
         } else {
             final Combo combo = new Combo(this, SWT.CENTER);
             GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
             if (!Util.searchInExtElement(name)) {
             	gridData.horizontalSpan = 2;
             }
             combo.setLayoutData(gridData);
 
             ArrayList<String> list = comboMap.get(name);
             for (String str : list) {
                 combo.add(str);
             }
             final Combo extCombo;
             String[] values = null;
             boolean ext = false;
             if (Util.searchInExtElement(name)) {
                 ext = true;
                 values = Util.convertExtString(value);
                 extCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
                 extCombo.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 
                 for (int i = 0; i < Constants.extSizes.length; i++) {
                     extCombo.add(Constants.extSizes[i]);
                 }
 
                 extCombo.select(extCombo.indexOf(values[EXT_VALUE_NUMBER]));
                 extCombo.addModifyListener(new ModifyListener() {
                         String key = name;
 
                         public void modifyText(ModifyEvent event) {
                             String tmpCombo = combo.getText().trim();
                             if (tmpCombo != null && !tmpCombo.equals(Constants.EMPTY)) {
                                 String tmpExt = extCombo.getText();
                                 if (tmpExt != null) {
                                     styleAttributes.addAttribute(key, tmpCombo + tmpExt);
                                 }
                             }
                             if (!updateDataFromStyleAttributes) {
                             	notifyListeners();
                             }
                         }
                     });
 
                 combo.addModifyListener(new ModifyListener() {
                         String key = name;
 
                         public void modifyText(ModifyEvent e) {
                             String currentText = combo.getText();
                             String[] items = combo.getItems();
                             for (int i = 0; i < items.length; i++) {
                                 if (currentText.equalsIgnoreCase(items[i])) {
                                     extCombo.select(VALUE_NUMBER);
                                     extCombo.setEnabled(false);
                                     styleAttributes.addAttribute(key, currentText);
                                     if (!updateDataFromStyleAttributes) {
                                     	notifyListeners();
                                     }
                                     return;
                                 }
                             }
 
                             extCombo.setEnabled(true);
                             String tmp = combo.getText().trim();
                             if (tmp != null && !tmp.equals(Constants.EMPTY)) {
                             	String extTmp = extCombo.getText().trim();
                             	if (extTmp != null) {
                             		styleAttributes.addAttribute(key, tmp + extTmp);
                             	} else {
                             		styleAttributes.addAttribute(key, tmp);
                             	}
                             } else {
                                 styleAttributes.removeAttribute(key);
                             }
                             if (!updateDataFromStyleAttributes) {
                             	notifyListeners();
                             }
                         }
                     });
             } else {
                 combo.addModifyListener(new ModifyListener() {
                         String key = name;
 
                         public void modifyText(ModifyEvent event) {
                             if (!combo.getText().trim().equals(Constants.EMPTY)) {
                                 styleAttributes.addAttribute(key, combo.getText().trim());
                             } else {
                                 styleAttributes.removeAttribute(key);
                             }
                             if (!updateDataFromStyleAttributes) {
                             	notifyListeners();
                             }
                         }
                     });
             }
             if (ext) {
                 combo.setText(values[VALUE_NUMBER]);
             } else {
                 combo.setText(value);
             }
         }
     }
 
     /**
      * Method for creating text controls
      *
      * @param composite The parent composite for tab
      * @param name Name of css element
      * @param value Value of css element
      */
     private void createText(final String name, final String value) {
         Button btn = null;
         final Text text;
 
         // create "button" in case of FONT_FAMILY style property
         if (name.equalsIgnoreCase(CSSConstants.FONT_FAMILY)) {
             Composite tmpComposite = getCompositeElement();
             text = new Text(tmpComposite, SWT.BORDER);
             text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 
             btn = new Button(tmpComposite, SWT.NONE);
             btn.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 
             ImageDescriptor imageDes = JspEditorPlugin.getImageDescriptor(Constants.IMAGE_FONT_FILE_LOCATION);
             Image fontImage = imageDes.createImage();
             btn.setImage(fontImage);
             btn.setToolTipText(JstUIMessages.FONT_FAMILY_TIP);
             btn.addDisposeListener(new DisposeListener() {
                     public void widgetDisposed(DisposeEvent e) {
                         Button button = (Button) e.getSource();
                         button.getImage().dispose();
                     }
                 });
             btn.addSelectionListener(new SelectionAdapter() {
                     public void widgetSelected(SelectionEvent event) {
                         FontFamilyDialog dialog = new FontFamilyDialog(getShell(), text.getText());
                         if (dialog.open() == Window.OK) {
                             text.setText(dialog.getFontFamily());
                         }
                     }
                 });
 
             text.addModifyListener(new ModifyListener() {
                     String key = name;
 
                     public void modifyText(ModifyEvent event) {
                         if (!text.getText().trim().equals(Constants.EMPTY)) {
                             styleAttributes.addAttribute(key, text.getText().trim());
                         } else {
                             styleAttributes.removeAttribute(key);
                         }
                         if (!updateDataFromStyleAttributes) {
                         	notifyListeners();
                         }
                     }
                 });
         } else {
            text = new Text(this, SWT.BORDER);
            text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        }

        String[] values = null;
        boolean ext = false;
        if (btn == null) {
             if (Util.searchInExtElement(name)) {
                 values = Util.convertExtString(value);
                 ext = true;
                 final Combo extCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
                 extCombo.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 
                 for (int i = 0; i < Constants.extSizes.length; i++) {
                     extCombo.add(Constants.extSizes[i]);
                 }
 
                 extCombo.select(extCombo.indexOf(values[EXT_VALUE_NUMBER]));
 
                 extCombo.addModifyListener(new ModifyListener() {
                         String key = name;
 
                         public void modifyText(ModifyEvent event) {
                             String tmpCombo = text.getText().trim();
                             if (tmpCombo != null && !tmpCombo.equals(Constants.EMPTY)) {
                                 String tmpExt = extCombo.getText();
                                 if (tmpExt != null) {
                                     styleAttributes.addAttribute(key, tmpCombo + tmpExt);
                                 }
                             }
                             if (!updateDataFromStyleAttributes) {
                             	notifyListeners();
                             }
                         }
                     });
                 text.addModifyListener(new ModifyListener() {
                         String key = name;
 
                         public void modifyText(ModifyEvent e) {
                             String tmp = text.getText().trim();
                             if (tmp != null && !tmp.equals(Constants.EMPTY)) {
                             	String extTmp = extCombo.getText().trim();
                             	if (extTmp != null) {
                             		styleAttributes.addAttribute(key, tmp + extTmp);
                             	} else {
                             		styleAttributes.addAttribute(key, tmp);
                             	}
                             } else {
                                 styleAttributes.removeAttribute(key);
                             }
                             if (!updateDataFromStyleAttributes) {
                             	notifyListeners();
                             }
                         }
                     });
             } else {
                 text.addModifyListener(new ModifyListener() {
                         String key = name;
 
                         public void modifyText(ModifyEvent event) {
                             if (!text.getText().trim().equals(Constants.EMPTY)) {
                                 styleAttributes.addAttribute(key, text.getText().trim());
                             } else {
                                 styleAttributes.removeAttribute(key);
                             }
                             if (!updateDataFromStyleAttributes) {
                             	notifyListeners();
                             }
                         }
                     });
             }
         }
 
         if (ext) {
             text.setText(values[VALUE_NUMBER]);
         } else {
             text.setText(value);
         }
     }
 
     /**
      * Create container that take up 2 cells and contains fontSizeCombo and extFontSizeCombo elements.
      */
     private Composite getCompositeElement() {
         GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
         gridData.horizontalSpan = 2;
         GridLayout gridLayoutTmp = new GridLayout(2, false);
         gridLayoutTmp.marginHeight = 0;
         gridLayoutTmp.marginWidth = 0;
         Composite classComposite = new Composite(this, SWT.CENTER);
         classComposite.setLayoutData(gridData);
         classComposite.setLayout(gridLayoutTmp);
 
         return classComposite;
     }
 
     /**
      * Method for format css names (text-decoration : Text Decoration)
      *
      * @param str CSS name
      * @return Format ss name
      */
     private String format(String str) {
         StringTokenizer st = new StringTokenizer(str, Constants.DASH);
         String finalStr = Constants.EMPTY;
 
         while (st.hasMoreTokens()) {
             StringBuffer sb = new StringBuffer(st.nextToken());
             String upper = sb.substring(FIRST_INDEX, SECOND_INDEX).toUpperCase();
             sb.replace(FIRST_INDEX, SECOND_INDEX, upper);
             finalStr += sb;
 
             if (st.hasMoreTokens()) {
                 finalStr += Constants.WHITE_SPACE;
             } else {
                 finalStr += Constants.COLON;
             }
         }
 
         return finalStr;
     }
 
     /**
      * Add ManualChangeStyleListener object.
      *
      * @param listener ManualChangeStyleListener object to be added
      */
     public void addManualChangeStyleListener(ManualChangeStyleListener listener) {
         listeners.add(listener);
     }
 
     /**
      * Gets an array of ChangeStyleListener object.
      *
      * @return an array of ChangeStyleListener object
      */
     public ManualChangeStyleListener[] getManualChangeStyleListeners() {
         return listeners.toArray(new ManualChangeStyleListener[listeners.size()]);
     }
 
     /**
      * Remove ManualChangeStyleListener object passed by parameter.
      *
      * @param listener ManualChangeStyleListener object to be removed
      */
     public void removeManualChangeStyleListener(ManualChangeStyleListener listener) {
         listeners.remove(listener);
     }
 
     /**
      * Method is used to notify all subscribed listeners about any changes within style attribute map.
      */
     private void notifyListeners() {
         ChangeStyleEvent event = new ChangeStyleEvent(this);
         for (ManualChangeStyleListener listener : listeners) {
             listener.styleChanged(event);
         }
     }
 }
