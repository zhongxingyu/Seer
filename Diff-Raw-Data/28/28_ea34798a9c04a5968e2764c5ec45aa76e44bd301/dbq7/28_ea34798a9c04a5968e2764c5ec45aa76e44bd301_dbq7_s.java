 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

 import operators.join.SimpleJoin.ReduceSideJoin;
 import operators.selection.SelectionEntry;
 import operators.selection.SelectionFilter;
 
 import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
 import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
 
 import relations.Relation;
 import relations.Schema;
import sun.rmi.log.ReliableLog;
 
 public class dbq7 {
 
 	// TODO: More robust way of initializing schemas
 	// TODO: fill in the fields
 	static Schema schemaMgrSupplier = new Schema(Schema.SUPPLIER_FIELDS);
 	static Schema schemaMgrNations = new Schema(Schema.NATIONS_FIELDS);
 	static Schema schemaMgrLineItem = new Schema(Schema.LINEITEM_FIELDS);
 
 	// TODO: nice way of handling schema changes, including projections
 	// TODO: check which comes first in the actual schema!
 	static Schema schemaMgrN1Supplier = schemaMgrNations.join(schemaMgrSupplier);
 
 	static Schema schemaImdN1Supplier_LineItem = schemaMgrN1Supplier.join(schemaMgrLineItem);
 
 	static String inputFilesPrefix = "/team8/input/";
 	static String tmpFilesPrefix = "/team8/temp/";
 
 	static String inSupplier = inputFilesPrefix + "supplier.tbl";
 	static String inNations = inputFilesPrefix + "nation.tbl";
 	static String inLineItem = inputFilesPrefix + "lineitem.tbl";
 	static String inCustomer = inputFilesPrefix + "customer.tbl";
 	static String inOrder = inputFilesPrefix + "orders.tbl";
 
 	static Relation relSupplier = new Relation(schemaMgrSupplier, inSupplier);
 	static Relation relNations = new Relation(schemaMgrNations, inNations);
 	static Relation relLineItem = new Relation(schemaMgrLineItem, inLineItem);
 
 	// Intermediate result files
 	static Relation relImdN1Supplier = new Relation(schemaMgrN1Supplier, tmpFilesPrefix + "imdSupplier");
 	static Relation relImdN1SupplierLineItem = new Relation(schemaImdN1Supplier_LineItem, tmpFilesPrefix + "imdSupplierLineItem");
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 
 		JobControl jobs = buildJobs();
 		jobs.run();
 		System.exit(0);
 	}
 
 	public static JobControl buildJobs() throws IOException {
 		JobControl jbcntrl = new JobControl("jbcntrl");
 
 		// ======
 		// map-side join o(n1) |><| supplier
 		// =========
 		// TODO: this still will have to know about selections and projections
 
 		JobConf job_n1_suppliers_conf = ReduceSideJoin.getConf(relSupplier, relNations, "NATIONKEY", relImdN1Supplier);
 		// add selection: TODO: for now default selection type is OR
 		ArrayList<SelectionEntry<String>> nationFilters = new ArrayList<SelectionEntry<String>>();
 
		nationFilters.add(new SelectionEntry("NAME", "FRANCE"));
		nationFilters.add(new SelectionEntry("NAME", "GERMANY"));
 
 		SelectionFilter.addSelectionsToJob(job_n1_suppliers_conf, ReduceSideJoin.PREFIX_JOIN_SMALLER, nationFilters, relNations.schema);
 
 		ControlledJob job_n1_suppliers = new ControlledJob(job_n1_suppliers_conf);
 		job_n1_suppliers.setJobName("job_n1_suppliers");
 		jbcntrl.addJob(job_n1_suppliers);
 
 		System.out.println("Join schema:" + schemaMgrN1Supplier.toString() + " ind of SUPPKEY=" + schemaMgrN1Supplier.columnIndex("SUPPKEY"));
 		// map-side join LineItem with o(n1) |><| supplier
 		JobConf job_n1_suppliers_lineitem_conf = ReduceSideJoin.getConf(relLineItem, relImdN1Supplier, "SUPPKEY", relImdN1SupplierLineItem);
 		ControlledJob job_n1_suppliers_lineitem = new ControlledJob(job_n1_suppliers_lineitem_conf);
 
 		// set job dependencies
 		job_n1_suppliers_lineitem.addDependingJob(job_n1_suppliers);
 		jbcntrl.addJob(job_n1_suppliers_lineitem);
 
 		/**
 		 * TODO: This filter we will have to enforce once more again after n2
 		 * and n1 was joined together!!! ( (n1.n_name = '[NATION1]' and
 		 * n2.n_name = '[NATION2]') or (n1.n_name = '[NATION2]' and n2.n_name =
 		 * '[NATION1]') )
 		 */
 
 		return jbcntrl;
 
 	}
 
 }
