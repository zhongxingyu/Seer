 package ca.gc.agr.itis.ITISProxy;
 
 import ca.gc.agr.itis.ITISModel.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Arrays;  
 
 public class MockUtil{
 
 	private MockUtil(){
 
 	}
 
 	static private TaxonPublication sharedPub = null;
 	
 
 	// Languages using https://en.wikipedia.org/wiki/ISO_639-2 
 	static private String ENG = "eng";
 	static private String POR = "por";
 	static private String FRA = "fra";    
 
 	// Rank Names
 	static final private String RANK_KINGDOM = "Kingdom";
 	static final private String RANK_DIVISION = "Division";
 	static final private String RANK_SUBDIVISION = "Subdivision";
 	static final private String RANK_ORDER = "Order";
 	static final private String RANK_SUBORDER = "Suborder";
 	static final private String RANK_CLASS = "Class";
 	static final private String RANK_FAMILY = "Family";
 	static final private String RANK_GENUS = "Genus";
 	static final private String RANK_SPECIES = "species";
 
 
 	// Taxonomic Standing names
 	static final private String TAXONOMIC_STANDING_VALID = "standing";
 	static final private String TAXONOMIC_STANDING_ACCEPTED = "accepted";
 
 	// Credibility: Completeness names
 	static final private String COMPLETENESS_UNVERIFIED = "unverified";
 	static final private String COMPLETENESS_UNKNOWN = "unknown";
 	static final private String COMPLETENESS_PARTIAL = "partial";
 	// Credibility: Global Species names
 	static final private String GLOBAL_SPECIES_VERIFIED= "verified - standards met";
 	static final private String GLOBAL_SPECIES_UNVERIFIED= "unverified";
 	// Credibility: Currancy names
 	static final private String CURRENCY_2002= "2002";
 	static final private String CURRENCY_2004= "2004";
 	static final private String CURRENCY_UNKNOWN= "unknown";
 
 
 
 	////////////////////
 	static final public String topLevelTsn = "-1";
 	static final public String fungiTsn = "555705";
 	static final public String ascomycotaTsn = "610624";
 	static final public String pezizomycotinaTsn = "610625";
 	static final public String lecanoromycetesTsn = "610630";
 	static final public String lecanoralesTsn = "14000";
 	static final public String teloschistineaeTsn = "191608";
 	static final public String teloschistaceaeTsn = "14034";
 	static final public String caloplacaTsn = "14035";
 	static final public String caloplacaAlbovariegataTsn = "191636";
 
 	static public String[] hierarchyToCaloplacaAlbovariegata = {fungiTsn, ascomycotaTsn, pezizomycotinaTsn, 
 	                                                         lecanoromycetesTsn, lecanoralesTsn, teloschistineaeTsn, 
 	                                                         teloschistaceaeTsn, caloplacaTsn, caloplacaAlbovariegataTsn};
 
 	public static Map<String,String> tsnToNameMap;
 	static private Map<String,String> tsnToRankMap;
 	static private Map<String,String[]> tsnToDownOneLevelNames;
 	static private Map<String,String[]> tsnToDownOneLevelRankNames;
 	static private Map<String,String[]> tsnToDownOneLevelTsns;
 	static private Map<String,String> tsnToTaxonomicStandingMap;
 	static private Map<String,String> tsnToGlobalSpeciesMap;
 	static private Map<String,String> tsnToCompletenessMap;
 	static private Map<String,String> tsnToCurrencyMap;
 	static private Map<String,String> tsnToAuthorMap;
 	static private Map<String, Map<String, List<String>>> tsnToVernacularMap;
 	static private Map<String,TaxonOtherSource[]> tsnToOtherSourcesMap;
 	
 
 
 
 
 	// Kingdoms: special
 	static private String[] topDownOneLevelNames = {"Monera", "Protozoa", "Plantae", "*Fungi", "Animalia", "Chromista"};
 	static private String[] topDownOneLevelTsns = {"202420", "630577", "202422", fungiTsn, "202423", "630578"};
 	static private String[] topDownOneLevelRankNames = {RANK_KINGDOM, RANK_KINGDOM,
 	                                            RANK_KINGDOM, RANK_KINGDOM,
 	                                            RANK_KINGDOM, RANK_KINGDOM};
 
 	static private String[] topLevelTaxonomicStanding = {TAXONOMIC_STANDING_VALID, TAXONOMIC_STANDING_VALID, 
 	                                                    TAXONOMIC_STANDING_ACCEPTED, TAXONOMIC_STANDING_VALID, 
 	                                                    TAXONOMIC_STANDING_ACCEPTED, TAXONOMIC_STANDING_VALID};
 	static private String[] topLevelCompleteness = {COMPLETENESS_UNKNOWN, COMPLETENESS_UNKNOWN, 
 	                                               COMPLETENESS_PARTIAL, COMPLETENESS_UNKNOWN, 
 	                                               COMPLETENESS_UNKNOWN, COMPLETENESS_UNKNOWN, };
 
 
 	//#######################################################################################
 	//Kingdom Fungi
 	//   Divisions
 	static private String[] fungiDownOneLevelNames = {"*Ascomycota","Basidiomycota","Deuteromycotina","Myxomycota"};
 	static private String[] fungiDownOneLevelTsns = {ascomycotaTsn,"623881","14115","13762"};
 	static private String[] fungiDownOneLevelRankNames = {RANK_DIVISION, RANK_DIVISION, RANK_DIVISION, RANK_DIVISION};
 
 	//   Vernacular
 	static private String[] fungiVernacularNamesEng = {"fungi"};
 	static private String[] fungiVernacularNamesPor = {"Fungo"};
 	static private String[] fungiVernacularNamesFra = {"champignons"};
 
 
 
 	//#######################################################################################
 	//Division: Ascomycota
 	static private String[] ascomycotaDownOneLevelNames = {"Coryneliales","Lahmiales","Medeolariales","Mycocaliciales","Ostropales","*Pezizomycotina"};
 	static private String[] ascomycotaDownOneLevelTsns = {"612827", "612828","612829", "610647", "14044", pezizomycotinaTsn};
 	static private String[] ascomycotaDownOneLevelRankNames = { RANK_ORDER, RANK_ORDER, RANK_ORDER, RANK_ORDER, RANK_ORDER, RANK_SUBDIVISION};
 
 	static private String[] ascomycotaSynonyms = {"Ascomycotina", "Ascomycetes"};
 	static private String[] ascomycotaSynonymsTsns = {"13964", "13965"};
 
 	static private String[] ascomycotaVernacularNamesEng = {"asco's", "ascomycetes", "sac fungi"};
 	static private String[] ascomycotaVernacularNamesPor = {"ascomiceto"};
 	static private String[] ascomycotaVernacularNamesFra = {"ascomycètes"};
 
 
 	//#######################################################################################
 	//Subdivision: 
 	static private String[] pezizomycotinaDownOneLevelNames = {"Arthoniomycetes", "Chaetothyriomycetes", "Dothideomycetes", 
 	                                                           "Eurotiomycetes", "Laboulbeniomycetes", "*Lecanoromycetes"};
 	static private String[] pezizomycotinaDownOneLevelTsns = {"610629","610632","610631","610633","610636",lecanoromycetesTsn};
 	static private String[] pezizomycotinaDownOneLevelRankNames = {RANK_CLASS, RANK_CLASS, RANK_CLASS,
 	                                                               RANK_CLASS,RANK_CLASS,RANK_CLASS};
 	static private String[] pezizomycotinaSynonyms = {"Asterinales"};
 	static private String[] pezizomycotinaSynonymsTsn = {"1407"};
 	static private String[] pezizomycotinaSynonymsTaxonAuthorship = {"M.E. Barr ex D. Hawksw. O.E. Erikss."};
 
 
 	//#######################################################################################
 	//Class
 	static private String[] lecanoromycetesDownOneLevelNames = {"Acarosporineae", "Agyriales", "Gyalectales", 
 	                                                            "Icmadophilaceae", "*Lecanorales", "Pertusariales"};
 	static private String[] lecanoromycetesDownOneLevelTsns = {"189587", "610653", "610643", 
 	                                                           "612916", lecanoralesTsn, "610656"};
 	static private String[] lecanoromycetesDownOneLevelRankNames = {RANK_SUBORDER, RANK_ORDER, RANK_ORDER, 
 	                                                                RANK_FAMILY, RANK_ORDER, RANK_ORDER };
 	static private String[] lecanoromycetesSynonyms = {"Umbilicarineae"};
 	static private String[] lecanoromycetesSynonymsTsn = {"191809"};
 	static private String[] lecanoromycetesSynonymsTaxonAuthorship = {};
 
 	//#######################################################################################
 	//Order: 
 	static private String[] lecanoralesDownOneLevelNames = {"Auriculora", "Bartlettiella", "Brigantiaeaceae", 
 	                                                        "Buelliastrum", "Ectolechiaceae", "Eschatogonia", 
 	                                                        "Haploloma", "*Lecanorineae", "*Teloschistineae"};
 	static private String[] lecanoralesDownOneLevelTsns = {"190278", "192013", "191847", 
 	                                                       "191990", "191864", "190166",
 	                                                       "191991", "190073", teloschistineaeTsn};
 
 	static private String[] lecanoralesDownOneLevelRankNames = {RANK_GENUS, RANK_GENUS, RANK_FAMILY, 
 	                                                            RANK_GENUS, RANK_FAMILY, RANK_GENUS, 
 	                                                            RANK_GENUS, RANK_SUBORDER, RANK_SUBORDER};
 
 
 	//#######################################################################################
 	//Suborder: 
 	static private String[] teloschistineaeDownOneLevelNames = {"Fuscideaceae", "Letrouitiaceae", "*Teloschistaceae"};
 	static private String[] teloschistineaeDownOneLevelTsns = {"191615", "191610", teloschistaceaeTsn};
 	static private String[] teloschistineaeDownOneLevelRankNames = {RANK_FAMILY,RANK_FAMILY,RANK_FAMILY};
 
 	//#######################################################################################
 	//Family: 	
 	static private String[] teloschistaceaeDownOneLevelNames = {"Apatoplaca", "*Caloplaca", "Cephalophysis", "Fulgensia"};
 	static private String[] teloschistaceaeDownOneLevelTsns = {"191633", caloplacaTsn, "191757", "191759"};
 	static private String[] teloschistaceaeDownOneLevelRankNames = {RANK_GENUS, RANK_GENUS, RANK_GENUS, RANK_GENUS,};
 
 	//#######################################################################################
 	//Genus: 
 	static private String[] caloplacaDownOneLevelNames = {"Caloplaca adnexa",  "*Caloplaca albovariegata",  "Caloplaca alcarum",  "Caloplaca ammiospila"};
 	static private String[] caloplacaDownOneLevelTsns = {"191635", caloplacaAlbovariegataTsn, "191637", "191638"};
 	static private String[] caloplacaDownOneLevelRankNames = {RANK_SPECIES,RANK_SPECIES,RANK_SPECIES,RANK_SPECIES};
 
 	//#######################################################################################
 	//Species: 
 	static private String[] caloplacaAlbovariegataDownOneLevelNames = {};
 	static private String[] caloplacaAlbovariegataDownOneLevelTsns = {};
 	static private String[] caloplacaAlbovariegataDownOneLevelRankNames = {};
 
 	//#######################################################################################
 	static{
 		init();
 	}
 
 
 	static final void init(){
 		// Init  tsnToNameMap tsn/names
 		tsnToNameMap = new HashMap<String,String>();
 		tsnToRankMap = new HashMap<String,String>();
 		tsnToTaxonomicStandingMap = new HashMap<String,String>();
 		tsnToAuthorMap = new HashMap<String,String>();
 		tsnToCompletenessMap = new HashMap<String,String>();
 		tsnToDownOneLevelNames = new HashMap<String,String[]>() ;
 		tsnToDownOneLevelNames.put(topLevelTsn, topDownOneLevelNames);
 		tsnToDownOneLevelTsns = new HashMap<String,String[]>() ;
 		tsnToDownOneLevelRankNames = new HashMap<String,String[]>() ;
 		tsnToVernacularMap = new HashMap<String, Map<String, List<String>>>();
 		tsnToOtherSourcesMap = new HashMap<String,TaxonOtherSource[]>();
 		
 
 		sharedPub = new TaxonPublication();
 		sharedPub.setReferenceAuthor("Eriksson, O. E., H. -O. Baral, R. S. Currah, K. Hansen, C. P. Kurtzman, G. Rambold et al., eds");
 		sharedPub.setPubYear("2001");
 		sharedPub.setTitle("Outline of Ascomycota");
 		sharedPub.setPubName("Myconet, vol. 7");
 		sharedPub.setPages("1-88");
 		sharedPub.setPubComment("Electronic version available from Myconet web site at http://www.umu.se/myconet/Myconet.html");
 
 		
 		for(int i=0; i<topDownOneLevelTsns.length; i++){
 			tsnToNameMap.put(topDownOneLevelTsns[i], topDownOneLevelNames[i]);
 			tsnToRankMap.put(topDownOneLevelTsns[i], topDownOneLevelRankNames[i]);
 			tsnToTaxonomicStandingMap.put(topDownOneLevelTsns[i], topLevelTaxonomicStanding[i]);
 			tsnToCompletenessMap.put(topDownOneLevelTsns[i], topLevelCompleteness[i]);
 		}
 
 		// Init Fungi
 		//   Vernacular
 		Map<String, List<String>> fungiLanguageToVernacularNamesMap = new HashMap<String, List<String>>();
 		fungiLanguageToVernacularNamesMap = new HashMap<String, List<String>>();
 		fungiLanguageToVernacularNamesMap.put(ENG, Arrays.asList(fungiVernacularNamesEng));
 		fungiLanguageToVernacularNamesMap.put(FRA, Arrays.asList(fungiVernacularNamesFra));
 		fungiLanguageToVernacularNamesMap.put(POR, Arrays.asList(fungiVernacularNamesPor));
 		tsnToVernacularMap.put(fungiTsn, fungiLanguageToVernacularNamesMap);
 		//   Vernacular
 
 		// Down one level
 		//   fungi
 		tsnToDownOneLevelNames.put(fungiTsn, fungiDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(fungiTsn, fungiDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(fungiTsn, fungiDownOneLevelRankNames);
 		for(int i=0; i<fungiDownOneLevelNames.length; i++){
 			tsnToNameMap.put(fungiDownOneLevelTsns[i], fungiDownOneLevelNames[i]);
 			tsnToRankMap.put(fungiDownOneLevelTsns[i], fungiDownOneLevelRankNames[i]);			
 		}
 
 		//   ascomycota
 		tsnToDownOneLevelNames.put(ascomycotaTsn, ascomycotaDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(ascomycotaTsn, ascomycotaDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(ascomycotaTsn, ascomycotaDownOneLevelRankNames);
 		for(int i=0; i<ascomycotaDownOneLevelNames.length; i++){
 			tsnToNameMap.put(ascomycotaDownOneLevelTsns[i], ascomycotaDownOneLevelNames[i]);
 			tsnToRankMap.put(ascomycotaDownOneLevelTsns[i], ascomycotaDownOneLevelRankNames[i]);			
 		}
 
 		//   pezizomycotina
 		tsnToDownOneLevelNames.put(pezizomycotinaTsn, pezizomycotinaDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(pezizomycotinaTsn, pezizomycotinaDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(pezizomycotinaTsn, pezizomycotinaDownOneLevelRankNames);
 		for(int i=0; i<pezizomycotinaDownOneLevelNames.length; i++){
 			tsnToNameMap.put(pezizomycotinaDownOneLevelTsns[i], pezizomycotinaDownOneLevelNames[i]);
 			tsnToRankMap.put(pezizomycotinaDownOneLevelTsns[i], pezizomycotinaDownOneLevelRankNames[i]);			
 		}
 
 		//   lecanoromycetes
 		tsnToDownOneLevelNames.put(lecanoromycetesTsn, lecanoromycetesDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(lecanoromycetesTsn, lecanoromycetesDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(lecanoromycetesTsn, lecanoromycetesDownOneLevelRankNames);
 		for(int i=0; i<lecanoromycetesDownOneLevelNames.length; i++){
 			tsnToNameMap.put(lecanoromycetesDownOneLevelTsns[i], lecanoromycetesDownOneLevelNames[i]);
 			tsnToRankMap.put(lecanoromycetesDownOneLevelTsns[i], lecanoromycetesDownOneLevelRankNames[i]);			
 		}
 
 		//   lecanorales
 		tsnToDownOneLevelNames.put(lecanoralesTsn, lecanoralesDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(lecanoralesTsn, lecanoralesDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(lecanoralesTsn, lecanoralesDownOneLevelRankNames);
 		for(int i=0; i<lecanoralesDownOneLevelNames.length; i++){
 			tsnToNameMap.put(lecanoralesDownOneLevelTsns[i], lecanoralesDownOneLevelNames[i]);
 			tsnToRankMap.put(lecanoralesDownOneLevelTsns[i], lecanoralesDownOneLevelRankNames[i]);			
 		}
 
 		//   teloschistineae
 		tsnToDownOneLevelNames.put(teloschistineaeTsn, teloschistineaeDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(teloschistineaeTsn, teloschistineaeDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(teloschistineaeTsn, teloschistineaeDownOneLevelRankNames);
 		for(int i=0; i<teloschistineaeDownOneLevelNames.length; i++){
 			tsnToNameMap.put(teloschistineaeDownOneLevelTsns[i], teloschistineaeDownOneLevelNames[i]);
 			tsnToRankMap.put(teloschistineaeDownOneLevelTsns[i], teloschistineaeDownOneLevelRankNames[i]);			
 		}
 
 		//   teloschistaceae
 		tsnToDownOneLevelNames.put(teloschistaceaeTsn, teloschistaceaeDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(teloschistaceaeTsn, teloschistaceaeDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(teloschistaceaeTsn, teloschistaceaeDownOneLevelRankNames);
 		for(int i=0; i<teloschistaceaeDownOneLevelNames.length; i++){
 			tsnToNameMap.put(teloschistaceaeDownOneLevelTsns[i], teloschistaceaeDownOneLevelNames[i]);
 			tsnToRankMap.put(teloschistaceaeDownOneLevelTsns[i], teloschistaceaeDownOneLevelRankNames[i]);			
 		}
 
 		//   caloplaca
 		tsnToDownOneLevelNames.put(caloplacaTsn, caloplacaDownOneLevelNames);
 		tsnToDownOneLevelTsns.put(caloplacaTsn, caloplacaDownOneLevelTsns);
 		tsnToDownOneLevelRankNames.put(caloplacaTsn, caloplacaDownOneLevelRankNames);
 		for(int i=0; i<caloplacaDownOneLevelNames.length; i++){
 			tsnToNameMap.put(caloplacaDownOneLevelTsns[i], caloplacaDownOneLevelNames[i]);
 			tsnToRankMap.put(caloplacaDownOneLevelTsns[i], caloplacaDownOneLevelRankNames[i]);			
 		}
 
 		// caloplacaAlbovariegata
 
 		// OtherSources
 		initOtherSources();
 
 		// authors
 		initAuthors();
 		
 	}
 
 	static void initAuthors(){
 		tsnToAuthorMap.put(caloplacaAlbovariegataTsn, "(de Lesd.) Wetmore");
 		tsnToAuthorMap.put(caloplacaTsn, "Th. Fr.");
 		tsnToAuthorMap.put(teloschistaceaeTsn, "Zahlbr., 1898");
 		tsnToAuthorMap.put(lecanoralesTsn, "Nannf., 1932");
 		
 
 		
 	}
 	
 
 	static void initOtherSources(){
 		TaxonOtherSource sources[] = new TaxonOtherSource[1];
 		TaxonOtherSource source = new TaxonOtherSource();
 		sources[0] = source;
 		tsnToOtherSourcesMap.put(caloplacaTsn, sources);		
 		tsnToOtherSourcesMap.put(caloplacaAlbovariegataTsn, sources);		
 
 		source.setSource("NODC Taxonomic Code");
 		source.setSourceType("database");
 		source.setSourceComment("(version 8.0)");
 	}
 	
 
 	static List<ItisRecord> getKingdoms(){
 		List<ItisRecord> kingdoms = new ArrayList<ItisRecord>();
 		for(int i=0; i<topDownOneLevelTsns.length; i++){
 			kingdoms.add(makeRecordFromTsn(topDownOneLevelTsns[i]));
 		}
 		return kingdoms;
 	}
 
 	static ItisRecord getByTSN(String tsn) throws Exception{
 		return makeRecordFromTsn(tsn);
 	}
 
 	static ItisRecord makeRecordFromTsn(String tsn){
 		//System.out.println(tsn + " " + tsnToNameMap);
 		
 		if(tsnToNameMap.containsKey(tsn)){
 			ItisRecord kir = new ItisRecord();
 			kir.setCombinedName(tsnToNameMap.get(tsn));
 			kir.setTsn(tsn);	
 			kir.setCompleteness(tsnToCompletenessMap.get(tsn));
 			kir.setVernacularNames(tsnToVernacularMap.get(tsn));
 			kir.setBelowSpeciesRanks(makeBelowRanks(tsn));
 			kir.setOtherSources(makeOtherSources(tsn));
 			kir.setTaxonomicHierarchy(makeTaxonomicHierarchyFromTsn(tsn));
 			if(tsnToAuthorMap.containsKey(tsn)){
 				kir.setNameAuthor(tsnToAuthorMap.get(tsn));
 			}else{
 				kir.setNameAuthor(null);
 			}
 			if(tsn.equals(caloplacaAlbovariegataTsn)){
 				List<TaxonPublication> pubs = new ArrayList<TaxonPublication>(1);
 				pubs.add(sharedPub);
 				kir.setReferences(pubs);
 			}
 			
 			
 			
 			return kir;
 		}else{
 			// If it is not found, send back the fungi record....
 			return makeRecordFromTsn(fungiTsn);
 		}
 	}
 	
 	private static List<TaxonOtherSource> makeOtherSources(String tsn)
 	{
 		TaxonOtherSource[] sources = tsnToOtherSourcesMap.get(tsn);
 		if(sources == null){
 			return null;
 		}
 		
 		return Arrays.asList(sources);
 	}
 	
 
 	static private List<TaxonomicRank> makeBelowRanks(String tsn)
 	{
 		if(!tsnToDownOneLevelNames.containsKey(tsn)){
 			return null;
 		}
 
 		String ranks[] = tsnToDownOneLevelRankNames.get(tsn);
 		String tsns[] = tsnToDownOneLevelTsns.get(tsn);
 		String names[] = tsnToDownOneLevelNames.get(tsn);
 
 		List<TaxonomicRank> tRanks = new ArrayList<TaxonomicRank>(ranks.length);
 
 		for(int i=0; i<ranks.length; i++){
 			TaxonomicRank rank = new TaxonomicRank();
 			rank.setRankName(ranks[i]);
 			rank.setTsn(tsns[i]);
 			rank.setRankValue(names[i]);
 			tRanks.add(rank);
 		}
 		return tRanks;
 	}
 	
 	public static List<ItisRecord> getRanksOneRankDownFromTsn(String tsn) throws Exception{
		// NOT implemented
		// Need to change from 1.7 to 1.6
		/*
 		switch(tsn){
 		case fungiTsn:
 	    
 			break;
 		case ascomycotaTsn:
 
 			break;
 		case pezizomycotinaTsn:
 
 			break;
 		case lecanoromycetesTsn:
 
 			break;
 		case lecanoralesTsn:
 
 			break;
 		case teloschistineaeTsn:
 
 			break;
 		case teloschistaceaeTsn:
 
 			break;
 		case caloplacaTsn:
 
 			break;
 		case caloplacaAlbovariegataTsn:
 
 			break;
 		}
		*/
 		return null;
 	}
 
 
 	/// Utils
 
     
 	static final TaxonomicRank makeTaxonomicRankFromTsn(String tsn){
 		TaxonomicRank tr = new TaxonomicRank();
 		tr.setTsn(tsn);
 		tr.setRankName(tsnToRankMap.get(tsn));
 		tr.setRankValue(tsnToNameMap.get(tsn));
 		return tr;
 	}
 
 	static final List<TaxonomicRank> makeTaxonomicHierarchyFromTsn(String tsn)
 	{
 		List<TaxonomicRank> hier = new ArrayList<TaxonomicRank>();
 		for(String hierTsn: hierarchyToCaloplacaAlbovariegata){
 			hier.add(makeTaxonomicRankFromTsn(hierTsn));
 			if(hierTsn.equals(tsn)){
 					break;
 				}
 		}
 		return hier;
 	}
 	
 }
