 package no.ntnu.fp.gui;
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import no.ntnu.fp.model.Meeting;
 import no.ntnu.fp.model.User;
 import no.ntnu.fp.model.Meeting.State;
import no.ntnu.fp.net.network.client.CommunicationController;
 
 
 public class SelectParticipantsFrame implements ListCellRenderer {
 	
 	
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		JPanel panel = new JPanel();
 		
 		User user = (User)list.getModel().getElementAt(index);
 		
 		panel.add(new JLabel(user.getName()));
 		panel.add(new JCheckBox("", isSelected));
 		
 		return panel;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args) {
 		
 		Meeting m = new Meeting();
 		m.addParticipant(new User("havard"), State.Pending);
 		m.addParticipant(new User("andy"), State.Pending);
 		
 		JFrame frame = new ListRenderingFrame(m);
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.show();
 	}
 }
 
 class ListRenderingFrame extends JFrame implements ListSelectionListener {
 	
 	/**
 	 * 
 	 */
 	private Meeting model;
 	private ListModel listModel;
 	private ListSelectionModel selectionModel;
 	private CancelAction cAction;
 	
 	public ListRenderingFrame(Meeting meeting) {
 		
 		this.model = meeting;
 		
 		JLabel labelUsers = new JLabel("Brukere", JLabel.CENTER);
 		labelUsers.setFont(new Font("sansserif", Font.BOLD, 26));
 		
 		JButton saveButton = new JButton(new SaveAction("Lagre"));
 		cAction = new CancelAction("Avbryt", meeting);
 		JButton cancelButton = new JButton(cAction);
 	
 		JPanel participantButtons = new JPanel();
 		participantButtons.add(saveButton);
 		participantButtons.add(cancelButton);
 		
 		setTitle("Oversikt over brukere");
 		setSize(380,360);
 		addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent e) {
 				cAction.actionPerformed(null);
 			}
 		});
 		
 		
 		
 		JList list = new JList(getListOfAllUsers());
 		
 		
 		list.setSelectionModel(new DefaultListSelectionModel() {
 		    /**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			
 			
 			@Override
 		    public void setSelectionInterval(int index0, int index1) {
 		        if(super.isSelectedIndex(index0)) {
 		            super.removeSelectionInterval(index0, index1);
 		        }
 		        else {
 		            super.addSelectionInterval(index0, index1);
 		        }
 		    }
 		});
 		
 		listModel = list.getModel();
 		selectionModel = list.getSelectionModel();
 		
 		for(int i = 0; i < list.getModel().getSize(); i++) {
 			User user = (User)list.getModel().getElementAt(i);
 			if (meeting.getParticipants().contains(user)) {
 				list.setSelectedIndex(i);
 			}
 		}
 		
 		list.setCellRenderer(new SelectParticipantsFrame());
 		JScrollPane scrollPane = new JScrollPane(list);
 		
 		JPanel p = new JPanel();
 		p.add(scrollPane);
 		list.addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent arg0) {
 				if (!arg0.getValueIsAdjusting()) {
 					int index = selectionModel.getAnchorSelectionIndex();
 					if (selectionModel.isSelectedIndex(index)) {
 						User user = (User)listModel.getElementAt(index);
 						model.addParticipant(user, State.Pending);
 					}
 					else if (!selectionModel.isSelectedIndex(index)) {
 						User user = (User)listModel.getElementAt(index);
 						model.removeParticipant(user);
 					}
 					System.out.println(model);
 					System.out.println("------------------------------");
 				}
 				
 				
 			}
 		});
 		
 		getContentPane().add(labelUsers, "North");
 		getContentPane().add(p, "Center");
 		getContentPane().add(participantButtons, "South");
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	
 	}
 	private Vector getListOfAllUsers() {
		/*Vector users = new Vector();
 	
 		User user = new User("havard");
 		user.setName("Håvard Wormdal Høiby");
 		User p1 = new User("bjorn");
 		p1.setName("Bjørn Åge Tungesvik");
 		User p2 = new User("odd");
 		p2.setName("Odd Magnus Trondrud");
 		User p3 = new User("andy");
 		p3.setName("Andre Philipp");
 		User p4 = new User("eivind");
 		p4.setName("Eivind Kvissel");
 		User p5 = new User("tina");
 		p5.setName("Tina Syversen");
 
 		users.add(user);
 		users.add(p1);
 		users.add(p2);
 		users.add(p3);
 		users.add(p4);
		users.add(p5);*/
		
		CommunicationController c = CommunicationController.getInstance();
		List<User> users = c.getListOfUsers();
 
		return new Vector(users);
 	}
 	//Action for lagring av skjema
 	private class SaveAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public SaveAction(String text) {
         	super(text, null);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
 
         	System.out.println("Lagre");
         	dispose();
         }
     }
     
     //Action for � avbryte skjema
     private class CancelAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         private Meeting model;
         
         private Map<User, State> savedState = new HashMap<User, State>();
         
         public CancelAction(String text, Meeting model) {
         	super(text, null);
         	this.model = model;
         	for(User user :model.getParticipants()) {
         		savedState.put(user, model.getState(user));
         	}
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
         	this.model.removeAllParticipants();
         	
         	for(User user : savedState.keySet()) {
         		this.model.addParticipant(user, State.Pending);
         	
         	}
         	System.out.println("Avbryt");
         	System.out.println(model);
         	dispose();
         }
     }
 
 	@Override
 	public void valueChanged(ListSelectionEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
