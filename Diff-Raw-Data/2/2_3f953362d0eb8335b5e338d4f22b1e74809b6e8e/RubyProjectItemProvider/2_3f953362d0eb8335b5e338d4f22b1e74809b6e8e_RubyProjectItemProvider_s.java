 /**
  * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * $Id$
  */
 package net.refractions.udig.project.internal.provider;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
import javax.mail.search.ReceivedDateTerm;
 
 import net.refractions.udig.project.IProject;
 import net.refractions.udig.project.IProjectElement;
 import net.refractions.udig.project.IRubyProject;
 import net.refractions.udig.project.IRubyProjectElement;
 import net.refractions.udig.project.internal.Map;
 import net.refractions.udig.project.internal.Project;
 import net.refractions.udig.project.internal.ProjectPackage;
 import net.refractions.udig.project.internal.RubyFile;
 import net.refractions.udig.project.internal.RubyProject;
 import net.refractions.udig.project.internal.impl.SynchronizedEList;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.util.ResourceLocator;
 
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 
 import com.gersis_software.integrator.rdt.RDTProjectManager;
 
 /**
  * This is the item provider adapter for a {@link net.refractions.udig.project.internal.RubyProject} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class RubyProjectItemProvider extends AbstractLazyLoadingItemProvider implements
 		IEditingDomainItemProvider, IStructuredItemContentProvider,
 		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004, Refractions Research Inc. This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details."; //$NON-NLS-1$
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RubyProjectItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addNamePropertyDescriptor(object);
 			addRubyElementsInternalPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Name feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors
         .add(createItemPropertyDescriptor(
                 ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_ProjectElement_name_feature"), //$NON-NLS-1$
                 getString(
                         "_UI_PropertyDescriptor_description", "_UI_ProjectElement_name_feature", "_UI_ProjectElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                 ProjectPackage.eINSTANCE.getProjectElement_Name(), true,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Ruby Elements Internal feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addRubyElementsInternalPropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 				.add(createItemPropertyDescriptor(
 						((ComposeableAdapterFactory) adapterFactory)
 								.getRootAdapterFactory(),
 						getResourceLocator(),
 						getString("_UI_RubyProject_rubyElementsInternal_feature"), //$NON-NLS-1$
 						getString(
 								"_UI_PropertyDescriptor_description", "_UI_RubyProject_rubyElementsInternal_feature", "_UI_RubyProject_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						ProjectPackage.eINSTANCE.getRubyProject_RubyElementsInternal(),
 						true, false, true, null, null, null));
 	}
 
 	/**
 	 * This returns RubyProject.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage(
 				"full/obj16/RubyProject")); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getText(Object object) {
 		RubyProject rubyProject = ((RubyProject) object);
 		String label = rubyProject.getName();		
         if( label == null ){
         	Resource resource = rubyProject.eResource();
         	if( resource !=null ){
 				String toString = resource.toString();
 				int lastSlash = toString.lastIndexOf(File.pathSeparator);
 				if( lastSlash==-1 )
 					lastSlash=0;
 				label = toString.substring(lastSlash);
         	}        	
         }
         //Lagutko: if this method called than AWE RubyProject is loaded
         //so it means that we must load content of this project from RDT Project structure
         //and we must load it
         RDTProjectManager.loadProject(label);
         return label == null || label.length() == 0 ? "Unable to load RubyProject" : label;
 	}
 	
 	/**
      * This handles model notifications by calling {@link #updateChildren} to update any cached
      * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * 
      * @generated
      */
     public void notifyChanged( Notification notification ) {
         updateChildren(notification);
 
         switch( notification.getFeatureID(Project.class) ) {
         case ProjectPackage.PROJECT__NAME:
             fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(),
                     false, true));
             return;
         case ProjectPackage.RUBY_PROJECT__RUBY_ELEMENTS_INTERNAL:
             fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(),
                     true, false));
             return;
         }
         super.notifyChanged(notification);
     }
 	
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ResourceLocator getResourceLocator() {
 		return ProjectEditPlugin.INSTANCE;
 	}
 	
 	/**
      * @see org.eclipse.emf.edit.provider.ITreeItemContentProvider#hasChildren(java.lang.Object)
      */
     public boolean hasChildren( Object object ) {
         return true;
     }
     
     /**
      * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate
      * feature for an {@link org.eclipse.emf.edit.command.AddCommand},
      * {@link org.eclipse.emf.edit.command.RemoveCommand} or
      * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}. <!--
      * begin-user-doc --> Changed so only layers and viewport model appears. <!-- end-user-doc -->
      * 
      * @generated NOT
      */
     public Collection getChildrenFeatures( Object object ) {
         if (childrenFeatures == null) {
             super.getChildrenFeatures(object);
             childrenFeatures.add(ProjectPackage.eINSTANCE.getRubyProject_RubyElementsInternal());
         }
         return childrenFeatures;
     }
     
     protected ChildFetcher createChildFetcher() {
         return new ChildFetcher(this){
             protected void notifyChanged() {
                 RubyProjectItemProvider.this.notifyChanged(new ENotificationImpl(
                         (InternalEObject) parent, Notification.SET,
                         ProjectPackage.RUBY_PROJECT__RUBY_ELEMENTS_INTERNAL, null, null));
             }            
         };    
     }
 }
