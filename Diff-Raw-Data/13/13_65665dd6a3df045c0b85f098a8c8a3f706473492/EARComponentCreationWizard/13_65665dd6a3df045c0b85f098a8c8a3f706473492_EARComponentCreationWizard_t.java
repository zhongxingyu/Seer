 /*
  * Created on Mar 17, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jst.j2ee.internal.actions.IJ2EEUIContextIds;
 import org.eclipse.jst.j2ee.internal.earcreation.EarComponentCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPluginIcons;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
 
 public class EARComponentCreationWizard extends J2EEComponentCreationWizard implements IExecutableExtension, INewWizard {
 	
 	/**
 	 * <p>
 	 * The Wizard ID of the EARComponentCreationWizard. Used for internal purposes and activities management.
 	 * </p>
 	 */
 	public static final String WIZARD_ID =  EARComponentCreationWizard.class.getName();
 	protected static final String SECOND_PG = "second"; //$NON-NLS-1$
 	 
 	
 	/**
 	 * <p>
 	 * The default constructor. Creates a wizard with no selection, 
 	 * no model instance, and no operation instance. The model and 
 	 * operation will be created as needed.
 	 * </p>
 	 */
 	public EARComponentCreationWizard() {
 		super();
 	}
 
 	/**
 	 * <p>
 	 * The model is used to prepopulate the wizard controls
 	 * and interface with the operation.
 	 * </p>
 	 * @param model The model parameter is used to pre-populate wizard controls and interface with the operation
 	 */
 	public EARComponentCreationWizard(IDataModel model) {
 		super(model);
 	}
 	/** 
 	 * {@inheritDoc}   
 	 * 
 	 * <p>
 	 * Sets up the dialog window title and default page image. 
 	 * </p> 
 	 * 
 	 * @see J2EEArtifactCreationWizard#doInit()
 	 */
 	protected void doInit() {
 		setWindowTitle(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_COMPONENT_WIZ_TITLE));
 		setDefaultPageImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(J2EEUIPluginIcons.EAR_WIZ_BANNER));
 	} 
 	 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * <p>
 	 * Adds a {@link JCAProjectCreationPage} as the {@link J2EEComponentCreationWizard#MAIN_PG}.
 	 * </p>
 	 */
 	protected void doAddPages() {
 		EARComponentCreationWizardPage page1 = new EARComponentCreationWizardPage(getDataModel(), MAIN_PG);
		page1.setInfopopID(IJ2EEUIContextIds.NEW_EAR_COMP_PAGE); //$NON-NLS-1$
 		addPage(page1);
 		EARComponentCreationSecondPage page2 = new EARComponentCreationSecondPage(getDataModel(), SECOND_PG);
 		addPage(page2);
 		super.doAddPages();
 	}
 
     protected IDataModelProvider getDefaultProvider() {
         return new EarComponentCreationDataModelProvider();
         
     } 
 }
