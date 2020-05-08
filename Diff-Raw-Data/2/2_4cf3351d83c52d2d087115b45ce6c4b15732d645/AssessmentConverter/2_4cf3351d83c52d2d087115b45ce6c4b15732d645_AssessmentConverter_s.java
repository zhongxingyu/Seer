 package org.iucn.sis.shared.conversions;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.naming.NamingException;
 
 import org.hibernate.criterion.Restrictions;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.ReferenceIO;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
 import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.BooleanRangePrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.BooleanUnknownPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.FloatPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.IntegerPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.RangePrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;
 import org.iucn.sis.shared.helpers.AssessmentData;
 import org.iucn.sis.shared.helpers.AssessmentParser;
 import org.iucn.sis.shared.helpers.CanonicalNames;
 import org.iucn.sis.shared.helpers.Note;
 import org.iucn.sis.shared.helpers.ReferenceUI;
 import org.iucn.sis.shared.report.MigrationReport;
 import org.restlet.util.Couple;
 
 import com.solertium.db.DBException;
 import com.solertium.db.DBSession;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.SystemExecutionContext;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.lwxml.factory.NativeDocumentFactory;
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.Replacer;
 import com.solertium.util.TrivialExceptionHandler;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.vfs.VFS;
 
 public class AssessmentConverter extends GenericConverter<VFSInfo> {
 	
 	private static final DateFormat shortfmt = new SimpleDateFormat("yyyy-MM-dd");
 	
 	public static enum ConversionMode {
 		DRAFT, PUBLISHED, ALL
 	}
 	
 	private ExecutionContext ec;
 	private Map<String, Row.Set> lookups;
 
 	private Map<String, Class> typeLookup;
 	
 	private ConversionMode mode = ConversionMode.ALL;
 	
 	private UserIO userIO;
 	private TaxonIO taxonIO;
 	private ReferenceIO referenceIO;
 	private AssessmentIO assessmentIO;
 	
 	public AssessmentConverter() throws NamingException {
 		this("sis_lookups");
 	}
 
 	public AssessmentConverter(String dbSessionName) throws NamingException {
 		setClearSessionAfterTransaction(true);
 		
 		ec = new SystemExecutionContext(dbSessionName);
 		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
 		ec.setExecutionLevel(ExecutionContext.ADMIN);
 		ec.getDBSession().setIdentifierCase(DBSession.CASE_UPPER);
 		
 		lookups = new HashMap<String, Row.Set>();
 
 		typeLookup = new HashMap<String, Class>();
 		typeLookup.put("range_primitive_field", RangePrimitiveField.class);
 		typeLookup.put("boolean_primitive_field", BooleanPrimitiveField.class);
 		typeLookup.put("boolean_range_primitive_field", BooleanRangePrimitiveField.class);
 		typeLookup.put("boolean_unknown_primitive_field", BooleanUnknownPrimitiveField.class);
 		typeLookup.put("fk_primitive_field", ForeignKeyPrimitiveField.class);
 		typeLookup.put("fk_list_primitive_field", ForeignKeyListPrimitiveField.class);
 		typeLookup.put("date_primitive_field", DatePrimitiveField.class);
 		typeLookup.put("float_primitive_field", FloatPrimitiveField.class);
 		typeLookup.put("integer_primitive_field", IntegerPrimitiveField.class);
 		typeLookup.put("text_primitive_field", TextPrimitiveField.class);
 		typeLookup.put("string_primitive_field", StringPrimitiveField.class);
 		typeLookup.put("field", Object.class);
 	}
 	
 	public void setConversionMode(ConversionMode mode) {
 		this.mode = mode;
 	}
 	
 	@Override
 	protected void run() throws Exception {
 		userIO = new UserIO(session);
 		taxonIO = new TaxonIO(session);
 		referenceIO = new ReferenceIO(session);
 		assessmentIO = new AssessmentIO(session);
 		
 		if (ConversionMode.DRAFT.equals(mode))
 			convertAllDrafts(data.getOldVFS(), data.getNewVFS());
 		else if (ConversionMode.PUBLISHED.equals(mode))
 			convertAllPublished(data.getOldVFS(), data.getNewVFS());
 		else {
 			convertAllDrafts(data.getOldVFS(), data.getNewVFS());
 			convertAllPublished(data.getOldVFS(), data.getNewVFS());
 		}
 	}
 
 	public void convertAllPublished(VFS oldVFS, VFS newVFS) throws Exception {
 		convertAll("/HEAD/browse/assessments", oldVFS, newVFS);
 	}
 	
 	public void convertAllDrafts(VFS oldVFS, VFS newVFS) throws Exception {
 		convertAll("/HEAD/drafts", oldVFS, newVFS);
 	}
 	
 	public void convertAllFaster(String rootURL, VFS oldVFS, VFS newVFS) throws Exception {
 		final AssessmentParser parser = new AssessmentParser();
 		final User user = userIO.getUserFromUsername("admin");
 		final AtomicInteger converted = new AtomicInteger(0);
 		
 		File folder = new File(data.getOldVFSPath() + rootURL);
 		
 		readFolder(parser, user, converted, folder);
 	}
 	
 	private void readFolder(AssessmentParser parser, User user, AtomicInteger converted, File folder) throws Exception {
 		for (File file : folder.listFiles()) {
 			if (file.isDirectory())
 				readFolder(parser, user, converted, file);
 			else if (file.getName().endsWith(".xml"))
 				readFile(parser, user, converted, file);
 		}
 	}
 	
 	private void readFile(AssessmentParser parser, User user, AtomicInteger converted, File file) throws Exception {
 		try {
 			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
 			ndoc.parse(FileListing.readFileAsString(file));
 			
 			parser.parse(ndoc);
 				
 			Couple<Assessment, MigrationReport> result = assessmentDataToAssessment(parser.getAssessment(), user);
 			Assessment assessment = result.getFirst();
 			if (assessment != null) {
 				if (assessment.getTaxon() != null) {
 					/*User userToSave;
 					if (assessment.getLastEdit() != null)  {
 						userToSave = assessment.getLastEdit().getUser();
 					} else {
 						userToSave = user;
 					}*/
 						
 					if (assessment.getLastEdit() == null) {
 						Edit edit = new Edit();
 						edit.setUser(user);
 						edit.getAssessment().add((assessment));
 						assessment.getEdit().add(edit);
 					}
 						
 					session.save(assessment);
 					
 					try {
 						if (!result.getSecond().isMigrationSuccessful())
 							result.getSecond().save(assessment, data.getNewVFS());
 					} catch (IOException e) {
 						printf("Failed to saved migration report: %s", e.getMessage());
 					}
 						
 					/*if (!assessmentIO.writeAssessment(assessment, userToSave, false).status.isSuccess()) {
 						throw new Exception("The assessment " + file.getPath() + " did not want to save");
 					}*/
 						
 					if (converted.incrementAndGet() % 50 == 0) {
 						commitAndStartTransaction();
 						printf("Converted %s assessments...", converted.get());
 					}
 				} else {
 					print("The taxon " + parser.getAssessment().getSpeciesID() + " is null");
 				}
 			} else {
 				print("The assessment " + file.getPath() + " is null");
 			}
 		} catch (Throwable e) {
 			print("Failed on file " + file.getPath());
 			e.printStackTrace();
 			throw new Exception(e);
 		}
 	}
 	
 	
 	public void convertAll(String rootURL, VFS oldVFS, VFS newVFS) throws Exception {
 		if (true) {
 			convertAllFaster(rootURL, oldVFS, newVFS);
 			return;
 		}
 			
 		List<File> allFiles = FileListing.main(data.getOldVFSPath() + rootURL);
 		int size = allFiles.size();
 		// List<File> allFiles = new ArrayList<File>();
 		// File aFile = new File(GoGoEgo.getInitProperties().get("sis_vfs") +
 		// "/HEAD/browse/nodes/100/100001.xml");
 		// allFiles.add(aFile);
 
 		//long assessmentsConverted = 0;
 		AssessmentParser parser = new AssessmentParser();
 
 		User user = userIO.getUserFromUsername("admin");
 		
 		final AtomicInteger converted = new AtomicInteger(0);
 		for (File file : allFiles) {
 			try {
 				if (file.getPath().endsWith(".xml")) {
 					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
 					ndoc.parse(FileListing.readFileAsString(file));
 					parser.parse(ndoc);
 					
 					Couple<Assessment, MigrationReport> result = 
 						assessmentDataToAssessment(parser.getAssessment(), user);
 					Assessment assessment = result.getFirst();
 					if (assessment != null) {
 						if (assessment.getTaxon() != null) {
 							/*User userToSave;
 							if (assessment.getLastEdit() != null)  {
 								userToSave = assessment.getLastEdit().getUser();
 							} else {
 								userToSave = user;
 							}*/
 							
 							if (assessment.getLastEdit() == null) {
 								Edit edit = new Edit();
 								edit.setUser(user);
 								edit.getAssessment().add((assessment));
 								assessment.getEdit().add(edit);
 							}
 							
 							session.save(assessment);
 							
 							result.getSecond().save(assessment, data.getNewVFS());
 							
 							/*if (!assessmentIO.writeAssessment(assessment, userToSave, false).status.isSuccess()) {
 								throw new Exception("The assessment " + file.getPath() + " did not want to save");
 							}*/
 							
 							if (converted.incrementAndGet() % 50 == 0) {
 								commitAndStartTransaction();
 								printf("Converted %s/%s assessments...", converted.get(), size);
 							}
 						} else {
 							print("The taxon " + parser.getAssessment().getSpeciesID() + " is null");
 						}
 					} else {
 						print("The assessment " + file.getPath() + " is null");
 					}
 
 				}
 			} catch (Throwable e) {
 				print("Failed on file " + file.getPath());
 				e.printStackTrace();
 				throw new Exception(e);
 			}
 		}
 		
 		/*
 		if( assessmentsConverted % 50 != 0 ) {
 			SIS.get().getManager().getSession().getTransaction().commit();
 			SIS.get().getManager().getSession().beginTransaction();
 		}*/
 
 	}
 	
 	private Taxon getTaxon(MigrationReport report, AssessmentData assessData) {
 		Taxon taxon = taxonIO.getTaxon(Integer.valueOf(assessData.getSpeciesID()));
 		/*if (taxon != null && taxon.getFriendlyName().equalsIgnoreCase(assessData.getSpeciesName()))
 			return taxon;
 		else if (taxon != null)
 			warning(report, "Found taxa %s by ID, but name %s doesn't match expected name %s", assessData.getSpeciesID(), taxon.getFriendlyName(), assessData.getSpeciesName());
 		*/
 		if (taxon != null)
 			return taxon;
 		
 		Taxon byName = (Taxon)session.createCriteria(Taxon.class).
 			add(Restrictions.eq("friendlyName", assessData.getSpeciesName()))
 			.uniqueResult();
 		if (byName == null)
 			debug("No taxon found matching %s", assessData.getSpeciesName());
 		else
 			return byName;
 		
 		return taxon;
 	}
 
 	public Couple<Assessment, MigrationReport> assessmentDataToAssessment(final AssessmentData assessData, final User user) throws DBException, InstantiationException,
 			IllegalAccessException, PersistentException {
 		OccurrenceMigratorUtils.migrateOccurrenceData(assessData);
 		
 		final MigrationReport report = new MigrationReport();
 		
 		final Assessment assessment = new Assessment();
 		assessment.setSchema("org.iucn.sis.server.schemas.redlist");
 		assessment.setInternalId(assessData.getAssessmentID());
 		assessment.setSource(assessData.getSource());
 		assessment.setSourceDate(assessData.getSourceDate());
 		assessment.setType(assessData.getType());
 		assessment.setDateFinalized(assessData.getDateFinalized());
 		assessment.setTaxon(getTaxon(report, assessData));
 		assessment.setIsHistorical(assessData.isHistorical());
 		
 		if (AssessmentData.DRAFT_ASSESSMENT_STATUS.equals(assessData.getType())) {
 			String dateAssessed = assessData.getDateAssessed();
 			if (dateAssessed != null && !"".equals(dateAssessed)) {
 				try {
 					assessment.setDateAssessed(shortfmt.parse(dateAssessed.replace('/', '-')));
 				} catch (Exception e) {
 					e.printStackTrace();
 					TrivialExceptionHandler.ignore(this, e);
 				}
 			}
 		}
 		else {
 			if (assessData.getDateAssessed() == null || "".equals(assessData.getDateAssessed()))
 				assessData.setDateAssessed("1900-01-01");
 			
 			assessData.setDateAssessed(assessData.getDateAssessed().replace('/', '-'));
 			
 			try {
 				assessment.setDateAssessed(shortfmt.parse(assessData.getDateAssessed()));
 			} catch (Exception e) {
 				e.printStackTrace();
 				TrivialExceptionHandler.ignore(this, e);
 			}
 			
 			if (assessment.getDateAssessed() == null)
 				assessData.setDateAssessed("1900-01-01");
 		}
 		assessment.generateFields();
 
 		for (final Entry<String, Object> curField : assessData.getDataMap().entrySet()) {
 			//Translate data
 			processField(report, curField, assessment, new ComplexListener<Field>() {
 				public void handleEvent(Field field) {
 					//Translate references
 					for (ReferenceUI curRef : assessData.getReferences(curField.getKey())) {
 						Reference ref;
 						try {
 							ref = referenceIO.getReferenceByHashCode(curRef.getReferenceID());
 						} catch (PersistentException e) {
 							continue;
 						}
 						
 						if (ref != null) {
 							field.getReference().add(ref);
 //							ref.getField().add(field);
 						}
 					}
 					
 					Set<Notes> notes = null;
 					try {
 						notes = getNotesForField(assessData.getType(), assessData.getAssessmentID(), field, user);
 					} catch (Exception e) {
 						notes = null;
 					}
 					if (notes != null && !notes.isEmpty())
 						field.setNotes(notes);
 					
 					if (field.hasData() || !field.getReference().isEmpty() || !field.getNotes().isEmpty())
 						assessment.getField().add(field);
 				}
 			});
 		}
 
 		for (ReferenceUI curRef : assessData.getReferences("global")) {
 			Reference ref = referenceIO.getReferenceByHashCode(curRef.getReferenceID());
 			if (ref != null) {
 				assessment.getReference().add(ref);
 				ref.getAssessment().add(assessment);
 			}
 		}
 
 		return new Couple<Assessment, MigrationReport>(assessment, report);
 	}
 	
 	private Set<Notes> getNotesForField(String type, String assessmentID, Field field, User user) throws Exception {
 		final VFS vfs = data.getOldVFS();
 		final String canonicalName = field.getName();
 		final String url;
 		if (vfs.exists("/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml"))
 			url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName + ".xml";
 		else if (vfs.exists("/notes/" + type + "/" + assessmentID + "/" + canonicalName))
 			url = "/notes/" + type + "/" + assessmentID + "/" + canonicalName;
 		//FIXME: hmm...?
 		/*else if (vfs.exists("/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml"))
 			url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName + ".xml";
 		else if (vfs.exists("/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName))
 			url = "/notes/" + type + "/" + username + "/" + assessmentID + "/" + canonicalName;*/
 		else
 			return new HashSet<Notes>();
 		
 		final NativeDocument document = new JavaNativeDocument();
 		document.parse(vfs.getString(url));
 		
 		final Set<Notes> notes = new HashSet<Notes>();
 		
 		for (Note oldNote : Note.notesFromXML(document.getDocumentElement())) {
 			if ("DEMImport".equalsIgnoreCase(oldNote.getUser()))
 				continue;
 			
 			String text = oldNote.getBody();
 			if (text == null)
 				continue;
 			
 			User possibleUser = userIO.getUserFromUsername(oldNote.getUser());
 			if (possibleUser == null) {
 				possibleUser = user;
 				text += " -- " + oldNote.getUser();
 			}
 			
 			final Edit edit = new Edit();
 			edit.setUser(user);
 			edit.setCreatedDate(shortfmt.parse(oldNote.getDate()));
 			
 			final Notes note = new Notes();
 			note.setField(field);
 			note.setValue(text);
 			
 			note.setEdit(edit);
 			edit.getNotes().add(note);
 			
 			notes.add(note);
 		}
 		
 		return notes;
 	}
 	
 	private void processField(MigrationReport report, Entry<String, Object> curField, Assessment assessment, ComplexListener<Field> callback) throws DBException, InstantiationException,
 			IllegalAccessException, PersistentException {
 		final Field field = new Field(correctFieldName(curField.getKey()), assessment);
 		
 		Row.Set lookup = getLookup(curField.getKey());
 		
 		List<Field> subfields = new ArrayList<Field>();
 
 		if (curField.getKey().equals(CanonicalNames.Lakes) || curField.getKey().equals(CanonicalNames.Rivers) || 
 				curField.getKey().equals(CanonicalNames.RedListAssessmentDate)) {
 			//DO NOTHING
 		} else if (curField.getKey().equals(CanonicalNames.UseTradeDetails) ||
 				curField.getKey().equals(CanonicalNames.Livelihoods)) {
 			
 			List<String> dataList = (List<String>) (curField.getValue());
 			if( dataList.size() > 1 ) {
 				String subfieldName = curField.getKey().equals(CanonicalNames.UseTradeDetails) ? 
 						"UseTradeSubfield" : "LivelihoodsSubfield";
 				int subfieldDataSize = curField.getKey().equals(CanonicalNames.UseTradeDetails) ? 
 						10 : 18;
 				
 				Integer numStresses = dataList.get(0).matches("\\d") ? Integer.valueOf(dataList.get(0)) : 0;
 				dataList.remove(0);
 						
 				for( int i = 0; i < numStresses.intValue(); i++ ) {
 					List<String> rawData = dataList.subList(subfieldDataSize*i, (subfieldDataSize*(i+1)) );
 					Field subfield = new Field(subfieldName, null);
 					addPrimitiveDataToField(report, field.getName(), subfield, rawData, getLookup(subfieldName));
 					subfields.add(subfield);
 				}
 				
 				field.getFields().addAll(subfields);
 			}
 		} else if (curField.getValue() instanceof List) {
 			if (CanonicalNames.RedListAssessors.equals(curField.getKey()) || 
 					CanonicalNames.RedListContributors.equals(curField.getKey()) || 
 					CanonicalNames.RedListEvaluators.equals(curField.getKey())) {
 				RedListCreditedUserField proxy = new RedListCreditedUserField(field);
 				
 				List<String> rawData = (List<String>) (curField.getValue());
 				if (!rawData.isEmpty()) {
 					proxy.setText(rawData.get(0));
 					 
 					StringBuilder order = new StringBuilder();
 					List<Integer> userIDs = new ArrayList<Integer>();
 					List<User> users = new ArrayList<User>();
 					for (int i = 1; i < rawData.size(); i++) {
 						final Integer userID;
 						try {
 							userID = Integer.valueOf(rawData.get(i));
 						} catch (Exception e) {
 							continue;
 						}
 						 
 						userIDs.add(userID);
 						order.append(userID + ",");
 						 
 						User user = null;
 						try {
 							user = (User)session.get(User.class, userID);
 						} catch (Exception e) {
 							continue;
 						}
 						if (user != null)
 							users.add(user);
 					}
 					 
 					if (!userIDs.isEmpty()) {
 						proxy.setUsers(userIDs);
 						proxy.setOrder(order.toString().substring(0, order.toString().length()-1));
 					}
 				}
 			}
 			else if (CanonicalNames.RedListSource.equals(curField.getKey())) {
 				List<String> rawData = (List)curField.getValue();
 				if (rawData.size() > 1) {
 					/*
 					 * If more than one data point exists, we remove 
 					 * the most-recent flag.  Then, we take only the 
 					 * first element via sublist.  Typically, after 
 					 * the removal of most recent, there will only 
 					 * be one element in the list, but either way, 
 					 * we only take the first one.  We are guaranteed 
 					 * to have at least one. 
 					 */
 					List<String> slimData = new ArrayList<String>(rawData);
 					slimData.remove("MOST RECENT-NEEDS UPDATING");
 					
 					rawData = slimData.subList(0, 1);
 				}
 					
 				addPrimitiveDataToField(report, curField.getKey(), field, rawData, lookup);	
 			}
 			else if (CanonicalNames.InPlaceEducation.equals(curField.getKey())) {
 				List<String> rawData = (List)curField.getValue();
 				if (!rawData.isEmpty()) {
 					if (!isBlank(rawData, 0)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 0, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceEducationSubjectToPrograms, 
 							assessment));
 					}
 					 
 					if (!isBlank(rawData, 2)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 2, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceEducationInternationalLegislation, 
 							assessment));
 					}
 					 
 					if (!isBlank(rawData, 4)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 4, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceEducationControlled, 
 							assessment));
 					}
 				}
 				else
 					error(4, report, "Found %s, but with only %s data fields.  Failed to convert.\n%s", curField.getKey(), rawData.size(), curField.getValue());
 			}
 			else if (CanonicalNames.InPlaceLandWaterProtection.equals(curField.getKey())) {
 				List<String> rawData = (List)curField.getValue();
 				if (!rawData.isEmpty()) {
 					if (!isBlank(rawData, 0)) {
 						int index = Integer.parseInt(rawData.get(0));
 						if (index == 0 && "1".equals(rawData.get(1)))
 							index = 2;
 						else
 							index++;
 						
 						Field controlled = new Field(correctFieldName(org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceLandWaterProtectionSitesIdentified), assessment); 
 						controlled.addPrimitiveField(new ForeignKeyPrimitiveField("value", controlled, index, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceEducationControlled + "_valueLookup"
 						));
 						controlled.addPrimitiveField(new StringPrimitiveField("note", controlled, rawData.get(2)));
 						
 						callback.handleEvent(controlled);
 					}
 					
 					if (!isBlank(rawData, 3)) {
 						Field inPA = new Field(correctFieldName(org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceLandWaterProtectionInPA), assessment); 
 						inPA.addPrimitiveField(new ForeignKeyPrimitiveField("value", inPA, Integer.valueOf(rawData.get(3))+1, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceLandWaterProtectionInPA + "_valueLookup"
 						));
 						inPA.addPrimitiveField(new StringPrimitiveField("note", inPA, rawData.get(5)));
 						
 						callback.handleEvent(inPA);
 					}
 					
 					if (!isBlank(rawData, 4)) {						
 						Field percentProtected = 
 							new Field(correctFieldName(org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceLandWaterProtectionPercentProtected), assessment);
 						percentProtected.addPrimitiveField(new StringPrimitiveField(
 							"value", percentProtected, rawData.get(4)
 						));
 						percentProtected.addPrimitiveField(new StringPrimitiveField("note", percentProtected, rawData.get(5)));
 						
 						callback.handleEvent(percentProtected);
 					}
 					
 					if (!isBlank(rawData, 6)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 6, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceLandWaterProtectionAreaPlanned, 
 							assessment));
 					}
 					
 					if (!isBlank(rawData, 8)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 8, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceLandWaterProtectionInvasiveControl, 
 							assessment));
 					}
 				}
 				else
 					error(4, report, "Found %s, but with only %s data fields.  Failed to convert.\n%s", curField.getKey(), rawData.size(), curField.getValue());
 			}
 			else if (CanonicalNames.InPlaceResearch.equals(curField.getKey())) {
 				List<String> rawData = (List)curField.getValue();
 				if (!rawData.isEmpty()) {
 					if (!isBlank(rawData, 0)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 0, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceResearchMonitoringScheme, 
 							assessment));
 					}
 					if (!isBlank(rawData, 2)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 2, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceResearchRecoveryPlan, 
 							assessment));
 					}
 				}
 				else
 					error(4, report, "Found %s, but with only %s data fields.  Failed to convert.\n%s", curField.getKey(), rawData.size(), curField.getValue());
 			}
 			else if (CanonicalNames.InPlaceSpeciesManagement.equals(curField.getKey())) {
 				List<String> rawData = (List)curField.getValue();
 				if (!rawData.isEmpty()) {
 					if (!isBlank(rawData, 0)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 0, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceSpeciesManagementHarvestPlan, 
 							assessment));
 					}
 					
 					if (!isBlank(rawData, 2)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 2, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceSpeciesManagementReintroduced, 
 							assessment));
 					}
 					
 					if (!isBlank(rawData, 4)) {
 						callback.handleEvent(createSimpleInPlaceFieldWithNote(rawData, 4, 
 							org.iucn.sis.shared.api.utils.CanonicalNames.InPlaceSpeciesManagementExSitu, 
 							assessment));
 					}
 				}
 				else
 					error(4, report, "Found %s, but with only %s data fields.  Failed to convert.\n%s", curField.getKey(), rawData.size(), curField.getValue());
 			}
 			else {
 				List<String> rawData = (List<String>) (curField.getValue());
 				addPrimitiveDataToField(report, curField.getKey(), field, rawData, lookup);
 			}
 		} else {
 			// It's a classification scheme!
 			Map<String, List<String>> dataMap = (Map<String, List<String>>) curField.getValue();
 			if( !field.getName().equals(CanonicalNames.Threats)) {
 				for( Entry<String, List<String>> selected : dataMap.entrySet() ) {
 					Field subfield = new Field(field.getName() + "Subfield", null);
 					List<String> dataList = selected.getValue();
 					dataList.add(0, selected.getKey()); //Add the class scheme ID back in
 
 					addPrimitiveDataToField(report, curField.getKey(), subfield, dataList, getLookup(subfield.getName()));
 					subfield.setParent(field);
 					subfields.add(subfield);
 				}
 				field.getFields().addAll(subfields);
 			} else {
 				for( Entry<String, List<String>> selected : dataMap.entrySet() ) {
 					Field subfield = new Field(field.getName() + "Subfield", null);
 					List<String> dataList = selected.getValue();
 					dataList.add(0, selected.getKey()); //Add the threat ID back in
 
 					List<String> threatData = dataList.subList(0, 5);
 					
 					addPrimitiveDataToField(report, curField.getKey(), subfield, threatData, getLookup(
 							subfield.getName()));
 					subfield.setParent(field);
 					
 					if( dataList.size() > 6 ) {
 						Integer numStresses = dataList.get(6).matches("\\d") ? Integer.valueOf(dataList.get(6)) : 0;
 
 						for( int i = 0; i < numStresses.intValue(); i++ ) {
 							Field stress = new Field("StressesSubfield", null);
 							ForeignKeyPrimitiveField fk = new ForeignKeyPrimitiveField("stress", stress);
 							fk.setValue(Integer.valueOf( dataList.get(7+i) ) );
 							fk.setTableID("StressesLookup");
 							stress.getPrimitiveField().add(fk);
 							fk.setField(stress);
 							subfields.add(stress);
 						}
 
 						subfields.add(subfield);
 					}
 				}
 				field.getFields().addAll(subfields);
 			}
 		}
 		
 		callback.handleEvent(field);
 	}
 	
 	private Field createSimpleInPlaceFieldWithNote(List<String> rawData, int dataIndex, String fieldName, Assessment assessment) {
 		Field field = new Field(correctFieldName(fieldName), assessment);
 		field.addPrimitiveField(new ForeignKeyPrimitiveField(
 			"value", field, Integer.valueOf(rawData.get(dataIndex))+1, 
 			fieldName + "_valueLookup"
 		));
 		field.addPrimitiveField(new StringPrimitiveField("note", field, rawData.get(dataIndex + 1)));
 		
 		return field;
 	}
 	
 	private boolean isBlank(List<String> rawData, int index) {
 		return index < 0 || index >= rawData.size() || 
 			rawData.get(index) == null || "".equals(rawData.get(index));
 	}
 	
 	/**
 	 * Perform any corrections or changes to field names 
 	 * from SIS 1 to SIS 2
 	 * @param name
 	 * @return
 	 */
 	private String correctFieldName(String name) {
 		if (CanonicalNames.ReproduictivePeriodicity.equals(name))
 			return org.iucn.sis.shared.api.utils.CanonicalNames.ReproductivePeriodicity;
 		
 		return name;
 	}
 
 	private void addPrimitiveDataToField(MigrationReport report, String canonicalName, Field field, List<String> rawData,
 			Row.Set lookup) throws InstantiationException, IllegalAccessException,
 			DBException {
 		PrimitiveField prim = null;
 		Row curRow = null;
 		int i = 0;
 		
 		debug("Found %s/%s primitive values for %s", rawData.size(), lookup.getSet().size(), canonicalName);
 		
 		if (rawData.size() > lookup.getSet().size()) {
			if (!(lookup.getSet().size() == 1 && lookup.getSet().get(0).get("data_type").equals("fk_list_primitive_field")))
 				error(4, report, "Found more data in SIS 1 than can fit in SIS 2 for %s\n%s", canonicalName, rawData);
 		}
 		
 		for (String curPrimitive : rawData) {
 			if (lookup.getSet().size() <= i) {
 				error(5, "Extra piece of data found for %s, Eliding: '%s'", canonicalName, curPrimitive);
 				continue;
 			}
 				
 			curRow = lookup.getSet().get(i);
 			
 			debug("Setting data point %s as %s.%s -> %s", i, field.getName(), curRow.get("name"), curPrimitive);
 			
 			String type = curRow.get("data_type").getString();
 			if (typeLookup.get(type) != Object.class) {
 				if( prim != null && (type.equals("fk_list_primitive_field")) ) {
 					if (!(prim instanceof ForeignKeyListPrimitiveField)) {
 						printf("Trying to magically set %s.%s to %s", field.getName(), curRow.get("name"), curPrimitive);
 						error(3, "Skipping as current field %s.%s is not a fk_list", field.getName(), prim.getName());
 						continue;
 					}
 					PrimitiveField newPrim = (PrimitiveField) typeLookup.get(type).newInstance();
 					newPrim.setRawValue(curPrimitive);
 					
 					prim.appendValue(newPrim.getValue());
 				} else if (!(curPrimitive == null || curPrimitive.equalsIgnoreCase(""))) {
 					prim = (PrimitiveField) typeLookup.get(type).newInstance();
 					prim.setName(curRow.get("name").getString());
 					
 					if (prim instanceof StringPrimitiveField) {
 						if (curPrimitive.length() > 1023) {
 							error(2, report, "On field %s.%s in assessment %s, this string has been chopped because it is too long: %s", field.getName(), prim.getName(), field.getAssessment().getInternalId(), curPrimitive);
 							
 							curPrimitive = curPrimitive.substring(0, 1023);
 							
 							prim = new TextPrimitiveField(prim.getName(), field, curPrimitive);
 						} else
 							prim.setRawValue(curPrimitive);
 					} else if (prim instanceof ForeignKeyPrimitiveField ) {
 						//Do the indexed lookup.
 						String lookupTable = curRow.get("name").getString().endsWith("Lookup") ?
 								curRow.get("name").getString() : getTableID(canonicalName, curRow.get("name").getString());
 						((ForeignKeyPrimitiveField) prim).setTableID(lookupTable);
 						
 						Integer index = getIndex(report, canonicalName, lookupTable, curRow.get("name").getString(), curPrimitive);
 						if (index > 0)
 							prim.setValue(index);
 						else {
 							prim = null;
 							i++;
 							continue;
 						}
 					} else if ( prim instanceof ForeignKeyListPrimitiveField ) {
 						String lookupTable = curRow.get("name").getString().endsWith("Lookup") ?
 								curRow.get("name").getString() : getTableID(canonicalName, curRow.get("name").getString());
 						((ForeignKeyListPrimitiveField) prim).setTableID(lookupTable);
 						
 						try {
 							prim.setRawValue(curPrimitive);
 						} catch (Exception e) {
 							error(2, report, "Error setting foreign key list data '%s' " +
 								"for field %s.%s", curPrimitive, prim.getName(), field.getName());
 						}
 						
 						if( canonicalName.equals(CanonicalNames.RegionInformation)) {
 							List<Integer> data = ((ForeignKeyListPrimitiveField)prim).getValue();
 							List<Integer> newData = new ArrayList<Integer>();
 							
 							for( Integer cur : data )
 								newData.add(RegionConverter.getNewRegionID(cur));
 							
 							prim.setValue(newData);
 						}
 						
 					} else if ( prim instanceof DatePrimitiveField ) {
 						String format = "yyyy-MM-dd";
 						String formatWithTime = "yyyy-MM-dd HH:mm:ss";
 						String formatSlashes = "yyyy/MM/dd";
 						String formatYear = "yyyy";
 						SimpleDateFormat formatter = new SimpleDateFormat(format);
 						SimpleDateFormat formatterWithTime = new SimpleDateFormat(formatWithTime);
 						SimpleDateFormat formatterWithSlashes = new SimpleDateFormat(formatSlashes);
 						SimpleDateFormat formatterYear = new SimpleDateFormat(formatYear);
 						
 						try {
 							prim.setValue(formatter.parse(curPrimitive));
 						} catch (ParseException e) {
 							try {
 								prim.setValue(formatterWithTime.parse(curPrimitive));
 							} catch (ParseException e1) {
 								try {
 									prim.setValue(formatterWithSlashes.parse(curPrimitive));
 								} catch (ParseException e2) {
 									try {
 										//Strip out non-year characters, first.
 										if( curPrimitive.replaceAll("\\D", "").matches("\\d{4}"))
 											curPrimitive = curPrimitive.replaceAll("\\D", "");
 										
 										prim.setValue(formatterYear.parse(curPrimitive));
 									} catch (ParseException e3) {
 										error(3, report, "Unable to parse date for %s.%s on assessment %s: %s", 
 											curPrimitive, field.getName(), prim.getName(), field.getAssessment().getInternalId());
 										
 										prim.setValue(new Date());
 										
 										Notes note = new Notes();
 										note.setValue("Unable to port data '" + curPrimitive + "' because it is not a valid Date format.");
 										
 										field.getNotes().add(note);
 										note.setField(field);
 									}
 								}
 							}
 						}
 					} else if (prim instanceof BooleanPrimitiveField) {
 						//Don't store false/null values
 						if (curPrimitive == null || "false".equals(curPrimitive.toLowerCase()))
 							prim = null;
 						else
 							prim.setRawValue(curPrimitive);
 					} else {
 						try {
 							prim.setRawValue(curPrimitive);
 						} catch (NumberFormatException e) {
 							error(3, report, "NumberFormatException on %s.%s in asm %s: '%s'",
 								field.getName(), prim.getName(), field.getAssessment().getInternalId(), curPrimitive);
 							
 							Notes note = new Notes();
 							note.setValue("Unable to port data '" + curPrimitive + "' because it is not a number.");
 							
 							field.getNotes().add(note);
 							note.setField(field);
 							
 							String nonNumeric = Replacer.stripNonNumeric(curPrimitive);
 							if ("".equals(nonNumeric))
 								nonNumeric = "0";
 							
 							prim.setValue(Float.valueOf(nonNumeric));
 						}
 					}
 					
 					if (prim != null) {
 						prim.setField(field);
 						prim.setName(curRow.get("name").getString());
 						field.getPrimitiveField().add(prim);
 					}
 					
 					if( lookup.getSet().size() > i+1 ) {
 						i++;
 						prim = null;
 					}
 				} else
 					i++;
 			} else {
 				// HANDLE NESTED FIELD TYPE
 			}
 		}
 	}
 
 	private String getTableID(String canonicalName, String name) {
 		return canonicalName + "_" + name + "Lookup";
 	}
 	
 	private Integer getIndex(MigrationReport report, String canonicalName, String libraryTable, String name, String value) throws DBException {
 //		String table = canonicalName + "_" + name + "Lookup";
 		
 		for( Row row : getLookup(libraryTable).getSet() ) {
 			if (row.get("code") != null) { 
 				if (value.equalsIgnoreCase(row.get("code").getString()))
 					return row.get("id").getInteger();
 			} else if( value.equalsIgnoreCase(row.get("label").getString()) || 
 					value.equalsIgnoreCase( Integer.toString((Integer.parseInt(
 							row.get("name").getString())+1)) ) )
 				return row.get("id").getInteger();
 		}
 		if( !value.equals("0") ) {
 			error(3, report, "For %s.%s, didn't find a lookup in %s to match: %s", 
 				canonicalName, name, libraryTable, value);
 			return -1;
 		} else
 			return 0;
 	}
 	
 	private Row.Set getLookup(String table) throws DBException {
 		String fieldName = table;
 		if (fieldName.equalsIgnoreCase(CanonicalNames.ReproduictivePeriodicity))
 			fieldName = org.iucn.sis.shared.api.utils.CanonicalNames.ReproductivePeriodicity;
 		
 		if (lookups.containsKey(fieldName))
 			return lookups.get(fieldName);
 		else {
 			SelectQuery query = new SelectQuery();
 			query.select(fieldName, "ID", "ASC");
 			query.select(fieldName, "*");
 			
 			Row.Set lookup = new Row.Set();
 			
 			ec.doQuery(query, lookup);
 
 			lookups.put(fieldName, lookup);
 
 			return lookup;
 		}
 	}
 	
 	private void debug(String template, Object... args) {
 		//printf(template, args);
 	}
 	
 	private void error(int level, String template, Object... args) {
 		error(level, null, template, args);
 	}
 	
 	private void error(int level, MigrationReport report, String template, Object... args) {
 		StringBuilder builder = new StringBuilder();
 		for (int i = 0; i < level; i++)
 			builder.append('#');
 		builder.append(" Level " + level + " Error: ");
 		builder.append(template);
 		
 		printf(builder.toString(), args);
 		
 		if (report != null)
 			report.addError(String.format(builder.toString(), args));
 	}
 	
 	private void warning(MigrationReport report, String template, Object... args) {
 		warning(report, true, template, args);
 	}
 	
 	private void warning(MigrationReport report, boolean debug, String template, Object... args) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Warning: ");
 		builder.append(template);
 		
 		if (debug)
 			printf(builder.toString(), args);
 		
 		if (report != null)
 			report.addWarning(String.format(builder.toString(), args));
 	}
 	
 }
