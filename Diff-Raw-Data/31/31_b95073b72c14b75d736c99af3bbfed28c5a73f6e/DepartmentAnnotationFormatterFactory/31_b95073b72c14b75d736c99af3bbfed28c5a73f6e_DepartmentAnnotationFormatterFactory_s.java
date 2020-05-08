 package net.kurochenko.ispub.department.form;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.format.AnnotationFormatterFactory;
 import org.springframework.format.Parser;
 import org.springframework.format.Printer;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author kurochenko
  */
 @Component
public final class DepartmentAnnotationFormatterFactory implements 
        AnnotationFormatterFactory<DepartmentFormat> {
 
     @Autowired
     DepartmentFormatter departmentFormatter;
 
     @Override
     public Set<Class<?>> getFieldTypes() {
         Set<Class<?>> set = new HashSet<Class<?>>();
         set.add(Department.class);
         return set;
     }
 
     @Override
     public Printer<Department> getPrinter(DepartmentFormat a, Class<?> type) {
         return departmentFormatter;
     }
 
     @Override
     public Parser<Department> getParser(DepartmentFormat a, Class<?> type) {
         return departmentFormatter;
     }
     
 }
