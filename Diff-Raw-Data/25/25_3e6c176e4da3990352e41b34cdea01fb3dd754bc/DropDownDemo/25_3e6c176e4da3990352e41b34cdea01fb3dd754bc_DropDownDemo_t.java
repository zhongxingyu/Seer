 package org.eclipse.rap.addon.dropdown;
 
 import org.eclipse.rap.rwt.application.AbstractEntryPoint;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 
 public class DropDownDemo extends AbstractEntryPoint {
 
   private DropDown dropdown;
   private Text text;
 
   @Override
   protected void createContents( Composite parent ) {
     getShell().setLayout( new GridLayout( 2, false ) );
    createText( parent );
    createTestControls( new Composite( parent, SWT.NONE ) );
  }

  private void createText( Composite parent ) {
     text = new Text( parent, SWT.BORDER );
     GridData gridData = new GridData( 200, 23 );
     gridData.verticalAlignment = SWT.TOP;
     text.setLayoutData( gridData );
     dropdown = new DropDown( text );
    text.addListener( SWT.Modify, new Listener() {
      public void handleEvent( Event event ) {
        dropdown.show();
      }
    } );
   }
 
  private void createTestControls( final Composite parent ) {
     parent.setLayoutData( new GridData( 400, 400 ) );
     parent.setLayout( new RowLayout() );
     createButton( parent, "show", new Listener() {
       public void handleEvent( Event event ) {
         dropdown.show();
       }
     } );
     createButton( parent, "hide", new Listener() {
       public void handleEvent( Event event ) {
         dropdown.hide();
       }
     } );
    createButton( parent, "recreate", new Listener() {
      public void handleEvent( Event event ) {
        text.dispose();
        createText( parent.getParent() );
        text.moveAbove( parent );
        dropdown.show();
        getShell().layout();
      }
    } );
   }
 
   private void createButton( Composite composite, String text, Listener listener ) {
     Button show = new Button( composite, SWT.PUSH );
     show.setText( text );
     show.addListener( SWT.Selection, listener);
   }
 }
