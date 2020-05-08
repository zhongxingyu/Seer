 /**
  * This document is a part of the source code and related artifacts for
  * SMSystem.
  * www.apeironsol.com
  * Copyright © 2012 apeironsol
  * 
  */
 package com.apeironsol.need.core.portal;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.faces.application.FacesMessage;
 import javax.inject.Named;
 import javax.validation.constraints.NotNull;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.primefaces.event.FlowEvent;
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.springframework.context.annotation.Scope;
 
 import com.apeironsol.framework.exception.ApplicationException;
 import com.apeironsol.framework.exception.BusinessException;
 import com.apeironsol.need.core.dao.AddressDao;
 import com.apeironsol.need.core.model.AcademicYear;
 import com.apeironsol.need.core.model.Address;
 import com.apeironsol.need.core.model.Admission;
 import com.apeironsol.need.core.model.AdmissionReservationFee;
 import com.apeironsol.need.core.model.Batch;
 import com.apeironsol.need.core.model.Branch;
 import com.apeironsol.need.core.model.BuildingBlock;
 import com.apeironsol.need.core.model.EducationHistory;
 import com.apeironsol.need.core.model.Klass;
 import com.apeironsol.need.core.model.MedicalHistory;
 import com.apeironsol.need.core.model.Relation;
 import com.apeironsol.need.core.model.Section;
 import com.apeironsol.need.core.model.Student;
 import com.apeironsol.need.core.model.StudentAcademicYear;
 import com.apeironsol.need.core.model.StudentStatusHistory;
 import com.apeironsol.need.core.service.AdmissionReservationFeeService;
 import com.apeironsol.need.core.service.BuildingBlockService;
 import com.apeironsol.need.core.service.RelationService;
 import com.apeironsol.need.core.service.StudentAcademicYearFeeSummaryService;
 import com.apeironsol.need.core.service.StudentService;
 import com.apeironsol.need.financial.model.BranchLevelFee;
 import com.apeironsol.need.financial.model.KlassLevelFee;
 import com.apeironsol.need.financial.service.BranchLevelFeeService;
 import com.apeironsol.need.financial.service.KlassLevelFeeService;
 import com.apeironsol.need.reporting.model.AdmissionReportParameterBean;
 import com.apeironsol.need.reporting.util.JReportGenerator;
 import com.apeironsol.need.reporting.util.JReportType;
 import com.apeironsol.need.util.constants.AdmissionStatusConstant;
 import com.apeironsol.need.util.constants.BuildingBlockConstant;
 import com.apeironsol.need.util.constants.FeeClassificationLevelConstant;
 import com.apeironsol.need.util.constants.FeeTypeConstant;
 import com.apeironsol.need.util.constants.GenderConstant;
 import com.apeironsol.need.util.constants.RelationTypeConstant;
 import com.apeironsol.need.util.constants.ResidenceConstant;
 import com.apeironsol.need.util.dataobject.AdmissionFeeDO;
 import com.apeironsol.need.util.portal.ViewExceptionHandler;
 import com.apeironsol.need.util.portal.ViewPathConstants;
 import com.apeironsol.need.util.portal.ViewUtil;
 import com.apeironsol.need.util.searchcriteria.AdmissionSearchCriteria;
 
 @Named
 @Scope(value = "session")
 public class AdmissionBean extends AbstractStudentBean {
 
 	/**
 	 * Unique serial version id for this class.
 	 */
 	private static final long				serialVersionUID							= 3120175111183256699L;
 
 	private static final Logger				log											= Logger.getLogger(AdmissionBean.class);
 
 	@Resource
 	private BuildingBlockService			buildingBlockService;
 
 	@Resource
 	private StudentService					studentService;
 
 	@Resource
 	private AdmissionReservationFeeService	admissionReservationFeeService;
 
 	@Resource
 	private KlassLevelFeeService			klassLevelFeeService;
 
 	@Resource
 	private BranchLevelFeeService			branchLevelFeeService;
 
 	@Resource
 	private RelationService					relationService;
 
 	@Resource
 	private AddressDao						addressDao;
 
 	private Admission						admission;
 
 	private Address							fatherAddress;
 
 	private Address							motherAddress;
 
 	private Address							guardianAddress;
 
 	private boolean							displayFatherInfo							= true;
 
 	private boolean							displayMotherInfo							= true;
 
 	private boolean							displayGuardianInfo							= false;
 
 	private boolean							fatherAddressSameAsPrimary					= false;
 
 	private boolean							motherAddressSameAsPrimary					= false;
 
 	private boolean							guardianAddressSameAsPrimary				= false;
 
 	private EducationHistory				educationHistory;
 
 	private Long							applyingForClassId;
 
 	private Collection<EducationHistory>	educationHistories;
 
 	private String							educationHistoryRowId;
 
 	private boolean							disableRecentSchoolInfo;
 
 	private String							activeStep									= GENERAL;
 
 	private static final String				GENERAL										= "general";
 
 	private static final String				PERSONAL									= "personal";
 
 	private static final String				CONTACT										= "contact";
 
 	private static final String				PARENTS										= "parents";
 
 	private static final String				EDUCATION									= "education";
 
 	private static final String				CONFIRM										= "confirm";
 
 	private static final String				PAYMNET										= "payment";
 
 	LinkedList<String>						processFlow									= new LinkedList<String>();
 
 	private AdmissionStatusConstant			admissionStatusAction;
 
 	private String							actionComment;
 
 	private boolean							admissionStateSetToRollback;
 
 	private boolean							viewNewAdmission;
 
 	private Collection<Student>				studentsBySearchCriteria					= new ArrayList<Student>();
 
 	private MedicalHistory					medicalHistory;
 
 	private Batch							appliedForBatch;
 
 	private AcademicYear					appliedForAcademicYear;
 
 	private Collection<AcademicYear>		academicYearsForBatch;
 
 	private AdmissionSearchCriteria			admissionSearchCriteria						= null;
 
 	private boolean							existingAdmissionNumber;
 
 	private Collection<BuildingBlock>		buildingBlockTypeAdmissionDocuments;
 
 	private Collection<BuildingBlock>		admissionSubmittedDocuments;
 
 	private Collection<BuildingBlock>		buildingBlockTypeStudentClassifications;
 
 	private boolean							loadStudentClassificationBuildingBlockTypes	= false;
 
 	private int								nrOfStudentsForSectionId;
 
 	private AdmissionReservationFee			admissionReservationFee;
 
 	private Collection<AdmissionFeeDO>		admissionFeeDOs;
 
 	private boolean							applicationFeeExternalTransaction;
 
 	private boolean							reservationFeeExternalTransaction;
 
 	private boolean							recentSchoolAddressAvailable;
 
 	private boolean							deductReservationFee;
 
 	private boolean							skipReservationFee;
 
 	private boolean							skipApplicationFee;
 
 	private boolean							reservationFeeDefinedInBranch;
 
 	private boolean							applicationFeeDefinedInBranch;
 
 	private Collection<AdmissionFeeDO>		admissionWavedFeeDOs;
 
 	@Resource
 	StudentAcademicYearFeeSummaryService	studentAcademicYearFeeSummaryService;
 
 	private double							applicationFormFee							= 0;
 
 	private boolean							previousAcademicYearAdmission				= false;
 
 	/**
 	 * @return the applicationFormFee
 	 */
 	public double getApplicationFormFee() {
 		return this.applicationFormFee;
 	}
 
 	/**
 	 * @param applicationFormFee
 	 *            the applicationFormFee to set
 	 */
 	public void setApplicationFormFee(final double applicationFormFee) {
 		this.applicationFormFee = applicationFormFee;
 	}
 
 	@PostConstruct
 	public void init() {
 		this.student = new Student();
 		this.admission = new Admission();
 		this.educationHistory = new EducationHistory();
 		this.primaryAddress = new Address();
 		this.initializeSearchCriteria();
 	}
 
 	public void newAdmission() {
 		this.setActiveStep(GENERAL);
 		this.createNewAdmissionObjects();
 	}
 
 	private void createNewAdmissionObjects() {
 		this.student = new Student();
 		this.appliedForBatch = null;
 		this.primaryAddress = new Address();
 		this.primaryAddress.setPrimary(true);
 		this.setStudentFather(new Relation());
 		this.setStudentMother(new Relation());
 		this.setStudentGuardian(new Relation());
 		this.fatherAddress = new Address();
 		this.motherAddress = new Address();
 		this.guardianAddress = new Address();
 		this.educationHistory = new EducationHistory();
 		this.educationHistories = new ArrayList<EducationHistory>();
 		this.setFatherAddressSameAsPrimary(false);
 		this.setMotherAddressSameAsPrimary(false);
 		this.setGuardianAddressSameAsPrimary(false);
 		this.processFlow.add(GENERAL);
 		this.processFlow.add(PERSONAL);
 		this.processFlow.add(CONTACT);
 		this.processFlow.add(PARENTS);
 		this.processFlow.add(EDUCATION);
 		this.processFlow.add(CONFIRM);
 		this.processFlow.add(PAYMNET);
 		this.displayMotherInfo = false;
 		this.displayFatherInfo = true;
 		this.displayGuardianInfo = false;
 		this.appliedForAcademicYear = null;
 		this.applyingForClassId = null;
 		this.setExistingAdmissionNumber(false);
 		this.academicYearsForBatch = null;
 		this.admissionReservationFee = new AdmissionReservationFee();
 		this.applicationFeeExternalTransaction = false;
 		this.disableRecentSchoolInfo = false;
 	}
 
 	public boolean isRecentSchoolAddressAvailable() {
 		return this.recentSchoolAddressAvailable;
 	}
 
 	public void setRecentSchoolAddressAvailable(final boolean recentSchoolAddressAvailable) {
 		this.recentSchoolAddressAvailable = recentSchoolAddressAvailable;
 	}
 
 	/**
 	 * @return the admissionReservationFee
 	 */
 	public AdmissionReservationFee getAdmissionReservationFee() {
 		return this.admissionReservationFee;
 	}
 
 	/**
 	 * @param admissionReservationFee
 	 *            the admissionReservationFee to set
 	 */
 	public void setAdmissionReservationFee(final AdmissionReservationFee admissionReservationFee) {
 		this.admissionReservationFee = admissionReservationFee;
 	}
 
 	/**
 	 * @return the loadStudentClassificationBuildingBlockTypes
 	 */
 	public boolean isLoadStudentClassificationBuildingBlockTypes() {
 		return this.loadStudentClassificationBuildingBlockTypes;
 	}
 
 	/**
 	 * @param loadStudentClassificationBuildingBlockTypes
 	 *            the loadStudentClassificationBuildingBlockTypes to set
 	 */
 	public void setLoadStudentClassificationBuildingBlockTypes(final boolean loadStudentClassificationBuildingBlockTypes) {
 		this.loadStudentClassificationBuildingBlockTypes = loadStudentClassificationBuildingBlockTypes;
 	}
 
 	public void performSearch() {
 		if (!this.admissionSearchCriteria.isSearchCriteriaIsEmpty()) {
 			this.searchAdmissionsBySearchCriteria();
 		}
 	}
 
 	public Collection<BuildingBlock> getAdmissionSubmittedDocuments() {
 		return this.admissionSubmittedDocuments;
 	}
 
 	public void setAdmissionSubmittedDocuments(final Collection<BuildingBlock> admissionSubmittedDocuments) {
 		this.admissionSubmittedDocuments = admissionSubmittedDocuments;
 	}
 
 	public Collection<BuildingBlock> getBuildingBlockTypeAdmissionDocuments() {
 		return this.buildingBlockTypeAdmissionDocuments;
 	}
 
 	public void setBuildingBlockTypeAdmissionDocuments(final Collection<BuildingBlock> buildingBlockTypeAdmissionDocuments) {
 		this.buildingBlockTypeAdmissionDocuments = buildingBlockTypeAdmissionDocuments;
 	}
 
 	/**
 	 * @return the existingAdmissionNumber
 	 */
 	public boolean isExistingAdmissionNumber() {
 		return this.existingAdmissionNumber;
 	}
 
 	/**
 	 * @param existingAdmissionNumber
 	 *            the existingAdmissionNumber to set
 	 */
 	public void setExistingAdmissionNumber(final boolean existingAdmissionNumber) {
 		this.existingAdmissionNumber = existingAdmissionNumber;
 	}
 
 	public Address getFatherAddress() {
 		return this.fatherAddress;
 	}
 
 	public void setFatherAddress(final Address fatherAddress) {
 		this.fatherAddress = fatherAddress;
 	}
 
 	public Address getMotherAddress() {
 		return this.motherAddress;
 	}
 
 	public void setMotherAddress(final Address motherAddress) {
 		this.motherAddress = motherAddress;
 	}
 
 	public Address getGuardianAddress() {
 		return this.guardianAddress;
 	}
 
 	public void setGuardianAddress(final Address guardianAddress) {
 		this.guardianAddress = guardianAddress;
 	}
 
 	public boolean isDisplayFatherInfo() {
 		return this.displayFatherInfo;
 	}
 
 	public void setDisplayFatherInfo(final boolean displayFatherInfo) {
 		this.displayFatherInfo = displayFatherInfo;
 	}
 
 	public boolean isDisplayMotherInfo() {
 		return this.displayMotherInfo;
 	}
 
 	public void setDisplayMotherInfo(final boolean displayMotherInfo) {
 		this.displayMotherInfo = displayMotherInfo;
 	}
 
 	public boolean isDisplayGuardianInfo() {
 		return this.displayGuardianInfo;
 	}
 
 	public void setDisplayGuardianInfo(final boolean displayGuardianInfo) {
 		this.displayGuardianInfo = displayGuardianInfo;
 	}
 
 	public boolean isFatherAddressSameAsPrimary() {
 		return this.fatherAddressSameAsPrimary;
 	}
 
 	public void setFatherAddressSameAsPrimary(final boolean fatherAddressSameAsPrimary) {
 		this.fatherAddressSameAsPrimary = fatherAddressSameAsPrimary;
 	}
 
 	public boolean isMotherAddressSameAsPrimary() {
 		return this.motherAddressSameAsPrimary;
 	}
 
 	public void setMotherAddressSameAsPrimary(final boolean motherAddressSameAsPrimary) {
 		this.motherAddressSameAsPrimary = motherAddressSameAsPrimary;
 	}
 
 	public boolean isGuardianAddressSameAsPrimary() {
 		return this.guardianAddressSameAsPrimary;
 	}
 
 	public void setGuardianAddressSameAsPrimary(final boolean guardianAddressSameAsPrimary) {
 		this.guardianAddressSameAsPrimary = guardianAddressSameAsPrimary;
 	}
 
 	public boolean isRenderFatherAddress() {
 		return this.displayFatherInfo && !this.fatherAddressSameAsPrimary;
 	}
 
 	public boolean isRenderMotherAddress() {
 		return this.displayMotherInfo && !this.motherAddressSameAsPrimary;
 	}
 
 	public boolean isRenderGuardianAddress() {
 		return this.displayGuardianInfo && !this.guardianAddressSameAsPrimary;
 	}
 
 	public EducationHistory getEducationHistory() {
 		return this.educationHistory;
 	}
 
 	public void setEducationHistory(final EducationHistory educationHistory) {
 		this.educationHistory = educationHistory;
 	}
 
 	@NotNull(message = "model.applying_for_class_mandatory")
 	public Long getApplyingForClassId() {
 		return this.applyingForClassId;
 	}
 
 	public void setApplyingForClassId(final Long applyingForClassId) {
 		this.applyingForClassId = applyingForClassId;
 	}
 
 	public Collection<EducationHistory> getEducationHistories() {
 		return this.educationHistories;
 	}
 
 	public void setEducationHistories(final List<EducationHistory> educationHistories) {
 		this.educationHistories = educationHistories;
 	}
 
 	public String getEducationHistoryRowId() {
 		return this.educationHistoryRowId;
 	}
 
 	public void setEducationHistoryRowId(final String educationHistoryRowId) {
 		this.educationHistoryRowId = educationHistoryRowId;
 	}
 
 	public boolean isDisableRecentSchoolInfo() {
 		return this.disableRecentSchoolInfo;
 	}
 
 	public void setDisableRecentSchoolInfo(final boolean disableRecentSchoolInfo) {
 		this.disableRecentSchoolInfo = disableRecentSchoolInfo;
 	}
 
 	public boolean isDisplayRecentSchoolInfo() {
 		return !this.disableRecentSchoolInfo;
 	}
 
 	public String getActiveStep() {
 		return this.activeStep;
 	}
 
 	public void setActiveStep(final String activeStep) {
 		this.activeStep = activeStep;
 	}
 
 	public Klass getApplyingForKlass() {
 		if (this.applyingForClassId != null) {
 			final Klass klass = this.klassService.findKlassById(this.applyingForClassId);
 			klass.setBranch(this.sessionBean.getCurrentBranch());
 			return klass;
 		} else {
 			return null;
 		}
 
 	}
 
 	public void fatherInfoCheck() {
 		if (!this.displayFatherInfo) {
 			this.setStudentFather(new Relation());
 			this.fatherAddress = new Address();
 		}
 	}
 
 	public void recentSchoolAddressCheck() {
 		if (this.recentSchoolAddressAvailable) {
 			final Address recentSchoolAddress = new Address();
 			this.educationHistory.setAddress(recentSchoolAddress);
 		} else {
 			this.educationHistory.setAddress(null);
 		}
 	}
 
 	public void fatherAddressInfoCheck() {
 
 		if (this.fatherAddressSameAsPrimary) {
 			this.fatherAddress = this.primaryAddress;
 		} else {
 			this.fatherAddress = new Address();
 		}
 
 	}
 
 	public void motherInfoCheck() {
 		if (!this.displayMotherInfo) {
 			this.setStudentMother(new Relation());
 			this.motherAddress = new Address();
 		}
 	}
 
 	public void motherAddressInfoCheck() {
 		if (this.motherAddressSameAsPrimary) {
 			this.motherAddress = this.primaryAddress;
 		} else {
 			this.motherAddress = new Address();
 		}
 
 	}
 
 	public void guardianInfoCheck() {
 		if (!this.displayGuardianInfo) {
 			this.setStudentGuardian(new Relation());
 			this.guardianAddress = new Address();
 		}
 	}
 
 	public void guardianAddressInfoCheck() {
 		if (this.guardianAddressSameAsPrimary) {
 			this.guardianAddress = this.primaryAddress;
 		} else {
 			this.guardianAddress = new Address();
 		}
 	}
 
 	public void saveAdmission() {
 		try {
 			final Klass appliedForKlass = this.klassService.findKlassById(this.applyingForClassId);
 			this.student.setApplyingForKlass(appliedForKlass);
 			this.student = this.studentService.saveStudent(this.student);
 			ViewUtil.addMessage("Admission details saved sucessfully.", FacesMessage.SEVERITY_INFO);
 		} catch (final ApplicationException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 	}
 
 	public void saveEducationHistory() {
 		this.educationHistory.setStudent(this.student);
 		this.educationHistory = this.studentService.saveEducationHistory(this.educationHistory);
 		ViewUtil.addMessage("Previous education details saved sucessfully", FacesMessage.SEVERITY_INFO);
 	}
 
 	public String viewAdmission() {
 		this.applyingForClassId = this.student.getApplyingForKlass().getId();
 		this.appliedForAcademicYear = this.student.getAppliedForAcademicYear();
 		this.appliedForBatch = this.student.getAppliedForBatch();
 		this.educationHistory = new EducationHistory();
 		return null;
 	}
 
 	public void submitAdmission() {
 		try {
 
 			this.validateApplicationFormFee();
 
 			// Admission details
 
 			final StudentStatusHistory history = new StudentStatusHistory();
 			history.setComments("Admission Submitted");
 			// Application For Batch
 			this.student.setAppliedForBatch(this.appliedForBatch);
 			// Application for academic year.
 			this.student.setAppliedForAcademicYear(this.appliedForAcademicYear);
 			// Application for class.
 			this.student.setApplyingForKlass(this.getApplyingForKlass());
 			// Student details
 			// Relations
 			final Collection<Relation> relations = new ArrayList<Relation>();
 			if (this.displayFatherInfo) {
 				this.getStudentFather().setGender(GenderConstant.MALE);
 				this.getStudentFather().setRelationType(RelationTypeConstant.FATHER);
 				relations.add(this.getStudentFather());
 				if (this.fatherAddressSameAsPrimary) {
 					this.getStudentFather().setAddress(this.primaryAddress);
 				} else {
 					this.getStudentFather().setAddress(this.fatherAddress);
 				}
 			}
 			if (this.displayMotherInfo) {
 				this.getStudentMother().setGender(GenderConstant.FEMALE);
 				this.getStudentMother().setRelationType(RelationTypeConstant.MOTHER);
 				relations.add(this.getStudentMother());
 				if (this.motherAddressSameAsPrimary) {
 					this.getStudentMother().setAddress(this.primaryAddress);
 				} else {
 					this.getStudentMother().setAddress(this.motherAddress);
 				}
 			}
 			if (this.displayGuardianInfo) {
 				this.getStudentGuardian().setRelationType(RelationTypeConstant.GUARDIAN);
 				relations.add(this.getStudentGuardian());
 				if (this.guardianAddressSameAsPrimary) {
 					this.getStudentGuardian().setAddress(this.primaryAddress);
 				} else {
 					this.getStudentGuardian().setAddress(this.guardianAddress);
 				}
 			}
 
 			// Previous education history
 			if (this.educationHistories == null) {
 				this.educationHistories = new ArrayList<EducationHistory>();
 			} else {
 				this.educationHistories.clear();
 			}
 
 			if (!this.disableRecentSchoolInfo) {
 				this.educationHistories.add(this.educationHistory);
 			}
 
 			final Branch currentBranch = this.sessionBean.getCurrentBranch();
 
 			this.student = this.admissionService.submitAdmission(this.student, relations, this.primaryAddress, this.educationHistories, currentBranch.getId(),
 					this.admissionReservationFee, this.applicationFeeExternalTransaction, history);
 
 			if (this.admissionSearchCriteria == null) {
 				this.admissionSearchCriteria = new AdmissionSearchCriteria(this.sessionBean.getCurrentBranch());
			} else {
				this.admissionSearchCriteria.resetSeacrhCriteria();
 			}
 
 			this.admissionSearchCriteria.setName(this.student.getFirstName());
 
 			this.admissionSearchCriteria.setAdmissionStatusConstant(AdmissionStatusConstant.SUBMITTED);
 
 			this.searchAdmissionsBySearchCriteria();
 
			ViewUtil.addMessage("Admission has submitted sucessfully with registration number " + this.student.getRegistrationNr(), FacesMessage.SEVERITY_INFO);
 
 			this.loadBranchStudentsFlag = true;
 
 			this.setNewAction(false);
 
 			this.setViewAction(false);
 
 		} catch (final ApplicationException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 	}
 
 	public void anotherReviewAdmission() {
 		this.student.setRelations(this.relationService.findRelationByStudentId(this.student.getId()));
 		try {
 
 			final StudentStatusHistory history = new StudentStatusHistory();
 			history.setComments(this.actionComment);
 			this.student = this.admissionService.anotherReviewAdmission(this.student, history);
			ViewUtil.addMessage("Admission state sucessfully changes as in-review.", FacesMessage.SEVERITY_INFO);
 			this.admissionStatusAction = null;
 		} catch (final BusinessException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 		this.student = this.studentService.findStudentById(this.student.getId());
 	}
 
 	public void reviewAdmission() {
 		this.student.setRelations(this.relationService.findRelationByStudentId(this.student.getId()));
 		try {
 			final StudentStatusHistory history = new StudentStatusHistory();
 			history.setComments(this.actionComment);
 			this.student = this.admissionService.reviewAdmission(this.student, history);
 			ViewUtil.addMessage("Admission state sucessfully changes as reviewed.", FacesMessage.SEVERITY_INFO);
 			this.admissionStatusAction = null;
 		} catch (final BusinessException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 		this.student = this.studentService.findStudentById(this.student.getId());
 	}
 
 	public void acceptAdmission() {
 		this.student.setRelations(this.relationService.findRelationByStudentId(this.student.getId()));
 		try {
 			final StudentStatusHistory history = new StudentStatusHistory();
 			history.setComments(this.actionComment);
 
 			this.student = this.admissionService.acceptAdmission(this.student, this.student.getAcceptedForKlass(), this.admissionReservationFee, history);
			/*
			 * if (!this.admissionSearchCriteria.isSearchCriteriaIsEmpty()) {
			 * this.searchAdmissionsBySearchCriteria();
			 * }
			 */
 			ViewUtil.addMessage("Admission state sucessfully changes as accepted.", FacesMessage.SEVERITY_INFO);
 			this.admissionStatusAction = null;
 		} catch (final BusinessException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 		this.student = this.studentService.findStudentById(this.student.getId());
 	}
 
 	public void admitStudent() {
 
 		if ((this.admissionReservationFee != null) && (this.admissionReservationFee.getApplicationFormFee() != null)
 				&& (this.admissionReservationFee.getApplicationFormFee() > 0) && this.skipApplicationFee) {
 
 			ViewUtil.addMessage("Application fee is paid so it cannot be skipped. ", FacesMessage.SEVERITY_ERROR);
 		}
 
 		if (this.isAdmissionReservationFeePaid() && (!this.reservationFeeDefinedInBranch || this.skipReservationFee)) {
 			this.deductReservationFee = true;
 		} else {
 			this.deductReservationFee = false;
 		}
 
 		try {
 			if (this.deductReservationFee) {
 
 				double totalReservationFeePaying = 0;
 
 				for (final AdmissionFeeDO admissionFeeDO : this.getAdmissionFeeDOs()) {
 
 					if (admissionFeeDO.getFeePaidDuringAdmission() != null) {
 
 						totalReservationFeePaying += admissionFeeDO.getFeePaidDuringAdmission().doubleValue();
 
 						if (admissionFeeDO.getAmount().doubleValue() < admissionFeeDO.getFeePaidDuringAdmission().doubleValue()) {
 
 							throw new BusinessException("Paying amount " + admissionFeeDO.getAmount().doubleValue() + " cannot be greater than actual fee "
 									+ admissionFeeDO.getFeePaidDuringAdmission().doubleValue() + " " + admissionFeeDO.getFeeName() + ".");
 
 						} else if (this.getAdmissionReservationFee().getReservationFee().doubleValue() < Double.valueOf(0).doubleValue()) {
 
 							throw new BusinessException("Paying amount for fee " + admissionFeeDO.getFeeName() + " cannot be less negative.");
 						}
 					}
 				}
 				if (!Double.valueOf(totalReservationFeePaying).equals(this.getAdmissionReservationFee().getReservationFee())) {
 					throw new BusinessException("Admission reservation amount do not match with paying amount.Please check.");
 				}
 			}
 
 			this.student.setRelations(this.relationService.findRelationByStudentId(this.student.getId()));
 			final Section admitForSection = this.sectionService.findSectionById(this.getAdmitForSectionId());
 			final StudentAcademicYear studentAcademicYear = this.admissionService.admitStudent(this.student, admitForSection, this.medicalHistory,
 					this.admissionSubmittedDocuments, this.getAdmissionFeeDOs(), this.deductReservationFee, this.skipApplicationFee, this.skipReservationFee);
 			this.studentAcademicYearFeeSummaryService.createStudentAcademicYearFeeSummaryForStudentAcademicYearId(studentAcademicYear.getId());
 			ViewUtil.addMessage("Admission state sucessfully changes as admitted.", FacesMessage.SEVERITY_INFO);
 			this.admissionStatusAction = null;
 		} catch (final BusinessException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 		this.student = this.studentService.findStudentById(this.student.getId());
 	}
 
 	public void removeAdmission() {
 		try {
 			this.admissionService.removeAdmission(this.student);
 			this.searchAdmissionsBySearchCriteria();
 		} catch (final BusinessException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 	}
 
 	public void rolebackAdmissonState() {
 		this.student.setRelations(this.relationService.findRelationByStudentId(this.student.getId()));
 		try {
 			final StudentStatusHistory history = new StudentStatusHistory();
 			history.setComments(this.actionComment);
 
 			this.student = this.admissionService.rollbackAdmissionCurrentState(this.student, history);
 			ViewUtil.addMessage("Admission state sucessfully rollbacked to " + this.student.getAdmissionStatus().getLabel() + ".", FacesMessage.SEVERITY_INFO);
 			this.admissionStateSetToRollback = false;
 		} catch (final ApplicationException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			ViewExceptionHandler.handle(e);
 		}
 		this.student = this.studentService.findStudentById(this.student.getId());
 	}
 
 	public void rejectAdmisson() {
 		this.student.setRelations(this.relationService.findRelationByStudentId(this.student.getId()));
 		try {
 
 			final StudentStatusHistory history = new StudentStatusHistory();
 			history.setComments(this.actionComment);
 
 			this.student = this.admissionService.rejectAdmission(this.student, history);
 			ViewUtil.addMessage("Admission is sucessfully rejected.", FacesMessage.SEVERITY_INFO);
 			this.admissionStatusAction = null;
 		} catch (final BusinessException e) {
 			this.student.setAdmissionStatus(AdmissionStatusConstant.SUBMITTED);
 			ViewExceptionHandler.handle(e);
 		} catch (final Throwable e) {
 			this.student.setAdmissionStatus(AdmissionStatusConstant.SUBMITTED);
 			ViewExceptionHandler.handle(e);
 		}
 	}
 
 	public String flowAction(final FlowEvent event) {
 		log.debug("Current wizard step:" + event.getOldStep());
 		log.debug("Next step:" + event.getNewStep());
 
 		// Check points on contacts
 		if (CONTACT.equals(event.getOldStep()) && PARENTS.equals(event.getNewStep()) && StringUtils.isEmpty(this.primaryAddress.getHomePhoneNr())
 				&& StringUtils.isEmpty(this.primaryAddress.getMobileNr())) {
 			ViewUtil.addMessage("Eaither home or mobile number is requried.", FacesMessage.SEVERITY_ERROR);
 			this.activeStep = CONTACT;
 			return CONTACT;
 
 		}
 
 		if (CONTACT.equals(event.getOldStep()) && PARENTS.equals(event.getNewStep()) && this.fatherAddressSameAsPrimary) {
 			this.fatherAddress = this.primaryAddress;
 		}
 
 		if (CONTACT.equals(event.getOldStep()) && PARENTS.equals(event.getNewStep()) && this.motherAddressSameAsPrimary) {
 			this.motherAddress = this.primaryAddress;
 		}
 
 		if (CONTACT.equals(event.getOldStep()) && PARENTS.equals(event.getNewStep()) && this.guardianAddressSameAsPrimary) {
 			this.guardianAddress = this.primaryAddress;
 		}
 
 		// Check points on patents
 		if (PARENTS.equals(event.getOldStep()) && EDUCATION.equals(event.getNewStep()) && !this.displayFatherInfo && !this.displayMotherInfo
 				&& !this.displayGuardianInfo) {
 
 			ViewUtil.addMessage("Eaither father , mother or guardian information should be entered.", FacesMessage.SEVERITY_ERROR);
 			this.activeStep = PARENTS;
 			return PARENTS;
 		}
 
 		// Check points on education
 		if (EDUCATION.equals(event.getOldStep()) && CONFIRM.equals(event.getNewStep())) {
 
 			ViewUtil.addMessage("Please verify the information carefully.", FacesMessage.SEVERITY_INFO);
 		}
 
 		// Check points on education
 		if (CONFIRM.equals(event.getOldStep()) && PAYMNET.equals(event.getNewStep())) {
 			this.checkForApplicationFee();
 		}
 
 		this.activeStep = event.getNewStep();
 		return event.getNewStep();
 	}
 
 	private void checkForApplicationFee() {
 		this.applicationFormFee = 0;
 		if (this.sessionBean.isBranchHavingApplicationFormFee()) {
 
 			final Collection<BuildingBlock> buildingBlocks = this.buildingBlockService.findFeeTypeBuildingBlocksbyBranchIdAndFeeType(this.sessionBean
 					.getCurrentBranch().getId(), FeeTypeConstant.APPLICATION_FEE);
 			final BuildingBlock applicationFeeBuildingBlock = buildingBlocks.iterator().next();
 
 			if (FeeClassificationLevelConstant.BRANCH_LEVEL.equals(applicationFeeBuildingBlock.getFeeClassificationLevel())) {
 
 				final Collection<BranchLevelFee> branchLevelFees = this.branchLevelFeeService.findBranchLevelFeeByBranchIdAndAcademicYearIdAndBuildingBlockId(
 						this.sessionBean.getCurrentBranch().getId(), this.appliedForAcademicYear.getId(), applicationFeeBuildingBlock.getId());
 
 				if ((branchLevelFees != null) && !branchLevelFees.isEmpty()) {
 					for (final BranchLevelFee branchLevelFee : branchLevelFees) {
 						this.applicationFormFee = this.applicationFormFee + branchLevelFee.getAmount();
 					}
 				}
 			} else {
 				ViewUtil.addMessage("Application fee is not defined for this branch.", FacesMessage.SEVERITY_INFO);
 			}
 		} else {
 			ViewUtil.addMessage("This branch does not requrie any applicatin fee for new applications", FacesMessage.SEVERITY_INFO);
 		}
 	}
 
 	public boolean isAdmissionDisabled() {
 		boolean result = false;
 		if ((this.student != null) && AdmissionStatusConstant.ADMITTED.equals(this.student.getAdmissionStatus())) {
 			result = true;
 		}
 		return result;
 	}
 
 	public String cancelAction() {
 		return ViewPathConstants.BRANCH_ADMISSIONS;
 	}
 
 	/**
 	 * @return the medicalHistory
 	 */
 	@Override
 	public MedicalHistory getMedicalHistory() {
 		return this.medicalHistory;
 	}
 
 	/**
 	 * @param medicalHistory
 	 *            the medicalHistory to set
 	 */
 	@Override
 	public void setMedicalHistory(final MedicalHistory medicalHistory) {
 		this.medicalHistory = medicalHistory;
 	}
 
 	@Override
 	public void onTabChange() {
 
 		if (2 == this.getActiveTabIndex()) {
 
 			this.loadRelationsFlag = true;
 			this.relation = new Relation();
 			this.relation.setAddress(new Address());
 			this.relationAddress = new Address();
 			this.loadRelations();
 
 		} else if (3 == this.getActiveTabIndex()) {
 			this.educationHistories = this.studentService.findPreviousEducationHistoryByStudentId(this.student.getId());
 			// Recent school addresses
 			if ((this.educationHistories == null) || this.educationHistories.isEmpty()) {
 				this.educationHistories = new ArrayList<EducationHistory>();
 				this.educationHistory = new EducationHistory();
 				this.educationHistory.setAddress(new Address());
 				this.disableRecentSchoolInfo = Boolean.FALSE;
 			} else if (this.educationHistories.size() > 0) {
 				this.educationHistory = this.educationHistories.iterator().next();
 				this.disableRecentSchoolInfo = Boolean.FALSE;
 			}
 		} else if (4 == this.getActiveTabIndex()) {
 			this.primaryAddress = this.studentService.findStudentAddressByStudentId(this.student.getId());
 		} else if (5 == this.getActiveTabIndex()) {
 
 			// Get Admission Reservation Fee
 			this.admissionReservationFee = this.admissionReservationFeeService.findAdmissionReservationFeeByStudentID(this.student.getId());
 
 			if (this.admissionReservationFee == null) {
 				this.admissionReservationFee = new AdmissionReservationFee();
 			}
 
 		}
 
 	}
 
 	public boolean isAdmissionBeReviewed() {
 		if (((this.student != null) && AdmissionStatusConstant.SUBMITTED.equals(this.student.getAdmissionStatus()))
 				|| AdmissionStatusConstant.INREVIEW.equals(this.student.getAdmissionStatus())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isAdmissionBeAccepted() {
 		if ((this.student != null) && AdmissionStatusConstant.REVIEWED.equals(this.student.getAdmissionStatus())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isAdmissionBeAdmitted() {
 		if ((this.student != null) && AdmissionStatusConstant.ACCEPTED.equals(this.student.getAdmissionStatus())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isAdmissionStateRollbacked() {
 		if ((this.student != null)
 				&& (AdmissionStatusConstant.REJECTED.equals(this.student.getAdmissionStatus())
 						|| AdmissionStatusConstant.INREVIEW.equals(this.student.getAdmissionStatus()) || AdmissionStatusConstant.REVIEWED.equals(this.student
 						.getAdmissionStatus()))) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isAdmissionBeRejected() {
 		if ((this.student != null)
 				&& (AdmissionStatusConstant.SUBMITTED.equals(this.student.getAdmissionStatus())
 						|| AdmissionStatusConstant.INREVIEW.equals(this.student.getAdmissionStatus())
 						|| AdmissionStatusConstant.REVIEWED.equals(this.student.getAdmissionStatus()) || AdmissionStatusConstant.ACCEPTED.equals(this.student
 						.getAdmissionStatus()))) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public AdmissionStatusConstant getAdmissionStatusAction() {
 		return this.admissionStatusAction;
 	}
 
 	public void setAdmissionStatusAction(final AdmissionStatusConstant admissionStatusAction) {
 		this.admissionStatusAction = admissionStatusAction;
 	}
 
 	public String getActionComment() {
 		return this.actionComment;
 	}
 
 	public void setActionComment(final String actionComment) {
 		this.actionComment = actionComment;
 	}
 
 	public void handleAssignedKlassChange() {
 		this.setLoadSectionForKlassFlag(true);
 		this.loadSectionsForClass();
 	}
 
 	public void resetBeforeReview() {
 		this.actionComment = null;
 		this.setAdmitForSectionId(null);
 	}
 
 	public void resetBeforeReject() {
 
 		this.actionComment = null;
 
 	}
 
 	public void resetBeforeAccept() {
 		this.actionComment = null;
 		this.admissionReservationFee = this.admissionReservationFeeService.findAdmissionReservationFeeByStudentID(this.student.getId());
 		if (this.admissionReservationFee == null) {
 			this.admissionReservationFee = new AdmissionReservationFee();
 		}
 	}
 
 	public void resetBeforeAdmit() {
 		this.loadStudentClassifications();
 		this.setLoadSectionForKlassFlag(true);
 		this.loadSectionsForClass();
 		this.loadAdmissionReservationFee();
 		this.medicalHistory = new MedicalHistory();
 		this.setBuildingBlockTypeAdmissionDocuments(this.buildingBlockService.findBuildingBlocksbyBranchIdAndBuildingBlockType(this.sessionBean
 				.getCurrentBranch().getId(), BuildingBlockConstant.DOCUMENTS_REQUIRD_FOR_ADMISSION));
 		this.admissionSubmittedDocuments = new ArrayList<BuildingBlock>();
 		this.actionComment = null;
 
 		this.determinAdmissionLevelFeeRules();
 	}
 
 	private void loadAdmissionReservationFee() {
 		this.admissionReservationFee = this.admissionReservationFeeService.findAdmissionReservationFeeByStudentID(this.student.getId());
 		if (this.isAdmissionReservationFeePaid()) {
 			this.admissionFeeDOs = new ArrayList<AdmissionFeeDO>();
 			AdmissionFeeDO admissionFeeDO = null;
 
 			final Collection<BranchLevelFee> branchLevelFees = this.branchLevelFeeService.findBranchLevelFeeByBranchIdAndAcademicYearId(this.sessionBean
 					.getCurrentBranch().getId(), this.student.getAppliedForAcademicYear().getId());
 			for (final BranchLevelFee branchLevelFee : branchLevelFees) {
 				if ((this.getStudent().getResidence().equals(ResidenceConstant.DAY_SCHOOLER) && FeeTypeConstant.HOSTEL_FEE.equals(branchLevelFee
 						.getBuildingBlock().getFeeType()))
 						|| FeeTypeConstant.APPLICATION_FEE.equals(branchLevelFee.getBuildingBlock().getFeeType())
 						|| FeeTypeConstant.RESERVATION_FEE.equals(branchLevelFee.getBuildingBlock().getFeeType())) {
 					// don't create hostel fee.
 					continue;
 				}
 
 				admissionFeeDO = new AdmissionFeeDO();
 				admissionFeeDO.setFeeClassificationLevelConstant(FeeClassificationLevelConstant.BRANCH_LEVEL);
 				admissionFeeDO.setBranchLevelFee(branchLevelFee);
 				this.admissionFeeDOs.add(admissionFeeDO);
 			}
 
 			final Collection<KlassLevelFee> klassLevelFees = this.klassLevelFeeService.findAllKlassFeesByKlassIdAndAcademicYearId(this.student
 					.getAcceptedForKlass().getId(), this.student.getAppliedForAcademicYear().getId());
 			for (final KlassLevelFee klassLevelFee : klassLevelFees) {
 				if (this.getStudent().getResidence().equals(ResidenceConstant.DAY_SCHOOLER)
 						&& FeeTypeConstant.HOSTEL_FEE.equals(klassLevelFee.getBuildingBlock().getFeeType())) {
 					// don't create hostel fee.
 					continue;
 				}
 				if (this.getStudent().getResidence().equals(ResidenceConstant.DAY_SCHOOLER)
 						&& FeeTypeConstant.APPLICATION_FEE.equals(klassLevelFee.getBuildingBlock().getFeeType())) {
 					// don't create hostel fee.
 					continue;
 				}
 				admissionFeeDO = new AdmissionFeeDO();
 				admissionFeeDO.setFeeClassificationLevelConstant(FeeClassificationLevelConstant.KLASS_LEVEL);
 				admissionFeeDO.setKlassLevelFee(klassLevelFee);
 				this.admissionFeeDOs.add(admissionFeeDO);
 			}
 		}
 	}
 
 	public Collection<AdmissionFeeDO> getAdmissionFeeDOs() {
 		return this.admissionFeeDOs;
 	}
 
 	public boolean isAdmissionReservationFeePaid() {
 		return (this.admissionReservationFee != null) && (this.admissionReservationFee.getReservationFee() != null)
 				&& (this.admissionReservationFee.getReservationFee() > 0);
 	}
 
 	public String admissionAdmitOnFlowProcess(final FlowEvent event) {
 		return event.getNewStep();
 	}
 
 	public boolean isAdmissionStateSetToRollback() {
 		return this.admissionStateSetToRollback;
 	}
 
 	public void setAdmissionStateSetToRollback(final boolean admissionStateSetToRollback) {
 		this.admissionStateSetToRollback = admissionStateSetToRollback;
 	}
 
 	public boolean isViewNewAdmission() {
 		return this.viewNewAdmission;
 	}
 
 	public void setViewNewAdmission(final boolean viewNewAdmission) {
 		this.viewNewAdmission = viewNewAdmission;
 	}
 
 	public StreamedContent getGenerateStudentAdmissionReport() {
 		StreamedContent file;
 		this.setLoadRelationsFlag(true);
 		this.loadRelations();
 		Relation parent = null;
 		if (this.getStudentFather() != null) {
 			parent = this.getStudentFather();
 		} else if (this.getStudentMother() != null) {
 			parent = this.getStudentMother();
 		} else {
 			parent = this.getStudentGuardian();
 		}
 		this.student.setBranch(this.sessionBean.getCurrentBranch());
 		final AdmissionReportParameterBean adRoPaBean = new AdmissionReportParameterBean(this.student, parent);
 		final byte[] bytes = JReportGenerator.generateReportUsingParameters(adRoPaBean, JReportType.PDF_REPORT);
 		final InputStream stream = new ByteArrayInputStream(bytes);
 		file = new DefaultStreamedContent(stream, "application/pdf", adRoPaBean.getReportOutPutFileName() + ".pdf");
 		return file;
 	}
 
 	/**
 	 * @return the students
 	 */
 	public Collection<Student> getStudentsBySearchCriteria() {
 		return this.studentsBySearchCriteria;
 	}
 
 	/**
 	 * @return the admissionSearchCriteria
 	 */
 	public AdmissionSearchCriteria getAdmissionSearchCriteria() {
 		return this.admissionSearchCriteria;
 	}
 
 	/**
 	 * @param admissionSearchCriteria
 	 *            the admissionSearchCriteria to set
 	 */
 	public void setAdmissionSearchCriteria(final AdmissionSearchCriteria admissionSearchCriteria) {
 		this.admissionSearchCriteria = admissionSearchCriteria;
 	}
 
 	public String searchAdmissionsBySearchCriteria() {
 		if (this.admissionSearchCriteria.isSearchCriteriaIsEmpty()) {
 			ViewUtil.addMessage("Please enter search criteria.", FacesMessage.SEVERITY_ERROR);
 		} else {
 			this.admissionSearchCriteria.setBranch(this.sessionBean.getCurrentBranch());
 			this.studentsBySearchCriteria = this.admissionService.findAdmissionsBySearchCriteria(this.admissionSearchCriteria);
 			if ((this.studentsBySearchCriteria == null) || this.studentsBySearchCriteria.isEmpty()) {
 				ViewUtil.addMessage("No admissions found for entered search criteria..", FacesMessage.SEVERITY_INFO);
 			}
 		}
 		return null;
 	}
 
 	public void initializeSearchCriteria() {
 		if (this.admissionSearchCriteria == null) {
 			this.admissionSearchCriteria = new AdmissionSearchCriteria(this.sessionBean.getCurrentBranch());
 		}
 	}
 
 	public String resetSearchCriteria() {
 		this.admissionSearchCriteria.resetSeacrhCriteria();
 		return null;
 	}
 
 	public Batch getAppliedForBatch() {
 		return this.appliedForBatch;
 	}
 
 	public void setAppliedForBatch(final Batch appliedForBatch) {
 		this.appliedForBatch = appliedForBatch;
 	}
 
 	public Collection<AcademicYear> getAcademicYearsForBatch() {
 		return this.academicYearsForBatch;
 	}
 
 	public void setAcademicYearsForBatch(final Collection<AcademicYear> academicYearsForBatch) {
 		this.academicYearsForBatch = academicYearsForBatch;
 	}
 
 	public void loadAcademicYearsForAdmission() {
 		try {
 			if (this.isPreviousAcademicYearAdmission()) {
 				this.academicYearsForBatch = this.getActiveAcademicYears();
 			} else if (!this.sessionBean.getCurrentBranchRule().isBatchRequiredIndicator()) {
 				this.academicYearsForBatch = this.getAcademicYearsWithAdmissionOpen();
 			}
 		} catch (final ApplicationException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Exception e) {
 			ViewExceptionHandler.handle(e);
 		}
 	}
 
 	public void loadAcademicYearsForBatch() {
 		try {
 			if (this.sessionBean.getCurrentBranchRule().isBatchRequiredIndicator() && (this.appliedForBatch != null)) {
 				this.academicYearsForBatch = this.academicYearService.findAcademicYearsForBatchId(this.appliedForBatch.getId(), this.sessionBean
 						.getCurrentBranch().getId());
 			}
 		} catch (final ApplicationException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Exception e) {
 			ViewExceptionHandler.handle(e);
 		}
 	}
 
 	public AcademicYear getAppliedForAcademicYear() {
 		return this.appliedForAcademicYear;
 	}
 
 	public void setAppliedForAcademicYear(final AcademicYear appliedForAcademicYear) {
 		this.appliedForAcademicYear = appliedForAcademicYear;
 	}
 
 	public void loadStudentClassifications() {
 		this.setBuildingBlockTypeStudentClassifications(this.buildingBlockService.findBuildingBlocksbyBranchIdAndBuildingBlockType(this.sessionBean
 				.getCurrentBranch().getId(), BuildingBlockConstant.STUDENT_CLASSIFICATION));
 	}
 
 	/**
 	 * @return the buildingBlockTypeDesignations
 	 */
 	public Collection<BuildingBlock> getBuildingBlockTypeStudentClassifications() {
 		return this.buildingBlockTypeStudentClassifications;
 	}
 
 	/**
 	 * @param buildingBlockTypeDesignations
 	 *            the buildingBlockTypeDesignations to set
 	 */
 	public void setBuildingBlockTypeStudentClassifications(final Collection<BuildingBlock> buildingBlockTypeStudentClassifications) {
 		this.buildingBlockTypeStudentClassifications = buildingBlockTypeStudentClassifications;
 	}
 
 	public void getNumberOfStudentsInSection() {
 		this.nrOfStudentsForSectionId = this.studentService.findNumberOfActiveStudentsBySectionId(this.getAdmitForSectionId());
 	}
 
 	/**
 	 * @return the nrOfStudentsForSectionId
 	 */
 	public int getNrOfStudentsForSectionId() {
 		return this.nrOfStudentsForSectionId;
 	}
 
 	/**
 	 * @param nrOfStudentsForSectionId
 	 *            the nrOfStudentsForSectionId to set
 	 */
 	public void setNrOfStudentsForSectionId(final int nrOfStudentsForSectionId) {
 		this.nrOfStudentsForSectionId = nrOfStudentsForSectionId;
 	}
 
 	public Admission getAdmission() {
 		return this.admission;
 	}
 
 	public void setAdmission(final Admission admission) {
 		this.admission = admission;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean showApplicationFormFee() {
 		return this.sessionBean.isBranchHavingApplicationFormFee();
 	}
 
 	public boolean isApplicationFeeExternalTransaction() {
 		return this.applicationFeeExternalTransaction;
 	}
 
 	public void setApplicationFeeExternalTransaction(final boolean applicationFeeExternalTransaction) {
 		this.applicationFeeExternalTransaction = applicationFeeExternalTransaction;
 	}
 
 	public boolean isReservationFeeExternalTransaction() {
 		return this.reservationFeeExternalTransaction;
 	}
 
 	public void setReservationFeeExternalTransaction(final boolean reservationFeeExternalTransaction) {
 		this.reservationFeeExternalTransaction = reservationFeeExternalTransaction;
 	}
 
 	public void resetExistingAdmissionNumber() {
 		this.student.setExternalAdmissionNr(null);
 	}
 
 	public void resetExistingApplicationFeeDetails() {
 		this.admissionReservationFee.setApplicationFeeExternalTransactionNr(null);
 		this.admissionReservationFee.setApplicationFeeExternalTransactionDate(null);
 	}
 
 	public void resetExistingAdmissionReservationFeeDetails() {
 		this.admissionReservationFee.setReservationFeeExternalTransactionDate(null);
 		this.admissionReservationFee.setReservationFeeExternalTransactionNr(null);
 	}
 
 	public Double getTotalAdmissionReservationFeeAdjusted() {
 		double totalAdjusted = 0.0;
 		for (final AdmissionFeeDO admissionFeeDO : this.admissionFeeDOs) {
 			if ((admissionFeeDO.getFeePaidDuringAdmission() != null) && (admissionFeeDO.getFeePaidDuringAdmission() > Double.valueOf(0))) {
 				totalAdjusted += admissionFeeDO.getFeePaidDuringAdmission().doubleValue();
 			}
 		}
 		return totalAdjusted;
 	}
 
 	public void findStudentRelations(final Student selectedStudent) {
 		final Collection<Relation> relations = this.relationService.findRelationByStudentId(selectedStudent.getId());
 		for (final Relation relation : relations) {
 			if (relation.getRelationType().equals(RelationTypeConstant.FATHER)) {
 				this.setDisplayFatherInfo(true);
 				this.setStudentFather(relation);
 				if (relation.getAddress().getId().equals(selectedStudent.getAddress().getId())) {
 					this.setFatherAddressSameAsPrimary(true);
 				} else {
 					this.setFatherAddress(this.addressDao.findById(relation.getAddress().getId()));
 				}
 			} else if (relation.getRelationType().equals(RelationTypeConstant.MOTHER)) {
 				this.setDisplayMotherInfo(true);
 				this.setStudentMother(relation);
 				if (relation.getAddress().getId().equals(selectedStudent.getAddress().getId())) {
 					this.setMotherAddressSameAsPrimary(true);
 				} else {
 					this.setMotherAddress(this.addressDao.findById(relation.getAddress().getId()));
 				}
 			} else if (relation.getRelationType().equals(RelationTypeConstant.GUARDIAN)) {
 				this.setDisplayGuardianInfo(true);
 				this.setStudentGuardian(relation);
 				if (relation.getAddress().getId().equals(selectedStudent.getAddress().getId())) {
 					this.setGuardianAddressSameAsPrimary(true);
 				} else {
 					this.setGuardianAddress(this.addressDao.findById(relation.getAddress().getId()));
 				}
 			}
 		}
 	}
 
 	public boolean isDeductReservationFee() {
 		return this.deductReservationFee;
 	}
 
 	public void setDeductReservationFee(final boolean deductReservationFee) {
 		this.deductReservationFee = deductReservationFee;
 	}
 
 	public void determinAdmissionLevelFeeRules() {
 
 		if ((this.admissionStatusAction != null) && this.admissionStatusAction.equals(AdmissionStatusConstant.ADMITTED)) {
 
 			final Collection<BranchLevelFee> reservationTypeFees = this.branchLevelFeeService.findBranchLevelFeeByBranchIdAndAcademicYearIdAndFeeType(
 					this.sessionBean.getCurrentBranch().getId(), this.student.getAppliedForAcademicYear().getId(), FeeTypeConstant.RESERVATION_FEE);
 
 			if ((reservationTypeFees != null) && !reservationTypeFees.isEmpty()) {
 
 				this.skipReservationFee = false;
 				this.reservationFeeDefinedInBranch = true;
 
 			} else {
 
 				this.skipReservationFee = true;
 				this.reservationFeeDefinedInBranch = false;
 
 			}
 
 			final Collection<BranchLevelFee> applicationFees = this.branchLevelFeeService.findBranchLevelFeeByBranchIdAndAcademicYearIdAndFeeType(
 					this.sessionBean.getCurrentBranch().getId(), this.student.getAppliedForAcademicYear().getId(), FeeTypeConstant.APPLICATION_FEE);
 
 			if ((applicationFees != null) && !applicationFees.isEmpty()) {
 
 				this.skipApplicationFee = false;
 				this.applicationFeeDefinedInBranch = true;
 
 			} else {
 
 				this.skipApplicationFee = true;
 				this.applicationFeeDefinedInBranch = false;
 
 			}
 
 		}
 
 	}
 
 	public void saveAdmissionReservationFee() {
 
 		if ((this.admissionReservationFee.getApplicationFormFee() == null) && (this.admissionReservationFee.getReservationFee() == null)) {
 			ViewUtil.addMessage("Unable to save, please specify application fee or reservation fee.", FacesMessage.SEVERITY_ERROR);
 			return;
 		}
 
 		try {
 			this.validateApplicationFormFee();
 			this.validateReservationFee();
 			if (this.admissionReservationFee.getStudent() == null) {
 				this.admissionReservationFee.setStudent(this.student);
 			}
 			this.admissionReservationFee = this.admissionReservationFeeService.saveAdmissionReservationFee(this.admissionReservationFee);
 			ViewUtil.addMessage("Fee saved successfully", FacesMessage.SEVERITY_INFO);
 		} catch (final ApplicationException e) {
 			ViewExceptionHandler.handle(e);
 		} catch (final Exception e) {
 			ViewExceptionHandler.handle(e);
 
 		}
 
 	}
 
 	public void resetAdmissionReservationFee() {
 		this.admissionReservationFee = this.admissionReservationFeeService.findAdmissionReservationFeeByStudentID(this.student.getId());
 
 		if (this.admissionReservationFee == null) {
 			this.admissionReservationFee = new AdmissionReservationFee();
 		}
 	}
 
 	public boolean isSkipReservationFee() {
 		return this.skipReservationFee;
 	}
 
 	public void setSkipReservationFee(final boolean skipReservationFee) {
 		this.skipReservationFee = skipReservationFee;
 	}
 
 	public boolean isSkipApplicationFee() {
 		return this.skipApplicationFee;
 	}
 
 	public void setSkipApplicationFee(final boolean skipApplicationFee) {
 		this.skipApplicationFee = skipApplicationFee;
 	}
 
 	public boolean isReservationFeeDefinedInBranch() {
 		return this.reservationFeeDefinedInBranch;
 	}
 
 	public void setReservationFeeDefinedInBranch(final boolean reservationFeeDefinedInBranch) {
 		this.reservationFeeDefinedInBranch = reservationFeeDefinedInBranch;
 	}
 
 	public boolean isApplicationFeeDefinedInBranch() {
 		return this.applicationFeeDefinedInBranch;
 	}
 
 	public void setApplicationFeeDefinedInBranch(final boolean applicationFeeDefinedInBranch) {
 		this.applicationFeeDefinedInBranch = applicationFeeDefinedInBranch;
 	}
 
 	/**
 	 * @return the admissionWavedFeeDOs
 	 */
 	public Collection<AdmissionFeeDO> getAdmissionWavedFeeDOs() {
 		return this.admissionWavedFeeDOs;
 	}
 
 	/**
 	 * @param admissionWavedFeeDOs
 	 *            the admissionWavedFeeDOs to set
 	 */
 	public void setAdmissionWavedFeeDOs(final Collection<AdmissionFeeDO> admissionWavedFeeDOs) {
 		this.admissionWavedFeeDOs = admissionWavedFeeDOs;
 	}
 
 	/**
 	 * Validates application form fee paid for the admissions with application
 	 * form fee applicable for the branch.
 	 */
 	private void validateApplicationFormFee() throws ApplicationException {
 		// Validate application form fee amount so that amount paid is not
 		// more than application fee applicable.
 		final Collection<BuildingBlock> buildingBlocks = this.buildingBlockService.findFeeTypeBuildingBlocksbyBranchIdAndFeeType(this.sessionBean
 				.getCurrentBranch().getId(), FeeTypeConstant.APPLICATION_FEE);
 		if ((this.admissionReservationFee.getApplicationFormFee() != null) && (buildingBlocks != null) && (buildingBlocks.size() > 0)) {
 
 			final BuildingBlock applicationFeeBuildingBlock = buildingBlocks.iterator().next();
 			double branchApplicationFormFee = 0;
 			if (FeeClassificationLevelConstant.BRANCH_LEVEL.equals(applicationFeeBuildingBlock.getFeeClassificationLevel())) {
 				final Collection<BranchLevelFee> branchLevelFees = this.branchLevelFeeService.findBranchLevelFeeByBranchIdAndAcademicYearIdAndBuildingBlockId(
 						this.sessionBean.getCurrentBranch().getId(), this.appliedForAcademicYear.getId(), applicationFeeBuildingBlock.getId());
 				if ((branchLevelFees != null) && !branchLevelFees.isEmpty()) {
 					for (final BranchLevelFee branchLevelFee : branchLevelFees) {
 						branchApplicationFormFee = branchApplicationFormFee + branchLevelFee.getAmount();
 					}
 				}
 			}
 			if (branchApplicationFormFee > 0) {
 				if (this.admissionReservationFee.getApplicationFormFee() > branchApplicationFormFee) {
 					throw new ApplicationException("Application fee cannot exceed " + branchApplicationFormFee);
 				} else if ((this.admissionReservationFee.getApplicationFormFee() != null) && (this.admissionReservationFee.getApplicationFormFee() <= 0d)) {
 
 					throw new ApplicationException("Application fee should be grater than 0.");
 				}
 			} else if ((branchApplicationFormFee == 0) && (this.admissionReservationFee.getApplicationFormFee() != 0.0)) {
 				throw new ApplicationException("Application fee is not defined for the academic year " + this.appliedForAcademicYear.getDisplayLabel());
 			}
 		}
 	}
 
 	private void validateReservationFee() {
 		double maxReservationFeeCanBePaid = 0;
 		if ((this.admissionReservationFee.getReservationFee() != null) && (this.admissionReservationFee.getReservationFee() <= 0d)) {
 			throw new ApplicationException("Reservation fee should be grater than 0.");
 		} else if ((this.admissionReservationFee.getComittedFee() != null) || (this.admissionReservationFee.getReservationFee() != null)) {
 			final Collection<BranchLevelFee> branchLevelFees = this.branchLevelFeeService.findBranchLevelFeeByBranchIdAndAcademicYearId(this.sessionBean
 					.getCurrentBranch().getId(), this.student.getAppliedForAcademicYear().getId());
 			for (final BranchLevelFee branchLevelFee : branchLevelFees) {
 				if ((this.getStudent().getResidence().equals(ResidenceConstant.DAY_SCHOOLER) && FeeTypeConstant.HOSTEL_FEE.equals(branchLevelFee
 						.getBuildingBlock().getFeeType()))
 						|| FeeTypeConstant.APPLICATION_FEE.equals(branchLevelFee.getBuildingBlock().getFeeType())
 						|| FeeTypeConstant.RESERVATION_FEE.equals(branchLevelFee.getBuildingBlock().getFeeType())) {
 					// don't create hostel fee.
 					continue;
 				}
 
 				maxReservationFeeCanBePaid = maxReservationFeeCanBePaid + branchLevelFee.getAmount();
 			}
 
 			final Collection<KlassLevelFee> klassLevelFees = this.klassLevelFeeService.findAllKlassFeesByKlassIdAndAcademicYearId(this.student
 					.getAcceptedForKlass().getId(), this.student.getAppliedForAcademicYear().getId());
 			for (final KlassLevelFee klassLevelFee : klassLevelFees) {
 				if (this.getStudent().getResidence().equals(ResidenceConstant.DAY_SCHOOLER)
 						&& FeeTypeConstant.HOSTEL_FEE.equals(klassLevelFee.getBuildingBlock().getFeeType())) {
 					// don't create hostel fee.
 					continue;
 				}
 				if (this.getStudent().getResidence().equals(ResidenceConstant.DAY_SCHOOLER)
 						&& FeeTypeConstant.APPLICATION_FEE.equals(klassLevelFee.getBuildingBlock().getFeeType())) {
 					// don't create hostel fee.
 					continue;
 				}
 				maxReservationFeeCanBePaid = maxReservationFeeCanBePaid + klassLevelFee.getAmount();
 			}
 			if (this.admissionReservationFee.getReservationFee() > maxReservationFeeCanBePaid) {
 				throw new ApplicationException("Reservation fee cannot exceed " + maxReservationFeeCanBePaid);
 			}
 			if ((this.admissionReservationFee.getComittedFee() != null) && (this.admissionReservationFee.getComittedFee() > maxReservationFeeCanBePaid)) {
 				throw new ApplicationException("Comitted fee cannot exceed " + maxReservationFeeCanBePaid);
 			}
 		}
 
 	}
 
 	/**
 	 * @return the previousAcademicYearAdmission
 	 */
 	public boolean isPreviousAcademicYearAdmission() {
 		return this.previousAcademicYearAdmission;
 	}
 
 	/**
 	 * @param previousAcademicYearAdmission
 	 *            the previousAcademicYearAdmission to set
 	 */
 	public void setPreviousAcademicYearAdmission(final boolean previousAcademicYearAdmission) {
 		this.previousAcademicYearAdmission = previousAcademicYearAdmission;
 	}
 
 }
