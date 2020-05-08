 package com.spring.mti.service;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import com.spring.mti.dao.CertificationDao;
 import com.spring.mti.dao.CertificationStateDao;
 import com.spring.mti.dao.QueshionDao;
 import com.spring.mti.dao.RelCertificationEmployeDao;
 import com.spring.mti.dao.RelTestQueshionDao;
 import com.spring.mti.dao.TestKnowledgeDao;
 import com.spring.mti.model.Certification;
 import com.spring.mti.model.Employe;
 import com.spring.mti.model.Queshion;
 import com.spring.mti.model.RelCertificationEmploye;
 import com.spring.mti.model.RelTestQueshion;
 import com.spring.mti.model.TestKnowledge;
 
 @Repository
 public class CertificationServiceImpl implements CertificationService {
 	@Autowired private TestKnowledgeDao testDao;
 	@Autowired private RelTestQueshionDao test_queshionDao;
 	@Autowired private QueshionDao queshionDao;
 	@Autowired private CertificationDao certificationDao;
 	@Autowired private CertificationStateDao certificationStateDao;
 	@Autowired private RelCertificationEmployeDao certification_employe;
 	
 	@Override
 	public void createtTest(String name) {
 		List<TestKnowledge>res = testDao.findByNamedQuery("select s from TestKnowledge s where s.name=?1",Arrays.asList(name).toArray());
 		if ( res != null && res.size()<1) {
 			TestKnowledge t = new TestKnowledge();
 			t.setName(name);
 			testDao.create(t);
 		}
 	}
 	
 	@Override
 	public TestKnowledge getTestByName(String name) {
 		try{
 			return testDao.findByNamedQuery("select s from TestKnowledge s where s.name=?1",Arrays.asList(name).toArray()).get(0);
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	@Override
 	public TestKnowledge getTestById(Long id) {
 		return testDao.getByid(new TestKnowledge(), id);
 	}
 	
 	@Override
 	public void deleteTest(TestKnowledge t) {
 		testDao.delete(t);
 	}
 	
 	@Override
 	public void pushQueshionToTest(TestKnowledge t, Queshion q) {
 		if (null == this.getQueshionFromTest(t, q)){
 			RelTestQueshion r = new RelTestQueshion();
 			r.setFk_queshion(q);
 			r.setFk_test(t);
 			test_queshionDao.create(r);
 		}
 	}
 	
 	@Override
 	public RelTestQueshion getQueshionFromTest(TestKnowledge t, Queshion q) {
 		List<Long> param = Arrays.asList(t.getId(),q.getId());
 		try {
 			return test_queshionDao.findByNamedQuery("select s from RelTestQueshion s where s.fk_test.id=?1 and s.fk_queshion.id=?2",param.toArray()).get(0);
 		} catch(IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	@Override
 	public void popQueshionFromTest(RelTestQueshion t) {
 		test_queshionDao.delete(t);
 	}
 	
 	@Override
 	public List<TestKnowledge> getAllTests() {
 		try{
 			List<TestKnowledge> t =testDao.findAll(new TestKnowledge());
 			t.size();
 			return t;
 		} catch(IndexOutOfBoundsException e) {
 			System.out.println(e);
 			return null;
 		}
 	}
 	
 	@Override
 	public List<Queshion> getListQueshionsFromTest(Long id) {
 		try{
 			List<Queshion> q = queshionDao.findByNamedQuery("select q from RelTestQueshion s, Queshion q where s.fk_test.id=?1 and s.fk_queshion.id=q.id",Arrays.asList(id).toArray());
 			q.size();
 			return q;
 		} catch(IndexOutOfBoundsException e) {
 			System.out.println(e);
 			return null;
 		}
 	}
 	
 	@Override
 	public void createCertification(String name) {
 		List<Certification> res = certificationDao.findByNamedQuery("select s from Certification s where s.name=?1",Arrays.asList(name).toArray());
 		if ( res != null && res.size()<1) {
 			Certification t = new Certification();
 			t.setName(name);
 			certificationDao.create(t);
 		}
 	}
 	
 	@Override
 	public Certification getCertificationByName(String name) {
 		try{
 			return certificationDao.findByNamedQuery("select s from Certification s where s.name=?1",Arrays.asList(name).toArray()).get(0);
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	@Override
 	public Certification getCertificationById(Long id) {
 		return certificationDao.getByid(new Certification(), id);
 	}
 	
 	@Override
 	public void deleteCertification(Certification t) {
 		certificationDao.delete(t);
 	}
 	
 	@Override
 	public void pushEmployeToCertification(Employe e, Certification c) {
 		if (null == this.getEmployeInCertification(e, c)){
 			RelCertificationEmploye r = new RelCertificationEmploye();
 			r.setFk_certification(c);
 			r.setFk_employe(e);
 			certification_employe.create(r);
 		}
 	}
 }
