 package org.idiginfo.docsvc.jpa.test;
 
 import java.util.GregorianCalendar;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.idiginfo.docsvc.jpa.citagora.AnnotationBodyImpl;
 import org.idiginfo.docsvc.jpa.citagora.ContainerImpl;
 import org.idiginfo.docsvc.jpa.citagora.CitagoraObjectImpl;
 import org.idiginfo.docsvc.jpa.citagora.PersonImpl;
 import org.idiginfo.docsvc.jpa.citagora.ReferenceImpl;
 import org.idiginfo.docsvc.jpa.citagora.ReviewImpl;
 import org.idiginfo.docsvc.jpa.citagora.TagImpl;
 import org.idiginfo.docsvc.model.apisvc.ApiParams;
 import org.idiginfo.docsvc.model.apisvc.DocService;
 import org.idiginfo.docsvc.model.apisvc.Document;
 import org.idiginfo.docsvc.model.apisvc.ServiceFactory;
 import org.idiginfo.docsvc.model.citagora.Container;
 import org.idiginfo.docsvc.model.citagora.RatingType;
 import org.idiginfo.docsvc.model.citagora.Reference;
 import org.idiginfo.docsvc.model.mapping.MapSvcapiToCitagora;
 
 
 /**
  * Class to test Citagora Persistence objects
  * 
  * @author griccardi
  * 
  */
 
 public class TestCitagoraPersistence {
 
 	private EntityManagerFactory emf;
 	private EntityManager em;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		TestCitagoraPersistence tester = new TestCitagoraPersistence();
 		tester.run(args);
 
 	}
 
 	private void run(String[] args) {
		emf = Persistence.createEntityManagerFactory("local");
 		em = emf.createEntityManager();
 		Container doc = createContainer();
 		// Reference ref = createSpringerDocument();
 		em.getTransaction().begin();
 		em.persist(doc);
 		// em.persist(ref);
 		em.getTransaction().commit();
		CitagoraObjectImpl doc2 = em.find(CitagoraObjectImpl.class, doc.getMyId());
 		System.out.println(doc2.getClass().getName());
 	}
 
 	private ContainerImpl createContainer() {
 		ContainerImpl document = new ContainerImpl();
 		// document.setId("http://citagora.com/documents/123456");
 		document.setSource("http://example.com/article");
 		document.setRights("http://www.nlm.nih.gov/databases/license/license.html");
 		// first review
 		ReviewImpl review = new ReviewImpl();
 		review.setDocumentReviewed(document);
 		review.setRatingType(RatingType.getUri("overall"));
 		review.setRating(4);
 		PersonImpl person = new PersonImpl();
 		review.setReviewer(person);
 		person.setAccountName("a_user_foaf");
 		// second review
 		review = new ReviewImpl();
 		review.setDocumentReviewed(document);
 		review.setRatingType("overall");
 		review.setTotalVotes(50);
 		review.setRating(4);
 		// third review
 		review = new ReviewImpl();
 		review.setDocumentReviewed(document);
 		review.setRatingType("overall");
 		review.setTotalVotes(10);
 		review.setRating(3);
 
 		// tag
 		TagImpl tag = new TagImpl();
 		tag.setTarget(document);
 		AnnotationBodyImpl body = (AnnotationBodyImpl) tag.getBody();
 		body.setChars("actual tag");
 		body.setCharacterEncoding("UTF-8");
 		PersonImpl annotator = new PersonImpl();
 		annotator.setAccountName("registered_annotator");
 		tag.setAnnotator(annotator);
 		tag.setAnnotated(new GregorianCalendar(2012, 6, 12).getTime());
 		PersonImpl generator = (PersonImpl) PersonImpl.createCitagoraAgent();
 		generator.setAccountName("http://citagora.com/annotator");
 		tag.setGenerator(generator);
 		tag.setGenerated(new GregorianCalendar(2012, 6, 12).getTime());
 		tag.setModelVersion("http://www.openannotation.org/spec/core/20120509.html");
 
 		// Reference
 		ReferenceImpl reference = new ReferenceImpl();
 		document.setIsAbout(reference);
 		reference.setLanguage("English");
 		reference.addContainer(document);
 		reference
 				.addSeeAlso("another link that also provides some information about this article");
 		reference.setUri("http://example.com/article");
 		reference.setTitle("Some Journal Article");
 		reference.setSubject("some keyword");
 
 		reference.setShortTitle("Short Article");
 		reference
 				.setAbstract("This is an abstract for a journal article. This article discusses something very important. This is an example.");
 		reference.setDoi("doi id");
 		// reference.setId("doi:doi id");
 		reference.setPmid("pmid number");
 		// note identifier is multi-valued
 		// <dcterms:identifier>pmid:pmid number</dcterms:identifier>
 
 		reference.setPublisher("a publisher as plain text");
 		reference.setVolume("6");
 		reference.setPageStart(8);
 		reference.setPageEnd(8);
 		reference.setPages("234 - 343");
 
 		return document;
 	}
 
 	Reference createSpringerDocument() {
 		ServiceFactory serviceFactory = ServiceFactory.getFactory();
 		DocService service = serviceFactory.createService(ServiceFactory.COLLECTION_SPRINGER);
 		ApiParams params = serviceFactory.createApiParams(ServiceFactory.COLLECTION_SPRINGER);
 		// params.setDoi("doi:10.1007/s11276-008-0131-4");
 		params.setId("doi:10.1007/s11276-008-0131-4");
 		Document document = service.getDocument(params);
 		System.out.println(document.getId());
 
 		MapSvcapiToCitagora documentMapper = new MapSvcapiToCitagora();
 		Reference reference = documentMapper.map(document);
 		return reference;
 
 	}
 
 }
