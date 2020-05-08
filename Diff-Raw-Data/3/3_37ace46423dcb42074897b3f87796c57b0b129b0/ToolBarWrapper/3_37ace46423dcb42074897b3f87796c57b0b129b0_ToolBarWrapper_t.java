 /*===========================================================================
   Copyright (C) 2011-2012 by the Okapi Framework contributors
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
 
 import java.util.ArrayList;
 
 import net.sf.okapi.lib.tmdb.DbUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Label;
 
 class ToolBarWrapper {
 
 	private final CoolBar coolBar;
 	private final Combo cbSource;
 	private final Combo cbTarget;
 	private final Button chkFlaggedEntries;
 //	private final Button chkExpression;
 //	private final Combo cbField;
 //	private final Combo cbOperator;
 //	private final Text edValue;
 //	private final FilterExpressionPanel pnlExpr;
 	
 	private TmPanel tp;
 
 	public ToolBarWrapper (MainForm mainForm) {
 		coolBar = new CoolBar(mainForm.getShell(), SWT.FLAT);
 		//coolBar.setLocked(true);
 		
 		Composite comp = new Composite(coolBar, SWT.NONE);
 		GridLayout layout = new GridLayout(6, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		comp.setLayout(layout);
 		
 		final Label stSource = new Label(comp, SWT.NONE);
 		stSource.setText("Source:");
 		cbSource = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
 		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.widthHint = 80;
 		cbSource.setLayoutData(gdTmp);
 		cbSource.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected (SelectionEvent event) {
 				tp.verifySourceChange();
 			}
 			@Override
 			public void widgetDefaultSelected (SelectionEvent event) {
 				tp.verifySourceChange();
 			}
 		});
 
 		final Label stTarget = new Label(comp, SWT.NONE);
 		stTarget.setText("Target:");
 		cbTarget = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.widthHint = 80;
 		cbTarget.setLayoutData(gdTmp);
 		cbTarget.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected (SelectionEvent event) {
 				tp.verifyTargetChange();
 			}
 			@Override
 			public void widgetDefaultSelected (SelectionEvent event) {
 				tp.verifyTargetChange();
 			}
 		});
 		
 		final Label stFilter = new Label(comp, SWT.NONE);
 		stFilter.setText("      ");
 
 		chkFlaggedEntries = new Button(comp, SWT.CHECK);
 		chkFlaggedEntries.setText("Flagged entries only");
 		chkFlaggedEntries.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				tp.setFilterForFlaggedEntries();
 			}
 		});
 
 //		chkExpression = new Button(comp, SWT.CHECK);
 //		chkExpression.setText("Condition:");
 //		
 //		cbField = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
 //		cbField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
 //		
 //		cbOperator = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
 //		cbOperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
 //		
 //		edValue = new Text(comp, SWT.BORDER);
 //		edValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
 //
 //		pnlExpr = new FilterExpressionPanel(comp, SWT.NONE);
 //		pnlExpr.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		CoolItem ci = new CoolItem(coolBar, SWT.NONE);
 
 		ci.setControl(comp);
 	    Point pt = comp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 	    pt = ci.computeSize(pt.x, pt.y);
 	    ci.setSize(pt);
 
 	    coolBar.pack();		
 	}
 	
 	void update (TmPanel tp) {
 		TmPanel oldTp = this.tp;
 		boolean enabled = (( tp != null ) && !tp.hasRunningThread() );
 		this.tp = tp;
 		
 		// Remember current selection if possible
 		String oldSrc = cbSource.getText();
 		String oldTrg = cbTarget.getText();
 		
 		if ( tp != oldTp ) {
 			fillLocales();
 			if ( cbSource.getItemCount() > 0 ) {
 				setTarget(oldTrg);
 				setSource(oldSrc, oldSrc.isEmpty());
 			}
 		}
 		if ( tp != null ) {
 			chkFlaggedEntries.setSelection(tp.getFilterOptions().getSimpleFilterFlaggedOnly());
 		}
 		else {
 			chkFlaggedEntries.setSelection(false);
 		}
 		
 		cbSource.setEnabled(enabled);
 		cbTarget.setEnabled(enabled);
 		chkFlaggedEntries.setEnabled(enabled);
 	}
 
 	void fillLocales () {
 		cbSource.removeAll();
 		cbTarget.removeAll();
 		if ( tp == null ) return;
 		// Get the list of the visible locales
 		java.util.List<String> fields = tp.getTmOptions().getVisibleFields();
 		java.util.List<String> locs = new ArrayList<String>();
 		for ( String fn : fields ) {
 			if ( fn.startsWith(DbUtil.TEXT_PREFIX) ) {
 				String loc = DbUtil.getFieldLocale(fn);
 				if (( loc != null ) && !locs.contains(loc) ) {
 					locs.add(loc);
 				}
 			}
 		}
 		for ( String loc : locs ) {
 			cbSource.add(loc);
 			cbTarget.add(loc);
 		}
 	}
 	
 	void setSource (String locale,
 		boolean forceFirst)
 	{
 		int n = cbSource.indexOf(locale);
 		// If the selected locale is not present, select another one:
 		if ( n < 0 ) {
 			if ( cbSource.getItemCount() > 0 ) {
 				if ( forceFirst ) {
 					n = 0;
 				}
 				// Else try to see if the top one is available
 				else if ( cbTarget.getSelectionIndex() == 0 ) {
 					// If it's already the target, use the second top if possible
 					if ( cbSource.getItemCount() > 1 ) {
 						n = 1;
 					}
 					else {
 						// If there is no second locale, switch the lone loacle to source
 						n = 0;
 					}
 				}
 				else {
 					n = 0;
 				}
 			}
 		}
 		// Set the locale
 		if ( n > -1 ) {
 			cbSource.select(n);
 			// Change the target if the source is now what was the target
 			if ( n == cbTarget.getSelectionIndex() ) {
 				setTarget("");
 			}
 		}
 	}
 	
 	void setTarget (String locale) {
 		int n = cbTarget.indexOf(locale);
 		// If the selected locale is not present, select another one:
 		if ( n < 0 ) {
 			if ( cbTarget.getItemCount() > 0 ) {
 				// Try to see if the top one is available
 				if ( cbSource.getSelectionIndex() == 0 ) {
 					// If it's already the source, use the second top if possible
 					if ( cbTarget.getItemCount() > 1 ) {
 						n = 1;
 					}
 				}
 				else {
 					n = 0;
 				}
 			}
 		}
 		// Set the locale
 		if ( n > -1 ) {
 			cbTarget.select(n);
 		}
 	}
 
 	/**
 	 * Gets the current source locale.
 	 * @return the code of the current source locale or an empty string if none is selected.
 	 */
 	String getSource () {
 		return cbSource.getText();
 	}
 	
 	int getSourceIndex () {
 		return cbSource.getSelectionIndex();
 	}
 	
 	/**
 	 * Gets the current target locale.
 	 * @return the code of the current target locale or an empty string if none is selected.
 	 */
 	String getTarget () {
 		return cbTarget.getText();
 	}
 
 	int getTargetIndex () {
 		return cbTarget.getSelectionIndex();
 	}
 	
 }
