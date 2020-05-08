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
 
 package org.caltoopia.codegen.transformer.analysis;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.caltoopia.ir.AbstractActor;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.ActorInstance;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.Assign;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.ExternalActor;
 import org.caltoopia.ir.ForwardDeclaration;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.Guard;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Namespace;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.PortPeek;
 import org.caltoopia.ir.PortRead;
 import org.caltoopia.ir.PortWrite;
 import org.caltoopia.ir.ProcCall;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeBool;
 import org.caltoopia.ir.TypeConstructor;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeFloat;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeProc;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeString;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableImport;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.util.IrReplaceSwitch;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.CompilationSession;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.codegen.IrXmlPrinter;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.transformer.IrTransformer;
 import org.caltoopia.codegen.transformer.IrTransformer.IrPassTypes;
 import org.caltoopia.codegen.transformer.TransUtil.HowLiteral;
 import org.caltoopia.codegen.transformer.TransUtil;
 
 public class IrUsedDeclarationAnnotation extends IrReplaceSwitch {
 
 	private Network currentNetwork=null;
 	private Namespace currentNamespace=null;
     private VariableExpression currentVarExpr=null;
     private VariableReference currentVarRef=null;
 	private PrintStream serr = null; 
 	private CompilationSession session;
 
 	/* 
 	 * Find all declarations which is needed due to 
 	 * at least one user has not removed usage in
 	 * constant expression evaluation.
 	 */
 	
 	public IrUsedDeclarationAnnotation(Node node, CompilationSession session, boolean errPrint) {
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
 
 	@Override
 	public Expression caseVariableExpression(VariableExpression var) {
 	    currentVarExpr = var;
 	    Expression expr = super.caseVariableExpression(var);
         currentVarExpr = null;
 	    return expr;
 	}
 	
 	@Override
 	public VariableReference caseVariableReference(VariableReference var) {
         currentVarRef = var;
         var = super.caseVariableReference(var);
         currentVarRef = null;
         return var;
 	}
 
     @Override
 	public Declaration caseVariable(Variable var) {
         if(currentVarExpr!=null || currentVarRef!=null)
             TransUtil.setAnnotation(var,IrTransformer.VARIABLE_ANNOTATION,"DeclUsed",
                     "TRUE");
 		return super.caseVariable(var);
 	}
 	
     @Override
    public Declaration caseForwardDeclaration(ForwardDeclaration decl) {
        if(currentVarExpr!=null || currentVarRef!=null) {
            TransUtil.setAnnotation(decl.getDeclaration(),IrTransformer.VARIABLE_ANNOTATION,"DeclUsed",
                    "TRUE");
        }
        return decl;
    }

    @Override
     public EObject caseVariableImport(VariableImport var) {
         Declaration decl = UtilIR.getDeclarationTransformed(var);
         if(decl!=null) {
             doSwitch(decl);
         }
         return var;
     }
     
     @Override
     public Declaration caseVariableExternal(VariableExternal var) {
         return var;
     }
 
 	@Override
 	public AbstractActor caseNetwork(Network obj) {
 		currentNetwork = obj;
 		for(ActorInstance a : obj.getActors()) {
 			AbstractActor actor=null;
 			try {
 				System.out.println("[IrAnnotate Used Declaration] Read in actor instance '" + a.getName() + "' of class " + ((TypeActor) a.getType()).getName());
 				actor = (AbstractActor) ActorDirectory.findTransformedActor(a);
 			} catch (DirectoryException x) {
 				//serr.println("[IrAnnotateVariable] Internal error could not get actor of type " + a.getType().toString());
 			}
 			if(actor!=null && !(actor instanceof ExternalActor)) {
 				actor = (AbstractActor) doSwitch(actor);
 				String path = TransUtil.getPath(actor);
 				TransUtil.AnnotatePass(actor, IrPassTypes.UsedDeclaration, "0");
 				ActorDirectory.addTransformedActor(actor, a, path);
 			}
 		}
 		AbstractActor ret = super.caseNetwork(obj);
 		String path = TransUtil.getPath(ret);
 		TransUtil.AnnotatePass(ret, IrPassTypes.UsedDeclaration, "0");
 		ActorDirectory.addTransformedActor(ret, null, path);
 		currentNetwork = null;
 		return ret;
 	}
 	
 	@Override
 	public Namespace caseNamespace(Namespace obj) {
 		currentNamespace = obj;
 		Namespace ret = super.caseNamespace(obj);
 		currentNamespace = null;
 		return ret;
 	}
 
 }
