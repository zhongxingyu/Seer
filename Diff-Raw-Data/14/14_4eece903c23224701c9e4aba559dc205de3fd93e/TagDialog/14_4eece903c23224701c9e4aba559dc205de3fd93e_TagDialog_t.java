 package org.openstreetmap.josm.plugins.housenumbertool;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.gui.ExtendedDialog;
 import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
 import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
 import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.Collection;
 import java.util.TreeSet;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 /**
  * @author Oliver Raupach 09.01.2012 <http://www.oliver-raupach.de>
  * @author Victor Kropp 10.03.2012 <http://victor.kropp.name>
  */
 public class TagDialog extends ExtendedDialog {
     public static final String TAG_BUILDING = "building";
     public static final String TAG_ADDR_COUNTRY = "addr:country";
     public static final String TAG_ADDR_STATE = "addr:state";
     public static final String TAG_ADDR_CITY = "addr:city";
     public static final String TAG_ADDR_POSTCODE = "addr:postcode";
     public static final String TAG_ADDR_HOUSENUMBER = "addr:housenumber";
     public static final String TAG_ADDR_STREET = "addr:street";
     /**
      *
      */
     private static final long serialVersionUID = 6414385452106276923L;
 
     private AutoCompletionManager acm;
     private Collection<? extends OsmPrimitive> selection;
 
     public static final String TEMPLATE_DATA = "/template.data";
 
     private AutoCompletingComboBox country;
     private AutoCompletingComboBox state;
     private AutoCompletingComboBox city;
     private AutoCompletingComboBox postcode;
     private AutoCompletingComboBox street;
     private JTextField housnumber;
     private final DtoReaderWriter dtoReaderWriter;
     private JLabel houseNumberToLabel;
     private JCheckBox buildingCheckBox;
 
     public TagDialog(String pluginDir, Collection<? extends OsmPrimitive> p_selection) {
         super(Main.parent,
                 tr("House Number Editor"),
                 new String[]{tr("OK"), tr("Cancel")},
                 true
         );
         dtoReaderWriter = new DtoReaderWriter(pluginDir);
         selection = p_selection;
 
         JPanel editPanel = createContentPane();
 
 
         setContent(editPanel);
         setButtonIcons(new String[]{"ok.png", "cancel.png"});
         setDefaultButton(1);
         setupDialog();
         getRootPane().setDefaultButton(defaultButton);
 
         // middle of the screen
         setLocationRelativeTo(null);
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 housnumber.requestFocus();
                 housnumber.selectAll();
             }
         });
     }
 
     private JPanel createContentPane() {
         OsmPrimitive first = selection.iterator().next();
         acm = first.getDataSet().getAutoCompletionManager();
 
         Dto dto = dtoReaderWriter.loadDto(first);
 
         JPanel editPanel = new JPanel(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
 
         buildingCheckBox = new JCheckBox(TAG_BUILDING);
         buildingCheckBox.setFocusable(false);
         buildingCheckBox.setSelected(dto.isSaveBuilding());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 0;
         editPanel.add(buildingCheckBox, c);
 
         AutoCompletingComboBox building = new AutoCompletingComboBox();
         building.setPossibleACItems(acm.getValues(TAG_BUILDING));
         building.setPreferredSize(new Dimension(200, 24));
         building.setEditable(true);
         building.setSelectedItem(first.get(TAG_BUILDING));
         c.gridx = 1;
         c.gridy = 0;
         c.weightx = 1;
         editPanel.add(building, c);
 
         // country
         JLabel countryLabel = new JLabel(TAG_ADDR_COUNTRY);
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 1;
         c.weightx = 0;
         editPanel.add(countryLabel, c);
 
         country = new AutoCompletingComboBox();
         country.setPossibleACItems(acm.getValues(TAG_ADDR_COUNTRY));
         country.setPreferredSize(new Dimension(200, 24));
         country.setEditable(true);
         country.setSelectedItem(dto.getCountry());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 1;
         c.weightx = 1;
         editPanel.add(country, c);
 
         // state
         JLabel stateLabel = new JLabel(TAG_ADDR_STATE);
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 2;
         c.weightx = 0;
         editPanel.add(stateLabel, c);
 
         state = new AutoCompletingComboBox();
         state.setPossibleACItems(acm.getValues(TAG_ADDR_STATE));
         state.setPreferredSize(new Dimension(200, 24));
         state.setEditable(true);
         state.setSelectedItem(dto.getState());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 2;
         c.weightx = 1;
         editPanel.add(state, c);
 
         // city
         JLabel cityLabel = new JLabel(TAG_ADDR_CITY);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 3;
         c.weightx = 0;
         editPanel.add(cityLabel, c);
 
         city = new AutoCompletingComboBox();
         city.setPossibleACItems(acm.getValues(TAG_ADDR_CITY));
         city.setPreferredSize(new Dimension(200, 24));
         city.setEditable(true);
         city.setSelectedItem(dto.getCity());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 3;
         c.weightx = 1;
         editPanel.add(city, c);
 
         // postcode
         JLabel zipEnabled = new JLabel(TAG_ADDR_POSTCODE);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 4;
         c.weightx = 0;
         editPanel.add(zipEnabled, c);
 
         postcode = new AutoCompletingComboBox();
         postcode.setPossibleACItems(acm.getValues(TAG_ADDR_POSTCODE));
         postcode.setPreferredSize(new Dimension(200, 24));
         postcode.setEditable(true);
         postcode.setSelectedItem(dto.getPostcode());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 4;
         c.weightx = 1;
         editPanel.add(postcode, c);
 
         // street
         JLabel streetLabel = new JLabel(TAG_ADDR_STREET);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 5;
         c.weightx = 0;
         editPanel.add(streetLabel, c);
 
         street = new AutoCompletingComboBox();
         street.setPossibleItems(getPossibleStreets());
         street.setPreferredSize(new Dimension(200, 24));
         street.setEditable(true);
         street.setSelectedItem(dto.getStreet());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 5;
         c.weightx = 1;
         editPanel.add(street, c);
 
         // housenumber
         String label = selection.size() > 1 ? tr("{0} from", TAG_ADDR_HOUSENUMBER) : TAG_ADDR_HOUSENUMBER;
         JLabel houseNumberLabel = new JLabel(label);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 6;
         c.weightx = 0;
         editPanel.add(houseNumberLabel, c);
 
         housnumber = new JTextField();
         housnumber.setPreferredSize(new Dimension(200, 24));
         housnumber.setText(dto.getHousenumber());
         housnumber.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent documentEvent) {
                 updateTo();
             }
 
             @Override
             public void removeUpdate(DocumentEvent documentEvent) {
                 updateTo();
             }
 
             @Override
             public void changedUpdate(DocumentEvent documentEvent) {
                 updateTo();
             }
 
             private void updateTo() {
                if (houseNumberToLabel != null)
                    houseNumberToLabel.setText(getHouseNumberToText(housnumber.getText()));
             }
         });
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 6;
         c.weightx = 1;
 
         if (selection.size() > 1) {
             JPanel panel = new JPanel();
             housnumber.setPreferredSize(new Dimension(100, 24));
             panel.add(housnumber);
             houseNumberToLabel = new JLabel(getHouseNumberToText(dto.getHousenumber()));
             houseNumberToLabel.setPreferredSize(new Dimension(100, 24));
             panel.add(houseNumberToLabel);
             editPanel.add(panel, c);
         } else {
             editPanel.add(housnumber, c);
         }
         return editPanel;
     }
 
     private String getHouseNumberToText(String housenumber) {
         return tr(" to {0}", incrementHouseNumber(housenumber, selection.size() - 1));
     }
 
     @Override
     protected void buttonAction(int buttonIndex, ActionEvent evt) {
         if (buttonIndex == 0) {
             Dto dto = new Dto();
             dto.setSaveBuilding(buildingCheckBox.isSelected());
             dto.setCity(getAutoCompletingComboBoxValue(city));
             dto.setCountry(getAutoCompletingComboBoxValue(country));
             dto.setHousenumber(housnumber.getText());
             dto.setPostcode(getAutoCompletingComboBoxValue(postcode));
             dto.setStreet(getAutoCompletingComboBoxValue(street));
             dto.setState(getAutoCompletingComboBoxValue(state));
 
             for (OsmPrimitive primitive : selection) {
                 OsmPrimitiveUpdater.updateJOSMSelection(primitive, dto);
                 dto.setHousenumber(incrementHouseNumber(dto.getHousenumber(), 1));
             }
             dtoReaderWriter.saveDto(dto);
         }
         setVisible(false);
     }
 
     private String incrementHouseNumber(String housenumber, int by) {
         if (housenumber.isEmpty())
             return housenumber;
 
         int houseNumberTo = 0;
         try {
             houseNumberTo = Integer.valueOf(housenumber);
         } catch (NumberFormatException e) {
             char c = housenumber.charAt(housenumber.length() - 1);
             if (Character.isLetter(c)) {
                 try {
                     houseNumberTo = Integer.valueOf(housenumber.substring(0, housenumber.length() - 1));
                     c += by;
                     return String.valueOf(houseNumberTo) + c;
                } catch (NumberFormatException e1) {
                     // nothing to do with this
                 }
             }
             return housenumber;
         }
        houseNumberTo += by * 2;
         return String.valueOf(houseNumberTo);
     }
 
     private String getAutoCompletingComboBoxValue(AutoCompletingComboBox box) {
         Object item = box.getSelectedItem();
         if (item != null) {
             if (item instanceof String) {
                 return (String) item;
             }
             if (item instanceof AutoCompletionListItem) {
                 return ((AutoCompletionListItem) item).getValue();
             }
             return item.toString();
         } else {
             return "";
         }
     }
 
     private Collection<String> getPossibleStreets() {
         /**
          * Generates a list of all visible names of highways in order to do
          * autocompletion on the road name.
          */
         final TreeSet<String> names = new TreeSet<String>();
         for (OsmPrimitive osm : Main.main.getCurrentDataSet()
                 .allNonDeletedPrimitives()) {
             if (osm.getKeys() != null && osm.keySet().contains("highway")
                     && osm.keySet().contains("name")) {
                 names.add(osm.get("name"));
             }
         }
         return names;
     }
 }
