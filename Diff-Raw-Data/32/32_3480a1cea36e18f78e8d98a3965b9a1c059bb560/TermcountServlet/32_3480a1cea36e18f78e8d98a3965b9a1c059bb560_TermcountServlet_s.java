 package edu.wpi.termcount;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.*;
 
 import java_cup.runtime.Symbol;
 import kodkod.ast.Formula;
 import kodkod.ast.LeafExpression;
 import kodkod.ast.Relation;
 
 class FormulaParserException extends Exception
 {	
 	int row;
 	int col;
 	Object errorValue;
 	
 	FormulaParserException(int row, int col, Object val)
 	{
 		this.row = row;
 		this.col = col;
 		errorValue = val;
 	}
 }
 
 public class TermcountServlet extends HttpServlet
 {
 	public void init() {
 		// Initialization routing, comparable to main()
 
 		/*
 		 * String str = getOutput("A\nB\nC\n\n", "A B\nB C", "c A\n",
 		 * "forsome x : A | x in P", true); System.out.println(str);
 		 * 
 		 * try { FormulaSigInfo.unitTests(); } catch(Exception e) {
 		 * System.err.println(e); }
 		 */
 	}
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
 				+ "Transitional//EN\">\n";
 		
 		String theFormula = request.getParameter("formula");
 		
 		String output = "";
 		try
 		{
 						
 			output = getOutput(request.getParameter("sorts"), 
 				request.getParameter("ordering"),
 				request.getParameter("functions"), 
 				theFormula);
 			
 			//output = request.getParameter("sorts") + "<BR>\n"+ 
 			//	request.getParameter("ordering")  + "<BR>\n"+
 			//	request.getParameter("functions")  + "<BR>\n"+
 			//	request.getParameter("formula") + "<BR>\n";
 
 		}
 		catch(FormulaLexerException e)
 		{				
 			String[] formulaLines = theFormula.split("\n\r|\r\n|\r|\n");	
 			
 			String charDesc = "";
 			if(e.at.length() == 0)
 				charDesc = "No character was found for this token.";
 			else
 			{
 				int charVal = e.at.charAt(0);
 				charDesc = "The first troublesome character was: '"+
 				           e.at.charAt(0)+"' (ASCII "+ charVal + ").";
 			}
 								
 			output = "Invalid lexical token in formula line "+e.row+", column "+
 			         e.col+". "+ charDesc +"<BR><BR>\n";
 			output += "Line "+e.row+" was:<BR>\n";
 			output += "<CODE>"+ formulaLines[e.row] + "<BR>\n";
 			
 			String indicatorString = "";
 			for(int ii = 0;ii<e.col;ii++)
 				indicatorString += "&nbsp;";
 			indicatorString += "<U><B>^</B></U></CODE>";
 			output += indicatorString;				
 		}
 		catch(FormulaParserException e)
 		{
 			// Focus on the correct line!
 			// May be separated by either CRs or LFs (or both at once! don't double-count)
			String[] formulaLines = theFormula.split("\n\r|\r\n|\r|\n");					
 			
			output = "Error parsing formula at row "+e.row+", column "+e.col+
			         ". The token was: "+e.errorValue+"<BR><BR>\n";
			output += "Line "+e.row+" was:<BR>\n";
			output += "<CODE>"+ formulaLines[e.row] + "<BR>\n";
			
			String indicatorString = "";
			for(int ii = 0;ii<e.col;ii++)
 				indicatorString += "&nbsp;";
			indicatorString += "<U><B>^</B></U></CODE>";
			output += indicatorString;
 		}
 		catch(NotASortException e)
 		{
 			// "The relation A appeared as a sort symbol but..."
 			output = e.getMessage();
 		}
 		catch(UnsupportedFormulaException e)
 		{
 			// "Formula: <f> uses a sort symbol in place of a predicate, which is not supported."
 			output = e.getMessage();			
 		}
 		catch(Exception e)
 		{			
 							
 			// ... other exceptions:	
 			output = "Error encountered: <BR>\n"+e.toString();
 		}
 		
 		out.println(docType
 				+ "<HTML>\n"
 				+ "<HEAD><TITLE>Results</TITLE></HEAD>\n"
 				+ "<BODY BGCOLOR=\"#FDF5E6\">\n"
 								
 				+ output
 						
 				+ "</BODY></HTML>");
 
 	}
 
 	public String getOutput(String sortsField, String orderingField,
 			String functionsField, String formulaField)
 	throws Exception // parser doesn't specify
 	{
 
 		// Form may pass in a blank line at the end. 
 		sortsField = sortsField.trim();
 		orderingField = orderingField.trim();
 		functionsField = functionsField.trim();
 		formulaField = formulaField.trim();
 		
 		String[] sortsArray = sortsField.split("\n");				
 		String[] orderingArray = orderingField.split("\n");			
 		String[] functionsArray = functionsField.split("\n");
 		
 			// Parse
 			// ******************************
 			Reader reader = new StringReader(formulaField);
 			TermcountLexer theLexer = new TermcountLexer(reader);
 			parser theParser = new parser(theLexer);
 
 			//System.err.println("Formula field: "+formulaField);
 
 			
 			Symbol result = theParser.parse();
 				
 			
 			if (!(result.value instanceof Formula))
 				return formulaField + " was not a formula.";
 			Formula fmla = (Formula) result.value;
 			
 			// System.out.println(fmla);
 
 			// NNF
 			fmla = fmla.accept(new NNFConverterV());
 
 			// System.out.println(fmla);
 
 			// Build maps for FormulaSigInfo
 			// ******************************
 			Set<LeafExpression> sorts = new HashSet<LeafExpression>();
 			for (String sortname : sortsArray)
 			{
 				sortname = sortname.trim();
 				
 				if(sortname.length() < 1)
 					continue;
 				
 				// Is this a valid sort name?
 				if(!sortname.matches("[A-Z][A-Z0-9]*"))
 					return "Invalid sort name: "+sortname;
 				
 				sorts.add(MFormulaManager.makeRelation(sortname.trim(), 1));
 			}
 
 			
 			// SORTS
 			Map<LeafExpression, Set<LeafExpression>> supers = new HashMap<LeafExpression, Set<LeafExpression>>();
 			String prettySorts = "";
 			boolean first = true;
 			for (LeafExpression s : sorts)
 			{				
 				supers.put(s, new HashSet<LeafExpression>());
 				
 				if(first)
 				{
 					prettySorts += s.name();
 					first = false;
 				}
 				else
 					prettySorts += ", " + s.name();
 				
 			}
 
 			
 			// SORT ORDERING
 			// must be transitive (even if user gave only the skeleton)
 			String prettyOrdering = "";
 			first = true;
 			if (orderingField.length() > 1)
 				for (String orderpair : orderingArray)
 				{
 					String[] thePair = orderpair.split("\\s");
 					
 					if(thePair.length != 2)
 						continue;
 					
 					if(first)
 					{
 						prettyOrdering += "("+thePair[0] + " < " + thePair[1]+")";
 						first = false;
 					}
 					else
 						prettyOrdering += ", (" + thePair[0] + " < " + thePair[1] +")";
 					
 					
 					Relation lessThan = MFormulaManager.makeRelation(
 							thePair[0], 1);
 					Relation greaterThan = MFormulaManager.makeRelation(
 							thePair[1], 1);
 					
 					// Can only use declared sorts.
 					if(!sorts.contains(lessThan))
 						return "Error: Sort "+lessThan+" was undeclared, but used in ordering: "+orderpair+".";
 					if(!sorts.contains(greaterThan))
 						return "Error: Sort "+greaterThan+" was undeclared, but used in ordering: "+orderpair+".";
 															
 					if(!supers.containsKey(lessThan))
 						supers.put(lessThan, new HashSet<LeafExpression>());
 					supers.get(lessThan).add(greaterThan);
 
 					// Make sure this is reflected in the rest of the ordering.
 					for (LeafExpression possibleLessThan : supers.keySet()) {
 						// If possibleLessThan < lessThan, we need to propagate
 						// this new <.
 						if (supers.get(possibleLessThan).contains(lessThan))
 							supers.get(possibleLessThan).add(greaterThan);
 					}
 
 				}
 
 			// PREDICATE SYMBOLS: everything used is ok
 			Map<LeafExpression, java.util.List<LeafExpression>> preds = new HashMap<LeafExpression, java.util.List<LeafExpression>>();
 			for (Relation rel : theParser.relations)
 			{
 				if (sorts.contains(rel))
 					continue;
 
 				// Don't care if it's well-formed. Arity may be off.
 				java.util.List<LeafExpression> theList = new ArrayList<LeafExpression>();
 
 				preds.put(rel, theList);
 			}
 			
 
 			// Pass in non-Skolem functions/constants
 			// **************************************
 			Set<SigFunction> origFunctions = new HashSet<SigFunction>();
 			Set<SigFunction> origConstants = new HashSet<SigFunction>();
 			if (functionsField.length() > 1) {
 				for (String funcsig : functionsArray) {
 
 					String[] theFunc = funcsig.split("\\s");
 
 					if(funcsig.trim().length() == 0)
 						continue; // empty line					
 					
 					if(theFunc.length < 2) // invalid non-empty line
 						return "Invalid function (or constant) declaration: <B>"+funcsig+
 						"</B>. Did you remember to provide the function's signature?<BR>\n"+
 						"For example, f: (A x B) -> C should be given as: f A B C<BR>\n";
 											
 					String n = theFunc[0];
 					
 					if(!n.matches("[a-z]+"))
 						return "Invalid function (or constant) name: "+n;
 					
 					// safe since we know the length >= 2
 					Relation r = MFormulaManager.makeRelation(
 							theFunc[theFunc.length - 1], 1);
 					SigFunction f = new SigFunction(n, r, false);
 
 					// arity
 					for (int iIndex = 1; iIndex < theFunc.length - 1; iIndex++)
 						f.arity.add(MFormulaManager.makeRelation(
 								theFunc[iIndex], 1));
 
 					if (f.arity.size() == 0)
 						origConstants.add(f);
 					else
 						origFunctions.add(f);
 				}
 			}
 			
 			// Create info object and get bounds
 			// *********************************
 			FormulaSigInfo info = new FormulaSigInfo(sorts, supers, preds,
 					origFunctions, origConstants, fmla, FormulaSigInfo.EnumSAPHandling.sapKeep,
 					true);
 
 			
 			String resultString = "<B>Original formula was:</B> "+formulaField+ "<BR>\n"
 			+ "<B>Over the following sorts: </B> " + prettySorts + "<BR>\n" 
 			+ "<B>With the following ordering: </B> "+ prettyOrdering + "<BR><BR>\n" 	
 			+ "<B>The pre-Skolem signature had the following functions and constants:</B> <BR>\n"+
 			prettySigFunctions(origFunctions, origConstants) 			
 			+ "<BR>";
 			return resultString + info.getInfoString();
 	}
 	
 	String prettySigFunctions(Set<SigFunction> origFunctions, Set<SigFunction> origConstants)
 	{
 		String result = "";
 		
 		for(SigFunction c : origConstants)		
 			result += c.toPrettyString() + "<BR>";		
 		
 		for(SigFunction f : origFunctions)
 			result += f.toPrettyString() + "<BR>";
 		
 		if(result.length() == 0)
 			return "(None!)<BR>";
 		return result;
 	}
 	
 }
