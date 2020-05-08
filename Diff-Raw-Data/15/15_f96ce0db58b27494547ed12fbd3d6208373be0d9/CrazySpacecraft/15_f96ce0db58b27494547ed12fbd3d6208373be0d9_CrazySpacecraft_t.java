 package rules.Spacecraft;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.swing.ImageIcon;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.collision.WorldManifold;
 import org.jbox2d.collision.shapes.EdgeShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.MathUtils;
 import org.jbox2d.common.Transform;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.contacts.Contact;
 import org.jbox2d.dynamics.joints.Joint;
 import org.jbox2d.dynamics.joints.JointEdge;
 import org.jbox2d.dynamics.joints.JointType;
 import org.jbox2d.dynamics.joints.PrismaticJoint;
 import org.jbox2d.dynamics.joints.PrismaticJointDef;
 import org.jbox2d.dynamics.joints.RevoluteJointDef;
 
 import prereference.ConfigSettings;
 import prereference.Setting;
 import render.CustomizedRenderer;
 import rules.RuleModel;
 import rules.Spacecraft.Rocket.RocketType;
 import rules.Spacecraft.SimpleDelay.ActionType;
 import utility.ContactPoint;
 import utility.DynamicTextLine;
 import utility.LinkedListItem;
 import utility.Log;
 
 import components.BuildConfig;
 
 import exceptions.ElementNotExistException;
 import framework.GameController;
 import framework.GameModel;
 
 public class CrazySpacecraft extends RuleModel{
 
   private BuildConfig config;
   private GameModel model;
   private CustomizedRenderer renderer = null;
 
   private int level = 1;
 
   private int cooldownCountDown = 0;
   private int energyCountDown = 0; //for laser
 
   private Spacecraft spacecraft;
 
   private Map<String,Obstacle> obstacles;
 
   private Map<String,Monster> monsters;
   private int numOfMonsters;
   private Map<String, PrismaticJoint> pjForMonster;
 
   private Map<String, ResourcePack> resourcePacks;
   private int numOfResourcePacks;
   private Map<ResourcePack, PrismaticJoint> pjForResourcePack;
 
   private Laser[] lasers;
 
   //customized rendering:
   private ImageIcon icon = null;
   private Image spacecraftImg = null;
   private Image[] resourcePackImg = null;
   private Image background = null;
   private Image rocketMonsterExplosion = null;
   private Image laserMonsterExplosion = null;
 
   private ConcurrentHashMap<String, Explosion> explosions;
   private LinkedList<SimpleDelay> delays;
   private DynamicTextLine textLines;
 
   Vec2 worldCenter;
 
   public static enum MovementType{
 	NoMovement, Linear,Circular,BackForth
   }
 
   public CrazySpacecraft(BuildConfig buildConfig, GameModel gameModel){
 	config = buildConfig;
 	model = gameModel;
 	editable = false;
 	numOfResourcePacks = 10;
 	level = 1;
 	spacecraft = new Spacecraft();
 
 	resourcePacks = new HashMap<String,ResourcePack>();
 	pjForResourcePack = new HashMap<ResourcePack, PrismaticJoint>();
 
 	obstacles = new HashMap<String,Obstacle>();
 
 	monsters = new HashMap<String, Monster>();
 	pjForMonster = new HashMap<String,PrismaticJoint>();
 	lasers = new Laser[4]; lasers[0] = lasers[1] = lasers[2] = lasers[3] = null;
 
 	explosions = new ConcurrentHashMap<String, Explosion>();
 	delays = new LinkedList<SimpleDelay>();
 	textLines = new DynamicTextLine();
 
 	init();
   }
 
   private void initLevel(int level){
 	if(level <= 1){
 	  numOfMonsters = 6;
 	  spacecraft.setRocketType(RocketType.NormalBullet);
 	}
 	else if(level  == 2){
 	  numOfMonsters = 7;
 	  spacecraft.setRocketType(RocketType.Laser);
 	  energyCountDown = Rocket.LaserDuration;
 	}
 	else if(level >= 3){
 	  numOfMonsters = 8;
 	  //TODO: spread rocket?
 	  spacecraft.setRocketType(RocketType.NormalBullet);
 	}
   }
 
   private void nextLevel() {
 	level++;
 	initWorld();
 	init();
   }
 
   private void initWorld() {
 	for(Body b = config.getWorld().getBodyList(); b!= null; b = b.getNext()){
 	  config.getWorld().destroyBody(b);
 	}
 	for(Joint j = config.getWorld().getJointList(); j != null ; j = j.getNext()){
 	  config.getWorld().destroyJoint(j);
 	}
 	spacecraft = new Spacecraft();
 	resourcePacks = new HashMap<String,ResourcePack>();
 	pjForResourcePack = new HashMap<ResourcePack, PrismaticJoint>();
 	monsters = new HashMap<String, Monster>();
 	pjForMonster = new HashMap<String,PrismaticJoint>();
 	explosions = new ConcurrentHashMap<String, Explosion>();
 	textLines = new DynamicTextLine();
 	obstacles = new HashMap<String,Obstacle>();
 	lasers = new Laser[4]; lasers[0] = lasers[1] = lasers[2] = lasers[3] = null;
   }
 
   @Override
   public void init() {
 	initLevel(level);
 	//init game message
 	TextLine textLine = new TextLine();
 	textLine.setTimer(500);textLine.setMaxTimer(500);
 	textLine.setString("Welcome to play game demo: Crazy Spaceship!");
 	textLines.addAtBack(textLine);
 	textLine = new TextLine();
 	textLine.setTimer(500);textLine.setMaxTimer(500);
 	textLine.setString("In this game, you will need to collect those resource packs that flying randomly in the space.");
 	textLines.addAtBack(textLine);
 	textLine = new TextLine();
 	textLine.setTimer(500);textLine.setMaxTimer(500);
 	textLine.setString("- Don't get too close to those monsters!");
 	textLines.addAtBack(textLine);
 	textLine = new TextLine();
 	textLine.setTimer(500);textLine.setMaxTimer(500);
 	textLine.setString("- If you can't avoid them, shoot them by slamming your space bar!");
 	textLines.addAtBack(textLine);
 	textLine = new TextLine();
 	textLine.setTimer(500);textLine.setMaxTimer(500);
 	textLine.setString("Control: W S A D + Space");
 	textLines.addAtBack(textLine);
 	textLine = new TextLine();
 	textLine.setTimer(500);textLine.setMaxTimer(500);
 	textLine.setString("Enjoy! ;)");
 	textLines.addAtBack(textLine);
 	//init keyboard input
 	model.initKeyboard();
 
 	//init world
 	config.getWorld().setGravity(new Vec2(0f,0f));
 
 	//init game specific settings
 	Setting cameraScale = config.getConfigSettings().getSetting(ConfigSettings.DefaultCameraScale);
 	Setting cameraPos = config.getConfigSettings().getSetting(ConfigSettings.DefaultCameraPos);
 	Setting screenMoveWithObj = config.getConfigSettings().getSetting(ConfigSettings.ScreenMoveWithObject);
 	Setting enableZoom = config.getConfigSettings().getSetting(ConfigSettings.EnableZoom);
 	Setting enableDrag = config.getConfigSettings().getSetting(ConfigSettings.EnableDragScreen);
 	Setting drawJoints = config.getConfigSettings().getSetting(ConfigSettings.DrawJoints);
 	Setting drawShapes = config.getConfigSettings().getSetting(ConfigSettings.DrawShapes);
 	cameraScale.value = 10f;
 	cameraPos.value = new Vec2(-30f,50f);
 	worldCenter = new Vec2(0f,20f);
 	//screenMoveWithObj.enabled = true;
 	enableZoom.enabled = false;
 	enableDrag.enabled = false;
 	drawJoints.enabled = false;
 	drawShapes.enabled = false;										//Turn off the default rendering
 	if(!drawShapes.enabled){
 	  renderer = GameModel.getGamePanel().getCustomizedRenderer(); 	//Turn on the customized rendering 
 	}
 	icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/spaceship.png"));
 	spacecraftImg = icon.getImage();
 	resourcePackImg = new Image[3];
 	icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/ResourcePack3.png"));
 	resourcePackImg[0] = icon.getImage();
 	icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/ResourcePack2.png"));;
 	resourcePackImg[1] = icon.getImage();
 	icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/VespeneGas.png"));
 	resourcePackImg[2] = icon.getImage();
 	icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/background.jpg"));
 	//1920 x 1080
 	background = icon.getImage();
 	icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/explosion2.png"));
 	rocketMonsterExplosion = icon.getImage();
 	laserMonsterExplosion = icon.getImage();
 
 	//init game
 	Body ground;
 	{
 	  BodyDef bd = new BodyDef();
 	  bd.position.set(0.0f, 20.0f);
 	  ground = config.getWorld().createBody(bd);
 
 	  EdgeShape shape = new EdgeShape();
 
 	  FixtureDef sd = new FixtureDef();
 	  sd.shape = shape;
 	  sd.density = 0.0f;
 	  sd.restitution = 1f;
 
 	  // Left vertical
 	  Obstacle bound = new Bound();
 	  shape.set(new Vec2(-30.0f, -30.0f), new Vec2(-30.0f, 30.0f));
 	  bound.setObstacleFixture(ground.createFixture(sd));
 	  String obstacleId = bound.getId();
 	  while(obstacles.containsKey(obstacleId)){
 		obstacleId = Bound.OriginalID;
 		int rand = (int)(Math.random()*10000);
 		obstacleId = obstacleId.replace("0000", ""+rand);
 	  }
 	  bound.setId(obstacleId); 
 	  obstacles.put(bound.getId(), bound);
 
 	  // Right vertical
 	  bound = new Bound();
 	  shape.set(new Vec2(30.0f, -30.0f), new Vec2(30.0f, 30.0f));
 	  bound.setObstacleFixture(ground.createFixture(sd));
 	  obstacleId = bound.getId();
 	  while(obstacles.containsKey(obstacleId)){
 		obstacleId = Bound.OriginalID;
 		int rand = (int)(Math.random()*10000);
 		obstacleId = obstacleId.replace("0000", ""+rand);
 	  }
 	  bound.setId(obstacleId);
 	  obstacles.put(bound.getId(), bound);
 
 	  // Top horizontal
 	  bound = new Bound();
 	  shape.set(new Vec2(-30.0f, 30.0f), new Vec2(30.0f, 30.0f));
 	  bound.setObstacleFixture(ground.createFixture(sd));
 	  obstacleId = bound.getId();
 	  while(obstacles.containsKey(obstacleId)){
 		obstacleId = Bound.OriginalID;
 		int rand = (int)(Math.random()*10000);
 		obstacleId = obstacleId.replace("0000", ""+rand);
 	  }
 	  bound.setId(obstacleId);
 	  obstacles.put(bound.getId(), bound);
 
 	  // Bottom horizontal
 	  bound = new Bound();
 	  shape.set(new Vec2(-30.0f, -30.0f), new Vec2(30.0f, -30.0f));
 	  bound.setObstacleFixture(ground.createFixture(sd));
 	  obstacleId = bound.getId();
 	  while(obstacles.containsKey(obstacleId)){
 		obstacleId = Bound.OriginalID;
 		int rand = (int)(Math.random()*10000);
 		obstacleId = obstacleId.replace("0000", ""+rand);
 	  }
 	  bound.setId(obstacleId);
 	  obstacles.put(bound.getId(), bound);
 	}
 
 	{//monsters:
 	  Monster monster = null;
 	  for(int i = 0; i < numOfMonsters ; i++){
 		monster = new Monster();
 		String monsterId = monster.getId();
 		while(monsters.containsKey(monsterId)){
 		  monsterId = Monster.OriginalID;
 		  int rand = (int)( Math.random() * 10000);
 		  monsterId = monsterId.replace("0000", ""+rand);
 		}
 		monster.setId(monsterId);
 
 		icon = new ImageIcon(getClass().getResource("/rules/Spacecraft/SpacecraftImage/Monster1.png"));
 		Image image = icon.getImage();
 		monster.setMonsterImg(image);
 
 		double limit = 50.0f * Math.random() - 25.0f;
 		float x = (float) limit;
 		limit = 50.0f * Math.random() - 5.0f;
 		float y = (float) limit;
 		Vec2 monsterSpawnPt = new Vec2(x,y);
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set(monsterSpawnPt);
 		bd.fixedRotation = true;
 		monster.setMonsterBody(config.getWorld().createBody(bd));
 		createMonsterBody(monster);
 		randomMovement(monster);
 		monsters.put(monster.getId(), monster);
 	  }
 	}
 
 	{//obstacles:
 
 	}
 
 	{//spacecraft 
 	  Vec2 vertices[] = new Vec2[6];
 
 	  vertices[0] = new Vec2(-.75f,.5f).mul(.75f);
 	  vertices[1] = new Vec2(-.25f,-1.5f).mul(.75f);
 	  vertices[2] = new Vec2(.25f,-1.5f).mul(.75f);
 	  vertices[3] = new Vec2(.75f,.5f).mul(.75f);
 	  PolygonShape front = new PolygonShape();
 	  front.set(vertices, 4);
 	  vertices[0] = new Vec2(-1f,.5f).mul(.75f);
 	  vertices[1] = new Vec2(-1f,.25f).mul(.75f);
 	  vertices[2] = new Vec2(1f,.25f).mul(.75f);
 	  vertices[3] = new Vec2(1f,.5f).mul(.75f);
 	  PolygonShape back = new PolygonShape();
 	  back.set(vertices, 4);
 	  PolygonShape leftWeapon = new PolygonShape();
 	  leftWeapon.setAsBox(.075f, .45f, new Vec2(-1f,0f), 0f);
 	  PolygonShape rightWeapon = new PolygonShape();
 	  rightWeapon.setAsBox(.075f, .45f, new Vec2(1f,0f), 0f);
 
 	  FixtureDef sd1 = new FixtureDef();
 	  sd1.shape = front;
 	  sd1.density = 2f;
 	  sd1.restitution = .5f;
 	  sd1.filter.groupIndex = Spacecraft.SpacecraftGroupIndex;
 	  FixtureDef sd2 = new FixtureDef();
 	  sd2.shape = back;
 	  sd2.density = 2f;
 	  sd2.restitution = .5f;
 	  sd2.filter.groupIndex = Spacecraft.SpacecraftGroupIndex;
 	  FixtureDef sd3 = new FixtureDef();
 	  sd3.shape = leftWeapon;
 	  sd3.density = .5f;
 	  sd3.restitution = .5f;
 	  sd3.filter.groupIndex = Spacecraft.SpacecraftGroupIndex;
 	  FixtureDef sd4 = new FixtureDef();
 	  sd4.shape = rightWeapon;
 	  sd4.density = .5f;
 	  sd4.restitution = .5f;
 	  sd4.filter.groupIndex = Spacecraft.SpacecraftGroupIndex;
 
 	  BodyDef bd = new BodyDef();
 	  bd.type = BodyType.DYNAMIC;
 	  bd.angularDamping = 5.0f;
 	  bd.linearDamping = 0.5f;
 
 	  bd.position.set(0.0f, 2.0f);
 	  bd.angle = MathUtils.PI;
 	  bd.allowSleep = false;
 	  spacecraft.setSpacecraftBody(config.getWorld().createBody(bd));
 	  spacecraft.getSpacecraftBody().createFixture(sd1);
 	  spacecraft.getSpacecraftBody().createFixture(sd2);
 	  spacecraft.getSpacecraftBody().createFixture(sd3);
 	  spacecraft.getSpacecraftBody().createFixture(sd4);
 	}
 
 	{//Resource packs
 	  PolygonShape shape = new PolygonShape();
 	  shape.setAsBox(0.5f, 0.5f);
 
 	  FixtureDef fd = new FixtureDef();
 	  fd.shape = shape;
 	  fd.density = 1.0f;
 	  fd.friction = 0.3f;
 	  fd.filter.groupIndex = ResourcePack.ResourcePackGroupIndex;
 
 	  ResourcePack resourcePack;
 	  for (int i = 0; i < numOfResourcePacks; i++) {
 		resourcePack = new ResourcePack();
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set(0.0f, 5.0f + 1.54f * i);
 		MovementType movement = randomMovement(bd);
 		String id = resourcePack.getId();
 		int rand = (int)(Math.random()*10000);
 		id = id.replace("0000", ""+rand);
 		resourcePack.setId(id);
 		rand = (int)(3* Math.random() + 0.99999999f);
 		Image image = null;
 		switch(rand){
 		case 1:
 		  image = resourcePackImg[0];
 		  break;
 		case 2:
 		  image = resourcePackImg[1];
 		  break;
 		case 3:
 		  image = resourcePackImg[2];
 		  break;
 		default:
 		  image = resourcePackImg[0];
 		  break;
 		}
 		resourcePack.setImage(image);
 		resourcePack.setMovement(movement);
 		resourcePack.setResourcePackBody(config.getWorld().createBody(bd));
 		resourcePack.getResourcePackBody().createFixture(fd);
 		randomMovement(resourcePack);
 		resourcePacks.put(resourcePack.getId(), resourcePack);
 	  }
 	}
   }
 
   @Override
   public void step() {
 	if(!delays.isEmpty()){
 	  SimpleDelay delay = delays.peek();
 	  if(delay.getTimer() > 0){
 		if(delay.getTimer() == delay.getMaxTimer()){//Start notification
 		  if(delay.getStartNotification() != null){
 			textLines.addAtBack(delay.getStartNotification());
 		  }
 
 		  if(delay.getStartAction() != ActionType.NoAction){
 
 		  }
 		}
 		delay.setTimer(delay.getTimer()-1);
 		model.pause = true;
 	  }
 	  else if(delay.getTimer() == 0){
 		if(delay.getEndNotification() != null){
 		  textLines.addAtBack(delay.getEndNotification());
 		}
 
 		if(delay.getEndAction() != ActionType.NoAction){
 		  switch (delay.getEndAction()){
 		  case Restart:
 			break;
 		  case NextLevel:
 			nextLevel();
 			break;
 		  case Exit:
 			break;
 		  default:
 			break;
 		  }
 		}
 		delays.removeFirst();
 		if(delays.isEmpty()){
 		  model.pause = false;
 		}
 	  }
 	  else{
 
 	  }
 	  return;
 	}
 
 	HashSet<Body> nuke = new HashSet<Body>();
 	HashSet<Fixture> dead = new HashSet<Fixture>();
 
 	//Deal with input
 	Body body = spacecraft.getSpacecraftBody();
 	if (model.getKeys()['w']) {
 	  Vec2 f = body.getWorldVector(new Vec2(0.0f, -30.0f));
 	  Vec2 p = body.getWorldPoint(body.getLocalCenter().add(new Vec2(0.0f, 2.0f)));
 	  body.applyForce(f, p);
 	} else if (model.getKeys()['s']) {
 	  Vec2 f = body.getWorldVector(new Vec2(0.0f, 30.0f));
 	  Vec2 p = body.getWorldCenter();
 	  body.applyForce(f, p);
 	}
 
 	if (model.getKeys()['a']) {
 	  body.applyTorque(30.0f);
 	}
 	if (model.getKeys()['d']) {
 	  body.applyTorque(-30.0f);
 	}
 
 	if(spacecraft.getRocketType() == RocketType.Laser){
 	  if(model.getCodedKeys()[32]){
 		if(lasers[0] != null){
 		  energyCountDown--;
 		}
 		else{
 		  energyCountDown++;
 		  if(energyCountDown >= (Rocket.LaserDuration/2)){
 			spacecraft.setOnCD(false);
 		  }
 		}
 
 		if(energyCountDown <= 0){
 		  energyCountDown = 0;
 		  spacecraft.setOnCD(true);
 		  lasers[0] = lasers[1] = lasers[2] = lasers[3] = null;
 		}
 		spacecraft.shootLaser(config.getWorld());
 		this.shootLaser();
 		if(lasers[0] != null && lasers[0].getKilledMonster() != null){
		  lasers[0].getKilledMonster().setAlpha(0f);
 		  nuke.add(lasers[0].getKilledMonster().getMonsterBody());
 		  lasers[0].setKilledMonster(null);
 		}
 		if(lasers[1] != null && lasers[1].getKilledMonster() != null){
		  lasers[1].getKilledMonster().setAlpha(0f);
 		  nuke.add(lasers[1].getKilledMonster().getMonsterBody());
 		  lasers[1].setKilledMonster(null);
 		}
 		if(lasers[2] != null && lasers[2].getKilledMonster() != null){
		  lasers[2].getKilledMonster().setAlpha(0f);
 		  nuke.add(lasers[2].getKilledMonster().getMonsterBody());
 		  lasers[2].setKilledMonster(null);
 		}
 		if(lasers[3] != null && lasers[3].getKilledMonster() != null){
		  lasers[3].getKilledMonster().setAlpha(0f);
 		  nuke.add(lasers[3].getKilledMonster().getMonsterBody());
 		  lasers[3].setKilledMonster(null);
 		}
 	  }
 	  else if(!model.getCodedKeys()[32]){
 		energyCountDown++;
 		if(energyCountDown >= (Rocket.LaserDuration/2)){
 		  spacecraft.setOnCD(false);
 		}
 		else if(energyCountDown >= (Rocket.LaserDuration)){
 		  energyCountDown = (Rocket.LaserDuration);
 		}
 		lasers[0] = lasers[1] = lasers[2] = lasers[3] = null;
 	  }
 	}
 
 	if(spacecraft.getRocketType() != RocketType.Laser){
 	  //Deal with cooldown
 	  if(cooldownCountDown <= 0){
 		cooldownCountDown = 0;
 		spacecraft.setOnCD(false);
 	  }else{
 		cooldownCountDown--;
 	  }
 	}
 
 	//Deal with prismatic joints: change direction
 	for(Map.Entry<ResourcePack, PrismaticJoint> entry: pjForResourcePack.entrySet()){
 	  if(Math.abs(entry.getValue().getJointTranslation()) >= Math.abs(entry.getValue().getUpperLimit())){
 		entry.getValue().setMotorSpeed(-entry.getValue().getMotorSpeed());
 	  }
 	}
 
 	for(Map.Entry<String, PrismaticJoint> entry: pjForMonster.entrySet()){
 	  if(Math.abs(entry.getValue().getJointTranslation()) >= Math.abs(entry.getValue().getUpperLimit())){
 		entry.getValue().setMotorSpeed(-entry.getValue().getMotorSpeed());
 	  }
 	}
 
 	//Deal with collisions
 	for (int i = 0; i < config.getPointCount(); i++) {
 	  ContactPoint point = config.points[i];
 
 	  Body body1 = point.fixtureA.getBody();
 	  Body body2 = point.fixtureB.getBody();
 
 	  //Spacecraft hitting a resourcePack
 	  if(body1.getUserData() != null && body1.getUserData() instanceof Spacecraft){
 		if(body2.getUserData() != null && body2.getUserData() instanceof ResourcePack){
 		  ((ResourcePack)body2.getUserData()).setImage(null);
 		  nuke.add(body2);
 		}
 	  }
 	  if(body2.getUserData() != null && body2.getUserData() instanceof Spacecraft){
 		if(body1.getUserData() != null && body1.getUserData() instanceof ResourcePack){
 		  ((ResourcePack)body1.getUserData()).setImage(null);
 		  nuke.add(body1);
 		}
 	  }
 
 	  //Rocket hitting obstacles(including bounds)
 	  if(body1.getUserData() != null && body1.getUserData() instanceof Rocket){
 		if(point.fixtureB.getUserData() != null && point.fixtureB.getUserData() instanceof Obstacle){
 		  Rocket rocket = (Rocket)body1.getUserData();
 		  rocket.setImage(null);
 		  rocket.applyDamage(point.fixtureB.getUserData());
 		  spacecraft.getRockets().remove(((Rocket)body1.getUserData()).getId());
 		  nuke.add(body1);
 		  if(((Obstacle)point.fixtureB.getUserData()).getHp() <= 0){
 			dead.add(point.fixtureB);
 		  }
 		}
 	  }
 
 	  if(body2.getUserData() != null && body2.getUserData() instanceof Rocket){
 		if(point.fixtureA.getUserData() != null && point.fixtureA.getUserData() instanceof Obstacle){
 		  Rocket rocket = (Rocket)body2.getUserData();
 		  rocket.setImage(null);
 		  rocket.applyDamage(point.fixtureA.getUserData());
 		  spacecraft.getRockets().remove(((Rocket)body2.getUserData()).getId());
 		  nuke.add(body2);
 		  if(((Obstacle)point.fixtureA.getUserData()).getHp() <= 0){
 			dead.add(point.fixtureA);
 		  }
 		}
 	  }
 
 	  if(body1.getUserData() != null && body1.getUserData() instanceof Rocket){
 		if(body2.getUserData() != null && body2.getUserData() instanceof Monster){
 		  spacecraft.getRockets().remove(((Rocket)body1.getUserData()).getId());
 		  ((Rocket) body1.getUserData()).setImage(null);
 		  nuke.add(body1);
 		  if(((Monster)body2.getUserData()).getHp() <= 0){
 			monsters.remove(((Monster)body2.getUserData()).getId());
 			((Monster)body2.getUserData()).setMonsterImg(null);
			((Monster)body2.getUserData()).setAlpha(0f);
 			nuke.add(body2);
 		  }
 		}
 	  }
 
 	  if(body2.getUserData() != null && body2.getUserData() instanceof Rocket){
 		if(body1.getUserData() != null && body1.getUserData() instanceof Monster){
 		  spacecraft.getRockets().remove(((Rocket)body2.getUserData()).getId());
 		  ((Rocket) body2.getUserData()).setImage(null);
 		  nuke.add(body2);
 		  if(((Monster)body1.getUserData()).getHp() <= 0){
 			monsters.remove(((Monster)body1.getUserData()).getId());
 			((Monster)body1.getUserData()).setMonsterImg(null);
			((Monster)body1.getUserData()).setAlpha(0f);
 			nuke.add(body1);
 		  }
 		}
 	  }
 	}
 
 	for (Body b : nuke) {
 	  JointEdge je = b.getJointList();
 	  if(je != null){
 		if(je.joint.getType() == JointType.PRISMATIC){
 		  if(je.joint.getUserData() != null && je.joint.getUserData() instanceof ResourcePack){
 			pjForResourcePack.remove(je.joint.getUserData());
 		  }
 		  else if(je.joint.getUserData() != null && je.joint.getUserData() instanceof Monster){
 			pjForMonster.remove(((Monster)je.joint.getUserData()).getId());
 		  }
 		}
 		while(je.next != null){
 		  je = je.next;
 		  if(je.joint.getType() == JointType.PRISMATIC){
 			if(je.joint.getUserData() != null && je.joint.getUserData() instanceof ResourcePack){
 			  pjForResourcePack.remove(je.joint.getUserData());
 			}
 			else if(je.joint.getUserData() != null && je.joint.getUserData() instanceof Monster){
 			  pjForMonster.remove(((Monster)je.joint.getUserData()).getId());
 			}
 		  }
 		}
 	  }
 
 	  config.getWorld().destroyBody(b);
 	}
 
 	if(config.getConfigSettings().getSetting(ConfigSettings.ScreenMoveWithObject).enabled){
 	  Vec2 center = spacecraft.getSpacecraftBody().getWorldCenter().clone();
 	  //center.subLocal(new Vec2(GameModel.getGamePanel().getWidth()/2,GameModel.getGamePanel().getHeight()/2));
 	  Vec2 shift = new Vec2(GameModel.getGamePanel().getWidth()/2,GameModel.getGamePanel().getHeight()/2);
 	  GameModel.getGamePanelRenderer().getViewportTranform().getScreenVectorToWorld(shift, shift);
 	  center.subLocal(shift);
 	  GameModel.getGamePanelRenderer().getViewportTranform().setCenter(center);
 	}
 
 	if(resourcePacks.size() <= 0){
 	  SimpleDelay delay = new SimpleDelay(60,"Congratulations! You have finished this level");
 	  String delayId = delay.getId();
 	  int rand = (int)( Math.random() * 10000);
 	  delayId = delayId.replace("0000", ""+rand);
 	  delay.setId(delayId);
 	  delays.add(delay);
 
 	  delay = new SimpleDelay(60,"Next level starts at: 3...");
 	  delayId = delay.getId();
 	  rand = (int)( Math.random() * 10000);
 	  delayId = delayId.replace("0000", ""+rand);
 	  delay.setId(delayId);
 	  delays.add(delay);
 
 	  delay = new SimpleDelay(60,"Next level starts at: 2...");
 	  delayId = delay.getId();
 	  rand = (int)( Math.random() * 10000);
 	  delayId = delayId.replace("0000", ""+rand);
 	  delay.setId(delayId);
 	  delays.add(delay);
 
 	  delay = new SimpleDelay(60,"Next level starts at: 1...");
 	  delayId = delay.getId();
 	  rand = (int)( Math.random() * 10000);
 	  delayId = delayId.replace("0000", ""+rand);
 	  delay.setId(delayId);
 	  delay.setEndAction(ActionType.NextLevel);
 	  delays.add(delay);
 	}
   }
 
   @Override
   public void beginContact(Contact contact) {}
 
   @Override
   public void endContact(Contact contact) {}
 
   @Override
   public void preSolve(Contact contact, Manifold oldManifold) {
 
 	Body body1 = contact.m_fixtureA.getBody();
 	Body body2 = contact.m_fixtureB.getBody();
 
 	//Spacecraft hitting a resourcePack: disable contact
 	if(body1.getUserData() != null && body1.getUserData() instanceof Spacecraft){
 	  if(body2.getUserData() != null && body2.getUserData() instanceof ResourcePack){
 		contact.setEnabled(false);
 		resourcePacks.remove(((ResourcePack)body2.getUserData()).getId());
 		TextLine textLine = new TextLine();
 		textLine.setString("You got a resource pack! There are only "+resourcePacks.size()+" left.");
 		textLines.addAtBack(textLine);
 	  }
 	}
 	if(body2.getUserData() != null && body2.getUserData() instanceof Spacecraft){
 	  if(body1.getUserData() != null && body1.getUserData() instanceof ResourcePack){
 		contact.setEnabled(false);
 		resourcePacks.remove(((ResourcePack)body1.getUserData()).getId());
 		TextLine textLine = new TextLine();
 		textLine.setString("You got a resource pack! There are only "+resourcePacks.size()+" left.");
 		textLines.addAtBack(textLine);
 	  }
 	}
 
 	//Rockets hitting resource packs: disable contact
 	if(body1.getUserData() != null && body1.getUserData() instanceof Rocket){
 	  if(body2.getUserData() != null 
 		  && (body2.getUserData() instanceof ResourcePack || body2.getUserData() instanceof Monster)){
 		if(body2.getUserData() instanceof Monster){
 
 		  Rocket rocket = (Rocket)body1.getUserData();
 		  rocket.applyDamage(body2.getUserData());
 		  ((Monster)body2.getUserData()).setAlpha(1f);
 		  ((Monster)body2.getUserData()).setTimer(180);
 
 		  Explosion explosion = new Explosion();
 		  String id = explosion.getId();
 		  while(explosions.containsKey(id)){
 			id = Explosion.OriginalID;
 			int rand = (int)( Math.random() * 10000);
 			id = id.replace("0000", ""+rand);
 		  }
 		  explosion.setId(id);
 
 		  WorldManifold wm = new WorldManifold();
 		  wm.initialize(contact.m_manifold, new Transform(), .1f, new Transform(), .1f);
 		  if(contact.getManifold().pointCount == 1){
 			explosion.setPoint(wm.points[0]);
 		  }
 
 		  explosion.setImage(rocketMonsterExplosion);
 		  explosion.setTimer(15);
 		  explosion.setMaxTimer(explosion.getTimer());
 
 		  Vec2 direction = wm.normal.clone();
 		  Vec2 up = new Vec2(0,1);
 		  float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 		  if(direction.x > 0){
 			angle = -angle;
 		  }
 		  //angle += Math.PI;
 		  explosion.setImageAngle(angle);
 
 		  explosions.put(explosion.getId(), explosion);
 		}
 		contact.setEnabled(false);
 	  }
 
 	  if(contact.m_fixtureB.getUserData() != null && contact.m_fixtureB.getUserData() instanceof Obstacle){
 
 	  }
 	}
 
 	if(body2.getUserData() != null && body2.getUserData() instanceof Rocket){
 	  if(body1.getUserData() != null 
 		  && (body1.getUserData() instanceof ResourcePack || body1.getUserData() instanceof Monster)){
 		if(body1.getUserData() instanceof Monster){
 
 		  Rocket rocket = (Rocket)body2.getUserData();
 		  rocket.applyDamage(body1.getUserData());
 		  ((Monster)body1.getUserData()).setAlpha(1f);
 		  ((Monster)body1.getUserData()).setTimer(180);
 
 		  Explosion explosion = new Explosion();
 		  String id = explosion.getId();
 		  while(explosions.containsKey(id)){
 			id = Explosion.OriginalID;
 			int rand = (int)( Math.random() * 10000);
 			id = id.replace("0000", ""+rand);
 		  }
 		  explosion.setId(id);
 
 		  WorldManifold wm = new WorldManifold();
 		  wm.initialize(contact.m_manifold, body1.getTransform(), .1f, body2.getTransform(), .1f);
 		  if(contact.getManifold().pointCount == 1){
 			explosion.setPoint(wm.points[0]);
 		  }
 
 		  explosion.setImage(rocketMonsterExplosion);
 		  explosion.setTimer(15);
 		  explosion.setMaxTimer(explosion.getTimer());
 
 		  Vec2 direction = wm.normal.clone();
 		  Vec2 up = new Vec2(0,1);
 		  float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 		  if(direction.x > 0){
 			angle = -angle;
 		  }
 		  explosion.setImageAngle(angle);
 
 		  explosions.put(explosion.getId(), explosion);
 		}
 		contact.setEnabled(false);
 	  }
 	}
   }
 
   @Override
   public void postSolve(Contact contact, ContactImpulse impulse) {}
 
   @Override
   public void keyTyped(char c, int code) {}
 
   @Override
   public void keyReleased(char c, int code) {}
 
   @Override
   public void keyPressed(char c, int code) {
 	//Log.print("Key Pressed: "+ c+ "  "+ code);
 	if(model.getCodedKeys()[32]){
 	  if(spacecraft.shoot(config.getWorld())){
 		if(spacecraft.getRocketType() == RocketType.NormalBullet){
 		  float cooldown = spacecraft.getCooldown();
 		  float timestep = 1000f/GameController.DEFAULT_FPS;
 		  cooldownCountDown = (int)(cooldown/timestep);
 		  spacecraft.setOnCD(true);
 		}
 	  }
 
 	}
   }
 
   @Override
   public void mouseUp(Vec2 pos) {}
 
   @Override
   public void mouseDown(Vec2 pos) {
 	//GameModel.getGamePanel().getCustomizedRenderer().drawSolidCircle(pos, 1f, null, Color.white);
   }
 
   @Override
   public void mouseMove(Vec2 pos) {}
 
   private void createMonsterBody(Monster monster){
 	if(monster.getMonsterBody() == null) return;
 	Body body = monster.getMonsterBody();
 
 	PolygonShape mainBody = new PolygonShape();
 	mainBody.setAsBox(.75f, 1.15f, new Vec2(0f,1f), 0f);
 	FixtureDef main = new FixtureDef();
 	main.shape = mainBody;
 	main.density = 2f;
 	main.restitution = .2f;
 	main.filter.groupIndex = Monster.MonsterGroupIndex;
 	body.createFixture(main);
 
 
 	PolygonShape leftArm = new PolygonShape();
 	Vec2 vertices[] = new Vec2[6];
 	vertices[0] = new Vec2(-1.5f,.5f).addLocal(0,0.75f);
 	vertices[1] = new Vec2(-1.5f,-.5f).addLocal(0,0.75f);
 	vertices[2] = new Vec2(-.75f, -.5f).addLocal(0,0.75f);
 	vertices[3] = new Vec2(-.75f, -0f).addLocal(0,0.75f);
 	vertices[4] = new Vec2(-1.2f, -0f).addLocal(0,0.75f);
 	vertices[5] = new Vec2(-1.2f, .5f).addLocal(0,0.75f);
 	leftArm.set(vertices, 6);
 	FixtureDef sd1 = new FixtureDef();
 	sd1.shape = leftArm;
 	sd1.density = 2f;
 	sd1.restitution = .2f;
 	sd1.filter.groupIndex = Monster.MonsterGroupIndex;
 	body.createFixture(sd1);
 
 	PolygonShape rightArm = new PolygonShape();
 	vertices[0] = new Vec2(-vertices[0].x,vertices[0].y);
 	vertices[1] = new Vec2(-vertices[1].x,vertices[1].y);
 	vertices[2] = new Vec2(-vertices[2].x,vertices[2].y);
 	vertices[3] = new Vec2(-vertices[3].x,vertices[3].y);
 	vertices[4] = new Vec2(-vertices[4].x,vertices[4].y);
 	vertices[5] = new Vec2(-vertices[5].x,vertices[5].y);
 	rightArm.set(vertices, 6);
 	FixtureDef sd2 = new FixtureDef();
 	sd2.shape = rightArm;
 	sd2.density = 2f;
 	sd2.restitution = .2f;
 	sd2.filter.groupIndex = Monster.MonsterGroupIndex;
 	body.createFixture(sd2);
 
 	PolygonShape leftLeg = new PolygonShape();
 	leftLeg.setAsBox(0.375f, .25f, new Vec2(-1.125f,-.35f), 0f);
 	FixtureDef sd3 = new FixtureDef();
 	sd3.shape = leftLeg;
 	sd3.density = 2f;
 	sd3.restitution = .2f;
 	sd3.filter.groupIndex = Monster.MonsterGroupIndex;
 	body.createFixture(sd3);
 
 	PolygonShape rightLeg = new PolygonShape();
 	rightLeg.setAsBox(0.375f, .25f, new Vec2(1.125f,-.35f), 0f);
 	FixtureDef sd4 = new FixtureDef();
 	sd4.shape = rightLeg;
 	sd4.density = 2f;
 	sd4.restitution = .2f;
 	sd4.filter.groupIndex = Monster.MonsterGroupIndex;
 	body.createFixture(sd4);
   }
 
   private MovementType randomMovement(BodyDef bd){
 	int rand = (int)(3 * Math.random() + 0.9999999f);
 	MovementType movement;
 	switch (rand){
 	case 1:
 	  movement = MovementType.Linear;
 	  linearMovement(bd);
 	  break;
 	case 2:
 	  movement = MovementType.Circular;
 	  circularMovement(bd);
 	  break;
 	case 3:
 	  movement = MovementType.BackForth;
 	  backForthMovement(bd);
 	  break;
 	default:
 	  movement = MovementType.NoMovement;
 	  break;
 	}
 	return movement;
   }
 
   private void randomMovement(ResourcePack resourcePack){
 	switch (resourcePack.getMovement()){
 	case NoMovement:
 	  break;
 	case Linear:
 	  linearMovement(resourcePack);
 	  break;
 	case Circular:
 	  circularMovement(resourcePack);
 	  break;
 	case BackForth:
 	  backForthMovement(resourcePack);
 	  break;
 	default:
 	  break;
 	}
   }
 
   private void randomMovement(Monster monster){
 	int rand = (int)(4 * Math.random() + 0.9999999f);
 	switch (rand){
 	case 1:
 	  linearMovement(monster);
 	  break;
 	case 2:
 	  //circularMovement(monster);
 	  break;
 	case 3:
 	  backForthMovement(monster);
 	  break;
 	default:
 	  break;
 	}
   }
 
   private void linearMovement(BodyDef bd){
 	double rand = Math.random();
 	int multiplier = 1;
 	if(rand < .5d && rand > 0d){
 	  multiplier = -1;
 	}
 	else if(rand > 0.5d && rand < 1d){
 	  multiplier = 1;
 	}
 	float randX =(float) (5.0f * Math.random() + 0.99999999f) * multiplier;
 
 	rand = Math.random();
 	if(rand < .5d && rand > 0d){
 	  multiplier = -1;
 	}
 	else if(rand > 0.5d && rand < 1d){
 	  multiplier = 1;
 	}
 
 	float randY =(float) (5.0f * Math.random() + 0.99999999f) * multiplier;
 	bd.linearVelocity.set(randX,randY);
   }
 
   private void linearMovement(ResourcePack resourcePack){}
 
   private void linearMovement(Monster monster){
 	if (monster.getMonsterBody() == null) return;
 
 	Body body = monster.getMonsterBody();
 	double rand = Math.random();
 	int multiplier = 1;
 	if(rand < .5d && rand > 0d){
 	  multiplier = -1;
 	}
 	else if(rand > 0.5d && rand < 1d){
 	  multiplier = 1;
 	}
 	float randX =(float) (5.0f * Math.random() + 0.99999999f) * multiplier;
 
 	rand = Math.random();
 	if(rand < .5d && rand > 0d){
 	  multiplier = -1;
 	}
 	else if(rand > 0.5d && rand < 1d){
 	  multiplier = 1;
 	}
 
 	float randY =(float) (5.0f * Math.random() + 0.99999999f) * multiplier;
 	body.setLinearVelocity(new Vec2(randX,randY));
   }
 
   private void circularMovement(BodyDef bd){}
 
   private void circularMovement(ResourcePack resourcePack){
 	//Revolute Joint:
 	//body1 & body2
 	Body resourcePackBody = resourcePack.getResourcePackBody();
 	Body body2 = config.getGroundBody();
 
 	//anchor
 	double randLength = (5f * Math.random() + 4.99999999f);
 	double randAngle = (2 * Math.PI * Math.random());
 	Vec2 shift = new Vec2((float)(randLength * Math.cos(randAngle)), (float)(randLength * Math.sin(randAngle)));
 	Vec2 anchor = resourcePackBody.getWorldCenter().add(shift);
 
 	RevoluteJointDef rjd = new RevoluteJointDef();
 	rjd.initialize(resourcePackBody, body2, anchor);
 	rjd.motorSpeed = MathUtils.PI;
 	rjd.maxMotorTorque = 15.0f;
 	rjd.enableMotor = true;
 	config.getWorld().createJoint(rjd);
   }
 
   private void circularMovement(Monster monster){
 	//Revolute Joint:
 	//body1 & body2
 	Body body = monster.getMonsterBody();
 	Body body2 = config.getGroundBody();
 
 	//anchor
 	double randLength = (5f * Math.random() + 4.99999999f);
 	double randAngle = (2 * Math.PI * Math.random());
 	Vec2 shift = new Vec2((float)(randLength * Math.cos(randAngle)), (float)(randLength * Math.sin(randAngle)));
 	Vec2 anchor = body.getWorldCenter().add(shift);
 
 	RevoluteJointDef rjd = new RevoluteJointDef();
 	rjd.initialize(body, body2, anchor);
 	rjd.motorSpeed = MathUtils.PI;
 	rjd.maxMotorTorque = 100.0f;
 	rjd.enableMotor = true;
 	config.getWorld().createJoint(rjd);
   }
 
   private void backForthMovement(BodyDef bd){}
 
   private void backForthMovement(ResourcePack resourcePack){
 	//Prismatic Joint:
 	//body1 & body2
 	Body resourcePackBody = resourcePack.getResourcePackBody();
 	Body body2 = config.getGroundBody();
 
 	//anchor
 	double randLength = (5f * Math.random() + 4.99999999f);
 	double randAngle = (2 * Math.PI * Math.random());
 	Vec2 shift = new Vec2((float)(randLength * Math.cos(randAngle)), (float)(randLength * Math.sin(randAngle)));
 
 	PrismaticJointDef pjd = new PrismaticJointDef();
 	pjd.initialize(resourcePackBody, body2, resourcePackBody.getWorldCenter(), shift);
 	pjd.lowerTranslation = -1f * (float)randLength;
 	pjd.upperTranslation = 1f * (float)randLength;
 	pjd.enableLimit = true;
 	pjd.motorSpeed = (float)(randLength/4d);
 	pjd.maxMotorForce = 15f;
 	pjd.enableMotor = true;
 	PrismaticJoint joint = (PrismaticJoint)config.getWorld().createJoint(pjd);
 	joint.setUserData(resourcePack);
 	pjForResourcePack.put(resourcePack, joint);
   }
 
   private void backForthMovement(Monster monster){
 	//Prismatic Joint:
 	//body1 & body2
 	Body body1 = monster.getMonsterBody();
 	Body body2 = config.getGroundBody();
 
 	//anchor
 	double randLength = (5f * Math.random() + 4.99999999f);
 	double randAngle = (2 * Math.PI * Math.random());
 	Vec2 shift = new Vec2((float)(randLength * Math.cos(randAngle)), (float)(randLength * Math.sin(randAngle)));
 
 	PrismaticJointDef pjd = new PrismaticJointDef();
 	pjd.initialize(body1, body2, body1.getWorldCenter(), shift);
 	pjd.lowerTranslation = -1f * (float)randLength;
 	pjd.upperTranslation = 1f * (float)randLength;
 	pjd.enableLimit = true;
 	pjd.motorSpeed = (float)(randLength/4d);
 	pjd.maxMotorForce = 100f;
 	pjd.enableMotor = true;
 	PrismaticJoint joint = (PrismaticJoint)config.getWorld().createJoint(pjd);
 	joint.setUserData(monster);
 	pjForMonster.put(monster.getId(), joint);
   }
 
   @Override
   public void customizedPainting() {
 	if (renderer == null) {
 	  return;
 	}
 
 	drawBackground();
 
 	drawTextLines();
 
 	World world = config.getWorld();
 	for(Body b = world.getBodyList(); b!= null; b = b.getNext()){
 	  if(b.getUserData() != null && b.getUserData() instanceof Spacecraft){
 		drawSpacecraft();
 	  }
 	  else if(b.getUserData() != null && b.getUserData() instanceof ResourcePack){
 		drawResourcePack(b);
 	  }
 	  else if(b.getUserData() != null && b.getUserData() instanceof Rocket){
 		drawRocket(b);
 	  }
 	  else if(b.getUserData() != null && b.getUserData() instanceof Monster){
 		drawMonster(b);
 	  }
 	  else if (b.getUserData() == null){
 		for(Fixture f = b.getFixtureList(); f != null; f = f.getNext()){
 		  if(f.getUserData() != null && f.getUserData() instanceof Obstacle){
 			if(f.getUserData() instanceof Bound){
 
 			}else{
 
 			}
 		  }
 		}
 	  }
 	}
 
 	drawLasers();
 	//Handle explosions
 	drawExplosions();
   }
 
   private void drawLasers() {
 
 	if(lasers[0] != null){
 	  renderer.drawSegment(lasers[0].getStart(), lasers[0].getEnd(), lasers[0].getColor());
 
 	  Explosion explosion = new Explosion();
 	  String id = explosion.getId();
 	  while(explosions.containsKey(id)){
 		id = Explosion.OriginalID;
 		int rand = (int)( Math.random() * 10000);
 		id = id.replace("0000", ""+rand);
 	  }
 	  explosion.setId(id);
 
 	  explosion.setPoint(lasers[0].getEnd());
 
 
 	  explosion.setImage(laserMonsterExplosion);
 	  explosion.setTimer(10);
 	  explosion.setMaxTimer(explosion.getTimer());
 
 	  Vec2 direction = lasers[0].getNormal().clone();
 	  Vec2 up = new Vec2(0,1);
 	  float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 	  if(direction.x > 0){
 		angle = -angle;
 	  }
 	  explosion.setImageAngle(angle);
 
 	  explosions.put(explosion.getId(), explosion);
 	}
 
 	if(lasers[1] != null){
 	  renderer.drawSegment(lasers[1].getStart(), lasers[1].getEnd(), lasers[1].getColor());
 
 	  Explosion explosion = new Explosion();
 	  String id = explosion.getId();
 	  while(explosions.containsKey(id)){
 		id = Explosion.OriginalID;
 		int rand = (int)( Math.random() * 10000);
 		id = id.replace("0000", ""+rand);
 	  }
 	  explosion.setId(id);
 
 	  explosion.setPoint(lasers[1].getEnd());
 
 	  explosion.setImage(laserMonsterExplosion);
 	  explosion.setTimer(10);
 	  explosion.setMaxTimer(explosion.getTimer());
 
 	  Vec2 direction = lasers[1].getNormal().clone();
 	  Vec2 up = new Vec2(0,1);
 	  float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 	  if(direction.x > 0){
 		angle = -angle;
 	  }
 	  explosion.setImageAngle(angle);
 
 	  explosions.put(explosion.getId(), explosion);
 	}
 
 	if(lasers[2] != null){
 	  renderer.drawSegment(lasers[2].getStart(), lasers[2].getEnd(), lasers[2].getColor());
 
 	  Explosion explosion = new Explosion();
 	  String id = explosion.getId();
 	  while(explosions.containsKey(id)){
 		id = Explosion.OriginalID;
 		int rand = (int)( Math.random() * 10000);
 		id = id.replace("0000", ""+rand);
 	  }
 	  explosion.setId(id);
 
 	  explosion.setPoint(lasers[2].getEnd());
 
 	  explosion.setImage(laserMonsterExplosion);
 	  explosion.setTimer(10);
 	  explosion.setMaxTimer(explosion.getTimer());
 
 	  Vec2 direction = lasers[2].getNormal().clone();
 	  Vec2 up = new Vec2(0,1);
 	  float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 	  if(direction.x > 0){
 		angle = -angle;
 	  }
 	  explosion.setImageAngle(angle);
 
 	  explosions.put(explosion.getId(), explosion);
 	}
 
 	if(lasers[3] != null){
 	  renderer.drawSegment(lasers[3].getStart(), lasers[3].getEnd(), lasers[3].getColor());
 
 	  Explosion explosion = new Explosion();
 	  String id = explosion.getId();
 	  while(explosions.containsKey(id)){
 		id = Explosion.OriginalID;
 		int rand = (int)( Math.random() * 10000);
 		id = id.replace("0000", ""+rand);
 	  }
 	  explosion.setId(id);
 
 	  explosion.setPoint(lasers[3].getEnd());
 
 
 	  explosion.setImage(laserMonsterExplosion);
 	  explosion.setTimer(10);
 	  explosion.setMaxTimer(explosion.getTimer());
 
 	  Vec2 direction = lasers[3].getNormal().clone();
 	  Vec2 up = new Vec2(0,1);
 	  float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 	  if(direction.x > 0){
 		angle = -angle;
 	  }
 	  explosion.setImageAngle(angle);
 
 	  explosions.put(explosion.getId(), explosion);
 	}
   }
 
   private void drawTextLines() {
 	LinkedListItem<TextLine> curr = new LinkedListItem<TextLine>();
 	for(curr = textLines.getHead(); curr != null; curr = curr.getNext()){
 	  TextLine textline = curr.getElement();
 
 	  if(textline.getTimer() > 0 && textline.getTextline() <= 140){
 		textline.setTimer(textline.getTimer()-1);
 		renderer.drawStringWithTransparency(5f, textline.getTextline(), 
 			textline.getString(), textline.getColor(), textline.getAlpha());
 		if(textline.getTimer() <= textline.getTimeStartFading()){
 		  textline.decreaseTransparency();
 		}
 	  }
 	  else if(textline.getTimer() <= 0){
 		try {
 		  textLines.remove(curr.getElement());
 		} catch (ElementNotExistException e) {
 		  e.printStackTrace();
 		}
 	  }
 	}
   }
 
   private void drawExplosions() {
 	for(Map.Entry<String, Explosion> entry : explosions.entrySet()){
 	  Explosion explosion = entry.getValue();
 	  if(explosion.getTimer() > 0){
 		explosion.setTimer(explosion.getTimer()-1);
 
 		float rand = (float)(3*Math.random());
 		renderer.drawImageWithTransparency(explosion.getPoint(),rand, rand,
 			explosion.getImage(), explosion.getImageAngle(),explosion.getAlpha());
 		explosion.decreaseTransparency();
 	  }
 	  else{
 		explosions.remove(explosion.getId());
 	  }
 	}
   }
 
   private void drawBackground() {
 	Vec2 offsetToWorldCenter = spacecraft.getSpacecraftBody().getWorldCenter().clone();
 	offsetToWorldCenter.subLocal(GameModel.getGamePanelRenderer().getViewportTranform().getCenter());
 	//offsetToWorldCenter.subLocal(worldCenter);
 
 	Vec2  imageCenter = new Vec2(background.getWidth(null)/2,background.getHeight(null)/2);
 	renderer.drawBackgroundImage(background, imageCenter, offsetToWorldCenter,1);
 	//renderer.drawStaticBackgroundImage(background);
   }
 
   private void drawRocket(Body body) {
 	Rocket rocket = (Rocket) body.getUserData();
 	if(rocket.getType() == RocketType.NormalBullet){
 	  if(rocket.getImage() == null) return;
 	  renderer.drawImage(body.getWorldCenter(), .3f, .3f, 
 		  rocket.getImage(), 0f);
 	}
   }
 
   private void drawMonster(Body body) {
 	Monster monster = (Monster) body.getUserData();
 	float maxHp = monster.getMaxHp();
 	float hp = monster.getHp();
 	float percentage = hp/maxHp;
 	renderer.drawRectWithTransparency(body.getWorldCenter().add(new Vec2(0,2f)), 3f, .5f, Color.white,monster.getAlpha());
 	renderer.fillRectFromTopLeftWithTransparency(body.getWorldCenter().add(new Vec2(0,2f)).add(new Vec2(-3f/2f, .5f/2f)),
 		3f * percentage, .5f, Color.red,monster.getAlpha());
 	if(monster.getMonsterImg() == null) return;
 	renderer.drawImage(body.getWorldCenter(), 3f, 3f, monster.getMonsterImg(), 0f);	
 
 	if(monster.getTimer() > 0){
 	  monster.setTimer(monster.getTimer()-1);
 	  if(monster.getTimer() <= monster.getTimeStartFading()){
 		monster.decreaseTransparency();
 		if(monster.getAlpha() < 0){
 		  monster.setAlpha(0f);
 		}
 	  }
 	}
   }
 
   private void drawResourcePack(Body body) {
 	//calculate the angle:
 	ResourcePack pack = (ResourcePack) body.getUserData();
 	Vec2 up = new Vec2(0,1);
 	Vec2 head = body.getWorldPoint(up);
 	Vec2 direction = head.sub(body.getWorldCenter());
 	float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 	if(direction.x > 0){
 	  angle = -angle;
 	}
 
 	if(pack.getImage() == null) return;
 	renderer.drawImage(body.getWorldCenter(), 1.5f, 1.5f, pack.getImage(), angle);
   }
 
   private void drawSpacecraft() {
 	//calculate the angle:
 	Body body = spacecraft.getSpacecraftBody();
 	Vec2 head = body.getWorldPoint(new Vec2(0,1));
 	Vec2 direction = head.sub(body.getWorldCenter()); 
 	Vec2 up = new Vec2(0,1);
 	float angle = (float)Math.acos((Vec2.dot(direction, up))/(direction.length() * up.length()));
 	if(direction.x > 0){
 	  angle = -angle;
 	}
 	angle += Math.PI;
 
 	renderer.drawImage(body.getWorldCenter(), 3f, 3f, spacecraftImg, angle);
   }
 
   private void shootLaser() {
 	if(spacecraft.isOnCD()) return;
 	if(config.getConfigSettings().getSetting(ConfigSettings.DrawShapes).enabled) return;
 
 	Vec2 leftSpawnPt = null;
 	Vec2 rightSpawnPt = null;
 
 	int rocketNum = spacecraft.getRocketNumPerShot();
 	if(rocketNum == 2){
 	  leftSpawnPt = spacecraft.getSpacecraftBody().getWorldPoint(new Vec2(-.8f,-.4f));
 	  rightSpawnPt = spacecraft.getSpacecraftBody().getWorldPoint(new Vec2(.8f,-.4f));
 	}
 	else if(rocketNum == 4){
 	  //Double laser
 	}
 	else{
 	  Log.print("Unexpected Error: Invalid number of rockets per shot.");
 	  return;
 	}
 
 	LaserCastClosestCallback closestCallback = new LaserCastClosestCallback();
 	if(rocketNum == 2 && leftSpawnPt != null && rightSpawnPt != null){
 	  if(spacecraft.getRocketType() == RocketType.Laser){
 		closestCallback.init();
 		Vec2 direction = spacecraft.getSpacecraftBody().getWorldPoint(new Vec2(0f,-1f));
 		direction.subLocal(spacecraft.getSpacecraftBody().getWorldCenter().clone());
 		direction.normalize();
 		direction.mulLocal(70f);
 		//left beam
 		Vec2 leftEndPt = leftSpawnPt.add(direction);
 		config.getWorld().raycast(closestCallback, leftSpawnPt, leftEndPt);
 		if(closestCallback.hit){
 		  Laser laser = new Laser();
 		  laser.setStart(leftSpawnPt);
 		  laser.setEnd(closestCallback.point);
 		  laser.setColor(new Color(51,255,255,255));
 		  laser.setNormal(closestCallback.normal);
 		  lasers[0] = laser;
 		  if(closestCallback.fixture.getBody().getUserData() != null 
 			  && closestCallback.fixture.getBody().getUserData() instanceof Monster){
 			Rocket temp = new Rocket();
 			temp.setBaseDamage(1);
 			temp.applyDamage(closestCallback.fixture.getBody().getUserData());
 			((Monster)closestCallback.fixture.getBody().getUserData()).setAlpha(1f);
 			((Monster)closestCallback.fixture.getBody().getUserData()).setTimer(180);
 			if(((Monster)closestCallback.fixture.getBody().getUserData()).getHp() <= 0){
 			  ((Monster)closestCallback.fixture.getBody().getUserData()).setHp(0);
 			  lasers[0].setKilledMonster((Monster)closestCallback.fixture.getBody().getUserData());
 			}
 		  }
 		}
 
 		//right beam
 		closestCallback.init();
 		Vec2 rightEndPt = rightSpawnPt.add(direction);
 		config.getWorld().raycast(closestCallback, rightSpawnPt, rightEndPt);
 		if(closestCallback.hit){
 		  Laser laser = new Laser();
 		  laser.setStart(rightSpawnPt);
 		  laser.setEnd(closestCallback.point);
 		  laser.setColor(new Color(51,255,255,255));
 		  laser.setNormal(closestCallback.normal);
		  lasers[1] = laser;
 		  if(closestCallback.fixture.getBody().getUserData() != null 
 			  && closestCallback.fixture.getBody().getUserData() instanceof Monster){
 			Rocket temp = new Rocket();
 			temp.setBaseDamage(1);
 			temp.applyDamage(closestCallback.fixture.getBody().getUserData());
 			((Monster)closestCallback.fixture.getBody().getUserData()).setAlpha(1f);
 			((Monster)closestCallback.fixture.getBody().getUserData()).setTimer(180);
 			if(((Monster)closestCallback.fixture.getBody().getUserData()).getHp() <= 0){
 			  ((Monster)closestCallback.fixture.getBody().getUserData()).setHp(0);
 			  lasers[1].setKilledMonster((Monster)closestCallback.fixture.getBody().getUserData());
 			}
 		  }
 		}
 	  }
 	}
   }
 }
