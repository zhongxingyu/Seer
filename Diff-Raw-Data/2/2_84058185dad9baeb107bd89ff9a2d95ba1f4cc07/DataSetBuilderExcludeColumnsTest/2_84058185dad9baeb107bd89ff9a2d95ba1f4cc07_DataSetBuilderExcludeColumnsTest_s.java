 /**
  * Copyright (C) 2009 kiy0taka.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kiy0taka.dbunit;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.dbunit.Assertion;
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class DataSetBuilderExcludeColumnsTest {
 
     private String inFileName;
 
     private String expectedFileName;
 
     private String[] excludeColumns;
 
     public DataSetBuilderExcludeColumnsTest(String inFileName, String[] excludeColumns, String expectedFileName) {
         this.inFileName = inFileName;
         this.excludeColumns = excludeColumns;
         this.expectedFileName = expectedFileName;
     }
 
     @Test
    public void test() throws IOException, DatabaseUnitException {
         IDataSet in = new FlatXmlDataSet(getClass().getResource("filter/" + inFileName));
         IDataSet expected = new FlatXmlDataSet(getClass().getResource("filter/" + expectedFileName));
         IDataSet actual = new DataSetBuilder(in).excludeColumns(excludeColumns).toDataSet();
         Assertion.assertEquals(expected, actual);
     }
 
     @Parameters
     public static List<Object[]> parameters() {
         return Arrays.asList(new Object[][] {
             {"emp.xml",      a(),                            "emp.xml"},
             {"emp.xml",      a("empno"),                     "emp_exclude_emp_empno.xml"},
             {"emp.xml",      a("emp.empno"),                 "emp_exclude_emp_empno.xml"},
             {"emp.xml",      a("empno", "ename"),            "emp_exclude_emp_empno_emp_ename.xml"},
             {"emp.xml",      a("emp.empno", "emp.ename"),    "emp_exclude_emp_empno_emp_ename.xml"},
             {"emp_dept.xml", a(),                            "emp_dept.xml"},
             {"emp_dept.xml", a("empno"),                     "emp_dept_exclude_emp_empno.xml"},
             {"emp_dept.xml", a("emp.empno"),                 "emp_dept_exclude_emp_empno.xml"},
             {"emp_dept.xml", a("dname"),                     "emp_dept_exclude_dept_dname.xml"},
             {"emp_dept.xml", a("dept.dname"),                "emp_dept_exclude_dept_dname.xml"},
             {"emp_dept.xml", a("deptno"),                    "emp_dept_exclude_emp_deptno_dept_deptno.xml"},
             {"emp_dept.xml", a("emp.deptno"),                "emp_dept_exclude_emp_deptno.xml"},
             {"emp_dept.xml", a("dept.deptno"),               "emp_dept_exclude_dept_deptno.xml"},
             {"emp_dept.xml", a("emp.deptno", "dept.deptno"), "emp_dept_exclude_emp_deptno_dept_deptno.xml"},
             {"emp_dept.xml", a("empno", "dname"),            "emp_dept_exclude_emp_empno_dept_dname.xml"},
             {"emp_dept.xml", a("emp.empno", "dept.dname"),   "emp_dept_exclude_emp_empno_dept_dname.xml"}
         });
     }
 
     private static String[] a(String... strings) {
         return strings;
     }
 }
