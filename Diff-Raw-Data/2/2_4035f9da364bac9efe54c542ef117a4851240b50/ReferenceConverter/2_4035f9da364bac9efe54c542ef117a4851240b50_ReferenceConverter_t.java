 package org.iucn.sis.shared.conversions;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.hibernate.HibernateException;
 import org.iucn.sis.shared.api.models.Reference;
 
 import com.solertium.db.DBSession;
 import com.solertium.db.DBSessionFactory;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.RowProcessor;
 import com.solertium.db.SystemExecutionContext;
 
 public class ReferenceConverter extends Converter {
 	
 	public ReferenceConverter() {
 		super();
 		setClearSessionAfterTransaction(true);
 	}
 
 	@Override
 	protected void run() throws Exception {
 		final AtomicInteger converted = new AtomicInteger(0);
 		
 		DBSession db = DBSessionFactory.getDBSession("ref_lookup");
 		ExecutionContext ec = new SystemExecutionContext(db);
 		ec.setExecutionLevel(ExecutionContext.SQL_ALLOWED);
 		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
 		
 		ec.doQuery("SELECT * FROM BIBLIOGRAPHY;", new RowProcessor() {
 			@Override
 			public void process(Row row) {
 				Reference ref = new Reference();
 				ref.setCitationShort(row.get("CITATION_SHORT").getString());
 				ref.setHash(row.get("BIB_HASH").getString());
 				ref.setBibCode(row.get("BIB_CODE").getInteger());
 				ref.setBibNumber(row.get("BIBLIOGRAPHY_NUMBER").getInteger());
 				ref.setBibNoInt(row.get("BIB_NO_INIT").getInteger());
 				ref.setExternalBibCode(row.get("EXTERNAL_BIB_CODE").getString());
 				ref.setAuthor(row.get("AUTHOR").getString());
 				ref.setYear(row.get("YEAR").getString());
 				ref.setTitle(row.get("TITLE").getString());
 				ref.setSecondaryAuthor(row.get("SECONDARY_AUTHOR").getString());
 				ref.setSecondaryTitle(row.get("SECONDARY_TITLE").getString());
 				ref.setPlacePublished(row.get("PLACE_PUBLISHED").getString());
 				ref.setPublisher(row.get("PUBLISHER").getString());
 				ref.setVolume(row.get("VOLUME").getString());
 				ref.setNumberOfVolumes(row.get("NUMBER_OF_VOLUMES").getString());
 				ref.setNumber(row.get("NUMBER").getString());
 				ref.setPages(row.get("PAGES").getString());
 				ref.setSection(row.get("SECTION").getString());
 				ref.setTertiaryAuthor(row.get("TERTIARY_AUTHOR").getString());
 				ref.setTertiaryTitle(row.get("TERTIARY_TITLE").getString());
 				ref.setEdition(row.get("EDITION").getString());
 				ref.setDateValue(row.get("DATE").getString());
 				ref.setType(row.get("PUBLICATION_TYPE").getString());
				ref.setSubsidiaryAuthor(row.get("SUBSIDIARY_AUTHOR").getString());
 				ref.setShortTitle(row.get("SHORT_TITLE").getString());
 				ref.setAlternateTitle(row.get("ALTERNATE_TITLE").getString());
 				ref.setIsbnIssn(row.get("ISBN/ISSN").getString());
 				ref.setKeywords(row.get("KEYWORDS").getString());
 				ref.setUrl(row.get("URL").getString());
 				ref.setCitationComplete(row.get("CITATION_COMPLETE").getString()!=null && row.get("CITATION_COMPLETE").getString().equalsIgnoreCase("y"));
 				ref.setCitation(row.get("CITATION").getString());
 				ref.setSubmissionType(row.get("SUBMISSION_TYPE").getString());
 				
 				try {
 					session.save(ref);
 					
 					if (converted.addAndGet(1) % 50 == 0) {
 						printf("Converted and saved %s references...", converted.get());
 						commitAndStartTransaction();
 					}
 				} catch (HibernateException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		});
 		/*
 		try {
 			if( converted % 20 != 0 ) {
 				SIS.get().getManager().getSession().getTransaction().commit();
 			}
 		}
 		catch (HibernateException e) {
 			throw new RuntimeException(e);
 		} catch (PersistentException e) {
 			throw new RuntimeException(e);
 		}*/
 
 	}
 }
