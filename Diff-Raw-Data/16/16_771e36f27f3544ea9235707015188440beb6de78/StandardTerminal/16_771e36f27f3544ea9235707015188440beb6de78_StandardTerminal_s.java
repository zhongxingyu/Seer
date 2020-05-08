package us.exultant.grid.terminal;
 
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 
 public class StandardTerminal implements Terminal {
 	public static StandardTerminal get() {
 		return SingletonHolder.INSTANCE;
 	}
 	private static class SingletonHolder {
 		public static final StandardTerminal INSTANCE = new StandardTerminal();
 	}
 	private StandardTerminal() {
 		$console = System.console();
 		$cursor = new StdCursor();
 		$palette = new StdPalette();
 		
 		setEchoMode(false);
 		// Add a shutdown hook to restore console's echo state when we exit.
 		Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { setEchoMode(true); } });
 		
 		try {
 			//$restoreStty = IOForge.readString(Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty -g" }).getInputStream());
 			Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty raw iutf8 icrnl opost isig </dev/tty" }).waitFor();
 		} catch (InterruptedException $e) {
 			throw new Error("failed to set console to raw mode", $e);
 		} catch (IOException $e) {
 			throw new Error("failed to set console to raw mode", $e);
 		}
 		
 		clear();
 	}
 	/** deal with the crappitude of taking control of echos.  I'm not sure i expect this to work on jvm's other than sun's, since it relies on reflecting to a private method. */
 	protected void setEchoMode(boolean $on) {
 		try {
 			Method $mecho = Console.class.getDeclaredMethod("echo", boolean.class);
 			$mecho.setAccessible(true);
 			$mecho.invoke(null, $on);
 		} catch (Exception $e) {
 			throw new Error("Cannot set the echo mode of the Console -- is this a Sun JVM?", $e);
 		}
 	}
 	
 	private final Console	$console;
 	private final Cursor	$cursor;
 	private final Palette	$palette;
 	
 	public Cursor cursor() {
 		return $cursor;
 	}
 	
 	public Palette palette() {
 		return $palette;
 	}
 	
 	public void clear() {
 		$console.printf(
 				TermCodes.CLEAR_SCREEN+	// duh
 				TermCodes.CSI+"f"	// also reset cursor to 1,1 (some terminals do this with just the clear screen code, so we shoot for consistency here) (we don't use the normal function for cursor positioning because we can skip some characters by taking advantage of the "1;1" default args).
 		);
 	}
 	
 	
 
 	public class StdCursor implements Cursor {
 		public void place(int $x, int $y) {
 			$console.printf(TermCodes.CSI+$y+";"+$x+"f");
 		}
 		
 		public void shiftUp(int $n) {
 			$console.printf(TermCodes.CSI+$n+"A");
 		}
 		
 		public void shiftDown(int $n) {
 			$console.printf(TermCodes.CSI+$n+"B");
 		}
 		
 		public void shiftLeft(int $n) {
 			$console.printf(TermCodes.CSI+$n+"D");
 		}
 		
 		public void shiftRight(int $n) {
 			$console.printf(TermCodes.CSI+$n+"C");
 		}
 		
 		public void lineNext(int $n) {
 			// xterm supports this, but konsole doesn't.  i suppose i could check for compliance at startup and emulate it with asking current location and then doing a place, but... i really intend to avoid that sort of thing as strongly as possible.
 			$console.printf(TermCodes.CSI+$n+"E");
 		}
 		
 		public void linePrev(int $n) {
 			// xterm supports this, but konsole doesn't.  i suppose i could check for compliance at startup and emulate it with asking current location and then doing a place, but... i really intend to avoid that sort of thing as strongly as possible.
 			$console.printf(TermCodes.CSI+$n+"F");
 		}
 	}
 	
 	
 	
 	public class StdPalette implements Palette {
 		public void bold(boolean $n) {
 			//TODO
 			
 		}
 		
 		public void blink(boolean $n) {
 			//TODO
 			
 		}
 		
 		public void underline(boolean $n) {
 			//TODO
 			
 		}
 		
 		public void background(Color $c) {
 			//TODO
 			
 		}
 		
 		public void foreground(Color $c) {
 			//TODO
 			
 		}
 	}
 
 
 
 	public int getWidth() {
 		try {
 			return new Scanner(Runtime.getRuntime().exec(new String[] { "tput cols" }).getInputStream()).nextInt();
 		} catch (IOException $e) {
 			return -1;
 		}
 	}
 
 	public int getHeight() {
 		try {
 			return new Scanner(Runtime.getRuntime().exec(new String[] { "tput lines" }).getInputStream()).nextInt();
 		} catch (IOException $e) {
 			return -1;
 		}
 	}
 }
