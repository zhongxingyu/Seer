 package com.crystalclash.renders;
 
 import java.util.Random;
 
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.utils.Array;
 import com.crystalclash.CrystalClash;
 import com.crystalclash.accessors.ActorAccessor;
 import com.crystalclash.renders.helpers.ResourceHelper;
 
 public class ParallaxRender extends Group {
 	private static ParallaxRender instance;
 	private Array<ParallaxLevel> levels;
 	private Random rand;
 
 	private boolean logInLoaded = false;
 	private boolean gmesListLoaded = false;
 	private float bg_y;
 
 	public static ParallaxRender getInstance() {
 		if (instance == null)
 			instance = new ParallaxRender();
 		return instance;
 	}
 
 	private ParallaxRender() {
 		rand = new Random();
 		levels = new Array<ParallaxLevel>();
 		setBounds(0, -854, 1280, 1708);
 		addLevel(new BackgroundParallaxLevel(ResourceHelper.getTexture("menu/background"), -427, 0.01f, 0.01f));
 		setColor(getColor().r, getColor().g, getColor().b, 0);
 	}
 
 	public void loadLogIn() {
 		if (!logInLoaded) {
 			addLevel(new NimbusParallaxLevel(ResourceHelper.getTexture("menu/nimbus1"), 1400, -(CrystalClash.WIDTH + rand.nextInt(500)), -0.6f, 0.1f));
 			addLevel(new NimbusParallaxLevel(ResourceHelper.getTexture("menu/nimbus2"), 1200, -(CrystalClash.WIDTH + rand.nextInt(500)), -1f, 0.2f));
 			addLevel(new NimbusParallaxLevel(ResourceHelper.getTexture("menu/nimbus3"), 1000, -(CrystalClash.WIDTH + rand.nextInt(500)), -2f, 0.15f));
 			logInLoaded = true;
 		}
 	}
 
 	public void loadGamesList() {
 		if (!gmesListLoaded) {
 			addLevel(new CloudParallaxLevel(ResourceHelper.getTexture("menu/level1"), -427, 0, -0.1f, 0.05f));
 			addLevel(new CloudParallaxLevel(ResourceHelper.getTexture("menu/level2"), -427, 0, -0.3f, 0.09f));
 			addLevel(new CloudParallaxLevel(ResourceHelper.getTexture("menu/level3"), -427, 0, -0.5f, 0.12f));
 
			addLevel(new NimbusParallaxLevel(ResourceHelper.getTexture("menu/nimbus4"), 600, -rand.nextInt((int) (CrystalClash.WIDTH + 500)), -0.4f, 0.08f));
 			addLevel(new NimbusParallaxLevel(ResourceHelper.getTexture("menu/nimbus5"), 300, -rand.nextInt((int) (CrystalClash.WIDTH + 500)), -0.6f, 0.05f));
 
 			gmesListLoaded = true;
 		}
 	}
 
 	public Timeline pushMoveToLogin(Timeline t) {
 		return t.push(Tween.to(this, ActorAccessor.Y, CrystalClash.SLOW_ANIMATION_SPEED)
 				.target(-854));
 	}
 
 	public Timeline pushMoveToGamesList(Timeline t) {
 		return t.push(Tween.to(this, ActorAccessor.Y, CrystalClash.SLOW_ANIMATION_SPEED)
 				.target(0));
 	}
 
 	public Timeline pushMoveToGame(Timeline t) {
 		return t.push(Tween.to(this, ActorAccessor.Y, CrystalClash.SLOW_ANIMATION_SPEED)
 				.target(854));
 	}
 
 	private void addLevel(ParallaxLevel level) {
 		addActor(level);
 		levels.add(level);
 	}
 
 	public Timeline pushShow(Timeline t) {
 		return t.push(Tween.to(this, ActorAccessor.ALPHA, CrystalClash.NORMAL_ANIMATION_SPEED).target(1));
 	}
 
 	public Timeline pushHide(Timeline t) {
 		return t.push(Tween.to(this, ActorAccessor.ALPHA, CrystalClash.NORMAL_ANIMATION_SPEED).target(0));
 	}
 
 	public void updateY(float dy) {
 		bg_y = dy;
 	}
 
 	@Override
 	public void act(float delta) {
 		for (int i = 0; i < levels.size; i++) {
 			levels.get(i).update(bg_y * 0.1f);
 		}
 		super.act(delta);
 	}
 }
