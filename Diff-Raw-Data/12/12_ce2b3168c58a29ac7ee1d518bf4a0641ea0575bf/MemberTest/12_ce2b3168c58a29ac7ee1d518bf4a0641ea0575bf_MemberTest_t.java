 package org.rimasu.cloister.common.model;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.assertion.DbUnitAssert;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.dataset.DataSetException;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.dataset.xml.XmlDataSet;
 import org.dbunit.operation.DatabaseOperation;
 import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Test unit for {@link Member}.
  * 
  * @author richards
  * 
  */
 public class MemberTest {
 
 	private static final String TEST_PERSISTENCE_UNIT = "TEST_UNIT";
 
 	// properties values used in getter/setter testing.
 	private static final String FIRST_NAME = "Al'fr-e.d";
 	private static final String UUID = "51d04e30-bdd3-11e0-962b-0800200c9a66";
	private static final String UUID2 = "51d04e30-bdd3-11e9-962b-0800200c9a66";
 
 	private DatabaseConnection connection;
 
 	private Validator validator;
 
 	@Before
 	public void beforeEachTest() {
 		ValidatorFactory factory = Validation.byDefaultProvider().configure()
 				.buildValidatorFactory();
 		validator = factory.getValidator();
 	}
 
 	@Test
 	public void canChangeMembersFirstName() {
 		Member member = new Member();
 		member.setFirstName(FIRST_NAME);
 		assertThat(member.getFirstName(), is(FIRST_NAME));
 	}
 
 	@Test
 	public void canChangeAMembersUuid() {
 		Member member = new Member();
 		member.setUuid(UUID);
 		assertThat(member.getUuid(), is(UUID));
 	}
 
 	@Test
 	public void canCreateAValidationReportFromAMember() {
 		checkNoReports(createPopulatedMember());
 	}
 
 	private void checkNoReports(Member member) {
 		Set<ConstraintViolation<Member>> reports = validator.validate(member);
 		assertTrue(reports.isEmpty());
 	}
 
 	@Test
 	public void nullUuidIsIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		member.setUuid(null);
 		checkReport(member, "uuid");
 	}
 
 	@Test
 	public void notValidUuidIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		String invalidUuid = "0237eee0-bNOTV-ALID-0800200c9a666";
 		member.setUuid(invalidUuid);
 		checkReport(member, "uuid");
 	}
 
 	@Test
 	public void nullFirstNameIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		member.setFirstName(null);
 		checkReport(member, "firstName");
 	}
 
 	@Test
 	public void firstNameWithSpaceIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		member.setFirstName("Alfr ed");
 		checkReport(member, "firstName");
 	}
 
 	@Test
 	public void singleCharFristNameIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		member.setFirstName("A");
 		checkReport(member, "firstName");
 	}
 
 	@Test
 	public void digitInFristNameIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		member.setFirstName("Alfr3d");
 		checkReport(member, "firstName");
 	}
 	
 	@Test
 	public void semicolonInFristNameIsReportedByValidationReport() {
 		Member member = createPopulatedMember();
 		member.setFirstName("Alfr;d");
 		checkReport(member, "firstName");
 	}
 
 	@Test
 	public void firstNameCanContainDotDashAndApostrophe() {
 		Member member = createPopulatedMember();
 		member.setFirstName("Alfr'ed");
 		checkNoReports(member);
 		member.setFirstName("Alfr-ed");
 		checkNoReports(member);
 		member.setFirstName("Alfr.ed");
 		checkNoReports(member);
 	}
 
 	private void checkReport(Member member, String expectedField) {
 		Set<ConstraintViolation<Member>> reports = validator.validate(member);
 		assertNotNull(reports);
 		assertThat(reports.size(), is(1));
 		ConstraintViolation<Member> report = reports.iterator().next();
 		assertThat(report.getPropertyPath().toString(), is(expectedField));
 		assertThat(report.getRootBean(), is(member));
 		System.out.println(report.getMessage());
 	}
 
 	@Test
 	public void canConvertXmlToMember() throws JAXBException {
 		JAXBContext context = JAXBContext.newInstance(getClass().getPackage()
 				.getName());
 		Unmarshaller unmarshaller = context.createUnmarshaller();
 		Object result = unmarshaller.unmarshal(getClass().getClassLoader()
 				.getResource("member.xml"));
 		assertTrue(result instanceof Member);
 		Member member = (Member) result;
 		assertThat(member.getFirstName(), is(FIRST_NAME));
 		assertThat(member.getUuid(), is(UUID));
 	}
 
 	@Test
 	public void canConvertMemberToXml() throws JAXBException, IOException {
 		JAXBContext context = JAXBContext.newInstance(getClass().getPackage()
 				.getName());
 		Marshaller marshaller = context.createMarshaller();
 		Member member = createPopulatedMember();
 		StringWriter dest = new StringWriter();
 		marshaller.marshal(member, dest);
 		dest.close();
 		String content = dest.toString();
 		assertTrue(content.contains(UUID));
 		assertTrue(content.contains(FIRST_NAME));
 		
 	}
 
 	@Test
 	public void canLoadMemberViaJpa() throws SQLException,
 			DatabaseUnitException, IOException {
 		// need to create the entity manager to ensure the schema
 		// is in place.
 		EntityManager em = createEntityManager();
 		// populate the schema.
 		prepareDatabase("member");
 
 		Member found = em.find(Member.class, UUID);
 
 		assertNotNull(found);
 		Member member = (Member) found;
 		assertThat(member.getFirstName(), is(FIRST_NAME));
 	}
	
 	@Test
 	public void canAccessesMemberViaJpa() throws DatabaseUnitException,
 			SQLException, IOException {
		
 		EntityManager em = createEntityManager();
 		em.getTransaction().begin();
 		Member member = createPopulatedMember();
		member.setUuid(UUID2);
 		em.persist(member);
 		em.flush();
 		em.getTransaction().commit();
 
 		ITable actual = getCurrentDataset().getTable("member");
		ITable expected = loadDataset("member-post-write").getTable("member");
 		new DbUnitAssert().assertEquals(expected, actual);
 	}
 
 	private Member createPopulatedMember() {
 		Member member = new Member();
 		member.setUuid(UUID);
 		member.setFirstName(FIRST_NAME);
 		return member;
 	}
 
 	@After
 	public void closeDatabase() throws SQLException {
 		if (connection != null) {
 			connection.close();
 		}
 	}
 
 	private EntityManager createEntityManager() {
 		EntityManagerFactory emf = Persistence
 				.createEntityManagerFactory(TEST_PERSISTENCE_UNIT);
 		EntityManager em = emf.createEntityManager();
 		return em;
 	}
 
 	/**
 	 * Use config file to per-configure database.
 	 * 
 	 * @param baseName
 	 *            name of xml file in classpath without .dbunit.xml suffix.
 	 * @return IDataSet configured from the XML file.
 	 * @throws IOException
 	 * @throws DataSetException
 	 *             if failed to load configuration file.
 	 */
 	protected void prepareDatabase(String config) throws SQLException,
 			DatabaseUnitException, IOException {
 		ensureConnected();
 		IDataSet dataset = loadDataset(config);
 		DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
 	}
 
 	/**
 	 * Read the current database configuration.
 	 * 
 	 * @return IDataSet representing current state of database.
 	 * @throws DatabaseUnitException
 	 *             if failed to read current state of database.
 	 * @throws SQLException
 	 *             if failed to read current state of database.
 	 */
 	private IDataSet getCurrentDataset() throws DatabaseUnitException,
 			SQLException {
 		ensureConnected();
 		return connection.createDataSet();
 	}
 
 	/**
 	 * Ensure that a connection to the database has been established.
 	 * 
 	 * @throws DatabaseUnitException
 	 *             if failed to create connection.
 	 * @throws SQLException
 	 *             if failed to create exception.
 	 */
 	private void ensureConnected() throws DatabaseUnitException, SQLException {
 		if (connection == null) {
 			connection = new DatabaseConnection(
 					DriverManager.getConnection("jdbc:hsqldb:mem:test"));
 		}
 	}
 
 	/**
 	 * Load a FlatXmlDataSet file from the root of the classpath, assuming
 	 * .dbunt.xml extension.
 	 * 
 	 * @param baseName
 	 *            name of xml file in classpath without .dbunit.xml suffix.
 	 * @return IDataSet configured from the XML file.
 	 * @throws DataSetException
 	 *             if failed to load configuration file.
 	 * @throws IOException
 	 */
 	private IDataSet loadDataset(String baseName) throws DataSetException,
 			IOException {
 		String path = baseName + ".dbunit.xml";
 		URL url = getClass().getClassLoader().getResource(path);
 		return new XmlDataSet(url.openStream());
 	}
 }
