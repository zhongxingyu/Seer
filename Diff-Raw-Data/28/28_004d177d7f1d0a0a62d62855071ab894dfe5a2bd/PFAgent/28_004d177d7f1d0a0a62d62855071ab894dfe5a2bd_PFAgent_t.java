 package main;
 
 import ServerResponse.*;
 import math.geom2d.Point2D;
 import potentialFields.PotentialField;
 import potentialFields.circular.SeekGoalCircularPF;
 import potentialFields.rectangular.AvoidObstacleRectangularPF;
 import potentialFields.rectangular.AvoidObstacleTangentialRectangularPF;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ty
  * Date: 10/2/13
  * Time: 6:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PFAgent {
 
     /**Our portal to the server*/
     private BZRFlag mServer;
     /**Previous time stamp in seconds*/
     private double mPrevTime;
     /**My team color*/
     private Tank.TeamColor mTeamColor;
     /**Potential fields in the world*/
     private ArrayList<PotentialField> mPotentialFields;
     /**PF for flag*/
     private SeekGoalCircularPF mFlagPf;
     /**PD Controller for angles*/
     PDAngVelController mPdAngVelController;
 
     /**
      * Constructor
      */
     public PFAgent(BZRFlag teamConnection, Tank.TeamColor myTeamColor) throws IOException {
         //mServer = new BZRFlag("localhost", 38508);
         mServer = teamConnection;
         mServer.handshake();
         mTeamColor = myTeamColor;
         mPrevTime = System.currentTimeMillis();
         mPdAngVelController = new PDAngVelController(2, .5);
         buildPotentialFields(myTeamColor);
         plotPfs();
     }
 
     /**
      * Builds up the potential fields
      */
     private void buildPotentialFields(Tank.TeamColor myTeamColor) throws IOException {
         ArrayList<Flag> flags = mServer.getFlags();
         for(Flag flag : flags) {
             if(flag.getTeamColor() != myTeamColor) {
                 mFlagPf = new SeekGoalCircularPF(.1, flag.getPos(), 100, .3);
                 break;
             }
         }
 
         mPotentialFields = new ArrayList<PotentialField>();
         ArrayList<Obstacle> obstacles = mServer.getObstacles();
         for(Obstacle obstacle : obstacles) {
             AvoidObstacleRectangularPF rectPF = new AvoidObstacleRectangularPF(obstacle.getPoints(), 100.0, .35);
             mPotentialFields.add(rectPF);
 
             // calculate the relationship of obstacle center to the goal
             Point2D obsCentroid = Point2D.centroid(obstacle.getPoints());
             Point2D goalToCentroid = new Point2D(new Point2D(obsCentroid.x() - mFlagPf.getCenter().x(),
                     obsCentroid.y() - mFlagPf.getCenter().y()));
             int quadrant = Util.GeometryUtils.getQuadrant(goalToCentroid);
             boolean clockwise = true;
             if(quadrant == 1 || quadrant == 3)
                 clockwise = false;
 
             AvoidObstacleTangentialRectangularPF tangRectPf = new AvoidObstacleTangentialRectangularPF(obstacle.getPoints(), 120, clockwise, .15);
             mPotentialFields.add(tangRectPf);
         }
     }
 
     public void plotPfs() throws IOException {
         ArrayList<Obstacle> obstacles = mServer.getObstacles();
 
         PrintWriter gpiFile = new PrintWriter("world.gpi", "UTF-8");
         gpiFile.println("set xrange [-400.0: 400.0]");
         gpiFile.println("set yrange [-400.0: 400.0]");
         gpiFile.println("unset arrow");
 
         for(Obstacle obstacle : obstacles) {
             gpiFile.println(GnuplotPrinter.getObstaclePlotCmds(obstacle));
         }
 
         gpiFile.println("plot '-' with vectors head");
 
         mPotentialFields.add(mFlagPf);
         for(double x = -400; x <= 400; x += 50.0) {
             for(double y = -400; y <= 400; y += 50.0) {
                 Vector pos = new Vector(x, y);
                 Vector force = PotentialField.getNetVector(pos, mPotentialFields);
                 gpiFile.println(String.format("%s %s %s %s", x, y, force.x(), force.y()));
             }
         }
         mPotentialFields.remove(mFlagPf);
 
         gpiFile.println("e");
         gpiFile.close();
     }
 
     /**
      * Some time has passed; decide what to do.
      */
     public void tick() throws IOException {
         double newTime = System.currentTimeMillis();
         double timeDiffInSec = (newTime - mPrevTime) / 1000;
         mPrevTime = newTime;
 
         ArrayList<MyTank> myTanks = mServer.getMyTanks(Tank.TeamColor.BLUE);
         int pfTankIndex = 1;
         MyTank pfTank0 = myTanks.get(pfTankIndex);
 
         // get the goal angle
         double currAng = pfTank0.getAngle();
         double currAngVel = pfTank0.getAngVel();
         double goalAngle = PotentialField.getNetAngle(pfTank0.getPos(), mPotentialFields, mFlagPf);
 
         double angAcceleration = mPdAngVelController.getAcceleration(goalAngle, currAng, timeDiffInSec);
         double targetVel = currAngVel + angAcceleration;
         boolean needsNormalization = StrictMath.abs(targetVel) > 1.0;
         if(needsNormalization)
             targetVel = targetVel / StrictMath.abs(targetVel);
 
         mServer.angVel(pfTankIndex, targetVel);
         mServer.speed(pfTankIndex, 1.0);
         mServer.shoot(pfTankIndex);
 
         /*ArrayList<MyTank> myTanks = mServer.getMyTanks(Tank.TeamColor.BLUE);
         double newTime = System.currentTimeMillis();
         double timeDiffInSec = (newTime - mPrevTime) / 1000;
         mPrevTime = newTime;
 
         dumbTank0.update(timeDiffInSec, myTanks.get(0).getAngle());
 
         mServer.speed(0, dumbTank0.getVel());
         mServer.angVel(0, dumbTank0.getAngVel());
         if(dumbTank0.shouldShoot())
             mServer.shoot(0);
 
         dumbTank1.update(timeDiffInSec, myTanks.get(1).getAngle());
 
         mServer.speed(1, dumbTank1.getVel());
         mServer.angVel(1, dumbTank1.getAngVel());
         if(dumbTank1.shouldShoot())
             mServer.shoot(1);*/
 
     }
 }
