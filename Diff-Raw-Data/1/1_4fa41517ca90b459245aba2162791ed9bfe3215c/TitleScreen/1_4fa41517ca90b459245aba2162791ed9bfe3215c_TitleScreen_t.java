 package es.wiyarmir.minigdxcraft.screen;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.GL20;
 import com.mojang.ld22.gfx.Color;
 
 import es.wiyarmir.minigdxcraft.Globals;
 import es.wiyarmir.minigdxcraft.PortedGame;
 import es.wiyarmir.minigdxcraft.gfx.PortFont;
 import es.wiyarmir.minigdxcraft.gfx.PortScreen;
 
 public class TitleScreen implements Screen {
 
 	private static final String[] options = { "Start game", "How to play",
 			"About" };
 
 	int selected = 0;
 	float accumulator = 0;
 
 	private PortedGame game;
 	private FPSLogger fpsl;
 
 	PortScreen supportscreen;
 
 	public TitleScreen(PortedGame g) {
 		Gdx.app.log(Globals.TAG, "new TitleScreen()");
 		game = g;
 		fpsl = new FPSLogger();
 
 		supportscreen = new PortScreen(Globals.WIDTH, Globals.HEIGHT,
 				game.spriteSheet, game);
 	}
 
 	@Override
 	public void render(float delta) {
 		accumulator += delta;
 		if (accumulator > Globals.TICK_TIME) {
 			accumulator = 0;
 			tick();
 		}
 
 		// Gdx.app.log(GlobalSettings.TAG, "TitleScreen render()");
 		fpsl.log();
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		game.batch.begin();
 
 		game.batch.draw(game.spriteSheet, (Globals.WIDTH - (17 * 8))
 				* Globals.SCALE, (Globals.HEIGHT - 32) * Globals.SCALE,
 				104 * Globals.SCALE, 16 * Globals.SCALE, 0, 48, 104, 16, false,
 				false);
 		for (int i = 0; i < 3; i++) {
 			String msg = options[i];
 			// int col = Color.get(0, 222, 222, 222);
 			if (i == selected) {
 				msg = "> " + msg + " <";
 				// col = Color.get(0, 555, 555, 555);
 			}
 			PortFont.draw(msg, supportscreen,
 					(supportscreen.w - msg.length() * 8) / 2, (8 + i) * 8, 0);
 		}
 
 		PortFont.draw("(Arrow keys,X and C)", supportscreen, 0,
 				supportscreen.h - 8, Color.get(0, 111, 111, 111));
 
 		game.batch.end();
 
 	}
 
 	public void tick() {
 
 		game.input.tick();
 
 		if (game.input.up.down)
 			selected--;
 		if (game.input.down.down)
 			selected++;
 
 		int len = options.length;
 		if (selected < 0)
 			selected += len;
 		if (selected >= len)
 			selected -= len;
 
 		if (game.input.attack.down || game.input.menu.down) {
 			if (selected == 0) {
 				// FIXME
 				// Sound.test.play();
 				game.setScreen(new GameScreen(game));
 			}
 			if (selected == 1) {
 				// game.setMenu(new InstructionsMenu(this));
 				throw new NotImplementedException();
 			}
 			if (selected == 2) {
 				// game.setMenu(new AboutMenu(this));
 				throw new NotImplementedException();
 			}
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		Gdx.app.log(Globals.TAG, "TitleScreen resize()");
 
 	}
 
 	@Override
 	public void show() {
 		Gdx.app.log(Globals.TAG, "TitleScreen show()");
 
 	}
 
 	@Override
 	public void hide() {
 		Gdx.app.log(Globals.TAG, "TitleScreen hide()");
 
 	}
 
 	@Override
 	public void pause() {
 		Gdx.app.log(Globals.TAG, "TitleScreen pause()");
 	}
 
 	@Override
 	public void resume() {
 		Gdx.app.log(Globals.TAG, "TitleScreen resume()");
 
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.app.log(Globals.TAG, "TitleScreen dispose()");
 	}
 
 }
