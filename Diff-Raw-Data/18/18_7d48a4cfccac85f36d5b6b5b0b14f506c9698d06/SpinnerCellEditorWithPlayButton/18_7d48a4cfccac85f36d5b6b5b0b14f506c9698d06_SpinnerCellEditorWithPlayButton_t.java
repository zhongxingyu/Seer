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
 
 package uk.ac.gda.richbeans.components.cell;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import uk.ac.gda.common.rcp.util.GridUtils;
 import uk.ac.gda.richbeans.components.scalebox.RangeBox;
 import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
 import uk.ac.gda.richbeans.event.ValueListener;
 
 import com.swtdesigner.SWTResourceManager;
 
 
 /**
  * A cell editor that presents a list of items in a spinner box.
  * <p>
  * This class may be instantiated; it is not intended to be subclassed.
  * </p>
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class SpinnerCellEditorWithPlayButton extends AppliableCellEditor {
 
 	private final String      playJobName;
 	private final long        waitTime;
 	
 	private boolean        isPlaying = false;
 	private Button         play;
 	private RangeBox       rangeBox;
 	private SpinnerWrapper spinner;
	private Composite area;
 
 	/**
 	 * 
 	 * @param viewer
 	 * @param playJobName
 	 * @param waitTime - ms
 	 */
 	public SpinnerCellEditorWithPlayButton(final TableViewer viewer, 
 			                               final String      playJobName,
 			                               final long        waitTime) {
 		
 		super(viewer.getTable());		
 		this.playJobName = playJobName;
 		this.waitTime    = waitTime>0 ? waitTime : 1000;
 	}
 	
 
 	@Override
 	protected Control createControl(Composite parent) {
 		
		this.area   = new Composite(parent, SWT.NONE);
 		area.setLayout(new GridLayout(3, false));
 		GridUtils.removeMargins(area);
 		
         this.spinner = new SpinnerWrapper(area, SWT.NONE);
         spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
         spinner.on();
        
         this.play = new Button(area, SWT.TOGGLE);
         play.setImage(SWTResourceManager.getImage(SpinnerCellEditorWithPlayButton.class, "/icons/control_play_blue.png"));
         final GridData playData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
         playData.widthHint  = 24;
         playData.heightHint = 24;
         play.setLayoutData(playData);
         play.setToolTipText("Play through the slices");
         
         play.addSelectionListener(new SelectionAdapter() {
            	@Override
        	    public void widgetDefaultSelected(SelectionEvent e) {
            		isPlaying = !isPlaying;
            		updatePlaying();
         	}
         	@Override
 			public void widgetSelected(SelectionEvent e) {
         		isPlaying = !isPlaying;
         		updatePlaying();
         	}
 		});
         
         this.rangeBox = new RangeBox(area, SWT.NONE);
         rangeBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         rangeBox.setIntegerBox(true);
         rangeBox.setRangeOnly(true);
         GridUtils.setVisible(rangeBox, false);
         rangeBox.off();
        
         return area;
 	}
 	
 	public void setRangeMode(final boolean range)  {
 		GridUtils.setVisible(rangeBox,  range);
 		GridUtils.setVisible(spinner,  !range);
 		GridUtils.setVisible(play,     !range);
 		((Composite)this.getControl()).getParent().layout(new Control[]{rangeBox,spinner,play});
 		if (range) {
 			rangeBox.setValue(spinner.getValue().toString());
 			rangeBox.on();
 			spinner.off();
 		} else {
 			spinner.setValue(rangeBox.getRange().get(0).intValue());
 			rangeBox.off();
 			spinner.on();
 		}
 	}
 	
 	private IProgressMonitor playMonitor;
 	/**
 	 * Plays or stops the playing
 	 */
 	protected void updatePlaying() {
 		
 		if (playJobName==null) return;
 		
 		if (!isPlaying) {
 			if (playMonitor!=null) playMonitor.setCanceled(true);
 			playMonitor = null;
 			return;
 		}
 		
 		final int currentValue = (Integer)spinner.getValue();
 		final int max          = spinner.getMaximum();
 		final int min          = spinner.getMinimum();
 		
 		final Job job = new Job(playJobName) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 			
 				playMonitor = monitor;
 				int val = currentValue;
 				while(!monitor.isCanceled() && !getControl().isDisposed()) {
 					val++;
 					if (val>max) val = min;
 					
 					final int newValue = val;
 					getControl().getDisplay().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							if (spinner.isDisposed()) return;
 							spinner.setValue(newValue);
 						    spinner.fireValueListeners();
 						}
 					});
 					
 					try {
 						Thread.sleep(waitTime);
 					} catch (InterruptedException ignored) {
 						
 					}
 				}
 				
 				return Status.OK_STATUS;
 			}
 		};
 		job.setPriority(Job.INTERACTIVE);
 		job.setUser(false);
 		job.schedule();
 	}
 
 
 	@Override
 	public void activate() {
 		((Composite)this.getControl()).getParent().layout();
 	}
 
 	@Override
 	public void deactivate() {
         super.deactivate();
         play.setSelection(false);
         isPlaying = false;
         updatePlaying();
 	}
 
 
 	@Override
 	protected Object doGetValue() {
 		if (spinner.isOn()) {
 			return spinner.getValue();
 		} else if (rangeBox.isOn()) {
 			return rangeBox.getValue();
 		}
 		return null;
 	}
 
 
 	@Override
 	protected void doSetFocus() {
 		if (spinner.isOn()) {
 			spinner.setFocus();
 		} else if (rangeBox.isOn()) {
 			rangeBox.setFocus();
 		}
 	}
 
 
 	@Override
 	protected void doSetValue(Object value) {
 		if (spinner.isOn()) {
 			try {
 				spinner.setValue(value);
 			} catch (java.lang.NumberFormatException nfe) {
 				spinner.setValue(0d);
 			}
 		} else if (rangeBox.isOn()) {
 			rangeBox.setValue(value);
 		}
 	}
 
 	/**
 	 * Applies the currently selected value and deactivates the cell editor
 	 */
 	@Override
 	public void applyEditorValueAndDeactivate() {
 		// must set the selection before getting value
 		Object newValue = doGetValue();
 		markDirty();
 		boolean isValid = isCorrect(newValue);
 		setValueValid(isValid);
 		fireApplyEditorValue();
 		deactivate();
 	}
 
 	public void addValueListener(ValueListener l) {
 		spinner.addValueListener(l);
 		rangeBox.addValueListener(l);
 	}
 
 	/**
 	 * @param i
 	 */
 	public void setMaximum(int i) {
 		if (spinner!=null) spinner.setMaximum(i);
 		if (rangeBox!=null) rangeBox.setMaximum(i);
 	}
 	/**
 	 * @param i
 	 */
 	public void setMinimum(int i) {
 		if (spinner!=null) spinner.setMinimum(i);
 		if (rangeBox!=null) rangeBox.setMinimum(i);
 	}
 
 
 	public void setRangeDialogTitle(String title) {
 		if (rangeBox!=null) rangeBox.setDialogTitle(title);
 	}
 
 
 	public void setPlayButtonVisible(boolean isVisible) {
 		GridUtils.setVisible(play, isVisible);
 		play.getParent().layout(play.getParent().getChildren());
 	}
	
	public void setBackground(Color back) {
		area.setBackground(back);
		rangeBox.setBackground(back);
	}
	
	@Override
	protected int getDoubleClickTimeout() {
		return 0;
	}
 }
