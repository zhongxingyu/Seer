 package org.eclipse.emf.refactor.metrics.ocl.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.refactor.metrics.generator.interfaces.INewMetricWizard;
 import org.eclipse.emf.refactor.metrics.generator.ui.MetricBasicDataWizardPage;
 import org.eclipse.emf.refactor.metrics.managers.MetricManager;
 import org.eclipse.emf.refactor.metrics.ocl.core.OCLMetricInfo;
 import org.eclipse.emf.refactor.metrics.ocl.managers.OCLGenerationManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 
 public class NewMetricWizardOCL extends Wizard implements INewWizard, INewMetricWizard {
 	
 	private final String WINDOW_TITLE = "New Metric";
 	private MetricBasicDataWizardPage basicDataPage;
 	private OCLDataWizardPage oclPage;
 	private String name, id, description, metamodel, context, jar;
 	private LinkedList<IProject> projects;
 	private IProject targetProject;
 	private String importPackage;
 
 	public NewMetricWizardOCL() { }
 	
 	public NewMetricWizardOCL(String metaModel, String contextType) {
 		metamodel = metaModel;
 		context = contextType;
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		initProjects();
 	}
 	
 	@Override
 	public void addPages() {
 		setWindowTitle(WINDOW_TITLE);
 		basicDataPage = new MetricBasicDataWizardPage();
 		if (metamodel != null && ! metamodel.isEmpty() 
 				&& context != null && ! context.isEmpty()) {
 			setMetamodelAndContext(metamodel, context);
 		}
 		addPage(basicDataPage);
 		oclPage = new OCLDataWizardPage();
 		addPage(oclPage);
 	}
 	
 	@Override
 	public boolean canFinish() {
 		return (basicDataPage.isPageComplete() && oclPage.isPageComplete());
 	}
 
 	@Override
 	public boolean performFinish() {
 		try{
 			getContainer().run(true, true, new IRunnableWithProgress(){
 				public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException{
 					OCLGenerationManager.getInstance();
 					OCLGenerationManager.createNewMetric(monitor, getMetricInfo(), targetProject);
 				}
 			});
 		}
 		catch(InvocationTargetException e){
 			e.printStackTrace();
 			return false;
 		}
 		catch(InterruptedException e){
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	private void initProjects(){
 		this.projects = new LinkedList<IProject>();
 		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (IProject project : allProjects) {
 			if (project.isOpen()) {
 				IProjectNature nature = null;
 				try {
 					nature = project.getNature("org.eclipse.pde.PluginNature");
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 				if (null != nature) 
 					this.projects.add(project);
 			}
 		}
 	}
 	
 	private OCLMetricInfo getMetricInfo() {
 		MetricManager.getInstance();
 		String proj = this.targetProject.getLocation().toString();
 		String expression = oclPage.getOCLExpression();
 		OCLMetricInfo info = new OCLMetricInfo(name, id, description, 
 				metamodel, context, proj, getJar(), importPackage, expression);				
 		return info;
 	}	
 	
 	private String getJar() {
 		return jar;
 	}
 	
 	public MetricBasicDataWizardPage getDataPage() {
 		return basicDataPage;
 	}
 	
 	public LinkedList<IProject> getProjects() {
 		return projects;
 	}
 	
 	public IProject getTargetProject() {
 		return targetProject;
 	}
 	
 	public String getMetamodel() {
 		return metamodel;
 	}
 
 	public String getContext() {
 		return context;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setMetamodel(String metamodel) {
 		this.metamodel = metamodel;
 	}
 
 	public void setContext(String context) {
 		this.context = context;
 	}
 
 	public void setTargetProject(String projectName){
 		for (IProject project : projects)
 			if (project.getName().equals(projectName))
 				this.targetProject = project;
 	}
 
 	public void setJar(String jar) {
 		this.jar = jar;
 	}
 	
 	public OCLDataWizardPage getOCLPage() {
 		return oclPage;
 	}
 
 	@Override
 	public int getPageNumbers() {
 		return 2;
 	}
 
 	@Override
 	public WizardPage getSecondPage() {
 		return this.oclPage;
 	}
 
 	@Override
 	public void setImportPackage(String importPackage) {
 		this.importPackage = importPackage;
 	}
 
 	@Override
 	public void updateSecondPage() { }
 	
 	@Override
 	public void setMetamodelAndContext(String metamodel, String contextType) {
 		basicDataPage.setMetamodel(metamodel);
 		basicDataPage.setContextType(contextType);
 	}
 
 }
