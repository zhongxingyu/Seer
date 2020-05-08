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
 package de.jevopi.j2og.ui;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 import de.jevopi.j2og.AppleScriptLauncher;
 import de.jevopi.j2og.Plugin;
 import de.jevopi.j2og.config.Config;
 import de.jevopi.j2og.model.Type;
 import de.jevopi.j2og.okas.OGAppleScriptCreator;
 import de.jevopi.j2og.simpleParser.SimpleJDTParser;
 
 public class CreateOmniGraffleDiagramAction implements IObjectActionDelegate {
 
 	private ISelection selection;
 
 	@Override
 	public void run(IAction action) {
 		if (!(selection instanceof IStructuredSelection)) return;
 		Iterator<?> it = ((IStructuredSelection) selection).iterator();
 		Set<IPackageFragment> packageFragments =
 			new HashSet<IPackageFragment>();
 		Set<ICompilationUnit> compilationUnits =
 			new HashSet<ICompilationUnit>();
 		while (it.hasNext()) {
 			Object next = it.next();
 			if (next instanceof IPackageFragment) {
 				packageFragments.add((IPackageFragment) next);
 			} else if (next instanceof ICompilationUnit) {
 				compilationUnits.add((ICompilationUnit) next);
 			}
//				else {
//				MessageDialog.openError(getShell(),
//						"Create OmniGraffle Diagram",
//						"This tool can only create diagrams for packages");
//				return;
//			}
 		}
 
 		if (packageFragments.size()+compilationUnits.size() == 0) return;
 
 		Config config = ConfigDialog.show(getShell());
 		if (config == null) return;
 		config.omniGraffleAppName =
 			Plugin.getDefault().getPreferenceStore()
 				.getString(PreferenceInitializer.OMNIGRAFFLE_APP);
 
 		if (config.recursive) {
 			HashSet<IPackageFragment> allPackageFragments =
 				new HashSet<IPackageFragment>();
 			try {
 				for (IPackageFragment packageFragment : packageFragments) {
 					allPackageFragments.add(packageFragment);
 					collectSubPackages(packageFragment, allPackageFragments);
 				}
 			} catch (Exception ex) {
 				MessageDialog.openError(getShell(),
 						"Error retrieving subpackages", ex.toString());
 				return;
 			}
 			packageFragments = allPackageFragments;
 		}
 
 		String s = "Nothing created :-)";
 		Collection<Type> classifiers = new HashSet<Type>();
 		Set<String> packageNames = new HashSet<String>();
 		SimpleJDTParser simpleJDTParser =
 			new SimpleJDTParser();
 		for (IPackageFragment packageFragment : packageFragments) {
 			packageNames.add(packageFragment.getElementName());
 			simpleJDTParser.setPackageFragment(packageFragment);
 			try {
 				simpleJDTParser.run();
 				
 			} catch (Exception ex) {
 				MessageDialog.openError(getShell(), "Error parsing packages",
 						ex.toString());
 				return;
 			}
 			classifiers = simpleJDTParser.getClassifiers();
 		}
 		for (ICompilationUnit compilationUnit: compilationUnits) {
 			packageNames.add(compilationUnit.getParent().getElementName());
 			simpleJDTParser.setCompilationUnit(compilationUnit);
 			try {
 				simpleJDTParser.run();
 				
 			} catch (Exception ex) {
 				MessageDialog.openError(getShell(), "Error parsing packages",
 						ex.toString());
 				return;
 			}
 			classifiers = simpleJDTParser.getClassifiers();
 		}
 		try {
 			OGAppleScriptCreator creator = new OGAppleScriptCreator();
 			s = creator.toAppleScript(packageNames, config, classifiers);
 			System.out
 				.println("------------------------------------------------------------------");
 			System.out.println(s);
 			System.out
 				.println("------------------------------------------------------------------");
 
 			AppleScriptLauncher.launch(s);
 
 		} catch (Exception ex) {
 			MessageDialog.openError(getShell(),
 					"Error creating OmniGraffle Diagram", ex.toString());
 			return;
 		}
 
 		MessageDialog
 			.openInformation(
 					getShell(),
 					"OmniGraffle Diagram Created",
 					"New classes should be visible in your OmniGraffle drawing.\n\n"
 						+ "If you like this little tool, "
 						+ "visit http://jevopisdeveloperblog.blogspot.com and "
 						+ "drop me a note, flattr me or donate to support the development of this tool.\n" 
 						+ "Software development requires time, and time is money ;-)");
 
 	}
 
 	/**
 	 * @param i_packageFragment
 	 * @param i_allPackageFragments
 	 * @throws JavaModelException
 	 * @since Oct 31, 2011
 	 */
 	private void collectSubPackages(IPackageFragment packageFragment,
 			HashSet<IPackageFragment> o_allPackageFragments)
 			throws JavaModelException {
 		IJavaElement[] packages =
 			((IPackageFragmentRoot) (packageFragment.getParent()))
 				.getChildren();
 		String n = packageFragment.getElementName();
 		for (IJavaElement sub : packages) {
 			if (sub != packageFragment && sub instanceof IPackageFragment
 				&& sub.getElementName().startsWith(n)) {
 				o_allPackageFragments.add((IPackageFragment) sub);
 				collectSubPackages((IPackageFragment) sub,
 						o_allPackageFragments);
 			}
 
 		}
 	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 	}
 
 	@Override
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
 
 	private Shell getShell() {
 		IWorkbenchWindow window =
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (window == null) {
 			return null;
 		}
 		return window.getShell();
 	}
 }
