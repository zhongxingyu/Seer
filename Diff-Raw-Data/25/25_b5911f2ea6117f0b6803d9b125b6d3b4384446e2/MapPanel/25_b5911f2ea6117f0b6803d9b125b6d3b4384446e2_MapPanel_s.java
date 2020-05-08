 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class MapPanel extends JPanel{
 	private Map map=null;
 	private GridLayout grid=null;
 	private boolean random; //implement random maps later
 	private JFrame frame;
 	
 	public MapPanel(boolean random, JFrame frame){
 		this.frame=frame;
 		this.random = random;
 		setVisible(true);
		map = new Map(true, frame);
 		grid = new GridLayout(Map.NUM_ROW, Map.NUM_COL);
 		setLayout(grid);
 		JButton[][] buttons = map.getMap();
 		
 		for(int r=0; r<buttons.length; r++){
             if(buttons[r]!=null){
            for(int c=0; c<buttons[r].length; c++){
                if(buttons[r][c]!=null){
             	   System.out.println(buttons[r][c].toString());
                    add(buttons[r][c]);
                }
            }
        }
             
 	}
 	this.frame.setContentPane(this);
 }
 }
