 /*******************************************************************************
  * Copyright (c) 2009, 2012 EclipseSource and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    EclipseSource - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rap.demo.enrondata.internal;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ILazyTreeContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.rap.demo.enrondata.internal.model.Mail;
 import org.eclipse.rap.demo.enrondata.internal.model.MailDir;
 import org.eclipse.rap.demo.enrondata.internal.model.MailDirIndex;
 import org.eclipse.rap.demo.enrondata.internal.model.MailFile;
 import org.eclipse.rap.demo.enrondata.internal.model.MailNode;
 import org.eclipse.rap.examples.ExampleUtil;
 import org.eclipse.rap.examples.IExamplePage;
import org.eclipse.rwt.RWT;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 
 public class EnronExamplePage implements IExamplePage {
 
   private static final String DEFAULT_DATASET_DIR = "/data/enron/maildir";
   private static final String DATASET_DIR_PROP = "org.eclipse.rap.demo.enronDatasetDirectory";
 
   private TreeViewer viewer;
   private Text messageText;
   private Text senderText;
   private Text subjectText;
 
   public void createControl( Composite parent ) {
     parent.setLayout( ExampleUtil.createMainLayout( 1 ) );
     createInfoArea( parent );
     createMainArea( parent );
   }
 
   private void createInfoArea( Composite parent ) {
     Composite composite = new Composite( parent, SWT.NONE );
     ExampleUtil.createHeading( composite, "Enron Dataset (520.929 items)", 1 );
     composite.setLayout( ExampleUtil.createGridLayout( 1, false, true, true ) );
     composite.setLayoutData( ExampleUtil.createHorzFillData() );
     Label label = new Label( composite, SWT.WRAP );
     label.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
     label.setText( "This example demonstates how a <em>large dataset</em> can be displayed"
                    + " using a virtual JFace tree viewer with a lazy content provider."
                    + " The example uses the"
                    + " <a href=\"http://en.wikipedia.org/wiki/Enron_scandal\">Enron</a>"
                    + " dataset, that contains half a million emails from about 150 users."
                    + " The dataset is available in the"
                    + " <a href=\"http://www.cs.cmu.edu/~enron/\">public domain</a>.<br/>"
                    + " The source code of this example page currently resides on"
                    + " <a href=\"https://github.com/ralfstx/rap-demo-additions\">github</a>." );
     label.setLayoutData( ExampleUtil.createHorzFillData() );
   }
 
   private void createMainArea( Composite parent ) {
     Composite composite = new Composite( parent, SWT.NONE );
     composite.setLayout( ExampleUtil.createGridLayout( 1, false, true, true ) );
     composite.setLayoutData( ExampleUtil.createFillData() );
     SashForm sashForm = new SashForm( composite, SWT.HORIZONTAL );
     createTreeArea( sashForm );
     createContentArea( sashForm );
     sashForm.setWeights( new int[] { 35, 65 } );
     sashForm.setLayoutData( ExampleUtil.createFillData() );
   }
 
   private void createTreeArea( Composite parent ) {
     Composite composite = new Composite( parent, SWT.NONE );
     composite.setLayout( ExampleUtil.createGridLayoutWithoutMargin( 1, false ) );
     viewer = new TreeViewer( composite, SWT.SINGLE | SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION );
     viewer.getControl().setLayoutData( ExampleUtil.createFillData() );
     viewer.setLabelProvider( new EnronLabelProvider( parent.getDisplay() ) );
     viewer.setContentProvider( new EnronLazyContentProvider( viewer ) );
     viewer.setInput( getDataSet() );
     viewer.addSelectionChangedListener( new ISelectionChangedListener() {
 
       public void selectionChanged( SelectionChangedEvent event ) {
         IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
         Object firstElement = selection.getFirstElement();
         if( firstElement instanceof MailNode ) {
           nodeSelected( ( MailNode )firstElement );
         }
       }
     } );
   }
 
   private void createContentArea( Composite parent ) {
     Composite composite = new Composite( parent, SWT.BORDER );
     GridLayout layout = ExampleUtil.createGridLayoutWithoutMargin( 1, false );
     layout.verticalSpacing = 0;
     composite.setLayout( layout );
     createMailHeaderArea( composite );
     createMailContentArea( composite );
   }
 
   private void createMailHeaderArea( Composite parent ) {
     Composite header = new Composite( parent, SWT.NONE );
     header.setBackground( new Color( header.getDisplay(), 0xe5, 0xe5, 0xe5 ) );
     header.setBackgroundMode( SWT.INHERIT_FORCE );
     header.setLayoutData( ExampleUtil.createHorzFillData() );
     GridLayout headerLayout = new GridLayout( 2, false );
     headerLayout.marginLeft = 5;
     headerLayout.horizontalSpacing = 5;
     headerLayout.verticalSpacing = 5;
     header.setLayout( headerLayout );
     Label senderLabel = new Label( header, SWT.NONE );
     senderLabel.setText( "From:" );
     senderLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
     senderText = new Text( header, SWT.SINGLE | SWT.READ_ONLY );
     GridData senderTextData = ExampleUtil.createHorzFillData();
     senderTextData.horizontalIndent = 2;
     senderText.setLayoutData( senderTextData );
     Label subjectLabel = new Label( header, SWT.NONE );
     subjectLabel.setText( "Subject:" );
     subjectLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
     subjectText = new Text( header, SWT.SINGLE | SWT.READ_ONLY );
     subjectText.setLayoutData( senderTextData );
   }
 
   private void createMailContentArea( Composite parent ) {
     messageText = new Text( parent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY );
     GridData messageTextData = ExampleUtil.createFillData();
     messageTextData.horizontalIndent = 5;
     messageText.setLayoutData( messageTextData );
   }
 
   private void nodeSelected( MailNode selectedNode ) {
     if( selectedNode != null ) {
       if( selectedNode instanceof MailFile ) {
         MailFile selectedFile = ( MailFile )selectedNode;
         try {
           Mail mail = new Mail( selectedFile.getContent() );
           senderText.setText( mail.getSender() );
           subjectText.setText( mail.getSubject() );
           messageText.setText( mail.getContent() );
         } catch( IOException exception ) {
           throw new RuntimeException( "Failed to read contents from node", exception );
         }
       }
     }
   }
 
   private static MailNode getDataSet() {
     File root = getRootDirectory();
     MailDir mailDir = new MailDir( root );
     MailDirIndex index = new MailDirIndex( mailDir );
     try {
       index.create();
     } catch( IOException exception ) {
       throw new IllegalStateException( "Could not access data model", exception );
     }
     return mailDir;
   }
 
   private static File getRootDirectory() {
     String path = getRootDirectoryPath();
     File root = new File( path );
     if( !root.isDirectory() ) {
       throw new RuntimeException( "Enron dataset directory missing: " + root );
     }
     return root;
   }
 
   private static String getRootDirectoryPath() {
     String path = System.getProperty( DATASET_DIR_PROP );
     if( path == null ) {
       path = DEFAULT_DATASET_DIR;
     }
     return path;
   }
 
   private static final class EnronLabelProvider extends CellLabelProvider {
 
     private static final String ICON_FILE = "resources/file.png";
     private static final String ICON_FOLDER = "resources/folder.png";
 
     private static final int COLUMN_NAME = 0;
     private static final int COLUMN_OFFSET = 2;
     private static final int COLUMN_TIMEZONE = 1;
 
     private final Image fileImage;
     private final Image folderImage;
 
     EnronLabelProvider( final Device device ) {
       fileImage = createImage( device, ICON_FILE );
       folderImage = createImage( device, ICON_FOLDER );
     }
 
     @Override
     public void update( final ViewerCell cell ) {
       Object element = cell.getElement();
       if( element instanceof MailNode ) {
         MailNode node = ( MailNode )element;
         int columnIndex = cell.getColumnIndex();
         switch( columnIndex ) {
           case COLUMN_NAME:
             updateName( cell, node );
             break;
           case COLUMN_TIMEZONE:
             updateName( cell, node );
             break;
           case COLUMN_OFFSET:
             updateName( cell, node );
             break;
         }
       }
     }
 
     @Override
     public String getToolTipText( final Object element ) {
       String result = "";
       if( element instanceof File ) {
         File file = ( File )element;
         result = file.getName();
       }
       return result;
     }
 
     private void updateName( ViewerCell cell, MailNode node ) {
       if( node instanceof MailDir ) {
         cell.setText( node.getName() );
         cell.setImage( folderImage );
       } else {
         cell.setText( getSubject( ( MailFile )node ) );
         cell.setImage( fileImage );
       }
     }
 
     private static String getSubject( MailFile mailFile ) {
       String subject;
       try {
         subject = mailFile.getSubject();
       } catch( IOException exception ) {
         throw new RuntimeException( exception );
       }
       return subject.length() != 0 ? subject : "No Subject";
     }
 
     private static Image createImage( Device device, String name ) {
       ClassLoader classLoader = EnronLabelProvider.class.getClassLoader();
       InputStream inputStream = classLoader.getResourceAsStream( name );
       Image result = null;
       if( inputStream != null ) {
         try {
           result = new Image( device, inputStream );
         } finally {
           try {
             inputStream.close();
           } catch( IOException e ) {
             // ignore
           }
         }
       }
       return result;
     }
   }
 
   private static class EnronLazyContentProvider implements ILazyTreeContentProvider {
 
     private final TreeViewer viewer;
 
     public EnronLazyContentProvider( TreeViewer viewer ) {
       this.viewer = viewer;
     }
 
     public Object getParent( Object element ) {
       Object result = null;
       if( element instanceof MailNode ) {
         result = ( ( MailNode )element ).getParent();
       }
       return result;
     }
 
     public void updateElement( Object parent, int index ) {
       if( parent instanceof MailDir ) {
         MailDir folder = ( MailDir )parent;
         MailNode node = folder.getChild( index );
         if( node != null ) {
           viewer.replace( parent, index, node );
           viewer.setChildCount( node, node.getChildCount() );
         }
       }
     }
 
     public void updateChildCount( Object element, int currentChildCount ) {
       if( element instanceof MailNode ) {
         MailNode node = ( MailNode )element;
         int childCount = node.getChildCount();
         if( childCount != currentChildCount ) {
             viewer.setChildCount( element, childCount );
         }
       }
     }
 
     public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
       // nothing
     }
 
     public void dispose() {
       // nothing
     }
   }
 }
