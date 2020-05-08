 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz.datamanager;
 
 import org.fiz.*;
 import java.util.*;
 import org.apache.log4j.Logger;
 import com.google.appengine.api.datastore.*;
 
 /**
  * GaeDataManager provides access to Google AppEngine's Datastore via its
  * low-level API.  Unlike traditional relational database schemas, the Datastore
  * uses a single large table with varying row types.  Each row, an Entity, can
  * have varying "columns" (referred to as "properties") with varying values -
  * although rows have types (kinds), that typing does not place any restriction
  * on what properties or values an Entity can or cannot have.  In
  * GaeDataManager, Entities are presented to the developer as Datasets - an
  * Entity property-value pair is encapsulated as a Dataset key-value pair.
  *
  * Although there are no separate tables in Datastore, Entities are grouped both
  * by their kind and by their ancestry.  Every Entity has a pointer to an
  * optional parent Entity, creating a tree-like organization of Entities.
  * Entities with the same root (a parent-less ancestor) are all stored near each
  * other within the Datastore, which makes queries by common ancestor very
  * efficient.
  *
  * Each Entity in the Datastore has an associated Key.  Keys uniquely
  * identify Entities, and encapsulate the Entity's kind, a unique ID, and an
  * optional pointer to a parent Key.  In GaeDataManager, Keys are often
  * returned to the developer and are expected as arguments to many functions.
  *
  * While Datasets support values of arbitrary classes, the Datastore is more
 * restrictive.  See {@link http://code.google.com/appengine/docs/java/datastore
 * /dataclasses.html#Core_Value_Types} for a list of approved Datastore Objects.
 * Datastore also supports collections of these Objects - as in Datasets,
 * Entities support the notion of multiple values associated with a key or
 * property.
  *
  * GaeDataManager exports a number of Datastore operations found below, most
  * of which fall into one of five categories:
  * - Find By Key:       given a list of Keys, retrieve their associated records.
  * - Insert:            given a Dataset, convert it to an Entity and store it.
  * - Delete:            given a Key, delete the record associated with that Key.
  * - Find By Query:     given a series of filters and sorts, return a list of
  *                      records that satisfy the parameters.  Queries can only
  *                      be performed on Entities of the same kind.
  * - Find By Ancestor:  given an ancestor Key, return all records who have that
  *                      Key as an ancestor.  Entities and their ancestors do
  *                      not necessarily have the same kind.
  *
  */
 public class GaeDataManager {
 
     /* The Dataset key in which an Entity's Key is placed */
     public static final String KEY_PROPERTY = "_key_";
 
     /* The following variable is used for log4j-based logging */
     protected static Logger logger = Logger.getLogger(
             "org.fiz.DatastoreDataManager");
 
     /* Used for specifying messages for common Datastore-related errors */
     protected static enum ErrType {
         DS_FAILURE,
         TXN_INACTIVE,
         BAD_ARG
     }
 
     /**
      * Thrown when a QueryAttribute is created with invalid input
      */
     public static class InvalidQueryError extends Error {
         /**
          * Creates the InvalidQueryError with the given message
          * @param message          Describes why the query was invalid
          */
         public InvalidQueryError(String message) {
             super(message);
         }
     }
 
     /**
      * Used internally when an exception that should be caught by the
      * data manager occurs.
      */
     protected static class GaeException extends Exception {
         /**
          * Constructs a GaeException with the given message
          * @param message          Describes the exception that occured
          */
         public GaeException(String message) {
             super(message);
         }
     }
 
     /* The handle on the Datastore used by this DataManager. */
     protected DatastoreService datastore;
 
     /**
      * Constructs a new GaeDataManager, acquiring a connection to the
      * Datastore.
      *
      */
     public GaeDataManager() {
         datastore = DatastoreServiceFactory.getDatastoreService();
         logger.info("DatastoreService instance successfully created.");
     }
 
     /**
      * Returns a new Transaction on the Datastore.  That Transaction is
      * considered the current Transaction (and is used implicitly for all
      * subsequent Datastore actions) until a new Transaction is created, or
      * until the Transaction is committed or rolled back.  Note: all actions
      * taken under a Transaction must act only on Entities with the same
      * root ancestor.
      *
      * @return          A new, current Transaction.
      */
     public Transaction getNewTransaction() {
         return datastore.beginTransaction();
     }
 
     /**
      * Given the Key of a parent Entity and an Entity kind, returns an array of
      * new Keys that can be used for inserting Entities of that kind and
      * ancestry.
      *
      * @param parent    The parent Key for the newly allocated Keys.
      * @param kind      The kind of the newly allocated Keys.
      * @param nKeys     The number of new Keys to allocate.
      * @return          A Key[] containing the newly allocated Keys.
      */
     public Key[] allocateKeys(Key parent, String kind, int nKeys) {
         return convertKeyRange(datastore.allocateIds(parent, kind, nKeys));
     }
 
     /**
      * Given an Entity kind, returns an array of new Keys that can be used for
      * inserting Entities of that kind.
      *
      * @param kind      The kind of the newly allocated Keys.
      * @param nKeys     The number of new Keys to allocate.
      * @return          A Key[] containing the newly allocated Keys.
      */
     public Key[] allocateKeys(String kind, int nKeys) {
         return convertKeyRange(datastore.allocateIds(kind, nKeys));
     }
 
     /**
      * Converts a given KeyRange into a more easily usable Key[].
      *
      * @param range     The KeyRange to convert.
      * @return          A Key[] with the contents of the KeyRange.
      */
     protected Key[] convertKeyRange(KeyRange range) {
         Iterator<Key> rangeIter = range.iterator();
         Key[] result = new Key[(int)range.getSize()];
         int counter = 0;
         while (rangeIter.hasNext()) {
             result[counter++] = rangeIter.next();
         }
         return result;
     }
 
     /**
      * An interface for the two classes used to specify filters and sorts on
      * Datastore queries.  Specifies that these classes must know how to add
      * whatever query restriction they encapsulate to a given Query object.
      */
     public static interface QueryAttribute {
         /**
          * Adds this QueryAttribute as a filter or sort on the specified Query.
          *
          * @param query         The Query to filter or sort.
          */
         public void addToQuery(Query query);
     }
 
     /**
      * Encapsulates a Query term or filter.  Given an attribute name, one of
      * the five supported Datastore comparison tests, and an Object with which
      * to compare, adds that restriction to the Query in which this QueryTerm
      * is used.  The following Query restrictions apply:
      *
      * - Only one attribute per query may have any inequality tests.  However,
      * that attribute may have more than one inequality test placed on it.
      * - When testing on an attribute with more than one value, the test will
      * pass if any one of the values satisfies the constraint.
      * - Collections are not supported as comparison attribute values - only a
     * single Object of a Datastore-supported type is acceptable.  See {@link
      * http://code.google.com/appengine/docs/java/datastore/dataclasses.html
      * #Core_Value_Types} for a list of supported types.
      */
     public static class QueryTerm implements QueryAttribute {
         /* The name of the Entity property that this QueryTerm should apply
          * to. */
         protected String attrName;
         /* The Object with which to compare the value of the specified Entity
          * property. */
         protected Object attrValue;
         /* The operator to use when comparing the specified value to the Entity
          * values (one of: =, <, >, <=, >=). */
         protected Query.FilterOperator operator;
 
         /**
          * Constructs a new QueryTerm, caching the name of the Entity property
          * to filter and the value of the property with which to compare.  Also
          * converts the specified String operand to one of the five supported
          * Datastore Query filter operators.  If one of the five operands is
          * not detected, throws a DatastoreError.
          *
          * @param attrName      The name of the Entity property that this
          *                      QueryTerm should apply to.
          * @param op            The operator to use for comparing Entity values.
          * @param attrValue     The Object with which to compare the value of
          *                      the specified Entity property.
          */
         public QueryTerm(String attrName, String op, Object attrValue) {
             this.attrName = attrName;
             this.attrValue = attrValue;
 
             if (op.equals("=")) {
                 operator = Query.FilterOperator.EQUAL;
             } else if (op.equals("<")) {
                 operator = Query.FilterOperator.LESS_THAN;
             } else if (op.equals(">")) {
                 operator = Query.FilterOperator.GREATER_THAN;
             } else if (op.equals("<=")) {
                 operator = Query.FilterOperator.LESS_THAN_OR_EQUAL;
             } else if (op.equals(">=")) {
                 operator = Query.FilterOperator.GREATER_THAN_OR_EQUAL;
             } else {
                 // Throw an error if the operator is unexpected.
                 throw new InvalidQueryError("Operator (" + op + ") must be " +
                                             "one of: {=, <, >, <=, >=}.");
             }
         }
 
         /**
          * Adds this QueryTerm as a filter on the specified Query.
          *
          * @param query         The Query to filter.
          */
         public void addToQuery(Query query) {
             query.addFilter(attrName, operator, attrValue);
         }
     }
 
     /**
      * Encapsulates a Query sort.  Given an attribute name and an optional
      * specification of sort direction (ascending by default), adds that sort
      * to the Query in which this QuerySort is used.  The following Query
      * restrictions apply:
      *
      * - If a column has an inequality filter on it, that column must be sorted
      * before any other sorts can be placed on that query.  The ordering is
      * important as well - if column A has an inequality filter and you wish to
      * sort on column B, you must specify (in this order): filter A, sort A,
      * sort B.
      * - Sorts are applied in the order in which they are specified.  For
      * example, specifying a sort on A and then on B will first sort on A, and
      * wherever two A values are equal, the sort on B will be used to determine
      * the relative ordering.
      * - An ascending sort on a attribute with multiple values will sort using
      * the lowest-valued Entity.  A descending sort will use the highest-
      * valued Entity.
      * - If a sorted attribute has values of multiple types (classes), Entities
      * with values of the same type will be grouped together and sorted in the
      * result; however, the relative ordering of the types is unspecified.
      */
     public static class QuerySort implements QueryAttribute {
         /* The name of the Entity property that should be sorted. */
         protected String attrName;
         /* The direction in which to sort (ascending or descending). */
         protected Query.SortDirection direction;
 
         /**
          * Constructs a new ascending QuerySort on the specified Entity
          * property.
          *
          * @param attrName      The Entity property to sort.
          */
         public QuerySort(String attrName) {
             this.attrName = attrName;
             this.direction = Query.SortDirection.ASCENDING;
         }
 
         /**
          * Constructs a new QuerySort on the specified Entity property, with the
          * specified sort direction.
          *
          * @param attrName      The Entity property to sort.
          * @param dir           A String representation of the direction in
          *                      which to sort.  If (@code dir} cannot be parsed
          *                      as either ascending or descending, a
          *                      DatastoreError is thrown.
          */
         public QuerySort(String attrName, String dir) {
             this.attrName = attrName;
 
             if (dir.equalsIgnoreCase("ascending")
                     || dir.equalsIgnoreCase("asc")) {
                 direction = Query.SortDirection.ASCENDING;
             } else if (dir.equalsIgnoreCase("descending")
                     || dir.equalsIgnoreCase("desc")) {
                 direction = Query.SortDirection.DESCENDING;
             } else {
                 // Throw an error if the sort direction is unexpected.
                 throw new InvalidQueryError("QuerySort Direction (" + dir +
                             ") must be (case insensitive) one of: {ascending, asc, " +
                             "descending, desc}.");
             }
         }
 
         /**
          * Adds this QuerySort as a sort on the specified Query.
          *
          * @param query         The Query to sort.
          */
         public void addToQuery(Query query) {
             query.addSort(attrName, direction);
         }
     }
 
 
     /* FIND BY KEY */
     /**
      * Finds the records with the specified Keys.
      *
      * @param keys      The Keys of the records to find.
      * @return          A Dataset containing the found records.  Records are
      *                  mapped by the attribute "record".  No error is thrown
      *                  if a Key did not match a record.
      */
     public Dataset findByKey(Key ... keys) {
         return handleFindKey(null, false, keys);
     }
 
     /**
      * Finds the records with the specified Keys.
      *
      * @param txn       The Transaction under which to perform the find.
      * @param keys      The Keys of the records to find.
      * @return          A Dataset containing the found records.  Records are
      *                  mapped by the attribute "record".  No error is thrown
      *                  if a Key did not match a record.
      */
     public Dataset findByKey(Transaction txn, Key ... keys) {
         return handleFindKey(txn, false, keys);
     }
 
     /* FIND MAPPING BY KEY */
     /**
      * Finds the records with the specified Keys, mapped by their String-ified
      * Key.
      *
      * @param keys      The Keys of the records to find.
      * @return          A Dataset containing the found records.  Records are
      *                  mapped by their String-ified Key.  No error is thrown
      *                  if a Key did not match a record.
      */
     public Dataset findMappingByKey(Key ... keys) {
         return handleFindKey(null, true, keys);
     }
 
     /**
      * Finds the records with the specified Keys, mapped by their String-ified
      * Key.
      *
      * @param txn       The Transaction under which to perform the find.
      * @param keys      The Keys of the records to find.
      * @return          A Dataset containing the found records.  Records are
      *                  mapped by their String-ified Key.  No error is thrown
      *                  if a Key did not match a record.
      */
     public Dataset findMappingByKey(Transaction txn, Key ... keys) {
         return handleFindKey(txn, true, keys);
     }
 
     /**
      * Exectutes a get operation on the Datastore.  Given a
      * Transaction to use, whether or not the return format should
      * map Keys to Entities, and the Keys whose Entities we wish to retrieve,
      * makes the necessary Datastore calls to perform the get and saves the
      * resulting Dataset of records.
      *
      * @param txn           The Transaction under which to perform the find.
      * @param keyMapping    Whether or not the results should be mapped by
      *                      String-ified Key.
      * @param keys          The Keys of the records to find.
      * @return              A Dataset containing the found records.  No error
      *                      is thrown if a Key did not match a record.
      */
     protected Dataset handleFindKey(Transaction txn, boolean keyMapping,
             Key ... keys) {
         boolean nullTxn = (txn == null);
         Dataset result = null;
         try {
             if (keys.length == 1) { // If only one Key was specified.
                 Entity entity = nullTxn ?
                     datastore.get(keys[0]) :
                     datastore.get(txn, keys[0]);
                 return entityToDataset(entity);
             } else { // If more than one Key was specified.
                 Iterable<Key> iter = Arrays.asList(keys);
                 Map<Key, Entity> entities = nullTxn ?
                     datastore.get(iter) :
                     datastore.get(txn, iter);
                 result = new Dataset();
                 // Store each Entity record into the result Dataset, mapping
                 // the Entity either by "record" or by its String-ified Key.
                 for (Map.Entry<Key, Entity> entity : entities.entrySet()) {
                     String key = keyMapping ? keyToString(entity.getKey()) :
                         "record";
                     result.add(key, entityToDataset(entity.getValue()));
                 }
 
                 return result;
             }
         } catch (EntityNotFoundException e) {
             // Thrown if the single-Key Datastore get failed to return a
             // result.  In this case, return an empty Dataset as the result.
             return new Dataset();
         } catch (IllegalArgumentException e) {
             return addErrorData(result, ErrType.BAD_ARG, "Key", e);
         } catch (IllegalStateException e) {
             return addErrorData(result, ErrType.TXN_INACTIVE, "", e);
         } catch (DatastoreFailureException e) {
             return addErrorData(result, ErrType.DS_FAILURE, "", e);
         }
     }
 
 
     /* INSERT (single record) */
     /**
      * Inserts the specified Dataset into the Datastore, using the Datastore
      * Key implicitly located in that Dataset.
      *
      * @param dataset       The Dataset to insert.
      * @return              Dataset with a single entry "key" containing the Key
      *                      corresponding to the inserted record.
      */
     public Dataset insert(Dataset dataset) {
         return insert((Transaction)null, dataset);
     }
 
     /**
      * Inserts the specified Dataset into the Datastore, giving it the
      * specified kind.
      *
      * @param kind          The kind (type) of the inserted record.
      * @param dataset       The Dataset to insert.
      * @return              Dataset with a single entry "key" containing the Key
      *                      corresponding to the inserted record.
      */
     public Dataset insert(String kind, Dataset dataset) {
         return insert((Transaction)null, kind, dataset);
     }
 
     /**
      * Inserts the specified Dataset into the Datastore, giving it the
      * specified kind and making it a descendant of the specified Key.
      *
      * @param parentKey     The ancestor Key of the inserted record.
      * @param kind          The kind (type) of the inserted record.
      * @param dataset       The Dataset to insert.
      * @return              Dataset with a single entry "key" containing the Key
      *                      corresponding to the inserted record.
      */
     public Dataset insert(Key parentKey, String kind, Dataset dataset) {
         return insert((Transaction)null, parentKey, kind, dataset);
     }
 
     /**
      * Inserts the specified Dataset into the Datastore, using the Datastore
      * Key implicitly located in that Dataset.
      *
      * @param txn           The Transaction under which this insert will take
      *                      place.
      * @param dataset       The Dataset to insert.
      * @return              Dataset with a single entry "key" containing the Key
      *                      corresponding to the inserted record.
      */
     public Dataset insert(Transaction txn, Dataset dataset) {
         Entity entity;
         try {
             entity = datasetToEntity(dataset);
         } catch (GaeException e) {
             return new Dataset(e);
         }
         return handleInsert(txn, entity);
     }
 
     /**
      * Inserts the specified Dataset into the Datastore, giving it the
      * specified kind.
      *
      * @param txn           The Transaction under which this insert will take
      *                      place.
      * @param kind          The kind (type) of the inserted record.
      * @param dataset       The Dataset to insert.
      * @return              Dataset with a single entry "key" containing the Key
      *                      corresponding to the inserted record.
      */
     public Dataset insert(Transaction txn, String kind, Dataset dataset) {
         return handleInsert(txn, datasetToEntity(kind, dataset));
     }
 
     /**
      * Inserts the specified Dataset into the Datastore, giving it the
      * specified kind and making it a descendant of the specified Key.
      *
      * @param txn           The Transaction under which this insert will take
      *                      place.
      * @param parentKey     The parent Key of the inserted record.
      * @param kind          The kind (type) of the inserted record.
      * @param dataset       The Dataset to insert.
      * @return              Dataset with a single entry "key" containing the Key
      *                      corresponding to the inserted record.
      */
     public Dataset insert(Transaction txn, Key parentKey, String kind,
             Dataset dataset) {
         return handleInsert(txn, datasetToEntity(parentKey, kind, dataset));
     }
 
     /* INSERT (multiple records) */
     /**
      * Inserts the specified Datasets into the Datastore, using the Datastore
      * Key implicitly located in those Datasets.
      *
      * @param datasets      The Datasets to insert.
      * @return              Dataset continaing a "key" entry for each of the
      *                      keys of the Entities inserted
      */
     public Dataset insert(Dataset ... datasets) {
         return insert((Transaction)null, datasets);
     }
 
     /**
      * Inserts the specified Datasets into the Datastore, giving them the
      * specified kind.
      *
      * @param kind          The kind (type) of the inserted records.
      * @param datasets      The Datasets to insert.
      * @return              Dataset continaing a "key" entry for each of the
      *                      keys of the Entities inserted
      */
     public Dataset insert(String kind, Dataset ... datasets) {
         Entity entities[] = new Entity[datasets.length];
         for (int i=0; i<datasets.length; i++) {
             entities[i] = datasetToEntity(kind, datasets[i]);
         }
         return handleInsert(null, entities);
     }
 
     /**
      * Inserts the specified Datasets into the Datastore, giving them the
      * specified kind and making them descendants of the specified Key.
      *
      * @param parentKey     The ancestor Key of the inserted records.
      * @param kind          The kind (type) of the inserted records.
      * @param datasets      The Datasets to insert.
      * @return              Dataset continaing a "key" entry for each of the
      *                      keys of the Entities inserted
      */
     public Dataset insert(Key parentKey, String kind, Dataset ... datasets) {
         return insert((Transaction)null, parentKey, kind, datasets);
     }
 
     /**
      * Inserts the specified Datasets into the Datastore, using the Datastore
      * Key implicitly located in those Datasets.
      *
      * @param txn           The Transaction under which this insert will take
      *                      place.
      * @param datasets      The Datasets to insert.
      * @return              Dataset continaing a "key" entry for each of the
      *                      keys of the Entities inserted
      */
     public Dataset insert(Transaction txn, Dataset ... datasets) {
         Entity entities[] = new Entity[datasets.length];
         for (int i=0; i<datasets.length; i++) {
             try {
                 entities[i] = datasetToEntity(datasets[i]);
             } catch (GaeException e) {
                 return new Dataset(e);
             }
         }
         return handleInsert(txn, entities);
     }
 
     /**
      * Inserts the specified Datasets into the Datastore, giving them the
      * specified kind and making them descendants of the specified Key.
      *
      * @param txn           The Transaction under which this insert will take
      *                      place.
      * @param parentKey     The parent Key of the inserted records.
      * @param kind          The kind (type) of the inserted records.
      * @param datasets      The Datasets to insert.
      * @return              Dataset continaing a "key" entry for each of the
      *                      keys of the Entities inserted
      */
     public Dataset insert(Transaction txn, Key parentKey, String kind,
             Dataset ... datasets) {
         Entity entities[] = new Entity[datasets.length];
         for (int i=0; i<datasets.length; i++) {
             entities[i] = datasetToEntity(parentKey, kind, datasets[i]);
         }
         return handleInsert(txn, entities);
     }
 
     /**
      * Executes an insert operation on the Datastore.  Given the a
      * a Transaction to use, and the Entities to insert, makes the
      * necessary Datastore calls to insert the Entities and saves the Keys of
      * the newly inserted records.
      *
      * @param txn           The Transaction under which this insert will take
      *                      place.
      * @param entities      The Entities to insert.
      * @return              Dataset continaing a "key" entry for each of the
      *                      keys of the Entities inserted
      */
     protected Dataset handleInsert(Transaction txn, Entity ... entities) {
         Dataset records = null;
         try {
             if (entities.length == 1) { // If only one Entity was specified.
                 Key key = (txn == null) ?
                     datastore.put(entities[0]) :
                     datastore.put(txn, entities[0]);
                 // Save the Datastore Key for the inserted Entity.
                 return new Dataset("record", key);
             } else { // If more than one Entity was specified.
                 Iterable<Entity> iter = Arrays.asList(entities);
                 Iterable<Key> keys = (txn == null) ?
                     datastore.put(iter) :
                     datastore.put(txn, iter);
                 records = new Dataset();
                 // Save the Datastore Keys for the inserted Entities.
                 for (Key key : keys) {
                     records.add("record", key);
                 }
                 return records;
             }
         } catch (IllegalArgumentException e) {
             return addErrorData(records, ErrType.BAD_ARG, "Entity", e);
         } catch (IllegalStateException e) {
             return addErrorData(records, ErrType.TXN_INACTIVE, "", e);
         } catch (DatastoreFailureException e) {
             return addErrorData(records, ErrType.DS_FAILURE, "", e);
         }
     }
 
     /* DELETE */
     /**
      * Deletes the records with the specified Keys.
      *
      * @param keys          The Keys corresponding to the records to delete.
      * @return              An empty dataset
      */
     public Dataset delete(Key ... keys) {
         return handleDelete(null, keys);
     }
 
     /**
      * Deletes the records with the specified Keys.
      *
      * @param txn           The Transaction under which this delete will take
      *                      place.
      * @param keys          The Keys corresponding to the records to delete.
      * @return              An empty dataset
      */
     public Dataset delete(Transaction txn, Key ... keys) {
         return handleDelete(txn, keys);
     }
 
     /**
      * Executes a delete operation on the Datastore.  Given a
      * Transaction to use, and the Keys of Entities to delete, makes
      * the necessary Datastore calls to delete the Entities.
      *
      * @param txn           The Transaction under which this delete will take
      *                      place.
      * @param keys          The Keys corresponding to the records to delete.
      * @return              An empty dataset
      */
     protected Dataset handleDelete(Transaction txn, Key ... keys) {
         try {
             // Perform the delete with the Transaction if it isn't null.
             if (txn == null) {
                 datastore.delete(keys);
             } else {
                 datastore.delete(txn, keys);
             }
         } catch (IllegalArgumentException e) {
             return addErrorData(null, ErrType.BAD_ARG, "Key", e);
         } catch (IllegalStateException e) {
             return addErrorData(null, ErrType.TXN_INACTIVE, "", e);
         } catch (DatastoreFailureException e) {
             return addErrorData(null, ErrType.DS_FAILURE, "", e);
         }
 
         return null;
     }
 
 
     /* FIND BY QUERY */
     /**
      * Finds records based on their kind and a list of QueryAttributes.
      *
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the records matching the query.
      *                      Records are mapped by the attribute "record".
      */
     public Dataset findByQuery(String kind, QueryAttribute ... attributes) {
         return findByQuery(null, null, kind, attributes);
     }
 
     /**
      * Finds records based on their ancestor Key, their kind, and a list of
      * QueryAttributes.
      *
      * @param ancestorKey   The ancestor Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the records matching the query.
      *                      Records are mapped by the attribute "record".
      */
     public Dataset findByQuery(Key ancestorKey, String kind,
             QueryAttribute ... attributes) {
         return findByQuery(null, ancestorKey, kind, attributes);
     }
 
     /**
      * Finds records based on their ancestor Key, their kind, and a
      * list of QueryAttributes.
      *
      * @param txn           The Transaction under which this query will take
      *                      place.
      * @param ancestorKey   The ancestor Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the records matching the query.
      *                      Records are mapped by the attribute "record".
      */
     public Dataset findByQuery(Transaction txn, Key ancestorKey, String kind,
             QueryAttribute ... attributes) {
         return handleFindQuery(txn, ancestorKey, kind, false, false,
                 attributes);
     }
 
     /* FIND MAPPING BY QUERY */
     /**
      * Finds records based on their kind and a list of QueryAttributes,
      * mapping them by their String-ified Key.
      *
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the Key-mapped records matching
      *                      the query.
      */
     public Dataset findMappingByQuery(String kind,
             QueryAttribute ... attributes) {
         return findMappingByQuery(null, null, kind, attributes);
     }
 
     /**
      * Finds records based on their ancestor Key, their kind, and a list of
      * QueryAttributes, mapping them by their String-ified Key.
      *
      * @param ancestorKey   The ancestor Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the Key-mapped records matching
      *                      the query.
      */
     public Dataset findMappingByQuery(Key ancestorKey, String kind,
             QueryAttribute ... attributes) {
         return findMappingByQuery(null, ancestorKey, kind, attributes);
     }
 
     /**
      * Finds records based on their ancestor Key, their kind, and a list of
      * QueryAttributes, mapping them by their String-ified Key.
      *
      * @param txn           The Transaction under which this query will take
      *                      place.
      * @param ancestorKey   The ancestor Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the Key-mapped records matching
      *                      the query.
      */
     public Dataset findMappingByQuery(Transaction txn, Key ancestorKey,
             String kind, QueryAttribute ... attributes) {
         return handleFindQuery(txn, ancestorKey, kind, true, false, attributes);
     }
 
     /* FIND KEY BY QUERY */
     /**
      * Finds the Keys of records based on their kind and a list of
      * QueryAttributes.
      *
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              The Keys of the records matching the query.
      */
     public Key[] findKeyByQuery(String kind,
             QueryAttribute ... attributes) {
         return findKeyByQuery(null, null, kind, attributes);
     }
 
     /**
      * Finds the Keys of records based on their ancestor Key, their kind, and a
      * list of QueryAttributes.
      *
      * @param ancestorKey   The ancestor Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              The Keys of the records matching the query.
      */
     public Key[] findKeyByQuery(Key ancestorKey, String kind,
             QueryAttribute ... attributes) {
         return findKeyByQuery(null, ancestorKey, kind, attributes);
     }
 
     /**
      * Finds the Keys of records based on their ancestor Key, their kind, and a
      * list of QueryAttributes.
      *
      * @param txn           The Transaction under which this query will take
      *                      place.
      * @param ancestorKey   The ancestor (parent) Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              The Keys of the records matching the query.
      */
     public Key[] findKeyByQuery(Transaction txn, Key ancestorKey,
             String kind, QueryAttribute ... attributes) {
         return handleFindQuery(txn, ancestorKey, kind, false, true, attributes)
             .getList("record").toArray(new Key[0]);
     }
 
     /**
      * An intermediary method for finding by query.
      *
      * @param txn           The Transaction under which this query will take
      *                      place.
      * @param ancestorKey   The ancestor Key of the records to search.
      * @param kind          The kind (type) of the records to search.
      * @param keyMapping    Whether or not the records should be mapped by their
      *                      String-ified Keys.
      * @param keysOnly      Whether or not the results should be the entire
      *                      records or just their Keys.
      * @param attributes    A series of filters and sorts to govern the query.
      * @return              A Dataset containing the records matching the query.
      */
     protected Dataset handleFindQuery(Transaction txn, Key ancestorKey,
             String kind, boolean keyMapping, boolean keysOnly,
             QueryAttribute ... attributes) {
         Query query = new Query(kind, ancestorKey);
         for (QueryAttribute attr : attributes) {
             attr.addToQuery(query);
         }
         return handleQuery(txn, keyMapping, keysOnly, query);
     }
 
 /* FIND BY ANCESTOR */
     /**
      * Finds records with a common ancestor.  A record's ancestry includes
      * itself, its parent, and its parent's ancestors.
      *
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              A Dataset containing all records with {@code
      *                      ancestorKey} as an ancestor.  Records are mapped by
      *                      the attribute "record".
      */
     public Dataset findByAncestor(Key ancestorKey) {
         return findByAncestor(null, ancestorKey);
     }
 
     /**
      * Finds records with a common ancestor.  A record's ancestry includes
      * itself, its parent, and its parent's ancestors.
      *
      * @param txn           The Transaction under which this search will take
      *                      place.
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              A Dataset containing all records with {@code
      *                      ancestorKey} as an ancestor.  Records are mapped by
      *                      the attribute "record".
      */
     public Dataset findByAncestor(Transaction txn, Key ancestorKey) {
         return handleFindAncestor(txn, false, false, ancestorKey);
     }
 
     /* FIND MAPPING BY ANCESTOR */
     /**
      * Finds records with a common ancestor, mapping them by their
      * String-ified Key.  A record's ancestry includes itself, its parent, and
      * its parent's ancestors.
      *
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              A Dataset containing the Key-mapped records with
      *                      {@code ancestorKey} as an ancestor.
      */
     public Dataset findMappingByAncestor(Key ancestorKey) {
         return findMappingByAncestor(null, ancestorKey);
     }
 
     /**
      * Finds records with a common ancestor, mapping them by their
      * String-ified Key.  A record's ancestry includes itself, its parent, and
      * its parent's ancestors.
      *
      * @param txn           The Transaction under which this search will take
      *                      place.
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              A Dataset containing the Key-mapped records with
      *                      {@code ancestorKey} as an ancestor.
      */
     public Dataset findMappingByAncestor(Transaction txn, Key ancestorKey) {
         return handleFindAncestor(txn, true, false, ancestorKey);
     }
 
     /* FIND KEY BY ANCESTOR */
     /**
      * Finds the Keys of records with a common ancestor.  A record's ancestry
      * includes itself, its parent, and its parent's ancestors.
      *
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              The Keys of all records with {@code ancestorKey} as
      *                      an ancestor.
      */
     public Key[] findKeyByAncestor(Key ancestorKey) {
         return findKeyByAncestor(null, ancestorKey);
     }
 
     /**
      * Finds the Keys of records with a common ancestor.  A record's ancestry
      * includes itself, its parent, and its parent's ancestors.
      *
      * @param txn           The Transaction under which this search will take
      *                      place.
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              The Keys of all records with {@code ancestorKey} as
      *                      an ancestor.
      */
     public Key[] findKeyByAncestor(Transaction txn, Key ancestorKey) {
         return handleFindAncestor(txn, false, true, ancestorKey).getList(
                 "record").toArray(new Key[0]);
     }
 
     /**
      * An intermediary method for finding by ancestor key.
      *
      * @param txn           The Transaction under which this search will take
      *                      place.
      * @param keyMapping    Whether or not the records should be mapped by their
      *                      String-ified Keys.
      * @param keysOnly      Whether or not the results should be the entire
      *                      records or just their Keys.
      * @param ancestorKey   The Key whose descendants are returned.
      * @return              A Dataset containing all records with {@code
      *                      ancestorKey} as an ancestor.
      */
     protected Dataset handleFindAncestor(Transaction txn, boolean keyMapping,
                                          boolean keysOnly, Key ancestorKey) {
         Query query = new Query(ancestorKey);
         query.addSort(Entity.KEY_RESERVED_PROPERTY);
         return handleQuery(txn, keyMapping, keysOnly, query);
     }
 
     /**
      * Executes a query operation on the Datastore.  Given the a
      * Transaction to use, result formatting parameters, and the
      * Query to perform, makes the necessary Datastore calls to perform the
      * Query and saves the resulting Dataset of records.
      * @param txn           The Transaction with which to execute this
      *                      Query.
      * @param keyMapping    Whether or not the request should store its
      *                      results mapped by String-ified Key.
      * @param keysOnly      Whether or not the request should retrieve only
      *                      the Keys of the Entities matching the query.
      * @param query         The Datastore Query to execture.
      */
     protected Dataset handleQuery(Transaction txn, boolean keyMapping,
                   boolean keysOnly, Query query) {
         Dataset results = null;
         if (keysOnly) {
             query.setKeysOnly();
         }
         try {
             // Prepare and execute the query.
             PreparedQuery pq = (txn == null) ?
                 datastore.prepare(query) :
                 datastore.prepare(txn, query);
             results = new Dataset();
             for (Entity record : pq.asIterable()) {
                 Key key = record.getKey();
                 // For each record, store it in the Dataset:
                 // - mapped by either "record" or its String-ified Key.
                 // - as a Dataset (the entire Entity) or as just its Key.
                 String attrName = keyMapping ? keyToString(key) : "record";
                 Object attrVal = keysOnly ? key : entityToDataset(record);
                 results.add(attrName, attrVal);
             }
             return results;
         } catch (IllegalStateException e) {
             return addErrorData(results, ErrType.TXN_INACTIVE, "", e);
         } catch (IllegalArgumentException e) {
             return addErrorData(results, ErrType.BAD_ARG, "QueryArgument", e);
         } catch (DatastoreFailureException e) {
             return addErrorData(results, ErrType.DS_FAILURE, "", e);
         }
     }
 
     /* ENTITY - DATASET CONVERSION UTILITIES */
     /**
      * Converts an Entity to a Dataset, copying over all Entity fields and
      * storing the Entity Key as the Dataset's {@code KEY_PROPERTY} value.
      *
      * @param entity        The Entity to convert.
      * @return              A Dataset containing the Key and contents of the
      *                      Entity.
      */
     protected static Dataset entityToDataset(Entity entity) {
         Dataset result = new Dataset();
 
         for (Map.Entry<String, Object> property :
                 entity.getProperties().entrySet()) {
             String key = property.getKey();
             Object value = property.getValue();
             // Ensure that the resulting Dataset internally stores an Entity
             // Collection<T> as a series of single T objects, instead of as a
             // single Collection<T> object; all Entity collections are intended
             // as multiple values with the same key, not a single Collection
             // value.
             if (value instanceof Collection) {
                 for (Object subVal : (Collection)value) {
                     result.add(key, subVal);
                 }
             } else {
                 result.add(key, value);
             }
         }
 
         // Add the Entity Key.
         result.set(KEY_PROPERTY, entity.getKey());
 
         return result;
     }
 
     /**
      * Converts a Dataset to an Entity with the specified parent key and kind.
      *
      * @param parentKey     The parent key for the Entity.
      * @param kind          The kind of the Entity.
      * @param dataset       The contents of the Entity.
      * @return              An Entity with the specified contents, ancestry,
      *                      and kind.
      */
     protected static Entity datasetToEntity(Key parentKey, String kind,
             Dataset dataset) {
         // Create a new Entity of the specified kind with the specified parent.
         Entity result = new Entity(kind, parentKey);
         // Fill the Entity.
         return fillEntity(dataset, result);
     }
 
     /**
      * Converts a Dataset to an Entity with the specified kind.
      *
      * @param kind          The kind of the Entity.
      * @param dataset       The contents of the Entity.
      * @return              An Entity with the specified contents and kind.
      */
     protected static Entity datasetToEntity(String kind, Dataset dataset) {
         // Create a new Entity of the specified kind.
         Entity result = new Entity(kind);
         // Fill the Entity.
         return fillEntity(dataset, result);
     }
 
     /**
      * Converts a Dataset to an Entity, acting under the assumption that the
      * Entity Key is already present in the specified Dataset.  If a Key is not
      * present where expected, a DatastoreError is thrown.
      *
      * @param dataset       The contents of the Entity, including its Key.
      * @return              An Entity with the specified contents and Key.
      */
     protected static Entity datasetToEntity(Dataset dataset)
         throws GaeException {
         // Extract the Key, and create a new Entity with that Key.
         Entity result = null;
         if (dataset.containsKey(KEY_PROPERTY)) {
             Object keyObj = dataset.get(KEY_PROPERTY);
             Key key = null;
             if (keyObj instanceof Key) {
                 key = (Key)keyObj;
             } else if (keyObj instanceof String) {
                 key = stringToKey((String) keyObj);
                 if (key == null) {
                     throw new GaeException("String (" + (String) keyObj +
                                            ") is not a valid key");
                 }
             } else {
                 throw new GaeException("Key property (" +
                         KEY_PROPERTY + ") is not of type " + "Key or String: " +
                         keyObj.getClass().getName());
             }
             result = new Entity(key);
         } else {
             throw new GaeException("Dataset must contain a Key " +
                                                "attribute " + KEY_PROPERTY);
         }
         // Fill ohe Entity.
         return fillEntity(dataset, result);
     }
 
     /**
      * Given an Entity and a Dataset, fills the Entity with the key-value
      * pairs of the Dataset.
      *
      * @param dataset       A Dataset whose contents will be copied into the
      *                      Entity.
      * @param entity        An empty Entity.
      * @return              The populated Entity.
      */
     protected static Entity fillEntity(Dataset dataset, Entity entity) {
         for (String property : dataset.keySet()) {
             if (property.equals(KEY_PROPERTY)) continue; // Ignore any Key.
             ArrayList<Object> value = dataset.getList(property);
             // If there are any one-value lists in the dataset, just insert
             // the single value.
             Object toInsert = (value.size() == 1) ? value.get(0) : value;
             entity.setProperty(property, toInsert);
         }
         return entity;
     }
 
 
     /* KEY - STRING CONVERSION UTILITIES */
     /**
      * Converts a String to a Key
      *
      * @param key           The String to convert.
      * @return              The converted Key, or null if the string is not a
      *                      valid key
      */
     public static Key stringToKey(String key) {
         try {
             return KeyFactory.stringToKey(key);
         } catch (IllegalArgumentException e) {
             return null;
         }
     }
 
     /**
      * Converts a Key to a String.  The resulting string is guaranteed by
      * Google to be web-safe.  This String-ified Key is what is used to map
      * Entity records in the various findMappingBy...() methods ({@code
      * findMappingByKey()}, {@code findMappingByQuery()}, {@code
      * findMappingByAncestor()}).  This differs from Key.toString(), which
      * yields a different value.
      *
      * @param key           The Key to convert.
      * @return              The converted String.
      */
     public static String keyToString(Key key) {
         return KeyFactory.keyToString(key);
     }
 
     /**
      * Associates error information with the given dataset
      * @param data          Error information is added here. If null, a new
      *                      dataset is created.
      * @param type          The type (ErrType) of the error.
      * @param message       An additional error message.
      * @param cause         A Throwable cause: the original Error, or null.
      * @return              The dataset with error information
      */
     protected static Dataset addErrorData(Dataset data, ErrType type, String message,
                                 Throwable cause) {
         if (data == null) {
             data = new Dataset();
         }
         String typeMsg = "";
         switch (type) {
             case DS_FAILURE: typeMsg = "Datastore failure. "; break;
             case TXN_INACTIVE: typeMsg = "Transaction is inactive. "; break;
             case BAD_ARG: typeMsg = "Invalid, incomplete, or malformed " +
                     "argument of type: "; break;
         }
 
         data.setError(new Dataset("message", typeMsg + message, "cause", cause));
 
         return data;
     }
 }
