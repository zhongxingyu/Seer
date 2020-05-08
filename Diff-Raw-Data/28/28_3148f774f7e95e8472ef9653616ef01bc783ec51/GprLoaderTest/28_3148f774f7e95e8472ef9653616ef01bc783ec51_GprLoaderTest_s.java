 package org.padacore.core.gnat.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.junit.Test;
 import org.padacore.core.gnat.GprLoader;
 import org.padacore.core.test.utils.CommonTestUtils;
 
 public class GprLoaderTest {
 
 	private GprLoader sut;
 	private IPath pathToProjectToLoad;
 
 	private void createFixture(String gprProjectName) {
 		sut = new GprLoader();
 		pathToProjectToLoad = new Path(CommonTestUtils.GetPathToTestProject() + gprProjectName);
 	}
 
 	private void exercize() {
 		this.sut.load(this.pathToProjectToLoad);
 	}
 
 	@Test
 	public void testLoading() {
 		this.createFixture("sample_project.gpr");
 
 		this.exercize();
 
 		checkNbOfLoadedProjects(2);
 
 		checkAttribute("Exec_Dir", "new_exe");
 		checkAttribute("Source_Dirs", new String[] { "src", "include" });
 		checkAttribute("Switches(\"main.ada\")", new String[] { "-v", "-gnatv" });
 		checkAttribute("Object_Dir", "new_exe");
 
 		checkVariable("my_var", "My_Var");
 		checkVariable("my_var2", "My_Var2");
 		checkVariable("Name", "My_Var");
 		checkVariable("Save_Name", "My_Var.saved");
 		checkVariable("Empty_List", new String[] {});
 		checkVariable("List_With_One_Element", new String[] { "-gnaty" });
 		checkVariable("List_With_Two_Elements", new String[] { "-gnaty", "-gnatg" });
 		checkVariable("Long_List", new String[] { "main.ada", "pack1_.ada", "pack1.ada",
 				"pack2_.ada" });
 
 		checkVariable("sample_project_included_exec_dir", "exe");
 		checkVariable("sample_project_included_compiler_warnings", new String[] { "-gnatwua",
 				"-gnaty", "-gnatQ" });
 		checkVariable("sample_project_included_compiler_switches", new String[] { "-gnatwua",
 				"-gnaty", "-gnatQ" });
 
 		assertEquals(this.sut.getLoadedProjects().get(0).getReferenceProjects().size(), 1);
 
 	}
 
 	@Test
 	public void testDefaultAttribute() {
 		this.createFixture("sample_project.gpr");
 
 		this.exercize();
 
 		this.checkAttribute("name", "sample_project");
		String pathToTestProject = CommonTestUtils.GetPathToTestProject();
 		this.checkAttribute("project_dir",
 				pathToTestProject.substring(0, pathToTestProject.length() - 1));
 
 	}
 	
 	@Test
 	public void testRenames() {
 		this.createFixture("sample_project.gpr");
 
 		this.exercize();
 		
 		this.checkAttribute("Builder'Switches (\"main.ada\")", new String[] {"-g"});
 	}
 
 	@Test
 	public void testExtends() {
 		this.createFixture("sample_project.gpr");
 
 		this.exercize();
 		
 		this.checkAttribute("Ide'Ui", "Qt");
 	}
 	
 	private void checkNbOfLoadedProjects(int expectedNumberOfLoadedProject) {
 		assertEquals("Number of loaded projects", expectedNumberOfLoadedProject, sut
 				.getLoadedProjects().size());
 	}
 
 	private void checkVariable(String variableName, String expectedValue) {
 		assertEquals("Variable value " + variableName, expectedValue, sut.getLoadedProjects()
 				.get(0).getVariable(variableName).getAsString());
 	}
 
 	private void checkVariable(String variableName, String[] expectedValues) {
 		List<String> computed = sut.getLoadedProjects().get(0).getVariable(variableName)
 				.getAsStringList();
 		assertEquals("Variable size " + variableName, expectedValues.length, computed.size());
 		for (int i = 0; i < expectedValues.length; i++) {
 			assertEquals("Variable n" + i, expectedValues[i], computed.get(i));
 		}
 	}
 
 	private void checkAttribute(String attributeName, String expectedValue) {
 		assertEquals("Attribute value", expectedValue,
 				sut.getLoadedProjects().get(0).getAttribute(attributeName).getAsString());
 	}
 
 	private void checkAttribute(String attributeName, String[] expectedValues) {
 		List<String> computed = sut.getLoadedProjects().get(0).getAttribute(attributeName)
 				.getAsStringList();
 		assertEquals("Attribute size", expectedValues.length, computed.size());
 		for (int i = 0; i < expectedValues.length; i++) {
 			assertEquals("Attribute n" + i, expectedValues[i], computed.get(i));
 		}
 	}
 }
