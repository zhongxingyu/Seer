 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.Line2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import javax.swing.*;
 
 public class WClient extends JFrame {
 	private JButton ButtonClear;
 	private JButton ButtonLogin;
 	private JButton ButtonLogout;
 	private JButton ButtonPen;
 	private ButtonGroup GroupLogin;
 	private JLabel LabelHost;
 	private DrawingCanvas PaneDrawArea;
 	private JPanel PaneDrawing;
 	private JPanel PaneLogin;
 	private JRadioButton RButtonSOAP;
 	private JRadioButton RButtonSocket;
 	private JRadioButton RButtonX;
 	private JTextField TextHost;
 	private JList UserItemList;
 	private JButton jButton3;
 	private JButton jButton4;
 	private JScrollPane jScrollPane1;
 	private ArrayList<User> Users = new ArrayList<User>();
 	private boolean drawingEnabled = false;
 	private Graphics2D g;
 	private BufferedImage buff;
 
 	public static void main(String args[]) {
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			public void run() {new WClient().setVisible(true);}
 		});
 	}
 
 	public WClient() {
 		super("Select a Technology");
 
 		initComponents();
 		PaneDrawing.setVisible(false);
 		PaneLogin.setVisible(true);
 	}
 
 	private void populateUserList() {
 		UserItemList = new JList(Users.toArray());
 		UserItemList.setCellRenderer(new UserCellRenderer());
 	}
 
 	private void initComponents() {
 		GroupLogin = new ButtonGroup();
 		PaneLogin = new JPanel();
 		ButtonLogin = new JButton();
 		RButtonSOAP = new JRadioButton();
 		RButtonSocket = new JRadioButton();
 		RButtonX = new JRadioButton();
 		TextHost = new JTextField();
 		LabelHost = new JLabel();
 		PaneDrawing = new JPanel();
 		jScrollPane1 = new JScrollPane();
 		ButtonPen = new JButton();
 		ButtonClear = new JButton();
 		jButton3 = new JButton();
 		jButton4 = new JButton();
 		ButtonLogout = new JButton();
 		PaneDrawArea = new DrawingCanvas();
 
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		setResizable(false);
 
 		ButtonLogin.setText("Login");
 		ButtonLogin.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
				PaneDrawArea.clear();
				
 				TextHost.getText(); //which host
				
 				for (Enumeration<AbstractButton> e = GroupLogin.getElements(); e.hasMoreElements();) {
 					JRadioButton b = (JRadioButton)e.nextElement();
 					if (b.getModel() == GroupLogin.getSelection()) {
						//b; //which radio button
 					}
 				}
 				//notify server
 				//Server should add user to user list
 
 				Users.clear();
 				Users.add(new User(1,"ABC")); //test user
 				//if success logged on{
 				//get user list from server
 				populateUserList();
 				jScrollPane1.setViewportView(UserItemList);
 				//select Self from user list
 
 				//enable drawing for self
 				drawingEnabled = true;
 				PaneLogin.setVisible(false);
 				PaneDrawing.setVisible(true);
 				setTitle("Whiteboard");
 			}
 		});
 
 		GroupLogin.add(RButtonSOAP);
 		RButtonSOAP.setText("SOAP");
 
 		GroupLogin.add(RButtonX);
 		RButtonX.setText("X");
 
 		GroupLogin.add(RButtonSocket);
 		RButtonSocket.setSelected(true);
 		RButtonSocket.setText("SOCKET");
 
 		LabelHost.setText("Host:");
 
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
 								.addComponent(RButtonSocket)
 								.addComponent(RButtonSOAP)
 								.addComponent(RButtonX))
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
 								.addComponent(RButtonSOAP)
 								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 								.addComponent(RButtonSocket)
 								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 								.addComponent(RButtonX)
 								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 								.addComponent(ButtonLogin)
 								.addGap(77, 77, 77))
 				);
 		
 		ButtonPen.setText("Pen");
 
 		ButtonClear.setText("Clear"); //FIXME Doesn't clear on action for somereason
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
 
 				//notify server
 				//re-initialise local variables
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
 			User u = (User) value;
 			setText(u.getName());
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
 
 	class DrawingCanvas extends JPanel {
 		int x1, y1, x2, y2;
 		BasicStroke wideStroke = new BasicStroke(8.0f);
 
 		public DrawingCanvas() {
 			buff = new BufferedImage(500, 400, BufferedImage.TYPE_INT_ARGB);
 			g = buff.createGraphics();
 			g.setStroke(wideStroke);
 			clear();
 
 			addMouseListener(new MyMouseListener());
 			addMouseMotionListener(new MyMouseMotionListener());
 		}
 		public void paintComponent(Graphics g) {
 			super.paintComponents(g);
 			g.drawImage(buff, 0, 0, this);
 		}
 
 		public void clear() {
 			g.setPaintMode();
 			g.setColor(Color.white);
 			g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
 			g.setColor(Color.black); //set pen back to black
 			repaint();
 		}
 
 		class MyMouseListener extends MouseAdapter {
 			public void mousePressed(MouseEvent e) {
 				if (drawingEnabled) {
 					x1 = e.getX();
 					y1 = e.getY();
 					x2 = e.getX();
 					y2 = e.getY();
 					g.draw(new Line2D.Double(x1, y1, x2, y2));
 					repaint();
 				}
 			}
 			/*public void mouseReleased(MouseEvent e) {
 				repaint();
 			}*/
 			public void mouseClicked(MouseEvent e) {
 				if (drawingEnabled) {
 					x1 = e.getX();
 					y1 = e.getY();
 					x2 = e.getX();
 					y2 = e.getY();
 					g.draw(new Line2D.Double(x1, y1, x2, y2));
 					repaint();
 				}
 			}
 		}
 		class MyMouseMotionListener extends MouseMotionAdapter {
 			public void mouseDragged(MouseEvent e) {
 				if (drawingEnabled) {
 					x1 = e.getX();
 					y1 = e.getY();
 					x2 = e.getX();
 					y2 = e.getY();
 					g.draw(new Line2D.Double(x1, y1, x2, y2));
 					repaint();
 				}
 			}
 			/*public void mouseMoved(MouseEvent e) {
 				repaint();
 			}*/
 		}
 	}
 }
