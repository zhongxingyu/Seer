 package com.summ.sge.graphics.engine.software.objects;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 import com.summ.sge.graphics.engine.software.SwingCanvas;
 import com.summ.sge.graphics.engine.software.utils.TextureHolder;
 import com.summ.sge.graphics.engine.software.utils.TextureResizer;
 import com.summ.sge.graphics.objects.Rectangle;
 
 public class SwingRectangle extends Rectangle {
 
 	@Override
 	public void draw() {
 		Graphics2D g = SwingCanvas.getContext();
 
 		AffineTransform identity = g.getTransform();
 
 		g.translate(mPosition.getX(), mPosition.getY());
		g.rotate(Math.toRadians(mRotation));
 
 		Composite originComposite = g.getComposite();
 		if (getAlpha() < 1.0f) {
 			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
 		}
 
 		if (mTexture > -1) {
 			BufferedImage image = TextureResizer.BILINEAR.resize(TextureHolder.INSTANCE.getTextureImage(mTexture), mWidth, mHeight);
 			g.drawImage(image, null, 0, 0);
 		} else {
 			g.setStroke(new BasicStroke(1));
 			g.setColor(new Color(mColor));
 			g.fillRect(0, 0, mWidth, mHeight);
 		}
 
 		g.setComposite(originComposite);
 
 		g.setTransform(identity);
 	}
 
 }
