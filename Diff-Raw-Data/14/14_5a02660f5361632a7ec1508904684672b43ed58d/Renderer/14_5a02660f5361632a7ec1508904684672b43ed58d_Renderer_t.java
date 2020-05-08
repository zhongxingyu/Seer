 package de.otori.mandelbrot;
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 
 public class Renderer {
 
 	// Thread Manager
 	
 	final int width, height; 
 	final int iThreads;
 	final MBRenderThread[] rThreads, activeThreads;
 	final BufferedImage biImage;		
 	final Dimension threadRenderArea;
 	
 	final int xThreads, yThreads;
 	
 	public Renderer(BufferedImage biImage, int iThreads, Dimension renderArea)
 	{
 		width = biImage.getWidth();
 		height = biImage.getHeight();
 		
 		this.biImage = biImage;		
 		
 		threadRenderArea = renderArea;
 		
 		xThreads = (int)Math.ceil(width / (double)threadRenderArea.width);
 		yThreads = (int)Math.ceil(height / (double)threadRenderArea.height);

		this.iThreads = Math.min(iThreads, xThreads * yThreads);
 		
 		rThreads = new MBRenderThread[xThreads * yThreads];
		activeThreads = new MBRenderThread[this.iThreads];
 	}
 		
 	public void renderImage(double zoom)
 	{
 		for(int y = 0; y < yThreads; y++)
 		{
 			for(int x = 0; x < xThreads; x++)
 			{								
 				rThreads[y*xThreads + x] = new MBRenderThread(biImage, x * threadRenderArea.width, y * threadRenderArea.height, threadRenderArea.width, threadRenderArea.height, zoom);				
 			}
 		}
 		for(int i = 0; i < iThreads; i++)
 		{
 			activeThreads[i] = rThreads[i];
 			activeThreads[i].start();
 		}
 		
 		int thrCount = iThreads;
 		int anzThreads = xThreads * yThreads;
 		while(thrCount < anzThreads)
 		{
 			for(int i = 0; i < iThreads; i++)
 			{
 				if (activeThreads[i].isAlive())
 				{					
 					try {
 						Thread.sleep(0, 500000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						System.out.println("Thread Exception :(");
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					activeThreads[i] = rThreads[thrCount];
 					activeThreads[i].start();
 					thrCount++;
 					if(!(thrCount < anzThreads))
 						break;
 				}
 			}
 		}
 	}
 	
 }
