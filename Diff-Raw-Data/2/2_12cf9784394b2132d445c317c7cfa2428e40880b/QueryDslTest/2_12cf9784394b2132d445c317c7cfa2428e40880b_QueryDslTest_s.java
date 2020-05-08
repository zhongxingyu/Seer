 package com.sopovs.moradanen;
 
 import static com.sopovs.moradanen.domain.QSalary.salary;
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mysema.query.jpa.JPASubQuery;
 import com.mysema.query.jpa.impl.JPAQuery;
 import com.sopovs.moradanen.domain.Employee;
 import com.sopovs.moradanen.domain.QSalary;
 import com.sopovs.moradanen.domain.Salary;
 import com.sopovs.moradanen.domain.SalaryDay;
 import com.sopovs.moradanen.dto.EmployeeSumSalary;
 import com.sopovs.moradanen.dto.QEmployeeSumSalary;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:springContext.xml")
 @Transactional
 public class QueryDslTest {
 
     @PersistenceContext
     private EntityManager em;
 
     @Before
     public void setup() {
         Employee smith = new Employee("Smith");
         Employee anderson = new Employee("Anderson");
 
         em.persist(smith);
         em.persist(anderson);
 
         SalaryDay first = new SalaryDay();
         SalaryDay second = new SalaryDay();
         SalaryDay third = new SalaryDay();
 
         em.persist(first);
         em.persist(second);
         em.persist(third);
 
         em.persist(new Salary(smith, first, 5L));
         em.persist(new Salary(smith, second, 10L));
         em.persist(new Salary(smith, third, 15L));
 
         em.persist(new Salary(anderson, first, 7L));
         em.persist(new Salary(anderson, second, 14L));
         em.persist(new Salary(anderson, third, 21L));
     }
 
     @Test
     public void testSize() {
         assertEquals(6l, new JPAQuery(em).from(salary).count());
     }
 
     @Test
     public void testSalarySum() {
         assertEquals(
                 asList(new EmployeeSumSalary("Anderson", 42L), new EmployeeSumSalary("Smith", 30L)),
                 new JPAQuery(em)
                         .from(salary)
                         .groupBy(salary.employee)
                         .orderBy(salary.employee.name.asc())
                         .list(new QEmployeeSumSalary(salary.employee.name, salary.value.sum())));
     }
 
     @Test
     public void testCumulativeSalarySum() {
         QSalary salary2 = new QSalary("salary2");
         assertEquals(
                 asList(new EmployeeSumSalary("Anderson", 7L),
                         new EmployeeSumSalary("Anderson", 21L),
                         new EmployeeSumSalary("Anderson", 42L)),
                 new JPAQuery(em)
                         .from(salary)
                         .where(salary.employee.name.eq("Anderson"))
                        .orderBy(salary.employee.name.asc())
                         .list(new QEmployeeSumSalary(salary.employee.name,
                                 new JPASubQuery()
                                         .from(salary2)
                                         .where(salary.employee.eq(salary2.employee)
                                                 .and(salary.id.goe(salary2.id))
                                         )
                                         .unique(salary2.value.sum())
                                 )
                         ));
     }
 }
