 package mkpc.fligthcontrol;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JSlider;
 
 import javax.swing.WindowConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import mkpc.log.LogSystem;
 import mkpc.webcam.WebcamView;
 
 import org.jdesktop.application.Application;
 
 
 /***
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class MKCameraControlWindow extends javax.swing.JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private JButton btn_makePhoto;
 	private JLabel lbl_webcamBackground;
 	private JSlider nickCameraSlider;
 	private JSlider rollCameraSlider;
 
 	/**
 	* Auto-generated main method to display this JFrame
 	*/
 		
 	public MKCameraControlWindow() {
 		super();
 		initGUI();
 	}
 	
 	private void initGUI() {
 		try {
 			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			getContentPane().setLayout(null);
 			
 			KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		    manager.addKeyEventDispatcher(new MKKeyDispatcher());
 			
 			{
 				btn_makePhoto = new JButton();
 				getContentPane().add(btn_makePhoto);
 				btn_makePhoto.setBounds(35, 30, 128, 28);
 				btn_makePhoto.setName("btn_makePhoto");
 				btn_makePhoto.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						btn_makePhotoActionPerformed(evt);
 					}
 				});
 			}
 			{
 				rollCameraSlider = new JSlider();
 				getContentPane().add(rollCameraSlider);
 				rollCameraSlider.setBounds(35, 309, 320, 16);
				rollCameraSlider.setName("rollCameraSlider");
 				rollCameraSlider.addChangeListener(new ChangeListener() {
 					public void stateChanged(ChangeEvent evt) {
 						rollCameraSliderStateChanged(evt);
 					}
 				});
 			}
 			{
 				nickCameraSlider = new JSlider();
 				getContentPane().add(nickCameraSlider);
 				nickCameraSlider.setBounds(12, 63, 11, 240);
 				nickCameraSlider.setName("nickCameraSlider");
 				nickCameraSlider.addChangeListener(new ChangeListener() {
 					public void stateChanged(ChangeEvent evt) {
 						nickCameraSliderStateChanged(evt);
 					}
 				});
 			}
 			{
 				lbl_webcamBackground = new JLabel();
 				getContentPane().add(lbl_webcamBackground);
 				lbl_webcamBackground.setBounds(35, 63, 320, 240);
 				lbl_webcamBackground.setName("lbl_webcamBackground");
 				lbl_webcamBackground.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 			}
 			{
 				WebcamView webcamView = new WebcamView("vfw://0");
 				getContentPane().add(webcamView);
 				webcamView.setBounds(35, 63, 320, 240);
 			}
 			pack();
 			this.setSize(442, 370);
 			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(getContentPane());
 		} catch (Exception e) {
 		    //add your error handling code here
 			e.printStackTrace();
 		}
 	}
 	
 	private void btn_makePhotoActionPerformed(ActionEvent evt) 
 	{
 		LogSystem.CLog("btn_makePhoto.actionPerformed, event="+evt);
 	}
 	
 	private void nickCameraSliderStateChanged(ChangeEvent evt) 
 	{
 		LogSystem.CLog("nickCameraSlider.stateChanged, event="+evt);
 	}
 	
 	private void rollCameraSliderStateChanged(ChangeEvent evt) 
 	{
 		LogSystem.CLog("rollCameraSlider.stateChanged, event="+evt);
 	}
 	
 	private class MKKeyDispatcher implements KeyEventDispatcher {
 	    @Override
 	    public boolean dispatchKeyEvent(KeyEvent e) {
 	        if(isActive() == true)
 	        {
 		    	if (e.getID() == KeyEvent.KEY_PRESSED) 
 		    	{
 		            System.out.println("Keynumber: "+e.getKeyCode());
 		            switch (e.getKeyCode()) 
 		            { 
 		            case 10: //Enter
 		            	LogSystem.CLog("FOTO geschossen");
 		            	break;
 		            
 					case 37: //arrow left
 						rollCameraSlider.setValue(rollCameraSlider.getValue()-1);
 						break;
 					
 					case 38: //arrow up
 						nickCameraSlider.setValue(nickCameraSlider.getValue()+1);
 						break;
 						
 					case 39: //arrow right
 						rollCameraSlider.setValue(rollCameraSlider.getValue()+1);
 						break;
 						
 					case 40: //arrow down
 						nickCameraSlider.setValue(nickCameraSlider.getValue()-1);
 						break;
 
 					default:
 						break;
 					}
 		        } 
 		    	else if (e.getID() == KeyEvent.KEY_RELEASED) 
 		    	{
 		            System.out.println("2test2");
 		        } 
 		    	else if (e.getID() == KeyEvent.KEY_TYPED) 
 		    	{
 		            System.out.println("3test3");
 		        }		
 	        }
 	        return false;
 	    }
 	}
 
 
 
 
 }
