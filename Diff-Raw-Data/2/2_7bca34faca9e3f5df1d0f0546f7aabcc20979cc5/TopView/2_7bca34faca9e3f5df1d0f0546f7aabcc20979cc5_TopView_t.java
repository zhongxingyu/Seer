 package View;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Toolkit;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.Obstacle;
 import Model.Runway;
 import net.miginfocom.swing.MigLayout;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.MouseWheelEvent;
 
 
 @SuppressWarnings("serial")
 public class TopView extends JPanel implements AirportObserver, ViewPanel{
 
 	boolean visible = false;
 
 	//this value determines the amount of pixels the tag is from the end of the runway
 	int tagBorder = 10;
 
 	//this value determines how much of the width of the panel the runway takes up.
 	double ratio = 0.95;
 
 	//this value determines how much of the width of the runway the runwayTag takes up.
 	final double fontRatio = 0.75;
 
 	int xOffset = 0;
 	int yOffset = 0;
 
 	Color toraColor = new Color(184, 134, 11);
 	Color dtColor = new Color(184, 11, 134);
 	Color todaColor = new Color(255, 0, 0);
 	Color asdaColor = new Color(0, 0, 255);
 	Color ldaColor = new Color(255, 0, 255);
 	Color resaColor = new Color(84, 84, 84);
 
 	double meterToPixel;
 	int oppositeDT;
 	int TORA;
 	int TODA;
 	int ASDA;
 	int LDA;
 	int DT;
 	int RESA;
 	int stopway;
 	int stopwayStart;
 	int RESAStart;
 	int DTStart;
 	int TORAStart;
 	int TODAStart;
 	int ASDAStart;
 	int LDAStart;
 	int ANGLE;
 	int runwayWidth;
 	int runwayHeight;
 	int obstacleLength;
 	int obstacleWidth;
 	boolean obstacleLeft;
 	String leftTag;
 	String rightTag;
 	Airport airport;
 	Runway runway;
 	Obstacle obstacle;
 	String threshold;
 	int spaceForScale;
 	int leftDT;
 	int rightDT;
 
 	//relative to panel
 	int xRunway;
 	int yRunway;
 
 	//relative to runway
 	int xObstacle;
 	int yObstacle;
 
 	int mousex;
 	int mousey;
 	
 	int runwayStripWidthFromCentreLine=150;
 	int runwayToEdgeOfCnGArea = 60;
 	int CnGAreaWidthFromCentreLine = 75;
 	int CnGWiderAreaWidthFromCentreLine=105;
 	int widerAreaPointOne = 150;
 	int widerAreaPointTwo = 300;
 	
 
 	public TopView(Airport airport){
 		super();
 
 		this.setBackground(new Color(154, 205, 50));
 		setLayout(new MigLayout("", "[grow]", "[grow][]"));
 		
 		updateAirport(airport);
 		setValues();
 
 		setDragCursor(this);
 		createSlider();
 		createDraggingListeners();
 		setVisible(true);
 
 	}
 
 	public void paint (Graphics g){
 		super.paint(g);
 		if(visible){
 		
 			setValues();
 			Graphics2D g2d = (Graphics2D)g;
 			
 			g.translate(xOffset, yOffset);
 			runwayCreation(g2d);
 			obstacleCreation(g2d);
 			declaredRunwaysCreation(g2d);
 			drawDirection(g2d);
 			
 			g.translate(-xOffset, -yOffset);
 			paintComponents(g);
 			drawKey(g2d);
 			drawScale(g2d);
 		}
 	}
 
 	public void drawDirection(Graphics2D g2d){
 		if(airport.getCurrentRunway()!=null){
 		g2d.setColor(Color.BLACK);
 		g2d.drawString("Runway Take-off/Landing Direction", xRunway+meterToPixel(3*runwayWidth/8),  yRunway+-10-meterToPixel(runwayStripWidthFromCentreLine*2));
 		
 		g2d.setStroke(new BasicStroke(3));
 		g2d.drawLine(xRunway+meterToPixel(runwayWidth/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(3*runwayWidth/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2));
 
 		if(airport.getCurrentRunway().getName().equals(leftTag)){
 			g2d.drawLine(xRunway+meterToPixel(3*runwayWidth/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(11*runwayWidth/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)+meterToPixel(runwayStripWidthFromCentreLine/4));
 			g2d.drawLine(xRunway+meterToPixel(3*runwayWidth/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(11*runwayWidth/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)-meterToPixel(runwayStripWidthFromCentreLine/4));
 		}else{
 			g2d.drawLine(xRunway+meterToPixel(runwayWidth/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(5*runwayWidth/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)+meterToPixel(runwayStripWidthFromCentreLine/4));
 			g2d.drawLine(xRunway+meterToPixel(runwayWidth/4),yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(5*runwayWidth/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)-meterToPixel(runwayStripWidthFromCentreLine/4));
 		}
 		}
 	}
 		
 	public void setVisible(boolean b){
 		visible=b;
 	}
 
 	public void createSlider(){
 		final JSlider zoomSlider = new JSlider();
 		zoomSlider.setBackground(null);
 		zoomSlider.setValue(950);
 		zoomSlider.setMinimum(900);
 		zoomSlider.setMaximum(6000);
 		zoomSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				JSlider slider = ((JSlider)e.getSource());
 				setZoom((double)slider.getValue() / (double)1000);
 				repaint();
 				updateUI();
 			}
 		});
 		add(zoomSlider, "cell 0 1,alignx right,aligny bottom");
 		zoomSlider.setCursor(Cursor.getDefaultCursor());
 		
 		addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(MouseWheelEvent arg0) {
 				zoomSlider.setValue(zoomSlider.getValue() - (arg0.getWheelRotation() * 100));
 			}
 		});
 		
 		zoomSlider.setPaintLabels(false);
 		
 	}
 
 	public void createDraggingListeners(){
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				mousex = arg0.getX();
 				mousey = arg0.getY();
 			}
 		});
 		addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 
 				int deltaMouseX = arg0.getX() - mousex;
 				int deltaMouseY = arg0.getY() - mousey;
 
 				mousex = arg0.getX();
 				mousey = arg0.getY();
 
 				setOffset(xOffset + deltaMouseX, yOffset + deltaMouseY);
 				repaint();
 				updateUI();
 			}
 		});
 	}
 
 	public int meterToPixel(int x){
 		return (int) (meterToPixel*x);
 	}
 	
 	public int pixelToMeter(int x){
 		return (int) (x/meterToPixel);
 	}
 	
 	public void runwayCreation(Graphics2D g2d){
 		if(airport!=null && runway!=null){
 
 			int pWidth = this.getWidth();
 			int pHeight = this.getHeight();
 
 			int width = (int) (ratio*pWidth);
 			meterToPixel=width/(double)runwayWidth;
 
 			int height = meterToPixel(runwayHeight);
 
 			//calculates the x and y values to position the runway on the view
 			xRunway = (int) (((1.0-ratio)/2) * pWidth);
 			yRunway = (pHeight - height)/2;
 
 			
 			drawRunwayArea(g2d);
 
 			g2d.setColor(Color.GRAY);
 			g2d.fillRect(xRunway, yRunway, width, height);
 
 			drawRunwayMarkings(g2d);
 			
 			
 			g2d.setColor(Color.WHITE);
 			for(int i=0; i<1000; i++){
 				Font f = new Font("tag", 1, 1000-i);
 				g2d.setFont(f);
 				if(g2d.getFontMetrics().stringWidth(leftTag)<=(fontRatio*height))break;
 			}			
 			g2d.rotate(0.5 * Math.PI,  xRunway+tagBorder+ meterToPixel(leftDT), yRunway+(height/8));
 			g2d.drawString(leftTag, xRunway+ tagBorder+ meterToPixel(leftDT), yRunway+(height/8));
 			g2d.rotate(-0.5 * Math.PI,  xRunway + tagBorder+ meterToPixel(leftDT), yRunway+(height/8));
 			
 			g2d.rotate(-0.5 * Math.PI,  xRunway + width - tagBorder -meterToPixel(rightDT), yRunway+(7*(height/8)));
 			g2d.drawString(rightTag, xRunway+ width - tagBorder-meterToPixel(rightDT), yRunway+(7*(height/8)));
 			g2d.rotate(0.5 * Math.PI,  xRunway + width - tagBorder-meterToPixel(rightDT), yRunway+(7*(height/8)));
 			
 			
 
 		}
 
 	}
 
 	public void drawRunwayArea(Graphics2D g2d){
 
 		int nPoints = 12;
 		int xPoints[] = new int[nPoints];
 		int yPoints[] = new int[nPoints];
 		
 		xPoints[0]= xRunway-meterToPixel(runwayToEdgeOfCnGArea);
 		yPoints[0]= yRunway+meterToPixel((runwayHeight/2) -CnGAreaWidthFromCentreLine);
 		xPoints[1]= xPoints[0];
 		yPoints[1]= yRunway+meterToPixel((runwayHeight/2) +CnGAreaWidthFromCentreLine);
 		xPoints[2]= xPoints[1]+meterToPixel(runwayToEdgeOfCnGArea+CnGAreaWidthFromCentreLine);
 		yPoints[2]= yPoints[1];
 		xPoints[3]= xRunway+meterToPixel(widerAreaPointTwo);
 		yPoints[3]= yRunway+meterToPixel((runwayHeight/2) +CnGWiderAreaWidthFromCentreLine);
 		xPoints[4]= xRunway+meterToPixel(runwayWidth-widerAreaPointTwo);
 		yPoints[4]= yPoints[3];
 		xPoints[5]= xRunway+meterToPixel(runwayWidth-widerAreaPointOne);
 		yPoints[5]= yPoints[1];
 		xPoints[6]= xRunway+meterToPixel(runwayWidth+runwayToEdgeOfCnGArea);
 		yPoints[6]= yPoints[1];
 		xPoints[7]=xPoints[6];
 		yPoints[7]= yPoints[0];
 		xPoints[8]=xPoints[5];
 		yPoints[8]= yPoints[0];
 		xPoints[9]=xPoints[4];
 		yPoints[9]= yRunway-meterToPixel(CnGWiderAreaWidthFromCentreLine-(runwayHeight/2));
 		xPoints[10]=xPoints[3];
 		yPoints[10]= yPoints[9];
 		xPoints[11]=xPoints[2];
 		yPoints[11]= yPoints[0];
 		g2d.setColor(new Color(34, 139, 34));
 		g2d.fillRect(xPoints[0], yRunway - meterToPixel(runwayStripWidthFromCentreLine-(runwayHeight/2)), meterToPixel(runwayWidth+runwayToEdgeOfCnGArea*2), meterToPixel(runwayStripWidthFromCentreLine*2));
 
 		g2d.setColor(new Color(0, 100, 0));
 
 		g2d.fillPolygon(xPoints, yPoints, nPoints);
 	}
 	
 	public void drawRunwayMarkings(Graphics2D g2d){
 		int ratioOfThresholdDashesWidthToRunwayHeight = 10;
 		int ratioOfDashesToThresholdDashes = 2;
 		g2d.setColor(Color.WHITE);
 		int dashesY = (int) (yRunway+meterToPixel(runwayHeight/2));
 		int dashesLength = 35;
 		int dashesWidth=(runwayHeight/ratioOfThresholdDashesWidthToRunwayHeight);
 		int gaps = 25;
 		int ratioOfDashesToThresholdMarker = 4;
 		for(int i = dashesLength*ratioOfDashesToThresholdMarker+leftDT; i<runwayWidth-(dashesLength*ratioOfDashesToThresholdMarker)-rightDT; i=i+gaps+dashesLength){
 			g2d.fillRect(xRunway+meterToPixel(i), dashesY, meterToPixel(dashesLength), meterToPixel(dashesWidth));
 		}
 		
 		int ratioOfDashesToTagSize = 3;
 		
 		tagBorder=(int) meterToPixel(dashesLength*ratioOfDashesToTagSize);
 			
 	
 		ratioOfThresholdDashesWidthToRunwayHeight = 12;
 		dashesWidth=(runwayHeight/ratioOfThresholdDashesWidthToRunwayHeight);
 		if (dashesWidth == 0) dashesWidth = 3;
 		gaps=dashesWidth;
 		int dashesX = xRunway + meterToPixel((dashesLength/2)+leftDT);
 		dashesLength=dashesLength*ratioOfDashesToThresholdDashes;
 
 		for(int i = gaps; i<runwayHeight-(gaps*2); i=i+gaps+dashesWidth){
 			g2d.fillRect(dashesX, meterToPixel(i)+yRunway, meterToPixel(dashesLength), meterToPixel(dashesWidth));
 		}
 
 		dashesX = xRunway + meterToPixel(runwayWidth)  - meterToPixel(dashesLength/ratioOfDashesToThresholdMarker) - meterToPixel(dashesLength) - meterToPixel(rightDT);
 		for(int i = gaps; i<runwayHeight-(gaps*2); i=i+gaps+dashesWidth){
 			g2d.fillRect(dashesX, meterToPixel(i)+yRunway, meterToPixel(dashesLength), meterToPixel(dashesWidth));
 		}
 		
 		displaceThresholdMarkings(g2d);
 	}
 	
 	public void displaceThresholdMarkings(Graphics g2d){
 		int dashesLength = 35;
 		int gaps=25;
 		int dashesWidth = 2;
 		if(leftDT>0){
 			
 			for(int i = 0; i<leftDT-dashesLength; i=i+dashesLength+gaps){
 				g2d.drawLine(xRunway+meterToPixel(i), yRunway+meterToPixel(runwayHeight/2), xRunway+meterToPixel(i+dashesLength), yRunway+meterToPixel(runwayHeight/2));
 				g2d.drawLine(xRunway+meterToPixel(i+dashesLength), yRunway+meterToPixel(runwayHeight/2), xRunway+meterToPixel(i+(7*dashesLength/8)), yRunway+meterToPixel(dashesWidth*2+runwayHeight/2));
 				g2d.drawLine(xRunway+meterToPixel(i+dashesLength), yRunway+meterToPixel(runwayHeight/2), xRunway+meterToPixel(i+(7*dashesLength/8)), yRunway+meterToPixel(-(dashesWidth*2)+runwayHeight/2));
 				
 			}
 			g2d.setColor(Color.YELLOW);
 			g2d.drawLine(xRunway+meterToPixel(leftDT), yRunway, xRunway+meterToPixel(leftDT), yRunway+meterToPixel(runwayHeight));
 		}
 		g2d.setColor(Color.WHITE);
 		if(rightDT>0){
 			for(int i = runwayWidth-rightDT+dashesLength; i<runwayWidth; i=i+dashesLength+gaps){
 				g2d.drawLine(xRunway+meterToPixel(i), yRunway+meterToPixel(runwayHeight/2), xRunway+meterToPixel(i)+meterToPixel(dashesLength), yRunway+meterToPixel(runwayHeight/2));
 				g2d.drawLine(xRunway+meterToPixel(i), yRunway+meterToPixel(runwayHeight/2), xRunway+meterToPixel(i)+meterToPixel(dashesLength/8), yRunway+meterToPixel(dashesWidth*2+runwayHeight/2));
 				g2d.drawLine(xRunway+meterToPixel(i), yRunway+meterToPixel(runwayHeight/2), xRunway+meterToPixel(i)+meterToPixel(dashesLength/8), yRunway+meterToPixel(-(dashesWidth*2)+runwayHeight/2));
 				
 			}
 			g2d.setColor(Color.YELLOW);
 			g2d.drawLine(xRunway+meterToPixel(runwayWidth-rightDT), yRunway, xRunway+meterToPixel(runwayWidth-rightDT), yRunway+meterToPixel(runwayHeight));
 		}
 	}
 	
 	public void obstacleCreation(Graphics2D g2d){
 		if(obstacle!=null){
 			g2d.setColor(Color.WHITE);
 			g2d.fillRect(meterToPixel(xObstacle-rightDT)+xRunway, meterToPixel(yObstacle+(obstacleWidth/2))+yRunway, meterToPixel(obstacleLength), meterToPixel(obstacleWidth));
 			g2d.setColor(Color.BLACK);
 			g2d.drawRect(meterToPixel(xObstacle-rightDT)+xRunway, meterToPixel(yObstacle+(obstacleWidth/2))+yRunway, meterToPixel(obstacleLength), meterToPixel(obstacleWidth));
 			g2d.drawLine(meterToPixel(xObstacle-rightDT)+xRunway, meterToPixel(yObstacle+(obstacleWidth/2))+yRunway, meterToPixel(xObstacle-rightDT)+xRunway+meterToPixel(obstacleLength), meterToPixel(yObstacle+(obstacleWidth/2))+yRunway+meterToPixel(obstacleWidth));
			g2d.drawLine(meterToPixel(xObstacle-rightDT)+xRunway+meterToPixel(obstacleLength), meterToPixel(yObstacle+(obstacleWidth/2))+yRunway, meterToPixel(xObstacle-rightDT)+xRunway, meterToPixel(yObstacle+(obstacleWidth/2))+yRunway+meterToPixel(obstacleWidth));
 			
 			
 		}
 	}
 	
 	public void drawKey(Graphics2D g2d){
 		g2d.setFont(new Font("key", 1, 15));
 		int textDistance = g2d.getFontMetrics().getHeight();
 		spaceForScale = 30;
 		int spaceFromLeftEdge = 10;
 		g2d.setColor(toraColor);
 		g2d.drawString("TORA", spaceFromLeftEdge, this.getHeight()- spaceForScale-textDistance);
 		g2d.setColor(todaColor);
 		g2d.drawString("TODA", spaceFromLeftEdge, this.getHeight()- spaceForScale-(2*textDistance) );
 		g2d.setColor(asdaColor);
 		g2d.drawString("ASDA",spaceFromLeftEdge, this.getHeight()- spaceForScale- (3*textDistance));
 		g2d.setColor(ldaColor);
 		g2d.drawString("LDA", spaceFromLeftEdge, this.getHeight()- spaceForScale);
 		
 		
 		g2d.setColor(dtColor);
 		g2d.drawString("DT", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale);
 		g2d.setColor(Color.WHITE);
 		g2d.drawString("STOPWAY", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale-textDistance);
 		g2d.setColor(resaColor);
 		g2d.drawString("RESA", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale-(2*textDistance));
 		g2d.setColor(Color.BLACK);
 		g2d.drawString("ANGLE", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale-(3*textDistance));
 //		int stringWidth = g2d.getFontMetrics().stringWidth("TODA")+ g2d.getFontMetrics().stringWidth("STOPWAY");
 //		g2d.drawRect(5, this.getHeight()+ spaceForScale+ (3*textDistance),  (spaceFromLeftEdge*2) + stringWidth, this.getHeight()-spaceForScale);
 	}
 	
 	public void drawScale(Graphics2D g2d){
 		if(airport!=null && runway!=null){
 			int scaleWidth = Math.round(pixelToMeter(this.getWidth()/3)/100)*100;
 			g2d.setColor(Color.BLACK);
 			g2d.setFont(new Font("scale", 1, 10));
 			g2d.fillRect(10, (int) this.getHeight()-10, (int) (scaleWidth*meterToPixel), 2);	
 			g2d.fillRect(10, (int) this.getHeight()-15, (int) 2, 5);
 			g2d.fillRect((int) (8+(scaleWidth*meterToPixel)), (int) this.getHeight()-15, (int) 2, 5);
 			g2d.fillRect((int) (8+(scaleWidth/2*meterToPixel)), (int) this.getHeight()-12, (int) 2, 2);
 			g2d.drawString(Integer.toString(scaleWidth)+"m", (int) ((scaleWidth*meterToPixel)), (int) this.getHeight()-15);
 			g2d.drawString(Integer.toString(scaleWidth/2)+"m", (int) ((scaleWidth/2*meterToPixel)), (int) this.getHeight()-12);
 			g2d.drawString("0m", 8, (int) this.getHeight()-15);
 		}
 	}
 
 	public void declaredRunwaysCreation(Graphics2D g2d){
 		if(airport!=null && runway!=null){
 			int yInc = meterToPixel(runwayHeight/2);
 			g2d.setFont(new Font("key", 1, 15));
 			int width = 3;
 			g2d.setColor(toraColor);
 			g2d.fillRect(xRunway+TORAStart,  (yRunway-yInc),  meterToPixel(TORA), width);
 			
 			g2d.setColor(todaColor);
 			g2d.fillRect(xRunway+TODAStart, (yRunway -yInc*2),  meterToPixel(TODA), width);
 			
 			g2d.setColor(asdaColor);
 			g2d.fillRect(xRunway+ASDAStart, (yRunway -yInc*3),  meterToPixel(ASDA), width);
 			
 			g2d.setColor(ldaColor);
 			g2d.fillRect(xRunway+LDAStart, (yRunway + yInc + meterToPixel(runwayHeight)),  meterToPixel(LDA), width);
 			
 			g2d.setColor(dtColor);
 			
 			g2d.fillRect(xRunway+DTStart, (yRunway + yInc*2 + meterToPixel(runwayHeight)),  meterToPixel(DT), width);
 			
 			if(obstacle!=null){
 			g2d.setColor(Color.WHITE);
 			g2d.fillRect(xRunway+TORAStart+ meterToPixel(TORA),  (yRunway-yInc),  meterToPixel(stopway), width);
 
 			g2d.fillRect(xRunway+TODAStart+ meterToPixel(TODA),  (yRunway-yInc*2),  meterToPixel(stopway), width);
 
 			g2d.fillRect(xRunway+ASDAStart+ meterToPixel(ASDA),  (yRunway-yInc*3),  meterToPixel(stopway), width);
 
 			g2d.fillRect(xRunway+LDAStart+ meterToPixel(LDA),  (yRunway+yInc+meterToPixel(runwayHeight)),  meterToPixel(stopway), width);
 			
 			
 			int greater = RESA;
 			g2d.setColor(resaColor);
 			if(greater<ANGLE){
 				g2d.setColor(Color.BLACK);
 				greater=ANGLE;
 			}
 			
 			
 			if(airport.getCurrentRunway().getName().equals(leftTag)){
 								
 				g2d.fillRect(xRunway+TORAStart+ meterToPixel(TORA+stopway),  (yRunway-yInc),  meterToPixel(greater), width);
 	
 				g2d.fillRect(xRunway+TODAStart+ meterToPixel(TODA+stopway),  (yRunway-yInc*2),  meterToPixel(greater), width);
 	
 				g2d.fillRect(xRunway+ASDAStart+ meterToPixel(ASDA+stopway),  (yRunway-yInc*3),  meterToPixel(greater), width);
 				
 				g2d.setColor(resaColor);
 	
 				g2d.fillRect(xRunway+LDAStart+ meterToPixel(LDA+stopway),  (yRunway+yInc+meterToPixel(runwayHeight)),  meterToPixel(RESA), width);
 			}else{
 				
 				
 				g2d.fillRect(xRunway+LDAStart+ meterToPixel(LDA+stopway),  (yRunway+yInc+meterToPixel(runwayHeight)),  meterToPixel(greater), width);
 				
 				g2d.setColor(resaColor);
 				
 				g2d.fillRect(xRunway+TORAStart+ meterToPixel(TORA+stopway),  (yRunway-yInc),  meterToPixel(RESA), width);
 	
 				g2d.fillRect(xRunway+TODAStart+ meterToPixel(TODA+stopway),  (yRunway-yInc*2),  meterToPixel(RESA), width);
 	
 				g2d.fillRect(xRunway+ASDAStart+ meterToPixel(ASDA+stopway),  (yRunway-yInc*3),  meterToPixel(RESA), width);
 	
 				
 			}
 			
 			}
 		}
 	}
 
 	public void setValues(){
 		if(airport!=null && runway!=null){
 			this.runwayWidth = (int) runway.getTORA(Runway.DEFAULT);
 			this.runwayHeight = 60;
 			
 			this.TORA = (int) runway.getTORA(Runway.REDECLARED);
 			this.TODA = (int) runway.getTODA(Runway.REDECLARED);
 			this.ASDA = (int) runway.getASDA(Runway.REDECLARED);
 			this.LDA = (int) runway.getLDA(Runway.REDECLARED);
 			this.DT = (int) runway.getDisplacedThreshold(runway.DEFAULT);
 		
 			this.RESA = (int) airport.getCurrentPhysicalRunway().getRESA();
 			this.stopway = (int) airport.getCurrentPhysicalRunway().getStopway();
 			this.leftTag= airport.getCurrentPhysicalRunway().getRunway(0).getName();
 			this.rightTag = airport.getCurrentPhysicalRunway().getRunway(1).getName();
 			runwayStripWidthFromCentreLine=150;
 			runwayToEdgeOfCnGArea = stopway;
 			CnGAreaWidthFromCentreLine = 75;
 			
 			int pWidth = this.getWidth();
 			int pHeight = this.getHeight();
 
 			int width = (int) (ratio*pWidth);
 			meterToPixel=width/(double)runwayWidth;
 
 			int height = meterToPixel(runwayHeight);
 
 			//calculates the x and y values to position the runway on the view
 			xRunway = (int) (((1.0-ratio)/2) * pWidth);
 			yRunway = (pHeight - height)/2;
 			
 			
 			
 			if(obstacle!=null){
 				int distance = (int) airport.getCurrentPhysicalRunway().getDistanceAwayFromThreshold();
 				this.threshold=airport.getCurrentPhysicalRunway().closeTo().getName(); 
 				if(threshold.equals(airport.getCurrentPhysicalRunway().getRunway(0).getName())){this.leftTag=airport.getCurrentPhysicalRunway().getRunway(1).getName();}else{this.leftTag=airport.getCurrentPhysicalRunway().getRunway(0).getName();}
 				this.rightTag=threshold;
 				this.xObstacle=runwayWidth-distance;
 				this.yObstacle = (int) airport.getCurrentPhysicalRunway().getDistanceAwayFromCenterLine();
 				this.obstacleLength =(int) obstacle.getLength();
 				this.obstacleWidth = (int) obstacle.getWidth();
 
 				this.ANGLE=(int) (obstacle.getHeight()*airport.getCurrentPhysicalRunway().getAngleOfSlope());
 				
 				
 			}
 			
 			this.TORAStart=0;
 			this.TODAStart=(int) (TORAStart+meterToPixel(TORA-TODA));
 			this.ASDAStart=(int) (TORAStart+meterToPixel(TORA-ASDA));
 						
 			if(leftTag.equals(airport.getCurrentPhysicalRunway().getRunway(1).getName())){
 				leftDT =(int) airport.getCurrentPhysicalRunway().getRunway(1).getDisplacedThreshold(0);
 				rightDT = (int) airport.getCurrentPhysicalRunway().getRunway(0).getDisplacedThreshold(0);
 				
 			}else{
 				leftDT = (int) airport.getCurrentPhysicalRunway().getRunway(0).getDisplacedThreshold(0);
 				rightDT = (int) airport.getCurrentPhysicalRunway().getRunway(1).getDisplacedThreshold(0);
 				
 			}
 			
 			if(leftDT>0){
 				DTStart = 0;
 				LDAStart = meterToPixel(DT);
 			}
 			if(rightDT>0){
 				DTStart = runwayWidth-meterToPixel(DT);
 				LDAStart = 0;
 			}
 				
 			
 			
 		}
 	}
 
 	public void updateAirport(Airport airport) {
 		this.airport=airport;
 		if(airport!=null &&
 				airport.getCurrentPhysicalRunway() != null)
 		{
 			runway=airport.getCurrentRunway();
 			obstacle=airport.getCurrentPhysicalRunway().getObstacle();
 			setOffset(0, 0);
 			setValues();
 			repaint();
 		}
 
 	}
 
 	public void setZoom(double ratio){
 		this.ratio = ratio;
 	}
 
 	public void setOffset(int x, int y){
 		int a = meterToPixel(runwayWidth/2);
 		int b = meterToPixel(this.getHeight());
 		if(x>a){
 			x=a;
 		}
 		if(y>b){
 			y=b;
 		}
 		if(x<-a){
 			x=-a;
 		}
 		if(y<-b){
 			y=-b;
 		}
 		this.xOffset=x;
 		this.yOffset=y;
 	}
 	
 	
 	public void setDragCursor(final JComponent component){
 		Cursor openHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("data/opengrab.png"), new Point(16,16), "closed");
 		component.setCursor(openHandCursor);	
 
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				super.mousePressed(e);
 				Cursor closedHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("data/closedgrab.png"), new Point(16,16), "closed");
 				component.setCursor(closedHandCursor);	
 
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				super.mouseReleased(e);
 				Cursor openHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("data/opengrab.png"), new Point(16,16), "closed");
 				component.setCursor(openHandCursor);	
 			}
 		});
 	}
 }
 
 
 
