 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.studio.data.provider;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bonitasoft.studio.common.ExpressionConstants;
 import org.bonitasoft.studio.common.jface.DataStyledTreeLabelProvider;
 import org.bonitasoft.studio.common.jface.TableColumnSorter;
 import org.bonitasoft.studio.data.i18n.Messages;
 import org.bonitasoft.studio.data.ui.wizard.DataWizard;
 import org.bonitasoft.studio.data.ui.wizard.DataWizardDialog;
 import org.bonitasoft.studio.expression.editor.ExpressionEditorService;
 import org.bonitasoft.studio.expression.editor.provider.IExpressionEditor;
 import org.bonitasoft.studio.expression.editor.provider.IExpressionProvider;
 import org.bonitasoft.studio.expression.editor.provider.SelectionAwareExpressionEditor;
 import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
 import org.bonitasoft.studio.model.expression.Expression;
 import org.bonitasoft.studio.model.expression.ExpressionPackage;
 import org.bonitasoft.studio.model.form.DateFormField;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.impl.FormImpl;
 import org.bonitasoft.studio.model.process.Data;
 import org.bonitasoft.studio.model.process.DataAware;
 import org.bonitasoft.studio.model.process.ProcessPackage;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.EMFObservables;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author Romain Bioteau
  * 
  */
 public class DataExpressionEditor extends SelectionAwareExpressionEditor
 		implements IExpressionEditor {
 
 	private TableViewer viewer;
 	private GridLayout gridLayout;
 	private Expression editorInputExpression;
 	private Composite mainComposite;
 	private Text typeText;
 	private IObservableValue returnTypeObservable = null;
 	private Button addExpressionButton;
 
 	@Override
 	public Control createExpressionEditor(Composite parent) {
 		mainComposite = new Composite(parent, SWT.NONE);
 		mainComposite.setLayoutData(GridDataFactory.fillDefaults()
 				.grab(true, true).create());
 		gridLayout = new GridLayout(1, false);
 		mainComposite.setLayout(gridLayout);
 
 		viewer = new TableViewer(mainComposite, SWT.FULL_SELECTION | SWT.BORDER
 				| SWT.SINGLE | SWT.V_SCROLL);
 
 		TableLayout layout = new TableLayout();
 		layout.addColumnData(new ColumnWeightData(100, false));
 		viewer.getTable().setLayout(layout);
 		viewer.getTable().setLayoutData(
 				GridDataFactory.fillDefaults().grab(true, true).create());
 
 		TableViewerColumn columnViewer = new TableViewerColumn(viewer, SWT.NONE);
 		TableColumn column = columnViewer.getColumn();
 		column.setText(Messages.name);
 
 		TableColumnSorter sorter = new TableColumnSorter(viewer);
 		sorter.setColumn(column);
 
 		viewer.getTable().setHeaderVisible(true);
 		viewer.setContentProvider(new ArrayContentProvider());
 		viewer.setLabelProvider(new DataStyledTreeLabelProvider());
 
 		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (!event.getSelection().isEmpty()) {
 					DataExpressionEditor.this.fireSelectionChanged();
 				}
 			}
 		});
 
 		createReturnTypeComposite(parent);
 
 		addExpressionButton = new Button(parent, SWT.FLAT);
 		addExpressionButton.setLayoutData(GridDataFactory.fillDefaults()
 				.align(SWT.LEFT, SWT.CENTER).hint(85, SWT.DEFAULT).create());
 		addExpressionButton.setText(Messages.addData);
 
 		return mainComposite;
 	}
 
 	protected void createReturnTypeComposite(Composite parent) {
 		Composite typeComposite = new Composite(parent, SWT.NONE);
 		typeComposite.setLayoutData(GridDataFactory.fillDefaults()
 				.grab(true, false).create());
 		GridLayout gl = new GridLayout(2, false);
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		typeComposite.setLayout(gl);
 
 		Label typeLabel = new Label(typeComposite, SWT.NONE);
 		typeLabel.setText(Messages.returnType);
 		typeLabel.setLayoutData(GridDataFactory.fillDefaults()
 				.align(SWT.FILL, SWT.CENTER).create());
 
 		typeText = new Text(typeComposite, SWT.BORDER | SWT.READ_ONLY);
 		typeText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
 				.align(SWT.FILL, SWT.CENTER).indent(10, 0).create());
 	}
 
 	protected void handleSpecificDatatypeEdition(Data data) {
 		if (gridLayout.numColumns > 1) {
 			mainComposite.getChildren()[1].dispose();
 			gridLayout.numColumns--;
 			viewer.getTable().setLayoutData(
 					GridDataFactory.fillDefaults().grab(true, true).create());
 			mainComposite.layout();
 		}
 
 	}
 
 	private void expressionButtonListener(EObject context,
 			ViewerFilter[] filters) {
 		EObject container = context;
 
		while (!(container instanceof DataAware) || container instanceof Form) {
 			container = container.eContainer();
 		}
 		EStructuralFeature feat = ProcessPackage.Literals.DATA_AWARE__DATA;
 
 		Set<EStructuralFeature> res = new HashSet<EStructuralFeature>();
 		res.add(ProcessPackage.Literals.DATA_AWARE__DATA);
 
 		DataWizardDialog wizardDialog = new DataWizardDialog(Display
 				.getCurrent().getActiveShell(), new DataWizard(container, feat,
 				res, true), null);
 		if (wizardDialog.open() == Dialog.OK) {
 			fillViewerData(context, filters);
 		}
 	}
 
 	private void fillViewerData(EObject context, ViewerFilter[] filters) {
 		Set<Data> input = new HashSet<Data>();
 		IExpressionProvider provider = ExpressionEditorService.getInstance()
 				.getExpressionProvider(ExpressionConstants.VARIABLE_TYPE);
 		final Set<Expression> expressions = provider.getExpressions(context);
 		final Set<Expression> filteredExpressions = new HashSet<Expression>();
 		if (expressions != null) {
 			filteredExpressions.addAll(expressions);
 			if (input != null && filters != null) {
 				for (final Expression exp : expressions) {
 					for (final ViewerFilter filter : filters) {
 						if (filter != null
 								&& !filter.select(viewer, context, exp)) {
 							filteredExpressions.remove(exp);
 						}
 					}
 				}
 			}
 		}
 		for (Expression e1 : filteredExpressions) {
 			if (editorInputExpression.isReturnTypeFixed()) {
 				if (compatibleReturnType(editorInputExpression, e1)) {
 					input.add((Data) e1.getReferencedElements().get(0));
 				}
 			} else {
 				input.add((Data) e1.getReferencedElements().get(0));
 			}
 		}
 		viewer.setInput(input);
 	}
 
 	@Override
 	public void bindExpression(EMFDataBindingContext dataBindingContext,
 			EObject context, Expression inputExpression, ViewerFilter[] filters,ExpressionViewer expressionViewer) {
 
 		final EObject finalContext = context;
 		final ViewerFilter[] finalFilters = filters;
 		addExpressionButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				super.widgetSelected(e);
 				expressionButtonListener(finalContext, finalFilters);
 			}
 		});
 
 		editorInputExpression = inputExpression;
 		fillViewerData(context, filters);
 
 		IObservableValue contentObservable = EMFObservables
 				.observeValue(inputExpression,
 						ExpressionPackage.Literals.EXPRESSION__CONTENT);
 		IObservableValue nameObservable = EMFObservables.observeValue(
 				inputExpression, ExpressionPackage.Literals.EXPRESSION__NAME);
 		returnTypeObservable = EMFObservables.observeValue(inputExpression,
 				ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE);
 		IObservableValue referenceObservable = EMFObservables.observeValue(
 				inputExpression,
 				ExpressionPackage.Literals.EXPRESSION__REFERENCED_ELEMENTS);
 
 		UpdateValueStrategy selectionToName = new UpdateValueStrategy();
 		IConverter nameConverter = new Converter(Data.class, String.class) {
 
 			@Override
 			public Object convert(Object data) {
 				return ((Data) data).getName();
 			}
 
 		};
 		selectionToName.setConverter(nameConverter);
 
 		UpdateValueStrategy selectionToContent = new UpdateValueStrategy();
 		IConverter contentConverter = new Converter(Data.class, String.class) {
 
 			@Override
 			public Object convert(Object data) {
 				// if(((Data)data).isMultiple()){
 				// if(editorInputExpression.getContent() != null){
 				// return editorInputExpression.getContent() ;
 				// }
 				// }else if(data instanceof XMLData){
 				// if(editorInputExpression.getContent() != null){
 				// return editorInputExpression.getContent() ;
 				// }
 				// }else if(data instanceof JavaObjectData){
 				// if(editorInputExpression.getContent() != null){
 				// return editorInputExpression.getContent() ;
 				// }
 				// }
 				return ((Data) data).getName();
 			}
 
 		};
 		selectionToContent.setConverter(contentConverter);
 
 		UpdateValueStrategy selectionToReturnType = new UpdateValueStrategy();
 		IConverter returnTypeConverter = new Converter(Data.class, String.class) {
 
 			@Override
 			public Object convert(Object data) {
 				return org.bonitasoft.studio.common.DataUtil
 						.getTechnicalTypeFor((Data) data);
 			}
 
 		};
 		selectionToReturnType.setConverter(returnTypeConverter);
 
 		UpdateValueStrategy selectionToReferencedData = new UpdateValueStrategy();
 		IConverter referenceConverter = new Converter(Data.class, List.class) {
 
 			@Override
 			public Object convert(Object data) {
 				if (data != null) {
 					return Collections.singletonList(data);
 				} else {
 					return Collections.emptyList();
 				}
 			}
 
 		};
 		selectionToReferencedData.setConverter(referenceConverter);
 
 		UpdateValueStrategy referencedDataToSelection = new UpdateValueStrategy();
 		IConverter referencetoDataConverter = new Converter(List.class,
 				Data.class) {
 
 			@Override
 			public Object convert(Object dataList) {
 				Data d = ((List<Data>) dataList).get(0);
 				Collection<Data> inputData = (Collection<Data>) viewer
 						.getInput();
 				for (Data data : inputData) {
 					if (data.getName().equals(d.getName())
 							&& data.getDataType().getName()
 									.equals(d.getDataType().getName())) {
 						return data;
 					}
 				}
 				return null;
 			}
 
 		};
 		referencedDataToSelection.setConverter(referencetoDataConverter);
 
 		dataBindingContext.bindValue(ViewersObservables
 				.observeSingleSelection(viewer), nameObservable,
 				selectionToName, new UpdateValueStrategy(
 						UpdateValueStrategy.POLICY_NEVER));
 		dataBindingContext.bindValue(ViewersObservables
 				.observeSingleSelection(viewer), contentObservable,
 				selectionToContent, new UpdateValueStrategy(
 						UpdateValueStrategy.POLICY_NEVER));
 		dataBindingContext.bindValue(
 				ViewersObservables.observeSingleSelection(viewer),
 				returnTypeObservable, selectionToReturnType,
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER));
 		dataBindingContext.bindValue(
 				ViewersObservables.observeSingleSelection(viewer),
 				referenceObservable, selectionToReferencedData,
 				referencedDataToSelection);
 		dataBindingContext.bindValue(
 				SWTObservables.observeText(typeText, SWT.Modify),
 				returnTypeObservable);
 
 		if (context instanceof DateFormField) {
 
 			final ControlDecoration cd = new ControlDecoration(typeText,
 					SWT.TOP | SWT.LEFT);
 			// cd.setImage(Pics.getImage(PicsConstants.error)) ;
 			cd.setImage(PlatformUI.getWorkbench().getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_WARN_TSK));
 			cd.setDescriptionText("It is recommanded to use Return type ad String or Date for Date Widget");
 			cd.setShowOnlyOnFocus(false);
 			if (typeText.getText().equals(Date.class.getName())
 					|| typeText.getText().equals(String.class.getName())) {
 				cd.hide();
 			} else {
 				cd.show();
 			}
 
 			returnTypeObservable
 					.addValueChangeListener(new IValueChangeListener() {
 
 						@Override
 						public void handleValueChange(ValueChangeEvent event) {
 							if (typeText.getText().equals(Date.class.getName())
 									|| typeText.getText().equals(
 											String.class.getName())) {
 								cd.hide();
 							} else {
 								cd.show();
 							}
 
 						}
 					});
 
 		}
 
 	}
 
 	@Override
 	public boolean canFinish() {
 		return !viewer.getSelection().isEmpty();
 	}
 
 	@Override
 	public void okPressed() {
 		if (!editorInputExpression.getContent().equals(
 				editorInputExpression.getName())) {
 			editorInputExpression.setName(editorInputExpression.getContent());
 		}
 	}
 
 	@Override
 	public Control getTextControl() {
 		return null;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (returnTypeObservable != null) {
 			returnTypeObservable.dispose();
 		}
 	}
 }
