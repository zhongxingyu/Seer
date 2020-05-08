 package ar.com.ktulu.editorHuesos.ui.images;
 
 
 public class ImageException extends Exception {
 
 	private static final long serialVersionUID = 5315206931164996184L;
 
 	public ImageException(Exception e) {
		super("Formato de imagen no reconocido. Solo se permiten png y jpg (en rgb)", e);
 	}
 
 }
