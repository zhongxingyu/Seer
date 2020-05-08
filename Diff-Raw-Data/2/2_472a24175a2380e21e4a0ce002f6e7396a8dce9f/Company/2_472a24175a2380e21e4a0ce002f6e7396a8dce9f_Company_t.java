 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class Company {
     public static void main(final String args[]) {
 	final Employee emps[] = { new Employee("Finance", "Degree, Debbie"),
 		new Employee("Finance", "Grade, Geri"),
 		new Employee("Finance", "Extent, Ester"),
 		new Employee("Engineering", "Measure, Mary"),
 		new Employee("Engineering", "Amount, Anastasia"),
 		new Employee("Engineering", "Ratio, Ringo"),
		new Employee("Support", "Measure, Mary"),
		new Employee("Support", "Amount, Anastasia"),
 		new Employee("Support", "Rate, Rhoda"), };
 	final Set set = new TreeSet(Arrays.asList(emps));
 	System.out.println(set);
 	final Set set2 = new TreeSet(Collections.reverseOrder());
 	System.out.println(set2);
 	set2.addAll(Arrays.asList(emps));
 	System.out.println(set2);
 
 	final Set set3 = new TreeSet(new EmpComparator());
 	for (final Employee emp : emps) {
 	    set3.add(emp);
 	}
 	System.out.println(set3);
     }
 }
 
 class EmpComparator implements Comparator {
 
     public int compare(final Object obj1, final Object obj2) {
 	final Employee emp1 = (Employee) obj1;
 	final Employee emp2 = (Employee) obj2;
 
 	final int nameComp = emp1.getName().compareTo(emp2.getName());
 
 	return ((nameComp == 0) ? emp1.getDepartment().compareTo(
 		emp2.getDepartment()) : nameComp);
     }
 }
 
 class Employee implements Comparable {
     String department, name;
 
     public Employee(final String department, final String name) {
 	this.department = department;
 	this.name = name;
     }
 
     public String getDepartment() {
 	return department;
     }
 
     public String getName() {
 	return name;
     }
 
     @Override
     public String toString() {
 	return "[dept=" + department + ",name=" + name + "]";
     }
 
     public int compareTo(final Object obj) {
 	final Employee emp = (Employee) obj;
 	final int deptComp = department.compareTo(emp.getDepartment());
 
 	return ((deptComp == 0) ? name.compareTo(emp.getName()) : deptComp);
     }
 
     @Override
     public boolean equals(final Object obj) {
 	if (!(obj instanceof Employee)) {
 	    return false;
 	}
 	final Employee emp = (Employee) obj;
 	return department.equals(emp.getDepartment())
 		&& name.equals(emp.getName());
     }
 
     @Override
     public int hashCode() {
 	return 31 * department.hashCode() + name.hashCode();
     }
 }
