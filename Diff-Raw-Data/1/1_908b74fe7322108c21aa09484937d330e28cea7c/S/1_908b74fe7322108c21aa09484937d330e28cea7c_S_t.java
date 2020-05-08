 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class S extends JPanel{
 
 	private int n;
 	private ArrayList<Color>colors;
 	private double tx, ty;
 	private double side, sL;
 	private double width,height;
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
 		side=900;
 		sL=side/(Math.pow(2,n+1)-1);
 		colors = new ArrayList<Color>();
 		for (int i=0;i<n+1;i++){
 			colors.add(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
 		}
 		tx=600.0;
 		ty=50.0;
 
 		tS=5;
 		hS=30;
 		mF=new Font("Arial", Font.PLAIN, 12);
 		eF=mF;
 	}
 
 	public void drawMove(Graphics g, int depth, double tX, double tY, String order){	
 		//order stuff
 		
 		Graphics2D g2 = (Graphics2D) g.create();
 
 		double eH = 10;
 		double tW = sL;
 		double tH = 2*sL/3;
 
 		int[] disks = {1,1,1};
 		
 		String mBase = "01,02,12";
 		String hBase = "0,1,2";
 		
 		double dx = sL * Math.cos(Math.PI/3);
 		double dy = sL * Math.sin(Math.PI/3);
 		double tcX = tX;
 		double tcY = tY-hS;
 		double lcX = tX - dx - hS-tW/2;
 		double lcY = tY+dy;
 		double rcX = tX + dx + hS + tW/2;
 		double rcY = tY+dy;
 		
 		Point2D.Double[] p = {new Point2D.Double(lcX,lcY),new Point2D.Double(tcX,tcY),new Point2D.Double(rcX,rcY)};
 
 		if(depth==0){
 
 			for (int j = 0; j < 3; j++){
 				g2.draw(new Line2D.Double(p[j].x-tW/2,p[j].y,p[j].x+tW/2,p[j].y));
 				g2.draw(new Line2D.Double(p[j].x-tW/3,p[j].y,p[j].x-tW/3,p[j].y-tH));
 				g2.draw(new Line2D.Double(p[j].x,p[j].y,p[j].x,p[j].y-tH));
 				g2.draw(new Line2D.Double(p[j].x+tW/3,p[j].y,p[j].x+tW/3,p[j].y-tH));	
 			
 			}
 
 		}
 

 		for (int i=0;i<order.length();i++){
 			String[] b = mBase.split(",");
 			String[] h = hBase.split(",");
 			switch(order.charAt(i)){
 				case 'T': 
 					mBase=""+b[2].charAt(1)+b[2].charAt(0)+","+b[1].charAt(1)+b[1].charAt(0)+","+b[0].charAt(1)+b[0].charAt(0);
 					if (depth==0){
 						
 						g2.setColor(colors.get(n-i));
 						for (int j = 0; j < 3; j++){
 							g2.fill(new Ellipse2D.Double(p[j].x-tW/2+(Integer.parseInt(h[1]))*tW/3, p[j].y-eH*disks[1],tW/3,eH));
 						}
 						disks[1]++;
 						hBase = ""+h[2]+","+h[1]+","+h[0];
 					}
 
 					break;
 				case 'R':
 					mBase=""+b[0].charAt(1)+b[0].charAt(0)+","+b[2]+","+b[1];
 
 					if (depth==0){
 					
 						g2.setColor(colors.get(n-i));
 						for (int j = 0; j < 3; j++){
 							g2.fill(new Ellipse2D.Double(p[j].x-tW/2+(Integer.parseInt(h[2]))*tW/3, p[j].y-eH*disks[2],tW/3,eH));
 						}
 						disks[2]++;	
 						hBase= ""+h[1]+","+h[0]+","+h[2];
 					}
 					break;
 				case 'L':
 					mBase=""+b[1]+","+b[0]+","+b[2].charAt(1)+b[2].charAt(0);
 
 
 					if(depth==0){
 					
 						g2.setColor(colors.get(n-i));
 						for (int j = 0; j < 3; j++){
 							g2.fill(new Ellipse2D.Double(p[j].x-tW/2+(Integer.parseInt(h[0]))*tW/3, p[j].y-eH*disks[0],tW/3,eH));
 						}
 						disks[0]++;
 						hBase=""+h[0]+","+h[2]+","+h[1];
 					}
 					break;
 			}
 
 		}
 		if (depth==0){
 			String[] h = hBase.split(",");
 			g2.setColor(colors.get(0));
 			for (int j = 0; j < 3; j++){
 				g2.fill(new Ellipse2D.Double(p[j].x-tW/2+(Integer.parseInt(h[j]))*tW/3, p[j].y-eH*disks[j],tW/3,eH));
 			}						
 		//	g2.fill(new Ellipse2D.Double(tX-sL/2+(Integer.parseInt(h[1]))*sL/3, tY-eH*(n+1),sL/3,eH));
 		//	g2.fill(new Ellipse2D.Double(tX-dx-sL/2+(Integer.parseInt(h[0]))*sL/3, tY+dy-eH*(n+1),sL/3,eH));
 		//	g2.fill(new Ellipse2D.Double(tX+dx-sL/2+(Integer.parseInt(h[2]))*sL/3, tY+dy-eH*(n+1),sL/3,eH));
 			
 		}
 		String move = "move "+depth+": ";
 		String[]  b = mBase.split(",");
 
 		double x1 = sL * (Math.pow(2,depth)-1) * Math.cos(Math.PI/3);
 		double y1 = sL * (Math.pow(2,depth)-1) * Math.sin(Math.PI/3);
 		double x2 = sL * (Math.pow(2,depth)) * Math.cos(Math.PI/3);
 		double y2 = sL * (Math.pow(2,depth)) * Math.sin(Math.PI/3);
 
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
 		g2.drawString(move+b[1].charAt(0)+"->"+b[1].charAt(1),(float)(bX+tS),(float)(bY+tS));
 		at.rotate(-Math.PI/3);
 		g2.setFont(mF.deriveFont(at));
 		g2.drawString(move+b[0].charAt(0)+"->"+b[0].charAt(1),(float)(tX-x2-tS),(float)(tY+y2-tS));
 		at.rotate(2*Math.PI/3);
 		g2.setFont(mF.deriveFont(AffineTransform.getRotateInstance(Math.PI/3)));
 		g2.drawString(move+b[2].charAt(0)+"->"+b[2].charAt(1),(float)(tX+x1+tS),(float)(tY+y1+tS));
 
 		g2.dispose();
 	}	
 
 	public void drawSerpienski(Graphics g, int depth, double topX, double topY, String order){
 
 		if(depth < 0) return;
 
 		drawMove(g, depth, topX, topY, order);
 
 		double x = sL * Math.pow(2,depth) * Math.cos(Math.PI/3);
 		double y = sL * Math.pow(2,depth) * Math.sin(Math.PI/3);
 
 		drawSerpienski(g,depth-1, topX, topY, order+"T");
 		drawSerpienski(g,depth-1, topX-x,topY+y, order+"L");
 		drawSerpienski(g,depth-1, topX+x, topY+y, order+"R");
 
 	}
 
 	public void paintComponent(Graphics g){
 		drawSerpienski(g, n, tx, ty, "");	
 	}
 
 	public static void main(String[] args){
 
 		JFrame f = new JFrame("yo");
 		S s = new S(Integer.parseInt(args[0]));
 		JScrollPane jsp = new JScrollPane(s,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		f.add(jsp);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setSize(3000,3000);
 		f.setVisible(true);
 
 	}
 
 
 
 
 }
