 package elfville.client.views;
 
 import java.io.IOException;
 
 import javax.swing.*;
 
 import elfville.client.ClientWindow;
 import elfville.client.SocketController;
 import elfville.client.views.subcomponents.CreatePostPanel;
 import elfville.client.views.subcomponents.Post;
 import elfville.protocol.*;
 import elfville.protocol.models.SerializablePost;
 
 /**
  * Renders the central board using the central board request.
  * 
  * @author Caleb Perkins
  * 
  */
 public class CentralBoard extends JPanel implements Refreshable {
 	private static final long serialVersionUID = 1L;
 	private final JLabel title = new JLabel("Central Board");
 	private final JPanel createPost = new CreatePostPanel(this, null);
 
 	public CentralBoard(CentralBoardResponse response) {
 		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		// TODO fix the scrolling problem (also maybe add auto wrap)
 		// basically posts with sentences that are really long run off page
 		// and central boards with too many posts can't see the newest ones
 		// (bottom of page)
 		add(title);
 		add(createPost);
		JPanel postPanel = new JPanel();
		postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
 		for (SerializablePost post : response.posts) {
			postPanel.add(new Post(post));
 		}
		JScrollPane scroll = new JScrollPane(postPanel);
		add(scroll);
		//revalidate();
		//repaint();
 	}
 
 	@Override
 	public void refresh() {
 		showCentralBoard();
 	}
 
 	public static void showCentralBoard() {
 		try {
 			CentralBoardResponse resp = SocketController
 					.send(new CentralBoardRequest());
 			if (resp.status == Response.Status.SUCCESS) {
 				CentralBoard b = new CentralBoard(resp);
 				ClientWindow.switchScreen(b);
 			} else {
 				ClientWindow.showError(resp.message,
 						"Error retrieving central board");
 			}
 		} catch (IOException e) {
 			ClientWindow.showConnectionError();
 		}
 	}
 
 }
