 package com.game.rania.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.game.rania.Config;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.MainController;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.model.element.StaticObject;
 import com.game.rania.net.NetController;
 import com.game.rania.view.MainView;
 
 public class MainMenu implements Screen{
 	
 	private Skin skin;
 	private Stage stage;
 
 	private MainView view = null;
 	private MainController controller = null;
 	private RaniaGame game = null;
 	
 	public MainMenu(){
 		view = RaniaGame.mView;
 		controller = RaniaGame.mController;
 		game = RaniaGame.mGame;
 	}
 	
 	@Override
 	public void show() {
 		view.loadTexture("data/backgrounds/menu.jpg", RegionID.BACKGROUND_MENU, 0, 0, 768, 512);
 		controller.addStaticObject(new StaticObject(RegionID.BACKGROUND_MENU, 0.0f, 0.0f));
 
 		float width = Gdx.graphics.getWidth();
 		float height = Gdx.graphics.getHeight();
 
 
 		skin = new Skin(Gdx.files.internal("data/gui/uiskin.json"));
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, view.getSpriteBatch());
 		
 		//login
 		final TextField loginTextField = new TextField("", skin);
 		loginTextField.setMessageText("Enter email");
 		loginTextField.setTextFieldListener(new TextFieldListener() {
 			public void keyTyped (TextField textField, char key) {
 				if (key == '\n')
 					textField.getOnscreenKeyboard().show(false);
 			}
 		});
 		
 		//password
 		final TextField passwordTextField = new TextField("", skin);
 		passwordTextField.setMessageText("Enter Password");
 		passwordTextField.setPasswordCharacter('*');
 		passwordTextField.setPasswordMode(true);
 		passwordTextField.setTextFieldListener(new TextFieldListener() {
 			public void keyTyped (TextField textField, char key) {
 				if (key == '\n')
 					textField.getOnscreenKeyboard().show(false);
 			}
 		});
 		
 		Button loginButton = new Button(skin);
 		loginButton.add("Login");
 		loginButton.addListener(new ClickListener() {
 	        @Override
 	        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
 				NetController netController = new NetController();
 				//
 				loginTextField.setText(Config.autoLogin);
 				passwordTextField.setText(Config.autoPassword);
 				if ((loginTextField.getText() != "") && (passwordTextField.getText() != ""))
 				{
 					RaniaGame.mUser = netController.ClientLogin(loginTextField.getText(), passwordTextField.getText());
 					if (RaniaGame.mUser.isLogin)
 					{
 						dispose();
 						netController.GetUserData(RaniaGame.mUser);
 						RaniaGame.mLocations = RaniaGame.nController.GetAllLocations(RaniaGame.mUser);
 						RaniaGame.mUser.isWorkReciver = true;
 						game.setScreen(new SpaceScreen());
 					}
 					else
 					{
 						//RaniaGame.mHelperUI.showToastLong("Invalid login or password...");
 					}
 				}
 	        }
 	    });
 		
 		Button exitButton = new Button(skin);
 		exitButton.add("Exit");
 		exitButton.addListener(new ClickListener() {
 	        @Override
 	        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
 	            dispose();
 	    		game.dispose();
 	        }
 	    });
 		
 		Table container = new Table();
 		container.setSize(width, height);
 		container.setPosition(0.0f, 0.0f);
 	    container.row().fill(true).width(width * 0.25f).height(height * 0.1f).pad(5, width * 0.5f, 5, 0);
 	    container.add(loginTextField);
 	    container.row().fill(true).width(width * 0.25f).height(height * 0.1f).pad(5, width * 0.5f, 5, 0);
 	    container.add(passwordTextField);
 	    container.row().fill(true).width(width * 0.25f).height(height * 0.1f).pad(5, width * 0.5f, height * 0.1f, 0);
 	    container.add(loginButton);
 	    container.row().fill(true).width(width * 0.25f).height(height * 0.1f).pad(5, width * 0.5f, height * 0.4f, 0);
 	    container.add(exitButton);
 		stage.addActor(container);
 		controller.addProcessor(stage);
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.input.setOnscreenKeyboardVisible(false);
 		stage.dispose();
 		skin.dispose();
 		controller.clear();
 		view.clear();
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void pause() {		
 	}
 	
 	@Override
 	public void render(float deltaTime) {		
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		view.draw();
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
