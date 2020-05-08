package us.exultant.ahs.terminal;
 
 public interface Terminal {
 	
 	public Cursor cursor();
 	
 	public static interface Cursor {	// i'd like to be able to have component-like boxes that can confine the cursor... but we're talking about a system that regularly screws up the distinction between input and output channels, and so i'm deeply afraid we really might not be able to do that.
 		/** Places the cursor at an arbitrary horizontal and vertical position.  The values are 1-based and counted in user-space (i.e. increasing {@code $y} is moving down). */
 		public void place(int $x, int $y);
 		
 		/** Moves the cursor {@code $n} cells up from its current location. If the cursor is already at the edge of the screen, calling this method has no effect. */
 		public void shiftUp(int $n);
 		/** Moves the cursor {@code $n} cells down from its current location. If the cursor is already at the edge of the screen, calling this method has no effect. */
 		public void shiftDown(int $n);
 		/** Moves the cursor {@code $n} cells left/backward from its current location. If the cursor is already at the edge of the screen, calling this method has no effect. */
 		public void shiftLeft(int $n);
 		/** Moves the cursor {@code $n} cells right/forward from its current location. If the cursor is already at the edge of the screen, calling this method has no effect. */
 		public void shiftRight(int $n);
 		
 		/** Moves cursor to beginning of the line {@code $n} lines down. */
 		public void lineNext(int $n);
 		/** Moves cursor to beginning of the line {@code $n} lines up. */
 		public void linePrev(int $n);
 	}
 	
 	
 	
 	public Palette palette();
 	
 	public static interface Palette {
 		public void bold(boolean $n);
 		public void blink(boolean $n);
 		public void underline(boolean $n);
 		public void background(Color $c);
 		public void foreground(Color $c);
 	}
 	
 	public static enum Color {
 		BLACK(0), RED(1), GREEN(2), YELLOW(3), BLUE(4), MAGENTA(5), CYAN(6), WHITE(7);
 		
 		Color(int $c) { this.$c = $c; }
 		private final int $c;
 		public int code() { return $c; }	// this happens to be pretty much the same as calling ordinal, but I like my semantics solid.
 	}
 	
 	
 	
 	public void clear();
 	
 	public int getWidth();
 	
 	public int getHeight();
 	
 	
 	
 	public static enum Event {
 		INPUT,
 		RESIZE,
 		CURSOR,
 	}
 }
