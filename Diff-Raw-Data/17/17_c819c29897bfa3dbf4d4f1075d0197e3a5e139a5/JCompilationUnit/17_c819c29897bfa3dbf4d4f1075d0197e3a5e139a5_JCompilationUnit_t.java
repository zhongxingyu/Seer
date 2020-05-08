 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright � 2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: JCompilationUnit.java,v 1.21 2005-11-16 17:38:42 gasiunas Exp $
  */
 
 package org.caesarj.compiler.ast.phylum;
 
 import java.lang.ref.WeakReference;
 import java.util.Hashtable;
 
 import org.caesarj.compiler.ClassReader;
 import org.caesarj.compiler.CompilerBase;
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
 import org.caesarj.compiler.ast.visitor.IVisitor;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.context.CCompilationUnitContext;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.compiler.export.CCompilationUnit;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 
 /**
  * This class represents a virtual file and is the main entry point in java grammar
  */
 public class JCompilationUnit extends JPhylum {
 
 	// ----------------------------------------------------------------------
 	// CONSTRUCTORS
 	// ----------------------------------------------------------------------
 
 	/**
 	 * Constructs a CompilationUnit with the specified top level context
 	 * @param	where		the position of this token
 	 */
 	public JCompilationUnit(
 		TokenReference where,
 		KjcEnvironment environment,
 		JPackageName packageName,
 		JPackageImport[] importedPackages,
 		JClassImport[] importedClasses,
 		JTypeDeclaration[] typeDeclarations) {
 		super(where);
 		this.environment = new WeakReference<KjcEnvironment>(environment);
 		this.packageName = packageName;
 		this.importedPackages = importedPackages;
 		this.importedClasses = importedClasses;
 		this.typeDeclarations = typeDeclarations;
 		if (!packageName.isCollaboration()) {
 			generateInterfaces();
 		}
 	}
 
 	// ----------------------------------------------------------------------
 	// ACCESSORS
 	// ----------------------------------------------------------------------
 
 	/*
 	public String getPackageName() {
 		verify(packageName != null);
 		return packageName.getName();
 	}
 	*/
 	
 	public JPackageName getPackageName() {		
 		return packageName;
 	}
 	
 	public JClassImport[] getImportedClasses() {
 	    return importedClasses;
     }
 
 	public JPackageImport[] getImportedPackages() {
 	    return importedPackages;
     }
 
 	/**
 	 * @return	the name of the file associated with this compilation unit
 	 */
 	public String getFileName() {
 		return getTokenReference().getFile();
 	}
 
 	// ----------------------------------------------------------------------
 	// INTERFACE CHECKING
 	// ----------------------------------------------------------------------
 	
 	public void prepareCUContext(CompilerBase compiler) throws PositionedError {
 		for (int i = 0; i < importedClasses.length; i++) {
 			JClassImport ic = importedClasses[i];
 
 			if (ic.getQualifiedName().indexOf('/') < 0) {
 				compiler.reportTrouble(
 					new PositionedError(
						ic.getTokenReference(),
 						KjcMessages.IMPORT_UNNAMED_PACKAGE,
 						ic.getQualifiedName()));
 			}
 
 			CClass impClass =
 				getEnvironment().getClassReader().loadClass(
 						getEnvironment().getTypeFactory(),
 					ic.getQualifiedName());
 
 			if (impClass == null || impClass == CClass.CLS_UNDEFINED) {
 				// maybe InnerClass Import
 				impClass =
 					loadOuterClass(
 						compiler,
 						getEnvironment().getClassReader(),
 						getEnvironment().getTypeFactory(),
 						ic.getQualifiedName());
 
 				if (impClass == null || impClass == CClass.CLS_UNDEFINED) {
 					compiler.reportTrouble(
 						new PositionedError(
							ic.getTokenReference(),
 							KjcMessages.CLASS_UNKNOWN,
 							ic.getQualifiedName()));
 					if (impClass == null) {
 						continue;
 					}
 				}
 			}
 
 			// JLS 7.5.1 :
 			// The named type must be accessible (JLS 6.6) or a compile-time error occurs.
 			if (!impClass
 				.isAccessible(
 					getEnvironment()
 						.getTypeFactory()
 						.createReferenceType(TypeFactory.RFT_OBJECT)
 						.getCClass())
 				&& !impClass.getPackage().equals(packageName.getName())) {
 				compiler.reportTrouble(
 					new PositionedError(
						ic.getTokenReference(),
 						KjcMessages.IMPORT_UNACCESSIBLE,
 						ic.getQualifiedName()));
 			}
 
 			Object clazz = allLoadedClasses.put(ic.getSimpleName(), impClass);
 
 			if (clazz != null) {
 				// JLS 7.5.1 :
 				// If two single-type-import declarations in the same compilation
 				// unit attempt to import types with the same simple name, then a
 				// compile-time error occurs, unless the two types are the same type,
 				// in which case the duplicate declaration is ignored.
 				if (impClass.getCClass() != clazz) {
 					compiler.reportTrouble(
 						new PositionedError(
							ic.getTokenReference(),
 							KjcMessages.DUPLICATE_TYPE_NAME,
 							impClass.getIdent()));
 				}
 				else {
 					compiler.reportTrouble(
 						new CWarning(
							ic.getTokenReference(),
 							KjcMessages.DUPLICATE_CLASS_IMPORT,
 							ic.getQualifiedName()));
 				}
 			}
 		}
 		// JLS 7.5.2 Type-Import-on-Demand Declaration
 		// It is a compile-time error for a type-import-on-demand declaration 
 		// to name a type or package that is not accessible. 
 
 		// check uniquness of classes
 		for (int i = 0; i < typeDeclarations.length; i++) {
 			CClass object = typeDeclarations[i].getSourceClass();
 
 			Object clazz = allLoadedClasses.get(object.getIdent());
 
 			if (clazz == null) {
 				allLoadedClasses.put(object.getIdent(), object);
 			}
 			else {
 				if (clazz != object) {
 					compiler.reportTrouble(
 						new PositionedError(
 							getTokenReference(),
 							KjcMessages.DUPLICATE_TYPE_NAME,
 							object.getQualifiedName()));
 				}
 			}
 		}
 
 	}
 
 	/** 
 	 * In the pass the superclass of this class the interfaces must be set, 
 	 * so that they are  available for the next pass.
 	 * It is not possible to check the interface of methods, fields, ... in 
 	 * the same pass.
 	 */
 	public void join(CompilerBase compiler) throws PositionedError {
 		
 		if (packageName == JPackageName.UNNAMED) {
 			throw new PositionedError(
 					getTokenReference(),
 					KjcMessages.PACKAGE_IS_MISSING);
 		}
 		
 		for (int i = 0; i < typeDeclarations.length; i++) {
 			typeDeclarations[i].join(getContext());
 		}
 	}
 	
     // IVICA
     public void createImplicitCaesarTypes(CompilerBase compiler) throws PositionedError {
        for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].createImplicitCaesarTypes(getContext());
         }
     }
 
     // IVICA
     public void adjustSuperTypes(CompilerBase compiler) throws PositionedError {
         for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].adjustSuperType(getContext());
         }
     }
 
 	/**
 	 * Second pass (quick), check interface looks good
 	 * @exception	PositionedError	an error with reference to the source file
 	 */
     public void checkInterface(CompilerBase compiler) throws PositionedError {
         for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].checkInterface(getContext());
         }
     }
 
     public void checkDependentTypes(CompilerBase compiler) throws PositionedError {
         for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].checkDependentTypes(getContext());
         }
     }
 
     public void checkVirtualClassMethodSignatures(CompilerBase compiler) throws PositionedError {
         for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].checkVirtualClassMethodSignatures(getContext());
         }
     }
     
     public void completeCClassInterfaces(CompilerBase compiler) throws PositionedError {
         for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].completeCClassInterfaces(getContext());
         }
     }
 
 	private CClass loadOuterClass(
 		CompilerBase compiler,
 		ClassReader classReader,
 		TypeFactory typeFactory,
 		String qualifiedName)
 		throws PositionedError {
 		CClass outer = null;
 		int index = qualifiedName.lastIndexOf("/");
 		String name = qualifiedName; // for beautiful error msg
 
 		while ((index > 0)
 			&& (outer == null || outer == CClass.CLS_UNDEFINED)) {
 			qualifiedName =
 				qualifiedName.substring(0, index)
 					+ "$"
 					+ qualifiedName.substring(index + 1, qualifiedName.length());
 			outer = classReader.loadClass(typeFactory, qualifiedName);
 			index = qualifiedName.lastIndexOf("/");
 		}
 		if (index < 0) {
 			compiler.reportTrouble(
 				new PositionedError(
 					getTokenReference(),
 					KjcMessages.IMPORT_NOT_EXISTS,
 					name));
 		}
 
 		return outer;
 	}
 
 	/**
 	 * Second pass (quick), check interface looks good
 	 * Exceptions are not allowed here, this pass is just a tuning
 	 * pass in order to create informations about exported elements
 	 * such as Classes, Interfaces, Methods, Constructors and Fields
 	 * @return	true iff sub tree is correct enought to check code
 	 * @exception	PositionedError	an error with reference to the source file
 	 */
 	public void checkInitializers(CompilerBase compiler)
 		throws PositionedError {
 		for (int i = 0; i < typeDeclarations.length; i++) {
 			typeDeclarations[i].checkInitializers(getContext());
 		}
 	}
 
 	// ----------------------------------------------------------------------
 	// SEMANTIC ANALYSIS
 	// ----------------------------------------------------------------------
 
 	/**
 	 * Check expression and evaluate and alter context
 	 * @exception	PositionedError Error catched as soon as possible
 	 */
 	public void checkBody(CompilerBase compiler)
 		throws PositionedError {
 		for (int i = 0; i < typeDeclarations.length; i++) {
 			typeDeclarations[i].checkTypeBody(getContext());
 		}
 
 		// Check for unused class imports
 		for (int i = 0; i < importedClasses.length; i++) {
 			importedClasses[i].analyse(compiler);
 		}
 		// Check for unused package imports
 		for (int i = 0; i < importedPackages.length; i++) {
 			importedPackages[i].analyse(
 				compiler,
 				getEnvironment().getClassReader(),
 				getEnvironment().getTypeFactory(),
 				packageName);
 		}
 	}
 
 	public void analyseConditions() throws PositionedError {
 		for (int i = 0; i < typeDeclarations.length; i++) {
 			typeDeclarations[i].analyseConditions();
 		}
 	}
 
 	/**
 	 * Returns the environment.
 	 * @return KjcEnvironment
 	 */
 	public KjcEnvironment getEnvironment() {
 		return environment.get();
 	}
 
 	/**
 	 * Returns the export.
 	 * @return CCompilationUnit
 	 * Walter
 	 */
 	public CCompilationUnit getExport() {
 		if (export == null) {
 			export =
 				new CCompilationUnit(
 					getEnvironment(),
 					packageName.getName(),
 					importedClasses,
 					importedPackages,
 					this,
 					allLoadedClasses);
 		}
 		return export;
 	}
 	
 	/**
 	 * Creates compilation unit context
 	 * @return CCompilationUnitContext
 	 */
 	public CCompilationUnitContext getContext() {
 		if (context == null) {
 			context = new CCompilationUnitContext(getEnvironment().getCompiler(), getEnvironment(), getExport());
 		}
 		return context;
 	}
 
 	// ----------------------------------------------------------------------
 	// CODE GENERATION
 	// ----------------------------------------------------------------------
 
 	/**
 	 * Accepts the specified visitor
 	 * @param	p		the visitor
 	 */
 	public void recurse(IVisitor p) {
 		for (int i = 0; i < typeDeclarations.length; i++) {
             typeDeclarations[i].accept(p);
         }
 	}
 	
 	/**
 	 * Sets the collaboration class declaration, if this compilation unit
 	 * uses an externalized class.
 	 * 
 	 * @param collaboration the class declaration
 	 */
 	public void setCollaboration(CjVirtualClassDeclaration collaboration) {
 	    this.collaboration = collaboration;
 	}
 	  
 	/**
 	 * Gets the collaboration class declaration
 	 * 
 	 * @return
 	 */
 	public CjVirtualClassDeclaration getCollaboration() {
 	    return this.collaboration;
 	}
 	
 	/**
 	 * Returns all top level type declarations in the compilation unit. 
 	 * A copy of the array is returned.
 	 */
 	public JTypeDeclaration[] getInners() {
 		JTypeDeclaration[] innersCopy = new JTypeDeclaration[typeDeclarations.length];
 		System.arraycopy(typeDeclarations, 0, innersCopy, 0, typeDeclarations.length);
         return innersCopy;
 	}
 	
 	public void generateInterfaces() {
 		/* export inners */
         for(int i=0; i<typeDeclarations.length; i++) {
         	typeDeclarations[i].generateInterface(
         			getEnvironment().getClassReader(),
 	                getExport(),
 	                null,
 	                getPackageName().getName() + '/');
 	    }
     }
 	
 	public void addInners(JTypeDeclaration newDecls[]) {
         JTypeDeclaration newInners[] = new JTypeDeclaration[typeDeclarations.length + newDecls.length];
         System.arraycopy(typeDeclarations, 0, newInners, 0, typeDeclarations.length);
         System.arraycopy(newDecls, 0, newInners, typeDeclarations.length, newDecls.length);
         typeDeclarations = newInners;
         
         /* export inners */
         for(int i=0; i<newDecls.length; i++) {
         	newDecls[i].generateInterface(
         			getEnvironment().getClassReader(),
 	                getExport(),
 	                null,
 	                getPackageName().getName() + '/');
 	    }
     }
 
 	public JTypeDeclaration[] getOriginalInners() {
 	    return origTypeDeclarations;
 	}
 	
 	/**
 	 * When inners have been copied, store them in the origTypeDeclarations
 	 * and set the typeDeclarations to the empty array
 	 *
 	 */
 	public void fireInnersCopied() {
 	    this.origTypeDeclarations = this.typeDeclarations;
 	    this.typeDeclarations = new JTypeDeclaration[0];
 	}
 	  
 	// ----------------------------------------------------------------------
 	// DATA MEMBERS
 	// ----------------------------------------------------------------------
 	private JPackageName packageName;
 	private JClassImport[] importedClasses;
 	private JPackageImport[] importedPackages;
 	// andreas start
 	//private JTypeDeclaration[]		typeDeclarations;
 	protected JTypeDeclaration[] typeDeclarations;
 	protected JTypeDeclaration[] origTypeDeclarations;
 	private CjVirtualClassDeclaration collaboration;
 	// andreas end
 
 	private Hashtable allLoadedClasses = new Hashtable();
 	// $$$ DEFAULT VALUE IS OKAY ???
 	private CCompilationUnit export;
 	private CCompilationUnitContext context;
 	private WeakReference<KjcEnvironment> environment;
 }
