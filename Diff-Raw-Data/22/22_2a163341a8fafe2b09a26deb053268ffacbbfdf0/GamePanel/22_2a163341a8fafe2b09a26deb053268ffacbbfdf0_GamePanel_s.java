 package javagui;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 import java.util.LinkedList;
 import java.awt.Color;
 
 public class GamePanel extends JPanel 
 {
 	private final int MAXMESSAGES = 5;
 	private int wwidth= 1024;
 	private int wheight= 768;
 	private int nwidth = 100;//width of the names Panel;
 	private int mheight = 150;//height of the Messages Panel;
 	private int width = wwidth-nwidth;
 	private int height = wheight-mheight;
 	private int vwidth = 1;
 	private int vheight = 1;
 	private Color bgcolor=new Color(0,0,0);
 	private Color [][] pixmatrix;
 	private LinkedList<Player> players;
 	private String[] messages;
 	public void autoresize()
 	{
 		Dimension d = this.getSize();
 		wwidth=d.width;
 		wheight=d.height;
 		width = wwidth-nwidth;
 		height = wheight-mheight;
 		if ((width/vwidth) > (height/vheight))
 		{
 			width=vwidth*(height/vheight);
 		}
 		else
 		{
 			height=vheight*(width/vwidth);
 		}
 		FLUSH();
 	}
 	public void doLayout()
 	{
 		super.doLayout();
 		this.autoresize();
 	}
 	private Color convertColor(String hexcolor)
 	{
 		try
 		{
 			int r = Integer.parseInt(hexcolor.substring(0, 2), 16);
 			int g = Integer.parseInt(hexcolor.substring(2, 4), 16);
 			int b = Integer.parseInt(hexcolor.substring(4, 6), 16);
 			return new Color(r,g,b);
 		}
 		catch (Exception e)
 		{
 			System.err.println("Der idiot kann keine farben... wir machen schwarz.,");
 			return new Color(0,0,0);
 		}
 	}
 	public void RESET(boolean field, boolean players, boolean colors, boolean points, boolean messages)
 	{
 		if (field)
 		{
 			pixmatrix = new Color[vwidth][vheight];
 		}
 		if (points)
 		{
 			for (int i=0;i<this.players.size();i++)
 				this.players.get(i).points=0;
 		}
 		if (players)
 		{
 			this.players.clear();
 		}
 		if (messages)
 		{
 			this.messages = new String[MAXMESSAGES];
 		}
 	}
 	public void AUTH(String s)
 	{
 		MESSAGE(s);
 	}
 	public void SIZE(int w, int h)
 	{
 		vwidth=w;
 		vheight=h;
 		pixmatrix = new Color[w][h];
		repaint();
 	}
 	public void COLOR(int player, String color)
 	{
 		for (int k=0;k<players.size();k++)
 			if (players.get(k).ID==player)
 			{
 				players.get(k).color=convertColor(color);
 				return;
 			}
 		Player p=new Player(player, "");
 		p.color=convertColor(color);
 		players.add(p);
 	}
 	public void PLAYER(int n, String name)
 	{
 		for (int i=0;i<players.size();i++)
 		{
 			if (players.get(i).ID==n)
 			{
 				players.get(i).name=name;
 				return;
 			}
 		}
 		players.add(new Player(n, name));
 	}
 	public void SET(int row, int column, int color)
 	{
 		if (color==0)
 		{
 			pixmatrix[column][row]=null;
 			return;
 		}
 		for(int i=0;i<players.size();i++)
 			if (players.get(i).ID==color)
 			{
 				pixmatrix[column][row]=players.get(i).color;
 				break;
 			}
 	}
 	public void MESSAGE(String txt)
 	{
 		for(int i=messages.length-1;i>0;i--)
 			messages[i]=messages[i-1];
 		messages[0]=txt;
 	}
 	public void POINTS(int n, int p)
 	{
 		for (int k=0;k<players.size();k++)
 			if (players.get(k).ID==n)
 			{
 				players.get(k).points=p;
 				break;
 			}
 	}
 	public void FLUSH()
 	{
 		this.repaint();
 	}
 	public GamePanel()
 	{
 		this.setSize(width,height);
 		pixmatrix = new Color [vwidth] [vheight];
 		players=new LinkedList<Player>();
 		messages=new String[MAXMESSAGES];
 	}
 	public void paint(Graphics g)
 	{
 		g.setColor(bgcolor);
 		g.fillRect(0, 0, wwidth, wheight);
		for (int x=0;x<pixmatrix.length;x++)
			for (int y=0;y<pixmatrix.length;y++)
 			{
 				if (pixmatrix[x][y]!=null)
 				{
 					g.setColor(pixmatrix[x][y]);
 					g.fillRect(x*(width/vwidth), y*(height/vheight), (width/vwidth), (width/vwidth));
 				}
 			}
 		
 		int y=0;
 		for (int i=0;i<players.size();i++)
 			if(players.get(i).name!="")
 			{
 				g.setColor(players.get(i).color);
 				g.drawString(players.get(i).name+": "+players.get(i).points, width+5, 20+25*(y++));
 			}
 		
 		y=0;
 		g.setColor(new Color(255,255,255));
 		for (int i=0;i<MAXMESSAGES;i++)
 			if (messages[i]!=null)
 				g.drawString(messages[i], 5, height+10+25*(y++));
 	}
 }
