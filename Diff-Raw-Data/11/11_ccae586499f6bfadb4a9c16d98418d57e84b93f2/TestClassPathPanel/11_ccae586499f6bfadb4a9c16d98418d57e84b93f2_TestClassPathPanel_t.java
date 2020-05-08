 package fest.org.kalisen.classpathdoctor.gui;
 
 import java.io.File;
 
 import org.fest.swing.annotation.GUITest;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JButtonFixture;
 import org.fest.swing.fixture.JListItemFixture;
 import org.kalisen.classpathdoctor.DirectoryPath;
 import org.kalisen.classpathdoctor.gui.ClassPathDoctorGUI;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @Test
 @GUITest
 public class TestClassPathPanel {
 	private final static String SEPARATOR = System
 			.getProperty("path.separator");
 	private final static DirectoryPath CURRENT_DIR = new DirectoryPath(
 			new File("."));
 	private final static DirectoryPath PARENT_DIR = new DirectoryPath(new File(
 			".."));
 
 	private FrameFixture frame = null;
 
	@BeforeMethod(alwaysRun=true)
 	protected void setUp() {
 		this.frame = new FrameFixture(new ClassPathDoctorGUI());
 		this.frame.show();
 	}
 	
 	@Test(groups="DnD")
 	public void testDroppingOnStartingPointShouldHaveNoEffect() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		this.frame.list().drag(2);
 		this.frame.list().drop(2);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.list().drag(2);
 		this.frame.list().drop(0);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
		this.frame.list().requireSelection(ENTRY3);
 	}
 
 	@Test(groups="DnD")
 	public void testDraggingObjectToTheTop() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		this.frame.list().drag(2);
 		this.frame.list().drop(0);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
		this.frame.list().requireSelection(ENTRY3);
 	}
 
 	@Test(groups="DnD")
 	public void testDraggingObjectToTheBottom() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		this.frame.list().drag(0);
 		this.frame.list().drop(2);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY1);
		this.frame.list().requireSelection(ENTRY1);
 	}
 	
 	public void testAddAnEntryButton() {
 		String path = ".";
 		addEntry(path);
 		assertListEntryEquals(this.frame.list().item(0), ".");
 		this.frame.textBox().requireText(
 				CURRENT_DIR.getFile().getAbsolutePath());
 	}
 
 	public void testRemoveAnEntryButton() {
 		addEntry(".");
 		addEntry("..");
 		addEntry(".");
 		this.frame.list().selectItem(0);
 		JButtonFixture removeButton = this.frame.button("REMOVE_ENTRY");
 		removeButton.click();
 		this.frame.textBox().requireText(
 				PARENT_DIR.getFile().getAbsolutePath() + SEPARATOR
 						+ CURRENT_DIR.getFile().getAbsolutePath());
 		assertListEntryEquals(this.frame.list().item(0), "..");
 		assertListEntryEquals(this.frame.list().item(1), ".");
 		this.frame.list().requireNoSelection();
 
 		this.frame.list().selectItem(0);
 		removeButton.click();
 		this.frame.textBox().requireText(
 				CURRENT_DIR.getFile().getAbsolutePath());
 		assertListEntryEquals(this.frame.list().item(0), ".");
 		this.frame.list().requireNoSelection();
 
 		this.frame.list().selectItem(0);
 		removeButton.click();
 		this.frame.textBox().requireEmpty();
 		Assert.assertTrue(this.frame.list().contents().length == 0);
 		this.frame.list().requireNoSelection();
 	}
 
 	public void testRemoveAnEntryWithCurrentAndParentDirectories() {
 		this.frame.textBox().setText(
 				"." + SEPARATOR + ".." + SEPARATOR + "." + SEPARATOR + "..");
 		Assert.assertEquals(this.frame.list().item(0).value(), ".");
 		Assert.assertEquals(this.frame.list().item(1).value(), "..");
 		Assert.assertEquals(this.frame.list().item(2).value(), ".");
 		Assert.assertEquals(this.frame.list().item(3).value(), "..");
 		// then remove the first entry
 		this.frame.list().selectItem(0);
 		JButtonFixture removeButton = this.frame.button("REMOVE_ENTRY");
 		removeButton.click();
 		this.frame.textBox().requireText(
 				".." + SEPARATOR + "." + SEPARATOR + "..");
 		// then remove the first entry again
 		this.frame.list().selectItem(0);
 		removeButton.click();
 		this.frame.textBox().requireText("." + SEPARATOR + "..");
 	}
 
 	public void testMoveUpButton() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.list().selectItem(2);
 		JButtonFixture moveUpButton = this.frame.button("MOVE_UP");
 		moveUpButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
 		moveUpButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
 		moveUpButton.requireDisabled();
 	}
 
 	public void moveSeveralEntriesUp() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.list().selectItems(1, 2);
 		JButtonFixture moveUpButton = this.frame.button("MOVE_UP");
 		moveUpButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY1);
 		moveUpButton.requireDisabled();
 	}
 
 	public void moveUpButtonShouldBeDisabledWhenEntryAtTheTop() {
 		final String ENTRY1 = "entry1";
 		addEntry(ENTRY1);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		this.frame.list().selectItems(0);
 		this.frame.button("MOVE_UP").requireDisabled();
 	}
 
 	public void moveUpButtonShouldBeDisabledWhenNoEntrySelected() {
 		this.frame.button("MOVE_UP").requireDisabled();
 	}
 
 	public void testMoveDownButton() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.list().selectItem(0);
 		JButtonFixture moveDownButton = this.frame.button("MOVE_DOWN");
 		moveDownButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		moveDownButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY1);
 		moveDownButton.requireDisabled();
 	}
 
 	public void moveSeveralEntriesDown() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.list().selectItems(0, 1);
 		JButtonFixture moveUpButton = this.frame.button("MOVE_DOWN");
 		moveUpButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
 		moveUpButton.requireDisabled();
 	}
 
 	public void moveDownButtonShouldBeDisabledWhenEntryAtTheBottom() {
 		final String ENTRY1 = "entry1";
 		addEntry(ENTRY1);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		this.frame.list().selectItems(0);
 		this.frame.button("MOVE_DOWN").requireDisabled();
 	}
 
 	public void moveDownButtonShouldBeDisabledWhenNoEntrySelected() {
 		this.frame.button("MOVE_DOWN").requireDisabled();
 	}
 
 	public void changingTheContentOfTheListIsReflectedInTheTextArea() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.textBox().requireText(
 				getAbsolutePath(ENTRY1) + SEPARATOR + getAbsolutePath(ENTRY2)
 						+ SEPARATOR + getAbsolutePath(ENTRY3));
 		this.frame.list().selectItem(0);
 		JButtonFixture moveUpButton = this.frame.button("MOVE_DOWN");
 		moveUpButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.textBox().requireText(
 				getAbsolutePath(ENTRY2) + SEPARATOR + getAbsolutePath(ENTRY1)
 						+ SEPARATOR + getAbsolutePath(ENTRY3));
 		moveUpButton.click();
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY1);
 		this.frame.textBox().requireText(
 				getAbsolutePath(ENTRY2) + SEPARATOR + getAbsolutePath(ENTRY3)
 						+ SEPARATOR + getAbsolutePath(ENTRY1));
 		moveUpButton.requireDisabled();
 	}
 
 	public void testRemoveTheLastElementInTheList() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		this.frame.list().selectItem(1);
 		JButtonFixture removeButton = this.frame.button("REMOVE_ENTRY");
 		removeButton.click();
 		this.frame.textBox().requireText(getAbsolutePath(ENTRY1));
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		this.frame.list().requireNoSelection();
 	}
 
 	protected void addEntry(String path) {
 		File file = new File(path);
 		this.frame.button("ADD_ENTRY").click();
 		this.frame.fileChooser().setCurrentDirectory(CURRENT_DIR.getFile())
 				.selectFile(file).approve();
 	}
 
 	protected String getAbsolutePath(String relativePath) {
 		return new File(relativePath).getAbsolutePath();
 	}
 
 	protected void assertListEntryEquals(JListItemFixture listItemFixture,
 			String expectedRelativePath) {
 		Assert.assertEquals(listItemFixture.value(), new File(
 				expectedRelativePath).getAbsolutePath());
 	}
 
	@AfterMethod(alwaysRun=true)
 	public void tearDown() {
 		this.frame.cleanUp();
 	}
 
 }
