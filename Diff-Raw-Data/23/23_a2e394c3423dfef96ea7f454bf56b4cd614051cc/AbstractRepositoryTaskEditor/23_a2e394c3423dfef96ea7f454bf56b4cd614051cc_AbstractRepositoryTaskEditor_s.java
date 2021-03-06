 /*******************************************************************************
  * Copyright (c) 2003 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylar.internal.tasks.ui.editors;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.mylar.context.core.MylarStatusHandler;
 import org.eclipse.mylar.internal.tasks.ui.TaskListColorsAndFonts;
 import org.eclipse.mylar.internal.tasks.ui.TaskListImages;
 import org.eclipse.mylar.internal.tasks.ui.TaskUiUtil;
 import org.eclipse.mylar.internal.tasks.ui.actions.CopyToClipboardAction;
 import org.eclipse.mylar.internal.tasks.ui.actions.SaveRemoteFileAction;
 import org.eclipse.mylar.internal.tasks.ui.wizards.NewAttachmentWizard;
 import org.eclipse.mylar.internal.tasks.ui.wizards.NewAttachmentWizardDialog;
 import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
 import org.eclipse.mylar.tasks.core.IOfflineTaskHandler;
 import org.eclipse.mylar.tasks.core.LocalAttachment;
 import org.eclipse.mylar.tasks.core.RepositoryAttachment;
 import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
 import org.eclipse.mylar.tasks.core.RepositoryTaskData;
 import org.eclipse.mylar.tasks.core.TaskComment;
 import org.eclipse.mylar.tasks.core.TaskRepository;
 import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnector;
 import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.RetargetAction;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IExpansionListener;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.themes.IThemeManager;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 /**
  * @author Mik Kersten
  * @author Rob Elves
  * @author Jeff Pound (Attachment work)
  */
 public abstract class AbstractRepositoryTaskEditor extends TaskFormPage {
 
 	private static final String ATTACHMENT_DEFAULT_NAME = "attachment";
 
 	private static final String CTYPE_ZIP = "zip";
 
 	private static final String CTYPE_OCTET_STREAM = "octet-stream";
 
 	private static final String CTYPE_TEXT = "text";
 
 	private static final String ATTR_FILENAME = "filename";
 
 	private static final String CTYPE_HTML = "html";
 
 	private static final String LABEL_OPEN_IN_BROWSER = "Open in Browser";
 
 	private static final String ATTACHMENT_URL_SUFFIX = "/attachment.cgi?id=";
 
 	protected static final String CONTEXT_MENU_ID = "#MylarRepositoryEditor";
 
 	public static final String HYPERLINK_TYPE_TASK = "task";
 
 	public static final String HYPERLINK_TYPE_JAVA = "java";
 
 	private static final String LABEL_BUTTON_SUBMIT = "Submit to Repository";
 
 	protected static final String LABEL_SECTION_ACTIONS = "Actions";
 
 	private static final String LABEL_SECTION_ATTRIBUTES = "Attributes";
 
 	private static final String LABEL_SECTION_ATTACHMENTS = "Attachments";
 
 	protected static final String LABEL_SECTION_DESCRIPTION = "Description";
 
 	protected static final String LABEL_SECTION_COMMENTS = "Comments";
 
 	protected static final String LABEL_SECTION_NEW_COMMENT = "New Comment";
 
 	private FormToolkit toolkit;
 
 	private ScrolledForm form;
 
 	protected TaskRepository repository;
 
 	public static final int WRAP_LENGTH = 90;
 
 	protected Display display;
 
 	public static final Font TITLE_FONT = JFaceResources.getBannerFont();
 
 	public static final Font TEXT_FONT = JFaceResources.getDefaultFont();
 
 	public static final Font HEADER_FONT = JFaceResources.getDefaultFont();
 
 	public static final int DESCRIPTION_WIDTH = 79 * 8; // 500;
 
 	public static final int DESCRIPTION_HEIGHT = 10 * 14;
 
 	protected AbstractBugEditorInput editorInput;
 
 	private MylarTaskEditor parentEditor = null;
 
 	protected RepositoryTaskOutlineNode taskOutlineModel = null;
 
 	/**
 	 * Style option for function <code>newLayout</code>. This will create a
 	 * plain-styled, selectable text label.
 	 */
 	protected final String VALUE = "VALUE";
 
 	/**
 	 * Style option for function <code>newLayout</code>. This will create a
 	 * bolded, selectable header. It will also have an arrow image before the
 	 * text (simply for decoration).
 	 */
 	protected final String HEADER = "HEADER";
 
 	/**
 	 * Style option for function <code>newLayout</code>. This will create a
 	 * bolded, unselectable label.
 	 */
 	protected final String PROPERTY = "PROPERTY";
 
 	protected final int HORZ_INDENT = 0;
 
 	protected CCombo attributeCombo;
 
 	protected Button addSelfToCCCheck;
 
 	protected Text summaryText;
 
 	// protected Text addCommentsText;
 
 	protected Button submitButton;
 
 	protected Table attachmentsTable;
 
 	protected TableViewer attachmentTableViewer;
 
 	protected String[] attachmentsColumns = { "Description", "Type", "Creator", "Created" };
 
 	protected int[] attachmentsColumnWidths = { 200, 100, 100, 200 };
 
 	protected int scrollIncrement;
 
 	protected int scrollVertPageIncrement;
 
 	protected int scrollHorzPageIncrement;
 
 	public boolean isDirty = false;
 
 	protected StyledText currentSelectedText;
 
 	protected RetargetAction cutAction;
 
 	protected RetargetAction pasteAction;
 
 	protected Composite editorComposite;
 
 	private List<IRepositoryTaskAttributeListener> attributesListeners = new ArrayList<IRepositoryTaskAttributeListener>();
 
 	protected final ISelectionProvider selectionProvider = new ISelectionProvider() {
 		public void addSelectionChangedListener(ISelectionChangedListener listener) {
 			selectionChangedListeners.add(listener);
 		}
 
 		public ISelection getSelection() {
 			return null;
 		}
 
 		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 			selectionChangedListeners.remove(listener);
 		}
 
 		public void setSelection(ISelection selection) {
 			// No implementation.
 		}
 	};
 
 	protected List<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
 	protected HashMap<CCombo, RepositoryTaskAttribute> comboListenerMap = new HashMap<CCombo, RepositoryTaskAttribute>();
 
 	private IRepositoryTaskSelection lastSelected = null;
 
 	protected final ISelectionListener selectionListener = new ISelectionListener() {
 		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 			if ((part instanceof ContentOutline) && (selection instanceof StructuredSelection)) {
 				Object select = ((StructuredSelection) selection).getFirstElement();
 				if (select instanceof RepositoryTaskOutlineNode) {
 					RepositoryTaskOutlineNode n = (RepositoryTaskOutlineNode) select;
 
 					if (n != null && lastSelected != null
 							&& ContentOutlineTools.getHandle(n).equals(ContentOutlineTools.getHandle(lastSelected))) {
 						// we don't need to set the selection if it is already
 						// set
 						return;
 					}
 					lastSelected = n;
 
 					Object data = n.getData();
 					boolean highlight = true;
 					if (n.getKey().toLowerCase().equals("comments")) {
 						highlight = false;
 					}
 					if (n.getKey().toLowerCase().equals("new comment")) {
 						selectNewComment();
 					} else if (n.getKey().toLowerCase().equals("new description")) {
 						selectNewDescription();
 					} else if (data != null) {
 						select(data, highlight);
 					}
 				}
 			}
 		}
 	};
 
 	private TableViewer attachmentsTableViewer;
 
 	private final class AttachmentLabelProvider extends LabelProvider implements IColorProvider {
 
 		public Color getBackground(Object element) {
 			return attachmentsTable.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 		}
 
 		public Color getForeground(Object element) {
 			return attachmentsTable.getDisplay().getSystemColor(SWT.COLOR_BLACK);
 		}
 
 	}
 
 	private static final class AttachmentTableLabelProvider extends DecoratingLabelProvider implements
 			ITableColorProvider, ITableLabelProvider {
 
 		public AttachmentTableLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
 			super(provider, decorator);
 		}
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			// RepositoryAttachment attachment = (RepositoryAttachment)
 			// element;
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			RepositoryAttachment attachment = (RepositoryAttachment) element;
 			switch (columnIndex) {
 			case 0:
 				return attachment.getDescription();
 			case 1:
 				if (attachment.isPatch()) {
 					return "patch";
 				} else {
 					return attachment.getContentType();
 				}
 			case 2:
 				return attachment.getCreator();
 			case 3:
 				return attachment.getDateCreated();
 			}
 			return "unrecognized column";
 		}
 
 		public void addListener(ILabelProviderListener listener) {
 			// ignore
 
 		}
 
 		public void dispose() {
 			// ignore
 
 		}
 
 		public boolean isLabelProperty(Object element, String property) {
 			// ignore
 			return false;
 		}
 
 		public void removeListener(ILabelProviderListener listener) {
 			// ignore
 
 		}
 
 		public Color getForeground(Object element, int columnIndex) {
 			RepositoryAttachment att = (RepositoryAttachment) element;
 			if (att.isObsolete()) {
 				return TaskListColorsAndFonts.COLOR_GRAY_LIGHT;
 			}
 			return super.getForeground(element);
 		}
 
 		public Color getBackground(Object element, int columnIndex) {
 			return super.getBackground(element);
 		}
 
 		public Font getFont(Object element, int columnIndex) {
 			return super.getFont(element);
 		}
 	}
 
 	private class ComboSelectionListener extends SelectionAdapter {
 
 		private CCombo combo;
 
 		public ComboSelectionListener(CCombo combo) {
 			this.combo = combo;
 		}
 
 		public void widgetDefaultSelected(SelectionEvent event) {
 			// ignore
 		}
 
 		public void widgetSelected(SelectionEvent event) {
 			if (comboListenerMap.containsKey(combo)) {
 				if (combo.getSelectionIndex() > -1) {
 					String sel = combo.getItem(combo.getSelectionIndex());
 					RepositoryTaskAttribute attribute = comboListenerMap.get(combo);
 					if (sel != null && !(sel.equals(attribute.getValue()))) {
 						attribute.setValue(sel);
 						for (IRepositoryTaskAttributeListener client : attributesListeners) {
 							client.attributeChanged(attribute.getName(), sel);
 						}
 						markDirty(true);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a new <code>AbstractRepositoryTaskEditor</code>. Sets up the
 	 * default fonts and cut/copy/paste actions.
 	 */
 	public AbstractRepositoryTaskEditor(FormEditor editor) {
 		// set the scroll increments so the editor scrolls normally with the
 		// scroll wheel
 		super(editor, "id", "label"); //$NON-NLS-1$ //$NON-NLS-2$
 		FontData[] fd = TEXT_FONT.getFontData();
 		int cushion = 4;
 		scrollIncrement = fd[0].getHeight() + cushion;
 		scrollVertPageIncrement = 0;
 		scrollHorzPageIncrement = 0;
 
 	}
 
 	/**
 	 * @return The task data this editor is displaying.
 	 */
 	public abstract RepositoryTaskData getRepositoryTaskData();
 
 	public String getNewCommentText() {
 		return addCommentsTextBox.getText();
 	}
 
 	// /**
 	// * @return Any currently selected text.
 	// */
 	// protected StyledText getCurrentText() {
 	// return currentSelectedText;
 	// }
 
 	protected void createFormContent(final IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 		form = managedForm.getForm();
 		toolkit = managedForm.getToolkit();
		if (getRepositoryTaskData() == null) {
			Composite composite = new Composite(form.getBody(), SWT.NULL);
			composite.setLayout(new GridLayout());
			Label noBugLabel = new Label(composite, SWT.NULL);
			noBugLabel.setText("Could not download task data, possibly due to timeout or connectivity problem.\n"
					+ "Please check connection and try again.");
			return;
		}

 		editorComposite = form.getBody();
 		editorComposite.setLayout(new GridLayout());
 		editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// /createContextMenu(editorComposite);
 
 		createReportHeaderLayout(editorComposite);
 		Composite attribComp = createAttributeLayout(editorComposite);
 		createCustomAttributeLayout(attribComp);
 		createDescriptionLayout(editorComposite);
 		createAttachmentLayout(editorComposite);
 		createCommentLayout(editorComposite, form);
 		createActionsLayout(editorComposite);
 
		// editorComposite.setMenu(contextMenuManager.createContextMenu(editorComposite));

 		form.reflow(true);
 		getSite().getPage().addSelectionListener(selectionListener);
 		getSite().setSelectionProvider(selectionProvider);
 	}
 
 	// @Override
 	// public void createPartControl(Composite parent) {
 	// if (getRepositoryTaskData() == null) {
 	// Composite composite = new Composite(parent, SWT.NULL);
 	// composite.setLayout(new GridLayout());
 	// Label noBugLabel = new Label(composite, SWT.NULL);
 	// noBugLabel.setText("Could not download task data, possibly due to timeout
 	// or connectivity problem.\n"
 	// + "Please check connection and try again.");
 	// return;
 	// }
 	//
 	// toolkit = new FormToolkit(parent.getDisplay());
 	// form = toolkit.createScrolledForm(parent);
 	//
 	// editorComposite = form.getBody();
 	// editorComposite.setLayout(new GridLayout());
 	// editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 	// createContextMenu(editorComposite);
 	//
 	// createReportHeaderLayout(editorComposite);
 	// Composite attribComp = createAttributeLayout(editorComposite);
 	// createCustomAttributeLayout(attribComp);
 	// createDescriptionLayout(editorComposite);
 	// createAttachmentLayout(editorComposite);
 	// createCommentLayout(editorComposite, form);
 	// createActionsLayout(editorComposite);
 	//
 	// //
 	// editorComposite.setMenu(contextMenuManager.createContextMenu(editorComposite));
 	//
 	// form.reflow(true);
 	// getSite().getPage().addSelectionListener(selectionListener);
 	// getSite().setSelectionProvider(selectionProvider);
 	// }
 
 	/**
 	 * By default puts task number, date opened and date modified in header
 	 */
 	protected void createReportHeaderLayout(Composite composite) {
 		addSummaryText(composite);
 
 		Composite headerInfoComposite = toolkit.createComposite(composite);
 		headerInfoComposite.setLayout(new GridLayout(6, false));
 		toolkit.createLabel(headerInfoComposite, "ID: ").setFont(TITLE_FONT);
 		toolkit.createText(headerInfoComposite, "" + getRepositoryTaskData().getId(), SWT.FLAT | SWT.READ_ONLY);
 
 		toolkit.createLabel(headerInfoComposite, " Opened: ").setFont(TITLE_FONT);
 		String openedDateString = "";
 		if (getRepositoryTaskData().getCreated() != null) {
 			openedDateString = getRepositoryTaskData().getCreated();
 		}
 		toolkit.createText(headerInfoComposite, openedDateString, SWT.FLAT | SWT.READ_ONLY);
 		toolkit.createLabel(headerInfoComposite, " Modified: ").setFont(TITLE_FONT);
 		toolkit.createText(headerInfoComposite, getRepositoryTaskData().getLastModified(), SWT.FLAT | SWT.READ_ONLY);
 	}
 
 	/**
 	 * Creates the attribute layout, which contains most of the basic attributes
 	 * of the bug (some of which are editable).
 	 */
 	protected Composite createAttributeLayout(Composite composite) {
 
 		String title = getTitleString();
 		Section section = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR | Section.TWISTIE);
 		section.setText(LABEL_SECTION_ATTRIBUTES);
 		section.setExpanded(true);
 		section.setLayout(new GridLayout());
 		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		section.addExpansionListener(new IExpansionListener() {
 			public void expansionStateChanging(ExpansionEvent e) {
 				form.reflow(true);
 			}
 
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 
 		// Attributes Composite- this holds all the combo fields and text fields
 		Composite attributesComposite = toolkit.createComposite(section);
 		GridLayout attributesLayout = new GridLayout();
 		attributesLayout.numColumns = 4;
 		attributesLayout.horizontalSpacing = 14;
 		attributesLayout.verticalSpacing = 6;
 		attributesComposite.setLayout(attributesLayout);
 		GridData attributesData = new GridData(GridData.FILL_BOTH);
 		attributesData.horizontalSpan = 1;
 		attributesData.grabExcessVerticalSpace = false;
 		attributesComposite.setLayoutData(attributesData);
 		section.setClient(attributesComposite);
 		editorInput.setToolTipText(title);
 
 		int currentCol = 1;
 
 		for (RepositoryTaskAttribute attribute : getRepositoryTaskData().getAttributes()) {
 			String name = attribute.getName();
 			String value = "";
 			value = checkText(attribute.getValue());
 			if (attribute.isHidden())
 				continue;
 			Map<String, String> values = attribute.getOptionValues();
 			if (values == null)
 				values = new HashMap<String, String>();
 
 			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 			data.horizontalSpan = 1;
 			data.horizontalIndent = HORZ_INDENT;
 
 			if (attribute.hasOptions() && !attribute.isReadOnly()) {
 				toolkit.createLabel(attributesComposite, name);
 				attributeCombo = new CCombo(attributesComposite, SWT.FLAT | SWT.READ_ONLY);
 				toolkit.adapt(attributeCombo, true, true);
 				attributeCombo.setFont(TEXT_FONT);
 				attributeCombo.setLayoutData(data);
 				Set<String> s = values.keySet();
 				String[] a = s.toArray(new String[s.size()]);
 				for (int i = 0; i < a.length; i++) {
 					attributeCombo.add(a[i]);
 				}
 				if (attributeCombo.indexOf(value) != -1) {
 					attributeCombo.select(attributeCombo.indexOf(value));
 				}
 				attributeCombo.addSelectionListener(new ComboSelectionListener(attributeCombo));
 				comboListenerMap.put(attributeCombo, attribute);
 				attributeCombo.addListener(SWT.FocusIn, new GenericListener());
 				currentCol += 2;
 			} else {
 				toolkit.createLabel(attributesComposite, name);
 				Composite textFieldComposite = toolkit.createComposite(attributesComposite);
 				GridLayout textLayout = new GridLayout();
 				textLayout.marginWidth = 1;
 				textFieldComposite.setLayout(textLayout);
 				GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 				textData.horizontalSpan = 1;
 				textData.widthHint = 135;
 
 				if (attribute.isReadOnly()) {
 					final Text text = toolkit.createText(textFieldComposite, value, SWT.FLAT | SWT.READ_ONLY);
 					text.setLayoutData(textData);
 				} else {
 					final Text text = toolkit.createText(textFieldComposite, value, SWT.FLAT);
 					// text.setFont(COMMENT_FONT);
 					text.setLayoutData(textData);
 					toolkit.paintBordersFor(textFieldComposite);
 					text.setData(attribute);
 					text.addListener(SWT.KeyUp, new Listener() {
 						public void handleEvent(Event event) {
 							String sel = text.getText();
 							RepositoryTaskAttribute a = (RepositoryTaskAttribute) text.getData();
 							if (!(a.getValue().equals(sel))) {
 								a.setValue(sel);
 								markDirty(true);
 							}
 						}
 					});
 					text.addListener(SWT.FocusIn, new GenericListener());
 				}
 
 				currentCol += 2;
 			}
 			if (currentCol > attributesLayout.numColumns) {
 				currentCol -= attributesLayout.numColumns;
 			}
 		}
 		toolkit.paintBordersFor(attributesComposite);
 		// make sure that we are in the first column
 		if (currentCol > 1) {
 			while (currentCol <= attributesLayout.numColumns) {
 				toolkit.createLabel(attributesComposite, "");
 				// newLayout(attributesComposite, 1, "", PROPERTY);
 				currentCol++;
 			}
 		}
 		return attributesComposite;
 	}
 
 	public abstract void createCustomAttributeLayout();
 
 	// protected void createContextMenu(final Composite comp) {
 	// contextMenuManager = new MenuManager(CONTEXT_MENU_ID);
 	// contextMenuManager.setRemoveAllWhenShown(true);
 	// contextMenuManager.addMenuListener(new IMenuListener() {
 	// public void menuAboutToShow(IMenuManager manager) {
 	// manager.add(cutAction);
 	// manager.add(copyAction);
 	// manager.add(pasteAction);
 	// // Clipboard clipboard = new Clipboard(comp.getDisplay());
 	// // TextTransfer textTransfer = TextTransfer.getInstance();
 	// // String textData = (String) clipboard.getContents(textTransfer);
 	// // if (textData != null) {
 	// // pasteAction.setEnabled(true);
 	// // } else {
 	// // pasteAction.setEnabled(false);
 	// // }
 	//
 	// if (currentSelectedText == null ||
 	// currentSelectedText.getSelectionText().length() == 0) {
 	// copyAction.setEnabled(false);
 	// } else {
 	// copyAction.setEnabled(true);
 	// }
 	// // manager.add(revealAllAction);
 	// manager.add(new Separator());
 	// manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
 	// }
 	// });
 	// // getSite().registerContextMenu(CONTEXT_MENU_ID, contextMenuManager,
 	// // getSite().getSelectionProvider());
 	// }
 
 	/**
 	 * Adds a text field to display and edit the bug's summary.
 	 * 
 	 * @param attributesComposite
 	 *            The composite to add the text field to.
 	 */
 	protected void addSummaryText(Composite attributesComposite) {
 
 		Composite summaryComposite = toolkit.createComposite(attributesComposite);
 		summaryComposite.setLayout(new GridLayout(2, false));
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(summaryComposite);
 
 		toolkit.createLabel(summaryComposite, "Summary:").setFont(TITLE_FONT);
 		summaryText = toolkit.createText(summaryComposite, getRepositoryTaskData().getSummary(), SWT.FLAT);
 		IThemeManager themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
 		Font summaryFont = themeManager.getCurrentTheme().getFontRegistry()
 				.get(TaskListColorsAndFonts.TASK_EDITOR_FONT);
 		summaryText.setFont(summaryFont);
 		GridData summaryTextData = new GridData(GridData.FILL_HORIZONTAL);// HORIZONTAL_ALIGN_FILL
 		summaryTextData.horizontalSpan = 1;
 		// summaryTextData.widthHint = 200;
 
 		summaryText.setLayoutData(summaryTextData);
 		summaryText.addListener(SWT.KeyUp, new SummaryListener());
 		summaryText.addListener(SWT.FocusIn, new GenericListener());
 		toolkit.paintBordersFor(summaryComposite);
 	}
 
 	protected void createAttachmentLayout(Composite composite) {
 		Section section = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR | Section.TWISTIE);
 		section.setText(LABEL_SECTION_ATTACHMENTS);
 		section.setExpanded(getRepositoryTaskData().getAttachments().size() > 0);
 		section.setLayout(new GridLayout());
 		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		final Composite attachmentsComposite = toolkit.createComposite(section);
 		attachmentsComposite.setLayout(new GridLayout(2, false));
 		attachmentsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		section.setClient(attachmentsComposite);
 
 		if (getRepositoryTaskData().getAttachments().size() > 0) {
 
 			attachmentsTable = toolkit.createTable(attachmentsComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
 			attachmentsTable.setLinesVisible(true);
 			attachmentsTable.setHeaderVisible(true);
 			attachmentsTable.setLayout(new GridLayout());
 			GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
 			// tableGridData.heightHint = 100;
 			tableGridData.widthHint = DESCRIPTION_WIDTH;
 			attachmentsTable.setLayoutData(tableGridData);
 
 			for (int i = 0; i < attachmentsColumns.length; i++) {
 				TableColumn column = new TableColumn(attachmentsTable, SWT.LEFT, i);
 				column.setText(attachmentsColumns[i]);
 				column.setWidth(attachmentsColumnWidths[i]);
 			}
 
 			attachmentsTableViewer = new TableViewer(attachmentsTable);
 			attachmentsTableViewer.setUseHashlookup(true);
 			attachmentsTableViewer.setColumnProperties(attachmentsColumns);
 
 			final AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
 					getRepositoryTaskData().getRepositoryKind());
 
 			if (connector != null) {
 				final IOfflineTaskHandler offlineHandler = connector.getOfflineTaskHandler();
 				if (offlineHandler != null) {
 					attachmentsTableViewer.setSorter(new ViewerSorter() {
 						public int compare(Viewer viewer, Object e1, Object e2) {
 							RepositoryAttachment attachment1 = (RepositoryAttachment) e1;
 							RepositoryAttachment attachment2 = (RepositoryAttachment) e2;
 							Date created1 = offlineHandler.getDateForAttributeType(
 									RepositoryTaskAttribute.ATTACHMENT_DATE, attachment1.getDateCreated());
 							Date created2 = offlineHandler.getDateForAttributeType(
 									RepositoryTaskAttribute.ATTACHMENT_DATE, attachment2.getDateCreated());
 							if (created1 != null && created2 != null) {
 								return attachment1.getDateCreated().compareTo(attachment2.getDateCreated());
 							} else {
 								return 0;
 							}
 						}
 					});
 				}
 
 			}
 
 			attachmentsTableViewer.setContentProvider(new IStructuredContentProvider() {
 				public Object[] getElements(Object inputElement) {
 					return getRepositoryTaskData().getAttachments().toArray();
 				}
 
 				public void dispose() {
 					// ignore
 				}
 
 				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 					if (!viewer.getControl().isDisposed()) {
 						viewer.refresh();
 					}
 				}
 			});
 
 			attachmentsTableViewer.setLabelProvider(new AttachmentTableLabelProvider(new AttachmentLabelProvider(),
 					PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
 
 			attachmentsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
 				public void doubleClick(DoubleClickEvent event) {
 					String address = repository.getUrl() + ATTACHMENT_URL_SUFFIX;
 					if (!event.getSelection().isEmpty()) {
 						StructuredSelection selection = (StructuredSelection) event.getSelection();
 						RepositoryAttachment attachment = (RepositoryAttachment) selection.getFirstElement();
 						address += attachment.getId() + "&amp;action=view";
 						;
 						TaskUiUtil.openUrl(address);
 					}
 				}
 			});
 
 			attachmentsTableViewer.setInput(getRepositoryTaskData());
 
 			final MenuManager popupMenu = new MenuManager();
 			popupMenu.add(new Action(LABEL_OPEN_IN_BROWSER) {
 				public void run() {
 					RepositoryAttachment att = (RepositoryAttachment) (((StructuredSelection) attachmentsTableViewer
 							.getSelection()).getFirstElement());
 					String url = repository.getUrl() + ATTACHMENT_URL_SUFFIX + att.getId();
 					TaskUiUtil.openUrl(url);
 				}
 			});
 
 			popupMenu.add(new Action(SaveRemoteFileAction.TITLE) {
 				public void run() {
 					RepositoryAttachment att = (RepositoryAttachment) (((StructuredSelection) attachmentsTableViewer
 							.getSelection()).getFirstElement());
 					/* Launch Browser */
 					FileDialog fileChooser = new FileDialog(attachmentsTable.getShell(), SWT.SAVE);
 					String fname = att.getAttributeValue(ATTR_FILENAME);
 					// Default name if none is found
 					if (fname.equals("")) {
 						String ctype = att.getContentType();
 						if (ctype.endsWith(CTYPE_HTML)) {
 							fname = ATTACHMENT_DEFAULT_NAME + ".html";
 						} else if (ctype.startsWith(CTYPE_TEXT)) {
 							fname = ATTACHMENT_DEFAULT_NAME + ".txt";
 						} else if (ctype.endsWith(CTYPE_OCTET_STREAM)) {
 							fname = ATTACHMENT_DEFAULT_NAME;
 						} else if (ctype.endsWith(CTYPE_ZIP)) {
 							fname = ATTACHMENT_DEFAULT_NAME + "." + CTYPE_ZIP;
 						} else {
 							fname = ATTACHMENT_DEFAULT_NAME + "." + ctype.substring(ctype.indexOf("/") + 1);
 						}
 					}
 					fileChooser.setFileName(fname);
 					String file = fileChooser.open();
 
 					// Check if the dialog was canceled or an error occured
 					if (file == null) {
 						return;
 					}
 					SaveRemoteFileAction save = new SaveRemoteFileAction();
 					save.setDestinationFilePath(file);
 					save.setInputStream(getAttachmentInputStream(repository.getUrl() + ATTACHMENT_URL_SUFFIX
 							+ att.getId()));
 					save.run();
 				}
 			});
 			final Action copyToClip = new Action(CopyToClipboardAction.TITLE) {
 				public void run() {
 					RepositoryAttachment att = (RepositoryAttachment) (((StructuredSelection) attachmentsTableViewer
 							.getSelection()).getFirstElement());
 					CopyToClipboardAction copyToClip = new CopyToClipboardAction();
 					copyToClip.setContents(getAttachmentContents(repository.getUrl() + ATTACHMENT_URL_SUFFIX
 							+ att.getId()));
 					copyToClip.setControl(attachmentsTable.getParent());
 					copyToClip.run();
 				}
 			};
 			copyToClip.setId("ID_COPY_TO_CLIPBOARD");
 
 			final Action applyPatch = new Action("Apply Patch...") {
 				public void run() {
 					// RepositoryAttachment att =
 					// (RepositoryAttachment)(((StructuredSelection)attachmentsTableViewer.getSelection()).getFirstElement());
 					// implementation pending bug 98707
 				}
 			};
 			applyPatch.setId("ID_APPLY_PATCH");
 			applyPatch.setEnabled(false); // pending bug 98707
 
 			popupMenu.add(new Separator());
 			Menu menu = popupMenu.createContextMenu(attachmentsTable);
 			attachmentsTable.setMenu(menu);
 
 			/*
 			 * Add the apply patch option if the attachment is a patch. Add the
 			 * copy to clipboard option if the attachment is text or xml
 			 */
 			attachmentsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 				public void selectionChanged(SelectionChangedEvent e) {
 					RepositoryAttachment att = (RepositoryAttachment) (((StructuredSelection) e.getSelection())
 							.getFirstElement());
 					popupMenu.remove("ID_APPLY_PATCH");
 					popupMenu.remove("ID_COPY_TO_CLIPBOARD");
 					if (att.getContentType().startsWith(CTYPE_TEXT) || att.getContentType().endsWith("xml")) {
 						popupMenu.add(copyToClip);
 					}
 					if (att.isPatch()) {
 						popupMenu.add(applyPatch);
 					}
 				}
 			});
 
 		} else {
 			toolkit.createLabel(attachmentsComposite, "No attachments");
 			toolkit.createLabel(attachmentsComposite, "");
 		}
 
 		/* Launch a NewAttachemntWizard */
 		Button addAttachmentButton = toolkit.createButton(attachmentsComposite, "Add...", SWT.PUSH);
 		final Text newAttachment = new Text(attachmentsComposite, SWT.LEFT);
 		newAttachment.setEditable(false);
 		newAttachment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		newAttachment.setBackground(form.getBackground());
 
 		addAttachmentButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// ignore
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				NewAttachmentWizard naw = new NewAttachmentWizard();
 				NewAttachmentWizardDialog dialog = new NewAttachmentWizardDialog(attachmentsComposite.getShell(), naw);
 				naw.setDialog(dialog);
 				dialog.create();
 				dialog.open();
 				if (dialog.getReturnCode() == WizardDialog.CANCEL) {
 					getRepositoryTaskData().setNewAttachment(null);
 				} else {
 					final LocalAttachment att = naw.getAttachment();
 					att.setReport(getRepositoryTaskData());
 					getRepositoryTaskData().setNewAttachment(att);
 					setAttachContext(att.isPatch());
 
 					// TODO: Add row to table
 					// RepositoryTaskData data = getRepositoryTaskData();
 					// data.addAttachment(new DisplayableLocalAttachment(att));
 					// attachmentsTableViewer.setInput(data);
 					newAttachment.setText((new File(att.getFilePath())).getName() + " <not yet submitted>");
 				}
 			}
 		});
 	}
 
 	public static InputStream getAttachmentInputStream(String url) {
 		URLConnection urlConnect;
 		InputStream stream = null;
 		try {
 			urlConnect = (new URL(url)).openConnection();
 			urlConnect.connect();
 			stream = urlConnect.getInputStream();
 
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return stream;
 	}
 
 	public static String getAttachmentContents(String url) {
 		URLConnection urlConnect;
 		StringBuffer contents = new StringBuffer();
 		try {
 			urlConnect = (new URL(url)).openConnection();
 			urlConnect.connect();
 			BufferedInputStream stream = new BufferedInputStream(urlConnect.getInputStream());
 			int c;
 			while ((c = stream.read()) != -1) {
 				/* TODO jpound - handle non-text */
 				contents.append((char) c);
 			}
 			stream.close();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return contents.toString();
 	}
 
 	protected abstract void createCustomAttributeLayout(Composite composite);
 
 	protected void createDescriptionLayout(Composite composite) {
 		final Section section = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR | Section.TWISTIE);
 		section.setText(LABEL_SECTION_DESCRIPTION);
 		section.setExpanded(true);
 		section.setLayout(new GridLayout());
 		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		section.addExpansionListener(new IExpansionListener() {
 			public void expansionStateChanging(ExpansionEvent e) {
 				form.reflow(true);
 			}
 
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 
 		final Composite sectionComposite = toolkit.createComposite(section);
 		section.setClient(sectionComposite);
 		GridLayout addCommentsLayout = new GridLayout();
 		addCommentsLayout.numColumns = 1;
 		sectionComposite.setLayout(addCommentsLayout);
 		GridData sectionCompositeData = new GridData(GridData.FILL_HORIZONTAL);
 		sectionComposite.setLayoutData(sectionCompositeData);
 
 		TextViewer viewer = addRepositoryTextViewer(repository, sectionComposite, getRepositoryTaskData()
 				.getDescription(), SWT.MULTI | SWT.WRAP);
 		final StyledText styledText = viewer.getTextWidget();
 		styledText.addListener(SWT.FocusIn, new DescriptionListener());
 		styledText.setLayout(new GridLayout());
 		GridDataFactory.fillDefaults().hint(DESCRIPTION_WIDTH, SWT.DEFAULT).applyTo(styledText);
 
 		texts.add(textsindex, styledText);
 		textHash.put(getRepositoryTaskData().getDescription(), styledText);
 		textsindex++;
 	}
 
 	/**
 	 * A listener for selection of the description field.
 	 */
 	protected class DescriptionListener implements Listener {
 		public void handleEvent(Event event) {
 			fireSelectionChanged(new SelectionChangedEvent(selectionProvider,
 					new StructuredSelection(new RepositoryTaskSelection(getRepositoryTaskData().getId(),
 							getRepositoryTaskData().getRepositoryUrl(), LABEL_SECTION_DESCRIPTION, true,
 							getRepositoryTaskData().getSummary()))));
 		}
 	}
 
 	protected void createCommentLayout(Composite composite, final ScrolledForm form) {
 		Section section = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR | Section.TWISTIE);
 		section.setText(LABEL_SECTION_COMMENTS);
 		section.setExpanded(true);
 		section.setLayout(new GridLayout());
 		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		section.addExpansionListener(new IExpansionListener() {
 			public void expansionStateChanging(ExpansionEvent e) {
 				form.reflow(true);
 			}
 
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 
 		ImageHyperlink hyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
 		hyperlink.setBackgroundMode(SWT.INHERIT_NONE);
 		hyperlink.setBackground(section.getTitleBarBackground());
 		hyperlink.setImage(TaskListImages.getImage(TaskListImages.EXPAND_ALL));
 		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
 			public void linkActivated(HyperlinkEvent e) {
 				revealAllComments();
 			}
 		});
 
 		section.setTextClient(hyperlink);
 
 		// Additional (read-only) Comments Area
 		Composite addCommentsComposite = toolkit.createComposite(section);
 		section.setClient(addCommentsComposite);
 		GridLayout addCommentsLayout = new GridLayout();
 		addCommentsLayout.numColumns = 1;
 		addCommentsComposite.setLayout(addCommentsLayout);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(addCommentsComposite);
 
 		StyledText styledText = null;
 		for (Iterator<TaskComment> it = getRepositoryTaskData().getComments().iterator(); it.hasNext();) {
 			final TaskComment taskComment = it.next();
 
 			// skip comment 0 as it is the description
 			if (taskComment.getNumber() == 0)
 				continue;
 
 			ExpandableComposite expandableComposite = toolkit.createExpandableComposite(addCommentsComposite,
 					ExpandableComposite.TREE_NODE);
 
 			// if (comment.getCreated().after(lastSynced)) {
 			if (!it.hasNext()) {
 				expandableComposite.setExpanded(true);
 			}
 
 			expandableComposite.setText(taskComment.getNumber() + ": " + taskComment.getAuthorName() + ", "
 					+ taskComment.getCreated());
 
 			expandableComposite.addExpansionListener(new ExpansionAdapter() {
 				public void expansionStateChanged(ExpansionEvent e) {
 					form.reflow(true);
 				}
 			});
 
 			expandableComposite.setLayout(new GridLayout());
 			expandableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			Composite ecComposite = toolkit.createComposite(expandableComposite);
 			GridLayout ecLayout = new GridLayout();
 			ecLayout.marginHeight = 0;
 			ecLayout.marginBottom = 10;
 			ecLayout.marginLeft = 10;
 			ecComposite.setLayout(ecLayout);
 			ecComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			expandableComposite.setClient(ecComposite);
 
 			TextViewer viewer = addRepositoryTextViewer(repository, ecComposite, taskComment.getText(), SWT.MULTI
 					| SWT.WRAP);
 			styledText = viewer.getTextWidget();
 			GridDataFactory.fillDefaults().hint(DESCRIPTION_WIDTH, SWT.DEFAULT).applyTo(styledText);
 
 			// code for outline
 			commentStyleText.add(styledText);
 			texts.add(textsindex, styledText);
 			textHash.put(taskComment, styledText);
 			textsindex++;
 		}
 
 		Section sectionAdditionalComments = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR
 				| Section.TWISTIE);
 		sectionAdditionalComments.setText(LABEL_SECTION_NEW_COMMENT);
 		sectionAdditionalComments.setExpanded(true);
 
 		sectionAdditionalComments.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		sectionAdditionalComments.addExpansionListener(new IExpansionListener() {
 			public void expansionStateChanging(ExpansionEvent e) {
 				form.reflow(true);
 			}
 
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 
 		Composite newCommentsComposite = toolkit.createComposite(sectionAdditionalComments);
 		newCommentsComposite.setLayout(new GridLayout());
 
 		final TextViewer newCommentTextViewer = addRepositoryTextViewer(repository, newCommentsComposite,
 				getRepositoryTaskData().getNewComment(), SWT.FLAT | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
 		newCommentTextViewer.setEditable(true);
 
 		GridData addCommentsTextData = new GridData(GridData.FILL_HORIZONTAL);
 		addCommentsTextData.widthHint = DESCRIPTION_WIDTH;
 		addCommentsTextData.heightHint = DESCRIPTION_HEIGHT;
 		addCommentsTextData.grabExcessHorizontalSpace = true;
 		newCommentTextViewer.getTextWidget().setLayoutData(addCommentsTextData);
 		newCommentTextViewer.getTextWidget().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
 
 		newCommentTextViewer.getTextWidget().addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				String sel = addCommentsTextBox.getText();
 				if (!(getRepositoryTaskData().getNewComment().equals(sel))) {
 					getRepositoryTaskData().setNewComment(sel);
 					markDirty(true);
 				}
 				validateInput();
 			}
 		});
 
 		newCommentTextViewer.getTextWidget().addListener(SWT.FocusIn, new NewCommentListener());
 		addCommentsTextBox = newCommentTextViewer.getTextWidget();
 
 		sectionAdditionalComments.setClient(newCommentsComposite);
 
 		toolkit.paintBordersFor(newCommentsComposite);
 
 	}
 
 	protected abstract void validateInput();
 
 	/**
 	 * Creates the button layout. This displays options and buttons at the
 	 * bottom of the editor to allow actions to be performed on the bug.
 	 */
 	protected void createActionsLayout(Composite formComposite) {
 		Section section = toolkit.createSection(formComposite, ExpandableComposite.TITLE_BAR | Section.TWISTIE);
 		section.setText(LABEL_SECTION_ACTIONS);
 		section.setExpanded(true);
 		section.setLayout(new GridLayout());
 		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		section.addExpansionListener(new IExpansionListener() {
 			public void expansionStateChanging(ExpansionEvent e) {
 				form.reflow(true);
 			}
 
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 
 		Composite buttonComposite = toolkit.createComposite(section);
 		GridLayout buttonLayout = new GridLayout();
 		buttonLayout.numColumns = 4;
 		buttonComposite.setLayout(buttonLayout);
 		GridData buttonData = new GridData(GridData.FILL_BOTH);
 		buttonData.horizontalSpan = 1;
 		buttonData.grabExcessVerticalSpace = false;
 		buttonComposite.setLayoutData(buttonData);
 		section.setClient(buttonComposite);
 		addRadioButtons(buttonComposite);
 		addActionButtons(buttonComposite);
 	}
 
 	/**
 	 * Adds radio buttons to this composite.
 	 * 
 	 * @param buttonComposite
 	 *            Composite to add the radio buttons to.
 	 */
 	abstract protected void addRadioButtons(Composite buttonComposite);
 
 	/**
 	 * Adds buttons to this composite. Subclasses can override this method to
 	 * provide different/additional buttons.
 	 * 
 	 * @param buttonComposite
 	 *            Composite to add the buttons to.
 	 */
 	protected void addActionButtons(Composite buttonComposite) {
 		submitButton = toolkit.createButton(buttonComposite, LABEL_BUTTON_SUBMIT, SWT.NONE);
 		GridData submitButtonData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		submitButton.setLayoutData(submitButtonData);
 		submitButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				submitBug();
 			}
 		});
 		submitButton.addListener(SWT.FocusIn, new GenericListener());
 
 	}
 
 	/**
 	 * Make sure that a String that is <code>null</code> is changed to a null
 	 * string
 	 * 
 	 * @param text
 	 *            The text to check if it is null or not
 	 * @return If the text is <code>null</code>, then return the null string (<code>""</code>).
 	 *         Otherwise, return the text.
 	 */
 	public static String checkText(String text) {
 		if (text == null)
 			return "";
 		else
 			return text;
 	}
 
 	/**
 	 * @return A string to use as a title for this editor.
 	 */
 	protected abstract String getTitleString();
 
 	/**
 	 * This refreshes the text in the title label of the info area (it contains
 	 * elements which can change).
 	 */
 	protected void setGeneralTitleText() {
 		// String text = "[Open in Internal Browser]";
 		// linkToBug.setText(text);
 		// linkToBug.setFont(TEXT_FONT);
 		// if (this instanceof ExistingBugEditor) {
 		// linkToBug.setUnderlined(true);
 		// linkToBug.setForeground(JFaceColors.getHyperlinkText(Display.getCurrent()));
 		// linkToBug.addMouseListener(new MouseListener() {
 		//
 		// public void mouseDoubleClick(MouseEvent e) {
 		// }
 		//
 		// public void mouseUp(MouseEvent e) {
 		// }
 		//
 		// public void mouseDown(MouseEvent e) {
 		// TaskListUiUtil.openUrl(getTitle(), getTitleToolTip(),
 		// BugzillaRepositoryUtil.getBugUrlWithoutLogin(
 		// bugzillaInput.getBug().getRepositoryUrl(),
 		// bugzillaInput.getBug().getId()));
 		// if (e.stateMask == SWT.MOD3) {
 		// // XXX come back to look at this ui
 		// close();
 		// }
 		//
 		// }
 		// });
 		// } else {
 		// linkToBug.setEnabled(false);
 		// }
 		// linkToBug.addListener(SWT.FocusIn, new GenericListener());
 		//
 		// // Resize the composite, in case the new summary is longer than the
 		// // previous one.
 		// // Then redraw it to show the changes.
 		// linkToBug.getParent().pack(true);
 		// linkToBug.redraw();
 
 		// String text = getTitleString();
 		// generalTitleText.setText(text);
 		// StyleRange sr = new StyleRange(generalTitleText.getOffsetAtLine(0),
 		// text.length(), foreground, background,
 		// SWT.BOLD);
 		// generalTitleText.setStyleRange(sr);
 		// generalTitleText.addListener(SWT.FocusIn, new GenericListener());
 		//
 		// // Resize the composite, in case the new summary is longer than the
 		// // previous one.
 		// // Then redraw it to show the changes.
 		// generalTitleText.getParent().pack(true);
 		// generalTitleText.redraw();
 	}
 
 	protected abstract void submitBug();
 
 	/**
 	 * If there is no locally saved copy of the current bug, then it saved
 	 * offline. Otherwise, any changes are updated in the file.
 	 */
 	public void saveBug() {
 		try {
 			updateBug();
 
 			final AbstractRepositoryConnector repositoryClient = (AbstractRepositoryConnector) TasksUiPlugin
 					.getRepositoryManager().getRepositoryConnector(getRepositoryTaskData().getRepositoryKind());
 
 			IEditorInput input = this.getEditorInput();
 			if (input instanceof ExistingBugEditorInput) {
 				ExistingBugEditorInput existingInput = (ExistingBugEditorInput) input;
 				AbstractRepositoryTask repositoryTask = existingInput.getRepositoryTask();
 				// AbstractRepositoryTask repositoryTask = getRepositoryTask();
 				if (repositoryTask != null) {
 					// if (getRepositoryTaskData().hasChanges()) {
 					// repositoryTask.setSyncState(RepositoryTaskSyncState.OUTGOING);
 					// } else {
 					// repositoryTask.setSyncState(RepositoryTaskSyncState.SYNCHRONIZED);
 					// }
 					if (getRepositoryTaskData().hasLocalChanges() == true) {
 						repositoryClient.updateOfflineState(repositoryTask, getRepositoryTaskData(), false);
 					}
 					TasksUiPlugin.getTaskListManager().getTaskList().notifyRepositoryInfoChanged(repositoryTask);
 				}
 
 				// For new bug reports something along these lines...
 				// repositoryClient.saveOffline(getRepositoryTaskData());
 			}
 			markDirty(false);
 			if (parentEditor != null) {
 				parentEditor.notifyTaskChanged();
 			}
 		} catch (Exception e) {
 			MylarStatusHandler.fail(e, "bug save offline failed", true);
 		}
 
 	}
 
 	protected abstract void updateBug();
 
 	/**
 	 * Refreshes any text labels in the editor that contain information that
 	 * might change.
 	 */
 	protected void updateEditor() {
 		setGeneralTitleText();
 	}
 
 	@Override
 	public void setFocus() {
 		form.setFocus();
 	}
 
 	// /**
 	// * Updates the dirty status of this editor page. The dirty status is true
 	// if
 	// * the bug report has been modified but not saved. The title of the editor
 	// * is also updated to reflect the status.
 	// *
 	// * @param newDirtyStatus
 	// * is true when the bug report has been modified but not saved
 	// */
 	// public void changeDirtyStatus(boolean newDirtyStatus) {
 	// isDirty = newDirtyStatus;
 	// // if (parentEditor == null) {
 	// // firePropertyChange(PROP_DIRTY);
 	// // } else {
 	// // parentEditor.markDirty();
 	// // }
 	// getManagedForm().dirtyStateChanged();
 	// }
 
 	/**
 	 * Updates the title of the editor to reflect dirty status. If the bug
 	 * report has been modified but not saved, then an indicator will appear in
 	 * the title.
 	 */
 	protected void updateEditorTitle() {
 		setPartName(editorInput.getName());
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		saveBug();
 		updateEditor();
 	}
 
 	@Override
 	public void doSaveAs() {
 		// we don't save, so no need to implement
 	}
 
 	/**
 	 * @return The composite for the whole editor.
 	 */
 	public Composite getEditorComposite() {
 		return editorComposite;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		isDisposed = true;
 		getSite().getPage().removeSelectionListener(selectionListener);
 	}
 
 	// public void handleEvent(Event event) {
 	// if (event.widget instanceof CCombo) {
 	// CCombo combo = (CCombo) event.widget;
 	// if (comboListenerMap.containsKey(combo)) {
 	// if (combo.getSelectionIndex() > -1) {
 	// String sel = combo.getItem(combo.getSelectionIndex());
 	// Attribute attribute = getBug().getAttribute(comboListenerMap.get(combo));
 	// if (sel != null && !(sel.equals(attribute.getNewValue()))) {
 	// attribute.setNewValue(sel);
 	// for (IRepositoryTaskAttributeListener client : attributesListeners) {
 	// client.attributeChanged(attribute.getName(), sel);
 	// }
 	// changeDirtyStatus(true);
 	// }
 	// }
 	// }
 	// }
 	// }
 
 	/**
 	 * Fires a <code>SelectionChangedEvent</code> to all listeners registered
 	 * under <code>selectionChangedListeners</code>.
 	 * 
 	 * @param event
 	 *            The selection event.
 	 */
 	protected void fireSelectionChanged(final SelectionChangedEvent event) {
 		Object[] listeners = selectionChangedListeners.toArray();
 		for (int i = 0; i < listeners.length; i++) {
 			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
 			SafeRunnable.run(new SafeRunnable() {
 				public void run() {
 					l.selectionChanged(event);
 				}
 			});
 		}
 	}
 
 	/**
 	 * A generic listener for selection of unimportant items. The default
 	 * selection item sent out is the entire bug object.
 	 */
 	public class GenericListener implements Listener {
 		public void handleEvent(Event event) {
 			RepositoryTaskData bug = (RepositoryTaskData) getRepositoryTaskData();
 			fireSelectionChanged(new SelectionChangedEvent(selectionProvider, new StructuredSelection(
 					new RepositoryTaskSelection(bug.getId(), bug.getRepositoryUrl(), bug.getLabel(), false, bug
 							.getSummary()))));
 		}
 	}
 
 	/**
 	 * A listener to check if the summary field was modified.
 	 */
 	protected class SummaryListener implements Listener {
 		public void handleEvent(Event event) {
 			handleSummaryEvent();
 		}
 	}
 
 	/**
 	 * Check if the summary field was modified, and update it if necessary.
 	 */
 	public void handleSummaryEvent() {
 		String sel = summaryText.getText();
 		RepositoryTaskAttribute a = getRepositoryTaskData().getAttribute(RepositoryTaskAttribute.SUMMARY);
 		if (!(a.getValue().equals(sel))) {
 			a.setValue(sel);
 			markDirty(true);
 		}
 	}
 
 	/*----------------------------------------------------------*
 	 * CODE TO SCROLL TO A COMMENT OR OTHER PIECE OF TEXT
 	 *----------------------------------------------------------*/
 
 	/** List of the StyledText's so that we can get the previous and the next */
 	protected ArrayList<StyledText> texts = new ArrayList<StyledText>();
 
 	protected HashMap<Object, StyledText> textHash = new HashMap<Object, StyledText>();
 
 	protected List<StyledText> commentStyleText = new ArrayList<StyledText>();
 
 	/** Index into the styled texts */
 	protected int textsindex = 0;
 
 	protected StyledText addCommentsTextBox = null;
 
 	// protected Text descriptionTextBox = null;
 	protected TextViewer newDescriptionTextViewer = null;
 
 	// private FormText previousText = null;
 
 	/**
 	 * Selects the given object in the editor.
 	 * 
 	 * @param commentNumber
 	 *            The comment number to be selected
 	 */
 	public void select(int commentNumber) {
 		if (commentNumber == -1)
 			return;
 
 		for (Object o : textHash.keySet()) {
 			if (o instanceof TaskComment) {
 				if (((TaskComment) o).getNumber() == commentNumber) {
 					select(o, true);
 				}
 			}
 		}
 	}
 
 	public void revealAllComments() {
 		for (StyledText text : commentStyleText) {
 			Composite comp = text.getParent();
 			while (comp != null) {
 				if (comp instanceof ExpandableComposite) {
 					ExpandableComposite ex = (ExpandableComposite) comp;
 					ex.setExpanded(true);
 				}
 				comp = comp.getParent();
 			}
 		}
 	}
 
 	/**
 	 * Selects the given object in the editor.
 	 * 
 	 * @param o
 	 *            The object to be selected.
 	 * @param highlight
 	 *            Whether or not the object should be highlighted.
 	 */
 	public void select(Object o, boolean highlight) {
 		if (textHash.containsKey(o)) {
 			StyledText t = textHash.get(o);
 			if (t != null) {
 				Composite comp = t.getParent();
 				while (comp != null) {
 					if (comp instanceof ExpandableComposite) {
 						ExpandableComposite ex = (ExpandableComposite) comp;
 						ex.setExpanded(true);
 					}
 					comp = comp.getParent();
 				}
 				focusOn(t, highlight);
 			}
 		} else if (o instanceof RepositoryTaskData) {
 			focusOn(null, highlight);
 		}
 	}
 
 	public void selectDescription() {
 		for (Object o : textHash.keySet()) {
 			if (o.equals(editorInput.getRepositoryTaskData().getDescription())) {
 				select(o, true);
 			}
 		}
 	}
 
 	public void selectNewComment() {
 		focusOn(addCommentsTextBox, false);
 	}
 
 	public void selectNewDescription() {
 		focusOn(newDescriptionTextViewer.getTextWidget(), false);
 	}
 
 	/**
 	 * Scroll to a specified piece of text
 	 * 
 	 * @param selectionComposite
 	 *            The StyledText to scroll to
 	 */
 	private void focusOn(Control selectionComposite, boolean highlight) {
 		int pos = 0;
 		// if (previousText != null && !previousText.isDisposed()) {
 		// previousText.setsetSelection(0);
 		// }
 
 		// if (selectionComposite instanceof FormText)
 		// previousText = (FormText) selectionComposite;
 
 		if (selectionComposite != null) {
 
 			// if (highlight && selectionComposite instanceof FormText &&
 			// !selectionComposite.isDisposed())
 			// ((FormText) selectionComposite).set.setSelection(0, ((FormText)
 			// selectionComposite).getText().length());
 
 			// get the position of the text in the composite
 			pos = 0;
 			Control s = selectionComposite;
 			if (s.isDisposed())
 				return;
 			s.setEnabled(true);
 			s.setFocus();
 			s.forceFocus();
 			while (s != null && s != getEditorComposite()) {
 				if (!s.isDisposed()) {
 					pos += s.getLocation().y;
 					s = s.getParent();
 				}
 			}
 
 			pos = pos - 60; // form.getOrigin().y;
 
 		}
 		if (!form.getBody().isDisposed())
 			form.setOrigin(0, pos);
 	}
 
 	private RepositoryTaskOutlinePage outlinePage = null;
 
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (IContentOutlinePage.class.equals(adapter)) {
 			if (outlinePage == null && editorInput != null) {
 				outlinePage = new RepositoryTaskOutlinePage(taskOutlineModel);
 			}
 			return outlinePage;
 		}
 		return super.getAdapter(adapter);
 	}
 
 	public RepositoryTaskOutlineNode getOutlineModel() {
 		return taskOutlineModel;
 	}
 
 	public RepositoryTaskOutlinePage getOutline() {
 		return outlinePage;
 	}
 
 	private boolean isDisposed = false;
 
 	public boolean isDisposed() {
 		return isDisposed;
 	}
 
 	// The implementation of the attach context UI is connector dependant.
 	protected boolean getAttachContext() {
 		return false;
 	}
 
 	// The implementation of the attach context UI is connector dependant.
 	// this method is called when a user attaches a patch to the task
 	protected void setAttachContext(boolean attachContext) {
 
 	}
 
 	public void close() {
 		Display activeDisplay = getSite().getShell().getDisplay();
 		activeDisplay.asyncExec(new Runnable() {
 			public void run() {
 				if (getSite() != null && getSite().getPage() != null && !AbstractRepositoryTaskEditor.this.isDisposed())
 					if (parentEditor != null) {
 						getSite().getPage().closeEditor(parentEditor, false);
 					} else {
 						getSite().getPage().closeEditor(AbstractRepositoryTaskEditor.this, false);
 					}
 			}
 		});
 	}
 
 	public void addAttributeListener(IRepositoryTaskAttributeListener listener) {
 		attributesListeners.add(listener);
 	}
 
 	public void removeAttributeListener(IRepositoryTaskAttributeListener listener) {
 		attributesListeners.remove(listener);
 	}
 
 	public void setParentEditor(MylarTaskEditor parentEditor) {
 		this.parentEditor = parentEditor;
 	}
 
 	public RepositoryTaskOutlineNode getTaskOutlineModel() {
 		return taskOutlineModel;
 	}
 
 	public void setTaskOutlineModel(RepositoryTaskOutlineNode taskOutlineModel) {
 		this.taskOutlineModel = taskOutlineModel;
 	}
 
 	/**
 	 * A listener for selection of the textbox where a new comment is entered
 	 * in.
 	 */
 	protected class NewCommentListener implements Listener {
 		public void handleEvent(Event event) {
 			fireSelectionChanged(new SelectionChangedEvent(selectionProvider, new StructuredSelection(
 					new RepositoryTaskSelection(getRepositoryTaskData().getId(), getRepositoryTaskData()
 							.getRepositoryUrl(), "New Comment", false, getRepositoryTaskData().getSummary()))));
 		}
 	}
 
 	public Control getControl() {
 		return form;
 	}
 
 	public void setSummaryText(String text) {
 		this.summaryText.setText(text);
 		handleSummaryEvent();
 	}
 
 	public void setDescriptionText(String text) {
 		this.newDescriptionTextViewer.setDocument(new Document(text));
 	}
 
 	// private class DisplayableLocalAttachment extends RepositoryAttachment {
 	// private static final long serialVersionUID = 900218036143022422L;
 	//		
 	// private Date date;
 	// private String description;
 	// private String creator;
 	// private String name;
 	//		
 	// public String getCreator() {
 	// return creator;
 	// }
 	// public void setCreator(String creator) {
 	// this.creator = creator;
 	// }
 	// public Date getDateCreated() {
 	// return date;
 	// }
 	// public void setDateCreated(Date date) {
 	// this.date = date;
 	// }
 	// public String getDescription() {
 	// return description;
 	// }
 	// public void setDescription(String description) {
 	// this.description = description;
 	// }
 	// public String getName() {
 	// return name;
 	// }
 	// public void setName(String name) {
 	// this.name = name;
 	// }
 	// public DisplayableLocalAttachment(LocalAttachment att) {
 	// super(null);
 	// setName(att.getFilePath());
 	// setDescription(att.getDescription());
 	// setDateCreated(new Date());
 	// }
 	// }
 }
