 /*
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package net.sekien.elesmyr.ui;
 
 import com.esotericsoftware.minlog.Log;
 import net.sekien.elesmyr.msgsys.MessageEndPoint;
 import net.sekien.elesmyr.player.Camera;
 import net.sekien.elesmyr.system.FontRenderer;
 import net.sekien.elesmyr.system.GameClient;
 import net.sekien.elesmyr.system.Main;
 import net.sekien.elesmyr.ui.dm.*;
 import net.sekien.elesmyr.util.FileHandler;
 import net.sekien.hbt.*;
 import net.sekien.pepper.Renderer;
 import org.newdawn.slick.*;
 import org.newdawn.slick.gui.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA. User: matt Date: 17/04/13 Time: 4:51 PM To change this template use File | Settings |
  * File Templates.
  */
 public class DevMode implements UserInterface {
 
 	public static final Color TEXT_BG = new Color(1, 1, 1, 0.2f);
 	public static final Color PANEL_BG = new Color(0, 0, 0, 0.75f);
 	private String target = "NULL";
 	private HBTCompound list;
 	private int panelWidth = 260;
 	private boolean enabled = true;
 	private HBTTag activeElement;
 	private TextField textField;
 	private boolean showTextField;
 
 	private HBTCompound listNew; //NEW list
 	private HBTCompound listDM; //DevMode list
 
 	private final HBTTag targetAETag = new HBTComment("_DTARGET");
 
 	private HashMap<String, DevModeTarget> targets;
 	private ArrayList<String> openCompounds;
 
 	private boolean inited = false;
 
 	private int updateTimer = 10;
 
 	private String tmpGetReturnId;
 
 	@Override
 	public boolean inited() {return inited; }
 
 	@Override
 	public void ctor(String extd) {
 	}
 
 	@Override
 	public void init(GameContainer gc, MessageEndPoint receiver) throws SlickException {
 		inited = true;
 
 		textField = new TextField(gc, FontRenderer.getFont(), 0, 16, 530, 16);
 		textField.setBorderColor(null);
 		textField.setBackgroundColor(new Color(0, 0, 0, 0.2f));
 		textField.setTextColor(Color.white);
 		textField.setAcceptingInput(false);
 		textField.setMaxLength(57);
 
 		//listNew = new HBTCompound("NEW");
 		//listDM = new HBTCompound("DEVMODE");
 
 		targets = new HashMap<String, DevModeTarget>();
 		targets.put("NEW", new StoredListTarget());
 		targets.get("NEW").getList(null, null).addTag(new HBTInt("aint", 123));
 		targets.get("NEW").getList(null, null).addTag(new HBTString("cheese", "MARY HAD A LITTLE EGG"));
 		targets.get("NEW").getList(null, null).addTag(HBTTools.location("here", 42, 9001));
 		targets.get("NEW").getList(null, null).addTag(new HBTCompound("baa", new HBTTag[]{new HBTInt("aint", 123), new HBTComment("ADD FIELD")}));
 		targets.get("NEW").getList(null, null).addTag(new HBTByteArray("r2", new byte[]{0, 54, 75, 44, 3, 45, 32, 6, 56, 54, 3, 64, 36, 46}));
 		targets.put("DEVMODE", new StoredListTarget());
 		targets.put("DATA", new ReadOnlyTarget(FileHandler.getData()));
 		targets.put("ENT", new ServerEntTarget("clearing_thing"));
 		targets.put("PDAT", new ServerSideTarget(((GameClient) receiver).player.getRegionName()+"."+((GameClient) receiver).player.entid, "pdat_GET", "pdat_SET"));
 		//targets.put("ENT",new ServerEntTarget());
 		openCompounds = new ArrayList<String>();
 	}
 
 	@Override
 	public void render(Renderer renderer, Camera cam, GameClient receiver) throws SlickException {
 		Graphics g = renderer.g;
 		if (enabled) {
 			g.setColor(PANEL_BG);
 			g.fillRect(Main.INTERNAL_RESX-panelWidth, 16, panelWidth, Main.INTERNAL_RESY-80);
 			g.setColor(Color.white);
 
 			if (showTextField) {
 				//textField.render(gc, g);
 				textField.setFocus(true);
 			}
 
 			if (activeElement == targetAETag) {
 				renderTextField((Main.INTERNAL_RESX-panelWidth)+6, 16, g);
 			} else {
 				FontRenderer.drawString((Main.INTERNAL_RESX-panelWidth)+6, 16, target, g);
 			}
 
 			if (list != null) {
 				renderList((Main.INTERNAL_RESX-panelWidth)+6+9, 32, list, g, "");
 			}
 		}
 	}
 
 	private int renderList(int x, int y, HBTCompound list, Graphics g, String idprefix) {
 		int ry = y;
 		for (HBTTag tag : list) {
 			if (tag == activeElement) {
 				renderTextField(x, ry, g);
 				g.setColor(TEXT_BG);
 				g.fillRect(x, ry, panelWidth-20, 16);
 				g.setColor(Color.white);
 			} else if (tag instanceof HBTCompound) {
 				if (openCompounds.contains(idprefix+"."+tag.getName())) {
 					FontRenderer.drawString(x, ry, tag.getName()+" [-]", g);
 					ry += renderList(x+18, ry+16, (HBTCompound) tag, g, idprefix+"."+tag.getName());
 				} else {
 					FontRenderer.drawString(x, ry, tag.getName()+" [+]", g);
 				}
 			} else if (tag instanceof HBTComment) {
 				FontRenderer.drawString(x, ry, "["+tag.getName()+"]", g);
 			} else {
 				FontRenderer.drawString(x, ry, tag.getName()+" ="+tag.toString().split("=", 2)[1], g);
 			}
 			ry += 16;
 		}
 		return (ry-y);
 	}
 
 	private boolean mouseDragging = false;
 
 	@Override
 	public void update(GameContainer gc, GameClient receiver) {
 		Input input = gc.getInput();
 		if (input.isKeyPressed(Input.KEY_TAB)) {
 			enabled = !enabled;
 			panelWidth = 260;
 		}
 
 		if (enabled) {
 			int mx = (int) (((float) input.getMouseX()/gc.getWidth())*Main.INTERNAL_RESX);
 			int my = (int) (((float) input.getMouseY()/gc.getHeight())*Main.INTERNAL_RESY);
 
 			if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 				if (mx > (Main.INTERNAL_RESX-panelWidth)-16 && mx < (Main.INTERNAL_RESX-panelWidth)+16) {
 					mouseDragging = true;
 				}
 			} else {
 				mouseDragging = false;
 			}
 
			if (input.isKeyDown(Input.KEY_RETURN)) writeActiveElement(receiver);
 
 			if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 				if (activeElement != null) {
 					writeActiveElement(receiver);
 					activeElement = null;
 				} else if (mx > (Main.INTERNAL_RESX-panelWidth)+16 && my > 16 && my < Main.INTERNAL_RESY-64) {
 					my = my-16;
 					if (my < 16) { //In the 'target' area.
 						activeElement = targetAETag;
 						setTextFieldActive(true);
 						setText(target);
 					} else if (list != null) {
 						Object found = getElementAt((my-16)/16, list, "");
 						if (found instanceof Integer) {
 							activeElement = null;
 							setTextFieldActive(false);
 						} else if (found instanceof HBTCompound) {
 							HBTTag element = (HBTTag) found;
 							if (openCompounds.contains(tmpGetReturnId)) {
 								openCompounds.remove(tmpGetReturnId);
 							} else {
 								openCompounds.add(tmpGetReturnId);
 							}
 						} else {
 							HBTTag element = (HBTTag) found;
 							activeElement = element;
 							setTextFieldActive(true);
 							setText(element.toString());
 						}
 					} else {
 						activeElement = null;
 						setTextFieldActive(false);
 					}
 				} else {
 					activeElement = null;
 					setTextFieldActive(false);
 				}
 			}
 
 			if (mouseDragging) {
 				panelWidth = Math.min(Math.max(-(mx-Main.INTERNAL_RESX), 20), Main.INTERNAL_RESX-20);
 			}
 			if (activeElement == null)
 				updateTimer--;
 			if (updateTimer < 1) {
 				updateList(receiver);
 				updateTimer = 10;
 			}
 		}
 	}
 
 	private Object getElementAt(int i, HBTCompound search, String idprefix) {
 		if (search == null) //In case target=="NULL"
 			return null;
 		int si = 0;
 		for (HBTTag tag : search) {
 			if (si == i) {
 				tmpGetReturnId = idprefix+"."+tag.getName();
 				return tag;
 			} else if (tag instanceof HBTCompound) {
 				if (openCompounds.contains(idprefix+"."+tag.getName())) {
 					Object found = getElementAt(i-si-1, (HBTCompound) tag, idprefix+"."+tag.getName());
 					if (found instanceof HBTTag) {
 						return found;
 					} else {
 						si += (Integer) found;
 					}
 				}
 			}
 			si++;
 		}
 		return new Integer(si);
 	}
 
 	private void writeActiveElement(GameClient client) {
 		String str = textField.getText();
 		if (activeElement == targetAETag) { //Target
 			String olTarget = target;
 			target = str;
 			if (!updateList(client)) {
 				target = olTarget;
 			}
 		} else {
 			HBTCompound test = getActiveParent(list);
 			if (test == null) {
 				Log.error("DevMode activeElement unrecognised: "+activeElement);
 			} else {
 				int index = test.getData().indexOf(activeElement);
 				HBTTag old = activeElement;
 				test.getData().remove(activeElement);
 				try {
 					for (HBTTag tag : FileHandler.parseTextHBT(textField.getText()))
 						test.getData().add(index, tag);
 				} catch (Exception e) {
 					try {
 						test.getData().add(index, old); //Reset incase adding new tag fails.
 						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 					} catch (Exception e2) {
 						e2.initCause(e);
 						e2.printStackTrace();
 					}
 				}
 				targetUpdate(client);
 			}
 		}
 	}
 
 	private void targetUpdate(GameClient client) {
 		String target = "";
 		String subtarget = "";
 		if (this.target.contains(".")) {
 			target = this.target.split("\\.", 2)[0];
 			subtarget = this.target.split("\\.", 2)[1];
 		} else {
 			target = this.target;
 		}
 
 		if (target.equals("NULL")) {
 		} else if (targets.containsKey(target)) {
 			targets.get(target).set(list, subtarget, client);
 		} else {
 			Log.error("Unrecognised target "+target+".");
 		}
 	/*} else if (target.equals("NEW")) {
 		listNew = list; //Unneeded?
 	} else if (target.startsWith("ENT")) {
 		String sub = target.split("\\.", 2)[1];
 		Entity ent = client.getPlayer().getRegion().entities.get(Integer.parseInt(sub));
 		ent.fromHBT(list);
 	}*/
 	}
 
 	private HBTCompound getActiveParent(HBTCompound search) {
 		for (HBTTag tag : search) {
 			if (tag == activeElement) {
 				return search;
 			} else if (tag instanceof HBTCompound) {
 				if (getActiveParent((HBTCompound) tag) != null) {
 					return (HBTCompound) tag;
 				}
 			}
 		}
 		return null;
 	}
 
 	private boolean updateList(GameClient client) {
 		String target = "";
 		String subtarget = "";
 		if (this.target.contains(".")) {
 			target = this.target.split("\\.", 2)[0];
 			subtarget = this.target.split("\\.", 2)[1];
 		} else {
 			target = this.target;
 		}
 
 		if (target.equals("NULL")) {
 			list = null;
 		} else if (targets.containsKey(target)) {
 			list = targets.get(target).getList(client, subtarget);
 		} else {
 			Log.error("Unrecognised target "+target+".");
 			return false;
 		}
 	/*if (target.equals("NULL")) {
 		list = null;
 	} else if (target.equals("NEW")) {
 		list = listNew;
 	} else if (target.startsWith("ENT.")) {
 		String sub = target.split("\\.", 2)[1];
 		Entity ent = client.getPlayer().getRegion().entities.get(Integer.parseInt(sub));
 		list = ent.toHBT(false);
 	} else {
 		return false;
 	}*/
 		return true;
 	}
 
 	@Override
 	public boolean blockUpdates() {
 		return false;
 	}
 
 	public void setTextFieldActive(boolean active) {
 		showTextField = active;
 		if (!active)
 			setText("");
 		textField.setAcceptingInput(active);
 		textField.setFocus(active);
 	}
 
 	private void setText(String text) {
 		textField.setText(text);
 		textField.setCursorPos(text.length());
 	}
 
 	public void renderTextField(int x, int y, Graphics g) {
 		FontRenderer.drawString(x, y, textField.getText(), g);
 		FontRenderer.drawString(x-4+textField.getText().length()*9, y, "_", g);
 	}
 }
