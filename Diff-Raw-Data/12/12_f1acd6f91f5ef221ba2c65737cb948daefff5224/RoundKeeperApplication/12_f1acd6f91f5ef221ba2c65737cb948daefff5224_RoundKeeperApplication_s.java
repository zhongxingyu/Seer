 import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class RoundKeeperApplication {
  
 	private Shell shell;
 	
 	public RoundKeeperApplication(Display display){
 		shell = new Shell(display);
 		shell.setText("Round Keeper");
 		shell.setSize(250,200);
 		
 		shell.setToolTipText("This is a tooltip");
 		center(shell);
 		
 		initUI();
 		
 		shell.pack();
 		shell.open();
 
         while (!shell.isDisposed()) {
           if (!display.readAndDispatch()) {
             display.sleep();
           }
         }
 	}
 	
 	/**
 	 * Centers the shell within its own display.
 	 * @param shell
 	 */
 	private void center(Shell shell){
 		Rectangle bds = shell.getMonitor().getBounds();
 		Point p = shell.getSize();
 		int nLeft = (bds.width - p.x) / 2;
 		System.out.println("bds.width: " + bds.width);
 		System.out.println("p.x: " + p.x);
 		System.out.println("nLeft: " + nLeft);
         int nTop = (bds.height - p.y) / 2;
         System.out.println("bds.height: " + bds.height);
         System.out.println("p.y: " + p.y);
         System.out.println("nTop: " + nTop);
         
         Rectangle r= new Rectangle(nLeft, nTop, p.x, p.y);
         
         shell.setBounds(r);
 	}
 	
 	private void initUI(){
 		GridLayout gl = new GridLayout(4, true);
         gl.horizontalSpacing = 4;
         gl.verticalSpacing = 4;
         gl.marginBottom = 5;
         gl.marginTop = 5;
         shell.setLayout(gl);
 
         String[] buttons = {
             "Cls", "Bck", "", "Close", "7", "8", "9", "/", "4",
             "5", "6", "*", "1", "2", "3", "-", "0", ".", "=", "+"
         };
 
         Text display = new Text(shell, SWT.SINGLE);
         GridData gridData = new GridData();
         gridData.horizontalSpan = 4;
         gridData.horizontalAlignment = GridData.FILL;
         display.setLayoutData(gridData);
 
         for (int i = 0; i < buttons.length; i++) {
 
             if (i == 2) {
                 Label lbl = new Label(shell, SWT.CENTER);
                 GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
                 lbl.setLayoutData(gd);
             } else {
                Button btn = new Button(shell, SWT.PUSH);
                btn.setText(buttons[i]);
                GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
                gd.widthHint = 50;
                gd.heightHint = 30;
                btn.setLayoutData(gd);
             }
         }
         
 	}
 	
 	public static void main(String[] args) {
 		Display display = new Display();
         new RoundKeeperApplication(display);
         display.dispose();
 	}
   }
