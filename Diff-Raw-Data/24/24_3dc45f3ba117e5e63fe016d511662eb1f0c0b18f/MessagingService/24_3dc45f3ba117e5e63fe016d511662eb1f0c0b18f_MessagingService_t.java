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
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.actions.Delay;
 import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
 import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
 import com.badlogic.gdx.scenes.scene2d.actions.Remove;
 import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
 import com.badlogic.gdx.utils.Array;
 import com.madthrax.ridiculousRPG.GameBase;
 import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;
 
 /**
  * This class provides a customizable standard menu for the game.<br>
  * 
  * @author Alexander Baumgartner
  */
 public class MessagingService extends ActorsOnStageService {
 
 	private Rectangle boxPosition;
 	private TextureRegionRef face;
 	private String title;
 	private float displayInfoTime = 2f;
 	private Array<Message> lines = new Array<Message>();
 	private boolean allowNull;
 	private boolean dispose;
 	Object[] resultPointer = new Object[] { null };
 
 	public MessagingService() {
 		boxPosition = new Rectangle(0, 0, 0, 200);
 		setAllowNull(true);
 		setFadeTime(.15f);
 	}
 
 	public void setAllowNull(boolean allowNull) {
 		this.allowNull = allowNull;
 		setCloseOnAction(allowNull);
 	}
 
 	public void addGUIcomponent(Object component) {
 		if (component instanceof Actor)
 			addActor((Actor) component);
 	}
 
 	public void center(Object obj) {
 		if (obj instanceof Actor) {
 			Actor actor = (Actor) obj;
 			actor.x = (int) (centerX() - actor.width * .5f);
 			actor.y = (int) (centerY() - actor.height * .5f);
 		}
 	}
 
 	public void focus(Object guiElement) {
 		if (guiElement instanceof Actor) {
 			super.focus((Actor) guiElement);
 		}
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.ESCAPE) {
 			if (allowNull)
 				fadeOutAllActors();
 			return true;
 		}
 		return super.keyDown(keycode);
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public float getWidth() {
 		return width;
 	}
 
 	/*
 	 * box(x,y,width,height) - set preferred position, width and height for this
 	 * conversations message box. (Default = 0,0,screen.width,250)
 	 * face("filename",x,y,width,height) - set face for conversation.
 	 * (automatically performs a commit if some text is outstanding)
 	 * say("Line of text") - simply some text choice("item 1", 1) - one choice
 	 * with the integer to return on click
 	 * input("default value",maximum,numberInput) - text or number input. If
 	 * numberInput is true, only numbers are allowed. Maximum specifies the
 	 * maximum text length or the maximum value for number input. commit() -
 	 * prints the message box and waits for the result. returns the result (or
 	 * NULL if no result)
 	 */
 
 	// info("Text", "title") - an info box with title, which will disappear
 	// automatically
 
 	public void showInfoNormal(String info) {
 		showInfo(getSkinNormal(), info);
 	}
 
 	public void showInfoFocused(String info) {
 		showInfo(getSkinFocused(), info);
 	}
 
 	private void showInfo(final Skin skin, String info) {
 		try {
 			final Window w = new Window(skin);
 
 			w.touchable = false;
 			w.color.a = .1f;
 			w.action(Sequence.$(FadeIn.$(getFadeTime()), Delay.$(FadeOut
 					.$(getFadeTime()), displayInfoTime), Remove.$()));
 			w.add(info);
 
 			w.pack();
 			center(w);
 			addActor(w);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void info(String info) {
 		showInfoNormal(info);
 	}
 
 	public void box(float x, float y, float width, float height) {
 		boxPosition.x = x;
 		boxPosition.y = y;
 		boxPosition.width = width;
 		boxPosition.height = height;
 	}
 
 	public Object face() {
 		return commit();
 	}
 
 	public void title(String title) {
 		this.title = title;
 	}
 
 	public void say(String text) {
 		lines.add(new MessageText(text));
 	}
 
 	public void choice(String text, int value) {
 		setAllowNull(false);
 		lines.add(new MessageChoice(text, value));
 	}
 
 	public void input(String text, int maximum, boolean numeric,
 			boolean password) {
 		setAllowNull(false);
 		lines.add(new MessageInput(text, maximum, numeric, password));
 	}
 
 	public Object commit() {
 		if (dispose)
 			return null;
 		if (lines.size > 0
 				&& GameBase.$serviceProvider().requestAttention(this, false,
 						false)) {
 			// an other box is showing up
 			while (getActors().size() > 0) {
				if (dispose) {
					GameBase.$serviceProvider().releaseAttention(this);
 					return null;
				}
 				Thread.yield();
 			}
 
 			resultPointer[0] = null;
 			drawWindow();
 
 			lines.clear();
 
 			while (resultPointer[0] == null && getActors().size() > 0
 					&& !dispose)
 				Thread.yield();
 			if (!GameBase.$serviceProvider().releaseAttention(this)) {
 				throw new RuntimeException(
 						"Something got terribly wrong! This should never happen!");
 			}
 			fadeOutAllActors();
 			setAllowNull(true);
 			// sleep until box has disappeared
 			do {
 				Thread.yield();
 			} while (getActors().size() > 0 && !dispose);
 		}
 		return resultPointer[0];
 	}
 
 	private void drawWindow() {
 		try {
 			final Window w = new Window(getSkinNormal());
 
 			if (title != null) {
 				w.setTitle(title);
 			}
 			w.color.a = .1f;
			w.align(Align.TOP);
 			w.action(Sequence.$(FadeIn.$(getFadeTime())));
 			for (Message line : lines) {
 				w.row().fill(true, false).expand(true, false);
 				w.add(line.getActor());
 			}
 			w.x = boxPosition.x;
 			w.y = boxPosition.y;
			w.pack();
 			if (boxPosition.width == 0) {
 				// width defined by margin x
 				w.width = GameBase.$().getScreen().width - 2 * w.x;
 			} else if (boxPosition.width < 0) {
 				// bind box at the top edge of the screen
 				if (w.width < -boxPosition.width) {
 					// set preferred width
 					w.width = -boxPosition.width;
 				}
 				w.x = GameBase.$().getScreen().width - w.width - w.x;
 			} else if (w.width < boxPosition.width) {
 				// set preferred width
 				w.width = boxPosition.width;
 			}
 			if (boxPosition.height == 0) {
 				// height defined by margin y
 				w.height = GameBase.$().getScreen().height - 2 * w.y;
 			} else if (boxPosition.height < 0) {
 				// bind box at the right edge of the screen
 				if (w.height < -boxPosition.height) {
 					// set preferred height
 					w.height = -boxPosition.height;
 				}
 				w.y = GameBase.$().getScreen().height - w.height - w.y;
 			} else if (w.height < boxPosition.height) {
 				// set preferred height
 				w.height = boxPosition.height;
 			}
 			if (boxPosition.x < 0) {
 				// center horizontal
 				w.x = (int) (centerX() - w.width * .5f);
 			}
 			if (boxPosition.y < 0) {
 				// center vertical
 				w.y = (int) (centerY() - w.height * .5f);
 			}
 			addActor(w);
 			focus(w);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setDisplayInfoTime(float displayInfoTime) {
 		this.displayInfoTime = displayInfoTime;
 	}
 
 	public float getDisplayInfoTime() {
 		return displayInfoTime;
 	}
 
 	public interface Message {
 		public Actor getActor();
 	}
 
 	public class MessageInput implements Message {
 		private String text;
 		private int maximum;
 		private boolean numeric;
 		private boolean password;
 
 		public MessageInput(String text, int maximum, boolean numeric,
 				boolean password) {
 			if (numeric && text != null) {
 				text = text.replaceAll("\\D+", "");
 			}
 			if (maximum < 0) {
 				maximum = numeric ? 5 : 30;
 			}
 			this.text = text;
 			this.maximum = maximum;
 			this.numeric = numeric;
 			this.password = password;
 		}
 
 		@Override
 		public Actor getActor() {
 			TextField tf = new TextField(text, getSkinNormal());
 			if (password)
 				tf.setPasswordMode(true);
 			if (numeric) {
 				tf.setTextFieldFilter(new TextFieldFilter() {
 					@Override
 					public boolean acceptChar(TextField textField, char key) {
 						int len = 0;
 						String text = textField.getText();
 						if (text != null)
 							len = text.length();
 						return len < maximum && Character.isDigit(key);
 					}
 				});
 			} else {
 				tf.setTextFieldFilter(new TextFieldFilter() {
 					@Override
 					public boolean acceptChar(TextField textField, char key) {
 						int len = 0;
 						String text = textField.getText();
 						if (text != null)
 							len = text.length();
 						return len < maximum;
 					}
 				});
 			}
 			tf.setTextFieldListener(new TextFieldListener() {
 				@Override
 				public void keyTyped(TextField textField, char key) {
 					String text = textField.getText();
 					if (text != null && text.trim().length() > 0
 							&& (key == '\r' || key == '\n' || key == '\t')) {
 						if (numeric) {
 							resultPointer[0] = new Integer(text.trim());
 						} else {
 							resultPointer[0] = text.trim();
 						}
 					}
 				}
 			});
 			return tf;
 		}
 	}
 
 	public class MessageChoice implements Message {
 		private String text;
 		private int value;
 
 		public MessageChoice(String text, int value) {
 			this.text = text;
 			this.value = value;
 		}
 
 		@Override
 		public Actor getActor() {
 			TextButton tb = new TextButton(text, getSkinNormal());
 			tb.setClickListener(new ClickListener() {
 				@Override
 				public void click(Actor actor, float x, float y) {
 					resultPointer[0] = value;
 				}
 			});
 			return tb;
 		}
 
 	}
 
 	public class MessageText implements Message {
 		private String text;
 
 		public MessageText(String text) {
 			this.text = text;
 		}
 
 		@Override
 		public Actor getActor() {
			Label l = new Label(text, getSkinNormal());
			l.setWrap(true);
			return l;
 		}
 	}
 
 	@Override
 	public void dispose() {
 		dispose = true;
 		super.dispose();
 		if (face != null)
 			face.dispose();
 	}
 }
