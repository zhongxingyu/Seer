 /*
  * Demoiselle Framework
  * Copyright (C) 2010 SERPRO
  * ----------------------------------------------------------------------------
  * This file is part of Demoiselle Framework.
  * 
  * Demoiselle Framework is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License version 3
  * as published by the Free Software Foundation.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License version 3
  * along with this program; if not,  see <http://www.gnu.org/licenses/>
  * or write to the Free Software Foundation, Inc., 51 Franklin Street,
  * Fifth Floor, Boston, MA  02110-1301, USA.
  * ----------------------------------------------------------------------------
  * Este arquivo é parte do Framework Demoiselle.
  * 
  * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
  * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
  * do Software Livre (FSF).
  * 
  * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
  * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
  * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
  * para maiores detalhes.
  * 
  * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
  * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
  * ou escreva para a Fundação do Software Livre (FSF) Inc.,
  * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
  */
 package br.gov.frameworkdemoiselle.template.contrib;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.inject.Inject;
 
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import br.gov.frameworkdemoiselle.menu.core.MenuContext;
 import br.gov.frameworkdemoiselle.query.contrib.QueryConfig;
 import br.gov.frameworkdemoiselle.query.contrib.QueryContext;
 import br.gov.frameworkdemoiselle.template.AbstractPageBean;
 import br.gov.frameworkdemoiselle.template.ListPageBean;
 import br.gov.frameworkdemoiselle.util.Reflections;
 import br.gov.frameworkdemoiselle.util.contrib.Faces;
 
 public abstract class AbstractListPageBean<T, I> extends AbstractPageBean implements ListPageBean<T, I> {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Generic's Integration;
 	 */
 	private Class<T> beanClass;
 
 	protected Class<T> getBeanClass() {
 		if (this.beanClass == null)
 			this.beanClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
 		return this.beanClass;
 	}
 
 	/**
 	 * Cache DataModel;
 	 */
 	private transient DataModel<T> dataModel;
 
 	@Override
 	public DataModel<T> getDataModel() {
 		if (this.dataModel == null)
 			this.dataModel = new ListDataModel<T>(this.getResultList());
 		return this.dataModel;
 	}
 
 	/**
 	 * Cache Result List;
 	 */
 	protected abstract List<T> handleResultList(QueryConfig<T> queryConfig);
 
 	private List<T> resultList;
 
 	private Integer resultSize = 0;
 
 	private Long resultTime = 0L;
 
 	@Override
 	public List<T> getResultList() {
 		if (this.resultList == null) {
 			Calendar handleStart = new GregorianCalendar();
 			this.resultList = handleResultList(getQueryConfig());
 			Calendar handleStop = new GregorianCalendar();
			resultTime = (handleStart.getTimeInMillis() - handleStop.getTimeInMillis());
 
 		}
 		if (this.resultList != null)
 			this.resultSize = this.resultList.size();
 		return this.resultList;
 	}
 
 	public Integer getResultSize() {
 		return this.resultSize;
 	}
 
	public Long getResultTime() {
		return this.resultTime;
 	}
 
 	/**
 	 * Clean Result List and DataModel;
 	 */
 	@Override
 	public String list() {
 		this.dataModel = null;
 		this.resultList = null;
 		resultSize = 0;
 		resultTime = 0L;
 		getQueryConfig().setTotalResults(0);
 		return null;
 	}
 
 	/**
 	 * QueryConfig Integration;
 	 */
 	@Inject
 	private QueryContext queryContext;
 
 	public QueryConfig<T> getQueryConfig() {
 		return queryContext.getQueryConfig(getBeanClass(), true);
 	}
 
 	/**
 	 * LazyDataModel with QueryConfig Integration;
 	 */
 	private LazyDataModel<T> lazyDataModel = new LazyDataModel<T>() {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public List<T> load(int first, int pageSize, String sortAttribute, SortOrder sortOrder, Map<String, String> filters) {
 			QueryConfig<T> queryConfig = getQueryConfig();
 			queryConfig.setPagination(first, pageSize);
 			queryConfig.setSorting(sortAttribute);
 			queryConfig.setSortOrder(sortOrder.equals(SortOrder.ASCENDING));
 			queryConfig.setFilterStr(filters);
 
 			Calendar handleStart = new GregorianCalendar();
 			List<T> t = handleResultList(queryConfig);
 			Calendar handleStop = new GregorianCalendar();
			resultTime = (handleStart.getTimeInMillis() - handleStop.getTimeInMillis());
 
 			resultSize = queryConfig.getTotalResults();
 			this.setRowCount(resultSize);
 
 			return t;
 		}
 
 	};
 
 	public LazyDataModel<T> getLazyDataModel() {
 		return lazyDataModel;
 	}
 
 	public void setLazyDataModel(LazyDataModel<T> lazyDataModel) {
 		this.lazyDataModel = lazyDataModel;
 	}
 
 	/**
 	 * MenuContext Integration;
 	 */
 	@Inject
 	private MenuContext menuContext;
 
 	public MenuContext getMenuContext() {
 		return menuContext;
 	}
 
 	public String getSelectedMenu() {
 		return menuContext.getSelected(getBeanClass().getSimpleName());
 	}
 
 	public void selectMenu(String itemName) {
 		menuContext.select(getBeanClass().getSimpleName(), itemName);
 	}
 
 	/**
 	 * Simple filter attribute
 	 */
 	private String resultFilter;
 
 	public String getResultFilter() {
 		return resultFilter;
 	}
 
 	public void setResultFilter(String resultFilter) {
 		if (resultFilter == null || resultFilter.trim().isEmpty())
 			resultFilter = null;
 
 		if (resultFilter == null || !resultFilter.equals(this.resultFilter))
 			list();
 
 		this.resultFilter = resultFilter;
 	}
 
 	public void clearResultFilter() {
 		resultFilter = null;
 		list();
 	}
 
 	/**
 	 * Improve selection from ListPageBean.class;
 	 */
 	private boolean selectionAll = false;
 
 	private Map<I, Boolean> selection = new SelectionMap<I>(this.selectionAll);
 
 	public boolean isSelectionAll() {
 		return selectionAll;
 	}
 
 	public void setSelectionAll(boolean selectionAll) {
 		this.selectionAll = selectionAll;
 		this.selection = new SelectionMap<I>(this.selectionAll, this.selection);
 	}
 
 	public void setSelection(Map<I, Boolean> selection) {
 		this.selection = selection;
 	}
 
 	public Map<I, Boolean> getSelection() {
 		return selection;
 	}
 
 	public void selectionToggle() {
 		for (Entry<I, Boolean> entry : selection.entrySet())
 			entry.setValue(selectionAll);
 	}
 
 	public void initSelection() {
 		selection = new SelectionMap<I>(this.selectionAll);
 	}
 
 	public List<I> getSelectedList() {
 		List<I> selectedList = new ArrayList<I>();
 		Iterator<I> iter = getSelection().keySet().iterator();
 		while (iter.hasNext()) {
 			I id = iter.next();
 			if (getSelection().get(id))
 				selectedList.add(id);
 		}
 		return selectedList;
 	}
 
 	/**
 	 * Deprecated
 	 */
 	public String clearValidation() {
 		Faces.resetValidation();
 		list();
 		return null;
 	}
 
 }
 
 class SelectionMap<I> extends HashMap<I, Boolean> {
 
 	private static final long serialVersionUID = 1L;
 
 	private boolean defaultvalue;
 
 	public SelectionMap(boolean defaultValue) {
 		this.defaultvalue = defaultValue;
 	}
 
 	public SelectionMap(boolean defaultValue, Map<I, Boolean> map) {
 		this.defaultvalue = defaultValue;
 		this.putAll(map);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Boolean get(Object id) {
 		Boolean value = super.get(id);
 		if (value == null) {
 			this.put((I) id, defaultvalue);
 			return defaultvalue;
 		}
 		return value;
 	}
 
 }
