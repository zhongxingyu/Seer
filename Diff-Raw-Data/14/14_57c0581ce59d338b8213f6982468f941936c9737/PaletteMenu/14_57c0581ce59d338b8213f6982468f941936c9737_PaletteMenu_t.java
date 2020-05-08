 package editor.window;
 
 import java.util.ArrayList;
 
 import de.matthiasmann.twl.Color;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.ResizableFrame;
 import de.matthiasmann.twl.DialogLayout.Group;
 import editor.Block;
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 
 public class PaletteMenu extends ResizableFrame implements ActionListener{
 	private DialogLayout layout;
 	private ArrayList<ColorCell> grid;
 	private ColorCell primary_color;
 	private ColorCell alt_color;
 	
 	public PaletteMenu(Integer grid_size) {
 		setTitle("Palette Menu");
 
 		// Create the layout and button instances
 		layout = new DialogLayout();
 		
 		primary_color  = new ColorCell();
 		alt_color = new ColorCell();
 		
 		layout = new DialogLayout();
 
 		grid = new ArrayList<ColorCell>(grid_size);
 		
 		int num_rows=10;
 		Integer num_colors = (int)Math.pow(grid_size, 1.0f/3.0f);
 		for(int b=0;b<num_colors;b++) {
 			for(int g=0;g<num_colors;g++) {
 				for(int r=0;r<num_colors;r++) {
 					grid.add(
 						r+b+g, 
 						new ColorCell(
 							new Color(
 								new Byte((byte)(r * (256/num_colors))),
 								new Byte((byte)(g * (256/num_colors))),
 								new Byte((byte)(b * (256/num_colors))),
 								new Byte((byte)0xFF)
 							)
 						)
 					);
 				}
 			}
 		}
 		
		this.addCellListener(this);
		
 		
 		// !!!EXAMPLE OF DIALOG LAYOUT!!!//
 		// Sequential groups are like a Swing boxlayout and just lists from top
 		// to bottom
 		// Parallel groups align each start and size and can be cascaded
 		//
 		// Group for holding the Horizontal alignment of the buttons
 		Group h_grid = layout.createParallelGroup();
 		// Group for holding the Vertical alignment of the buttons
 		Group v_grid = layout.createParallelGroup();
 		// Generic row up buttons
 		Group row = layout.createSequentialGroup(); 
 		
 		
 		//Create the horizontal rows
 		for(int i=0;i<num_rows;i++) {
 			row = layout.createSequentialGroup();
 			for(int j=0;j<(grid_size/num_rows);j++) {
 				row.addWidget(grid.get(j+(i*(grid_size/num_rows))));
 			}
 			h_grid.addGroup(row);
 		}
 
 		//Create vertical rows
 		for(int i=0;i<(grid_size/num_rows);i++) {
 			row = layout.createSequentialGroup();
 			for(int j=0;j<num_rows;j++) {
 				row.addWidget(grid.get(i+(j*(grid_size/num_rows))));
 			}
 			v_grid.addGroup(row);
 		}
 
 		// All Dialog layout groups must have both a HorizontalGroup and
 		// VerticalGroup
 		// Otherwise "incomplete" exception is thrown and layout is not applied
 		layout.setHorizontalGroup(h_grid);
 		layout.setVerticalGroup(v_grid);
 
 		// Make sure to add the layout to the frame
 		add(layout);
 		// !!! END EXAMPLE !!!//
 	}
 
 	public void addCellListener(ActionListener listener){
 	    for(int i = 0; i < grid.size(); i++){
 	      grid.get(i).addActionListener(listener);
 	    }
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		System.out.println(((ColorCell) e.getSource()).getColor().toString());
 	}
 }
