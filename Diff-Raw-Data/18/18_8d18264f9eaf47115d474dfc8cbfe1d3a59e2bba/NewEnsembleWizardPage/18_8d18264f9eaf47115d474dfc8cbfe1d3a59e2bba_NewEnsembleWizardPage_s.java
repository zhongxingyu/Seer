 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.navigator.wizards;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * page for the NewEnsembleWizard
  * 
  * @author Lars
  * 
  */
 public class NewEnsembleWizardPage extends WizardPage {
 	private Text name;
 	private Composite container;
 	private Text description;
 
 	public NewEnsembleWizardPage() {
 		super("Super First Page");
 		setTitle("New Ensemble");
 		setDescription("Choose a name for the new Ensemble");
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 2;
 		Label nameLabel = new Label(container, SWT.NULL);
 		nameLabel.setText("Ensemble Name");
 
 		name = new Text(container, SWT.BORDER | SWT.SINGLE);
 		name.setText("");
 		name.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (!name.getText().isEmpty()) {
 					setPageComplete(true);
 
 				}
 			}
 
 		});
 		
 		Label descriptionLabel = new Label(container, SWT.NULL);
 		descriptionLabel.setText("Ensemble Description");
 		descriptionLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		description = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
 		description.setText("");
 		description.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
				if (!description.getText().isEmpty()) {
 					setPageComplete(true);
 
				}
 			}
 
 		});
 		
 		
 		GridData nameGD = new GridData(GridData.FILL_HORIZONTAL);
 		name.setLayoutData(nameGD);
 		GridData descriptionGD = new GridData(GridData.FILL_HORIZONTAL);
 		descriptionGD.heightHint = 200;
 		description.setLayoutData(descriptionGD);
 		// Required to avoid an error in the system
 		setControl(container);
 		setPageComplete(false);
 
 	}
 
 	public String getEnsembleName() {
 		return name.getText();
 	}
 	
 	public String getEnsembleDescription(){
 		return description.getText();
 	}
 }
