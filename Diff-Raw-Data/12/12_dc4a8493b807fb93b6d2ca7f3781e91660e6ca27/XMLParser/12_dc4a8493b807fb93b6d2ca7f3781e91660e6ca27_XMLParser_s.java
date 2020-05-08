 package net.jonp.sorm.codegen.parser;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.jonp.sorm.codegen.LinkMode;
 import net.jonp.sorm.codegen.SQLType;
 import net.jonp.sorm.codegen.model.Field;
 import net.jonp.sorm.codegen.model.FieldGetter;
 import net.jonp.sorm.codegen.model.FieldLink;
 import net.jonp.sorm.codegen.model.FieldLinkCollection;
 import net.jonp.sorm.codegen.model.FieldSetter;
 import net.jonp.sorm.codegen.model.NamedQuery;
 import net.jonp.sorm.codegen.model.Query;
 import net.jonp.sorm.codegen.model.QueryParam;
 import net.jonp.sorm.codegen.model.Sorm;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 
 /**
  * A class to read XML data and generate a {@link Sorm} description.
  */
 public class XMLParser
     implements Parser
 {
     private static final Logger LOG = Logger.getLogger(XMLParser.class);
 
     public XMLParser()
     {
         // Nothing to do
     }
 
     @Override
     public Sorm parseSorm(final Reader in)
         throws BadInputException, IOException
     {
         final Document doc = readConfiguration(in);
         final Namespace ns = Namespace.getNamespace("http://jonp.net/sorm");
         final Element eRoot = doc.getRootElement();
 
         final Sorm sorm = new Sorm();
         sorm.setPkg(eRoot.getAttributeValue("pkg", sorm.getPkg()));
         sorm.setAccessor(eRoot.getAttributeValue("accessor", sorm.getAccessor()));
         sorm.setName(eRoot.getAttributeValue("name", sorm.getName()));
         sorm.setOrm_accessor(eRoot.getAttributeValue("orm_accessor", sorm.getOrm_accessor()));
         sorm.setSuper(eRoot.getAttributeValue("super", sorm.getSuper()));
 
         sorm.getFields().addAll(readFields(eRoot, ns));
         readQuery(eRoot, ns, sorm.getCreate(), "create", "c");
         readQuery(eRoot, ns, sorm.getPk(), "pk", "pk");
         readQuery(eRoot, ns, sorm.getRead(), "read", "r");
         readQuery(eRoot, ns, sorm.getUpdate(), "update", "u");
         readQuery(eRoot, ns, sorm.getDelete(), "delete", "d");
         sorm.getQueries().addAll(readNamedQueries(eRoot, ns));
 
         return sorm;
     }
 
     private Document readConfiguration(final Reader in)
         throws BadInputException, IOException
     {
         final SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
         builder.setFeature("http://apache.org/xml/features/validation/schema", true);
         builder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", getClass().getClassLoader()
             .getResource("sorm.xsd").toString());
         final Document doc;
         try {
             doc = builder.build(in);
         }
         catch (final JDOMException jdome) {
             throw new BadInputException("Error parsing input data: " + jdome.getMessage(), jdome);
         }
 
         return doc;
     }
 
     private List<Field> readFields(final Element eRoot, final Namespace ns)
         throws BadInputException
     {
         final List<Field> fields = new LinkedList<Field>();
         for (final Object o : eRoot.getChildren("field", ns)) {
             final Element eField = (Element)o;
             final Field field = new Field();
             field.setAccessor(eField.getAttributeValue("accessor", field.getAccessor()));
             field.setType(eField.getAttributeValue("type", field.getType()));
             field.setName(eField.getAttributeValue("name", field.getName()));
             field.setPrimary(checkBoolean(eField.getAttributeValue("primary"), field.isPrimary()));
             field.setSql_type(findSQLType(eField.getAttributeValue("sql-type"), field.getSql_type()));
             field.setSql_column(eField.getAttributeValue("sql-column", field.getSql_column()));
             field.setNullable(checkBoolean(eField.getAttributeValue("nullable"), field.isNullable()));
             field.setFromSuper(checkBoolean(eField.getAttributeValue("from-super"), field.isFromSuper()));
             field.setGroup(checkBoolean(eField.getAttributeValue("group"), field.isGroup()));
             field.setParent(eField.getAttributeValue("parent", field.getParent()));
 
             LOG.debug("Reading field " + field.getName());
 
             readFieldGetter(eField, ns, field);
             readFieldSetter(eField, ns, field);
             readFieldLink(eField, ns, field);
 
             fields.add(field);
         }
 
         return fields;
     }
 
     private void readFieldGetter(final Element eField, final Namespace ns, final Field field)
     {
         final FieldGetter getter = field.getGet();
         final Element eGetter = eField.getChild("get", ns);
         if (null != eGetter) {
             getter.setAccessor(eGetter.getAttributeValue("accessor", getter.getAccessor()));
             getter.setName(eGetter.getAttributeValue("name", getter.getName()));
             getter.setOverride(checkBoolean(eGetter.getAttributeValue("override"), getter.isOverride()));
             getter.setFromSuper(checkBoolean(eGetter.getAttributeValue("from-super"), getter.isFromSuper()));
 
             getter.setContent(eGetter.getTextTrim());
         }
     }
 
     private void readFieldSetter(final Element eField, final Namespace ns, final Field field)
     {
         final FieldSetter setter = field.getSet();
         final Element eSetter = eField.getChild("set", ns);
         if (null != eSetter) {
             setter.setAccessor(eSetter.getAttributeValue("accessor", setter.getAccessor()));
             setter.setName(eSetter.getAttributeValue("name", setter.getName()));
             setter.setOverride(checkBoolean(eSetter.getAttributeValue("override"), setter.isOverride()));
             setter.setFromSuper(checkBoolean(eSetter.getAttributeValue("from-super"), setter.isFromSuper()));
 
             setter.setContent(eSetter.getTextTrim());
         }
     }
 
     private void readFieldLink(final Element eField, final Namespace ns, final Field field)
         throws BadInputException
     {
         final FieldLink link = field.getLink();
         final Element eLink = eField.getChild("link", ns);
         if (null != eLink) {
             link.setMode(findLinkMode(eLink.getAttributeValue("mode"), link.getMode()));
             link.setType(eLink.getAttributeValue("type", link.getType()));
             link.setKey_type(eLink.getAttributeValue("key-type", link.getKey_type()));
             link.setSql_type(findSQLType(eLink.getAttributeValue("sql-type"), link.getSql_type()));
 
             if (link.getMode() == LinkMode.OneToMany || link.getMode() == LinkMode.ManyToMany) {
                 link.setCollection(readFieldLinkCollection(eLink, ns, link));
             }
             else if (null != eLink.getChild("collection", ns)) {
                 throw new BadInputException("Illegal presence of 'collection' element beneath link with mode " + link.getMode() +
                                             " on field " + field.getName());
             }
         }
     }
 
     private FieldLinkCollection readFieldLinkCollection(final Element eLink, final Namespace ns, final FieldLink link)
         throws BadInputException
     {
         final FieldLinkCollection collection = new FieldLinkCollection(link);
         final Element eCollection = eLink.getChild("collection", ns);
         if (null == eCollection) {
             throw new BadInputException("Missing 'collection' element beneath link with mode " + link.getMode());
         }
         else {
             final Element eRead = eCollection.getChild("read", ns);
             if (null == eRead) {
                 throw new BadInputException("Missing 'read' element beneath link collection");
             }
             readQuery(eRead, ns, collection.getRead(), null, "r");
 
             final Element eCreate = eCollection.getChild("create", ns);
             if (null != eCreate) {
                 collection.setCreate(new Query());
                 readQuery(eCreate, ns, collection.getCreate(), null, "c");
             }
 
             final Element eDelete = eCollection.getChild("delete", ns);
             if (null != eDelete) {
                 collection.setDelete(new Query());
                 readQuery(eDelete, ns, collection.getDelete(), null, "d");
             }
         }
 
         return collection;
     }
 
     /**
      * Read a {@link Query}.
      * 
      * @param eRoot The root element (or the query element; see the
      *            <code>type</code> parameter).
      * @param ns The namespace.
      * @param query [OUT] The query to read.
      * @param type The name of the query element within the root element to
      *            read, or (if <code>null</code>) treat the root element as the
      *            query element.
      * @param subtype The name of the individual per-dialect elements to read.
      * @throws BadInputException If there was a problem reading the data.
      */
     private void readQuery(final Element eRoot, final Namespace ns, final Query query, final String type, final String subtype)
         throws BadInputException
     {
         final Element element;
         if (null == type) {
             element = eRoot;
         }
         else {
             element = eRoot.getChild(type, ns);
         }
 
         if (null != element) {
             for (final Object o : element.getChildren(subtype, ns)) {
                 final Element ec = (Element)o;
                 final String dialect = ec.getAttributeValue("dialect");
                 final String content = ec.getTextTrim();
 
                 if (null != query.putQuery(dialect, content)) {
                     throw new BadInputException("Duplicate '" + subtype + "' query dialect: " + dialect);
                 }
             }
         }
     }
 
     private List<NamedQuery> readNamedQueries(final Element eRoot, final Namespace ns)
         throws BadInputException
     {
         final List<NamedQuery> queries = new LinkedList<NamedQuery>();
         for (final Object o : eRoot.getChildren("query", ns)) {
             final Element eQuery = (Element)o;
             queries.add(readNamedQuery(eQuery, ns));
         }
 
         return queries;
     }
 
     private NamedQuery readNamedQuery(final Element eQuery, final Namespace ns)
         throws BadInputException
     {
         final NamedQuery query = new NamedQuery();
         query.setAccessor(eQuery.getAttributeValue("accessor", query.getAccessor()));
         query.setName(eQuery.getAttributeValue("name", query.getName()));
 
         for (final Object o : eQuery.getChildren("param", ns)) {
             final Element eParam = (Element)o;
             query.getParams().add(readNamedQueryParam(eParam, ns));
         }
 
         readQuery(eQuery, ns, query.getQuery(), null, "q");
 
         return query;
     }
 
     private QueryParam readNamedQueryParam(final Element eParam, final Namespace ns)
     {
         final QueryParam param = new QueryParam();
         param.setType(eParam.getAttributeValue("type", param.getType()));
         param.setName(eParam.getAttributeValue("name", param.getName()));
         param.setSql_type(findSQLType(eParam.getAttributeValue("sql-type"), param.getSql_type()));
 
         final Element eSetter = eParam.getChild("set", ns);
         if (null != eSetter) {
             param.getSet().setContent(eSetter.getTextTrim());
         }
 
         return param;
     }
 
     private SQLType findSQLType(final String input, final SQLType def)
     {
         if (null != input) {
             try {
                 return SQLType.valueOf(input);
             }
             catch (final IllegalArgumentException iae) {
                 return def;
             }
         }
         else {
             return def;
         }
     }
 
     private boolean checkBoolean(final String input, final boolean def)
     {
         if (null != input) {
             return Boolean.parseBoolean(input);
         }
         else {
             return def;
         }
     }
 
     private LinkMode findLinkMode(final String input, final LinkMode def)
     {
         if (null != input) {
             try {
                 return LinkMode.valueOf(input);
             }
             catch (final IllegalArgumentException iae) {
                 return def;
             }
         }
         else {
             return def;
         }
     }
 }
