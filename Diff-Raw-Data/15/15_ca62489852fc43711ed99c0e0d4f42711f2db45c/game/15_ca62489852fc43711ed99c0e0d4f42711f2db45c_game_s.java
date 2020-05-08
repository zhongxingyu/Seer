 import java.applet.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.util.*;
 import java.io.*;
 import java.util.Scanner;
 import java.util.ArrayList;
 
 class posCalc
 {
 	int camOnAbsX, camOnAbsY, camSizeX, camSizeY;
 	int camY(int absX)
 	{
 		return 0;
 	}
 	int camX(int absY)
 	{
 		return 0;
 	}
 }
 class oBase
 {
 	Image img;
 	String imageName;
 
 	int posx,posy;
 	int vspeed=0, hspeed=0;
 	int buffy;
 	int width, length;
 
 	Boolean in_air = false;
 	Boolean gravity = false;
 	int jumpstr = 80;
 
 	Boolean tiled = false;
 	int depth = 0;
 	Rectangle mask;
 	public oBase(Image a, int x, int y,int d, game z)//without mask
 	{
 		posx=x;
 		posy=y;
 		depth = d;
 		img = a;
 		mask = new Rectangle(0,0,0,0);
 	}
 	public oBase(Image a, int x, int y,int d, game z, int maskX, int maskY)//with mask
 	{
 		posx=x;
 		posy=y;
 		depth = d;
 		img = a;
 	}
 	void move()
 	{
 		posx+=hspeed;
 		posy+=vspeed;
 		if(gravity == true && posy < buffy-jumpstr && vspeed < 0)
 			vspeed *=-1;
 		if(posy>500)
 		{
 			vspeed=0;
 			in_air=false;
 		}
 	}
 }
 class oList
 {
 	ArrayList<Image> images = new ArrayList<Image>();
 	ArrayList<oBase> objects = new ArrayList<oBase>();
 	int camera;
 	int maxX, maxY, cX, cY;
 	public oList(String imgs[], int cam, int screenSizeX, int screenSizeY, int cameraX, int cameraY, game z)
 	{
 		addImages(imgs,z);
 		camera = cam;
 		maxX = screenSizeX;
 		maxY = screenSizeY;
 		cX = cameraX;
 		cY = cameraY;
 	}
 	public oList(int cam, int screenSizeX, int screenSizeY, int cameraX, int cameraY, game z)
 	{
 		camera = cam;
 		maxX = screenSizeX;
 		maxY = screenSizeY;
 		cX = cameraX;
 		cY = cameraY;
 	}
 	void addImages(String imgs[], game z)
 	{
 		for(int i = 0; i < imgs.length; i++)
 		{
 				addImage(imgs[i],z);
 		}
 	}
 	void addImage(String img, game z)
 	{
 		images.add(z.getImage(z.getDocumentBase(),img));
 	}
 	void add(oBase x)
 	{
 		objects.add(x);
 	}
 	void add(int ImageIndex, int x, int y, int depth, game z)
 	{
 		objects.add(new oBase(images.get(ImageIndex),x,y,depth, z));
 	}
 	private void swap(int x, int y)
 	{
 		oBase temp = objects.get(x);
 		objects.set(x,objects.get(y));
 		objects.set(y,temp);
 	}
 
 	public void sortDepth()//simple bubble sort
 	{
 		boolean sorted;
 		int p = 1;
 		do
 		{
 			sorted = true;
 			for (int q = 0; q < objects.size()-p; q++)
 				if (objects.get(q).depth < objects.get(q+1).depth) //sort objects with least depth to last so they are drawn last
 				{
 					swap(q,q+1);
 					sorted = false;
 				}
 			p++;
 		}
 		while (!sorted);
 	}
 	void setTiled(int i)
 	{
 		objects.get(i).tiled=true;
 	}
 	void unsetTiled(int i)
 	{
 		objects.get(i).tiled=false;
 	}
 	void setGravity(int i)
 	{
 		objects.get(i).gravity=true;
 	}
 	void unsetGravity(int i)
 	{
 		objects.get(i).gravity=false;
 	}
 	void sethSpeed(int i, int x)
 	{
 		objects.get(i).hspeed=x;
 	}
 	void setvSpeed(int i, int x)
 	{
 		objects.get(i).vspeed=x;
 	}
 	void draw(Graphics g, game z)
 	{
 		for(int x = 0; x<objects.size();x++)
 		{
 			objects.get(x).move();
 			if(objects.get(x).tiled == true) //if background
 			{
 				g.drawImage(objects.get(x).img, 700-objects.get(camera).posx%700,  350-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 0-objects.get(camera).posx%700,  350-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 700-objects.get(camera).posx%700,  0-objects.get(camera).posy%350, z);
 				g.drawImage(objects.get(x).img, 0-objects.get(camera).posx%700,  0-objects.get(camera).posy%350, z);
 			}
 			else
 			{
 				g.drawImage(objects.get(x).img,	cX-(objects.get(camera).posx-objects.get(x).posx),	cY-(objects.get(camera).posy-objects.get(x).posy),z);
 			}
 		}
 	}
 }
 public class game extends Applet implements KeyListener
 {
 	//String[] x = {"backgrounds/01.jpg", "img/snoopy.gif", "img/barrel.gif"};
 	oList objectlist = new oList(1,700,350,300,180,this);
 
  	AudioClip soundFile1;
 	public void init()
 	{
 		objectlist.addImage("game/res/tiles/01.jpg",this);
 		objectlist.addImage("game/res/snoopy.gif",this);
 		objectlist.addImage("game/res/barrel.gif",this);
 
 		soundFile1 = getAudioClip(getDocumentBase(),"game/music/01.wav");
 		addKeyListener(this);
 		soundFile1.play();
 
 		objectlist.add(0,0,0,1,this);
		objectlist.add(1,500,500,0,this);
 		objectlist.add(2,500,500,0,this);
 		objectlist.setTiled(0);
 		//objectlist.get(2).gravity=true;
 	}
 	public void paint(Graphics g)
 	{
 		objectlist.sortDepth();
 		objectlist.draw(g,this);
 	}
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 	public void keyPressed(KeyEvent ke)
 	{
 		switch(ke.getKeyCode())
 		{
 			case KeyEvent.VK_DOWN:
 				objectlist.setvSpeed(objectlist.camera,10);
 				break;
 			case KeyEvent.VK_RIGHT:
 				//objects.get(camera).hspeed=10;
 				objectlist.sethSpeed(objectlist.camera,10);
 				break;
 			case KeyEvent.VK_LEFT:
 				objectlist.sethSpeed(objectlist.camera,-10);
 				//objects.get(camera).hspeed=-6;
 				break;
 			case KeyEvent.VK_UP:
 				objectlist.setvSpeed(objectlist.camera,-10);
 				/*if(objects.get(camera).in_air==false)
 				{
 					objects.get(camera).buffy=objects.get(camera).posy;
 					objects.get(camera).vspeed=-20;
 					objects.get(camera).in_air = true;
 				}*/
 				break;
 		}
 	}
 	public void keyTyped(KeyEvent ke) {}
 	public void keyReleased(KeyEvent ke)
 	{
 		switch(ke.getKeyCode())
 		{
 			case KeyEvent.VK_DOWN:
 				objectlist.setvSpeed(objectlist.camera,0);
 				break;
 			case KeyEvent.VK_RIGHT:
 				objectlist.sethSpeed(objectlist.camera,0);
 				//objects.get(camera).hspeed=0;
 				break;
 			case KeyEvent.VK_LEFT:
 				objectlist.sethSpeed(objectlist.camera,0);
 				//objects.get(camera).hspeed=0;
 				break;
 			case KeyEvent.VK_UP:
 				objectlist.setvSpeed(objectlist.camera,0);
 				break;
 		}
 	}
 	public boolean mouseDrag(Event e, int x, int y)
 	{
 		repaint();
 		return true;
 	}
 	public boolean mouseDown(Event e, int x, int y)
 	{
 	    repaint();
 		return true;
 	}
 	public boolean mouseUp(Event e, int x, int y)
 	{
 		repaint();
 		return true;
 	}
 }
