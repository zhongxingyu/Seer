 package gui;
 
 import gui.buttons.ArrowButton;
 import gui.buttons.CircleButton;
 import gui.buttons.FreeButton;
 import gui.buttons.LineButton;
 import gui.buttons.RectangleButton;
 import gui.buttons.SaveWorkButton;
 import gui.buttons.SendButton;
 import gui.buttons.StarButton;
 import app.Mediator;
 import app.MouseApp;
 import app.Pair;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import javax.swing.JToolBar;
 import java.awt.GridBagConstraints;
 import javax.swing.JToggleButton;
 import javax.swing.JLabel;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import javax.swing.JProgressBar;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 import javax.swing.DefaultComboBoxModel;
 
 import worker.ExportTask;
 
 
 public class GroupTab extends JPanel {
 	private static final long serialVersionUID = -163956933872151109L;
 	private static final int MAX_LINE_CHARS = 40;
 	private JTextField textField;
 	JToolBar toolBar;
 	JProgressBar progressBar;
 	public JCanvas panel;
 	JList userLegend;
 	JComboBox comboBox;
 	JComboBox comboBox_1;
 	JTextPane txtpnHello;
 	private static Color cls[] = {Color.black, Color.green, Color.blue, Color.yellow, Color.red, Color.pink};
 	Mediator med;
 	public GroupTab(Gui g, final Mediator m) {
 		super();
 		this.med = m;
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 0.1, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.5, Double.MIN_VALUE, 0.0};
 		setLayout(gridBagLayout);
 
 		toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		GridBagConstraints gbc_toolBar = new GridBagConstraints();
 		gbc_toolBar.insets = new Insets(0, 0, 5, 5);
 		gbc_toolBar.gridx = 0;
 		gbc_toolBar.gridy = 0;
 		add(toolBar, gbc_toolBar);
 
 
 		JToggleButton tglbtnA = new CircleButton(g, m);
 		tglbtnA.setSelected(true);
 		toolBar.add(tglbtnA);
 
 		JToggleButton tglbtnB = new RectangleButton(g, m);
 		toolBar.add(tglbtnB);
 
 		JToggleButton tglbtnC = new ArrowButton(g,m);
 		toolBar.add(tglbtnC);
 
 		JToggleButton tglbtnD = new LineButton(g, m);
 		toolBar.add(tglbtnD);
 
 		JToggleButton tglbtnE = new FreeButton(g, m);
 		toolBar.add(tglbtnE);
 
 		JToggleButton tglbtnF = new StarButton(g, m);
 		toolBar.add(tglbtnF);
 
 		m.addGroupElement(tglbtnA, this);
 		m.addGroupElement(tglbtnB, this);
 		m.addGroupElement(tglbtnC, this);
 		m.addGroupElement(tglbtnD, this);
 		m.addGroupElement(tglbtnE, this);
 		m.addGroupElement(tglbtnF, this);
 
 		JButton btnSaveWork = new SaveWorkButton(g, m);
 		GridBagConstraints gbc_btnSaveWork = new GridBagConstraints();
 		gbc_btnSaveWork.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSaveWork.gridx = 1;
 		gbc_btnSaveWork.gridy = 0;
 		add(btnSaveWork, gbc_btnSaveWork);
 		m.addGroupElement(btnSaveWork, this);
 
 		final ExportTask export = new ExportTask();
 		
 		progressBar = new JProgressBar(0,10);
 		btnSaveWork.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				export.execute();
 			}
 		});
 		
 		export.addPropertyChangeListener(new PropertyChangeListener() {
 			
 			@Override
 			public void propertyChange(PropertyChangeEvent arg0) {
 				Object o = arg0.getNewValue();
 				if (o instanceof Integer)
 					progressBar.setValue((Integer) o);
 				
 			}
 		});
 		
 		
 		GridBagConstraints gbc_progressBar = new GridBagConstraints();
 		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
 		gbc_progressBar.gridx = 1;
 		gbc_progressBar.gridy = 1;
 		add(progressBar, gbc_progressBar);
 		m.addGroupElement(progressBar, this);
 
 		// Canvas - Drawing area
 		panel = new JCanvas(m);
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.gridheight = 2;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 1;
 		add(panel, gbc_panel);
 		m.addGroupElement(panel, this);
 		panel.addMouseListener(new MouseApp(m));
 		panel.addMouseMotionListener(new MouseApp(m));
 		userLegend = new JList();
 
 
 		GridBagConstraints gbc_list = new GridBagConstraints();
 		gbc_list.insets = new Insets(0, 0, 5, 0);
 		gbc_list.fill = GridBagConstraints.BOTH;
 		gbc_list.gridx = 1;
 		gbc_list.gridy = 2;
 		JScrollPane p = new JScrollPane(userLegend);
 		p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		add(p, gbc_list);
 
 
 		// Discutii
 		txtpnHello = new JTextPane();
 		txtpnHello.setEditable(false);
 
 
 		GridBagConstraints gbc_textPane = new GridBagConstraints();
 		gbc_textPane.insets = new Insets(0, 0, 5, 0);
 		gbc_textPane.gridwidth = 2;
 		gbc_textPane.fill = GridBagConstraints.BOTH;
 		gbc_textPane.gridx = 0;
 		gbc_textPane.gridy = 3;
 		p = new JScrollPane(txtpnHello);
 		p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
 		add(p, gbc_textPane);
 
 		JToolBar toolBar2 = new JToolBar();
 		toolBar2.setFloatable(false);
 		toolBar.setFloatable(false);
 		GridBagConstraints gbc_toolBar2 = new GridBagConstraints();
 		gbc_toolBar2.insets = new Insets(0, 0, 5, 5);
 		gbc_toolBar2.gridx = 0;
 		gbc_toolBar2.gridy = 4;
 		add(toolBar2, gbc_toolBar2);
 
 		JLabel lblFont = new JLabel("Font:");
 		toolBar2.add(lblFont);
 
 		comboBox = new JComboBox(); 
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"16", "20", "24", "30", "36"}));
 		toolBar2.add(comboBox);
 
 		JLabel lblColour = new JLabel("Colour:");
 		toolBar2.add(lblColour);
 
 		comboBox_1 = new JComboBox();
 		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"Black", "Green", "Blue", "Yellow", "Red", "Pink"}));
 		toolBar2.add(comboBox_1);
 
 		textField = new JTextField();
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 0, 5);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 0;
 		gbc_textField.gridy = 5;
 		add(textField, gbc_textField);
 		textField.setColumns(10);
 
 		final JButton btnSend = new SendButton(g, m, this);
 		GridBagConstraints gbc_btnSend = new GridBagConstraints();
 		gbc_btnSend.gridx = 1;
 		gbc_btnSend.gridy = 5;
 		add(btnSend, gbc_btnSend);
 
 		textField.addKeyListener(new KeyListener() {
 			public void keyTyped(KeyEvent e) { }
 			public void keyPressed(KeyEvent e) { }
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER)
 					btnSend.doClick(10);
 			}
 		});
 
 	}
 
 	public void setDocument(StyledDocument doc) {
 		txtpnHello.setDocument(doc);
 	}
 	
 	public void setLegend(DefaultListModel model) {
 		userLegend.setModel(model);
 	}
 	
 	/**
 	 * Used to pop the other selected buttons
 	 * @param The clicked button
 	 */
 	public void popOthers(Object o) {
 		for (int i=0;i<toolBar.getComponentCount();i++) {
 			JToggleButton b = (JToggleButton) toolBar.getComponent(i);
 			if (!b.equals(o))
 				b.setSelected(false);
 			else
 				b.setSelected(true);
 		}
 	}
 
 	public String getText() {
 		String t =  new String(textField.getText());
 		textField.setText("");
 		return t;
 	}
 
 	public int getFontSize() {
 		return Integer.parseInt(comboBox.getSelectedItem().toString());
 	}
 
 	public Color getFontColor() {
 		return cls[comboBox_1.getSelectedIndex()];
 	}
 
 	// functie de afisare a unui text mai mare, restrans la MAX_LINE_CHARS pe linie
 	private String prettyPrinting(String s) {
 		int buckets = s.length() / MAX_LINE_CHARS;
 		String newS = "";
 		for (int i = 0; i < buckets; i++) {
 			if (i == 0) {
 				newS = newS.concat(" " + s.substring(i * MAX_LINE_CHARS, (i + 1) * MAX_LINE_CHARS) + "\n");
 			} else
 				newS = newS.concat("   " + s.substring(i * MAX_LINE_CHARS, (i + 1) * MAX_LINE_CHARS) + "\n");
 		}
 		if (buckets == 0)
 			newS = newS.concat(" ");
 		else
 			newS = newS.concat("   ");
 		newS = newS.concat(s.substring(buckets * MAX_LINE_CHARS) + "\n");
 		return newS;
 	}
 
 	public void printText(String username, String string, int fontSize, Color fontColor) {
 		StyleContext ctx = new StyleContext();
 		StyledDocument doc = txtpnHello.getStyledDocument();
 		Style st = ctx.getStyle(StyleContext.DEFAULT_STYLE);
 		StyleConstants.setBold(st, true);
 		
 		try {
 			doc.insertString(doc.getLength(), username + ":", st);
 		} catch (BadLocationException e1) {
 
 		}
 		
 		StyleConstants.setBold(st, false);
 		StyleConstants.setFontSize(st, fontSize);
 		StyleConstants.setForeground(st, fontColor);
 
 		try {
 			doc.insertString(doc.getLength(), prettyPrinting(string), st);
 		} catch (BadLocationException e1) {
 
 		}
 
 		txtpnHello.setDocument(doc);
 		txtpnHello.setCaretPosition(txtpnHello.getText().length());
 	}
 
 	public void setLegendModel(DefaultListModel l, String groupName) {
 		userLegend.setModel(l);
 		userLegend.setCellRenderer(new CustomListCellRenderer(med,groupName));
 	}
 	
 	public void setHistory(DefaultStyledDocument document) {
 		txtpnHello.setDocument(document);
 	}
 
 }
 
 
 class CustomListCellRenderer extends DefaultListCellRenderer{
 
 	private static final long serialVersionUID = 1L;
 	
 	Mediator m;
 	String group;
 	public CustomListCellRenderer(Mediator m, String group) {
 		this.m = m;
 		this.group = group;
 	}
 
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 
 		Component ret = super.getListCellRendererComponent(list, value, index, false, false); // List is not selectable
 
 		JLabel label = (JLabel) ret ;
 		@SuppressWarnings("unchecked")
 		Pair<String, Color> p = (Pair<String, Color>)(value);  
 		String username = p.getK();
 		String conn;
 		if (m.userInGroup(username, group))
			conn = " (c)";
 		else
			conn = " (u)";
 		label.setText(p.getK()+conn); 
 
 		label.setForeground(p.getV());
 		return label;
 	}
 
 
 }
