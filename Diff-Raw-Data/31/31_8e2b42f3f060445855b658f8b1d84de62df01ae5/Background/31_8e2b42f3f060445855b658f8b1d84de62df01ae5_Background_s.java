 package main;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public class Background {
 
 	public enum Planet {
		EARTH(9.81, "Earth"), MARS(3.71, "Mars");
 		private double gravity;
 		private String name;
 		Planet(double gravity, String name) {
 			this.gravity = gravity;
 			this.name = name;
 		}
 		public double getGravity() {
 			return gravity;
 		}
 		public String getName() {
 			return name;
 		}
 	};
 	
 	private BufferedImage image;
 	private Planet planet;
 	
 	public Background() {
 		
 	}
 	
 	public Background(Planet planet) {
 		super();
 		setPlanet(planet);
 	}
 
 	public Image getImage() {
 		return image;
 	}
 	
 	public void setPlanet(Planet planet) {
 		this.planet = planet;
 		if (planet == Planet.EARTH) {
 			try {
				image = ImageIO.read(new File("src/main/resources/earth_view_from_moon.jpg"));
 			} catch (IOException e) {
 				System.out.println("Earth image not found.");
 			}
 		} else if (planet == Planet.MARS) {
 			try {
 				image = ImageIO.read(new File("src/main/resources/mars_view.jpg"));
 			} catch (IOException e) {
 				System.out.println("Mars image not found.");
 			}
 		}
 	}
 
 	public Planet getPlanet() {
 		return planet;
 	}
 }
