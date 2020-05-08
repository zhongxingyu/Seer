 package elfville.client.views;
 
 import javax.swing.*;
 
 import elfville.client.ClientWindow;
 import elfville.client.SocketController;
 import elfville.client.views.subcomponents.VotablePost;
 import elfville.protocol.*;
 import elfville.protocol.models.SerializablePost;
 
 public class ElfProfile extends Board {
 	private static final long serialVersionUID = 1L;
 	String elf;
 	
 	public ElfProfile(ClientWindow clientWindow, SocketController socketController, String elf) {
 		super(clientWindow, socketController);
 		this.elf = elf;
 		ProfileRequest req = new ProfileRequest(elf);
 		getSocketController().sendRequest(req, this,
 				"Error retrieving elf profile.", this);
 	}
 	
 	@Override
 	public void refresh() {
 		new ElfProfile(clientWindow, socketController, elf);
 	}
 
 	@Override
 	public void handleRequestSuccess(Response resp) {
 		ProfileResponse response = (ProfileResponse) resp;
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
		JLabel title = new JLabel("Profile of " + response.elf.elfName + ", " + response.elf.numSocks);
 		add(title);
 		
 		JTextArea description = new JTextArea(response.elf.description);
 		description.setEditable(false);
 		add(new JScrollPane(description));
 		
 		JPanel postPanel = new JPanel();
 		postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
 		for (SerializablePost post : response.elf.centralBoardPosts) {
 			postPanel.add(new VotablePost(this, post));
 		}
 		JScrollPane scroll = new JScrollPane(postPanel);
 		add(scroll);
 		clientWindow.switchScreen(this);
 	}
 }
