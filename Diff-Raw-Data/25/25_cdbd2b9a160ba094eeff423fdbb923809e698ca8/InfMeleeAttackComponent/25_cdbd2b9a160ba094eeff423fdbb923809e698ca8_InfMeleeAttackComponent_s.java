 package com.liongrid.infectosaurus.components;
 
 import android.util.Log;
 
 import com.liongrid.gameengine.Component;
 import com.liongrid.gameengine.tools.FixedSizeArray;
 import com.liongrid.infectosaurus.GameActivity;
 import com.liongrid.infectosaurus.InfectoGameObjectHandler;
 import com.liongrid.infectosaurus.Team;
 import com.liongrid.infectosaurus.InfectoGameObject;
 import com.liongrid.infectosaurus.components.SpriteComponent.SpriteState;
 import com.liongrid.infectosaurus.effects.InfectedDamageEffect;
 import com.liongrid.infectosaurus.effects.SpeedBuffEffect;
 
 
 public class InfMeleeAttackComponent extends Component<InfectoGameObject> {
 
 	InfectoGameObjectHandler gameObjHandler;
 	static final int CLOSE_CAPACITY = 20;
 	FixedSizeArray<InfectoGameObject> close;
	int mReach = 80^2; //Square of the actual mReach 100 means 10 px
 	private int mDamage = 1;
 	private float mDelay = 0.5f; //sec
 	private float mDelayCountDown = 0;
 	private float mInfectChance = 0f;
 	
 	
 	public InfMeleeAttackComponent(){
 		super();
 		close = new FixedSizeArray<InfectoGameObject>(CLOSE_CAPACITY);
 		set();
 		
 	}
 	
 	public void set(){
 		gameObjHandler = GameActivity.infectoPointers.gameObjectHandler;
 	}
 	
 	public void addToDamage(int value){
 		mDamage += value;
 	}
 	
 	@Override
 	public void update(float dt, InfectoGameObject parent) {
 		
 		mDelayCountDown -= dt;
 		if(mDelayCountDown > 0) return;
 		
 		InfectoGameObject target =	gameObjHandler.mCH.getClosest(parent.pos, 
 			parent.team == Team.Human ? Team.Alien.ordinal() : Team.Human.ordinal());
 
 		
 		if(target == null || 
				target.pos.distance2(parent.pos) > mReach){
 			return;
 		}
 			
 		
 		mDelayCountDown = mDelay;
 		
 		
 		//TODO pool this!!
 		
 		InfectedDamageEffect eff = new InfectedDamageEffect();
 		eff.set(mDamage, mInfectChance );
 		target.afflict(eff);
 		
 		SpeedBuffEffect speed = new SpeedBuffEffect();
 		speed.set(1f, 1, 100);
 		target.afflict(speed);
 		
 		
 		SpriteComponent spr = (SpriteComponent) parent.findComponentOfType(SpriteComponent.class);
 		if(spr == null) {
 			Log.d("Infectosaurus", "Could not find SpriteComponent... that`s odd");
 			return;
 		}
 		
 		spr.currentState = SpriteState.attacking;
 		
 	}
 
 	/**
 	 * Sets the infect chance
 	 * @param infectChance - 1 = 100% infect chance.
 	 */
 	public void setInfectChance(float infectChance) {
 		this.mInfectChance = infectChance;
 		
 	}
 }
