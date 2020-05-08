 package ch.epfl.ad.app;
 
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import ch.epfl.ad.AbstractQuery;
 import ch.epfl.ad.db.parsing.Aggregate;
 import ch.epfl.ad.db.parsing.AggregateField;
 import ch.epfl.ad.db.parsing.ExpressionField;
 import ch.epfl.ad.db.parsing.Field;
 import ch.epfl.ad.db.parsing.FunctionField;
 import ch.epfl.ad.db.parsing.LiteralOperand;
 import ch.epfl.ad.db.parsing.NamedField;
 import ch.epfl.ad.db.parsing.NamedRelation;
 import ch.epfl.ad.db.parsing.Operand;
 import ch.epfl.ad.db.parsing.Operator;
 import ch.epfl.ad.db.parsing.OrderingItem;
 import ch.epfl.ad.db.parsing.Parser;
 import ch.epfl.ad.db.parsing.Qualifier;
 import ch.epfl.ad.db.parsing.QueryRelation;
 import ch.epfl.ad.db.parsing.Relation;
 import ch.epfl.ad.db.querytackling.GraphProcessor;
 import ch.epfl.ad.db.querytackling.QueryGraph;
 
 public class TestQuery extends AbstractQuery {
 
 	@Override
 	public void run(String[] args) throws SQLException, InterruptedException {
 		
 		/*if (args.length < 1) {
 		    System.out.println("Arguments: config-file");
 		    System.exit(1);
 		}
 		
 		DatabaseManager dbManager = createDatabaseManager(args[0]);
 		
 		dbManager.setResultShipmentBatchSize(5000);*/
 		
 		QueryRelation q = new Parser().parse("select extract(year from max(t.cid)), 2, 5 * 3 + sum(t.kk) from t group by t.sid order by 2 * t.sid, min(t.cid) desc");
 		System.out.println(q.toIntermediateString());
 		System.out.println(q.toFinalString(new NamedRelation("k")));
 		
 		/* Test query 1 */
 		
 		String query1 =
 				"SELECT S.id " +
 				"FROM S " +
 				"WHERE NOT EXISTS (" +
 				                  "SELECT C.id " +
 				                  "FROM C " +
 				                  "WHERE NOT EXISTS (" +
 				                                    "SELECT T.sid " +
 				                                    "FROM T " +
 				                                    "WHERE S.id = T.sid AND " +
 				                                          "C.id = T.cid" +
 				                                   ")" +
 				                 ")";
 		
 		Relation s = new NamedRelation("S");
 		Relation c = new NamedRelation("C");
 		Relation t = new NamedRelation("T");
 		
 		NamedField sId = new NamedField(s, "id");
 		NamedField cId = new NamedField(c, "id");
 		NamedField tSid = new NamedField(t, "sid");
 		NamedField tCid = new NamedField(t, "cid");
 		
 		QueryRelation tree1m = new QueryRelation(
 				sId,
 				s
 				).setQualifier(new Qualifier(Operator.NOT_EXISTS, new QueryRelation(
 						cId,
 						c
 						).setQualifier(new Qualifier(Operator.NOT_EXISTS, new QueryRelation(
 								tSid,
 								t
 								).setQualifiers(new LinkedList<Qualifier>(Arrays.asList(
 										new Qualifier(Operator.EQUALS, new LinkedList<Operand>(Arrays.<Operand>asList(sId, tSid))),
 										new Qualifier(Operator.EQUALS, new LinkedList<Operand>(Arrays.<Operand>asList(cId, tCid)))
 										))
 								))
 						))
 				);
 		
 		//QueryRelation tree1 = new Parser().parse(query1);
 		
 		System.out.println(query1);
 		System.out.println(tree1m);
 		//System.out.println(tree1);
 		
 		//QueryGraph graph1 = new QueryGraph(tree1);
 		//System.out.println(graph1);
 		//process(graph1);
 		
 		/* Test query 2 */
 		
 		String query2 =
 				"SELECT myS.id " +
 				"FROM (" +
 				       "SELECT S.id " +
 				       "FROM S" +
 				     ") myS, " +
 				     "(" +
 				      "SELECT T.sid " +
 				      "FROM T" +
 				     ") myT " +
 				"WHERE myS.id = myT.sid";
 		
 		// (SELECT S.id FROM S) myS
 		Relation sId_S = new QueryRelation(sId, s).setAlias("myS");
 		
 		// (SELECT T.sid FROM T) myT
 		Relation tSid_T = new QueryRelation(tSid, t).setAlias("myT");
 		
 		NamedField mySId = new NamedField(sId_S, "id");
 		NamedField myTSid = new NamedField(tSid_T, "sid");
 		
 		QueryRelation tree2m = new QueryRelation(
 				mySId,
 				new LinkedList<Relation>(Arrays.<Relation>asList(sId_S, tSid_T))
 				).setQualifier(
 						new Qualifier(Operator.EQUALS, new LinkedList<Operand>(Arrays.<Operand>asList(mySId, myTSid)))
 				);
 		
 		QueryRelation tree2 = new Parser().parse(query2);
 		
 		System.out.println(query2);
 		System.out.println(tree2m);
 		System.out.println(tree2);
 		
 		QueryGraph graph2 = new QueryGraph(tree2);
 		System.out.println(graph2);
 		process(graph2);
 		
 		/* Test query 3 */
 		
 		String query3 =
 				"SELECT C.id " +
 				"FROM C " +
 				"WHERE EXISTS (" +
 				              "SELECT myPT.* " +
 				              "FROM (" +
 				                    "SELECT T.sid, P.cid " +
 				                    "FROM P, T " +
 				                    "WHERE P.cid = C.id" +
 				                   ") myPT " +
 				              "WHERE EXISTS (" +
 				                            "SELECT S.id " +
 				                            "FROM S " +
 				                            "WHERE S.id = myPT.sid" +
 				                           ")" +
 				             ")";
 		
 		Relation p = new NamedRelation("P");
 		NamedField pCid = new NamedField(p, "cid");
 		
 		// (SELECT T.sid, P.cid FROM P, T WHERE P.cid = C.id) myPT
 		Relation tSidpCid_PT = new QueryRelation(
 				new LinkedList<Field>(Arrays.<Field>asList(tSid, pCid)),
 				new LinkedList<Relation>(Arrays.<Relation>asList(p, t))
 				).setQualifier(
 						new Qualifier(Operator.EQUALS, new LinkedList<Operand>(Arrays.<Operand>asList(pCid, cId)))
 				).setAlias("myPT");
 		
 		NamedField myPTAll = new NamedField(tSidpCid_PT, "*");
 		NamedField myPTSid = new NamedField(tSidpCid_PT, "sid");
 		
 		// SELECT S.id FROM S WHERE s.id = myPT.sid
 		Relation sId_S2 = new QueryRelation(
 				sId,
 				s
 				).setQualifier(
 						new Qualifier(Operator.EQUALS, new LinkedList<Operand>(Arrays.<Operand>asList(sId, myPTSid)))
 				);
 		
 		QueryRelation tree3m = new QueryRelation(
 				cId,
 				c
 				).setQualifier(
 						new Qualifier(Operator.EXISTS, new QueryRelation(
 								myPTAll,
 								tSidpCid_PT
 								).setQualifier(
 										new Qualifier(Operator.EXISTS, sId_S2)
 								))
 				);
 		
 		//QueryRelation tree3 = new Parser().parse(query3);
 		
 		System.out.println(query3);
 		System.out.println(tree3m);
 		tree3m.replaceRelation(tSidpCid_PT, new NamedRelation("cochon"));
 		System.out.println(tree3m);
 		//System.out.println(tree3);
 		
 		//QueryGraph graph3 = new QueryGraph(tree3);
 		//System.out.println(graph3);
 		//process(graph3);
 		
 		/* TPCH Query 7 */
 		
 		String q7 = // removed ORed Germany/France
 				"SELECT shipping.supp_nation, shipping.cust_nation, shipping.l_year, SUM(shipping.volume) AS revenue " +
 		        "FROM (" +
 				      "SELECT n1.n_name AS supp_nation, n2.n_name AS cust_nation, EXTRACT(YEAR FROM lineitem.l_shipdate) AS l_year, lineitem.l_extendedprice * (1 - lineitem.l_discount) AS volume " +
 		              "FROM supplier, lineitem, orders, customer, nation n1, nation n2 " +
 				      "WHERE supplier.s_suppkey = lineitem.l_suppkey AND " +
 		                    "orders.o_orderkey = lineitem.l_orderkey AND " +
 				            "customer.c_custkey = orders.o_custkey AND " +
 		                    "supplier.s_nationkey = n1.n_nationkey AND " +
 				            "customer.c_nationkey = n2.n_nationkey AND " +
 		                    "n1.n_name = 'GERMANY' AND " +
 				            "n2.n_name = 'FRANCE' AND " +
 		                    "lineitem.l_shipdate BETWEEN '1995-01-01' AND '1996-12-31'" +
 				     ") shipping " +
 		        "GROUP BY shipping.supp_nation, shipping.cust_nation, shipping.l_year " +
 				"ORDER BY shipping.supp_nation, shipping.cust_nation, shipping.l_year";
 		
 		Relation supplier = new NamedRelation("supplier");
 		Relation lineitem = new NamedRelation("lineitem");
 		Relation orders = new NamedRelation("orders");
 		Relation customer = new NamedRelation("customer");
 		Relation nation1 = new NamedRelation("nation").setAlias("n1");
 		Relation nation2 = new NamedRelation("nation").setAlias("n2");
 
 		NamedField s_suppkey = new NamedField(supplier, "s_suppkey");
 		NamedField l_suppkey = new NamedField(lineitem, "l_suppkey");
 		NamedField o_orderkey = new NamedField(orders, "o_orderkey");
 		NamedField l_orderkey = new NamedField(lineitem, "l_orderkey");
 		NamedField c_custkey = new NamedField(customer, "c_custkey");
 		NamedField o_custkey = new NamedField(orders, "o_custkey");
 		NamedField s_nationkey = new NamedField(supplier, "s_nationkey");
 		NamedField n1_nationkey = new NamedField(nation1, "n_nationkey");
 		NamedField c_nationkey = new NamedField(customer, "c_nationkey");
 		NamedField n2_nationkey = new NamedField(nation2, "n_nationkey");
 		NamedField n1_name = new NamedField(nation1, "n_name").setAlias("supp_nation");
 		NamedField n2_name = new NamedField(nation2, "n_name").setAlias("cust_nation");
 		NamedField l_shipdate = new NamedField(lineitem, "l_shipdate");
 		FunctionField extract_l_year = new FunctionField(
 				"EXTRACT",
 				new ExpressionField("YEAR FROM " + ExpressionField.PLACEHOLDER + "1", l_shipdate)
 				).setAlias("l_year");
 		NamedField l_extendedprice = new NamedField(lineitem, "l_extendedprice");
 		NamedField l_discount = new NamedField(lineitem, "l_discount");
 		ExpressionField volume = new ExpressionField(
 				ExpressionField.PLACEHOLDER + "1 * (1 - " + ExpressionField.PLACEHOLDER + "2)",
 				new LinkedList<Field>(Arrays.<Field>asList(l_extendedprice, l_discount))
 				).setAlias("volume");
 		
 		Relation q7Shipping = new QueryRelation(
 				new LinkedList<Field>(Arrays.<Field>asList(
 						n1_name,
 						n2_name,
 						extract_l_year,
 						volume
 						)),
 				new LinkedList<Relation>(Arrays.<Relation>asList(
 						supplier,
 						lineitem,
 						orders,
 						customer,
 						nation1,
 						nation2
 						))
 				).setQualifiers(new LinkedList<Qualifier>(Arrays.asList(
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							s_suppkey,
 							l_suppkey
 							)
 						)),
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							o_orderkey,
 							l_orderkey
 							)
 						)),
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							c_custkey,
 							o_custkey
 							)
 						)),
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							s_nationkey,
 							n1_nationkey
 							)
 						)),
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							c_nationkey,
 							n2_nationkey
 							)
 						)),
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							n1_name,
 							new LiteralOperand("'GERMANY'")
 							)
 						)),
 					new Qualifier(
 						Operator.EQUALS,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							n2_name,
 							new LiteralOperand("'FRANCE'")
 							)
 						)),
 					new Qualifier(
 						Operator.BETWEEN,
 						new LinkedList<Operand>(Arrays.<Operand>asList(
 							l_shipdate,
 							new LiteralOperand("'1995-01-01'"),
 							new LiteralOperand("'1996-12-31'")
 								)
 							)
 						)))
 				).setAlias("shipping");
 		
 		NamedField supp_nation = new NamedField(q7Shipping, n1_name);
 		NamedField cust_nation = new NamedField(q7Shipping, n2_name);
 		NamedField l_year = new NamedField(q7Shipping, extract_l_year);
 		AggregateField revenue = new AggregateField(
 				Aggregate.SUM,
 				new NamedField(q7Shipping, volume)
 				).setAlias("revenue");
 		
 		QueryRelation treeQ7m = new QueryRelation(
 				new LinkedList<Field>(Arrays.<Field>asList(
 						supp_nation,
 						cust_nation,
 						l_year,
 						revenue
 						)),
 				q7Shipping
 				).setGrouping(new LinkedList<Field>(Arrays.<Field>asList(
 						supp_nation,
 						cust_nation,
 						l_year
 						))
 				).setOrdering(new LinkedList<OrderingItem>(Arrays.asList(
 						new OrderingItem(supp_nation),
 						new OrderingItem(cust_nation),
 						new OrderingItem(l_year)
 						))
 				);
 		
 		QueryRelation treeQ7 = new Parser().parse(q7);
 		
 		System.out.println(q7);
 		System.out.println(treeQ7m);
 		treeQ7m.replaceRelation(q7Shipping, new NamedRelation("creeping"));
 		System.out.println(treeQ7m);
 		System.out.println(treeQ7);
 		
 		QueryGraph graphQ7 = new QueryGraph(treeQ7);
 		System.out.println(graphQ7);
 		process(graphQ7);
 		
 		/* TPCH Query 3 */
 		
		String q3 = // unmodified!
 				"select lineitem.l_orderkey, sum(lineitem.l_extendedprice * (1 - lineitem.l_discount)) as revenue, orders.o_orderdate, orders.o_shippriority " +
 				"from customer, orders, lineitem " +
 				"where customer.c_mktsegment = 'BUILDING' and " +
 				      "customer.c_custkey = orders.o_custkey and " +
 				      "lineitem.l_orderkey = orders.o_orderkey and " +
 				      "orders.o_orderdate < '1995-03-15' and " +
 				      "lineitem.l_shipdate > '1995-03-15' " +
 				"group by lineitem.l_orderkey, orders.o_orderdate, orders.o_shippriority " +
 				"order by revenue desc, orders.o_orderdate"; // revenue is an alias, cannot be qualified by relation
 		
 		QueryRelation treeQ3 = new Parser().parse(q3);
 		
 		System.out.println(q3);
 		System.out.println(treeQ3);
 		
 		QueryGraph graphQ3 = new QueryGraph(treeQ3);
 		System.out.println(graphQ3);
		process(graphQ3);
 		
 		/* Test query 4 */
 		
 		String query4 = "select a.id from (select b.id from (select c.id from c where c.id not in (select avg(d.id) from d group by d.blah)) b, x where b.id = x.bid and x.cochon >= 5) a";
 		QueryRelation tree4 = new Parser().parse(query4);
 		
 		System.out.println(query4);
 		System.out.println(tree4);
 		
 		QueryGraph graph4 = new QueryGraph(tree4);
 		System.out.println(graph4);
 		process(graph4);
 	}
 	
 	private void process(QueryGraph g) {
 		
 		//DigestedGraph dg = GraphEater.eatGraph(g);
 		
 		GraphProcessor gp = new GraphProcessor(g);
 		
 		System.out.println("DONE\n\n##########################");
 		
 	}
 	
 	public static void main(String[] args) throws SQLException, InterruptedException {
 		new TestQuery().run(args);
 	}
 }
