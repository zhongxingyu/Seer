 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.ecolib.samples.controls.AsyncDemo
  * Created on 29.04.2008
  * $Id: AsyncDemo.java,v 1.3 2012/08/16 21:58:45 lordsam Exp $
  */
 package de.jwic.ecolib.samples.controls;
 
 import java.util.Date;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.Button;
 import de.jwic.controls.InputBoxControl;
 import de.jwic.ecolib.async.AbstractAsyncProcess;
 import de.jwic.ecolib.async.AsyncInfoContainer;
 import de.jwic.ecolib.async.IProcessListener;
 import de.jwic.ecolib.async.ProcessEvent;
 import de.jwic.ecolib.async.ProcessInfo;
 import de.jwic.events.SelectionEvent;
 import de.jwic.events.SelectionListener;
 
 /**
  *
  * @author Florian Lippisch
  */
 public class AsyncDemo extends ControlContainer {
 
 	private AsyncInfoContainer infoContainer;
 	private Button btRun;
 	
 	private InputBoxControl inpValue;
 	private ProcessInfo processInfo;
 	
 	private class DemoProcess extends AbstractAsyncProcess {
 
 		/* (non-Javadoc)
 		 * @see de.jwic.ecolib.async.AbstractAsyncProcess#run()
 		 */
 		protected Object runProcess() {
 			try {
 				int max = 200;
 				monitor.setMaximum(max);
 				setStatusMessage("Open Connection...");
 				Thread.sleep(2000);
 				worked();
 				
 				for (int i = 1; i < max; i++) {
 					setStatusMessage("Reading record #" + i + " evaluating if a long text is also properly displayed in the progress bar control ");
 					Thread.sleep(100);
 					worked();
 				}
 				
 				setStatusMessage("Writing results....");
 				Thread.sleep(2000);
 				worked();
 				
 				return new Date();
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return e;
 			}
 		}
 		
 	}
 	
 	/**
 	 * @param container
 	 */
 	public AsyncDemo(IControlContainer container) {
 		super(container);
 		init();
 	}
 
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public AsyncDemo(IControlContainer container, String name) {
 		super(container, name);
 		init();
 	}
 
 	/**
 	 * 
 	 */
 	private void init() {
 		
 		btRun = new Button(this, "btRun");
 		btRun.setTitle("Run");
 		btRun.addSelectionListener(new SelectionListener() {
 			public void objectSelected(SelectionEvent event) {
 				startProcess();
 			}
 		});
 		
 		infoContainer = new AsyncInfoContainer(this, "async");
 		infoContainer.setInfoMessage("Come on, hit the run button to see some Action!");
 		
 		AsyncDemoResult result = new AsyncDemoResult(infoContainer.getFrameContainer(), "result");
 		infoContainer.setResultViewer(result);
 		
 		
 		inpValue = new InputBoxControl(this, "inpValue");
 		inpValue.setText("100");
 		
 		processInfo = new ProcessInfo(this, "processInfo");
 		
 		Button btUp = new Button(this, "btUp");
 		btUp.setTitle("+");
 		btUp.addSelectionListener(new SelectionListener() {
 			public void objectSelected(SelectionEvent event) {
 				addValue(1);
 			}
 		});
 
 		Button btDown = new Button(this, "btDown");
 		btDown.setTitle("-");
 		btDown.addSelectionListener(new SelectionListener() {
 			public void objectSelected(SelectionEvent event) {
 				addValue(-1);
 			}
 		});
 
 	}
 
 	/**
 	 * 
 	 */
 	protected void addValue(int by) {
 		
 		int value = Integer.parseInt(inpValue.getText());
 		value += by;
 		inpValue.setText(Integer.toString(value));
 		
 	}
 
 	/**
 	 * 
 	 */
 	protected void startProcess() {
 		
 		DemoProcess process = new DemoProcess();
 		infoContainer.showProcessStatus(process);
 
 		processInfo.setProgressMonitor(process.getMonitor());
 		process.addProcessListener(new IProcessListener() {
 			
 			public void processFinished(ProcessEvent e) {
  				processInfo.stopRefresh();
 			}
 		});
 		
 		
 		Thread t = new Thread(process);
 		t.start();
 		
 	}
 
 }
