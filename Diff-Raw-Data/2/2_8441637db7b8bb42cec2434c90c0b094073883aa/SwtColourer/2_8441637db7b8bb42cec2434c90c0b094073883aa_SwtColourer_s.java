 package com.redcareditor.mate.colouring.swt;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CaretEvent;
 import org.eclipse.swt.custom.CaretListener;
 import org.eclipse.swt.custom.LineStyleEvent;
 import org.eclipse.swt.custom.LineStyleListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 
 import com.redcareditor.mate.DoublePattern;
 import com.redcareditor.mate.MateText;
 import com.redcareditor.mate.Scope;
 import com.redcareditor.mate.SinglePattern;
 import com.redcareditor.mate.colouring.Colourer;
 import com.redcareditor.theme.Theme;
 import com.redcareditor.theme.ThemeSetting;
 import com.redcareditor.util.swt.ColourUtil;
 
 public class SwtColourer implements Colourer {
 	private Theme theme;
 	private MateText mateText;
 
 	private int highlightedLine = 0;
 	private StyledText control;
 
 	/* cached swt colors */
 	private Color globalLineBackground;
 	private Color globalBackground;
 	private Color globalForeground;
 
 	public SwtColourer(MateText mt) {
 		mateText = mt;
 
 		control = mateText.getControl();
 
 		this.control.addLineStyleListener(new LineStyleListener() {
 			public void lineGetStyle(LineStyleEvent event) {
 				colourLine(event);
 			}
 		});
 
 		control.addCaretListener(new CaretListener() {
 			public void caretMoved(CaretEvent e) {
 				updateHighlightedLine(control.getLineAtOffset(e.caretOffset));
 			}
 		});
 	}
 
 	private void updateHighlightedLine(int line) {
 		if (caretLineHasChanged(line)) {
 			control.setLineBackground(line, 1, globalLineBackground);
 			control.setLineBackground(highlightedLine, 1, globalBackground);
 			highlightedLine = line;
 		}
 	}
 
 	private boolean caretLineHasChanged(int line) {
 		return line != highlightedLine;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.redcareditor.mate.Colourer#setTheme(com.redcareditor.theme.Theme)
 	 */
 	public void setTheme(Theme theme) {
 		this.theme = theme;
 		theme.initForUse();
 		initCachedColours();
 		setMateTextColors();
 	}
 
 	private void setMateTextColors() {
 		control.setBackground(globalBackground);
 		control.setForeground(globalForeground);
 		int currentLine = control.getLineAtOffset(control.getCaretOffset());
 		control.setLineBackground(currentLine, 1, globalLineBackground);
 		mateText.setGutterBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
 		mateText.setGutterForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
 	}
 
 	private void initCachedColours() {
 		globalBackground = ColourUtil.getColour(globalColour("background"));
 		globalForeground = ColourUtil.getColour(globalColour("foreground"));
 		globalLineBackground = ColourUtil.getColour(ColourUtil.mergeColour(globalColour("background"), globalColour("lineHighlight")));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.redcareditor.mate.Colourer#getTheme()
 	 */
 	public Theme getTheme() {
 		return theme;
 	}
 
 	private String globalColour(String name) {
 		String colour = theme.globalSettings.get(name);
 		if (isColorDefined(colour)) {
 			return colour;
 		} else {
 			return "#FFFFFF";
 		}
 	}
 
 	private boolean isColorDefined(String colour) {
		return colour != null && colour.isEmpty();
 	}
 
 	private void colourLine(LineStyleEvent event) {
 		if (theme == null)
 			return;
 		int eventLine = mateText.getControl().getLineAtOffset(event.lineOffset);
 		ArrayList<Scope> scopes = mateText.parser.root.scopesOnLine(eventLine);
 		System.out.printf("got to colour %d scopes\n", scopes.size());
 		ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
 		for (Scope scope : scopes) {
 			// System.out.printf("  %s\n", scope.name);
 			if (scope.parent == null) {
 				continue;
 			}
 			if (scope.name == null && scope.pattern != null
 					&& (scope.pattern instanceof SinglePattern || ((DoublePattern) scope.pattern).contentName == null)) {
 				continue;
 			}
 			styleRanges.add(getStyleRangeForScope(scope, false));
 			if (scope.pattern instanceof DoublePattern && ((DoublePattern) scope.pattern).contentName != null && scope.isCapture == false)
 				styleRanges.add(getStyleRangeForScope(scope, true));
 		}
 		// StyleRange[] styleRangeArray = new StyleRange[styleRanges.size()];
 		event.styles = (StyleRange[]) styleRanges.toArray(new StyleRange[0]);
 	}
 
 	private StyleRange getStyleRangeForScope(Scope scope, boolean inner) {
 		StyleRange styleRange = new StyleRange();
 		// TODO: allow for multiple settings that set different
 		// parts of the style.
 		ThemeSetting setting = null;
 		ThemeSetting excludeSetting = null;
 		if (scope.parent != null)
 			excludeSetting = scope.parent.themeSetting;
 		setting = theme.settingsForScope(scope, inner, null);// exclude_setting);
 
 		if (inner) {
 			styleRange.start = scope.getInnerStart().getOffset();
 			styleRange.length = scope.getInnerEnd().getOffset() - styleRange.start;
 		} else {
 			styleRange.start = scope.getStart().getOffset();
 			styleRange.length = scope.getEnd().getOffset() - styleRange.start;
 		}
 		System.out.printf("colour %s (%d, %d)\n", scope.name, styleRange.start, styleRange.length);
 		if (setting != null)
 			setStyleRangeProperties(scope, setting, styleRange);
 
 		return styleRange;
 	}
 
 	private void setStyleRangeProperties(Scope scope, ThemeSetting setting, StyleRange styleRange) {
 		String fontStyle = setting.settings.get("fontStyle");
 		if (fontStyle == "italic") {
 			styleRange.fontStyle = SWT.ITALIC;
 		} else
 			styleRange.fontStyle = SWT.NORMAL;
 
 		if (fontStyle == "underline")
 			styleRange.fontStyle = SWT.UNDERLINE_SINGLE;
 		else
 			styleRange.fontStyle = SWT.NORMAL;
 
 		String background = setting.settings.get("background");
 		System.out.printf("        scope background:        %s\n", background);
 		String mergedBgColour;
 		String parentBg = theme.globalSettings.get("background");
 		System.out.printf("        global background: %s\n", parentBg);
 		// TODO: wasn't this a better way of creating the background colours?
 		// var parent_bg = scope.nearest_background_colour();
 		// if (parent_bg == null) {
 		// }
 		// else {
 		// stdout.printf("        parent background: %s\n", parent_bg);
 		// }
 		if (background != null && background != "") {
 			// if (parent_bg != null) {
 			// merged_bg_colour = parent_bg;
 			// }
 			// else {
 			mergedBgColour = ColourUtil.mergeColour(parentBg, background);
 			// }
 			if (mergedBgColour != null) {
 				scope.bgColour = mergedBgColour;
 				styleRange.background = ColourUtil.getColour(mergedBgColour);
 				System.out.printf("       tag.background = %s\n", mergedBgColour);
 			}
 		} else {
 			mergedBgColour = parentBg;
 		}
 		// stdout.printf("        merged_bg_colour:  %s\n", merged_bg_colour);
 		String foreground = setting.settings.get("foreground");
 		// stdout.printf("        scope foreground:        %s\n", foreground);
 		String parentFg = scope.nearestForegroundColour();
 		if (parentFg == null) {
 			parentFg = theme.globalSettings.get("foreground");
 			// stdout.printf("        global foreground:        %s\n",
 			// parent_fg);
 		}
 		if (foreground != null && foreground != "") {
 			String mergedFgColour;
 			if (parentFg != null && !scope.isCapture)
 				mergedFgColour = ColourUtil.mergeColour(parentFg, foreground);
 			else
 				mergedFgColour = foreground;
 			if (mergedFgColour != null) {
 				scope.fgColour = mergedFgColour;
 				styleRange.foreground = ColourUtil.getColour(mergedFgColour);
 			}
 			// stdout.printf("       merged_fg_colour: %s\n", merged_fg_colour);
 		}
 		// stdout.printf("\n");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.redcareditor.mate.Colourer#uncolourScopes(java.util.List)
 	 */
 	public void uncolourScopes(List<Scope> scopes) {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.redcareditor.mate.Colourer#uncolourScope(com.redcareditor.mate.Scope,
 	 * boolean)
 	 */
 	public void uncolourScope(Scope scope, boolean something) {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.redcareditor.mate.Colourer#colourLineWithScopes(java.util.List)
 	 */
 	public void colourLineWithScopes(List<Scope> scopes) {
 
 	}
 }
