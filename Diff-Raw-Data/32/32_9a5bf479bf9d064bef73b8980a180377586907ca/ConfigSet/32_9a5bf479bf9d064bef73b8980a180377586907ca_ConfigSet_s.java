 package org.monospace.configcreator;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 import java.util.Vector;
 
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.monospace.configcreator.ConfigComponent.EditEvent;
 
 public class ConfigSet {
 	public class ConfigSelectElement extends ConfigElement {
 		public class ConfigSelectComponent extends ConfigComponent {
 			private JComboBox<ConfigOption> comboBox;
 			public ConfigSelectComponent(String name, Vector<ConfigOption> options) {
 				super(name);
 				comboBox = new JComboBox<ConfigOption>(options);
 				comboBox.addItemListener(new ItemListener() {
 					@Override
 					public void itemStateChanged(ItemEvent e) {
 						if (e.getStateChange() == ItemEvent.SELECTED) {
 							invokeEditListeners(new EditEvent(((ConfigOption) e.getItem()).getValue()));
 						}
 					}
 				});
 				input = comboBox;
 			}
 			@Override
 			public void setValue(String value) {
 				int index = options.indexOf(value);
 				if (index >= 0) {
 					comboBox.setSelectedIndex(index);
 				} else {
 					comboBox.setSelectedIndex(0);
 				}
 				comboBox.setVisible(true);
 			}
 			@Override
 			public String getValue() {
 				return ((ConfigOption) comboBox.getSelectedItem()).getValue();
 			}
 		}
 		public class ConfigOption implements Comparable<ConfigOption> {
 			private String value;
 			private String desc;
 			public ConfigOption(String value, String desc) {
 				this.value = value;
 				this.desc = desc;
 			}
 			@Override
 			public int compareTo(ConfigOption o) {
 				return value.compareTo(o.getValue());
 			}
 			@Override
 			public String toString() {
 				return desc;
 			}
 			@Override
 			public boolean equals(Object o) {
 				if (o instanceof ConfigOption) {
 					return ((ConfigOption) o).getValue().equals(value);
 				}
 				if (o instanceof String) {
 					return value.equals(o);
 				}
 				return false;
 			}
 			public String getValue() {
 				return value;
 			}
 			public void setValue(String value) {
 				this.value = value;
 			}
 			public String getDesc() {
 				return desc;
 			}
 			public void setDesc(String desc) {
 				this.desc = desc;
 			}
 		}
 		private Vector<ConfigOption> options;
 		
 		public ConfigSelectElement(String key, String desc, int priority) {
 			super(key, desc, priority);
 			this.options = new Vector<ConfigOption>();
 			setComponent(new ConfigSelectComponent(getDescription(), options));
 		}
 		@Override
 		public boolean isValid() {
 			if (super.isValid()) {
 				for (int i = 0; i < options.size(); i++) {
 					if (options.get(i).equals(getValue())) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 		public void addOption(String value, String desc) {
 			options.add(new ConfigOption(value, desc));
 		}
 	}
 	public class ConfigTextElement extends ConfigElement {
 		public class ConfigTextComponent extends ConfigComponent {
 			private JTextField textField;
 			public ConfigTextComponent(String name) {
 				super(name);
 				textField = new JTextField();
 				textField.setColumns(18);
 				textField.getDocument().addDocumentListener(new DocumentListener() {
 					@Override
 					public void removeUpdate(DocumentEvent e) {
 						invokeEditListeners(new EditEvent(textField.getText().trim()));
 					}
 					@Override
 					public void insertUpdate(DocumentEvent e) {
 						invokeEditListeners(new EditEvent(textField.getText().trim()));
 					}
 					@Override
 					public void changedUpdate(DocumentEvent e) {
 						invokeEditListeners(new EditEvent(textField.getText().trim()));
 					}
 				});
 				input = textField;
 			}
 			@Override
 			public void setValue(String value) {
 				textField.setText(value);
 				textField.setVisible(true);
 			}
 			@Override
 			public String getValue() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		}
 		public ConfigTextElement(String key, String desc, int priority) {
 			super(key, desc, priority);
 			setComponent(new ConfigTextComponent(getDescription()));
 		}
 		@Override
 		public boolean isValid() {
 			return super.isValid();
 		}
 	}
 	public class ConfigIPAddrElement extends ConfigTextElement {
 	    private static final String IPADDRESS_PATTERN = 
 	    		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 		public ConfigIPAddrElement(String key, String desc, int priority) {
 			super(key, desc, priority);
 		}
 		@Override
 		public boolean isValid() {
 			return super.isValid() && getValue().matches(IPADDRESS_PATTERN);
 		}
 	}
 	
 	private ArrayList<ConfigElement> list;
 	private boolean modified;
 	private ConfigComponent.EditListener listener;
 	
 	public ConfigSet() {
 		list = new ArrayList<ConfigElement>();
 		modified = false;
 	}
 	public void parseTemplate(String source) throws RuntimeException {
 		ArrayList<ConfigElement> templist = new ArrayList<ConfigElement>();
 		try {
 			JSONArray json = new JSONArray(source);
 			for (int i = 0; i < json.length(); i++) {
 				JSONObject element = json.getJSONObject(i);
 				String key = element.getString("key");
 				String desc = key;
 				int priority = 0;
 				if (element.has("priority")) {
 					priority = element.getInt("priority");
 				}
 				if (element.has("desc")) {
 					desc = element.getString("desc");
 				}
 				ConfigElement conf = null;
 				switch (element.getString("type")) {
 				case "text":
 					conf = new ConfigTextElement(key, desc, priority);
 					break;
 				case "ipaddr":
 					conf = new ConfigIPAddrElement(key, desc, priority);
 					break;
 				case "select":
 					JSONArray options = element.getJSONArray("options");
 					ConfigSelectElement confref = new ConfigSelectElement(key, desc, priority);
 					for (int j = 0; j < options.length(); j++) {
 						JSONObject option = options.getJSONObject(j);
 						confref.addOption(option.getString("value"), option.getString("desc"));
 						conf = confref;
 					}
 					break;
 				default:
 					throw new JSONException("");
 				}
 				conf.addEditListener(new ConfigComponent.EditListener() {
 					@Override
 					public void contentChanged(EditEvent e) {
 						modified = true;
 						if (listener != null) {
 							listener.contentChanged(null);
 						}
 					}
 				});
 				templist.add(conf);
 			}
 		} catch (JSONException e) {
 			throw new RuntimeException("Error parsing source");
 		}
 		list = templist;
 	}
	public void parseTemplate(File file) throws IOException, RuntimeException {
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		String src = new String(b);
 		parseTemplate(src);
 	}
 	public ConfigElement get(int i) {
 		return list.get(i);
 	}
 	public int size() {
 		return list.size();
 	}
 	public ConfigComponent getComponent(int i) {
 		return list.get(i).getComponent();
 	}
 	public void loadFromFile(File file) throws IOException, RuntimeException {
 		Scanner scanner = new Scanner(file);
 		clearValue();
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (line.length() == 0 || line.charAt(0) == '#' || line.trim().length() == 0) {
 				continue;
 			}
 			String[] segs = line.split("=", 2);
 			if (segs.length != 2) {
 				continue;
 			}
 			String key = segs[0].trim().toUpperCase();
 			String value = segs[1].trim();
 			if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
 				value = value.substring(1, value.length() - 1);
 			}
 			int index = -1;
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i).equals(key)) {
 					index = i;
 					break;
 				}
 			} 
 			if (index < 0) {
 				System.err.print("Invalid: ");
 				System.err.println(line);
 				continue;
 			}
 			list.get(index).setValue(value, true);
 		}
 		scanner.close();
 	}
 	public void writeToFile(File file) throws IOException {
 		Collections.sort(list);
 		PrintWriter writer = new PrintWriter(file);
 		for (int i = 0; i < list.size(); i++) {
 			writer.println(list.get(i));
 		}
 		writer.close();
 	}
 	public void clearValue() {
 		for (int i = 0; i < list.size(); i++) {
 			list.get(i).clearValue();
 		}
 	}
 	public boolean isModified() {
 		return modified;
 	}
 	public void setModified(boolean m) {
 		modified = m;
 	}
 	public void setListener(ConfigComponent.EditListener listener) {
 		this.listener = listener;
 	}
 }
