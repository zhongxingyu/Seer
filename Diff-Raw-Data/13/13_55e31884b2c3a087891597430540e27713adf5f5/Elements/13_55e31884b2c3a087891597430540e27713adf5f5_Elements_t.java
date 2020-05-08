 package pbs.parser;
 
 import java.util.ArrayList;
 
 import jig.engine.util.Vector2D;
 
 import pbs.*;
 import pbs.Level.*;
 import pbs.Entity.*;
 import pbs.Animations.*;
 import pbs.parser.Statements.*;
 import pbs.Trigger.*;
 import pbs.Triggers.*;
 import pbs.parser.BooleanElements.*;
 import pbs.parser.ExpressionElements.*;
 
 public class Elements {
 
     public static String TRIGGER_IMAGE = "resources/pbs-spritesheet.png#blue_bullet";
     
     //entity descriptions... 
     public abstract static class ObjectDescription {
 	//returns an entity matching this description
 	//public abstract ALAYERTYPE getLayerType();
 	protected ArrayList<Param> paramlist;
 	protected ArrayList<Param> paramtemp;
 	public void addParam(Param p){
 	    if(p == null)
 		return;
 	    
 	    if(paramtemp == null)
 		paramtemp = new ArrayList<Param>();
 	    paramtemp.add(p);
 	}
        	public abstract void mutate(Level l);	
     }
 
     public static class TemplateDescription extends ObjectDescription {
 
 	String key;
 	public TemplateDescription(String s, ArrayList<Param> pl){ 
 	    key = s;
 	    paramlist = pl;
 	}
 	public void mutate(Level l){
 	    ObjectDescription od = l.getTemplate(key);
 	    for(Param p : paramlist){
 		od.addParam(p);
 	    }
 	    AddEntity ae = new AddEntity(od);
 	    ae.finalParams(paramtemp);
 	    paramtemp = null;
 	    l.addStatement(ae);
 	}
     }
 
     public static class TriggerDescription extends ObjectDescription {
 	//triggers have a list of statements that get added to the
 	//event queue when they are triggered
 	ArrayList<Statement> stmtlist;
 	Entity triggerType;
 	CustomTrigger triggerParam;
 
 	public TriggerDescription(ArrayList<Param> pl, ArrayList<Statement> sl){
 	    paramlist = pl;
 	    stmtlist = sl;
 	    triggerParam = null;
 	}
 
 	public void mutate(Level l){
 	    //this method SHOULD add a trigger with the specified stmtlist to the event layer
 	    //System.out.println("Trigger Description");
 	    
 	    Entity e = triggerType;
 	    	  
 	    if(paramlist != null)
 		for(int i = 0; i < paramlist.size(); i++){
 		    paramlist.get(i).mutate(e);
 		}
 
 	    if(paramtemp != null){
 		for(int i = 0; i < paramtemp.size(); i++){
 		    paramtemp.get(i).mutate(e);
 		}
 		paramtemp = null;
 	    }
 	    
 	    e.setCustomTrigger(triggerParam);
 
 	    l.add(e, Layer.TRIGGERS);
 	}
     }
 
     public static class timedTrigger extends TriggerDescription {
 	protected long timer;
 	public timedTrigger(long t, ArrayList<Param> pl, ArrayList<Statement> sl){ 
 	    super(pl, sl); 
 	    timer = t;
 	}
 
 	public void mutate(Level l){
 	    //System.out.print("Timed ");
 	    triggerType = new TimedTrigger(TRIGGER_IMAGE, timer);
 	    triggerParam = new Trigger(stmtlist);
 	    super.mutate(l);
 	}
     }
 
     public static class collisionTrigger extends TriggerDescription {
 	public collisionTrigger(ArrayList<Param> pl, ArrayList<Statement> sl){ 
 	    super(pl, sl);
 	}
 
 	public void mutate(Level l){
 	    //System.out.print("Collision ");
 	    triggerType = new CollisionTrigger(TRIGGER_IMAGE);
 	    triggerParam = new Trigger(stmtlist);
 	    super.mutate(l);
 	}
     }
 
     public static class onscreenTrigger extends TriggerDescription {
 	public onscreenTrigger(ArrayList<Param> pl, ArrayList<Statement> sl){ 
 	    super(pl, sl);
 	}
 
 	public void mutate(Level l){
 	    //System.out.print("Onscreen ");
 	    triggerType = new OnscreenTrigger(TRIGGER_IMAGE);
 	    triggerParam = new Trigger(stmtlist);
 	    super.mutate(l);
 	}
     }
 
 
     public static class EntityDescription extends ObjectDescription {
 
 	String imgsrc;
 	Layer targetLayer;
 
 	public EntityDescription(String img, ArrayList<Param> pl){
 	    imgsrc = img;
 	    paramlist = pl;
 	}
 
 	public void mutate(Level l){
 	    Entity e = new Entity(imgsrc);
 	    
 	    for(int i = 0; i < paramlist.size(); i++){
 		paramlist.get(i).mutate(e);
 	    }
 
 	    if(paramtemp != null){
 		for(int i = 0; i < paramtemp.size(); i++){
 		    paramtemp.get(i).mutate(e);
 		}
 		paramtemp = null;
 	    }
 	    
 	    l.add(e, targetLayer);
 
 	    //System.out.println("Entity Description");
 	}
     }
 
     public static class fxEntity extends EntityDescription {
 	public fxEntity(String img, ArrayList<Param> pl){ super(img, pl); }
 
 	public void mutate(Level l){
 	    //set layer
 	    targetLayer = Layer.FX;
 	    paramlist.add(new AnimationParam(new AnimateOnce(75)));
 	    //System.out.print("Fx ");
 	    super.mutate(l);
 	}
     }
 
     public static class enemyEntity extends EntityDescription {
 	public enemyEntity(String img, ArrayList<Param> pl){ super(img, pl); }
 
 	public void mutate(Level l){
 	    //set layer
 	    targetLayer = Layer.ENEMY;
 	    //System.out.print("Enemy ");
 	    super.mutate(l);
 	}
     }
 
     public static class staticEntity extends EntityDescription {
 	public staticEntity(String img, ArrayList<Param> pl){ super(img, pl); }
 
 	public void mutate(Level l){
 	    //set layer
 	    targetLayer = Layer.STATIC;
 	    //System.out.print("Static ");
 	    super.mutate(l);
 
 	}
     }
 
     //parameters for entities
     public interface Param {
 	public abstract boolean mutate(Entity e);
     }
 
     public static abstract class VectorParam implements Param {
 	Vector2D vec;
 	public VectorParam(Vector2D v){ vec = v; }
     }
 
     public static class VelocityParam extends VectorParam {
 	public VelocityParam(Vector2D v){ super(v); }
 	public boolean mutate(Entity e){
 	    e.setVelocity(vec);
 	    return true;
 	}
     }
     
     public static class PositionParam extends VectorParam {
 	public PositionParam(Vector2D p){ super(p); }
 	public boolean mutate(Entity e){
 	    e.setPosition(vec);
 	    return true;
 	}
     }
 
     public static class TranslatePositionParam extends VectorParam {
 	public TranslatePositionParam(Vector2D p){ super(p); }
 	public boolean mutate(Entity e){
 	    e.setPosition(e.getPosition().translate(vec));
 	    return true;
 	}
     }
     
     public static class ScoreValueParam implements Param {
 	int score;
 	public ScoreValueParam(int s){ score = s; }
 	public boolean mutate(Entity e){
 	    e.setValue(score);
 	    return true;
 	}
     } 
 
     public static class SpawnTime implements Param {
 	long time;
 	public SpawnTime(long t){ time = t; }
 	public boolean mutate(Entity e){
 	    e.setAge(-time * 1000); //sets age to negative, entity spawns at age > 0
 	    return true;
 	}
     }
 
     public static class UpdateParam implements Param {
 	CustomUpdate cu;
 	public UpdateParam(CustomUpdate u){ cu = u; }
 	public boolean mutate(Entity e){
 	    e.setCustomUpdate(cu);
 	    return true;
 	}
     }
 
     public static class RenderParam implements Param {
 	CustomRender cr;
 	public RenderParam(CustomRender r){ cr = r; }
 	public boolean mutate(Entity e){
 	    e.setCustomRender(cr);
 	    return true;
 	}
     }
 
     public static class WeaponParam implements Param {
 	CustomWeapon cw;
 	public WeaponParam(CustomWeapon w){ cw = w; }
 	public boolean mutate(Entity e){
 	    e.setCustomWeapon(cw);
 	    return true;
 	}
     }
 
     public static class AnimationParam implements Param {
 	CustomAnimation ca;
 	public AnimationParam(CustomAnimation a){ ca = a; }
 	public boolean mutate(Entity e){
 	    e.setCustomAnimation(ca);
 	    return true;
 	}
     }
 
 
    //these parameters need their own instance
    //because their behavior relies on the object state
    public static class SpinRenderParam implements Param {
	double dTheta;
	public SpinRenderParam(int dw){ dTheta = dw; }
	public boolean mutate(Entity e){
	    e.setCustomRender(new Spin(0.0, dTheta));
	    return true;
	}
    }
    


 
 }
