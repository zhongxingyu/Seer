 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 @SuppressWarnings("serial")
 public class FactoryProductionSchedulePanel extends JPanel implements
 		ActionListener{
 	/** kits which are sent from server */
 	public ArrayList<Kit> kits;
 	/** produce button when user want to produce kits */
 	public JButton btnProduce;
 	/** print text "Select a Kit" */
 	private JLabel lblSelectKit;
 	/** strings that used in combobox for kits' names*/
 	private Vector<String> vectorjcbKitStrings = new Vector<String>();
 	/** combobox for selecting a kit to produce */
 	public JComboBox jcbSelectKit;
 	/** textfield to enter amount of kit user wnat to produce */
 	public JTextField txtKitQuantity;
 	/** work on this feature when we want enhance the Factory Production GUI */
 	private JLabel picture;
 	/** ProduceStatusMsg is sent from Server to info the kits' amount and status */
 	private ProduceStatusMsg status;
 	/** factory needed for GridBagLayout */
 	GridBagConstraints c = new GridBagConstraints();
 	/** scroll pane for displaying kits schedule */
 	private JScrollPane scroll;
 	/** put pnlKits inside JscrollPane */
 	public JPanel pnlKits;
 	/** label for prompting user input */
 	public JLabel lblQuantity;
 	public FactoryProductionSchedulePanel() {
 		this.setPreferredSize(FactoryPainter.getAreaSize(FactoryPainter.FactoryArea.ENTIRE_FACTORY));
 		
 		initialize();
 		makeSchedule();
 	}
 	
 	/** initialize variables */
 	public void initialize() {
 		pnlKits = new JPanel();
 		pnlKits.setLayout(new BoxLayout(pnlKits, BoxLayout.Y_AXIS));
 		pnlKits.add(new JLabel("Welcome to Kit Queue List"));
 		pnlKits.setVisible(true);
 		scroll = new JScrollPane(pnlKits);
 
 		kits = new ArrayList<Kit>();
 		status = new ProduceStatusMsg();
 		lblQuantity = new JLabel("Quantity");
 		lblSelectKit = new JLabel("Select a Kit: ");
 		jcbSelectKit = new JComboBox(vectorjcbKitStrings);
 		jcbSelectKit.setPreferredSize(new Dimension(100, 25));
 		btnProduce = new JButton("Add to Production Queue");
 		txtKitQuantity = new JTextField();
 //		txtKitQuantity.setPreferredSize(new Dimension(100, 60));
 		
 		// work on this feature when we want enhance the Factory Production GUI
 		picture = new JLabel();
 		picture.setPreferredSize(new Dimension(50, 50));
 		jcbSelectKit.addActionListener(this);
 	}
 	/** manage the layout for schedule */
 	public void makeSchedule() {
 
 		setLayout(new GridBagLayout());
 
 		// layout for combobox
 		
 		// JScrollPane for listing production
 		c.fill = GridBagConstraints.VERTICAL;
 		c.anchor = GridBagConstraints.WEST;
 		c.ipadx = 200;
 		c.weighty = 0.0;
 		c.gridheight = 5;
 		c.gridx = 0;
 		c.gridy = 0;
 		add(scroll, c);
 		
 		c.gridheight = 1;
 		c.weightx = 0.5;
 		c.weighty = 0.5;
 		c.fill = GridBagConstraints.NONE;
 		c.anchor = GridBagConstraints.CENTER;
 		
 		// Select kit label
 		c.ipadx = 0;
 		c.ipady = 0;
 		c.gridx = 3;
 		c.gridy = 0;
 		add(lblSelectKit, c);
 		
 		// Kit drop down menu
 		c.ipadx = 100;
 		c.ipady = 0;
 		c.gridx = 5;
 		c.gridy = 0;
 		add(jcbSelectKit, c);
 		
 		// Quantity label
 		c.ipadx = 0;
 		c.ipady = 0;
 		c.gridx = 3;
 		c.gridy = 2;
 		add(lblQuantity, c);
 		
 		// Quantity text box
 		c.ipadx = 100;
 		c.ipady = 0;
 		c.gridx = 5;
 		c.gridy = 2;
 		add(txtKitQuantity, c);
 		
 		// Produce button
 		c.ipadx = 100;
 		c.ipady = 25;
 		c.gridwidth = 3;
 		c.gridx = 3;
 		c.gridy = 4;
 		add(btnProduce, c);
 	}
 	
 	/** when received ProduceStatusMsg from Server, regenerate all of the available kits */
 	public void updateSchedule(ProduceStatusMsg msg) {
 		status = msg;
 		regenerateSchedule(status);
 	}
 	
 	/** regenerate all of the available kits */
 	public void regenerateSchedule(ProduceStatusMsg statusmsg) {
 		ProduceStatusMsg status = statusmsg;
 		String kitname = "";
 		pnlKits.removeAll();
 		if (status.cmds.size() > 0) {
 			
 			for (int i = 0; i < status.cmds.size(); i++) {
 				for (int j = 0; j < kits.size(); j++) {
 					kitname = kits.get(j).getName();
 
 					if (kits.get(j).getNumber() == status.cmds.get(i).kitNumber) {
 						System.out.println(kitname);
 						
 						pnlKits.add(new JLabel(kitname + " - "
 								+ status.cmds.get(i).howMany + " - "
 								+ status.status.get(i)));
 					}
 				}
 			}
 		} 
 	
 		validate();
 		repaint();
 	}
 	/** regenerate all of the available kits */
 	public void updateKitList(KitListMsg msgObj) {
		validate();
		repaint();
 		vectorjcbKitStrings.clear();
 		kits.clear();
 		for (int i = 0; i < msgObj.kits.size(); i++) {
 			kits.add(msgObj.kits.get(i));
 			vectorjcbKitStrings.add(msgObj.kits.get(i).getName());
 		}

 		validate();
 		repaint();
 	}
 	/**this method never run since we didn't enable this feature until we decide what is our factory going to produce  */
 	protected void updateLabel(String name) {
 		ImageIcon icon = new ImageIcon(/* type image address here */);
 		picture.setIcon(icon);
 		picture.setPreferredSize(new Dimension(50, 50));
 		picture.setToolTipText("A drawing of a " + name.toLowerCase());
 		if (icon != null) {
 			picture.setText(null);
 		}
 
 	}
 
 	@Override
 	/** handle user's input (mostly combobox selection) */
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 		if (e.getSource() == jcbSelectKit) {
 			JComboBox cb = (JComboBox) e.getSource();
 			String kitName = (String) cb.getSelectedItem();
 			updateLabel(kitName);
 		}
 	}
 }
