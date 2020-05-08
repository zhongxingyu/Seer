 package stkl.spectropolarisclient;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 public class Model {
 	private ArrayList<GameCharacter> d_characters;
 	private ArrayList<Bullet> d_bullets;
 	private int d_numOfBullets;
 	private ArrayList<Block> d_blocks;
 	private Player d_player;
 	
 	private GameActivity d_context;
 	private float d_scale;
 	
 	private Point d_motionOrigin;
 	private float d_motionControlX;
 	private float d_motionControlY;
 	private Point d_shootOrigin;
 	private float d_shootControlX;
 	private float d_shootControlY;	
 	
	private int d_mapWidth = 1024;
 	private int d_mapHeight = 768;
 	
 	public Model(GameActivity context) {
 		d_player = new Player(10, 10, new Paint(Paint.ANTI_ALIAS_FLAG));
 		d_characters = new ArrayList<GameCharacter>();
 		
 		d_bullets = new ArrayList<Bullet>();
 		d_numOfBullets = 0;
 		
 		d_blocks = new ArrayList<Block>();
 		d_motionOrigin = new Point(-1, -1);
 		d_shootOrigin = new Point(-1, -1);
 		
 		d_context = context;
 		
 		InputStream is = context.getResources().openRawResource(R.raw.map);
 		DataInputStream file = new DataInputStream(is);
 		try {
 		int numOfBlocks = file.readInt();
 		
 		for(int idx = 0; idx != numOfBlocks; ++idx) {
 			int x = file.readInt();
 			int y = file.readInt();
 			int blockWidth = file.readInt();
 			int blockHeight = file.readInt();
 
 			addBlock(new Block(x, y, blockWidth, blockHeight));
 		}
 			
 		file.close();
 		
 		} catch (IOException e) {
 			System.err.println("Error occured reading from file");
 			e.printStackTrace();
 		} 
 		
 		float numOfPixels = 500;
 		float minScreenDimension = Math.min(d_context.centerHorizontal() * 2, d_context.centerVertical() * 2);
 		
 		d_scale = minScreenDimension / numOfPixels;
 	}
 	
 	public void addBlock(Block block) {
 		d_blocks.add(block);
 	}
 	
 	public void setMotionOrigin(float x, float y) {
 		d_motionOrigin.set((int)x, (int)y);
 	}
 
 	public void setShootOrigin(float x, float y) {
 		d_shootOrigin.set((int)x, (int)y);
 	}
 	
 	public void setMotionControls(float controlX, float controlY) {
 		d_motionControlX = controlX;
 		d_motionControlY = controlY;
 	}
 	
 	public void setShootControls(float controlX, float controlY) {
 		d_shootControlX = controlX;
 		d_shootControlY = controlY;
 	}
 	
 	public void addGameCharacter(GameCharacter character) {
 		d_characters.add(character);
 	}
 	
 	public Bullet addBullet() {
 		if(d_numOfBullets == d_bullets.size()) {
 			Bullet newBullet = new Bullet();
 			d_bullets.add(newBullet);
 			++d_numOfBullets;
 			return newBullet;
 		}
 		else // d_numOfBullets < d_bullets.size()
 		{
 			++d_numOfBullets;
 			return d_bullets.get(d_numOfBullets - 1);
 		}
 	}
 	
 
 	
 	public void step() {
 		synchronized(this) {
 			d_player.update(d_motionControlX, d_motionControlY, d_shootControlX, d_shootControlY);
 			d_player.step();
 			
 			for(GameCharacter character : d_characters)
 				character.step();
 		}
 		
 		for(int index = 0; index != d_numOfBullets; ++index) {
 			Bullet bullet = d_bullets.get(index);
 			if(bullet.step())
 			{
 				removeBullet(bullet);
 				--index;
 			}
 		}
 	}
 
 	public void removeBullet(Bullet bullet) {
 		--d_numOfBullets;
 		bullet.instantiate(d_bullets.get(d_numOfBullets));
 		d_bullets.get(d_numOfBullets).destroy();
 	}
 
 	public void draw(Canvas canvas) {
 		canvas.save();
 		
 		canvas.scale(4 * d_scale, 4 * d_scale, d_context.centerHorizontal(), d_context.centerVertical());
 		
 		d_player.draw(canvas, d_context.centerHorizontal(), d_context.centerVertical());
 		
 		canvas.translate(-d_player.xOffset() + d_context.centerHorizontal(),
 				 -d_player.yOffset() + d_context.centerVertical());
 		
 		for(GameCharacter character : d_characters)
 			character.draw(canvas);
 		
 		for(Bullet bullet : d_bullets)
 			bullet.draw(canvas);
 				
 		for(Block block : d_blocks)
 			block.draw(canvas);
 		
 		canvas.restore();
 		
 		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		paint.setColor(Color.CYAN);
 		paint.setTextSize(20);
 		canvas.drawText("Motion Controls: " + d_motionControlX + ", " + d_motionControlY, 10, 20, paint);
 		canvas.drawText("Shoot Controls: " + d_shootControlX + ", " + d_shootControlY, 10, 40, paint);
 		
 		if(!d_motionOrigin.equals(-1, -1)) {
 			canvas.drawCircle(d_motionOrigin.x, d_motionOrigin.y, 5, paint);
 		}
 		if(!d_shootOrigin.equals(-1, -1)) {
 			canvas.drawCircle(d_shootOrigin.x, d_shootOrigin.y, 5, paint);
 		}
 	}
 
 	public boolean collision(float potentialX, float potentialY, int radius) {
 		if(potentialX < radius || potentialX + radius > d_mapWidth 
 				|| potentialY < radius || potentialY + radius > d_mapHeight) {
 			return true;
 		}
 				
 		for(Block block : d_blocks)
 		{
 			if(block.collision(potentialX, potentialY, radius))
 				return true;
 		}	
 		return false;
 	}
 
 	public void receive(ByteBuffer buffer, int numOfCharacters) {
 		// Don't count the Player as a character
 		--numOfCharacters;
 		
 		for(int idx = 0; idx != numOfCharacters; ++idx) {
 			float x = buffer.getFloat();
 			float y = buffer.getFloat();
 			float direction = buffer.getFloat();
 			float speed = buffer.getFloat();
 			int color = buffer.getInt();
 			int id = buffer.getInt();
 			
 			if(id == d_player.id()) {
 				--idx;
 				continue;
 			}
 			
 			if(idx < d_characters.size())
 				d_characters.get(idx).instantiate(x, y, direction, speed, color, id);
 			else {
 				GameCharacter character = new GameCharacter(x, y, direction, speed, color, id);
 				d_characters.add(character);
 			}
 		}
 		
 		if(numOfCharacters < d_characters.size()) {
 			for(int idx = numOfCharacters; idx != d_characters.size(); ++idx)
 				d_characters.get(idx).instantiate(0, 0, 0, 0, 0, -1);
 		}
 	}
 }
