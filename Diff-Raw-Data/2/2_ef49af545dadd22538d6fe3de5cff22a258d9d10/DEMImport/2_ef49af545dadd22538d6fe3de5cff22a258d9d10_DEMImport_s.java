 package org.iucn.sis.server.extensions.demimport;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.naming.NamingException;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.IsoLanguageIO;
 import org.iucn.sis.server.api.io.RegionIO;
 import org.iucn.sis.server.api.io.TaxomaticIO;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.io.WorkingSetIO;
 import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
 import org.iucn.sis.server.api.persistance.RegionCriteria;
 import org.iucn.sis.server.api.utils.DocumentUtils;
 import org.iucn.sis.server.api.utils.FormattedDate;
 import org.iucn.sis.server.api.utils.WordUtils;
 import org.iucn.sis.server.api.utils.XMLUtils;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Infratype;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Relationship;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.models.fields.ProxyField;
 import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
 import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
 import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.iucn.sis.shared.api.utils.FormattingStripper;
 import org.iucn.sis.shared.helpers.TaxonNode;
 import org.iucn.sis.shared.helpers.TaxonomyTree;
 import org.iucn.sis.shared.helpers.TaxonomyTree.Kingdom;
 
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.Column;
 import com.solertium.db.DBException;
 import com.solertium.db.DBSessionFactory;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.SystemExecutionContext;
 import com.solertium.db.query.QConstraint;
 import com.solertium.db.query.QRelationConstraint;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.util.DynamicWriter;
 import com.solertium.util.Replacer;
 import com.solertium.util.TrivialExceptionHandler;
 
 /**
  * This will perform an import of DEM data, creating taxa as it goes if required
  * and adding assessments and their data as required. This process speaks with a
  * running SIS server to achieve creation of taxa and assessments, so please
  * ensure a server is running.
  * 
  * @author adam.schwartz
  */
 public class DEMImport extends DynamicWriter implements Runnable {
 	
 	private static final String CANNED_NOTE = "Unparseable data encountered during DEM import: ";
 	
 	private static final int STATE_CHANGED = 1001;
 	private static final int STATE_TO_IMPORT = 1002;
 	
 	public static final int COUNTRY_CODING_OCCURRENCE_TYPE = 1;
 	public static final int SUBCOUNTRY_CODING_OCCURRENCE_TYPE = 2;
 	public static final int FAO_CODING_OCCURRENCE_TYPE = 3;
 	public static final int LME_CODING_OCCURRENCE_TYPE = 4;
 	
 	private static AtomicBoolean running = new AtomicBoolean(false);
 	private static AtomicBoolean failed = new AtomicBoolean(false);
 	private static StringBuilder statusMessage = new StringBuilder();
 	
 	public static String getStatusMessage() {
 		return statusMessage.toString();
 	}
 
 	public static boolean isFailure() {
 		return failed.get();
 	}
 
 	public static boolean isRunning() {
 		return running.get();
 	}
 	
 	private final String user;
 	private final String demSessionName;
 	private final Session session;
 	private final boolean allowCreate;
 	
 	private final User userO;
 	private final IsoLanguageIO isoLanguageIO;
 	private final TaxomaticIO taxomaticIO;
 	private final TaxonIO taxonIO;
 	private final RegionIO regionIO;
 	private final AssessmentIO assessmentIO;
 	private final WorkingSetIO workingSetIO;
 	private final FieldSchemaGenerator generator;
 	
 	private final Map<String, Row.Set> lookups;
 	
 	private final LinkedHashMap<Long, Taxon> assessedNodesBySpc_id;
 	private final LinkedHashMap<String, Long> spcNameToIDMap;
 	private final Collection<Taxon> nodes;
 	
 	private final List<Assessment> successfulAssessments;
 	private final List<Assessment> failedAssessments;
 	
 	private final ExecutionContext lec;
 
 	private ExecutionContext ec;
 	private TaxonomyTree tree;
 
 	private StringBuilder log = new StringBuilder();	
 
 	public DEMImport(String user, String demSessionName, boolean allowCreate, Session session) throws NamingException {
 		this.user = user;
 		this.userO = new UserIO(session).getUserFromUsername(user);
 		this.demSessionName = demSessionName;
 		this.session = session;
 		this.allowCreate = allowCreate;
 		
 		this.isoLanguageIO = new IsoLanguageIO(session);
 		this.taxomaticIO = new TaxomaticIO(session);
 		this.taxonIO = new TaxonIO(session);
 		this.regionIO = new RegionIO(session);
 		this.assessmentIO = new AssessmentIO(session);
 		this.workingSetIO = new WorkingSetIO(session);
 		this.generator = new FieldSchemaGenerator();
 		
 		lookups = new HashMap<String, Row.Set>();
 		lec = SIS.get().getLookupDatabase();
 
 		spcNameToIDMap = new LinkedHashMap<String, Long>();
 		assessedNodesBySpc_id = new LinkedHashMap<Long, Taxon>();
 		nodes = new HashSet<Taxon>();
 		
 		successfulAssessments = new ArrayList<Assessment>();
 		failedAssessments = new ArrayList<Assessment>();
 	}
 
 	private void addTaxaDetails() throws DBException {
 		for (Iterator<Long> iter = assessedNodesBySpc_id.keySet().iterator(); iter.hasNext();) {
 			Long curSpcID = iter.next();
 			Taxon curNode = assessedNodesBySpc_id.get(curSpcID);
 			
 			printf("Processing %s", curNode.getFriendlyName());
 			
 			boolean changed = false;
 
 			// DO COMMON NAMES
 			for (Row curRow : queryDEM("common_names", curSpcID)) {
 				String name = curRow.get("Common_name").getString(Column.NEVER_NULL);
 				boolean primary = curRow.get("Primary").getInteger(Column.NEVER_NULL) == 1;
 				String language = curRow.get("Language").getString(Column.NEVER_NULL);
 				String isoCode = curRow.get("ISO_LANG").getString(Column.NEVER_NULL);
 
 				CommonName commonName = new CommonName();
 				commonName.setName(name);
 				commonName.setIso(isoLanguageIO.getIsoLanguageByCode(isoCode));
 				commonName.setPrincipal(primary);
 				commonName.setChangeReason(CommonName.ADDED);
 				//CommonNameFactory.createCommonName(name, language, isoCode, primary);
 				commonName.setValidated(false);
 
 				boolean alreadyExists = false;
 
 				for (CommonName cn : curNode.getCommonNames()) {
 					if( commonName.getName().equalsIgnoreCase(cn.getName()) &&
 							commonName.getIsoCode().equalsIgnoreCase(cn.getIsoCode()) ) {
 						alreadyExists = true;
 						break;
 					}
 				}
 
 				if (!alreadyExists) {
 					if( commonName.getName().toUpperCase().equals(commonName.getName())) {
 						commonName.setName(WordUtils.capitalizeFully(commonName.getName()));
 					}
 					
 					commonName.setTaxon(curNode);
 					curNode.getCommonNames().add(commonName);
 
 					printf(" - Added common name %s", commonName.getName());
 					
 					changed = true;
 				}
 			}
 			
 			// DO SYNONYMS
 			for (Row curRow : queryDEM("Synonyms", curSpcID)) {
 				String name = curRow.get("Species_name").getString(Column.NEVER_NULL);
 				String notes = curRow.get("SynonymNotes").getString(Column.NEVER_NULL);
 
 				Synonym curSyn;
 				int synlevel = TaxonNode.SPECIES;
 				String[] brokenName = name.split("\\s");
 				String genusName = "";
 				String spcName = "";
 				int infraType = -1;
 				String infraName = "";
 				String subpopName = "";
 
 				genusName = brokenName[0];
 				synlevel = TaxonNode.GENUS;
 
 				if (brokenName.length > 1) {
 					spcName = brokenName[1];
 					synlevel = TaxonNode.SPECIES;
 				}
 				if (brokenName.length > 2) {
 					if (brokenName[2].matches("^ssp\\.?$") || brokenName[2].matches("^var\\.?$")) {
 						if (brokenName[2].matches("^ssp\\.?$"))
 							infraType = TaxonNode.INFRARANK_TYPE_SUBSPECIES;
 						else
 							infraType = TaxonNode.INFRARANK_TYPE_VARIETY;
 
 						infraName = brokenName[3];
 						for (int i = 4; i < brokenName.length; i++)
 							infraName += " " + brokenName[i];
 
 						synlevel = TaxonNode.INFRARANK;
 					} else if (brokenName.length > 3
 							&& (brokenName[3].matches("^\\.?$") || brokenName[3].matches("^var\\.?$"))) {
 						spcName += " " + brokenName[2];
 						if (brokenName[3].matches("^ssp\\.?$"))
 							infraType = TaxonNode.INFRARANK_TYPE_SUBSPECIES;
 						else
 							infraType = TaxonNode.INFRARANK_TYPE_VARIETY;
 
 						infraName = brokenName[4];
 						for (int i = 5; i < brokenName.length; i++)
 							infraName += " " + brokenName[i];
 					} else {
 						String whatsLeft = brokenName[2];
 						for (int i = 3; i < brokenName.length; i++)
 							whatsLeft += " " + brokenName[i];
 
 						if (whatsLeft.toLowerCase().contains("stock")
 								|| whatsLeft.toLowerCase().contains("subpopulation")) {
 							subpopName = whatsLeft;
 							synlevel = TaxonNode.SUBPOPULATION;
 						} else
 							spcName += " " + whatsLeft;
 					}
 				}
 				String authority = curRow.get("Syn_Authority").getString(Column.NEVER_NULL);
 
 				curSyn = new Synonym();
 				curSyn.setGenusName(genusName);
 				curSyn.setSpeciesName(spcName);
 				curSyn.setInfraName(infraName);
 				try {
 				curSyn.setInfraType(Infratype.getInfratype(infraType).getName());
 				} catch (NullPointerException e) { }
 				curSyn.setTaxon_level(TaxonLevel.getTaxonLevel(synlevel));
 				curSyn.setStockName(subpopName);
 				curSyn.setStatus(Synonym.ADDED);
 				curSyn.setAuthority(authority, synlevel);
 				//FIXME add notes
 
 				boolean alreadyExists = false;
 
 				for (Synonym syn : curNode.getSynonyms()) 
 					alreadyExists = curSyn.getFriendlyName().equals(syn.getFriendlyName());
 
 				if (!alreadyExists) {
 					curSyn.setTaxon(curNode);
 					curNode.getSynonyms().add(curSyn);
 					changed = true;
 					
 					printf("Added %s", curSyn.getFriendlyName());
 				}
 			}
 
 			if (changed && curNode.getState() != STATE_TO_IMPORT)
 				curNode.setState(STATE_CHANGED);
 		}
 	}
 
 	private void buildAssessments() throws Exception {
 		log.append("<tr><td align=\"center\" colspan=\"9\">" + "<b><u>New Draft Assessments</u></b></td></tr>");
 
 		// FOR EACH NEW SPECIES...
 		for (Iterator<Long> iter = assessedNodesBySpc_id.keySet().iterator(); iter.hasNext();) {
 			Long curDEMid = iter.next();
 			Taxon curNode = assessedNodesBySpc_id.get(curDEMid);
 
 			logNode(curNode);
 
 			Assessment curAssessment = new Assessment();
 			curAssessment.setId(0);
 			curAssessment.setTaxon(curNode);
 			curAssessment.setSchema(SIS.get().getDefaultSchema());
 			curAssessment.setType(AssessmentType.DRAFT_ASSESSMENT_TYPE);
 
 			systematicsTableImport(curDEMid, curAssessment);
 			
 			if (curAssessment.getField().isEmpty())
 				continue;
 			
 			if (!assessmentIO.allowedToCreateNewAssessment(curAssessment)) {
 				failedAssessments.add(curAssessment);
 			}
 			else {
 				distributionTableImport(curDEMid, curAssessment);
 	//			populationTableImport(curDEMid, curAssessment, data);
 				habitatTableImport(curDEMid, curAssessment);
 	//			lifeHistoryTableImport(curDEMid, curAssessment, data);
 	//			threatTableImport(curDEMid, curAssessment, data);
 	//			countryTableImport(curDEMid, curAssessment, data);
 				redListingTableImport(curDEMid, curAssessment);
 	//			landCoverTableImport(curDEMid, curAssessment, data);
 	//			utilisationTableImport(curDEMid, curAssessment, data);
 	//			growthFormTableImport(curDEMid, curAssessment, data);
 	//			conservationMeasuresTableImport(curDEMid, curAssessment, data);
 	//			ecosystemServicesTableImport(curDEMid, curAssessment, data);
 	//			riversTableImport(curDEMid, curAssessment, data);
 	//			lakesTableImport(curDEMid, curAssessment, data);
 	//			faoMarineTableImport(curDEMid, curAssessment, data);
 	//			lmeTableImport(curDEMid, curAssessment, data);
 	//			useTradeImport(curDEMid, curAssessment, data);
 	//			livelihoodsTableImport(curDEMid, curAssessment, data);
 	//
 	//			curAssessment.addData(data);
 	//
 				referencesImport(curDEMid, curAssessment);
 	
 				//FIXME: this is probably wrong
 				//OccurrenceMigratorUtils.migrateOccurrenceData(curAssessment);
 	
 				successfulAssessments.add(curAssessment);
 			}
 		}
 	}
 	
 	private void report(Taxon taxon) {
 		printf("Adding taxon #%s %s at level %s to tree", taxon.getId(), taxon.getFriendlyName(), taxon.getLevel());
 	}
 	
 	private Taxon newSimpleNode(String name, String status, int level, Taxon parent) {
 		Taxon taxon = new Taxon();
 		taxon.setId(0);
 		taxon.setName(name);
 		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(level));
 		taxon.setStatus(status);
 		taxon.setParent(parent);
 		taxon.setFriendlyName(name);
 		taxon.setState(STATE_TO_IMPORT);
 		
 		return taxon;
 	}
 
 	public void buildTree() throws Exception {
 		tree = new TaxonomyTree();
 
 		SelectQuery select = new SelectQuery();
 		select.select("Systematics", "*");
 
 		Row.Set rows = new Row.Set();
 
 		ec.doQuery(select, rows);
 
 		List<Row> rowList = rows.getSet();
 
 		print("There are " + rowList.size() + " entries in this DEM.");
 
 		for (Iterator<Row> iter = rowList.listIterator(); iter.hasNext();) {
 			Row curCol = iter.next();
 
 			ArrayList<String> footprint = new ArrayList<String>();
 
 			Taxon kingdomN = null;
 			Taxon phylumN = null;
 			Taxon classN = null;
 			Taxon orderN = null;
 			Taxon familyN = null;
 			Taxon genusN = null;
 			Taxon speciesN = null;
 			Taxon rankN = null;
 			Taxon sspN = null;
 
 			String curKingdom = curCol.get("Kingdom").getString().trim().toUpperCase();
 			String curPhylum = curCol.get("Phylum").getString().trim().toUpperCase();
 			String curClass = curCol.get("Class").getString().trim().toUpperCase();
 			String curOrder = curCol.get("Order").getString().trim().toUpperCase();
 			String curFamily = curCol.get("Family").getString().trim().toUpperCase();
 			String curGenus = curCol.get("Genus").getString().trim();
 			String curSpecies = curCol.get("Species").getString().trim();
 			String curInfratype = curCol.get("Rank").toString();
 			String curInfraEpithet = curCol.get("Rank_epithet").toString();
 			String curSSP = null;
 			String curStat = "New";
 
 			String spcTaxonomicAuthority = "";
 			String infraTaxonomicAuthority = "";
 			long specID = curCol.get("Sp_code").getPrimitiveLong();
 
 			boolean hybrid = curCol.get("Hybrid").getInteger(Column.NEVER_NULL) == 1;
 
 			if (curCol.get("Author_year").getString(Column.NATURAL_NULL) != null)
 				spcTaxonomicAuthority = curCol.get("Author_year").getString();
 			if (curCol.get("Rank_author").getString(Column.NATURAL_NULL) != null)
 				infraTaxonomicAuthority = curCol.get("Rank_author").getString();
 
 			if (!isBlank(curInfratype) && isBlank(curInfraEpithet)) {
 				failed.set(true);
 				statusMessage.append("Your DEM contains an infrarank with no epithet specified."
 						+ " Please change your data, and try the import again.<br>");
 				return;
 			}
 
 			if (isBlank(curInfratype) && !isBlank(curInfraEpithet)) {
 				failed.set(true);
 				statusMessage.append("Your DEM contains an infrarank with an epithet, but no"
 						+ " rank specified. Please change your data, and try the import again.<br>");
 				return;
 			}
 
 			if (curCol.get("Rank_author").getString(Column.NATURAL_NULL) != null)
 				spcTaxonomicAuthority = curCol.get("Rank_author").getString(Column.NATURAL_NULL);
 
 			curSSP = curCol.get("Sub_pop").getString(Column.NATURAL_NULL);
 			if (curSSP != null)
 				curSSP = curSSP.trim();
 
 			kingdomN = tree.getNode(TaxonNode.KINGDOM, curKingdom, curKingdom);
 			if (kingdomN == null) {
 				kingdomN = fetchNode(curKingdom, curKingdom);
 
 				if (kingdomN == null) {
 					kingdomN = newSimpleNode(curKingdom, curStat, TaxonLevel.KINGDOM, null);
 				}
 
 				kingdomN.getFootprint();
 				tree.addNode(curKingdom, kingdomN);
 				
 				report(kingdomN);
 			}
 			footprint.add(kingdomN.getName());
 
 			nodes.add(kingdomN);
 
 			phylumN = tree.getNode(TaxonNode.PHYLUM, curKingdom, curPhylum);
 			if (phylumN == null) {
 				phylumN = fetchNode(curKingdom, curPhylum);
 
 				if (phylumN == null) {
 					phylumN = newSimpleNode(curPhylum, curStat, TaxonLevel.PHYLUM, kingdomN);
 				}
 
 				kingdomN.getChildren().add(phylumN);
 				phylumN.getFootprint();
 				tree.addNode(curKingdom, phylumN);
 				
 				report(phylumN);
 			}
 			updateFootprint(footprint, phylumN);
 
 			nodes.add(phylumN);
 
 			classN = tree.getNode(TaxonNode.CLASS, curKingdom, curClass);
 			if (classN == null) {
 				classN = fetchNode(curKingdom, curClass);
 
 				if (classN == null) {
 					classN = newSimpleNode(curClass, curStat, TaxonLevel.CLASS, phylumN);
 				}
 
 				phylumN.getChildren().add(classN);
 				classN.getFootprint();
 				tree.addNode(curKingdom, classN);
 				
 				report(classN);
 			}
 			updateFootprint(footprint, classN);
 			nodes.add(classN);
 
 			orderN = tree.getNode(TaxonNode.ORDER, curKingdom, curOrder);
 			if (orderN == null) {
 				orderN = fetchNode(curKingdom, curOrder);
 
 				if (orderN == null) {
 					orderN = newSimpleNode(curOrder, curStat, TaxonLevel.ORDER, classN);
 				}
 
 				classN.getChildren().add(orderN);
 				orderN.getFootprint();
 				tree.addNode(curKingdom, orderN);
 				
 				report(orderN);
 			}
 			updateFootprint(footprint, orderN);
 			nodes.add(orderN);
 
 			familyN = tree.getNode(TaxonNode.FAMILY, curKingdom, curFamily);
 			if (familyN == null) {
 				familyN = fetchNode(curKingdom, curFamily);
 
 				if (familyN == null) {
 					familyN = newSimpleNode(curFamily, curStat, TaxonLevel.FAMILY, orderN);
 				}
 
 				orderN.getChildren().add(familyN);
 				familyN.getFootprint();
 				tree.addNode(curKingdom, familyN);
 			
 				report(familyN);
 			}
 			updateFootprint(footprint, familyN);
 			nodes.add(familyN);
 
 			genusN = tree.getNode(TaxonNode.GENUS, curKingdom, curGenus);
 			if (genusN == null) {
 				genusN = fetchNode(curKingdom, curGenus);
 
 				if (genusN == null) {
 					genusN = newSimpleNode(curGenus, curStat, TaxonLevel.GENUS, familyN);
 				}
 
 				familyN.getChildren().add(genusN);
 				genusN.getFootprint();
 				tree.addNode(curKingdom, genusN);
 				
 				report(genusN);
 			}
 			updateFootprint(footprint, genusN);
 			nodes.add(genusN);
 
 			speciesN = tree.getNode(TaxonNode.SPECIES, curKingdom, curGenus + " " + curSpecies);
 			if (speciesN == null) {
 				speciesN = fetchNode(curKingdom, curGenus + " " + curSpecies);
 
 				if (speciesN == null) {
 					speciesN = newSimpleNode(curSpecies, curStat, TaxonLevel.SPECIES, genusN);
 					speciesN.setFriendlyName(curGenus + " " + curSpecies);
 					
 					spcNameToIDMap.put(speciesN.getFullName(), new Long(specID));
 				}
 
 				speciesN.setFriendlyName(curGenus + " " + speciesN.getName());
 				speciesN.setTaxonomicAuthority(spcTaxonomicAuthority);
 
 				genusN.getChildren().add(speciesN);
 				speciesN.getFootprint();
 				tree.addNode(curKingdom, speciesN);
 				
 				report(speciesN);
 			}
 			
 			//if (curInfratype == null && curSSP == null) {
 				assessedNodesBySpc_id.put(new Long(specID), speciesN);
 				speciesN.setHybrid(hybrid);
 			//}
 
 			updateFootprint(footprint, speciesN);
 			nodes.add(speciesN);
 
 			if (!isBlank(curInfratype)) {
 				rankN = tree.getNode(TaxonNode.INFRARANK, curKingdom, curGenus + " " + curSpecies + " " + curInfratype
 						+ " " + curInfraEpithet);
 				if (rankN == null) {
 					rankN = fetchNode(curKingdom, curGenus + " " + curSpecies + " " + curInfratype + " " + curInfraEpithet);
 
 					if (rankN == null) {
 						rankN = newSimpleNode(curInfraEpithet, curStat, TaxonLevel.INFRARANK, speciesN);
 						
 
 						if (curInfratype.trim().matches("^ssp\\.?$"))
 							rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
 						else if (curInfratype.trim().matches("^var\\.?$")
 								&& rankN.getFootprint()[0].equalsIgnoreCase("PLANTAE"))
 							rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_VARIETY));
 						else {
 							failed.set(true);
 							statusMessage.append("Your DEM contains an infrarank with an invalid rank of " + curInfratype + "."
 									+ " The valid ranks are \"ssp.\" and \"var.\", and a variety MUST be"
 									+ " in the kingdom PLANTAE. Please change your data, and"
 									+ " try the import again.<br>");
 							return;
 						}
 
 						rankN.setFriendlyName(rankN.generateFullName());
 						
 						spcNameToIDMap.put(rankN.getFullName(), new Long(specID));
 					}
 
 					rankN.setFriendlyName(rankN.generateFullName());
 					rankN.setTaxonomicAuthority(infraTaxonomicAuthority);
 
 					speciesN.getChildren().add(rankN);
 					rankN.getFootprint();
 					tree.addNode(curKingdom, rankN);
 					
 					report(rankN);
 				}
 
 				//if (curSSP == null) {
 					assessedNodesBySpc_id.put(new Long(specID), rankN);
 					rankN.setHybrid(hybrid);
 				//}
 
 				nodes.add(rankN);
 			}
 
 			if (curSSP != null) {
 				if (curInfratype == null)
 					sspN = tree
 							.getNode(TaxonNode.SUBPOPULATION, curKingdom, curGenus + " " + curSpecies + " " + curSSP);
 				else
 					sspN = tree.getNode(TaxonNode.INFRARANK_SUBPOPULATION, curKingdom, curGenus + " " + curSpecies
 							+ " " + curInfratype + " " + " " + curInfraEpithet + " " + curSSP);
 
 				if (sspN == null) {
 					if (curInfratype == null)
 						sspN = fetchNode(curKingdom, curGenus + " " +  curSpecies + " " +  curSSP);
 					else
 						sspN = fetchNode(curKingdom, curGenus + " " + curSpecies + " " + curInfratype + " " + curInfraEpithet + " " +curSSP);
 
 					if (sspN == null) {
 						sspN = newSimpleNode(curSSP, curStat, TaxonLevel.SUBPOPULATION, speciesN);
 						sspN.setTaxonomicAuthority(spcTaxonomicAuthority);
 
 						if (curInfratype != null) {
 							if (curInfratype.trim().matches("^ssp\\.?$"))
 								rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
 							else if (curInfratype.trim().matches("^var\\.?$")
 									&& rankN.getFootprint()[0].equalsIgnoreCase("PLANTAE"))
 								rankN.setInfratype(Infratype.getInfratype(Infratype.INFRARANK_TYPE_VARIETY));
 							else {
 								failed.set(true);
 								statusMessage.append("Your DEM contains an infrarank with an invalid rank."
 										+ " The valid ranks are \"ssp.\" and \"var.\", and a variety MUST be"
 										+ " in the kingdom PLANTAE. Please change your data, and"
 										+ " try the import again.<br>");
 								return;
 							}
 						}
 
 						if (curInfratype != null)
 							updateFootprint(footprint, rankN);
 						sspN.setFriendlyName(sspN.generateFullName());
 
 						spcNameToIDMap.put(sspN.getFullName(), new Long(specID));
 					}
 
 					sspN.setFriendlyName(sspN.generateFullName());
 					speciesN.getChildren().add(sspN);
 					sspN.getFootprint();
 					tree.addNode(curKingdom, sspN);
 					
 					report(sspN);
 				}
 
 				assessedNodesBySpc_id.put(new Long(specID), sspN);
 				sspN.setHybrid(hybrid);
 				nodes.add(sspN);
 			}
 		}
 		printf("Found %s nodes in the tree", nodes.size());
 		printf("Kingdoms are %s", tree.getKingdoms());
 	}
 //
 //	private void conservationMeasuresTableImport(Long curDEMid, AssessmentData curAssessment,
 //			HashMap<String, Object> data) throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("conservation_measures", "*");
 //		select.constrain(new CanonicalColumnName("conservation_measures", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> consSelected = new HashMap<String, ArrayList<String>>();
 //		HashMap<String, ArrayList<String>> researchSelected = new HashMap<String, ArrayList<String>>();
 //		for (Row curRow : rowLoader.getSet()) {
 //			int curCode = curRow.get("Measure_code").getInteger();
 //			int score = curRow.get("cm_timing").getInteger(Column.NEVER_NULL);
 //
 //			try {
 //				switchToDBSession("demConversion");
 //
 //				if (score == 1) // Do in-place conversion
 //				{
 //					try {
 //						doResearchInPlace(data, curCode);
 //					} catch (DBException ignored) {
 //					}
 //				}// End if in-place needed
 //
 //				if (score == 2) // Do research needed conversion
 //				{
 //					try {
 //						doResearchNeeded(researchSelected, curCode);
 //					} catch (DBException ignored) {
 //					}
 //				}
 //
 //				if (score == 2) // Do conservation needed conversion
 //				{
 //					try {
 //						doConservationNeeded(consSelected, curCode);
 //					} catch (DBException ignored) {
 //					}
 //				}
 //
 //				data.put(CanonicalNames.ConservationActions, consSelected);
 //				data.put(CanonicalNames.Research, researchSelected);
 //
 //				try {
 //					switchToDBSession("dem");
 //				} catch (Exception e) {
 //					e.printStackTrace();
 //					SysDebugger
 //							.getInstance()
 //							.println(
 //									"Error switching back to DEM after building Conservation actions. Import will assuredly fail.");
 //				}
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //				SysDebugger.getInstance().println("Error checking against conversion table.");
 //			}
 //		}
 //	}
 //
 //	private void countryTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws Exception {
 //		SelectQuery select = new SelectQuery();
 //		select.select("coding_occurence", "*");
 //		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
 //				COUNTRY_CODING_OCCURRENCE_TYPE);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String country_number = curRow.get("obj_id").getString(Column.NEVER_NULL);
 //
 //			ArrayList<String> dataList = new ArrayList<String>();
 //			fetchCodingOccurrenceData(curRow, dataList);
 //
 //			selected.put(fetchCountryIsoCode(country_number), dataList);
 //		}
 //		switchToDBSession("dem");
 //
 //		select = new SelectQuery();
 //		select.select("coding_occurence", "*");
 //		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
 //				SUBCOUNTRY_CODING_OCCURRENCE_TYPE);
 //
 //		rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String subcountry_number = curRow.get("obj_id").getString(Column.NEVER_NULL);
 //
 //			ArrayList<String> dataList = new ArrayList<String>();
 //			fetchCodingOccurrenceData(curRow, dataList);
 //
 //			selected.put(fetchSubcountryIsoCode(subcountry_number), dataList);
 //		}
 //
 //		switchToDBSession("dem");
 //		data.put(CanonicalNames.CountryOccurrence, selected);
 //	}
 	
 	private void addTextPrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
 		if (isBlank(value))
 			return;
 		
 		Field field = new Field(fieldName, assessment);
 		
 		ProxyField proxy = new ProxyField(field);
 		proxy.setTextPrimitiveField(dataPoint, value);
 		
 		assessment.getField().add(field);
 	}
 	
 	private void addStringPrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
 		if (isBlank(value))
 			return;
 		
 		Field field = new Field(fieldName, assessment);
 		
 		ProxyField proxy = new ProxyField(field);
 		proxy.setStringPrimitiveField(dataPoint, value);
 		
 		assessment.getField().add(field);
 	}
 	
 	private void addDatePrimitiveField(String fieldName, String dataPoint, Assessment assessment, Date value) {
 		if (value == null)
 			return;
 		
 		Field field = new Field(fieldName, assessment);
 		ProxyField proxy = new ProxyField(field);
 		proxy.setDatePrimitiveField(dataPoint, value);
 		
 		assessment.getField().add(field);
 	}
 	
 	private void addBooleanRangePrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
 		if (isBlank(value))
 			return;
 		
 		String dataValue = value;
 		if ("2".equals(dataValue))
 			dataValue = BooleanRangePrimitiveField.UNKNOWN;
 		
 		Field field = new Field(fieldName, assessment);
 		ProxyField proxy = new ProxyField(field);
 		proxy.setBooleanRangePrimitiveField(dataPoint, dataValue);
 		
 		assessment.getField().add(field);
 	}
 	
 	private void addRangePrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
 		if (isBlank(value))
 			return;
 		
 		if (!isValidRangeFormat(value)) {
 			/*sendNote(curAssessment.getType(), CANNED_NOTE + locations, curAssessment.getAssessmentID(),
 					CanonicalNames.LocationsNumber);*/
 		}
 		
 		Field field = new Field(fieldName, assessment);
 		
 		ProxyField proxy = new ProxyField(field);
 		proxy.setRangePrimitiveField(dataPoint, value);
 		
 		assessment.getField().add(field);
 	}
 	
 	private Field addFloatPrimitiveField(String fieldName, String dataPoint, Assessment assessment, String value) {
 		final Float floatValue;
 		try {
 			floatValue = Float.valueOf(value);
 		} catch (NullPointerException e) {
 			return null;
 		} catch (NumberFormatException e) {
 			return null;
 		}
 		
 		Field field = new Field(fieldName, assessment);
 		
 		ProxyField proxy = new ProxyField(field);
 		proxy.setFloatPrimitiveField(dataPoint, floatValue);
 		
 		assessment.getField().add(field);
 		
 		return field;
 	}
 	
 	private Field addFieldForCheckedOptions(String fieldName, String dataPoint, Row row, Assessment assessment, String... options) throws DBException {
 		ArrayList<Integer> selection = new ArrayList<Integer>();
 		for (String option : options) {
 			boolean checked = Integer.valueOf(1).equals(row.get(option).getInteger());
 			if (checked) {
 				Integer value = getIndex(fieldName, dataPoint, option, null);
 				if (value != null)
 					selection.add(value);
 			}
 		}
 		
 		return addFieldForCheckedOptions(fieldName, dataPoint, row, assessment, selection);
 	}
 	
 	private Field addFieldForCheckedOptions(String fieldName, String dataPoint, Row row, Assessment assessment, List<Integer> selection) throws DBException {
 		if (!selection.isEmpty()) {
 			Field field = new Field(fieldName, assessment);
 			
 			ProxyField proxy = new ProxyField(field);
 			proxy.setForeignKeyListPrimitiveField(dataPoint, selection, fieldName + "_" + dataPoint + "Lookup");
 			
 			assessment.getField().add(field);
 			
 			return field;
 		}
 		else
 			return null;
 	}
 
 	private void distributionTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
 		List<Row> rows = queryDEM("Distribution", curDEMid);
 		if (rows.isEmpty())
 			return;
 		
 		Row row = rows.get(0);
 
 		// Biogeographic Realm
 		addFieldForCheckedOptions(CanonicalNames.BiogeographicRealm, "realm", row, curAssessment,
 				"Afrotropical", "Antarctic", "Australasian", "Indomalayan", 
 				"Nearctic", "Neotropical", "Oceanian", "Palearctic");
 		
 		addFieldForCheckedOptions(CanonicalNames.System, "value", row, curAssessment,
 				"Terrestrial", "Freshwater", "Marine");
 
 		//FIXME: update to use MovementPatterns and Congregatory fields.
 		/*
 		// Movement Patterns
 		dataList = new ArrayList<String>();
 		String movementPatterns = "";
 		if (row.get("Nomadic").getInteger(Column.NEVER_NULL) == 1)
 			movementPatterns += "1,";
 		if (row.get("Congregatory").getInteger(Column.NEVER_NULL) == 1)
 			movementPatterns += "2,";
 		if (row.get("Migratory").getInteger(Column.NEVER_NULL) == 1)
 			movementPatterns += "3,";
 		if (row.get("Altitudinally_migrant").getInteger(Column.NEVER_NULL) == 1)
 			movementPatterns += "4,";
 		if (movementPatterns.endsWith(","))
 			movementPatterns = movementPatterns.substring(0, movementPatterns.length() - 1);
 		else
 			movementPatterns = "0";
 
 		dataList.add(movementPatterns);
 		data.put(CanonicalNames.MovementPatterns, dataList);
 		*/
 		
 		// Map Status
 		String mapStatus = row.get("Map_status").toString();
 		Integer mapStatusSelected = getIndex(CanonicalNames.MapStatus, "status", mapStatus, null);
 		if (mapStatusSelected != null) {
 			Field field = new Field(CanonicalNames.MapStatus, curAssessment);
 			
 			ProxyField proxy = new ProxyField(field);
 			proxy.setForeignKeyPrimitiveField("status", mapStatusSelected, CanonicalNames.MapStatus + "_statusLookup");
 			
 			curAssessment.getField().add(field);
 		}
 
 		addFloatPrimitiveField(CanonicalNames.ElevationLower, "limit", curAssessment, row.get("lower_elev").toString());
 		addFloatPrimitiveField(CanonicalNames.ElevationUpper, "limit", curAssessment, row.get("upper_elev").toString());
 		addFloatPrimitiveField(CanonicalNames.DepthUpper, "limit", curAssessment, row.get("upper_depth").toString());
 		addFloatPrimitiveField(CanonicalNames.DepthLower, "limit", curAssessment, row.get("lower_depth").toString());
 		
 		// DepthZone by hand since the options don't map up
 		List<Integer> depthZoneSelection = new ArrayList<Integer>();
 		int index = 1;
 		for (String key : new String[] { "Shallow_photic", "Photic", "Bathyl", "Abyssal", "Hadal" }) {
 			if (row.get(key) != null && Integer.valueOf(1).equals(row.get(key).getInteger())) {
 				depthZoneSelection.add(index);
 			}
 			index++;
 		}
 		addFieldForCheckedOptions(CanonicalNames.DepthZone, "depthZone", row, curAssessment, depthZoneSelection);
 
 		addRangePrimitiveField(CanonicalNames.AOO, "range", curAssessment, row.get("AOO").toString());
 		addRangePrimitiveField(CanonicalNames.EOO, "range", curAssessment, row.get("EOO").toString());
 	}
 //
 //	private void doConservationNeeded(HashMap<String, ArrayList<String>> consSelected, int curCode) throws DBException {
 //		String notes;
 //		// DO Straight conversion
 //		SelectQuery conversionSelect = new SelectQuery();
 //		conversionSelect.select("CONS ACTIONS MAPPING OLD TO NEW", "*");
 //		conversionSelect.constrain(new CanonicalColumnName("CONS ACTIONS MAPPING OLD TO NEW", "oldActionID"),
 //				QConstraint.CT_EQUALS, curCode);
 //
 //		Row.Loader conversionRow = new Row.Loader();
 //
 //		ec.doQuery(conversionSelect, conversionRow);
 //
 //		if (conversionRow.getRow() != null) {
 //			curCode = conversionRow.getRow().get("newActionID").getInteger();
 //			notes = conversionRow.getRow().get("Comment").getString(Column.NEVER_NULL);
 //
 //			if (!consSelected.containsKey("" + curCode))
 //				consSelected.put("" + curCode, createDataArray(notes, true));
 //			else if (!notes.equals("")) {
 //				String temp = consSelected.get("" + curCode).get(0);
 //				temp += " --- \n" + XMLUtils.clean(notes);
 //				consSelected.put("" + curCode, createDataArray(temp, true));
 //			}
 //		}
 //	}
 //
 //	private void doResearchInPlace(HashMap<String, Object> data, int curCode) throws DBException {
 //		String notes;
 //		SelectQuery conversionSelect = new SelectQuery();
 //		conversionSelect.select("OldConsActions to new Actions IN Place", "*");
 //		conversionSelect.constrain(new CanonicalColumnName("OldConsActions to new Actions IN Place", "oldaction"),
 //				QConstraint.CT_EQUALS, curCode);
 //
 //		Row.Loader conversionRow = new Row.Loader();
 //
 //		ec.doQuery(conversionSelect, conversionRow);
 //
 //		if (conversionRow.getRow() != null) {
 //			curCode = conversionRow.getRow().get("newaction").getInteger();
 //			notes = conversionRow.getRow().get("comment").getString(Column.NEVER_NULL);
 //
 //			if (curCode > 0 && curCode < 3) {
 //				ArrayList<String> dataList;
 //
 //				if (data.containsKey(CanonicalNames.InPlaceResearch))
 //					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceResearch);
 //				else
 //					dataList = createDataArray("0", "", "0", "");
 //
 //				if (curCode == 1)
 //					curCode--;
 //
 //				dataList.remove(curCode);
 //				dataList.add(curCode, "1");
 //				dataList.remove(curCode + 1);
 //				dataList.add(curCode + 1, notes);
 //
 //				data.put(CanonicalNames.InPlaceResearch, dataList);
 //			} else if (curCode >= 3 && curCode < 9) {
 //				curCode -= 3;
 //
 //				ArrayList<String> dataList;
 //
 //				if (data.containsKey(CanonicalNames.InPlaceLandWaterProtection))
 //					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceLandWaterProtection);
 //				else
 //					dataList = createDataArray(new String[] { "0", "0", "", "0", "", "", "0", "" });
 //
 //				if (curCode == 5)
 //					curCode++;
 //
 //				dataList.remove(curCode);
 //				dataList.add(curCode, "1");
 //				if (curCode < 4) {
 //					dataList.remove(curCode + 2);
 //					dataList.add(curCode + 2, notes);
 //				} else {
 //					dataList.remove(curCode + 1);
 //					dataList.add(curCode + 1, notes);
 //				}
 //
 //				data.put(CanonicalNames.InPlaceLandWaterProtection, dataList);
 //			} else if (curCode >= 9 && curCode < 12) {
 //				curCode -= 9;
 //
 //				ArrayList<String> dataList;
 //
 //				if (data.containsKey(CanonicalNames.InPlaceSpeciesManagement))
 //					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceSpeciesManagement);
 //				else
 //					dataList = createDataArray(new String[] { "0", "", "0", "", "0", "" });
 //
 //				curCode = curCode * 2;
 //
 //				dataList.remove(curCode);
 //				dataList.add(curCode, "1");
 //				dataList.remove(curCode + 1);
 //				dataList.add(curCode + 1, notes);
 //
 //				data.put(CanonicalNames.InPlaceSpeciesManagement, dataList);
 //			} else if (curCode >= 12 && curCode < 15) {
 //				curCode -= 12;
 //
 //				ArrayList<String> dataList;
 //
 //				if (data.containsKey(CanonicalNames.InPlaceEducation))
 //					dataList = (ArrayList<String>) data.get(CanonicalNames.InPlaceEducation);
 //				else
 //					dataList = createDataArray(new String[] { "0", "", "0", "", "0", "" });
 //
 //				curCode = curCode * 2;
 //
 //				dataList.remove(curCode);
 //				dataList.add(curCode, "1");
 //				dataList.remove(curCode + 1);
 //				dataList.add(curCode + 1, notes);
 //
 //				data.put(CanonicalNames.InPlaceEducation, dataList);
 //			}
 //		}
 //	}
 //
 //	private void doResearchNeeded(HashMap<String, ArrayList<String>> researchSelected, int curCode) throws DBException {
 //		String notes;
 //		SelectQuery conversionSelect = new SelectQuery();
 //		conversionSelect.select("OLD CONS ACT to RESEARCH MAPPING", "*");
 //		conversionSelect.constrain(new CanonicalColumnName("OLD CONS ACT to RESEARCH MAPPING", "old ActionAFID"),
 //				QConstraint.CT_EQUALS, curCode);
 //
 //		Row.Loader conversionRow = new Row.Loader();
 //
 //		ec.doQuery(conversionSelect, conversionRow);
 //
 //		if (conversionRow.getRow() != null) {
 //			curCode = conversionRow.getRow().get("newResearchAFID").getInteger();
 //			notes = conversionRow.getRow().get("comments").getString(Column.NEVER_NULL);
 //
 //			if (!researchSelected.containsKey("" + curCode))
 //				researchSelected.put("" + curCode, createDataArray(notes, true));
 //			else if (!notes.equals("")) {
 //				String temp = researchSelected.get("" + curCode).get(0);
 //				temp += " --- \n" + XMLUtils.clean(notes);
 //				researchSelected.put("" + curCode, createDataArray(temp, true));
 //			}
 //		}
 //	}
 //
 //	private void ecosystemServicesTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("ecosystem_services", "*");
 //		select.constrain(new CanonicalColumnName("ecosystem_services", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			int curCode = 1;
 //			int curRank;
 //			String curScale;
 //
 //			data.put(CanonicalNames.EcosystemServicesInsufficientInfo, createDataArray(""
 //					+ (curRow.get("info_available").getInteger(Column.NEVER_NULL) == 1), true));
 //			data.put(CanonicalNames.EcosystemServicesProvidesNone, createDataArray(""
 //					+ (curRow.get("no_services").getInteger(Column.NEVER_NULL) == 1), true));
 //
 //			for (int i = 4; i < 30; i += 2) {
 //				curRank = curRow.get(i).getInteger(Column.NEVER_NULL);
 //				curScale = curRow.get(i + 1).getString(Column.NEVER_NULL);
 //				if (curScale.equals(""))
 //					curScale = "0";
 //
 //				selected.put("" + curCode, createDataArray("" + curRank, "" + curScale));
 //				curCode++;
 //			}
 //
 //			if (curRow.get("specify_other1").getString() != null) {
 //				curRank = curRow.get("Other_rank1").getInteger(Column.NEVER_NULL);
 //				curScale = curRow.get("Other_scale1").getString(Column.NEVER_NULL);
 //				if (curScale.equals(""))
 //					curScale = "0";
 //
 //				String content = curRow.get("specify_other1").getString();
 //				selected.put("" + curCode, createDataArray("" + curRank, "" + curScale));
 //				curCode++;
 //
 //				sendNote(curAssessment.getType(), "Other 1 specified as: " + content, curAssessment.getAssessmentID(),
 //						CanonicalNames.EcosystemServices);
 //			}
 //			if (curRow.get("specify_other2").getString() != null) {
 //				curRank = curRow.get("Other_rank2").getInteger(Column.NEVER_NULL);
 //				curScale = curRow.get("Other_scale2").getString(Column.NEVER_NULL);
 //				if (curScale.equals(""))
 //					curScale = "0";
 //
 //				String content = curRow.get("specify_other2").getString();
 //				selected.put("" + curCode, createDataArray("" + curRank, "" + curScale));
 //				curCode++;
 //
 //				sendNote(curAssessment.getType(), "Other 2 specified as: " + content, curAssessment.getAssessmentID(),
 //						CanonicalNames.EcosystemServices);
 //			}
 //		}
 //
 //		data.put(CanonicalNames.EcosystemServices, selected);
 //	}
 //
 	private void exportAssessments() throws Exception {
 		session.beginTransaction();
 		
 		Map<Integer, Region> regionCache = new HashMap<Integer, Region>();
 		
 		Set<Region> regions = new HashSet<Region>();
 		Set<Taxon> taxa = new HashSet<Taxon>();
 		
 		if (!successfulAssessments.isEmpty()) {
 			printf(" - Saving %s assessments", successfulAssessments.size());
 			for (Assessment assessment : successfulAssessments) {
 				taxa.add(assessment.getTaxon());
 				for (Integer regionID : assessment.getRegionIDs()) {
 					Region region;
 					if (regionCache.containsKey(regionID))
 						region = regionCache.get(regionID);
 					else {
 						Region r = regionIO.getRegion(regionID);
 						regionCache.put(regionID, r);
 						region = r;
 					}
 						
 					if (region != null)
 						regions.add(region);
 				}
 				
 				AssessmentIOWriteResult result = assessmentIO.saveNewAssessment(assessment, userO);
 				if (!result.status.isSuccess())
 					println("Error putting draft assessment for " + assessment.getSpeciesName());
 			}
 		}
 		
 		if (!failedAssessments.isEmpty()) {
 			printf("====== Failed Assessments (%s) ======", failedAssessments.size());
 			for (Assessment assessment : failedAssessments) {
 				if (assessment.isGlobal())
 					printf("A global draft assessment for the species %s" +  
 							" already exists. Remove the species from the DEM, or delete the assessment in SIS" +
 							" so the import can succeed.", assessment.getSpeciesName());
 				else
 					printf("A regional draft assessment for the species %s" +
 							" with the region specified in the DEM already exists. Remove the species" +
 							" from the DEM, or delete the assessment in SIS so the import can.", 
 							assessment.getSpeciesName());
 			}
 			print("============");
 		}
 		
 		WorkingSet ws = new WorkingSet();
 		ws.setCreatedDate(Calendar.getInstance().getTime());
 		ws.setName("DEM Import for " + userO.getUsername() + " on " + FormattedDate.impl.getDate(ws.getCreatedDate()));
 		ws.setDescription(ws.getName());
		ws.getAssessmentTypes().add(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
 		ws.setCreator(userO);
 		ws.getUsers().add(userO);
 		ws.setIsMostRecentPublished(false);
 		ws.setRelationship(Relationship.fromName(Relationship.OR));
 		
 		ws.setRegion(regions);
 		ws.setTaxon(taxa);
 		
 		boolean saved = workingSetIO.saveWorkingSet(ws, userO, "Working Set Created via DEM Import");
 		if (!saved)
 			println("Error putting new working set!");
 		
 		session.getTransaction().commit();
 	}
 
 	private void exportNodes() throws Exception {
 		log.append("<tr><td align=\"center\" colspan=\"9\">" + "<b><u>Newly Created Taxa</u></b></td></tr>");
 		session.beginTransaction();
 		for (Kingdom curKingdom : tree.getKingdoms().values()) {
 			Taxon kingdom = curKingdom.getTheKingdom();
 			if (kingdom.getState() == STATE_TO_IMPORT) {
 				if (allowCreate)
 					throw new Exception("Creating taxa only allowed at the Genus level and below.");
 				
 				kingdom.setState(Taxon.ACTIVE);
 				printf("Saving new kingdom with id %s", kingdom.getId());
 				session.save(kingdom);
 				logNode(kingdom);
 			}
 
 			for (HashMap<String, Taxon> curLevel : curKingdom.getLevels()) {
 				for (Taxon cur : curLevel.values()) {
 					if (cur.getState() == STATE_TO_IMPORT) // SUBMIT IT
 					{	
 						if (!allowCreate && cur.getLevel() < TaxonLevel.GENUS)
 							throw new Exception("Creating taxa only allowed at the Genus level and below.");
 						
 						cur.setState(Taxon.ACTIVE);
 						session.save(cur);
 						//taxomaticIO.saveNewTaxon(cur, userO);
 						logNode(cur);
 					} else if (cur.getState() == STATE_CHANGED) {
 						cur.setState(Taxon.ACTIVE);
 						
 						Hibernate.initialize(cur.getEdits());
 						taxonIO.writeTaxon(cur, userO, "DEM Import.");
 						
 						/*if( ) {
 							failed.set(true);
 							statusMessage.append("Failed to save changes to an existing taxon, " + cur.getFullName()
 									+ ".<br>");
 							statusMessage.append("Please forward this message: <br>");
 							statusMessage.append("Failed TaxaIO call!<br>");
 							statusMessage.append("with XML " + XMLUtils.clean(curXML) + "<br>");
 							return;
 						}*/
 					}
 				}
 			}
 		}
 		session.getTransaction().commit();
 	}
 //
 //	private void faoMarineTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws Exception {
 //		SelectQuery select = new SelectQuery();
 //		select.select("coding_occurence", "*");
 //		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
 //				FAO_CODING_OCCURRENCE_TYPE);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("obj_id").getString();
 //
 //			ArrayList<String> dataList = new ArrayList<String>();
 //			fetchCodingOccurrenceData(curRow, dataList);
 //
 //			selected.put(curCode, dataList);
 //		}
 //
 //		data.put(CanonicalNames.FAOOccurrence, selected);
 //	}
 //
 //	private void fetchCodingOccurrenceData(Row codingRow, ArrayList<String> dataList) throws Exception {
 //		String p_code = "0";
 //		String o_code = "0";
 //		String m_code = "0";
 //
 //		if (codingRow != null) {
 //			p_code = codingRow.get("p_code").getString(Column.NEVER_NULL);
 //			o_code = codingRow.get("o_code").getString(Column.NEVER_NULL);
 //			m_code = codingRow.get("m_code").getString(Column.NEVER_NULL);
 //
 //			if (p_code.equals("9"))
 //				p_code = "6";
 //			if (o_code.equals("9"))
 //				o_code = "5";
 //		}
 //
 //		dataList.add(p_code);
 //		dataList.add(new Boolean(m_code.equals("1")).toString());
 //		dataList.add(o_code);
 //	}
 //
 //	private String fetchCountryIsoCode(String country_number) throws Exception {
 //		switchToDBSession("demSource");
 //
 //		SelectQuery select = new SelectQuery();
 //		select.select("countries_list_all", "Country_code");
 //		select.select("countries_list_all", "Country_Number");
 //		select.constrain(new CanonicalColumnName("countries_list_all", "Country_Number"), QConstraint.CT_EQUALS,
 //				country_number);
 //
 //		Row.Loader code = new Row.Loader();
 //		ec.doQuery(select, code);
 //
 //		return code.getRow().get("Country_code").getString().trim();
 //	}
 //
 	private Taxon fetchNode(String kingdomName, String fullName) throws Exception {
 		Taxon existing = taxonIO.readTaxonByName(kingdomName, fullName);
 		if (existing != null)
 			printf("Found existing taxon %s with id %s", existing.getFullName(), existing.getId());
 		return existing;
 		//FIXME: 
 		/*String id = TaxonomyDocUtils.getIDByName(kingdomName, fullNameNoSpaces.replaceAll("\\s", ""));
 		
 		if (id != null) {
 			return TaxaIO.readNode(id, SISContainerApp.getStaticVFS());
 		} else
 			return null;*/
 	}
 //
 //	private String fetchSubcountryIsoCode(String subcountry_number) throws Exception {
 //		switchToDBSession("demSource");
 //
 //		SelectQuery select = new SelectQuery();
 //		select.select("subcountry_list_all", "BruLevel4Code");
 //		select.select("subcountry_list_all", "subcountry_number");
 //		select.constrain(new CanonicalColumnName("subcountry_list_all", "subcountry_number"), QConstraint.CT_EQUALS,
 //				subcountry_number);
 //
 //		Row.Loader code = new Row.Loader();
 //		ec.doQuery(select, code);
 //
 //		return code.getRow().get("BruLevel4Code").getString().trim();
 //	}
 //
 //	private void growthFormTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("growth_form_table", "*");
 //		select.constrain(new CanonicalColumnName("growth_form_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("Growthform_code").getString();
 //			selected.put(curCode, new ArrayList<String>());
 //		}
 //
 //		data.put(CanonicalNames.PlantGrowthForms, selected);
 //	}
 	
 	private String fmtLookupTableName(String field, String dataPoint) {
 		return field + "_" + dataPoint + "Lookup";
 	}
 //
 	private void habitatTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
 		List<Row> rows = queryDEM("General_habitat", curDEMid);
 		if (rows.isEmpty())
 			return;
 
 		Field field = new Field(CanonicalNames.GeneralHabitats, curAssessment);
 		
 		for (Row curRow : rows) {
 			String fieldName = CanonicalNames.GeneralHabitats + "Subfield";
 			String curCode = curRow.get("Gh_N").getString();
 			
 			Integer lookup = getIndex(fieldName, curCode, null);
 			if (lookup == null) {
 				printf("No lookup to match habitat %s", curCode);
 				continue;
 			}
 			
 			Integer majorImportance = curRow.get("major_importance").getInteger();
 			Integer suitability = curRow.get("Score").getInteger();
 
 			if (suitability != null && suitability == 9)
 				suitability = 3; // 3 is index of "possible" answer
 
 			if (majorImportance != null) {
 				if (majorImportance == 1)
 					majorImportance = null;
 				else if (majorImportance > 0) // 3 is index of "No", 2 of "Yes"
 					majorImportance--;
 			}
 			
 			Field subfield = new Field(fieldName, null);
 			
 			ProxyField proxy = new ProxyField(subfield);
 			proxy.setForeignKeyPrimitiveField(CanonicalNames.GeneralHabitats + "Lookup", lookup, CanonicalNames.GeneralHabitats + "Lookup");
 			proxy.setForeignKeyPrimitiveField("suitability", suitability, fmtLookupTableName(CanonicalNames.GeneralHabitats, "suitability"));
 			proxy.setForeignKeyPrimitiveField("majorImportance", majorImportance, fmtLookupTableName(CanonicalNames.GeneralHabitats, "majorImportance"));
 
 			subfield.setParent(field);
 			field.getFields().add(subfield);
 		}
 
 		curAssessment.getField().add(field);
 	}
 //
 	private boolean isValidRangeFormat(String text) {
 		boolean ret = text.equals("")
 				|| text.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?")
 				|| text.matches("(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*(-)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*"
 						+ "(,)(\\s)*(\\d)*(\\.)?(\\d)+(\\s)*((-)((\\s)*(\\d)*(\\.)?(\\d)+))?");
 
 		// SysDebugger.getInstance().println("Checking against text " + text +
 		// " : result is " + ret );
 
 		return ret;
 	}
 //
 //	private void lakesTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("lakes_table", "*");
 //		select.constrain(new CanonicalColumnName("lakes_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("lake_number").getString();
 //
 //			ArrayList<String> dataList = new ArrayList<String>();
 //			dataList.add("0");
 //			dataList.add("false");
 //			dataList.add("0");
 //
 //			selected.put(curCode, dataList);
 //		}
 //
 //		data.put(CanonicalNames.Lakes, selected);
 //	}
 //
 //	private void landCoverTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("Land_cover", "*");
 //		select.constrain(new CanonicalColumnName("Land_cover", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("Lc_N").getString();
 //			int score = curRow.get("Score").getInteger(Column.NEVER_NULL);
 //
 //			if (score == 9)
 //				score = 3; // 3 is index of possible answer
 //
 //			ArrayList<String> landCoverData = new ArrayList<String>();
 //			landCoverData.add("" + score);
 //
 //			selected.put(curCode, landCoverData);
 //		}
 //
 //		data.put(CanonicalNames.LandCover, selected);
 //	}
 //
 //	private void lifeHistoryTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("life_history", "*");
 //		select.constrain(new CanonicalColumnName("life_history", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Loader rowLoader = new Row.Loader();
 //
 //		try {
 //			ec.doQuery(select, rowLoader);
 //		} catch (Exception e) {
 //			// NO LIFE HISTORY RECORDS FOR THIS BABY
 //			return;
 //		}
 //
 //		Row row = rowLoader.getRow();
 //
 //		if (row == null)
 //			return;
 //
 //		// AGE MATURITY UNITS - FOR BOTH FEMALE AND MALE
 //		String units = row.get("age_maturity_units").getString(Column.NEVER_NULL);
 //		if (units.equals(""))
 //			units = "0";
 //		else if (units.equalsIgnoreCase("days"))
 //			units = "1";
 //		else if (units.equalsIgnoreCase("weeks"))
 //			units = "2";
 //		else if (units.equalsIgnoreCase("months"))
 //			units = "3";
 //		else if (units.equalsIgnoreCase("years"))
 //			units = "4";
 //		else {
 //			String message = "Unparseable units value from DEM import on "
 //					+ DateFormatUtils.ISO_DATE_FORMAT.format(new Date()) + ". Imported value: " + units;
 //
 //			try {
 //				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
 //						CanonicalNames.FemaleMaturityAge);
 //				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
 //						CanonicalNames.MaleMaturityAge);
 //
 //				units = "";
 //			} catch (Exception e) {
 //				print("Unable to attach note: stack trace follows.");
 //				e.printStackTrace();
 //				units = "";
 //			}
 //		}
 //
 //		data.put(CanonicalNames.FemaleMaturityAge, createDataArray(row.get("f_age_maturity").getString(
 //				Column.NEVER_NULL), units));
 //
 //		data.put(CanonicalNames.MaleMaturityAge, createDataArray(
 //				row.get("m_age_maturity").getString(Column.NEVER_NULL), units));
 //
 //		// FEMALE MATURITY SIZE
 //		data.put(CanonicalNames.FemaleMaturitySize, createDataArray(row.get("f_size_maturity").getString(
 //				Column.NEVER_NULL), true));
 //
 //		// MALE MATURITY SIZE
 //		data.put(CanonicalNames.MaleMaturitySize, createDataArray(row.get("m_size_maturity").getString(
 //				Column.NEVER_NULL), true));
 //
 //		// MAX SIZE
 //		data.put(CanonicalNames.MaxSize, createDataArray(row.get("max_size").getString(Column.NEVER_NULL), true));
 //
 //		// Birth SIZE
 //		data.put(CanonicalNames.BirthSize, createDataArray(row.get("birth_size").getString(Column.NEVER_NULL), true));
 //
 //		// GENERATION LENGTH
 //		String generations = row.get("Gen_len").getString(Column.NEVER_NULL);
 //		String genJust = XMLUtils.clean(row.get("Gen_Len_Just").getString(Column.NEVER_NULL));
 //		
 //		String mod_generations = generations.toLowerCase();
 //		mod_generations = mod_generations.replaceAll("\\s", "");
 //		mod_generations = mod_generations.replaceAll("years", "");
 //		mod_generations = mod_generations.replaceAll("days", "");
 //		mod_generations = mod_generations.replaceAll("weeks", "");
 //		mod_generations = mod_generations.replaceAll("months", "");
 //
 //		String leftovers = generations.replace(mod_generations, "");
 //		if( !leftovers.replaceAll("\\s", "").equals("") )
 //			genJust = XMLUtils.clean(leftovers.trim()) + (genJust.equals("") ? "" : " -- " + genJust);
 //		
 //		if (!isValidRangeFormat(mod_generations))
 //			sendNote(curAssessment.getType(), CANNED_NOTE + generations + " with justification: " + genJust, curAssessment.getAssessmentID(),
 //					CanonicalNames.GenerationLength);
 //		else
 //			data.put(CanonicalNames.GenerationLength, createDataArray(mod_generations, genJust));
 //
 //		// REPRODUCTIVE PERIODICITY
 //		data.put(CanonicalNames.ReproduictivePeriodicity, createDataArray(row.get("reproductive_periodicity")
 //				.getString(Column.NEVER_NULL), true));
 //
 //		// LITTER SIZE
 //		data.put(CanonicalNames.AvgAnnualFecundity, createDataArray(
 //				row.get("litter_size").getString(Column.NEVER_NULL), true));
 //
 //		// POPULATION INCREASE RATE
 //		data.put(CanonicalNames.PopulationIncreaseRate, createDataArray(row.get("rate_pop_increase").getString(
 //				Column.NEVER_NULL), true));
 //
 //		// MORTALITY
 //		data.put(CanonicalNames.NaturalMortality, createDataArray(row.get("mortality").getString(Column.NEVER_NULL),
 //				true));
 //
 //		// EGG LAYING
 //		data.put(CanonicalNames.EggLaying, createDataArray(
 //				row.get("egg_laying").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));
 //
 //		// LARVAL
 //		data.put(CanonicalNames.FreeLivingLarvae, createDataArray(
 //				row.get("larval").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));
 //
 //		// LIVE YOUNG
 //		data.put(CanonicalNames.LiveBirth, createDataArray(
 //				row.get("live_young").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));
 //
 //		// PARTHENOGENSIS
 //		data.put(CanonicalNames.Parthenogenesis, createDataArray(row.get("parthenogenesis").getInteger(
 //				Column.NEVER_NULL) == 1 ? "true" : "false", true));
 //
 //		// WATER BREEDING
 //		data.put(CanonicalNames.WaterBreeding, createDataArray(
 //				row.get("water_breeding").getInteger(Column.NEVER_NULL) == 1 ? "true" : "false", true));
 //
 //		// LONGEVITY
 //		units = row.get("longevity_units").getString(Column.NEVER_NULL);
 //		if (units.equals(""))
 //			units = "0";
 //		else if (units.equalsIgnoreCase("days"))
 //			units = "1";
 //		else if (units.equalsIgnoreCase("months"))
 //			units = "2";
 //		else if (units.equalsIgnoreCase("years"))
 //			units = "3";
 //		else {
 //			String message = "Unparseable units value from DEM import on "
 //					+ DateFormatUtils.ISO_DATE_FORMAT.format(new Date()) + ". Imported value: " + units;
 //
 //			try {
 //				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(), CanonicalNames.Longevity);
 //				units = "";
 //			} catch (Exception e) {
 //				print("Unable to attach note: stack trace follows.");
 //				e.printStackTrace();
 //				units = "";
 //			}
 //		}
 //		data.put(CanonicalNames.Longevity, createDataArray(row.get("longevity").getString(Column.NEVER_NULL), units));
 //
 //		// REPRODUCTIVE AGE
 //		units = row.get("ave_reproductive_age_units").getString(Column.NEVER_NULL);
 //		if (units.equals(""))
 //			units = "0";
 //		else if (units.equalsIgnoreCase("days"))
 //			units = "1";
 //		else if (units.equalsIgnoreCase("months"))
 //			units = "2";
 //		else if (units.equalsIgnoreCase("years"))
 //			units = "3";
 //		else {
 //			String message = "Unparseable units value from DEM import on "
 //					+ DateFormatUtils.ISO_DATE_FORMAT.format(new Date()) + ". Imported value: " + units;
 //
 //			try {
 //				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
 //						CanonicalNames.AvgReproductiveAge);
 //				units = "";
 //			} catch (Exception e) {
 //				print("Unable to attach note: stack trace follows.");
 //				e.printStackTrace();
 //				units = "";
 //			}
 //		}
 //		data.put(CanonicalNames.AvgReproductiveAge, createDataArray(row.get("ave_reproductive_age").getString(
 //				Column.NEVER_NULL), units));
 //
 //		// GESTATION
 //		units = row.get("gestation_units").getString(Column.NEVER_NULL);
 //		if (units.equals(""))
 //			units = "0";
 //		else if (units.equalsIgnoreCase("days"))
 //			units = "1";
 //		else if (units.equalsIgnoreCase("months"))
 //			units = "2";
 //		else if (units.equalsIgnoreCase("years"))
 //			units = "3";
 //		else {
 //			String message = "Unparseable units value from DEM import on "
 //					+ DateFormatUtils.ISO_DATE_FORMAT.format(new Date()) + ". Imported value: " + units;
 //
 //			try {
 //				sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(),
 //						CanonicalNames.GestationTime);
 //				units = "";
 //			} catch (Exception e) {
 //				print("Unable to attach note: stack trace follows.");
 //				e.printStackTrace();
 //				units = "";
 //			}
 //		}
 //		data.put(CanonicalNames.GestationTime,
 //				createDataArray(row.get("gestation").getString(Column.NEVER_NULL), units));
 //	}
 //
 //	private void livelihoodsTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("Livelihoods", "*");
 //		select.constrain(new CanonicalColumnName("Livelihoods", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		ArrayList<String> selected = new ArrayList<String>();
 //
 //		int numSelected = 0;
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			ArrayList<String> curLivelihood = SISLivelihoods.generateDefaultDataList();
 //
 //			boolean noInformation = curRow.get("data_available").getInteger(Column.NEVER_NULL) == 1;
 //			selected.add("" + noInformation);
 //
 //			int count = 0;
 //			Integer scale = curRow.get("Assess_type_ID").getInteger(Column.NEVER_NULL);
 //			curLivelihood.set(count++, scale.toString());
 //
 //			String regionName = curRow.get("Assess_name").getString(Column.NEVER_NULL);
 //			curLivelihood.set(count++, regionName);
 //			curLivelihood.set(count++, curRow.get("Assess_date").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_product").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_Single_harvest_amount").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("P_Single_harvest_amount_unit").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_Multi_harvest_amount").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_Multi_harvest_amount_unit").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_harvest_percent").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_Multi_amount").getString(Column.NEVER_NULL));
 //
 //			curLivelihood.set(count++, curRow.get("p_human_reliance").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_harvest_gender").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_harvest_socioeconomic").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_other_harvest_socioeconomic").getString(Column.NEVER_NULL));
 //
 //			curLivelihood.set(count++, curRow.get("p_involve_percent").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_household_consumption_percent").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_household_income_percent").getString(Column.NEVER_NULL));
 //			curLivelihood.set(count++, curRow.get("p_cash_income").getString(Column.NEVER_NULL));
 //
 //			selected.addAll(curLivelihood);
 //			numSelected++;
 //		}
 //
 //		if (selected.size() == 0) {
 //			selected.add("false");
 //			selected.add("0");
 //		} else
 //			selected.add(1, "" + numSelected);
 //
 //		data.put(CanonicalNames.Livelihoods, selected);
 //	}
 //
 //	private void lmeTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws Exception {
 //		SelectQuery select = new SelectQuery();
 //		select.select("coding_occurence", "*");
 //		select.constrain(new CanonicalColumnName("coding_occurence", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //		select.constrain(new CanonicalColumnName("coding_occurence", "co_type"), QConstraint.CT_EQUALS,
 //				LME_CODING_OCCURRENCE_TYPE);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("obj_id").getString();
 //
 //			ArrayList<String> dataList = new ArrayList<String>();
 //
 //			fetchCodingOccurrenceData(curRow, dataList);
 //
 //			selected.put(curCode, dataList);
 //		}
 //
 //		data.put(CanonicalNames.LargeMarineEcosystems, selected);
 //	}
 //
 	private void logAsNewCell(String string) {
 		log.append("<td>" + string + "</td>");
 	}
 
 	private void logNode(Taxon cur) {
 		log.append("<tr>");
 		logAsNewCell(Integer.toString(cur.getLevel()));
 		logAsNewCell(cur.getFullName());
 		for (int j = 0; j < TaxonLevel.INFRARANK; j++)
 			logAsNewCell(j < cur.getFootprint().length ? cur.getFootprint()[j] : "&nbsp");
 		log.append("</tr>");
 	}
 	
 	private boolean isChecked(Row row, String key) {
 		try {
 			return Integer.valueOf(1).equals(row.get(key).toString());
 		} catch (NullPointerException e) {
 			return false;
 		}
 	}
 //
 //	private void populationTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("Population", "*");
 //		select.constrain(new CanonicalColumnName("Population", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Loader rowLoader = new Row.Loader();
 //		ec.doQuery(select, rowLoader);
 //		Row row = rowLoader.getRow();
 //
 //		if (row == null)
 //			return;
 //
 //		String max = row.get("Max_population").getString(Column.NEVER_NULL);
 //		String min = row.get("Min_population").getString(Column.NEVER_NULL);
 //
 //		max = max.replaceAll(",", "").replaceAll("\\s", "");
 //		min = min.replaceAll(",", "").replaceAll("\\s", "");
 //
 //		// IF EITHER ARE "", WE DON'T NEED A COMMA - WE'LL TREAT IT AS A BEST
 //		// GUESS
 //		String minMax = min + (min.equals("") || max.equals("") ? "" : "-") + max;
 //
 //		if (!(isValidRangeFormat(minMax))) {
 //			String message = "Unparseable population values from DEM import on "
 //					+ DateFormatUtils.ISO_DATE_FORMAT.format(new Date()) + ". Minimum value from DEM: " + min
 //					+ " --- maximum value from DEM: " + max;
 //
 //			sendNote(curAssessment.getType(), message, curAssessment.getAssessmentID(), CanonicalNames.PopulationSize);
 //		} else {
 //			ArrayList<String> dataList = new ArrayList<String>();
 //			dataList.add(minMax);
 //			data.put(CanonicalNames.PopulationSize, dataList);
 //		}
 //	}
 //
 //	@SuppressWarnings(value = "unchecked")
 	
 	private boolean isBlank(String value) {
 		return value == null || "".equals(value);
 	}
 	
 	private void redListingTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
 		List<Row> rows = queryDEM("red_listing", curDEMid);
 		if (rows.isEmpty())
 			return;
 		
 		//Pretty sure there's only one row, but...
 		for (Row curRow : rows) {
 			{
 				Field redListCriteria = new Field(CanonicalNames.RedListCriteria, curAssessment);
 				RedListCriteriaField proxy = new RedListCriteriaField(redListCriteria);
 	
 				String cat = curRow.get("rl_category").toString();
 				String crit = curRow.get("rl_criteria").toString();
 				
 				if (!isBlank(cat) || !isBlank(crit)) {
 					proxy.setManual(true);
 					proxy.setManualCategory(cat);
 					proxy.setManualCriteria(crit);
 				}
 				
 				/*
 				 * FIXME: date last seen is now year last seen. Need to convert this 
 				 * to a date, then scrape the year, if possible.
 				 */
 				/*dataList.add(SISCategoryAndCriteria.DATE_LAST_SEEN_INDEX, XMLUtils.clean(curRow.get("last_seen").getString(
 						Column.NEVER_NULL)));*/
 	
 				/*
 				 * PEC in it's old form has essentially been removed in SIS 2
 				 */
 				/*
 				dataList.remove(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX);
 				dataList.add(SISCategoryAndCriteria.POSSIBLY_EXTINCT_CANDIDATE_INDEX, ""
 						+ (curRow.get("Poss_extinct_Cand").getInteger() == 1));*/
 	
 				proxy.setPossiblyExtinct(Integer.valueOf(1).equals(curRow.get("Poss_extinct").getInteger()));
 	
 				if (redListCriteria.hasData())
 					curAssessment.getField().add(redListCriteria);
 			}
 
 			String ratValue = curRow.get("rl_rationale").toString();
 			if (!isBlank(ratValue)) {
 				ratValue = XMLUtils.clean(ratValue);
 				ratValue = Replacer.replace(ratValue, "\n", "<br/>");
 				ratValue = Replacer.replace(ratValue, "\r", "");
 				Field rationale = new Field(CanonicalNames.RedListRationale, curAssessment);
 				
 				ProxyField proxy = new ProxyField(rationale);
 				proxy.setTextPrimitiveField("value", ratValue);
 				
 				curAssessment.getField().add(rationale);
 				
 			}
 
 			// CHANGE REASON
 			int changeReason = 0;
 			Integer genuineChangeReason = null;
 			List<Integer> nonGenuineSelection = null;
 			String nonGenuineOtherText = null;
 			Integer noChangeReason = null;
 
 			if (isChecked(curRow, "genuine_change")) {
 				changeReason = 1;
 
 				if (isChecked(curRow, "genuine_recent"))
 					genuineChangeReason = 1;
 				else if (isChecked(curRow, "genuine_sincefirst"))
 					genuineChangeReason = 2;
 			} else if (curRow.get("nongenuine_change").getInteger() == 1) {
 				changeReason = 2;
 				
 				nonGenuineSelection = new ArrayList<Integer>();
 				int index = 1;
 				for (String key : new String[] {"knowledge_new", "Knowledge_criteria", 
 						"Knowledge_correction", "Taxonomy", "Knowledge_criteria", "Other"}) {
 					if (isChecked(curRow, key))
 						nonGenuineSelection.add(index);
 					index++;
 				}
 			} else if (isChecked(curRow, "no_change")) {
 				changeReason = 3;
 
 				if (isChecked(curRow, "same"))
 					noChangeReason = 1;
 				else if (isChecked(curRow, "criteria_change"))
 					noChangeReason = 2;
 			}
 			
 			if (changeReason > 0) {
 				Field field = new Field(CanonicalNames.RedListReasonsForChange, curAssessment);
 				ProxyField proxy = new ProxyField(field);
 				proxy.setForeignKeyPrimitiveField("type", changeReason, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "type"));
 				proxy.setForeignKeyPrimitiveField("timeframe", genuineChangeReason, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "timeframe"));
 				proxy.setForeignKeyListPrimitiveField("changeReasons", nonGenuineSelection, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "changeReasons"));
 				proxy.setStringPrimitiveField("otherReason", nonGenuineOtherText);
 				proxy.setForeignKeyPrimitiveField("catCritChanges", noChangeReason, fmtLookupTableName(CanonicalNames.RedListReasonsForChange, "catCritChanges"));
 				
 				curAssessment.getField().add(field);
 			}
 
 			// Population trend
 			String populationTrend = curRow.get("rl_trend").toString();
 			if (!isBlank(populationTrend)) {
 				Integer populationTrendSelected = getIndex(CanonicalNames.PopulationTrend, "value", populationTrend, null);
 				if (populationTrendSelected != null) {
 					Field field = new Field(CanonicalNames.PopulationTrend, curAssessment);
 					
 					ProxyField proxy = new ProxyField(field);
 					proxy.setForeignKeyPrimitiveField("value", populationTrendSelected, fmtLookupTableName(CanonicalNames.PopulationTrend, "value"));
 					
 					curAssessment.getField().add(field);
 				}
 			}
 			
 			//TODO: port assessment date.
 			String rlAsmDate = curRow.get("assess_date").toString();
 			if (!isBlank(rlAsmDate)) {
 				addDatePrimitiveField(CanonicalNames.RedListAssessmentDate, "value", 
 					curAssessment, FormattedDate.impl.getDate(rlAsmDate));
 			}
 
 			// Red List Notes
 			addTextPrimitiveField(CanonicalNames.RedListNotes, "value", curAssessment, 
 				XMLUtils.clean(curRow.get("Notes").toString()));
 
 			Map<String, String> creditedUsers = new HashMap<String, String>();
 			creditedUsers.put(CanonicalNames.RedListAssessors, "Assessors");
 			creditedUsers.put(CanonicalNames.RedListEvaluators, "Evaluator");
 			
 			for (Map.Entry<String, String> entry : creditedUsers.entrySet()) {
 				String value = curRow.get(entry.getValue()).toString();
 				if (!isBlank(value)) {
 					Field field = new Field(entry.getKey(), curAssessment);
 					RedListCreditedUserField proxy = new RedListCreditedUserField(field);
 					proxy.setText(value);
 					
 					curAssessment.getField().add(field);
 				}
 			}
 
 			// Locations
 			addRangePrimitiveField(CanonicalNames.LocationsNumber, "range", curAssessment, curRow.get("Number_locations").toString());
 
 //			Generation length - DO NOT USE GEN_LENGTH FROM THIS TABLE!!!!!!!!!!!!!!!!!!!!!!
 			//Per e-mail from Jim Ragle, June 9th, 2009
 
 			// Mature individuals
 			String matureIndividuals = curRow.get("Number_mat_ind").toString();
 			if (!isBlank(matureIndividuals)) {
 				if (!isValidRangeFormat(matureIndividuals)) {
 					printf("Invalid range format: " + matureIndividuals);
 					/*sendNote(curAssessment.getType(), CANNED_NOTE + matureIndividuals, curAssessment.getAssessmentID(),
 							CanonicalNames.MaleMaturitySize);
 					sendNote(curAssessment.getType(), CANNED_NOTE + matureIndividuals, curAssessment.getAssessmentID(),
 							CanonicalNames.FemaleMaturitySize);*/
 				} else {
 					addStringPrimitiveField(CanonicalNames.FemaleMaturitySize, "size", curAssessment, matureIndividuals);
 					addStringPrimitiveField(CanonicalNames.MaleMaturitySize, "size", curAssessment, matureIndividuals);
 				}
 			}
 
 			addStringPrimitiveField(CanonicalNames.OldDEMPastDecline, "value", curAssessment, curRow.get("past_decline").toString());
 			addStringPrimitiveField(CanonicalNames.OldDEMPeriodPastDecline, "value", curAssessment, curRow.get("period_past_decline").toString());
 			addStringPrimitiveField(CanonicalNames.OldDEMFutureDecline, "value", curAssessment, curRow.get("future_decline").toString());
 			addStringPrimitiveField(CanonicalNames.OldDEMPeriodFutureDecline, "value", curAssessment, curRow.get("period_future_decline").toString());
 			
 			addBooleanRangePrimitiveField(CanonicalNames.SevereFragmentation, "isFragmented", curAssessment, curRow.get("severely_frag").toString());
 		}
 	}
 	
 
 	private void referencesImport(Long curDEMid, Assessment curAssessment) {
 		// query the DEM bibliographic_original_records that go with this assessment
 		SelectQuery select = new SelectQuery();
 		select = new SelectQuery();
 		select.select("bibliographic_original_records", "*");
 		select.join("bibliography_link", new QRelationConstraint(new CanonicalColumnName("bibliography_link",
 				"Bibliography_number"), new CanonicalColumnName("bibliographic_original_records", "Bib_Code")));
 		select.constrain(new CanonicalColumnName("bibliography_link", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 		try {
 			// build a single document containing all the references for this
 			// assessment
 			ElementalReferenceRowProcessor errp = new ElementalReferenceRowProcessor(session);
 			ec.doQuery(select, errp);
 			
 			curAssessment.getReference().addAll(errp.getReferences());
 		} catch (Throwable e) {
 			printf("Error: %s", e);
 		}
 	}
 
 	/*
 	 * UTILITY FUNCTIONS
 	 */
 
 //	private void riversTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("rivers_table", "*");
 //		select.constrain(new CanonicalColumnName("rivers_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("river_number").getString();
 //
 //			ArrayList<String> dataList = new ArrayList<String>();
 //			dataList.add("0");
 //			dataList.add("false");
 //			dataList.add("0");
 //
 //			selected.put(curCode, dataList);
 //		}
 //
 //		data.put(CanonicalNames.Rivers, selected);
 //	}
 
 	public void run() {
 		running.set(true);
 		failed.set(false);
 		statusMessage = new StringBuilder();
 		
 		Date start = Calendar.getInstance().getTime();
 		printf("! -- Starting %s conversion at %s", getClass().getSimpleName(), start.toString());
 		
 		try {
 
 			//registerDatasource("dem", "jdbc:access:///" + source.getAbsolutePath(), "com.hxtt.sql.access.AccessDriver", "", "");
 
 			/*try {
 				Document structDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
 						DEMImport.class.getResourceAsStream("refstruct.xml"));
 				new SystemExecutionContext("dem").setStructure(structDoc);
 			} catch (Exception ugly) {
 				ugly.printStackTrace();
 				statusMessage.append("Internal system failure: could not read DEM.<br>");
 				statusMessage.append("Please report the following message:<br>");
 				statusMessage.append(DocumentUtils.getStackTraceAsString(ugly).replaceAll("\n", "<br>"));
 				failed.set(true);
 			}*/
 			
 			/*registerDatasource("demConversion", "jdbc:access:////usr/data/demMigration.mdb",
 					"com.hxtt.sql.access.AccessDriver", "", "");
 			registerDatasource("demSource", "jdbc:access:////usr/data/demSource.mdb",
 					"com.hxtt.sql.access.AccessDriver", "", "");*/
 
 			switchToDBSession("dem");
 
 			log.append("<table border=\"1\"><tr>" + "<th>Level</th>" + "<th>Friendly Name</th>" + "<th>Kingdom</th>"
 					+ "<th>Phylum</th>" + "<th>Class</th>" + "<th>Order</th>" + "<th>Family</th>" + "<th>Genus</th>"
 					+ "<th>Species</th>" + "<th>Infrarank</th></tr>");
 
 			if (!failed.get()) {
 				try {
 					buildTree();
 				} catch (Exception e) {
 					statusMessage.append("Failed to convert taxa to the SIS data format.<br>");
 					statusMessage.append("Please report the following message:<br>");
 					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
 					failed.set(true);
 					if( e instanceof DBException ) {
 						Throwable f = e.getCause();
 						
 						while( f != null ) {
 							if( f instanceof SQLException && ((SQLException)f).getNextException() != null )
 								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
 							
 							f = f.getCause();
 						}
 					}
 					failed.set(true);
 				}
 			} else
 				statusMessage.append("(Did not convert taxa to the SIS data format due to a prior failure.)<br>");
 
 			if (!failed.get()) {
 				try {
 					addTaxaDetails();
 				} catch (Exception e) {
 					statusMessage.append("Failed adding taxa details.<br>");
 					statusMessage.append("Please report the following message:<br>");
 					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
 					failed.set(true);
 					if( e instanceof DBException ) {
 						Throwable f = e.getCause();
 						
 						while( f != null ) {
 							if( f instanceof SQLException )
 								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
 							
 							f = f.getCause();
 						}
 					}
 					failed.set(true);
 				}
 			}
 
 			if (!failed.get()) {
 				try {
 					exportNodes();
 				} catch (Exception e) {
 					statusMessage.append("Failed saving new taxa in SIS.<br>");
 					statusMessage.append("Please report the following message:<br>");
 					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
 					failed.set(true);
 					if( e instanceof DBException ) {
 						Throwable f = e.getCause();
 						
 						while( f != null ) {
 							if( f instanceof SQLException )
 								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
 							
 							f = f.getCause();
 						}
 					}
 					failed.set(true);
 				}
 			} else
 				statusMessage.append("(Did not save new taxa in SIS due to a prior failure.)<br>");
 
 			
 			if (!failed.get()) {
 				try {
 					buildAssessments();
 				} catch (Exception e) {
 					statusMessage.append("Failed converting assessments to SIS format.<br>");
 					statusMessage.append("Please report the following message:<br>");
 					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
 					failed.set(true);
 					if( e instanceof DBException ) {
 						Throwable f = e.getCause();
 						
 						while( f != null ) {
 							if( f instanceof SQLException )
 								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
 							
 							f = f.getCause();
 						}
 					}
 					failed.set(true);
 				}
 			} else
 				statusMessage
 						.append("(Did not attempt conversion of assessments to SIS format due to a prior failure.)<br>");
 
 			
 			if (!failed.get()) {
 				try {
 					exportAssessments();
 					statusMessage.append("Import successful.");
 				} catch (Exception e) {
 					statusMessage.append("Failed saving assessments in SIS.<br>");
 					statusMessage.append("Please report the following message:<br>");
 					statusMessage.append(DocumentUtils.getStackTraceAsString(e).replaceAll("\n", "<br>"));
 					failed.set(true);
 					if( e instanceof DBException ) {
 						Throwable f = e.getCause();
 						
 						while( f != null ) {
 							if( f instanceof SQLException )
 								print("--- SQL Exception - Next Exception ---\n" + DocumentUtils.getStackTraceAsString(((SQLException)f).getNextException()));
 							
 							f = f.getCause();
 						}
 					}
 					failed.set(true);
 				}
 			} else
 				statusMessage.append("(Did not save assessments in SIS due to a prior failure.)<br>");
 
 		} catch (Throwable ex) {
 			ex.printStackTrace();
 			statusMessage.append("Internal system failure setting up DEMImport.<br>");
 			statusMessage.append("Please report the following message:<br>");
 			statusMessage.append(DocumentUtils.getStackTraceAsString(ex).replaceAll("\n", "<br>"));
 			failed.set(true);
 		}
 		
 		log.append("</table>");
 		
 		running.set(false);
 		
 		DBSessionFactory.unregisterDataSource(demSessionName);
 		
 		DEMImportInformation info = new DEMImportInformation(
 			new Date(), !failed.get(), statusMessage.toString(), "", user, log.toString()
 		);
 		
 		Date end = Calendar.getInstance().getTime();
 		
 		long millis = end.getTime() - start.getTime();
 		long secs = millis / 1000;
 		long mins = secs / 60;
 		
 		printf("! -- Finished %s import in %s mins, %s seconds at %s", getClass().getSimpleName(), mins, secs, end.toString());
 		print("Results:");
 		print(info.toString());
 		
 		close();
 		
 		DEMImportInformation.addToQueue(info, SIS.get().getVFS());
 	}
 
 //	private void sendNote(String assessmentType, String noteBody, String assessmentID, String canonicalName) {
 //		Note note = new Note();
 //		note.setBody(noteBody);
 //		note.setCanonicalName(canonicalName);
 //		note.setUser("DEMimport");
 //		note.setDate(DateFormatUtils.ISO_DATE_FORMAT.format(new Date()));
 //
 //		String url = uriPrefix + "/notes/" + assessmentType + "/" + assessmentID + "/" + canonicalName;
 //
 //		try {
 //			Request request = new Request(Method.POST , url, new StringRepresentation(note.toXML(), MediaType.TEXT_XML, null,
 //					CharacterSet.UTF_8));
 //			Response response = context.getClientDispatcher().handle(request);
 //
 //			if (!response.getStatus().isSuccess())
 //				SysDebugger.getInstance().println("Failure response from Notes server.");
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //			SysDebugger.getInstance().println("Error noting unparseable " + canonicalName + " data.");
 //		}
 //	}
 
 	private void switchToDBSession(String sessionName) throws Exception {
 		ec = new SystemExecutionContext("dem".equals(sessionName) ? demSessionName : sessionName);
 		ec.setExecutionLevel(ExecutionContext.ADMIN);
 		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
 	}
 
 	private void systematicsTableImport(Long curDEMid, Assessment curAssessment) throws DBException {
 		List<Row> rows = queryDEM("Systematics", curDEMid);
 		if (rows.isEmpty())
 			return;
 		
 		Row row = rows.get(0);
 
 		try {
 			curAssessment.setDateAssessed(new Date(Long.valueOf(row.get("date_added").toString())));
 		} catch (NullPointerException e) {
 			TrivialExceptionHandler.ignore(this, e);
 		} catch (NumberFormatException e) {
 			TrivialExceptionHandler.ignore(this, e);
 		}
 		// curAssessment.setDateModified(row.get("date_modified").getString(
 		// Column.NEVER_NULL));
 
 		Boolean global = "global".equals(row.get("Assessments").toString());
 		Boolean endemic = Boolean.FALSE;
 		
 		String regionName = "Global";
 
 		if (!global.booleanValue()) {
 			endemic = Integer.valueOf(1).equals(row.get("Endemic_region").getInteger());
 
 			Integer region = row.get("Region").getInteger();
 			if (region != null) {
 				//TODO: cache this query...
 				SelectQuery selectRegion = new SelectQuery();
 				selectRegion.select("region_lookup_table", "*");
 				selectRegion.constrain(new CanonicalColumnName("region_lookup_table", "Region_number"),
 						QConstraint.CT_EQUALS, region);
 	
 				Row.Loader regionLoader = new Row.Loader();
 	
 				try {
 					ec.doQuery(selectRegion, regionLoader);
 					regionName = regionLoader.getRow().get("Region_name").toString();
 				} catch (DBException e) {
 					//print("Error grabbing region name for id " + region);
 					e.printStackTrace();
 				} catch (NullPointerException e) {
 					//print("NPE. Just going to assume it's a global, then, couldn't find " + region);
 				}
 			}
 		}
 
 		if (!regionName.equalsIgnoreCase("Global")) {
 			// Check to make sure the region exists and get its proper ID
 			RegionCriteria criteria = new RegionCriteria(session);
 			criteria.name.ilike(regionName);
 			
 			Region region = null;
 			try {
 				region = criteria.uniqueRegion();
 			} catch (Exception e) {
 				statusMessage.append("Could not properly create a Regional assessment for " + "region named "
 						+ regionName + ". Please report this error and include "
 						+ "the DEM you are attempting to import.");
 			}
 			
 			if (region != null) {
 				curAssessment.setRegions(Arrays.asList(region), endemic);
 				/*try {
 					data.put(CanonicalNames.RegionInformation, createDataArray("true", region.getId(), endemic.toString()));
 				} catch (IOException e) {
 					e.printStackTrace();
 					failed.set(true);
 					statusMessage.append("Could not properly create a Regional assessment for " + "region named "
 							+ regionName + ". Please report this error and include "
 							+ "the DEM you are attempting to import.");
 				}*/
 			}
 		} else
 			curAssessment.setRegions(Arrays.asList(Region.getGlobalRegion()), endemic);
 		
 		Map<String, String> docFields = new HashMap<String, String>();
 		docFields.put(CanonicalNames.ConservationActionsDocumentation, "cons_measures");
 		docFields.put(CanonicalNames.ThreatsDocumentation, "threats_info");
 		docFields.put(CanonicalNames.HabitatDocumentation, "habitat");
 		docFields.put(CanonicalNames.PopulationDocumentation, "population");
 		docFields.put(CanonicalNames.RangeDocumentation, "range");
 		docFields.put(CanonicalNames.TaxonomicNotes, "notes");
 		
 		for (Map.Entry<String, String> entry : docFields.entrySet()) {
 			String value = null;
 			try {
 				value = row.get(entry.getValue()).toString();
 			} catch (NullPointerException e) {
 				continue;
 			}
 			
 			if (value != null && !"".equals(value)) {
 				if (!CanonicalNames.TaxonomicNotes.equals(entry.getKey()))
 					value = FormattingStripper.stripText(value);
 				value = Replacer.replace(value, "\n", "<br/>");
 				value = Replacer.replace(value, "\r", "");
 				
 				Field field;
 				try {
 					field = generator.getField(entry.getKey());
 				} catch (Exception e) {
 					printf("No field found in schema for %s", entry.getKey());
 					continue;
 				}
 				
 				String prim = field.getPrimitiveField().iterator().next().getName();
 				
 				ProxyField proxy = new ProxyField(field);
 				proxy.setTextPrimitiveField(prim, value);
 				
 				field.setAssessment(curAssessment);
 				curAssessment.getField().add(field);
 			}
 			
 		}
 	}
 //
 //	private void threatTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("threat_table", "*");
 //		select.constrain(new CanonicalColumnName("threat_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
 //		for (Iterator<Row> iter = rowLoader.getSet().listIterator(); iter.hasNext();) {
 //			Row curRow = iter.next();
 //
 //			String curCode = curRow.get("Threat_code").getString();
 //			int curTiming = curRow.get("threat_timing").getInteger(Column.NEVER_NULL);
 //
 //			String stress1 = null;
 //			String stress2 = null;
 //			String notes = "";
 //
 //			try {
 //				switchToDBSession("demConversion");
 //
 //				SelectQuery conversionSelect = new SelectQuery();
 //				conversionSelect.select("Threat crosswalking", "*");
 //				conversionSelect.constrain(new CanonicalColumnName("Threat crosswalking", "oldthreat_id"),
 //						QConstraint.CT_EQUALS, curCode);
 //
 //				Row.Loader conversionRow = new Row.Loader();
 //				ec.doQuery(conversionSelect, conversionRow);
 //
 //				if (conversionRow.getRow() != null) {
 //					curCode = conversionRow.getRow().get("newthreat_id").getString();
 //					stress1 = conversionRow.getRow().get("stress 1").getString();
 //					stress2 = conversionRow.getRow().get("stress 2").getString();
 //					notes = XMLUtils.clean(conversionRow.getRow().get("comment").getString(Column.NEVER_NULL));
 //				}
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //				SysDebugger.getInstance().println("Error checking against conversion table.");
 //			}
 //
 //			// If an entry has already been added, just tweak the timings
 //			if (selected.containsKey(curCode)) {
 //				ArrayList arr = (ArrayList) selected.get(curCode);
 //				String extantTiming = ((ArrayList) selected.get(curCode)).get(0).toString();
 //
 //				if (extantTiming == "" + SISThreatStructure.TIMING_ONGOING_INDEX) {
 //					// If it's ongoing just leave things alone
 //				}
 //
 //				// If it's ever present mark it ongoing
 //				else if (curTiming == SISThreatStructure.TIMING_ONGOING_INDEX)
 //					arr.set(0, "" + SISThreatStructure.TIMING_ONGOING_INDEX);
 //
 //				// If it's definitely not ongoing, look for past and future
 //				else if (curTiming == SISThreatStructure.TIMING_PAST_UNLIKELY_RETURN_INDEX
 //						&& extantTiming.equals("" + SISThreatStructure.TIMING_FUTURE_INDEX))
 //					arr.set(0, "" + SISThreatStructure.TIMING_PAST_LIKELY_RETURN_INDEX);
 //
 //				else if (curTiming == SISThreatStructure.TIMING_FUTURE_INDEX
 //						&& extantTiming.equals("" + SISThreatStructure.TIMING_PAST_UNLIKELY_RETURN_INDEX))
 //					arr.set(0, "" + SISThreatStructure.TIMING_PAST_LIKELY_RETURN_INDEX);
 //
 //				// No need to put it back in, I don't believe
 //				// selected.put(curCode, arr);
 //			} else {
 //				ArrayList<String> dataArray = new ArrayList<String>();
 //				dataArray.add("" + curTiming);
 //				dataArray.add("0");
 //				dataArray.add("0");
 //				dataArray.add("");
 //				dataArray.add(notes);
 //
 //				if (stress1 != null && !stress1.equals("0") && !stress1.equals("")) {
 //					if (stress2 != null && !stress2.equals("0") && !stress2.equals("")) {
 //						dataArray.add("2");
 //						dataArray.add(stress1);
 //						dataArray.add(stress2);
 //					} else {
 //						dataArray.add("1");
 //						dataArray.add(stress1);
 //					}
 //				} else
 //					dataArray.add("0");
 //
 //				selected.put(curCode, dataArray);
 //			}
 //		}
 //
 //		data.put(CanonicalNames.Threats, selected);
 //
 //		try {
 //			switchToDBSession("dem");
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //			print("Error switching back to DEM after building Threats. Import will now fail.");
 //		}
 //	}
 //
 	private void updateFootprint(ArrayList<String> footprint, Taxon taxon) {
 		// Ensure footprint is on par with what the nodes say, not what the DEM
 		// says
 		footprint.clear();
 		footprint.addAll(Arrays.asList(taxon.getFootprint()));
 		footprint.add(taxon.getName());
 	}
 //
 //	/**
 //	 * Tables used: Source_of_specimens_table purpose_table
 //	 * removed_from_wild_table
 //	 */
 //	@SuppressWarnings(value = "unchecked")
 //	private void useTradeImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("Source_of_specimens_table", "*");
 //		select.constrain(new CanonicalColumnName("Source_of_specimens_table", "Sp_code"), QConstraint.CT_EQUALS,
 //				curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		ArrayList<Integer> sourcesSelected = new ArrayList<Integer>();
 //		for (Row curRow : rowLoader.getSet())
 //			sourcesSelected.add(curRow.get("Source_code").getInteger(Column.NEVER_NULL));
 //
 //		select = new SelectQuery();
 //		select.select("purpose_table", "*");
 //		select.constrain(new CanonicalColumnName("purpose_table", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		HashMap<Integer, ArrayList<Integer>> purposesSelected = new HashMap<Integer, ArrayList<Integer>>();
 //		for (Row curRow : rowLoader.getSet()) {
 //			int purposeCode = curRow.get("Purpose_code").getInteger(Column.NEVER_NULL);
 //			int useCode = curRow.get("utilisation_code").getInteger(Column.NEVER_NULL);
 //
 //			ArrayList<Integer> mine = null;
 //
 //			if (purposesSelected.containsKey(new Integer(purposeCode)))
 //				mine = purposesSelected.get(new Integer(purposeCode));
 //			else {
 //				mine = new ArrayList<Integer>();
 //				purposesSelected.put(new Integer(purposeCode), mine);
 //			}
 //
 //			mine.add(new Integer(useCode));
 //		}
 //
 //		select = new SelectQuery();
 //		select.select("removed_from_wild_table", "*");
 //		select
 //				.constrain(new CanonicalColumnName("removed_from_wild_table", "Sp_code"), QConstraint.CT_EQUALS,
 //						curDEMid);
 //
 //		rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		ArrayList<Integer> wildSelected = new ArrayList<Integer>();
 //		for (Row curRow : rowLoader.getSet())
 //			wildSelected.add(curRow.get("Wild_code").getInteger(Column.NEVER_NULL));
 //
 //		if (!(sourcesSelected.size() == 0 && purposesSelected.size() == 0 && wildSelected.size() == 0)) {
 //			if (sourcesSelected.size() == 0)
 //				sourcesSelected.add(new Integer(0));
 //			if (purposesSelected.keySet().size() == 0)
 //				purposesSelected.put(new Integer(0), null);
 //			if (wildSelected.size() == 0)
 //				wildSelected.add(new Integer(0));
 //
 //			ArrayList<String> selected = new ArrayList();
 //
 //			int count = 0;
 //			int index1;
 //			int index2;
 //			int index3;
 //
 //			for (Integer curPurpose : purposesSelected.keySet()) {
 //				index1 = curPurpose.intValue();
 //
 //				for (int j = 0; j < sourcesSelected.size(); j++) {
 //					index2 = sourcesSelected.get(j).intValue();
 //
 //					for (int k = 0; k < wildSelected.size(); k++) {
 //						index3 = wildSelected.get(k).intValue();
 //						ArrayList dataList = UseTrade.generateDefaultDataList();
 //						dataList.remove(2);
 //						dataList.add(2, "" + index3);
 //						dataList.remove(1);
 //						dataList.add(1, "" + index2);
 //						dataList.remove(0);
 //						dataList.add(0, "" + index1);
 //
 //						ArrayList<Integer> ticks = purposesSelected.get(curPurpose);
 //						if (ticks != null) {
 //							// Magic number use: 2, as subsistence == 1,
 //							// national == 2
 //							// and international == 3, and the tick box offsets
 //							// in the
 //							// structure are 3, 4 and 5
 //							for (Integer purp : ticks) {
 //								dataList.remove(2 + purp.intValue());
 //								dataList.add(2 + purp.intValue(), "true");
 //							}
 //						}
 //
 //						selected.addAll(dataList);
 //
 //						count++;
 //					}
 //				}
 //			}
 //
 //			selected.add(0, "" + count);
 //			data.put(CanonicalNames.UseTradeDetails, selected);
 //		}
 //	}
 //
 //	private void utilisationTableImport(Long curDEMid, AssessmentData curAssessment, HashMap<String, Object> data)
 //			throws DBException {
 //		SelectQuery select = new SelectQuery();
 //		select.select("utilisation_general", "*");
 //		select.constrain(new CanonicalColumnName("utilisation_general", "Sp_code"), QConstraint.CT_EQUALS, curDEMid);
 //
 //		Row.Set rowLoader = new Row.Set();
 //		ec.doQuery(select, rowLoader);
 //
 //		if (rowLoader == null)
 //			return;
 //
 //		for (Row curRow : rowLoader.getSet()) {
 //			String useTradeNarrative = "";
 //
 //			data.put(CanonicalNames.NotUtilized, createDataArray(""
 //					+ (curRow.get("Utilised").getInteger(Column.NEVER_NULL) == 1), true));
 //
 //			String curNarText = XMLUtils.clean(curRow.get("Other_purpose").getString(Column.NEVER_NULL));
 //			if (!curNarText.equals("")) {
 //				useTradeNarrative += "--- Other purpose text ---<br>";
 //				useTradeNarrative += curNarText.replaceAll("\n", "<br>").replaceAll("\r", "");
 //			}
 //
 //			curNarText = XMLUtils.clean(curRow.get("Other_wild").getString(Column.NEVER_NULL));
 //			if (!curNarText.equals("")) {
 //				useTradeNarrative += "--- Other wild text ---<br>";
 //				useTradeNarrative += curNarText.replaceAll("\n", "<br>").replaceAll("\r", "");
 //			}
 //
 //			curNarText = XMLUtils.clean(curRow.get("Other_source").getString(Column.NEVER_NULL));
 //			if (!curNarText.equals("")) {
 //				useTradeNarrative += "--- Other source text ---<br>";
 //				useTradeNarrative += curNarText.replaceAll("\n", "<br>").replaceAll("\r", "");
 //			}
 //			data.put(CanonicalNames.UseTradeDocumentation, createDataArray(useTradeNarrative, false));
 //
 //			String wildOfftake = curRow.get("Offtake").getString(Column.NEVER_NULL);
 //
 //			if (wildOfftake.equalsIgnoreCase("Increasing"))
 //				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("1", true));
 //			else if (wildOfftake.equalsIgnoreCase("Decreasing"))
 //				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("2", true));
 //			else if (wildOfftake.equalsIgnoreCase("Stable"))
 //				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("3", true));
 //			else if (wildOfftake.equalsIgnoreCase("Unknown"))
 //				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("4", true));
 //			else
 //				data.put(CanonicalNames.TrendInWildOfftake, createDataArray("0", true));
 //
 //			String domesticOfftake = curRow.get("Trend").getString(Column.NEVER_NULL);
 //
 //			if (domesticOfftake.equalsIgnoreCase("Increasing"))
 //				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("1", true));
 //			else if (domesticOfftake.equalsIgnoreCase("Decreasing"))
 //				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("2", true));
 //			else if (domesticOfftake.equalsIgnoreCase("Stable"))
 //				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("3", true));
 //			else if (domesticOfftake.equalsIgnoreCase("Not cultivated"))
 //				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("4", true));
 //			else if (domesticOfftake.equalsIgnoreCase("Unknown"))
 //				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("5", true));
 //			else
 //				data.put(CanonicalNames.TrendInDomesticOfftake, createDataArray("0", true));
 //
 //		}
 //	}
 	
 	private List<Row> queryDEM(String table, Long SpcRecID) throws DBException {
 		//FIXME: use Jackcess API instead?
 		SelectQuery select = new SelectQuery();
 		select.select(table, "*");
 		select.constrain(new CanonicalColumnName(table, "Sp_code"), QConstraint.CT_EQUALS, SpcRecID);
 
 		Row.Set rs = new Row.Set();
 		
 		ec.doQuery(select, rs);
 
 		return rs.getSet();
 	}
 	
 	private String correctCode(String code) {
 		if ("NLA-CU".equals(code))
 			return "CW";
 		
 		return code;
 	}
 	
 	private Integer getIndex(String canonicalName, String dataPointName, String value, Integer defaultValue) throws DBException {
 		return getIndex(canonicalName + "_" + dataPointName + "Lookup", value, defaultValue);
 	}
 	
 	private Integer getIndex(String libraryTable, String value, Integer defaultValue) throws DBException {
 //		String table = canonicalName + "_" + name + "Lookup";
 		
 		for( Row row : getLookup(libraryTable).getSet() ) {
 			if (row.get("code") != null) {
 				if (correctCode(value).equalsIgnoreCase(row.get("code").getString()))
 					return row.get("id").getInteger();
 			} else if( value.equalsIgnoreCase(row.get("label").getString()) || 
 					value.equalsIgnoreCase( Integer.toString((Integer.parseInt(
 							row.get("name").getString())+1)) ) )
 				return row.get("id").getInteger();
 		}
 		
 		return defaultValue;
 	}
 	
 	private Row.Set getLookup(String table) throws DBException {
 		String fieldName = table;
 		
 		if (lookups.containsKey(fieldName))
 			return lookups.get(fieldName);
 		else {
 			SelectQuery query = new SelectQuery();
 			query.select(fieldName, "ID", "ASC");
 			query.select(fieldName, "*");
 			
 			Row.Set lookup = new Row.Set();
 			
 			lec.doQuery(query, lookup);
 			
 			lookups.put(fieldName, lookup);
 
 			return lookup;
 		}
 	}
 	
 	public void print(String out) {
 		write(out);
 	}
 	
 	public void println(String out) {
 		write(out);
 	}
 
 	public void printf(String out, Object... args) {
 		write(String.format(out, args));
 	}
 	
 }
