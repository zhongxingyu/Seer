 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.*;
 
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.Game;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Keyboard;
 import playn.core.Keyboard.Event;
 import playn.core.Keyboard.TypedEvent;
 
 public class TinyWorld implements Game, Keyboard.Listener {
 	ArrayList<Planetoid> planetoids;
 	World world;
 
 	@Override
 	public void init() {
 		planetoids = new ArrayList<Planetoid>();
 
 		// create and add background image layer
 		Image bgImage = assets().getImage("images/bg.png");
 		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
 		graphics().rootLayer().add(bgLayer);
 		
 		// Load grey asteroid image asset
 		Image asteroid = assets().getImage("images/grey-asteroid.png");
 
 		Vec2 pStart = new Vec2(20,20);
 		
 		//Set up the world
 		world = new World(new Vec2(), false);
 		
 		//Body Definition for a planetoid
 		BodyDef pBodyDef = new BodyDef();
 		pBodyDef.type = BodyType.DYNAMIC;
 		
 		//Need to multiply pStart by a Physics factor
		pBodyDef.position.set(pStart.mul(1/Constants.PHYS_RATIO));
 		
 		// Create a testing planetoid and add it to the arraylist
 		Planetoid p = new Planetoid(pStart, new Sprite(20, 20), pBodyDef, world);
 		p.getSprite().addFrame(asteroid);
 		graphics().rootLayer().add(p.getSprite().getImageLayer());
 		planetoids.add(p);
 	}
 
 	@Override
 	public void paint(float alpha) {
 		// the background automatically paints itself, so no need to do anything
 		// here!
 		
 	}
 
 	@Override
 	public void update(float delta) {
 		// For every planetoid update it's sprite
 		for (Planetoid p : planetoids) {
 			p.getSprite().update();
 		}
 	}
 
 	@Override
 	public int updateRate() {
 		return 25;
 	}
 
 	@Override
 	public void onKeyDown(Event event) {
 		
 		switch (event.key()) {
 		case ESCAPE:
 			//Quit
 		}
 		
 	}
 
 	@Override
 	public void onKeyTyped(TypedEvent event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onKeyUp(Event event) {
 		// TODO Auto-generated method stub
 		
 	}
 }
