 /*
  * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
  * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
  * 
  * For more information on the project, please refer to the this web site:
  * http://www.esdi-humboldt.eu
  * 
  * LICENSE: For information on the license under which this program is 
  * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
  * (c) the HUMBOLDT Consortium, 2007 to 2010.
  */
 package eu.esdihumboldt.hale.rcp.views.map.style;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.LineSymbolizer;
 import org.geotools.styling.PointSymbolizer;
 import org.geotools.styling.PolygonSymbolizer;
 import org.geotools.styling.Rule;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleBuilder;
 import org.geotools.styling.Symbolizer;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.filter.Filter;
 
 import eu.esdihumboldt.hale.models.impl.StyleServiceImpl;
 import eu.esdihumboldt.hale.rcp.HALEActivator;
 import eu.esdihumboldt.hale.rcp.views.map.style.editors.Editor;
 import eu.esdihumboldt.hale.rcp.views.map.style.editors.EditorFactory;
 import eu.esdihumboldt.hale.rcp.views.map.style.editors.LineSymbolizerEditor;
 import eu.esdihumboldt.hale.rcp.views.map.style.editors.PointSymbolizerEditor;
 import eu.esdihumboldt.hale.rcp.views.map.style.editors.PolygonSymbolizerEditor;
 import eu.esdihumboldt.hale.rcp.views.map.style.editors.RuleEditor;
 
 /**
  * Rule based style editor page
  * 
  * @author Simon Templer
  * @partner 01 / Fraunhofer Institute for Computer Graphics Research
  * @version $Id$ 
  */
 public class RuleStylePage extends FeatureStylePage {
 	
 	/**
 	 * {@link Rule} list item
 	 */
 	private class RuleItem {
 		
 		private Rule rule;
 
 		/**
 		 * Creates a new rule item
 		 * 
 		 * @param rule the rule
 		 */
 		public RuleItem(Rule rule) {
 			this.rule = rule;
 		}
 
 		/**
 		 * Get the item's rule
 		 * 
 		 * @return the rule
 		 */
 		public Rule getRule() {
 			return rule;
 		}
 
 		/**
 		 * Set the item's rule
 		 * 
 		 * @param rule the rule to set
 		 */
 		public void setRule(Rule rule) {
 			this.rule = rule;
 		}
 
 		/**
 		 * @see Object#toString()
 		 */
 		@Override
 		public String toString() {
 			String name = getRule().getName();
 			if (name == null || name.isEmpty()) {
 				return "Rule " + rules.indexOf(this);
 			}
 			else {
 				return name;
 			}
 		}
 
 	}
 	
 	private static final StyleBuilder styleBuilder = new StyleBuilder();
 	
 	private static Image addImage = null;
 	
 	private static Image removeImage = null;
 	
 	private static Image renameImage = null;
 
 	private Composite editorArea;
 	
 	private ListViewer listViewer;
 	
 	private int currentIndex = -1;
 	
 	private Editor<Rule> currentEditor = null;
 	
 	private List<RuleItem> rules;
 	
 	private boolean changed = false;
 
 	/**
 	 * Creates a new editor page
 	 * 
 	 * @param parent the parent dialog
 	 */
 	public RuleStylePage(FeatureStyleDialog parent) {
 		super(parent, "Advanced");
 		
 		if (addImage == null) {
 			addImage = AbstractUIPlugin.imageDescriptorFromPlugin(
 					HALEActivator.PLUGIN_ID, "/icons/add.gif").createImage();
 		}
 		
 		if (removeImage == null) {
 			removeImage = AbstractUIPlugin.imageDescriptorFromPlugin(
 					HALEActivator.PLUGIN_ID, "/icons/remove.gif").createImage();
 		}
 		
 		if (renameImage == null) {
 			renameImage = AbstractUIPlugin.imageDescriptorFromPlugin(
 					HALEActivator.PLUGIN_ID, "/icons/rename.gif").createImage();
 		}
 	}
 
 	/**
 	 * @see FeatureStylePage#getStyle(boolean)
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public Style getStyle(boolean force) throws Exception {
 		updateCurrentRule();
 		
 		if (force || changed) {
 			Rule[] ruleArray = new Rule[rules.size()];
 			for (int i = 0; i < rules.size(); i++) {
 				Rule rule = rules.get(i).getRule();
 				
 				// set else filter
 				rule.setIsElseFilter(rule.getFilter() == null);
 				
 				//TODO other rule manipulation?
 				
 				ruleArray[i] = rule;
 			}
 			
 			// create style
 			FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("Feature", ruleArray);
 			Style style = styleBuilder.createStyle();
 			style.addFeatureTypeStyle(fts);
 			return style;
 		}
 		else {
 			return null;
 		}
 	}
 
 	/**
 	 * Update the {@link Rule} whose editor is currently open
 	 */
 	private void updateCurrentRule() {
 		if (currentEditor != null && currentEditor.isChanged()) {
 			Rule rule = currentEditor.getValue();
 			
 			if (rule != null) {
 				rules.get(currentIndex).setRule(rule);
 				
 				changed = true;
 			}
 		}
 	}
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void createControl(Composite parent) {
 		changed = false;
 		
 		Composite page = new Composite(parent, SWT.NONE);
 		page.setLayout(new GridLayout(2, false));
 		
 		// DISABLED - this method seems to change the rule order - Rule[] ruleArray = SLD.rules(getParent().getStyle());
 		// use list instead:
 		List<Rule> ruleList;
 		try {
 			ruleList = getParent().getStyle().getFeatureTypeStyles()[0].rules();
 		} catch (Exception e) {
 			ruleList = new ArrayList<Rule>();
 		}
 		
 		// init index
 		if (ruleList.size() > 0) {
 			currentIndex = 0;
 		}
 		else {
 			currentIndex = -1;
 		}
 		
 		currentEditor = null;
 		
 		// populate rule map
 		rules = new ArrayList<RuleItem>(ruleList.size() + 5);
 		for (int i = 0; i < ruleList.size(); i++) {
 			Rule rule = ruleList.get(i);
 			if (rule != null) {
 				rules.add(new RuleItem(rule));
 			}
 		}
 		
 		// rule list
 		Composite ruleArea = new Composite(page, SWT.NONE);
 		ruleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
 		ruleArea.setLayout(new GridLayout(3, true));
 		
 		// label
 		Label rulesLabel = new Label(ruleArea, SWT.NONE);
 		rulesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
 		rulesLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
 		rulesLabel.setText("Rules");
 		
 		// viewer
 		listViewer = new ListViewer(ruleArea);
 		listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
 		listViewer.setContentProvider(new IStructuredContentProvider() {
 			
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 				// ignore
 			}
 			
 			@Override
 			public void dispose() {
 				// ignore
 			}
 			
 			@SuppressWarnings("unchecked")
 			@Override
 			public Object[] getElements(Object inputElement) {
 				try {
 					List<RuleItem> rules = (List<RuleItem>) inputElement;
 					return rules.toArray();
 				} catch (Exception e) {
 					return null;
 				}
 			}
 		});
 		listViewer.setInput(rules);
 		
 		if (currentIndex >= 0 && currentIndex < rules.size()) {
 			listViewer.setSelection(new StructuredSelection(rules.get(currentIndex)));
 		}
 		
 		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				RuleItem item = (RuleItem) ((IStructuredSelection) event.getSelection()).getFirstElement();
 				
 				int newIndex = rules.indexOf(item);
 				
 				if (currentIndex != newIndex) {
 					updateCurrentRule();
 					currentIndex = newIndex;
 					updateEditor();
 				}
 			}
 		});
 		
 		// buttons
 		Button addButton = new Button(ruleArea, SWT.PUSH);
 		addButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		addButton.setImage(addImage);
 		addButton.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				addRule();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// ignore
 			}
 		});
 		
 		Button removeButton = new Button(ruleArea, SWT.PUSH);
 		removeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		removeButton.setImage(removeImage);
 		removeButton.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				removeCurrentRule();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// ignore
 			}
 		});
 		
 		Button renameButton = new Button(ruleArea, SWT.PUSH);
 		renameButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		renameButton.setImage(renameImage);
 		renameButton.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				renameCurrentRule();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// ignore
 			}
 		});
 		
 		// editor area
 		editorArea = new Composite(page, SWT.NONE);
		editorArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		editorArea.setLayout(new FillLayout());
 		
 		setControl(page);
 		
 		updateEditor();
 	}
 
 	/**
 	 * Rename the current rule
 	 */
 	protected void renameCurrentRule() {
 		if (currentIndex >= 0 && currentIndex < rules.size()) {
 			RuleItem item = rules.get(currentIndex);
 			Rule rule = item.getRule();
 			
 			InputDialog dlg = new InputDialog(getShell(), "Rule name",
 					"Enter the rule name:", rule.getName(), null);
 			
 			if (dlg.open() == InputDialog.OK) {
 				rule.setName(dlg.getValue());
 				listViewer.update(item, null);
 			}
 		}
 	}
 
 	/**
 	 * Remove the current rule
 	 */
 	protected void removeCurrentRule() {
 		if (currentIndex >= 0 && currentIndex < rules.size()) {
 			RuleItem item = rules.remove(currentIndex);
 			currentIndex--;
 			listViewer.remove(item);
 			updateEditor();
 		}
 	}
 
 	/**
 	 * Add a new {@link Rule}
 	 */
 	protected void addRule() {
 		SymbolizerDialog symDlg = new SymbolizerDialog(getShell());
 		symDlg.open();
 		Symbolizer symbolizer = symDlg.getSymbolizer();
 		
 		if (symbolizer != null) {
 			Rule rule = styleBuilder.createRule(symbolizer);
 			RuleItem item = new RuleItem(rule);
 			rules.add(item);
 			listViewer.add(item);
 		}
 	}
 
 	/**
 	 * Display the editor for the current rule in the editor area
 	 */
 	private void updateEditor() {
 		if (currentEditor != null) {
 			currentEditor.getControl().dispose();
 		}
 		
 		if (currentIndex >= 0 && currentIndex < rules.size()) {
 			Rule rule = rules.get(currentIndex).getRule();
 			
 			currentEditor = createEditor(rule, editorArea);
 			editorArea.layout(true);
 		}
 	}
 
 	/**
 	 * Create a rule editor
 	 * 
 	 * @param rule the rule
 	 * @param parent the parent composite
 	 * 
 	 * @return the {@link Rule} editor
 	 */
 	@SuppressWarnings("deprecation")
 	private Editor<Rule> createEditor(Rule rule, Composite parent) {
 		FeatureType ft = getParent().getType();
 		Filter filter = rule.getFilter();
 		
 		Symbolizer symbolizer = null;
 		Symbolizer[] symbolizers = rule.getSymbolizers();
 		
 		if (symbolizers != null && symbolizers.length > 0) {
 			symbolizer = symbolizers[0];
 		}
 		
 		if (symbolizer == null) {
 			// fallback if there is no symbolizer defined
 			FeatureTypeStyle fts = StyleServiceImpl.getDefaultStyle(ft);
 			symbolizer = fts.rules().get(0).getSymbolizers()[0];
 		}
 		
 		Editor<Rule> editor;
 		
 		if (symbolizer instanceof PointSymbolizer) {
 			editor = createEditor(parent, ft, filter, PointSymbolizer.class,
 					(PointSymbolizer) symbolizer);
 		}
 		else if (symbolizer instanceof PolygonSymbolizer) {
 			editor = createEditor(parent, ft, filter, PolygonSymbolizer.class,
 					(PolygonSymbolizer) symbolizer);
 		}
 		else { //TODO support other symbolizers
 			// default: LineSymbolizer
 			editor = createEditor(parent, ft, filter, LineSymbolizer.class,
 					(LineSymbolizer) symbolizer);
 		}
 		
 		return editor;
 	}
 	
 	private static <T extends Symbolizer> RuleEditor<?> createEditor(Composite parent, FeatureType ft, Filter filter, Class<T> type, T symbolizer) {
 		if (PointSymbolizer.class.isAssignableFrom(type)) {
 			return new RuleEditor<PointSymbolizer>(parent, ft, filter,
 					PointSymbolizer.class, (PointSymbolizer) symbolizer,
 					new EditorFactory<PointSymbolizer>() {
 
 						@Override
 						public Editor<PointSymbolizer> createEditor(
 								Composite parent, PointSymbolizer value) {
 							return new PointSymbolizerEditor(parent, value);
 						}
 					});
 		}
 		else if (PolygonSymbolizer.class.isAssignableFrom(type)) {
 			return new RuleEditor<PolygonSymbolizer>(parent, ft, filter,
 					PolygonSymbolizer.class, (PolygonSymbolizer) symbolizer,
 					new EditorFactory<PolygonSymbolizer>() {
 
 						@Override
 						public Editor<PolygonSymbolizer> createEditor(
 								Composite parent, PolygonSymbolizer value) {
 							return new PolygonSymbolizerEditor(parent, value);
 						}
 					});
 		}
 		else {
 			return new RuleEditor<LineSymbolizer>(parent, ft, filter,
 					LineSymbolizer.class, (LineSymbolizer) symbolizer,
 					new EditorFactory<LineSymbolizer>() {
 
 						@Override
 						public Editor<LineSymbolizer> createEditor(
 								Composite parent, LineSymbolizer value) {
 							return new LineSymbolizerEditor(parent, value);
 						}
 					});
 		}
 	}
 
 }
