 package MandelFred;
 
 
 
 public class Mandelbrot {
 	private static int numThreads = 8;
 	private static final int SizeX=1600;
 	private static final int SizeY=900;
 
 	public static void show_mandelbrot(Complex c_origin, double c_step, int max_iter) {
 		MainWindow frame = new MainWindow(10,10,SizeX,SizeY);
 		frame.setTitle("show_mandelbrot");
 
 		/* Implementieren des Mandelbrot Algorithmus */
 
 		//iterate through all pixels
 		for(int pos_x=0;pos_x<frame.getszx();pos_x++){
 			for(int pos_y=0;pos_y<frame.getszy();pos_y++){
 				//now calculate escape velocity for every pixel
 
 				//init expansion
 				Complex c=new Complex(c_origin.real()+pos_x*c_step,c_origin.imag()+pos_y*c_step);
 				Complex z=new Complex(0,0);
 				int n=0;
 
 				//expansion  (reihenentwicklung)
 				while(z.abs_sqr()<=4&&n<max_iter){  //4 because we don't take the root of the .abs_sqr
 					z=c.add(z.sqr());
 					n++;
 				}
 
 				//System.out.println(Math.log(n));
 
 				//color pixel
 				frame.setPixel(pos_x, pos_y, (int)(Math.log(n)/Math.log(max_iter)*255) , (int)(n/(double)(max_iter)*255), 255-(int)(Math.log(n)/Math.log(max_iter)*255));
 			}
 		}
 
 
 		frame.repaint();
 		frame.setVisible(true);
 
 	}
 
 	public static void show_mandelbrot_threaded(Complex c_origin, double c_step, int max_iter) {
 		MainWindow frame = new MainWindow(10,10,SizeX,SizeY);
 		frame.setTitle("show_mandelbrot_threaded");
 		MandelRunnable f[] = new MandelRunnable[numThreads];
 		int stepX = frame.getszx()/f.length;
 		int end = frame.getszx()-1;
 		
 		for(int i = 0; i != f.length-1;i++)
 		{
 			f[i]=new MandelRunnable();
 			f[i].setOptions(c_origin, c_step, max_iter, i*stepX, ((i+1)*stepX)-1, frame);
 			f[i].start();
 		}
 		f[f.length-1]=new MandelRunnable();
 		f[f.length-1].setOptions(c_origin, c_step, max_iter, (f.length-1)*stepX, end, frame);
 		f[f.length-1].start();
 		
 		//wait for all threads
 		for(MandelRunnable i: f)
 		{
 			try
 			{
 				i.join();
 			} catch (InterruptedException e)
 			{
 				//last time I checked, I didn't care
 			}			
 		}
 		
 		
 
 
 		frame.repaint();
 		frame.setVisible(true);
 
 	}
 	
 	public static void show_mandelbrot_inplace_threaded(Complex c_origin, double c_step, int max_iter) {
 		MainWindow frame = new MainWindow(10,10,SizeX,SizeY);
 		frame.setTitle("show_mandelbrot_inplace_threaded");
 		MandelRunnable_inplace f[] = new MandelRunnable_inplace[numThreads];
 		int stepX = frame.getszx()/f.length;
 		int end = frame.getszx()-1;
 		
 		for(int i = 0; i != f.length-1;i++)
 		{
 			f[i]=new MandelRunnable_inplace();
 			f[i].setOptions(c_origin, c_step, max_iter, i*stepX, ((i+1)*stepX)-1, frame);
 			f[i].start();
 		}
 		f[f.length-1]=new MandelRunnable_inplace();
 		f[f.length-1].setOptions(c_origin, c_step, max_iter, (f.length-1)*stepX, end, frame);
 		f[f.length-1].start();
 		
 		//wait for all threads
 		for(MandelRunnable_inplace i: f)
 		{
 			try
 			{
 				i.join();
 			} catch (InterruptedException e)
 			{
 				//last time I checked, I didn't care
 			}			
 		}
 		
 		
 
 
 		frame.repaint();
 		frame.setVisible(true);
 
 	}
 
 
 	public static void show_mandelbrot_inplace(Complex c_origin, double c_step, int max_iter) {
 		MainWindow frame = new MainWindow(10,10,SizeX,SizeY);
 		frame.setTitle("show_mandelbrot_inplace");
 
 		//iterate through all pixels
 		for(int pos_x=0;pos_x<frame.getszx();pos_x++){
 			for(int pos_y=0;pos_y<frame.getszy();pos_y++){
 				//now calculate escape velocity for every pixel
 
 				//init expansion
 				Complex c=new Complex(c_origin.real()+pos_x*c_step,c_origin.imag()+pos_y*c_step);
 				Complex z=new Complex(0,0);
 				int n=0;
 
 				//expansion  (reihenentwicklung)
 				while(z.abs_sqr()<=4&&n<max_iter){  //4 because we don't take the root of the .abs_sqr
 					z.sqr_inplace();
 					z.add_inplace(c);
 					n++;
 				}
 
 				//System.out.println(Math.log(n));
 
 				//color pixel
 				frame.setPixel(pos_x, pos_y, (int)(Math.log(n)/Math.log(max_iter)*255) , (int)(n/(double)(max_iter)*255), 255-(int)(Math.log(n)/Math.log(max_iter)*255));
 			}
 		}
 
 
 		frame.repaint();
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) {
 		if(args != null && args.length == 1)
 		{
 			try
 			{
 				numThreads = Integer.parseInt(args[0]);				
 			}
 			catch(NumberFormatException e)
 			{
 				numThreads = Runtime.getRuntime().availableProcessors();
 			}
 		}
 		else
 		{
 			numThreads = Runtime.getRuntime().availableProcessors();
 		}
 		
 		numThreads = 64;
 
 		long time1=System.currentTimeMillis();    
 		show_mandelbrot(new Complex(-2.5, -1.3),   0.005, 1000);
 		show_mandelbrot(new Complex(-0.755, -0.1), 0.00002, 1000);
 		time1=System.currentTimeMillis()-time1;
 
 		long time2=System.currentTimeMillis();
 		show_mandelbrot_inplace(new Complex(-2.5, -1.3),   0.005, 1000);
 		show_mandelbrot_inplace(new Complex(-0.755, -0.1), 0.00002, 1000);
 		time2=System.currentTimeMillis()-time2;
 		
 		long time3=System.currentTimeMillis();
 		show_mandelbrot_threaded(new Complex(-2.5, -1.3),   0.005, 1000);
 		show_mandelbrot_threaded(new Complex(-0.755, -0.1), 0.00002, 1000);
 		time3=System.currentTimeMillis()-time3;
 		
 		long time4=System.currentTimeMillis();
 		show_mandelbrot_inplace_threaded(new Complex(-2.5, -1.3),   0.005, 1000);
 		show_mandelbrot_inplace_threaded(new Complex(-0.755, -0.1), 0.00002, 1000);
 		time4=System.currentTimeMillis()-time4;
 		
 		System.out.println("First time  ="+time1+"MS\nSecond time ="+time2+"MS");
		System.out.println("Runtime with "+ numThreads +" Threads = "+time3+"MS\nrunning it inplace = "+time4+"MS");
 	}
 
 
 }
 
