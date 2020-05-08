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
 
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
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
 
 public class GEMPreferences extends FieldEditorPreferencePage implements
 		IWorkbenchPreferencePage {
 	// appearance
 	public final static String USE_SYSTEM_COLORS = "UseSystemColors"; //$NON-NLS-1$
 
 	public final static String CANVAS_COLOR = "CanvasColor"; //$NON-NLS-1$
 
 	public final static String FIGURE_LABEL_FONT = "FigureLabelFont"; //$NON-NLS-1$
 
 	public final static String FIGURE_LABEL_FONT_COLOR = "FigureLabelFontColor"; //$NON-NLS-1$
 
 	public final static String LABEL_PLACEMENT = "LabelPlacement";
 
 	public final static String INPUT_PORT_COLOR = "InputPortColor";
 
 	public final static String OUTPUT_PORT_COLOR = "OutputPortColor";
 
 	public final static String SHOW_LINE_LABELS = "ShowLineLabels";
 
 	public final static String LINE_LABEL_FONT = "LineLabelFont"; //$NON-NLS-1$
 
 	public final static String LINE_LABEL_FONT_COLOR = "LineLabelFontColor"; //$NON-NLS-1$
 
 	public final static String LINE_LABEL_COLOR = "LineLabelColor"; //$NON-NLS-1$
 
 	public final static String LINE_WIDTH = "LineWidth"; //$NON-NLS-1$
 
 	public final static String LINE_COLOR = "LineColor"; //$NON-NLS-1$
 
 	public final static String LINE_ROUTING = "LineRouting"; //$NON-NLS-1$
 
 	public final static String SNAP_TO_GRID = "SnapToGrid"; //$NON-NLS-1$
 
 	public final static String SNAP_TO_GEOMETRY = "SnapToGeometry"; //$NON-NLS-1$
 
 	public final static String GRID_WIDTH = "GridWidth"; //$NON-NLS-1$
 
 	public final static String GRID_HEIGHT = "GridHeight"; //$NON-NLS-1$
 
 	public final static String GRID_COLOR = "GridColor"; //$NON-NLS-1$
 
 	public final static String LABEL_PLACEMENT_TOP = "Top";
 
 	public final static String LABEL_PLACEMENT_BOTTOM = "Bottom";
 
 	public final static String LABEL_PLACEMENT_LEFT = "Left";
 
 	public final static String LABEL_PLACEMENT_RIGHT = "Right";
 
 	// "Direct" routing was intended for connections lines without bendpoints;
 	// this has been removed because it is unnecessary.
 	// public final static String LINE_ROUTING_DIRECT = "Direct";
 	// "Manhattan" line routing creates orthogonal lines
 	public final static String LINE_ROUTING_MANHATTAN = "Manhattan";
 
 	// "Manual" routing allows user to create bendpoints
 	public final static String LINE_ROUTING_MANUAL = "Manaul";
 
 	private final static String[][] m_lineRoutingLabels = {
 			// display, key
 			// { GEMPlugin.getResourceString("CanvasPreferences.LABEL.Direct"),
 			// LINE_ROUTING_DIRECT }, //$NON-NLS-1$ //$NON-NLS-2$
 			{
 					EditorPlugin
 							.getResourceString("CanvasPreferences.LABEL.Manual"), LINE_ROUTING_MANUAL }, //$NON-NLS-1$ //$NON-NLS-2$
 			{
 					EditorPlugin
 							.getResourceString("CanvasPreferences.LABEL.Manhattan"), LINE_ROUTING_MANHATTAN } //$NON-NLS-1$ //$NON-NLS-2$
 	};
 
 	private final static String[][] m_labelPlacementLabels = {
 			{
 					EditorPlugin
 							.getResourceString("CanvasPreferences.LABEL.Top"), LABEL_PLACEMENT_TOP }, //$NON-NLS-1$ //$NON-NLS-2$
 			{
 					EditorPlugin
 							.getResourceString("CanvasPreferences.LABEL.Bottom"), LABEL_PLACEMENT_BOTTOM }, //$NON-NLS-1$ //$NON-NLS-2$
 			{
 					EditorPlugin
 							.getResourceString("CanvasPreferences.LABEL.Left"), LABEL_PLACEMENT_LEFT }, //$NON-NLS-1$ //$NON-NLS-2$
 			{
 					EditorPlugin
 							.getResourceString("CanvasPreferences.LABEL.Right"), LABEL_PLACEMENT_RIGHT } //$NON-NLS-1$ //$NON-NLS-2$
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
 
 	private ColorFieldEditor inputPortColor;
 
 	private ColorFieldEditor outputPortColor;
 
 	// CR392586: resource leaks
 	// at least keep leaks bounded...
 	private static Hashtable resourceRegistry = new Hashtable();
 
 	private class BooleanField extends BooleanFieldEditor {
 		private Composite parent;
 
 		public BooleanField(String name, String label, Composite parent) {
 			super(name, label, parent);
 			this.parent = parent;
 		}
 
 		public Button getButton() {
 			return getChangeControl(parent);
 		}
 	}
 
 	public GEMPreferences() {
 		super(GRID);
 		setPreferenceStore(EditorPlugin.getDefault().getPreferenceStore());
 		setDescription("Preferences for the graphical page of FacesConfig editor.");
 		initializeDefaults();
 	}
 
 	/**
 	 * Sets the default values of the preferences.
 	 */
 	private void initializeDefaults() {
 		IPreferenceStore store = getPreferenceStore();
 		Font f = JFaceResources.getFontRegistry().get(
 				JFaceResources.DEFAULT_FONT);
 
 		store.setDefault(USE_SYSTEM_COLORS, true);
 		PreferenceConverter.setDefault(store, CANVAS_COLOR, new RGB(255, 255,
 				255));
 		PreferenceConverter.setDefault(store, FIGURE_LABEL_FONT, f
 				.getFontData());
 		store.setDefault(LINE_WIDTH, 1);
 		store.setDefault(LINE_ROUTING, getLineRoutingLabels()[0][1]);
		store.setDefault(SHOW_LINE_LABELS, false);
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
 		useSystemColors = addBooleanField(
 				USE_SYSTEM_COLORS,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.UseSystemColors"),
 				getFieldEditorParent());
 
 		canvasGroup = new Group(getFieldEditorParent(), SWT.NULL);
 		lineGroup = new Group(getFieldEditorParent(), SWT.NULL);
 		iconGroup = new Group(getFieldEditorParent(), SWT.NULL);
 
 		canvasGroup.setText(EditorPlugin
 				.getResourceString("CanvasPreferenceTab.LABEL.Canvas")); //$NON-NLS-1$
 		canvasColor = addColorField(
 				CANVAS_COLOR,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.BackgroundColor"), canvasGroup); //$NON-NLS-1$
 		addBooleanField(
 				SNAP_TO_GEOMETRY,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.SnapToGeometry"), canvasGroup); //$NON-NLS-1$
 		snapToGrid = addBooleanField(
 				SNAP_TO_GRID,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.SnapToGrid"), canvasGroup); //$NON-NLS-1$
 		gridColor = addColorField(
 				GRID_COLOR,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.GridLineColor"), canvasGroup); //$NON-NLS-1$
 		gridWidth = addIntegerField(
 				GRID_WIDTH,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.GridWidth"), canvasGroup); //$NON-NLS-1$
 		gridHeight = addIntegerField(
 				GRID_HEIGHT,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.GridHeight"), canvasGroup); //$NON-NLS-1$
 
 		iconGroup.setText(EditorPlugin
 				.getResourceString("CanvasPreferenceTab.LABEL.IconGroup")); //$NON-NLS-1$
 		iconFont = addFontField(
 				FIGURE_LABEL_FONT,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.IconLabelFont"), iconGroup); //$NON-NLS-1$
 		addComboField(LABEL_PLACEMENT, EditorPlugin
 				.getResourceString("CanvasPreferenceTab.LABEL.LabelPlacement"), //$NON-NLS-1$
 				getLabelPlacementLabels(), iconGroup);
 		inputPortColor = addColorField(
 				INPUT_PORT_COLOR,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.InputPortColor"), iconGroup); //$NON-NLS-1$
 		outputPortColor = addColorField(
 				OUTPUT_PORT_COLOR,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.OutputPortColor"), iconGroup); //$NON-NLS-1$
 
 		lineGroup.setText(EditorPlugin
 				.getResourceString("CanvasPreferenceTab.LABEL.LineGroup")); //$NON-NLS-1$
 		showLineLabels = addBooleanField(SHOW_LINE_LABELS, EditorPlugin
 				.getResourceString("CanvasPreferenceTab.LABEL.ShowLineLabels"),
 				lineGroup);
 		lineFont = addFontField(
 				LINE_LABEL_FONT,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.LineLabelFont"), lineGroup); //$NON-NLS-1$
 		lineLabelColor = addColorField(
 				LINE_LABEL_COLOR,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.LineLabelColor"), lineGroup); //$NON-NLS-1$
 		lineColor = addColorField(
 				LINE_COLOR,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.LineColor"), lineGroup); //$NON-NLS-1$
 		addIntegerField(
 				LINE_WIDTH,
 				EditorPlugin
 						.getResourceString("CanvasPreferenceTab.LABEL.LineWidth"), lineGroup); //$NON-NLS-1$
 		addComboField(LINE_ROUTING, EditorPlugin
 				.getResourceString("CanvasPreferenceTab.LABEL.LineRouting"), //$NON-NLS-1$
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
 		inputPortColor.setEnabled(userColorsValue, iconGroup);
 		outputPortColor.setEnabled(userColorsValue, iconGroup);
 		lineColor.setEnabled(userColorsValue, lineGroup);
 		lineLabelColor.setEnabled(showLineLabelsValue && userColorsValue,
 				lineGroup);
 		lineFont.setEnabled(showLineLabelsValue && userColorsValue, lineGroup);
 		gridWidth.setEnabled(snapToGridValue, canvasGroup);
 		gridHeight.setEnabled(snapToGridValue, canvasGroup);
 
 		useSystemColors.getButton().addSelectionListener(
 				new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						boolean userColorsValue = !useSystemColors
 								.getBooleanValue();
 						boolean showLineLabelsValue = showLineLabels
 								.getBooleanValue();
 						boolean snapToGridValue = snapToGrid.getBooleanValue();
 
 						canvasColor.setEnabled(userColorsValue, canvasGroup);
 						gridColor
 								.setEnabled(snapToGridValue && userColorsValue,
 										canvasGroup);
 						iconFont.setEnabled(userColorsValue, iconGroup);
 						inputPortColor.setEnabled(userColorsValue, iconGroup);
 						outputPortColor.setEnabled(userColorsValue, iconGroup);
 						lineColor.setEnabled(userColorsValue, lineGroup);
 						lineLabelColor.setEnabled(showLineLabelsValue
 								&& userColorsValue, lineGroup);
 						lineFont.setEnabled(showLineLabelsValue
 								&& userColorsValue, lineGroup);
 					}
 				});
 
 		showLineLabels.getButton().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				boolean userColorsValue = !useSystemColors.getBooleanValue();
 				boolean showLineLabelsValue = showLineLabels.getBooleanValue();
 				lineLabelColor.setEnabled(showLineLabelsValue
 						&& userColorsValue, lineGroup);
 				lineFont.setEnabled(showLineLabelsValue && userColorsValue,
 						lineGroup);
 			}
 		});
 
 		snapToGrid.getButton().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				boolean userColorsValue = !useSystemColors.getBooleanValue();
 				boolean snapToGridValue = snapToGrid.getBooleanValue();
 
 				gridColor.setEnabled(snapToGridValue && userColorsValue,
 						canvasGroup);
 				gridWidth.setEnabled(snapToGridValue, canvasGroup);
 				gridHeight.setEnabled(snapToGridValue, canvasGroup);
 			}
 		});
 
 	}
 
 	public void init(IWorkbench workbench) {
 	}
 
 	protected ColorFieldEditor addColorField(String name, String labelText,
 			Composite parent) {
 		ColorFieldEditor f = new ColorFieldEditor(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 	protected ComboFieldEditor addComboField(String name, String labelText,
 			String[][] entryNamesAndValues, Composite parent) {
 		ComboFieldEditor f = new ComboFieldEditor(name, labelText,
 				entryNamesAndValues, parent);
 		addField(f);
 		return f;
 	}
 
 	protected IntegerFieldEditor addIntegerField(String name, String labelText,
 			Composite parent) {
 		IntegerFieldEditor f = new IntegerFieldEditor(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 	protected BooleanField addBooleanField(String name, String labelText,
 			Composite parent) {
 		BooleanField f = new BooleanField(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 	protected StringFieldEditor addStringField(String name, String labelText,
 			Composite parent) {
 		StringFieldEditor f = new StringFieldEditor(name, labelText, parent);
 		addField(f);
 		return f;
 	}
 
 	// protected NumberField addNumberField(String name, String labelText,
 	// Composite parent)
 	// {
 	// NumberField f = new NumberField(name,labelText,parent);
 	// addField(f);
 	// return f;
 	// }
 
 	protected ColorFontFieldEditor addFontField(String name, String labelText,
 			Composite parent) {
 		ColorFontFieldEditor f = new ColorFontFieldEditor(name, labelText,
 				parent);
 		addField(f);
 		return f;
 	}
 
 	public static String[][] getLineRoutingLabels() {
 		return m_lineRoutingLabels;
 	}
 
 	public static String[][] getLabelPlacementLabels() {
 		return m_labelPlacementLabels;
 	}
 
 	public static void propagateProperty(String property, EditPart part) {
 		Iterator iter = part.getChildren().iterator();
 		while (iter.hasNext()) {
 			EditPart child = (EditPart) iter.next();
 			Figure fig = (Figure) ((GraphicalEditPart) child).getFigure();
 			GEMPreferences.propagateProperty(property, fig);
 			propagateProperty(property, child);
 		}
 	}
 
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
 			if (resourceRegistry.containsKey(rgb.toString()))
 				return (Color) resourceRegistry.get(rgb.toString());
 			c = new Color(Display.getCurrent(), rgb);
 			resourceRegistry.put(rgb.toString(), c);
 		}
 		return c;
 	}
 
 	// CR392586: resource leaks
 	public static Font getFont(IPreferenceStore store, String property) {
 		FontData fd = PreferenceConverter.getFontData(store, property);
 		if (resourceRegistry.containsKey(fd.toString()))
 			return (Font) resourceRegistry.get(fd.toString());
 
 		Font f = new Font(null, fd);
 		resourceRegistry.put(fd.toString(), f);
 		return f;
 	}
 
 	public static void propagateProperty(String property, Figure fig) {
 		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
 		WindowFigure window = null;
 		IconFigure icon = null;
 		LinkFigure link = null;
 		if (fig instanceof CompoundNodeFigure) {
 			window = ((CompoundNodeFigure) fig).getWindowFigure();
 			icon = ((CompoundNodeFigure) fig).getIconFigure();
 		} else if (fig instanceof WindowFigure)
 			window = (WindowFigure) fig;
 		else if (fig instanceof LinkFigure)
 			link = (LinkFigure) fig;
 
 		if (property != null && property.equals(USE_SYSTEM_COLORS))
 			// reload all properties - it's easiest
 			property = null;
 
 		if (property == null || SNAP_TO_GRID.equals(property)) {
 			boolean b = store.getBoolean(SNAP_TO_GRID);
 			WindowFigure.defaultGridEnabled = b;
 
 			if (window != null)
 				window.getGridLayer().setVisible(b);
 		}
 
 		if (property == null || GRID_WIDTH.equals(property)
 				|| GRID_HEIGHT.equals(property)) {
 			Dimension d = new Dimension(store.getInt(GRID_WIDTH), store
 					.getInt(GRID_HEIGHT));
 			WindowFigure.defaultGridSpacing = d;
 
 			if (window != null)
 				window.getGridLayer().setSpacing(d);
 		}
 
 		if (property == null || GRID_COLOR.equals(property)) {
 			Color c = getColor(store, GRID_COLOR);
 			WindowFigure.defaultGridColor = c;
 
 			if (window != null)
 				window.getGridLayer().setForegroundColor(c);
 		}
 
 		// TODO: since the line router is managed by the EditPart for the
 		// container figure, setting the line routing style in the WindowFigure
 		// does not change the line routing immediately. The editor must be
 		// restarted for line routing to take effect.
 		if (property == null || LINE_ROUTING.equals(property)) {
 			String s = store.getString(LINE_ROUTING);
 			int style;
 			if (LINE_ROUTING_MANHATTAN.equals(s))
 				style = WindowFigure.LINE_ROUTING_MANHATTAN;
 			else
 				style = WindowFigure.LINE_ROUTING_MANUAL;
 
 			WindowFigure.defaultLineRoutingStyle = style;
 			if (window != null)
 				window.setLineRoutingStyle(style);
 		}
 
 		if (property == null || LINE_WIDTH.equals(property)) {
 			int w = store.getInt(LINE_WIDTH);
 			LinkFigure.defaultLineWidth = w;
 
 			if (link != null)
 				link.setLineWidth(w);
 		}
 
 		if (property == null || LINE_COLOR.equals(property)) {
 			Color c = getColor(store, LINE_COLOR);
 			LinkFigure.defaultLineColor = c;
 
 			if (link != null)
 				link.setForegroundColor(c);
 		}
 
 		if (property == null || SHOW_LINE_LABELS.equals(property)) {
 			boolean b = store.getBoolean(SHOW_LINE_LABELS);
 			LinkFigure.defaultLabelVisible = b;
 
 			if (link != null)
 				link.setLabelVisible(b);
 		}
 
 		if (property == null || LINE_LABEL_FONT.equals(property)
 				|| LINE_LABEL_FONT_COLOR.equals(property)) {
 			// CR392586: resource leaks
 			Font f = getFont(store, LINE_LABEL_FONT);
 			Color c = getColor(store, LINE_LABEL_FONT_COLOR);
 			LinkFigure.defaultFont = f;
 			LinkFigure.defaultLabelForeground = c;
 
 			if (link != null) {
 				link.setFont(f);
 				link.setLabelForeground(c);
 			}
 		}
 
 		if (property == null || LINE_LABEL_COLOR.equals(property)) {
 			Color c = getColor(store, LINE_LABEL_COLOR);
 			LinkFigure.defaultLabelBackground = c;
 
 			if (link != null)
 				link.setLabelBackground(c);
 		}
 
 		if (property == null || CANVAS_COLOR.equals(property)) {
 			Color c = getColor(store, CANVAS_COLOR);
 			WindowFigure.defaultBackgroundColor = c;
 
 			if (window != null)
 				window.setBackgroundColor(c);
 			if (icon != null)
 				icon.setBackgroundColor(c);
 		}
 
 		if (property == null || INPUT_PORT_COLOR.equals(property)) {
 			Color c = getColor(store, INPUT_PORT_COLOR);
 			InputPortFigure.defaultForegroundColor = c;
 
 			if (fig instanceof InputPortFigure)
 				fig.setForegroundColor(c);
 		}
 
 		if (property == null || OUTPUT_PORT_COLOR.equals(property)) {
 			Color c = getColor(store, OUTPUT_PORT_COLOR);
 			OutputPortFigure.defaultForegroundColor = c;
 
 			if (fig instanceof OutputPortFigure)
 				fig.setForegroundColor(c);
 		}
 
 		if (property == null || FIGURE_LABEL_FONT.equals(property)
 				|| FIGURE_LABEL_FONT_COLOR.equals(property)) {
 			// CR392586: resource leaks
 			Font f = getFont(store, FIGURE_LABEL_FONT);
 			Color c = getColor(store, FIGURE_LABEL_FONT_COLOR);
 			IconFigure.defaultFont = f;
 			IconFigure.defaultForegroundColor = c;
 			WindowFigure.defaultFont = f;
 			WindowFigure.defaultForegroundColor = c;
 
 			if (window != null) {
 				window.setFont(f);
 				window.setForegroundColor(c);
 			}
 			if (icon != null) {
 				icon.setFont(f);
 				icon.setForegroundColor(c);
 			}
 			if (fig instanceof IconFigure) {
 				fig.setFont(f);
 				fig.setForegroundColor(c);
 			}
 		}
 
 		if (property == null || LABEL_PLACEMENT.equals(property)) {
 			int placement = PositionConstants.SOUTH;
 			String s = store.getString(LABEL_PLACEMENT);
 			if (LABEL_PLACEMENT_TOP.equals(s))
 				placement = PositionConstants.NORTH;
 			if (LABEL_PLACEMENT_BOTTOM.equals(s))
 				placement = PositionConstants.SOUTH;
 			if (LABEL_PLACEMENT_LEFT.equals(s))
 				placement = PositionConstants.WEST;
 			if (LABEL_PLACEMENT_RIGHT.equals(s))
 				placement = PositionConstants.EAST;
 			IconFigure.defaultTextPlacement = placement;
 
 			if (icon != null)
 				icon.setTextPlacement(placement);
 			if (fig instanceof IconFigure)
 				((IconFigure) fig).setTextPlacement(placement);
 		}
 
 		Iterator iter = fig.getChildren().iterator();
 		while (iter.hasNext()) {
 			Figure child = (Figure) iter.next();
 			propagateProperty(property, child);
 		}
 	}
 }
