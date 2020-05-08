 package sdf.util;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import sdf.ruleannotations.CustomATermAnnotation;
 
 import de.tud.stg.parlex.core.*;
 import de.tud.stg.parlex.core.groupcategories.*;
 import de.tud.stg.parlex.core.ruleannotations.*;
 
 /**
  * Outputs an HTML file listing all the categories and rules in the grammar (for debug purposes).
  * Both categories and rules are sorted alphabetically and categories are linked to the corresponding rules.
  * 
  * @author Pablo Hoch
  * @see de.tud.stg.parlex.core.IGrammar
  *
  */
 public class GrammarDebugPrinter {
 
 	private HashMap<ICategory<String>, String> catIds;
 	private HashMap<String, ArrayList<IRule<String>>> rulesByLhs;
 	private IGrammar<String> grammar;
 	private PrintStream out;
 	private ArrayList<ICategory<String>> catList;
 	private ArrayList<ICategory<String>> missingCatList;
 	
 	public GrammarDebugPrinter(IGrammar<String> grammar, OutputStream outStream) {
 		this.grammar = grammar;
 		this.out = new PrintStream(outStream);
 		
 		analyzeGrammar();
 	}
 	
 	private void analyzeGrammar() {
 		catIds = new HashMap<ICategory<String>, String>();
 		rulesByLhs = new HashMap<String, ArrayList<IRule<String>>>();
 		catList = new ArrayList<ICategory<String>>(grammar.getCategories());
 		missingCatList = new ArrayList<ICategory<String>>();
 		
 		int nextCatId = 1;
 		
 		Collections.sort(catList, new CategoryComparator());
 		for (ICategory<String> cat : catList) {
 			String catId = "cat_" + (nextCatId++);
 			catIds.put(cat, catId);
 		}
 		
 		// sort rules
 		for (IRule<String> rule : grammar.getRules()) {
 			ICategory<String> lhsCat = rule.getLhs();
 			String lhsCatId = catIds.get(lhsCat);
 			if (lhsCatId == null) {
 				// undeclared category!
 				String catId = "cat_" + (nextCatId++);
 				catIds.put(lhsCat, catId);
 				lhsCatId = catId;
 				missingCatList.add(lhsCat);
 			}
 			ArrayList<IRule<String>> ruleList = rulesByLhs.get(lhsCatId);
 			if (ruleList == null) {
 				ruleList = new ArrayList<IRule<String>>();
 				rulesByLhs.put(lhsCatId, ruleList);
 			}
 			ruleList.add(rule);
 		}
 	}
 	
 	public void printGrammar() {
 		printGrammar("Grammar");
 	}
 
 	public void printGrammar(String title) {
 
 		// html header
 		out.println("<!DOCTYPE HTML>");
 		out.println("<html><head>");
 		out.println("<title>" + escape(title) + "</title>");
 		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
 		out.println("<style type=\"text/css\">");
 		out.println("body {font-family: Arial, Helvetica, sans-serif;}");
 		out.println("li {font-family: Menlo, Consolas, monospace; font-size: 10pt;}");
 		out.println(".missing, .missing a {color: red;}");
 		out.println(".inline {background: #eee;}");
 		out.println(".inline .inline {background: #ddd;}");
 		out.println(".inline .inline .inline {background: #ccc;}");
 		out.println(".annotations {background: #FFE491;}");
 		out.println("</style>");
 		out.println("</head><body>");
 		
 		out.println("<h1>" + escape(title) + "</h1>");
 		out.println("<ul>");
 		out.println("<li><a href=\"#categories\">Categories</a></li>");
 		out.println("<li><a href=\"#rules\">Rules</a></li>");
 		out.println("</ul>");
 
 		// categories
 		
 		out.println("<h2 id=\"categories\">Categories</h2>");
 		out.println("<ul>");
 		for (ICategory<String> cat : catList) {
 			String catId = catIds.get(cat);
 			out.print("<li id=\"" + catId + "\">");
 			printRhsCategory(cat);
 			out.println("</li>");
 		}
 		
 		for (ICategory<String> cat : missingCatList) {
 			String catId = catIds.get(cat);
 			out.print("<li id=\"" + catId + "\" class=\"missing\">");
 			printRhsCategory(cat);
 			out.println("</li>");
 		}
 		
 		out.println("</ul>");
 		
 		
 		// rules
 		
 		IRule<String> startRule = grammar.getStartRule();
 		
 		ArrayList<IRule<String>> ruleList = new ArrayList<IRule<String>>(grammar.getRules());
 		Collections.sort(ruleList, new RuleComparator());
 		
 		out.println("<h2 id=\"rules\">Rules</h2>");
 		out.println("<ul>");
 		
 		// start rule
 		
 		{
 			if (ruleList.contains(startRule)) {
 				out.print("<li><a href=\"#start_rule\"><b>Start rule:</b></a> <b>");
 			} else {
 				out.print("<li><b class=\"missing\">Start rule:</b> <b>");
 			}
 			ICategory<String> lhsCat = startRule.getLhs();
 			out.print(escape(catString(lhsCat)) + "</b> -&gt; ");
 			for (ICategory<String> cat : startRule.getRhs()) {
 				printRhsCategory(cat);
 			}
 		}
 		
 		// rule list
 		
 		String lastCatId = null;
 		int ruleId = 1;
 		
 		for (IRule<String> irule : ruleList) {
 			Rule rule = (Rule)irule;
 			ICategory<String> lhsCat = rule.getLhs();
 			String catId = catIds.get(lhsCat);
 			
 			if (lastCatId == null || !lastCatId.equals(catId)) {
 				ruleId = 1;
 			} else {
 				ruleId++;
 			}
 			lastCatId = catId;
 			
 			out.print("<li id=\"" + catId + "_rule" + ruleId + "\">");
 			if (rule.equals(startRule)) {
 				out.print("<b id=\"start_rule\">");
 			} else {
 				out.print("<b>");
 			}
 			// lhs
 			out.print(escape(catString(lhsCat)) + "</b> -&gt; ");
 			// rhs
 			for (ICategory<String> cat : rule.getRhs()) {
 				printRhsCategory(cat);
 			}
 			// annotations
 			printRuleAnnotations(rule);
 			out.println("</li>");
 			ruleId++;
 		}
 
 		out.println("</ul>");
 		
 		out.println("</body></html>");
 	}
 
 	private void printRhsCategory(ICategory<String> cat) {
 		String str = escape(catString(cat));
 		if (cat.isTerminal()) {
 			out.print(str + " ");
 		} else {
 			String linkedCatId = catIds.get(cat);
 			boolean linkedCatHasRules = rulesByLhs.containsKey(linkedCatId);
 			if (linkedCatHasRules) {
 				ArrayList<IRule<String>> linkedCatRules = rulesByLhs.get(linkedCatId);
 				out.print("<a href=\"#" + linkedCatId + "_rule1\">" + str + "</a> ");
 				if (linkedCatRules.size() == 1 && linkedCatRules.get(0).getRhs().size() == 1) {
 					out.print("<span class=\"inline\">( -&gt; ");
 					printRhsCategory(linkedCatRules.get(0).getRhs().get(0));
 					out.print(")</span> ");
 				}
 			} else {
 				
 				out.print("<a href=\"#" + linkedCatId + "_rule1\" class=\"missing\">" + str + "</a> ");
 			}
 		}
 	}
 	
 	private void printRuleAnnotations(Rule rule) {
 		List<IRuleAnnotation> annotations = rule.getAnnotations();
 		
 		if (annotations != null && !annotations.isEmpty()) {
 			out.print(" <div class=\"annotations\">{ ");
 			
 			for (Iterator<IRuleAnnotation> it = annotations.iterator(); it.hasNext(); ) {
 				IRuleAnnotation ann = it.next();
 				out.print(annotationString(ann));
 				if (it.hasNext())
 					out.print(", ");
 			}
 			
 			out.print(" }</div>");
 		}
 	}
 	
 	private String catString(ICategory<String> cat) {
 		if (cat.isTerminal()) {
 			if (cat instanceof WaterCategory) {
 				return "$water$";
 			} else if (cat instanceof IntegerCategory) {
 				return "$integer$";
 			} else if (cat instanceof DoubleCategory) {
 				return "$double$";
 			} else if (cat instanceof ClassCategory) {
 				return "$class$";
 			} else if (cat instanceof StringCategory) {
 				return "string(" + cat.getName() + ")";
 			} else if (cat instanceof CustomCategory) {
 				// TODO: class is unfortunately private
 				return "custom(" + cat.getName() + ")";
 			} else if (cat.getClass() == Category.class) {
 				return "\"" + cat.getName() + "\"";
 			} else {
 				return cat.getClass().getName() + "(" + cat.getName() + ")";
 			}
 		} else {
 			// non terminal
 			return "<" + cat.getName() + ">";
 		}
 	}
 	
 	private String annotationString(IRuleAnnotation ann) {
 		if (ann instanceof AbsolutePriorityAnnotation) {
 			return "AbsolutePriority(" + ((AbsolutePriorityAnnotation)ann).getPriority() + ")";
 		} else if (ann instanceof AssociativityAnnotation) {
 			return "Associativity(" + ((AssociativityAnnotation)ann).getAssociativity().toString() + ")";
 		} else if (ann instanceof AvoidAnnotation) {
 			return "Avoid";
 		} else if (ann instanceof PreferAnnotation) {
 			return "Prefer";
 		} else if (ann instanceof RejectAnnotation) {
 			return "Reject";
 		} else if (ann instanceof RelativePriorityAnnotation) {
 			RelativePriorityAnnotation rpa = (RelativePriorityAnnotation)ann;
 			Set<IRule<String>> lowerPriorityRules = rpa.getLowerPriorityRules();
 			StringBuilder sb = new StringBuilder();
 			sb.append("RelativePriority(higher priority than: ");
 			for (Iterator<IRule<String>> it = lowerPriorityRules.iterator(); it.hasNext(); ) {
 				IRule<String> rule = it.next();
				sb.append("<span class=\"inline\">");
 				
 				// print rule
 				String lhsCatId = catIds.get(rule.getLhs());
				out.print("<a href=\"#" + lhsCatId + "_rule1\">" + escape(catString(rule.getLhs())) + "</a> -&gt; ");
 				for (ICategory<String> rhsCat : rule.getRhs()) {
					printRhsCategory(rhsCat);
 				}
 				
				sb.append("</span> ");
 				if (it.hasNext())
 					sb.append(", ");
 			}
 			sb.append(")");
 			return sb.toString();
 		} else if (ann instanceof CustomATermAnnotation) {
 			return "CustomATerm( " + ((CustomATermAnnotation)ann).getAterm().toString() + " )";
 		} else {
 			return ann.toString();
 		}
 	}
 	
 	private String escape(String s) {
 		return s.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll(" ", "&nbsp;");
 	}
 	
 	private class CategoryComparator implements Comparator<ICategory<String>> {
 
 		@Override
 		public int compare(ICategory<String> o1, ICategory<String> o2) {
 			return catString(o1).compareTo(catString(o2));
 		}
 		
 	}
 	
 	private class RuleComparator implements Comparator<IRule<String>> {
 
 		@Override
 		public int compare(IRule<String> o1, IRule<String> o2) {
 			int result = catString(o1.getLhs()).compareTo(catString(o2.getLhs()));
 			if (result == 0) {
 				// lhs equal, compare rhs
 				result = o1.toString().compareTo(o2.toString());
 			}
 			return result;
 		}
 		
 	}
 }
