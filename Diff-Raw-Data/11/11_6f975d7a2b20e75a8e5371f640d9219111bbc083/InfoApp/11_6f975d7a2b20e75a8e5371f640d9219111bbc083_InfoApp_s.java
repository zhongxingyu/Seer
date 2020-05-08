 package dapp;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 public class InfoApp implements ActionListener {
 	static javax.swing.JTextArea text;
 	static javax.swing.JFrame frame;
 
 	public void startT() {
 		Timer t = new Timer(100, this);
 		t.start();
 	}
 
 	public static void main( String[] args )
     {
         frame = new javax.swing.JFrame( "dapp:getspecs" );
         //javax.swing.border.TitledBorder dragBorder = new javax.swing.border.TitledBorder( "Drop 'em" );
         text = new javax.swing.JTextArea();
         frame.getContentPane().add( 
             new javax.swing.JScrollPane( text ), 
             java.awt.BorderLayout.CENTER );
         // Start the text updater Thread
         
         TextUpdate.txtAppend("Ready.");
         new FileDrop( null, text, /*dragBorder,*/ new FileDrop.Listener()
         {   
         	
         	public void filesDropped( final java.io.File[] files )
             {
         	String assembled = "";
         	int i;
         	for(  i = 0; i < files.length; i++ )
                 {   try
                     {
                 		System.out.println("step1");
                 		final String fname = files[i].getCanonicalPath();
                 		System.out.println("step2");
                 		TextUpdate.txtAppend("Loading: " + fname);
                     	MediaInfo mi = new MediaInfo();
                     	System.out.println("step3");
                     	String info = mi.grabInfo(fname);
                     	System.out.println("step3");
 						String screens = GrabScreens.screens(fname);
 						System.out.println("step4");
                     	assembled+=info+"\n"+screens;
                     }   // end try
                     catch( java.io.IOException e ) {
                     	e.printStackTrace();
                     } catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
                 }   // end for: through each dropped file
         	ModifyClipboard clipboard = new ModifyClipboard();
             clipboard.setClipboardContents(assembled);
             TextUpdate.txtAppend(i+" files uploaded (links copied to clipboard)\n");
             text.append(assembled+"\n");
             InfoApp.frame.setTitle("dapp:getspecs");
             }   // end filesDropped
         }); // end FileDrop.Listener
 
         frame.setBounds( 100, 100, 500, 400 );
         frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
         frame.setVisible(true);
     }   // end main
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		text.repaint();
 		frame.repaint();
 	}
 }
