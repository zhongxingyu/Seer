 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud.domain.model;
 
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import com.vaadin.data.Item;

 import de.unioninvestment.eai.portal.portlet.crud.domain.container.GenericVaadinContainerEventWrapper;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.BeforeCommitEvent;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.CommitEvent;
 import de.unioninvestment.eai.portal.portlet.crud.domain.exception.ContainerException;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.CustomFilter;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.CustomFilterMatcher;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Filter;
import de.unioninvestment.eai.portal.support.vaadin.container.GenericDelegate;
import de.unioninvestment.eai.portal.support.vaadin.container.GenericItem;
import de.unioninvestment.eai.portal.support.vaadin.container.GenericItemId;
import de.unioninvestment.eai.portal.support.vaadin.container.MetaData;
import de.unioninvestment.eai.portal.support.vaadin.container.TemporaryItemId;
 import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
 
 /**
  * Repräsentation eines auf generischen Daten basierten Vaadin Containers als
  * Backend für die Tabellenansicht.
  * 
  * @author markus.bonsch
  * 
  */
 public class GenericDataContainer extends AbstractDataContainer {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(GenericDataContainer.class);
 
 	private static final long serialVersionUID = 1L;
 	private GenericDelegate delegate;
 	private MetaData metaData;
 
 	/**
 	 * Maps Vaadin Custom filtering to a matcher that gets a
 	 * {@link ContainerRow} instead of a Vaadin {@link Item}.
 	 * 
 	 * @author carsten.mjartan
 	 */
 	private static class CustomFilterMatcherWrapper
 			implements
 			de.unioninvestment.eai.portal.support.vaadin.filter.CustomFilter.Matcher {
 
 		private final GenericDataContainer container;
 		private final CustomFilterMatcher matcher;
 
 		public CustomFilterMatcherWrapper(GenericDataContainer container,
 				CustomFilterMatcher matcher) {
 			this.container = container;
 			this.matcher = matcher;
 		}
 
 		@Override
 		public boolean matches(Object itemId, Item item) {
 			ContainerRow row = container.convertItemToRow(item, false, true);
 			return matcher.matches(row);
 		}
 
 	}
 
 	/**
 	 * @param eventBus
 	 *            für Events
 	 * @param formatPattern
 	 *            Formatinformationen
 	 * @param defaultOrder
 	 * @param filterPolicy
 	 */
 	public GenericDataContainer(EventBus eventBus,
 			Map<String, String> formatPattern,
 			List<ContainerOrder> defaultOrder, FilterPolicy filterPolicy) {
 		super(eventBus, formatPattern, defaultOrder, filterPolicy);
 	}
 
 	@Override
 	public boolean isInsertable() {
 		return metaData.isInsertSupported();
 	}
 
 	@Override
 	public boolean isUpdateable() {
 		return metaData.isUpdateSupported();
 	}
 
 	@Override
 	public boolean isDeleteable() {
 		return metaData.isRemoveSupported();
 	}
 
 	@Override
 	public List<String> getPrimaryKeyColumns() {
 		return new ArrayList<String>(metaData.getPrimaryKeys());
 	}
 
 	@Override
 	public List<String> getColumns() {
 		return new ArrayList<String>(metaData.getColumnNames());
 	}
 	
 	@Override
 	public ContainerClob getCLob(ContainerRowId containerRowId,
 			String columnName) {
 
 		if (clobFields.containsKey(containerRowId)) {
 			if (clobFields.get(containerRowId).containsKey(columnName)) {
 				return clobFields.get(containerRowId).get(columnName);
 			}
 		}
 
 		if (containerRowId.getInternalId() instanceof TemporaryItemId) {
 			ContainerClob clob = new ContainerClob();
 			setCLob(containerRowId, columnName, clob);
 			return clob;
 		}
 
 		ContainerClob clob = new ContainerClob((CharBuffer) container
 				.getContainerProperty(containerRowId, columnName).getValue());
 
 		setCLob(containerRowId, columnName, clob);
 		return clob;
 	}
 
 	@Override
 	public Class<?> getType(String name) {
 		Class<?> type = getVaadinContainer().getType(name);
 		if (type == null) {
 			LOGGER.info(
 					"Could not retrieve type information for column '{}'. Does it exist in the backend?",
 					name);
 			return null;
 		} else if (CharBuffer.class.isAssignableFrom(type)) {
 			return ContainerClob.class;
 		} else if (byte[].class.isAssignableFrom(type)) {
 			return ContainerBlob.class;
 		} else {
 			return type;
 		}
 	}
 
 	@Override
 	public boolean isCLob(String columnName) {
 		return ContainerClob.class.equals(getType(columnName));
 	}
 
 	@Override
 	public boolean isBLob(String columnName) {
 		return ContainerBlob.class.equals(getType(columnName));
 	}
 
 	@Override
 	public ContainerBlob getBLob(ContainerRowId rowId, String columnName) {
 		if (blobFields.containsKey(rowId)) {
 			if (blobFields.get(rowId).containsKey(columnName)) {
 				return blobFields.get(rowId).get(columnName);
 			}
 		}
 
 		if (rowId.getInternalId() instanceof TemporaryItemId) {
 			ContainerBlob blob = new ContainerBlob();
 			setBLob(rowId, columnName, blob);
 			return blob;
 		}
 
 		ContainerBlob blob = new ContainerBlob((byte[]) container
 				.getContainerProperty(rowId, columnName).getValue());
 
 		setBLob(rowId, columnName, blob);
 		return blob;
 	}
 
 	@Override
 	public boolean isBLobEmpty(ContainerRowId rowId, String columnName) {
 		ContainerBlob blob = getBLob(rowId, columnName);
 		if (blob != null)
 			return blob.isEmpty();
 		return true;
 	}
 
 	public void setDelegate(GenericDelegate backend) {
 		this.delegate = backend;
 	}
 
 	@Override
 	protected GenericVaadinContainerEventWrapper createVaadinContainer() {
 
 		metaData = delegate.getMetaData();
 
 		return new GenericVaadinContainerEventWrapper(delegate, this,
 				getOnCreateEventRouter(), getOnInsertEventRouter(),
 				getOnUpdateEventRouter(), getOnDeleteEventRouter());
 	}
 
 	@Override
 	protected GenericVaadinContainerEventWrapper getVaadinContainer() {
 		return (GenericVaadinContainerEventWrapper) super.getVaadinContainer();
 	}
 
 	@Override
 	public void commit() {
 		getBeforeCommitEventRouter().fireEvent(new BeforeCommitEvent(this));
 		if (getVaadinContainer().isModified()) {
 			try {
 				getVaadinContainer().commit();
 			} catch (Exception e) {
 				throw new ContainerException(e);
 			}
 
 			getOnCommitEventRouter().fireEvent(new CommitEvent(this));
 		} else {
 			getVaadinContainer().fireContentsChange();
 		}
 	}
 
 	@Override
 	public void rollback() {
 		try {
 			getVaadinContainer().rollback();
 		} finally {
 			clearAdditionalFields();
 		}
 
 	}
 
 	@Override
 	public void refresh() {
 		getVaadinContainer().refresh();
 		applyDefaultOrder();
 	}
 
 	@Override
 	public ContainerRow convertItemToRow(Item item, boolean transactional,
 			boolean immutable) {
 		GenericContainerRowId id = new GenericContainerRowId(
 				((GenericItem) item).getId(), getPrimaryKeyColumns());
 		return new GenericContainerRow(id, (GenericItem) item, this,
 				transactional, immutable);
 	}
 
 	@Override
 	public ContainerRowId convertInternalRowId(Object s) {
 		final GenericItemId rowId;
 		if (s instanceof GenericItemId) {
 			rowId = (GenericItemId) s;
 		} else if (s instanceof GenericItem) {
 			rowId = ((GenericItem) s).getId();
 		} else {
 			throw new IllegalArgumentException(s.getClass() + " nicht erlaubt.");
 		}
 
 		return new GenericContainerRowId(rowId, getPrimaryKeyColumns());
 	}
 
 	@Override
 	com.vaadin.data.Container.Filter buildVaadinFilter(Filter filter) {
 		if (filter instanceof CustomFilter) {
 			return buildCustomFilter(filter);
 		} else {
 			return super.buildVaadinFilter(filter);
 		}
 	}
 
 	private com.vaadin.data.Container.Filter buildCustomFilter(Filter filter) {
 		CustomFilter customFilter = (CustomFilter) filter;
 		return new de.unioninvestment.eai.portal.support.vaadin.filter.CustomFilter(
 				new CustomFilterMatcherWrapper(this, customFilter.getMatcher()));
 	}
 
 	/**
 	 * Only for Unit Tests
 	 * 
 	 * @param metaData
 	 */
 	void setMetaData(MetaData metaData) {
 		this.metaData = metaData;
 	}
 
 	public GenericDelegate getDelegate() {
 		return delegate;
 	}
 
 }
