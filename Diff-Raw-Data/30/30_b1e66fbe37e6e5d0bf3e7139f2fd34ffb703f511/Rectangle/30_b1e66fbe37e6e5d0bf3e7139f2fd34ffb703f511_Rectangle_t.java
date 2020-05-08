 package tools;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Rectangle2D;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFileChooser;
 
 import commands.Invoker;
 import commands.Move;
 import commands.Size;
 
 import application.Global;
 import application.MyWindow;
 
 @SuppressWarnings("serial")
 public class Rectangle extends Shape {
 
	private transient MouseListener _ml;
 	private Boolean _selected = false;
 	
 	public Rectangle(LocationVector _tempFirstCoord,
 			LocationVector _tempSecondCoord, Color theColor, int theWeight,
 			int theStyle) {
 		_pointStart = _tempFirstCoord;
 		_pointEnd = _tempSecondCoord;
 		_color = theColor;
 		_weight = theWeight;
 		_style = theStyle;
 	}
 
 	@Override
 	public LocationVector getPointStart() {
 		// TODO Auto-generated method stub
 		return _pointStart;
 	}
 
 	@Override
 	public LocationVector getPointEnd() {
 		// TODO Auto-generated method stub
 		return _pointEnd;
 	}
 
 	@Override
 	public Color getColor() {
 		// TODO Auto-generated method stub
 		return _color;
 	}
 
 	@Override
 	public int getWeight() {
 		// TODO Auto-generated method stub
 		return _weight;
 	}
 
 	@Override
 	public int getStyle() {
 		// TODO Auto-generated method stub
 		return _style;
 	}
 
 	@Override
 	public void setPointStart(LocationVector x) {
 		_pointStart = x;		
 	}
 
 	@Override
 	public void setPointEnd(LocationVector x) {
 		_pointEnd = x;
 	}
 
 	@Override
 	public void setColor(Color x) {
 		_color = x;
 	}
 
 	@Override
 	public void setWeight(int x) {
 		_weight = x;
 	}
 
 	@Override
 	public void setStyle(int x) {
 		_style = x;
 	}
 
 	@Override
 	public boolean resize(LocationVector start, LocationVector end) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
     public void paintComponent(Graphics g) {
 		super.paintComponent(g); 
 	     Graphics2D g2 = (Graphics2D) g; 
 
 	     g2.setColor(this._color);
 	     Rectangle2D.Float sign1 = new Rectangle2D.Float(5, 5, _pointEnd.get_x()-_pointStart.get_x(), _pointEnd.get_y()-_pointStart.get_y()); 
 	     
 	     if (_style == 0){	//dashed
 	    	 float f1[] = {12.0f};
 	    	 g2.setStroke(new BasicStroke((float)_weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 12.0f, f1, 0.0f));
 	     }
 	     else{
 	    	 g2.setStroke(new BasicStroke((float)_weight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 	     }
 	     
 	     g2.draw(sign1); 
     }
 
 	@Override
 	public MouseListener getMl() {
 		return _ml;
 	}
 
 	@Override
 	public void setMl(MouseListener m) {
 		_ml = m;
 	}
 
 	@Override
 	public void addML(int x) {
 		if (x == 1){	// Move
 			_ml = new MouseAdapter() {
 				private LocationVector nsl;
 				private LocationVector nel;
 				
 				@Override
 				public void mousePressed(MouseEvent e) {
 					//System.out.println("Pressed Rectangle: " + e.getX() + ", " + e.getY());
 					nsl = new LocationVector(e.getX(), e.getY());
 					
 				}
 				@SuppressWarnings("static-access")
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					//System.out.println("Released Rectangle: " + e.getX() + ", " + e.getY());
 					nel = new LocationVector(e.getX(), e.getY());
 					Invoker invoker = new Invoker();
 					Move cmd = new Move(getThis(), nsl, nel);
 					invoker.getInstance().storeAndExecute(cmd);
 					MyWindow x = new MyWindow();
 					x.getInstance().updateUI();
 				}
 			};
 		}
 		else if (x == 2){	// Resize
 			_ml = new MouseAdapter() {
 				private LocationVector nsl;
 				private LocationVector nel;
 				
 				@Override
 				public void mousePressed(MouseEvent e) {
 					//System.out.println("Pressed Rectangle: " + e.getX() + ", " + e.getY());
 					nsl = new LocationVector(e.getX(), e.getY());
 					
 				}
 				@SuppressWarnings("static-access")
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					//System.out.println("Released Rectangle: " + e.getX() + ", " + e.getY());
 					nel = new LocationVector(e.getX(), e.getY());
 					Invoker invoker = new Invoker();
 					Size cmd = new Size(getThis(), nsl, nel);
 					invoker.getInstance().storeAndExecute(cmd);
 					MyWindow x = new MyWindow();
 					x.getInstance().updateUI();
 				}
 			};
 		}
 		else if (x == 3){	// Selected
 			_ml = new MouseAdapter() {				
 				@SuppressWarnings("static-access")
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					selectThatShape();
 					MyWindow x = new MyWindow();
 					x.getInstance().updateUI();
 					x.getInstance().getWidth();
 				}
 			};
 		}
 		System.out.println("THE MOUSE LISTENER HAS BEEN SET FOR " + x);
 		this.addMouseListener(_ml);
 	}
 	
 	@Override
 	public void moveThatShape(LocationVector s, LocationVector e){
 		LocationVector newStarter	= new LocationVector(_pointStart.get_x() + e.get_x() - ((_pointEnd.get_x() - _pointStart.get_x()) / 2), _pointStart.get_y() + e.get_y() - ((_pointEnd.get_y() - _pointStart.get_y()) / 2));
 		LocationVector newEnder		= new LocationVector(_pointEnd.get_x() + e.get_x() -  ((_pointEnd.get_x() - _pointStart.get_x()) / 2), _pointEnd.get_y() + e.get_y() - ((_pointEnd.get_y() - _pointStart.get_y()) / 2)); 
 		if (newStarter.get_x() > 0 && newStarter.get_y() >0 && newStarter.get_x() < Global.MAXSIZE && newStarter.get_y() < Global.MAXSIZE &&
 				newEnder.get_x() > 0 && newEnder.get_y() >0 && newEnder.get_x() < Global.MAXSIZE && newEnder.get_y() < Global.MAXSIZE){
 			setPointStart(newStarter);
 			setPointEnd(newEnder);
 		}
 	}
 
 	@Override
 	public void resizeThatShape(LocationVector s, LocationVector e) {
 		if (e.get_x() > 0 && e.get_y() >0 && e.get_x() < Global.MAXSIZE && e.get_y() < Global.MAXSIZE){
 			setPointEnd(new LocationVector(_pointStart.get_x() +e.get_x(), _pointStart.get_y() + e.get_y()));
 		}
 	}
 	
 	@Override
 	public void selectThatShape(){
 		if (!_selected){
 			_selected = true;
 			this.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
 		}
 		else { //(_selected)
 			_selected = false;
 			setBorder(null);
 		}
 	}
 	
 	public Shape getThis(){
 		return this;
 	}
 
 	@Override
 	public void unselect() {
 		_selected = false;
 		
 	}
 
 	@Override
 	public boolean isSelected() {
 		return _selected;
 	}
 
 	@Override
 	public Shape getACopy() {
 		Rectangle r = new Rectangle(null, null, null, 0, 0);
 		r.setPointStart(getPointStart().getACopy());
 		r.setPointEnd(getPointEnd().getACopy());
 		r.setColor(getColor());
 		r.setWeight(getWeight());
 		r.setStyle(getStyle());
 		r.getPointStart().offsetTheCoordinate(10);
 		r.getPointEnd().offsetTheCoordinate(10);
 		return r;
 	}
 
 	@Override
 	public void saveCanvas(JFileChooser filename) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void openCanvas(JFileChooser filename) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
