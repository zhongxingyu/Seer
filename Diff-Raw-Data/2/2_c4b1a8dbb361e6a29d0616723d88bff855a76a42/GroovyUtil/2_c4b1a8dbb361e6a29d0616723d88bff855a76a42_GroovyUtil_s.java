 /**
  * Copyright (C) 2009 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.bonitasoft.studio.groovy;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bonitasoft.engine.expression.ExpressionConstants;
 import org.bonitasoft.forms.server.validator.AbstractFormValidator;
 import org.bonitasoft.studio.common.ProjectUtil;
 import org.bonitasoft.studio.common.emf.tools.ModelHelper;
 import org.bonitasoft.studio.common.exporter.ExporterTools;
 import org.bonitasoft.studio.common.log.BonitaStudioLog;
 import org.bonitasoft.studio.common.repository.RepositoryManager;
 import org.bonitasoft.studio.connector.model.definition.Output;
 import org.bonitasoft.studio.data.util.DataUtil;
 import org.bonitasoft.studio.expression.editor.filter.DisplayEngineExpressionWithName;
 import org.bonitasoft.studio.model.expression.Expression;
 import org.bonitasoft.studio.model.expression.ExpressionFactory;
 import org.bonitasoft.studio.model.form.Duplicable;
 import org.bonitasoft.studio.model.form.DynamicTable;
 import org.bonitasoft.studio.model.form.Form;
 import org.bonitasoft.studio.model.form.FormField;
 import org.bonitasoft.studio.model.form.Group;
 import org.bonitasoft.studio.model.form.NextFormButton;
 import org.bonitasoft.studio.model.form.SubmitFormButton;
 import org.bonitasoft.studio.model.form.TextFormField;
 import org.bonitasoft.studio.model.form.Validator;
 import org.bonitasoft.studio.model.form.Widget;
 import org.bonitasoft.studio.model.parameter.Parameter;
 import org.bonitasoft.studio.model.process.AbstractProcess;
 import org.bonitasoft.studio.model.process.Activity;
 import org.bonitasoft.studio.model.process.Connection;
 import org.bonitasoft.studio.model.process.Data;
 import org.bonitasoft.studio.model.process.DataAware;
 import org.bonitasoft.studio.model.process.Element;
 import org.bonitasoft.studio.model.process.JavaObjectData;
 import org.bonitasoft.studio.model.process.PageFlow;
 import org.bonitasoft.studio.model.process.ProcessPackage;
 import org.bonitasoft.studio.model.process.RecapFlow;
 import org.bonitasoft.studio.model.process.SequenceFlow;
 import org.bonitasoft.studio.model.process.SourceElement;
 import org.bonitasoft.studio.model.process.StartTimerEvent;
 import org.bonitasoft.studio.model.process.Task;
 import org.bonitasoft.studio.model.process.ViewPageFlow;
 import org.bonitasoft.studio.model.simulation.SimulationBoolean;
 import org.bonitasoft.studio.model.simulation.SimulationData;
 import org.bonitasoft.studio.model.simulation.SimulationLiteralData;
 import org.bonitasoft.studio.model.simulation.SimulationNumberData;
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.ast.FieldNode;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 /**
  * @author Romain Bioteau
  */
 public class GroovyUtil {
 
 	public static final String GROOVY_PREFIX = "${"; //$NON-NLS-1$
 	public static final String GROOVY_SUFFIX = "}"; //$NON-NLS-1$
 	public static final String GROOVY_CONSTANT_SEPARATOR = "\'";
 
 	private static Map<String, Expression> expressions;
 
 	public static ScriptVariable createScriptVariable(final SimulationData d) {
 		String type = Object.class.getName();
 		if (d instanceof SimulationBoolean) {
 			type = Boolean.class.getName();
 		} else if (d instanceof SimulationLiteralData) {
 			type = String.class.getName();
 		} else if (d instanceof SimulationNumberData) {
 			type = Integer.class.getName();
 		}
 		return new ScriptVariable(d.getName(), type);
 	}
 
 	public static Class<?> getClassForData(final Element container,
 			String inputScript) {
 		if (inputScript != null && inputScript.trim().length() > 0) {
 			if (inputScript.startsWith(GROOVY_PREFIX)) {
 				inputScript = inputScript
 						.substring(2, inputScript.length() - 1);
 			}
 
 			for (final Data d : ModelHelper.getAccessibleData(container)) {
 				if (d.getName().equals(inputScript)) {
 					return DataUtil.getClassForData(d);
 				}
 			}
 		}
 		return Object.class;
 	}
 
 
 	public static List<ScriptVariable> createScriptVariablesFromSimulationElement(
 			final Element elem) {
 		if (elem == null) {
 			return new ArrayList<ScriptVariable>();
 		}
 
 		final AbstractProcess process = ModelHelper.getParentProcess(elem);
 		final List<ScriptVariable> result = new ArrayList<ScriptVariable>();
 
 		for (final SimulationData d : process.getSimulationData()) {
 			final ScriptVariable field = createScriptVariable(d);
 			if (field != null) {
 				result.add(field);
 			}
 
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<ScriptVariable> createScriptVariablesFromFormElement(
 			final Element elem, final boolean showFieldVarFromOtherForms,
 			final boolean showFieldVarFromOwnForms) {
 
 		Form form = null;
 		// get the form
 		if (elem instanceof Widget) {
 			form = ModelHelper.getForm((Widget) elem);
 		} else if (elem instanceof Form) {
 			form = (Form) elem;
 		}
 
 		if (form == null) {
 			return Collections.emptyList();
 		}
 
 		// Add all page flow transient data
 
 		boolean isInEntryPageFlow = false;
 		boolean isInViewPageFlow = false;
 		boolean isInOverviewPageFlow = false;
 		if (form != null) {
 			final EReference feature = form.eContainmentFeature();
 			isInEntryPageFlow = feature
 					.equals(ProcessPackage.Literals.PAGE_FLOW__FORM);
 			isInViewPageFlow = feature
 					.equals(ProcessPackage.Literals.VIEW_PAGE_FLOW__VIEW_FORM);
 			isInOverviewPageFlow = feature
 					.equals(ProcessPackage.Literals.RECAP_FLOW__RECAP_FORMS);
 		}
 		Element currentObject = elem;
 		final ArrayList<Data> pageFlowTransientData = new ArrayList<Data>();
 
 		while (currentObject != null) {
 			if (isInEntryPageFlow && currentObject instanceof PageFlow) {
 				pageFlowTransientData
 				.addAll((List<Data>) currentObject
 						.eGet(ProcessPackage.Literals.PAGE_FLOW__TRANSIENT_DATA));
 			}
 			if (isInViewPageFlow && currentObject instanceof ViewPageFlow) {
 				pageFlowTransientData
 				.addAll((List<Data>) currentObject
 						.eGet(ProcessPackage.Literals.VIEW_PAGE_FLOW__VIEW_TRANSIENT_DATA));
 			}
 			if (isInOverviewPageFlow && currentObject instanceof RecapFlow) {
 				pageFlowTransientData
 				.addAll((List<Data>) currentObject
 						.eGet(ProcessPackage.Literals.RECAP_FLOW__RECAP_TRANSIENT_DATA));
 			}
 			currentObject = (Element) currentObject.eContainer();
 		}
 
 		final List<ScriptVariable> result = new ArrayList<ScriptVariable>();
 		final Element eContainer = (Element) form.eContainer();
 		if (eContainer != null) {
 			final List<Data> datas = ModelHelper.getAccessibleData(eContainer);
 			for (final Data d : datas) {
 				final ScriptVariable field = createScriptVariable(d);
 				if (field != null) {
 					result.add(field);
 				}
 			}
 		}
 		for (final Data d : pageFlowTransientData) {
 			final ScriptVariable field = createScriptVariable(d);
 			if (field != null) {
 				result.add(field);
 			}
 		}
 		if (showFieldVarFromOwnForms) {
 			// get all fields from current form
 			for (final Iterator<?> iterator = form.eAllContents(); iterator
 					.hasNext();) {
 				final EObject child = (EObject) iterator.next();
 				if (child instanceof FormField) {
 					final ScriptVariable field = createScriptVariable((Widget) child);
 					if (field != null) {
 						result.add(field);
 					}
 				}
 			}
 		}
 		if (showFieldVarFromOtherForms) {
 			// get all fields from pageflow
 			final EObject pageFlow = form.eContainer();
 			final EStructuralFeature feature = form.eContainingFeature();
 			if (pageFlow != null) {
 				for (final Iterator<?> iterator = ((List<?>) pageFlow
 						.eGet(feature)).iterator(); iterator.hasNext();) {
 					final EObject eObject = (EObject) iterator.next();
 					if (!eObject.equals(form)) {
 						for (final Iterator<?> iterator2 = eObject
 								.eAllContents(); iterator2.hasNext();) {
 							final EObject child = (EObject) iterator2.next();
 							if (child instanceof FormField) {
 								final ScriptVariable field = createScriptVariable((Widget) child);
 								if (field != null) {
 									result.add(field);
 								}
 							}
 						}
 					}
 				}
 			}
 
 		}
 		if (!showFieldVarFromOwnForms && !showFieldVarFromOtherForms
 				&& elem instanceof Widget) {
 			result.add(createScriptVariable((Widget) elem));
 		}
 
 		return result;
 	}
 
 	private static List<ExpressionConstants> getBonitaConstantsFor(
 			EObject context, ViewerFilter[] filters) {
 		final List<ExpressionConstants> result = new ArrayList<ExpressionConstants>();
 		DisplayEngineExpressionWithName engineFilter = null;
 		if(filters != null){
 			for(ViewerFilter f : filters){
 				if( f instanceof DisplayEngineExpressionWithName){
 					engineFilter = (DisplayEngineExpressionWithName) f;
 				}
 			}
 		}
 
 		result.add(ExpressionConstants.API_ACCESSOR);
 		result.add(ExpressionConstants.PROCESS_DEFINITION_ID);
 		result.add(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID);
 		result.add(ExpressionConstants.PROCESS_INSTANCE_ID);
 		result.add(ExpressionConstants.ACTIVITY_INSTANCE_ID);
 
 		if(context instanceof Expression){
 			context = context.eContainer();
 		}
 		
 		if (context instanceof Activity) {
 			if (((Activity) context).isIsMultiInstance()) {
 				if(engineFilter != null){
 					if(engineFilter.select(null, null, ExpressionConstants.NUMBER_OF_ACTIVE_INSTANCES.getEngineConstantName())){
 						result.add(ExpressionConstants.NUMBER_OF_ACTIVE_INSTANCES);
 					}
 					if(engineFilter.select(null, null, ExpressionConstants.NUMBER_OF_TERMINATED_INSTANCES.getEngineConstantName())){
 						result.add(ExpressionConstants.NUMBER_OF_TERMINATED_INSTANCES);
 					}
 					if(engineFilter.select(null, null, ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName())){
 						result.add(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES);
 					}
 					if(engineFilter.select(null, null, ExpressionConstants.NUMBER_OF_INSTANCES.getEngineConstantName())){
 						result.add(ExpressionConstants.NUMBER_OF_INSTANCES);
 					}
 				}
 			} else if (((Activity) context).getIsLoop()) {
 				if(engineFilter != null && engineFilter.select(null, null, ExpressionConstants.LOOP_COUNTER.getEngineConstantName())){
 					result.add(ExpressionConstants.LOOP_COUNTER);
 				}
 			}
 		}
 
 		if (context instanceof Task) {
 			result.add(ExpressionConstants.TASK_ASSIGNEE_ID);
 		} else if (context instanceof Widget
 				&& ModelHelper.getPageFlow((Widget) context) != null) {
 			result.add(ExpressionConstants.LOGGED_USER_ID);
 			if(!(ModelHelper.getPageFlow((Widget) context) instanceof AbstractProcess)){
 				result.add(ExpressionConstants.TASK_ASSIGNEE_ID);
 			}
 		} else if (context instanceof Form) {
 			result.add(ExpressionConstants.LOGGED_USER_ID);
			if(!(ModelHelper.getPageFlow((Widget) context) instanceof AbstractProcess)){
 				result.add(ExpressionConstants.TASK_ASSIGNEE_ID);
 			}
 		}
 
 		return result;
 	}
 
 	public static List<String> getBonitaKeyWords(final EObject context, final ViewerFilter[] filters) {
 		final List<ExpressionConstants> bonitaConstantsFor = getBonitaConstantsFor(context,filters);
 		final ArrayList<String> result = new ArrayList<String>(
 				bonitaConstantsFor.size());
 		for (final ExpressionConstants expressionConstants : bonitaConstantsFor) {
 			result.add(expressionConstants.getEngineConstantName());
 		}
 		if (context!=null && context instanceof Validator){
 			result.add(AbstractFormValidator.CLICKED_BUTTON_VARNAME);
 		}
 		return result;
 	}
 
 	private static void addBonitaVariables(final List<ScriptVariable> result,
 			final EObject element, ViewerFilter[] filters) {
 		final List<ExpressionConstants> bonitaConstantsFor = getBonitaConstantsFor(element,filters);
 		for (final ExpressionConstants expressionConstants : bonitaConstantsFor) {
 			result.add(new ScriptVariable(expressionConstants.getEngineConstantName(),getEngineExpressionReturnType(expressionConstants.getEngineConstantName())));
 		}
 		if (element!=null && element instanceof Validator){
 			result.add(new ScriptVariable(AbstractFormValidator.CLICKED_BUTTON_VARNAME,
 					String.class.getName()));
 		}
 	}
 
 	public static Expression getEngineConstantExpression(final String name) {
 		if (expressions == null) {
 			final ExpressionConstants[] values = ExpressionConstants.values();
 			expressions = new HashMap<String, Expression>(values.length);
 			for (final ExpressionConstants constant : values) {
 				expressions.put(constant.getEngineConstantName(),
 						createExpression(constant));
 			}
 		}
 		return expressions.get(name);
 	}
 
 	private static Expression createExpression(
 			final ExpressionConstants engineConstant) {
 		final Expression exp = ExpressionFactory.eINSTANCE.createExpression();
 		exp.setType(org.bonitasoft.studio.common.ExpressionConstants.ENGINE_CONSTANT_TYPE);
 		exp.setContent(engineConstant.getEngineConstantName());
 		exp.setName(engineConstant.getEngineConstantName());
 		exp.setReturnType(getEngineExpressionReturnType(engineConstant.getEngineConstantName()));
 		return exp;
 	}
 
 	public static boolean isGroovyExpression(final String text) {
 		return text.startsWith(GROOVY_PREFIX) && text.endsWith(GROOVY_SUFFIX);
 	}
 
 	public static String toSimpleExpression(String expression) {
 		if (isGroovyExpression(expression)) {
 			expression = expression.substring(expression.indexOf(GROOVY_PREFIX)
 					+ GROOVY_PREFIX.length(),
 					expression.lastIndexOf(GROOVY_SUFFIX));
 		}
 		return expression;
 	}
 
 	/**
 	 * @param expression
 	 * @return
 	 */
 	public static String toGroovyExpression(final String expression) {
 		if (isGroovyExpression(expression)) {
 			return expression;
 		} else {
 			return GROOVY_PREFIX + expression + GROOVY_SUFFIX;
 		}
 	}
 
 	public static List<ScriptVariable> getBonitaVariables(final EObject element, ViewerFilter[] filters) {
 		final List<ScriptVariable> result = new ArrayList<ScriptVariable>();
 		addBonitaVariables(result, element,filters);
 		return result;
 	}
 
 	/**
 	 * Helper method to retrieve the constant field of Custom Groovy Type
 	 * 
 	 * @param className
 	 * @return list of String (the values)
 	 */
 	public static List<String> getTypeValues(final String className) {
 
 		final List<String> result = new ArrayList<String>();
 		try {
 			final IType t = getType(className);
 			if(t == null){
 				return result;
 			}
 			for (final IField f : t.getFields()) {
 				final String fieldSource = f.getSource();
 				if (fieldSource != null
 						&& fieldSource.indexOf(GROOVY_CONSTANT_SEPARATOR) != -1) {
 					result.add(fieldSource.substring(
 							fieldSource.indexOf(GROOVY_CONSTANT_SEPARATOR) + 1,
 							fieldSource.lastIndexOf(GROOVY_CONSTANT_SEPARATOR))); // )
 				}
 			}
 		} catch (final Exception e) {
 			BonitaStudioLog.error(e);
 		}
 		return result;
 	}
 
 	public static IType getType(final String className)
 			throws JavaModelException {
 		final IJavaProject project = RepositoryManager.getInstance()
 				.getCurrentRepository().getJavaProject();
 		final IType t = project.findType(className);
 		return t;
 	}
 
 	public static FieldNode createVariablesFromGroupIterator(final Group group,
 			final Class<?> clazz) {
 		return new FieldNode(group.getIteratorName(), Modifier.PUBLIC,
 				new ClassNode(clazz), new ClassNode(clazz), null);
 	}
 
 	public static boolean isMultipleData(final Element container,
 			String inputScript) {
 		if (inputScript != null && inputScript.trim().length() > 0) {
 			if (inputScript.startsWith(GROOVY_PREFIX)) {
 				inputScript = inputScript
 						.substring(2, inputScript.length() - 1);
 			}
 
 			for (final Data d : ModelHelper.getAccessibleData(container)) {
 				if (d.getName().equals(inputScript)) {
 					if (d.isMultiple()
 							|| d instanceof JavaObjectData
 							&& ((JavaObjectData) d).getClassName().equals(
 									"java.util.List")) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public static boolean containsAGroovyExpression(final String expression) {
 		final int begin = expression.indexOf(GROOVY_PREFIX);
 		int end = -1;
 		if (begin >= 0) {
 			end = begin
 					+ getGroovyExpressionEndIndex(expression.substring(begin));
 		}
 		return begin < end;
 	}
 
 	public static int getGroovyExpressionEndIndex(final String expression) {
 		int open = 0;
 		final char[] characters = expression.toCharArray();
 		for (int i = 1; i < characters.length; i++) {
 			if (characters[i] == '{') {
 				open++;
 			} else if (characters[i] == '}') {
 				open--;
 			}
 			if (open == 0) {
 				return i + 1;
 			}
 		}
 		return -1;
 	}
 
 	public static ScriptVariable createScriptVariable(final Data d) {
 		return new ScriptVariable(d.getName(),
 				org.bonitasoft.studio.common.DataUtil.getTechnicalTypeFor(d));
 	}
 
 	public static ScriptVariable createScriptVariable(final Output output) {
 		return new ScriptVariable(output.getName(), output.getType());
 	}
 
 	public static ScriptVariable createScriptVariable(final Widget widget) {
 		String type = String.class.getName();
 		if (widget instanceof Duplicable && ((Duplicable) widget).isDuplicate()) {
 			type = List.class.getName();
 		} else {
 			if (widget instanceof TextFormField
 					&& widget.getReturnTypeModifier() != null) {
 				type = widget.getReturnTypeModifier();
 			} else {
 				type = widget.getAssociatedReturnType();
 			}
 			if (widget instanceof FormField) {
 				if (widget instanceof Group) {
 					type = Map.class.getName();
 				} else if (widget instanceof DynamicTable) {
 					type = List.class.getName();
 				}
 			} else if (widget instanceof NextFormButton) {
 				type = Boolean.class.getName();
 			}
 		}
 		return new ScriptVariable(ExporterTools.FIELD_IDENTIFIER
 				+ widget.getName(), type);
 	}
 
 	public static List<ScriptVariable> createScriptVariablesFromFormElement(
 			final Element elem) {
 		if (elem == null || elem instanceof StartTimerEvent) {
 			return new ArrayList<ScriptVariable>();
 		}
 
 		if (elem instanceof Form || elem instanceof Widget) {
 			return createScriptVariablesFromFormElement(elem, true,
 					elem instanceof SubmitFormButton || elem instanceof Form);
 		}
 
 		final List<Data> datas = new ArrayList<Data>();
 
 		final AbstractProcess process = ModelHelper.getParentProcess(elem);
 		final List<ScriptVariable> result = new ArrayList<ScriptVariable>();
 
 		if (elem instanceof Connection) {
 			final SourceElement flowElem = ((SequenceFlow) elem).getSource();
 			if (flowElem instanceof DataAware) {
 				datas.addAll(((DataAware) flowElem).getData());
 			}
 
 		} else if (elem instanceof DataAware) {
 			datas.addAll(((DataAware) elem).getData());
 		}
 
 		final List<String> dataNames = new ArrayList<String>();
 
 		for (final Data d : datas) {
 			dataNames.add(d.getName());
 		}
 
 		for (final Data d : process.getData()) {
 			if (!dataNames.contains(d.getName())) {
 				datas.add(d);
 			}
 		}
 
 		for (final Data d : datas) {
 			final ScriptVariable field = createScriptVariable(d);
 			if (field != null) {
 				result.add(field);
 			}
 		}
 
 		return result;
 	}
 
 	public static ScriptVariable createScriptVariable(final Parameter p) {
 		return new ScriptVariable(p.getName(), p.getTypeClassname());
 	}
 
 
 	public static ScriptVariable createScriptVariable(final Expression e) {
 		if (org.bonitasoft.studio.common.ExpressionConstants.FORM_FIELD_TYPE
 				.equals(e.getType())) {
 			final Widget widget = (Widget) e.getReferencedElements().get(0);
 			return createScriptVariable(widget);
 		} else if (org.bonitasoft.studio.common.ExpressionConstants.VARIABLE_TYPE
 				.equals(e.getType())) {
 			final Data data = (Data) e.getReferencedElements().get(0);
 			return createScriptVariable(data);
 		} else if (org.bonitasoft.studio.common.ExpressionConstants.PARAMETER_TYPE
 				.equals(e.getType())) {
 			final Parameter parameter = (Parameter) e.getReferencedElements()
 					.get(0);
 			return createScriptVariable(parameter);
 		} else if (org.bonitasoft.studio.common.ExpressionConstants.CONNECTOR_OUTPUT_TYPE
 				.equals(e.getType())) {
 			final Output output = (Output) e.getReferencedElements().get(0);
 			return createScriptVariable(output);
 		} else if (org.bonitasoft.studio.common.ExpressionConstants.SIMULATION_VARIABLE_TYPE
 				.equals(e.getType())) {
 			final SimulationData data = (SimulationData) e
 					.getReferencedElements().get(0);
 			return createScriptVariable(data);
 		} else if (org.bonitasoft.studio.common.ExpressionConstants.ENGINE_CONSTANT_TYPE
 				.equals(e.getType())) {
 			return new ScriptVariable(e.getContent(),getEngineExpressionReturnType(e.getName()));
 		}else if (org.bonitasoft.studio.common.ExpressionConstants.DOCUMENT_TYPE.equals(e.getType())) {
 			return new ScriptVariable(e.getContent(), e.getReturnType());
 		}
 		return null;
 	}
 
 	private static String getEngineExpressionReturnType(String name) {
 		for(ExpressionConstants exp : ExpressionConstants.values()){
 			if(name.equals(exp.getEngineConstantName())){
 				if("apiAccessor".equals(name)){
 					try{
 						final String apiAccessorExtClassName = "com.bonitasoft.engine.api.APIAccessor";
 						ProjectUtil.getConsoleLibsBundle().loadClass(apiAccessorExtClassName);
 						return apiAccessorExtClassName;
 					}catch (Exception e) {
 						return exp.getReturnType();
 					}
 				}else{
 					return exp.getReturnType();
 				}
 			}
 		}
 		return null;
 	}
 }
