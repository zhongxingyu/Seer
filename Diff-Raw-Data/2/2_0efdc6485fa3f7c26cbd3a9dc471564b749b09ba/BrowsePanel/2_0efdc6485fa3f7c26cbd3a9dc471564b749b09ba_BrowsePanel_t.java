 package presentation;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.*;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 
 import presentation.action.BrowseBuyButtonListener;
 import presentation.action.BrowseOkButtonListener;
 import presentation.action.LoginPanelListener;
 
 /**
  * Presentation class for the start/browse tab.
  * @author Kristoffer Karlsson
  *
  */
 
 
 public class BrowsePanel extends JPanel {
 
 	private JPanel upperPanel;
 	private JPanel lowerPanel;
 	private JPanel leftPanel;
 	private JPanel lowerRightPanel;
 	private JTextField theField;
 	private JComboBox theComboBox;
 	private JButton OKButton;
 	private JScrollPane firstScrollPane;
 	private JScrollPane secondScrollPane;
 	private JLabel totalPriceLabel;
 	private JLabel customerCarrier;
 	private JLabel filler1;
 	private JLabel filler2;
 	private JLabel filler3;
 	private JLabel upperLabel;
 	private JLabel lowerLabel;
 	private JButton buyButton;
 	private JTable upperTable;
 	private JTable lowerTable;
 	private int price;
 	
 	public int getprice() {
 		return price;
 	}
 
 	public void setprice(int price) {
 		this.price = price;
 	}
 
 	public BrowsePanel() {
 		
 
 	
 	}
 	
 	public void initActions() {
 		this.addOkButtonListener(new BrowseOkButtonListener(this));
 		this.addBuyButtonListener(new BrowseBuyButtonListener(this));
 	}
 	public void addOkButtonListener(ActionListener al) {
 		OKButton.addActionListener(al);
 	}
 	public void addBuyButtonListener(ActionListener al) {
 		buyButton.addActionListener(al);
 	}
 	
 	public void initBrowse() {
 		
 	    setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(10, 20));
 		upperPanel = new JPanel(new GridLayout(1, 6, 10, 10));
 		lowerPanel = new JPanel(new BorderLayout());
 		lowerRightPanel = new JPanel(new GridLayout(1, 2, 10, 10));
 		leftPanel = new JPanel(new GridLayout(2, 1, 15, 15));
 		
 		theField = new JTextField();
 		upperPanel.add(theField);
 		theField.setColumns(7);
 		
 		String[] stringsForBox = {
 		         "Hey",
 		         "Ho",
 		         "Yo",
 		         "Wut",
 		};
 
 		theComboBox = new JComboBox(stringsForBox);
 		upperPanel.add(theComboBox);
 		
 		OKButton = new JButton("OK");
 		upperPanel.add(OKButton);
 		
 		filler1 = new JLabel("");
 		upperPanel.add(filler1);
 		
 		filler2 = new JLabel("");
 		upperPanel.add(filler2);
 
 		filler3 = new JLabel("");
 		upperPanel.add(filler3);
 		
 		/*upperScroll = new JScrollBar();
 		upperScroll.setBounds(185, 77, 17, 185);
 		add(upperScroll);
 		
 		lowerScroll = new JScrollBar();
 		lowerScroll.setBounds(185, 286, 17, 185);
 	    add(lowerScroll);*/
 		
 		totalPriceLabel = new JLabel("Totalt pris :         " + price + "        ");
 		lowerRightPanel.add(totalPriceLabel);
 		
 		//customerCarrier = new JLabel("Kundvagn");
 		/*customerCarrier.setBounds(not sure yet);*/
 		/*add(customerCarrier);*/
 		
 		buyButton = new JButton("Kp");
 		lowerRightPanel.add(buyButton);
 		
 		upperLabel = new JLabel("Put stuff here");
 		upperLabel.setPreferredSize(new Dimension(180, 100));
 		lowerLabel = new JLabel("Here too");
 		lowerLabel.setPreferredSize(new Dimension(180, 100));
 		
 		firstScrollPane = new JScrollPane(upperLabel);
 		firstScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		firstScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		firstScrollPane.setViewportBorder(new LineBorder(Color.BLUE));
 		
 		secondScrollPane = new JScrollPane(lowerLabel);
 		secondScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		secondScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		secondScrollPane.setViewportBorder(new LineBorder(Color.BLUE));
 		
 		leftPanel.add(firstScrollPane);
 		leftPanel.add(secondScrollPane);
 		
 		
 		/*upperTable = new JTable();
 		upperTable.setBounds(169, 258, -161, -175);
 		add(upperTable);
 		
 		lowerTable = new JTable();
 		lowerTable.setBounds(169, 466, -161, -175);
 		add(lowerTable);*/
 		
 		lowerPanel.add(lowerRightPanel, BorderLayout.LINE_END);
 		this.add(upperPanel, BorderLayout.NORTH);
 		this.add(lowerPanel, BorderLayout.SOUTH);
 		this.add(leftPanel, BorderLayout.LINE_START);
 		
 	    setVisible(true);
 		
 	}
 	
 }	
 	
