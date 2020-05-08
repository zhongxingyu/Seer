 package controllers;
 import java.util.*;
 
 import play.mvc.*;
 import play.data.*;
 import play.*;
 import play.cache.*;
 
 import views.html.*;
 
 import models.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 import static play.data.Form.*;
 
 
 import com.avaje.ebean.*; //dont think this should be here due to SqlRow
 
 import static play.libs.Json.*;
 import java.net.URLDecoder;
 import java.io.UnsupportedEncodingException;
 
 /**
  * Manage a database of computers
  */
 public class Application extends Controller {
 
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_HOME = redirect(
 			routes.Application.list2(0, "name", "asc", "", "")
 			);
 
 	public static Result stref(Long id) {
 		Streference display = Streference.find.byId(id);
 		return ok( 
 				stref.ref().render(display)
 				); 
 	};
 
 	public static Result proteinsummary(String protein) {	
 
 		List<com.avaje.ebean.SqlRow> listSql = null;
 		List<com.avaje.ebean.SqlRow> listSqlArray = null; // = new List<com.avaje.ebean.SqlRow>();
 
 		String proteinName = "";
 		String swissProtName = null;	
 		String typeProteinEntry = "";
 		ArrayList<Biolsource> biolSourceProtein = new ArrayList<Biolsource>();
 		String accession = "";
 		List<Proteins> proteins = null;
 		//List<String> uniprotDetails = null;
 		List<String> uniprotDetails = new ArrayList<String>();
 		//List<SitesReferences> description = null ;
 		List<SitesReferences> description = new ArrayList<SitesReferences>();
 		List<GsProteinStr2> gsProteinSite = null;
 		String sequenceRetrieval = "";
 		//List<Proteins> proteinMultiple = null;
 		List<Proteins> proteinMultiple = new ArrayList<Proteins>();
 		List<GeneralSites> generalSites = null;
 		List<DefinedSites> definedSites = null;
 
 		List<Biolsource> biolSourceProteins = null;
 
 		if(protein.matches("[A-Z][0-9].*")) {
 			biolSourceProteins = Biolsource.findBiolSourceIdsUniProt(protein);
 		}
 		else{
 			biolSourceProteins = Biolsource.findBiolSourceIdsName(protein);
 		}
 
 		List<Reference> referencesFound = new ArrayList<Reference>();
 		HashSet referencesU = new HashSet();
 
 		for(Biolsource biol : biolSourceProteins){
 			proteinName = biol.protein;
 			swissProtName = biol.swiss_prot;
 			Biolsource objectBiolSource = Ebean.find(Biolsource.class, biol.id);
 			biolSourceProtein.add(objectBiolSource);
 			listSqlArray = Sourceref.findReferenceSource(biol.id);
 			for (com.avaje.ebean.SqlRow r : listSqlArray) {
 				Reference reference = Ebean.find(Reference.class, r.get("id").toString() );
 				referencesFound.add(reference);	
 			}
 			referencesU.addAll(referencesFound);
 		}
 
 		proteins = Proteins.findProteins(protein);
 
 		//problems with legacy feature of and in the swiss prot names
 		if(protein.matches("[A-Z][0-9].*")) {
 
 			generalSites = GeneralSites.findProteinsGeneral(protein);
 			definedSites = DefinedSites.findProteinsDefined(protein);
 
 			String [] splitProtein = protein.split("\\s*[and]+\\s*");
 
 			proteinMultiple = Proteins.findProteinsSwissProt(protein);
 
 
 			for(String partProtein : splitProtein) {
 				uniprotDetails = UniprotConnection.EntryRetrievalExample(partProtein);
 				sequenceRetrieval = UniprotConnection.EntryRetrievalSequence(partProtein);
 			}
 			//List<GsProteinSite> gsProteinSite = GsProteinSite.ProteinRetrieval(protein);
 			gsProteinSite = GsProteinStr2.ProteinRetrieval(protein);
 			//List<SitesReferences2> description = SitesReferences2.findSitesReferences(protein);
 			description = SitesReferences.findSites(protein);
 
 		}
 
 		if(!protein.matches("[A-Z][0-9].*")) {
 			generalSites = GeneralSites.findProteinsGeneralName(protein);
 			definedSites = DefinedSites.findProteinsDefinedName(protein);
 			proteinMultiple = Proteins.findProteinsName(protein);
 			//typeProteinEntry = "not swiss prot";	
 		}
 
 		if (swissProtName == null) {
 			typeProteinEntry = "not swiss prot";
 		}	
 
 		if (uniprotDetails.isEmpty()) {
 			uniprotDetails.add("No info");
 		}
 
 		List<com.avaje.ebean.SqlRow> proteinTax = Proteinsource.findProteinSource(protein);
 		
 		
 		/*List<Proteinsource> proteinsource = Proteinsource.findProteinsource(protein); //need to clean dups
 		List<Proteinsource> depdupeProteinsource =
 			    new ArrayList<Proteinsource>(new LinkedHashSet<Proteinsource>(proteinsource));
 		
 		HashSet test = new HashSet();
 		
 		
 		Set<Proteinsource> s = new HashSet<Proteinsource>(depdupeProteinsource);
 		
 		for(Proteinsource ss : s){
 			System.out.println("whats up   " + ss.div1 + " " + ss.div2 + " " + ss.div3 + " " + ss.div4 );
 			test.add(ss);
 		}
 		
 		for (Object test2 : test) {
 			System.out.println("help me " + test2.toString() );
 		}*/
 		
 
 		Object format = Cache.get("format");	
 		String notation = "gs";
 		if(format != null) {notation = (String) format.toString();}	
 
 
 		return ok(
 				proteinsummary.render(notation, proteinName, protein, biolSourceProtein, proteins, uniprotDetails, gsProteinSite, referencesU, description, sequenceRetrieval, proteinMultiple, generalSites, definedSites, typeProteinEntry, swissProtName, proteinTax)
 				);
 
 	}
 
 	public static Result compositions() {
 		//List<Structure> compositionResult = null;
 		List<Structurecomp> compositionResult = null;
 		
 		Object format = Cache.get("format");	
 		String notation = "gs";
 		if(format != null) {notation = (String) format.toString();}
 
 		if (request().queryString().size() > 0  ) {
 			Map<String, String[]> params = request().queryString();
 			String[] searchTerms = null;
 			String key = null;
 			for (Map.Entry<String, String[]> entry : params.entrySet() ){
 				key = entry.getKey();
 				searchTerms = entry.getValue();
 			}
 			if(key.contains("comp")) {
 				String out =  Structure.buildComposition(searchTerms);
 				//compositionResult = Structure.findComposition(out);
 				
 				compositionResult = Structurecomp.findStructurecomp(out);
 			}
 
 			return ok(
 					compositions.render(notation, compositionResult)
 					);
 		}
 		return ok(compositions.render(notation, compositionResult));
 	}
 
 	public static Result query() {
 		//Cache.set("item.key", "testing", 0);
 		List<String> taxonomy  = Taxonomy.findSpecies(); 
 		HashSet taxUnique = Taxonomy.findSpeciesUnique();
 
 		HashSet sourceUnique = Tissue.sourceSummary();
 		HashSet tissueUnique = GlycobaseSource.tissueSummary();
 		sourceUnique.addAll(tissueUnique);
 
 		HashSet proteinUnique = Proteins.proteinSummary();
 		HashSet pertubationUnique = GlycobaseSource.perturbationSummary();
 		proteinUnique.addAll(pertubationUnique);
 
 
 		List<Tissue> foundTissue = null;
 		List<GlycobaseSource> glycobasesource = null;
 		List<GlycobaseSource> glycobaseFindPerturbation = null;
 
 		List<Biolsource> biolsource = null;
 		List<SqlRow> listSql = null;
 		List<SqlRow> glycobaseSql = null;
 		ArrayList<SqlRow> glycobaseSqlArray = new ArrayList<SqlRow>();
 
 		List<SqlRow> glycobaseSqlTissue = null;
 		ArrayList<SqlRow> glycobaseSqlArrayTissue = new ArrayList<SqlRow>();
 
 		ArrayList<SqlRow> listSqlArray = new ArrayList<SqlRow>();
 		Taxonomy taxonomyId = null;
 		Proteins proteinId = null;
 		Tissue tissueId = null;
 		ArrayList<Taxonomy> taxonomyList = new ArrayList<Taxonomy>();
 		ArrayList<Proteins> proteinList = new ArrayList<Proteins>();
 		ArrayList<Tissue> tissueList = new ArrayList<Tissue>();
 		List<String> listSql22 = new ArrayList<String>();
 		List<List<String>> listSql2 = new ArrayList<List<String>>();
 		String output = "";
 		String outputtissue = "";
 		String outputprotein = "";
 		List<String> outputlist = new ArrayList<String>();
 		List<String> outputtissuelist = new ArrayList<String>();
 		List<String> outputproteinlist = new ArrayList<String>();
 		int countGlycobase = 0;
 		int countTissueGlycobase = 0;
 		int countProteinGlycobase = 0;
 
 		if (request().queryString().size() > 0  ) {
 			String glycobasePerturbationFind = "";
 			Map<String, String[]> params = request().queryString();
 			String[] searchTerms = null;
 			String key = null;
 			for (Map.Entry<String, String[]> entry : params.entrySet() ){
 				key = entry.getKey();
 				searchTerms = entry.getValue();
 			}
 
 			/*if(key.contains("comp")) {
 			String out =  Structure.buildComposition(searchTerms);
 			System.out.println("output is " + out);
 			List<Structure> compositions = Structure.findComposition(out);
 
 			return ok(compositions.render(compositions));
 
 
 			}*/
 
 			if(key.equals("taxonomy")){
 				for (String queryTaxonomy : searchTerms) {
 
 					String glycobasePerturbation = queryTaxonomy;
 					output = GlycobaseSource.findGlycobaseTaxonomy(glycobasePerturbation);
 					outputlist.add(output);
 
 					List<Taxonomy> foundTaxonomy  = Taxonomy.findSpeciesTemp(queryTaxonomy);
 					Long taxId = null;
 					for (Taxonomy tax : foundTaxonomy) {
 						taxId = tax.id;
 						taxonomyId  = Taxonomy.find.byId(taxId);
 						taxonomyList.add(taxonomyId);
 						String taxon = taxonomyId.species;
 						biolsource = Biolsource.findTaxonomyProtein(taxon);
 						listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 						listSql2 = Biolsource.findTaxonomyProteinString(taxon);
 					}
 				}
 			}
 
 			if(key.equals("protein")) {
 				for (String queryProtein : searchTerms) {
 
 					String glycobasePerturbation = queryProtein;
 
 					//glycobaseFindPerturbation = GlycobaseSource.findPerturbation(glycobasePerturbation);
 					outputprotein = GlycobaseSource.findPerturbation(glycobasePerturbation);
 					outputproteinlist.add(outputprotein);
 
 					List<Proteins> foundProteins = Proteins.findProteins(queryProtein);
 					Long protId = null;
 					for (Proteins protein : foundProteins){
 						protId = protein.id;
 						//}
 						//if (protId > 0 && protId != null ) {
 						proteinId = Proteins.find.byId(protId);	
 						proteinList.add(proteinId);
 					}
 				}
 			}
 
 			if(key.equals("tissue")) {
 				for (String queryTissue : searchTerms) {
 					foundTissue = Tissue.findTissue(queryTissue);
 					//glycobaseSqlTissue = GlycobaseSource.findGlycobaseTissue(queryTissue);
 					//glycobaseSqlArrayTissue.addAll(glycobaseSqlTissue);
 
 					outputtissue = GlycobaseSource.findGlycobaseTissue(queryTissue);
 					outputtissuelist.add(outputtissue);
 
 
 					Long tissId = null;
 					for (Tissue source : foundTissue) {
 						tissId = source.id;
 					}
 					if (tissId > 0 ) {
 						tissueId = Tissue.find.byId(tissId);
 						tissueList.add(tissueId);
 					}
 				}
 			}
 
 			for(String xyz : outputlist) {
 				if(xyz.length() > 10) {countGlycobase++;}
 			}
 
 			for(String xyz : outputtissuelist) {
 				if(xyz.length() > 10) {countTissueGlycobase++;}
 			}
 
 			for(String xyz : outputproteinlist) {
 				if(xyz.length() > 10) {countProteinGlycobase++;}
 			}
 
 
 
 
 			return ok(query.render(taxonomy, taxonomyList, biolsource, listSql2, sourceUnique, proteinUnique, proteinList, tissueList, foundTissue, glycobaseFindPerturbation, /*glycobaseSqlArray, glycobaseSqlArrayTissue,*/ outputlist, countGlycobase, outputtissuelist, countTissueGlycobase, outputproteinlist, countProteinGlycobase));
 		}
 
 		return ok(query.render(taxonomy, taxonomyList, biolsource, listSql2, sourceUnique, proteinUnique, proteinList, tissueList, foundTissue, glycobaseFindPerturbation, /* glycobaseSqlArray, glycobaseSqlArrayTissue,*/ outputlist, countGlycobase, outputtissuelist, countTissueGlycobase, outputproteinlist, countProteinGlycobase));
 	}
 
 
 	public static Result structureDetails(Long id) {
 
 		List<Structure> strDisplay = Structure.findStructureRef(id);
 		ArrayList proteinNames = new ArrayList();
 		ArrayList proteinIds = new ArrayList();
 		ArrayList proteinItems = new ArrayList();
 		HashSet proteinNamesUnique = new HashSet();
 		ArrayList taxNames = new ArrayList();
 		ArrayList taxItems = new ArrayList();
 		ArrayList sourceNames = new ArrayList();
 		ArrayList sourceItems = new ArrayList();
 		HashSet sourceNamesUnique = new HashSet();
 		ArrayList uniprot = new ArrayList();
 		HashSet uniprotUnique = new HashSet();
 		ArrayList taxIds = new ArrayList();
 		HashSet taxIdsUnique = new HashSet();
 		HashSet taxDivsUnique = new HashSet();
 
 
 		List<String[]> rowList = new ArrayList<String[]>();
 		Map<String, Integer> m = new HashMap<String, Integer>();
 		String compositionId = "";
 		String type = "";
 
 		if (strDisplay !=null){
 			for (Structure entries : strDisplay){
 				List<Stproteins> stToProtein = entries.stproteins;
 				List<Strtaxonomy> stToTax = entries.strtaxonomy;
 				List<Stsource> stToSource = entries.stsource;
 				type = entries.type;
 
 				compositionId = entries.compositionId;
 
 				if (!stToProtein.isEmpty()) {
 					for (Stproteins stProteinEntry : stToProtein){
 
 						if (stProteinEntry.proteins != null) { 
 							proteinNames.add(stProteinEntry.proteins.name);
 							proteinItems.add(stProteinEntry.proteins);
 							proteinNamesUnique.add(stProteinEntry.proteins);
 						}
 						if (stProteinEntry.proteins == null) {
 
 						}
 					}
 				}
 
 				if (!stToTax.isEmpty()){
 					for (Strtaxonomy stTaxEntry : stToTax){
 						taxNames.add(stTaxEntry.taxonomy.species);
 						taxItems.add(stTaxEntry.taxonomy);
 					}
 				}
 
 				if (!stToSource.isEmpty()){
 					for (Stsource stSourceEntry : stToSource) {
 						String div = "<a href=\"../taxonomy/" +  stSourceEntry.tissue.id + "\"> > " + stSourceEntry.tissue.div1 + " > " + stSourceEntry.tissue.div2 + " > " + stSourceEntry.tissue.div3 + " > " + stSourceEntry.tissue.div4 + "</a> <br />";
 						sourceNamesUnique.add(div);
 						sourceNames.add(stSourceEntry.tissue.id);
 						sourceItems.add(stSourceEntry.tissue);
 					}
 				}
 			}
 		}
 
 		Object format = Cache.get("format");
 		String notation = "gs";
 		if(format != null) {notation = (String) format.toString();} 
 
 		List<Composition> strMain = Composition.findCompositionDetails(compositionId.trim());
 
 		return ok(
 				structureDetails.render(type, strMain, notation, strDisplay, id, proteinNames, proteinNamesUnique, sourceNames, sourceItems, rowList, uniprot, taxItems, taxNames)
 
 				);
 	};
 
 	public static Result refdisplay(Long id) {
 		//Reference displayReference = null;
 		Reference displayReferencce = Reference.find.byId(id);
 		List<Reference> t = Reference.findJournal(id);
 		List<Method> m = Method.findmethod(id);
 		List<Reference> u = Reference.findRefMethods(id);
 
 		for (Reference r : t ) {
 			List<Sourceref> biol = r.sourceref;		
 		}
 
 		ArrayList taxsources = new ArrayList();
 		ArrayList proteinsources = new ArrayList();
 		ArrayList protsources = new ArrayList();
 		HashSet hs = new HashSet();
 		HashSet proteinHs = new HashSet();
 		HashSet swissHs = new HashSet();
 
 		for (Reference taxfind : u){
 			List<Sourceref> source = taxfind.sourceref;
 			for (Sourceref tax : source){
 				hs.add(tax.biolsource.taxonomy);
 				proteinHs.add(tax.biolsource.protein);
 				swissHs.add(tax.biolsource.swiss_prot);
 				//String taxsource = tax.biolsource.taxonomy;
 				//taxsources.add(taxsource); 
 			}
 		}
 
 		taxsources.addAll(hs);
 		proteinsources.addAll(proteinHs);
 		protsources.addAll(swissHs);
 
 		Object format = Cache.get("format");
 		String notation = "gs";
 		if(format != null) {notation = (String) format.toString();}
 
 		return ok( 
 				refdisplay.render(notation, "View selected reference", t, u, taxsources, proteinsources, protsources)
 				); 
 	};
 
 
 
 
 	@Cached(key = "index", duration = 86400)
 	public static Result index() {
 		//return GO_HOME;
 		Cache.set("format", "gs", 0);
 		return ok ( index.render() );
 	}
 
 	public static Result about() {
 		return ok ( about.render() );
 	}
 
 	public static Result builder() {
 		return ok (builder.render() );
 	}
 
 	public static Result login() {
 		return ok ( login.render(form(Login.class)) );
 	}
 
 	public static Result workflows() {
 		return ok ( workflows.render() );
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result massspec() {
 		System.out.println("message " + request().username() );
 		return ok ( mass_spec.render() );
 	}
 
 	public static Result format(String s) {
 		String refererUrl = request().getHeader("referer");
 		System.out.println("referer " + refererUrl);
 		Cache.set("format", s, 0);
 
 		return redirect(refererUrl);
 	}
 
 	/*public static Result saySearch(String structure) {
 	try{
 	String result = URLDecoder.decode(structure, "UTF-8");
 	System.out.println("string test; " + result);
 	} catch (UnsupportedEncodingException e) {
         e.printStackTrace();
         }
 
         controllers.Search.searchStructure();
 	//return ok (saysearch.render() );
 	return ok ( saySearch.render() );
 	}*/
 
 	public static Result ms() {
 		List<SqlRow> results = Lcmucin.groupLcGlycans();
 		return ok ( ms.render() );
 	}
 
 	/**
 	 * Display the taxon search results.
 	 * @param taxon Search taxon string
 	 */
 	public static Result taxonDetails(Long id){
 		Taxonomy taxonomy  = Taxonomy.find.byId(id);
 		String taxon = taxonomy.species;
 		List<Biolsource> biolsource = Biolsource.findTaxonomyProtein(taxon);
 		List<com.avaje.ebean.SqlRow> listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 		Object format = Cache.get("format");
 		String notation = "gs";
 		if(format != null) {notation = (String) format.toString();}
 		//return TODO;
 		return ok(
 				taxonDetails.render(notation, "Taxonomy Description", taxonomy, biolsource, listSql)
 				);
 	}
 
 	public static Result taxonsearch(String findTaxon) {
 		List<Taxonomy> foundTaxonomy  = Taxonomy.findSpeciesTemp(findTaxon);
 		Long taxId = null;
 		for (Taxonomy tax : foundTaxonomy) {
 			taxId = tax.id;
 		};
 		if (taxId > 0) {
 			Taxonomy taxonomy  = Taxonomy.find.byId(taxId);
 			String taxon = taxonomy.species;
 			List<Biolsource> biolsource = Biolsource.findTaxonomyProtein(taxon);
 			List<SqlRow> listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 
 			Object format = Cache.get("format");
 			String notation = "gs";
 			if(format != null) {notation = (String) format.toString();}	
 			//return TODO;
 			return ok(
 					taxonDetails.render(notation, "Taxonomy Description", taxonomy, biolsource, listSql));
 		}
 		else { return TODO;}
 	}
 
 
 	/**
 	 * Display the paginated list of computers.
 	 *
 	 * @param page Current page number (starts from 0)
 	 * @param sortBy Column to be sorted
 	 * @param order Sort order (either asc or desc)
 	 * @param filter Filter applied 
 	 */
 	public static Result list2(int page, String sortBy, String order, String filter, String protein) {
 		return ok(
 				list.render(
 						Reference.page(page, 10, sortBy, order, filter),
 						sortBy, order, filter
 						)
 				);
 	}
 
 	/**
 	 * Display the paginated list of unicarbreferences.
 	 *
 	 * @param page Current page number (starts from 0)
 	 * @param sortBy Column to be sorted
 	 * @param order Sort order (either asc or desc)
 	 * @param filter Filter applied on computer names
 	 */
 	public static Result unicarb(int page, String sortBy, String order, String filter) {
 		return ok(
 				unicarb.render(
 						Unicarbdbreference.page(page, 10, sortBy, order, filter),
 						sortBy, order, filter
 						)
 				);
 	}
 
 	public static Result findAllSpecies() {
 		List<String> speciesCollection = Taxonomy.findSpecies();
 
 		//AString target = "[ ";	
 		String target = "";
 		for(String species : speciesCollection) {
 			//target += "\"" + species + "\", ";
 			target += "replacethis" + species + "replacethis, ";
 		}
 		String endTarget = ""; // " ]";
 		String finalTarget = target.concat(endTarget).toString();
 		return ok(
 				species.render("Display Species", speciesCollection, finalTarget)
 				);
 	}
 
 
 	public static Result tissueSummary(Long id) {
 
 		Tissue tissueFind = Ebean.find(Tissue.class, id);
 
 		String databaseReference = tissueFind.div1 + tissueFind.div2 + tissueFind.div3 + tissueFind.div4;
 
 		ArrayList taxNames = new ArrayList();
 		ArrayList taxItems = new ArrayList();
 		ArrayList taxIds = new ArrayList();
 		ArrayList proteinNames = new ArrayList();
 		ArrayList proteinIds = new ArrayList();
 		ArrayList proteinItems = new ArrayList();
 		ArrayList sourceNames = new ArrayList();
 		ArrayList sourceItems = new ArrayList();
 		ArrayList structureTest = new ArrayList();
 		HashSet structureTesta = new HashSet();
 		HashSet structureTax = new HashSet();
 		//Set<String> ordertax = new TreeSet<String>(); //order tax
 		TreeMap ordertax = new TreeMap();
 		
 		
 		List<com.avaje.ebean.SqlRow> listSql2 = Tissue.findTissueStructures(id);
 		for(com.avaje.ebean.SqlRow l : listSql2) {
 			
 			structureTest.add(l.getLong("structure_id"));
 			for(Object ll : structureTest){
 				structureTesta.add(ll.toString() );
 			}
 		}
 		
 		for(Object a : structureTesta){
 			String aa = a.toString();
 			Long look =  Long.valueOf(aa);
 			Structure objectStr = Ebean.find(Structure.class, look);
 
 			List<Strtaxonomy> tax = objectStr.strtaxonomy;
 			for(Strtaxonomy t : tax){
 				structureTax.add(t.species);
 				//ordertax.add(t.species.toString());
 				ordertax.put(t.species, t.species );
 				
 			}
 
 		}
 		
 
 		//Taxonomy taxonomy  = Taxonomy.find.byId(id); //check here
 		//String taxon = taxonomy.species;
 		//List<Biolsource> biolsource = Biolsource.findTaxonomyProtein(taxon);
 		//List<com.avaje.ebean.SqlRow> listSql = Biolsource.findTaxonomyProteinSQL(taxon);
 
 		Object format = Cache.get("format");
 		String notation = "gs";
 		if(format != null) {notation = (String) format.toString();} 
 
 
 
 		return ok(
 				tissuesummary.render(tissueFind, structureTesta, ordertax, notation, "Tissue Summary", databaseReference,  taxNames, taxItems, proteinNames, proteinItems, sourceNames, sourceItems)
 				);
 	} 
 
 
 	// -- Authentication
 
 	public static class Login {
 
 		public String email;
 		public String password;
 
 		public String validate() {
 			if(User.authenticate(email, password) == null) {
 				return "Invalid user or password";
 			}
 			return null;
 		}
 
 	}
 
 	/**
 	 * Handle login form submission.
 	 */
 	public static Result authenticate() {
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		if(loginForm.hasErrors()) {
 			return badRequest(login.render(loginForm));
 		} else {
 			session("email", loginForm.get().email);
 			return redirect(
 					routes.Application.index() 
 					);
 		}
 	}
 
 	/**
 	 * Logout and clean the session.
 	 */
 	public static Result logout() {
 		session().clear();
 		flash("success", "You've been logged out");
 		return redirect(
 				routes.Application.index() 
 				);
 	}
 
 }
 
