 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 
 /**
  * A mechanism for serializing/deserializing state
  * 
  * @author David Green
  */
 public class SelectionModelStateSerializer {
 
 	private final SelectionModel selectionModel;
 
 	private final MarketplaceCatalog catalog;
 
 	public SelectionModelStateSerializer(MarketplaceCatalog catalog, SelectionModel selectionModel) {
 		this.catalog = catalog;
 		this.selectionModel = selectionModel;
 	}
 
 	public String serialize() {
 		StringBuilder state = new StringBuilder(1024);
 		for (Map.Entry<CatalogItem, Operation> entry : selectionModel.getItemToOperation().entrySet()) {
 			if (entry.getValue() != Operation.NONE) {
 				if (state.length() > 0) {
 					state.append(' ');
 				}
 				Node data = (Node) entry.getKey().getData();
 				state.append(data.getId());
 				state.append('=');
 				state.append(entry.getValue().name());
 			}
 		}
 		return state.toString();
 	}
 
 	public void deserialize(IProgressMonitor monitor, String state) {
 		if (state != null && state.length() > 0) {
 			Map<String, Operation> operationByNodeId = new HashMap<String, Operation>();
 
 			Pattern pattern = Pattern.compile("([^\\s=]+)=(\\S+)"); //$NON-NLS-1$
 			Matcher matcher = pattern.matcher(state);
 			while (matcher.find()) {
 				String nodeId = matcher.group(1);
 				String operationName = matcher.group(2);
 
 				Operation operation = Operation.valueOf(operationName);
 
 				operationByNodeId.put(nodeId, operation);
 			}
 			if (!operationByNodeId.isEmpty()) {
 				catalog.performQuery(monitor, operationByNodeId.keySet());
 			}
 		}
 	}
 }
