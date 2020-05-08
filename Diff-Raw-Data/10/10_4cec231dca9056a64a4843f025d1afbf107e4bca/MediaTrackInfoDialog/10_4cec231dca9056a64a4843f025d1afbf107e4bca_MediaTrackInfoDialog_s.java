 package house.neko.media.slave;
 
 import house.neko.media.common.Media;
 import house.neko.media.common.MediaLocation;
 import house.neko.media.common.ConfigurationManager;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Container;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 
 
 import org.apache.commons.logging.Log;
 
 public class MediaTrackInfoDialog extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 60L;
 	
 	private static final String UPDATE_ACTION = "U";
 	private static final String CANCEL_ACTION = "C";
 
 	private Slave slave;
 	private Media media;
 	
 	private Log log = null;
 	
 	private JTextField nameField;
 	private JTextField authorField;
 	private JTextField artistField;
 	private JTextField artistAliasField;
 	private JTextField albumField;
 	private JButton cancelButton;
 	private JButton okayButton;
 	
 	public MediaTrackInfoDialog(Slave slave, Media media)
 	{
 		super("Media Track: " + media.getName());
 		this.log = ConfigurationManager.getLog(getClass());
 		this.slave = slave;
 		this.media = media;
 		setMinimumSize(new Dimension(150, 100));
 		setSize(new Dimension(450, 400));
 
 		Container c = getContentPane();
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints gc = new GridBagConstraints();
 		c.setLayout(gridbag);
 		gc.anchor = GridBagConstraints.NORTHWEST;
 		gc.ipadx = 7;
 		
 		int y = 0;
 		
 		
 		addLabel(new JLabel("Artist"), gridbag, gc, 0, ++y);
 		artistAliasField = new JTextField(media.getArtist());
 		addField(artistAliasField, gridbag, gc, 3, y);
 		
 		addLabel(new JLabel("Artist Alias"), gridbag, gc, 0, ++y);
 		artistField = new JTextField(media.getArtistAlias());
 		addField(artistField, gridbag, gc, 3, y);
 		
 		addLabel(new JLabel("Title"), gridbag, gc, 0, ++y);
 		nameField = new JTextField(media.getName());
 		addField(nameField, gridbag, gc, 3, y);
 		
 		addLabel(new JLabel("Album"), gridbag, gc, 0, ++y);
 		albumField = new JTextField(media.getAlbum());
 		addField(albumField, gridbag, gc, 3, y);
 		
 		addLabel(new JLabel("Author"), gridbag, gc, 0, ++y);
 		authorField = new JTextField(media.getAuthor());
 		addField(authorField, gridbag, gc, 3, y);
 		
 		addLabel(new JLabel("Interal ID"), gridbag, gc, 2, ++y);
 		addLabel(new JLabel(media.getID()), gridbag, gc, 3, y);
 		addLabel(new JLabel("Local ID"), gridbag, gc, 2, ++y);
		addLabel(new JLabel(Long.toString(media.getLocalID())), gridbag, gc, 3, y);
 		
 		addLabel(new JLabel("URL"), gridbag, gc, 2, ++y);
 		MediaLocation l = media.getLocation();
 		addLabel(new JLabel(l != null ? l.getLocationURLString() : ""), gridbag, gc, 3, y);
 		
 		okayButton = new JButton("  OK  ");
 		okayButton.setActionCommand(UPDATE_ACTION);
 		okayButton.addActionListener(this);
 		addComponent(okayButton, gridbag, gc, 2, ++y);
 		
 		cancelButton= new JButton("  Cancel  ");
 		cancelButton.setActionCommand(CANCEL_ACTION);
 		cancelButton.addActionListener(this);
 		addComponent(cancelButton, gridbag, gc, 3, y);
 		
 		
 	}
 	
 	private void addComponent(Component c, GridBagLayout gridbag, GridBagConstraints gc, int x, int y)
 	{
 		gc.gridx = x;
 		gc.gridy = y;
 		gridbag.setConstraints(c, gc);
 		getContentPane().add(c);
 	}
 	
 	private void addLabel(JLabel label, GridBagLayout gridbag, GridBagConstraints gc, int x, int y)
 	{
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		addComponent(label, gridbag, gc, x, y);
 	}
 	
 	private void addField(JTextField field, GridBagLayout gridbag, GridBagConstraints gc, int x, int y)
 	{
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		addComponent(field, gridbag, gc, x, y);
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		if(log.isTraceEnabled())
 		{	log.trace("Action " + e.getActionCommand()); }
 		if(UPDATE_ACTION.equals(e.getActionCommand()))
 		{
 			
 		} else if(CANCEL_ACTION.equals(e.getActionCommand())) {
 			setVisible(false);
 		}
 	}
 }
