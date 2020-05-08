 package sk.amokk.imagesorter.gui;
 
 
 import javax.swing.JPanel;
 import java.awt.Dimension;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.KeyStroke;
 
 import sk.amokk.imagesorter.actions.ActionMove;
 import sk.amokk.imagesorter.actions.ActionNext;
 
 import javax.swing.JRadioButton;
 
 
 public class PanelMover extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private JTextField jTextField = null;
 	private JButton chooserButton = null;
 	private int number;
 	private JButton moveButton = null;
 	private JFileChooser jFileChooser;
 	protected PanelMover pm = null;
 	protected String path = null;
 	private JRadioButton jRadioButtonCopy = null;
 	private JRadioButton jRadioButtonMove = null;
 	
 
 	public int getNumber() {
 		return number;
 	}
 
 	
 	public String getPath() {
 		return path;
 	}
 
 	
 
 	/**
 	 * This is the default constructor
 	 */
 	public PanelMover(int number) {
 		super();
 		this.number = number;
 		initialize();
 		this.pm = this;
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		this.add(getButtonMove(), null);
 		this.add(getJTextField(), null);
 		this.add(getButtonChooser(), null);
 		this.add(getJRadioButtonCopy(), null);
 		this.add(getJRadioButtonMove(), null);
 		//TODO fix next two lines (bad hack to fire two actions by one keybinding)
 		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(""+number), "move");
 		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released "+number), "next");
		
 		this.getActionMap().put("move", new ActionMove(this));
 		this.getActionMap().put("next", ActionNext.getInstance());
 	
 		ButtonGroup group = new ButtonGroup();
 		group.add(getJRadioButtonCopy());
 		group.add(getJRadioButtonMove());
 		
 	}
 
 	/**
 	 * This method initializes jTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getJTextField() {
 		if (jTextField == null) {
 			jTextField = new JTextField();
 			jTextField.setPreferredSize(new Dimension(400, 20));
 			jTextField.setEditable(false);
 			jTextField.setHorizontalAlignment(JTextField.LEFT);
 		}
 		return jTextField;
 	}
 	
 	
 
 	/**
 	 * This method initializes jButton	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getButtonChooser() {
 		if (chooserButton == null) {
 			chooserButton = new JButton("...");
 			chooserButton.setFocusable(false);
 			chooserButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					JFileChooser fc = getJFileChooser();
 					int returnValue = fc.showOpenDialog(ImageSorter.getJFrame());
 					if (returnValue == JFileChooser.APPROVE_OPTION) {
 						path = fc.getSelectedFile().getAbsolutePath();
 			            getJTextField().setText(path);
 					}
 					
 
 				}
 			});
 		}
 		return chooserButton;
 	}
 
 	/**
 	 * This method initializes jButton1	
 	 * 	
	 * @return javax.swing.JButtonhttp://www.tu-ke.com/forum/4-rocnik-46/modelovanie-a-simulacia/60/	
 	 */
 	private JButton getButtonMove() {
 		if (moveButton == null) {
 			moveButton = new JButton();
 			moveButton.addActionListener(new ActionMove(this));
 			moveButton.addActionListener(ActionNext.getInstance());
 			moveButton.setText(""+number);
 			moveButton.setFocusable(false);
 
 		}
 		return moveButton;
 	}
 
 	private JFileChooser getJFileChooser() {
 		if (jFileChooser == null) {
 			jFileChooser = new JFileChooser();
 			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			
 		}
 		return jFileChooser;
 	}
 
 
 	private JRadioButton getJRadioButtonCopy() {
 		if (jRadioButtonCopy == null) {
 			jRadioButtonCopy = new JRadioButton("Copy");
 			jRadioButtonCopy.setFocusable(false);
 		}
 		return jRadioButtonCopy;
 	}
 
 
 	
 	private JRadioButton getJRadioButtonMove() {
 		if (jRadioButtonMove == null) {
 			jRadioButtonMove = new JRadioButton("Move");
 			jRadioButtonMove.setSelected(true);
 			jRadioButtonMove.setFocusable(false);
 		}
 		return jRadioButtonMove;
 	}
 
 
 
 }  //  @jve:decl-index=0:visual-constraint="10,22"
