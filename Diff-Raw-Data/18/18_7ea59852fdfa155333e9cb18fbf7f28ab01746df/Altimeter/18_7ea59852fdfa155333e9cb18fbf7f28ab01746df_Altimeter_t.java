 package lab2;
 
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * @author moorea
  * @version 1.0
  * @created 05-Dec-2012 9:54:32 AM
  */
 public class Altimeter implements Observer, Displayable {
 
     private GPSCoordinate lastCoordinate;
     private GPSCoordinate  currentCoordinate;
     private Observable subject;
 
	public Altimeter(Observable subject) {
 	 this.subject = subject;
	 subject.addObserver(this);
 	}
 
 	@Override
 	public void display() {
	 System.out.println("==Altimeter Display==");	
          System.out.println("Current Altitude: " + currentCoordinate.getElevation());
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
         if(o instanceof GpsSubject){
          	GpsSubject subject = (GpsSubject) o;
          	lastCoordinate = currentCoordinate;
          	currentCoordinate = subject.getCoordinates();
          	display();
           }
 	}
 
 }
