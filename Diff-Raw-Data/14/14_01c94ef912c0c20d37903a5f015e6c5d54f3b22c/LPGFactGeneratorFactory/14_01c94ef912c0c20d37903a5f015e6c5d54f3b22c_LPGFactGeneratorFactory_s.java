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
 
 import org.eclipse.imp.pdb.analysis.IFactGenerator;
 import org.eclipse.imp.pdb.analysis.IFactGeneratorFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 
 public class LPGFactGeneratorFactory implements IFactGeneratorFactory {
     public IFactGenerator create(Type type) {
         if (type.equals(LPGAnalysisTypes.LPGCallGraphType)) {
             return new LPGCallGraphGenerator();
         }
         throw new IllegalArgumentException("LPG Fact Generator: don't know how to produce fact " + type);
    }
 
     public String getName() {
         return "LPG Fact Generator Factory";
     }
 
     public void declareTypes(TypeFactory factory) {
         // Force static initializers on LPGAnalysisTypes to run
         Type dummy= LPGAnalysisTypes.LPGCallGraphType;
     }
 }
