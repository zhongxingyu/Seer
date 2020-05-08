 package descent.internal.core;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 
 import descent.core.IJavaElement;
 import descent.core.IPackageFragment;
 import descent.core.IType;
 import descent.core.JavaCore;
 import descent.core.JavaModelException;
 import descent.core.WorkingCopyOwner;
 import descent.core.compiler.CharOperation;
 import descent.core.search.IJavaSearchConstants;
 import descent.core.search.IJavaSearchScope;
 import descent.core.search.SearchPattern;
 import descent.internal.codeassist.ISearchRequestor;
 import descent.internal.compiler.env.AccessRestriction;
 import descent.internal.compiler.env.ICompilationUnit;
 import descent.internal.compiler.env.INameEnvironment;
 import descent.internal.compiler.env.ISourceType;
 import descent.internal.compiler.env.NameEnvironmentAdapter;
 import descent.internal.compiler.env.NameEnvironmentAnswer;
 import descent.internal.core.search.BasicSearchEngine;
 import descent.internal.core.search.IRestrictedAccessDeclarationRequestor;
 import descent.internal.core.search.IRestrictedAccessTypeRequestor;
 
 /**
  *	This class provides a <code>SearchableBuilderEnvironment</code> for code assist which
  *	uses the Java model as a search tool.  
  */
 public class SearchableEnvironment
 	extends NameEnvironmentAdapter
 	implements INameEnvironment, IJavaSearchConstants {
 	
 	public NameLookup nameLookup;
 	protected ICompilationUnit unitToSkip;
 	protected descent.core.ICompilationUnit[] workingCopies;
 
 	protected JavaProject project;
 	protected IJavaSearchScope searchScope;
 	
 	protected boolean checkAccessRestrictions;
 
 	/**
 	 * Creates a SearchableEnvironment on the given project
 	 */
 	public SearchableEnvironment(JavaProject project, descent.core.ICompilationUnit[] workingCopies) throws JavaModelException {
 		this.project = project;
 		this.checkAccessRestrictions = 
 			!JavaCore.IGNORE.equals(project.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, true))
 			|| !JavaCore.IGNORE.equals(project.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, true));
 		this.workingCopies = workingCopies;
 		this.nameLookup = project.newNameLookup(workingCopies);
 
 		// Create search scope with visible entry on the project's classpath
 		if(this.checkAccessRestrictions) {
 			this.searchScope = BasicSearchEngine.createJavaSearchScope(new IJavaElement[] {project});
 		} else {
 			this.searchScope = BasicSearchEngine.createJavaSearchScope(this.nameLookup.packageFragmentRoots);
 		}
 	}
 
 	/**
 	 * Creates a SearchableEnvironment on the given project
 	 */
 	public SearchableEnvironment(JavaProject project, WorkingCopyOwner owner) throws JavaModelException {
 		this(project, owner == null ? null : JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary WCs*/));
 	}
 
 	/**
 	 * Returns the given type in the the given package if it exists,
 	 * otherwise <code>null</code>.
 	 */
 	protected NameEnvironmentAnswer find(String typeName, String packageName) {
 		if (packageName == null)
 			packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
 		NameLookup.Answer answer =
 			this.nameLookup.findType(
 				typeName,
 				packageName,
 				false/*exact match*/,
 				NameLookup.ACCEPT_ALL,
 				this.checkAccessRestrictions);
 		if (answer != null) {
 			// construct name env answer
 			/* TODO JDT binary
 			if (answer.type instanceof BinaryType) { // BinaryType
 				try {
 					return new NameEnvironmentAnswer((IBinaryType) ((BinaryType) answer.type).getElementInfo(), answer.restriction);
 				} catch (JavaModelException npe) {
 					return null;
 				}
 			} else { //SourceType
 			*/
 				try {
 					// retrieve the requested type
 					SourceTypeElementInfo sourceType = (SourceTypeElementInfo)((SourceType) answer.type).getElementInfo();
 					ISourceType topLevelType = sourceType;
 					while (topLevelType.getEnclosingType() != null) {
 						topLevelType = topLevelType.getEnclosingType();
 					}
 					// find all siblings (other types declared in same unit, since may be used for name resolution)
 					IType[] types = sourceType.getHandle().getCompilationUnit().getTypes();
 					ISourceType[] sourceTypes = new ISourceType[types.length];
 	
 					// in the resulting collection, ensure the requested type is the first one
 					sourceTypes[0] = sourceType;
 					int length = types.length;
 					for (int i = 0, index = 1; i < length; i++) {
 						ISourceType otherType =
 							(ISourceType) ((JavaElement) types[i]).getElementInfo();
 						if (!otherType.equals(topLevelType) && index < length) // check that the index is in bounds (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=62861)
 							sourceTypes[index++] = otherType;
 					}
 					return new NameEnvironmentAnswer(sourceTypes, answer.restriction);
 				} catch (JavaModelException npe) {
 					return null;
 				}
 			//}
 		}
 		return null;
 	}
 
 	/**
 	 * Find the packages that start with the given prefix.
 	 * A valid prefix is a qualified name separated by periods
 	 * (ex. std.stdio).
 	 * The packages found are passed to:
 	 *    ISearchRequestor.acceptPackage(char[][] packageName)
 	 */
 	public void findPackages(char[] prefix, ISearchRequestor requestor) {
 		this.nameLookup.seekPackageFragments(
 			new String(prefix),
 			true,
 			new SearchableEnvironmentRequestor(requestor));
 	}
 	
 	/**
 	 * Find the compilation units that start with the given prefix.
 	 * A valid prefix is a qualified name separated by periods
 	 * (ex. std.stdio).
 	 * The packages found are passed to:
 	 *    ISearchRequestor.acceptCompilationUnit(char[][] fullyQualifiedName)
 	 */
 	public void findCompilationUnits(char[] prefix, ISearchRequestor requestor) {
 		this.nameLookup.seekCompilationUnits(
 			new String(prefix),
 			true,
 			new SearchableEnvironmentRequestor(requestor));
 	}
 
 	/**
 	 * @see descent.internal.compiler.env.INameEnvironment#findType(char[][])
 	 */
 	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
 		if (compoundTypeName == null) return null;
 
 		int length = compoundTypeName.length;
 		if (length <= 1) {
 			if (length == 0) return null;
 			return find(new String(compoundTypeName[0]), null);
 		}
 
 		int lengthM1 = length - 1;
 		char[][] packageName = new char[lengthM1][];
 		System.arraycopy(compoundTypeName, 0, packageName, 0, lengthM1);
 
 		return find(
 			new String(compoundTypeName[lengthM1]),
 			CharOperation.toString(packageName));
 	}
 
 	/**
 	 * @see descent.internal.compiler.env.INameEnvironment#findType(char[], char[][])
 	 */
 	public NameEnvironmentAnswer findType(char[] name, char[][] packageName) {
 		if (name == null) return null;
 
 		return find(
 			new String(name),
 			packageName == null || packageName.length == 0 ? null : CharOperation.toString(packageName));
 	}
 
 	/**
 	 * Find the top-level types (classes and interfaces) that are defined
 	 * in the current environment and whose name starts with the
 	 * given prefix. The prefix is a qualified name separated by periods
 	 * or a simple name (ex. java.util.V or V).
 	 *
 	 * The types found are passed to one of the following methods (if additional
 	 * information is known about the types):
 	 *    ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
 	 *    ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int modifiers)
 	 *    ISearchRequestor.acceptInterface(char[][] packageName, char[] typeName, int modifiers)
 	 *
 	 * This method can not be used to find member types... member
 	 * types are found relative to their enclosing type.
 	 */
 	public void findTypes(char[] prefix, final boolean findMembers, boolean camelCaseMatch, final ISearchRequestor storage) {
 
 		/*
 			if (true){
 				findTypes(new String(prefix), storage, NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
 				return;		
 			}
 		*/
 		try {
 			final String excludePath;
 			if (this.unitToSkip != null) {
 				if (!(this.unitToSkip instanceof IJavaElement)) {
 					// revert to model investigation
 					findTypes(
 						new String(prefix),
 						storage,
 						NameLookup.ACCEPT_ALL);
 					return;
 				}
 				excludePath = ((IJavaElement) this.unitToSkip).getPath().toString();
 			} else {
 				excludePath = null;
 			}
 			int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
 			char[] qualification, simpleName;
 			if (lastDotIndex < 0) {
 				qualification = null;
 				if (camelCaseMatch) {
 					simpleName = prefix;
 				} else {
 					simpleName = CharOperation.toLowerCase(prefix);
 				}
 			} else {
 				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
 				if (camelCaseMatch) {
 					simpleName = CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length);
 				} else {
 					simpleName =
 						CharOperation.toLowerCase(
 							CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length));
 				}
 			}
 
 			IProgressMonitor progressMonitor = new IProgressMonitor() {
 				boolean isCanceled = false;
 				public void beginTask(String name, int totalWork) {
 					// implements interface method
 				}
 				public void done() {
 					// implements interface method
 				}
 				public void internalWorked(double work) {
 					// implements interface method
 				}
 				public boolean isCanceled() {
 					return isCanceled;
 				}
 				public void setCanceled(boolean value) {
 					isCanceled = value;
 				}
 				public void setTaskName(String name) {
 					// implements interface method
 				}
 				public void subTask(String name) {
 					// implements interface method
 				}
 				public void worked(int work) {
 					// implements interface method
 				}
 			};
 			IRestrictedAccessTypeRequestor typeRequestor = new IRestrictedAccessTypeRequestor() {
 				public void acceptType(long modifiers, char[] packageName, char[] simpleTypeName, char[] templateParametersSignature, char[][] enclosingTypeNames, String path, int declarationStart, AccessRestriction access) {
 					if (excludePath != null && excludePath.equals(path))
 						return;
 					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
 						return; // accept only top level types
 					storage.acceptType(packageName, simpleTypeName, templateParametersSignature, enclosingTypeNames, modifiers, declarationStart, access);
 				}
 			};
 			try {
 				int matchRule = SearchPattern.R_PREFIX_MATCH;
 				if (camelCaseMatch) matchRule |= SearchPattern.R_CAMELCASE_MATCH;
 				new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
 					qualification,
 					simpleName,
 					matchRule, // not case sensitive
 					IJavaSearchConstants.TYPE,
 					this.searchScope,
 					typeRequestor,
 					CANCEL_IF_NOT_READY_TO_SEARCH,
 					progressMonitor);
 			} catch (OperationCanceledException e) {
 				findTypes(
 					new String(prefix),
 					storage,
 					NameLookup.ACCEPT_ALL);
 			}
 		} catch (JavaModelException e) {
 			findTypes(
 				new String(prefix),
 				storage,
 				NameLookup.ACCEPT_ALL);
 		}
 	}
 
 	/**
 	 * Returns all types whose name starts with the given (qualified) <code>prefix</code>.
 	 *
 	 * If the <code>prefix</code> is unqualified, all types whose simple name matches
 	 * the <code>prefix</code> are returned.
 	 */
 	private void findTypes(String prefix, ISearchRequestor storage, int type) {
 		//TODO (david) should add camel case support
 		SearchableEnvironmentRequestor requestor =
 			new SearchableEnvironmentRequestor(storage, this.unitToSkip, this.project, this.nameLookup);
 		int index = prefix.lastIndexOf('.');
 		if (index == -1) {
 			this.nameLookup.seekTypes(prefix, null, true, type, requestor);
 		} else {
 			String packageName = prefix.substring(0, index);
 			JavaElementRequestor elementRequestor = new JavaElementRequestor();
 			this.nameLookup.seekPackageFragments(packageName, false, elementRequestor);
 			IPackageFragment[] fragments = elementRequestor.getPackageFragments();
 			if (fragments != null) {
 				String className = prefix.substring(index + 1);
 				for (int i = 0, length = fragments.length; i < length; i++)
 					if (fragments[i] != null)
 						this.nameLookup.seekTypes(className, fragments[i], true, type, requestor);
 			}
 		}
 	}
 
 	/**
 	 * @see descent.internal.compiler.env.INameEnvironment#isPackage(char[][], char[])
 	 */
 	public boolean isPackage(char[][] parentPackageName, char[] subPackageName) {
 		String[] pkgName;
 		if (parentPackageName == null)
 			pkgName = new String[] {new String(subPackageName)};
 		else {
 			int length = parentPackageName.length;
 			pkgName = new String[length+1];
 			for (int i = 0; i < length; i++)
 				pkgName[i] = new String(parentPackageName[i]);
 			pkgName[length] = new String(subPackageName);
 		}
 		return this.nameLookup.isPackage(pkgName);
 	}
 	
 	@Override
 	public descent.core.ICompilationUnit findCompilationUnit(char[][] compoundName) {
 		return this.nameLookup.findCompilationUnit(CharOperation.toString(compoundName));
 	}
 
 	/**
 	 * Returns a printable string for the array.
 	 */
 	protected String toStringChar(char[] name) {
 		return "["  //$NON-NLS-1$
 		+ new String(name) + "]" ; //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns a printable string for the array.
 	 */
 	protected String toStringCharChar(char[][] names) {
 		StringBuffer result = new StringBuffer();
 		for (int i = 0; i < names.length; i++) {
 			result.append(toStringChar(names[i]));
 		}
 		return result.toString();
 	}
 	
 	public void cleanup() {
 		// nothing to do
 	}
 	
 	@Override
 	public void findDeclarations(char[] ident, ISearchRequestor requestor) {
 		findExactDeclarations(ident, 
 				false /* don't search members */, 
 				false /* no camel case match */, 
 				requestor);
 	}
 	
 	public void findExactDeclarations(char[] prefix, final boolean findMembers, boolean camelCaseMatch, final ISearchRequestor storage) {
 		findDeclarations(prefix, findMembers, camelCaseMatch, storage, true /* exact match */);
 	}
 	
 	public void findPrefixDeclarations(char[] prefix, final boolean findMembers, boolean camelCaseMatch, final ISearchRequestor storage) {
 		findDeclarations(prefix, findMembers, camelCaseMatch, storage, false /* not exact match */);
 	}
 
 	private void findDeclarations(char[] prefix, final boolean findMembers, boolean camelCaseMatch, final ISearchRequestor storage, boolean exactMatch) {
 
 		/*
 			if (true){
 				findTypes(new String(prefix), storage, NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
 				return;		
 			}
 		*/
 		try {
 			final String excludePath;
 			if (this.unitToSkip != null) {
 				if (!(this.unitToSkip instanceof IJavaElement)) {
 					// revert to model investigation
 					findTypes(
 						new String(prefix),
 						storage,
 						NameLookup.ACCEPT_ALL);
 					return;
 				}
 				excludePath = ((IJavaElement) this.unitToSkip).getPath().toString();
 			} else {
 				excludePath = null;
 			}
 			int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
 			char[] qualification, simpleName;
 			if (lastDotIndex < 0) {
 				qualification = null;
 				if (camelCaseMatch) {
 					simpleName = prefix;
 				} else {
 					simpleName = CharOperation.toLowerCase(prefix);
 				}
 			} else {
 				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
 				if (camelCaseMatch) {
 					simpleName = CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length);
 				} else {
 					simpleName =
 						CharOperation.toLowerCase(
 							CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length));
 				}
 			}
 
 			IProgressMonitor progressMonitor = new IProgressMonitor() {
 				boolean isCanceled = false;
 				public void beginTask(String name, int totalWork) {
 					// implements interface method
 				}
 				public void done() {
 					// implements interface method
 				}
 				public void internalWorked(double work) {
 					// implements interface method
 				}
 				public boolean isCanceled() {
 					return isCanceled;
 				}
 				public void setCanceled(boolean value) {
 					isCanceled = value;
 				}
 				public void setTaskName(String name) {
 					// implements interface method
 				}
 				public void subTask(String name) {
 					// implements interface method
 				}
 				public void worked(int work) {
 					// implements interface method
 				}
 			};
 			IRestrictedAccessDeclarationRequestor typeRequestor = new IRestrictedAccessDeclarationRequestor() {
 				public void acceptType(long modifiers, char[] packageName, char[] simpleTypeName, char[] templateParametersSignature, char[][] enclosingTypeNames, String path, int declarationStart, AccessRestriction access) {
 					if (excludePath != null && excludePath.equals(path))
 						return;
 					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
 						return; // accept only top level types
 					storage.acceptType(packageName, simpleTypeName, templateParametersSignature, enclosingTypeNames, modifiers, declarationStart, access);
 				}
				public void acceptField(long modifiers, char[] packageName, char[] name, char[] typeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {
 					if (excludePath != null && excludePath.equals(path))
 						return;
 					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
 						return; // accept only top level types
					storage.acceptField(packageName, name, typeName, enclosingTypeNames, modifiers, access);
 				}
 				public void acceptMethod(long modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, char[] signature, char[] templateParametersSignature, String path, int declarationStart, AccessRestriction access) {
 					if (excludePath != null && excludePath.equals(path))
 						return;
 					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
 						return; // accept only top level types
 					storage.acceptMethod(packageName, name, enclosingTypeNames, signature, templateParametersSignature, modifiers, declarationStart, access);
 				}
 			};
 			try {
 				int matchRule;
 				
 				if (exactMatch) {
 					matchRule = SearchPattern.R_PATTERN_MATCH;
 				} else {
 					matchRule = SearchPattern.R_PREFIX_MATCH;
 				}
 				
 				if (camelCaseMatch) {
 					matchRule |= SearchPattern.R_CAMELCASE_MATCH;
 				}
 				
 				new BasicSearchEngine(this.workingCopies).searchAllDeclarations(
 					qualification,
 					simpleName,
 					matchRule, // not case sensitive
 					IJavaSearchConstants.DECLARATIONS,
 					this.searchScope,
 					typeRequestor,
 					exactMatch ? WAIT_UNTIL_READY_TO_SEARCH : CANCEL_IF_NOT_READY_TO_SEARCH,
 					progressMonitor);
 			} catch (OperationCanceledException e) {
 				findTypes(
 					new String(prefix),
 					storage,
 					NameLookup.ACCEPT_ALL);
 			}
 		} catch (JavaModelException e) {
 			findTypes(
 				new String(prefix),
 				storage,
 				NameLookup.ACCEPT_ALL);
 		}
 	}
 	
 }
