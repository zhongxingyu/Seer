 package net.jonp.sorm.codegen;
 
 import java.io.PrintWriter;
 import java.sql.PreparedStatement;
 import java.util.Collection;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
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
 
     private static final Pattern P_RHSMAP = Pattern.compile("^%\\{2\\.(\\w|_)((?:\\w|\\d)*)\\(\\):(nullable )?(\\w+)\\}.*",
                                                             Pattern.DOTALL);
 
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
 
     @Override
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
         writeln("import java.util.Collections;");
         writeln("import java.util.Iterator;");
         writeln("import java.util.LinkedHashMap;");
         writeln("import java.util.LinkedList;");
         writeln("import java.util.Map;");
         writeln("import java.util.NoSuchElementException;");
         writeln();
 
         writeln("import net.jonp.sorm.SormBase;");
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
 
         writeln("/**");
         writeln(" * Database-backed data model class.");
         writeln(" */");
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
 
         writeln("/**");
         writeln(" * Initialize a new, empty %s.", sorm.getName());
         writeln(" */");
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
         final Field primary = sorm.getPrimaryField();
 
         writeln("/**");
         writeln(" * Maps between the %s class and a database.", sorm.getName());
         writeln(" */");
         writeln("%s static class Orm", sorm.getOrm_accessor());
         writeln("extends SormBase<%s, %s>", primary.getType(), sorm.getName());
         writeln("{");
         writeln("static final Logger LOG = Logger.getLogger(Orm.class);");
         writeln();
 
         writeln("/**");
         writeln(" * Instantiate an Orm object, wrapped around a {@link SormSession}.");
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use for all database accesses.");
         writeln(" */");
         writeln("public Orm(final SormSession session)");
         writeln("{");
         writeln("super(session);");
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
             if (field.getLink().getCollection() != null) {
                 if (field.getLink().getMode() == LinkMode.OneToMany || field.getLink().getMode() == LinkMode.ManyToMany) {
                     writeln();
                     dumpOrmMapRead(field);
                 }
 
                 if (field.getLink().getMode() == LinkMode.ManyToMany) {
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
         }
 
         for (final NamedQuery nq : sorm.getQueries()) {
             writeln();
             dumpOrmQuery(nq);
         }
 
         writeln("}");
     }
 
     private void dumpOrmCreate()
     {
         writeln("@Override");
         writeln("public void create(final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("create(getSession(), %ss);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #create(SormSession, Collection)}. */");
         writeln("public static void create(final SormSession session, final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("create(session, Arrays.asList(%ss));", OBJ);
         writeln("}");
         writeln();
 
         writeln("@Override");
         writeln("public void create(final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("create(getSession(), %ss);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Insert a number of %s objects into the database.", sorm.getName());
         writeln(" * Each object will have its primary field set to the value created during");
         writeln(" * the insert.");
         writeln(" *");
         writeln(" * @param session The session to use for the inserts.");
         writeln(" * @param %ss The objects to insert.", OBJ);
         writeln(" * @throws SQLException If there was a problem accessing the database.");
         writeln(" */");
         writeln("public static void create(final SormSession session, final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("for (final %s %s : %ss)", sorm.getName(), OBJ, OBJ);
         writeln("{");
         writeln("create(session, %s);", OBJ);
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("@Override");
         writeln("public void create(final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("create(getSession(), %s);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #create(SormSession, Collection)}. */");
         writeln("public static void create(final SormSession session, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
 
         dumpPreparedStatement(sorm.getCreate(), OBJ);
 
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
 
         writeln("/** Get the last assigned primary key. */");
         writeln("private static %s getPk(final SormSession session)", primary.getType());
         writeln("throws SQLException");
         writeln("{");
 
         dumpPreparedStatement(sorm.getPk(), null);
 
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
 
         writeln("@Override");
         writeln("public Collection<%s> read(final %s... %ss)", sorm.getName(), primary.getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
         writeln("return read(getSession(), %ss);", KEY);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #read(SormSession, Collection)}. */");
         writeln("public static Collection<%s> read(final SormSession session, final %s... %ss)", sorm.getName(), primary.getType(),
                 KEY);
         writeln("throws SQLException");
         writeln("{");
         writeln("return read(session, Arrays.asList(%ss));", KEY);
         writeln("}");
         writeln();
 
         writeln("@Override");
         writeln("public Collection<%s> read(final Collection<%s> %ss)", sorm.getName(), primary.getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
         writeln("return read(getSession(), %ss);", KEY);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Read a number of %s objects out of the database.", sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %ss The keys of the %s objects to read.", KEY, sorm.getName());
         writeln(" * @return The %s objects that were read. May not include all requested objects.", sorm.getName());
         writeln(" * @throws SQLException If there was a problem accessing the database.");
         writeln(" */");
         writeln("public static Collection<%s> read(final SormSession session, final Collection<%s> %ss)", sorm.getName(),
                 primary.getType(), KEY);
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
         buildPreparedStatement(sorm.getRead(), null);
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
 
         writeln("@Override");
         writeln("public %s read(final %s %s)", sorm.getName(), primary.getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
         writeln("return read(getSession(), %s);", KEY);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #read(SormSession, Collection)}. */");
         writeln("public static %s read(final SormSession session, final %s %s)", sorm.getName(), primary.getType(), KEY);
         writeln("throws SQLException");
         writeln("{");
         writeln("final Collection<%s> %ss = read(session, Collections.singleton(%s));", sorm.getName(), OBJ, KEY);
         writeln("if (%ss.isEmpty())", OBJ);
         writeln("{");
         writeln("return null;");
         writeln("}");
         writeln("else");
         writeln("{");
         writeln("return %ss.iterator().next();", OBJ);
         writeln("}");
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
                 // TODO: Test whether this field was returned by the query
                 // before trying to read it
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
 
         writeln("@Override");
         writeln("public SormIterable<%s> matches(final Collection<%s> %ss)", sorm.getName(), primary.getType(), KEY);
         writeln("{");
         writeln("return matches(getSession(), %ss);", KEY);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Get an {@link Iterable} that will provide {@link Iterator}s that can read");
         writeln(" * a given sequence of %s objects from the database.", sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %ss The keys of the %s objects to read. Order will be preserved.", KEY, sorm.getName());
         writeln(" * @return An {@link SormIterable} that will return {@link SormIterator}s.");
         writeln(" *         Don't forget to close the {@link SormIterator}s that you get.");
         writeln(" */");
         writeln("public static SormIterable<%s> matches(final SormSession session, final Collection<%s> %ss)", sorm.getName(),
                 primary.getType(), KEY);
         writeln("{");
         writeln("return new SormIterable<%s>()", sorm.getName());
         writeln("{");
         writeln("@Override");
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
         buildPreparedStatement(sorm.getRead(), null);
         writeln("}");
         writeln("catch (final SQLException sqle)");
         writeln("{");
         writeln("throw new SormSQLException(sqle);");
         writeln("}");
         writeln("}");
         writeln();
 
         writeln("@Override");
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
 
         writeln("@Override");
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
 
         writeln("@Override");
         writeln("public void remove()");
         writeln("{");
         writeln("throw new UnsupportedOperationException(\"remove() not supported\");");
         writeln("}");
         writeln();
 
         writeln("@Override");
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
         writeln("@Override");
         writeln("public void update(final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("update(getSession(), %ss);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #update(SormSession, Collection)}. */");
         writeln("public static void update(final SormSession session, final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("update(session, Arrays.asList(%ss));", OBJ);
         writeln("}");
         writeln();
 
         writeln("@Override");
         writeln("public void update(final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("update(getSession(), %ss);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Update a collection of %s objects in the database.", sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %ss The %s objects to update in the database.", OBJ, sorm.getName());
         writeln(" * @throws SQLException If there is a problem.");
         writeln(" */");
         writeln("public static void update(final SormSession session, final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getUpdate(), null);
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
 
         writeln("@Override");
         writeln("public void update(final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("update(getSession(), %s);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #update(SormSession, Collection)}. */");
         writeln("public static void update(final SormSession session, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("update(session, Collections.singleton(%s));", OBJ);
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
         writeln("@Override");
         writeln("public void delete(final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("delete(getSession(), %ss);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #delete(SormSession, Collection)}. */");
         writeln("public static void delete(final SormSession session, final %s... %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("delete(session, Arrays.asList(%ss));", OBJ);
         writeln("}");
         writeln();
 
         writeln("@Override");
         writeln("public void delete(final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("delete(getSession(), %ss);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Delete a collection of %s objects from the database.", sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %ss The %s objects to delete from the database.", OBJ, sorm.getName());
         writeln(" * @throws SQLException If there was a problem.");
         writeln(" */");
         writeln("public static void delete(final SormSession session, final Collection<%s> %ss)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(sorm.getDelete(), null);
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
 
         writeln("@Override");
         writeln("public void delete(final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("delete(getSession(), %s);", OBJ);
         writeln("}");
         writeln();
 
         writeln("/** Convenience wrapper around {@link #delete(SormSession, Collection)}. */");
         writeln("public static void delete(final SormSession session, final %s %s)", sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("delete(session, Collections.singleton(%s));", OBJ);
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
         final String fieldName = getSafeFieldName(field);
         writeln("/** Convenience wrapper around {@link #readMapped%s(SormSession, %s)}. */", StringUtil.capFirst(fieldName),
                 sorm.getName());
         writeln("public Collection<%s> readMapped%s(final %s %s)", field.getLink().getType(), StringUtil.capFirst(fieldName),
                 sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("return readMapped%s(getSession(), %s);", StringUtil.capFirst(fieldName), OBJ);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Read all mapped %s related to a given %s.", fieldName, sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %s The %s whose %s to read.", OBJ, sorm.getName(), fieldName);
         writeln(" * @return The collection of %s mapped to this %s.", fieldName, sorm.getName());
         writeln(" * @throws SQLException If there was a problem.");
         writeln(" */");
         writeln("public static Collection<%s> readMapped%s(final SormSession session, final %s %s)", field.getLink().getType(),
                 StringUtil.capFirst(fieldName), sorm.getName(), OBJ);
         writeln("throws SQLException");
         writeln("{");
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(field.getLink().getCollection().getRead(), null);
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
         final String fieldName = getSafeFieldName(field);
         writeln("/** Convenience wrapper around {@link #map%s(SormSession, %s, %s)}. */", StringUtil.capFirst(fieldName),
                 sorm.getName(), field.getLink().getType());
         writeln("public void map%s(final %s %s, final %s %s)", StringUtil.capFirst(fieldName), sorm.getName(), LHS, field.getLink()
             .getType(), RHS);
         writeln("throws SQLException");
         writeln("{");
         writeln("map%s(getSession(), %s, %s);", StringUtil.capFirst(fieldName), LHS, RHS);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Add a new %s mapping to the given %s.", StringUtil.capFirst(fieldName), sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %s The %s object to which the mapping should be added.", LHS, sorm.getName());
         writeln(" * @param %s The %s to add to the mappings of <code>%s</code>.", RHS, StringUtil.capFirst(fieldName), LHS);
         writeln(" * @throws SQLException If there was a problem.");
         writeln(" */");
         writeln("public static void map%s(final SormSession session, final %s %s, final %s %s)", StringUtil.capFirst(fieldName),
                 sorm.getName(), LHS, field.getLink().getType(), RHS);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(field.getLink().getCollection().getCreate(), null);
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
         final String fieldName = getSafeFieldName(field);
         writeln("/** Convenience wrapper around {@link #unmap%s(SormSession, %s, %s)}. */", StringUtil.capFirst(fieldName),
                 sorm.getName(), field.getLink().getType());
         writeln("public void unmap%s(final %s %s, final %s %s)", StringUtil.capFirst(fieldName), sorm.getName(), LHS, field
             .getLink().getType(), RHS);
         writeln("throws SQLException");
         writeln("{");
         writeln("unmap%s(getSession(), %s, %s);", StringUtil.capFirst(fieldName), LHS, RHS);
         writeln("}");
         writeln();
 
         writeln("/**");
         writeln(" * Remove an existing %s mapping from the given %s.", StringUtil.capFirst(fieldName), sorm.getName());
         writeln(" *");
         writeln(" * @param session The {@link SormSession} to use.");
         writeln(" * @param %s The %s object from which the mapping should be removed.", LHS, sorm.getName());
         writeln(" * @param %s The %s to remove from the mappings of <code>%s</code>.", RHS, StringUtil.capFirst(fieldName), LHS);
         writeln(" * @throws SQLException If there was a problem.");
         writeln(" */");
         writeln("public static void unmap%s(final SormSession session, final %s %s, final %s %s)", StringUtil.capFirst(fieldName),
                 sorm.getName(), LHS, field.getLink().getType(), RHS);
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(field.getLink().getCollection().getDelete(), null);
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
 
         final StringBuilder args = new StringBuilder();
         final StringBuilder argNames = new StringBuilder();
         for (final QueryParam param : nq.getParams()) {
             if (args.length() > 0) {
                 args.append(", ");
                 argNames.append(", ");
             }
 
             args.append(String.format("final %s %s", param.getType(), param.getName()));
             argNames.append(String.format("%s", param.getName()));
         }
 
         // FUTURE: Is there any relevant documentation we can provide for this
         // method?
         writeln("%s Collection<%s> %s(%s)", nq.getAccessor(), primary.getType(), nq.getName(), args);
         writeln("throws SQLException");
         writeln("{");
         if (argNames.length() > 0) {
             writeln("return %s(getSession(), %s);", nq.getName(), argNames);
         }
         else {
             writeln("return %s(getSession());", nq.getName());
         }
         writeln("}");
         writeln();
 
         if (args.length() > 0) {
             writeln("%s static Collection<%s> %s(final SormSession session, %s)", nq.getAccessor(), primary.getType(),
                     nq.getName(), args);
         }
         else {
             writeln("%s static Collection<%s> %s(final SormSession session)", nq.getAccessor(), primary.getType(), nq.getName());
         }
         writeln("throws SQLException");
         writeln("{");
 
         writeln("final Collection<%s> %ss= new LinkedList<%s>();", primary.getType(), KEY, primary.getType());
         dumpPreparedStatement(nq);
 
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
 
            if (null != field.getParent()) {
                continue;
            }

             final String fieldName = getSafeFieldName(field);
             switch (field.getLink().getMode()) {
                 case None:
                 case OneToMany:
                 case ManyToMany:
                     writeln("%s %s _%s;", field.getAccessor(), field.getType(), fieldName);
                     break;
 
                 case OneToOne:
                 case ManyToOne:
                     writeln("%s %s _%s;", field.getAccessor(), field.getType(), fieldName);
                     writeln("%s %s _%sObject;", field.getAccessor(), field.getLink().getType(), fieldName);
                     break;
             }
         }
     }
 
     private void dumpAccessors()
     {
         boolean first = true;
         for (final Field field : sorm.getFields()) {
             if (field.isFromSuper()) {
                 continue;
             }
 
             if (null != field.getParent()) {
                 continue;
             }
 
             if (first) {
                 first = false;
             }
             else {
                 writeln();
             }
 
             final String fieldName = getSafeFieldName(field);
             if (!field.getGet().isFromSuper()) {
                 if (LinkMode.OneToOne == field.getLink().getMode() || LinkMode.ManyToOne == field.getLink().getMode()) {
                     if (field.getGet().isOverride()) {
                         writeln("@Override");
                     }
                     writeln("%s %s %s()", field.getGet().getAccessor(), field.getType(), field.getGet().getName());
                     writeln("{");
                     writeln("return _%s;", fieldName);
                     writeln("}");
                     writeln();
 
                     if (field.getGet().isOverride()) {
                         writeln("@Override");
                     }
                     writeln("%s %s %sObject()", field.getGet().getAccessor(), field.getLink().getType(), field.getGet().getName());
                     writeln("{");
                     writeln("return _%sObject;", fieldName);
                     writeln("}");
                     writeln();
                 }
                 else {
                     if (field.getGet().isOverride()) {
                         writeln("@Override");
                     }
                     writeln("%s %s %s()", field.getGet().getAccessor(), field.getType(), field.getGet().getName());
                     writeln("{");
                     writeln("return _%s;", fieldName);
                     writeln("}");
                     writeln();
                 }
             }
 
             if (!field.getSet().isFromSuper()) {
                 if (LinkMode.OneToOne == field.getLink().getMode() || LinkMode.ManyToOne == field.getLink().getMode()) {
                     if (field.getSet().isOverride()) {
                         writeln("@Override");
                     }
                     writeln("%s void %s(final %s %s)", field.getSet().getAccessor(), field.getSet().getName(), field.getType(),
                             fieldName);
                     writeln("{");
                     writeln("_%s = %s;", fieldName, fieldName);
                     writeln("}");
                     writeln();
 
                     if (field.getSet().isOverride()) {
                         writeln("@Override");
                     }
                     writeln("%s void %sObject(final %s %sObject)", field.getSet().getAccessor(), field.getSet().getName(), field
                         .getLink().getType(), fieldName);
                     writeln("{");
                     writeln("_%sObject = %sObject;", fieldName, fieldName);
                     writeln("}");
                 }
                 else {
                     if (field.getSet().isOverride()) {
                         writeln("@Override");
                     }
                     writeln("%s void %s(final %s %s)", field.getSet().getAccessor(), field.getSet().getName(), field.getType(),
                             fieldName);
                     writeln("{");
                     writeln("_%s = %s;", fieldName, fieldName);
                     writeln("}");
                 }
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
         buildPreparedStatement(query, null);
         writeln();
         writeln("try");
         writeln("{");
         populatePreparedStatement(query, objname);
     }
 
     private void dumpPreparedStatement(final NamedQuery nq)
     {
         writeln("final PreparedStatement ps;");
         buildPreparedStatement(nq.getQuery(), nq.getParams());
         writeln();
         writeln("try");
         writeln("{");
         populatePreparedStatement(nq);
     }
 
     private void buildPreparedStatement(final Query query, final Collection<QueryParam> params)
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
                 writeln("\"%s\";", compileSql(query.getQuery(dialect), params));
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
             writeln("\"%s\";", compileSql(query.getQuery("*"), params));
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
 
     private String compileSql(String query, final Collection<QueryParam> params)
     {
         query = query.replaceAll("%\\{\\}", "?");
         if (params == null) {
             for (final Field field : sorm.getFields()) {
                 if (field.isGroup()) {
                     // Skip grouped fields because you cannot directly write
                     // them to
                     // the database
                     continue;
                 }
 
                 final String fieldName = getFieldName(field);
                 query = query.replaceAll("%\\{" + fieldName + "\\}", "?");
                 query = query.replaceAll("%\\{1\\." + fieldName + "\\}", "?");
             }
         }
         else {
             for (final QueryParam param : params) {
                 final String paramName = param.getName();
                 query = query.replaceAll("%\\{" + paramName + "\\}", "?");
                 query = query.replaceAll("%\\{1\\." + paramName + "\\}", "?");
             }
         }
 
         // Search for %{2.*} references
         query = query.replaceAll("%\\{2\\..*?:.*?\\}", "?");
 
         // Fix embedded newlines
         query = query.replaceAll("\r?\n", String.format("\\\\n\" +%n" + indentString + "\""));
 
         return query;
     }
 
     private String compileAccessor(final Field field, final String query, final String objName)
     {
         final String obj;
         final Field parent = getParentField(field);
         if (null != parent) {
             obj = compileAccessor(parent, parent.getGet().getContent(), objName);
         }
         else {
             obj = objName;
         }
 
         return query.replaceAll("%\\{\\}", obj);
     }
 
     private void compileFromRS(final Field f, final String objname)
     {
         // Cannot get *-to-many fields from an individual ResultSet row
         if (f.getLink().getMode() == LinkMode.OneToMany || f.getLink().getMode() == LinkMode.ManyToMany) {
             return;
         }
 
         // Cannot read individual subfields of a group one at a time
         if (null != f.getParent()) {
             return;
         }
 
         // Convert all %{} into objname
         String s = f.getSet().getContent();
         s = s.replaceAll("%\\{\\}", objname);
 
         // Convert all %{xxx} into rs.get*("xxx")
         for (final Field field : sorm.getFields()) {
             final String fieldName = getFieldName(field);
             if (s.contains("%{" + fieldName + "}")) {
                 s = s.replaceAll("%\\{" + fieldName + "\\}", //
                                  String.format("rs.%s(\"%s\")", field.getSql_type().getter, field.getSql_column()));
             }
         }
 
         writeln("%s;", s);
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
                     else if (start.startsWith("%{2.")) {
                         final Matcher matcher = P_RHSMAP.matcher(start);
                         if (matcher.matches()) {
                             final String function = matcher.group(1) + matcher.group(2);
                             final boolean nullable = null == matcher.group(3) ? false : matcher.group(3).equals("nullable ");
                             final String type = matcher.group(4);
 
                             // Create a temporary field so we can use
                             // compileAccessor() and dumpSet()
                             final Field field = new Field();
                             field.getGet().setName(function);
                             field.setNullable(nullable);
                             field.setSql_type(SQLType.valueOf(type));
 
                             final String accessor = compileAccessor(field, field.getGet().getContent(), RHS);
                             dumpSet(field, arg++, accessor);
                             wroteSet = true;
                         }
                     }
                     else {
                         for (final Field field : sorm.getFields()) {
                             final String fieldName = getFieldName(field);
                             if (start.startsWith("%{" + fieldName + "}")) {
                                 final String accessor = compileAccessor(field, field.getGet().getContent(), objname);
                                 dumpSet(field, arg++, accessor);
                                 wroteSet = true;
                             }
                             else if (start.startsWith("%{1." + fieldName + "}")) {
                                 final String accessor = compileAccessor(field, field.getGet().getContent(), LHS);
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
                             final String accessor = compileAccessor(null, param.getSet().getContent(), param.getName());
                             writeln("LOG.debug(\"  Param %d: \" + %s);", arg, accessor);
                             writeln("ps.%s(%d, %s);", param.getSql_type().setter, arg++, accessor);
                             writeln();
                             wroteSet = true;
                         }
                     }
                 }
             }
         }
 
         return wroteSet;
     }
 
     /**
      * Get a group-safe field name safe for use as a Java identifier.
      * 
      * @param field The field whose name to get.
      * @return A Java-safe name for the field.
      */
     private String getSafeFieldName(final Field field)
     {
         return getFieldName(field).replaceAll("\\.", "_");
     }
 
     /**
      * Get a group-safe field name for use in pre-compiled SQL.
      * 
      * @param field The field.
      * @return The name of the field.
      */
     private String getFieldName(final Field field)
     {
         final Field parent = getParentField(field);
         if (null == parent) {
             return field.getName();
         }
         else {
             return getFieldName(parent) + "." + field.getName();
         }
     }
 
     /**
      * Find the parent field for the given field.
      * 
      * @param field The field whose parent to find.
      * @return The parent field, or <code>null</code> if the either the field
      *         was <code>null</code>, or it did not have a parent.
      */
     private Field getParentField(final Field field)
     {
         if (null == field) {
             return null;
         }
         else if (null == field.getParent()) {
             return null;
         }
         else {
             Field parent = null;
             for (final Field f : sorm.getFields()) {
                 if (f.getName().equals(field.getParent())) {
                     parent = f;
                     break;
                 }
             }
 
             if (null == parent) {
                 throw new IllegalStateException("Field " + field.getName() + " specifies non-existent parent field " +
                                                 field.getParent());
             }
 
             return parent;
         }
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
        writeln("LOG.debug(\"  Param %d: \" + (%s));", arg, accessor);
         if (field.isNullable()) {
            writeln("if (null == (%s))", accessor);
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
 
         writeln();
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
