 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: ArtifactGenerator.java,v 1.18 2011/09/21 14:03:58 mtaal Exp $
  */
 package org.eclipse.emf.texo.generator;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.internal.xpand2.XpandTokens;
 import org.eclipse.internal.xpand2.ast.Definition;
 import org.eclipse.internal.xpand2.ast.ExpandStatement;
 import org.eclipse.internal.xpand2.ast.Template;
 import org.eclipse.internal.xpand2.parser.XpandParseFacade;
 import org.eclipse.internal.xtend.expression.ast.SyntaxElement;
 import org.eclipse.xpand2.XpandExecutionContextImpl;
 import org.eclipse.xpand2.output.NoChangesVetoStrategy;
 import org.eclipse.xpand2.output.Outlet;
 import org.eclipse.xpand2.output.OutputImpl;
 import org.eclipse.xpand2.output.PostProcessor;
 import org.eclipse.xtend.expression.ExceptionHandler;
 import org.eclipse.xtend.expression.ExecutionContext;
 import org.eclipse.xtend.expression.Variable;
 import org.eclipse.xtend.type.impl.java.JavaMetaModel;
 
 /**
  * Calls the XPand generator directly without using a workflow.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class ArtifactGenerator {
 
   static final String MODEL_CONTROLLER = "modelController"; //$NON-NLS-1$
   static final String DO_DAO = "doDao"; //$NON-NLS-1$
   private static final String FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
 
   private IProgressMonitor monitor = new org.eclipse.core.runtime.NullProgressMonitor();
   private TexoResourceManager resourceManager = ComponentProvider.getInstance().newInstance(TexoResourceManager.class);
   private String projectName;
  private String sourceProjectName;
   private ModelController modelController;
   private String outputFolder;
   private String expand = "org::eclipse::emf::texo::modelgenerator::templates::model::root(modelController, doDao) FOREACH modelController.EPackages"; //$NON-NLS-1$
   private FileCleaner fileCleaner = new FileCleaner();
 
   private List<Outlet> outlets;
 
   private boolean doDao;
 
   private BaseMainTemplate xTendTemplate;
 
   /**
    * Run the generation for the project and output location. Uses XPand to call the expand command (
    * {@link #getExpand()}). The output is generated in the output folder ({@link #getOutputFolder()}.
    * 
    * The outlets ({@link #getOutlets()} are used to format the output.
    */
   public void run() {
 
     initializeOutlets();
 
     try {
       final OutputImpl out = new OutputImpl();
       out.setAutomaticHyphens(false);
       for (final Outlet outlet : getOutlets()) {
         out.addOutlet(outlet);
       }
 
       // use an xtend template
       if (xTendTemplate != null) {
         xTendTemplate.setArtifactGenerator(this);
         for (EPackage ePackage : modelController.getEPackages()) {
           xTendTemplate.generate(ePackage, modelController, doDao);
 
           final Map<String, String> xTendResult = xTendTemplate.getFiles();
           for (String fileName : xTendResult.keySet()) {
             final String content = xTendResult.get(fileName);
             final boolean isJava = fileName.endsWith("java"); //$NON-NLS-1$
             out.openFile(fileName, isJava ? "java" : null); //$NON-NLS-1$
             out.write(content);
             out.closeFile();
           }
 
           xTendTemplate.clearFiles();
         }
 
         // execute the advices template
         final XPandTemplate xPandTemplate = new XPandTemplate();
         xPandTemplate.setArtifactGenerator(this);
         xPandTemplate.setXPandTemplate("org::eclipse::emf::texo::modelgenerator::templates::advices"); //$NON-NLS-1$
         xPandTemplate.setMainObject(getModelController());
         xPandTemplate.generate();
 
         final Map<String, String> xPandResult = xTendTemplate.getFiles();
         for (String fileName : xPandResult.keySet()) {
           final String content = xPandResult.get(fileName);
           final boolean isJava = fileName.endsWith("java"); //$NON-NLS-1$
           out.openFile(fileName, isJava ? "java" : null); //$NON-NLS-1$
           out.write(content);
           out.closeFile();
         }
 
       } else {
         final Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put(MODEL_CONTROLLER, modelController);
         parameters.put(DO_DAO, isDoDao());
 
         XpandExecutionContextImpl executionContext = new XpandExecutionContextImpl(resourceManager, out, null,
             new HashMap<String, Variable>(), null, getExceptionHandler(), null, null);
 
         executionContext.registerMetaModel(new JavaMetaModel());
 
         final ExpandStatement es = getStatement();
 
         for (final String name : parameters.keySet()) {
           executionContext = (XpandExecutionContextImpl) executionContext.cloneWithVariable(new Variable(name,
               parameters.get(name)));
         }
 
         executionContext.registerAdvices("org::eclipse::emf::texo::modelgenerator::templates::advices"); //$NON-NLS-1$
 
         es.evaluate(executionContext);
       }
 
       fileCleaner.clean();
 
     } catch (final Exception e) {
       throw new IllegalStateException(e.getMessage(), e);
     }
   }
 
   private ExpandStatement getStatement() {
     final Template tpl = XpandParseFacade.file(new StringReader(XpandTokens.LT + "DEFINE test FOR test" //$NON-NLS-1$
         + XpandTokens.RT + XpandTokens.LT + "EXPAND " + getExpand() + XpandTokens.RT + XpandTokens.LT //$NON-NLS-1$
         + "ENDDEFINE" + XpandTokens.RT), null); //$NON-NLS-1$
     return (ExpandStatement) ((Definition) tpl.getDefinitions()[0]).getBody()[1];
   }
 
   protected ExceptionHandler getExceptionHandler() {
     return new ExceptionHandler() {
       public void handleRuntimeException(RuntimeException ex, SyntaxElement element, ExecutionContext ctx,
           Map<String, Object> additionalContextInfo) {
         throw ex;
       }
     };
   }
 
   /**
    * @return the projectName
    */
   public String getProjectName() {
     return projectName;
   }
 
   /**
    * @param projectName
    *          the target projectName to set
    */
   public void setProjectName(final String projectName) {
     this.projectName = projectName;
   }
 
   /**
    * @param sourceProjectName
    *          the name of the source project containing the models and templates to set
    */
  public void setSourceProjectName(final String projectName) {
    this.sourceProjectName = projectName;
     resourceManager.setProjectName(projectName);
   }
 
   /**
    * Creates 2 outlets, one default and one for java. Both use the {@link NoChangesVetoStrategy} veto strategy, the java
    * outlet uses the {@link SourceMerger} as the {@link PostProcessor}. This PostProcessor merges sources, organizes
    * imports and formats the source according to the project settings.
    * 
    * The {@link FileCleaner#getNewOutputLocationTracker()} (a VetoStrategy) is used to keep track of which files are
    * being generated. This VetoStrategy is added to each of the 2 outlets.
    */
   protected void initializeOutlets() {
     if (outlets != null) {
       return;
     }
     outlets = new ArrayList<Outlet>();
     {
       final Outlet outlet = new Outlet();
       outlet.setFileEncoding(FILE_ENCODING);
       // make sure not to overwrite generated files
       // NOTE: apparently the vetoStrategies is a public member
       // the output location tracker needs to be first otherwise it won't
       // always be called
       outlet.addVetoStrategy(fileCleaner.getNewOutputLocationTracker());
       outlet.addVetoStrategy(new NoChangesVetoStrategy());
       outlet.setPath(EclipseGeneratorUtils.getProjectFilePath(getProjectName()) + "/" + outputFolder); //$NON-NLS-1$
 
       outlets.add(outlet);
     }
     {
       final Outlet outlet = new Outlet();
       outlet.setFileEncoding(FILE_ENCODING);
       outlet.setName("java"); //$NON-NLS-1$
 
       // make sure not to overwrite generated files
       // NOTE: apparently the vetoStrategies is a public member
       // the output location tracker needs to be first otherwise it won't
       // always be called
       outlet.addVetoStrategy(fileCleaner.getNewOutputLocationTracker());
       outlet.addVetoStrategy(new NoChangesVetoStrategy());
 
       final SourceMerger sourceMerger = new SourceMerger();
       sourceMerger.setProjectName(getProjectName());
       sourceMerger.setMonitor(monitor);
 
       outlet.addPostprocessor(sourceMerger);
 
       outlet.setPath(EclipseGeneratorUtils.getProjectFilePath(getProjectName()) + "/" + outputFolder); //$NON-NLS-1$
       outlets.add(outlet);
     }
   }
 
   /**
    * Return the outlets, if they are not yet defined then the {@link #initializeOutlets()} method is called.
    * 
    * @return the outlets used in the generation.
    */
   public final List<Outlet> getOutlets() {
     if (outlets == null) {
       initializeOutlets();
     }
     return outlets;
   }
 
   public ModelController getModelController() {
     return modelController;
   }
 
   public void setModelController(ModelController modelController) {
     this.modelController = modelController;
   }
 
   public String getOutputFolder() {
     return outputFolder;
   }
 
   public void setOutputFolder(String outputFolder) {
     this.outputFolder = outputFolder;
   }
 
   public IProgressMonitor getMonitor() {
     return monitor;
   }
 
   public void setMonitor(IProgressMonitor monitor) {
     this.monitor = monitor;
   }
 
   /**
    * @return the expand command, the default value is:
    *         org::eclipse::emf::texo::modelgenerator::templates::model::root(modelController) FOREACH
    *         modelController.EPackages
    */
   public String getExpand() {
     return expand;
   }
 
   public void setExpand(String expand) {
     this.expand = expand;
   }
 
   public void setOutlets(List<Outlet> outlets) {
     this.outlets = outlets;
   }
 
   public boolean isDoDao() {
     return doDao;
   }
 
   public void setDoDao(boolean doDao) {
     this.doDao = doDao;
   }
 
   public TexoResourceManager getResourceManager() {
     return resourceManager;
   }
 
   public void setResourceManager(TexoResourceManager resourceManager) {
     this.resourceManager = resourceManager;
   }
 
   public BaseMainTemplate getXTendTemplate() {
     return xTendTemplate;
   }
 
   public void setXTendTemplate(BaseMainTemplate xTendTemplate) {
     this.xTendTemplate = xTendTemplate;
   }
 }
