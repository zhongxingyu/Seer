 package com.spring.mti.service;
 
 import java.util.List;
 
 import com.spring.mti.model.Certification;
 import com.spring.mti.model.Employe;
 import com.spring.mti.model.Queshion;
import com.spring.mti.model.RelCertificationEmploye;
 import com.spring.mti.model.RelTestQueshion;
 import com.spring.mti.model.TestKnowledge;
 
 public interface CertificationService {
 	void createtTest(String name);
 	TestKnowledge getTestByName(String name);
 	void deleteTest(TestKnowledge t);
 	RelTestQueshion getQueshionFromTest(TestKnowledge t, Queshion q);
 	void pushQueshionToTest(TestKnowledge t, Queshion q);
 	void popQueshionFromTest(RelTestQueshion t);
 	List<TestKnowledge> getAllTests();
 	TestKnowledge getTestById(Long id);
 	List<Queshion> getListQueshionsFromTest(Long id);
 	Certification getCertificationByName(String name);
 	Certification getCertificationById(Long id);
 	void deleteCertification(Certification t);
 	void createCertification(String name);
 	void pushEmployeToCertification(Employe e, Certification c);
	RelCertificationEmploye getEmployeInCertification(Employe e, Certification c);
 }
