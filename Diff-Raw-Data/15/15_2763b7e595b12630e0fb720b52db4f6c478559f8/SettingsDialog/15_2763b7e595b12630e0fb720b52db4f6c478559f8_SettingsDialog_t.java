 /*
  * A dialog where the user can change various settings.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JDialog;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import teo.isgci.problem.Complexity;
 import teo.isgci.util.UserSettings;
 
 /**
  * A dialog where the user can change various settings.
  * 
  */
 public class SettingsDialog extends JDialog {
     
     /**
      * Separator for colorlist.
      */
     private static final String SEPARATOR = "-----------";
     
     /** Fontname. */
     private static final String FONT = "Font";
     
     /** Backgroundname. */
     private static final String BACKGROUND = "Background";
     
     /** Highlightname. */
     private static final String HIGHLIGHT = "Highlight";
     
     /** Selectionname. */
     private static final String SELECTION = "Selection";
     
     /**
      * A button to save changes. Only enabled if there are any changes. 
      * Does not close the dialogue.
      */
     private JButton applyButton;
     
     /**
      * A list with all possible options for which the colours can be changed.
      */
     private JList colorList;
 
     /** Disables enabling the apply button during "internal" changes. */
     private boolean disableApply;
     
     /** Maps a name to a specific color, includes all complexities. */
     private HashMap<String, Color> nameToColor;
 
     /**
      * This field should change every time the class is changed - once it is
      * actually deployed.
      */
     private static final long serialVersionUID = 4L;
    
    /**
     * The value which was selected before the current value in the colorList.
     */
    private Object lastSelectedValue;
 
     /**
      * Creates a new options dialogue.
      * 
      * @param parent
      *            The mainframe from which the dialog is created.
      */
     public SettingsDialog(ISGCIMainFrame parent) {
         super(parent, "Coloursettings", true);
 
         // initialize colors
         nameToColor = new HashMap<String, Color>();
 
         nameToColor.put(FONT, UserSettings.getCurrentFontColor());
         nameToColor.put(BACKGROUND, UserSettings.getCurrentBackgroundColor());
         nameToColor.put(HIGHLIGHT, UserSettings.getCurrentHighlightColor());
         nameToColor.put(SELECTION, UserSettings.getCurrentSelectionColor());
         nameToColor.put(SEPARATOR, Color.gray);
         
         for (Complexity c : Complexity.values()) {
             nameToColor.put(c.getComplexityString(), UserSettings.getColor(c));
         }
 
         // Initialize layout and buttons
         setLayout(new BorderLayout());
         createColorPanel();
         createBottomPanel();
 
         // window related stuff
         pack();
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     }
 
     /**
      * Creates a panel where the user can change colors.
      */
     private void createColorPanel() {
         // CONSTANTS
         final int gap = 10;
 
         /*
          * Adds a list, a color chooser and a checkbox to the graph colors
          * panel. The list holds the different problems, for which the user can
          * color the graph. With the color chooser the user can set colors for
          * the options in the list. If the checkbox is activated, the default
          * grouping of problem colors is set.
          */
 
         // COLORCHOOSER
         final JColorChooser colorChooser = new JColorChooser();
         colorChooser.getSelectionModel().addChangeListener(
                 new ChangeListener() {
 
                     @Override
                     public void stateChanged(ChangeEvent e) {
                         Color newColor = colorChooser.getColor();
 
                         String name = (String) colorList.getSelectedValue();
 
                         if (newColor != nameToColor.get(name)) {
                             nameToColor.put(name, newColor);
                         }
                         
                         enableApplyButton();
                     }
                 });
 
         // COLOR LIST
         colorList = new JList(getColorNames());
         colorList.setSelectedIndex(0);
        lastSelectedValue = colorList.getSelectedValue();
 
         colorChooser.setColor(nameToColor.get(colorList.getSelectedValue()));
 
         colorList.addListSelectionListener(new ListSelectionListener() {
 
             /*
              * Changes the color chooser to the color of the corresponding
              * selection choosen in the list.
              */
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 String name = (String) colorList.getSelectedValue();
 
                 if (name.equals(SEPARATOR)) {
                    colorList.setSelectedValue(lastSelectedValue, false);
                    return;
                 }
                 
                 disableApply = true;
                 colorChooser.setColor(nameToColor.get(name));
                 disableApply = false;
                lastSelectedValue = colorList.getSelectedValue();
             }
         });
 
         // DEFAULT COLOR BUTTON
         JButton setDefaultButtonButton = new JButton("Default colors");
         setDefaultButtonButton.setToolTipText("Chose colors to be used "
                 + "in drawing");
         setDefaultButtonButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 enableApplyButton();
                 
                 // Set default color scheme.
                 HashMap<Complexity, Color> colorscheme = UserSettings
                         .getDefaultColorScheme();
 
                 // DO NOT USE colorScheme.keySet() - it's null with enums!
                 for (Complexity complexity : Complexity.values()) {
                     nameToColor.put(complexity.getComplexityString(),
                             colorscheme.get(complexity));
                 }
 
                 // special colors
                 nameToColor.put(FONT, UserSettings.getDefaultFontColor());
                 nameToColor.put(BACKGROUND,
                         UserSettings.getDefaultBackgroundColor());
                 nameToColor.put(HIGHLIGHT, 
                         UserSettings.getDefaultHighlightColor());
                 nameToColor.put(SELECTION, 
                         UserSettings.getDefaultSelectionColor());
                 
                 String name = (String) colorList.getSelectedValue();
                 colorChooser.setColor(nameToColor.get(name));
 
             }
         });
 
         // COLORBLIND BUTTON
         JButton colorBlindButton = new JButton("Set scheme for color blind");
         colorBlindButton.setToolTipText("Chose presentation of drawing for "
                 + "color-blind people");
         colorBlindButton.addActionListener(new ActionListener() {
 
             // Set color blind color scheme.
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 enableApplyButton();
                 
                 HashMap<Complexity, Color> colorscheme = UserSettings
                         .getDefaultColorBlindColorScheme();
 
                 // DO NOT USE colorScheme.keySet() - it's null with enums!
                 for (Complexity complexity : Complexity.values()) {
                     nameToColor.put(complexity.getComplexityString(),
                             colorscheme.get(complexity));
                 }
 
                 // special colors
                 nameToColor.put(FONT, UserSettings.getDefaultFontColor());
                 nameToColor.put(BACKGROUND,
                         UserSettings.getDefaultBackgroundColor());
                 nameToColor.put(HIGHLIGHT, UserSettings.BLINDHIGHTLIGHTCOLOR);
                 nameToColor.put(SELECTION, 
                         UserSettings.getDefaultSelectionColor());
 
                 String name = (String) colorList.getSelectedValue();
                 colorChooser.setColor(nameToColor.get(name));
             }
         });
 
         // Add layout
         JPanel leftPanel = new JPanel(new BorderLayout(gap, gap));
         leftPanel.add(colorList, BorderLayout.CENTER);
 
         JPanel bottomPanel = new JPanel(new BorderLayout(gap, gap));
         bottomPanel.add(colorBlindButton, BorderLayout.LINE_START);
         bottomPanel.add(setDefaultButtonButton, BorderLayout.LINE_END);
 
         JPanel colorPanel = new JPanel(new BorderLayout(gap, gap));
         colorPanel.add(colorChooser, BorderLayout.CENTER);
         colorPanel.add(leftPanel, BorderLayout.LINE_START);
         colorPanel.add(bottomPanel, BorderLayout.PAGE_END);
 
         // Add border around entire panel
         JPanel bevelPanel = new JPanel();
         bevelPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
         bevelPanel.add(colorPanel);
 
         add(bevelPanel, BorderLayout.CENTER);
     }
 
     /**
      * Creates the basic layout with tabbedpane and apply etc buttons.
      */
     private void createBottomPanel() {
         // bottomright
         JPanel bottomPanel = new JPanel();
 
         // layout
         FlowLayout layout = new FlowLayout();
         layout.setAlignment(FlowLayout.RIGHT);
         bottomPanel.setLayout(layout);
 
         // OK Button
         JButton okButton = new JButton("Ok");
         okButton.setToolTipText("Apply changes and close this dialogue");
         bottomPanel.add(okButton);
 
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 // Saves all changes and close the dialogue.
                 if (applyButton.isEnabled()) {
                     applySettings();
                 }
                 closeDialog();
             }
         });
 
         // Cancel button
         JButton cancelButton = new JButton("Cancel");
         cancelButton.setToolTipText("Discard changes and close this dialogue");
         bottomPanel.add(cancelButton);
 
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 closeDialog();
             }
         });
 
         // Apply button
         applyButton = new JButton("Apply");
         applyButton.setToolTipText("Apply changes");
         applyButton.setEnabled(false);
         bottomPanel.add(applyButton);
 
         applyButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 applySettings();
             }
         });
 
         // add to general layout
         add(bottomPanel, BorderLayout.PAGE_END);
     }
 
     /**
      * Enables the apply button.
      */
     private void enableApplyButton() {
         if (applyButton != null && !disableApply) {
             applyButton.setEnabled(true);
         }
     }
 
     /**
      * Writes all settings the UserSettings.
      */
     private void applySettings() {
 
         // Saves all color.
         applyButton.setEnabled(false);
 
         // set font and background color separately because they're
         // not enums
         UserSettings.setCurrentFontColor(nameToColor.get(FONT));
         UserSettings.setCurrentBackgroundColor(nameToColor.get(BACKGROUND));
         UserSettings.setCurrentHighlightColor(nameToColor.get(HIGHLIGHT));
         UserSettings.setCurrentSelectionColor(nameToColor.get(SELECTION));
 
         // build a new colorscheme and set colors
         HashMap<Complexity, Color> colorScheme 
             = new HashMap<Complexity, Color>();
 
         for (String name : nameToColor.keySet()) {
             Complexity c = Complexity.getComplexity(name);
 
             // Can be null because of font or background
             if (c != null) {
                 UserSettings.setColor(c, nameToColor.get(name));
                 colorScheme.put(c, nameToColor.get(name));
             }
         }
 
         UserSettings.setColorScheme(colorScheme);
     }
 
     /**
      * Close and dispose the dialogue.
      */
     public void closeDialog() {
         setVisible(false);
         dispose();
     }
 
     /**
      * Creates a ordered list of all available colors.
      * 
      * @return
      *          An array containing all available colors in order.
      */
     private Object[] getColorNames() {        
         Vector<String> names = new Vector<String>();
         
         // add special colors
         names.add(FONT);
         names.add(BACKGROUND);
         names.add(HIGHLIGHT);
         names.add(SELECTION);
         names.add(SEPARATOR);
         
         // add sorted complexities
         Complexity[] complexities = Complexity.values();
         Arrays.sort(complexities);
         
         for (Complexity c : complexities) {
             names.add(c.getComplexityString());
         }
         
         return names.toArray();
     }
 }
 
 /* EOF */
