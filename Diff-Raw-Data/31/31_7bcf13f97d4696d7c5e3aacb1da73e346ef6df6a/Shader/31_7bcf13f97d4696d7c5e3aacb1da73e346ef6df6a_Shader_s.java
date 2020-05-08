 package com.matrunks.shooter.models;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.math.Vector3;
 
 public class Shader extends GameObject{
 	
	static TextureAtlas shaders = new TextureAtlas(Gdx.files.internal("data/shader.atlas"));
 
 	public Shader (int type, int x, int y){
 		if(type==0){ //nos estamos refiriendo a disparos
 			image = shaders.findRegion("Shot");
 			width=50;
 			height=50;
 		}
 		
 		position= new Vector3(x-(width/2) , y-(height/2) ,0); //le resto ancho y alto para que salga centrado
 	}
 	
 	@Override
 	public AtlasRegion image() {
 		// TODO Auto-generated method stub
 		return image;
 	}
 
 	@Override
 	public int width() {
 		// TODO Auto-generated method stub
 		return width;
 	}
 
 	@Override
 	public int height() {
 		// TODO Auto-generated method stub
 		return height;
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		shaders.dispose();
 	}
 
 }
