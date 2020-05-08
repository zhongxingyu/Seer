 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-04-04
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis;
 
import eu.serscis.sam.lexer.LexerException;
 import org.deri.iris.api.terms.IConcreteTerm;
 import eu.serscis.sam.parser.ParserException;
 import java.io.BufferedReader;
 import java.util.HashSet;
 import java.util.Set;
 import eu.serscis.sam.node.*;
 import java.io.StringReader;
 import java.io.PushbackReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.io.FileWriter;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.FileReader;
 import java.io.Reader;
 import java.io.IOException;
 import org.deri.iris.Configuration;
 import org.deri.iris.KnowledgeBaseFactory;
 import org.deri.iris.api.IKnowledgeBase;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.basics.IAtom;
 import org.deri.iris.api.terms.IVariable;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.rules.IRuleSafetyProcessor;
 import org.deri.iris.RuleUnsafeException;
 import org.deri.iris.compiler.BuiltinRegister;
 import static org.deri.iris.factory.Factory.*;
 import eu.serscis.sam.lexer.Lexer;
 import eu.serscis.sam.parser.Parser;
 import static eu.serscis.Constants.*;
 
 public class SAMParser {
 	public List<IQuery> queries = new LinkedList<IQuery>();
 	private Model model;
 	private File dir, myPath;
 	private static int assertions = 0;
 
 	public SAMParser(Model model, File path) throws Exception {
 		this.model = model;
 		dir = path.getParentFile();
 		myPath = path;
 
 		FileReader reader = new FileReader(path);
 		try {
 			parse(reader);
		} catch (LexerException ex) {
			String msg = ex.getMessage();
			System.out.println("\nLexing error: " + ex.getMessage());
			int lbracket = msg.indexOf('[');
			int rbracket = msg.indexOf(']', lbracket);
			String[] loc = msg.substring(lbracket + 1, rbracket).split(",");
			int line = Integer.valueOf(loc[0]);
			int col = Integer.valueOf(loc[1]);

			System.out.println(getLine(path, line));
			String spaces = "";
			for (int i = col; i > 1; i--) {
				spaces += " ";
			}
			System.out.println(spaces + "^");
			System.out.println(path + ":" + line);
			System.exit(1);
		} catch (ParserException ex) {
 			Token t = ex.getToken();
 			System.out.println("\nParsing error: " + ex.getMessage());
 			System.out.println(getLine(path, t.getLine()));
 			String spaces = "";
 			for (int i = t.getPos(); i > 1; i--) {
 				spaces += " ";
 			}
 			System.out.println(spaces + "^");
 			System.out.println(path + ":" + t.getLine());
 			System.exit(1);
 		} finally {
 			reader.close();
 		}
 	}
 
 	public SAMParser(Model model, Reader source) throws Exception {
 		this.model = model;
 		parse(source);
 	}
 
 	private void parse(Reader source) throws Exception {
 		Map<String,SAMClass> classDefinitions = new HashMap<String,SAMClass>();
 
 		PushbackReader pbr = new PushbackReader(source);
 		Lexer lexer = new Lexer(pbr);
 		Parser parser = new Parser(lexer);
 		Start start = parser.parse();
 
 		for (PToplevel toplevel : ((AProgram) start.getPProgram()).getToplevel()) {
 			//System.out.println("Processing " + toplevel);
 
 			if (toplevel instanceof ABehaviourToplevel) {
 				SAMClass klass = new SAMDeclaredClass(model, (ABehaviour) ((ABehaviourToplevel) toplevel).getBehaviour());
 				if (classDefinitions.containsKey(klass.name)) {
 					throw new RuntimeException("Duplicate class definition: " + klass.name);
 				}
 				classDefinitions.put(klass.name, klass);
 			} else if (toplevel instanceof AFactToplevel) {
 				addFact((AFact) ((AFactToplevel) toplevel).getFact());
 			} else if (toplevel instanceof ARuleToplevel) {
 				addRule((ARule) ((ARuleToplevel) toplevel).getRule());
 			} else if (toplevel instanceof AQueryToplevel) {
 				addQuery((AQuery) ((AQueryToplevel) toplevel).getQuery());
 			} else if (toplevel instanceof ADeclareToplevel) {
 				addDeclare((ADeclare) ((ADeclareToplevel) toplevel).getDeclare());
 			} else if (toplevel instanceof AAssertToplevel) {
 				addAssert((AAssert) ((AAssertToplevel) toplevel).getAssert());
 			} else if (toplevel instanceof AImportToplevel) {
 				addImport((AImport) ((AImportToplevel) toplevel).getImport());
 			} else if (toplevel instanceof AConfigToplevel) {
 				SAMClass klass = processConfig((AConfig) ((AConfigToplevel) toplevel).getConfig());
 				if (classDefinitions.containsKey(klass.name)) {
 					throw new RuntimeException("Duplicate class definition: " + klass.name);
 				}
 				classDefinitions.put(klass.name, klass);
 			} else {
 				throw new RuntimeException("UNKNOWN " + toplevel + ", " + toplevel.getClass());
 			}
 		}
 
 		for (SAMClass klass : classDefinitions.values()) {
 			klass.addDatalog();
 		}
 	}
 
 	public List<IQuery> getQueries() {
 		return queries;
 	}
 
 
 	private void addFact(AFact fact) throws ParserException {
 		IAtom atom = model.parseAtom(fact.getAtom());
 
 		//System.out.println(" --> " + atom);
 
 		IRelation rel = model.getRelation(atom.getPredicate());
 		rel.add(atom.getTuple());
 	}
 
 	private void addRule(ARule rule) throws ParserException {
 		ILiteral head = BASIC.createLiteral(true, model.parseAtom(rule.getHead()));
 
 		List<ILiteral> body = model.parseLiterals((ALiterals) rule.getBody(), null);
 
 		IRule r = BASIC.createRule(makeList(head), body);
 		//System.out.println(" --> " + r);
 
 		model.rules.add(r);
 	}
 
 	private void addQuery(AQuery query) throws ParserException {
 		List<ILiteral> literals = model.parseLiterals((ALiterals) query.getLiterals(), null);
 
 		IQuery q = BASIC.createQuery(literals);
 
 		queries.add(q);
 	}
 
 	private void addAssert(AAssert ass) throws ParserException {
 		List<ILiteral> body = model.parseLiterals((ALiterals) ass.getLiterals(), null);
 
 		if (body.size() != 1) {
 			throw new ParserException(ass.getAssertTok(), "Assert must contain exactly one goal");
 		}
 		ILiteral bodyLit = body.get(0);
 
 		assertions += 1;
 		ITerm assertionN = CONCRETE.createInteger(assertions);
 
 		// If body contains a single literal that refers to two objects,
 		// add a suitable red arrow to the graph
 		//
 		// For positive assertions, the object terms must be literals:
 		//
 		//   assert didCall("a", "b", ?M)
 		// becomes
 		//   assertionArrow("a", "b", false) :- !didCall("a", "b", ?M)
 		//
 		// For negative assertions:
 		//
 		//   assert !didCall(?X, "b", ?M).
 		// becomes
 		//   assertionArrow(?X, "b", true) :- didCall(?X, "b", ?M).
 		int nObjectTerms = 0;
 		ITerm[] objectTerms = new ITerm[2];
 
 		IPredicate pred = bodyLit.getAtom().getPredicate();
 		boolean positive = bodyLit.isPositive();
 		ITuple terms = model.getDefinition(pred);
 		//System.out.println(":" + ass + " => " + terms);
 
 		int i = 0;
 		for (ITerm term : terms) {
 			String var = ((IVariable) term).getValue();
 			ITuple bodyTuple = bodyLit.getAtom().getTuple();
 
 			if ("Caller".equals(var) || "Target".equals(var) || "Object".equals(var) ||
 			    "Source".equals(var) || "SourceObject".equals(var) || "TargetObject".equals(var)) {
 				ITerm bodyTerm = bodyTuple.get(i);
 				if (positive && !(bodyTerm instanceof IConcreteTerm)) {
 					//System.out.println("Can't do arrow for variable term " + bodyTerm + " in " + ass);
 					break;
 				}
 				objectTerms[nObjectTerms] = bodyTerm;
 				nObjectTerms++;
 				if (nObjectTerms == 2) {
 					break;
 				}
 			}
 			i++;
 		}
 
 		ILiteral opposite = BASIC.createLiteral(!positive, bodyLit.getAtom());
 
 		if (nObjectTerms == 2) {
 			// assertionArrow(n, ?Source, ?Target, positive) :- !body
 			ILiteral arrow = BASIC.createLiteral(true,
 						BASIC.createAtom(assertionArrowP,
 							BASIC.createTuple(
 								assertionN,
 								objectTerms[0],
 								objectTerms[1],
 								CONCRETE.createBoolean(bodyLit.isPositive()))));
 
 			IRule r = BASIC.createRule(makeList(arrow), makeList(opposite));
 			model.rules.add(r);
 			//System.out.println(r);
 
 			// failedAssertion(?N) :- assertionArrow(?N, ?Source, ?Target, ?Positive) in checks.dl
 		} else {
 			// failedAssertion(?N) :- !body
 			ILiteral head = BASIC.createLiteral(true,
 						BASIC.createAtom(failedAssertionP,
 							BASIC.createTuple(assertionN)));
 
 			IRule r = BASIC.createRule(makeList(head), makeList(opposite));
 			model.rules.add(r);
 		}
 
 		// assertionMsg(n, msg).
 		String loc = myPath.getName() + ":" + ass.getAssertTok().getLine();
 		String msg = "Assertion failed (" + loc + "): ";
 		boolean first = true;
 		for (ILiteral lit : body) {
 			if (first) {
 				first = false;
 			} else {
 				msg += ", ";
 			}
 			msg += lit;
 		}
 
 		IRelation messages = model.getRelation(assertionMessageP);
 		messages.add(BASIC.createTuple(assertionN, TERM.createString(msg)));
 	}
 
 	private void addDeclare(ADeclare declare) throws ParserException {
 		model.parseAtom(declare.getAtom(), true, null);
 	}
 
 	private void addImport(AImport aImport) throws Exception {
 		String path = getString(aImport.getStringLiteral());
 
 		new SAMParser(model, new File(dir, path));
 	}
 
 	/* Get the nth line of a file. */
 	private String getLine(File source, int line) throws Exception {
 		 BufferedReader in = new BufferedReader(new FileReader(source));
 		 for (int i = 1; i < line; i++) {
 			  in.readLine();
 		 }
 		 return in.readLine();
 	}
 
 	private SAMClass processConfig(AConfig config) throws ParserException {
 		AConfigBody body = (AConfigBody) config.getConfigBody();
 		SAMTestDriver testDriver = new SAMTestDriver(model);
 
 		for (PConfigField field : body.getConfigField()) {
 			testDriver.declareField(((AConfigField) field).getName());
 		}
 
 		for (PNamedblock block : body.getNamedblock()) {
 			ANamedblock namedBlock = (ANamedblock) block;
 			String name = namedBlock.getName().getText();
 
 			if ("setup".equals(name) || "test".equals(name)) {
 				testDriver.add(namedBlock);
 			} else {
 				throw new ParserException(namedBlock.getName(), "Unknown block '" + name + "'; try 'setup' or 'test'");
 			}
 		}
 
 		return testDriver;
 	}
 }
