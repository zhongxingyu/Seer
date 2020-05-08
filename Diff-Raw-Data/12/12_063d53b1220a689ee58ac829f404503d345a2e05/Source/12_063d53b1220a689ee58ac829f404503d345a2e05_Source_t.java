 package com.github.dbourdette.otto.source;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 
 import com.github.dbourdette.otto.Registry;
 import com.github.dbourdette.otto.data.DataTablePeriod;
 import com.github.dbourdette.otto.data.SimpleDataTable;
 import com.github.dbourdette.otto.data.filler.OperationChain;
 import com.github.dbourdette.otto.source.config.AggregationConfig;
 import com.github.dbourdette.otto.source.config.DefaultGraphParameters;
 import com.github.dbourdette.otto.source.config.TransformConfig;
 import com.github.dbourdette.otto.source.reports.ReportConfig;
 import com.github.dbourdette.otto.util.Page;
 import com.github.dbourdette.otto.web.exception.SourceAlreadyExists;
 import com.github.dbourdette.otto.web.exception.SourceNotFound;
 import com.github.dbourdette.otto.web.form.CappingForm;
 import com.github.dbourdette.otto.web.form.IndexForm;
 import com.github.dbourdette.otto.web.form.Sort;
 import com.github.dbourdette.otto.web.form.SourceForm;
 import com.github.dbourdette.otto.web.util.Constants;
 import com.github.dbourdette.otto.web.util.Frequency;
 import com.github.dbourdette.otto.web.util.SizeInBytes;
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.Property;
 import com.google.code.morphia.query.Query;
 import com.mongodb.BasicDBObject;
 import com.mongodb.CommandResult;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 /**
  * @author damien bourdette
  * @version \$Revision$
  */
 @Entity(value = Constants.SOURCES, noClassnameStored = true)
 public class Source {
     private static final int DEFAULT_PAGE_SIZE = 100;
 
     @Id
     private ObjectId id;
 
     @NotEmpty
     @Property
     private String name;
 
     @Property
     private String displayName;
 
     @Property
     private String displayGroup;
 
     @Embedded
     private AggregationConfig aggregationConfig;
 
     @Embedded
     private DefaultGraphParameters defaultGraphParameters = new DefaultGraphParameters();
 
     @Embedded
     private TransformConfig transformConfig = new TransformConfig();
 
     public static Source create(String name) {
         assertNotExists(name);
 
         Source source = new Source();
         source.name = name;
 
         Registry.datastore.save(source);
 
         return source;
     }
 
     public static Source create(SourceForm form) {
         assertNotExists(form.getName());
 
         createEventsCollection(form);
 
         Source source = new Source();
         source.name = form.getName().trim();
         source.displayGroup = form.getDisplayGroup().trim();
         source.displayName = form.getDisplayName().trim();
 
         Registry.datastore.save(source);
 
         return source;
     }
 
     public static List<Source> findAll() {
         return Registry.datastore.find(Source.class).order("name").asList();
     }
 
     public static boolean exists(String name) {
         return queryByName(name).countAll() > 0;
     }
 
     public static Source findByName(String name) {
         assertExists(name);
 
         return queryByName(name).get();
     }
 
     public void save() {
         Registry.datastore.save(this);
     }
 
     public void delete() {
         assertExists(name);
 
         getEventsDBCollection().drop();
 
         Registry.datastore.delete(queryByName(name));
     }
 
     public SimpleDataTable buildTable(ReportConfig config, DataTablePeriod period) {
         return buildTable(config, period.getInterval(), period.getStepDuration());
     }
 
     public SimpleDataTable buildTable(ReportConfig config, Interval interval, Duration step) {
         SimpleDataTable table = new SimpleDataTable();
 
         table.setupRows(interval, step);
 
         OperationChain chain = config.buildChain(table);
 
         Iterator<DBObject> events = findEvents(interval);
 
         while (events.hasNext()) {
             DBObject event = events.next();
 
             chain.write(event);
         }
 
         if (table.getColumnCount() == 0) {
             table.ensureColumnExists("no data");
         }
 
         if (config.getSort() == Sort.ALPHABETICALLY) {
             table.sortAlphabetically();
         } else if (config.getSort() == Sort.BY_SUM) {
             table.sortBySum();
         }
 
         return table;
     }
 
     public void post(Event event) {
         transformConfig.applyOn(event);
 
        if (aggregationConfig != null && aggregationConfig.isAggregating()) {
            event.setDate(aggregationConfig.getTimeFrame().roundDate(event.getDate()));
 
             BasicDBObject inc = new BasicDBObject();
            inc.put("$inc", new BasicDBObject(aggregationConfig.getAttributeName(), 1));
 
             getEventsDBCollection().update(event.toDBObject(), inc, true, false);
         } else {
             getEventsDBCollection().insert(event.toDBObject());
         }
     }
 
     public Iterator<DBObject> findEvents(Interval interval) {
         EventsQuery query = new EventsQuery(interval);
 
         return query.createCursor(getEventsDBCollection()).iterator();
     }
 
     public Page<DBObject> findEvents(Integer page) {
         EventsQuery query = new EventsQuery();
         query.setPage(page);
         query.setPageSize(DEFAULT_PAGE_SIZE);
 
         return findEvents(query);
     }
 
     public Page<DBObject> findEvents(EventsQuery query) {
         return query.findPage(getEventsDBCollection());
     }
 
     public void clearEvents() {
         getEventsDBCollection().remove(new BasicDBObject());
     }
 
     public long getCount() {
         return getEventsDBCollection().count();
     }
 
     public String getCollectionName() {
         return getEventsDBCollection().getName();
     }
 
     public CommandResult getStats() {
         return getEventsDBCollection().getStats();
     }
 
     public Frequency findEventsFrequency(Interval interval) {
         BasicDBObject query = new EventsQuery(interval).createQuery();
 
         int count = 0;
 
        if (aggregationConfig != null && aggregationConfig.isAggregating()) {
             String attName = aggregationConfig.getAttributeName();
 
             BasicDBObject fields = new BasicDBObject(attName, "1");
 
             for (DBObject object : getEventsDBCollection().find(query, fields)) {
                 Object value = object.get(attName);
 
                 if (value instanceof Integer) {
                     count += (Integer) value;
                 } else {
                     count += 1;
                 }
             }
         } else {
             count = (int) getEventsDBCollection().count(query);
         }
 
         return new Frequency(count, interval.toDuration());
     }
 
     public List<DBObject> getIndexes() {
         return getEventsDBCollection().getIndexInfo();
     }
 
     public void cap(CappingForm form) {
         SizeInBytes sizeInBytes = form.getSizeInBytes();
 
         if (sizeInBytes == null) {
             return;
         }
 
         BasicDBObject capping = new BasicDBObject();
 
         capping.put("convertToCapped", getEventsCollectionName(name));
         capping.put("capped", true);
         capping.put("size", sizeInBytes.getValue());
 
         Registry.mongoDb.command(capping);
     }
 
     public void createIndex(IndexForm form) {
         BasicDBObject keys = new BasicDBObject(form.getKey(), form.isAscending() ? 1 : -1);
 
         String name = form.getKey() + (form.isAscending() ? "_1" : "_-1");
 
         BasicDBObject options = new BasicDBObject("name", name);
 
         if (form.isBackground()) {
             options.put("background", "1");
         }
 
         getEventsDBCollection().ensureIndex(keys, options);
     }
 
     public void dropIndex(String index) {
         getEventsDBCollection().dropIndex(index);
     }
 
     public boolean isCapped() {
         return getEventsDBCollection().isCapped();
     }
 
     public SizeInBytes getSize() {
         return new SizeInBytes(getEventsDBCollection().getStats().getLong("storageSize"));
     }
 
     public Long getMax() {
         return getEventsDBCollection().getStats().getLong("max");
     }
 
     public String getName() {
         return name;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
     public String getDisplayGroup() {
         return displayGroup;
     }
 
     public void setDisplayGroup(String displayGroup) {
         this.displayGroup = displayGroup;
     }
 
     public AggregationConfig getAggregationConfig() {
         return aggregationConfig;
     }
 
     public void setAggregationConfig(AggregationConfig aggregationConfig) {
         this.aggregationConfig = aggregationConfig;
     }
 
     public DefaultGraphParameters getDefaultGraphParameters() {
         return defaultGraphParameters;
     }
 
     public void setDefaultGraphParameters(DefaultGraphParameters defaultGraphParameters) {
         this.defaultGraphParameters = defaultGraphParameters;
     }
 
     private DBCollection getEventsDBCollection() {
         return Source.getEventsDBCollection(name);
     }
 
     public TransformConfig getTransformConfig() {
         return transformConfig;
     }
 
     public void setTransformConfig(TransformConfig transformConfig) {
         this.transformConfig = transformConfig;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Source source = (Source) o;
 
         if (name != null ? !name.equals(source.name) : source.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return name != null ? name.hashCode() : 0;
     }
 
     private static void createEventsCollection(SourceForm form) {
         BasicDBObject capping = new BasicDBObject();
 
         SizeInBytes sizeInBytes = form.getSizeInBytes();
 
         if (sizeInBytes == null) {
             capping.put("capped", false);
         } else {
             capping.put("capped", true);
             capping.put("size", sizeInBytes.getValue());
 
             if (form.getMaxEvents() != null) {
                 capping.put("max", form.getMaxEvents());
             }
         }
 
         String collectionName = getEventsCollectionName(form.getName());
 
         Registry.mongoDb.createCollection(collectionName, capping);
     }
 
     private static DBCollection getEventsDBCollection(String name) {
         return Registry.mongoDb.getCollection(getEventsCollectionName(name));
     }
 
     private static String getEventsCollectionName(String name) {
         return Constants.SOURCES_ROOT + name + Constants.EVENTS;
     }
 
     private static void assertExists(String name) {
         if (!exists(name)) {
             throw new SourceNotFound();
         }
     }
 
     private static void assertNotExists(String name) {
         if (exists(name)) {
             throw new SourceAlreadyExists();
         }
     }
 
     private static Query<Source> queryByName(String name) {
         return Registry.datastore.find(Source.class).filter("name", name);
     }
 }
