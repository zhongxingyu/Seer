 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.olifant;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Label;
 
 class ToolBarWrapper {
 
 	//private MainForm mainForm;
 	private final CoolBar coolBar;
 	private final Combo cbSource;
 	private final Combo cbTarget;
 	
 	public ToolBarWrapper (MainForm mainForm) {
 		coolBar = new CoolBar(mainForm.getShell(), SWT.FLAT);
 		//coolBar.setLocked(true);
 		
 		Composite comp = new Composite(coolBar, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
 		
 		final Label stSource = new Label(comp, SWT.NONE);
 		stSource.setText("Source:");
 		cbSource = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
 
 		final Label stTarget = new Label(comp, SWT.NONE);
 		stTarget.setText("Target:");
 		cbTarget = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
 
 		CoolItem ci = new CoolItem(coolBar, SWT.NONE);
 
 		ci.setControl(comp);
 	    Point pt = comp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 	    pt = ci.computeSize(pt.x, pt.y);
 	    ci.setSize(pt);
 
 	    coolBar.pack();		
 	}
 	
 	void update (TmPanel tp) {
 		boolean enabled = (( tp != null ) && !tp.hasRunningThread() );
 		
 		cbSource.removeAll();
 		cbTarget.removeAll();
 		if ( tp != null ) {
 			java.util.List<String> locs = tp.getTm().getLocales();
 			for ( String loc : locs ) {
 				cbSource.add(loc);
 				cbTarget.add(loc);
 			}
 //TODO: select from tm options object			
 			if ( locs.size() > 0 ) cbSource.select(0);
 			if ( locs.size() > 1 ) cbTarget.select(1);
 		}
 		
 		cbSource.setEnabled(enabled);
 		cbTarget.setEnabled(enabled);
 	}
 
 }
