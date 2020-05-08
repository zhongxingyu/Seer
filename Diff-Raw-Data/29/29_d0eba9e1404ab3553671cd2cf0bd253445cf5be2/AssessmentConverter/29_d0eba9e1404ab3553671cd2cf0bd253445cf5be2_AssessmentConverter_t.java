 package org.iucn.sis.shared.conversions;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.naming.NamingException;
 
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.dialect.PostgreSQLDialect;
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
 import org.iucn.sis.shared.helpers.ReferenceUI;
 
 import com.solertium.db.DBException;
 import com.solertium.db.DBSession;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.SystemExecutionContext;
 import com.solertium.db.Row.Set;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.lwxml.factory.NativeDocumentFactory;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.TrivialExceptionHandler;
 import com.solertium.vfs.VFS;
 
 public class AssessmentConverter extends GenericConverter<VFSInfo> {
 	
 	private static final DateFormat shortfmt = new SimpleDateFormat("yyyy-MM-dd");
 	
 	public static enum ConversionMode {
 		DRAFT, PUBLISHED, ALL
 	}
 	
 	private ExecutionContext ec;
 	private Map<String, Set> lookups;
 
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
 		
 		lookups = new HashMap<String, Set>();
 
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
 				
 			Assessment assessment = assessmentDataToAssessment(parser.getAssessment());
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
 					
 					Assessment assessment = assessmentDataToAssessment(parser.getAssessment());
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
 	
 	private Taxon getTaxon(AssessmentData assessData) {
 		Taxon taxon = taxonIO.getTaxon(Integer.valueOf(assessData.getSpeciesID()));
 		if (taxon != null && taxon.getFriendlyName().equalsIgnoreCase(assessData.getSpeciesName()))
 			return taxon;
 		else if (taxon != null)
 			printf("Found taxa %s by ID, but name %s doesn't match expected name %s", assessData.getSpeciesID(), taxon.getFriendlyName(), assessData.getSpeciesName());
 		
 		Taxon byName = (Taxon)session.createCriteria(Taxon.class).
 			add(Restrictions.eq("friendlyName", assessData.getSpeciesName()))
 			.uniqueResult();
 		if (byName == null)
 			printf("No taxon found matching %s", assessData.getSpeciesName());
 		else
 			return byName;
 		
 		return taxon;
 	}
 
 	public Assessment assessmentDataToAssessment(AssessmentData assessData) throws DBException, InstantiationException,
 			IllegalAccessException, PersistentException {
 		OccurrenceMigratorUtils.migrateOccurrenceData(assessData);
 		
 		Assessment assessment = new Assessment();
 		assessment.setSchema("org.iucn.sis.server.schemas.redlist");
 		assessment.setInternalId(assessData.getAssessmentID());
 		assessment.setSource(assessData.getSource());
 		assessment.setSourceDate(assessData.getSourceDate());
 		assessment.setType(assessData.getType());
 		assessment.setDateFinalized(assessData.getDateFinalized());
 		assessment.setTaxon(getTaxon(assessData));
 		
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
 				throw new NullPointerException("No date assessed!");
 		}
 		assessment.generateFields();
 
 		for (Entry<String, Object> curField : assessData.getDataMap().entrySet()) {
 			Set lookup = getLookup(curField.getKey());
 			
			Field field = new Field(correctFieldName(curField.getKey()), assessment);
 			field.setAssessment(assessment);
 			
 			List<Field> subfields = new ArrayList<Field>();
 
 			if (curField.getKey().equals(CanonicalNames.Lakes) || curField.getKey().equals(CanonicalNames.Rivers)) {
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
 						Field subfield = new Field(subfieldName, assessment);
 						addPrimitiveDataToField(field.getName(), subfield, rawData, getLookup(subfieldName));
 						subfields.add(subfield);
 					}
 					
 					field.getFields().addAll(subfields);
 				}
 			} else if (curField.getValue() instanceof List) {
 				List<String> rawData = (List<String>) (curField.getValue());
 				addPrimitiveDataToField(curField.getKey(), field, rawData, lookup);
 			} else {
 				// It's a classification scheme!
 				Map<String, List<String>> dataMap = (Map<String, List<String>>) curField.getValue();
 				if( !field.getName().equals(CanonicalNames.Threats)) {
 					for( Entry<String, List<String>> selected : dataMap.entrySet() ) {
 						Field subfield = new Field(field.getName() + "Subfield", assessment);
 						List<String> dataList = selected.getValue();
 						dataList.add(0, selected.getKey()); //Add the class scheme ID back in
 
 						addPrimitiveDataToField(curField.getKey(), subfield, dataList, getLookup(subfield.getName()));
 						subfield.setParent(field);
 						subfields.add(subfield);
 					}
 					field.getFields().addAll(subfields);
 				} else {
 					for( Entry<String, List<String>> selected : dataMap.entrySet() ) {
 						Field subfield = new Field(field.getName() + "Subfield", assessment);
 						List<String> dataList = selected.getValue();
 						dataList.add(0, selected.getKey()); //Add the threat ID back in
 
 						List<String> threatData = dataList.subList(0, 5);
 						
 						addPrimitiveDataToField(curField.getKey(), subfield, threatData, getLookup(
 								subfield.getName()));
 						subfield.setParent(field);
 						
 						if( dataList.size() > 6 ) {
 							Integer numStresses = dataList.get(6).matches("\\d") ? Integer.valueOf(dataList.get(6)) : 0;
 
 							for( int i = 0; i < numStresses.intValue(); i++ ) {
 								Field stress = new Field("StressesSubfield", assessment);
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
 
 			for (ReferenceUI curRef : assessData.getReferences(curField.getKey())) {
 				Reference ref = referenceIO.getReferenceByHashCode(curRef.getReferenceID());
 				if (ref != null) {
 					field.getReference().add(ref);
 //					ref.getField().add(field);
 				}
 			}
 			
 			//TODO: don't see where notes are being parsed...
 			if (field.hasData() || !field.getReference().isEmpty() || !field.getNotes().isEmpty())
 				assessment.getField().add(field);
 		}
 
 		for (ReferenceUI curRef : assessData.getReferences("global")) {
 			Reference ref = referenceIO.getReferenceByHashCode(curRef.getReferenceID());
 			if (ref != null) {
 				assessment.getReferences().add(ref);
 				ref.getAssessment().add(assessment);
 			}
 		}
 
 		return assessment;
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
 
 	private void addPrimitiveDataToField(String canonicalName, Field field, List<String> rawData,
 			Set lookup) throws InstantiationException, IllegalAccessException,
 			DBException {
 		PrimitiveField prim = null;
 		Row curRow = null;
 		int i = 0;
 		for (String curPrimitive : rawData) {
 			if( lookup.getSet().size() <= i ) {
 				print("**** Extra piece of data found for " + canonicalName + ". Eliding.");
 				continue;
 			}
 				
 			curRow = lookup.getSet().get(i);
 			
 			String type = curRow.get("data_type").getString();
 			if (typeLookup.get(type) != Object.class) {
 				if( prim != null && (type.equals("fk_list_primitive_field")) ) {
 					PrimitiveField newPrim = (PrimitiveField) typeLookup.get(type).newInstance();
 					newPrim.setRawValue(curPrimitive);
 					prim.appendValue(newPrim.getValue());
 				} else if (!(curPrimitive == null || curPrimitive.equalsIgnoreCase(""))) {
 					prim = (PrimitiveField) typeLookup.get(type).newInstance();
 					
 					if( prim instanceof StringPrimitiveField ) {
 						if( curPrimitive.length() >= 1023 ) {
 							print("on current field " + field.getName() + "found some too-long string data in assessment " + field.getAssessment().getInternalId());
 							
 							try {
 								Thread.sleep(10000);
 							} catch (InterruptedException e) {}
 							prim = new TextPrimitiveField(prim.getName(), field, curPrimitive);
 						} else
 							prim.setRawValue(curPrimitive);
 					} else if( prim instanceof ForeignKeyPrimitiveField ) {
 						//Do the indexed lookup.
 						String lookupTable = curRow.get("name").getString().endsWith("Lookup") ?
 								curRow.get("name").getString() : getTableID(canonicalName, curRow.get("name").getString());
 						((ForeignKeyPrimitiveField) prim).setTableID(lookupTable);
 						Integer index = getIndex(canonicalName, lookupTable, curRow.get("name").getString(), curPrimitive);
 						if( index > -1 )
 							prim.setValue(index);
 						else {
 							print("...in assessment " + field.getAssessment().getInternalId());
 							continue;
 						}
 					} else if( prim instanceof ForeignKeyListPrimitiveField ) {
 						String lookupTable = curRow.get("name").getString().endsWith("Lookup") ?
 								curRow.get("name").getString() : getTableID(canonicalName, curRow.get("name").getString());
 						((ForeignKeyListPrimitiveField) prim).setTableID(lookupTable);
 						
 						try {
 							prim.setRawValue(curPrimitive);
 						} catch (Exception e) {
 							e.printStackTrace();
 							print("ERROR setting data " + curPrimitive + 
 									" for primitive " + prim.getName() + " in field " + field.getName());
 						}
 						
 						if( canonicalName.equals(CanonicalNames.RegionInformation)) {
 							List<Integer> data = ((ForeignKeyListPrimitiveField)prim).getValue();
 							List<Integer> newData = new ArrayList<Integer>();
 							
 							for( Integer cur : data )
 								newData.add(RegionConverter.getNewRegionID(cur));
 							
 							prim.setValue(newData);
 						}
 						
 					} else if( prim instanceof DatePrimitiveField ) {
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
 										print("*****UNABLE TO PARSE DATE " + curPrimitive + " on assessment " + field.getAssessment().getInternalId());
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
 							print("*****NUMBER FORMAT ISSUES WITH DATA " + curPrimitive + "on field " + field.getName() + " in assessment " + field.getAssessment().getInternalId() );
 							Notes note = new Notes();
 							note.setValue("Unable to port data '" + curPrimitive + "' because it is not a number.");
 							field.getNotes().add(note);
 							note.setField(field);
 							prim.setValue(Float.valueOf(0.0f));
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
 	
 	private Integer getIndex(String canonicalName, String libraryTable, String name, String value) throws DBException {
 //		String table = canonicalName + "_" + name + "Lookup";
 		
 		for( Row row : getLookup(libraryTable).getSet() ) {
 			if( row.get("code") != null ) { 
 				if ( value.equalsIgnoreCase(row.get("code").getString()) )
 					return row.get("id").getInteger();
 			} else if( value.equalsIgnoreCase(row.get("label").getString()) || 
 					value.equalsIgnoreCase( Integer.toString((Integer.parseInt(
 							row.get("name").getString())+1)) ) )
 				return row.get("id").getInteger();
 		}
 		if( !value.equals("0") ) {
 			print("Didn't match value " + value + " to lookup " + libraryTable);
 			return -1;
 		} else
 			return 0;
 	}
 	
 	private Set getLookup(String fieldName) throws DBException {
 		if (lookups.containsKey(fieldName))
 			return lookups.get(fieldName);
 		else {
 			SelectQuery query = new SelectQuery();
 			query.select(fieldName, "ID", "ASC");
 			query.select(fieldName, "*");
 			
 			Set lookup = new Set();
 			
 			ec.doQuery(query, lookup);
 
 			lookups.put(fieldName, lookup);
 
 			return lookup;
 		}
 	} 
 }
