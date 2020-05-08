 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 
 public class ConnectionGUI extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 1L;
 	
 	JPanel mainPanel = new JPanel();
 
 	JLabel titleLabel = new JLabel("Connection Information");
 	JLabel addressLabel = new JLabel("Address: ");
 	JLabel portLabel = new JLabel("Port: ");
 	JLabel downloadLabel = new JLabel("Download Save Directory: ");
 	
 	JButton okButton = new JButton("OK");
 	
 	JTextField address = new JTextField();
 	JSpinner port = new JSpinner(new SpinnerNumberModel(8010, 0, 65535, 1));
 	JTextField download = new JTextField();
 	
 	ConnectionGUI()
 	{
 		super("Connection Information");
 		FlowLayout fl = new FlowLayout();
 		fl.setAlignment(FlowLayout.LEFT);
 		setLayout(fl);
 		
 		createPanel();
 		
 		add(mainPanel);
 	}
 	
 	public void createPanel()
 	{
 		mainPanel.setPreferredSize(new Dimension(300, 300));
 		titleLabel.setPreferredSize(new Dimension(300, 20));
 		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
 		mainPanel.add(titleLabel);
 		
 		addressLabel.setPreferredSize(new Dimension(250, 20));
 		mainPanel.add(addressLabel);
 		address.setPreferredSize(new Dimension(250, 20));
 		address.setText(Resource.IP);
 		mainPanel.add(address);
 		
 		portLabel.setPreferredSize(new Dimension(250, 20));
 		mainPanel.add(portLabel);
 		port.setPreferredSize(new Dimension(250, 20));
 		mainPanel.add(port);
 		
 		downloadLabel.setPreferredSize(new Dimension(250, 20));
 		mainPanel.add(downloadLabel);
 		download.setPreferredSize(new Dimension(250, 20));
 		download.setText(Resource.FILE_SAVE_DIR);
 		mainPanel.add(download);
 		
 		mainPanel.add(okButton);
 		okButton.addActionListener(this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		if(e.getSource() == okButton)
 		{
 			Resource.IP = address.getText();
			Resource.PORT = String.valueOf(port.getValue());
 			Resource.FILE_SAVE_DIR = download.getText();
 			
 			GUI.connectionGUIStatus = true;
 		}
 	}
 }
