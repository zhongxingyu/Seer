 /*******************************************************************************
  * Copyright (c) 2012 Jens von Pilgrim
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU General Public License v3
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package de.jevopi.j2og.simpleParser;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 
 import de.jevopi.j2og.model.Type;
import de.jevopi.j2og.model.Model;
 
 /**
  * @author Jens von Pilgrim (developer@jevopi.de)
  */
 public class SimpleJDTParser {
 
 	IPackageFragment packageFragment;
 	Visitor v;
 	private ICompilationUnit compilationUnit;
 
 	/**
 	 * @param i_packageFragment
 	 */
 	public SimpleJDTParser() {
 		v = new Visitor();
 	}
 
 	/**
 	 * @param i_packageFragment the packageFragment to set
 	 */
 	public void setPackageFragment(IPackageFragment i_packageFragment) {
 		packageFragment = i_packageFragment;
 		compilationUnit = null;
 	}
 
 	/**
 	 * @param i_compilationUnit
 	 * @since Nov 1, 2011
 	 */
 	public void setCompilationUnit(ICompilationUnit i_compilationUnit) {
 		compilationUnit = i_compilationUnit;
 		packageFragment = null;
 
 	};
 
 	/**
 	 * 
 	 * @since Aug 18, 2011
 	 */
 	public void run() throws Exception {
 
 		//		IJavaProject javaProject = null;
 		//		if (packageFragment != null)
 		//			javaProject = packageFragment.getJavaProject();
 		//		else
 		//			javaProject = compilationUnit.getJavaProject();
 
 		IProgressMonitor monitor = new NullProgressMonitor();
 
 		Collection<CompilationUnit> compilationUnits =
 			new ArrayList<CompilationUnit>();
 
 		ICompilationUnit[] cus =
 			(packageFragment != null) ? packageFragment.getCompilationUnits()
 				: new ICompilationUnit[] {
 					compilationUnit
 				};
 
 		for (ICompilationUnit icu : cus) {
 			final ASTParser parser = ASTParser.newParser(AST.JLS3);
 			parser.setResolveBindings(true);
 			parser.setSource(icu);
 			CompilationUnit cu = (CompilationUnit) parser.createAST(monitor);
 			compilationUnits.add(cu);
 		}
 		for (CompilationUnit cu : compilationUnits) {
 			cu.accept(v);
 		}
 
 	}
 
 	/**
 	 * @return the classifiers
 	 */
 	public Collection<Type> getClassifiers() {
 		return v.getModel().allTypes();
 	}
 
 }
