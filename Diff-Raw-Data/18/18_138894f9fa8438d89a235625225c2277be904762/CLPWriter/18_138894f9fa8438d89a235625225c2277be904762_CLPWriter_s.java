 /*
  * Copyright (C) 2013 onwards University of Deusto
  * 
  * All rights reserved.
  *
  * This software is licensed as described in the file LICENSE, which
  * you should have received as part of this distribution.
  * 
  * This software consists of contributions made by many individuals, 
  * listed in the file NOTICE and below:
  *
  * Author: Aitor Gómez Goiri <aitor.gomez@deusto.es>
  */
 package es.deusto.deustotech.rio.clips;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.Set;
 
 import org.openrdf.model.Namespace;
 import org.openrdf.model.Statement;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.helpers.RDFWriterBase;
 
 
 /**
  * An implementation of the RDFWriter interface that writes RDF documents in
  * CLP format. The CLP format is a serialization with <a
  * href="http://clipsrules.sourceforge.net/">CLIPS</a>' format.
  * 
  * @author Aitor Gómez Goiri
  */
 public class CLPWriter extends RDFWriterBase implements RDFWriter {
 
 	/*-----------*
 	 * Variables *
 	 *-----------*/
 
 	protected final Writer writer;
 	
 	protected final Set<Namespace> namespacesToBeShortened;
 
 	protected boolean writingStarted;
 
 	/*--------------*
 	 * Constructors *
 	 *--------------*/
 
 	/**
 	 * Creates a new NTriplesWriter that will write to the supplied OutputStream.
 	 * 
 	 * @param out
 	 *        The OutputStream to write the N-Triples document to.
 	 */
 	public CLPWriter(OutputStream out) {
 		this(new OutputStreamWriter(out, Charset.forName("US-ASCII")));
 	}
 
 	/**
 	 * Creates a new NTriplesWriter that will write to the supplied Writer.
 	 * 
 	 * @param writer
 	 *        The Writer to write the N-Triples document to.
 	 */
 	public CLPWriter(Writer writer) {
 		this.writer = writer;
 		this.writingStarted = false;
 		this.namespacesToBeShortened = NamespacesFactory.createNamespacesUsedByDefaultInOurCLIPSRules();
 	}
 
 	/*---------*
 	 * Methods *
 	 *---------*/
 
 	public RDFFormat getRDFFormat() {
 		return RDFFormat.NTRIPLES;
 	}
 
 	public void startRDF()
 		throws RDFHandlerException
 	{
 		if (writingStarted) {
 			throw new RuntimeException("Document writing has already started");
 		}
 
 		writingStarted = true;
 	}
 
 	public void endRDF()
 		throws RDFHandlerException
 	{
 		if (!writingStarted) {
 			throw new RuntimeException("Document writing has not yet started");
 		}
 
 		try {
 			writer.flush();
 		}
 		catch (IOException e) {
 			throw new RDFHandlerException(e);
 		}
 		finally {
 			writingStarted = false;
 		}
 	}
 
 	public void handleNamespace(String prefix, String name) {
 		// N-Triples does not support namespace prefixes.
 	}
 
 	public void handleStatement(Statement st)
 		throws RDFHandlerException
 	{
 		if (!writingStarted) {
 			throw new RuntimeException("Document writing has not yet been started");
 		}
 
 		try {
			// TODO expected format: (. ?s owl:sameAs ?s)
 			writer.write("(. ");
			CLPUtil.append(st.getSubject(), writer, namespacesToBeShortened);
 			writer.write(" ");
			CLPUtil.append(st.getPredicate(), writer, namespacesToBeShortened);
 			writer.write(" ");
			CLPUtil.append(st.getObject(), writer, namespacesToBeShortened);
 			writer.write(" )\n");
 		}
 		catch (IOException e) {
 			throw new RDFHandlerException(e);
 		}
 	}
 
 	public void handleComment(String comment)
 		throws RDFHandlerException
 	{
 		try {
 			writer.write("# ");
 			writer.write(comment);
 			writer.write("\n");
 		}
 		catch (IOException e) {
 			throw new RDFHandlerException(e);
 		}
 	}
 }
