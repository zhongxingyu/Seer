 package asidesktop.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 import org.openstreetmap.gui.jmapviewer.JMapViewer;
 import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
 import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import java.awt.Font;
 import javax.swing.UIManager;
 
 /**
 *
 * @author Liam
 */
 public class AsiDesktopGui extends javax.swing.JFrame {
 
 	private static final String URL = 
 			//*   //<-- Comment toggler, add leading / to enable first section
 			"http://asi-estimator.appspot.com/asi_estimator"
 			/*/
 			"http://127.0.0.1:8888/asi_estimator"
 			//*/
 			;
 	
 	private enum Direction {
 		N (0),
 		NE (45),
 		E (90),
 		SE (135),
 		S (180),
 		SW (225),
 		W (270),
 		NW (315);
 		
 		private int angle;
 		private Direction(int angle) {
 			this.angle = angle;
 		}
 		public int getAngle() {
 			return angle;
 		}
 	}
 	
	
 	private double lat = 0.0;
 	private double lng;
 	
    /**
     * Creates new form AsiDesktopGui
     */
    public AsiDesktopGui() {
        initComponents();
        calculateButton.addActionListener(new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			try {
 				//Set up the connection
 				URL url = new URL(URL+"/estimate");
				URLConnection conn = url.openConnection();
 				conn.setDoOutput(true);
 				conn.connect();
 				//Send the data
 				String xml = generateXML();
 				System.out.println(xml);
 				OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
 				osw.write(xml);
 				osw.flush();
				osw.close();
 				//Assemble data into useful state
 				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 				DocumentBuilder db = dbf.newDocumentBuilder();
 				Document doc = db.parse(conn.getInputStream());
 				
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
     	   
        });
    }
    
    private void showResults(Document doc){
 	   //TODO
    }
    
    public int getDirectionAngle(String direction) {
 	   return Direction.valueOf(direction).getAngle();
    }
    
    private String generateXML() {
 		return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
 				"<!DOCTYPE solarquery SYSTEM \"http://asi-estimator.appspot.com/solarquery.dtd\">"+
 				"<solarquery>"+
 				"	<array>"+
 				"		<bank facing=\"%d\"" +
 							" number=\"%s\" power=\"%s\"" +
 							" tilt=\"%s\"" +
 							" price=\"%s\"" +
 							" latitude=\"%f\" />"+
 				"	</array>"+
 				"	<feedin rate=\"%s\" />"+
 				"	<consumption power=\"%s\" rate=\"%s\"/>" +
 				"	<sunlight hours=\"%s\" />" +
 				"	<inverter efficiency=\"%s\" price=\"%f\" />"+
 				"</solarquery>", getDirectionAngle(panelDirection.getSelectedItem()), numberPanels.getText(), panelWattage.getText(),
 				panelAngle.getSelectedItem(), panelCost.getText(), lat, tariffRates.getText(),
 				annualPower.getText(), electricityCost.getText(), sunlightHours.getText(),
 				inverterEfficiency.getText(), 
 				Double.parseDouble(inverterCost.getText()) + Double.parseDouble(installationCost.getText()));
 	}
    
    public void setLocation(double lat, double lng) {
 	   this.lat = lat;
 	   this.lng = lng;
 	   map.removeMapMarker(mapMarker);
 	   mapMarker = new MapMarkerDot(lat, lng);
 	   map.addMapMarker(mapMarker);
 	   
 	   doPrefill(findLocationName(lat, lng));
 	   
    }
    
    /**
     * Using lat and lng, finds the location's name
     * @return
     */
    private String findLocationName(double lat, double lng)  {
 	   BufferedReader in = null;
 	   String xml = "";
 	   
 	   try {
 		   String returnType = "xml"; //xml or json
 		   URL url = new URL("http://maps.googleapis.com/maps/api/geocode/"+returnType+"?latlng="+lat+","+lng+"&sensor=false");
 		   in = new BufferedReader(new InputStreamReader(url.openStream()));
 		   String inputLine;
 		   while ((inputLine = in.readLine()) != null) {
 			 xml += inputLine;
 		   }
     
 	   } catch (MalformedURLException e) {
 		   
 	   } catch (IOException e) {
 		   
 	   } finally {
 		   // will try to close the reader even if exception thrown
 		   try {
 			in.close();
 		} catch (IOException e) {
 			// We really REALLY don't want it to die here
 			e.printStackTrace();
 		}
 	   }
 
 	   return convertXmlToLocation(xml);
    }
    
    
    // guts of this found here.
    // http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
    private String convertXmlToLocation(String rawText) {
 	   String location = "";
 	   String country = "";
 	   String state = "";
 	   
 	   try {
 		   DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		   DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		   InputSource xml = new InputSource(new StringReader(rawText));
 		   Document doc = dBuilder.parse(xml);
 		   
 		   doc.getDocumentElement().normalize();
 		   
 		   NodeList results = doc.getElementsByTagName("result");
 		   
 		   for (int temp = 0; temp < results.getLength(); temp++) {
 			   Node nNode = results.item(temp);
 			   
 			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 			      Element eElement = (Element) nNode;
 			      
 			      if (getTagValue("type", eElement).equals("administrative_area_level_1")) {
 			    	  state = getTagValue("short_name", eElement);
 			    	  
 			      } else if (getTagValue("type", eElement).equals("country")) {
 			    	  country = getTagValue("short_name", eElement);
 			      }
 			   }
 		   }
 	   
    	   } catch (ParserConfigurationException e) {
 		   e.printStackTrace();
 	   } catch (IOException e) {
 		   // internet is dead maybe?
 		   e.printStackTrace();
 	   } catch (SAXException e) {
 		   e.printStackTrace();
 	   }
 	   
 	   location = country + "." + state;
 	   return location;
    }
 	   
 	   // from here
 	   //http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
    private static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 	    Node nValue = (Node) nlList.item(0);
 		return nValue.getNodeValue();
 	  }
    
    /**
     * will prefill using the datastore
     * @param location
  * @throws  
     */
 	private void doPrefill(String location) {
 		location = location.toLowerCase();
 	
 		String output = "";
 		
 		try {
 			location = URLEncoder.encode(location, "UTF-8");
 	
 			URL url = new URL( URL+"/prefill" );
 			URLConnection connection = url.openConnection();
 			connection.setDoOutput(true);
 	
 			OutputStreamWriter out = new OutputStreamWriter(
 							 connection.getOutputStream());
 			out.write(location);
 			out.close();
 	
 			BufferedReader in = new BufferedReader(
 						    new InputStreamReader(
 						    connection.getInputStream()));
 			String response;
 			while ((response = in.readLine()) != null) {
 			    output+=response;
 			}
 			in.close();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		showPrefills(output);
 	}
 
 	// split up the string and display the information
 	private void showPrefills(String text) {
 
 		char[] chars = text.toCharArray(); 
 		int count = 0;
 		
 		for ( int i = 0; i < chars.length; i++ ) {
 			if ( chars[i] == ',' ) {
 				count++;
 			}
 		}
 
 		// if it's faulty, we'll change it to a complete unknown
 		if (count != 2) {
 			text = "?,?,?";
 		}
 
 		String splitText[] = text.split(",");
 		
 		// change ? to empty text (we didnt find anything in the database)
 		for (int i = 0; i < splitText.length; i++ ) {
 			if (splitText[i].equals("?")) {
 				splitText[i] = "";
 			}
 		}
 		
 		String avgCons = splitText[0];
 		String feedIn = splitText[1];
 		String elecCost = splitText[2];
 		
 		electricityCost.setText(elecCost);
 		tariffRates.setText(feedIn);
 		annualPower.setText(avgCons);
 	}
    
 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
 
        jPanel5 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        tabPanel = new javax.swing.JTabbedPane();
        homePanel = new javax.swing.JPanel();
        homeNextButton = new javax.swing.JButton();
        locationPanel = new javax.swing.JPanel();
        locationNextButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        costNextButton = new javax.swing.JButton();
        panelCostLabel = new java.awt.Label();
        panelCost = new java.awt.TextField();
        installationCostLabel = new java.awt.Label();
        installationCost = new java.awt.TextField();
        inverterCostLabel = new java.awt.Label();
        inverterCost = new java.awt.TextField();
        panelPanel = new javax.swing.JPanel();
        panelsNextButton = new javax.swing.JButton();
        numberPanelsLabel = new java.awt.Label();
        numberPanels = new java.awt.TextField();
        panelAngleLabel = new java.awt.Label();
        panelAngle = new java.awt.Choice();
        panelDirectionLabel = new java.awt.Label();
        panelDirection = new java.awt.Choice();
        panelWattageLabel = new java.awt.Label();
        panelWattage = new java.awt.TextField();
        sunlightHoursLabel = new java.awt.Label();
        sunlightHours = new java.awt.TextField();
        inverterEfficiencyLabel = new java.awt.Label();
        inverterEfficiency = new java.awt.TextField();
        powerPanel = new javax.swing.JPanel();
        panelCostLabel2 = new java.awt.Label();
        annualPower = new java.awt.TextField();
        tariffRateLabel = new java.awt.Label();
        tariffRates = new java.awt.TextField();
        electricityCostLabel = new java.awt.Label();
        electricityCost = new java.awt.TextField();
        calculateButton = new java.awt.Button();
        resultPanel = new javax.swing.JPanel();
        
        map = new JMapViewer();
       mapMarker = new MapMarkerDot(-27.4667, 153.0333); //Default to Brisbane
 
        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
 
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
 
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
        tabPanel.setName("tabPanel"); // NOI18N
 
        homeNextButton.setText("Next");
        homeNextButton.addActionListener(new ActionListener() {
     	   public void actionPerformed(ActionEvent arg0) {
                tabPanel.setSelectedIndex(tabPanel.getSelectedIndex()+1);
     	   }
       });
 
        javax.swing.GroupLayout homePanelLayout = new javax.swing.GroupLayout(homePanel);
        homePanel.setLayout(homePanelLayout);
        homePanelLayout.setHorizontalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(homeNextButton)
                .addContainerGap(383, Short.MAX_VALUE))
        );
        homePanelLayout.setVerticalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, homePanelLayout.createSequentialGroup()
                .addContainerGap(230, Short.MAX_VALUE)
                .addComponent(homeNextButton)
                .addGap(22, 22, 22))
        );
 
        tabPanel.addTab("Home", homePanel);
        homePanel.getAccessibleContext().setAccessibleName("");
        
 
        locationNextButton.setText("Next");
        locationNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabPanel.setSelectedIndex(tabPanel.getSelectedIndex()+1);
            }
        });
        
        JLabel lblRightClickAnd = new JLabel("Right click and drag to move the view, click to set location");
        lblRightClickAnd.setForeground(UIManager.getColor("Label.disabledForeground"));
        lblRightClickAnd.setFont(new Font("Dialog", Font.ITALIC, 12));
 
        javax.swing.GroupLayout locationPanelLayout = new javax.swing.GroupLayout(locationPanel);
        locationPanelLayout.setHorizontalGroup(
        	locationPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(locationPanelLayout.createSequentialGroup()
        			.addGap(24)
        			.addGroup(locationPanelLayout.createParallelGroup(Alignment.LEADING)
        				.addComponent(map, GroupLayout.DEFAULT_SIZE, 1237, Short.MAX_VALUE)
        				.addComponent(locationNextButton)
        				.addComponent(lblRightClickAnd, GroupLayout.PREFERRED_SIZE, 372, GroupLayout.PREFERRED_SIZE))
        			.addGap(24))
        );
        locationPanelLayout.setVerticalGroup(
        	locationPanelLayout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(locationPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(map, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(lblRightClickAnd)
        			.addGap(18)
        			.addComponent(locationNextButton)
        			.addContainerGap())
        );
        locationPanel.setLayout(locationPanelLayout);
        
       map.addMapMarker(mapMarker);
       doPrefill(findLocationName(mapMarker.getLat(), mapMarker.getLon()));
        map.addMouseListener(new MouseAdapter() {
     	   public void mouseClicked(MouseEvent e) {
     		   Coordinate coord = map.getPosition(e.getX(), e.getY());
     		   setLocation(coord.getLat(), coord.getLon());
     	   }
        });
        map.setDisplayPositionByLatLon(mapMarker.getLat(), mapMarker.getLon(), 4);
 
        tabPanel.addTab("Location", locationPanel);
        locationPanel.getAccessibleContext().setAccessibleName("");
 
        costNextButton.setText("Next");
        costNextButton.addActionListener(new ActionListener() {
     	   public void actionPerformed(ActionEvent arg0) {
                tabPanel.setSelectedIndex(tabPanel.getSelectedIndex()+1);
     	   }
       });
 
        panelCostLabel.setText("Enter the cost of each panel: ");
 
        installationCostLabel.setText("Enter the cost of installation: ");
 
        inverterCostLabel.setText("Enter the cost of the inverter: ");
 
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inverterCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inverterCost, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(installationCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(installationCost, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCost, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(costNextButton))
                .addContainerGap(278, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installationCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installationCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inverterCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inverterCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(costNextButton)
                .addGap(22, 22, 22))
        );
 
        tabPanel.addTab("Cost", jPanel3);
 
        panelsNextButton.setText("Next");
        panelsNextButton.addActionListener(new ActionListener() {
     	   public void actionPerformed(ActionEvent arg0) {
                tabPanel.setSelectedIndex(tabPanel.getSelectedIndex()+1);
     	   }
       });
        for(int i = 0; i <= 90; i += 5) {
            panelAngle.add(Integer.toString(i));
        }
        
        panelDirection.add("N");
        panelDirection.add("NE");
        panelDirection.add("E");
        panelDirection.add("SE");
        panelDirection.add("S");
        panelDirection.add("SW");
        panelDirection.add("W");
        panelDirection.add("NW");
 
        numberPanelsLabel.setText("Enter the number of panels:");
 
        panelAngleLabel.setText("Select tilt angle of solar panels");
 
        panelDirectionLabel.setText("Select direction of solar panels");
 
        panelWattageLabel.setText("Enter your panel wattage (Watts)");
 
        sunlightHoursLabel.setText("Enter the average hours of sunlight per day");
 
        inverterEfficiencyLabel.setText("Enter the efficiency of your inverter (%)");
 
        javax.swing.GroupLayout panelPanelLayout = new javax.swing.GroupLayout(panelPanel);
        panelPanel.setLayout(panelPanelLayout);
        panelPanelLayout.setHorizontalGroup(
            panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelDirectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(panelDirection, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panelsNextButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(19, 19, 19)
                        .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inverterEfficiency, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(inverterEfficiencyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(numberPanelsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(numberPanels, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelAngleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelAngle, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(35, 35, 35)
                        .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sunlightHours, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelWattageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelWattage, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sunlightHoursLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        panelPanelLayout.setVerticalGroup(
            panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addComponent(numberPanelsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberPanels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addComponent(panelWattageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelWattage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addComponent(panelAngleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addComponent(sunlightHoursLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sunlightHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addComponent(panelDirectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPanelLayout.createSequentialGroup()
                        .addComponent(inverterEfficiencyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inverterEfficiency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(panelsNextButton)
                .addGap(22, 22, 22))
        );
 
        tabPanel.addTab("Panels", panelPanel);
 
        panelCostLabel2.setText("Enter annual power consumption (kWh)");
 
        tariffRateLabel.setText("Enter feed-in tariff rates ($/kWh)");
 
        electricityCostLabel.setText("Enter the cost of your electricty ($/kWh)");
 
        calculateButton.setLabel("Calculate");
 
        javax.swing.GroupLayout powerPanelLayout = new javax.swing.GroupLayout(powerPanel);
        powerPanel.setLayout(powerPanelLayout);
        powerPanelLayout.setHorizontalGroup(
            powerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(powerPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(powerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(calculateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(electricityCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(electricityCost, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tariffRateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tariffRates, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCostLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(annualPower, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(219, Short.MAX_VALUE))
        );
        powerPanelLayout.setVerticalGroup(
            powerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(powerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelCostLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(annualPower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tariffRateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tariffRates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(electricityCostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(electricityCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(calculateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
 
        tabPanel.addTab("Power", powerPanel);
 
        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 464, Short.MAX_VALUE)
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
        );
 
        tabPanel.addTab("Result", resultPanel);
 
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanel)
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPanel, javax.swing.GroupLayout.Alignment.TRAILING)
        );
 
        pack();
    }// </editor-fold>                                                                         
 
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AsiDesktopGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AsiDesktopGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AsiDesktopGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AsiDesktopGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
 
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AsiDesktopGui().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify                     
    private java.awt.TextField annualPower;
    private java.awt.Button calculateButton;
    private javax.swing.JButton costNextButton;
    private java.awt.Label electricityCostLabel;
    private java.awt.TextField electricityCost;
    private javax.swing.JButton homeNextButton;
    private javax.swing.JPanel homePanel;
    private java.awt.TextField installationCost;
    private java.awt.Label installationCostLabel;
    private java.awt.TextField inverterCost;
    private java.awt.Label inverterCostLabel;
    private java.awt.TextField inverterEfficiency;
    private java.awt.Label inverterEfficiencyLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton locationNextButton;
    private javax.swing.JPanel locationPanel;
    private java.awt.TextField numberPanels;
    private java.awt.Label numberPanelsLabel;
    private java.awt.Choice panelAngle;
    private java.awt.Choice panelDirection;
    private java.awt.Label panelAngleLabel;
    private java.awt.TextField panelCost;
    private java.awt.Label panelCostLabel;
    private java.awt.Label panelCostLabel2;
    private java.awt.Label panelDirectionLabel;
    private javax.swing.JPanel panelPanel;
    private java.awt.TextField panelWattage;
    private java.awt.Label panelWattageLabel;
    private javax.swing.JButton panelsNextButton;
    private javax.swing.JPanel powerPanel;
    private javax.swing.JPanel resultPanel;
    private java.awt.TextField sunlightHours;
    private java.awt.Label sunlightHoursLabel;
    private javax.swing.JTabbedPane tabPanel;
    private java.awt.Label tariffRateLabel;
    private java.awt.TextField tariffRates;
    // End of variables declaration   
    private JMapViewer map;
    private MapMarker mapMarker;
 }
