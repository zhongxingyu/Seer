 /*-
  * Copyright (C) 2011-2014 by Iwao AVE!
  * This program is made available under the terms of the MIT License.
  */
 
 package org.eclipselabs.stlipse.jspeditor;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.validation.AbstractValidator;
 import org.eclipse.wst.validation.ValidationResult;
 import org.eclipse.wst.validation.ValidationState;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 import org.eclipse.wst.validation.internal.provisional.core.IValidator;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipselabs.stlipse.Activator;
 import org.eclipselabs.stlipse.cache.BeanClassCache;
 import org.eclipselabs.stlipse.cache.BeanPropertyCache;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Iwao AVE!
  */
 @SuppressWarnings("restriction")
 public class JspValidator extends AbstractValidator implements IValidator
 {
 
 	public static final String MARKER_ID = "org.eclipselabs.stlipse.stripesJspProblem";
 
 	public static final String MISSING_ACTION_BEAN = "missingActionBean";
 
 	public static final String NO_WRITABLE_PROPERTY = "noWritableProperty";
 
 	public static final String NO_EVENT_HANDLER = "noEventhandler";
 
 	public void cleanup(IReporter reporter)
 	{
 		// Nothing to do.
 	}
 
 	public void validate(IValidationContext helper, IReporter reporter)
 		throws ValidationException
 	{
 		String[] uris = helper.getURIs();
 		if (uris != null)
 		{
 			for (int i = 0; i < uris.length && !reporter.isCancelled(); i++)
 			{
 				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uris[i]));
 				validateFile(file, reporter);
 			}
 		}
 	}
 
 	public ValidationResult validate(final IResource resource, int kind, ValidationState state,
 		IProgressMonitor monitor)
 	{
 		if (resource.getType() != IResource.FILE)
 			return null;
 		ValidationResult result = new ValidationResult();
 		final IReporter reporter = result.getReporter(monitor);
 		validateFile((IFile)resource, reporter);
 		return result;
 	}
 
 	/**
 	 * @param file
 	 * @param reporter
 	 */
 	private void validateFile(IFile file, IReporter reporter)
 	{
 		IStructuredModel model = null;
 		try
 		{
 			file.deleteMarkers(MARKER_ID, false, IResource.DEPTH_ZERO);
 			model = StructuredModelManager.getModelManager().getModelForRead(file);
 			IDOMModel domModel = (IDOMModel)model;
 			IDOMDocument domDoc = domModel.getDocument();
 			NodeList nodes = domDoc.getChildNodes();
 			for (int k = 0; k < nodes.getLength(); k++)
 			{
 				Node child = nodes.item(k);
 				if (child instanceof IDOMElement)
 				{
 					validateElement((IDOMElement)child, file, domDoc.getStructuredDocument(), reporter);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			Activator.log(Status.WARNING, "Error occurred during validation.", e);
 		}
 		finally
 		{
 			if (model != null)
 			{
 				model.releaseFromRead();
 			}
 		}
 	}
 
 	private void validateElement(IDOMElement element, IFile file, IStructuredDocument doc,
 		IReporter reporter) throws JavaModelException
 	{
 		if (element == null)
 			return;
 
 		IJavaProject project = JavaCore.create(file.getProject());
 		String tagName = element.getNodeName();
 		NamedNodeMap attrs = element.getAttributes();
 		for (int i = 0; i < attrs.getLength(); i++)
 		{
 			IDOMAttr attr = (IDOMAttr)attrs.item(i);
 			String attributeName = attr.getName();
 			String attrValue = attr.getValue().trim();
 
 			if (containsElExpression(attrValue))
 			{
 				// ignore EL expressions
 			}
 			else if ("beanclass".equals(attributeName))
 			{
 				validateBeanclass(project, file, doc, attr, attrValue);
 			}
 			else if (StripesTagUtil.isSuggestableFormTag(tagName, attributeName)
 				&& !"label".equals(StripesTagUtil.getStripesTagSuffix(tagName)))
 			{
 				String beanclass = StripesTagUtil.getParentBeanclass(element, "form");
 				validateField(project, file, doc, element, attr, beanclass, attrValue);
 			}
 			else if (StripesTagUtil.isParamTag(tagName, attributeName))
 			{
 				String beanclass = StripesTagUtil.getParentBeanclass(element, "url", "link");
 				validateField(project, file, doc, element, attr, beanclass, attrValue);
 			}
 			else if (StripesTagUtil.isSubmitTag(tagName, attributeName))
 			{
 				String beanclass = StripesTagUtil.getParentBeanclass(element, "form");
 				validateEvent(project, file, doc, element, attr, beanclass, attrValue, true);
 			}
 			else if (StripesTagUtil.isEventAttribute(tagName, attributeName))
 			{
 				String beanclass = StripesTagUtil.getBeanclassAttribute(element);
 				validateEvent(project, file, doc, element, attr, beanclass, attrValue, false);
 			}
 		}
 
 		NodeList nodes = element.getChildNodes();
 		for (int j = 0; j < nodes.getLength(); j++)
 		{
 			Node child = nodes.item(j);
 			if (child instanceof IDOMElement)
 			{
 				validateElement((IDOMElement)child, file, doc, reporter);
 			}
 		}
 	}
 
 	private void validateField(IJavaProject project, IFile file, IStructuredDocument doc,
 		IDOMElement element, IDOMAttr attr, String beanclass, String property)
 	{
		if (isBeanclassValidatable(beanclass))
 		{
 			Map<String, String> fields = BeanPropertyCache.searchFields(project, beanclass, property,
 				false, -1, true);
 			if (fields.size() == 0)
 			{
 				addMarker(file, doc, attr, NO_WRITABLE_PROPERTY, IMarker.SEVERITY_WARNING,
 					IMarker.PRIORITY_NORMAL, "Writable property '" + property + "' not found in "
 						+ beanclass);
 			}
 		}
 	}
 
 	private void validateEvent(IJavaProject project, IFile file, IStructuredDocument doc,
 		IDOMElement element, IDOMAttr attr, String beanclass, String event,
 		boolean validIfDefaultHandlerExists)
 	{
		if (isBeanclassValidatable(beanclass))
 		{
 			List<String> events = BeanPropertyCache.searchEventHandler(project, beanclass, event,
 				true, validIfDefaultHandlerExists);
 			if (events.size() == 0)
 			{
 				addMarker(file, doc, attr, NO_EVENT_HANDLER, IMarker.SEVERITY_WARNING,
 					IMarker.PRIORITY_NORMAL, "Event handler '" + event + "' not found in " + beanclass);
 			}
 		}
 	}
 
 	private void validateBeanclass(IJavaProject project, IFile file, IStructuredDocument doc,
 		IDOMAttr attr, String beanclass) throws JavaModelException
 	{
		if (isBeanclassValidatable(beanclass)
			&& !BeanClassCache.actionBeanExists(project, beanclass))
 		{
 			addMarker(file, doc, attr, MISSING_ACTION_BEAN, IMarker.SEVERITY_ERROR,
 				IMarker.PRIORITY_HIGH, "ActionBean '" + beanclass + "' not found.");
 		}
 	}
 
	private boolean isBeanclassValidatable(String beanclass)
	{
		return beanclass != null && beanclass.indexOf('$') == -1;
	}

 	private boolean containsElExpression(String str)
 	{
 		boolean inIndexedProperty = false;
 		for (int i = 0; i < str.length(); i++)
 		{
 			char c = str.charAt(i);
 			if (!inIndexedProperty && c == '$')
 				return true;
 			else if (!inIndexedProperty && c == '[')
 				inIndexedProperty = true;
 			else if (c == ']')
 				inIndexedProperty = false;
 		}
 		return false;
 	}
 
 	private void addMarker(IFile file, IStructuredDocument doc, IDOMAttr attr,
 		String problemType, int severity, int priority, String message)
 	{
 		try
 		{
 			int start = attr.getValueRegionStartOffset();
 			int length = attr.getValueRegionText().length();
 			int lineNo = doc.getLineOfOffset(start);
 			IMarker marker = file.createMarker(MARKER_ID);
 			marker.setAttribute(IMarker.SEVERITY, severity);
 			marker.setAttribute(IMarker.PRIORITY, priority);
 			marker.setAttribute(IMarker.MESSAGE, message);
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
 			if (start != 0)
 			{
 				marker.setAttribute(IMarker.CHAR_START, start);
 				marker.setAttribute(IMarker.CHAR_END, start + length);
 			}
 			// Adds custom attributes.
 			marker.setAttribute("problemType", problemType);
 			marker.setAttribute("errorValue", attr.getValue());
 		}
 		catch (CoreException e)
 		{
 			Activator.log(Status.ERROR, "Failed to create a custom marker.", e);
 		}
 	}
 }
