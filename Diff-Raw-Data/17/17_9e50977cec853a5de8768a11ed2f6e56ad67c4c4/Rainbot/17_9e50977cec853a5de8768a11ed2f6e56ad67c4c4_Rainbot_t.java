 package net.adbenson.robocode.rainbot;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.util.LinkedList;
 
 import net.adbenson.robocode.botstate.BattleState;
 import net.adbenson.robocode.botstate.OpponentState;
 import net.adbenson.robocode.bullet.BulletQueue;
 import net.adbenson.robocode.bullet.OpponentBullet;
 import net.adbenson.robocode.prediction.ImpossibleToSeeTheFutureIsException;
 import net.adbenson.robocode.prediction.PredictedTarget;
 import net.adbenson.robocode.prediction.PredictiveTargeting;
 import net.adbenson.robocode.prediction.TargetOutOfRangeException;
 import net.adbenson.utility.Utility;
 import net.adbenson.utility.Vector;
 import robocode.AdvancedRobot;
 import robocode.BulletHitBulletEvent;
 import robocode.BulletHitEvent;
 import robocode.BulletMissedEvent;
 import robocode.HitByBulletEvent;
 import robocode.HitRobotEvent;
 import robocode.HitWallEvent;
 import robocode.RobotDeathEvent;
 import robocode.Rules;
 import robocode.ScannedRobotEvent;
 import robocode.util.Utils;
 
 public class Rainbot extends AdvancedRobot {
 	
 	public static final double MAX_TURN = Math.PI / 5d;
 	
 	//Min: 0.03 will assure accuracy to the farthest point on the field
 	//Max: 0.15 will assure accuracy at 1/4 of field diagonal
 	//Lower will increase likelihood of waiting additional turns for gun to turn
 	public static final double ACCEPTABLE_GUN_OFFTARGET = 0.07;
 	
 	private BattleState state;
 	
 	public LinkedList<ScannedRobotEvent> foundOpponents;
 	
 	private PredictiveTargeting predictor;
 	
 	private int preferredDirection;
 	
 	private static Rectangle2D field;
 	private Rectangle2D safety;
 	
 	private double preferredDistance;
 	
 	private BotColor color;
 	
 	public Rainbot() {
 		super();
 		
 		state = new BattleState();
 		
 		predictor = new PredictiveTargeting(state);
 		
 		preferredDirection = 1;
 		
 		color = new BotColor();
 		
 		foundOpponents = new LinkedList<ScannedRobotEvent>();
 	}
 	
 	public void run() {
 		generateBoundries();
 		
		preferredDistance = Rules.RADAR_SCAN_RADIUS / 3.0;		
 		
 		setAdjustGunForRobotTurn(true);
 		setAdjustRadarForGunTurn(true);
 		setAdjustRadarForRobotTurn(true);
 	    
 	    startRadarLock();
 	    
 	    double requiredFirepower = 0;
 		boolean ready = false;
 		boolean aim = false;
 		boolean fire = false;
 		
 		long turnTargeted = 0;
 	    
 	    do {
 	    	//Store the current state of this bot and any others scanned
 	    	state.addBots(this, foundOpponents);
 	    	//Reset the list of scanned robots
 	    	foundOpponents = new LinkedList<ScannedRobotEvent>();
 
 	    	color.hueShift(this);
 	    	
 	    	detectOpponentFire();
 	    	
 	    	//Square off!
 	    	faceOpponent();
 	    	
 	    	updateBulletStates();
 	    	
 	    	if (predictor.canPredict(getTime())) {
 	    		//TODO decide to predict when the opponent stops moving
 
 		    	//ONLY look into prediction if we're not preparing to fire or have recently fired 
 	    		if (!ready && this.getGunHeat() <= getGunCoolingRate()) {
 	    			
 					try {
 						PredictedTarget target = predictor.getNewTarget(getPosition());
 
 						requiredFirepower = target.requiredPower;
 						setGunTurnToTarget(target.target);
 
 						ready = true;
 						turnTargeted = getTime();
 						
 					} catch (TargetOutOfRangeException e) {
 						System.out.println("Predicted target unreachable");
 					} catch (ImpossibleToSeeTheFutureIsException e) {
 						System.out.println("Impossible to see, the future is. ("+e.getMessage()+")");
 					}
 		    		
 	    			aim = false;
 	    			fire = false;
 	    		}   	
 	    		
 		    	if (ready && 
 		    			Math.abs(this.getGunTurnRemainingRadians()) < ACCEPTABLE_GUN_OFFTARGET &&
 		    			this.getGunHeat() <= 0) {	    		
 		    		aim = true;
 		    		fire = false;
 		    	}
 	    		
 		    	if (state.getTarget().isAlive() && ready && aim && !fire) {
 		    		setFire(requiredFirepower);   		
 		    		ready = false;
 		    		aim = false;
 		    		fire = true;
 		    	}
 
 	    	
 	    	}

 	    	execute();
 
 	    } while (true);
 	}
 	
 	private void updateBulletStates() {
     	//Update bullet positions
     	state.getSelfBullets().updateAll(getTime());
     	for(BulletQueue<OpponentBullet> queue: state.getAllOpponentBullets()) {
     		queue.updateAll(getTime());
     	}	  
 	}
 
 	private void generateBoundries() {
 		Vector botSize = new Vector(getWidth(), getHeight());
 	
 		field = new Rectangle2D.Double(
 				(botSize.x/2), (botSize.y/2), 
 				getBattleFieldWidth()-(botSize.x), getBattleFieldHeight()-(botSize.y)
 		);
 		safety = new Rectangle2D.Double(
 				botSize.x, botSize.y, 
 				getBattleFieldWidth()-(botSize.x*2), getBattleFieldHeight()-(botSize.y*2)
 		);
 	}
 
 	private void setGunTurnToTarget(OpponentState target) {
 		Vector offset = target.position.subtract(getPosition());
 		double heading = Utility.angleDifference(offset.getAngle(), this.getGunHeadingRadians());
 		this.setTurnGunRightRadians(heading);
 	}
 	
 	private void faceOpponent() {
     	if (state.hasTarget()) {
     		OpponentState o = state.getTarget();
     		
     		double offFace = o.bearing;
     		//We don't care which direction we face, so treat either direction the same.
     		if (offFace < 0) {
     			offFace += Math.PI;
     		}
     		   		
     		//Offset so that "facing" is 0
     		offFace -= Utility.HALF_PI;
     		
     		//Turn farther away the closer we are - by 1/2 field away, straighten out
     		double distanceRatio = (preferredDistance - o.distance) / (preferredDistance);   		
     		offFace += MAX_TURN * distanceRatio * preferredDirection;
     		    		
     		//Multiply the offset - we don't have all day! Move it! (If it's too high, it introduces jitter.)
     		setTurnRight(offFace * 10); 
     		
     	}
     	else {
     		//Nuthin' better to do...
     		setTurnRight(Double.POSITIVE_INFINITY);
     	}
 	}
 	
 	private void detectOpponentFire() {
 		for (OpponentState opponent : state.getAllOpponents()) {
 			if (opponent.hasFired()) {
 				state.opponentFired(opponent.name, getTime());
 				
 				preferredDirection = -preferredDirection;
 				setAhead(100 * preferredDirection);
 			}
 		}		
 	}
 
 	private Vector getPosition() {
 		return new Vector(getX(), getY());
 	}
 	
 	public void setFire(double power) {	
 		robocode.Bullet bullet = super.setFireBullet(power);
 		state.selfFired(predictor.getTarget(), bullet, getTime());
 	}
 	
 	private void startRadarLock() {
 	    setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
 	}
 	
 	private void maintainRadarLock(ScannedRobotEvent e) {
 		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
 		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn) * 1.9);
 	}
 	
 	public void onPaint(Graphics2D g) {
 		
 		g.setColor(Color.red);
 		g.draw(field);
 		
 		g.setColor(Color.green);
 		g.draw(safety);
 		g.setStroke(new BasicStroke(3));
 		
 		for(BulletQueue<OpponentBullet> queue: state.getAllOpponentBullets()) {
 			queue.draw(g);
 		}
 		state.getSelfBullets().draw(g);
 		
 		for(OpponentState opponent: state.getAllOpponents()) {
 			opponent.drawTarget(g);
 		}
 		state.getSelf().draw(g);
 		
 		predictor.drawPrediction(g);
 	}
 	
 	public void onScannedRobot(ScannedRobotEvent e) {
 		foundOpponents.add(e);
 		maintainRadarLock(e);
 		state.setTargetName(e.getName());
 	}
 	
 	public void onRobotDeath(RobotDeathEvent event) {
 		state.getOpponent(event.getName()).died();
 	}
 	
	//Shot opponent with bullet
 	public void onBulletHit(BulletHitEvent event)  {
 		color.startRainbow();
 		predictor.hitTarget(event.getName());
		state.getOpponent(event.getName()).shotBySelf();
 	}
 	
 	public void onBulletHitBullet(BulletHitBulletEvent event) {
 		state.getSelfBullets().remove(event.getBullet());
 		//TODO remove opponent bullets		
 	}
 	
 	public void onBulletMissed(BulletMissedEvent event) {
 		state.getSelfBullets().remove(event.getBullet());
 		predictor.missedTarget(event.getBullet());
 	}
 	
	//Shot by opponent
 	public void onHitByBullet(HitByBulletEvent event) {
 		//TODO avoid?
 	}
 	
 	//Collision with self
 	public void onHitRobot(HitRobotEvent event) {
 		state.getOpponent(event.getName()).collidedWithSelf();
 	}
 	
 	public void onHitWall(HitWallEvent event) {
 		//TODO avoid?
 	}
 	
 	public static Rectangle2D getField() {
 		return field;
 	}
 }
