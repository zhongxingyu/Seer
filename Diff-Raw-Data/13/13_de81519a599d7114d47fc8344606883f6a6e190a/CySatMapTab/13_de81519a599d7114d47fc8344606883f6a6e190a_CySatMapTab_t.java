 package gui;
 
 import gov.nasa.worldwind.BasicModel;
 import gov.nasa.worldwind.WorldWind;
 import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
 import gov.nasa.worldwind.geom.Position;
 import gov.nasa.worldwind.layers.RenderableLayer;
 import gov.nasa.worldwind.render.Polyline;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import orbits.CelestrakSatellite;
 import orbits.CommandSet;
 import orbits.SatellitePass;
 import serial.client.SerialClient;
 import uk.me.g4dpz.satellite.SatPassTime;
 import uk.me.g4dpz.satellite.SatPos;
 import util.SatelliteUtils;
 import worldwind.ClippingBasicOrbitView;
 import api.AntennaRotator;
 
 public class CySatMapTab extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private CommandSet cmdSet = null;
 	private Timer timer = new Timer();
 	private static final int timeStep = 5;
 	private static final boolean startTrackingImmediately = false;
 	private final int BASE_LAYERS;
 	private final CySatGUI gui;
 	private final WorldWindowGLCanvas worldWindCanvas;
 	private final JButton startTracking;
 	private final JButton connectButton;
 
 	private AntennaRotator rotator = null;
 	private SerialClient client = null;
 
 	public CySatMapTab(final CySatGUI gui) {
 		this.gui = gui;
 		setBackground(Color.RED);
 		setFocusable(false);
 
 		WorldWind.setOfflineMode(true);
 		try{
 			SatelliteUtils.init();
 		} catch(Exception e){
 			e.printStackTrace();
 			System.err.println("Unable to load satellites information! Exiting...");
 			System.exit(1);
 		}
 		// create a WorldWind main object
 		worldWindCanvas = new WorldWindowGLCanvas();
 		worldWindCanvas.setModel(new BasicModel());
 
 		BASE_LAYERS = worldWindCanvas.getModel().getLayers().size() - 17 * 1;
 		System.out.println(BASE_LAYERS);
 		while(worldWindCanvas.getModel().getLayers().size() > BASE_LAYERS - 1)
 			worldWindCanvas.getModel().getLayers().remove(BASE_LAYERS - 1);
 
 		addCompass(worldWindCanvas);
 		worldWindCanvas.setView(new ClippingBasicOrbitView());
 
 		setLayout(new BorderLayout());
 
 		final JPanel topPanel = new JPanel();
 		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
 		topPanel.setBackground(Color.BLACK);
 		
 		final JPanel topPanel1 = new JPanel();
 		topPanel1.setBackground(Color.BLACK);
 		
 		final JPanel topPanel2 = new JPanel();
 		topPanel2.setBackground(Color.BLACK);
 		
 		topPanel.add(topPanel1);
 		topPanel.add(topPanel2);
 
 		final JComboBox<CelestrakSatellite> satChooseBox = new JComboBox<CelestrakSatellite>();
 		for(CelestrakSatellite satellite : SatelliteUtils.getSatellites()){
 			satChooseBox.addItem(satellite);
 		}
 
 		JTextArea satLabelText = new JTextArea("    Choose Satellite:");
 		satLabelText.setBackground(null);
 		satLabelText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
 		satLabelText.setForeground(Color.WHITE);
 		satLabelText.setEditable(false);
 		satLabelText.setFocusable(false);
 
 
 		final JButton pathButton = new JButton("Load Path");
 
 		final JButton getPasses = new JButton("Get Passes");
 		getPasses.setEnabled(true);
 		getPasses.setFocusable(false);
 		getPasses.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				NextPassesView nextPassesView = gui.getNextPassesPanel().getNextPassesView();
 				nextPassesView.removeAllPasses();
 				CelestrakSatellite sat = (CelestrakSatellite) satChooseBox.getSelectedItem();
 				nextPassesView.showNextPasses(sat.getSatName(), 5);
 				gui.getNextPassesPanel().setSatTitle(sat.getSatName());
 				
 				nextPassesView.refresh();
 				loadSatPath();
 			}
 		});
 		
 		startTracking = new JButton("Begin Tracking");
 		startTracking.setEnabled(false);
 		startTracking.setFocusable(false);
 		startTracking.addActionListener(new ActionListener(){
 			private JTextArea status;
 			private JButton stop;
 			private JTextArea updateStatus;
 
 			private void finishTracking(){
 				startTracking.setEnabled(true);
 				satChooseBox.setEnabled(true);
 				pathButton.setEnabled(true);
				getPasses.setEnabled(true);
 
 				timer.cancel();
 				timer = new Timer();
 
 				topPanel2.remove(status);
 				topPanel2.remove(stop);
 				topPanel2.remove(updateStatus);
 				repaint();
 			}
 
 			@Override
 			public void actionPerformed(ActionEvent e){
 				if(rotator == null){
 					JOptionPane.showMessageDialog(null,
 							"You need to connect to the rotator first, or nothing will happen!");
 				}
 
 				if(cmdSet != null && cmdSet.getBase() != null){
 					status = new JTextArea("   Waiting for AOS...");
 					status.setBackground(null);
 					status.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
 					status.setForeground(new Color(0, 200, 0));
 					status.setEditable(false);
 					status.setFocusable(false);
 
 					updateStatus = new JTextArea("Next update in: 0 seconds");
 					updateStatus.setBackground(null);
 					updateStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
 					updateStatus.setForeground(Color.WHITE);
 					updateStatus.setEditable(false);
 					updateStatus.setFocusable(false);
 
 					stop = new JButton("Stop");
 					stop.setFocusable(false);
 					stop.addActionListener(new ActionListener(){
 						@Override
 						public void actionPerformed(ActionEvent e){
 							finishTracking();
 						}
 					});
 
 					startTracking.setEnabled(false);
 					satChooseBox.setEnabled(false);
 					pathButton.setEnabled(false);
					getPasses.setEnabled(false);

 
 					topPanel2.add(stop);
 					topPanel2.add(status);
 					topPanel2.add(updateStatus);
 					repaint();
 
 //					System.out.println(new SimpleDateFormat("hh:mm:ss").format(new Date().getTime()));
 //					System.out.println(new SimpleDateFormat("hh:mm:ss").format(cmdSet.base));
 //					System.out.println(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(cmdSet.base));
 					if (startTrackingImmediately) {
 						cmdSet.base = new Date();
 					}
 					timer.schedule(new TimerTask(){
 						private Iterator<String> iter;
 						private int count = 0;
 						private boolean started = false;
 						private int offset = 0;
 
 						@Override
 						public synchronized void run(){
 							if(iter == null)
 								iter = cmdSet.iterator();
 
 							if(!iter.hasNext()){
 								finishTracking();
 								return;
 							}
 
 							if(started){
 								if(++count == cmdSet.getTimeStep()){
 									count = 0;
 
 									String cmd = iter.next();
 									issueCommand(cmd);
 								}
 							} else{
 								Date now = new Date();
 								if(cmdSet.getBase().before(now)){
 									started = true;
 									offset = 0;
 									count = 0;
 
 									String cmd = iter.next();
 									issueCommand(cmd);
 								} else{
 									offset = (int) ((cmdSet.getBase().getTime() - now.getTime()) / 1000)
 											- cmdSet.getTimeStep() + 1;
 								}
 							}
 
 							String text = "   Next update in: " + (offset + cmdSet.getTimeStep() - count)
 									+ " seconds";
 							updateStatus.setText(text);
 							CurrentStatePanel.up2.setText(text);
 //							System.out.println(text);
 						}
 
 						private void issueCommand(String cmd){
 							String text = "   Az: " + cmd.substring(0, 3) + "; El: " + cmd.substring(4);
 							status.setText(text);
 							CurrentStatePanel.status2.setText(text);
 							System.out.println(text);
 							
 							try{
 								if(rotator != null)
 									rotator.RotatorSet(Integer.parseInt(cmd.substring(0, 3)),
 											Integer.parseInt(cmd.substring(4)));
 							} catch(NumberFormatException | IOException e){
 								e.printStackTrace();
 							}
 						}
 					}, 0, 1000);
 				}
 			}
 		});
 
 		pathButton.setFocusable(false);
 		pathButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				loadSatPath();
 			}
 		});
 
 		satChooseBox.setEditable(false);
 		satChooseBox.setFocusable(false);
 		satChooseBox.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				startTracking.setEnabled(false);
 			}
 		});
 
 		connectButton = new JButton("Connect to serial server");
 		connectButton.setFocusable(false);
 		connectButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				connectToServer();
 			}
 		});
 
 		topPanel1.add(connectButton);
 
 		topPanel1.add(satLabelText);
 		topPanel1.add(satChooseBox);
 		topPanel1.add(getPasses);
 		
 		topPanel2.add(pathButton);
 		topPanel2.add(startTracking);
 
 		add(worldWindCanvas);
 		add(topPanel, BorderLayout.NORTH);
 	}
 
 	private void addCompass(WorldWindowGLCanvas worldWindCanvas){
 		ArrayList<Position> positions = new ArrayList<Position>();
 		for(int i = -6533000; i < 1000000; i += 100000){
 			positions.add(Position.fromDegrees(0, 0, i));
 		}
 
 		//Add western hemisphere horizon line
 		for(int i=0; i <= 180; i += 5) {
 			positions.add(Position.fromDegrees(0, i, 1000000));
 		}
 		
 		// create "Polyline" with list of "Position" and set color / thickness
 		Polyline polyline = new Polyline(positions);
 		polyline.setColor(Color.BLUE);
 		polyline.setLineWidth(4.0);
 
 		// create a layer and add Polyline
 		RenderableLayer layer = new RenderableLayer();
 		layer.addRenderable(polyline);
 
 		// add layer to WorldWind
 		worldWindCanvas.getModel().getLayers().add(layer);
 	}
 
 	private void addPath(WorldWindowGLCanvas worldWindCanvas, List<SatPos> list){
 		List<Position> positions = new ArrayList<Position>();
 		List<Position> belowMinElevAsc = new ArrayList<Position>();
 		List<Position> belowMinElevDesc = new ArrayList<Position>();
 		boolean rising = true;
 		for(SatPos satPos : list){
 			if (satPos.getElevation() < SatelliteUtils.MIN_ELEV) {
 				if (rising) {
 					belowMinElevAsc.add(Position.fromDegrees(satPos.getElevation(), -satPos.getAzimuth(), 1000000));
 				} else {
 					belowMinElevDesc.add(Position.fromDegrees(satPos.getElevation(), -satPos.getAzimuth(), 1000000));
 				}
 			} else {
 				rising = false;
 				positions.add(Position.fromDegrees(satPos.getElevation(), -satPos.getAzimuth(), 1000000));
 			}
 		}
 		
 		belowMinElevAsc.add(positions.get(0));
 		belowMinElevDesc.add(0, positions.get(positions.size() - 1));
 		
 		//Layer to display the lines
 		RenderableLayer layer = new RenderableLayer();
 		
 		// create "Polyline" with list of "Position" and set color / thickness
 		Polyline polyline = new Polyline(positions.subList(0, positions.size() / 2 + 1));
 		polyline.setColor(Color.RED);
 		polyline.setLineWidth(4.0);
 		layer.addRenderable(polyline);
 
 		// create "Polyline" with list of "Position" and set color / thickness
 		polyline = new Polyline(positions.subList(positions.size() / 2, positions.size()));
 		polyline.setColor(Color.GREEN);
 		polyline.setLineWidth(4.0);
 		layer.addRenderable(polyline);
 		
 		// line for the ascending invisible areas
 		polyline = new Polyline(belowMinElevAsc);
 		polyline.setColor(new Color(25, 25, 25));
 		polyline.setLineWidth(4.0);
 		layer.addRenderable(polyline);
 		
 		// line for the descending invisible areas
 		polyline = new Polyline(belowMinElevDesc);
 		polyline.setColor(new Color(25, 25, 25));
 		polyline.setLineWidth(4.0);
 		layer.addRenderable(polyline);
 
 		// add layer to WorldWind
 		worldWindCanvas.getModel().getLayers().add(layer);
 	}
 	
 	private void loadSatPath() {
 		try{
 			SatellitePass satPass = gui.getNextPassesPanel().getNextPassesView().getSelectedPass();
 			if (satPass != null) {
 				CelestrakSatellite satellite = satPass.getSatellite();
 				SatPassTime spt = satPass.getSatPassTime();
 				
 				System.out.println();
 				System.out.println("For satellite: " + satellite.getSatName() + " @"
 						+ satellite.getInfoPage().getURL());
 				System.out.println();
 				System.out.println(spt);
 
 				while(worldWindCanvas.getModel().getLayers().size() > BASE_LAYERS)
 					worldWindCanvas.getModel().getLayers().remove(BASE_LAYERS);
 
 				worldWindCanvas.setView(new ClippingBasicOrbitView());
 
 				List<SatPos> passPoints = satPass.getPassPoints(SatelliteUtils.MIN_ELEV);
 				cmdSet = SatelliteUtils.getRotatorCommandSet(passPoints, passPoints.get(0).getTime().getTime(), timeStep);
 				addPath(worldWindCanvas, satPass.getPassPoints());
 
 				startTracking.setEnabled(true);
 			} else {
 				JOptionPane.showMessageDialog(null, "No pass is currently selected!");
 			}
 		} catch(Exception e2){
 			e2.printStackTrace();
 			System.err.println("Unable to track satellite path! Exiting...");
 			System.exit(1);
 		}
 	}
 	
 	private void connectToServer() {
 		String serverIP = JOptionPane.showInputDialog(null, new LabelText("Enter server IP: "),
 				"penthouse.aere.iastate.edu");
 		if(serverIP != null){
 			String portNum = JOptionPane.showInputDialog(null, new LabelText("Enter port number: "), "2809");
 			if(portNum != null && new Scanner(portNum).hasNextInt()){
 				String username = JOptionPane.showInputDialog(null, new LabelText("Enter username: "), "joe");
 				if(username != null){
 					String password = JOptionPane.showInputDialog(null, new LabelText("Enter password: "),
 							"password23");
 					if(password != null){
 						client = new SerialClient(serverIP, Integer.parseInt(portNum), username, password);
 						if(client.getState() == SerialClient.State.ALIVE){
 							rotator = new AntennaRotator(client, 0);
 							JOptionPane.showMessageDialog(null, "Success! Server connection established :D");
 							connectButton.setEnabled(false);
 						} else if(client.getState() == SerialClient.State.DEAD){
 							JOptionPane.showMessageDialog(null, "Unable to connect to " + serverIP + ":"
 									+ portNum);
 						} else if(client.getState() == SerialClient.State.INVALID_PASSWORD){
 							JOptionPane.showMessageDialog(null, "Invalid username or password!");
 						}
 					}
 				}
 			} else if(portNum != null){
 				JOptionPane.showMessageDialog(null, "Invalid port number: " + portNum);
 			}
 		}
 	}
 }
