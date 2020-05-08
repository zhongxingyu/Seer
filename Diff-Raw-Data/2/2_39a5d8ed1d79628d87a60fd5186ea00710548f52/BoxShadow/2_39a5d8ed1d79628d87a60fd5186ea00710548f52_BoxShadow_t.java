 package com.eagerlogic.cubee.client.styles;
 
 import com.google.gwt.dom.client.Element;
 
 /**
  *
  * @author dipacs
  */
 public final class BoxShadow {
 	
 	public static class BoxShadowBuilder {
 		
 		private int hPos = 5;
 		private int vPos = 5;
 		private int blur = 5;
 		private int spread = 0;
 		private Color color = Color.getArgbColor(0xc0000000);
 		private boolean inner = false;
 
 		private BoxShadowBuilder() {
 		}
 
 		public void setHPos(int hPos) {
 			this.hPos = hPos;
 		}
 
 		public void setVPos(int vPos) {
 			this.vPos = vPos;
 		}
 
 		public void setBlur(int blur) {
 			this.blur = blur;
 		}
 
 		public void setSpread(int spread) {
 			this.spread = spread;
 		}
 
 		public void setColor(Color color) {
 			this.color = color;
 		}
 
 		public void setInner(boolean inner) {
 			this.inner = inner;
 		}
 		
 		public BoxShadow build() {
 			return new BoxShadow(hPos, vPos, blur, spread, color, inner);
 		}
 		
 	}
 	
 	public static BoxShadowBuilder builder() {
 		return new BoxShadowBuilder();
 	}
 	
 	private final int hPos;
 	private final int vPos;
 	private final int blur;
 	private final int spread;
 	private final Color color;
 	private final boolean inner;
 
 	public BoxShadow(int hPos, int vPos, int blur, int spread, Color color, boolean inner) {
 		this.hPos = hPos;
 		this.vPos = vPos;
 		this.blur = blur;
 		this.spread = spread;
 		this.color = color;
 		this.inner = inner;
 	}
 
 	public int getHPos() {
 		return hPos;
 	}
 
 	public int getVPos() {
 		return vPos;
 	}
 
 	public int getBlur() {
 		return blur;
 	}
 
 	public int getSpread() {
 		return spread;
 	}
 
 	public Color getColor() {
 		return color;
 	}
 
 	public boolean isInner() {
 		return inner;
 	}
 	
 	public void apply(Element element) {
 		element.getStyle().setProperty("boxShadow", hPos + "px " + vPos + "px " + blur + "px " + spread + "px " 
				+ color.toCSS() + (inner ? " inset" : ""));
 		
 	}
 
 }
