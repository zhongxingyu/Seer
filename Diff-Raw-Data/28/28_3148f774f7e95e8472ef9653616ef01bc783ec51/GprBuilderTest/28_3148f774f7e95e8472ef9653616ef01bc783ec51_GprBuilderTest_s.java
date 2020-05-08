 package org.padacore.core.gnat.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Path;
 import org.junit.Test;
 import org.padacore.core.gnat.GprBuilder;
 import org.padacore.core.gnat.GprProject;
 import org.padacore.core.gnat.Symbol;
 import org.padacore.core.test.stubs.PropertiesProviderStub;
 import org.padacore.core.test.utils.CommonTestUtils;
 
 public class GprBuilderTest {
 
 	private PropertiesProviderStub propertiesProviderStub;
 	private GprBuilder sut;
 
 	private void createFixture() {
 		this.propertiesProviderStub = new PropertiesProviderStub();
 		this.sut = new GprBuilder(propertiesProviderStub, new Path(
 				CommonTestUtils.GetPathToTestProject() + "sample_project.gpr"));
 	}
 
 	@Test
 	public void test() {
 
 		this.createFixture();
 
 		String name = "sample_project";
 
 		this.propertiesProviderStub.setName(name);
 		this.propertiesProviderStub.addAttribute("Exec_Dir", Symbol.CreateString("new_exe"));
 		this.propertiesProviderStub.addAttribute("Object_Dir", Symbol.CreateString("obj"));
 
 		List<String> sourcesDir = new ArrayList<String>();
 		sourcesDir.add("src");
 		sourcesDir.add("include");
 		this.propertiesProviderStub.addAttribute("Source_Dirs", Symbol.CreateStringList(sourcesDir));
 		
 		List<String> main = new ArrayList<String>();
 		main.add("main.adb");			
 		this.propertiesProviderStub.addAttribute("Main", Symbol.CreateStringList(main));
 		
 		GprProject gpr = this.sut.build();
 
 		assertEquals("sample_project", gpr.getName());
		assertEquals(new Path(CommonTestUtils.GetPathToTestProject()), gpr.getRootDirPath());
 		assertEquals("new_exe", gpr.getExecutableDir());
 		assertEquals("obj", gpr.getObjectDir());
 		assertEquals(2, gpr.getSourcesDir().size());
 		assertEquals("src", gpr.getSourcesDir().get(0));
 		assertEquals("include", gpr.getSourcesDir().get(1));
 		assertTrue(gpr.isExecutable());
 		assertEquals("main.adb", gpr.getExecutableSourceNames().get(0));
 
 	}
 
 }
