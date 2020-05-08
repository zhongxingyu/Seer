 package filelist;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 public class ListPopUp extends JPopupMenu {
 	private static final long serialVersionUID = -8544109534020676273L;
 
 	public ListPopUp(FileList list) { Initialize(list); }
 	
 	public void Initialize(FileList list) {
 		add(AddMenuItem(list, "Show current folder"));
 	}
 	
 	JMenuItem AddMenuItem(final FileList list, String text) {
 	    JMenuItem m = new JMenuItem(text);
 	    m.addActionListener(new ActionListener() {
 	    	public void actionPerformed(ActionEvent e) {
 	    		ListItem i = list.getItemFromCursor();
 	    		if (i == null) {
 	    			Object o = list.getSelectedValue();
 	    			if (o != null)
 	    				((ListItem)list.getSelectedValue()).OpenFolder();
 	    		}
 	    		else i.OpenFolder();
 	        }
 	    });
 	    return m;
 	}	
 }
