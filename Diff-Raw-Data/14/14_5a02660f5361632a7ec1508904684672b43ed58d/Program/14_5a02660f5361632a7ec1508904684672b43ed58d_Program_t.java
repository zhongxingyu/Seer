 package de.otori.mandelbrot;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class Program extends JPanel implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final BufferedImage mbImage;
 	private final int width, height, iThreads;
 	//private final MBRenderThread[] renderer;
 	private long tStart;
 	private final Renderer renderer;
 		
 	public Program(final int winWidth, final int winHeight, final int iThreads)
 	{
 		width = winWidth;
 		height = winHeight;
 		this.iThreads = iThreads;
 		mbImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
 		
 		//renderer = new MBRenderThread[iThreads];
 				
 		tStart = System.currentTimeMillis();
 		
 		renderer = new Renderer(mbImage, iThreads, new Dimension(80,80));
 		//Timer t = new Timer(40, this);
 		//t.start();
 	}
 	
 	/*
 	private void initThreads(double zoom)
 	{
 		int renderHeight = height / iThreads;
 		for(int i = 0; i < iThreads; i++)
 		{
 			renderer[i] = new MBRenderThread(mbImage, 0, i * renderHeight, width, renderHeight, zoom);						
 		}
 	}
 	*/
 	
 	/*
 	private void renderImage()
 	{	
 		long deltaTime = System.currentTimeMillis() - tStart;
 		
 		double zoom = 1 + 0.3 * (deltaTime % 6000) / 1000;
 		zoom *= zoom;		
 		
 		initThreads(zoom);
 		
 		for(int i = 0; i < iThreads; i++)
 		{
 			renderer[i].start();			
 		}
 		for(int i = 0; i < iThreads; i++)
 		{
 			try {
 				renderer[i].join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				System.out.println("Thread Exception: " + e.getMessage());
 			}			
 		}
 	}
 	*/
 	
 	@Override
 	public void paint (Graphics g)
 	{		
 		long ltStart = System.currentTimeMillis(); 
 				
 		//renderImage();
 		
 		long deltaTime = System.currentTimeMillis() - tStart;
 		
 		double zoom = 1 + 0.3 * (deltaTime % 6000) / 1000;
 		zoom *= zoom;
 		
 		renderer.renderImage(zoom);
 		g.drawImage(mbImage, 0, 0, null);
 		
 		long ltDur = System.currentTimeMillis() - ltStart;
 		
 		g.setColor(Color.white);		
 		String frameRate = Double.toString(1000.0 / ltDur);
 		frameRate = frameRate.substring(0, Math.min(5, frameRate.length() - 1));
 		g.drawString(frameRate + " fp/s", 5, 15);
 		
 		System.out.println("Rendering took " + ltDur + "ms");
 		System.out.println("Estimated FPS: " + 1000.0 / ltDur + "");
 			
 		repaint();		
 	}
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
		int imWidth = 600, imHeight = 400;
 		
 		Program mbProgram = new Program(imWidth, imHeight, 4);
 		JFrame frame = new JFrame("Mandelbrot / Julia");		
         frame.add(mbProgram);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(true);
         frame.setSize(imWidth, imHeight);
         
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
        
         Insets winInsets = frame.getInsets(); 
         frame.setSize(imWidth + winInsets.left + winInsets.right, imHeight + winInsets.top + winInsets.bottom);
         
 		System.out.println("Fractal Time 1337");			
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		repaint();
 	}
 }
