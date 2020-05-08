 /*
  * de.jwic.samples.controls.AsyncRenderContainerDemo 
  */
 package de.jwic.demo.framework;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.Dimension;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.AsyncRenderContainer;
 import de.jwic.controls.InputBox;
 import de.jwic.controls.LabelControl;
 import de.jwic.controls.LazyInitializationHandler;
 import de.jwic.controls.combo.DropDown;
 import de.jwic.controls.layout.TableLayoutContainer;
 import de.jwic.samples.controls.propeditor.PropertyEditorView;
 
 /**
  * @author lippisch
  */
 public class AsyncRenderContainerDemo extends ControlContainer {
 
 	private AsyncRenderContainer arContainer1;
 	private AsyncRenderContainer arContainer2;
 	
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public AsyncRenderContainerDemo(IControlContainer container) {
 		super(container);
 
 		// Adding a control that takes a long time to render
 		arContainer1 = new AsyncRenderContainer(this, "arContainer1");
 		new SlowRenderingControl(arContainer1, null);
 		arContainer1.setWaitBlockDimension(new Dimension(600, 150));
 		arContainer1.setWaitText("Searching for the answer...");
 		
 		final PropertyEditorView propEditor = new PropertyEditorView(this, "propEditor");
 		propEditor.setBean(arContainer1);
 		
 
 		// A complex control, without delay.
 		arContainer2 = new AsyncRenderContainer(this, "arContainer2");
 
 		TableLayoutContainer tlc = new TableLayoutContainer(arContainer2);
 		tlc.setColumnCount(2);
 		tlc.setWidth("400");
 		new LabelControl(tlc).setText("Your Name");
 		
 		InputBox inp = new InputBox(tlc);
 		inp.setWidth(200);
 		inp.setEmptyInfoText("Enter Name Here");
 		
 		new LabelControl(tlc).setText("Your Favorit Color");
 		
 		DropDown dd = new DropDown(tlc, "dd");
 		dd.setWidth(200);
 		dd.addElement("Green");
 		dd.addElement("Blue");
 		dd.addElement("Red");
 		dd.addElement("Gray");
 		dd.addElement("Black");
 		
 		// delayed creation.
 		AsyncRenderContainer arContainer3 = new AsyncRenderContainer(this, "arContainer3");
 		arContainer3.setLazyInitializationHandler(new LazyInitializationHandler() {
			LabelControl control;
 			@Override
 			public void initialize(IControlContainer container) {
 				
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
				control = new LabelControl(container);
				control.setText("The creation of this control has taken 2 seconds. But once created, it is just there...");
			}

			@Override
			public void success() {
				control.setText(control.getText()+" Also we have both success and fail method for handling when the create is ok or not. in this case it is");
			}

			@Override
			public void fail(Throwable t) {
 				
 			}
 		});
 
 	}
 
 }
