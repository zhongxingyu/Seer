 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.NodeFinder;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
 import org.eclipse.jdt.internal.corext.refactoring.Checks;
 import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
 import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
 import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
 import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
 import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
 import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
 import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
 import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
 import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
 import org.eclipse.jdt.internal.corext.util.Messages;
 import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
 import org.eclipse.jdt.ui.JavaElementLabels;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.Refactoring;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
 
 import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
 import edu.illinois.compositerefactorings.refactorings.NewClassCreator;
 
 @SuppressWarnings("restriction")
 public class CreateNewTopLevelSuperClassRefactoring extends Refactoring {
 
 	/**
 	 * CompilationUnitRewrites for all affected cus
 	 */
 	private Map<ICompilationUnit, CompilationUnitRewrite> fRewrites;
 
 	/**
 	 * Text change manager (actually a CompilationUnitChange manager) which manages all changes.
 	 */
 	private TextChangeManager fChangeManager;
 
 	/** The subtype to replace */
 	private IType fType;
 
 	private String fClassName;
 
 	private List<ResourceChange> fClassCreationChanges;
 
 	public CreateNewTopLevelSuperClassRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
 		RefactoringStatus initializeStatus= initialize(arguments);
 		status.merge(initializeStatus);
 	}
 
 	private final RefactoringStatus initialize(JavaRefactoringArguments extended) {
 		String handle= extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
 		if (handle != null) {
 			final IJavaElement element= JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), handle, false);
 			if (element == null || !element.exists() || element.getElementType() != IJavaElement.TYPE) {
 				return JavaRefactoringDescriptorUtil.createInputFatalStatus(element, getName(), CreateNewTopLevelSuperClassDescriptor.ID);
 			} else {
 				fType= (IType)element;
 			}
 		} else {
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
 		}
 		fClassName= extended.getAttribute(CreateNewTopLevelSuperClassDescriptor.CLASS_NAME);
 		if (fClassName == null) {
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, CreateNewTopLevelSuperClassDescriptor.CLASS_NAME));
 		}
 		return new RefactoringStatus();
 	}
 
 	public final IType getType() {
 		return fType;
 	}
 
 	private CompilationUnitRewrite getCachedCURewrite(ICompilationUnit unit) {
 		CompilationUnitRewrite rewrite= fRewrites.get(unit);
 		if (rewrite == null) {
 			rewrite= new CompilationUnitRewrite(unit);
 			fRewrites.put(unit, rewrite);
 		}
 		return rewrite;
 	}
 
 	private void createChangeAndDiscardRewrite(ICompilationUnit compilationUnit) throws CoreException {
 		CompilationUnitRewrite rewrite= fRewrites.get(compilationUnit);
 		if (rewrite != null) {
 			fChangeManager.manage(compilationUnit, rewrite.createChange(true));
 			fRewrites.remove(compilationUnit);
 		}
 	}
 
 	private IFile[] getAllFilesToModify() {
 		List<ICompilationUnit> cus= new ArrayList<ICompilationUnit>();
 		cus.addAll(Arrays.asList(fChangeManager.getAllCompilationUnits()));
 		return ResourceUtil.getFiles(cus.toArray(new ICompilationUnit[cus.size()]));
 	}
 
 	@Override
 	public String getName() {
 		return CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_name;
 	}
 
 	@Override
 	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		fRewrites= new HashMap<ICompilationUnit, CompilationUnitRewrite>();
 		RefactoringStatus result= new RefactoringStatus();
 		result.merge(Checks.checkAvailability(fType));
 		if (!result.isOK()) {
 			return result;
 		}
 		return result;
 	}
 
 	private RefactoringStatus checkPackageClass() {
 		RefactoringStatus status= new RefactoringStatus();
 		IPackageFragment packageFragment= (IPackageFragment)fType.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
 		if (packageFragment.getCompilationUnit(fClassName + JavaModelUtil.DEFAULT_CU_SUFFIX).exists())
 			status.addError(Messages.format(RefactoringCoreMessages.ExtractClassRefactoring_error_toplevel_name_clash, new Object[] {
 					BasicElementLabels.getJavaElementName(fClassName), BasicElementLabels.getJavaElementName(packageFragment.getElementName()) }));
 		return status;
 	}
 
 	@Override
 	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		RefactoringStatus result= new RefactoringStatus();
 		result.merge(Checks.checkTypeName(fClassName, fType));
 		if (result.hasFatalError()) {
 			return result;
 		}
 
 		result.merge(checkPackageClass());
 		if (result.hasFatalError()) {
 			return result;
 		}
 
 		fChangeManager= new TextChangeManager();
 
 		IPackageFragment packageFragment= fType.getPackageFragment();
 
 		CompilationUnitRewrite typeCURewrite= getCachedCURewrite(fType.getCompilationUnit());
 		typeCURewrite.getImportRewrite().addImport(packageFragment.getElementName() + "." + fClassName);
 		TypeDeclaration typeDeclaration= (TypeDeclaration)NodeFinder.perform(typeCURewrite.getRoot(), fType.getSourceRange());
 		SimpleName simpleName= typeDeclaration.getAST().newSimpleName(fClassName);
 		SimpleType simpleType= typeDeclaration.getAST().newSimpleType(simpleName);
 		typeCURewrite.getASTRewrite().set(typeDeclaration, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, simpleType, null);
 		createChangeAndDiscardRewrite(fType.getCompilationUnit());
 
		IType superclassType= fType.newSupertypeHierarchy(new NullProgressMonitor()).getSuperclass(fType);
		fClassCreationChanges= new NewClassCreator(fClassName, packageFragment, typeDeclaration.getSuperclassType()).createTopLevelParameterObject();
 
 		Checks.checkCompileErrorsInAffectedFile(result, fType.getResource());
 		if (result.hasFatalError()) {
 			return result;
 		}
 		result.merge(Checks.validateModifiesFiles(getAllFilesToModify(), getValidationContext()));
 		return result;
 	}
 
 	@Override
 	public final Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		Assert.isNotNull(pm);
 		try {
 			pm.beginTask("", 1); //$NON-NLS-1$
 			pm.setTaskName(RefactoringCoreMessages.ExtractInterfaceProcessor_creating);
 
 			ArrayList<Change> changes= new ArrayList<Change>();
 			changes.addAll(fClassCreationChanges);
 			changes.addAll(Arrays.asList(fChangeManager.getAllChanges()));
 
 			if (changes.size() != 0) {
 				IJavaProject project= fType.getJavaProject();
 				int flags= RefactoringDescriptor.STRUCTURAL_CHANGE | JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING
 						| JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
 				final String name= project != null ? project.getElementName() : null;
 				final String description= MessageFormat.format(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_description,
 						BasicElementLabels.getJavaElementName(fType.getElementName()));
 				final String header= MessageFormat.format(
 						CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_description,
 						JavaElementLabels.getElementLabel(fType, JavaElementLabels.ALL_FULLY_QUALIFIED));
 				final JDTRefactoringDescriptorComment comment= new JDTRefactoringDescriptorComment(name, this, header);
 				comment.addSetting(Messages.format(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_comment_new_super_class, BasicElementLabels.getJavaElementName(fClassName)));
 
 				final CreateNewTopLevelSuperClassDescriptor descriptor= new CreateNewTopLevelSuperClassDescriptor();
 				descriptor.setProject(name);
 				descriptor.setDescription(description);
 				descriptor.setComment(comment.asString());
 				descriptor.setFlags(flags);
 				descriptor.setType(getType());
 				descriptor.setClassName(fClassName);
 
 				return new DynamicValidationRefactoringChange(descriptor, description, changes.toArray(new Change[] {}));
 			}
 			pm.worked(1);
 		} finally {
 			pm.done();
 		}
 		return null;
 	}
 
 }
