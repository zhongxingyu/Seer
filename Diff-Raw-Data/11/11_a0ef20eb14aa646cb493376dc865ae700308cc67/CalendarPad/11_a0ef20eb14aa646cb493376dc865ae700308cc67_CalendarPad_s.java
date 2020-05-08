 package org.eclipse.iee.sample.calendar.pad;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.iee.editor.core.pad.Pad;
 
 public class CalendarPad extends Pad {
 	
 	public CalendarPad() {
 		super();
 	}
 
 	protected CalendarPad(String containerID) {
 		super();
 	}
 	
 	@Override
 	public void createPartControl(Composite parent)
 	{
 		parent.setLayout(new RowLayout());
 		@SuppressWarnings("unused")
 		DateTime calendar = new DateTime(parent, SWT.CALENDAR);
 	}
 
 	
 	@Override
 	public Pad copy() {
 		// TODO Auto-generated method stub
 		return new CalendarPad();
 	}
 
 	@Override
 	public void save() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void unsave() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public String getType() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public void onContainerAttached() {
 		// TODO Auto-generated method stub		
 	}
 
 	@Override
 	public void activate() {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
