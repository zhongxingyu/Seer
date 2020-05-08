 package plugin;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class PluginCore {
 	// GUI Widgets that we will need
 	private JFrame frame;
 	private JPanel contentPane;
 	private JLabel bottomLabel;
 	private JList sideList;
 	private DefaultListModel<String> listModel;
 	private JPanel centerEnvelope;
 	private final JButton addButton = new JButton("Add Plugin");
 	private final JButton removeButton = new JButton("Remove Plugin"); 
 	// For holding registered plugin
 	private HashMap<String, Plugin> idToPlugin;
 	private Plugin currentPlugin;
 	
 	public PluginCore() 
 	{
 		idToPlugin = new HashMap<String, Plugin>();
 		
 		// Lets create the elements that we will need
 		frame = new JFrame("Pluggable Board Application");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		contentPane = (JPanel)frame.getContentPane();
 		contentPane.setPreferredSize(new Dimension(700, 500));
 		bottomLabel = new JLabel("No plugins registered yet!");
 
 		// Lets lay them out, contentPane by default has BorderLayout as its layout manager
 		contentPane.add(centerEnvelope(), BorderLayout.CENTER);
 		contentPane.add(sidePanel(), BorderLayout.EAST);
 		contentPane.add(bottomLabel, BorderLayout.SOUTH);
 		
 		JPanel bottomPanel = new JPanel(); 
 		bottomPanel.add(addButton);
 		bottomPanel.add(removeButton); 
 		contentPane.add(bottomPanel, BorderLayout.PAGE_END); 
 		
 
 	}
 	
 	public void start() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run()
 			{
 				frame.pack();
 				frame.setVisible(true);
 			}
 		});
 	}
 
 	public void stop() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run()
 			{
 				frame.setVisible(false);
 			}
 		});
 	}
 	
 	public void addPlugin(Plugin plugin) 
 	{
 		this.idToPlugin.put(plugin.getId(), plugin);
 		this.listModel.addElement(plugin.getId());
 		this.bottomLabel.setText("The " + plugin.getId() + " plugin has been recently added!");
 	}
 	
 	public void removePlugin(String id) 
 	{
 		Plugin plugin = this.idToPlugin.remove(id);
 		this.listModel.removeElement(id);
 		
 		// Stop the plugin if it is still running
 		plugin.stop();
 
 		this.bottomLabel.setText("The " + plugin.getId() + " plugin has been recently removed!");
 	}
 	private JScrollPane sidePanel()
 	{
 		listModel = new DefaultListModel<String>();
 		sideList = new JList(listModel);
 		sideList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		sideList.setLayoutOrientation(JList.VERTICAL);
 		JScrollPane scrollPane = new JScrollPane(sideList);
 		scrollPane.setPreferredSize(new Dimension(100, 50));
 		sideList.getSelectionModel().addListSelectionListener(new sideListListener());
 		return scrollPane;
 	}
 	private JPanel centerEnvelope()
 	{
 		centerEnvelope = new JPanel(new BorderLayout());
 		centerEnvelope.setBorder(BorderFactory.createLineBorder(Color.black, 5));
 		return centerEnvelope;
 	}
 	
 	public void addAddActionListener(ActionListener a)
 	{
 		addButton.addActionListener(a);
 	}
 	
 	public void addRemoveActionListener(ActionListener r)
 	{
 		removeButton.addActionListener(r); 
 	}
 	public JButton getAddButton()
 	{
 		return addButton; 
 	}
 	
 	public JButton getRemoveButton()
 	{
 		return removeButton; 
 	}
 	
 	public HashMap pluginMap()
 	{
 		return idToPlugin; 
 	}
 	
 	private class sideListListener implements ListSelectionListener {
 
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if(e.getValueIsAdjusting())
 				return;
 			
 			// List has finalized selection, let's process further
 			int index = sideList.getSelectedIndex();
 			String id = listModel.elementAt(index);
 			Plugin plugin = idToPlugin.get(id);
 			
 			if(plugin == null || plugin.equals(currentPlugin))
 				return;
 			
 			// Stop previously running plugin
 			if(currentPlugin != null)
 				currentPlugin.stop();
 			
 			// The newly selected plugin is our current plugin
 			currentPlugin = plugin;
 			
 			// Clear previous working area
 			centerEnvelope.removeAll();
 			
 			// Create new working area
 			JPanel centerPanel = new JPanel();
 			centerEnvelope.add(centerPanel, BorderLayout.CENTER); 
 			
 			// Ask plugin to layout the working area
 			currentPlugin.layout(centerPanel);
 			contentPane.revalidate();
 			contentPane.repaint();
 			
 			// Start the plugin
 			currentPlugin.start();
 			
 			bottomLabel.setText("The " + currentPlugin.getId() + " is running!");
 		};
 		};
 }
