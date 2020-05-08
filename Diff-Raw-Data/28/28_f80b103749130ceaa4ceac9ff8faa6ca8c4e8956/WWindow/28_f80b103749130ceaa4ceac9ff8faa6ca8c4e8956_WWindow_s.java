 import java.awt.Color;
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.ListCellRenderer;
 import javax.swing.WindowConstants;
 
 
 public class WWindow extends JFrame{
 	private JButton ButtonClear;
 	private JButton ButtonLogin;
 	private JButton ButtonLogout;
 	private JButton ButtonPen;
 	private ButtonGroup GroupLogin;
 	private JLabel LabelHost;
 	private JLabel LabelName;
 	private WCanvas PaneDrawArea;
 	private WCanvas MyPaneDrawArea;
 	private JPanel PaneDrawing;
 	private JPanel PaneLogin;
 	private JRadioButton RButtonRMI;
 	private JRadioButton RButtonSocket;
 	private JRadioButton RButtonRPC;
 	private JTextField TextHost;
 	private JTextField TextName;
 	private JList UserItemList;
 	private JButton jButton3;
 	private JButton jButton4;
 	private JScrollPane jScrollPane1;
 	protected Whiteboard w;
 
 	public void sendMessage(String m) {
 		w.sendMessage(m);
 	}
 
 	public WWindow (Whiteboard w) {
 		setTitle("Log in");
 
 		this.w = w;
 		initComponents();
 		PaneDrawing.setVisible(false);
 		PaneLogin.setVisible(true);
 		this.setVisible(true);
 	}
 	
 	public void select(Integer id) { //show id's whiteboard
 		if (id == 2) {
 			PaneDrawArea = MyPaneDrawArea;
 		} else {
 			MyPaneDrawArea = PaneDrawArea;
 		}
 	}
 	
 	public void populateUserList(ArrayList<WUser> Users) {
		//UserItemList = new JList(Users.toArray());
		UserItemList.setListData(Users.toArray());
		UserItemList.setCellRenderer(new UserCellRenderer());
		jScrollPane1.setViewportView(UserItemList);
		jScrollPane1.revalidate();
		jScrollPane1.repaint();
 	}
 
 	private void initComponents() {
 		UserItemList = new JList();
 		GroupLogin = new ButtonGroup();
 		PaneLogin = new JPanel();
 		ButtonLogin = new JButton();
 		RButtonRMI = new JRadioButton();
 		RButtonSocket = new JRadioButton();
 		RButtonRPC = new JRadioButton();
 		TextHost = new JTextField();
 		TextName = new JTextField();
 		LabelHost = new JLabel();
 		LabelName = new JLabel();
 		PaneDrawing = new JPanel();
		jScrollPane1 = new JScrollPane();
 		ButtonPen = new JButton();
 		ButtonClear = new JButton();
 		jButton3 = new JButton();
 		jButton4 = new JButton();
 		ButtonLogout = new JButton();
 		PaneDrawArea = new WCanvas(this);
 
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		setResizable(false);
 
 		ButtonLogin.setText("Login");
 		ButtonLogin.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				PaneDrawArea.clear();
 
 
 				String where = TextHost.getText();
 				String how = null;
 				String nick = TextName.getText();
 				for (Enumeration<AbstractButton> e = GroupLogin.getElements(); e.hasMoreElements();) {
 					JRadioButton b = (JRadioButton)e.nextElement();
 					if (b.getModel() == GroupLogin.getSelection()) {
 						how = b.getText(); //which radio button
 					}
 				}
 
 				w.login(how, where, nick);
 				
 				//select Self from user list
 				//enable drawing for self
 				PaneDrawArea.drawingEnabled(true);
 				PaneLogin.setVisible(false);
 				PaneDrawing.setVisible(true);
 				setTitle("Whiteboard");
 			}
 		});
 
 		GroupLogin.add(RButtonRMI);
 		RButtonRMI.setText("RMI");
 
 		GroupLogin.add(RButtonRPC);
 		RButtonRPC.setText("RPC");
 
 		GroupLogin.add(RButtonSocket);
 		RButtonSocket.setSelected(true);
 		RButtonSocket.setText("SOCKET");
 
 		LabelHost.setText("Host:");
 		LabelName.setText("Username:");
 
 		GroupLayout PaneLoginLayout = new GroupLayout(PaneLogin);
 		PaneLogin.setLayout(PaneLoginLayout);
 		PaneLoginLayout.setHorizontalGroup(
 				PaneLoginLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(PaneLoginLayout.createSequentialGroup()
 						.addGap(203, 203, 203)
 						.addComponent(LabelHost)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addGroup(PaneLoginLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 								.addComponent(ButtonLogin)
 								.addComponent(TextHost, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
 								.addComponent(TextName, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
 								.addComponent(RButtonSocket)
 								.addComponent(RButtonRMI)
 								.addComponent(RButtonRPC))
 								.addContainerGap(186, Short.MAX_VALUE))
 				);
 		PaneLoginLayout.setVerticalGroup(
 				PaneLoginLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, PaneLoginLayout.createSequentialGroup()
 						.addContainerGap(176, Short.MAX_VALUE)
 						.addGroup(PaneLoginLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 								.addComponent(TextHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(LabelHost))
 								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(PaneLoginLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 										.addComponent(TextName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 										.addComponent(LabelHost)
 										)
 										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(RButtonRMI)
 										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(RButtonSocket)
 										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(RButtonRPC)
 										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(ButtonLogin))
 				);
 
 		ButtonPen.setText("Pen");
 
 		ButtonClear.setText("Clear");
 		ButtonClear.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				PaneDrawArea.clear();
 			}
 		});
 
 		jButton3.setText("jButton3");
 
 		jButton4.setText("jButton4");
 
 		ButtonLogout.setText("Logout");
 		ButtonLogout.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				sendMessage("exit");
 				setTitle("Select a Technology");
 				PaneDrawing.setVisible(false);
 				PaneLogin.setVisible(true);
 			}
 		});
 
 		GroupLayout PaneDrawAreaLayout = new GroupLayout(PaneDrawArea);
 		PaneDrawArea.setLayout(PaneDrawAreaLayout);
 		PaneDrawAreaLayout.setHorizontalGroup(PaneDrawAreaLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 421, Short.MAX_VALUE));
 		PaneDrawAreaLayout.setVerticalGroup(PaneDrawAreaLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 353, Short.MAX_VALUE));
 
 		GroupLayout PaneDrawingLayout = new GroupLayout(PaneDrawing);
 		PaneDrawing.setLayout(PaneDrawingLayout);
 		PaneDrawingLayout.setHorizontalGroup(
 				PaneDrawingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(PaneDrawingLayout.createSequentialGroup()
 						.addComponent(PaneDrawArea, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addGroup(PaneDrawingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 								.addGroup(GroupLayout.Alignment.TRAILING, PaneDrawingLayout.createSequentialGroup()
 										.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
 										.addContainerGap())
 										.addGroup(GroupLayout.Alignment.TRAILING, PaneDrawingLayout.createSequentialGroup()
 												.addGroup(PaneDrawingLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 														.addComponent(ButtonClear)
 														.addComponent(ButtonPen)
 														.addComponent(jButton3)
 														.addComponent(jButton4)
 														.addComponent(ButtonLogout))
 														.addGap(41, 41, 41))))
 				);
 		PaneDrawingLayout.setVerticalGroup(
 				PaneDrawingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(PaneDrawingLayout.createSequentialGroup()
 						.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(ButtonPen)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(ButtonClear)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(jButton3)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(jButton4)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(ButtonLogout)
 						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 						.addComponent(PaneDrawArea, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(PaneDrawing, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addComponent(PaneLogin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(PaneDrawing, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(PaneLogin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
 				);
 
 		pack();
 	}
 
	class UserCellRenderer extends JLabel implements ListCellRenderer {
 		private final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
 
 		public UserCellRenderer() {
 			setOpaque(true);
 			setIconTextGap(12);
 		}
 
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			WUser u = (WUser) value;
 			select(u.getId());
 			if (isSelected) {
 				setBackground(HIGHLIGHT_COLOR);
 				setForeground(Color.white);
 			} else {
 				setBackground(Color.white);
 				setForeground(Color.black);
 			}
 			return this;
 		}
	}
 }
