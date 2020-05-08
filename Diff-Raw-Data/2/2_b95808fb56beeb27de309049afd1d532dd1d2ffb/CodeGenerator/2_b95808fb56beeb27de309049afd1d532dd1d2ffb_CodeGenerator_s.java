 package net.jonp.sorm.codegen;
 
 import java.io.PrintWriter;
 import java.sql.PreparedStatement;
 import java.util.Collection;
 import java.util.Date;
 
 import net.jonp.sorm.codegen.model.Field;
 import net.jonp.sorm.codegen.model.NamedQuery;
 import net.jonp.sorm.codegen.model.Query;
 import net.jonp.sorm.codegen.model.QueryParam;
 import net.jonp.sorm.codegen.model.Sorm;
 
 /**
  * Given a {@link Sorm} description, generates source for a Sorm class.
  */
 public class CodeGenerator
     implements Runnable
 {
     private static final String OBJ = "obj";
     private static final String KEY = "key";
     private static final String LHS = "lhs";
     private static final String RHS = "rhs";
 
     private static final String INDENT = "    ";
 
     private PrintWriter out;
     private Sorm sorm;
 
     private int indent = 0;
     private String indentString = "";
 
     public CodeGenerator()
     {
         // Nothing to do
     }
 
     public PrintWriter getOut()
     {
         return out;
     }
 
     public void setOut(final PrintWriter out)
     {
         this.out = out;
     }
 
     public Sorm getSorm()
     {
         return sorm;
     }
 
     public void setSorm(final Sorm sorm)
     {
         this.sorm = sorm;
     }
 
     protected void writeln()
     {
         out.println();
     }
 
     protected void writeln(final String fmt, final Object... args)
     {
         String line = String.format(fmt, args);
         final String trimmed = line.trim();
         if (trimmed.startsWith("extends ") || trimmed.startsWith("implements ") || trimmed.startsWith("throws ")) {
             line = INDENT + line;
         }
 
         if (trimmed.startsWith("}")) {
             decIndent();
         }
 
         line = indentString + line;
         out.println(line);
 
         if (trimmed.endsWith("{")) {
             incIndent();
         }
     }
 
     public void run()
     {
         writeln("/*");
         writeln(" * GENERATED CODE");
         writeln(" * DO NOT EDIT");
         writeln(" *");
         writeln(" * Generated on %1$tF at %1$tT", new Date());
         writeln(" */");
         writeln();
         writeln("package %s;", sorm.getPkg());
         writeln();
 
         writeln("import java.sql.PreparedStatement;");
         writeln("import java.sql.ResultSet;");
         writeln("import java.sql.SQLException;");
         writeln("import java.sql.Types;");
         writeln("import java.util.ArrayList;");
         writeln("import java.util.Arrays;");
         writeln("import java.util.Collection;");
         writeln("import java.util.Iterator;");
         writeln("import java.util.LinkedHashMap;");
         writeln("import java.util.LinkedList;");
         writeln("import java.util.Map;");
         writeln("import java.util.NoSuchElementException;");
         writeln();
 
         writeln("import net.jonp.sorm.SormIterable;");
         writeln("import net.jonp.sorm.SormIterator;");
         writeln("import net.jonp.sorm.SormObject;");
         writeln("import net.jonp.sorm.SormSession;");
         writeln("import net.jonp.sorm.SormSQLException;");
         writeln("import net.jonp.sorm.UnknownDialectException;");
         writeln();
 
         // FUTURE: Support other logging frameworks
         writeln("import org.apache.log4j.Logger;");
         writeln();
 
         writeln("%s class %s", sorm.getAccessor(), sorm.getName());
         if (null != sorm.getSuper()) {
             writeln("extends %s", sorm.getSuper());
         }
         writeln("implements SormObject");
         writeln("{");
         dumpOrm();
         writeln();
 
         dumpFields();
         writeln();
 
         writeln("public %s()", sorm.getName());
         writeln("{");
         writeln("// Nothing to do");
         writeln("}");
         writeln();
 
         dumpAccessors();
         writeln("}");
     }
 
     private void dumpOrm()
     {
         writeln("%s static class Orm", sorm.getOrm_accessor());
         writeln("{");
         writeln("static final Logger LOG = Logger.getLogger(Orm.class);");
         writeln();
 
         writeln("private Orm()");
         writeln("{");
         writeln("// Nothing to do");
         writeln("}");
 
         if (!sorm.getCreate().isEmpty()) {
             writeln();
             dumpOrmCreate();
             if (!sorm.getPk().isEmpty()) {
                 writeln();
                 dumpOrmPk();
             }
         }
 
         if (!sorm.getRead().isEmpty()) {
             writeln();
             dumpOrmRead();
         }
 
         if (!sorm.getUpdate().isEmpty()) {
             writeln();
             dumpOrmUpdate();
         }
 
         if (!sorm.getDelete().isEmpty()) {
             writeln();
             dumpOrmDelete();
         }
 
         for (final Field field : sorm.getFields()) {
             if (field.getLink().getMode() == LinkMode.ManyToMany && field.getLink().getCollection() != null) {
                 writeln();
                 dumpOrmMapRead(field);
 
                 if (field.getLink().getCollection().getCreate() != null) {
                     writeln();
                     dumpOrmMapCreate(field);
                 }
 
                 if (field.getLink().getCollection().getDelete() != null) {
                     writeln();
                     dumpOrmMapDelete(field);
                 }
             }
         }
 
         for (final NamedQuery nq : sorm.getQueries()) {
             writeln();
             dumpOrmQuery(nq);
         }
 
         writeln("}");
     }
 
     private void dumpOrmCreate()
     {
         writeln("public static void create(final SormSession session, final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("create(session, Arrays.asList(%ss));", OBJ);
         writeln("}");
         writeln();
 
         writeln("public static void create(final SormSession session, final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("for (final %s %s : %ss)", sorm.getName(), OBJ, OBJ);
         writeln("{");
         writeln("create(session, %s);", OBJ);
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("public static void create(final SormSession session, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
 
         dumpPreparedStatement(sorm.getCreate(), OBJ);
         writeln();
 
         writeln("ps.executeUpdate();");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
 
         if (sorm.getPk().size() > 0) {
             writeln();
             final Field primary = sorm.getPrimaryField();
             writeln("final %s %s = getPk(session);", primary.getType(), KEY);
             writeln("%s.%s(%s);", OBJ, primary.getSet().getName(), KEY);
             writeln();
             writeln("session.cacheAdd(%s.class, %s, %s);", sorm.getName(), KEY, OBJ);
         }
 
         writeln("}");
     }
 
     private void dumpOrmPk()
     {
         final Field primary = sorm.getPrimaryField();
         writeln("private static %s getPk(final SormSession session)", primary.getType());
         writeln("throws SQLException");
         writeln("{");
 
         dumpPreparedStatement(sorm.getPk(), null);
         writeln();
 
         writeln("final ResultSet rs = ps.executeQuery();");
         writeln("try");
         writeln("{");
         writeln("rs.next();");
         writeln("return rs.%s(\"id\");", primary.getSql_type().getter);
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("rs.close();");
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
     }
 
     private void dumpOrmRead()
     {
         final Field primary = sorm.getPrimaryField();
 
         writeln("public static Collection<%s> read(final SormSession session, final %s... %ss)", sorm.getName(), primary.getType(),
                 KEY);
         writeln("throws SQLException");
         writeln("{");
         writeln("return read(session, Arrays.asList(%ss));", KEY);
         writeln("}");
         writeln();
 
         writeln("public static Collection<%s> read(final SormSession session, final Collection<%s> %ss)", sorm.getName(), primary
             .getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
 
         // Avoid building a PreparedStatement if possible, by scanning for all
         // values in the cache; If we cannot find them all, we can still use
         // this local copy of the cache to populate some
         writeln("final Map<%s, %s> cached = new LinkedHashMap<%s, %s>();", primary.getType(), sorm.getName(), primary.getType(),
                 sorm.getName());
         writeln("for (final %s %s : %ss)", primary.getType(), KEY, KEY);
         writeln("{");
         writeln("final %s %s = session.cacheGet(%s.class, %s);", sorm.getName(), OBJ, sorm.getName(), KEY);
         writeln("if (null != %s)", OBJ);
         writeln("{");
         writeln("cached.put(%s, %s);", KEY, OBJ);
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("if (cached.size() == %ss.size())", KEY);
         writeln("{");
         writeln("return cached.values();");
         writeln("}");
         writeln();
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getRead());
         writeln();
 
         writeln("final Collection<%s> %ss = new ArrayList<%s>(%ss.size());", sorm.getName(), OBJ, sorm.getName(), KEY);
         writeln("try");
         writeln("{");
         writeln("for (final %s %s : %ss)", primary.getType(), KEY, KEY);
         writeln("{");
         writeln("%s %s = cached.remove(%s);", sorm.getName(), OBJ, KEY);
         writeln("if (null == %s)", OBJ);
         writeln("{");
         writeln("%s = readSingle(ps, %s);", OBJ, KEY);
         writeln("session.cacheAdd(%s.class, %s, %s);", sorm.getName(), KEY, OBJ);
         writeln("}");
         writeln();
         writeln("if (null != %s)", OBJ);
         writeln("{");
         writeln("%ss.add(%s);", OBJ, OBJ);
         writeln("}");
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln();
         writeln("return %ss;", OBJ);
         writeln("}");
         writeln();
 
         writeln("public static %s read(final SormSession session, final %s %s)", sorm.getName(), primary.getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
 
         // Check the cache before building a PreparedStatement
         writeln("%s %s = session.cacheGet(%s.class, %s);", sorm.getName(), OBJ, sorm.getName(), KEY);
         writeln("if (null == %s)", OBJ);
         writeln("{");
         // Not in the cache, read from the DB
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getRead());
         writeln();
 
         writeln("try");
         writeln("{");
         writeln("%s = readSingle(ps, %s);", OBJ, KEY);
         writeln("session.cacheAdd(%s.class, %s, %s);", sorm.getName(), KEY, OBJ);
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
         writeln();
         writeln("return %s;", OBJ);
         writeln("}");
         writeln();
 
         writeln("static %s readSingle(final PreparedStatement ps, final %s %s)", sorm.getName(), primary.getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
 
         populatePreparedStatement(sorm.getRead(), KEY);
         writeln();
 
         writeln("final ResultSet rs = ps.executeQuery();");
         writeln("try");
         writeln("{");
         writeln("final %s %s;", sorm.getName(), OBJ);
         writeln("if (!rs.next())");
         writeln("{");
         writeln("%s = null;", OBJ);
         writeln("}");
         writeln("else");
         writeln("{");
         writeln("%s = new %s();", OBJ, sorm.getName());
 
         for (final Field f : sorm.getFields()) {
             // FUTURE: Figure out a way to avoid requiring the query to
             // return all fields
             if (f == primary) {
                 writeln("%s.%s(%s);", OBJ, primary.getSet().getName(), KEY);
             }
             else {
                 compileFromRS(f, OBJ);
             }
         }
 
         writeln("}");
         writeln();
 
         writeln("return %s;", OBJ);
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("rs.close();");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("public static SormIterable<%s> matches(final SormSession session, final Collection<%s> %ss)", sorm.getName(),
                 primary.getType(), KEY);
         writeln("{");
         writeln("return new SormIterable<%s>()", sorm.getName());
         writeln("{");
         writeln("public SormIterator<%s> iterator()", sorm.getName());
         writeln("{");
         writeln("return new SormIterator<%s>()", sorm.getName());
         writeln("{");
         writeln("private %s nextRow;", sorm.getName());
         writeln("private boolean nextKnown = false;");
         writeln("private final Iterator<%s> it%ss = %ss.iterator();", primary.getType(), KEY, KEY);
         writeln("private final PreparedStatement ps;");
         writeln();
 
         writeln("{");
         writeln("try");
         writeln("{");
         buildPreparedStatement(sorm.getRead());
         writeln("}");
         writeln("catch (final SQLException sqle)");
         writeln("{");
         writeln("throw new SormSQLException(sqle);");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("public boolean hasNext()");
         writeln("{");
         writeln("if (!nextKnown)");
         writeln("{");
         writeln("if (it%ss.hasNext())", KEY);
         writeln("{");
         writeln("final %s %s = it%ss.next();", primary.getType(), KEY, KEY);
         writeln("try");
         writeln("{");
         writeln("%s %s = session.cacheGet(%s.class, %s);", sorm.getName(), OBJ, sorm.getName(), KEY);
         writeln("if (null == %s)", OBJ);
         writeln("{");
         writeln("%s = readSingle(ps, %s);", OBJ, KEY);
         writeln("session.cacheAdd(%s.class, %s, %s);", sorm.getName(), KEY, OBJ);
         writeln("}");
         writeln();
 
         writeln("nextRow = %s;", OBJ);
         writeln("}");
         writeln("catch (final SQLException sqle)");
         writeln("{");
         writeln("throw new SormSQLException(sqle);");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("nextKnown = true;");
         writeln("}");
         writeln();
 
         writeln("return (null != nextRow);");
         writeln("}");
         writeln();
 
         writeln("public %s next()", sorm.getName());
         writeln("{");
         writeln("if (!hasNext())");
         writeln("{");
         writeln("throw new NoSuchElementException();");
         writeln("}");
         writeln();
 
         writeln("final %s row = nextRow;", sorm.getName());
         writeln("nextRow = null;");
         writeln("nextKnown = false;");
         writeln("return row;");
         writeln("}");
         writeln();
 
         writeln("public void remove()");
         writeln("{");
         writeln("throw new UnsupportedOperationException(\"remove() not supported\");");
         writeln("}");
         writeln();
 
         writeln("public void close()");
         writeln("throws SQLException");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("};");
         writeln("}");
         writeln("};");
         writeln("}");
     }
 
     private void dumpOrmUpdate()
     {
         writeln("public static void update(final SormSession session, final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("update(session, Arrays.asList(%ss));", OBJ);
         writeln("}");
         writeln();
 
         writeln("public static void update(final SormSession session, final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getUpdate());
         writeln();
 
         writeln("try");
         writeln("{");
         writeln("for (final %s %s : %ss)", sorm.getName(), OBJ, OBJ);
         writeln("{");
         writeln("updateSingle(ps, %s);", OBJ);
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("public static void update(final SormSession session, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getUpdate());
         writeln();
 
         writeln("try");
         writeln("{");
         writeln("updateSingle(ps, %s);", OBJ);
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("static void updateSingle(final PreparedStatement ps, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         populatePreparedStatement(sorm.getUpdate(), OBJ);
         writeln();
 
         writeln("ps.executeUpdate();");
         writeln("}");
     }
 
     private void dumpOrmDelete()
     {
         final Field primary = sorm.getPrimaryField();
 
         // TODO: Allow deletion by key
         writeln("public static void delete(final SormSession session, final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("delete(session, Arrays.asList(%ss));", OBJ);
         writeln("}");
         writeln();
 
         writeln("public static void delete(final SormSession session, final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getDelete());
         writeln();
 
         writeln("try");
         writeln("{");
         writeln("for (final %s %s : %ss)", sorm.getName(), OBJ, OBJ);
         writeln("{");
         writeln("deleteSingle(ps, %s);", OBJ);
         writeln("session.cacheDel(%s.class, %s.%s());", sorm.getName(), OBJ, primary.getGet().getName());
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("public static void delete(final SormSession session, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getDelete());
         writeln();
 
         writeln("try");
         writeln("{");
         writeln("deleteSingle(ps, %s);", OBJ);
         writeln("session.cacheDel(%s.class, %s.%s());", sorm.getName(), OBJ, primary.getGet().getName());
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("static void deleteSingle(final PreparedStatement ps, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         populatePreparedStatement(sorm.getDelete(), OBJ);
         writeln();
 
         writeln("ps.executeUpdate();");
         writeln("}");
     }
 
     private void dumpOrmMapRead(final Field field)
     {
         writeln("public static Collection<%s> readMapped%s(final SormSession session, final %s %s)", field.getLink().getType(),
                 StringUtil.capFirst(field.getName()), sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(field.getLink().getCollection().getRead());
         writeln();
 
         writeln("final Collection<%s> %ss = new LinkedList<%s>();", field.getLink().getKey_type(), KEY, field.getLink()
             .getKey_type());
         writeln("try");
         writeln("{");
         populatePreparedStatement(field.getLink().getCollection().getRead(), OBJ);
         writeln();
 
         writeln("final ResultSet rs = ps.executeQuery();");
         writeln("try");
         writeln("{");
         writeln("while (rs.next())");
         writeln("{");
         writeln("final %s %s = rs.%s(\"id\");", field.getLink().getKey_type(), KEY, field.getLink().getSql_type().getter);
         writeln("%ss.add(%s);", KEY, KEY);
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("rs.close();");
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln();
         writeln("return %s.Orm.read(session, %ss);", field.getLink().getType(), KEY);
         writeln("}");
     }
 
     private void dumpOrmMapCreate(final Field field)
     {
         writeln("public static void map%s(final SormSession session, final %s %s, final %s %s)", StringUtil.capFirst(field
             .getName()), sorm.getName(), LHS, field.getLink().getType(), RHS);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(field.getLink().getCollection().getCreate());
         writeln();
 
         writeln("try");
         writeln("{");
         populatePreparedStatement(field.getLink().getCollection().getCreate(), null);
         writeln();
 
         writeln("ps.executeUpdate();");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
     }
 
     private void dumpOrmMapDelete(final Field field)
     {
         writeln("public static void unmap%s(final SormSession session, final %s %s, final %s %s)", StringUtil.capFirst(field
             .getName()), sorm.getName(), LHS, field.getLink().getType(), RHS);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(field.getLink().getCollection().getDelete());
         writeln();
 
         writeln("try");
         writeln("{");
         populatePreparedStatement(field.getLink().getCollection().getDelete(), null);
         writeln();
 
         writeln("ps.executeUpdate();");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
     }
 
     private void dumpOrmQuery(final NamedQuery nq)
     {
         final Field primary = sorm.getPrimaryField();
 
         final StringBuilder buf = new StringBuilder();
         buf.append(String.format("%s static Collection<%s> %s(final SormSession session", nq.getAccessor(), primary.getType(), nq
             .getName()));
         for (final QueryParam param : nq.getParams()) {
             buf.append(String.format(", final %s %s", param.getType(), param.getName()));
         }
         buf.append(")");
         writeln("%s", buf);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final Collection<%s> %ss= new LinkedList<%s>();", primary.getType(), KEY, primary.getType());
         dumpPreparedStatement(nq);
         writeln();
 
         writeln("final ResultSet rs = ps.executeQuery();");
         writeln("try");
         writeln("{");
         writeln("while (rs.next())");
         writeln("{");
         writeln("%ss.add(rs.%s(\"%s\"));", KEY, primary.getSql_type().getter, primary.getSql_column());
         writeln("}");
         writeln();
         writeln("return %ss;", KEY);
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("rs.close();");
         writeln("}");
         writeln("}");
         writeln("finally");
         writeln("{");
         writeln("ps.close();");
         writeln("}");
         writeln("}");
     }
 
     private void dumpFields()
     {
         for (final Field field : sorm.getFields()) {
             if (field.isFromSuper()) {
                 continue;
             }
 
             writeln("%s %s _%s;", field.getAccessor(), field.getType(), field.getName());
         }
     }
 
     private void dumpAccessors()
     {
         boolean first = true;
         for (final Field field : sorm.getFields()) {
             if (field.isFromSuper()) {
                 continue;
             }
 
             if (first) {
                 first = false;
             }
             else {
                 writeln();
             }
 
             if (!field.getGet().isFromSuper()) {
                 if (field.getGet().isOverride()) {
                     writeln("@Override");
                 }
                 writeln("%s %s %s()", field.getGet().getAccessor(), field.getType(), field.getGet().getName());
                 writeln("{");
                 writeln("return _%s;", field.getName());
                 writeln("}");
                 writeln();
             }
 
             if (!field.getSet().isFromSuper()) {
                 if (field.getSet().isOverride()) {
                     writeln("@Override");
                 }
                 writeln("%s void %s(final %s %s)", field.getSet().getAccessor(), field.getSet().getName(), field.getType(), field
                     .getName());
                 writeln("{");
                 writeln("_%s = %s;", field.getName(), field.getName());
                 writeln("}");
             }
 
             // TODO: Dump special accessors for linked fields (adders/removers,
             // lazy-access types)
         }
     }
 
     /**
      * Dump a {@link PreparedStatement} declaration and initialization
      * (including setters).
      * 
      * @param query The query from which to build the statement.
      * @param objname The name of the object from which to read the fields for
      *            the setters.
      */
     private void dumpPreparedStatement(final Query query, final String objname)
     {
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(query);
         writeln();
         writeln("try");
         writeln("{");
         populatePreparedStatement(query, objname);
     }
 
     private void dumpPreparedStatement(final NamedQuery nq)
     {
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(nq.getQuery());
         writeln();
         writeln("try");
         writeln("{");
         populatePreparedStatement(nq);
     }
 
     private void buildPreparedStatement(final Query query)
     {
         final Collection<String> dialects = query.getDialects();
         final boolean hasAny = dialects.remove("*");
         if (!dialects.isEmpty()) {
             boolean first = true;
             for (final String dialect : dialects) {
                 final String els;
                 if (first) {
                     first = false;
                     els = "";
                 }
                 else {
                     els = "else ";
                 }
 
                 writeln("%sif (\"%s\".equals(session.getDialect()))", els, dialect);
                 writeln("{");
                 writeln("final String sql =");
                 incIndent();
                 writeln("\"%s\";", compileSql(query.getQuery(dialect)));
                 decIndent();
                 writeln("LOG.debug(\"SQL statement:\\n\" + sql);");
                 writeln("ps = session.getConnection().prepareStatement(sql);");
                 writeln("}");
             }
 
             writeln("else");
             writeln("{");
         }
 
         if (hasAny) {
             writeln("final String sql =");
             incIndent();
             writeln("\"%s\";", compileSql(query.getQuery("*")));
             decIndent();
             writeln("LOG.debug(\"SQL statement:\\n\" + sql);");
             writeln("ps = session.getConnection().prepareStatement(sql);");
         }
         else {
             writeln("throw new UnknownDialectException(session.getDialect());");
         }
 
         if (!dialects.isEmpty()) {
             writeln("}");
         }
     }
 
     private void populatePreparedStatement(final Query query, final String objname)
     {
         final Collection<String> dialects = query.getDialects();
         final boolean hasAny = dialects.remove("*");
         if (!dialects.isEmpty()) {
             boolean first = true;
             for (final String dialect : dialects) {
                 final String els;
                 if (first) {
                     first = false;
                     els = "";
                 }
                 else {
                     els = "else ";
                 }
 
                 writeln("%sif (\"%s\".equals(session.getDialect()))", els, dialect);
                 writeln("{");
                 if (!buildSets(query.getQuery(dialect), objname)) {
                     writeln("// Nothing necessary for this dialect");
                 }
                 writeln("}");
             }
 
             if (hasAny) {
                 writeln("else");
                 writeln("{");
             }
         }
 
         if (hasAny) {
             if (!buildSets(query.getQuery("*"), objname)) {
                 writeln("// No ps.set* calls neccessary");
             }
 
             if (!dialects.isEmpty()) {
                 writeln("}");
             }
         }
     }
 
     private void populatePreparedStatement(final NamedQuery nq)
     {
         final Collection<String> dialects = nq.getQuery().getDialects();
         final boolean hasAny = dialects.remove("*");
         if (!dialects.isEmpty()) {
             boolean first = true;
             for (final String dialect : dialects) {
                 final String els;
                 if (first) {
                     first = false;
                     els = "";
                 }
                 else {
                     els = "else ";
                 }
 
                 writeln("%sif (\"%s\".equals(session.getDialect()))", els, dialect);
                 writeln("{");
                 if (!buildSets(nq, dialect)) {
                     writeln("// Nothing necessary for this dialect");
                 }
                 writeln("}");
             }
 
             if (hasAny) {
                 writeln("else");
                 writeln("{");
             }
         }
 
         if (hasAny) {
             if (!buildSets(nq, "*")) {
                 writeln("// No ps.set* calls neccessary");
             }
 
             if (!dialects.isEmpty()) {
                 writeln("}");
             }
         }
     }
 
     private String compileSql(String query)
     {
         query = query.replaceAll("%\\{\\}", "?");
         for (final Field field : sorm.getFields()) {
             query = query.replaceAll("%\\{" + field.getName() + "\\}", "?");
             query = query.replaceAll("%\\{1\\." + field.getName() + "\\}", "?");
             query = query.replaceAll("%\\{2\\." + field.getName() + "\\}", "?");
         }
 
         // Fix embedded newlines
         query = query.replaceAll("\r?\n", String.format("\\\\n\" +%n" + indentString + "\""));
 
         return query;
     }
 
     private String compileAccessor(final String query, final String objName)
     {
         return query.replaceAll("%\\{\\}", objName);
     }
 
     private void compileFromRS(final Field f, final String objname)
     {
         // Cannot get *-to-many fields from an individual ResultSet row
         if (!(f.getLink().getMode() == LinkMode.OneToMany || f.getLink().getMode() == LinkMode.ManyToMany)) {
             // Convert all %{} into objname
             String s = f.getSet().getContent();
             s = s.replaceAll("%\\{\\}", objname);
 
             // Convert all %{xxx} into rs.get*("xxx")
             for (final Field field : sorm.getFields()) {
                 if (s.contains("%{" + field.getName() + "}")) {
                     s = s.replaceAll("%\\{" + field.getName() + "\\}", //
                                      String.format("rs.%s(\"%s\")", field.getSql_type().getter, field.getSql_column()));
                 }
             }
 
             writeln("%s;", s);
         }
     }
 
     /**
      * Build set statements for a {@link PreparedStatement}.
      * 
      * @param query The query for which to build the sets.
      * @param objname The name of the object that may be referenced by the
      *            query.
      * @return True if any set statements were written, false if none.
      */
     private boolean buildSets(final String query, final String objname)
     {
         boolean wroteSet = false;
         Character quote = null;
         int arg = 1;
         final char[] chars = query.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             final char c = chars[i];
             if (null != quote && c == quote) {
                 quote = null;
             }
             else if (null == quote) {
                 if ('\'' == c || '\"' == c || '`' == c) {
                     quote = c;
                 }
                 else if ('%' == c) {
                     final String start = query.substring(i);
                     if (start.startsWith("%{}")) {
                         dumpSet(sorm.getPrimaryField(), arg++, KEY);
                         wroteSet = true;
                     }
                     else {
                         for (final Field field : sorm.getFields()) {
                             if (start.startsWith("%{" + field.getName() + "}")) {
                                 final String accessor = compileAccessor(field.getGet().getContent(), objname);
                                 dumpSet(field, arg++, accessor);
                                 wroteSet = true;
                             }
                             else if (start.startsWith("%{1." + field.getName() + "}")) {
                                 final String accessor = compileAccessor(field.getGet().getContent(), LHS);
                                 dumpSet(field, arg++, accessor);
                                 wroteSet = true;
                             }
                             else if (start.startsWith("%{2." + field.getName() + "}")) {
                                 final String accessor = compileAccessor(field.getGet().getContent(), RHS);
                                 dumpSet(field, arg++, accessor);
                                 wroteSet = true;
                             }
                         }
                     }
                 }
             }
         }
 
         return wroteSet;
     }
 
     /**
      * Build set statements for a {@link PreparedStatement}.
      * 
      * @param nq The named query for which to build the sets.
      * @param dialect The dialect for which to build the sets.
      * @return True if any set statements were written, false if none.
      */
     private boolean buildSets(final NamedQuery nq, final String dialect)
     {
         boolean wroteSet = false;
         Character quote = null;
         int arg = 1;
         final String query = nq.getQuery().getQuery(dialect);
         final char[] chars = query.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             final char c = chars[i];
             if (null != quote && c == quote) {
                 quote = null;
             }
             else if (null == quote) {
                 if ('\'' == c || '\"' == c || '`' == c) {
                     quote = c;
                 }
                 else if ('%' == c) {
                     final String start = query.substring(i);
                     for (final QueryParam param : nq.getParams()) {
                         if (start.startsWith("%{" + param.getName() + "}")) {
                             // FIXME: Allow for nullable parameters, then use
                             // something like dumpSet() to test for it
                             final String accessor = compileAccessor(param.getSet().getContent(), param.getName());
                             writeln("LOG.debug(\"  Param %d: \" + %s);", arg, accessor);
                             writeln("ps.%s(%d, %s);", param.getSql_type().setter, arg++, accessor);
                             wroteSet = true;
                         }
                     }
                 }
             }
         }
 
         return wroteSet;
     }
 
     /**
      * Dump a set block for a single field at a specified Prepared Statement
      * position.
      * 
      * @param field The field.
      * @param arg The position.
      * @param accessor The accessor that provides the field value.
      */
     private void dumpSet(final Field field, final int arg, final String accessor)
     {
         writeln("LOG.debug(\"  Param %d: \" + %s);", arg, accessor);
         if (field.isNullable()) {
             writeln("if (null == %s)", accessor);
             writeln("{");
             writeln("ps.setNull(%d, Types.%s);", arg, field.getSql_type().sqltype);
             writeln("}");
             writeln("else");
             writeln("{");
         }
 
         writeln("ps.%s(%d, %s);", field.getSql_type().setter, arg, accessor);
 
         if (field.isNullable()) {
             writeln("}");
         }
     }
 
     private void incIndent()
     {
         indent++;
         indentString += INDENT;
     }
 
     private void decIndent()
     {
         indent--;
         indentString = StringUtil.multiplyString(INDENT, indent);
     }
 }
