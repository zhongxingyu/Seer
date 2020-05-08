 import java.lang.Thread;
 import java.awt.Point;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import static java.lang.Math.*;
 
 public class Processor implements Runnable{
 	private Stage stage;
 	private Clock clock;
 	//private State myState;
 	private long time;
 	private EventConnect eventConnect;
 	private int HEIGHT;
 	private int WIDTH;
 
 	private BlockingQueue<Printable[]> queue;
 	private ArrayList<Enemy> enemyList;
 	private ArrayList<Bullet> bulletList;
 	private ArrayList<Bullet> playerBulletList;
 
 	private Hero player;
 
 	private void runInstructions(){
 		Instruction[] insts = stage.get();
 		if(insts == null){
 			return ;
 		}
 		for(int i=0;i<insts.length;i++){
 			String instType = insts[i].getInstType();
 			if(instType.equals("enemy")){
 				EnemyInstruction inst = insts[i];
 				int enemyId = inst.getEnemyId();
 				Enemy e = EnemyFactory.getEnemy(inst);
 				/* do not use i */
 				for(int j=enemyList.size();j<enemyId;j++)
 					enemyList.add(null);
 				Enemy enemy =  EnemyFactory.getEnemy(inst);
 				enemyList.add(enemyId, enemy );
 			}
 			else if(instType.equals("bullet")){
 				BulletInstruction inst = insts[i];
 				if(enemyList.get(inst.getEnemyId()) != null){
 					String bulletType = inst.getBulletType();
 					int enemyId = inst.getEnemyId();
 					int bulletId= inst.getBulletId();
 					int radius = 0;
                     int power  = 0;
                     int imageId= 0;
 					if(bulletType.equals("costum")){
 						radius  = inst.getRadius();
 						power   = inst.getPower();
 						imageId = inst.getImageId();
 					}
 					Enemy enemy = enemyList.get(enemyId);
 					/* do not use i */
 					for(int j=bulletList.size();j<bulletId;j++)
 						bulletList.add(null);
					Bullet bullet = BulletFactory.getBullet(imageId, bulletId, radius, power, enemy.getPosition(), bulletType);
					bulletList.add(bulletId,bullet);
 				}
 			}
 			else if(instType.equals("route")){
 				RouteInstruction inst = insts[i];
 				String targetType = inst.getTargetType();
 				int    targetId   = inst.getTargetId();
 				int    speed      = inst.getSpeed();
 				String pointRefer = inst.getPointRefer();
 				String routeType  = inst.getRouteType();
 				int    pointAngle = inst.getPointAngle();
 				Point  offset     = inst.getPointOffset();
 				Point target;
 				if(targetType.equals("enemy")){
 					if(enemyList.get(targetId) != null){
 						Enemy enemy;
 						enemy = enemyList.get(targetId);
 						target = calcTarget(enemy.getPosition(), pointRefer, pointAngle, offset);
 						Route route = RouteFactory.getRoute(enemy.getPosition(), target, speed, routeType);
 						enemy.setRoute(route);
 					}
 				}
 				else if(targetType.equals("bullet")){
 					if(bulletList.get(targetId) != null){
 						Bullet bullet;
 						bullet = bulletList.get(targetId);
 						target = calcTarget(bullet.getPosition(), pointRefer, pointAngle, offset);
 						Route route = RouteFactory.getRoute(bullet.getPosition(), target, speed, routeType);
 						bullet.setRoute(route);
 					}
 				}
 			}
 			/*
 			else if(instType.equals("end")){
 				System.out.println("The End");
 				clock.stop();
 			}
 			*/
 		}
 	}
 	private void calcBullet(){
 		Point position;
 		for(Bullet bullet : bulletList){
 			if(bullet ==null)
 				continue;
 			bullet.move();
 			position = bullet.getPosition();
 			if(position.getX() < 0 || position.getY() < 0)
 				bulletList.remove(bullet);
 		}
 	}
 	private void calcEnemy(){
 		Point position;
 		for(Enemy enemy : enemyList){
 			if(enemy == null)
 				continue;
 			enemy.move();
 			position = enemy.getPosition();
 		}
 	}
 	private void calcPlayer(){
 		player.move();
 		Bullet playerBullet = player.shoot();
 		if(playerBullet != null){
 			playerBulletList.add( playerBullet);
 		}
 	}
 	private void collision(){
 		for(Bullet bullet : bulletList){
 			if(bullet == null)
 				continue;
 			if(bullet.collision(player)){
 				player.crash();
 			}
 		}
 		for(Bullet bullet : playerBulletList){
 			if(bullet == null)
 				continue;
 			for(Enemy enemy: enemyList){
 				if(enemy == null)
 					continue;
 				if(bullet.collision(enemy)){
 					if(enemy.attacted( player.getPower())){
 						enemy.crash();
 						enemyList.remove(enemy);
 					}
 				}				
 			}
 		}
 	}
 	private void outputToScreem(){
 		ArrayList<Printable> output = new ArrayList<Printable>();
 		output.addAll(enemyList);
 		output.addAll(bulletList);
 		output.addAll(playerBulletList);
 		ArrayList<Printable> temp = new ArrayList<Printable>();
 		for(Printable toPrint: output){
 			if(toPrint != null)
 				temp.add(toPrint);
 		}
 		Object[] obj = temp.toArray();
 		Printable[] array = new Printable[obj.length];
 		for(int i=0;i<obj.length;i++){
 			array[i] = (Printable)obj[i];
 		}
 		queue.offer(array);
 		/*
 		Printable[] enemy = enemyList.toArray();
 		Printable[] bullet = bulletList.toArray();
 		Printable[] playerBullet = playerBulletList.toArray();
 		int elength = enemy.length;
 		int blength = bullet.length;
 		int plength = playerBullet.length;
 		Printable[] output = new Printable[];
 		Systme.
 		*/
 	}
 	/*recycle trash over the screen*/
 	private void recycle(){
 		int x;
 		int y;
 		for(Bullet bullet : bulletList){
 			x = (int)bullet.getPosition().getX();
 			y = (int)bullet.getPosition().getY();
 			if( calcRecycle(x, y) )
 				bulletList.remove(bullet);
 		}
 		for(Bullet bullet : playerBulletList){
 			x = (int)bullet.getPosition().getX();
 			y = (int)bullet.getPosition().getY();
 			if( calcRecycle(x, y) )
 				playerBulletList.remove(bullet);
 		}
 		for(Enemy enemy : enemyList){
 			x = (int)enemy.getPosition().getX();
 			y = (int)enemy.getPosition().getY();
 			if( calcRecycle(x, y) )
 				enemyList.remove(enemy);
 		}
 	}
 	private boolean calcRecycle(int x, int y){
 		boolean xOver;
 		boolean yOver;
 		xOver = ( (x<0 || x>WIDTH) );
 		yOver = ( (y<0 || y>HEIGHT) );
 		if( xOver || yOver)
 			return true;
 		else
 			return false;
 	}
 	private Point calcTarget(Point self, String pointRefer, int pointAngle, Point offset){
 		int x,y;
 		Point selfOffset = null;
 		if(pointRefer.equals("global")){
 			if(pointAngle == 0){
 				Point point = new Point(offset);
 				return point;
 			}else{
 				x = (int)offset.getX()-(int)self.getX();
 				y = (int)offset.getY()-(int)self.getY();
 				Point point = new Point(x,y);
 				selfOffset = round(point, pointAngle);
 			}
 		}
 		else if(pointRefer.equals("self")){
 			if(pointAngle == 0){
 				x = (int)(self.getX()+offset.getX());
 				y = (int)(self.getY()+offset.getY());
 				Point point = new Point(x, y);
 				return point;
 			}else{
 				x = (int)offset.getX();
 				y = (int)offset.getY();
 				Point point = new Point(x,y);
 				selfOffset = round(point, pointAngle);
 			}
 		}
 		else if(pointRefer.equals("player")){
 			if(pointAngle == 0){
 				x = (int)(player.getPosition().getX()+offset.getX());
 				y = (int)(player.getPosition().getY()+offset.getY());
 				Point point = new Point(x, y);
 				return point;
 			}
 			else{
 				x = (int)((offset.getX()+player.getPosition().getX())-self.getX());
 				y = (int)((offset.getY()+player.getPosition().getY())-self.getY());
 				Point point = new Point(x,y);
 				selfOffset = round(point, pointAngle);
 			}
 		}
 		if(selfOffset != null){
 			x = (int)(selfOffset.getX()+self.getX());
 			y = (int)(selfOffset.getY()+self.getY());
 			Point target = new Point(x, y);
 			return target;
 		}
 		else{
 			return null;
 		}
 	}
 	private Point round(Point point,int degree){
 		double x = point.getX();
 		double y = point.getY();
 		/*
 		 *Before change
 		double length = Math.sqrt(x*x+y*y);
 		double th;
 		th = Math.tan(y, x);
 		th = th+ ((double)degree / 180 * Math.PI);
 		x = length*Math.cos(th);
 		y = length*Math.sin(th);
 		Point afterRound = new Point((int)x, (int)y);
 		*/
 		double radian = PI*degree/180;
 		double newX = x*cos(radian) + y*sin(radian);
 		double newY = y*cos(radian) - x*sin(radian);
 		return new Point((int)newX,(int)newY);
 	}
 	public void stateCheck(){
 		if(player.isPause())
 			eventConnect.setPause(true);
 	}
 	public void run(){
 		while(true){
 			try{
 				time = clock.next(time);
 			}
 			catch(DelayException e){
 			}
 			stateCheck();
 			runInstructions();
 			calcBullet();
 			calcEnemy();
 			calcPlayer();
 			collision();
 			outputToScreem();
 		}
 	}
 	public Processor(Stage stage,
 					Clock clock,
 					Hero player,
 					BlockingQueue<Printable[]> queue,
 					EventConnect eventConnect){
 		enemyList  = new ArrayList<Enemy>();
 		bulletList = new ArrayList<Bullet>();
 		playerBulletList = new ArrayList<Bullet>();
 		this.queue = queue;
 		this.player= player; 
 		this.stage = stage;
 		this.clock = clock;
 		this.eventConnect = eventConnect;
 		this.time = 0;
 		this.HEIGHT = 600;
 		this.WIDTH  = 450;
 	}
 }
 
