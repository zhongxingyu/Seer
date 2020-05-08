 package editor.window;
 
 import java.util.ArrayList;
 
 import de.matthiasmann.twl.Color;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.DialogLayout.Group;
 import de.matthiasmann.twl.ResizableFrame;
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 
 public class PaletteMenu extends ResizableFrame implements ActionListener {
 	private DialogLayout preview_layout;
 	private DialogLayout grid_layout;
 	private DialogLayout frame_layout;
 	private ArrayList<ColorCell> grid;
 	private ColorCell primary_color;
 	private ColorCell alt_color;
 	
 	private ArrayList<ActionListener> action_listeners;
 	
 	public PaletteMenu(Integer grid_size) {
 	  action_listeners = new ArrayList<ActionListener>();
 		setTitle("Palette Menu");
 
 		// Create the layout and button instances
 		frame_layout = new DialogLayout();
 		frame_layout.setTheme("frame_layout");
 		
 		preview_layout = new DialogLayout();
 		grid_layout = new DialogLayout();
 		
 		Group h_frame = frame_layout.createParallelGroup()
 			.addWidget(preview_layout)
 			.addWidget(grid_layout);
 		Group v_frame = frame_layout.createSequentialGroup()
 			.addWidget(preview_layout)
 			.addWidget(grid_layout);
 		
 		frame_layout.setHorizontalGroup(h_frame);
 		frame_layout.setVerticalGroup(v_frame);
 		
 		primary_color  = new ColorCell();
 		alt_color = new ColorCell();
 		
 		Group h_preview = preview_layout.createParallelGroup()
 			.addWidget(primary_color)
 			.addWidget(alt_color);
 		Group v_preview = preview_layout.createSequentialGroup()
 			.addWidget(primary_color)
 			.addWidget(alt_color);
 	
 		//Add preview colors
 		preview_layout.setHorizontalGroup(h_preview);
 		preview_layout.setVerticalGroup(v_preview);
 
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
 		Group h_grid = grid_layout.createParallelGroup();
 		// Group for holding the Vertical alignment of the buttons
 		Group v_grid = grid_layout.createParallelGroup();
 		// Generic row up buttons
 		Group row = grid_layout.createSequentialGroup(); 
 		
 		//Create the horizontal rows
 		for(int i=0;i<num_rows;i++) {
 			row = grid_layout.createSequentialGroup();
 			for(int j=0;j<(grid_size/num_rows);j++) {
 				row.addWidget(grid.get(j+(i*(grid_size/num_rows))));
 			}
 			h_grid.addGroup(row);
 		}
 
 		//Create vertical rows
 		for(int i=0;i<(grid_size/num_rows);i++) {
 			row = grid_layout.createSequentialGroup();
 			for(int j=0;j<num_rows;j++) {
 				row.addWidget(grid.get(i+(j*(grid_size/num_rows))));
 			}
 			v_grid.addGroup(row);
 		}
 
 		// All Dialog layout groups must have both a HorizontalGroup and
 		// VerticalGroup
 		// Otherwise "incomplete" exception is thrown and layout is not applied
 		grid_layout.setHorizontalGroup(h_grid);
 		grid_layout.setVerticalGroup(v_grid);
 
 		// Make sure to add the layout to the frame
 		add(frame_layout);
 		// !!! END EXAMPLE !!!//
 	}
 	
 	public Color getPrimaryColor(){
 	  return primary_color.getColor();
 	}
 
 	public void addActionListener(ActionListener al){
 	  action_listeners.add(al);
 	}
 	
 	public void fireActionEvent(){
 	  for(ActionListener al : action_listeners){
 	    al.actionPerformed(new ActionEvent(this));
 	  }
 	}
 	
 	private void addCellListener(ActionListener listener){
 	    for(int i = 0; i < grid.size(); i++){
 	      grid.get(i).addActionListener(listener);
 	    }
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		System.out.println(((ColorCell) e.getSource()).getColor().toString());
 		primary_color.setColor(((ColorCell) e.getSource()).getColor());
 		fireActionEvent();
 	}
 }
