 package editor;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import linewars.display.IconConfiguration;
 import linewars.display.IconConfiguration.IconType;
 import configuration.Configuration;
 import editor.BigFrameworkGuy.ConfigType;
 import editor.animations.FileCopy;
 
 /**
  * 
  * @author Connor Schenck
  *
  *This class represents a panel that allows icons to be
  *selected and previewed. Adding this panel to any other
  *panel will present the user with a list of buttons,
  *and when a valid icon is selected, it will preview that
  *icon next to the button. This panel contains itself in
  *a scroll pain of size (200, 175).
  */
 public class IconEditor extends JPanel implements ConfigurationEditor {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5888582783506951971L;
 	
 	private HashMap<IconType, IconPanel> panels = new HashMap<IconType, IconEditor.IconPanel>();
 	
 	private JPanel mainPanel;
 	
 	public IconEditor(List<IconType> iconTypes, List<String> descriptions)
 	{
 		this(iconTypes, descriptions, new Dimension(200, 175));
 	}
 	
 	/**
 	 * Creates the Icon Editor. Does not display it in any way. The
 	 * panel that this editor will be on is responsible for adding
 	 * it as a component.
 	 */
 	public IconEditor(List<IconType> iconTypes, List<String> descriptions, Dimension defaultSize)
 	{
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		for(int i = 0; i < iconTypes.size(); i++)
 		{
 			IconPanel panel = new IconPanel(iconTypes.get(i), descriptions.get(i));
 			this.add(panel);
 			panels.put(iconTypes.get(i), panel);
 		}
 		
 		JScrollPane scroller = new JScrollPane(this);
 		scroller.setPreferredSize(defaultSize);
 		mainPanel = new JPanel();
 		mainPanel.add(scroller);
 	}
 
 	@Override
 	public void setData(Configuration cd) {
 		IconConfiguration ic = (IconConfiguration) cd;
 		
 		this.resetEditor();
 		
 		for(IconType name : ic.getIconTypes())
 		{
 			if(panels.get(name) == null)
 				continue;
 			 panels.get(name).setURI(ic.getIconURI(name));
 		}
 		
 		this.validate();
 		this.updateUI();
 	}
 	
 	public void resetEditor()
 	{
 		for(IconPanel ip : panels.values())
 			ip.clearIcon();
 		this.validate();
 		this.updateUI();
 	}
 
 	@Override
 	public Configuration instantiateNewConfiguration() {
 		return new IconConfiguration();
 	}
 
 	@Override
 	public ConfigType getData(Configuration toSet) {
 		IconConfiguration ic = (IconConfiguration) toSet;
 		for(IconPanel ip : panels.values())
 			ic.setIcon(ip.getType(), ip.getURI());
 		return ConfigType.icon;
 	}
 
 	@Override
 	public List<ConfigType> getAllLoadableTypes() {
 		List<ConfigType> ret = new ArrayList<ConfigType>();
 		ret.add(ConfigType.icon);
 		return ret;
 	}
 
 	@Override
 	public JPanel getPanel() {
 		return mainPanel;
 	}
 	
 	private class IconPanel extends JPanel implements ActionListener {
 		
 		private IconType type;
 		private String description;
 		private JLabel icon;
 		private String uri;
 		
 		public IconPanel(IconType type, String description)
 		{
 			this.type = type;
 			this.description = description;
 			JButton set = new JButton("Set " + type.toString() + " icon");
 			set.addActionListener(this);
 			set.setToolTipText(description);
 			this.add(set);
 //			this.add(new JLabel(description));
 		}
 		
 		public IconType getType()
 		{
 			return type;
 		}
 		
 		public String getURI()
 		{
 			return uri;
 		}
 		
 		public void clearIcon() {
 			if(icon != null)
 				this.remove(icon);
 			icon = null;
 			this.uri = "";
 		}
 		
 		public void setURI(String u) {
			if(u == null || u.length() == 0)
 			{
 				clearIcon();
 				return;
 			}
 			if(u.charAt(0) == '/')
 				u = u.substring(1, u.length());
 			
 			if(icon != null)
 				this.remove(icon);
 			ImageIcon i = new ImageIcon(u);
 			i = new ImageIcon(i.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
 			this.uri = u;
 			icon = new JLabel("", i, JLabel.RIGHT);
 			this.add(icon);
 			this.validate();
 			this.updateUI();
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			JFileChooser fc = new JFileChooser();
 			try {
 				Scanner s = new Scanner(new File("lastDirectory.txt"));
 				fc = new JFileChooser(s.nextLine());
 			} catch (FileNotFoundException e) {	}
 			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			
 			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			{
 				File f = fc.getSelectedFile();
 				
 				//test loading the image
 				try {
 					ImageIcon i = new ImageIcon(f.getAbsolutePath());
 					if(i.getImage().getHeight(null) < 0)
 						throw new Exception();
 				} catch(Exception e) {
 					JOptionPane.showMessageDialog(IconEditor.this,
 						    "Error loading\n" + f.getAbsolutePath(),
 						    "Error",
 						    JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				File to = new File(new File("resources/animations"), f.getName());
 				if(!to.exists())
 				{
 					try {
 						FileCopy.copy(f.getAbsolutePath(), to.getAbsolutePath());
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				
 				String u = "resources/animations/" + to.getName();
 				this.setURI(u);
 				
 				FileWriter lastDirFile = null;
 				try {
 					lastDirFile = new FileWriter("lastDirectory.txt");
 					lastDirFile.write(f.getParent());
 					lastDirFile.flush();
 					lastDirFile.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}		
 		
 		
 	}
 
 }
