 package net.psj;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.Channels;
 
 import net.psj.Interface.MenuData;
 import net.psj.Interface.Overlay;
 import net.psj.Simulation.Air;
 import net.psj.Simulation.ParticleData;
 import net.psj.Simulation.ShaderData;
 import net.psj.Simulation.WallData;
 import net.psj.Walls.WallFan;
import net.psj.shader.Shader;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.MouseListener;
 import org.newdawn.slick.SlickException;
 
 
 public class PowderSimJ extends BasicGame implements MouseListener,KeyListener{
 	
 	static boolean hasGotNatives = false;
 	
 	public static final int width = 612;
 	public static final int height = 384;
 	public static final int cenX = width/2;
 	public static final int cenY = height/2;
 	public static final int menuSize = 40;
 	public static final int barSize = 17;
 	public static final int cell = 4;
 	
 	public static final int MAX_TEMP = 9000;
 	public static final int MIN_TEMP = 0;
 	
 	public static File appDir;
 	
 	/* Settings */
 	public static int AA = 0;
 	public static boolean VSync = false;
 	public static boolean Debug = true;
 	public static int targetFrames = 60;
 	
 	public static int mouseX;
 	public static int mouseY;
 	
 	public static int selectedl = 1;/*0x00 - 0xFF are particles */
 	public static int selectedr = 0;
 	
 	public static int wallStart = 4096; //Basically the element limit.
 	
 	int fanX,fanY;
 	
 	boolean isSettingFan = false;
 		
 	int keyTick = 5;
 		
 	public boolean isPaused = false;
 	
 	public boolean airHeat = false;
 	
 	public static GameContainer gc;
 	
 	public Air air = new Air();
 	public WallData wall = new WallData();
 	public static ParticleData ptypes = new ParticleData();
 	
 	public static int brushSize = 10;
 
 	public static String version = "0.1 Alpha";
 	
 	public PowderSimJ()
     {
         super("Powder Sim Java");
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException {
     	PowderSimJ.gc = gc;
     	RenderUtils.setAntiAliasing(true);
     	GL11.glDisable(GL11.GL_LIGHTING);
     	GL11.glShadeModel(GL11.GL_SMOOTH);
     	RenderUtils.init();
     	ShaderData.init();
     }
  
     public static void main(String[] args) throws SlickException
     {
     	System.out.println(getDirectory());
     	while(hasGotNatives==false)
     	{
     		
     	}
     	System.setProperty("org.lwjgl.librarypath",getDirectory() + "/natives/" + getOs() + "");
         AppGameContainer app = new AppGameContainer(new PowderSimJ());
         app.setDisplayMode(width+barSize, height+menuSize, false);
         app.setVSync(VSync);
         app.setMultiSample(AA);
         app.setVerbose(Debug);
         app.setTargetFrameRate(targetFrames);
         app.setShowFPS(false);
         app.start();
     }
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 		if(!isSettingFan)
 			air.drawAir();
 		wall.renderWalls();
 		ShaderData.blurH.activate();
 		ShaderData.blurV.activate();
 		ShaderData.fancy.activate();
 		ptypes.render();
 		ShaderData.blurH.deactivate();
 		ShaderData.blurV.deactivate();
 		ShaderData.fancy.deactivate();
 		
 		MenuData.draw();
 		
 		if(isSettingFan)
 			RenderUtils.drawLine(fanX,fanY,mouseX,mouseY, 1,1.0f,1.0f,1.0f);
 		else
 		{
 			int x1 = mouseX, y1 = mouseY;
 			x1 = x1-(PowderSimJ.brushSize/2);
 			y1 = y1-(PowderSimJ.brushSize/2);
 			RenderUtils.drawRectLine(x1, y1, x1+brushSize, y1+brushSize, 1.0f, 1.0f, 1.0f);
 		}
 		
 		Overlay.drawInfoBar();
 		Overlay.drawPixInfo();
 	}
 
 	@Override
 	public void update(GameContainer arg0, int arg1) throws SlickException 
 	{
 		if(!isPaused)
 		{
 			air.update_air();
 			air.make_kernel();
 			if(airHeat)
 				air.update_airh();
 			
 			ptypes.update();
 		}
 		
 		Input input = arg0.getInput();
 		mouseX = input.getMouseX();
 		mouseY = input.getMouseY();
 		if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
 			onMouseClick(arg0,0);
 		if(input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON))
 			onMouseClick(arg0,4);
 	}
 	
 	@Override
 	public void keyPressed(int key, char c)
 	{
 		if(key==Input.KEY_EQUALS)
 			for (int y=0; y<height/cell; y++)
 					for (int x=0; x<width/cell; x++)
 					{
 						Air.pv[y][x] = 0f;
 						Air.vy[y][x] = 0f;
 						Air.vx[y][x] = 0f;
 						air.hv[y][x] = 0f;
 					}
 		if(key==Input.KEY_SPACE)
 			isPaused = !isPaused;
 		
 		if(key==Input.KEY_LBRACKET)
 			brushSize-=20;
 		
 		if(key==Input.KEY_RBRACKET)
 			brushSize+=20;
 		
 		if(brushSize<1) brushSize = 1;
 	}
 	
 	@Override
 	public void mouseWheelMoved(int change) {
 		brushSize += change/100;
 		if(brushSize<1) brushSize = 1;
 	}
 	
 	@Override
 	public void mouseReleased(int button, int x, int y)
 	{
 		//TODO make more brush types;
 	}
 	
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount)
 	{
 		MenuData.click(button,x,y);
 		if(mouseY>0 && mouseY<height)
 		{
 			if(mouseX>0 && mouseX<width)
 			{
 				while(!(mouseY%cell==0))
 					mouseY--;
 				while(!(mouseX%cell==0))
 					mouseX--;
 				if(WallData.bmap[mouseY/cell][mouseX/cell] instanceof WallFan && gc.getInput().isKeyDown(Input.KEY_LSHIFT))
 				{
 					isSettingFan = !isSettingFan;
 					fanX = mouseX;
 					fanY = mouseY;
 					return;
 				}
 				else if(isSettingFan)
 				{
 					float nfvx = (mouseX-fanX)*0.055f;
 					float nfvy = (mouseY-fanY)*0.055f;
 					air.fvx[fanY/cell][fanX/cell] = nfvx;
 					air.fvy[fanY/cell][fanX/cell] = nfvy;
 					isSettingFan = false;
 					return;
 				}
 			}
 		}
 	}
 	
 	public static boolean isInPlayField(int x, int y)
 	{
 		if(mouseY>0 && mouseY<height)
 			if(mouseX>0 && mouseX<width)
 				return true;
 		
 		return false;
 	}
 	
 	public void onMouseClick(GameContainer arg0, int button)
 	{
 		if(isInPlayField(mouseX,mouseY))
 		{
 			if(button==0)
 			{
 				if(selectedl<wallStart)
 					ptypes.create_parts(mouseX, mouseY, selectedl);
 				else
 					wall.create_walls(mouseX/4, mouseY/4, selectedl);
 			}
 			else if(button==4)
 				ptypes.create_parts(mouseX, mouseY, selectedr);
 		}
 	}
 	
 	
     private static String getOs()
     {
         String s = System.getProperty("os.name").toLowerCase();
 
         if (s.contains("win"))
         {
             return "windows";
         }
 
         if (s.contains("mac"))
         {
             return "macosx";
         }
 
         if (s.contains("solaris"))
         {
             return "solaris";
         }
 
         if (s.contains("sunos"))
         {
             return "solaris";
         }
 
         if (s.contains("linux"))
         {
             return "linux";
         }
 
         if (s.contains("unix"))
         {
             return "linux";
         }
         else
         {
             return "linux";
         }
     }
     
     public static File getDirectory()
     {
         if (appDir == null)
         {
         	appDir = getAppDir("powdersimj");
         }
         
         File natives = new File(appDir, "natives/");
         if(!natives.exists())
         	natives.mkdir();
         File os = new File(natives, getOs());
         if(!os.exists())
         {
         	os.mkdir();
         }
         downloadFiles("http://dl.dropbox.com/u/20806998/PS/natives/" + getOs() + "/files.txt",os);
 
         return appDir;
     }
 
     public static File getAppDir(String par0Str)
     {
         String s = System.getProperty("user.home", ".");
         File file = null;
 
         if(getOs().equalsIgnoreCase("windows"))
         {
             String s1 = System.getenv("APPDATA");
 
             if (s1 != null)
             {
                 file = new File(s1, (new StringBuilder()).append(".").append(par0Str).append('/').toString());
             }
             else
             {
                 file = new File(s, (new StringBuilder()).append('.').append(par0Str).append('/').toString());
             }
         }
         else if (getOs().equalsIgnoreCase("macosx"))
         {
             file = new File(s, (new StringBuilder()).append("Library/Application Support/").append(par0Str).toString());
         }
         else if(getOs().equalsIgnoreCase("solaris"))
         {
             file = new File(s, (new StringBuilder()).append('.').append(par0Str).append('/').toString());
         }
         else if(getOs().equalsIgnoreCase("linux"))
         {}
         else
         {
         	file = new File(s, (new StringBuilder()).append(par0Str).append('/').toString());
         }
         
         if (!file.exists() && !file.mkdirs())
         {
             throw new RuntimeException((new StringBuilder()).append("The working directory could not be created: ").append(file).toString());
         }
         else
         {
             return file;
         }
     }
     
     public static void downloadFiles(String list, File outputDir)
     {
     	try
     	{
 	        URL url = new URL(list);
 	        URLConnection urlconnection = url.openConnection();
 	        urlconnection.setReadTimeout(5000);
 	        urlconnection.setDoOutput(true);
 	        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
 	        String s;
 	        while ((s = bufferedreader.readLine()) != null)
 	        {
 	            downloadFile(list.replace("files.txt", s),new File(outputDir, s));
 	        }
 	        bufferedreader.close();
 	        if(hasGotNatives==false)
 	    		hasGotNatives = true;
     	}
     	catch(Exception e){e.printStackTrace();}
     }
     
     public static void downloadFile(final String url, final File out)
     {
     	if(out.exists()) return;
     	try
     	{
     		URL url1 = new URL(url);
     		java.nio.channels.ReadableByteChannel readablebytechannel = Channels.newChannel(url1.openStream());
     		FileOutputStream fileoutputstream = new FileOutputStream(out);
     		fileoutputstream.getChannel().transferFrom(readablebytechannel, 0L, 0x1000000L);
     		fileoutputstream.close();
     	}
     	catch (Exception exception)
     	{
     		exception.printStackTrace();
     	}
     }
 }
