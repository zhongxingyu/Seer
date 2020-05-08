 package vort;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import robocode.AdvancedRobot;
 import robocode.BulletHitEvent;
 import robocode.ScannedRobotEvent;
 
 class ScannedData
 {
 	public double X;
 	public double Y;
 	public double PredX;
 	public double PredY;
 	int tick;
 	ScannedRobotEvent rawData;
 	
     public ScannedData(AdvancedRobot myRobot, ScannedRobotEvent scanned, int tick)
     {
         rawData = scanned;
         double absAngle = myRobot.getHeadingRadians() + scanned.getBearingRadians();
         X = myRobot.getX() + Math.sin(absAngle) * scanned.getDistance();
         Y = myRobot.getY() + Math.cos(absAngle) * scanned.getDistance();
         PredX = X + Math.sin(scanned.getHeadingRadians()) * scanned.getVelocity();
         PredY = Y + Math.cos(scanned.getHeadingRadians()) * scanned.getVelocity();
         this.tick = tick;
     }	
     
     public void Paint(Graphics2D g)
     {
     	g.setColor(Color.red);
    	g.drawRect((int)X - 5, (int)Y - 5, 10, 10);
     	g.setColor(Color.pink);
    	g.drawRect((int)PredX - 5, (int)PredY - 5, 10, 10);
     }
     
     public boolean IsExpired(int tick)
     {
         return (tick - this.tick) > 6;
     }
 }
 
 public class Chaser extends AdvancedRobot
 {
 	Map<String, ScannedData> scanned;
 	int tick;
 	
     public void onBulletHit(BulletHitEvent evnt)
     {
         if (evnt.getEnergy() <= 0.0)
             if (scanned.containsKey(evnt.getName()))
                 scanned.remove(evnt.getName());
     }
     
     private ScannedData GetNearestBot()
     {
     	double minDist = 0.0;
     	ScannedData minDistBot = null;
         for (ScannedData bot : scanned.values())
         {
             double deltaX = bot.PredX - getX();
             double deltaY = bot.PredY - getY();
             double vlen = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
             if (minDistBot == null)
             {
             	minDist = vlen;
             	minDistBot = bot;
             }
             else
             {
             	if (vlen < minDist)
             	{
             		minDist = vlen;
             		minDistBot = bot;
             	}
             }
         }
         return minDistBot;
     }
     
     public void onPaint(Graphics2D g)
     {
         for (ScannedData bot : scanned.values())
         	bot.Paint(g);
     }
     
 	public void run()
 	{
 		tick = 0;
 		scanned = new HashMap<String, ScannedData>();
 		Color botColor = new Color(0xEE, 0xEE, 0xFF); 
 		setColors(botColor, botColor, botColor);
 		
         for (; ; )
         {
         	Map<String, ScannedData> scannedNew = 
         			new HashMap<String, ScannedData>();
         	for (Entry<String, ScannedData> kv : scanned.entrySet())
         	{
         		if (!kv.getValue().IsExpired(tick))
         			scannedNew.put(kv.getKey(), kv.getValue());
         	}
     		scanned = scannedNew;
 
             if (scanned.size() == 0)
             {
                 setTurnRadarRight(360);
             }
             else
             {
                 ScannedData sd = GetNearestBot();
                 double deltaX = sd.PredX - getX();
                 double deltaY = sd.PredY - getY();
                 double vlen = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                 double vxn = deltaX / vlen;
                 double vyn = deltaY / vlen;
                 double angle = Math.acos(vyn) * 180 / Math.PI;
                 if (vxn < 0)
                     angle = 360 - angle;
 
                 double turnAngle = FixTurnAngle(angle - getHeading());
                double maxTurn = 10.0 - 0.75 * Math.abs(getVelocity());
                double nextTurn = turnAngle;
                if (Math.abs(turnAngle) > maxTurn)
                	nextTurn = Math.signum(turnAngle) * maxTurn;
                double radarTurnAngle = FixTurnAngle(angle - getRadarHeading() - nextTurn);

                 if (vlen < 100.0 && Math.abs(turnAngle) < 4.0)
                     setFire(3.0);
                 
                if (radarTurnAngle > 0.0)
                	setTurnRadarRight(radarTurnAngle);
                else
                	setTurnRadarLeft(-radarTurnAngle);
                 setTurnRight(turnAngle);
                 setAhead(vlen - 60);
             }
             execute();
             tick++;
         }
 	}
 	
     private double FixTurnAngle(double angle)
     {
         double result = angle;
         while (result > 180)
             result -= 360;
         while (result < -180)
             result += 360;
         return result;
     }
     
 	public void onScannedRobot(ScannedRobotEvent evnt)
 	{
 		scanned.put(evnt.getName(), new ScannedData(this, evnt, tick));
 	}
 }
