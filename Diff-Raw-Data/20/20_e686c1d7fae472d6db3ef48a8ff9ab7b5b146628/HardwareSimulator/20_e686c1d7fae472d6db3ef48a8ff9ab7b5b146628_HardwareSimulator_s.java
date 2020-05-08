 package propinquity.hardware;
 
 import processing.core.*;
 import java.util.*;
 import java.awt.event.KeyEvent;
 import propinquity.*;
 
 public class HardwareSimulator implements HardwareInterface, UIElement {
 
 	PApplet p;
 
 	boolean isVisible;
 
 	int width, height, current_patch;
 
 	PFont font;
 
 	Vector<Glove> gloves;
 	Vector<Patch> patches;
 	Vector<ProxEventListener> proxListeners;
 
 	public HardwareSimulator(PApplet parent) {
 		this(parent, 500, 100);
 	}
 
 	public HardwareSimulator(PApplet parent, int width, int height) {
 		this.p = parent;
 
 		this.width = width;
 		this.height = height;
 
 		font = p.createFont("Arial", 20);
 
 		gloves = new Vector<Glove>();
 		patches = new Vector<Patch>();
 		proxListeners = new Vector<ProxEventListener>();
 
 		p.registerKeyEvent(this);
 	}
 
 	public void addPatch(Patch patch) {
 		if(patches.indexOf(patch) == -1) patches.add(patch);
 	}
 
 	public boolean removePatch(Patch patch) {
 		return patches.remove(patch);
 	}
 
 	public void addGlove(Glove glove) {
 		if(gloves.indexOf(glove) == -1) gloves.add(glove);
 	}
 
 	public boolean removeGlove(Glove glove) {
 		return gloves.remove(glove);
 	}
 
 	public void addProxEventListener(ProxEventListener listener) {
 		if(proxListeners.indexOf(listener) == -1) proxListeners.add(listener);
 	}
 
 	public boolean removeProxEventListener(ProxEventListener listener) {
 		return proxListeners.remove(listener);
 	}
 
 	public void sendPacket(Packet packet) {
 		//Do nothing
 	}
 
 	public void draw() {
 		if(isVisible) {
 			p.strokeWeight(3);
 			p.stroke(100);
 			p.fill(0);
 
 			p.rect(-3, -3, width+6, height+6);
 
 			int num_patch = patches.size();
 			int num_gloves = gloves.size();
 			int total = num_patch+num_gloves;
 			int local_width = width/total;
 
 			p.strokeWeight(1);
 			p.noStroke();
 
 			for(int i = 0;i < num_patch+num_gloves;i++) {
 				p.pushMatrix();
 				p.translate(local_width*(i), 0);
 
 				if(i < num_patch) {
 					int[] color = patches.get(i).getColor();
 					p.fill(color[0], color[1], color[2]);
 					p.rect(0, 0, local_width*0.9f, height/2);
 
 					int vibe = patches.get(i).getVibeLevel();
 					p.fill(vibe);
 					p.rect(0, height/2, local_width*0.9f, height/2);
 
 					int prox = patches.get(i).getProx();
 					p.fill(100);
					p.rect(local_width*0.9f, height, local_width*0.1f, p.map(prox, 0, 1024, 0, -height));
 				} else {
 					int vibe = gloves.get(i-num_patch).getVibeLevel();
 					p.fill(vibe);
 					p.rect(0, height, local_width*0.9f, height);
 				}
 
 				if(current_patch == i) {
 					p.fill(100, 0, 0);
 					p.rect(2, 2, 15, 15);
 				}
 
 				p.stroke(100);
 				p.noFill();
 
 				p.rect(0, 0, local_width, height);
 
 				p.noStroke();
 
 				p.popMatrix();
 			}
 		}
 	}
 
 	public void show() {
 		isVisible = true;
 	}
 
 	public void hide() {
 		isVisible = false;
 	}
 
 	public boolean isVisible() {
 		return isVisible;
 	}
 
 	public void keyEvent(KeyEvent e) {
 		if(isVisible) {
			char keychar = e.getKeyChar();
 			int keycode = e.getKeyCode();
 			
 			if(keycode >= 48 && keycode <= 57) {
 				current_patch = PApplet.constrain(keycode-48, 0, patches.size()-1);
 			} else if(keycode == KeyEvent.VK_MINUS) {
 				Patch patch = patches.get(current_patch);
 				if(patch.getActive()) {
 					patch.setProx(patch.getProx()-50);
 					for(ProxEventListener listener : proxListeners) listener.proxEvent(patch);
 				}
 			} else if(keycode == KeyEvent.VK_EQUALS) {
 				Patch patch = patches.get(current_patch);
 				if(patch.getActive()) {
 					patch.setProx(patch.getProx()+50);
 					for(ProxEventListener listener : proxListeners) listener.proxEvent(patch);
 				}
 			}
 		}
 	}
 }
