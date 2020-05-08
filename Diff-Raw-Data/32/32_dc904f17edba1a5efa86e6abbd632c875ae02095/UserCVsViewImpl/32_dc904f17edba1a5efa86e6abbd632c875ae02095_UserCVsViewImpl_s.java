 package com.clouway.jobex.client.cv;
 
 
 import com.clouway.jobex.client.navigation.NavigationMenu;
 import com.clouway.jobex.client.security.UserCredentialsLocalStorage;
 import com.clouway.jobex.shared.CVProxy;
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.ButtonCell;
 import com.github.gwtbootstrap.client.ui.CellTable;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 
 import java.util.List;
 
 /**
  * @author Adelin Ghanayem adelin.ghanaem@clouway.com
  */
 public class UserCVsViewImpl extends Composite implements UserCVsView {
 
 
   interface JobApplicationViewImplUiBinder extends UiBinder<HTMLPanel, UserCVsViewImpl> {
   }
 
   private static JobApplicationViewImplUiBinder ourUiBinder = GWT.create(JobApplicationViewImplUiBinder.class);
 
   @UiField(provided = true)
   CellTable<CVProxy> cVCellTable;
 
   @UiField
   Label errors;
 
   @UiField
   Label messages;
   @UiField
   Button createCv;
 
   @Inject
   PlaceController controller;
 
   private UserCVsPresenter presenter;
 
   private Long jobId;
 
   private SingleSelectionModel<CVProxy> selectionModel = new SingleSelectionModel<CVProxy>();
 
   Column<CVProxy, String> selectButton = new Column<CVProxy, String>(new ButtonCell()) {
     @Override
     public String getValue(final CVProxy object) {
       return "select";
     }
   };
 
   @Inject
   UserCredentialsLocalStorage provider;
 
   @Inject
   public UserCVsViewImpl(NavigationMenu menu) {
 
     cVCellTable = new CellTable<CVProxy>();
 
     cVCellTable.addColumn(new TextColumn<CVProxy>() {
       @Override
       public String getValue(CVProxy object) {
         return String.valueOf(object.getId());
       }
     }, "Id");
 
     cVCellTable.addColumn(new TextColumn<CVProxy>() {
       @Override
       public String getValue(CVProxy object) {
         return object.getName();
       }
     }, "name");
 
 
     cVCellTable.addColumn(new TextColumn<CVProxy>() {
       @Override
       public String getValue(CVProxy object) {
         return object.getGender();
       }
     }, "gender");
 
 
     cVCellTable.addColumn(new TextColumn<CVProxy>() {
       @Override
       public String getValue(CVProxy object) {
         return String.valueOf(DateTimeFormat.getFormat("yyyy/MM/dd").format(object.getDateOfBirth()));
       }
     }, "Date of birth");
 
     Column<CVProxy, String> editButton = new Column<CVProxy, String>(new ButtonCell()) {
       @Override
       public String getValue(CVProxy object) {
         return "edit";
       }
     };
 
     cVCellTable.addColumn(new TextColumn<CVProxy>() {
       @Override
       public String getValue(CVProxy object) {
         return object.getPhoneNumber();
       }
     }, "phone number");
 
     cVCellTable.addColumn(new TextColumn<CVProxy>() {
       @Override
       public String getValue(CVProxy object) {
         return object.getSkills();
       }
     }, "skills:");
 
     Column<CVProxy, String> delete = new Column<CVProxy, String>(new ButtonCell()) {
       @Override
       public String getValue(CVProxy object) {
         return "X";
       }
     };
 
     delete.setFieldUpdater(new FieldUpdater<CVProxy, String>() {
       @Override
       public void update(int index, CVProxy object, String value) {
         presenter.deleteCv(object.getId());
       }
     });
 
     editButton.setFieldUpdater(new FieldUpdater<CVProxy, String>() {
       @Override
       public void update(int index, CVProxy object, String value) {
 
         controller.goTo(new EditCVPlace(object.getId()));
       }
     });
 
     cVCellTable.addColumn(editButton);
 
     cVCellTable.addColumn(delete);
 
     cVCellTable.setSelectionModel(selectionModel);
 
     HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
 
     initWidget(rootElement);
   }
 
 
   @Override
   public void notifyUserOfSuccessfulAppliance() {
     Window.alert("You have successfully applied for A Job. ");
   }
 
 
   @Override
   public void showErrors(List<String> errorsList) {
     StringBuilder builder = new StringBuilder();
     for (String string : errorsList) {
       builder.append(string);
     }
     errors.setText(builder.toString());
   }
 
   @Override
   public void notifyUserOfCommunicationError() {
     errors.setText("System error occurred while trying to apply job !");
   }
 
 
   public void setPresenter(UserCVsPresenter presenter) {
     this.presenter = presenter;
   }
 
   @Override
   public void showCVs(List<CVProxy> cvs) {
 
     cVCellTable.setVisibleRange(0, cvs.size());
 
     cVCellTable.setRowData(cvs);
 
   }
 
 
   @Override
   public void goToCreateNewCVForm() {
     controller.goTo(new CreateCvPlace());
   }
 
   @Override
   public void setJobId(Long id) {
     jobId = id;
     addSelectButton();
   }
 
   @Override
   public void delete(int cvIndex) {
   }
 
 
   public void addSelectButton() {
 
     selectButton.setFieldUpdater(new FieldUpdater<CVProxy, String>() {
       @Override
       public void update(int index, CVProxy cvProxy, String value) {
         presenter.applyForJob(jobId, cvProxy.getId(), provider.getUsername());
       }
     });
 
 
     int index = cVCellTable.getColumnIndex(selectButton);
 
     if (index == -1) {
       cVCellTable.addColumn(selectButton);
     }
 
   }
 
   @UiHandler("createCv")
   public void onCreateNewCv(ClickEvent event) {
     controller.goTo(new CreateCvPlace());
   }
 }
