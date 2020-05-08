 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fuml.convert.xmof;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.modelexecution.fuml.convert.FUMLConvertPlugin;
 import org.modelexecution.fuml.convert.IConversionResult;
 import org.modelexecution.fuml.convert.IConversionStatus;
 import org.modelexecution.fuml.convert.IConverter;
 import org.modelexecution.fuml.convert.impl.ConversionResultImpl;
 import org.modelexecution.fuml.convert.impl.ConversionStatusImpl;
 import org.modelexecution.fuml.convert.xmof.internal.ElementFactory;
 import org.modelexecution.fuml.convert.xmof.internal.XMOFInput;
 import org.modelexecution.fuml.convert.xmof.internal.gen.ElementPopulatorSuite;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 
 import fUML.Syntax.Classes.Kernel.Element;
 
 /**
  * Converter for converting {@link Activity xMOF activities} or resources
  * containing {@link Activity xMOF activities} into
  * {@link fUML.Syntax.Activities.IntermediateActivities.Activity fUML
  * activities}.
  * 
  * @author Philip Langer (langer@big.tuwien.ac.at)
  * 
  */
 public class XMOFConverter implements IConverter {
 
 	private XMOFInput xmofInput;
 	private ConversionResultImpl result;
 	private ConversionStatusImpl status;
 
 	@Override
 	public boolean canConvert(Object input) {
 		if (input == null)
 			return false;
 		initializeXMOFInput(input);
 		return xmofInput.containsActivities();
 	}
 
 	private void initializeXMOFInput(Object input) {
 		if (needToInitializeXMOFInput(input)) {
 			xmofInput = new XMOFInput(input);
 		}
 	}
 
 	private boolean needToInitializeXMOFInput(Object input) {
 		return xmofInput == null || !xmofInput.getOriginalInput().equals(input);
 	}
 
 	@Override
 	public IConversionResult convert(Object input) {
 		if (!canConvert(input))
 			return createCannotConvertResult(input);
 		initializeXMOFInput(input);
 		return startConversion();
 	}
 
 	private IConversionResult createCannotConvertResult(Object input) {
 		ConversionResultImpl result = new ConversionResultImpl(input);
 		ConversionStatusImpl status = new ConversionStatusImpl(
 				XMOFConverterPlugin.ID,
 				IConversionStatus.CANNOT_CONVERT_INPUT_ERROR, "Cannot convert "
 						+ input.toString(), new IllegalArgumentException(
 						"Cannot convert " + input.toString()));
 		result.setStatus(status);
 		return result;
 	}
 
 	protected ConversionResultImpl startConversion() {
 		initializeResult();
 		instantiateModel();
 		populateModelValues();
 		setMainActivitiesToResult();
 		return result;
 	}
 
 	private void initializeResult() {
 		result = new ConversionResultImpl(xmofInput.getOriginalInput());
 		status = new ConversionStatusImpl(XMOFConverterPlugin.ID,
 				IConversionStatus.OK, "Initializing OK", null);
 		result.setStatus(status);
 	}
 
 	private void instantiateModel() {
 		ElementFactory factory = new ElementFactory();
 		for (EModelElement inputElement : xmofInput.getElementsToConvert()) {
 			instantiateElement(factory, inputElement);
 			for (TreeIterator<EObject> treeIterator = inputElement
 					.eAllContents(); treeIterator.hasNext();) {
 				EObject inputElementChild = treeIterator.next();
 				if (isEModelElementToBeProcessed(inputElementChild)) {
 					instantiateElement(factory,
 							(EModelElement) inputElementChild);
 				}
 			}
 		}
 	}
 
 	private boolean isEModelElementToBeProcessed(EObject inputElementChild) {
 		return inputElementChild instanceof EModelElement
 				&& isNotOppositeOfProcessedReference(inputElementChild);
 	}
 
 	private boolean isNotOppositeOfProcessedReference(EObject inputElementChild) {
 		return !(inputElementChild instanceof EReference)
 				|| result.getFUMLElement(((EReference) inputElementChild)
 						.getEOpposite()) == null;
 	}
 
 	private void instantiateElement(ElementFactory factory,
 			EModelElement inputElement) {
 		Element element = factory.create(inputElement);
 		if (element != null) {
 			result.addInOutMapping(inputElement, element);
 		} else {
 			addWarningToResult("Could not convert " + inputElement.toString());
 		}
 	}
 
 	private void populateModelValues() {
 		ElementPopulatorSuite populator = new ElementPopulatorSuite(result);
 		List<Entry<Object, Element>> mappedClasses = new ArrayList<Entry<Object, Element>>();
 		List<Entry<Object, Element>> mappedElementsExceptClasses = new ArrayList<Entry<Object, Element>>();
 		
 		for (Iterator<Entry<Object, Element>> iterator = createIteratorOverCopyOfMappings(); iterator
 				.hasNext();) {
 			Entry<Object, Element> mapping = iterator.next();
 			if (mapping.getKey() instanceof EClass) {
 				mappedClasses.add(mapping);
 			} else {
 				mappedElementsExceptClasses.add(mapping);
 			}
 		}
 		
 		populateModelValues(populator, mappedClasses);
 		populateModelValues(populator, mappedElementsExceptClasses);
 	}
 
 	private void populateModelValues(ElementPopulatorSuite populator,
 			List<Entry<Object, Element>> mappedClasses) {
 		for (Entry<Object, Element> mapping : mappedClasses) {
 			applyPopulator(populator, mapping.getValue(),
 					(ENamedElement) mapping.getKey());
 		}
 	}
 
 	private Iterator<Entry<Object, Element>> createIteratorOverCopyOfMappings() {
 		return new ArrayList<Entry<Object, Element>>(result.getMappings())
 				.iterator();
 	}
 
 	private void applyPopulator(ElementPopulatorSuite populator,
 			Element fUMLElement, EModelElement xmofElement) {
 		try {
 			populator.populate(fUMLElement, xmofElement);
 		} catch (Exception e) {
 			addErrorToResult(IConversionStatus.ERROR_WHILE_CONVERSION,
 					"Exception while applying feature values.", e);
 		}
 	}
 
 	private void setMainActivitiesToResult() {
 		for (Activity activity : xmofInput.getMainActivities()) {
 			Element fumlElement = result.getFUMLElement(activity);
 			if (fumlElement != null
 					&& fumlElement instanceof fUML.Syntax.Activities.IntermediateActivities.Activity) {
 				result.addActivity((fUML.Syntax.Activities.IntermediateActivities.Activity) fumlElement);
 			}
 		}
 	}
 
 	protected void addErrorToResult(int code, String message,
 			Throwable throwable) {
 		status.add(new Status(IStatus.ERROR, XMOFConverterPlugin.ID, code,
 				message, throwable));
 		FUMLConvertPlugin.instance.getLog().log(IStatus.ERROR, message,
 				throwable);
 
 		// TODO remove
 		throwable.printStackTrace();
 	}
 
 	protected void addInfoToResult(String message) {
 		status.add(new Status(IStatus.INFO, XMOFConverterPlugin.ID, message));
 	}
 
 	protected void addWarningToResult(String message) {
 		status.add(new Status(IStatus.WARNING, XMOFConverterPlugin.ID, message));
		FUMLConvertPlugin.log(IStatus.WARNING, message);
 	}
 
 }
