 package uk.ac.cam.cl.dtg.android.time.data;
 
 public class TransportDataException extends Exception {
 
   private static final long serialVersionUID = 1L;
 
   public TransportDataException(String message) {
 		super(message);
 	}
 	public TransportDataException(Throwable t) {
 	  super(t);
 	}
  public TransportDataException(String message, Exception e) {
    super(message,e);
  }
 
 }
