 package life.display;
 
 import java.util.Iterator;
 
 import life.LIFE;
 import life.cell.Cellule;
 import life.cell.Coord;
 import net.slashie.libjcsi.CSIColor;
 import net.slashie.libjcsi.ConsoleSystemInterface;
 
 public class DisplayCurse 
 implements Display {
 	public ConsoleSystemInterface csi;
 	protected LIFE data;
 	private Coord origin = null;
 	private Coord span = null;
 	private Integer[] defaut = new Integer[] { 30, 60 };
 	private static String[] utf8 = {" ", "#"};
 
 	public DisplayCurse(ConsoleSystemInterface board) {
 		origin = new Coord(0,0);
 		span = new Coord(defaut[0], defaut[1]);
 		csi = board;
 	}
 
 	@Override
 	public void update(LIFE life) {
 		data = life;
 	}
 
 	public void show() {
 		view(new Coord(0, 0), new Coord(defaut[0], defaut[1]));
 		refresh();
 	}
 	public void view() {
 		view(new Coord(0, 0), new Coord(defaut[0], defaut[1]));
 	}
 
 	@Override
 	public void view(Coord from, Coord span) {
 		origin    = new Coord(origin.x()+from.x(), origin.y()+from.y());
 		this.span = span;
 	}
 
 	public void refresh() {
 		Iterator<Cellule> raw = data.raw();
		Coord next = (raw.hasNext()) ? raw.next() : null;
 		// continuer tant que l'on est pas dans origin <> span
 		csi.cls();
 		boolean todo = true;
		for (int y = 0, oy = origin.y(), yp = oy+span.y(); y < yp; ++y) {
			for (int x = 0, ox = origin.x(), xp =ox+span.x(); x < xp; ++x) {
 				if(todo){
 					todo = false;
 				}
 				if (next != null && next.isAt(ox+x, oy+y)) {
 					csi.print(x, y, utf8[1], CSIColor.FUCSHIA_PINK);
 					do{
 						next = (raw.hasNext()) ? raw.next() : null;
					}while(next != null && !next.isIn(ox, oy, xp, yp));
 				}
 			}
 		}
 		csi.refresh();
 	}
 
 	public void move(Coord delta) {
 		origin = new Coord(origin.x() + delta.x(), origin.y() + delta.y());
 		try {
 			refresh();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
