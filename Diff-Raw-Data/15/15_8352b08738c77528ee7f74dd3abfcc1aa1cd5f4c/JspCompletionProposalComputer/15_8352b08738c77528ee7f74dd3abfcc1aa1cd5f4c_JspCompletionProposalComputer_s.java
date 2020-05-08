 /*-
  * Copyright (C) 2011 by Iwao AVE!
  * This program is made available under the terms of the MIT License.
  */
 
 package org.eclipselabs.stlipse.jspeditor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
 import org.eclipse.jdt.core.search.SearchEngine;
 import org.eclipse.jdt.core.search.SearchPattern;
 import org.eclipse.jdt.core.search.TypeNameRequestor;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.CompletionProposal;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jst.j2ee.common.ParamValue;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.j2ee.webapplication.Filter;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.sse.core.utils.StringUtils;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLCompletionProposalComputer;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;
 import org.eclipselabs.stlipse.Activator;
 import org.eclipselabs.stlipse.ast.BeanParser;
 import org.eclipselabs.stlipse.util.ClassNameUtil;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * The super class {@link AbstractXMLCompletionProposalComputer} will be available to API.
  * https://bugs.eclipse.org/bugs/show_bug.cgi?id=310696
  * 
  * @author ave
  */
 public class JspCompletionProposalComputer extends DefaultXMLCompletionProposalComputer
 {
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest,
 		CompletionProposalInvocationContext context)
 	{
 		IDOMNode node = (IDOMNode)contentAssistRequest.getNode();
 		String tagName = node.getNodeName();
 		IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
 		ITextRegionList openRegions = open.getRegions();
 		int i = openRegions.indexOf(contentAssistRequest.getRegion());
 		if (i < 0)
 			return;
 		ITextRegion nameRegion = null;
 		while (i >= 0)
 		{
 			nameRegion = openRegions.get(i--);
 			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME)
 				break;
 		}
 
 		// get the attribute in question (first attr name to the left of the cursor)
 		String attributeName = null;
 		if (nameRegion != null)
 			attributeName = open.getText(nameRegion);
 
 		if (isProposalsAvailable(tagName, attributeName))
 		{
 			// boolean existingComplicatedValue = contentAssistRequest.getRegion() != null
 			// && contentAssistRequest.getRegion() instanceof ITextRegionContainer;
 			// if (existingComplicatedValue)
 			// {
 			// contentAssistRequest.getProposals().clear();
 			// contentAssistRequest.getMacros().clear();
 			// return;
 			// }
 			String currentValue = null;
 			if (contentAssistRequest.getRegion().getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)
 				currentValue = contentAssistRequest.getText();
 			else
 				currentValue = "";
 
 			String matchString = null;
 			int start = contentAssistRequest.getReplacementBeginPosition();
 			int length = contentAssistRequest.getReplacementLength();
 			if (currentValue.length() > StringUtils.strip(currentValue).length()
 				&& (currentValue.startsWith("\"") || currentValue.startsWith("'"))
 				&& contentAssistRequest.getMatchString().length() > 0)
 			{
 				// Value is surrounded by (double) quotes.
 				matchString = currentValue.substring(1, contentAssistRequest.getMatchString()
 					.length());
 				start++;
 				length = currentValue.length() - 2;
 			}
 			else
 				matchString = currentValue.substring(0, contentAssistRequest.getMatchString()
 					.length());
 
 			if ("beanclass".equalsIgnoreCase(attributeName))
 			{
 				List<ICompletionProposal> classProposals = getBeanclassProposals(context,
 					contentAssistRequest, matchString, start, length);
 				if (classProposals != null)
 				{
 					for (ICompletionProposal proposal : classProposals)
 					{
 						contentAssistRequest.addProposal(proposal);
 					}
 				}
 			}
 			else if (isActionBeanProperty(tagName, attributeName))
 			{
 				Node parentNode = node.getParentNode();
 				while (parentNode != null && !isFormTag(parentNode.getNodeName()))
 				{
 					parentNode = parentNode.getParentNode();
 				}
 				if (parentNode == null)
 				{
 					Activator.log(Status.WARNING, "Stripes form tag was not found.");
 				}
 				else
 				{
 					NamedNodeMap attributes = parentNode.getAttributes();
 					Node beanclassAttribute = attributes.getNamedItem("beanclass");
 					if (beanclassAttribute == null)
 					{
 						Activator.log(Status.WARNING,
 							"Stripes form tag does not have a 'beanclass' attribute.");
 					}
 					else
 					{
 						String qualifiedName = beanclassAttribute.getNodeValue();
 						IResource resource = getResource(contentAssistRequest);
 						IJavaProject project = getJavaProject(resource);
 						try
 						{
 							Map<String, ITypeBinding> fields = BeanParser.searchFields(project,
 								qualifiedName, matchString, -1);
 							List<ICompletionProposal> proposals = buildFieldNameProposal(
 								fields, matchString, start, length);
 							for (ICompletionProposal proposal : proposals)
 							{
 								contentAssistRequest.addProposal(proposal);
 							}
 						}
 						catch (JavaModelException e)
 						{
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private List<ICompletionProposal> buildFieldNameProposal(Map<String, ITypeBinding> fields,
 		final String input, final int offset, final int replacementLength)
 	{
 		int lastDot = input.lastIndexOf(".");
 		String prefix = lastDot > -1 ? input.substring(0, lastDot) : "";
 		List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();
 		for (Entry<String, ITypeBinding> fieldEntry : fields.entrySet())
 		{
 			String fieldName = fieldEntry.getKey();
 			ITypeBinding fieldType = fieldEntry.getValue();
 			StringBuilder replaceStr = new StringBuilder();
 			if (lastDot > -1)
 				replaceStr.append(prefix).append('.');
 			replaceStr.append(fieldName);
 			StringBuilder displayStr = new StringBuilder();
 			displayStr.append(fieldName).append(" - ").append(fieldType.getQualifiedName());
 			ICompletionProposal proposal = new CompletionProposal(replaceStr.toString(),
 				offset, replacementLength, replaceStr.length(), null, displayStr.toString(),
 				null, null);
 			proposalList.add(proposal);
 		}
 		return proposalList;
 	}
 
 	protected static List<ICompletionProposal> getBeanclassProposals(
 		CompletionProposalInvocationContext context, ContentAssistRequest contentAssistRequest,
 		final String input, final int offset, final int replacementLength)
 	{
 		final List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();
 		IResource resource = getResource(contentAssistRequest);
 		final List<String> packageList = getActionResolverPackages(resource.getProject());
 		// Returns an empty list if no package root for action beans is defined.
 		if (packageList.size() == 0)
 			return proposalList;
 
 		IJavaProject project = getJavaProject(resource);
 		IType actionBeanInterface;
 		try
 		{
 			actionBeanInterface = project.findType("net.sourceforge.stripes.action.ActionBean");
 			IJavaSearchScope scope = SearchEngine.createHierarchyScope(actionBeanInterface);
 			TypeNameRequestor requestor = new TypeNameRequestor()
 			{
 				@Override
 				public void acceptType(int modifiers, char[] packageName,
 					char[] simpleTypeName, char[][] enclosingTypeNames, String path)
 				{
 					// Ignore abstract classes.
 					if (Flags.isAbstract(modifiers))
 						return;
 					// Should be in action resolver packages.
 					if (!isInTargetPackage(packageList, String.valueOf(packageName)))
 						return;
 
 					StringBuilder replacementString = new StringBuilder();
 					replacementString.append(packageName).append('.').append(simpleTypeName);
 
 					StringBuilder displayString = new StringBuilder();
 					displayString.append(simpleTypeName).append(" - ").append(packageName);
 
 					int replacementOffset = offset;
 					int cursorPosition = replacementString.length();
 					ICompletionProposal proposal = new CompletionProposal(
 						replacementString.toString(), replacementOffset, replacementLength,
 						cursorPosition, null, displayString.toString(), null, null);
 					proposalList.add(proposal);
 				}
 
 				private boolean isInTargetPackage(List<String> targetPackages,
 					String packageToTest)
 				{
 					if (targetPackages == null || packageToTest == null)
 						return false;
 
 					for (String targetPackage : targetPackages)
 					{
 						if (packageToTest.startsWith(targetPackage))
 							return true;
 					}
 					return false;
 				}
 			};
 
 			SearchEngine searchEngine = new SearchEngine();
 			searchEngine.searchAllTypeNames(ClassNameUtil.getPackage(input).toCharArray(),
 				SearchPattern.R_PREFIX_MATCH, ClassNameUtil.getTypeName(input).toCharArray(),
 				SearchPattern.R_CAMELCASE_MATCH, IJavaSearchConstants.DECLARATIONS
 					| IJavaSearchConstants.IMPLEMENTORS, scope, requestor,
 				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
 		}
 		catch (CoreException e)
 		{
 			Activator.log(Status.ERROR, "Error occurred while creating proposals.", e);
 		}
 		return proposalList;
 	}
 
 	private static IJavaProject getJavaProject(IResource resource)
 	{
 		IProject proj = resource.getProject();
 		IJavaProject javaProject = JavaCore.create(proj);
 		return javaProject;
 	}
 
 	private static IResource getResource(ContentAssistRequest request)
 	{
 		IResource resource = null;
 		String baselocation = null;
 
 		if (request != null)
 		{
 			IStructuredDocumentRegion region = request.getDocumentRegion();
 			if (region != null)
 			{
 				IDocument document = region.getParentDocument();
 				IStructuredModel model = null;
 				try
 				{
 					model = StructuredModelManager.getModelManager().getExistingModelForRead(
 						document);
 					if (model != null)
 					{
 						baselocation = model.getBaseLocation();
 					}
 				}
 				finally
 				{
 					if (model != null)
 						model.releaseFromRead();
 				}
 			}
 		}
 
 		if (baselocation != null)
 		{
 			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 			IPath filePath = new Path(baselocation);
 			IFile file = null;
 
 			if (filePath.segmentCount() > 1)
 			{
 				file = root.getFile(filePath);
 			}
 			if (file != null)
 			{
 				resource = file.getProject();
 			}
 		}
 		return resource;
 	}
 
 	/**
 	 * Returns package names listed as ActionResolver.Packages init-param in the project's
 	 * web.xml.
 	 * 
 	 * @param project
 	 * @return a list of packages.
 	 */
 	private static List<String> getActionResolverPackages(IProject project)
 	{
 		List<String> packageList = new ArrayList<String>();
 		WebArtifactEdit artifactEdit = WebArtifactEdit.getWebArtifactEditForRead(project);
 		WebApp webApp = artifactEdit.getWebApp();
 		EList<Filter> filters = webApp.getFilters();
 		for (Filter filter : filters)
 		{
 			EList<ParamValue> initParamValues = filter.getInitParamValues();
 			for (ParamValue paramValue : initParamValues)
 			{
 				if ("ActionResolver.Packages".equals(paramValue.getName()))
 				{
 					String packages = paramValue.getValue();
 					if (packages != null)
 					{
 						String[] pkgArray = packages.split(",");
 						for (String pkg : pkgArray)
 						{
 							String trimmed = pkg.trim();
 							if (trimmed.length() > 0)
 								packageList.add(trimmed);
 						}
 					}
 				}
 			}
 		}
 
 		if (packageList.size() == 0)
 		{
 			Activator.log(Status.WARNING,
 				"The filter init-param 'ActionResolver.Packages' was not found in your web.xml. ");
 		}
 
 		return packageList;
 	}
 
 	private boolean isFormTag(String tagName)
 	{
 		String suffix = getStripesTagSuffix(tagName);
 		return "form".equals(suffix);
 	}
 
 	private boolean isProposalsAvailable(String tagName, String attributeName)
 	{
 		if ("beanclass".equalsIgnoreCase(attributeName))
 		{
 			return true;
 		}
 		return isActionBeanProperty(tagName, attributeName);
 	}
 
 	private boolean isActionBeanProperty(String tagName, String attributeName)
 	{
 		String suffix = getStripesTagSuffix(tagName);
		return "name".equalsIgnoreCase(attributeName) && isSuggestable(suffix);
 	}
 
	private boolean isSuggestable(String tag)
 	{
		final List<String> tags = Arrays.asList("checkbox", "file", "hidden", "password",
			"radio", "select", "text", "textarea");
 		return tags.contains(tag);
 	}
 
 	private String getStripesTagSuffix(String tagName)
 	{
 		final String prefix = getStripesTagPrefix(tagName);
 		if (prefix != null && prefix.length() + 1 < tagName.length())
 			return tagName.substring(prefix.length() + 1);
 		return null;
 	}
 
 	private String getStripesTagPrefix(String tagName)
 	{
 		Set<String> prefixes = getPrefixesFromPreference();
 		int prefixLength = tagName.indexOf(':');
 		if (prefixLength > 0)
 		{
 			String prefix = tagName.substring(0, prefixLength);
 			if (prefixes.contains(prefix))
 				return prefix;
 		}
 		return null;
 	}
 
 	private Set<String> getPrefixesFromPreference()
 	{
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		List<String> list = Arrays.asList(store.getString("tagPrefixes").split(" *, *"));
 		Set<String> result = new HashSet<String>();
 		result.addAll(list);
 		return result;
 	}
 }
