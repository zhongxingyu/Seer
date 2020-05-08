 package bt.GUIComponents;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import bt.Model.Bittorrent;
 
 /**
  * First GUI component presented to user for torrent file set up.
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci
  *
  */
 
 @SuppressWarnings("serial")
 public class FileSelectionDialog extends JDialog {
 
 	private JPanel mainPanel;
 	private JTextField textField;
 	private JButton browseButton;
 	private JButton continueButton;
 	private JButton cancelButton;
 	private JLabel titleLabel;
 	private JLabel torrentFileLabel;
 	private JLabel fileNameLabel;
 	private JTextField saveFileField;
 	private JLabel errorLabel;
 	
 	/**
 	 * Constructs a FileSelectionDialog object.
 	 * @param parent
 	 * @param modal
 	 */
 	public FileSelectionDialog(JFrame parent, boolean modal) {
 		super(parent, modal);		
 		this.initLayout();
 		this.initDecorations();
 		this.add(this.mainPanel);
 		this.setUndecorated(true);
 		this.initBehaviors();
 		this.pack();
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 	}
 	
 	/**
 	 * Initializes this frame's layout.
 	 */
 	private void initLayout() {
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints gc;
 		this.mainPanel = new JPanel(gb);
 		
 		
 		// init components
 		this.titleLabel = new JLabel(" CS352 - Bittorrent Project ");
 		this.torrentFileLabel = new JLabel("Type or browse .torrent file");
 		this.textField = new JTextField(28);
 		this.browseButton = new JButton(" ... ");
 		this.cancelButton = new JButton(" Exit ");
 		this.continueButton = new JButton(" Start Client ");
 		this.fileNameLabel = new JLabel("File to save torrent");
 		this.saveFileField = new JTextField(40);
 		this.errorLabel = new JLabel(" ");
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(10,5,10,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		gc.weightx = 1;
 		gc.anchor = GridBagConstraints.CENTER;
 		this.mainPanel.add(this.titleLabel, gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,2,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.anchor = GridBagConstraints.WEST;
 		this.mainPanel.add(this.torrentFileLabel, gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,10,5);
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		this.mainPanel.add(this.textField,gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,10,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.anchor = GridBagConstraints.EAST;
 		this.mainPanel.add(this.browseButton, gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,5,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.anchor = GridBagConstraints.WEST;
 		this.mainPanel.add(this.fileNameLabel, gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,5,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.anchor = GridBagConstraints.WEST;
 		this.mainPanel.add(this.saveFileField, gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,5,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.anchor = GridBagConstraints.CENTER;
 		this.mainPanel.add(this.errorLabel, gc);
 		
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,5,5);
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		gc.anchor = GridBagConstraints.WEST;
 		this.mainPanel.add(this.cancelButton, gc);
 		
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(5,5,5,5);
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		this.mainPanel.add(this.continueButton, gc);
 		
 		
 	}
 	
 	/**
 	 * Initializes this frame's behaviors.
 	 */
 	private void initBehaviors() {
 		this.browseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JFileChooser fc = new JFileChooser();
 				fc.setAcceptAllFileFilterUsed(false);
 				fc.addChoosableFileFilter(new TorrentFileFilter());
 				int rVal = fc.showOpenDialog(FileSelectionDialog.this);
 				if(rVal == JFileChooser.APPROVE_OPTION) {
 					textField.setText(fc.getSelectedFile().getAbsolutePath());
 				}
 			}
 		});
 		
 		this.continueButton.addActionListener(new ActionListener() {		
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String torrentFile = textField.getText().toString();
 				String saveTorrentFile = saveFileField.getText().toString();
 				try {
 					if(torrentFile.length() > 0 && saveTorrentFile.length() > 0){
 						Bittorrent.getInstance(torrentFile,saveTorrentFile);
 						FileSelectionDialog.this.dispose();
 					}
 				} catch (Exception exception) {
 					textField.setText("");
 					saveFileField.setText("");
 					errorLabel.setText("Invalid parameters, please try again");
 				}
 			}
 		});
 		
 		this.cancelButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 	}
 	
 	/**
 	 * Initializes this frames decorations.
 	 */
 	private void initDecorations() {
 		this.mainPanel.setBorder(BorderFactory.createRaisedBevelBorder());
 		this.titleLabel.setFont(new Font("Courrier", Font.BOLD, 14));
 		this.titleLabel.setBorder(BorderFactory.createEtchedBorder());
 		this.titleLabel.setHorizontalAlignment(JLabel.CENTER);
 		this.errorLabel.setFont(new Font("Courrier", Font.ITALIC, 12));
 	}
 }
