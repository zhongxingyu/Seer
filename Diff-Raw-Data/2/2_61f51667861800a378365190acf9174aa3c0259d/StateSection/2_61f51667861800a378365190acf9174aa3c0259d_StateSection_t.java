 package com.vectorsf.jvoice.ui.diagram.properties.filters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.platform.GFPropertySection;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 import com.vectorsf.jvoice.model.operations.CallFlowState;
 import com.vectorsf.jvoice.model.operations.Case;
 import com.vectorsf.jvoice.model.operations.FinalState;
 import com.vectorsf.jvoice.model.operations.InitialState;
 import com.vectorsf.jvoice.model.operations.InputState;
 import com.vectorsf.jvoice.model.operations.MenuState;
 import com.vectorsf.jvoice.model.operations.PromptState;
 import com.vectorsf.jvoice.model.operations.State;
 import com.vectorsf.jvoice.model.operations.SwitchState;
 import com.vectorsf.jvoice.model.operations.Transition;
 import com.vectorsf.jvoice.ui.diagram.properties.Activator;
 import com.vectorsf.jvoice.ui.diagram.properties.editting.ConditionEditingSupport;
 import com.vectorsf.jvoice.ui.diagram.properties.editting.EventNameEditingSupport;
 import com.vectorsf.jvoice.ui.diagram.properties.listeners.ListenerIntentionName;
 import com.vectorsf.jvoice.ui.diagram.properties.listeners.PropertiesListener;
 import com.vectorsf.jvoice.ui.diagram.properties.provider.CaseContentProvider;
 import com.vectorsf.jvoice.ui.diagram.properties.provider.CaseLabelProvider;
 
 public class StateSection  extends  GFPropertySection implements
 ITabbedPropertyConstants {
 	
 	private Text nameText;
 	private Text pathText;
 	private Text nameSubFlow;
 	private CCombo InTransitions;
 	private CCombo OutTransitions;
 	private FormData data;
 	private Table table;
 	private TableViewer tableViewer;
 	private org.eclipse.graphiti.mm.algorithms.Text nameArrow;
 	private List<Case> casos = new ArrayList<Case>();
 	private static final String CONDITION = "Condition";
 	private static final String NAME = "EventName";
 	private static final String[] PROPS = { CONDITION, NAME};
 	private static SwitchState estadoSelection;
 	private CLabel error;
 	private static IFeatureProvider fp;
 	private PictogramElement pe;
 
 	public StateSection() {}
 	
 	
 	@Override
     public void createControls(Composite parent,
         TabbedPropertySheetPage tabbedPropertySheetPage) {
         super.createControls(parent, tabbedPropertySheetPage);
  
         TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
         Composite composite = factory.createFlatFormComposite(parent);
     
         nombre_path(factory, composite);
         
         comboTransaIn(factory, composite);
 
         comboTransaOut(factory, composite); 
     }
 
 	/**
 	 * @param factory
 	 * @param composite
 	 */
 	protected void comboTransaIn(TabbedPropertySheetWidgetFactory factory,
 			Composite composite) {
 		//Transiciones de Entrada. 
 	       	InTransitions = factory.createCCombo(composite, 0);
 	       	data = new FormData();
 		    data.left = new FormAttachment(nameText, 0,SWT.LEFT);
 		    data.right = new FormAttachment(nameText,0,SWT.RIGHT);
 	       	data.top = new FormAttachment(pathText, 10);
 	       	InTransitions.setLayoutData(data);
 	       	InTransitions.setEditable(false);
 	        	
 	        CLabel LabelINTrans = factory.createCLabel(composite, "Incoming Transitions:");
 	        data = new FormData();
 	        data.left = new FormAttachment(0, 0);
 	        data.right = new FormAttachment(InTransitions,-HSPACE);
 	        data.top = new FormAttachment(InTransitions, 0, SWT.CENTER);
 	        LabelINTrans.setLayoutData(data);
 	}
 	
 	/**
 	 * @param factory
 	 * @param composite
 	 */
 	protected void comboTransaOut(TabbedPropertySheetWidgetFactory factory,
 			Composite composite) {
 		//Transiciones de salida.
         OutTransitions = factory.createCCombo(composite, 0);
         data = new FormData();
 	    data.left = new FormAttachment(nameText, 0,SWT.LEFT);
 	    data.right = new FormAttachment(nameText,0,SWT.RIGHT);
         if(InTransitions!=null)
         	data.top = new FormAttachment(InTransitions, 10);
         else
         	data.top = new FormAttachment(pathText, 10);
         OutTransitions.setLayoutData(data);
         OutTransitions.setEditable(false);
         
         CLabel LabelOutTrans = factory.createCLabel(composite, "Out Transitions:");
         data = new FormData();
         data.left = new FormAttachment(0, 0);
         data.right = new FormAttachment(OutTransitions,-HSPACE);
         data.top = new FormAttachment(OutTransitions, 0, SWT.CENTER);
         LabelOutTrans.setLayoutData(data);
 	}
 	
 	/**
 	 * @param factory
 	 * @param composite
 	 */
 	protected void tableSwitchCase(TabbedPropertySheetWidgetFactory factory,
 			Composite composite) {
 		//Tabla con los Case de un Switch
		table = factory.createTable(composite, SWT.SINGLE|SWT.FULL_SELECTION);
 		data = new FormData();
 		data.left = new FormAttachment(nameText, 0,SWT.LEFT);
 		data.right = new FormAttachment(nameText,-120,SWT.RIGHT);
         data.top = new FormAttachment(OutTransitions, 10);
         table.setLayoutData(data);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		tableViewer = new TableViewer(table);
 	    tableViewer.setUseHashlookup(true);
 	    
 	    TableViewerColumn condition = createTableViewerColumn(CONDITION, 400);
 	    condition.setEditingSupport(new ConditionEditingSupport(tableViewer));
 	    TableViewerColumn eventName = createTableViewerColumn(NAME, 120);
 	    eventName.setEditingSupport(new EventNameEditingSupport(tableViewer, error));
 	    
 	    
 	    tableViewer.setContentProvider(new CaseContentProvider());
 	    tableViewer.setLabelProvider(new CaseLabelProvider());
 	    tableViewer.setInput(casos);
 	    
 	    CellEditor[] editors = new CellEditor[2];
 	    editors[0] = new TextCellEditor(table);
 	    editors[1] = new TextCellEditor(table);
 
 	    tableViewer.setColumnProperties(PROPS);
 	    tableViewer.setCellEditors(editors);
 
 	    PropertiesListener listener = new PropertiesListener(this, tableViewer);
 
 	    Button btAdd = factory.createButton(composite, "", SWT.PUSH);
 	    btAdd.setData("add");
 	    btAdd.setImage(Activator.getDefault().getImageRegistry().get("imageAdd"));
 	    data = new FormData();
 	    data.left = new FormAttachment(table, 5);
 	    data.top =  new FormAttachment(table, 0,SWT.TOP);
 	    btAdd.setLayoutData(data);
 		btAdd.addListener(SWT.Selection, listener);
 
 	    Button btRemove = factory.createButton(composite, "", SWT.PUSH);
 	    btRemove.setData("remove");
 	    btRemove.setImage(Activator.getDefault().getImageRegistry().get("imageRemove"));
 	    data = new FormData();
 	    data.left = new FormAttachment(btAdd,0,SWT.LEFT);
 	    data.right = new FormAttachment(btAdd,0,SWT.RIGHT);
 	    data.top =  new FormAttachment(btAdd, 10);
 	    btRemove.setLayoutData(data);
 	    btRemove.addListener(SWT.Selection, listener);
 	    
 	    Button btUp = factory.createButton(composite, "",SWT.PUSH);
 	    btUp.setData("up");
 	    btUp.setImage(Activator.getDefault().getImageRegistry().get("imageUp"));
 	    data = new FormData();
 	    data.left = new FormAttachment(btAdd, 5);
 	    data.top =  new FormAttachment(table, 0,SWT.TOP);
 	    btUp.setLayoutData(data);
 	    btUp.addListener(SWT.Selection, listener);
 
 	    Button btDown = factory.createButton(composite, "", SWT.PUSH);
 	    btDown.setData("down");
 	    btDown.setImage(Activator.getDefault().getImageRegistry().get("imageDown"));
 	    data = new FormData();
 	    data.left = new FormAttachment(btUp,0,SWT.LEFT);
 	    data.right = new FormAttachment(btUp,0,SWT.RIGHT);
 	    data.top =  new FormAttachment(btUp, 10);
 	    btDown.setLayoutData(data);
 	    btDown.addListener(SWT.Selection, listener);
 	}
 
 	/**
 	 * @param factory
 	 * @param composite
 	 */
 	protected void nombre_path(TabbedPropertySheetWidgetFactory factory,
 			Composite composite) {
 			error = factory.createCLabel(composite, "", SWT.CENTER);
 			data = new FormData();
 		    data.left = new FormAttachment(0, 0);
 		    data.right = new FormAttachment(100, 0);
 		    error.setLayoutData(data);
 		
 			//Nombre del elemento
 		    nameText = factory.createText(composite, "");
 		    data = new FormData();
 		    data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH+50);
 		    data.right = new FormAttachment(error,0,SWT.RIGHT);
 		    data.top = new FormAttachment(error, 10);
 		    nameText.setLayoutData(data);
 		    nameText.addFocusListener(new ListenerIntentionName(this, nameText));
 		 
 		    CLabel LabelName = factory.createCLabel(composite, "Name:");
 		    data = new FormData();
 		    data.left = new FormAttachment(0, 0);
 		    data.right = new FormAttachment(nameText, -HSPACE);
 		    data.top = new FormAttachment(nameText, 0, SWT.CENTER);
 		    LabelName.setLayoutData(data);
 		    
 		    //Path donde se encuentra el elemento
 		    pathText = factory.createText(composite, "");
 		    data = new FormData();
 		    data.left = new FormAttachment(nameText, 0,SWT.LEFT);
 		    data.right = new FormAttachment(nameText,0,SWT.RIGHT);
 		    data.top = new FormAttachment(nameText, 10);
 		    pathText.setLayoutData(data);
 		    pathText.setEditable(false);
 		    pathText.setEnabled(false);
 		    
 	        CLabel LabelPath = factory.createCLabel(composite, "Path:");
 	        data = new FormData();
 	        data.left = new FormAttachment(0, 0);
 	        data.right = new FormAttachment(pathText, -HSPACE);
 	        data.top = new FormAttachment(pathText, 0, SWT.CENTER);
 	        LabelPath.setLayoutData(data);
 	}
 	
 	protected void subFlowPath(TabbedPropertySheetWidgetFactory factory,
 			Composite composite, String nameLabel) {
 		//Nombre del elemento
 
 		nameSubFlow = factory.createText(composite, "");
 	    data = new FormData();
 	    data.left = new FormAttachment(nameText, 0,SWT.LEFT);
 		data.right = new FormAttachment(nameText,-120,SWT.RIGHT);
         data.top = new FormAttachment(OutTransitions, 10);
 	    nameSubFlow.setLayoutData(data);
 	    nameSubFlow.setEditable(false);
 	    nameSubFlow.setEnabled(false);
 	 
 	    CLabel LabelName = factory.createCLabel(composite, nameLabel);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(nameSubFlow, -HSPACE);
 	    data.top = new FormAttachment(nameSubFlow, 0, SWT.CENTER);
 	    LabelName.setLayoutData(data);
 	    
 	    Button btEditFlow = factory.createButton(composite, "",SWT.PUSH);
 	    btEditFlow.setImage(Activator.getDefault().getImageRegistry().get("imageModify"));
 	    data = new FormData();
 	    data.left = new FormAttachment(nameSubFlow, 5);
 	    data.top =  new FormAttachment(nameSubFlow, 0,SWT.CENTER);
 	    btEditFlow.setLayoutData(data);
 	    btEditFlow.addListener(SWT.Selection, new PropertiesListener(nameSubFlow));
 	}
 	
     @Override
     public void refresh() {
     	removelistener();
     	pe = getSelectedPictogramElement();
         if (pe != null) {
         	fp =getDiagramTypeProvider().getFeatureProvider();
         	
             Object bo = Graphiti.getLinkService()
                  .getBusinessObjectForLinkedPictogramElement(pe);
             // the filter assured, that it is a EClass
             if (bo == null)
                 return;
             textnompath(bo);
 
             if(!(bo instanceof InitialState))
             	transactionIncoming(bo);
             
             if(!(bo instanceof FinalState))
             	transactionOut(bo);
             
             if(bo instanceof SwitchState){
             	estadoSelection = (SwitchState)bo;
             	casos = estadoSelection.getCase();
             	tableViewer.setInput(casos);
             	
             }else if(bo instanceof CallFlowState){
             	CallFlowState subFlow = (CallFlowState)bo;
             	if (subFlow.getSubflow().getName()!=null){
             		nameSubFlow.setText(subFlow.getSubflow().getName());
             	}
             }else if(bo instanceof MenuState){
             	MenuState menuLocution = (MenuState)bo;
             	if (menuLocution.getLocution().getName()!=null){
             		nameSubFlow.setText(menuLocution.getLocution().getName());
             	}
             }else if(bo instanceof InputState){
             	InputState inputLocution = (InputState)bo;
             	if (inputLocution.getLocution().getName()!=null){
             		nameSubFlow.setText(inputLocution.getLocution().getName());
             	}
             }else if(bo instanceof PromptState){
             	PromptState outputLocution = (PromptState)bo;
             	if (outputLocution.getLocution().getName()!=null){
             		nameSubFlow.setText(outputLocution.getLocution().getName());
             	}
             }
         }
     }
     
     public PictogramElement obtenerPe(){
     	return pe;
     }
 
 	/**
 	 * Eliminamos el listener
 	 */
 	protected void removelistener() {
 		nameText.removeFocusListener(new ListenerIntentionName(this, nameText));
 	}
 
 	/**
 	 * Obtenemos el nombre de las transiciones de salida.
 	 * @param bo
 	 */
 	protected void transactionOut(Object bo) {
 		EList<Transition> OUTtransaction = ((State) bo).getOutgoingTransitions();
 		OutTransitions.removeAll();
 
 		if(OUTtransaction.size()>0){             	
 			for (Object trans : OUTtransaction){            		
 				Transition tr = (Transition)trans;
 		    	//Cogemos el Pictogram Elements a la transacion.
 				PictogramElement obj =  obtenerFeatureProvider().getPictogramElementForBusinessObject(tr);
 	    		if(obj instanceof Connection){
 		    		Connection connection = (Connection) obj;
     				//Obtenemos la conexion de nuestra transacion y la recorremos.
                 	EList<ConnectionDecorator> liCD = connection.getConnectionDecorators();
                 	for (ConnectionDecorator connectionDecorator : liCD) 
                 	{
                 		/**Obtenemos los Graphics Algorithms, que en este caso son dos, 
                 		 * el texto y el arrow.
                 		 * Para obtener el nombre, comprobamos que el Graphics Algorithm sea Text,
                 		 * de ser asi obtenemos el nombre.
                 		**/
                 		GraphicsAlgorithm ga= connectionDecorator.getGraphicsAlgorithm();
                 		if (ga instanceof org.eclipse.graphiti.mm.algorithms.Text)
                 		{
                 			nameArrow = (org.eclipse.graphiti.mm.algorithms.Text)ga;
                 			OutTransitions.add(nameArrow.getValue().toString());
                 		}
                 		
     				}
 				}//Fin del for.   
 			} 
 			
 		}else{
 			OutTransitions.add("none");      
 		}
 		OutTransitions.select(0);
 	}
 
 	/**
 	 * Obtenemos el nombre de las transiciones de entrada.
 	 * @param bo
 	 */
 	protected void transactionIncoming(Object bo) {
 		EList<Transition>INCOMtransaction = ((State) bo).getIncomingTransitions();
 		InTransitions.removeAll();
            
 		if(INCOMtransaction.size()>0){ 
 			
 				for (Object transIN : INCOMtransaction){            		
 		    		Transition tr = (Transition)transIN;
 		        	//Cogemos el Pictogram Elements a la transacion.
 		        	List<?> li = Graphiti.getLinkService().getPictogramElements(getDiagramTypeProvider().getDiagram(), tr);
 		        	for (Object object : li) {            		
 		        		if(object instanceof Connection){
 		        		Connection connection = (Connection) object;
 		        				//Obtenemos la conexion de nuestra transacion y la recorremos.
 		                    	EList<ConnectionDecorator> liCD = connection.getConnectionDecorators();
 		                    	for (ConnectionDecorator connectionDecorator : liCD) 
 		                    	{
 		                    		/**Obtenemos los Graphics Algorithms, que en este caso son dos, 
 		                    		 * el texto y el arrow.
 		                    		 * Para obtener el nombre, comprobamos que el Graphics Algorithm sea Text,
 		                    		 * de ser asi obtenemos el nombre.
 		                    		**/
 		                    		GraphicsAlgorithm ga= connectionDecorator.getGraphicsAlgorithm();
 		                    		if (ga instanceof org.eclipse.graphiti.mm.algorithms.Text)
 		                    		{
 		                    			nameArrow = (org.eclipse.graphiti.mm.algorithms.Text)ga;
 		                    			InTransitions.add(nameArrow.getValue().toString());
 		                    		}
 		                    		
 		        				}
 		        		}
 					}//Fin del for.           		
 			}
 				
 		}else{
 			InTransitions.add("none");      
 		}
 		
 		InTransitions.select(0);
 	}
 
 	/**
 	 * @param bo
 	 */
 	protected void textnompath(Object bo) {
 		String name = null;
 		name = ((State) bo).getName();            
 		nameText.setText(name == null ? "" : name);
 		nameText.addFocusListener(new ListenerIntentionName(this, nameText));
 		String path = (((State) bo).eResource()).getURI().path().substring(9).toString();
 		pathText.setText(path == null ? "" : path);
 	}
 	
 	private TableViewerColumn createTableViewerColumn(String title, int bound) {
 		    final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer,
 		        SWT.NONE);
 		    final TableColumn column = viewerColumn.getColumn();
 		    column.setText(title);
 		    column.setWidth(bound);
 		    column.setResizable(true);
 		    column.setMoveable(true);
 		    return viewerColumn;
 	}
 	
 	public IFeatureProvider obtenerFeatureProvider (){
 		return fp;
 	}
 
 	public SwitchState obtenerSwitch(){
 		return estadoSelection;
 	}
 	
 	public List<Case> obtenerCases(){
 		return casos;
 	}
 }
