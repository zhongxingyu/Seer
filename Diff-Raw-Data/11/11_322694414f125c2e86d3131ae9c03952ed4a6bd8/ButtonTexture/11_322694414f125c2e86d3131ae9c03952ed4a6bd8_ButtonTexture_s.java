 package org.muis.base.style;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import org.muis.core.MuisDocument;
 
 /** Renders a button-looking texture over an element */
 public class ButtonTexture extends org.muis.core.style.BaseTexture
 {
 	private static class CornerRender
 	{
 		private int [][] theLightAlphas;
 
 		private int [][] theShadowAlphas;
 
 		CornerRender(int radius)
 		{
 			theLightAlphas = new int[radius][radius + 1];
 			theShadowAlphas = new int[radius][radius + 1];
 		}
 
 		void render(float lightSource)
 		{
 			lightSource += 180;
 			if(lightSource > 360)
 				lightSource -= 360;
 			lightSource *= Math.PI / 180;
 			int xL = (int) Math.round(-Math.cos(lightSource) * 255);
 			int yL = (int) Math.round(Math.sin(lightSource) * 255);
 			int rad = theLightAlphas.length;
 			int rad2 = rad * rad;
 			for(int x = 0; x < rad; x++)
 			{
 				int x2 = x * x;
 				for(int y = 0; y < rad; y++)
 				{
 					if(x2 + y * y > rad2)
 						continue;
 					int dot = (x + 1) * xL / (rad + 1) + (y + 1) * yL / (rad + 1);
 					if(Math.abs(dot) > 255)
 						continue;
 					if(dot > 0)
 						theLightAlphas[rad - y - 1][rad - x - 1] = dot;
 					else
 						theShadowAlphas[rad - y - 1][rad - x - 1] = -dot;
 				}
 			}
 			for(int y = 0; y < rad; y++)
 			{
				int dot = xL + (y + 1) * yL / (rad + 1);
 				if(Math.abs(dot) > 255)
 					continue;
 				if(dot > 0)
 					theLightAlphas[rad - y - 1][rad] = dot;
 				else
 					theShadowAlphas[rad - y - 1][rad] = -dot;
 			}
 		}
 
 		int getRadius()
 		{
 			return theLightAlphas.length;
 		}
 
 		int getLightAlpha(int x, int y)
 		{
 			return theLightAlphas[y][x];
 		}
 
 		int getShadowAlpha(int x, int y)
 		{
 			return theShadowAlphas[y][x];
 		}
 	}
 
 	private static class CornerRenderKey
 	{
 		final float source;
 
 		final int radius;
 
 		CornerRenderKey(float src, int rad)
 		{
 			source = src;
 			radius = rad;
 		}
 
 		@Override
 		public boolean equals(Object o)
 		{
 			return o instanceof CornerRenderKey && Math.abs(((CornerRenderKey) o).source - source) <= 1; // 1 degree doesn't matter
 		}
 
 		@Override
 		public int hashCode()
 		{
 			return Float.floatToIntBits(source);
 		}
 	}
 
 	private static org.muis.core.MuisCache.CacheItemType<CornerRenderKey, CornerRender, RuntimeException> cornerRendering = new org.muis.core.MuisCache.CacheItemType<CornerRenderKey, CornerRender, RuntimeException>() {
 		@Override
 		public CornerRender generate(MuisDocument doc, CornerRenderKey key) throws RuntimeException
 		{
 			CornerRender ret = new CornerRender(key.radius);
 			ret.render(key.source);
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
 		super.render(graphics, element, area);
 		int w = element.getWidth();
 		int h = element.getHeight();
 		// int startX = area == null ? 0 : area.x;
 		// int startY = area == null ? 0 : area.y;
 		// int endX = area == null ? w : startX + area.width;
 		// int endY = area == null ? h : startY + area.height;
 		org.muis.core.style.Size radius = element.getStyle().get(org.muis.core.style.BackgroundStyles.cornerRadius);
 		if(radius.getValue() == 0)
 			return;
 		int wRad = radius.evaluate(w);
 		int hRad = radius.evaluate(h);
 		if(wRad * 2 > w)
 			wRad = w / 2;
 		if(hRad * 2 > h)
 			hRad = h / 2;
 		// The radius of the corner render we get needs to be either at least the width or height radius for good resolution.
 		int maxRad = wRad;
 		if(hRad > maxRad)
 			maxRad = hRad;
 		float source = element.getStyle().get(org.muis.core.style.LightedStyle.lightSource).floatValue();
 		Color light = element.getStyle().get(org.muis.core.style.LightedStyle.lightColor);
 		Color shadow = element.getStyle().get(org.muis.core.style.LightedStyle.shadowColor);
 		int lightRGB = light.getRGB() & 0xffffff;
 		int shadowRGB = shadow.getRGB() & 0xffffff;
 		BufferedImage cornerImg = new BufferedImage(wRad, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 		BufferedImage tbEdgeImg = new BufferedImage(1, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 		BufferedImage lrEdgeImg = new BufferedImage(wRad, 1, BufferedImage.TYPE_4BYTE_ABGR);
		for(int i = 0; i < 1; i++)
 		{
 			// TODO test for area's containment of this corner, if area!=null
 			float tempSource = source - 90 * i;
 			while(tempSource < 0)
 				tempSource += 360;
 			CornerRenderKey key = new CornerRenderKey(tempSource, (int) (maxRad * 1.5f)); // If we need to generate, step it up
 			CornerRender cr = element.getDocument().getCache().getAndWait(element.getDocument(), cornerRendering, key, true);
 			if(cr.getRadius() < maxRad)
 			{
 				// Regenerate with a big enough radius
 				element.getDocument().getCache().remove(cornerRendering, key);
 				cr = element.getDocument().getCache().getAndWait(element.getDocument(), cornerRendering, key, true);
 			}
 
 			for(int x = 0; x < wRad; x++)
 				for(int y = 0; y < hRad; y++)
 				{
 					int crX = x, crY = y;
 					switch (i)
 					{
 					case 0: // Top left corner
 						crX = x;
 						crY = y;
 						break;
 					case 1: // Top right corner
 						crX = wRad - x - 1;
 						crY = y;
 						break;
 					case 2: // Bottom right corner
 						crX = wRad - x - 1;
 						crY = hRad - y - 1;
 						break;
 					case 3: // Bottom left corner
 						crX = x;
 						crY = hRad - y - 1;
 						break;
 					}
 					crX = Math.round(crX * cr.getRadius() * 1.0f / wRad);
 					crY = Math.round(crY * cr.getRadius() * 1.0f / hRad);
 					if(crX >= cr.getRadius())
 						crX = cr.getRadius() - 1;
 					if(crY >= cr.getRadius())
 						crY = cr.getRadius() - 1;
 					int alpha = cr.getLightAlpha(crX, crY);
 					if(alpha > 0)
 						cornerImg.setRGB(x, y, lightRGB | (alpha << 24));
 					else
 					{
 						alpha = cr.getShadowAlpha(crX, crY);
 						if(alpha > 0)
 							cornerImg.setRGB(x, y, shadowRGB | (alpha << 24));
 					}
 				}
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
			if(!graphics.drawImage(cornerImg, renderX, renderY, wRad, hRad, 0, 0, wRad, hRad, null))
 				cornerImg = new BufferedImage(wRad, hRad, BufferedImage.TYPE_4BYTE_ABGR);
 
 			// Corners drawn, now draw lines
 			switch (i)
 			{
 			case 0:
 				for(int y = 0; y < hRad; y++)
 				{
 					tbEdgeImg.setRGB(0, y, 0);
 					int crY = Math.round(y * cr.getRadius() * 1.0f / hRad);
 					int alpha = cr.getLightAlpha(cr.getRadius(), crY);
 					if(alpha > 0)
 						tbEdgeImg.setRGB(0, y, lightRGB | (alpha << 24));
 					else
 					{
 						alpha = cr.getShadowAlpha(cr.getRadius(), crY);
 						if(alpha > 0)
 							tbEdgeImg.setRGB(0, y, shadowRGB | (alpha << 24));
 					}
 				}
				graphics.drawImage(tbEdgeImg, wRad, 0, w - wRad, hRad, 0, 0, 1, hRad, null);
 				break;
 			case 1:
 				for(int x = 0; x < wRad; x++)
 				{
 					lrEdgeImg.setRGB(x, 0, 0);
 					int crY = Math.round((wRad - x - 1) * cr.getRadius() * 1.0f / wRad);
 					int alpha = cr.getLightAlpha(cr.getRadius(), crY);
 					if(alpha > 0)
 						lrEdgeImg.setRGB(x, 0, lightRGB | (alpha << 24));
 					else
 					{
 						alpha = cr.getShadowAlpha(cr.getRadius(), crY);
 						if(alpha > 0)
 							lrEdgeImg.setRGB(x, 0, shadowRGB | (alpha << 24));
 					}
 				}
 				graphics.drawImage(lrEdgeImg, w - wRad, hRad, w, h - hRad, 0, 0, wRad, 1, null);
 				break;
 			case 2:
 				for(int y = 0; y < hRad; y++)
 				{
 					tbEdgeImg.setRGB(0, y, 0);
 					int crY = Math.round((hRad - y - 1) * cr.getRadius() * 1.0f / hRad);
 					int alpha = cr.getLightAlpha(cr.getRadius(), crY);
 					if(alpha > 0)
 						tbEdgeImg.setRGB(0, y, lightRGB | (alpha << 24));
 					else
 					{
 						alpha = cr.getShadowAlpha(cr.getRadius(), crY);
 						if(alpha > 0)
 							tbEdgeImg.setRGB(0, y, shadowRGB | (alpha << 24));
 					}
 				}
 				graphics.drawImage(tbEdgeImg, wRad, h - hRad, w - wRad, h, 0, 0, 1, hRad, null);
 				break;
 			case 3:
 				for(int x = 0; x < wRad; x++)
 				{
 					lrEdgeImg.setRGB(x, 0, 0);
 					int crY = Math.round(x * cr.getRadius() * 1.0f / wRad);
 					int alpha = cr.getLightAlpha(cr.getRadius(), crY);
 					if(alpha > 0)
 						lrEdgeImg.setRGB(x, 0, lightRGB | (alpha << 24));
 					else
 					{
 						alpha = cr.getShadowAlpha(cr.getRadius(), crY);
 						if(alpha > 0)
 							lrEdgeImg.setRGB(x, 0, shadowRGB | (alpha << 24));
 					}
 				}
 				graphics.drawImage(lrEdgeImg, 0, hRad, wRad, h - hRad, 0, 0, wRad, 1, null);
 				break;
 			}
 		}
 
 		/*float sin = (float) Math.sin(source * Math.PI / 180);
 		float cos = (float) Math.cos(source * Math.PI / 180);
 
 		boolean left = source >= 180;
 		boolean top = source <= 90 || source > 270;
 		if(left)
 		{
 			source -= 180;
 			if(top)
 				source -= 90;
 		}
 		else if(!top)
 			source -= 90;
 		for(int i = 0; i < radius; i++)
 		{
 			float brighten;
 			// Vertical edges
 			int lineStartY = i;
 			int lineEndY = h - i;
 			if(lineStartY < startY)
 				lineStartY = startY;
 			if(lineEndY > endY)
 				lineEndY = endY;
 			if(lineStartY < lineEndY)
 			{
 				int x = i;
 				// Left edge
 				if(x >= startX || x < endX)
 				{
 					brighten = -sin * (radius - i) / radius;
 					if(brighten > 0)
 						graphics.setColor(new Color(255, 255, 255, (int) (brighten * 255)));
 					else
 						graphics.setColor(new Color(0, 0, 0, (int) (-brighten * 255)));
 					graphics.drawLine(x, lineStartY, x, lineEndY);
 				}
 				x = w - i - 1;
 				// Right edge
 				if(x >= startX || x < endX)
 				{
 					brighten = sin * (radius - i) / radius;
 					if(brighten > 0)
 						graphics.setColor(new Color(255, 255, 255, (int) (brighten * 255)));
 					else
 						graphics.setColor(new Color(0, 0, 0, (int) (-brighten * 255)));
 					graphics.drawLine(x, lineStartY, x, lineEndY);
 				}
 			}
 			int lineStartX = i;
 			int lineEndX = w - i;
 			if(lineStartX < startX)
 				lineStartX = startX;
 			if(lineEndX > endX)
 				lineEndX = endX;
 			if(lineStartX < lineEndX)
 			{
 				int y = i;
 				// Top edge
 				if(y >= startY || y < endY)
 				{
 					brighten = cos * (radius - i) / radius;
 					if(brighten > 0)
 						graphics.setColor(new Color(255, 255, 255, (int) (brighten * 255)));
 					else
 						graphics.setColor(new Color(0, 0, 0, (int) (-brighten * 255)));
 					graphics.drawLine(lineStartX, y, lineEndX, y);
 				}
 				y = h - i - 1;
 				// Right edge
 				if(y >= startY || y < endY)
 				{
 					brighten = -cos * (radius - i) / radius;
 					if(brighten > 0)
 						graphics.setColor(new Color(255, 255, 255, (int) (brighten * 255)));
 					else
 						graphics.setColor(new Color(0, 0, 0, (int) (-brighten * 255)));
 					graphics.drawLine(lineStartX, y, lineEndX, y);
 				}
 			}
 		}*/
 
 		/*for(int y = startY; y < endY; y++)
 			for(int x = startX; x < endX; x++)
 			{
 				float brighten = getBrighten(x, y, w, h, radius, sin, cos);
 				if(brighten < 0)
 				{
 					graphics.setColor(new Color(0, 0, 0, (int) (-brighten * 255)));
 					graphics.drawRect(x, y, 1, 1);
 				}
 				else if(brighten > 0)
 				{
 					graphics.setColor(new Color(255, 255, 255, (int) (brighten * 255)));
 					graphics.drawRect(x, y, 1, 1);
 				}
 			}*/
 	}
 
 	/**
 	 * @param x The x-value of the pixel to brighten or darken
 	 * @param y The y-value of the pixel to brighten or darken
 	 * @param w The width of the element being rendered
 	 * @param h The height of the element being rendered
 	 * @param radius The button radius to render with
 	 * @param sinSource The sin of the light source angle from the vertical
 	 * @param cosSource The cosine of the light source angle from the vertical
 	 * @return A number between -1 (to darken completely) and 1 (to wash out completely) to lighten or darken the background color
 	 */
 	public float getBrighten(int x, int y, int w, int h, int radius, float sinSource, float cosSource)
 	{
 		int minDist;
 		minDist = x;
 		boolean horizEdge = true;
 		boolean negOrPos = false;
 		if(w - x < minDist)
 		{
 			minDist = w - x;
 			negOrPos = true;
 		}
 		if(y < minDist)
 		{
 			minDist = y;
 			horizEdge = false;
 			negOrPos = false;
 		}
 		if(h - y < minDist)
 		{
 			minDist = h - y;
 			horizEdge = false;
 			negOrPos = true;
 		}
 		if(minDist > radius)
 			return 0;
 
 		if(!horizEdge)
 		{ // Top or bottom edge
 			if(!negOrPos) // Top edge
 				return cosSource * (radius - minDist) / radius;
 			else
 				// Bottom edge
 				return -cosSource * (radius - minDist) / radius;
 		}
 		else
 		{
 			if(!negOrPos) // Left edge
 				return -sinSource * (radius - minDist) / radius;
 			else
 				// Right edge
 				return sinSource * (radius - minDist) / radius;
 		}
 	}
 }
