 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.repository.IRepository;
 import org.zend.sdklib.repository.site.Application;
import org.zend.sdklib.repository.site.CategoryDef;
import org.zend.sdklib.repository.site.ProviderDef;
 import org.zend.sdklib.repository.site.Site;
 
 /**
  * List all targets and their statuses
  */
 public class DiscoverApplicationCommand extends RepositoryAwareCommand {
 
 	private static final String STATUS = "s";
 	private static final String QUERY = "q";
 
 	@Option(opt = STATUS, required = false, description = "show status line for applications")
 	public boolean isStatus() {
 		return hasOption(STATUS);
 	}
 
 	@Option(opt = QUERY, required = false, description = "filters according to a given pattern")
 	public String getQuery() {
 		return getValue(QUERY);
 	}
 
 	@Override
 	public boolean doExecute() {
 		final IRepository[] list = getRepositoryManager().getRepositories();
 		if (list.length == 0) {
 			commandLine.getLog().info("No Available repositories.");
 			return true;
 		}
 
 		for (IRepository r : list) {
 			commandLine.getLog().info("id: " + r.getId());
 
 			try {
 				final Site site = r.getSite();
 				for (Application a : site.getApplication()) {
 
 					// skip if irrelevant
 					if (getQuery() != null && !a.getName().matches(getQuery())) {
 						break;
 					}
 
 					commandLine.getLog().info(
 							"\t" + a.getName() + " (" + a.getVersion() + ")");
 					if (isStatus()) {
 						commandLine.getLog().info(
 								"\t\tFull Name: " + a.getLabel());
 						final Object category = a.getCategory();
 						if (category != null) {
 							commandLine.getLog().info(
									"\t\tCategory: "
											+ ((CategoryDef) category)
													.getLabel());
 						}
 
 						final Object provider = a.getProvider();
 						if (provider != null) {
							commandLine.getLog().info(
									"\tProvider: "
											+ ((ProviderDef) provider)
													.getLabel());
 						}
 						commandLine.getLog().info(
 								"\tUpdate Range: " + a.getUpdateRange());
 
 					}
 				}
 			} catch (SdkException e) {
 				getLogger().error(e);
 			}
 		}
 
 		return true;
 	}
 }
