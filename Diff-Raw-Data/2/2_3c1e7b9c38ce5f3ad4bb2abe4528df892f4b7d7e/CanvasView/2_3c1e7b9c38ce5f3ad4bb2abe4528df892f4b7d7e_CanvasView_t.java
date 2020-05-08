 package sketch.view;
 
 import sketch.model.*;
 import sketch.model.object.*;
 import sketch.*;
 import javax.swing.*;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.event.MouseInputListener;
 import java.awt.event.MouseEvent;
 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 
 public final class CanvasView extends JComponent{
 	private final MouseInputAdapter drawMode = new DrawMode();
 	private final MouseInputAdapter eraseMode = new EraseMode();
 	private final MouseInputAdapter selectMode = new SelectMode();
 
 	private Shape buffer = null;
 	private ArrayList<DrawableObject> selected = new ArrayList<DrawableObject>();
 	private final SketchModel model;
 
 	public ModeState getModeState(){
 		return new ModeState();
 	}
 
 	public Dimension getPreferredSize() {
    return new Dimension(780,505);
 	}
 
 	public CanvasView(SketchModel model){
 		this.model = model;
 		this.setForeground(Color.BLACK);
 		this.setBackground(Color.WHITE);
 		model.addView(new IView(){
 			public void updateView(){
 				repaint();
 			}
 			public void resetView(){
 				changeModeGarbageCollect();
 				updateView();
 			}
 		});
 		setMainMode(drawMode);
 	}
 
 	private void drawSelectedArea(Graphics2D g2d, int currentFrame){
 		int x = 0;
 		int y = 0;
 		if (buffer != null){
 			Point delta = null;
 			if (!selected.isEmpty()){
 				Path path = selected.get(0).getPath();
 				if (path != null){
 					delta = selected.get(0).getDelta(currentFrame);
 					x = (int)delta.getX();
 					y = (int)delta.getY();
 				}
 			}
 			g2d.setColor(Config.SELECTED_COLOUR);
 			Stroke temp = g2d.getStroke();
 			g2d.setStroke(Config.DASHED);
 			g2d.translate(x,y);
 			g2d.draw(buffer);
 			g2d.translate(-x,-y);
 			g2d.setColor(Color.BLACK);
 			g2d.setStroke(temp);
 		}
 	}
 
 	public Point findCentreOfSelected(){
 		if (buffer == null) return null;
 		Rectangle rect = buffer.getBounds();
 		return new Point((int)(rect.getX() + rect.getWidth() / 2),
 			(int)(rect.getY() + rect.getHeight() / 2));
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 
 		int currentFrame = model.getFrame();
 		for (DrawableObject obj : model.getObjLst()){
 			if (!obj.exist(currentFrame)) continue;
 			obj.draw(g2d, currentFrame);
 		}
 
 		drawSelectedArea(g2d, currentFrame);
 
 		g2d.setColor(Color.RED);
 		for (DrawableObject obj : selected){
 			obj.draw(g2d, currentFrame);
 			obj.drawPath(g2d, currentFrame);
 		}
 		g2d.setColor(Color.BLACK);
 	}
 
 	public void changeModeGarbageCollect(){
 		selected.clear();
 		buffer = new Polygon();
 	}
 
 	public void setMainMode(MouseInputListener mil){
 		changeModeGarbageCollect();
 		registerControllers(mil);
 		model.resetAllViews();
 	}
 
 	private void registerControllers(MouseInputListener mil) {
 		this.removeMouseListener(drawMode);
 		this.removeMouseListener(eraseMode);
 		this.removeMouseListener(selectMode);
 
 		this.removeMouseMotionListener(drawMode);
 		this.removeMouseMotionListener(eraseMode);
 		this.removeMouseMotionListener(selectMode);
 
 		this.addMouseListener(mil);
 		this.addMouseMotionListener(mil);
 	}
 
 	class DrawMode extends MouseInputAdapter{
 		DrawableObject obj = null;
 		long prevTime = System.nanoTime();
 	
 		public void mousePressed(MouseEvent e) {
 			int frame = model.getFrame();
 			obj = new DrawableObject(frame);
 			model.addObject(obj);
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			obj.addPoint(e.getPoint());
 			if (System.nanoTime() - prevTime < Config.TICK_PER_NANOSEC) return;
 			prevTime = System.nanoTime();
 			model.resetAllViews();
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			if (obj == null) return;
 			obj.finalize();
 			obj = null;
 			model.resetAllViews();
  		}
 	}
 
 	class SelectMode extends MouseInputAdapter{
 		Boolean alreadySelected = false;
 		Point prevMouseLoc;
 		Path path;
 		int numTicks, startTick;
 
 		public void mousePressed(MouseEvent e) {
 			prevMouseLoc = e.getPoint();
 			numTicks = 0;
 			startTick = model.getFrame();
 			if (!selected.isEmpty()){// && buffer.contains(e.getX(), e.getY())){
 				Point pt = PointTools.ptDiff(e.getPoint(),selected.get(0).getDelta(startTick));
 				if (buffer.contains(pt.getX(), pt.getY())){
 					Point centre = findCentreOfSelected();
 					for (DrawableObject obj : selected){
 						if (obj.getPath() != null) continue;
 						obj.setPath(new Path(centre));
 					}
 					alreadySelected = true;
 					return;
 				}
 			}
 			alreadySelected = false;
 			buffer = new Polygon();
 			selected.clear();
 			model.resetAllViews();
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			if(alreadySelected){
 				Rectangle rect = buffer.getBounds();
 				for (DrawableObject obj : selected){
 					obj.addPathDelta(startTick + numTicks, PointTools.ptDiff(e.getPoint(), prevMouseLoc));
 				}
 				prevMouseLoc = e.getPoint();
 				model.setFrame(startTick + numTicks++);
 			} else {
 				Point pt = e.getPoint();
 				((Polygon)buffer).addPoint((int)pt.x, (int)pt.y);
 			}
 			Shape temp = buffer;
 			ArrayList<DrawableObject> tempSelected = new ArrayList<DrawableObject>(selected);
 			
 			model.resetAllViews();
 			buffer = temp;
 			selected = tempSelected;
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			if (!alreadySelected){
 				pushSelectedObj();
 			}
 			if (selected.isEmpty()){
 				buffer = null;
 			} else {
 				setTightSelectBounds();
 			}
 			Shape temp = buffer;
 			ArrayList<DrawableObject> tempSelected = new ArrayList<DrawableObject>(selected);
 			
 			model.resetAllViews();
 			buffer = temp;
 			selected = tempSelected;
 		}
 
 		private void setTightSelectBounds(){
 			Polygon temp = new Polygon();
 			for (DrawableObject obj : selected){
 				for (Point pt : obj.getPtLst()){
 					temp.addPoint((int)pt.x, (int)pt.y);
 				}
 			}
 			buffer = temp.getBounds();
 		}
 
 		private void pushSelectedObj(){
 			for (DrawableObject obj : model.getObjLst()){
 				if (obj.containedIn(buffer, model.getFrame())){
 					selected.add(obj);
 				}
 			}
 		}
 	}
 
 	class EraseMode extends MouseInputAdapter{
 
 		public void mouseDragged(MouseEvent e) {
 			Shape sh = new Rectangle((int)(e.getX() -5), (int)(e.getY()-5), 10, 10);
 			int frame = model.getFrame();
 			for (Iterator<DrawableObject> it =  model.getObjLst().iterator(); it.hasNext();){
 				DrawableObject obj = it.next();
 				if (!obj.exist(frame))continue;
 				if (obj.containedPartlyIn(sh,frame)){
 					obj.erasedAt(frame);
 					if (obj.nonExistence()){
 						it.remove();
 					}
 				}
 			}
 			model.resetAllViews();
 		}
 	}
 
 	class ModeState{
 		public MouseInputAdapter getDraw(){
 			return drawMode;
 		}
 		public MouseInputAdapter getErase(){
 			return eraseMode;
 		}
 		public MouseInputAdapter getSelect(){
 			return selectMode;
 		}
 		public void setMode(MouseInputAdapter mil){
 			setMainMode(mil);
 		}
 	}
 }
