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
 package org.eclipse.epp.internal.mpc.ui.commands;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.ui.CatalogRegistry;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCategory;
 import org.eclipse.epp.internal.mpc.ui.wizards.ComboTagFilter;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceCatalogConfiguration;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceFilter;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceWizard;
 import org.eclipse.epp.internal.mpc.ui.wizards.Operation;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
 import org.eclipse.equinox.internal.p2.discovery.model.Tag;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogFilter;
 import org.eclipse.jface.wizard.WizardDialog;
 
 /**
  * @author David Green
  */
 public class MarketplaceWizardCommand extends AbstractHandler implements IHandler {
 
 	private List<CatalogDescriptor> catalogDescriptors;
 
 	private CatalogDescriptor selectedCatalogDescriptor;
 
 	private String wizardState;
 
 	private Map<String, Operation> operationByNodeId;
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		final MarketplaceCatalog catalog = new MarketplaceCatalog();
 
 		catalog.setEnvironment(DiscoveryCore.createEnvironment());
 		catalog.setVerifyUpdateSiteAvailability(false);
 
 		MarketplaceCatalogConfiguration configuration = new MarketplaceCatalogConfiguration();
 
 		if (catalogDescriptors == null || catalogDescriptors.isEmpty()) {
 			configuration.getCatalogDescriptors().addAll(CatalogRegistry.getInstance().getCatalogDescriptors());
 		} else {
 			configuration.getCatalogDescriptors().addAll(catalogDescriptors);
 		}
 		if (selectedCatalogDescriptor != null) {
 			configuration.setCatalogDescriptor(selectedCatalogDescriptor);
 		}
 
 		configuration.getFilters().clear();
 
 		final ComboTagFilter marketFilter = new ComboTagFilter() {
 			@Override
 			public void catalogUpdated(boolean wasCancelled) {
 				List<Tag> choices = new ArrayList<Tag>();
 				for (CatalogCategory category : catalog.getCategories()) {
 					if (category instanceof MarketplaceCategory) {
 						MarketplaceCategory marketplaceCategory = (MarketplaceCategory) category;
 						for (Market market : marketplaceCategory.getMarkets()) {
 							Tag marketTag = new Tag(Market.class, market.getId(), market.getName());
 							marketTag.setData(market);
 							choices.add(marketTag);
 						}
 					}
 				}
 				setChoices(choices);
 			}
 		};
 		marketFilter.setSelectAllOnNoSelection(true);
 		marketFilter.setNoSelectionLabel(Messages.MarketplaceWizardCommand_allMarkets);
 		marketFilter.setTagClassification(Category.class);
 		marketFilter.setChoices(new ArrayList<Tag>());
 
 		final ComboTagFilter marketCategoryTagFilter = new ComboTagFilter() {
 			@Override
 			public void catalogUpdated(boolean wasCancelled) {
 				Set<Tag> newChoices = new HashSet<Tag>();
 				List<Tag> choices = new ArrayList<Tag>();
 				for (CatalogCategory category : catalog.getCategories()) {
 					if (category instanceof MarketplaceCategory) {
 						MarketplaceCategory marketplaceCategory = (MarketplaceCategory) category;
 						for (Market market : marketplaceCategory.getMarkets()) {
 							for (Category marketCategory : market.getCategory()) {
 								Tag categoryTag = new Tag(Category.class, marketCategory.getId(),
 										marketCategory.getName());
 								categoryTag.setData(marketCategory);
 								if (newChoices.add(categoryTag)) {
 									choices.add(categoryTag);
 								}
 							}
 						}
 					}
 				}
 				setChoices(choices);
 			}
 		};
 		marketCategoryTagFilter.setSelectAllOnNoSelection(true);
 		marketCategoryTagFilter.setNoSelectionLabel(Messages.MarketplaceWizardCommand_allCategories);
 		marketCategoryTagFilter.setTagClassification(Category.class);
 		marketCategoryTagFilter.setChoices(new ArrayList<Tag>());
 
 		configuration.getFilters().add(marketFilter);
 		configuration.getFilters().add(marketCategoryTagFilter);
 		configuration.setInitialState(wizardState);
 		if (operationByNodeId != null && !operationByNodeId.isEmpty()) {
 			configuration.setInitialOperationByNodeId(operationByNodeId);
 		}
 
 		for (CatalogFilter filter : configuration.getFilters()) {
 			((MarketplaceFilter) filter).setCatalog(catalog);
 		}
 
 		MarketplaceWizard wizard = new MarketplaceWizard(catalog, configuration);
 
 		wizard.setWindowTitle(Messages.MarketplaceWizardCommand_eclipseMarketplace);
 
 		WizardDialog dialog = new WizardDialog(WorkbenchUtil.getShell(), wizard);
 		dialog.open();
 
 		return null;
 	}
 
 	public void setCatalogDescriptors(List<CatalogDescriptor> catalogDescriptors) {
 		this.catalogDescriptors = catalogDescriptors;
 	}
 
 	public void setSelectedCatalogDescriptor(CatalogDescriptor selectedCatalogDescriptor) {
 		this.selectedCatalogDescriptor = selectedCatalogDescriptor;
 	}
 
 	public void setWizardState(String wizardState) {
 		this.wizardState = wizardState;
 	}
 
 	public void setOperationByNodeId(Map<String, Operation> operationByNodeId) {
 		this.operationByNodeId = operationByNodeId;
 	}
 
 }
