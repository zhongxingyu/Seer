 package edu.cs56.projects.discretemath.towers_sierpinski;
 import java.awt.Toolkit;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.BasicStroke;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JPanel;
 import java.awt.Color;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import javax.swing.*;
 import java.awt.event.*;
 /**
    @author Jacob Anderson, Gordon Cheung
    @version CS56, S13
 */
 
 public class S extends JPanel{
 
     int numberOfT=0;
     
     private boolean printMessage=false;
     
     /**
       method to draw ellipse from the center. 
       @param x the x component of the original ellipse
       @param y the y component of the original ellipse
       @param width width of the ellipse
       @param height height of the ellipse
 
      */
     
     public Ellipse2D centerOfDisk(double x, double y, double width, double height)//code to place from center of ellipse rather than from topleft
     {
         double newX = x - width / 2.0;
         double newY = y - height / 2.0;
 
         Ellipse2D newDisk = new Ellipse2D.Double(newX, newY, width, height);
 
         return newDisk;
     }
 
     /**
        draws the towers based off of the top of the depth0 triangle
        @param startX x coordinate of the top of the depth0 triangle
        @param startY y coordinate of the top of the dpeth 0 triangle
        @param sideLength length of a side of the triangle
        @param g the graphics
      */
     
     public void drawTowers(double startX, double startY, double sideLength, Graphics g){
 	//startX and startY are both based off of the bottom left of the towers
 	Graphics2D g2da = (Graphics2D) g;
 	g2da.setColor(Color.black);
 	g2da.setStroke(new BasicStroke(3));
 	g2da.drawLine((int)startX-(int)sideLength/3,(int)startY,(int)startX+(int)sideLength/3,(int)startY); //base line
 	g2da.drawLine((int)startX+(int)sideLength/4-(int)sideLength/2,(int)startY,(int)startX+(int)sideLength/4-(int)sideLength/2,(int)startY-(int)sideLength*1/3);//first peg
 	g2da.drawLine((int)startX+(int)sideLength/2-(int)sideLength/2,(int)startY,(int)startX+(int)sideLength/2-(int)sideLength/2,(int)startY-(int)sideLength*1/3);//2nd peg
 	g2da.drawLine((int)startX+(int)sideLength*3/4-(int)sideLength/2,(int)startY,(int)startX+(int)sideLength*3/4-(int)sideLength/2,(int)startY-(int)sideLength*1/3);//3rdpeg
     }
     
     
 
     /**
        draws the towers and disks
        @param depth the number of disks-1
        @param colors an arraylist of the colors that the disks should be
        @param lefttopright the string of the current location of the top of depth0 triangle
        @param g graphics
        @param tx the x coordinate of the top of the current triangle
        @param ty the y coordinate of the top of the current triangle
        @param sideLength the length of a side of the triangle
 
      */
     public void drawSystem(int depth, ArrayList<Color> colors, String lefttopright, Graphics g, double tx, double ty, double sideLength){
 	if(depth!=0){
 	    return;
 	}
 	
 	Graphics2D g2d = (Graphics2D) g;
 	if(lefttopright.length()==0){
 	    Font font = new Font("Arial", Font.BOLD, 14);
 	    g2d.setFont(font);
 	}
 	else{
 	    Font font = new Font("Arial", Font.BOLD,25/lefttopright.length());
 	    g2d.setFont(font);
 	}
 	
 	double x = sideLength * Math.cos(Math.PI/3);
 	double y = sideLength * Math.sin(Math.PI/3);
 	
 
 	
 	double tW = sL;
 	double tH = 1.5*sL/4;
 	double eH = Math.min(tH/(n+1),20);
 	double tcX = tx;
 	double tcY = ty;
 	double lcX = tx - x;
 	double lcY = ty+y*1.1;
 	double rcX = tx + x;
 	double rcY = ty+y*1.1;
 	
 		
 	if(lefttopright.length()==0){
 	}
 	else{
 	    block:{
 		
 		if(lefttopright.charAt(0)=='T'){
 		    if(lefttopright.charAt(lefttopright.length()-1)=='L'){
 			lcX -= 1.5*x;
 			rcY += y/2;
 			tcX -= 1.5*x;
 			boolean isR=false;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if('R' == lefttopright.charAt(q)) isR=true;
 			}
 			if(isR){
 			    lcX+=1.5*x;
 			    lcY+=y/2;
 			}
 		    }
 		    else if(lefttopright.charAt(lefttopright.length()-1)=='R'){
 			rcX += 1.5*x;
 			lcY += y/2;
 			tcX += 1.5*x;
 			boolean isL=false;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if('L' == lefttopright.charAt(q)) isL=true;
 			}
 			if(isL){
 			    rcX-=1.5*x;
 			    rcY+=y/2;
 			}
 			
 			
 		    }
 		    else if(lefttopright.charAt(lefttopright.length()-1)=='T'){
 			rcX += 1.5*x;
 			lcX -= 1.5*x;
 			boolean allSame=true;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if(lefttopright.charAt(0) != lefttopright.charAt(q)) allSame=false;
 			}
 			if(allSame){
 			}
 			else{
 			    boolean noL=true;
 			    boolean noR=true;
 			    int indexL=0;
 			    int indexR=0;
 			    for(int q=0; q<lefttopright.length()-1; q++){
 				if('L' == lefttopright.charAt(q)) {noL=false; indexL=q;}
 				if('R' == lefttopright.charAt(q)) {noR=false; indexR=q;}
 				
 			    }
 			    if(!noL && !noR){
 				if(indexL>indexR)
 				    tcX -=1.5*x;
 				else
 				    tcX+=1.5*x;
 			    }
 			    else if(noL){
 				tcX += 1.5*x;
 			    }
 			    
 			    else if(!noL){
 				tcX -= 1.5*x;
 			    }
 			    
 			}
 		    }
 		}
 		else if(lefttopright.charAt(0)=='R'){
 		    if(lefttopright.charAt(lefttopright.length()-1)=='T'){
 			rcX += 1.5*x;
 			lcX -= 1.5*x;
 			tcX += 1.5*x;
 			boolean isL=false;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if('L' == lefttopright.charAt(q)) isL=true;
 			}
 			if(isL){
 			    tcX-=3*x;
 			}
 		    }
 		    else if(lefttopright.charAt(lefttopright.length()-1)=='L'){
 			tcX -=1.2*x;
 			lcY += y/2;
 			rcY += y/2;
 			boolean isT=false;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if('T' == lefttopright.charAt(q)) isT=true;
 			}
 			if(isT){
 			    lcY-=y/2;
 			    lcX-=1.5*x;
 			}
 		    }
 		    else if(lefttopright.charAt(lefttopright.length()-1)=='R'){
 			tcX += 1.5*x;
 			lcY += y/2;
 			
 			boolean allSame=true;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if(lefttopright.charAt(0) != lefttopright.charAt(q)) allSame=false;
 			}
 			if(allSame){
 			    rcX +=1.5*x;
 			}
 			else{ 
 			    boolean noL=true;
 			    boolean noT=true;
 			    int indexL=0;
 			    int indexT=0;
 			    for(int q=0; q<lefttopright.length()-1; q++){
 				if('L' == lefttopright.charAt(q)) {noL=false; indexL=q;}
 				if('T' == lefttopright.charAt(q)) {noT=false; indexT=q;}
 			    }
 			    if(!noL && !noT){
 				if(indexL < indexT)
 				    rcX += 1.5*x;
 				else
 				    rcY += y/2;
 				//rcY -= y/2;
 			    }
 			    else if(noL){
 				rcX += 1.5*x;
 			    }
 			    else{
 				rcY += y/2;
 			    }
 			}
 		    }
 		}
 		else if(lefttopright.charAt(0)=='L'){
 		    if(lefttopright.charAt(lefttopright.length()-1)=='T'){
 			rcX += 1.5*x;
 			lcX -= 1.5*x;
 			tcX -= 1.5*x;
 			boolean isR=false;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if('R' == lefttopright.charAt(q)) isR=true;
 			}
 			if(isR){
 			    tcX+=3*x;
 			}
 		    }
 		    else if(lefttopright.charAt(lefttopright.length()-1)=='R'){
 			tcX += 1.2*x;
 			lcY += y/2;
 			rcY += y/2;
 			boolean isT=false;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if('T' == lefttopright.charAt(q)) isT=true;
 			}
 			if(isT){
 			    rcY-=y/2;
 			    rcX+=1.5*x;
 			}
 		    }
 		    else if(lefttopright.charAt(lefttopright.length()-1)=='L'){
 			tcX -= 1.5*x;
 			rcY += y/2;
 			boolean allSame=true;
 			for(int q=0; q<lefttopright.length()-1; q++){
 			    if(lefttopright.charAt(0) != lefttopright.charAt(q)) allSame=false;
 			}
 			if(allSame){
 			    lcX -= 1.5*x;
 			}
 			else{
 			    boolean noL=true;
 			    boolean noT=true;
 			    int indexR=0;
 			    int indexT=0;
 			    for(int q=0; q<lefttopright.length()-1; q++){
 				if('R' == lefttopright.charAt(q)) {noL=false; indexR=q;}
 				if('T' == lefttopright.charAt(q)) {noT=false; indexT=q;}
 			    }
 			    if(!noL && !noT){
 				if(indexR < indexT)
 				    lcX -= 1.5*x;
 				else
 				    lcY += y/2;
 			    }
 			    else if(noL){
 				lcX -= 1.5*x;
 			    }
 			    else{
 				lcY += y/2;
 			    }
 			}
 		    }
 		}
 		
 	    }
 	    
 	}
 	
 	
 	drawTowers(tcX,tcY,sideLength,g);
 	drawTowers(rcX,rcY,sideLength,g);
 	drawTowers(lcX,lcY,sideLength,g);
 	
 	Point2D.Double[] p = {new Point2D.Double(lcX,lcY),new Point2D.Double(tcX,tcY),new Point2D.Double(rcX,rcY)};
 
 	int lastDiskWidth=0;
 	int[] disks = {1,1,1};
 	String hBase = "0,1,2";
 	for (int i=0;i<lefttopright.length();i++){
 	    lastDiskWidth=i;
 	    double newWidth=tW;
 	    if(lefttopright.length()==0){
 		newWidth=tW;
 	    }
 	    else{
 		newWidth= tW*(lefttopright.length()-i*.5)/lefttopright.length();
 	    }
 	    String[] h = hBase.split(",");
 	    switch(lefttopright.charAt(i)){
 	    case 'T': 
 		if (depth==0){
 		    int d = Integer.parseInt(h[1]);
 		    for (int j = 0; j < 3; j++){
 			if(lefttopright.length()==0){
 			    
 			    g2d.setColor(colors.get(n-i));
 			    
 			    Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/6, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 			    g2d.fill(disk2);
 			    
 			    //g2d.fill(new Ellipse2D.Double(p[j].x-tW/2+d*tW/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			    g2d.setColor(Color.BLACK);
 			    g2d.drawString(""+(n-i),(float)(p[j].x-tW/3+d*tW/3),(float)(p[j].y-eH*(disks[d]-1)));
 			}
 			else{
 			    g2d.setColor(colors.get(n-i));
 			    g2d.fill(new Ellipse2D.Double(p[j].x-newWidth/2+d*newWidth/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			    g2d.setColor(Color.BLACK);
 			    g2d.drawString(""+(n-i),(float)(p[j].x-newWidth/3+d*newWidth/3),(float)(p[j].y-eH*(disks[d]-1)));
 			    
 			}
 		    }
 		    disks[d]++;
 		    hBase = ""+h[2]+","+h[1]+","+h[0];
 		}
 
 		break;
 	    case 'R':
 		if (depth==0){
 		    int d = Integer.parseInt(h[2]);
 		    for (int j = 0; j < 3; j++){
 			g2d.setColor(colors.get(n-i));
 			if(i==0){
 			    Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/12, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 			    g2d.fill(disk2);
 			    g2d.setColor(Color.BLACK);
 
 			    g2d.drawString(""+(n-i),(float)(p[j].x-tW/3+d*tW/3-tW/12),(float)(p[j].y-eH*(disks[d]-1)));
 
 			}
 			
 			
 			else{
 			    g2d.setColor(colors.get(n-i));
 			    g2d.fill(new Ellipse2D.Double(p[j].x-newWidth/2+d*newWidth/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			    g2d.setColor(Color.BLACK);
 			    g2d.drawString(""+(n-i),(float)(p[j].x-newWidth/3+d*newWidth/3),(float)(p[j].y-eH*(disks[d]-1)));
 			    
 			}
 
 		    }
 		    
 		    disks[d]++;
 		    hBase= ""+h[1]+","+h[0]+","+h[2];
 		}
 		break;
 	    case 'L':
 		if(depth==0){
 		    int d = Integer.parseInt(h[0]);
 		    for (int j = 0; j < 3; j++){
 			
 			g2d.setColor(colors.get(n-i));
 			if(i==0){
 			    Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/4, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 			    g2d.fill(disk2);
 			    g2d.setColor(Color.BLACK);
 			    g2d.drawString(""+(n-i),(float)(p[j].x-tW/3+d*tW/3+tW/12),(float)(p[j].y-eH*(disks[d]-1)));
 			}
 			else{
 			    g2d.setColor(colors.get(n-i));
 			    g2d.fill(new Ellipse2D.Double(p[j].x-newWidth/2+d*newWidth/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			    g2d.setColor(Color.BLACK);
 			    g2d.drawString(""+(n-i),(float)(p[j].x-newWidth/3+d*newWidth/3),(float)(p[j].y-eH*(disks[d]-1)));
 			}
 		    }
 		    disks[d]++;
 		    hBase=""+h[0]+","+h[2]+","+h[1];
 		}
 		break;
 	    }
 
 	}
 	
 	if (depth==0){
 	    double newWidth= tW*(lefttopright.length()-(lastDiskWidth+1)*.5)/lefttopright.length();
 	    if(lefttopright.length()==0){
 		newWidth=tW;
 		String[] h = hBase.split(",");
 		for (int j = 0; j < 3; j++){
 		    int d = Integer.parseInt(h[j]);
 		    g2d.setColor(colors.get(0));
 		    Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+tW/2, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 		    g2d.fill(disk2);
 		    g2d.setColor(Color.BLACK);
 		    if(lefttopright.length()==0)
 			g2d.drawString(""+0,(float)(p[j].x-tW/2+tW/2),(float)(p[j].y-eH*(disks[d]-1)));
 		    else
 			g2d.drawString(""+0,(float)(p[j].x-tW/3+d*tW/3-tW/16),(float)(p[j].y-eH*(disks[d]-1)));
 		}
 	    }
 	    else{
 		if (depth==0){
 		    String[] h = hBase.split(",");
 		    g2d.setFont(eF);
 		    for (int j = 0; j < 3; j++){
 			int d = Integer.parseInt(h[j]);
 			g2d.setColor(colors.get(0));
 			
 			Ellipse2D disk2 = centerOfDisk(p[j].x, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 
 			g2d.fill(new Ellipse2D.Double(p[j].x-newWidth/3/2-newWidth/2+d*newWidth/2, p[j].y-eH*disks[d],newWidth/3,eH));
 			if(lefttopright.length()==0){
 			    Font font = new Font("Arial", Font.BOLD, 14);
 			    g2d.setFont(font);
 			}
 			else{
 			    Font font = new Font("Arial", Font.BOLD,25/lefttopright.length());
 			    g2d.setFont(font);
 			}
 			
 			g2d.setColor(Color.BLACK);
 			g2d.drawString(""+0,(float)(p[j].x-newWidth/2-newWidth/2+newWidth/2+d*newWidth/2),(float)(p[j].y-eH*(disks[d]-1)));
 			
 		    }
 		}
 		
 	    }
 	}
 	
     }
     
 
     
     
     private int n;
     private ArrayList<Color>colors;
     private double tx, ty;
     private double side, sL;
     private double width,height;
     private double tS, hS;
     private Font mF, eF;
 
     /**
        constructor
 
        @param n the number of disks -1
        
      */
 
     public S(int n) {
 	initS(n);
 	this.colors = new ArrayList<Color>();
 	for (int i=0;i<n+1;i++){
     		colors.add(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
 	}
 
     }
 
     public void initS(int n){
 	this.n=n;
 	side=Math.min(175*(Math.pow(2,n+1)-1),getPreferredSize().getHeight()*4/5);
 	sL=side/(Math.pow(2,n+1)-1);
	tx=getPreferredSize().getWidth()*2/3;
 	ty=100;
 
 	tS=3;
 	hS=30;
 	mF=new Font("Arial", Font.PLAIN, 12);
 	eF=mF;
 
 	}
     /**
        draws a single triangle
        @param g graphics
        @param depth the # of disks -1
        @param tX the x coordinate of the top of the current triangle
        @param tY the y coordinate of the top of the current triangle
        @param order a string that represent the location of the top of the current triangle
      */
     public void drawMove(Graphics g, int depth, double tX, double tY, String order){
 	//order stuff
 	String base = "01,02,12";
 	for (char c: order.toCharArray()){
 	    String[] b = base.split(",");
 	    switch(c){
 	    case 'T': 
 		base=""+b[2].charAt(1)+b[2].charAt(0)+","+b[1].charAt(1)+b[1].charAt(0)+","+b[0].charAt(1)+b[0].charAt(0);
 		break;
 	    case 'R':
 		base=""+b[0].charAt(1)+b[0].charAt(0)+","+b[2]+","+b[1];
 		break;
 	    case 'L':
 		base=""+b[1]+","+b[0]+","+b[2].charAt(1)+b[2].charAt(0);
 		break;
 	    }
 
 	}
 	String move = "move "+depth+": ";
 	String[]  b = base.split(",");
 
 	double x1 = sL * (Math.pow(2,depth)-1) * Math.cos(Math.PI/3);
 	double y1 = sL * (Math.pow(2,depth)-1) * Math.sin(Math.PI/3);
 	double x2 = sL * (Math.pow(2,depth)) * Math.cos(Math.PI/3);
 	double y2 = sL * (Math.pow(2,depth)) * Math.sin(Math.PI/3);
 
 	Graphics2D g2 = (Graphics2D) g;
 	g2.setColor(colors.get(depth));
 	g2.setStroke(new BasicStroke(3));
 	g2.draw(new Line2D.Double(tX-x1,tY+y1,tX-x2,tY+y2));
 	g2.draw(new Line2D.Double(tX+x1,tY+y1,tX+x2,tY+y2));
 
 	double bX=tX+sL * (Math.pow(2,depth+1)-1)*Math.cos(Math.PI/3)-sL*Math.pow(2,depth);
 	double bY=tY+sL* (Math.pow(2,depth+1)-1) * Math.sin(Math.PI/3);
 
 	g2.draw(new Line2D.Double(bX,bY,bX+sL,bY));
 
 	//draw Strings along lines
 	AffineTransform at = new AffineTransform();
 	
 	
 	if(order.length()==0){
 	    //mF = new Font("Arial", Font.BOLD, (int) (14));
 	    g2.setFont(mF);
 	}
 	else{
 	    mF = new Font("Arial", Font.BOLD,(int) Math.sqrt(sL));
 	    g2.setFont(mF);
 	}
 	
 	
 	
 	g2.drawString(move+b[1].charAt(0)+"->"+b[1].charAt(1),(float)(bX+tS),(float)(bY+tS)+(float)sL/15);
 	at.rotate(-Math.PI/3);
 	g2.setFont(mF.deriveFont(at));
 	g2.drawString(move+b[0].charAt(0)+"->"+b[0].charAt(1),(float)(tX-x2-tS),(float)(tY+y2-tS));
 	at.rotate(2*Math.PI/3);
 	g2.setFont(mF.deriveFont(AffineTransform.getRotateInstance(Math.PI/3)));
 	g2.drawString(move+b[2].charAt(0)+"->"+b[2].charAt(1),(float)(tX+x1+tS),(float)(tY+y1+tS));
 	
 	
 	drawSystem(depth,colors,order,g,tX,tY,sL);
     }
 
     /**
        calls drawMove to draw Sierpinski's Triangle
        @param g graphics
        @param depth the number of disks-1
        @param topX the x coordinate of the top of the current triangle
        @param topY the y coordinate of the top of the current triangle
        @param order a string to represent the location of each iteration
      */
 
     public void drawSierpinski(Graphics g, int depth, double topX, double topY, String order){
 
 	if(depth < 0) return;
 	//drawSystem(depth,colors,order,g,topX,topY,sL);
 	drawMove(g, depth, topX, topY, order);
 	
 	double x = sL * Math.pow(2,depth) * Math.cos(Math.PI/3);
 	double y = sL * Math.pow(2,depth) * Math.sin(Math.PI/3);
 
 	drawSierpinski(g,depth-1, topX, topY, order+"T");
 	drawSierpinski(g,depth-1, topX-x,topY+y, order+"L");
 	drawSierpinski(g,depth-1, topX+x, topY+y, order+"R");
 
     }
     /** 
 	paint component method
 	@param g graphics
     */
 
     public void paintComponent(Graphics g){
 	super.paintComponent(g);
 	drawSierpinski(g, n, tx, ty, "");
     }
 
 	public void setPreferredSize(Dimension d){
 		super.setPreferredSize(d);
 		initS(this.n);
 	}
     /**
        main
        @param args the number of disks-1
      */
     public static void main(String[] args){
 	JFrame f = new JFrame("Sierpinski's Triangle with Towers of Hanoi");
 	if (args.length != 1){
 		System.err.println("Usage: ant -D#=numDisks run");
 		System.exit(1);
 	}
 	 int n=0;
 	try{
 		n = Integer.parseInt(args[0]);
 	}
 	catch (NumberFormatException nfe){
 		System.err.println("Usage: ant -D#=numDisks run");
 		System.exit(1);
 	}
 	final int numDisks=n;
 	final S s = new S(numDisks-1);
 	final JPanel top = new JPanel();
 	final JButton png = new JButton("save png");
 	final JButton jpg = new JButton("save jpg");
 	final JButton small = new JButton("small");
 	final JButton large = new JButton("large");
 	png.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 			top.remove(jpg);
 			top.remove(png);
 			top.remove(small);
 			top.remove(large);
 			SaveImage.savePNG(s);
 			top.add(png);
 			top.add(jpg);
 			top.add(small);
 			top.add(large);
 		}
 	});
 	jpg.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 			top.remove(jpg);
 			top.remove(png);
 			top.remove(small);
 			top.remove(large);
 			SaveImage.saveJPG(s);
 			top.add(png);
 			top.add(jpg);
 			top.add(small);
 			top.add(large);
 		}
 	});
 	small.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 			s.setPreferredSize(new Dimension(800,800));
 			s.update(s.getGraphics());	
 		}
 	});
 	large.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 			s.setPreferredSize(new Dimension((int)(Math.pow(2,numDisks)*175), (int)(Math.pow(2,numDisks)*175)));
 			s.update(s.getGraphics());
 		}
 	});
 	top.add(png);
 	top.add(jpg);
 	top.add(small);
 	top.add(large);
 	s.setPreferredSize(new Dimension(800,800));
 	JScrollPane jsp = new JScrollPane(s, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 	JPanel whole = new JPanel();
 	whole.setLayout(new BorderLayout());
 	whole.add(top,BorderLayout.NORTH);
 	whole.add(jsp,BorderLayout.CENTER);
 	f.add(whole);
 	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	f.setSize(1200,800);
 	f.setVisible(true);
 	
     }
 
 
 
 }
