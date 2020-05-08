 package ecologylab.generic;
 
 import java.awt.AlphaComposite;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
 import javax.imageio.stream.ImageOutputStream;
 
 /**
  * A set of lovely convenience methods for working with images.
  */
 public class ImageTools extends Debug
 {
 
 	/**
 	 * Returns the new rectangle, which is the bounding box for <code>rect</code> rotated by
 	 * <code>theta</code> around the center
 	 * 
 	 * @param rect
 	 * @param theta
 	 * @return
 	 */
 	public static Rectangle getRotatedExtent(Rectangle rect, double theta)
 	{
 		return getRotatedExtent(rect.width, rect.height, theta);
 	}
 
 	/**
 	 * Calculates the new bounding box for a rectangle with width, height rotated by an angle theta
 	 * around the center
 	 * 
 	 * @param width
 	 * @param height
 	 * @param theta
 	 * @return
 	 */
 	public static Rectangle getRotatedExtent(int width, int height, double theta)
 	{
 		double diag = Math.sqrt(width * width + height * height);
 		double alpha = Math.atan2(height, width);
 		double newWidth = diag * Math.cos(theta - alpha);
 		double newHeight = diag * Math.cos(Math.PI / 2 - theta - alpha);
 		return new Rectangle((int) newWidth, (int) newHeight);
 	}
 
 	/**
 	 * Make a copy of the BufferedImage.
 	 * 
 	 * @param srcImage
 	 * @param destImage
 	 */
 	public static void copyImage(BufferedImage srcImage, BufferedImage destImage)
 	{
 		// scaleAndCopyImage(srcImage.getWidth(), srcImage.getHeight(), srcImage, destImage);
 		Graphics2D g2 = destImage.createGraphics();
 		g2.setComposite(AlphaComposite.Src);
 		g2.drawImage(srcImage, 0, 0, null);
 		g2.dispose();
 	}
 
 	/**
 	 * Make a scaled copy of the BufferedImage. Uses INTERPOLATION_BILINEAR.
 	 * 
 	 * @param srcImage
 	 * @param destImage
 	 */
 	public static void scaleAndCopyImage(int newWidth, int newHeight, BufferedImage srcImage,
 			BufferedImage destImage)
 	{
 		// AffineTransformOp scaleOp = createScaleOp(newWidth, newHeight, width, height);
 		// scaleOp.filter(bufferedImage, scaledBImage);
 		// faster than using AffineTransformOp scaleOp. i promise.
 		// -- use the source, luke!
 
 		Graphics2D g2 = destImage.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		g2.drawImage(srcImage, 0, 0, newWidth, newHeight, null);
 		g2.dispose();
 	}
 
 	/**
 	 * Take the RenderedImage passed in, compress it, and writes it to a file created from
 	 * outfileName.
 	 * 
 	 * @param compressionQuality
 	 *          ranges between 0 and 1, 0-lowest, 1-highest.
 	 */
 	public static void writeJpegFile(RenderedImage rendImage, String outfileName,
 			float compressionQuality)
 	{
 		writeJpegFile(rendImage, new File(outfileName), compressionQuality);
 	}
 
 	public static void writeFile(RenderedImage rendImage, File outfile, String formatName)
 	{
 		try
 		{
 			// Find a png writer
 			ImageWriter writer = null;
 			Iterator iter = ImageIO.getImageWritersByFormatName(formatName);
 			if (iter.hasNext())
 			{
 				writer = (ImageWriter) iter.next();
 			}
 
 			// Prepare output file
 			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
 			writer.setOutput(ios);
 
 			ImageTools imageTools = new ImageTools();
 			// Set the compression quality
 			ImageWriteParam iwparam = imageTools.new MyImageWriteParam();
 
 			// Write the image
 			writer.write(null, new IIOImage(rendImage, null, null), iwparam);
 
 			// Cleanup
 			ios.flush();
 			writer.dispose();
 			ios.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void writePngFile(RenderedImage rendImage, File outfile)
 	{
 		writeFile(rendImage, outfile, "png");
 	}
 
 	public static void writeTifFile(RenderedImage rendImage, File outfile)
 	{
 		writeFile(rendImage, outfile, "tif");
 	}
 
 	/**
 	 * Take the RenderedImage passed in, compress it, and writes it to outfile.
 	 * 
 	 * @param compressionQuality
 	 *          ranges between 0 and 1, 0-lowest, 1-highest.
 	 */
 	public static void writeJpegFile(RenderedImage rendImage, File outfile, float compressionQuality)
 	{
 		try
 		{
 			// Find a jpeg writer
 			ImageWriter writer = null;
 			Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
 			if (iter.hasNext())
 			{
 				writer = (ImageWriter) iter.next();
 			}
 			// Prepare output file
 			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
 			writer.setOutput(ios);
 		
 			ImageTools imageTools = new ImageTools();
 			// Set the compression quality
 			ImageWriteParam iwparam = imageTools.new MyImageWriteParam();
 			iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 			iwparam.setCompressionQuality(compressionQuality);
 
 			// Write the image
 			writer.write(null, new IIOImage(rendImage, null, null), iwparam);
 
 			// Cleanup
 			ios.flush();
 			writer.dispose();
 			ios.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Scale the image, then write a jpeg.
 	 */
 	/**
 	 * Take the RenderedImage passed in, compress it, and writes it to a file created from
 	 * outfileName.
 	 * 
 	 * @param compressionQuality
 	 *          ranges between 0 and 1, 0-lowest, 1-highest.
 	 */
 	public static void writeJpegFile(BufferedImage image, String fileName, float compressionQuality,
 			int width, int height)
 	{
 		// final int THUMBNAIL_WIDTH = 245;
 		// final int THUMBNAIL_HEIGHT = 350;
 
 		// int width = THUMBNAIL_WIDTH;
 		// int height = THUMBNAIL_HEIGHT;
 
 		float thumbRatio = (float) width / (float) height;
 		int imageWidth = image.getWidth(null);
 		int imageHeight = image.getHeight(null);
 		float imageRatio = (float) imageWidth / (float) imageHeight;
 
 		if (thumbRatio < imageRatio)
 		{
 			height = (int) (width / imageRatio);
 		}
 		else
 		{
 			width = (int) (height * imageRatio);
 		}
 		// println("writeThumbnail("+width+","+height+
 		// " type="+image.getType());
 
 		// draw original image to thumbnail image object and
 		// scale it to the new size on-the-fly
 		BufferedImage scaledImage = new BufferedImage(width, height, image.getType());
 
 		Graphics2D graphics2D = scaledImage.createGraphics();
 
 		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 
 		graphics2D.drawImage(image, 0, 0, width, height, null);
 
 		writeJpegFile(scaledImage, fileName, compressionQuality);
 	}
 
 	// This class overrides the setCompressionQuality() method to workaround
 	// a problem in compressing JPEG images using the javax.imageio package.
 	class MyImageWriteParam extends JPEGImageWriteParam
 	{
 		public MyImageWriteParam()
 		{
 			super(Locale.getDefault());
 		}
 
 		// This method accepts quality levels between 0 (lowest) and 1 (highest) and simply converts
 		// it to a range between 0 and 256; this is not a correct conversion algorithm.
 		// However, a proper alternative is a lot more complicated.
 		// This should do until the bug is fixed.
 		public void setCompressionQuality(float quality)
 		{
 			if (quality < 0.0F || quality > 1.0F)
 			{
 				throw new IllegalArgumentException("Quality out-of-bounds!");
 			}
 			this.compressionQuality = 256 - (quality * 256);
 		}
 	}
 
 	/**
 	 * Borrowed code from : http://forums.sun.com/thread.jspa?threadID=524460 
 	 * Allows determining the byte size of a compressed image.
 	 * 
 	 * @param image
 	 * @param quality
 	 * @param out
 	 * @throws IOException
 	 */
 	public static void write(BufferedImage image, float quality, OutputStream out) throws IOException
 	{
 		Iterator writers = ImageIO.getImageWritersBySuffix("jpeg");
 		if (!writers.hasNext())
 			throw new IllegalStateException("No writers found");
 		ImageWriter writer = (ImageWriter) writers.next();
 		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
 		writer.setOutput(ios);
 		ImageWriteParam param = writer.getDefaultWriteParam();
 		if (quality >= 0)
 		{
 			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 			param.setCompressionQuality(quality);
 		}
 		writer.write(null, new IIOImage(image, null, null), param);
 	}
 
 	/**
 	 * Uses image quality of .5
 	 * @param image
 	 * @return
 	 */
 	public static int getNumBytes(BufferedImage image)
 	{
 		return getNumBytes(image, .5f);
 	}
 	
 	/**
 	 * Altered method to NOT create a byteArray to find size.
 	 * @param image
 	 * @param quality
 	 * @return
 	 */
 	public static int getNumBytes(BufferedImage image, float quality)
 	{
 		try
 		{
 			ByteArrayOutputStream out = new ByteArrayOutputStream(5000);
 			write(image, quality, out);
 			return out.size();
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	public static byte[] getBytes(BufferedImage image, float quality)
 	{
 		try
 		{
 			ByteArrayOutputStream out = new ByteArrayOutputStream(5000);
 			write(image, quality, out);
 			return out.toByteArray();
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 
 }
