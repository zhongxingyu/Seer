 package org.sukrupa.student;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.*;
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @Repository
 public class StudentRepository {
 
     private static final String STUDENT_CLASS = "studentClass";
     private static final String GENDER = "gender";
     private static final String CASTE = "caste";
     private static final String COMMUNITY_LOCATION = "communityLocation";
     private static final String NAME = "name";
     private static final String DATE_OF_BIRTH = "dateOfBirth";
     private static final String STUDENT_ID = "studentId";
     private static final String TALENTS = "talents";
     private static final String DESCRIPTION = "description";
     private final SessionFactory sessionFactory;
     private static final String RELIGION = "religion";
 
     @Autowired
     public StudentRepository(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     @SuppressWarnings("unchecked")
     public List<Student> findAll() {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Student.class);
         return addOrderCriteria(criteria).list();
     }
 
     public Student find(String studentId) {
         Criteria criteria = sessionFactory.getCurrentSession()
                 .createCriteria(Student.class)
                 .add(Restrictions.eq(STUDENT_ID, studentId));
         return (Student) criteria.uniqueResult();
     }
 
     public List<Student> parametricSearch(StudentSearchParameter searchParam) {
 
         Conjunction conjunction = createConjunction(searchParam.getStudentClass(), searchParam.getGender(),
                 searchParam.getCaste(), searchParam.getCommunityLocation(), searchParam.getReligion());
         if (!searchParam.getAgeFrom().isEmpty()) {
             addAgeCriteria(Integer.parseInt(searchParam.getAgeFrom()), Integer.parseInt(searchParam.getAgeTo()), conjunction);
         }
 
         Criteria criteria = addOrderCriteria(sessionFactory.getCurrentSession().createCriteria(Student.class));
         criteria.add(conjunction);
         addTalentsSearchCriteria(criteria, searchParam.getTalent());
 
         return criteria.list();
     }
 
     private void addAgeCriteria(int ageFrom, int ageTo, Conjunction conjunction) {
         LocalDate birthDateFrom = computeBirthDateFromAge(ageFrom);
         LocalDate birthDateTo = computeBirthDateFromAge(getInclusiveUpperBoundAge(ageTo));
         conjunction.add(Restrictions.between(DATE_OF_BIRTH, birthDateTo, birthDateFrom));
     }
 
     private int getInclusiveUpperBoundAge(int ageTo) {
         return ageTo + 1;
     }
 
     private void addTalentsSearchCriteria(Criteria criteria, String talent) {
         if (!talent.isEmpty()) {
             criteria.createCriteria(TALENTS).add(Restrictions.eq(DESCRIPTION, talent));
         }
     }
 
     private LocalDate computeBirthDateFromAge(int age) {
         return new LocalDate().minusYears(age);
     }
 
     private Criteria addOrderCriteria(Criteria criteria) {
         return criteria.addOrder(Order.asc(GENDER).ignoreCase()).addOrder(Order.asc(NAME).ignoreCase());
     }
 
     private Conjunction createConjunction(String studentClass, String gender, String caste, String communityLocation, String religion) {
         Conjunction conjunction = Restrictions.conjunction();
         addRestrictionIfNotEmpty(STUDENT_CLASS, studentClass, conjunction);
         addRestrictionIfNotEmpty(GENDER, gender, conjunction);
         addRestrictionIfNotEmpty(CASTE, caste, conjunction);
         addRestrictionIfNotEmpty(COMMUNITY_LOCATION, communityLocation, conjunction);
         addRestrictionIfNotEmpty(RELIGION, religion, conjunction);
         return conjunction;
     }
 
     private void addRestrictionIfNotEmpty(String field, String parameter, Conjunction conjunction) {
         if (!parameter.isEmpty()) {
             conjunction.add(Restrictions.eq(field, parameter));
         }
     }
 
     public Student update(UpdateStudentParameter studentParam) {
         Student student = find(studentParam.getStudentId());
         if (student == null) {
             return null;
         }
 	    student.updateFrom(studentParam, findTalents(studentParam.getTalents()));
 
         sessionFactory.getCurrentSession().save(student);
         sessionFactory.getCurrentSession().flush();
         return student;
     }
 
     public Set<Talent> findTalents(Set<String> talentsDecriptions) {
         Disjunction disjunction = Restrictions.disjunction();
         for (String description: talentsDecriptions){
             disjunction.add(Restrictions.eq("description", description));
         }
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Talent.class).add(disjunction);
         return new HashSet<Talent>(criteria.list());
     }
 }
