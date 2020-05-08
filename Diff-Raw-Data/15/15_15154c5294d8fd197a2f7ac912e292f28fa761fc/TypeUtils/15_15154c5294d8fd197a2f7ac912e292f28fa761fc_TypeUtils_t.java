 /*
  * Copyright 2010-2012 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.compiler.lang.types;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.descriptors.ClassifierDescriptor;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.TypeParameterDescriptor;
 import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
 import org.napile.compiler.lang.resolve.DescriptorUtils;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintResolutionListener;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintSystemSolution;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintSystemWithPriorities;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintType;
 import org.napile.compiler.lang.resolve.name.FqName;
 import org.napile.compiler.lang.resolve.scopes.ChainedScope;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.rt.NapileLangPackage;
import org.napile.compiler.lang.types.checker.JetTypeChecker;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.intellij.util.Processor;
 
 /**
  * @author abreslav
  */
 public class TypeUtils
 {
 	public static final JetType NO_EXPECTED_TYPE = new JetType()
 	{
 		@NotNull
 		@Override
 		public TypeConstructor getConstructor()
 		{
 			throw new UnsupportedOperationException(); // TODO
 		}
 
 		@NotNull
 		@Override
 		public List<JetType> getArguments()
 		{
 			throw new UnsupportedOperationException(); // TODO
 		}
 
 		@Override
 		public boolean isNullable()
 		{
 			throw new UnsupportedOperationException(); // TODO
 		}
 
 		@NotNull
 		@Override
 		public JetScope getMemberScope()
 		{
 			throw new UnsupportedOperationException(); // TODO
 		}
 
 		@Override
 		public List<AnnotationDescriptor> getAnnotations()
 		{
 			throw new UnsupportedOperationException(); // TODO
 		}
 
 		@Override
 		public String toString()
 		{
 			return "NO_EXPECTED_TYPE";
 		}
 	};
 
 	@NotNull
 	public static JetType makeNullable(@NotNull JetType type)
 	{
 		return makeNullableAsSpecified(type, true);
 	}
 
 	@NotNull
 	public static JetType makeNotNullable(@NotNull JetType type)
 	{
 		return makeNullableAsSpecified(type, false);
 	}
 
 	@NotNull
 	public static JetType makeNullableAsSpecified(@NotNull JetType type, boolean nullable)
 	{
 		if(type.isNullable() == nullable)
 		{
 			return type;
 		}
 		if(ErrorUtils.isErrorType(type))
 		{
 			return type;
 		}
 		return new JetTypeImpl(type.getAnnotations(), type.getConstructor(), nullable, type.getArguments(), type.getMemberScope());
 	}
 
 	public static boolean isIntersectionEmpty(@NotNull JetType typeA, @NotNull JetType typeB)
 	{
 		return intersect(JetTypeChecker.INSTANCE, Sets.newLinkedHashSet(Lists.newArrayList(typeA, typeB)), new ChainedScope(null, typeA.getMemberScope(), typeB.getMemberScope())) == null;
 	}
 
 	@Nullable
 	public static JetType intersect(@NotNull JetTypeChecker typeChecker, @NotNull Set<JetType> types, @NotNull JetScope jetScope)
 	{
 		if(types.isEmpty())
 		{
 			return getTypeOfClassOrErrorType(jetScope, NapileLangPackage.ANY, true);
 		}
 
 		if(types.size() == 1)
 		{
 			return types.iterator().next();
 		}
 
 		// Intersection of T1..Tn is an intersection of their non-null versions,
 		//   made nullable is they all were nullable
 		boolean allNullable = true;
 		boolean nothingTypePresent = false;
 		List<JetType> nullabilityStripped = Lists.newArrayList();
 		for(JetType type : types)
 		{
 			nothingTypePresent |= isEqualFqName(type, NapileLangPackage.NULL);
 			allNullable &= type.isNullable();
 			nullabilityStripped.add(makeNotNullable(type));
 		}
 
 		if(nothingTypePresent)
 		{
 			return getTypeOfClassOrErrorType(jetScope, NapileLangPackage.NULL, allNullable);
 		}
 
 		// Now we remove types that have subtypes in the list
 		List<JetType> resultingTypes = Lists.newArrayList();
 		outer:
 		for(JetType type : nullabilityStripped)
 		{
 			if(!canHaveSubtypes(typeChecker, type))
 			{
 				for(JetType other : nullabilityStripped)
 				{
 					// It makes sense to check for subtyping (other <: type), despite that
 					// type is not supposed to be open, for there're enums
 					if(!TypeUnifier.mayBeEqual(type, other, jetScope) &&
 							!typeChecker.isSubtypeOf(type, other) &&
 							!typeChecker.isSubtypeOf(other, type))
 					{
 						return null;
 					}
 				}
 				return makeNullableAsSpecified(type, allNullable);
 			}
 			else
 			{
 				for(JetType other : nullabilityStripped)
 				{
 					if(!type.equals(other) && typeChecker.isSubtypeOf(other, type))
 					{
 						continue outer;
 					}
 				}
 			}
 
 			resultingTypes.add(type);
 		}
 
 		if(resultingTypes.size() == 1)
 		{
 			return makeNullableAsSpecified(resultingTypes.get(0), allNullable);
 		}
 
 
 		List<AnnotationDescriptor> noAnnotations = Collections.<AnnotationDescriptor>emptyList();
 		TypeConstructor constructor = new IntersectionTypeConstructor(noAnnotations, resultingTypes);
 
 		JetScope[] scopes = new JetScope[resultingTypes.size()];
 		int i = 0;
 		for(JetType type : resultingTypes)
 		{
 			scopes[i] = type.getMemberScope();
 			i++;
 		}
 
 		return new JetTypeImpl(noAnnotations, constructor, allNullable, Collections.<JetType>emptyList(), new ChainedScope(null, scopes)); // TODO : check intersectibility, don't use a chanied scope
 	}
 
 	private static class TypeUnifier
 	{
 		private static class TypeParameterUsage
 		{
 			private final TypeParameterDescriptor typeParameterDescriptor;
 
 			public TypeParameterUsage(TypeParameterDescriptor typeParameterDescriptor)
 			{
 				this.typeParameterDescriptor = typeParameterDescriptor;
 			}
 		}
 
 		public static boolean mayBeEqual(@NotNull JetType type, @NotNull JetType other, @NotNull JetScope jetScope)
 		{
 			return unify(type, other, jetScope);
 		}
 
 		private static boolean unify(JetType withParameters, JetType expected, @NotNull JetScope jetScope)
 		{
 			ConstraintSystemWithPriorities constraintSystem = new ConstraintSystemWithPriorities(ConstraintResolutionListener.DO_NOTHING);
 			// T -> how T is used
 			final List<TypeParameterDescriptor> parameters = Lists.newArrayList();
 			Processor<TypeParameterUsage> processor = new Processor<TypeParameterUsage>()
 			{
 				@Override
 				public boolean process(TypeParameterUsage parameterUsage)
 				{
 					parameters.add(parameterUsage.typeParameterDescriptor);
 					return true;
 				}
 			};
 			processAllTypeParameters(withParameters, processor);
 			processAllTypeParameters(expected, processor);
 			for(TypeParameterDescriptor entry : parameters)
 			{
 				constraintSystem.registerTypeVariable(entry);
 			}
 			constraintSystem.addSubtypingConstraint(ConstraintType.VALUE_ARGUMENT.assertSubtyping(withParameters, expected));
 
 			ConstraintSystemSolution solution = constraintSystem.solve(jetScope);
 			return solution.getStatus().isSuccessful();
 		}
 
 		private static void processAllTypeParameters(JetType type, Processor<TypeParameterUsage> result)
 		{
 			ClassifierDescriptor descriptor = type.getConstructor().getDeclarationDescriptor();
 			if(descriptor instanceof TypeParameterDescriptor)
 			{
 				result.process(new TypeParameterUsage((TypeParameterDescriptor) descriptor));
 			}
 			for(JetType projection : type.getArguments())
 			{
 				processAllTypeParameters(projection, result);
 			}
 		}
 	}
 
 	public static boolean canHaveSubtypes(JetTypeChecker typeChecker, JetType type)
 	{
 		if(type.isNullable())
 		{
 			return true;
 		}
 		if(!type.getConstructor().isSealed())
 		{
 			return true;
 		}
 
 		List<TypeParameterDescriptor> parameters = type.getConstructor().getParameters();
 		List<JetType> arguments = type.getArguments();
 		for(int i = 0, parametersSize = parameters.size(); i < parametersSize; i++)
 		{
 			TypeParameterDescriptor parameterDescriptor = parameters.get(i);
 			JetType typeProjection = arguments.get(i);
 
 			JetType argument = typeProjection;
 
 		/*	switch(parameterDescriptor.getVariance())
 			{
 				case INVARIANT:
 					switch(projectionKind)
 					{
 						case INVARIANT:
 							if(lowerThanBound(typeChecker, argument, parameterDescriptor) || canHaveSubtypes(typeChecker, argument))
 							{
 								return true;
 							}
 							break;
 						case IN_VARIANCE:
 							if(lowerThanBound(typeChecker, argument, parameterDescriptor))
 							{
 								return true;
 							}
 							break;
 						case OUT_VARIANCE:
 							if(canHaveSubtypes(typeChecker, argument))
 							{
 								return true;
 							}
 							break;
 					}
 					break;
 				case IN_VARIANCE:
 					if(projectionKind != Variance.OUT_VARIANCE)
 					{
 						if(lowerThanBound(typeChecker, argument, parameterDescriptor))
 						{
 							return true;
 						}
 					}
 					else
 					{
 						if(canHaveSubtypes(typeChecker, argument))
 						{
 							return true;
 						}
 					}
 					break;
 				case OUT_VARIANCE:
 					if(projectionKind != Variance.IN_VARIANCE)
 					{
 						if(canHaveSubtypes(typeChecker, argument))
 						{
 							return true;
 						}
 					}
 					else
 					{
 						if(lowerThanBound(typeChecker, argument, parameterDescriptor))
 						{
 							return true;
 						}
 					}
 					break;
 			}  */
 		}
 		return false;
 	}
 
 	private static boolean lowerThanBound(JetTypeChecker typeChecker, JetType argument, TypeParameterDescriptor parameterDescriptor)
 	{
 		for(JetType bound : parameterDescriptor.getUpperBounds())
 		{
 			if(typeChecker.isSubtypeOf(argument, bound))
 			{
 				if(!argument.getConstructor().equals(bound.getConstructor()))
 				{
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static JetType makeNullableIfNeeded(JetType type, boolean nullable)
 	{
 		if(nullable)
 		{
 			return makeNullable(type);
 		}
 		return type;
 	}
 
 	@NotNull
 	public static JetType makeUnsubstitutedType(ClassDescriptor classDescriptor, JetScope unsubstitutedMemberScope)
 	{
 		if(ErrorUtils.isError(classDescriptor))
 		{
 			return ErrorUtils.createErrorType("Unsubstituted type for " + classDescriptor);
 		}
 		List<JetType> arguments = getDefaultTypeProjections(classDescriptor.getTypeConstructor().getParameters());
 		return new JetTypeImpl(Collections.<AnnotationDescriptor>emptyList(), classDescriptor.getTypeConstructor(), false, arguments, unsubstitutedMemberScope);
 	}
 
 	@NotNull
 	public static List<JetType> getDefaultTypeProjections(List<TypeParameterDescriptor> parameters)
 	{
 		List<JetType> result = new ArrayList<JetType>(parameters.size());
 		for(TypeParameterDescriptor parameterDescriptor : parameters)
 		{
 			result.add(parameterDescriptor.getDefaultType());
 		}
 		return result;
 	}
 
 	@NotNull
 	public static List<JetType> getDefaultTypes(List<TypeParameterDescriptor> parameters)
 	{
 		List<JetType> result = Lists.newArrayList();
 		for(TypeParameterDescriptor parameterDescriptor : parameters)
 		{
 			result.add(parameterDescriptor.getDefaultType());
 		}
 		return result;
 	}
 
 	private static void collectImmediateSupertypes(@NotNull JetType type, @NotNull Collection<JetType> result)
 	{
 		TypeSubstitutor substitutor = TypeSubstitutor.create(type);
 		for(JetType supertype : type.getConstructor().getSupertypes())
 		{
 			result.add(substitutor.substitute(supertype));
 		}
 	}
 
 	@NotNull
 	public static List<JetType> getImmediateSupertypes(@NotNull JetType type)
 	{
 		List<JetType> result = Lists.newArrayList();
 		collectImmediateSupertypes(type, result);
 		return result;
 	}
 
 	private static void collectAllSupertypes(@NotNull JetType type, @NotNull Set<JetType> result)
 	{
 		List<JetType> immediateSupertypes = getImmediateSupertypes(type);
 		result.addAll(immediateSupertypes);
 		for(JetType supertype : immediateSupertypes)
 		{
 			collectAllSupertypes(supertype, result);
 		}
 	}
 
 
 	@NotNull
 	public static Set<JetType> getAllSupertypes(@NotNull JetType type)
 	{
 		Set<JetType> result = Sets.newLinkedHashSet();
 		collectAllSupertypes(type, result);
 		return result;
 	}
 
 	public static boolean hasNullableLowerBound(@NotNull TypeParameterDescriptor typeParameterDescriptor)
 	{
 		for(JetType bound : typeParameterDescriptor.getLowerBounds())
 		{
 			if(bound.isNullable())
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean equalClasses(@NotNull JetType type1, @NotNull JetType type2)
 	{
 		DeclarationDescriptor declarationDescriptor1 = type1.getConstructor().getDeclarationDescriptor();
 		if(declarationDescriptor1 == null)
 			return false; // No class, classes are not equal
 		DeclarationDescriptor declarationDescriptor2 = type2.getConstructor().getDeclarationDescriptor();
 		if(declarationDescriptor2 == null)
 			return false; // Class of type1 is not null
 		return declarationDescriptor1.getOriginal().equals(declarationDescriptor2.getOriginal());
 	}
 
 	@Nullable
 	public static ClassDescriptor getClassDescriptor(@NotNull JetType type)
 	{
 		DeclarationDescriptor declarationDescriptor = type.getConstructor().getDeclarationDescriptor();
 		if(declarationDescriptor instanceof ClassDescriptor)
 		{
 			return (ClassDescriptor) declarationDescriptor;
 		}
 		return null;
 	}
 
 	@NotNull
 	public static JetType substituteParameters(@NotNull ClassDescriptor clazz, @NotNull List<JetType> actualTypeParameters)
 	{
 		List<TypeParameterDescriptor> clazzTypeParameters = clazz.getTypeConstructor().getParameters();
 
 		if(clazzTypeParameters.size() != actualTypeParameters.size())
 		{
 			throw new IllegalArgumentException("type parameter counts do not match: " + clazz + ", " + actualTypeParameters);
 		}
 
 		Map<TypeConstructor, JetType> substitutions = Maps.newHashMap();
 
 		for(int i = 0; i < clazzTypeParameters.size(); ++i)
 		{
 			TypeConstructor typeConstructor = clazzTypeParameters.get(i).getTypeConstructor();
 			substitutions.put(typeConstructor, actualTypeParameters.get(i));
 		}
 
 		return TypeSubstitutor.create(substitutions).substitute(clazz.getDefaultType());
 	}
 
 	private static void addAllClassDescriptors(@NotNull JetType type, @NotNull Set<ClassDescriptor> set)
 	{
 		ClassDescriptor cd = getClassDescriptor(type);
 		if(cd != null)
 		{
 			set.add(cd);
 		}
 		for(JetType projection : type.getArguments())
 		{
 			addAllClassDescriptors(projection, set);
 		}
 	}
 
 	@NotNull
 	public static List<ClassDescriptor> getAllClassDescriptors(@NotNull JetType type)
 	{
 		Set<ClassDescriptor> classDescriptors = new HashSet<ClassDescriptor>();
 		addAllClassDescriptors(type, classDescriptors);
 		return new ArrayList<ClassDescriptor>(classDescriptors);
 	}
 
 	public static boolean equalTypes(@NotNull JetType a, @NotNull JetType b)
 	{
 		return JetTypeChecker.INSTANCE.isSubtypeOf(a, b) && JetTypeChecker.INSTANCE.isSubtypeOf(b, a);
 	}
 
 	public static boolean typeConstructorUsedInType(@NotNull TypeConstructor key, @NotNull JetType value)
 	{
 		if(value.getConstructor() == key)
 			return true;
 		for(JetType projection : value.getArguments())
 		{
 			if(typeConstructorUsedInType(key, projection))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean dependsOnTypeParameters(@NotNull JetType type, @NotNull Collection<TypeParameterDescriptor> typeParameters)
 	{
 		return dependsOnTypeParameterConstructors(type, Collections2.transform(typeParameters, new Function<TypeParameterDescriptor, TypeConstructor>()
 		{
 			@Override
 			public TypeConstructor apply(@Nullable TypeParameterDescriptor typeParameterDescriptor)
 			{
 				assert typeParameterDescriptor != null;
 				return typeParameterDescriptor.getTypeConstructor();
 			}
 		}));
 	}
 
 	public static boolean dependsOnTypeParameterConstructors(@NotNull JetType type, @NotNull Collection<TypeConstructor> typeParameterConstructors)
 	{
 		if(typeParameterConstructors.contains(type.getConstructor()))
 			return true;
 		for(JetType typeProjection : type.getArguments())
 		{
 			if(dependsOnTypeParameterConstructors(typeProjection, typeParameterConstructors))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@NotNull
 	public static JetType getTypeOfClassOrErrorType(@NotNull JetScope jetScope, @NotNull FqName name, boolean nullable)
 	{
 		ClassifierDescriptor classifierDescriptor = jetScope.getClassifier(name.shortName());
		if(!(classifierDescriptor instanceof ClassDescriptor) || jetScope instanceof ErrorUtils.ErrorScope)
 			return ErrorUtils.createErrorType(name.getFqName());
 		else
		{
			jetScope = ((ClassDescriptor)classifierDescriptor).getMemberScope(Collections.<JetType>emptyList());
			if(jetScope instanceof ErrorUtils.ErrorScope)
				return ErrorUtils.createErrorType(name.getFqName());

			return new JetTypeImpl(Collections.<AnnotationDescriptor>emptyList(), classifierDescriptor.getTypeConstructor(), nullable, Collections.<JetType>emptyList(), jetScope);
		}
 	}
 
 	public static boolean isEqualFqName(@NotNull JetType jetType, @NotNull FqName name)
 	{
 		return isEqualFqName(jetType.getConstructor(), name);
 	}
 
 	public static boolean isEqualFqName(@NotNull TypeConstructor constructor, @NotNull FqName name)
 	{
 		ClassifierDescriptor classifierDescriptor = constructor.getDeclarationDescriptor();
 		if(classifierDescriptor == null)
 			return false;
 		else
 			return DescriptorUtils.getFQName(classifierDescriptor).equals(name);
 	}
 
 	public static JetScope getChainedScope(Collection<JetType> set)
 	{
 		List<JetScope> list = new ArrayList<JetScope>(set.size());
 		for(JetType bound : set)
 			list.add(bound.getMemberScope());
 		return new ChainedScope(null, list.toArray(new JetScope[list.size()]));
 	}
 }
