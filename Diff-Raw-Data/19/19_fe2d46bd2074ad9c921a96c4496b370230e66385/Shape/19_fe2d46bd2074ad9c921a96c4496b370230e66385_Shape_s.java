 /*
  * Created on 12.06.2010
  *
  */
 package de.wertarbyte.chorddroid;
 
 public class Shape {
 	private final static int MUTE = -1;
 	
 	private final int[] pos;
 	
 	public Shape(int... p) {
 		pos = p;
 	}
 	
 	public static Shape createShape(String shape) {
 		String[] f = shape.split("-");
 		int[] p = new int[f.length];
 		for (int i = 0; i < f.length; i++) {
 			String c = f[i];
 			if ("x".equals(c.toLowerCase())) {
 				p[i] = MUTE;
 			} else {
 				p[i] = Integer.parseInt(c);
 			}
 		}
 		return new Shape(p);
 	}
 
 	public int[] getPositions() {
 		return pos;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < pos.length; i++) {
 			if (pos[i] == MUTE) {
 				sb.append('x');
 			} else {
 				sb.append(pos[i]);
 			}
 			if (i != pos.length-1) {
 				sb.append('-');
 			}
 		}
 		return sb.toString();
 	}
 	
 	public int getMinPos() {
 		int r=-2;
 		for (int i = 0; i < pos.length; i++) {
 			int p = pos[i];
			if (r == -2 || r > p) {
 				r = p;
 			}
 		}
 		return r;
 	}
 	public int getMaxPos() {
 		int r=-2;
 		for (int i = 0; i < pos.length; i++) {
 			int p = pos[i];
 			if (r == -2 || r < p) {
 				r = p;
 			}
 		}
 		return r;
 	}
 
 }
