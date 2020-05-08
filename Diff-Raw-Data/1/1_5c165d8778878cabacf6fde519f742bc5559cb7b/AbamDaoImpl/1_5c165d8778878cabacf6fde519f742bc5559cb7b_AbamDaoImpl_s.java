 package no.uis.abam.ws_abam;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.sojo.core.Converter;
 import net.sf.sojo.core.conversion.Iterateable2IterateableConversion;
 import no.uis.abam.dom.AbamGroup;
 import no.uis.abam.dom.AbamPerson;
 import no.uis.abam.dom.AbamType;
 import no.uis.abam.dom.Application;
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.AssignmentType;
 import no.uis.abam.dom.Employee;
 import no.uis.abam.dom.Student;
 import no.uis.abam.dom.Supervisor;
 import no.uis.abam.dom.Thesis;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.log4j.Logger;
 import org.springframework.orm.jpa.JpaTemplate;
 import org.springframework.orm.jpa.support.JpaDaoSupport;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
 @Repository
 public class AbamDaoImpl extends JpaDaoSupport implements AbamDao {
 
   private static Logger log = Logger.getLogger(AbamDaoImpl.class);
   
   private ObjectFinder<AbamPerson> personFinder;
   private ObjectFinder<Supervisor> supervisorFinder;
   
   public AbamDaoImpl() {
   }
  
   @Override
   protected void initDao() throws Exception {
     super.initDao();
     createObjectFinders();
   }
 
 
   private void createObjectFinders() {
     personFinder = new ObjectFinder<AbamPerson>(getJpaTemplate()) {
       
       @Override
       protected AbamPerson findObject(AbamPerson source) {
         if (source instanceof Employee) {
           Employee emp = (Employee)source;
           return findEmployeeByEmployeeNumber(emp.getEmployeeId());
         } else {
           throw new NotImplementedException(getClass());
         }
       }
     };
 
     supervisorFinder = new ObjectFinder<Supervisor>(getJpaTemplate()) {
       @Override
       protected Supervisor findObject(Supervisor source) {
         JpaTemplate jpa = getJpaTemplate();
         
         @SuppressWarnings("unchecked")
         List<Supervisor> svfind = jpa.find("select s from Supervisor s where s.name=?", source.getName());
         
         if (svfind.isEmpty()) {
           return null;
         } else if (svfind.size() == 1) {
           return svfind.get(0);
         } else {
           throw new NotImplementedException(getClass());
         }
       }
     };
   }
   
   @Override
   public void saveAssignment(Assignment assignment) {
     JpaTemplate jpa = getJpaTemplate();
     
     // author
     AbamPerson author = assignment.getAuthor();
 
     assignment.setAuthor(personFinder.findOrCreate(author));
     
     // faculty supervisor
     Employee supervisor = assignment.getFacultySupervisor();
     assignment.setFacultySupervisor((Employee)personFinder.findOrCreate(supervisor));
     
     // supervisor list
     Iterator<Supervisor> svIter = assignment.getSupervisorList().iterator();
     List<Supervisor> svAdded = new LinkedList<Supervisor>();
     while (svIter.hasNext()) {
       Supervisor sv = svIter.next();
       svAdded.add(supervisorFinder.findOrCreate(sv));
       svIter.remove();
     }
     assignment.getSupervisorList().addAll(svAdded);
     
     assignment = jpa.merge(assignment);
   }
 
   @SuppressWarnings("unchecked")
   private Employee findEmployeeByEmployeeNumber(String employeeId) {
     List<Employee> employees = getJpaTemplate().find("select e from Employee e where e.employeeId = ?", employeeId);
     if (employees == null || employees.isEmpty()) {
       return null;
     }
     if (employees.size() != 1) {
       throw new NotImplementedException(getClass());
     }
     return employees.get(0);
   }
 
   @Override
   public void removeAssignment(Assignment assignment) {
     JpaTemplate jpa = getJpaTemplate();
     assignment = jpa.merge(assignment);
     jpa.remove(assignment);
     jpa.flush();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Assignment> getAssignments() {
     
     List<Assignment> assignments = getJpaTemplate().find("FROM Assignment");
     return assignments;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Assignment> getAssignmentsFromDepartmentCode(String departmentCode) {
     List<Assignment> assignments = getJpaTemplate().find("FROM Assignment c fetch all properties WHERE c.departmentCode = ?", departmentCode);
     loadEntity(assignments);
     return assignments;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Assignment> getActiveAssignments() {
     List<Assignment> resultList = getJpaTemplate().find("select a FROM Assignment a fetch all properties WHERE a.expireDate > current_date()");
     loadEntity(resultList);
     return resultList;
   }
 
   @Override
   public Assignment getAssignment(long id) {
     Assignment assignment = getJpaTemplate().find(Assignment.class, Long.valueOf(id));
     return assignment;
   }
 
   
   @SuppressWarnings("unchecked")
   @Override
   public List<Application> getApplications() {
     List<Application> applications = getJpaTemplate().find("SELECT a FROM Application a fetch all properties");
     loadEntity(applications);
     return applications;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Application> getApplicationsByDepartmentCode(String departmentCode, AssignmentType assignmentType) {
     List<Application> applications = getJpaTemplate().find("SELECT a from Application a fetch all properties WHERE a.assignment.departmentCode=? AND a.assignment.type=?", departmentCode, assignmentType);
     loadEntity(applications);
     return applications;
   }
 
   @Override
   public Application saveApplication(Application application) {
     AbamPerson author = application.getAssignment().getAuthor();
     JpaTemplate jpa = getJpaTemplate();
     if (author != null && author.getOid()!= null) {
       // there is an error when fetching abampersons that are actually Employees
       try {
         AbamPerson authorDb = jpa.find(AbamPerson.class, author.getOid());
         if (!authorDb.getClass().isAssignableFrom(AbamPerson.class)) {
           application.getAssignment().setAuthor(authorDb);
         }
       } catch (Exception e) {
         log.error("", e);
       }
     }
     if (application.getApplicationDate() == null) {
       application.setApplicationDate(Calendar.getInstance());
     }
     application = jpa.merge(application);
     return application;
   }
 
   @Override
   public void removeApplication(Application application) {
     JpaTemplate jpa = getJpaTemplate();
     application = jpa.merge(application);
     jpa.remove(application);
   }
 
   @Override
   public Thesis saveThesis(Thesis thesis) {
     JpaTemplate jpa = getJpaTemplate();
     
     //Thesis pThesis = null;
     
     // refetch assignment to prevent exception
     Assignment assignment = jpa.find(Assignment.class, thesis.getAssignment().getOid());
     thesis.setAssignment(assignment);
     
     // refetch the superuser
     Employee supervisor = jpa.find(Employee.class, thesis.getFacultySupervisor().getOid());
     thesis.setFacultySupervisor(supervisor);
 
     @SuppressWarnings("unchecked")
     List<Thesis> pThesises = jpa.find("select t from Thesis t, Assignment a where t.assignment.oid = ?", thesis.getAssignment().getOid());
     
     if (pThesises.size() == 1) {
       Thesis pThesis = pThesises.get(0);
       thesis.setOid(pThesis.getOid());
       //copyThesis(pThesis, thesis);
     } else if (!pThesises.isEmpty()){
       throw new NotImplementedException("In class " + getClass().getName() + ": there should only be one thesis per Assignment");
     }
     thesis = jpa.merge(thesis);
     return loadEntity(thesis);
   }
 
   private void copyThesis(Thesis pThesis, Thesis thesis) {
     
     try {
       thesis.setOid(pThesis.getOid());
       
       BeanUtils.copyProperties(pThesis, thesis);
     } catch(Exception e) {
       log.error("copying Thesis", e);
     }
 //    pThesis.setActualSubmissionForEvalutation(thesis.getActualSubmissionForEvalutation());
 //    pThesis.setDeadlineForSubmissionOfTopic(thesis.getDeadlineForSubmissionOfTopic());
 //    pThesis.setEditExternalExaminer(thesis.isEditExternalExaminer());
 //    pThesis.setEvaluationDeadline(thesis.getEvaluationDeadline());
 //    pThesis.setStudentNumber1(thesis.getStudentNumber1());
 //    pThesis.setStudentNumber2(thesis.getStudentNumber2());
 //    pThesis.setStudentNumber3(thesis.getStudentNumber3());
   }
 
   @Override
   public Student saveStudent(Student student) {
     JpaTemplate jpa = getJpaTemplate();
     List<Application> applications = student.getApplications();
     applications = mergeApplications(applications);
     student.setApplications(applications);
     student = jpa.merge(student);
     loadEntity(student);
     return student;
   }
 
   private List<Application> mergeApplications(List<Application> applications) {
     JpaTemplate jpa = getJpaTemplate();
     List<Application> newApps = new ArrayList<Application>(applications.size());
     for (Application application : applications) {
       Assignment assignment = this.getAssignment(application.getAssignment().getOid());
       application.setAssignment(assignment);
       newApps.add(jpa.merge(application));
     }
     return newApps;
   }
 
   @SuppressWarnings("unchecked")
   private List<Thesis> getThesisesWithEvaluationDeadline(String operator, Calendar cal, String departmentCode, String employeeId) {
     StringBuilder sbFrom = new StringBuilder("SELECT t FROM Thesis t ");
     StringBuilder sbWhere = new StringBuilder("WHERE t.submissionDeadline ");
     
     sbWhere.append(operator);
     sbWhere.append(" :deadline");
     Map<String, Object> params = new HashMap<String, Object>();
     params.put("deadline", cal);
     
     if (departmentCode != null) {
       sbWhere.append(" AND g.name = :departmentCode and g member of t.facultySupervisor.groups");
       sbFrom.append(", AbamGroup g ");
       params.put("departmentCode", departmentCode);
     }
     
     if (employeeId != null) {
       sbWhere.append(" AND t.facultySupervisor.employeeId = :employeeId");
       params.put("employeeId", employeeId);
     }
     
     sbFrom.append(sbWhere.toString());
     List<Thesis> list = getJpaTemplate().findByNamedParams(sbFrom.toString(), params);
     loadEntity(list);
     return list;
   }
 
   @Override
   public List<Thesis> getThesisesAfterEvaluationDeadline(Calendar cal, String departmentCode, String employeeId) {
     return getThesisesWithEvaluationDeadline("<", cal, departmentCode, employeeId);
   }
 
   @Override
   public List<Thesis> getThesisesBeforeEvaluationDeadline(Calendar cal, String departmentCode) {
     return getThesisesWithEvaluationDeadline(">", cal, departmentCode, null);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public AbamGroup findOrCreateGroup(String placeRef) {
     JpaTemplate jpa = getJpaTemplate();
     List<AbamGroup> gl = jpa.find("select g from AbamGroup g where g.name = ?", placeRef);
     AbamGroup g = null;
     if (gl.isEmpty()) {
       AbamGroup g1 = new AbamGroup(placeRef);
       g = jpa.merge(g1);
     } else {
       g = gl.get(0);
     }
     return g;
   }
 
   
   @Override
   public Employee findOrCreateEmployee(String userId, String fullName, String email, String phone) {
     JpaTemplate jpa = getJpaTemplate();
     
     Employee employee = findEmployeeByEmployeeNumber(userId);
     
     if (employee == null) {
       employee = new Employee();
       employee.setName(fullName);
       employee.setEmployeeId(userId);
       employee = jpa.merge(employee);
     }
     employee.setEmail(email);
     employee.setPhoneNumber(phone);
     return employee;
   }
   
   @Override
   public Student findOrCreateStudent(String studentNumber, String name, String email) {
     JpaTemplate jpa = getJpaTemplate();
     Student student = null; 
     
     @SuppressWarnings("unchecked")
     List<Student> studentList = jpa.find("select s from Student s fetch all properties where s.studentNumber = ?", studentNumber);
     if (studentList.isEmpty()) {
       student = new Student();
       student = jpa.merge(student);
     } else if (studentList.size() == 1) {
       student = studentList.get(0);
       loadEntity(student);
     } else {
       throw new NotImplementedException(getClass());
     }
     student.setName(name);
     student.setStudentNumber(studentNumber);
     student.setEmail(email);
     return student;
   }
 
   @SuppressWarnings("unchecked")
   private <T> T loadEntity(T entity) {
 //    try {
       Converter converter = new Converter();
       converter.addConversion(new LazyLoadConversion());
       converter.addConversion(new Iterateable2IterateableConversion());
       return (T)converter.convert(entity);
 //    } catch(Exception e) {
 //      log.error("entity " + entity, e);
 //    }
   }
 
   private abstract static class ObjectFinder<T extends AbamType> {
     private final JpaTemplate jpa;
 
     public ObjectFinder(JpaTemplate jpa) {
       this.jpa = jpa;
     }
     
     public T findOrCreate(T sourceObject) {
       T targetObject = null;
       if (sourceObject.getOid() == null) {
         // not previously persisted
       
         targetObject = findObject(sourceObject);
         if (targetObject == null) {
           targetObject = jpa.merge(sourceObject);
         } 
       } else {
         targetObject = jpa.merge(sourceObject);
       }
       return targetObject;
     }
     
     protected abstract T findObject(T source);
   }
 }
