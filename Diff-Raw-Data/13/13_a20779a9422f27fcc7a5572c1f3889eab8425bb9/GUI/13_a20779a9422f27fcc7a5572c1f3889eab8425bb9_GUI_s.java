 package djproject.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 
 import net.miginfocom.swing.MigLayout;
 
 public class GUI extends JFrame implements ActionListener{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	ListenerHandler listener;
 	
 	JTabbedPane tab_pane = new JTabbedPane();
 	MigLayout layout = new MigLayout();
 	
 	JPanel panel_list = new JPanel(layout);
	JPanel panel_events = new JPanel(layout);
	JPanel panel_songs = new JPanel(layout);
	JPanel panel_wishes = new JPanel(layout);
	JPanel panel_comments = new JPanel(layout);
 	
 	JComboBox cbox_subscriptions = new JComboBox();
 	JComboBox cbox_subscriptions2 = new JComboBox();
 	
 	JButton btn_subscribeDJ = new JButton("Subscribe");
 	JButton btn_unsubscribeDJ = new JButton("Unsubscribe");
 	JButton btn_subscribeEv = new JButton("Subscribe");
 	JButton btn_unsubscribeEv = new JButton("Unsubscribe");
 	JButton btn_wish = new JButton("Request");
 	JButton btn_comment = new JButton("Send");
 	
 	JRadioButton[] rdb_rating = {
 			new JRadioButton("1"),
 			new JRadioButton("2"),
 			new JRadioButton("3"),
 			new JRadioButton("4"),
 			new JRadioButton("5")
 	};
 	
 	ButtonGroup grp_rating = new ButtonGroup();
 	
 	JTextArea txp_comments = new JTextArea();
 	
 	DefaultListModel<String> listModelDJ = new DefaultListModel<String>();
 	JList list_DJs = new JList(listModelDJ);
 	
 	DefaultListModel<String> listModelEvents = new DefaultListModel<String>();
 	JList list_Events = new JList(listModelEvents);
 	
 	DefaultListModel<String> listModelSongs = new DefaultListModel<String>();
 	JList list_Songs = new JList(listModelSongs);
 	
 	public GUI(ListenerHandler listen){
 		listener = listen;
 		
 		//Listener werden vergeben
 		btn_subscribeDJ.addActionListener(listener);
 		btn_unsubscribeDJ.addActionListener(listener);
 		btn_subscribeEv.addActionListener(listener);
 		btn_unsubscribeEv.addActionListener(listener);
 		btn_wish.addActionListener(listener);
 		btn_comment.addActionListener(listener);
 		
 		cbox_subscriptions.addActionListener(listener);
 		cbox_subscriptions2.addActionListener(listener);
 		
 		for(int i = 0; i < 5; i++){
 			rdb_rating[i].addActionListener(listener);
 			grp_rating.add(rdb_rating[i]);
 		}
 		
 		//DJ Panel wird bestckt
 		panel_list.add(new JScrollPane(list_DJs), "dock west, width 300!, height 240!");
 		panel_list.add(btn_subscribeDJ, "width 140!,height 30!, wrap");
 		panel_list.add(btn_unsubscribeDJ, "width 140!,height 30!, wrap");
 		
 		//Event Panel wird bestckt
 		panel_events.add(new JScrollPane(list_Events), "dock west, width 300!, height 240!");
 		panel_events.add(btn_subscribeEv, "width 140!,height 30!, wrap");
 		panel_events.add(btn_unsubscribeEv, "width 140!,height 30!, wrap");
 		
 		//Song Panel wird bestckt
 		
 		//Wnsche Panel wird bestckt
 		panel_wishes.add(cbox_subscriptions, "width 300!, span 2");
 		panel_wishes.add(btn_wish, "width 120!,height 30!, wrap");
 		panel_wishes.add(new JScrollPane(list_Songs), "width 420!, height 200!, span 3");
 		
 		//Kommentar Panel wird bestckt
 		panel_comments.add(cbox_subscriptions2, "width 300!, span 2");
 		panel_comments.add(btn_comment, "width 120!,height 30!, wrap");
 		panel_comments.add(new JScrollPane(txp_comments), "width 200!, height 200!, span 3");
 		
 		//Panels werden dem Tabbed Pane hinzugefgt
 		tab_pane.add("DJs", panel_list);
 		tab_pane.add("Events", panel_events);
 		tab_pane.add("Songs", panel_songs);
 		tab_pane.add("Wnsche", panel_wishes);
 		tab_pane.add("Kommentare", panel_comments);
 		add(tab_pane);
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setSize(new Dimension(450,300));
 		setResizable(false);
 		setVisible(true);
 		setTitle("DJ System - User Client");		
 	}
 
 	public void addDJ(String s){
 		listModelDJ.addElement(s);
 	}
 	
 	public void addEvent(String s){
 		listModelEvents.addElement(s);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
