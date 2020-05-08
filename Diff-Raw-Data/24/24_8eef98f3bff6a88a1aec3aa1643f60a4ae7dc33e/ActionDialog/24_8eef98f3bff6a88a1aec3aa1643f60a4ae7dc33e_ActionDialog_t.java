 package gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 
 import server.GameMethodsImpl.Phase;
 import valueobjects.Territory;
 
 public class ActionDialog extends Dialog {
     public Object result;
     private Phase phase;
     private Territory territory;
     private Image[] units = new Image[18];
 
     /**
      * create a new Dialog  and load all images needed for visualization
      * @param parent
      * @param style
      * @param phase2
      * @param territory
      */
     public ActionDialog (Shell parent, int style,Phase phase2,Territory territory) {
             super (parent, style);
             this.phase = phase2;
             this.territory = territory;
             
             //load unit pictures
             Device dev = parent.getDisplay();
 			//BLACK
 			units[0] = new Image(dev, "assets/unitsBLACK.png");
 			units[1] = new Image(dev, "assets/unitsBLACK2.png");
 			units[2] = new Image(dev, "assets/unitsBLACK3.png");
 			//BLUE
 			units[3] = new Image(dev, "assets/unitsGREEN.png");
 			units[4] = new Image(dev, "assets/unitsGREEN2.png");
 			units[5] = new Image(dev, "assets/unitsGREEN3.png");
 			//RED
 			units[6] = new Image(dev, "assets/unitsRED.png");
 			units[7] = new Image(dev, "assets/unitsRED2.png");
 			units[8] = new Image(dev, "assets/unitsRED3.png");
 			//YELLOW
 			units[9] = new Image(dev, "assets/unitsYELLOW.png");
 			units[10] = new Image(dev, "assets/unitsYELLOW2.png");
 			units[11] = new Image(dev, "assets/unitsYELLOW3.png");
 			//PINK
 			units[12] = new Image(dev, "assets/unitsPINK.png");
 			units[13] = new Image(dev, "assets/unitsPINK2.png");
 			units[14] = new Image(dev, "assets/unitsPINK3.png");
 			
 			//GREEN
 			units[15] = new Image(dev, "assets/unitsBLUE.png");
 			units[16] = new Image(dev, "assets/unitsBLUE2.png");
 			units[17] = new Image(dev, "assets/unitsBLUE3.png");
     }
     
     /**
      * opens the dialog
      * @return
      */
     public Object open() {
     		
     		if (phase.equals(Phase.TURNINCARDS)){
     			Shell parent = getParent();
     			final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
     			shell.setSize(155, 80);
     			shell.setText("Karteneintauschen");
     			center(shell);
     			
     			GridLayout gridLayout = new GridLayout();
     			gridLayout.numColumns = 2;
     			gridLayout.horizontalSpacing = 4;
     			shell.setLayout(gridLayout);
     			
     			Button ok = new Button(shell, SWT.PUSH);
     			ok.setText("Karten eintauschen");
     			ok.addMouseListener(new MouseListener() {
 					
 					@Override
 					public void mouseUp(MouseEvent e) {
 						// playselectedCARDS
 						
 					}
 					
 					@Override
 					public void mouseDown(MouseEvent e) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					@Override
 					public void mouseDoubleClick(MouseEvent e) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
     			
     			Button cancel = new Button(shell, SWT.PUSH);
     			cancel.setText("Abbrechen");
     			cancel.addMouseListener(new MouseListener() {
 					
 					@Override
 					public void mouseUp(MouseEvent e) {
 						shell.dispose();
 						
 					}
 					
 					@Override
 					public void mouseDown(MouseEvent e) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					@Override
 					public void mouseDoubleClick(MouseEvent e) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
     		}
     		
     		if(phase.equals(Phase.ATTACK2)){
             	Shell parent = getParent();
                 final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
             	shell.setSize(155,80);
                 shell.setText("Angriff");
                 center(shell);
                 
                 
                 GridLayout gridLayout = new GridLayout();
                 gridLayout.numColumns = 3;
                 gridLayout.horizontalSpacing = 4;
                 shell.setLayout(gridLayout);
                 
                 int playercolor = territory.getOwner().getColor();
                 
                 int maxUnits = territory.getUnits()-1;
                 
                 if(playercolor == 1){
                 	playercolor = 3;
                 }
                 else if(playercolor == 2){
                 	playercolor = 6;
                 }
                 else if(playercolor == 3){
                 	playercolor = 9;
                 }
                 else if(playercolor == 4){
                 	playercolor = 12;
                 }
                 else if(playercolor == 5){
                 	playercolor = 15;
                 }
                 
                 Button button = new Button(shell, SWT.PUSH);
         		button.setImage(units[playercolor]);
         		button.addMouseListener(new MouseListener() {
     				@Override
     				public void mouseDown(MouseEvent e) {
     					result = 1;
     					shell.close();
     				}
 
     				@Override
     				public void mouseDoubleClick(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
 
     				@Override
     				public void mouseUp(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
     		      });
         		
         		Button button2 = new Button(shell, SWT.PUSH);
         		button2.setImage(units[playercolor+1]);
         		button2.addMouseListener(new MouseListener() {
     				@Override
     				public void mouseDown(MouseEvent e) {
     					result = 2;
     					shell.close();
     				}
 
     				@Override
     				public void mouseDoubleClick(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
 
     				@Override
     				public void mouseUp(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
     		      });
         		
         		Button button3 = new Button(shell, SWT.PUSH);
         		button3.setImage(units[playercolor+2]);        		
         		button3.addMouseListener(new MouseListener() {
     				@Override
     				public void mouseDown(MouseEvent e) {
     					result = 3;
     					shell.close();
     				}
 
     				@Override
     				public void mouseDoubleClick(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
 
     				@Override
     				public void mouseUp(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
     		      });
                 
         		if(maxUnits == 1) {
         			button2.setEnabled(false);
         			button3.setEnabled(false);
         		}
         		
         		if(maxUnits == 2) {
         			button3.setEnabled(false);
         		}
         		
                 shell.open();
                 Display display = parent.getDisplay();
                 while (!shell.isDisposed()) {
                         if (!display.readAndDispatch()) display.sleep();
                 }
                 return result;
             }
     		
     		if(phase.equals(Phase.ATTACK3)){
             	Shell parent = getParent();
                 final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            	shell.setSize(85,80);
                 shell.setText("Verteidigung");
                 center(shell);
                 
                 
                 GridLayout gridLayout = new GridLayout();
                 gridLayout.numColumns = 2;
                 gridLayout.horizontalSpacing = 4;
                 shell.setLayout(gridLayout);
                 
                 int playercolor = territory.getOwner().getColor();
                 
                 int maxUnits = territory.getUnits();
                 
                 if(playercolor == 1){
                 	playercolor = 3;
                 }
                 else if(playercolor == 2){
                 	playercolor = 6;
                 }
                 else if(playercolor == 3){
                 	playercolor = 9;
                 }
                 else if(playercolor == 4){
                 	playercolor = 12;
                 }
                 else if(playercolor == 5){
                 	playercolor = 15;
                 }
                 
                 Button button = new Button(shell, SWT.PUSH);
         		button.setImage(units[playercolor]);
         		button.addMouseListener(new MouseListener() {
     				@Override
     				public void mouseDown(MouseEvent e) {
     					result = 1;
     					shell.close();
     				}
 
     				@Override
     				public void mouseDoubleClick(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
 
     				@Override
     				public void mouseUp(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
     		      });
         		
         		Button button2 = new Button(shell, SWT.PUSH);
         		button2.setImage(units[playercolor+1]);
         		button2.addMouseListener(new MouseListener() {
     				@Override
     				public void mouseDown(MouseEvent e) {
     					result = 2;
     					shell.close();
     				}
 
     				@Override
     				public void mouseDoubleClick(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
 
     				@Override
     				public void mouseUp(MouseEvent e) {
     					// TODO Auto-generated method stub
     					
     				}
     		      });
                 
         		if(maxUnits == 1) {
         			button2.setEnabled(false);
         		}
         		
                 shell.open();
                 Display display = parent.getDisplay();
                 while (!shell.isDisposed()) {
                         if (!display.readAndDispatch()) display.sleep();
                 }
                 return result;
             }
     		
     		if (phase.equals(Phase.MOVEMENT)){
     			Shell parent = getParent();
                 final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
             	shell.setSize(155,80);
                 shell.setText("Verschieben");
                 center(shell);
                 
                 GridLayout gridLayout = new GridLayout();
                 gridLayout.numColumns = 1;
                 gridLayout.horizontalSpacing = 4;
                 shell.setLayout(gridLayout);
                 
                 final Spinner spinner = new Spinner(shell, SWT.NONE);
                 spinner.setMinimum(0);
                 spinner.setMaximum(territory.getUnits()-1);
                 
                 Button ok = new Button(shell,SWT.PUSH);
                 ok.setText("Bestätigen");
                 ok.addMouseListener(new MouseListener() {
 					
 					@Override
 					public void mouseUp(MouseEvent e) {
 						result = spinner.getDigits();
 						
 					}
 					
 					@Override
 					public void mouseDown(MouseEvent e) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					@Override
 					public void mouseDoubleClick(MouseEvent e) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
     		}
     		
     		
 			return result;
             
     }
     
     /**
      * center the dialog shell in the middle of the Screen
      * @param shell
      */
     private void center(Shell shell) {
 
 		Rectangle bds = shell.getDisplay().getBounds();
 
 		Point p = shell.getSize();
 
 		int nLeft = (bds.width - p.x) / 2;
 		int nTop = (bds.height - p.y) / 2;
 
 		shell.setBounds(nLeft, nTop, p.x, p.y);
 	}
     
 }
