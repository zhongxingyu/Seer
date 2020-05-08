 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.epp.internal.mpc.ui.util.Util;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceViewer.ContentType;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogPage;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogViewer;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 /**
  * @author Steffen Pingel
  */
 public class MarketplacePage extends CatalogPage {
 
 	private final MarketplaceCatalogConfiguration configuration;
 
 	private CatalogDescriptor previousCatalogDescriptor;
 
 	private boolean updated;
 
 	private boolean switchLinkActivated;
 
 	private Link selectionLink;
 
 	private TabFolder tabFolder;
 
 	private TabItem searchTabItem;
 
 	public MarketplacePage(MarketplaceCatalog catalog, MarketplaceCatalogConfiguration configuration) {
 		super(catalog);
 		this.configuration = configuration;
 		setDescription(Messages.MarketplacePage_selectSolutionsToInstall);
 		setTitle(Messages.MarketplacePage_eclipseMarketplaceSolutions);
 		if (configuration.getCatalogDescriptor() != null) {
 			setTitle(configuration.getCatalogDescriptor().getLabel());
 		}
 	}
 
 	@Override
 	public void createControl(final Composite originalParent) {
 		Composite parent = originalParent;
 		boolean needSwitchMarketplaceLink = configuration.getCatalogDescriptors().size() > 1;
 
 		parent = new Composite(parent, SWT.NULL);
 		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 5).applyTo(parent);
 
 		tabFolder = new TabFolder(parent, SWT.TOP);
 		if (originalParent != parent) {
 			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tabFolder);
 		}
 
 		searchTabItem = new TabItem(tabFolder, SWT.NULL);
 		searchTabItem.setText(Messages.MarketplacePage_search);
 		final TabItem recentTabItem = new TabItem(tabFolder, SWT.NULL);
 		recentTabItem.setText(Messages.MarketplacePage_recent);
 		final TabItem popularTabItem = new TabItem(tabFolder, SWT.NULL);
 		popularTabItem.setText(Messages.MarketplacePage_popular);
 		final TabItem installedTabItem = new TabItem(tabFolder, SWT.NULL);
 		installedTabItem.setText(Messages.MarketplacePage_installed);
 
 		super.createControl(tabFolder);
 
 		searchTabItem.setControl(getControl());
 		recentTabItem.setControl(getControl());
 		popularTabItem.setControl(getControl());
 		installedTabItem.setControl(getControl());
 
 		tabFolder.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				MarketplaceViewer.ContentType contentType;
 				if (e.item == searchTabItem) {
 					contentType = ContentType.SEARCH;
 				} else if (e.item == recentTabItem) {
 					contentType = ContentType.RECENT;
 				} else if (e.item == popularTabItem) {
 					contentType = ContentType.POPULAR;
 				} else if (e.item == installedTabItem) {
 					contentType = ContentType.INSTALLED;
 				} else {
 					throw new IllegalStateException();
 				}
 				getViewer().setContentType(contentType);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		{
 			selectionLink = new Link(parent, SWT.NULL);
 			selectionLink.setToolTipText(Messages.MarketplacePage_showSelection);
 			selectionLink.addSelectionListener(new SelectionListener() {
 				public void widgetSelected(SelectionEvent e) {
 					selectionLinkActivated();
 				}
 
 				public void widgetDefaultSelected(SelectionEvent e) {
 					widgetSelected(e);
 
 				}
 			});
 			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(selectionLink);
 			computeSelectionLinkText();
 		}
 
 		if (needSwitchMarketplaceLink) {
 			Link link = new Link(parent, SWT.NULL);
 			link.setText(Messages.MarketplacePage_switchCatalog_link);
 			link.setToolTipText(Messages.MarketplacePage_selectAlternateCatalog);
 			link.addSelectionListener(new SelectionListener() {
 				public void widgetSelected(SelectionEvent e) {
 					switchMarketplaceLinkActivated();
 				}
 
 				public void widgetDefaultSelected(SelectionEvent e) {
 					widgetSelected(e);
 
 				}
 			});
 
 			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(link);
 		}
 
 		// bug 312411: a selection listener so that we can streamline install of single product
 		getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 
 			private int previousSelectionSize = 0;
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (!isCurrentPage()) {
 					return;
 				}
 				SelectionModel selectionModel = getWizard().getSelectionModel();
 				int newSelectionSize = selectionModel.getItemToOperation().size();
 
 				// important: we don't do anything if the selection is empty, since CatalogViewer
 				// sets the empty selection whenever the catalog is updated.
 				if (!event.getSelection().isEmpty()) {
 
 					if (previousSelectionSize == 0 && newSelectionSize == 1
 							&& selectionModel.computeProvisioningOperationViable()) {
 						IWizardPage currentPage = getContainer().getCurrentPage();
 						if (currentPage.isPageComplete()) {
 							IWizardPage nextPage = getWizard().getNextPage(MarketplacePage.this);
 							if (nextPage != null) {
 								getContainer().showPage(nextPage);
 							}
 						}
 					}
 				}
 				previousSelectionSize = newSelectionSize;
 			}
 		});
 
 		setControl(parent == originalParent ? tabFolder : parent);
 		MarketplaceClientUi.setDefaultHelp(getControl());
 	}
 
 	private void computeSelectionLinkText() {
 		if (selectionLink != null) {
 			final String originalText = selectionLink.getText();
 
 			String text = " "; //$NON-NLS-1$
 			int count = getWizard().getSelectionModel().getItemToOperation().size();
 			if (count == 1) {
 				text = Messages.MarketplacePage_linkShowSelection_One;
 			} else if (count > 0) {
 				text = NLS.bind(Messages.MarketplacePage_linkShowSelection_Multiple, Integer.valueOf(count));
 			}
 			if (!(text == originalText || (text != null && text.equals(originalText)))) {
 				boolean exclude = text == null || text.trim().length() == 0;
 				boolean originalExclude = ((GridData) selectionLink.getLayoutData()).exclude;
 
 				selectionLink.setText(text);
 				if (originalExclude != exclude) {
 					selectionLink.setVisible(!exclude);
 					((GridData) selectionLink.getLayoutData()).exclude = exclude;
 					((Composite) getControl()).layout(true, true);
 				}
 			}
 		}
 	}
 
 	protected void selectionLinkActivated() {
 		tabFolder.setSelection(searchTabItem);
 		getViewer().showSelected();
 	}
 
 	protected void switchMarketplaceLinkActivated() {
 		switchLinkActivated = true;
 		getWizard().getContainer().showPage(getWizard().getCatalogSelectionPage());
 	}
 
 	@Override
 	public IWizardPage getPreviousPage() {
 		if (!switchLinkActivated) {
 			return null;
 		}
 		return super.getPreviousPage();
 	}
 
 	@Override
 	public MarketplaceWizard getWizard() {
 		return (MarketplaceWizard) super.getWizard();
 	}
 
 	@Override
 	protected MarketplaceViewer getViewer() {
 		return (MarketplaceViewer) super.getViewer();
 	}
 
 	@Override
 	protected CatalogViewer doCreateViewer(Composite parent) {
 		MarketplaceViewer viewer = new MarketplaceViewer(getCatalog(), this, getWizard());
 		viewer.setMinimumHeight(MINIMUM_HEIGHT);
 		viewer.createControl(parent);
 		return viewer;
 	}
 
 	@Override
 	protected void doUpdateCatalog() {
 		if (!updated) {
 			updated = true;
 			Display.getCurrent().asyncExec(new Runnable() {
 				public void run() {
 					if (!getControl().isDisposed() && isCurrentPage()) {
 						getViewer().updateCatalog();
 					}
 				}
 			});
 		}
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		if (visible) {
 			CatalogDescriptor catalogDescriptor = configuration.getCatalogDescriptor();
 			if (catalogDescriptor != null) {
 				setTitle(catalogDescriptor.getLabel());
 			}
 			if (previousCatalogDescriptor == null || !previousCatalogDescriptor.equals(catalogDescriptor)) {
 				previousCatalogDescriptor = catalogDescriptor;
				tabFolder.setSelection(searchTabItem);
 				getWizard().initializeCatalog();
 				updated = false;
 			}
 		}
 		super.setVisible(visible);
 	}
 
 	@Override
 	public void setPageComplete(boolean complete) {
 		if (complete) {
 			complete = getWizard().getSelectionModel().computeProvisioningOperationViable();
 		}
 		computeMessages();
 		super.setPageComplete(complete);
 	}
 
 	private void computeMessages() {
 		computeStatusMessage();
 		computeSelectionLinkText();
 	}
 
 	private void computeStatusMessage() {
 		String message = null;
 		int messageType = IMessageProvider.NONE;
 
 		if (getWizard() != null) {
 			IStatus viability = getWizard().getSelectionModel().computeProvisioningOperationViability();
 			if (viability != null) {
 				message = viability.getMessage();
 				messageType = Util.computeMessageType(viability);
 			}
 		}
 
 		setMessage(message, messageType);
 	}
 
 	@Override
 	public void performHelp() {
 		getControl().notifyListeners(SWT.Help, new Event());
 	}
 }
