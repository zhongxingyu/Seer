 package org.eclipse.iee.editor.core.container;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextPresentationListener;
 import org.eclipse.jface.text.ITextViewerExtension4;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GlyphMetrics;
 
 class StyledTextManager {
 
 	private static final Logger logger = Logger
 			.getLogger(StyledTextManager.class);
 
 	/** left pad margin is necessary to show carret*/
 	public static final int PAD_LEFT_MARGIN = 2;
 	
 	private final StyledText fStyledText;
 	private final ContainerManager fContainerManager;
 	private final ISourceViewer fSourceViewer;
 
 	// private PaintObjectListener fPaintObjectListener;
 
 	public StyledTextManager(ContainerManager containerManager) {
 		fContainerManager = containerManager;
 		fSourceViewer = containerManager.getSourceViewer();
 		fStyledText = fSourceViewer.getTextWidget();
 
 		initListeners();
 	}
 
 	public void updateStyles(Container container) {
 		List<StyleRange> containerStyles = getContainerStyles(container);
 		for (StyleRange styleRange : containerStyles) {
 			fStyledText.setStyleRange(styleRange);
 		}
 	}
 
 	protected void initListeners() {
 
 		((ITextViewerExtension4) fSourceViewer)
 				.addTextPresentationListener(new ITextPresentationListener() {
 					@Override
 					public void applyTextPresentation(
 							TextPresentation textPresentation) {
 						logger.debug("applyTextPresentation start "
 								+ textPresentation.getExtent());
 						injectStylesToTextPresentation(textPresentation,
 								getContainersStyleRanges(textPresentation
 										.getExtent()));
 					}
 				});

 		fStyledText.addPaintListener(new PaintListener() {
 			@Override
 			public void paintControl(PaintEvent e) {
 				logger.debug("paintControl start " + e);
 				updateContainerPresentations();
 			}
 		});
 
 	}
 
 	protected void updateContainerPresentations() {
 		int topLineIndex = fSourceViewer.getTopIndex();
 		if (topLineIndex > 0) {
 			topLineIndex--;
 		}
 
 		int bottomLineIndex = fSourceViewer.getBottomIndex();
 		if (bottomLineIndex < fStyledText.getLineCount() - 1) {
 			bottomLineIndex++;
 		}
 
 		int topVisibleOffset = fStyledText.getOffsetAtLine(topLineIndex);
 		int bottomVisibleOffset = fStyledText.getOffsetAtLine(bottomLineIndex)
 				+ fStyledText.getLine(bottomLineIndex).length();
 
 		for (Container c : fContainerManager.getContainers()) {
 			boolean isVisible = false;
 			if (c.getPosition().getOffset() >= topVisibleOffset) {
 				isVisible = true;
 			}
 			if (c.getPosition().getOffset() + c.getPosition().getLength() > bottomVisibleOffset) {
 				isVisible = false;
 			}
 			c.setVisible(isVisible);
 			if (isVisible) {
 				c.updatePresentation();
 			}
 		}
 	}
 
 	protected void injectStylesToTextPresentation(TextPresentation tp,
 			StyleRange[] containersStyleRanges) {
 		tp.mergeStyleRanges(containersStyleRanges);
 	}
 
 	protected StyleRange[] getContainersStyleRanges(IRegion region) {
 
 		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
 		for (Container c : fContainerManager.getContainers()) {
 			Position position = c.getPosition();
 			int regionEnd = region.getOffset() + region.getLength();
 			int pEnd = position.getOffset() + position.getLength();
 			if (pEnd >= region.getOffset() && regionEnd > position.getOffset()) {
 				styleRanges.addAll(getContainerStyles(c));
 			}
 		}
 
 		StyleRange[] rangeArray = (StyleRange[]) styleRanges
 				.toArray(new StyleRange[styleRanges.size()]);
 
 		return rangeArray;
 	}
 
 	protected List<StyleRange> getContainerStyles(Container c) {
 		List<StyleRange> styles = new ArrayList<StyleRange>();
 
 		Position p = c.getPosition();
 
 		int descent = // TODO: Quickly fix it!!!
 		(c.getComposite().getSize().y < 10) ? 0
				: c.getComposite().getSize().y - 10;
 
 		/* First symbol is shaped by container's geometry */
 		StyleRange firstSymbol = new StyleRange();
 		firstSymbol.start = p.getOffset();
 		firstSymbol.length = 1;
		firstSymbol.metrics = new GlyphMetrics(0, descent, c.getComposite()
 				.getSize().x + PAD_LEFT_MARGIN);
 
 		/* Setting data */
 		firstSymbol.data = c;
 
 		styles.add(firstSymbol);
 
 		/* Other symbols in container's text region becomes invisible */
 		StyleRange hiddenText = new StyleRange();
 		hiddenText.start = p.getOffset() + 1;
 		hiddenText.length = p.getLength() - 1;
 		hiddenText.metrics = new GlyphMetrics(0, 0, 0);
 
 		styles.add(hiddenText);
 
 		return styles;
 	}
 
 }
