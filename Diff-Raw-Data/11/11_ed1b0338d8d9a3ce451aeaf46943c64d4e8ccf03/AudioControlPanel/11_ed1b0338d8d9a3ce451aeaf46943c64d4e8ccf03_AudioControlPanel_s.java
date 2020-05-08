 package view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.font.FontRenderContext;
 import java.awt.font.GlyphVector;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import javax.swing.Timer;
 
 import view.world.GameLevel;
 import view.world.GameWorld;
 
 public class AudioControlPanel extends GameLevel implements ActionListener
 {
 	private static final long	serialVersionUID	= 1L;
 	private String[] s = {"Audio controls:","volume","sound effects","Back"};
 	private int select, maxSelect;
 	private ArrayList<Shape> shapes;
 	private Timer timer;
 	
 	public AudioControlPanel(GameWorld world, GameFrame frame)
 	{
 		super(world,frame);
 		select = 0;
 		maxSelect = 3;
 		shapes = new ArrayList<Shape>();
 		Rectangle2D title = new Rectangle2D.Double(345, 100, 210, 40);
 		shapes.add(title);
 		Rectangle2D option1 = new Rectangle2D.Double(230, 200, 440, 40);
 		shapes.add(option1);
 		Rectangle2D option2 = new Rectangle2D.Double(230, 270, 440, 40);
 		shapes.add(option2);
 		Rectangle2D back = new Rectangle2D.Double(345, 340, 210, 40);
 		shapes.add(back);
 		timer = new Timer(1000/60, this);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;		
 		for(int id = 0; id < shapes.size(); id++)
 		{
 			GradientPaint p = null;
 			if(select+1 == id)
 				p = new GradientPaint(0,0,Color.GREEN,900,0,Color.BLUE);
 			else
 				p = new GradientPaint(0,0,Color.LIGHT_GRAY,800,0,Color.DARK_GRAY);
 			g2.setPaint(p);
 			g2.fill(shapes.get(id));
 			if(id > 0)
 			{
 				g2.setColor(Color.BLACK);
 				g2.draw(shapes.get(id));
 			}
 		}
 		Font font = new Font("Serif", Font.BOLD, 30);
 		g2.setFont(font);
 		FontRenderContext frc = g2.getFontRenderContext();
 		
 		GlyphVector gv = font.createGlyphVector(frc, s[0]);
 		Shape glyph = gv.getOutline(350,130);
 		g2.draw(glyph);
		String[] str = {"<< "+world.getVolumeInPerCent()+" >>","no sounds"};
 		for(int i = 0; i < 2; i++)
 		{
 			gv = font.createGlyphVector(frc, s[i+1]);
 			GlyphVector gv2 = font.createGlyphVector(frc, str[i]);
 			glyph = gv.getOutline(250,230+i*70);
 			FontMetrics fm = getFontMetrics( getFont() );
 			int width = fm.stringWidth(str[i]);
 			Shape glyph2 = gv2.getOutline(590-width,230+i*70);
 			g2.draw(glyph);
 			g2.draw(glyph2);
 		}
 		
 		gv = font.createGlyphVector(frc, s[3]);
 		glyph = gv.getOutline(400,370);
 		g2.draw(glyph);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0)
 	{
 		update();
 	}
 
 	@Override
 	public void up()
 	{
 		select = (select+maxSelect-1)%maxSelect;
 	}
 
 	@Override
 	public void down()
 	{
 		select = (select+1)%maxSelect;
 	}
 
 	@Override
 	public void left()
 	{
 		if(select == 0)
 			world.decreaseVolume();
 	}
 
 	@Override
 	public void right()
 	{
 		if(select == 0)
 			world.incrementVolume();
 	}
 
 	@Override
 	public void enter()
 	{
 		if(select == 2)
 			frame.loadMap(new MenuPanel(world,frame));
 	}
 	public void escape()
 	{
 		frame.loadMap(new MenuPanel(world,frame));
 	}
 
 	@Override
 	public void start()
 	{
 		timer.start();
 	}
 
 	@Override
 	public void stop()
 	{
 		timer.stop();
 	}
 
 	public void update()
 	{
 		repaint();
 	}
 }
