 package org.sukrupa.student;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.classic.Session;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 import java.util.Set;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static java.lang.String.format;
 
 @Repository
 public class StudentRepository {
 
     private static final Logger LOG = Logger.getLogger(StudentService.class);
 
 
     private SessionFactory sessionFactory;
     private StudentsSearchCriteriaGenerator studentsSearchCriteriaGenerator;
 
     @Autowired
     public StudentRepository(SessionFactory sessionFactory, StudentsSearchCriteriaGenerator studentsSearchCriteriaGenerator) {
         this.sessionFactory = sessionFactory;
         this.studentsSearchCriteriaGenerator = studentsSearchCriteriaGenerator;
     }
 
     public void put(Student student) {
         session().saveOrUpdate(student);
     }
 
     public Student update(Student student) {
         session().save(student);
         session().flush();
         return student;
     }
 
     @SuppressWarnings("unchecked")
     public List<Student> findAll() {
         return query("from Student").list();
     }
 
     public Student findByStudentId(String studentId) {
         return (Student) query("from Student where studentId = ?").setParameter(0, StringUtils.upperCase(studentId)).uniqueResult();
     }
 
     @SuppressWarnings("unchecked")
     public Set<Student> findByStudentIds(String... studentIds) {
         return newHashSet(query("from Student where studentId in (:ids)").setParameterList("ids", studentIds).list());
     }
 
       private Query query(String hibernateQueryLanguage) {
           return session().createQuery(hibernateQueryLanguage);
       }
 
     private Session session() {
         return sessionFactory.getCurrentSession();
     }
 
     @SuppressWarnings("unchecked")
     public List<Student> findBySearchParameter(StudentSearchParameter searchParam, int firstIndex, int maxResults) {
         Criteria getPageCriteria = studentsSearchCriteriaGenerator.createOrderedCriteriaFrom(searchParam);
         return managePaging(getPageCriteria, firstIndex, maxResults);
     }
 
    @SuppressWarnings("unchecked")
     public List<Student> findSponsorsBySearchParameter(StudentSearchParameter searchParam, int firstIndex, int maxResults) {
         Criteria getPageCriteria = studentsSearchCriteriaGenerator.createSponsorSearchOrderedCriteriaFrom(searchParam);
         return managePaging(getPageCriteria, firstIndex, maxResults);
     }
 
    @SuppressWarnings("unchecked")
     private List<Student> managePaging(Criteria getPageCriteria, int firstIndex, int maxResults) {
         getPageCriteria.setFirstResult(firstIndex);
         getPageCriteria.setMaxResults(maxResults);
         return getPageCriteria.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<Student> getList() {
         return (List<Student>) query("from Student where Status = '0' order by name").list(); // 0 is exsiting student
     }
 
     int getCountBasedOn(StudentSearchParameter searchParam) {
         Criteria countCriteria = studentsSearchCriteriaGenerator.createCountCriteriaBasedOn(searchParam);
         return Integer.parseInt(countCriteria.uniqueResult().toString());
     }
 
     public int getTotalStudentsCount() {
         List<Student> studentList = findAll();
         return studentList.size();
     }
 
     public int getSponsoredStudentsCount() {
         List<Student> sponsoredStudentList = findSponsoredStudentsCount();
         return sponsoredStudentList.size();
     }
 
    @SuppressWarnings("unchecked")
     public List<Student> findSponsoredStudentsCount() {
         return query("from Student where SPONSOR != '' ").list();
     }
 
 
 }
