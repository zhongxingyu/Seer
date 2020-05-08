 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.arc.mct.canvas.view;
 
 import gov.nasa.arc.mct.canvas.formatting.ControlAreaFormattingConstants;
 import gov.nasa.arc.mct.canvas.formatting.ControlAreaFormattingConstants.BorderStyle;
 import gov.nasa.arc.mct.canvas.formatting.ControlAreaFormattingConstants.JVMFontFamily;
 import gov.nasa.arc.mct.canvas.panel.Panel;
 import gov.nasa.arc.mct.canvas.panel.PanelBorder;
 import gov.nasa.arc.mct.canvas.view.CanvasIcons.Icons;
 import gov.nasa.arc.mct.gui.util.ConstraintBuilder;
 import static gov.nasa.arc.mct.gui.util.ConstraintBuilder.hbox;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.font.TextAttribute;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.ListCellRenderer;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 public class CanvasFormattingControlsPanel extends JPanel {
     private static final long serialVersionUID = 4964666916367162577L;
 
     // Canvas origin is -7,-7. use this constant to correctly display in
     // formatting to 0,0
     private static final int CORRECTION_OFFSET = 0;
 
     // Row and column numbers on individual formatting panels.
     private static final int POSTION_PANEL_NUMBER_ROWS = 2;
     private static final int POSTION_PANEL_NUMBER_COLUMNS = 1;
 
     // Input field sizes.
     private static final int POS_AND_DIPANEL_XY_TEXT_FIELD_WIDTH = 2;
     private static final int MISCELLANEOUS_PANEL_PANEL_TITLE_FIELD_SIZE = 15;
 
     // Text appearing on pallet
     private static final String POSITION_AND_DIMENSION_TITLE = "Position & Dimensions";
     private static final String BORDERS_TITLE = "Borders";
     private static final String ALIGNMENT_TITLE = "Alignment";
     private static final String MISCELLANEOUS_TITLE = "Miscellaneous";
     private static final String BORDER_STYLE_CAPTION = "Style:";
     private static final String BORDER_COLOR_CAPTION = "Color:";
     private static final String PANEL_TITLE_BAR = "Panel Title Bar";
     private static final String PANEL_TITLE = "Panel Title:";
     private static final String POS_AND_DIPANEL_XY_LABEL = "(x,y): ";
     private static final String POS_AND_DIPANEL_XY_OPEN_BRACE = "(";
     private static final String POS_AND_DIPANEL_XY_CLOSE_BRACE = ")";
     private static final String POS_AND_DIPANEL_XY_DELIMITER = ",";
 
     // GridBag constraint settings
     private static final double POS_AND_DIPANEL_WEIGHT_Y = 1.0;
     private static final double POS_AND_DIPANEL_WEIGHT_X = 0;
     private static final double BORDERS_PANEL_WEIGHT_Y = 1.0;
     private static final double BORDERS_PANEL_WEIGHT_X = 0;
     private static final double ALIGNMENT_PANEL_WEIGHT_Y = 1.0;
     private static final double ALIGNMENT_PANEL_WEIGHT_X = 0;
     private static final double MISCELLANEOUS_PANEL_WEIGHT_Y = 1.0;
     private static final double MISCELLANEOUS_PANEL_WEIGHT_X = 1.0;
     private static final double SEPARATOR_WEIGHT_Y = 1.0;
     private static final double SEPARATOR_WEIGHT_X = 0;
     private static final double MISC_PANEL_WEIGHT_Y = 0;
 
     // Small control buttons on Borders and Alignment panels
     private static final double SMALL_CONTROL_BUTTON_WEIGHT_Y = 1.0;
     private static final double SMALL_CONTROL_BUTTON_WEIGHT_X = 0;
     private static final int SMALL_CONTROL_BUTTON_IPAD_X = 5;
 
     // Button formatting. Provides the small tight square look for border and
     // alignment control buttons
     private static final int BUTTON_BORDER_STYLE_TOP = 1;
     private static final int BUTTON_BORDER_STYLE_LEFT = 0;
     private static final int BUTTON_BORDER_STYLE_BOTTOM = 0;
     private static final int BUTTON_BORDER_STYLE_RIGHT = 0;
 
     // Height/Width Spinner Constraints.
     private static final int SPINNER_INIT_VALUE = 0;
     private static final int SPINNER_MIN_VALUE = 0;
     private static final int SPINNER_MAX_VALUE = 10000; // upper bound on canvas
     // size.
     private static final int SPINNER_STEP_SIZE = 1;
 
     private static final int IPAD_X = 5;
     
     /** The resource bundle we should use for getting strings. */
     private static final ResourceBundle bundle = ResourceBundle.getBundle("CanvasResourceBundle"); //NOI18N
 
     // Class visible GUI controls
 
     // Enables listeners to be disabled when updating panel state.
     private boolean listenersEnabled = true;
 
     // Position & Dimensions Panel
     private JSpinner positionXSpinner = null;
     private JSpinner positionYSpinner = null;
     private JSpinner dimensionVerticalSpinner = null;
     private JSpinner dimensionHorizontalSpinner = null;
 
     // Borders Panel
     private JToggleButton leftBorderButton = null;
     private JToggleButton rightBorderButton = null;
     private JToggleButton topBorderButton = null;
     private JToggleButton bottomBorderButton = null;
     private JToggleButton fullBorderButton = null;
     private JToggleButton noBorderButton = null;
 
     private JComboBox borderStyleComboBox = null;
     private JComboBox borderColorComboBox = null;
 
     // Panel Title Bar Formatting 
     private JCheckBox miscPanelTitleBarCheckBox = null;
     private JFormattedTextField miscPanelTitleField = null;
     private JComboBox panelTitleFont = null;
     private JSpinner panelTitleFontSize;
     private JComboBox panelTitleFontColorComboBox;
     private JComboBox panelTitleBackgroundColorComboBox;
     private JToggleButton panelTitleFontStyleBold;
     private JToggleButton panelTitleFontStyleItalic;
     private JToggleButton panelTitleFontUnderline;
 
     private final CanvasManifestation managedCanvas;
 
     // alignment Panel
     private JButton alignTableLeftButton;
     private JButton alignTableRightButton;
     private JButton alignTableTopButton;
     private JButton alignTableBottomButton;
     private JButton alignTableCenterVerticleButton;
     private JButton alignTableCenterHorizontalButton;
 
     CanvasFormattingControlsPanel(CanvasManifestation managedCanvas) {
         this.managedCanvas = managedCanvas;
         setLayout(new BorderLayout());
         createDefaultCanvasFormmatingControlsPanel();
         // Nothing selected when first initialized, disable GUI controls
         this.setGUIControlsStatusForZeroSelect();
     }
 
     private void enableBorderButtons(boolean enabled) {
         leftBorderButton.setEnabled(enabled);
         rightBorderButton.setEnabled(enabled);
         topBorderButton.setEnabled(enabled);
         bottomBorderButton.setEnabled(enabled);
         fullBorderButton.setEnabled(enabled);
         noBorderButton.setEnabled(enabled);
     }
     
     private void enableAlignmentButtons(boolean enabled) {
         alignTableLeftButton.setEnabled(enabled);
         alignTableRightButton.setEnabled(enabled);
         alignTableTopButton.setEnabled(enabled);
         alignTableBottomButton.setEnabled(enabled);
         alignTableCenterVerticleButton.setEnabled(enabled);
         alignTableCenterHorizontalButton.setEnabled(enabled);
     }
     
     private void createDefaultCanvasFormmatingControlsPanel() {
         JPanel controlPanel = new JPanel(new GridBagLayout());
         // Create top-level panels
         JPanel positionAndDimensionsPanel = createPositionAndDimensionsPanel();
         JPanel bordersPanel = createBordersPanel();
         JPanel alignmentPanel = createAlignmentPanel();
         JPanel miscellaneousPanel = createMiscellaneousPanel();
         JPanel panelTitleFormattingPanel = createPanelTitleFormattingPanel();
 
         GridBagConstraints positionAndDimensionsPanelConstraints = new GridBagConstraints();
 
         // PositionAndDimensions Panel
         positionAndDimensionsPanelConstraints.fill = GridBagConstraints.NONE;
         positionAndDimensionsPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
         positionAndDimensionsPanelConstraints.weighty = POS_AND_DIPANEL_WEIGHT_Y;
         positionAndDimensionsPanelConstraints.weightx = POS_AND_DIPANEL_WEIGHT_X;
         positionAndDimensionsPanelConstraints.insets = new Insets(1, IPAD_X, 0, IPAD_X);
         controlPanel.add(positionAndDimensionsPanel, positionAndDimensionsPanelConstraints);
 
         // Separator
         JSeparator separator1 = new JSeparator(SwingConstants.VERTICAL);
         GridBagConstraints separatorConstraints = new GridBagConstraints();
         separatorConstraints.fill = GridBagConstraints.BOTH;
         separatorConstraints.weighty = SEPARATOR_WEIGHT_Y;
         separatorConstraints.weightx = SEPARATOR_WEIGHT_X;
         separatorConstraints.insets = new Insets(1, 0, 0, 0);
         controlPanel.add(separator1, separatorConstraints);
 
         // Borders Panel
         GridBagConstraints bordersPanelConstraints = new GridBagConstraints();
         bordersPanelConstraints.fill = GridBagConstraints.NONE;
         bordersPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
         bordersPanelConstraints.weighty = BORDERS_PANEL_WEIGHT_Y;
         bordersPanelConstraints.weightx = BORDERS_PANEL_WEIGHT_X;
         bordersPanelConstraints.insets = new Insets(1, IPAD_X, 0, IPAD_X);
         controlPanel.add(bordersPanel, bordersPanelConstraints);
 
         // Separator
         JSeparator separator2 = new JSeparator(SwingConstants.VERTICAL);
         controlPanel.add(separator2, separatorConstraints);
 
         // Alignment Panel
         GridBagConstraints alignmentPanelConstraints = new GridBagConstraints();
         alignmentPanelConstraints.fill = GridBagConstraints.NONE;
         alignmentPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
         alignmentPanelConstraints.weighty = ALIGNMENT_PANEL_WEIGHT_Y;
         alignmentPanelConstraints.weightx = ALIGNMENT_PANEL_WEIGHT_X;
         alignmentPanelConstraints.insets = new Insets(1, IPAD_X, 0, IPAD_X);
         controlPanel.add(alignmentPanel, alignmentPanelConstraints);
 
         // Separator
         JSeparator separator3 = new JSeparator(SwingConstants.VERTICAL);
         controlPanel.add(separator3, separatorConstraints);
         
         // Panel Title Format Panel
         GridBagConstraints panelTitleFormatConstraints = new GridBagConstraints();
         panelTitleFormatConstraints.fill = GridBagConstraints.NONE;
         panelTitleFormatConstraints.anchor = GridBagConstraints.NORTHWEST;
         panelTitleFormatConstraints.weighty = ALIGNMENT_PANEL_WEIGHT_Y;
         panelTitleFormatConstraints.weightx = ALIGNMENT_PANEL_WEIGHT_X;
         panelTitleFormatConstraints.insets = new Insets(1, IPAD_X, 0, IPAD_X);
         controlPanel.add(panelTitleFormattingPanel, panelTitleFormatConstraints);
         
         // Separator
         JSeparator separator4 = new JSeparator(SwingConstants.VERTICAL);
         controlPanel.add(separator4, separatorConstraints);
 
         // Miscellaneous Panel
         GridBagConstraints miscellaneousPanelConstraints = new GridBagConstraints();
         miscellaneousPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
         miscellaneousPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
         miscellaneousPanelConstraints.weightx = MISCELLANEOUS_PANEL_WEIGHT_X;
         miscellaneousPanelConstraints.weighty = MISCELLANEOUS_PANEL_WEIGHT_Y;
         miscellaneousPanelConstraints.insets = new Insets(1, IPAD_X, 0, IPAD_X);
         controlPanel.add(miscellaneousPanel, miscellaneousPanelConstraints);
 
         // Layout top-level panel
         add(controlPanel);
     }
 
     private JPanel createPositionAndDimensionsPanel() {
         JPanel positionAndDimensionsPanel = new JPanel();
         // Border layout - PAGE_START for panel title and CENTER for the
         // controls
         positionAndDimensionsPanel.setLayout(new BorderLayout());
 
         // Add title.
         JLabel positionAndDimensionsTitleLabel = new JLabel(POSITION_AND_DIMENSION_TITLE);
         positionAndDimensionsPanel.add(positionAndDimensionsTitleLabel, BorderLayout.PAGE_START);
 
         // Inner panel to hold position and dimension controls.
         // These are divided over two subpanels and attached to this panel.
         JPanel positionInnerPanel = new JPanel();
         positionInnerPanel.setLayout(new GridLayout(POSTION_PANEL_NUMBER_ROWS,
                         POSTION_PANEL_NUMBER_COLUMNS));
         positionAndDimensionsPanel.add(positionInnerPanel, BorderLayout.CENTER);
 
         // Position Controls
         JPanel positionControlsInnerPanel = new JPanel();
         positionControlsInnerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         JLabel xyLabel = new JLabel(POS_AND_DIPANEL_XY_LABEL);
         xyLabel.setToolTipText("X and Y positions");
 
         SpinnerNumberModel xSpinnerModel = new SpinnerNumberModel(SPINNER_INIT_VALUE,
                         SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, SPINNER_STEP_SIZE);
         positionXSpinner = new JSpinner(xSpinnerModel);
         positionXSpinner.setToolTipText("X position");
         JFormattedTextField xTextField = getTextField(positionXSpinner);
         positionXSpinner.setName("Position&DimensionsPanel_xTextField");
         xTextField.setColumns(POS_AND_DIPANEL_XY_TEXT_FIELD_WIDTH);
 
         SpinnerNumberModel ySpinnerModel = new SpinnerNumberModel(SPINNER_INIT_VALUE,
                         SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, SPINNER_STEP_SIZE);
         positionYSpinner = new JSpinner(ySpinnerModel);
         positionYSpinner.setToolTipText("Y position");
         JFormattedTextField yTextField = getTextField(positionYSpinner);
         positionYSpinner.setName("Position&DimensionsPanel_yTextField");
         yTextField.setColumns(POS_AND_DIPANEL_XY_TEXT_FIELD_WIDTH);
 
         // Attach listeners
         positionXSpinner.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 if (selectedPanels != null && selectedPanels.size() == 1) {
                     Panel selectedPanel = selectedPanels.get(0);
                     int newXLocation = ((Integer) positionXSpinner.getValue()).intValue()
                                     - CORRECTION_OFFSET;
                     if (selectedPanel.getLocation().x != newXLocation) {
                         CanvasFormattingController.notifyXPropertyChange(newXLocation, selectedPanel);
                         managedCanvas.fireFocusPersist();
                     }
                 }
             }
         });
 
         positionYSpinner.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 if (selectedPanels != null && selectedPanels.size() == 1) {
                     Panel selectedPanel = selectedPanels.get(0);
                     int newYLocation = ((Integer) positionYSpinner.getValue()).intValue()
                                     - CORRECTION_OFFSET;
                     if (selectedPanel.getLocation().y != newYLocation) {
                         CanvasFormattingController.notifyYPropertyChange(newYLocation, selectedPanel);
                         managedCanvas.fireFocusPersist();
                     }
                 }
             }
         });
 
         xyLabel.setLabelFor(positionXSpinner);
 
         positionControlsInnerPanel.add(xyLabel);
         positionControlsInnerPanel.add(new JLabel(POS_AND_DIPANEL_XY_OPEN_BRACE));
         positionControlsInnerPanel.add(positionXSpinner);
         positionControlsInnerPanel.add(new JLabel(POS_AND_DIPANEL_XY_DELIMITER));
         positionControlsInnerPanel.add(positionYSpinner);
         positionControlsInnerPanel.add(new JLabel(POS_AND_DIPANEL_XY_CLOSE_BRACE));
 
         // Dimension Controls
         JPanel dimensionsControlsInnerPanel = new JPanel();
         dimensionsControlsInnerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 
         JLabel horizontalLabel = new JLabel(CanvasIcons.getIcon(Icons.PANEL_WIDTH_ICON));
         horizontalLabel.setToolTipText("Panel width");
         SpinnerNumberModel horizontalModel = new SpinnerNumberModel(SPINNER_INIT_VALUE,
                         SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, SPINNER_STEP_SIZE);
         dimensionHorizontalSpinner = new JSpinner(horizontalModel);
         dimensionHorizontalSpinner.setName("Position&DimensionsPanel_horizotnalSpinner");
         dimensionHorizontalSpinner.setToolTipText("Panel width");
         JFormattedTextField horitontalTextField = getTextField(dimensionHorizontalSpinner);
         horitontalTextField.setColumns(POS_AND_DIPANEL_XY_TEXT_FIELD_WIDTH);
         JLabel verticalLabel = new JLabel(CanvasIcons.getIcon(Icons.PANEL_HEIGHT_ICON));
         verticalLabel.setToolTipText("Panel height");
         SpinnerNumberModel verticalModel = new SpinnerNumberModel(SPINNER_INIT_VALUE,
                         SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, SPINNER_STEP_SIZE);
         dimensionVerticalSpinner = new JSpinner(verticalModel);
         dimensionVerticalSpinner.setName("Position&DimensionsPanel_verticalSpinner");
         dimensionVerticalSpinner.setToolTipText("Panel height");
         JFormattedTextField verticleTextField = getTextField(dimensionVerticalSpinner);
         verticleTextField.setColumns(POS_AND_DIPANEL_XY_TEXT_FIELD_WIDTH);
 
         // Attach listeners
         dimensionHorizontalSpinner.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     int newWidth = ((Integer) dimensionHorizontalSpinner.getValue()).intValue();
                     CanvasFormattingController.notifyWidthPropertyChange(newWidth, selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         dimensionVerticalSpinner.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     int newHeight = ((Integer) dimensionVerticalSpinner.getValue()).intValue();
                     CanvasFormattingController.notifyHeightPropertyChange(newHeight, selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         dimensionsControlsInnerPanel.add(horizontalLabel);
         dimensionsControlsInnerPanel.add(dimensionHorizontalSpinner);
         dimensionsControlsInnerPanel.add(verticalLabel);
         dimensionsControlsInnerPanel.add(dimensionVerticalSpinner);
 
         // Place position and dimensions inner panels
         positionInnerPanel.add(positionControlsInnerPanel);
         positionInnerPanel.add(dimensionsControlsInnerPanel);
 
         return positionAndDimensionsPanel;
     }
 
     private JPanel createBordersPanel() {
         JPanel bordersPanel = new JPanel();
 
         // Border layout - PAGE_START for panel title and CENTER for the
         // controls
         bordersPanel.setLayout(new BorderLayout());
 
         JLabel borderPanelTitleLabel = new JLabel(BORDERS_TITLE);
         bordersPanel.add(borderPanelTitleLabel, BorderLayout.PAGE_START);
 
         JPanel centerBorderPanel = new JPanel();
         bordersPanel.add(centerBorderPanel, BorderLayout.CENTER);
 
         centerBorderPanel.setLayout(new GridBagLayout());
 
         // Build border control buttons.
         leftBorderButton = new JToggleButton(CanvasIcons.getIcon(Icons.JLS_LEFT_BORDER_ICON));
         leftBorderButton.setToolTipText("Left border");
         rightBorderButton = new JToggleButton(CanvasIcons.getIcon(Icons.JLS_RIGHT_BORDER_ICON));
         rightBorderButton.setToolTipText("Right border");
         topBorderButton = new JToggleButton(CanvasIcons.getIcon(Icons.JLS_TOP_BORDER_ICON));
         topBorderButton.setToolTipText("Top border");
         bottomBorderButton = new JToggleButton(CanvasIcons.getIcon(Icons.JLS_BOTTOM_BORDER_ICON));
         bottomBorderButton.setToolTipText("Bottom border");
         fullBorderButton = new JToggleButton(CanvasIcons.getIcon(Icons.JLS_ALL_BORDER_ICON));
         fullBorderButton.setToolTipText("All borders");
         noBorderButton = new JToggleButton(CanvasIcons.getIcon(Icons.JLS_NO_BORDER_ICON));
         noBorderButton.setToolTipText("No borders");
 
         leftBorderButton.setName("BorderPanel_LeftBorderButton");
         rightBorderButton.setName("BorderPanel_rightBorderButton");
         topBorderButton.setName("BorderPanel_topBorderButton");
         bottomBorderButton.setName("BorderPanel_bottomBorderButton");
         fullBorderButton.setName("BorderPanel_fullBorderButton");
         noBorderButton.setName("BorderPanel_noBorderButton");
 
         leftBorderButton.setSelectedIcon(CanvasIcons.getIcon(Icons.JLS_LEFT_BORDER_SELECTED_ICON));
         rightBorderButton.setSelectedIcon(CanvasIcons.getIcon(Icons.JLS_RIGHT_BORDER_SELECTED_ICON));
         topBorderButton.setSelectedIcon(CanvasIcons.getIcon(Icons.JLS_TOP_BORDER_SELECTED_ICON));
         bottomBorderButton.setSelectedIcon(CanvasIcons.getIcon(Icons.JLS_BOTTOM_BORDER_SELECTED_ICON));
         fullBorderButton.setSelectedIcon(CanvasIcons.getIcon(Icons.JLS_ALL_BORDER_SELECTED_ICON));
         noBorderButton.setSelectedIcon(CanvasIcons.getIcon(Icons.JLS_NO_BORDER_SELECTED_ICON));
 
         leftBorderButton.setOpaque(false);
         rightBorderButton.setOpaque(false);
         topBorderButton.setOpaque(false);
         bottomBorderButton.setOpaque(false);
         fullBorderButton.setOpaque(false);
         noBorderButton.setOpaque(false);
         leftBorderButton.setFocusPainted(false);
         rightBorderButton.setFocusPainted(false);
         topBorderButton.setFocusPainted(false);
         bottomBorderButton.setFocusPainted(false);
         fullBorderButton.setFocusPainted(false);
         noBorderButton.setFocusPainted(false);
 
         leftBorderButton.setSize(CanvasIcons.getIcon(Icons.JLS_LEFT_BORDER_SELECTED_ICON)
                         .getIconWidth(), CanvasIcons.getIcon(Icons.JLS_LEFT_BORDER_SELECTED_ICON)
                         .getIconHeight());
         leftBorderButton.setContentAreaFilled(false);
         rightBorderButton.setSize(CanvasIcons.getIcon(Icons.JLS_RIGHT_BORDER_SELECTED_ICON)
                         .getIconWidth(), CanvasIcons.getIcon(Icons.JLS_RIGHT_BORDER_SELECTED_ICON)
                         .getIconHeight());
         rightBorderButton.setContentAreaFilled(false);
         topBorderButton.setSize(
                         CanvasIcons.getIcon(Icons.JLS_TOP_BORDER_SELECTED_ICON).getIconWidth(),
                         CanvasIcons.getIcon(Icons.JLS_TOP_BORDER_SELECTED_ICON).getIconHeight());
         topBorderButton.setContentAreaFilled(false);
         bottomBorderButton.setSize(CanvasIcons.getIcon(Icons.JLS_BOTTOM_BORDER_SELECTED_ICON)
                         .getIconWidth(), CanvasIcons.getIcon(Icons.JLS_BOTTOM_BORDER_SELECTED_ICON)
                         .getIconHeight());
         bottomBorderButton.setContentAreaFilled(false);
         fullBorderButton.setSize(CanvasIcons.getIcon(Icons.JLS_ALL_BORDER_SELECTED_ICON)
                         .getIconWidth(), CanvasIcons.getIcon(Icons.JLS_ALL_BORDER_SELECTED_ICON)
                         .getIconHeight());
         fullBorderButton.setContentAreaFilled(false);
         noBorderButton.setSize(CanvasIcons.getIcon(Icons.JLS_NO_BORDER_SELECTED_ICON).getIconWidth(),
                         CanvasIcons.getIcon(Icons.JLS_NO_BORDER_SELECTED_ICON).getIconHeight());
         noBorderButton.setContentAreaFilled(false);
 
         leftBorderButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         rightBorderButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         topBorderButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         bottomBorderButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         fullBorderButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         noBorderButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP, BUTTON_BORDER_STYLE_LEFT,
                         BUTTON_BORDER_STYLE_BOTTOM, BUTTON_BORDER_STYLE_RIGHT));
 
         // Attach listeners
         leftBorderButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 if (listenersEnabled) {
                     CanvasFormattingController
                                     .notifyWestBorderStatus(leftBorderButton.isSelected(),
                                                     selectedPanels);
                     setAllAndNoBorderButtonState(selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         rightBorderButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 if (listenersEnabled) {
                     CanvasFormattingController.notifyEastBorderStatus(rightBorderButton.isSelected(),
                                     selectedPanels);
                     setAllAndNoBorderButtonState(selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         topBorderButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 if (listenersEnabled) {
                     CanvasFormattingController
                                     .notifyNorthBorderStatus(topBorderButton.isSelected(),
                                                     selectedPanels);
                     setAllAndNoBorderButtonState(selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         bottomBorderButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 if (listenersEnabled) {
                     CanvasFormattingController.notifySouthBorderStatus(bottomBorderButton.isSelected(),
                                     selectedPanels);
                     setAllAndNoBorderButtonState(selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         fullBorderButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 boolean enabled = listenersEnabled;
                 if (fullBorderButton.isSelected()) {
                     listenersEnabled = false;
                     leftBorderButton.setSelected(true);
                     rightBorderButton.setSelected(true);
                     topBorderButton.setSelected(true);
                     bottomBorderButton.setSelected(true);
                     noBorderButton.setSelected(false);
                     listenersEnabled = enabled;
                     if (listenersEnabled) {
                         List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                         CanvasFormattingController.notifyAllBorderStatus(true, selectedPanels);
                         managedCanvas.fireFocusPersist();
                     }
                 } else {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     for (Panel p : selectedPanels) {
                         if (p.getBorderState() != PanelBorder.ALL_BORDERS) {
                             fullBorderButton.setSelected(true);
                             return;
                         }
                     }
                 }
             }
         });
 
         noBorderButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 boolean enabled = listenersEnabled;
                 // Once this button is pressed, the user cannot press it again.
                 if (noBorderButton.isSelected()) {
                     listenersEnabled = false;
                     leftBorderButton.setSelected(false);
                     rightBorderButton.setSelected(false);
                     topBorderButton.setSelected(false);
                     bottomBorderButton.setSelected(false);
                     fullBorderButton.setSelected(false);
                     listenersEnabled = enabled;
                     if (listenersEnabled) {
                         List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                         CanvasFormattingController.notifyAllBorderStatus(false, selectedPanels);
                         managedCanvas.fireFocusPersist();
                     }
                 } else {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     for (Panel p : selectedPanels) {
                         if (p.getBorderState() != PanelBorder.NO_BORDER) {
                             noBorderButton.setSelected(true);
                             return;
                         }
                     }
                 }
 
             }
         });
 
         JLabel borderStyleLabel = new JLabel(BORDER_STYLE_CAPTION);
 
         borderStyleComboBox = new JComboBox(generateBorderStyles());
         borderStyleComboBox.setToolTipText("Border style");
         borderStyleComboBox.setName("BorderPanel_styleComboBox");
         borderStyleComboBox.setRenderer(new LineComboBoxRenderer());
         borderStyleComboBox.setPreferredSize(new Dimension(50, 20));
         borderStyleComboBox.setSelectedIndex(0);
 
         // Attach listener to show border styles in combo box.
         borderStyleComboBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyBorderFormattingStyle(borderStyleComboBox.getSelectedIndex(),
                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         borderStyleLabel.setLabelFor(borderStyleComboBox);
 
         JLabel borderColorLabel = new JLabel(BORDER_COLOR_CAPTION);
 
         // Build Color chooser
         borderColorComboBox = new JComboBox(ControlAreaFormattingConstants.BorderColors);
         borderColorComboBox.setName("BorderPanel_colorComboBox");
         borderColorComboBox.setToolTipText("Border color");
         borderColorComboBox.setMaximumRowCount(5);
         borderColorComboBox.setPreferredSize(new Dimension(50, 20));
         borderColorComboBox.setSelectedIndex(0);
         
         ColorComboBoxRenderer renderer = new ColorComboBoxRenderer();
         borderColorComboBox.setRenderer(renderer);
         renderer.remap(managedCanvas.getBackground(), managedCanvas.getDefaultBorderColor());
         
         borderColorLabel.setLabelFor(borderColorComboBox);
 
         // Attach listener to show border styles in combo box.
         borderColorComboBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyBorderColorSelected((Color) borderColorComboBox
                                     .getSelectedItem(), selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         // Arrange components.
         GridBagConstraints leftBorderButtonConstraints = new GridBagConstraints();
         leftBorderButtonConstraints.fill = GridBagConstraints.NONE;
         leftBorderButtonConstraints.anchor = GridBagConstraints.LINE_START;
         leftBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         leftBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         leftBorderButtonConstraints.gridx = 0;
         leftBorderButtonConstraints.gridy = 0;
         centerBorderPanel.add(leftBorderButton, leftBorderButtonConstraints);
 
         GridBagConstraints rightBorderButtonConstraints = new GridBagConstraints();
         rightBorderButtonConstraints.fill = GridBagConstraints.NONE;
         rightBorderButtonConstraints.anchor = GridBagConstraints.LINE_START;
         rightBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         rightBorderButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         rightBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         rightBorderButtonConstraints.gridx = 1;
         rightBorderButtonConstraints.gridy = 0;
         centerBorderPanel.add(rightBorderButton, rightBorderButtonConstraints);
 
         GridBagConstraints topBorderButtonConstraints = new GridBagConstraints();
         topBorderButtonConstraints.fill = GridBagConstraints.NONE;
         topBorderButtonConstraints.anchor = GridBagConstraints.LINE_START;
         topBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         topBorderButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         topBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         topBorderButtonConstraints.gridx = 2;
         topBorderButtonConstraints.gridy = 0;
         centerBorderPanel.add(topBorderButton, topBorderButtonConstraints);
 
         GridBagConstraints centerBorderButtonConstraints = new GridBagConstraints();
         centerBorderButtonConstraints.fill = GridBagConstraints.NONE;
         centerBorderButtonConstraints.anchor = GridBagConstraints.LINE_START;
         centerBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         centerBorderButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         centerBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         centerBorderButtonConstraints.gridx = 3;
         centerBorderButtonConstraints.gridy = 0;
         centerBorderPanel.add(borderStyleLabel, centerBorderButtonConstraints);
 
         GridBagConstraints bottomBorderButtonConstraints = new GridBagConstraints();
         bottomBorderButtonConstraints.fill = GridBagConstraints.BOTH;
         bottomBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         bottomBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         bottomBorderButtonConstraints.gridx = 0;
         bottomBorderButtonConstraints.gridy = 1;
         centerBorderPanel.add(bottomBorderButton, bottomBorderButtonConstraints);
 
         GridBagConstraints fullBorderButtonConstraints = new GridBagConstraints();
         fullBorderButtonConstraints.fill = GridBagConstraints.BOTH;
         fullBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         fullBorderButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         fullBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         fullBorderButtonConstraints.gridx = 1;
         fullBorderButtonConstraints.gridy = 1;
         centerBorderPanel.add(fullBorderButton, fullBorderButtonConstraints);
 
         GridBagConstraints noBorderButtonConstraints = new GridBagConstraints();
         noBorderButtonConstraints.fill = GridBagConstraints.BOTH;
         noBorderButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         noBorderButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         noBorderButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         noBorderButtonConstraints.gridx = 2;
         noBorderButtonConstraints.gridy = 1;
         centerBorderPanel.add(noBorderButton, noBorderButtonConstraints);
 
         GridBagConstraints borderColorLabelConstraints = new GridBagConstraints();
         borderColorLabelConstraints.fill = GridBagConstraints.BOTH;
         borderColorLabelConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         borderColorLabelConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         borderColorLabelConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         borderColorLabelConstraints.gridx = 3;
         borderColorLabelConstraints.gridy = 1;
         centerBorderPanel.add(borderColorLabel, borderColorLabelConstraints);
 
         GridBagConstraints borderSytleComboBoxConstraints = new GridBagConstraints();
         borderSytleComboBoxConstraints.insets = new Insets(0, IPAD_X, 0, 0);
         borderSytleComboBoxConstraints.fill = GridBagConstraints.NONE;
         borderSytleComboBoxConstraints.anchor = GridBagConstraints.LINE_START;
         borderSytleComboBoxConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         borderSytleComboBoxConstraints.weightx = 1;
         borderSytleComboBoxConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         borderSytleComboBoxConstraints.gridx = 4;
         borderSytleComboBoxConstraints.gridy = 0;
         centerBorderPanel.add(borderStyleComboBox, borderSytleComboBoxConstraints);
 
         GridBagConstraints borderColorChooserConstraints = new GridBagConstraints();
         borderColorChooserConstraints.insets = new Insets(0, IPAD_X, 0, 0);
         borderColorChooserConstraints.fill = GridBagConstraints.NONE;
         borderColorChooserConstraints.anchor = GridBagConstraints.LINE_START;
         borderColorChooserConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         borderColorChooserConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         borderColorChooserConstraints.gridx = 4;
         borderColorChooserConstraints.gridy = 1;
         centerBorderPanel.add(borderColorComboBox, borderColorChooserConstraints);
         return bordersPanel;
     }
     
     private JPanel createSpotlightPanel() {
         JPanel panel = new JPanel();
         panel.setLayout(new FlowLayout());
         panel.add(new JLabel("Find:"));
         final JTextField field = new JTextField(MISCELLANEOUS_PANEL_PANEL_TITLE_FIELD_SIZE);
         panel.add(field, BorderLayout.CENTER);
         
         field.getDocument().addDocumentListener(new DocumentListener() {
             
             @Override
             public void removeUpdate(DocumentEvent e) {
                 managedCanvas.augmentation.spotlightText = field.getText().trim();
                 managedCanvas.augmentation.repaint();                
             }
             
             @Override
             public void insertUpdate(DocumentEvent e) {                
                 managedCanvas.augmentation.spotlightText = field.getText().trim();
                 managedCanvas.augmentation.repaint();
             }
             
             @Override
             public void changedUpdate(DocumentEvent e) {
             }
         });
         return panel;            
     }
 
     private void setAllAndNoBorderButtonState(List<Panel> selectedPanels) {
         boolean enabled = listenersEnabled;
         listenersEnabled = false;
 
         boolean noBorder = true;
         boolean allBorder = true;
         for (Panel p : selectedPanels) {
             if (p.getBorderState() == PanelBorder.NO_BORDER) {
                 noBorder = true;
                 allBorder = false;
             } else if (p.getBorderState() == PanelBorder.ALL_BORDERS) {
                 allBorder = true;
                 noBorder = false;
             } else {
                 noBorder = false;
                 allBorder = false;
                 break;
             }
         }
         if (noBorder) {
             noBorderButton.setSelected(true);
             fullBorderButton.setSelected(false);
         } else if (allBorder) {
             noBorderButton.setSelected(false);
             fullBorderButton.setSelected(true);
         } else {
             noBorderButton.setSelected(false);
             fullBorderButton.setSelected(false);
         }
 
         listenersEnabled = enabled;
     }
 
     private JPanel createAlignmentPanel() {
         JPanel alignmentPanel = new JPanel();
         // Border layout - PAGE_START for panel title and CENTER for the
         // controls
         alignmentPanel.setLayout(new BorderLayout());
 
         JLabel alignmentPanelTitleLabel = new JLabel(ALIGNMENT_TITLE);
         alignmentPanel.add(alignmentPanelTitleLabel, BorderLayout.PAGE_START);
         JPanel alignmentCenterPanel = new JPanel();
 
         alignmentPanel.add(alignmentCenterPanel, BorderLayout.CENTER);
 
         alignTableLeftButton = new JButton(CanvasIcons
                         .getIcon(Icons.JLS_ALIGN_TABLE_LEFT_ICON));
         alignTableLeftButton.setToolTipText("Align to left edge");
         alignTableRightButton = new JButton(CanvasIcons
                         .getIcon(Icons.JLS_ALIGN_TABLE_RIGHT_ICON));
         alignTableRightButton.setToolTipText("Align to right edge");
         alignTableTopButton = new JButton(CanvasIcons.getIcon(Icons.JLS_ALIGN_TABLE_TOP_ICON));
         alignTableTopButton.setToolTipText("Align to top edge");
         alignTableBottomButton = new JButton(CanvasIcons
                         .getIcon(Icons.JLS_ALIGN_TABLE_BOTTOM_ICON));
         alignTableBottomButton.setToolTipText("Align to bottom edge");
         alignTableCenterVerticleButton = new JButton(CanvasIcons
                         .getIcon(Icons.JLS_ALIGN_TABLE_VCENTER_ICON));
         alignTableCenterVerticleButton.setToolTipText("Align to vertical center");
         alignTableCenterHorizontalButton = new JButton(CanvasIcons
                         .getIcon(Icons.JLS_ALIGN_TABLE_HCENTER_ICON));
         alignTableCenterHorizontalButton.setToolTipText("Align to horizontal center");
 
         alignTableLeftButton.setName("Alignment_alignLeft");
         alignTableCenterHorizontalButton.setName("Alignment_alignCenterH");
         alignTableRightButton.setName("Alignment_alignRight");
         alignTableBottomButton.setName("Alignment_alignBottom");
         alignTableTopButton.setName("Alignment_alignTop");
         alignTableCenterVerticleButton.setName("Alignment_alignCenterV");
 
         alignTableLeftButton.setContentAreaFilled(false);
         alignTableRightButton.setContentAreaFilled(false);
         alignTableTopButton.setContentAreaFilled(false);
         alignTableBottomButton.setContentAreaFilled(false);
         alignTableCenterVerticleButton.setContentAreaFilled(false);
         alignTableCenterHorizontalButton.setContentAreaFilled(false);
 
         alignTableLeftButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         alignTableRightButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         alignTableTopButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         alignTableBottomButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         alignTableCenterVerticleButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
         alignTableCenterHorizontalButton.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_STYLE_TOP,
                         BUTTON_BORDER_STYLE_LEFT, BUTTON_BORDER_STYLE_BOTTOM,
                         BUTTON_BORDER_STYLE_RIGHT));
 
         // Attach listeners
         alignTableLeftButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyAlignLeftSelected(selectedPanels);
                 managedCanvas.fireFocusPersist();
             }
         });
 
         alignTableCenterHorizontalButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyAlignCenterHSelected(selectedPanels);
                 managedCanvas.fireFocusPersist();
             }
         });
 
         alignTableRightButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyAlignRightSelected(selectedPanels);
                 managedCanvas.fireFocusPersist();
             }
         });
 
         alignTableTopButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyAlignTopSelected(selectedPanels);
                 managedCanvas.fireFocusPersist();
             }
         });
 
         alignTableBottomButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyAlignBottomSelected(selectedPanels);
                 managedCanvas.fireFocusPersist();
             }
         });
 
         alignTableCenterVerticleButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyAlignVCenterSelected(selectedPanels);
                 managedCanvas.fireFocusPersist();
             }
         });
 
         alignmentCenterPanel.setLayout(new GridBagLayout());
 
         GridBagConstraints alignmentCenterButtonConstraints = new GridBagConstraints();
         alignmentCenterButtonConstraints.fill = GridBagConstraints.NONE;
         alignmentCenterButtonConstraints.anchor = GridBagConstraints.LINE_START;
         alignmentCenterButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         alignmentCenterButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         alignmentCenterButtonConstraints.gridx = 0;
         alignmentCenterButtonConstraints.gridy = 0;
         alignmentCenterPanel.add(alignTableLeftButton, alignmentCenterButtonConstraints);
 
         GridBagConstraints alignCenterButtonConstraints = new GridBagConstraints();
         alignCenterButtonConstraints.fill = GridBagConstraints.NONE;
         alignCenterButtonConstraints.anchor = GridBagConstraints.LINE_START;
         alignCenterButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         alignCenterButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         alignCenterButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         alignCenterButtonConstraints.gridx = 1;
         alignCenterButtonConstraints.gridy = 0;
         alignmentCenterPanel.add(alignTableRightButton, alignCenterButtonConstraints);
 
         GridBagConstraints alignTableTopButtonConstraints = new GridBagConstraints();
         alignTableTopButtonConstraints.fill = GridBagConstraints.NONE;
         alignTableTopButtonConstraints.anchor = GridBagConstraints.LINE_START;
         alignTableTopButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         alignTableTopButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         alignTableTopButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         alignTableTopButtonConstraints.gridx = 2;
         alignTableTopButtonConstraints.gridy = 0;
         alignmentCenterPanel.add(alignTableTopButton, alignTableTopButtonConstraints);
 
         GridBagConstraints alignTableBottomButtonConstraints = new GridBagConstraints();
         alignTableBottomButtonConstraints.fill = GridBagConstraints.BOTH;
         alignTableBottomButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         alignTableBottomButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         alignTableBottomButtonConstraints.gridx = 0;
         alignTableBottomButtonConstraints.gridy = 1;
         alignmentCenterPanel.add(alignTableBottomButton, alignTableBottomButtonConstraints);
 
         GridBagConstraints alignTableCenterVerticleButtonConstraints = new GridBagConstraints();
         alignTableCenterVerticleButtonConstraints.fill = GridBagConstraints.BOTH;
         alignTableCenterVerticleButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         alignTableCenterVerticleButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         alignTableCenterVerticleButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         alignTableCenterVerticleButtonConstraints.gridx = 1;
         alignTableCenterVerticleButtonConstraints.gridy = 1;
         alignmentCenterPanel.add(alignTableCenterVerticleButton,
                         alignTableCenterVerticleButtonConstraints);
 
         GridBagConstraints alignTableCenterHorizontalButtonConstraints = new GridBagConstraints();
         alignTableCenterHorizontalButtonConstraints.fill = GridBagConstraints.NONE;
         alignTableCenterHorizontalButtonConstraints.anchor = GridBagConstraints.LINE_START;
         alignTableCenterHorizontalButtonConstraints.weighty = SMALL_CONTROL_BUTTON_WEIGHT_Y;
         alignTableCenterHorizontalButtonConstraints.weightx = SMALL_CONTROL_BUTTON_WEIGHT_X;
         alignTableCenterHorizontalButtonConstraints.ipadx = SMALL_CONTROL_BUTTON_IPAD_X;
         alignTableCenterHorizontalButtonConstraints.gridx = 2;
         alignTableCenterHorizontalButtonConstraints.gridy = 1;
         alignmentCenterPanel.add(alignTableCenterHorizontalButton,
                         alignTableCenterHorizontalButtonConstraints);
 
         // return the completed alignment panel
         return alignmentPanel;
     }
 
     private JPanel createMiscellaneousPanel() {
         JPanel miscellaneousPanel = new JPanel();
         // Border layout - PAGE_START for panel title and CENTER for the
         // controls
         miscellaneousPanel.setLayout(new BorderLayout());
         JLabel miscellaneousPanelTitleLabel = new JLabel(MISCELLANEOUS_TITLE);
 
         // Inner panel holds the controls.
         JPanel miscellaneousInnerPanel = new JPanel();
         miscellaneousInnerPanel.setLayout(new GridBagLayout());
 
         // Arrange inner panel
 
         GridBagConstraints findConstraints = new GridBagConstraints();
         findConstraints.anchor = GridBagConstraints.NORTHWEST;
         findConstraints.fill = GridBagConstraints.NONE;
         findConstraints.weighty = MISC_PANEL_WEIGHT_Y;
         findConstraints.weightx = 1;
         findConstraints.gridx = 0;
         findConstraints.gridy = 1;
         miscellaneousInnerPanel.add(createSpotlightPanel(), findConstraints);
         
         findConstraints = new GridBagConstraints();
         findConstraints.anchor = GridBagConstraints.NORTHWEST;
         findConstraints.fill = GridBagConstraints.NONE;
         findConstraints.weighty = MISC_PANEL_WEIGHT_Y;
         findConstraints.weightx = 1;
         findConstraints.gridx = 0;
         findConstraints.gridy = 0;
         miscellaneousInnerPanel.add(miscellaneousPanelTitleLabel, findConstraints);
         
 
         // attach inner panel
         miscellaneousPanel.add(miscellaneousInnerPanel, BorderLayout.CENTER);
 
         // return completed misc. panel.
         return miscellaneousPanel;
     }
 
     private JPanel createPanelTitleFormattingPanel() {
         JPanel panelTitleFormattingPanel = new JPanel();
         ConstraintBuilder builder = new ConstraintBuilder(panelTitleFormattingPanel);
         JLabel panelLabel = new JLabel("Panel Title Bar Formatting");
         JLabel panelTitleLabel = new JLabel(PANEL_TITLE);
         miscPanelTitleField = new JFormattedTextField();
         miscPanelTitleField.setName("MiscPanel_panelTitleField");
         panelTitleLabel.setLabelFor(miscPanelTitleField);
         miscPanelTitleField.setColumns(MISCELLANEOUS_PANEL_PANEL_TITLE_FIELD_SIZE);
         
         miscPanelTitleBarCheckBox = new JCheckBox(PANEL_TITLE_BAR);
         miscPanelTitleBarCheckBox.setName("MiscPanel_xTitleBarCheckBox");
         
         panelTitleFont = new JComboBox(ControlAreaFormattingConstants.JVMFontFamily.values());
         panelTitleFont.getAccessibleContext().setAccessibleName("panelTitleFontComboBox");
         
         panelTitleFont.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                 List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                 CanvasFormattingController.notifyTitleBarFontSelected(
                                 ((JVMFontFamily) panelTitleFont.getSelectedItem()).toString(),
                                 selectedPanels);
                 managedCanvas.fireFocusPersist();
                 }
             }
         });
 
         // Attach Listeners
         miscPanelTitleBarCheckBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyTitleBarStatus(miscPanelTitleBarCheckBox.isSelected(),
                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
                 miscPanelTitleField.setEditable(miscPanelTitleBarCheckBox.isSelected());
             }
         });
 
         miscPanelTitleField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyNewTitle(miscPanelTitleField.getText(), selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
         
         miscPanelTitleField.addFocusListener(new FocusListener() {
             @Override
             public void focusLost(FocusEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     boolean changed = false;
                     for (Panel p : selectedPanels) {
                         if (!p.getTitle().equals(miscPanelTitleField.getText())) {
                             changed = true;
                         }
                     }
                     if (changed) {
                         CanvasFormattingController.notifyNewTitle(miscPanelTitleField.getText(), selectedPanels);
                         managedCanvas.fireFocusPersist();
                     }
                 }
             }
             
             @Override
             public void focusGained(FocusEvent e) {
             }
         });
         
         
         SpinnerModel panelTitleFontSizeModel = new SpinnerNumberModel(12, 8, 36, 1);
         panelTitleFontSize = new JSpinner(panelTitleFontSizeModel);
         panelTitleFontSize.getAccessibleContext().setAccessibleName(bundle.getString("PANEL_TITLE_FONT_SIZE_SPINNER"));
         panelTitleFontSize.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyTitleBarFontSizeSelected(
                                     Integer.class.cast(panelTitleFontSize.getValue()), 
                                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
                 
             }
         });
         
         panelTitleFontStyleBold = getIconRadioButton("bold_off.png","bold_on.png", 
                         bundle.getString("FONT_BOLD"));
         Insets boldButtonInsets = panelTitleFontStyleBold.getInsets();
         boldButtonInsets.set(boldButtonInsets.top, 0, boldButtonInsets.bottom, 
                         boldButtonInsets.right);
         panelTitleFontStyleBold.setMargin(boldButtonInsets);
         panelTitleFontStyleBold.getAccessibleContext().setAccessibleName("panelTitleFontStyleBold");
         panelTitleFontStyleItalic = getIconRadioButton("italics_off.png","italics_on.png", 
                         bundle.getString("FONT_ITALIC")); 
         panelTitleFontStyleItalic.getAccessibleContext().setAccessibleName("panelTitleFontStyleItalic");
         panelTitleFontUnderline = getIconRadioButton("underline_off.png","underline_on.png", 
                         bundle.getString("FONT_UNDERLINE"));
         panelTitleFontUnderline.getAccessibleContext().setAccessibleName("panelTitleFontStyleUnderline");
         
         ActionListener panelTitleFontStyleListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                     int fontStyle = Font.PLAIN;
                     if (panelTitleFontStyleBold.getModel().isSelected()) {
                         fontStyle = Font.BOLD;
                         if (panelTitleFontStyleItalic.getModel().isSelected()) {
                             fontStyle += Font.ITALIC;
                         }
                     } else if (panelTitleFontStyleItalic.getModel().isSelected()) {
                         fontStyle = Font.ITALIC;
                     }
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyTitleBarFontStyleSelected(
                                     Integer.valueOf(fontStyle), 
                                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         }; 
 
         panelTitleFontStyleBold.addActionListener(panelTitleFontStyleListener);
         panelTitleFontStyleItalic.addActionListener(panelTitleFontStyleListener);
         
         ActionListener panelTitleFontTextAttributeListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                     int attribute = ControlAreaFormattingConstants.UNDERLINE_OFF;
                     if (panelTitleFontUnderline.getModel().isSelected()) {
                         attribute = TextAttribute.UNDERLINE_ON;
                     }
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyTitleBarFontUnderlineSelected(
                                     Integer.valueOf(attribute), 
                                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         }; 
         panelTitleFontUnderline.addActionListener(panelTitleFontTextAttributeListener);
         
         panelTitleFontColorComboBox = new JComboBox(ControlAreaFormattingConstants.BorderColors);
         panelTitleFontColorComboBox.setName("Foreground_colorComboBox");
         panelTitleFontColorComboBox.getAccessibleContext().setAccessibleName("panelTitleFontColorComboBox");
         panelTitleFontColorComboBox.setToolTipText("Font color");
         panelTitleFontColorComboBox.setMaximumRowCount(5);
         panelTitleFontColorComboBox.setPreferredSize(new Dimension(50, 20));
         panelTitleFontColorComboBox.setSelectedIndex(0);
 
 //        BackgroundColorComboBoxRenderer renderer = new BackgroundColorComboBoxRenderer();
         
         panelTitleFontColorComboBox.setRenderer(new ListCellRenderer() {
             
             private ColorPanel myColorPanel = new ColorPanel(new Color(0));
             
             @Override
             public Component getListCellRendererComponent(JList list,
                     Object obj, int arg2, boolean arg3, boolean arg4) { 
                 
                 if (obj instanceof Color) { 
                     myColorPanel.setColor((Color) obj);
                     return myColorPanel;
                 }
                 return new JPanel();
             }
                         
         });
         
         // Attach listener to show foreground colors in combo box.
         panelTitleFontColorComboBox.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyTitleBarFontForegroundColorSelected(
                                     Integer.valueOf((Color.class.cast(panelTitleFontColorComboBox.getSelectedItem())).getRGB()), 
                                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
         
         panelTitleFontStyleBold.addActionListener(panelTitleFontStyleListener);
         panelTitleFontStyleItalic.addActionListener(panelTitleFontStyleListener);
         
         panelTitleBackgroundColorComboBox = new JComboBox(ControlAreaFormattingConstants.BorderColors);
         panelTitleBackgroundColorComboBox.setName("Background_colorComboBox");
         panelTitleBackgroundColorComboBox.getAccessibleContext().setAccessibleName("panelTitleBackgroundColorComboBox");
         panelTitleBackgroundColorComboBox.setToolTipText("Background color");
         panelTitleBackgroundColorComboBox.setMaximumRowCount(5);
         panelTitleBackgroundColorComboBox.setPreferredSize(new Dimension(50, 20));
         panelTitleBackgroundColorComboBox.setSelectedIndex(0);
 
 //        renderer = new BackgroundColorComboBoxRenderer();
         
         panelTitleBackgroundColorComboBox.setRenderer(new ListCellRenderer() {
             
             private ColorPanel myColorPanel = new ColorPanel(new Color(0));
             
             @Override
             public Component getListCellRendererComponent(JList list,
                     Object obj, int arg2, boolean arg3, boolean arg4) { 
                 
                 if (obj instanceof Color) { 
                     myColorPanel.setColor((Color) obj);
                     return myColorPanel;
                 }
                 return new JPanel();
             }
                         
         });
         
         // Attach listener to show foreground colors in combo box.
         panelTitleBackgroundColorComboBox.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (listenersEnabled) {
                     List<Panel> selectedPanels = managedCanvas.getSelectedPanels();
                     CanvasFormattingController.notifyTitleBarFontBackgroundColorSelected(
                                     Integer.valueOf((Color.class.cast(panelTitleBackgroundColorComboBox.getSelectedItem())).getRGB()), 
                                                     selectedPanels);
                     managedCanvas.fireFocusPersist();
                 }
             }
         });
         builder.insets(0, 0, 3, 0).at(0, 0).span(1, 4).nw().add(panelLabel);
         builder.at(1, 0).nw().add(panelTitleLabel);
         builder.at(1, 1).nw().add(miscPanelTitleField);
         builder.at(1, 2).span(1, 2).nw().add(miscPanelTitleBarCheckBox);
         builder.at(2, 0).baseline_w().add(new JLabel(bundle.getString("FONT_NAME_LABEL")), hbox(5));
         builder.at(2, 1).baseline_w().add(panelTitleFont);
         builder.at(2, 2).nw().add(new JLabel(bundle.getString("FONT_COLOR_LABEL")));
         builder.at(2, 3).nw().add(panelTitleFontColorComboBox);
         builder.insets(5,0,0,0).at(3, 0).nw().add(new JLabel(bundle.getString("FONT_SIZE_LABEL")));
         builder.insets(5,0,0,0).at(3, 1).nw().add(panelTitleFontSize);
         builder.at(3, 2).nw().add(new JLabel(bundle.getString("FONT_BACKGROUND_COLOR_LABEL")));
         builder.at(3, 3).nw().add(panelTitleBackgroundColorComboBox);
         builder.insets(5,0,0,0).at(4, 0).nw().add(new JLabel(bundle.getString("FONT_STYLE_LABEL")));
         builder.at(4, 1).nw().add(panelTitleFontStyleBold,
                         panelTitleFontStyleItalic, panelTitleFontUnderline);
 
 
         return panelTitleFormattingPanel;
     }
     
     private JRadioButton getIconRadioButton(String offName, String onName, String description) {
         JRadioButton button = new JRadioButton(loadIcon(offName, description));
         button.setSelectedIcon(loadIcon(onName, description));
         return button;
     }
     
     private Icon loadIcon(String name, String description) {
         URL url = getClass().getClassLoader().getResource("images/" + name);
         return new ImageIcon(url, description);
     }
     
     private static class LineComboBoxRenderer extends JLabel implements ListCellRenderer {
 
         private static final long serialVersionUID = 2325113335574780392L;
         private final PanelBorder border = new PanelBorder(PanelBorder.NORTH_BORDER);
 
         public LineComboBoxRenderer() {
             setOpaque(true);
             setHorizontalAlignment(CENTER);
             setVerticalAlignment(CENTER);
 
             border.setBorderStyle(BorderStyle.SINGLE);
 
             setBorder(new CompoundBorder(new EmptyBorder(4, 4, 4, 4), border));
         }
 
         /*
          * This method finds the image and text corresponding to the selected
          * value and returns the label, set up to display the text and image.
          */
         public Component getListCellRendererComponent(JList list, Object value, int index,
                         boolean isSelected, boolean cellHasFocus) {
             // Get the selected index. (The index param isn't
             // always valid, so just use the value.)
             int selectedIndex = ((Integer) value).intValue();
 
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
 
             border.setBorderStyle(selectedIndex);
             setText("");
             return this;
         }
     }
 
    private static class ColorComboBoxRenderer extends DefaultListCellRenderer implements ListCellRenderer {
         private static final long serialVersionUID = 4172995566305076422L;
 
         private Map<Object, Color> remapper;
 
         public ColorComboBoxRenderer() {
             setPreferredSize(new Dimension(70, 20));
             remapper = new HashMap<Object, Color>();
         }
 
         public void remap(Object object, Color color) {
             remapper.put(object, color);
             
         }
 
         public Component getListCellRendererComponent(JList list, Object value, int index,
                         boolean isSelected, boolean cellHasFocus) {
             JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             /* remap certain colors to different ones, in case our presentation differs from 
              * the way colors are stored internally */
             
             Color c = remapper.get(value);
             if (c == null) {
                 c = (Color) value;
             }
             if (c != null) {
                 setBorder(BorderFactory.createLineBorder(c, 4));
                 component.setText("");
             }
             return component;
         }
     }
 
     /* State Setting methods */
     private void setPositionAndDimensionsValues(int xLoc, int yLoc, int height, int width) {
         listenersEnabled = false;
         positionXSpinner.setValue(xLoc + CORRECTION_OFFSET);
         positionYSpinner.setValue(yLoc + CORRECTION_OFFSET);
 
         dimensionVerticalSpinner.setValue(height);
         dimensionHorizontalSpinner.setValue(width);
         listenersEnabled = true;
     }
     
     
     /** Set the control panel values for multiple selected panels
      * @param selectedPanels
      */
     private void setPanelValuesForMultiSelect(Collection<Panel> selectedPanels) {
         listenersEnabled = false;
         String commonTitleFont = null;
         Integer commonTitleFontSize = null;
         Integer commonTitleFontStyleItalic = null;
         Integer commonTitleFontStyleBold = null;
         Integer commonTitleFontUnderline = null;
         Integer commonTitleFontColor = null;
         Integer commonTitleBackgroundColor = null;
         boolean hasCommonTitleFont = true;
         boolean hasCommonTitleFontSize = true;
         boolean hasCommonTitleFontStyleBold = true;
         boolean hasCommonTitleFontStyleItalic = true;
         boolean hasCommonTitleFontStyleUnderline = true;
         boolean hasCommonTitleFontColor = true;
         boolean hasCommonTitleBackgroundColor = true;
 
         
         for (Panel aPanel : selectedPanels) {
             String aTitleFont = aPanel.getTitleFont();
             if (commonTitleFont !=null && !commonTitleFont.equals(aTitleFont)) {
                 hasCommonTitleFont = false;
             }
             Integer aTitleFontSize = aPanel.getTitleFontSize();
             if (commonTitleFontSize !=null && !commonTitleFontSize.equals(aTitleFontSize)) {
                 hasCommonTitleFontSize = false;
             }
             Integer aTitleFontStyle = aPanel.getTitleFontStyle(); 
             Integer aTitleFontStyleBold = Font.PLAIN;
             
             if (aTitleFontStyle != null) {
                 if (aTitleFontStyle.equals(Font.BOLD) || aTitleFontStyle.equals(Font.ITALIC + Font.BOLD)) {
                     aTitleFontStyleBold = Font.BOLD;
                 }
             } else {
                 aTitleFontStyleBold = null;
             }
             if (commonTitleFontStyleBold !=null && !commonTitleFontStyleBold.equals(aTitleFontStyleBold)) {
                     hasCommonTitleFontStyleBold = false;
             }
             Integer aTitleFontStyleItalic = Font.PLAIN;
             if (aTitleFontStyle != null) {
                 if (aTitleFontStyle.equals(Font.ITALIC) || aTitleFontStyle.equals(Font.ITALIC + Font.BOLD)) {
                     aTitleFontStyleItalic = Font.ITALIC;
                 }
             }   else {
                 aTitleFontStyleItalic = null;
             }
                 
             if (commonTitleFontStyleItalic !=null && !commonTitleFontStyleItalic.equals(aTitleFontStyleItalic)) {
                     hasCommonTitleFontStyleItalic = false;
             }
             Integer aTitleFontUnderline = aPanel.getTitleFontUnderline();
             if (commonTitleFontUnderline !=null && !commonTitleFontUnderline.equals(aTitleFontUnderline)) {
                 hasCommonTitleFontStyleUnderline = false;
             }
             Integer aForegroundColor = aPanel.getTitleFontForegroundColor();
             if (commonTitleFontColor !=null && !commonTitleFontColor.equals(aForegroundColor)) {
                 hasCommonTitleFontColor = false;
             }
             Integer aBackgroundColor = aPanel.getTitleFontBackgroundColor();
             if (commonTitleBackgroundColor !=null && !commonTitleBackgroundColor.equals(aBackgroundColor)) {
                 hasCommonTitleBackgroundColor = false;
             }
 
             commonTitleFont = aTitleFont;
             commonTitleFontSize = aTitleFontSize;
             commonTitleFontStyleBold = aTitleFontStyleBold;
             commonTitleFontStyleItalic =  aTitleFontStyleItalic;
             commonTitleFontUnderline = aTitleFontUnderline;
             commonTitleFontColor = aForegroundColor;
             commonTitleBackgroundColor = aBackgroundColor;
         }
         
 
             if (hasCommonTitleFont && commonTitleFont != null) {
                 panelTitleFont.setSelectedItem(Enum.valueOf(JVMFontFamily.class, 
                                 commonTitleFont));
             } else {
                 panelTitleFont.setSelectedIndex(-1);
             }
             if (hasCommonTitleFontSize && commonTitleFontSize != null) {
                 panelTitleFontSize.setValue(commonTitleFontSize.intValue());
             }
             if (hasCommonTitleFontStyleBold && commonTitleFontStyleBold != null) {
                 if (commonTitleFontStyleBold.equals(Font.BOLD)) {
                     panelTitleFontStyleBold.setSelected(true);
                 } else {
                     panelTitleFontStyleBold.setSelected(false);
                 }
             }
             if (hasCommonTitleFontStyleItalic && commonTitleFontStyleItalic != null) {
                 if (commonTitleFontStyleItalic.equals(Font.BOLD)) {
                     panelTitleFontStyleItalic.setSelected(true);
                 } else {
                     panelTitleFontStyleItalic.setSelected(false);
                 }
             }
             if (hasCommonTitleFontStyleUnderline && commonTitleFontUnderline != null) {
                 panelTitleFontUnderline.setSelected(true);
             } else {
                 panelTitleFontUnderline.setSelected(false);
             }
             
             if (hasCommonTitleFontColor && commonTitleFontColor != null) {
                 panelTitleFontColorComboBox.setSelectedItem(
                                 new Color(commonTitleFontColor));
             } else {
                 panelTitleFontColorComboBox.setSelectedIndex(0);
             }
             
             if (hasCommonTitleBackgroundColor && commonTitleBackgroundColor != null) {
                 panelTitleBackgroundColorComboBox.setSelectedItem(
                                 new Color(commonTitleBackgroundColor));
             } else {
                 panelTitleBackgroundColorComboBox.setSelectedIndex(0);
             }
             
             miscPanelTitleField.setText("");
         
         listenersEnabled = true;
     }
     
     /* Set panel title */
     // For single panel select
     private void setPanelValues(Panel selectedPanel) {
         listenersEnabled = false;
         miscPanelTitleBarCheckBox.setSelected(selectedPanel.hasTitle());
         String panelTitle = selectedPanel.getTitle();
 
         if (selectedPanel.hasTitle()) {
             if (panelTitle == null) {
                 miscPanelTitleField.setText("");
             } else {
                 miscPanelTitleField.setText(panelTitle);
             }
         } else {
             miscPanelTitleField.setText("");
         }
         if (selectedPanel.hasTitle()) {
             if (selectedPanel.getTitleFont() != null) {
                 panelTitleFont.setSelectedItem(Enum.valueOf(JVMFontFamily.class, 
                                     selectedPanel.getTitleFont()));
             } else {
                 panelTitleFont.setSelectedIndex(0);
             }
             if (selectedPanel.getTitleFontSize() != null) {
                 panelTitleFontSize.setValue(selectedPanel.getTitleFontSize().intValue());
             }
             if (selectedPanel.getTitleFontStyle() != null) {
                 if (selectedPanel.getTitleFontStyle().equals(Font.BOLD)) {
                     panelTitleFontStyleBold.setSelected(true);
                 } else if (selectedPanel.getTitleFontStyle().equals(Font.ITALIC)) {
                     panelTitleFontStyleItalic.setSelected(true);     
                 } else if (selectedPanel.getTitleFontStyle().equals(Font.BOLD+Font.ITALIC)) {
                     panelTitleFontStyleBold.setSelected(true);
                     panelTitleFontStyleItalic.setSelected(true);
                 } else {
                     panelTitleFontStyleBold.setSelected(false);
                     panelTitleFontStyleItalic.setSelected(false);
                 }
             }
             if (selectedPanel.getTitleFontUnderline() != null) {
                 if (selectedPanel.getTitleFontUnderline().equals(TextAttribute.UNDERLINE_ON)) {
                     panelTitleFontUnderline.setSelected(true);
                 } else {
                     panelTitleFontUnderline.setSelected(false);
                 }
             }
             
             if (selectedPanel.getTitleFontForegroundColor() != null) {
                 panelTitleFontColorComboBox.setSelectedItem(
                                 new Color(selectedPanel.getTitleFontForegroundColor()));
             } else {
                 panelTitleFontColorComboBox.setSelectedIndex(0);
             }
             if (selectedPanel.getTitleFontBackgroundColor() != null) {
                 panelTitleBackgroundColorComboBox.setSelectedItem(
                                 new Color(selectedPanel.getTitleFontBackgroundColor()));
             } else {
                 panelTitleBackgroundColorComboBox.setSelectedIndex(0);
             }
          }
         
         listenersEnabled = true;
     }
 
     void setBorderValues(byte borderState, BorderStyle borderStyle, Color borderColor) {
         listenersEnabled = false;
         leftBorderButton.setSelected(PanelBorder.hasWestBorder(borderState));
         rightBorderButton.setSelected(PanelBorder.hasEastBorder(borderState));
         topBorderButton.setSelected(PanelBorder.hasNorthBorder(borderState));
         bottomBorderButton.setSelected(PanelBorder.hasSouthBorder(borderState));
         fullBorderButton.setSelected(borderState == PanelBorder.ALL_BORDERS);
         noBorderButton.setSelected(borderState == PanelBorder.NO_BORDER);
         borderStyleComboBox.setSelectedIndex(borderStyle.ordinal());
         borderColorComboBox.setSelectedItem(borderColor);
         listenersEnabled = true;
     }
 
     /* Callbacks from controller */
 
     void informZeroPanelsSelected() {
         setGUIControlsStatusForZeroSelect();
     }
 
     void informOnePanelSelected(List<Panel> selectedPanels) {
         assert selectedPanels.size() == 1;
 
         setGUIControlsStatusForSingleSelect(selectedPanels.get(0));
     }
 
     // Disable GUI components when multiple items are selected
     void informMultipleViewPanelsSelected(Collection<Panel> selectedPanels) {
         setGUIControlsStatusForMultiSelect(selectedPanels);
     }
 
     private void setGUIControlsStatusForZeroSelect() {
         // Turn off Position and Dimension controls */
         listenersEnabled = false;
         positionXSpinner.setEnabled(false);
         positionYSpinner.setEnabled(false);
         dimensionVerticalSpinner.setEnabled(false);
         dimensionHorizontalSpinner.setEnabled(false);
         borderColorComboBox.setEnabled(false);
         borderStyleComboBox.setEnabled(false);
         panelTitleFont.setEnabled(false);
         panelTitleFontSize.setEnabled(false);
         panelTitleFontColorComboBox.setSelectedIndex(0);
         panelTitleFontColorComboBox.setEnabled(false);
         panelTitleBackgroundColorComboBox.setSelectedIndex(0);
         panelTitleBackgroundColorComboBox.setEnabled(false);
         panelTitleFontStyleBold.setSelected(false);
         panelTitleFontStyleBold.setEnabled(false);
         panelTitleFontStyleItalic.setSelected(false);
         panelTitleFontStyleItalic.setEnabled(false);
         panelTitleFontUnderline.setSelected(false);
         panelTitleFontUnderline.setEnabled(false);
 
         // Turn off Miscellaneous panel's Panel Title Bar and Panel title
         // controls
         miscPanelTitleBarCheckBox.setEnabled(false);
         miscPanelTitleField.setEnabled(false);
         miscPanelTitleField.setText("");
         
         enableAlignmentButtons(false);
         enableBorderButtons(false);
         listenersEnabled = true;
     }
 
     private void setGUIControlsStatusForMultiSelect(Collection<Panel> selectedPanels) {
         // Turn off Position and Dimension controls */
         positionXSpinner.setEnabled(false);
         positionYSpinner.setEnabled(false);
         dimensionVerticalSpinner.setEnabled(true);
         dimensionHorizontalSpinner.setEnabled(true);
 
         // Turn off Miscellaneous panel's Panel Title Bar and Panel title
         // controls
         miscPanelTitleBarCheckBox.setEnabled(true);
         miscPanelTitleField.setEnabled(false);
         panelTitleFont.setEnabled(true);
         panelTitleFontSize.setEnabled(true);
         panelTitleFontColorComboBox.setEnabled(true);
         panelTitleBackgroundColorComboBox.setEnabled(true);
         panelTitleFontStyleBold.setEnabled(true);
         panelTitleFontStyleItalic.setEnabled(true);
         panelTitleFontUnderline.setEnabled(true);
 
         borderColorComboBox.setEnabled(true);
         borderStyleComboBox.setEnabled(true);
         setSelectedBorderColor(selectedPanels);
         setSelectedBorderStyle(selectedPanels);
         enableAlignmentButtons(true);
         enableBorderButtons(true);
         setPanelValuesForMultiSelect(selectedPanels);
     }
     
     private void setSelectedBorderStyle(Collection<Panel> panels) {
         listenersEnabled = false;
         Integer style = panels.isEmpty() ? null : panels.iterator().next().getBorderStyle();
         for (Panel p:panels) {
             if (p.getBorderStyle() != style) {
                 style = null;
                 break;
             }
         }
         
         if (style == null) {
             borderStyleComboBox.setSelectedIndex(0);
         } else {
             borderStyleComboBox.setSelectedItem(style);
         }
         
         listenersEnabled = true;
     }
     
     private void setSelectedBorderColor(Collection<Panel> panels) {
         listenersEnabled = false;
         Color c = panels.isEmpty() ? null : panels.iterator().next().getBorderColor();
         for (Panel p:panels) {
             if (!p.getBorderColor().equals(c)) {
                 c = null;
                 break;
             }
         }
         
         if (c == null) {
             borderColorComboBox.setSelectedIndex(-1);
         } else {
             for (int i = 0; i < ControlAreaFormattingConstants.BorderColors.length; i++) {
                 if (c.equals(ControlAreaFormattingConstants.BorderColors[i])) {
                     borderColorComboBox.setSelectedItem(c);
                     break;
                 }
             }
             
         } 
         listenersEnabled = true;
     }
 
     private void setGUIControlsStatusForSingleSelect(Panel selectedPanel) {
         // Turn off Position and Dimension controls */
         positionXSpinner.setEnabled(true);
         positionYSpinner.setEnabled(true);
         dimensionVerticalSpinner.setEnabled(true);
         dimensionHorizontalSpinner.setEnabled(true);
 
         // Turn on Miscellaneous panel's Panel Title Bar and Panel title
         // controls
         miscPanelTitleBarCheckBox.setEnabled(true);
         miscPanelTitleField.setEnabled(true);
         panelTitleFont.setEnabled(true);
         panelTitleFontSize.setEnabled(true);
         panelTitleFontColorComboBox.setEnabled(true);
         panelTitleBackgroundColorComboBox.setEnabled(true);
         panelTitleFontStyleBold.setEnabled(true);
         panelTitleFontStyleItalic.setEnabled(true);
         panelTitleFontUnderline.setEnabled(true);
 
         borderColorComboBox.setEnabled(true);
         borderStyleComboBox.setEnabled(true);
         setSelectedBorderColor(Collections.singleton(selectedPanel));
 
         Rectangle bound = selectedPanel.getBounds();
         setPositionAndDimensionsValues(bound.getLocation().x, bound.getLocation().y, bound
                         .getSize().height, bound.getSize().width);
         setBorderValues(selectedPanel.getBorderState(), BorderStyle.getBorderStyle(selectedPanel
                         .getBorderStyle()), selectedPanel.getBorderColor());
         setPanelValues(selectedPanel);
 
         enableAlignmentButtons(true);
         enableBorderButtons(true);
     }
 
     /* Utilities */
 
     /**
      * Return a list of border styles.
      * 
      * @return the list of border styles
      */
     static Integer[] generateBorderStyles() {
         Integer[] borderStyles = new Integer[ControlAreaFormattingConstants.NUMBER_BORDER_STYLES];
         for (int i = 0; i < ControlAreaFormattingConstants.NUMBER_BORDER_STYLES; i++) {
             borderStyles[i] = Integer.valueOf(i);
         }
         return borderStyles;
     }
 
     /**
      * Utility method extracts the JFormattedTextField from JSpinner controls
      * 
      * @param spinner
      *            the spinner for which we desire its text field
      * @return the text field associated with the spinner.
      * @throws IllegalARgumentExcpetion
      *             if Spinner's textField is not a JSpinner.DefaultEditor.
      */
     static JFormattedTextField getTextField(JSpinner spinner) {
         JComponent editor = spinner.getEditor();
         if (editor instanceof JSpinner.DefaultEditor) {
             return ((JSpinner.DefaultEditor) editor).getTextField();
         } else {
             throw new IllegalArgumentException();
         }
     }
 
         
         /**
          * A JPanel that draws a color, for color dropdowns.
          * @author vwoeltje  
          */
         private static class ColorPanel extends JPanel {
 
             /**
              * 
              */
             private static final long serialVersionUID = 5931786628055358422L;
 
             private static final Dimension COMBO_BOX_DIMENSION = new Dimension(50, 20);
 
             Color color;
             public ColorPanel(Color c) {
                 color = c;
                 setBackground(c);
                 this.setPreferredSize(COMBO_BOX_DIMENSION);         
             }
             
             @Override
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                 g.setColor(color);
                 g.fillRect(0, 0, getWidth(), getHeight());
             }
             
             protected void setColor(Color aColor) {
                 this.color = aColor;
             }
 
             
         }
 }
