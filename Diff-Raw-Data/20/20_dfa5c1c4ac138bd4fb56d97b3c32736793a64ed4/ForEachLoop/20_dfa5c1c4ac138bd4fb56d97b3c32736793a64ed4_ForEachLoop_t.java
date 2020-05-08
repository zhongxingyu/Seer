 package asteroids.model.programs.statements;
 
 import java.util.*;
 
 import be.kuleuven.cs.som.annotate.*;
 import asteroids.model.*;
 import asteroids.model.programs.*;
 import asteroids.model.programs.expressions.*;
 import asteroids.model.programs.parsing.ProgramFactory.*;
 
 public class ForEachLoop extends ComplexStatement {
 
 	private ForeachType type;
 	private String variableName;
 	private Statement body;
 	private ProgramController controller;
 	
 	public ForEachLoop(int line, int column, ForeachType type, String variableName,
 						Statement body, ProgramController controller){
 		super(line,column);
 		this.type = type;
 		this.variableName = variableName;
 		this.body = body;
 		this.controller = controller;
 	}
 	
 	@Basic
 	public ForeachType getType(){
 		return this.type;
 	}
 	
 	@Basic
 	public String getVariableName(){
 		return this.variableName;
 	}
 	
 	@Basic
 	public Statement getBody(){
 		return this.body;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void execute() { 
 		Variable global = this.controller.getProgram().getGlobal(getVariableName());
 		if(body != null) {
 			World world = controller.getProgram().getShipRunningProgram().getWorld();
 			Set<SpaceObject> objects;
 			
 			if(this.getType().equals(ForeachType.ANY)) 
 				objects = world.getSpaceObjects();
 			else if(this.getType().equals(ForeachType.ASTEROID))
 				objects = (Set<SpaceObject>)world.getObjects(Asteroid.class);
 			else if(this.getType().equals(ForeachType.BULLET))
 				objects = (Set<SpaceObject>) world.getObjects(Bullet.class);
 			else
 				objects = (Set<SpaceObject>) world.getObjects(Ship.class);
 			
 			for(SpaceObject object: objects) {
 				global.setValue(new EntityLiteral(this.getLine(),this.getColumn(),object));
 				this.getBody().execute();
 			}
 		}
 		setFinished(true);
 	}
 
 	@Override
 	public boolean typeCheck() {
 		if(this.getBody().containsActionStatement())
 			return false;
 		Variable global = this.controller.getProgram().getGlobal(getVariableName());
 		if(global == null || !global.getType().equals(Type.ENTITY))
 			return false;
		global.setWillBeAssigned(true);
 		if(!this.getBody().typeCheck())
 			return false;
 		return true;
 	}
 	
 	@Override
 	public boolean containsActionStatement() {
 		return this.getBody().containsActionStatement();
 	}
 	
 	@Override
 	public void reset() {
 		super.reset();
 		this.getBody().reset();
 	}
 
 }
