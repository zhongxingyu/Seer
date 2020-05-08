 package net.coobird.paint.image;
 
 /**
  * The {@code ImageRendererFactory} class will return a suitable 
  * {@link ImageRenderer} for rendering in [[$insert_name_here]].
  * @author coobird
  *
  */
 public final class ImageRendererFactory
 {
 	/**
 	 * Single instance of {@code ImageRenderer}.
 	 */
 	private static ImageRenderer renderer;
 	
 	/**
 	 * Disallow instantiation of this class.
 	 */
 	private ImageRendererFactory() {}
 	
 	/**
 	 * Returns an instance of ImageRenderer.
 	 * (Implementation specific -- this factory returns a DefaultImageRenderer.)
 	 * @return	An instance of ImageRenderer.
 	 */
 	public static ImageRenderer getInstance()
 	{
		if (renderer == null)
 		{
			renderer = new DefaultImageRenderer();
 		}
 		
 		return renderer;
 	}
 }
