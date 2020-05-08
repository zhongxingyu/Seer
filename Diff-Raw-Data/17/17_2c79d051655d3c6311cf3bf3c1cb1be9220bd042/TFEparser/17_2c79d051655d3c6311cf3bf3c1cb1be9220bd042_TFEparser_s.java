 package supersql.parser;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import supersql.codegenerator.Attribute;
 import supersql.codegenerator.CodeGenerator;
 import supersql.codegenerator.Connector;
 import supersql.codegenerator.FuncArg;
 import supersql.codegenerator.Function;
 import supersql.codegenerator.Grouper;
 import supersql.codegenerator.ITFE;
 import supersql.codegenerator.IfCondition;
 import supersql.codegenerator.TFE;
 
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 public class TFEparser {
 
 	private TFE schemaTop;
 
 	private ExtList sch;
 
 	private Hashtable attp;
 
 	private TFEtokenizer toks;
 
 	private int Level = 0;
 
 	private String dimch = "?,!%#";
 
 	private int attno;
 
 	private CodeGenerator cg;
 	
 	private static String att_tmp;
 	public TFEparser(String str, CodeGenerator cgenerator) {
 		attno = 0;
 		initialize(str, cgenerator);
 	}
 	
 	public TFEparser(String str, CodeGenerator cgenerator, int attno){
 		this.attno = attno;
 		initialize(str, cgenerator);
 	}
 	
 	private void initialize(String str, CodeGenerator cgenerator){
 		attp = new Hashtable();
 		toks = new TFEtokenizer(str);
 		cg = cgenerator;
 		
 
 		try {
 			schemaTop = connector("");
 			if (toks.hasMoreTokens()) {
 				System.err.println("*** Token remained after parsing TFE ***");
 				throw (new IllegalStateException());
 			}
 		} catch (IllegalStateException e) {
 			System.err.println("Error[TFEparser]: Syntax Error in TFE");
 			System.err.println(toks.DebugTrace());
 			GlobalEnv.addErr("Error[TFEparser]: Syntax Error in TFE");
 		}
 		sch = schemaTop.makesch();
 
 		Log.out("Schema is " + sch);
 		Log.out("le0 is " + schemaTop.makele0());;
 	}
 
 	private Grouper grouper() {
 
 		String token;
 
 		Level++;
 		Log.out("===Grouper=== Level=" + Level);
 
 		TFE operand1;
 
 		/* next is one operand */
 		/*
 		 * if (toks.lookToken().equals("[")) { toks.nextToken(); operand1 =
 		 * this.grouper(); } else { operand1 = connector(); }
 		 */
 		operand1 = connector("]");
 
 		token = toks.nextToken();
 
 		int dim = dimch.indexOf(token);
 		switch (dim) {
 		case 0:
 			Log.out("Grouping dim= ?");
 			break;
 		case 1:
 			Log.out("Grouping dim= ,");
 			break;
 		case 2:
 			Log.out("Grouping dim= !");
 			break;
 		case 3:
 			Log.out("Grouping dim= %");
 			break;
 		case 4:
 			Log.out("Grouping dim= #");
 			break;
 		default:
 			System.err.println("*** Grouper Operator Not Found after ']' ***");
 			throw (new IllegalStateException());
 		}
 
 		Grouper grp = cg.createGrouper(dim);
 		grp.setTFE(operand1);
 
 		this.setDecoration(grp);
 
 		Log.out("===Grouper end=== Level=" + Level);
 		Level--;
 
 		return grp;
 
 	}
 
 	private TFE connector(String closeparen) {
 
 		TFE outTFE;
 
 		Log.out("---connector---");
 		// Log.out(toks.DebugTrace());
 
 		/* read first operand */
 
 		TFE operand = read_attribute();
 
 		if (!toks.hasMoreTokens()) {
 			Log.out("* not connected *");
 			if (!closeparen.equals("")) {
 				System.err.println("*** Can't close paren '" + closeparen
 						+ "' ***");
 				throw (new IllegalStateException());
 			}
 			outTFE = operand;
 		} else if (toks.lookToken().equals("]")) {
 			Log.out("* not connected *");
 			if (!closeparen.equals("]")) {
 				System.err.println("*** mismatch paren at ']' ***");
 				throw (new IllegalStateException());
 			}
 			toks.nextToken();
 			outTFE = operand;
 		} else if (toks.lookToken().equals(")")) {
 			Log.out("* not connected *");
 			if (!closeparen.equals(")")) {
 				System.err.println("*** mismatch paren at ')' ***");
 				throw (new IllegalStateException());
 			}
 			toks.nextToken();
 			outTFE = operand;
 		} else if (toks.lookToken().equals("}")) {
 			Log.out("* not connected *");
 			if (!closeparen.equals("}")) {
 				System.err.println("*** mismatch paren at '}' ***");
 				throw (new IllegalStateException());
 			}
 			toks.nextToken();
 			this.setDecoration(operand);
 			outTFE = operand;
 		} else {
 			outTFE = connector_main(-10000, operand, closeparen);
 
 		}
 
 		Log.out("---close connector---");
 		// Log.out(toks.DebugTrace());
 
 		return outTFE;
 
 	}
 
 	private Connector connector_main(int dim, ITFE operand1, String closeparen) {
 
 		Log.out("connector_main: dim=" + dim);
 		String token = "";
 		int dim2 = -10000;
 		boolean different_op = false;
 		ExtList tfes = new ExtList();
 		tfes.add(operand1);
 
 		while (true) {
 
 			if (!toks.hasMoreTokens()) {
 				if (!closeparen.equals("")) {
 					toks.nextToken();
 					System.err.println("*** Detect end of TFE before paren '"
 							+ closeparen + "' ***");
 					throw (new IllegalStateException());
 				}
 				break;
 			}
 
 			/* read next oprator */
 			token = toks.nextToken();
 			Log.out("connector_operator*token=" + token);
 
 			if (token.equals("[")) {
 				/* error for operand */
 				System.err.println("*** Found '[' after Operand ***");
 				throw (new IllegalStateException());
 
 			} else if (token.equals("]")) {
 				if (!closeparen.equals("]")) {
 					System.err.println("*** mismatch paren at ']' ***");
 					throw (new IllegalStateException());
 				}
 				/* close group */
 				break;
 
 			} else if (token.equals("}")) {
 				if (!closeparen.equals("}")) {
 					System.err.println("*** mismatch paren at '}' ***");
 					throw (new IllegalStateException());
 				}
 				/* close paren */
 				break;
 
 			} else if (token.equals(")")) {
 				if (!closeparen.equals(")")) {
 					System.err.println("*** mismatch paren at ')' ***");
 					throw (new IllegalStateException());
 				}
 				/* close paren (func) */
 				break;
 
 			} else if (dimch.indexOf(token) == -1) {
 				/* error for operand */
 				System.err.println("*** Found Illegal Token after Operand ***");
 				throw (new IllegalStateException());
 
 			}
 
 			dim2 = dimch.indexOf(token);
 			Log.out("nextch : " + toks.lookToken());
 			if (token.equals(toks.lookToken())) {
 				dim2 = -dim2;
 				toks.nextToken();
 				Log.out("duplicated");
 			}
 
 			if (dim == -10000) {
 				/* first operator */
 				dim = dim2;
 				Log.out("operator : " + token);
 			} else if (dim != dim2) {
 
 				/* different oprator */
 				Log.out("different op dim=" + dim + " dim2=" + dim2);
 				different_op = true;
 				break;
 
 			}
 
 			/* read next operand */
 
 			tfes.add(read_attribute());
 
 		}
 
 		Log.out("connector closed dim:" + dim);
 		// Log.out(toks.DebugTrace());
 
 		Connector con = cg.createConnector(dim);
 
 		for (int i = 0; i < tfes.size(); i++) {
 			con.setTFE((ITFE) (tfes.get(i)));
 		}
 
 		if (token.equals("}"))
 			this.setDecoration(con);
 
 		if (different_op) {
 			Log.out("--- different connector start---");
 			toks.prevToken();
 			con = connector_main(-10000, con, closeparen);
 			Log.out("--- different connector end---");
 		}
 
 		return con;
 
 	}
 	
 	private TFE read_attribute() {
 
 		String token;
 
 		TFE out_tfe = null;
 
 		token = toks.nextToken();
 		Log.out("attribute*token=" + token);
 
 		if (token.equals("[")) {
 
 			/* grouping operand */
 			out_tfe = grouper();
 
 		} else if (token.equals("]")) {
 
 			/* error for first ']' */
 			System.err.println("*** Found ']' after Operator or Paren ***");
 			throw (new IllegalStateException());
 
 		} else if (token.equals("{")) {
 
 			/* open paren */
 			out_tfe = connector("}");
 
 		} else if (token.equals("(")) {
 			// If condition detected
 			out_tfe = read_condition(token);
 		}
 		
 		else if(token.equalsIgnoreCase("if")){
 			out_tfe = IfCondition();
 		}
 
 		else if (dimch.indexOf(token) > -1) {
 
 			/* error for operator */
 			System.err
 					.println("*** Found Operator after Operator or Paren ***");
 			throw (new IllegalStateException());
 
 		} else if (toks.lookToken().equals("(")) {
 			/* function name */
 			out_tfe = func_read(token);
 
 		} else {
 
 			Log.out("attribute=" + token);
 
 			Attribute att = makeAttribute(token);
 
 			out_tfe = att;
 
 		}
 
 		return out_tfe;
 
 	}
 
 	private TFE IfCondition() {
 		String token = "";
 		String firstTFE="";
 		String secondTFE="";
 		if(!toks.nextToken().equals("(")){
 			System.err.println("*** Syntax error in the if... then... else... condition ***");
 			throw new IllegalStateException();
 		}
 		token+=toks.nextToken();
 		boolean inParenthesis = false;
 		while((!toks.lookToken().equals(")") && !toks.lookToken().equals("")) || inParenthesis){
 			if(toks.lookToken().equals("("))
 				inParenthesis=true;
 			if(toks.lookToken().equals(")"))
 				inParenthesis=false;
 			token+=toks.nextToken();
 		}
 		if(toks.lookToken().equals("")){
 			System.err.println("*** Syntax error in the if... then... else... condition ***");
 			throw new IllegalStateException();
 		}
 		toks.nextToken();
 		Attribute condition = makeAttribute(token);
 		if(!toks.nextToken().equalsIgnoreCase("then")){
 			System.err.println("*** Syntax error in the if... then... else... condition ***");
 			throw new IllegalStateException();
 		}
 		if(!toks.nextToken().equals("(")){
 			System.err.println("*** Syntax error in the if... then... else... condition ***");
 			throw new IllegalStateException();
 		}
 		token = toks.nextToken();
 		inParenthesis=false;
 		while(!token.equalsIgnoreCase(")") || inParenthesis){
 			firstTFE+=token;
 			if(token.equals("("))
 				inParenthesis=true;
 			if(token.equals(")"))
 				inParenthesis=false;
 			token = toks.nextToken();
 
 			
 			if(token.equals("")){
 				System.err.println("*** Syntax error in the if... then... else... condition ***");
 				throw new IllegalStateException();
 			}
 		}
 		token = toks.lookToken();
 		if(token.equals("else")){
 			toks.nextToken();
 			if(!toks.nextToken().equals("(")){
 				System.err.println("*** Syntax error in the if... then... else... condition ***");
 				throw new IllegalStateException();
 			}
 			token = toks.nextToken();
 			boolean in_parenthesis = false;
 			while(!token.equalsIgnoreCase(")") || in_parenthesis){
 				secondTFE += token;
 				if(token.equals("("))
 					in_parenthesis = true;
 				else if(token.equals(")"))
 					in_parenthesis = false;
 				token= toks.nextToken();
 			}
 		}
 		
 		CodeGenerator IfCg = new CodeGenerator(cg.TFEid+1);
 		IfCg.setFactory(cg.getFactory());
 		TFEparser thenTfeParser = new TFEparser(firstTFE, IfCg, attno);
 		
 		Enumeration<Integer> enumeration = thenTfeParser.attp.keys();
 		while(enumeration.hasMoreElements()){
 			Integer key = enumeration.nextElement();
 			attp.put(key, thenTfeParser.attp.get(key));
 			attno++;
 		}
 		
 		CodeGenerator ElseCg = new CodeGenerator(IfCg.TFEid+1);
 		ElseCg.setFactory(cg.getFactory());
 		TFEparser elseTFEParser = new TFEparser(secondTFE, ElseCg, attno);
 		
 		TFE thenTfe = thenTfeParser.get_TFEschema();
 		TFE elseTfe = elseTFEParser.get_TFEschema();
 		
 		IfCondition out_tfe = makeIfCondition(condition, thenTfe, elseTfe );
 
 		enumeration = elseTFEParser.attp.keys();
 		while(enumeration.hasMoreElements()){
 			Integer key = enumeration.nextElement();
 			attp.put(key, elseTFEParser.attp.get(key));
 		}
 		return out_tfe;
 	}
 
 	private IfCondition makeIfCondition(Attribute condition, TFE thenTfe, TFE elseTfe) {
 		return cg.createIfCondition(condition, thenTfe, elseTfe);
 	}
 
 	private TFE read_condition(String token) {
 		String condition = "";
 		String if_token;
 		Log.info("[Parser::tfe] If condition detected");
 		condition= find_sql_condition();
 		if_token = toks.nextToken();
 		String[] attributes = if_token.split(":");
 		return makeConditionalAttribute(condition, attributes);
 	}
 
 	private Attribute makeAttribute(String token) {
 
 		String line;
 		String name;
 		String key = null;
 
 		int equalidx = token.indexOf('=');
 
 		if (equalidx != -1) {
 			// found key = att
 			key = token.substring(0, equalidx);
			token = token.substring(equalidx + 1);
 
 			// tk to ignore space between = and value/////////////////
 			key = key.trim();
 			// tk///////////////////
 
 			Log.out("[makeAttiribute] === Attribute Key : " + key + " ===");
 		} else {
 		}
 
 		int as_string = token.toLowerCase().indexOf(" as ");
 		if (as_string != -1) {
 			line = (String) (token.substring(0, as_string));
 			name = (String) (token.substring(as_string + 4));
 		} else {
 			line = token;
 			name = token;
 		}
 
 		// tk to ignore space between = and value/////////////////
 		line = line.trim();
 		name = name.trim();
 		att_tmp = name;
 		// tk//////////////////////////////////
 		Log.out("[makeAttribute] line : " + line);
 		Log.out("[makeAttribute] name : " + name);
 
 		Attribute att = cg.createAttribute();
 		attno = att.setItem(attno, name, line, key, attp);
 
 		this.setDecoration(att);
 
 		return att;
 
 	}
 
 	private Attribute makeConditionalAttribute(String condition,
 			String[] attributes) {
 
 		Attribute att = cg.createConditionalAttribute();
 		attno = att.setItem(attno, attributes[0], attributes[0], null, attp);
 		if (attributes.length == 2) {
 			attno = att.setItem(attno, attributes[1], attributes[1], null, attp);
 		}
 		att.setCondition(condition);
 		attno = att.setItem(attno, condition, condition,
 				null, attp);
 		this.setDecoration(att);
 
 		return att;
 	}
 
 	private Function func_read(String fn) {
 
 		String token;
 		Function fnc = cg.createFunction();
 		fnc.setFname(fn);
 		FunctionData fnd = new FunctionData(fn);
 
 		String name, value;
 
 		if (!toks.nextToken().equals("(")) {
 			System.err
 					.println("*** token '(' not Found after Function Name token ***");
 			throw (new IllegalStateException());
 		} // not start "("
 
 		Log.out("[func*read start funcname]=" + fn);
 		/* func_read */
 		TFE read_tfe = connector(")");
 
 		Log.out("[func*TFE]=" + read_tfe.makele0());
 
 		if (read_tfe instanceof Connector) {
 			// many Attribute
 			for (int i = 0; i < ((Connector) read_tfe).tfeItems; i++) {
 				fnc.setArg(makeFuncArg(((Connector) read_tfe).gettfe(i)));
 				if (fn.equals("select") && i == 0) {
 					fnc.addDeco("select", att_tmp);
 				}
 			}
 		} else {
 			// only one Attribute
 			fnc.setArg(makeFuncArg(read_tfe));
 			if (fn.equals("select")) {
 				fnc.addDeco("select", att_tmp);
 			}
 		}
 
 		this.setDecoration(fnc);
 
 		return fnc;
 
 	}
 
 	private FuncArg makeFuncArg(TFE arg) {
 		FuncArg out_fa;
 		Log.out("argsaregs: " + arg);
 
 		if (arg instanceof Attribute) {
 			out_fa = new FuncArg(((Attribute) arg).getKey(), 1, arg);
 		} else {
 			out_fa = new FuncArg("TFE", 1, arg);
 		}
 
 		return out_fa;
 	}
 
 	private void setDecoration(ITFE tfe) {
 
 		String token;
 
 		// @�ǻϤޤäƤ�?��
 		if (!toks.lookToken().equals("@"))
 			return;
 		toks.nextToken();
 
 		// ���μ��� {
 		if (!toks.nextToken().equals("{")) {
 			System.err
 					.println("*** Illegal Token Found after Decoration token '@' ***");
 			throw (new IllegalStateException());
 		}
 
 		// hanki start
 		if (fromPreprocessorOnly(tfe)) {
 			return;
 		}
 		// hanki end
 
 		Log.out("@ decoration found @");
 		// Log.out(toks.DebugTrace());
 		
 		String name, value;
 		int equalidx;
 
 		while (toks.hasMoreTokens()) {
 			
 			
 			name = new String();
 			value = new String();
 
 			// read name
 			token = toks.nextToken();
 			
 			Log.out("decoration*token=" + token);
 			
 
 			if(token.equals("(")){
 				read_conditional_decoration(tfe);
 				token= toks.nextToken();
 				if(token.equals(",")){
 					token=toks.nextToken();
 				}
 			}
 			if (token.equals("}")) {
 				break;
 			}
 			equalidx = token.indexOf('=');
 			if (equalidx != -1) {
 				// key = idx
 				name = token.substring(0, equalidx);
 				value = token.substring(equalidx + 1);
 				while (toks.hasMoreTokens()) {
 					token = toks.lookToken();
 					Log.out("decoration*looktoken=" + token);
 					if (token.equals(",") || token.equals("}")) {
 						break;
 					}
 					value = value.concat(toks.nextToken());
 				}
 				decoration_out(tfe, name, value);
 			} else {
 				// key only
 				decoration_out(tfe, token, "");
 			}
 
 			token = toks.nextToken();
 			Log.out("decoration*token=" + token);
 			if (token.equals("}")) {
 				// end of decoration
 				break;
 			}
 			if (!token.equals(",")) {
 				// not close in "}"
 				System.err
 						.println("*** Found Illegal Token after Decoration value ***");
 				throw (new IllegalStateException());
 			}
 		}
 		Log.out("@ decoration end @");
 		// Log.out(toks.DebugTrace());
 
 	}
 
 	private void read_conditional_decoration(ITFE tfe) {
 		String token;
 		String condition = find_sql_condition();
 		token= toks.prevToken();
 		if(token.equals("?")){
 			toks.nextToken();
 			token="";
 			while(!toks.lookToken().contains(":") && !toks.lookToken().equals("}"))
 				token += toks.nextToken();
 			if(!toks.lookToken().equals("}"))
 				token+=toks.nextToken();
 			if(toks.lookToken().equals("(")){
 				toks.nextToken();
 				while(!toks.lookToken().equals(")")){
 					if(toks.lookToken().equals("")){
 						System.err.println("*** Syntax error in decoration condition of "+condition+" ***");
 						throw new IllegalStateException();
 					}
 					token+=toks.nextToken();
 				}
 				toks.nextToken();
 			}
 			try{
 				for(int i = 0; i< token.split(":")[0].split(",").length; i++){
 					String name1= token.split(":")[0].split(",")[i].split("=")[0];
 					String value1= token.split(":")[0].split(",")[i].split("=")[1];
 					decoration_out(tfe, name1, value1, condition);
 				}
 				try{
 					for(int i = 0; i< token.split(":")[1].split(",").length; i++){
 						String name2 = token.split(":")[1].split(",")[i].split("=")[0];
 						String value2 = token.split(":")[1].split(",")[i].split("=")[1];
 						decoration_out(tfe,name2,value2,"!"+condition);
 					}
 				}
 				catch(Exception e){
 					
 				}
 				attno = ((Attribute) tfe).setItem(attno, condition, condition, null, attp);
 				
 			}
 			catch(ArrayIndexOutOfBoundsException e){
 					System.err.println("*** Found Illegal Token after Decoration condition ***");
 					throw new IllegalStateException();
 				}
 		}
 		else{
 			System.err.println("*** Found Illegal Token after Decoration condition ***"	);
 			throw new IllegalStateException();
 		}
 		
 	}
 
 	private void decoration_out(ITFE tfe, String name, String value,
 			String condition) {
 		tfe.addDeco(name, value, condition);
 		Log.out("[decoration name=" + name + " value=" + value + " condition="+condition+"]");
 		
 	}
 
 	private String find_sql_condition() {
 		String token = toks.nextToken();
 		String condition="";
 		boolean in_parenthesis = false;
 		while (!token.equals(")") || in_parenthesis) {
 				
 			condition += token;
 			if(token.equals("("))
 				in_parenthesis = true;
 			else if (token.equals(")"))
 				in_parenthesis = false;
 			token = toks.nextToken();
 
 
 			if (token.equals("")) {
 				System.err.println("***End of if statement not found ***");
 				throw (new IllegalStateException());
 			}
 		}
 		token = toks.nextToken();
 		while (!token.equals("?")) {
 			token = toks.nextToken();
 			if (token.equals("")) {
 				System.err.println("***Syntax error in if statement ***");
 				throw (new IllegalStateException());
 			}
 		}
 		return condition;
 	}
 
 	// hanki start
 	private boolean fromPreprocessorOnly(ITFE tfe) {
 
 		String token;
 
 		do {
 
 			/* "order by" found */
 			if (toks.lookToken().toLowerCase().indexOf("asc") > -1
 					|| toks.lookToken().toLowerCase().indexOf("desc") > -1) {
 
 				Log.out("@ order by found @");
 				token = toks.nextToken();
 
 				tfe.setOrderBy(token);
 
 				/* decoration doesn't exist originally */
 				if (toks.lookToken().equals("}")) {
 					toks.nextToken(); // skip "}"
 					return true;
 					/* decoration exists originally */
 				} else if (toks.lookToken().equals(",")) {
 					toks.nextToken(); // skip ","
 				}
 
             /* "aggregate functions" found */
             } else if (toks.lookToken().equalsIgnoreCase("max") ||
     				   toks.lookToken().equalsIgnoreCase("min") ||
     				   toks.lookToken().equalsIgnoreCase("avg") ||
     				   toks.lookToken().equalsIgnoreCase("sum") ||
             		   toks.lookToken().equalsIgnoreCase("count") /*||
             		   //added by goto 20130122
             		   toks.lookToken().equalsIgnoreCase("slideshow")*/) {
 
 
 				Log.out("@ aggregate functions found @");
 				token = toks.nextToken();
 
 				tfe.setAggregate(token);
 
 				/* decoration doesn't exist originally */
 				if (toks.lookToken().equals("}")) {
 					toks.nextToken(); // skip "}"
 					return true;
 					/* decoration exists originally */
 				} else if (toks.lookToken().equals(",")) {
 					toks.nextToken(); // skip ","
 				}
 
 				/* neither "order by" nor "aggregate functions" found */
 			} else {
 				return false;
 			}
 
     	} while (toks.lookToken().toLowerCase().indexOf("asc") > -1 ||
     			 toks.lookToken().toLowerCase().indexOf("desc") > -1 ||
     			 toks.lookToken().equalsIgnoreCase("max") ||
 				 toks.lookToken().equalsIgnoreCase("min") ||
 				 toks.lookToken().equalsIgnoreCase("avg") ||
 				 toks.lookToken().equalsIgnoreCase("sum") ||
 				 toks.lookToken().equalsIgnoreCase("count") /*||
 				 //added by goto 20130122
 				 toks.lookToken().equalsIgnoreCase("slideshow")*/);
     	
 
 		return false;
 
     }
     //hanki end
     /*
      * // ?ʲ // private Function deco_read(String fn) { private void
      * deco_read(TFE tfe) { // ǽ餬@ǻϤޤäƤʤ??ʤ if
      * (!toks.lookToken().equals("@")) return; toks.nextToken();
      *
      * Log.out("@@ start read decoration @@");
      *
      * String token; Function fnc = cg.createFunction(); fnc.setFname(fn);
      * FunctionData fnd = new FunctionData(fn);
      *
      * if (!toks.nextToken().equals("{")) { Exception e = new Exception(); } //
      * not start "{" // deco_read TFE read_tfe = connector();
      * Log.out("[decoration]=" + read_tfe.makele0());
      *
      * if (read_tfe instanceof Connector) { // many Attribute for (int i = 0; i <
      * ((Connector) read_tfe).tfeitems; i++) {
      * fnc.setArg(makeFuncArg(((Connector) read_tfe).gettfe(i))); } } else { //
      * only one Attribute fnc.setArg(makeFuncArg(read_tfe)); }
      *
      * this.setDecoration(fnc); return; // return fnc; }
      */
 
     private void decoration_out(ITFE tfe, String name, Object value) {
 
         /* ?ŪStringɤʤ */
         tfe.addDeco(name, (String) value);
         Log.out("[decoration name=" + name + " value=" + value + "]");
 
 	}
 
 	public TFE get_TFEschema() {
 		return schemaTop;
 	}
 
 	public void debugout() {
 
 		Log.out("========================================");
 		Log.out("  output Schema Tree");
 		Log.out("========================================");
 		schemaTop.debugout(0);
 	}
 
 	public Hashtable get_attp() {
 		return this.attp;
 	}
 
 }
