 package gui;
 
 import gui.listeners.DimmerChangeListener;
 import gui.listeners.SwitchActionListener;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JToggleButton;
 import javax.swing.SpinnerListModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeListener;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import classes.Config;
 import classes.config.Device;
 import classes.config.Location;
 
 public class MainDlg {
 
 	private JFrame frame;	
 
 	private static final Insets WEST_INSETS = new Insets(5, 0, 5, 5);
 	private static final Insets EAST_INSETS = new Insets(5, 5, 5, 0);
 	
 	private static int y = 0;
 	private static int posX = 0;
 	private static int posY = 0;
 
 	private Runnable funcSettings = null;
 	private HashMap componentMap = new HashMap<String,Component>();
 	private Map<String, Long> decimals = new HashMap<String, Long>();
 	
 	public static void main(String[] args) {
 	}
 		
 	public void show() {	
 		frame.setVisible(true);
 	}
 	
 	public void create() {
 		frame = new JFrame();
 		frame.setTitle("pilight");
 		frame.setBounds(0, 0, 240, 320);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.addWindowListener(new WindowListener() {
 			public void windowOpened(WindowEvent e) { };
 			public void windowClosing(WindowEvent e) { };
 			public void windowDeiconified(WindowEvent e) { };
 			public void windowIconified(WindowEvent e) { };
 			public void windowDeactivated(WindowEvent e) { };
 			public void windowActivated(WindowEvent e) { };
 			public void windowClosed(WindowEvent e) {
 				System.exit(0);
 			};
 		});
 		frame.setLayout(new BorderLayout());
 		frame.setLocationRelativeTo(null);
 		frame.addMouseListener(new MouseAdapter() {
 		   public void mousePressed(MouseEvent e) {
 		      posX=e.getX();
 		      posY=e.getY();
 		   }
 		});
 		frame.addMouseMotionListener(new MouseAdapter() {
 		     public void mouseDragged(MouseEvent evt) {		
 				frame.setLocation(evt.getXOnScreen()-posX,evt.getYOnScreen()-posY);
 							
 		     }
 		});
 		
 //		JMenuBar jmb = new JMenuBar();
 //		JMenu jmSettings = new JMenu("Settings");
 //		jmSettings.addMenuListener(settingsAction);
 //		jmb.add(jmSettings);
 //		frame.setJMenuBar(jmb);
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 		tabbedPane.setTabPlacement(JTabbedPane.TOP);
 		JPanel panel = null;
 		boolean state = false;
 
 		for(Map.Entry<String, Location> lentry : Config.getConfig().entrySet()) {
 
 			Location location = (Location)lentry.getValue();
 
 			panel = new JPanel();
 			panel.setLayout(new GridBagLayout());
 			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
 			for(Map.Entry<String, Device> dentry : location.getDevices().entrySet()) {
 
 				Device device = (Device)dentry.getValue();
 
 				switch(Integer.parseInt(device.getSettings().get("type").get(0))){
 					case 4:
 					case 1:
 						createSwitchElement(panel, lentry.getKey().toString(), dentry.getKey().toString(), device);
 					break;
 					case 2:
 						createDimmerElement(panel, lentry.getKey().toString(), dentry.getKey().toString(), device);
 					break;
 					case 3:
 						state = true;
 						createWeatherElement(panel, lentry.getKey().toString(), dentry.getKey().toString(), device);
 					break;
 				}
 			}
 			createFill(panel);
 			tabbedPane.addTab(location.getName(), new JScrollPane(panel));	
 		}		
 		frame.add(tabbedPane);
 	}
 	
 //	private MenuListener settingsAction = new MenuListener() {
 //		public void menuSelected(MenuEvent e) {
 //			onSettings();
 //		}
 //		public void menuDeselected(MenuEvent e) { };
 //		public void menuCanceled(MenuEvent e) { };
 //	};
 //	
 //	public void setOnSettings(Runnable func) {
 //		this.funcSettings = func;
 //	}
 //	
 //	public void onSettings() {
 //		this.funcSettings.run();
 //	}		
 	
 	public void close() {
 		frame.dispose();
 	}
 	
 	public void hide() {
 		frame.setVisible(false);
 		frame.toFront();
 		frame.repaint();
 	}
 	
 	public void disable() {
 		frame.setEnabled(false);
 	}
 	
 	public void enable() {
 		frame.setEnabled(true);
 		frame.toFront();
 		frame.repaint();
 	}
 	
 	public boolean enabled() {
 		return (frame != null && frame.isEnabled() && frame.isVisible());
 	}
 	
 	public void update(JSONObject json) {
 		if(json.has("values") && json.has("origin") && json.has("devices") && json.has("type")) {
 			if(new String("config").equals(json.getString("origin"))) {
 				JSONObject values = json.optJSONObject("values");
 				int type = json.getInt("type");
 				JSONObject locations = json.optJSONObject("devices");
 				
 				Iterator<?> lit = locations.keys();
 				while(lit.hasNext()) {
 					String location = (String)lit.next().toString();
 					JSONArray devices = (JSONArray)locations.optJSONArray(location);
 
 					for(Short i=0; i<devices.length(); i++) {
 						String device = (String)devices.get(i).toString();
 					
 						Iterator<?> vit;
 						switch(type) {
 							case 4:
 							case 1:
								JToggleButton button = (JToggleButton)getComponentByName(location, device);
 								vit = values.keys();
 								while(vit.hasNext()) {
 									String key = (String)vit.next().toString();
 									if(new String("state").equals(key)) {
 										ActionListener btmp = button.getActionListeners()[0];
 										button.removeActionListener(btmp);
 										if(new String("off").equals(values.getString(key))) {
 											button.setSelected(false);
 											button.setText("Off");
 										} else {
 											button.setSelected(true);
 											button.setText("On");
 										}
 										button.addActionListener(btmp);
 									}
 								}
 							break;
 							case 2:
 								JSpinner spinner = (JSpinner)getComponentByName(location, device+"_spinner");
 								vit = values.keys();
 								while(vit.hasNext()) {
 									String key = (String)vit.next().toString();
 									if(new String("dimlevel").equals(key)) {
 										ChangeListener stmp = spinner.getChangeListeners()[0];
 										spinner.removeChangeListener(stmp);
 										spinner.setValue(values.getString(key));
 										spinner.addChangeListener(stmp);								
 									}
 								}
 								JToggleButton button1 = (JToggleButton)getComponentByName(location, device+"_button");
 								vit = values.keys();
 								while(vit.hasNext()) {
 									String key = (String)vit.next().toString();
 									if(new String("state").equals(key)) {
 										ActionListener btmp = button1.getActionListeners()[0];
 										button1.removeActionListener(btmp);
 										if(new String("off").equals(values.getString(key))) {
 											button1.setSelected(false);
 											button1.setText("Off");
 										} else {
 											button1.setSelected(true);
 											button1.setText("On");
 										}
 										button1.addActionListener(btmp);
 									}
 								}
 							break;
 							case 3:
 								JLabel lblTemp = (JLabel)getComponentByName(location, device+"_temp");
 								JLabel lblHumi = (JLabel)getComponentByName(location, device+"_humi");
 								JLabel lblBatt = (JLabel)getComponentByName(location, device+"_batt");
 
 								if(values.optLong("temperature", -999) != -999) {
 									try {
 										float temperature = 0;
 										if(decimals.get(location+'_'+device) > 0) {
 											temperature = Float.parseFloat(new String().valueOf(values.getLong("temperature")));
 											temperature /= Math.pow(10, decimals.get(location+'_'+device));
 											lblTemp.setText(new String().valueOf(temperature));
 										} else {
 											lblTemp.setText(new String().valueOf((int)values.getLong("temperature")));
 										}
 									} catch(NullPointerException e) {
 									}
 								}
 								if(values.optLong("humidity", -999) != -999) {
 									try {
 										float humidity = 0;
 										if(decimals.get(location+'_'+device) > 0) {
 											humidity /= Math.pow(10, decimals.get(location+'_'+device));
 											lblHumi.setText(new String().valueOf(humidity));
 										} else {
 											lblHumi.setText(new String().valueOf((int)values.getLong("humidity")));
 										}
 									} catch(NullPointerException e) {
 									}
 								}
 								if(values.optLong("battery", -1) != -1) {
 									try {
 										lblBatt.setText(new String().valueOf(values.getLong("battery")));
 									} catch(NullPointerException e) {
 									}
 								}
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void createSwitchElement(JPanel panel, String lid, String did, Device device) {
 		String state = "Off";
 		if(device.getSettings().get("state").get(0).toString().equals("on")) {
 			state = "On ";
 		}
 		
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = y;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.insets = WEST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.weightx = 1.0;
 		JLabel label = new JLabel(device.getName()+":", JLabel.LEFT);
 		panel.add(label, gbc);
 
 		gbc.gridx = 1;
 		gbc.gridy = y;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.insets = EAST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.weightx = 1;
 		JToggleButton button = new JToggleButton(state);
 
 		JSONObject json = new JSONObject(device.getSettings().get("settings").get(0));
 		if(json.getLong("readonly") == 1) {
 			button.setEnabled(false);
 		}
 		
 		button.setSelected(device.getSettings().get("state").get(0).toString().equals("on"));
 		button.addActionListener(new SwitchActionListener(lid, did, device.getSettings().get("state").get(0).toString().equals("on")));
 		panel.add(button, gbc);
		registerComponent(button, lid, did);
 
 		y++;
 	}
 	
 	private void createDimmerElement(JPanel panel, String lid, String did, Device device) {
 		String state = "Off";
 		if(device.getSettings().get("state").get(0).toString().equals("on")) {
 			state = "On ";
 		}
 
 		JSONObject json = new JSONObject(device.getSettings().get("settings").get(0));
 
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = y;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.insets = WEST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.weightx = 1.0;
 		JLabel label = new JLabel(device.getName()+":", JLabel.LEFT);
 		panel.add(label, gbc);
 
 		gbc.gridx = 1;
 		gbc.gridy = y;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.insets = EAST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.weightx = 1;
 		JToggleButton button = new JToggleButton(state);
 
 		if(json.getLong("readonly") == 1) {
 			button.setEnabled(false);
 		}
 
 		button.setSelected(device.getSettings().get("state").get(0).toString().equals("on"));
 		button.addActionListener(new SwitchActionListener(lid, did, device.getSettings().get("state").get(0).toString().equals("on")));
 		panel.add(button, gbc);
 		registerComponent(button, lid, did+"_button");
 
 		y++;
 
 		ArrayList<String> values = new ArrayList<String>(); 
 		for(int i=(int)json.getLong("min");i<=(int)json.getLong("max");i++) {
 			values.add(new String().valueOf(i));
 		}

 		gbc.gridx = 1;
 		gbc.gridy = y;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.insets = EAST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.weightx = 0;
 		SpinnerListModel list = new SpinnerListModel(values);
 		JSpinner spinner = new JSpinner(list);
 		
 		if(json.getLong("readonly") == 1) {
 			spinner.setEnabled(false);
 		}		
 		
 		spinner.setValue(device.getSettings().get("dimlevel").get(0).toString());
 		spinner.addChangeListener(new DimmerChangeListener(lid, did));
 		panel.add(spinner, gbc);		
 		registerComponent(spinner, lid, did+"_spinner");
 		y++;		
 	}
 	
 	private void createWeatherElement(JPanel panel, String lid, String did, Device device) {
 
 		JSONObject json = new JSONObject(device.getSettings().get("settings").get(0));
 
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = y;
 		gbc.gridwidth = 2;
 		gbc.gridheight = 1;
 		gbc.insets = WEST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.weightx = 1.0;
 		JLabel label = new JLabel(device.getName(), JLabel.LEFT);
 		panel.add(label, gbc);
 		y++;
 
 		decimals.put(lid+'_'+did, json.getLong("decimals"));
 		
 		if(json.getLong("temperature") == 1) {
 			float temperature = 0;
 			if(json.getLong("decimals") > 0) {
 				temperature = Float.parseFloat(device.getSettings().get("temperature").get(0));
 				temperature /= Math.pow(10, json.getLong("decimals"));
 			}
 		
 			gbc.gridx = 0;
 			gbc.gridy = y;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			gbc.insets = WEST_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.LINE_START;
 			gbc.weightx = 1.0;
 			JLabel label1 = new JLabel("- Temparature:", JLabel.LEFT);
 			panel.add(label1, gbc);
 			
 			gbc.gridx = 1;
 			gbc.gridy = y;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			gbc.insets = EAST_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.LINE_END;
 			gbc.weightx = 1;
 			JLabel temp;
 			if(json.getLong("decimals") > 0) {
 				temp = new JLabel(new String().valueOf(temperature), JLabel.RIGHT);
 			} else {
 				temp = new JLabel(new String().valueOf(device.getSettings().get("temperature").get(0)), JLabel.RIGHT);
 			}
 			temp.setHorizontalAlignment(SwingConstants.RIGHT);
 			panel.add(temp, gbc);	
 			registerComponent(temp, lid, did+"_temp");
 			y++;
 		}
 
 		if(json.getLong("humidity") == 1) {
 			float humidity = 0;
 			if(json.getLong("decimals") > 0) {
 				humidity = Float.parseFloat(device.getSettings().get("humidity").get(0));
 				humidity /= Math.pow(10, json.getLong("decimals"));
 			}			
 			
 			gbc.gridx = 0;
 			gbc.gridy = y;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			gbc.insets = WEST_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.LINE_START;
 			gbc.weightx = 1.0;
 			JLabel fill = new JLabel("- Humidity:", JLabel.LEFT);
 			panel.add(fill, gbc);	
 			
 			gbc.gridx = 1;
 			gbc.gridy = y;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			gbc.insets = EAST_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.LINE_END;
 			gbc.weightx = 1;
 			JLabel humi;
 			if(json.getLong("decimals") > 0) {
 				humi = new JLabel(new String().valueOf(humidity), JLabel.RIGHT);
 			} else {
 				humi = new JLabel(new String().valueOf(device.getSettings().get("humidity").get(0)), JLabel.RIGHT);
 			}
 			humi.setHorizontalAlignment(SwingConstants.RIGHT);
 			panel.add(humi, gbc);
 			registerComponent(humi, lid, did+"_humi");
 			y++;
 		}
 		
 		if(json.getLong("battery") == 1) {
 			gbc.gridx = 0;
 			gbc.gridy = y;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			gbc.insets = WEST_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.LINE_START;
 			gbc.weightx = 1.0;
 			JLabel fill1 = new JLabel("- Battery:", JLabel.LEFT);
 			panel.add(fill1, gbc);
 			
 			gbc.gridx = 1;
 			gbc.gridy = y;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			gbc.insets = EAST_INSETS;
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.LINE_END;
 			gbc.weightx = 1;
 			JLabel batt = new JLabel(device.getSettings().get("battery").get(0).toString(), JLabel.RIGHT);
 			batt.setHorizontalAlignment(SwingConstants.RIGHT);
 			panel.add(batt, gbc);		
 			registerComponent(batt, lid, did+"_batt");		
 			y++;
 		}
 	}	
 	
 	private void createFill(JPanel panel) {
 		JPanel fill = new JPanel();
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = y;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.insets = WEST_INSETS;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.weighty = 1.0;
 		panel.add(fill, gbc);
 		y++;
 	}
 
 	private void registerComponent(Component component, String location, String id) {
 		if(!componentMap.containsKey(location+"_"+id)) {
 			componentMap.put(location+"_"+id, component);
 		}
 	}
 
 	public Component getComponentByName(String location, String id) {
 	    if(componentMap.containsKey(location+"_"+id)) {
 	    	return (Component)componentMap.get(location+"_"+id);
 	    }
 	    else 
 	    	return null;
 	}
 }
