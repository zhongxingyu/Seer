 package filelist;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
import service.Common;
import service.Utils;

 public class ListPopUp extends JPopupMenu {
 	private static final long serialVersionUID = -8544109534020676273L;
 
 	public ListPopUp(FileList list) { Initialize(list); }
 	
 	public void Initialize(FileList list) {
 		add(AddMenuItem(list, "Show current folder"));
		add(AddShuffle(list, "Shuffle"));
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
	    m.setIcon(Utils.GetIcon("popup/open.png"));
	    return m;
	}
	
	JMenuItem AddShuffle(final FileList list, String text) {
	    JMenuItem m = new JMenuItem(text);
	    m.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) { Common.tabber.GetCurrentTab().Shuffle(); }
	    });
	    m.setIcon(Utils.GetIcon("popup/shuffle.png"));
 	    return m;
 	}	
 }
