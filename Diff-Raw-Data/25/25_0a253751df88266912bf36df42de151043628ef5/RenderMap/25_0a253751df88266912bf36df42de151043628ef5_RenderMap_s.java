 package de.redlion.civilwar.render;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g3d.StillModelNode;
 import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
 import com.badlogic.gdx.graphics.g3d.materials.Material;
 import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
 import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
 import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 import com.badlogic.gdx.math.collision.Ray;
 
 import de.redlion.civilwar.Configuration;
 import de.redlion.civilwar.GameSession;
 import de.redlion.civilwar.collision.HeightMap;
 import de.redlion.civilwar.render.LightManager.LightQuality;
 import de.redlion.civilwar.units.EnemySoldier;
 import de.redlion.civilwar.units.PlayerSoldier;
 import de.redlion.civilwar.units.Soldier;
 
 public class RenderMap {
 	
 	static final int LIGHTS_NUM = 0;
 	static final float LIGHT_INTESITY = 0f;
 	
 	boolean highQuality = true;
 
 	LightManager lightManager;
 	public static PrototypeRendererGL20 protoRenderer;
 
 	StillModel modelLandscapeObj;
 	
 	StillModel modelSoldierObj;
 	StillModel modelEnemySoldierObj;
 	StillModel modelSelectedSoldierObj;
 	Texture texSoldierDiff;
 	Texture texEnemySoldierDiff;	
 	
 	StillModel modelWeaponObj;
 	Texture texWeaponDiff;
 	
 	Texture texAOMap;	
 	Texture imageLightning;	
 	
 	Material circledSoldier; /* debug */
 	Material materialSoldier;
 	
 	int soldierSelector = 0;
 	
 	float time;
 
 	Texture whiteTex;
 	
 	StillModelNode instanceLand;	
 	BoundingBox instanceLandBB;
 
 	public OrthographicCamera cam;
 	PerspectiveCamera currCam;
 	
 	public HeightMap heightMap;
 	
 	Matrix4 tmp = new Matrix4().idt();
 
 	Preferences prefs;
 	
 	public RenderMap() {
 		setupScene();
 	}
 
 	private void setupScene() {
 		prefs = Gdx.app.getPreferences(Configuration.getInstance().TAG);
 		
 		highQuality = prefs.getBoolean("highQuality", false);
 		if(highQuality) {
 			lightManager = new LightManager(LIGHTS_NUM, LightQuality.FRAGMENT);
 		} else {
 			lightManager = new LightManager(LIGHTS_NUM, LightQuality.VERTEX);
 		}
 		
 		lightManager.dirLight = new DirectionalLight();
 		lightManager.dirLight.color.set(1f, 1f, 1f, 1);
 		lightManager.dirLight.direction.set(-0.4f, -1.0f, 0.03f).nor();
 
 		lightManager.ambientLight.set(0.2f, 0.2f, 0.21f, 0.0f);
 		
 		protoRenderer = new PrototypeRendererGL20(lightManager);
 
 		
 		texAOMap = new Texture(Gdx.files.internal("data/landscape_diff.png"), true);
 		texAOMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		modelLandscapeObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/landscape.g3dt"));	
 		heightMap = new HeightMap(modelLandscapeObj);
 		
 		modelSoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
 		modelEnemySoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
 		modelSelectedSoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
 		texSoldierDiff = new Texture(Gdx.files.internal("data/soldier_diff.png"), true);		
 		texEnemySoldierDiff = new Texture(Gdx.files.internal("data/enemy_soldier_diff.png"), true);
 		
 		modelWeaponObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/rifle.g3dt"));
 		texWeaponDiff = new Texture(Gdx.files.internal("data/rifle_diff.png"), true);
 		
 		imageLightning = new Texture(Gdx.files.internal("data/beach_probe_diffuse.png"), true);
 		imageLightning.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
 		imageLightning.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		cam.zoom = 0.008f;
 		cam.position.set(-17, 18, 6);
 		cam.lookAt(0, 0, 0);
 		cam.update();
 		
 		// set materials
 		MaterialAttribute materialAttributeSoldierDiffTex = new TextureAttribute(texSoldierDiff, 0, TextureAttribute.diffuseTexture);
 		MaterialAttribute materialAttributeEnemySoldierDiffTex = new TextureAttribute(texEnemySoldierDiff, 0, TextureAttribute.diffuseTexture);
 
 		MaterialAttribute materialAttributeWeaponDiffTex = new TextureAttribute(texWeaponDiff, 0, TextureAttribute.diffuseTexture);
 		
 		MaterialAttribute materialAttributeLandscapeDiffTex = new TextureAttribute(texAOMap, 0, TextureAttribute.diffuseTexture);
 		
 		materialSoldier = new Material("soldier", materialAttributeSoldierDiffTex);
 		Material materialEnemySoldier = new Material("enemySoldier", materialAttributeEnemySoldierDiffTex);
 		Material materialLandscape = new Material("landscape", materialAttributeLandscapeDiffTex);
 		Material materialWeapon = new Material("weapon", materialAttributeWeaponDiffTex);
 		
 		//debug
 		Texture texCircledSoldierDiff = new Texture(Gdx.files.internal("data/white.png"), true);
 		MaterialAttribute materialAttributeCircledSoldierDiffTex = new TextureAttribute(texCircledSoldierDiff, 0, TextureAttribute.diffuseTexture);
 		circledSoldier = new Material("circledSoldier", materialAttributeCircledSoldierDiffTex);
 		
 		modelWeaponObj.setMaterial(materialWeapon);
 		modelSoldierObj.setMaterial(materialSoldier);
 		modelEnemySoldierObj.setMaterial(materialEnemySoldier);
 		modelSelectedSoldierObj.setMaterial(circledSoldier);
 		modelLandscapeObj.setMaterial(materialLandscape);
 		
 		
 		// create instances
 		{
 			instanceLandBB = new BoundingBox();		
 			instanceLand = new StillModelNode();
 			modelLandscapeObj.getBoundingBox(instanceLandBB);
 			instanceLandBB.mul(instanceLand.matrix);
 			instanceLand.radius = (instanceLandBB.getDimensions().len() / 2);
 		}
 	}
 
 	public void updateCamera(PerspectiveCamera cam) {
 //		protoRenderer.cam = cam;
 	}
 
 	public void render() {
 		Gdx.gl.glClearColor(0.6f, 0.7f, 1, 1);
 		
 		time += Gdx.graphics.getDeltaTime();		
 		
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
 		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
 		
 		protoRenderer.cam = cam;
 		
 		protoRenderer.begin();
 		protoRenderer.draw(modelLandscapeObj, instanceLand);
 		
 		//update height per frame		
 		Soldier tempSoldier = GameSession.getInstance().soldiers.get(soldierSelector);
 		Ray ray = new Ray(new Vector3(tempSoldier.position.x + (tempSoldier.velocity.x * Gdx.graphics.getDeltaTime()*GameSession.getInstance().soldiers.size), -100, tempSoldier.position.y + (tempSoldier.velocity.y* Gdx.graphics.getDeltaTime()*GameSession.getInstance().soldiers.size)), Vector3.Y);
 		Vector3 localIntersection = new Vector3();
 		Intersector.intersectRayTriangles(ray, heightMap.map, localIntersection);
		tempSoldier.height = tempSoldier.targetHeight; 
		tempSoldier.targetHeight = localIntersection.y;
		tempSoldier.heightIterator = soldierSelector;
 		soldierSelector = (soldierSelector + 1) % GameSession.getInstance().soldiers.size;
 		
 		//render soldier
 		for(int i = 0; i < GameSession.getInstance().soldiers.size; i++) {
 			Soldier soldier = GameSession.getInstance().soldiers.get(i);
 			
 			soldier.instance.matrix.idt();
 			
			float height = Interpolation.linear.apply(soldier.height, soldier.targetHeight, (GameSession.getInstance().soldiers.size) * ((soldier.heightIterator + soldierSelector - soldier.heightIterator) / (float) (GameSession.getInstance().soldiers.size))/ (float) (GameSession.getInstance().soldiers.size));
			
//			System.out.println("frame:" + soldierSelector);
//			System.out.println("soldier:" + soldier.heightIterator);
//			System.out.println("soldier.height:" + soldier.height);
//			System.out.println("soldier.targetHeight:" + soldier.targetHeight);
//			System.out.println("height:" + height);
 			
 			soldier.instance.matrix.trn(soldier.position.x, height, soldier.position.y);
 			soldier.instance.matrix.rotate(Vector3.Y, soldier.facing.angle());
 			soldier.instance.matrix.rotate(Vector3.Y, -90);
 			soldier.instance.matrix.rotate(Vector3.Z, soldier.angle);
 			
 			if((soldier instanceof PlayerSoldier)) {
 				if(!((PlayerSoldier) soldier).circled) {
 					protoRenderer.draw(modelSoldierObj, soldier.instance);
 				}
 				else {
 					protoRenderer.draw(modelSelectedSoldierObj, soldier.instance);
 				}
 			}
 			
 			if((soldier instanceof EnemySoldier)) {
 				protoRenderer.draw(modelEnemySoldierObj, soldier.instance);
 			}
 		}
 
 		protoRenderer.end();	
 
 	}
 
 }
