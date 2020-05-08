 package fr.romainpedra.VolcanoEscape;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 
 public class Assets {
 	
 	public Texture perso;
 	public Texture paroie;
 	public Texture rocher;
 	public Texture lave;
 	public Texture background;
 	
 	
 	public void load(){
 		perso = new Texture(Gdx.files.internal("res/drawable-hdpi/ic_launcher.png"));
 		perso.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 	}
 	
 	public void dispose(){
 		
 		if(perso != null){
 			perso.dispose();
 			perso = null;
 		}
 		if(paroie != null){
 			paroie.dispose();
 			paroie = null;
 		}
 		if(rocher != null){
 			rocher.dispose();
 			rocher = null;
 		}
 		if(lave != null){
 			lave.dispose();
 			lave = null;
 		}
 		if(background != null){
 			background.dispose();
 			background = null;
 		}
 	}
 
 }
