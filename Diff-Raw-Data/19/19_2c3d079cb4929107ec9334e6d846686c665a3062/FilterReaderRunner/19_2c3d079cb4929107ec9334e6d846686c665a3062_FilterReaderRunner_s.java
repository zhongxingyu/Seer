 package ch.isbsib.sparql.bed;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.regex.Pattern;
 
 import org.broad.tribble.AbstractFeatureReader;
 import org.broad.tribble.Feature;
 import org.broad.tribble.bed.BEDCodec;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class FilterReaderRunner implements Runnable {
 	private static final Logger log = LoggerFactory
 			.getLogger(FilterReaderRunner.class);
 	private final BlockingQueue<Statement> statements;
 	private final Resource subj;
 	private final Value obj;
 	private final URI pred;
 	private final AbstractFeatureReader reader;
 	volatile boolean done = false;
 	private final File bedFile;
 	private final ValueFactory vf;
 	private final Pattern comma = Pattern.compile(",");
 	private final String wait;
 
 	public FilterReaderRunner(File bedFile, Resource subj, URI pred, Value obj,
 			BlockingQueue<Statement> statements, ValueFactory vf, String wait) {
 
 		this.vf = vf;
 		this.wait = wait;
 
 		this.reader = AbstractFeatureReader.getFeatureReader(
 				bedFile.getAbsolutePath(), new BEDCodec(), false);
 
 		this.subj = subj;
 		this.pred = pred;
 		this.obj = obj;
 		this.statements = statements;
 		this.bedFile = bedFile;
 	}
 
 	@Override
 	public void run() {
 		long lineNo = 0;
 		String filePath = "file:///" + bedFile.getAbsolutePath();
 		Iterable<Feature> iter;
 		try {
 			BEDToTripleConverter conv = new BEDToTripleConverter(vf);
 			iter = reader.iterator();
 			for (Feature feature : iter) {
 				for (Statement statement : filter(conv.convertLineToTriples(
 						filePath, feature, lineNo++))) {
 					try {
						statements.put(statement);
						synchronized (wait) {
							wait.notify();
						}
 					} catch (InterruptedException e) {
 						Thread.interrupted();
 					}
 				}
 			}
 		} catch (IOException e) {
 			log.error("IO error while reading bed file", e);
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				log.error("IO error while closing bed file", e);
 			}
 			done = true;
 			synchronized (wait) {
 				wait.notify();
 			}
 		}
 	}
 
 	private List<Statement> filter(List<Statement> statements) {
 		List<Statement> filtered = new ArrayList<Statement>();
 		for (Statement toFilter : statements) {
 			Resource subject = toFilter.getSubject();
 			Resource predicate = toFilter.getPredicate();
 			Value object = toFilter.getObject();
 			if (matches(subject, subj) && matches(predicate, pred)
 					&& matches(object, obj)) {
 				filtered.add(toFilter);
 			}
 		}
 		return filtered;
 	}
 
 	protected boolean matches(Value subject, Value subj) {
 		return subject.equals(subj) || subj == null;
 	}
 
 }
