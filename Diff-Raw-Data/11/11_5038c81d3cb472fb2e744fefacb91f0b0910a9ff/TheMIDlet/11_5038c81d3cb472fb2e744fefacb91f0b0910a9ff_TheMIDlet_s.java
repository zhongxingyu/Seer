 
 package kaezadraw;
 
 import javax.microedition.midlet.*;
 import javax.microedition.lcdui.*;
 
 public class TheMIDlet extends MIDlet implements CommandListener {
 
 	public static Display display;
 	public static DrawCanvas canvas;
 
 	private static final Command SAVE_FILE_COMMAND = new Command("Save File", Command.SCREEN, 2);
 	private static final Command LOAD_FILE_COMMAND = new Command("Load File", Command.SCREEN, 2);
 	private static final Command PALETTE_COMMAND = new Command("Palette", Command.SCREEN, 3);
 	private static final Command CLEAR_COMMAND = new Command("Clear", Command.SCREEN, 3);
 
 	public static final void show(Displayable d) {
		TheMIDlet.display.setCurrent(d);
 	}
 
 	public void startApp() {
 		TheMIDlet.canvas = new DrawCanvas();
 		TheMIDlet.canvas.addCommand(new Command("Quit", Command.EXIT, 1));
 		TheMIDlet.canvas.addCommand(SAVE_FILE_COMMAND);
 		TheMIDlet.canvas.addCommand(LOAD_FILE_COMMAND);
 		TheMIDlet.canvas.addCommand(PALETTE_COMMAND);
 		TheMIDlet.canvas.addCommand(CLEAR_COMMAND);
 		TheMIDlet.canvas.setCommandListener(this);
 		TheMIDlet.display = Display.getDisplay(this);
 		display.setCurrent(TheMIDlet.canvas);
 	}
 
 	public void commandAction(Command c, Displayable d) {
 		if (c == SAVE_FILE_COMMAND) {
 			FileDialog fd = new FileDialog(true, TheMIDlet.canvas);
			TheMIDlet.display.setCurrent(fd);
 		}
 		else if (c == LOAD_FILE_COMMAND) {
 			FileDialog fd = new FileDialog(false, TheMIDlet.canvas);
			TheMIDlet.display.setCurrent(fd);
 		}
 		else if (c == PALETTE_COMMAND) {
 			PaletteForm pf = new PaletteForm(TheMIDlet.canvas);
			TheMIDlet.display.setCurrent(pf);
 		}
 		else if (c == CLEAR_COMMAND) {
 			
 		}
 		else if (c.getCommandType() == Command.EXIT) {
 			destroyApp(false);
 			notifyDestroyed();
 		}
 	}
 
 	public void pauseApp() {
 		display.setCurrent(TheMIDlet.canvas = null);
 	}
 
 	public void destroyApp(boolean unconditional) {
 		display.setCurrent(TheMIDlet.canvas = null);
 	}
 
 }
