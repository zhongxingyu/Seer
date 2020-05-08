 package org.muis.base.style;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import org.muis.core.MuisDocument;
 
 /** Renders a raised, round, button-looking texture over an element */
 public class RaisedRoundTexture implements org.muis.core.style.Texture
 {
 	private static class CornerRender
 	{
 		private short [][] theShadeAmt;
 
 		private java.util.BitSet theContainment;
 
 		CornerRender(int radius)
 		{
 			theShadeAmt = new short[radius][radius + 1];
 			theContainment = new java.util.BitSet(radius * radius);
 		}
 
 		void render(float lightSource, float maxShading)
 		{
 			lightSource -= 90;
 			if(lightSource < 0)
 				lightSource += 360;
 			lightSource *= Math.PI / 180;
 			int xL = (int) Math.round(-Math.cos(lightSource) * 255);
 			int yL = (int) Math.round(-Math.sin(lightSource) * 255);
 			int rad = theShadeAmt.length;
 			int rad2 = rad * rad;
 			// -1 so it renders some pixels for the edge too
 			for(int x = -1; x < rad; x++)
 			{
 				int x2 = (x + 1) * (x + 1);
 				for(int y = 0; y < rad; y++)
 				{
 					int contIdx = (rad - y - 1) * rad + (rad - x - 1);
 					theContainment.set(contIdx, x2 + (y + 1) * (y + 1) < rad2);
 					if(!theContainment.get(contIdx))
 						continue;
 					int dot = (x + 1) * xL / (rad + 1) + (y + 1) * yL / (rad + 1);
 					if(dot > 255)
 						dot = 255;
 					else if(dot < -255)
 						dot = -255;
 					theShadeAmt[rad - y - 1][rad - x - 1] = (short) (maxShading * dot);
 				}
 			}
 		}
 
 		int getRadius()
 		{
 			return theShadeAmt.length;
 		}
 
 		int getShadeAmount(int x, int y)
 		{
 			return theShadeAmt[y][x];
 		}
 
 		boolean contains(int x, int y)
 		{
 			int contIdx = theShadeAmt.length * y + x;
 			return theContainment.get(contIdx);
 		}
 	}
 
 	private static class CornerRenderKey
 	{
 		final float source;
 
 		final float maxShading;
 
 		final int radius;
 
 		CornerRenderKey(float src, float maxShade, int rad)
 		{
 			source = src;
 			maxShading = maxShade;
 			radius = rad;
 		}
 
 		@Override
 		public boolean equals(Object o)
 		{
 			if(!(o instanceof CornerRenderKey))
 				return false;
 			CornerRenderKey key = (CornerRenderKey) o;
 			return Math.abs(key.source - source) <= 1 && Math.abs(key.maxShading - maxShading) <= .01f;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			return Float.floatToIntBits(source) ^ Float.floatToIntBits(maxShading);
 		}
 	}
 
 	private static org.muis.core.MuisCache.CacheItemType<CornerRenderKey, CornerRender, RuntimeException> cornerRendering = new org.muis.core.MuisCache.CacheItemType<CornerRenderKey, CornerRender, RuntimeException>() {
 		@Override
 		public CornerRender generate(MuisDocument doc, CornerRenderKey key) throws RuntimeException
 		{
 			CornerRender ret = new CornerRender(key.radius);
 			ret.render(key.source, key.maxShading);
 			return ret;
 		}
 
 		@Override
 		public int size(CornerRender value)
 		{
 			return value.getRadius() * value.getRadius() * 2 + 16;
 		}
 	};
 
 	@Override
 	public void render(java.awt.Graphics2D graphics, org.muis.core.MuisElement element, java.awt.Rectangle area)
 	{
 		int w = element.getWidth();
 		int h = element.getHeight();
 		org.muis.core.style.Size radius = element.getStyle().get(org.muis.core.style.BackgroundStyles.cornerRadius);
 		int wRad = radius.evaluate(w);
 		int hRad = radius.evaluate(h);
 		Color bg = org.muis.core.MuisUtils.getBackground(element.getStyle());
 		graphics.setColor(bg);
 		if(area == null || (area.y <= h - hRad && area.y + area.height >= hRad))
 			graphics.fillRect(0, hRad, w, h - hRad * 2);
 		if(area == null || (area.x <= w - wRad && area.x + area.width >= w && area.y <= hRad && area.y + area.height >= hRad))
 			graphics.fillRect(wRad, 0, w - wRad * 2, hRad);
 		if(area == null || (area.x <= w - wRad && area.x + area.width >= w && area.y <= h && area.y + area.height >= h - hRad))
 			graphics.fillRect(wRad, h - hRad, w - wRad * 2, hRad);
 		if(wRad * 2 > w)
 			wRad = w / 2;
 		if(hRad * 2 > h)
 			hRad = h / 2;
 		// The radius of the corner render we get needs to be either at least the width or height radius for good resolution.
 		int maxRad = wRad;
 		if(hRad > maxRad)
 			maxRad = hRad;
 		float source = element.getStyle().get(org.muis.core.style.LightedStyle.lightSource).floatValue();
 		float maxShading = element.getStyle().get(org.muis.core.style.LightedStyle.maxShadingAmount).floatValue();
 		Color light = element.getStyle().get(org.muis.core.style.LightedStyle.lightColor);
 		Color shadow = element.getStyle().get(org.muis.core.style.LightedStyle.shadowColor);
 		int bgRGB = bg.getRGB();
 		int lightRGB = light.getRGB() & 0xffffff;
 		int shadowRGB = shadow.getRGB() & 0xffffff;
 		BufferedImage cornerBgImg = new BufferedImage(wRad, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 		BufferedImage cornerShadeImg = new BufferedImage(wRad, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 		BufferedImage tbEdgeImg = new BufferedImage(1, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 		BufferedImage lrEdgeImg = new BufferedImage(wRad, 1, BufferedImage.TYPE_4BYTE_ABGR);
 		int [][][] rot = new int[4][][];
 		rot[0] = null;
 		rot[1] = new int[][] {new int[] {0, 1}, new int[] {-1, 0}};
 		rot[2] = new int[][] {new int[] {-1, 0}, new int[] {0, -1}};
 		rot[3] = new int[][] {new int[] {0, -1}, new int[] {1, 0}};
 		for(int i = 0; i < 4; i++)
 		{
 			float tempSource = source - 90 * i;
 			while(tempSource < 0)
 				tempSource += 360;
 			CornerRenderKey key = new CornerRenderKey(tempSource, maxShading, (int) (maxRad * 1.5f)); // If we need to generate, step it up
 			CornerRender cr = element.getDocument().getCache().getAndWait(element.getDocument(), cornerRendering, key, true);
 			if(cr.getRadius() < maxRad)
 			{
 				// Regenerate with a big enough radius
 				element.getDocument().getCache().remove(cornerRendering, key);
 				cr = element.getDocument().getCache().getAndWait(element.getDocument(), cornerRendering, key, true);
 			}
 
 			// Draw the corner
 			int renderX = 0, renderY = 0;
 			switch (i)
 			{
 			case 0:
 				renderX = 0;
 				renderY = 0;
 				break;
 			case 1:
 				renderX = w - wRad - 1;
 				renderY = 0;
 				break;
 			case 2:
 				renderX = w - wRad - 1;
 				renderY = h - hRad - 1;
 				break;
 			case 3:
 				renderX = 0;
 				renderY = h - hRad - 1;
 				break;
 			}
 			if(area == null
 				|| (area.x <= renderX + wRad && area.x + area.width >= renderX && area.y <= renderY + hRad && area.y + area.height >= renderY))
 			{
 				for(int x = 0; x < wRad; x++)
 					for(int y = 0; y < hRad; y++)
 					{
 						int crX = x, crY = y;
 						crX = Math.round(crX * cr.getRadius() * 1.0f / wRad);
 						crY = Math.round(crY * cr.getRadius() * 1.0f / hRad);
 						if(rot[i] != null)
 						{
 							int preCrX = crX;
 							crX = rot[i][0][0] * crX + rot[i][0][1] * crY;
 							crY = rot[i][1][0] * preCrX + rot[i][1][1] * crY;
 						}
 						if(crX < 0)
 							crX += cr.getRadius();
 						else if(crX >= cr.getRadius())
 							crX = cr.getRadius() - 1;
 						if(crY < 0)
 							crY += cr.getRadius();
 						else if(crY >= cr.getRadius())
 							crY = cr.getRadius() - 1;
 
 						if(cr.contains(crX, crY))
 							cornerBgImg.setRGB(x, y, bgRGB);
 						else
 							cornerBgImg.setRGB(x, y, 0);
 
 						int alpha = cr.getShadeAmount(crX, crY);
 						if(alpha > 0)
 							cornerShadeImg.setRGB(x, y, lightRGB | (alpha << 24));
 						else if(alpha < 0)
 							cornerShadeImg.setRGB(x, y, shadowRGB | ((-alpha) << 24));
 						else
 							cornerShadeImg.setRGB(x, y, 0);
 					}
 				if(!graphics.drawImage(cornerBgImg, renderX, renderY, renderX + wRad, renderY + hRad, 0, 0, wRad, hRad, null))
 					cornerBgImg = new BufferedImage(wRad, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 				if(!graphics.drawImage(cornerShadeImg, renderX, renderY, renderX + wRad, renderY + hRad, 0, 0, wRad, hRad, null))
 					cornerShadeImg = new BufferedImage(wRad, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 			}
 
 			// Corner drawn, now draw the edge
 			switch (i)
 			{
 			case 0:
				if(area != null && (area.y > hRad || area.y + area.height < 0 || area.x > w - wRad || area.x + area.height < wRad))
 					break;
 				for(int y = 0; y < hRad; y++)
 				{
 					tbEdgeImg.setRGB(0, y, 0);
 					int crY = Math.round(y * cr.getRadius() * 1.0f / hRad);
 					int alpha = cr.getShadeAmount(cr.getRadius(), crY);
 					if(alpha > 0)
 						tbEdgeImg.setRGB(0, y, lightRGB | (alpha << 24));
 					else if(alpha < 0)
 						tbEdgeImg.setRGB(0, y, shadowRGB | ((-alpha) << 24));
 				}
 				graphics.drawImage(tbEdgeImg, wRad, 0, w - wRad, hRad, 0, 0, 1, hRad, null);
 				break;
 			case 1:
				if(area != null && (area.y > h - hRad || area.y + area.height < hRad || area.x > w || area.x + area.height < w - wRad))
 					break;
 				for(int x = 0; x < wRad; x++)
 				{
 					lrEdgeImg.setRGB(x, 0, 0);
 					int crY = Math.round((wRad - x - 1) * cr.getRadius() * 1.0f / wRad);
 					int alpha = cr.getShadeAmount(cr.getRadius(), crY);
 					if(alpha > 0)
 						lrEdgeImg.setRGB(x, 0, lightRGB | (alpha << 24));
 					else if(alpha < 0)
 						lrEdgeImg.setRGB(x, 0, shadowRGB | ((-alpha) << 24));
 				}
 				graphics.drawImage(lrEdgeImg, w - wRad, hRad, w, h - hRad, 0, 0, wRad, 1, null);
 				break;
 			case 2:
				if(area != null && (area.y > h || area.y + area.height < h - hRad || area.x > w - wRad || area.x + area.height < wRad))
 					break;
 				for(int y = 0; y < hRad; y++)
 				{
 					tbEdgeImg.setRGB(0, y, 0);
 					int crY = Math.round((hRad - y - 1) * cr.getRadius() * 1.0f / hRad);
 					int alpha = cr.getShadeAmount(cr.getRadius(), crY);
 					if(alpha > 0)
 						tbEdgeImg.setRGB(0, y, lightRGB | (alpha << 24));
 					else if(alpha < 0)
 						tbEdgeImg.setRGB(0, y, shadowRGB | ((-alpha) << 24));
 				}
 				graphics.drawImage(tbEdgeImg, wRad, h - hRad, w - wRad, h, 0, 0, 1, hRad, null);
 				break;
 			case 3:
				if(area != null && (area.y > h - hRad || area.y + area.height < hRad || area.x > wRad || area.x + area.height < 0))
 					break;
 				for(int x = 0; x < wRad; x++)
 				{
 					lrEdgeImg.setRGB(x, 0, 0);
 					int crY = Math.round(x * cr.getRadius() * 1.0f / wRad);
 					int alpha = cr.getShadeAmount(cr.getRadius(), crY);
 					if(alpha > 0)
 						lrEdgeImg.setRGB(x, 0, lightRGB | (alpha << 24));
 					else if(alpha < 0)
 						lrEdgeImg.setRGB(x, 0, shadowRGB | ((-alpha) << 24));
 				}
 				graphics.drawImage(lrEdgeImg, 0, hRad, wRad, h - hRad, 0, 0, wRad, 1, null);
 				break;
 			}
 		}
 	}
 }
