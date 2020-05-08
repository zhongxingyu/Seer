 package com.matrunks.shooter.models;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.matrunks.shooter.models.RagDoll;
 import com.matrunks.shooter.Assets;
 import com.matrunks.shooter.screens.GameScreen;
 import com.badlogic.gdx.math.MathUtils;
 import java.lang.Math;
 
 import java.util.Collections;
 
 public class World {
 	public static RagDoll rag_doll;
 	public static Player pj;
 	public static LevelManager level;
 	public static Map map;
 	public static Collisions collisions;
 	public float ShotSecs=0;
 	public float RagdollReloadSecs=0;
 	public static Cover cover;
 	public int indice;
 	public static ArrayList<GameObject> objects;
 	public static ArrayList<GameObject> initialobjects;
 	public static ArrayList<GameObject> shaders;
 	public Shader shot;
 	private MathUtils math;
 	private boolean bool=false;
 	
 	public World(){
 		level = new LevelManager();
 		rag_doll = new RagDoll();
 		objects = new ArrayList<GameObject>();
 		initialobjects = new ArrayList<GameObject>();
 		shaders = new ArrayList<GameObject>();
 		map = new Map();
 		for(int i=0;i<13;i++){
 			cover = new Cover(math.random(-50,map.width()),math.random(0,map.height()));
 			initialobjects.add(cover);
 		}
 		objects = (ArrayList<GameObject>) initialobjects.clone();
 		collisions = new Collisions();
 		initialize();
 	}
 	
 	public void update (float delta){
 		ShotSecs+=delta; //vamos acumulando los segundos
 		RagdollReloadSecs+=delta;
 		
 		//aqui metemos los cambios efectuados en los objetos
 		if(Gdx.input.isTouched()){ //si es pulsada la pantalla
 			if(pj.checkGun()){//Comprobamos el enfriamiento del arma
 				//la cmara proyecta la imagen a escala de la pantalla fsica
 				GameScreen.camera.unproject(GameScreen.touchPoint.set(Gdx.input.getX(), Gdx.input.getY(),0));
 				//obtengo los metros desde los pxeles gracias a la cmara y lo guardo en touchPoint
 
 				//si el mueco no est en una cobertura
 				if(!rag_doll.isHidden()){
 						if(hitOnRagDoll(rag_doll)){
 							System.out.println("Hit");
 							rag_doll.damage(10); //le hacemos dao
 							rag_doll.Freeze();
 							Assets.hit.play();
 						}
 			    }
 				//si no hemos dado al queco, disparo con shader
				if((!hitOnRagDoll(rag_doll) || rag_doll.isHidden()) && GameScreen.touchPoint.y < map.height()){ //compruebo no dejar marca en el aire
 						shot = new Shader(0, (int)GameScreen.touchPoint.x, (int)GameScreen.touchPoint.y);
 						shaders.add(shot);
 				}
 				Assets.disparo.play(0.3f); //sonido de disparo	
 			}
 		}
 		
 		//actualizamos los datos del mueco y personaje
 		rag_doll.update(delta);
 		pj.update(delta);
 		collisions.update(rag_doll,map);
 		
 		//Si est listo y no est escondido, disparar
 		if(rag_doll.isReady() && !rag_doll.isHidden()){
 			Assets.pium.play();
 			Gdx.input.vibrate(500);
 			pj.takeDamage(rag_doll.getDamage());
 			rag_doll.Shoot();
 			rag_doll.NotReady();
 		}
 		
 		//si muere aumentamos el nivel
 		if(!rag_doll.isAlive()){
 			Assets.dead.play();
 			level.setRecord();
 			initialize();
 		}
 		
 		//si muere el jugador, reiniciamos el nivel
 		if(!pj.isAlive()){
 			level.reset();
 			objects = (ArrayList<GameObject>) initialobjects.clone(); //necesitamos los objetos como al principio
 			initialize();
 		}
 		
 		//Comprobamos donde se encuentra el mueco y si est tras cobertura cambiamos su estado
 		checkRagdollCover();
 		
 		//Reordenamos los objetos del juego
 		Collections.sort(objects);
 	}
 	
 	public void initialize(){
 		shaders.clear();
 		level.incrementLevel(); //se incrementa el nivel ya que level empieza en 0
 		pj = new Player();
 		rag_doll.reset();
 		level.update(objects,rag_doll,map); //hacemos un update de los rboles mueco y mapa
 		Collections.sort(objects); //ordenamos los objetos
 	}
 	
 	public void checkRagdollCover(){
 		bool=false;
 		for(int i=0; i!=objects.size();i++){ //le sumo y resto ya que el arbol por los laterales tiene partes traspasables
 			if(rag_doll.position.x > objects.get(i).position.x && rag_doll.position.x+rag_doll.width < objects.get(i).position.x+objects.get(i).width+5 && rag_doll.position.y < objects.get(i).position.y+objects.get(i).height+5 && rag_doll.position.y+5 > objects.get(i).position.y){
 				if(!rag_doll.isHidden()){
 					rag_doll.hide();
 				}	
 				bool=true;
 			}
 		}
 		
 		if(rag_doll.isHidden() && bool==false){
 			rag_doll.notHide();
 		}
 	}
 	
 	public boolean hitOnObject(Vector3 touchPoint, float x, float y, int width, int height){
 		if(touchPoint.x > x && touchPoint.x < x +width && touchPoint.y > y && touchPoint.y < y+height){
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean hitOnRagDoll(RagDoll ragdoll){
 		return (hitOnObject(GameScreen.touchPoint,ragdoll.position.x-30, ragdoll.position.y-30,ragdoll.width()+60, ragdoll.height()+60));
 	}
 	
 	//actualmente no se usa esta funcin
 	/*public boolean hitOnCovers(ArrayList<GameObject> objects){
 		for(int i=0; i!=objects.size();i++){ //le sumo y resto ya que el arbol por los laterales tiene partes traspasables
 			if(objects.get(i)!=rag_doll){
 				if(hitOnObject(GameScreen.touchPoint,objects.get(i).position.x+30, objects.get(i).position.y, objects.get(i).width-60, objects.get(i).height)){
 					//en este if comprobamos si el mueco y el arbol estn en la misma x, evaluamos la y, si el mueco est por debajo del arbol devolvemos false (la covertura no le cubre)
 					if(rag_doll.position.x > objects.get(i).position.x && rag_doll.position.x < objects.get(i).position.x + objects.get(i).width && rag_doll.position.y < objects.get(i).position.y){
 						return false;
 					}
 					else{
 						System.out.println("Cover");
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}*/
 	
 	public void dispose(){
 		//limpiamos todos los objetos
 		for(int i=0; i !=objects.size(); i++){
 			objects.get(i).dispose();
 		}
 		for(int i=0; i !=initialobjects.size();i++){
 			initialobjects.get(i).dispose();
 		}
 		for(int i=0; i !=shaders.size(); i++){
 			shaders.get(i).dispose();
 		}
 	}
 	
 	public static ArrayList<GameObject> objects(){
 		return objects;
 	}
 	
 	public static ArrayList<GameObject> shaders(){
 		return shaders;
 	}
 }
 
 
