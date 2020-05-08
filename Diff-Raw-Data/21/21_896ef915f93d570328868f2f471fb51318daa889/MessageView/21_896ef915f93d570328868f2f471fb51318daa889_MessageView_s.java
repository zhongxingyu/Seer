 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.views.baton;
 
 import gda.configuration.properties.LocalProperties;
 import gda.jython.InterfaceProvider;
 import gda.jython.UserMessage;
 import gda.observable.IObserver;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class MessageView extends ViewPart implements IObserver {
 
 	private static final Logger logger = LoggerFactory.getLogger(MessageView.class);
 
 	/**
 	 * 
 	 */
 	public static final String ID = "uk.ac.gda.rcp.views.baton.MessageView"; //$NON-NLS-1$
 	
 	private StyledText history;
 	private Text text;
 	private KeyListener keyListener;
 
 	private Button btnSend;
 
 	private SelectionAdapter selectionListener;
 
 	@Override
 	public void init(final IViewSite site) throws PartInitException {
 		super.init(site);
 		try {
 		    InterfaceProvider.getJSFObserver().addIObserver(this);
 		} catch (Exception e) {
 			throw new PartInitException("Cannot attach to Jython Server", e);
 		}
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		
 /*		if (!LocalProperties.isAccessControlEnabled()) {
 			final Label error = new Label(parent, SWT.NONE);
 			error.setText();
 			return;
 		}*/
 		
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new GridLayout());
 		{
 			SashForm sashForm = new SashForm(container, SWT.NONE);
 			sashForm.setOrientation(SWT.VERTICAL);
 			sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 			{
 				history = new StyledText(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SEARCH | SWT.CANCEL | SWT.MULTI);
 				history.setEditable(false);
 			}
 			{
 				Composite composite = new Composite(sashForm, SWT.NONE);
 				composite.setLayout(new GridLayout(2, false));
 				{
 					text = new Text(composite, SWT.BORDER | SWT.MULTI);
 					text.setToolTipText("Enter message and ENTER to send.");
 					text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 					this.keyListener = new KeyListener() {
 						
 						@Override
 						public void keyReleased(KeyEvent e) {
 							// TODO Auto-generated method stub
 							
 						}
 						
 						@Override
 						public void keyPressed(KeyEvent e) {
 							if (e.character=='\r') {
 								sendMessage();
 								e.doit = false;
 							}
 						}
 					};
 					text.addKeyListener(keyListener);
 				}
 				{
 					this.btnSend = new Button(composite, SWT.NONE);
 					btnSend.setText("Send");
 					this.selectionListener = new SelectionAdapter() {
 						@Override
 						public void widgetSelected(SelectionEvent e) {
 							sendMessage();
 						}
 					};
 					btnSend.addSelectionListener(selectionListener);
 				}
 			}
 			sashForm.setWeights(new int[] {412, 38});
 		}
 		if(!LocalProperties.isBatonManagementEnabled()){
 			addUserMessageText("", "Baton control is not enabled for this beam line.");
 		}
 
 	}
 	
 	private void sendMessage() {
 		InterfaceProvider.getBatonStateProvider().sendMessage(text.getText());
 		text.setText("");
 	}
 
 	@Override
 	public void setFocus() {
 		this.text.setFocus();
 	}
 	
 	@Override
 	public void dispose() {
		text.removeKeyListener(keyListener);
		text.dispose();
		btnSend.removeSelectionListener(selectionListener);
		btnSend.dispose();
 		try {
 			InterfaceProvider.getJSFObserver().deleteIObserver(this);
 		} catch (Exception e) {
 			logger.error("Cannot reomve MessageView from JythonServerFacade", e);
 		}
 		super.dispose();
 	}
 	
 
 	protected void addUserMessageText(final String userName, final String text) {
 		
 		StyleRange style = new StyleRange();
 		style.start = history.getCharCount();
 		style.length = userName.length() + 3;
 		history.append("\n");
 		history.append(userName);
 		history.append("> ");
 		history.setStyleRange(style);
 
 		style = new StyleRange();
 		style.start = history.getCharCount();
 		style.length = text.length();
 		style.fontStyle = SWT.BOLD;
 		// CadetBlue from http://www.wilsonmar.com/1colors.htm#TopMenu
 		style.foreground = new Color(this.getSite().getShell().getDisplay(),95,158,160); 
 		history.append(text);
 		history.setStyleRange(style);
 	}
 
 	@Override
 	public void update(final Object theObserved, final Object changeCode) {
 		if (changeCode instanceof UserMessage) {
 			this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					UserMessage message = (UserMessage) changeCode;
    				    addUserMessageText(message.getSourceUsername(), message.getMessage());
 				}
 			});	
 		}
 	}
 
 }
