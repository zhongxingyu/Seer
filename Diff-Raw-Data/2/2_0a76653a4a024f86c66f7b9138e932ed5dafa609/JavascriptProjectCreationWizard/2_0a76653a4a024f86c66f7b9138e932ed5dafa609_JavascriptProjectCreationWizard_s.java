 package org.eclipse.dltk.javascript.internal.ui.wizards;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.javascript.core.JavaScriptNature;
 import org.eclipse.dltk.javascript.internal.ui.JavaScriptImages;
 import org.eclipse.dltk.javascript.internal.ui.JavaScriptUI;
 import org.eclipse.dltk.javascript.internal.ui.preferences.JavascriptBuildPathsBlock;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.util.BusyIndicatorRunnableContext;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
 import org.eclipse.dltk.ui.wizards.NewElementWizard;
 import org.eclipse.dltk.ui.wizards.ProjectWizardFirstPage;
 import org.eclipse.dltk.ui.wizards.ProjectWizardSecondPage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
 
 public class JavascriptProjectCreationWizard extends NewElementWizard implements
 		INewWizard, IExecutableExtension {
 	private ProjectWizardFirstPage fFirstPage;
 	private ProjectWizardSecondPage fSecondPage;
 	private IConfigurationElement fConfigElement;
 
 	public JavascriptProjectCreationWizard() {
 		setDefaultPageImageDescriptor(JavaScriptImages.DESC_WIZBAN_PROJECT_CREATION);
 		setDialogSettings(DLTKUIPlugin.getDefault().getDialogSettings());
 		setWindowTitle(JavascriptWizardMessages.ProjectCreationWizard_title);
 	}
 
 	public void addPages() {
 		super.addPages();
 		fFirstPage = new ProjectWizardFirstPage() {
 
 			JavascriptInterpreterGroup fInterpreterGroup;
 
 			final class JavascriptInterpreterGroup extends
 					AbstractInterpreterGroup {
 
 				public JavascriptInterpreterGroup(Composite composite) {
 					super(composite);
 				}
 
 				protected String getCurrentLanguageNature() {
 					return JavaScriptNature.NATURE_ID;
 				}
 
 				protected void showInterpreterPreferencePage() {
 					// IPreferencePage page = new
 					// JavascriptInterpreterPreferencePage();
 					// DLTKDebugUIPlugin.showPreferencePage("org.eclipse.dltk.javascript.debug.ui.interpreters.JavascriptInterpreterPreferencePage",
 					// page);
 				}
 
 			};
 
 			protected void createInterpreterGroup(Composite parent) {
 				fInterpreterGroup = new JavascriptInterpreterGroup(parent);
 			}
 
			protected Observer getInterpreterGroupObservable() {
 				return fInterpreterGroup;
 			}
 
 			protected boolean supportInterpreter() {
 				return true;
 			}
 
 			protected IInterpreterInstall getInterpreter() {
 				return fInterpreterGroup.getSelectedJInterpreter();
 			}
 
 			protected void handlePossibleInterpreterChange() {
 				fInterpreterGroup.handlePossibleInterpreterChange();
 			}
 
 			protected boolean interpeterRequired() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 		};
 		fFirstPage
 				.setTitle(JavascriptWizardMessages.ProjectCreationWizardFirstPage_title);
 		fFirstPage
 				.setDescription(JavascriptWizardMessages.ProjectCreationWizardFirstPage_description);
 		addPage(fFirstPage);
 		fSecondPage = new ProjectWizardSecondPage(fFirstPage) {
 			protected BuildpathsBlock createBuildpathBlock(
 					IStatusChangeListener listener) {
 				return new JavascriptBuildPathsBlock(
 						new BusyIndicatorRunnableContext(), listener, 0,
 						useNewSourcePage(), null);
 			}
 
 			protected String getScriptNature() {
 				return JavaScriptNature.NATURE_ID;
 			}
 
 			protected IPreferenceStore getPreferenceStore() {
 				return JavaScriptUI.getDefault().getPreferenceStore();
 			}
 		};
 		addPage(fSecondPage);
 	}
 
 	protected void finishPage(IProgressMonitor monitor)
 			throws InterruptedException, CoreException {
 		fSecondPage.performFinish(monitor); // use the full progress monitor
 	}
 
 	public boolean performFinish() {
 		boolean res = super.performFinish();
 		if (res) {
 			BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
 			selectAndReveal(fSecondPage.getDLTKProject().getProject());
 		}
 		return res;
 	}
 
 	/*
 	 * Stores the configuration element for the wizard. The config element will
 	 * be used in <code>performFinish</code> to set the result perspective.
 	 */
 	public void setInitializationData(IConfigurationElement cfig,
 			String propertyName, Object data) {
 		fConfigElement = cfig;
 	}
 
 	public boolean performCancel() {
 		fSecondPage.performCancel();
 		return super.performCancel();
 	}
 
 	public IModelElement getCreatedElement() {
 		return DLTKCore.create(fFirstPage.getProjectHandle());
 	}
 }
