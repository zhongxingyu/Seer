 package com.soc.hud;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.List;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.utils.Array;
 import com.soc.core.SoC;
 import com.soc.utils.GraphicsLoader;
 
 public class InstructionBox extends Actor{
 	
 
 
 
 
 	
 	private static String instructions = 
 			"INSTRUCTIONS:\n\n\n"
 			+ "INGAME\n\n"
 			+ "SPACE - Attack\n"
 			+ "NUM KEYS - Skills\n"
 			+ "SHIFT LEFT - Shield\n"
 			+ "I - Inventory\n"
 			+ "ESC - Game menu\n"
 			+ "C - Character menu\n"
 			+ "Q - Use health potion\n"
 			+ "E - Use mana potion\n"
			+ "H - Pause and see this instructions again\n"
			+ "F5 - Quick save\n\n"
 			+ "Double tap a movement key to sprint\n\n"
 			+ "INVENTORY\n\n"
 			+ "TAB - Switch item focus			"
 			+ "E - Use focused item\n"
 			+ "B - Drop item\n\n"
 			+ "Press H to continue";
 	
 	
 	private Skin skin;
 	private BitmapFont font;
 	private Texture container;
 	public HudSystem parent;
 	private boolean h;
 	
 	public InstructionBox(HudSystem parent){
 
 		this.container = new Texture(Gdx.files.internal("resources/character-menu.png"));
 		this.skin = new Skin(  Gdx.files.internal( "resources/skin2.json" ) );
 		this.font =skin.getFont("buttonFont");
 		this.parent = parent;
 		h = true;
 	}
 	
 	@Override
 	public void act(float delta){
 		if(!h && Gdx.input.isKeyPressed(Input.Keys.H)){
 			Array<Actor> actors = parent.stage.getActors();
 			for(int i = 0; i < parent.stage.getActors().size; i++){
 				if(actors.get(i) == this){
 					actors.removeIndex(i);
 				}
 			}
 			h = true;
 			SoC.game.pause = false;
 		}
 		if(!Gdx.input.isKeyPressed(Input.Keys.H)){
 			h = false;
 		}
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha){
 		batch.setColor(1,1,1,0.7f);
 		batch.draw(container, getX(), getY(), getWidth(), getHeight());
 		batch.setColor(1,1,1,1f);
 		font.setColor(0,0,0,1);
 		font.setScale(0.6f);
 		font.drawWrapped(batch, instructions, getX()+40, getY()+getHeight()-40, getWidth()-20);
 	}
 }
