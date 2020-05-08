 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.effect.shapes.EmitterBoxShape;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.SpotLight;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.FogFilter;
 import com.jme3.renderer.ViewPort;
 import com.jme3.renderer.queue.RenderQueue;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.shadow.BasicShadowRenderer;
 import com.jme3.texture.Texture;
 import com.jme3.util.SkyFactory;
 import controlador.MenuController;
 import java.util.ArrayList;
 
 /**
  *
  * @author normenhansen
  */
 public class WorldCreator {
     private static ParticleEmitter rain;
     private static ParticleEmitter snow;
     
     private static Box brick;
     static float bLength = 0.6f;
     static float bWidth = 0.40f;
     static float bHeight = 0.2f;
     
     private Node rootNode;
     private AssetManager assetManager;
     private BulletAppState space;
     private ViewPort viewPort;
     private ArrayList<Geometry> obstacleList;
     private MenuController menu;
     
     private Material mat_road;
     private Material mat_brick;
     private Material mat_box;
     private Material mat_snow;
     private Material mat_rain;
     private Material mat_bounds;
     private Spatial roadModel;
     private ListaMapas listaMapas;
     private Mapa mapaActual;
     private Audio rain_sound;
     private LlistaReproduccio game_music;
 
     /**
      * creates a simple physics test world with a floor, an obstacle and some test boxes
      * @param rootNode
      * @param assetManager
      * @param space
      */
     
     public WorldCreator(Node rootNode, AssetManager assetManager, BulletAppState space, ViewPort viewPort, MenuController menu) {
         this.rootNode = rootNode;
         this.assetManager = assetManager;
         this.space = space;
         this.viewPort = viewPort;
         this.menu = menu;
        initAudio();
         obstacleList = new ArrayList<Geometry>();
         listaMapas = new ListaMapas();
         initMapas();
         initMaterial();
         createWorld();
         
     }
     
     private void initAudio() {
         String musicNames[] = new String[3];
         musicNames[0] = "must_destroy.ogg";
         musicNames[1] = "3_point_1.ogg";
         musicNames[2] = "hot_ride.ogg";
         game_music = new LlistaReproduccio(true, rootNode, assetManager, musicNames);
         float volumes[] = new float[3];
         volumes[0] = 0.3f;
         volumes[1] = 0.5f;
         volumes[2] = 0.4f;
         game_music.setVolumes(volumes);
     }
     
     private void initAudioPluja() {
         rain_sound = new Audio(rootNode, assetManager, "rain_sound.wav", true);
         rain_sound.play();
     }
     
     public void updateMusic() {
         if (game_music.isStopped()) {
             game_music.playNext();
         }
     }
     
     private void createWorld() {
        game_music.playNext();
         //Afegim la llum
         /*boolean sky = menu.getModoNoche();
         if(sky){
             //Mode nit
             AmbientLight ambient = new AmbientLight();
             ambient.setColor(ColorRGBA.DarkGray);
             rootNode.addLight(ambient);
         }else{
             //Mode dia
             DirectionalLight sun = new DirectionalLight();
             Vector3f lightDir=new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f);
             sun.setDirection(lightDir);
             sun.setColor(ColorRGBA.White.clone().multLocal(2));
             rootNode.addLight(sun);
 
             AmbientLight ambient = new AmbientLight();
             ambient.setColor(ColorRGBA.LightGray);
             rootNode.addLight(ambient);
         }*/
         
         DirectionalLight sun = new DirectionalLight();
         Vector3f lightDir=new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f);
         sun.setDirection(lightDir);
         sun.setColor(ColorRGBA.White.clone().multLocal(2));
         rootNode.addLight(sun);
 
         AmbientLight ambient = new AmbientLight();
         ambient.setColor(ColorRGBA.LightGray);
         rootNode.addLight(ambient);
         
         
         brick = new Box(Vector3f.ZERO, bLength, bHeight, bWidth);
         brick.scaleTextureCoordinates(new Vector2f(1f, .5f));
         
         //Afegim ombres
         BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 256);
         bsr.setDirection(new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f)); // light direction
         viewPort.addProcessor(bsr); 
         
         //Afegim el cel
         /*
         Node sky = new Node();
         if(sky){
             //Mode nit
             sky.attachChild(SkyFactory.createSky(assetManager, assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg"), assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg"), assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg"), assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg"), assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg"), assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg")));
         }else{
             //Mode dia
             sky.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
         }
         rootNode.attachChild(sky);*/
         Node sky = new Node();
         sky.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
         rootNode.attachChild(sky);
         
         //Mirem quin circuit ha estat seleccionat en el menu
         mapaActual = listaMapas.getMapa(menu.getIdCircuit());
         
         //Road creation
         // We load the scene
         Spatial sceneModel = assetManager.loadModel(mapaActual.getSceneModel());
         sceneModel.setLocalTranslation(0, -5, 0);
         sceneModel.scale(20,20,20);
         //sceneModel.setMaterial(mat_road);
         
         // We set up collision detection for the scene by creating a
         // compound collision shape and a static RigidBodyControl with mass zero.
         CollisionShape sceneShape =
                 CollisionShapeFactory.createMeshShape((Node) sceneModel);
         RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
         sceneModel.addControl(landscape);
         sceneModel.setShadowMode(ShadowMode.Receive);
 
         // We attach the scene  and its limits to the rootNode and the physics space,
         // to make them appear in the game world.
         rootNode.attachChild(sceneModel);
         space.getPhysicsSpace().add(sceneModel);
         
         //We load the limits of the scene
         /*Spatial boundsModel = assetManager.loadModel("Models/AngularRoad/InvisibleWalls/InvisibleWalls.scene");
         boundsModel.setLocalTranslation(0, -5, 0);
         boundsModel.scale(10,10,10);
         boundsModel.setMaterial(mat_bounds);
          
         // We set up collision detection for the walls.
         CollisionShape boundsShape =
                 CollisionShapeFactory.createMeshShape((Node) boundsModel);
         RigidBodyControl limits = new RigidBodyControl(boundsShape, 0);
         boundsModel.addControl(limits);
         boundsModel.setQueueBucket(RenderQueue.Bucket.Transparent);*/
         
         //rootNode.attachChild(boundsModel);
         //space.getPhysicsSpace().add(boundsModel);
         
         //We load the limits of the scene
         roadModel = assetManager.loadModel("Models/StraightRoad/Carretera/StraightRoad.j3o");
         roadModel.setLocalTranslation(0, -5, 0);
         roadModel.scale(20,0.25f,20);
         roadModel.setMaterial(mat_road);
          
         // We set up collision detection for the walls.
         CollisionShape roadShape =
                 CollisionShapeFactory.createMeshShape((Node) roadModel);
         RigidBodyControl limits = new RigidBodyControl(roadShape, 0);
         roadModel.addControl(limits);
         
         rootNode.attachChild(roadModel);
         space.getPhysicsSpace().add(roadModel);
         
         //streetlamps creation
         mostrarLlums();
         
         //wall creation
         mostrarMurs();
 
         //Obstacle creation
         mostrarCaixes();
 
 
         //Creem el efecte de clima que s'hagi seleccionat al menu
         initClima(menu.getWeatherName());
     }
     
     
     private void initClima(String weatherName) {
         if(weatherName == "Soleado"){
             
         }else if(weatherName == "Lluvioso"){
             initPluja();
         }else if(weatherName == "Nevado"){
             initNeu();
         }else if(weatherName == "Nebuloso"){
             initBoira();
         }else{
             System.out.println("El clima " + weatherName + " es desconegut!");
         }
     }
     
     private void initNeu() {
         snow = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 1000);
         snow.setMaterial(mat_snow);
         snow.setImagesX(2); snow.setImagesY(2); // 2x2 texture animation
         snow.setShape(new EmitterBoxShape(new Vector3f(-100f,10f,-100f),new Vector3f(100f,10f,100f)));
         snow.setStartColor(ColorRGBA.White);
         snow.getParticleInfluencer().setInitialVelocity(new Vector3f(0,-2,0));
         snow.setStartSize(10.11f);
         snow.setEndSize(10.11f);
         snow.setGravity(0,2,0);
         snow.setLowLife(6.5f);
         snow.setHighLife(6.5f);
         snow.getParticleInfluencer().setVelocityVariation(0.3f);
         snow.setLocalTranslation(0f, 0f, 0f);
         snow.setParticlesPerSec(200);
         rootNode.attachChild(snow);
     }
     
     private void initPluja() {
         initAudioPluja();
         rain = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 100000);
         rain.setMaterial(mat_rain);
         //rain.setParticlesPerSec(50);
         rain.setImagesX(2); rain.setImagesY(2); // 2x2 texture animation
         rain.setShape(new EmitterBoxShape(new Vector3f(-100f,10f,-100f),new Vector3f(100f,10f,100f)));
         rain.setStartColor(new ColorRGBA(192f,192f,192f,0.2f));
         //rain.setStartColor(new ColorRGBA(0f,0f,255f,1f));
         rain.getParticleInfluencer().setInitialVelocity(new Vector3f(0,-25,0));
         rain.getParticleInfluencer().setVelocityVariation(0f);
         rain.setStartSize(0.50f);
         rain.setEndSize(0.50f);
         rain.setGravity(0,1,0);
         rain.setLowLife(6.5f);
         rain.setHighLife(6.5f);
         rain.setParticlesPerSec(2000);
         rootNode.attachChild(rain);
     }
     
     private void initBoira() {
         FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
         FogFilter fog=new FogFilter();
         fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
         fog.setFogDistance(100);
         fog.setFogDensity(2.0f);
         fpp.addFilter(fog);
         viewPort.addProcessor(fpp);
         
         /*FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
         BloomFilter bloom=new BloomFilter();
         //bloom.setBlurScale(0.5f);
         bloom.setBloomIntensity(1.25f);
         fpp.addFilter(bloom);
         viewPort.addProcessor(fpp);*/
     }
     
     private void crearMur(int x, int y, int z) {
         float startpt = bLength / 4;
         float height = 0;
         for (int j = 0; j < 10; j++) {
             for (int i = 0; i < 6; i++) {
                 Vector3f vt = new Vector3f(i * bLength * 2 + startpt+x, bHeight + height+y, z);
                 addBrick(vt);
             }
             startpt = -startpt;
             height += 2 * bHeight;
         }
     }
     
     private void crearCaixa(int x, int y, int z) {
         Box obstacleBox = new Box(1,1,1);
         Geometry obstacleModel = new Geometry("Obstacle", obstacleBox);
         obstacleModel.setLocalTranslation(x, y, z);
         obstacleModel.setMaterial(mat_box);
         obstacleModel.addControl(new RigidBodyControl(5));
         obstacleModel.setShadowMode(ShadowMode.CastAndReceive);
         rootNode.attachChild(obstacleModel);
         space.getPhysicsSpace().add(obstacleModel);
         obstacleList.add(obstacleModel);
     }
     
     private void crearCaixaFracturada(int x, int y, int z) {
         Spatial caja_parte1 = assetManager.loadModel("Models/caixa_fracturada/superior.j3o");
         caja_parte1.setLocalTranslation(x,y,z);
         caja_parte1.addControl(new RigidBodyControl(5));
         caja_parte1.getControl(RigidBodyControl.class).setFriction(2f);
         rootNode.attachChild(caja_parte1);
         space.getPhysicsSpace().add(caja_parte1);
         Spatial caja_parte2 = assetManager.loadModel("Models/caixa_fracturada/medio.j3o");
         caja_parte2.setLocalTranslation(x,y,z);
         caja_parte2.addControl(new RigidBodyControl(5));
         caja_parte2.getControl(RigidBodyControl.class).setFriction(2f);
         rootNode.attachChild(caja_parte2);
         space.getPhysicsSpace().add(caja_parte2);
         Spatial caja_parte3 = assetManager.loadModel("Models/caixa_fracturada/inferior.j3o");
         caja_parte3.setLocalTranslation(x,y,z);
         caja_parte3.addControl(new RigidBodyControl(5));
         caja_parte3.getControl(RigidBodyControl.class).setFriction(2f);
         rootNode.attachChild(caja_parte3);
         space.getPhysicsSpace().add(caja_parte3);
     }
     
     private void initMapas() {
         ArrayList<Vector3f> luces = new ArrayList<Vector3f>();
         ArrayList<Vector3f> cajas = new ArrayList<Vector3f>();
         ArrayList<Vector3f> muros = new ArrayList<Vector3f>();
         cajas.add(new Vector3f(2,-3,-10));
         cajas.add(new Vector3f(2,-3,-50));
         cajas.add(new Vector3f(-25,-3,-50));
         cajas.add(new Vector3f(-50,-3,-50));
         cajas.add(new Vector3f(-25,-3,-25));
         cajas.add(new Vector3f(-50,-3,0));
         cajas.add(new Vector3f(-50,-3,50));
         cajas.add(new Vector3f(-50,-3,20));
         cajas.add(new Vector3f(0,-3,50));
         muros.add(new Vector3f(-2,-5,10));
         muros.add(new Vector3f(-55,-5,-15));
         Mapa m = new Mapa(new Vector3f(-10,-2,80),new Quaternion().fromAngles(0, (float)Math.toRadians(-90), 0),"Models/StraightRoad/Ciutat/solo_city.j3o",luces,cajas,muros);
         listaMapas.añadirMapa(m);
         luces.clear();
         cajas.clear();
         muros.clear();
         cajas.add(new Vector3f(2,-3,-10));
         cajas.add(new Vector3f(2,-3,-50));
         cajas.add(new Vector3f(-25,-3,-50));
         cajas.add(new Vector3f(-50,-3,-50));
         cajas.add(new Vector3f(-25,-3,-25));
         cajas.add(new Vector3f(-50,-3,0));
         cajas.add(new Vector3f(-50,-3,50));
         cajas.add(new Vector3f(-50,-3,20));
         cajas.add(new Vector3f(0,-3,50));
         muros.add(new Vector3f(-2,-5,10));
         muros.add(new Vector3f(-55,-5,-15));
         m = new Mapa(new Vector3f(0,0,0),new Quaternion().fromAngles(0, (float)Math.toRadians(0), 0),"Models/AngularRoad/AngularRoad.j3o",luces,cajas,muros);
         listaMapas.añadirMapa(m);
     }
     
     private void afegirLlum(float x, float y, float z){
         SpotLight spot = new SpotLight();
         spot.setSpotRange(100f);                           // distance
         spot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
         spot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
         spot.setColor(ColorRGBA.White.mult(1.3f));         // light color
         spot.setPosition(new Vector3f(x,y,z));               // shine from camera loc
         spot.setDirection(new Vector3f(0,-1,0));             // shine forward from camera loc
         rootNode.addLight(spot);   
     }
 
     
     public void mostrarCaixes() {
         ArrayList<Vector3f> cajas = mapaActual.getListaCajas();
         for(int i = 0; i < cajas.size();i++) {
             Vector3f v = cajas.get(i);
             //crearCaixa((int)v.x,(int)v.y,(int)v.z);
             crearCaixaFracturada((int)v.x,(int)v.y,(int)v.z);
         }
     }
     
     public void mostrarMurs() {
         ArrayList<Vector3f> murs = mapaActual.getListaMuros();
         for(int i = 0; i < murs.size();i++) {
             Vector3f v = murs.get(i);
             crearMur((int)v.x,(int)v.y,(int)v.z);
         }
     }
     
     public void mostrarLlums() {
         ArrayList<Vector3f> llums = mapaActual.getListaLuces();
         for(int i = 0; i < llums.size();i++) {
             Vector3f v = llums.get(i);
             afegirLlum((int)v.x,(int)v.y,(int)v.z);
         }
     }
 
     private void initMaterial() {
         
         mat_road = new Material( 
             assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat_road.setTexture("ColorMap", 
             assetManager.loadTexture("Textures/RoadTexture.jpg"));
         
         mat_bounds = new Material(
                 assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat_bounds.setTexture("ColorMap",
                 assetManager.loadTexture("Textures/transparentTexture.png"));
         mat_bounds.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         mat_bounds.setTransparent(true);
 
         mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         TextureKey key2 = new TextureKey("Textures/ladrillo2.jpg");
         key2.setGenerateMips(true);
         Texture tex2 = assetManager.loadTexture(key2);
         mat_brick.setTexture("ColorMap", tex2);
         
         mat_box = new Material( 
             assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_box.setTexture("DiffuseMap", 
             assetManager.loadTexture("Textures/BoxTexture.jpg"));
         
         mat_snow = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
         mat_snow.setTexture("Texture", assetManager.loadTexture("Textures/snow.png"));
         
         mat_rain = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
         mat_rain.setTexture("Texture", assetManager.loadTexture("Textures/teardrop.png"));
 
     }
 
     private void addBrick(Vector3f ori) {
 
         Geometry reBoxg = new Geometry("brick", brick);
         reBoxg.setMaterial(mat_brick);
         reBoxg.setLocalTranslation(ori);
         //for geometry with sphere mesh the physics system automatically uses a sphere collision shape
         reBoxg.addControl(new RigidBodyControl(1.5f));
         reBoxg.setShadowMode(ShadowMode.CastAndReceive);
         reBoxg.getControl(RigidBodyControl.class).setFriction(0.6f);
         rootNode.attachChild(reBoxg);
         space.getPhysicsSpace().add(reBoxg);
         obstacleList.add(reBoxg);
     }
     
     public ArrayList<Geometry> getObstacles(){
         return obstacleList;
     }
     
     public Spatial getCarretera(){
         return roadModel;
     }
 
     public Vector3f getInitialPos(){
         //return new Vector3f(-10, -2, 80); // lloc on comenca el cotxe
         return mapaActual.getOrigen();
     }
     
     public Quaternion getInitialRot(){
         //return new Quaternion().fromAngles(0, (float)Math.toRadians(-90), 0);
         return mapaActual.getRotacionInicial();
     }
 
 }
