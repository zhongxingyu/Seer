 package gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import logic.Debt;
 import logic.User;
 
 import session.Session;
 
 public class DebtListPanel extends JPanel{
 
 	private GridBagConstraints c;
 	private JButton plus, minus, pending;
 	private Component plusTable, minusTable, pendingFromTable, pendingToTable;
 	private boolean plusIsShowing, minusIsShowing, pendingIsShowing;
 	private final int plusYCoord = 1, minusYCoord = 3, pendingYCoord = 5;
 	private JLabel pendingToLabel = new JLabel("Pending requests:"), pendingFromLabel = new JLabel("Your pending requests:");
 	private ClickListener clickListener = new ClickListener();
 	
 	public DebtListPanel() {
 		super(new GridBagLayout());
 		plusIsShowing = false;
 		minusIsShowing = false;
 		pendingIsShowing = false;
 		
 		c = new GridBagConstraints();
 		c.anchor = GridBagConstraints.NORTHWEST;
 		c.gridx = 0;
 		c.gridy = 0;
 		plus = new JButton("+");
 		plus.setForeground(Color.green);
 		plus.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
 		plus.setBorderPainted(false);
 		plus.setFocusable(false);
 		plus.setContentAreaFilled(false);
 		add(plus,c);
 		
 		c.gridy++;
 		c.gridy++;
 		minus = new JButton("-");
 		minus.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
 		minus.setForeground(Color.red);
 		minus.setContentAreaFilled(false);
 		minus.setBorderPainted(false);
 		minus.setFocusable(false);
 		add(minus, c);
 		
 		c.gridy++;
 		c.gridy++;
 		pending = new JButton("Pending (" + Session.session.getUser().getNumberOfPendingDebts() + ")");
 		pending.setContentAreaFilled(false);
 		pending.setBorderPainted(false);
 		pending.setFocusable(false);
 		pending.setFont(new Font(null, Font.PLAIN, 15));
 		add(pending, c);
 		
 		ButtonListener listener = new ButtonListener(Session.session.getUser());
 		plus.addActionListener(listener);
 		minus.addActionListener(listener);
 		pending.addActionListener(listener);
 	}
 	
 	class ClickListener implements MouseListener {
 		@Override
 		public void mouseReleased(MouseEvent arg0) {}
 		@Override
 		public void mousePressed(MouseEvent arg0) {}
 		@Override
 		public void mouseExited(MouseEvent arg0) {}
 		@Override
 		public void mouseEntered(MouseEvent arg0) {}
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if(e.getSource() == plusTable) {
 				if(minusTable instanceof JScrollPane) {
 					// Fix asap
					((JTable)((JScrollPane) minusTable).getComponent(0)).clearSelection();
 				}
 			}
 			System.out.println(e.getSource());
 		}
 	}
 	
 	class ButtonListener implements ActionListener {
 
 		private User user;
 		
 		public ButtonListener(User user) {
 			this.user = user;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			c.gridx = 0;
 			List<String> extraValues = new ArrayList<String>();
 			if(e.getSource() == plus) {
 				if(plusIsShowing) {
 					remove(plusTable);
 					plusIsShowing = false;
 				} else {
 					extraValues.add("From");
 					c.gridy = plusYCoord;
 					List<Debt> debts = new ArrayList<Debt>();
 					for (int i = 0; i < user.getNumberOfConfirmedDebts(); i++) {
 						if(user.getConfirmedDebt(i).getTo() == user) {
 							debts.add(user.getConfirmedDebt(i));
 						}
 					}
 					plusIsShowing = true;
 					plusTable = getDebtList(debts, extraValues);
 					add(plusTable, c);
 				}
 			} else if(e.getSource() == minus) {
 				if(minusIsShowing) {
 					remove(minusTable);
 					minusIsShowing = false;
 				} else {
 					extraValues.add("To");
 					c.gridy = minusYCoord;
 					List<Debt> debts = new ArrayList<Debt>();
 					for (int i = 0; i < user.getNumberOfConfirmedDebts(); i++) {
 						if(user.getConfirmedDebt(i).getFrom() == user) {
 							debts.add(user.getConfirmedDebt(i));
 						}
 					}
 					minusIsShowing = true;
 					minusTable = getDebtList(debts, extraValues);
 					add(minusTable, c);
 				}
 			} else if(e.getSource() == pending){
 				if(pendingIsShowing) {
 					remove(pendingFromTable);
 					remove(pendingToTable);
 					remove(pendingFromLabel);
 					remove(pendingToLabel);
 					pendingIsShowing = false;
 				} else {
 					extraValues.add("From");
 					extraValues.add("To");
 					c.gridy  = pendingYCoord;
 					List<Debt> debtsFromMe = new ArrayList<Debt>();
 					List<Debt> debtsToMe = new ArrayList<Debt>();
 					for (int i = 0; i < user.getNumberOfPendingDebts(); i++) {
 						if(user.getPendingDebt(i).getRequestedBy() == user) {
 							debtsToMe.add(user.getPendingDebt(i));
 						} else {
 							debtsFromMe.add(user.getPendingDebt(i));
 						}
 					}
 					pendingIsShowing = true;
 					add(pendingToLabel, c);
 					c.gridy++;
 					pendingFromTable = getDebtList(debtsFromMe, extraValues);
 					add(pendingFromTable, c);
 					c.gridy++;
 					add(pendingFromLabel, c);
 					c.gridy++;
 					pendingToTable = getDebtList(debtsToMe, extraValues);
 					add(pendingToTable, c);
 				}
 			}
 //			repaint();
 			Session.session.fixFrame();
 		}
 		
 	}
 	
 	public Component getDebtList(List<Debt> debts, List<String> extraValues) {
 		if(debts.isEmpty()) {
 			return new JLabel("None");
 		} else {
 //			String[] columnNames = {"What", "Amount"/*, "Edit?", "Done?"*/};
 			String[] columnNames = new String[2 + extraValues.size()];
 			columnNames[1] = "What";
 			columnNames[0] = "Amount";
 			for (int i = 0; i < extraValues.size(); i++) {
 				columnNames[i + 2] = extraValues.get(i);
 			}
 			Object[][] rowData = new Object[debts.size()][columnNames.length];
 			for (int i = 0; i < debts.size(); i++) {
 				Debt d = debts.get(i);
 				rowData[i][1] = d.getWhat();
 				rowData[i][0] = d.getAmount();
 				if(extraValues.size() == 2) {
 					rowData[i][2] = d.getFrom().getUsername();
 					rowData[i][3] = d.getTo().getUsername();
 				} else {
 					rowData[i][2] = extraValues.get(i).equals("From") ? d.getFrom().getUsername() : d.getTo().getUsername();
 				}
 			}
 			JTable table = new JTable(new DefaultTableModel(rowData, columnNames) {
 				@Override
 				public boolean isCellEditable(int row, int column) {
 					return false;
 				}
 			});
 			table.setBackground(this.getBackground());
 			table.addMouseListener(clickListener);
 //			table.setEnabled(false);
 			JScrollPane scrollPane = new JScrollPane(table);
 			scrollPane.setPreferredSize(new Dimension(400, debts.size() < 8 ? table.getRowHeight()*(1 + debts.size()) + 4: table.getRowHeight()*9));
 			scrollPane.setBorder(BorderFactory.createEmptyBorder());
 			return scrollPane;
 		}
 	}
 	
 	public static void main(String[] args) {
 		User u = new User("Stian", "123");
 		User u2 = new User("Arne", "qazqaz");
 		u.addPendingDebt(new Debt(100, "NOK", u2, u, "Test", u));
 		u.addPendingDebt(new Debt(20, "NOK", u, u2, "Potetgull + brus", u2));
 		u.addConfirmedDebt(new Debt(2, "Slaps", u, u2, "Test", u));
		u.addConfirmedDebt(new Debt(10, "asd", u2, u, "Test", u));
 		Session.session.setUser(u);
 		
 		JFrame frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 		frame.add(new DebtListPanel());
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 //		frame.setSize(frame.getWidth() + 100, frame.getHeight() + 100);
 		Session.session.setFrame(frame);
 	}
 }
