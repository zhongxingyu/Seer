 /*******************************************************************************
   * Copyright (c) 2009 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.kb.refactoring;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
 import org.jboss.tools.common.el.core.ELCorePlugin;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELMethodInvocation;
 import org.jboss.tools.common.el.core.model.ELObject;
 import org.jboss.tools.common.el.core.model.ELPropertyInvocation;
 import org.jboss.tools.common.el.core.resolver.ELCompletionEngine;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolution;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.common.el.core.resolver.ElVarSearcher;
 import org.jboss.tools.common.el.core.resolver.IRelevanceCheck;
 import org.jboss.tools.common.el.core.resolver.SimpleELContext;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.model.project.ProjectHome;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.util.BeanUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 
 public abstract class RefactorSearcher {
 	protected static final String JAVA_EXT = "java"; //$NON-NLS-1$
 	protected static final String XML_EXT = "xml"; //$NON-NLS-1$
 	protected static final String XHTML_EXT = "xhtml"; //$NON-NLS-1$
 	protected static final String JSP_EXT = "jsp"; //$NON-NLS-1$
 	protected static final String JSPX_EXT = "jspx"; //$NON-NLS-1$
 	protected static final String PROPERTIES_EXT = "properties"; //$NON-NLS-1$
 	
 //	private static final String GET = "get"; //$NON-NLS-1$
 //	private static final String SET = "set"; //$NON-NLS-1$
 //	private static final String IS = "is"; //$NON-NLS-1$
 	
 	protected static final String SEAM_PROPERTIES_FILE = "seam.properties"; //$NON-NLS-1$
 	
 	protected IFile baseFile;
 	protected String propertyName;
 	protected IJavaElement javaElement;
 	protected IJavaSearchScope searchScope;
 	
 	public RefactorSearcher(IFile baseFile, String propertyName){
 		this.baseFile = baseFile;
 		this.propertyName = propertyName;
 	}
 	
 	public RefactorSearcher(IFile baseFile, String propertyName, IJavaElement javaElement){
 		this(baseFile, propertyName);
 		this.javaElement = javaElement;
 	}
 	
 	public void setSearchScope(IJavaSearchScope searchScope){
 		this.searchScope = searchScope;
 	}
 	
 	private void scanProject(IProject project){
 		if(project == null || !project.exists()) return;
 		if(doneProjects.contains(project)) return;
 		
 		doneProjects.add(project);
 		
 		IProject[] referencingProject = project.getReferencingProjects();
 		for(IProject rProject: referencingProject){
 			scanProject(rProject);
 		}
 		
 		if(!containsInSearchScope(project))
 			return;
 		
 		updateEnvironment(project);
 		
 		IJavaProject javaProject = EclipseResourceUtil.getJavaProject(project);
 		
 		// searching java, xml and property files in source folders
 		if(javaProject != null){
 			for(IResource resource : EclipseResourceUtil.getJavaSourceRoots(project)){
 				if(resource instanceof IFolder)
 					if(!scanForJava((IFolder) resource)){
 						outOfSynch(((IFolder) resource).getProject());
 						return;
 					}
 				else if(resource instanceof IFile)
 					if(!scanForJava((IFile) resource)){
 						outOfSynch(((IFile) resource).getProject());
 						return;
 					}
 			}
 		}
 		
 		// searching jsp, xhtml and xml files in WebContent folders
 		
 		if(getViewFolder(project) != null){
 			if(!scan(getViewFolder(project))){
 				outOfSynch(project);
 				return;
 			}
 		}else{
 			if(!scan(project)){
 				outOfSynch(project);
 				return;
 			}
 		}
 	}
 	
 	private HashSet<IProject> doneProjects = new HashSet<IProject>();
 
 	public final void findELReferences(){
 		if(baseFile == null)
 			return;
 		
 		doneProjects.clear();
 		
 		//startStatistics();
 		
 		IProject[] projects = getProjects();
 		for (IProject project : projects) {
 			scanProject(project);
 		}
 		//stopStatistic();
 	}
 	
 	protected void updateEnvironment(IProject project){
 		
 	}
 	
 	protected abstract IProject[] getProjects();
 	
 	protected IContainer getViewFolder(IProject project) {
 		IPath path = ProjectHome.getFirstWebContentPath(project);
 		
 		if(path != null)
 			return path.segmentCount() > 1 ? project.getFolder(path.removeFirstSegments(1)) : project;
 		
 		return null;
 	}
 	
 	private boolean scanForJava(IContainer container){
 		if(container.getName().startsWith(".")) //$NON-NLS-1$
 			return true;
 		
 		try{
 			for(IResource resource : container.members()){
 				if(resource instanceof IFolder){
 					if(!scanForJava((IFolder) resource))
 						return false;
 				}else if(resource instanceof IFile){
 					if(!scanForJava((IFile) resource))
 						return false;
 				}
 			}
 		}catch(CoreException ex){
 			ELCorePlugin.getDefault().logError(ex);
 		}
 		return true;
 	}
 
 	private boolean scan(IContainer container){
 		if(container.getName().startsWith(".")) //$NON-NLS-1$
 			return true;
 
 		try{
 			for(IResource resource : container.members()){
 				if(resource instanceof IFolder){
 					if(!scan((IFolder) resource))
 						return false;
 				}else if(resource instanceof IFile){
 					if(!scan((IFile) resource))
 						return false;
 				}
 			}
 		}catch(CoreException ex){
 			ELCorePlugin.getDefault().logError(ex);
 		}
 		return true;
 	}
 	
 	private String getFileContent(IFile file){
 		try {
 			return FileUtil.readStream(file);
 			//collectStatistic(content.length());
 		} catch (CoreException e) {
 			ELCorePlugin.getDefault().logError(e);
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param file
 	 * @return
 	 * true - in order to continue searching
 	 * false - in order to stop searching
 	 */
 	private boolean scanForJava(IFile file){
 		if(isFilePhantom(file))
 			return true;
 		
 		if(isFileOutOfSynch(file))
 			return false;
 		
 		if(PROPERTIES_EXT.equalsIgnoreCase(file.getFileExtension())){
 			if(file.getName().equals(SEAM_PROPERTIES_FILE)){
 				String content = getFileContent(file);
 				scanProperties(file, content);
 			}else
 				searchInCach(file);
 		} else if (JAVA_EXT.equalsIgnoreCase(file.getFileExtension())
 				|| JSP_EXT.equalsIgnoreCase(file.getFileExtension())
 				|| JSPX_EXT.equalsIgnoreCase(file.getFileExtension())
 				|| XHTML_EXT.equalsIgnoreCase(file.getFileExtension())
 				|| XML_EXT.equalsIgnoreCase(file.getFileExtension())) {
 			searchInCach(file);
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param file
 	 * @return
 	 * true - in order to continue searching
 	 * false - in order to stop searching
 	 */
 	private boolean scan(IFile file){
 		if(isFilePhantom(file))
 			return true;
 		
 		if(isFileOutOfSynch(file))
 			return false;
 
 		String ext = file.getFileExtension();			
 		if(XML_EXT.equalsIgnoreCase(ext) 
 			|| XHTML_EXT.equalsIgnoreCase(ext) 
 			|| JSP_EXT.equalsIgnoreCase(ext)
 			|| JSPX_EXT.equalsIgnoreCase(ext)) {
 			searchInCach(file);
 		}
 		return true;
 	}
 	
 	private void resolveByResolvers(ELExpression operand, ELResolver[] resolvers, ELContext context, IRelevanceCheck[] checks, int offset, List<MatchArea> areas, IFile file){
 		for (int i = 0; i < resolvers.length; i++) {
 			ELResolver resolver = resolvers[i];
 			if (!checks[i].isRelevant(operand.getText())) 
 				continue;
 			
 			ELResolution resolution = resolver.resolve(context, operand, offset);
 			
 			if(resolution != null) {
 				List<ELSegment> segments = resolution.findSegmentsByJavaElement(javaElement);
 				for(ELSegment segment : segments){
 					int o = offset+segment.getSourceReference().getStartPosition();
 					int l = segment.getSourceReference().getLength();
 					
 					if(!contains(areas, o, l)){
 						match(file, o, l);
 						areas.add(new MatchArea(o, l));
 					}
 				}
 			}
 		}
 	}
 	
 	protected void searchInCach(IFile file){
		if(file == null || !file.isAccessible() || file.isDerived(IResource.CHECK_ANCESTORS)) {
			return;
		}
 		ELResolver[] resolvers = ELResolverFactoryManager.getInstance().getResolvers(file);
 		
 		ELContext context = PageContextFactory.createPageContext(file);
 		
 		if(context == null)
 			return;
 		
 		ELReference[] references = context.getELReferences();
 
 		if(javaElement != null){
 			resolvers = context.getElResolvers();
 			IRelevanceCheck[] checks = getRelevanceChecks(resolvers);
 			List<MatchArea> areas = new ArrayList<MatchArea>();
 			for(ELReference reference : references){
 				int offset = reference.getStartPosition();
 				for(ELExpression operand : reference.getEl()){
 					resolveByResolvers(operand, resolvers, context, checks, offset, areas, file);
 					
 					for(ELObject child : operand.getChildren()){
 						if(child instanceof ELExpression){
 							resolveByResolvers((ELExpression)child, resolvers, context, checks, offset, areas, file);
 						}
 					}
 				}
 			}
 		} else {
 			for(ELReference reference : references){
 				int offset = reference.getStartPosition();
 				ELExpression[] expressions = reference.getEl();
 				for(ELExpression operand : expressions){
 					if(operand instanceof ELInvocationExpression){
 						ELInvocationExpression expression = findComponentReference((ELInvocationExpression)operand);
 						if(expression != null){
 							checkMatch(file, expression, offset+getOffset(expression), getLength(expression));
 						}
 					}
 					for(ELObject child : operand.getChildren()){
 						if(child instanceof ELInvocationExpression){
 							ELInvocationExpression expression = findComponentReference((ELInvocationExpression)child);
 							if(expression != null){
 								checkMatch(file, expression, offset+getOffset(expression), getLength(expression));
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	class MatchArea{
 		int offset;
 		int length;
 		
 		public MatchArea(int offset, int length){
 			this.offset = offset;
 			this.length = length;
 		}
 	}
 	
 	private boolean contains(List<MatchArea> list, int offset, int length){
 		for(MatchArea area : list){
 			if(area.offset == offset && area.length == length)
 				return true;
 		}
 		return false;
 	}
 
 	protected IRelevanceCheck[] getRelevanceChecks(ELResolver[] resolvers) {
 		IRelevanceCheck[] checks = new IRelevanceCheck[resolvers.length];
 		for (int i = 0; i < checks.length; i++) {
 			checks[i] = resolvers[i].createRelevanceCheck(javaElement);
 		}
 		return checks;
 	}
 
 	protected int getOffset(ELInvocationExpression expression){
 		if(expression instanceof ELPropertyInvocation){
 			ELPropertyInvocation pi = (ELPropertyInvocation)expression;
 			
 			if(pi.getName() != null)
 				return pi.getName().getStart();
 		}else if(expression instanceof ELMethodInvocation){
 			ELMethodInvocation mi = (ELMethodInvocation)expression;
 			
 			if(mi.getName() != null)
 				return mi.getName().getStart();
 		}
 		return 0;
 	}
 	
 	private int getLength(ELInvocationExpression expression){
 		if(expression instanceof ELPropertyInvocation){
 			ELPropertyInvocation pi = (ELPropertyInvocation)expression;
 			
 			if(pi.getName() != null)
 				return pi.getName().getLength();
 		}else if(expression instanceof ELMethodInvocation){
 			ELMethodInvocation mi = (ELMethodInvocation)expression;
 			
 			if(mi.getName() != null)
 				return mi.getName().getLength();
 		}
 		return 0;
 	}
 	
 	private void scanProperties(IFile file, String content){
 		if(!file.getName().equals(SEAM_PROPERTIES_FILE))
 			return;
 		
 		StringTokenizer tokenizer = new StringTokenizer(content, "#= \t\r\n\f", true); //$NON-NLS-1$
 		
 		String lastToken = "\n"; //$NON-NLS-1$
 		int offset = 0;
 		boolean comment = false;
 		boolean key = true;
 		
 		while(tokenizer.hasMoreTokens()){
 			String token = tokenizer.nextToken("#= \t\r\n\f"); //$NON-NLS-1$
 			if(token.equals("\r")) //$NON-NLS-1$
 				token = "\n"; //$NON-NLS-1$
 			
 			if(token.equals("#") && lastToken.equals("\n")) //$NON-NLS-1$ //$NON-NLS-2$
 				comment = true;
 			else if(token.equals("\n") && comment) //$NON-NLS-1$
 				comment = false;
 			
 			if(!comment){
 				if(!token.equals("\n") && lastToken.equals("\n")) //$NON-NLS-1$ //$NON-NLS-2$
 					key = true;
 				else if(key && (token.equals("=") || token.equals(" "))) //$NON-NLS-1$ //$NON-NLS-2$
 					key = false;
 				
 				if(key && token.startsWith(propertyName)){
 					match(file, offset, token.length());
 				}
 			}
 			
 			lastToken = token;
 			offset += token.length();
 		}
 	}
 	
 	protected ELInvocationExpression findComponentReference(ELInvocationExpression invocationExpression){
 		return invocationExpression;
 	}
 	
 	protected boolean isFileOutOfSynch(IFile file){
 		return !file.isSynchronized(IResource.DEPTH_ZERO);
 	}
 	
 	protected boolean isFilePhantom(IFile file){
 		return file.isPhantom();
 	}
 	
 	protected boolean isFileReadOnly(IFile file){
 		return file.isReadOnly();
 	}
 	
 	protected abstract void outOfSynch(IProject file);
 	
 	protected abstract void match(IFile file, int offset, int length);
 	
 	protected void checkMatch(IFile file, ELExpression operand, int offset, int length){
 		if(javaElement != null && operand != null)
 			resolve(file, operand, offset-getOffset((ELInvocationExpression)operand));
 		else
 			match(file, offset, length);
 	}
 	
 	public static String getPropertyName(IMethod method, String methodName){
 		if (BeanUtil.isGetter(method) || BeanUtil.isSetter(method)) {
 			String propertyName = BeanUtil.getPropertyName(methodName);
 			if(propertyName != null) {
 				return propertyName;
 			}
 		}
 		return methodName;
 	}
 	
 	public static String getPropertyName(IType method, String className){
 		StringBuffer name = new StringBuffer(className);
 		if(name.length()<2 || Character.isLowerCase(name.charAt(1))) {
 			name.setCharAt(0, Character.toLowerCase(name.charAt(0)));
 		}
 		String propertyName = name.toString();
 		return propertyName;
 	}
 	
 	private boolean containsInSearchScope(IProject project){
 		if(searchScope == null)
 			return true;
 		IPath[] paths = searchScope.enclosingProjectsAndJars();
 		for(IPath path : paths){
 			if(path.equals(project.getFullPath()))
 				return true;
 		}
 		return false;
 	}
 
 	protected void resolve(IFile file, ELExpression operand, int offset) {
 		ELResolver[] resolvers = ELResolverFactoryManager.getInstance()
 				.getResolvers(file);
 
 		for (ELResolver resolver : resolvers) {
 			SimpleELContext context = new SimpleELContext();
 
 			context.setResource(file);
 			context.setElResolvers(resolvers);
 
 			List<Var> vars = ElVarSearcher.findAllVars(context, offset,
 					resolver);
 
 			context.setVars(vars);
 
 			ELResolution resolution = resolver.resolve(context, operand, offset);
 			if(resolution!=null) {
 				List<ELSegment> segments = resolution.findSegmentsByJavaElement(javaElement);
 				
 				for(ELSegment segment : segments){
 					match(file, offset+segment.getSourceReference().getStartPosition(), segment.getSourceReference().getLength());
 				}
 			}
 		}
 	}
 	// performance measure 
 //	private int totalSize = 0;
 //	private int filesNumber = 0;
 //	private long startTime = 0;
 //	private long stopTime = 0;
 //	private long startMem = 0;
 //	private long stopMem = 0;
 //	
 //	private boolean log = false;
 //	
 //	private void clearHistory(){
 //		totalSize = 0;
 //		filesNumber = 0;
 //		startTime = 0;
 //		stopTime = 0;
 //		startMem = 0;
 //		stopMem = 0;
 //	}
 //	
 //	private void startStatistics(){
 //		clearHistory();
 //		startTime = System.currentTimeMillis();
 //		startMem = Runtime.getRuntime().freeMemory();
 //	}
 //	
 //	private void stopStatistic(){
 //		stopTime = System.currentTimeMillis();
 //		stopMem = Runtime.getRuntime().freeMemory();
 //		printELSearchStatistics();
 //	}
 //	
 //	private void collectStatistic(int fileSize){
 //		filesNumber++;
 //		totalSize += fileSize;
 //	}
 //	
 //	private void printELSearchStatistics(){
 //		if(log){
 //			System.out.println("EL Search"); //$NON-NLS-1$
 //			System.out.println("Total files number: "+getFilesNumber()); //$NON-NLS-1$
 //			System.out.println("Total files size: "+getTotlalFilesSize()+" Mb"); //$NON-NLS-1$ $NON-NLS-2$
 //			System.out.println("Memory usage size: "+getTotlalMemorySize()+" Mb"); //$NON-NLS-1$ $NON-NLS-2$
 //			System.out.println("Free Memory size: "+getRestMemorySize()+" Mb"); //$NON-NLS-1$ $NON-NLS-2$
 //			System.out.println("Total time: "+getTotalTime()+" sec"); //$NON-NLS-1$ $NON-NLS-2$
 //		}
 //	}
 //	
 //	private double getTotlalFilesSize(){
 //		return (double)totalSize/(1024*1025);
 //	}
 //
 //	private double getTotlalMemorySize(){
 //		return (double)(startMem-stopMem)/(1024*1025);
 //	}
 //
 //	private double getRestMemorySize(){
 //		return (double)stopMem/(1024*1025);
 //	}
 //	
 //	private int getFilesNumber(){
 //		return filesNumber;
 //	}
 //	
 //	private double getTotalTime(){
 //		return (double)(stopTime - startTime)/1000;
 //	}
 
 }
