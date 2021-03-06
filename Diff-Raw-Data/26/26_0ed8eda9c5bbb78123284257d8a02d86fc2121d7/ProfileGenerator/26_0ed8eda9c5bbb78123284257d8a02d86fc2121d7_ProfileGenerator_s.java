 package com.petpet.c3po.analysis;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MapReduceOutput;
 import com.petpet.c3po.analysis.mapreduce.HistogramJob;
 import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
 import com.petpet.c3po.api.dao.PersistenceLayer;
 import com.petpet.c3po.common.Constants;
 import com.petpet.c3po.datamodel.Filter;
 import com.petpet.c3po.datamodel.MetadataRecord;
 import com.petpet.c3po.datamodel.MetadataRecord.Status;
 import com.petpet.c3po.datamodel.Property;
 import com.petpet.c3po.datamodel.Property.PropertyType;
 import com.petpet.c3po.utils.DataHelper;
 
 public class ProfileGenerator {
 
   private static final Logger LOG = LoggerFactory.getLogger(ProfileGenerator.class);
 
   private static final String[] PROPERTIES = { "format", "format_version", "puid", "mimetype", "charset", "linebreak",
       "compressionscheme", "creating_os", "byteorder", "compression_scheme", "colorspace", "icc_profile_name",
       "icc_profile_version", "created", "creating.application.name" };
 
   private PersistenceLayer persistence;
 
   public ProfileGenerator(final PersistenceLayer persistence) {
     this.persistence = persistence;
   }
 
   public void write(final String xml) {
     try {
       final Document doc = DocumentHelper.parseText(xml);
       this.write(doc);
 
     } catch (final DocumentException e) {
       e.printStackTrace();
     }
   }
 
   public void write(final Document doc) {
     this.write(doc, "profiles/output.xml");
   }
 
   public void write(final Document doc, final String path) {
     try {
       final OutputFormat format = OutputFormat.createPrettyPrint();
       final File file = new File(path);
 
       LOG.info("Will create profile in {}", file.getAbsolutePath());
 
       if (file.getParentFile() != null && !file.getParentFile().exists()) {
         file.getParentFile().mkdirs();
       }
 
       file.createNewFile();
 
       final XMLWriter writer = new XMLWriter(new FileWriter(path), format);
       writer.write(doc);
       writer.close();
 
     } catch (final IOException e) {
       e.printStackTrace();
     }
   }
 
   public Document generateProfile(Filter filter) {
     final BasicDBObject ref = new BasicDBObject();
     ref.put("collection", filter.getCollection());
     final long count = this.persistence.count(Constants.TBL_ELEMENTS, ref);
 
     final Document document = DocumentHelper.createDocument();
 
     final Element root = this.createRootElement(document, filter.getCollection(), count);
     final Element partition = this.createPartition(root, filter);
     this.genereateFilterElement(partition, filter);
     final Element properties = this.createPropertiesElement(partition);
     this.generateProperties(filter, properties);
     this.createSamples(filter, partition);
     this.createElements(filter, partition);
 
     return document;
   }
 
   private void genereateFilterElement(Element partition, Filter filter) {
     Element elmntFilter = partition.addElement("filter");
     elmntFilter.addAttribute("id", filter.getDescriminator());
     BasicDBObject query = DataHelper.getFilterQuery(filter);
     Element parameters = elmntFilter.addElement("parameters");
     for (String key : query.keySet()) {
       Element parameter = parameters.addElement("parameter");
       parameter.addElement("name").addText(key);
       parameter.addElement("value").addText(query.getString(key));
     }
 
   }
 
   // TODO serialize the filter better, not with the temp id.
   private Element createPartition(Element root, Filter filter) {
     BasicDBObject query = DataHelper.getFilterQuery(filter);
 
     DBCursor cursor = this.persistence.find(Constants.TBL_ELEMENTS, query);
 
     final Element partition = root.addElement("partition").addAttribute("count", cursor.count() + "");
     return partition;
   }
 
   private void generateProperties(final Filter filter, final Element properties) {
     final List<Property> allprops = this.getProperties(this.persistence.findAll(Constants.TBL_PROEPRTIES));
     // final BasicDBObject query = new BasicDBObject("_id", null);
 
     for (Property p : allprops) {
       final BasicDBObject ref = DataHelper.getFilterQuery(filter);
       // if it is already in the query do not overwrite
       if (!ref.containsField("metadata." + p.getId() + ".value")) {
         ref.put("metadata." + p.getId() + ".value", new BasicDBObject("$exists", true));
       }
       final int count = this.persistence.find(Constants.TBL_ELEMENTS, ref).count();
 
       if (count != 0) {
         this.createPropertyElement(filter, properties, p, count);
       }
     }
   }
 
   private void createPropertyElement(final Filter filter, final Element properties, final Property p, int count) {
     final Element prop = properties.addElement("property").addAttribute("id", p.getKey())
         .addAttribute("type", p.getType()).addAttribute("count", count + "");
 
     final PropertyType type = PropertyType.valueOf(p.getType());
 
     switch (type) {
       case STRING:
         this.processStringProperty(filter, prop, p);
         break;
       case BOOL:
         this.processBoolProperty(filter, prop, p);
         break;
       case INTEGER:
       case FLOAT:
         this.processNumericProperty(filter, prop, p);
         break;
       case DATE:
         this.processDateProperty(filter, prop, p);
         break;
     }
   }
 
   private Element createRootElement(final Document doc, final String collection, final long count) {
     final Element profile = doc.addElement("profile", "http://ifs.tuwien.ac.at/dp/c3po")
         .addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance").addAttribute("collection", collection)
         .addAttribute("date", new Date() + "").addAttribute("count", count + "");
 
     return profile;
   }
 
   private Element createPropertiesElement(final Element partition) {
     return partition.addElement("properties");
   }
 
   private void createSamples(final Filter filter, final Element partition) {
     final RepresentativeGenerator sg = new SizeRepresentativeGenerator();
     final Element samples = partition.addElement("samples");
     samples.addAttribute("type", sg.getType());
     sg.setFilter(filter);
     final List<String> output = sg.execute(5);
 
     for (String s : output) {
       createSampleElement(samples, s);
     }
   }
 
   private void createSampleElement(final Element samples, final String uid) {
     DBCursor cursor = this.persistence.find(Constants.TBL_ELEMENTS, new BasicDBObject("uid", uid));
     assert cursor.count() == 1;
 
     com.petpet.c3po.datamodel.Element element = DataHelper.parseElement(cursor.next(), this.persistence);
 
 
     Element sample = samples.addElement("sample").addAttribute("uid", uid);
     for (MetadataRecord mr : element.getMetadata()) {
      
       if (mr.getStatus().equals(Status.CONFLICT.toString())) {
         for (int i = 0; i < mr.getValues().size(); i++) {
           sample.addElement("record").addAttribute("name", mr.getProperty().getKey())
              .addAttribute("value", mr.getValues().get(i)).addAttribute("tool", mr.getSources().get(i));
         }
 
       } else {
         sample.addElement("record").addAttribute("name", mr.getProperty().getKey())
            .addAttribute("value", mr.getValue()).addAttribute("tool", mr.getSources().get(0));
       }
     }
   }
 
   private void createElements(final Filter filter, final Element partition) {
     final Element elements = partition.addElement("elements");
 
     final BasicDBObject ref = DataHelper.getFilterQuery(filter);
     final BasicDBObject keys = new BasicDBObject("_id", null);
     keys.put("uid", 1);
 
     final DBCursor cursor = this.persistence.find(Constants.TBL_ELEMENTS, ref, keys);
 
     while (cursor.hasNext()) {
       final DBObject element = cursor.next();
       elements.addElement("element").addAttribute("uid", (String) element.get("uid"));
     }
 
   }
 
   private void processStringProperty(final Filter filter, final Element prop, final Property p) {
     for (final String s : PROPERTIES) {
       if (p.getKey().equals(s)) {
 
         HistogramJob job = new HistogramJob(filter.getCollection(), p.getKey());
         job.setFilterquery(DataHelper.getFilterQuery(filter));
 
         final MapReduceOutput output = job.execute();
         final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
 
         Collections.sort(results, new Comparator<BasicDBObject>() {
 
           @Override
           public int compare(BasicDBObject o1, BasicDBObject o2) {
             final String key = "value";
             final Long l1 = o1.getLong(key);
             final Long l2 = o2.getLong(key);
             return l2.compareTo(l1); // from largest to smallest.
           }
 
         });
 
         for (final BasicDBObject dbo : results) {
           prop.addElement("item").addAttribute("id", dbo.getString("_id"))
               .addAttribute("value", dbo.getLong("value") + "");
         }
 
         break;
       }
     }
   }
 
   private void processBoolProperty(final Filter filter, final Element prop, final Property p) {
     final BasicDBObject query = new BasicDBObject("_id", null);
     final BasicDBObject ref = DataHelper.getFilterQuery(filter);
     final String key = "metadata." + p.getId() + ".value";
     int yes = 0;
     int no = 0;
 
     // when it equals uknown remove the elment.
     // TODO when it equals conflicted.
     if (ref.get(key) != null && ref.get(key).toString().equals("{ \"$exists\" : false}")) {
       prop.getParent().remove(prop);
       return;
 
     } else if (ref.get(key) != null) {
       if (ref.getBoolean(key)) {
         yes = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();
       } else {
         no = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();
       }
     } else {
       ref.put(key, true);
       yes = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();
 
       ref.put(key, false);
       no = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();
 
     }
 
     prop.addElement("item").addAttribute("value", "true").addAttribute("count", yes + "");
     prop.addElement("item").addAttribute("value", "false").addAttribute("count", no + "");
 
   }
 
   // if also a histogram is done, do not forget the bin_width...
   private void processNumericProperty(final Filter filter, final Element prop, final Property p) {
     final NumericAggregationJob job = new NumericAggregationJob(filter.getCollection(), p.getId());
     final BasicDBObject query = DataHelper.getFilterQuery(filter);
     job.setFilterquery(query);
     final MapReduceOutput output = job.execute();
 
     final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
     final BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");
 
     prop.addAttribute("count", removeTrailingZero(aggregation.getString("count")));
     prop.addAttribute("sum", removeTrailingZero(aggregation.getString("sum")));
     prop.addAttribute("min", removeTrailingZero(aggregation.getString("min")));
     prop.addAttribute("max", removeTrailingZero(aggregation.getString("max")));
     prop.addAttribute("avg", removeTrailingZero(aggregation.getString("avg")));
     prop.addAttribute("var", removeTrailingZero(aggregation.getString("variance")));
     prop.addAttribute("sd", removeTrailingZero(aggregation.getString("stddev")));
   }
 
   private void processDateProperty(Filter filter, Element prop, Property p) {
     final HistogramJob job = new HistogramJob(filter.getCollection(), p.getKey());
     job.setFilterquery(DataHelper.getFilterQuery(filter));
 
     MapReduceOutput output = job.execute();
     List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
     for (BasicDBObject obj : results) {
       String id = removeTrailingZero(obj.getString("_id"));
       String val = removeTrailingZero(obj.getString("value"));
       prop.addElement("item").addAttribute("id", id).addAttribute("value", val);
     }
   }
 
   private String removeTrailingZero(final String str) {
     if (str != null && str.endsWith(".0")) {
       return str.substring(0, str.length() - 2);
     }
 
     return str;
   }
 
   // TODO find a better place for this and remove it from here and CSVGenerator
   /**
    * Extracts {@link Property} objects from the given cursor and only sets the
    * id and the name field.
    * 
    * @param cursor
    *          the cursor to look for property objects.
    * @return a list of properties or an empty list.
    */
   private List<Property> getProperties(final DBCursor cursor) {
     final List<Property> result = new ArrayList<Property>();
 
     while (cursor.hasNext()) {
       final DBObject next = cursor.next();
 
       final String id = (String) next.get("_id");
       final String name = (String) next.get("key");
       final String type = (String) next.get("type");
 
       if (id != null && name != null) {
         final Property p = new Property();
         p.setId(id);
         p.setKey(name);
         p.setType(type);
 
         result.add(p);
       }
     }
 
     return result;
   }
 
 }
