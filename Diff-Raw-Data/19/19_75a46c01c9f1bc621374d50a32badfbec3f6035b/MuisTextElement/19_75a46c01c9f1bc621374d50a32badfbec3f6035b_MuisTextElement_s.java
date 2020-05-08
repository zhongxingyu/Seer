 package org.muis.core;
 
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.font.TextLayout;
 
 import org.muis.core.layout.AbstractSizeGuide;
 import org.muis.core.layout.SimpleSizeGuide;
 import org.muis.core.layout.SizeGuide;
 
 /** A MUIS element that serves as a placeholder for text content which may be interspersed with element children in an element. */
 public class MuisTextElement extends MuisLeaf {
 	private static final String DIVERSE_TEXT = "AjpQq";
 
 	private String theText;
 
 	/** Creates a MUIS text element */
 	public MuisTextElement() {
 		getDefaultStyleListener().addDomain(org.muis.core.style.FontStyle.getDomainInstance());
 	}
 
 	/**
 	 * Creates a MUIS text element with text
 	 *
 	 * @param text The text for the element
 	 */
 	public MuisTextElement(String text) {
 		theText = text;
 	}
 
 	/** @param text The text content for this element */
 	public void setText(String text) {
 		theText = text;
 	}
 
 	/** @return This element's text content */
 	public String getText() {
 		return theText;
 	}
 
 	@Override
 	public SizeGuide getWSizer() {
 		if(theText.length() == 0)
 			return new SimpleSizeGuide(0, 0, 0, 0, 0);
 		java.awt.Font font = MuisUtils.getFont(getStyle().getSelf());
 		if(font == null) {
 			msg().error("Could not derive font");
 			return new SimpleSizeGuide(0, 0, 0, 0, 0);
 		}
		java.awt.font.FontRenderContext context = new java.awt.font.FontRenderContext(null, getStyle().getSelf()
 			.get(org.muis.core.style.FontStyle.antiAlias).booleanValue(), false);
 		int min = 0;
 		int max = 0;
		java.awt.font.LineBreakMeasurer measurer = new java.awt.font.LineBreakMeasurer(
			new java.text.AttributedString(theText).getIterator(), java.text.BreakIterator.getWordInstance(), context);
 		while(true) {
 			TextLayout layout = measurer.nextLayout(Integer.MAX_VALUE);
 			if(layout == null)
 				break;
 			int advance = Math.round(layout.getAdvance());
 			if(advance > max)
 				max = advance;
 		}
 		if(getStyle().getSelf().get(org.muis.core.style.FontStyle.wordWrap)) {
 			measurer.setPosition(0);
 			while(true) {
 				TextLayout layout = measurer.nextLayout(1);
 				if(layout == null)
 					break;
 				int advance = Math.round(layout.getAdvance());
 				if(advance > min)
 					min = advance;
 			}
 		} else
 			min = max;
 		return new SimpleSizeGuide(min, min, max, max, max);
 	}
 
 	@Override
 	public SizeGuide getHSizer() {
 		return new AbstractSizeGuide() {
 			private int theCachedWidth;
 
 			private int theCachedHeight;
 
 			private int theCachedBaseline;
 
 			{
 				theCachedWidth = -1;
 			}
 
 			private void getSizes(int crossSize) {
 				if(crossSize == theCachedWidth)
 					return;
 				theCachedWidth = crossSize;
 				if(theText.length() == 0) {
 					theCachedHeight = 0;
 					theCachedBaseline = 0;
 					return;
 				}
 				int [] baseline = new int[1];
 				theCachedHeight = render(crossSize, null, baseline);
 				theCachedBaseline = baseline[0];
 				/*
 				java.awt.Font font = MuisUtils.getFont(getStyle());
 				if(font == null) {
 					msg().error("Could not derive font");
 					theCachedHeight = 0;
 					theCachedBaseline = 0;
 					return;
 				}
 				java.awt.font.FontRenderContext context = new java.awt.font.FontRenderContext(null, getStyle().getSelf()
 					.get(org.muis.core.style.FontStyle.antiAlias).booleanValue(), false);
 				java.awt.font.LineMetrics metrics = font.getLineMetrics(DIVERSE_TEXT, context);
 				theCachedHeight = Math.round(metrics.getHeight());
 				theCachedBaseline = Math.round(metrics.getBaselineOffsets()[metrics.getBaselineIndex()]);
 				*/
 			}
 
 			@Override
 			public int getMinPreferred(int crossSize, boolean csMax) {
 				getSizes(crossSize);
 				return theCachedHeight;
 			}
 
 			@Override
 			public int getMaxPreferred(int crossSize, boolean csMax) {
 				getSizes(crossSize);
 				return theCachedHeight;
 			}
 
 			@Override
 			public int getMin(int crossSize, boolean csMax) {
 				getSizes(crossSize);
 				return theCachedHeight;
 			}
 
 			@Override
 			public int getPreferred(int crossSize, boolean csMax) {
 				getSizes(crossSize);
 				return theCachedHeight;
 			}
 
 			@Override
 			public int getMax(int crossSize, boolean csMax) {
 				getSizes(crossSize);
 				return theCachedHeight;
 			}
 
 			@Override
 			public int getBaseline(int size) {
 				if(theCachedWidth < 0)
 					getSizes(Integer.MAX_VALUE);
 				return theCachedBaseline;
 			}
 		};
 	}
 
 	private int render(int width, Graphics2D graphics, int [] baseline) {
 		if(theText.length() == 0)
 			return 0;
 		java.awt.Font font = MuisUtils.getFont(getStyle().getSelf());
 		if(font == null) {
 			msg().error("Could not derive font");
 			return 0;
 		}
		java.awt.font.FontRenderContext context = new java.awt.font.FontRenderContext(null, getStyle().getSelf()
 			.get(org.muis.core.style.FontStyle.antiAlias).booleanValue(), false);
 		Font preFont = null;
 		boolean preAntiAlias = false;
 		int base = 0;
 		if(graphics != null) {
 			preFont = graphics.getFont();
 			preAntiAlias = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING) == RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
 			graphics.setFont(font);
 			graphics.setColor(getStyle().getSelf().get(org.muis.core.style.FontStyle.color));
 			if(getStyle().getSelf().get(org.muis.core.style.FontStyle.antiAlias))
 				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			else
 				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 			base = Math.round(font.getLineMetrics(DIVERSE_TEXT, context).getAscent());
 		}
 		int height = 0;
 		try {
 			int lineIdx = 0;
 			if(getStyle().get(org.muis.core.style.FontStyle.wordWrap) && width > 0) {
				java.awt.font.LineBreakMeasurer measurer = new java.awt.font.LineBreakMeasurer(
					new java.text.AttributedString(theText).getIterator(), java.text.BreakIterator.getWordInstance(), context);
 				boolean hasSetBaseline = false;
 				while(true) {
 					TextLayout layout = measurer.nextLayout(width);
 					if(layout == null)
 						break;
 					if(!hasSetBaseline && baseline != null)
 						baseline[0] = layout.getBaseline();
 					if(graphics != null)
 						layout.draw(graphics, 0, height + layout.getAscent());
 					if(height > 0)
 						height += layout.getLeading();
 					height += Math.round(layout.getAscent() + layout.getDescent());
 				}
 			} else {
 				for(int c = 0; c < theText.length(); c++) {
 					char ch = theText.charAt(c);
 					if(ch == '\n') {
 						TextLayout layout = new TextLayout(theText.substring(lineIdx, c), font, context);
 						if(graphics != null)
 							layout.draw(graphics, 0, base + height);
 						graphics.drawString(theText.substring(lineIdx, c), 0, base + height);
 						int lineH = Math.round(layout.getAscent() + layout.getDescent()) * 2;
 						height += lineH;
 						lineIdx = c + 1;
 					}
 				}
 				if(lineIdx < theText.length()) {
 					if(graphics != null)
 						graphics.drawString(theText.substring(lineIdx), 0, base + height);
 					height += Math.round(font.getLineMetrics(theText, lineIdx, theText.length(), context).getHeight()) * 2;
 				}
 			}
 		} finally {
 			if(graphics != null) {
 				graphics.setFont(preFont);
 				if(preAntiAlias)
 					graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 				else
 					graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 			}
 		}
 		return height;
 	}
 
 	@Override
 	public void paintSelf(Graphics2D graphics, Rectangle area) {
 		super.paintSelf(graphics, area);
 		render(bounds().getWidth(), graphics, null);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder ret = new StringBuilder();
 		if(getTagName() != null)
 			ret.append('<').append(getTagName()).append('>');
 		else
 			ret.append("<!TEXT>");
 		ret.append(org.jdom2.output.Format.escapeText(new org.jdom2.output.EscapeStrategy() {
 			@Override
 			public boolean shouldEscape(char ch) {
 				if(org.jdom2.Verifier.isHighSurrogate(ch)) {
 					return true; // Safer this way per http://unicode.org/faq/utf_bom.html#utf8-4
 				}
 				return false;
 			}
 		}, "\n", theText));
 		if(getTagName() != null)
 			ret.append('<').append('/').append(getTagName()).append('>');
 		else
 			ret.append("</TEXT\u00a1>");
 		return ret.toString();
 	}
 }
