 package net.coobird.paint.filter;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 /*
  * FIXME Threads are not being freed when done.
  * Fix this or else programs that create a new instance of this class cannot
  * exit. Find a way to release the threads.
  */
 
 /*
  * Note: with CachedThreadPool, the cached threads will stay alive for 60 s,
  * therefore, prevents GC, therefore prevents immediate termination of calling
  * application. Ouch.
  */
 
 /**
  * <p>
  * The {@code ImageFilterThreadingWrapper} class is a wrapper class to
  * </p>
  * <p>
  * Note: An image with at least one dimension under 100 pixels will cause the
  * {@code ImageFilterThreadingWrapper} to bypass the multithreaded rendering
  * operation and@handoff the processing to the wrapped {@link ImageFilter}.
  * </p>
  * <p>
  * At small sizes, the expected time savings from using multithreaded rendering
  * is negative, as the overhead for creating threads is larger than the benefit
  * of using multiple threads to render the image.
  * </p>
  * @author coobird
  *
  */
 public class ImageFilterThreadingWrapper extends ImageFilter
 {
 	/**
 	 * Number of pixels to overlap between the rendered quadrants.
 	 */
 	private static final int OVERLAP = 5;
 
 	/**
 	 * The wrapped {@code ImageFilter}.
 	 */
 	private ImageFilter filter;
 	/**
 	 * Indicates whether or not multi-threaded rendering should be bypassed.
 	 */
 	private boolean bypass = false;
 
 	/**
 	 * Instantiates a {@code ThreadedWrapperFilter} object with the specified
 	 * {@link ImageFilter}.
 	 * @param filter		The {@code ImageFilter} to wrap.
 	 */
 	public ImageFilterThreadingWrapper(ImageFilter filter)
 	{
		this("ImageFilterThreadingWrapper wrapping" + filter.getName(), filter);
 	}
 	
 	/**
 	 * Instantiates a {@code ThreadedWrapperFilter} object with the specified
 	 * {@link ImageFilter}.
 	 * @param name			The name of the filter.
 	 * @param filter		The {@code ImageFilter} to wrap.
 	 */
 	public ImageFilterThreadingWrapper(String name, ImageFilter filter)
 	{
 		super(name);
 		this.filter = filter;
 	}
 
 	@Override
 	public BufferedImage processImage(BufferedImage img)
 	{
 		ExecutorService es = null;
 		
 		/*
 		 * If only one processor is available, multi-threaded processing will
 		 * be bypassed.
 		 */
 		if (Runtime.getRuntime().availableProcessors() == 1)
 		{
 			bypass = true;
 		}
 		else
 		{
 			es = Executors.newFixedThreadPool(4);
 		}
 
 		int width = img.getWidth();
 		int height = img.getHeight();
 		
 		/*
 		 *  If the input size is less than 100 in any dimension, or if the
 		 *  bypass flag is set, multi-threaded rendering will be bypassed, and
 		 *  processing will be performed by the wrapped
 		 */
 		if (width < 100 || height < 100 || bypass)
 		{
 			return filter.processImage(img);
 		}
 		
 		final int halfWidth = img.getWidth() / 2;
 		final int halfHeight = img.getHeight() / 2;
 		
 		/*
 		 * Prepare the source images to perform the filtering on.
 		 * Each image has an overhang to prevent a "bordering" effect when
 		 * stitching the images back together at the end.
 		 */
 		final BufferedImage i1 = img.getSubimage(
 				0,
 				0,
 				halfWidth + OVERLAP,
 				halfHeight + OVERLAP
 		);
 		
 		final BufferedImage i2 = img.getSubimage(
 				halfWidth - OVERLAP,
 				0,
 				halfWidth + OVERLAP,
 				halfHeight + OVERLAP
 		);
 		
 		final BufferedImage i3 = img.getSubimage(
 				0,
 				halfHeight - OVERLAP,
 				halfWidth + OVERLAP,
 				halfHeight + OVERLAP
 		);
 		
 		final BufferedImage i4 = img.getSubimage(
 				halfWidth - OVERLAP,
 				halfHeight - OVERLAP,
 				halfWidth + OVERLAP,
 				halfHeight + OVERLAP
 		);
 		
 		BufferedImage result = new BufferedImage(
 				img.getWidth(),
 				img.getHeight(),
 				img.getType()
 		);
 		
 		/*
 		 * Send off a Callable to four threads.
 		 * Each thread will call the filter.processImage, then take a subimage
 		 * and return that as the result.
 		 * The subimage@is shifted to remove the overlap to eliminate the 
 		 * "bordering" effect (which occurs at the edges of an image when a
 		 * filter is applied) which makes the resulting stitched up image to
 		 * have borders down the horizontal and vertical center of the image.
 		 */
 		Future<BufferedImage> f1 = es.submit(new Callable<BufferedImage>() {
 			public BufferedImage call()
 			{
 				return filter.processImage(i1).getSubimage(
 						0,
 						0,
 						halfWidth,
 						halfHeight
 				);
 			}
 		});
 		Future<BufferedImage> f2 = es.submit(new Callable<BufferedImage>() {
 			public BufferedImage call()
 			{
 				return filter.processImage(i2).getSubimage(
 						OVERLAP,
 						0,
 						halfWidth,
 						halfHeight
 				);
 			}
 		});
 		Future<BufferedImage> f3 = es.submit(new Callable<BufferedImage>() {
 			public BufferedImage call()
 			{
 				return filter.processImage(i3).getSubimage(
 						0,
 						OVERLAP,
 						halfWidth,
 						halfHeight
 				);
 			}
 		});
 		Future<BufferedImage> f4 = es.submit(new Callable<BufferedImage>() {
 			public BufferedImage call()
 			{
 				return filter.processImage(i4).getSubimage(
 						OVERLAP,
 						OVERLAP,
 						halfWidth,
 						halfHeight
 				);
 			}
 		});
 		
 		/*
 		 * Stitch up the results from each of the four threads to make the
 		 * resulting image.
 		 */
 		Graphics2D g = result.createGraphics();
 		try
 		{
 			g.drawImage(f1.get(), 0, 0, null);
 			g.drawImage(f2.get(), halfWidth, 0, null);
 			g.drawImage(f3.get(), 0, halfHeight, null);
 			g.drawImage(f4.get(), halfWidth, halfHeight, null);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		catch (ExecutionException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			g.dispose();
 			es.shutdown();
 			es = null;
 		}
 		
 		return result;
 	}
 }
