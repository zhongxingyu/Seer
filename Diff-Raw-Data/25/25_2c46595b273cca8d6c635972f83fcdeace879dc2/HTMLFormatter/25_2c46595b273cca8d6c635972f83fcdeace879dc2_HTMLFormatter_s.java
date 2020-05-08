 package cz.cuni.mff.odcleanstore.engine.outputws.output;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.Collection;
 
 import org.restlet.data.CharacterSet;
 import org.restlet.data.MediaType;
 import org.restlet.data.Reference;
 import org.restlet.representation.Representation;
 import org.restlet.representation.WriterRepresentation;
 
 import com.hp.hpl.jena.graph.Node;
 
 import cz.cuni.mff.odcleanstore.configuration.OutputWSConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.CRQuad;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadata;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.engine.Engine;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl.GraphScoreWithTrace;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.Rule;
 import cz.cuni.mff.odcleanstore.queryexecution.BasicQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.NamedGraphMetadataQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryResultBase;
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 /**
  * Returns a representation of a query result in a user-friendly HTML document.
  * 
  * TODO: process labels?
  * 
  * @author Jan Michelfeit
  */
 public class HTMLFormatter extends ResultFormatterBase {
     /** Configuration of the output webservice from the global configuration file. */
     private OutputWSConfig outputWSConfig;
     
     /**
      * Creates a new instance.
      * @param outputWSConfig configuration of the output webservice from the global configuration file
      */
 	public HTMLFormatter(OutputWSConfig outputWSConfig) {
 		this.outputWSConfig = outputWSConfig;
 	}
 	
 	@Override
 	public Representation format(BasicQueryResult result, Reference requestReference) {
 		WriterRepresentation representation = new BasicQueryHTMLRepresentation(result, requestReference); 
 		representation.setCharacterSet(CharacterSet.UTF_8);
 		return representation;
 	}
 
 	@Override
 	public Representation format(NamedGraphMetadataQueryResult metadataResult,
 			GraphScoreWithTrace qaResult, long totalTime, Reference requestReference) {
 		
 		WriterRepresentation representation = new NamedGraphQueryHTMLRepresentation(
 				metadataResult, qaResult, totalTime, requestReference); 
 		representation.setCharacterSet(CharacterSet.UTF_8);
 		return representation;
 	}
 	
 	/** The actual representation of the result HTML document. */
 	private abstract class HTMLRepresentationBase extends WriterRepresentation {
 		/** Representation of the requested URI */
 		private Reference requestReference;
 		
 		/** 
 		 * Initialize.
 		 * @param queryResult query result
 		 * @param requestReference representation of the requested URI
 		 */
 		public HTMLRepresentationBase(QueryResultBase queryResult, Reference requestReference) {
 			super(MediaType.TEXT_HTML);
 			this.requestReference = requestReference;
 		}
 		
 		@Override
 		public abstract void write(Writer writer) throws IOException;
 		
 		/**
 		 * Write start of the HTML document.
 		 * @param writer output writer
 		 * @throws IOException if an I/O error occurs
 		 */
 		protected void writeHeader(Writer writer, QueryResultBase queryResult, Long executionTime) throws IOException {
 			writer.write("<!DOCTYPE html>" 
 					+ "\n<html lang=\"en\">"
 					+ "\n<head>"
 					+ "\n <meta charset=\"utf-8\" />"
 					+ "\n <title>");
 			writer.write(queryResult.getQueryType().toString());
 			writer.write(" query</title>"
 					+ "\n</head>"
 					+ "\n<body>"
 					+ "\n");
 			writer.write(" <p>");
 			if (queryResult.getQuery() != null) {
 				switch (queryResult.getQueryType()) {
 				case KEYWORD:
 					writer.write("Keyword query for <code>");
 					writer.write(queryResult.getQuery());
 					writer.write("</code>.");
 					break;
 				case URI:
 					writer.write("URI query for &lt;");
 					writer.write(queryResult.getQuery());
 					writer.write("&gt;.");
 					break;
 				case NAMED_GRAPH:
 					writer.write("Metadata query for named graph &lt;");
 					writer.write(queryResult.getQuery());
 					writer.write("&gt;.");
 					break;
 				default:
 					writer.write("Query <code>");
 					writer.write(queryResult.getQuery());
 					writer.write("</code>.");
 				}
 			}
 			if (executionTime != null) {
 				writer.write(" Query executed in ");
 				writer.write(formatExecutionTime(executionTime));
 				writer.write('.');
 			}
 			writer.write("</p>\n");
 		}
 
 		/**
 		 * Write table with metadata.
 		 * @param writer output writer
 		 * @throws IOException if an I/O error occurs
 		 */
 		protected void writeMetadata(Writer writer, NamedGraphMetadataMap metadataMap) throws IOException {
 			writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
 			writer.write("  <tr><th>Named graph</th><th>Data source</th><th>Inserted at</th><th>Graph score</th><th>License</th></tr>");
 			for (NamedGraphMetadata metadata : metadataMap.listMetadata()) {
 				writer.write("  <tr><td>");
 				writeRelativeLink(
 						writer,
 						getRequestForNamedGraph(metadata.getNamedGraphURI()),
 						metadata.getNamedGraphURI());
 				writer.write("</td><td>");
 				if (metadata.getSource() != null) {
 					writeAbsoluteLink(writer, metadata.getSource(), metadata.getSource());
 				}
 				writer.write("</td><td>");
 				if (metadata.getInsertedAt() != null) {
 					writer.write(formatDate(metadata.getInsertedAt()));
 				}
 				writer.write("</td><td>");
 				if (metadata.getScore() != null) {
 					writer.write(metadata.getScore().toString());
 				}
 				writer.write("</td><td>");
 				if (metadata.getLicence() != null) {
 					writer.write(metadata.getLicence());
 				}
 				writer.write("</td></tr>\n");					
 			}
 			writer.write(" </table>\n");
 		}
 		
 		/**
 		 * Write end of the HTML document.
 		 * @param writer output writer
 		 * @throws IOException if an I/O error occurs
 		 */
 		protected void writerFooter(Writer writer) throws IOException {
 			writer.write("\n</body>\n</html>");
 		}
 		
 		/**
 		 * Write a single node.
 		 * @param writer output writer
 		 * @throws IOException if an I/O error occurs
 		 */
 		protected void writeNode(Writer writer, Node node) throws IOException {
 			if (node.isURI()) {
 				writeRelativeLink(writer, getRequestForURI(node.getURI()), node.toString());
 			} else if (node.isLiteral()) {
 				writeRelativeLink(writer, getRequestForKeyword(node.getLiteralLexicalForm()), node.toString());
 			} else {
 				writer.write(node.toString());
 			}
 		}
 		
 		/**
 		 * Write a relative hyperlink.
 		 * @param writer output writer
 		 * @param uri URI of the hyperlink
 		 * @param text text of the hyperlink
 		 * @throws IOException if an I/O error occurs
 		 */
 		protected void writeRelativeLink(Writer writer, CharSequence uri, String text) throws IOException {
 			writer.append("<a href=\"/")
 					.append(escapeHTML(uri))
 					.append("\">")
 					.append(text)
 					.append("</a>");
 		}
 		
 		/**
 		 * Write an absolute hyperlink.
 		 * @param writer output writer
 		 * @param uri URI of the hyperlink
 		 * @param text text of the hyperlink
 		 * @throws IOException if an I/O error occurs
 		 */
 		protected void writeAbsoluteLink(Writer writer, CharSequence uri, String text) throws IOException {
 			writer.append("<a href=\"")
 					.append(escapeHTML(uri))
 					.append("\">")
 					.append(text)
 					.append("</a>");
 		}
 		
 		/**
 		 * Returns a URI of a URI query request with other settings same as for the current request 
 		 * @param uri the requested URI
 		 * @return URI of the query request
 		 * @throws UnsupportedEncodingException exception
 		 */
 		protected CharSequence getRequestForURI(String uri) throws UnsupportedEncodingException {
 			StringBuilder result = new StringBuilder();
 			result.append(outputWSConfig.getUriPath());
 			result.append("?uri=");
 			result.append(URLEncoder.encode(uri, "UTF-8"));
 			result.append("&");
 			result.append(requestReference.getQuery());
 			return result.toString();
 		}
 		
 		/**
 		 * Returns a URI of a keyword query request with other settings same as for the current request 
 		 * @param keyword the searched keyword
 		 * @return URI of the keyword request
 		 * @throws UnsupportedEncodingException exception
 		 */
 		protected CharSequence getRequestForKeyword(String keyword) throws UnsupportedEncodingException {
 			StringBuilder result = new StringBuilder();
 			result.append(outputWSConfig.getKeywordPath());
 			result.append("?kw=");
 			result.append(URLEncoder.encode(keyword, "UTF-8"));
 			result.append("&");
 			result.append(requestReference.getQuery());
 			return result.toString();
 		}
 		
 		/**
 		 * Returns a URI of a named graph query request  
 		 * @param namedGraphURI the requested named graph
 		 * @return URI of the named graph request
 		 * @throws UnsupportedEncodingException exception
 		 */
 		protected CharSequence getRequestForNamedGraph(String namedGraphURI) throws UnsupportedEncodingException {
 			StringBuilder result = new StringBuilder();
 			result.append(outputWSConfig.getNamedGraphPath());
 			result.append("?uri=");
 			result.append(URLEncoder.encode(namedGraphURI, "UTF-8"));
 			result.append("&format=HTML");
 			return result.toString();
 		}
 		
 		/**
 		 * Return a text escaped for use in HTML.
 		 * @param text text to escape
 		 * @return escaped text
 		 */
 		protected String escapeHTML(CharSequence text) {
 			return text.toString()
 					.replace("&", "&amp;")
 					.replace("<", "&lt;")
 					.replace(">", "&gt;")
 					.replace("\"", "&quot;")
 					.replace("'", "&#x27;")
 					.replace("/", "&#x2F;");
 		}
 	}
 	
 	private class BasicQueryHTMLRepresentation extends HTMLRepresentationBase {
 		/** Query result. */
 		private BasicQueryResult queryResult;
 		
 		/** 
 		 * Initialize.
 		 * @param queryResult query result
 		 * @param requestReference representation of the requested URI
 		 */
 		public BasicQueryHTMLRepresentation(BasicQueryResult queryResult, Reference requestReference) {
 			super(queryResult, requestReference);
 			this.queryResult = queryResult;
 		}
 		
 		@Override
 		public void write(Writer writer) throws IOException {
 			writeHeader(writer, queryResult, queryResult.getExecutionTime());
 			writeResultQuads(writer);
 			writer.write("\n <br />Source graphs:\n");
 			writeMetadata(writer, queryResult.getMetadata());
 			writerFooter(writer);
 		}
 		
 		/**
 		 * Write table with result quads.
 		 * @param writer output writer
 		 * @throws IOException if an I/O error occurs
 		 */
 		private void writeResultQuads(Writer writer) throws IOException {
 			writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
 			writer.write("  <tr><th>Subject</th><th>Predicate</th><th>Object</th><th>Quality</th><th>Source named graphs</th></tr>\n");
 			for (CRQuad crQuad : queryResult.getResultQuads()) {
 				writer.write("  <tr><td>");
 				writeNode(writer, crQuad.getQuad().getSubject());
 				writer.write("</td><td>");
 				writer.write(crQuad.getQuad().getPredicate().toString());
 				writer.write("</td><td>");
 				writeNode(writer, crQuad.getQuad().getObject());
 				writer.write("</td><td>");
 				writer.write(String.format("%.5f", crQuad.getQuality()));
 				writer.write("</td><td>");
 				boolean first = true;
 				for (String sourceURI : crQuad.getSourceNamedGraphURIs()) {
 					if (!first) {
 						writer.write(", ");
 					}
 					first = false;
 					writeRelativeLink(writer, getRequestForNamedGraph(sourceURI), sourceURI);
 				}
 				writer.write("</td></tr>\n");
 			}
 			writer.write(" </table>\n");
 		}
 	}
 	
 	private class NamedGraphQueryHTMLRepresentation extends HTMLRepresentationBase {
 		/** Result of metadata query about the requested named graph. */
 		private NamedGraphMetadataQueryResult metadataResult;
 		
 		/** Result of quality assessment over the given named graph. */
 		private GraphScoreWithTrace qaResult;
 		
 		/** Execution time of the query. */
 		private long totalTime;
 		
 		
 		/** 
 		 * Initialize.
 		 * @param metadataResult result of metadata query about the requested named graph 
 		 * @param qaResult result of quality assessment over the given named graph
 		 * @param totalTime execution time of the query
 		 * @param requestReference representation of the requested URI
 		 */
 		public NamedGraphQueryHTMLRepresentation(
 				NamedGraphMetadataQueryResult metadataResult,
 				GraphScoreWithTrace qaResult, 
 				long totalTime, 
 				Reference requestReference) {
 			
 			super(metadataResult, requestReference);
 			this.metadataResult = metadataResult;
 			this.qaResult = qaResult;
 			this.totalTime = totalTime;
 		}
 		
 		@Override
 		public void write(Writer writer) throws IOException {
 			// Header
 			writeHeader(writer, metadataResult, totalTime);
 			
 			// Basic metadata
 			writer.write("\n Basic metadata:\n");			
 			writeMetadata(writer, metadataResult.getMetadata());
 			
 			// QA results
 			writer.write("\n <br />Total Quality Assessment score: ");
 			writer.write(qaResult.getScore().toString());
 			writer.write("\n <br />Quality Assessment rule violations:\n");
 			writeQARules(writer, qaResult.getTrace());
 			
 			// Additional provenance metadata
 			if (!metadataResult.getProvenanceMetadata().isEmpty()) {
 				writer.write("\n <br />Additional provenance metadata:\n");
 				writeTriples(writer, metadataResult.getProvenanceMetadata());
 			}
 			
 			// Footer
 			writerFooter(writer);
 		}
 		
 		/**
 		 * Write table with quads converted to triples.
 		 * @param writer output writer
 		 * @throws IOException if an I/O error occurs
 		 */
 		private void writeTriples(Writer writer, Collection<Quad> quads) throws IOException {
 			writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
 			writer.write("  <tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>\n");
 			for (Quad quad : quads) {
 				writer.write("  <tr><td>");
 				writeNode(writer, quad.getSubject());
 				writer.write("</td><td>");
 				writer.write(quad.getPredicate().toString());
 				writer.write("</td><td>");
 				writeNode(writer, quad.getObject());
 				writer.write("</td></tr>\n");
 			}
 			writer.write(" </table>\n");
 		}		
 		
 		/**
 		 * Write table with violated QA rules.
 		 * @param writer output writer
 		 * @param qaRules violated QA rules
 		 * @throws IOException if an I/O error occurs
 		 */
 		private void writeQARules(Writer writer, Collection<Rule> qaRules) throws IOException {
 			writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
 			writer.write("  <tr><th>Rule description</th><th>Score decreased by</th></tr>\n");
 			for (Rule rule : qaRules) {
 				writer.write("  <tr><td>");
				writer.write(rule.getComment());
 				writer.write("</td><td>");
 				writer.write(rule.getCoefficient().toString());
 				writer.write("</td></tr>\n");
 			}
 			writer.write(" </table>\n");
 		}
 	}
 }
