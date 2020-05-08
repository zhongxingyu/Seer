 package balle.strategy.pFStrategy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PFPlanning {
 	Pos robot;
 	PointObject opponent;
 	PointObject ball;
 	double default_power = 5;
 	RobotConf config;
 	double Stopdistance = 5;
 	List<Object> objects;
 	// power for opponent.
 	double opponentPower;
 	// influence distance for opponent
 	double opponentInf;
 	// power for goal location.
 	double ballPower;
 	// power orientation, extended potential field.
 	double opponentAlphaPower;
 
 	// Constructor for PFPlanning:
 	// conf: Configuration parameters of the robot.
 	// opponentPower: Repulsive power for opponent
 	// opponentInf: Influence distance for opponent.
 	// ballPower: Power for goal position
 	public PFPlanning(RobotConf conf, double opponentPower, double opponentInf,
 			double targetPower) {
 		this.config = conf;
 		objects = new ArrayList<Object>();
 		this.opponentPower = opponentPower;
 		this.opponentInf = opponentInf;
 		this.ballPower = targetPower;
 	}
 
 	// Constructor for PFPlanning:
 	// conf: Configuration parameters of the robot.
 	// opponentPower: Repulsive power for opponent
 	// opponentInf: Influence distance for opponent.
 	// ballPower: Power for goal position
 	// alpha: Orientation power
 	public PFPlanning(RobotConf conf, double opponentPower, double opponentInf,
 			double targetPower, double alpha) {
 		this.config = conf;
 		objects = new ArrayList<Object>();
 		this.opponentPower = opponentPower;
 		this.opponentInf = opponentInf;
 		this.ballPower = targetPower;
 		this.opponentAlphaPower = alpha;
 	}
 
 
 	private void init(Pos robot, Pos opponent, Point ball,
 			boolean orig)
 	{
 		PointObject opponentObj = new PointObject(opponent.getLocation(),
 				opponentPower, opponentInf, opponentAlphaPower);
 		PointObject ballObj = new PointObject(ball, ballPower, Double.MAX_VALUE);
 		this.robot = robot;
 		this.opponent = opponentObj;
 		this.ball = ballObj;
 		
 	}
 	public VelocityVec update(Pos robot, Pos opponent,Point goal, Point ball,
 			boolean orig){
 		init(robot,opponent,ball,orig);
 		Vector vgoal=new Vector(goal);
 		Vector vball=new Vector(ball);
 		Vector vres=vgoal.subtract(vball);
 		Vector threshold=new Vector(20,20);
 		Vector finalres=vres.subtract(threshold);
 		PointObject obj=new PointObject(finalres, 10000, this.opponentInf);
 		List<Object> complList =  new ArrayList<Object>(objects);
 		complList.add((Object) this.opponent);
 		complList.add(obj);
		Vector res = GoTo(complList, this.ball, robot.getLocation());
<<<<<<< HEAD
 /*		if (Runner.DEBUG) {
 			//System.out.println("Result Vector: " + res.toString());
		}*/
=======
//		if (Runner.DEBUG) {
//			//System.out.println("Result Vector: " + res.toString());
//		}
>>>>>>> 61e4de4b673159bdc8b0d093e4b2e3a9d1c9677e
 		if (orig)
 			return new VelocityVec(res.getX(),res.getY());
 		else
 			return getVelocity(res, robot);
 
 
 	}
 
 	// Given Pos of different objects, this function will create PointObjects to
 	// be used later.
 	// robot: robot Pos.
 	// opponent: opponent Pos.
 	// ball: ball Location.
 	// srr: add surrounding objects for target position to approach the target
 	// position
 	// from specific direction.
 	// orig: return original vector, if unset return vector is left,right wheels
 	// velocity, if
 	// if set it will return the basic velocity vector created by PFPlanner.
 	public VelocityVec update(Pos robot, Pos opponent, Point ball,
 			boolean orig) {
 
 		init(robot,opponent,ball,orig);
 		List<Object> complList =  new ArrayList<Object>(objects);
 		complList.add((Object) this.opponent);
 		Vector res = GoTo(complList, this.ball, robot.getLocation());
 		Vector vball=new Vector(ball);
 		Vector vrobot=new Vector(robot.getLocation());
 		if(vball.subtract(vrobot).size()<Stopdistance)
 		{
 			res=new Vector(0,0);
		}
<<<<<<< HEAD
 	/*	if (Runner.DEBUG) {
 			//System.out.println("Result Vector: " + res.toString());
		}*/
=======
//		if (Runner.DEBUG) {
//			//System.out.println("Result Vector: " + res.toString());
//		}
>>>>>>> 61e4de4b673159bdc8b0d093e4b2e3a9d1c9677e
 		if (orig)
 			return (VelocityVec) res;
 		else
 			return getVelocity(res, robot);
 
 	}
 
 	// Adds static object in the arena to list of static obstacles.
 	public void AddObjects(Object r) {
 		objects.add(r);
 	}
 
 	// Actual function which computes the the next velocity vector to be applied
 	// to the robor.
 	public Vector GoTo(List<Object> obstacles, PointObject dest_obj,
 			Point start_point) {
 		// calculate distance so if we reached the target position we stop.
 		double dist = Math.sqrt((start_point.getX() - dest_obj.getX())
 				* (start_point.getX() - dest_obj.getX())
 				+ (start_point.getY() - dest_obj.getY())
 				* (start_point.getY() - dest_obj.getY()));
 		if (dist < Stopdistance) {
 			return new Vector(0, 0);
 		}
 
 		Vector rep = new Vector(0, 0);
 		// iterate through all obstacles and compute sum of all repulsive
 		// vectors
 		for (int i = 0; i < obstacles.size(); i++) {
 			rep = rep.add(obstacles.get(i).getVector(start_point, true));
 		}
 		// Compute attractive vector.
 		Vector att = dest_obj.getVector(start_point, false);
 
 		return att.add(rep);
 
 	}
 
 	// Extended Potential Field.This function takes into account the orientation
 	// of Robot.
 	public Vector GoTo(List<Object> obstacles, PointObject dest_obj,
 			Pos start_point) {
 		// calculate distance so if we reached the target position we stop.
 		double dist = Math.sqrt((start_point.getLocation().getX() - dest_obj
 				.getX())
 				* (start_point.getLocation().getX() - dest_obj.getX())
 				+ (start_point.getLocation().getY() - dest_obj.getY())
 				* (start_point.getLocation().getY() - dest_obj.getY()));
 		if (dist < Stopdistance) {
 			return new Vector(0, 0);
 		}
 
 		Vector rep = new Vector(0, 0);
 		// iterate through all obstacles and compute sum of all repulsive
 		// vectors
 		for (int i = 0; i < obstacles.size(); i++) {
 			rep = rep.add(obstacles.get(i).getVector(start_point, true));
 		}
 		// Compute attractive vector.
 <<<<<<< HEAD
 	/*	if (Runner.DEBUG){
 			//System.out.println("PFPlanning::PointObject::attractive Force, Clac. attractive forc");
 		}*/
 =======
 //		if (Runner.DEBUG){
 //			//System.out.println("PFPlanning::PointObject::attractive Force, Clac. attractive forc");
 //		}
 >>>>>>> 61e4de4b673159bdc8b0d093e4b2e3a9d1c9677e
 		Vector att = dest_obj.getVector(start_point, false);
 		
 		return att.add(rep);
 
 	}
 
 	// This function translates linear and angular velocities to left and right
 	// velocities.
 	private VelocityVec CvtVelocity(double Vlin, double VAng, double r) {
 		double left = Vlin - r * Math.sin(VAng);
 		double right = Vlin + r * Math.sin(VAng);
 		VelocityVec vector = new VelocityVec(left, right);
 		// System.out.println("r: " + r+" left" + left + "right" + right);
 		return vector;
 	}
 
 	// Given current Pos of the robot and current velocity this function
 	// computes linear and angular
 	// velocities for a differential drive robot using input parameters of the
 	// robot.
 	private VelocityVec getVelocity(Vector inputVel, Pos current) {
 
 		
 		double size = inputVel.size();
 		if(size==0)
 			return new VelocityVec(0, 0);
 		double alpha = inputVel.normalAngle();
 		double dist_alpha = alpha - current.getAngle();
 <<<<<<< HEAD
 	/*	if(Runner.DEBUG) {
 			//System.out.println("Current T: "+current.getAngle()+", dist_alpha="+dist_alpha);
 		}*/
 =======
 //		if(Runner.DEBUG) {
 //			//System.out.println("Current T: "+current.getAngle()+", dist_alpha="+dist_alpha);
 //		}
 >>>>>>> 61e4de4b673159bdc8b0d093e4b2e3a9d1c9677e
 		if(dist_alpha>Math.PI)
 			dist_alpha=-1*(2*Math.PI-dist_alpha);
 		else if(dist_alpha<-1*Math.PI)
 			dist_alpha=2*Math.PI+dist_alpha;
 <<<<<<< HEAD
 	/*	if(Runner.DEBUG) {
 			//System.out.println("Final dist_alpha="+dist_alpha);
 		}*/
 =======
 //		if(Runner.DEBUG) {
 //			//System.out.println("Final dist_alpha="+dist_alpha);
 //		}
 >>>>>>> 61e4de4b673159bdc8b0d093e4b2e3a9d1c9677e
 		double Vlin = Math.cos(dist_alpha) * size;
 		double angSize=1/size;
 		//if(*angSizeangSize>10)
 		//	angSize=10;
 		double Vang = 0.4*dist_alpha/Math.PI;
 		//double threshold=0.1;
 		//if(Vang>threshold)
 		//	Vang=threshold;
 		//if(Vang<-1*threshold)
 		//	Vang=-1*threshold;
 		
 <<<<<<< HEAD
 	/*	if(Runner.DEBUG) {
 			//System.out.println(Vlin + " " + Vang);
 		}*/
 =======
 //		if(Runner.DEBUG) {
 //			//System.out.println(Vlin + " " + Vang);
 //		}
 >>>>>>> 61e4de4b673159bdc8b0d093e4b2e3a9d1c9677e
 		return CvtVelocity(Vlin, Vang, config.getr());
 	}
 
 }
