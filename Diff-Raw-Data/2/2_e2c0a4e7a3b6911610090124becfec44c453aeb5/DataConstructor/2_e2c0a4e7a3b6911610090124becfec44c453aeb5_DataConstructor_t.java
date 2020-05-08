 //proposed process
 package supersql.dataconstructor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.sun.tools.doclets.internal.toolkit.resources.doclets;
 
 import supersql.codegenerator.AttributeItem;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.db.GetFromDB;
 import supersql.extendclass.ExtList;
 import supersql.parser.SSQLparser;
 
 import supersql.db.ConnectDB;
 
 public class DataConstructor {
 
 	ExtList data_info;
 
 	ConnectDB cdb;
 	ArrayList<SQLQuery> sqlQueries = null;
 	QueryDivider qd; 
 	String key = null;
 	Attribute keyAtt = null;
 	int col = -1;
 	long exectime[] = new long[4];
 	final int ISDIVIS = 0;
 	final int MAKESQL = 1;
 	final int EXECSQL = 2;
 	final int MKETREE = 3;
 	boolean flag = true;
 	public static String SQL_string;	//added by goto 20130306  "FROMʤк"
 	
 	public DataConstructor(SSQLparser parser) {
 
 		ExtList sep_sch;
 		ExtList sep_data_info;
 		
 		MakeSQL msql = null;
 
 		//Make schema
 		sep_sch = parser.get_TFEschema().makesch();
 		System.out.println("Schema: " + sep_sch);
 		
 		//Check Optimization Parameters
 		if ( GlobalEnv.getOptLevel() == 0 || !GlobalEnv.isOptimizable() || SSQLparser.isDbpediaQuery())
 		{
 			sqlQueries = null;
 		}
 		else
 		{
 			//Initialize QueryDivider
 			long start = System.nanoTime();
 			
 			try 
 			{
 			    qd = new QueryDivider( parser );
 			
 			
 			    if ( qd.MakeGraph() )
 			    {
 			    	//if graph was made successfully, divide
 			    	sqlQueries = qd.divideQuery();	
 			    }
 			}
 			catch ( Exception e )
 			{
 				;
 				//System.out.println( e.getMessage() );		//commented out by goto 20120620
 			}
 			
 			long end = System.nanoTime(); 
 			exectime[ISDIVIS] = end - start;
 		}
 		
 		//Make SQL
 		if ( (sqlQueries == null || sqlQueries.size() < 2) && !SSQLparser.isDbpediaQuery())
 		{
 			//if graph was not made successfully or
 			//if graph has only one connected component
 			//query cannot be divided
 			msql = new MakeSQL(parser);
 		}
 
 		sep_data_info = new ExtList();
 		if(SSQLparser.isDbpediaQuery()){
 			sep_data_info = schemaToData(parser, sep_sch, sep_data_info);
 		}
 		else{
 			sep_data_info = schemaToData(parser, msql, sep_sch, sep_data_info);
 		}
 		data_info = sep_data_info;
 
 		//changed by goto 20120620 start
 		/*
 		System.out.print( "exec_time = " );
 		for (int i = 0; i < 4; i ++ )
 			System.out.print( (exectime[i]/1000000.0) + "\t");
 		System.out.println();
 		*/
 		//changed by goto 20120620 end
 		
 		Log.out("## Result ##");
 		Log.out(data_info);
 	}
 
 	private ExtList schemaToData(SSQLparser parser, ExtList sep_sch,
 			ExtList sep_data_info) {
 		int attno = parser.get_att_info().size();
 		String[] array = new String[attno];
 		int i = 0;
 		for(Object info : parser.get_att_info().values()){
 			String infoText = ((AttributeItem)info).toString();
 			array[i] = infoText;
 			i++;
 		}
 		sep_data_info = getDataFromDBPedia(parser.get_where_info().getSparqlWhereQuery(), array);
 		sep_data_info = makeTree(sep_sch, sep_data_info);
 		return sep_data_info;
 	}
 
 	private ExtList schemaToData(SSQLparser parser, MakeSQL msql, ExtList sep_sch,
 			ExtList sep_data_info) {
 
 		long start, end;
 		
 		if ( msql != null )
 		{
 		    getFromDB(msql, sep_sch, sep_data_info);
 		    sep_data_info = makeTree(sep_sch, sep_data_info);
 			
 		}
 		else 
 		{
 	        getTuples(sep_sch, sep_data_info);
 	        start = System.nanoTime();
 		    sep_data_info = MakeTree( qd.getSchema() );
 		    //System.out.println(sep_data_info);
 		    end = System.nanoTime();
 		    
 		    exectime[MKETREE] = end - start;
 		}
 		
         return sep_data_info;
 		
 	}
 
 	private ExtList[] getTuples( ExtList sep_sch, ExtList sep_data_info) {
 
 		long start, end;
 		start = System.nanoTime();
 		
 		ExtList[] table;
 		GetFromDB gfd;
 		int comp_size;
 		
 		comp_size = sqlQueries.size();
 		table = new ExtList[comp_size];
 		
 		if(GlobalEnv.isMultiThread())
 		{
 			System.out.println("[Enter MultiThread mode]");
 			ConnectDB cdb = new ConnectDB(GlobalEnv.geturl(),GlobalEnv.getusername(), GlobalEnv.getDriver(), GlobalEnv.getpassword());
 			System.out.println(GlobalEnv.geturl() + GlobalEnv.getusername() + GlobalEnv.getpassword());
 		
 			cdb.setName("CDB1");
 			cdb.run();
 		
 			gfd = new GetFromDB(cdb);
 		}
 
 		else{
 			gfd = new GetFromDB();
 		}
 
         long time = 0;
 
         //changed by goto 20120630
         System.out.println("sqlQueries.size() = "+sqlQueries.size());
         //for (int i = 0; i < sqlQueries.size(); i++)
         for (int i = 0; i < sqlQueries.size()-1; i++)       
 		{
 			table[i] = new ExtList();
 			
 			long time1 = System.nanoTime();
 			String s = sqlQueries.get(i).getString();
 			time += (System.nanoTime() - time1);
 			
 			gfd.execQuery( s, table[i] );
 			sqlQueries.get(i).setResult(table[i]);
 		}
 
 		gfd.close();
 		end = System.nanoTime();
 		
 		exectime[EXECSQL] = end - start - time;
 		exectime[MAKESQL] = time;
 		
 		Log.out("## DB result ##");
 		
 		return table;
 
 	}
 
 	private ExtList getFromDB(MakeSQL msql, ExtList sep_sch,
 			ExtList sep_data_info) {
 
         //MakeSQL
 		long start, end;
 		start = System.nanoTime();                
 		
 		SQL_string = msql.makeSQL(sep_sch);
 		
 		end = System.nanoTime();
 		exectime[MAKESQL] = end - start;
 		Log.out("## SQL Query ##");
 		Log.out(SQL_string);
 
 		//Connect to DB
 		start = System.nanoTime(); 
 		
 		GetFromDB gfd;
 		if(GlobalEnv.isMultiThread())
 		{
 			System.out.println("[Enter MultiThread mode]");
 			ConnectDB cdb = new ConnectDB(GlobalEnv.geturl(),GlobalEnv.getusername(), GlobalEnv.getDriver(), GlobalEnv.getpassword());
 			System.out.println(GlobalEnv.geturl() + GlobalEnv.getusername() + GlobalEnv.getpassword());
 
 			cdb.setName("CDB1");
 			cdb.run();
 		
 			gfd = new GetFromDB(cdb);
 		}
 
 		else{
 			gfd = new GetFromDB();
 		}
 
 		gfd.execQuery(SQL_string, sep_data_info);
 
 		gfd.close();
         
 		end = System.nanoTime(); 
 		exectime[EXECSQL] = end - start;
 		
 		Log.out("## DB result ##");
 		Log.out(sep_data_info);
 
 		return sep_data_info;
 
 	}
 
 	private ExtList makeTree(ExtList sep_sch, ExtList sep_data_info) {
 
         //MakeTree
         long start, end;
         start = System.nanoTime();
         
 		TreeGenerator tg = new TreeGenerator();
 
 		sep_data_info = tg.makeTree(sep_sch, sep_data_info);
 	
 		end = System.nanoTime();
 		
 		exectime[MKETREE] = end - start;
 		
 		Log.out("## constructed Data ##");
 		Log.out(sep_data_info);
 
 		return sep_data_info;
 	}
 
 	public ExtList getData() {
 		return data_info;
 	}
 	
 	private ExtList MakeTree( ExtList schema ) 
 	{
 		//added by ria
 	    Object o;
 	    ExtList buf = new ExtList();
 	    for ( int i = 0; i < schema.size(); i++ )
 	    {
 	    	o = schema.get( i );
 	    	
 	    	if ( !(o instanceof ExtList) )
 	    	{
 	    		if ( keyAtt == null )
 	    		{
 	    			keyAtt = (Attribute) o;
 		    		buf.add( keyAtt.getTuple() );
 		    		//System.out.println(buf);
 		    		key = keyAtt.getTuple().toString();
 		    		col = keyAtt.getColumn();
 	    		}
 	    		else
 	    		{
 	    			Attribute a = (Attribute) o;
 	    			if ( a == keyAtt )
 	    			{
 	    				buf.add( keyAtt.getTuple() );
 	    				//System.out.println(buf);
 			    		key = keyAtt.getTuple().toString();
 	    			}
 	    			else
 	    			{
 	    				//add here checking if the keyAtt is a connector
 	    				buf.add(a.getTuple(key, col));
 	    				//System.out.println(buf);
 	    				a.delTuples(key, col);
 	    			}
 	    		}
 	    	}
 	    	else if ( IsLeaf( (ExtList) o ) )
 	    	{
 	    		
 	    		ExtList obj = (ExtList) o;
 	    		ExtList temp = new ExtList();
 
 	    		Attribute a = (Attribute) obj.get(0);
 		    	temp.addAll((a.getTuples( key, col )));
 		    	
 		    	if ( temp.size() == 0 )
 		    	{
 		    		flag = false;
 		    	}
 		    	else 
 		    	{
 		    		flag = true;
 		    	}
 		    	
 		    	buf.add(temp);
 		    	//System.out.println(buf);
 	    		
 	    		if (keyAtt != null )
 	    		{
 	    		    keyAtt.delTuples( key, col );
 	    		}
 
 	    	}
 	    	else 
 	        {
 	    		if ( schema.size() == 1 )
 	    		{
 	    			ExtList temp = new ExtList();
 	    			do
 	    			{
 	    				ExtList temp2 = MakeTree( (ExtList) o );
 	    				if ( !temp2.isEmpty() ) 
 	    			    {
 	    					temp.add( temp2 );
 	    					if ( keyAtt != null )
 	    					{
 	    						keyAtt.delTuples( key, col );
 	    					}
 	    				}
 	    			} while ( (keyAtt != null) && keyAtt.getSize() != 0 );
 	    	    	
 	    			buf.add( temp );
 	    			//System.out.println(buf);
 	    	    	flag = true;
 	    		}
 	    		else
 	    		{
 	    			ExtList temp = new ExtList();
     	    		temp.add( MakeTree( (ExtList) o ) );
     	    		
     	    		if (flag)
     	    		{
     	    		    buf.add( temp );
     	    		    //System.out.println(buf);
     	    		}
 	    		}
 	        }
 	    }
 	    if ( !flag ) {
 	        buf = new ExtList();
 	    }
 	    
 	    return buf;
 	}
 	
 	private boolean IsLeaf(ExtList sch)
 	{
 		for (int i = 0; i < sch.size(); i++)
 		{
 			if ( sch.get(i) instanceof ExtList)
 				return false;
 		}
 		return true;
 	}
 	
 	public static ExtList getDataFromDBPedia(String sparqlWhereQuery, String[] varNames){
 	    BufferedReader br = null;
 	    String everything = "";
 		try {
 			br = new BufferedReader(new FileReader("dbpedia.config"));
 		} catch (FileNotFoundException e1) {
 			System.err.println("*** DBPedia config file not found ***");
 			e1.printStackTrace();
 			throw new IllegalStateException();
 		}
 	    try {
 	        StringBuilder sb = new StringBuilder();
 	        String line = br.readLine();
 
 	        while (line != null) {
 	            sb.append(line);
 	            sb.append("\n");
 	            line = br.readLine();
 	        }
 	        everything = sb.toString();
 	    } catch (IOException e) {
 			System.err.println("*** Error while reading the Dbpedia config file ***");
 			e.printStackTrace();
 		} finally {
 	        try {
 				br.close();
 			} catch (IOException e) {
 				System.err.println("*** Error while closig the dbpedia config file ***");
 				e.printStackTrace();
 			}
 	    }
 		try {
 			Document doc;
 			ExtList data = new ExtList();
 				String query = everything + "\nSELECT ";
				for(int i = (varNames.length-1); i >= 0 ; i--){
 					query+= "?" + varNames[i] + " ";
 				}
 				query+=" WHERE "+sparqlWhereQuery+"";
 				doc = Jsoup.connect("http://dbpedia.org/sparql?")
 						.data("default-graph-uri", "http://dbpedia.org")
 						.data("query", query)
 						.data("format", "text/html")
 						.data("debug", "on")
 						.timeout(0)
 						.get();
 			Elements tdInfos = doc.getElementsByTag("td");
 			int columnCount = 0;
 			int rowCount = -1;
 			for(Element info : tdInfos){
 				String infoText = info.html();
 				columnCount %= varNames.length;
 				if(columnCount == 0){
 					ExtList e = new ExtList();
 					e.add(new ExtList(infoText));
 					data.add(e);
 					columnCount+=1;
 					rowCount +=1;
 				}else{
 					((ExtList) data.get(rowCount)).add(new ExtList(infoText));
 					columnCount+=1;
 				}
 				
 				
 			}
 			return data;
 		} catch (IOException e) {
 			System.err.println("*** Error while querying dbpedia, please check your internet connection and your query syntax ***");
 			throw new IllegalStateException();
 		}
 	}
 }
