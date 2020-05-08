 
 public class CarOwner {
 	public static void main(String[] args) {
 		Cars myCars = new Cars();
 		myCars.start();
 		myCars.stop();
 		myCars.drive (40);
 		myCars.drive (25);

        JamesBondCar jbCar = new JamesBondCar();
        jbCar.start();
        jbCar.stop();
        jbCar.drive (40);
        jbCar.drive (25);

 	}
 
 }
