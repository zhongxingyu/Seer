 package com.opg.javagamexyz.artemis.systems;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.EntitySystem;
 import com.artemis.annotations.Mapper;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.opg.javagamexyz.artemis.components.Position;
 import com.opg.javagamexyz.artemis.components.Sprite;
 
 public class SpriteRenderSystem extends EntitySystem {
 	@Mapper protected ComponentMapper<Position> pm;
 	@Mapper protected ComponentMapper<Sprite> sm;
 	
 	protected OrthographicCamera camera;
 	protected SpriteBatch batch;
 	
 	protected TextureAtlas atlas;
 	protected HashMap<String, AtlasRegion> regions;
 	protected Bag<AtlasRegion> regionsByEntity;
 	
 	protected List<Entity> sortedEntities;
 	
 	@SuppressWarnings("unchecked")
 	public SpriteRenderSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Position.class, Sprite.class));
 		
 		this.camera = camera;
 	}
 	
 	@Override
 	protected void initialize() {
 		batch = new SpriteBatch();
 		
 		atlas = new TextureAtlas(Gdx.files.internal("textures/pack.atlas"), Gdx.files.internal("textures"));
 		regions = new HashMap<String, AtlasRegion>();
 		for (AtlasRegion region : atlas.getRegions()) {
 			regions.put(region.name, region);
 		}
 		regionsByEntity = new Bag<AtlasRegion>();
 		
 		sortedEntities = new ArrayList<Entity>();
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 	
 	@Override
 	protected void begin() {
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 	}
 	
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		for (Entity entity : sortedEntities) {
 			process(entity);
 		}
 	}
 
 	protected void process(Entity e) {
		// TODO is this necessary?
		if (pm.has(e)) {
			// TODO getSafe(...) necessary?
			Position position = pm.getSafe(e);
 			Sprite sprite = sm.get(e);
 			
 			AtlasRegion region = regionsByEntity.get(e.getId());
 			batch.setColor(sprite.r, sprite.g, sprite.b, sprite.a);
 			
 			float posX = position.x - region.getRegionWidth() / 2 * sprite.scaleX;
 			float posY = position.y - region.getRegionHeight() / 2 * sprite.scaleY;
 			
 			batch.draw(
 				region,
 				posX, posY,
 				0, 0,
 				region.getRegionWidth(), region.getRegionHeight(),
 				sprite.scaleX, sprite.scaleY,
 				sprite.rotation
 			);
		}
 	}
 	
 	@Override
 	protected void end() {
 		batch.end();
 	}
 	
 	@Override
 	protected void inserted(Entity e) {
 		Sprite sprite = sm.get(e);
 		regionsByEntity.set(e.getId(), regions.get(sprite.name));
 		
 		sortedEntities.add(e);
 		
 		Collections.sort(sortedEntities, new Comparator<Entity>() {
 			@Override
 			public int compare(Entity e1, Entity e2) {
 				Sprite s1 = sm.get(e1);
 				Sprite s2 = sm.get(e2);
 				return s1.layer.compareTo(s2.layer);
 			}
 		});
 	}
 	
 	@Override
 	protected void removed(Entity e) {
		// TODO why not regionsByEntity.remove(e.getId());
 		regionsByEntity.set(e.getId(), null);
 		sortedEntities.remove(e);
 	}
 }
