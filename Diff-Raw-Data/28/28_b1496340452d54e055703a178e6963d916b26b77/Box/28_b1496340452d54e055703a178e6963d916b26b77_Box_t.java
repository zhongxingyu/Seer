 package com.freedom.gameObjects;
 
import java.awt.Graphics;
 import java.awt.Image;
import java.io.File;
import java.io.IOException;
 import java.util.Scanner;
 
import javax.imageio.ImageIO;

 public class Box extends Stuff {
 	
 
 	public Box()
 	{
 		super(true, false,0);
	
		try {
			texture = ImageIO.read(new File("Resource/Textures/BoxBlack.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
 	
	public void draw(Graphics g) {
		g.drawImage(texture, (int)(getX()*getSize()), (int)(getY()*getSize()), getSize(), getSize(), null);
	}
	
 	private String color; 
 	private Image texture;
 }
