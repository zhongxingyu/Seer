 /*
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 /*
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package net.sekien.elesmyr.ui;
 
 import net.sekien.elesmyr.msgsys.MessageEndPoint;
 import net.sekien.elesmyr.player.Camera;
 import net.sekien.elesmyr.system.FontRenderer;
 import net.sekien.elesmyr.system.GameClient;
 import net.sekien.elesmyr.system.Main;
 import net.sekien.elesmyr.util.FileHandler;
 import net.sekien.pepper.Renderer;
 import org.newdawn.slick.*;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class DMSGUI implements UserInterface {
 	private boolean inited = false;
 	private static LinkedList<DMessage> msgBuffer;
 	private Image msg;
 
 	@Override public boolean inited() { return inited; }
 
 	public DMSGUI() {
 		msgBuffer = new LinkedList<DMessage>();
 	}
 
 	@Override public void init(GameContainer gc, MessageEndPoint receiver) throws SlickException {
 		inited = true;
 		msg = FileHandler.getImage("ui.hud.msg");
 		displayMessage("Loaded!", null);
 		displayMessage("Obtained Gold Sword!", "item.swordgold");
 		displayMessage("Obtained Egg!", "item.egg");
 		displayMessage("Msg3!", null);
 	}
 
 	@Override public void render(Renderer renderer, Camera cam, GameClient receiver) throws SlickException {
 		int i = 0;
         Graphics g = renderer.g;
         if(msgBuffer.size()>0) {
             msg.draw(Main.INTERNAL_RESX-360, 0);
         }
 		for (DMessage dmsg : msgBuffer) {
 			float anim_enter = Math.min(20, 200-dmsg.getTime())/20f;
 			float anim_leave = Math.min(20, dmsg.getTime())/20f;
 			Color alpha = new Color(1, 1, 1, anim_leave);
 
			int yoffset = (int) (i+(anim_enter-1));
 			renderer.pushPos(Main.INTERNAL_RESX-256, yoffset);
             FontRenderer.drawStringFlat(248-FontRenderer.getStringWidth(dmsg.getText()), 8, dmsg.getText(), alpha,g);
 			i = yoffset+20;
 			renderer.popPos();
 		}
 	}
 
 	@Override public void update(GameContainer gc, GameClient receiver) {
 		List<DMessage> removeMessages = new LinkedList<DMessage>();
 		for (DMessage dmsg : msgBuffer) {
 			if (dmsg.update()) {
 				removeMessages.add(dmsg);
 			}
 		}
 		msgBuffer.removeAll(removeMessages);
 	}
 
 	@Override public boolean blockUpdates() {return false;}
 
 	@Override public void ctor(String extd) {}
 
 	public static void displayMessage(String text, String image) {
 		msgBuffer.addFirst(new DMessage(text, image));
 	}
 
 	private static class DMessage {
 		private final String text;
 		private final String image;
 		private Image image_cached;
 		private int timer;
 
 		public DMessage(String text, String image) {
 			this.text = text;
 			this.image = image;
 			timer = 200;
 		}
 
 		public boolean update() {
 			return timer-- == 0;
 		}
 
 		public Image getImage() {
 			if (image_cached == null) {
 				try {
 					image_cached = FileHandler.getImage(image);
 				} catch (SlickException e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 			return image_cached;
 		}
 
 		public boolean hasImage() {
 			return image != null;
 		}
 
 		public String getText() {
 			return text;
 		}
 
 		public int getTime() {
 			return timer;
 		}
 
 		@Override public String toString() {
 			return "\""+text+(image == null?"\"":"\":"+image);
 		}
 	}
 }
