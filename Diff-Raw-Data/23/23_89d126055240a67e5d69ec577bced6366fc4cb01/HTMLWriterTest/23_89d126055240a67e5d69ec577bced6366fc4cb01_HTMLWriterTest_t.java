 package test;
 
 import static org.junit.Assert.assertEquals;
 import html.HTMLWriter;
 import items.User;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class HTMLWriterTest {
 
 	private StringWriter sw;
 	private HTMLWriter hw;
 
 	@Before
 	public void setUp() throws Exception {
 		sw = new StringWriter();
 		hw = new HTMLWriter(new PrintWriter(sw));
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		sw = null;
 		hw = null;
 	}
 
 	@Test
 	public void testPrintHead() {
 		hw.printHead(null);
 		assertEquals(
 				"<html><head><meta charset=\"latin1\"><title>E-PUSS 1301</title></head><body><h1>E-PUSS 1301</h1>",
 				sw.toString());
 	}
 
 	@Test
 	public void testPrintFoot() {
 		hw.printFoot();
 		assertEquals("</body></html>", sw.toString());
 	}
 
 	@Test
 	public void testPrintAddUserForm() {
 		hw.printAddUserForm();
 		assertEquals(
 				"<form method=\"POST\" action=\"/ProjectPUSS/administration?action=createUser\">"
 						+ "<label>Användarnamn</label><input name=\"username\" type=\"text\" />"
 						+ "<input type=\"submit\" value=\"Skapa\" />"
 						+ "</form>", sw.toString());
 	}
 
 	@Test
 	public void testPrintAddProjectGroupForm() {
 		hw.printAddProjectGroupForm();
 		assertEquals(
 				"<form method=\"POST\" action=\"/ProjectPUSS/projectadmin?action=createProjectGroup\">"
 				+ "<label>Projekgruppnamn</label><input name=\"projectname\" type=\"text\" />"
 				+ "<input type=\"submit\" value=\"Skapa\" />"
 						+ "</form>", sw.toString());
 	}
 
 	@Test
 	public void testPrintadminUserListEmptyList() {
 		ArrayList<User> users = new ArrayList<User>();
 		hw.printAdminUserList(users);
 		assertEquals("", sw.toString());
 	}
 
 	@Test
 	public void testPrintadminUserListOneUser() {
 		ArrayList<User> users = new ArrayList<User>();
 		users.add(new User("abcde0", "password"));
 		hw.printAdminUserList(users);
 		assertEquals(
 				"<table><tr><th>Användare</th><th>Passwords</th><th></th></tr><tr><td>abcde0</td><td>password</td><td><a href=\"/ProjectPUSS/administration?action=deleteUser&username=abcde0\">Ta bort</a></td></tr></table>",
 				sw.toString());
 	}
 
 	@Test
 	public void testPrintadminUserListMultipleUsers() {
 		ArrayList<User> users = new ArrayList<User>();
 		users.add(new User("abcde0", "password0"));
 		users.add(new User("abcde1", "password1"));
 		users.add(new User("abcde2", "password2"));
 		hw.printAdminUserList(users);
 		assertEquals("<table><tr><th>Användare</th><th>Passwords</th><th></th></tr>"
 				+ "<tr><td>abcde0</td><td>password0</td><td><a href=\"/ProjectPUSS/administration?action=deleteUser&username=abcde0\">Ta bort</a></td></tr>"
 				+ "<tr><td>abcde1</td><td>password1</td><td><a href=\"/ProjectPUSS/administration?action=deleteUser&username=abcde1\">Ta bort</a></td></tr>"
 				+ "<tr><td>abcde2</td><td>password2</td><td><a href=\"/ProjectPUSS/administration?action=deleteUser&username=abcde2\">Ta bort</a></td></tr>" 
 				+ "</table>",
 				sw.toString());
 	}
 	
 	@Test
 	public void testPrintAddUserToProjectGroupForm() {
 		hw.printAddUserToProjectGroupForm("whatever");
 		assertEquals(
 				"<form method=\"POST\" action=\"/ProjectPUSS/projectoverview?action=addUser&project=whatever\">"
 				+ "<label>Användarnamn</label><input name=\"username\" type=\"text\" />"
 				+ "<input type=\"submit\" value=\"Lägg till\" />"
 						+ "</form>", sw.toString());
 	}
 	
 	@Test
 	public void testPrintProjectGroupEmpty() {
 		hw.printProjectGroupMembers(new ArrayList<User>(), new ArrayList<User>(), "whatever");
 		assertEquals("", sw.toString());
 	}
 	
 	@Test
 	public void testPrintProjectGroupMembersOneUser() {
 		List<User> users = new ArrayList<User>();
 		List<User> projectManagers = new ArrayList<User>();
 		users.add(new User("Alpha", "Numerisk"));
 		projectManagers.add(new User("Alpha", "Numerisk"));
 
 		hw.printProjectGroupMembers(users, projectManagers, "whatever");
 		
		assertEquals("<table><tr><th>Projektledare</th><th></th><th></th></tr><tr>"
 				+ "<td>Alpha</td><td>"
 				+ "<a href=\"projectoverview?action=demoteUser&project=whatever&username=Alpha\">Gör till användare</a></td>"
 				+ "<td><a href=\"projectoverview?action=deleteUser&project=whatever&username=Alpha\">Ta bort</a></td>"
				+ "</tr><tr><th>Användare</th><th></th><th></th></tr></table>", sw.toString());
 	}
 	
 	@Test
 	public void testPrintProjectGroups() {
 		List<String> groups = new ArrayList<String>();
 		groups.add(new String("Project1"));
 		groups.add(new String("Project2"));
 		
 		hw.printProjectGroups(groups);
 		
 		assertEquals("<table><tr><th>Projektgrupper</th><th></th></tr>"
 				+ "<tr><td><a href=\"projectoverview?project=Project1\">Project1</a></td><td><a href=\"projectadmin?action=removeProjectGroup&projectName=Project1\">Ta bort</a></td></tr>"
 				+ "<tr><td><a href=\"projectoverview?project=Project2\">Project2</a></td><td><a href=\"projectadmin?action=removeProjectGroup&projectName=Project2\">Ta bort</a></td></tr></table>",sw.toString());
 		
 		
 	}
 	
 	@Test
 	public void testPrintSuccessMessage() {
 		hw.printSuccessMessage("Success in some successful action");
 		assertEquals("<p style=\"color:green;\">Success in some successful action</p>", sw.toString());
 	}
 	
 	@Test
 	public void testPrintErrorMessage() {
 		hw.printErrorMessage("Error");
 		assertEquals("<p style=\"color:red;\">Error</p>", sw.toString());
 	}
 	
 	@Test
 	public void testPrintLink() {
 		hw.printLink("http://url.com/", "A cool place to be");
 		assertEquals("<a href=\"http://url.com/\">A cool place to be</a>" + System.getProperty("line.separator"), sw.toString());
 	}
 
 }
