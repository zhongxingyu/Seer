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
 
 	public String getQuery(String query, String begin, String end, String db) throws Exception {
 
 		String queryToReturn ="";
 
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
			for (int i =0 ; i!= kws.size(); ++i) 
				System.out.println("KEWORD\t" + kws.get(i));
 		
 			/*for (int i =0 ; i!= cs.size(); ++i) {
 				Object obj = cs.get(i);
 				if(i%2 == 0) {
 					Constraint o = (Constraint)obj;
 					//System.out.println("KEY " + o.getKey() + "\t\tOP " + o.getOp() + "\t\tVAL " + o.getValue());
 	
 				}//else System.out.println("REL " + (String)obj);
 			}*/
 			QueryBuilder qb = new QueryBuilder(db);
 			queryToReturn = qb.genQuery(kws, cs, okws, begin, end);
 			bindValues = qb.getBindValues();
 			bindIntValues = qb.getBindIntValues();
 			countQuery = qb.getCountQuery();
 		} catch (NoViableAltException nvae) {
 			Token t =  nvae.token;
 			String msg = "Invalid Token " + t.getText() + " on line " + t.getLine() + " at column " + t.getCharPositionInLine() + "\n";
 			msg += "QUERY    " + query + "\nPOSITION ";
 			for(int i = 0; i != t.getCharPositionInLine(); ++i) msg += " ";
 			msg += "^\n";
 			//System.out.println("col " + nvae.getColumn());
 			//System.out.println("file name " + nvae.getFilename());
 			//System.out.println("line " + t.getLine());
 			//System.out.println("col " + t.getColumn());
 			//System.out.println("index " + t.getTokenIndex());
 			//System.out.println("position " + t.getCharPositionInLine());
 			//System.out.println("type " + t.getType());
 			//System.out.println("msg " + t.getText());
                         //throw new Exception (nvae.toString());
                         throw new Exception (msg);
 		} catch (Exception e) {
                         throw new Exception (e.toString());
                 }		
 		return queryToReturn;
 		//return "";
 		}
 }
