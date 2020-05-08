 package projekt;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.tiled.TiledMap;
 
 
 public class OOWorld extends BasicGame{
 	private TiledMap tileMap;
 	private Animation up, down, left, right;
 	private Player player;
 	private Boss entity;
 	private int SIZE = 32;
 	private int WIDTH = 800;
 	private int HEIGHT = 600;
 	private boolean[][] blocked;
 	private Vector2d playerNextPos;
 	private ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
 	
 	public OOWorld(){ 
 		super("Game");
 		}
 	
 	public static void main(String args[]){
 		try{
 			AppGameContainer app = new AppGameContainer(new OOWorld());
 			app.setDisplayMode(800, 600, false);
 			app.start();
 		}catch(SlickException e){
 		e.printStackTrace();
 		}
 	}
 	
 	public boolean isBlocked(Vector2d nextPos, int width, int height){
 		Rectangle tileRect = new Rectangle(0, 0, SIZE, SIZE);
 		Rectangle objRect = new Rectangle(nextPos.getX(), nextPos.getY(), width, height);
 		for(int y = 0; y < blocked.length; y++){
 			for(int x = 0; x < blocked[0].length; x++){
 				if(blocked[x][y]){
 					tileRect.setLocation(x*SIZE, y*SIZE);
 					if(objRect.intersects(tileRect))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public void init(GameContainer container) throws SlickException{
 		Image[] movementUp = {new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png"),
 								new Image("res/Character1_back.png")};
 		
 		Image[] movementDown = {new Image("res/Character1_frontanimation-frame-1.png"), 
 								new Image("res/Character1_frontanimation-frame-2.png"),
 								new Image("res/Character1_frontanimation-frame-3.png"),
 								new Image("res/Character1_frontanimation-frame-4.png"),
 								new Image("res/Character1_frontanimation-frame-5.png"),
 								new Image("res/Character1_frontanimation-frame-6.png"),
 								new Image("res/Character1_frontanimation-frame-7.png"),
 								new Image("res/Character1_frontanimation-frame-8.png")};
 		
 		Image[] movementRight = {new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png"),
 								new Image("res/Character1_right.png")};
 		
 		Image[] movementLeft = {new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png"),
 								new Image("res/Character1_left.png")};
 		
 		
 		int duration[] = {100, 100, 100, 100, 100, 100, 100, 100};
 		up = new Animation(movementUp, duration, false);
 		down = new Animation(movementDown, duration, false);
 		left = new Animation(movementLeft, duration, false);
 		right = new Animation(movementRight, duration, false);
 		Animation[] animList = {up, down, left, right};
 		
 		player = new Player(new Vector2d(10*SIZE, 10*SIZE), animList);	
 		
 		entity = new Boss(new Vector2d(25 * SIZE, 25 * SIZE), animList);
 		entity.setProjectileList(projectiles);
 		player.setCurrentBoss(entity);
 		
 		tileMap = new TiledMap("res/tilemap.tmx");
 		blocked = new boolean[tileMap.getWidth()][tileMap.getHeight()];
 		try{
 			this.initAbilities();
 		}catch (IOException e){
 			e.printStackTrace();
 			System.out.println("failed");
 		}
 		for(int x = 0; x < tileMap.getWidth(); x ++){
 			for (int y = 0; y < tileMap.getHeight(); y++){
 				int ID = tileMap.getTileId(x,  y , 0);
 				String value = tileMap.getTileProperty(ID, "blocked", "false");
 				if("true".equals(value)){
 					blocked[x][y] = true;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta)throws SlickException{		
 		Projectile proj;
 		playerNextPos = inputHandler.playerInput(player, container.getInput(), delta);
 		entity.update(delta);
 		if(!isBlocked(	playerNextPos, 
 						player.getWidth(), 
 						player.getHeight())){
 			player.setCoord(playerNextPos);
 		}
 		
 		player.logic();
 		
 		for(int i = 0; i < projectiles.size(); i++){
 			proj = projectiles.get(i);
 			proj.update(delta);
 			if(isBlocked(	proj.pos, 
 							proj.getWidth(), 
 							proj.getHeight())){
 				projectiles.remove(i);
 			}
 			else if(proj.collidesWith(entity) && proj.getOwner() == player){
 				proj.damage(entity);
 				projectiles.remove(i);
 			}
 			else if(proj.collidesWith(player) && proj.getOwner() == entity){
 				proj.damage(player);
 				projectiles.remove(i);
 			}
 		}
 	}
 	
 	private void drawHealthBar(Graphics g, Entity entity, int xOffset, int yOffset){
 		int width = entity.getTexture().getWidth();
 		float xPos = entity.getX() + xOffset;
 		
 		int height = entity.getTexture().getHeight();
 		float yPos = entity.getY() + yOffset + height;
 		
 		g.setColor(new Color(200, 0 ,0));
 		g.fillRect(xPos, yPos, width, 3);
 		g.setColor(new Color(0, 200, 0));
 		g.fillRect(xPos, yPos, width*(entity.getHealth()/entity.getMaxHealth()), 3);
 	}
 	
 	public void render(GameContainer container, Graphics g) throws SlickException{		
 		int xOffset = -(int)player.getX()+WIDTH/2;
 		int yOffset = -(int)player.getY()+HEIGHT/2;
 		
 		tileMap.render(xOffset, yOffset);
 		player.draw(WIDTH, HEIGHT);
 		entity.draw(xOffset, yOffset);
 		for(int i = 0; i < projectiles.size();i++){
 			projectiles.get(i).draw(xOffset, yOffset);
 		}
 		this.drawHealthBar(g, player, xOffset, yOffset);
 		this.drawHealthBar(g, entity, xOffset, yOffset);
 	
 	}
 	
 	public void initAbilities() throws FileNotFoundException, IOException, SlickException{
 		BufferedReader reader = new BufferedReader(new FileReader("res/abilities.txt"));
 		String[] values;
 		String line;
 		Ability ability = new Ability();
 		Projectile proj;
 		try{
 			line = reader.readLine();
 			while(line != null){	
 				values = line.split(",");
 				if(values[0].equals("add")){
 					entity.addAttack(ability, Integer.parseInt(values[1]));
 					ability = new Ability();
 					
 				}else{
					System.out.println(line);
 					proj = new Projectile(	
											Float.parseFloat(values[0]),
											Float.parseFloat(values[1]),
 											Float.parseFloat(values[2]),
 											Float.parseFloat(values[3]),
 											new Image(values[4]),
 											entity);
 					ability.addProjectile(proj);
 				}
 				line = reader.readLine();
 			}
 		} finally{
 			reader.close();
 		}
 	}
 }
