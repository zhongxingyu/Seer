 package evopaint.commands;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import evopaint.Configuration;
 import evopaint.Selection;
 import evopaint.gui.SelectionManager;
 import evopaint.util.mapping.AbsoluteCoordinate;
 
 public class EraseCommand extends AbstractCommand {
 
 	private final Configuration config;
 	private Point location;
 	private final SelectionManager selectionManager;
 	public EraseCommand(Configuration config, SelectionManager selectionManager) {
 		this.config = config;
 		this.selectionManager = selectionManager;
 	}
 	
 	public void setLocation(Point location) {
 		this.location = location;
 	}
 	@Override
 	public void execute() {
 		int brushSize = config.brush.size / 2;
 		Selection activeSelection = selectionManager.getActiveSelection();
         if (activeSelection != null) {
 			Rectangle rectangle = activeSelection.getRectangle();
 
 			if (location.x - brushSize < rectangle.x)
 				location.x = rectangle.x + brushSize;
 			if (location.x + brushSize > rectangle.x + rectangle.width) 
 				location.x = rectangle.x + rectangle.width - brushSize;
 			if (location.y - brushSize < rectangle.y)
 				location.y = rectangle.y + brushSize;
 			if (location.y + brushSize > rectangle.y + rectangle.height)
 				location.y = rectangle.y + rectangle.height - brushSize;
         }
         Rectangle rectangle = new Rectangle(new Point(location.x - brushSize, location.y - brushSize), 
         		new Dimension(brushSize, brushSize));
         for(int x = rectangle.x; x < rectangle.x + rectangle.width; x++ ){
         	for(int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
        		config.world.remove(x, y);
         	}
         }
 	}
 
 }
