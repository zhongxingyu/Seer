 package us.mbilker.minecraftportable;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.applet.Applet;
 import java.applet.AppletStub;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.VolatileImage;
 import java.util.HashMap;
 import java.util.Map;
 import javax.imageio.ImageIO;
 
 public class Launcher extends Applet
   implements Runnable, AppletStub, MouseListener
 {
   private static final long serialVersionUID = 1L;
   public Map<String, String> customParameters = new HashMap<String, String>();
   private GameUpdater gameUpdater;
   private boolean gameUpdaterStarted = false;
   private Applet applet;
   private Image bgImage;
   private boolean active = false;
   private int context = 0;
   private boolean hasMouseListener = false;
   private VolatileImage img;
 
   public boolean isActive()
   {
     if (context == 0) {
       context = -1;
       try {
         if (getAppletContext() != null) context = 1; 
       }
       catch (Exception localException) {  }
 
     }
     if (context == -1) return active;
     return super.isActive();
   }
   
   public static void setMinecraftDirectory(ClassLoader loader, File directory) {
 		try {
 			Class<?> clazz = loader.loadClass("net.minecraft.client.Minecraft");
 			Field[] fields = clazz.getDeclaredFields();
 
 			int fieldCount = 0;
 			Field mineDirField = null;
 			for (Field field : fields) {
 				if (field.getType() == File.class) {
 					int mods = field.getModifiers();
 					if (Modifier.isStatic(mods) && Modifier.isPrivate(mods)) {
 						mineDirField = field;
 						fieldCount++;
 					}
 				}
 			}
 			if (fieldCount != 1) { return; }
 
 			mineDirField.setAccessible(true);
 			mineDirField.set(null, directory);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
	
	public void run() {
		
	}
 
   public void init(String paramString1, String paramString2, String paramString3, String paramString4)
   {
     try {
       bgImage = ImageIO.read(LoginForm.class.getResource("dirt.png")).getScaledInstance(32, 32, 16);
     } catch (IOException localIOException) {
       localIOException.printStackTrace();
     }
 
     customParameters.put("username", paramString1);
     customParameters.put("sessionid", paramString4);
 
     gameUpdater = new GameUpdater(paramString2, "minecraft.jar?user=" + paramString1 + "&ticket=" + paramString3, customParameters.containsKey("noupdate"));
   }
 
   public boolean canPlayOffline() {
     return gameUpdater.canPlayOffline();
   }
 
   public void init()
   {
     if (applet != null) {
       applet.init();
       return;
     }
     init(getParameter("userName"), getParameter("latestVersion"), getParameter("downloadTicket"), getParameter("sessionId"));
   }
 
   public void start()
   {
     if (applet != null) {
       applet.start();
       return;
     }
     if (gameUpdaterStarted) return;
 
     Thread localObject = new Thread()
     {
       public void run() {
         gameUpdater.run();
         try {
           if (!gameUpdater.fatalError)
             replace(gameUpdater.createApplet());
         }
         catch (ClassNotFoundException localClassNotFoundException)
         {
           localClassNotFoundException.printStackTrace();
         } catch (InstantiationException localInstantiationException) {
           localInstantiationException.printStackTrace();
         } catch (IllegalAccessException localIllegalAccessException) {
           localIllegalAccessException.printStackTrace();
         }
       }
     };
     localObject.setDaemon(true);
     localObject.start();
 
     localObject = new Thread()
     {
       public void run() {
         while (applet == null) {
           repaint();
           try {
             Thread.sleep(10L);
           } catch (InterruptedException localInterruptedException) {
             localInterruptedException.printStackTrace();
           }
         }
       }
     };
     localObject.setDaemon(true);
     localObject.start();
 
     gameUpdaterStarted = true;
   }
 
   public void stop()
   {
     if (applet != null) {
       active = false;
       applet.stop();
     }
   }
 
   public void destroy()
   {
     if (applet != null)
       applet.destroy();
   }
 
   public void replace(Applet paramApplet)
   {
     applet = paramApplet;
     paramApplet.setStub(this);
     paramApplet.setSize(getWidth(), getHeight());
 
     setLayout(new BorderLayout());
     add(paramApplet, "Center");
 
     paramApplet.init();
     active = true;
     paramApplet.start();
     validate();
   }
 
   public void update(Graphics paramGraphics)
   {
     paint(paramGraphics);
   }
 
   public void paint(Graphics paramGraphics)
   {
     if (applet != null) return;
 
     int i = getWidth() / 2;
     int j = getHeight() / 2;
     if ((img == null) || (img.getWidth() != i) || (img.getHeight() != j)) {
       img = createVolatileImage(i, j);
     }
 
     Graphics localGraphics = img.getGraphics();
     for (int k = 0; k <= i / 32; k++)
       for (int m = 0; m <= j / 32; m++)
         localGraphics.drawImage(bgImage, k * 32, m * 32, null);
     String str;
     FontMetrics localFontMetrics;
     if (gameUpdater.pauseAskUpdate) {
       if (!hasMouseListener) {
         hasMouseListener = true;
         addMouseListener(this);
       }
       localGraphics.setColor(Color.LIGHT_GRAY);
       str = "New update available";
       localGraphics.setFont(new Font(null, 1, 20));
       localFontMetrics = localGraphics.getFontMetrics();
       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 - localFontMetrics.getHeight() * 2);
 
       localGraphics.setFont(new Font(null, 0, 12));
       localFontMetrics = localGraphics.getFontMetrics();
 
       localGraphics.fill3DRect(i / 2 - 56 - 8, j / 2, 56, 20, true);
       localGraphics.fill3DRect(i / 2 + 8, j / 2, 56, 20, true);
 
       str = "Would you like to update?";
       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 - 8);
 
       localGraphics.setColor(Color.BLACK);
       str = "Yes";
       localGraphics.drawString(str, i / 2 - 56 - 8 - localFontMetrics.stringWidth(str) / 2 + 28, j / 2 + 14);
       str = "Not now";
       localGraphics.drawString(str, i / 2 + 8 - localFontMetrics.stringWidth(str) / 2 + 28, j / 2 + 14);
     }
     else
     {
       localGraphics.setColor(Color.LIGHT_GRAY);
 
       str = "Updating Minecraft";
       if (gameUpdater.fatalError) {
         str = "Failed to launch";
       }
 
       localGraphics.setFont(new Font(null, 1, 20));
       localFontMetrics = localGraphics.getFontMetrics();
       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 - localFontMetrics.getHeight() * 2);
 
       localGraphics.setFont(new Font(null, 0, 12));
       localFontMetrics = localGraphics.getFontMetrics();
       str = gameUpdater.getDescriptionForState();
       if (gameUpdater.fatalError) {
         str = gameUpdater.fatalErrorDescription;
       }
 
       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 + localFontMetrics.getHeight() * 1);
       str = gameUpdater.subtaskMessage;
       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 + localFontMetrics.getHeight() * 2);
 
       if (!gameUpdater.fatalError) {
         localGraphics.setColor(Color.black);
         localGraphics.fillRect(64, j - 64, i - 128 + 1, 5);
         localGraphics.setColor(new Color(32768));
         localGraphics.fillRect(64, j - 64, gameUpdater.percentage * (i - 128) / 100, 4);
         localGraphics.setColor(new Color(2138144));
         localGraphics.fillRect(65, j - 64 + 1, gameUpdater.percentage * (i - 128) / 100 - 2, 1);
       }
     }
 
     localGraphics.dispose();
 
     paramGraphics.drawImage(img, 0, 0, i * 2, j * 2, null);
   }
 
   public void run()
   {
   }
 
   public String getParameter(String paramString) {
     String str = (String)customParameters.get(paramString);
     if (str != null) return str; try
     {
       return super.getParameter(paramString);
     } catch (Exception localException) {
       customParameters.put(paramString, null);
     }return null;
   }
 
   public void appletResize(int paramInt1, int paramInt2)
   {
   }
 
   public URL getDocumentBase()
   {
     try {
       return new URL("http://www.minecraft.net/game/");
     } catch (MalformedURLException localMalformedURLException) {
       localMalformedURLException.printStackTrace();
     }
     return null;
   }
 
   public void mouseClicked(MouseEvent paramMouseEvent) {
   }
 
   public void mouseEntered(MouseEvent paramMouseEvent) {
   }
 
   public void mouseExited(MouseEvent paramMouseEvent) {
   }
 
   public void mousePressed(MouseEvent paramMouseEvent) {
     int i = paramMouseEvent.getX() / 2;
     int j = paramMouseEvent.getY() / 2;
     int k = getWidth() / 2;
     int m = getHeight() / 2;
 
     if (contains(i, j, k / 2 - 56 - 8, m / 2, 56, 20)) {
       removeMouseListener(this);
       gameUpdater.shouldUpdate = true;
       gameUpdater.pauseAskUpdate = false;
       hasMouseListener = false;
     }
     if (contains(i, j, k / 2 + 8, m / 2, 56, 20)) {
       removeMouseListener(this);
       gameUpdater.shouldUpdate = false;
       gameUpdater.pauseAskUpdate = false;
       hasMouseListener = false;
     }
   }
 
   private boolean contains(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
     return (paramInt1 >= paramInt3) && (paramInt2 >= paramInt4) && (paramInt1 < paramInt3 + paramInt5) && (paramInt2 < paramInt4 + paramInt6);
   }
 
   public void mouseReleased(MouseEvent paramMouseEvent)
   {
   }
 }
