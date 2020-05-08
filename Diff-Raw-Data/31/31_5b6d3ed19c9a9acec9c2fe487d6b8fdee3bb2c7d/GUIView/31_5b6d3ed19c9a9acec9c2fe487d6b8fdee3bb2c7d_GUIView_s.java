 package view;
 import java.awt.*;
 import javax.swing.*;
 public class GUIView extends JPanel {
 	private HeaderView header;
 	private ListView list;
 	private CategoryView cat;
 	private JPanel headerPanel = new JPanel();
 	private JPanel listPanel = new JPanel();
 	private JPanel categoryPanel = new JPanel();
 	/**
 	 * Creates the frame.
 	 */
 	public GUIView(HeaderView header, ListView list, CategoryView cat) {
 		this.header = header;
 		this.list = list;
		this.cat=cat;
 		
 		headerPanel.add(header);
 		listPanel.add(list);	
 		categoryPanel.add(cat);
		setLayout(new BorderLayout(0, 0));
 		add(headerPanel, BorderLayout.NORTH);
 		add(listPanel, BorderLayout.CENTER);
 		add(categoryPanel, BorderLayout.WEST);
 		setVisible(true);
 	}	
 }
