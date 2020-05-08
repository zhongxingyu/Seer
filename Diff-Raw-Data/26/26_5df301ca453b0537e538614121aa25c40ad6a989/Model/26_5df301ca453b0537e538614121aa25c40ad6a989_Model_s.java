 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class Model {
 	private ArrayList<GameCharacter> d_characters;
 	private ArrayList<Player> d_players;
 	
 	private ArrayList<Block> d_blocks;
 	
 	private Rectangle d_hill;
 	private int d_points;
 	
 	public Model() {
 		d_characters = new ArrayList<GameCharacter>();
 		d_blocks = new ArrayList<Block>();
 		d_players = new ArrayList<Player>();
 		
 		d_hill = new Rectangle(200, 200, 100, 100);
 		
 		
 
 		try {		
 			DataInputStream file = new DataInputStream(new FileInputStream("map.dat"));
 			
 			int numOfBlocks = file.readInt();
 			
 			for(int idx = 0; idx != numOfBlocks; ++idx) {
 				int x = file.readInt();
 				int y = file.readInt();
 				int width = file.readInt();
 				int height = file.readInt();
 				
 				if(width > 0 || height > 0)
 					addBlock(new Block(x, y, width, height));
 			}
 			
 			file.close();
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		}
 	}
 	
 	public void save() {
 		try {		
 			DataOutputStream file = new DataOutputStream(new FileOutputStream("map.dat"));
 			
 			file.writeInt(d_blocks.size());
 			
 			for(Block block : d_blocks) {
 				file.writeInt(block.x());
 				file.writeInt(block.y());
 				file.writeInt(block.width());
 				file.writeInt(block.height());
 			}
 			
 			file.close();
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		}
 	}
 	
 	public void addGameCharacter(GameCharacter character) {
 		d_characters.add(character);
 	}
 	
 	public void addPlayer(Player player) {
 		synchronized(this) {
 			d_players.add(player);
 		}
 	}
 		
 	public void addBlock(Block block) {
 		d_blocks.add(block);
 	}
 	
 	public void step() {
 		boolean hillCaptured = false;
 		
 		for(Player player : d_players) {
 			if(d_hill.contains(player.x(), player.y()))
 				hillCaptured = true;
 		}
 		
 		for(GameCharacter character : d_characters) {
 			character.step();
 			if(d_hill.contains(character.x(), character.y()))
 				hillCaptured = false;
 		}
 		
 		if(hillCaptured)
 			d_points += 1;
 		
 		int numOfCharacters = d_players.size() + d_characters.size();
 		ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + numOfCharacters * GameCharacter.sendSize());
 		buffer.putInt(Message.CHARACTERS.value());
 		buffer.putInt(numOfCharacters);
 		
 		for(Player player : d_players)
 			player.addToBuffer(buffer);
 		
 		for(GameCharacter character : d_characters)
 			character.addToBuffer(buffer);
 		
 		System.out.println(4 + 4 + numOfCharacters * GameCharacter.sendSize());
 				
 		SpectroPolaris.server().send(buffer.array());
 	}
 	
 	//private static long time = 0;
 	
 	public void draw(Graphics2D g2d) {
 		//System.out.println(System.nanoTime() - time);
 		//time = System.nanoTime();
 		
 		g2d.setColor(Color.YELLOW);
 		g2d.fill(d_hill);
 		
 		for(GameCharacter character : d_characters)
 			character.draw(g2d);
 		
 		synchronized(this) {
 			
 			for(Player player : d_players)
 				player.draw(g2d);
 		}
 		
 		for(Block block : d_blocks)
 			block.draw(g2d);
 		
 		
 		g2d.fillRect(800, 0, 224, 768);
 		
 		for(int index = 0; index != d_players.size(); ++index)
 			d_players.get(index).drawUI(g2d, index);
 		
 		g2d.setColor(Color.WHITE);
 		g2d.drawString("Points: " + d_points, 805, 730);
 		g2d.drawString("Connect to: " + SpectroPolaris.server().ip(), 805, 750);
 	}
 
 	public void removeGameCharacter(GameCharacter character) {
 		d_characters.remove(character);
 	}
 
 	public void removeBlock(int x, int y) {
 		Block remove = null;
 		for(Block block : d_blocks) {
 			if(block.pointCollision(x, y))
 				remove = block;
 		}
 		d_blocks.remove(remove);
 	}
 
 	public void removePlayer(Player player) {
 		synchronized(this) {
 			d_players.remove(player);
 		}
 		
 	}
 
 
 }
