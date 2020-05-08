 package com.github.kanafghan.welipse.webdsl.expressions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 
 import com.github.kanafghan.welipse.webdsl.ActualParameter;
 import com.github.kanafghan.welipse.webdsl.ClassifierOperation;
 import com.github.kanafghan.welipse.webdsl.Expression;
 import com.github.kanafghan.welipse.webdsl.ExternalLink;
 import com.github.kanafghan.welipse.webdsl.Image;
 import com.github.kanafghan.welipse.webdsl.Input;
 import com.github.kanafghan.welipse.webdsl.InternalLink;
 import com.github.kanafghan.welipse.webdsl.Page;
 import com.github.kanafghan.welipse.webdsl.PageElement;
 import com.github.kanafghan.welipse.webdsl.Parameter;
 import com.github.kanafghan.welipse.webdsl.PresentationElement;
 import com.github.kanafghan.welipse.webdsl.PropertyOperation;
 import com.github.kanafghan.welipse.webdsl.SelectionList;
 import com.github.kanafghan.welipse.webdsl.StructuralExp;
 import com.github.kanafghan.welipse.webdsl.Submit;
 import com.github.kanafghan.welipse.webdsl.Text;
 import com.github.kanafghan.welipse.webdsl.VariableDeclaration;
 import com.github.kanafghan.welipse.webdsl.VariableExp;
 import com.github.kanafghan.welipse.webdsl.VariableInitialization;
 import com.github.kanafghan.welipse.webdsl.WebDSLPackage;
 import com.github.kanafghan.welipse.webdsl.parsers.ExpressionsLanguage;
 
 public class ExpressionsAnalyzer {
 	
 	private EditingDomain editingDomain;
 	private Page page;
 	private EObject element;
 	private String expression;
 	
 	public ExpressionsAnalyzer(EditingDomain editingDomain, 
 			EObject element,
 			String expression) {
 		this.editingDomain = editingDomain;
 		this.element = element;
 		this.expression = expression;
 	}
 	
 	public ExpressionsAnalyzer(EditingDomain editingDomain, 
 			Page page, 
 			String expression) {
 		this.editingDomain = editingDomain;
 		this.page = page;
 		this.expression = expression;
 	}
 
 	public void analyzeInputExpressions(final boolean isValueExpression) {
 		String kind = isValueExpression ? "value" : "label";
 		final Job job = new Job("Analyzing input "+ kind +" expression: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the expression
 					Expression e = parser.getExpression();
 					if (e != null) {
 						// Initialize the expression
 						if (element instanceof Input) {
 							Input input = (Input) element;
 							e.initialize(getPage(input));
 						}
 						
 						// Create and execute the command
 						EClass eClass = element.eClass();
 					    EStructuralFeature f = null;
 					    if (isValueExpression) {
 					    	// Set the name of the input by deriving it from the expression
 					    	String inputName = "";
 					    	if (e instanceof StructuralExp) {
 					    		StructuralExp structuralExp = (StructuralExp) e;
 					    		inputName = structuralExp.getIdentifier();
 					    	} else if (e instanceof VariableExp) {
 					    		VariableExp variableExp = (VariableExp) e;
 					    		inputName = variableExp.getVar();
 					    	}
 					    	
 					    	f = eClass.getEStructuralFeature(WebDSLPackage.PAGE_ELEMENT__NAME);
 					    	editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, inputName));
 					    	
 					    	// Set f back to the input value feature in order to set it
 					    	f = eClass.getEStructuralFeature(WebDSLPackage.INPUT__VALUE);
 					    } else {
 					    	f = eClass.getEStructuralFeature(WebDSLPackage.INPUT__LABEL);
 					    }
 						
 						if (f != null) {											
 							editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, e));
 						}
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The expression '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();									
 	}
 	
 	public void analyzeSubmitExpressions(final boolean isPerformerExpression) {
 		String kind = isPerformerExpression ? "value" : "label";
 		final Job job = new Job("Analyzing submit "+ kind +" expression: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the expression
 					Expression e = parser.getExpression();
 					if (e != null) {
 						// Initialize the expression
 						if (element instanceof Submit) {
 							Submit submit = (Submit) element;
 							e.initialize(getPage(submit));
 						}
 						
 						// Create and execute the command
 						EClass eClass = element.eClass();
 					    EStructuralFeature f = null;
 					    if (isPerformerExpression) {
 					    	f = eClass.getEStructuralFeature(WebDSLPackage.SUBMIT__PERFORMER);
 					    } else {
 					    	f = eClass.getEStructuralFeature(WebDSLPackage.SUBMIT__VALIDATOR);
 					    }
 						
 						if (f != null) {											
 							editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, e));
 						}
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The expression '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();									
 	}	
 	
 	public void analyzeMainExpressions() {
 		final Job job = new Job("Analyzing expression: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the expression
 					Expression e = parser.getExpression();
 					if (e != null) {
 						// Initialize the expression
 						if (element instanceof PageElement) {
 							e.initialize(getPage((PageElement) element));
 						} else if (element instanceof ActualParameter) {
 							ActualParameter actualParameter = (ActualParameter) element;
 							InternalLink internalLink = (InternalLink) actualParameter.eContainer();
 							e.initialize(getPage(internalLink));
 						}
 						
 						// Type check the expression
 						EClassifier type = e.type();
 						
 						// Create and execute the command
 						EClass eClass = element.eClass();
 					    EStructuralFeature f = null;
 						if (element instanceof Text) {
 							f = eClass.getEStructuralFeature(WebDSLPackage.TEXT__CONTENT);
 						} else if (element instanceof Image) {
 							f = eClass.getEStructuralFeature(WebDSLPackage.IMAGE__SOURCE);
 						} else if (element instanceof com.github.kanafghan.welipse.webdsl.List) {
 							if (e instanceof PropertyOperation) {
 								ETypedElement elem = null;
 								if (e instanceof ClassifierOperation) {
 									elem = ((ClassifierOperation) e).getOperation();
 								} else if (e instanceof StructuralExp) {
 									elem = ((StructuralExp) e).getFeature();
 								}
 								
 								if (elem != null) {
 									if (!(elem.getUpperBound() > 1 || elem.getUpperBound() == -1)) {
 										return new Status(Status.ERROR, PLUGIN_ID,
 											"The expression '"+ expression +"' is not a valid expression for the "
 												+ "collection property of the list. It must result in a "
 												+ "list of elements of type "+ type.getName());
 									}
 								}
 							} else {
 								return new Status(Status.ERROR, PLUGIN_ID,
 										"The expression '"+ expression +"' is not a valid expression for the "
 											+ "collection property of the list. It must be either a "
 											+ "classifier operation or a structural expression.");
 							}
 							f = eClass.getEStructuralFeature(WebDSLPackage.LIST__COLLECTION);
 						} else if (element instanceof ExternalLink) {
 							f = eClass.getEStructuralFeature(WebDSLPackage.EXTERNAL_LINK__TARGET);
 						} else if (element instanceof Input) {
 							//TODO do something about this
 						} else if (element instanceof ActualParameter) {
 							ActualParameter actualParameter = (ActualParameter) element;
 							VariableExp variable;
 							
 							// The expression must be a variable expression
 							if (!(e instanceof VariableExp)) {
 								return new Status(Status.ERROR, PLUGIN_ID,
 										"The allowed expression for an actual parameter is a variable expression. "
 										+ "You have provided: "+ e.eClass().getName());
 							} else {
 								variable = (VariableExp) e;
 							}
 							
 							// Set the formal parameter
 							InternalLink internalLink = (InternalLink) actualParameter.eContainer();
 							PageElement targetElement = internalLink.getTarget();
 							if (targetElement != null) {
 								Page targetPage = getPage((PageElement) targetElement);
 								if (targetPage != null) {
 									EList<Parameter> parameters = targetPage.getParameters();
 									for (Parameter formalParameter : parameters) {
 										if (formalParameter.getVar().equals(actualParameter.getIdentifier())) {
 											// Types must also match
											if (formalParameter.getType().equals(variable.type())) {												
												actualParameter.setFormalParameter(formalParameter);
 											}
 										}
 									}
 									
 									if (actualParameter.getFormalParameter() == null) {
 										return new Status(Status.ERROR, PLUGIN_ID,
 												"The target page '"+ targetPage.getName() 
 												+"' of the internal link '"+ internalLink.getName() 
 												+"' does not have either a parameter with the name '"+ expression 
 												+"' or a parameter with the type '"+ variable.type().getName()
												+"'. Your model is incomplete at the moment");
 									}
 								} else {
 									return new Status(Status.ERROR, PLUGIN_ID,
 											"Could not found the target page of the link. Please define the target of the internal link '"
 											+ internalLink.getName() 
											+"' before defining its actual parameters. Your model is incomplete at the moment");
 								}
 								
 							} else {
 								return new Status(Status.ERROR, PLUGIN_ID,
 										"Please define the target of the internal link '"+ internalLink.getName() 
										+"' before defining its actual parameters. Your model is incomplete at the moment");
 							}
							
							f = eClass.getEStructuralFeature(WebDSLPackage.ACTUAL_PARAMETER__VALUE);
							
 						}
 						
 						if (f != null) {											
 							editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, e));
 						}
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The expression '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();							
 	}
 
 	//TODO this is a fix - we should do something about the context of initialization
 	protected Page getPage(PageElement pageElement) {
 		if (pageElement.getPage() != null) {
 			return pageElement.getPage();
 		}
 		return getPage((PageElement) pageElement.eContainer());
 	}
 
 	public void analyzeVariable() {
 		final Job job = new Job("Analyzing variable: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the parameter declaration
 					VariableInitialization var = parser.getVariable();
 					if (var != null) {
 						// Initialize the variable
 						var.initialize(page);
 						
 						EClassifier type = var.getInitExp().type();
 						if (type == null) {
 							return new Status(Status.ERROR, PLUGIN_ID,
 									"The type of the expresion  in the variable declaration '"
 											+ expression +"' is not defined.");
 						}
 						
 						if (!var.getType().getName().equals(type.getName())) {
 							return new Status(Status.ERROR, PLUGIN_ID,
 									"The type of the variable in '"+ expression +"' is different from the "
 											+ "expression initializing it. (variable type: "
 											+ var.getType().getName() +", expression type: "
 											+ type.getName());
 						}
 						
 						// Create and execute the command
 						EClass eClass = page.eClass();
 					    EStructuralFeature f = eClass.getEStructuralFeature(WebDSLPackage.PAGE__VARIABLES);
 					    editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, page, f, var));
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The variable declaration '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The variable declaration '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The variable declaration '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();
 	}
 
 	public void analyzeParameter() {
 		final Job job = new Job("Analyzing parameter: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the parameter declaration
 					Parameter param = parser.getParameter();
 					if (param != null) {
 						// Initialize the expression
 						param.initialize(page);
 						
 						// Create and execute the command
 						EClass eClass = page.eClass();
 					    EStructuralFeature f = eClass.getEStructuralFeature(WebDSLPackage.PAGE__PARAMETERS);
 					    editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, page, f, param));
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The parameter '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The parameter '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The parameter declaration '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();									
 	}
 
 	public void analyzeIteratorVariable() {
 		final Job job = new Job("Analyzing iterator variable: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the declaration (as a parameter)
 					Parameter iteratorVar = parser.getParameter();
 					if (iteratorVar != null) {
 						// Initialize the expression
 						if (element instanceof PageElement) {							
 							iteratorVar.initialize(getPage((PageElement) element));
 						}
 						
 						// Create and execute the command
 						EClass eClass = element.eClass();
 					    EStructuralFeature f = eClass.getEStructuralFeature(WebDSLPackage.LIST__ITERATOR_VARIABLE);
 					    editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, iteratorVar));
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The iterator variable '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The iterator variable '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The iterator variable declaration '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();
 	}
 	
 	public void analyzeSelectionListOptionsExpression() {
 		final Job job = new Job("Analyzing selection list options expression: "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					// parse the expression
 					Expression e = parser.getExpression();
 					if (e != null) {
 						// Initialize the expression
 						if (element instanceof SelectionList) {
 							SelectionList select = (SelectionList) element;
 							e.initialize(getPage(select));
 						}
 						
 						// Create and execute the command
 						EClass eClass = element.eClass();
 					    EStructuralFeature f = eClass.getEStructuralFeature(WebDSLPackage.SELECTION_LIST__OPTIONS);
 						editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, e));
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The expression '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();		
 	}	
 	
 	public void analyzeVariableDeclaration(final boolean isParameter) {
 		final Job job = new Job("Analyzing "+ (isParameter ? "Parameter" : "Variable") +": "+ expression) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setExpression(expression);
 				
 				try {
 					VariableDeclaration oldVar = (VariableDeclaration) element;
 					
 					// parse the variable declaration
 					VariableDeclaration newVar;
 					if (isParameter) {
 						newVar = parser.getParameter();
 					} else {
 						newVar = parser.getVariable();
 					}
 					
 					if (newVar != null) {
 						// Initialize the variable
 						Page containerPage = (Page) oldVar.eContainer();
 						newVar.initialize(containerPage);
 						
 						EClass eClass = oldVar.eClass();
 					    EStructuralFeature sfClassifier = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_DECLARATION__CLASSIFIER);
 					    EStructuralFeature sfType = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_DECLARATION__TYPE);
 					    EStructuralFeature sfVar = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_DECLARATION__VAR);
 					    
 					    // The list of commands that will set the features of the oldVar
 					    List<Command> commandList = new ArrayList<Command>();
 					    commandList.add(SetCommand.create(editingDomain, oldVar, sfClassifier, newVar.getClassifier()));
 					    commandList.add(SetCommand.create(editingDomain, oldVar, sfType, newVar.getType()));
 					    commandList.add(SetCommand.create(editingDomain, oldVar, sfVar, newVar.getVar()));
 						
 						if (!isParameter) {
 							VariableInitialization oldVarInit = (VariableInitialization) oldVar;
 							
 							VariableInitialization newVarInit = (VariableInitialization) newVar;
 							EClassifier type = newVarInit.getInitExp().type();
 							if (type == null) {
 								return new Status(Status.ERROR, PLUGIN_ID,
 										"The type of the expresion in the variable declaration '"
 												+ expression
 												+ "' is not defined.");
 							}
 							if (!newVar.getType().getName().equals(type.getName())) {
 								return new Status(
 										Status.ERROR,
 										PLUGIN_ID,
 										"The type of the variable in '"
 												+ expression
 												+ "' is different from the "
 												+ "expression initializing it. (variable type: "
 												+ newVarInit.getType().getName()
 												+ ", expression type: "
 												+ type.getName());
 							}
 							
 							EStructuralFeature sfInitExp = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_INITIALIZATION__INIT_EXP);
 							commandList.add(SetCommand.create(editingDomain, oldVarInit, sfInitExp, newVarInit.getInitExp()));
 						}
 						
 						// Execute the commands
 						CompoundCommand command = new CompoundCommand(commandList);
 					    editingDomain.getCommandStack().execute(command);
 					} else {
 						return new Status(Status.ERROR, PLUGIN_ID,
 								"The variable declaration '"+ expression +"' could not be parsed.");
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The variable declaration '"+ expression +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The variable declaration '"+ expression +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 	
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();
 	}	
 	
 	public void setExpression(String expression) {
 		this.expression = expression;
 	}
 
 	public void setEditingDomain(EditingDomain editingDomain) {
 		this.editingDomain = editingDomain;
 	}
 
 	public void setPage(Page page) {
 		this.page = page;
 	}
 
 	public void setElement(PresentationElement element) {
 		this.element = element;
 	}
 }
