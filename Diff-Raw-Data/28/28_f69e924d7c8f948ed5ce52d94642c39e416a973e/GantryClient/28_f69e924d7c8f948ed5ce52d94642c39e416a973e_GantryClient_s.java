 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 /** shows the gantry robot and its interaction with all 4 feeders (communicate with server) */
 public class GantryClient extends JFrame implements ActionListener,
 		Networked {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * @param args
 	 */
 	/** use this variable to send Msg classes to the server (communicate with server) */
 	private NetComm netComm;
 	/** connection panel for connecting to the server  */
 	private ConnectPanel conp;
 	/** cardlayout to switch between connection panel and gantry manager */
 	private CardLayout cardlayout;
 	/** GantryManager variable */
 	private GantryManager gantryMan;
 	
 	/** initialize variables */
 	public GantryClient() {
 		Painter.loadImages();
 		cardlayout = new CardLayout();
 		conp = new ConnectPanel(this);
 		gantryMan = new GantryManager(this);
 		setLayout(cardlayout);
 		add(conp, "connect");
 		add(gantryMan, "Manager");
 		setTitle("Gantry Manager");
 		setSize(FactoryPainter.getAreaSize(FactoryPainter.FactoryArea.GANTRY_MANAGER));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setVisible(true);
 		new Timer(GantryGraphics.UPDATE_RATE, this).start();
 	}
 	
 	public static void main(String[] args) {
 		new GantryClient();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getSource() == conp) { // connect to server
 			try {
 				netComm = new NetComm(new Socket(e.getActionCommand(),
 						Server.PORT), this);
 				netComm.write(new FactoryStateMsg());
 
 			} catch (Exception exception) {
 				netComm = null;
 				conp.setActionError("Could not connect to server; check that it was entered correctly");
 			}
 		}
 		
 		else if (e.getSource() instanceof Timer) {
 			repaint();
 		}
 	}
 
 
 	@Override
 	public void msgReceived(Object msgObj, NetComm sender) {
 
 		if (msgObj instanceof CloseConnectionMsg) { // handles CloseConnectionMsg
 			netComm.close();
 			netComm = null;
 			conp.reset();
 			conp.setActionMsg("Unexpectedly disconnected from server");
 
 		} else if (msgObj instanceof FactoryStateMsg) {
 			gantryMan.setFactoryState((FactoryStateMsg) msgObj);
 			cardlayout.last(this.getContentPane());
 		}
 
 		else if (msgObj instanceof FactoryUpdateMsg) {
 			gantryMan.update((FactoryUpdateMsg) msgObj);
 		} else if (msgObj instanceof StringMsg) {
 			System.out.println(((StringMsg) msgObj).message);
 		} else {
 			System.out.println("Warning: received unknown message " + msgObj);
 		}
 	}
 
 	public boolean isInteger(String number) {
 		try {
 			Integer.parseInt(number);
 			return true;
 		} catch (Exception e) {
 			return false;
 
 		}
 
 	}
 	
 	public NetComm getCom(){
 		return netComm;
 	}
 
}
