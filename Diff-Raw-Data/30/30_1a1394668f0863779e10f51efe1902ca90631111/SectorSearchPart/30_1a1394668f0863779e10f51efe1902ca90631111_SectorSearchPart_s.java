 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.djv.stockresearcher.parts;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import org.djv.stockresearcher.db.AppState;
 import org.djv.stockresearcher.db.IndustryStockListener;
 import org.djv.stockresearcher.db.StockDB;
 import org.djv.stockresearcher.db.StockDataChangeListener;
 import org.djv.stockresearcher.model.StockData;
 import org.djv.stockresearcher.widgets.StockTable;
 import org.djv.stockresearcher.widgets.TextProgressBar;
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 
 public class SectorSearchPart implements IndustryStockListener, StockDataChangeListener {
 	
 	private Combo sectorCombo;
 	private Combo industryCombo;
 	private Button searchButton;
 	private Button addButton;
 	private TextProgressBar indProgressBar;
 	private TextProgressBar stockProgressLabel;
 	
 	StockTable table;
 	
 	private StockDB db = StockDB.getInstance();
 	
 	@PostConstruct
 	public void postConstruct(final Composite parent) {
 		buildStockTable(parent);
 		db.addIndustryStockListener(this);
 		db.addStockDataChangeListener(this);
 	}
 
 	public void buildStockTable(Composite leftSide) {
 		Composite stockComboComposite = new Composite(leftSide, SWT.BORDER);
 		stockComboComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		stockComboComposite.setLayout(new GridLayout(6, false));
 		
 		sectorCombo = new Combo(stockComboComposite, SWT.NONE);
 		sectorCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		sectorCombo.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				refreshIndustryCombo();
 			}
 		});
 		
 		refreshSectors();
 		
 		industryCombo = new Combo(stockComboComposite, SWT.NONE);
 		industryCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		
 		searchButton = new Button(stockComboComposite, SWT.NONE);
 		searchButton.setText("Search");
 		searchButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
 		searchButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				int sIndex = sectorCombo.getSelectionIndex();
 				if (sIndex == -1){
 					return;
 				}
 				final String sector = sectorCombo.getItem(sIndex);
 				
 				int iIndex = industryCombo.getSelectionIndex();
 				if (iIndex == -1){
 					return;
 				}
 				String industry = industryCombo.getItem(iIndex);
 				
 				table.reset();
 				db.updateSectorAndIndustry(sector, industry);
 			}
 		});
 		
 		addButton = new Button(stockComboComposite, SWT.NONE);
 		addButton.setText("Watch Selected");
 		addButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
 		addButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				List<StockData> sdList = table.getSelectedStocks();
 				if(sdList != null){
 					try {
 						db.addAllToWatchList(sdList);
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		indProgressBar = new TextProgressBar(stockComboComposite, SWT.NONE);
 		indProgressBar.setText("");
 		indProgressBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		stockProgressLabel = new TextProgressBar(stockComboComposite, SWT.NONE);
 		stockProgressLabel.setText("");
 		stockProgressLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		table = new StockTable (stockComboComposite, SWT.NONE);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
 		table.setLayoutData(data);
 		
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				tableRowSelect();
 			}
 		});
 		table.packColumns();
 	}
 
 	public void tableRowSelect() {
 		StockData sd = table.getSelectedStock();
 		if (sd != null){
 			AppState.getInstance().setSelectedStock(sd);
 		}
 	}
 	
 	private void refreshIndustryCombo() {
 		String sector = sectorCombo.getItem(sectorCombo.getSelectionIndex());
 		List<String> cl;
 		try {
 			cl = db.getIndustriesForSector(sector);
 			industryCombo.removeAll();
 			industryCombo.setItems(cl.toArray(new String[0]));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		industryCombo.getParent().layout(true,  true);
 	}
 
 	private void refreshSectors() {
 		sectorCombo.removeAll();
 		try {
 			sectorCombo.setItems(db.getAllSectors().toArray(new String[0]));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		sectorCombo.getParent().layout(true,  true);
 	}
 
 	@Focus
 	public void setFocus() {
 	}
 	
 	@Override
 	public void notifyChanged(final String industryName, final List<StockData> sdList, final int industriesToUpdate, final int industriesUpdated, final int beginOrEnd) {
 		if (!table.isDisposed()){
 			if (beginOrEnd == 0){
 				indProgressBar.setText("Updating \"" + industryName + "\" (" + industriesUpdated + "/" + industriesToUpdate + ")");
 				indProgressBar.setMaximum(industriesToUpdate);
 				indProgressBar.setSelection(industriesUpdated);
 			}
 
 			if (beginOrEnd == 1){
 				indProgressBar.setText("Updated \"" + industryName + "\" (" + industriesUpdated + "/" + industriesToUpdate + ")");
 				if (industriesUpdated == 1){
 					table.packColumns();
 				}
 			}
 
 			if (sdList == null){
 				if (beginOrEnd == 1){
 					indProgressBar.setText("");
 					indProgressBar.setMaximum(0);
 					indProgressBar.setSelection(0);
 					table.packColumns();
 				}
 				
 			} else {
 				table.addOrUpdateItems(sdList);
 			}
 				
 		}
 	}
 
 	@Override
 	public void notifyChanged(final StockData sd, final int toUpdate, final int updated) {
 		if (sd == null || toUpdate == updated){
 			stockProgressLabel.setText("");
 			stockProgressLabel.setMaximum(0);
 			stockProgressLabel.setSelection(0);
 		} else {
 			stockProgressLabel.setMaximum(toUpdate);
 			stockProgressLabel.setSelection(updated);
 			stockProgressLabel.setText("Updating " + sd.getStock().getSymbol()  + "(" + updated + "/" + toUpdate + ")");
 		}
 		
 		if (sd != null){
			if (sd.isRanksCalculated() && (sd.getGrowthRank() == 0.00 && sd.getYieldRank()==0.00)){
 				table.removeItem(sd);
 			} else {
 				if (!table.isDisposed()){
 					table.updateItem(sd, true);
 				}
 			}
 		}
 	}
 }
