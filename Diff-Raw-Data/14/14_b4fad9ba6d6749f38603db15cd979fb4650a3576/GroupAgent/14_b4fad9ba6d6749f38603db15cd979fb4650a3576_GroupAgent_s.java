 package game.agent;
 
 import game.Game;
 import game.base.Base;
 import game.player.Player;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.vecmath.Vector2f;
 
 import window.AppliWindow;
 
 //import javax.swing.JLabel;
 
 public class GroupAgent extends Agent {
 
 	private int nbAgent;
 	private Base baseOrigin;
 	private Base baseDestination;
 	private Vector2f vectorDirector;
 	//private BufferedImage image;
 	
 	
 
 	public GroupAgent(int nbInitialAgent, Base source, Base destination) throws IOException{
 		super(true, 10, 10, 15, 15, new Vector2f(source.getPositionCenter().x, source.getPositionCenter().y), null);
 		this.nbAgent = nbInitialAgent;
 		this.baseOrigin = source;
 		this.baseDestination = destination;
 		this.position = new Vector2f(source.getPositionCenter().x, source.getPositionCenter().y);
 		this.speed = 1;
 		this.computeVectorDirector();
 
 		this.setBackground(source.getPlayer().getColor());
 
 		Dimension dim = new Dimension(this.nbAgent, this.nbAgent);
 		this.setSize(dim);
 		this.setPreferredSize(dim);
 		this.setMinimumSize(dim);
 		this.setMaximumSize(dim);
 		
 		/*
 		try {
 			this.image = ImageIO.read(new File("design/groupe.jpeg"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 		*/	
 	}
 
 	public Base getBaseOrigin() {
 		return baseOrigin;
 	}
 
 	public void setBaseOrigin(Base baseOrigin) {
 		this.baseOrigin = baseOrigin;
 	}
 
 	public Base getBaseDestination() {
 		return baseDestination;
 	}
 
 	public void setBaseDestination(Base baseDestination) {
 		this.baseDestination = baseDestination;
 	}
 
 	public Vector2f getVectorDirector() {
 		return vectorDirector;
 	}
 
 	public void setVectorDirector(Vector2f vectorDirector) {
 		this.vectorDirector = vectorDirector;
 	}
 
 	public void setNbAgent(int nbAgent) {
 		this.nbAgent = nbAgent;
 	}
 
 	/**
 	 * Compute the Vector director, i.e the good direction agent to go
 	 */
 	private void computeVectorDirector() {
 		this.vectorDirector = new Vector2f();
 		this.vectorDirector.x = this.baseDestination.getPositionCenter().x - baseOrigin.getPositionCenter().x;
 		this.vectorDirector.y = baseDestination.getPositionCenter().y - baseOrigin.getPositionCenter().y;
 		this.vectorDirector.normalize();
 	}
 
 	public int getNbAgent() {
 		return this.nbAgent;
 	}
 
 	/*public boolean AuthorizedMove(){
 		position = getPosition();
 		map[x][y] = -1;
 		if(position = map[x][y]) setMoving();
 		return true;
 	}*/
 
 	/*
 	public void moveOneStep(){
 		float x = this.position.x + this.vectorDirector.x * this.speed;
 		float y = this.position.y += this.vectorDirector.y * this.speed;
 		this.setPosition(x, y);
 	}
 	 */
 
 	public void moveOneStep(){
 
		//int radiusBase = baseDestination.getMight();
		int sizeBase = AppliWindow.getTilesSize() / 2;
		int distX  = 0, distY = 0;

		distX = (int) Math.min(Math.abs(this.position.x - baseDestination.getPosition().x), Math.abs(this.position.x - baseDestination.getPosition().x - sizeBase));
		distY = (int) Math.min(Math.abs(this.position.y - baseDestination.getPosition().y), Math.abs(this.position.y - baseDestination.getPosition().y - sizeBase));
 		
		//if(distX > radiusBase && distY > radiusBase) {
		if(distX > 0 && distY > 0) {
 			float x = this.position.x + this.vectorDirector.x * this.speed;
 			float y = this.position.y += this.vectorDirector.y * this.speed;
 			this.setPosition(x, y);
 		}
 		else{
 			this.setVisible(false);
 			return;
 		}
 	}
 
 }	
 
