 package com.game.rania.screen;
 
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.MainController;
 import com.game.rania.controller.ShipController;
 import com.game.rania.model.EnemyUser;
 import com.game.rania.model.Location;
 import com.game.rania.model.LocationSprite;
 import com.game.rania.model.Planet;
 import com.game.rania.model.PlanetSprite;
 import com.game.rania.model.SpaceShip;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.userdata.User;
 import com.game.rania.view.MainView;
 
 public class LocationScreen implements Screen{
 	
 	private MainView view = null;
 	private MainController controller = null;
 	private User user = null;
 	
 	public LocationScreen(){
 		view = RaniaGame.mView;
 		controller = RaniaGame.mController;
 		user = RaniaGame.mUser;
 	}
 
 	@Override
 	public void show() {
 		Location location = RaniaGame.mLocations.get(String.valueOf(user.idLocation));
 		if (location == null)
 			return;
 
 		view.loadTexture("data/sprites/star.png", RegionID.STAR);
 		for (int i = 0; i < 18; i++)
 			view.loadTexture("data/sprites/planets.png", RegionID.fromInt(RegionID.PLANET_0.ordinal() + i), i % 5 * 102, i / 5 * 102, 102, 102);
 		
 		view.loadTexture("data/sprites/SpaceShip.png", RegionID.SHIP);
 		view.loadTexture("data/backgrounds/space.jpg", RegionID.BACKGROUND_SPACE);
 		view.getTexture(RegionID.BACKGROUND_SPACE).setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
 		view.loadTexture("data/backgrounds/stars.png", RegionID.BACKGROUND_STARS);
 		view.getTexture(RegionID.BACKGROUND_STARS).setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
 
 		controller.addObject(new LocationSprite(location));
		RaniaGame.nController.isWorkReciver=false;
 		HashMap<String, Planet> planets = RaniaGame.nController.GetCurrentPlanets(user);
 		for (Planet planet : planets.values()) {
 			controller.addStaticObject(new PlanetSprite(planet));
 		}
 		
 		HashMap<String, EnemyUser> users = RaniaGame.nController.GetUsersInLocation(user);
		RaniaGame.nController.isWorkReciver=true;
 		for (EnemyUser enemyUser : users.values()) {
 			controller.addDynamicObject(new SpaceShip(enemyUser.x, enemyUser.y));
 		}
 		SpaceShip ship = new SpaceShip(user.x, user.y);
 		controller.setPlayer(ship);
 		controller.addProcessor(new ShipController(ship));
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void dispose() {
 		controller.clear();
 		view.clear();
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void render(float deltaTime) {
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		controller.update(deltaTime);
 		view.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
