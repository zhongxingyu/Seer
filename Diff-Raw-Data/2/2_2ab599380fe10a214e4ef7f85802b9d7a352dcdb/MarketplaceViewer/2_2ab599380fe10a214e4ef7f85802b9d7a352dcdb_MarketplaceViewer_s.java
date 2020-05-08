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
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCategory;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCategory.Contents;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.discovery.Catalog;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.discovery.model.Tag;
 import org.eclipse.equinox.internal.p2.discovery.util.CatalogCategoryComparator;
 import org.eclipse.equinox.internal.p2.discovery.util.CatalogItemComparator;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.ControlListItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.PatternFilter;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogConfiguration;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogFilter;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogViewer;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * @author Steffen Pingel
  * @author David Green
  */
 public class MarketplaceViewer extends CatalogViewer {
 
 	enum ContentType {
 		SEARCH, RECENT, POPULAR, INSTALLED
 	}
 
 	public static class MarketplaceCatalogContentProvider extends CatalogContentProvider {
 
 		private static final Object[] NO_ELEMENTS = new Object[0];
 
 		@Override
 		public Catalog getCatalog() {
 			return super.getCatalog();
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (getCatalog() != null) {
 				// don't provide any categories unless it's featured
 				List<Object> items = new ArrayList<Object>(getCatalog().getItems());
 				for (CatalogCategory category : getCatalog().getCategories()) {
 					if (category instanceof MarketplaceCategory) {
 						MarketplaceCategory marketplaceCategory = (MarketplaceCategory) category;
 						if (marketplaceCategory.getContents() == Contents.FEATURED) {
 							items.add(category);
 						}
 					}
 				}
 				return items.toArray();
 			}
 			return NO_ELEMENTS;
 		}
 
 	}
 
 	private String queryText;
 
 	private ViewerFilter[] filters;
 
 	private ContentType contentType = ContentType.SEARCH;
 
 	public MarketplaceViewer(Catalog catalog, IShellProvider shellProvider, IRunnableContext context,
 			CatalogConfiguration configuration) {
 		super(catalog, shellProvider, context, configuration);
 		setRefreshJobDelay(100L);
 	}
 
 	@Override
 	protected void doCreateHeaderControls(Composite parent) {
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof MarketplaceFilter) {
 				((MarketplaceFilter) filter).createControl(parent);
 			}
 		}
 		super.doCreateHeaderControls(parent);
 	}
 
 	@Override
 	protected CatalogContentProvider doCreateContentProvider() {
 		return new MarketplaceCatalogContentProvider();
 	}
 
 	@Override
 	protected void catalogUpdated(boolean wasCancelled) {
 		super.catalogUpdated(wasCancelled);
 
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof MarketplaceFilter) {
 				((MarketplaceFilter) filter).catalogUpdated(wasCancelled);
 			}
 		}
 	}
 
 	@Override
 	protected void doFind(String text) {
 		this.queryText = text;
 
 		doQuery();
 	}
 
 	@Override
 	protected ControlListItem<?> doCreateViewerItem(Composite parent, Object element) {
 		if (element instanceof CatalogItem) {
 			CatalogItem catalogItem = (CatalogItem) element;
 			if (catalogItem.getData() instanceof CatalogDescriptor) {
 				CatalogDescriptor catalogDescriptor = (CatalogDescriptor) catalogItem.getData();
 				return new BrowseCatalogItem(parent, getResources(), shellProvider,
 						(MarketplaceCategory) catalogItem.getCategory(), catalogDescriptor, this);
 			} else {
 				DiscoveryItem discoveryItem = new DiscoveryItem(parent, SWT.NONE, getResources(), shellProvider,
 						catalogItem, this);
 				discoveryItem.setSelected(getCheckedItems().contains(catalogItem));
 				return discoveryItem;
 			}
 		} else if (element instanceof MarketplaceCategory) {
 			MarketplaceCategory category = (MarketplaceCategory) element;
 			if (category.getContents() == Contents.FEATURED) {
 				category.setName("Featured");
 			} else {
 				throw new IllegalStateException();
 			}
 		}
 		return super.doCreateViewerItem(parent, element);
 	}
 
 	private void doQuery() {
 		Market market = null;
 		Category category = null;
 
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof AbstractTagFilter) {
 				AbstractTagFilter tagFilter = (AbstractTagFilter) filter;
 				if (tagFilter.getTagClassification() == Category.class) {
 					Tag tag = tagFilter.getSelected().isEmpty() ? null : tagFilter.getSelected().iterator().next();
 					if (tag != null) {
 						if (tag.getTagClassifier() == Market.class) {
 							market = (Market) tag.getData();
 						} else if (tag.getTagClassifier() == Category.class) {
 							category = (Category) tag.getData();
 						}
 					}
 				}
 			}
 		}
 		doQuery(market, category, queryText);
 	}
 
 	private void doQuery(final Market market, final Category category, final String queryText) {
 		try {
 			final ContentType queryType = contentType;
 			final IStatus[] result = new IStatus[1];
 			context.run(true, true, new IRunnableWithProgress() {
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					switch (queryType) {
 					case POPULAR:
 						result[0] = getCatalog().popular(monitor);
 						break;
 					case RECENT:
 						result[0] = getCatalog().recent(monitor);
 						break;
 					case INSTALLED:
 						result[0] = getCatalog().installed(monitor);
 						break;
 					case SEARCH:
 					default:
 						if (queryText == null || queryText.length() == 0) {
 							result[0] = getCatalog().featured(monitor);
 						} else {
 							result[0] = getCatalog().performQuery(market, category, queryText, monitor);
 						}
 						break;
 					}
 					postDiscovery();
 				}
 			});
 
 			if (result[0] != null && !result[0].isOK()) {
 				StatusManager.getManager().handle(result[0],
 						StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 			}
 		} catch (InvocationTargetException e) {
 			IStatus status = computeStatus(e, "Unexpected exception");
 			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 		} catch (InterruptedException e) {
 			// cancelled by user so nothing to do here.
 		}
 		super.doFind(queryText);
 	}
 
 	@Override
 	public MarketplaceCatalog getCatalog() {
 		return (MarketplaceCatalog) super.getCatalog();
 	}
 
 	public ContentType getContentType() {
 		return contentType;
 	}
 
 	@Override
 	protected PatternFilter doCreateFilter() {
 		return new MarketplacePatternFilter();
 	}
 
 	public void setContentType(ContentType contentType) {
 		if (this.contentType != contentType) {
 			this.contentType = contentType;
 			setHeaderVisible(contentType == ContentType.SEARCH);
 			doQuery();
 		}
 	}
 
 	@Override
 	public void setHeaderVisible(boolean visible) {
 		if (visible != isHeaderVisible()) {
 			if (!visible) {
 				filters = getViewer().getFilters();
 				getViewer().resetFilters();
 			} else {
 				if (filters != null) {
 					getViewer().setFilters(filters);
 					filters = null;
 				}
 			}
 			super.setHeaderVisible(visible);
 		}
 	}
 
 	@Override
 	protected boolean doFilter(CatalogItem item) {
 		// all filtering is done server-side, so never filter here
 		return true;
 	}
 
 	@Override
 	protected StructuredViewer doCreateViewer(Composite container) {
 		StructuredViewer viewer = super.doCreateViewer(container);
 		viewer.setSorter(new ViewerSorter() {
 			CatalogCategoryComparator categoryComparator = new CatalogCategoryComparator();
 
 			CatalogItemComparator itemComparator = new CatalogItemComparator();
 
 			@Override
 			public int compare(Viewer viewer, Object o1, Object o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 				CatalogCategory cat1 = getCategory(o1);
 				CatalogCategory cat2 = getCategory(o2);
 
 				// FIXME filter uncategorized items?
 				if (cat1 == null) {
 					return (cat2 != null) ? 1 : 0;
 				} else if (cat2 == null) {
 					return 1;
 				}
 
 				int i = categoryComparator.compare(cat1, cat2);
 				if (i == 0) {
 					if (o1 instanceof CatalogCategory) {
 						return -1;
 					}
 					if (o2 instanceof CatalogCategory) {
 						return 1;
 					}
 
 					CatalogItem i1 = (CatalogItem) o1;
 					CatalogItem i2 = (CatalogItem) o2;
 
 					// catalog descriptor comes last
 					if (i1.getData() instanceof CatalogDescriptor) {
 						i = 1;
 					} else if (i2.getData() instanceof CatalogDescriptor) {
 						i = -1;
 					} else {
 						// otherwise we sort by name
 						i = i1.getName().compareToIgnoreCase(i2.getName());
 						if (i == 0) {
 							i = i1.getName().compareTo(i2.getName());
 							if (i == 0) {
 								// same name, so we sort by id.
 								i = i1.getId().compareTo(i2.getId());
 							}
 						}
 					}
 				}
 				return i;
 			}
 
 			private CatalogCategory getCategory(Object o) {
 				if (o instanceof CatalogCategory) {
 					return (CatalogCategory) o;
 				}
 				if (o instanceof CatalogItem) {
 					return ((CatalogItem) o).getCategory();
 				}
 				return null;
 			}
 		});
 		return viewer;
 	}
 
 	@Override
 	protected void modifySelection(CatalogItem connector, boolean selected) {
 		// TODO Auto-generated method stub
 		super.modifySelection(connector, selected);
 	}
 
 }
