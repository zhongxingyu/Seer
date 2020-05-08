 package visualization;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Map;
 
 class Surface extends JPanel implements ActionListener {
 	
 //	Flower flower1 = new Flower(Color.BLUE, 50, 300, 250, 10, );
 //	Flower flower2 = new Flower(Color.RED, 25, 500, 350, 10);
 //	Flower[] flowers  = {flower1, flower2};
 //	
 	int currentFrame = 0;
 	
 	int frameRate = 10;
 	int currentTFrame = 0;
 	
 	Repository repo;
 	
 	private Timer timer;
 	
 	public Surface () {
 		Initialize();
 	}
 	
 	private void Initialize () {
 	       timer = new Timer(100, this);
 	       timer.start(); 
 	       
 	       repo = new Repository("sampleXMLFile.xml");
 	}
 	
     @Override
     public void actionPerformed (ActionEvent e) {    	
     	    	
     	   	
     	repaint();
     	
     	
 //    	for(int i=0;i<flowers.length;i++){
 //			Flower flower = flowers[i];
 //						
 //			flower.makeDarker();		    
 //		    //System.out.println("Darker");
 //    	}
     	
     	if(currentTFrame > frameRate){
     		currentTFrame = 0;
     		
     		if(currentFrame < repo.frames.length-1)
     		currentFrame++;
     	}
     	
     	currentTFrame++;
     	
     }
     
     private void drawContributorLegend (Graphics g){
     	Contributor[] contributors = repo.contributors;
     	
     	Font font = Font.decode("Times New Roman");
     	
     	int x, y;
     	x = 10;
     	y = 10;
     	
     	for(Contributor contributor : repo.contributorColor.values()){
 	
 
     		Rectangle2D nameRect = g.getFontMetrics(font).getStringBounds(contributor.name, g);
     		double nameWidth = nameRect.getWidth();
     		
     		if(x+30+15+nameWidth > Visualization.width){
     			y += 25 + 20;
     			x = 10;
     		}
     		g.setColor(Color.white);
     	    g.fillRect(x, y,30 + 15 + (int) nameWidth,25);
     	    
     		g.setColor(contributor.color);
     		g.fillRect(x, y, 25,25);
     		g.setColor(Color.black);
     		g.drawString(contributor.name, x + 30, y + 25);
     		
     		x += 30 + 15 + nameWidth;
     		
 
     	}
     }
     
 	
 	private void doDrawing(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;	
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, Visualization.width,Visualization.height);
 		AffineTransform at = g2.getTransform();
 
 		drawFlowers(g2);
 		
 		g2.setTransform(at);
 		drawContributorLegend(g2);
 
     }
 	
 	private void testCollision (Graphics2D g){
 //		int radius1 = 50;
 //		int radius2 = 50;
 //		int x1 = 20;
 //		int y1 = 20;
 //		int x2 = 150;
 //		int y2 = 150;
 //		
 //			
 //		g.setColor(Color.blue);
 //		g.fillOval(x1,y1,radius1*2,radius1*2);
 //		
 //		g.setColor(Color.red);
 //		g.fillOval(x2,y2,radius2*2,radius2*2);
 //		
 		Flower flower1 = new Flower(null, Color.BLUE, 100, 0, 0, 10, null, null );
 		Flower flower2 = new Flower(null, Color.RED, 100, 20, 250, 10, null, null);
 		double radius1 = flower1.size/2 * 3;
 		double radius2 = flower2.size/2 * 3;
 		
 		g.setColor(flower1.color);
 		g.fillOval(flower1.x-flower1.size/2,flower1.y-flower1.size/2,flower1.size,flower1.size);
 		drawPedals(g, flower1);
 		
 		g.setColor(flower2.color);
 		g.fillOval(flower2.x-flower2.size/2,flower2.y-flower2.size/2,flower2.size,flower2.size);
 		drawPedals(g, flower2);
 		
 		System.out.println("Collision Test: " + Flower.checkCollision(flower1.x,flower1.y,radius1,flower2.x,flower2.y,radius2));
 	}
 	
 	private void drawFlowers(Graphics2D g) {
 		
 		Frame frame = repo.frames[currentFrame];
 		Map<String, Flower> flowersMap = repo.flowers;
 		
 		Flower[] currentFlowers = frame.flowers;
 		
     	for(int i=0;i<currentFlowers.length;i++){
 			Flower frameFlower = currentFlowers[i];
 						
 			Flower flower = flowersMap.get(frameFlower.methodName);
 			
 			
 			flower.size = frameFlower.size;
 			
 //			if(flower.size > maxFlowerSize){
 //				flower.size = maxFlowerSize;
 //			}else if(flower.size < maxFlowerSize*.3){
 //				flower.size = (int) Math.ceil( maxFlowerSize * .3);
 //			}
 //			int size = (int) ((int) ((flower.size / repo.maxClassLines) * (maxFlowerSize - maxFlowerSize*0.4)) + maxFlowerSize*0.4);
 //			if (size > maxFlowerSize) {
 //				flower.size = maxFlowerSize;
 //			} else {
 //				flower.size = size;
 //			}
 			
 			flower.numMethods = frameFlower.numMethods;
 			flower.contributor = frameFlower.contributor;
 			
 			
 			if(frameFlower.changed){
 				flower.color = frameFlower.color;
 				frameFlower.changed = false;
 
 			}
 			
 			flower.makeDarker();
 			
 //			g.setColor(Color.black);
 //			g.fillOval(flower.x-(flower.size*3/2),flower.y-(flower.size*3/2),flower.size*3,flower.size*3);
 			g.setColor(flower.color);
 			g.fillOval(flower.x-flower.size/2,flower.y-flower.size/2,flower.size,flower.size);
 			
 			
 			
 			drawPedals(g, flower);
     	}
     	
     	
 		
 //		for(int i=0;i<flowers.length;i++){
 //			Flower flower = flowers[i];
 //			
 //			g.setColor(flower.color);
 //			g.fillOval(flower.x-flower.size/2,flower.y-flower.size/2,flower.size,flower.size);
 //			drawPedals(g, flower);
 //		}
 		
 	}
 	
 	private void drawPedals(Graphics2D g, Flower flower){			
 		
 		int pedalSize = flower.size/2;
 		
 		flower.numMethods = Math.min(flower.numMethods, 10);
 		
 		for(int i=0;i<flower.numMethods;i++){
 			g.fillOval(flower.x-pedalSize/2,flower.y + flower.size/2,pedalSize,pedalSize*2);
 //			g.draw(new Ellipse2D.Double(flower.x-pedalSize/2,flower.y + flower.size/2,pedalSize,pedalSize*2));
 			g.rotate(Math.toRadians(360/flower.numMethods),flower.x,flower.y);
 		}
 	}
 
     @Override
     public void paintComponent(Graphics g) {
 
         super.paintComponent(g);
         doDrawing(g);
     }
 }
