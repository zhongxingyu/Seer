 package supersql.codegenerator.Mobile_HTML5;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 
 import supersql.codegenerator.DecorateList;
 import supersql.codegenerator.FuncArg;
 import supersql.codegenerator.ITFE;
 import supersql.codegenerator.TFE;
 import supersql.common.DB;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 public class Mobile_HTML5 {
 	
 	public static boolean preProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		//Pre-process (前処理)
 		showProcess(decos, html_env);	//TODO この位置でOKか確認
 		
 		if(symbol.contains("G1") || symbol.contains("G2")){
 			dynamicPreProcess0(symbol, decos, html_env);
 		}
 		if(!symbol.contains("G1") && !symbol.contains("G2")){
 			formPreProcess(symbol, decos, html_env);
 		}
 		return true;
 	}
 	public static boolean beforeWhileProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(!symbol.contains("G1") && !symbol.contains("G2")){
 			dynamicPreProcess(symbol, decos, html_env);//最終的には不要
 			
 //			formPreProcess(symbol, decos, html_env);
 		}
 		return true;
 	}
 	public static boolean whileProcess1(String symbol, DecorateList decos, Mobile_HTML5Env html_env, ExtList data, ExtList data_info, ITFE tfe, ExtList<TFE> tfes, int tfeItems){
 		//while process 1 (while内の処理1)
 		//Attribute: decos, html_env, data_info
 		//C1, C2:    decos, html_env, data, data_info, tfe, tfes, tfeItems
 		//G1, G2:    decos, html_env, data, data_info, tfe
 		if(symbol.contains("G1") || symbol.contains("G2")){
 			dynamicPreProcess(symbol, decos, html_env);
 		}
 		return true;
 	}
 	public static boolean whileProcess2(String symbol, DecorateList decos, Mobile_HTML5Env html_env, ExtList data, ExtList data_info, ITFE tfe, ExtList<TFE> tfes, int tfeItems){
 		//while process 2 (while内の処理2)
 		//Attribute: decos, html_env, data_info
 		//C1, C2:    decos, html_env, data, data_info, tfe, tfes, tfeItems
 		//G1, G2:    decos, html_env, data, data_info, tfe
 		if(symbol.contains("G1") || symbol.contains("G2")){
 			dynamicStringGetProcess(symbol, decos, html_env);
 		}
 		return true;
 	}
 	public static boolean afterWhileProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(!symbol.contains("G1") && !symbol.contains("G2")){
 			dynamicStringGetProcess(symbol, decos, html_env);//最終的には不要
 			dynamicProcess(symbol, decos, html_env);//最終的には不要
 			
 //			formStringGetProcess(symbol, decos, html_env);
 //			formProcess(symbol, decos, html_env);
 		}
 		return true;
 	}
 	public static boolean postProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(symbol.contains("G1") || symbol.contains("G2")){
 			dynamicProcess(symbol, decos, html_env);
 		}
 		if(!symbol.contains("G1") && !symbol.contains("G2")){
 			formStringGetProcess(symbol, decos, html_env);
 			formProcess(symbol, decos, html_env);
 		}
 		
 		//Post-process (後処理)
 		showCloseProcess(decos, html_env);
 		Mobile_HTML5Env.divWidth = "";
 		dynamicString = "";
 		formString = "";
 		return true;
 	}
 	
 	
 	//20131127 form
 	static String formString = "";
 //	static String formHTMLbuf0 = "";
 	static String formHTMLbuf = "";
 	static int formCount = 1;
 	static int formWordCount = 1;
 //	static String formFuncCountLabel = "___DynamicFunc_CountLabel___";
 	public static boolean form = false;
 	static ArrayList<String> formColumn = new ArrayList<>();
 	static ArrayList<String> formColumn0 = new ArrayList<>();
 	static ArrayList<String> formColumnAlias = new ArrayList<>();
 	static ArrayList<String> formColumnTable = new ArrayList<>();
 //	public static String formFuncArgProcess(ITFE tfe, DecorateList decos){
 //		//For Function
 //		return createFormAttribute(tfe, decos);
 //	}
 	public static String formAttributeProcess(ITFE tfe, DecorateList decos){
 		//For Attribute (C1, C2, G1, G2)
 		return createFormAttribute(tfe, decos);
 	}
 	private static String createFormAttribute(ITFE tfe, DecorateList decos){
 		String s = ""+tfe;
 		s = s.trim();
 		//Log.e("decos = "+decos);
 		if(s.startsWith("\"") && s.endsWith("\"")){
 			//not attribute
 			s = s.substring(1,s.length()-1);
 		}else{
 			//attribute
 //			s = "'||"+s+"||'";
 			String buf = "";
 			s = s.toLowerCase();
 			if(s.contains(".")){
 				formColumnAlias.add(s.substring(0,s.indexOf(".")));
 				buf = s.substring(s.indexOf(".")+1);
 				formColumn.add(buf);
 			}else{
 				formColumnAlias.add("");
 				buf = s;
 				formColumn.add(buf);
 			}
 			formColumnTable.add("");
 			
 			//E-mail:email=$session(email)@{noupdate},
 //	          備考:notes@{textarea},
 //	          attend={出席|欠席}", 
 //			"attendance WHERE id=$session(id)"
 			if(decos.containsKey("label")){
 				buf = decos.getStr("label")+":"+buf;
 			}
 			if(decos.containsKey("value")){
 				buf += "="+decos.getStr("value");
 			}
 			if(decos.containsKey("hidden")){
 				buf += "@{hidden}";
 			}else if(decos.containsKey("noupdate")){
 				//TODO
 				buf += "@{noupdate}";
 			}
 			formColumn0.add(buf);
 			//Log.e("buf="+buf);
 			
 			s = "    <input type=\"text\" name=\"form"+formCount+"_words"+(formWordCount++)+"\" placeholder=\"\">";	//TODO ここ以外の位置で
 		}
 		return s;
 	}
 	private static boolean formPreProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("form")){
 			formHTMLbuf = html_env.code.toString();
 			form = true;
 			return true;
 		}
 		return false;
 	}
 	private static boolean formStringGetProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("form")){
 			String currentHTML = html_env.code.toString();
 			formString = currentHTML.substring(formHTMLbuf.length(), currentHTML.length());
 			html_env.code = new StringBuffer(formHTMLbuf);
 			return true;
 		}
 		return false;
 	}
 	private static boolean formProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("form")){
 			
 			
 			//Log.e(" - Start form process -");
 			//Log.e(formString);
 			
 			//formColumnに格納されている列名がどのTableのものか判定
 			checkFormColumnSTableName();
 			
 			
 			boolean update = false;
 			boolean insert_update = true;
 			
 			
 			
 //	    	String title = "";
 	    	String columns = "";
 	    	String after_from = "";
 	    	String insertFlag = "";
 	    	for(String s:formColumn0){
 	    		columns += s+",";
 	    		Log.i(s);
 	    	}
 	    	columns = columns.substring(0,columns.length()-1);
 	    	after_from = formColumnTable.get(0);	//TODO 複数テーブルへのinsert
 
 	    	//columns = "user_id = $session(user_id)@{hidden},text";
 	    	
 	    	try{
 	    		//title（第一引数）
 //	    		FuncArg fa1 = (FuncArg) this.Args.get(0);
 //	    		if(!fa1.getStr().equals(""))	title = fa1.getStr();
 //	    		else{
 //	    			if(update || insert_update)	title = "Update Form";
 //	    			else						title = "Insert";
 //	    		}
 //	    		//columns（第二引数）
 //	    		FuncArg fa2 = (FuncArg) this.Args.get(1);
 //	    		columns += fa2.getStr();
 //	    		//after_from（第三引数）
 //	    		FuncArg fa3 = (FuncArg) this.Args.get(2);
 //	    		after_from += fa3.getStr().trim();
 //	    		if(update){
 //		    		//（第四引数）
 //		    		FuncArg fa4 = (FuncArg) this.Args.get(3);
 //		    		insertFlag += fa4.getStr().toLowerCase().trim();
 //		    		if(insertFlag.equals(""))	insertFlag="false";
 //	    		}
 	    	}catch(Exception e){
 	    		Log.info("<Warning> insert関数の引数が不足しています。 ex. insert(\"title\", \"c1:column1, c2:column2, ... \", \"From以下\")");
 	    		return false;
 	    	}
 			if(columns.trim().equals("") || after_from.equals("")){
 				Log.info("<Warning> insert関数の引数が不足しています。 ex. insert(\"title\", \"c1:column1, c2:column2, ... \", \"From以下\")");
 	    		return false;
 			}
 			if(after_from.toLowerCase().startsWith("from "))	after_from = after_from.substring("from".length()).trim();
 			if(insert_update)	insertFlag = "true";	//20130721
 			//Log.info(title);
 	    	
 	    	
 	    	//置換 ( @ { , }  ->  @ { ; } )
 			//Log.i("Before: "+columns);
 	    	int inAtFlg = 0;
 	    	for(int i=0; i<columns.length();i++){
 	    		//Log.i(columns.charAt(i));
 	    		if(inAtFlg==0){
 	    			if(columns.charAt(i)=='@')		inAtFlg=1;
 	    		}else if(inAtFlg==1){
 		    		if(columns.charAt(i)==' ')		inAtFlg=1;
 		    		else if(columns.charAt(i)=='{')	inAtFlg=2;
 	    		}else if(inAtFlg==2){
 	    			if(columns.charAt(i)==',')
 	    				columns = columns.substring(0,i)+";"+columns.substring(i+1);	//置換
 	    			else if(columns.charAt(i)=='}')	inAtFlg=0;
 	    		}
 	    	}
 	    	//Log.i("After:  "+columns);
 	    	
 	    	
 	    	int col_num=1;
 	    	String columns0 = columns;
 	    	while(columns0.contains(",")){
 	    		columns0 = columns0.substring(columns0.indexOf(",")+1);
 	    		col_num++;		//カウント
 	    	}
 	    	String[] s_name_array = new String[col_num];
 	    	String[] s_array = new String[col_num];
 	    	columns0 = columns;
 	    	for(int i=0; i<col_num-1; i++){
 	    		s_array[i] = columns0.substring(0,columns0.indexOf(","));
 	    		columns0 = columns0.substring(columns0.indexOf(",")+1);
 	    		//Log.i( "s_array["+i+"] = "+s_array[i]+"	"+columns0);
 	    	}
 	    	s_array[col_num-1] = columns0;
 			//Log.i( "s_array["+(col_num-1)+"] = "+s_array[col_num-1]);
 	    	int j=0;
 			for(int i=0; i<col_num; i++){
 				//Log.i( "s_array["+i+"] = "+s_array[i]);
 				if(s_array[i].contains(":")){
 					if(!s_array[i].substring(0,s_array[i].indexOf(":")).contains(")"))
 							s_name_array[j++] = s_array[i].substring(0,s_array[i].indexOf(":")).trim();
 					s_array[i] = s_array[i].substring(s_array[i].indexOf(":")+1);
 				}else{
 					s_name_array[j++] = "";
 					//if(!s_array[i].contains(")"))	s_name_array[j++] = s_array[i];	  <- ??
 				}
 				//Log.i("s_name_array["+(j-1)+"] = "+s_name_array[j-1] + "	s_array["+i+"] = "+s_array[i]);
 			}
 			boolean groupbyFlg = false;	//Flg
 			//boolean[] aFlg = new boolean[col_num];	//Flg
 			//boolean[] popFlg = new boolean[col_num];	//Flg
 			String a = "";
 	    	String insert_col = "";
 	    	String update_col_array = "'";
 	    	String update_where = "";
 	    	boolean[] textareaFlg = new boolean[col_num];
 	    	boolean[] hiddenFlg = new boolean[col_num];
 	    	boolean[] noinsertFlg = new boolean[col_num];
 	    	String[] validationType = new String[col_num];
 	    	String notnullFlg_array = "";
 	    	String[] $session_array = new String[col_num];
 	    	String[] $time_array = new String[col_num];
 	    	String[] $gps_array = new String[col_num];
 	    	String[] button_array = new String[col_num];
 	    	String buttonSubmit = "";
 	    	String insert_aFlg = "\"";	//Flg
 	    	String insert_popFlg = "\"";	//Flg
 	    	int noinsert_count = 0;
 	    	int a_pop_count = 0;
 	    	for(int i=0; i<col_num; i++){
 	    		a = s_array[i].replaceAll(" ","");
 	    		//Log.i(a);
 	    		
 	    		//$session()あり
 	    		if(a.contains("=")){
 	    			String a_right = a.substring(a.indexOf("=")+1).trim();
 	    			if(a_right.startsWith("$session(")){
 	    				$session_array[i] = a.substring(a.indexOf("$session(")+"$session(".length(),a.indexOf(")"));
 	    				$time_array[i] = "";
 	    				$gps_array[i] = "";
 	    				button_array[i] = "";
 	    				a = a.substring(0,a.indexOf("=")).trim() + a.substring(a.indexOf(")")+1).trim();
 	        			s_array[i] = s_array[i].substring(0,s_array[i].indexOf("=")).trim() + s_array[i].substring(s_array[i].indexOf(")")+1).trim();
 	    			}else if(a_right.startsWith("time(") || a_right.startsWith("date(")){
 	    				String d = s_array[i].substring(s_array[i].indexOf("(")+1,s_array[i].lastIndexOf(")")).trim(); 
 //	    				$time_array[i] = "date(\"Y-m-d H:i:s\")";	//"date(\"Y/m/d(D) H:i:s\")";
 	    				$time_array[i] = "date(\""+( (d.equals(""))? ("Y-m-d H:i:s") : (d) )+"\")";	//"date(\"Y/m/d(D) H:i:s\")";
 	    				$session_array[i] = "";
 	    				$gps_array[i] = "";
 	    				button_array[i] = "";
 	    				a = a.substring(0,a.indexOf("=")).trim() + a.substring(a.indexOf(")")+1).trim();
 	    				s_array[i] = s_array[i].substring(0,s_array[i].indexOf("=")).trim() + s_array[i].substring(s_array[i].indexOf(")")+1).trim();
 	    			}else if(a_right.startsWith("gps_info(")){
 	    				//gps_info()の取得
 	    				//String d = s_array[i].substring(s_array[i].indexOf("(")+1,s_array[i].lastIndexOf(")")).trim(); 
 	    				//$gps_array[i] = "date(\""+( (d.equals(""))? ("Y-m-d H:i:s") : (d) )+"\")";	//"date(\"Y/m/d(D) H:i:s\")";
 	    				$gps_array[i] = "gps_info";
 	    				
 	    				$session_array[i] = "";
 	    				$time_array[i] = "";
 	    				button_array[i] = "";
 	    				a = a.substring(0,a.indexOf("=")).trim() + a.substring(a.indexOf(")")+1).trim();
 	    				s_array[i] = s_array[i].substring(0,s_array[i].indexOf("=")).trim() + s_array[i].substring(s_array[i].indexOf(")")+1).trim();
 	    			}else if(a.contains("{")){
 	    				String ss = a.substring(a.indexOf("{")+"{".length(),a.indexOf("}"));
 	    				button_array[i] = ss;
 	    				$session_array[i] = "";
 	    				$time_array[i] = "";
 	    				$gps_array[i] = "";
 	    				a = a.substring(0,a.indexOf("=")).trim() + a.substring(a.indexOf("}")+1).trim();
 	        			s_array[i] = s_array[i].substring(0,s_array[i].indexOf("=")).trim() + s_array[i].substring(s_array[i].indexOf("}")+1).trim();
 	    			}else{
 	    				$session_array[i] = "";
 	    				$time_array[i] = "";
 	    				$gps_array[i] = "";
 	    				button_array[i] = "";
 	    			}
 	    		}else{
 	    			$session_array[i] = "";
 	    			$time_array[i] = "";
 	    			$gps_array[i] = "";
 	    			button_array[i] = "";
 	    		}
 	    		//Log.i(s_array[i]+"	"+$session_array[i]);
 	    		//Log.i(button_array[i]+"	"+button_array[i]);
 	    		
 	    		if(a.startsWith("max(") || a.startsWith("min(") || a.startsWith("avg(") ||  a.startsWith("count(") )	groupbyFlg = true;
 	    		if(a.startsWith("a(") || a.startsWith("anchor(")){
 	    			insert_aFlg += "true\""+((i<col_num-1)?(",\""):(""));
 	    			if(a.endsWith(")")){
 //	    				insert_col += s_array[i] +((i<col_num-1)?(","):(""));
 //	    	    		insert_col_array += s_array[i] +"\""+((i<col_num-1)?(",\""):(""));
 //	    	    		insert_aFlg += "false\""+((i<col_num-1)?(",\""):(""));
 //	    	    		insert_popFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    	    		insert_col += s_array[i]+",";
 //	    				insert_col_array += s_array[i]+"\",\"";
 	    				insert_aFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				insert_popFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    			}else	a_pop_count++;
 	    		}else
 	    			insert_aFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    		if(a.startsWith("pop(") || a.startsWith("popup(")){
 	    			insert_popFlg += "true\""+((i<col_num-1)?(",\""):(""));
 	    			if(a.endsWith(")")){
 //	    				insert_col += s_array[i] +((i<col_num-1)?(","):(""));
 //	    	    		insert_col_array += s_array[i] +"\""+((i<col_num-1)?(",\""):(""));
 //	    	    		insert_aFlg += "false\""+((i<col_num-1)?(",\""):(""));
 //	    	    		insert_popFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    				insert_col += s_array[i]+",";
 //	    				insert_col_array += s_array[i]+"\",\"";
 	    				insert_aFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				insert_popFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    			}else	a_pop_count++;
 	    		}else
 	    			insert_popFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    		
 	    		//Log.i(s_array[i]);
 	    		//Check: @textarea, @hidden, @noinsert, @notnull, @date, @date1-5, @time	//TODO:リファクタリング
 	    		textareaFlg[i] = false;
 	    		hiddenFlg[i] = false;
 	    		noinsertFlg[i] = false;
 	    		validationType[i] = "";
 	    		String str = "";
 	    		if(s_array[i].replaceAll(" ","").contains("@{")){
 	    			str = s_array[i].substring(s_array[i].lastIndexOf("@")+1);	//@以下の文字列
 		    		if(str.contains("textarea"))
 		    			textareaFlg[i] = true;
 		    		if(str.contains("hidden"))
 		    			hiddenFlg[i] = true;
 		    		if(str.contains("noinsert") || str.contains("noupdate")){
 		    			noinsertFlg[i] = true;
 		    			noinsert_count++;
 		    		}else{
 			    		if(str.contains("notnull")){
 			    			if(i==(col_num-1))	notnullFlg_array += "TRUE";
 			    			else				notnullFlg_array += "TRUE,";
 			    		}else{
 			    			if(i==(col_num-1))	notnullFlg_array += "FALSE";
 			    			else				notnullFlg_array += "FALSE,";
 			    		}
 		    		}
 		    		validationType[i] = Mobile_HTML5.checkFormValidationType(str);	//form validation
 		    		
 		    		s_array[i] = s_array[i].substring(0,s_array[i].indexOf("@"));
 		    		//Log.i(s_array[i]);
 	    		}else{
 	    			if(i==(col_num-1))	notnullFlg_array += "FALSE";
 	    			else				notnullFlg_array += "FALSE,";
 	    		}
 	    		
 	    		if(!noinsertFlg[i]){
 	    			insert_col += s_array[i] +((i<col_num-1)?(","):(""));
 	    			if(update)	update_col_array += s_array[i] +"'"+((i<col_num-1)?(",'"):(""));
 	    		}
 	    	}
 	    	col_num -= a_pop_count;
 	    	insert_col = insert_col.replaceAll("a\\(","").replaceAll("anchor\\(","").replaceAll("pop\\(","").replaceAll("popup\\(","").replaceAll("\\)","");
 //	    	insert_col_array = insert_col_array.replaceAll("a\\(","").replaceAll("anchor\\(","").replaceAll("pop\\(","").replaceAll("popup\\(","").replaceAll("\\)","");
 	    	
 	    	
 	    	//Log.i("	1:"+title+"	2:"+columns+"	col_num:"+col_num);
 	    	//Log.i("	insert_col:"+insert_col+"	update_col_array:"+update_col_array);
 	    	//Log.i("	insert_aFlg:"+insert_aFlg+"	insert_popFlg:"+insert_popFlg);
 	    	//Log.i("	notnullFlg_array: "+notnullFlg_array);
 	    	
 	    	
 	    	String DBMS = GlobalEnv.getdbms();										//DBMS
 	    	String DB = GlobalEnv.getdbname();										//DB
 	    	
 	    	String query = "";
 	    	//Log.i(after_from_string);
 //	    	if(after_from.startsWith("#")){					//From以下をクエリの下(#*)から取ってくる場合
 //	    		if(!after_from_string.contains(after_from)){
 //	    			Log.info("<Warning> insert関数の第三引数に指定されている '"+after_from+"' が見つかりません。");
 //	    			return;
 //	    		}
 //	    		query = after_from_string
 //	    				.substring(after_from_string.indexOf(after_from)+after_from.length())
 //	    				.trim().toLowerCase();
 //	    		if(query.contains("#"))	query = query.substring(0,query.indexOf("#")).trim().toLowerCase();
 //	    	}else
 	    		query = after_from.toLowerCase();			//From以下を第三引数へ書く場合
 	    	//Log.i("\n	Query: "+query);
 	    	String from = "";
 	    	from = query.toLowerCase().trim();
 	    	if(update){
 	    		update_where = from.substring(from.indexOf(" where ")).trim();
 	    		if(update_where.contains("$session"))
 	    			update_where = update_where.replaceAll("\\$session","'\".\\$_SESSION").replaceAll("\\(","[").replaceAll("\\)","].\"'");
 	    		from = from.substring(0,from.indexOf(" where ")).trim();
 	    	}
 	    	//Log.i("	FROM:"+from+"	update_where:"+update_where);
 	    	//Log.i("	FROM: "+from+"\n	WHERE: "+where+"\n	GROUP: "+groupby+"\n	HAVING: "+having);
 	    	//Log.i("	ORDER: "+orderby+"\n	LIMIT: "+limit+"\n	Query: "+query);
 	    	
 	    	
 
 	    	String statement = "";
 	    	String gps_js = "";
 	    	//sqlite3 php
 	    	if(DBMS.equals("sqlite3")){
 	    		statement += 
 	    				"<!-- Form start -->\n" +
 	    				"<!-- Form Panel start -->\n" +
 	    				"<br>\n" +
 	    				//"<div id=\"FORM"+insertCount+"panel\" style=\"background-color:whitesmoke; width:99%; border:0.1px gray solid;\" data-role=\"none\">\n" +
 	    				//"<div style=\"padding:3px 5px;border-color:hotpink;border-width:0 0 1px 7px;border-style:solid;background:#F8F8F8; font-size:30;\" id=\"FormTitle"+formCount+"\">"+title+"</div>\n" +
 	    				"<div id=\"FORM"+formCount+"panel\" style=\"\" data-role=\"none\">\n" +
 //	    				"<hr>\n<div style=\"font-size:30;\" id=\"FormTitle"+formCount+"\">"+title+"</div>\n<hr>\n" +
 //	    				"<br>\n" +
 	    				"<form method=\"post\" action=\"\" target=\"dummy_ifr\">\n";
 	    				//"<form method=\"post\" action=\"\" target=\"form"+formCount+"_ifr\">\n";
 	
 	    		//TODO 下の部分の処理が必要
 //				statement += formString;
 				
 	    		int insertWordCount = 0;
 	    		for(int i=0; i<col_num; i++){
 //	    			if(!textareaFlg[i]){
 					if($session_array[i].equals("") && $time_array[i].equals("") && $gps_array[i].equals("")){
 						if(!button_array[i].equals("")){
 							//Log.i("bt_array:"+button_array[i]);
 							String ss = button_array[i]+"|";
 							int btRcount = ss.length() - ss.replaceAll("\\|","").length();
 							//Log.i("btRcount:"+btRcount);
 							
 							if(btRcount == 1){				//テキスト ex){2013秋}
 
 								//statement +=
 								//		"    <input type="text" disabled="disabled" value="お名前: 五嶋">";
 								statement += 
 										"    <"+((!textareaFlg[i])?("input"):("textarea"))+" type=\""+((!hiddenFlg[i])?("text"):("hidden"))+"\" disabled=\"disabled\" value=\""+( (!s_name_array[i].equals(""))? (s_name_array[i]+": "):("") )+"" +
 										""+( (!textareaFlg[i])? ("\n") : ((!s_name_array[i].equals(""))? ("\">"+s_name_array[i]+": "):("")) )+button_array[i]+"" +
 										""+((!textareaFlg[i])?("\">"):("</textarea>"))+"\n";
 								if(!noinsertFlg[i])
 									statement += 
 											"    <input type=\"hidden\" name=\"form"+formCount+"_words"+(++insertWordCount)+"\" value=\""+button_array[i]+"\">\n";
 							
 							
 							}else if(btRcount == 2){		//ボタン ex){出席|欠席}
 								String bt1=ss.substring(0,ss.indexOf("|")).trim();
 								String bt2=ss.substring(ss.indexOf("|")+1,ss.length()-1).trim();
 								insertWordCount++;
 								statement += 
 										"	<div class=\"ui-grid-a\">\n" +
 										"		<div class=\"ui-block-a\">\n" +
 										"    		<input type=\"submit\" name=\"form"+formCount+"_words"+(insertWordCount)+"\" value=\""+bt1+"\" data-theme=\"a\">\n" +
 										"		</div>\n" +
 										"		<div class=\"ui-block-b\">\n" +
 										"    		<input type=\"submit\" name=\"form"+formCount+"_words"+(insertWordCount)+"\" value=\""+bt2+"\" data-theme=\"a\">\n" +
 										"		</div>\n" +
 										"	</div>\n";
 								buttonSubmit += " || $_POST['form"+formCount+"_words"+(insertWordCount)+"']";
 							}else{							//ラジオボタン ex){出席|欠席|その他}
 								statement += "   <div data-role=\"controlgroup\">\n";
 								insertWordCount++;
 								for(int k=1; k<=btRcount; k++){
 									String val = ss.substring(0,ss.indexOf("|")).trim();
 									statement += 
 											"		<input type=\"radio\" name=\"form"+formCount+"_words"+(insertWordCount)+"\" id=\"form"+formCount+"_words"+(insertWordCount)+"_"+k+"\" value=\""+val+"\""+( (k>1)? (""):(" checked=\"checked\"") )+">\n" +
 											"		<label for=\"form"+formCount+"_words"+(insertWordCount)+"_"+k+"\">"+val+"</label>\n";
 									ss = ss.substring(ss.indexOf("|")+1);
 								}
 								statement += "	</div>\n";
 							}
 						}else{
 							if(validationType[i].isEmpty()){
 								statement += 
 										"    <"+((!textareaFlg[i])?("input"):("textarea"))+" type=\""+((!hiddenFlg[i])?("text"):("hidden"))+"\" name=\"form"+formCount+"_words"+(++insertWordCount)+"\" placeholder=\""+s_name_array[i]+"\">" +
 										""+((!textareaFlg[i])?(""):("</textarea>"))+"\n";
 							}else{
 								statement += Mobile_HTML5.getFormValidationString(validationType[i], "form"+formCount+"_words"+(++insertWordCount), s_name_array[i]);
 							}
 						}
 					}else{
 						//statement += "    <input type=\"text\" name=\"form"+formCount+"_words"+(++insertWordCount)+"\" placeholder=\""+s_name_array[i]+"\">\n";
 						String echo = "";
 						if(!$session_array[i].equals(""))	echo += "	echo $_SESSION["+$session_array[i]+"];\n";
 						else if(!$time_array[i].equals(""))	echo += "	echo "+$time_array[i]+";\n";
 						//else if(!$gps_array[i].equals(""))	echo += "	echo \"<script> getGPSinfo(); </script>\";\n";
 						//else if(!$gps_array[i].equals(""))	echo += "	echo\"<script> getGPSinfo(); </script>\";\n";
 						else if(!$gps_array[i].equals("")){
 							echo += "	echo \"位置情報(緯度・経度)\";\n";
 							gps_js +=
 									"\n<!-- getGPSinfo() -->\n" +
 									"<script src=\"http://maps.google.com/maps/api/js?sensor=false&libraries=geometry\"></script>\n" +
 									"<script type=\"text/javascript\">\n" +
 									"<!--\n" +
 									"$(document).on(\"pageinit\", \"#p-top1\", function(e) {\n" +
 									"  	// Geolocation APIのオプション設定\n" +
 									"  	var geolocationOptions = {\n" +
 									"    	\"enableHighAccuracy\" : true, // 高精度位置情報の取得\n" +
 									"    	\"maximumAge\" : 0, // キャッシュの無効化\n" +
 									"    	\"timeout\" : 30000 // タイムアウトは30秒\n" +
 									"  	};\n" +
 									"    navigator.geolocation.getCurrentPosition(function(pos) {\n" +
 									"      	// 経度、緯度を取得 //\n" +
 									"		document.getElementsByName('form"+formCount+"_words"+(insertWordCount+1)+"')[0].value=pos.coords.latitude+\",\"+pos.coords.longitude;\n" +
 									"    }, function(e) {\n" +
 									"		gpsInfo = \"\";\n" +
 									"    }, geolocationOptions);\n" +
 									"});\n" +
 									"// -->\n" +
 									"</script>\n";
 						}
 						
 						statement += 
 								"    <"+((!textareaFlg[i])?("input"):("textarea"))+" type=\""+((!hiddenFlg[i])?("text"):("hidden"))+"\" disabled=\"disabled\" value=\""+( (!s_name_array[i].equals(""))? (s_name_array[i]+": "):("") )+"" +
 								""+( (!textareaFlg[i])? ("\n") : ((!s_name_array[i].equals(""))? ("\">"+s_name_array[i]+": "):("")) )+"\n";
 						statement += 
 								"EOF;\n" +
 								echo +
 								"		echo <<<EOF\n";
 							
 						statement += 
 								""+((!textareaFlg[i])?("\">"):("</textarea>"))+"\n";
 						if(!noinsertFlg[i])
 							statement += 
 									"    <input type=\"hidden\" name=\"form"+formCount+"_words"+(++insertWordCount)+"\" value=\"\n" +
 									"EOF;\n" +
 									echo +
 									"		echo <<<EOF\n" +
 									"\">\n";
 					}
 	    		}
 	    		
 	    		if(buttonSubmit.equals(""))
 	    			statement += 
 		    			"    <input type=\"submit\" value=\"OK&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\" name=\"form"+formCount+"\" id=\"form"+formCount+"\" data-mini=\"false\" data-inline=\"false\">\n";
 //	    				"    <input type=\"submit\" value=\""+( (!update)? ("登録"):("更新") )+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\" name=\"form"+formCount+"\" id=\"form"+formCount+"\" data-icon=\"insert\" data-mini=\"false\" data-inline=\"false\">\n";
 				
 	    		statement += 
 	    				"</form>\n" +
 //	    				"\n" +
 //	    				"<div id=\"Form"+formCount+"_text0\" data-role=\"none\"><!-- Form result --></div>\n" +
 //	    				"\n" +
 //	    				"<br>\n" +
 	    				"</div>\n";
 	    		//getGPSinfo()
 	    		statement += gps_js;
 	    		statement += 
 	    				"<!-- Form Panel end -->\n" +
 	    				"\n";
 				statement += 
 	    				"\n" +
 	    				"<script type=\"text/javascript\">\n" +
 	    				"function Form"+formCount+"_echo1(str){\n" +
 	    				"  jQuery(function ($) {\n" +
 	    				"  	$('input,textarea').not('input[type=\\\"radio\\\"],input[type=\\\"checkbox\\\"],:hidden, :button, :submit,:reset').val('');\n" +
 	    				"	$('input[type=\"radio\"], input[type=\\\"checkbox\\\"],select').removeAttr('checked').removeAttr('selected');\n" +
 	    				"  });\n" +
 	    				"  var textArea = document.getElementById(\"Form"+formCount+"_text0\");\n" +
 	    				"  textArea.innerHTML = str;\n" +
 	    				"}\n" +
 	    				"</script>\n" +
 	    				"<!-- Form end -->\n";
 				
 				
 				Mobile_HTML5Env.PHP +=
 	    				"<?php\n" +
 	    				"if($_POST['form"+formCount+"'] "+buttonSubmit+"){\n" +
 	    				//"if($_POST['form"+formCount+"'] || $_POST['form"+formCount+"_words"+formCount+"']){\n" +
 	    				"    //ユーザ定義\n" +
 	    				"    $sqlite3_DB = '"+DB+"';\n" +
 	    				"    $insert_col = \""+insert_col+"\";\n";
 				if(update){
 					Mobile_HTML5Env.PHP +=
 							"    $update_col_array = array("+update_col_array+");\n" +
 							"    $update_where = \""+update_where+"\";\n";
 				}
 				Mobile_HTML5Env.PHP +=
 	    				"    $notnullFlg = array("+notnullFlg_array+");\n" +
 	    				"    $col_num = "+(col_num - noinsert_count)+";                          //カラム数(Java側で指定)\n" +
 	    				"    $table = '"+from+"';\n" +
 	    				"\n" +
 	    				"	$insert_str = \"notnull\";\n" +
 	    				"	for($k=1; $k<=$col_num; $k++){\n" +
 	    				"    	$var[$k] = checkHTMLsc($_POST['form"+formCount+"_words'.$k]);\n" +
 	    				"    	$var[$k] = str_replace(array(\"\\r\\n\",\"\\r\",\"\\n\"), '<br>', $var[$k]);	//改行コードを<br>へ\n" +
 	    				//"    	//$var[$k] = mb_convert_encoding($var[$k], 'UTF-8', 'auto');					//エンコードをUTF-8へ PHP環境によってはうまく動かない？\n" +
 	    				//"    	$insert_str .= trim($var[$k]);\n" +
 	    				"    	if($notnullFlg[$k-1]){\n" +
 	    				"    		if(trim($var[$k]) == \"\")	$insert_str = \"\";\n" +
 	    				"    	}\n";
 				for(int i=0; i<col_num; i++){
 					if(!$time_array[i].equals(""))
 						Mobile_HTML5Env.PHP += "		if($k=="+i+")	$var[$k] = "+$time_array[i]+";\n";	//現在時刻
 				}
 				Mobile_HTML5Env.PHP +=	
 	    				"    }\n" +
 	    				"\n" +
 	    				"	if($insert_str == \"\"){\n" +
 	    				"        form"+formCount+"_p1('<font color=\\\"red\\\">Please check the value.</font>');\n" +
 	    				"	}else{\n";
 				
 				if(!update){
 					//insert()
 					Mobile_HTML5Env.PHP +=
 		    				"		$insert_str = \"\";\n" +
 		    				"		for($k=1; $k<=$col_num; $k++){\n" +
 		    				"			if($k==1)	$insert_str .= \"'\".$var[$k].\"'\";\n" +
 		    				"			else		$insert_str .= \",'\".$var[$k].\"'\";\n" +
 		    				"		}\n" +
 		    				"		//DBへ登録\n" +
 		    				"		$insert_db"+formCount+" = new SQLite3($sqlite3_DB);\n" +
 		    				"        $insert_sql = \"INSERT INTO \".$table.\" (\".$insert_col.\") VALUES (\".$insert_str.\")\";\n" +
 		    				"        \n" +
 		    				"        try{\n" +
 		    				"			$result2 = $insert_db"+formCount+"->exec($insert_sql);\n" +
 		    				"			unset($insert_db"+formCount+");\n" +
 		    				"		 	form"+formCount+"_p1(\"Registration completed.\");\n" +
 		    				"		 	//form"+formCount+"_p1($insert_sql);\n" +
 		    				"        }catch(Exception $e){\n" +
 		    				"       		unset($insert_db"+formCount+");\n" +
 		    				"       		form"+formCount+"_p1('<font color=red>Registration failed.</font>');	//登録失敗\n" +
 		    				"        }\n";
 				}else{
 					//update()
 					Mobile_HTML5Env.PHP +=
 							"		$insert_db1 = new SQLite3($sqlite3_DB);\n" +
 							"		try{\n" +
 							"			//データが存在しているかチェック\n" +
 							"			$select_sql = \"SELECT \".$insert_col.\" FROM \".$table.\" \".$update_where;\n" +
 							"			$result2 = $insert_db1->query($select_sql);\n" +
 							"			$j = 0;\n" +
 							"			while($row = $result2->fetchArray()){\n" +
 							"			    $j++;\n" +
 							"			}\n" +
 							"			\n" +
 							"			if($j>0){\n" +
 							"				//更新(update)\n" +
 							"				$update_str = \"\";\n" +
 							"				for($k=1; $k<=$col_num; $k++){\n" +
 							"					if($k==1)	$update_str .= $update_col_array[$k-1].\"='\".$var[$k].\"'\";\n" +
 							"					else		$update_str .= \",\".$update_col_array[$k-1].\"='\".$var[$k].\"'\";\n" +
 							"				}\n" +
 							"				\n" +
 							"				$update_sql = \"UPDATE \".$table.\" SET \".$update_str.\" \".$update_where;\n" +
 							"				$result2 = $insert_db1->exec($update_sql);\n" +
 							"				//echo '変更された行の数: ', $db->changes();\n" +
 							"				form"+formCount+"_p1(\"Update completed.\");\n" +
 							"			}else{\n";
 					//Log.i("formFlag:"+formFlag);
 					if(!insertFlag.equals("true"))
 							Mobile_HTML5Env.PHP +=
 								"				form"+formCount+"_p1('<font color=red>No data found.</font>');	//更新データなし\n";
 					else
 							Mobile_HTML5Env.PHP +=
 								"				//新規登録(insert)\n" +
 								"				$insert_str = \"\";\n" +
 								"				for($k=1; $k<=$col_num; $k++){\n" +
 								"					if($k==1)	$insert_str .= \"'\".$var[$k].\"'\";\n" +
 								"					else		$insert_str .= \",'\".$var[$k].\"'\";\n" +
 								"				}\n" +
 								"				\n" +
 								"				$insert_sql = \"INSERT INTO \".$table.\" (\".$insert_col.\") VALUES (\".$insert_str.\")\";\n" +
 								"				$result2 = $insert_db1->exec($insert_sql);\n" +
 								"				form"+formCount+"_p1(\"Registration completed.\");\n";
 					Mobile_HTML5Env.PHP +=
 							"			}\n" +
 							"        }catch(Exception $e){\n" +
 							"       		unset($insert_db1);\n" +
 							"       		form"+formCount+"_p1('<font color=red>Update failed.</font>');	//更新失敗\n" +
 							"        }\n" +
 							"        unset($insert_db1);\n";
 				}
 	    				
 				Mobile_HTML5Env.PHP +=
 	    				"    }\n" +
 	    				"}\n" +
 	    				"function form"+formCount+"_p1($str){\n" +
 	    				"    echo '<script type=\"text/javascript\">window.parent.Form"+formCount+"_echo1(\"'.$str.'\");</script>';\n" +
 	    				"}\n" +
 	    				"?>\n";
 	    				
 
 	    	}
 	    	//else if(DBMS.equals("postgresql")){
 	    	//	;
 	    	//}
 
 	    	// 各引数毎に処理した結果をHTMLに書きこむ
 	    	html_env.code.append(statement);
 	    	
 
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			//Log.e(" - End form process -");
 
 			formCount++;
 			formWordCount = 1;
 			formColumn.clear();
 			form = false;
 		}
 		return true;
 	}
 	//formColumnに格納されている列名がどのTableのものか判定
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private static void checkFormColumnSTableName() {
 		//formColumnに格納されている列名がどのTableのものか判定
 		ArrayList<ArrayList> tables = DB.getTableNamesFromQuery(GlobalEnv.query);	//(0)=Table name, (1)=Table alias, (2)=From phrase
 		ArrayList<String> al1 = tables.get(0);	//Table name
 		ArrayList<String> al2 = tables.get(1);	//Table alias
 		Map<String,HashSet<String>> dbTablesMap = DB.getTableAndColumnNamesMap();
 		for(int i=0;i<formColumn.size();i++){
 			String fcAlias = formColumnAlias.get(i);
 			String fc = formColumn.get(i);
 			
 			if(fcAlias.isEmpty()){
 				//Aliasなし: From句に書かれている全テーブルの列名(上で取得)と照合してTable名を決定
 				for(String tableName : al1){
 					try{
 						for(String dbColumn: dbTablesMap.get(tableName)){
 				            if(fc.equals(dbColumn)){
 				            	formColumnTable.set(i,tableName);
 				            }
 						}
 					}catch (Exception e) { }
 				}
 			}else{
 				//Aliasあり: From句に書かれているテーブル名のAliasと照合してTable名を決定
 				for(int j=0;j<al1.size();j++){
 					String tableAlias = al2.get(j);
 					if(tableAlias.equals(fcAlias)){
 						formColumnTable.set(i,al1.get(j));
 					}
 				}
 			}
 			//Log.i(fcAlias+" "+fc+" "+formColumnTable.get(i));
 		}
 	}
 
 	//20131201 form validation
 	public static String checkFormValidationType(String s){
 		String type = "";
 		String types[] = {"date1","date2","date3","date4","date5","date","time"};	//Order is significant!
 		for(int i=0;i<types.length;i++){
 			if(s.contains(types[i])){	//TODO: リファクタリング
 				type = types[i];
 				break;
 			}
 		}
 		//Log.e("type = "+type);
 		return type;
 	}
 	public static String getFormValidationString(String type, String name, String placeholder){
 		String s = "";
 		type = type.toLowerCase().trim();
 		
 		//date, time
 //	    ◎date <input type="text" name="insert1_words4" placeholder="Year / Month / Day" data-role="datebox" data-options='{"mode":"calbox", "useNewStyle":true, "overrideCalHeaderFormat": "%Y / %m / %d", "overrideDateFormat": "%Y/%m/%d" }' >
 //	    ◎date1<input type="text" name="insert1_words4" placeholder="Year" data-role="datebox" data-options='{"mode":"flipbox", "useNewStyle":true, "overrideHeaderFormat": "%Y", "overrideDateFormat": "%Y", "overrideDateFieldOrder":["y"] }' >
 //	    ◎date2<input type="text" name="insert1_words4" placeholder="Month" data-role="datebox" data-options='{"mode":"flipbox", "useNewStyle":true, "overrideHeaderFormat": "%m", "overrideDateFormat": "%m", "overrideDateFieldOrder":["m"] }' >
 //	    ◎date3<input type="text" name="insert1_words4" placeholder="Day" data-role="datebox" min="2016-01-01" max="2016-01-31" data-options='{"mode":"flipbox", "useNewStyle":true, "overrideHeaderFormat": "%d", "overrideDateFormat": "%d", "overrideDateFieldOrder":["d"] }' >
 //	    ◎date4<input type="text" name="insert1_words4" placeholder="Year / Month" data-role="datebox" data-options='{"mode":"calbox", "useNewStyle":true, "overrideCalHeaderFormat": "%Y / %m", "overrideDateFormat": "%Y/%m" }'}' >
 //	    ◎date5<input type="text" name="insert1_words4" placeholder="Month / Day" data-role="datebox" min="2016-01-01" max="2016-12-31" data-options='{"mode":"datebox", "useNewStyle":true, "overrideHeaderFormat": "%m / %d",  "overrideDateFormat": "%m/%d", "overrideDateFieldOrder":["m","d"] }'}' >
 //	    X date5<input type="text" name="insert1_words4" placeholder="Month / Day" data-role="datebox" data-options='{"mode":"calbox", "useNewStyle":true, "overrideCalHeaderFormat": "%m / %d", "overrideDateFormat": "%m/%d" }'}' >
 //	    ◎time <input type="text" name="insert1_words5" placeholder="Ex) 12:01" data-role="datebox" data-options='{"mode":"timebox", "overrideTimeFormat":24, "useNewStyle":true}'>
 
 		switch (type){
 		  case "date":	//Year / Month / Day
 		    s += "    <input type=\"date\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Year / Month / Day" )+"\" data-role=\"datebox\" data-options='{\"mode\":\"calbox\", \"useNewStyle\":true, \"overrideCalHeaderFormat\": \"%Y / %m / %d\", \"overrideDateFormat\": \"%Y/%m/%d\" }' >";
 		    break;
 		  case "date1":	//Year
 		    s += "    <input type=\"date\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Year" )+"\" data-role=\"datebox\" data-options='{\"mode\":\"flipbox\", \"useNewStyle\":true, \"overrideHeaderFormat\": \"%Y\", \"overrideDateFormat\": \"%Y\", \"overrideDateFieldOrder\":[\"y\"] }' >";
 		    break;
 		  case "date2":	//Month
 		    s += "    <input type=\"date\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Month" )+"\" data-role=\"datebox\" data-options='{\"mode\":\"flipbox\", \"useNewStyle\":true, \"overrideHeaderFormat\": \"%m\", \"overrideDateFormat\": \"%m\", \"overrideDateFieldOrder\":[\"m\"] }' >";
 		    break;
 		  case "date3":	//Day
 		    s += "    <input type=\"date\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Day" )+"\" data-role=\"datebox\" min=\"2016-01-01\" max=\"2016-01-31\" data-options='{\"mode\":\"flipbox\", \"useNewStyle\":true, \"overrideHeaderFormat\": \"%d\", \"overrideDateFormat\": \"%d\", \"overrideDateFieldOrder\":[\"d\"] }' >";
 		    break;
 		  case "date4":	//Year / Month
 		    s += "    <input type=\"date\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Year / Month" )+"\" data-role=\"datebox\" data-options='{\"mode\":\"calbox\", \"useNewStyle\":true, \"overrideCalHeaderFormat\": \"%Y / %m\", \"overrideDateFormat\": \"%Y/%m\" }' >";
 		    break;
 		  case "date5":	//Month / Day
 			s += "    <input type=\"date\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Month / Day" )+"\" data-role=\"datebox\" min=\"2016-01-01\" max=\"2016-12-31\" data-options='{\"mode\":\"datebox\", \"useNewStyle\":true, \"overrideHeaderFormat\": \"%m / %d\",  \"overrideDateFormat\": \"%m/%d\", \"overrideDateFieldOrder\":[\"m\",\"d\"] }' >";
 			break;
 		  case "time":	//Hour : Minute
 			s += "    <input type=\"time\" name=\""+name+"\" placeholder=\""+( (!placeholder.isEmpty())? placeholder : "Ex) 12:01" )+"\" data-role=\"datebox\" data-options='{\"mode\":\"timebox\", \"overrideTimeFormat\":24, \"useNewStyle\":true }'>";
 			break;
 		}
 		//Log.e("formValidation = "+s);
 		return s+"\n";
 	}
 	
 	
 	
 	//20130529 dynamic
 	//20131118 dynamic
 	static String dynamicString = "";
 	static String dynamicHTMLbuf0 = "";
 	static String dynamicHTMLbuf = "";
 	static int dynamicCount = 1;
 	static String dynamicFuncCountLabel = "___DynamicFunc_CountLabel___";
 	public static boolean dynamicDisplay = false;
 	public static String dynamicFuncArgProcess(ITFE tfe){
 		//For Function
 		return createDynamicAttribute(tfe);
 	}
 	public static String dynamicAttributeProcess(ITFE tfe){
 		//For Attribute (C1, C2, G1, G2)
 		return createDynamicAttribute(tfe);
 	}
 	private static String createDynamicAttribute(ITFE tfe){
 		String s = ""+tfe;
 		s = s.trim();
 		if(s.startsWith("\"") && s.endsWith("\"")){
 			//not attribute
 			s = s.substring(1,s.length()-1);
 		}else{
 			//attribute
 			s = "'||"+s+"||'";
 		}
 		return s;
 	}
 	public static String getDynamicLabel(){	//+Mobile_HTML5.getDinamicLabel()
 		//For function's count
 		//※ Count付きのfunc()には、+Mobile_HTML5.getDinamicLabel()を付加する
 		//TODO dynamicFuncCountLabelがユニーク値かどうか判定
 		if(dynamicDisplay){
 			return dynamicFuncCountLabel;
 		}
 		return "";
 	}
 	//For Dynamic paging
 	static int dynamicRow = 1;
 	static boolean dynamicRowFlg = false;
 	static int dynamicPagingCount = 1;
 	static String dynamicPHPfileName =  "";
 	//ajax
 	static int ajax_loadInterval = 0;
 	
 	private static boolean dynamicPreProcess0(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("dynamic")){
 			dynamicHTMLbuf0 = html_env.code.toString();
 			dynamicDisplay = true;
 			dynamicPHPfileName = html_env.getFileName2()+"_dynamic_"+dynamicCount+".php";
 			
 	        if(decos.containsKey("row")){
 	        	dynamicRow = Integer.parseInt(decos.getStr("row").replace("\"", ""));
 	        	if(dynamicRow<1){	//範囲外のとき
 	        		Log.err("<<Warning>> row指定の範囲は、1〜です。指定された「row="+dynamicRow+"」は使用できません。");
 	        	}else{
 	        		dynamicRowFlg = true;
 	        		dynamicPHPfileName = html_env.getFileName2()+"_dynamicPaging_"+dynamicPagingCount+".php";
 	        	}
 	        	//Log.i("dynamicRow = "+dynamicRow);
 	        }
 			return true;
 		}
 		return false;
 	}
 	private static boolean dynamicPreProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("dynamic")){
 			dynamicHTMLbuf = html_env.code.toString();
 			dynamicDisplay = true;
 			return true;
 		}
 		return false;
 	}
 	private static boolean dynamicStringGetProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("dynamic")){
 			String currentHTML = html_env.code.toString();
 			dynamicString = currentHTML.substring(dynamicHTMLbuf.length(), currentHTML.length());
 			html_env.code = new StringBuffer(dynamicHTMLbuf);
 			//dynamicDisplay = false;
 			return true;
 		}
 		return false;
 	}
 	private static boolean dynamicProcess(String symbol, DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("dynamic")){
 			//if(symbol.contains("Attribute")){
 			//	Log.e("in dynamic: <"+symbol+"> "+data+" "+data_info+" "+tfe+" "+tfes+" "+tfeItems);
 			//}
 //			html_env.code.append("</DIV><!-- End of Show More -->\n");
 			
 //			dynamicWhileCount = 0;	//init
 			
 			if(symbol.contains("G1") || symbol.contains("G2")){
 //				String currentHTML = html_env.code.toString();
 //				String cut = currentHTML.substring(dynamicHTMLbuf0.length(), currentHTML.length());
 				html_env.code = new StringBuffer(dynamicHTMLbuf0);
 			}
 			
 //			Log.e("");
 //			Log.e("");
 //			Log.e(dynamicString2);
 //			Log.e("");
 //			Log.e("");
 			//Log.e(" - Start dynamic process -");
 //			Log.e(dynamicString);
 			
 			
 			
 			//ajax load interval
 			if(decos.containsKey("ajax-load") || decos.containsKey("load") || decos.containsKey("load-interval")
 					|| decos.containsKey("load-next") || decos.containsKey("load-next-page")
 					|| decos.containsKey("reload")){
 				String s = "";
 				if(decos.containsKey("ajax-load")) 				s = decos.getStr("ajax-load");
 				else if(decos.containsKey("load")) 				s = decos.getStr("load");
 				else if(decos.containsKey("load-interval"))		s = decos.getStr("load-interval");
 				else if(decos.containsKey("load-next"))			s = decos.getStr("load-next");
 				else if(decos.containsKey("load-next-page"))	s = decos.getStr("load-next-page");
 				else if(decos.containsKey("reload"))			s = decos.getStr("reload");
 				s = s.trim().toLowerCase().replaceAll("sec", "").replaceAll("s", "");
 				ajax_loadInterval = (int) (Float.parseFloat(s)*1000.0);
 				//Log.i(ajax_loadInterval);
 			}
 			 
 			
 			
 //			dynamicString = dynamicString.replaceAll("'", "\\\\\\\\\\\\\'");											//　' -> \'				//TODO これでOK?
 			
 			//TODO div, table以外の場合
 //			dynamicString = "'"+dynamicString2+"</div>'";
 			if(!symbol.contains("G1") && !symbol.contains("G2")){
 				if(decos.containsKey("table")){
 					dynamicString = "'<table border=1><tr>"+dynamicString+"</tr></table>'";
 				}else if(decos.containsKey("table0")){
 					dynamicString = "'<table><tr>"+dynamicString+"</tr></table>'";
 	//				String close = "";
 	//				if(symbol.contains("Attribute")){
 	//					close = "</td></tr>";
 	//				}else if(symbol.contains("C1") || symbol.contains("G1")){
 	//					close = "</tr>";
 	//				}
 	//				dynamicString = "'"+dynamicString+close+"</table>'";
 				}else{
 					dynamicString = "'"+dynamicString+"</div>'";
 				}
 			}else{
 				//G1, G2
 				dynamicString = "'"+dynamicString+"'";
 			}
 			
 			dynamicString = dynamicString.replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", "");	//改行コードの削除
 //			if(!dynamicRowFlg)	dynamicString = dynamicString.replaceAll("\"", "\\\\\\\\\\\\\"");			//　" -> \\\"			//TODO これでOK?
 //			else				dynamicString = dynamicString.replaceAll("\"", "\\\\\"");					//　" -> \"
 			dynamicString = dynamicString.replaceAll("\"", "\\\\\"");										//　" -> \"
 			//Log.e(dynamicString);
 			
 			
 			//String after_from_string = Mobile_HTML5Function.after_from_string;	//TODO
 			
 			
 		    /*  //ユーザ定義
 			    $sqlite3_DB = '/Users/goto/Desktop/SQLite_DB/sample2.db';
 			    $dynamic_col = "w.name, pr.name, count(*), w.r_year, ko.kind";
 			    $col_num = 5;                          //カラム数(Java側で指定)
 	    	 *    $table = 'world_heritage w, prefectures pr, wh_prefectures wpr, kind_of_wh ko';
 	    	 *    $where0 = 'w.wh_id=wpr.wh_id and wpr.p_id=pr.p_id and w.k_id=ko.k_id';
 			    $dynamic_col_array = array("w.name","pr.name", "count(*)", "w.r_year", "ko.kind");
 	    	 *    $groupby = " pr.name "; 	           //null => WHERE句にlikeを書く／ not null => HAVING句に～    //[要] Java側で、列名に予約語から始まるものがあるかチェック
 	    	 *    $having0 = " count(*)>1 ";
 	    	 *    $orderby = " ORDER BY w.name asc ";
 	    	 *    $limit = " LIMIT 10 ";
 	    	 */
 	    	
 	    	
 //	    	String title = "";
 	    	String columns = "";
 	    	String after_from = "";
 	    	//columns = "w.wh_id, w.name";
 	    	//columns = "'<h1>'||w.wh_id||' '||w.name||'</h1>'";
 	    	columns = dynamicString;
 	    	//after_from = "FROM world_heritage w";
 	    	after_from = Mobile_HTML5Function.after_from_string;
 	    	
 //	    	try{
 ////	    		//title（第一引数）
 //////	    		FuncArg fa1 = (FuncArg) this.Args.get(0);
 ////	    		if(!fa1.getStr().equals(""))	title = fa1.getStr();
 ////	    		else							title = "Dynamic";
 //	    		//columns（第二引数）
 //	    		FuncArg fa2 = (FuncArg) this.Args.get(1);
 //	    		columns += fa2.getStr();
 //	    		//after_from（第三引数）
 //	    		FuncArg fa3 = (FuncArg) this.Args.get(2);
 //	    		after_from += fa3.getStr().trim();
 //	    	}catch(Exception e){
 //	    		Log.info("<Warning> serach関数の引数が不足しています。 ex. dynamic(\"title\", \"c1:column1, c2:column2, ... \", \"From以下\")");
 //	    		return false;
 //	    	}
 //	    	if(columns.trim().equals("") || after_from.equals("")){
 //	    		Log.info("<Warning> serach関数の引数が不足しています。 ex. dynamic(\"title\", \"c1:column1, c2:column2, ... \", \"From以下\")");
 //	    		return false;
 //	    	}
 	    	if(after_from.toLowerCase().startsWith("from "))	after_from = after_from.substring("from".length()).trim();
 	    	//Log.info(title);
 	    	
 	    	int col_num=1;
 	    	String columns0 = columns;
 	    	while(columns0.contains(",")){
 	    		columns0 = columns0.substring(columns0.indexOf(",")+1);
 	    		col_num++;		//カウント
 	    	}
 	    	String[] s_name_array = new String[col_num];
 	    	String[] s_array = new String[col_num];
 	    	columns0 = columns;
 	    	for(int i=0; i<col_num-1; i++){
 	    		s_array[i] = columns0.substring(0,columns0.indexOf(","));
 	    		columns0 = columns0.substring(columns0.indexOf(",")+1);
 	    		//Log.i( "s_array["+i+"] = "+s_array[i]+"	"+columns0);
 	    	}
 	    	s_array[col_num-1] = columns0;
 	    	//Log.i( "s_array["+(col_num-1)+"] = "+s_array[col_num-1]);
 	    	int j=0;
 	    	for(int i=0; i<col_num; i++){
 	    		if(s_array[i].contains(":")){
 	    			if(!s_array[i].substring(0,s_array[i].indexOf(":")).contains(")"))
 	    				s_name_array[j++] = s_array[i].substring(0,s_array[i].indexOf(":"));
 	    			s_array[i] = s_array[i].substring(s_array[i].indexOf(":")+1);
 	    		}else{
 	    			if(!s_array[i].contains(")"))	s_name_array[j++] = s_array[i];
 	    		}
 	    		//Log.i("s_name_array["+(j-1)+"] = "+s_name_array[j-1] + "	s_array["+i+"] = "+s_array[i]);
 	    	}
 	    	boolean groupbyFlg = false;	//Flg
 	    	//boolean[] aFlg = new boolean[col_num];	//Flg
 	    	//boolean[] popFlg = new boolean[col_num];	//Flg
 	    	String a = "";
 	    	String dynamic_col = "";
 	    	String dynamic_col_array = "\"";
 	    	String dynamic_aFlg = "\"";		//Flg
 	    	String dynamic_mailFlg = "\"";		//Flg
 	    	String dynamic_popFlg = "\"";	//Flg
 	    	int a_pop_count = 0;
 	    	for(int i=0; i<col_num; i++){
 	    		a = s_array[i].replaceAll(" ","");
 	    		if( a.startsWith("max(") || a.startsWith("min(") || a.startsWith("avg(") ||  a.startsWith("count(") )	groupbyFlg = true;
 	    		if(a.startsWith("a(") || a.startsWith("anchor(")){
 	    			dynamic_aFlg += "true\""+((i<col_num-1)?(",\""):(""));
 	    			if(a.endsWith(")")){
 	    				dynamic_col += s_array[i]+",";
 	    				dynamic_col_array += s_array[i]+"\",\"";
 	    				dynamic_aFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				dynamic_mailFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				dynamic_popFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    			}else	a_pop_count++;
 	    		}else
 	    			dynamic_aFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    		if(a.startsWith("mail(")){
 	    			dynamic_mailFlg += "true\""+((i<col_num-1)?(",\""):(""));
 	    			if(a.endsWith(")")){
 	    				dynamic_col += s_array[i]+",";
 	    				dynamic_col_array += s_array[i]+"\",\"";
 	    				dynamic_aFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				dynamic_mailFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				dynamic_popFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    			}else	a_pop_count++;
 	    		}else
 	    			dynamic_mailFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    		if(a.startsWith("pop(") || a.startsWith("popup(")){
 	    			dynamic_popFlg += "true\""+((i<col_num-1)?(",\""):(""));
 	    			if(a.endsWith(")")){
 	    				dynamic_col += s_array[i]+",";
 	    				dynamic_col_array += s_array[i]+"\",\"";
 	    				dynamic_aFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				dynamic_mailFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    				dynamic_popFlg += ((i<col_num-1)?(""):(",\""))+"false\""+((i<col_num-1)?(",\""):(""));
 	    			}else	a_pop_count++;
 	    		}else
 	    			dynamic_popFlg += "false\""+((i<col_num-1)?(",\""):(""));
 	    		dynamic_col += s_array[i] +((i<col_num-1)?(","):(""));
 	    		dynamic_col_array += s_array[i] +"\""+((i<col_num-1)?(",\""):(""));
 	    	}
 	    	col_num -= a_pop_count;
 	//    	dynamic_col = dynamic_col.replaceAll("a\\(","").replaceAll("anchor\\(","").replaceAll("mail\\(","").replaceAll("pop\\(","").replaceAll("popup\\(","").replaceAll("\\)","");
 	//    	dynamic_col_array = dynamic_col_array.replaceAll("a\\(","").replaceAll("anchor\\(","").replaceAll("mail\\(","").replaceAll("pop\\(","").replaceAll("popup\\(","").replaceAll("\\)","");
 	    	dynamic_col = dynamic_col.replaceAll("a\\(","").replaceAll("anchor\\(","").replaceAll("mail\\(","").replaceAll("pop\\(","").replaceAll("popup\\(","").replaceAll("count\\(\\*\\)","count[*]").replaceAll("\\)","").replaceAll("count\\[\\*\\]","count(*)");
 	    	dynamic_col_array = dynamic_col_array.replaceAll("a\\(","").replaceAll("anchor\\(","").replaceAll("mail\\(","").replaceAll("pop\\(","").replaceAll("popup\\(","").replaceAll("count\\(\\*\\)","count[*]").replaceAll("\\)","").replaceAll("count\\[\\*\\]","count(*)");
 	    	
 	    	//TODO 余計なコードの削除
 	    	dynamic_col = dynamicString;
 	    	dynamic_col_array = dynamicString;
 	    	
 	    	//Log.i("	1:"+title+"	2:"+columns+"	col_num:"+col_num);
 	    	//Log.i("	dynamic_col:"+dynamic_col+"	dynamic_col_array:"+dynamic_col_array);
 	    	//Log.i("	dynamic_aFlg:"+dynamic_aFlg+"	dynamic_popFlg:"+dynamic_popFlg);
 	    	//Log.i("	groupbyFlg: "+groupbyFlg);
 	    	
 	    	
 	    	String DBMS = GlobalEnv.getdbms();										//DBMS
 	    	String DB = GlobalEnv.getdbname();										//DB
 	    	
 	    	String query = "";
 	    	//Log.i(after_from_string);
 //	    	if(after_from.startsWith("#")){					//From以下をクエリの下(#*)から取ってくる場合	//TODO
 //	    		if(!after_from_string.contains(after_from)){
 //	    			Log.info("<Warning> dynamic関数の第三引数に指定されている '"+after_from+"' が見つかりません。");
 //	    			return false;
 //	    		}
 //	    		query = after_from_string
 //	    				.substring(after_from_string.indexOf(after_from)+after_from.length())
 //	    				.trim().toLowerCase();
 //	    		if(query.contains("#"))	query = query.substring(0,query.indexOf("#")).trim().toLowerCase();
 //	    	}else
 	    		query = after_from.toLowerCase();			//From以下を第三引数へ書く場合
 	
 	    	//Log.i("\n	Query: "+query);
 	    	String from = "";
 	    	String where = "";
 	    	String groupby = "";
 	    	String having = "";
 	    	String orderby = "";
 	    	String limit = "";
 	    	if(query.contains(" limit ")){
 	    		limit = query.substring(query.lastIndexOf(" limit ")+" limit ".length());
 	    		query = query.substring(0,query.lastIndexOf(" limit "));
 	    	}
 	    	if(query.contains(" order by ")){
 	    		orderby = query.substring(query.lastIndexOf(" order by ")+" order by ".length());
 	    		query = query.substring(0,query.lastIndexOf(" order by "));
 	    	}
 	    	if(query.contains(" having ")){
 	    		having = query.substring(query.lastIndexOf(" having ")+" having ".length());
 	    		having = having.replaceAll("\\\"","\\\\\"");	// " -> \"
 	    		query = query.substring(0,query.lastIndexOf(" having "));
 	    	}
 	    	if(query.contains(" group by ")){
 	    		groupby = query.substring(query.lastIndexOf(" group by ")+" group by ".length());
 	    		query = query.substring(0,query.lastIndexOf(" group by "));
 	    	}
 	    	if(query.contains(" where ")){
 	    		where = query.substring(query.lastIndexOf(" where ")+" where ".length());
 	    		where = where.replaceAll("\\'","\\\\'");		// ' -> \'
 	    		query = query.substring(0,query.lastIndexOf(" where "));
 	    	}
 	    	from = query.trim();
 	    	//Log.i("	FROM: "+from+"\n	WHERE: "+where+"\n	GROUP: "+groupby+"\n	HAVING: "+having);
 	    	//Log.i("	ORDER: "+orderby+"\n	LIMIT: "+limit+"\n	Query: "+query);
 	    	
 	    	if(!groupbyFlg){
 	    		groupby = "";
 	    		having = "";
 	    	}
 	    	
 	    	
 	    	String statement = "";
 	    	String php = "<?php\n";
 	    	//sqlite3 php
 	    	if(DBMS.equals("sqlite3")){
 	    		if(!dynamicRowFlg){
 		    		statement += getDynamicHTML(dynamicCount, dynamicPHPfileName);
 //		    				"<!-- Dynamic start -->\n" +
 //							"<!-- Dynamic Panel start -->\n" +
 //							//"<br>\n" +
 //							"<div id=\"DYNAMIC"+dynamicCount+"panel\" style=\"\" data-role=\"none\">\n" +
 //	//						"<hr>\n<div style=\"font-size:30;\" id=\"DynamicTitle"+dynamicCount+"\">"+title+"</div>\n<hr>\n" +
 //							//"<br>\n" +
 //							"\n" +
 //							"<div id=\"Dynamic"+dynamicCount+"_text0\" data-role=\"none\"><!--データ --></div>\n" +
 //							"\n" +
 //	//						"<table style=\"table-layout:fixed;\" data-role=\"table\" id=\"table-column-toggle"+dynamicCount+"\" data-mode=\"columntoggle\" class=\"ui-responsive table-stroke\">\n" +
 //	//						"  <thead>\n" +
 //	//						"    <tr id=\"Dynamic"+dynamicCount+"_text_th\">\n";
 //	//	    		for(int i=0; i<col_num; i++){
 //	//	    			statement += 
 //	//						"        <th data-priority=\"1\">"+s_name_array[i]+"</th>\n";
 //	//	    		}
 //	//	    		statement += 
 //	//	    				"    </tr>\n" +
 //	//						"  </thead>\n" +
 //	//						"  <tbody>\n" +
 //	//						"    <tr>\n";
 //	//	    		for(int i=0; i<col_num; i++){
 //	//	    			statement +=
 //	//						"        <td id=\"Dynamic"+dynamicCount+"_text"+(i+1)+"\" style=\"white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\"></td>\n";
 //	//	    		}
 //	//	    		statement += 
 //	//	    				"    </tr>\n" +
 //	//						"  </tbody>\n" +
 //	//						"</table>\n" +
 //	//						"\n" +
 //							//"<br>\n" +
 //							"</div>\n" +
 //	//						"<script type=\"text/javascript\"> $('#Dynamic"+dynamicCount+"_text_th').hide(); </script>\n" +
 //							"<!-- Dynamic Panel end -->\n" +
 //							"\n";
 	    		}else{
 	    			statement += getDynamicPagingHTML(dynamicRow, dynamicPagingCount, dynamicPHPfileName);
 	    		}
 	    		
 	    		
 	    		php += 
 						"$ret = array();\n" +
 						"$ret['result'] = \"\";\n\n";
 	    		if(dynamicRowFlg){
 	    			php += 
 							"if ($_POST['currentPage'] != \"\") {\n" +
 							"	$cp = $_POST['currentPage'];\n" +
 							"	$r = $_POST['row'];\n" +
 							"	$end = $cp * $r;\n" +
 							"	$start = $end - $r + 1;\n" +
 							"\n";
 	    		}
 	    		php +=
 //    						"if($_POST['dynamic"+dynamicCount+"'] || $_POST['dynamic_words"+dynamicCount+"']){\n" +
 //    						"    echo '<script type=\"text/javascript\">window.parent.Dynamic"+dynamicCount+"_refresh();</script>';    //表示をリフレッシュ\n" +
 //    						"\n" +
     						"    //ユーザ定義\n" +
 							"    $sqlite3_DB = '"+DB+"';\n" +
 							"    $dynamic_col = \""+dynamic_col+"\";\n" +
 							"    $col_num = "+col_num+";                          //カラム数(Java側で指定)\n" +
 							"    $table = '"+from+"';\n" +
 							"    $where0 = '"+where+"';\n" +
 							"    $dynamic_col_array = array("+dynamic_col_array+");\n" +
 							"    $dynamic_col_num = count($dynamic_col_array);\n" +
 							"    $dynamic_a_Flg = array("+dynamic_aFlg+");\n" +
 							"    $dynamic_mail_Flg = array("+dynamic_mailFlg+");\n" +
 							"    $dynamic_pop_Flg = array("+dynamic_popFlg+");\n" +
 							"    $groupby = \""+groupby+"\"; 	           //null => WHERE句にlikeを書く／ not null => HAVING句に～    //[要] Java側で、列名に予約語から始まるものがあるかチェック\n" +
 							"    $having0 = \""+having+"\";\n" +
 							"    $orderby = \""+((orderby!="")?(" ORDER BY "+orderby+" "):("")) +"\";\n" +
 							"    $limit = \""+((limit!="")?(" LIMIT "+limit+" "):("")) +"\";\n" +
 							((limit!="")?(" $limitNum = "+limit+";\n"):("")) +	//TODO dynamicPaging時にLIMITが指定されていた場合
 							"\n" +
 							//"    $dynamicWord"+dynamicCount+" = checkHTMLsc($_POST['dynamic_words"+dynamicCount+"']);\n" +
 							"    $dynamicWord"+dynamicCount+" = checkHTMLsc('%');\n" +
 							"    $dynamicWord"+dynamicCount+" = preg_replace('/　/', ' ', $dynamicWord"+dynamicCount+");       //全角スペースを半角スペースへ\n" +
 							"    $dynamicWord"+dynamicCount+" = preg_replace('/\\s+/', ' ', $dynamicWord"+dynamicCount+");      //連続する半角スペースを1つの半角スペースへ\n" +
 							"    $dynamicWord"+dynamicCount+" = trim($dynamicWord"+dynamicCount+");                            //trim\n" +
 							"    $dynamicWord"+dynamicCount+" = preg_replace('/\\s/', '%', $dynamicWord"+dynamicCount+");       //半角スペースを%へ変換\n" +
 							"\n" +
 							"    if($dynamicWord"+dynamicCount+" != \"\"){\n" +
 							"        $dynamic_db"+dynamicCount+" = new SQLite3($sqlite3_DB);\n" +
							//"        $sql = \"SELECT DISTINCT \".$dynamic_col.\" FROM \".$table;\n" +
							"        $sql = \"SELECT \".$dynamic_col.\" FROM \".$table;\n" +
 							"        if($where0 != \"\")    $sql .= \" WHERE \".$where0.\" \";\n" +
 							"    \n" +
 							"    	//左辺の作成（※Java側でOK?)\n" +
 							"        $sw = $dynamicWord"+dynamicCount+";\n" +
 							"        $sw_buf = \"\";\n" +
 							"        $l_str = \"\";\n" +
 							"        foreach($dynamic_col_array as $val)    $l_str .= \"ifnull(\".$val.\",'')||\";\n" +
 							"        $l_str = substr($l_str, 0, -2);      //substring   最後の||をカット\n" +
 							"        $l_str .= \" LIKE '%\";\n" +
 							"        //右辺の作成\n" +
 							"        while(strpos($sw,'%')){		//%を含んでいる間\n" +
 							"            $pos = strpos($sw,'%');          //indxOf  		%が最初に現れる位置\n" +
 							"            $rest = substr($sw, 0, $pos);    //substring    最初の%以降をカット\n" +
 							"            $sw = substr($sw, $pos+1);       //substring    最初の%までカット\n" +
 							"            $sw_buf .= $l_str.$rest.\"%' AND \";\n" +
 							"        }\n" +
 							"        $sw_buf .= $l_str.$sw.\"%' \";         //最後のswを結合\n" +
 							"        \n" +
 							"        if($groupby == \"\"){    //null => WHERE句にlikeを書く／ not null => HAVING句に～\n" +
 							"            /*** WHERE句の作成 start ***/\n" +
 							"            //WHERE  ifnull(id,'')||ifnull(name,'')||ifnull(r_year,'') LIKE '%sw[1]%'\n" +
 							"            //   AND ifnull(id,'')||ifnull(name,'')||ifnull(r_year,'') LIKE '%sw[2]%'...\n" +
 							"            \n" +
 							"            $WHERE = \"\";\n" +
 							"            if($where0 == \"\")   $WHERE = \" WHERE \";\n" +
 							"            else                $WHERE = \" AND \";\n" +
 							"            $WHERE .= $sw_buf;\n" +
 							"            \n" +
 							"            $sql .= \" \".$WHERE.\" \";\n" +
 							"            //$sql .= $WHERE.\" \".$groupby.\" \";\n" +
 							"            /*** WHERE句の作成 end ***/\n" +
 							"        }else{                        //null => WHERE句にlikeを書く／ not null => HAVING句に～\n" +
 							"            /*** HAVING句の作成 start ***/\n" +
 							"            //HAVING  ifnull(id,'')||ifnull(name,'')||ifnull(r_year,'') LIKE '%sw[1]%'\n" +
 							"            //    AND ifnull(id,'')||ifnull(name,'')||ifnull(r_year,'') LIKE '%sw[2]%'...\n" +
 							"            \n" +
 							"            $HAVING = \"\";\n" +
 							"            if($having0 == \"\")  $HAVING = \" HAVING \";\n" +
 							"            else	            $HAVING = \" HAVING \".$having0.\" AND \";\n" +
 							"    		$HAVING .= $sw_buf;\n" +
 							"            \n" +
 							"            $sql .= \" GROUP BY \".$groupby.\" \".$HAVING;\n" +
 							"            /*** HAVING句の作成 end ***/\n" +
 							"        }\n" +
 							"        $sql .= \" \".$orderby.\" \".$limit;	//order by句とlimitを結合\n" +
 							//((dynamicRowFlg)? "//":"") +
 							//"        dynamic"+dynamicCount+"_p1('<font color=red>SQL error: '.$sql.\";</font>\");	//エラー時\n" +
 							"\n" +
 							"        $result = $dynamic_db"+dynamicCount+"->query($sql);\n" +
 							"\n" +
 							"        $i = 0;\n" +
 							"        $pop_num = 0;\n" +
 							"        $b = \"\";\n" +
 							"        while($row = $result->fetchArray()){\n" +
 							"              $i++;\n" +
 							//"              $k=0;\n" +
 							((dynamicRowFlg)? "              if($i>=$start && $i<=$end){	//New\n":"") +
 							"              for($j=0; $j<$dynamic_col_num; $j++){\n" +
 							//"                    dynamic"+dynamicCount+"_p2($row[$j], $j+1);     //tdに結果を埋め込む\n" +
 //							"					if($dynamic_a_Flg[$j]=='true' || $dynamic_mail_Flg[$j]=='true' || $dynamic_pop_Flg[$j]=='true')	;\n" +
 //							"                    else if($j>0 && $dynamic_a_Flg[$j-1]=='true')	dynamic"+dynamicCount+"_p2('<a href=\\\"'.$row[$j].'\\\" target=\\\"_blank\\\" rel=\\\"external\\\" style=\\\"white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\\\">'.$row[$j-1].'</a>', ++$k);     //tdに結果を埋め込む\n" +
 //							"                    else if($j>0 && $dynamic_mail_Flg[$j-1]=='true')	dynamic"+dynamicCount+"_p2('<a href=\\\"mailto:'.$row[$j].'\\\" target=\\\"_self\\\" style=\\\"white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\\\">'.$row[$j-1].'</a>', ++$k);     //tdに結果を埋め込む\n" +
 //							"                    //else if($j>0 && $dynamic_pop_Flg[$j-1]=='true')	dynamic"+dynamicCount+"_p2('<a href=\\\"'.$row[$j].'\\\" target=\\\"_blank\\\" rel=\\\"external\\\" style=\\\"white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\\\">'.$row[$j-1].'</a>', ++$k);     //tdに結果を埋め込む\n" +
 //							"                    else if($j>0 && $dynamic_pop_Flg[$j-1]=='true' && !is_null($row[$j])){\n" +
 //							"                    	$pop_str = '<a href=\\\"#dynamic_popup1_'.(++$pop_num).'\\\" data-rel=\\\"popup\\\" data-icon=\\\"arrow-r\\\" style=\\\"white-space:nowrap; overflow:hidden; text-overflow:ellipsis;\\\">'.$row[$j-1].'</a>'\n" +
 //							"							.'<div data-role=\\\"popup\\\" id=\\\"dynamic_popup1_'.($pop_num).'\\\" data-transition=\\\"slideup\\\" style=\\\"width:95%;\\\" data-overlay-theme=\\\"a\\\">'\n" +
 //							"								.'<a href=\\\"#\\\" data-rel=\\\"back\\\" data-role=\\\"button\\\" data-theme=\\\"a\\\" data-icon=\\\"delete\\\" data-iconpos=\\\"notext\\\" class=\\\"ui-btn-right\\\">Close</a>'\n" +
 //							"								.'<h2>'.$row[$j-1].'</h2>'\n" +
 //							"								.'<p>'.$row[$j].'</p>'\n" +
 //							"							.'</div>';\n" +
 //							"                    	dynamic"+dynamicCount+"_p2($pop_str, ++$k);     	//tdに結果を埋め込む\n" +
 //							"                    }else									dynamic"+dynamicCount+"_p2($row[$j], ++$k);     //tdに結果を埋め込む\n" +
 							"              		//$b .= $row[$j];\n" +
 							"              		$b .= str_replace("+dynamicFuncCountLabel+", '_'.$i, $row[$j]);\n" +	//For function's count
 							"              }\n" +
 							((dynamicRowFlg)? "              }\n":"") +
 							"        }\n" +
 //							"		 if($i>0)	echo \"<script type=\\\"text/javascript\\\">window.parent.$('#Dynamic"+dynamicCount+"_text_th').show();</script>\";    //カラム名を表示\n" +
 							//"        dynamic"+dynamicCount+"_p1($i.' result'.(($i != 1)?('s'):('')));    //件数表示\n" +
 							//"        dynamic"+dynamicCount+"_p1('"+dynamicString+"');    //件数表示\n" +
 							//((dynamicRowFlg)? "//":"") +
 							//"        dynamic"+dynamicCount+"_p1($b);    //データ表示\n" +
 							"    }\n" +
 							//"	 else{\n" +
 							//((dynamicRowFlg)? "//":"") +
 							//"        dynamic"+dynamicCount+"_p1('0 results');\n" +
 							//"    }\n" +
 							"    \n" +
 							"    unset($dynamic_db"+dynamicCount+");\n" +
 	    					((dynamicRowFlg)? "}\n":"") +
 							//"function dynamic"+dynamicCount+"_p1($str){\n" +
 							//((dynamicRowFlg)? "//":"") +
 							//"    echo '<script type=\"text/javascript\">window.parent.Dynamic"+dynamicCount+"_echo1(\"'.$str.'\");</script>';\n" +
 							//"}\n" +
 //							"function dynamic"+dynamicCount+"_p2($str,$num){\n" +
 //							"    echo '<script type=\"text/javascript\">window.parent.Dynamic"+dynamicCount+"_echo2(\"'.$str.'\",\"'.$num.'\");</script>';\n" +
 //							"}\n";
 	    					"$ret['result'] = $b;\n";
 	    		if(dynamicRowFlg){
 	    			php += 
 							"$ret['start'] = $start;\n" +
 							"$ret['end'] = ($end<$i)? $end:$i;\n" +
 							"$ret['all'] = $i;\n" +
 							"$ret['info'] = (($ret['start']!=$ret['end'])? ($ret['start'].\" - \") : (\"\")) .$ret['end'].\" / \".$ret['all'];\n" +
 							"$ret['currentItems'] = ceil($i/$r);\n";
 	    		}
 	    		php += 
 							"\n" +
 							"header(\"Content-Type: application/json; charset=utf-8\");\n" +
 							"echo json_encode($ret);\n" +
 							"\n" +
 							"//XSS対策\n" +
 							"function checkHTMLsc($str){\n" +
 							"	return htmlspecialchars($str, ENT_QUOTES, 'UTF-8');\n" +
 							"}\n" +
 							"?>\n";
 	    		
 //	    		statement += 
 //    						"\n" +
 //    						"<script type=\"text/javascript\">\n" +
 //    						"function Dynamic"+dynamicCount+"_echo1(str){\n" +
 //    						"  var textArea = document.getElementById(\"Dynamic"+dynamicCount+"_text0\");\n" +
 //    						"  textArea.innerHTML = str;\n" +
 //    						"}\n" +
 ////    						"function Dynamic"+dynamicCount+"_echo2(str,num){\n" +
 ////    						"  var textArea = document.getElementById(\"Dynamic"+dynamicCount+"_text\"+num);\n" +
 ////    						//"  textArea.innerHTML += str+\"<br>\";\n" +
 ////    						//"  $(\"#Dynamic"+dynamicCount+"_text\"+num).html(textArea.innerHTML+str+\"<br>\").trigger(\"create\");\n" +
 ////    						"  $(\"#Dynamic"+dynamicCount+"_text\"+num).html(textArea.innerHTML+str+\"<br>\");\n" +
 ////    						"}\n" +
 ////    						"\n" +
 ////    						"function Dynamic"+dynamicCount+"_refresh(){\n";
 ////	    		
 ////	    		for(int i=0; i<col_num; i++){
 ////	    			statement +=
 ////	    					"  document.getElementById(\"Dynamic"+dynamicCount+"_text"+(i+1)+"\").innerHTML = \"\";\n";
 ////	    		}
 ////	    		statement +=
 ////	    					"}\n" +
 //    						"</script>\n" +
 //    						"<!-- Dynamic end -->\n";
 	    		
 	    		
 	    	}
 	    	else if(DBMS.equals("sqlite")){
 	    		Log.e("<Warning> dynamic() for 'sqlite' is not implemented yet.");
 	    		;	//TODO
 	    	}
 	    	else if(DBMS.equals("postgresql")){
 	    		Log.e("<Warning> dynamic() for 'postgresql' is not implemented yet.");
 	    		;	//TODO
 	    	}
 
 	    	
 	    	// 各引数毎に処理した結果をHTMLに書きこむ
 	    	html_env.code.append(statement);
 	    	
 	    	if(!dynamicRowFlg){
 	    		createFile(html_env, dynamicPHPfileName, php);//PHPファイルの作成
 //    			Mobile_HTML5Env.PHP += php;
 	    		dynamicCount++;
     		}else{
 				createFile(html_env, dynamicPHPfileName, php);//PHPファイルの作成
 				dynamicPagingCount++;
 				dynamicRowFlg = false;
 			}
 	    	//dynamicCount++;
 	    	dynamicDisplay = false;
 	    	ajax_loadInterval = 0;
 	    	
 	    	
 			//Log.e(" - End dynamic process -");
         	return true;
         }
 		return false;
 	}
 	private static String getDynamicHTML(int num, String phpFileName){
 		phpFileName = new File(phpFileName).getName();
 		String s =
 				"\n" +
 				"<!-- Dynamic "+num+" start -->\n" +
 				"<!-- Dynamic "+num+" DIV start -->\n" +
 				"<div id=\"SSQL_DynamicDisplay"+num+"_Panel\" style=\"\" data-role=\"none\">\n" +
 				"<div id=\"SSQL_DynamicDisplay"+num+"\" data-role=\"none\"><!-- Dynamic Display Data --></div>\n" +
 				"</div>\n" +
 				"<!-- Dynamic "+num+" DIV end -->\n" +
 				"\n" +
 				"<!-- Dynamic "+num+" JS start -->\n" +
 				"<script type=\"text/javascript\">\n" +
 				"SSQL_DynamicDisplay"+num+"();	//ロード時に実行\n";
 		if(ajax_loadInterval>0){
 			s += "setInterval(function(){\n" +
 				 "	SSQL_DynamicDisplay"+num+"();\n" +
 				 "},"+ajax_loadInterval+");\n";
 		}
 		s +=	"function SSQL_DynamicDisplay"+num+"_echo(str){\n" +
 				"  var textArea = document.getElementById(\"SSQL_DynamicDisplay"+num+"\");\n" +
 				"  textArea.innerHTML = str;\n" +
 				"}\n" +
 				"function SSQL_DynamicDisplay"+num+"(){\n" +
 				"	//ajax: PHPへ値を渡して実行\n" +
 				"	$.ajax({\n" +
 				"		type: \"POST\",\n" +
 				"		url: \""+phpFileName+"\",\n" +
 				"		dataType: \"json\",\n" +
 				"		success: function(data, textStatus){\n" +
 				"			if (data.result != \"\") {\n" +
 				"				SSQL_DynamicDisplay"+num+"_echo(data.result);\n" +
 				"			}\n" +
 				"		},\n" +
 				"		error: function(XMLHttpRequest, textStatus, errorThrown) {\n" +
 				"			SSQL_DynamicDisplay"+num+"_echo(textStatus+\"<br>\"+errorThrown);\n" +
 				"		}\n" +
 				"	});\n" +
 				"}\n" +
 				"</script>\n" +
 				"<!-- Dynamic "+num+" JS end -->\n" +
 				"<!-- Dynamic "+num+" end -->\n\n";
 		return s;
 	}
 	private static String getDynamicPagingHTML(int row, int num, String phpFileName){
 		phpFileName = new File(phpFileName).getName();
 		String s =
 				"\n" +
 				"<!-- Dynamic Paging "+num+" start -->\n" +
 				"<!-- Dynamic Paging "+num+" DIV start -->\n" +
 				//"<div id=\"SSQL_DynamicDisplayPaging"+num+"_Buttons\"></div>\n" +
 				"\n" +
 				"<div id=\"SSQL_DynamicDisplayPaging"+num+"\" data-role=\"none\"><!-- Dynamic Display Data --></div>\n" +
 				"<script type=\"text/javascript\">\n" +
 				"function SSQL_DynamicDisplayPaging"+num+"_echo(str){\n" +
 				"  var textArea = document.getElementById(\"SSQL_DynamicDisplayPaging"+num+"\");\n" +
 				"  textArea.innerHTML = str;\n" +
 				"}\n" +
 				"</script>\n" +
 				"\n" +
 				"<div id=\"SSQL_DynamicDisplayPaging"+num+"_Buttons\"></div>\n" +
 				"<!-- Dynamic Paging "+num+" DIV end -->\n" +
 				"\n" +
 				"<!-- Dynamic Paging "+num+" JS start -->\n" +
 				"<script type=\"text/javascript\">\n" +
 				"SSQL_DynamicDisplayPaging"+num+"(1);	//初期ロード時\n" +
 				"SSQL_DynamicDisplayPaging"+num+"_setButtons();\n" +
 				"\n" +
 				"var SSQL_DynamicDisplayPaging"+num+"_currentItems = 1;		//グローバル変数\n";
 		if(ajax_loadInterval>0){
 			s += "\n" +
 				 "setInterval(function(){\n" +
 				 "	$('#SSQL_DynamicDisplayPaging"+num+"_Buttons .next').trigger(\"click\");\n" +
 				 "},"+ajax_loadInterval+");\n\n";
 		}
 		s +=	"function SSQL_DynamicDisplayPaging"+num+"_setButtons(){\n" +
 				"	$(function(){\n" +
 				"	    $(\"[id=SSQL_DynamicDisplayPaging"+num+"_Buttons]\").pagination({\n" +
 				"	        items: SSQL_DynamicDisplayPaging"+num+"_currentItems, //ページング数\n" +
 				"	        displayedPages: 2, 	  //表示したいページング要素数\n" +
 				"	        onPageClick: function(pageNum){ SSQL_DynamicDisplayPaging"+num+"(pageNum) }\n" +
 				"	    })\n" +
 				"	});\n" +
 				"}\n" +
 				"function SSQL_DynamicDisplayPaging"+num+"(pn){\n" +
 				"	//ajax: PHPへ値を渡して実行\n" +
 				"	$.ajax({\n" +
 				"		type: \"POST\",\n" +
 				"		url: \""+phpFileName+"\",\n" +
 				"		dataType: \"json\",\n" +
 				"		data: {\n" +
 				"			\"currentPage\": pn,\n" +
 				"			\"row\": '"+row+"',\n" +
 				"		},\n" +
 				"		success: function(data, textStatus){\n" +
 				"			if (data.result != \"\") {\n" +
 				//"				//SSQL_DynamicDisplayPaging"+num+"_echo(SSQL_DynamicDisplayPaging"+num+"_currentItems+\" \"+data.currentItems+\"<br>\"+data.info+\"<br>\"+data.result);\n" +
 				"				SSQL_DynamicDisplayPaging"+num+"_echo(data.result+\"<span style='font-size:small; color:#808080;'>\"+data.info+\"</span>\");\n" +
 				"				if(data.currentItems != null && data.currentItems != SSQL_DynamicDisplayPaging"+num+"_currentItems){\n" +
 				"					//ページ数が変わった場合の処理\n" +
 				"					SSQL_DynamicDisplayPaging"+num+"_currentItems = data.currentItems;\n" +
 				"					SSQL_DynamicDisplayPaging"+num+"_setButtons();\n" +
 				"				}\n" +
 				"			}\n" +
 				//"			else {\n" +
 				//"				SSQL_DynamicDisplayPaging"+num+"_echo(\"失敗\");\n" +
 				//"			}\n" +
 				"		},\n" +
 				"		error: function(XMLHttpRequest, textStatus, errorThrown) {\n" +
 				"			SSQL_DynamicDisplayPaging"+num+"_echo(textStatus+\"<br>\"+errorThrown);\n" +
 				"		}\n" +
 				"	});\n" +
 				"}\n" +
 				"</script>\n" +
 				"<!-- Dynamic Paging "+num+" JS end -->\n" +
 				"<!-- Dynamic Paging "+num+" end -->\n\n";
 		return s;
 	}
 	
 	
 	
 	
 	
 	//20131106 show
 	static int show_count = 0;
 	static int show_count_G2 = 0;
 	//static boolean showFlg = false;
 	//static String showSymbol = "";	//important variable
 	private static boolean showProcess(DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("show") || decos.containsKey("show-first") || decos.containsKey("show-next")){
 			//showSymbol = symbol;
     		String show = "";
 			String showEach = "0";
     		if(decos.containsKey("show"))				show = decos.getStr("show").replaceAll("px", "").replaceAll(";", "");
     		else if(decos.containsKey("show-first"))	show = decos.getStr("show-first").replaceAll("px", "").replaceAll(";", "");
     		if(decos.containsKey("show-next"))	showEach = decos.getStr("show-next").replaceAll("px", "").replaceAll(";", "");
     		//Log.e(show+"  "+showEach);
     		if(show.isEmpty())	show = showEach;
     		//Log.e(show+"  "+showEach);
     		show_count++;
     		
     		String showClass = "ssql_showmore_"+show_count;
     		html_env.code.append(
     				"\n<script>\n" +
     				"	$(document).ready(function() {\n" +
     				"		$('."+showClass+"').showmore({\n" +
     				"	       maxHeight: "+show+",\n" +
     				"	       showEach: "+showEach+"\n" +
     				"	    });\n" +
     				"	});\n" +
     				"</script>\n\n" +
     				"<DIV class=\""+showClass+"\">\n");
     		//showFlg = true;
     		return true;
         }
 		else if(decos.containsKey("autoshow") || decos.containsKey("auto-show") || 
 				decos.containsKey("autoshow-first") || decos.containsKey("auto-show-first") ||
 				decos.containsKey("autoshow-next") || decos.containsKey("auto-show-next")){
 			String autoshow = "";
 			String autoshowEach = "0";
     		if(decos.containsKey("autoshow"))				autoshow = decos.getStr("autoshow").replaceAll("px", "").replaceAll(";", "");
     		else if(decos.containsKey("autoshow-first"))	autoshow = decos.getStr("autoshow-first").replaceAll("px", "").replaceAll(";", "");
     		else if(decos.containsKey("auto-show"))			autoshow = decos.getStr("auto-show").replaceAll("px", "").replaceAll(";", "");
     		else if(decos.containsKey("auto-show-first"))	autoshow = decos.getStr("auto-show-first").replaceAll("px", "").replaceAll(";", "");
     		if(decos.containsKey("autoshow-next"))			autoshowEach = decos.getStr("autoshow-next").replaceAll("px", "").replaceAll(";", "");
     		else if(decos.containsKey("auto-show-next"))	autoshowEach = decos.getStr("auto-show-next").replaceAll("px", "").replaceAll(";", "");
     		if(autoshow.isEmpty())	autoshow = autoshowEach;
     		show_count++;
     		
     		String autoshowClass = "ssql_showmore_"+show_count;
     		String autoshowButtonClass = "ssql_autoshowmore_"+show_count;
     		String loadID = "ssql_show_loading_"+show_count;
     		String loadImage = "jscss/loading.gif";
     		int timeout = 500;
     		html_env.code.append(
     				"\n<script>\n" +
 					"	$(function() {\n" +
 					"		//ページ最下部にきたら自動的に次を読み込む\n" +
 					"	    $(window).scroll(function() {\n" +
 					"		     var current = $(window).scrollTop() + window.innerHeight;\n" +
 					"		     if (current < $(document).height() - 100) return;\n" +
 					"		     if ($(this).data('loading')) return;\n" +
 					"\n" +
 					"		     $(this).data('loading', true);\n" +
 					"			 var boxHeight = $(\"."+autoshowClass+"\").outerHeight();\n" +
 					//"			 //alert( $(\"."+autoshowClass+"\").height() +\"/\"+ $(\"."+autoshowClass+"\").data().boxHeight);\n" +
 					"			 if( $(\"."+autoshowClass+"\").height() < $(\"."+autoshowClass+"\").data().boxHeight )\n" +
 					"			 	$(\"#"+loadID+"\").html(\"<div id='"+loadID+"'><img src='"+loadImage+"'/></div>\");\n" +
 					"		     setTimeout(function(){\n" +
 					"			 	$(\"#"+loadID+"\").html(\"<div id='"+loadID+"'></div>\");\n" +
 					"				$(\"."+autoshowButtonClass+"\").eq(0).click();\n" +
 					"	    	 },"+timeout+");\n" +
 					"		     $(this).data('loading', false);\n" +
 					"		});\n\n" +
     				"		$('."+autoshowClass+"').showmore({\n" +
     				//"		   moreLink: '<div><input type=\"button\" class=\""+autoshowButtonClass+"\" style=\"visibility:hidden;\"></div>',\n" +
     				"		   moreLink: '<div id=\""+loadID+"\"></div><div><input type=\"button\" class=\""+autoshowButtonClass+"\" style=\"visibility:hidden;\"></div>',\n" +
     				"	       maxHeight: "+autoshow+",\n" +
     				"	       showEach: "+autoshowEach+"\n" +
     				"	    });\n" +
     				"	});\n" +
     				"</script>\n\n" +
     				"<DIV class=\""+autoshowClass+"\">\n");
     		return true;
 		}
 		return false;
 	}
 	public static String addShowCountClassName(DecorateList decos){
 		//not use
 		if(decos.containsKey("show") || decos.containsKey("show-first") || decos.containsKey("show-next") ||
 		   decos.containsKey("autoshow") || decos.containsKey("autoshow-first") || decos.containsKey("autoshow-next")||
 		   decos.containsKey("auto-show") || decos.containsKey("auto-show-first") ||  decos.containsKey("auto-show-next")){
 			show_count_G2++;
 			return "show_count_G2_"+show_count+"_"+show_count_G2;
 		}
 		return "";
 	}
 	private static boolean showCloseProcess(DecorateList decos, Mobile_HTML5Env html_env){
 		if(decos.containsKey("show") || decos.containsKey("show-first") || decos.containsKey("show-next") ||
 		   decos.containsKey("autoshow") || decos.containsKey("autoshow-first") || decos.containsKey("autoshow-next")||
 		   decos.containsKey("auto-show") || decos.containsKey("auto-show-first") ||  decos.containsKey("auto-show-next")){
         //if(symbol.equals(showSymbol) && showFlg){
         	html_env.code.append("</DIV><!-- End of Show More -->\n");
         	//showFlg = false;
         	show_count_G2 = 0;
         	return true;
         }
 		return false;
 	}
 
 	
 	
 	//////////////////////////////////////////////////////////////////////////////////////////
 	
 	//create file
 	public static boolean createFile(Mobile_HTML5Env html_env, String fileName, String code){
         try {
     		PrintWriter pw;
             if (html_env.charset != null)
 	        	pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
 	        			new FileOutputStream(fileName),html_env.charset)));
             else
             	pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
     		pw.println(code);
             pw.close();
             return true;
         } catch (Exception e) { }
         return false;
     }
 	
 }
