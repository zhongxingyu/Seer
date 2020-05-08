 package view;
 import java.awt.*;
 import javax.swing.*;
 public class GUIView extends JPanel {
 	private HeaderView header;
 	private ListView list;
 	private JPanel headerPanel = new JPanel();
 	private JPanel listPanel = new JPanel();
 	/**
 	 * Creates the frame.
 	 */
 	public GUIView(HeaderView header, ListView list) {
 		this.header = header;
 		this.list = list;
 		headerPanel.add(header);
 		listPanel.add(list);	
 		setLayout(new BorderLayout(0, 0));
 		add(headerPanel, BorderLayout.NORTH);
 		add(listPanel, BorderLayout.CENTER);
 		setVisible(true);
 	}	
 }
