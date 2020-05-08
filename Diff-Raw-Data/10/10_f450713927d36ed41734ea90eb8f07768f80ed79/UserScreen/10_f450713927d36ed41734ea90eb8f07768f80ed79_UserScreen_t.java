 package org.redhatchallenge.rhc2013.client;
 
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.SelectionCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.DefaultSelectionEventManager;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionModel;
 import org.redhatchallenge.rhc2013.shared.Student;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import static com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 
 /**
  * @author: Terry Chia (terrycwk1994@gmail.com)
  */
 public class UserScreen extends Composite {
     interface UserScreenUiBinder extends UiBinder<Widget, UserScreen> {
     }
 
     private static UserScreenUiBinder UiBinder = GWT.create(UserScreenUiBinder.class);
 
     @UiField TextBox searchField;
     @UiField ListBox searchTerms;
     @UiField Button searchButton;
     @UiField Button registerButton;
     @UiField Button deleteButton;
     @UiField Button exportButton;
     @UiField Button refreshButton;
     @UiField CellTable<Student> cellTable;
     @UiField MySimplePager pager;
     @UiField Label registrationLabel;
     @UiField Label verifiedLabel;
     @UiField Label testing;
 
     private UserServiceAsync userService = UserService.Util.getInstance();
     private List<Student> studentList;
     private ListDataProvider<Student> provider;
     private List<Student> listOfSelectedStudents = new ArrayList<Student>();
     private static final ProvidesKey<Student> KEY_PROVIDER = new ProvidesKey<Student>() {
         @Override
         public Object getKey(Student item) {
             return item.getEmail();
         }
     };
 
     public UserScreen() {
 
         initWidget(UiBinder.createAndBindUi(this));
 
         userService.getListOfStudents(new AsyncCallback<List<Student>>() {
             @Override
             public void onFailure(Throwable caught) {
                 caught.printStackTrace();
             }
 
             @Override
             public void onSuccess(List<Student> result) {
 
                 studentList = result;
 
                 setUserCount();
 
                 provider = new ListDataProvider<Student>(studentList);
                 provider.addDataDisplay(cellTable);
 
                 initCellTable();
             }
         });
 
         pager.setDisplay(cellTable);
         pager.setPageSize(2);
     }
 
     private void initCellTable() {
 
         List list = provider.getList();
 
         final MultiSelectionModel<Student> selectionModel = new MultiSelectionModel<Student>(KEY_PROVIDER);
 
         Column<Student, Boolean> selectColumn = new Column<Student, Boolean>(new CheckboxCell(true, false)) {
             @Override
             public Boolean getValue(Student student) {
                 testing.setText(listOfSelectedStudents + "");
                 return selectionModel.isSelected(student);
 
             }
         };
 
         selectColumn.setFieldUpdater(new FieldUpdater<Student, Boolean>() {
             @Override
             public void update(int index, Student student, Boolean value) {
                 if(value) {
                     listOfSelectedStudents.add(student);
                     testing.setText(listOfSelectedStudents + "");
                 }
 
                 else {
                     listOfSelectedStudents.remove(student);
                 }
             }
         });

        // checkbox header
        Header<Boolean> selectAllHeader = new Header<Boolean>(new CheckboxCell(true, false)) {
             @Override
             public Boolean getValue() {
                 for (Student student : cellTable.getVisibleItems()){
                     if (!selectionModel.isSelected(student)){
                         return false;
                     }
                 }
                 return cellTable.getVisibleItems().size() > 0;
             }
         };
 
         selectAllHeader.setUpdater(new ValueUpdater<Boolean>() {
             @Override
             public void update(Boolean aBoolean) {
                 for(Student student : cellTable.getVisibleItems()){
                     selectionModel.setSelected(student, aBoolean);
                 }
                 if (aBoolean == true){
                     for (int i=0;i<cellTable.getVisibleItemCount(); i++){
                         if (!listOfSelectedStudents.contains(cellTable.getVisibleItem(i)))
                             listOfSelectedStudents.add(cellTable.getVisibleItem(i));
                     }
                     testing.setText(listOfSelectedStudents.toString());
                 }
                 else if (aBoolean == false){
                     for (int i=0;i<cellTable.getVisibleItemCount(); i++){
                         listOfSelectedStudents.remove(cellTable.getVisibleItem(i));
                     }
                 testing.setText(listOfSelectedStudents.toString());
                 }
             }
         });//End of checkbox
 
         Column<Student, String> emailColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getEmail();
             }
         };
 
         emailColumn.setSortable(true);
         emailColumn.setDefaultSortAscending(false);
         final ListHandler<Student> emailSortHandler = new ListHandler<Student>(list);
         emailSortHandler.setComparator(emailColumn, new Comparator<Student>() {
             @Override
             public int compare(Student o1, Student o2) {
                 if (o1 == o2) {
                     return 0;
                 }
                 if (o1 != null) {
                     return (o2 != null) ? o1.getEmail().compareTo(o2.getEmail()) : 1;
                 }
                 return -1;
             }
         });
 
         cellTable.addColumnSortHandler(emailSortHandler);
         cellTable.getColumnSortList().push(emailColumn);
 
         emailColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setEmail(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> firstNameColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getFirstName();
             }
         };
 
         firstNameColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setFirstName(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> lastNameColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getLastName();
             }
         };
 
         lastNameColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setLastName(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         ArrayList<String> countryList = new ArrayList<String>();
             countryList.add("Singapore");
             countryList.add("Malaysia");
             countryList.add("Thailand");
             countryList.add("China/Region 1");
             countryList.add("China/Region 2");
             countryList.add("China/Region 3");
             countryList.add("China/Region 4");
             countryList.add("China/Region 5");
             countryList.add("China/Region 6");
             countryList.add("China/Others");
             countryList.add("Hong Kong");
             countryList.add("Taiwan");
 
         Column<Student, String> countryColumn = new Column<Student, String>(new SelectionCell(countryList)) {
             @Override
             public String getValue(Student student) {
                 return student.getCountry();
             }
         };
 
         countryColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setCountry(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         ArrayList<String> countryCodeList = new ArrayList<String>();
             countryCodeList.add("+65");
             countryCodeList.add("+60");
             countryCodeList.add("+66");
             countryCodeList.add("+86");
             countryCodeList.add("+852");
             countryCodeList.add("+886");
 
         Column<Student, String> countryCodeColumn = new Column<Student, String>(new SelectionCell(countryCodeList)) {
             @Override
             public String getValue(Student student) {
                 return student.getCountryCode();
             }
         };
 
         countryCodeColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setCountryCode(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> contactColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getContact();
             }
         };
 
         contactColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setContact(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> schoolColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getSchool();
             }
         };
 
         schoolColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setSchool(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> lecturerFirstNameColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getLecturerFirstName();
             }
         };
 
         lecturerFirstNameColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setLecturerFirstName(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> lecturerLastNameColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getLecturerLastName();
             }
         };
 
 
         lecturerLastNameColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setLecturerLastName(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, String> lecturerEmailColumn = new Column<Student, String>(new EditTextCell()) {
             @Override
             public String getValue(Student student) {
                 return student.getLecturerEmail();
             }
         };
 
         lecturerEmailColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setLecturerEmail(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         ArrayList<String> languageList = new ArrayList<String>();
             languageList.add("English");
             languageList.add("Chinese (Simplified)");
             languageList.add("Chinese (Tranditional)");
 
         Column<Student, String> languageColumn = new Column<Student, String>(new SelectionCell(languageList)) {
             @Override
             public String getValue(Student student) {
                 return student.getLanguage();
             }
         };
 
         languageColumn.setFieldUpdater(new FieldUpdater<Student, String>() {
             @Override
             public void update(int index, Student object, String value) {
                 object.setLanguage(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         Column<Student, Boolean> verifiedColumn = new Column<Student, Boolean>(new CheckboxCell(true, false)) {
             @Override
             public Boolean getValue(Student student) {
                 return student.getVerified();
             }
         };
 
         verifiedColumn.setFieldUpdater(new FieldUpdater<Student, Boolean>() {
             @Override
             public void update(int index, final Student object, Boolean value) {
                 object.setVerified(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if (!result) {
                             displayErrorBox("Failed", "Update has failed");
                         } else {
                             userService.assignTimeslotAndQuestions(object.getEmail(), new AsyncCallback<Void>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     caught.printStackTrace();
                                 }
 
                                 @Override
                                 public void onSuccess(Void result) {
                                     cellTable.redraw();
                                     setUserCount();
                                 }
                             });
                         }
                     }
                 });
 
             }
         });
 
         Column<Student, Boolean> statusColumn = new Column<Student, Boolean>(new CheckboxCell(true, false)) {
             @Override
             public Boolean getValue(Student student) {
                 return student.getStatus();
             }
         };
 
         statusColumn.setFieldUpdater(new FieldUpdater<Student, Boolean>() {
             @Override
             public void update(int index, Student object, Boolean value) {
                 object.setStatus(value);
                 userService.updateStudentData(object, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if(!result) {
                             displayErrorBox("Failed", "Update has failed");
                         }
 
                         else {
                             cellTable.redraw();
                         }
                     }
                 });
             }
         });
 
         cellTable.addColumn(selectColumn, selectAllHeader);
         cellTable.addColumn(emailColumn, "Email");
         cellTable.addColumn(firstNameColumn, "First Name");
         cellTable.addColumn(lastNameColumn, "Last Name");
         cellTable.addColumn(countryColumn, "Country");
         cellTable.addColumn(countryCodeColumn, "Country Code");
         cellTable.addColumn(contactColumn, "Contact");
         cellTable.addColumn(schoolColumn, "School");
         cellTable.addColumn(lecturerFirstNameColumn, "Lecturer's First Name");
         cellTable.addColumn(lecturerLastNameColumn, "Lecturer's Last Name");
         cellTable.addColumn(lecturerEmailColumn, "Lecturer's Email");
         cellTable.addColumn(languageColumn, "Language");
         cellTable.addColumn(verifiedColumn, "Verified");
         cellTable.addColumn(statusColumn, "Status");

        cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Student> createCheckboxManager(cellTable.getColumnIndex(selectColumn)));
     }
 
     @UiHandler("searchButton")
     public void handleSearchButtonClick(ClickEvent event) {
         String contains = searchField.getText();
         List<Student> list = new ArrayList<Student>();
 
         if(contains.equals("")) {
             provider.setList(studentList);
         }
 
         else {
             String category = searchTerms.getItemText(searchTerms.getSelectedIndex());
             if(category.equalsIgnoreCase("Email")) {
                 for(Student s : studentList) {
                     if(s.getEmail().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("First Name")) {
                 for(Student s : studentList) {
                     if(s.getFirstName().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Last Name")) {
                 for(Student s : studentList) {
                     if(s.getLastName().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Contact")) {
                 for(Student s : studentList) {
                     if(s.getContact().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Country")) {
                 for(Student s : studentList) {
                     if(s.getCountry().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Country Code")) {
                 for(Student s : studentList) {
                     if(s.getCountryCode().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("School")) {
                 for(Student s : studentList) {
                     if(s.getFirstName().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Lecturer's First Name")) {
                 for(Student s : studentList) {
                     if(s.getLecturerFirstName().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Lecturer's Last Name")) {
                 for(Student s : studentList) {
                     if(s.getLecturerLastName().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Lecturer's Email")) {
                 for(Student s : studentList) {
                     if(s.getLecturerEmail().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             else if(category.equalsIgnoreCase("Language")) {
                 for(Student s : studentList) {
                     if(s.getLanguage().contains(contains)) {
                         list.add(s);
                     }
                 }
             }
 
             provider.setList(list);
         }
     }
 
     @UiHandler("deleteButton")
     public void handleDeleteButtonClick(ClickEvent event) {
         userService.deleteStudents(listOfSelectedStudents, new AsyncCallback<Boolean>() {
             @Override
             public void onFailure(Throwable caught) {
                 caught.printStackTrace();
             }
 
             @Override
             public void onSuccess(Boolean result) {
                 if(!result) {
                     displayErrorBox("Error", "Error with deleting users");
                 }
 
                 else {
                     List<Student> toBeRemoved = new ArrayList<Student>();
                     for(Student s : studentList) {
                         if(listOfSelectedStudents.contains(s)) {
                             toBeRemoved.add(s);
                         }
                     }
                     studentList.removeAll(toBeRemoved);
                     provider.setList(studentList);
                     setUserCount();
                 }
             }
         });
 
     }
 
     @UiHandler("registerButton")
     public void handleRegisterButtonClick(ClickEvent event) {
         ContentContainer.INSTANCE.setContent(new RegisterScreen());
     }
 
     @UiHandler("exportButton")
     public void handleExportButtonClick(ClickEvent event) {
         /**
          * The following two lines is to avoid the issue
          * of .getList() returning a ListWrapper type
          * instead of a Serializable list type which
          * causes a SerializationException to be thrown.
          *
          * See: http://blog.rubiconred.com/2011/04/gwt-serializationexception-on-rpc-call.html
          */
         List<Student> list = new ArrayList<Student>();
         list.addAll(provider.getList());
 
         userService.exportCsv(list, new AsyncCallback<String>() {
             @Override
             public void onFailure(Throwable caught) {
                 caught.printStackTrace();
             }
 
             @Override
             public void onSuccess(String result) {
                 String url = GWT.getHostPageBaseURL() + "administration/download?file=" + result;
 
                 Frame downloadFrame = Frame.wrap(Document.get().getElementById("__gwt_downloadFrame"));
                 downloadFrame.setUrl(url);
             }
         });
     }
 
     @UiHandler("refreshButton")
     public void handleRefreshButtonClick(ClickEvent event) {
         ContentContainer.INSTANCE.setContent(new UserScreen());
     }
 
     private void setUserCount() {
 
         int count = 0;
 
         registrationLabel.setText("Total number of registered user: " + studentList.size());
         for (Student student : studentList) {
             if (student.getVerified()) {
                 count++;
             }
         }
 
         verifiedLabel.setText("Total number of verified user:  " + count);
     }
 
     private void displayErrorBox(String errorHeader, String message) {
         final DialogBox errorBox = new DialogBox();
         errorBox.setText(errorHeader);
         final HTML errorLabel = new HTML();
         errorLabel.setHTML(message);
         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
         final Button closeButton = new Button("Close");
         closeButton.setEnabled(true);
         closeButton.getElement().setId("close");
         closeButton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 errorBox.hide();
             }
         });
         verticalPanel.add(errorLabel);
         verticalPanel.add(closeButton);
         errorBox.setWidget(verticalPanel);
         errorBox.center();
     }
 }
