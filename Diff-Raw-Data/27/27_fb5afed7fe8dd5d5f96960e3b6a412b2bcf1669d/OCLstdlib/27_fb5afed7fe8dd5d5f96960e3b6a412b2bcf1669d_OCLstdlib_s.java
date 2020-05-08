 /**
  * <copyright>
  *
  * Copyright (c) 2010,2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * This code is auto-generated
  * from: /org.eclipse.ocl.examples.library/model/OCL-2.4.oclstdlib
  * by: org.eclipse.ocl.examples.build.acceleo.GenerateOCLstdlib
  * defined by: org.eclipse.ocl.examples.build.acceleo.generateOCLstdlib.mtl
  * invoked by: org.eclipse.ocl.examples.build.utilities.OCLstdlibCodeGenerator
  * from: org.eclipse.ocl.examples.build.GenerateOCLstdlibModel.mwe2
  *
  * Do not edit it.
  */
 package	org.eclipse.ocl.examples.pivot.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.pivot.*;
 import org.eclipse.ocl.examples.pivot.Class;
 import org.eclipse.ocl.examples.pivot.Package;
 import org.eclipse.ocl.examples.pivot.library.StandardLibraryContribution;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 
 /**
  * This is the http://www.eclipse.org/ocl/3.1.0/OCL.oclstdlib Standard Library
  * auto-generated from /org.eclipse.ocl.examples.library/model/OCL-2.4.oclstdlib.
  * It facilitates efficient library loading without the overheads of model reading.
  * <p>
  * This Standard Library may be registered as the definition of a Standard Library for
  * the OCL evaluation framework by invoking {@link #install}.
  * <p>
  * The Standard Library is normally activated when the MetaModelManager attempts
  * to locate a library type when its default Standard Library URI is the same
  * as this Standard Library.
  */
 @SuppressWarnings({"nls", "unused"})
 public class OCLstdlib extends XMIResourceImpl
 {
 	/**
 	 *	The static package-of-types pivot model of the Standard Library.
 	 */
 	private static OCLstdlib INSTANCE = null;
 	
 	/**
 	 *	The URI of this Standard Library.
 	 */
 	public static final @NonNull String STDLIB_URI = "http://www.eclipse.org/ocl/3.1.0/OCL.oclstdlib";
 
 	/**
 	 * Return the default OCL standard Library. 
 	 *  This static definition auto-generated from /org.eclipse.ocl.examples.library/model/OCL-2.4.oclstdlib
 	 *  is used as the default when no overriding copy is registered. 
 	 */
 	public static @NonNull OCLstdlib getDefault() {
 		OCLstdlib oclstdlib = INSTANCE;
 		if (oclstdlib == null) {
 			Contents contents = new Contents();
 			Root libraryModel = contents.create("http://www.eclipse.org/ocl/3.1.0/OCL.oclstdlib", "ocl", "ocl", "http://www.eclipse.org/ocl/3.1.0/OCL.oclstdlib");
 			oclstdlib = INSTANCE = new OCLstdlib(STDLIB_URI, libraryModel);
 		}
 		return oclstdlib;
 	}
 
 	/**
 	 * Return true if the default OCL standard Library has been created. 
 	 */
 	public static boolean hasDefault() {
 		return INSTANCE != null;
 	}
 
 	/**
 	 * Install this library in the {@link StandardLibraryContribution#REGISTRY}.
 	 * This method may be invoked by standalone applications to replicate
 	 * the registration that should appear as a standard_library plugin
 	 * extension when running within Eclipse. 
 	 */
 	public static void install() {
 		StandardLibraryContribution.REGISTRY.put(STDLIB_URI, new Loader());
 	}
 
 	/**
 	 * Unnstall this library from the {@link StandardLibraryContribution#REGISTRY}.
 	 * This method may be invoked by standalone applications to release the library
 	 * resources for garbage collection and memory leakage detection. 
 	 */
 	public static void uninstall() {
 		StandardLibraryContribution.REGISTRY.remove(STDLIB_URI);
 		INSTANCE = null;
 	}
 	
 	/**
 	 * The Loader shares the Standard Library instance whenever this default library
 	 * is loaded from the registry of Standard Libraries populated by the standard_library 
 	 * extension point.
 	 */
 	public static class Loader implements StandardLibraryContribution
 	{
 		public StandardLibraryContribution getContribution() {
 			return this;
 		}
 		
 		public @NonNull Resource getResource() {
 			return getDefault();
 		}
 	}
 	
 	/**
 	 *	Construct a copy of the OCL Standard Library with specified resource URI,
 	 *  and package name, prefix and namespace URI.
 	 */
 	public static @NonNull OCLstdlib create(@NonNull String uri, @NonNull String name, @NonNull String nsPrefix, @NonNull String nsURI) {
 		Contents contents = new Contents();
 		Root libraryModel = contents.create(uri, name, nsPrefix, nsURI);
 		return new OCLstdlib(uri, libraryModel);
 	}
 	
 	/**
 	 *	Construct an OCL Standard Library with specified resource URI and library content.
 	 */
 	public OCLstdlib(@NonNull String uri, @NonNull Root libraryModel) {
 		super(URI.createURI(uri));
 		getContents().add(libraryModel);
 //		System.out.println(Thread.currentThread().getName() + " Create " + debugSimpleName(this));		
 //		liveOCLstdlibs.put(this, null);
 	}
 
 	protected static class Contents extends AbstractContents
 	{
 		protected Root root;
 		protected Library library;
 		// protected Package orphans;
 
 		protected @NonNull Root create(@NonNull String uri, @NonNull String name, @NonNull String nsPrefix, @NonNull String nsURI)
 		{
 			Root theRoot = root = createRoot("OCL-2.4.oclstdlib", uri);
 			library = createLibrary(name, nsPrefix, nsURI);
 			installPackages();
 			installOclTypes();
 			installPrimitiveTypes();
 			installParameterTypes();
 			installCollectionTypes();
 			installMetaclasses();
 			installLambdaTypes();
 			installTupleTypes();
 			installOperations();
 			installIterations();
 			installProperties();
 			installTemplateSignatures();
 			installTemplateBindings();
 			installPrecedences();
 			installComments();
 			return theRoot;
 		}
 	
 		protected final @NonNull Package orphans = createPackage("$$", null, "http://www.eclipse.org/ocl/3.1.0/orphanage");
 		
 		protected void installPackages() {
 			root.getNestedPackage().add(orphans);
 			root.getNestedPackage().add(library);	
 		}
 		
 		protected final @NonNull Class _Class = createClass("Class");
 		protected final @NonNull Class _Enumeration = createClass("Enumeration");
 		protected final @NonNull Class _EnumerationLiteral = createClass("EnumerationLiteral");
 		protected final @NonNull AnyType _OclAny = createAnyType("OclAny");
 		protected final @NonNull Class _OclComparable = createClass("OclComparable");
 		protected final @NonNull Class _OclElement = createClass("OclElement");
 		protected final @NonNull InvalidType _OclInvalid = createInvalidType("OclInvalid");
 		protected final @NonNull Class _OclLambda = createClass("OclLambda");
 		protected final @NonNull Class _OclMessage = createClass("OclMessage");
 		protected final @NonNull SelfType _OclSelf = createSelfType("OclSelf");
 		protected final @NonNull Class _OclState = createClass("OclState");
 		protected final @NonNull Class _OclSummable = createClass("OclSummable");
 		protected final @NonNull Class _OclTuple = createClass("OclTuple");
 		protected final @NonNull Class _OclType = createClass("OclType");
 		protected final @NonNull VoidType _OclVoid = createVoidType("OclVoid");
 		protected final @NonNull Class _State = createClass("State");
 		protected final @NonNull Class _Type = createClass("Type");
 		protected final @NonNull AnyType _$$ = createAnyType("$$");
 		
 		protected final @NonNull PrimitiveType _Boolean = createPrimitiveType("Boolean");
 		protected final @NonNull PrimitiveType _Integer = createPrimitiveType("Integer");
 		protected final @NonNull PrimitiveType _Real = createPrimitiveType("Real");
 		protected final @NonNull PrimitiveType _String = createPrimitiveType("String");
 		protected final @NonNull PrimitiveType _UnlimitedNatural = createPrimitiveType("UnlimitedNatural");
 		
 		protected final @NonNull Class _UnlimitedNatural_oclAsType_TT = createClass("TT");
 		protected final @NonNull Class _Bag_collectNested_V = createClass("V");
 		protected final @NonNull Class _Bag_collect_V = createClass("V");
 		protected final @NonNull Class _Bag_flatten_T2 = createClass("T2");
 		protected final @NonNull Class _Bag_selectByKind_TT = createClass("TT");
 		protected final @NonNull Class _Bag_selectByType_TT = createClass("TT");
 		protected final @NonNull Class _Bag_T = createClass("T");
 		protected final @NonNull Class _Collection_collectNested_V = createClass("V");
 		protected final @NonNull Class _Collection_collect_V = createClass("V");
 		protected final @NonNull Class _Collection_excludesAll_T2 = createClass("T2");
 		protected final @NonNull Class _Collection_flatten_T2 = createClass("T2");
 		protected final @NonNull Class _Collection_includesAll_T2 = createClass("T2");
 		protected final @NonNull Class _Collection_iterate_Tacc = createClass("Tacc");
 		protected final @NonNull Class _Collection_product_T2 = createClass("T2");
 		protected final @NonNull Class _Collection_selectByKind_TT = createClass("TT");
 		protected final @NonNull Class _Collection_selectByType_TT = createClass("TT");
 		protected final @NonNull Class _Collection_T = createClass("T");
 		protected final @NonNull Class _Metaclass_T = createClass("T");
 		protected final @NonNull Class _OclAny_oclAsType_TT = createClass("TT");
 		protected final @NonNull Class _OclAny_oclIsKindOf_T = createClass("T");
 		protected final @NonNull Class _OclAny_oclIsTypeOf_T = createClass("T");
 		protected final @NonNull Class _OclInvalid_oclAsType_TT = createClass("TT");
 		protected final @NonNull Class _OclInvalid_oclIsKindOf_T = createClass("T");
 		protected final @NonNull Class _OclInvalid_oclIsTypeOf_T = createClass("T");
 		protected final @NonNull Class _OrderedSet_flatten_T2 = createClass("T2");
 		protected final @NonNull Class _OrderedSet_selectByKind_TT = createClass("TT");
 		protected final @NonNull Class _OrderedSet_selectByType_TT = createClass("TT");
 		protected final @NonNull Class _OrderedSet_T = createClass("T");
 		protected final @NonNull Class _Sequence_collectNested_V = createClass("V");
 		protected final @NonNull Class _Sequence_collect_V = createClass("V");
 		protected final @NonNull Class _Sequence_flatten_T2 = createClass("T2");
 		protected final @NonNull Class _Sequence_selectByKind_TT = createClass("TT");
 		protected final @NonNull Class _Sequence_selectByType_TT = createClass("TT");
 		protected final @NonNull Class _Sequence_T = createClass("T");
 		protected final @NonNull Class _Set_flatten_T2 = createClass("T2");
 		protected final @NonNull Class _Set_selectByKind_TT = createClass("TT");
 		protected final @NonNull Class _Set_selectByType_TT = createClass("TT");
 		protected final @NonNull Class _Set_T = createClass("T");
 		protected final @NonNull Class _UniqueCollection_T = createClass("T");
 		
 		protected final @NonNull BagType _Bag_Tuple = createBagType("Bag"/*Tuple*/, "0", "*");
 		protected final @NonNull BagType _Bag_Bag_collect_V = createBagType("Bag"/*V*/, "0", "*");
 		protected final @NonNull BagType _Bag_Bag_flatten_T2 = createBagType("Bag"/*T2*/, "0", "*");
 		protected final @NonNull BagType _Bag_Bag_selectByKind_TT = createBagType("Bag"/*TT*/, "0", "*");
 		protected final @NonNull BagType _Bag_Bag_selectByType_TT = createBagType("Bag"/*TT*/, "0", "*");
 		protected final @NonNull BagType _Bag_Collection_T = createBagType("Bag"/*T*/, "0", "*");
 		protected final @NonNull BagType _Bag_Set_selectByKind_TT = createBagType("Bag"/*TT*/, "0", "*");
 		protected final @NonNull BagType _Bag_Set_selectByType_TT = createBagType("Bag"/*TT*/, "0", "*");
 		protected final @NonNull BagType _Bag_Set_T = createBagType("Bag"/*T*/, "0", "*");
 		protected final @NonNull BagType _Bag_UniqueCollection_T = createBagType("Bag"/*T*/, "0", "*");
 		protected final @NonNull BagType _Bag_Bag_T = createBagType("Bag"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Integer = createCollectionType("Collection"/*Integer*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Tuple = createCollectionType("Collection"/*Tuple*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Bag_collect_V = createCollectionType("Collection"/*V*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Bag_selectByKind_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Bag_selectByType_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Bag_T = createCollectionType("Collection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_collect_V = createCollectionType("Collection"/*V*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_excludesAll_T2 = createCollectionType("Collection"/*T2*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_flatten_T2 = createCollectionType("Collection"/*T2*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_includesAll_T2 = createCollectionType("Collection"/*T2*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_product_T2 = createCollectionType("Collection"/*T2*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_selectByKind_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_selectByType_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_OrderedSet_selectByKind_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_OrderedSet_selectByType_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Sequence_collect_V = createCollectionType("Collection"/*V*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Sequence_selectByKind_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Sequence_selectByType_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Sequence_T = createCollectionType("Collection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Set_selectByKind_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Set_selectByType_TT = createCollectionType("Collection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Set_T = createCollectionType("Collection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_UniqueCollection_T = createCollectionType("Collection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _Collection_Collection_T = createCollectionType("Collection"/*T*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_Collection_T = createOrderedSetType("OrderedSet"/*T*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_EnumerationLiteral = createOrderedSetType("OrderedSet"/*EnumerationLiteral*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_OrderedSet_flatten_T2 = createOrderedSetType("OrderedSet"/*T2*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_OrderedSet_selectByKind_TT = createOrderedSetType("OrderedSet"/*TT*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_OrderedSet_selectByType_TT = createOrderedSetType("OrderedSet"/*TT*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_Sequence_T = createOrderedSetType("OrderedSet"/*T*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_Set_T = createOrderedSetType("OrderedSet"/*T*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_UniqueCollection_T = createOrderedSetType("OrderedSet"/*T*/, "0", "*");
 		protected final @NonNull OrderedSetType _OrderedSet_OrderedSet_T = createOrderedSetType("OrderedSet"/*T*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Integer = createSequenceType("Sequence"/*Integer*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_String = createSequenceType("Sequence"/*String*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Bag_T = createSequenceType("Sequence"/*T*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Collection_T = createSequenceType("Sequence"/*T*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_OrderedSet_selectByKind_TT = createSequenceType("Sequence"/*TT*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_OrderedSet_selectByType_TT = createSequenceType("Sequence"/*TT*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_OrderedSet_T = createSequenceType("Sequence"/*T*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Sequence_collect_V = createSequenceType("Sequence"/*V*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Sequence_flatten_T2 = createSequenceType("Sequence"/*T2*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Sequence_selectByKind_TT = createSequenceType("Sequence"/*TT*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Sequence_selectByType_TT = createSequenceType("Sequence"/*TT*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Set_T = createSequenceType("Sequence"/*T*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_UniqueCollection_T = createSequenceType("Sequence"/*T*/, "0", "*");
 		protected final @NonNull SequenceType _Sequence_Sequence_T = createSequenceType("Sequence"/*T*/, "0", "*");
 		protected final @NonNull SetType _Set_Tuple = createSetType("Set"/*Tuple*/, "0", "*");
 		protected final @NonNull SetType _Set_Bag_T = createSetType("Set"/*T*/, "0", "*");
 		protected final @NonNull SetType _Set_Collection_T = createSetType("Set"/*T*/, "0", "*");
 		protected final @NonNull SetType _Set_OclElement = createSetType("Set"/*OclElement*/, "0", "*");
 		protected final @NonNull SetType _Set_OclSelf = createSetType("Set"/*OclSelf*/, "0", "*");
 		protected final @NonNull SetType _Set_Set_flatten_T2 = createSetType("Set"/*T2*/, "0", "*");
 		protected final @NonNull SetType _Set_Set_selectByKind_TT = createSetType("Set"/*TT*/, "0", "*");
 		protected final @NonNull SetType _Set_Set_selectByType_TT = createSetType("Set"/*TT*/, "0", "*");
 		protected final @NonNull SetType _Set_UniqueCollection_T = createSetType("Set"/*T*/, "0", "*");
 		protected final @NonNull SetType _Set_Set_T = createSetType("Set"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_Tuple = createCollectionType("UniqueCollection"/*Tuple*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_Collection_T = createCollectionType("UniqueCollection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_OclAny = createCollectionType("UniqueCollection"/*OclAny*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_OrderedSet_selectByKind_TT = createCollectionType("UniqueCollection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_OrderedSet_selectByType_TT = createCollectionType("UniqueCollection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_OrderedSet_T = createCollectionType("UniqueCollection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_Set_selectByKind_TT = createCollectionType("UniqueCollection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_Set_selectByType_TT = createCollectionType("UniqueCollection"/*TT*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_Set_T = createCollectionType("UniqueCollection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_UniqueCollection_T = createCollectionType("UniqueCollection"/*T*/, "0", "*");
 		protected final @NonNull CollectionType _UniqueCollection_UniqueCollection_T_1 = createCollectionType("UniqueCollection"/*T*/, "0", "*");
 		
 		protected final @NonNull Metaclass _Metaclass_UnlimitedNatural_oclAsType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Bag_selectByKind_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Bag_selectByType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Collection_selectByKind_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Collection_selectByType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclAny_oclAsType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclAny_oclIsKindOf_T = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclAny_oclIsTypeOf_T = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclInvalid_oclAsType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclInvalid_oclIsKindOf_T = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclInvalid_oclIsTypeOf_T = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclInvalid = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclSelf = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OclVoid = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OrderedSet_selectByKind_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_OrderedSet_selectByType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Sequence_selectByKind_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Sequence_selectByType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Set_selectByKind_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass_Set_selectByType_TT = createMetaclass("Metaclass");
 		protected final @NonNull Metaclass _Metaclass = createMetaclass("Metaclass");
 		
 		protected final @NonNull TupleType _Tuple = createTupleType("Tuple",
 			createProperty("first", _Collection_T),
 			createProperty("second", _Collection_product_T2));
 		
 		protected void installOclTypes() {
 			final List<Type> ownedTypes = library.getOwnedType();
 			Type type;
 			List<Type> superClasses;
 			ownedTypes.add(type = _Class);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _Enumeration);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _EnumerationLiteral);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclAny);
 			ownedTypes.add(type = _OclComparable);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclElement);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclInvalid);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclVoid);
 			ownedTypes.add(type = _OclLambda);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclMessage);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclSelf);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclState);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclSummable);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclTuple);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _OclType);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			ownedTypes.add(type = _OclVoid);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _State);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclState);
 			ownedTypes.add(type = _Type);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclType);
 			ownedTypes.add(type = _$$);
 		}
 		
 		protected void installPrimitiveTypes() {
 			final List<Type> ownedTypes = library.getOwnedType();
 			PrimitiveType type;
 			ownedTypes.add(type = _Boolean);
 			type.getSuperClass().add(_OclAny);
 			ownedTypes.add(type = _Integer);
 			type.getSuperClass().add(_Real);
 			ownedTypes.add(type = _Real);
 			type.getSuperClass().add(_OclComparable);
 			type.getSuperClass().add(_OclSummable);
 			ownedTypes.add(type = _String);
 			type.getSuperClass().add(_OclComparable);
 			type.getSuperClass().add(_OclSummable);
 			ownedTypes.add(type = _UnlimitedNatural);
 			type.getSuperClass().add(_Integer);
 		}
 		
 		protected void installParameterTypes() {
 		}
 		
 		protected void installCollectionTypes() {
 			final List<Type> ownedTypes = library.getOwnedType();
 			final List<Type> orphanTypes = orphans.getOwnedType();
 			CollectionType type;
 			List<Type> superClasses;
 			orphanTypes.add(type = _Bag_Tuple);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Tuple);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Tuple);
 			orphanTypes.add(type = _Bag_Bag_collect_V);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Bag_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Bag_collect_V);
 			orphanTypes.add(type = _Bag_Bag_flatten_T2);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Bag_flatten_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Bag_Bag_selectByKind_TT);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Bag_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Bag_selectByKind_TT);
 			orphanTypes.add(type = _Bag_Bag_selectByType_TT);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Bag_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Bag_selectByType_TT);
 			orphanTypes.add(type = _Bag_Collection_T);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Collection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Bag_Set_selectByKind_TT);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Set_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Set_selectByKind_TT);
 			orphanTypes.add(type = _Bag_Set_selectByType_TT);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Set_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Set_selectByType_TT);
 			orphanTypes.add(type = _Bag_Set_T);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_Set_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Bag_UniqueCollection_T);
 			type.setUnspecializedElement(_Bag_Bag_T);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_UniqueCollection_T);
 			ownedTypes.add(type = _Bag_Bag_T);
 			type.setElementType(_Bag_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Bag_T);
 			orphanTypes.add(type = _Collection_Integer);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Integer);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Tuple);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Tuple);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Bag_collect_V);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Bag_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Bag_selectByKind_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Bag_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Bag_selectByType_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Bag_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Bag_T);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Bag_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_collect_V);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_excludesAll_T2);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_excludesAll_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_flatten_T2);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_flatten_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_includesAll_T2);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_includesAll_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_product_T2);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_product_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_selectByKind_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Collection_selectByType_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Collection_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_OrderedSet_selectByKind_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_OrderedSet_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_OrderedSet_selectByType_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_OrderedSet_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Sequence_collect_V);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Sequence_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Sequence_selectByKind_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Sequence_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Sequence_selectByType_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Sequence_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Sequence_T);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Sequence_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Set_selectByKind_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Set_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Set_selectByType_TT);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Set_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_Set_T);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_Set_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _Collection_UniqueCollection_T);
 			type.setUnspecializedElement(_Collection_Collection_T);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			ownedTypes.add(type = _Collection_Collection_T);
 			type.setElementType(_Collection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclAny);
 			orphanTypes.add(type = _OrderedSet_Collection_T);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_Collection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _OrderedSet_EnumerationLiteral);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_EnumerationLiteral);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _OrderedSet_OrderedSet_flatten_T2);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_OrderedSet_flatten_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _OrderedSet_OrderedSet_selectByKind_TT);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_OrderedSet_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Sequence_OrderedSet_selectByKind_TT);
 			superClasses.add(_UniqueCollection_OrderedSet_selectByKind_TT);
 			orphanTypes.add(type = _OrderedSet_OrderedSet_selectByType_TT);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_OrderedSet_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Sequence_OrderedSet_selectByType_TT);
 			superClasses.add(_UniqueCollection_OrderedSet_selectByType_TT);
 			orphanTypes.add(type = _OrderedSet_Sequence_T);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_Sequence_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _OrderedSet_Set_T);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_Set_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Sequence_Set_T);
 			superClasses.add(_UniqueCollection_Set_T);
 			orphanTypes.add(type = _OrderedSet_UniqueCollection_T);
 			type.setUnspecializedElement(_OrderedSet_OrderedSet_T);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Sequence_UniqueCollection_T);
 			superClasses.add(_UniqueCollection_UniqueCollection_T);
 			ownedTypes.add(type = _OrderedSet_OrderedSet_T);
 			type.setElementType(_OrderedSet_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Sequence_OrderedSet_T);
 			superClasses.add(_UniqueCollection_OrderedSet_T);
 			orphanTypes.add(type = _Sequence_Integer);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Integer);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Integer);
 			orphanTypes.add(type = _Sequence_String);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_String);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Sequence_Bag_T);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Bag_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Sequence_Collection_T);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Collection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Sequence_OrderedSet_selectByKind_TT);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_OrderedSet_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_OrderedSet_selectByKind_TT);
 			orphanTypes.add(type = _Sequence_OrderedSet_selectByType_TT);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_OrderedSet_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_OrderedSet_selectByType_TT);
 			orphanTypes.add(type = _Sequence_OrderedSet_T);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_OrderedSet_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Sequence_Sequence_collect_V);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Sequence_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Sequence_collect_V);
 			orphanTypes.add(type = _Sequence_Sequence_flatten_T2);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Sequence_flatten_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Sequence_Sequence_selectByKind_TT);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Sequence_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Sequence_selectByKind_TT);
 			orphanTypes.add(type = _Sequence_Sequence_selectByType_TT);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Sequence_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Sequence_selectByType_TT);
 			orphanTypes.add(type = _Sequence_Set_T);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_Set_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Set_T);
 			orphanTypes.add(type = _Sequence_UniqueCollection_T);
 			type.setUnspecializedElement(_Sequence_Sequence_T);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_UniqueCollection_T);
 			ownedTypes.add(type = _Sequence_Sequence_T);
 			type.setElementType(_Sequence_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Sequence_T);
 			orphanTypes.add(type = _Set_Tuple);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_Tuple);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Bag_Tuple);
 			superClasses.add(_UniqueCollection_Tuple);
 			orphanTypes.add(type = _Set_Bag_T);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_Bag_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Set_Collection_T);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_Collection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Set_OclElement);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_OclElement);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Set_OclSelf);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_OclSelf);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Set_Set_flatten_T2);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_Set_flatten_T2);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _Set_Set_selectByKind_TT);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_Set_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Bag_Set_selectByKind_TT);
 			superClasses.add(_UniqueCollection_Set_selectByKind_TT);
 			orphanTypes.add(type = _Set_Set_selectByType_TT);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_Set_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Bag_Set_selectByType_TT);
 			superClasses.add(_UniqueCollection_Set_selectByType_TT);
 			orphanTypes.add(type = _Set_UniqueCollection_T);
 			type.setUnspecializedElement(_Set_Set_T);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Bag_UniqueCollection_T);
 			superClasses.add(_UniqueCollection_UniqueCollection_T);
 			ownedTypes.add(type = _Set_Set_T);
 			type.setElementType(_Set_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Bag_Set_T);
 			superClasses.add(_UniqueCollection_Set_T);
 			orphanTypes.add(type = _UniqueCollection_Tuple);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_Tuple);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Tuple);
 			orphanTypes.add(type = _UniqueCollection_Collection_T);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_Collection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _UniqueCollection_OclAny);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _UniqueCollection_OrderedSet_selectByKind_TT);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_OrderedSet_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_OrderedSet_selectByKind_TT);
 			orphanTypes.add(type = _UniqueCollection_OrderedSet_selectByType_TT);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_OrderedSet_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_OrderedSet_selectByType_TT);
 			orphanTypes.add(type = _UniqueCollection_OrderedSet_T);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_OrderedSet_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _UniqueCollection_Set_selectByKind_TT);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_Set_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Set_selectByKind_TT);
 			orphanTypes.add(type = _UniqueCollection_Set_selectByType_TT);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_Set_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_Set_selectByType_TT);
 			orphanTypes.add(type = _UniqueCollection_Set_T);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_Set_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclElement);
 			orphanTypes.add(type = _UniqueCollection_UniqueCollection_T);
 			type.setUnspecializedElement(_UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_UniqueCollection_T);
 			ownedTypes.add(type = _UniqueCollection_UniqueCollection_T_1);
 			type.setElementType(_UniqueCollection_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Collection_UniqueCollection_T);
 		}
 		
 		protected void installMetaclasses() {
 			final List<Type> ownedTypes = library.getOwnedType();
 			final List<Type> orphanTypes = orphans.getOwnedType();
 			Metaclass type;
 			List<Type> superClasses;
 			orphanTypes.add(type = _Metaclass_UnlimitedNatural_oclAsType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_UnlimitedNatural_oclAsType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Bag_selectByKind_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Bag_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Bag_selectByType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Bag_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Collection_selectByKind_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Collection_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Collection_selectByType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Collection_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclAny_oclAsType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclAny_oclAsType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclAny_oclIsKindOf_T);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclAny_oclIsKindOf_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclAny_oclIsTypeOf_T);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclAny_oclIsTypeOf_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclInvalid_oclAsType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclInvalid_oclAsType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclInvalid_oclIsKindOf_T);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclInvalid_oclIsKindOf_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclInvalid_oclIsTypeOf_T);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclInvalid_oclIsTypeOf_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclInvalid);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclInvalid);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclSelf);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclSelf);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OclVoid);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OclVoid);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OrderedSet_selectByKind_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OrderedSet_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_OrderedSet_selectByType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_OrderedSet_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Sequence_selectByKind_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Sequence_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Sequence_selectByType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Sequence_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Set_selectByKind_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Set_selectByKind_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			orphanTypes.add(type = _Metaclass_Set_selectByType_TT);
 			type.setUnspecializedElement(_Metaclass);
 			type.setInstanceType(_Set_selectByType_TT);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 			ownedTypes.add(type = _Metaclass);
 			type.setInstanceType(_Metaclass_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_Class);
 			superClasses.add(_OclType);
 		}
 		
 		protected void installTupleTypes() {
 			final List<Type> orphanTypes = orphans.getOwnedType();
 			TupleType type;
 			List<Type> superClasses;
 			orphanTypes.add(type = _Tuple);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclTuple);
 		}
 		
 		protected final @NonNull LambdaType _Lambda_Bag_T = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Bag_T_1 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Bag_T_2 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Bag_T_3 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Bag_T_4 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Collection_T = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Collection_T_1 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Collection_T_2 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Collection_T_3 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Collection_T_4 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_OrderedSet_T = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_OrderedSet_T_1 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Sequence_T = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Sequence_T_1 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Sequence_T_2 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Sequence_T_3 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Sequence_T_4 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Set_T = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_Set_T_1 = createLambdaType("Lambda");
 		protected final @NonNull LambdaType _Lambda_UniqueCollection_T = createLambdaType("Lambda");
 		
 		protected void installLambdaTypes() {
 			final List<Type> orphanTypes = orphans.getOwnedType();
 			LambdaType type;
 			List<Type> superClasses;
 			orphanTypes.add(type = _Lambda_Bag_T);
 			type.setContextType(_Bag_T);
 			type.setResultType(_Boolean);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Bag_T_1);
 			type.setContextType(_Bag_T);
 			type.setResultType(_Bag_collectNested_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Bag_T_2);
 			type.setContextType(_Bag_T);
 			type.setResultType(_Bag_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Bag_T_3);
 			type.setContextType(_Bag_T);
 			type.setResultType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Bag_T_4);
 			type.setContextType(_Bag_T);
 			type.setResultType(_Set_Bag_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Collection_T);
 			type.setContextType(_Collection_T);
 			type.setResultType(_Boolean);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Collection_T_1);
 			type.setContextType(_Collection_T);
 			type.setResultType(_Collection_collectNested_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Collection_T_2);
 			type.setContextType(_Collection_T);
 			type.setResultType(_Collection_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Collection_T_3);
 			type.setContextType(_Collection_T);
 			type.setResultType(_Collection_iterate_Tacc);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Collection_T_4);
 			type.setContextType(_Collection_T);
 			type.setResultType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_OrderedSet_T);
 			type.setContextType(_OrderedSet_T);
 			type.setResultType(_Boolean);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_OrderedSet_T_1);
 			type.setContextType(_OrderedSet_T);
 			type.setResultType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Sequence_T);
 			type.setContextType(_Sequence_T);
 			type.setResultType(_Boolean);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Sequence_T_1);
 			type.setContextType(_Sequence_T);
 			type.setResultType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Sequence_T_2);
 			type.setContextType(_Sequence_T);
 			type.setResultType(_OrderedSet_Sequence_T);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Sequence_T_3);
 			type.setContextType(_Sequence_T);
 			type.setResultType(_Sequence_collectNested_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Sequence_T_4);
 			type.setContextType(_Sequence_T);
 			type.setResultType(_Sequence_collect_V);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Set_T);
 			type.setContextType(_Set_T);
 			type.setResultType(_Boolean);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_Set_T_1);
 			type.setContextType(_Set_T);
 			type.setResultType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 			orphanTypes.add(type = _Lambda_UniqueCollection_T);
 			type.setContextType(_UniqueCollection_T);
 			type.setResultType(_OclAny);
 			superClasses = type.getSuperClass();
 			superClasses.add(_OclLambda);
 		}
 			
 		protected final @NonNull Operation op_Boolean__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_allInstances = createOperation("allInstances", _Set_OclSelf, "org.eclipse.ocl.examples.library.logical.BooleanAllInstancesOperation", org.eclipse.ocl.examples.library.logical.BooleanAllInstancesOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_and = createOperation("and", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanAndOperation", org.eclipse.ocl.examples.library.logical.BooleanAndOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_implies = createOperation("implies", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanImpliesOperation", org.eclipse.ocl.examples.library.logical.BooleanImpliesOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_not = createOperation("not", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanNotOperation", org.eclipse.ocl.examples.library.logical.BooleanNotOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_or = createOperation("or", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanOrOperation", org.eclipse.ocl.examples.library.logical.BooleanOrOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_Boolean_xor = createOperation("xor", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanXorOperation", org.eclipse.ocl.examples.library.logical.BooleanXorOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer__mul_ = createOperation("*", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericTimesOperation", org.eclipse.ocl.examples.library.numeric.NumericTimesOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer__add_ = createOperation("+", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericPlusOperation", org.eclipse.ocl.examples.library.numeric.NumericPlusOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer__neg_ = createOperation("-", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericNegateOperation", org.eclipse.ocl.examples.library.numeric.NumericNegateOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer__neg__1 = createOperation("-", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericMinusOperation", org.eclipse.ocl.examples.library.numeric.NumericMinusOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer__div_ = createOperation("/", _Real, "org.eclipse.ocl.examples.library.numeric.NumericDivideOperation", org.eclipse.ocl.examples.library.numeric.NumericDivideOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_abs = createOperation("abs", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericAbsOperation", org.eclipse.ocl.examples.library.numeric.NumericAbsOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_compareTo = createOperation("compareTo", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericCompareToOperation", org.eclipse.ocl.examples.library.numeric.NumericCompareToOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_div = createOperation("div", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericDivOperation", org.eclipse.ocl.examples.library.numeric.NumericDivOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_max = createOperation("max", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericMaxOperation", org.eclipse.ocl.examples.library.numeric.NumericMaxOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_min = createOperation("min", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericMinOperation", org.eclipse.ocl.examples.library.numeric.NumericMinOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_mod = createOperation("mod", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericModOperation", org.eclipse.ocl.examples.library.numeric.NumericModOperation.INSTANCE);
 		protected final @NonNull Operation op_Integer_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__mul_ = createOperation("*", _Real, "org.eclipse.ocl.examples.library.numeric.NumericTimesOperation", org.eclipse.ocl.examples.library.numeric.NumericTimesOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__add_ = createOperation("+", _Real, "org.eclipse.ocl.examples.library.numeric.NumericPlusOperation", org.eclipse.ocl.examples.library.numeric.NumericPlusOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__neg_ = createOperation("-", _Real, "org.eclipse.ocl.examples.library.numeric.NumericNegateOperation", org.eclipse.ocl.examples.library.numeric.NumericNegateOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__neg__1 = createOperation("-", _Real, "org.eclipse.ocl.examples.library.numeric.NumericMinusOperation", org.eclipse.ocl.examples.library.numeric.NumericMinusOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__div_ = createOperation("/", _Real, "org.eclipse.ocl.examples.library.numeric.NumericDivideOperation", org.eclipse.ocl.examples.library.numeric.NumericDivideOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__lt_ = createOperation("<", _Boolean, "org.eclipse.ocl.examples.library.numeric.NumericLessThanOperation", org.eclipse.ocl.examples.library.numeric.NumericLessThanOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__lt__eq_ = createOperation("<=", _Boolean, "org.eclipse.ocl.examples.library.numeric.NumericLessThanEqualOperation", org.eclipse.ocl.examples.library.numeric.NumericLessThanEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__gt_ = createOperation(">", _Boolean, "org.eclipse.ocl.examples.library.numeric.NumericGreaterThanOperation", org.eclipse.ocl.examples.library.numeric.NumericGreaterThanOperation.INSTANCE);
 		protected final @NonNull Operation op_Real__gt__eq_ = createOperation(">=", _Boolean, "org.eclipse.ocl.examples.library.numeric.NumericGreaterThanEqualOperation", org.eclipse.ocl.examples.library.numeric.NumericGreaterThanEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_abs = createOperation("abs", _Real, "org.eclipse.ocl.examples.library.numeric.NumericAbsOperation", org.eclipse.ocl.examples.library.numeric.NumericAbsOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_compareTo = createOperation("compareTo", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericCompareToOperation", org.eclipse.ocl.examples.library.numeric.NumericCompareToOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_floor = createOperation("floor", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericFloorOperation", org.eclipse.ocl.examples.library.numeric.NumericFloorOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_max = createOperation("max", _Real, "org.eclipse.ocl.examples.library.numeric.NumericMaxOperation", org.eclipse.ocl.examples.library.numeric.NumericMaxOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_min = createOperation("min", _Real, "org.eclipse.ocl.examples.library.numeric.NumericMinOperation", org.eclipse.ocl.examples.library.numeric.NumericMinOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_round = createOperation("round", _Integer, "org.eclipse.ocl.examples.library.numeric.NumericRoundOperation", org.eclipse.ocl.examples.library.numeric.NumericRoundOperation.INSTANCE);
 		protected final @NonNull Operation op_Real_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_String__add_ = createOperation("+", _String, "org.eclipse.ocl.examples.library.string.StringConcatOperation", org.eclipse.ocl.examples.library.string.StringConcatOperation.INSTANCE);
 		protected final @NonNull Operation op_String__lt_ = createOperation("<", _Boolean, "org.eclipse.ocl.examples.library.string.StringLessThanOperation", org.eclipse.ocl.examples.library.string.StringLessThanOperation.INSTANCE);
 		protected final @NonNull Operation op_String__lt__eq_ = createOperation("<=", _Boolean, "org.eclipse.ocl.examples.library.string.StringLessThanEqualOperation", org.eclipse.ocl.examples.library.string.StringLessThanEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_String__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_String__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_String__gt_ = createOperation(">", _Boolean, "org.eclipse.ocl.examples.library.string.StringGreaterThanOperation", org.eclipse.ocl.examples.library.string.StringGreaterThanOperation.INSTANCE);
 		protected final @NonNull Operation op_String__gt__eq_ = createOperation(">=", _Boolean, "org.eclipse.ocl.examples.library.string.StringGreaterThanEqualOperation", org.eclipse.ocl.examples.library.string.StringGreaterThanEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_String_at = createOperation("at", _String, "org.eclipse.ocl.examples.library.string.StringAtOperation", org.eclipse.ocl.examples.library.string.StringAtOperation.INSTANCE);
 		protected final @NonNull Operation op_String_characters = createOperation("characters", _Sequence_String, "org.eclipse.ocl.examples.library.string.StringCharactersOperation", org.eclipse.ocl.examples.library.string.StringCharactersOperation.INSTANCE);
 		protected final @NonNull Operation op_String_compareTo = createOperation("compareTo", _Integer, "org.eclipse.ocl.examples.library.string.StringCompareToOperation", org.eclipse.ocl.examples.library.string.StringCompareToOperation.INSTANCE);
 		protected final @NonNull Operation op_String_concat = createOperation("concat", _String, "org.eclipse.ocl.examples.library.string.StringConcatOperation", org.eclipse.ocl.examples.library.string.StringConcatOperation.INSTANCE);
 		protected final @NonNull Operation op_String_endsWith = createOperation("endsWith", _Boolean, "org.eclipse.ocl.examples.library.string.StringEndsWithOperation", org.eclipse.ocl.examples.library.string.StringEndsWithOperation.INSTANCE);
 		protected final @NonNull Operation op_String_equalsIgnoreCase = createOperation("equalsIgnoreCase", _Boolean, "org.eclipse.ocl.examples.library.string.StringEqualsIgnoreCaseOperation", org.eclipse.ocl.examples.library.string.StringEqualsIgnoreCaseOperation.INSTANCE);
 		protected final @NonNull Operation op_String_indexOf = createOperation("indexOf", _Integer, "org.eclipse.ocl.examples.library.string.StringIndexOfOperation", org.eclipse.ocl.examples.library.string.StringIndexOfOperation.INSTANCE);
 		protected final @NonNull Operation op_String_lastIndexOf = createOperation("lastIndexOf", _Integer, "org.eclipse.ocl.examples.library.string.StringLastIndexOfOperation", org.eclipse.ocl.examples.library.string.StringLastIndexOfOperation.INSTANCE);
 		protected final @NonNull Operation op_String_matches = createOperation("matches", _Boolean, "org.eclipse.ocl.examples.library.string.StringMatchesOperation", org.eclipse.ocl.examples.library.string.StringMatchesOperation.INSTANCE);
 		protected final @NonNull Operation op_String_replaceAll = createOperation("replaceAll", _String, "org.eclipse.ocl.examples.library.string.StringReplaceAllOperation", org.eclipse.ocl.examples.library.string.StringReplaceAllOperation.INSTANCE);
 		protected final @NonNull Operation op_String_replaceFirst = createOperation("replaceFirst", _String, "org.eclipse.ocl.examples.library.string.StringReplaceFirstOperation", org.eclipse.ocl.examples.library.string.StringReplaceFirstOperation.INSTANCE);
 		protected final @NonNull Operation op_String_size = createOperation("size", _Integer, "org.eclipse.ocl.examples.library.string.StringSizeOperation", org.eclipse.ocl.examples.library.string.StringSizeOperation.INSTANCE);
 		protected final @NonNull Operation op_String_startsWith = createOperation("startsWith", _Boolean, "org.eclipse.ocl.examples.library.string.StringStartsWithOperation", org.eclipse.ocl.examples.library.string.StringStartsWithOperation.INSTANCE);
 		protected final @NonNull Operation op_String_substituteAll = createOperation("substituteAll", _String, "org.eclipse.ocl.examples.library.string.StringSubstituteAllOperation", org.eclipse.ocl.examples.library.string.StringSubstituteAllOperation.INSTANCE);
 		protected final @NonNull Operation op_String_substituteFirst = createOperation("substituteFirst", _String, "org.eclipse.ocl.examples.library.string.StringSubstituteFirstOperation", org.eclipse.ocl.examples.library.string.StringSubstituteFirstOperation.INSTANCE);
 		protected final @NonNull Operation op_String_substring = createOperation("substring", _String, "org.eclipse.ocl.examples.library.string.StringSubstringOperation", org.eclipse.ocl.examples.library.string.StringSubstringOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toBoolean = createOperation("toBoolean", _Boolean, "org.eclipse.ocl.examples.library.string.StringToBooleanOperation", org.eclipse.ocl.examples.library.string.StringToBooleanOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toInteger = createOperation("toInteger", _Integer, "org.eclipse.ocl.examples.library.string.StringToIntegerOperation", org.eclipse.ocl.examples.library.string.StringToIntegerOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toLower = createOperation("toLower", _String, "org.eclipse.ocl.examples.library.string.StringToLowerCaseOperation", org.eclipse.ocl.examples.library.string.StringToLowerCaseOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toLowerCase = createOperation("toLowerCase", _String, "org.eclipse.ocl.examples.library.string.StringToLowerCaseOperation", org.eclipse.ocl.examples.library.string.StringToLowerCaseOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toReal = createOperation("toReal", _Real, "org.eclipse.ocl.examples.library.string.StringToRealOperation", org.eclipse.ocl.examples.library.string.StringToRealOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toUpper = createOperation("toUpper", _String, "org.eclipse.ocl.examples.library.string.StringToUpperCaseOperation", org.eclipse.ocl.examples.library.string.StringToUpperCaseOperation.INSTANCE);
 		protected final @NonNull Operation op_String_toUpperCase = createOperation("toUpperCase", _String, "org.eclipse.ocl.examples.library.string.StringToUpperCaseOperation", org.eclipse.ocl.examples.library.string.StringToUpperCaseOperation.INSTANCE);
 		protected final @NonNull Operation op_String_tokenize = createOperation("tokenize", _Sequence_String, "org.eclipse.ocl.examples.library.string.StringTokenizeOperation", org.eclipse.ocl.examples.library.string.StringTokenizeOperation.INSTANCE);
 		protected final @NonNull Operation op_String_tokenize_1 = createOperation("tokenize", _Sequence_String, "org.eclipse.ocl.examples.library.string.StringTokenizeOperation", org.eclipse.ocl.examples.library.string.StringTokenizeOperation.INSTANCE);
 		protected final @NonNull Operation op_String_tokenize_2 = createOperation("tokenize", _Sequence_String, "org.eclipse.ocl.examples.library.string.StringTokenizeOperation", org.eclipse.ocl.examples.library.string.StringTokenizeOperation.INSTANCE);
 		protected final @NonNull Operation op_String_trim = createOperation("trim", _String, "org.eclipse.ocl.examples.library.string.StringTrimOperation", org.eclipse.ocl.examples.library.string.StringTrimOperation.INSTANCE);
 		protected final @NonNull Operation op_UnlimitedNatural_oclAsType = createOperation("oclAsType", _UnlimitedNatural_oclAsType_TT, "org.eclipse.ocl.examples.library.numeric.UnlimitedNaturalOclAsTypeOperation", org.eclipse.ocl.examples.library.numeric.UnlimitedNaturalOclAsTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T_excluding = createOperation("excluding", _Bag_Bag_T, "org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T_flatten = createOperation("flatten", _Bag_Bag_flatten_T2, "org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation", org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T_including = createOperation("including", _Bag_Bag_T, "org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T_selectByKind = createOperation("selectByKind", _Bag_Bag_selectByKind_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation.INSTANCE);
 		protected final @NonNull Operation op_Bag_Bag_T_selectByType = createOperation("selectByType", _Bag_Bag_selectByType_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_asBag = createOperation("asBag", _Bag_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionAsBagOperation", org.eclipse.ocl.examples.library.collection.CollectionAsBagOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_asOrderedSet = createOperation("asOrderedSet", _OrderedSet_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionAsOrderedSetOperation", org.eclipse.ocl.examples.library.collection.CollectionAsOrderedSetOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_asSequence = createOperation("asSequence", _Sequence_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionAsSequenceOperation", org.eclipse.ocl.examples.library.collection.CollectionAsSequenceOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_asSet = createOperation("asSet", _Set_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionAsSetOperation", org.eclipse.ocl.examples.library.collection.CollectionAsSetOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_count = createOperation("count", _Integer, "org.eclipse.ocl.examples.library.collection.CollectionCountOperation", org.eclipse.ocl.examples.library.collection.CollectionCountOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_excludes = createOperation("excludes", _Boolean, "org.eclipse.ocl.examples.library.collection.CollectionExcludesOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludesOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_excludesAll = createOperation("excludesAll", _Boolean, "org.eclipse.ocl.examples.library.collection.CollectionExcludesAllOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludesAllOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_excluding = createOperation("excluding", _Collection_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_flatten = createOperation("flatten", _Collection_Collection_flatten_T2, "org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation", org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_includes = createOperation("includes", _Boolean, "org.eclipse.ocl.examples.library.collection.CollectionIncludesOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludesOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_includesAll = createOperation("includesAll", _Boolean, "org.eclipse.ocl.examples.library.collection.CollectionIncludesAllOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludesAllOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_including = createOperation("including", _Collection_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_intersection = createOperation("intersection", _Bag_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionIntersectionOperation", org.eclipse.ocl.examples.library.collection.CollectionIntersectionOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_intersection_1 = createOperation("intersection", _Set_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionIntersectionOperation", org.eclipse.ocl.examples.library.collection.CollectionIntersectionOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_isEmpty = createOperation("isEmpty", _Boolean, "org.eclipse.ocl.examples.library.collection.CollectionIsEmptyOperation", org.eclipse.ocl.examples.library.collection.CollectionIsEmptyOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_max = createOperation("max", _Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionMaxOperation", org.eclipse.ocl.examples.library.collection.CollectionMaxOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_min = createOperation("min", _Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionMinOperation", org.eclipse.ocl.examples.library.collection.CollectionMinOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_notEmpty = createOperation("notEmpty", _Boolean, "org.eclipse.ocl.examples.library.collection.CollectionNotEmptyOperation", org.eclipse.ocl.examples.library.collection.CollectionNotEmptyOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_product = createOperation("product", _Set_Tuple, "org.eclipse.ocl.examples.library.collection.CollectionProductOperation", org.eclipse.ocl.examples.library.collection.CollectionProductOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_selectByKind = createOperation("selectByKind", _Collection_Collection_selectByKind_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_selectByType = createOperation("selectByType", _Collection_Collection_selectByType_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_size = createOperation("size", _Integer, "org.eclipse.ocl.examples.library.collection.CollectionSizeOperation", org.eclipse.ocl.examples.library.collection.CollectionSizeOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_sum = createOperation("sum", _Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionSumOperation", org.eclipse.ocl.examples.library.collection.CollectionSumOperation.INSTANCE);
 		protected final @NonNull Operation op_Collection_Collection_T_union = createOperation("union", _Bag_Collection_T, "org.eclipse.ocl.examples.library.collection.CollectionUnionOperation", org.eclipse.ocl.examples.library.collection.CollectionUnionOperation.INSTANCE);
 		protected final @NonNull Operation op_Enumeration_allInstances = createOperation("allInstances", _Set_OclSelf, "org.eclipse.ocl.examples.library.enumeration.EnumerationAllInstancesOperation", org.eclipse.ocl.examples.library.enumeration.EnumerationAllInstancesOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclAsSet = createOperation("oclAsSet", _Set_OclSelf, "org.eclipse.ocl.examples.library.oclany.OclAnyOclAsSetOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclAsSetOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclAsType = createOperation("oclAsType", _OclAny_oclAsType_TT, "org.eclipse.ocl.examples.library.oclany.OclAnyOclAsTypeOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclAsTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclIsInState = createOperation("oclIsInState", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInStateOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInStateOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclIsInvalid = createOperation("oclIsInvalid", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInvalidOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInvalidOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclIsKindOf = createOperation("oclIsKindOf", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsKindOfOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsKindOfOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclIsNew = createOperation("oclIsNew", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclIsTypeOf = createOperation("oclIsTypeOf", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsTypeOfOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsTypeOfOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclIsUndefined = createOperation("oclIsUndefined", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsUndefinedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsUndefinedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclLog = createOperation("oclLog", _OclSelf, "org.eclipse.ocl.examples.library.oclany.OclAnyOclLogOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclLogOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclLog_1 = createOperation("oclLog", _OclSelf, "org.eclipse.ocl.examples.library.oclany.OclAnyOclLogOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclLogOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_oclType = createOperation("oclType", _Metaclass_OclSelf, "org.eclipse.ocl.examples.library.oclany.OclAnyOclTypeOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_OclAny_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_OclComparable__lt_ = createOperation("<", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclComparableLessThanOperation", org.eclipse.ocl.examples.library.oclany.OclComparableLessThanOperation.INSTANCE);
 		protected final @NonNull Operation op_OclComparable__lt__eq_ = createOperation("<=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclComparableLessThanEqualOperation", org.eclipse.ocl.examples.library.oclany.OclComparableLessThanEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclComparable__gt_ = createOperation(">", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclComparableGreaterThanOperation", org.eclipse.ocl.examples.library.oclany.OclComparableGreaterThanOperation.INSTANCE);
 		protected final @NonNull Operation op_OclComparable__gt__eq_ = createOperation(">=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclComparableGreaterThanEqualOperation", org.eclipse.ocl.examples.library.oclany.OclComparableGreaterThanEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclComparable_compareTo = createOperation("compareTo", _Integer, "org.eclipse.ocl.examples.library.oclany.OclComparableCompareToOperation", org.eclipse.ocl.examples.library.oclany.OclComparableCompareToOperation.INSTANCE);
 		protected final @NonNull Operation op_OclElement_allInstances = createOperation("allInstances", _Set_OclSelf, "org.eclipse.ocl.examples.library.classifier.ClassifierAllInstancesOperation", org.eclipse.ocl.examples.library.classifier.ClassifierAllInstancesOperation.INSTANCE);
 		protected final @NonNull Operation op_OclElement_oclContainer = createOperation("oclContainer", _OclElement, "org.eclipse.ocl.examples.library.classifier.ClassifierOclContainerOperation", org.eclipse.ocl.examples.library.classifier.ClassifierOclContainerOperation.INSTANCE);
 		protected final @NonNull Operation op_OclElement_oclContents = createOperation("oclContents", _Set_OclElement, "org.eclipse.ocl.examples.library.classifier.ClassifierOclContentsOperation", org.eclipse.ocl.examples.library.classifier.ClassifierOclContentsOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_allInstances = createOperation("allInstances", _Set_OclSelf, "org.eclipse.ocl.examples.library.oclinvalid.OclInvalidAllInstancesOperation", org.eclipse.ocl.examples.library.oclinvalid.OclInvalidAllInstancesOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_and = createOperation("and", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanAndOperation", org.eclipse.ocl.examples.library.logical.BooleanAndOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_implies = createOperation("implies", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanImpliesOperation", org.eclipse.ocl.examples.library.logical.BooleanImpliesOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclAsSet = createOperation("oclAsSet", _Set_OclSelf, "org.eclipse.ocl.examples.library.oclany.OclAnyOclAsSetOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclAsSetOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclAsType = createOperation("oclAsType", _OclInvalid_oclAsType_TT, "org.eclipse.ocl.examples.library.oclany.OclAnyOclAsTypeOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclAsTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclIsInvalid = createOperation("oclIsInvalid", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInvalidOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInvalidOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclIsKindOf = createOperation("oclIsKindOf", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsKindOfOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsKindOfOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclIsTypeOf = createOperation("oclIsTypeOf", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsTypeOfOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsTypeOfOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclIsUndefined = createOperation("oclIsUndefined", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyOclIsUndefinedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclIsUndefinedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_oclType = createOperation("oclType", _Metaclass_OclSelf, "org.eclipse.ocl.examples.library.oclany.OclAnyOclTypeOperation", org.eclipse.ocl.examples.library.oclany.OclAnyOclTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_or = createOperation("or", _Boolean, "org.eclipse.ocl.examples.library.logical.BooleanAndOperation", org.eclipse.ocl.examples.library.logical.BooleanAndOperation.INSTANCE);
 		protected final @NonNull Operation op_OclInvalid_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_OclMessage_hasReturned = createOperation("hasReturned", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclMessage_isOperationCall = createOperation("isOperationCall", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclMessage_isSignalSent = createOperation("isSignalSent", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclMessage_result = createOperation("result", _OclAny, "org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation", org.eclipse.ocl.examples.library.oclany.OclAnyUnsupportedOperation.INSTANCE);
 		protected final @NonNull Operation op_OclSummable_sum = createOperation("sum", _OclSelf, null, null);
 		protected final @NonNull Operation op_OclSummable_zero = createOperation("zero", _OclSelf, null, null);
 		protected final @NonNull Operation op_OclTuple__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclTuple__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclType_conformsTo = createOperation("conformsTo", _Boolean, "org.eclipse.ocl.examples.library.classifier.OclTypeConformsToOperation", org.eclipse.ocl.examples.library.classifier.OclTypeConformsToOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid_allInstances = createOperation("allInstances", _Set_OclSelf, "org.eclipse.ocl.examples.library.oclvoid.OclVoidAllInstancesOperation", org.eclipse.ocl.examples.library.oclvoid.OclVoidAllInstancesOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid_and = createOperation("and", _Boolean, "org.eclipse.ocl.examples.library.oclvoid.OclVoidAndOperation", org.eclipse.ocl.examples.library.oclvoid.OclVoidAndOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid_implies = createOperation("implies", _Boolean, "org.eclipse.ocl.examples.library.oclvoid.OclVoidImpliesOperation", org.eclipse.ocl.examples.library.oclvoid.OclVoidImpliesOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid_or = createOperation("or", _Boolean, "org.eclipse.ocl.examples.library.oclvoid.OclVoidOrOperation", org.eclipse.ocl.examples.library.oclvoid.OclVoidOrOperation.INSTANCE);
 		protected final @NonNull Operation op_OclVoid_toString = createOperation("toString", _String, "org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation", org.eclipse.ocl.examples.library.oclany.OclAnyToStringOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T__neg_ = createOperation("-", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.SetMinusOperation", org.eclipse.ocl.examples.library.collection.SetMinusOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_append = createOperation("append", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionAppendOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionAppendOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_excluding = createOperation("excluding", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_flatten = createOperation("flatten", _OrderedSet_OrderedSet_flatten_T2, "org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation", org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_including = createOperation("including", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_insertAt = createOperation("insertAt", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionInsertAtOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionInsertAtOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_prepend = createOperation("prepend", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionPrependOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionPrependOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_reverse = createOperation("reverse", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionReverseOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionReverseOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_selectByKind = createOperation("selectByKind", _OrderedSet_OrderedSet_selectByKind_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_selectByType = createOperation("selectByType", _OrderedSet_OrderedSet_selectByType_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_OrderedSet_OrderedSet_T_subOrderedSet = createOperation("subOrderedSet", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.collection.OrderedSetSubOrderedSetOperation", org.eclipse.ocl.examples.library.collection.OrderedSetSubOrderedSetOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_append = createOperation("append", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionAppendOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionAppendOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_at = createOperation("at", _Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionAtOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionAtOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_excluding = createOperation("excluding", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_first = createOperation("first", _Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionFirstOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionFirstOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_flatten = createOperation("flatten", _Sequence_Sequence_flatten_T2, "org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation", org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_including = createOperation("including", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_indexOf = createOperation("indexOf", _Integer, "org.eclipse.ocl.examples.library.collection.OrderedCollectionIndexOfOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionIndexOfOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_insertAt = createOperation("insertAt", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionInsertAtOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionInsertAtOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_last = createOperation("last", _Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionLastOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionLastOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_prepend = createOperation("prepend", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionPrependOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionPrependOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_reverse = createOperation("reverse", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.OrderedCollectionReverseOperation", org.eclipse.ocl.examples.library.collection.OrderedCollectionReverseOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_selectByKind = createOperation("selectByKind", _Sequence_Sequence_selectByKind_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_selectByType = createOperation("selectByType", _Sequence_Sequence_selectByType_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_Sequence_Sequence_T_subSequence = createOperation("subSequence", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.collection.SequenceSubSequenceOperation", org.eclipse.ocl.examples.library.collection.SequenceSubSequenceOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T__neg_ = createOperation("-", _Set_Set_T, "org.eclipse.ocl.examples.library.collection.SetMinusOperation", org.eclipse.ocl.examples.library.collection.SetMinusOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T__lt__gt_ = createOperation("<>", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T__eq_ = createOperation("=", _Boolean, "org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation", org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T_excluding = createOperation("excluding", _Set_Set_T, "org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation", org.eclipse.ocl.examples.library.collection.CollectionExcludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T_flatten = createOperation("flatten", _Set_Set_flatten_T2, "org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation", org.eclipse.ocl.examples.library.collection.CollectionFlattenOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T_including = createOperation("including", _Set_Set_T, "org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation", org.eclipse.ocl.examples.library.collection.CollectionIncludingOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T_selectByKind = createOperation("selectByKind", _Set_Set_selectByKind_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByKindOperation.INSTANCE);
 		protected final @NonNull Operation op_Set_Set_T_selectByType = createOperation("selectByType", _Set_Set_selectByType_TT, "org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation", org.eclipse.ocl.examples.library.collection.CollectionSelectByTypeOperation.INSTANCE);
 		protected final @NonNull Operation op_UniqueCollection_UniqueCollection_T__neg_ = createOperation("-", _UniqueCollection_UniqueCollection_T_1, "org.eclipse.ocl.examples.library.collection.SetMinusOperation", org.eclipse.ocl.examples.library.collection.SetMinusOperation.INSTANCE);
 		protected final @NonNull Operation op_UniqueCollection_UniqueCollection_T_intersection = createOperation("intersection", _Set_UniqueCollection_T, "org.eclipse.ocl.examples.library.collection.CollectionIntersectionOperation", org.eclipse.ocl.examples.library.collection.CollectionIntersectionOperation.INSTANCE);
 		protected final @NonNull Operation op_UniqueCollection_UniqueCollection_T_symmetricDifference = createOperation("symmetricDifference", _Set_UniqueCollection_T, "org.eclipse.ocl.examples.library.collection.SetSymmetricDifferenceOperation", org.eclipse.ocl.examples.library.collection.SetSymmetricDifferenceOperation.INSTANCE);
 		protected final @NonNull Operation op_UniqueCollection_UniqueCollection_T_union = createOperation("union", _Set_UniqueCollection_T, "org.eclipse.ocl.examples.library.collection.CollectionUnionOperation", org.eclipse.ocl.examples.library.collection.CollectionUnionOperation.INSTANCE);
 		
 		protected void installOperations() {
 			List<Operation> ownedOperations;
 			List<Parameter> ownedParameters;
 			Operation operation;
 			Parameter parameter;
 			ownedOperations = _Boolean.getOwnedOperation();
 			ownedOperations.add(operation = op_Boolean__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Boolean__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Boolean_allInstances);
 			operation.setIsStatic(true);
 			ownedOperations.add(operation = op_Boolean_and);
 			operation.setIsInvalidating(true);
 			operation.setIsRequired(false);
 			operation.setIsValidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_Boolean_implies);
 			operation.setIsInvalidating(true);
 			operation.setIsRequired(false);
 			operation.setIsValidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_Boolean_not);
 			operation.setIsRequired(false);
 			ownedOperations.add(operation = op_Boolean_or);
 			operation.setIsInvalidating(true);
 			operation.setIsRequired(false);
 			operation.setIsValidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_Boolean_toString);
 			ownedOperations.add(operation = op_Boolean_xor);
 			operation.setIsRequired(false);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations = _Integer.getOwnedOperation();
 			ownedOperations.add(operation = op_Integer__mul_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer__add_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer__neg_);
 			ownedOperations.add(operation = op_Integer__neg__1);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer__div_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer_abs);
 			ownedOperations.add(operation = op_Integer_compareTo);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer_div);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _Integer, true));
 			ownedOperations.add(operation = op_Integer_max);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer_min);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _OclSelf, true));
 			ownedOperations.add(operation = op_Integer_mod);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _Integer, true));
 			ownedOperations.add(operation = op_Integer_toString);
 			ownedOperations = _Real.getOwnedOperation();
 			ownedOperations.add(operation = op_Real__mul_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__add_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__neg_);
 			ownedOperations.add(operation = op_Real__neg__1);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__div_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__lt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__lt__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real__gt__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real_abs);
 			ownedOperations.add(operation = op_Real_compareTo);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_Real_floor);
 			ownedOperations.add(operation = op_Real_max);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real_min);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("r", _OclSelf, true));
 			ownedOperations.add(operation = op_Real_round);
 			ownedOperations.add(operation = op_Real_toString);
 			ownedOperations = _String.getOwnedOperation();
 			ownedOperations.add(operation = op_String__add_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String__lt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _OclSelf, true));
 			ownedOperations.add(operation = op_String__lt__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _OclSelf, true));
 			ownedOperations.add(operation = op_String__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_String__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_String__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _OclSelf, true));
 			ownedOperations.add(operation = op_String__gt__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _OclSelf, true));
 			ownedOperations.add(operation = op_String_at);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("i", _Integer, true));
 			ownedOperations.add(operation = op_String_characters);
 			ownedOperations.add(operation = op_String_compareTo);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_String_concat);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String_endsWith);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String_equalsIgnoreCase);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String_indexOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String_lastIndexOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String_matches);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("regex", _String, true));
 			ownedOperations.add(operation = op_String_replaceAll);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("regex", _String, true));
 			ownedParameters.add(parameter = createParameter("replacement", _String, true));
 			ownedOperations.add(operation = op_String_replaceFirst);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("regex", _String, true));
 			ownedParameters.add(parameter = createParameter("replacement", _String, true));
 			ownedOperations.add(operation = op_String_size);
 			ownedOperations.add(operation = op_String_startsWith);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _String, true));
 			ownedOperations.add(operation = op_String_substituteAll);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("oldSubstring", _String, true));
 			ownedParameters.add(parameter = createParameter("newSubstring", _String, true));
 			ownedOperations.add(operation = op_String_substituteFirst);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("oldSubstring", _String, true));
 			ownedParameters.add(parameter = createParameter("newSubstring", _String, true));
 			ownedOperations.add(operation = op_String_substring);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("lower", _Integer, true));
 			ownedParameters.add(parameter = createParameter("upper", _Integer, true));
 			ownedOperations.add(operation = op_String_toBoolean);
 			operation.setIsInvalidating(true);
 			ownedOperations.add(operation = op_String_toInteger);
 			operation.setIsInvalidating(true);
 			ownedOperations.add(operation = op_String_toLower);
 			ownedOperations.add(operation = op_String_toLowerCase);
 			ownedOperations.add(operation = op_String_toReal);
 			operation.setIsInvalidating(true);
 			ownedOperations.add(operation = op_String_toString);
 			ownedOperations.add(operation = op_String_toUpper);
 			ownedOperations.add(operation = op_String_toUpperCase);
 			ownedOperations.add(operation = op_String_tokenize);
 			ownedOperations.add(operation = op_String_tokenize_1);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("delimiters", _String, true));
 			ownedOperations.add(operation = op_String_tokenize_2);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("delimiters", _String, true));
 			ownedParameters.add(parameter = createParameter("returnDelimiters", _Boolean, true));
 			ownedOperations.add(operation = op_String_trim);
 			ownedOperations = _UnlimitedNatural.getOwnedOperation();
 			ownedOperations.add(operation = op_UnlimitedNatural_oclAsType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_UnlimitedNatural_oclAsType_TT, true));
 			ownedOperations = _Bag_Bag_T.getOwnedOperation();
 			ownedOperations.add(operation = op_Bag_Bag_T__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Bag_Bag_T__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Bag_Bag_T_excluding);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Bag_Bag_T_flatten);
 			ownedOperations.add(operation = op_Bag_Bag_T_including);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _Bag_T, false));
 			ownedOperations.add(operation = op_Bag_Bag_T_selectByKind);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Bag_selectByKind_TT, true));
 			ownedOperations.add(operation = op_Bag_Bag_T_selectByType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Bag_selectByType_TT, true));
 			ownedOperations = _Collection_Collection_T.getOwnedOperation();
 			ownedOperations.add(operation = op_Collection_Collection_T__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Collection_Collection_T__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_asBag);
 			ownedOperations.add(operation = op_Collection_Collection_T_asOrderedSet);
 			ownedOperations.add(operation = op_Collection_Collection_T_asSequence);
 			ownedOperations.add(operation = op_Collection_Collection_T_asSet);
 			ownedOperations.add(operation = op_Collection_Collection_T_count);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Collection_Collection_T_excludes);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Collection_Collection_T_excludesAll);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("c2", _Collection_Collection_excludesAll_T2, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_excluding);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Collection_Collection_T_flatten);
 			ownedOperations.add(operation = op_Collection_Collection_T_includes);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Collection_Collection_T_includesAll);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("c2", _Collection_Collection_includesAll_T2, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_including);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _Collection_T, false));
 			ownedOperations.add(operation = op_Collection_Collection_T_intersection);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("c", _Collection_Collection_T, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_intersection_1);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("u", _UniqueCollection_Collection_T, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_isEmpty);
 			ownedOperations.add(operation = op_Collection_Collection_T_max);
 			ownedOperations.add(operation = op_Collection_Collection_T_min);
 			ownedOperations.add(operation = op_Collection_Collection_T_notEmpty);
 			ownedOperations.add(operation = op_Collection_Collection_T_product);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("c2", _Collection_Collection_product_T2, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_selectByKind);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Collection_selectByKind_TT, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_selectByType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Collection_selectByType_TT, true));
 			ownedOperations.add(operation = op_Collection_Collection_T_size);
 			ownedOperations.add(operation = op_Collection_Collection_T_sum);
 			ownedOperations.add(operation = op_Collection_Collection_T_union);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("c", _Collection_Collection_T, true));
 			ownedOperations = _Enumeration.getOwnedOperation();
 			ownedOperations.add(operation = op_Enumeration_allInstances);
 			operation.setIsStatic(true);
 			ownedOperations = _OclAny.getOwnedOperation();
 			ownedOperations.add(operation = op_OclAny__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclAny__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclAny_oclAsSet);
 			ownedOperations.add(operation = op_OclAny_oclAsType);
 			operation.setIsRequired(false);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OclAny_oclAsType_TT, true));
 			ownedOperations.add(operation = op_OclAny_oclIsInState);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("statespec", _OclState, true));
 			ownedOperations.add(operation = op_OclAny_oclIsInvalid);
 			operation.setIsValidating(true);
 			ownedOperations.add(operation = op_OclAny_oclIsKindOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OclAny_oclIsKindOf_T, true));
 			ownedOperations.add(operation = op_OclAny_oclIsNew);
 			ownedOperations.add(operation = op_OclAny_oclIsTypeOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OclAny_oclIsTypeOf_T, true));
 			ownedOperations.add(operation = op_OclAny_oclIsUndefined);
 			operation.setIsValidating(true);
 			ownedOperations.add(operation = op_OclAny_oclLog);
 			ownedOperations.add(operation = op_OclAny_oclLog_1);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("message", _String, true));
 			ownedOperations.add(operation = op_OclAny_oclType);
 			ownedOperations.add(operation = op_OclAny_toString);
 			ownedOperations = _OclComparable.getOwnedOperation();
 			ownedOperations.add(operation = op_OclComparable__lt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_OclComparable__lt__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_OclComparable__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_OclComparable__gt__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_OclComparable_compareTo);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations = _OclElement.getOwnedOperation();
 			ownedOperations.add(operation = op_OclElement_allInstances);
 			operation.setIsStatic(true);
 			ownedOperations.add(operation = op_OclElement_oclContainer);
 			operation.setIsRequired(false);
 			ownedOperations.add(operation = op_OclElement_oclContents);
 			ownedOperations = _OclInvalid.getOwnedOperation();
 			ownedOperations.add(operation = op_OclInvalid__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclInvalid__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclInvalid_allInstances);
 			operation.setIsStatic(true);
 			ownedOperations.add(operation = op_OclInvalid_and);
 			operation.setIsRequired(false);
 			operation.setIsValidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_OclInvalid_implies);
 			operation.setIsRequired(false);
 			operation.setIsValidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_OclInvalid_oclAsSet);
 			ownedOperations.add(operation = op_OclInvalid_oclAsType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OclInvalid_oclAsType_TT, true));
 			ownedOperations.add(operation = op_OclInvalid_oclIsInvalid);
 			operation.setIsValidating(true);
 			ownedOperations.add(operation = op_OclInvalid_oclIsKindOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OclInvalid_oclIsKindOf_T, true));
 			ownedOperations.add(operation = op_OclInvalid_oclIsTypeOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OclInvalid_oclIsTypeOf_T, true));
 			ownedOperations.add(operation = op_OclInvalid_oclIsUndefined);
 			operation.setIsValidating(true);
 			ownedOperations.add(operation = op_OclInvalid_oclType);
 			ownedOperations.add(operation = op_OclInvalid_or);
 			operation.setIsRequired(false);
 			operation.setIsValidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_OclInvalid_toString);
 			ownedOperations = _OclMessage.getOwnedOperation();
 			ownedOperations.add(operation = op_OclMessage_hasReturned);
 			ownedOperations.add(operation = op_OclMessage_isOperationCall);
 			ownedOperations.add(operation = op_OclMessage_isSignalSent);
 			ownedOperations.add(operation = op_OclMessage_result);
 			ownedOperations = _OclSummable.getOwnedOperation();
 			ownedOperations.add(operation = op_OclSummable_sum);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("that", _OclSelf, true));
 			ownedOperations.add(operation = op_OclSummable_zero);
 			ownedOperations = _OclTuple.getOwnedOperation();
 			ownedOperations.add(operation = op_OclTuple__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclTuple__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations = _OclType.getOwnedOperation();
 			ownedOperations.add(operation = op_OclType_conformsTo);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type2", _OclType, true));
 			ownedOperations = _OclVoid.getOwnedOperation();
 			ownedOperations.add(operation = op_OclVoid__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclVoid__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OclVoid_allInstances);
 			operation.setIsStatic(true);
 			ownedOperations.add(operation = op_OclVoid_and);
 			operation.setIsRequired(false);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_OclVoid_implies);
 			operation.setIsRequired(false);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_OclVoid_or);
 			operation.setIsRequired(false);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("b", _Boolean, false));
 			ownedOperations.add(operation = op_OclVoid_toString);
 			ownedOperations = _OrderedSet_OrderedSet_T.getOwnedOperation();
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T__neg_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _UniqueCollection_OclAny, true));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_append);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OrderedSet_T, false));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_excluding);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_flatten);
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_including);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OrderedSet_T, false));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_insertAt);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("index", _Integer, true));
 			ownedParameters.add(parameter = createParameter("object", _OrderedSet_T, false));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_prepend);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OrderedSet_T, false));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_reverse);
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_selectByKind);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OrderedSet_selectByKind_TT, true));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_selectByType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_OrderedSet_selectByType_TT, true));
 			ownedOperations.add(operation = op_OrderedSet_OrderedSet_T_subOrderedSet);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("lower", _Integer, true));
 			ownedParameters.add(parameter = createParameter("upper", _Integer, true));
 			ownedOperations = _Sequence_Sequence_T.getOwnedOperation();
 			ownedOperations.add(operation = op_Sequence_Sequence_T__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Sequence_Sequence_T__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_append);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _Sequence_T, false));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_at);
 			operation.setIsInvalidating(true);
 			operation.setIsRequired(false);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("index", _Integer, true));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_excluding);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_first);
 			operation.setIsInvalidating(true);
 			operation.setIsRequired(false);
 			ownedOperations.add(operation = op_Sequence_Sequence_T_flatten);
 			ownedOperations.add(operation = op_Sequence_Sequence_T_including);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _Sequence_T, false));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_indexOf);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("obj", _OclAny, false));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_insertAt);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("index", _Integer, true));
 			ownedParameters.add(parameter = createParameter("object", _Sequence_T, false));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_last);
 			operation.setIsInvalidating(true);
 			operation.setIsRequired(false);
 			ownedOperations.add(operation = op_Sequence_Sequence_T_prepend);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _Sequence_T, false));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_reverse);
 			ownedOperations.add(operation = op_Sequence_Sequence_T_selectByKind);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Sequence_selectByKind_TT, true));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_selectByType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Sequence_selectByType_TT, true));
 			ownedOperations.add(operation = op_Sequence_Sequence_T_subSequence);
 			operation.setIsInvalidating(true);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("lower", _Integer, true));
 			ownedParameters.add(parameter = createParameter("upper", _Integer, true));
 			ownedOperations = _Set_Set_T.getOwnedOperation();
 			ownedOperations.add(operation = op_Set_Set_T__neg_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _UniqueCollection_OclAny, true));
 			ownedOperations.add(operation = op_Set_Set_T__lt__gt_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Set_Set_T__eq_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object2", _OclSelf, true));
 			ownedOperations.add(operation = op_Set_Set_T_excluding);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _OclAny, false));
 			ownedOperations.add(operation = op_Set_Set_T_flatten);
 			ownedOperations.add(operation = op_Set_Set_T_including);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("object", _Set_T, false));
 			ownedOperations.add(operation = op_Set_Set_T_selectByKind);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Set_selectByKind_TT, true));
 			ownedOperations.add(operation = op_Set_Set_T_selectByType);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("type", _Metaclass_Set_selectByType_TT, true));
 			ownedOperations = _UniqueCollection_UniqueCollection_T_1.getOwnedOperation();
 			ownedOperations.add(operation = op_UniqueCollection_UniqueCollection_T__neg_);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _UniqueCollection_OclAny, true));
 			ownedOperations.add(operation = op_UniqueCollection_UniqueCollection_T_intersection);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("c", _Collection_UniqueCollection_T, true));
 			ownedOperations.add(operation = op_UniqueCollection_UniqueCollection_T_symmetricDifference);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _UniqueCollection_OclAny, true));
 			ownedOperations.add(operation = op_UniqueCollection_UniqueCollection_T_union);
 			ownedParameters = operation.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("s", _UniqueCollection_UniqueCollection_T_1, true));
 		}
 			
 		protected final @NonNull Iteration it_Bag_Bag_T_closure = createIteration("closure", _Set_Bag_T, "org.eclipse.ocl.examples.library.iterator.ClosureIteration", org.eclipse.ocl.examples.library.iterator.ClosureIteration.INSTANCE);
 		protected final @NonNull Iteration it_Bag_Bag_T_collectNested = createIteration("collectNested", _Bag_Bag_T, "org.eclipse.ocl.examples.library.iterator.CollectNestedIteration", org.eclipse.ocl.examples.library.iterator.CollectNestedIteration.INSTANCE);
 		protected final @NonNull Iteration it_Bag_Bag_T_collect = createIteration("collect", _Bag_Bag_collect_V, "org.eclipse.ocl.examples.library.iterator.CollectIteration", org.eclipse.ocl.examples.library.iterator.CollectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Bag_Bag_T_reject = createIteration("reject", _Bag_Bag_T, "org.eclipse.ocl.examples.library.iterator.RejectIteration", org.eclipse.ocl.examples.library.iterator.RejectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Bag_Bag_T_select = createIteration("select", _Bag_Bag_T, "org.eclipse.ocl.examples.library.iterator.SelectIteration", org.eclipse.ocl.examples.library.iterator.SelectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Bag_Bag_T_sortedBy = createIteration("sortedBy", _Sequence_Bag_T, "org.eclipse.ocl.examples.library.iterator.SortedByIteration", org.eclipse.ocl.examples.library.iterator.SortedByIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_any = createIteration("any", _Collection_T, "org.eclipse.ocl.examples.library.iterator.AnyIteration", org.eclipse.ocl.examples.library.iterator.AnyIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_collectNested = createIteration("collectNested", _Collection_Collection_T, "org.eclipse.ocl.examples.library.iterator.CollectNestedIteration", org.eclipse.ocl.examples.library.iterator.CollectNestedIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_collect = createIteration("collect", _Collection_Collection_collect_V, "org.eclipse.ocl.examples.library.iterator.CollectIteration", org.eclipse.ocl.examples.library.iterator.CollectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_exists = createIteration("exists", _Boolean, "org.eclipse.ocl.examples.library.iterator.ExistsIteration", org.eclipse.ocl.examples.library.iterator.ExistsIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_exists_1 = createIteration("exists", _Boolean, "org.eclipse.ocl.examples.library.iterator.ExistsIteration", org.eclipse.ocl.examples.library.iterator.ExistsIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_forAll = createIteration("forAll", _Boolean, "org.eclipse.ocl.examples.library.iterator.ForAllIteration", org.eclipse.ocl.examples.library.iterator.ForAllIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_forAll_1 = createIteration("forAll", _Boolean, "org.eclipse.ocl.examples.library.iterator.ForAllIteration", org.eclipse.ocl.examples.library.iterator.ForAllIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_isUnique = createIteration("isUnique", _Boolean, "org.eclipse.ocl.examples.library.iterator.IsUniqueIteration", org.eclipse.ocl.examples.library.iterator.IsUniqueIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_iterate = createIteration("iterate", _Collection_iterate_Tacc, "org.eclipse.ocl.examples.library.iterator.IterateIteration", org.eclipse.ocl.examples.library.iterator.IterateIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_one = createIteration("one", _Boolean, "org.eclipse.ocl.examples.library.iterator.OneIteration", org.eclipse.ocl.examples.library.iterator.OneIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_reject = createIteration("reject", _Collection_Collection_T, "org.eclipse.ocl.examples.library.iterator.RejectIteration", org.eclipse.ocl.examples.library.iterator.RejectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_select = createIteration("select", _Collection_Collection_T, "org.eclipse.ocl.examples.library.iterator.SelectIteration", org.eclipse.ocl.examples.library.iterator.SelectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Collection_Collection_T_sortedBy = createIteration("sortedBy", _Sequence_Collection_T, "org.eclipse.ocl.examples.library.iterator.SortedByIteration", org.eclipse.ocl.examples.library.iterator.SortedByIteration.INSTANCE);
 		protected final @NonNull Iteration it_OrderedSet_OrderedSet_T_reject = createIteration("reject", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.iterator.RejectIteration", org.eclipse.ocl.examples.library.iterator.RejectIteration.INSTANCE);
 		protected final @NonNull Iteration it_OrderedSet_OrderedSet_T_select = createIteration("select", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.iterator.SelectIteration", org.eclipse.ocl.examples.library.iterator.SelectIteration.INSTANCE);
 		protected final @NonNull Iteration it_OrderedSet_OrderedSet_T_sortedBy = createIteration("sortedBy", _OrderedSet_OrderedSet_T, "org.eclipse.ocl.examples.library.iterator.SortedByIteration", org.eclipse.ocl.examples.library.iterator.SortedByIteration.INSTANCE);
 		protected final @NonNull Iteration it_Sequence_Sequence_T_closure = createIteration("closure", _OrderedSet_Sequence_T, "org.eclipse.ocl.examples.library.iterator.ClosureIteration", org.eclipse.ocl.examples.library.iterator.ClosureIteration.INSTANCE);
 		protected final @NonNull Iteration it_Sequence_Sequence_T_collectNested = createIteration("collectNested", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.iterator.CollectNestedIteration", org.eclipse.ocl.examples.library.iterator.CollectNestedIteration.INSTANCE);
 		protected final @NonNull Iteration it_Sequence_Sequence_T_collect = createIteration("collect", _Sequence_Sequence_collect_V, "org.eclipse.ocl.examples.library.iterator.CollectIteration", org.eclipse.ocl.examples.library.iterator.CollectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Sequence_Sequence_T_reject = createIteration("reject", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.iterator.RejectIteration", org.eclipse.ocl.examples.library.iterator.RejectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Sequence_Sequence_T_select = createIteration("select", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.iterator.SelectIteration", org.eclipse.ocl.examples.library.iterator.SelectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Sequence_Sequence_T_sortedBy = createIteration("sortedBy", _Sequence_Sequence_T, "org.eclipse.ocl.examples.library.iterator.SortedByIteration", org.eclipse.ocl.examples.library.iterator.SortedByIteration.INSTANCE);
 		protected final @NonNull Iteration it_Set_Set_T_reject = createIteration("reject", _Set_Set_T, "org.eclipse.ocl.examples.library.iterator.RejectIteration", org.eclipse.ocl.examples.library.iterator.RejectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Set_Set_T_select = createIteration("select", _Set_Set_T, "org.eclipse.ocl.examples.library.iterator.SelectIteration", org.eclipse.ocl.examples.library.iterator.SelectIteration.INSTANCE);
 		protected final @NonNull Iteration it_Set_Set_T_sortedBy = createIteration("sortedBy", _OrderedSet_Set_T, "org.eclipse.ocl.examples.library.iterator.SortedByIteration", org.eclipse.ocl.examples.library.iterator.SortedByIteration.INSTANCE);
 		protected final @NonNull Iteration it_UniqueCollection_UniqueCollection_T_sortedBy = createIteration("sortedBy", _OrderedSet_UniqueCollection_T, "org.eclipse.ocl.examples.library.iterator.SortedByIteration", org.eclipse.ocl.examples.library.iterator.SortedByIteration.INSTANCE);
 		
 		protected void installIterations() {
 			List<Operation> ownedIterations;
 			List<Parameter> ownedParameters;
 			Iteration iteration;
 			Parameter parameter;
 			ownedIterations = _Bag_Bag_T.getOwnedOperation();
 			ownedIterations.add(iteration = it_Bag_Bag_T_closure);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Bag_T, true));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Bag_T_4, true));
 			ownedIterations.add(iteration = it_Bag_Bag_T_collectNested);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Bag_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Bag_T_1, true));
 			ownedIterations.add(iteration = it_Bag_Bag_T_collect);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Bag_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Bag_T_2, true));
 			ownedIterations.add(iteration = it_Bag_Bag_T_reject);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Bag_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Bag_T, true));
 			ownedIterations.add(iteration = it_Bag_Bag_T_select);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Bag_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Bag_T, true));
 			ownedIterations.add(iteration = it_Bag_Bag_T_sortedBy);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Bag_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Bag_T_3, true));
 			ownedIterations = _Collection_Collection_T.getOwnedOperation();
 			ownedIterations.add(iteration = it_Collection_Collection_T_any);
 			iteration.setIsInvalidating(true);
 			iteration.setIsRequired(false);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_collectNested);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T_1, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_collect);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T_2, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_exists);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters.add(parameter = createParameter("j", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_exists_1);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_forAll);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters.add(parameter = createParameter("j", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_forAll_1);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_isUnique);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T_4, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_iterate);
 			iteration.setIsRequired(false);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedAccumulator();
 			ownedParameters.add(parameter = createParameter("acc", _Collection_iterate_Tacc, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T_3, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_one);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_reject);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_select);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T, true));
 			ownedIterations.add(iteration = it_Collection_Collection_T_sortedBy);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Collection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Collection_T_4, true));
 			ownedIterations = _OrderedSet_OrderedSet_T.getOwnedOperation();
 			ownedIterations.add(iteration = it_OrderedSet_OrderedSet_T_reject);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _OrderedSet_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_OrderedSet_T, true));
 			ownedIterations.add(iteration = it_OrderedSet_OrderedSet_T_select);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _OrderedSet_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_OrderedSet_T, true));
 			ownedIterations.add(iteration = it_OrderedSet_OrderedSet_T_sortedBy);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _OrderedSet_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_OrderedSet_T_1, true));
 			ownedIterations = _Sequence_Sequence_T.getOwnedOperation();
 			ownedIterations.add(iteration = it_Sequence_Sequence_T_closure);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Sequence_T, true));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Sequence_T_2, true));
 			ownedIterations.add(iteration = it_Sequence_Sequence_T_collectNested);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Sequence_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Sequence_T_3, true));
 			ownedIterations.add(iteration = it_Sequence_Sequence_T_collect);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Sequence_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Sequence_T_4, true));
 			ownedIterations.add(iteration = it_Sequence_Sequence_T_reject);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Sequence_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Sequence_T, true));
 			ownedIterations.add(iteration = it_Sequence_Sequence_T_select);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Sequence_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Sequence_T, true));
 			ownedIterations.add(iteration = it_Sequence_Sequence_T_sortedBy);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Sequence_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Sequence_T_1, true));
 			ownedIterations = _Set_Set_T.getOwnedOperation();
 			ownedIterations.add(iteration = it_Set_Set_T_reject);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Set_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Set_T, true));
 			ownedIterations.add(iteration = it_Set_Set_T_select);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Set_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Set_T, true));
 			ownedIterations.add(iteration = it_Set_Set_T_sortedBy);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _Set_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_Set_T_1, true));
 			ownedIterations = _UniqueCollection_UniqueCollection_T_1.getOwnedOperation();
 			ownedIterations.add(iteration = it_UniqueCollection_UniqueCollection_T_sortedBy);
 			ownedParameters = iteration.getOwnedIterator();
 			ownedParameters.add(parameter = createParameter("i", _UniqueCollection_T, false));
 			ownedParameters = iteration.getOwnedParameter();
 			ownedParameters.add(parameter = createParameter("body", _Lambda_UniqueCollection_T, true));
 		}
 			
 		protected final @NonNull Property pr_Collection_Collection_T_elementType = createProperty("elementType", _Collection_T);
 		protected final @NonNull Property pr_Collection_Collection_T_lower = createProperty("lower", _Integer);
 		protected final @NonNull Property pr_Collection_Collection_T_upper = createProperty("upper", _Integer);
 		protected final @NonNull Property pr_Enumeration_allLiterals = createProperty("allLiterals", _OrderedSet_EnumerationLiteral);
 		
 		protected void installProperties() {
 			List<Property> ownedProperties;
 			Property property;
 			ownedProperties = _Collection_Collection_T.getOwnedAttribute();
 			ownedProperties.add(property = pr_Collection_Collection_T_elementType);
 			property.setIsResolveProxies(true);
 			property.setIsStatic(true);
 			property.setImplementationClass("org.eclipse.ocl.examples.library.collection.CollectionElementTypeProperty");
 			property.setImplementation(org.eclipse.ocl.examples.library.collection.CollectionElementTypeProperty.INSTANCE);
 			ownedProperties.add(property = pr_Collection_Collection_T_lower);
 			property.setIsResolveProxies(true);
 			property.setIsStatic(true);
 			property.setImplementationClass("org.eclipse.ocl.examples.library.collection.CollectionLowerProperty");
 			property.setImplementation(org.eclipse.ocl.examples.library.collection.CollectionLowerProperty.INSTANCE);
 			ownedProperties.add(property = pr_Collection_Collection_T_upper);
 			property.setIsResolveProxies(true);
 			property.setIsStatic(true);
 			property.setImplementationClass("org.eclipse.ocl.examples.library.collection.CollectionUpperProperty");
 			property.setImplementation(org.eclipse.ocl.examples.library.collection.CollectionUpperProperty.INSTANCE);
 			ownedProperties = _Enumeration.getOwnedAttribute();
 			ownedProperties.add(property = pr_Enumeration_allLiterals);
 			property.setIsResolveProxies(true);
 			property.setIsStatic(true);
 			property.setImplementationClass("org.eclipse.ocl.examples.library.enumeration.EnumerationOwnedLiteralProperty");
 			property.setImplementation(org.eclipse.ocl.examples.library.enumeration.EnumerationOwnedLiteralProperty.INSTANCE);
 		}
 		protected final @NonNull TypeTemplateParameter tp_UnlimitedNatural_oclAsType = createTypeTemplateParameter(_UnlimitedNatural_oclAsType_TT);
 		protected final @NonNull TypeTemplateParameter tp_Bag_Bag_T_collectNested = createTypeTemplateParameter(_Bag_collectNested_V);
 		protected final @NonNull TypeTemplateParameter tp_Bag_Bag_T_collect = createTypeTemplateParameter(_Bag_collect_V);
 		protected final @NonNull TypeTemplateParameter tp_Bag_Bag_T_flatten = createTypeTemplateParameter(_Bag_flatten_T2);
 		protected final @NonNull TypeTemplateParameter tp_Bag_Bag_T_selectByKind = createTypeTemplateParameter(_Bag_selectByKind_TT);
 		protected final @NonNull TypeTemplateParameter tp_Bag_Bag_T_selectByType = createTypeTemplateParameter(_Bag_selectByType_TT);
 		protected final @NonNull TypeTemplateParameter tp_Bag_Bag_T = createTypeTemplateParameter(_Bag_T);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_collectNested = createTypeTemplateParameter(_Collection_collectNested_V);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_collect = createTypeTemplateParameter(_Collection_collect_V);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_excludesAll = createTypeTemplateParameter(_Collection_excludesAll_T2);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_flatten = createTypeTemplateParameter(_Collection_flatten_T2);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_includesAll = createTypeTemplateParameter(_Collection_includesAll_T2);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_iterate = createTypeTemplateParameter(_Collection_iterate_Tacc);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_product = createTypeTemplateParameter(_Collection_product_T2);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_selectByKind = createTypeTemplateParameter(_Collection_selectByKind_TT);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T_selectByType = createTypeTemplateParameter(_Collection_selectByType_TT);
 		protected final @NonNull TypeTemplateParameter tp_Collection_Collection_T = createTypeTemplateParameter(_Collection_T);
 		protected final @NonNull TypeTemplateParameter tp_Metaclass = createTypeTemplateParameter(_Metaclass_T);
 		protected final @NonNull TypeTemplateParameter tp_OclAny_oclAsType = createTypeTemplateParameter(_OclAny_oclAsType_TT);
 		protected final @NonNull TypeTemplateParameter tp_OclAny_oclIsKindOf = createTypeTemplateParameter(_OclAny_oclIsKindOf_T);
 		protected final @NonNull TypeTemplateParameter tp_OclAny_oclIsTypeOf = createTypeTemplateParameter(_OclAny_oclIsTypeOf_T);
 		protected final @NonNull TypeTemplateParameter tp_OclInvalid_oclAsType = createTypeTemplateParameter(_OclInvalid_oclAsType_TT);
 		protected final @NonNull TypeTemplateParameter tp_OclInvalid_oclIsKindOf = createTypeTemplateParameter(_OclInvalid_oclIsKindOf_T);
 		protected final @NonNull TypeTemplateParameter tp_OclInvalid_oclIsTypeOf = createTypeTemplateParameter(_OclInvalid_oclIsTypeOf_T);
 		protected final @NonNull TypeTemplateParameter tp_OrderedSet_OrderedSet_T_flatten = createTypeTemplateParameter(_OrderedSet_flatten_T2);
 		protected final @NonNull TypeTemplateParameter tp_OrderedSet_OrderedSet_T_selectByKind = createTypeTemplateParameter(_OrderedSet_selectByKind_TT);
 		protected final @NonNull TypeTemplateParameter tp_OrderedSet_OrderedSet_T_selectByType = createTypeTemplateParameter(_OrderedSet_selectByType_TT);
 		protected final @NonNull TypeTemplateParameter tp_OrderedSet_OrderedSet_T = createTypeTemplateParameter(_OrderedSet_T);
 		protected final @NonNull TypeTemplateParameter tp_Sequence_Sequence_T_collectNested = createTypeTemplateParameter(_Sequence_collectNested_V);
 		protected final @NonNull TypeTemplateParameter tp_Sequence_Sequence_T_collect = createTypeTemplateParameter(_Sequence_collect_V);
 		protected final @NonNull TypeTemplateParameter tp_Sequence_Sequence_T_flatten = createTypeTemplateParameter(_Sequence_flatten_T2);
 		protected final @NonNull TypeTemplateParameter tp_Sequence_Sequence_T_selectByKind = createTypeTemplateParameter(_Sequence_selectByKind_TT);
 		protected final @NonNull TypeTemplateParameter tp_Sequence_Sequence_T_selectByType = createTypeTemplateParameter(_Sequence_selectByType_TT);
 		protected final @NonNull TypeTemplateParameter tp_Sequence_Sequence_T = createTypeTemplateParameter(_Sequence_T);
 		protected final @NonNull TypeTemplateParameter tp_Set_Set_T_flatten = createTypeTemplateParameter(_Set_flatten_T2);
 		protected final @NonNull TypeTemplateParameter tp_Set_Set_T_selectByKind = createTypeTemplateParameter(_Set_selectByKind_TT);
 		protected final @NonNull TypeTemplateParameter tp_Set_Set_T_selectByType = createTypeTemplateParameter(_Set_selectByType_TT);
 		protected final @NonNull TypeTemplateParameter tp_Set_Set_T = createTypeTemplateParameter(_Set_T);
 		protected final @NonNull TypeTemplateParameter tp_UniqueCollection_UniqueCollection_T = createTypeTemplateParameter(_UniqueCollection_T);
 		
 		protected final @NonNull TemplateSignature ts_UnlimitedNatural_oclAsType = createTemplateSignature(op_UnlimitedNatural_oclAsType, tp_UnlimitedNatural_oclAsType);
 		protected final @NonNull TemplateSignature ts_Bag_Bag_T = createTemplateSignature(_Bag_Bag_T, tp_Bag_Bag_T);
 		protected final @NonNull TemplateSignature ts_Bag_Bag_T_collectNested = createTemplateSignature(it_Bag_Bag_T_collectNested, tp_Bag_Bag_T_collectNested);
 		protected final @NonNull TemplateSignature ts_Bag_Bag_T_collect = createTemplateSignature(it_Bag_Bag_T_collect, tp_Bag_Bag_T_collect);
 		protected final @NonNull TemplateSignature ts_Bag_Bag_T_flatten = createTemplateSignature(op_Bag_Bag_T_flatten, tp_Bag_Bag_T_flatten);
 		protected final @NonNull TemplateSignature ts_Bag_Bag_T_selectByKind = createTemplateSignature(op_Bag_Bag_T_selectByKind, tp_Bag_Bag_T_selectByKind);
 		protected final @NonNull TemplateSignature ts_Bag_Bag_T_selectByType = createTemplateSignature(op_Bag_Bag_T_selectByType, tp_Bag_Bag_T_selectByType);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T = createTemplateSignature(_Collection_Collection_T, tp_Collection_Collection_T);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_collectNested = createTemplateSignature(it_Collection_Collection_T_collectNested, tp_Collection_Collection_T_collectNested);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_collect = createTemplateSignature(it_Collection_Collection_T_collect, tp_Collection_Collection_T_collect);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_excludesAll = createTemplateSignature(op_Collection_Collection_T_excludesAll, tp_Collection_Collection_T_excludesAll);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_flatten = createTemplateSignature(op_Collection_Collection_T_flatten, tp_Collection_Collection_T_flatten);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_includesAll = createTemplateSignature(op_Collection_Collection_T_includesAll, tp_Collection_Collection_T_includesAll);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_iterate = createTemplateSignature(it_Collection_Collection_T_iterate, tp_Collection_Collection_T_iterate);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_product = createTemplateSignature(op_Collection_Collection_T_product, tp_Collection_Collection_T_product);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_selectByKind = createTemplateSignature(op_Collection_Collection_T_selectByKind, tp_Collection_Collection_T_selectByKind);
 		protected final @NonNull TemplateSignature ts_Collection_Collection_T_selectByType = createTemplateSignature(op_Collection_Collection_T_selectByType, tp_Collection_Collection_T_selectByType);
 		protected final @NonNull TemplateSignature ts_Metaclass = createTemplateSignature(_Metaclass, tp_Metaclass);
 		protected final @NonNull TemplateSignature ts_OclAny_oclAsType = createTemplateSignature(op_OclAny_oclAsType, tp_OclAny_oclAsType);
 		protected final @NonNull TemplateSignature ts_OclAny_oclIsKindOf = createTemplateSignature(op_OclAny_oclIsKindOf, tp_OclAny_oclIsKindOf);
 		protected final @NonNull TemplateSignature ts_OclAny_oclIsTypeOf = createTemplateSignature(op_OclAny_oclIsTypeOf, tp_OclAny_oclIsTypeOf);
 		protected final @NonNull TemplateSignature ts_OclInvalid_oclAsType = createTemplateSignature(op_OclInvalid_oclAsType, tp_OclInvalid_oclAsType);
 		protected final @NonNull TemplateSignature ts_OclInvalid_oclIsKindOf = createTemplateSignature(op_OclInvalid_oclIsKindOf, tp_OclInvalid_oclIsKindOf);
 		protected final @NonNull TemplateSignature ts_OclInvalid_oclIsTypeOf = createTemplateSignature(op_OclInvalid_oclIsTypeOf, tp_OclInvalid_oclIsTypeOf);
 		protected final @NonNull TemplateSignature ts_OrderedSet_OrderedSet_T = createTemplateSignature(_OrderedSet_OrderedSet_T, tp_OrderedSet_OrderedSet_T);
 		protected final @NonNull TemplateSignature ts_OrderedSet_OrderedSet_T_flatten = createTemplateSignature(op_OrderedSet_OrderedSet_T_flatten, tp_OrderedSet_OrderedSet_T_flatten);
 		protected final @NonNull TemplateSignature ts_OrderedSet_OrderedSet_T_selectByKind = createTemplateSignature(op_OrderedSet_OrderedSet_T_selectByKind, tp_OrderedSet_OrderedSet_T_selectByKind);
 		protected final @NonNull TemplateSignature ts_OrderedSet_OrderedSet_T_selectByType = createTemplateSignature(op_OrderedSet_OrderedSet_T_selectByType, tp_OrderedSet_OrderedSet_T_selectByType);
 		protected final @NonNull TemplateSignature ts_Sequence_Sequence_T = createTemplateSignature(_Sequence_Sequence_T, tp_Sequence_Sequence_T);
 		protected final @NonNull TemplateSignature ts_Sequence_Sequence_T_collectNested = createTemplateSignature(it_Sequence_Sequence_T_collectNested, tp_Sequence_Sequence_T_collectNested);
 		protected final @NonNull TemplateSignature ts_Sequence_Sequence_T_collect = createTemplateSignature(it_Sequence_Sequence_T_collect, tp_Sequence_Sequence_T_collect);
 		protected final @NonNull TemplateSignature ts_Sequence_Sequence_T_flatten = createTemplateSignature(op_Sequence_Sequence_T_flatten, tp_Sequence_Sequence_T_flatten);
 		protected final @NonNull TemplateSignature ts_Sequence_Sequence_T_selectByKind = createTemplateSignature(op_Sequence_Sequence_T_selectByKind, tp_Sequence_Sequence_T_selectByKind);
 		protected final @NonNull TemplateSignature ts_Sequence_Sequence_T_selectByType = createTemplateSignature(op_Sequence_Sequence_T_selectByType, tp_Sequence_Sequence_T_selectByType);
 		protected final @NonNull TemplateSignature ts_Set_Set_T = createTemplateSignature(_Set_Set_T, tp_Set_Set_T);
 		protected final @NonNull TemplateSignature ts_Set_Set_T_flatten = createTemplateSignature(op_Set_Set_T_flatten, tp_Set_Set_T_flatten);
 		protected final @NonNull TemplateSignature ts_Set_Set_T_selectByKind = createTemplateSignature(op_Set_Set_T_selectByKind, tp_Set_Set_T_selectByKind);
 		protected final @NonNull TemplateSignature ts_Set_Set_T_selectByType = createTemplateSignature(op_Set_Set_T_selectByType, tp_Set_Set_T_selectByType);
 		protected final @NonNull TemplateSignature ts_UniqueCollection_UniqueCollection_T = createTemplateSignature(_UniqueCollection_UniqueCollection_T_1, tp_UniqueCollection_UniqueCollection_T);
 		
 		protected void installTemplateSignatures() {
 		}
 		
 		protected void installTemplateBindings() {
 			_Bag_Tuple.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Tuple)));
 			_Bag_Bag_collect_V.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Bag_collect_V)));
 			_Bag_Bag_flatten_T2.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Bag_flatten_T2)));
 			_Bag_Bag_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Bag_selectByKind_TT)));
 			_Bag_Bag_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Bag_selectByType_TT)));
 			_Bag_Collection_T.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Collection_T)));
 			_Bag_Set_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Set_selectByKind_TT)));
 			_Bag_Set_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Set_selectByType_TT)));
 			_Bag_Set_T.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _Set_T)));
 			_Bag_UniqueCollection_T.getTemplateBinding().add(createTemplateBinding(ts_Bag_Bag_T,
 				createTemplateParameterSubstitution(tp_Bag_Bag_T, _UniqueCollection_T)));
 			_Collection_Integer.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Integer)));
 			_Collection_Tuple.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Tuple)));
 			_Collection_Bag_collect_V.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Bag_collect_V)));
 			_Collection_Bag_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Bag_selectByKind_TT)));
 			_Collection_Bag_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Bag_selectByType_TT)));
 			_Collection_Bag_T.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Bag_T)));
 			_Collection_Collection_collect_V.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_collect_V)));
 			_Collection_Collection_excludesAll_T2.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_excludesAll_T2)));
 			_Collection_Collection_flatten_T2.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_flatten_T2)));
 			_Collection_Collection_includesAll_T2.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_includesAll_T2)));
 			_Collection_Collection_product_T2.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_product_T2)));
 			_Collection_Collection_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_selectByKind_TT)));
 			_Collection_Collection_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Collection_selectByType_TT)));
 			_Collection_OrderedSet_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _OrderedSet_selectByKind_TT)));
 			_Collection_OrderedSet_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _OrderedSet_selectByType_TT)));
 			_Collection_Sequence_collect_V.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Sequence_collect_V)));
 			_Collection_Sequence_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Sequence_selectByKind_TT)));
 			_Collection_Sequence_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Sequence_selectByType_TT)));
 			_Collection_Sequence_T.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Sequence_T)));
 			_Collection_Set_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Set_selectByKind_TT)));
 			_Collection_Set_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Set_selectByType_TT)));
 			_Collection_Set_T.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _Set_T)));
 			_Collection_UniqueCollection_T.getTemplateBinding().add(createTemplateBinding(ts_Collection_Collection_T,
 				createTemplateParameterSubstitution(tp_Collection_Collection_T, _UniqueCollection_T)));
 			_Metaclass_UnlimitedNatural_oclAsType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _UnlimitedNatural_oclAsType_TT)));
 			_Metaclass_Bag_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Bag_selectByKind_TT)));
 			_Metaclass_Bag_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Bag_selectByType_TT)));
 			_Metaclass_Collection_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Collection_selectByKind_TT)));
 			_Metaclass_Collection_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Collection_selectByType_TT)));
 			_Metaclass_OclAny_oclAsType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclAny_oclAsType_TT)));
 			_Metaclass_OclAny_oclIsKindOf_T.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclAny_oclIsKindOf_T)));
 			_Metaclass_OclAny_oclIsTypeOf_T.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclAny_oclIsTypeOf_T)));
 			_Metaclass_OclInvalid_oclAsType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclInvalid_oclAsType_TT)));
 			_Metaclass_OclInvalid_oclIsKindOf_T.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclInvalid_oclIsKindOf_T)));
 			_Metaclass_OclInvalid_oclIsTypeOf_T.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclInvalid_oclIsTypeOf_T)));
 			_Metaclass_OclInvalid.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclInvalid)));
 			_Metaclass_OclSelf.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclSelf)));
 			_Metaclass_OclVoid.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OclVoid)));
 			_Metaclass_OrderedSet_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OrderedSet_selectByKind_TT)));
 			_Metaclass_OrderedSet_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _OrderedSet_selectByType_TT)));
 			_Metaclass_Sequence_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Sequence_selectByKind_TT)));
 			_Metaclass_Sequence_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Sequence_selectByType_TT)));
 			_Metaclass_Set_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Set_selectByKind_TT)));
 			_Metaclass_Set_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Metaclass,
 				createTemplateParameterSubstitution(tp_Metaclass, _Set_selectByType_TT)));
 			_OrderedSet_Collection_T.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _Collection_T)));
 			_OrderedSet_EnumerationLiteral.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _EnumerationLiteral)));
 			_OrderedSet_OrderedSet_flatten_T2.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _OrderedSet_flatten_T2)));
 			_OrderedSet_OrderedSet_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _OrderedSet_selectByKind_TT)));
 			_OrderedSet_OrderedSet_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _OrderedSet_selectByType_TT)));
 			_OrderedSet_Sequence_T.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _Sequence_T)));
 			_OrderedSet_Set_T.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _Set_T)));
 			_OrderedSet_UniqueCollection_T.getTemplateBinding().add(createTemplateBinding(ts_OrderedSet_OrderedSet_T,
 				createTemplateParameterSubstitution(tp_OrderedSet_OrderedSet_T, _UniqueCollection_T)));
 			_Sequence_Integer.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Integer)));
 			_Sequence_String.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _String)));
 			_Sequence_Bag_T.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Bag_T)));
 			_Sequence_Collection_T.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Collection_T)));
 			_Sequence_OrderedSet_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _OrderedSet_selectByKind_TT)));
 			_Sequence_OrderedSet_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _OrderedSet_selectByType_TT)));
 			_Sequence_OrderedSet_T.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _OrderedSet_T)));
 			_Sequence_Sequence_collect_V.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Sequence_collect_V)));
 			_Sequence_Sequence_flatten_T2.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Sequence_flatten_T2)));
 			_Sequence_Sequence_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Sequence_selectByKind_TT)));
 			_Sequence_Sequence_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Sequence_selectByType_TT)));
 			_Sequence_Set_T.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _Set_T)));
 			_Sequence_UniqueCollection_T.getTemplateBinding().add(createTemplateBinding(ts_Sequence_Sequence_T,
 				createTemplateParameterSubstitution(tp_Sequence_Sequence_T, _UniqueCollection_T)));
 			_Set_Tuple.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _Tuple)));
 			_Set_Bag_T.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _Bag_T)));
 			_Set_Collection_T.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _Collection_T)));
 			_Set_OclElement.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _OclElement)));
 			_Set_OclSelf.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _OclSelf)));
 			_Set_Set_flatten_T2.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _Set_flatten_T2)));
 			_Set_Set_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _Set_selectByKind_TT)));
 			_Set_Set_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _Set_selectByType_TT)));
 			_Set_UniqueCollection_T.getTemplateBinding().add(createTemplateBinding(ts_Set_Set_T,
 				createTemplateParameterSubstitution(tp_Set_Set_T, _UniqueCollection_T)));
 			_UniqueCollection_Tuple.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _Tuple)));
 			_UniqueCollection_Collection_T.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _Collection_T)));
 			_UniqueCollection_OclAny.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _OclAny)));
 			_UniqueCollection_OrderedSet_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _OrderedSet_selectByKind_TT)));
 			_UniqueCollection_OrderedSet_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _OrderedSet_selectByType_TT)));
 			_UniqueCollection_OrderedSet_T.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _OrderedSet_T)));
 			_UniqueCollection_Set_selectByKind_TT.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _Set_selectByKind_TT)));
 			_UniqueCollection_Set_selectByType_TT.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _Set_selectByType_TT)));
 			_UniqueCollection_Set_T.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _Set_T)));
 			_UniqueCollection_UniqueCollection_T.getTemplateBinding().add(createTemplateBinding(ts_UniqueCollection_UniqueCollection_T,
 				createTemplateParameterSubstitution(tp_UniqueCollection_UniqueCollection_T, _UniqueCollection_T)));
 		}
 		
 		protected void installPrecedences() {
 			final Precedence prec_ADDITIVE = createPrecedence("ADDITIVE", AssociativityKind.LEFT);
 			final Precedence prec_AND = createPrecedence("AND", AssociativityKind.LEFT);
 			final Precedence prec_EQUALITY = createPrecedence("EQUALITY", AssociativityKind.LEFT);
 			final Precedence prec_IMPLIES = createPrecedence("IMPLIES", AssociativityKind.LEFT);
 			final Precedence prec_MULTIPLICATIVE = createPrecedence("MULTIPLICATIVE", AssociativityKind.LEFT);
 			final Precedence prec_NAVIGATION = createPrecedence("NAVIGATION", AssociativityKind.LEFT);
 			final Precedence prec_OR = createPrecedence("OR", AssociativityKind.LEFT);
 			final Precedence prec_RELATIONAL = createPrecedence("RELATIONAL", AssociativityKind.LEFT);
 			final Precedence prec_UNARY = createPrecedence("UNARY", AssociativityKind.LEFT);
 			final Precedence prec_XOR = createPrecedence("XOR", AssociativityKind.LEFT);
 		
 			final List<Precedence> ownedPrecedences = library.getOwnedPrecedence();
 			ownedPrecedences.add(prec_NAVIGATION);
 			ownedPrecedences.add(prec_UNARY);
 			ownedPrecedences.add(prec_MULTIPLICATIVE);
 			ownedPrecedences.add(prec_ADDITIVE);
 			ownedPrecedences.add(prec_RELATIONAL);
 			ownedPrecedences.add(prec_EQUALITY);
 			ownedPrecedences.add(prec_AND);
 			ownedPrecedences.add(prec_OR);
 			ownedPrecedences.add(prec_XOR);
 			ownedPrecedences.add(prec_IMPLIES);
 		
 			op_Boolean__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_Boolean__eq_.setPrecedence(prec_EQUALITY);
 			op_Boolean_and.setPrecedence(prec_AND);
 			op_Boolean_implies.setPrecedence(prec_IMPLIES);
 			op_Boolean_not.setPrecedence(prec_UNARY);
 			op_Boolean_or.setPrecedence(prec_OR);
 			op_Boolean_xor.setPrecedence(prec_XOR);
 			op_Integer__mul_.setPrecedence(prec_MULTIPLICATIVE);
 			op_Integer__add_.setPrecedence(prec_ADDITIVE);
 			op_Integer__neg_.setPrecedence(prec_UNARY);
 			op_Integer__neg__1.setPrecedence(prec_ADDITIVE);
 			op_Integer__div_.setPrecedence(prec_MULTIPLICATIVE);
 			op_Real__mul_.setPrecedence(prec_MULTIPLICATIVE);
 			op_Real__add_.setPrecedence(prec_ADDITIVE);
 			op_Real__neg_.setPrecedence(prec_UNARY);
 			op_Real__neg__1.setPrecedence(prec_ADDITIVE);
 			op_Real__div_.setPrecedence(prec_MULTIPLICATIVE);
 			op_Real__lt_.setPrecedence(prec_RELATIONAL);
 			op_Real__lt__eq_.setPrecedence(prec_RELATIONAL);
 			op_Real__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_Real__eq_.setPrecedence(prec_EQUALITY);
 			op_Real__gt_.setPrecedence(prec_RELATIONAL);
 			op_Real__gt__eq_.setPrecedence(prec_RELATIONAL);
 			op_String__add_.setPrecedence(prec_ADDITIVE);
 			op_String__lt_.setPrecedence(prec_RELATIONAL);
 			op_String__lt__eq_.setPrecedence(prec_RELATIONAL);
 			op_String__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_String__eq_.setPrecedence(prec_EQUALITY);
 			op_String__gt_.setPrecedence(prec_RELATIONAL);
 			op_String__gt__eq_.setPrecedence(prec_RELATIONAL);
 			op_Bag_Bag_T__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_Bag_Bag_T__eq_.setPrecedence(prec_EQUALITY);
 			op_Collection_Collection_T__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_Collection_Collection_T__eq_.setPrecedence(prec_EQUALITY);
 			op_OclAny__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_OclAny__eq_.setPrecedence(prec_EQUALITY);
 			op_OclComparable__lt_.setPrecedence(prec_RELATIONAL);
 			op_OclComparable__lt__eq_.setPrecedence(prec_RELATIONAL);
 			op_OclComparable__gt_.setPrecedence(prec_RELATIONAL);
 			op_OclComparable__gt__eq_.setPrecedence(prec_RELATIONAL);
 			op_OclInvalid__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_OclInvalid__eq_.setPrecedence(prec_EQUALITY);
 			op_OclInvalid_and.setPrecedence(prec_AND);
 			op_OclInvalid_implies.setPrecedence(prec_IMPLIES);
 			op_OclInvalid_or.setPrecedence(prec_OR);
 			op_OclTuple__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_OclTuple__eq_.setPrecedence(prec_EQUALITY);
 			op_OclVoid__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_OclVoid__eq_.setPrecedence(prec_EQUALITY);
 			op_OclVoid_and.setPrecedence(prec_AND);
 			op_OclVoid_implies.setPrecedence(prec_IMPLIES);
 			op_OclVoid_or.setPrecedence(prec_OR);
 			op_OrderedSet_OrderedSet_T__neg_.setPrecedence(prec_ADDITIVE);
 			op_OrderedSet_OrderedSet_T__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_OrderedSet_OrderedSet_T__eq_.setPrecedence(prec_EQUALITY);
 			op_Sequence_Sequence_T__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_Sequence_Sequence_T__eq_.setPrecedence(prec_EQUALITY);
 			op_Set_Set_T__neg_.setPrecedence(prec_ADDITIVE);
 			op_Set_Set_T__lt__gt_.setPrecedence(prec_EQUALITY);
 			op_Set_Set_T__eq_.setPrecedence(prec_EQUALITY);
 			op_UniqueCollection_UniqueCollection_T__neg_.setPrecedence(prec_ADDITIVE);
 		}
 		
 		protected void installComments() {
 			installComment(op_Boolean__lt__gt_, "Returns oclText[true] if the logical value of oclText[self] is the not same as the numeric value of object2, oclText[false] otherwise.");
 			installComment(op_Boolean__eq_, "Returns oclText[true] if the logical value of oclText[self] is the same as the numeric value of object2, oclText[false] otherwise.");
 			installComment(op_Boolean_allInstances, "Returns oclText[Set{false, true}].");
 			installComment(op_Boolean_and, "True if both b1 and b are oclText[true].");
 			installComment(op_Boolean_implies, "True if oclText[self] is oclText[false], or if oclText[self] is oclText[true] and b is oclText[true].");
 			installComment(op_Boolean_not, "True if oclText[self] is oclText[false].");
 			installComment(op_Boolean_or, "True if either oclText[self] or b is oclText[true].");
 			installComment(op_Boolean_toString, "Converts oclText[self] to a string value.");
 			installComment(op_Boolean_xor, "True if either oclText[self] or b is oclText[true], but not both.");
 			installComment(op_Integer__mul_, "The value of the multiplication of oclText[self] and i.");
 			installComment(op_Integer__add_, "The value of the addition of oclText[self] and i.");
 			installComment(op_Integer__neg_, "The negative value of oclText[self].");
 			installComment(op_Integer__neg__1, "The value of the subtraction of i from oclText[self].");
 			installComment(op_Integer__div_, "The value of oclText[self] divided by i.\nEvaluates to oclText[invalid] if r is equal to zero.");
 			installComment(op_Integer_abs, "The absolute value of oclText[self].");
 			installComment(op_Integer_compareTo, "The comparison of oclText[self] with oclText[that]. -ve if less than, 0 if equal, +ve if greater than.");
 			installComment(op_Integer_div, "The number of times that i fits completely within oclText[self].");
 			installComment(op_Integer_max, "The maximum of oclText[self] an i.");
 			installComment(op_Integer_min, "The minimum of oclText[self] an i.");
 			installComment(op_Integer_mod, "The result is oclText[self] modulo i.");
 			installComment(op_Integer_toString, "Converts oclText[self] to a string value.");
 			installComment(op_Real__mul_, "The value of the multiplication of oclText[self] and r.");
 			installComment(op_Real__add_, "The value of the addition of oclText[self] and r.");
 			installComment(op_Real__neg_, "The negative value of oclText[self].");
 			installComment(op_Real__neg__1, "The value of the subtraction of r from oclText[self].");
 			installComment(op_Real__div_, "The value of oclText[self] divided by r. Evaluates to oclText[invalid] if r is equal to zero.");
 			installComment(op_Real__lt_, "True if oclText[self] is less than r.");
 			installComment(op_Real__lt__eq_, "True if oclText[self] is less than or equal to r.");
 			installComment(op_Real__lt__gt_, "Returns oclText[true] if the numeric value of oclText[self] is the not the same as the numeric value of object2, oclText[false] otherwise.");
 			installComment(op_Real__eq_, "Returns oclText[true] if the numeric value of oclText[self] is the same as the numeric value of object2, oclText[false] otherwise.");
 			installComment(op_Real__gt_, "True if oclText[self] is greater than r.");
 			installComment(op_Real__gt__eq_, "True if oclText[self] is greater than or equal to r.");
 			installComment(op_Real_abs, "The absolute value of oclText[self].");
 			installComment(op_Real_compareTo, "The comparison of oclText[self] with oclText[that]. -ve if less than, 0 if equal, +ve if greater than.");
 			installComment(op_Real_floor, "The largest integer that is less than or equal to oclText[self].");
 			installComment(op_Real_max, "The maximum of oclText[self] and r.");
 			installComment(op_Real_min, "The minimum of oclText[self] and r.");
 			installComment(op_Real_round, "The integer that is closest to oclText[self]. When there are two such integers, the largest one.");
 			installComment(op_Real_toString, "Converts oclText[self] to a string value.");
 			installComment(op_String__add_, "The concatenation of oclText[self] and s.");
 			installComment(op_String__lt_, "True if oclText[self] is less than s, using the locale defined by looking up oclLocale in the current environment.");
 			installComment(op_String__lt__eq_, "True if oclText[self] is less than or equal to s, using the locale defined by looking up oclLocale in the current environment.");
 			installComment(op_String__gt_, "True if oclText[self] is greater than s, using the locale defined by looking up oclLocale in the current environment.");
 			installComment(op_String__gt__eq_, "True if oclText[self] is greater than or equal to s, using the locale defined by looking up oclLocale in the current environment.");
 			installComment(op_String_at, "Queries the character at position i in oclText[self].");
 			installComment(op_String_characters, "Obtains the characters of oclText[self] as a sequence.");
 			installComment(op_String_compareTo, "The comparison of oclText[self] with oclText[that]. -ve if less than, 0 if equal, +ve if greater than.");
 			installComment(op_String_concat, "The concatenation of oclText[self] and s.");
 			installComment(op_String_endsWith, "Returns true if oclText[self] ends with the string s.\nEvery string ends with the empty string.");
 			installComment(op_String_equalsIgnoreCase, "Queries whether s and oclText[self] are equivalent under case-insensitive collation.");
 			installComment(op_String_indexOf, "Queries the first index in oclText[self] at which s is a substring of oclText[self], or zero if s is not a substring of oclText[self].\nThe empty string is a substring of every string at index 1 (and also at all other indexes).");
 			installComment(op_String_lastIndexOf, "Queries the last in oclText[self] at which s is a substring of oclText[self], or zero if s is not a substring of oclText[self].\nThe empty string is a substring of every string at index oclText[self]-size()+1 (and also at all other indexes).");
 			installComment(op_String_matches, "Use a regular expression match and return true if self matches regex, false otherwise.");
 			installComment(op_String_replaceAll, "Return a string derived from self by replacing all matches of regex by replacement.");
 			installComment(op_String_replaceFirst, "Return a string derived from self by replacing the first match of regex by replacement.");
 			installComment(op_String_size, "The number of characters in oclText[self].");
 			installComment(op_String_startsWith, "Returns true if oclText[self] starts with the string s.\nEvery string starts with the empty string.");
 			installComment(op_String_substituteAll, "Return a string derived from self by replacing all occurrences of oldSubstring by newSubstring.");
 			installComment(op_String_substituteFirst, "Return a string derived from self by replacing the first occurrence of oldSubstring by newSubstring.\nReturns invalid if there is no first occurrence.");
 			installComment(op_String_substring, "The sub-string of oclText[self] starting at character number lower, up to and including character number upper. Character numbers run from 1 to self.size().");
 			installComment(op_String_toBoolean, "Converts oclText[self] to a boolean value.");
 			installComment(op_String_toInteger, "Converts oclText[self] to an Integer value.");
 			installComment(op_String_toLower, "This is a deprecated variant of toLowerCase() preserving compatibility with traditional Eclipse OCL behaviour.");
 			installComment(op_String_toLowerCase, "Converts oclText[self] to lower case, using the locale defined by looking up oclLocale in the current environment.\nOtherwise, returns the same string as oclText[self].");
 			installComment(op_String_toReal, "Converts oclText[self] to a Real value.");
 			installComment(op_String_toString, "Returns oclText[self].");
 			installComment(op_String_toUpper, "This is a deprecated variant of toUpperCase() preserving compatibility with traditional Eclipse OCL behaviour.");
 			installComment(op_String_toUpperCase, "Converts oclText[self] to upper case, using the locale defined by looking up oclLocale in the current environment.\nOtherwise, returns the same string as oclText[self].");
 			installComment(op_String_tokenize, "Partition oclText[self] into a sequence substrings separated by any of space, line-feed, carriage-return, form-feed and horizontal-tab delimiters.\nThe delimiters are omitted from the return.");
 			installComment(op_String_tokenize_1, "Partition oclText[self] into a sequence substrings separated by characters in the delimiters. The delimiters are omitted from the return.");
 			installComment(op_String_tokenize_2, "Partition oclText[self] into a sequence substrings separated by characters in the delimiters. If returnDelimeters is\ntrue the returned sequence includes the delimiters, otherwise the delimiters are omitted.");
 			installComment(op_String_trim, "Return oclText[self] with leading and trailing whitespace removed.");
 			installComment(op_UnlimitedNatural_oclAsType, "Evaluates to oclText[self], where oclText[self] is of the type identified by T.\nThe type T may be any classifier defined in the UML model;\nif the actual type of oclText[self] at evaluation time does not conform to T,\nthen the oclAsType operation evaluates to oclText[invalid].\n\nThe standard behavior is redefined for UnlimitedNatural. Numeric values may be converted to\nReal or Integer, but the e[unlimited] value may not.\nConversion of e[unlimited] to Real or Integer returns oclText[invalid].");
 			installComment(library, "This clause describes the OCL Standard Library of predefined types, their operations, and predefined expression templates in the OCL.\nThis sub clause contains all standard types defined within OCL, including all the operations defined on those types.\nFor each operation the signature and a description of the semantics is given.\nWithin the description, the reserved word \u2018result\u2019 is used to refer to the value that results from evaluating the operation.\nIn several places, post conditions are used to describe properties of the result.\nWhen there is more than one postcondition, all postconditions must be true.\nA similar thing is true for multiple preconditions.\nIf these are used, the operation is only defined if all preconditions evaluate to oclText[true].\n\nheading:1[Introduction]\n\nThe structure, syntax, and semantics of the OCL is defined in Clauses 8 (\u201CAbstract Syntax\u201D), 9 (\u201CConcrete Syntax\u201D),\nand 10 (\u201CSemantics Described using UML\u201D).\nThis sub clause adds another part to the OCL definition: a library of predefined types and operations.\nAny implementation of OCL must include this library package. This approach has also been taken by e.g., the Java definition,\nwhere the language definition and the standard libraries are both mandatory parts of the complete language definition.\n\nThe OCL standard library defines a number of types.\nIt includes several primitive types: UnlimitedNatural, Integer, Real, String, and Boolean.\nThese are familiar from many other languages. The second part of the standard library consists of the collection types.\nThey are Bag, Set, Sequence, and Collection where Collection is an abstract type.\nNote that all types defined in the OCL standard library are instances of an abstract syntax class.\nThe OCL standard library exists at the modeling level, also referred to as the M1 level, where the abstract syntax is the metalevel or M2 level.\n\nNext to definitions of types the OCL standard library defines a number of template expressions.\nMany operations defined on collections map not on the abstract syntax metaclass FeatureCallExp, but on the IteratorExp.\nFor each of these a template expression that defines the name and format of the expression is defined in 11.8, Predefined Iterator Expressions.\n\nThe Standard Library may be extended with new types, new operations and new iterators.\nIn particular new operations can be defined for collections.\n\nCertain String operations depend on the prevailing locale to ensure that Strings are collated and characters are case-converted\nin an appropriate fashion.\nA locale is defined as a concatenation of up to three character sequences separated by underscores,\nwith the first sequence identifying the language and the second sequence identifying the country.\nThe third sequence is empty but may encode an implementation-specific variant.\nTrailing empty strings and separators may be omitted.\n\nThe character sequences for languages are defined by ISO 639.\n\nThe character sequences for countries are defined by ISO 3166.\n\n\u2018fr_CA\u2019 therefore identifies the locale for the French language in the Canada country.\n\nComparison of strings and consequently the collation order of Collection::sortedBy()\nconforms to the Unicode Collation algorithm defined by Unicode Technical Standard#10.\n\nThe locale is \u2018en_us\u2019 by default but may be configured by a property constraint on OclAny::oclLocale.\n\nThe prevailing locale is defined by the prevailing value of oclLocale within the current environment;\nit may therefore be changed temporarily by using a Let expression.\nlet oclLocale : String = \u2018fr_CA\u2019 in aString.toUpperCase()\n\nheading:1[Iterators]\n\nThis sub clause defines the standard OCL iterator expressions.\nIn the abstract syntax these are all instances of IteratorExp.\nThese iterator expressions always have a collection expression as their source,\nas is defined in the well-formedness rules in Clause 8 (\u201CAbstract Syntax\u201D).\nThe defined iterator expressions are shown per source collection type.\nThe semantics of each iterator expression is defined through a mapping from the iterator to the \u2018iterate\u2019 construct.\nThis means that the semantics of the iterator expressions do not need to be defined separately in the semantics sub clauses.\n\nIn all of the following OCL expressions, the lefthand side of the equals sign is the IteratorExp to be defined,\nand the righthand side of the equals sign is the equivalent as an IterateExp.\nThe names source, body, and iterator refer to the role names in the abstract syntax:\n\nsource\tThe source expression of the IteratorExp.\n\nbody\tThe body expression of the IteratorExp.\n\niterator\tThe iterator variable of the IteratorExp.\n\nresult\tThe result variable of the IterateExp.\n\nheading:2[Extending the Standard Library with Iterator Expressions]\n\nIt is possible to add new iterator expressions in the standard library.\nIf this is done the semantics of a new iterator should be defined by mapping it to existing constructs,\nin the same way the semantics of pre-defined iterators is done (see sub clause 11.9)");
 			installComment(op_Bag_Bag_T__eq_, "True if oclText[self] and bag contain the same elements, the same number of times.");
 			installComment(it_Bag_Bag_T_closure, "The closure of applying body transitively to every distinct element of the source collection.");
 			installComment(it_Bag_Bag_T_collectNested, "The Bag of elements which results from applying body to every member of the source nonordered collection.");
 			installComment(op_Bag_Bag_T_excluding, "The bag containing all elements of oclText[self] apart from all occurrences of object.");
 			installComment(op_Bag_Bag_T_flatten, "Redefines the Collection operation. If the element type is not a collection type, this results in the same bag as oclText[self].\nIf the element type is a collection type, the result is the bag containing all the elements of all the recursively flattened elements of oclText[self].");
 			installComment(op_Bag_Bag_T_including, "The bag containing all elements of oclText[self] plus object.");
 			installComment(it_Bag_Bag_T_reject, "The sub-bag of the source bag for which body is oclText[false].\n\noclCode[self->reject(iterator | body) = self->select(iterator | not body)].");
 			installComment(it_Bag_Bag_T_select, "The sub-bag of the source bag for which body is oclText[true].\n\noclCode[self->select(iterator | body) =\nself->iterate(iterator; result : Bag(T) = Bag{} |\nif body then result->including(iterator)\nelse result\nendif)]");
 			installComment(it_Bag_Bag_T_sortedBy, "Results in the Sequence containing all elements of the source collection.\nThe element for which body has the lowest value comes first, and so on.\nThe type of the body expression must have the < operation defined.\nThe < operation must return a Boolean value and must be transitive (i.e., if a < b and b < c then a < c).");
 			installComment(op_Collection_Collection_T__lt__gt_, "True if c is not equal to oclText[self].");
 			installComment(op_Collection_Collection_T__eq_, "True if c is a collection of the same kind as oclText[self] and contains the same elements in the same quantities and in the same order,\nin the case of an ordered collection type.");
 			installComment(it_Collection_Collection_T_any, "Returns any element in the source collection for which body evaluates to oclText[true].\nIf there is more than one element for which body is oclText[true], one of them is returned.\nThere must be at least one element fulfilling body, otherwise the result of this IteratorExp is oclText[null].");
 			installComment(op_Collection_Collection_T_asBag, "The Bag that contains all the elements from oclText[self].");
 			installComment(op_Collection_Collection_T_asOrderedSet, "An OrderedSet that contains all the elements from oclText[self], with duplicates removed,\nin an order dependent on the particular concrete collection type.");
			installComment(op_Collection_Collection_T_asSequence, "Sequence that contains all the elements from oclText[self], in an order dependent on the particular concrete collection type.");
 			installComment(op_Collection_Collection_T_asSet, "The Set containing all the elements from oclText[self], with duplicates removed.");
 			installComment(it_Collection_Collection_T_collectNested, "The Collection of elements which results from applying body to every member of the source collection.");
 			installComment(it_Collection_Collection_T_collect, "The Collection of elements that results from applying body to every member of the source set.\nThe result is flattened. Notice that this is based on collectNested, which can be of different type depending on the type of source.\ncollectNested is defined individually for each subclass of CollectionType.");
 			installComment(op_Collection_Collection_T_count, "The number of times that object occurs in the collection oclText[self].");
 			installComment(pr_Collection_Collection_T_elementType, "Evaluates to the type of the collection elements.");
 			installComment(op_Collection_Collection_T_excludes, "True if object is not an element of oclText[self], oclText[false] otherwise.");
 			installComment(op_Collection_Collection_T_excludesAll, "Does oclText[self] contain none of the elements of c2 ?");
 			installComment(op_Collection_Collection_T_excluding, "The collection containing all elements of oclText[self] apart from object.");
 			installComment(it_Collection_Collection_T_exists_1, "Results in oclText[true] if body evaluates to oclText[true] for at least one element in the source collection.");
 			installComment(op_Collection_Collection_T_flatten, "If the element type is not a collection type, this results in the same collection as oclText[self].\nIf the element type is a collection type, the result is a collection containing all the elements of all the recursively flattened elements of oclText[self].");
 			installComment(it_Collection_Collection_T_forAll_1, "Results in oclText[true] if the body expression evaluates to oclText[true] for each element in the source collection; otherwise, result is oclText[false].");
 			installComment(op_Collection_Collection_T_includes, "True if object is an element of oclText[self], oclText[false] otherwise.");
 			installComment(op_Collection_Collection_T_includesAll, "Does oclText[self] contain all the elements of c2 ?");
 			installComment(op_Collection_Collection_T_including, "The collection containing all elements of oclText[self] plus object.");
 			installComment(op_Collection_Collection_T_intersection, "The intersection of oclText[self] and bag; the bag of all elements that are in both oclText[self] and c.");
 			installComment(op_Collection_Collection_T_intersection_1, "The intersection of oclText[self] and a unique collection; the set of all elements that are in both oclText[self] and u.");
 			installComment(op_Collection_Collection_T_isEmpty, "Is oclText[self] the empty collection?\n\nNote: oclText[null->isEmpty()] returns oclText[true] in virtue of the implicit casting from oclText[null] to oclText[Bag{}].");
 			installComment(it_Collection_Collection_T_isUnique, "Results in oclText[true] if body evaluates to a different value for each element in the source collection; otherwise, result is oclText[false].");
 			installComment(pr_Collection_Collection_T_lower, "Evaluates to the lower bound on the number of collection elements.");
 			installComment(op_Collection_Collection_T_max, "The element with the maximum value of all elements in oclText[self].\nElements must be of a type supporting the max operation.\nThe max operation - supported by the elements - must take one parameter of type T and be both associative and commutative.\nUnlimitedNatural, Integer and Real fulfill this condition.");
 			installComment(op_Collection_Collection_T_min, "The element with the minimum value of all elements in oclText[self].\nElements must be of a type supporting the min operation.\nThe min operation - supported by the elements - must take one parameter of type T and be both associative and commutative.\nUnlimitedNatural, Integer and Real fulfill this condition.");
 			installComment(op_Collection_Collection_T_notEmpty, "Is oclText[self] not the empty collection?\n\noclText[null->notEmpty()] returns oclText[false] in virtue of the implicit casting from oclText[null] to oclText[Bag{}].");
 			installComment(it_Collection_Collection_T_one, "Results in oclText[true] if there is exactly one element in the source collection for which body is oclText[true].");
 			installComment(op_Collection_Collection_T_product, "The cartesian product operation of oclText[self] and c2.");
 			installComment(it_Collection_Collection_T_reject, "The sub-collection of the source collection for which body is oclText[false].");
 			installComment(it_Collection_Collection_T_select, "The sub-collection of the source collection for which body is oclText[true].");
 			installComment(op_Collection_Collection_T_size, "The number of elements in the collection oclText[self].");
 			installComment(it_Collection_Collection_T_sortedBy, "Results in the Collection containing all elements of the source collection.\nThe element for which body has the lowest value comes first, and so on.\nThe type of the body expression must have the < operation defined.\nThe < operation must return a Boolean value and must be transitive (i.e., if a < b and b < c then a < c).");
 			installComment(op_Collection_Collection_T_sum, "The addition of all elements in oclText[self].\nElements must be of an oclText[OclSummable] type to provide the zero() and sum() operations.\nThe e[sum] operation must be both associative: a.sum(b).sum(c) = a.sum(b.sum(c)), and commutative: a.sum(b) = b.sum(a).\nUnlimitedNatural, Integer and Real fulfill this condition.\n\nIf the e[sum] operation is not both associative and commutative, the e[sum] expression is not well-formed,\nwhich may result in unpredictable results during evaluation.\nIf an implementation is able to detect a lack of associativity or commutativity,\nthe implementation may bypass the evaluation and return an oclText[invalid] result.");
 			installComment(op_Collection_Collection_T_union, "The bag consisting of all elements in oclText[self] and all elements in c.");
 			installComment(pr_Collection_Collection_T_upper, "Evaluates to the upper bound on the number of collection elements.");
 			installComment(_Enumeration, "The Enumeration type is the type of an OrderedSet of EnumerationLiteral.");
 			installComment(op_Enumeration_allInstances, "Return a set of all enumeration values of oclText[self].");
 			installComment(pr_Enumeration_allLiterals, "Evaluates to the literals of the enumeration.");
 			installComment(_EnumerationLiteral, "The standard type EnumerationLiteral represents a named constant value of an Enumeration.");
 			installComment(_OclAny, "All types in the UML model and the primitive and collection types in the OCL standard library conforms to the type OclAny.\nConceptually, OclAny behaves as a supertype for all the types.\nFeatures of OclAny are available on each object in all OCL expressions.\nOclAny is itself an instance of the metatype AnyType.\n\nAll classes in a UML model inherit all operations defined on OclAny.\nTo avoid name conflicts between properties in the model and the properties inherited from OclAny,\nall names on the properties of OclAny start with \u2018ocl.\u2019\nAlthough theoretically there may still be name conflicts, they can be avoided.\nOne can also use qualification by OclAny (name of the type) to explicitly refer to the OclAny properties.\n\nOperations of OclAny, where the instance of OclAny is called object.");
 			installComment(op_OclAny__lt__gt_, "True if oclText[self] is a different object from object2. Infix operator.");
 			installComment(op_OclAny__eq_, "True if oclText[self] is the same object as object2. Infix operator.");
 			installComment(op_OclAny_oclAsSet, "Returns a Set with oclText[self] as the sole content, unless oclText[self] is oclText[null] in which case returns an empty set,");
 			installComment(op_OclAny_oclAsType, "Evaluates to oclText[self], where oclText[self] is of the type identified by T.\nThe type T may be any classifier defined in the UML model;\nif the actual type of oclText[self] at evaluation time does not conform to T,\nthen the oclAsType operation evaluates to oclText[invalid].\n\nIn the case of feature redefinition, casting an object to a supertype of its actual type\ndoes not access the supertype\u2019s definition of the feature;\naccording to the semantics of redefinition, the redefined feature simply does not exist for the object.\nHowever, when casting to a supertype, any features additionally defined by the subtype are suppressed.");
 			installComment(op_OclAny_oclIsInState, "Evaluates to oclText[true] if the oclText[self] is in the state indentified by statespec.");
 			installComment(op_OclAny_oclIsInvalid, "Evaluates to oclText[true] if the oclText[self] is equal to OclInvalid.");
 			installComment(op_OclAny_oclIsKindOf, "Evaluates to oclText[true] if the type of oclText[self] conforms to t.\nThat is, oclText[self] is of type t or a subtype of t.");
 			installComment(op_OclAny_oclIsNew, "Can only be used in a postcondition.\nEvaluates to oclText[true] if the oclText[self] is created during performing the operation (for instance, it didn\u2019t exist at precondition time).");
 			installComment(op_OclAny_oclIsTypeOf, "Evaluates to oclText[true] if oclText[self] is of the type t but not a subtype of t");
 			installComment(op_OclAny_oclIsUndefined, "Evaluates to oclText[true] if the oclText[self] is equal to oclText[invalid] or equal to oclText[null].");
 			installComment(op_OclAny_oclLog, "Evaluates to the self, with the side effect of generating a log message comprising self.");
 			installComment(op_OclAny_oclLog_1, "Evaluates to the self, with the side effect of generating a log message comprising message followed by self.");
 			installComment(op_OclAny_oclType, "Evaluates to the type of which oclText[self] is an instance.");
 			installComment(op_OclAny_toString, "Returns a string representation of oclText[self].");
 			installComment(_OclComparable, "The type OclComparable defines the compareTo operation used by the sortedBy iteration. Only types that provide a derived\ncompareTo implementation may be sorted.");
 			installComment(op_OclComparable__lt_, "True if oclText[self] is less than oclText[that].");
 			installComment(op_OclComparable__lt__eq_, "True if oclText[self] is less than or equal to oclText[that].");
 			installComment(op_OclComparable__gt_, "True if oclText[self] is greater than oclText[that].");
 			installComment(op_OclComparable__gt__eq_, "True if oclText[self] is greater than or equal to oclText[that].");
 			installComment(op_OclComparable_compareTo, "Return -ve, 0, +ve according to whether self is less than, equal to , or greater than that.\n\nThe compareTo operation should be commutative.");
 			installComment(_OclElement, "The type OclElement is the implicit supertype of any user-defined type that has no explicit supertypes. Operations defined\nfor OclElement are therefore applicable to all user-defined types.");
 			installComment(op_OclElement_allInstances, "Return a set of all instances of the type and derived types of self.");
 			installComment(op_OclElement_oclContainer, "Returns the object for which self is a composed content or null if there is no such object.");
 			installComment(op_OclElement_oclContents, "Returns the composed contents of self.");
 			installComment(_OclInvalid, "The type OclInvalid is a type that conforms to all other types.\nIt has one single instance, identified as  oclText[invalid].\nAny property call applied on invalid results in oclText[invalid], except for the operations oclIsUndefined() and oclIsInvalid().\nOclInvalid is itself an instance of the metatype InvalidType.");
 			installComment(op_OclInvalid__lt__gt_, "Returns oclText[invalid].");
 			installComment(op_OclInvalid__eq_, "Returns oclText[invalid].");
 			installComment(op_OclInvalid_allInstances, "Returns oclText[invalid].");
 			installComment(op_OclInvalid_toString, "operation oclBadOperation() : OclInvalid;\nproperty oclBadProperty : OclInvalid;\n/**\nReturns \'invalid\'.");
 			installComment(_OclLambda, "The type OclLambda is the implicit supertype of all Lambda types. The operations defined for OclLambda\ntherefore apply to all lambda expressions.");
 			installComment(_OclMessage, "OclMessage\nThis sub clause contains the definition of the standard type OclMessage.\nAs defined in this sub clause, each ocl message type is actually a template type with one parameter.\n\u2018T\u2019 denotes the parameter.\nA concrete ocl message type is created by substituting an operation or signal for the T.\n\nThe predefined type OclMessage is an instance of MessageType.\nEvery OclMessage is fully determined by either the operation, or signal given as parameter.\nNote that there is conceptually an undefined (infinite) number of these types,\nas each is determined by a different operation or signal.\nThese types are unnamed. Every type has as attributes the name of the operation or signal,\nand either all formal parameters of the operation, or all attributes of the signal.\nOclMessage is itself an instance of the metatype MessageType.\n\nOclMessage has a number of predefined operations, as shown in the OCL Standard Library.");
 			installComment(op_OclMessage_hasReturned, "True if type of template parameter is an operation call, and the called operation has returned a value.\nThis implies the fact that the message has been sent. False in all other cases.");
 			installComment(op_OclMessage_isOperationCall, "Returns oclText[true] if the OclMessage represents the sending of a UML Operation call.");
 			installComment(op_OclMessage_isSignalSent, "Returns oclText[true] if the OclMessage represents the sending of a UML Signal.");
 			installComment(op_OclMessage_result, "Returns the result of the called operation, if type of template parameter is an operation call,\nand the called operation has returned a value. Otherwise the oclText[invalid] value is returned.");
 			installComment(_OclSelf, "The pseudo-type OclSelf denotes the statically determinate type of oclText[self] in Operation\nand Iteration signatures. Instances of OclSelf are never created.");
 			installComment(_OclSummable, "The type OclSummable defines the sum and zero operations used by the Collection::sum iteration. Only types that provide derived\nsum and zero implementations may be summed.");
 			installComment(op_OclSummable_sum, "Return the sum of self and that.\n\nThe sum operation should be associative.");
 			installComment(op_OclSummable_zero, "Return the \'zero\' value of self to initialize a summation.\n\nzero().sum(self) = self.");
 			installComment(_OclTuple, "The type OclTuple is the implicit supertype of all Tuple types. The operations defined for OclTuple\ntherefore apply to all tuples.");
 			installComment(_OclType, "The type OclType is the implicit supertype of any UML type. Operations defined\nfor OclType are therefore applicable to all UML types.");
 			installComment(op_OclType_conformsTo, "Returns true if type2 conforms to self.");
 			installComment(_OclVoid, "The type OclVoid is a type that conforms to all other types except OclInvalid.\nIt has one single instance, identified as oclText[null], that corresponds with the UML LiteralNull value specification.\nAny property call applied on oclText[null] results in oclText[invalid], except for the\noclIsUndefined(), oclIsInvalid(), =(OclAny) and <>(OclAny) operations.\nHowever, by virtue of the implicit conversion to a collection literal,\nan expression evaluating to oclText[null] can be used as source of collection operations (such as \u2018isEmpty\u2019).\nIf the source is the oclText[null] literal, it is implicitly converted to Bag{}.\n\nOclVoid is itself an instance of the metatype VoidType.");
 			installComment(op_OclVoid__eq_, "Redefines the OclAny operation, returning oclText[true] if object is oclText[null], oclText[invalid]\nif object is oclText[invalid], oclText[false] otherwise.");
 			installComment(op_OclVoid_allInstances, "Returns oclText[Set{null}].");
 			installComment(op_OclVoid_toString, "Returns oclText[null].");
 			installComment(op_OrderedSet_OrderedSet_T__neg_, "The elements of oclText[self], which are not in s.");
 			installComment(op_OrderedSet_OrderedSet_T_append, "The set of elements, consisting of all elements of oclText[self], followed by object.");
 			installComment(op_OrderedSet_OrderedSet_T_excluding, "The ordered set  containing all elements of oclText[self] apart from object.\n\nThe order of the remaining elements is not changed.");
 			installComment(op_OrderedSet_OrderedSet_T_including, "The ordered set containing all elements of oclText[self] plus object added as the last element if not already present.");
 			installComment(op_OrderedSet_OrderedSet_T_insertAt, "The ordered set consisting of oclText[self] with object present at position index.");
 			installComment(op_OrderedSet_OrderedSet_T_prepend, "The sequence consisting of object, followed by all elements in oclText[self].");
 			installComment(it_OrderedSet_OrderedSet_T_reject, "The ordered set of the source ordered set for which body is oclText[false].");
 			installComment(op_OrderedSet_OrderedSet_T_reverse, "The ordered set of elements with same elements but with the opposite order.");
 			installComment(it_OrderedSet_OrderedSet_T_select, "The ordered set of the source ordered set for which body is oclText[true]");
 			installComment(it_OrderedSet_OrderedSet_T_sortedBy, "Results in the ordered set containing all elements of the source collection.\nThe element for which body has the lowest value comes first, and so on.\nThe type of the body expression must have the < operation defined.\nThe < operation must return a Boolean value and must be transitive (i.e., if a < b and b < c, then a < c).");
 			installComment(op_OrderedSet_OrderedSet_T_subOrderedSet, "The sub-set of oclText[self] starting at number lower, up to and including element number upper.");
 			installComment(op_Sequence_Sequence_T__eq_, "True if oclText[self] contains the same elements as s in the same order.");
 			installComment(op_Sequence_Sequence_T_append, "The sequence of elements, consisting of all elements of oclText[self], followed by object.");
 			installComment(op_Sequence_Sequence_T_at, "The i-th element of sequence.");
 			installComment(it_Sequence_Sequence_T_closure, "The closure of applying body transitively to every distinct element of the source collection.");
 			installComment(it_Sequence_Sequence_T_collectNested, "The sequence of elements that results from applying body to every member of the source ordered collection.");
 			installComment(op_Sequence_Sequence_T_excluding, "The sequence containing all elements of oclText[self] apart from all occurrences of object.\n\nThe order of the remaining elements is not changed.");
 			installComment(op_Sequence_Sequence_T_first, "The first element in oclText[self].");
 			installComment(op_Sequence_Sequence_T_flatten, "Redefines the Collection operation. If the element type is not a collection type, this results in the same sequence as oclText[self].\nIf the element type is a collection type, the result is the sequence containing all the elements\nof all the recursively flattened elements of oclText[self]. The order of the elements is partial.");
 			installComment(op_Sequence_Sequence_T_including, "The sequence containing all elements of oclText[self] plus object added as the last element.");
 			installComment(op_Sequence_Sequence_T_indexOf, "The index of object obj in the ordered collection.");
 			installComment(op_Sequence_Sequence_T_insertAt, "The sequence consisting of oclText[self] with object inserted at position index.");
 			installComment(op_Sequence_Sequence_T_last, "The last element in oclText[self].");
 			installComment(op_Sequence_Sequence_T_prepend, "The sequence consisting of object, followed by all elements in oclText[self].");
 			installComment(it_Sequence_Sequence_T_reject, "The subsequence of the source sequence for which body is oclText[false].");
 			installComment(op_Sequence_Sequence_T_reverse, "The sequence containing the same elements but with the opposite order.");
 			installComment(it_Sequence_Sequence_T_select, "The subsequence of the source sequence for which body is oclText[true].");
 			installComment(it_Sequence_Sequence_T_sortedBy, "Results in the Sequence containing all elements of the source collection.\nThe element for which body has the lowest value comes first, and so on.\nThe type of the body expression must have the < operation defined.\nThe < operation must return a Boolean value and must be transitive (i.e., if a < b and b < c then a < c).");
 			installComment(op_Sequence_Sequence_T_subSequence, "The sub-sequence of oclText[self] starting at number lower, up to and including element number upper.");
 			installComment(op_Set_Set_T__neg_, "The elements of oclText[self], which are not in s.");
 			installComment(op_Set_Set_T__eq_, "Evaluates to oclText[true] if oclText[self] and s contain the same elements.");
 			installComment(op_Set_Set_T_excluding, "The set containing all elements of oclText[self] without object.");
 			installComment(op_Set_Set_T_flatten, "Redefines the Collection operation. If the element type is not a collection type, this results in the same set as oclText[self].\nIf the element type is a collection type, the result is the set containing all the elements of all the recursively flattened elements of oclText[self].");
 			installComment(op_Set_Set_T_including, "The set containing all elements of oclText[self] plus object.");
 			installComment(it_Set_Set_T_reject, "The subset of the source set for which body is oclText[false].");
 			installComment(it_Set_Set_T_select, "The subset of set for which expr is oclText[true].");
 			installComment(it_Set_Set_T_sortedBy, "Results in the ordered set containing all elements of the source collection.\nThe element for which body has the lowest value comes first, and so on.\nThe type of the body expression must have the < operation defined.\nThe < operation must return a Boolean value and must be transitive (i.e., if a < b and b < c, then a < c).");
 			installComment(op_UniqueCollection_UniqueCollection_T__neg_, "The elements of oclText[self], which are not in s.");
 			installComment(op_UniqueCollection_UniqueCollection_T_intersection, "The intersection of oclText[self] and c (i.e., the set of all elements that are in both oclText[self] and c).");
 			installComment(it_UniqueCollection_UniqueCollection_T_sortedBy, "Results in the ordered set containing all elements of the source collection.\nThe element for which body has the lowest value comes first, and so on.\nThe type of the body expression must have the < operation defined.\nThe < operation must return a Boolean value and must be transitive (i.e., if a < b and b < c, then a < c).");
 			installComment(op_UniqueCollection_UniqueCollection_T_symmetricDifference, "The set containing all the elements that are in oclText[self] or s, but not in both.");
 			installComment(op_UniqueCollection_UniqueCollection_T_union, "The set consisting of all elements in oclText[self] and all elements in s.");
 		}
 	}
 	
 /*	private static WeakHashMap<OCLstdlib,Object> liveOCLstdlibs = new WeakHashMap<OCLstdlib,Object>();
 	
 	public static String debugSimpleName(Object object) {
 		if (object == null) {
 			return "null";
 		}
 		else {
 			return object.getClass().getSimpleName() + "@" + Integer.toHexString(object.hashCode());
 		}
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		System.out.println("Finalize " + debugSimpleName(this));		
 		super.finalize();
 		Set<OCLstdlib> keySet = liveOCLstdlibs.keySet();
 		if (!keySet.isEmpty()) {
 			StringBuilder s = new StringBuilder();
 			s.append(" live");
 			for (OCLstdlib stdlib : keySet) {
 				s.append(" @" + Integer.toHexString(stdlib.hashCode()));		
 			}
 			System.out.println(s);		
 		}
 	} */
 
 /*	public static void decontain() {
 		Map<EObject, Object> allContents = new WeakHashMap<EObject,Object>(1000);
 		for (OCLstdlib oclstdlib : liveOCLstdlibs.keySet()) {
 			for (TreeIterator<EObject> tit = oclstdlib.getAllContents(); tit.hasNext(); ) {
 				allContents.put(tit.next(), null);
 			}
 		}
 		for (EObject eObject : allContents.keySet()) {
 			for (EReference eReference : eObject.eClass().getEAllReferences()) {
 				boolean isUnsettable = eReference.isUnsettable();
 				boolean isChangeable = eReference.isChangeable();
 				if (isChangeable) {
 //					System.out.println("unset : " + debugSimpleName(eObject) + " " + eReference.getName());
 					try {
 						eObject.eUnset(eReference);
 					}
 					catch (Exception e) {}
 				}
 			}
 		}
 		System.gc();
 		System.runFinalization();
 		for (EObject eObject : allContents.keySet()) {
 			System.out.println("   still live : " + debugSimpleName(eObject));
 		}
 	} */
 }
