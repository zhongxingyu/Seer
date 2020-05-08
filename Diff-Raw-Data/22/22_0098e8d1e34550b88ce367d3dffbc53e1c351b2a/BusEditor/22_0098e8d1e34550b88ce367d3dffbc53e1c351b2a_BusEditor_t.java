 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Bus View
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.bus.view;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.bus.Bus;
 import de.tuilmenau.ics.fog.eclipse.ui.EditorRowComposite;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.EditorInput;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.SelectionProvider;
 
 
 /**
  * Editor for showing and editing the internals of a bus.
  */
 public class BusEditor extends EditorPart
 {
 	public BusEditor()
 	{
 	}
 	
 	@Override
 	public void createPartControl(Composite parent)
 	{
 		mDisplay = Display.getCurrent();
 		mSelectionCache = new SelectionProvider(mDisplay);
 		getSite().setSelectionProvider(mSelectionCache);
 
 		EditorRowComposite tGrp = new EditorRowComposite(parent, SWT.SHADOW_NONE);
 		
 		//
 		// Packet loss probability
 		//
 		int from = 1;
 		int to = 40;
 		boolean enabled = true;
 		
 		if(Config.DEVELOPER_VERSION) {
 			from = 0;
 			to = 100;
 		}
 		if(!Config.DEVELOPER_VERSION) {
 			if (mBus.getPacketLossProbability() == 0) {
 				enabled = false;
 			}
 		}
 		
 		tGrp.createRow("Loss probability:", Integer.toString(mBus.getPacketLossProbability()), "%", from, to, mBus.getPacketLossProbability(), enabled, tGrp.new SliderChangeListener() {
 			@Override
 			public void handleEvent(Event event)
 			{
 				super.handleEvent(event);
 				
 				mBus.setPacketLossProbability(mSlider.getSelection());
 			}
 		});
 
 		//
 		// Bit error probability
 		//
		tGrp.createRow("Bit error probability:", Integer.toString(mBus.getBitErrorProbability()), "%", from, to, mBus.getBitErrorProbability(), enabled, tGrp.new SliderChangeListener() {
 			@Override
 			public void handleEvent(Event event)
 			{
 				super.handleEvent(event);
 
 				mBus.setBitErrorProbability(mSlider.getSelection());
 			}
 		});
 
 		//
 		// Delay
 		//
 		int currentDelay = (int) mBus.getDelayMSec();
 		
 		tGrp.createRow("Link delay:", Integer.toString(currentDelay), "msec", 0, Math.max(1000, currentDelay), currentDelay, true, tGrp.new SliderChangeListener() {
 			@Override
 			public void handleEvent(Event event)
 			{
 				super.handleEvent(event);
 
 				mBus.setDelayMSec(mSlider.getSelection());
 			}
 		});
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException
 	{
 		setSite(site);
 		setInput(input);
 		
 		// get selected object to show in editor
 		Object inputObject;
 		if(input instanceof EditorInput) {
 			inputObject = ((EditorInput) input).getObj();
 		} else {
 			inputObject = null;
 		}
 		
 		if(inputObject != null) {
 			// update title of editor
 			setTitle(inputObject.toString());
 
 			if(inputObject instanceof Bus) {
 				mBus = (Bus) inputObject;
 				
 				// TODO impl
 			} else {
 				throw new PartInitException("Invalid input object " +inputObject +" (class=" +inputObject.getClass() +"). " +Bus.class +" expected.");
 			}
 		} else {
 			throw new PartInitException("No input for editor in " +input +" or unknown class. " +EditorInput.class +" expected.");
 		}
 	}
 	
 	@Override
 	public void doSave(IProgressMonitor arg0)
 	{
 	}
 
 	@Override
 	public void doSaveAs()
 	{
 	}
 
 	@Override
 	public boolean isDirty()
 	{
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed()
 	{
 		return false;
 	}
 
 	@Override
 	public void setFocus()
 	{
 	}
 
 
 	@Override
 	public Object getAdapter(Class required)
 	{
 		if(this.getClass().equals(required)) return this;
 		
 		Object res = super.getAdapter(required);
 		
 		if(res == null) {
 			res = Platform.getAdapterManager().getAdapter(this, required);
 			
 			if(res == null)	res = Platform.getAdapterManager().getAdapter(mBus, required);
 		}
 		
 		return res;
 	}
 
 		
 	private Bus mBus = null;
 	private SelectionProvider mSelectionCache = null;
 	private Display mDisplay = null;
 }
 
