 /**
  * Class PeopleBrowser
  *
  * A subclass containing everything needed to handle the people browser
  *
  * @author Leon Blakey/Lord.Quackstar
  */
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import java.util.Map.Entry;
 
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.MouseInputAdapter;
 
 import javax.swing.table.TableCellRenderer;
 
 import javax.swing.text.View;
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 class PeopleBrowser extends JSplitPane implements ActionListener, ListSelectionListener {
 	
 	JTable userTable;
 	int currentRow = -1;
 	TreeMap<String,TreeMap<String,String>> userInfo = new TreeMap<String,TreeMap<String,String>>();
 	JSplitPane userPanel;
 	JPanel avatarPanel,infoPanel;
 	
 	public JSplitPane generate() {
 		System.out.println("PeopleBrowser Class initiated");
 		
 		setOrientation(HORIZONTAL_SPLIT );
 		userPanel = new JSplitPane();
 		userPanel.setDividerSize(0);
 		userPanel.setLeftComponent(avatarPanel = new JPanel());
 		userPanel.setRightComponent(infoPanel = new JPanel());
 
 		infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.Y_AXIS));
 		infoPanel.add(new JLabel("Select User on right",JLabel.CENTER));
 		setRightComponent(userPanel);
 		
 		
 		Vector columnNames = new Vector();
 		columnNames.add("Real Name");
 		columnNames.add("Gamers Tag");
 		
 		//Need to get list of users from DB
 		String response = Globs.webTalk("mode=fetchUsers",null,null);
 		System.out.println("Response: "+response);
 		Vector userList = null;
 		try {
 			TreeMap<String,String> currentUser = new TreeMap<String,String>();
 			Set userSet = new JSONObject(response).myHashMap.entrySet();
 			userList = new Vector(userSet.size());
 			Iterator usersItr = userSet.iterator();
 			Vector currentVector = null;
 			while(usersItr.hasNext()) {
 				currentVector = new Vector(2);
 				Map.Entry currentKey = (Map.Entry)usersItr.next();
 				String realName = (String)currentKey.getKey();
 				currentVector.add(realName);
 				Iterator userItr = new JSONObject((String)currentKey.getValue()).myHashMap.entrySet().iterator();
 				while(userItr.hasNext()) {
 					Map.Entry currentValue = (Map.Entry)userItr.next();
 					String value = (currentValue.getValue() == JSONObject.NULL) ? null : (String)currentValue.getValue();
 					String key = (String)currentValue.getKey();
 					currentUser.put(key,value);	
 					if(key.equals("gamersTag")) {
 						currentVector.add(value);
 					}
 				}
 				userList.add(currentVector);
 				userInfo.put(realName,currentUser);
 			}
 		}
 		catch(JSONException e) {
 			System.err.println("Unable to parse response!");
 			e.printStackTrace();
 		}
         userTable = new RollOverTable(userList, columnNames);
         userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         userTable.setPreferredScrollableViewportSize(new Dimension(200, 70));
         userTable.setFillsViewportHeight(true);
         userTable.setAutoCreateRowSorter(true);
         userTable.getSelectionModel().addListSelectionListener(this);
         setLeftComponent(new JScrollPane(userTable)); //Left component
 		return this;
 	}
 	public void actionPerformed(ActionEvent e) {
     }
 
 	public void valueChanged(ListSelectionEvent event) {
 		int viewRow = userTable.getSelectedRow();
    		if (currentRow == viewRow || viewRow < 0) 
    			return;
  		currentRow = viewRow;
 		String userName = (String)userTable.getValueAt(viewRow,0);
 		System.out.println("Selected Row in view: "+viewRow+" UserName: "+userName);
 		
 		//Start making profile
 		infoPanel.removeAll();
 		avatarPanel.removeAll();
 		TreeMap<String,String> curUsr = userInfo.get(userName);
 		
 		//Show avatar
		ImageIcon avatar = (curUsr.get("avatar") == null || curUsr.get("avatar").isEmpty()) ? new ImageIcon("no_avatar.gif") : Globs.resizePic((String)curUsr.get("avatar"),200,200);
 		JLabel avatarL = new JLabel(avatar,JLabel.CENTER);
 		Dimension size = new Dimension(avatar.getIconWidth(),avatar.getIconHeight());
 		avatarL.setPreferredSize(size);
 		avatarL.setMaximumSize(size);
 		avatarL.setMinimumSize(size);
 		avatarPanel.add(avatarL);
 		
 		//Show info header
 		infoPanel.add(new JLabel("<HTML><H2>"+curUsr.get("name") + " AKA " + curUsr.get("gamersTag")+"</H2></HTML>",JLabel.CENTER));
 		
 		//Show desc
 		infoPanel.add(new JLabel(curUsr.get("desc"),JLabel.CENTER));
 		
 		//Seperator
 		infoPanel.add(Globs.setSize(new JSeparator(),30,0));
 		
 		//Show admin or not
 		String role = (Integer.parseInt(curUsr.get("admin")) == 1) ? "Admin" : "User";
 		infoPanel.add(new JLabel("<HTML><b>Role:</b> "+role+"</html>",JLabel.CENTER));
 		
 		//Show grade
 		infoPanel.add(new JLabel("<HTML><b>Grade: </b>"+curUsr.get("gradeNum")+"</html>",JLabel.CENTER));
 		
 		//Display user info in tiny boxes
 		JPanel bestAtPanel = new JPanel();
 		bestAtPanel.setLayout(new BoxLayout(bestAtPanel,BoxLayout.X_AXIS));
 		bestAtPanel.add(new JLabel("<HTML><B>Best At</B></HTML>"));
 		bestAtPanel.add(new JLabel((String)curUsr.get("bestAt")));
 		infoPanel.add(bestAtPanel);		
 			
 		//Display user info in tiny boxes
 		JPanel favGamesPanel = new JPanel();
 		favGamesPanel.setLayout(new BoxLayout(favGamesPanel,BoxLayout.X_AXIS));
 		favGamesPanel.add(new JLabel("<HTML><B>Favorite Games</B></HTML>"));
 		favGamesPanel.add(new JLabel((String)curUsr.get("favGames")));
 		infoPanel.add(favGamesPanel);
 		
 		//Component[] components = (Component[])Globs.mergeArrays(infoPanel.getComponents(),bestAtPanel.getComponents(),favGamesPanel.getComponents());
 		Component[] components = infoPanel.getComponents();
 		for (Component component : components) {
 			if(component instanceof  JPanel || component instanceof  JTextArea || component instanceof  JLabel || component instanceof  JSeparator) {
 				//((JComponent)component).setBorder(BorderFactory.createLineBorder(Color.BLUE));
 				((JComponent)component).setAlignmentX(JComponent.CENTER_ALIGNMENT);
 			}
 		}
 		userPanel.resetToPreferredSizes();
 		userPanel.repaint();
 		userPanel.revalidate();
 	}
     
     class RollOverTable extends JTable  {
 	 	
 	    private int rollOverRowIndex = -1;
 	    
 	    public RollOverTable(Vector rowData, Vector columnNames) {
 	        super(rowData,columnNames);
 	        RollOverListener lst = new RollOverListener();
 	        addMouseMotionListener(lst);
 	        addMouseListener(lst);
 	        selectionModel.addListSelectionListener(this);
 	    }
 	    
 	    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
 	        Component c = super.prepareRenderer(renderer, row, column);
 	        if( isRowSelected(row) ) {
 	        	c.setBackground(Color.RED);
 	        }
 	        else if (row == rollOverRowIndex) {
 	            c.setForeground(getSelectionForeground());
 	            c.setBackground(getSelectionBackground());
 	        }
 	        else {
 	            c.setForeground(getForeground());
 	            c.setBackground(getBackground());
 	        }
 	        return c;
 	    }
 	    public boolean isCellEditable(int rowIndex, int vColIndex) { 
 	    	return false; 
 	    }
 	 
 	    private class RollOverListener extends MouseInputAdapter {
 	 
 	        public void mouseExited(MouseEvent e) {
 	            rollOverRowIndex = -1;
 	            repaint();
 	        }
 	 
 	        public void mouseMoved(MouseEvent e) {
 	            int row = rowAtPoint(e.getPoint());
 	            if( row != rollOverRowIndex ) {
 	                rollOverRowIndex = row;
 	                repaint();
 	            }
 	        }
 	    }
     }
 }
