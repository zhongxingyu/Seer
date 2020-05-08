 package org.opendarts.prototype.ui.utils;
 
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.FormColors;
 import org.eclipse.ui.forms.IFormColors;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * The Class OpenDartsFormsToolkit.
  */
 public class OpenDartsFormsToolkit extends FormToolkit {
 
 	/** The Constant COLOR_BROKEN. */
 	public static final String COLOR_BROKEN = "BrokenBackgroundColor";
 
 	/** The Constant COLOR_WINNING. */
 	public static final String COLOR_WINNING = "WinningBackgroundColor";
 
 	/** The Constant COLOR_ACTIVE. */
 	public static final String COLOR_ACTIVE = "ActiveBackgroundColor";
 
 	/** The Constant COLOR_ACTIVE. */
 	public static final String COLOR_INACTIVE = "InactiveBackgroundColor";
 
 	/** The Constant FONT_SCORE_LEFT. */
 	public static final String FONT_SCORE_LEFT = "ScoreLeftFont";
 
 	/** The Constant FONT_SCORE_SHEET. */
 	public static final String FONT_SCORE_SHEET = "ScoreSheetFont";
 
 	/** The Constant FONT_SCORE_SHEET_BOLD. */
 	public static final String FONT_SCORE_SHEET_BOLD = "ScoreSheetFontBold";
 
 	/** The Constant FONT_SCORE_INPUT. */
 	public static final String FONT_SCORE_INPUT = "ScoreInputFont";
 
 	/** The form colors. */
 	private static FormColors formColors;
 
 	/** The toolkit. */
 	private static OpenDartsFormsToolkit toolkit;
 
 	/** The font registry. */
 	private static FontRegistry fontRegistry = JFaceResources.getFontRegistry();
 
 	/** The display. */
 	private static Display display;
 
 	/**
 	 * Instantiates a new open darts forms toolkit.
 	 *
 	 * @param colors the colors
 	 */
 	private OpenDartsFormsToolkit(FormColors colors) {
 		super(colors);
 
 		Font initialFont;
 		// Fonts
 		initialFont = fontRegistry.defaultFont();
 		registerFont(initialFont, FONT_SCORE_SHEET, 18);
 		initialFont = fontRegistry.getBold(JFaceResources.DEFAULT_FONT);
 		registerFont(initialFont, FONT_SCORE_SHEET_BOLD, 18);
 		registerFont(initialFont, FONT_SCORE_INPUT, 64);
		registerFont(initialFont, FONT_SCORE_LEFT, 126);
 	}
 
 	/**
 	 * Gets the form colors.
 	 *
 	 * @param display the display
 	 * @return the form colors
 	 */
 	public static FormColors getFormColors(Display display) {
 		if (formColors == null) {
 			formColors = new FormColors(display);
 			formColors.createColor(COLOR_ACTIVE, 0xF2, 0xDA, 0x2E);
 			formColors.createColor(COLOR_BROKEN, 0xD0, 0x6D, 0x58);
 			formColors.createColor(COLOR_INACTIVE, 0xC1, 0xC1, 0xC1);
 			formColors.createColor(COLOR_WINNING,
 					formColors.getColor(IFormColors.H_GRADIENT_START).getRGB());
 			formColors.markShared();
 		}
 		return formColors;
 	}
 
 	/**
 	 * Gets the font.
 	 *
 	 * @param initialFont the initial font
 	 * @param key the key
 	 * @param height the height
 	 * @return the font
 	 */
 	private static void registerFont(Font initialFont, String key, int height) {
 		FontData[] fontData = initialFont.getFontData();
 		for (FontData element : fontData) {
 			element.setHeight(height);
 		}
 		fontRegistry.put(key, fontData);
 	}
 
 	/**
 	 * Gets the font.
 	 *
 	 * @param key the key
 	 * @return the font
 	 */
 	public static Font getFont(String key) {
 		return fontRegistry.get(key);
 	}
 
 	/**
 	 * Gets the toolkit.
 	 *
 	 * @return the toolkit
 	 */
 	public static OpenDartsFormsToolkit getToolkit() {
 		if (toolkit == null) {
 			display = Display.getDefault();
 			toolkit = new OpenDartsFormsToolkit(getFormColors(display));
 		}
 		return toolkit;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.widgets.FormToolkit#createLabel(org.eclipse.swt.widgets.Composite, java.lang.String, int)
 	 */
 	@Override
 	public Label createLabel(Composite parent, String text, int style) {
 		Label label = super.createLabel(parent, text, style);
 		label.setForeground(this.getColors().getColor(IFormColors.TITLE));
 		return label;
 	}
 
 	/**
 	 * Creates the dummy label.
 	 *
 	 * @param parent the parent
 	 * @param text the text
 	 * @return the label
 	 */
 	public Label createDummyLabel(Composite parent, String text) {
 		Label label = new Label(parent, SWT.WRAP);
 		label.setText(text);
 		label.setFont(fontRegistry.getItalic(JFaceResources.DIALOG_FONT));
 		label.setForeground(Display.getDefault().getSystemColor(
 				SWT.COLOR_DARK_CYAN));
 		return label;
 	}
 
 	/**
 	 * Builds the column.
 	 *
 	 * @param title the title
 	 * @param viewer the viewer
 	 * @param width the width
 	 * @param style the style
 	 * @param labelProvider the label provider
 	 * @return the table viewer column
 	 */
 	public TableViewerColumn createTableColumn(String title,
 			TableViewer viewer, int width, int style,
 			ColumnLabelProvider labelProvider) {
 		TableViewerColumn column = new TableViewerColumn(viewer, style);
 		TableColumn tableColumn = column.getColumn();
 		tableColumn.setText(title);
 		tableColumn.setWidth(width);
 		tableColumn.setResizable(false);
 
 		column.setLabelProvider(labelProvider);
 		return column;
 	}
 
 	/**
 	 * Creates the table column.
 	 *
 	 * @param title the title
 	 * @param viewer the viewer
 	 * @param width the width
 	 * @param style the style
 	 * @param labelProvider the label provider
 	 * @param editingSupport the editing support
 	 * @return the table viewer column
 	 */
 	public TableViewerColumn createTableColumn(String title,
 			TableViewer viewer, int width, int style,
 			ColumnLabelProvider labelProvider, EditingSupport editingSupport) {
 		TableViewerColumn column = this.createTableColumn(title, viewer, width,
 				style, labelProvider);
 		column.setEditingSupport(editingSupport);
 		return column;
 	}
 
 }
