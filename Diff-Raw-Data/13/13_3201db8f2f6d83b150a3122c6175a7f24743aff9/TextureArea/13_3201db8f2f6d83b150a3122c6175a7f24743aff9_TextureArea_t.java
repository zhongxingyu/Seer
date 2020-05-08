 package de.futjikato.mrwhiz.xml;
 
 import org.lwjgl.opengl.GL11;
 
 import de.futjikato.mrwhiz.Renderable;
 import de.futjikato.mrwhiz.xml.attributes.Dimensions;
 import de.futjikato.mrwhiz.xml.attributes.XmlAttribute;
 
 public class TextureArea extends XmlObject implements Renderable {
 
 	private Texture texture;
 
 	@Override
 	public void handleValue(String currentValue) throws ObjectNoValueSupport {
 		throw new ObjectNoValueSupport();
 	}
 
 	@Override
 	public void addChildObj(XmlObject mapObj) {
 		if(mapObj instanceof Texture) {
 			this.texture = (Texture) mapObj;
 		}
 	}
 
 	@Override
 	public void draw() {
 		
 		// get dimension attributes
 		XmlAttribute dimAttr = this.attrs.get("xywh");
 		if(!(dimAttr instanceof Dimensions)) {
 			return;
 		}
 		Dimensions dim = (Dimensions)dimAttr;
 		
 		GL11.glPushMatrix();
 	    
 			//TODO calculate position on screen
 			GL11.glTranslatef(dim.getX(), dim.getY(), 0);
 			
 			// bind texture
 			if(this.texture != null) {
 				this.texture.bind();  
 			}
 			
 			GL11.glBegin(GL11.GL_QUADS);
 				
				
 				if(this.texture != null) {
					GL11.glTexCoord2f(0, (float)dim.getH() / (float)this.texture.getHeight());
 				}
 				GL11.glVertex2i(0, 0);
 				
 				if(this.texture != null) {
					GL11.glTexCoord2f((float)dim.getW() / (float)this.texture.getWidth(), (float)dim.getH() / (float)this.texture.getHeight());
 				}
 				GL11.glVertex2i(dim.getW(), 0);
 				
 				if(this.texture != null) {
					GL11.glTexCoord2f((float)dim.getW() / (float)this.texture.getWidth(), 0);
 				}
 				GL11.glVertex2i(dim.getW(), dim.getH());
 				
 				if(this.texture != null) {
					GL11.glTexCoord2f(0, 0);
 				}
 				GL11.glVertex2i(0, dim.getH());
 				
 			GL11.glEnd();
 			
 		GL11.glPopMatrix();
 	}
 
 	@Override
 	protected void initAttributeFallback() {
 		XmlAttribute dim = this.attrs.get("xywh");
 		if(dim == null) {
 			
 		}
 	}
 }
