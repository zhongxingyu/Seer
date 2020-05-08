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
 
import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 
 public class LPGAnalysisTypes {
     public static final TypeFactory tf= TypeFactory.getInstance();
 
    public static final Type LPGNonTerminalType= tf.namedType("org.lpg.nonTerminal", tf.stringType());
    public static final Type LPGCallGraphType= tf.namedType("org.lpg.callGraphType", tf.relType(LPGNonTerminalType, LPGNonTerminalType));
 }
