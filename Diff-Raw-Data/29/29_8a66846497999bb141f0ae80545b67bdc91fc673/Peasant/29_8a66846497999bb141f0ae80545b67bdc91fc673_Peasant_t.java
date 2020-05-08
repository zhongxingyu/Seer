 package mob;
 
 import java.util.Random;
 
 import buildings.Building;
 import buildings.BuildingTypes;
 
 import resorsers.Resource;
 import sprite.Orientation;
 import sprite.Sprite;
 import sprite.SpriteTypes;
 import world.World;
 import mesh.Material;
 import mesh.Mesh;
 import model.Model;
 
 public class Peasant extends Sprite{
 	private final float scale = 0.008f;
 	private Sprite head, rightArm, leftArm, rightLeg, leftLeg, item;
 	private Mesh toolMesh;
 	private double d = 0;
 	private int time = (int) ((Math.random() * 1800) % 1800);
 	private Model model;
 	private boolean faceCam = false, walk = false, idle = false, hitAnime = false, hasResource = false, swim = false;
 	private float desX = 0.0f, desZ = 0.0f, originX = 0, originZ = 0, baseRot, distance, wanderDistance = 0.08f, speed = 0.001f;
 	private Object target;
 	private Task task = Task.NONE;
 	private World world;
 	
 	public Peasant(float x, float z, Mesh h, Mesh body, Material mat, Model m, Mesh ra, Mesh la, Mesh rl, Mesh ll, Mesh itemMesh, Task t, World w){
 		super(body);
 		super.setTex(mat);
 		originX = x;
 		originZ = z;
 		super.setWidth(scale);
 		super.setHeight(scale);
 		super.setDepth(scale);
 		model = m;
 		world = w;
 		
 		head = new Sprite(h);
 		head.setWidth(scale);
 		head.setHeight(scale);
 		head.setDepth(scale);
 		head.setX(x);
 		head.setZ(z);
 		head.setY(super.getMesh()[0].getTop() * scale);
 		head.setTex(mat);
		//head.setParent(this);
 
 		
 		rightArm = new Sprite(ra);
 		rightArm.setYRot(-90);
 		rightArm.setWidth(scale);
 		rightArm.setHeight(scale);
 		rightArm.setDepth(scale);
 		rightArm.setY(super.getMesh()[0].getTop() * scale - (rightArm.getMesh()[0].getTop() * scale));
 		rightArm.setTex(mat);
 		rightArm.setParent(this);
 		rightArm.setYRot(0.0f);
 		
 		leftArm = new Sprite(la);
 		leftArm.setYRot(-90);
 		leftArm.setWidth(scale);
 		leftArm.setHeight(scale);
 		leftArm.setDepth(scale);
 		leftArm.setY(super.getMesh()[0].getTop() * scale - (rightArm.getMesh()[0].getTop() * scale));
 		leftArm.setTex(mat);
 		leftArm.setParent(this);
 		leftArm.setYRot(0.0f);
 		
 		rightLeg = new Sprite(rl);
 		rightLeg.setWidth(scale);
 		rightLeg.setHeight(scale);
 		rightLeg.setDepth(scale);
 		rightLeg.setY(super.getMesh()[0].getBottom() * scale - (rightLeg.getMesh()[0].getTop() * scale));
 		rightLeg.setTex(mat);
 		rightLeg.setParent(this);
 		
 		leftLeg = new Sprite(ll);
 		leftLeg.setWidth(scale);
 		leftLeg.setHeight(scale);
 		leftLeg.setDepth(scale);
 		leftLeg.setY(super.getMesh()[0].getBottom() * scale - (leftLeg.getMesh()[0].getTop() * scale));
 		leftLeg.setTex(mat);
 		leftLeg.setParent(this);
 		
 		toolMesh = itemMesh;
 		item = new Sprite(itemMesh);
 		item.setWidth(scale);
 		item.setHeight(scale);
 		item.setDepth(scale);
 		item.setParent(leftArm);
 		item.cullFace(false);
 		
 		m.getSpriteList().add(item);
 		m.getSpriteList().add(leftLeg);
 		m.getSpriteList().add(rightLeg);
 		m.getSpriteList().add(leftArm);
 		m.getSpriteList().add(rightArm);
 		m.getSpriteList().add(head);
 		if(getDistance(originX - desX, originZ - desZ) != 0){
 			distance = getDistance(originX - desX, originZ - desZ);
 		}
 		
 		setTask(t);
 	}
 	
 	public void update(){
 		//basic movement
 		super.setX(interpolate(originX, desX, d));
 		super.setZ(interpolate(originZ, desZ, d));
 		super.update();
 		head.setX(super.getX());
 		head.setZ(super.getZ());
 		
 		animate();
 		
 		if(task == Task.NONE){
 			idle = true;
 		}else{
 			if(target == null && !idle){
 				findTarget();
 				calcTarget();
 			}
 		}
 		
 		if(d < 1.0 && walk){
 			d += speed / distance;
 		}else{
 			doTask();
 		}
 		
 		setTool();
		
		//System.out.println(super.getParent());
 	}
 	
 	public void setTool(){
 		if(hasResource){
 			switch(task){
 			case STONE:{
 				item.setMesh(model.getModel("itemrock"));
 			}break;
 			case WOOD:{
 				item.setMesh(model.getModel("itemtree"));
 			}break;
 			}
 		}else{
 			item.setVisible(true);
 			item.setMesh(toolMesh);
 			switch(task){
 			case STONE:{
 				item.setTex(model.getMaterial("pickaxe"));
 			}break;
 			case WOOD:{
 				item.setTex(model.getMaterial("woodaxe"));
 			}break;
 			case BUILD:{
 				item.setTex(model.getMaterial("hammer"));
 			}break;
 			case SOLDIER:{
 				item.setTex(model.getMaterial("sword"));
 			}break;
 			default:{
 				idle = true;
 				item.setVisible(false);
 			}break;
 			}
 		}
 	}
 	
 	private void calcTarget(){
 		originX = super.getX();
 		originZ = super.getZ();
 		
 		if(!idle){
 			walk = true;
 			if(task == Task.BUILD){
 				desX = ((Orientation) target).getX();
 				desZ = ((Orientation) target).getZ();	
 			}else{
 				desX = ((Orientation) target).getX();
 				desZ = ((Orientation) target).getZ();
 			}
 		}else{
 			if(time % 100 == 0){
 				desX = originX + (new Random().nextFloat() * (wanderDistance * 2) - wanderDistance);
 				desZ = originZ + (new Random().nextFloat() * (wanderDistance * 2) - wanderDistance);
 				walk = true;
 			}else{
 				desX = originX;
 				desZ = originZ;
 				walk = false;
 			}
 		}
 		
 		check(desX, desZ, super.getX(), super.getZ(), -1.0f, -0.3f, 1.0f, -0.3f);
 		
 		d = 0;
 		if(getDistance(originX - desX, originZ - desZ) != 0){
 			distance = getDistance(originX - desX, originZ - desZ);
 		}else{
 			
 		}
 	}
 	
 	public void check(double x00, double y00, double x01, double y01, double x10, double y10, double x11, double y11){
 		double m0 = (y01-y00)/(x01-x00);
 		double m1 = (y11-y10)/(x11-x10);
 			 
 		double q0 = y00 - m0 * x00;
 		double q1 = y10 - m1 * x10;
 			 
 		if(m0 == m1){
 			if(q0 == q1){
 				//System.out.println("SAME RECT");
 			}
 			else{
 				//System.out.println("THEY WILL NEVER COLLIDE");
 			}
 		}else{
 			//System.out.println("huh");
 		}
 		
 		double collision = (q1-q0)/(m1-m0);
 		
 		if(x00 <= collision && collision <= x01 && x10 <=collision && collision <=x11){
 			//System.out.println("THE SEGMETS COLLIDED AT" + collision);
 		}
 		
 		//System.out.println(collision);
 	}
 	
 	public void doTask(){
 		walk = true;
 		hitAnime = false;
 		
 		if(idle){
 			findTarget();
 			calcTarget();
 			return;
 		}
 		
 		switch(task){
 		case BUILD:{
 			walk = false;
 			hitAnime = true;
 			if(!((Building) target).isBuilt()){
 				((Building) target).build();
 			}else{
 				findTarget();
 				calcTarget();
 			}
 		}break;
 		case STONE:{
 			walk = false;
 			hitAnime = true;
 			
 			if(hasResource){
 				hasResource = false;
 				world.ROCK++;
 				findTarget();
 				calcTarget();
 			}
 			
 			if(((Sprite) target).getType() == SpriteTypes.ROCK){
 				if(((Resource) target).harvest()){
 					hasResource = true;
 					findTarget();
 					calcTarget();
 				}
 			}
 		}break;
 		case WOOD:{
 			walk = false;
 			hitAnime = true;
 			
 			if(hasResource){
 				hasResource = false;
 				world.WOOD++;
 				findTarget();
 				calcTarget();
 			}
 			
 			if(((Sprite) target).getType() == SpriteTypes.TREE){
 				if(((Resource) target).harvest()){
 					hasResource = true;
 					findTarget();
 					calcTarget();
 				}
 			}
 		}break;
 		default:{
 			calcTarget();
 		}break;
 		}
 	}
 	
 	public void findTarget(){
 		hitAnime = false;
 		switch(task){
 		case BUILD:{
 			Building current = null;
 			idle = false;
 			for(int i = 0; i < model.getBuildingList().size(); i++){
 				if(!model.getBuildingList().get(i).isBuilt() && model.getBuildingList().get(i) != target){
 					if(current == null){
 						current = model.getBuildingList().get(i);
 					}
 					if(getDistance(model.getBuildingList().get(i).getX() - super.getX(), model.getBuildingList().get(i).getZ() - super.getZ()) 
 							< getDistance(current.getX() - super.getX(),current.getZ() - super.getZ()) 
 							&& model.getBuildingList().get(i).getWorkers() <= current.getWorkers()){
 						
 						current = model.getBuildingList().get(i);
 					}
 				}
 			}
 			target = current;
 			if(current == null){
 				idle = true;
 			}else{
 				((Building) target).addWorker();
 			}
 			
 		}break;
 		case STONE:{
 			boolean hasMason = false;
 			for(int i = 0; i < model.getBuildingList().size(); i++){
 				if(model.getBuildingList().get(i).isBuilt() 
 						&& model.getBuildingList().get(i).getBuildingType() == BuildingTypes.MASONRY){
 					hasMason = true;
 				}
 			}
 			if(!hasMason){
 				idle = true;
 				return;
 			}else{
 				idle = false;
 			}
 			Sprite current = null;
 			for(int i = 0; i < model.getSpriteList().size(); i++){
 				if(model.getSpriteList().get(i).getType() == SpriteTypes.ROCK){
 					if(current == null ){
 						current = model.getSpriteList().get(i);
 					}
 					if(getDistance(model.getSpriteList().get(i).getX() - super.getX(), model.getSpriteList().get(i).getZ() - super.getZ()) 
 							< getDistance(current.getX() - super.getX(),  current.getZ() - super.getZ()) 
 							&& !((Resource) model.getSpriteList().get(i)).isClaimed(this)){
 						current = model.getSpriteList().get(i);
 						
 					}
 				}
 			}
 			if(current != null){
 				target = current;
 				
 				((Resource) target).claim(this);
 			}
 			
 			if(hasResource){
 				for(int i = 0; i < model.getBuildingList().size(); i++){
 					if(model.getBuildingList().get(i).getBuildingType() == BuildingTypes.MASONRY 
 							&& model.getBuildingList().get(i).isBuilt()){
 						current = model.getBuildingList().get(i);
 					}
 				}
 				
 				if(current == null){
 					target = null;
 					idle = true;
 				}else{
 					target = current;
 				}
 			}
 		}break;
 		case WOOD:{
 			boolean hasShop = false;
 			for(int i = 0; i < model.getBuildingList().size(); i++){
 				if(model.getBuildingList().get(i).isBuilt() 
 						&& model.getBuildingList().get(i).getBuildingType() == BuildingTypes.WORKSHOP){
 					hasShop = true;
 				}
 			}
 			if(!hasShop){
 				idle = true;
 				return;
 			}else{
 				idle = false;
 			}
 			
 			Sprite current = null;
 			
 			for(int i = 0; i < model.getSpriteList().size(); i++){
 				if(model.getSpriteList().get(i).getType() == SpriteTypes.TREE){
 					//System.out.println("found basic index");
 					if(current == null){
 						current = model.getSpriteList().get(i);
 					}
 					if(getDistance(model.getSpriteList().get(i).getX() - super.getX(), model.getSpriteList().get(i).getZ() - super.getZ()) 
 							< getDistance(current.getX() - super.getX(),  current.getZ() - super.getZ()) 
 							&& !((Resource) model.getSpriteList().get(i)).isClaimed(this)){
 						current = model.getSpriteList().get(i);
 					}
 				}
 			}
 			
 			if(current != null){
 				target = current;
 				
 				((Resource) target).claim(this);
 			}
 			
 			if(hasResource){
 				for(int i = 0; i < model.getBuildingList().size(); i++){
 					if(model.getBuildingList().get(i).getBuildingType() == BuildingTypes.WORKSHOP
 							&& model.getBuildingList().get(i).isBuilt()){
 						current = model.getBuildingList().get(i);
 					}
 				}
 				
 			}
 			
 			if(current == null){
 				target = null;
 				idle = true;
 				//System.out.println("tree not found");
 			}else{
 				target = current;
 			}
 		}break;
 		default:{
 			
 		}break;
 		}
 	}
 	
 	public void getSprite(){
 		
 	}
 	
 	private void animate(){
 		if(idle){
 			speed = 0.0005f;
 		}else{
 			speed = 0.001f;
 		}
 		
 		if(time > 1800) time = 0;
 		else time++;
 		
 		baseRot = (-(float)Math.toDegrees(Math.atan2(originZ - desZ, originX - desX))) - 180;
 		head.setZRot((float) ((float)(Math.sin((float)time / 15) * 5)));
 		head.setXRot(((float)(Math.sin((float)time / 25) * 2)));
 		if(walk){
 			super.setYRot((float) (baseRot + (Math.sin((float)time / 10) * 10)));
 			head.setYRot(baseRot + (float) ((Math.sin((float)time / 50) * 7)));
 			//rightArm.setXRot((float)(Math.sin((float)time / 20) * 10));
 			rightArm.setZRot((float)(Math.sin((float)time / 10) * 20));
 			leftArm.setZRot((float)-(Math.sin((float)time / 10) * 20));
 			rightLeg.setZRot((float)(Math.sin((float)time / 8) * 30));
 			leftLeg.setZRot((float)-(Math.sin((float)time / 8) * 30));
 		}else{
 			rightArm.setZRot((float)(Math.sin((float)time / 20) * 5));
 			leftArm.setZRot((float)-(Math.sin((float)time / 20) * 5));
 			rightLeg.setZRot((float)(Math.sin((float)time / 50) * 3));
 			leftLeg.setZRot((float)-(Math.sin((float)time / 50) * 3));
 		}
 		rightArm.setXRot((float)(Math.sin((float)time / 20) * 3));
 		leftArm.setXRot((float)-(Math.sin((float)time / 20) * 3));
 		if(faceCam){
 			head.setYRot(-model.getCamera().getYRot() - 90);
 			if(model.getCamera().getXRot() < 65 && model.getCamera().getXRot() > -35){
 				head.setZRot(model.getCamera().getXRot());
 			}
 			if(Math.abs(head.getYRot() - super.getYRot()) > 60){
 				if(head.getYRot() > super.getYRot()){
 					super.setYRotVel(Math.abs(head.getYRot() - super.getYRot()) / 10);
 				}else{
 					super.setYRotVel(-(Math.abs(head.getYRot() - super.getYRot()) / 10));
 				}
 			}else{
 				super.setYRotVel(0.0f);
 			}
 		}
 		
 		if(hitAnime){
 			leftArm.setZRot((float)Math.sin(leftArm.getZRot() * 1.0f) * 50.0f + 50.0f);
 		}
 		
 		if(hasResource){
 			leftArm.setZRot((float)Math.sin(time / 20.0f) * 10.0f + 100.0f);
 			rightArm.setZRot((float)Math.sin(-time / 20.0f) * 10.0f + 100.0f);
 		}
 		
 		if(swim){
 			super.setY((float)Math.sin(time / 20.0f) * 0.005f - 0.03f);
 		}else{
 			super.setY(0.0f);
 		}
 		
 		//swim = true;
 	}
 	
 	public void setTask(Task t){
 		item.setVisible(true);
 		task = t;
 		idle = false;
 		item.setMesh(toolMesh);
 		switch(t){
 		case STONE:{
 			item.setTex(model.getMaterial("pickaxe"));
 		}break;
 		case WOOD:{
 			item.setTex(model.getMaterial("woodaxe"));
 		}break;
 		case BUILD:{
 			item.setTex(model.getMaterial("hammer"));
 		}break;
 		case SOLDIER:{
 			item.setTex(model.getMaterial("sword"));
 		}break;
 		default:{
 			idle = true;
 			item.setVisible(false);
 		}break;
 		}
 	}
 	
 	public float interpolate(float a, float b, double t){
 		return (float) (a * (1 - t) + b * t);
 	}
 	
 	public float getDistance(float a, float b){
 		return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
 	}
 	
 	public float getDesX(){
 		return desX;
 	}
 	public float getDesZ(){
 		return desZ;
 	}
 }
