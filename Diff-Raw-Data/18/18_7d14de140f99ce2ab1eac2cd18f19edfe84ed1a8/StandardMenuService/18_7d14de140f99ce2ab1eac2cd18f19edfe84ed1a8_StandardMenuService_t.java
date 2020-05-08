 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madthrax.ridiculousRPG.ui;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.actions.Delay;
 import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
 import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
 import com.badlogic.gdx.scenes.scene2d.actions.Remove;
 import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.madthrax.ridiculousRPG.GameBase;
 import com.madthrax.ridiculousRPG.TextureRegionLoader;
 import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;
 import com.madthrax.ridiculousRPG.animation.BoundedImage;
 import com.madthrax.ridiculousRPG.animation.ImageProjectionService;
 import com.madthrax.ridiculousRPG.service.ResizeListener;
 
 /**
  * This class provides a standard menu for the game.<br>
  * 
  * @author Alexander Baumgartner
  */
public class StandardMenuService extends ActorsOnStageService implements
		ResizeListener {
 	protected enum ServiceState {
 		PAUSED, START_MENU, GAME_MENU, IDLE
 	};
 
 	private TextureRegionRef background;
 	private ServiceState serviceState = ServiceState.IDLE;
 
 	public StandardMenuService() {
 		if (GameBase.$().getOptions().titleBackground != null) {
 			setBackground(GameBase.$().getOptions().titleBackground);
 		}
 		changeState(ServiceState.START_MENU);
 	}
 
 	public static void toggleCursor() {
 		Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
 	}
 
 	public void setBackground(String path) {
 		if (background != null)
 			background.dispose();
 		background = TextureRegionLoader.load(path);
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return processInput(keycode) || super.keyUp(keycode);
 	}
 
 	private boolean processInput(int keycode) {
 		switch (serviceState) {
 		case PAUSED:
 			return processPausedState(keycode);
 		case IDLE:
 			return processIdleState(keycode);
 		case GAME_MENU:
 			return processGameMenuState(keycode);
 		case START_MENU:
 			return processStartMenuState(keycode);
 		}
 		return false;
 	}
 
 	private boolean processStartMenuState(int keycode) {
 		if (keycode == Input.Keys.ESCAPE) {
 			GameBase.exit();
 			return true;
 		}
 		return false;
 	}
 
 	private boolean processGameMenuState(int keycode) {
 		if (keycode == Input.Keys.ESCAPE) {
 			return changeState(ServiceState.IDLE);
 		}
 		return false;
 	}
 
 	private boolean processIdleState(int keycode) {
 		if (keycode == Input.Keys.P) {
 			return changeState(ServiceState.PAUSED);
 		}
 		if (keycode == Input.Keys.ESCAPE) {
 			return changeState(ServiceState.GAME_MENU);
 		}
 		return false;
 	}
 
 	private boolean processPausedState(int keycode) {
 		if (keycode == Input.Keys.P) {
 			return changeState(ServiceState.IDLE);
 		}
 		return false;
 	}
 
 	/**
 	 * Refresh the menu
 	 */
 	public void refreshMenu() {
 		changeState(serviceState);
 	}
 
 	public boolean changeState(ServiceState newState) {
 		boolean consumed = false;
 		if (serviceState != ServiceState.IDLE) {
 			consumed = GameBase.$serviceProvider().releaseAttention(this);
 		}
 		if (newState != ServiceState.IDLE) {
 			consumed = GameBase.$serviceProvider().requestAttention(this, true,
 					newState == ServiceState.START_MENU)
 					| consumed;
 		}
 
 		clear();
 		switch (newState) {
 		case PAUSED:
 			createPauseMenu();
 			break;
 		case GAME_MENU:
 			createGameMenu();
 			break;
 		case START_MENU:
 			createStartMenu();
 			break;
 		}
 
 		serviceState = newState;
 		return consumed;
 	}
 
 	private void createGameMenu() {
 		final Skin skin = getSkinNormal();
 		Window w = new Window("Game menu", skin);
 		addActor(w);
 
 		TextButton resume = new TextButton("Resume (Esc)", skin);
 		resume.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				changeState(ServiceState.IDLE);
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(resume);
 
 		TextButton bag = new TextButton("Open bag", skin);
 		bag.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				showInfo(getSkinFocused(), "Bag is not implemented yet.\n"
 						+ "This is an early alpha release!");
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(bag);
 
 		TextButton save = new TextButton("Save", skin);
 		save.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				showInfo(getSkinFocused(), "Save is not implemented yet.\n"
 						+ "This is an early alpha release!");
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(save);
 
 		TextButton load = new TextButton("Load", skin);
 		load.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				showInfo(getSkinFocused(), "Load is not implemented yet.\n"
 						+ "This is an early alpha release!");
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(load);
 
 		TextButton toTitle = new TextButton("Return to title", skin);
 		toTitle.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				changeState(ServiceState.START_MENU);
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(toTitle);
 
 		TextButton exit = new TextButton("Exit game", skin);
 		exit.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				GameBase.exit();
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(exit);
 
 		w.pack();
 		w.height = height();
 		addActor(w);
 		focus(resume);
 	}
 
 	private void createStartMenu() {
 		if (background != null) {
			ImageProjectionService.$background.getImages().add(
					new BoundedImage(background));
 		}
 		final Skin skin = getSkinNormal();
 		Window w = new Window("Start menu", skin);
 		addActor(w);
 
 		TextButton resume = new TextButton("Continue at last save point", skin);
 		resume.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				changeState(ServiceState.IDLE);
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(resume);
 
 		TextButton load = new TextButton("Load game", skin);
 		load.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				showInfo(getSkinFocused(), "Load is not implemented yet.\n"
 						+ "This is an early alpha release!");
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(load);
 
 		TextButton start = new TextButton("Start new game", skin);
 		start.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				changeState(ServiceState.IDLE);
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(start);
 
 		TextButton toggleFull = new TextButton(
 				GameBase.$().isFullscreen() ? "Window mode" : "Fullscreen mode",
 				skin);
 		toggleFull.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				GameBase.$().toggleFullscreen();
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(toggleFull);
 
 		TextButton exit = new TextButton("Exit game (Esc)", skin);
 		exit.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				GameBase.exit();
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(exit);
 
 		w.pack();
 		center(w);
 		focus(resume);
 	}
 
 	private void center(Actor actor) {
		actor.x = (int) (centerX() - actor.width * .5f);
		actor.y = (int) (centerY() - actor.height * .5f);
 	}
 
 	protected void createPauseMenu() {
 		Skin skin = getSkinNormal();
 		Window w = new Window("PAUSE", skin);
 		addActor(w);
 
 		TextButton resume = new TextButton("Resume (P)", skin);
 		resume.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				changeState(ServiceState.IDLE);
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(resume);
 
 		TextButton exit = new TextButton("Return to title", skin);
 		exit.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				changeState(ServiceState.START_MENU);
 			}
 		});
 		w.row().fill(true, true).expand(true, false);
 		w.add(exit);
 
 		w.pack();
 		center(w);
 		focus(resume);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		refreshMenu();
 	}
 
 	@Override
 	public void dispose() {
 		if (background != null)
 			background.dispose();
 	}
 
 	public void showInfoNormal(String info) {
 		showInfo(getSkinNormal(), info);
 	}

 	public void showInfoFocused(String info) {
 		showInfo(getSkinFocused(), info);
 	}

 	private void showInfo(final Skin skin, String info) {
 		final Window w = new Window(skin);
 		addActor(w);
 
 		w.touchable = false;
 		w.color.a = .1f;
 		w.action(Sequence.$(FadeIn.$(.3f), Delay.$(FadeOut.$(.3f), 2f), Remove
 				.$()));
 		w.add(new Label(info, skin));
 
 		w.pack();
 		center(w);
 	}
 }
