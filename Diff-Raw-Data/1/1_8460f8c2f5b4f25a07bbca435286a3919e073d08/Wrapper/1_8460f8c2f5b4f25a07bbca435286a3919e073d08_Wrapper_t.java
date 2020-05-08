 package dbs.search.parser;
 
 import org.antlr.runtime.*;
 import java.util.ArrayList;
 import java.io.ByteArrayInputStream;
 import java.util.StringTokenizer;
 import dbs.search.qb.QueryBuilder;
 
 public class Wrapper {
 	private ArrayList bindValues ;
 	private ArrayList bindIntValues ;
 	private String countQuery = "";
 	public ArrayList getBindValues() {
 		return bindValues;
 	}
 	public ArrayList getBindIntValues() {
 		return bindIntValues;
 	}
 	public String getCountQuery() {
 		return countQuery;	
 	}
 
 	public String getQuery(String query, String begin, String end, String db, boolean upper) throws Exception {
 
 		String queryToReturn ="";
 		//System.out.println("Query " + query);
 			
 
 		try{		
 			//ByteArrayInputStream bis = new ByteArrayInputStream(query.getBytes());
 			ANTLRStringStream input = new ANTLRStringStream(query);
 			//ANTLRInputStream input = new ANTLRInputStream();
 		        //ANTLRInputStream input = new ANTLRInputStream(bis);
         		SqlLexer lexer = new SqlLexer(input);
 	        	CommonTokenStream tokens = new CommonTokenStream(lexer);
 	        	SqlParser parser = new SqlParser(tokens);
 		        parser.stmt();
 	
 			ArrayList kws = parser.kws;
 			ArrayList okws = parser.okws;
 			ArrayList cs = parser.constraints;
 			String orderingkw = parser.orderingkw;
 			/*if (orderingkw == null) {
                             System.out.println("orderingkw is NULL");
                         } else {
 			    System.out.println("ordering is "+ orderingkw);
                         }*/
                         // merge ordering and selection keywords to allow users to specify
                         // ordering while not doing this selection, e. g.
                         // find dataset where ... ordering by run
 			for(int i=0; i != okws.size(); ++i)  {
                             String orderby = (String)okws.get(i);
 //                            System.out.println("okws = '" + orderby + "'");
                             int found=0;
                             for(int j=0; j != kws.size(); ++j) {
                                 String name = (String)kws.get(j);
 //                                System.out.println("kws = '" + name+"'");
                                 if (name.equals(orderby)) {
 //                                    System.out.println("Found kws=okws, " + name);
                                     found = 1;
                                 }
                             }
                             if (found == 0) {
 //                                System.out.println("Adding okws to kws, " + orderby);
                                 kws.add(orderby);
                             }
                         }
                         // check if there is no ordering, if so use first acceptable selected
                         // keyword
                         if (okws.size()==0 ) {
                             for (Object o: kws) {
                                 String firstSelName = (String)o;
                                 if (firstSelName.indexOf("(")==-1 && firstSelName.indexOf(".count")==-1 && firstSelName.indexOf("config.content")==-1) {
 //                                System.out.println("Adding default ordering " +firstSelName);
                                     okws.add(firstSelName);
                                     orderingkw="desc";
                                    break;
                                 }
                             }
                         }
 
 			//for (int i =0 ; i!= kws.size(); ++i) 
 			//	System.out.println("KEWORD\t" + kws.get(i));
 		
 			/*for (int i =0 ; i!= cs.size(); ++i) {
 				Object obj = cs.get(i);
 				if(i%2 == 0) {
 					Constraint o = (Constraint)obj;
 					//System.out.println("KEY " + o.getKey() + "\t\tOP " + o.getOp() + "\t\tVAL " + o.getValue());
 	
 				}//else System.out.println("REL " + (String)obj);
 			}*/
 			QueryBuilder qb = new QueryBuilder(db);
 			queryToReturn = qb.genQuery(kws, cs, okws, orderingkw, begin, end, upper);
 			bindValues = qb.getBindValues();
 			bindIntValues = qb.getBindIntValues();
 			countQuery = qb.getCountQuery();
 		} catch (NoViableAltException nvae) {
 			//String msg = makeExcepMsg(nvae.token);
 			/*
 			Token t =  nvae.token;
 			String msg = "Invalid Token " + t.getText() + " on line " + t.getLine() + " at column " + t.getCharPositionInLine() + "\n";
 			msg += "QUERY    " + query + "\nPOSITION ";
 			int pos = t.getCharPositionInLine();
 			if ( pos > 0 )	for(int i = 0; i != pos; ++i) msg += " ";
 			//for(int i = 0; i != pos; ++i) msg += " ";
 			msg += "^\n";*/
 
                         throw new Exception (makeExcepMsg(nvae.token, query));
 		} catch (MismatchedSetException mse) {
 			throw new Exception (makeExcepMsg(mse.token, query));
 		} catch (Exception e) {
                         throw new Exception (e.toString());
                 }		
 		return queryToReturn;
 		//return "";
 		}
 	private String makeExcepMsg(Token t, String query) {
 			String msg = "Invalid Token " + t.getText() + " on line " + t.getLine() + " at column " + t.getCharPositionInLine() + "\n";
 			msg += "QUERY    " + query + "\nPOSITION ";
 			int pos = t.getCharPositionInLine();
 			if ( pos > 0 )	for(int i = 0; i != pos; ++i) msg += " ";
 			//for(int i = 0; i != pos; ++i) msg += " ";
 			msg += "^\n";
 			return msg;
 
 	}
 }
