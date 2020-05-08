 package EpicsChins.util;
 
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.bot.Context;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 public class GUI extends JFrame {
 	public static int foodUser = 0; // user selected food
 	public static int antipoisonUser = 0; // user selected Antipoison
 
 	public GUI() {
 		setVisible(true);
 		// Title
		String version = " v0.185";
 		setTitle("EC" + version);
 		setResizable(false);
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		Container contentPane = getContentPane();
 		contentPane.setLayout(null);
 		// ---- foodLabel ----
 		final JLabel FOOD_LABEL = new JLabel("What food should we use?");
 
 		FOOD_LABEL.setBackground(new Color(212, 208, 200));
 		FOOD_LABEL.setFont(FOOD_LABEL.getFont().deriveFont(FOOD_LABEL.getFont().getStyle() | Font.BOLD));
 		contentPane.add(FOOD_LABEL);
 		FOOD_LABEL.setBounds(20, 185, 155, FOOD_LABEL.getPreferredSize().height);
 		// ---- antiLabel ----
 		final JLabel ANTI_LABEL = new JLabel("What antipoison should we use?");
 
 		ANTI_LABEL.setBackground(new Color(212, 208, 200));
 		ANTI_LABEL.setText("What antipoison should we use?");
 		ANTI_LABEL.setFont(ANTI_LABEL.getFont().deriveFont(ANTI_LABEL.getFont().getStyle() | Font.BOLD));
 		contentPane.add(ANTI_LABEL);
 		ANTI_LABEL.setBounds(5, 235, 190, 25);
 		// ---- warningLabel ----
 		final JLabel WARNING_LABEL = new JLabel("WARNING");
 
 		WARNING_LABEL.setForeground(Color.red);
 		WARNING_LABEL.setFont(WARNING_LABEL.getFont().deriveFont(WARNING_LABEL.getFont().getStyle() | Font.BOLD));
 		contentPane.add(WARNING_LABEL);
 		WARNING_LABEL.setBounds(70, 285, 60, WARNING_LABEL.getPreferredSize().height);
 		// ---- warningLabelB ----
 		final JLabel WARNING_LABEL_B = new JLabel("Start in the Grand Exchange!");
 
 		contentPane.add(WARNING_LABEL_B);
 		WARNING_LABEL_B.setBounds(new Rectangle(new Point(40, 305), WARNING_LABEL_B.getPreferredSize()));
 		// ---- greeLabel ----
 		final JLabel GREEGREE_LABEL = new JLabel("Are we using a greegree?");
 
 		GREEGREE_LABEL.setFont(GREEGREE_LABEL.getFont().deriveFont(GREEGREE_LABEL.getFont().getStyle() | Font.BOLD));
 		contentPane.add(GREEGREE_LABEL);
 		GREEGREE_LABEL.setBounds(new Rectangle(new Point(25, 140), GREEGREE_LABEL.getPreferredSize()));
 		// ---- titleLabel ----
 		final JLabel TITLE_LABEL = new JLabel("Epics Chinner" + version);
 		TITLE_LABEL.setFont(TITLE_LABEL.getFont().deriveFont(TITLE_LABEL.getFont().getStyle() | Font.BOLD));
 		contentPane.add(TITLE_LABEL);
 		TITLE_LABEL.setBounds(45, 10, 130, 25);
 		// ---- reqTextPane ----
 		final JTextPane REQ_TEXT_PANE = new JTextPane();
 		REQ_TEXT_PANE.setBackground(new Color(212, 208, 200));
 		REQ_TEXT_PANE.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		REQ_TEXT_PANE.setDisabledTextColor(new Color(240, 240, 240));
 		REQ_TEXT_PANE.setEditable(false);
 		REQ_TEXT_PANE.setText("Requirements:");
 		REQ_TEXT_PANE.setFont(REQ_TEXT_PANE.getFont().deriveFont(REQ_TEXT_PANE.getFont().getStyle() | Font.BOLD));
 		contentPane.add(REQ_TEXT_PANE);
 		REQ_TEXT_PANE.setBounds(45, 40, 95, 20);
 		// ---- reqTextPaneB ----
 		final JTextPane REQ_TEXT_PANE_B = new JTextPane();
 		REQ_TEXT_PANE_B.setBackground(new Color(212, 208, 200));
 		REQ_TEXT_PANE_B.setText("- Access to Ape Atoll\n- 43 Prayer\n- 55 Ranged\n- 3+ Prayer renewal flasks\n- 3+ Ranged flasks");
 		REQ_TEXT_PANE_B.setEditable(false);
 		contentPane.add(REQ_TEXT_PANE_B);
 		REQ_TEXT_PANE_B.setBounds(25, 55, 135, 75);
 		// ---- greeBoxYes ----
 		final JCheckBox GREEGREE_BOX_YES = new JCheckBox("Yes");
 		GREEGREE_BOX_YES.setSelected(true);
 		if (GREEGREE_BOX_YES.isSelected()) {
 			Data.usingGreegree = true;
 		}
 		contentPane.add(GREEGREE_BOX_YES);
 		GREEGREE_BOX_YES.setBounds(new Rectangle(new Point(45, 160), GREEGREE_BOX_YES.getPreferredSize()));
 		// ---- greeBoxNo ----
 		final JCheckBox GREEGREE_BOX_NO = new JCheckBox("No");
 		GREEGREE_BOX_NO.setSelected(false);
 		if (GREEGREE_BOX_NO.isSelected()) {
 			Data.usingGreegree = false;
 		}
 		contentPane.add(GREEGREE_BOX_NO);
 		GREEGREE_BOX_NO.setBounds(new Rectangle(new Point(100, 160), GREEGREE_BOX_NO.getPreferredSize()));
 		// ---- foodCombo ----
 		final JComboBox<String> FOOD_COMBO_BOX = new JComboBox<>();
 		FOOD_COMBO_BOX.setModel(new DefaultComboBoxModel<>(new String[]{"Select your food...", "Shark", "Rocktail",
 		                                                                "Monkfish", "Swordfish", "Lobster", "Tuna",
 		                                                                "Trout", "Salmon"}));
 		contentPane.add(FOOD_COMBO_BOX);
 		FOOD_COMBO_BOX.setBounds(25, 210, 150, FOOD_COMBO_BOX.getPreferredSize().height);
 		// ---- poisonCombo ----
 		final JComboBox<String> POISON_COMBO_BOX = new JComboBox<>();
 		POISON_COMBO_BOX.setModel(new DefaultComboBoxModel<>(new String[]{"Select an antipoison...",
 		                                                                  "Super antipoison flask",
 		                                                                  "Antipoison++ flask", "Antipoison+ flask",
 		                                                                  "Antipoison flask", "Super antipoison",
 		                                                                  "Antipoison++", "Antipoison+", "Antipoison",
 		                                                                  "Antipoison mix", "Antipoison elixir"}));
 		contentPane.add(POISON_COMBO_BOX);
 		POISON_COMBO_BOX.setBounds(25, 260, 150, POISON_COMBO_BOX.getPreferredSize().height);
 		{
 			Dimension preferredSize = new Dimension();
 			for (int i = 0; i < contentPane.getComponentCount(); i++) {
 				Rectangle bounds = contentPane.getComponent(i).getBounds();
 				preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
 				preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
 			}
 			Insets insets = contentPane.getInsets();
 			preferredSize.width += insets.right;
 			preferredSize.height += insets.bottom;
 			contentPane.setMinimumSize(preferredSize);
 			contentPane.setPreferredSize(preferredSize);
 		}
 		setSize(210, 395);
 		setLocationRelativeTo(null);
 		// ---- startButton ----
 		final JButton START_BUTTON = new JButton("Start");
 		contentPane.add(START_BUTTON);
 		START_BUTTON.setBounds(5, 330, 185, 25);
 		START_BUTTON.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Data.START_SCRIPT = true;
 				final String USER_FOOD = FOOD_COMBO_BOX.getSelectedItem().toString();
 				if (USER_FOOD.equals("Select your food...")) {
 					Context.get().getActiveScript().log.info("No food selected, stopping script");
 					Context.get().getActiveScript().stop();
 				}
 				if (USER_FOOD.equals("Shark")) {
 					foodUser = 385;
 				}
 				if (USER_FOOD.equals("Rocktail")) {
 					foodUser = 15272;
 				}
 				if (USER_FOOD.equals("Monkfish")) {
 					foodUser = 7946;
 				}
 				if (USER_FOOD.equals("Swordfish")) {
 					foodUser = 373;
 				}
 				if (USER_FOOD.equals("Lobster")) {
 					foodUser = 379;
 				}
 				if (USER_FOOD.equals("Tuna")) {
 					foodUser = 361;
 				}
 				if (USER_FOOD.equals("Trout")) {
 					foodUser = 333;
 				}
 				if (USER_FOOD.equals("Salmon")) {
 					foodUser = 329;
 				}
 				final String USER_ANTIPOISON = POISON_COMBO_BOX.getSelectedItem().toString();
 				if (USER_ANTIPOISON.equals("Select an antipoison...")) {
 					Logger.getLogger("EpicsChins").info("No antipoison selected, stopping script");
 					Game.logout(false);
 					Context.get().getActiveScript().stop();
 				}
 				if (USER_ANTIPOISON.equals("Super antipoison flask")) {
 					antipoisonUser = Data.FLASK_ANTIPOISON_SUPER_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison++ flask")) {
 					antipoisonUser = Data.FLASK_ANTIPOISON_PLUSPLUS_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison+ flask")) {
 					antipoisonUser = Data.FLASK_ANTIPOISON_PLUS_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison Flask")) {
 					antipoisonUser = Data.FLASK_ANTIPOISON_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Super Antipoison")) {
 					antipoisonUser = Data.POT_ANTIPOISON_SUPER_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison++")) {
 					antipoisonUser = Data.POT_ANTIPOISON_PLUSPLUS_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison+")) {
 					antipoisonUser = Data.POT_ANTIPOISON_PLUS_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison")) {
 					antipoisonUser = Data.POT_ANTIPOISON_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison mix")) {
 					antipoisonUser = Data.MIX_ANTIPOISON_FULL;
 				}
 				if (USER_ANTIPOISON.equals("Antipoison elixir")) {
 					antipoisonUser = Data.ELIXIR_ANTIPOISON;
 				}
 				dispose();
 			}
 		});
 	}
 
 
 	public static Image getImage(String url) {
 
 		try {
 			return ImageIO.read(new URL(url));
 
 		} catch (IOException e) {
 			return null;
 		}
 	}
 }
