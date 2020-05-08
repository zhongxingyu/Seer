 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.internal.proposal;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 
 /**
  * @author Alexey Kazakov
  */
 public class ResourcePathProposalType extends ModelProposalType {
 
 	private static final String IMAGE_NAME = "ResourcePathProposal.gif"; //$NON-NLS-1$
 	private static Image ICON;
 	private static Set<String> GRAPHIC_FILE_EXTENSIONS = new HashSet<String>();
 	private static Set<String> PAGE_FILE_EXTENSIONS = new HashSet<String>();
 	private static Set<String> CSS_FILE_EXTENSIONS = new HashSet<String>();
 	static {
 		String[] images = {"gif", "jpeg", "jpg", "png", "wbmp", "bmp"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 		for (int i = 0; i < images.length; i++) {
 			GRAPHIC_FILE_EXTENSIONS.add(images[i]);
 		}
 		String[] pages = {"jsp", "htm", "html", "xhtml", "xml"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		for (int i = 0; i < pages.length; i++) {
 			PAGE_FILE_EXTENSIONS.add(pages[i]);
 		}
 		String[] css = {"css", "xcss"}; //$NON-NLS-1$ //$NON-NLS-2$
 		for (int i = 0; i < css.length; i++) {
 			CSS_FILE_EXTENSIONS.add(css[i]);
 		}
 	}
 	private static String PATH_ADDITION = "pathAddition"; //$NON-NLS-1$
 
 	private IContainer webRootResource;
 	private Set<String> extensions = new HashSet<String>();
 	private String optionalPrefix;
 	private List<String> enumeration;
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.ModelProposalType#init(org.jboss.tools.jst.web.kb.IPageContext)
 	 */
 	@Override
 	protected void init(IPageContext context) {
 		super.init(context);
 		webRootResource = null;
 		if(xModel != null) {
 			XModelObject webInf = FileSystemsHelper.getWebInf(xModel);
 			XModelObject webRoot = FileSystemsHelper.getWebRoot(xModel);
 			if(webInf != null && webRoot != null) {
 				webRootResource = (IContainer)EclipseResourceUtil.getResource(webRoot);
 			}
 		}
 //		if(extensions==null) {
 			initExtensions();
 //		}
 		if(enumeration==null) {
 			enumeration = new ArrayList<String>();
 			if(params!=null) {
 				for (int i = 0; i < params.length; i++) {
 					if(PATH_ADDITION.equals(params[i].getName())) {
 						enumeration.add(params[i].getValue());
 					}
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.CustomProposalType#getProposals(org.jboss.tools.jst.web.kb.KbQuery)
 	 */
 	@Override
 	public TextProposal[] getProposals(KbQuery query) {
 		if(!isReadyToUse()) {
 			return EMPTY_PROPOSAL_LIST;
 		}
 		List<TextProposal> proposals = new ArrayList<TextProposal>();
 		String newValue = null;
 		String value = query.getValue();
 		if(optionalPrefix!=null) {
 			char[] optionalPrefixArray = optionalPrefix.toCharArray();
 			StringBuffer prefix = new StringBuffer();
 			for (char c : optionalPrefixArray) {
 				prefix.append(c);
 				if(value.startsWith(prefix.toString())) {
 					newValue = value.substring(prefix.length());
 				}
 			}
 		}
 		if(newValue==null) {
 			newValue = value;
 		}
 		ResourcePathDescriptor[] resources = getResourcePathes(newValue);
 		for(int i=0; i<resources.length; i++) {
 			TextProposal proposal = new TextProposal();
 			proposal.setLabel(resources[i].getQueryPath());
 			String replacementString = resources[i].getQueryPath();
 			if(resources[i].getResource() instanceof IFolder) {
 				replacementString = replacementString + "/"; //$NON-NLS-1$
 				proposal.setAutoActivationContentAssistantAfterApplication(true);
 			}
 			proposal.setReplacementString(replacementString);
 			proposal.setPosition(replacementString.length());
 			if(ICON==null) {
 				ICON = ImageDescriptor.createFromFile(WebKbPlugin.class, IMAGE_NAME).createImage();
 			}
 			proposal.setImage(ICON);
 			if(newValue == value) {
 				proposals.add(proposal);
 			}
 			if(optionalPrefix!=null) {
 				try {
					TextProposal clone = proposal.clone();
 					clone.setLabel(optionalPrefix + proposal.getLabel());
 					clone.setReplacementString(optionalPrefix + proposal.getReplacementString());
 					clone.setPosition(clone.getReplacementString().length());
 					proposals.add(clone);
 				} catch (CloneNotSupportedException e) {
 					WebKbPlugin.getDefault().logError(e);
 				}
 			}
 		}
 		for (String path : enumeration) {
 			TextProposal proposal = new TextProposal();
 			proposal.setLabel(path);
 			String replacementString = path;
 			proposal.setAutoActivationContentAssistantAfterApplication(false);
 			proposal.setReplacementString(replacementString);
 			proposal.setPosition(replacementString.length());
 			if(ICON==null) {
 				ICON = ImageDescriptor.createFromFile(WebKbPlugin.class, IMAGE_NAME).createImage();
 			}
 			proposal.setImage(ICON);
 			proposals.add(proposal);
 		}
 		return proposals.toArray(new TextProposal[0]);
 	}
 
 	private static final String EXTENSIONS_PARAM_NAME = "extensions"; //$NON-NLS-1$
 	private static final String IMAGE_PARAM_TYPE = "%image%"; //$NON-NLS-1$
 	private static final String PAGE_PARAM_TYPE = "%page%"; //$NON-NLS-1$
 	private static final String CSS_PARAM_TYPE = "%css%"; //$NON-NLS-1$
 	private static final String OPTIONAL_PREFIX = "optionalPrefix"; //$NON-NLS-1$
 
 	private void initExtensions() {
 		extensions.clear();
 		String value = getParamValue(EXTENSIONS_PARAM_NAME);
 		if(value != null && !value.equals("*")) { //$NON-NLS-1$
 			if(IMAGE_PARAM_TYPE.equals(value)) {
 				this.extensions.addAll(GRAPHIC_FILE_EXTENSIONS);
 			} else if(PAGE_PARAM_TYPE.equals(value)) {
 				this.extensions.addAll(PAGE_FILE_EXTENSIONS);
 			} else if(CSS_PARAM_TYPE.equals(value)) {
 				this.extensions.addAll(CSS_FILE_EXTENSIONS);
 			} else {
 				StringTokenizer st = new StringTokenizer(value, ",;"); //$NON-NLS-1$
 				if(st.countTokens() > 0) {
 					while(st.hasMoreTokens()) {
 						String t = st.nextToken().trim();
 						if(t.length() == 0) {
 							continue;
 						}
 						if(IMAGE_PARAM_TYPE.equals(t)) {
 							extensions.addAll(GRAPHIC_FILE_EXTENSIONS);
 						} else if(PAGE_PARAM_TYPE.equals(t)) {
 							extensions.addAll(PAGE_FILE_EXTENSIONS);
 						} else if(CSS_PARAM_TYPE.equals(t)) {
 							extensions.addAll(CSS_FILE_EXTENSIONS);
 						} else {
 							extensions.add(t);
 						}
 					}
 				}
 			}
 		}
 		optionalPrefix = getParamValue(OPTIONAL_PREFIX);
 		if(optionalPrefix!=null && optionalPrefix.trim().length()==0) {
 			optionalPrefix = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.taglib.ModelProposalType#isReadyToUse()
 	 */
 	protected boolean isReadyToUse() {
 		return webRootResource!=null;
 	}
 
 	private ResourcePathDescriptor[] getResourcePathes(String query) {
 		query = query.trim();
 		if(query.indexOf('\\')>-1) {
 			return new ResourcePathDescriptor[0];
 		}
 		if(query.length()==0) {
 			query = "/"; //$NON-NLS-1$
 		}
 		int lastSeparator = query.lastIndexOf('/');
 		String name = null;
 		String pathWithoutLastSegment = null;
 		if(lastSeparator>-1) {
 			pathWithoutLastSegment = query.substring(0, lastSeparator);
 			if(lastSeparator+1<query.length()) {
 				name = query.substring(lastSeparator+1, query.length());
 			} else {
 				name = ""; //$NON-NLS-1$
 			}
 		} else {
 			pathWithoutLastSegment = ""; //$NON-NLS-1$
 			name = query;
 		}
 		if(name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
 			if(pathWithoutLastSegment.length()>0) {
 				pathWithoutLastSegment = pathWithoutLastSegment + "/" + name; //$NON-NLS-1$
 			} else {
 				if(query.startsWith("/")) { //$NON-NLS-1$
 					pathWithoutLastSegment = "/" + name; //$NON-NLS-1$
 				} else {
 					pathWithoutLastSegment = name;
 				}
 			}
 			name = ""; //$NON-NLS-1$
 		}
 		IResource resource;
 		String startPath = pathWithoutLastSegment;
 		if(pathWithoutLastSegment.startsWith("/")) { //$NON-NLS-1$
 			if(pathWithoutLastSegment.length()>1) {
 				startPath = pathWithoutLastSegment.substring(1);
 			} else {
 				startPath = ""; //$NON-NLS-1$
 			}
 		}
 		if(query.startsWith("/")) { //$NON-NLS-1$
 			resource = webRootResource.findMember(startPath);
 		} else {
 			resource = context.getResource().getParent().findMember(startPath);
 		}
 
 		List<IResource> resources = new ArrayList<IResource>();
 		try {
 			if(resource != null) resource.accept(new ResourceFinder(resources, name, extensions));
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		ResourcePathDescriptor[] filesPathes = new ResourcePathDescriptor[resources.size()];
 		for(int i=0; i<filesPathes.length; i++) {
 			String prefix = pathWithoutLastSegment.toString();
 			if(!prefix.endsWith("/")) { //$NON-NLS-1$
 				prefix = prefix + '/';
 			}
 			IResource r = (IResource)resources.get(i);
 			filesPathes[i] = new ResourcePathDescriptor(prefix + r.getName(), r);
 		}
 		return filesPathes;
 	}
 
 	private static class ResourcePathDescriptor {
 		private String queryPath;
 		private IResource resource;
 
 		public ResourcePathDescriptor(String queryPath, IResource resource) {
 			this.queryPath = queryPath;
 			this.resource = resource;
 		}
 
 		public String getQueryPath() {
 			return queryPath;
 		}
 
 		public IResource getResource() {
 			return resource;
 		}
 	}
 
 	private static class ResourceFinder implements IResourceVisitor {
 		private List<IResource> resources;
 		private int count = 0;
 		private String name;
 		Set<String> extensions;
 
 		/**
 		 * @param resources
 		 * @param name
 		 * @param extensions
 		 */
 		public ResourceFinder(List<IResource> resources, String name, Set<String> extensions) {
 			this.resources = resources;
 			this.name = name;
 			this.extensions = extensions;
 		}
 
 		boolean acceptExtension(String ext) {
 			if(ext != null) {
 		        ext = ext.toLowerCase();
 			}
 			return extensions == null || extensions.isEmpty() || extensions.contains(ext);
 		}
 
 		public boolean visit(IResource resource) throws CoreException {
 			if(resource instanceof IFile) {
 				IFile file = (IFile)resource;
 				if(resource.getName().startsWith(name) && acceptExtension(file.getFileExtension())) {
 					resources.add(resource);
 				}
 			} else if(resource instanceof IFolder) {
 				if(count==0) {
 					count++;
 					return true;
 				} else if(resource.getName().startsWith(name) && (!resource.getName().equals("WEB-INF")) && (!resource.getName().equals("META-INF"))) { //$NON-NLS-1$ //$NON-NLS-2$
 					resources.add(resource);
 				}
 			}
 			return false;
 		}
 	}
 }
