 package info.kwarc.sally;
 
 import info.kwarc.sally.core.SallyContext;
 import info.kwarc.sally.core.SallyInteraction;
 import info.kwarc.sally.core.SallyInteractionResultAcceptor;
 import info.kwarc.sally.core.SallyService;
 import info.kwarc.sally.core.comm.SallyMenuItem;
 import info.kwarc.sally.core.comm.SallyModelRequest;
 import info.kwarc.sally.core.interfaces.Theo;
 import info.kwarc.sissi.bpm.inferfaces.ISallyKnowledgeBase;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sally.CADAlexClick;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 @Singleton
 public class PricingService {
 
 	String pricingSparql;
 	String navigateSparql;
 	Model common;
 	Theo theo;
 	SallyInteraction sally;
 	Logger log;
 	ISallyKnowledgeBase kb;
 
 	@Inject
 	public PricingService(Theo theo, SallyInteraction sally, ISallyKnowledgeBase kb) {
 		this.theo = theo;
 		pricingSparql = loadSparql("/pricing.sparql");
 		navigateSparql = loadSparql("/navigate.sparql");
 		
 		common = null;
 		this.sally = sally;
 		this.kb = kb;
 		log = LoggerFactory.getLogger(getClass());
 	}
 
 	private void loadModels() {
 		common = ModelFactory.createDefaultModel();
 		List<Model> models = sally.getPossibleInteractions("/get/semantics", new SallyModelRequest(), Model.class);
 		for (Model mod : models) {
 			common.add(mod);
 		}
 	}
 
 	private String loadSparql(String file) {
 		try {
 			InputStream is = getClass().getResourceAsStream(file);
 			StringWriter writer = new StringWriter();
 			IOUtils.copy(is, writer);
 			return writer.toString();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@SallyService
 	public void pricingService(final CADAlexClick uri, SallyInteractionResultAcceptor acceptor, final SallyContext context) {
 		final Collection<String> files = getFiles(queryModel(uri.getCadNodeId()));
 		if (files.size() ==0)
 			return;
 
 		for (final String file : files) {
			acceptor.acceptResult(new SallyMenuItem("Pricing", file, "Open pricing information associated to the chosen CAD object") {
 				@Override
 				public void run() {
 					kb.signal_global_event("switch_app", file);
 
 					theo.openWindow("Pricing results", "http://localhost:8181/sally/pricing?node="+uri.getCadNodeId()+"&file="+file, 450, 600);
 				}
 			});
 		}
 	}
 
 	public boolean isResultOk(QuerySolution sol) {
 		String objthread = sol.get("objthread").asLiteral().getString();
 		String threadVal = sol.get("threadval").asLiteral().getString();
 		if (objthread.length()==0) {
 			return true;
 		}
 		return threadVal.contains(objthread);
 	}
 
 	public Collection<String> getFiles(ResultSet results) {
 		HashSet<String> result = new HashSet<String>();
 		if (results == null)
 			return result;
 
 		while (results.hasNext()) {
 			QuerySolution sol = results.next();
 			if (!isResultOk(sol)) {
 				continue;
 			}
 			result.add(sol.get("file").toString());
 		}
 		return result;
 	}
 
 	public ResultSet queryModel(String uri) {
 		//if (common == null)
 		loadModels();
 		if (common == null) {
 			return null;
 		}
 
 		String queryStr = String.format(pricingSparql, uri);
 		Query query = QueryFactory.create(queryStr);
 		QueryExecution qe = QueryExecutionFactory.create(query, common);
 		return qe.execSelect();		
 	}
 	
 	public ResultSet getNavigate(String uri) {
 		//if (common == null)
 		loadModels();
 		if (common == null) {
 			return null;
 		}
 
 		String queryStr = String.format(navigateSparql, uri, uri, uri, uri, uri, uri);
 		Query query = QueryFactory.create(queryStr);
 		QueryExecution qe = QueryExecutionFactory.create(query, common);
 		return qe.execSelect();		
 	}
 
 }
