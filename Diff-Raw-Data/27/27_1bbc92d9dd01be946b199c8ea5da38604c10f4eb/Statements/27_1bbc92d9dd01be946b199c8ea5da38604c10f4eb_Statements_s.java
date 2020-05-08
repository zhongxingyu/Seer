 package pbs.parser;
 
 import java.util.ArrayList;
 
 import jig.engine.util.Vector2D;
 
 import pbs.*;
 import pbs.parser.Elements.*;
 import pbs.parser.BooleanElements.*;
 import pbs.parser.ExpressionElements.*;
 
 
 public class Statements {
 
     //statements are top level commands that alter the level data
     //upon execution, set level parameters, and add entities
     //the event queue in the level is a series of statements
     public interface Statement {
 	//allows a list of final params to be set, specific to this statement
 	public abstract void finalParams(ArrayList<Param> plist);
 	//behavior of this statement when executing...
 	public abstract boolean execute(Level l);
     }
 
     //if the conditional is satisfied, executes a statement on execute
     public static class Conditional implements Statement {
 	
 	BooleanOperation boolop;
 	Statement stmt;
 
 	public Conditional(BooleanOperation b, Statement s){
 	    boolop = b;
 	    stmt = s;
 	}
 
 	public void finalParams(ArrayList<Param> plist){
 	    stmt.finalParams(plist);
 	}
 
 	public boolean execute(Level l){
 	    boolean b = boolop.eval();
 	    
 	    if(b){
 		stmt.execute(l);
 	    }
 
 	    return b;
 	}
     }
 
     //addEntity adds an object to the level on execute
     public static class AddEntity implements Statement {
 
 	protected ObjectDescription theObject;
 	
 	public AddEntity(ObjectDescription od){
 	    theObject = od;
 	}
 	
 	public void setDescription(ObjectDescription od){
 	    theObject = od;
 	}
 
 	public void finalParams(ArrayList<Param> plist){
	    for(Param p : plist){
		theObject.addParam(p);
	    }
 	}
 
 	//does the work of adding events to levels
 	public boolean execute(Level l){
 	    System.out.println("Creation event!");
 	    if(theObject != null)
 		theObject.mutate(l);
 
 	    return true;
 	}
     }
 
 
     //adds a template to the level template hash (entity symbol table) on execute
     public static class AddTemplate extends AddEntity {
 	
 	String name;
 
 	public AddTemplate(String s, ObjectDescription od){
 	    super(od);
 	    name = s;
 	}
 
 	public boolean execute(Level l){
 	    //adds new 'class template' to level
 	    System.out.println("[Add template <" + name + 
 			       "> to level template hash] ");
 	    l.addTemplate(name, theObject);
 	    return true;
 	}
 
 	public void setName(String n){
 	    name = n;
 	}
 
     }
 
 
     //setter statements, these all set some parameter of the level
     public abstract static class Setter implements Statement {
 	public Setter(){}
 	public void finalParams(ArrayList<Param> plist){}
     }
 
     /*
       Each of the below methods get created by lines in the level spec...
       Adding a type here means adding a new method in the parser, and a switch
       to invoke it. 
       Usually they will be attached to triggers, and execute when the trigger fires. 
       They may also be used by themselves to initialize the level. 
      */
 
     public static abstract class VectorSetter extends Setter {
 	Vector2D vec;
 	public VectorSetter(Vector2D v) { vec = v; }
 	public abstract boolean execute(Level l);
     }
 
     //Sets the Camera position
     public static class CameraSetter extends VectorSetter {
 	public CameraSetter(Vector2D c) { super(c); }
 	public boolean execute(Level l) { l.setCam(vec); return true; }
     }
 
 
     //sets the scroll vector
     public static class ScrollSetter extends VectorSetter {
 	public ScrollSetter(Vector2D c) { super(c); }
 	public boolean execute(Level l) { l.setScrollSpeed(vec); return true; }
     }
 
     //modifies the score
     public static class ScoreModifier extends Setter {
 	int mod;
 	public ScoreModifier(int m) { mod = m; }
 	public boolean execute(Level l) { l.modScore(mod); return true; }
     }
 
     public static class LevelEnder extends Setter {
 	//way of signalling the level is over
 	//perhaps we should have a successor file to 
 	//specify a successor level
 	public LevelEnder() {}
 	public boolean execute(Level l) { return true; }
     }
 
 }
