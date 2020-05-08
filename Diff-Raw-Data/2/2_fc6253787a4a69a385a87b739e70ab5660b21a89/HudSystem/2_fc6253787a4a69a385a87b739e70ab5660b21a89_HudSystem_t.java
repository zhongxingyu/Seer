 package com.soc.hud;
 
 import com.artemis.systems.VoidEntitySystem;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.soc.core.SoC;
 
 public class HudSystem extends VoidEntitySystem{
 
 	public OrthographicCamera camera;
 	public Stage stage;
 	public StatusBar statusBar;
 	public ActionBar actionBar;
 	public Inventory inventory;
 	public CharacterMenu characterMenu;
 	public TextButton textButton;
 	public TooltipBox tooltip;
 	public GameMenu gameMenu;
 	public InstructionBox instructions;
 	public Skin skin;
 	
 	
 	public HudSystem(OrthographicCamera camera){
 		
 		this.camera = camera;
 		this.stage = new Stage();
 		this.skin = new Skin( Gdx.files.internal("resources/skin2.json"));
 		this.statusBar = new StatusBar(this);
 		this.actionBar = new ActionBar(this);
 		this.inventory=new Inventory(this);
 		this.characterMenu = new CharacterMenu(this);
 		this.gameMenu=new GameMenu(this);
 		this.tooltip = new TooltipBox(this);
 		this.instructions = new InstructionBox(this);
 	}
 	
 	@Override
 	protected void initialize(){
 		SoC.game.inputMultiplexer.addProcessor(actionBar);
 	}
 
 	
 	@Override
 	protected void processSystem() {
 		stage.act(world.delta);
 		stage.draw();
 	}
 	
 	public void setViewport(int width, int height){
 		stage.setViewport(width, height, true);
 		statusBar.setPosition(10, height - 70);
 		inventory.setPosition(10, 150);
 		characterMenu.setPosition(215, height-205);
 		gameMenu.setPosition(width/2,height/2+50 );
 		gameMenu.setViewport(height);
 		tooltip.setBounds(width-350, 20, 200, 200);
 		instructions.setBounds(width/2 - width/4, 100, width/2, height-200);
 	}
 	
 	public void toggleInventory(){
 		if(!inventory.hasParent()){
 			stage.addActor(inventory);
 			SoC.game.inputMultiplexer.addProcessor(inventory);
 		}else{
 			tooltip.setText(null, 0);
 			inventory.remove();
 			SoC.game.inputMultiplexer.removeProcessor(inventory);
 		}
 	}
 	
 	public void toogleCharacterMenu(){
 		if(!characterMenu.hasParent()){
 			stage.addActor(characterMenu);
 		} else {
 			characterMenu.remove();
 		}
 	}
 	
 	public void popInstructions(){
 		stage.addActor(instructions);
 		SoC.game.pause = true;
 		if(characterMenu.hasParent()){
 			characterMenu.remove();
 		}
		if(inventory.hasParent()){
 			tooltip.setText(null, 0);
 			inventory.remove();
 			SoC.game.inputMultiplexer.removeProcessor(inventory);
 		}
 		
 	}
 	
 	public void toogleGameMenu(){
 		if(!gameMenu.hasParent()){
 			stage.addActor(gameMenu);
 			SoC.game.inputMultiplexer.addProcessor(gameMenu);
 		}else{
 			gameMenu.remove();
 			SoC.game.inputMultiplexer.removeProcessor(gameMenu);
 		}
 	}
 	
 	public void hideCharacterGameMenu(){
 		if(gameMenu.hasParent()){
 			gameMenu.remove();
 			SoC.game.inputMultiplexer.removeProcessor(gameMenu);
 		}
 		if(inventory.hasParent()){
 			inventory.remove();
 			SoC.game.inputMultiplexer.removeProcessor(inventory);
 		}
 	}
 }
