 package Prototype;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PConstants;
 import processing.core.PImage;
 
 public class Part {
 	private Blueprint blueprint;
 	public Part parent;
 	ArrayList<Part> parts;
 
 
 	public Ratio pos;
 	public Ratio rel;
 	public Ratio size;
 	public Ratio scale;
 	public Ratio pivot;
 	public float left;
 	public float top;
 	public float right;
 	public float bottom;
 	boolean visible;
 	boolean enabled;
 	private float alpha;
 	public boolean showPivot;  
 
 	public Part (Blueprint blueprint) {
 		this.pos = blueprint.pos.get();
 		initPart(blueprint);
 	}
 
 	public Part (Blueprint blueprint, float x, float y) {
 		this.pos = new Ratio(x, y);
 		initPart(blueprint);
 	}
 
 	private void initPart (Blueprint blueprint) {
 		this.setBlueprint(blueprint);
 		this.rel = this.getBlueprint().rel.get();
 		this.size = new Ratio(this.getBlueprint().initialWidth, this.getBlueprint().initialHeight);
 		this.scale = this.getBlueprint().scale.get();
 		this.pivot = this.getBlueprint().pivot.get();
 
 		visible = this.getBlueprint().visible;
 		enabled = this.getBlueprint().enabled;
 		alpha = alpha(this.getBlueprint().alpha);
 		showPivot = this.getBlueprint().showPivot;
 
 
 		this.getBlueprint().initBlueprint();
 		calcBox();
 		readBlueprint();
 
 		parts = new ArrayList<Part>();
 	}
 
 	private void calcBox() {
		left = this.pos.x - this.pivot.x * this.size.x * this.scale.x;
 		top = pos.y - pivot.y * size.y * scale.y;
 		if(parent != null) {
 			left += rel.x * parent.size.x;
 			top += rel.y * parent.size.y;
 		}
 		right =  left + size.x * scale.x;
 		bottom = top + size.y * scale.y;
 	}
 
 	public void readBlueprint() {
 		getBlueprint().beginDraw();
		getBlueprint().clearBlueprint();
		//getBlueprint().background(0, 0);
 		getBlueprint().description();
 		getBlueprint().endDraw();
 	}
 
 	public void draw() {
 		if(visible) {
 			Prototype.stage.pushMatrix();
			Prototype.stage.translate(left, top);
 			Prototype.stage.pushStyle();
 			Prototype.stage.tint(255, 255*alpha);
			Prototype.stage.image(
					getBlueprint(),
					0,
					0,
					right-left,
					bottom-top
			);
 			if (showPivot) { 
 				drawPivot();
 			}
 			drawParts();
 			Prototype.stage.popStyle();
 			Prototype.stage.popMatrix();
 		}
 	}
 
 	void drawParts() {
 		for(int p=0; p < parts.size(); p++) {
 			Part part = parts.get(p);
 			part.draw();
 		}
 	}
 
 	private void drawPivot() {
 		Prototype.stage.pushStyle();
 		Prototype.stage.fill(255, 0, 0);
 		Prototype.stage.noStroke();
 		Prototype.stage.rectMode(PConstants.CENTER);
 		Prototype.stage.rect(0, 0, 5, 5);
 		Prototype.stage.popStyle();
 	}
 
 	public void pre() {
 		calcBox();
 		updateParts();
 	}
 
 	void updateParts() {
 		for(int p=0; p<parts.size(); p++) {
 			Part part = parts.get(p);
 			part.pre();
 		}
 	}
 
 	private boolean mouseInside(int shiftX, int shiftY) {
 		calcBox();
 		return (Prototype.stage.mouseX > shiftX+left && Prototype.stage.mouseX < shiftX+right && Prototype.stage.mouseY > shiftY+top && Prototype.stage.mouseY < shiftY+bottom) ? true : false;
 	}
 
 	public boolean mouseReallyInside(int shiftX, int shiftY) {
 		if (mouseInside(shiftX, shiftY)) {
 			int pixelX = Prototype.stage.mouseX - (int) (shiftX+left);
 			int pixelY = Prototype.stage.mouseY - (int) (shiftY+top);
 			PImage buffer = getBlueprint().get();
 			buffer.resize((int)size.x * (int)scale.x, (int)size.y * (int)scale.y);
 			buffer.loadPixels();
 			if (buffer.pixels[PApplet.constrain( pixelX + pixelY * (int)size.x, 0, buffer.pixels.length-1)] == 0x00000000) {
 				buffer.updatePixels();
 				return false;
 			}
 			buffer.updatePixels();
 			return true;    
 		}
 		return false;
 	}
 
 	public Part subpart(Blueprint blueprint) {
 		Part newPart = new Part(blueprint);
 		newPart.parent = this;
 		parts.add(newPart);
 		return newPart;
 	}
 
 	public Part subpart(Blueprint blueprint, float x, float y) {
 		Part newPart = new Part(blueprint, x, y);
 		newPart.parent = this;
 		parts.add(newPart);
 		return newPart;
 	}
 
 	public float alpha(float alpha) {
 		return alpha = PApplet.constrain(alpha, 0, 1);
 	}
 
 	public float alpha() {
 		return alpha;
 	}
 
 	public void setBlueprint(Blueprint blueprint) {
 		this.blueprint = blueprint;
 	}
 
 	public Blueprint getBlueprint() {
 		return blueprint;
 	}
 
 	public void mouseEvent(MouseEvent event) {
 		if(enabled && visible) {
 			for(int p=0; p < parts.size(); p++) {
 				Part part = parts.get(p);		
 				part.mouseEvent(event);		
 			}
 			blueprint.partEvent(new PartEvent(this, event));
 		}
 	}
 
 	public void mouseEvent(MouseWheelEvent event) {
 		if(enabled && visible) {
 			for(int p=0; p < parts.size(); p++) {
 				Part part = parts.get(p);		
 				part.mouseEvent(event);		
 			}
 			blueprint.partEvent(new PartEvent(this, event));
 		}
 	}
 
 	public boolean partEvent(MouseEvent event, float shiftX, float shiftY) {
 		if(enabled && visible) {
 			boolean found = false;
 			for(int p=parts.size()-1; p >= 0 ; p--) {
 				Part part = parts.get(p);		
 				found = part.partEvent(event, left+shiftX, top+shiftY);
 			}
 			if(!found) {
 				if(mouseReallyInside((int)shiftX, (int)shiftY)) {
 					blueprint.partEvent(new PartEvent(this, event, event.getID()-500));
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean partEvent(MouseWheelEvent event, float shiftX, float shiftY) {
 		if(enabled && visible) {
 			for(int p=parts.size()-1; p >= 0 ; p--) {
 				Part part = parts.get(p);		
 				if(part.partEvent(event, left+shiftX, top+shiftY)) {
 					part.blueprint.partEvent(new PartEvent(part, event, event.getID()-500));
 					break;
 				}
 			}
 			if(mouseReallyInside((int)shiftX, (int)shiftY)) {
 				blueprint.partEvent(new PartEvent(this, event, event.getID()-500));
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@SuppressWarnings("deprecation")
	void drawPlane(Ratio pivot, Ratio size, PImage texture) {
 		Prototype.stage.pushStyle();
 		Prototype.stage.textureMode(PConstants.NORMALIZED);
 		Prototype.stage.noStroke();
 		Prototype.stage.beginShape();
 		Prototype.stage.texture(texture);
 		Prototype.stage.vertex(-size.x*pivot.x, -size.y*pivot.y, 0, 0);
 		Prototype.stage.vertex(size.x*(1-pivot.x), -size.y*pivot.y, 1, 0);
 		Prototype.stage.vertex(size.x*(1-pivot.x), size.y*(1-pivot.y), 1, 1);
 		Prototype.stage.vertex(-size.x*pivot.x, size.y*(1-pivot.y), 0, 1);
 		Prototype.stage.endShape(PConstants.CLOSE);
 		Prototype.stage.popStyle();
 	}
 }
