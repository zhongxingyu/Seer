 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *  Yatta Solutions - news (bug 401721)
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.epp.internal.mpc.core.service.CatalogBranding;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.core.service.News;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.ui.CatalogRegistry;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUiPlugin;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.epp.internal.mpc.ui.util.Util;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceViewer.ContentType;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.ui.discovery.DiscoveryImages;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogPage;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogViewer;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 /**
  * @author Steffen Pingel
  * @author Carsten Reckord
  */
 public class MarketplacePage extends CatalogPage {
 
 	private final MarketplaceCatalogConfiguration configuration;
 
 	private CatalogDescriptor previousCatalogDescriptor;
 
 	private boolean updated;
 
 	private Link selectionLink;
 
 	private TabFolder tabFolder;
 
 	private TabItem searchTabItem;
 
 	private TabItem recentTabItem;
 
 	private TabItem popularTabItem;
 
 	private TabItem newsTabItem;
 
 	private Control tabContent;
 
 	private TabItem installedTabItem;
 
 	private NewsViewer newsViewer;
 
 	private CatalogSwitcher marketplaceSwitcher;
 
 	protected boolean disableTabSelection;
 
 	protected CatalogDescriptor lastSelection;
 
 	public MarketplacePage(MarketplaceCatalog catalog, MarketplaceCatalogConfiguration configuration) {
 		super(catalog);
 		this.configuration = configuration;
 		setDescription(Messages.MarketplacePage_selectSolutionsToInstall);
 		setTitle(Messages.MarketplacePage_eclipseMarketplaceSolutions);
 		updateTitle();
 	}
 
 	private void updateTitle() {
 		if (configuration.getCatalogDescriptor() != null) {
 			setTitle(configuration.getCatalogDescriptor().getLabel());
 		}
 	}
 
 	@Override
 	public void createControl(final Composite originalParent) {
 		Composite parent = originalParent;
 		boolean needSwitchMarketplaceControl = configuration.getCatalogDescriptors().size() > 1;
 
 		parent = new Composite(parent, SWT.NULL);
 		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 5).applyTo(parent);
 
 		tabFolder = new TabFolder(parent, SWT.TOP);
 		if (originalParent != parent) {
 			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tabFolder);
 		}
 
 		super.createControl(tabFolder);
 
 		tabContent = getControl();
 		createSearchTab();
 		createRecentTab();
 		createPopularTab();
 		createInstalledTab();
 		createNewsTab();
 		tabFolder.setSelection(searchTabItem);
 
 		tabFolder.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				setActiveTab((TabItem) e.item);
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
 
 		if (needSwitchMarketplaceControl) {
 			createMarketplaceSwitcher(parent);
 		}
 		updateBranding();
 
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
 		getViewer().addPropertyChangeListener(new IPropertyChangeListener() {
 
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(MarketplaceViewer.CONTENT_TYPE_PROPERTY) && event.getNewValue() != null) {
 					setActiveTab((ContentType) event.getNewValue());
 				}
 			}
 		});
 		setControl(parent == originalParent ? tabFolder : parent);
 		MarketplaceClientUi.setDefaultHelp(tabContent);
 	}
 
 	private void setActiveTab(TabItem tab) {
 		if (disableTabSelection) {
 			return;
 		}
 		if (tab == newsTabItem) {
 			final News news = getNews();
 			boolean wasUpdated = newsViewer.isUpdated(news);
 			newsViewer.showNews(news);
 			if (wasUpdated) {
 				updateBranding();
 				tabFolder.setSelection(newsTabItem);
 				// required for Mac to not switch back to first tab
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						tabFolder.setSelection(newsTabItem);
					}
				});
 			}
 			return;
 		}
 		for (ContentType contentType : ContentType.values()) {
 			if (getTabItem(contentType) == tab) {
 				setActiveTab(contentType);
 				return;
 			}
 		}
 		throw new IllegalArgumentException();
 	}
 
 	private void setActiveTab(ContentType contentType) {
 		if (disableTabSelection) {
 			return;
 		}
 		final TabItem tabItem = getTabItem(contentType);
 		tabFolder.setSelection(tabItem);
 		getViewer().setContentType(contentType);
 	}
 
 	private TabItem getTabItem(ContentType content) {
 		switch (content) {
 		case INSTALLED:
 			return installedTabItem;
 		case POPULAR:
 			return popularTabItem;
 		case RECENT:
 			return recentTabItem;
 		case SEARCH:
 			return searchTabItem;
 		case SELECTION:
 			return searchTabItem;
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	private void createInstalledTab() {
 		installedTabItem = new TabItem(tabFolder, SWT.NULL);
 		installedTabItem.setText(Messages.MarketplacePage_installed);
 		installedTabItem.setControl(tabContent);
 	}
 
 	private void createPopularTab() {
 		popularTabItem = new TabItem(tabFolder, SWT.NULL);
 		popularTabItem.setText(Messages.MarketplacePage_popular);
 		popularTabItem.setControl(tabContent);
 	}
 
 	private void createRecentTab() {
 		recentTabItem = new TabItem(tabFolder, SWT.NULL);
 		recentTabItem.setText(Messages.MarketplacePage_recent);
 		recentTabItem.setControl(tabContent);
 	}
 
 	private void createSearchTab() {
 		searchTabItem = new TabItem(tabFolder, SWT.NULL);
 		searchTabItem.setText(Messages.MarketplacePage_search);
 		searchTabItem.setControl(tabContent);
 	}
 
 	private void createNewsTab() {
 		newsTabItem = new TabItem(tabFolder, SWT.NULL | SWT.BOLD);
 		newsTabItem.setText(Messages.MarketplacePage_DefaultNewsTitle);
 
 		News news = getNews();
 		if (news == null) {
 			newsTabItem.dispose();
 			return;
 		}
 
 		if (news.getShortTitle() != null && news.getShortTitle().length() > 0) {
 			String title = news.getShortTitle();
 			String tooltip = title;
 			if (title.length() > 40) {
 				tooltip = title;
 				title = title.substring(0, 39) + '\u2026';
 			}
 			newsTabItem.setText(title);
 			newsTabItem.setToolTipText(tooltip);
 		}
 
 		if (newsViewer == null) {
 			createNewsViewer(tabFolder);
 		}
 		newsTabItem.setControl(newsViewer.getControl());
 
 		updateNewsStatus();
 	}
 
 	private void updateNewsStatus() {
 		News news = getNews();
 
 		Image tabImage = null;
 		if (news != null && newsViewer.isUpdated(news)) {
 			tabImage = MarketplaceClientUiPlugin.getInstance()
 					.getImageRegistry()
 					.get(MarketplaceClientUiPlugin.NEWS_ICON_UPDATE);
 		}
 		newsTabItem.setImage(tabImage);
 		newsTabItem.getParent().layout();
 	}
 
 	private News getNews() {
 		CatalogDescriptor descriptor = configuration.getCatalogDescriptor();
 		News news = CatalogRegistry.getInstance().getCatalogNews(descriptor);
 		return news;
 	}
 
 	private void createNewsViewer(Composite parent) {
 		newsViewer = new NewsViewer(getWizard());
 		newsViewer.createControl(parent);
 	}
 
 	private void createMarketplaceSwitcher(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 
 		final CatalogSwitcher switcher = new CatalogSwitcher(composite, SWT.BORDER, configuration);
 		switcher.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				CatalogDescriptor descriptor = (CatalogDescriptor) ((IStructuredSelection) event.getSelection()).getFirstElement();
 				showMarketplace(descriptor);
 			}
 		});
 		CatalogDescriptor selectedDescriptor = configuration.getCatalogDescriptor();
 		if (selectedDescriptor != null) {
 			switcher.setSelection(new StructuredSelection(selectedDescriptor));
 		}
 		marketplaceSwitcher = switcher;
 		GridDataFactory.fillDefaults()
 		.align(SWT.FILL, SWT.FILL)
		.grab(true, false)
 		.minSize(1, SWT.DEFAULT)
 		.hint(500, SWT.DEFAULT)
 		.applyTo(composite);
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
 
 	@Override
 	public IWizardPage getPreviousPage() {
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
 						getWizard().updateNews();
 						getViewer().updateCatalog();
 						updateBranding();
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
 				getViewer().setContentType(ContentType.SEARCH);
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
 
 	private void updateBranding() {
 		disableTabSelection = true;
 		updateTitle();
 		CatalogDescriptor descriptor = configuration.getCatalogDescriptor();
 		CatalogBranding branding = CatalogRegistry.getInstance().getCatalogBranding(descriptor);
 		if (branding == null) {
 			branding = getDefaultBranding();
 		}
 
 		newsTabItem.dispose();
 		searchTabItem.dispose();
 		recentTabItem.dispose();
 		popularTabItem.dispose();
 		installedTabItem.dispose();
 
 		boolean hasSearchTab = branding.hasSearchTab();
 		if (hasSearchTab) {
 			createSearchTab();
 			searchTabItem.setText(branding.getSearchTabName());
 		}
 		boolean hasRecentTab = branding.hasRecentTab();
 		if (hasRecentTab) {
 			createRecentTab();
 			recentTabItem.setText(branding.getRecentTabName());
 		}
 
 		boolean hasPopularTab = branding.hasPopularTab();
 		if (hasPopularTab) {
 			createPopularTab();
 			popularTabItem.setText(branding.getPopularTabName());
 		}
 
 		createInstalledTab();
 
 		createNewsTab();
 
 		tabFolder.setSelection(searchTabItem);
 
 		try {
 			ImageDescriptor wizardIconDescriptor;
 			if (branding.getWizardIcon() == null) {
 				wizardIconDescriptor = DiscoveryImages.BANNER_DISOVERY;
 			} else {
 				wizardIconDescriptor = ImageDescriptor.createFromURL(new URL(branding.getWizardIcon()));
 			}
 			setImageDescriptor(wizardIconDescriptor);
 		} catch (MalformedURLException e) {
 			MarketplaceClientUi.error(e);
 		}
 		disableTabSelection = false;
 	}
 
 	private CatalogBranding getDefaultBranding() {
 		CatalogBranding branding = new CatalogBranding();
 		branding.setHasSearchTab(true);
 		branding.setHasPopularTab(true);
 		branding.setHasRecentTab(true);
 		branding.setSearchTabName(Messages.MarketplacePage_search);
 		branding.setPopularTabName(Messages.MarketplacePage_popular);
 		branding.setRecentTabName(Messages.MarketplacePage_recent);
 		branding.setWizardTitle(Messages.MarketplacePage_eclipseMarketplaceSolutions);
 		return branding;
 	}
 
 	public IStatus showMarketplace(CatalogDescriptor catalogDescriptor) {
 		if (configuration.getCatalogDescriptor() != catalogDescriptor) {
 			if (getWizard().getSelectionModel().getSelectedCatalogItems().size() > 0) {
 				boolean discardSelection = MessageDialog.openConfirm(getShell(),
 						Messages.MarketplacePage_selectionSolutions, Messages.MarketplacePage_discardPendingSolutions);
 				if (discardSelection) {
 					getWizard().getSelectionModel().clear();
 					computeSelectionLinkText();
 				} else {
 					if (marketplaceSwitcher != null) {
 						marketplaceSwitcher.setSelection(new StructuredSelection(lastSelection));
 					}
 					return Status.CANCEL_STATUS;
 				}
 			}
 			lastSelection = catalogDescriptor;
 			configuration.setCatalogDescriptor(catalogDescriptor);
 			if (marketplaceSwitcher != null) {
 				marketplaceSwitcher.setSelection(new StructuredSelection(catalogDescriptor));
 			}
 			updateCatalog();
 		}
 		return Status.OK_STATUS;
 	}
 
 	private void updateCatalog() {
 		getWizard().initializeCatalog();
 		getWizard().updateNews();
 		getViewer().updateCatalog();
 		updateBranding();
 	}
 
 	public void show(CatalogDescriptor catalogDescriptor, MarketplaceViewer.ContentType content) {
 		IStatus proceed = Status.OK_STATUS;
 		if (catalogDescriptor != null) {
 			proceed = showMarketplace(catalogDescriptor);
 		}
 		if (proceed.isOK()) {
 			setActiveTab(content);
 		}
 	}
 
 	public void show(CatalogDescriptor catalogDescriptor, final Set<Node> nodes) {
 		IStatus proceed = Status.OK_STATUS;
 		if (catalogDescriptor != null) {
 			proceed = showMarketplace(catalogDescriptor);
 		}
 		if (proceed.isOK()) {
 			setActiveTab(searchTabItem);
 			getViewer().show(nodes);
 		}
 	}
 
 	public void search(CatalogDescriptor catalogDescriptor, final Market searchMarket, final Category searchCategory,
 			final String searchString) {
 		IStatus proceed = Status.OK_STATUS;
 		if (catalogDescriptor != null) {
 			proceed = showMarketplace(catalogDescriptor);
 		}
 		if (proceed.isOK()) {
 			setActiveTab(searchTabItem);
 			getViewer().search(searchMarket, searchCategory, searchString);
 		}
 	}
 }
