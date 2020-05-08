 /**
  * 
  */
 package com.boky.SubjectParser.services.services;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.ServletContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.boky.SubjectParser.daolayer.entities.Dependency;
 import com.boky.SubjectParser.daolayer.entities.Subject;
 import com.boky.SubjectParser.daolayer.interfaces.DependencyDao;
 import com.boky.SubjectParser.daolayer.interfaces.SpecializationDao;
 import com.boky.SubjectParser.daolayer.interfaces.SubjectDao;
 import com.boky.SubjectParser.services.dto.DTOTransformatorUtil;
 import com.boky.SubjectParser.services.dto.DependencyDepthAndDescriptionDTO;
 import com.boky.SubjectParser.services.dto.InitializationDataForWebDTO;
 import com.boky.SubjectParser.services.dto.SpecializationWithNameAndCodeDTO;
 import com.boky.SubjectParser.services.dto.SubjectWithCodeAndNameAndSemesterDTO;
 import com.boky.SubjectParser.services.services.error.NoSuchSpecializationException;
 
 /**
  * @author Andras_Bokor
  * 
  */
 @Service
 @Transactional
 public class ServiceForWeb implements IServiceForWeb, ApplicationContextAware {
 	final Logger LOGGER = LoggerFactory.getLogger(ServiceForWeb.class);
 	private ApplicationContext applicationContext;
 
 	@Autowired
 	private SubjectDao subjectDao;
 
 	@Autowired
 	private DependencyDao dependencyDao;
 
 	@Autowired
 	private SpecializationDao specializationDao;
 
 	@Autowired
 	private Random randomGenerator;
 
 	@Autowired
 	private ServletContext servletContext;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.boky.SubjectParser.services.services.IServiceForWeb#getInitializedData (java.lang.String)
 	 */
 	@Override
 	public InitializationDataForWebDTO getInitializedData(String specializationId) {
 		if (specializationDao.getById(specializationId) == null) {
 			throw new NoSuchSpecializationException(specializationId);
 		}
 		List<SubjectWithCodeAndNameAndSemesterDTO> subjectWithCodeAndNameAndSemesterDTOs = DTOTransformatorUtil
 				.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(subjectDao.getSubjectsBySpecializationId(specializationId));
 		if (!specializationId.equals("KOTELEZO") && !specializationId.equals("VALASZTHATO")) {
 			subjectWithCodeAndNameAndSemesterDTOs.addAll(DTOTransformatorUtil.convertSubjectListToSubjectWithCodeAndNameAndSemesterDTOList(subjectDao
 					.getSubjectsBySpecializationId("KOTELEZO")));
 		}
 
 		List<SpecializationWithNameAndCodeDTO> specializationWithNameAndCodeDTOs = DTOTransformatorUtil
 				.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializationDao.getAllEntity());
 		Integer numberOfSemester = applicationContext.getBean("numberOfSemester", Integer.class);
 		SpecializationWithNameAndCodeDTO actualSpecialization = DTOTransformatorUtil.convertSpecializatonToSpecializationWithNameAndCodeDTO(specializationDao
 				.getById(specializationId));
 		InitializationDataForWebDTO initializationData = new InitializationDataForWebDTO(specializationWithNameAndCodeDTOs, subjectWithCodeAndNameAndSemesterDTOs,
 				actualSpecialization, numberOfSemester);
 
 		return initializationData;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.boky.SubjectParser.services.services.IServiceForWeb# getDependencyDepthAndDescription(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public DependencyDepthAndDescriptionDTO getDependencyDepthAndDescription(String subjectId, String specialization) {
 		Subject subject = subjectDao.getById(subjectId);
 		Map<String, Integer> subjectDepths = new HashMap<>();
 		subjectDepths.put(subjectId, 0);
 		buildDependencyBackwardDepthHashMap(subject, specialization, -1, subjectDepths); // backward
 		buildDependencyForwardDepthHashMap(subject, specialization, 1, subjectDepths);
 
 		DependencyDepthAndDescriptionDTO resultDTO = new DependencyDepthAndDescriptionDTO();
 		resultDTO.setDependencyDepths(subjectDepths);
 		resultDTO.setDescription(createDescriptionFromSubject(subject));
 		return resultDTO;
 	}
 
 	@Override
 	public List<SpecializationWithNameAndCodeDTO> getAllSpecializationWithNameAndId() {
 		List<SpecializationWithNameAndCodeDTO> specializationWithNameAndCodeDTOs = DTOTransformatorUtil
 				.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(specializationDao.getAllEntity());
 		return specializationWithNameAndCodeDTOs;
 	}
 
 	private String createDescriptionFromSubject(Subject subject) {
 		List<SpecializationWithNameAndCodeDTO> specializations = DTOTransformatorUtil.convertSpecializatonListToSpecializationWithNameAndCodeDTOList(subject.getSpecializations());
 		return "Azonosító: " + subject.getId() + "\nNév: " + subject.getName() + "\nE/GY/L/FZ: " + subject.getTheoretical() + "/" + subject.getPractical() + "/"
 				+ subject.getLabor() + "/" + subject.getSemesterClosing().getValue() + "\nKredit: " + subject.getCredit() + "\nAjánlott félév: " + subject.getOfferedSemester()
				+ "\nSzakirányok: " + specializations + "\nLeírás: " + subject.getDescription() + "\n";
 	}
 
 	private void buildDependencyBackwardDepthHashMap(Subject subject, String specialization, int level, Map<String, Integer> subjectDepths) {
 
 		for (Dependency dependency : subject.getDependencies()) {
 			if (dependency.getSpecialization().getId().equals(specialization) || dependency.getSpecialization().getId().equals("KOTELEZO")) {
 				buildDependencyBackwardDepthHashMap(dependency.getDependencySubject(), specialization, (level - 1), subjectDepths);
				if (!subjectDepthsContainsADeeperDependency(subjectDepths, dependency.getDependencySubject().getId(), level, false)) {
 					if (dependency.isOnlyRegistration()) {
 						if (level == -1) {
 							subjectDepths.put(dependency.getDependencySubject().getId(), -99); // 99 means it is an onlyRegistration dependency
 						} else {
 							subjectDepths.put(dependency.getDependencySubject().getId(), level);
 						}
 					} else {
 						subjectDepths.put(dependency.getDependencySubject().getId(), level);
 					}
 				}
 
 			}
 		}
 
 	}
 
 	private void buildDependencyForwardDepthHashMap(Subject subject, String specialization, int level, Map<String, Integer> subjectDepths) {
 
 		List<Dependency> dependencies = dependencyDao.getForwardDependenciesOfASubject(subject.getId());
 		for (Dependency dependency : dependencies) {
 			String specializationOfDependency = dependency.getSpecialization().getId();
 			if (specializationOfDependency.equals(specialization) || specializationOfDependency.equals("KOTELEZO")) {
 				buildDependencyForwardDepthHashMap(dependency.getSubject(), specialization, level + 1, subjectDepths);
				if (!subjectDepthsContainsADeeperDependency(subjectDepths, dependency.getSubject().getId(), level, true)) {
 					if (dependency.isOnlyRegistration()) {
 						if (level == 1) {
 							subjectDepths.put(dependency.getSubject().getId(), 99);
 						} else {
 							subjectDepths.put(dependency.getSubject().getId(), level);
 						}
 					} else {
 						subjectDepths.put(dependency.getSubject().getId(), level);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * It is needed because one subject is reachable in more path. It will be kept only the deepest level.
 	 * 
 	 * @param subjectDepths The subjectDepths map.
 	 * @param id Id of subject.
 	 * @param level
 	 * @param isForward It will decide if we are seeking the highes or the lowest level number.
 	 * @return true or false
 	 */
 	private boolean subjectDepthsContainsADeeperDependency(Map<String, Integer> subjectDepths, String id, int level, boolean isForward) {
 		if (subjectDepths.containsKey(id)) {
 			if (isForward) {
 				if (subjectDepths.get(id) >= level) {
 					return true;
 				}
 			} else {
 				if (subjectDepths.get(id) <= level) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String getImageName(String imageFolder) {
 		File imageDirectory = new File(servletContext.getRealPath(imageFolder));
 		File[] images = imageDirectory.listFiles();
 		String imageName = images[randomGenerator.nextInt(images.length)].getName();
 		return imageFolder + "/" + imageName;
 	}
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		this.applicationContext = applicationContext;
 
 	}
 
 	@Override
 	public String getDailyImageName(String imageFolder) {
 		int day = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
 		File imageDirectory = new File(servletContext.getRealPath(imageFolder));
 		File[] images = imageDirectory.listFiles();
 		String imageName = images[day - images.length * (day / images.length)].getName();
 		return imageFolder + "/" + imageName;
 	}
 
 }
