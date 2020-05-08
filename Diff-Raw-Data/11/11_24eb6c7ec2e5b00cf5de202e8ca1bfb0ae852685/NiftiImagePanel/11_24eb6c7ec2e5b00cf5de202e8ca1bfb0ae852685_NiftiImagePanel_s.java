 package display.containers.viewer;
 
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.process.ByteProcessor;
 import ij.process.FloatProcessor;
 import ij.process.ImageProcessor;
 import ij.process.ShortProcessor;
 import ij.process.StackConverter;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 
 import javax.swing.*;
 
 import daemon.tools.nifti.Nifti_Reader;
 
 import java.io.*;
  
  
 /**
  * Display image (ImagePlus) in a panel
  * 
  * ATTTENTION : utiliser getMricronCoord pour repasser dans l'espace de 
  * coordonnee MRICRON like
  * @author Jrmy DEVERDUN
  *
  */
 public class NiftiImagePanel extends JPanel implements ComponentListener, MouseWheelListener, MouseListener, MouseMotionListener {
 	public static enum Plan{AXIAL, SAGITTAL, CORONAL}; // les differents plan possible
 	public static final int AXIAL = 1;
 	
 	private ImagePlus niftiImage = null;
 	private ImagePlus overlayImage = null;
 	private float alphaOverlay; // transparence de l'overlay
 	private BufferedImage image = null; // on stock l'image courante
 	private BufferedImage overlay; // on stock l'image de l'overlay courant (slice courante)
 	private ViewerPanel viewer = null;
 	private boolean isMousePressed = false;
 	private int slice = 1;
 	private Plan orientation; // vue associe a ce panel (axial sagital etc)
 	private Dimension imageDim; // dimension de l'ImagePlus (dim 2D)
 	private Point currentLocation; // dernier point clique dans referentiel panel
 	private Point imageCurrentLocation; // dernier point clique dans referentiel image
 	private boolean showCrosshair = true; // si on montre le crosshair ou pas
 	private boolean keepRatio = true;//false; // si on contraint l'affichage pourver le ratio original
 	private int zoom = 1;//zoom (multiplicateur)
 	private Point zoomlocation; // coordonnees du zoom
 	private double displayScaleFactor = 1d;
 	private Dimension offsets; // quand on est en mode keepRatio les offsets
 	private double[] coefficients; // coefficient y = a + bx (calibration)
 	private double[] overlayCoefficients; // coefficient y = a + bx (calibration) pour l'overlay
 
 
 
 	
     /**
      * @wbp.parser.constructor
      */
     public NiftiImagePanel(ViewerPanel v, ImagePlus nr, Plan plan) {
     	this.setOrientation(plan);
         this.setNiftiImage(nr); 
         setViewer(v);
         init();
         
     }
     
     /**
      * Plan par defaut : axial
      * @param v
      * @param nr
      */
     public NiftiImagePanel(ViewerPanel v, ImagePlus nr) {
     	this.setOrientation(Plan.AXIAL);
         this.setNiftiImage(nr); 
         setViewer(v);
         init();
     }
     
     public NiftiImagePanel(ViewerPanel v,Plan plan) {
     	this.setOrientation(plan);
     	setViewer(v);
     	init();
     }
     
     public NiftiImagePanel(ViewerPanel v) {
     	this.setOrientation(Plan.AXIAL);
     	setViewer(v);
     	init();
 
     }
    
     public void init(){
     	alphaOverlay = 0.5f;
     	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
     	setBackground(Color.BLACK);
        	isMousePressed = false;
        	setZoom(1);
        	keepRatio = true;
        	displayScaleFactor = 1d;
        	offsets = new Dimension(0,0);
        	imageCurrentLocation = null;
     }
     public ImagePlus getNiftiImage() {
 		return niftiImage;
 	}
 
 	public ImagePlus getOverlayImage() {
 		return overlayImage;
 	}
 
 	public void setNiftiImage(ImagePlus niftiImage) {
 		this.niftiImage = niftiImage;
 		setSlice(Math.round(this.niftiImage.getNSlices()/2));
 		imageCurrentLocation = new Point((int)this.niftiImage.getWidth()/2,(int)this.niftiImage.getHeight()/2);
 		imageDim = new Dimension(this.niftiImage.getWidth(), this.niftiImage.getHeight());
 		coefficients = niftiImage.getCalibration().getCoefficients();
 		if(coefficients == null)
 			coefficients = new double[]{0.0,1.0};
 		offsets = new Dimension(0,0);
 		currentLocation = imageXYtoPanelXY(imageCurrentLocation);
 		overlayImage = null;
 		addMouseMotionListener(this);
        	addMouseListener(this);
        	addMouseWheelListener(this);
        	addComponentListener(this);
 	}
 
 	public void setOverlayImage(ImagePlus overlay){
 		this.overlayImage = overlay;
 		this.overlayImage.setSlice(getSlice());
 		this.overlay = this.overlayImage.getBufferedImage();
 		overlayCoefficients = this.overlayImage.getCalibration().getCoefficients();
 		if(overlayCoefficients == null)
 			overlayCoefficients = new double[]{0.0,1.0};
 	}
 	
 	public int getSlice() {
 		return slice;
 	}
 
 	public void setSlice(int slice) {
 		this.slice = slice;
 		if(niftiImage!=null){
 			niftiImage.setSlice(this.slice);
 			image = niftiImage.getBufferedImage();
 			if(overlayImage!=null){
 				overlayImage.setSlice(this.slice);
 				overlay = overlayImage.getBufferedImage();
 			}
 			
 			repaint();
 		}
 	}
 
 	public float getAlphaOverlay() {
 		return alphaOverlay;
 	}
 
 	public void setAlphaOverlay(float alphaOverlay) {
 		this.alphaOverlay = alphaOverlay;
 	}
 
 	/**
 	 * Refresh l'image affiche avec les nouveaux parametres de Min/Max
 	 */
 	public void refreshImage(){
 		image = niftiImage.getBufferedImage();
 		repaint();
 	}
 	
 	/**
 	 * Refresh overlay affiche avec les nouveaux parametres de Min/Max
 	 */
 	public void refreshOverlay(){
 		if(overlayImage!=null){
 			overlay = overlayImage.getBufferedImage();
 			repaint();
 		}
 	}
 	public ViewerPanel getViewer() {
 		return viewer;
 	}
 
 	public void setViewer(ViewerPanel viewer) {
 		this.viewer = viewer;
 	}
 
 	public Plan getOrientation() {
 		return orientation;
 	}
 
 	public void setOrientation(Plan orientation) {
 		this.orientation = orientation;
 	}
 
 	public int getZoom() {
 		return zoom;
 	}
 
 	public void setZoom(int zoom) {
 		this.zoom = zoom;
 	}
 
 	public double[] getCoefficients() {
 		return coefficients;
 	}
 
 	public void setCoefficients(double[] coefficients) {
 		this.coefficients = coefficients;
 	}
 
 	public double[] getOverlayCoefficients() {
 		return overlayCoefficients;
 	}
 
 	public void setOverlayCoefficients(double[] overlayCoefficients) {
 		this.overlayCoefficients = overlayCoefficients;
 	}
 
 	public boolean isKeepRatio() {
 		return keepRatio;
 	}
 
 	public boolean isShowCrosshair() {
 		return showCrosshair;
 	}
 
 	public void setShowCrosshair(boolean showCrosshair) {
 		this.showCrosshair = showCrosshair;
 	}
 
 	public void setKeepRatio(boolean keepRatio) {
 		this.keepRatio = keepRatio;
 		if(!this.keepRatio)
 			offsets = new Dimension(0,0);
 	}
 
 	/**
      * 
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g); //paint background
         Graphics2D g2d = (Graphics2D) g.create();
         if (niftiImage != null) { //there is a picture: draw it
             int height = this.getSize().height;
             int width = this.getSize().width;      
             if(keepRatio){
             	int scaleWidth;
                 int scaleHeight;
             	if(height>image.getHeight() && width>image.getWidth()){
             		displayScaleFactor = Math.max(1d, getScaleFactorToFit(new Dimension(image.getWidth(), image.getHeight()),getSize()));
             		displayScaleFactor *= zoom;// on gere le zoom
             		scaleWidth = (int) Math.round(image.getWidth() * displayScaleFactor);
                     scaleHeight = (int) Math.round(image.getHeight() * displayScaleFactor);
             	}else{
             		displayScaleFactor = Math.min(1d, getScaleFactorToFit(new Dimension(image.getWidth(), image.getHeight()), getSize()));
             		displayScaleFactor *= zoom;// on gere le zoom
             		scaleWidth = (int) Math.round(image.getWidth() * displayScaleFactor);
                     scaleHeight = (int) Math.round(image.getHeight() * displayScaleFactor);
             	}
                 //Image scaled = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
             	
                 int widthloc = getWidth() - 1;
                 int heightloc = getHeight() - 1;
 
                 int x = (widthloc - scaleWidth) / 2;
                 int y = (heightloc - scaleHeight) / 2;
                 offsets.setSize(x, y);
 
                 g2d.drawImage(image,x,y, scaleWidth, scaleHeight, this);
 
 
                 if(overlayImage!=null){
                     int rule = AlphaComposite.SRC_OVER;
                     Composite comp = AlphaComposite.getInstance(rule , alphaOverlay );
 					g2d.setComposite(comp);
 					g2d.drawImage(overlay, x,y, scaleWidth, scaleHeight, this);
                 }
             }else{
             	g2d.drawImage(image,0,0, width*zoom, height*zoom, this);
             	if(overlayImage!=null){
                     int rule = AlphaComposite.SRC_OVER;
                     Composite comp = AlphaComposite.getInstance(rule , alphaOverlay );
 					g2d.setComposite(comp);
 					g2d.drawImage(overlay, 0,0, width*zoom, height*zoom, this);
                 }
             }
             if(showCrosshair && currentLocation!=null){
             	g2d.setColor(new Color(255,140,0));
             	int rule = AlphaComposite.SRC_OVER;
                 Composite comp = AlphaComposite.getInstance(rule , 1.0f );
 				g2d.setComposite(comp);
             	if(orientation != Plan.SAGITTAL)
             		g2d.drawString("L", offsets.width+10, getHeight()-offsets.height-20);
             	else
             		g2d.drawString("P", getWidth()-offsets.width-20, getHeight()-offsets.height-20);
             	//System.out.println(orientation+"@@"+imageCurrentLocation.x+"-"+imageCurrentLocation.y+"-"+getSlice());
             	g2d.drawLine((int)Math.round(currentLocation.getX()), 0, (int) Math.round(currentLocation.getX()), getHeight());
             	g2d.drawLine(0, (int) Math.round(currentLocation.getY()), getWidth(), (int) Math.round(currentLocation.getY()));
             }
         }
     }
 
     /**
      * Recupere la valeur du pixel p pour la slice courrante
      * quand on est pas en AXIAL les y sont inverse attention
      * @param p
      */
     public double getValueAt(Point p){
     	switch(niftiImage.getBitDepth()){
     	case 8:
     		ByteProcessor bp = (ByteProcessor) niftiImage.getProcessor();
     		if(orientation != Plan.AXIAL)
     			return coefficients[0]+coefficients[1]*(double)bp.getf((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
     		else
     			return coefficients[0]+coefficients[1]*(double)bp.getf((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
     	case 16:
     		ShortProcessor sp = (ShortProcessor) niftiImage.getProcessor();
     		if(orientation != Plan.AXIAL)
     			return coefficients[0]+coefficients[1]*(double)sp.getf((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
     		else
     			return coefficients[0]+coefficients[1]*(double)sp.getf((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
     	case 32:
     		FloatProcessor fp = (FloatProcessor) niftiImage.getProcessor();
     		if(orientation != Plan.AXIAL)
    			return coefficients[0]+coefficients[1]*(double)fp.getf((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
    		else
     			return coefficients[0]+coefficients[1]*(double)fp.getf((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
     	default:
     		return -1;
     	}
     	
     }
     
     /**
      * Recupere la valeur du pixel p pour la slice courrante pour l'overlay
      * quand on est pas en AXIAL les y sont inverse attention
      * @param p
      */
     public double getOverlayValueAt(Point p){
     	switch(overlayImage.getBitDepth()){
     	case 8:
     		ByteProcessor bp = (ByteProcessor) overlayImage.getProcessor();
     		if(orientation != Plan.AXIAL)
     			return overlayCoefficients[0]+overlayCoefficients[1]*(double)bp.getf((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
     		else
     			return overlayCoefficients[0]+overlayCoefficients[1]*(double)bp.getf((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
     	case 16:
     		ShortProcessor sp = (ShortProcessor) overlayImage.getProcessor();
     		if(orientation != Plan.AXIAL)
     			return overlayCoefficients[0]+overlayCoefficients[1]*(double)sp.getf((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
     		else
     			return overlayCoefficients[0]+overlayCoefficients[1]*(double)sp.getf((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
     	case 32:
     		FloatProcessor fp = (FloatProcessor) overlayImage.getProcessor();
     		if(orientation != Plan.AXIAL)
    			return overlayCoefficients[0]+overlayCoefficients[1]*(double)fp.getf((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
    		else
     			return overlayCoefficients[0]+overlayCoefficients[1]*(double)fp.getf((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
     	default:
     		return -1;
     	}
     	
     }
 	public void mouseDragged(MouseEvent e) {
 		if(isMousePressed){
 			currentLocation = e.getPoint();
 			Point p = panelXYtoImageXY(e.getPoint());
 			switch(this.orientation){
 			case AXIAL:
 				getViewer().setXY(p);
 				break;
 			case SAGITTAL:
 				getViewer().setYZ(p);
 				break;
 			case CORONAL:
 				getViewer().setXZ(p);
 				break;
 			}
 			imageCurrentLocation = p;
 			repaint();
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		
 	}
 
 
 
 	public void mouseClicked(MouseEvent e) {
 		if(e.getButton()==MouseEvent.BUTTON1){
 			
 			currentLocation = e.getPoint();
 			Point p = panelXYtoImageXY(e.getPoint());
 			switch(this.orientation){
 			case AXIAL:
 				getViewer().setXY(p);
 				break;
 			case SAGITTAL:
 				getViewer().setYZ(p);
 				break;
 			case CORONAL:
 				getViewer().setXZ(p);
 				break;
 			}
 			imageCurrentLocation = p;
 			repaint();
 			
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mousePressed(MouseEvent e) {
 		if(e.getButton() == MouseEvent.BUTTON1){
 			isMousePressed = true;
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if(e.getButton() == MouseEvent.BUTTON1)
 			isMousePressed = false;
 	}
 	
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL){
 			zoom = Math.max(1, zoom - e.getWheelRotation());
 			if(zoom>2)//zoom max x2
 				zoom = 2;
 			if(zoom == 1)
 				zoomlocation = null;
 			else
 				zoomlocation = e.getPoint();
 			repaint();
 		}
 	}
 	/**
 	 * convertie une coordonnee dans le referentiel panel 
 	 * au referentiel ImagePlus
 	 * @param point
 	 * @return
 	 */
 	private Point panelXYtoImageXY(Point point) {
 		Point impt = null;
 		int pwidth = this.getWidth() - 1;
 		int pheight = this.getHeight() - 1;
 		float sizeOneImPixelY = ((float)pheight - ((float)offsets.height*2))/ (float)imageDim.height;
 		float sizeOneImPixelX = ((float)pwidth -((float)offsets.width*2)) / (float)imageDim.width;
 		if(orientation != Plan.AXIAL){
 			impt = new Point((int)Math.round((point.getX()-offsets.width+sizeOneImPixelX/2)/sizeOneImPixelX),(int)Math.round(imageDim.height-(point.getY()-offsets.height)/sizeOneImPixelY));
 		}else{
 			impt = new Point((int)Math.round((point.getX()-offsets.width+sizeOneImPixelX/2)/sizeOneImPixelX),(int)Math.round((point.getY()-offsets.height)/sizeOneImPixelY));
 		}
 		if(impt.getX()>imageDim.getWidth())
 			impt.x = (int)imageDim.getWidth();
 		if(impt.getX()<=0)
 			impt.x = 1;
 		if(impt.getY()>imageDim.getHeight()){
 			impt.y = (int)imageDim.getHeight();
 		}
 		if(impt.getY()<=0){
 			impt.y = 1;
 		}
 		return impt;
 	}
 
 	/**
 	 * convertie une coordonnee dans le referentiel image 
 	 * au referentiel panel
 	 * @param point
 	 * @return
 	 */
 	private Point imageXYtoPanelXY(Point p) {
 		Point impt = null;
 		int pwidth = this.getWidth()-1;
 		int pheight = this.getHeight()-1;
 		float sizeOneImPixelY = ((float)pheight - ((float)offsets.height*2)) / (float)imageDim.height;
 		float sizeOneImPixelX = ((float)pwidth - ((float)offsets.width*2)) / (float)imageDim.width;
 		if(orientation != Plan.AXIAL)
 			impt = new Point((int)Math.round(p.getX()*sizeOneImPixelX+offsets.width-sizeOneImPixelX/2),(int)Math.round(pheight-p.getY()*sizeOneImPixelY-offsets.height+sizeOneImPixelY/2));
 		else
 			impt = new Point((int)Math.round(p.getX()*sizeOneImPixelX+offsets.width-sizeOneImPixelX/2),(int)Math.round(p.getY()*sizeOneImPixelY+offsets.height-sizeOneImPixelY/2));//impt = new Point((int)Math.round(x-(widthvoxsize/2)),(int)Math.round(y+(heightvoxsize/2)));
 		return impt;
 	}
 	
 	/**
 	 * Met a jours le crosshair 
 	 * a partir de coordonnees dans l'espace image
 	 * @param coord
 	 */
 	public void updateCrosshair(int[] coord) {
 		Point p = null;
 		switch(this.orientation){
 		case AXIAL:
 			p = new Point(coord[0],coord[1]);
 			break;
 		case SAGITTAL:
 			p = new Point(coord[1],coord[2]);
 			break;
 		case CORONAL:
 			p = new Point(coord[0],coord[2]);
 			break;
 		}
 		imageCurrentLocation = p;
 		currentLocation = imageXYtoPanelXY(p);
 		repaint();
 	}
 
 	/**
 	 * Remet le panel a 0 
 	 */
 	public void reset() {
 		niftiImage = null;
 		currentLocation = null;
 		image = null;
 		imageCurrentLocation = null;
 		removeMouseMotionListener(this);
 		removeMouseListener(this);
 		removeMouseWheelListener(this);
 		removeComponentListener(this);
 		repaint();
 	}
 	
 	/**
 	 * Permet de repasser dans l'espace de coordonnee mricron (pt 0,0,0 en bas a gauche dans toute les vue)
 	 * @return
 	 */
 	public Integer[] getMricronCoord(){
 		switch(orientation){
 		case AXIAL:
 			return new Integer[]{(int) imageCurrentLocation.getX(),(int) (imageDim.height - imageCurrentLocation.getY()+1),getSlice()};
 		case CORONAL:
 			return new Integer[]{(int) imageCurrentLocation.getX(),(int) imageCurrentLocation.getY(),niftiImage.getNSlices()-getSlice()+1};
 		case SAGITTAL:
 			return new Integer[]{(int) ( imageDim.width - imageCurrentLocation.getX()+1),(int) (imageCurrentLocation.getY()),getSlice()};
 		default:
 			return null;
 		}
 	}
 	
 	/**
 	 * Permet de passer de l'espace mricron a l'espace local image
 	 * @return
 	 */
 	public Integer[] mricronCoordToLocal(Integer[] mric){
 		switch(orientation){
 		case AXIAL:
 			return new Integer[]{mric[0],(int) Math.round(imageDim.height - mric[1]+1),mric[2]};
 		case CORONAL:
 			return new Integer[]{mric[0],mric[1],niftiImage.getNSlices()-mric[2]+1};
 		case SAGITTAL:
 			return new Integer[]{(int) ( imageDim.width - mric[0]+1),mric[1],mric[2]};
 		default:
 			return null;
 		}
 	}
 	/**
 	 * Recupere le facteur de scaling
 	 * @param iMasterSize
 	 * @param iTargetSize
 	 * @return
 	 */
 	public double getScaleFactor(int iMasterSize, int iTargetSize) {
 
 	    double dScale = 1;
 	    if (iMasterSize > iTargetSize) {
 
 	        dScale = (double) iTargetSize / (double) iMasterSize;
 
 	    } else {
 
 	        dScale = (double) iTargetSize / (double) iMasterSize;
 
 	    }
 
 	    return dScale;
 
 	}
 	
 	/**
 	 * Recupere le facteur de scaling en fonction de la taille du panel
 	 * et de l'image
 	 * @param original
 	 * @param toFit
 	 * @return
 	 */
 	public double getScaleFactorToFit(Dimension original, Dimension toFit) {
 
 	    double dScale = 1d;
 
 	    if (original != null && toFit != null) {
 
 	        double dScaleWidth = getScaleFactor(original.width, toFit.width);
 	        double dScaleHeight = getScaleFactor(original.height, toFit.height);
 
 	        dScale = Math.min(dScaleHeight, dScaleWidth);
 
 	    }
 
 	    return dScale;
 
 	}
 
 	public void componentHidden(ComponentEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void componentMoved(ComponentEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void componentResized(ComponentEvent arg0) {
 		updateCrosshair(getViewer().getCoord());
 	}
 
 	public void componentShown(ComponentEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public double getVoxelValue() {
 		return getValueAt(imageCurrentLocation);
 	}
 	
 	public double getVoxelOverlayValue() {
 		return getOverlayValueAt(imageCurrentLocation);
 	}
 
 	public void resetOverlay() {
 		overlayImage = null;
 		overlay = null;
 		repaint();
 	}
 
 
 
 }
