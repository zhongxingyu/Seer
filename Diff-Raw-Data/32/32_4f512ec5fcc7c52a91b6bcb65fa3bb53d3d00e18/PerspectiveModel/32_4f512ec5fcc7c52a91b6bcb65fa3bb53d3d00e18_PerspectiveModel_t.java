 package modele;
 
 import java.awt.image.BufferedImage;
 
 import core.Memento;
 
 public class PerspectiveModel extends Subject implements java.io.Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static PerspectiveModel instance;
 	private int positionX = 0;
 	private int positionY = 0;
	private int height = -1;
	private int width = -1;
 	private int zoomFactor =0;
 	
 	public static PerspectiveModel getInstance(){
 		if (instance == null)
 			instance = new PerspectiveModel();
 		return instance;
 	}
 	
 	private PerspectiveModel(){
 		positionX = Image.getInstance().getPosX();
 		positionY = Image.getInstance().getPosY();
 		height = Image.getInstance().getHeigth();
 		width = Image.getInstance().getWidth();
 	}
 	
 	public int getPosX(){
 		return positionX;
 	}
 	public int getPosY(){
 		return positionY;
 	}
 	public int getHeigth(){
 		if(height == -1)
 			height = Image.getInstance().getHeigth();
 		return height;
 	}
 	public int getWidth(){
 		if(width == -1)
 			width = Image.getInstance().getWidth();
 		return width;
 	}
 	public int getZoomFactor(){
 		return zoomFactor;
 	}
 	
 	public BufferedImage getImg(){
 		
 		BufferedImage img = null;
 		try{
 			img = Image.getInstance().getImg();
 		}catch(Exception except){
 			except.getMessage();
 		}
 		return img;
 	}
 	
 	public void translation(int posX, int posY){
 		positionX = posX;
 		positionY = posY;
 		notifyObservers();
 	}
 	public void zoom(int factor){
 		height += height/100 * factor;
 		width += width/100* factor;
 		zoomFactor += factor;
 		notifyObservers();
 	}
 	
 	public void percentZoom(int percent){
 		height = Image.getInstance().getHeigth() * percent/100;
 		width  = Image.getInstance().getWidth() * percent/100;
 		zoomFactor = percent;
 		notifyObservers();
 	}
 	public void undo(){}
 	
	public void reset(){
		height = Image.getInstance().getHeigth();
		width  = Image.getInstance().getWidth();
		positionX = Image.getInstance().getPosX();
		positionY = Image.getInstance().getPosY();
	}
	
 	/*
 	 * Mthode setProperties(PerspectiveModel)
 	 * Mthode servant  copier les proprits
 	 * d'un objet Perspective model  un autre.
 	 */
 	public void setProperties(PerspectiveModel perspec){
 		instance.height = perspec.getHeigth();
 		instance.width = perspec.getWidth();
 		instance.positionX = perspec.getPosX();
 		instance.positionY = perspec.getPosY();
 		notifyObservers();
 	}
 	
 	public Memento getMemento(){
 		return new Memento(positionX, positionY, height, width, zoomFactor);
 	}
 	
 	public void setMemento(Memento memento){
 		positionX = memento.getPositionX();
 		positionY = memento.getPositionY();
 		height = memento.getHeight();
 		width = memento.getWidth();
 		zoomFactor = memento.getZoom();
 		notifyObservers();
 	}
 }
