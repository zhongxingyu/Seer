 package transactions;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import ui.LibraryDB;
 
 import model.BookCopy;
 import model.Borrowing;
 
 public class CheckOverdue extends JFrame{
 
 	// dimensions of the window
 	private int WIDTH = 400;
 	private int HEIGHT = 500;
 	private JList list;
 	private DefaultListModel listModel;
 	private JButton send2AllButton;
 	private JButton send2SelectedButton;
 	private JButton addButton;
 	private List<Borrowing> overdues;
 	private List<Integer> selected;
 	final JTextArea textArea = new JTextArea(5, 12);
 	private int currentIndex = -1; // the index of the current selected item on the list
 	
 	public CheckOverdue(){
 		super("Check Overdue");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //Exits the window when user clicks on "x"
 		
 		overdues = getOverdueItems();
 		selected = new ArrayList<Integer>();
 		
 		initPanel();
 
 		// Center the window
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = screenSize.width/2 - WIDTH/2;
 		int y = screenSize.height/2 - HEIGHT/2;
 		this.setLocation(x,y);
 
 		setSize(WIDTH,HEIGHT);
 		setVisible(true);
 	}
 	
 	private void initList(){
 		
         listModel = new DefaultListModel();
         for (int i=0;i<overdues.size();i++){
         	Borrowing b = overdues.get(i);
         	String msg = 
         			"User " + b.getBid() + 
         			" borrowered " + b.getCallNumber() + 
         		        	" " + b.getCopyNo();
         	listModel.addElement(msg);
         }
 
 
 
         
 	}
 	
 	private void initPanel(){
 		int disp = 20;
 		
 		JPanel p = new JPanel();
 		p.setLayout(null);
        
         //****************** TextArea ***********************
         textArea.setEditable(false);
         JScrollPane scrollPane = new JScrollPane(textArea);
         scrollPane.setBounds(WIDTH*5/9 - disp, HEIGHT/8 - disp, 4*WIDTH/9, HEIGHT/2);
         add(scrollPane, BorderLayout.CENTER);
         
         addButton = new JButton("Select Borrowing");
         addButton.setBounds(WIDTH/4, HEIGHT*5/8, WIDTH/2, HEIGHT/10);
         addButton.setEnabled(false);
         addButton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				onClickAddSelected();
 				
 			}
         	
         });
         p.add(addButton);
         
         send2AllButton = new JButton("Send Email To All");
         send2AllButton.setBounds(disp, HEIGHT*6/8 + disp, WIDTH*3/7, HEIGHT/10);
         send2AllButton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				onClickSendAll();
 				
 			}
         	
         });
         p.add(send2AllButton);
         
 		
         send2SelectedButton = new JButton("Send Email To Selected");
         send2SelectedButton.setBounds(WIDTH/2, HEIGHT*6/8 + disp, WIDTH*3/7, HEIGHT/10);
         send2SelectedButton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				onClickSendSelected();
 				
 			}
         	
         });
         p.add(send2SelectedButton);
         
         // *********************** LIST **************************
 		initList();
         list = new JList(listModel);
         list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         list.setSelectedIndex(0);
         list.addListSelectionListener(new ListSelectionListener(){
 
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 		        if (e.getValueIsAdjusting() == false) {
		        	 
		            if (list.getSelectedIndex() == -1) {
		            	addButton.setEnabled(false);
		 
		            } else {
		            	addButton.setEnabled(true);
		            }
 		        }
 			}
         	
         });
         list.setVisibleRowCount(5);
         
         MouseListener mouseListener = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 1) {
                     currentIndex = list.locationToIndex(e.getPoint());
                     System.out.println("clicked on Item " + currentIndex);
                  }
             }
         };
         list.addMouseListener(mouseListener);
         
         JScrollPane listScrollPane = new JScrollPane(list);
         listScrollPane.setBounds(WIDTH/9 - disp, HEIGHT/8 - disp, WIDTH*4/9 , HEIGHT/2);
         p.add(listScrollPane);
         
 		this.add(p);
 		
 	}
 	
 	protected void onClickAddSelected() {
 		System.out.println("current is :::: " + currentIndex);
 		Borrowing selectedBorrowing = overdues.get(currentIndex);
 		addUser(selectedBorrowing);
 		String message = new String("User " + selectedBorrowing.getBid() + " borrowed " + selectedBorrowing.getCallNumber() + " " + selectedBorrowing.getCopyNo()+ "\n");
 		textArea.append(message);
 		listModel.remove(currentIndex);
 	}
 
 	private void addUser(Borrowing b){
 		if (!selected.contains(b)) selected.add(b.getBid());
 	}
 	
 	private void onClickSendSelected() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void onClickSendAll() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	// Returns list of overdue Borrowings
 	private ArrayList<Borrowing> getOverdueItems(){
 		ArrayList<Borrowing> result = new ArrayList<Borrowing>();
 		result = LibraryDB.getManager().getOutBorrowings();
 		ArrayList<Borrowing> toRemove = new ArrayList<Borrowing>();
 		for (Borrowing b : result){
 			if (isOverDue(b)) toRemove.add(b);
 		}
 		result.removeAll(toRemove);
 		return result;
 	}
 
 	private boolean isOverDue(Borrowing b) {
 		int timeLimit = 0;
 		Calendar currentCal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
 		
 		try {
 			timeLimit = LibraryDB.getManager().getTimeLimit(b.getBid());
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			return false;
 		}
 		
 		Calendar dueCal = stringToCalendar(b.getOutDate());
 		dueCal.add(Calendar.DATE, timeLimit);
 		if (dueCal.after(currentCal)) return true;	
 		System.out.println(b.getBid());
 		return false;
 	}
 	
 	// converts "yyyy/mm/dd" to Calendar.
 	private Calendar stringToCalendar(String str){
 		String parts[] = str.split("/");
 		int year = Integer.parseInt(parts[0]);
 		int month = Integer.parseInt(parts[1]) - 1;
 		int date = Integer.parseInt(parts[2]);
 		Calendar cal = Calendar.getInstance();
 		cal.set(year, month, date);
 		return cal;
 		
 	}
 }
