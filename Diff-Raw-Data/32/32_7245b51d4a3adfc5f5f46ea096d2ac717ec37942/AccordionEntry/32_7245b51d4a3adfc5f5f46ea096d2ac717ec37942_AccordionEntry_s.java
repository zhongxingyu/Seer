 package evaluation.simulator.gui.customElements.accordion;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import evaluation.simulator.annotations.simulationProperty.IntProp;
 import evaluation.simulator.annotations.simulationProperty.SimProp;
 import evaluation.simulator.gui.customElements.configElements.IntConfigElement;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 @SuppressWarnings("serial")
 public class AccordionEntry extends JPanel {
 
 	private static Logger logger = Logger.getLogger(AccordionEntry.class);
 
 	private final JComboBox<String> comboBox;
 	private boolean fresh;
 	private final JButton entryButton;
 	private final String localName;
 	private final PropertyPanel propertyPanel;
 
 	public AccordionEntry(String name, JComboBox<String> jComboBox) {
 		this.localName = name;
 
 		if ( jComboBox == null ){
 			logger.log(Level.ERROR, "jComboBox == null");
 		}
 		this.comboBox = jComboBox;
 		this.fresh = true;
 
 		this.setLayout(new BorderLayout(0, 0));
 		this.entryButton = new JButton(this.localName, new ImageIcon(
 				"etc/img/icons/green/arrow-144-24.png"));
 		this.entryButton.setForeground(Color.BLACK);
 		this.entryButton.setHorizontalAlignment(SwingConstants.LEFT);
 		this.entryButton.setHorizontalTextPosition(SwingConstants.RIGHT);
 
 		ActionListener actionListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AccordionEntry.this.toggleVisibility(e.getSource(),AccordionEntry.this.comboBox);
 			}
 		};
 
 		this.entryButton.addActionListener(actionListener);
 		this.entryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		this.add(this.entryButton, BorderLayout.NORTH);
 		this.comboBox.setVisible(false);
 		this.comboBox.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 
 				if (e.getStateChange() == ItemEvent.SELECTED && AccordionEntry.this.comboBox.isVisible()) {
 					AccordionEntry.this.comboBoxChanged(AccordionEntry.this.comboBox);
 				}
 
 			}
 		});
 		this.comboBox.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
 
 		if ( this.comboBox.getModel().getSize() > 1 ){
 			this.add(this.comboBox, BorderLayout.CENTER);
 		}
 		
 		propertyPanel = new PropertyPanel(this.localName);
 		propertyPanel.setVisible(false);
 		this.add(propertyPanel, BorderLayout.SOUTH);
 
 	}
 
 	private void comboBoxChanged(JComboBox<String> jComboBox) {
 		
 
 			logger.log(Level.DEBUG, "Reload table");
 			SimPropRegistry simPropRegistry = SimPropRegistry.getInstance();
 
 			String pluginLevel = this.localName;
 			String pluginName = (String) jComboBox.getSelectedItem();
 
 			logger.log( Level.DEBUG, "Set plugin-level " + pluginLevel + " to " + pluginName);
 			simPropRegistry.setActivePlugins(pluginLevel, pluginName); // GGF Mapped
 			
 			if(this.fresh==true){			
 				fresh=false;
 				jComboBox.removeItemAt(0);
 			}
 
 			propertyPanel.realoadContent(pluginName);
 			propertyPanel.setVisible(true);			
 			
 			this.updateUI();	
 			
 	}
 
 //	public void setVibility(boolean b) {
 //
 //		this.propertyPanel.setVisible(b);
 //		this.comboBox.setVisible(b);
 //		this.entryButton.setIcon(new ImageIcon("etc/img/icons/red/arrow-144-24.png"));
 //	}
 
 	private void toggleVisibility(Object source, JComboBox<String> jComboBox) {
 
 		JButton btn = (JButton) source;
 		if (!jComboBox.isVisible()) {
 			btn.setIcon(new ImageIcon("etc/img/icons/red/arrow-144-24.png"));
 			propertyPanel.setVisible(true);
 			jComboBox.setVisible(true);
 		} else {
 			btn.setIcon(new ImageIcon("etc/img/icons/green/arrow-144-24.png"));
 			propertyPanel.setVisible(false);
 			jComboBox.setVisible(false);
 		}
 		this.repaint();
 	}
 
 	public void setVibility(boolean b) {
		entryButton.setIcon(new ImageIcon("etc/img/icons/green/arrow-144-24.png"));
		propertyPanel.setVisible(false);
		comboBox.setVisible(false);
 		this.repaint();
 	}
 }
