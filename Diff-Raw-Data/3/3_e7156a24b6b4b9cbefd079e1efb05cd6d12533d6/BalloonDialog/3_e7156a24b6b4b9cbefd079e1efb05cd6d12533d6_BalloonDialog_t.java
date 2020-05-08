 package net.bioclipse.balloon.ui;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * A dialog to select number of conformers for generation with balloon
  * @author ola
  *
  */
 public class BalloonDialog extends TitleAreaDialog{
 
     private static final String DEFAULT_NUM_CONFORMERS = "15";
 
    int numConformers = Integer.parseInt( DEFAULT_NUM_CONFORMERS );
    
     private Text txtNumConf;
 
     public BalloonDialog(Shell parentShell) {
         super( parentShell );
     }
 
     @Override
     protected Control createDialogArea( Composite parent ) {
         
         setTitle( "Generate conformers using Balloon" );
         setMessage( "Generate conformers using Balloon" );
 //        setTitleImage(Activator.getImageDescriptor( "icons/balloon_wiz.jpg" ).createImage() );
 
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(2, false);
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         layout.verticalSpacing = 0;
         layout.horizontalSpacing = 0;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
         composite.setFont(parent.getFont());
         // Build the separator line
         Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                                             | SWT.SEPARATOR);
         GridData gdl=new GridData(SWT.FILL, SWT.TOP, true, false);
         gdl.horizontalSpan=2;
         titleBarSeparator.setLayoutData(gdl);
 
         Label lblNumConf=new Label(composite,SWT.NONE);
         lblNumConf.setText( "Number of conformations (target):  " );
         lblNumConf.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
         txtNumConf=new Text(composite, SWT.BORDER);
         GridData gd=new GridData(SWT.LEFT, SWT.NONE, true, false);
         gd.widthHint=100;
         txtNumConf.setLayoutData(gd);
         txtNumConf.setText( DEFAULT_NUM_CONFORMERS );
 
         txtNumConf.addKeyListener( new KeyListener(){
 
             public void keyPressed( KeyEvent e ) {
             }
 
             public void keyReleased( KeyEvent e ) {
                 updateStatus();
             }} );
 
         return composite;
     }
 
     protected void updateStatus() {
 
         setErrorMessage( null );
         getButton(IDialogConstants.OK_ID).setEnabled( true );
 
         String numstr=txtNumConf.getText();
         if (numstr==null || numstr.length()<=0){
             setErrorMessage( "Number of conformers cannot be empty." );
             getButton(IDialogConstants.OK_ID).setEnabled( false );
         }
         else{
             try{
                 numConformers=Integer.parseInt( numstr );
                 if (numConformers<=0){
                     setErrorMessage( "Number of conformers must be a positive integer." );
                     getButton(IDialogConstants.OK_ID).setEnabled( false );
                 }
             }catch (NumberFormatException e){
                 setErrorMessage( "Cannot parse number of conformers to integer" );
                 getButton(IDialogConstants.OK_ID).setEnabled( false );
             }
         }        
 
         return;
 
     }
 
     public int getNumConformers() {
         return numConformers;
     }
 
 }
