 package org.eclipse.gmf.ecore.edit.policies;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.UnexecutableCommand;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 import org.eclipse.gmf.runtime.diagram.ui.commands.EtoolsProxyCommand;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
 import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.GetEditContextRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientReferenceRelationshipRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EcorePackage;
 
 import org.eclipse.emf.ocl.expressions.ExpressionsFactory;
 import org.eclipse.emf.ocl.expressions.VariableDeclaration;
 
 import org.eclipse.emf.ocl.helper.HelperUtil;
import org.eclipse.emf.ocl.helper.IOclHelper;
 
 import org.eclipse.emf.ocl.parser.EcoreEnvironment;
 import org.eclipse.emf.ocl.parser.EcoreEnvironmentFactory;
 import org.eclipse.emf.ocl.parser.Environment;
 import org.eclipse.emf.ocl.parser.EvaluationEnvironment;
 
 import org.eclipse.emf.ocl.query.Query;
 import org.eclipse.emf.ocl.query.QueryFactory;
 
 import org.eclipse.gmf.ecore.editor.EcoreDiagramEditorPlugin;
 
 /**
  * @generated
  */
 public class EcoreBaseItemSemanticEditPolicy extends SemanticEditPolicy {
 
 	/**
 	 * @generated
 	 */
 	protected Command getSemanticCommand(IEditCommandRequest request) {
 		IEditCommandRequest completedRequest = completeRequest(request);
 		Command semanticCommand = getSemanticCommandSwitch(completedRequest);
 		if (semanticCommand == null) {
 			return UnexecutableCommand.INSTANCE;
 		}
 		boolean shouldProceed = true;
 		if (completedRequest instanceof DestroyRequest) {
 			shouldProceed = shouldProceed((DestroyRequest) completedRequest);
 		}
 		if (shouldProceed) {
 			if (completedRequest instanceof DestroyRequest) {
 				ICommand deleteCommand = new DeleteCommand((View) getHost().getModel());
 				semanticCommand = semanticCommand.chain(new EtoolsProxyCommand(deleteCommand));
 			}
 			return semanticCommand;
 		}
 		return UnexecutableCommand.INSTANCE;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getSemanticCommandSwitch(IEditCommandRequest req) {
 		if (req instanceof CreateRelationshipRequest) {
 			return getCreateRelationshipCommand((CreateRelationshipRequest) req);
 		} else if (req instanceof CreateElementRequest) {
 			return getCreateCommand((CreateElementRequest) req);
 		} else if (req instanceof ConfigureRequest) {
 			return getConfigureCommand((ConfigureRequest) req);
 		} else if (req instanceof DestroyElementRequest) {
 			return getDestroyElementCommand((DestroyElementRequest) req);
 		} else if (req instanceof DestroyReferenceRequest) {
 			return getDestroyReferenceCommand((DestroyReferenceRequest) req);
 		} else if (req instanceof DuplicateElementsRequest) {
 			return getDuplicateCommand((DuplicateElementsRequest) req);
 		} else if (req instanceof GetEditContextRequest) {
 			return getEditContextCommand((GetEditContextRequest) req);
 		} else if (req instanceof MoveRequest) {
 			return getMoveCommand((MoveRequest) req);
 		} else if (req instanceof ReorientReferenceRelationshipRequest) {
 			return getReorientReferenceRelationshipCommand((ReorientReferenceRelationshipRequest) req);
 		} else if (req instanceof ReorientRelationshipRequest) {
 			return getReorientRelationshipCommand((ReorientRelationshipRequest) req);
 		} else if (req instanceof SetRequest) {
 			return getSetCommand((SetRequest) req);
 		}
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getConfigureCommand(ConfigureRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getCreateCommand(CreateElementRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getSetCommand(SetRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getEditContextCommand(GetEditContextRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getDestroyElementCommand(DestroyElementRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getDestroyReferenceCommand(DestroyReferenceRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getDuplicateCommand(DuplicateElementsRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getMoveCommand(MoveRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getReorientReferenceRelationshipCommand(ReorientReferenceRelationshipRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getReorientRelationshipCommand(ReorientRelationshipRequest req) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Command getMSLWrapper(ICommand cmd) {
 		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
 		CompositeTransactionalCommand modelCmd = new CompositeTransactionalCommand(editingDomain, cmd.getLabel());
 		modelCmd.compose(cmd);
 		return new EtoolsProxyCommand(modelCmd);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected EObject getSemanticElement() {
 		return ViewUtil.resolveSemanticElement((View) getHost().getModel());
 	}
 
 	/**
 	 * Finds container element for the new relationship of the specified type.
 	 * Default implementation goes up by containment hierarchy starting from
 	 * the specified element and returns the first element that is instance of
 	 * the specified container class.
 	 * 
 	 * @generated
 	 */
 	protected EObject getRelationshipContainer(EObject element, EClass containerClass, IElementType relationshipType) {
 		for (; element != null; element = element.eContainer()) {
 			if (containerClass.isSuperTypeOf(element.eClass())) {
 				return element;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @generated 
 	 */
 	protected static class LinkConstraints {
 
 		/**
 		 * @generated 
 		 */
 		public static final LinkConstraints EClassESuperTypes_3004 = new LinkConstraints(new LinkEndConstraint(null, //$NON-NLS-1$
 				EcorePackage.eINSTANCE.getEClass()), new LinkEndConstraint("self <> oppositeEnd and not oppositeEnd.eSuperTypes->includes(self) and not self.eAllSuperTypes->includes(oppositeEnd)", //$NON-NLS-1$
 				EcorePackage.eINSTANCE.getEClass()));
 
 		/**
 		 * @generated 
 		 */
 		private static final String OPPOSITE_END_VAR = "oppositeEnd"; //$NON-NLS-1$
 
 		/**
 		 * @generated 
 		 */
 		private VariableDeclaration varOppositeEndToSource;
 
 		/**
 		 * @generated 
 		 */
 		private VariableDeclaration varOppositeEndToTarget;
 
 		/**
 		 * @generated 
 		 */
 		private Query srcEndInv;
 
 		/**
 		 * @generated 
 		 */
 		private Query targetEndInv;
 
 		/**
 		 * @generated 
 		 */
 		public LinkConstraints(LinkEndConstraint sourceEnd, LinkEndConstraint targetEnd) {
 			if (sourceEnd != null && sourceEnd.context != null && sourceEnd.body != null) {
 				if (targetEnd != null && targetEnd.context != null) {
 					this.varOppositeEndToTarget = createVar(OPPOSITE_END_VAR, targetEnd.context);
 				}
 				this.srcEndInv = createQuery(sourceEnd, varOppositeEndToTarget);
 			}
 
 			if (targetEnd != null && targetEnd.context != null && targetEnd.body != null) {
 				if (sourceEnd != null && sourceEnd.context != null) {
 					this.varOppositeEndToSource = createVar(OPPOSITE_END_VAR, sourceEnd.context);
 				}
 				this.targetEndInv = createQuery(targetEnd, varOppositeEndToSource);
 			}
 		}
 
 		/**
 		 * @generated 
 		 */
 		public boolean canCreateLink(CreateRelationshipRequest req, boolean isBackDirected) {
 			Object source = req.getSource();
 			Object target = req.getTarget();
 
 			Query sourceConstraint = isBackDirected ? targetEndInv : srcEndInv;
 			Query targetConstraint = null;
 			if (req.getTarget() != null) {
 				targetConstraint = isBackDirected ? srcEndInv : targetEndInv;
 			}
 			boolean isSourceAccepted = sourceConstraint != null ? evaluate(sourceConstraint, source, target, false) : true;
 			if (isSourceAccepted && targetConstraint != null) {
 				return evaluate(targetConstraint, target, source, true);
 			}
 			return isSourceAccepted;
 		}
 
 		/**
 		 * @generated 
 		 */
 		private Query createQuery(LinkEndConstraint constraint, VariableDeclaration oppositeEndDecl) {
 			final VariableDeclaration oppositeEndDeclLocal = oppositeEndDecl;
 			try {
				IOclHelper oclHelper = HelperUtil.createOclHelper(new EcoreEnvironmentFactory() {
 
 					public Environment createClassifierContext(Object context) {
 						Environment env = super.createClassifierContext(context);
 						if (oppositeEndDeclLocal != null) {
 							env.addElement(oppositeEndDeclLocal.getName(), oppositeEndDeclLocal, true);
 						}
 						return env;
 					}
 				});
 				oclHelper.setContext(constraint.context);
 				return QueryFactory.eINSTANCE.createQuery(oclHelper.createInvariant(constraint.body));
 			} catch (Exception e) {
 				EcoreDiagramEditorPlugin.getInstance().logError(null, e);
 				return null;
 			}
 		}
 
 		/**
 		 * @generated 
 		 */
 		private static boolean evaluate(Query query, Object sourceEnd, Object oppositeEnd, boolean clearEnv) {
 			EvaluationEnvironment evalEnv = query.getEvaluationEnvironment();
 			evalEnv.replace(OPPOSITE_END_VAR, oppositeEnd);
 			try {
 				Object val = query.evaluate(sourceEnd);
 				return (val instanceof Boolean) ? ((Boolean) val).booleanValue() : false;
 			} catch (Exception e) {
 				EcoreDiagramEditorPlugin.getInstance().logError(null, e);
 				if (evalEnv != null)
 					evalEnv.clear();
 				return false;
 			} finally {
 				if (clearEnv)
 					evalEnv.clear();
 			}
 		}
 
 		/**
 		 * @generated 
 		 */
 		private static VariableDeclaration createVar(String name, EClassifier type) {
 			VariableDeclaration var = ExpressionsFactory.eINSTANCE.createVariableDeclaration();
 			var.setName(name);
 			var.setType(EcoreEnvironment.getOclType(type));
 			return var;
 		}
 	}
 
 	/**
 	 * @generated 
 	 */
 	static class LinkEndConstraint {
 
 		final EClassifier context;
 
 		final String body;
 
 		LinkEndConstraint(String body, EClassifier context) {
 			this.context = context;
 			this.body = body;
 		}
 	}
 }
