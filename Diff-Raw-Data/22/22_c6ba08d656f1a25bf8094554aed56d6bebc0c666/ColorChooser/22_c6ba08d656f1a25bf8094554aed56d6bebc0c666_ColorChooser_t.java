 package gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.EmptyBorder;
 
 import app.Mediator;
 
 import java.util.Vector;
 
 public class ColorChooser extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 
 	/**
 	 * Create the frame.
 	 */
 	public ColorChooser(Vector<Color> colors, final Mediator m, final String user, final String group) {
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		Vector<Object> elements = new Vector<Object>(colors);
 		elements.add(0, new String("Choose color..."));
 		final JComboBox box = new JComboBox(elements);
 		box.setRenderer(new MyComboboxRenderer());
 		box.setBackground(Color.lightGray);
 		box.setForeground(Color.black);
 		contentPane.add(box);
 		setContentPane(contentPane);
 		setVisible(true);
 		setLocationRelativeTo(null);
 		setSize(200, 100);
 		box.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Color c = (Color) box.getSelectedItem(); 
 				m.joinGroupCommand(user,group,c);
 				dispose();
 			}
 		});
 	}
 
 }
 
 class MyComboboxRenderer implements ListCellRenderer {
 	  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value,
 			int index, boolean isSelected, boolean cellHasFocus) {
 		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
 		        isSelected, cellHasFocus);
 		if (value instanceof Color) {
 			if (((Color)value).equals(Color.black))
 				renderer.setText("Black");
 			if (((Color)value).equals(Color.blue))
 				renderer.setText("Blue");
 			if (((Color)value).equals(Color.cyan))
 				renderer.setText("Cyan");
 			if (((Color)value).equals(Color.darkGray))
 				renderer.setText("Dark gray");
 			if (((Color)value).equals(Color.gray))
 				renderer.setText("Gray");
 			if (((Color)value).equals(Color.green))
 				renderer.setText("Green");
 			if (((Color)value).equals(Color.lightGray))
 				renderer.setText("Light gray");
 			if (((Color)value).equals(Color.magenta))
 				renderer.setText("Magenta");
 			if (((Color)value).equals(Color.orange))
 				renderer.setText("Orange");
 			if (((Color)value).equals(Color.pink))
 				renderer.setText("Pink");
 			if (((Color)value).equals(Color.red))
 				renderer.setText("Red");
 			if (((Color)value).equals(Color.yellow))
 				renderer.setText("Yellow");
 		}
 		return renderer;
 	}
 }
