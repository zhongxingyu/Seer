 package game;
 
 import game.model.entity.Asteroid;
 import game.model.entity.Bullet;
 import game.model.entity.Entity;
 import game.model.entity.Spaceship;
 
 import java.util.ArrayList;
 
 public class Game {
 	private static final int MAX_TIME_BETWEEN_WAVES=10000;//in ms
 	private static final int MIN_TIME_BETWEEN_WAVES=5000;
 	private static final int MAX_ASTEROIDS_PER_WAVE = 3;
 	private static final int MIN_ASTEROIDS_PER_WAVE = 1;
	private static final int ASTEROIDS_CAP=0;
 	private long time=0;
 	private long lastTime=0;
 	private ArrayList<Spaceship> spaceships;
 	private ArrayList<Asteroid> asteroids;
 	private ArrayList<Bullet> bullets;
 	private final int height;
 	private final int width;
 	private long nextWave=-1;
 
 	public Game(int width, int height){
 		this.spaceships=new ArrayList<Spaceship>();
 		this.asteroids=new ArrayList<Asteroid>();
 		this.bullets=new ArrayList<Bullet>();
 		this.height=height;
 		this.width=width;
 	}
 	
 	public void clear(){
 		this.spaceships=new ArrayList<Spaceship>();
 		this.asteroids=new ArrayList<Asteroid>();
 		this.bullets=new ArrayList<Bullet>();
 	}
 
 	public Spaceship getShip(String playerName){
 		for(int i=0; i<spaceships.size();i++){
 			if(spaceships.get(i).getPlayerName()==playerName)
 				return spaceships.get(i);
 		}
 		return null;
 	}
 	public ArrayList<Spaceship> getShips(){
 		return spaceships;
 	}
 
 	public ArrayList<Entity> getEntities(){
 		ArrayList<Entity> entities=new ArrayList<Entity>();
 		entities.addAll(spaceships);
 		entities.addAll(asteroids);
 		entities.addAll(bullets);
 		return entities;
 	}
 
 	public void updateTime(long currentTime){
 		lastTime=time;
 		time=currentTime;
 	}
 	private long getDelta(){
 		return time-lastTime;
 	}
 
 	public void gameLoop(){
 		for(int i=0;i<getEntities().size();i++){
 			Entity current = getEntities().get(i);
 			if(current instanceof Bullet){
 				if(current.getX() <= 0 || current.getY() <= 0 || current.getX() >= Settings.WIDTH || current.getY() >= Settings.HEIGHT)
 					current.destroy();
 			}
 			if(!current.isDestroyed()){
 				if(current.hasPoped()){
 					current.updatePosition(getDelta());
 					current.updateSpeed(getDelta());
 				}
 			}else{
 				spaceships.remove(current);
 				asteroids.remove(current);
 				bullets.remove(current);
 			}
 		}
 		processCollisions();
 		asteroidsWave(time);
 	}
 
 	private void processCollisions(){
 		ArrayList<Entity> entities= this.getEntities();
 		for(int i=0;i<entities.size();i++){
 			boolean collided=false;
 			for(int j=i+1;j<entities.size();j++){
 				if(entities.get(i).collides(entities.get(j))){
 					collided=true;
 					if(entities.get(i).hasPoped() && entities.get(j).hasPoped()){
 						entities.get(i).onCollision(entities.get(j));
 						entities.get(j).onCollision(entities.get(i));
 					}
 				}
 			}
 			if(!collided){
 				entities.get(i).pop();
 			}
 		}
 	}
 	
 
 	public void executeCommand(Command com, Entity entity){
 		switch(com){
 			case GO_FORWARD:
 				entity.increaseMomentum(getDelta());
 				break;
 			case GO_BACKWARD:
 				entity.increaseMomentum(getDelta(), true);
 				break;
 			case RESET_MOMENTUM:
 				//System.out.println("reset");
 				entity.resetMomentum();
 				break;
 			case TURN_LEFT:
 				entity.rotate(getDelta(), true);
 				break;
 			case TURN_RIGHT:
 				entity.rotate(getDelta(), false);
 				break;
 			case FIRE:
 				Spaceship player= (Spaceship) entity;
 				if(player.canFire(time)){
 					player.updateFireTime(time);
 					this.bullets.add(new Bullet(player));
 				}
 				break;
 		}
 	}
 
 	public void asteroidsWave(long currentTime){
 		if(nextWave==-1)
 			nextWave=(long)(currentTime+MIN_TIME_BETWEEN_WAVES+Math.random()*(MAX_TIME_BETWEEN_WAVES-MIN_TIME_BETWEEN_WAVES));
 		else if(currentTime>=nextWave){
 			int asteroids=MIN_ASTEROIDS_PER_WAVE+(int)Math.round(Math.random()*(MAX_ASTEROIDS_PER_WAVE-MIN_ASTEROIDS_PER_WAVE));
 			for(int i=0;i<asteroids && ASTEROIDS_CAP>this.asteroids.size();i++){
 				this.asteroids.add(new Asteroid((int)(Math.random()*this.width),(int) (Math.random()*this.height)));
 			}
 			nextWave=(long)(currentTime+MIN_TIME_BETWEEN_WAVES+Math.random()*(MAX_TIME_BETWEEN_WAVES-MIN_TIME_BETWEEN_WAVES));
 		}
 	}
 	public void addPlayer(String id){
		Spaceship player = new Spaceship(100,100,id);
 		if(id.equals(Settings.PLAYER1)){
 			player =new Spaceship(100,100,id);
 			player.setColor(1.0f, .0f, .0f);
 		}else if(id.equals(Settings.PLAYER2)){
 			player =new Spaceship(Settings.WIDTH-100,Settings.HEIGHT-100,id);
 			player.setColor(.2f, 1.0f, 1.0f);
 		}
		this.spaceships.add(player);
 	}
 }
