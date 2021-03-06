 package edu.brown.catalog;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.commons.collections15.map.ListOrderedMap;
 import org.apache.log4j.Logger;
 
 import org.json.*;
 
 import org.voltdb.*;
 import org.voltdb.plannodes.*;
 import org.voltdb.types.*;
 import org.voltdb.utils.*;
 import org.voltdb.catalog.*;
 import org.voltdb.expressions.*;
 
 import edu.brown.catalog.special.MultiColumn;
 import edu.brown.catalog.special.MultiProcParameter;
 import edu.brown.catalog.special.NullProcParameter;
 import edu.brown.catalog.special.ReplicatedColumn;
 import edu.brown.expressions.ExpressionTreeWalker;
 import edu.brown.plannodes.PlanNodeTreeWalker;
 import edu.brown.plannodes.PlanNodeUtil;
 import edu.brown.utils.AbstractTreeWalker;
 import edu.brown.utils.CollectionUtil;
 import edu.brown.utils.FileUtil;
 
 /**
  * @author pavlo
  */
 public abstract class CatalogUtil extends org.voltdb.utils.CatalogUtil {
     static final Logger LOG = Logger.getLogger(CatalogUtil.class);
 
     public static final String DEFAULT_CLUSTER_NAME = "cluster";
     public static final String DEFAULT_DATABASE_NAME = "database";
     public static final String DEFAULT_PROCEDURE_NAME = "procedure";
     public static final String DEFAULT_STATEMENT_NAME = "statement";
 
     private static final Random rand = new Random();
 
     // ------------------------------------------------------------
     // CACHES
     // ------------------------------------------------------------
 
     /**
      * StatementKey -> Set<ColumnKey>
      */
     public static final Map<String, Set<String>> CACHE_STATEMENT_COLUMNS_KEYS = new HashMap<String, Set<String>>();
 
     /**
      * Table -> Long
      */
     public static final Map<String, Long> CACHE_TABLE_TUPLE_SIZE = new HashMap<String, Long>();
 
     public static Map<Object, String> getHistogramLabels(Set<Object> items) {
         Map<Object, String> labels = new HashMap<Object, String>();
         for (Object o : items) {
             if (o instanceof CatalogType) labels.put(o, CatalogUtil.getDisplayName((CatalogType)o));
         } // FOR
         return (labels);
     }
     
     // TODO: Make generic!
     public static List<Column> getColumns(CatalogMap<ColumnRef> map) {
         int capacity = map.size();
         ArrayList<Column> ret = new ArrayList<Column>(capacity);
         for (int i = 0; i < capacity; i++) {
             ret.add(null);
         }
         for (ColumnRef ref : map) {
             Column catalog_col = ref.getColumn();
             ret.set(catalog_col.getIndex(), catalog_col);
         }
         return (ret);
     }
 
     public static Set<Constraint> getConstraints(Iterable<ConstraintRef> map) {
         Set<Constraint> ret = new HashSet<Constraint>();
         if (map != null) {
             for (ConstraintRef ref : map) {
                 Constraint catalog_item = ref.getConstraint();
                 assert (catalog_item != null);
                 ret.add(catalog_item);
             }
         }
         return (ret);
     }
 
     public static ProcParameter getProcParameter(Procedure catalog_proc) {
         assert (catalog_proc != null);
         ProcParameter catalog_param = null;
         if (catalog_proc.getParameters().size() > 0
                 && !catalog_proc.getSystemproc()) {
             int idx = catalog_proc.getPartitionparameter();
             if (idx == NullProcParameter.PARAM_IDX) {
                 catalog_param = NullProcParameter
                         .getNullProcParameter(catalog_proc);
             } else {
                 catalog_param = catalog_proc.getParameters().get(idx);
                 assert (catalog_param != null) : "Unexpected Null ProcParameter for "
                         + catalog_proc.getName() + " at idx #" + idx;
             }
         }
         return (catalog_param);
     }
 
     /**
      * Return the unique Site catalog object for the given id
      * 
      * @param catalog_item
      * @return
      */
     public static Site getSiteFromId(CatalogType catalog_item, int site_id) {
         assert (site_id >= 0);
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
         for (Site catalog_site : catalog_clus.getSites()) {
             if (catalog_site.getId() == site_id)
                 return (catalog_site);
         } // FOR
         return (null);
     }
 
     /**
      * Return the list of ProcParameters that are array parameters for the given procedure
      */
     public static List<ProcParameter> getArrayProcParameters(final Procedure catalog_proc) {
         List<ProcParameter> params = new ArrayList<ProcParameter>();
         for (ProcParameter catalog_param : catalog_proc.getParameters()) {
             if (catalog_param.getIsarray()) params.add(catalog_param);
         } // FOR
         return (params);
     }
     
     /**
      * Return the number of hosts for a catalog for any catalog item
      * @param catalog_item
      * @return
      */
     public static int getNumberOfHosts(CatalogType catalog_item) {
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
         int ret = catalog_clus.getHosts().size();
         assert (ret > 0);
         return (ret);
     }
     
     /**
      * Return the number of sites for a catalog for any catalog item
      * @param catalog_item
      * @return
      */
     public static int getNumberOfSites(CatalogType catalog_item) {
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
         int ret = catalog_clus.getSites().size();
         assert (ret > 0);
         return (ret);
     }
     
     /**
      * Return the number of partitions for a catalog for any catalog item
      * 
      * @param catalog_item
      * @return
      */
     public static int getNumberOfPartitions(CatalogType catalog_item) {
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
         int ret = catalog_clus.getNum_partitions();
         assert (ret > 0);
         return (ret);
     }
 
     /**
      * Return a random partition id for all of the partitions available
      * 
      * @param catalog_item
      * @return
      */
     public static int getRandomPartition(CatalogType catalog_item) {
         return (rand.nextInt(CatalogUtil.getNumberOfPartitions(catalog_item)));
     }
 
     public static Partition getPartitionById(CatalogType catalog_item,
             Integer id) {
         if (CACHE_ALL_PARTITIONS.isEmpty()) {
             CatalogUtil.getAllPartitions(catalog_item);
         }
         return (CACHE_ALL_PARTITIONS.get(id));
     }
 
     public static Collection<Partition> getAllPartitions(
             CatalogType catalog_item) {
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
 
         if (CACHE_ALL_PARTITIONS.isEmpty()) {
             for (Site catalog_site : catalog_clus.getSites()) {
                 for (Partition catalog_part : catalog_site.getPartitions()) {
                     CACHE_ALL_PARTITIONS
                             .put(catalog_part.getId(), catalog_part);
                 } // FOR
             } // FOR
         }
         return (Collections.unmodifiableCollection(CACHE_ALL_PARTITIONS
                 .values()));
     }
 
     private static final ListOrderedMap<Integer, Partition> CACHE_ALL_PARTITIONS = new ListOrderedMap<Integer, Partition>();
 
     /**
      * Get a new list of all the partition ids in this catalog
      * 
      * @return
      */
     public static List<Integer> getAllPartitionIds(CatalogType catalog_item) {
         if (CACHE_ALL_PARTITIONS.isEmpty()) {
             CatalogUtil.getAllPartitions(catalog_item);
         }
         return (Collections.unmodifiableList(CACHE_ALL_PARTITIONS.asList()));
     }
 
     /**
      * Get a mapping of partitions for each host. We have to return the Site
      * objects in order to get the Partition handle that we want
      * 
      * @return
      */
     public static Map<Host, Set<Site>> getSitesPerHost(CatalogType catalog_item) {
         final Catalog catalog = catalog_item.getCatalog();
         Map<Host, Set<Site>> ret = CACHE_HOST_SITES.get(catalog);
 
         if (ret == null) {
             Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
             ret = new HashMap<Host, Set<Site>>();
 
             final Comparator<Site> comparator = new Comparator<Site>() {
                 @Override
                 public int compare(Site o1, Site o2) {
                     return (o1.getId() - o2.getId());
                 }
             };
 
             for (Site catalog_site : catalog_clus.getSites()) {
                 Host catalog_host = catalog_site.getHost();
                 if (!ret.containsKey(catalog_host)) {
                     ret.put(catalog_host, new TreeSet<Site>(comparator));
                 }
                 ret.get(catalog_host).add(catalog_site);
                 LOG.debug(catalog_host + " => " + catalog_site);
             } // FOR
             assert (ret.size() == catalog_clus.getHosts().size());
             ret = Collections.unmodifiableMap(ret);
             CACHE_HOST_SITES.put(catalog, ret);
             LOG.debug("HOST SITES: " + ret);
         }
         return (ret);
     }
 
     private static final Map<Catalog, Map<Host, Set<Site>>> CACHE_HOST_SITES = new HashMap<Catalog, Map<Host, Set<Site>>>();
 
     /**
      * For a given VoltTable object, return the matching Table catalog object
      * based on the column names.
      * 
      * @param catalog_db
      * @param voltTable
      * @return
      * @throws Exception
      */
     public static Table getCatalogTable(Database catalog_db, VoltTable voltTable) {
         int num_columns = voltTable.getColumnCount();
         for (Table catalog_tbl : catalog_db.getTables()) {
             if (num_columns == catalog_tbl.getColumns().size()) {
                 boolean match = true;
                 List<Column> catalog_cols = CatalogUtil.getSortedCatalogItems(
                         catalog_tbl.getColumns(), "index");
                 for (int i = 0; i < num_columns; i++) {
                     if (!voltTable.getColumnName(i).equals(
                             catalog_cols.get(i).getName())) {
                         match = false;
                         break;
                     }
                 } // FOR
                 if (match)
                     return (catalog_tbl);
             }
         } // FOR
         return (null);
     }
 
     /**
      * Return a list of the triplets [Host IP Address, Port #, Site ID]
      * 
      * @param catalog_item
      * @return
      */
     public static List<String[]> getExecutionSites(CatalogType catalog_item) {
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
         ArrayList<String[]> ret = new ArrayList<String[]>();
         for (Host catalog_host : catalog_clus.getHosts()) {
             assert (catalog_host != null);
             for (Site catalog_site : CatalogUtil.getSitesForHost(catalog_host)) {
                 ret.add(new String[] {
                         catalog_host.getIpaddr(),
                         Integer.toString(catalog_site.getProc_port()),
                         catalog_site.getName() });
             } // FOR
         } // FOR
         return (ret);
     }
 
     /**
      * Clone and return the given catalog
      * 
      * @param catalog_db
      * @return
      * @throws Exception
      */
     public static Database cloneDatabase(Database catalog_db) throws Exception {
         assert (catalog_db != null);
         // Catalog clone_catalog = new Catalog();
         // clone_catalog.execute(catalog_db.getCatalog().serialize());
         // return (CatalogUtil.getDatabase(clone_catalog));
 
         final Catalog clone_catalog = cloneBaseCatalog(catalog_db.getCatalog(),
                 new ArrayList<Class<? extends CatalogType>>());
         Database clone_db = CatalogUtil.getDatabase(clone_catalog);
         assert (!catalog_db.equals(clone_db));
 
         // Need to also clone the MultiColumn guys too!
         for (Table catalog_tbl : catalog_db.getTables()) {
             Table clone_tbl = clone_db.getTables().get(catalog_tbl.getName());
             for (Column catalog_col : catalog_tbl.getColumns()) {
                 if (catalog_col instanceof MultiColumn) {
                     MultiColumn mc = (MultiColumn) catalog_col;
                     Column clone_cols[] = new Column[mc.size()];
                     for (int i = 0; i < clone_cols.length; i++) {
                         clone_cols[i] = clone_tbl.getColumns().get(
                                 mc.get(i).getName());
                     } // FOR
 
                     // This will automatically add our guy into clone_tbl
                     MultiColumn clone_mc = MultiColumn.get(clone_cols);
                     assert (clone_mc != null);
                 }
             }
             assert (catalog_tbl.getColumns().size() == clone_tbl.getColumns()
                     .size()) : catalog_tbl.getColumns() + " != "
                     + clone_tbl.getColumns();
         } // FOR
 
         // And don't forget MultiProcParameter!
         for (Procedure catalog_proc : catalog_db.getProcedures()) {
             Procedure clone_proc = clone_db.getProcedures().get(
                     catalog_proc.getName());
             for (ProcParameter catalog_param : catalog_proc.getParameters()) {
                 if (catalog_param instanceof MultiProcParameter) {
                     MultiProcParameter mpp = (MultiProcParameter) catalog_param;
                     ProcParameter clone_params[] = new ProcParameter[mpp.size()];
                     for (int i = 0; i < clone_params.length; i++) {
                         clone_params[i] = clone_proc.getParameters().get(
                                 mpp.get(i).getIndex());
                     } // FOR
 
                     // This will automatically add our guy into clone_tbl
                     MultiProcParameter clone_mpp = MultiProcParameter
                             .get(clone_params);
                     assert (clone_mpp != null);
                 }
             }
             assert (catalog_proc.getParameters().size() == clone_proc
                     .getParameters().size()) : catalog_proc.getParameters()
                     + " != " + clone_proc.getParameters();
         } // FOR
 
         return (clone_db);
     }
 
     /**
      * Clones the base components of a catalog. All underlying objects are
      * recreated
      * 
      * @param catalog
      * @return
      */
     public static Catalog cloneBaseCatalog(Catalog catalog) {
         HashSet<Class<? extends CatalogType>> skip_types = new HashSet<Class<? extends CatalogType>>();
         skip_types.add(Table.class);
         return (CatalogUtil.cloneBaseCatalog(catalog, skip_types));
     }
 
     public static Catalog cloneBaseCatalog(Catalog catalog,
             Class<? extends CatalogType>... skip_types) {
         return (CatalogUtil
                 .cloneBaseCatalog(catalog, Arrays.asList(skip_types)));
     }
 
     /**
      * 
      * @param catalog
      * @param skip_types
      * @return
      */
     public static Catalog cloneBaseCatalog(Catalog catalog,
             final Collection<Class<? extends CatalogType>> skip_types) {
         final Catalog new_catalog = new Catalog();
 
         new AbstractTreeWalker<CatalogType>() {
             protected void populate_children(
                     AbstractTreeWalker<CatalogType>.Children children,
                     CatalogType element) {
                 if (element instanceof Catalog) {
                     children.addAfter(((Catalog) element).getClusters()
                             .values());
                 } else if (element instanceof Cluster) {
                     children.addAfter(((Cluster) element).getDatabases()
                             .values());
                     children.addAfter(((Cluster) element).getHosts().values());
                     // children.addAfter(((Cluster)element).getPartitions().values());
                     children.addAfter(((Cluster) element).getSites().values());
                     // children.addAfter(((Cluster)element).getElhosts().values());
                 } else if (element instanceof Database) {
                     children.addAfter(((Database) element).getProcedures()
                             .values());
                     children.addAfter(((Database) element).getPrograms()
                             .values());
                     children
                             .addAfter(((Database) element).getTables().values());
                 } else if (element instanceof Procedure) {
                     for (ProcParameter param : ((Procedure) element)
                             .getParameters().values()) {
                         if (!(param instanceof MultiProcParameter))
                             children.addAfter(param);
                     } // FOR
                     children.addAfter(((Procedure) element).getStatements()
                             .values());
                 } else if (element instanceof Statement) {
                     children.addAfter(((Statement) element).getParameters()
                             .values());
                     children.addAfter(((Statement) element).getFragments()
                             .values());
                     children.addAfter(((Statement) element).getMs_fragments()
                             .values());
                     children.addAfter(((Statement) element).getOutput_columns()
                             .values());
                 } else if (element instanceof PlanFragment) {
                     // children.addAfter(((PlanFragment)element).getDependencyids().values());
                     // children.addAfter(((PlanFragment)element).getOutputdependencyids().values());
                 }
             };
 
             @Override
             protected void callback(CatalogType element) {
                 if (element != null && !skip_types.contains(element.getClass()))
                     CatalogUtil.clone(element, new_catalog);
             }
         }.traverse(catalog);
 
         // Clone constraints if they were not skipped
         if (!(skip_types.contains(Table.class)
                 || skip_types.contains(Column.class) || skip_types
                 .contains(Constraint.class))) {
             CatalogUtil.cloneConstraints(CatalogUtil.getDatabase(catalog),
                     CatalogUtil.getDatabase(new_catalog));
         }
         return (new_catalog);
     }
 
     /**
      * Add a single catalog element from one catalog into the destination
      * catalog Note that this will not copy constraints for tables, since that
      * needs to be done later to ensure that any foreign key references are
      * included properly
      * 
      * @param <T>
      * @param src_item
      * @param dest_catalog
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T extends CatalogType> T clone(T src_item,
             Catalog dest_catalog) {
         StringBuilder buffer = new StringBuilder();
         if (src_item instanceof MultiProcParameter) {
             System.err.println(src_item + ": ??????????");
             return (null);
         }
         CatalogProxy.writeCommands(src_item, buffer);
         dest_catalog.execute(buffer.toString());
         T clone = (T) dest_catalog.getItemForRef(src_item.getPath());
 
         // Table
         if (src_item instanceof Table) {
             Table src_tbl = (Table) src_item;
             Table dest_tbl = (Table) clone;
 
             // Columns
             for (Column src_col : src_tbl.getColumns()) {
                 if (!(src_col instanceof MultiColumn))
                     CatalogUtil.clone(src_col, dest_catalog);
             } // FOR
             // Indexes
             for (Index src_idx : src_tbl.getIndexes()) {
                 CatalogUtil.clone(src_idx, dest_catalog);
             } // FOR
             // // Constraints
             // for (Constraint src_cons : ((Table)src).getConstraints()) {
             // CatalogUtil.clone(src_cons, dest_catalog);
             // } // FOR
 
             // Partitioning Column
             if (src_tbl.getPartitioncolumn() != null) {
                 Column src_part_col = src_tbl.getPartitioncolumn();
                 Column dest_part_col = null;
 
                 // Special Case: Replicated Column Marker
                 if (src_part_col instanceof ReplicatedColumn) {
                     dest_part_col = ReplicatedColumn.get(dest_tbl);
                     // Special Case: MultiColumn
                 } else if (src_part_col instanceof MultiColumn) {
                     MultiColumn mc = (MultiColumn) src_part_col;
                     Column dest_cols[] = new Column[mc.size()];
                     for (int i = 0; i < dest_cols.length; i++) {
                         dest_cols[i] = dest_tbl.getColumns().get(
                                 mc.get(i).getName());
                     } // FOR
                     dest_part_col = MultiColumn.get(dest_cols);
 
                 } else {
                     dest_part_col = dest_tbl.getColumns().get(
                             src_part_col.getName());
                 }
                 assert (dest_part_col != null) : "Missing partitioning column "
                         + CatalogUtil.getDisplayName(src_part_col);
                 dest_tbl.setPartitioncolumn(dest_part_col);
             }
 
             //
             // Index
             //
         } else if (src_item instanceof Index) {
             // ColumnRefs
             Index src_idx = (Index) src_item;
             Index dest_idx = (Index) clone;
             for (ColumnRef src_colref : src_idx.getColumns()) {
                 CatalogUtil.clone(src_colref, dest_catalog);
 
                 // Correct what it's pointing to
                 ColumnRef dest_colref = dest_idx.getColumns().get(
                         src_colref.getName());
                 Table dest_tbl = (Table) dest_idx.getParent();
                 dest_colref.setColumn(dest_tbl.getColumns().get(
                         src_colref.getColumn().getName()));
             } // FOR
             //
             // Constraint
             //
         } else if (src_item instanceof Constraint) {
             // ColumnRefs
             Constraint src_cons = (Constraint) src_item;
             Constraint dest_cons = (Constraint) clone;
 
             Table src_fkey_tbl = src_cons.getForeignkeytable();
             if (src_fkey_tbl != null) {
                 Database dest_db = (Database) dest_cons.getParent().getParent();
                 Table dest_fkey_tbl = dest_db.getTables().get(
                         src_fkey_tbl.getName());
                 if (dest_fkey_tbl != null) {
                     dest_cons.setForeignkeytable(dest_fkey_tbl);
                     for (ColumnRef src_cref : ((Constraint) src_item)
                             .getForeignkeycols()) {
                         CatalogUtil.clone(src_cref, dest_catalog);
 
                         // Correct what it's pointing to
                         ColumnRef dest_colref = dest_cons.getForeignkeycols()
                                 .get(src_cref.getName());
                         assert (dest_colref != null);
                         dest_colref.setColumn(dest_fkey_tbl.getColumns().get(
                                 src_cref.getColumn().getName()));
                     } // FOR
                 }
             }
 
             // Important: We have to add ConstraintRefs to Columns *after* we
             // add the columns
             Table src_tbl = (Table) src_cons.getParent();
             Table dest_tbl = (Table) dest_cons.getParent();
             for (Column src_col : src_tbl.getColumns()) {
                 Column dest_col = dest_tbl.getColumns().get(src_col.getName());
                 assert (dest_col != null);
                 for (ConstraintRef src_conref : src_col.getConstraints()) {
                     if (!src_conref.getConstraint().equals(src_cons))
                         continue;
                     CatalogUtil.clone(src_conref, dest_catalog);
 
                     // Correct what it's pointing to
                     ConstraintRef dest_conref = dest_col.getConstraints().get(
                             src_conref.getName());
                     assert (dest_conref != null);
                     // System.out.println("dest_tbl: " + dest_tbl);
                     // System.out.println("dest_tbl.getConstraints(): " +
                     // CatalogUtil.debug(dest_tbl.getConstraints()));
                     // System.out.println("src_confref: " + src_conref);
                     // System.out.println("src_confref.getConstraint(): " +
                     // src_conref.getConstraint());
                     dest_conref.setConstraint(dest_tbl.getConstraints().get(
                             src_conref.getConstraint().getName()));
                 } // FOR
             } // FOR
 
             Index src_index = src_cons.getIndex();
             if (src_index != null) {
                 Index dest_index = dest_tbl.getIndexes().get(
                         src_index.getName());
                 dest_cons.setIndex(dest_index);
             }
 
             //
             // StmtParameter
             //
         } else if (src_item instanceof StmtParameter) {
             // We need to fix the reference to the ProcParameter (if one exists)
             StmtParameter src_stmt_param = (StmtParameter) src_item;
             StmtParameter dest_stmt_param = (StmtParameter) clone;
 
             if (src_stmt_param.getProcparameter() != null) {
                 Procedure dest_proc = (Procedure) dest_stmt_param.getParent()
                         .getParent();
                 ProcParameter src_proc_param = src_stmt_param
                         .getProcparameter();
                 ProcParameter dest_proc_param = dest_proc.getParameters().get(
                         src_proc_param.getName());
                 if (dest_proc_param == null) {
                     System.out.println("dest_proc:      " + dest_proc);
                     System.out.println("dest_stmt:      "
                             + dest_stmt_param.getParent());
                     System.out.println("src_proc_param: " + src_proc_param);
                     System.out.println("dest_proc.getParameters(): "
                             + CatalogUtil.debug(dest_proc.getParameters()));
                     CatalogUtil.saveCatalog(dest_catalog, "catalog.txt");
                 }
 
                 assert (dest_proc_param != null);
                 dest_stmt_param.setProcparameter(dest_proc_param);
             }
         }
         return (clone);
     }
 
     /**
      * 
      * @param src_db
      * @param dest_db
      */
     public static void cloneConstraints(Database src_db, Database dest_db) {
         Catalog dest_catalog = dest_db.getCatalog();
         for (Table src_tbl : src_db.getTables()) {
             Table dest_tbl = dest_db.getTables().get(src_tbl.getName());
             if (dest_tbl != null) {
                 for (Constraint src_cons : src_tbl.getConstraints()) {
                     // Only clone a FKEY constraint if the other table is in the
                     // catalog
                     ConstraintType cons_type = ConstraintType.get(src_cons
                             .getType());
                     if (cons_type != ConstraintType.FOREIGN_KEY
                             || (cons_type == ConstraintType.FOREIGN_KEY && dest_db
                                     .getTables().get(
                                             src_cons.getForeignkeytable()
                                                     .getName()) != null)) {
                         Constraint dest_cons = CatalogUtil.clone(src_cons,
                                 dest_catalog);
                         assert (dest_cons != null);
                     }
                 } // FOR
             }
         } // FOR
     }
 
     /**
      * 
      * @param from_column
      * @return
      */
     public static Column getForeignKeyParent(Column from_column) {
         assert (from_column != null);
         Column to_column = null;
 
         for (Constraint catalog_const : CatalogUtil.getConstraints(from_column
                 .getConstraints())) {
             if (catalog_const.getType() == ConstraintType.FOREIGN_KEY
                     .getValue()) {
                 assert (!catalog_const.getForeignkeycols().isEmpty());
                 for (ColumnRef catalog_col_ref : catalog_const
                         .getForeignkeycols()) {
                     if (catalog_col_ref.getName().equals(from_column.getName())) {
                         assert (to_column == null);
                         to_column = catalog_col_ref.getColumn();
                         break;
                     }
                 } // FOR
                 if (to_column != null)
                     break;
             }
         } // FOR
 
         return (to_column);
     }
 
     /**
      * Return the list of Sites for a particular host
      * 
      * @param catalog_host
      * @return
      */
     public static List<Site> getSitesForHost(Host catalog_host) {
         List<Site> sites = new ArrayList<Site>();
         Cluster cluster = (Cluster) catalog_host.getParent();
         for (Site catalog_site : cluster.getSites()) {
             if (catalog_site.getHost().getName().equals(catalog_host.getName()))
                 sites.add(catalog_site);
         } // FOR
         return (sites);
     }
 
     /**
      * Returns all the columns for this table that have a foreign key dependency
      * on another table
      * 
      * @param catalog_tbl
      * @return
      */
     public static Set<Column> getForeignKeyDependents(Table catalog_tbl) {
         Set<Column> found = new HashSet<Column>();
         for (Column catalog_col : catalog_tbl.getColumns()) {
             assert (catalog_col != null);
             if (!catalog_col.getConstraints().isEmpty()) {
                 // System.out.println(catalog_col + ": " +
                 // CatalogUtil.getConstraints(catalog_col.getConstraints()));
                 if (!CatalogUtil.findAll(
                         CatalogUtil
                                 .getConstraints(catalog_col.getConstraints()),
                         "type", ConstraintType.FOREIGN_KEY.getValue())
                         .isEmpty()) {
                     found.add(catalog_col);
                 }
             }
         } // FOR
         return (found);
     }
 
     /**
      * Returns all the StmtParameters that are linked to the ProcParameter
      * 
      * @param catalog_stmt
      * @param catalog_proc_param
      * @return
      */
     public static Set<StmtParameter> getStmtParameters(Statement catalog_stmt,
             ProcParameter catalog_proc_param) {
         Set<StmtParameter> found = new HashSet<StmtParameter>();
         for (StmtParameter param : catalog_stmt.getParameters()) {
             if (param.getProcparameter() != null
                     && param.getProcparameter().equals(catalog_proc_param))
                 found.add(param);
         } // FOR
         return (found);
     }
 
     /**
      * 
      * @param <T>
      * @param <U>
      * @param items
      * @param field
      * @param value
      * @return
      */
     public static <T extends CatalogType, U> Set<T> findAll(Iterable<T> items,
             String field, U value) {
         Set<T> found = new HashSet<T>();
         for (T catalog_item : items) {
             assert (catalog_item != null);
             try {
                 Object field_value = catalog_item.getField(field);
                 if (field_value.equals(value))
                     found.add(catalog_item);
             } catch (NullPointerException ex) {
                 System.err.println(catalog_item + ": "
                         + catalog_item.getFields());
                 LOG.fatal(catalog_item + " does not have a field '" + field
                         + "'");
                 throw ex;
             }
         } // FOR
         return (found);
     }
 
     /**
      * 
      * @param <T>
      * @param <U>
      * @param items
      * @param field
      * @param value
      * @return
      */
     public static <T extends CatalogType, U> T findOne(Iterable<T> items,
             String field, U value) {
         return (CollectionUtil.getFirst(CatalogUtil
                 .findAll(items, field, value)));
     }
 
     /**
      * 
      * @param item0
      * @param item1
      * @return
      */
     public static Pair<CatalogType, CatalogType> pair(CatalogType item0,
             CatalogType item1) {
         Pair<CatalogType, CatalogType> pair = null;
         if (item0.compareTo(item1) < 0) {
             pair = Pair.of(item0, item1);
         } else {
             pair = Pair.of(item1, item0);
         }
         return (pair);
     }
 
     /**
      * 
      * @param item
      * @return
      */
     public static String getDisplayName(CatalogType item) {
         return (CatalogUtil.getDisplayName(item, false));
     }
 
     /**
      * 
      * @param item
      * @param include_class
      * @return
      */
     public static String getDisplayName(CatalogType item, boolean include_class) {
         if (item != null) {
             StringBuilder sb = new StringBuilder();
             if (item instanceof Column || item instanceof Statement) {
                 sb.append(item.getParent().getName()).append(".").append(item.getName());
             } else if (item instanceof ProcParameter
                     || item instanceof StmtParameter) {
                 sb.append(item.getParent().getName()).append(".").append(include_class ? item : item.getName());
             } else if (item instanceof PlanFragment) {
                 sb.append(item.getParent().getParent().getName())
                   .append(".")
                   .append(item.getParent().getName())
                   .append("[Fragment #").append(item.getName()).append("]");
             } else if (item instanceof ConstantValue) {
                 sb.append(item.getClass().getSimpleName())
                   .append("{").append(((ConstantValue) item).getValue()).append("}");
             } else {
                 sb.append(include_class ? item.getClass().getSimpleName() + ":" : "")
                   .append(item.getName());
             }
             return (sb.toString());
         }
         return (null);
     }
 
     /**
      * Loads a serialized catalog specification from a jar file and creates a
      * new Catalog object from it
      * 
      * @param jar_path
      * @return
      */
     public static Catalog loadCatalogFromJar(String jar_path) {
         final boolean debug = LOG.isDebugEnabled();
             
         Catalog catalog = null;
         String serializedCatalog = null;
         File file_path = new File(jar_path);
         if (debug) LOG.debug("Loading catalog from jar file at '" + file_path.getAbsolutePath() + "'");
         if (!file_path.exists()) {
             LOG.error("The catalog jar file '" + jar_path + "' does not exist");
             return (null);
         }
         try {
             serializedCatalog = JarReader.readFileFromJarfile(jar_path, CatalogUtil.CATALOG_FILENAME);
         } catch (Exception ex) {
             ex.printStackTrace();
             return (null);
         }
         if (serializedCatalog == null) {
             LOG.warn("The catalog file '" + CatalogUtil.CATALOG_FILENAME
                     + "' in jar file '" + jar_path + "' is null");
         } else if (serializedCatalog.isEmpty()) {
             LOG.warn("The catalog file '" + CatalogUtil.CATALOG_FILENAME
                     + "' in jar file '" + jar_path + "' is empty");
         } else {
             catalog = new Catalog();
             if (debug) LOG.debug("Extracted file '" + CatalogUtil.CATALOG_FILENAME
                                  + "' from jar file '" + jar_path + "'");
             catalog.execute(serializedCatalog);
         }
         return (catalog);
     }
 
     /**
      * Loads a serialized catalog specification from a text file and creates a
      * new Catalog object from it
      * 
      * @param jar_path
      * @return
      */
     public static Catalog loadCatalog(String path) {
         Catalog catalog = null;
         String serializedCatalog = null;
         try {
             serializedCatalog = FileUtil.readFile(path);
         } catch (Exception ex) {
             ex.printStackTrace();
             return (null);
         }
         if (serializedCatalog == null) {
             LOG.warn("The catalog file '" + CatalogUtil.CATALOG_FILENAME
                     + "' in file '" + path + "' is null");
         } else if (serializedCatalog.isEmpty()) {
             LOG.warn("The catalog file '" + CatalogUtil.CATALOG_FILENAME
                     + "' in file '" + path + "' is empty");
         } else {
             catalog = new Catalog();
             LOG.debug("Executing catalog from file '" + path + "'");
             catalog.execute(serializedCatalog);
         }
         return (catalog);
     }
 
     public static void saveCatalog(Catalog catalog, String file_path) {
         File file = new File(file_path);
         try {
             FileUtil.writeStringToFile(file, catalog.serialize());
         } catch (Exception ex) {
             ex.printStackTrace();
             System.exit(1);
         }
         LOG.info("Wrote catalog contents to '" + file.getAbsolutePath() + "'");
     }
 
     /**
      * Convenience method to return the default database object from a catalog
      * 
      * @param catalog
      * @return
      */
     public static Database getDatabase(CatalogType catalog_item) {
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_item);
         assert (catalog_clus != null);
         return (catalog_clus.getDatabases().get(DEFAULT_DATABASE_NAME));
     }
 
     /**
      * Convenience method to return the default cluster object from a catalog
      * 
      * @param catalog
      * @return
      */
     public static Cluster getCluster(CatalogType catalog_item) {
         assert (catalog_item != null) : "Null Catalog Item!";
         if (catalog_item instanceof Cluster)
             return ((Cluster) catalog_item);
         Catalog catalog = catalog_item.getCatalog();
         assert (catalog != null);
         return (catalog.getClusters().get(DEFAULT_CLUSTER_NAME));
     }
 
     /**
      * 
      * @param catalog_db
      * @param table_name
      * @param column_name
      * @return
      */
     public static Column getColumn(Database catalog_db, String table_name,
             String column_name) {
         Column catalog_col = null;
         Table catalog_table = catalog_db.getTables().get(table_name);
         if (catalog_table != null)
             catalog_col = catalog_table.getColumns().get(column_name);
         return (catalog_col);
     }
 
     /**
      * Returns all the tables access/modified in all the Statements for this
      * Procedure
      * 
      * @param catalog_proc
      * @return
      * @throws Exception
      */
     public static Set<Table> getReferencedTables(Procedure catalog_proc)
             throws Exception {
         Set<Table> ret = new HashSet<Table>();
         for (Statement catalog_stmt : catalog_proc.getStatements()) {
             ret.addAll(CatalogUtil.getReferencedTables(catalog_stmt));
         } // FOR
         return (ret);
     }
 
     /**
      * Returns all the columns access/modified in all the Statements for this
      * Procedure
      * 
      * @param catalog_proc
      * @return
      * @throws Exception
      */
     public static Set<Column> getReferencedColumns(Procedure catalog_proc) throws Exception {
         Set<Column> ret = new HashSet<Column>();
         for (Statement catalog_stmt : catalog_proc.getStatements()) {
             ret.addAll(CatalogUtil.getReferencedColumns(catalog_stmt));
         } // FOR
         return (ret);
     }
 
     /**
      * Returns all of the procedures that access/modify the given table
      * 
      * @param catalog_tbl
      * @return
      * @throws Exception
      */
     public static Set<Procedure> getReferencingProcedures(Table catalog_tbl)
             throws Exception {
         Set<Procedure> ret = new HashSet<Procedure>();
         Database catalog_db = CatalogUtil.getDatabase(catalog_tbl);
         for (Procedure catalog_proc : catalog_db.getProcedures()) {
             if (catalog_proc.getSystemproc())
                 continue;
             if (CatalogUtil.getReferencedTables(catalog_proc).contains(
                     catalog_tbl)) {
                 ret.add(catalog_proc);
             }
         } // FOR
         return (ret);
     }
 
     /**
      * Returns all of the procedures that access/modify the given Column
      * 
      * @param catalog_col
      * @return
      * @throws Exception
      */
     public static Set<Procedure> getReferencingProcedures(Column catalog_col)
             throws Exception {
         Set<Procedure> ret = new HashSet<Procedure>();
         Database catalog_db = CatalogUtil.getDatabase(catalog_col.getParent());
 
         // Special Case: ReplicatedColumn
         if (catalog_col instanceof ReplicatedColumn) {
             Table catalog_tbl = catalog_col.getParent();
             for (Column col : catalog_tbl.getColumns()) {
                 for (Procedure catalog_proc : catalog_db.getProcedures()) {
                     if (catalog_proc.getSystemproc())
                         continue;
                     if (CatalogUtil.getReferencedColumns(catalog_proc)
                             .contains(col)) {
                         ret.add(catalog_proc);
                     }
                 } // FOR
             } // FOR
         } else {
             for (Procedure catalog_proc : catalog_db.getProcedures()) {
                 if (catalog_proc.getSystemproc())
                     continue;
                 if (CatalogUtil.getReferencedColumns(catalog_proc).contains(
                         catalog_col)) {
                     ret.add(catalog_proc);
                 }
             } // FOR
         }
         return (ret);
     }
 
     /**
      * Returns all the tables access/modified in the given Statement's query
      * 
      * @param catalog_stmt
      * @return
      * @throws Exception
      */
     public static Set<Table> getReferencedTables(Statement catalog_stmt) throws Exception {
         Set<Table> ret = new HashSet<Table>();
         for (Column catalog_col : CatalogUtil.getReferencedColumns(catalog_stmt)) {
             ret.add((Table) catalog_col.getParent());
         }
         return (ret);
     }
     
     /**
      * Get all the tables referenced in this statement
      * @param catalog_stmt
      * @return
      * @throws Exception
      */
     public static Set<Table> getAllTables(Statement catalog_stmt) throws Exception {
         final Database catalog_db = (Database) catalog_stmt.getParent().getParent();
         AbstractPlanNode node = QueryPlanUtil.deserializeStatement(catalog_stmt, true);
         return (CatalogUtil.getReferencedTables(catalog_db, node));
     }
 
     /**
      * Returns all the columns access/modified in the given Statement's query
      * 
      * @param catalog_stmt
      * @return
      * @throws Exception
      */
     public static Set<Column> getReferencedColumns(Statement catalog_stmt) throws Exception {
         final boolean debug = LOG.isDebugEnabled();
 
         if (debug) LOG.debug("Extracting table set from statement " + CatalogUtil.getDisplayName(catalog_stmt));
         final String catalog_key = CatalogKey.createKey(catalog_stmt);
         if (!CatalogUtil.CACHE_STATEMENT_COLUMNS_KEYS.containsKey(catalog_key)) {
             final Database catalog_db = (Database) catalog_stmt.getParent().getParent();
 
             // 2010-07-14: Always use the AbstractPlanNodes from the PlanFragments to figure out
             // what columns the query touches. It's more accurate because we will pick apart plan nodes
             // and expression trees to figure things out
             AbstractPlanNode node = QueryPlanUtil.deserializeStatement(catalog_stmt, true);
             Set<Column> columns = CatalogUtil.getPartitionableColumnReferences(catalog_db, node);
             assert (columns != null) : "Failed to get catalog tables for " + CatalogUtil.getDisplayName(catalog_stmt);
 
             // Convert to ColumnKeys
             Set<String> column_keys = new HashSet<String>();
             for (Column catalog_col : columns) {
                 column_keys.add(CatalogKey.createKey(catalog_col));
             } // FOR
             CatalogUtil.CACHE_STATEMENT_COLUMNS_KEYS.put(catalog_key, Collections.unmodifiableSet(column_keys));
         }
         Set<Column> ret = new HashSet<Column>();
         Database catalog_db = (Database) catalog_stmt.getParent().getParent();
         for (String column_key : CatalogUtil.CACHE_STATEMENT_COLUMNS_KEYS.get(catalog_key)) {
             Column catalog_col = CatalogKey.getFromKey(catalog_db, column_key, Column.class);
             if (catalog_col != null) ret.add(catalog_col);
         } // FOR
         return (ret);
     }
 
     /**
      * Return all of the columns referenced in the given AbstractPlanNode
      * Non-recursive.
      * @param catalog_db
      * @param node
      * @return
      * @throws Exception
      */
     public static Set<Column> getReferencedColumns(final Database catalog_db, final AbstractPlanNode node) throws Exception {
        final Set<Column> ret = new HashSet<Column>();
         CatalogUtil.getReferencedColumns(catalog_db, node, ret);
         return (ret);
     }
     
     /**
      * Returns all the columns access/modified in the given Statement's query
      * 
      * @param catalog_stmt
      * @return
      * @throws Exception
      */
     private static void getReferencedColumns(final Database catalog_db, final AbstractPlanNode node, final Set<Column> columns) throws Exception {
         switch (node.getPlanNodeType()) {
             // SCANS
             case INDEXSCAN: {
                 IndexScanPlanNode idx_node = (IndexScanPlanNode) node;
                 if (idx_node.getEndExpression() != null)
                     columns.addAll(CatalogUtil.getReferencedColumns(catalog_db, idx_node.getEndExpression()));
                 for (AbstractExpression exp : idx_node.getSearchKeyExpressions()) {
                     if (exp != null)
                         columns.addAll(CatalogUtil.getReferencedColumns(catalog_db, exp));
                 } // FOR
 
                 // Fall through down into SEQSCAN....
             }
             case SEQSCAN: {
                 AbstractScanPlanNode scan_node = (AbstractScanPlanNode) node;
                 if (scan_node.getPredicate() != null)
                     columns.addAll(CatalogUtil.getReferencedColumns(catalog_db, scan_node.getPredicate()));
                 break;
             }
            // JOINS
             case NESTLOOP:
             case NESTLOOPINDEX: {
                 AbstractJoinPlanNode cast_node = (AbstractJoinPlanNode) node;
                 if (cast_node.getPredicate() != null)
                     columns.addAll(CatalogUtil.getReferencedColumns(catalog_db, cast_node.getPredicate()));

                // We always need to look at the inline scan nodes for joins 
                for (AbstractPlanNode inline_node : cast_node.getInlinePlanNodes().values()) {
                    if (inline_node instanceof AbstractScanPlanNode) CatalogUtil.getReferencedColumns(catalog_db, inline_node, columns);
                }
                 break;
             }
             // INSERT
             case INSERT: {
                 // All columns are accessed whenever we insert a new record
                 InsertPlanNode ins_node = (InsertPlanNode) node;
                 Table catalog_tbl = catalog_db.getTables().get(ins_node.getTargetTableName());
                 assert (catalog_tbl != null) : "Missing table " + ins_node.getTargetTableName();
                 CollectionUtil.addAll(columns, catalog_tbl.getColumns());
                 break;
             }
             // UPDATE
             case UPDATE: {
                 // Need to make sure we get both the WHERE clause and the fields that are updated
                 // We need to get the list of columns from the ScanPlanNode below us
                 UpdatePlanNode up_node = (UpdatePlanNode) node;
                 AbstractScanPlanNode scan_node = CollectionUtil.getFirst(PlanNodeUtil.getPlanNodes(up_node, AbstractScanPlanNode.class));
                 assert (scan_node != null) : "Failed to find underlying scan node for " + up_node;
                 columns.addAll(PlanNodeUtil.getOutputColumns(catalog_db, scan_node));
                 break;
             }
             case DELETE:
                 // I don't think we need anything here because all the
                 // columns will get get picked up by the scans that feed into the DELETE
                 break;
             default:
                 // Do nothing...
         } // SWITCH
     }
 
     /**
      * Returns all the tables access/modified in the Expression tree
      * 
      * @param catalog_db
      * @param exp
      * @return
      * @throws Exception
      */
     public static Set<Column> getReferencedColumns(final Database catalog_db, AbstractExpression exp) throws Exception {
         final Set<Column> found_columns = new HashSet<Column>();
         new ExpressionTreeWalker() {
             @Override
             protected void callback(AbstractExpression element) {
                 if (element instanceof TupleValueExpression) {
                     String table_name = ((TupleValueExpression) element)
                             .getTableName();
                     Table catalog_tbl = catalog_db.getTables().get(table_name);
                     if (catalog_tbl == null) {
                         LOG.fatal("Unknown table '" + table_name + "' referenced in Expression node " + element);
                         this.stop();
                         return;
                     }
 
                     String column_name = ((TupleValueExpression) element).getColumnName();
                     Column catalog_col = catalog_tbl.getColumns().get(column_name);
                     if (catalog_col == null) {
                         LOG.fatal("Unknown column '" + table_name + "."
                                 + column_name
                                 + "' referenced in Expression node " + element);
                         this.stop();
                         return;
                     }
                     found_columns.add(catalog_col);
                 }
                 return;
             }
         }.traverse(exp);
         return (found_columns);
     }
 
     /**
      * Return all of tables referenced in the PlanNode tree, regardless if they
      * are modified or not
      * 
      * @param catalog_db
      * @param root
      * @return
      */
     public static Set<Table> getReferencedTables(final Database catalog_db, final AbstractPlanNode root) {
         final Set<Table> found = new HashSet<Table>();
         new PlanNodeTreeWalker() {
             @Override
             protected void populate_children(PlanNodeTreeWalker.Children children, AbstractPlanNode node) {
                 super.populate_children(children, node);
                 // Visit the inline nodes after the parent
                 for (AbstractPlanNode inline_node : node.getInlinePlanNodes()
                         .values()) {
                     children.addAfter(inline_node);
                 }
                 return;
             }
 
             @Override
             protected void callback(AbstractPlanNode element) {
                 String table_name = null;
                 // AbstractScanNode
                 if (element instanceof AbstractScanPlanNode) {
                     AbstractScanPlanNode cast_node = (AbstractScanPlanNode) element;
                     table_name = cast_node.getTargetTableName();
                     assert (table_name != null);
                     assert (!table_name.isEmpty());
                 // AbstractOperationPlanNode
                 } else if (element instanceof AbstractOperationPlanNode) {
                     AbstractOperationPlanNode cast_node = (AbstractOperationPlanNode) element;
                     table_name = cast_node.getTargetTableName();
                     assert (table_name != null);
                     assert (!table_name.isEmpty());
                 }
 
                 if (table_name != null) {
                     Table catalog_tbl = catalog_db.getTables().get(table_name);
                     assert (catalog_tbl != null) : "Invalid table '"
                             + table_name + "' extracted from " + element;
                     found.add(catalog_tbl);
                 }
                 return;
             }
         }.traverse(root);
         return (found);
     }
 
     /**
      * Returns the set of Column catalog objects modified by the given
      * AbstractPlanNode If you're looking for where we figure out what columns a
      * PlanNode touches that is of interest to us for figuring out how we will
      * partition things, then you've come to the right place.
      * 
      * @param catalog_db
      * @param node
      * @return
      * @throws Exception
      */
     public static Set<Column> getPartitionableColumnReferences(final Database catalog_db, AbstractPlanNode node) throws Exception {
         final Set<Column> columns = new TreeSet<Column>();
         new PlanNodeTreeWalker() {
             @Override
             protected void populate_children(
                     PlanNodeTreeWalker.Children children, AbstractPlanNode node) {
                 super.populate_children(children, node);
                 List<AbstractPlanNode> to_add = new ArrayList<AbstractPlanNode>();
                 for (AbstractPlanNode child : children.getBefore()) {
                     to_add.addAll(child.getInlinePlanNodes().values());
                 } // FOR
                 children.addBefore(to_add);
 
                 to_add.clear();
                 for (AbstractPlanNode child : children.getAfter()) {
                     to_add.addAll(child.getInlinePlanNodes().values());
                 } // FOR
                 children.addAfter(to_add);
             };
 
             @Override
             protected void callback(final AbstractPlanNode node) {
                 try {
                     CatalogUtil.getReferencedColumns(catalog_db, node, columns);
                 } catch (Exception ex) {
                     LOG.fatal("Failed to extract columns from " + node, ex);
                     System.exit(1);
                 }
                 return;
             }
         }.traverse(node);
         return (columns);
     }
 
     /**
      * Returns a set of all of the PlanFragments (both multi-partition and
      * single-partition)
      * 
      * @param catalogs_stmt
      * @return
      */
     public static Set<PlanFragment> getAllPlanFragments(Statement catalog_stmt) {
         Set<PlanFragment> frags = new HashSet<PlanFragment>();
         for (PlanFragment catalog_frag : catalog_stmt.getFragments())
             frags.add(catalog_frag);
         for (PlanFragment catalog_frag : catalog_stmt.getMs_fragments())
             frags.add(catalog_frag);
         return (frags);
     }
 
     /**
      * Returns the estimated partition that a procedure will be sent to based on
      * its input parameters
      * 
      * @param catalog_proc
      * @param params
      * @return
      * @throws Exception
      */
     public static int estimatePartition(Procedure catalog_proc, Object params[])
             throws Exception {
         int param_idx = catalog_proc.getPartitionparameter();
         assert (param_idx >= 0);
         TheHashinator.initialize(catalog_proc.getCatalog());
         return (TheHashinator.hashToPartition(params[param_idx]));
     }
 
     /**
      * Returns the estimate size of a tuple in bytes
      * 
      * @param catalog_tbl
      * @return
      */
     public static Long estimateTupleSize(Table catalog_tbl, Statement catalog_stmt, Object params[]) throws Exception {
         long bytes = 0;
 
         //
         // If the table contains nothing but numeral values, then we don't need
         // to loop
         // through and calculate the estimated tuple size each time around,
         // since it's always
         // going to be the same
         //
         if (CatalogUtil.CACHE_TABLE_TUPLE_SIZE.containsKey(catalog_tbl
                 .getName())) {
             return (CatalogUtil.CACHE_TABLE_TUPLE_SIZE.get(catalog_tbl
                     .getName()));
         }
 
         //
         // Otherwise, we have to calculate things.
         // Then pluck out all the MaterializePlanNodes so that we inspect the
         // tuples
         //
         AbstractPlanNode node = QueryPlanUtil.deserializeStatement(
                 catalog_stmt, true);
         Set<MaterializePlanNode> matched_nodes = PlanNodeUtil.getPlanNodes(
                 node, MaterializePlanNode.class);
         if (matched_nodes.isEmpty()) {
             LOG.fatal("Failed to retrieve any MaterializePlanNodes from "
                     + catalog_stmt);
             return 0l;
         } else if (matched_nodes.size() > 1) {
             LOG
                     .fatal("Unexpectadly found more than one MaterializePlanNode in "
                             + catalog_stmt);
             return 0l;
         }
         // MaterializePlanNode mat_node =
         // (MaterializePlanNode)CollectionUtil.getFirst(matched_nodes);
 
         //
         // This obviously isn't going to be exact because they may be inserting
         // from a SELECT statement or the columns might complex
         // AbstractExpressions
         // That's ok really, because all we really need to do is look at size of
         // the strings
         //
         boolean numerals_only = true;
         for (Column catalog_col : CatalogUtil.getSortedCatalogItems(catalog_tbl
                 .getColumns(), "index")) {
             VoltType type = VoltType.get((byte) catalog_col.getType());
             switch (type) {
             case TINYINT:
                 bytes += 1;
                 break;
             case SMALLINT:
                 bytes += 2;
                 break;
             case INTEGER:
                 bytes += 4;
                 break;
             case BIGINT:
             case FLOAT:
             case TIMESTAMP:
                 bytes += 8;
                 break;
             case STRING: {
                 numerals_only = false;
                 if (params[catalog_col.getIndex()] != null) {
                     bytes += 8 * ((String) params[catalog_col.getIndex()])
                             .length();
                 }
                 /*
                  * AbstractExpression root_exp =
                  * mat_node.getOutputColumnExpressions
                  * ().get(catalog_col.getIndex()); for (ParameterValueExpression
                  * value_exp : ExpressionUtil.getExpressions(root_exp,
                  * ParameterValueExpression.class)) { int param_idx =
                  * value_exp.getParameterId(); bytes += 8 *
                  * ((String)params[param_idx]).length(); } // FOR
                  */
                 break;
             }
             default:
                 LOG.warn("Unsupported VoltType: " + type);
             } // SWITCH
         } // FOR
         //
         // If the table only has numerals, then we can store it in our cache
         //
         if (numerals_only)
             CatalogUtil.CACHE_TABLE_TUPLE_SIZE
                     .put(catalog_tbl.getName(), bytes);
 
         return (bytes);
     }
 
     /**
      * 
      * @param catalog_db
      * @param base_partition
      * @return
      */
     public static Set<Integer> getLocalPartitionIds(Database catalog_db, int base_partition) {
         Set<Integer> partition_ids = new HashSet<Integer>();
         for (Partition catalog_proc : CatalogUtil.getLocalPartitions(
                 catalog_db, base_partition)) {
             partition_ids.add(catalog_proc.getId());
         } // FOR
         return (partition_ids);
     }
 
     /**
      * For a given base partition id, return the list of partitions that are on
      * same node as the base partition
      * 
      * @param catalog_db
      * @param base_partition
      * @return
      */
     public static Set<Partition> getLocalPartitions(Database catalog_db, int base_partition) {
         Set<Partition> partitions = new HashSet<Partition>();
 
         // First figure out what partition we are in the catalog
         Cluster catalog_clus = CatalogUtil.getCluster(catalog_db);
         assert (catalog_clus != null);
         Partition catalog_part = CatalogUtil.getPartitionById(catalog_clus, base_partition);
         assert (catalog_part != null);
         Site catalog_site = catalog_part.getParent();
         assert(catalog_site != null);
         Host catalog_host = catalog_site.getHost();
         assert(catalog_host != null);
 
         // Now look at what other partitions are on the same host that we are
         for (Site other_site : catalog_clus.getSites()) {
             if (other_site.getHost().equals(catalog_host) == false) continue;
             LOG.trace(catalog_host + " => " + CatalogUtil.debug(other_site.getPartitions()));
             CollectionUtil.addAll(partitions, other_site.getPartitions());
         } // FOR
         return (partitions);
     }
 
     /**
      * 
      * @param catalog_stmt
      * @return
      */
     public static String debugJSON(Statement catalog_stmt) {
         String jsonString = Encoder.hexDecodeToString(catalog_stmt
                 .getFullplan());
         String line = "\n----------------------------------------\n";
         String ret = "FULL PLAN ORIG STRING:\n" + jsonString + line;
 
         for (PlanFragment catalog_frgmt : catalog_stmt.getFragments()) {
             jsonString = Encoder.hexDecodeToString(catalog_frgmt
                     .getPlannodetree());
             try {
                 JSONObject jsonObject = new JSONObject(jsonString);
                 ret += "FRAGMENT " + catalog_frgmt.getName() + "\n"
                         + jsonObject.toString(2) + line;
             } catch (Exception ex) {
                 ex.printStackTrace();
                 System.exit(1);
             }
         } // FOR
         return (ret);
     }
 
     /**
      * Return a string representation of this CatalogType handle
      * 
      * @param catalog_item
      * @return
      */
     public static String debug(CatalogType catalog_item) {
         StringBuilder buffer = new StringBuilder();
         buffer.append(catalog_item.toString()).append("\n");
         Set<String> fields = new HashSet<String>();
         fields.addAll(catalog_item.getFields());
         fields.addAll(catalog_item.getChildFields());
 
         for (String field : fields) {
             String value = null;
             if (catalog_item.getChildFields().contains(field)) {
                 value = CatalogUtil.debug(catalog_item.getChildren(field));
             } else {
                 value = catalog_item.getField(field).toString();
             }
 
             buffer.append("  ").append(String.format("%-20s", field + ":"))
                     .append(value).append("\n");
         } // FOR
         return (buffer.toString());
     }
     
     public static String debug(CatalogMap<? extends CatalogType> map) {
         String ret = "[ ";
         String add = "";
         for (CatalogType item : map) {
             ret += add + item;
             add = ", ";
         } // FOR
         ret += " ]";
         return (ret);
     }
 
     public static String debug(Collection<? extends CatalogType> items) {
         String ret = "[ ";
         String add = "";
         for (CatalogType item : items) {
             if (item != null) {
                 ret += add + CatalogUtil.getDisplayName(item);
                 add = ", ";
             }
         } // FOR
         ret += " ]";
         return (ret);
     }
 
 } // END CLASS
