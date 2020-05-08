 package dk.frv.eavdam.app;
 
 import com.bbn.openmap.gui.MapPanelChild;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.layers.OMBaseStation;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.swing.AbstractButton;
 import javax.swing.border.Border;
 import javax.swing.border.CompoundBorder;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 
 public class SidePanel extends JPanel implements MapPanelChild, ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
     private JEditorPane infoPane;
 
 	public SidePanel() {
 	        
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));             
 
 /*
               
         // choose maps title      
               
         JPanel chooseMapsPanel = new JPanel();  
         chooseMapsPanel.setLayout(new BorderLayout());
         chooseMapsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         JLabel chooseMapsLabel = new JLabel("Choose Map(s)", null, JLabel.CENTER);
         chooseMapsLabel.setFont(new Font("Serif", Font.BOLD, 16));
         chooseMapsLabel.setVerticalTextPosition(JLabel.BOTTOM);
         chooseMapsLabel.setHorizontalTextPosition(JLabel.CENTER);               
         chooseMapsPanel.add(chooseMapsLabel);
         chooseMapsPanel.setPreferredSize(new Dimension(250, 40));
         chooseMapsPanel.setMaximumSize(new Dimension(250, 40));
         add(chooseMapsPanel);
                    
         // map names list           
                               
         String[] mapNames = { "Bornholm2W4mEu", "Bornholm2W4mEz", "Bornholm2W4mEz1", "Bornholm2W10mEu", "Bornholm2W10mEz" };
         JList mapList = new JList(mapNames);
         mapList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         mapList.setLayoutOrientation(JList.VERTICAL);
         mapList.setVisibleRowCount(4);
         JScrollPane mapListScroller = new JScrollPane(mapList);
         mapListScroller.setBorder(new CompoundBorder
             (BorderFactory.createEmptyBorder(0,10,10,10),
             BorderFactory.createLineBorder(new Color(122, 138, 153), 1)));
         mapListScroller.setPreferredSize(new Dimension(250, 90));
         mapListScroller.setMaximumSize(new Dimension(250, 90));
         add(mapListScroller);
      
         // add map and remove map buttons
      
         JPanel mapButtonsPanel = new JPanel();
         mapButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10)); 
         GridLayout gridLayout = new GridLayout(1,2);               
         gridLayout.setHgap(10);
         mapButtonsPanel.setLayout(gridLayout);
         JButton addMapButton = new JButton("Add map", null);        
         addMapButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         addMapButton.setHorizontalTextPosition(AbstractButton.CENTER);
         addMapButton.setPreferredSize(new Dimension(100, 35));
         addMapButton.setMaximumSize(new Dimension(100, 35));
         mapButtonsPanel.add(addMapButton);
         JButton removeMapButton = new JButton("Remove map", null);        
         removeMapButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         removeMapButton.setHorizontalTextPosition(AbstractButton.CENTER);
         removeMapButton.setPreferredSize(new Dimension(130, 35));
         removeMapButton.setMaximumSize(new Dimension(130, 35));
         mapButtonsPanel.add(removeMapButton);        
         mapButtonsPanel.setMinimumSize(new Dimension(250, 35));
         mapButtonsPanel.setPreferredSize(new Dimension(250, 35));
         mapButtonsPanel.setMaximumSize(new Dimension(250, 35));        
         add(mapButtonsPanel);
 
         // empty textarea
 
         JTextArea textArea = new JTextArea("");
         textArea.setLineWrap(true);
         textArea.setWrapStyleWord(true);
         textArea.setEditable(false);
         JScrollPane areaScrollPane = new JScrollPane(textArea);
         areaScrollPane.setBorder(new CompoundBorder
             (BorderFactory.createEmptyBorder(0,10,10,10),
             BorderFactory.createLineBorder(new Color(122, 138, 153), 1)));
         areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         areaScrollPane.setPreferredSize(new Dimension(250, 90));
         areaScrollPane.setMaximumSize(new Dimension(250, 90));
         add(areaScrollPane);
         
         // display maps button
         
         JPanel displayMapsButtonPanel = new JPanel();
         displayMapsButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));                
         displayMapsButtonPanel.setLayout(new GridLayout(1,1));
         JButton displayMapsButton = new JButton("Display map(s)", null);        
         displayMapsButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         displayMapsButton.setHorizontalTextPosition(AbstractButton.CENTER);
         displayMapsButtonPanel.add(displayMapsButton);        
         displayMapsButtonPanel.setPreferredSize(new Dimension(150, 35));
         displayMapsButtonPanel.setMaximumSize(new Dimension(150, 35));        
         add(displayMapsButtonPanel);        
           
         // base station parameters title
 
         JPanel baseStationParametersPanel = new JPanel();  
         baseStationParametersPanel.setLayout(new BorderLayout());
         baseStationParametersPanel.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));
         JLabel baseStationParametersLabel = new JLabel("Base station parameters", null, JLabel.CENTER);
         baseStationParametersLabel.setFont(new Font("Serif", Font.BOLD, 16));
         baseStationParametersLabel.setVerticalTextPosition(JLabel.BOTTOM);
         baseStationParametersLabel.setHorizontalTextPosition(JLabel.CENTER);               
         baseStationParametersPanel.add(baseStationParametersLabel);
         baseStationParametersPanel.setPreferredSize(new Dimension(250, 40));
         baseStationParametersPanel.setMaximumSize(new Dimension(250, 40));
         add(baseStationParametersPanel);
         */
         
         // station information
         
         infoPane = new JEditorPane("text/html",
             "<p><strong>Click a station to view data:<strong></p>" +
             "<table cellspacing=1 cellpadding=1><tr><td>Organization:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Name:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Type:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Latitude:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Longitude:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">MMSI:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Power:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Antenna type:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Antenna height:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Terrain height:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Heading:</td><td>...</td></tr>" +
             "<tr><td valign=\"top\">Angle:</td><td>...</td></tr>" +            
             "<tr><td valign=\"top\">Gain:</td><td>...</td></tr>" +
            "<tr><td valign=\"top\">Reserved blocks for Ch A:</td><td>...</td></tr>" +
			"<tr><td valign=\"top\">Reserved blocks for Ch B:</td><td>...</td></tr></table>");
         infoPane.setBackground(new Color(238, 238, 238));
         //infoPane.setBorder(new CompoundBorder
         //    (BorderFactory.createLineBorder(new Color(122, 138, 153), 1),
         //    BorderFactory.createEmptyBorder(0,10,0,10)));
         infoPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
         infoPane.setPreferredSize(new Dimension(230, 500));
         infoPane.setMaximumSize(new Dimension(230, 500));
         infoPane.setEditable(false);
         JScrollPane infoScrollPane = new JScrollPane(infoPane);
         infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         infoScrollPane.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
         infoScrollPane.setPreferredSize(new Dimension(230, 500));
         infoScrollPane.setMaximumSize(new Dimension(230, 500));
         add(infoScrollPane);
         //add(infoPane);        
         
         // efficiensea logo
         
         URL efficienSeaImgURL = getClass().getResource("/share/data/images/efficiensea.png");        
         if (efficienSeaImgURL != null) {
             ImageIcon icon = new ImageIcon(efficienSeaImgURL, "");           
             JLabel iconLabel = new JLabel(icon);
             JPanel iconPanel = new JPanel();  
             iconPanel.setLayout(new BorderLayout());
             iconPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));             
             iconPanel.add(iconLabel);
             iconPanel.setMinimumSize(new Dimension(250, 75));
             iconPanel.setPreferredSize(new Dimension(250, 75));
             iconPanel.setMaximumSize(new Dimension(250, 75));
             add(iconPanel);
         } else {
             System.err.println("Couldn't find efficiensea.png file");
         }     
         
         // eu baltic logo
         
         URL euBalticImgURL = getClass().getResource("/share/data/images/euBaltic.png");        
         if (euBalticImgURL != null) {
             ImageIcon icon = new ImageIcon(euBalticImgURL, "");           
             JLabel iconLabel = new JLabel(icon);
             JPanel iconPanel = new JPanel();  
             iconPanel.setLayout(new BorderLayout());
             iconPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));             
             iconPanel.add(iconLabel);
             iconPanel.setMinimumSize(new Dimension(250, 50));
             iconPanel.setPreferredSize(new Dimension(250, 50));
             iconPanel.setMaximumSize(new Dimension(250, 50));
             add(iconPanel);
         } else {
             System.err.println("Couldn't find euBaltic.png file");
         }             	    
         
 	}
 
 	@Override
 	public String getPreferredLocation() {
 		return BorderLayout.EAST;
 	}
 
 	@Override
 	public void setPreferredLocation(String arg0) {}
 
 	@Override
 	public String getParentName() {
 		return null;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {}
 	
 	public void showInfo(OMBaseStation omBaseStation) {
 		EAVDAMUser owner = omBaseStation.getOwner();
 	    AISFixedStationData stationData = omBaseStation.getStationData();
 	    
 	    String infoText = "<p><strong>Click a station to view data:</strong></p>" +
             "<table cellspacing=1 cellpadding=1><tr><td valign=\"top\">Organization:</td><td valign=\"top\">";
         if (owner != null && owner.getOrganizationName() != null) {
             infoText += owner.getOrganizationName();
         } else {
             infoText += "...";
         }
         infoText += "</td></tr><tr><td valign=\"top\">Name:</td><td valign=\"top\">";		
 		if (stationData.getStationName() != null) {
             infoText += stationData.getStationName();
         } else {
             infoText += "...";
         }
         infoText += "</td></tr><tr><td valign=\"top\">Type:</td><td valign=\"top\">";
         if (stationData.getStationType() != null) {
             if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
                 infoText += "AIS Base Station";
             } else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
                 infoText += "AIS Repeater";
             } else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
                 infoText += "AIS Receiver station";
             } else if (stationData.getStationType() == AISFixedStationType.ATON) {
                 infoText += "AIS AtoN station";
             }
         } else {
             infoText += "...";
         }
         infoText += "</td></tr><tr><td valign=\"top\">Latitude:</td><td valign=\"top\">" + stationData.getLat() +
             "</td></tr><tr><td valign=\"top\">Longitude:</td><td valign=\"top\">" + stationData.getLon() + "</td></tr>" +
             "<tr><td>MMSI:</td><td>";
         if (stationData.getMmsi() != null) {    
             infoText += stationData.getMmsi();
         } else {
             infoText += "...";
         }
         infoText += "</td></tr><tr><td valign=\"top\">Power:</td><td valign=\"top\">";
         if (stationData.getTransmissionPower() != null) {
             infoText += stationData.getTransmissionPower().toString();
         } else {
             infoText += "...";
         }
         infoText +="</td></tr><tr><td valign=\"top\">Antenna type:</td><td valign=\"top\">";
         if (stationData.getAntenna() != null) {
             Antenna antenna = stationData.getAntenna();            
             if (antenna.getAntennaType() != null) {
                 AntennaType antennaType = antenna.getAntennaType();
                 if (antenna.getAntennaType() == AntennaType.OMNIDIRECTIONAL) {
                     infoText += "Omnidirectional";
                 } else if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
                     infoText += "Directional";
                 }
             } else {
                 infoText += "...";
             }
             infoText += "</td></tr><tr><td valign=\"top\">Antenna height:</td><td valign=\"top\">" +
                 antenna.getAntennaHeight() + "</td></tr>" +
                 "<tr><td>Terrain height:</td><td valign=\"top\">" + antenna.getTerrainHeight() +
                 "</td></tr><tr><td valign=\"top\">Heading:</td><td valign=\"top\">";
             if (antenna.getHeading() != null) {                
                 infoText += antenna.getHeading().toString();
             } else {
                 infoText += "...";
             }
             infoText += "</td></tr><tr><td valign=\"top\">Angle:</td><td valign=\"top\">";
             if (antenna.getFieldOfViewAngle() != null) {                
                 infoText += antenna.getFieldOfViewAngle().toString();
             } else {
                 infoText += "...";
             }  
             infoText += "</td></tr><tr><td valign=\"top\">Gain:</td><td valign=\"top\">";
             if (antenna.getGain() != null) {                
                 infoText += antenna.getGain().toString();
             } else {
                 infoText += "...";
             }
             infoText += "</td></tr>";         
         } else {
             infoText += "...</td></tr>" +
             "<tr><td valign=\"top\">Antenna height:</td><td valign=\"top\">...</td></tr>" +
             "<tr><td valign=\"top\">Terrain height:</td><td valign=\"top\">...</td></tr>" +
             "<tr><td valign=\"top\">Heading:</td><td>...</td valign=\"top\"></tr>" +
             "<tr><td valign=\"top\">Angle:</td><td>...</td valign=\"top\"></tr>" +            
             "<tr><td valign=\"top\">Gain:</td><td>...</td valign=\"top\"></tr>";
         }
 		if (stationData.getReservedBlocksForChannelA() != null && !stationData.getReservedBlocksForChannelA().isEmpty()) {
 			String temp = "";
 			for (int i=0; i<stationData.getReservedBlocksForChannelA().size(); i++) {
 				Integer reservedBlock = stationData.getReservedBlocksForChannelA().get(i);
 				if (i > 0) {
 					temp += ", ";
 				}
 				temp += reservedBlock.toString();
 			}
			infoText += "<tr><td valign=\"top\">Reserved blocks for Ch A:</td><td valign=\"top\">" + temp + "</td></tr>";
 		} else {			 
			 infoText += "<tr><td valign=\"top\">Reserved blocks for Ch A:</td><td valign=\"top\">...</td></tr>";
 		}
 		if (stationData.getReservedBlocksForChannelB() != null && !stationData.getReservedBlocksForChannelB().isEmpty()) {
 			String temp = "";
 			for (int i=0; i<stationData.getReservedBlocksForChannelB().size(); i++) {
 				Integer reservedBlock = stationData.getReservedBlocksForChannelB().get(i);
 				if (i > 0) {
 					temp += ", ";
 				}
 				temp += reservedBlock.toString();
 			}
			infoText += "<tr><td valign=\"top\">Reserved blocks for Ch B:</td><td valign=\"top\">" + temp + "</td></tr>";
 		} else {			 
			 infoText += "<tr><td valign=\"top\">Reserved blocks for Ch B:</td><td valign=\"top\">...</td></tr>";
 		}			
 		infoText += "</table>";					
 					
         infoPane.setText(infoText);
 	}
 	
 }
