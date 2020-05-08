 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.wizards;
 
 import java.util.Observable;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.internal.debug.ui.interpreters.RubyInterpreterPreferencePage;
 import org.eclipse.dltk.ruby.internal.ui.RubyImages;
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.ruby.internal.ui.preferences.RubyBuildPathsBlock;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.util.BusyIndicatorRunnableContext;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
 import org.eclipse.dltk.ui.wizards.NewElementWizard;
 import org.eclipse.dltk.ui.wizards.ProjectWizardFirstPage;
 import org.eclipse.dltk.ui.wizards.ProjectWizardSecondPage;
 import org.eclipse.jface.preference.IPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
 
 public class RubyNewProjectWizard extends NewElementWizard implements INewWizard, IExecutableExtension {
 	public static final String WIZARD_ID = "org.eclipse.dltk.ruby.wizards.newproject";
 	
 	private ProjectWizardFirstPage fFirstPage;
 	private ProjectWizardSecondPage fSecondPage;
 	private IConfigurationElement fConfigElement;
 
 	public RubyNewProjectWizard() {
 		setDefaultPageImageDescriptor(RubyImages.DESC_WIZBAN_PROJECT_CREATION);
 		setDialogSettings(DLTKUIPlugin.getDefault().getDialogSettings());
 		setWindowTitle(RubyWizardMessages.NewProjectWizard_title);
 	}
 
 	public void addPages() {
 		super.addPages();
 		fFirstPage = new ProjectWizardFirstPage() {
 
 			RubyInterpreterGroup fInterpreterGroup;
         	
         	final class RubyInterpreterGroup extends AbstractInterpreterGroup {
         		
         		public RubyInterpreterGroup(Composite composite) {
         			super (composite);
         		}
 
 				protected String getCurrentLanguageNature() {
 					return RubyNature.NATURE_ID;
 				}
 
 				protected void showInterpreterPreferencePage() {
 					IPreferencePage page = new RubyInterpreterPreferencePage(); 
 					DLTKDebugUIPlugin.showPreferencePage("org.eclipse.dltk.ruby.debug.ui.interpreters.RubyInterpreterPreferencePage", page); 					
 				}
             	
             };
         	
 			protected void createInterpreterGroup(Composite parent) {
 				fInterpreterGroup = new RubyInterpreterGroup(parent);
 			}
 
 			protected Observable getInterpreterGroupObservable() {
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
 				return true;
 			}
 		};
 		
 		// First page
 		fFirstPage.setTitle(RubyWizardMessages.NewProjectFirstPage_title);
 		fFirstPage.setDescription(RubyWizardMessages.NewProjectFirstPage_description);
 		addPage(fFirstPage);
 
 		// Second page
 		fSecondPage = new ProjectWizardSecondPage(fFirstPage) {
 			protected BuildpathsBlock createBuildpathBlock(IStatusChangeListener listener) {
 				return new RubyBuildPathsBlock(new BusyIndicatorRunnableContext(), listener, 0, useNewSourcePage(), null);
 			}
 
 			protected String getScriptNature() {
 				return RubyNature.NATURE_ID;
 			}
 
 			protected IPreferenceStore getPreferenceStore() {
 				return RubyUI.getDefault().getPreferenceStore();
 			}
 		};
 		addPage(fSecondPage);
 	}
 
 	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
 		fSecondPage.performFinish(monitor); // use the full progress monitor
 	}
 
 	public boolean performFinish() {
 		boolean res = super.performFinish();
 		if (res) {
 			BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
 			selectAndReveal(fSecondPage.getScriptProject().getProject());
 		}
 		return res;
 	}
 	
 	/*
 	 * Stores the configuration element for the wizard. The config element will
 	 * be used in <code>performFinish</code> to set the result perspective.
 	 */
 	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
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
