import java.awt.Color;

import car.BuilderCarDsl;
import car.Car;
 
 public class Main {
	public static BuilderCarDsl build() {
		return new BuilderCarDsl();
	}

	public static void main(String[] args) {

		Car c = new BuilderCarDsl().thermal().withTank(12).color(Color.BLACK);
		System.out.println(c);
		c = new BuilderCarDsl().electrical().withBattery(50).color(Color.BLUE);
		System.out.println(c);

	}
 }
