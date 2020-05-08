 package elfville.client.views.subcomponents;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.*;
 
 import elfville.client.ClientWindow;
 import elfville.client.SocketController;
 import elfville.client.views.Refreshable;
 import elfville.protocol.*;
 
 public class CreatePostPanel extends JPanel implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	private final JTextArea text = new JTextArea();
 	private final JTextField title = new JTextField();
 	private final JButton button = new JButton("Post");
 	private final String clanID;
 	private final JLabel titleLabel = new JLabel("Title");
 	private final JLabel textLabel = new JLabel("Text");
 	private final Refreshable board;
 
 	public CreatePostPanel(Refreshable board, String clanID) {
 		super();
 		this.clanID = clanID;
 		this.board = board;
 		makeThePanel();
 	}
 
 	private void makeThePanel() {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createTitledBorder("New Post"),
 				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
 
 		button.addActionListener(this);
 
 		add(titleLabel);
 		add(title);
 		add(textLabel);
 		add(text);
 		add(button);
 	}
 
 	// Post the message
 	// TODO finish.... Actually, I think I have now finished it (Aaron), but I'm
 	// not sure enough to remove this TODO myself
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Force there to be a title? Force there to be text? Both?
 		try {
 			Response resp;
 			if (clanID == null) {
 				PostCentralBoardRequest req = new PostCentralBoardRequest(
 						text.getText(), title.getText());
 				resp = SocketController.send(req); // returns
 													// PostCentralBoardResponse
 			} else {
 				PostClanBoardRequest req = new PostClanBoardRequest(
 						text.getText(), title.getText(), clanID);
 				resp = SocketController.send(req); // returns
 													// PostClanlBoardResponse
 			}
 
 			if (resp.isOK()) {
 				board.refresh();
 			} else {
 				System.err.println("Not posted!");
 			}
 		} catch (IOException e1) {
 			ClientWindow.showConnectionError();
 		}
 	}
 
 }
