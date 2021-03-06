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
 
 package eu.esdihumboldt.hale.rcp.utils.definition.internal.editors.codelist;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 import eu.esdihumboldt.hale.rcp.utils.codelist.CodeList;
 import eu.esdihumboldt.hale.rcp.utils.codelist.CodeListService;
 
 /**
  * 
  *
  * @author Simon Templer
  * @partner 01 / Fraunhofer Institute for Computer Graphics Research
  * @version $Id$ 
  */
 public class ListSelector implements CodeListSelector {
 	
 	private final Composite page;
 	
 	private final ListViewer listViewer;
 	
 	private final List<CodeList> codeLists;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param parent the parent composite
 	 */
 	public ListSelector(Composite parent) {
 		page = new Composite(parent, SWT.NONE);
 		GridLayout gridLayout = new GridLayout(2, false);
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		gridLayout.verticalSpacing = 0;
 		gridLayout.horizontalSpacing = 0;
 		page.setLayout(gridLayout);
 		
 		CodeListService codeListService = (CodeListService) PlatformUI.getWorkbench().getService(CodeListService.class);
 		
 		codeLists = codeListService.getSearchPathCodeLists();
 		Collections.sort(codeLists, new Comparator<CodeList>() {
 
 			@Override
 			public int compare(CodeList o1, CodeList o2) {
 				int result = o1.getIdentifier().compareToIgnoreCase(o2.getIdentifier());
 				
 				if (result == 0) {
 					result = o1.getNamespace().compareToIgnoreCase(o2.getNamespace());
 				}
 				
 				return result;
 			}
 			
 		});
 		
 		// search field
 		String tip = "Filter the code lists by a string contained in their identifiers, namespaces or descriptions";
 		
 		Label searchLabel = new Label(page, SWT.NONE);
 		searchLabel.setText("Filter: ");
 		searchLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
 		searchLabel.setToolTipText(tip);
 		
 		final Text searchText = new Text(page, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
 		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		searchText.setToolTipText(tip);
 		
 		// list viewer
 		listViewer = new ListViewer(page, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL | SWT.SINGLE);
 		listViewer.setContentProvider(ArrayContentProvider.getInstance());
 		listViewer.setLabelProvider(new LabelProvider() {
 
 			@Override
 			public String getText(Object element) {
 				if (element instanceof CodeList) {
 					CodeList codeList = (CodeList) element;
 					return codeList.getIdentifier() + " (" + codeList.getNamespace() + ")";
 				}
 				else {
 					return super.getText(element);
 				}
 			}
 			
 		});
 		listViewer.setInput(codeLists);
 		
 		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
 		layoutData.widthHint = SWT.DEFAULT;
 		layoutData.heightHint = 10 * listViewer.getList().getItemHeight();
 		listViewer.getControl().setLayoutData(layoutData);
 		
 		// info
 		final Text info = new Text(page, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
 		
 		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
 		layoutData.widthHint = SWT.DEFAULT;
 		layoutData.heightHint = 6 * listViewer.getList().getItemHeight();
 		info.setLayoutData(layoutData);
 		
 		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection selection = event.getSelection();
 				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
 					CodeList codeList = (CodeList) ((IStructuredSelection) selection).getFirstElement();
 					String desc = codeList.getDescritpion();
 					if (desc != null) {
 						info.setText(desc);
 					}
 					else {
 						info.setText("No description");
 					}
 				}
 				else {
 					info.setText("No description");
 				}
 			}
 		});
 		
 		// search filter & update
 		listViewer.addFilter(new ViewerFilter() {
 			
 			@Override
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				String filterText = searchText.getText();
 				// handle empty filter
 				if (filterText == null || filterText.isEmpty()) {
 					return true;
 				}
 				
 				if (element instanceof CodeList) {
 					CodeList codeList = (CodeList) element;
 					filterText = filterText.toLowerCase();
 					
 					if (codeList.getIdentifier().toLowerCase().contains(filterText)) {
 						return true;
 					}
 					if (codeList.getNamespace().toLowerCase().contains(filterText)) {
 						return true;
 					}
					if (codeList.getDescritpion() != null && codeList.getDescritpion().toLowerCase().contains(filterText)) {
 						return true;
 					}
 				}
 				
 				return false;
 			}
 		});
 		searchText.addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				// refilter
 				listViewer.refresh();
 			}
 		});
 	}
 
 	/**
 	 * @see CodeListSelector#getCodeList()
 	 */
 	@Override
 	public CodeList getCodeList() {
 		ISelection selection = listViewer.getSelection();
 		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
 			return (CodeList) ((IStructuredSelection) selection).getFirstElement();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * @see CodeListSelector#getControl()
 	 */
 	@Override
 	public Control getControl() {
 		return page;
 	}
 
 	/**
 	 * Select the given code list
 	 * 
 	 * @param codeList the code list
 	 * 
 	 * @return true if the code list was selected
 	 */
 	public boolean selectCodeList(CodeList codeList) {
 		if (codeList != null && codeLists.contains(codeList)) {
 			listViewer.setSelection(new StructuredSelection(codeList), true);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 }
