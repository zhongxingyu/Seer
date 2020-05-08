 package gui.steps;
 import java.awt.BorderLayout;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 
 import javax.swing.WindowConstants;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.WindowConstants;
 
 import ontologyAndDB.OntToDbConnection;
 
 import model.steps.DistanceStepModel;
 import model.steps.InformationGatherStepModel;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.*;
 import model.steps.DistanceStepModel;
 import model.steps.InformationGatherStepModel;
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class DistanceStep extends AbstractViewModelConnectionImpl{
 	private JPanel tiltePnl;
 	private JPanel contentPnl;
 	private JButton jButton1;
 	private JLabel jLabel1;
 	private JTable jTable1;
 	private JTextPane hintTxt;
 	private JComboBox unitCbo;
 	private JTextField distanceTxt;
 	private JTextPane titleTxt;
 	private OntToDbConnection ontoconn;
 	private HashMap<String, Double> cityandDist;
 	private DefaultTableModel jTable1Model;
 
 	/**
 	* Auto-generated main method to display this 
 	* JPanel inside a new JFrame.
 	*/
 	public static void main(String[] args) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             	JFrame frame = new JFrame();
         		frame.getContentPane().add(new DistanceStep());
         		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         		frame.pack();
         		frame.setVisible(true);
             }
         });
 		
 	}
 	
 	public DistanceStep() {
 		super();
 		cityandDist = new HashMap<String, Double>();
		ontoconn = OntToDbConnection.getInstance();
 		initGUI();
 	}
 	
 	private void addToHash(String city, double dist){
 		cityandDist.put(city, new Double(dist));
 	}
 	
 	private void initGUI() {
 		try {
 			BorderLayout thisLayout = new BorderLayout();
 			this.setLayout(thisLayout);
 			this.setPreferredSize(new java.awt.Dimension(691, 416));
 			{
 				tiltePnl = new JPanel();
 				BorderLayout tiltePnlLayout = new BorderLayout();
 				tiltePnl.setLayout(tiltePnlLayout);
 				this.add(tiltePnl, BorderLayout.NORTH);
 				tiltePnl.setPreferredSize(new java.awt.Dimension(691, 51));
 				{
 					titleTxt = new JTextPane();
 					tiltePnl.add(titleTxt, BorderLayout.WEST);
 					titleTxt.setText("Wie weit darf das Event entfernt sein?");
 					titleTxt.setPreferredSize(new java.awt.Dimension(626, 58));
 					titleTxt.setBackground(new java.awt.Color(212,208,200));
 					titleTxt.setEditable(false);
 					titleTxt.setOpaque(false);
 					titleTxt.setFont(new java.awt.Font("Segoe UI",0,18));
 				}
 			}
 			{
 				contentPnl = new JPanel();
 				this.add(contentPnl, BorderLayout.CENTER);
 				contentPnl.setLayout(null);
 				contentPnl.setPreferredSize(new java.awt.Dimension(635, 277));
 				{
 					distanceTxt = new JTextField();
 					contentPnl.add(distanceTxt);
 					distanceTxt.setBounds(72, 25, 106, 22);
 				}
 				{
 					unitCbo = new JComboBox(new String[] { "km", "h" });
 					contentPnl.add(unitCbo);
 					unitCbo.setBounds(198, 24, 112, 22);
 				}
 				{
 					hintTxt = new JTextPane();
 					contentPnl.add(hintTxt);
 					hintTxt.setText("Hinweis: Ohne Auto kann sich die Reisezeit verzoegern!");
 					hintTxt.setBounds(12, 79, 391, 35);
 					hintTxt.setEditable(false);
 					hintTxt.setBackground(new java.awt.Color(255,43,52));
 				}
 				{
 					jButton1 = new JButton();
 					contentPnl.add(jButton1);
 					jButton1.setText("suchen");
 					jButton1.setBounds(331, 24, 100, 23);
 					jButton1.addActionListener(new SearchListener());
 				}
 				{
 					jLabel1 = new JLabel();
 					contentPnl.add(jLabel1);
 					jLabel1.setText("Ergebnis:");
 					jLabel1.setBounds(72, 72, 73, 16);
 
 				}
 				{
 					 jTable1Model = 
 							new DefaultTableModel(
 									new String[][] { { "Eine Stadt", "Die Entferung" } },
 									new String[] { "Stadt", "Entfernung" });
 					jTable1 = new JTable();
 					jTable1.setModel(jTable1Model);
 					jTable1.setAutoCreateRowSorter(true);
 					jTable1.setFillsViewportHeight(true);
 					JScrollPane scrollPane = new JScrollPane(jTable1);
 					scrollPane.setBounds(93, 111, 300, 150);
 					contentPnl.add(scrollPane);
 					
 					//jTable1.setBounds(93, 111, 94, 30);
 					//jTable1.setPreferredSize(new java.awt.Dimension(90, 73));
 				}
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public  ArrayList<String> getReachableCities(double wish_distance) throws Exception {
 		ArrayList<String> allcities = ontoconn.getCitiesFromDB();
 		ArrayList<String> reachablecities = new ArrayList<String>();
 		
 		for (int i=0; i<allcities.size(); i++){
 			String city = allcities.get(i).replace("[", "").replace("]", "").trim();
 			city = city.replaceAll("", "ue");
 			city = city.replaceAll("", "ae");
 			city = city.replaceAll("", "oe");
 			city = city.replaceAll("", "ss");
 			city = city.replaceAll(" ", "%20");
 			city = city.replaceAll("", "UE");
 			city = city.replaceAll("A", "AE");
 			city = city.replaceAll("", "OE");
 			double dist = getRouteDistance("Saarbruecken", city, "motorcar", "1");
 			
 			if (dist < wish_distance){
 				reachablecities.add(city);
 				addToHash(city, dist);
 				jTable1Model.addRow(new Object[]{city,dist});
 			}
 		}
 		return reachablecities;
 	}
 	
 		public static double[] getLatLon(String city) throws Exception {
 		double[] pos = new double[2];
 		URL citylat = new URL("http://nominatim.openstreetmap.org/search?q="
 				+ city + "&format=xml");
 		URLConnection con = citylat.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				con.getInputStream()));
 		String inputLine;
 		while ((inputLine = in.readLine()) != null){
 			if (inputLine.contains("lat='")){
 				int index = inputLine.indexOf("lat='");
 				String lat = inputLine.substring(index+5, inputLine.indexOf("'", index+5));
 				pos[0]=Double.parseDouble(lat);
 			}
 			if (inputLine.contains("lon='")){
 				int index = inputLine.indexOf("lon='");
 				String lon = inputLine.substring(index+5, inputLine.indexOf("'", index+5));
 				pos[1]=Double.parseDouble(lon);
 			}
 		}
 			
 		in.close();
 		return pos;
 	}
 	
 	public static double getDistance(String from, String to) throws Exception{
 		double[] pos1 = getLatLon(from);
 		double[] pos2 = getLatLon(to);
 		double dLon = Math.toRadians(pos2[1]-pos1[1]);
 		double dLat = Math.toRadians(pos2[0]-pos1[0]);
 		double lat1 = Math.toRadians(pos1[0]);
 		double lat2 = Math.toRadians(pos2[0]);
 		double a = Math.pow(Math.sin(dLat/2), 2)+ Math.pow(Math.sin(dLon/2), 2)* Math.cos(lat1) * Math.cos(lat2); 
 		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 		return 6371*c;
 	}
 
 	public static double getRouteDistance (String from, String to, String vehicle, String type) throws Exception {
 		double[] fpos = getLatLon(from);
 		double[] tpos = getLatLon(to);
 		URL dis = new URL("http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat="+fpos[0]+"&flon="+fpos[1]+"&tlat="+tpos[0]+"&tlon="+tpos[1]+"&v="+vehicle+"&fast="+type);
 		URLConnection con = dis.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
 		String inputLine;
 		while ((inputLine = in.readLine())!=null){
 			inputLine = inputLine.trim();
 			if (inputLine.startsWith("<distance>") && inputLine.endsWith("</distance>")){
                         	String value = inputLine.substring(10, inputLine.indexOf("</distance"));
 				in.close();
 				return Double.parseDouble(value);
 			}
 		}
 		in.close();
 		return 0;
 	}
 	
 	@Override
 	public void fillModel() {
 		DistanceStepModel model = DistanceStepModel.getInstance();
 		model.setDistance(distanceTxt.getText());
 		model.setUnit((String)unitCbo.getSelectedItem());
 	}
 
 	@Override
 	public InformationGatherStepModel getModel() {
 		// TODO Auto-generated method stub
 		return DistanceStepModel.getInstance();
 	}
 	
 	class SearchListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			System.out.println(distanceTxt.getText());
 			String input = distanceTxt.getText();
 			input = input.replace(',', '.');
 			final double dist;
 			try {
 				dist = Double.parseDouble(input);
 			} catch (Exception e){
 				JOptionPane.showMessageDialog(null, "Die Eingabe ist ungltig.", "Fehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			jTable1Model.getDataVector().clear();
 				Runnable run = new Runnable() {
 		            public void run() {
 		            	try {
 		            		System.out.println("run");
 							getReachableCities(dist);
 						} catch (Exception e) {
 							JOptionPane.showMessageDialog(null, "Bearbeitung fehlerhaft.", "Fehler", JOptionPane.ERROR_MESSAGE);
 							e.printStackTrace();
 						}
 		            }
 		        };
 			 
 			new Thread(run).start();
 		}
 		
 	}
 
 }
