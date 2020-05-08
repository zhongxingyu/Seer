 /*
  * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.qi4j.tutorials.recipe.view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.AbstractListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import org.qi4j.api.common.ConstructionException;
 import org.qi4j.api.injection.scope.State;
 import org.qi4j.api.mixin.Initializable;
 import org.qi4j.api.property.Property;
 import org.qi4j.api.unitofwork.UnitOfWork;
 import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
 import org.qi4j.tutorials.recipe.domain.Category;
 import org.qi4j.tutorials.recipe.domain.CategoryEntity;
 
 /**
  * JAVADOC
  */
 public class CategoryViewMixin
     implements FramePlugin, Initializable
 {
     @State private Property<String> name;
 
     public void initialize() throws ConstructionException
     {
         name.set( "CategoryEntity view" );
     }
 
     public Property<String> name()
     {
         return name;
     }
 
     public Component activate( UnitOfWork unitOfWork )
     {
         JPanel panel = new JPanel( new BorderLayout() );
 
         panel.add( BorderLayout.NORTH, new JLabel( "Categories" ) );
 
         // Listing
         JList list = createList( unitOfWork );
 
         panel.add( BorderLayout.CENTER, list );
 
         return panel;
     }
 
     private JList createList( final UnitOfWork unitOfWork )
     {
         Iterable<? extends Category> categories = unitOfWork.queryBuilderFactory().newQueryBuilder( CategoryEntity.class ).newQuery();
         final List<Category> categoryList = new ArrayList<Category>();
         for( Category category : categories )
         {
             categoryList.add( category );
         }
         final JList list = new JList( new AbstractListModel()
         {
             public int getSize()
             {
                 return categoryList.size();
             }
 
             public Object getElementAt( int i )
             {
                 Category cat = categoryList.get( i );
                 return cat.name();
             }
         } );
 
         list.addMouseListener( new MouseAdapter()
         {
             @Override public void mouseClicked( MouseEvent mouseEvent )
             {
                 if( mouseEvent.getClickCount() == 2 )
                 {
                     JPanel editPanel = new JPanel( new GridLayout( 0, 1 ) );
                     JPanel showPanel = new JPanel( new BorderLayout() );
                     showPanel.add( editPanel, BorderLayout.SOUTH );
 
                     UnitOfWork dialogUnitOfWork = unitOfWork.unitOfWorkFactory().nestedUnitOfWork();
                     Category category = dialogUnitOfWork.dereference( categoryList.get( list.getSelectedIndex() ) );
                     PropertyBinder binder = new PropertyBinder();
 
                     Property property = category.name();
                     addProperty( editPanel, property, binder );
 
                     int result = JOptionPane.showConfirmDialog( SwingUtilities.getWindowAncestor( list ), showPanel, "Edit", JOptionPane.OK_CANCEL_OPTION );
                     if( result == JOptionPane.OK_OPTION )
                     {
                         try
                         {
                             dialogUnitOfWork.complete();
                         }
                         catch( UnitOfWorkCompletionException e )
                         {
                             dialogUnitOfWork.discard();
                             JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor( list ), e.getMessage() );
                         }
                     }
                     else
                     {
                         dialogUnitOfWork.discard();
                     }
                 }
             }
         } );
         return list;
     }
 
     private void addProperty( JPanel editPanel, Property property, PropertyBinder binder )
     {
        String name = property.qualifiedName().name();
         name = name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
 
         JPanel linePanel = new JPanel( new BorderLayout() );
         editPanel.add( linePanel );
         linePanel.add( new JLabel( name ), BorderLayout.WEST );
 
         if( property.type().equals( String.class ) )
         {
             linePanel.add( binder.bind( new JTextField(), property ), BorderLayout.CENTER );
         }
     }
 }
