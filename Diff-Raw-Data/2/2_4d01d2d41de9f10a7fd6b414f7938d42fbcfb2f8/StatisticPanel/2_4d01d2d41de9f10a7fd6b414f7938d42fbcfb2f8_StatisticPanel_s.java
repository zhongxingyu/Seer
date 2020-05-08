 package presentation;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.table.DefaultTableModel;
 
 import presentation.action.StatisticPanelListener;
 
 /**
  * Presentation class for the statistic tab.
  * 
  * @author Kerub
  * 
  */
 
 public class StatisticPanel extends JPanel {
 
 	private JPanel upperPanel;
 	private JPanel lowerPanel;
 	private JButton weekButton;
 	private JButton monthButton;
 	private JButton yearButton;
 	private JLabel amountSold;
 	private JLabel cashIn;
 	private JLabel titel;
 	private JLabel firstEmpty;
 	private JLabel secondEmpty;
 	private JLabel thirdEmpty;
 	private JLabel rubrik;
 	private JScrollPane firstPane;
 	private JScrollPane secondPane;
 	private JScrollPane thirdPane;
 	private JTable titleTable;
 	private JTable amountTable;
 	private JTable cashInTable;
 	
 	public StatisticPanel() {
 
 	
 	}
 
 	public void initActions() {
 		this.addWeekButtonListener(new StatisticPanelListener(this));
 		this.addMonthButtonListener(new StatisticPanelListener(this));
 		this.addYearButtonListener(new StatisticPanelListener(this));
 	}
 
 	public void addWeekButtonListener(ActionListener al) {
 		weekButton.addActionListener(al);
 	}
 
 	public void addMonthButtonListener(ActionListener al) {
 		monthButton.addActionListener(al);
 	}
 
 	public void addYearButtonListener(ActionListener al) {
 		yearButton.addActionListener(al);
 	}
 
 	public void initStatistic() {
 		
 		setBorder(new EmptyBorder(50, 30, 50, 50));
 		setLayout(new BorderLayout(50, 50));
 		upperPanel = new JPanel(new GridLayout(1, 3, 40, 40));
		lowerPanel = new JPanel(new GridLayout(1, 3));
 
 		weekButton = new JButton("Vecka");
 		upperPanel.add(weekButton);
 
 		monthButton = new JButton("Mnad");
 		upperPanel.add(monthButton);
 
 		yearButton = new JButton("r");
 		upperPanel.add(yearButton);
 
 		/*titel = new JLabel("Titel");
 		titel.setBounds(16, 332, 83, 14);
 		add(titel);
 
 		amountSold = new JLabel("Antal slda");
 		amountSold.setBounds(170, 332, 83, 14);
 		add(amountSold);
 
 		cashIn = new JLabel("Intkter");
 		cashIn.setBounds(349, 332, 46, 14);
 		add(cashIn);*/
 		
 		String [] [] title = {{"Exempel"}};
         String titleColumns[] = {"Titel"};
         DefaultTableModel titleModel = new DefaultTableModel(title, titleColumns);
 		titleTable = new JTable(titleModel);
 		
 		String [] [] amount = {{"Exempel"}};
         String amountColumns[] = {"Antal"};
         DefaultTableModel columnsModel = new DefaultTableModel(amount, amountColumns);
 		amountTable = new JTable(columnsModel);
 		
 		String [] [] cashIn = {{"Exempel"}};
 		String cashInColumns [] = {"Intkter"};
 		DefaultTableModel cashInModel = new DefaultTableModel(cashIn, cashInColumns);
 		cashInTable = new JTable(cashInModel);
 		
 		firstPane = new JScrollPane(titleTable);
 		firstPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		firstPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		firstPane.setViewportBorder(new LineBorder(Color.BLUE));
 		
 		secondPane = new JScrollPane(amountTable);
 		secondPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		secondPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		secondPane.setViewportBorder(new LineBorder(Color.BLUE));
 		
 		thirdPane = new JScrollPane(cashInTable);
 		thirdPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		thirdPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		thirdPane.setViewportBorder(new LineBorder(Color.BLUE));
 
 		/*firstEmpty = new JLabel("");
 		firstEmpty.setBounds(10, 357, 89, 114);
 		add(firstEmpty);
 
 		secondEmpty = new JLabel("");
 		secondEmpty.setBounds(164, 357, 102, 114);
 		add(secondEmpty);
 
 		thirdEmpty = new JLabel("");
 		thirdEmpty.setBounds(322, 357, 89, 114);
 		add(thirdEmpty);*/
 
 		rubrik = new JLabel("Bstsljande bcker", JLabel.CENTER);
 		lowerPanel.add(firstPane);
 		lowerPanel.add(secondPane);
 		lowerPanel.add(thirdPane);
 		this.add(rubrik, BorderLayout.SOUTH);
 		this.add(upperPanel, BorderLayout.NORTH);
 		this.add(lowerPanel, BorderLayout.CENTER);
 	
 		
 	}
 }
