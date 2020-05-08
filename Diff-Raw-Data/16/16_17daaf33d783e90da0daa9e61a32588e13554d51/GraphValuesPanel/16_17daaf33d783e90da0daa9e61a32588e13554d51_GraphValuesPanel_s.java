 package ch.zhaw.dynsys.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 import javax.swing.border.LineBorder;
 
 import ch.zhaw.dynsys.simulation.Culture;
 
 public class GraphValuesPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	// map: text field -> culture name
 	private Map<String, JTextField> textFieldMap = new HashMap<String, JTextField>();
 
 	public GraphValuesPanel() {
 		configure();
 	}
 
 	public void setValue(Culture culture) {
 		JTextField textField = textFieldMap.get(culture.getName());
 		if (textField == null) {
 			textField = createTextField(culture);
 		}
 		textField
 				.setText(NumberFormat.getInstance().format(culture.getValue()));
 	}
 
 	public void reset() {
 		for (JTextField textField : textFieldMap.values()) {
 			this.remove(textField.getParent());
 		}
 		textFieldMap.clear();
 		this.repaint();
 	}
 
 	private JTextField createTextField(final Culture culture) {
 
 		JPanel textPanel = new JPanel();
 		textPanel.setPreferredSize(new Dimension(120, 22));
 		textPanel.setBackground(Color.WHITE);
 
 		JLabel label = new JLabel(culture.getVariable().toUpperCase());
 		label.setPreferredSize(new Dimension(10, 16));
 		label.setForeground(GraphPanel.COLOR_SEQUENCE[culture.getIndex()]);
 		textPanel.add(label);
 
 		JTextField textField = new JTextField();
 		textField.setPreferredSize(new Dimension(50, 16));
 		textPanel.add(textField);
 		textField.setEditable(false);
 
		JButton minus = new JButton(new ImageIcon(
				"src/main/resources/minus.png"));
 		minus.setBackground(Color.WHITE);
 		minus.setBorderPainted(false);
 		minus.setPreferredSize(new Dimension(16, 16));
 		textPanel.add(minus);
 
 		minus.addMouseListener(new MouseAdapter() {
 
 			private Timer timer = new Timer(100, new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					culture.setValue(culture.getValue() - 5);
 					setValue(culture);
 				}
 			});
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				culture.getSimulation().stop();
 				timer.start();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				timer.stop();
 				culture.getSimulation().start();
 			}
 		});
 
		JButton plus = new JButton(new ImageIcon("src/main/resources/plus.png"));
 		plus.setBackground(Color.WHITE);
 		plus.setBorderPainted(false);
 		plus.setPreferredSize(new Dimension(16, 16));
 		textPanel.add(plus);
 
 		plus.addMouseListener(new MouseAdapter() {
 
 			private Timer timer = new Timer(100, new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					culture.setValue(culture.getValue() + 5);
 					setValue(culture);
 				}
 			});
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				culture.getSimulation().stop();
 				timer.start();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				timer.stop();
 				culture.getSimulation().start();
 			}
 		});
 
 		this.add(textPanel);
 
 		textFieldMap.put(culture.getName(), textField);
 		return textField;
 	}
 
 	private void configure() {
 		setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
 		// setLocation(50, getParent().getHeight() - 100);
 		setBackground(Color.WHITE);
 		setLayout(new GridLayout(0, 1, 5, 5));
 	}
 }
