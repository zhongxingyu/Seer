 package org.paxle.core.filter.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.filter.IFilterManager;
 import org.paxle.core.filter.IFilterQueue;
 
 public class FilterManager implements IFilterManager, MetaTypeProvider, ManagedService {
 	public static final String PID = IFilterManager.class.getName();
 	
 	/* =======================================================================
 	 * CONFIGURATION MANAGEMENT PROPERTIES
 	 * @see #ManagedService
 	 * ======================================================================= */
 	private static final String CM_FILTER_DEFAULT_STATUS = PID + ".filterDefaultStatus";
 	private static final String CM_FILTER_ENABLED_FILTERS =  PID + ".enabledFilters.";
 	
 	/* =======================================================================
 	 * PREFERENCE PROPERTIES 
 	 * @see #props
 	 * ======================================================================= */
 	private static final String PROPS_KNOWN_FILTERCONTEXTS = PID + ".knownFilterContexts";
 	
 	/**
 	 * A map containing a sorted list of {@link FilterContext filters} for each {@link IFilterQueue targer}-id 
 	 */
 	private HashMap<String, SortedSet<FilterContext>> filters = new HashMap<String, SortedSet<FilterContext>>();
 	
 	/**
 	 * A datastructure to map a IFilter target-id to a given {@link IFilterQueue}
 	 */
 	private HashMap<String, IFilterQueue> queues = new HashMap<String, IFilterQueue>();
 
 	/**
 	 * A datastructure holding information about the currently enabled Filters.
 	 * The key of this map is the filterqueue-ID, the value is a list of filtercontext-PIDs that are known
 	 * to be enabled.
 	 */
 	private Map<String, Set<String>> enabledFilterContexts = new HashMap<String, Set<String>>();
 	
 	/**
 	 * A set containing the PID of all currently known {@link IFilterContext filterContexts}
 	 */
 	private HashSet<String> knownFilterContexts = new HashSet<String>();
 	
 	/**
 	 * for logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 			
 	/**
 	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	private String[] locales;	
 	
 	/**
 	 * Object to store component properties
 	 */
 	private Properties props;
 	
 	/**
 	 * Specifies if newly installed {@link IFilterContext filters} should be enabled per default
 	 */
 	private Boolean enableNewFilters = Boolean.TRUE;
 	
 	/**
 	 * the CM {@link Configuration} applied to this {@link ManagedService}
 	 */
 	private final Configuration config;
 	
 	/**
 	 * @param config the CM configuration that belongs to this component
 	 * @throws IOException 
 	 * @throws IOException
 	 * @throws ConfigurationException 
 	 * @throws ConfigurationException
 	 */
 	public FilterManager(String[] locales, Configuration config, Properties props) throws IOException, ConfigurationException {
 		if (locales == null) throw new NullPointerException("The locale array is null");
 		if (config == null) throw new NullPointerException("The configuration object is null");
 		if (props == null) throw new NullPointerException("The property object is null");
 		
 		this.locales = locales;
 		this.config = config;
 		this.props = props;
 		
 		if (props.containsKey(PROPS_KNOWN_FILTERCONTEXTS)) {
 			String filtersStr = (String)props.get(PROPS_KNOWN_FILTERCONTEXTS);
 			if (filtersStr.length() > 2) {			
 				String[] filterContextPIDs = filtersStr.substring(1, filtersStr.length()-1).split(",");
 				for (String filterContextPID : filterContextPIDs) {
 					this.knownFilterContexts.add(filterContextPID.trim());
 				}
 			}
 		}
 		
 		// initialize CM values
 		if (config.getProperties() == null) {
 			config.update(this.getCMDefaults());
 		}
 		
 		// update configuration of this component
 		this.updated(config.getProperties());		
 	}
 	
 	private Dictionary<String, Object> getCMDefaults() {
 		Hashtable<String, Object> props = new Hashtable<String, Object>();
 		props.put(CM_FILTER_DEFAULT_STATUS, Boolean.TRUE);
 		return props;
 	}
 	
 	
 	public void close() {
 		// stopre pref. properties
 		this.props.put(PROPS_KNOWN_FILTERCONTEXTS, this.knownFilterContexts.toString());
 	}
 	
 	/**
 	 * @param queueID the unique {@link IFilterQueue queue} ID
 	 * @return all {@link FilterContext filters} that were registered for the given {@link IFilterQueue queue}
 	 */
 	private Set<FilterContext> getRegisteredFilters(String queueID) {
 		SortedSet<FilterContext> filterList = null;
 		if (this.filters.containsKey(queueID)) {
 			filterList = this.filters.get(queueID);
 		} else {
 			filterList = new TreeSet<FilterContext>();
 			this.filters.put(queueID, filterList);
 		}
 		return filterList;
 	}
 	
 	/**
 	 * @param queueID the unique {@link IFilterQueue queue} ID
 	 * @return the PID of all {@link FilterContext}s enabled for the given {@link IFilterQueue queue}
 	 */
 	private Set<String> getEnabledFilters(String queueID) {
 		Set<String> enabledFilters = null;
 		if (this.enabledFilterContexts.containsKey(queueID)) {
 			enabledFilters = this.enabledFilterContexts.get(queueID);
 		} else {
 			enabledFilters = new HashSet<String>();
 			this.enabledFilterContexts.put(queueID, enabledFilters);
 		}
 		return enabledFilters;
 	}
 	
 	/**
 	 * @param queueID the unique {@link IFilterQueue queue} ID
 	 * @return a previously registered {@link IFilterQueue} specified by the given ID
 	 */
 	private IFilterQueue getQueue(String queueID) {
 		return this.queues.get(queueID);
 	}
 	
 	/**
 	 * Registering a new {@link IFilter}
 	 * @param filterContext the context-object for the {@link IFilter}
 	 */
 	@SuppressWarnings("unchecked")
 	public synchronized void addFilter(FilterContext filterContext) {
 		if (filterContext == null) throw new NullPointerException("The filtercontext is null");
 		
 		final String queueID = filterContext.getTargetID();
 		final String filterPID = filterContext.getFilterPID();
 		final String filterContextPID = filterContext.getFilterContextPID();		
 		try {
 			/* 
 			 * Getting the registered-filter-list of the target
 			 * and add the new filter to it
 			 */
 			final Set<FilterContext> knownFilters = this.getRegisteredFilters(queueID);
 			knownFilters.add(filterContext);
 
 			/* 
 			 * getting the enabled-filter-list
 			 * and add the new filter-to it (if newly installed)
 			 */
 			final Set<String> enabledFilters = this.getEnabledFilters(queueID);
 
 			// check if we have detected a newly installed filter
 			if (!this.knownFilterContexts.contains(filterContextPID)) {
 				this.knownFilterContexts.add(filterContextPID);
 				
 				/* If newly installed filters should be enabled per default and
 				 * the filter-provider has not disabled the filter per default
 				 * we enable the filter now
 				 */
 				if (this.enableNewFilters.booleanValue() && filterContext.isEnabled()) {
 					enabledFilters.add(filterContextPID);
 
 					// updating CM if needed
 					Dictionary<String,Object> props = this.config.getProperties();
 					if (props.get(CM_FILTER_ENABLED_FILTERS + queueID) != null) {
 						// we need to update the list
 						props.put(CM_FILTER_ENABLED_FILTERS + queueID, enabledFilters.toArray(new String[enabledFilters.size()]));					
 						this.config.update(props);
 					}
 				} else {
 					filterContext.setEnabled(false);
 				}
			} else if (props.get(CM_FILTER_ENABLED_FILTERS + queueID) != null) {
 				boolean enabled = enabledFilters.contains(filterContextPID);
 				filterContext.setEnabled(enabled);
 			}
 
 			// if the queue is already registered pass the filters to it
 			if (this.queues.containsKey(queueID)) {
 				/*
 				 * passing the changed filter-list to the filter-queue
 				 */
 				IFilterQueue queue = this.getQueue(queueID);
 				this.setFilters(queue, knownFilters, enabledFilters);
 			}		
 		} catch (Throwable e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while registering a new filter '%s' for queue '%s'.",
 					e.getClass().getName(),
 					filterPID,
 					queueID
 			));
 		}
 	}
 	
 	/**
 	 * Unregistering a {@link IFilter}
 	 * @param serviceID the {@link Constants#SERVICE_ID} of the {@link IFilter}
 	 * @param targetID the property {@link IFilter#PROP_FILTER_TARGET} wich was used by the
 	 * {@link IFilter} during service-registration.
 	 */
 	public synchronized void removeFilter(Long serviceID, String targetID) {
 		if (serviceID == null) throw new NullPointerException("The serviceID must not be null.");
 		if (targetID == null) throw new NullPointerException("The targetID must not be null.");
 		
 		int idx = targetID.indexOf(";");
 		if (idx != -1) {
 			targetID = targetID.substring(0,idx).trim();			
 		}
 		
 		if (!this.filters.containsKey(targetID)) {
 			this.logger.warn(String.format(
 					"Unable to find a filter-queue with targetID '%s'. Unable to remove filter with service-id '%s'.",
 					targetID,
 					serviceID.toString()
 			));
 			return;
 		}
 		
 		// get the queue from which the filter should be removed 
 		final Set<FilterContext> knownFilters = this.getRegisteredFilters(targetID);
 		
 		// getting the list of enabled-filters
 		final Set<String> enabledFilters = this.getEnabledFilters(targetID);
 		
 		// find the desired filter
 		Iterator<FilterContext> filterIter = knownFilters.iterator();
 		while (filterIter.hasNext()) {
 			FilterContext next = filterIter.next();
 			if (next.getServiceID().equals(serviceID) /* TODO: && next.getFilterPosition() == position */) {				
 				// remove it from the list
 				filterIter.remove();
 				
 				// free resources
 				next.close();
 				
 				this.logger.info(String.format(
 						"Filter '%s' with service-id '%s' successfully removed from target '%s'.",
 						next.getFilter().getClass().getName(),
 						serviceID.toString(),
 						targetID
 				));
 				break;
 			}
 		}
 		
 		// re-set the modified filter-list to the queue
 		if (this.queues.containsKey(targetID)) {
 			IFilterQueue queue = this.queues.get(targetID);
 			this.setFilters(queue, knownFilters, enabledFilters);
 		}	
 	}
 	
 	public synchronized void addFilterQueue(String queueID, IFilterQueue queue) {
 		if (queueID == null) throw new NullPointerException("The queueID is null.");
 		if (queue == null) throw new NullPointerException("The queue is null");
 		
 		// remember queue
 		this.queues.put(queueID, queue);
 		
 		// getting the filter-list registered for this queue
 		Set<FilterContext> registeredFilters = this.getRegisteredFilters(queueID);
 
 		// getting all filters known to be enabled for this filter
 		Set<String> enabledFilters = this.getEnabledFilters(queueID);
 
 		// applie the filters to the queue
 		this.setFilters(queue, registeredFilters, enabledFilters);
 	}
 	
 	/**
 	 * Function to unregister a {@link IFilterQueue}
 	 * @param queueID the ID of the {@link IFilterQueue}, which should be unregistered
 	 * 
 	 * TODO: should we shutdown the queue here?
 	 */	
 	@SuppressWarnings("unchecked")
 	public synchronized void removeFilterQueue(String queueID) {
 		if (this.queues.containsKey(queueID)) {
 			// remove the queue from the list
 			IFilterQueue queue = this.queues.remove(queueID);	
 			queue.setFilters(new ArrayList<IFilterContext>(0));			
 			this.logger.info(String.format("FilterQueue with id '%s' successfully removed.",queueID));			
 		}
 	}
 	
 	private void setFilters(IFilterQueue queue, Set<FilterContext> knownFilters, Set<String> enabledFilters) {
 		// sort filters
 		ArrayList<IFilterContext> temp = new ArrayList<IFilterContext>();
 		
 		if (knownFilters != null) {
 			for (FilterContext filter : knownFilters) {
 				// getting the unqiue filter-PID + position
 				String filterContextPID = filter.getFilterContextPID();
 				
 				// if enabled apply the filter to the queue
 				if (enabledFilters != null && enabledFilters.contains(filterContextPID)) {
 					temp.add(filter); 
 				}
 			}
 		}
 		
 		// pass it to the queue
 		queue.setFilters(temp);
 	}
 	
 	/**
 	 * @see IFilterManager#getFilters(String)
 	 */
 	public Set<FilterContext> getFilters(String queueID) {
 		if (queueID == null) throw new NullPointerException("QueueID is null");
 		
 		if (!this.filters.containsKey(queueID)) return Collections.emptySet();		
 		return Collections.unmodifiableSet(this.filters.get(queueID));
 	}
 
 	/**
 	 * @see IFilterManager#hasFilters(String)
 	 */
 	public boolean hasFilters(String queueID) {
 		if (queueID == null) throw new NullPointerException("QueueID is null");
 		return this.filters.containsKey(queueID);
 	}
 	
 	/**
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	public String[] getLocales() {
 		return this.locales;
 	}
 	
 	private String[] getFilterContextPIDs(SortedSet<FilterContext> filtersForTarget, boolean inclDisabled) {
 		ArrayList<String> filterContextPIDs = new ArrayList<String>(filtersForTarget.size());
 		
 		for (FilterContext fContext : filtersForTarget) {
 			if (inclDisabled || fContext.isEnabled()) {
 				filterContextPIDs.add(fContext.getFilterContextPID());
 			}
 		}
 		
 		return filterContextPIDs.toArray(new String[filterContextPIDs.size()]);
 	}
 
 	private String[] getFilterNames(SortedSet<FilterContext> filtersForTarget, boolean inclDisabled) {
 		ArrayList<String> filterNames = new ArrayList<String>(filtersForTarget.size());
 		
 		for (FilterContext fContext : filtersForTarget) {
 			// XXX: we have no real filter-name at the moment
 			filterNames.add(fContext.getFilter().getClass().getName());
 		}
 		
 		return filterNames.toArray(new String[filterNames.size()]);
 	}
 	
 	/**
 	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
 	 */
 	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
 		final Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
 		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + IFilterManager.class.getSimpleName(), locale);			
 		
 		return new ObjectClassDefinition() {
 			public AttributeDefinition[] getAttributeDefinitions(int filter) {
 				ArrayList<AttributeDefinition> attribs = new ArrayList<AttributeDefinition>();
 				
 				// default filter activation status
 				attribs.add(new AttributeDefinition(){						
 					public String getID() { return CM_FILTER_DEFAULT_STATUS; }										
 					public int getCardinality() { return 0; }
 					public String[] getDefaultValue() { return new String[]{Boolean.TRUE.toString()}; }
 					public String getDescription() { return rb.getString("filterDefaultStatus.desc"); }
 					public String getName() { return rb.getString("filterDefaultStatus.name"); }
 					public String[] getOptionLabels() { return null; }
 					public String[] getOptionValues() { return null; }
 					public int getType() { return AttributeDefinition.BOOLEAN; }
 					public String validate(String value) { return null; }
 				});
 				
 				// current filter activation status
 				for (Map.Entry<String, SortedSet<FilterContext>> entry : filters.entrySet()) {
 					final String queueID = entry.getKey();
 					final SortedSet<FilterContext> filtersForTarget = entry.getValue();
 					if (filtersForTarget.size() == 0) continue;
 					
 					attribs.add(new AttributeDefinition(){						
 							public String getID() {
 								return CM_FILTER_ENABLED_FILTERS + queueID;
 							}						
 						
 							public int getCardinality() {
 								return filtersForTarget.size();
 							}
 	
 							public String[] getDefaultValue() {
 								return getFilterContextPIDs(filtersForTarget, false);
 							}
 	
 							public String getDescription() {
 								return MessageFormat.format(rb.getString("filterListParam.desc"),queueID);
 							}
 	
 							public String getName() {
 								return MessageFormat.format(rb.getString("filterListParam.name"),queueID);
 							}
 	
 							public String[] getOptionLabels() {
 								return getFilterNames(filtersForTarget, true);
 							}
 	
 							public String[] getOptionValues() {
 								return getFilterContextPIDs(filtersForTarget, true);
 							}
 	
 							public int getType() {
 								return AttributeDefinition.STRING;
 							}
 	
 							public String validate(String value) {
 								return null;
 							}						
 						}	
 					);
 				
 				}
 				
 				return attribs.toArray(new AttributeDefinition[attribs.size()]);
 			}
 
 			public String getDescription() {
 				return rb.getString("filterManager.desc");
 			}
 
 			public String getID() {
 				return PID;
 			}
 
 			public InputStream getIcon(int size) throws IOException {
 				return null;
 			}
 
 			public String getName() {				
 				return rb.getString("filterManager.name");
 			}			
 		};
 	}
 
 	/**
 	 * @see ManagedService#updated(Dictionary)
 	 */
 	@SuppressWarnings("unchecked")
 	public synchronized void updated(Dictionary properties) throws ConfigurationException {
 		if (properties == null) return;
 		
 		// getting default filter statis
 		Boolean defaultStatus = (Boolean) properties.get(CM_FILTER_DEFAULT_STATUS);
 		if (defaultStatus != null) this.enableNewFilters = defaultStatus;
 		
 		// getting filter-status settings
 		Enumeration<String> keys = properties.keys();
 		while (keys.hasMoreElements()) {
 			String key = keys.nextElement();
 			if (!key.startsWith(CM_FILTER_ENABLED_FILTERS)) continue;
 			
 			String[] enabledFiltes = (String[]) properties.get(key);
 			if (enabledFiltes != null) {
 				String queueID =  key.substring(CM_FILTER_ENABLED_FILTERS.length());
 				
 				// remember enabled filters
 				Set<String> enabledFilters = this.getEnabledFilters(queueID);
 				enabledFilters.clear();
 				enabledFilters.addAll(Arrays.asList(enabledFiltes));
 				
 				// getting all filters registered for the queue
 				 Set<FilterContext> knownFilters = this.getRegisteredFilters(queueID);
 				 
 				 // updating filter-context status
 				 for (FilterContext fContext : knownFilters) {
 					 boolean enabled = enabledFilters.contains(fContext.getFilterContextPID());
 					 fContext.setEnabled(enabled);
 				 }
 				
 				// getting the queue
 				IFilterQueue queue = this.getQueue(queueID);
 				 
 				if (this.queues.containsKey(queueID)) {
 					// apply filters to the queue
 					this.setFilters(queue, knownFilters, enabledFilters);
 				}
 			}
 		}
 	}
 }
