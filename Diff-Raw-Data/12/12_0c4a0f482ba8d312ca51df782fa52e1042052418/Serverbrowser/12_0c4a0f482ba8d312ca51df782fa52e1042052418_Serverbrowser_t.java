 package gui;
 
 import game.Game;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.Timer;
 
 import network.Discover;
 import network.Server;
 
 public class Serverbrowser extends JFrame implements ActionListener, WindowListener {
 	private Discover discover;
 	private Object[] old_list;
 	private JButton connectButton;
 	private JList list;
 	private JScrollPane listScroller;
 	private JPanel panel;
 	private Container parentWindow;
 
 	public Serverbrowser(Container frame) {
 		super("Serverbrowser");
 		this.parentWindow = frame;
 		this.setLocationRelativeTo(this.parentWindow);
 
 		this.panel = new JPanel();
 		this.panel.setLayout(new GridBagLayout());
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10, 10, 5, 5);
 
 		this.discover = new Discover();
 		this.old_list = this.discover.servers.toArray();
 
 		this.list = new JList(this.old_list);
 
 		this.list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		this.list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		// this.list.setVisibleRowCount(-1);
 
 		this.listScroller = new JScrollPane(this.list);
 
 		this.connectButton = new JButton("Connect");
 		this.connectButton.addActionListener(this);
 
 		c.gridx = 1;
 		c.gridy = 0;
 		c.ipadx = 120;
 		c.ipady = 80;
 
 		this.panel.add(this.listScroller, c);
 
 		c.gridy = 1;
 		c.ipady = 0;
 		this.panel.add(this.connectButton, c);
 
 		this.setContentPane(this.panel);
 
 		this.pack();
 		this.setVisible(true);
 		Timer t = new Timer(2000, new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Object[] l = Serverbrowser.this.discover.servers.toArray();
 				if (Serverbrowser.this.old_list.length != l.length) {
 					Serverbrowser.this.list.setListData(l);
 					Serverbrowser.this.old_list = l;
 				}
 			}
 		});
 		t.start();
 		this.addWindowListener(this);
 		this.discover.start();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == this.connectButton) {
 			if (this.list.getSelectedValue() != null) {
 				Game.getInstance().connectServer((Server) this.list.getSelectedValue(), this.parentWindow);
 				this.discover.running = false;
 				this.dispose();
 			} else {
				JOptionPane.showMessageDialog(this, "Bitte Server auswählen", "Fehler!", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 		this.discover.running = false;
 
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		this.discover.running = false;
 
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 }
