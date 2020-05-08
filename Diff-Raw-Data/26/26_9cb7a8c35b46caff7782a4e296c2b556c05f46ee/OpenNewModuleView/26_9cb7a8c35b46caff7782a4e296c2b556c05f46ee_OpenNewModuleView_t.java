 // Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //    * Redistributions of source code must retain the above copyright
 //      notice, this list of conditions and the following disclaimer.
 //    * Redistributions in binary form must reproduce the above copyright
 //      notice, this list of conditions and the following disclaimer in the
 //      documentation and/or other materials provided with the distribution.
 //    * Neither the name of the <organization> nor the
 //      names of its contributors may be used to endorse or promote products
 //      derived from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
 // DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package wyclipse.actions;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.swt.widgets.Shell;
 
 import wyclipse.wizards.NewModuleWizard;
 
 public class OpenNewModuleView implements IWorkbenchWindowActionDelegate, IViewActionDelegate {
 	private Shell shell;
 	public static final String ID = "wyclipse.newModuleAction";
 
 	@Override
 	public void run(IAction action) {
 		NewModuleWizard wizard = new NewModuleWizard();		
         WizardDialog dialog = new WizardDialog(shell, wizard);
         dialog.create();
         dialog.open();
     }
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {}
 
 	@Override
 	public void dispose() {}
 
 	@Override
 	public void init(IWorkbenchWindow window) {
 		shell = window.getShell();
 	}
 	
 	@Override
 	public void init(IViewPart window) {
 		shell = window.getSite().getShell();
 	}
 }
