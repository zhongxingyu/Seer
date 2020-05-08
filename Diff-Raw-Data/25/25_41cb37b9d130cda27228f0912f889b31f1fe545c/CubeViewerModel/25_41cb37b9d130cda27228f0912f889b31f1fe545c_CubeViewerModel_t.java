 /**
  * 
  */
 package de.xwic.cube.webui.viewer;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import de.xwic.cube.ICube;
 import de.xwic.cube.IDimension;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.IMeasure;
 import de.xwic.cube.IQuery;
 import de.xwic.cube.IValueFormat;
 import de.xwic.cube.Key;
 
 /**
  * Model that controls the CubeViewer.
  * 
  * @author Florian Lippisch
  */
 public class CubeViewerModel implements Serializable {
 
 	private enum EventType { FILTER_UPDATE, CUBE_UPDATED, CELL_SELECTION }; 
 	private ICube cube = null;
 
 	protected IMeasure measure = null;
 	protected List<INavigationProvider> rowProvider = new ArrayList<INavigationProvider>();
 	protected List<INavigationProvider> columnProvider = new ArrayList<INavigationProvider>();
 	
 	protected Map<IDimension, IDimensionElement> filter = new HashMap<IDimension, IDimensionElement>(); 
 	
 	protected Set<String> expandedElements = new HashSet<String>(); 
 	
 	protected IValueFormat valueFormat;
 	protected Key baseKey = null;
 	protected IQuery baseQuery = null;
 	
 	protected List<ICubeViewerModelListener> listeners = new ArrayList<ICubeViewerModelListener>();
 
 	private final Locale locale;
 	
 	/**
 	 * Constructor.
 	 * @param locale
 	 */
 	public CubeViewerModel(Locale locale) {
 		this.locale = locale;
 		
 	}
 	
 	/**
 	 * Add a listener.
 	 * @param listener
 	 */
 	public synchronized void addCubeViewerModelListener(ICubeViewerModelListener listener) {
 		listeners.add(listener);
 	}
 	
 	/**
 	 * Remove a listener.
 	 * @param listener
 	 */
 	public synchronized void removeCubeViewerModelListener(ICubeViewerModelListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * @param filter_update
 	 * @param event
 	 */
 	private void fireEvent(EventType eventType, CubeViewerModelEvent event) {
 		ICubeViewerModelListener[] l = new ICubeViewerModelListener[listeners.size()];
 		l = listeners.toArray(l);
 		for (ICubeViewerModelListener listener : l) {
 			switch (eventType) {
 			case FILTER_UPDATE: 
 				listener.filterUpdated(event);
 				break;
 			case CUBE_UPDATED: 
 				listener.cubeUpdated(event);
 				break;
 			case CELL_SELECTION:
 				listener.cellSelected(event);
 				break;
 			}
 		}
 	}
 	
 	public void notifyCellSelection(String dimKey, String[] args) {
 	
 		Key key = cube.createKey(dimKey);
 		for (IDimensionElement de : filter.values()) {
 			int idx = cube.getDimensionIndex(de.getDimension());
 			if (key.getDimensionElement(idx) instanceof IDimension) {
 				key.setDimensionElement(idx, de);
 			}
 		}
 		
 		fireEvent(EventType.CELL_SELECTION, new CubeViewerModelEvent(this, key, args));
 	}
 	
 	public void notifyCubeUpdated() {
 		fireEvent(EventType.CUBE_UPDATED, new CubeViewerModelEvent(this));
 	}
 	
 	/**
 	 * @return the cube
 	 */
 	public ICube getCube() {
 		return cube;
 	}
 
 	/**
 	 * @param cube the cube to set
 	 */
 	public void setCube(ICube cube) {
 		this.cube = cube;
 		baseKey = cube.createKey("");
 	}
 
 	/**
 	 * @return the measure
 	 */
 	public IMeasure getMeasure() {
 		return measure;
 	}
 
 	/**
 	 * @param measure the measure to set
 	 */
 	public void setMeasure(IMeasure measure) {
 		this.measure = measure;
 		fireEvent(EventType.FILTER_UPDATE, new CubeViewerModelEvent(this));
 		valueFormat = measure.getValueFormatProvider().createValueFormat(locale);
 	}
 
 	/**
 	 * @return the verticals
 	 */
 	public List<INavigationProvider> getRowProvider() {
 		return rowProvider;
 	}
 
 	/**
 	 * @return the horizontals
 	 */
 	public List<INavigationProvider> getColumnProvider() {
 		return columnProvider;
 	}
 
 	/**
 	 * @return
 	 */
 	public Key getTotalKey() {
 		return baseKey.clone();
 	}
 
 
 	/**
 	 * @return the numberFormat
 	 */
 	public IValueFormat getValueFormat() {
 		return valueFormat;
 	}
 
 	/**
 	 * @param navigationProvider
 	 */
 	public void addColumnNavigationProvider(INavigationProvider navigationProvider) {
 		columnProvider.add(navigationProvider);
 	}
 
 	/**
 	 * @param vertical
 	 * @param dimension
 	 */
 	public void addRowNavigationProvider(INavigationProvider navigationProvider) {
 		rowProvider.add(navigationProvider);
 	}
 
 
 	/**
 	 * @return
 	 */
 	public Key createCursor() {
 		// later: inject filter 
 		Key key = baseKey.clone();
 		return key;
 	}
 
 
 	/**
 	 * @param dimVert
 	 * @return
 	 */
 	public int getDimensionIndex(IDimension dimVert) {
 		return cube.getDimensionIndex(dimVert);
 	}
 
 
 	/**
 	 * Returns true if the specified element is expanded.
 	 * @param elementId
 	 * @return
 	 */
 	public boolean isExpanded(String elementId) {
 		return expandedElements.contains(elementId);
 	}
 	
 	/**
 	 * Expand an element.
 	 * @param elementId
 	 */
 	public void expand(String elementId) {
 		expandedElements.add(elementId);
 	}
 
 	/**
 	 * Collapse an element.
 	 * @param elementId
 	 */
 	public void collapse(String elementId) {
 		expandedElements.remove(elementId);
 	}
 
 
 	/**
 	 * @param dimension
 	 */
 	public void applyFilter(IDimensionElement dimensionElement) {
 		filter.put(dimensionElement.getDimension(), dimensionElement);
 		
 		updateBaseKey();
 		fireEvent(EventType.FILTER_UPDATE, new CubeViewerModelEvent(this));
 	}
 
 	/**
 	 * Returns the dimension element that is set for the specified dimension.
 	 * @param dimension
 	 * @return
 	 */
 	public IDimensionElement getFilterDimension(IDimension dimension) {
 		IDimensionElement element = filter.get(dimension);
 		return element == null ? dimension : element;
 	}
 
 	/**
 	 * 
 	 */
 	private void updateBaseKey() {
 		
 		baseKey = cube.createKey("");
 		for (IDimension dim : filter.keySet()) {
 			int idx = cube.getDimensionIndex(dim);
 			baseKey.setDimensionElement(idx, filter.get(dim));
 		}
 		
 	}
 
 	/**
 	 * Expands the element and all its child elements.
 	 * @param dimGEO
 	 */
 	public void expandAll(IDimensionElement de) {
 
 		expand(de.getID());
 		for (IDimensionElement child : de.getDimensionElements()) {
 			expandAll(child);
 		}
 		
 	}
 
 	/**
 	 * @return the baseQuery
 	 */
 	public IQuery getBaseQuery() {
 		return baseQuery;
 	}
 
 	/**
 	 * @param baseQuery the baseQuery to set
 	 */
 	public void setBaseQuery(IQuery baseQuery) {
 		this.baseQuery = baseQuery;
 	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}
 	
 }
