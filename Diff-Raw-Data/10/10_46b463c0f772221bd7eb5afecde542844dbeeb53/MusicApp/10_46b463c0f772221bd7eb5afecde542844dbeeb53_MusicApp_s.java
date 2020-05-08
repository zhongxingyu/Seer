 package Client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 
 import Library.Album;
 import Library.Library;
 import Library.Song;
 import cst420.media.MusicLibraryGui;
 
 /**
  * Purpose is to serve as the main gui and flow of control for the app.
  * 
  * @author James Harris
  * @version November 2 2012 new Gui?
  */
 @SuppressWarnings("serial")
 public class MusicApp extends MusicLibraryGui implements
 		TreeWillExpandListener, ActionListener, TreeSelectionListener {
 
 	private PlayWavThread player = null;
 	private boolean stopPlaying;
 	private Socket socket;
 	private DataInputStream inStream;
 	private DataOutputStream outStream;
 	private FileSendThreadClient fileUploader;
 	private String host;
 	private LibraryRefreshNotifier treeRefresher;
 	private int port;
 	private int clientID;
 
 	public MusicApp(String base) {
 		super(base);
 		try {
 			host = JOptionPane.showInputDialog(this, "What is the server ip?",
 					"localhost");
 			// "wizardofmath.no-ip.org");
 			port = Integer.parseInt(JOptionPane.showInputDialog(this,
 					"What is the server port?", "8888"));
 			socket = new Socket(host, port);
 			Thread.sleep(200);
 			treeRefresher = new LibraryRefreshNotifier(this);
 			treeRefresher.start();
 			inStream = new DataInputStream(socket.getInputStream());
 			outStream = new DataOutputStream(socket.getOutputStream());
 			setStopPlaying(false);
 			byte[] bytesRecieved = new byte[1024];
 			clientID = Integer.parseInt(new String(bytesRecieved, 0, inStream
 					.read(bytesRecieved)));
 			for (int i = 0; i < userMenuItems.length; i++) {
 				for (int j = 0; j < userMenuItems[i].length; j++) {
 					userMenuItems[i][j].addActionListener(this);
 				}
 			}
 			tree.addTreeSelectionListener(this);
 			tree.addTreeWillExpandListener(this);
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			treeRefresh();
 			setVisible(true);
 			System.out.println("Connected");
 
 		} catch (IOException | InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@SuppressWarnings("unused")
 	@Deprecated
 	private Library getLibrary() throws UnknownHostException, IOException,
 			InterruptedException {
 		outStream.write("getLibrary".getBytes());
 		Thread.sleep(200);
 		Socket libSocket = new Socket(host, (port + 4));
 		DataInputStream inPut = new DataInputStream(libSocket.getInputStream());
 		File theDir = new File(System.getProperty("user.dir") + "/Temp/");
 		if (!theDir.exists()) {
 			System.out.println("creating directory: "
 					+ System.getProperty("user.dir") + "/Temp/");
 			theDir.mkdir();
 		}
 		String fileName = System.getProperty("user.dir") + "/Temp/" + "lib.xml";
 		FileOutputStream outStream = new FileOutputStream(fileName);
 		byte[] buffer = new byte[1024];
 		int size = inPut.read(buffer);
 		while (size > 0) {
 			outStream.write(buffer, 0, size);
 			size = inPut.read(buffer);
 		}
 		outStream.close();
 		libSocket.close();
 		System.out.println("Download Successfully!");
 		return new Library().restore(fileName);
 
 	}
 
 	private Library getSongs() throws IOException, InterruptedException {
 		outStream.write("getSongs".getBytes());
 		Thread.sleep(200);
 		Socket tempSocket = new Socket(host, port + 100 + getClientID());
 		DataInputStream inPut = new DataInputStream(tempSocket.getInputStream());
 		byte[] bytestoRecieve = new byte[1024];
 		int size = inPut.read(bytestoRecieve);
 		String songs = new String();
 		Library tempLib = new Library("Temp");
 		while (size > 0) {
 			songs += new String(bytestoRecieve, 0, size);
 			size = inPut.read(bytestoRecieve);
 		}
 		System.out.println(songs);
 		String[] songsSeperated = songs.split("\\Q#");
 		for (String song : songsSeperated) {
 			tempLib.addSong(song);
 		}
 		tempSocket.close();
 		return tempLib;
 	}
 
 	public void treeRefresh() {
 
 		try {
 			tree.removeTreeSelectionListener(this);
 			tree.removeTreeWillExpandListener(this);
 			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
 					.getRoot();
 			clearTree(root, model);
 			int pos = 0;
 			for (Album alb : getSongs().getAlbums()) {
 				model.insertNodeInto(
 						new DefaultMutableTreeNode(alb.getAlbum()), root,
 						model.getChildCount(root));
 				for (Song son : alb.getSongs()) {
 					model.insertNodeInto(
 							new DefaultMutableTreeNode(son.getTitle()),
 							(MutableTreeNode) root.getChildAt(pos),
 							model.getChildCount(root.getChildAt(pos)));
 				}
 				pos++;
 			}
 			for (int r = 0; r < tree.getRowCount(); r++) {
 				tree.expandRow(r);
 			}
 			tree.addTreeSelectionListener(this);
 			tree.addTreeWillExpandListener(this);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public boolean isStopPlaying() {
 		return stopPlaying;
 	}
 
 	public void setStopPlaying(boolean stopPlaying) {
 		this.stopPlaying = stopPlaying;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public LibraryRefreshNotifier getTreeRefresher() {
 		return treeRefresher;
 	}
 
 	public void setTreeRefresher(LibraryRefreshNotifier treeRefresher) {
 		this.treeRefresher = treeRefresher;
 	}
 
 	public DataInputStream getIn() {
 		return inStream;
 	}
 
 	public void setIn(DataInputStream in) {
 		this.inStream = in;
 	}
 
 	public JTextField getTitleJTF() {
 		return this.titleJTF;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public DataOutputStream getOut() {
 		return outStream;
 	}
 
 	public PlayWavThread getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(PlayWavThread player) {
 		this.player = player;
 	}
 
 	public void setOut(DataOutputStream out) {
 		this.outStream = out;
 	}
 
 	public FileSendThreadClient getFileHandler() {
 		return fileUploader;
 	}
 
 	public void setFileHandler(FileSendThreadClient fileHandler) {
 		this.fileUploader = fileHandler;
 	}
 
 	public int getClientID() {
 		return clientID;
 	}
 
 	public void setClientID(int clientID) {
 		this.clientID = clientID;
 	}
 
 	public Socket getSocket() {
 		return socket;
 	}
 
 	public void setSocket(Socket socket) {
 		this.socket = socket;
 	}
 
 	public void treeWillCollapse(TreeExpansionEvent tee) {
 		tree.setSelectionPath(tee.getPath());
 	}
 
 	public void treeWillExpand(TreeExpansionEvent tee) {
 		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tee.getPath()
 				.getLastPathComponent();
 		System.out.println("will expand node: " + dmtn.getUserObject()
 				+ " whose path is: " + tee.getPath());
 	}
 
 	// This method makes fields update when a node is clicked on
 	public void valueChanged(TreeSelectionEvent e) {
 		try {
 			tree.removeTreeSelectionListener(this);
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
 					.getLastSelectedPathComponent();
 			String nodeLabel = (String) node.getUserObject();
 			setFields(nodeLabel);
 		} catch (Exception ex) {
 		}
 		tree.addTreeSelectionListener(this);
 	}
 
 	// This function is to set fields according to a song object
 	private void setFields(String label) throws UnknownHostException,
 			IOException, InterruptedException {
 		Song song = getSong(label);
 		if (!(song == null)) {
 			this.titleJTF.setText(song.getTitle());
 			this.albumJTF.setText(song.getAlbum());
 			this.authorJTF.setText(song.getAuthor());
 		} else {
 			// This is so if a song had be selected and the main lirary nod is
 			// selected the fields are cleared.
 			this.titleJTF.setText("");
 			this.albumJTF.setText("");
 			this.authorJTF.setText("");
 		}
 
 	}
 
 	private Song getSong(String label) throws IOException {
 		outStream.write("aSong".getBytes());
 		outStream.write(label.getBytes());
 		Socket tempSocket = new Socket(host, port + 10 + clientID);
 		DataInputStream inPut = new DataInputStream(tempSocket.getInputStream());
 		byte[] bytestoRecieve = new byte[1024];
 		int size = inPut.read(bytestoRecieve);
 		String song = new String();
 		while (size > 0) {
 			song += new String(bytestoRecieve, 0, size);
 			size = inPut.read(bytestoRecieve);
 		}
 		tempSocket.close();
 		return new Song(song);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Exit")) {
 			exit();
 			// The following is to Serialize a library when pressing save.
 		} else if (e.getActionCommand().equals("Save")) {
 			save();
 		}
 		// The following is to Deserialize a library when pressing restore.
 		else if (e.getActionCommand().equals("Restore")) {
 			restore();
 		}
 		// The following is what adds a song to the library it then rebuilds the
 		// tree
 		else if (e.getActionCommand().equals("Add")) {
 			add();
 		}
 		// The following is what removes a song or album from the library
 		else if (e.getActionCommand().equals("Remove")) {
 			remove();
 		}
 		// Plays the Selected Song
 		else if (e.getActionCommand().equals("Play")) {
 			play();
 		}
 		// Refresh the tree
 		else if (e.getActionCommand().equals("Tree Refresh")) {
 			treeRefresh();
 
 		}
 
 	}
 
 	private void add() {
 		try {
 			JFileChooser chooser = new JFileChooser();
 			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
 			FileNameExtensionFilter filter = new FileNameExtensionFilter(
 					"Wav files", "wav");
 			chooser.setFileFilter(filter);
 			String song = this.titleJTF.getText() + "$"
 					+ this.authorJTF.getText() + "$" + this.albumJTF.getText();
 			// if (title.equalsIgnoreCase("")) {
 			// title = "_";
 			// }
 			// if (author.equalsIgnoreCase("")) {
 			// author = "_";
 			// }
 			// if (album.equalsIgnoreCase("")) {
 			// album = "_";
 			// }
 			int returnVal = chooser.showOpenDialog(this);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				String file = chooser.getSelectedFile().getAbsolutePath();
 				outStream.write("add".getBytes());
 				outStream.write(song.getBytes());
 				this.fileUploader = new FileSendThreadClient(file, this,
 						new Socket(host, (port + 1)));
 				this.fileUploader.start();
 			}
 
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	private void play() {
 		try {
 			// get the currently selected node in the tree.
 			// if the user hasn't already selected a node for which
 			// there must be a wav file then exit ungracefully!
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
 					.getLastSelectedPathComponent();
 			String nodeLabel = (String) node.getUserObject();
 			if (getPlayer() != null && getPlayer().isAlive()) {
 				stopPlaying = true;
 				Thread.sleep(500); // give the thread time to complete
 				stopPlaying = false;
 			}
 			if (!(getSongs().findSong(nodeLabel).getFile().equalsIgnoreCase(""))) {
 				outStream.write("play".getBytes());
 				outStream.write(nodeLabel.getBytes());
 				FileRecieveThreadClient fileRecieveSong = new FileRecieveThreadClient(
 						new Socket(host, port + 2), nodeLabel, this);
 				fileRecieveSong.start();
 			}
 		} catch (InterruptedException | IOException ex) { // sleep may throw
 															// this
 			// exception
 			System.out.println("MusicThread sleep was interrupted.");
 		}
 	}
 
 	private void remove() {
 		try {
 			outStream.write("remove".getBytes());
 			outStream.write(this.titleJTF.getText().getBytes());
 			// if (!this.titleJTF.getText().equalsIgnoreCase("")) {
 			// out.write(this.titleJTF.getText().getBytes());
 			// } else {
 			// out.write(this.albumJTF.getText().getBytes());
 			// }
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	private void restore() {
 		try {
 			outStream.write("restore".getBytes());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	private void save() {
 		try {
 			outStream.write("save".getBytes());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	private void exit() {
 		try {
 			outStream.write("exit".getBytes());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		System.exit(0);
 	}
 
 	private void clearTree(DefaultMutableTreeNode root, DefaultTreeModel model) {
 		tree.removeTreeSelectionListener(this);
 		tree.removeTreeWillExpandListener(this);
 		try {
 			DefaultMutableTreeNode next = null;
 			int subs = model.getChildCount(root);
 			for (int k = subs - 1; k >= 0; k--) {
 				next = (DefaultMutableTreeNode) model.getChild(root, k);
 				model.removeNodeFromParent(next);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		tree.addTreeSelectionListener(this);
 		tree.addTreeWillExpandListener(this);
 	}
 
 	@SuppressWarnings("unused")
 	public static void main(String args[]) {
 		try {
 			String name = "Music Library";
 			if (args.length >= 1) {
 				name = args[0];
 			}
 			MusicApp ltree = new MusicApp(name);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 }
