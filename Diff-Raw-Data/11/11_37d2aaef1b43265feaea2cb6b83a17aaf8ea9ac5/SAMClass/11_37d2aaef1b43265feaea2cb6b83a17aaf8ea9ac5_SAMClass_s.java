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
 
 class SAMClass {
 	public String name;
 	private ABehaviour behaviour;
 	Set<String> fields = new HashSet<String>();
 	Model model;
 
 	public SAMClass(Model model, ABehaviour behaviour) throws Exception {
 		this.model = model;
 
 		this.behaviour = behaviour;
 		//System.out.println("Class: " + behaviour);
 
 		this.name = behaviour.getName().getText();
 	}
 
 	public void addDatalog() throws Exception {
		IRelation isType = model.getRelation(isTypeP);
		isType.add(BASIC.createTuple(TERM.createString(name)));
 
 		AExtends extend = (AExtends) behaviour.getExtends();
 		if (extend != null) {
 			String superclass = extend.getName().getText();
 			ITuple objAndSuper = BASIC.createTuple(TERM.createVariable("X"), TERM.createString(superclass));
 			ITuple objAndName = BASIC.createTuple(TERM.createVariable("X"), TERM.createString(name));
 			ILiteral head = BASIC.createLiteral(true, isAP, objAndSuper);
 			ILiteral body = BASIC.createLiteral(true, isAP, objAndName);
 			IRule rule = BASIC.createRule(makeList(head), makeList(body));
 			//System.out.println(rule);
 			model.rules.add(rule);
 		}
 
 		AClassBody body = (AClassBody) behaviour.getClassBody();
 
 		for (PField f : body.getField()) {
 			AField field = (AField) f;
 
 			// hasField(type, name)
 			IRelation rel = model.getRelation(hasFieldP);
 			String fieldName = field.getName().getText();
 			fields.add(fieldName);
 			rel.add(BASIC.createTuple(TERM.createString(this.name), TERM.createString(fieldName)));
 		}
 
 		IRelation hasConstructorRel = model.getRelation(hasConstructorP);
 		IRelation hasMethodRel = model.getRelation(hasMethodP);
 		IRelation methodNameRel = model.getRelation(methodNameP);
 
 		List<PMethod> methods = body.getMethod();
 		for (PMethod m : methods) {
 			AMethod method = (AMethod) m;
 
 			PPattern methodPattern = method.getName();
 			String methodName;
 			if (methodPattern instanceof ANamedPattern) {
 				methodName = ((ANamedPattern) methodPattern).getName().getText();
 			} else {
 				methodName = "*";
 			}
 			ITerm methodNameFull;
 
 			if (method.getType() == null) {
 				if (!methodName.equals(this.name)) {
 					throw new RuntimeException("Constructor must be named after class (or missing return type): " + methodName + " in " + this.name);
 				}
 				methodNameFull = TERM.createString(this.name + ".<init>");
 
 				// hasConstructor(type, "class.<init>")
 				hasConstructorRel.add(BASIC.createTuple(TERM.createString(this.name), methodNameFull));
 
 				// methodName("class.method", "method")
 				methodNameRel.add(BASIC.createTuple(methodNameFull, TERM.createString(methodName)));
 			} else {
 				methodNameFull = TERM.createString(this.name + "." + methodName);
 
 				// hasMethod(type, "class.method")
 				hasMethodRel.add(BASIC.createTuple(TERM.createString(this.name), methodNameFull));
 
 				// methodName("class.method", "method")
 				methodNameRel.add(BASIC.createTuple(methodNameFull, TERM.createString(methodName)));
 			}
 
 			SAMMethod sm = new SAMMethod(this, method, methodNameFull);
 			sm.addDatalog();
 		}
 	}
 }
