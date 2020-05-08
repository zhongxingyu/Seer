 package petter.bjelm;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import java.util.ArrayList;
 import javax.swing.JComboBox;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 public class GUI extends JFrame {
 
 	private JPanel panel;
 	private int i;
 	private int lastRow = 0;
 	private int rowX = 0;
 	private int rowY = 0;
 	private int setButtonsPerRows = 9;
 	private int buttonCounter = 0;
 	private ArrayList<String> anArray;
 	private ArrayList<JButton> anArrayTwo;
 	private ArrayList<String> URLArray;
 	private GetCategory data;
 	private ImageParsing imageParse;
 	private JButton[] buttons;
 	private JComboBox comboBox;
 	private GetImgFromURL URLImage;
 
	private int buttonWidth = 150;
	private int buttonHeight = 150;
 
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					GUI frame = new GUI();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	public GUI() {
 		super("Add component on JFrame at runtime");
 
 		anArray = new ArrayList<String>();
 		anArrayTwo = new ArrayList<JButton>();
 		URLArray = new ArrayList<String>();
 		URLImage = new GetImgFromURL();
 		imageParse = new ImageParsing();
 		data = new GetCategory();
 		data.setDate(imageParse.getCurrDate());
 		imageParse.setDate(imageParse.getCurrDate());
 
 		JButton button = new JButton("Skaldjur");
 		button.setHorizontalTextPosition(SwingConstants.CENTER);
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				rowY = 0;
 				lastRow = setButtonsPerRows;
 				rowX = 30;
 				buttonCounter = 0;
 				data.setCategory("Skaldjur");
 				// buttons = new JButton[0];

 				if (buttons != null) {
 					for (i = 0; i < buttons.length; i++) {
 
 						panel.remove(buttons[i]);
 						panel.revalidate();
 						panel.repaint();
 
 					}
 				}
 
 				try {
 					anArray = data.getArray();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				try {
 					URLArray = imageParse.getURLArray("Skaldjur");
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 
 				for (int i = 0; i < URLArray.size(); i++) {
 					System.out.println(URLArray.get(i));
 				}
 
 				buttons = new JButton[anArray.size()];
 
 				for (i = 0; i < buttons.length; i++) {
 
 					buttons[i] = new JButton(anArray.get(i).replace('_', ' '),
 							new ImageIcon(URLImage.getURLImage(URLArray.get(i),
 									buttonWidth, buttonHeight)));
 					buttons[i].setHorizontalTextPosition(SwingConstants.CENTER);
 					buttons[i].setForeground(Color.WHITE);
 
 					if (i == lastRow) {
 						rowY = rowY + buttonHeight;
 						lastRow = i + setButtonsPerRows;
 						// System.out.println(rowY + " Y");
 
 					} else {
 
 					}
 
 					// System.out.println(buttonCounter);
 					if (buttonCounter == setButtonsPerRows) {
 						buttonCounter = 0;
 
 						buttons[i].setBounds(
								(buttonWidth * buttonCounter + 10), rowY,
 								buttonWidth, buttonHeight);
 					} else {
 
 						buttons[i].setBounds(
								(buttonWidth * buttonCounter + 10), rowY,
 								buttonWidth, buttonHeight);
 					}
 
 					buttonCounter++;
 					// System.out.println(rowX);
 					// buttons[i].setBounds((150*buttonCounter+10), rowY,
 					// buttonWidth, buttonWidth);
 
 					buttons[i].setActionCommand(anArray.get(i));
 
 					buttons[i].addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							String choice = e.getActionCommand();
 							JOptionPane.showMessageDialog(null,
 									"You have clicked: " + choice);
 						}
 					});
 
 					panel.add(buttons[i]);
 					panel.revalidate();
 					validate();
 					panel.repaint();
 				}
 
 			}
 
 		});
 
 		String[] patternExamples = { "Vlj mnad", "Januari", "Februari",
 				"Mars", "April", "Maj", "Juni", "Juli", "Augusti", "September",
 				"Oktober", "November", "December", };
 
 		comboBox = new JComboBox(patternExamples);
 		comboBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				int index = comboBox.getSelectedIndex();
 
 				switch (index) {
 				case 0:
 					data.setDate(imageParse.getCurrDate());
 					imageParse.setDate(imageParse.getCurrDate());
 					break;
 				case 1:
 					data.setDate("01");
 					imageParse.setDate("01");
 					break;
 				case 2:
 					data.setDate("02");
 					imageParse.setDate("02");
 					break;
 				case 3:
 					data.setDate("03");
 					imageParse.setDate("03");
 					break;
 				case 4:
 					data.setDate("04");
 					imageParse.setDate("04");
 					break;
 				case 5:
 					data.setDate("05");
 					imageParse.setDate("05");
 					break;
 				case 6:
 					data.setDate("06");
 					imageParse.setDate("06");
 					break;
 				case 7:
 					data.setDate("07");
 					imageParse.setDate("07");
 					break;
 				case 8:
 					data.setDate("08");
 					imageParse.setDate("08");
 					break;
 				case 9:
 					data.setDate("09");
 					imageParse.setDate("09");
 					break;
 				case 10:
 					data.setDate("10");
 					imageParse.setDate("10");
 					break;
 				case 11:
 					data.setDate("11");
 					imageParse.setDate("11");
 					break;
 				case 12:
 					data.setDate("12");
 					imageParse.setDate("12");
 				default:
 					data.setDate("00");
 					imageParse.setDate("00");
 					break;
 				}
 			}
 		});
 
 		JButton buttonTwo = new JButton("Grnsaker");
 		buttonTwo.setHorizontalTextPosition(SwingConstants.CENTER);
 		panel = new JPanel();
 		panel.setBorder(null);
 		GroupLayout groupLayout = new GroupLayout(getContentPane());
 		groupLayout
 				.setHorizontalGroup(groupLayout
 						.createParallelGroup(Alignment.LEADING)
 						.addGroup(
 								groupLayout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.LEADING)
 														.addComponent(
 																panel,
 																GroupLayout.DEFAULT_SIZE,
 																932,
 																Short.MAX_VALUE)
 														.addComponent(comboBox,
 																0, 932,
 																Short.MAX_VALUE)
 														.addGroup(
 																groupLayout
 																		.createSequentialGroup()
 																		.addComponent(
 																				buttonTwo,
 																				GroupLayout.DEFAULT_SIZE,
 																				457,
 																				Short.MAX_VALUE)
 																		.addGap(18)
 																		.addComponent(
 																				button,
 																				GroupLayout.DEFAULT_SIZE,
 																				457,
 																				Short.MAX_VALUE)))
 										.addContainerGap()));
 		groupLayout
 				.setVerticalGroup(groupLayout
 						.createParallelGroup(Alignment.LEADING)
 						.addGroup(
 								Alignment.TRAILING,
 								groupLayout
 										.createSequentialGroup()
 										.addContainerGap(
 												GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)
 										.addGroup(
 												groupLayout
 														.createParallelGroup(
 																Alignment.BASELINE)
 														.addComponent(
 																button,
 																GroupLayout.PREFERRED_SIZE,
 																25,
 																GroupLayout.PREFERRED_SIZE)
 														.addComponent(
 																buttonTwo,
 																GroupLayout.PREFERRED_SIZE,
 																25,
 																GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(
 												ComponentPlacement.UNRELATED)
 										.addComponent(comboBox,
 												GroupLayout.PREFERRED_SIZE, 25,
 												GroupLayout.PREFERRED_SIZE)
 										.addGap(18)
 										.addComponent(panel,
 												GroupLayout.PREFERRED_SIZE,
 												642, GroupLayout.PREFERRED_SIZE)
 										.addContainerGap()));
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setAutoCreateGaps(true);
 		gl_panel.setAutoCreateContainerGaps(true);
 		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(
 				Alignment.LEADING).addGap(0, 736, Short.MAX_VALUE));
 		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(
 				Alignment.LEADING).addGap(0, 539, Short.MAX_VALUE));
 		panel.setLayout(gl_panel);
 		getContentPane().setLayout(groupLayout);
 
 		buttonTwo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				rowY = 0;
 				lastRow = setButtonsPerRows;
 				rowX = 30;
 				buttonCounter = 0;
 				data.setCategory("Grnsaker");

 				// buttons = new JButton[0];

 				if (buttons != null) {
 					for (i = 0; i < buttons.length; i++) {
 
 						panel.remove(buttons[i]);
 						panel.revalidate();
 						panel.repaint();
 
 					}
 				}
 
 				try {
 					anArray = data.getArray();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				try {
 					URLArray = imageParse.getURLArray("Grnsaker");
 				} catch (Exception e6) {
 					// TODO Auto-generated catch block
 					e6.printStackTrace();
 				}
 
 				for (int i = 0; i < URLArray.size(); i++) {
 					System.out.println(URLArray.get(i));
 				}
 
 				buttons = new JButton[anArray.size()];
 
 				for (i = 0; i < buttons.length; i++) {
 
 					buttons[i] = new JButton(anArray.get(i).replace('_', ' '),
 							new ImageIcon(URLImage.getURLImage(URLArray.get(i),
 									buttonWidth, buttonHeight)));
 					buttons[i].setHorizontalTextPosition(SwingConstants.CENTER);
 					buttons[i].setForeground(Color.WHITE);
 
 					if (i == lastRow) {
 						rowY = rowY + buttonHeight;
 						lastRow = i + setButtonsPerRows;
 						// System.out.println(rowY + " Y");
 
 					} else {
 
 					}
 
 					// System.out.println(buttonCounter);
 					if (buttonCounter == setButtonsPerRows) {
 						buttonCounter = 0;
 
 						buttons[i].setBounds(
								(buttonWidth * buttonCounter + 10), rowY,
 								buttonWidth, buttonHeight);
 					} else {
 
 						buttons[i].setBounds(
								(buttonWidth * buttonCounter + 10), rowY,
 								buttonWidth, buttonHeight);
 					}
 
 					buttonCounter++;
 					// System.out.println(rowX);
 					// buttons[i].setBounds((150*buttonCounter+10), rowY,
 					// buttonWidth, buttonWidth);
 
 					buttons[i].setActionCommand(anArray.get(i));
 
 					buttons[i].addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							String choice = e.getActionCommand();
 							JOptionPane.showMessageDialog(null,
 									"You have clicked: " + choice);
 						}
 					});
 
 					panel.add(buttons[i]);
 					panel.revalidate();
 					validate();
 					panel.repaint();
 				}
 
 			}
 		});
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(960, 768);
 		setVisible(true);
 	}
 
 	public JComboBox getComboBox() {
 		return comboBox;
 	}
 }
