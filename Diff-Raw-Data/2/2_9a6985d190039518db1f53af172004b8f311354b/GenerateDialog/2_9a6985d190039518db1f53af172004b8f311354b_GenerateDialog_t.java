 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.tools.common.generation.ui;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 import org.savara.common.logging.DefaultFeedbackHandler;
 import org.savara.common.logging.FeedbackHandler;
 import org.savara.contract.model.Contract;
 import org.savara.protocol.contract.generator.ContractGenerator;
 import org.savara.protocol.contract.generator.ContractGeneratorFactory;
 import org.savara.protocol.util.JournalProxy;
 import org.savara.protocol.util.ProtocolServices;
 //import org.savara.tools.common.generation.Generator;
 import org.savara.tools.common.ArtifactType;
 import org.savara.tools.common.generation.Generator;
 import org.savara.tools.common.logging.FeedbackHandlerDialog;
 import org.scribble.common.resource.Content;
 import org.scribble.common.resource.FileContent;
 import org.scribble.protocol.model.*;
 
 /**
  * This class provides the dialog for generating BPEL
  * artefacts.
  */
 public class GenerateDialog extends org.eclipse.jface.dialogs.Dialog {
 
 	private static Logger logger = Logger.getLogger(GenerateDialog.class.getName());
 
 	private IFile m_file=null;
 	private ProtocolModel m_protocolModel=null;
 	private ArtifactType m_artifactType=null;
 	private java.util.List<Button> m_roleButtons=new java.util.Vector<Button>();
 	private java.util.List<Text> m_projectNames=new java.util.Vector<Text>();
 	private java.util.List<Combo> m_roleArtifactTypes=new java.util.Vector<Combo>();
 	private java.util.List<Role> m_roles=new java.util.Vector<Role>();
 	
 	private static java.util.Map<String,Generator> m_generatorMap=new java.util.HashMap<String,Generator>();
 	private java.util.List<Generator> m_generators=null;
 	
 	static {
 		
 		try {
 			// Initialize list of generators
 			IExtensionRegistry registry = Platform.getExtensionRegistry();
 			IExtensionPoint point = registry.getExtensionPoint("org.savara.tools.common.generation.Generator");
 	
 			if (point != null) {
 				IExtension[] extensions = point.getExtensions();
 				
 				for (int i = 0; i < extensions.length; i++) {
 					for (int j=0; j < extensions[i].getConfigurationElements().length; j++) {
 						
 						if (extensions[i].getConfigurationElements()[j].getName().equals("generator")) {
 							IConfigurationElement elem=extensions[i].getConfigurationElements()[j];
 							
 							try {
 								Object am=elem.createExecutableExtension("class");	
 								
 								if (am instanceof Generator) {
 									GenerateDialog.addGenerator((Generator)am);
 								} else {
 									logger.severe("Failed to load generator: "+am);
 								}
 							} catch(Exception e) {
 								logger.log(Level.SEVERE, "Failed to load generator", e);
 							}
 						}
 					}
 				}
 			}
 		} catch(Throwable t) {
 			// Ignore classes not found, so can be used outside Eclipse
 		}
 	}	
 
 	/**
 	 * This is the constructor for the generate dialog.
 	 * 
 	 * @param shell The shell
 	 */
 	public GenerateDialog(Shell shell, IFile file, ArtifactType artifactType) {
 		super(shell);
 		
 		m_file = file;
 		m_artifactType = artifactType;
 		
 		initialize(m_file);
 	}
 	
 	public static void addGenerator(Generator generator) {
 		m_generatorMap.put(generator.getName(), generator);
 	}
 	
 	protected String getArtifactLabel() {
 		if (m_artifactType == ArtifactType.ServiceContract) {
 			return("Contract Type");
 		} else if (m_artifactType == ArtifactType.ServiceImplementation) {
 			return("Service Type");
 		}
 		return("Unknown");
 	}
 	
 	protected void initializeGeneratorList() {
 		m_generators = new java.util.Vector<Generator>();
 		for (Generator gen : m_generatorMap.values()) {
 			if (gen.getArtifactType() == m_artifactType) {
 				m_generators.add(gen);
 			}
 		}
 		
 		Collections.sort(m_generators, new Comparator<Generator>() {
 			public int compare(Generator arg0, Generator arg1) {
 				return(arg0.getName().compareTo(arg1.getName()));
 			}			
 		});
 	}
 	
 	/**
 	 * This method initializes the conversation model associated
 	 * with the supplied file resource.
 	 * 
 	 * @param res The file
 	 */
 	protected void initialize(IFile res) {
 		FeedbackHandler journal=new DefaultFeedbackHandler();
 		
 		try {
			Content content=new FileContent(res.getRawLocation().toFile());
 			
 			m_protocolModel = ProtocolServices.getParserManager().parse(content,
 							new JournalProxy(journal), null);
 			
 			if (m_protocolModel == null) {
 				logger.severe("Unable to load model");
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 			logger.log(Level.SEVERE, "Failed to parse model", e);
 		}
 	}
 	
 	/**
 	 * This method creates the dialog details.
 	 * 
 	 * @param parent The parent control
 	 * @return The control containing the dialog components
 	 */
 	protected Control createDialogArea(Composite parent) {
 		
 		initializeGeneratorList();
 		
 		Composite composite=(Composite)super.createDialogArea(parent);
 		
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		composite.setLayout(layout);		
 	
 		GridData gd=null;
 
 		Group group=new Group(composite, SWT.H_SCROLL|SWT.V_SCROLL);
 		
 		gd=new GridData();
 		gd.horizontalAlignment = SWT.FILL;
 		gd.horizontalSpan = 1;
 		gd.widthHint = (m_artifactType == ArtifactType.ServiceImplementation ? 680 : 380);
 		gd.grabExcessHorizontalSpace = true;
 		group.setLayoutData(gd);
 		
 		layout = new GridLayout();
 		layout.numColumns = (m_artifactType == ArtifactType.ServiceImplementation ? 6 : 4);
 		group.setLayout(layout);
 
 		// Labels
 		Label label=new Label(group, SWT.NONE);
 		label.setText("Service Role");
 		
 		gd = new GridData();
 		gd.horizontalSpan = 2;
 		gd.widthHint = 150;
 		label.setLayoutData(gd);
 
 		if (m_artifactType == ArtifactType.ServiceImplementation) {
 			label = new Label(group, SWT.NONE);
 			label.setText("Project Name");
 			
 			gd = new GridData();
 			gd.horizontalSpan = 2;
 			gd.widthHint = 300;
 			label.setLayoutData(gd);
 		}
 		
 		label = new Label(group, SWT.NONE);
 		label.setText(getArtifactLabel());
 		
 		gd = new GridData();
 		gd.horizontalSpan = 2;
 		gd.widthHint = 150;
 		label.setLayoutData(gd);
 
 		if (m_protocolModel != null) {
 			java.util.List<Role> roles=m_protocolModel.getProtocol().getRoles();
 
 			ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 			
 			for (int i=0; i < roles.size(); i++) {
 				Role role=roles.get(i);
 				boolean f_server=true;
 				
 				if (cg != null) {
 					Contract c=cg.generate(m_protocolModel.getProtocol(),
 							null, role, new DefaultFeedbackHandler());
 					
 					if (c != null && c.getInterfaces().size() == 0) {
 						f_server = false;
 						if (logger.isLoggable(Level.FINE)) {
 							logger.fine("Role "+role+" is not a service");
 						}
 					}
 				}
 
 				if (f_server) {
 					m_roles.add(role);
 					
 					Button button=new Button(group, SWT.CHECK);
 					button.setText(roles.get(i).getName());
 					button.setSelection(true);
 					
 					gd = new GridData();
 					gd.horizontalSpan = 2;
 					gd.widthHint = 195;
 					button.setLayoutData(gd);
 					
 					m_roleButtons.add(button);
 					
 					button.addSelectionListener(new SelectionListener() {
 						public void widgetDefaultSelected(SelectionEvent e) {
 							widgetSelected(e);
 						}
 	
 						public void widgetSelected(SelectionEvent e) {
 							checkStatus();
 						}
 					});
 					
 					if (m_artifactType == ArtifactType.ServiceImplementation) {
 						Text projectName=new Text(group, SWT.NONE);
 						
 						String prjName=roles.get(i).getName();
 						
 						if (m_protocolModel.getProtocol() != null) {
 							prjName = m_protocolModel.getProtocol().getName()+"-"+prjName;
 						}
 						
 						projectName.setText(prjName);
 						
 						gd = new GridData();
 						gd.horizontalSpan = 2;
 						gd.widthHint = 300;
 						projectName.setLayoutData(gd);
 						
 						m_projectNames.add(projectName);
 		
 						projectName.addModifyListener(new ModifyListener() {					
 							public void modifyText(ModifyEvent e) {
 								checkStatus();
 							}
 						});
 					}
 					
 					Combo genType=new Combo(group, SWT.NONE|SWT.READ_ONLY);
 					for (Generator gen : m_generators) {
 						genType.add(gen.getName());
 					}
 					if (m_generators.size() > 0) {
 						genType.select(0);
 					}
 					
 					gd = new GridData();
 					gd.horizontalSpan = 2;
 					gd.widthHint = 150;
 					genType.setLayoutData(gd);
 					
 					m_roleArtifactTypes.add(genType);
 				}
 			}
 		}
 
 		Button button=new Button(group, SWT.NONE);
 		button.setText("Check All");
 		
 		gd = new GridData();
 		gd.horizontalSpan = 1;
 		gd.widthHint = 100;
 		button.setLayoutData(gd);
 		
 		button.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);	
 			}
 			
 			public void widgetSelected(SelectionEvent e) {
 				for (int i=0; i < m_roleButtons.size(); i++) {
 					m_roleButtons.get(i).setSelection(true);
 				}
 				checkStatus();
 			}			
 		});
 
 		button=new Button(group, SWT.NONE);
 		button.setText("Clear All");
 		
 		gd = new GridData();
 		gd.horizontalSpan = 1;
 		gd.widthHint = 100;
 		button.setLayoutData(gd);
 		
 		button.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);	
 			}
 			
 			public void widgetSelected(SelectionEvent e) {
 				for (int i=0; i < m_roleButtons.size(); i++) {
 					m_roleButtons.get(i).setSelection(false);
 				}
 				checkStatus();
 			}			
 		});
 		
 		return(composite);
 	}
 
 	@Override
 	protected Control createButtonBar(Composite parent) {
 		Control ret=super.createButtonBar(parent);
 		
 		checkStatus();
 		
 		return(ret);
 	}
 	
 	protected void checkStatus() {
 		int selected=0;
 		boolean f_error=false;
 		
 		for (int i=0; i < m_roleButtons.size(); i++) {
 			if (m_roleButtons.get(i).getSelection()) {
 				selected++;
 					
 				if (m_artifactType == ArtifactType.ServiceImplementation) {
 					m_projectNames.get(i).setEnabled(true);
 					
 					// Check project name
 					String projectName=m_projectNames.get(i).getText();
 					
 					if (isProjectNameValid(projectName) == false) {
 						f_error = true;
 						
 						m_projectNames.get(i).setBackground(
 								Display.getCurrent().getSystemColor(SWT.COLOR_RED));
 					} else {
 						m_projectNames.get(i).setBackground(
 								Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					}
 				}
 
 				m_roleArtifactTypes.get(i).setEnabled(true);
 			} else {
 				if (m_artifactType == ArtifactType.ServiceImplementation) {
 					m_projectNames.get(i).setEnabled(false);
 					m_projectNames.get(i).setBackground(
 							Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 				}
 				m_roleArtifactTypes.get(i).setEnabled(false);
 			}
 		}
 		
 		if (f_error || selected == 0) {
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 		} else {
 			getButton(IDialogConstants.OK_ID).setEnabled(true);
 		}
 	}
 	
 	protected boolean isProjectNameValid(String name) {
 		boolean ret=true;
 		
 		if (name == null || name.trim().length() == 0) {
 			ret = false;
 		} else if (m_file.getWorkspace().getRoot().getProject(name).exists()) {
 			ret = false;
 		} else {
 			for (int i=0; ret && i < name.length(); i++) {
 				if (i == 0) {
 					ret = Character.isJavaIdentifierStart(name.charAt(i));
 				} else if ("-.".indexOf(name.charAt(i)) != -1) {
 					ret = true;
 				} else {
 					ret = Character.isJavaIdentifierPart(name.charAt(i));
 				}
 			}
 		}
 		
 		return(ret);
 	}
 	
 	/**
 	 * The ok button has been pressed.
 	 */
 	public void okPressed() {
 		
 		try {
 			FeedbackHandlerDialog journal=new FeedbackHandlerDialog(Display.getCurrent().getActiveShell());			
 			
 			for (int i=0; i < m_roles.size(); i++) {
 				
 				if (m_roleButtons.get(i).getSelection()) {
 					
 					// Get generator
 					Combo combo=m_roleArtifactTypes.get(i);
 					
 					int index=combo.getSelectionIndex();
 					
 					if (index >= 0) {
 						Generator generator=m_generators.get(index);
 						String projectName=null;
 						
 						if (m_projectNames.size() > 0) {
 							projectName = m_projectNames.get(i).getText();
 						}
 						
 						generator.generate(m_protocolModel, m_roles.get(i),
 								projectName, m_file, journal);
 					}
 				}
 			}
 			
 			journal.show();
 			
 			super.okPressed();
 		} catch(Exception e) {
 			error("Failed to generate artifacts", e);
 		}
 	}
 	
 	/**
 	 * This method is used to report an error.
 	 * 
 	 * @param mesg The error message
 	 * @param ex The exception 
 	 */
 	public void error(String mesg, Exception ex) {
 		
 		org.savara.tools.common.osgi.Activator.logError(mesg, ex);
 		
 		MessageBox mbox=new MessageBox(getShell(),
 				SWT.ICON_ERROR|SWT.OK);
 		mbox.setMessage(mesg);
 		mbox.open();
 		
 		logger.log(Level.SEVERE, mesg, ex);
 	}
 }
