 /*
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen.transformer.transforms;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.CompilationSession;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.transformer.FixMovedExpr;
 import org.caltoopia.codegen.transformer.IrTransformer;
 import org.caltoopia.codegen.transformer.IrTransformer.IrPassTypes;
 import org.caltoopia.codegen.transformer.TransUtil;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAssign;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.codegen.transformer.analysis.IrVariablePlacementAnnotation.VarPlacement;
 import org.caltoopia.ir.AbstractActor;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.ActorInstance;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.Assign;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.ExternalActor;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Scope;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.util.IrFindSwitch;
 import org.caltoopia.ir.util.IrReplaceSwitch;
 import org.caltoopia.types.TypeSystem;
 import org.eclipse.emf.ecore.EObject;
 
 public class MoveInitValueExpr extends IrReplaceSwitch {
 	
 	private PrintStream serr = null; 
 	private CompilationSession session;
 
 	public MoveInitValueExpr(Node node, CompilationSession session, boolean errPrint) {
 		if(!errPrint) {
 			serr = new PrintStream(new OutputStream(){
 			    public void write(int b) {
 			        //NO-OP
 			    }
 			});
 		} else {
 			serr = System.err;
 		}
 		this.session = session;
 		this.doSwitch(node);
 	}
 	
 	private static boolean initValueToStatements(List<Declaration> declarations, Scope scope) {
 		int pos = 0;
 		if(declarations == null || scope == null)
 			return false;
 		for(Declaration d:declarations) {
 			if(d instanceof Variable) {
 				Variable var = (Variable) d;
 				if(var.getInitValue()!=null) {
 					Map<String,String> annotations = TransUtil.getAnnotationsMap(var);
 					if(annotations!=null) {
						//When init expression depends on in port var or is placed on heap 
 						//move to statement. Also any declarations that depend on previous
 						//moved statements need to be moved.
 						String varType = annotations.get(TransUtil.varAnn("VarType"));
 						String varPlacement = annotations.get(TransUtil.varAnn("VarPlacement"));
 						if((varType!=null && varType.equals(VarType.actionInitInDepVar.name())) ||
							(varPlacement!=null && varPlacement.equals(VarPlacement.heap.name()))) {
 							Assign assign = UtilIR.createAssign(pos, scope, var, var.getInitValue());
 							TransUtil.setAnnotation(assign.getTarget().getDeclaration(),IrTransformer.VARIABLE_ANNOTATION, 
 									"VarAssign",VarAssign.movedInitAssigned.name());
 							TransUtil.copyAnnotations(assign.getTarget(), var);
 							pos++;
 							var.setInitValue(null);
 							System.out.println("[MoveInitValueExpr] Moved " + var.getName() + ", " + var.getId() + " in " + scope.getId());
 						} else if(
 							new IrFindSwitch(var){
 								@Override
 								public VariableReference caseVariableReference(VariableReference var) {
 									Map<String,String> annotations = TransUtil.getAnnotationsMap(var.getDeclaration());
 									if(annotations!=null) {
 										String varAssign = annotations.get(TransUtil.varAnn("VarAssign"));
 										if(varAssign != null && varAssign.equals(VarAssign.movedInitAssigned.name())) {
 											foundInSwitch=true;
 										}
 									}
 									return var;
 								}
 								
 								@Override
 								public VariableExpression caseVariableExpression(VariableExpression var) {
 									Map<String,String> annotations = TransUtil.getAnnotationsMap(var.getVariable());
 									if(annotations!=null) {
 										String varAssign = annotations.get(TransUtil.varAnn("VarAssign"));
 										if(varAssign != null && varAssign.equals(VarAssign.movedInitAssigned.name())) {
 											foundInSwitch=true;
 										}
 									}
 									return var;
 								}
 							}.found()) {
 								Assign assign = UtilIR.createAssign(pos, scope, var, var.getInitValue());
 								TransUtil.setAnnotation(assign.getTarget().getDeclaration(),IrTransformer.VARIABLE_ANNOTATION, 
 										"VarAssign",VarAssign.movedInitAssigned.name());
 								TransUtil.copyAnnotations(assign.getTarget(), var);
 								pos++;
 								var.setInitValue(null);
 								System.out.println("[MoveInitValueExpr] Chain-moved " + var.getName() + ", " + var.getId() + " in " + scope.getId());
 						}
 					}
 				}
 			}
 		}
 		return pos>0;
 	}
 	
 	@Override
 	public Action caseAction(Action action) {
 		initValueToStatements(action.getDeclarations(),action);
 		return super.caseAction(action);
 	}
 	
 	@Override
 	public EObject caseLambdaExpression(LambdaExpression obj) {
 		/*
 		 * Lambda only have an expression and no block.
 		 * We might need to insert statements surrounding the expression.
 		 * Hence we set the expression to an anonymous proc expression
 		 * to obtain a Block were to place statements. 
 		 * (With a return of the original expression,
 		 * which is OK in C but not in CAL.)
 		 */
 		ProcExpression pe = null;
 		Block body = null;
 		if(obj.getBody() instanceof ProcExpression) {
 			pe = (ProcExpression) obj.getBody();
 			body = pe.getBody();
 		} else {
 			body = IrFactory.eINSTANCE.createBlock();
 			pe = IrFactory.eINSTANCE.createProcExpression();
 			pe.setBody(body);
 			pe.setContext(obj);
 			pe.setOuter(obj.getOuter());
 			pe.setId(Util.getDefinitionId());
 			pe.setType(TypeSystem.createTypeUndef()); //Normally a TypeProc, but this is a nameless parameterless ProcExpression
 			body.setId(Util.getDefinitionId());
 			body.setOuter(pe);
 			body.getDeclarations().addAll(obj.getDeclarations());
 			//The original expr as return stmt in temp variable (to allow for statements after the expression)
 			Variable ret = UtilIR.createVarDef(body, "_cal__ret__", ((TypeLambda)obj.getType()).getOutputType());
 			Assign assign = UtilIR.createAssign(body, ret, obj.getBody());
 			TransUtil.setAnnotation(assign.getTarget().getDeclaration(),IrTransformer.VARIABLE_ANNOTATION, 
 					"VarAssign",VarAssign.movedRetAssigned.name());
 			TransUtil.copyAnnotations(assign.getTarget(), obj.getBody());
 			body.getStatements().add(assign);
 			ReturnValue retstmt = IrFactory.eINSTANCE.createReturnValue();
 			retstmt.setValue(UtilIR.createExpression(body,ret));
 			retstmt.setId(Util.getDefinitionId());
 			body.getStatements().add(retstmt);
 
 			obj.setBody(pe);
 		}
     
 		initValueToStatements(obj.getDeclarations(),body);
 
 		return super.caseLambdaExpression(obj);
 	}
 	
 	@Override
 	public AbstractActor caseActor(Actor actor) {
 		/*
 		 * No actor constructor exist in IR, but we might need one to initialize variables etc
 		 * Use a first initializer action to handle actor construction
 		 */
 		Action actorConstructor = null;
 		boolean existing = false;
 		List<Action> init = actor.getInitializers();
 		if(init != null) {
 			for(Action i:init) {
 				if(i.getTag() != null && !i.getTag().isEmpty() && i.getTag().get(0).equals("CAL__actorConstructor__")) {
 					actorConstructor = i;
 					existing = true;
 				}
 			}
 		}
 		if(actorConstructor == null) {
 			actorConstructor = IrFactory.eINSTANCE.createAction();
 		}
 		//If we actually needed the constructor push it in first in the list
 		if(initValueToStatements(actorConstructor.getDeclarations(),actorConstructor) && !existing) {
 			actorConstructor.setId(Util.getDefinitionId());
 			actorConstructor.setOuter(actor);
 			actorConstructor.getTag().add("CAL__actorConstructor__");
 			actor.getInitializers().add(0, actorConstructor);
 		}
 		return super.caseActor(actor);
 	}
 	
 	@Override
 	public Statement caseBlock(Block block) {
 		initValueToStatements(block.getDeclarations(),block);
 		return super.caseBlock(block);
 	}
 
 	@Override
 	public AbstractActor caseNetwork(Network obj) {
 		AbstractActor ret = super.caseNetwork(obj);
 		String path = TransUtil.getPath(ret);
 		TransUtil.AnnotatePass(ret, IrPassTypes.MoveInitValueExpr, "0");
 		ActorDirectory.addTransformedActor(ret, null, path);
 
 		for(ActorInstance a : obj.getActors()) {
 			AbstractActor actor=null;
 			try {
 				System.out.println("[MoveInitValue] Read in actor instance '" + a.getName() + "' of class " + ((TypeActor) a.getType()).getName());
 				actor = (AbstractActor) ActorDirectory.findTransformedActor(a);
 			} catch (DirectoryException x) {
 				//OK, since likely external
 			}
 			if(actor!=null && !(actor instanceof ExternalActor)) {
 				actor = (AbstractActor) doSwitch(actor);
 				path = TransUtil.getPath(actor);
 				TransUtil.AnnotatePass(actor, IrPassTypes.MoveInitValueExpr, "0");
 				ActorDirectory.addTransformedActor(actor, a, path);
 			}
 		}
 		return ret;
 	}
 
 }
