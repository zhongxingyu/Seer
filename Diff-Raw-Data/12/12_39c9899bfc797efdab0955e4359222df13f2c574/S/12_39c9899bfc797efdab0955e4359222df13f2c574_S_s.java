 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 
 import java.awt.BasicStroke;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JPanel;
 import java.awt.Color;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 
 public class S extends JPanel{
 
 
     private int width=1200;
     private int height = 900;
     
 
     int numberOfT=0;
 
     public int getWidth(){ return this.width;}
     public int getHeight(){ return this.height;}
     
     
     
     
     public Ellipse2D centerOfDisk(double x, double y, double width, double height)//code to place from center of ellipse rather than from topleft
     {
         double newX = x - width / 2.0;
         double newY = y - height / 2.0;
 
         Ellipse2D newDisk = new Ellipse2D.Double(newX, newY, width, height);
 
         return newDisk;
     }
     
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
     
     
     public void drawSystem(int depth, ArrayList<Color> colors, String lefttopright, Graphics g, double tx, double ty, double sideLength){
 	if(depth!=0){
 	    return;
 	}
 	
 	Graphics2D g2d = (Graphics2D) g;
 	Font font = new Font("Arial", Font.BOLD, 14);
 	g2d.setFont(font);
 	
 	double x = sideLength * Math.cos(Math.PI/3);
 	double y = sideLength * Math.sin(Math.PI/3);
 	
 
 	
 	//int i =0;
 	double tW = sL;
 	double tH = 1.5*sL/4;
 	double eH = Math.min(tH/(n+1),20);
 	double tcX = tx;
 	double tcY = ty;
 	double lcX = tx - x;
 	double lcY = ty+y*1.1;
 	double rcX = tx + x;
 	double rcY = ty+y*1.1;
 	
 	/*
 	  for(int r = 0; r<lefttopright.length();r++){
 	  
 	  if(r==lefttopright.length()-1 && lefttopright.charAt(r)!='T'){
 	  System.out.println("everasdsad= "+lefttopright);
 	  lcY+=y/2;
 	  rcY+=y/2;
 	  break block;
 	  }
 	  else if(lefttopright.charAt(r)=='T')break;
 	  else if(lefttopright.charAt(r)!='T'){
 	  System.out.println("everytime= "+lefttopright);
 	  //break;
 	  }
 	  
 	  }
 	*/
 	/* save for lefttopright.length() == 1
 	   if(lefttopright.charAt(q)=='L'){
 	   System.out.println("Any L's= " + lefttopright);
 	   lcX -= 1.5*x;
 	   tcX -= 1.5*x;
 	   }
 	   else if(lefttopright.charAt(q)=='R'){
 	rcX += 1.5*x;
 	tcX += 1.5*x;
     }
 	
 	   else if(lefttopright.charAt(q)=='T' && q==0){
 	lcX -= .75*x;
 	rcX += .75*x;
     }
 	   else if(lefttopright.charAt(q)=='T' && q!=0){
 	lcX -= .75*x;
 	rcX += .75*x;
 	
 	if(lefttopright.charAt(q-1)=='L'){
 	lcX -=1.5*x;
     }
 	else if(lefttopright.charAt(q-1)=='R'){
 	rcX +=1.5*x;
     }
 	
     }
 	*/
	
 	block:{
 	    
 	    //for(int q = 0; q<lefttopright.length();q++){
 	    
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
 	
	
 	
 	
 	drawTowers(tcX,tcY,sideLength,g);
 	drawTowers(rcX,rcY,sideLength,g);
 	drawTowers(lcX,lcY,sideLength,g);
 	
 	Point2D.Double[] p = {new Point2D.Double(lcX,lcY),new Point2D.Double(tcX,tcY),new Point2D.Double(rcX,rcY)};
 
 	
 	int[] disks = {1,1,1};
 	String hBase = "0,1,2";
 	for (int i=0;i<lefttopright.length();i++){
 	    double newWidth= tW*(lefttopright.length()-i*.5)/lefttopright.length();
 	    String[] h = hBase.split(",");
 	    switch(lefttopright.charAt(i)){
 	    case 'T': 
 		if (depth==0){
 		    int d = Integer.parseInt(h[1]);
 		    for (int j = 0; j < 3; j++){
 			g2d.setColor(colors.get(n-i));
 			Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/6, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 			g2d.fill(disk2);
 			//g2d.fill(new Ellipse2D.Double(p[j].x-tW/2+d*tW/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			g2d.setColor(Color.BLACK);
 			g2d.drawString(""+(n-i),(float)(p[j].x-tW/3+d*tW/3),(float)(p[j].y-eH*(disks[d]-1)));
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
 			
 			Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/6, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 			g2d.fill(disk2);
 			//g2d.fill(new Ellipse2D.Double(p[j].x-tW/2+d*tW/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			g2d.setColor(Color.BLACK);
 			g2d.drawString(""+(n-i),(float)(p[j].x-tW/3+d*tW/3-tW/12),(float)(p[j].y-eH*(disks[d]-1)));
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
 			Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/8, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 			g2d.fill(disk2);
 			//g2d.fill(new Ellipse2D.Double(p[j].x-tW/2+d*tW/3, p[j].y-eH*disks[d],newWidth/3,eH));
 			g2d.setColor(Color.BLACK);
 			g2d.drawString(""+(n-i),(float)(p[j].x-tW/3+d*tW/3-tW/12),(float)(p[j].y-eH*(disks[d]-1)));
 		    }
 		    disks[d]++;
 		    hBase=""+h[0]+","+h[2]+","+h[1];
 		}
 		break;
 	    }
 
 	}
 	
 	if (depth==0){
 	    
 	    double newWidth= tW/lefttopright.length();
 	    if(lefttopright.length()==1)newWidth=tW/2;
 	    String[] h = hBase.split(",");
 	    for (int j = 0; j < 3; j++){
 		int d = Integer.parseInt(h[j]);
 		g2d.setColor(colors.get(0));
 		//Ellipse2D disk2 = centerOfDisk(p[j].x-tW/2+d*tW/3+tW/4, p[j].y-eH*disks[d]+eH/2,newWidth/3,eH);
 		//g2d.fill(disk2);
 		g2d.fill(new Ellipse2D.Double(p[j].x-tW/2+d*tW/3+tW/12, p[j].y-eH*disks[d],newWidth/3,eH));
 		g2d.setColor(Color.BLACK);
 		g2d.drawString(""+0,(float)(p[j].x-tW/3+d*tW/3-tW/16),(float)(p[j].y-eH*(disks[d]-1)));
 	    }
 	}
 	
 	//placeDisk(set1,g, tower1coordx,tower2coordx,tower3coordx,towercoordy,depth,sideLength);
 	//placeDisk(set2,g, xtower11,xtower21,xtower31,ytower,depth,sideLength);
 	//placeDisk(set3,g, xtower12,xtower22,xtower32,ytower,depth,sideLength);
 	
 	
     }
     
 
     
     
     private int n;
     private ArrayList<Color>colors;
     private double tx, ty;
     private double side, sL;
     //private double width,height;
     private double tS, hS;
     private Font mF, eF;
 
     public S(int n /*, double width, double height*/){
 	this.n=n;
 	/*
 	  this.width=width;
 	  this.height=height;
 	  setSize(width,height);
 	  double min = Math.min(width,height); //wrong got bored
 	*/
 	side=700;
 	sL=side/(Math.pow(2,n+1)-1);
 	colors = new ArrayList<Color>();
 	for (int i=0;i<n+1;i++){
 	    colors.add(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
 	}
 	tx=600.0;
 	ty=100.0;
 
 	tS=5;
 	hS=30;
 	mF=new Font("Arial", Font.PLAIN, 12);
 	eF=mF;
     }
 
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
 	g2.setFont(mF);
 	g2.drawString(move+b[1].charAt(0)+"->"+b[1].charAt(1),(float)(bX+tS),(float)(bY+tS)+(float)sL/15);
 	at.rotate(-Math.PI/3);
 	g2.setFont(mF.deriveFont(at));
 	g2.drawString(move+b[0].charAt(0)+"->"+b[0].charAt(1),(float)(tX-x2-tS),(float)(tY+y2-tS));
 	at.rotate(2*Math.PI/3);
 	g2.setFont(mF.deriveFont(AffineTransform.getRotateInstance(Math.PI/3)));
 	g2.drawString(move+b[2].charAt(0)+"->"+b[2].charAt(1),(float)(tX+x1+tS),(float)(tY+y1+tS));
 
 	drawSystem(depth,colors,order,g,tX,tY,sL);
     }
 
 
     public void drawSerpienski(Graphics g, int depth, double topX, double topY, String order){
 
 	if(depth < 0) return;
 	//drawSystem(depth,colors,order,g,topX,topY,sL);
 	drawMove(g, depth, topX, topY, order);
 	
 	double x = sL * Math.pow(2,depth) * Math.cos(Math.PI/3);
 	double y = sL * Math.pow(2,depth) * Math.sin(Math.PI/3);
 
 	drawSerpienski(g,depth-1, topX, topY, order+"T");
 	drawSerpienski(g,depth-1, topX-x,topY+y, order+"L");
 	drawSerpienski(g,depth-1, topX+x, topY+y, order+"R");
 	//System.out.println(order);
 
     }
 
     public void paintComponent(Graphics g){
 	drawSerpienski(g, n, tx, ty, "");
     }
 
     public static void main(String[] args){
 	System.out.println("start");
 	JFrame f = new JFrame("yo");
 	//f.setContentPane(new Tester(Integer.parseInt(args[0])));
 	//S s = new S(4);
 	S s = new S(Integer.parseInt(args[0]));
 	JScrollPane jsp = new JScrollPane(s,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 	f.add(jsp);
 	//f.setContentPane(new S(4));
 	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	f.setSize(1200,900);
 	f.setVisible(true);
 
     }
 
 
 
 }
