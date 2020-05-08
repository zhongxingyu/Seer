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
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.CompilationSession;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.printer.CBuildBody;
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
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.BooleanLiteral;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.ExternalActor;
 import org.caltoopia.ir.FloatLiteral;
 import org.caltoopia.ir.ForEach;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.IfExpression;
 import org.caltoopia.ir.IfStatement;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Scope;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.StringLiteral;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.UnaryExpression;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.WhileLoop;
 import org.caltoopia.ir.util.IrFindSwitch;
 import org.caltoopia.ir.util.IrReplaceSwitch;
 import org.caltoopia.ir.util.IrSwitch;
 import org.caltoopia.types.TypeSystem;
 import org.eclipse.emf.ecore.EObject;
 
 public class CreateForLoop extends IrReplaceSwitch {
 	
 	private PrintStream serr = null; 
 	private CompilationSession session;
 
 	public CreateForLoop(Node node, CompilationSession session, boolean errPrint) {
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
 		if(node!=null) {
 		    this.doSwitch(node);
 		}
 	}
 	
     @Override
     public ForEach caseForEach(ForEach stmt) {
         /* 
          * At least currently the generator expressions can only be one of: 
          * VariableExpression of type List, 
          * ListExpression, 
          * BinaryExpression with ..-operator,
          * FunctionCall returning result of type List
          * Also the list must be exactly one-dimension more than the index variable type, typically one-dimensional.
          * ---------------------
          * This class converts all the generators into a while loops inside a block to enable setup and handle of helper variables
          * Hence after this conversion all the generators will be removed and the Body in ForEach will refer to the outer
          * most block containing a while loop.
          */
         //But first run thru the body it might have a separate foreach statement
         stmt.setBody((Block) doSwitch(stmt.getBody()));
         Block innerBody = stmt.getBody();
         Scope outer = innerBody.getOuter();
         while(outer instanceof Generator) {
             outer = outer.getOuter();
         }
         Statement access = null;
         int nesting = 0;
         for(int i = stmt.getGenerators().size()-1;i>=0;i--) {
             Generator g = stmt.getGenerators().get(i);
             if(g.getSource() instanceof VariableExpression) {
                 if(g.getDeclarations().get(0) instanceof Variable) {
                     Variable v = (Variable) g.getDeclarations().get(0);
                     int listDim=0;
                     Type t = ((Variable)v).getType();
                     while(t instanceof TypeList) {
                         listDim++;
                         t = ((TypeList)t).getType();
                     }
                     int listExprDim = 0;
                     t = g.getSource().getType();
                     while(t instanceof TypeList) {
                         listExprDim++;
                         t = ((TypeList)t).getType();
                     }
                     if(listExprDim != listDim+1) {
                         throw new RuntimeException("[CreateForLoop] Expecting foreach list variable to have same type and be of one more dimension then index variable.");
                     }
                     if(0 != listDim) {
                         throw new RuntimeException("[CreateForLoop] Not yet implemented foreach variable expressions to be more than one dimensional.");
                     }
 
                     Block body = UtilIR.createBlock(null);
                     innerBody.setOuter(body);
                     body.getDeclarations().add(v);
                     v.setScope(body);
                     //Create a variable that can be used to index into the list
                     TypeInt type = IrFactory.eINSTANCE.createTypeInt();
                     Declaration index = UtilIR.createVarDef(body, v.getName()+"__ListIndex", type, false, false, UtilIR.lit(0));
                     Expression sz = null;
                     if(g.getSource().getType() instanceof TypeList) {
                         sz = ((TypeList)g.getSource().getType()).getSize();
                     } else {
                         throw new RuntimeException("[CreateForLoop] Not yet implemented foreach variable expressions being lists of unknown length.");
                     }
                     //initialize;while(cond){access;innerBody;update;}
                     BinaryExpression cond = (BinaryExpression) UtilIR.createExpression(UtilIR.createExpression(body, (Variable) index), "<", sz);
                     //empty intialize
                     //increment index var
                     Statement update = UtilIR.createAssign(-1,innerBody, (Variable) index,
                             UtilIR.createExpression(UtilIR.createExpression(innerBody, (Variable) index), "+", UtilIR.lit(1)));
                     //statement to access list inserted first in nested/inner block
                     access = UtilIR.createAssign(0,innerBody, v, 
                             UtilIR.createExpression(body, (Variable)((VariableExpression)g.getSource()).getVariable(),
                                     Arrays.asList(UtilIR.createExpression(body, (Variable) index)),
                                     null));
                     //make while statement
                     WhileLoop whileloop = UtilIR.createWhileLoop(body, innerBody, cond);
                     innerBody = body;
                     nesting++;
                 }
             } else if(g.getSource() instanceof BinaryExpression) {
                 BinaryExpression expr = (BinaryExpression) g.getSource();
                 if(!expr.getOperator().equals("..")
                         ||!(expr.getOperand1().getType() instanceof TypeUint
                         ||expr.getOperand1().getType() instanceof TypeInt)
                         ||!(expr.getOperand2().getType() instanceof TypeUint
                         ||expr.getOperand2().getType() instanceof TypeInt)) {
                     throw new RuntimeException("[CreateForLoop] Expecting foreach expressions to have A .. B syntax, with A and B (u)int types.");
                 }
                 if(g.getDeclarations().get(0) instanceof Variable) {
                     Variable v = (Variable) g.getDeclarations().get(0);
                     Block body = UtilIR.createBlock(null);
                     innerBody.setOuter(body);
                     body.getDeclarations().add(v);
                     v.setScope(body);
                     Expression sz = expr.getOperand2();
                     //initialize;while(cond){access;innerBody;update;}
                     FixMovedExpr.moveScope(sz, body, g, false);
                    BinaryExpression cond = (BinaryExpression) UtilIR.createExpression(UtilIR.createExpression(body, (Variable) v), "<=", sz);
                     //v=op1 intialize
                     Expression start = expr.getOperand1();
                     start.setContext(body);
                     UtilIR.createAssign(0,body, (Variable) v, start);
                     //increment index var
                     UtilIR.createAssign(-1,innerBody, (Variable) v,
                             UtilIR.createExpression(UtilIR.createExpression(innerBody, (Variable) v), "+", UtilIR.lit(1)));
                     //make while statement
                     WhileLoop whileloop = UtilIR.createWhileLoop(body, innerBody, cond);
                     innerBody = body;
                     nesting++;
                 }
             } else if(g.getSource() instanceof ListExpression) {
                 if(g.getDeclarations().get(0) instanceof Variable) {
                     Variable v = (Variable) g.getDeclarations().get(0);
                     ListExpression lit = (ListExpression) g.getSource();
                     int listDim=0;
                     Type t = ((Variable)v).getType();
                     while(t instanceof TypeList) {
                         listDim++;
                         t = ((TypeList)t).getType();
                     }
                     int listExprDim = 0;
                     t = g.getSource().getType();
                     while(t instanceof TypeList) {
                         listExprDim++;
                         t = ((TypeList)t).getType();
                     }
                     if(listExprDim != listDim+1) {
                         throw new RuntimeException("[CreateForLoop] Expecting foreach list expressions to have same type and be of one more dimension then index variable.");
                     }
                     if(0 != listDim) {
                         throw new RuntimeException("[CreateForLoop] Not yet implemented foreach list expressions to be more than one dimensional.");
                     }
                     Block body = UtilIR.createBlock(outer);
                     innerBody.setOuter(body);
                     body.getDeclarations().add(v);
                     v.setScope(body);
                     //Create a variable that can be used to index into the list
                     TypeInt type = IrFactory.eINSTANCE.createTypeInt();
                     Declaration index = UtilIR.createVarDef(body, v.getName()+"__ListIndex", type, false, false, UtilIR.lit(0));
                     //Create a variable that const instantiate the list
                     lit.setContext(body);
                     Declaration list = UtilIR.createVarDef(body, v.getName()+"__ListExpression", lit.getType());
                     FixMovedExpr.moveScope(lit, body, g, false);
                     UtilIR.createAssign(0,body, (Variable) list, lit);
                     Expression sz = null;
                     if(lit.getType() instanceof TypeList) {
                         sz = ((TypeList)lit.getType()).getSize();
                     } else {
                         //Fallback when list expression misses type
                         sz = UtilIR.lit(lit.getExpressions().size());
                     }
                     //initialize;while(cond){access;innerBody;update;}
                     BinaryExpression cond = (BinaryExpression) UtilIR.createExpression(UtilIR.createExpression(body, (Variable) index), "<", sz);
                     //statement to access list inserted first in nested/inner block
                     access = UtilIR.createAssign(0,innerBody, v, 
                             UtilIR.createExpression(innerBody, (Variable) list,
                                     Arrays.asList(UtilIR.createExpression(innerBody, (Variable) index)),
                                     null));
                     //increment index var
                     UtilIR.createAssign(-1,innerBody, (Variable) index,
                             UtilIR.createExpression(UtilIR.createExpression(innerBody, (Variable) index), "+", UtilIR.lit(1)));
                     //make while statement
                     WhileLoop whileloop = UtilIR.createWhileLoop(body, innerBody, cond);
                     innerBody = body;
                     nesting++;
                 }
             } else if(g.getSource() instanceof FunctionCall) {
                 if(g.getDeclarations().get(0) instanceof Variable) {
                     Variable v = (Variable) g.getDeclarations().get(0);
                     FunctionCall call = (FunctionCall) g.getSource();
                     int listDim=0;
                     Type t = ((Variable)v).getType();
                     while(t instanceof TypeList) {
                         listDim++;
                         t = ((TypeList)t).getType();
                     }
                     int listExprDim = 0;
                     t = g.getSource().getType();
                     while(t instanceof TypeList) {
                         listExprDim++;
                         t = ((TypeList)t).getType();
                     }
                     if(listExprDim != listDim+1) {
                         throw new RuntimeException("[CreateForLoop] Expecting foreach list function to have same type and be of one more dimension then index variable.");
                     }
                     if(0 != listDim) {
                         throw new RuntimeException("[CreateForLoop] Not yet implemented foreach function generated list to be more than one dimensional.");
                     }
                     Block body = UtilIR.createBlock(outer);
                     innerBody.setOuter(body);
                     body.getDeclarations().add(v);
                     v.setScope(body);
                     //Create a variable that can be used to index into the list
                     TypeInt type = IrFactory.eINSTANCE.createTypeInt();
                     Declaration index = UtilIR.createVarDef(body, v.getName()+"__ListIndex", type, false, false, UtilIR.lit(0));
                     //Create a variable that const instantiate the list
                     call.setContext(body);
                     call.getFunction().setContext(body);
                     for(Expression p: call.getParameters()) {
                         p.setContext(body);
                     }
                     Declaration list = UtilIR.createVarDef(body, v.getName()+"__ListExpression", call.getType());
                     UtilIR.createAssign(0,body, (Variable) list, call);
                     Expression sz = null;
                     if(call.getType() instanceof TypeList && ((TypeList)call.getType()).getSize()!=null) {
                         sz = ((TypeList)call.getType()).getSize();
                     } else {
                         throw new RuntimeException("[CreateForLoop] Not yet implemented foreach list function returning lists of unknown length.");
                     }
                     //initialize;while(cond){access;innerBody;update;}
                     BinaryExpression cond = (BinaryExpression) UtilIR.createExpression(UtilIR.createExpression(body, (Variable) index), "<", sz);
                     //statement to access list inserted first in nested/inner block
                     access = UtilIR.createAssign(0,innerBody, v, 
                             UtilIR.createExpression(innerBody, (Variable) list,
                                     Arrays.asList(UtilIR.createExpression(innerBody, (Variable) index)),
                                     null));
                     //increment index var
                     UtilIR.createAssign(-1,innerBody, (Variable) index,
                             UtilIR.createExpression(UtilIR.createExpression(innerBody, (Variable) index), "+", UtilIR.lit(1)));
                     //make while statement
                     WhileLoop whileloop = UtilIR.createWhileLoop(body, innerBody, cond);
                     innerBody = body;
                     nesting++;
                 }
             }
         }
         innerBody.setOuter(outer);
         stmt.setBody(innerBody);
         stmt.getGenerators().clear();
         return stmt;
     }
 
 	
 	@Override
 	public AbstractActor caseNetwork(Network obj) {
 		AbstractActor ret = super.caseNetwork(obj);
 		String path = TransUtil.getPath(ret);
 		TransUtil.AnnotatePass(ret, IrPassTypes.CreateForLoop, "0");
 		ActorDirectory.addTransformedActor(ret, null, path);
 
 		for(ActorInstance a : obj.getActors()) {
 			AbstractActor actor=null;
 			try {
 				System.out.println("[CreateForEachDeclarations] Read in actor instance '" + a.getName() + "' of class " + ((TypeActor) a.getType()).getName());
 				actor = (AbstractActor) ActorDirectory.findTransformedActor(a);
 			} catch (DirectoryException x) {
 				//OK, since likely external
 			}
 			if(actor!=null && !(actor instanceof ExternalActor)) {
 				actor = (AbstractActor) doSwitch(actor);
 				path = TransUtil.getPath(actor);
 				TransUtil.AnnotatePass(actor, IrPassTypes.CreateForLoop, "0");
 				ActorDirectory.addTransformedActor(actor, a, path);
 			}
 		}
 		return ret;
 	}
 
 }
