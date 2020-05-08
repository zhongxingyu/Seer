 package elfville.client.views;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.*;
 
 import elfville.client.SocketController;
 import elfville.protocol.*;
 
 
 public class CreateClanPanel extends JPanel implements ActionListener {
 	private final JTextArea description = new JTextArea();
 	private final JTextField name = new JTextField();
 	private final JButton button = new JButton("Create");
 	private final JLabel nameLabel = new JLabel("name");
 	private final JLabel descriptionLabel = new JLabel("Description");
 	private final Refreshable board;
 	
 	public CreateClanPanel(Refreshable board) {
 		super();
 		this.board = board;
 		makeThePanel();
 	}
 	
 	private void makeThePanel() {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder("New Post"),
                 BorderFactory.createEmptyBorder(5,5,5,5)));
 		
 		button.addActionListener(this);
 		
 		add(nameLabel);
 		add(name);
 		add(descriptionLabel);
 		add(description);
 		add(button);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		try {
 			if (name.getText().equals("")) {
 				System.err.println("Need a clan name to create a clan!");
 			}
 			else {
 				SerializableClan clan = new SerializableClan();
 				clan.clanName = name.getText();
 				clan.clanDescription = description.getText();
 				CreateClanRequest req = new CreateClanRequest(clan);
 				CreateClanResponse resp = SocketController.send(req);
 				if (resp.isOK()) {
 					board.refresh();
 				} else {
 					System.err.println("Not posted!");
 				}
 			}
 		} catch (IOException e1) {
			ClientWindow.showConnectionError();
 		}
 	}
 
 }
