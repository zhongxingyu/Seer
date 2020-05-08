 package com.amee.restlet.profile.builder.v2;
 
 import com.amee.domain.APIVersion;
 import com.amee.domain.data.DataItem;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.path.PathItem;
 import com.amee.restlet.profile.ProfileCategoryResource;
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.model.*;
 import org.apache.abdera.parser.Parser;
 import org.apache.abdera.writer.Writer;
 import org.apache.abdera.writer.WriterFactory;
 import org.restlet.data.MediaType;
 
 import javax.xml.namespace.QName;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class AtomFeed {
 
    private static final String AMEE_SCHEMA = "http://schemas.amee.com/2.0";
     private static final String AMEE_ITEM_SCHEME = AMEE_SCHEMA + "#item";
     private static final String AMEE_ITEM_VALUE_SCHEME = AMEE_SCHEMA + "#itemValue";
 
     private static final String PREFIX = "amee";
     public static final QName Q_NAME_START_DATE = new QName(AMEE_SCHEMA, "startDate", PREFIX);
     public static final QName Q_NAME_END_DATE = new QName(AMEE_SCHEMA, "endDate", PREFIX);
     public static final QName Q_NAME_DURATION = new QName(AMEE_SCHEMA, "duration", PREFIX);
     public static final QName Q_NAME_TOTAL_AMOUNT = new QName(AMEE_SCHEMA, "totalAmount", PREFIX);
     public static final QName Q_NAME_AMOUNT = new QName(AMEE_SCHEMA, "amount", PREFIX);
     public static final QName Q_NAME_ITEM_VALUE = new QName(AMEE_SCHEMA, "itemValue", PREFIX);
     public static final QName Q_NAME_NAME = new QName(AMEE_SCHEMA, "name", PREFIX);
     public static final QName Q_NAME_VALUE = new QName(AMEE_SCHEMA, "value", PREFIX);
     public static final QName Q_NAME_UNIT = new QName(AMEE_SCHEMA, "unit", PREFIX);
     public static final QName Q_NAME_PER_UNIT = new QName(AMEE_SCHEMA, "perUnit", PREFIX);
     public static final QName Q_NAME_DATA_ITEM = new QName(AMEE_SCHEMA, "dataItem", PREFIX);
     public static final QName Q_CATEGORIES = new QName(AMEE_SCHEMA, "categories", PREFIX);
     public static final QName Q_CATEGORY = new QName(AMEE_SCHEMA, "category", PREFIX);
 
     private static final SimpleDateFormat ATOM_DATE_DISPLAY_FMT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
 
     private static final String AMEE_LANG = "en-US";
 
     WriterFactory writerFactory;
     private Factory factory;
     private Parser parser;
 
     private static final AtomFeed instance = new AtomFeed();
 
     private AtomFeed() {
         Abdera abdera = new Abdera();
         factory = abdera.getFactory();
         writerFactory = abdera.getWriterFactory();
         parser = abdera.getParser();
     }
 
     public static AtomFeed getInstance() {
         return instance;
     }
 
     public <T extends Element> Document<T> parse(InputStream is) {
         return parser.parse(is);
     }
 
     public String format(Date date) {
         return ATOM_DATE_DISPLAY_FMT.format(date);
     }
 
     public Feed newFeed() {
         Feed feed = factory.newFeed();
         feed.setLanguage(AMEE_LANG);
         return feed;
     }
 
     public Entry newEntry() {
         Entry entry = factory.newEntry();
         entry.setLanguage(AMEE_LANG);
         return entry;
     }
 
     public Writer getWriter() {
         return writerFactory.getWriter("prettyxml");
     }
 
     public IRIElement newID(Element e) {
         return factory.newID(e);
     }
 
     public Link newLink(Element e) {
         return factory.newLink(e);
     }
 
     public Person newAuthor(Element e) {
         return factory.newAuthor(e);
     }
 
     public Text newTitle(Element e) {
         return factory.newTitle(e);
     }
 
     public Text newSubtitle(Element e) {
         return factory.newSubtitle(e);
     }
 
     public Category newCategory(Element e) {
         return factory.newCategory(e);
     }
 
     public Category newItemCategory(Element e) {
         Category category = factory.newCategory(e);
         category.setScheme(AMEE_ITEM_SCHEME);
         return category;
     }
 
     public Category newItemValueCategory(Element e) {
         Category category = factory.newCategory(e);
         category.setScheme(AMEE_ITEM_VALUE_SCHEME);
         return category;
     }
 
     public Service newService() {
         Service service = factory.newService();
         service.setLanguage(AMEE_LANG);
         return service;
     }
 
     public Workspace newWorkspace(Service service) {
         return factory.newWorkspace(service);
     }
 
     public Collection newCollection(Workspace workspace) {
         return factory.newCollection(workspace);
     }
 
     public Categories newItemCategories(Collection collection) {
         Categories categories = factory.newCategories(collection);
         categories.setScheme(AMEE_ITEM_SCHEME);
         return categories;
     }
 
     public void addGenerator(Feed feed, APIVersion version) {
         Generator generator = factory.newGenerator(feed);
         generator.setVersion(version.toString());
         generator.setUri("http://www.amee.cc");
         generator.setText("AMEE");
     }
 
     public void addStartDate(ExtensibleElement e, String date) {
         e.addSimpleExtension(Q_NAME_START_DATE, date);
     }
 
     public void addEndDate(ExtensibleElement e, String date) {
         e.addSimpleExtension(Q_NAME_END_DATE, date);
     }
 
     public void addName(ExtensibleElement e, String name) {
         e.addSimpleExtension(Q_NAME_NAME, name);
     }
 
     public void addAmount(ExtensibleElement e, String amount, String unit) {
         Element extension = e.addSimpleExtension(Q_NAME_AMOUNT, amount);
         extension.setAttributeValue("unit", unit);
     }
 
     public void addTotalAmount(ExtensibleElement e, String amount, String unit) {
         Element extension = e.addSimpleExtension(Q_NAME_TOTAL_AMOUNT, amount);
         extension.setAttributeValue("unit", unit);
     }
 
     public void addItemValuesWithLinks(Entry entry, List<ItemValue> itemValues, String parentPath) {
         for (ItemValue itemValue : itemValues) {
             Element extension = entry.addExtension(Q_NAME_ITEM_VALUE);
             addItemValue(extension, itemValue, parentPath);
         }
     }
 
     public void addDataItem(ExtensibleElement e, DataItem dataItem) {
         Element dataItemElement = factory.newElement(Q_NAME_DATA_ITEM);
         dataItemElement.setAttributeValue("uid", dataItem.getUid());
         e.addExtension(dataItemElement);
     }
 
     public void addItemValue(Element element, ItemValue itemValue) {
         addItemValue(element, itemValue, null);
     }
 
     private void addItemValue(Element element, ItemValue itemValue, String parentPath) {
 
         factory.newExtensionElement(Q_NAME_NAME, element).setText(itemValue.getName());
         String value = (itemValue.getValue().isEmpty() ? "N/A" : itemValue.getValue());
         factory.newExtensionElement(Q_NAME_VALUE, element).setText(value);
 
         // A non-Null parent path signifies that the ItemValue should include a link element. This would be the case
         // when an ItemValue is being included within an Item or DataCategory feed.
         if (parentPath != null) {
             Link l = factory.newLink(element);
             if (parentPath.length() > 0)
                 parentPath = parentPath + "/";
             l.setHref(parentPath + itemValue.getPath());
             l.setRel(AMEE_ITEM_VALUE_SCHEME);
         }
 
         if (itemValue.hasUnit())
             factory.newExtensionElement(Q_NAME_UNIT, element).setText(itemValue.getUnit().toString());
         if (itemValue.hasPerUnit())
             factory.newExtensionElement(Q_NAME_PER_UNIT, element).setText(itemValue.getPerUnit().toString());
     }
 
     public void addLinks(Element element, String href) {
         Link link = factory.newLink(element);
         link.setHref(href);
         link.setMimeType(MediaType.APPLICATION_ATOM_XML.toString());
         link.setRel(Link.REL_EDIT);
 
         link = factory.newLink(element);
         link.setHref(href);
         link.setMimeType(MediaType.APPLICATION_JSON.toString());
         link.setRel(Link.REL_ALTERNATE);
 
         link = factory.newLink(element);
         link.setHref(href);
         link.setMimeType(MediaType.APPLICATION_XML.toString());
         link.setRel(Link.REL_ALTERNATE);
 
     }
 
     public void addChildCategories(ExtensibleElement e, ProfileCategoryResource resource) {
         ExtensibleElement categories = e.addExtension(Q_CATEGORIES);
         for (PathItem pi : resource.getChildrenByType("DC")) {
             Element category = factory.newElement(Q_CATEGORY);
             category.setText(pi.getPath());
             category.setAttributeValue("href",pi.getPath());
             categories.addExtension(category);
         }
     }
 }
