 package fr.zenexity.dbhelper;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public abstract class Sql {
 
     private Sql() {
     }
 
     public static class Concat {
         private String prefix, localPrefix, separator, suffix;
         private String defaultValue;
         private String value;
 
         public Concat(String prefix, String separator, String suffix) {
             this.prefix = prefix;
             this.localPrefix = null;
             this.separator = separator;
             this.suffix = suffix;
             this.defaultValue = "";
             this.value = "";
         }
 
         public Concat(String prefix, String separator) {
             this(prefix, separator, "");
         }
 
         public Concat(Concat src) {
             this.prefix = src.prefix;
             this.localPrefix = src.localPrefix;
             this.separator = src.separator;
             this.suffix = src.suffix;
             this.defaultValue = src.defaultValue;
             this.value = src.value;
         }
 
         public Concat defaultValue(String defaultValue) {
             this.defaultValue = defaultValue;
             return this;
         }
 
         public Concat prefix(String prefix) {
             this.prefix = prefix;
             return this;
         }
 
         public Concat localPrefix(String localPrefix) {
             this.localPrefix = localPrefix;
             return this;
         }
 
         public Concat separator(String separator) {
             this.separator = separator;
             return this;
         }
 
         public Concat suffix(String suffix) {
             this.suffix = suffix;
             return this;
         }
 
         public Concat append(Object obj) {
             final String text;
             if (obj != null) {
                 String objStr = obj.toString();
                 if (objStr.length() > 0) text = objStr;
                 else text = defaultValue;
             } else text = defaultValue;
 
             if (text != null) {
                 if (value.length() > 0) {
                     if (separator == null) throw new NullPointerException();
                     value += separator;
                 } else if (localPrefix != null) {
                     value += localPrefix;
                     localPrefix = null;
                 }
                 value += text;
             }
             return this;
         }
 
         public Concat add(Object... objs) {
             for (Object obj : objs) append(obj);
             return this;
         }
 
         public boolean isEmpty() {
             return value.length()<=0;
         }
 
         @Override
         public String toString() {
             if (isEmpty()) return "";
             if (prefix == null || suffix == null) throw new NullPointerException();
             return prefix + value + suffix;
         }
 
         public String value() {
             return value;
         }
     }
 
     public static class ConcatWithParams extends Concat {
         public final List<Object> params;
 
         public ConcatWithParams(String prefix, String separator, String suffix) {
             super(prefix, separator, suffix);
             params = new ArrayList<Object>();
         }
 
         public ConcatWithParams(String prefix, String separator) {
             this(prefix, separator, "");
         }
 
         public ConcatWithParams(ConcatWithParams src) {
             super(src);
             params = new ArrayList<Object>(src.params);
         }
 
         public ConcatWithParams param(Object obj) {
             params.add(obj);
             return this;
         }
 
         public ConcatWithParams params(Object... objs) {
             for (Object obj : objs) params.add(obj);
             return this;
         }
 
         public ConcatWithParams paramsList(Iterable<?> objs) {
             for (Object obj : objs) params.add(obj);
             return this;
         }
     }
 
     public static class Where {
         private final ConcatWithParams query;
 
         public Where() {
             query = new ConcatWithParams("(", null, ")");
             query.defaultValue(null);
         }
 
         public Where(Where src) {
             query = new ConcatWithParams(src.query);
         }
 
         public Where and(String expr, Object... params) { query.params(params).separator(" AND ").append(expr); return this; }
         public Where and(Where where) { query.paramsList(where.query.params).separator(" AND ").append(where.toString()); return this; }
         public Where or(String expr, Object... params) { query.params(params).separator(" OR ").append(expr); return this; }
         public Where or(Where where) { query.paramsList(where.query.params).separator(" OR ").append(where.toString()); return this; }
 
         public static Where key(Object obj, String... keyFields) throws SqlException {
             Where keyWhere = new Where();
             Class<?> objClass = obj.getClass();
             try {
                 for (String field : keyFields) {
                     keyWhere.and(field+"=?", objClass.getDeclaredField(field).get(obj));
                 }
             } catch (Exception e) {
                 throw new SqlException(e);
             }
             return keyWhere;
         }
 
         public static Where key(Map<String, ?> map, String... keyFields) {
             Where keyWhere = new Where();
             for (String field : keyFields) keyWhere.and(field+"=?", map.get(field));
             return keyWhere;
         }
 
         @Override
         public String toString() {
             return query.toString();
         }
 
         public String value() {
             return query.value();
         }
 
         public Iterable<Object> params() {
             return query.params;
         }
 
         public List<Object> paramsList() {
             return new ArrayList<Object>(query.params);
         }
     }
 
     public interface Query {
         @Override String toString();
         Iterable<Object> params();
         List<Object> copyParams();
     }
 
     public interface UpdateQuery {
         @Override String toString();
         Iterable<Object> params();
         List<Object> copyParams();
     }
 
     public static final class FinalQuery implements Query {
         public final String query;
         public final List<Object> params;
 
         public FinalQuery(Query src) {
             query = src.toString();
             params = src.copyParams();
         }
 
         public FinalQuery(Query src, Object param) {
             this(src);
             this.params.add(param);
         }
 
         public FinalQuery(Query src, Object... params) {
             this(src);
             for (Object param : params) this.params.add(param);
         }
 
         public FinalQuery(Query src, Iterable<?> params) {
             this(src);
             for (Object param : params) this.params.add(param);
         }
 
         @Override
         public String toString() {
             return query;
         }
 
         public Iterable<Object> params() {
             return params;
         }
 
         public List<Object> copyParams() {
             return new ArrayList<Object>(params);
         }
     }
 
     public static final class FinalUpdateQuery implements UpdateQuery {
         public final String query;
         public final List<Object> params;
 
         public FinalUpdateQuery(UpdateQuery src) {
             query = src.toString();
             params = src.copyParams();
         }
 
         public FinalUpdateQuery(UpdateQuery src, Object param) {
             this(src);
             this.params.add(param);
         }
 
         public FinalUpdateQuery(UpdateQuery src, Object... params) {
             this(src);
             for (Object param : params) this.params.add(param);
         }
 
         public FinalUpdateQuery(UpdateQuery src, Iterable<?> params) {
             this(src);
             for (Object param : params) this.params.add(param);
         }
 
         @Override
         public String toString() {
             return query;
         }
 
         public Iterable<Object> params() {
             return params;
         }
 
         public List<Object> copyParams() {
             return new ArrayList<Object>(params);
         }
     }
 
     public static final class Select implements Query {
         private final Concat select;
         private final Concat from;
         private final ConcatWithParams join;
         public final Where where;
         private final Concat groupBy;
         private final ConcatWithParams having;
         private final Concat orderBy;
         private final Concat offset;
         private final Concat limit;
 
         public Select() {
             select = new Concat("SELECT ", ", ");
             select.defaultValue(null);
             from = new Concat("FROM ", ", ");
             from.defaultValue(null);
             join = new ConcatWithParams("", null);
             join.defaultValue(null);
             where = new Where();
             where.query.prefix("WHERE ").suffix("");
             groupBy = new Concat("GROUP BY ", ", ");
             groupBy.defaultValue(null);
             having = new ConcatWithParams("HAVING ", ", ");
             having.defaultValue(null);
             orderBy = new Concat("ORDER BY ", ", ");
             orderBy.defaultValue(null);
             offset = new Concat("OFFSET ", null);
             limit = new Concat("LIMIT ", null);
         }
 
         public Select(Select src) {
             select = new Concat(src.select);
             from = new Concat(src.from);
             join = new ConcatWithParams(src.join);
             where = new Where(src.where);
             groupBy = new Concat(src.groupBy);
             having = new ConcatWithParams(src.having);
             orderBy = new Concat(src.orderBy);
             offset = new Concat(src.offset);
             limit = new Concat(src.limit);
         }
 
         public Select select(Object expression) { select.append(expression); return this; }
         public Select select(Object... expressions) { select.add(expressions); return this; }
         public Select selectAll() { select.append("*"); return this; }
 
         public Select from(String table) { from.append(table); return this; }
         public Select from(String... tables) { from.add((Object[])tables); return this; }
         public Select from(Class<?> clazz) { from.append(clazz.getSimpleName()); return this; }
         public Select from(Class<?>... classes) { for (Class<?> clazz : classes) from.append(clazz.getSimpleName()); return this; }
 
         public Select join(String expr, Object... params) { join.params(params).localPrefix("JOIN ").separator(" JOIN ").append(expr); return this; }
        public Select join(String table, Where on) { return join(table +" ON "+ on.value(), on.params()); }
         public Select innerJoin(String expr, Object... params) { join.params(params).localPrefix("INNER JOIN ").separator(" INNER JOIN ").append(expr); return this; }
        public Select innerJoin(String table, Where on) { return innerJoin(table +" ON "+ on.value(), on.params()); }
         public Select leftJoin(String expr, Object... params) { join.params(params).localPrefix("LEFT JOIN ").separator(" LEFT JOIN ").append(expr); return this; }
        public Select leftJoin(String table, Where on) { return leftJoin(table +" ON "+ on.value(), on.params()); }
 
         public Select where(String expr, Object... params) { return andWhere(expr, params); }
         public Select where(Where expr) { return andWhere(expr); }
         public Select andWhere(String expr, Object... params) { where.and(expr, params); return this; }
         public Select andWhere(Where expr) { where.and(expr); return this; }
         public Select orWhere(String expr, Object... params) { where.or(expr, params); return this; }
         public Select orWhere(Where expr) { where.or(expr); return this; }
 
         public Select groupBy(Object column) { groupBy.append(column); return this; }
         public Select groupBy(Object... columns) { groupBy.add(columns); return this; }
 
         public Select having(String expr, Object... params) { having.params(params).append(expr); return this; }
         public Select having(Where expr) { having.paramsList(expr.params()).append(expr.toString()); return this; }
 
         public Select orderBy(Object column) { orderBy.append(column); return this; }
         public Select orderBy(Object... columns) { orderBy.add(columns); return this; }
 
         public Select offset(long start) { offset.append(start); return this; }
         public Select limit(long count) { limit.append(count); return this; }
 
         @Override
         public String toString() {
             if (select.isEmpty() && from.isEmpty() && join.isEmpty()) where.query.prefix("");
             return new Concat(""," ").defaultValue(null)
                     .append(select)
                     .append(from)
                     .append(join)
                     .append(where)
                     .append(groupBy)
                     .append(having)
                     .append(orderBy)
                     .append(offset)
                     .append(limit)
                     .toString();
         }
 
         public Iterable<Object> params() {
             return copyParams();
         }
 
         public List<Object> copyParams() {
             List<Object> list = new ArrayList<Object>();
             list.addAll(join.params);
             for (Object whereParam : where.params()) list.add(whereParam);
             list.addAll(having.params);
             return list;
         }
     }
 
     public static final class Union implements Query {
         private final ConcatWithParams union;
         private final Concat orderBy;
         private final Concat offset;
         private final Concat limit;
 
         public Union() {
             union = new ConcatWithParams("", null);
             orderBy = new Concat("ORDER BY ", ", ");
             orderBy.defaultValue(null);
             offset = new Concat("OFFSET ", null);
             limit = new Concat("LIMIT ", null);
         }
 
         public Union(Union src) {
             union = new ConcatWithParams(src.union);
             orderBy = new Concat(src.orderBy);
             offset = new Concat(src.offset);
             limit = new Concat(src.limit);
         }
 
         private void unionSep(String separator, Select query) {
             String sql = query.toString();
             if (sql.length() != 0) sql = "(" + sql + ")";
             union.paramsList(query.params()).separator(separator).append(sql);
         }
 
         public Union union(Select expr) { unionSep(" UNION ", expr); return this; }
         public Union unionAll(Select expr) { unionSep(" UNION ALL ", expr); return this; }
 
         public Union union(Select... expr) { for (Select e : expr) union(e); return this; }
         public Union unionAll(Select... expr) { for (Select e : expr) unionAll(e); return this; }
 
         public Union orderBy(Object column) { orderBy.append(column); return this; }
         public Union orderBy(Object... columns) { orderBy.add(columns); return this; }
 
         public Union offset(long start) { offset.append(start); return this; }
         public Union limit(long count) { limit.append(count); return this; }
 
         @Override
         public String toString() {
             return new Concat("", " ").defaultValue(null)
                     .append(union)
                     .append(orderBy)
                     .append(offset)
                     .append(limit)
                     .toString();
         }
 
         public Iterable<Object> params() {
             return union.params;
         }
 
         public List<Object> copyParams() {
             return new ArrayList<Object>(union.params);
         }
     }
 
     public static final class Insert implements UpdateQuery {
         private final Concat into;
         private final Concat columns;
         private final ConcatWithParams values;
 
         public Insert() {
             into = new Concat("INSERT INTO ", null);
             into.defaultValue(null);
             columns = new Concat("(", ", ", ")");
             values = new ConcatWithParams("VALUES (", ", ", ")");
         }
 
         public Insert(Insert src) {
             into = new Concat(src.into);
             columns = new Concat(src.columns);
             values = new ConcatWithParams(src.values);
         }
 
         public Insert replace() { into.prefix = "REPLACE INTO "; return this; }
 
         public Insert into(String table) { into.append(table); return this; }
         public Insert into(Class<?> clazz) { into.append(clazz.getSimpleName()); return this; }
 
         public Insert column(Object column) { columns.append(column); return this; }
         public Insert columns(Object... columns) { this.columns.add(columns); return this; }
 
         public Insert defaultValues() { values.prefix("").suffix("").separator(null).append("DEFAULT VALUES"); return this; }
         public Insert select(Select values) { this.values.paramsList(values.params()).prefix("").suffix("").separator(null).append(values.toString()); return this; }
         public Insert valueExpr(String value, Object... params) { values.params(params).append(value); return this; }
         public Insert value(Object value) { return valueExpr("?", value); }
         public Insert values(Object... values) { for (Object value : values) value(value); return this; }
 
         public Insert set(String column, Object value) { return column(column).value(value); }
         public Insert setExpr(String column, String value, Object... params) { return column(column).valueExpr(value, params); }
 
         public Insert object(Object obj, String... exceptFields) throws SqlException {
             Set<String> excepts = new HashSet<String>(exceptFields.length);
             for (String field : exceptFields) excepts.add(field);
 
             Class<?> objClass = obj.getClass();
             Field[] objFields = objClass.getFields();
             try {
                 for (Field objField : objFields) {
                     String fieldName = objField.getName();
                     if (excepts.contains(fieldName)) continue;
 
                     if ((objField.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0) {
                         set(fieldName, objField.get(obj));
                     }
                 }
             } catch (Exception e) {
                 throw new SqlException(e);
             }
             return this;
         }
 
         public Insert objectField(Object obj, String... fields) throws SqlException {
             Class<?> objClass = obj.getClass();
             try {
                 for (String field : fields) {
                     Field objField = objClass.getField(field);
                     if ((objField.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
                         throw new IllegalArgumentException(field);
                     set(field, objField.get(obj));
                 }
             } catch (Exception e) {
                 throw new SqlException(e);
             }
             return this;
         }
 
         public Insert map(Map<String,?> map) {
             for (Map.Entry<String,?> entry : map.entrySet()) set(entry.getKey(), entry.getValue());
             return this;
         }
 
         public Insert map(Map<String,?> map, String... fields) {
             for (String field : fields) set(field, map.get(field));
             return this;
         }
 
         public Insert setColumns(String... columns) {
             for (String column : columns) setExpr(column, "?");
             return this;
         }
 
         public Insert setColumns(Iterable<String> columns) {
             for (String column : columns) setExpr(column, "?");
             return this;
         }
 
         @Override
         public String toString() {
             return new Concat(""," ").defaultValue(null)
                     .append(into)
                     .append(columns)
                     .append(values)
                     .toString();
         }
 
         public Iterable<Object> params() {
             return values.params;
         }
 
         public List<Object> copyParams() {
             return new ArrayList<Object>(values.params);
         }
     }
 
     public static final class Update implements UpdateQuery {
         private final Concat update;
         private final ConcatWithParams set;
         public final Where where;
         private final Concat orderBy;
         private final Concat limit;
 
         public Update() {
             update = new Concat("UPDATE ", ", ");
             update.defaultValue(null);
             set = new ConcatWithParams("SET ", ", ");
             where = new Where();
             where.query.prefix("WHERE ").suffix("");
             orderBy = new Concat("ORDER BY ", ", ");
             orderBy.defaultValue(null);
             limit = new Concat("LIMIT ", null);
         }
 
         public Update(Update src) {
             update = new Concat(src.update);
             set = new ConcatWithParams(src.set);
             where = new Where(src.where);
             orderBy = new Concat(src.orderBy);
             limit = new Concat(src.limit);
         }
 
         public Update update(String table) { update.append(table); return this; }
         public Update update(String... tables) { update.add((Object[])tables); return this; }
         public Update update(Class<?> clazz) { update.append(clazz.getSimpleName()); return this; }
         public Update update(Class<?>... classes) { for (Class<?> clazz : classes) update.append(clazz.getSimpleName()); return this; }
 
         public Update set(String name, Object value) { set.param(value).append(name+"=?"); return this; }
         public Update setExpr(String name, String expr, Object... params) { set.params(params).append(name+"="+expr); return this; }
 
         public Update object(Object obj, String... exceptFields) throws SqlException {
             Set<String> excepts = new HashSet<String>(exceptFields.length);
             for (String field : exceptFields) excepts.add(field);
 
             Class<?> objClass = obj.getClass();
             Field[] objFields = objClass.getFields();
             try {
                 for (Field objField : objFields) {
                     String fieldName = objField.getName();
                     if (excepts.contains(fieldName)) continue;
 
                     if ((objField.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0) {
                         set(objField.getName(), objField.get(obj));
                     }
                 }
             } catch (Exception e) {
                 throw new SqlException(e);
             }
             return this;
         }
 
         public Update objectField(Object obj, String... fields) throws SqlException {
             Class<?> objClass = obj.getClass();
             try {
                 for (String field : fields) {
                     Field objField = objClass.getField(field);
                     if ((objField.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
                         throw new IllegalArgumentException(field);
                     set(field, objField.get(obj));
                 }
             } catch (Exception e) {
                 throw new SqlException(e);
             }
             return this;
         }
 
         public Update map(Map<String,?> map) {
             for (Map.Entry<String,?> entry : map.entrySet()) set(entry.getKey(), entry.getValue());
             return this;
         }
 
         public Update map(Map<String,?> map, String... fields) {
             for (String field : fields) set(field, map.get(field));
             return this;
         }
 
         public Update setColumns(String... columns) {
             for (String column : columns) setExpr(column, "?");
             return this;
         }
 
         public Update setColumns(Iterable<String> columns) {
             for (String column : columns) setExpr(column, "?");
             return this;
         }
 
         public Update where(String expr, Object... params) { return andWhere(expr, params); }
         public Update where(Where expr) { return andWhere(expr); }
         public Update andWhere(String expr, Object... params) { where.and(expr, params); return this; }
         public Update andWhere(Where expr) { where.and(expr); return this; }
         public Update orWhere(String expr, Object... params) { where.or(expr, params); return this; }
         public Update orWhere(Where expr) { where.or(expr); return this; }
 
         public Update orderBy(Object column) { orderBy.append(column); return this; }
         public Update orderBy(Object... columns) { orderBy.add(columns); return this; }
 
         public Update limit(long count) { limit.append(count); return this; }
 
         @Override
         public String toString() {
             return new Concat(""," ").defaultValue(null)
                     .append(update)
                     .append(set)
                     .append(where)
                     .append(orderBy)
                     .append(limit)
                     .toString();
         }
 
         public Iterable<Object> params() {
             return copyParams();
         }
 
         public List<Object> copyParams() {
             List<Object> list = new ArrayList<Object>();
             list.addAll(set.params);
             for (Object whereParam : where.params()) list.add(whereParam);
             return list;
         }
     }
 
     public static final class Delete implements UpdateQuery {
         private final String from;
         private final Concat using;
         public final Where where;
 
         public Delete(String from) {
             this(from, false);
         }
 
         public Delete(Class<?> from) {
             this(from.getSimpleName(), false);
         }
 
         public Delete(Class<?> from, boolean only) {
             this(from.getSimpleName(), only);
         }
 
         public Delete(String from, boolean only) {
             this.from = "DELETE FROM " + (only?"ONLY ":"") + from;
             using = new Concat("USING ", ", ");
             using.defaultValue(null);
             where = new Where();
             where.query.prefix("WHERE ").suffix("");
         }
 
         public Delete(Delete src) {
             from = new String(src.from);
             using = new Concat(src.using);
             where = new Where(src.where);
         }
 
         public Delete using(String table) { using.append(table); return this; }
         public Delete using(String... tables) { using.add((Object[])tables); return this; }
         public Delete using(Class<?> clazz) { using.append(clazz.getSimpleName()); return this; }
         public Delete using(Class<?>... classes) { for (Class<?> clazz : classes) using.append(clazz.getSimpleName()); return this; }
 
         public Delete where(String expr, Object... params) { return andWhere(expr, params); }
         public Delete where(Where expr) { return andWhere(expr); }
         public Delete andWhere(String expr, Object... params) { where.and(expr, params); return this; }
         public Delete andWhere(Where expr) { where.and(expr); return this; }
         public Delete orWhere(String expr, Object... params) { where.or(expr, params); return this; }
         public Delete orWhere(Where expr) { where.or(expr); return this; }
 
         @Override
         public String toString() {
             return new Concat(""," ").defaultValue(null)
                     .append(from)
                     .append(using)
                     .append(where)
                     .toString();
         }
 
         public Iterable<Object> params() {
             return where.params();
         }
 
         public List<Object> copyParams() {
             List<Object> list = new ArrayList<Object>();
             for (Object whereParam : where.params()) list.add(whereParam);
             return list;
         }
     }
 
     public static Select select() { return new Select(); }
     public static Select selectAll() { return new Select().selectAll(); }
     public static Select select(Object column) { return new Select().select(column); }
     public static Select select(Object... columns) { return new Select().select(columns); }
     public static Select from(String table) { return new Select().from(table); }
     public static Select from(String... tables) { return new Select().from(tables); }
     public static Select from(Class<?> clazz) { return new Select().from(clazz); }
     public static Select from(Class<?>... classes) { return new Select().from(classes); }
 
     public static Union union() { return new Union(); }
     public static Union union(Select expr) { return new Union().union(expr); }
     public static Union union(Select... expr) { return new Union().union(expr); }
     public static Union unionAll(Select expr) { return new Union().unionAll(expr); }
     public static Union unionAll(Select... expr) { return new Union().unionAll(expr); }
 
     public static Insert insert() { return new Insert(); }
     public static Insert insert(String table) { return new Insert().into(table); }
     public static Insert insert(Class<?> clazz) { return new Insert().into(clazz); }
     public static Insert replace() { return new Insert().replace(); }
     public static Insert replace(String table) { return new Insert().replace().into(table); }
     public static Insert replace(Class<?> clazz) { return new Insert().replace().into(clazz); }
 
     public static Update update() { return new Update(); }
     public static Update update(String table) { return new Update().update(table); }
     public static Update update(String... tables) { return new Update().update(tables); }
     public static Update update(Class<?> clazz) { return new Update().update(clazz); }
     public static Update update(Class<?>... classes) { return new Update().update(classes); }
 
     public static Delete delete(String table) { return new Delete(table); }
     public static Delete delete(Class<?> clazz) { return new Delete(clazz); }
     public static Delete deleteOnly(String table) { return new Delete(table, true); }
     public static Delete deleteOnly(Class<?> clazz) { return new Delete(clazz, true); }
 
     public static Where where() { return new Where(); }
     public static Where where(String expr, Object... params) { return new Where().and(expr, params); }
 
     public static Select clone(Select src) { return new Select(src); }
     public static Union  clone(Union  src) { return new Union (src); }
     public static Insert clone(Insert src) { return new Insert(src); }
     public static Update clone(Update src) { return new Update(src); }
     public static Delete clone(Delete src) { return new Delete(src); }
     public static Where  clone(Where  src) { return new Where (src); }
     public static FinalQuery clone(FinalQuery src) { return new FinalQuery(src); }
     public static FinalUpdateQuery clone(FinalUpdateQuery src) { return new FinalUpdateQuery(src); }
 
     public static FinalQuery finalQuery(Query src) { return new FinalQuery(src); }
     public static FinalQuery finalQuery(Query src, Object param) { return new FinalQuery(src, param); }
     public static FinalQuery finalQuery(Query src, Object... params) { return new FinalQuery(src, params); }
     public static FinalQuery finalQuery(Query src, Iterable<?> params) { return new FinalQuery(src, params); }
     public static FinalUpdateQuery finalQuery(UpdateQuery src) { return new FinalUpdateQuery(src); }
     public static FinalUpdateQuery finalQuery(UpdateQuery src, Object param) { return new FinalUpdateQuery(src, param); }
     public static FinalUpdateQuery finalQuery(UpdateQuery src, Object... params) { return new FinalUpdateQuery(src, params); }
     public static FinalUpdateQuery finalQuery(UpdateQuery src, Iterable<?> params) { return new FinalUpdateQuery(src, params); }
 
     public static String quote(String str) {
         return "'" + str.replace("'","\\'") + "'";
     }
 
     public static String inlineParam(Object param) {
         if (param == null) return "NULL";
 
         String str;
         if (param instanceof String) str = quote(param.toString());
         else if (param instanceof Iterable<?>) {
             Concat list = new Concat("(", ", ", ")");
             for (Object p : (Iterable<?>)param) list.append(inlineParam(p));
             str = list.toString();
         } else if (param instanceof Object[]) {
             Concat list = new Concat("(", ", ", ")");
             for (Object p : (Object[])param) list.append(inlineParam(p));
             str = list.toString();
         } else if (param instanceof Enum<?>) {
             str = quote(param.toString());
         } else str = param.toString();
         return str;
     }
 
     public static String whereIn(String column, Object param) {
         String value = inlineParam(param);
         if (value.length() == 0) return value;
 
         String operator;
         if (param instanceof Object[]) {
             operator = " in ";
         } else if (param instanceof Iterable<?>) {
             operator = " in ";
         } else {
             operator = "=";
         }
 
         return column + operator + value;
     }
 
 }
