 package com.github.joukojo.testgame.images;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.junit.Test;
 
 public class ImageRotateTest {
 
	@Test
 	public void testRotateImages() throws IOException {
 		final BufferedImage playerNorthImage = ImageFactory.getPlayerNorthImage();
 
 		final BufferedImage squareImage = createSquareImage(playerNorthImage);
 
 		final File northFile = new File("/tmp/player-north.png");
 		ImageIO.write(playerNorthImage, "png", northFile);
 
 		rotateCW(0, squareImage);
 
 		final BufferedImage image90 = ImageIO.read(new File("/tmp/player-90.png"));
 
 		rotateCW(90, image90);
 	}
 
 	private void rotateCW(final int preRotation, final BufferedImage squareImage) throws IOException {
 		for (int i = 0; i <= 90; i++) {
 			// final AffineTransform affineTransform = new AffineTransform();
 			final AffineTransform affineTransform = AffineTransform.getTranslateInstance(squareImage.getHeight() / 2, squareImage.getWidth() / 2);
 
 			// scale image
 			affineTransform.scale(2.0, 2.0);
 			final double radians = Math.toRadians(i);
 			// rotate 45 degrees around image center
 			// affineTransform.rotate(i * Math.PI / 180.0,
 			// playerNorthImage.getWidth() / 2.0, playerNorthImage.getHeight() /
 			// 2.0);
 			affineTransform.translate(-squareImage.getWidth(null) / 2, -squareImage.getHeight(null) / 2);
 			affineTransform.rotate(radians);
 
 			final AffineTransform translationTransform = findTranslation(affineTransform, squareImage);
 			affineTransform.preConcatenate(translationTransform);
 
 			// instantiate and apply affine transformation filter
 			final BufferedImageOp bio = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
 
 			final BufferedImage background = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
 			final Graphics g2d = background.getGraphics();
 
 			final Color backgroundColor = Color.BLACK;
 			g2d.setColor(backgroundColor);
 			g2d.fillRect(0, 0, background.getWidth(), background.getHeight());
 
 			final BufferedImage rotatedImage = bio.filter(squareImage, background);
 
 			final File outputfile = new File("/tmp/player-" + (preRotation + i) + ".png");
 			ImageIO.write(rotatedImage, "png", outputfile);
 			System.out.println("file:" + outputfile.getName());
 		}
 	}
 
 	private BufferedImage createSquareImage(final BufferedImage bufferImage) throws IOException {
 		final BufferedImage square = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
 
 		final Graphics squareGraph = square.getGraphics();
 
 		squareGraph.drawImage(bufferImage, 50, 50, null);
 
 		final File northFile = new File("/tmp/player-square.png");
 		ImageIO.write(square, "png", northFile);
 
 		return square;
 
 	}
 
 	private AffineTransform findTranslation(final AffineTransform at, final BufferedImage bi) {
 
 		Point2D p2din = new Point2D.Double(0.0, 0.0);
 		Point2D p2dout = at.transform(p2din, null);
 		final double ytrans = p2dout.getY();
 
 		p2din = new Point2D.Double(0, bi.getHeight());
 		p2dout = at.transform(p2din, null);
 		final double xtrans = p2dout.getX();
 
 		final AffineTransform tat = new AffineTransform();
 		tat.translate(-xtrans, -ytrans);
 		return tat;
 	}
 
 }
