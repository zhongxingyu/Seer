 package awana.database;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 
 /**
  *
  * @author Renlar
  */
 public final class Record {
 
 	private static ArrayList<Field> masterFieldList = new ArrayList<>();
 	private static ArrayList<Book> masterBookList = new ArrayList<>();
 	private static String[] fieldNameList = {
 		"First Name", "Last Name", "Birth Date", "Email", "Mother",
 		"Mother Cell", "Father", "Father Cell", "Legal Guardian", "Legal Guardian Phone",
 		"Parent Email", "Address Line1", "Address Line2", "City", "State/Province",
 		"Zip", "Emergency Contact", "Emergency Contact Phone"
 	};
 	private static String[] fieldStorageType = {
 		"VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)",
 		"VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)",
 		"VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)",
 		"VARCHAR(100)", "VARCHAR(100)", "VARCHAR(100)"
 	};
 	private static String[] fieldDataType = {
 		"name", "name", "date", "email", "name",
 		"phone","name", "phone", "name", "phone",
 		"email", "string", "string", "string15", "string5",
 		"int", "name", "phone"
 	};
 	private static int[] fieldDisplayLength;
 	private static String[] fieldTypes = {
 		"default", "int", "string5", "string10", "string15", "string20", "string", "name", "email", "phone", "date"
 	};
 	private static int[] fieldTypeDefaultDisplayLength = {
 		10, 8, 5, 10, 15, 20, 30, 15, 20, 12, 10
 	};
 	private static String fieldDefaultValue[] = {
 		null, null, null, null, null,
 		null, null, null, null, null,
 		null, null, null, null, "CO",
 		null, null, null
 	};
 	private int ID;
 	private ArrayList<Field> fieldList;
 	private ArrayList<Book> books;
 
 	public Record(int ID) {
 		this.fieldList = (ArrayList<Field>)masterFieldList.clone();
 		this.books = (ArrayList<Book>)masterBookList.clone();
 		this.ID = ID;
 	}
 
 	public static void loadMasterData() {
 		Field f;
 		for (int i = 0; i < fieldNameList.length; i++) {
 			f = new Field(fieldNameList[i], fieldDefaultValue[i], fieldDataType[i], fieldStorageType[i], getDisplayLengthByType(fieldDataType[i]));
 			addMasterField(f);
 		}
 		for(int i = 0; i < Book.bookNames.length; i++){
 			String name = Book.bookNames[i];
 			ArrayList<Section> section = new ArrayList<>();
 			for(int j = 0; j < Book.bookSections[i].length; j++){
 				section.add(new Section(Book.bookSections[i][j], false, null));
 			}
 			masterBookList.add(new Book(name, section, false, null));
 		}
 	}
 
 	public static ArrayList<Book> getMasterBookList(){
 		return (ArrayList<Book>)masterBookList.clone();
 	}
 
 	public int getID() {
 		return ID;
 	}
 
 	public Field get(String field) {
 		Field f;
 		for (int i = 0; i < fieldList.size(); i++) {
 			f = fieldList.get(i);
 			if (f.getName().equalsIgnoreCase(field)) {
 				return f;
 			}
 		}
 		return null;
 	}
 
 	public Field getField(int number) {
 		return fieldList.get(number);
 	}
 
 	public ArrayList<Field> getFieldList() {
 		return (ArrayList<Field>) fieldList.clone();
 	}
 
 	public ArrayList<Book> getBookList() {
 		return (ArrayList<Book>) books.clone();
 	}
 
 	public void setBookList(ArrayList<Book> books) {
 		this.books = books;
 	}
 
 	public int getBookListSize() {
 		return books.size();
 	}
 
 	public Book getBook(int i) {
 		return books.get(i);
 	}
 
 	public void setFieldList(ArrayList<Field> f) {
 		fieldList = (ArrayList<Field>)f.clone();
 	}
 
 	public Listing createListing() {
 		return new Listing(ID, this.get("First Name").getData(), this.get("Last Name").getData());
 	}
 
 	public static Field getMasterField(int number) {
 		return masterFieldList.get(number);
 	}
 
 	public static Book getMasterBook(int number){
 		return masterBookList.get(number);
 	}
 
 	public int getFieldListSize() {
 		return fieldList.size();
 	}
 
 	public static int getMasterFieldListSize() {
 		return masterFieldList.size();
 	}
 
 	public static void addMasterField(Field f) {
 		masterFieldList.add(f);
 	}
 
 	public static int getDisplayLengthByType(String type) {
 		for (int i = 0; i < fieldTypes.length; i++) {
 			if (type.equalsIgnoreCase(fieldTypes[i])) {
 				return fieldTypeDefaultDisplayLength[i];
 			}
 		}
 		return fieldTypeDefaultDisplayLength[0];
 	}
 
 	public static ArrayList<String> getBookGroups() {
 		ArrayList<String> names = new ArrayList<>();
 		String group;
 		String oldName = null;
 		for (int i = 0; i < masterBookList.size(); i++) {
 			group = masterBookList.get(i).getGroup();
 			if(oldName == null || !group.equals(oldName)){
 				names.add(group);
 			}
 			oldName = group;
 		}
 		return names;
 	}
 
 	public static ArrayList<String> getBookNamesByGroup(String group){
 		ArrayList<String> names = new ArrayList<>();
 		for(int i = 0; i < masterBookList.size(); i++){
 			Book book = masterBookList.get(i);
 			if(book.getGroup().equals(group)){
 				names.add(book.getName());
 			}
 		}
 		return names;
 	}
 
 	public static ArrayList<Book> getMasterBooksByGroup(String group){
 		ArrayList<Book> books = new ArrayList<>();
 		for(int i = 0; i < masterBookList.size(); i++){
 			Book book = masterBookList.get(i);
 			if(book.getGroup().equals(group)){
 				books.add(book);
 			}
 		}
 		return books;
 	}
 
 	private ArrayList<Book> getBooksByGroup(String group) {
 		ArrayList<Book> groupBooks = new ArrayList<>();
 		for(int i = 0; i < books.size(); i++){
 			Book book = books.get(i);
 			if(book.getGroup().equals(group)){
 				groupBooks.add(book);
 			}
 		}
 		return groupBooks;
 	}
 
 	public void draw(JTabbedPane pane){
 		setupFieldTab(pane);
 		setupBookTabs(pane);
 	}
 
 	public void setupFieldTab(JTabbedPane pane) {
 		JPanel contactPane = new JPanel();
 		contactPane.setName("Contact");
 		contactPane.setLayout(new WrapLayout());
 		contactPane.setSize(new Dimension(100, 100));
 		for (int i = 0; i < getFieldListSize(); i++) {
 			contactPane.add(getField(i).getRenderable());
 		}
 		JScrollPane scrollPane = new JScrollPane(contactPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setName("Contact");
 		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
 		pane.add(scrollPane.getName(), scrollPane);
 	}
 
 	public void setupBookTabs(JTabbedPane pane) {
 		ArrayList<String> bookTabNames = getBookGroups();
 		for (int i = 0; i < bookTabNames.size(); i++) {
 			JPanel jPanel = new JPanel();
 			BoxLayout layout = new BoxLayout(jPanel, BoxLayout.Y_AXIS);
 			jPanel.setLayout(layout);
 			jPanel.setName(bookTabNames.get(i));
 			ArrayList<Book> bookGroup = getBooksByGroup(bookTabNames.get(i));
 			for (int k = 0; k < bookGroup.size(); k++) {
 				jPanel.add(bookGroup.get(k).getRenderable());
 				jPanel.add(new JSeparator());
 			}
 			JScrollPane scrollPane = new JScrollPane(jPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 			scrollPane.setName(bookTabNames.get(i));
 			scrollPane.getVerticalScrollBar().setUnitIncrement(12);
 			pane.add(scrollPane.getName(), scrollPane);
 			scrollPane.validate();
 		}
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(o == null){
 			return false;
 		}
 		if (o.getClass() == this.getClass()) {
 			Record s = (Record) o;
 			if (s.getID() == this.getID()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 5;
 		hash = 79 * hash + this.ID;
 		return hash;
 	}
 
 	@Override
 	public String toString(){
 		StringBuilder b = new StringBuilder("\n" + getID() + " Fields:\n");
 		for(int i = 0; i < fieldList.size(); i++){
 			b.append(fieldList.get(i).toString());
 		}
 		b.append("\n Books:");
 		for(int i = 0; i < books.size(); i++){
 			b.append(books.get(i).toString());
 		}
 		b.append("\n");
 		return b.toString();
 	}
 }
