 package ubc.cpsc544;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JSeparator;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class Ideas {
 
 	private JFrame frame;
 	private JTextField txtSearch;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Ideas window = new Ideas();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Ideas() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setBounds(100, 100,  1024, 768);
 		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(21, 372, 694, 12);
 		frame.getContentPane().add(separator);
 		
 		final JLabel label_5 = new JLabel("4");
 		label_5.setFont(new Font("Lucida Grande", Font.PLAIN, 30));
 		label_5.setBounds(68, 269, 59, 43);
 		frame.getContentPane().add(label_5);
 		
		final JLabel label_6 = new JLabel("-2");
 		label_6.setFont(new Font("Lucida Grande", Font.PLAIN, 30));
		label_6.setBounds(60, 493, 55, 43);
 		frame.getContentPane().add(label_6);
 		
 		final JLabel lblNewLabel = new JLabel("");
 		lblNewLabel.addMouseListener(new MouseAdapter() {
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (label_5.getText().equals("3"))
 					label_5.setText(Integer.toString(4));
 				else
 				label_5.setText(Integer.toString(5));
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				lblNewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			}
 		});
 		lblNewLabel.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/UpArrow.png")));
 		lblNewLabel.setBounds(60, 241, 34, 16);
 		frame.getContentPane().add(lblNewLabel);
 		
 		final JLabel label_2 = new JLabel("");
 		label_2.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (label_5.getText().equals("5"))
 					label_5.setText(Integer.toString(4));
 				else
 				label_5.setText(Integer.toString(3));
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				label_2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			}
 		});
 		label_2.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/DownArrow.png")));
 		label_2.setBounds(62, 323, 34, 16);
 		frame.getContentPane().add(label_2);
 		
 		final JLabel label = new JLabel("");
 		label.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
				if(label_6.getText().equals("-3"))
					label_6.setText(Integer.toString(-2));
 				else
				label_6.setText(Integer.toString(-1));
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			}
 		});
 		label.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/UpArrow.png")));
 		label.setBounds(61, 465, 34, 16);
 		frame.getContentPane().add(label);
 		
 		final JLabel label_3 = new JLabel("");
 		label_3.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
				if(label_6.getText().equals("-1"))
					label_6.setText(Integer.toString(-2));
 				else
				label_6.setText(Integer.toString(-3));
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				label_3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			}
 		});
 		label_3.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/DownArrow.png")));
 		label_3.setBounds(63, 548, 34, 16);
 		frame.getContentPane().add(label_3);
 		
 		JSeparator separator_1 = new JSeparator();
 		separator_1.setOrientation(SwingConstants.VERTICAL);
 		separator_1.setBounds(520, 197, 12, 162);
 		frame.getContentPane().add(separator_1);
 		
 		JSeparator separator_2 = new JSeparator();
 		separator_2.setOrientation(SwingConstants.VERTICAL);
 		separator_2.setBounds(520, 421, 12, 162);
 		frame.getContentPane().add(separator_2);
 		
 		final JLabel lblNewLabel_1 = new JLabel("Optimal Thermostat setting");
 		lblNewLabel_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		lblNewLabel_1.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Comment com = new Comment();
 				com.setVisible(true);
 			}
 		});
 		lblNewLabel_1.setFont(new Font("Lucida Grande", Font.BOLD, 16));
 		lblNewLabel_1.setBounds(66, 197, 343, 27);
 		frame.getContentPane().add(lblNewLabel_1);
 		
 		final JTextPane txtpnIThinkThe = new JTextPane();
 		txtpnIThinkThe.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Comment com = new Comment();
 				com.setVisible(true);
 			}
 		});
 		txtpnIThinkThe.setText("Lower your thermostat by 4 - 5 degrees Celsius (7 - 9 degrees Fahrenheit) while you're sleeping at night and when no one is at home.\n\n\t\t   \n\t\t  Edited Oct 23 at 16:35 ");
 		txtpnIThinkThe.setBounds(126, 242, 319, 111);
 		frame.getContentPane().add(txtpnIThinkThe);
 		
 		final JTextPane txtpnWeShouldLeave = new JTextPane();
 		txtpnWeShouldLeave.setText("Switch to cold when doing your laundry. 85  90% of the energy used to wash your clothes is used to heat the water.\n\n\t\t   \n\t\t  Edited Nov 1 at 13:36 ");
 		txtpnWeShouldLeave.setBounds(126, 468, 319, 107);
 		frame.getContentPane().add(txtpnWeShouldLeave);
 		
 		final JLabel lblSuggestionForEnegy = new JLabel("Use cold water for Laundary");
 		lblSuggestionForEnegy.setFont(new Font("Lucida Grande", Font.BOLD, 16));
 		lblSuggestionForEnegy.setBounds(61, 421, 319, 27);
 		frame.getContentPane().add(lblSuggestionForEnegy);
 		
 		final JLabel label_1 = new JLabel("");
 		label_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		label_1.addMouseListener(new MouseAdapter() {
 //			@Override
 //			public void mouseReleased(MouseEvent e) {
 //				Profile pikachu = new Profile();
 //				pikachu.setVisible(true);
 //
 //			}
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Profile pikachu = new Profile();
 				pikachu.setVisible(true);
 			}
 		});
 		label_1.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/Picachu.png")));
 		label_1.setBounds(612, 238, 103, 101);
 		frame.getContentPane().add(label_1);
 		
 		final JLabel lblPatrick = new JLabel("Patrick");
 		lblPatrick.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Profile pikachu = new Profile();
 				pikachu.setVisible(true);
 			}
 		});
 		lblPatrick.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		lblPatrick.setFont(new Font("Apple Chancery", Font.BOLD | Font.ITALIC, 15));
 		lblPatrick.setBounds(632, 210, 61, 16);
 		frame.getContentPane().add(lblPatrick);
 		
 		final JTextPane txtpnRank = new JTextPane();
 		txtpnRank.setText("Rank  \n\n90%");
 		txtpnRank.setBounds(554, 273, 45, 66);
 		frame.getContentPane().add(txtpnRank);
 		
 		final JLabel label_4 = new JLabel("");
 		label_4.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/Turtle.png")));
 		label_4.setBounds(602, 447, 114, 115);
 		frame.getContentPane().add(label_4);
 		
 		final JLabel lblKatherine = new JLabel("Katherine");
 		lblKatherine.setFont(new Font("Apple Chancery", Font.BOLD | Font.ITALIC, 15));
 		lblKatherine.setBounds(618, 421, 81, 16);
 		frame.getContentPane().add(lblKatherine);
 		
 		final JTextPane txtpnRank_1 = new JTextPane();
 		txtpnRank_1.setText("Rank  \n\n75%");
 		txtpnRank_1.setBounds(554, 485, 45, 66);
 		frame.getContentPane().add(txtpnRank_1);
 		
 		JButton btnNewest = new JButton("Newest");
 		btnNewest.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(label_5.getY() > 400){
 					label_5.setLocation(label_5.getX(), label_5.getY() - 224);
 					lblNewLabel.setLocation(lblNewLabel.getX(), lblNewLabel.getY() - 224);
 					label_2.setLocation(label_2.getX(), label_2.getY() - 224);
 					lblNewLabel_1.setLocation(lblNewLabel_1.getX(), lblNewLabel_1.getY() - 224);
 					txtpnIThinkThe.setLocation(txtpnIThinkThe.getX(), txtpnIThinkThe.getY() - 224);
 					label_1.setLocation(label_1.getX(), label_1.getY() - 211);
 					txtpnRank.setLocation(txtpnRank.getX(), txtpnRank.getY() - 211);
 					lblPatrick.setLocation(lblPatrick.getX(), lblPatrick.getY() - 211);
 				
 					label_6.setLocation(label_6.getX(), label_6.getY() + 224);
 					label.setLocation(label.getX(), label.getY() + 224);
 					label_3.setLocation(label_3.getX(), label_3.getY() + 224);
 					txtpnWeShouldLeave.setLocation(txtpnWeShouldLeave.getX(), txtpnWeShouldLeave.getY() + 224);
 					lblSuggestionForEnegy.setLocation(lblSuggestionForEnegy.getX(), lblSuggestionForEnegy.getY() + 224);
 					label_4.setLocation(label_4.getX(), label_4.getY() + 211);
 					txtpnRank_1.setLocation(txtpnRank_1.getX(), txtpnRank_1.getY() + 211);
 					lblKatherine.setLocation(lblKatherine.getX(), lblKatherine.getY() + 211);
 				}
 			}
 		});
 		btnNewest.setBounds(220, 95, 117, 29);
 		frame.getContentPane().add(btnNewest);
 		
 		JButton btnTopMonthly = new JButton("Top Monthly");
 		btnTopMonthly.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if(label_5.getY() < 400){
 					label_5.setLocation(label_5.getX(), label_5.getY() + 224);
 					lblNewLabel.setLocation(lblNewLabel.getX(), lblNewLabel.getY() + 224);
 					label_2.setLocation(label_2.getX(), label_2.getY() + 224);
 					lblNewLabel_1.setLocation(lblNewLabel_1.getX(), lblNewLabel_1.getY() + 224);
 					txtpnIThinkThe.setLocation(txtpnIThinkThe.getX(), txtpnIThinkThe.getY() + 224);
 					lblPatrick.setLocation(lblPatrick.getX(), lblPatrick.getY() + 211);
 					label_1.setLocation(label_1.getX(), label_1.getY() + 211);
 					txtpnRank.setLocation(txtpnRank.getX(), txtpnRank.getY() + 211);
 					
 				
 					label_6.setLocation(label_6.getX(), label_6.getY() - 224);
 					label.setLocation(label.getX(), label.getY() - 224);
 					label_3.setLocation(label_3.getX(), label_3.getY() - 224);
 					txtpnWeShouldLeave.setLocation(txtpnWeShouldLeave.getX(), txtpnWeShouldLeave.getY() - 224);
 					lblSuggestionForEnegy.setLocation(lblSuggestionForEnegy.getX(), lblSuggestionForEnegy.getY() - 224);
 					lblKatherine.setLocation(lblKatherine.getX(), lblKatherine.getY() - 211);
 					label_4.setLocation(label_4.getX(), label_4.getY() - 211);
 					txtpnRank_1.setLocation(txtpnRank_1.getX(), txtpnRank_1.getY() - 211);
 					
 				}
 				
 			}
 		});
 		btnTopMonthly.setBounds(361, 95, 117, 29);
 		frame.getContentPane().add(btnTopMonthly);
 		
 		JButton btnTopYearly = new JButton("Top Yearly\r\n");
 		btnTopYearly.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(label_5.getY() < 400){
 					label_5.setLocation(label_5.getX(), label_5.getY() + 224);
 					lblNewLabel.setLocation(lblNewLabel.getX(), lblNewLabel.getY() + 224);
 					label_2.setLocation(label_2.getX(), label_2.getY() + 224);
 					lblNewLabel_1.setLocation(lblNewLabel_1.getX(), lblNewLabel_1.getY() + 224);
 					txtpnIThinkThe.setLocation(txtpnIThinkThe.getX(), txtpnIThinkThe.getY() + 224);
 					lblPatrick.setLocation(lblPatrick.getX(), lblPatrick.getY() + 211);
 					label_1.setLocation(label_1.getX(), label_1.getY() + 211);
 					txtpnRank.setLocation(txtpnRank.getX(), txtpnRank.getY() + 211);
 					
 				
 					label_6.setLocation(label_6.getX(), label_6.getY() - 224);
 					label.setLocation(label.getX(), label.getY() - 224);
 					label_3.setLocation(label_3.getX(), label_3.getY() - 224);
 					txtpnWeShouldLeave.setLocation(txtpnWeShouldLeave.getX(), txtpnWeShouldLeave.getY() - 224);
 					lblSuggestionForEnegy.setLocation(lblSuggestionForEnegy.getX(), lblSuggestionForEnegy.getY() - 224);
 					lblKatherine.setLocation(lblKatherine.getX(), lblKatherine.getY() - 211);
 					label_4.setLocation(label_4.getX(), label_4.getY() - 211);
 					txtpnRank_1.setLocation(txtpnRank_1.getX(), txtpnRank_1.getY() - 211);
 					
 				}
 			}
 		});
 		btnTopYearly.setBounds(513, 95, 117, 29);
 		frame.getContentPane().add(btnTopYearly);
 		
 		JLabel label_7 = new JLabel("Overview");
 		label_7.setOpaque(true);
 		label_7.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				PrivateDashboard privateDashboard = new PrivateDashboard();
 				privateDashboard.getFrame().setVisible(true);
 			}
 		});
 		label_7.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		label_7.setHorizontalAlignment(SwingConstants.CENTER);
 		label_7.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		label_7.setBorder(new LineBorder(new Color(0, 0, 0)));
 		label_7.setBackground(new Color(0, 204, 204));
 		label_7.setBounds(251, 14, 86, 29);
 		frame.getContentPane().add(label_7);
 		
 		JLabel lblIdeaCompetition = new JLabel("Idea Competition");
 		lblIdeaCompetition.setBackground(Color.ORANGE);
 		lblIdeaCompetition.setOpaque(true);
 		lblIdeaCompetition.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		lblIdeaCompetition.setHorizontalAlignment(SwingConstants.CENTER);
 		lblIdeaCompetition.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblIdeaCompetition.setBorder(new LineBorder(new Color(0, 0, 0)));
 		lblIdeaCompetition.setBounds(619, 14, 133, 29);
 		frame.getContentPane().add(lblIdeaCompetition);
 		
 		JLabel label_9 = new JLabel("Comparison");
 		label_9.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Comparison comp = new Comparison();
 				comp.getFrame().setVisible(true);
 			}
 		});
 		label_9.setOpaque(true);
 		label_9.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		label_9.setHorizontalAlignment(SwingConstants.CENTER);
 		label_9.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		label_9.setBorder(new LineBorder(new Color(0, 0, 0)));
 		label_9.setBackground(new Color(0, 204, 204));
 		label_9.setBounds(440, 14, 86, 29);
 		frame.getContentPane().add(label_9);
 		
 		JLabel lblBulbIcon = new JLabel("");
 		lblBulbIcon.setIcon(new ImageIcon(PublicDashboard.class.getResource("/ubc/cpsc544/bulb.png")));
 		lblBulbIcon.setBounds(12, 11, 35, 33);
 		frame.getContentPane().add(lblBulbIcon);
 		
 		JLabel lblUbcEnergyDashboard = new JLabel("UBC Energy Dashboard");
 		lblUbcEnergyDashboard.setForeground(new Color(0, 128, 0));
 		lblUbcEnergyDashboard.setFont(new Font("Tahoma", Font.BOLD, 14));
 		lblUbcEnergyDashboard.setBorder(null);
 		lblUbcEnergyDashboard.setBounds(49, 15, 174, 29);
 		frame.getContentPane().add(lblUbcEnergyDashboard);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(null);
 		panel.setBackground(Color.WHITE);
 		panel.setBounds(12, 54, 174, 115);
 		frame.getContentPane().add(panel);
 		
 		JLabel label_12 = new JLabel("");
 		label_12.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/user.png")));
 		panel.add(label_12);
 		
 		JLabel label_11 = new JLabel("");
 		panel.add(label_11);
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		panel_1.setBackground(Color.WHITE);
 		panel_1.setBounds(745, 173, 240, 493);
 		frame.getContentPane().add(panel_1);
 		panel_1.setLayout(null);
 		
 		JLabel lblResidents = new JLabel("Residents");
 		lblResidents.setBounds(10, 11, 147, 19);
 		lblResidents.setFont(new Font("Tahoma", Font.BOLD, 15));
 		lblResidents.setBorder(null);
 		panel_1.add(lblResidents);
 		
 		JLabel label_13 = new JLabel("");
 		label_13.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/Picachu.png")));
 		label_13.setBounds(10, 51, 63, 61);
 		panel_1.add(label_13);
 		
 		JLabel label_14 = new JLabel("");
 		label_14.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/Me.png")));
 		label_14.setBounds(129, 51, 63, 61);
 		panel_1.add(label_14);
 		
 		JLabel label_15 = new JLabel("");
 		label_15.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/Turtle.png")));
 		label_15.setBounds(129, 202, 63, 61);
 		panel_1.add(label_15);
 		
 		JLabel label_16 = new JLabel("");
 		label_16.setIcon(new ImageIcon(Ideas.class.getResource("/ubc/cpsc544/Me.png")));
 		label_16.setBounds(20, 202, 63, 61);
 		panel_1.add(label_16);
 		
 		JLabel lblPatrick_1 = new JLabel(" Patrick");
 		lblPatrick_1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 15));
 		lblPatrick_1.setBounds(10, 123, 81, 19);
 		panel_1.add(lblPatrick_1);
 		
 		JLabel lblTom = new JLabel("Tom");
 		lblTom.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 15));
 		lblTom.setBounds(139, 123, 61, 16);
 		panel_1.add(lblTom);
 		
 		JLabel lblJennifer = new JLabel("Jennifer");
 		lblJennifer.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 15));
 		lblJennifer.setBounds(30, 274, 61, 16);
 		panel_1.add(lblJennifer);
 		
 		JLabel lblSam = new JLabel("Katherine");
 		lblSam.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 15));
 		lblSam.setBounds(139, 274, 79, 16);
 		panel_1.add(lblSam);
 		
 		JLabel label_17 = new JLabel("90%");
 		label_17.setBounds(20, 145, 46, 14);
 		panel_1.add(label_17);
 		
 		JLabel label_18 = new JLabel("85%");
 		label_18.setBounds(146, 145, 46, 14);
 		panel_1.add(label_18);
 		
 		JLabel label_19 = new JLabel("60%");
 		label_19.setBounds(40, 301, 46, 14);
 		panel_1.add(label_19);
 		
 		JLabel label_20 = new JLabel("75%");
 		label_20.setBounds(146, 301, 46, 14);
 		panel_1.add(label_20);
 		
 		JLabel label_8 = new JLabel("");
 		label_8.setBounds(612, 291, 103, 101);
 		frame.getContentPane().add(label_8);
 		
 		JLabel lblProposeIdea = new JLabel("Propose Idea");
 		lblProposeIdea.setOpaque(true);
 		lblProposeIdea.setHorizontalAlignment(SwingConstants.CENTER);
 		lblProposeIdea.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblProposeIdea.setBorder(new LineBorder(new Color(0, 0, 0)));
 		lblProposeIdea.setBackground(new Color(0, 204, 102));
 		lblProposeIdea.setBounds(745, 93, 133, 29);
 		frame.getContentPane().add(lblProposeIdea);
 		
 		txtSearch = new JTextField();
 		txtSearch.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		txtSearch.setText("Search");
 		txtSearch.setForeground(Color.LIGHT_GRAY);
 		txtSearch.setBounds(870, 14, 115, 29);
 		frame.getContentPane().add(txtSearch);
 		txtSearch.setColumns(10);
 		
 		
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 
 	public void setFrame(JFrame frame) {
 		this.frame = frame;
 	}
 }
