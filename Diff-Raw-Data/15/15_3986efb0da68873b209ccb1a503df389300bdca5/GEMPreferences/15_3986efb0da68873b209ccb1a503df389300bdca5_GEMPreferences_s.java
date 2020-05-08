 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.preference;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * This class represents a preference page that is contributed to the
  * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage </samp>,
  * we can use the field support built into JFace that allows us to create a page
  * that is small and knows how to save, restore and apply itself.
  * <p>
  * This page is used to modify preferences only. They are stored in the
  * preference store that belongs to the main plug-in class. That way,
  * preferences can be accessed directly via the preference store.
  */
 
 public final class GEMPreferences extends FieldEditorPreferencePage implements
 		IWorkbenchPreferencePage {
 	// appearance
     /**
      * Key for preference for whether or not the intro editor should be shown
      */
     public final static String SHOW_INTRO_EDITOR = "ShowIntroEditor"; //$NON-NLS-1$
     
 	/**
 	 * use system colors preference
 	 */
 	public final static String USE_SYSTEM_COLORS = "UseSystemColors"; //$NON-NLS-1$
 
 	/**
 	 * canvas color preference
 	 */
 	public final static String CANVAS_COLOR = "CanvasColor"; //$NON-NLS-1$
 
 	/**
 	 * figure label font
 	 */
 	public final static String FIGURE_LABEL_FONT = "FigureLabelFont"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String FIGURE_LABEL_FONT_COLOR = "FigureLabelFontColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LABEL_PLACEMENT = "LabelPlacement"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String INPUT_PORT_COLOR = "InputPortColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String OUTPUT_PORT_COLOR = "OutputPortColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String SHOW_LINE_LABELS = "ShowLineLabels"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LINE_LABEL_FONT = "LineLabelFont"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LINE_LABEL_FONT_COLOR = "LineLabelFontColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LINE_LABEL_COLOR = "LineLabelColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LINE_WIDTH = "LineWidth"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LINE_COLOR = "LineColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LINE_ROUTING = "LineRouting"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String SNAP_TO_GRID = "SnapToGrid"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String SNAP_TO_GEOMETRY = "SnapToGeometry"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String GRID_WIDTH = "GridWidth"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String GRID_HEIGHT = "GridHeight"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String GRID_COLOR = "GridColor"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LABEL_PLACEMENT_TOP = "Top"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LABEL_PLACEMENT_BOTTOM = "Bottom"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LABEL_PLACEMENT_LEFT = "Left"; //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public final static String LABEL_PLACEMENT_RIGHT = "Right"; //$NON-NLS-1$
 
 	// 
 	/**
 	 * "Direct" routing was intended for connections lines without bendpoints;
 	 * this has been removed because it is unnecessary.
 	 * public final static String LINE_ROUTING_DIRECT = "Direct";
 	 * "Manhattan" line routing creates orthogonal lines
 	 */
 	public final static String LINE_ROUTING_MANHATTAN = "Manhattan"; //$NON-NLS-1$
 
 	/**
 	 * "Manual" routing allows user to create bendpoints
 	 */
 	public final static String LINE_ROUTING_MANUAL = "Manaul"; //$NON-NLS-1$
 
 	private final static String[][] m_lineRoutingLabels = {
 			// display, key
 			// { GEMPlugin.getResourceString("CanvasPreferences.LABEL.Direct"),
 			// LINE_ROUTING_DIRECT }, //$NON-NLS-1$ //$NON-NLS-2$
 			{
 				PreferenceMessages.CanvasPreferences_LABEL_Manual, LINE_ROUTING_MANUAL },
 			{
 					PreferenceMessages.CanvasPreferences_LABEL_Manhattan, LINE_ROUTING_MANHATTAN }
 	};
 
 	private final static String[][] m_labelPlacementLabels = {
 			{
 				PreferenceMessages.CanvasPreferences_LABEL_Top, LABEL_PLACEMENT_TOP },
 			{
 					PreferenceMessages.CanvasPreferences_LABEL_Bottom, LABEL_PLACEMENT_BOTTOM },
 			{
 						PreferenceMessages.CanvasPreferences_LABEL_Left, LABEL_PLACEMENT_LEFT },
 			{
 							PreferenceMessages.CanvasPreferences_LABEL_Right, LABEL_PLACEMENT_RIGHT }
 	};
 
 	private Group canvasGroup;
 
 	private Group iconGroup;
 
 	private Group lineGroup;
 
 	private BooleanField useSystemColors;
 
 	private ColorFieldEditor canvasColor;
 
 	private BooleanField snapToGrid;
 
 	private IntegerFieldEditor gridWidth;
 
 	private IntegerFieldEditor gridHeight;
 
 	private ColorFieldEditor gridColor;
 
 	private BooleanField showLineLabels;
 
 	private ColorFieldEditor lineLabelColor;
 
 	private ColorFieldEditor lineColor;
 
 	private ColorFontFieldEditor iconFont;
 
 	private ColorFontFieldEditor lineFont;
 
 	// private ColorFieldEditor inputPortColor;
 	//
 	// private ColorFieldEditor outputPortColor;
 
 	// CR392586: resource leaks
 	// at least keep leaks bounded...
 //	private static Hashtable resourceRegistry = new Hashtable();
 
 	private class BooleanField extends BooleanFieldEditor {
 		private Composite parent;
 
 		/**
 		 * @param name
 		 * @param label
 		 * @param parent
 		 */
 		public BooleanField(String name, String label, Composite parent) {
 			super(name, label, parent);
 			this.parent = parent;
 		}
 
 		/**
 		 * @return the change control button
 		 */
 		public Button getButton() {
 			return getChangeControl(parent);
 		}
 	}
 
 	/**
 	 * Constructor
 	 */
 	public GEMPreferences() {
 		super(GRID);
 		// FIXME: we should be encapsulating what pref store is used for all callers of this class
 		setPreferenceStore(EditorPlugin.getDefault().getPreferenceStore());
 		setDescription(PreferenceMessages.GEMPreferences_description);
		initializeDefaults();
 	}
 
 	/**
 	 * Sets the default values of the preferences.
 	 */
	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
 		Font f = JFaceResources.getFontRegistry().get(
 				JFaceResources.DEFAULT_FONT);
 
 		store.setDefault(SHOW_INTRO_EDITOR, true);
 		store.setDefault(USE_SYSTEM_COLORS, true);
 		PreferenceConverter.setDefault(store, CANVAS_COLOR, new RGB(255, 255,
 				255));
 		PreferenceConverter.setDefault(store, FIGURE_LABEL_FONT, f
 				.getFontData());
 		store.setDefault(LINE_WIDTH, 1);
 		store.setDefault(LINE_ROUTING, getLineRoutingLabels()[0][1]);
 		store.setDefault(SHOW_LINE_LABELS, true);
 		PreferenceConverter.setDefault(store, LINE_COLOR, new RGB(0, 0, 0));
 		PreferenceConverter.setDefault(store, LINE_LABEL_COLOR, new RGB(255,
 				255, 255));
 		PreferenceConverter.setDefault(store, LINE_LABEL_FONT, f.getFontData());
 		store.setDefault(SNAP_TO_GEOMETRY, true);
 		store.setDefault(SNAP_TO_GRID, true);
 		store.setDefault(GRID_WIDTH, 12);
 		store.setDefault(GRID_HEIGHT, 12);
 		PreferenceConverter.setDefault(store, GRID_COLOR,
 				new RGB(230, 230, 230));
 	}
 
 	/**
 	 * Creates the field editors. Field editors are abstractions of the common
 	 * GUI blocks needed to manipulate various types of preferences. Each field
 	 * editor knows how to save and restore itself.
 	 */
 
 	public void createFieldEditors() {
 	    
 	    // note, we aren't saving the reference.  It's assumed that parent
 	    // worries about destruction, persistence etc.
 	    addBooleanField(
 	            SHOW_INTRO_EDITOR,
 	            PreferenceMessages.EditorPreferences_LABEL_ShowIntroEditor,
 	            getFieldEditorParent());
 	            
 		useSystemColors = addBooleanField(
 				USE_SYSTEM_COLORS,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_UseSystemColors,
 				getFieldEditorParent());
 
 		canvasGroup = new Group(getFieldEditorParent(), SWT.NULL);
 		lineGroup = new Group(getFieldEditorParent(), SWT.NULL);
 		iconGroup = new Group(getFieldEditorParent(), SWT.NULL);
 
 		canvasGroup.setText(PreferenceMessages.CanvasPreferenceTab_LABEL_Canvas);
 		canvasColor = addColorField(
 				CANVAS_COLOR,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_BackgroundColor, canvasGroup);
 		addBooleanField(
 				SNAP_TO_GEOMETRY,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_SnapToGeometry, canvasGroup);
 		snapToGrid = addBooleanField(
 				SNAP_TO_GRID,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_SnapToGrid, canvasGroup);
 		gridColor = addColorField(
 				GRID_COLOR,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_GridLineColor, canvasGroup);
 		gridWidth = addIntegerField(
 				GRID_WIDTH,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_GridWidth, canvasGroup);
 		gridHeight = addIntegerField(
 				GRID_HEIGHT,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_GridHeight, canvasGroup);
 
 		iconGroup.setText(PreferenceMessages.CanvasPreferenceTab_LABEL_IconGroup);
 		iconFont = addFontField(
 				FIGURE_LABEL_FONT,
 				PreferenceMessages.CanvasPreferenceTab_LABEL_IconLabelFont, iconGroup);
 		addComboField(LABEL_PLACEMENT, PreferenceMessages.CanvasPreferenceTab_LABEL_LabelPlacement,
 				getLabelPlacementLabels(), iconGroup);
 		// inputPortColor = addColorField(
 		// INPUT_PORT_COLOR,
 		// EditorPlugin
 		// .getResourceString("CanvasPreferenceTab.LABEL.InputPortColor"),
 		// iconGroup); //$NON-NLS-1$
 		// outputPortColor = addColorField(
 		// OUTPUT_PORT_COLOR,
 		// EditorPlugin
 		// .getResourceString("CanvasPreferenceTab.LABEL.OutputPortColor"),
 		// iconGroup); //$NON-NLS-1$
 
 		lineGroup.setText(PreferenceMessages.CanvasPreferenceTab_LABEL_LineGroup);
 		showLineLabels = addBooleanField(SHOW_LINE_LABELS, PreferenceMessages.CanvasPreferenceTab_LABEL_ShowLineLabels,
 				lineGroup);
 		lineFont = addFontField(
 				LINE_LABEL_FONT,
 				 PreferenceMessages.CanvasPreferenceTab_LABEL_LineLabelFont, lineGroup);
 		lineLabelColor = addColorField(
 				LINE_LABEL_COLOR,
 				 PreferenceMessages.CanvasPreferenceTab_LABEL_LineLabelColor, lineGroup);
 		lineColor = addColorField(
 				LINE_COLOR,
 				 PreferenceMessages.CanvasPreferenceTab_LABEL_LineColor, lineGroup);
 		addIntegerField(
 				LINE_WIDTH,
 				 PreferenceMessages.CanvasPreferenceTab_LABEL_LineWidth, lineGroup);
 		addComboField(LINE_ROUTING,  PreferenceMessages.CanvasPreferenceTab_LABEL_LineRouting,
 				getLineRoutingLabels(), lineGroup);
 	}
 
 	protected void initialize() {
 		// Color use: Default canvas colors should pick up system defaults
 		// enable or disable all of the color and font selection controls in the
 		// preference dialog
 		// depending on whether the "Use System Colors" checkbox is selected.
 		super.initialize();
 
 		((GridLayout) getFieldEditorParent().getLayout()).numColumns = 2;
 
 		canvasGroup.setLayout(new GridLayout(3, false));
 		canvasGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_BEGINNING));
 		canvasColor.fillIntoGrid(canvasGroup, 3);
 		gridColor.fillIntoGrid(canvasGroup, 3);
 
 		iconGroup.setLayout(new GridLayout(3, false));
 		iconGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_BEGINNING));
 		iconFont.fillIntoGrid(iconGroup, 3);
 
 		lineGroup.setLayout(new GridLayout(3, false));
 		lineGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
 				| GridData.VERTICAL_ALIGN_BEGINNING));
 		lineColor.fillIntoGrid(lineGroup, 3);
 		lineLabelColor.fillIntoGrid(lineGroup, 3);
 		lineFont.fillIntoGrid(lineGroup, 3);
 
 		boolean userColorsValue = !useSystemColors.getBooleanValue();
 		boolean showLineLabelsValue = showLineLabels.getBooleanValue();
 		boolean snapToGridValue = snapToGrid.getBooleanValue();
 		canvasColor.setEnabled(userColorsValue, canvasGroup);
 		gridColor.setEnabled(snapToGridValue && userColorsValue, canvasGroup);
 		iconFont.setEnabled(userColorsValue, iconGroup);
 		// inputPortColor.setEnabled(userColorsValue, iconGroup);
 		// outputPortColor.setEnabled(userColorsValue, iconGroup);
 		lineColor.setEnabled(userColorsValue, lineGroup);
 		lineLabelColor.setEnabled(showLineLabelsValue && userColorsValue,
 				lineGroup);
 		lineFont.setEnabled(showLineLabelsValue && userColorsValue, lineGroup);
 		gridWidth.setEnabled(snapToGridValue, canvasGroup);
 		gridHeight.setEnabled(snapToGridValue, canvasGroup);
 
 		useSystemColors.getButton().addSelectionListener(
 				new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						boolean userColorsValue_ = !useSystemColors
 								.getBooleanValue();
 						boolean showLineLabelsValue_ = showLineLabels
 								.getBooleanValue();
 						boolean snapToGridValue_ = snapToGrid.getBooleanValue();
 
 						canvasColor.setEnabled(userColorsValue_, canvasGroup);
 						gridColor
 								.setEnabled(snapToGridValue_ && userColorsValue_,
 										canvasGroup);
 						iconFont.setEnabled(userColorsValue_, iconGroup);
 						// inputPortColor.setEnabled(userColorsValue,
 						// iconGroup);
 						// outputPortColor.setEnabled(userColorsValue,
 						// iconGroup);
 						lineColor.setEnabled(userColorsValue_, lineGroup);
 						lineLabelColor.setEnabled(showLineLabelsValue_
 								&& userColorsValue_, lineGroup);
 						lineFont.setEnabled(showLineLabelsValue_
 								&& userColorsValue_, lineGroup);
 					}
 				});
 
 		showLineLabels.getButton().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				boolean userColorsValue_ = !useSystemColors.getBooleanValue();
 				boolean showLineLabelsValue_ = showLineLabels.getBooleanValue();
 				lineLabelColor.setEnabled(showLineLabelsValue_
 						&& userColorsValue_, lineGroup);
 				lineFont.setEnabled(showLineLabelsValue_ && userColorsValue_,
 						lineGroup);
 			}
 		});
 
 		snapToGrid.getButton().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				boolean userColorsValue_ = !useSystemColors.getBooleanValue();
 				boolean snapToGridValue_ = snapToGrid.getBooleanValue();
 
 				gridColor.setEnabled(snapToGridValue_ && userColorsValue_,
 						canvasGroup);
 				gridWidth.setEnabled(snapToGridValue_, canvasGroup);
 				gridHeight.setEnabled(snapToGridValue_, canvasGroup);
 			}
 		});
 
 	}
 
 	public void init(IWorkbench workbench) {
         // no initialization
 	}
 
 	private ColorFieldEditor addColorField(String name, String labelText,
 			Composite parent) {
 		ColorFieldEditor f = new ColorFieldEditor(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 	private ComboFieldEditor addComboField(String name, String labelText,
 			String[][] entryNamesAndValues, Composite parent) {
 		ComboFieldEditor f = new ComboFieldEditor(name, labelText,
 				entryNamesAndValues, parent);
 		addField(f);
 		return f;
 	}
 
 	private IntegerFieldEditor addIntegerField(String name, String labelText,
 			Composite parent) {
 		IntegerFieldEditor f = new IntegerFieldEditor(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 	private BooleanField addBooleanField(String name, String labelText,
 			Composite parent) {
 		BooleanField f = new BooleanField(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 //	private StringFieldEditor addStringField(String name, String labelText,
 //			Composite parent) {
 //		StringFieldEditor f = new StringFieldEditor(name, labelText, parent);
 //		addField(f);
 //		return f;
 //	}
 
 	// protected NumberField addNumberField(String name, String labelText,
 	// Composite parent)
 	// {
 	// NumberField f = new NumberField(name,labelText,parent);
 	// addField(f);
 	// return f;
 	// }
 
 	private ColorFontFieldEditor addFontField(String name, String labelText,
 			Composite parent) {
 		ColorFontFieldEditor f = new ColorFontFieldEditor(name, labelText,
 				parent);
 		addField(f);
 		return f;
 	}
 
 	private static String[][] getLineRoutingLabels() {
 		return m_lineRoutingLabels;
 	}
 
 	private static String[][] getLabelPlacementLabels() {
 		return m_labelPlacementLabels;
 	}
 
 //	private static void propagateProperty(String property, EditPart part) {
 //		Iterator iter = part.getChildren().iterator();
 //		while (iter.hasNext()) {
 //			EditPart child = (EditPart) iter.next();
 //			Figure fig = (Figure) ((GraphicalEditPart) child).getFigure();
 //			GEMPreferences.propagateProperty(property, fig);
 //			propagateProperty(property, child);
 //		}
 //	}
 
 	/**
 	 * @return true if the preference is set to show the introduction
 	 * editor.  false otherwise.
 	 */
 	public static boolean getShowIntroEditor()
 	{
 	    IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
 	    return store.getBoolean(SHOW_INTRO_EDITOR);
 	}
 	
 	/**
 	 * @param store
 	 * @param property
 	 * @return the color property for the key 'property'
 	 */
 	public static Color getColor(IPreferenceStore store, String property) {
 		boolean useSystemColors = store.getBoolean(USE_SYSTEM_COLORS);
 
 		Color c = ColorConstants.black;
 		if (useSystemColors) {
 			if (GRID_COLOR.equals(property))
 				// c = ColorConstants.buttonDarkest;
 				c = ColorConstants.button;
 			if (LINE_COLOR.equals(property))
 				c = ColorConstants.listForeground;
 			if (LINE_LABEL_FONT_COLOR.equals(property))
 				c = ColorConstants.listForeground;
 			if (LINE_LABEL_COLOR.equals(property))
 				c = ColorConstants.listBackground;
 			if (CANVAS_COLOR.equals(property))
 				c = ColorConstants.listBackground;
 			if (INPUT_PORT_COLOR.equals(property))
 				c = ColorConstants.listForeground;
 			if (OUTPUT_PORT_COLOR.equals(property))
 				c = ColorConstants.listForeground;
 			if (FIGURE_LABEL_FONT_COLOR.equals(property))
 				c = ColorConstants.listForeground;
 		} else {
 			// CR392586: resource leaks
 			RGB rgb = PreferenceConverter.getColor(store, property);
 			ColorRegistry registry = JFaceResources.getColorRegistry();
 			if (registry.get(rgb.toString()) != null)
 				return registry.get(rgb.toString());
 			registry.put(rgb.toString(), rgb);
 			c = registry.get(rgb.toString());
 		}
 		return c;
 	}
 
 //	// CR392586: resource leaks
 //	private static Font getFont(IPreferenceStore store, String property) {
 //		FontData fd = PreferenceConverter.getFontData(store, property);
 //		FontRegistry registry = JFaceResources.getFontRegistry();
 //		if (!registry.get(fd.toString()).equals(registry.defaultFont()))
 //			return registry.get(fd.toString());
 //
 //		registry.put(fd.toString(), new FontData[] {fd});
 //		return registry.get(fd.toString());
 //	}
 
 //	private static void propagateProperty(String property, Figure fig) {
 //		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
 //		WindowFigure window = null;
 //		IconFigure icon = null;
 //		LinkFigure link = null;
 //		if (fig instanceof CompoundNodeFigure) {
 //			window = ((CompoundNodeFigure) fig).getWindowFigure();
 //			icon = ((CompoundNodeFigure) fig).getIconFigure();
 //		} else if (fig instanceof WindowFigure)
 //			window = (WindowFigure) fig;
 //		else if (fig instanceof LinkFigure)
 //			link = (LinkFigure) fig;
 //
 //		if (property != null && property.equals(USE_SYSTEM_COLORS))
 //			// reload all properties - it's easiest
 //			property = null;
 //
 //		if (property == null || SNAP_TO_GRID.equals(property)) {
 //			boolean b = store.getBoolean(SNAP_TO_GRID);
 //			WindowFigure.defaultGridEnabled = b;
 //
 //			if (window != null)
 //				window.getGridLayer().setVisible(b);
 //		}
 //
 //		if (property == null || GRID_WIDTH.equals(property)
 //				|| GRID_HEIGHT.equals(property)) {
 //			Dimension d = new Dimension(store.getInt(GRID_WIDTH), store
 //					.getInt(GRID_HEIGHT));
 //			WindowFigure.defaultGridSpacing = d;
 //
 //			if (window != null)
 //				window.getGridLayer().setSpacing(d);
 //		}
 //
 //		if (property == null || GRID_COLOR.equals(property)) {
 //			Color c = getColor(store, GRID_COLOR);
 //			WindowFigure.defaultGridColor = c;
 //
 //			if (window != null)
 //				window.getGridLayer().setForegroundColor(c);
 //		}
 //
 //		// TODO: since the line router is managed by the EditPart for the
 //		// container figure, setting the line routing style in the WindowFigure
 //		// does not change the line routing immediately. The editor must be
 //		// restarted for line routing to take effect.
 //		if (property == null || LINE_ROUTING.equals(property)) {
 //			String s = store.getString(LINE_ROUTING);
 //			int style;
 //			if (LINE_ROUTING_MANHATTAN.equals(s))
 //				style = WindowFigure.LINE_ROUTING_MANHATTAN;
 //			else
 //				style = WindowFigure.LINE_ROUTING_MANUAL;
 //
 //			WindowFigure.defaultLineRoutingStyle = style;
 //			if (window != null)
 //				window.setLineRoutingStyle(style);
 //		}
 //
 //		if (property == null || LINE_WIDTH.equals(property)) {
 //			int w = store.getInt(LINE_WIDTH);
 //			LinkFigure.defaultLineWidth = w;
 //
 //			if (link != null)
 //				link.setLineWidth(w);
 //		}
 //
 //		if (property == null || LINE_COLOR.equals(property)) {
 //			Color c = getColor(store, LINE_COLOR);
 //			LinkFigure.defaultLineColor = c;
 //
 //			if (link != null)
 //				link.setForegroundColor(c);
 //		}
 //
 //		if (property == null || SHOW_LINE_LABELS.equals(property)) {
 //			boolean b = store.getBoolean(SHOW_LINE_LABELS);
 //			LinkFigure.defaultLabelVisible = b;
 //
 //			if (link != null)
 //				link.setLabelVisible(b);
 //		}
 //
 //		if (property == null || LINE_LABEL_FONT.equals(property)
 //				|| LINE_LABEL_FONT_COLOR.equals(property)) {
 //			// CR392586: resource leaks
 //			Font f = getFont(store, LINE_LABEL_FONT);
 //			Color c = getColor(store, LINE_LABEL_FONT_COLOR);
 //			LinkFigure.defaultFont = f;
 //			LinkFigure.defaultLabelForeground = c;
 //
 //			if (link != null) {
 //				link.setFont(f);
 //				link.setLabelForeground(c);
 //			}
 //		}
 //
 //		if (property == null || LINE_LABEL_COLOR.equals(property)) {
 //			Color c = getColor(store, LINE_LABEL_COLOR);
 //			LinkFigure.defaultLabelBackground = c;
 //
 //			if (link != null)
 //				link.setLabelBackground(c);
 //		}
 //
 //		if (property == null || CANVAS_COLOR.equals(property)) {
 //			Color c = getColor(store, CANVAS_COLOR);
 //			WindowFigure.defaultBackgroundColor = c;
 //
 //			if (window != null)
 //				window.setBackgroundColor(c);
 //			if (icon != null)
 //				icon.setBackgroundColor(c);
 //		}
 //
 //		if (property == null || INPUT_PORT_COLOR.equals(property)) {
 //			Color c = getColor(store, INPUT_PORT_COLOR);
 //			InputPortFigure.defaultForegroundColor = c;
 //
 //			if (fig instanceof InputPortFigure)
 //				fig.setForegroundColor(c);
 //		}
 //
 //		if (property == null || OUTPUT_PORT_COLOR.equals(property)) {
 //			Color c = getColor(store, OUTPUT_PORT_COLOR);
 //			OutputPortFigure.defaultForegroundColor = c;
 //
 //			if (fig instanceof OutputPortFigure)
 //				fig.setForegroundColor(c);
 //		}
 //
 //		if (property == null || FIGURE_LABEL_FONT.equals(property)
 //				|| FIGURE_LABEL_FONT_COLOR.equals(property)) {
 //			// CR392586: resource leaks
 //			Font f = getFont(store, FIGURE_LABEL_FONT);
 //			Color c = getColor(store, FIGURE_LABEL_FONT_COLOR);
 //			IconFigure.defaultFont = f;
 //			IconFigure.defaultForegroundColor = c;
 //			WindowFigure.defaultFont = f;
 //			WindowFigure.defaultForegroundColor = c;
 //
 //			if (window != null) {
 //				window.setFont(f);
 //				window.setForegroundColor(c);
 //			}
 //			if (icon != null) {
 //				icon.setFont(f);
 //				icon.setForegroundColor(c);
 //			}
 //			if (fig instanceof IconFigure) {
 //				fig.setFont(f);
 //				fig.setForegroundColor(c);
 //			}
 //		}
 //
 //		if (property == null || LABEL_PLACEMENT.equals(property)) {
 //			int placement = PositionConstants.SOUTH;
 //			String s = store.getString(LABEL_PLACEMENT);
 //			if (LABEL_PLACEMENT_TOP.equals(s))
 //				placement = PositionConstants.NORTH;
 //			if (LABEL_PLACEMENT_BOTTOM.equals(s))
 //				placement = PositionConstants.SOUTH;
 //			if (LABEL_PLACEMENT_LEFT.equals(s))
 //				placement = PositionConstants.WEST;
 //			if (LABEL_PLACEMENT_RIGHT.equals(s))
 //				placement = PositionConstants.EAST;
 //			IconFigure.defaultTextPlacement = placement;
 //
 //			if (icon != null)
 //				icon.setTextPlacement(placement);
 //			if (fig instanceof IconFigure)
 //				((IconFigure) fig).setTextPlacement(placement);
 //		}
 //
 //		Iterator iter = fig.getChildren().iterator();
 //		while (iter.hasNext()) {
 //			Figure child = (Figure) iter.next();
 //			propagateProperty(property, child);
 //		}
 //	}
 }
