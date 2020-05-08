 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.geom.Path2D;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 
 
 public class LensDemoCanvas extends JPanel {
 
 	private static final long serialVersionUID = 892163485801692577L;
 	static final int SCALE=10;
 	static int dotR=2;
 	static int textOffSet=15;
 	static double lensFactor=0.075;
 	static Color lightBlue=new Color(0.0f,1.0f,1.0f,1.0f);
 	static Color ligthGray=new Color(1.0f,1.0f,1.0f,0.5f);
 	private double f=7.5,u=15,v=15;
 	private double objHeight=7.5,objWidth=2.0;
 	private int ctrX,ctrY,maxY,maxX;
 	private BufferedImage imgRO,imgVO,imgRI,imgVI;
 	private LensDemoApplication parentDemo;
 	//image of real object, virtual object, real image and virtual image.
 	
 	static Stroke thinDashed=new BasicStroke(1.0f,BasicStroke.CAP_ROUND,
             BasicStroke.JOIN_ROUND,1.0f, new float[]{5.0f}, 7.5f);
 	static Stroke thinSolid =new BasicStroke(1.0f,BasicStroke.CAP_ROUND,
             BasicStroke.JOIN_ROUND,1.0f);
 	static Stroke dashed    =new BasicStroke(1.5f,BasicStroke.CAP_ROUND,
             BasicStroke.JOIN_ROUND,1.0f, new float[]{10.0f}, 0.0f);
 	static Stroke thinGrid  =new BasicStroke(0.0f,BasicStroke.CAP_ROUND,
             BasicStroke.JOIN_ROUND,1.0f);
 	
 	LensDemoCanvas(){
 		addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				System.out.println("drag"+e.getPoint());
 				if(parentDemo!=null){
					double tmp=(double)(ctrX-e.getX())/SCALE;
 					double heightTmp=(double)(ctrY-e.getY())/SCALE;
					parentDemo.setValueExternal(f, tmp, v, heightTmp, 
 							LensDemoApplication.SolveFor.V);
 					//repaint();
 				}
 				
 			}
 		});
 		URL urlRO = LensDemoCanvas.class.getResource("imgRO.png");
 		URL urlVO = LensDemoCanvas.class.getResource("imgVO.png");
 		URL urlRI = LensDemoCanvas.class.getResource("imgRI.png");
 		URL urlVI = LensDemoCanvas.class.getResource("imgVI.png");
 		try {
 			imgRO=ImageIO.read(urlRO);
 			imgVO=ImageIO.read(urlVO);
 			imgRI=ImageIO.read(urlRI);
 			imgVI=ImageIO.read(urlVI);
 			objWidth=objHeight*imgRO.getWidth()/imgRO.getHeight();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(this, "Unable to load necessary images.",
 					"Image loading failure",JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void setObjectHeight(double objectHeight){
 		objHeight=objectHeight;
 		objWidth=objHeight*imgRO.getWidth()/imgRO.getHeight();
 	}
 	
 	private void drawGrid(Graphics2D g){
 		Stroke tmpStroke=g.getStroke();
 		g.setStroke(thinGrid);
 		g.setColor(Color.gray);
 		for(int ix=ctrX%SCALE; ix<maxX;ix+=SCALE){
 			g.drawLine(ix,0,ix,maxY);
 		}
 		for(int iy=ctrY%SCALE; iy<maxX;iy+=SCALE){
 			g.drawLine(0,iy,maxX,iy);
 		}
 		g.setStroke(tmpStroke);
 	}
 	
 	private void drawLens(Graphics2D g){
 		Path2D lens=new Path2D.Double();
 		double control=lensFactor*maxY;
 		if(f>0){
 			lens.moveTo(ctrX,0);
 			lens.quadTo(ctrX+control, ctrY, ctrX, maxY);
 			lens.quadTo(ctrX-control, ctrY, ctrX, 0);
 		}
 		else{
 			lens.moveTo(ctrX+control,0);
 			lens.quadTo(ctrX, ctrY, ctrX+control, maxY);
 			lens.lineTo(ctrX-control, maxY);
 			lens.quadTo(ctrX, ctrY, ctrX-control, 0);
 		}
 		lens.closePath();
 		GradientPaint lensPaint=new GradientPaint(ctrX,0,Color.white,ctrX,ctrY,lightBlue,true);
 		g.setPaint(lensPaint);
 		g.fill(lens);
 		g.setColor(lightBlue);
 		g.draw(lens);
 		//draw the converging/diverging lens.
 		
 		Stroke tmpStroke=g.getStroke();
 		g.setStroke(dashed);
 		g.setColor(Color.black);
 		g.drawLine(0, ctrY, maxX, ctrY);
 		g.setStroke(tmpStroke);
 		g.fillOval((int) (ctrX+SCALE*f-dotR), ctrY-dotR, dotR*2, dotR*2);
 		g.drawString("F", (int) (ctrX+SCALE*f), ctrY+textOffSet);
 		g.fillOval((int) (ctrX+SCALE*f*2-dotR), ctrY-dotR, dotR*2, dotR*2);
 		g.drawString("2F", (int) (ctrX+SCALE*f*2), ctrY+textOffSet);
 		g.fillOval((int) (ctrX-SCALE*f-dotR), ctrY-dotR, dotR*2, dotR*2);
 		g.drawString("F'", (int) (ctrX-SCALE*f), ctrY+textOffSet);
 		g.fillOval((int) (ctrX-SCALE*f*2-dotR), ctrY-dotR, dotR*2, dotR*2);
 		g.drawString("2F'", (int) (ctrX-SCALE*f*2), ctrY+textOffSet);
 		g.fillOval(ctrX-dotR, ctrY-dotR, dotR*2, dotR*2);
 		g.drawString("O", ctrX, ctrY+textOffSet);
 		g.setStroke(tmpStroke);
 		//drawing the principle axis and label special dots.
 		
 		
 	}
 	private void drawObject(Graphics2D g){
 		g.setColor(Color.black);
 		int width=(int) (objWidth*SCALE);
 		int height=(int) (objHeight*SCALE);
 		BufferedImage img;
 		if(u>0){
 			img=imgRO;
 		}else{
 			img=imgVO;
 		}
 		g.drawImage(img,(int) (ctrX-SCALE*u-width/2),ctrY-height,width,height,null);
 		g.drawString("Obj", (int) (ctrX-SCALE*u),ctrY+textOffSet*2);
 		g.fillOval((int) (ctrX-SCALE*u-dotR/2.0), ctrY-dotR, dotR*2, dotR*2);
 	}
 	
 	private void drawImage(Graphics2D g){
 		g.setColor(Color.black);
 		int width=(int) (objWidth*SCALE*(-v/u));
 		int height=(int) (objHeight*SCALE*(-v/u));
 		BufferedImage img;
 		if(v>0){
 			img=imgRI;
 		}else{
 			img=imgVI;
 		}
 		g.drawImage(img,(int) (ctrX+SCALE*v-width/2),ctrY-height,width,height,null);
 		if(v/u>0){
 			g.drawString("Img", (int) (ctrX+SCALE*v),ctrY-textOffSet);
 		}else{
 			g.drawString("Img", (int) (ctrX+SCALE*v),ctrY+textOffSet);
 		}
 		g.fillOval((int) (ctrX+SCALE*v-dotR/2.0), ctrY-dotR, dotR*2, dotR*2);
 	}
 	
 	private void drawRayCenter(Graphics2D g){
 		double uX=SCALE*u,uY=objHeight*SCALE;
 		double slope=uY/uX;
 		Stroke tmpStroke=g.getStroke();
 		g.setStroke(thinSolid);
 		if(u>0){
 			g.drawLine((int)(ctrX-uX), (int)(ctrY-uY),ctrX,ctrY);
 			g.drawLine(ctrX, ctrY,  maxX, (int) (ctrY+(maxX-ctrX)*slope));
 			g.setStroke(thinDashed);
 			g.drawLine(0, (int) (ctrY-ctrX*slope),(int)(ctrX-uX), (int)(ctrY-uY));
 		}else{
 			g.drawLine(0, (int) (ctrY-ctrX*slope), ctrX, ctrY);
 			g.drawLine(ctrX, ctrY, (int)(ctrX-uX), (int)(ctrY-uY));
 			g.drawLine((int)(ctrX-uX), (int)(ctrY-uY),maxX, (int) (ctrY+(maxX-ctrX)*slope));
 		}
 		g.setStroke(tmpStroke);
 	}
 	private void drawRayParallel(Graphics2D g){
 		double uX=SCALE*u,uY=objHeight*SCALE;
 		double fX=SCALE*f;
 		Stroke tmpStroke=g.getStroke();
 		g.setStroke(thinSolid);
 		if(u>0){
 			g.drawLine((int)(ctrX-uX), (int)(ctrY-uY), ctrX, (int)(ctrY-uY));
 		}else{
 			g.drawLine(0, (int)(ctrY-uY), ctrX, (int)(ctrY-uY));
 			g.setStroke(thinDashed);
 			g.drawLine(ctrX, (int)(ctrY-uY),(int)(ctrX-uX), (int)(ctrY-uY));
 		}
 		if(f>0){
 			g.setStroke(thinSolid);
 			g.drawLine(ctrX, (int)(ctrY-uY), (int)(ctrX+fX), ctrY);
 			double endY=ctrY+(maxX-ctrX-fX)*uY/fX;
 			g.drawLine((int)(ctrX+fX), ctrY , maxX, (int)endY);
 			endY=ctrY-uY-(ctrX)*uY/fX;
 			g.setStroke(thinDashed);
 			g.drawLine(0, (int) endY, ctrX,(int)(ctrY-uY));
 		}else{
 			g.setStroke(thinDashed);
 			g.drawLine((int)(ctrX+fX), ctrY, ctrX, (int)(ctrY-uY));
 			double endY=ctrY-uY-(ctrX)*uY/fX;
 			g.drawLine(0, (int) endY, (int)(ctrX+fX), ctrY);
 			g.setStroke(thinSolid);
 			endY=ctrY+(maxX-ctrX-fX)*uY/fX;
 			g.drawLine(ctrX, (int)(ctrY-uY), maxX, (int)endY);
 		}
 		g.setStroke(tmpStroke);
 	}
 	
 	private void drawRayFocus(Graphics2D g){
 		if(u/f<=1 && u/f>=0){return;}
 		double uX=SCALE*u,uY=objHeight*SCALE;
 		double fX=SCALE*f;
 		Stroke tmpStroke=g.getStroke();
 		double slope=uY/(uX-fX);
 		double offSetY=ctrY+slope*uX-uY;
 		g.setStroke(thinSolid);
 		if(u>0 && f>0){
 			g.drawLine((int)(ctrX-uX), (int)(ctrY-uY), (int)(ctrX-fX), ctrY);
 			g.drawLine((int)(ctrX-fX), ctrY, ctrX, (int) offSetY);
 		}else if(u<0 && f>0){
 			g.drawLine(0,(int)(ctrY-(ctrX-fX)*slope),(int)(ctrX-fX),ctrY);
 			g.drawLine((int)(ctrX-fX), ctrY, ctrX, (int) offSetY);
 			g.setStroke(thinDashed);
 			g.drawLine(ctrX, (int)offSetY,(int)(ctrX-uX),(int)(ctrY-uY));
 		}else if(u>0 && f<0){
 			g.drawLine((int)(ctrX-uX),(int)(ctrY-uY),ctrX,(int)offSetY);
 			g.setStroke(thinDashed);
 			g.drawLine(ctrX,(int)offSetY ,(int)(ctrX-fX),ctrY);
 		}else if(u<0 && f<0){
 			g.drawLine(0,(int)(ctrY-(ctrX-fX)*slope),ctrX, (int) offSetY);
 			g.setStroke(thinDashed);
 			g.drawLine(ctrX, (int) offSetY, (int)(ctrX-fX), ctrY);
 			g.drawLine((int)(ctrX-fX), ctrY, (int)(ctrX-uX),(int)(ctrY-uY));
 		}
 		g.setStroke(thinDashed);
 		g.drawLine(0,(int)offSetY,ctrX,(int)offSetY);
 		g.setStroke(thinSolid);
 		g.drawLine(ctrX,(int)offSetY,maxX,(int)offSetY);
 		g.setStroke(tmpStroke);
 	}
 	
 	protected void paintComponent(Graphics g){
 		super.paintComponent(g);
 		
 		Graphics2D g2d=(Graphics2D) g;
 		maxY=getHeight();
 		maxX=getWidth();
 		ctrX=maxX/2;
 		ctrY=maxY/2;
 		//g.clearRect(0, 0, maxX, maxY);
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
 		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		//remember to turn ANTIALIASING on, or the lens will look ugly....
 		
 		drawLens(g2d);
 		drawGrid(g2d);
 		drawObject(g2d);
 		drawImage(g2d);
 		g2d.setColor(Color.red);
 		drawRayCenter(g2d);
 		g2d.setColor(Color.green);
 		drawRayParallel(g2d);
 		g2d.setColor(Color.blue);
 		drawRayFocus(g2d);
 		System.out.println("paintComponent called");
 
 	}
 		
 	public void draw(double f,double u,double v){
 		System.out.println("draw called f: "+f+" u: "+u+" v: "+v);
		if(f==0 || Double.isInfinite(f) || Double.isNaN(f)){
			getGraphics().drawString( "f is zero/infinite/NaN, which is not able to draw", 50,50);
			System.out.println("Zero/infinite/NaN f encountered inside LensDemoCanvas. Rejected.");
			JOptionPane.showMessageDialog(this.getParent(), "f cannot be equal to zero/infinite/NaN!", 
					"Invalid Focal Length",JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(Double.isInfinite(u) || Double.isNaN(u)){
			getGraphics().drawString( "u is infinite/NaN, which is not able to draw", 50,50);
			JOptionPane.showMessageDialog(this.getParent(), "u is infinite/NaN, which is not able to draw", 
					"Unable to draw diagram due to out-of-range value.",JOptionPane.WARNING_MESSAGE);
			return;
		}
 		this.f=f;
 		this.u=u;
 		this.v=v;
 		repaint();
 	}
 	
 	public void setParentDemo(LensDemoApplication parentDemo) {
 		this.parentDemo = parentDemo;
 	}
 
 }
