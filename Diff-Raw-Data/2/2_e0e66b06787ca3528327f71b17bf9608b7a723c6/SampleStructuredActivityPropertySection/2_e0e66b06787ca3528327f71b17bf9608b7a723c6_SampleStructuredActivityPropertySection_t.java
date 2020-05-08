 package org.eclipse.bpel.extensionssample.ui.properties;
 
 import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
 import org.eclipse.bpel.common.ui.details.IValue;
 import org.eclipse.bpel.common.ui.details.TextIValue;
 import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
 import org.eclipse.bpel.common.ui.flatui.FlatFormData;
 import org.eclipse.bpel.extensionsample.model.SampleStructuredActivity;
 import org.eclipse.bpel.model.BPELFactory;
 import org.eclipse.bpel.model.BPELPackage;
 import org.eclipse.bpel.model.Variable;
 import org.eclipse.bpel.ui.adapters.INamedElement;
 import org.eclipse.bpel.ui.commands.CompoundCommand;
 import org.eclipse.bpel.ui.commands.util.AutoUndoCommand;
 import org.eclipse.bpel.ui.properties.BPELPropertySection;
 import org.eclipse.bpel.ui.properties.EditController;
 import org.eclipse.bpel.ui.properties.VariableTypeSelector;
 import org.eclipse.bpel.ui.util.BPELUtil;
 import org.eclipse.bpel.ui.util.BatchedMultiObjectAdapter;
 import org.eclipse.bpel.ui.util.MultiObjectAdapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.wsdl.Message;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDTypeDefinition;
 
 /*
  * Bug 120110
  * This class implements the detail property tab for the "structured" extension activity
  * This property detail tab allows definition of a variable name and type, which (presumably)
  * will be in-scope for the children of this container activity.
  * 
  * This class demonstrates the use of a change tracker to update the model and keep the XML
  * source in sync with the property page widgets.
  * 
  * Note that validation of this activity is not yet implemented.
  */
 public class SampleStructuredActivityPropertySection extends BPELPropertySection {
 	
 	protected VariableTypeSelector variableTypeSelector;
 	protected Composite parentComposite;
 	protected Text variableName;
 	protected EditController variableNameEditController;
 	INamedElement variableNamedElement;
 	
 	public class VariableTypeCallback implements VariableTypeSelector.Callback {
 
 		/**
 		 * @see org.eclipse.bpel.ui.properties.VariableTypeSelector.Callback#selectRadioButton(int)
 		 */
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.bpel.ui.properties.VariableTypeSelector.Callback#selectRadioButton(int)
 		 */
 		public void selectRadioButton(final int index) {
 			throw new IllegalArgumentException("oops!");
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.bpel.ui.properties.VariableTypeSelector.Callback#selectXSDType(org.eclipse.xsd.XSDTypeDefinition)
 		 */
 		public void selectXSDType(XSDTypeDefinition xsdType) {
 			applyChanges(xsdType);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.bpel.ui.properties.VariableTypeSelector.Callback#selectXSDElement(org.eclipse.xsd.XSDElementDeclaration)
 		 */
 		public void selectXSDElement(XSDElementDeclaration xsdElement) {
 			applyChanges(xsdElement);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.bpel.ui.properties.VariableTypeSelector.Callback#selectMessageType(org.eclipse.wst.wsdl.Message)
 		 */
 		public void selectMessageType(Message message) {
 			applyChanges(message); 
 		}
 	}
 
 	private SampleStructuredActivity getActivity() {
 		SampleStructuredActivity activity = (SampleStructuredActivity)getInput();
 		// make sure this has a Variable
 		if (activity.getVariable() == null)
 			activity.setVariable(BPELFactory.eINSTANCE.createVariable());
 		return activity;
 	}
 
 	@Override
 	protected MultiObjectAdapter[] createAdapters() {
 		return new MultiObjectAdapter[] {
 			/* model object */
 			new BatchedMultiObjectAdapter() {
 				
 				@Override
 				public void notify (Notification n) {
 				}
 				
 				@Override
 				public void finish() {
 					updateVariableTypeSelector();
 				}
 			}
 		};
 	}
 
 	@Override
 	protected void createClient(Composite parent) {
 		FlatFormData data;
 
 		Composite composite = parentComposite = createFlatFormComposite(parent);
 		Label description = getWidgetFactory().createLabel(composite, "Declare a variable that is in scope only for this structured activity");
 		data = new FlatFormData();
 		data.left = new FlatFormAttachment(0, 0);
 		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE );
 		description.setLayoutData(data);
 		
 		Label nameLabel = getWidgetFactory().createLabel(composite, "Variable Name:");
 		
 		variableName = fWidgetFactory.createText(composite, EMPTY_STRING);
 		data = new FlatFormData();
 		data.left = new FlatFormAttachment(0, BPELUtil.calculateLabelWidth(nameLabel, STANDARD_LABEL_WIDTH_AVG));
 		data.right = new FlatFormAttachment(100, (-2) * IDetailsAreaConstants.HSPACE );
 		data.top = new FlatFormAttachment(description, 10, SWT.LEFT);
 		variableName.setLayoutData(data);
 		
 		data = new FlatFormData();
 		data.left = new FlatFormAttachment(0, 0);
 		data.right = new FlatFormAttachment(variableName, -IDetailsAreaConstants.HSPACE);
 		data.top = new FlatFormAttachment(variableName, 0, SWT.CENTER);
 		nameLabel.setLayoutData(data);
 		
 		variableTypeSelector = new VariableTypeSelector(composite, SWT.NONE, getBPELEditor(),
 			fWidgetFactory, new VariableTypeCallback());
 		data = new FlatFormData();
 		data.top = new FlatFormAttachment(nameLabel,0, SWT.LEFT);
 		data.left = new FlatFormAttachment(0,0);
 		data.right = new FlatFormAttachment(100,0);
 		data.bottom = new FlatFormAttachment(100,0);
 		variableTypeSelector.setLayoutData(data);
 
 		createChangeTrackers();
 	}
 	
 	protected void createChangeTrackers() {
 		variableNameEditController = new EditController(getCommandFramework()) {
 			@Override
 			public boolean checkNotification (Notification notification) {
 				return variableNamedElement != null && variableNamedElement.isNameAffected(variableNameEditController.getInput(), notification);				
 			}
 			@Override
 			public Command createApplyCommand() {
 				return wrapInShowContextCommand( createCommand(null) );
 			}
 			
 		};		
 		variableNameEditController.setLabel( BPELPackage.eINSTANCE.getActivity_Name().getName() );
 		
 		variableNameEditController.setViewIValue(new TextIValue ( variableName )) ;
 		variableNameEditController.setModeIValue(new IValue () {
 			public Object get() {			
 				return variableNamedElement != null ? variableNamedElement.getName( variableNameEditController.getInput() ) : null;
 			}
 			public void set (Object object) {				
 				if (variableNamedElement != null) {
 					variableNamedElement.setName(variableNameEditController.getInput(),object.toString() );								
 				}
 			}			
 		});
 		
 		variableNameEditController.startListeningTo(variableName);
 	}
 	
 	protected Command createCommand(final EObject type) {
 		CompoundCommand command = new CompoundCommand();
 		command.add(new AutoUndoCommand(getProcess()) {
 			@Override
 			public void doExecute() {
 				SampleStructuredActivity activity = getActivity();
 				Variable variable = activity.getVariable();
 				variable.setName(variableName.getText());
 				// https://issues.jboss.org/browse/JBIDE-8045
 				// Data caught by fault handler can be either a Message Type
 				// or an XSD Element.
 				if (type instanceof Message) {
 				    variable.setMessageType((Message)type);
 					variable.setXSDElement(null);
 					variable.setType(null);
 				}
 				else if (type instanceof XSDElementDeclaration) {
 				    variable.setMessageType(null);
 					variable.setXSDElement((XSDElementDeclaration)type);
 					variable.setType(null);
 				}
 				else if (type instanceof XSDTypeDefinition) {
 				    variable.setMessageType(null);
 					variable.setXSDElement(null);
 					variable.setType((XSDTypeDefinition)type);
 				}
 				
 				if (variable==activity.getVariable())
 					// force update of variable
 					activity.setVariable(null);
 				activity.setVariable(variable);
 			}
 		});
 		return command;
 	}
 	
 	protected void applyChanges(final EObject type) {
 		Command command = createCommand(type);
 		getCommandFramework().execute(wrapInShowContextCommand(command));
 	}
 
 	public void updateVariableName() {
 		if (getActivity() != null ) {
 			Variable variable = getActivity().getVariable();
			variableName.setText(variable==null || variable.getName()==null ? "" : variable.getName());
 		}
 	}
 	
 	public void updateVariableTypeSelector() {
 		if (getActivity() != null ) {
 			Variable variable = getActivity().getVariable();
 			variableTypeSelector.setVariable(variable);
 		}
 	}
 	
 	@Override
 	protected void basicSetInput(EObject newInput) {
 		if ( newInput instanceof SampleStructuredActivity) {
 			super.basicSetInput(newInput);
 			updateVariableTypeSelector();
 			updateVariableName();
 			variableNamedElement = BPELUtil.adapt(getActivity().getVariable(),INamedElement.class);
 			variableNameEditController.setInput(getActivity().getVariable());
 		}
 	}
 }
