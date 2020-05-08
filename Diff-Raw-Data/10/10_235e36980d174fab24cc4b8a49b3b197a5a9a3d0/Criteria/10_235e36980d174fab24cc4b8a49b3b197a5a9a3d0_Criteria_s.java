 /**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.deephacks.graphene;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.sleepycat.je.LockMode;
 import org.deephacks.graphene.internal.EntityClassWrapper;
 import org.deephacks.graphene.internal.RowKey;
 import org.deephacks.graphene.internal.UniqueIds;
 import org.deephacks.graphene.internal.ValueSerialization.ValueReader;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * A criteria is used as a rule that evaluates to true or false on a
  * specific type of value.
  */
 @SuppressWarnings("rawtypes")
 public class Criteria implements Predicate {
 
     private static final EntityRepository repository = new EntityRepository();
 
     private static final UniqueIds ids = new UniqueIds();
     private final String fieldName;
     private Predicate p;
 
     private Criteria(String name, Predicate p) {
         this.fieldName = name;
         this.p = p;
     }
 
     public static Builder field(String name) {
         return new Builder(name);
     }
 
     @SuppressWarnings("unchecked")
     public Criteria and(Predicate predicate) {
         if (!Criteria.class.isInstance(predicate)) {
             predicate = new PredicateProxy(fieldName, predicate);
         }
         p = Predicates.and(p, predicate);
         return this;
     }
 
     @SuppressWarnings("unchecked")
     public Criteria or(Predicate predicate) {
         if (!Criteria.class.isInstance(predicate)) {
             predicate = new PredicateProxy(fieldName, predicate);
         }
         p = Predicates.or(p, predicate);
         return this;
     }
 
     public static class Builder {
         String fieldName;
 
         private Builder(String name) {
             this.fieldName = name;
         }
 
         public Criteria is(Predicate p) {
             return new Criteria(fieldName, new PredicateProxy(fieldName, p));
         }
 
         @SuppressWarnings("unchecked")
         public Criteria not(Predicate p) {
             return new Criteria(fieldName, new PredicateProxy(fieldName, Predicates.not(p)));
         }
     }
 
     /**
      * If a date is before.
      *
      * @param value
      * @return A date criteria
      */
     public static Predicate<Date> before(final Date value) {
         return new Predicate<Date>() {
 
             @Override
             public boolean apply(Date field) {
                 if (field == null) {
                     return false;
                 }
                 return field.before(value);
             }
 
         };
     }
 
     /**
      * If a date is before or equals.
      *
      * @param value
      * @return A date criteria
      */
     public static Predicate<Date> beforeOrEquals(final Date value) {
         return new Predicate<Date>() {
 
             @Override
             public boolean apply(Date field) {
                 if (field == null) {
                     return false;
                 }
                 return field.before(value) || field.equals(value);
             }
 
         };
     }
 
     /**
      * If a date is after.
      *
      * @param value
      * @return A date criteria
      */
     public static Predicate<Date> after(final Date value) {
         return new Predicate<Date>() {
 
             @Override
             public boolean apply(Date field) {
                 if (field == null) {
                     return false;
                 }
                 return field.after(value);
             }
 
         };
     }
 
     /**
      * If a date is after or equals.
      *
      * @param value
      * @return A date criteria
      */
     public static Predicate<Date> afterOrEquals(final Date value) {
         return new Predicate<Date>() {
 
             @Override
             public boolean apply(Date field) {
                 if (field == null) {
                     return false;
                 }
                 return field.after(value) || field.equals(value);
             }
 
         };
     }
 
     /**
      * If a a date is between two other dates.
      *
      * @param start
      * @param end
      * @return A date criteria
      */
     public static Predicate<Date> between(final Date start, final Date end) {
         return new Predicate<Date>() {
 
             @Override
             public boolean apply(Date field) {
                 if (field == null) {
                     return false;
                 }
                 boolean startMatch = field.after(start) || field.equals(start);
                 boolean endMatch = field.before(end) || field.equals(end);
                 return startMatch && endMatch;
             }
 
         };
     }
 
     /**
      * If a number is larger.
      *
      * @param value
      * @return A number criteria
      */
     public static Predicate<Number> largerThan(final Number value) {
         return new Predicate<Number>() {
 
             @Override
             public boolean apply(Number field) {
                 if (field == null) {
                     return false;
                 }
                 return field.doubleValue() > value.doubleValue();
             }
 
         };
     }
 
     /**
      * If a number is larger or equals.
      *
      * @param value
      * @return A number criteria
      */
     public static Predicate<Number> largerOrEquals(final Number value) {
         return new Predicate<Number>() {
 
             @Override
             public boolean apply(Number field) {
                 if (field == null) {
                     return false;
                 }
                 return field.doubleValue() >= value.doubleValue();
             }
 
         };
     }
 
     /**
      * If a number is less than.
      *
      * @param value
      * @return A number criteria
      */
     public static Predicate<Number> lessThan(final Number value) {
         return new Predicate<Number>() {
 
             @Override
             public boolean apply(Number field) {
                 if (field == null) {
                     return false;
                 }
                 return field.doubleValue() < value.doubleValue();
             }
 
         };
     }
 
     /**
      * If a number is less or equal.
      *
      * @param value
      * @return A number criteria
      */
     public static Predicate<Number> lessOrEquals(final Number value) {
         return new Predicate<Number>() {
 
             @Override
             public boolean apply(Number field) {
                 if (field == null) {
                     return false;
                 }
                 return field.doubleValue() < value.doubleValue();
             }
 
         };
     }
 
     /**
      * Evaluate if a Serializable if equal another Serializable.
      *
      * @param value
      * @return A Serializable criteria
      */
     public static Predicate<Serializable> equal(final Object value) {
         return new Predicate<Serializable>() {
 
             @Override
             public boolean apply(Serializable field) {
                 if (field == null) {
                     return value == null;
                 }
                 return field.equals(value);
             }
 
         };
     }
 
     /**
      * Evaluate if a string contains another string.
      *
      * @param value
      * @return A string criteria
      */
     public static Predicate<String> contains(final String value) {
         return new Predicate<String>() {
 
             @Override
             public boolean apply(String field) {
                 if (field == null) {
                     return false;
                 }
                 return field.contains(value);
             }
 
         };
     }
 
     /**
      * Evaluate if a string contains another string, case insensitive
      *
      * @param value
      * @return A string criteria
      */
     public static Predicate<String> containsNoCase(final String value) {
         return new Predicate<String>() {
 
             @Override
             public boolean apply(String field) {
                 if (field == null) {
                     return false;
                 }
                 return field.toLowerCase().contains(value.toLowerCase());
             }
 
         };
     }
 
     /**
      * Evaluate if a string starts with another string.
      *
      * @param value
      * @return A string criteria
      */
     public static Predicate<String> startsWith(final String value) {
         return new Predicate<String>() {
 
             @Override
             public boolean apply(String field) {
                 if (field == null) {
                     return false;
                 }
                 return field.startsWith(value);
             }
 
         };
     }
 
     /**
      * Evaluate if a string ends with another string.
      *
      * @param value
      * @return A string criteria
      */
     public static Predicate<String> endsWith(final String value) {
         return new Predicate<String>() {
 
             @Override
             public boolean apply(String field) {
                 if (field == null) {
                     return false;
                 }
                 return field.endsWith(value);
             }
 
         };
     }
 
     /**
      * Evaluate a regular expression.
      *
      * @param pattern
      * @return A string criteria
      */
     public static Predicate<String> regexp(final String pattern) {
         return new Predicate<String>() {
 
             @Override
             public boolean apply(String field) {
                 if (field == null) {
                     return false;
                 }
                 return field.matches(pattern);
             }
 
         };
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public boolean apply(Object input) {
         return p.apply(input);
     }
 
     private static class PredicateProxy implements Predicate {
         private final Predicate target;
         private final String fieldName;
 
         public PredicateProxy(String fieldName, Predicate target) {
             this.target = target;
             this.fieldName = fieldName;
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public boolean apply(Object object) {
             String[] fields = fieldName.split("\\.");
             byte[][] entity =  (byte[][]) object;
             if (fields.length == 1) {
                 return evaluateField(entity[1], fieldName);
             } else if (fields.length > 1) {
                 RowKey key = new RowKey(entity[0]);
                 EntityClassWrapper cls = EntityClassWrapper.get(key.getCls());
                 if (cls.isEmbedded(fields[0])) {
                     return evaluateEmdeddedFields(entity[1], fields);
                 } else if (cls.isReference(fields[0])) {
                     return evaluateReferenceFields(entity[1], fields, cls);
                 } else {
                     throw new IllegalStateException("Cannot handle field " + fieldName);
                 }
             } else {
                 throw new IllegalArgumentException("Cannot handle field " + fieldName);
             }
         }
 
         public boolean evaluateField(byte[] data, String fieldName) {
             Object value = getValue(data, fieldName);
             return target.apply(value);
         }
 
         public boolean evaluateEmdeddedFields(byte[] data, String[] fields) {
             if (fields.length > 2) {
                 throw new UnsupportedOperationException("Can only handle one deep embedded object ATM " + fields.length);
             }
             ValueReader reader = new ValueReader(data);
             int[][] header = reader.getHeader();
             int id = ids.getSchemaId(fields[0]);
             Object value = reader.getValue(id, header);
 
             reader = new ValueReader((byte[])value);
             header = reader.getHeader();
             id = ids.getSchemaId(fields[1]);
             value = reader.getValue(id, header);
             return target.apply(value);
         }
 
         public boolean evaluateReferenceFields(byte[] data, String[] fields, EntityClassWrapper cls) {
             if (fields.length > 2) {
                 throw new UnsupportedOperationException("Can only handle one deep reference object ATM " + fields.length);
             }
             ValueReader reader = new ValueReader(data);
             int[][] header = reader.getHeader();
             int id = ids.getSchemaId(fields[0]);
             Object instanceId = reader.getValue(id, header);
            RowKey key = new RowKey(cls.getReferences().get(fields[0]).getType(), instanceId);

            Optional<byte[][]> kv = repository.getKv(key, LockMode.DEFAULT);
             if (!kv.isPresent()) {
                 return false;
             }
            if (cls.getId().getField().getName().equals(fields[1])) {
                 String instance = new RowKey(kv.get()[0]).getInstance();
                 return target.apply(instance);
             } else {
                 return evaluateField(kv.get()[1], fields[1]);
             }
         }
 
         public Object getValue(byte[] data, String fieldName) {
             ValueReader reader = new ValueReader(data);
             int[][] header = reader.getHeader();
             return reader.getValue(ids.getSchemaId(fieldName), header);
         }
     }
 }
