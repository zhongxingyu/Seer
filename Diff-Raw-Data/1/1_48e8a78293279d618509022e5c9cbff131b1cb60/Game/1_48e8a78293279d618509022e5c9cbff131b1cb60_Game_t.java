 package com.jpii.navalbattle.game;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.util.*;
 
 import com.jpii.dagen.*;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.renderer.*;
 
 public class Game implements Runnable
 {
 	private Grid grid;
 	private ArrayList<ChunkRenderer> chunks;
 	private Engine eng;
 	private EntityRenderer eRender;
 	private BufferedImage map;
 	private BufferedImage buffer;
 	private BufferedImage clouds;
 	private BufferedImage shadow;
 	//private int zoom;
 	private CloudRelator cr;
 	private int msax, msay;
 	private OmniMap omniMap;
 	public Game()
 	{
 		msax = Constants.WINDOW_WIDTH*2;
 		msay = Constants.WINDOW_HEIGHT*2;
 		grid = new Grid();
 		eRender = new EntityRenderer(grid);
 		chunks = new ArrayList<ChunkRenderer>();
 		map = new BufferedImage(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT,
 				BufferedImage.TYPE_INT_RGB);
 		buffer = new BufferedImage(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT,
 				BufferedImage.TYPE_INT_RGB);
 		if (RenderConstants.OPT_CLOUDS_ON)
 			clouds = new BufferedImage(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT,BufferedImage.TYPE_INT_ARGB);
 		eng = new Engine(Constants.WINDOW_WIDTH*4,
 				Constants.WINDOW_HEIGHT*4);
 		shadow = Helper.genInnerShadow(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT);
 		eng.setSmoothFactor(5);
 		eng.generate(Constants.MAIN_SEED,RenderConstants.GEN_TERRAIN_ROUGHNESS);
 		
 		omniMap = new OmniMap(eng,100,100);
 		omniMap.px = Constants.WINDOW_WIDTH - 120;
 		omniMap.py = 5;
 		
 		repaint(RepaintType.REPAINT_OMNIMAP);
 		
 		if (RenderConstants.OPT_CLOUDS_ON)
 			cr = new CloudRelator();
 		
 		for (int x = 0; x < Constants.WINDOW_WIDTH / Constants.CHUNK_SIZE; x++)
 		{
 			for (int z = 0; z < Constants.WINDOW_HEIGHT / Constants.CHUNK_SIZE; z++)
 			{
 				ChunkRenderer cr = new ChunkRenderer(eng,Constants.MAIN_SEED,x,z,
 						Constants.CHUNK_SIZE,Constants.CHUNK_SIZE,
 						RenderConstants.GEN_TERRAIN_ROUGHNESS);
 				//cr.setState(ChunkState.STATE_GENERATE);
 				//cr.run();
 				cr.setState(ChunkState.STATE_RENDER);
 				cr.run();
 				chunks.add(cr);
 			}
 		}
 		repaint(RepaintType.REPAINT_INDV_ENTITIES);
 	}
 	public Grid getGrid()
 	{
 		return grid;
 	}
 	public void setZoom(int level)
 	{
 	//	zoom = level;
 		repaint(RepaintType.REPAINT_CHUNKS);
 		repaint(RepaintType.REPAINT_CLOUDS);
 	}
 	public void update()
 	{
 		if (!RenderConstants.OPT_CLOUDS_ON)
 			return;
 		cr.run();
 	}
 	public void mouseMoved(MouseEvent me)
 	{
 		if (RenderConstants.OPT_CLOUDS_ON)
 			cr.updateMouse(me.getX(), me.getY());
 		omniMap.mouse(me);
 		if (!omniMap.entireWorldMode)
 			omniMap.update();
 	}
 	public void mouseClick(MouseEvent me)
 	{
 		omniMap.mouseClick(me);
 		omniMap.update();
 	}
 	public void run()
 	{
 		omniMap.msax = msax;
 		omniMap.msay = msay;
 		omniMap.update();
 		repaint(RepaintType.REPAINT_CHUNKS);
 		repaint(RepaintType.REPAINT_MAP);
 		repaint(RepaintType.REPAINT_INDV_ENTITIES);
 		
 		
 		repaint(RepaintType.REPAINT_BUFFERS);
 	}
 	int lastmx = -1;
 	int lastmy = -1;
 	public int FPS = 0;
 	public void mouseDrag(MouseEvent me)
 	{
 		if (msax > -200 && msay > -200 && msax < (Constants.WINDOW_WIDTH*4)+200 && msay < (Constants.WINDOW_HEIGHT*4)+200)
 		{
 			if (!RenderConstants.OPT_INVERSE_MOUSE)
 			{
 				msax += (me.getX() - (Constants.WINDOW_WIDTH/2))/8;
 				msay += (me.getY() - (Constants.WINDOW_HEIGHT/2))/8;
 			}
 			else
 			{
 				msax -= (me.getX() - (Constants.WINDOW_WIDTH/2))/8;
 				msay -= (me.getY() - (Constants.WINDOW_HEIGHT/2))/8;
 			}
 		}
 		else
 		{
 			if(msax <= -200)
 				msax = -198;
 			if (msay <= -200)
 				msay = -198;
 			if (msax >= (Constants.WINDOW_WIDTH*4)+100)
 				msax = (Constants.WINDOW_WIDTH*4)+98;
 			if (msay >= (Constants.WINDOW_HEIGHT*4)+100)
 				msay = (Constants.WINDOW_HEIGHT*4)+98;
 		}
 		run();
 	}
 	public void repaint(RepaintType type)
 	{
 		if (type == RepaintType.REPAINT_BUFFERS)
 		{
 			buffer = new BufferedImage(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT,
 					BufferedImage.TYPE_INT_RGB);
 			Graphics g = buffer.getGraphics();
 			g.drawImage(map,0,0,null);
 			g.drawImage(eRender.getBuffer(),0,0,null);
 			//g.drawImage(grid.getFeasibleGrid(),0,0,null);
 			g.drawImage(clouds,0,0,null);
 			g.drawImage(shadow,0,0,null);
 			g.drawImage(omniMap.getBuffer(), omniMap.px,omniMap.py,null);
 			g.setColor(Color.red);
 			g.drawString("X = " + msax + " Y = " + msay, 100,100);
 			g.drawString("FPS: " + FPS, 100, 124);
 		}
 		if (type == RepaintType.REPAINT_CHUNKS)
 		{
 			for (int v = 0; v < chunks.size(); v++)
 			{
 				ChunkRenderer cr = chunks.get(v);
 				cr.setLocation(msax,msay);
 				cr.setState(ChunkState.STATE_RENDER);
 				cr.run();
 			}
 		}
 		if (type == RepaintType.REPAINT_MAP)
 		{
 			Graphics g = map.getGraphics();
 			g.setColor(Color.black);
 			g.fillRect(0,0,Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT);
 			for (int v = 0; v < chunks.size(); v++)
 			{
 				ChunkRenderer cr = chunks.get(v);
 				g.drawImage(cr.getChunkBuffer(), cr.getX()*Constants.CHUNK_SIZE,cr.getZ()*Constants.CHUNK_SIZE,null);
 				//g.drawImage(cr.getChunkBuffer(),0,0,null);
 			}
 		}
 		if (type == RepaintType.REPAINT_INDV_ENTITIES)
 		{
 			eRender.render(this);
 		}
 		if (type == RepaintType.REPAINT_CLOUDS)
 		{
 			if (!RenderConstants.OPT_CLOUDS_ON)
 				return;
 			clouds = cr.buffer;
 		}
 		if (type == RepaintType.REPAINT_OMNIMAP)
 		{
 			omniMap.update();
 		}
 	}
 	public BufferedImage getBuffer()
 	{
 		return buffer;
 	}
     public Point getMouseSet()
     {
     	return new Point(msax,msay);
     }
 }
