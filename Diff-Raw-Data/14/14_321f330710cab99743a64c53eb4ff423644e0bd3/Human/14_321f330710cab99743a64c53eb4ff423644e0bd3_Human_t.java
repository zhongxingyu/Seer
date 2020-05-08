 package projectubernahme.lifeforms;
 
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import projectubernahme.Localizer;
 import projectubernahme.ProjectUbernahme;
 import projectubernahme.Vector2D;
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.gfx.LifeformHumanGraphics;
 import projectubernahme.simulator.MainSimulator;
 
 public class Human extends Lifeform {
 	
 	static ArrayList<String> names;
 	
 
 	private static ConvertedGraphics cg = new LifeformHumanGraphics();
 	
 	Point2D waypoint, lastWaypoint;
 
 	public Human (MainSimulator sim) {
 		super(sim);
 		if (names == null) {
 			loadNames();
 		}
 		setName(names.get((int)(Math.random()*names.size())));
 		setCanFly(false);
 		setCanSee(true);
 		setCanMove(true);
 		setBiomass(60.0 + Math.random()*60);
 		setIntelligence(50+100*Math.random());
 	}
 
 	private static void loadNames() {
 		InputStream is = ClassLoader.getSystemResourceAsStream("projectubernahme/lifeforms/first_names.txt");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		names = new ArrayList<String>();
 		String input;
 		try {
 			while ((input = br.readLine()) != null)
 			names.add(input);
 		} catch (IOException e) {
 			ProjectUbernahme.log(Localizer.get("ERROR")+": "+Localizer.get("could not open file with list of first names"));
 			e.printStackTrace();
 		}
 	}
 
 	public Human(MainSimulator sim, Point2D p) {
 		this(sim);
 		position = p;
 	}
 
 	public boolean canSee (Lifeform l) {
 		return (isCanSee() && distance(l) < 10);			
 	}
 
 	@Override
 	public void act(int sleepTime) {
		if (!isControlled()) {
			if (reachedWaypoint()) {
				generateNewWaypoint();
			}
			else {
				viewAngle = Math.atan2(-this.getPoint2D().getY()+waypoint.getY(), -this.getPoint2D().getX()+waypoint.getX());
				setVelocity(new Vector2D(0.8*Math.cos(viewAngle), 0.8*Math.sin(viewAngle)));
			}
 		}
 	}
 
 	private void generateNewWaypoint() {
 		Point2D lastLastWaypoint = lastWaypoint;
 		lastWaypoint = waypoint;
 		
 		//waypoint = new Point2D.Double(Math.random()*30, Math.random()*30);
 		int trials = 0;
 		do {
 			waypoint = sim.getEnv().generateNewWaypoint(getPoint2D());
 			System.out.println(waypoint+"\t"+lastLastWaypoint);
 			trials++;
 		} while (lastLastWaypoint != null && trials < 3 && waypoint.equals(lastLastWaypoint));
 		
 		// TODO generate new waypoint, if it is the same than the lastlast one.
 	}
 
 	private boolean reachedWaypoint() {
 		return waypoint == null || this.getPoint2D().distance(waypoint) < 0.25;
 	}
 
 	@Override
 	public ConvertedGraphics getConvertedGraphics() {
 		return cg;
 	}
 }
