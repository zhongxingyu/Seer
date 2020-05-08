 package com.laboki.eclipse.plugin.fastopen.opener.ui;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 
 import lombok.Synchronized;
 import lombok.val;
 import lombok.extern.java.Log;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILazyContentProvider;
 import org.eclipse.jface.viewers.StyledCellLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.TextStyle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 import com.google.common.collect.Lists;
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.fastopen.EventBus;
 import com.laboki.eclipse.plugin.fastopen.Task;
 import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
 import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesEvent;
 import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesResultEvent;
 import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
 import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
 import com.laboki.eclipse.plugin.fastopen.opener.resources.RFile;
 
 @Log
 public final class Dialog {
 
 	private static final int HEIGHT = 480;
 	private static final int WIDTH = Dialog.HEIGHT * 2;
 	private static final int SPACING_SIZE_IN_PIXELS = 10;
 	private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.UNICODE_CASE;
 	private static final Pattern TEXT_PATTERN = Pattern.compile("\\p{Punct}*|\\w*| *", Dialog.PATTERN_FLAGS);
 	private static final Shell SHELL = new Shell(EditorContext.getShell(), SWT.RESIZE | SWT.APPLICATION_MODAL);
 	private static final Text TEXT = new Text(Dialog.SHELL, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.NO_FOCUS);
 	private static final TableViewer VIEWER = new TableViewer(Dialog.SHELL, SWT.VIRTUAL | SWT.BORDER | SWT.MULTI);
 	private static final Table TABLE = Dialog.VIEWER.getTable();
 
 	public Dialog() {
 		Dialog.arrangeWidgets();
 		Dialog.setupDialog();
 		Dialog.setupText();
 		this.setupViewer();
 		this.addListeners();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void fileResourcesChanged(final FileResourcesEvent event) {
 		EditorContext.asyncExec(new Task("") {
 
 			@Override
 			public void execute() {
 				Dialog.this.updateViewer(event.getRFiles());
 			}
 		});
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void fileResourcesChanged(final FilterRecentFilesResultEvent event) {
 		EditorContext.asyncExec(new Task("") {
 
 			@Override
 			public void execute() {
 				Dialog.this.updateViewer(event.getRFiles());
 			}
 		});
 	}
 
 	@SuppressWarnings("static-method")
 	@Subscribe
 	@AllowConcurrentEvents
 	public void showDialog(@SuppressWarnings("unused") final ShowFastOpenDialogEvent event) {
 		EditorContext.asyncExec(new Task("") {
 
 			@Override
 			public void execute() {
 				Dialog.SHELL.open();
 				Dialog.focusViewer();
 			}
 		});
 	}
 
 	public static void reset() {
 		Dialog.TEXT.setText("");
 	}
 
 	@Synchronized
 	protected void updateViewer(final List<RFile> rFiles) {
		try {
			Dialog._updateViewer(rFiles);
		} catch (final Exception e) {}
	}

	private static void _updateViewer(final List<RFile> rFiles) {
 		Dialog.VIEWER.getControl().setRedraw(false);
 		Dialog.VIEWER.setInput(rFiles.toArray(new RFile[rFiles.size()]));
 		Dialog.VIEWER.setItemCount(rFiles.size());
 		Dialog.refresh();
 		Dialog.VIEWER.getControl().setRedraw(true);
 		Dialog.focusViewer();
 	}
 
 	private void addListeners() {
 		Dialog.SHELL.addShellListener(new DialogShellListener());
 		Dialog.TABLE.addFocusListener(new ViewerFocusListener());
 		Dialog.TEXT.addFocusListener(new TextFocusListener());
 		Dialog.SHELL.addFocusListener(new TextFocusListener());
 		Dialog.TABLE.addKeyListener(new ViewerKeyListener());
 		Dialog.TEXT.addModifyListener(new TextModifyListener());
 		Dialog.VIEWER.addDoubleClickListener(new ViewerDoubleClickListener());
 		Dialog.listenForTextSelection();
 	}
 
 	private static void listenForTextSelection() {
 		Dialog.SHELL.getDisplay().addFilter(SWT.KeyDown, new Listener() {
 
 			@Override
 			public void handleEvent(final Event event) {
 				if (this.isCtrlL(event)) EditorContext.asyncExec(new Task("") {
 
 					@Override
 					public void execute() {
 						selectText();
 					}
 				});
 			}
 
 			private boolean isCtrlL(final Event event) {
 				return (Character.toUpperCase((char) event.keyCode) == 'L') && ((event.stateMask & SWT.CTRL) == SWT.CTRL);
 			}
 
 			private void selectText() {
 				Dialog.TEXT.selectAll();
 			}
 		});
 	}
 
 	private void setupViewer() {
 		this.setupTable();
 		Dialog.VIEWER.setContentProvider(this.new ContentProvider());
 		Dialog.VIEWER.setUseHashlookup(true);
 	}
 
 	private void setupTable() {
 		Dialog.TABLE.setLinesVisible(true);
 		Dialog.TABLE.setHeaderVisible(false);
 		Dialog.TABLE.setSize(Dialog.TABLE.getClientArea().width, Dialog.TABLE.getClientArea().height);
 		this.createTableColumn();
 	}
 
 	private void createTableColumn() {
 		final TableViewerColumn col = new TableViewerColumn(Dialog.VIEWER, SWT.RIGHT | SWT.LEFT | SWT.CENTER);
 		col.getColumn().setWidth(Dialog.TABLE.getClientArea().width);
 		col.getColumn().setResizable(true);
 		col.setLabelProvider(new LabelProvider());
		if (!EditorContext.isWindows()) col.getColumn().pack();
 	}
 
 	private static void _focusViewer() {
 		Dialog.TABLE.setFocus();
 		Dialog.TABLE.forceFocus();
 	}
 
 	private static void arrangeWidgets() {
 		Dialog.setDialogLayout();
 		Dialog.setTextLayout();
 		Dialog.setViewerLayout();
 		Dialog.SHELL.pack();
 		Dialog.TABLE.pack();
 	}
 
 	private static void backspace() {
 		final int end = Dialog.TEXT.getCaretPosition();
 		if (end < 1) return;
 		Dialog.delete(end);
 	}
 
 	private static void delete(final int end) {
 		final int start = end - (Dialog.TEXT.getSelectionText().length() > 0 ? Dialog.TEXT.getSelectionText().length() : 1);
 		Dialog.TEXT.setSelection(start, end);
 		Dialog.TEXT.cut();
 		Dialog.TEXT.setSelection(start, start);
 	}
 
 	private static void closeFiles() {
 		for (final int index : Dialog.TABLE.getSelectionIndices())
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					EditorContext.closeEditor(((RFile) Dialog.VIEWER.getElementAt(index)).getFile());
 				}
 			});
 	}
 
 	private static GridData createFillGridData() {
 		return new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1);
 	}
 
 	private static void filterViewer() {
 		val searchString = Dialog.TEXT.getText().trim();
 		EventBus.post(new FilterRecentFilesEvent(searchString));
 	}
 
 	private static void focusViewer() {
 		Dialog._focusViewer();
 		Dialog.TABLE.setSelection(Dialog.TABLE.getTopIndex());
 	}
 
 	private static boolean isValidCharacter(final String character) {
 		return Dialog.TEXT_PATTERN.matcher(character).matches();
 	}
 
 	private static void openFile(final IFile file) {
 		try {
 			EditorContext.openEditor(file);
 		} catch (final Exception e) {
 			Dialog.openLink(file);
 		}
 	}
 
 	private static void openFiles() {
 		for (final int index : Dialog.TABLE.getSelectionIndices())
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					Dialog.openFile(((RFile) Dialog.VIEWER.getElementAt(index)).getFile());
 				}
 			});
 	}
 
 	private static void openLink(final IFile file) {
 		try {
 			EditorContext.openLink(file);
 		} catch (final Exception e) {
 			Dialog.log.log(Level.SEVERE, "Failed to open linked file", e);
 		}
 	}
 
 	private static void refocusViewer() {
 		Dialog._focusViewer();
 		Dialog.TABLE.setSelection(Dialog.TABLE.getSelectionIndex());
 	}
 
 	private static void refresh() {
 		EditorContext.flushEvents();
 		Dialog.VIEWER.refresh(true, true);
 		EditorContext.flushEvents();
 	}
 
 	private static void setDialogLayout() {
 		final GridLayout layout = new GridLayout(1, true);
 		Dialog.spaceDialogLayout(layout);
 		Dialog.SHELL.setLayout(layout);
 		Dialog.SHELL.setLayoutData(Dialog.createFillGridData());
 	}
 
 	private static void setTextLayout() {
 		final GridData textGridData = new GridData();
 		textGridData.horizontalAlignment = GridData.FILL;
 		textGridData.grabExcessHorizontalSpace = true;
 		Dialog.TEXT.setLayoutData(textGridData);
 	}
 
 	private static void setupDialog() {
 		Dialog.SHELL.setTabList(Lists.newArrayList(Dialog.VIEWER.getControl()).toArray(new Control[1]));
 		Dialog.SHELL.setSize(Dialog.WIDTH, Dialog.HEIGHT);
 	}
 
 	private static void setupText() {
 		Dialog.TEXT.setMessage("start typing to filter files...");
 	}
 
 	private static void setViewerLayout() {
 		Dialog.VIEWER.getTable().setLayoutData(Dialog.createFillGridData());
 	}
 
 	private static void spaceDialogLayout(final GridLayout layout) {
 		layout.marginLeft = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.marginTop = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.marginRight = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.marginBottom = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.horizontalSpacing = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.verticalSpacing = Dialog.SPACING_SIZE_IN_PIXELS;
 	}
 
 	private static void updateText(final char character) {
 		Dialog.TEXT.insert(String.valueOf(character));
 	}
 
 	private enum FONT {
 		FONT;
 
 		private static final FontData[] FONT_DATAS = Dialog.TABLE.getFont().getFontData();
 		private static final String DEFAULT_FONT_NAME = FONT.FONT_DATAS[0].getName();
 		private static final int DEFAULT_FONT_HEIGHT = FONT.FONT_DATAS[0].getHeight();
 		public static final Font ITALIC_FONT = FONT.makeItalicizedFont();
 		public static final Font LARGE_BOLD_FONT = FONT.makeLargeBoldFont();
 		public static final Font SMALL_BOLD_FONT = FONT.makeSmallBoldFont();
 		public static final Font SMALL_ITALIC_FONT = FONT.makeSmallItalicizedFont();
 		public static final Font NORMAL_FONT = FONT.makeNormalFont();
 
 		private static Font makeItalicizedFont() {
 			return new Font(EditorContext.DISPLAY, FONT.DEFAULT_FONT_NAME, FONT.DEFAULT_FONT_HEIGHT, SWT.ITALIC);
 		}
 
 		private static Font makeLargeBoldFont() {
 			return new Font(EditorContext.DISPLAY, FONT.DEFAULT_FONT_NAME, FONT.DEFAULT_FONT_HEIGHT + 2, SWT.BOLD);
 		}
 
 		private static Font makeSmallBoldFont() {
 			return new Font(EditorContext.DISPLAY, FONT.DEFAULT_FONT_NAME, FONT.DEFAULT_FONT_HEIGHT - 2, SWT.BOLD);
 		}
 
 		private static Font makeSmallItalicizedFont() {
 			return new Font(EditorContext.DISPLAY, FONT.DEFAULT_FONT_NAME, FONT.DEFAULT_FONT_HEIGHT - 2, SWT.ITALIC);
 		}
 
 		private static Font makeNormalFont() {
 			return new Font(EditorContext.DISPLAY, FONT.DEFAULT_FONT_NAME, FONT.DEFAULT_FONT_HEIGHT, SWT.NORMAL);
 		}
 	}
 
 	private final class ContentProvider implements ILazyContentProvider {
 
 		private RFile[] rFiles;
 
 		public ContentProvider() {}
 
 		@Override
 		public void dispose() {}
 
 		@Override
 		public void inputChanged(final Viewer arg0, final Object oldInput, final Object newInput) {
 			this.rFiles = (RFile[]) newInput;
 		}
 
 		@Override
 		public void updateElement(final int index) {
 			try {
 				Dialog.VIEWER.replace(this.rFiles[index], index);
 			} catch (final Exception e) {
 				Dialog.log.log(Level.FINE, "Failed to update index for lazy content provider.", e);
 			}
 		}
 	}
 
 	private final class DialogShellListener implements ShellListener {
 
 		public DialogShellListener() {}
 
 		@Override
 		public void shellActivated(final ShellEvent arg0) {
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					Dialog.focusViewer();
 				}
 			});
 		}
 
 		@Override
 		public void shellClosed(final ShellEvent event) {
 			event.doit = false;
 			Dialog.SHELL.setVisible(false);
 			Dialog.reset();
 		}
 
 		@Override
 		public void shellDeactivated(final ShellEvent arg0) {
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					Dialog.refresh();
 				}
 			});
 		}
 
 		@Override
 		public void shellDeiconified(final ShellEvent arg0) {}
 
 		@Override
 		public void shellIconified(final ShellEvent arg0) {}
 	}
 
 	private final class LabelProvider extends StyledCellLabelProvider {
 
 		private final String separator = this.getSeparator();
 		private final StyledString.Styler filenameStyler = this.styler(FONT.LARGE_BOLD_FONT, null);
 		private final StyledString.Styler folderStyler = this.styler(FONT.NORMAL_FONT, this.color(SWT.COLOR_DARK_GRAY));
 		private final StyledString.Styler inStyler = this.styler(FONT.ITALIC_FONT, this.color(SWT.COLOR_GRAY));
 		private final StyledString.Styler modifiedStyler = this.styler(FONT.SMALL_ITALIC_FONT, this.color(SWT.COLOR_GRAY));
 		private final StyledString.Styler timeStyler = this.styler(FONT.SMALL_BOLD_FONT, this.color(SWT.COLOR_DARK_RED));
 		private final StyledString.Styler typeStyler = this.styler(FONT.SMALL_BOLD_FONT, this.color(SWT.COLOR_DARK_BLUE));
 
 		public LabelProvider() {
 			this.setOwnerDrawEnabled(true);
 		}
 
 		@Override
 		protected StyleRange prepareStyleRange(final StyleRange styleRange, final boolean applyColors) {
 			return super.prepareStyleRange(styleRange, applyColors);
 		}
 
 		@Override
 		protected void paint(final Event event, final Object element) {
 			super.paint(event, element);
 		}
 
 		@Override
 		protected void measure(final Event event, final Object element) {
 			super.measure(event, element);
 		}
 
 		@Override
 		public void update(final ViewerCell cell) {
 			this.updateCellProperties(cell, (RFile) cell.getElement(), this.createStyledText((RFile) cell.getElement()));
 			super.update(cell);
 		}
 
 		private void updateCellProperties(final ViewerCell cell, final RFile file, final StyledString text) {
 			cell.setText(text.toString());
 			cell.setImage(file.getContentTypeImage());
 			cell.setStyleRanges(text.getStyleRanges());
 		}
 
 		private StyledString createStyledText(final RFile file) {
 			final StyledString text = new StyledString();
 			text.append(file.getName() + this.separator, this.filenameStyler);
 			text.append("in  ", this.inStyler);
 			text.append(file.getFolder() + this.separator, this.folderStyler);
 			text.append("modified  ", this.modifiedStyler);
 			text.append(file.getModificationTime() + "  ", this.timeStyler);
 			text.append(file.getContentTypeString(), this.typeStyler);
 			return text;
 		}
 
 		private Color color(final int color) {
 			return Display.getCurrent().getSystemColor(color);
 		}
 
 		private StyledString.Styler styler(final Font font, final Color color) {
 			return new StyledString.Styler() {
 
 				@Override
 				public void applyStyles(final TextStyle textStyle) {
 					textStyle.font = font;
 					textStyle.foreground = color;
 				}
 			};
 		}
 
 		private String getSeparator() {
 			if (EditorContext.isWindows()) return "  ";
 			return System.getProperty("line.separator");
 		}
 	}
 
 	private final class ViewerKeyListener implements KeyListener {
 
 		public ViewerKeyListener() {}
 
 		@Override
 		public void keyPressed(final KeyEvent event) {
 			if (Dialog.isValidCharacter(String.valueOf(event.character))) {
 				event.doit = false;
 				EditorContext.asyncExec(new Task("") {
 
 					@Override
 					public void execute() {
 						Dialog.updateText(event.character);
 					}
 				});
 			} else if (event.keyCode == SWT.BS) {
 				event.doit = false;
 				EditorContext.asyncExec(new Task("") {
 
 					@Override
 					public void execute() {
 						Dialog.backspace();
 					}
 				});
 			} else if ((event.keyCode == SWT.CR) || (event.keyCode == SWT.KEYPAD_CR)) {
 				event.doit = false;
 				EditorContext.asyncExec(new Task("") {
 
 					@Override
 					public void execute() {
 						Dialog.SHELL.close();
 						Dialog.openFiles();
 					}
 				});
 			} else if ((event.keyCode == SWT.DEL)) {
 				event.doit = false;
 				EditorContext.asyncExec(new Task("") {
 
 					@Override
 					public void execute() {
 						Dialog.SHELL.close();
 						Dialog.closeFiles();
 					}
 				});
 			}
 		}
 
 		@Override
 		public void keyReleased(final KeyEvent arg0) {}
 	}
 
 	private final class TextFocusListener implements FocusListener {
 
 		public TextFocusListener() {}
 
 		@Override
 		public void focusGained(final FocusEvent arg0) {
 			Dialog.refocusViewer();
 		}
 
 		@Override
 		public void focusLost(final FocusEvent arg0) {}
 	}
 
 	private final class TextModifyListener implements ModifyListener {
 
 		public TextModifyListener() {}
 
 		@Override
 		public void modifyText(final ModifyEvent arg0) {
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					Dialog.filterViewer();
 				}
 			});
 		}
 	}
 
 	private final class ViewerFocusListener implements FocusListener {
 
 		public ViewerFocusListener() {}
 
 		@Override
 		public void focusGained(final FocusEvent arg0) {}
 
 		@Override
 		public void focusLost(final FocusEvent arg0) {
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					Dialog.refocusViewer();
 				}
 			});
 		}
 	}
 
 	private final class ViewerDoubleClickListener implements IDoubleClickListener {
 
 		public ViewerDoubleClickListener() {}
 
 		@Override
 		public void doubleClick(final DoubleClickEvent arg0) {
 			EditorContext.asyncExec(new Task("") {
 
 				@Override
 				public void execute() {
 					Dialog.SHELL.close();
 					Dialog.openFiles();
 				}
 			});
 		}
 	}
 }
