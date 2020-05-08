 /**
  * Test class of ServiceForWeb.
  */
 package com.boky.SubjectParserTest.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.anyListOf;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.springframework.context.ApplicationContext;
 
 import com.boky.SubjectParser.daolayer.entities.Dependency;
 import com.boky.SubjectParser.daolayer.entities.Specialization;
 import com.boky.SubjectParser.daolayer.entities.Subject;
 import com.boky.SubjectParser.daolayer.enums.SemesterClosing;
 import com.boky.SubjectParser.daolayer.interfaces.DependencyDao;
 import com.boky.SubjectParser.daolayer.interfaces.SpecializationDao;
 import com.boky.SubjectParser.daolayer.interfaces.SubjectDao;
 import com.boky.SubjectParser.services.dto.DTOTransformatorUtil;
 import com.boky.SubjectParser.services.dto.DependencyDepthAndDescriptionDTO;
 import com.boky.SubjectParser.services.dto.InitializationDataForWebDTO;
 import com.boky.SubjectParser.services.dto.SpecializationWithNameAndCodeDTO;
 import com.boky.SubjectParser.services.dto.SubjectWithCodeAndNameAndSemesterDTO;
 import com.boky.SubjectParser.services.services.ServiceForWeb;
 
 /**
  * @author Andras_Bokor
  * 
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(DTOTransformatorUtil.class)
 public class TestServiceForWeb {
 	private static List<Subject> kotelezoSubjects;
 	private static List<Subject> getotechnikaiSubjects;
 	private static List<Specialization> specializations;
 	private static List<SpecializationWithNameAndCodeDTO> specializationWithNameAndCodeDTOs;
 	private static List<SubjectWithCodeAndNameAndSemesterDTO> subjectWithCodeAndNameAndSemesterDTOsForGeotechnikai;
 	private static List<SubjectWithCodeAndNameAndSemesterDTO> subjectWithCodeAndNameAndSemesterDTOsForKotelezo;
 
 	@Mock
 	private SubjectDao subjectDao;
 
 	@Mock
 	private DependencyDao dependencyDao;
 
 	@Mock
 	private SpecializationDao specializationDao;
 
 	@Mock
 	private ApplicationContext applicationContext;
 
 	@Mock
 	private Random randomGenerator;
 
 	@Mock
 	private ServletContext servletContext;
 
 	@InjectMocks
 	private ServiceForWeb serviceForWeb;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		kotelezoSubjects = createListForKotelzoSpec();
 		getotechnikaiSubjects = createListForGeotechnikaiSpec();
 		specializations = createSpecializationList();
 		specializationWithNameAndCodeDTOs = createSpeciailzationWithNameAndCodeDTOs();
 		subjectWithCodeAndNameAndSemesterDTOsForGeotechnikai = createSubjectWithCodeAndNameAndSemesterDTOForGeotechnikai();
 		subjectWithCodeAndNameAndSemesterDTOsForKotelezo = createSubjectWithCodeAndNameAndSemesterDTOForKotelezo();
 
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		when(applicationContext.getBean("numberOfSemester", Integer.class)).thenReturn(Integer.valueOf(8));
 
 		when(subjectDao.getSubjectsBySpecializationId("KOTELEZO")).thenReturn(kotelezoSubjects);
 		when(subjectDao.getSubjectsBySpecializationId("GEOTECHNIKAI")).thenReturn(getotechnikaiSubjects);
 		when(specializationDao.getAllEntity()).thenReturn(specializations);
 
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link com.boky.SubjectParser.services.services.ServiceForWeb#getInitializedData(java.lang.String)} .
 	 */
 	@Test
 	public void testGetInitializedDataWhenSpecializationIsGeotehcnikai() {
 		PowerMockito.mockStatic(DTOTransformatorUtil.class);
 
 		Specialization geotechnikaiSpecialization = new Specialization("GEOTECHNIKAI", "Geotechnikai");
 
 		when(specializationDao.getById("GEOTECHNIKAI")).thenReturn(geotechnikaiSpecialization);
 
 		when(DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(kotelezoSubjects)).thenReturn(
 				createSubjectWithCodeAndNameAndSemesterDTOForKotelezo());
 		when(DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(getotechnikaiSubjects)).thenReturn(
 				subjectWithCodeAndNameAndSemesterDTOsForGeotechnikai);
 		when(DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializations)).thenReturn(specializationWithNameAndCodeDTOs);
 		when(DTOTransformatorUtil.convertSpecializatonToSpecializationWithNameAndCodeDTO(geotechnikaiSpecialization)).thenReturn(
 				new SpecializationWithNameAndCodeDTO("GEOTECHNIKAI", "Geotechnikai"));
 
 		InitializationDataForWebDTO expectedInitializationDataForWebDTO = new InitializationDataForWebDTO();
 		expectedInitializationDataForWebDTO.setSpecializations(specializationWithNameAndCodeDTOs);
 		expectedInitializationDataForWebDTO.getSubjects().addAll(subjectWithCodeAndNameAndSemesterDTOsForGeotechnikai);
 		expectedInitializationDataForWebDTO.getSubjects().addAll(subjectWithCodeAndNameAndSemesterDTOsForKotelezo);
 		expectedInitializationDataForWebDTO.setNumberOfSemesters(Integer.valueOf(8));
 		expectedInitializationDataForWebDTO.setActualSpecialization(new SpecializationWithNameAndCodeDTO("GEOTECHNIKAI", "Geotechnikai"));
 
 		// THEN
 
 		assertEquals(expectedInitializationDataForWebDTO, serviceForWeb.getInitializedData("GEOTECHNIKAI"));
 		verify(subjectDao).getSubjectsBySpecializationId("KOTELEZO");
 		verify(subjectDao).getSubjectsBySpecializationId("GEOTECHNIKAI");
 		PowerMockito.verifyStatic(times(2));
 		DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(anyListOf(Subject.class));
 		PowerMockito.verifyStatic();
 		DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializations);
 
 	}
 
 	@Test
 	public void testGetInitializedDataWhenSpecializationIsKotelezo() {
 		PowerMockito.mockStatic(DTOTransformatorUtil.class);
 
 		Specialization kotelezoSpecialization = new Specialization("KOTELEZO", "Kötelező");
 
 		when(specializationDao.getById("KOTELEZO")).thenReturn(kotelezoSpecialization);
 
 		when(DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(kotelezoSubjects)).thenReturn(
 				createSubjectWithCodeAndNameAndSemesterDTOForKotelezo());
 		when(DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(getotechnikaiSubjects)).thenReturn(
 				subjectWithCodeAndNameAndSemesterDTOsForGeotechnikai);
 		when(DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializations)).thenReturn(specializationWithNameAndCodeDTOs);
 		when(DTOTransformatorUtil.convertSpecializatonToSpecializationWithNameAndCodeDTO(kotelezoSpecialization)).thenReturn(
 				new SpecializationWithNameAndCodeDTO("KOTELEZO", "Kötelező"));
 
 		InitializationDataForWebDTO expectedInitializationDataForWebDTO = new InitializationDataForWebDTO();
 		expectedInitializationDataForWebDTO.setSpecializations(specializationWithNameAndCodeDTOs);
 		expectedInitializationDataForWebDTO.getSubjects().addAll(subjectWithCodeAndNameAndSemesterDTOsForKotelezo);
 		expectedInitializationDataForWebDTO.setNumberOfSemesters(Integer.valueOf(8));
 		expectedInitializationDataForWebDTO.setActualSpecialization(new SpecializationWithNameAndCodeDTO("KOTELEZO", "Kötelező"));
 
 		// THEN
 
 		assertEquals(expectedInitializationDataForWebDTO, serviceForWeb.getInitializedData("KOTELEZO"));
 		verify(subjectDao).getSubjectsBySpecializationId("KOTELEZO");
 		PowerMockito.verifyStatic(times(1));
 		DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(kotelezoSubjects);
 		PowerMockito.verifyStatic();
 		DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializations);
 
 	}
 
 	/**
 	 * Test method for {@link com.boky.SubjectParser.services.services.ServiceForWeb#getDependencyDepthAndDescription(java.lang.String, java.lang.String)} .
 	 */
 	@Test
 	public void testGetDependencyDepthAndDescription() {
 		Subject subject = createSubjectWithDependecies();
 		when(subjectDao.getById("SGYMMET203XXX")).thenReturn(subject);
 		Map<String, Integer> exceptedMap = createExpectedDependencyDepthMap();
 		DependencyDepthAndDescriptionDTO expectedDTO = new DependencyDepthAndDescriptionDTO();
 		expectedDTO.setDependencyDepths(exceptedMap);
 		expectedDTO.setDescription(createExceptedDescription());
 
 		DependencyDepthAndDescriptionDTO actualDTO = serviceForWeb.getDependencyDepthAndDescription(subject.getId(), "GEOTECHNIKAI");
 
 		assertEquals(expectedDTO, actualDTO);
 	}
 
 	/**
 	 * Test method for {@link com.boky.SubjectParser.services.services.ServiceForWeb#getAllSpecializationWithNameAndId()} .
 	 */
 	@Test
 	public void testGetAllSpecializationWithNameAndId() {
 		PowerMockito.mockStatic(DTOTransformatorUtil.class);
 		when(DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializations)).thenReturn(specializationWithNameAndCodeDTOs);
 		List<SpecializationWithNameAndCodeDTO> actualSpecializationWithNameAndCodeDTOs = serviceForWeb.getAllSpecializationWithNameAndId();
 
 		PowerMockito.verifyStatic(times(1));
 		DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializations);
 		Assert.assertEquals(specializationWithNameAndCodeDTOs, actualSpecializationWithNameAndCodeDTOs);
 	}
 
 	/**
 	 * Test method for {@link com.boky.SubjectParser.services.services.ServiceForWeb#getImageName()} .
 	 */
 	@Test
 	public void testGetImageNameWhenWeGotOne() {
 		when(randomGenerator.nextInt(3)).thenReturn(0);
 		when(servletContext.getRealPath(anyString())).thenReturn("src/test/resources/images");
 		String imageName = serviceForWeb.getImageName("src/test/resources/images");
 		Assert.assertEquals("src/test/resources/images/Third.jpg", imageName);
 	}
 
 	/**
 	 * Test method for {@link com.boky.SubjectParser.services.services.ServiceForWeb#getImageName()} .
 	 */
 	@Test
 	public void testGetImageNameWhenWeGotThree() {
 		when(randomGenerator.nextInt(3)).thenReturn(2);
 		when(servletContext.getRealPath(anyString())).thenReturn("src/test/resources/images");
 		String imageName = serviceForWeb.getImageName("src/test/resources/images");
 		Assert.assertEquals("src/test/resources/images/Second.jpg", imageName);
 	}
 
 	private String createExceptedDescription() {
 		return "Azonosító: SGYMMET203XXX\n" + "Név: Mechanika III. (Tartók statikája)\nE/GY/L/FZ: 3/4/5/Félévközi\nKredit: 5\nAjánlott félév: 5\nSzakirányok: [Geotechnikai]"
				+ "\nLeírás: It is a very long description\n\n";
 	}
 
 	private Map<String, Integer> createExpectedDependencyDepthMap() {
 		Map<String, Integer> result = new HashMap<>();
 		result.put("3", -1);
 		result.put("2", -1);
 		result.put("7", -3);
 		result.put("5", -2);
 		result.put("4", -2);
 		result.put("SGYMMET203XXX", 0);
 		result.put("9", -5);
 		result.put("8", -4);
 		return result;
 	}
 
 	private Subject createSubjectWithDependecies() {
 		Specialization kotelezo = new Specialization("KOTELEZO", "Kötelező");
 		Specialization magasepitesi = new Specialization("MAGASEPITESI", "Magasépítési");
 		Specialization geotechnikai = new Specialization("GEOTECHNIKAI", "Geotechnikai");
 
 		Set<Specialization> onlyKotelezo = new HashSet<>();
 		onlyKotelezo.add(kotelezo);
 		Set<Specialization> onlyGeotechnikai = new HashSet<>();
 		onlyGeotechnikai.add(geotechnikai);
 		Set<Specialization> geotechnikaiMagasepitesi = new HashSet<>();
 		geotechnikaiMagasepitesi.add(magasepitesi);
 		geotechnikaiMagasepitesi.add(geotechnikai);
 
 		Set<Specialization> onlyMagasepitesi = new HashSet<>();
 		onlyKotelezo.add(magasepitesi);
 
 		Subject subject = new Subject("SGYMMET203XXX", "Mechanika III. (Tartók statikája)", 3, 4, 5, SemesterClosing.FELEVKOZI, 5, 5, new HashSet<Dependency>(), onlyGeotechnikai,
 				"It is a very long description");
 		Subject subject2 = new Subject("2", "Two", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), geotechnikaiMagasepitesi, "Description");
 		Subject subject3 = new Subject("3", "Three", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyKotelezo, "Description");
 		Subject subject4 = new Subject("4", "Four", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyKotelezo, "Description");
 		Subject subject5 = new Subject("5", "Five", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject6 = new Subject("6", "Six", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject7 = new Subject("7", "Seven", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject8 = new Subject("8", "Eight", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject9 = new Subject("9", "Nine", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyKotelezo, "Description");
 		Subject subject10 = new Subject("10", "Ten", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject11 = new Subject("11", "Eleven", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), geotechnikaiMagasepitesi, "Description");
 		Subject subject12 = new Subject("12", "Twelve", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject13 = new Subject("13", "Thirteen", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject14 = new Subject("14", "Fourteen", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyMagasepitesi, "Description");
 		Subject subject15 = new Subject("15", "Fifteen", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject16 = new Subject("16", "Sixteen", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 		Subject subject17 = new Subject("17", "Seventeen", 2, 2, 2, SemesterClosing.FELEVKOZI, 2, 2, new HashSet<Dependency>(), onlyGeotechnikai, "Description");
 
 		Dependency dependency = new Dependency(subject8, geotechnikai, false, subject9);
 		subject8.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject4, kotelezo, false, subject9);
 		subject4.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject7, geotechnikai, false, subject8);
 		subject7.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject5, geotechnikai, true, subject7);
 		subject5.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject2, geotechnikai, false, subject5);
 		subject2.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject2, magasepitesi, false, subject6);
 		subject2.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject3, kotelezo, false, subject4);
 		subject3.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject, geotechnikai, false, subject3);
 		subject.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject, geotechnikai, false, subject2);
 		subject.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject10, geotechnikai, false, subject);
 		subject10.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject11, geotechnikai, false, subject);
 		subject11.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject12, geotechnikai, false, subject10);
 		subject12.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject17, geotechnikai, false, subject12);
 		subject17.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject14, magasepitesi, false, subject11);
 		subject14.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject13, geotechnikai, false, subject11);
 		subject13.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject15, geotechnikai, true, subject13);
 		subject15.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject16, geotechnikai, false, subject15);
 		subject16.getDependencies().add(dependency);
 
 		dependency = new Dependency(subject17, geotechnikai, false, subject16);
 		subject17.getDependencies().add(dependency);
 
 		return subject;
 	}
 
 	private static List<Specialization> createSpecializationList() {
 		List<Specialization> specializations = new ArrayList<>();
 		specializations.add(new Specialization("KOTELEZO", "Kötelező"));
 		specializations.add(new Specialization("GEOTECHNIKAI", "Geotechnikai"));
 		specializations.add(new Specialization("TELEPULESI", "Települési"));
 		specializations.add(new Specialization("MAGASEPITESI", "Magasépítési"));
 		specializations.add(new Specialization("TUZVEDELMI", "Tűz- és katasztrófavédelmi"));
 		specializations.add(new Specialization("VALASZTHATO", "Szabadon választható"));
 		return specializations;
 	}
 
 	private static List<SpecializationWithNameAndCodeDTO> createSpeciailzationWithNameAndCodeDTOs() {
 		List<SpecializationWithNameAndCodeDTO> specializationWithNameAndCodeDTOs = new ArrayList<>();
 		specializationWithNameAndCodeDTOs.add(new SpecializationWithNameAndCodeDTO("KOTELEZO", "Kötelező"));
 		specializationWithNameAndCodeDTOs.add(new SpecializationWithNameAndCodeDTO("GEOTECHNIKAI", "Geotechnikai"));
 		specializationWithNameAndCodeDTOs.add(new SpecializationWithNameAndCodeDTO("TELEPULESI", "Települési"));
 		specializationWithNameAndCodeDTOs.add(new SpecializationWithNameAndCodeDTO("MAGASEPITESI", "Magasépítési"));
 		specializationWithNameAndCodeDTOs.add(new SpecializationWithNameAndCodeDTO("TUZVEDELMI", "Tűz- és katasztrófavédelmi"));
 		specializationWithNameAndCodeDTOs.add(new SpecializationWithNameAndCodeDTO("VALASZTHATO", "Szabadon választható"));
 		return specializationWithNameAndCodeDTOs;
 	}
 
 	private static List<SubjectWithCodeAndNameAndSemesterDTO> createSubjectWithCodeAndNameAndSemesterDTOForKotelezo() {
 		List<SubjectWithCodeAndNameAndSemesterDTO> codeAndNameAndSemesterDTOs = new ArrayList<>();
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMASZ2021XA", "Ábrázoló geometria", 1));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMASZ206XXX", "Építészeti rajz", 2));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMMAT201XXX", "Matematika I.", 3));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMMAT202XXX", "Matematika II.", 4));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMMET201XXX", "Mechanika I. (Statika)", 3));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMMET202XXX", "Mechanika II. (Szilárdságtan)", 2));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMMET203XXX", "Mechanika III. (Tartók statikája)", 1));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMMET2011XA", "Mérnökfizika és a méretezés alapjai", 6));
 
 		return codeAndNameAndSemesterDTOs;
 	}
 
 	private static List<SubjectWithCodeAndNameAndSemesterDTO> createSubjectWithCodeAndNameAndSemesterDTOForGeotechnikai() {
 		List<SubjectWithCodeAndNameAndSemesterDTO> codeAndNameAndSemesterDTOs = new ArrayList<>();
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMTUB2317XA", "A katasztrófavédelem alapjai", 5));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMKOM241XXX", "Alapozás kedvezőtlen altalajon", 4));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMKOM2356XA", "Az épületgépészet alapjai", 3));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMKOM2346XA", "Geotechnika IV. (Alapozás II.)", 3));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMKOM2376XA", "Geotechnika mérőgyakorlat", 5));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMKOM235XXX", "Geotechnika V.", 1));
 		codeAndNameAndSemesterDTOs.add(new SubjectWithCodeAndNameAndSemesterDTO("SGYMKOM2408XA", "Hulladékgazdálkodás", 7));
 		return codeAndNameAndSemesterDTOs;
 	}
 
 	private static List<Subject> createListForKotelzoSpec() {
 		List<Subject> subjects = new ArrayList<>();
 		subjects.add(new Subject("SGYMASZ2021XA", "Ábrázoló geometria", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 1, new HashSet<Dependency>(), new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMASZ206XXX", "Építészeti rajz", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 2, new HashSet<Dependency>(), new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMMAT201XXX", "Matematika I.", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 3, new HashSet<Dependency>(), new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMMAT202XXX", "Matematika II.", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 4, new HashSet<Dependency>(), new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMMET201XXX", "Mechanika I. (Statika)", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 3, new HashSet<Dependency>(), new HashSet<Specialization>(),
 				"Desc"));
 		subjects.add(new Subject("SGYMMET202XXX", "Mechanika II. (Szilárdságtan)", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 2, new HashSet<Dependency>(),
 				new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMMET203XXX", "Mechanika III. (Tartók statikája)", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 1, new HashSet<Dependency>(),
 				new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMMET2011XA", "Mérnökfizika és a méretezés alapjai", 1, 2, 2, SemesterClosing.FELEVKOZI, 5, 6, new HashSet<Dependency>(),
 				new HashSet<Specialization>(), "Desc"));
 
 		return subjects;
 	}
 
 	private static List<Subject> createListForGeotechnikaiSpec() {
 		List<Subject> subjects = new ArrayList<>();
 		subjects.add(new Subject("SGYMTUB2317XA", "A katasztrófavédelem alapjai", 0, 1, 1, SemesterClosing.FELEVKOZI, 4, 5, new HashSet<Dependency>(),
 				new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMKOM241XXX", "Alapozás kedvezőtlen altalajon", 1, 1, 1, SemesterClosing.FELEVKOZI, 3, 4, new HashSet<Dependency>(),
 				new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMKOM2356XA", "Az épületgépészet alapjai", 2, 1, 1, SemesterClosing.FELEVKOZI, 6, 3, new HashSet<Dependency>(), new HashSet<Specialization>(),
 				"Desc"));
 		subjects.add(new Subject("SGYMKOM2346XA", "Geotechnika IV. (Alapozás II.)", 3, 1, 1, SemesterClosing.FELEVKOZI, 4, 3, new HashSet<Dependency>(),
 				new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMKOM2376XA", "Geotechnika mérőgyakorlat", 4, 1, 1, SemesterClosing.FELEVKOZI, 7, 5, new HashSet<Dependency>(), new HashSet<Specialization>(),
 				"Desc"));
 		subjects.add(new Subject("SGYMKOM235XXX", "Geotechnika V.", 5, 1, 1, SemesterClosing.FELEVKOZI, 3, 1, new HashSet<Dependency>(), new HashSet<Specialization>(), "Desc"));
 		subjects.add(new Subject("SGYMKOM2408XA", "Hulladékgazdálkodás", 6, 1, 1, SemesterClosing.FELEVKOZI, 3, 7, new HashSet<Dependency>(), new HashSet<Specialization>(), "Desc"));
 		return subjects;
 	}
 
 }
