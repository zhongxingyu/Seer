 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.test.testcases;
 
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class MetaSubstitutionTest extends ExtensionAbstract {
 
 	@Override
 	public List<Option> config(){
 		List<Option> neu=super.config();
 		neu.add(mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-iface").versionAsInProject());
 		neu.add(mavenBundle("fr.imag.adele.apam.tests.services","apam-pax-samples-impl-s6").versionAsInProject());
 		return neu;
 	}
 	
 	@Test
 	public void SubstitutionGetPropertyString_tc089() {
 		Implementation impl = CST.apamResolver.findImplByName(null,
 				"MetasubstitutionStringTest");
 		
 		Instance instance = impl.createInstance(null, null);
 		
 		auxListProperties("\t", instance);
 		
 		Assert.assertTrue("geting property didnt work as expected",instance.getProperty("meta_string_retrieve").equals("goethe"));
 		Assert.assertTrue("prefixing didnt work as expected",instance.getProperty("meta_string_prefix").equals("pregoethe"));
 		Assert.assertTrue("postfixing didnt work as expected",instance.getProperty("meta_string_suffix").equals("goethepost"));
 		Assert.assertTrue("applying prefix and sufix at same time didnt work as expected",instance.getProperty("meta_string_prefix_suffix").equals("pregoethepost"));
 		
 	}
 	
 	@Test
 	public void SubstitutionGetPropertyOutsideDefinitionInSpecPropertyInImpl_tc090() {
 		Implementation subjectAimpl = CST.apamResolver.findImplByName(null,
 				"subject-a");
 		
 		Instance subjectA = subjectAimpl.createInstance(null, null);
 		
 		auxListProperties("\t", subjectA);
 		
		System.err.println(subjectA.getProperty("property-case-01"));
		
 		Assert.assertTrue("Given two composites A B, was not possible to reach the right value for a property of A through B by substituion (e.g. in B declare a property with the value '$AImpl.$property') ",subjectA.getProperty("property-case-01").equals("value-impl"));
 		
 	}
 	
 	@Test
 	public void SubstitutionGetPropertyOutsideDefinictionInSpecPropertyNowhere_tc091() {
 		Implementation subjectAimpl = CST.apamResolver.findImplByName(null,
 				"subject-a");
 		
 		Instance subjectA = subjectAimpl.createInstance(null, null);
 		
 		auxListProperties("\t", subjectA);
 		
		System.err.println(subjectA.getProperty("property-case-03"));
		
 		Assert.assertTrue("Given two composites A B, was not possible to reach the right value for a property of A through B by substituion (e.g. in B declare a property with the value '$AImpl.$property'): when there is only a definition in the Spec and no property in the Impl",subjectA.getProperty("property-case-03").equals("value-spec"));
 		
 	}
 
 	@Test
 	public void SubstitutionGetPropertyOutsideDefinitionNowherePropertyInImpl_tc092() {
 		Implementation subjectAimpl = CST.apamResolver.findImplByName(null,
 				"subject-a");
 		
 		Instance subjectA = subjectAimpl.createInstance(null, null);
 		
 		auxListProperties("\t", subjectA);
 		
 		System.err.println(subjectA.getProperty("property-case-08"));
 		
 		Assert.assertTrue("Given two composites A B, was not possible to reach the right value for a property of A through B by substituion (e.g. in B declare a property with the value '$AImpl.$property'): when there is only a definition in the Impl",subjectA.getProperty("property-case-08")!=null&&subjectA.getProperty("property-case-08").equals("value-impl"));
 		
 	}
 	
 	@Test
 	public void SubstitutionGetPropertyEscaped_tc095() {
 		Implementation subjectAimpl = CST.apamResolver.findImplByName(null,
 				"subject-a");
 		
 		Instance subjectA = subjectAimpl.createInstance(null, null);
 		
 		auxListProperties("\t", subjectA);
 		
		System.err.println(subjectA.getProperty("property-case-09"));
 		
		Assert.assertTrue(subjectA.getProperty("property-case-09").equals("$impl-case-09.$property-subject-b"));
 		
 	}
 	
 	@Test
 	public void FunctionCall_tc093() {
 		Implementation subjectAimpl = CST.apamResolver.findImplByName(null,
 				"subject-a");
 		
 		Instance subjectA = subjectAimpl.createInstance(null, null);
 		
 		auxListProperties("\t", subjectA);
 		
		Assert.assertTrue(subjectA.getProperty("function-case-01").equals("function called"));
 		
 	}
 	
 	@Test
 	public void FunctionCallEscaped_tc094() {
 		Implementation subjectAimpl = CST.apamResolver.findImplByName(null,
 				"subject-a");
 		
 		Instance subjectA = subjectAimpl.createInstance(null, null);
 		
 		auxListProperties("\t", subjectA);
 		
		Assert.assertTrue(subjectA.getProperty("function-case-02").equals("@functionCall"));
 		
 	}
 	
 	
 	
 }
