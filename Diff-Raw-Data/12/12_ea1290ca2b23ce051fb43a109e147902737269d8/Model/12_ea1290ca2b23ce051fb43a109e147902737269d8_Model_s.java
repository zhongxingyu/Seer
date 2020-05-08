 package stkl.spectropolarisclient;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.support.v4.app.NotificationCompat.Style;
 
 public class Model {
 	private ArrayList<GameCharacter> d_characters;
 	private ArrayList<Bullet> d_bullets;
 	private int d_numOfBullets;
 	private Player d_player;
 	
 	// tileMap with blocks (true means block, false means noblock)
 	private boolean[][] d_tileMap;
 	private final int d_tileSize = 10;
 	private Paint d_blockPaint;
 	
 	private final int d_mapWidth = 800;
 	private final int d_mapHeight = 768;
 	
 	// pickups
 	private ArrayList<HealthPickup> d_health;
 	private int d_lastNumOfPickups;
 	
 	private GameActivity d_context;
 	private float d_scale;
 	
 	private Point d_motionOrigin;
 	private float d_motionControlX;
 	private float d_motionControlY;
 	private Point d_shootOrigin;
 	private float d_shootControlX;
 	private float d_shootControlY;	
 	
 	private Paint d_borderPaint;
 	
 	public Model(GameActivity context, int color) {
 		Paint playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		playerPaint.setColor(color);
 		d_player = new Player(10, 10, playerPaint);
 		d_characters = new ArrayList<GameCharacter>();
 		
 		d_bullets = new ArrayList<Bullet>();
 		d_numOfBullets = 0;
 		
 		d_tileMap = new boolean[d_mapHeight / d_tileSize][d_mapWidth / d_tileSize];
 		d_blockPaint = new Paint();
 		
 		// pickups
 		d_health = new ArrayList<HealthPickup>();
 		d_lastNumOfPickups = 0;
 		
 		//d_blocks = new ArrayList<Block>();
 		d_motionOrigin = new Point(-1, -1);
 		d_shootOrigin = new Point(-1, -1);
 		
 		d_borderPaint = new Paint();
 		d_borderPaint.setStyle(Paint.Style.STROKE);
 		
 		d_context = context;
 		
 		try {		
 			InputStream is = context.getResources().openRawResource(R.raw.map);
 			DataInputStream file = new DataInputStream(is);
 			
 			//int numOfBlocks = file.readInt();
 			
 			//for(int idx = 0; idx != numOfBlocks; ++idx) {
 			//	int x = file.readInt();
 			//	int y = file.readInt();
 			//	int width = file.readInt();
 			//	int height = file.readInt();
 				
 			//	if(width > 0 && height > 0 && x + width < d_mapWidth && y + height < d_mapHeight)
 			//		addBlock(x, y, width, height);
 			//}
 			
 			for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 				for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 					d_tileMap[y][x] = file.readBoolean();
 				}
 			}
 			
 			file.close();
 			
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			e.printStackTrace();
 		}
 		
 		float numOfPixels = 500;
 		float minScreenDimension = Math.min(d_context.centerHorizontal() * 2, d_context.centerVertical() * 2);
 		
 		d_scale = minScreenDimension / numOfPixels;
 	}
 	
 	public void addBlock(int x, int y, int width, int height) {
 		// add blocks to the effected part of the map
 		for(int yIdx = y / d_tileSize; yIdx != (height + y) / d_tileSize; ++yIdx) {
 			for(int xIdx = x / d_tileSize; xIdx != (width + x) / d_tileSize; ++xIdx)
 				d_tileMap[yIdx][xIdx] = true;
 		}
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
 		synchronized(d_bullets) {
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
 	}
 	
 
 	
 	public void step() {
 		synchronized(d_player) {
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
 		synchronized(d_bullets) {
 			--d_numOfBullets;
 			bullet.instantiate(d_bullets.get(d_numOfBullets));
 			d_bullets.get(d_numOfBullets).destroy();
 		}
 	}
 
 	public void draw(Canvas canvas) {
 		canvas.save();
 		
 		canvas.scale(4 * d_scale, 4 * d_scale, d_context.centerHorizontal(), d_context.centerVertical());
 		
 		
 		
 		canvas.translate(-d_player.xOffset() + d_context.centerHorizontal(),
 				 -d_player.yOffset() + d_context.centerVertical());
 				
 		synchronized(d_health) {
 			for(HealthPickup pickup : d_health)
 				pickup.draw(canvas);
 		}
 		
 		synchronized(d_bullets) {
 			for(Bullet bullet : d_bullets)
 				bullet.draw(canvas);
 		}
 		
 		canvas.translate(d_player.xOffset() - d_context.centerHorizontal(),
 				 d_player.yOffset() - d_context.centerVertical());
 		
 		d_player.draw(canvas, d_context.centerHorizontal(), d_context.centerVertical());
 		
 		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
		paint.setColor(Color.GRAY);
		canvas.drawRect(d_context.centerHorizontal() - 50, 290, d_context.centerHorizontal(), 295, paint);
		paint.setColor(Color.RED);
		canvas.drawRect(d_context.centerHorizontal() - 50, 290, d_context.centerHorizontal() - 50 + d_player.health(), 295, paint);
 		
 		
 		canvas.translate(-d_player.xOffset() + d_context.centerHorizontal(),
 				 -d_player.yOffset() + d_context.centerVertical());
 		
 		for(GameCharacter character : d_characters)
 			character.draw(canvas);
 		
 		for(int y = 0; y != d_mapHeight / d_tileSize; ++y) {
 			for(int x = 0; x != d_mapWidth / d_tileSize; ++x) {
 				if(d_tileMap[y][x] == true)
 					canvas.drawRect(x * d_tileSize, y * d_tileSize, 
 							x * d_tileSize + d_tileSize, y * d_tileSize + d_tileSize, 
 							d_blockPaint);
 			}
 		}
 		
 		canvas.drawRect(0, 0, d_mapWidth, d_mapHeight, d_borderPaint);
 		
 		canvas.translate(d_player.xOffset() - d_context.centerHorizontal(),
 				 d_player.yOffset() - d_context.centerVertical());
 		
 		
 		paint.setColor(Color.CYAN);
 		//paint.setTextSize(20);
 		//canvas.drawText("Motion Controls: " + d_motionControlX + ", " + d_motionControlY, 10, 20, paint);
 		//canvas.drawText("Shoot Controls: " + d_shootControlX + ", " + d_shootControlY, 10, 40, paint);
 		
 		canvas.restore();
 		
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
 				
 		int startX = ((int) potentialX - radius) / d_tileSize;
 		int startY = ((int) potentialY - radius) / d_tileSize;
 		int endX = 1 + ((int) potentialX + radius) / d_tileSize;
 		int endY = 1 + ((int) potentialY + radius) / d_tileSize;
 		
 		for(int yIdx = startY; yIdx != endY; ++yIdx) {
 			for(int xIdx = startX; xIdx != endX; ++xIdx) {
 				if(d_tileMap[yIdx][xIdx] == true)
 					return true;
 			}
 		}
 		
 		
 		//for(Block block : d_blocks)
 		//{
 			//if(block.collision(potentialX, potentialY, radius))
 			//	return true;
 		//}	
 		return false;
 	}
 	
 	public Point collision(float x1, float y1, float x2, float y2) {
 		//Point point = null;
 		//for(Block block : d_blocks)
 		//{
 		//	point = block.collision(x1, y1, x2, y2);
 		//	if(point != null)
 		//		return point;
 		//}	
 		
 		/*
 		private float lineXsideCollision(float x1, float y1, float x2, float y2, float x) {
 			return y1 + (y2 - y1) * ((x - x1) / (x2 - x1));
 		}
 		
 		private float lineYsideCollision(float x1, float y1, float x2, float y2, float y) {
 			return x1 + (x2 - x1) * ((y - y1) / (y2 - y1));
 		}
 		
 		public Point collision(float x1, float y1, float x2, float y2) {
 			float minX = Math.min(x1, x2);
 			float maxX = Math.max(x1, x2);
 			float minY = Math.min(y1, y2);
 			float maxY = Math.min(y1, y2);
 			
 			if(minX < rect.left && maxX >= rect.left) {		//does it cross left edge?
 				float intersectionY = lineXsideCollision(x1, y1, x2, y2, rect.left);
 				if(intersectionY >= rect.top && intersectionY <= rect.bottom)
 					return new Point(rect.left, (int) intersectionY);
 			}
 			if(minX > rect.right && maxX <= rect.right) {//does it cross right edge?
 				float intersectionY = lineXsideCollision(x1, y1, x2, y2, rect.right);
 				if(intersectionY >= rect.top && intersectionY <= rect.bottom)
 					return new Point(rect.right, (int) intersectionY);
 			}
 			
 			if(minY < rect.top && maxY >= rect.top) {		//does it cross top edge?
 				float intersectionX = lineYsideCollision(x1, y1, x2, y2, rect.top);
 				if(intersectionX >= rect.left && intersectionX <= rect.right)
 					return new Point((int) intersectionX, rect.top);
 			}
 			if(minY > rect.bottom && maxY <= rect.bottom) {//does it cross bottom edge?
 				float intersectionX = lineYsideCollision(x1, y1, x2, y2, rect.bottom);
 				if(intersectionX >= rect.left && intersectionX <= rect.right)
 					return new Point((int) intersectionX, rect.bottom);
 			}
 			
 			return null;
 		}
 		*/
 		return null;
 	}
 
 	public void receive(ByteBuffer buffer, int numOfCharacters, int numOfBullets, int numOfPickups) {
 		if(d_player.id() == -1)
 			return;
 				
 		synchronized(d_player) {
 		
 			for(int idx = 0; idx != numOfCharacters; ++idx) {
 				float x = buffer.getFloat();
 				float y = buffer.getFloat();
 				float direction = buffer.getFloat();
 				float speed = buffer.getFloat();
 				int color = buffer.getInt();
 				int id = buffer.getInt();
 				
 				if(id == d_player.id()) {
 					--idx;
 					--numOfCharacters;
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
 		
 		synchronized(d_health) {
 			
 			// Check if a pickup has been picked up
 			if(numOfPickups < d_lastNumOfPickups) {
 				synchronized(d_player) {
 					d_player.addHealth();
 				}
 			}
 			
 			d_lastNumOfPickups = numOfPickups;
 			
 			for(int idx = 0; idx != numOfPickups; ++idx) {
 				int x = buffer.getInt();
 				int y = buffer.getInt();
 				
 				if(idx < d_health.size())
 					d_health.get(idx).instantiate(x, y);
 				else {
 					System.err.println("Alloc");
 					HealthPickup pickup = new HealthPickup(x, y);
 					d_health.add(pickup);
 				}
 			}
 			
 			if(numOfPickups < d_health.size()) {
 				for(int idx = numOfPickups; idx != d_health.size(); ++idx)
 					d_health.get(idx).instantiate(-1, -1);
 			}
 		}
 	}
 
 	public Player player() {
 		
 		return d_player;
 	}
 }
