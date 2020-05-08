 package ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.components.info;
 
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.layout.FlowData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Image;
 
 /**
  * User: Khurtin Denis (KhurtinDN@gmail.com)
  * Date: 2/3/11
  * Time: 6:02 PM
  */
 public class FacultyInfoLayoutContainer extends LayoutContainer {
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading("Инфомация о факультете");
         FormLayout formLayout = new FormLayout();
         formLayout.setLabelWidth(140);
         fieldSet.setLayout(formLayout);
 
         Image image = new Image("images/12_building.jpg");
         image.setWidth("100%");
 
         LabelField nameLabelField = new LabelField("КНиИТ");
         nameLabelField.setName("name");
         nameLabelField.setFieldLabel("Имя:");
         nameLabelField.setLabelStyle("font-weight: bold");
         nameLabelField.setAutoWidth(true);
 
         fieldSet.add(image);
         fieldSet.add(nameLabelField);
 
         add(fieldSet, new FlowData(5));
     }
 }
