 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.figures;
 
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.GridData;
 import org.eclipse.draw2d.GridLayout;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.LineBorder;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.text.BlockFlow;
 import org.eclipse.draw2d.text.FlowAdapter;
 import org.eclipse.draw2d.text.FlowPage;
 import org.eclipse.draw2d.text.TextFlow;
 import org.eclipse.examples.slideshow.core.Chunk;
 import org.eclipse.examples.slideshow.core.IContent;
 import org.eclipse.examples.slideshow.core.ImageChunk;
 import org.eclipse.examples.slideshow.core.ListContent;
 import org.eclipse.examples.slideshow.core.ListItemContent;
 import org.eclipse.examples.slideshow.core.SpanChunk;
 import org.eclipse.examples.slideshow.core.TextChunk;
 import org.eclipse.examples.slideshow.core.TextContent;
 import org.eclipse.examples.slideshow.resources.FontDescription;
 import org.eclipse.examples.slideshow.resources.ResourceManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Instances of this class renders slide content. This takes the form of an
  * array of {@link IContent} instances. Content can take the form of paragraphs,
  * lists, images, etc.
  * <p>
  * Instances require that an {@link FontDescription} be provided. This font
  * description is used as a basis for all font rendering in the instance.
  * Further, the instance requires that a {@link ResourceManager} be provided to
  * handle the allocation and management of system resources (like fonts and
  * images). If these values are not provided, the instance will not attempt to
  * render any content.
  * <p>
  * The instance can be scaled. Scaling changes the size of the content and is
  * typically used to make the instance fit into specific dimensions. When
  * scaled, both font and image content is resized as a ratio of their
  * &quot;natural&quot; size. Scale is set as an int value representing a
  * percentage.
  * <p>
  * The implementation is in its current form as a result of much exploration and
  * will benefit greatly by refactoring and/or reimplementation.
  */
 public class ListFigure extends Figure implements IResizeableFigure {
 
 	static final int MAX_BULLET_DEPTH = 10;
 		
 	private final ResourceManager resourceManager;
 
 	private IContent[] content;
 	private FontDescription fontDescription;
 	
 	/**
 	 * This field holds the scale, a percentage value, of the receiver.
 	 * A value of 100 means 100%.
 	 */
 	private int scale = 100;
 	
 	public ListFigure(ResourceManager resourceManager) {
 		this.resourceManager = resourceManager; 
 	}
 	
 	/**
 	 * Set the content displayed by the receiver. 
 	 * @param content
 	 */
 	public void setContent(IContent[] content) {
 		this.content = content;
 		rebuild();
 	}
 
 	/**
 	 * Rebuild the instance. This clears out any child figures that have previously
 	 * been laid out and replaces them with new figures based on the receiver's
 	 * {@link #content}. If the receiver bails out if it does not have enough 
 	 * information to do the layout.
 	 */
 	void rebuild() {
 		removeAll();
 		if (content == null) return;
 		if (getFontDescription() == null) return;
 		
 		GridLayout manager = new GridLayout(getMaximumBulletDepth(), false);
 		manager.verticalSpacing = getFontDescription().getHeight() /4;
 		setLayoutManager(manager);
 		
 		layoutFigures(this, 0, content);
 	}
 	
 
 	protected int getMaximumBulletDepth() {
 		return MAX_BULLET_DEPTH;
 	}
 
 	protected ResourceManager getResourceManager() {
 		return resourceManager;
 	}
 		
 	void layoutFigures(IFigure parent, int depth, IContent[] content) {
 		for (IContent item : content) {
 			if (item instanceof ListContent) {
 				layoutList(parent, depth + 1, (ListContent)item);
 			} else if (item instanceof TextContent) {
 				layoutText(parent, (TextContent)item);
 			}
 		}
 	}
 	
 	protected void layoutList(IFigure parent, int depth, ListContent list) {
 		for (IContent item : list.getItems()) {
			layoutListItem(parent, depth, (ListItemContent)item);
 		}
 	}
 
	private void layoutListItem(IFigure parent, int depth, ListItemContent item) {
 		if (item.hasText()) {
 			// Add some padding to shift us over to the place where the
 			// bullet should be placed.
 			for (int count = 0; count < depth; count++) {
 				parent.add(new Figure());
 			}
 			
 			/*
 			 * Obtain the font that we're going to use for the bullet and the
 			 * text. Note that the resource manager is going to take care of
 			 * managing the lifecycle of the Font.
 			 */
 			Font font = getResourceManager().getFont(getFontDescription().sizedBy(scale));
 			
 			// Create the bullet
			Label bullet = new Label("\u2022");
 			bullet.setFont(font);
 			parent.getLayoutManager().setConstraint(bullet, new GridData(SWT.RIGHT, SWT.TOP, false, false));
 			parent.add(bullet);
 			
 			IFigure text = createFlowPage(item);
 			parent.getLayoutManager().setConstraint(text, new GridData(SWT.FILL, SWT.TOP, true, false, ((GridLayout)parent.getLayoutManager()).numColumns - depth - 1, 1));
 			parent.add(text);
 		}
 		layoutFigures(parent, depth + 1, item.getNestedContent());
 	}
 
 	/**
 	 * This method lays out a text block for a {@link TextContent} instance into
 	 * an {@link IFigure}. A text block is only created if the instance actually
 	 * has text. More specifically, if the text content instance has zero-length
 	 * text content, then nothing happens; the visual impact is that the content
 	 * is effectively skipped.
 	 * 
 	 * @param parent
 	 *            the {@link IFigure} into which the text content is created.
 	 * @param item
 	 *            the {@link TextContent} instance to add to the parent.
 	 */
 	protected void layoutText(IFigure parent, TextContent item) {
 		if (!item.hasText()) return;
 		IFigure text = createFlowPage(item);
 		parent.add(text);
 		parent.getLayoutManager().setConstraint(text, new GridData(isImageOnlyContent(item) ? SWT.CENTER : SWT.FILL, SWT.BEGINNING, true, false, ((GridLayout)parent.getLayoutManager()).numColumns, 1));
 	}
 
 	protected IFigure createFlowPage(TextContent element) {
 		return createFlowPage(element, getFontDescription());
 	}
 	
 	protected IFigure createFlowPage(TextContent element, FontDescription fontDescription) {
 		FlowPage page = new FlowPage();
 		page.setForegroundColor(getForegroundColor());
 		BlockFlow blockFlow = new BlockFlow();
 		blockFlow.setHorizontalAligment(PositionConstants.LEFT);
 		addChunks(blockFlow, element.getChunks(), fontDescription);
 		page.add(blockFlow);
 		return page;
 	}
 
 	private void addChunks(BlockFlow blockFlow, Chunk[] chunks, FontDescription fontDescription) {
 		for (Chunk chunk : chunks) {
 			if (chunk instanceof TextChunk) addTextChunk(blockFlow, (TextChunk)chunk, fontDescription);
 			else if (chunk instanceof ImageChunk) addImageChunk(blockFlow, (ImageChunk)chunk);
 			else if (chunk instanceof SpanChunk) addSpanChunk(blockFlow, (SpanChunk)chunk, fontDescription);
 		}
 	}
 
 	private void addTextChunk(BlockFlow blockFlow, TextChunk chunk, FontDescription fontDescription) {
 		TextFlow textFlow = new TextFlow(chunk.getText());
 		blockFlow.add(textFlow);
 		textFlow.setFont(getResourceManager().getFont(fontDescription.sizedBy(scale)));
 	}
 
 	private void addImageChunk(BlockFlow blockFlow, ImageChunk chunk) {	
 		IResizeableFigure figure = createImageFigure(chunk);	
 	
 		FlowAdapter adapter = new FlowAdapter();
 		adapter.setLayoutManager(new StackLayout());
 		adapter.add(figure);
 		blockFlow.add(adapter);
 	}
 
 	/**
 	 * Create a figure for the image. The resulting figure will be a
 	 * {@link ResizeableImageFigure} if the image referred to by the parameter
 	 * is a valid image. If the image is not valid, the returned figure will be
 	 * an instance of MessageFigure containing a message describing the error.
 	 * 
 	 * @param chunk
 	 *            instance of {@link ImageChunk}
 	 * @return an instance of an implementor of {@link IResizeableFigure}.
 	 */
 	private IResizeableFigure createImageFigure(ImageChunk chunk) {
 		try {
 			Image image = getResourceManager().getImage(chunk.getBaseUrl(), chunk.getUrl());
 			return new ResizeableImageFigure(image, chunk.getWidth(), chunk.getHeight(), scale);
 		} catch (Exception e) {
 			MessageFigure figure = new MessageFigure(getResourceManager(), getFontDescription(), e.toString(), scale);
 			figure.setBorder(new LineBorder());
 			return figure;
 		}
 	}
 
 	private void addSpanChunk(BlockFlow blockFlow, SpanChunk chunk, FontDescription fontDescription) {
 		int style = SWT.NONE;
 		if (chunk.getStyle() == SpanChunk.STYLE_BOLD) style |= SWT.BOLD;
 		else if (chunk.getStyle() == SpanChunk.STYLE_ITALICS) style |= SWT.ITALIC;
 		
 		addChunks(blockFlow, chunk.getChunks(), fontDescription.withStyle(style));
 	}
 	
 	// TODO Move this to a helper class? Possibly as a static?
 	public boolean isImageOnlyContent(TextContent item) {
 		for (Chunk chunk : item.getChunks()) {
 			if (!(chunk instanceof ImageChunk)) return false;
 		}
 		return true;
 	}
 
 	public void setFontDescription(FontDescription fontDescription) {
 		this.fontDescription = fontDescription;
 		rebuild();
 	}
 
 	public FontDescription getFontDescription() {
 		return fontDescription;
 	}
 
 	public void setScale(int scale) {
 		this.scale = scale;
 		rebuild();
 	}
 
 	public int getScale() {
 		return scale;
 	}
 }
