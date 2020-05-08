 package behavior;
 
 import com.google.common.collect.ImmutableSet;
 
 import ability.BulletAbility;
 import ability.MachineBullet;
 import animation.AnimationPlayer;
 import animation.KeyframeTriggerEventArgs;
 import anotations.Editable;
 import math.Vector2;
 import components.AbilityComp;
 import components.AnimationComp;
 import components.InputComp;
 import components.PhysicsComp;
 import components.TransformationComp;
 import entityFramework.IEntity;
 
 import utils.IEventListener;
 import utils.input.Keyboard;
 import utils.input.Keys;
 import utils.input.Mouse;
 import utils.input.MouseButton;
 import utils.time.GameTime;
 /**
  * 
  * @author Joakim Johansson
  *
  */
 @Editable
 public class ChangelingAIScript extends Behavior{
 	private static final String IDLE_STATE = "IDLE_STATE";
 	private static final String WALKING_STATE = "WALKING_STATE";
 	private static final String JUMP_STATE = "JUMP_STATE";
 	private static final String FALLING_STATE = "FALLING_STATE";
 	private static final String SCANNING_STATE = "SCANNING_STATE";
 
 	private static final float JUMP_VELO = 600f;
 
 	private static final String CLOUD_ARCHETYPE_PATH = "dustcloud.archetype";
 
 	private AnimationComp animComp;
 	private PhysicsComp physComp;
 	private TransformationComp transComp;
 	private AbilityComp abComp;
 	private InputComp inpComp;
 
 	private IEntity targetEntity;
 
 	private BulletAbility bulletAbility;
 
 	private boolean mirroring;
 
 	@Editable
 	public float sightRange = 2000f;
 	@Editable
 	public float speed = 50f;
 	@Editable
 	public float minDistance = 700f;
 
 	@Editable
 	public String targetGroup = "Players";
 
 	@Override
 	public void start() {
 		this.transComp = this.Entity.getComponent(TransformationComp.class);
 		this.animComp = this.Entity.getComponent(AnimationComp.class);
 
 		this.mirroring = true;
 
 		this.StateMachine.registerState(SCANNING_STATE, new ScanningState());
 		this.StateMachine.registerState(IDLE_STATE, new IdleState());
 		this.StateMachine.registerState(WALKING_STATE, new WalkState());
 		this.StateMachine.registerState(JUMP_STATE, new JumpState());
 		this.StateMachine.registerState(FALLING_STATE, new FallingState());
 		this.StateMachine.changeState(FALLING_STATE);
 
 		if(animComp == null) {
 			this.animComp = new AnimationComp(); 
 			this.Entity.addComponent(this.animComp);
 		}
 		if(this.transComp == null) {
 			throw new NullPointerException("transComp");
 		}
 		if(physComp == null) {
 			this.physComp = new PhysicsComp();
 			this.Entity.addComponent(this.physComp);
 		}
 		if(abComp == null) {
 			this.abComp = new AbilityComp();
 			this.Entity.addComponent(this.abComp);
 		}
 
 		this.bulletAbility = new MachineBullet(this.ContentManager.loadArchetype("Bullet.archetype"), 1000, 0.3f);
 		this.bulletAbility.initialize(EntityManager, this.Entity);
 	}
 
 	@Override
 	public void update(GameTime time) {
 		super.update(time);
 
 		if(Math.random() <0.01)
 			mirroring = !mirroring;
 
 		if(targetEntity == null && !this.StateMachine.getActiveState().equals(FALLING_STATE)){
 			this.StateMachine.changeState(SCANNING_STATE);
 		}
 
 	}
 
 	private IEntity findNearestTarget(){
 		Vector2 position = this.Entity.getComponent(TransformationComp.class).getPosition();
 
 		IEntity nearestTarget = null;
 
 		for (IEntity player : this.EntityManager.getEntityGroup(this.targetGroup)) {
 			if(Vector2.distance(position, player.getComponent(TransformationComp.class).getPosition()) < this.sightRange){
 				if (nearestTarget == null ||
 						Vector2.distance(position, player.getComponent(TransformationComp.class).getPosition()) < 
 						Vector2.distance(position, nearestTarget.getComponent(TransformationComp.class).getPosition())){
 					nearestTarget = player;
 				}
 			}
 		}
 		return nearestTarget;
 	}
 	private IEntity findRandomTarget(){
 		Vector2 position = this.Entity.getComponent(TransformationComp.class).getPosition();
 
 		ImmutableSet<IEntity> listOfPlayers = EntityManager.getEntityGroup(targetGroup);
 		IEntity entity = listOfPlayers.asList().get((int) Math.random()*listOfPlayers.size());
 		if(Vector2.distance(position, entity.getComponent(TransformationComp.class).getPosition()) > this.sightRange){
 			return null;
 		}
 		return entity;
 	}
 
 	//	private void moveRandomly() {
 	//
 	//		double angle = this.physComp.getVelocity().angle() + (MathHelper.Tau / 40 - Math.random() * MathHelper.Tau / 20);
 	//
 	//		Vector2 rndV = new Vector2(MathHelper.sin(angle),MathHelper.cos(angle));
 	//		rndV = Vector2.mul(this.speed, rndV);
 	//		this.physComp.setVelocity(rndV);
 	//	}
 
 	private void mirrorInput() {
 		Keyboard keyboard = this.inpComp.getKeyboard();
 		Mouse mouse = this.inpComp.getMouse();
 
 		int speedFactor = 200;
 		if(keyboard.isKeyDown(inpComp.getGallopButton())){
 			speedFactor=400;
 		}
 
 		if(mouse.wasButtonPressed(MouseButton.Left)){
 			this.abComp.startAbility(bulletAbility);			
 		} else if(inpComp.getMouse().wasButtonReleased(MouseButton.Left)){
 			this.abComp.stopAbility(bulletAbility);
 		}	
 
 
 		Vector2 velocity = new Vector2(0,0);
 		if (keyboard.isKeyDown(inpComp.getBackButton())){
 			velocity=Vector2.add(velocity, new Vector2(0,1));
 		}
 		if (keyboard.isKeyDown(inpComp.getForwardButton())){
 			velocity=Vector2.add(velocity, new Vector2(0,-1));
 		}
 		if (keyboard.isKeyDown(inpComp.getRightButton())){
 			velocity=Vector2.add(velocity, new Vector2(-1,0));
 			transComp.setMirror(false);
 		}
 		if (keyboard.isKeyDown(inpComp.getLeftButton())){
 			velocity=Vector2.add(velocity, new Vector2(1,0));
 			transComp.setMirror(true);
 		}
 
 		if(velocity.length()!=0)
 			velocity=Vector2.norm(velocity);
 
 		velocity = Vector2.mul(speedFactor, velocity);
 		physComp.setVelocity(velocity);
 
 		if(keyboard.wasKeyPressed(Keys.Space)) {
 			jump();
 		}
 	}
 
 	private void jump() {
 		this.physComp.setHeightVelocity(JUMP_VELO);
 		this.StateMachine.changeState(JUMP_STATE);
 	}
 
 	private void moveTowardsTarget() {
 		Vector2 position = this.transComp.getPosition();
 		if(this.targetEntity.getComponent(TransformationComp.class)== null){
 			return;
 		}
 		Vector2 targetPosition = this.targetEntity.getComponent(TransformationComp.class).getPosition();
 		Vector2 dir = Vector2.subtract(targetPosition, position);
 		if(dir.length() != 0)
 			dir= Vector2.norm(dir);
 
 		this.physComp.setVelocity(Vector2.mul(this.speed, dir));
 		this.abComp.startAbility(bulletAbility);
 
 		if(this.physComp.getVelocity().X !=0){
 			this.transComp.setMirror(this.physComp.getVelocity().X > 0);
 		}
 	}
 
 	@Override
 	public Object clone() {
 		return new ChangelingAIScript();
 	}
 
 
 	private class ScanningState extends BehaviourState{
 		@Override
 		public void update(GameTime time) {		
 			if(targetEntity == null){
 				IEntity newTarget = findRandomTarget();
 				if(newTarget != null) {
 					targetEntity = newTarget;
 					StateMachine.changeState(IDLE_STATE);
 				}
 			} else {
 				StateMachine.changeState(IDLE_STATE);
 			}
 
 		}
 
 		@Override
 		public void exit(){
			AnimationPlayer animPlayer = targetEntity.getComponent(AnimationComp.class).getAnimationPlayer().clone();
 			Entity.getComponent(AnimationComp.class).setAnimationPlayer(animPlayer);
 			TransformationComp targetTransCom = targetEntity.getComponent(TransformationComp.class);
 			transComp.setScale(targetTransCom.getScale());
 			transComp.setRotation(targetTransCom.getRotation());
 			transComp.setOrigin(targetTransCom.getOrigin());
 			InputComp targetInpComp = targetEntity.getComponent(InputComp.class);
 			Entity.addComponent(targetInpComp);
 			inpComp = targetInpComp;
 			Entity.refresh();
 
 			final IEntity cloud = EntityManager.createEntity(ContentManager.loadArchetype(CLOUD_ARCHETYPE_PATH));
 			cloud.getComponent(TransformationComp.class).setPosition(transComp.getPosition());
 			cloud.getComponent(AnimationComp.class).getAnimationPlayer().addKeyframeTriggerListener(new IEventListener<KeyframeTriggerEventArgs>() {
 				@Override
 				public void onEvent(Object sender, KeyframeTriggerEventArgs e) {
 					if (e.triggerString.equals("kill"))
 						cloud.kill();
 				}
 			});
 		}
 	}
 
 
 	private class FallingState extends BehaviourState{
 		@Override
 		public void onGroundCollision() {
 			StateMachine.changeState(SCANNING_STATE);
 		}
 	}
 
 	private class IdleState extends BehaviourState {
 		@Override
 		public void enter() {
 			animComp.changeAnimation("idle", false );
 		}
 
 		@Override
 		public void update(GameTime time) {
 			if(mirroring){
 				mirrorInput();
 			}else{
 				if(targetEntity != null)
 					moveTowardsTarget();
 			}
 			Vector2 velocity = physComp.getVelocity();
 			if(!velocity.equals(Vector2.Zero)) {
 				StateMachine.changeState(WALKING_STATE);
 			}
 		}
 
 	}
 
 	private class WalkState extends BehaviourState {
 		@Override
 		public void enter() {
 			animComp.changeAnimation("walk", false);	
 		}
 
 		@Override
 		public void update(GameTime time) {
 			if(mirroring){
 				mirrorInput();
 			}else{
 				if(targetEntity != null)
 					moveTowardsTarget();
 			}
 			Vector2 velocity = physComp.getVelocity();
 			if(velocity.equals(Vector2.Zero)) {
 				System.out.println("idle");
 				StateMachine.changeState(IDLE_STATE);
 			}		
 		}
 	}
 
 	private class JumpState extends BehaviourState {
 		@Override
 		public void enter() {
 			animComp.changeAnimation("jump", false);
 		}
 
 		@Override
 		public void onGroundCollision() {
 			StateMachine.changeState(IDLE_STATE);
 		}
 
 		@Override
 		public void onTriggerEnter(IEntity other) {
 			if(physComp.getHeightVelocity() < 0) {
 				SoundManager.playSoundEffect("effects/boing.ogg");
 				animComp.changeAnimation("jump", false);
 				physComp.setHeightVelocity(600.0f);
 			}
 		}
 
 		@Override
 		public void update(GameTime time) {
 			if(mirroring){
 				mirrorInput();
 			}else{
 				if(targetEntity != null)
 					moveTowardsTarget();
 			}
 		}
 	}
 }
