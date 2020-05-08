 package org.eclipse.iee.editor.core.container;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.text.ITextPresentationListener;
 import org.eclipse.jface.text.ITextViewerExtension4;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.PaintObjectEvent;
 import org.eclipse.swt.custom.PaintObjectListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.GlyphMetrics;
 
 
 class StyledTextManager {
 	private final StyledText fStyledText;
 	private final ContainerManager fContainerManager;
 	private final ISourceViewer fSourceViewer;
 	
 	private enum State {
 		READY,
 		STYLES_UPDATE_REQUESTED,
 		TEXT_PRESENTATION_UPDATE_INITIATED,
 		STYLES_UPDATED,
 	}	
 	State fState = State.READY; 
 	
 	public StyledTextManager(ContainerManager containerManager) {
 		fContainerManager = containerManager;
 		fSourceViewer = containerManager.getSourceViewer();
 		fStyledText = fSourceViewer.getTextWidget();
 		
 		initListeners();
 	}
 		
 	public void updateStyles(boolean doInitiateTextPresentationUpdate) {
 		
 		assert(fState == State.READY);
 		System.out.println("READY/updateStyles()");
 
 		fState = State.STYLES_UPDATE_REQUESTED;
 			
 		if (doInitiateTextPresentationUpdate) {
 			fStyledText.redraw();
 		}
 	}
 	
 	protected void initListeners() {
 		
 		fStyledText.addPaintObjectListener(new PaintObjectListener() {
 			public void paintObject(PaintObjectEvent e) {
 				
 				switch (fState) {
 				
 				case STYLES_UPDATE_REQUESTED:
 					/* If we are here, TextPresentationListener have not been executed yet, so trying to do this */
 					System.out.println("STYLES_UPDATE_REQUESTED/paintObject(): invalidating text presentation");
 					
 					fState = State.TEXT_PRESENTATION_UPDATE_INITIATED; 
 					fSourceViewer.invalidateTextPresentation();
 					break;
 					
 				case STYLES_UPDATED:
 					System.out.println("STYLES_UPDATED/paintObject(): reconcile containers's positions");
 					for (Container c : fContainerManager.getContainers()) {
 						c.updatePresentation();
 					}
 					fState = State.READY;				
 					break;
 				}
 			}
 		});
 		
 		((ITextViewerExtension4) fSourceViewer).addTextPresentationListener(new ITextPresentationListener() {			
 			@Override
 			public void applyTextPresentation(TextPresentation textPresentation) {
 				
 				switch (fState) {
 				
				case STYLES_UPDATE_REQUESTED:
					System.out.println("STYLES_UPDATE_REQUESTED/applyTextPresentation(): applying styles to text presentation");
					
					printTextPresentationStyleRanges(textPresentation);					
					
					//textPresentation.mergeStyleRanges(getContainersStyleRanges());
					injectStylesToTextPresentation(textPresentation, getContainersStyleRanges());
					
					printTextPresentationStyleRanges(textPresentation);		
					break;
					
 				case TEXT_PRESENTATION_UPDATE_INITIATED:
 					System.out.println("TEXT_PRESENTATION_UPDATE_INITIATED/applyTextPresentation(): applying styles to text presentation");
 
					printTextPresentationStyleRanges(textPresentation);					
					
					//textPresentation.mergeStyleRanges(getContainersStyleRanges());
 					injectStylesToTextPresentation(textPresentation, getContainersStyleRanges());
 					
					printTextPresentationStyleRanges(textPresentation);	
 					
					fState = State.STYLES_UPDATED;
 					break;
 				}
 			}
 		});
 		
 	}
 	
 	protected void injectStylesToTextPresentation(TextPresentation tp, StyleRange[] containersStyleRanges) {
 		
 		/*
 		for (int i = 0; i < containersStyleRanges.length; i += 2) {
 			StyleRange first = containersStyleRanges[i];
 			StyleRange second = containersStyleRanges[i];
 		}*/
 		
 		List<StyleRange> allowedStyleRanges = new ArrayList<StyleRange>();
 		
 		Iterator it = tp.getAllStyleRangeIterator();
 		while (it.hasNext()) {
 			StyleRange styleRange = (StyleRange) it.next();
 			
 			boolean overlap = false;
 			for (StyleRange containersStyleRange : containersStyleRanges) {
 				if (doStyleRangesOverlap(styleRange, containersStyleRange)) {
 					overlap = true;
 					break;
 				}
 			}
 			
 			if (!overlap) {		
 				allowedStyleRanges.add(styleRange);
 			}
 		}
 		
 		/* Convert to array */
 		StyleRange[] allowedStyleRangesArray = new StyleRange[allowedStyleRanges.size()];
 		for (int i = 0; i < allowedStyleRanges.size(); i++) {
 			allowedStyleRangesArray[i] = allowedStyleRanges.get(i);
 		}
 		
 		StyleRange defaultStyleRange = tp.getDefaultStyleRange();
 		
 		tp.clear();
 		tp.setDefaultStyleRange(defaultStyleRange);		
 		tp.mergeStyleRanges(allowedStyleRangesArray);
 		tp.mergeStyleRanges(containersStyleRanges);		
 	}
 	
 	protected boolean doStyleRangesOverlap(StyleRange a, StyleRange b) {
 		
 		if (a.start <= b.start) {
 			if (b.start < a.start + a.length) {
 				return true;
 			}			
 		}
 		else {
 			if (a.start < b.start + b.length) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected void printTextPresentationStyleRanges(TextPresentation tp) {
 		System.out.println("\n== TextPresentation== ");
 		Iterator it = tp.getAllStyleRangeIterator();
 		while (it.hasNext()) {
 			StyleRange styleRange = (StyleRange) it.next();
 			System.out.println(styleRange);
 		}
 		System.out.println("\n");
 	}
 		
 	protected StyleRange[] getContainersStyleRanges() {	
 		
 		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
 		for (Container c : fContainerManager.getContainers()) {
 			styleRanges.addAll(getContainerStyles(c));
 		}
 
 		StyleRange[] rangeArray = new StyleRange[styleRanges.size()];
 		for (int i = 0; i < styleRanges.size(); i++) {
 			rangeArray[i] = styleRanges.get(i);
 		}
 				
 		return rangeArray;
 	}	
 	
 	protected List<StyleRange> getContainerStyles(Container c) {
 		List<StyleRange> styles = new ArrayList<StyleRange>();
 		
 		Position p = c.getPosition();
 
 		int descent = // TODO: Quickly fix it!!!
 			(c.getComposite().getSize().y < 10)
 				? 0
 				: c.getComposite().getSize().y - 10;
 
 		/* First symbol is shaped by container's geometry */
 		StyleRange firstSymbol = new StyleRange();
 		firstSymbol.start = p.getOffset();
 		firstSymbol.length = 1;
 		firstSymbol.metrics = new GlyphMetrics(0, descent, c.getComposite().getSize().x);
 
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
