 package de.rpi.minecraft.morpheu5.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import java.awt.BorderLayout;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.AbstractListModel;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Font;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.MatteBorder;
 import java.awt.Color;
 import javax.swing.JButton;
 import java.awt.FlowLayout;
 import javax.swing.SwingConstants;
 
 import de.rpi.minecraft.morpheu5.controller.Command;
 import de.rpi.minecraft.morpheu5.controller.Controller;
 import de.rpi.minecraft.morpheu5.interfaces.IWindowControl;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.ListSelectionModel;
 
 public class Window extends JFrame {
 
 	private IWindowControl control;
	private Command currentCommand = Command.NONE;
 
 	public Window() {
 		super("RPi MC API GUI");
 
 		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 
 				// Tabchange --> Change Current List Element
 				switch (tabbedPane.getSelectedIndex()) {
 				case 0:
 					currentCommand = Command.postToChat;
 					break;
 
 				case 1:
 					currentCommand = Command.DUMMY1;
 					break;
 
 				default:
 					break;
 				}
 				System.out.println(currentCommand);
 
 			}
 		});
 		getContentPane().add(tabbedPane, BorderLayout.CENTER);
 
 		create_chat_tab(tabbedPane);
 		create_world_tab(tabbedPane);
		
		
		//Initial Value
		currentCommand = Command.postToChat;
 	}
 
 	private void create_chat_tab(JTabbedPane tabbedPane) {
 		JPanel panel_chat = new JPanel();
 		tabbedPane.addTab("Chat", null, panel_chat, null);
 		panel_chat.setLayout(new BorderLayout(0, 0));
 
 		JList list_commands = new JList();
 		list_commands.setValueIsAdjusting(true);
 		list_commands.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list_commands.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(
 				0, 0, 0)));
 		list_commands.setPreferredSize(new Dimension(150, 0));
 		list_commands.setModel(new AbstractListModel() {
 			String[] values = new String[] { "PostToChat" };
 
 			public int getSize() {
 				return values.length;
 			}
 
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		list_commands.setSelectedIndex(0);
 		panel_chat.add(list_commands, BorderLayout.WEST);
 
 		JPanel panel_info = new JPanel();
 		panel_chat.add(panel_info, BorderLayout.CENTER);
 		panel_info.setLayout(new GridLayout(2, 1, 0, 0));
 
 		JPanel panel_info_description = new JPanel();
 		panel_info_description.setBackground(Color.WHITE);
 		panel_info.add(panel_info_description);
 		panel_info_description.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblHeadline = new JLabel("PostToChat");
 		lblHeadline.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0,
 				0, 0)));
 		lblHeadline.setBackground(Color.WHITE);
 		lblHeadline.setFont(new Font("Dialog", Font.BOLD, 30));
 		panel_info_description.add(lblHeadline, BorderLayout.NORTH);
 
 		JTextPane txtDescription = new JTextPane();
 		txtDescription.setEditable(false);
 		txtDescription.setText("Posts a Chat Message on the Minecraft Server");
 		panel_info_description.add(txtDescription, BorderLayout.CENTER);
 
 		JPanel panel_info_input = new JPanel();
 		panel_info.add(panel_info_input);
 		panel_info_input.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblValue = new JLabel("Value");
 		lblValue.setVerticalAlignment(SwingConstants.TOP);
 		lblValue.setPreferredSize(new Dimension(80, 80));
 		lblValue.setFont(new Font("Dialog", Font.BOLD, 30));
 		panel_info_input.add(lblValue, BorderLayout.NORTH);
 
 		JPanel panel_data_input = new JPanel();
 		panel_info_input.add(panel_data_input, BorderLayout.CENTER);
 		panel_data_input.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 
 		JLabel label_data_message = new JLabel("Message:");
 		label_data_message.setFont(new Font("Dialog", Font.BOLD, 20));
 		panel_data_input.add(label_data_message);
 
 		final JTextField textField_data_input = new JTextField();
 		textField_data_input.setHorizontalAlignment(SwingConstants.LEFT);
 		panel_data_input.add(textField_data_input);
 		textField_data_input.setColumns(10);
 
 		JPanel panel_data_action = new JPanel();
 		panel_info_input.add(panel_data_action, BorderLayout.SOUTH);
 		panel_data_action.setLayout(new GridLayout(0, 2, 0, 0));
 
 		JButton btnClear = new JButton("Clear");
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				textField_data_input.setText("");
 			}
 		});
 		panel_data_action.add(btnClear);
 
 		JButton btnSend = new JButton("Send");
 		btnSend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
				System.out.println(currentCommand);
 				if (currentCommand == Command.postToChat) {
 					control.postToChat(textField_data_input.getText());
 					textField_data_input.setText("");
 				}
 
 			}
 		});
 		panel_data_action.add(btnSend);
 	}
 
 	private void create_world_tab(JTabbedPane tabbedPane) {
 		JPanel panel_world = new JPanel();
 		tabbedPane.addTab("world", null, panel_world, null);
 		panel_world.setLayout(new BorderLayout(0, 0));
 
 		final JList list_commands = new JList();
 		list_commands.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list_commands.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				if (arg0.getValueIsAdjusting()) {
 					switch (list_commands.getSelectedIndex()) {
 					case 0:
 						currentCommand = Command.DUMMY1;
 						break;
 
 					case 1:
 						currentCommand = Command.DUMMY2;
 						break;
 
 					case 2:
 						currentCommand = Command.DUMMY3;
 						break;
 
 					default:
 						break;
 					}

 					System.out.println(currentCommand);
 				}
 
 			}
 		});
 		list_commands.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(
 				0, 0, 0)));
 		list_commands.setPreferredSize(new Dimension(150, 0));
 		list_commands.setValueIsAdjusting(true);
 		list_commands.setModel(new AbstractListModel() {
 			String[] values = new String[] { "CMD1", "CMD2", "CMD3" };
 
 			public int getSize() {
 				return values.length;
 			}
 
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		list_commands.setSelectedIndex(0);
 		panel_world.add(list_commands, BorderLayout.WEST);
 
 		JPanel panel_info = new JPanel();
 		panel_world.add(panel_info, BorderLayout.CENTER);
 		panel_info.setLayout(new GridLayout(2, 1, 0, 0));
 
 		JPanel panel_info_description = new JPanel();
 		panel_info_description.setBackground(Color.WHITE);
 		panel_info.add(panel_info_description);
 		panel_info_description.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblHeadline = new JLabel("COMMAND");
 		lblHeadline.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0,
 				0, 0)));
 		lblHeadline.setBackground(Color.WHITE);
 		lblHeadline.setFont(new Font("Dialog", Font.BOLD, 30));
 		panel_info_description.add(lblHeadline, BorderLayout.NORTH);
 
 		JTextPane txtDescription = new JTextPane();
 		txtDescription.setEditable(false);
 		txtDescription.setText("DESC");
 		panel_info_description.add(txtDescription, BorderLayout.CENTER);
 
 		JPanel panel_info_input = new JPanel();
 		panel_info.add(panel_info_input);
 		panel_info_input.setLayout(new BorderLayout(0, 0));
 
 		JLabel lblValue = new JLabel("Value");
 		lblValue.setVerticalAlignment(SwingConstants.TOP);
 		lblValue.setPreferredSize(new Dimension(80, 80));
 		lblValue.setFont(new Font("Dialog", Font.BOLD, 30));
 		panel_info_input.add(lblValue, BorderLayout.NORTH);
 
 		JPanel panel_data_input = new JPanel();
 		panel_info_input.add(panel_data_input, BorderLayout.CENTER);
 		panel_data_input.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 
 		JLabel label_data_message = new JLabel("Message:");
 		label_data_message.setFont(new Font("Dialog", Font.BOLD, 20));
 		panel_data_input.add(label_data_message);
 
 		final JTextField textField_data_input = new JTextField();
 		textField_data_input.setHorizontalAlignment(SwingConstants.LEFT);
 		panel_data_input.add(textField_data_input);
 		textField_data_input.setColumns(10);
 
 		JPanel panel_data_action = new JPanel();
 		panel_info_input.add(panel_data_action, BorderLayout.SOUTH);
 		panel_data_action.setLayout(new GridLayout(0, 2, 0, 0));
 
 		JButton btnClear = new JButton("Clear");
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				textField_data_input.setText("");
 			}
 		});
 		panel_data_action.add(btnClear);
 
 		JButton btnSend = new JButton("Send");
 		btnSend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				switch (currentCommand) {
 				case DUMMY1:
 					System.out.println("DUMMY1");
 					break;
 
 				case DUMMY2:
 					System.out.println("DUMMY2");
 					break;
 
 				case DUMMY3:
 					System.out.println("DUMMY3");
 					break;
 				default:
 					break;
 				}
 
 			}
 		});
 		panel_data_action.add(btnSend);
 	}
 
 	public void addController(IWindowControl control) {
 		this.control = control;
 	}
 
 }
