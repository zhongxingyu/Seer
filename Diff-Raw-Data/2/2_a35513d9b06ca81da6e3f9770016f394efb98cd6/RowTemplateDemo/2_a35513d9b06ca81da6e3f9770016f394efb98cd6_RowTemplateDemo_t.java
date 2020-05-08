 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    EclipseSource - initial API and implementation
  ******************************************************************************/
 package com.eclipsesource.rowtemplate.demo;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.rap.rwt.application.AbstractEntryPoint;
 import org.eclipse.rap.rwt.internal.template.Cell;
 import org.eclipse.rap.rwt.internal.template.Cells;
 import org.eclipse.rap.rwt.internal.template.RowTemplate;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 
 
 @SuppressWarnings("restriction")
 public class RowTemplateDemo extends AbstractEntryPoint {
 
   @Override
   protected void createContents( Composite parent ) {
     TableViewer tableViewer = new TableViewer( parent );
     GridDataFactory.fillDefaults().align( SWT.CENTER, SWT.CENTER ).grab( true, true ).applyTo( tableViewer.getTable() );
     tableViewer.setContentProvider( new ArrayContentProvider() );
     addFirstNameColumn( tableViewer );
     addLastNameColumn( tableViewer );
     addFooColumn( tableViewer );
     tableViewer.setInput( Persons.get() );
     RowTemplate rowTemplate = createRowTemplate( parent, tableViewer );
     tableViewer.getTable().setData( RowTemplate.ROW_TEMPLATE, rowTemplate );
   }
 
   private void addFirstNameColumn( final TableViewer tableViewer ) {
     TableViewerColumn firstNameColumn = new TableViewerColumn( tableViewer, SWT.NONE );
     firstNameColumn.getColumn().setWidth( 200 );
     firstNameColumn.getColumn().setText( "Firstname" );
     firstNameColumn.setLabelProvider( new ColumnLabelProvider() {
 
       @Override
       public String getText( Object element ) {
         Person p = ( Person )element;
         return p.getFirstName();
       }
 
       @Override
       public Image getImage(Object element) {
         Person p = ( Person )element;
         String name = p.getFirstName().toLowerCase();
         return new Image( tableViewer.getTable().getDisplay(), RowTemplateDemo.class.getResourceAsStream( "/" + name + ".png" ) );
       }
     } );
   }
 
   private RowTemplate createRowTemplate( Composite parent, TableViewer tableViewer ) {
     RowTemplate rowTemplate = new RowTemplate();
 
     Cell imageCell = Cells.createImageCell( rowTemplate, SWT.LEFT | SWT.TOP );
     imageCell.setBindingIndex( 0 );
     imageCell.setTop( 4 );
     imageCell.setLeft( 8 );
     imageCell.setWidth( 32 );
     imageCell.setHeight( 32 );
 
     imageCell.setName( "image" );
     imageCell.setSelectable( true );
 
     tableViewer.getTable().addSelectionListener( new SelectionAdapter() {
       @Override
       public void widgetSelected( SelectionEvent e) {
       if( "image".equals( e.text) ) {
          System.out.println( "Image Cell was clicked" );
        }
       }
     } );
 
 
     Cell firstNameCell = Cells.createTextCell( rowTemplate, SWT.LEFT );
     firstNameCell.setBindingIndex( 0 );
     firstNameCell.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_DARK_RED ) );
     firstNameCell.setLeft( 48 );
     firstNameCell.setTop( 4 );
     firstNameCell.setRight( 8 );
     firstNameCell.setHeight( 24 );
 
     Cell lastNameCell = Cells.createTextCell( rowTemplate, SWT.LEFT );
     lastNameCell.setBindingIndex( 1 );
     lastNameCell.setLeft( 48 );
     lastNameCell.setTop( 40 );
     lastNameCell.setRight( 8 );
     lastNameCell.setBottom( 4 );
     return rowTemplate;
   }
 
   private void addLastNameColumn( TableViewer tableViewer ) {
     TableViewerColumn lastNameColumn = new TableViewerColumn( tableViewer, SWT.NONE );
     lastNameColumn.getColumn().setWidth( 200 );
     lastNameColumn.getColumn().setText( "Firstname" );
     lastNameColumn.setLabelProvider( new ColumnLabelProvider() {
 
       @Override
       public String getText( Object element ) {
         Person p = ( Person )element;
         return p.getLastName();
       }
 
     } );
   }
 
   private void addFooColumn( TableViewer tableViewer ) {
     TableViewerColumn fooColumn = new TableViewerColumn( tableViewer, SWT.NONE );
     fooColumn.getColumn().setWidth( 200 );
     fooColumn.getColumn().setText( "Foo" );
     fooColumn.setLabelProvider( new ColumnLabelProvider() {
 
       @Override
       public String getText( Object element ) {
         return "foo";
       }
 
     } );
   }
 
 }
