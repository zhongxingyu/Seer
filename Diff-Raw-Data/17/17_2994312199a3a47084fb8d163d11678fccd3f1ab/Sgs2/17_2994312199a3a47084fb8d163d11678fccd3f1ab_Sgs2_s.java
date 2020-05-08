 /**********************************************************************************
  *
  * Copyright (c) 2009 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sgs2.client;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Sgs2 implements EntryPoint {
 
 	private enum Panel { OUTPUT(0) , RESULT(1), STACK_TRACE(3), HISTORY(4);
 		
 		public int position;
 		
 		Panel(int position) {
 			
 			this.position = position;
 		}
 	}
 	
 	// GWT RPC proxy
 	private GroovyShellServiceAsync groovyShellService = GWT.create(GroovyShellService.class);
 
 	public void onModuleLoad() {

 		// This is a reference for the declared servlet in Sgs2.gwt.xml
 		((ServiceDefTarget) groovyShellService).setServiceEntryPoint(GWT.getModuleBaseURL() + "rpc/sgs2");
 		
 		// Layout
 		VerticalPanel verticalPanel = new VerticalPanel();
 		
 		// Widgets
 		final TextArea textArea = new TextArea();
 		
 		final Button submitButton = new Button("Submit");
 		
 		final TabPanel tabPanel = new TabPanel();
 		
 		final FlowPanel outputFlowPanel = new FlowPanel();
 		final FlowPanel resultFlowPanel = new FlowPanel();
 		final FlowPanel stackTraceFlowPanel = new FlowPanel();
 		final FlowPanel historyFlowPanel = new FlowPanel();
 		
 		tabPanel.add(outputFlowPanel, "Output");
 		tabPanel.add(resultFlowPanel, "Result");
 		tabPanel.add(stackTraceFlowPanel, "Stack Trace");
 		tabPanel.add(historyFlowPanel, "History");
 		
 		tabPanel.selectTab(Panel.OUTPUT.position);
 
 		// Handlers
 		ClickHandler clickHandler = new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 
 				submitButton.setEnabled(false);
 				String sourceCode = textArea.getText();
 				
 				// Reset panels except the history one
 				outputFlowPanel.clear();
 				resultFlowPanel.clear();
 				stackTraceFlowPanel.clear();
 
 				groovyShellService.submit(sourceCode, new AsyncCallback<ScriptExecutionResult>() {
 
 					public void onFailure(Throwable caught) {
 						Window.alert("onFailure: groovyShellService.submit(...)");
 						submitButton.setEnabled(true);
 					}
 
 					public void onSuccess(ScriptExecutionResult result) {
 						
 						// History
 						historyFlowPanel.insert(new HTML("STACK TRACE: " + result.getStackTrace()), 0);
 						historyFlowPanel.insert(new HTML("RESULT: " + result.getResult()), 0);
 						historyFlowPanel.insert(new HTML("OUTPUT: " + result.getOutput()), 0);
 						historyFlowPanel.insert(new HTML("<br />==== " + new Date().toString() + " ===="), 0);
 
 						if(null != result.getOutput() && !"".equals(result.getOutput())) {
 							outputFlowPanel.add(new HTML(result.getOutput()));
 						}
 
 						if(null != result.getResult() && !"".equals(result.getResult())) {
 							resultFlowPanel.add(new HTML(result.getResult()));
 						}
 
 						if(null != result.getStackTrace() && !"".equals(result.getStackTrace())) {
 							stackTraceFlowPanel.add(new HTML(result.getStackTrace()));
 						}
 						
 						// Select first panel with data
 						if(outputFlowPanel.getWidgetCount() > 0) { 
 							tabPanel.selectTab(Panel.OUTPUT.position);
 						}
 						else if(resultFlowPanel.getWidgetCount() > 0) {
 							tabPanel.selectTab(Panel.RESULT.position);
 						}
 						else if(stackTraceFlowPanel.getWidgetCount() > 0) {
 							tabPanel.selectTab(Panel.STACK_TRACE.position);
 						}
 						
 						// Enabling the button again
 						submitButton.setEnabled(true);
 					}
 				});
 			}
 		};
 
 		submitButton.addClickHandler(clickHandler);
 
 		// CSS
 		submitButton.addStyleName("submitButton");
 
 		// Text Area Configuration
 		textArea.setCharacterWidth(80);
 		textArea.setVisibleLines(25);
 		
 		// Tab Panel Configuration
 		tabPanel.setWidth("100%");
 		
 		// Adding widgets to vertical panel
 		verticalPanel.add(textArea);
 		verticalPanel.add(submitButton);
 		verticalPanel.add(tabPanel);
 		
 		// Configure vertical panel
 		verticalPanel.setCellHorizontalAlignment(submitButton, HasHorizontalAlignment.ALIGN_RIGHT);
 		verticalPanel.setSpacing(5);
 
 		// Adding widget(s) to root panel
 		RootPanel.get("verticalPanel").add(verticalPanel);
 		
 		// Focus the cursor on the text area when the application loads
 		textArea.setFocus(true);
 		textArea.selectAll();
 	}
 
 	private native Document getWindowParentDocument() /*-{
 	
 		return $wnd.parent.document
 	}-*/;
 	
 }
