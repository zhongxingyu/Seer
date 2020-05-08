 package com.soc.hud;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Buttons;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.soc.core.Constants;
 import com.soc.core.SoC;
 import com.soc.game.objects.Armor;
 import com.soc.game.objects.Item;
 import com.soc.game.objects.Potion;
 import com.soc.game.objects.Weapon;
 import com.soc.utils.EffectsPlayer;
 
 public class Inventory extends Actor implements InputProcessor {
 	public Texture slot;
 	public Texture focusSlot;
 	public Texture armorSlot;
 	public Texture weaponSlot;
 	public int width;
 	public int height;
 	public int focusedSlot;
 	public HudSystem parent;
 
 	public Inventory(HudSystem parent) {
 		slot = new Texture(Gdx.files.internal("resources/slot.png"));
 		armorSlot = new Texture(Gdx.files.internal("resources/slot-armor.png"));
 		weaponSlot = new Texture(
 				Gdx.files.internal("resources/slot-weapon.png"));
 		focusSlot = new Texture(Gdx.files.internal("resources/slot-weapon.png"));
 		this.width = 1280;
 		this.height = 900;
 		focusedSlot = 1;
 		this.parent = parent;
 	}
 
 	public void draw(SpriteBatch batch, float partenAlpha) {
 
 		float posX = getX() + 256;
 		float posY = getY();
 		float posFocusX = 0;
 		float posFocusY = 0;
 		boolean existsFocus = false;
 		Item itemFocused = null;
 		for (int i = Constants.Items.INVENTORY_SIZE; i > 0; i--) {
 			if (i == focusedSlot) {
 				posFocusX = posX;
 				posFocusY = posY;
 				existsFocus = true;
 			} else {
 				batch.draw(slot, posX, posY, 64, 64);
 			}
 			Item item = SoC.game.playermapper.get(SoC.game.player).inventary[i - 1];
 			if (item != null) {
 				if (i == focusedSlot) {
 					itemFocused = item;
 				} else
 					batch.draw(item.icon, posX + 5, posY + 15, 55, 45);
 			}
 			posX -= 64;
 			if ((i - 1) % 5 == 0) {
 				posY += 64;
 				posX = getX() + 256;
 			}
 		}
 		if (existsFocus) {
 			batch.draw(slot, posFocusX, posFocusY, 70, 70);
 			if (itemFocused != null) {
 				batch.draw(itemFocused.icon, posFocusX + 5, posFocusY + 15, 61,
 						51);
 			}
 		}
 		batch.draw(armorSlot, getX(), posY, 64, 64);
 		Armor armor = SoC.game.playermapper.get(SoC.game.player).armor;
 		if (armor != null) {
 			batch.draw(armor.icon, getX() + 5, posY + 15, 55, 45);
 		}
 		batch.draw(weaponSlot, getX()+64, posY, 64, 64);
 		Weapon weapon = SoC.game.playermapper.get(SoC.game.player).weapon;
 		if (weapon != null) {
 			batch.draw(weapon.icon, getX()+64 + 5, posY + 15, 55, 45);
 		}
 	}
 
 	public void updateRes(int witdh, int height) {
 		this.width = witdh;
 		this.height = height;
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		if(SoC.game.pause) return true;
 
 		if (Gdx.input.isKeyPressed(Keys.TAB)) {
 			if (focusedSlot == 20) {
 				focusedSlot = 1;
 			} else {
 				focusedSlot++;
 			}
 		} else if (Gdx.input.isKeyPressed(Keys.E)) {
 				Item item = SoC.game.playermapper.get(SoC.game.player).inventary[focusedSlot - 1];
 				if (item != null) {
 					item.use();
 				}
 				return true;
 		} else if(Gdx.input.isKeyPressed(Keys.B)) {
 			Item item = SoC.game.playermapper.get(SoC.game.player).inventary[focusedSlot - 1];
 			if (item != null) {
 				SoC.game.playermapper.get(SoC.game.player).removeFromInventary(item);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		if(SoC.game.pause) return true;
 		
 		Vector2 pos = parent.stage.screenToStageCoordinates(new Vector2(screenX, screenY));
 
 		if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
 			if (pos.x > getX() && pos.x < getX() + 320 && pos.y > getY()
 					&& pos.y < getY()+256) {
 				float posX = getX() + 256;
 				float posY = getY();
 				for (int i = Constants.Items.INVENTORY_SIZE; i > 0; i--) {
 					if (pos.x > posX && pos.x < posX + 64 && pos.y > posY
 							&& pos.y < posY + 64) {
 						Item item = SoC.game.playermapper.get(SoC.game.player).inventary[i - 1];
 						if (item != null) {
 							item.use();
 						}
 						return true;
 					}
 					posX -= 64;
 					if ((i - 1) % 5 == 0) {
 						posY += 64;
 						posX = getX() + 256;
 					}
 				}
 			} else {
 				if (pos.x > getX() && pos.x < getX()+64 && pos.y > getY()+256
 						&& pos.y < getY()+320) {
 					Armor armor = SoC.game.playermapper.get(SoC.game.player).armor;
 					if (armor != null) {
 						armor.remove();
 					}
 				} else {
 					if (pos.x > getX()+64 && pos.x < getX()+128 && pos.y > getY()+256
 							&& pos.y < getY()+320) {
 						Weapon weapon = SoC.game.playermapper
 								.get(SoC.game.player).weapon;
 						if (weapon != null) {
 							weapon.remove();
 						}
 					}
 				}
 
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		Vector2 pos = parent.stage.screenToStageCoordinates(new Vector2(screenX, screenY));
 		if (pos.x > getX() && pos.x < getX() + 320 && pos.y > getY()
 				&& pos.y < getY()+256) {
 			float posX = getX() + 256;
 			float posY = getY();
 			for (int i = Constants.Items.INVENTORY_SIZE; i > 0; i--) {
 				if (pos.x > posX && pos.x < posX + 64 && pos.y > posY
 						&& pos.y < posY + 64) {
 					focusedSlot = i;
 					Item item = SoC.game.playermapper.get(SoC.game.player).inventary[i - 1];
 					if (item != null) {
 						parent.tooltip.setText(item.tooltip, 0f);
 					} else {
 						parent.tooltip.setText(null, 0);
 					}
 					return false;
 				}
 				posX -= 64;
 				if ((i - 1) % 5 == 0) {
 					posY += 64;
 					posX = getX() + 256;
 				}
 			}
 		} 
 		//else {
 			//focusedSlot = 1;
 			//parent.tooltip.setText(null, 0);
 
 		//}
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void useFirstHealthPotion() {
		if(this.hasParent())return;
 		Potion candidate=null;
 		boolean found=false;
 		int candidateValue=-1;
 		for (int i = Constants.Items.INVENTORY_SIZE ; i > 0 && !found; i--) {
 				Item item = SoC.game.playermapper.get(SoC.game.player).inventary[i - 1];
 				if (item != null && item instanceof Potion) {
 					candidate=(Potion)item;
 					if(candidate.gainHealth>0 && candidate.gainMana==0){
 						found=true;
 						candidate.use();
 						return;
 					}else if(candidate.gainHealth>0 && candidate.gainMana>0){
 						candidateValue=i;
 					}
 				}
 		}
 		if(candidateValue != -1){
 			SoC.game.playermapper.get(SoC.game.player).inventary[candidateValue - 1].use();
 		} else {
 			EffectsPlayer.play("negative.ogg");
 		}
 }
 
 	public void useFirstManaPotion() {
		if(this.hasParent())return;
 		Potion candidate=null;
 		boolean found=false;
 		int candidateValue=-1;
 		for (int i = Constants.Items.INVENTORY_SIZE ; i > 0 && !found; i--) {
 				Item item = SoC.game.playermapper.get(SoC.game.player).inventary[i - 1];
 				if (item != null && item instanceof Potion) {
 					candidate=(Potion)item;
 					if(candidate.gainMana>0 && candidate.gainHealth==0){
 						found=true;
 						candidate.use();
 						return;
 					}else if(candidate.gainMana>0 && candidate.gainHealth>0){
 						candidateValue=i;
 					}
 				}
 				
 		}
 		if(candidateValue!=-1){
 			SoC.game.playermapper.get(SoC.game.player).inventary[candidateValue - 1].use();
 		} else {
 			EffectsPlayer.play("negative.ogg");
 		}
 		
 	}
 
 }
