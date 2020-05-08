 package com.ladybug.engine.gameobject;
 
 import com.ladybug.engine.game.Global;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 
 import com.ladybug.engine.components.BoxCollider;
 import com.ladybug.engine.components.Collider;
 import com.ladybug.engine.components.Component;
 import com.ladybug.engine.components.Rigidbody;
 import com.ladybug.engine.components.Script;
 import com.ladybug.engine.components.Component.TYPE;
 
 public class GameObject extends Sprite {
 	public String m_name;
	//RENDERER
 	public String m_textureName;
	public int m_textureHeight, m_textureWidth;
 	
 	//Last position known
 	public Vector2 m_oldPos;
 	
 	//Visibilty
 	public boolean visible = true;
 	
 	//COMPONENTS
 	protected ArrayList<Component> m_components = new ArrayList<Component>();
 	public Rigidbody rigidbody;
 	public Collider collider;
 	public GameObject parent;
 	
 	//Children
 	protected ArrayList<GameObject> m_children = null;
 	
 	public GameObject(){
 		m_initialPosition = new Vector2();
 		m_name = "GameObject";
 	};
 	public GameObject(float x, float y){
 		m_initialPosition = new Vector2(x,y);
 		setPosition(x, y);
 		m_name = "GameObject";
 	};
 	
 	public GameObject(String name){
 		m_initialPosition = new Vector2();
 		m_name = name;
 	};
 	public GameObject(float x, float y,String name){
 		m_initialPosition = new Vector2(x,y);
 		setPosition(x, y);
 		m_name = name;
 	};
 	
 
 	//-----------------------------------------
 	//				COLLISION
 	//-----------------------------------------
 	
 	public void OnCollisionEnter(Collider collider){
 		//oncollisionenter
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				Component c = m_components.get(i);
 				if(c.enabled){
 					if(c.getType() == Component.TYPE.SCRIPT)
 						((Script)c).OnCollisionEnter(collider);
 				}
 			}
 		}
 	}
 	
 	public void OnCollisionStay(Collider collider){
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				Component c = m_components.get(i);
 				if(c.enabled){
 					if(c.getType() == Component.TYPE.SCRIPT)
 						((Script)c).OnCollisionStay(collider);
 				}
 			}
 		}
 	}
 	
 	public void OnCollisionExit(Collider collider){
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				Component c = m_components.get(i);
 				if(c.enabled){
 					if(c.getType() == Component.TYPE.SCRIPT)
 						((Script)c).OnCollisionExit(collider);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Called from a child : this resets the collider of the gameobject
 	 * to include all of its children's colliders
 	 */
 	public void updateCollider(){
 		if(m_children == null)
 			return;
 		if(collider == null)
 			addComponent(new BoxCollider());
 		Vector2 min = m_children.get(0).collider.min;
 		Vector2 max = m_children.get(0).collider.max;
 		for(int i = 1 ; i < m_children.size(); i++){
 			GameObject go = m_children.get(i);
 			if(go.collider.min.x < min.x)
 				min.x = go.collider.min.x;
 			if(go.collider.min.y < min.y)
 				min.y = go.collider.min.y;
 			if(go.collider.max.x > max.x)
 				max.x = go.collider.max.x;
 			if(go.collider.max.x > max.x)
 				max.x = go.collider.max.x;			
 		}
 		//set collider with new values
 		collider.min = min;
 		collider.max = max;
 		//go up in the tree
 		if(parent != null)
 			parent.updateCollider();
 	}
 	
 	//-----------------------------------------
 	//				RENDERING
 	//-----------------------------------------
 	
 	/**
 	 * draw the GameObject and all its children if visible
 	 */
 	public void draw(SpriteBatch batch){
 		//if the gameobject is not visible, children arent't either
 		if(!visible)
 			return;
 		if(getTexture() != null)
 			super.draw(batch);
 		//draw children
 		if(m_children != null){
 			for(int i=0; i< m_children.size();i++)
 				m_children.get(i).draw(batch);
 		}
 	}
 	
 	/**
 	 * Loads all the assets of the game objects in the AssetManager
 	 */
 	public void loadAssets(){
 		//load Children assets
 		loadChildrenAssets();
 		//do nothing if the texture name is null or blank
 		if(m_textureName == null || m_textureName == "")
 			return;
 		//check if the texture is loaded
 		if(! Global.assets.containsAsset(m_textureName)){
 			Global.assets.load(m_textureName, Texture.class); //if not load it
 		}
 	}
 	
 	/**
 	 * Loads the texture and creates the sprite with the texture size
 	 * @param Texture texture : the texture name (loaded internal)
 	 */
 
 	protected void loadSprite(){
 		//do nothing if the texture name is null or blank
 		if(m_textureName == null || m_textureName == "" || !Global.assets.isLoaded(m_textureName, Texture.class) )
 			return;
		
 		Texture texture;	
 		texture = Global.assets.get(m_textureName, Texture.class);
		
		//if texture size = 0
		if(m_textureHeight == 0 || m_textureWidth == 0){
			m_textureHeight = texture.getHeight();
			m_textureWidth = texture.getWidth();
		}
		
		TextureRegion region = new TextureRegion(texture, 0, 0, m_textureWidth, m_textureHeight);
		
		setRegion(region);
		
		this.setSize(m_textureWidth, m_textureHeight);
 	}
 		
 	/**
 	 * Binds the assets to the gameObject 
 	 * WARNING: all the assets have to be previously loaded in the AssetManager
 	 */
 	public void bindAssets(){
 		loadSprite();
 		//bind children assets
 		bindChildrenAssets();
 	}
 	
 	/**
 	 * Called To Load the gameObject assets
 	 */
 	private void loadChildrenAssets(){
 		if(m_children != null){
 			for(int i=0; i< m_children.size();i++)
 				m_children.get(i).loadAssets();
 		}
 	}
 	
 	/**
 	 * Called To Bind the children assets
 	 */
 	private void bindChildrenAssets(){
 		if(m_children != null){
 			for(int i=0; i< m_children.size();i++)
 				m_children.get(i).bindAssets();
 		}
 	}
 
 	//-----------------------------------------
 	//				TRANSFORM
 	//-----------------------------------------
 	/**
 	 * Replace the object to its old position
 	 */
 	public void replaceToOld(){
 		setPosition(m_oldPos.x, m_oldPos.y);
 	}
 	/**
 	 * Replace the object the its previous X position
 	 */
 	public void replaceToOldX(){
 		setPosition(m_oldPos.x, getY());
 	}
 	/**
 	 * Replace the object to its previous Y position
 	 */
 	public void replaceToOldY(){
 		setPosition(getX(), m_oldPos.y);
 	}
 	
 	/**
 	 * Compute the direction from the last position the current one
 	 * @return
 	 */
 	public Vector2 getDeltaDir(){
 		return new Vector2(getX()-m_oldPos.x,getY()-m_oldPos.y);
 	}
 	
 	public void die(){}
 	
 	public void dispose(){
 		if(getTexture() != null)
 			getTexture().dispose();
 	}
 
 
 	//-----------------------------------------
 	//		       COMPONENTS
 	//-----------------------------------------
 	/**
 	 * Add a component to the object
 	 * @param component the instance of component
 	 */
 	public void addComponent(Component component){
 		m_components.add(component);
 		component.setObject(this);
 		Component.TYPE type = component.getType();
 		//RIGIDBODY
 		if(type == Component.TYPE.RIGIDBODY){
 			rigidbody = (Rigidbody) component; 
 		//COLLIDER
 		}else if(type == Component.TYPE.COLLIDER){	
 			collider = (Collider) component;
 		}
 	}
 	
 	//-----------------------------------------
 	//		     CHILDREN
 	//-----------------------------------------
 	/**
 	 * Add a child to the GameObject children
 	 * @param go
 	 */
 	public void addChild(GameObject go){
 		if(m_children == null)
 			m_children = new ArrayList<GameObject>();
 		m_children.add(go);		
 		//Add this as a parent
 		go.parent = this;
 	}
 	
 	/**
 	 * Add a child with its position and scale
 	 * @param go
 	 * @param x position
 	 * @param y position
 	 * @param w width scale
 	 * @param h height scale
 	 */
 	public void addChild(GameObject go,float x, float y,float w, float h){
 		if(m_children == null)
 			m_children = new ArrayList<GameObject>();
 		go.setScale(w,h);
 		go.setPosition(x, y);
 		if(go.collider != null)
 			go.collider.scale(w, h);
 		m_children.add(go);		
 		//Add this as a parent
 		go.parent = this;	
 		//update collider
 		//updateCollider();
 	}
 		
 	//-----------------------------------------
 	//		          RESET
 	//-----------------------------------------
 	private Vector2 m_initialPosition;
 	public void reset(){
 		//Reset position
 		setPosition(m_initialPosition.x, m_initialPosition.y);
 		//Reset components
 		if(m_components != null){
 			for(int i=0; i < m_components.size();i++)
 				m_components.get(i).reset();
 		}
 	}
 	
 	//-----------------------------------------
 	//		     GETTERS - SETTERS
 	//-----------------------------------------
 	public Vector2 getPosition(){
 		return new Vector2(getX(),getY());
 	}
 		
 	public ArrayList<GameObject> getChildren(){
 		return this.m_children;
 	}
 	
 	public ArrayList<GameObject> getAllObjects(){
 		ArrayList<GameObject> GOs = new ArrayList<GameObject>();
 		if(m_children != null){
 		for(int i=0; i<m_children.size(); i++)
 			GOs.addAll(m_children.get(i).getAllObjects());
 		}
 		GOs.add(this);
 		return GOs;
 	}
 	//----------------------------------------------
 	//	CORE FUNCTIONS - NEVER OVERRIDE THAT
 	//----------------------------------------------
 	
 	public void coreAwake(){
 		if(m_children != null){
 			for(int i=0; i< m_children.size() ; i++)
 				m_children.get(i).coreAwake();
 		}
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				if(m_components.get(i).enabled)
 					m_components.get(i).awake();
 			}
 		}
 	}
 	
 	public void coreStart(){
 		if(m_children != null){
 			for(int i=0; i< m_children.size() ; i++)
 				m_children.get(i).coreStart();
 		}
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				if(m_components.get(i).enabled)
 					m_components.get(i).start();
 			}
 		}
 	}
 	
 	/**
 	 * Called before all the updates
 	 */
 	public void corePreUpdate(){
 		//keep old position
 		m_oldPos = getPosition();
 
 		if(m_children != null){
 			for(int i=0; i< m_children.size() ; i++)
 				m_children.get(i).corePreUpdate();
 		}
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				if(m_components.get(i).enabled)
 					m_components.get(i).preUpdate();
 			}
 		}
 	}
 	
 	/**
 	 * Called every frame
 	 */
 	public void coreUpdate(){
 		
 		//update children
 		if(m_children != null){
 			for(int i=0; i< m_children.size() ; i++)
 				m_children.get(i).coreUpdate();
 		}
 		//update components
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				if(m_components.get(i).enabled)
 					m_components.get(i).update();
 			}
 		}
 	}
 	
 	/**
 	 * Called after all the updates
 	 */
 	public void corePostUpdate(){
 		if(m_children != null){			
 			for(int i=0; i< m_children.size() ; i++)
 				m_children.get(i).corePostUpdate();
 		}
 		if(m_components != null){
 			for(int i=0;i< m_components.size(); i++){
 				if(m_components.get(i).enabled)
 					m_components.get(i).postUpdate();
 			}
 		}
 	}	
 }
