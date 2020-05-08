 package hexapixel.notifier;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 
 import shared.RoomManager;
 
 import hexapixel.cache.FontCache;
 
 public class NotifierDialog {
 
 	// TODO: extract these to options
 	// how long the the tray popup is displayed after fading in (in
 	// milliseconds)
 	private static final int DISPLAY_TIME = 4500;
 	// how long each tick is when fading in (in ms)
 	private static final int FADE_TIMER = 50;
 	// how long each tick is when fading out (in ms)
 	private static final int FADE_IN_STEP = 30;
 	// how many tick steps we use when fading out
 	private static final int FADE_OUT_STEP = 8;
 
 	// how high the alpha value is when we have finished fading in
 	private static final int FINAL_ALPHA = 225;
 
 	// contains list of all active popup shells
 	private static List<Shell> _activeShells = new ArrayList<Shell>();
 
 	private static Shell _shell;
 
 	//FIXME this does not pop up when the window is out of focus anymore. WHY
 	public static void window(final String title, final String message,
 			final NotificationType type, final Color textColor,
 			final Color titleColor, final Color borderColor,
 			final Color topGradient, final Color btmGradient) {
 		
 		final Display d = RoomManager.getMain().getDisplay();
 		
 		d.asyncExec(new Runnable() {
 			public void run() {
				_shell = new Shell(d.getActiveShell(), SWT.NO_TRIM | SWT.ON_TOP);
 				_shell.setLayout(new FillLayout());
 				_shell.setForeground(textColor);
 				_shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
 				_shell.addListener(SWT.Dispose, new Listener() {
 					@Override
 					public void handleEvent(Event event) {
 						_activeShells.remove(_shell);
 					}
 				});
 
 				final Composite inner = new Composite(_shell, SWT.NONE);
 
 				GridLayout gl = new GridLayout(2, false);
 				gl.marginLeft = 5;
 				gl.marginTop = 0;
 				gl.marginRight = 5;
 				gl.marginBottom = 5;
 
 				inner.setLayout(gl);
 				_shell.addListener(SWT.Resize, new Listener() {
 
 					@Override
 					public void handleEvent(Event e) {
 						try {
 							// get the size of the drawing area
 							Rectangle rect = _shell.getClientArea();
 							// create a new image with that size
 							Image newImage = new Image(d,
 									Math.max(1, rect.width), rect.height);
 							// create a GC object we can use to draw with
 							GC gc = new GC(newImage);
 
 							// fill background
 							gc.setForeground(topGradient);
 							gc.setBackground(btmGradient);
 							gc.fillGradientRectangle(rect.x, rect.y,
 									rect.width, rect.height, true);
 
 							// draw shell edge
 							gc.setLineWidth(2);
 							gc.setForeground(borderColor);
 							gc.drawRectangle(rect.x + 1, rect.y + 1,
 									rect.width - 2, rect.height - 2);
 							// remember to dipose the GC object!
 							gc.dispose();
 
 							// now set the background image on the shell
 							_shell.setBackgroundImage(newImage);
 
 						} catch (Exception err) {
 							err.printStackTrace();
 						}
 					}
 				});
 
 				GC gc = new GC(_shell);
 
 				String lines[] = message.split("\n");
 				Point longest = null;
 				int typicalHeight = gc.stringExtent("X").y;
 
 				for (String line : lines) {
 					Point extent = gc.stringExtent(line);
 					if (longest == null) {
 						longest = extent;
 						continue;
 					}
 
 					if (extent.x > longest.x) {
 						longest = extent;
 					}
 				}
 				gc.dispose();
 
 				int minHeight = typicalHeight * lines.length;
 
 				CLabel imgLabel = new CLabel(inner, SWT.NONE);
 				imgLabel.setLayoutData(new GridData(
 						GridData.VERTICAL_ALIGN_BEGINNING
 								| GridData.HORIZONTAL_ALIGN_BEGINNING));
 				
 				if(type!=null){
 					imgLabel.setImage(type.getImage());
 				}
 
 				CLabel titleLabel = new CLabel(inner, SWT.NONE);
 				titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
 						| GridData.VERTICAL_ALIGN_CENTER));
 				titleLabel.setText(title);
 				titleLabel.setForeground(titleColor);
 				Font f = titleLabel.getFont();
 				FontData fd = f.getFontData()[0];
 				fd.setStyle(SWT.BOLD);
 				fd.height = 11;
 				titleLabel.setFont(FontCache.getFont(fd));
 
 				Label text = new Label(inner, SWT.WRAP);
 				Font tf = text.getFont();
 				FontData tfd = tf.getFontData()[0];
 				tfd.setStyle(SWT.BOLD);
 				tfd.height = 8;
 				text.setFont(FontCache.getFont(tfd));
 				GridData gd = new GridData(GridData.FILL_BOTH);
 				gd.horizontalSpan = 2;
 				text.setLayoutData(gd);
 				text.setForeground(textColor);
 				text.setText(message);
 
 				minHeight = 100;
 
 				_shell.setSize(350, minHeight);
 
 				if (d.getActiveShell() == null
 						|| d.getActiveShell().getMonitor() == null) {
 					return;
 				}
 
 				Rectangle clientArea = d.getActiveShell()
 						.getMonitor().getClientArea();
 
 				int startX = clientArea.x + clientArea.width - 352;
 				int startY = clientArea.y + clientArea.height - 102;
 
 				// move other shells up
 				if (!_activeShells.isEmpty()) {
 					List<Shell> modifiable = new ArrayList<Shell>(_activeShells);
 					Collections.reverse(modifiable);
 					for (Shell shell : modifiable) {
 						Point curLoc = shell.getLocation();
 						shell.setLocation(curLoc.x, curLoc.y - 100);
 						if (curLoc.y - 100 < 0) {
 							_activeShells.remove(shell);
 							shell.dispose();
 						}
 					}
 				}
 
 				_shell.setLocation(startX, startY);
 				_shell.setAlpha(0);
 				_shell.setVisible(true);
 
 				_activeShells.add(_shell);
 
 				fadeIn(_shell);
 			}
 		});
 	}
 
 	private static void fadeIn(final Shell _shell) {
 		Runnable run = new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					if (_shell == null || _shell.isDisposed()) {
 						return;
 					}
 
 					int cur = _shell.getAlpha();
 					cur += FADE_IN_STEP;
 
 					if (cur > FINAL_ALPHA) {
 						_shell.setAlpha(FINAL_ALPHA);
 						startTimer(_shell);
 						return;
 					}
 
 					_shell.setAlpha(cur);
 					RoomManager.getMain().getDisplay().timerExec(FADE_TIMER, this);
 				} catch (Exception err) {
 					err.printStackTrace();
 				}
 			}
 
 		};
 		RoomManager.getMain().getDisplay().timerExec(FADE_TIMER, run);
 	}
 
 	private static void startTimer(final Shell _shell) {
 		Runnable run = new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					if (_shell == null || _shell.isDisposed()) {
 						return;
 					}
 
 					fadeOut(_shell);
 				} catch (Exception err) {
 					err.printStackTrace();
 				}
 			}
 
 		};
 		RoomManager.getMain().getDisplay().timerExec(DISPLAY_TIME, run);
 
 	}
 
 	private static void fadeOut(final Shell _shell) {
 		final Runnable run = new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					if (_shell == null || _shell.isDisposed()) {
 						return;
 					}
 
 					int cur = _shell.getAlpha();
 					cur -= FADE_OUT_STEP;
 
 					if (cur <= 0) {
 						_shell.setAlpha(0);
 						_shell.dispose();
 						_activeShells.remove(_shell);
 						return;
 					}
 
 					_shell.setAlpha(cur);
 
 					RoomManager.getMain().getDisplay().timerExec(FADE_TIMER, this);
 
 				} catch (Exception err) {
 					err.printStackTrace();
 				}
 			}
 
 		};
 		RoomManager.getMain().getDisplay().timerExec(FADE_TIMER, run);
 
 	}
 
 }
