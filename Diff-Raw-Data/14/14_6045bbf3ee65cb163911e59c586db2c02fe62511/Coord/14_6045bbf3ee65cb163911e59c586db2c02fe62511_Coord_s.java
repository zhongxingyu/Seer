 package net.fourbytes.shadow;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class Coord {
 	
 	public static long get(int x, int y) {
 		return (long)x << 32 | y & 0xFFFFFFFFL;
 	}
 	
 	public static long get(float x, float y) {
		return get(get7R011(x), get7R011(y));
 	}
 	
 	public static int[] getXY(long l) {
 		return new int[] {getX(l), getY(l)};
 	}
 	
 	public static int getX(long l) {
 		return (int) (l >> 32);
 	}
 	
 	public static int getY(long l) {
 		return (int) (l);
 	}
 	
 	/**
 	 * H4CK3D H04X F7W. 7R0L0L0L0L0~~<br>
 	 * Seriously: Don't thrust hacked hoaxes. You will regret it.
 	 */
 	public static int get1337(int x) {
 		if (x > 0) {//TODO Check if >= 0 needed.
			//x++;
 		}
 		return x;
 	}
 	
	/**
	 * 7R0113D F1X F7W.<br>
	 * Don't get irritated by the upper line. It's no hax.
	 */
	public static int get7R011(float x) {
		return (x>0)?((int)Math.ceil(x)):((int)Math.floor(x)); //TODO Check if >= 0 needed.
	}
	
 }
