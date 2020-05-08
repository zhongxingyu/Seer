 //
 // Created       : 2006 Jul 28 (Fri) 14:21:09 by Harold Carr.
// Last Modified : 2008 May 28 (Wed) 12:57:20 by Harold Carr.
 //
 
 package org.openhc.trowser.gwt.server;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 import org.openhc.trowser.gwt.common.Constants;
 import org.openhc.trowser.gwt.common.QueryRequest;
 import org.openhc.trowser.gwt.common.QueryResponse;
 import org.openhc.trowser.gwt.common.Triple;
 import org.openhc.trowser.gwt.common.Util;
 
 public class Jena
 {
     public final String NOWHERE = "http://nowhere/";
     public final String RDF_XML = "RDF/XML";
     public final Util   util    = new Util();
 
     Model model;
 
     ////////////////////////////////////////////////////
     //
     // Generic Jena methods
     //
 
     public Jena()
     {
 	model = ModelFactory.createDefaultModel();
     }
 
     public void close()
     {
 	model.close();
     }
 
     public void loadData(final String data)
 	throws IOException
     {
 	readRDF(new ByteArrayInputStream(data.getBytes()), RDF_XML);
     }
 
     public void readRDF(final String filename)
 	throws IOException
     {
 	FileInputStream in = null;
 	try {
 	    in = new FileInputStream(filename);
 	    readRDF(in, RDF_XML);
 	} finally {
 	    if (in != null) {
 		try { in.close(); } catch (IOException e) {}
 	    }
 	}
     }
 
     public void readRDF(final InputStream in, final String format)
 	throws IOException
     {
 	model.read(in, NOWHERE, format);
     }
 
     public void assertFact(final String s, final String p, final String v)
     {
 	final Resource subject  = model.createResource(s);
 	final Property property = model.createProperty(p);
 	final Resource value    = model.createResource(v);
 	subject.addProperty(property, value);
     }
 
     public String makeQueryString(final QueryRequest queryRequest)
     {
 	StringBuffer selectVars = new StringBuffer();
 	StringBuffer query = new StringBuffer();
 	for (Iterator i = queryRequest.getTriples().iterator(); i.hasNext();){
 	    Triple triple = (Triple) i.next();
 	    final String subject  = formatInput(triple.getSubject());
 	    final String property = formatInput(triple.getProperty());
 	    final String value    = formatInput(triple.getValue());
 	    if (subject.startsWith(Constants.questionMarkSymbol)) {
 		selectVars.append(" ").append(subject);
 	    }
 	    if (property.startsWith(Constants.questionMarkSymbol)) {
 		selectVars.append(" ").append(property);
 	    }
 	    if (value.startsWith(Constants.questionMarkSymbol)) {
 		selectVars.append(" ").append(value);
 	    }
 	    query.append(subject).append(" ")
 		 .append(property).append(" ")
 		 .append(value).append(" . ");
 	}
 
 	String selectVariables = selectVars.toString();
 	if (selectVariables.equals("")) {
 	    selectVariables = "?ddduuummmyyy";
 	}
 
 	final String queryString =
 	    " SELECT " + selectVariables +
 	    " WHERE { " +
 	    query.toString() +
 	    " }";
 
 	return queryString;
     }
 
 
     private String formatInput(final String x)
     {
 	if (x.startsWith("?")) {
 	    return x;
 	}
 	if (! x.startsWith("http")) {
 	    return '"' + x + '"';
 	}
 	/*
 	if (x.startsWith("-1")) {
 	    return "_:" + x;
 	}
 	*/
 	return "<" + x + ">";
     }
 
     ////////////////////////////////////////////////////
     //
     // Differentity specific methods
     //
 
     public QueryResponse doQuery(final QueryRequest queryRequest,
 				 final String servletContextRealPathOfSlash)
     {
 	final String queryString = makeQueryString(queryRequest);
 
 	return doQuery(queryString, queryRequest,
 		       servletContextRealPathOfSlash);
     }
 
     private QueryResponse doQuery(final String queryString,
 				  final QueryRequest queryRequest,
 				  final String servletContextRealPathOfSlash)
     {
 	final Query query = QueryFactory.create(queryString);
 	final QueryExecution queryExecution = 
 	    QueryExecutionFactory.create(query, model);
 	final ResultSet resultSet = queryExecution.execSelect();
 
 	// This method is almost generic except for this method call:
 	final QueryResponse queryResponse =
 	    makeResponse(queryRequest, resultSet,
 			 queryString, servletContextRealPathOfSlash);
 
 	// VERY IMPORTANT: close Jena's query engine to release resources.
 	queryExecution.close();
 
 	return queryResponse;
     }
 
     private QueryResponse makeResponse(
         final QueryRequest queryRequest, final ResultSet resultSet,
 	final String queryString, final String servletContextRealPathOfSlash)
     {
 	//
 	// Determine which parts of query were variables.
 	//
 
 	boolean isSubjectVar  = false;
 	boolean isPropertyVar = false;
 	boolean isValueVar    = false;
 	final List list = resultSet.getResultVars();
 	final Iterator i = list.iterator();
 	while (i.hasNext()) {
 	    // REVISIT: limits variables to "subject*"/"property*"/"value*"
 	    final String varName = (String) i.next();
 	    if (varName.startsWith(Constants.subject)) {
 		isSubjectVar  = true;
 	    } else if (varName.startsWith(Constants.property)) {
 		isPropertyVar = true;
 	    } else if (varName.startsWith(Constants.value)) {
 		isValueVar    = true;
 	    }
 	}
 
 	final List subjectResponse  = new ArrayList();
 	final List propertyResponse = new ArrayList();
 	final List valueResponse    = new ArrayList();
 
 	//
 	// For the parts of the query that are NOT variables
 	// return what was given in the query.
 	//
 
 	Iterator triples = queryRequest.getTriples().iterator();
 	while (triples.hasNext()) {
 	    Triple triple = (Triple) triples.next();
 	    if (!isSubjectVar) {
 		subjectResponse.add(triple.getSubject()); 
 	    }
 	    if (!isPropertyVar) {
 		propertyResponse.add(triple.getProperty());
 	    }
 	    if (!isValueVar) {
 		valueResponse.add(triple.getValue()); 
 	    }
 	}
 
 	//
 	// For the parts of the query that ARE variables
 	// return what was found.
 	// 
 
 	while (resultSet.hasNext()) {
 	    final QuerySolution querySolution = resultSet.nextSolution();
 	    if (isSubjectVar) { 
 		getSPVSolution(querySolution,
 			       Constants.subject, subjectResponse);
 	    }
 	    if (isPropertyVar) {
 		getSPVSolution(querySolution,
 			       Constants.property, propertyResponse);
 	    }
 	    if (isValueVar) {
 		getSPVSolution(querySolution,
 			       Constants.value, valueResponse);
 	    }
 	}
 
 	sort(subjectResponse);
 	sort(propertyResponse);
 	sort(valueResponse);
 
 	final QueryResponse queryResponse = 
 	    new QueryResponse(subjectResponse, propertyResponse,
 			      valueResponse,
 			      queryRequest.getSetContentsOf(), // Cookie
 			      // Debug:
 			      queryString + " " + servletContextRealPathOfSlash
 			      );
 
 	return queryResponse;
     }
 
     private void getSPVSolution(final QuerySolution querySolution,
 				final String kind, 
 				final List list)
     {
 	final String x = querySolution.get(kind).toString();
 	if (! list.contains(x)) {
 	    list.add(x);
 	}
 	/*
 	final Object o = querySolution.get(kind);
 	System.out.println(kind);
 	System.out.println(x);
 	System.out.println(o.getClass());
 	System.out.println();
 	*/
     }
 
     private void sort(final List list)
     {
 	// In alphabetic order - including URL.
 	java.util.Collections.sort(list);
 
 	// But do not start with numbers, put them last (if possible).
 
 	try {
 	    if (xxx(list.get(0),
 		    list.get(list.size()-1)) ) {
 		while (startsWithDigit((String) list.get(0))) {
 		    Object x = list.get(0);
 		    list.remove(0);
 		    list.add(x);
 		}
 	    }
 	} catch (Throwable t) {
	    t.printStackTrace(out);
 	}
     }
 
     private boolean xxx(Object x, Object y)
     {
 	boolean xb = startsWithDigit((String)x);
 	boolean yb = startsWithDigit((String)y);
 	//System.out.println(x + " " + xb + " " + y + " " yb);
 	return xb && !yb;
     }
 
     private boolean startsWithDigit(String x)
     {
 	// REVISIT - unicode
 	return 
 	   Character.isDigit(
                util.substringAfterLastSlashOrFirstSharp(x).charAt(0));
     }
 }
 
 // End of file.
