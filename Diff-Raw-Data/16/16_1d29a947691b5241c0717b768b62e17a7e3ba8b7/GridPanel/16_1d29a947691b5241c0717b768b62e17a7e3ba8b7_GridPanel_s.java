 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Area;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.*;
 import network.TouchPacket;
 import network.TouchSocket;
 import playback.GridConfiguration;
 import playback.InstrumentHolder;
 import playback.Player;
 import playback.ToneGrid;
 
 /**
  *
  * @author Niels Visser
  */
 public class GridPanel extends JPanel {
 
 	private static final Color[][] playerColors = {
 		{ new Color(255, 0, 0), new Color(255, 191, 0),
 			new Color(127, 95, 0), new Color(63, 127, 0) },
 			{ new Color(0, 255, 255), new Color(0, 63, 255),
 				new Color(0, 31, 127), new Color(63, 0, 127) },
 				{ new Color(127, 255, 0), new Color(0, 255, 63),
 					new Color(0, 127, 31), new Color(0, 127, 127) },
 					{ new Color(127, 0, 255), new Color(255, 0, 191),
 						new Color(127, 0, 95), new Color(127, 0, 0) } };
 
 	private MouseHandler mouseHandler = new MouseHandler();
 	private HashMap<Integer, Pointer> pressedNotes;
 	private int squareHeight = 18;
 
 	private static double radOffset = 0.25d * Math.PI;
 
 	private Player player;
 
 	/*
 	 * Variables Relating to Menu's
 	 */
 	private int activeMenu[];
 	private int instrMenuIndex[];
 	public final static int NO_MENU = 0;
 	public final static int INSTRUMENT_MENU = 1;
 	public final static int MENU_MENU = 2;
 	
 	/*constructor
     Player p: Player whose panels will be drawn
 	 */
 
 	public GridPanel(Player p) {
 		this.pressedNotes = new HashMap<Integer, Pointer>();
 		this.player = p;
 		this.player.setGridPanel(this);
 		this.setOpaque(false);
 		this.addMouseListener(mouseHandler);
 		this.addMouseMotionListener(mouseHandler);
 
 		// touch table connect...
 		TouchHandler th = new TouchHandler();
 		TouchSocket ts = new TouchSocket();
 		ts.addObserver(th);
 		ts.startServer();
 		activeMenu = new int[p.getActiveGrids().size()];
 		for(int i=0;i<activeMenu.length;i++){
 			activeMenu[i]=0;
 		}
 		instrMenuIndex = new int[p.getActiveGrids().size()];
 		for(int i=0;i<instrMenuIndex.length;i++){
 			instrMenuIndex[i]=0;
 		}
 	}
 
 	//Method used to draw main component
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		//g2d.setColor(Color.blue);
 		g2d.setRenderingHint(
 				RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g2d.setStroke(new BasicStroke(1,
 				BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
 
 		drawInterfaceBackground(g2d);
 		drawGrids(g2d);
 
 		if(!pressedNotes.isEmpty()) {
 			Iterator<Map.Entry<Integer, Pointer>> it = pressedNotes.entrySet().iterator();
 			while (it.hasNext()) {
 				NoteIndex noteIndex = translatePointToNoteIndex(it.next().getValue().getLocation());
 				if(noteIndex != null) {
 					drawNote(g2d, noteIndex.getPerson(), noteIndex.getColumn(), noteIndex.getNote(), 1.125d, 1.125d);
 				}
 			}
 			// TODO Draw Instrument Menu if active here
 		}
 		for (int i = 0; i < player.getActiveGrids().size(); i++) {
 			if (activeMenu[i] == INSTRUMENT_MENU) {
 				drawInstrumentMenu(g2d, i);
 			}
 			if (activeMenu[i] == MENU_MENU) {
 				drawMenuMenu(g2d, i);
 			}
 		}
 	}
 
 	public int[] getActiveMenus(){
 		return activeMenu;
 	}
 	
 	public int getInstrMenuIndex(int person) {
 		return instrMenuIndex[person];
 	}
 	
 	private class MouseHandler extends MouseAdapter {
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			Point p = e.getPoint();
 			p.setLocation(p.getX(), translateY(p.getY()));
 
 			processPress(0, p);
 			repaint();
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			Point p = e.getPoint();
 			p.setLocation(p.getX(), translateY(p.getY()));
 			processDrag(0, p);
 			repaint();
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			processRelease(0);
 			repaint();
 		}
 
 	}
 
 	private class TouchHandler implements Observer {
 
 		private HashMap<Integer, Point> pressed;
 
 		public TouchHandler() {
 			this.pressed = new HashMap<Integer, Point>();
 		}
 
 		public void touchPressed(int id, Point p) {
 			p.setLocation(p.getX(), translateY(p.getY()));
 			processPress(id, p);
 			repaint();
 		}
 
 		public void touchDragged(int id, Point p) {
 			p.setLocation(p.getX(), translateY(p.getY()));
 			processDrag(id, p);
 			repaint();
 		}
 
 		public void touchReleased(int id) {
 			processRelease(id);
 			repaint();
 		}
 
 		@Override
 		public void update(Observable o, Object arg) {
 			TouchPacket packet = (TouchPacket)arg;
 			if(packet.id == 800) {
 				// discard, special packet
 				return;
 			}
 			// translate x and y...
 			int x = (int)(((double)packet.x / 32768d) * (double)getWidth());
 			int y = (int)(((double)packet.y / 32768d) * (double)getHeight());
 			Point p = new Point(x,y);
 
 			if(packet.touch == 1 && this.pressed.containsKey((int)packet.id)) {
 				// send drag
 				Point pOld = this.pressed.get((int)packet.id);
 				if(Math.abs(pOld.distance(p)) > 5) {
 					this.touchDragged((int)packet.id, p);
 					this.pressed.put((int)packet.id, p);
 				}
 			}
 			else if(packet.touch == 1 && !this.pressed.containsKey((int)packet.id)) {
 				this.pressed.put((int)packet.id, p);
 				// send pressed
 				this.touchPressed((int)packet.id, p);
 			}
 			else if(packet.touch == 0) {
 				this.pressed.remove(new Integer((int)packet.id));
 				// send release
 				this.touchReleased((int)packet.id);
 			}
 		}
 	}
 
 	private void drawInterfaceBackground(Graphics2D g){
 
 		int outerRadius = getRadius() + (int)(0.2*squareHeight);
 		int innerRadius = getRadius() - (int)(0.1*squareHeight + player.getHeight()*squareHeight);
 
 		Point[] outerUpperLeft, outerUpperRight, outerLowerRight, outerLowerLeft;
 		Point[] innerUpperLeft, innerUpperRight, innerLowerRight, innerLowerLeft;
 
 		outerUpperLeft  = generateBezierPoints(outerRadius, Math.PI, 0.5*Math.PI);
 		outerUpperRight = generateBezierPoints(outerRadius, 0.5*Math.PI, 0.0);
 		outerLowerRight = generateBezierPoints(outerRadius, 0.0, -0.5*Math.PI);
 		outerLowerLeft  = generateBezierPoints(outerRadius, 1.5*Math.PI, Math.PI);
 
 		innerUpperLeft  = generateBezierPoints(innerRadius, Math.PI, 0.5*Math.PI);
 		innerUpperRight = generateBezierPoints(innerRadius, 0.5*Math.PI, 0.0);
 		innerLowerRight = generateBezierPoints(innerRadius, 0.0, -0.5*Math.PI);
 		innerLowerLeft  = generateBezierPoints(innerRadius, 1.5*Math.PI, Math.PI);
 
 		GeneralPath outerCircle = new GeneralPath(), innerCircle = new GeneralPath();
 
 		outerCircle.moveTo(outerUpperLeft[0].getX() + getWidth()/2, 
 				translateY(outerUpperLeft[0].getY() + getHeight()/2));
 		outerCircle.curveTo(outerUpperLeft[1].getX() + getWidth()/2, 
 				translateY(outerUpperLeft[1].getY() + getHeight()/2), 
 				outerUpperLeft[2].getX() + getWidth()/2, 
 				translateY(outerUpperLeft[2].getY() + getHeight()/2),
 				outerUpperLeft[3].getX() + getWidth()/2,
 				translateY(outerUpperLeft[3].getY() + getHeight()/2));
 		outerCircle.curveTo(outerUpperRight[1].getX() + getWidth()/2, 
 				translateY(outerUpperRight[1].getY() + getHeight()/2), 
 				outerUpperRight[2].getX() + getWidth()/2, 
 				translateY(outerUpperRight[2].getY() + getHeight()/2),
 				outerUpperRight[3].getX() + getWidth()/2,
 				translateY(outerUpperRight[3].getY() + getHeight()/2));
 		outerCircle.curveTo(outerLowerRight[1].getX() + getWidth()/2, 
 				translateY(outerLowerRight[1].getY() + getHeight()/2), 
 				outerLowerRight[2].getX() + getWidth()/2, 
 				translateY(outerLowerRight[2].getY() + getHeight()/2),
 				outerLowerRight[3].getX() + getWidth()/2,
 				translateY(outerLowerRight[3].getY() + getHeight()/2));
 		outerCircle.curveTo(outerLowerLeft[1].getX() + getWidth()/2, 
 				translateY(outerLowerLeft[1].getY() + getHeight()/2), 
 				outerLowerLeft[2].getX() + getWidth()/2, 
 				translateY(outerLowerLeft[2].getY() + getHeight()/2),
 				outerLowerLeft[3].getX() + getWidth()/2,
 				translateY(outerLowerLeft[3].getY() + getHeight()/2));
 		outerCircle.closePath();
 
 		innerCircle.moveTo(innerUpperLeft[0].getX() + getWidth()/2, 
 				translateY(innerUpperLeft[0].getY() + getHeight()/2));
 		innerCircle.curveTo(innerUpperLeft[1].getX() + getWidth()/2, 
 				translateY(innerUpperLeft[1].getY() + getHeight()/2), 
 				innerUpperLeft[2].getX() + getWidth()/2, 
 				translateY(innerUpperLeft[2].getY() + getHeight()/2),
 				innerUpperLeft[3].getX() + getWidth()/2,
 				translateY(innerUpperLeft[3].getY() + getHeight()/2));
 		innerCircle.curveTo(innerUpperRight[1].getX() + getWidth()/2, 
 				translateY(innerUpperRight[1].getY() + getHeight()/2), 
 				innerUpperRight[2].getX() + getWidth()/2, 
 				translateY(innerUpperRight[2].getY() + getHeight()/2),
 				innerUpperRight[3].getX() + getWidth()/2,
 				translateY(innerUpperRight[3].getY() + getHeight()/2));
 		innerCircle.curveTo(innerLowerRight[1].getX() + getWidth()/2, 
 				translateY(innerLowerRight[1].getY() + getHeight()/2), 
 				innerLowerRight[2].getX() + getWidth()/2, 
 				translateY(innerLowerRight[2].getY() + getHeight()/2),
 				innerLowerRight[3].getX() + getWidth()/2,
 				translateY(innerLowerRight[3].getY() + getHeight()/2));
 		innerCircle.curveTo(innerLowerLeft[1].getX() + getWidth()/2, 
 				translateY(innerLowerLeft[1].getY() + getHeight()/2), 
 				innerLowerLeft[2].getX() + getWidth()/2, 
 				translateY(innerLowerLeft[2].getY() + getHeight()/2),
 				innerLowerLeft[3].getX() + getWidth()/2,
 				translateY(innerLowerLeft[3].getY() + getHeight()/2));
 		innerCircle.closePath();
 
 		Area donut = new Area(outerCircle);
 		donut.subtract(new Area(innerCircle));
 
 
 		g.setPaint(Color.black);
 
 		g.fill(donut);
 	}
 
 	/* Calls draw methods for each active grid
 	 */
 
 	private void drawGrids(Graphics2D g){
 		int people = player.getActiveGrids().size();
 		for(int i=0; i<people; i++){
 			drawPlayerGrid(g, i);
 		}
 	}
 
 	/* Calls draw methods for each column in a grid
 	 * int personIndex: person whose grid is to be drawn.
 	 */
 
 	private void drawPlayerGrid(Graphics2D g, int personIndex){
 		int columns = player.getWidth();
 		for(int i=0; i<columns; i++){
 			drawColumn(g, personIndex, i);
 		}
 		//Add Button Drawing
 		drawButton(g, personIndex, 2, player.getHeight()+1, 0.9, 0.9, 1);
 		drawButton(g, personIndex, 10, player.getHeight()+1, 0.9, 0.9, 2);
 	}
 
 	/* Calls draw methods for each note in a column
 	 */
 	private void drawColumn(Graphics2D g, int personIndex, int colIndex){
 		int notes = player.getHeight();
 		for(int i=0; i<notes; i++){
 			drawNote(g, personIndex, colIndex, i);
 		}
 	}
 
 	/* Draws the note with full width and height.
 	 */
 	private void drawNote(Graphics2D g, int personIndex, int colIndex, int toneIndex){
 		drawNote(g, personIndex, colIndex, toneIndex, 0.9, 0.9);
 	}
 
 	/* Draws the note.
 	 */
 	private void drawNote(Graphics2D g, int personIndex, int colIndex, int toneIndex, double xFactor, double yFactor){
 
 		double beginAngle = (double)(personIndex*player.getWidth())*radPerColumn() + 
 				(double)((double)(colIndex+1) - xFactor)*radPerColumn() + radOffset;
 		double endAngle = (double)(personIndex*player.getWidth())*radPerColumn() + 
 				(double)((double)colIndex + xFactor)*radPerColumn() + radOffset;
 
 		double lowerRadius = getRadius() - ((double)(toneIndex+1) - yFactor)*squareHeight;
 		double upperRadius = getRadius() - ((double)toneIndex + yFactor)*squareHeight;
 
 		GeneralPath gp = drawPath(beginAngle, endAngle, lowerRadius,
 				upperRadius);
 		gp.closePath();
 
 		Color squareColour = getColorFor(personIndex, colIndex, toneIndex);
 
 		for(int i = 0; i < player.getHeight()-toneIndex; i++)
 			squareColour = new Color(Math.max(squareColour.getRed()-8, 0),
 					Math.max(squareColour.getGreen()-8, 0),
 					Math.max(squareColour.getBlue()-8, 0));
 
 		boolean toneActive = player.getActiveGrids().get(personIndex).getTone(colIndex, toneIndex);
 		boolean tonePlayed = player.getPosition() == colIndex;
 
 		if(toneActive && tonePlayed) {
 			squareColour = new Color(squareColour.getRed()/2 + Color.white.getRed()/2,
 					squareColour.getGreen()/2 + Color.white.getGreen()/2,
 					squareColour.getBlue()/2 + Color.white.getBlue()/2);
 		}
 		else if(toneActive) {
 			squareColour = Color.white;
 		}
 		else if(tonePlayed) {
 			squareColour = Color.black;
 		}
 		if (activeMenu[personIndex]==INSTRUMENT_MENU||activeMenu[personIndex]==MENU_MENU){
 			squareColour=squareColour.darker().darker();
 		}
 		g.setPaint(squareColour);
 
 		g.fill(gp);
 	}
 
 	/* Draws the Button.
 	 */
 	private void drawButton(Graphics2D g, int personIndex, int colIndex, int toneIndex, double xFactor, double yFactor, int id){
 
 		double beginAngle = (double)(personIndex*player.getWidth())*radPerColumn() + 
 				(double)((double)(colIndex+1) - xFactor)*radPerColumn() + radOffset;
 		double endAngle = (double)(personIndex*player.getWidth())*radPerColumn() + 
 				(double)((double)(colIndex+3) + xFactor)*radPerColumn() + radOffset;
 
 		double lowerRadius = getRadius() - ((double)(toneIndex+1) - yFactor)*squareHeight;
 		double upperRadius = getRadius() - ((double)(toneIndex+2) + yFactor)*squareHeight;
 
 		GeneralPath gp = drawPath(beginAngle, endAngle, lowerRadius, upperRadius);
 		gp.closePath();
 
 		Color squareColour = getColorFor(personIndex, colIndex+4, toneIndex);
 		if ((activeMenu[personIndex]==INSTRUMENT_MENU)&&id==2||activeMenu[personIndex]==MENU_MENU&&id==1){
 			squareColour=squareColour.darker().darker();
 		}
 		g.setPaint(squareColour);
 
 		g.fill(gp);
 	}
 	
 	
 //	id 0	=>	instrumentbutton
 //	id 1	=>	settingsbutton
 //	id 2-5	=>	instrumentmenu instrumentbuttons
 //	id 6	=>	instrumentmenu nextbutton
 	public int[] getButtonCoordinates(int personIndex, int id){
 		int[] result = new int[4];
 		int toneIndex = player.getHeight();
 		double xFactor = 0.9;
 		double yFactor = 0.9;
 		int colIndex = 0;
 		int i = 3;
 		int j = 4;
 		switch(id) {
 		case 0:	colIndex = 2;	break;
 		case 1:	colIndex = 10;	break;
 		case 2: 
 		case 3:	
 		case 4:	
 		case 5:	i = 2;
 				j = 5;
 				colIndex = 2+(id-2)*3;
 				toneIndex = 2;
 				break;
 		case 6:	i = 1;
 				j = 1;
 				colIndex=14;
 				toneIndex = 1;
 				break;
 		}
 
 		double beginAngle = (double)((double)(-1-i)*.5 - xFactor)*radPerColumn() + radOffset;
 		double endAngle = (double)((double)(1+i)*.5 + xFactor)*radPerColumn() + radOffset;
 
 		double lowerRadius = getRadius() - ((double)(toneIndex+1) - yFactor)*squareHeight;
 		double upperRadius = getRadius() - ((double)(toneIndex+j) + yFactor)*squareHeight;
 		double midRadius = (lowerRadius+upperRadius)*.5;
 		
 		double width = Math.abs(midRadius*Math.cos(beginAngle) - midRadius*Math.cos(endAngle));
 		double height = Math.abs(upperRadius - lowerRadius);
 		
 		beginAngle = (double)(personIndex*player.getWidth())*radPerColumn() + 
 				(double)((double)(colIndex+1) - xFactor)*radPerColumn() + radOffset;
 		endAngle = (double)(personIndex*player.getWidth())*radPerColumn() + 
 				(double)((double)(colIndex+i) + xFactor)*radPerColumn() + radOffset;
 		double midAngle = (beginAngle+endAngle)*.5;
 		
 		double x = midRadius*Math.cos(midAngle) + getWidth()/2;
 		double y = translateY(midRadius*Math.sin(midAngle) + getHeight()/2);
 		
 		result[0] = (int)x;
 		result[1] = (int)y;
 		result[2] = (int)width;
 		result[3] = (int)height;
 		return result;
 	}
 
 	private GeneralPath drawPath(double beginAngle, double endAngle, double lowerRadius, double upperRadius) {
 		GeneralPath gp = new GeneralPath();
 		gp.moveTo(lowerRadius*Math.cos(beginAngle) + getWidth()/2,						//x1 (lower left)
 				translateY(lowerRadius*Math.sin(beginAngle) + getHeight()/2));	//y1
 				gp.lineTo(upperRadius*Math.cos(beginAngle) + getWidth()/2,						//x2 (upper left)
 						translateY(upperRadius*Math.sin(beginAngle) + getHeight()/2));	//y2
 
 				Point[] upperBP = generateBezierPoints(upperRadius, beginAngle, endAngle);		//upper curve
 				gp.curveTo(upperBP[1].getX() + getWidth()/2,
 						translateY(upperBP[1].getY() + getHeight()/2),
 						upperBP[2].getX() + getWidth()/2,
 						translateY(upperBP[2].getY() + getHeight()/2),
 						upperRadius*Math.cos(endAngle) + getWidth()/2,
 						translateY(upperRadius*Math.sin(endAngle) + getHeight()/2));
 
 				//    	gp.lineTo(upperRadius*Math.cos(endAngle) + getWidth()/2,						//x3
 				//    			translateY((int)(upperRadius*Math.sin(endAngle) + getHeight()/2)));		//y3
 
 				gp.lineTo(lowerRadius*Math.cos(endAngle) + getWidth()/2,						//x4
 						translateY(lowerRadius*Math.sin(endAngle) + getHeight()/2));		//y4
 
 				Point[] lowerBP = generateBezierPoints(lowerRadius, beginAngle, endAngle);
 				gp.curveTo(lowerBP[2].getX() + getWidth()/2,
 						translateY(lowerBP[2].getY() + getHeight()/2),
 						lowerBP[1].getX() + getWidth()/2,
 						translateY(lowerBP[1].getY() + getHeight()/2),
 						lowerRadius*Math.cos(beginAngle) + getWidth()/2,
 						translateY(lowerRadius*Math.sin(beginAngle) + getHeight()/2));
 
 				//    	gp.lineTo(lowerRadius*Math.cos(beginAngle) + getWidth()/2,						//x1
 				//    			translateY((int)(lowerRadius*Math.sin(beginAngle) + getHeight()/2)));	//y1
 
 				return gp;
 	}
 
 	private void drawInstrumentMenu(Graphics2D g, int personIndex) {
 		int colIndex = 2;
 		int toneIndex = 3;
 		double xFactor = 0.9;
 		double yFactor = 0.9;
 
 		Color squareColour = getColorFor(personIndex, 0, 1);
 		for (colIndex = 2; colIndex < 12; colIndex = colIndex + 3) {
 
 			double beginAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 					+ (double) ((double) (colIndex + 1) - xFactor) * radPerColumn() + radOffset;
 			double endAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 					+ (double) ((double) (colIndex + 2) + xFactor) * radPerColumn() + radOffset;
 
 			double lowerRadius = getRadius() - ((double) (toneIndex + 1) - yFactor) * squareHeight;
 			double upperRadius = getRadius() - ((double) (toneIndex + 3) + yFactor) * squareHeight;
 
 			GeneralPath gp = drawPath(beginAngle, endAngle, lowerRadius, upperRadius);
 			gp.closePath();
 
 			g.setPaint(squareColour);
 
 			g.fill(gp);
 
 
 		}
 
 		toneIndex = 1;
 		colIndex = 0;
 		double beginAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 				+ (double) ((double) (colIndex + 1) - xFactor) * radPerColumn() + radOffset;
 		double endAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 				+ (double) ((double) (colIndex + 1) + xFactor) * radPerColumn() + radOffset;
 
 		double lowerRadius = getRadius() - ((double) (toneIndex + 1) - yFactor) * squareHeight;
 		double upperRadius = getRadius() - ((double) (toneIndex + 1) + yFactor) * squareHeight;
 
 		GeneralPath gp = drawPath(beginAngle, endAngle, lowerRadius, upperRadius);
 		gp.closePath();
 
 		g.setPaint(squareColour);
 		g.fill(gp);
 
 		toneIndex = 1;
 		colIndex = 14;
 		beginAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 				+ (double) ((double) (colIndex + 1) - xFactor) * radPerColumn() + radOffset;
 		endAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 				+ (double) ((double) (colIndex + 1) + xFactor) * radPerColumn() + radOffset;
 
 		lowerRadius = getRadius() - ((double) (toneIndex + 1) - yFactor) * squareHeight;
 		upperRadius = getRadius() - ((double) (toneIndex + 1) + yFactor) * squareHeight;
 
 		gp = drawPath(beginAngle, endAngle, lowerRadius, upperRadius);
 		gp.closePath();
 
 		g.setPaint(squareColour);
 		g.fill(gp);
 
 
 	}
 
 	private void drawMenuMenu(Graphics2D g, int personIndex){
 		int colIndex = 4;
 		int toneIndex = 8;
 		double xFactor = 0.9;
 		double yFactor = 0.9;
 
 		Color squareColour = getColorFor(personIndex, 0, 1);
 
 		double beginAngle = (double) (personIndex * player.getWidth())
 				* radPerColumn() + (double) ((double) (colIndex + 1) - xFactor)
 				* radPerColumn() + radOffset;
 		double endAngle = (double) (personIndex * player.getWidth())
 				* radPerColumn() + (double) ((double) (colIndex + 7) + xFactor)
 				* radPerColumn() + radOffset;
 
 		double lowerRadius = getRadius() - ((double) (toneIndex + 1) - yFactor)	* squareHeight;
 		double upperRadius = getRadius() - ((double) (toneIndex + 1) + yFactor)	* squareHeight;
 
 		GeneralPath gp = drawPath(beginAngle, endAngle, lowerRadius, upperRadius);
 		gp.closePath();
 
 		g.setPaint(squareColour);
 		g.fill(gp);
 
 		//    	int colIndex = 3;
 		//    	int toneIndex = 0;
 		//		double xFactor = 0.9;
 		//		double yFactor = 0.9;
 		//		
 		//		double beginAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 		//				+ (double) ((double) (colIndex + 1) - xFactor) * radPerColumn() + radOffset;
 		//		double endAngle = (double) (personIndex * player.getWidth()) * radPerColumn()
 		//				+ (double) ((double) (colIndex + 7) + xFactor) * radPerColumn() + radOffset;
 		//
 		//		double lowerRadius = getRadius() - ((double) (toneIndex + 1) - yFactor) * squareHeight;
 		//		double upperRadius = getRadius() - ((double) (toneIndex + 8) + yFactor) * squareHeight;
 		//		
 		//		int x = (int) (upperRadius * Math.cos(beginAngle) + getWidth() / 2);
 		//		int y = (int) translateY(upperRadius * Math.sin(beginAngle) + getHeight() / 2);
 		//		int width = (int) (lowerRadius * Math.cos(endAngle) + getWidth() / 2) - x;
 		//		int height = (int) translateY(lowerRadius * Math.sin(endAngle) + getHeight() / 2) - y;
 		//		
 		//		g.setPaint(Color.WHITE);
 		//		g.fillRect(x, y, width, height);
 		//				
 		//		g.setPaint(Color.BLACK);
 		//		g.drawRect(x, y, width, height);
 		//		
 		//		Font font = new Font("Times New Roman", Font.PLAIN, 20);
 		//		g.setFont(font);
 		//
 		//		upperRadius = getRadius() - ((double) (toneIndex + 7) + yFactor) * squareHeight;
 		//		y = (int) translateY(upperRadius * Math.sin(beginAngle) + getHeight() / 2);
 		//		
 		//		g.drawString(" Clear", x, y);
 
 	}
 
 	private Point[] generateBezierPoints(double radius, double beginAngle, double endAngle) {
 
 		//    	beginAngle = ((beginAngle + Math.PI) % (2d*Math.PI)) - Math.PI;
 		//    	endAngle = ((endAngle + Math.PI) % (2d*Math.PI)) - Math.PI;
 
 		double sweepAngle = Math.abs(endAngle - beginAngle);
 
 		double minAngle = Math.min(beginAngle, endAngle);
 		double maxAngle = Math.max(beginAngle, endAngle);
 
 		double[] p0 = {Math.cos(sweepAngle/2d),
 				Math.sin(sweepAngle/-2d)};
 		double[] p1 = {(4d-p0[0])/3d,
 				((1d-p0[0])*(3d-p0[0]))/(3d*p0[1])};
 		double[] p2 = {p1[0],
 				-1d*p1[1]};
 		double[] p3 = {p0[0],
 				-1d*p0[1]};
 
 		double[][] p = {p0, p1, p2, p3};
 		Point[] result = new Point[4];
 
 		for(int i = 0; i < p.length; i++) {
 			p[i] = rotate(radius*p[i][0], radius*p[i][1], minAngle+sweepAngle/2d);
 			result[i] = new Point();
 			result[i].setLocation(p[i][0], p[i][1]);
 		}
 
 		result[0].setLocation(radius*Math.cos(minAngle), radius*Math.sin(minAngle));
 		result[3].setLocation(radius*Math.cos(maxAngle), radius*Math.sin(maxAngle));
 
 		if(beginAngle > endAngle) {
 			Point temp = result[0];
 			result[0] = result[3];
 			result[3] = temp;
 
 			temp = result[1];
 			result[1] = result[2];
 			result[2] = temp;
 		}
 
 		return result;
 	}
 
 	private double radPerColumn() {
 		return (Math.PI * 2d) / ((double)(player.getWidth() * player.getActiveGrids().size()));
 	}
 
 	private double[] rotate(double x, double y, double rotationAngle) {
 		return rotate(x, y, 0d, 0d, rotationAngle);
 	}
 
 	private double[] rotate(double x, double y, double centerX, double centerY, double rotationAngle) {
 		double diffX = x-centerX;
 		double diffY = y-centerY;
 
 		double radius = Math.sqrt(diffX*diffX+diffY*diffY);
 		double angle = Math.atan2(diffY, diffX);
 
 		angle += rotationAngle;
 
 		x = radius*Math.cos(angle);
 		y = radius*Math.sin(angle);
 		x += centerX;
 		y += centerY;
 
 		return new double[]{x, y};
 	}
 
 	/* 
 	 * @returns the colorarray associated with a person.
 	 */
 
 	//  private Color[] getColorsFor(int person) {
 	//  return playerColors[person%4];
 	//}
 
 	private Color getColorFor(int personIndex, int colIndex, int toneIndex) {
 		Color result;
 
 		int alternate = ((colIndex/4 + toneIndex) % 2) * 2;				//	Visually separating quarter time
 
 		Color begin = playerColors[personIndex%4][alternate], end = playerColors[personIndex%4][alternate+1];
 
 		int playerWidth = player.getWidth();
 
 		result = new Color((begin.getRed()*(playerWidth - colIndex) + end.getRed()*colIndex) / playerWidth,
 				(begin.getGreen()*(playerWidth - colIndex) + end.getGreen()*colIndex) / playerWidth,
 				(begin.getBlue()*(playerWidth - colIndex) + end.getBlue()*colIndex) / playerWidth);
 
 		return result;
 	}
 
 	private int getRadius(){
 		return Math.min(getWidth()/2, getHeight()/2);
 	}
 
 	private NoteIndex translatePointToNoteIndex(Point point) {
 		// centreer de punten
 		int rx = point.x - this.getWidth()/2;
 		int ry = point.y - this.getHeight()/2;
 
 		int rr = (int)Math.sqrt(rx*rx+ry*ry);
 		double radr = Math.atan(((double)ry)/((double)rx));
 		if(rx < 0 && ry >= 0) {
 			radr += Math.PI;
 		}
 		else if(rx < 0 && ry < 0) {
 			radr += Math.PI;
 		}
 		else if(rx > 0 && ry < 0) {
 			radr += Math.PI * 2d;
 		}
 		radr -= radOffset;
 		// nu weten we de hoek (radr) en de straal (rr)
 		double sizePerPerson = (Math.PI * 2d) / (double)(player.getActiveGrids().size());
 		int personIndex = (int)Math.floor(radr / sizePerPerson);
 		int num = player.getActiveGrids().size();
 		personIndex = (personIndex % num + num) % num;
 		int colIndex = (int)Math.floor((radr - (double)personIndex * sizePerPerson) / radPerColumn());
 		int w = player.getWidth();
 		colIndex = (colIndex % w + w) % w;
 		int toneIndex = player.getHeight() - (int)Math.floor(((double)(rr - (getRadius() - squareHeight * player.getHeight())) / (double)squareHeight)) - 1;
 
 		if(toneIndex >= 0 && toneIndex < player.getHeight())
 			return new NoteIndex(personIndex, colIndex, toneIndex);
 		return null;
 	}
 
 	private NoteIndex translatePointToIndex(Point point) {
 		// centreer de punten
 		int rx = point.x - this.getWidth()/2;
 		int ry = point.y - this.getHeight()/2;
 
 		int rr = (int)Math.sqrt(rx*rx+ry*ry);
 		double radr = Math.atan(((double)ry)/((double)rx));
 		if(rx < 0 && ry >= 0) {
 			radr += Math.PI;
 		}
 		else if(rx < 0 && ry < 0) {
 			radr += Math.PI;
 		}
 		else if(rx > 0 && ry < 0) {
 			radr += Math.PI * 2d;
 		}
 		radr -= radOffset;
 		// nu weten we de hoek (radr) en de straal (rr)
 		double sizePerPerson = (Math.PI * 2d) / (double)(player.getActiveGrids().size());
 		int personIndex = (int)Math.floor(radr / sizePerPerson);
 		int num = player.getActiveGrids().size();
 		personIndex = (personIndex % num + num) % num;
 		int colIndex = (int)Math.floor((radr - (double)personIndex * sizePerPerson) / radPerColumn());
 		int w = player.getWidth();
 		colIndex = (colIndex % w + w) % w;
 		int toneIndex = player.getHeight() - (int)Math.floor(((double)(rr - (getRadius() - squareHeight * player.getHeight())) / (double)squareHeight)) - 1;
 		
 		return new NoteIndex(personIndex, colIndex, toneIndex);
 
 	}
 
 	// Process press events
 	private void processPress(int id, Point point) {
 
 		boolean drawing;
 
 		NoteIndex pressedNote = translatePointToIndex(point);
 		int[] colrow = { pressedNote.getColumn(), pressedNote.getNote(),
 				pressedNote.getPerson() };
 
 		if (colrow[1] >= 0 && colrow[1] < player.getHeight()
 				&& activeMenu[colrow[2]] == NO_MENU) {
 			ToneGrid tg = this.player.getActiveGrids().get(colrow[2]);
 			tg.toggleTone(colrow[0], colrow[1]);
 			drawing = tg.getTone(colrow[0], colrow[1]);
 		} else {
 			drawing = true;
 			if (colrow[1] >= player.getHeight() + 1
 					&& colrow[1] < player.getHeight() + 4) {
 				// Menu's may have been pressed
 				// col 3-6 11-14
 				if (colrow[0] >= 2 && colrow[0] <= 5) {
 					if (activeMenu[colrow[2]] == INSTRUMENT_MENU) {
 						activeMenu[colrow[2]] = NO_MENU;
 					}
 					else
 						activeMenu[colrow[2]] = INSTRUMENT_MENU;
 				} else if (pressedNote.getColumn() >= 10 && colrow[0] <= 13) {
 					if (activeMenu[colrow[2]] == MENU_MENU)
 						activeMenu[colrow[2]] = NO_MENU;
 					else
 						activeMenu[colrow[2]] = MENU_MENU;
 				}
 			}
 			List<GridConfiguration> configs = InstrumentHolder.getInstance().getAvailableConfigurations();
 			if (colrow[1] >= 3 && colrow[1] <= 6
 					&& activeMenu[colrow[2]] == INSTRUMENT_MENU) {
 				// Instrument Button has Been Pressed in INSTRUMENT_MENU
 				if (colrow[0] >= 2 && colrow[0] <= 4) {
 					player.changeInstrument(player.getActiveGrids().get(colrow[2]), configs.get(instrMenuIndex[colrow[2]]));
 					activeMenu[colrow[2]] = NO_MENU;
 				} else if (colrow[0] >= 5 && colrow[0] <= 7) {
 					player.changeInstrument(player.getActiveGrids().get(colrow[2]), configs.get((instrMenuIndex[colrow[2]]+1)%configs.size()));
 					activeMenu[colrow[2]] = NO_MENU;
 				} else if (colrow[0] >= 8 && colrow[0] <= 10) {
 					player.changeInstrument(player.getActiveGrids().get(colrow[2]), configs.get((instrMenuIndex[colrow[2]]+2)%configs.size()));
 					activeMenu[colrow[2]] = NO_MENU;
 				} else if (colrow[0] >= 11 && colrow[0] <= 13) {
 					player.changeInstrument(player.getActiveGrids().get(colrow[2]), configs.get((instrMenuIndex[colrow[2]]+3)%configs.size()));
 					activeMenu[colrow[2]] = NO_MENU;
 				}
 			} else if (colrow[1] >= 1 && colrow[1] <= 2 && colrow[0] >= 14 && colrow[0] <= 15
 					&& activeMenu[colrow[2]] == INSTRUMENT_MENU) instrMenuIndex[colrow[2]] = (instrMenuIndex[colrow[2]]+1)%configs.size();
 			else if (colrow[1] >= 1 && colrow[1] <= 2 && colrow[0] >= 0 && colrow[0] <= 2 
					&& activeMenu[colrow[2]] == INSTRUMENT_MENU) instrMenuIndex[colrow[2]] = (instrMenuIndex[colrow[2]]+3)%configs.size();
 
 			else if (colrow[0] >= 4 && colrow[0] <= 12 && activeMenu[colrow[2]] == MENU_MENU) {
 				if (colrow[1] >= 8 && colrow[1] <= 10) {
 					ToneGrid tg = this.player.getActiveGrids().get(colrow[2]);
 					tg.clear();
 					activeMenu[colrow[2]] = NO_MENU;
 				}
 			}
 		}
 		pressedNotes.put(id, new Pointer(point, drawing));
 	}
 
 	//	Process drag events
 	private void processDrag(int id, Point point) {
 
 		boolean drawing = pressedNotes.get(id).isDrawing();
 
 		pressedNotes.put(id, new Pointer(point, drawing));
 
 		NoteIndex pressedNote = translatePointToNoteIndex(point);
 
 
 		if(pressedNote != null&&activeMenu[pressedNote.getPerson()]==NO_MENU) {
 			ToneGrid tg = this.player.getActiveGrids().get(pressedNote.getPerson());
 			if(drawing)
 				tg.activateTone(pressedNote.getColumn(), pressedNote.getNote());
 			else
 				tg.deactivateTone(pressedNote.getColumn(), pressedNote.getNote());
 		}
 	}
 
 	private void processRelease(int id) {
 		pressedNotes.remove(id);
 	}
 
 	private double translateY(double y) {
 		return getHeight() - y;
 	}
 
 	public void display() {
 		JFrame f = new JFrame("LinePanel");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.add(this);
 		f.pack();
 		f.setLocationRelativeTo(null);
 		f.setVisible(true);
 	}
 
 
 	private class Pointer {
 		private Point point;
 		private boolean drawing;
 
 		public Pointer(Point point, boolean drawing) {
 			this.point = point;
 			this.drawing = drawing;
 		}
 
 		public Point getLocation() {
 			return this.point.getLocation();
 		}
 
 		public boolean isDrawing() {
 			return drawing;
 		}
 	}
 }
