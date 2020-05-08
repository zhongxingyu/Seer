 package com.freedom.gameObjects;
 
 import java.awt.Image;
 import java.util.Scanner;
 
 public class Box extends Stuff {
 	
 
 	public Box()
 	{
 		super(true, false,0);
		//texture = ImageIO...
 	}
 
 	/**
 	 * Метод, который считывает всю инфу из файла с лвлами
 	 * 
 	 * @param - Scanner файла
 	 */
 	public void readLvlFile(Scanner sc) {
 		this.x = sc.nextInt(); // В данном случае считывает x y и color
 		this.y = sc.nextInt();
 		this.color = sc.next();
 
 	}
 	
 	public String getColour() {
 		return this.color;
 	}
 	
 	private String color; 
 	private Image texture;
 }
