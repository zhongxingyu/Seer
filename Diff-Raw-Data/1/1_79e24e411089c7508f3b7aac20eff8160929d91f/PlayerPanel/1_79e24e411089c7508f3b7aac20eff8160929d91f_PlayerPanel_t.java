 package admin.playertab;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import data.Contestant;
 import data.GameData;
 
 
 import admin.Main;
 
 
 public class PlayerPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private JButton imgDisplay;
 
 	private EditPlayerFieldsPanel paneEditFields;
 	// container for top stuff
 	private JPanel paneTop;
 	
 	private JButton bCastOff;
 	private JButton bSavePlayer;
 	
 	private JLabel labelName;
 	// TODO: Refactor to something more obvious?
 	private JLabel labelCastOff;
 	private JLabel labelCastStatus;
 	private JLabel labelTribe;
 	
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 	private JComboBox<String> cbTribe;
 	
 	private PlayerTablePanel paneTable;
 	
 	private static Contestant INACTIVE_CONT = new Contestant();
 	private Contestant activeCon = INACTIVE_CONT;
 
 	private PlayerTableModel tableModel;
 	
 	
 	public PlayerPanel(){
 		paneTop = new JPanel();
 		paneTop.setLayout(new BoxLayout(paneTop, BoxLayout.X_AXIS));
 		
 		// TODO: Resize?
 		String path = "res/test/defaultpic.png"; //apparently images have to be .png and alphanumeric
 		ImageIcon imgD = new ImageIcon(path);
 		imgDisplay = new JButton();
 		imgDisplay.setIcon(imgD);
 		
 		/// Edit fields:
 		labelName = new JLabel("Name:");
 		tfFirstName = new JTextField();
 		tfFirstName.setSize(200, 50);
 		tfLastName = new JTextField();
 		tfLastName.setSize(200, 50);
 		
 		labelCastOff = new JLabel("Cast of:");
 		// TODO: FIx the init of this.. :>
 		labelCastStatus = new JLabel("-");
 		
 		labelTribe = new JLabel("Tribe:");
 		cbTribe = new JComboBox<String>(GameData.getCurrentGame().getTribeNames());
 		
 		// holds all the fields
 		paneEditFields = new EditPlayerFieldsPanel(labelName, labelCastOff, 
 				labelCastStatus, labelTribe, tfFirstName, tfLastName, 
 				cbTribe);
 		
 		// buttons:
 		bCastOff = new JButton("Cast Off");
 		bSavePlayer = new JButton("Save");
 		
 		bSavePlayer.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String pattern = "[A-Za-z]{1,20}";
 				if(!Main.checkString(tfFirstName.getText(), pattern)||
 						!Main.checkString(tfLastName.getText(), pattern)){
 					JOptionPane.showMessageDialog(null,"Invalid name!(dialog box not permanent)");
 				}
 			}
 			
 		});
 		
 		imgDisplay.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				int ret = fc.showOpenDialog(null);
 				if(ret==JFileChooser.APPROVE_OPTION){
 					//File f = fc.getSelectedFile();
 					ImageIcon i = new ImageIcon(fc.getSelectedFile().getAbsolutePath());
 					imgDisplay.setIcon(i);
 					
 				}
 				
 				
 			}
 			
 		});
 		
 		// this does not need to be referenced else where, only for layout
 		JPanel paneButtons = new JPanel();
 		GridLayout bl = new GridLayout(2, 1);
 		paneButtons.setLayout(bl);
 		
 		paneButtons.add(bCastOff);
 		paneButtons.add(bSavePlayer);
 		
 		// add all components on top:
 		paneTop.add(imgDisplay);
 		paneTop.add(paneEditFields);
 		paneTop.add(paneButtons);
 		
 		// bottom panel
 		tableModel = new PlayerTableModel(GameData.getCurrentGame().getAllContestants());
 		paneTable = new PlayerTablePanel(tableModel);
 		
 		
 		add(paneTop);
 		add(paneTable);
 		
 		buildActions();
 	}
 	
 	/**
 	 * gets the current information with the current contestant, will update 
 	 * from the fields associated.
 	 * @return Current contestant loaded
 	 */
 	private Contestant getCurrentContestant() {
 		boolean newCont = false;
 		Contestant x = null;
 		if (activeCon == INACTIVE_CONT) {
 			activeCon = new Contestant();
 			newCont = true;
 		}
 		
 		activeCon.setFirstName(tfFirstName.getText());
 		activeCon.setLastName(tfLastName.getText());
 		activeCon.setTribe((String)cbTribe.getSelectedItem());
 		
 		if (newCont)
 			activeCon.generateID();
 		
 		x = activeCon;
 		activeCon = INACTIVE_CONT;
 		
 		return x;
 	}
 	
 	/**
 	 * Updates the image displayed to have the path associated, helper method
 	 * <br>
 	 * <b>Note:</b> Pictures must be PNG format.
 	 * @param path Path to new image.
 	 */
 	private void updateContPicture(String path) {
 		//apparently images have to be .png and alphanumeric
 		try {
 			Image img = ImageIO.read(new File(path));
 			
 			// NO IO errors occured if getting here:
 			ImageIcon imgD = new ImageIcon(img);
 			imgDisplay.setIcon(imgD);
 		} catch (IOException e) {
 			System.out.println("Exception loading image for contestant " +
 					"picture [" + path + "]");
 			imgDisplay.setIcon(null);
 			imgDisplay.setText("Could not load: " + path);
 		}
 		
 	}
 	
 	private void setActiveContestant(Contestant c) {
 		if (c == INACTIVE_CONT) {
 			tfFirstName.setText("");
 			tfLastName.setText("");
 			labelCastStatus.setText("-");
 			cbTribe.setSelectedItem(0);
 			
 			// TODO: Make a picture of an X or some shit for nothing loaded
 			//updateContPicture(SOMEPATH);
 			return;
 		}
 		
 		activeCon = c;
 		
 		tfFirstName.setText(c.getFirstName());
 		tfLastName.setText(c.getLastName());
 		
 		if (c.isCastOff()) {
 			labelCastStatus.setText("Week: " + c.getCastDate());
 		} else {
 			labelCastStatus.setText("Active");
 		}
 		
 		cbTribe.setSelectedItem(c.getTribe());
 		
 		updateContPicture(c.getPicture());
 	}
 	
 	private void buildActions() {
 		ActionListener addListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// check if the contestant is active
 				Contestant con = getCurrentContestant();
 				
 				tableModel.updateContestant(con);
 				
 				System.out.println("We here");
 				
 			}
 			
 		};
 		bSavePlayer.addActionListener(addListener);
 	}
 	
 }
