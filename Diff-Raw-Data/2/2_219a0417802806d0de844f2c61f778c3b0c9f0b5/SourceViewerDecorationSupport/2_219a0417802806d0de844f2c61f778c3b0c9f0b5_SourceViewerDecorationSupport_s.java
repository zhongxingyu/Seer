 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.ui.texteditor;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 
 import org.eclipse.jface.text.CursorLinePainter;
 import org.eclipse.jface.text.IPainter;
 import org.eclipse.jface.text.ITextViewerExtension2;
 import org.eclipse.jface.text.ITextViewerExtension4;
 import org.eclipse.jface.text.MarginPainter;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationPainter;
 import org.eclipse.jface.text.source.IAnnotationAccess;
 import org.eclipse.jface.text.source.ICharacterPairMatcher;
 import org.eclipse.jface.text.source.IOverviewRuler;
 import org.eclipse.jface.text.source.ISharedTextColors;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.MatchingCharacterPainter;
 import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
 
 /**
  * Support class used by text editors to draw and update decorations on the
  * source viewer and its rulers. An instance of this class is independent of a
  * certain editor and must be configured with the needed preference keys and
  * helper objects before it can be used.
  * <p>
  * Once configured, an instance may be installed (see
  * {@link #install(IPreferenceStore) install}) on a preference store, from then
  * on monitoring the configured preference settings and changing the respective
  * decorations. Calling {@link #uninstall() uninstall} will unregister the
  * listeners with the preferences store and must be called before changing the
  * preference store by another call to <code>install</code>.<br>
  * {@link #dispose() dispose} will uninstall the support and remove any
  * decorations from the viewer. It is okay to reuse a
  * <code>SourceViewerDecorationSupport</code> instance after disposing it.
  * </p>
  * <p>
  * <code>SourceViewerDecorationSupport</code> can draw the following
  * decorations:
  * <ul>
  * <li>matching character highlighting,</li>
  * <li>current line highlighting,</li>
  * <li>print margin, and</li>
  * <li>annotations.</li>
  * </ul>
  * Annotations are managed for the overview ruler and also drawn onto the text
  * widget by an 
  * {@link org.eclipse.jface.text.source.AnnotationPainter AnnotationPainter}
  * instance.
  * </p>
  * <p>
  * Subclasses may add decorations but should adhere to the lifecyle described
  * above.
  * </p>
  * 
  * @see org.eclipse.jface.text.source.AnnotationPainter
  * @since 2.1
  */
 public class SourceViewerDecorationSupport {
 	
 	
 	/**
 	 * Underline drawing strategy.
 	 * 
 	 * @since 3.0
 	 */
 	private static final class UnderlineDrawingStrategy implements IDrawingStrategy {
 
 		/*
 		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
 		 */
 		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
 			if (gc != null) {
 				
 				Point left= textWidget.getLocationAtOffset(offset);
 				Point right= textWidget.getLocationAtOffset(offset + length);
 				int y= left.y + textWidget.getLineHeight() - 1;
 				
 				gc.setForeground(color);
 				gc.drawLine(left.x, y, right.x, y);
 									
 			} else {
 				textWidget.redrawRange(offset, length, true);
 			}
 		}
 	}
 	
 	/**
 	 * Draws a box around a given range.
 	 * 
 	 * @since 3.0
 	 */
 	private static final class BoxDrawingStrategy implements IDrawingStrategy {
 		/*
 		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
 		 */
 		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
 			
 			if (length == 0) {
 				fgIBeamStrategy.draw(annotation, gc, textWidget, offset, length, color);
 				return;
 			}
 			
 			if (gc != null) {
 				
 				Point left= textWidget.getLocationAtOffset(offset);
 				Point right= textWidget.getLocationAtOffset(offset + length);
 				int x1= left.x;
 				int x2= right.x - 1;
 				int y1= left.y;
 				int y2= y1 + textWidget.getLineHeight() - 1;
 				
 				gc.setForeground(color);
 				gc.drawRectangle(x1, y1, x2 - x1, y2 - y1);
 									
 			} else {
 				textWidget.redrawRange(offset, length, true);
 			}
 		}
 	}
 	
 	/**
 	 * Draws an iBeam at the given offset, the length is ignored.
 	 * 
 	 * @since 3.0
 	 */
 	private static final class IBeamStrategy implements IDrawingStrategy {
 
 		/*
 		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
 		 */
 		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
 			if (gc != null) {
 				
 				Point left= textWidget.getLocationAtOffset(offset);
 				int x1= left.x;
 				int y1= left.y;
 				
 				gc.setForeground(color);
 				gc.drawLine(x1, y1, x1, left.y + textWidget.getLineHeight() - 1);
 									
 			} else {
 				textWidget.redrawRange(offset, length, true);
 			}
 		}
 	}
 	
 	/**
 	 * The box drawing strategy.
 	 * @since 3.0
 	 */
 	private static IDrawingStrategy fgBoxStrategy= new BoxDrawingStrategy();
 	
 	/**
 	 * The null drawing strategy.
 	 * @since 3.0
 	 */
 	private static IDrawingStrategy fgNullStrategy= new AnnotationPainter.NullStrategy();
 	
 	/**
 	 * The underline drawing strategy.
 	 * @since 3.0
 	 */
 	private static IDrawingStrategy fgUnderlineStrategy= new UnderlineDrawingStrategy();
 	
 	/**
 	 * The iBeam drawing strategy.
 	 * @since 3.0
 	 */
 	private static IDrawingStrategy fgIBeamStrategy= new IBeamStrategy();
 	
 	/**
 	 * The squiggles drawing strategy.
 	 * @since 3.0
 	 */
 	private static IDrawingStrategy fgSquigglesStrategy= new AnnotationPainter.SquigglesStrategy();
 	
 	/*
 	 * @see IPropertyChangeListener
 	 */
 	private class FontPropertyChangeListener implements IPropertyChangeListener {
 		/*
 		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 		 */
 		public void propertyChange(PropertyChangeEvent event) {
 			if (fMarginPainter != null && fSymbolicFontName != null && fSymbolicFontName.equals(event.getProperty()))
 				fMarginPainter.initialize();
 		}
 	}
 
 	
 	/** The viewer */
 	private ISourceViewer fSourceViewer;
 	/** The viewer's overview ruler */
 	private IOverviewRuler fOverviewRuler;
 	/** The annotation access */
 	private IAnnotationAccess fAnnotationAccess;
 	/** The shared color manager */
 	private ISharedTextColors fSharedTextColors;
 	
 	/** The editor's line painter */
 	private CursorLinePainter fCursorLinePainter;
 	/** The editor's margin ruler painter */
 	private MarginPainter fMarginPainter;
 	/** The editor's annotation painter */
 	private AnnotationPainter fAnnotationPainter;
 	/** The editor's peer character painter */
 	private MatchingCharacterPainter fMatchingCharacterPainter;
 	/** The character painter's pair matcher */
 	private ICharacterPairMatcher fCharacterPairMatcher;
 	
	/** Table of annotation type preference infos */
 	private Map fAnnotationTypeKeyMap= new HashMap();
 	/** Preference key for the cursor line highlighting */
 	private String fCursorLinePainterEnableKey;
 	/** Preference key for the cursor line background color */
 	private String fCursorLinePainterColorKey;
 	/** Preference key for the margin painter */	
 	private String fMarginPainterEnableKey;
 	/** Preference key for the margin painter color */
 	private String fMarginPainterColorKey;
 	/** Preference key for the margin painter column */
 	private String fMarginPainterColumnKey;
 	/** Preference key for the matching character painter */
 	private String fMatchingCharacterPainterEnableKey;
 	/** Preference key for the matching character painter color */
 	private String fMatchingCharacterPainterColorKey;
 	/** The property change listener */
 	private IPropertyChangeListener fPropertyChangeListener;
 	/** The preference store */
 	private IPreferenceStore fPreferenceStore;
 	/** The symbolic font name */
 	private String fSymbolicFontName;
 	/** The font change listener */
 	private FontPropertyChangeListener fFontPropertyChangeListener;
 
 
 	/**
 	 * Creates a new decoration support for the given viewer.
 	 * 
 	 * @param sourceViewer the source viewer
 	 * @param overviewRuler the viewer's overview ruler
 	 * @param annotationAccess the annotation access
 	 * @param sharedTextColors the shared text color manager
 	 */
 	public SourceViewerDecorationSupport(ISourceViewer sourceViewer, IOverviewRuler overviewRuler, IAnnotationAccess annotationAccess, ISharedTextColors sharedTextColors) {
 		fSourceViewer= sourceViewer;
 		fOverviewRuler= overviewRuler;
 		fAnnotationAccess= annotationAccess;
 		fSharedTextColors= sharedTextColors;
 	}
 	
 	/**
 	 * Installs this decoration support on the given preference store. It assumes
 	 * that this support has completely been configured.
 	 * 
 	 * @param store the preference store
 	 */
 	public void install(IPreferenceStore store) {
 		
 		fPreferenceStore= store;
 		if (fPreferenceStore != null) {
 			fPropertyChangeListener= new IPropertyChangeListener() {
 				public void propertyChange(PropertyChangeEvent event) {
 					handlePreferenceStoreChanged(event);
 				}
 			};
 			fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);
 		}
 		
 		if (fMarginPainter != null) {
 			fFontPropertyChangeListener= new FontPropertyChangeListener();
 			JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);
 		}
 		
 		updateTextDecorations();
 		updateOverviewDecorations();
 	}
 
 	/**
 	 * Updates the text decorations for all configured annotation types.
 	 */
 	private void updateTextDecorations() {
 		
 		StyledText widget= fSourceViewer.getTextWidget();
 		if (widget == null || widget.isDisposed())
 			return;
 		
 		if (areMatchingCharactersShown())
 			showMatchingCharacters();
 		else
 			hideMatchingCharacters();
 			
 		if (isCursorLineShown())
 			showCursorLine();
 		else
 			hideCursorLine();
 		
 		if (isMarginShown())
 			showMargin();
 		else
 			hideMargin();
 		
 		Iterator e= fAnnotationTypeKeyMap.keySet().iterator();
 		while (e.hasNext()) {
 			Object type= e.next();
 			Object style= getAnnotationDecorationType(type);
 			if (style != AnnotationPreference.STYLE_NONE)
 				showAnnotations(type, false, false);
 			else
 				hideAnnotations(type, false, false);
 			if (areAnnotationsHighlighted(type))
 				showAnnotations(type, true, false);
 			else
 				hideAnnotations(type, true, false);
 			
 		}
 		updateAnnotationPainter();
 	}
 	
 	/**
 	 * Returns the annotation decoration style used for the show in text preference for
 	 * a given annotation type.
 	 * 
 	 * @param annotationType the annotation type being looked up
 	 * @return the decoration style for <code>type</code>
 	 * @since 3.0
 	 */
 	private Object getAnnotationDecorationType(Object annotationType) {
 		if (areAnnotationsShown(annotationType) && fPreferenceStore != null) {
 			AnnotationPreference info= (AnnotationPreference) fAnnotationTypeKeyMap.get(annotationType);
 			if (info != null) {
 				String key= info.getTextStylePreferenceKey();
 				if (key != null)
 					return fPreferenceStore.getString(key);
 				else
 					// legacy
 					return AnnotationPreference.STYLE_SQUIGGLES;
 			}
 		}
 		return AnnotationPreference.STYLE_NONE;
 	}
 
 	/**
 	 * Updates the annotation overview for all configured annotation types.
 	 */
 	public void updateOverviewDecorations() {
 		if (fOverviewRuler != null) {
 			Iterator e= fAnnotationTypeKeyMap.keySet().iterator();
 			while (e.hasNext()) {
 				Object type= e.next();
 				if (isAnnotationOverviewShown(type))
 					showAnnotationOverview(type, false);
 				else
 					hideAnnotationOverview(type, false);
 			}
 			fOverviewRuler.update();
 		}
 	}
 	
 	/**
 	 * Uninstalls this support from the preference store it has previously been
 	 * installed on. If there is no such preference store, this call is without
 	 * effect.
 	 */
 	public void uninstall() {
 		
 		if (fPreferenceStore != null) {
 			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
 			fPropertyChangeListener= null;
 			fPreferenceStore= null;
 		}
 		
 		if (fFontPropertyChangeListener != null) {		
 			JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
 			fFontPropertyChangeListener= null;
 		}
 	}
 	
 	/**
 	 * Disposes this decoration support. Internally calls
 	 * <code>uninstall</code>.
 	 */
 	public void dispose() {
 		uninstall();
 		updateTextDecorations();
 		updateOverviewDecorations();
 	}
 	
 	/**
 	 * Sets the character pair matcher for the matching character painter.
 	 * 
 	 * @param pairMatcher
 	 */
 	public void setCharacterPairMatcher(ICharacterPairMatcher pairMatcher) {
 		fCharacterPairMatcher= pairMatcher;
 	}
 	
 	/**
 	 * Sets the preference keys for the annotation painter.
 	 * 
 	 * @param type the annotation type
 	 * @param colorKey the preference key for the color
 	 * @param editorKey the preference key for the presentation in the text area
 	 * @param overviewRulerKey the preference key for the presentation in the overview  ruler
 	 * @param layer the layer
 	 */
 	public void setAnnotationPainterPreferenceKeys(Object type, String colorKey, String editorKey, String overviewRulerKey, int layer) {
 		AnnotationPreference info= new AnnotationPreference(type, colorKey, editorKey, overviewRulerKey, layer);
 		fAnnotationTypeKeyMap.put(type, info);
 	}
 	
 	/**
 	 * Sets the preference info for the annotation painter.
 	 * @param info the preference info to be set
 	 */
 	public void setAnnotationPreference(AnnotationPreference info) {
 		fAnnotationTypeKeyMap.put(info.getAnnotationType(), info);
 	}
 	
 	/**
 	 * Sets the preference keys for the cursor line painter.
 	 * @param enableKey the preference key for the cursor line painter
 	 * @param colorKey the preference key for the color used by the cursor line
 	 *        painter
 	 */
 	public void setCursorLinePainterPreferenceKeys(String enableKey, String colorKey) {
 		fCursorLinePainterEnableKey= enableKey;
 		fCursorLinePainterColorKey= colorKey;
 	}
 	
 	/**
 	 * Sets the preference keys for the margin painter.
 	 * @param enableKey the preference key for the margin painter
 	 * @param colorKey the preference key for the color used by the margin
 	 *         painter
 	 * @param columnKey the preference key for the margin column
 	 */
 	public void setMarginPainterPreferenceKeys(String enableKey, String colorKey, String columnKey) {
 		fMarginPainterEnableKey= enableKey;
 		fMarginPainterColorKey= colorKey;
 		fMarginPainterColumnKey= columnKey;
 	}
 	
 	/**
 	 * Sets the preference keys for the matching character painter.
 	 * @param enableKey the preference key for the matching character painter
 	 * @param colorKey the preference key for the color used by the matching
 	 *         character  painter
 	 */
 	public void setMatchingCharacterPainterPreferenceKeys(String enableKey, String colorKey) {
 		fMatchingCharacterPainterEnableKey= enableKey;
 		fMatchingCharacterPainterColorKey= colorKey;
 	}
 	
 	/**
 	 * Sets the symbolic font name that is used for computing the margin width.
 	 * @param symbolicFontName
 	 */
 	public void setSymbolicFontName(String symbolicFontName) {
 		fSymbolicFontName= symbolicFontName;
 	}
 	
 	/**
 	 * Returns the annotation preference for the given key.
 	 * 
 	 * @param preferenceKey the preference key string
 	 * @return the annotation preference
 	 */
 	private AnnotationPreference getAnnotationPreferenceInfo(String preferenceKey) {
 		Iterator e= fAnnotationTypeKeyMap.values().iterator();
 		while (e.hasNext()) {
 			AnnotationPreference info= (AnnotationPreference) e.next();
 			if (info != null && info.isPreferenceKey(preferenceKey)) 
 				return info;
 		}
 		return null;
 	}
 	
 	
 	/*
 	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
  	 */
 	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
 		
 		String p= event.getProperty();		
 		
 		if (fMatchingCharacterPainterEnableKey != null && fMatchingCharacterPainterEnableKey.equals(p) && fCharacterPairMatcher != null) {
 			if (areMatchingCharactersShown())
 				showMatchingCharacters();
 			else
 				hideMatchingCharacters();
 			return;
 		}
 		
 		if (fMatchingCharacterPainterColorKey != null && fMatchingCharacterPainterColorKey.equals(p)) {
 			if (fMatchingCharacterPainter != null) {
 				fMatchingCharacterPainter.setColor(getColor(fMatchingCharacterPainterColorKey));
 				fMatchingCharacterPainter.paint(IPainter.CONFIGURATION);
 			}
 			return;
 		}
 		
 		if (fCursorLinePainterEnableKey != null && fCursorLinePainterEnableKey.equals(p)) {
 			if (isCursorLineShown())
 				showCursorLine();
 			else
 				hideCursorLine();
 			return;
 		}
 		
 		if (fCursorLinePainterColorKey != null && fCursorLinePainterColorKey.equals(p)) {
 			if (fCursorLinePainter != null) {
 				hideCursorLine();
 				showCursorLine();
 			}					
 			return;
 		}
 		
 		if (fMarginPainterEnableKey != null && fMarginPainterEnableKey.equals(p)) {
 			if (isMarginShown())
 				showMargin();
 			else
 				hideMargin();
 			return;
 		}
 		
 		if (fMarginPainterColorKey != null && fMarginPainterColorKey.equals(p)) {
 			if (fMarginPainter != null) {
 				fMarginPainter.setMarginRulerColor(getColor(fMarginPainterColorKey));
 				fMarginPainter.paint(IPainter.CONFIGURATION);
 			}
 			return;
 		}
 		
 		if (fMarginPainterColumnKey != null && fMarginPainterColumnKey.equals(p)) {
 			if (fMarginPainter != null && fPreferenceStore != null) {
 				fMarginPainter.setMarginRulerColumn(fPreferenceStore.getInt(fMarginPainterColumnKey));
 				fMarginPainter.paint(IPainter.CONFIGURATION);
 			}
 			return;
 		}
 		
 		AnnotationPreference info= getAnnotationPreferenceInfo(p);
 		if (info != null) {
 			
 			if (info.getColorPreferenceKey().equals(p)) {
 				Color color= getColor(info.getColorPreferenceKey());
 				if (fAnnotationPainter != null) {
 					fAnnotationPainter.setAnnotationTypeColor(info.getAnnotationType(), color);
 					fAnnotationPainter.paint(IPainter.CONFIGURATION);
 				}
 				setAnnotationOverviewColor(info.getAnnotationType(), color);
 				return;
 			}
 			
 			if (info.getTextPreferenceKey().equals(p) || info.getTextStylePreferenceKey() != null && info.getTextStylePreferenceKey().equals(p)) {
 				Object style= getAnnotationDecorationType(info.getAnnotationType());
 				if (AnnotationPreference.STYLE_NONE != style)
 					showAnnotations(info.getAnnotationType(), false, true);
 				else
 					hideAnnotations(info.getAnnotationType(), false, true);
 				return;
 			}
 			
 			if (info.getHighlightPreferenceKey() != null && info.getHighlightPreferenceKey().equals(p)) {
 				if (areAnnotationsHighlighted(info.getAnnotationType()))
 					showAnnotations(info.getAnnotationType(), true, true);
 				else
 					hideAnnotations(info.getAnnotationType(), true, true);
 				return;
 			}
 			
 			Object style= getAnnotationDecorationType(info.getAnnotationType());
 			if (style != AnnotationPreference.STYLE_NONE)
 				showAnnotations(info.getAnnotationType(), false, false);
 			else
 				hideAnnotations(info.getAnnotationType(), false, false);
 
 			if (info.getOverviewRulerPreferenceKey().equals(p)) {
 				if (isAnnotationOverviewShown(info.getAnnotationType()))
 					showAnnotationOverview(info.getAnnotationType(), true);
 				else
 					hideAnnotationOverview(info.getAnnotationType(), true);
 				return;
 			}
 		}			
 				
 	}
 	
 	/**
 	 * Returns the shared color for the given key.
 	 * 
 	 * @param key the color key string
 	 * @return the shared color for the given key
 	 */
 	private Color getColor(String key) {
 		if (fPreferenceStore != null) {
 			RGB rgb= PreferenceConverter.getColor(fPreferenceStore, key);
 			return getColor(rgb);
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the shared color for the given RGB.
 	 * 
 	 * @param rgb the RGB
 	 * @return the shared color for the given RGB
 	 */
 	private Color getColor(RGB rgb) {
 		return fSharedTextColors.getColor(rgb);
 	}
 	
 	/**
 	 * Returns the color of the given annotation type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @return the color of the annotation type
 	 */
 	private Color getAnnotationTypeColor(Object annotationType) {
 		AnnotationPreference info= (AnnotationPreference) fAnnotationTypeKeyMap.get(annotationType);
 		if (info != null)
 			return getColor( info.getColorPreferenceKey());
 		return null;
 	}
 	
 
 	
 	/**
 	 * Returns the layer of the given annotation type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @return the layer
 	 */
 	private int getAnnotationTypeLayer(Object annotationType) {
 		AnnotationPreference info= (AnnotationPreference) fAnnotationTypeKeyMap.get(annotationType);
 		if (info != null)
 			return info.getPresentationLayer();
 		return 0;
 	}
 	
 	/**
 	 * Enables showing of matching characters.
 	 */
 	private void showMatchingCharacters() {
 		if (fMatchingCharacterPainter == null) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				fMatchingCharacterPainter= new MatchingCharacterPainter(fSourceViewer, fCharacterPairMatcher);
 				fMatchingCharacterPainter.setColor(getColor(fMatchingCharacterPainterColorKey));
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.addPainter(fMatchingCharacterPainter);
 			}
 		}
 	}
 
 	/**
 	 * Disables showing of matching characters.
 	 */
 	private void hideMatchingCharacters() {
 		if (fMatchingCharacterPainter != null) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.removePainter(fMatchingCharacterPainter);
 				fMatchingCharacterPainter.deactivate(true);
 				fMatchingCharacterPainter.dispose();
 				fMatchingCharacterPainter= null;
 			}
 		}
 	}
 	
 	/**
 	 * Tells whether matching characters are shown.
 	 * 
 	 * @return <code>true</code> if the matching characters are shown
 	 */
 	private boolean areMatchingCharactersShown() {
 		if (fPreferenceStore != null && fMatchingCharacterPainterEnableKey != null)
 			return fPreferenceStore.getBoolean(fMatchingCharacterPainterEnableKey);
 		return false;
 	}
 	
 	/**
 	 * Shows the cursor line.
 	 */	
 	private void showCursorLine() {
 		if (fCursorLinePainter == null) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				fCursorLinePainter= new CursorLinePainter(fSourceViewer);
 				fCursorLinePainter.setHighlightColor(getColor(fCursorLinePainterColorKey));
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.addPainter(fCursorLinePainter);
 			}
 		}
 	}
 
 	/**
 	 * Hides the cursor line.
 	 */	
 	private void hideCursorLine() {
 		if (fCursorLinePainter != null) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.removePainter(fCursorLinePainter);
 				fCursorLinePainter.deactivate(true);
 				fCursorLinePainter.dispose();
 				fCursorLinePainter= null;
 			}
 		}
 	}
 	
 	/**
 	 * Tells whether the cursor line is shown.
 	 * 
 	 * @return <code>true</code> if the cursor line is shown
 	 */
 	private boolean isCursorLineShown() {
 		if (fPreferenceStore != null && fCursorLinePainterEnableKey != null)
 			return fPreferenceStore.getBoolean(fCursorLinePainterEnableKey);
 		return false;
 	}
 	
 	/**
 	 * Shows the margin.
 	 */	
 	private void showMargin() {
 		if (fMarginPainter == null) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				fMarginPainter= new MarginPainter(fSourceViewer);
 				fMarginPainter.setMarginRulerColor(getColor(fMarginPainterColorKey));
 				if (fPreferenceStore != null)
 					fMarginPainter.setMarginRulerColumn(fPreferenceStore.getInt(fMarginPainterColumnKey));
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.addPainter(fMarginPainter);
 				
 				fFontPropertyChangeListener= new FontPropertyChangeListener();
 				JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);
 			}
 		}
 	}
 
 	/**
 	 * Hides the margin.
 	 */	
 	private void hideMargin() {
 		if (fMarginPainter != null) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
 				fFontPropertyChangeListener= null;
 				
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.removePainter(fMarginPainter);
 				fMarginPainter.deactivate(true);
 				fMarginPainter.dispose();
 				fMarginPainter= null;
 			}
 		}
 	}
 
 	/**
 	 * Tells whether the margin is shown.
 	 * 
 	 * @return <code>true</code> if the margin is shown
 	 */	
 	private boolean isMarginShown() {
 		if (fPreferenceStore != null && fMarginPainterEnableKey != null)
 			return fPreferenceStore.getBoolean(fMarginPainterEnableKey);
 		return false;
 	}
 	
 	/**
 	 * Enables annotations in the source viewer for the given annotation type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @param highlighting <code>true</code> if highlighting <code>false</code> if painting squiggles
 	 * @param updatePainter if <code>true</code> update the annotation painter
 	 * @since 3.0
 	 */
 	private void showAnnotations(Object annotationType, boolean highlighting, boolean updatePainter) {
 		if (fSourceViewer instanceof ITextViewerExtension2) {
 			if (fAnnotationPainter == null) {
 				fAnnotationPainter= createAnnotationPainter();
 				if (fSourceViewer instanceof ITextViewerExtension4)
 					((ITextViewerExtension4)fSourceViewer).addTextPresentationListener(fAnnotationPainter);
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.addPainter(fAnnotationPainter);
 			}
 			fAnnotationPainter.setAnnotationTypeColor(annotationType, getAnnotationTypeColor(annotationType));
 			if (highlighting)
 				fAnnotationPainter.addHighlightAnnotationType(annotationType);
 			else
 				fAnnotationPainter.addAnnotationType(annotationType, getAnnotationDecorationType(annotationType));
 			
 			if (updatePainter)
 				updateAnnotationPainter();
 		}
 	}
 	
 	/**
 	 * Creates and configures the annotation painter and configures.
 	 * @return an annotation painter 
 	 * @since 3.0
 	 */
 	protected AnnotationPainter createAnnotationPainter() {
 		AnnotationPainter painter= new AnnotationPainter(fSourceViewer, fAnnotationAccess);
 		
 		// TODO add extension point for drawing strategies?
 		painter.addDrawingStrategy(AnnotationPreference.STYLE_BOX, fgBoxStrategy);
 		painter.addDrawingStrategy(AnnotationPreference.STYLE_NONE, fgNullStrategy);
 		painter.addDrawingStrategy(AnnotationPreference.STYLE_SQUIGGLES, fgSquigglesStrategy);
 		painter.addDrawingStrategy(AnnotationPreference.STYLE_UNDERLINE, fgUnderlineStrategy);
 		painter.addDrawingStrategy(AnnotationPreference.STYLE_IBEAM, fgIBeamStrategy);
 		
 		return painter;
 	}
 
 	/**
 	 * Updates the annotation painter.
 	 * @since 3.0
 	 */
 	private void updateAnnotationPainter() {
 		if (fAnnotationPainter == null)
 			return;
 		
 		fAnnotationPainter.paint(IPainter.CONFIGURATION);
 		if (!fAnnotationPainter.isPaintingAnnotations()) {
 			if (fSourceViewer instanceof ITextViewerExtension2) {
 				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
 				extension.removePainter(fAnnotationPainter);
 			}
 			if (fSourceViewer instanceof ITextViewerExtension4)
 				((ITextViewerExtension4)fSourceViewer).removeTextPresentationListener(fAnnotationPainter);
 			
 			fAnnotationPainter.deactivate(true);
 			fAnnotationPainter.dispose();
 			fAnnotationPainter= null;
 		}
 	}
 
 	/**
 	 * Hides annotations in the source viewer for the given annotation type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @param highlighting <code>true</code> if highlighting <code>false</code> if painting squiggles
 	 * @param updatePainter if <code>true</code> update the annotation painter
 	 * @since 3.0
 	 */
 	private void hideAnnotations(Object annotationType, boolean highlighting, boolean updatePainter) {
 		if (fAnnotationPainter != null) {
 			if (highlighting)
 				fAnnotationPainter.removeHighlightAnnotationType(annotationType);
 			else
 				fAnnotationPainter.removeAnnotationType(annotationType);
 			
 			if (updatePainter) {
 				updateAnnotationPainter();
 			}
 		}
 	}
 	
 	/**
 	 * Tells whether annotations are shown in the source viewer for the given type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @return <code>true</code> if the annotations are shown
 	 */	
 	private boolean areAnnotationsShown(Object annotationType) {
 		if (fPreferenceStore != null) {
 			AnnotationPreference info= (AnnotationPreference) fAnnotationTypeKeyMap.get(annotationType);
 			if (info != null) {
 				String key= info.getTextPreferenceKey();
 				return key != null && fPreferenceStore.getBoolean(key);
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Tells whether annotations are highlighted in the source viewer for the given type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @return <code>true</code> if the annotations are highlighted
 	 * @since 3.0
 	 */	
 	private boolean areAnnotationsHighlighted(Object annotationType) {
 		if (fPreferenceStore != null) {
 			AnnotationPreference info= (AnnotationPreference)fAnnotationTypeKeyMap.get(annotationType);
 			if (info != null)
 				return info.getHighlightPreferenceKey() != null && fPreferenceStore.getBoolean(info.getHighlightPreferenceKey());
 		}
 		return false;
 	}
 	
 	/**
 	 * Tells whether annotation overview is enabled for the given type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @return <code>true</code> if the annotation overview is shown
 	 */	
 	private boolean isAnnotationOverviewShown(Object annotationType) {
 		if (fPreferenceStore != null && fOverviewRuler != null) {
 			AnnotationPreference info= (AnnotationPreference) fAnnotationTypeKeyMap.get(annotationType);
 			if (info != null)
 				return fPreferenceStore.getBoolean(info.getOverviewRulerPreferenceKey());
 		}
 		return false;
 	}
 
 	/**
 	 * Enable annotation overview for the given annotation type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @param update <code>true</code> if the overview should be updated
 	 */	
 	private void showAnnotationOverview(Object annotationType, boolean update) {
 		if (fOverviewRuler != null) {
 			fOverviewRuler.setAnnotationTypeColor(annotationType, getAnnotationTypeColor(annotationType));
 			fOverviewRuler.setAnnotationTypeLayer(annotationType, getAnnotationTypeLayer(annotationType));
 			fOverviewRuler.addAnnotationType(annotationType);
 			if (update)
 				fOverviewRuler.update();
 		}
 	}
 	
 	/**
 	 * Hides the annotation overview for the given type.
 	 * @param annotationType the annotation type
 	 * @param update <code>true</code> if the overview should be updated
 	 */
 	private void hideAnnotationOverview(Object annotationType, boolean update) {
 		if (fOverviewRuler != null) {
 			fOverviewRuler.removeAnnotationType(annotationType);
 			if (update)
 				fOverviewRuler.update();
 		}
 	}
 	
 	/**
 	 * Hides the annotation overview.
 	 */
 	public void hideAnnotationOverview() {
 		if (fOverviewRuler != null) {
 			Iterator e= fAnnotationTypeKeyMap.keySet().iterator();
 			while (e.hasNext())
 				fOverviewRuler.removeAnnotationType(e.next());
 			fOverviewRuler.update();
 		}
 	}
 	
 	/**
 	 * Sets the annotation overview color for the given annotation type.
 	 * 
 	 * @param annotationType the annotation type
 	 * @param color the color
 	 */
 	private void setAnnotationOverviewColor(Object annotationType, Color color) {
 		if (fOverviewRuler != null) {
 			fOverviewRuler.setAnnotationTypeColor(annotationType, color);
 			fOverviewRuler.update();
 		}
 	}	
 }
