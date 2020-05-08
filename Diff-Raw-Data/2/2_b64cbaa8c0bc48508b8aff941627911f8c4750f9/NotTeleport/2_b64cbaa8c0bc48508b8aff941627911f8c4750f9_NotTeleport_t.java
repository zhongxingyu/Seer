 package com.freedom.gameObjects.controlled;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import org.w3c.dom.Element;
 
 import com.freedom.gameObjects.base.Stuff;
 import com.freedom.model.GameField;
 
 public class NotTeleport extends Stuff {
 
 	// TODO подумать на тему того, стоит ли запретить телепортацию объектов,
 	// если не позволяет сила
 	public NotTeleport()
 	{
 		super(false, true, false, false, 0, 1);
 		textureRed = texturesOn[1];
 		textureGreen = texturesOn[2];
 		textureBlue = texturesOn[3];
 
 	}
 
 	@Override
 	public void readLvlFile(Element obj) {
 		super.readLvlFile(obj);
 		// System.out.println("Teleport" +this.getColour());
 		this.xLeadTo = Integer.parseInt(obj.getAttribute("xLeadTo"));
 		this.yLeadTo = Integer.parseInt(obj.getAttribute("yLeadTo"));
 	}
 
 	@Override
 	public void loadToFile(Element obj) {
 		super.loadToFile(obj);
 		obj.setAttribute("xLeadTo", String.valueOf((int) this.xLeadTo));
 		obj.setAttribute("yLeadTo", String.valueOf((int) this.yLeadTo));
		obj.setAttribute("class", "com.freedom.gameObjects.controlled.NotTeleport");
 	}
 
 	@Override
 	public void touch(Stuff element) {
 		if (!on)
 			return;
 		if (!GameField.getInstance().getCells()[this.xLeadTo][this.yLeadTo]
 				.getIfPassable())
 			return;
 
 		for (Stuff containedElement : element.container) {
 			if (containedElement != null) {
 				containedElement.x = xLeadTo;
 				containedElement.y = yLeadTo;
 			}
 		}
 		if (GameField.getInstance().getCells()[this.xLeadTo][this.yLeadTo]
 				.add(element)) {
 			GameField.getInstance().getCells()[this.getX()][this.getY()]
 					.deleteStuff();
 			return;
 		}
 	}
 
 	@Override
 	public boolean useOn() {
 		if (this.on) {
 			textureRed = texturesOff[1];
 			textureGreen = texturesOff[2];
 			textureBlue = texturesOff[3];
 			this.on = false;
 			return true;
 		}
 		return false;
 
 	}
 
 	@Override
 	public boolean useOff() {
 		if (!this.on) {
 			textureRed = texturesOn[1];
 			textureGreen = texturesOn[2];
 			textureBlue = texturesOn[3];
 			this.on = true;
 			return true;
 		} else
 			return false;
 	}
 
 	@Override
 	public boolean ifCoolEnough(Stuff element) {
 		if (!super.ifCoolEnough(element)
 				|| this.getColour().equals(element.getColour()))
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void giveInfo() {
 		GameField.getInstance().getCells()[(int) x][(int) y].highlight();
 		GameField.getInstance().getCells()[xLeadTo][yLeadTo].highlight();
 	}
 
 	@Override
 	public void removeInfo() {
 		GameField.getInstance().getCells()[(int) x][(int) y].unhighlight();
 		GameField.getInstance().getCells()[xLeadTo][yLeadTo].unhighlight();
 	}
 
 	private static Image texturesOn[] = new Image[4];
 	private static Image texturesOff[] = new Image[4];
 
 	private int xLeadTo;
 	private int yLeadTo;
 
 	private boolean on=true;
 
 	protected int getXLeadTo() {
 		return xLeadTo;
 	}
 
 	protected int getYLeadTo() {
 		return yLeadTo;
 	}
 
 	private static Logger logger = Logger.getLogger("Teleport");
 	static {
 		logger.setLevel(Level.WARNING);
 		try {
 			for (int i = 1; i <= 3; i++) {
 				texturesOn[4-i] = ImageIO
 						.read(new File("Resource/Textures/Teleport/On" + i
 								+ ".png")).getScaledInstance(getSize(),
 								getSize(), BufferedImage.SCALE_SMOOTH);
 
 				texturesOff[4-i] = ImageIO.read(
 						new File("Resource/Textures/Teleport/Off" + i
 								+ ".png")).getScaledInstance(getSize(),
 						getSize(), BufferedImage.SCALE_SMOOTH);
 			}
 		} catch (IOException e) {
 			logger.warning("Teleport texture was corrupted or deleted");
 		}
 	}
 
 }
