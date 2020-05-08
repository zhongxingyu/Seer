 package org.chaoticbits.collabcloud.visualizer;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.font.FontRenderContext;
 import java.awt.font.GlyphVector;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.imageio.ImageIO;
 
 import org.chaoticbits.collabcloud.CloudWeights;
 import org.chaoticbits.collabcloud.ISummaryToken;
 import org.chaoticbits.collabcloud.visualizer.LastHitCache.IHitCheck;
 import org.chaoticbits.collabcloud.visualizer.color.IColorScheme;
 import org.chaoticbits.collabcloud.visualizer.font.IFontTransformer;
 import org.chaoticbits.collabcloud.visualizer.placement.IPlaceStrategy;
 import org.chaoticbits.collabcloud.visualizer.spiral.SpiralIterator;
 
 /**
  * The main class for laying out tokens on an image.
  * 
  * @author Andy
  * 
  */
 public class LayoutTokens {
 	private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(), true, true);
 	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutTokens.class);
 	private final int width;
 	private final int height;
 	private final IFontTransformer fontTrans;
 	private final IHitCheck<Shape> checker;
 	private final IPlaceStrategy placeStrategy;
 	private final SpiralIterator spiral;
 	private final IColorScheme colorScheme;
 	private final int maxTokens;
 
 	public LayoutTokens(int width, int height, int maxTokens, IFontTransformer fontTrans, IHitCheck<Shape> checker,
 			IPlaceStrategy placeStrategy, SpiralIterator spiral, IColorScheme colorScheme) {
 		this.width = width;
 		this.height = height;
 		this.maxTokens = maxTokens;
 		this.fontTrans = fontTrans;
 		this.checker = checker;
 		this.placeStrategy = placeStrategy;
 		this.spiral = spiral;
 		this.colorScheme = colorScheme;
 	}
 
 	public void makeImage(CloudWeights weights, File outputImageFile, String imageFormat) throws IOException {
 		log.info("Laying out tokens...");
 		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2d = bi.createGraphics();
 		g2d.setTransform(new AffineTransform()); // fixes upside down problem
 		setRenderingHints(g2d);
 		layoutTokens(g2d, weights);
 		log.info("Writing image...");
 		ImageIO.write(bi, "PNG", new File("output/summarizerepo.png"));
 		log.info("Done!");
 	}
 
 	private void layoutTokens(Graphics2D g2d, CloudWeights weights) throws IOException {
 		initImage(g2d);
 		LastHitCache<Shape> placedShapes = new LastHitCache<Shape>(checker);
 		List<Entry<ISummaryToken, Double>> entries = weights.sortedEntries();
 		int tokensHit = 0;
 		for (Entry<ISummaryToken, Double> entry : entries) {
 			if (tokensHit++ > maxTokens)
 				break;
 			Font font = fontTrans.transform(entry.getKey(), entry.getValue());
 			log.debug("Laying out " + entry.getKey() + "...[" + entry.getValue() + "]");
 			GlyphVector glyph = font.createGlyphVector(FONT_RENDER_CONTEXT, entry.getKey().getToken());
			Point2D startingPlace = placeStrategy.getStartingPlace(entry.getKey(), glyph.getOutline());
			Shape nextShape = glyph.getOutline((float) startingPlace.getX(), (float) startingPlace.getY());
			spiral.resetCenter(startingPlace);
			Point2D last = startingPlace;
 			while (spiral.hasNext()) {
 				Point2D next = spiral.next();
				nextShape = AffineTransform.getTranslateInstance(next.getX() - last.getX(), next.getY() - last.getY()).createTransformedShape(
						nextShape);
				last = next;
 				if (!placedShapes.hitNCache(nextShape)) {
 					g2d.setColor(colorScheme.lookup(entry.getKey(), weights));
 					g2d.fill(nextShape);
 					break;
 				}
 				// if (entry.getKey().getType() == JavaTokenType.CLASS)
 				// g2d.fillRect((int) next.getX(), (int) next.getY(), 3, 3);
 			}
 		}
 	}
 
 	private void setRenderingHints(Graphics2D g2d) {
 		RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 		g2d.setRenderingHints(renderHints);
 	}
 
 	private void initImage(Graphics2D g2d) {
 		g2d.setColor(Color.WHITE);
 		g2d.fillRect(0, 0, width, height);
 		g2d.setColor(Color.DARK_GRAY);
 		g2d.drawRect(0, 0, width - 1, height - 1);
 	}
 
 }
