 package com.razh.laser.tests;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
 import static com.razh.laser.Actor3D.*;
 import static com.razh.laser.sprites.DashedRingSpriteActor.segmentAngleTo;
 import static com.razh.laser.sprites.DashedRingSpriteActor.segmentSpacingTo;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g3d.decals.Decal;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.razh.laser.ActorContainer;
 import com.razh.laser.AnimationActor;
 import com.razh.laser.DecalActor;
 import com.razh.laser.Geometry;
 import com.razh.laser.MeshActor;
 import com.razh.laser.Shader;
 import com.razh.laser.ShaderGroup;
 import com.razh.laser.ShaderStage;
 import com.razh.laser.TransformActorContainer;
 import com.razh.laser.components.LaserComponent;
 import com.razh.laser.components.MissileComponent;
 import com.razh.laser.components.MissilePathComponent;
 import com.razh.laser.entities.Entity;
 import com.razh.laser.entities.EntityFactory;
 import com.razh.laser.sprites.ArcSpriteActor;
 import com.razh.laser.sprites.CircleSpriteActor;
 import com.razh.laser.sprites.DashedRingSpriteActor;
 import com.razh.laser.sprites.ProceduralSpriteActor;
 import com.razh.laser.sprites.SpriteActor;
 import com.razh.laser.sprites.SpriteActor.Origin;
 
 public class SimpleStageTest extends StageTest {
 
 	@Override
 	public void load(ShaderStage stage) {
 
		stage.setColor(new Color(30 / 255.0f, 24 / 255.0f, 47 / 255.0f, 1.0f));
 
 		float halfWidth = 0.5f * Gdx.graphics.getWidth();
 		float halfHeight = 0.5f * Gdx.graphics.getHeight();
 
 		Texture texture = new Texture("data/test_ship.png");
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		Entity entity = EntityFactory.createEmitter();
 		stage.addActor(entity.getActor());
 		entity.getActor().addAction(
 			forever(
 				rotateBy(360.0f, 1.0f)
 			)
 		);
 
 		Entity entity2 = EntityFactory.createEmitter();
 		stage.addActor(entity2.getActor());
 
 		Entity entity3 = EntityFactory.createCircleThing();
 		stage.addActor(entity3.getActor());
 
 		ShaderGroup circleSpriteGroup = new ShaderGroup();
 		ShaderProgram circleShader = Shader.createCircleShader();
 		circleSpriteGroup.setShaderProgram(circleShader);
 		CircleSpriteActor circleSprite = new CircleSpriteActor();
 		circleSprite.setColor(0.5f, 0.0f, 0.0f, 0.2f);
 		circleSprite.setWidth(180.0f);
 		circleSprite.setHeight(180.0f);
 //		circleSprite.setPosition(Gdx.graphics.getWidth() * 0.1f + 128f, Gdx.graphics.getHeight() * 0.1f + 128f);
 		circleSprite.setPosition(0,0);
 		circleSprite.addAction(
 			forever(
 				parallel(
 					sequence(
 						color(new Color(0.75f, 0.0f, 0.0f, 0.5f), 2.0f),
 						color(new Color(0.25f, 0.0f, 0.0f, 0.5f), 2.0f)
 					),
 					sequence(
 						repeat(
 							2,
 							sizeBy(100f, 100f, 4.0f, Interpolation.linear)
 						),
 						repeat(
 							2,
 							sizeBy(-100f, -100f, 4.0f, Interpolation.linear)
 						)
 					)
 				)
 			)
 		);
 		circleSpriteGroup.addActor(circleSprite);
 		stage.addActor(circleSpriteGroup);
 
 		ShaderGroup rectSpriteGroup = new ShaderGroup();
 		rectSpriteGroup.setShaderProgram(Shader.createRectangleShader());
 
 		ProceduralSpriteActor topLeftActor = new ProceduralSpriteActor();
 		topLeftActor.setColor(0.0f, 1.0f, 0.0f, 1.0f);
 		topLeftActor.setWidth(40.0f);
 		topLeftActor.setHeight(40.0f);
 		topLeftActor.setPosition(-halfWidth, halfHeight);
 //		topLeftActor.setPosition(0, 0);
 		rectSpriteGroup.addActor(topLeftActor);
 
 		ProceduralSpriteActor topRightActor = new ProceduralSpriteActor();
 		topRightActor.setColor(0.0f, 1.0f, 0.0f, 1.0f);
 		topRightActor.setWidth(40.0f);
 		topRightActor.setHeight(40.0f);
 		topRightActor.setPosition(halfWidth, halfHeight);
 		rectSpriteGroup.addActor(topRightActor);
 
 		ProceduralSpriteActor bottomLeftActor = new ProceduralSpriteActor();
 		bottomLeftActor.setColor(0.0f, 1.0f, 0.0f, 1.0f);
 		bottomLeftActor.setWidth(40.0f);
 		bottomLeftActor.setHeight(40.0f);
 		bottomLeftActor.setPosition(-halfWidth, -halfHeight);
 		rectSpriteGroup.addActor(bottomLeftActor);
 
 		ProceduralSpriteActor bottomRightActor = new ProceduralSpriteActor();
 		bottomRightActor.setColor(0.0f, 1.0f, 0.0f, 1.0f);
 		bottomRightActor.setWidth(40.0f);
 		bottomRightActor.setHeight(40.0f);
 		bottomRightActor.setPosition(halfWidth, -halfHeight);
 		rectSpriteGroup.addActor(bottomRightActor);
 
 		stage.addActor(rectSpriteGroup);
 
 		ShaderGroup dashedRingSpriteGroup = new ShaderGroup();
 		Shader.smooth = true;
 		dashedRingSpriteGroup.setShaderProgram(Shader.createDashedRingShader());
 		Shader.smooth = false;
 
 		DashedRingSpriteActor dashedRing = new DashedRingSpriteActor();
 		dashedRing.setColor(0.0f, 1.0f, 1.0f, 0.15f);
 		dashedRing.setWidth(200.0f);
 		dashedRing.setHeight(200.0f);
 		dashedRing.setPosition(-0.3f * halfWidth, -0.3f * halfHeight);
 		dashedRing.setOuterRadius(0.5f);
 		dashedRing.setInnerRadius(0.4f);
 		dashedRing.setSegmentAngle(120.0f);
 		dashedRing.setSegmentSpacing(10.0f);
 		dashedRing.addAction(
 			sequence(
 				segmentAngleTo(36.0f, 2.0f),
 				segmentAngleTo(120.0f, 2.0f),
 				segmentSpacingTo(22.5f, 1.0f),
 				forever(
 					parallel(
 						rotateBy(-180.0f, 1.0f),
 						sequence(
 							scaleTo(2.0f, 2.0f, 2.0f),
 							scaleTo(1.0f, 1.0f, 2.0f)
 						)
 					)
 				)
 			)
 		);
 		dashedRingSpriteGroup.addActor(dashedRing);
 
 		stage.addActor(dashedRingSpriteGroup);
 
 		ArcSpriteActor arcSprite = new ArcSpriteActor();
 		arcSprite.setColor(1.0f, 1.0f, 1.0f, 0.7f);
 		arcSprite.setWidth(300.0f);
 		arcSprite.setHeight(300.0f);
 		arcSprite.setOuterRadius(0.5f);
 		arcSprite.setInnerRadius(0.4f);
 		arcSprite.setLeftAngle(45.0f);
 		arcSprite.setRightAngle(45.0f);
 		arcSprite.setPosition(-0.25f * halfWidth, 0.0f * halfHeight);
 
 		CircleSpriteActor circle = new CircleSpriteActor();
 		circle.setColor(0.8f, 0.0f, 0.0f, 1.0f);
 		circle.setRadius(100.0f);
 
 		MeshActor mesh = new MeshActor();
 		mesh.setMesh(Geometry.createCircle(6));
 		mesh.setMode(GL20.GL_TRIANGLE_FAN);
 		mesh.setColor(0.5f, 0.0f, 0.0f, 1.0f);
 		mesh.setOrigin(200.0f, 0.0f);
 		mesh.setWidth(20.0f);
 		mesh.setHeight(20.0f);
 
 		SpriteActor shipSprite = new SpriteActor();
 		shipSprite.setPosition(-0.25f * halfWidth, 0.25f * halfHeight);
 		shipSprite.setWidth(300.0f);
 		shipSprite.setHeight(300.0f);
 		shipSprite.setColor(1.0f, 0.0f, 0.0f, 1.0f);
 
 		shipSprite.setSprite(new Sprite(texture));
 		stage.addActor(shipSprite);
 
 		SpriteActor shipSprite2 = new SpriteActor();
 		shipSprite2.setPosition(0.25f * halfWidth, -0.25f * halfHeight);
 		shipSprite2.setWidth(300.0f);
 		shipSprite2.setHeight(300.0f);
 		shipSprite2.setOrigin(Origin.BOTTOM_LEFT);
 		shipSprite2.addAction(forever(rotateBy(180.0f, 2.0f)));
 
 		Texture texture2 = new Texture("data/test_ship2.png");
 		texture2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		shipSprite2.setSprite(new Sprite(texture2));
 		stage.addActor(shipSprite2);
 
 		ActorContainer container = new TransformActorContainer();
 		container.addActor(arcSprite);
 		container.addActor(circle);
 		container.addActor(mesh);
 
 		stage.addActorContainer(container);
 		container.addAction(
 			parallel(
 				forever(
 					rotateBy(-1800.0f, 10.0f)
 				),
 				forever(
 					sequence(
 						moveBy(700.0f, 0.0f, 5.0f),
 						moveBy(-700.0f, 0.0f, 5.0f)
 					)
 				),
 				sequence(
 					scaleTo(2, 2, 5.0f),
 					scaleTo(1.0f, 1.0f, 5.0f),
 					removeActor()
 				)
 			)
 		);
 
 		DecalActor decal = new DecalActor();
 		decal.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decal.setWidth(200.0f);
 		decal.setHeight(200.0f);
 		decal.setPosition(0.3f * halfWidth, -0.3f * halfHeight, -100.0f);
 		decal.addAction(
 			forever(
 				rotateBy3D(0.0f, 0.0f, 180.0f, 2.0f)
 			)
 		);
 		stage.addActor(decal);
 
 		// Second decal.
 		final DecalActor decal2 = new DecalActor();
 		decal2.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decal2.setWidth(200.0f);
 		decal2.setHeight(200.0f);
 		decal2.setColor(0.5f, 0.5f, 1.0f, 0.5f);
 		decal2.setPosition(0.3f * halfWidth, 0.4f * halfHeight, 100.0f);
 		decal2.addAction(
 			forever(
 				sequence(
 					rotateTo3D(180.0f, 0.0f, 0.0f, 1.0f),
 					rotateTo3D(360.0f, 0.0f, 0.0f, 1.0f),
 					rotateTo3D(0.0f, 0.0f, 0.0f, 1.0f),
 					new Action() {
 						int frame = 0;
 						@Override
 						public boolean act(float delta) {
 							switch (frame % 4) {
 								case 0:
 									decal2.setOriginX(decal2.getOriginX() + 100.0f);
 									break;
 
 								case 1:
 									decal2.setOriginY(decal2.getOriginY() - 100.0f);
 									break;
 
 								case 2:
 									decal2.setOriginX(decal2.getOriginX() - 100.0f);
 									break;
 
 								case 3:
 									decal2.setOriginY(decal2.getOriginY() + 100.0f);
 									break;
 							}
 							frame++;
 
 							return true;
 						}
 					}
 				)
 			)
 		);
 
 		stage.addActor(decal2);
 
 		// Third decal.
 		DecalActor decal3 = new DecalActor();
 		decal3.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decal3.setWidth(200.0f);
 		decal3.setHeight(200.0f);
 		decal3.setPosition(-0.5f * halfWidth, 0.4f * halfHeight, 100.0f);
 		decal3.addAction(
 			forever(
 				rotateBy3D(0.0f, 180.0f, 0.0f, 2.0f)
 			)
 		);
 
 		stage.addActor(decal3);
 
 		// Fourth/fifth decal (test overlap).
 		DecalActor decal4 = new DecalActor();
 		decal4.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decal4.setWidth(200.0f);
 		decal4.setHeight(200.0f);
 		decal4.setOrigin(50.0f, 0.0f, 5.0f);
 		decal4.setPosition(-0.5f * halfWidth, -0.5f * halfHeight, 100.0f);
 
 		stage.addActor(decal4);
 
 		DecalActor decal5 = new DecalActor();
 		decal5.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decal5.setWidth(200.0f);
 		decal5.setHeight(200.0f);
 		decal5.setOrigin(-50.0f, 0.0f, -5.0f);
 		decal5.setPosition(-0.5f * halfWidth, -0.5f * halfHeight, 100.0f);
 
 		stage.addActor(decal5);
 
 		Entity missile = EntityFactory.createMissile(0);
 		MissileComponent missileComponent = (MissileComponent) missile.getComponentOfType(MissileComponent.class);
 		missileComponent.getPhysicsComponent().setVelocity(0.0f, -500.0f);
 		missileComponent.getTargetComponent().setTarget(shipSprite2);
 
 		stage.addActorContainer((ActorContainer) missile.getActor());
 
 		// Add missile path actors.
 		MissilePathComponent missilePath = (MissilePathComponent) missile.getComponentOfType(MissilePathComponent.class);
 		ActorContainer pathActors = missilePath.getPathActors();
 		stage.addActorContainer(pathActors);
 
 		// Second missile.
 		Entity missile2 = EntityFactory.createMissile(0);
 		missileComponent = (MissileComponent) missile2.getComponentOfType(MissileComponent.class);
 		missileComponent.getPhysicsComponent().setVelocity(-800.0f, -300.0f);
 		missileComponent.getTargetComponent().setTarget(shipSprite2);
 		missile2.getActor().setPosition(Gdx.graphics.getWidth() * -0.4f, Gdx.graphics.getHeight() * 0.2f);
 
 		stage.addActorContainer((ActorContainer) missile2.getActor());
 
 		// Add missile path actors.
 		missilePath = (MissilePathComponent) missile2.getComponentOfType(MissilePathComponent.class);
 		pathActors = missilePath.getPathActors();
 		stage.addActorContainer(pathActors);
 
 		// Test SpriteActor Origin enum.
 		SpriteActor testPath = new SpriteActor();
 		testPath.setSprite(new Sprite(new Texture("data/missile_path.png")));
 		testPath.setPosition(-0.7f * halfWidth, -0.7f * halfHeight);
 		testPath.setWidth(200.0f);
 		testPath.setHeight(50.0f);
 		testPath.setOrigin(Origin.LEFT);
 		testPath.addAction(
 			forever(
 				rotateBy(180.0f, 1.0f)
 			)
 		);
 
 		stage.addActor(testPath);
 
 		// Test ActorContainer with Decals.
 		DecalActor decalComponent = new DecalActor();
 		decalComponent.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decalComponent.setOrigin(0.0f, 200.0f, 0.0f);
 		decalComponent.setWidth(200.0f);
 		decalComponent.setHeight(200.0f);
 
 		DecalActor decalComponent2 = new DecalActor();
 		decalComponent2.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decalComponent2.setOrigin(200.0f, 0.0f, 0.0f);
 		decalComponent2.setWidth(200.0f);
 		decalComponent2.setHeight(200.0f);
 		decalComponent2.setColor(1.0f, 1.0f, 0.0f, 0.5f);
 
 		DecalActor decalComponent3 = new DecalActor();
 		decalComponent3.setDecal(Decal.newDecal(new TextureRegion(texture2), true));
 		decalComponent3.setOrigin(-200.0f, 0.0f, 0.0f);
 		decalComponent3.setWidth(200.0f);
 		decalComponent3.setHeight(200.0f);
 
 		ActorContainer decalContainer = new TransformActorContainer();
 		decalContainer.addActor(decalComponent);
 		decalContainer.addActor(decalComponent2);
 		decalContainer.addActor(decalComponent3);
 		decalContainer.setPosition(0.0f, -0.8f * halfHeight);
 
 		decalContainer.addAction(
 			forever(
 				parallel(
 					sequence(
 						moveBy(-100.0f, -300.0f, 1.0f),
 						moveBy(100.0f, 300.0f, 1.0f)
 					),
 					rotateBy(180.0f, 2.0f)
 				)
 			)
 		);
 
 		stage.addActorContainer(decalContainer);
 
 		// Animation.
 		AnimationActor animation = AnimationActor.create(new Texture("data/animation.png"), 9, 0.0417f, 4, 4);
 		animation.setOrigin(Origin.LEFT);
 		animation.setWidth(256.0f);
 		animation.setHeight(256.0f);
 		animation.setPosition(0.8f * halfWidth, -0.4f * halfHeight);
 		animation.addAction(forever(rotateBy(180.0f, 1.0f)));
 		stage.addActor(animation);
 
 		// Flock.
 		Entity flock = EntityFactory.createFlock();
 		stage.addActorContainer((ActorContainer) flock.getActor());
 
 		// Player.
 		Entity player = stage.getGame().getPlayer().getEntity();
 		stage.addActorContainer((ActorContainer) player.getActor());
 
 		LaserComponent laserComponent = (LaserComponent) player.getComponentOfType(LaserComponent.class);
 		stage.addActorContainer(laserComponent.getLaserActors());
 	}
 }
