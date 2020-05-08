 //////////////////////////////////////////////////////////////////
 //Creat a sidepanel in Pathvisio for the metabolite information.//
 //////////////////////////////////////////////////////////////////
 
 package metaboliteplugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JEditorPane;
 import javax.swing.SwingUtilities;
 import javax.swing.table.TableStringConverter;
 import javax.swing.text.html.HTMLEditorKit;
 
 import net.sf.jniinchi.JniInchiInput;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.bridgedb.IDMapper;
 import org.bridgedb.IDMapperException;
 import org.bridgedb.Xref;
 import org.bridgedb.bio.BioDataSource;
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.interfaces.IMolecularFormula;
 import org.openscience.cdk.silent.SilentChemObjectBuilder;
 import org.openscience.cdk.smiles.SmilesParser;
 import org.openscience.cdk.tools.BremserOneSphereHOSECodePredictor;
 import org.openscience.cdk.tools.CDKHydrogenAdder;
 import org.openscience.cdk.tools.HOSECodeGenerator;
 import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
 import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
 import org.pathvisio.core.ApplicationEvent;
 import org.pathvisio.core.Engine;
 import org.pathvisio.core.Engine.ApplicationEventListener;
 import org.pathvisio.core.data.GdbManager;
 import org.pathvisio.core.debug.Logger;
 import org.pathvisio.core.model.PathwayElement;
 import org.pathvisio.core.model.PathwayElementEvent;
 import org.pathvisio.core.model.PathwayElementListener;
 import org.pathvisio.core.util.Utils;
 import org.pathvisio.core.view.GeneProduct;
 import org.pathvisio.core.view.SelectionBox.SelectionEvent;
 import org.pathvisio.core.view.SelectionBox.SelectionListener;
 import org.pathvisio.core.view.VPathway;
 import org.pathvisio.core.view.VPathwayElement;
 import org.pathvisio.gui.SwingEngine;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.shared.PrefixMapping;
 
 
 public class MetaboliteInfo extends JEditorPane implements SelectionListener, PathwayElementListener, ApplicationEventListener
 {
 
 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 /////////////////////////////////////////PLUGIN WORKING MECHANISM/////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	public static final String TITLE = "Metabolite";
 		
 	private Engine engine;
 	private ExecutorService executor;
 
 	PathwayElement input;
 	final static int maxThreads = 1;
 	volatile ThreadGroup threads;
 	volatile Thread lastThread;
 	
 	private GdbManager gdbManager;
 	private final SwingEngine se;
 	Model model;
 	public MetaboliteInfo(SwingEngine se)
 	{
 //		super();
 		Engine engine = se.getEngine();
 		engine.addApplicationEventListener(this);
 		VPathway vp = engine.getActiveVPathway();
 		if(vp != null) vp.addSelectionListener(this);
 		this.se = se;
 		this.gdbManager = se.getGdbManager();
 		
 		addHyperlinkListener(se);
 		setEditable(false);
 		setContentType("text/html");
 				
 		executor = Executors.newSingleThreadExecutor();
 
 		//Workaround for #1313
 		//Cause is java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993691
 		setEditorKit(new HTMLEditorKit() {
 			protected Parser getParser() {
 				try {
 					Class c = Class
 							.forName("javax.swing.text.html.parser.ParserDelegator");
 					Parser defaultParser = (Parser) c.newInstance();
 					return defaultParser;
 				} catch (Throwable e) {
 				}
 				return null;
 			}
 		});
 		
 		//Create RDF model
 	
 		try {
 			model = newRdf();
 			loadRdf(model);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("Exception in creation of RDF model");
 		} 
 		
 	}
 		
 	public void setInput(final PathwayElement e) 
 	{
 		//System.err.println("===== SetInput Called ==== " + e);
 		//System.out.println("pathwayelement e: " + input);
 		if(e == input) return; //Don't set same input twice
 		
 		//Remove pathwaylistener from old input
 		if(input != null) input.removeListener(this);
 		
 		if(e == null /*|| e.getObjectType() != ObjectType.DATANODE*/) {
 			input = null;
 			setText("<p>No pathway element is selected.</p>");
 		} 
 		else {
 			input = e;
 			input.addListener(this);
 			doQuery();
 		}
 	}
 
 	private void doQuery() 
 	{
 		setText("Loading");
 		currRef = input.getXref();
 		
 		executor.execute(new Runnable()
 		{
 			public void run()
 			{
 				if(input == null) return;
 				final String txt = getContent(input);
 
 				SwingUtilities.invokeLater(new Runnable()
 				{
 					public void run()
 					{
 						setText(txt);
 						setCaretPosition(0); // scroll to top.
 					}
 				});
 			}
 		});
 	}
 			
 	public void selectionEvent(SelectionEvent e) 
 	{
 		switch(e.type) {
 		case SelectionEvent.OBJECT_ADDED:
 			//Just take the first DataNode in the selection
 			Iterator<VPathwayElement> it = e.selection.iterator();
 			while(it.hasNext()) {
 				VPathwayElement o = it.next();
 				if(o instanceof GeneProduct) {
 					setInput(((GeneProduct)o).getPathwayElement());
 					break;
 				}
 			}
 			break;
 		case SelectionEvent.OBJECT_REMOVED:
 			if(e.selection.size() != 0) break;
 		case SelectionEvent.SELECTION_CLEARED:
 			setInput(null);
 			break;
 		}
 	}
 
 	public void applicationEvent(ApplicationEvent e) {
 		if(e.getType() == ApplicationEvent.Type.VPATHWAY_CREATED) {
 			((VPathway)e.getSource()).addSelectionListener(this);
 		}
 	}
 		
 	Xref currRef;
 	
 	public void gmmlObjectModified(PathwayElementEvent e) {
 		PathwayElement pe = e.getModifiedPathwayElement();
 		if(input != null) {
 			Xref nref = new Xref (pe.getGeneID(), input.getDataSource());
 			if(!nref.equals(currRef)) 
 			{
 				doQuery();
 			}				
 		}
 	}
 	
 	private boolean disposed = false;
 	public void dispose()
 	{
 		assert (!disposed);
 		engine.removeApplicationEventListener(this);
 		VPathway vpwy = engine.getActiveVPathway();
 		if (vpwy != null) vpwy.removeSelectionListener(this);
 		executor.shutdown();
 		disposed = true;
 	}
 	
 
 	public void done() {}
 		
 
 //////////////////////////////////////////////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////PLUGIN CONTENT////////////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	private static StringBuilder builder = new StringBuilder();
 	private String HMDB = null;
 	private String smiles = null;
 	private String name = null;
 	
 	public String getContent(PathwayElement e){
 			builder.setLength(0);
 			Xref ref = e.getXref();
 			//System.out.println("ref: " + ref);
 			IDMapper gdb = gdbManager.getCurrentGdb();		
 			Set<Xref> destrefs = null;
 			try {
 				destrefs = gdb.mapID(ref, BioDataSource.HMDB);
 				HMDB = destrefs.toString();
 				HMDB = HMDB.replace("[Ch:","");
 				HMDB = HMDB.replace("]", "");
 			} catch (IDMapperException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				System.out.println("IDMapperException1");
 			}
 			
 			if (e.getDataNodeType().equals("Metabolite"))
 			{		
 				if (se.getGdbManager().getMetaboliteDb() == null){
 					String str = "Please select a metabolite database";
 					return str;
 				}
 
 				try
 				{
 					smiles = Utils.oneOf (
 							gdbManager.getCurrentGdb().getAttributes (Utils.oneOf(destrefs), "SMILES"));
 					name = Utils.oneOf (
 							gdbManager.getCurrentGdb().getAttributes (Utils.oneOf(destrefs), "Symbol"));
 					builder.append("<h3> General info: </h3>");
 					builder.append("<table border=\"0\">");
 					builder.append("<tr><td>Name: </td><td>" + name + "</td></tr>");
 					builder.append("<tr><td>ID: </td><td>" + HMDB + "</td></tr>");
 					builder.append("<tr><td>SMILES: </td><td>" + smiles + "</td></tr>");
 
 					//Execute other methods to get their content
 					CreateAtomContainer(smiles);
 					CDKInfo();
 					Inchi();
 					//MSImages();
 					//NMR();
 					HOSEGenerator(molecule);
 					//String endpoint = "http://sparql.wikipathways.org/";
 					
 //					String queryString1 = "prefix wp: <http://vocabularies.wikipathways.org/wp#> " + 
 //							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
 //							"prefix dcterms:  <http://purl.org/dc/terms/> " +
 //							"select distinct ?mb " +
 //							"where {?mb a wp:Metabolite ; dc:source \"HMDB\"^^xsd:string ; dc:identifier <http://identifiers.org/hmdb/" + HMDB +"> .} ";
 
 					//String user = null;
 					//String password = null;
 					//sparqlRemoteNoJena(endpoint, queryString, user, password);
 
 					//first sparql queries
 					
 					String queryStringMS1 = "select ?spectrum " + 
 							"where {?spectrum  a <http://linkedchemistry.info/hmdb/MassSpectrum>; " + 
 							"<http://linkedchemistry.info/hmdb/forMolecule> <http://identifier.org/hmdb/" + HMDB + ">;}";
 					String queryStringNMR1 = "select ?spectrum " + 
 							"where {?spectrum  a <http://linkedchemistry.info/hmdb/NMRSpectrum>; " + 
 							"<http://linkedchemistry.info/hmdb/forMolecule> <http://identifier.org/hmdb/" + HMDB + ">;}";
 					
 					List<String> MSspectra = sparql(model, queryStringMS1).getColumn("spectrum");
 					List<String> NMRspectra = sparql(model, queryStringNMR1).getColumn("spectrum");
 					
 					for (String spectrum : MSspectra) {
 						//Second sparql queries
 						String queryStringMS2 = "select ?peak " + 
 								"where { <" + spectrum + "> " +
 								" <http://linkedchemistry.info/hmdb/hasPeak> ?peak .}";
 						builder.append("<p>MS spectra<br />");
 						sparql(model, queryStringMS2);
 					}
 					for (String spectrum : NMRspectra) {
 						String queryStringNMR2 = "select ?peak ?nucleus " + 
 								"where { <" + spectrum + "> " +
 								"<http://linkedchemistry.info/hmdb/hasNucleas> ?nucleus;" + 
 								"<http://linkedchemistry.info/hmdb/hasPeak> ?peak .}";
 						builder.append("<p>\n NMR spectra<br />");
 						sparql(model, queryStringNMR2);
 						}
 				}
 				catch (IDMapperException ex)
 				{
 					System.out.println(ex.getMessage());
 					System.out.println("IDMapperException");
 					ex.printStackTrace();
 					String str = "This HMDB ID was not recognized";
 					return str;
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					System.out.println("Exception e1");
 					String str = "The associated ID was not recognised by BridgeDB.";
 					return str;
 				}
 			return builder.toString();
 			}
 			
 			else {
 				String str = "The plugin only works for metabolites";
 				return str;
 			}
 	}
 	public void CDKInfo(){
 		IMolecularFormula molForm = MolecularFormulaManipulator.getMolecularFormula(molecule);
 		String mf = MolecularFormulaManipulator.getString(molForm);
 		//System.out.println(mf);
 		builder.append("<tr><td>Molecular formula: </td><td>" + mf + "</td></tr>");
 
 	}
 				
 	public void Inchi() {
 //		InChIGeneratorFactory factory = null;
 //		InChIGenerator generator = null;
 //		System.out.println("inchi 1");
 		JniInchiInput input = new JniInchiInput();
 		//System.out.println(input);
 		
 	}
 		
 	public void MSImages(){
 		//MS images
 		String urlLow = "http://www.hmdb.ca/labm/metabolites/" + HMDB + "/ms/spectra/" + HMDB + "L.png";
 		String urlMed = "http://www.hmdb.ca/labm/metabolites/" + HMDB + "/ms/spectraM/" + HMDB + "M.png";
 		String urlHigh = "http://www.hmdb.ca/labm/metabolites/" + HMDB + "/ms/spectraH/" + HMDB + "H.png";
 		
 		builder.append("<br /><h3> Mass spectrometry images: </h3><p>");
 		builder.append("<a href=\"" + urlLow + "\"> Low energy MS image </a><br />");
 		builder.append("<a href=\"" + urlMed + "\"> Medium energy MS image </a><br />");
 		builder.append("<a href=\"" + urlHigh + "\"> High energy MS image </a><br /></p>");
 	}
 	
 	public void NMR(){
 		//NMR tables
 		builder.append("<h3> NMR peak lists and images predicted by HMDB <sup>2</sup>: </h3>");
 		
 		//1H NMR predicted spectra
 		
 		//1H NMR spectrum image link
 		String H1NMRLink = "http://www.hmdb.ca/labm/metabolites/" + HMDB + 
 				"/chemical/pred_hnmr_spectrum/";
 		H1NMRLink = "<a href=\"" + H1NMRLink + "\"> Spectrum image </a><br /><br />";
 	
 		//1H NMR peak list
 		String H1NMR = null;
 		try {
 		//Set up connection and put InChI key into a string
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			HttpGet getH1NMR = new HttpGet("http://www.hmdb.ca/labm/metabolites/"
 			+ HMDB + "/chemical/pred_hnmr_peaklist/" + HMDB + "_peaks.txt");
 
 			HttpResponse response = null;
 			response = httpclient.execute(getH1NMR);
 
 			HttpEntity entity = response.getEntity();
 			H1NMR = EntityUtils.toString(entity);
 			if (H1NMR.startsWith("Group")){
 				H1NMR = H1NMR.replace("	", "</td><td>");
 				H1NMR = H1NMR.replace("\n", "</tr><tr>");
 				H1NMR = H1NMR.replace("<td></td>", "");
 				H1NMR = "Peak list: <br /><table border=\"0\"><tr> " + H1NMR + "</tr></table>";
 				//TODO remove last column (the most right)
 				builder.append("<p><sup>1</sup>H NMR peak list and image predicted by HMDB<sup>2</sup>: <br /></p>");
 				builder.append(H1NMRLink + H1NMR);
 			}
 			else {builder.append("<i>Peak list is not available</i>");}
 		} catch (ClientProtocolException ClientException) {
 			System.out.println("clientexception");
 			ClientException.printStackTrace();
 		} catch (IOException IoException) {
 			System.out.println("IOException");
 			IoException.printStackTrace();
 		} catch (Throwable throwable) {
 			System.out.println(throwable.getMessage());
 			  System.out.println("Throwableblabla");
 			  throwable.printStackTrace();
 		}
 		
 		
 		//13C NMR predicted spectra
 		
 		//13C NMR spectrum image
 		String C13NMRLink = "http://www.hmdb.ca/labm/metabolites/" + HMDB + 
 				"/chemical/pred_cnmr_spectrum/";
 		C13NMRLink = "<a href=\"" + C13NMRLink + "\"> Spectrum image </a><br /><br />";
 
 		//13C NMR peak list
 		String C13NMR = null;
 		try {
 		//Set up connection and put InChI key into a string
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			HttpGet getC13NMR = new HttpGet("http://www.hmdb.ca/labm/metabolites/"
 			+ HMDB + "/chemical/pred_cnmr_peaklist/" + HMDB + "_peaks.txt");
 			HttpResponse response = null;
 			response = httpclient.execute(getC13NMR);
 
 			HttpEntity entity = response.getEntity();
 			C13NMR = EntityUtils.toString(entity);
 			if (C13NMR.startsWith("Carbon")){
 				C13NMR = C13NMR.replace("	", "</td><td>");
 				C13NMR = C13NMR.replace("\n", "</tr><tr>");
 				C13NMR = "Peak list: <br /><table border=\"0\"><tr> " + C13NMR + "</tr></table>";
 				//TODO remove last column (the most right)
 				builder.append("<br /> <p><sup>13</sup>C NMR peak list and image predicted by HMDB<sup>2</sup>: <br /></p>");
 				builder.append(C13NMRLink + C13NMR);
 			}
 			else {builder.append("<i> Peak list could not be loaded</i>");}
 
 		} catch (ClientProtocolException ClientException) {
 			System.out.println(ClientException.getMessage());
 			ClientException.printStackTrace();
 		} catch (IOException IoException) {
 			System.out.println(IoException.getMessage());
 			IoException.printStackTrace();
 		} catch (Throwable throwable) {
 			  System.out.println(throwable.getMessage());
 			  throwable.printStackTrace();
 		}
 		
 	}
 
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////GENERATE IATOMCONTAINER FOR METABOLITE/////////////////////////////////////
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	IAtomContainer molecule;
 	private void CreateAtomContainer(String sm) {
 		String text = sm;
 		try
 		{
 			SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
 			molecule = sp.parseSmiles(text);
 			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(
 					SilentChemObjectBuilder.getInstance());
 			adder.addImplicitHydrogens(molecule);
 			AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
 		}
 		catch (Exception e)
 		{
 			Logger.log.error ("Chempaint error", e);
 		};
 	}
 	
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////////////NMR SHIFT CALCULATIONS/////////////////////////////////////////////
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	protected transient HOSECodeGenerator hcg = new HOSECodeGenerator();
 	protected transient BremserOneSphereHOSECodePredictor predictor = new BremserOneSphereHOSECodePredictor();	
 	StringBuilder data = new StringBuilder();
 	
 	@SuppressWarnings("null")
 	public void HOSEGenerator(IAtomContainer ac){
 		IAtomContainer mc = ac;
 		String hoseCode = null;
 		double shift = 0;
 		
 		
 		
 		builder.append("<h3> NMR shifts predicted by CDK <sup>3</sup>: </h3>" +
 				"<table border=\"0\"> " +
 				"<tr><td> Carbon No. </td><td> Neighbors</td><td> Chem. Shift</td></tr>");
 		StringBuilder All = new StringBuilder();
 		for (int f = 0; f < mc.getAtomCount(); f++) {
 			IAtom atom = mc.getAtom(f);
 			StringBuilder C = new StringBuilder();
 			int g = f+1;
 			
 			if (atom.getSymbol().equals("C")) {
 				try {
 					hoseCode = hcg.getHOSECode(mc, mc.getAtom(f), 1);
 					hoseCode = hcg.makeBremserCompliant(hoseCode);
 					//System.out.println(hoseCode);
 					shift = predictor.predict(hoseCode);
 
 					List<IAtom> neighborList = mc.getConnectedAtomsList(atom);
 					
 					for (int i = 0; i < neighborList.size(); i++){
 						IAtom neighbor= neighborList.get(i);
 						C.append(neighbor.getSymbol());
 					}
 					builder.append("<tr><td>" + g + "</td><td>" + C.toString() + 
 							"</td><td>" + shift + "</td></tr>");
 										
 				} catch (Throwable e) {
 					builder.append("<tr><td>" + g + "</td><td>" + C.toString() + 
 							"</td><td>NA</td></tr>");
 				}
 			}
 		}
 		builder.append("</table>");
 	}
 	
 //////////////////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////////SPARQL ENDPOINT/////////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////////////////
 	//String PathwayName;
 	//String DataNodeCode = input.toString();	
 	
 		
 //	public String sparqlRemoteNoJena(String endpoint, String queryString, String user, String password)
 //			  throws Exception {
 //			      String s = null;
 //
 //			      // use Apache for doing the SPARQL query
 //			      DefaultHttpClient httpclient = new DefaultHttpClient();
 //
 //			      
 //			      // TEST TEST
 //			      System.out.println("endpoint: " + endpoint);
 //			      System.out.println("queryString: " + queryString);
 //			      
 //			      // Set credentials on the client
 //			      if (user != null) {
 //			    	  URL endpointURL = new URL(endpoint);
 //			    	  CredentialsProvider credsProvider = new BasicCredentialsProvider();
 //			    	  credsProvider.setCredentials(
 //			            new AuthScope(endpointURL.getHost(), AuthScope.ANY_PORT), 
 //			            new UsernamePasswordCredentials(user, password)
 //			          );
 //			    	  httpclient.setCredentialsProvider(credsProvider);
 //			      }
 //			         
 //			      List<NameValuePair> formparams = new ArrayList<NameValuePair>();
 //			      formparams.add(new BasicNameValuePair("query", queryString));
 //			      System.out.println("formparams: " + formparams);
 //			      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
 //			      HttpPost httppost = new HttpPost(endpoint);
 //			      System.out.println("HttpPost: " + httppost);
 //			      httppost.setEntity(entity);
 //			      HttpResponse response = httpclient.execute(httppost);
 //			      System.out.println("HttpResponse: " + response);
 //			      HttpEntity responseEntity = response.getEntity();
 //			      InputStream in = responseEntity.getContent();
 //	
 //			      // create a new DocumentBuilderFactory
 //			      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 //			      System.out.println("Factory: " + factory);
 //
 //			      try {
 //			         // use the factory to create a documentbuilder
 //			         DocumentBuilder builder = factory.newDocumentBuilder();
 //			         System.out.println("Builder: " + builder);
 //			         // create a new document from input source
 //			         Document doc = builder.parse(in);
 //			         System.out.println("doc: " + doc);
 //			         Element root = doc.getDocumentElement();
 //			         s = root.toString();
 //			         System.out.println("string s: " + s);
 //			      } catch (Exception ex) {
 //			         ex.printStackTrace();
 //			         System.out.println("exception ex");
 //			      }
 //			      in.close();
 //			      builder.append(s);
 //			      return s;
 //		   }
 	
 	
 	
 ///////////////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////SPARQL WITH JENA/////////////////////////////////////
 ///////////////////////////////////////////////////////////////////////////////////////
 	
 	public static  StringMatrix sparql(Model model, String queryString) throws Exception {
 		  StringMatrix table = null;
 		  List<String> tablePeaks = null;
		  List<String> tableSpectrum = null;
 		  List<String> tableNucleus = null;
 		  
 		  //create table layout
 		  builder.append("<table border=\"0\">");
 		  		  
 		  //start query
 	      com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);
 	      PrefixMapping prefixMap = query.getPrefixMapping();
 	      QueryExecution qexec = QueryExecutionFactory.create(query, model);
 	      try {
 	          com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
 	          table = convertIntoTable(prefixMap, results);
 	          
 	          
 	          if (queryString.contains("?peak")){
 	        	  tablePeaks = table.getColumn("peak");
	        	  if (queryString.contains("hasNucleas")){
	        		  System.out.println("YESSSSSS");
 		        	  tableNucleus = table.getColumn("nucleus");
 		        	  System.out.println("tableNucleus: " + tableNucleus);
		        	  System.out.println("tableNucleus: " + tableNucleus);
 		        	  String tableNucleusS = tableNucleus.get(0);
 		        	  builder.append("<tr><td> NMR type: " + tableNucleusS + "<td></tr>");
 		          }
	        	  for (String peakStr : tablePeaks) {
		        	  peakStr = peakStr.replace("^^http://www.w3.org/2000/10/XMLSchema#float", "");
		        	//add peak values into table 
		        	  builder.append("<tr><td>" + peakStr + "</td></tr>");
	        	  }
 	          }
 	          else {
	        	  tableSpectrum = table.getColumn("spectrum");
	          }       	  
 	      } finally {
 	          qexec.close();
 	      }
 	      return table;
 	  }
 	
 	private static StringMatrix convertIntoTable(PrefixMapping prefixMap, com.hp.hpl.jena.query.ResultSet results) {
 	      StringMatrix table = new StringMatrix();
 	      int rowCount = 0;
 	      while (results.hasNext()) {
 	              rowCount++;
 	          QuerySolution soln = results.nextSolution();
 	          Iterator<String> varNames = soln.varNames();
 	          while (varNames.hasNext()) {
 	              String varName = varNames.next();
 	              int colCount = -1;
 	              if (table.hasColumn(varName)) {
 	            	  colCount = table.getColumnNumber(varName);
 	              } else {
 	            	  colCount = table.getColumnCount() + 1;
 	                  table.setColumnName(colCount, varName);
 	              }
 	              RDFNode node = soln.get(varName);
 	              if (node != null) {
 	                  String nodeStr = node.toString();
 	                  if (node.isResource()) {
 	                      Resource resource = (Resource)node;
 	                      // the resource.getLocalName() is not accurate, so I
 	                      // use some custom code
 	                      String[] uriLocalSplit = split(prefixMap, resource);
 	                      if (uriLocalSplit[0] == null) {
 	                              if (resource.getURI() != null) {
 	                                      table.set(rowCount, colCount, resource.getURI());
 	                              } else {
 	                                      // anonymous node
 	                                      table.set(rowCount, colCount, "" + resource.hashCode());
 	                              }
 	                      } else {
 	                              table.set(rowCount, colCount,
 	                              uriLocalSplit[0] + ":" + uriLocalSplit[1]
 	                          );
 	                      }
 	                  } else {
 	                      if (nodeStr.endsWith("@en"))
 	                              nodeStr = nodeStr.substring(
 	                                      0, nodeStr.lastIndexOf('@')
 	                              );
 	                      table.set(rowCount, colCount, nodeStr);
 	                  }
 	              }
 	          }
 	      }
 	      return table;
 	  }
 	
 	public static String[] split(PrefixMapping prefixMap, Resource resource) {
 	      String uri = resource.getURI();
 	      if (uri == null) {
 	          return new String[] {null, null};
 	      }
 	      Map<String,String> prefixMapMap = prefixMap.getNsPrefixMap();
 	      Set<String> prefixes = prefixMapMap.keySet();
 	      String[] split = { null, null };
 	      for (String key : prefixes){
 	          String ns = prefixMapMap.get(key);
 	          if (uri.startsWith(ns)) {
 	              split[0] = key;
 	              split[1] = uri.substring(ns.length());
 	              return split;
 	          }
 	      }
 	      split[1] = uri;
 	      return split;
 	  }
 	
 	public static Model loadRdf(Model appendTo) throws Exception {
		String file = "/metaboliteplugin/hmdb_spectra.ttl";
		InputStream ins = MetaboliteInfo.class.getClassLoader().getResourceAsStream(file);
         appendTo.read(ins, "", "TTL");
         return appendTo;
 	  }
 	
 	public static Model newRdf() throws Exception {
 	    return ModelFactory.createDefaultModel();
 	  }
 }
