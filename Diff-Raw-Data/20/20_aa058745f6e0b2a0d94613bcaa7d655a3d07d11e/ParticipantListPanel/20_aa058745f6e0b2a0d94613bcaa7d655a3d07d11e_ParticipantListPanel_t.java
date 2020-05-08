 package gui;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import db.User;
 
 
 public class ParticipantListPanel {  
    public static void main(String args[]) {
       JFrame frame = new JFrame("Participants");
      Dimension d = new Dimension(200,200);
       frame.setSize(d);
       frame.setResizable(false);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      User user1 = new User(142, "Kathrine Steffensen", "morr4d1erm4nn", "kathrine.steffensen@gmail.com");
      User user2 = new User(142, "Petter Astrup", "morr4d1erm4nn", "kathrine.steffensen@gmail.com");
      User user3 = new User(142, "Espen Hellerud", "morr4d1erm4nn", "kathrine.steffensen@gmail.com");
      JList list = new JList(new DefaultListModel());
       list.setCellRenderer(new CheckListRenderer());
      ((DefaultListModel)list.getModel()).addElement(new CheckListItem(user1));
      ((DefaultListModel)list.getModel()).addElement(new CheckListItem(user2));
      ((DefaultListModel)list.getModel()).addElement(new CheckListItem(user3));
       list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       list.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent event) {
             JList list = (JList) event.getSource();
             
             // Get index of item clicked
             
             int index = list.locationToIndex(event.getPoint());
             CheckListItem item = (CheckListItem)
                list.getModel().getElementAt(index);
             
             // Toggle selected state
             
             item.setSelected(! item.isSelected());
             
             // Repaint cell
             
             list.repaint(list.getCellBounds(index, index));
          }
       });   
 
       frame.getContentPane().add(new JScrollPane(list));
       frame.pack();
       frame.setVisible(true);
    }
 
 	public ParticipantListPanel getParticipantList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
 class CheckListItem {
 	protected User user;
 	protected boolean isSelected = false;
 	
 	public CheckListItem(User u) {
		this.user = u;
 	}
 	
 	public boolean isSelected() {
 	    return isSelected;
 	}
 	
 	public void setSelected(boolean isSelected) {
 		this.isSelected = isSelected;
 	}
 	public String toString() {
 		return user.getName();
 	}
 }
 
 class CheckListRenderer extends JCheckBox implements ListCellRenderer {
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
 		setEnabled(list.isEnabled());
 		setSelected(((CheckListItem)value).isSelected());
 		setFont(list.getFont());
 		setBackground(list.getBackground());
 		setForeground(list.getForeground());
 		setText(((CheckListItem)value).toString());
 		return this;
 	}
 }
