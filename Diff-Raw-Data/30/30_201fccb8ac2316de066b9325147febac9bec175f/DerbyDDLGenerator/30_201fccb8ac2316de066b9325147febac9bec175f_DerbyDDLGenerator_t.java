 package org.nsdl.mptstore.impl.derby;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.nsdl.mptstore.core.AbstractDDLGenerator;
 
 /**
  * A <code>DDLGenerator</code> that works with Derby.
  * <p>
 *   The map table DDL defines pKey as <code>INT NOT NULL GENERATED ALWAYS
  *   AS IDENTITY</code>, and p as a VARCHAR, with indexes on each column.
  * </p>
  * <p>
 *   The relationship table DDL defines s and o as <code>VARCHAR</code>
  *   values, with indexes on each column.
  * </p>
  * <p>
 *   The default VARCHAR length is 512, but this can be overridden by using
 *   the appropriate constructor.  Note: Derby's limit for VARCHAR length
 *   is actually 32,672 characters, but because these values are indexed,
 *   the actual maximum possible here is lower and dependent on the page
 *   size that Derby has been configured to use. See the <i>Derby Reference
  *   Manual</i> for more information.
  * </p>
  *
  * @see <a href="http://db.apache.org/derby/docs/10.1/ref/">
  *        Derby Reference Manual</a>
  * @author cwilper@cs.cornell.edu
  */
 public class DerbyDDLGenerator extends AbstractDDLGenerator {
 
     private static final int DEFAULT_VARCHAR_LENGTH = 512;
 
     private final int _varcharLength;
 
     /**
      * Construct a DerbyDDLGenerator that uses the default maximum length (512)
      * for varchar columns that store URIs.
      */
     public DerbyDDLGenerator() {
         _varcharLength = DEFAULT_VARCHAR_LENGTH;
     }
 
     /**
      * Construct a DerbyDDLGenerator that uses the specified maximum length
      * for varchar columns that store URIs.
     *
     * @param varcharLength the length for all VARCHAR columns.
      */
    public DerbyDDLGenerator(final int varcharLength) {
         _varcharLength = varcharLength;
     }
 
     /** {@inheritDoc} */
     public List<String> getCreateMapTableDDL(final String table) {
 
         List<String> cmds = new ArrayList<String>();
 
         cmds.add("CREATE TABLE " + table + " (\n"
                + "  pKey INT NOT NULL GENERATED ALWAYS AS IDENTITY,\n"
                + "  p VARCHAR(" + _varcharLength + ") NOT NULL\n"
                + ")");
         cmds.add("CREATE INDEX " + table + "_pKey "
                + " on " + table + " (pKey)");
         cmds.add("CREATE INDEX " + table + "_p "
                + " on " + table + " (p)");
 
         return cmds;
     }
 
     /** {@inheritDoc} */
     public List<String> getCreateSOTableDDL(final String table) {
 
         List<String> cmds = new ArrayList<String>();
 
         cmds.add("CREATE TABLE " + table + " (\n"
                + "  s VARCHAR(" + _varcharLength + ") NOT NULL,\n"
                + "  o VARCHAR(" + _varcharLength + ") NOT NULL\n"
                + ")");
         cmds.add("CREATE INDEX " + table + "_s "
                + " on " + table + " (s)");
         cmds.add("CREATE INDEX " + table + "_o "
                + " on " + table + " (o)");
 
         return cmds;
     }
 
 }
