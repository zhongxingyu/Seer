 package org.eclipse.iee.translator.math.pad;
 
 import java.io.Serializable;
 
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.translator.jmole.math.generator.Mole;
 import org.eclipse.iee.translator.math.Activator;
 import org.eclipse.iee.translator.math.FileStorage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 
 public class CompiledMathPad extends Pad implements Serializable {
 
 	private transient static FileStorage fFileStorage;
 
 	private String fText;
 
 	public CompiledMathPad() {
 		super();
 		fText = "";
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
 		layout.marginHeight = 5;
 		layout.marginWidth = 5;
 		parent.setLayout(layout);
 		final StyledText styledText = new StyledText(parent, SWT.NONE);
 		styledText.setText(fText);
 				
 		styledText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				//System.out.println(styledText.getText());
 								
 				System.out.println("before translation");
 				try {
 					String result = Activator.getMole().translateMath(styledText.getText());
 					System.out.println(result);
					getContainer().writeAtContainerRegionTail(result);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 
 			}
 			
 		});	
 		MouseEventManager mouseManager = new MouseEventManager(parent);
 		parent.addMouseTrackListener(mouseManager);
 		parent.addMouseMoveListener(mouseManager);
 		parent.addMouseListener(mouseManager);
 		
 	}
 
 	protected CompiledMathPad(String containerID) {
 		super(containerID);
 	}
 
 	public static void setStorage(FileStorage fStorage) {
 		CompiledMathPad.fFileStorage = fStorage;
 	}
 
 	@Override
 	public Pad copy() {
 		CompiledMathPad newPad = new CompiledMathPad();
 		newPad.fText = this.fText;
 		return newPad;
 	}
 
 	@Override
 	public String getType() {
 		return "CompiledMath";
 	}
 
 	// Save&Load operations, use it for serialization
 
 	public void save() {
 		CompiledMathPad.fFileStorage.saveToFile(this);
 	}
 
 	@Override
 	public void unsave() {
 		CompiledMathPad.fFileStorage.removeFile(getContainerID());
 	}
 }
