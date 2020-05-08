 package us.exultant.ahs.terminal.gui;
 
 import us.exultant.ahs.terminal.*;
 import java.awt.*;
 
 /**
  * <p>
  * Buffers a 2-D character array with graphical attributes (i.e. a {@link Palette}).
  * Typically, one of these with a single backing array is produced for every draw to be
  * rendered on the terminal, then various other Window objects as "views" to the first are
  * spawned for each component to be drawn, always with a clip assigned to it so that
  * components cannot draw outside of their allocated area.
  * </p>
  * 
  * <p>
  * A window clipped to 2,1,6,4 will be able to draw on the starred area:
  * 
  * <pre>
  *  012345678
  * 0.........
  * 1..******.
  * 2..******.
  * 3..******.
  * 4..******.
  * 5.........
  * 6.........
  * 7.........
  * </pre>
  * 
  * Placing a character in relative position 3,1 after the clip will place the character in
  * 5,2 globally. The farthest to the bottom-left that it is legal to place a character
  * with this clip is 5,3 (global 7,4).
  * </p>
  * 
  * @author hash
  * 
  */
 public class Window {
 	public Window(int $w, int $h) {
 		$chars = new char[$h][$w];
 		$pales = new Palette[$h][$w];
 		$clip = new Rectangle(0,0,$w,$h);
 	}
 	
 	public Window clip(Rectangle $r) {
 		if (
 				$r.x < 0 ||
 				$r.y < 0 ||
 				$r.width < 0 ||
 				$r.height < 0
 		) throw new IllegalArgumentException("negative dimensions not allowed in clip");
 		return new Window(this, $r);
 	}
 	private Window(Window $parent, Rectangle $newClip) {
 		if (
 				$newClip.width+$newClip.x > $parent.$clip.width ||
 				$newClip.width+$newClip.x > $parent.$clip.width
 		) throw new IllegalArgumentException("new clip must fit within already active clip");
 		$chars = $parent.$chars;
 		$pales = $parent.$pales;
 		$clip = new Rectangle(
 				$parent.$clip.x + $newClip.x,
 				$parent.$clip.y + $newClip.y,
 				Math.min($parent.$clip.width-$newClip.x, $newClip.width),
 				Math.min($parent.$clip.height-$newClip.y, $newClip.height)
 		);
 	}
 	
 	/**
 	 * {@code [y][x]}, because our priority is to iterate horizontally with high
 	 * efficiency; we're less concerned with vertical since that's not how we render.
 	 */
 	private final char[][]		$chars;
 	
 	/**
 	 * {@code [y][x]}, because our priority is to iterate horizontally with high
 	 * efficiency; we're less concerned with vertical since that's not how we render.
 	 * 
 	 * I'll be the first to admit that this seems like a rather obscenely large amount
 	 * of memory to throw at this (it's substantially larger than what we're likely to
 	 * actually issue as escape sequences to the console when we dump out the actual
 	 * render). But I can't think of any significantly better idea at the moment, and
 	 * we're still talking kilobytes here (not megs), even for large screens.
 	 */
 	private final Palette[][]	$pales;
 	
 	/**
 	 * The active clip.  The {@code x} and {@code y} coordinates are global.
 	 */
 	private final Rectangle		$clip;
 	
 	protected final int absX(int $x) {
 		if ($x < 0 || $x >= $clip.width) return -1;
 		return ($x + $clip.x);
 	}
 	
 	protected final int absY(int $y) {
 		if ($y < 0 || $y >= $clip.height) return -1;
 		return ($y + $clip.y);
 	}
 	
 	/**
 	 * Writes the given string to the window buffer starting at the given coordinates
 	 * and using the specified Palette, placing each character to the right of the
 	 * last.
 	 * 
 	 * If the string cannot fit within the active clip, it will be truncated.
 	 * 
 	 * @throws IllegalArgumentException
 	 *                 if the {@code x} or {@code y} coordinates are negative or
	 *                 greater than allowed by the active clip.
 	 */
 	public void print(int $x, int $y, String $str, Palette $pal) {
 		final int $yg = absY($y);
 		int $xg = absX($x);
 		if ($xg == -1 || $yg == -1) throw new IllegalArgumentException("starting location ("+$x+","+$y+") not inside active clip");
 		final int $len = Math.min($clip.width-$x, $str.length());
 		final char[] $charsRow = $chars[$yg];
 		final Palette[] $palesRow = $pales[$yg];
 		for (int $i = 0; $i < $len; $i++, $xg++) {
 			$charsRow[$xg] = $str.charAt($i);
 			$palesRow[$xg] = $pal;	// i'd like to honor the concept of null color selections in a pallete just leaving the previous setting intact, but i can't think of a terribly convenient way to do that without spamming craploads of new palette objects all the time.
 		}
 	}
 	
 	/**
 	 * Writes the given string to the window buffer starting at the given coordinates
 	 * and using the specified Palette, placing each character below the last.
 	 * 
 	 * If the string cannot fit within the active clip, it will be truncated.
 	 * 
 	 * @throws IllegalArgumentException
 	 *                 if the {@code x} or {@code y} coordinates are negative or
	 *                 greater than allowed by the active clip.
 	 */
 	public void printVertical(int $x, int $y, String $str, Palette $pal) {
 		final int $xg = absX($x);
 		int $yg = absY($y);
 		if ($xg == -1 || $yg == -1) throw new IllegalArgumentException("starting location ("+$x+","+$y+") not inside active clip");	
 		final int $len = Math.min($clip.height-$y, $str.length());
 		for (int $i = 0; $i < $len; $i++, $yg++) {
 			$chars[$yg][$xg] = $str.charAt($i);
 			$pales[$yg][$xg] = $pal;
 		}
 	}
 	
 	/**
 	 * Draws all characters buffered in this Window onto a Terminal (assuming that the
 	 * dimentions of this Window match the dimensions of the Terminal; if not the draw
 	 * will be truncated or leave blank space). The Terminal is cleared before drawing
 	 * begins.
 	 * 
 	 * @param $term
 	 *                the Terminal to render on
 	 */
 	public void render(Terminal $term) {
 		$term.clear();
 		render($term, new Rectangle(0, 0, $chars[0].length, $chars.length));
 	}
 	
 	/**
 	 * Draws all characters buffered in this Window onto a region of a Terminal.
 	 * 
 	 * @param $term
 	 *                the Terminal to render on
 	 * @param $region
 	 *                the "dirty" area of the screen that needs rendering. If the
 	 *                coordinates of this rectangle are outside of the boundaries of
 	 *                the Terminal, this object will be modified such that the
 	 *                coordinates are now confined to the boundaries of the Terminal.
 	 */
 	public void render(Terminal $term, Rectangle $region) {
 		$region.x = Math.max(0, $region.x);
 		$region.y = Math.max(0, $region.y);
 		$region.width = Math.min($term.getWidth()-$region.x, $region.width);
 		$region.height = Math.min($term.getHeight()-$region.y, $region.height);
 		
 		final StringBuilder $sb = new StringBuilder();
 		Palette $prevPalette = null;		// use this to shortcut out of redundant escape sequences and checks whenever possible
 		final int $ymax = $region.y + $region.height;
 		final int $xmax = $region.x + $region.width;
 		for (int $y = $region.y; $y < $ymax; $y++) {
 			$sb.delete(0, $sb.length());		// this is a really dumb way to have to phrase a request to just set the sb's internal count to zero.
 			final char[] $charsRow = $chars[$y];
 			final Palette[] $palesRow = $pales[$y];
 			for (int $x = $region.x; $x < $xmax; $x++) {
 				final Palette $palette = $palesRow[$x];
 				if (!$palette.equals($prevPalette)) $sb.append($palette.code());	//TODO:AHS:TERM: we could be doing substantially better delta'ing here.  also, the null selections are being allowed to go through here with an utterly odd concept of previous setting that's worse than useless for all practical purposes.
 				$sb.append(($charsRow[$x] == 0x0) ? ' ' : $charsRow[$x]);		//XXX:AHS:TERM: we're... kinda assuming that our application is going to be nice enough to give us one graphical character per char.  The alternative is to reset the cursor position with every single character we output, which... would certainly work, but would add at least 6 extra bytes of crap to write to the terminal per every single real character.
 				$prevPalette = $palette;
 			}
 			$term.print($sb.toString());
			$term.cursor().place($y, $region.x);
 		}
 	}
 }
