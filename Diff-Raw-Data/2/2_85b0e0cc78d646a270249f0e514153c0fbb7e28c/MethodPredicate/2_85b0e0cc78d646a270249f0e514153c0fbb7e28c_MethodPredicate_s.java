 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.search;
 
 import org.eclipse.dltk.core.search.matching2.AbstractMatchingPredicate;
 import org.eclipse.dltk.core.search.matching2.MatchLevel;
 import org.eclipse.dltk.internal.core.search.matching.MethodDeclarationPattern;
 import org.eclipse.dltk.internal.core.search.matching.MethodPattern;
 
 public class MethodPredicate extends AbstractMatchingPredicate<MatchingNode> {
 
 	private final boolean declarations;
 	private final boolean references;
 
 	public MethodPredicate(MethodPattern pattern) {
		super(pattern, pattern.declaringSimpleName);
 		this.declarations = pattern.findDeclarations;
 		this.references = pattern.findReferences;
 	}
 
 	public MethodPredicate(MethodDeclarationPattern pattern) {
 		super(pattern, pattern.simpleName);
 		this.declarations = true;
 		this.references = false;
 	}
 
 	public MatchLevel match(MatchingNode node) {
 		if (node instanceof MethodDeclarationNode) {
 			if (!declarations)
 				return null;
 			final MethodDeclarationNode mNode = (MethodDeclarationNode) node;
 			return matchName(mNode.node.getName());
 		} else if (node instanceof MethodReferenceNode) {
 			if (!references)
 				return null;
 			final MethodReferenceNode mNode = (MethodReferenceNode) node;
 			return matchName(mNode.node.getName());
 		} else {
 			return null;
 		}
 	}
 
 }
