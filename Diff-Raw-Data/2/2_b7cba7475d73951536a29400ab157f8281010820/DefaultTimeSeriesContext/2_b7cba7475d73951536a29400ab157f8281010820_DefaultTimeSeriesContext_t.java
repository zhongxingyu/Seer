 /**
  * Copyright (C) 2009 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.context.impl;
 
 import com.od.jtimeseries.capture.Capture;
 import com.od.jtimeseries.capture.CaptureFactory;
 import com.od.jtimeseries.capture.function.CaptureFunction;
 import com.od.jtimeseries.capture.impl.DefaultCaptureFactory;
 import com.od.jtimeseries.context.*;
 import com.od.jtimeseries.scheduling.DefaultScheduler;
 import com.od.jtimeseries.scheduling.Scheduler;
 import com.od.jtimeseries.scheduling.Triggerable;
 import com.od.jtimeseries.source.ValueSource;
 import com.od.jtimeseries.source.ValueSourceFactory;
 import com.od.jtimeseries.source.impl.DefaultValueSourceFactory;
 import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
 import com.od.jtimeseries.timeseries.TimeSeriesFactory;
 import com.od.jtimeseries.timeseries.impl.DefaultTimeSeriesFactory;
 import com.od.jtimeseries.util.JTimeSeriesConstants;
 import com.od.jtimeseries.util.identifiable.Identifiable;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 18-Dec-2008
  * Time: 10:17:44
  */
 public class DefaultTimeSeriesContext extends LockingTimeSeriesContext {
 
     private final List<Identifiable> children = Collections.synchronizedList(new ArrayList<Identifiable>());
     private final Map<String, Identifiable> childrenById = Collections.synchronizedMap(new HashMap<String, Identifiable>());
     private ContextQueries contextQueries = new DefaultContextQueries(this);
     private DefaultMetricCreator defaultMetricCreator = new DefaultMetricCreator(this);
 
     /**
      * Create a context without a parent (i.e. a root context), using a default id and description, which has its own
      * scheduler and factories for series, value sources, captures and child contexts, and sets default context properties.
      * Child contexts generally inherit these from the root context, although any set up at a child level override the parent.
      */
     public DefaultTimeSeriesContext() {
         this(null, JTimeSeriesConstants.DEFAULT_ROOT_CONTEXT_ID, JTimeSeriesConstants.DEFAULT_ROOT_CONTEXT_ID);
     }
 
     /**
      * Create a context without a parent (i.e. a root context), which has its own scheduler and factories for
      * series, value sources, captures and child contexts, and sets default context properties.
      * Child contexts generally inherit these from the root context, although any set up at a child level override the parent.
      */
     public DefaultTimeSeriesContext(String id, String description) {
         this(null, id, description);
     }
 
     /**
      * Create a context which is a child of the parentContext supplied
      * @param parentContext parentContext or null, if null then a root context is created
      */
     public DefaultTimeSeriesContext(TimeSeriesContext parentContext, String id, String description) {
         super(parentContext, id, description);
         checkId(id);
         if ( parentContext == null) {
             createRootContextResources();
         }
     }
 
     private void createRootContextResources() {
         setScheduler(new DefaultScheduler());
         setTimeSeriesFactory(new DefaultTimeSeriesFactory());
         setValueSourceFactory(new DefaultValueSourceFactory());
         setCaptureFactory(new DefaultCaptureFactory());
         setContextFactory(new DefaultContextFactory());
         setDefaultContextProperties();
     }
 
     protected <E extends Identifiable> E getFromAncestors_Locked(String id, Class<E> classType) {
         E result = get(id, classType);
         if (result == null && ! isRoot()) {
             result = getParent().getFromAncestors(id, classType);
         }
         return result;
     }
 
     private void setDefaultContextProperties() {
         setProperty(ContextProperties.START_CAPTURES_IMMEDIATELY_PROPERTY, "true");
     }
 
     protected <E extends Identifiable> List<E> getChildren_Locked(Class<E> classType) {
         List<E> list = new ArrayList<E>();
         for ( Identifiable i : children) {
             if ( classType.isAssignableFrom(i.getClass())) {
                 list.add((E)i);
             }
         }
         return list;
     }
 
     protected List<Identifiable> getChildren_Locked() {
         List<Identifiable> children = new ArrayList<Identifiable>();
         children.addAll(this.children);
         return children;
     }
 
     protected TimeSeriesContext addChild_Locked(Identifiable... identifiables) {
         checkIdCharacters(identifiables);
         for ( Identifiable i : identifiables) {
             if ( i instanceof Scheduler) {
                 doSetScheduler((Scheduler)i);
             }
             if ( i instanceof ValueSourceFactory) {
                 remove(ValueSourceFactory.ID);
             }
             if ( i instanceof ContextFactory) {
                 remove(ContextFactory.ID);
             }
             if ( i instanceof CaptureFactory) {
                 remove(CaptureFactory.ID);
             }
             if ( i instanceof TimeSeriesFactory) {
                 remove(TimeSeriesFactory.ID);
             }
             if ( i instanceof Triggerable ) {
                 getScheduler().addTriggerable((Triggerable)i);
             }
             if ( i instanceof Capture ) {
                 if (Boolean.parseBoolean(findProperty(ContextProperties.START_CAPTURES_IMMEDIATELY_PROPERTY))) {
                     ((Capture)i).start();
                 }
             }
             checkUniqueIdAndAdd(i);
         }
         return this;
     }
 
     protected boolean removeChild_Locked(Identifiable e) {
         boolean removed = children.remove(e);
         if ( removed) {
             e.setParent(null);
             childrenById.remove(e.getId());
         }
         return removed;
     }
 
    protected <E extends Identifiable> E remove_Locked(String path, Class<E> classType) {
         PathParser p = new PathParser(path);
         E result;
         if (p.isSingleNode()) {
             result = get(path, classType);
             if ( result != null ) {
                 removeChild(result);
             }
         } else {
             String childContext = p.removeFirstNode();
             TimeSeriesContext c = getContext(childContext);
             result = c == null ? null : c.remove(p.getRemainingPath(), classType);
         }
         return result;
     }
 
     private void doSetScheduler(Scheduler scheduler) {
         if ( isSchedulerStarted() ) {
             throw new UnsupportedOperationException("Cannot setScheduler while the existing scheduler is running");
         } else {
             Scheduler oldSchedulerForThisContext = remove(Scheduler.ID, Scheduler.class);
             if (oldSchedulerForThisContext == null) {
                 oldSchedulerForThisContext = getScheduler();  //this context was using the parent scheduler
             }
             moveTriggerablesFromOldToNewScheduler(oldSchedulerForThisContext, scheduler);
         }
     }
 
     //If any capture from this context in the hierarchy downwards is assoicated with the old scheduler, remove it
     //and add it to the new scheduler
     private void moveTriggerablesFromOldToNewScheduler(Scheduler oldScheduler, Scheduler newScheduler) {
         for ( Triggerable t : findAll(Triggerable.class).getAllMatches()) {
             if ( oldScheduler != null && oldScheduler.containsTriggerable(t)) {
                 oldScheduler.removeTriggerable(t);
                 newScheduler.addTriggerable(t);
             }
         }
     }
 
     protected <E extends Identifiable> E get_Locked(String path, Class<E> classType) {
         PathParser p = new PathParser(path);
         Identifiable result;
         if (p.isEmpty()) {
             result = this;
         } else if (p.isSingleNode()) {
             result = childrenById.get(path);
         } else {
             String childContext = p.removeFirstNode();
             TimeSeriesContext c = getContext(childContext);
             result = c == null ? null : c.get(p.getRemainingPath(), classType);
         }
         if ( result != null && ! classType.isAssignableFrom(result.getClass())) {
             throw new WrongClassTypeException("Cannot convert identifiable " + result.getPath() + " from clss " + result.getClass() + " to " + classType);
         }
         return (E)result;
     }
 
     protected boolean containsChildWithId_Locked(String id) {
         return childrenById.containsKey(id);
     }
 
     protected boolean containsChild_Locked(Identifiable child) {
         return children.contains(child);
     }
 
     protected boolean isSchedulerStarted_Locked() {
         //can be null only if the root context during construction
         return getScheduler() != null && getScheduler().isStarted();
     }
 
     protected TimeSeriesContext startScheduling_Locked() {
         for (Scheduler s : findAllSchedulers().getAllMatches()) {
             s.start();
         }
         return this;
     }
 
     protected TimeSeriesContext stopScheduling_Locked() {
         for (Scheduler s : findAllSchedulers().getAllMatches()) {
             s.stop();
         }
         return this;
     }
 
     protected TimeSeriesContext startDataCapture_Locked() {
         List<Capture> allCaptures = findAllCaptures().getAllMatches();
         for ( Capture c : allCaptures) {
             c.start();
         }
         return this;
     }
 
     protected TimeSeriesContext stopDataCapture_Locked() {
         List<Capture> allCaptures = findAllCaptures().getAllMatches();
         for ( Capture c : allCaptures) {
             c.stop();
         }
         return this;
     }
 
     protected <E extends Identifiable> E create_Locked(String path, String description, Class<E> clazz, Object... parameters) {
         PathParser p = new PathParser(path);
         if ( p.isSingleNode() || p.isEmpty() ) {
             E s = get(path, clazz);
             if ( s == null) {
                 s = doCreate(path, description, clazz, parameters);
                 addChild(s);
             }
             return s;
         } else {
             //create the next context in the path, and recusively call create
             String nextContext = p.removeFirstNode();
             TimeSeriesContext c = create(nextContext, nextContext, TimeSeriesContext.class);
             return c.create(p.getRemainingPath(), description, clazz);
         }
     }
 
     private <E extends Identifiable> E doCreate(String id, String description, Class<E> classType, Object... parameters) {
         E result;
         if ( TimeSeriesContext.class.isAssignableFrom(classType)) {
             result = getContextFactory().createContext(this, id, description, classType, parameters);
         }
         else if ( IdentifiableTimeSeries.class.isAssignableFrom(classType)) {
             result = getTimeSeriesFactory().createTimeSeries(this, getPathForChild(id), id, description, classType, parameters);
         }
         else if ( ValueSource.class.isAssignableFrom(classType) ) {
             result = createValueSource(id, description, classType, parameters);
         }
         else if ( Capture.class.isAssignableFrom(classType)) {
             result = getCaptureFactory().createCapture(this, getPathForChild(id), id, (ValueSource)parameters[0], (IdentifiableTimeSeries)parameters[1], (CaptureFunction)parameters[2], classType, parameters);
         }
         else {
             throw new UnsupportedOperationException("Cannot create identifiable of class " + classType);
         }
         return result;
     }
 
     //here we have to cater for just creating the individual source, or creating a capture and series as well using MetricCreator,
     //depending on whether there are CaptureFunctions as parameters
     private <E extends Identifiable> E createValueSource(String id, String description, Class<E> classType, Object... parameters) {
         E result;
         if ( parameters.length == 0 || ! (parameters[0] instanceof CaptureFunction)) {
             result = getValueSourceFactory().createValueSource(this, getPathForChild(id), id, description, classType, parameters);
         } else {
             List<CaptureFunction> functions = new ArrayList<CaptureFunction>();
             List<Object> params = new ArrayList<Object>();
             for ( Object o : parameters ) {
                 if ( o instanceof CaptureFunction ) {
                     functions.add((CaptureFunction)o);
                 } else {
                     params.add(o);
                 }
             }
             result = defaultMetricCreator.createValueSourceSeries(this, getPathForChild(id), id, description, classType, functions, params.toArray(new Object[params.size()]));
         }
         return result;
     }
 
     protected QueryResult<IdentifiableTimeSeries> findTimeSeries_Locked(CaptureCriteria criteria) {
         return contextQueries.findTimeSeries(criteria);
     }
 
     protected QueryResult<IdentifiableTimeSeries> findTimeSeries_Locked(ValueSource source) {
         return contextQueries.findTimeSeries(source);
     }
 
     protected QueryResult<IdentifiableTimeSeries> findTimeSeries_Locked(String searchPattern) {
         return contextQueries.findTimeSeries(searchPattern);
     }
 
     protected QueryResult<IdentifiableTimeSeries> findAllTimeSeries_Locked() {
         return contextQueries.findAllTimeSeries();
     }
 
     protected QueryResult<Capture> findCaptures_Locked(String searchPattern) {
         return contextQueries.findCaptures(searchPattern);
     }
 
     protected QueryResult<Capture> findCaptures_Locked(CaptureCriteria criteria) {
         return contextQueries.findCaptures(criteria);
     }
 
     protected QueryResult<Capture> findCaptures_Locked(ValueSource valueSource) {
         return contextQueries.findCaptures(valueSource);
     }
 
     protected QueryResult<Capture> findCaptures_Locked(IdentifiableTimeSeries timeSeries) {
         return contextQueries.findCaptures(timeSeries);
     }
 
     protected QueryResult<Capture> findAllCaptures_Locked() {
         return contextQueries.findAllCaptures();
     }
 
     protected QueryResult<ValueSource> findValueSources_Locked(CaptureCriteria criteria) {
         return contextQueries.findValueSources(criteria);
     }
 
     protected QueryResult<ValueSource> findValueSources_Locked(IdentifiableTimeSeries timeSeries) {
         return contextQueries.findValueSources(timeSeries);
     }
 
     protected QueryResult<ValueSource> findValueSources_Locked(String searchPattern) {
         return contextQueries.findValueSources(searchPattern);
     }
 
     protected QueryResult<ValueSource> findAllValueSources_Locked() {
         return contextQueries.findAllValueSources();
     }
 
     protected QueryResult<Scheduler> findAllSchedulers_Locked() {
         return contextQueries.findAllSchedulers();
     }
 
     protected QueryResult<Scheduler> findSchedulers_Locked(String searchPattern) {
         return contextQueries.findSchedulers(searchPattern);
     }
 
     protected QueryResult<Scheduler> findSchedulers_Locked(Triggerable triggerable) {
         return contextQueries.findSchedulers(triggerable);
     }
 
     protected <E> QueryResult<E> findAllChildren_Locked(Class<E> assignableToClass) {
         return contextQueries.findAll(assignableToClass);
     }
 
     protected <E> QueryResult<E> findAllChildren_Locked(String searchPattern, Class<E> assignableToClass) {
         return contextQueries.findAll(searchPattern, assignableToClass);
     }
 
     public String toString() {
         return "Context " + getId();
     }
 
     private <E extends Identifiable> void checkUniqueIdAndAdd(E identifiable) {
         if (childrenById.containsKey(identifiable.getId())) {
             throw new DuplicateIdException("id " + identifiable.getId() + " already exists in this context");
         } else {
             children.add(identifiable);
             childrenById.put(identifiable.getId(), identifiable);
         }
         identifiable.setParent(this);
     }
 
 }
