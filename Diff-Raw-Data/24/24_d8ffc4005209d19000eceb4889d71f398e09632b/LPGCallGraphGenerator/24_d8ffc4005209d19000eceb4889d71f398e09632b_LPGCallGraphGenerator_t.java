 /*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/
 
 package org.eclipse.imp.lpg.analysis;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.language.ServiceFactory;
 import org.eclipse.imp.lpg.LPGRuntimePlugin;
 import org.eclipse.imp.lpg.parser.LPGParser.ASTNode;
 import org.eclipse.imp.lpg.parser.LPGParser.AbstractVisitor;
 import org.eclipse.imp.lpg.parser.LPGParser.IsymWithAttrs;
 import org.eclipse.imp.lpg.parser.LPGParser.nonTerm;
 import org.eclipse.imp.lpg.parser.LPGParser.rule;
 import org.eclipse.imp.lpg.parser.LPGParser.symWithAttrsList;
 import org.eclipse.imp.lpg.parser.LPGParser.symWithAttrs__SYMBOL_optAttrList;
 import org.eclipse.imp.model.ICompilationUnit;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.pdb.analysis.AnalysisException;
 import org.eclipse.imp.pdb.analysis.IFactGenerator;
 import org.eclipse.imp.pdb.facts.ISetWriter;
 import org.eclipse.imp.pdb.facts.ITuple;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.db.FactBase;
 import org.eclipse.imp.pdb.facts.db.FactKey;
 import org.eclipse.imp.pdb.facts.db.IFactContext;
 import org.eclipse.imp.pdb.facts.db.context.CompilationUnitContext;
 import org.eclipse.imp.pdb.facts.impl.reference.ValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.utils.ErrorIndicatorMessageHandler;
 
 public class LPGCallGraphGenerator implements IFactGenerator {
 
     private final class CGVisitor extends AbstractVisitor {
         private String lhsStr;
         private final ISetWriter fWriter;
         private final ValueFactory fValueFactory= ValueFactory.getInstance();
 
         public CGVisitor(ISetWriter rw) {
             fWriter= rw;
         }
 
         @Override
         public void unimplementedVisitor(String s) { }
 
         @Override
         public boolean visit(nonTerm n) {
             lhsStr= n.getruleNameWithAttributes().getSYMBOL().toString();
             return super.visit(n);
         }
 
         @Override
         public boolean visit(rule n) {
             symWithAttrsList rhsList= n.getsymWithAttrsList();
             for(int i=0; i < rhsList.size(); i++) {
                 IsymWithAttrs sym= rhsList.getsymWithAttrsAt(i);
                 if (sym instanceof symWithAttrs__SYMBOL_optAttrList) {
                 	symWithAttrs__SYMBOL_optAttrList sym1= (symWithAttrs__SYMBOL_optAttrList) sym;
                     String rhsStr= sym1.getSYMBOL().toString();
         
                     IValue lhs = LPGAnalysisTypes.LPGNonTerminalType.make(fValueFactory, lhsStr);
 					IValue rhs = LPGAnalysisTypes.LPGNonTerminalType.make(fValueFactory, rhsStr);
 					ITuple tuple = fValueFactory.tuple(lhs, rhs);

 					fWriter.insert(tuple);
                 }
             }
             return true;
         }
     }
 
     public void generate(FactBase factBase, Type type, IFactContext context) throws AnalysisException {
         CompilationUnitContext cuc= (CompilationUnitContext) context;
         Language lpgLang= LanguageRegistry.findLanguage(LPGRuntimePlugin.getInstance().getLanguageID());
         IParseController pc= ServiceFactory.getInstance().getParseController(lpgLang);
        ErrorIndicatorMessageHandler mh= new ErrorIndicatorMessageHandler();
         ICompilationUnit cu= cuc.getCompilationUnit();
 
         pc.initialize(cu.getPath(), cu.getProject(), mh);
 
         ASTNode root= (ASTNode) pc.parse(cu.getSource(), new NullProgressMonitor());

        if (mh.hadErrors()) {
            // do something here?
        }
         final ISetWriter cgw= LPGAnalysisTypes.LPGCallGraphType.writer(ValueFactory.getInstance());

         root.accept(new CGVisitor(cgw));
         factBase.defineFact(new FactKey(type, context), cgw.done());
     }
 }
