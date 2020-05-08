 package com.github.desmaster.Devio.gfx.userinterface.userinterfaces;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.Color;
 import org.lwjgl.util.Rectangle;
 
 import com.github.desmaster.Devio.gfx.Screen;
 import com.github.desmaster.Devio.gfx.userinterface.UserInterface;
 
 public class Interface_Console extends UserInterface {
 
 	public Interface_Console() {
 		super("Console");
		container = new Rectangle(0, 0, Display.getWidth(), (Display.getWidth() / 2) - 150);
 		//color = new Color(255, 255, 255, 255);
 	}
 
 	public void tick() {
 		if (Screen.getInput().grave.clicked) {
 			active = !active;
 		}
 	}
 
 }
