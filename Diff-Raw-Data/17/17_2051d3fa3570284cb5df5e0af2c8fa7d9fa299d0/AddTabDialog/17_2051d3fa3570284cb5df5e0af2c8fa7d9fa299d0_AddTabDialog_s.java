 package net.bubbaland.trivia.client;
 
 import java.awt.GridBagConstraints;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Set;
 
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 
 public class AddTabDialog extends TriviaDialogPanel implements ItemListener {
 
 	private static final long		serialVersionUID	= -6388311089354721920L;
 
 	private final JComboBox<String>	tabSelector;
 	private final JTextArea			descriptionLabel;
 	private final TriviaClient		client;
 
 
 	public AddTabDialog(TriviaClient client, DnDTabbedPane pane) {
 		super();
 
 		this.client = client;
 
 		Set<String> tabNameSet = client.getTabNames();
 		String[] tabNames = new String[tabNameSet.size()];
 		tabNameSet.toArray(tabNames);
 
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.CENTER;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 
 		this.tabSelector = new JComboBox<String>(tabNames);
 		this.add(this.tabSelector, constraints);
 		this.tabSelector.addItemListener(this);
 
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		this.descriptionLabel = new JTextArea(this.client.getTabDescription(tabNames[0]));
 		this.descriptionLabel.setEditable(false);
 		this.descriptionLabel.setLineWrap(true);
 		this.descriptionLabel.setWrapStyleWord(true);
 		this.add(this.descriptionLabel, constraints);
 
 		// Display the dialog box
		this.dialog = new TriviaDialog(client.getFrame(), "Add tab", this, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
 		this.dialog.setName("Add Tab");
 		this.dialog.setVisible(true);
 
 		// If the OK button was pressed, add the proposed answer to the queue
 		final int option = ( (Integer) dialog.getValue() ).intValue();
 		if (option == JOptionPane.OK_OPTION) {
 			final String tabName = (String) tabSelector.getSelectedItem();
 			String altName = tabName;
 			int i = 1;
 			while (pane.indexOfTab(altName) > -1) {
 				altName = tabName + " (" + i + ")";
 				i++;
 			}
 			pane.addTab(altName, client.getTab(tabName));
 			final int tabLocation = pane.indexOfTab(altName);
 			pane.setSelectedIndex(tabLocation);
 		}
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		String tabName = (String) this.tabSelector.getSelectedItem();
 		String description = this.client.getTabDescription(tabName);
 		this.descriptionLabel.setText(description);
 	}
 
 }
