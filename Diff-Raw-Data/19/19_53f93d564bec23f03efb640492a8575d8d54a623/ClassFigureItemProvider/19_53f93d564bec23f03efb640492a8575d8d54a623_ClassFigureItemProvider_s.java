 /**
  * Copyright (c) 2010, Jean-Francois Brazeau. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  1. Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer.
  * 
  *  2. Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  * 
  *  3. The name of the author may not be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIEDWARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.emftools.emf2gv.graphdesc.provider;
 
 
 import java.awt.Color;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.emftools.emf2gv.graphdesc.ClassFigure;
 import org.emftools.emf2gv.graphdesc.GVFigureDescription;
 import org.emftools.emf2gv.graphdesc.GraphdescFactory;
 import org.emftools.emf2gv.graphdesc.GraphdescPackage;
 
 /**
  * This is the item provider adapter for a {@link org.emftools.emf2gv.graphdesc.ClassFigure} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class ClassFigureItemProvider
 	extends AbstractFigureItemProvider
 	implements
 		IEditingDomainItemProvider,
 		IStructuredItemContentProvider,
 		ITreeItemContentProvider,
 		IItemLabelProvider,
 		IItemPropertySource {
 	private ItemPropertyDescriptor defaultHeaderBgColor;
 	private ItemPropertyDescriptor defaultBodyBgColor;
 	private ItemPropertyDescriptor headerBgColor;
 	private ItemPropertyDescriptor bodyBgColor;
 	private ItemPropertyDescriptor headerBgColorAccessor;
 	private ItemPropertyDescriptor bodyBgColorAccessor;
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ClassFigureItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addEPackagePropertyDescriptor(object);
 			addEClassPropertyDescriptor(object);
 			addLabelEAttributePropertyDescriptor(object);
 			addDynamicAppearancePropertyDescriptor(object);
 			addHeaderBackgroundColorPropertyDescriptor(object);
 			addDefaultHeaderBackgroundColorPropertyDescriptor(object);
 			addHeaderBackgroundColorAccessorPropertyDescriptor(object);
 			addBodyBackgroundColorPropertyDescriptor(object);
 			addDefaultBodyBackgroundColorPropertyDescriptor(object);
 			addBodyBackgroundColorAccessorPropertyDescriptor(object);
 			addNestedFiguresEReferencesPropertyDescriptor(object);
 		}
 		// Dynamic color fields managemebt 
 		List<IItemPropertyDescriptor> result = new ArrayList<IItemPropertyDescriptor>();
 		result.addAll(itemPropertyDescriptors);
 		ClassFigure figure = (ClassFigure) object;
 		if (figure.isDynamicAppearance()) {
 			result.remove(headerBgColor);
 			result.remove(bodyBgColor);
 		}
 		else {
 			result.remove(defaultHeaderBgColor);
 			result.remove(headerBgColorAccessor);
 			result.remove(defaultBodyBgColor);
 			result.remove(bodyBgColorAccessor);
 		}
 		return result;
 	}
 
 	/**
 	 * This adds a property descriptor for the EClass feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addEClassPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_eClass_feature"),
 				 getString("_UI_ClassFigure_eClass_description", "_UI_ClassFigure_eClass_feature", "_UI_ClassFigure_type"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__ECLASS,
 				 true,
 				 false,
 				 true,
 				 null,
 				 getString("_UI_ModelPropertyCategory"),
 				 null) {
 				@Override
 				public Collection<?> getChoiceOfValues(Object object) {
 					List<EClass> result = new ArrayList<EClass>();
 					ClassFigure classFigure = (ClassFigure) object;
 					GVFigureDescription gvFigureDesc = classFigure.getGvFigureDescription();
 					if (gvFigureDesc != null) {
 						for (EPackage ePackage : gvFigureDesc.getEPackages()) {
 							EList<EClassifier> classifiers = ePackage.getEClassifiers();
 							for (EClassifier eClassifier : classifiers) {
 							if (eClassifier instanceof EClass
 									&& !((EClass) eClassifier).isAbstract()) {
 								if (!result.contains(eClassifier))
 									result.add((EClass) eClassifier);
 							}
 							}
 						}
 						Collections.sort(result, ENAMED_ELEMENT_COMPARATOR);
 					}
 					return result;
 				}
 			});
 	}
 
 	/**
 	 * This adds a property descriptor for the Label EAttribute feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addLabelEAttributePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_labelEAttribute_feature"),
 				 getString("_UI_ClassFigure_labelEAttribute_description", "_UI_ClassFigure_labelEAttribute_feature", "_UI_ClassFigure_type"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__LABEL_EATTRIBUTE,
 				 true,
 				 false,
 				 true,
 				 null,
 				 getString("_UI_AppearancePropertyCategory"),
 				 null) {
 				@Override
 				public Collection<?> getChoiceOfValues(Object object) {
 					ClassFigure classFigure = (ClassFigure) object;
 					List<EAttribute> result = new ArrayList<EAttribute>();
 					EClass eClass = classFigure.getEClass();
 					if (eClass != null) {
 						result.addAll(eClass.getEAllAttributes());
 						Collections.sort(result, ENAMED_ELEMENT_COMPARATOR);
 					}
 					return result;
 				}
 			});
 	}
 
 	/**
 	 * This adds a property descriptor for the Header Background Color feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addHeaderBackgroundColorPropertyDescriptor(Object object) {
 		headerBgColor = new ItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory)
 						.getRootAdapterFactory(),
 				getResourceLocator(),
 				getString("_UI_ClassFigure_headerBackgroundColor_feature"),
 				getString("_UI_ClassFigure_headerBackgroundColor_description"),
 				GraphdescPackage.Literals.CLASS_FIGURE__HEADER_BACKGROUND_COLOR,
 				true, false, false,
 				ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				getString("_UI_AppearancePropertyCategory"), null);
 		itemPropertyDescriptors.add(headerBgColor);
 	}
 	protected void addDefaultHeaderBackgroundColorPropertyDescriptor(Object object) {
 		// The default header bg color is mapped to the same attribute
 		// but a different property in a different category is more comprehensive
 		// for the user
 		defaultHeaderBgColor = new ItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory)
 						.getRootAdapterFactory(),
 				getResourceLocator(),
 				getString("_UI_ClassFigure_defaultHeaderBackgroundColor_feature"),
 				getString("_UI_ClassFigure_defaultHeaderBackgroundColor_description"),
 				GraphdescPackage.Literals.CLASS_FIGURE__HEADER_BACKGROUND_COLOR,
 				true, false, false,
 				ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				getString("_UI_AppearancePropertyCategory"), null);
 		itemPropertyDescriptors.add(defaultHeaderBgColor);
 	}
 
 	/**
 	 * This adds a property descriptor for the Body Background Color feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addBodyBackgroundColorPropertyDescriptor(Object object) {
 		bodyBgColor = new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_bodyBackgroundColor_feature"),
 				 getString("_UI_ClassFigure_bodyBackgroundColor_description"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__BODY_BACKGROUND_COLOR,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 getString("_UI_AppearancePropertyCategory"),
 				 null);
 		itemPropertyDescriptors.add(bodyBgColor);
 	}
 	protected void addDefaultBodyBackgroundColorPropertyDescriptor(Object object) {
 		// The default body color is mapped to the same attribute
 		// but a different property in a different category is more comprehensive
 		// for the user
 		defaultBodyBgColor = new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 						 getResourceLocator(),
 						 getString("_UI_ClassFigure_defaultBodyBackgroundColor_feature"),
 						 getString("_UI_ClassFigure_defaultBodyBackgroundColor_description"),
 						 GraphdescPackage.Literals.CLASS_FIGURE__BODY_BACKGROUND_COLOR,
 						 true,
 						 false,
 						 false,
 						 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 						 getString("_UI_AppearancePropertyCategory"),
 						 null);
 		itemPropertyDescriptors.add(defaultBodyBgColor);
 	}
 
 	/**
 	 * This adds a property descriptor for the Nested Figures EReferences feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addNestedFiguresEReferencesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_nestedFiguresEReferences_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_ClassFigure_nestedFiguresEReferences_feature", "_UI_ClassFigure_type"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__NESTED_FIGURES_EREFERENCES,
 				 true,
 				 false,
 				 true,
 				 null,
 				 getString("_UI_ContainerPropertyCategory"),
 				 null) {
 				@Override
 				public Collection<?> getChoiceOfValues(Object object) {
 					List<EReference> result = new ArrayList<EReference>();
 					ClassFigure classFigure = (ClassFigure) object;
 					EClass eClass = classFigure.getEClass();
 					if (eClass != null) {
 						result.addAll(eClass.getEAllContainments());
 						Collections.sort(result, ENAMED_ELEMENT_COMPARATOR);
 					}
 					return result;
 				}
 			});
 	}
 
 	/**
 	 * This adds a property descriptor for the Dynamic Appearance feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addDynamicAppearancePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_dynamicAppearance_feature"),
 				 getString("_UI_ClassFigure_dynamicAppearance_description"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__DYNAMIC_APPEARANCE,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 getString("_UI_AppearancePropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Header Background Color Accessor feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addHeaderBackgroundColorAccessorPropertyDescriptor(Object object) {
 		headerBgColorAccessor = new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_headerBackgroundColorAccessor_feature"),
 				 getString("_UI_ClassFigure_headerBackgroundColorAccessor_description"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__HEADER_BACKGROUND_COLOR_ACCESSOR,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 getString("_UI_AppearancePropertyCategory"),
 				 null) {
			// TODO Changer icone de la propriété
 			protected Collection<?> getComboBoxObjects(Object object) {
				return getAvailableColorAccessors((ClassFigure) object);
 			};
 		};
 		itemPropertyDescriptors.add(headerBgColorAccessor);
 	}
 
 	/**
 	 * Returns the available color accessors.
 	 * @param classFigure the class figure.
 	 * @return the color accessors.
 	 */
 	private List<String> getAvailableColorAccessors(ClassFigure classFigure) {
 		List<String> colorMethods = new ArrayList<String>();
 		colorMethods.add(null);
 		Class<?> clasz = classFigure.getEClass().getInstanceClass();
 		Method[] methods = clasz.getMethods();
 		for (Method method : methods) {
 			if (method.getReturnType().equals(Color.class)
 					&& Modifier.isPublic(method.getModifiers())
 					&& method.getParameterTypes().length == 0) {
 				colorMethods.add(method.getName());
 			}
 		}
 		return colorMethods;
 	}
 
 	/**
 	 * This adds a property descriptor for the Body Background Color Accessor feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addBodyBackgroundColorAccessorPropertyDescriptor(Object object) {
 		bodyBgColorAccessor = new ItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_bodyBackgroundColorAccessor_feature"),
 				 getString("_UI_ClassFigure_bodyBackgroundColorAccessor_description"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__BODY_BACKGROUND_COLOR_ACCESSOR,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 getString("_UI_AppearancePropertyCategory"),
 				 null) {
			// TODO Changer icone de la propriété
 			protected Collection<?> getComboBoxObjects(Object object) {
				return getAvailableColorAccessors((ClassFigure) object);
 			};
 			
 		};
 		itemPropertyDescriptors.add(bodyBgColorAccessor);
 	}
 
 	/**
 	 * This adds a property descriptor for the EPackage feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addEPackagePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ClassFigure_ePackage_feature"),
 				 getString("_UI_ClassFigure_ePackage_description"),
 				 GraphdescPackage.Literals.CLASS_FIGURE__EPACKAGE,
 				 false,
 				 false,
 				 false,
 				 null,
 				 getString("_UI_ModelPropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(GraphdescPackage.Literals.CLASS_FIGURE__ATTRIBUTE_FIGURES);
 			childrenFeatures.add(GraphdescPackage.Literals.CLASS_FIGURE__REFERENCE_FIGURES);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	/**
 	 * This returns ClassFigure.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/ClassFigure"));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected boolean shouldComposeCreationImage() {
 		return true;
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((ClassFigure)object).getName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_ClassFigure_type") :
 			getString("_UI_ClassFigure_type") + " " + label;
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(ClassFigure.class)) {
 			case GraphdescPackage.CLASS_FIGURE__HEADER_BACKGROUND_COLOR:
 			case GraphdescPackage.CLASS_FIGURE__BODY_BACKGROUND_COLOR:
 			case GraphdescPackage.CLASS_FIGURE__DYNAMIC_APPEARANCE:
 			case GraphdescPackage.CLASS_FIGURE__HEADER_BACKGROUND_COLOR_ACCESSOR:
 			case GraphdescPackage.CLASS_FIGURE__BODY_BACKGROUND_COLOR_ACCESSOR:
 			case GraphdescPackage.CLASS_FIGURE__CONTAINER:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 			case GraphdescPackage.CLASS_FIGURE__ATTRIBUTE_FIGURES:
 			case GraphdescPackage.CLASS_FIGURE__REFERENCE_FIGURES:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GraphdescPackage.Literals.CLASS_FIGURE__ATTRIBUTE_FIGURES,
 				 GraphdescFactory.eINSTANCE.createAttributeFigure()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GraphdescPackage.Literals.CLASS_FIGURE__REFERENCE_FIGURES,
 				 GraphdescFactory.eINSTANCE.createReferenceFigure()));
 	}
 
 }
