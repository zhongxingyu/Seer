 //This class and it's bro need to have a common parent that is more specific that JPanel ;)
 
 package View;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import javax.swing.JPanel;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.Obstacle;
 import Model.Runway;
 
 
 @SuppressWarnings("serial")
 public class TopView extends JPanel implements AirportObserver{
 	
 	
 	//this value determines the amount of pixels the tag is from the end of the runway
 	final int tagBorder = 10;
 	
 	//this value determines how much of the width of the panel the runway takes up.
 	final double ratio = 0.75;
 	
 	//this value determines how much of the width of the runway the runwayTag takes up.
 	final double fontRatio = 0.5;
 	
 	//final removed to prevent warning
 	double tagRotate = 1;
 	
 	double r;
 	int TORA;
 	int TODA;
 	int ASDA;
 	int LDA;
 	int TORAStart;
 	int TODAStart;
 	int ASDAStart;
 	int LDAStart;
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
 	
 	//relative to panel
 	int xRunway;
 	int yRunway;
 	
 	//relative to runway
 	int xObstacle;
 	int yObstacle;
 	
 		
 	public TopView(Airport airport){
 		super();
 		setSize(300,200);
 		this.setBackground(Color.WHITE);
 		setVisible(true);
 		updateAirport(airport);
 		setValues();		
 	}
 	
 	public void paint (Graphics g){
 		super.paint(g);
 		Graphics2D g2d = (Graphics2D)g;
 		//TODO: You can have this back when you fix it!
 		//runwayCreation(g2d);
 		if(obstacle != null)obstacleCreation(g2d);
 		declaredRunwaysCreation(g2d);
 			
 	}
 	
 	public void runwayCreation(Graphics2D g2d){
 		g2d.setColor(Color.GRAY);
 		
 		int pWidth = this.getWidth();
 		int pHeight = this.getHeight();
 		
 		
 		int width = (int) (ratio*pWidth);
 		r=width/(double)runwayWidth;
 				
 		int height = (int) (runwayHeight * r);
 		
 		if(2*height>pHeight){
 			height = (int) (pHeight/2);
 			r=height/(double)runwayHeight;
 			width=(int) (runwayWidth*r);
 		}
 		
 			
 		//calculates the x and y values to position the runway on the view
 		xRunway = (int) (((1.0-ratio)/2) *pWidth);
 		yRunway = (pHeight - height)/2;
 		
 		g2d.fillRect(xRunway, yRunway, width, height);
 		
 		//27R 08L tags
 		g2d.setColor(Color.BLACK);
 		int i;
 		for(i=0; i<1000; i++){
 			Font f = new Font("tag", 1, 1000-i);
 			g2d.setFont(f);
 			if(g2d.getFontMetrics().stringWidth(leftTag)<=(fontRatio*height))break;
 		}
 		
 		if(tagRotate == 1){
 			g2d.rotate(0.5 * Math.PI,  xRunway+tagBorder, yRunway+(height/4));
 			g2d.drawString(leftTag, xRunway+ tagBorder, yRunway+(height/4));
 			g2d.rotate(-0.5 * Math.PI,  xRunway + tagBorder, yRunway+(height/4));
 			g2d.rotate(-0.5 * Math.PI,  xRunway + width - tagBorder, yRunway+(3*(height/4)));
 			g2d.drawString(rightTag, xRunway+ width - tagBorder, yRunway+(3*(height/4)));
 			g2d.rotate(0.5 * Math.PI,  xRunway + width - tagBorder, yRunway+(3*(height/4)));
 		}else{
 			int offset = g2d.getFontMetrics().getHeight();
 			g2d.rotate(-0.5 * Math.PI,  xRunway+tagBorder+offset, yRunway+(3*(height/4)));
 			g2d.drawString(leftTag, xRunway+ tagBorder+offset, yRunway+(3*(height/4)));
 			g2d.rotate(0.5 * Math.PI,  xRunway + tagBorder+offset, yRunway+(3*(height/4)));
 			g2d.rotate(0.5 * Math.PI,  xRunway + width - tagBorder - offset, yRunway+(height/4));
 			g2d.drawString(rightTag, xRunway+ width - tagBorder - offset, yRunway+(height/4));
 			g2d.rotate(-0.5 * Math.PI,  xRunway + width - tagBorder - offset, yRunway+(height/4));
 		}
 		
 		
 	}
 	
 	public void obstacleCreation(Graphics2D g2d){
 		if(obstacle!=null){
 			g2d.setColor(Color.RED);
 			g2d.fillRect((int) ((r*xObstacle)+xRunway), (int)((r*yObstacle)+yRunway), (int)(r*obstacleLength), (int)(r*obstacleWidth));
 		}
 	}
 	
 	public void declaredRunwaysCreation(Graphics2D g2d){
 		Color toraColor = new Color(255, 0, 0, 125);
 		Color todaColor = new Color(0, 255, 0, 125);
 		Color asdaColor = new Color(0, 0, 255, 125);
 		Color ldaColor = new Color(255, 0, 255, 125);
 		if(obstacleLeft){
 			g2d.setColor(toraColor);
 			g2d.fillRect(xRunway+TORAStart-(int) (TORA*r), yRunway, (int) (TORA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			g2d.setColor(todaColor);
 			g2d.fillRect(xRunway+TODAStart-(int) (TODA*r), (int) (yRunway -((runwayHeight*r)/2)), (int) (TODA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			g2d.setColor(asdaColor);
 			g2d.fillRect(xRunway+ASDAStart-(int) (ASDA*r), (int) (yRunway +((runwayHeight*r)/2)), (int) (ASDA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			g2d.setColor(ldaColor);
 			g2d.fillRect(xRunway+LDAStart-(int) (LDA*r), (int) (yRunway + (2* ((runwayHeight*r)/2))), (int) (LDA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			
 		}else{	
 			g2d.setColor(toraColor);
 			g2d.fillRect(xRunway+TORAStart, yRunway, (int) (TORA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			g2d.setColor(todaColor);
 			g2d.fillRect(xRunway+TODAStart, (int) (yRunway -((runwayHeight*r)/2)), (int) (TODA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			g2d.setColor(asdaColor);
 			g2d.fillRect(xRunway+ASDAStart, (int) (yRunway +((runwayHeight*r)/2)), (int) (ASDA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 			g2d.setColor(ldaColor);
 			g2d.fillRect(xRunway+LDAStart, (int) (yRunway + (2* ((runwayHeight*r)/2))), (int) (LDA*r), (int) (((runwayHeight*r)/2)-(r*runwayHeight *0.05)));
 		}
 		
 		g2d.setColor(Color.BLACK);
 		g2d.drawString("TORA", (int) (xRunway+((TORA*r)/2)), (yRunway+g2d.getFontMetrics().getHeight()));
 		g2d.drawString("TODA", (int) (xRunway+((TODA*r)/2)), (int) ((yRunway -((runwayHeight*r)/2)) +g2d.getFontMetrics().getHeight()));
 		g2d.drawString("ASDA", (int) (xRunway+((ASDA*r)/2)), (int) ((yRunway +((runwayHeight*r)/2) +g2d.getFontMetrics().getHeight())));
 		g2d.drawString("LDA", (int) (xRunway+((LDA*r)/2)), (int) ((yRunway + (2* ((runwayHeight*r)/2)) +g2d.getFontMetrics().getHeight())));
	
 	}
 	
 	
 	public void setValues(){
 		this.runwayWidth = (int) runway.getTORA(runway.DEFAULT);
 		this.runwayHeight = runwayWidth/20;
 		this.leftTag=airport.getCurrentPhysicalRunway().getRunway(0).getName();
 		this.rightTag=airport.getCurrentPhysicalRunway().getRunway(1).getName();
 		this.TORA = (int) runway.getTORA(runway.REDECLARED);
 		this.TODA = (int) runway.getTODA(runway.REDECLARED);
 		this.ASDA = (int) runway.getASDA(runway.REDECLARED);
 		this.LDA = (int) runway.getLDA(runway.REDECLARED);
 		
 		if(obstacle!=null){
 			int distance = (int) airport.getCurrentPhysicalRunway().getDistanceAwayFromThreshold();
 			this.threshold=airport.getCurrentPhysicalRunway().closeTo().getName(); 
 			obstacleLeft=threshold.equals(leftTag);
 			if(obstacleLeft){this.xObstacle = distance;}else{this.xObstacle=runwayWidth-distance;}
 			this.yObstacle = runwayHeight/2;
 			this.obstacleLength =(int) obstacle.getLength();
 			this.obstacleWidth = (int) obstacle.getWidth();
 		}else{
 			obstacleLeft=false;
 		}
 		if(obstacleLeft){this.LDAStart = runwayWidth;}else{this.LDAStart=0;}
 		if(obstacleLeft){this.TORAStart = runwayWidth;}else{this.TORAStart=0;}
 		if(obstacleLeft){this.TODAStart = runwayWidth;}else{this.TODAStart=0;}
 		if(obstacleLeft){this.ASDAStart = runwayWidth;}else{this.ASDAStart=0;}
				
 	}
 
 	public void updateAirport(Airport airport) {
 		this.airport=airport;
 		if(airport!=null){
 			runway=airport.getCurrentRunway();
 			obstacle=airport.getCurrentPhysicalRunway().getObstacle();
 		}
 		
 	}
 }
 
 
 
