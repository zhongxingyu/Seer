 package com.amee.platform.resource.profileitem.v_3_6;
 
 import com.amee.base.domain.Since;
 import com.amee.domain.ProfileItemService;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.item.profile.ProfileItem;
 import com.amee.platform.resource.ResourceService;
 import com.amee.platform.resource.profileitem.ProfileItemResource;
 import com.amee.platform.science.Note;
 import com.amee.platform.science.ReturnValue;
 import com.amee.platform.science.ReturnValues;
 import com.amee.platform.science.StartEndDate;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import java.util.Map;
 import java.util.TimeZone;
 
 @Service
 @Scope("prototype")
 @Since("3.6.0")
 public class ProfileItemDOMRenderer_3_6_0 implements ProfileItemResource.Renderer {
 
     @Autowired
     ProfileItemService profileItemService;
 
     @Autowired
     ResourceService resourceService;
 
     protected ProfileItem profileItem;
     protected Element rootElem;
     protected Element profileItemElem;
     protected Element amountsElem;
     protected Element notesElem;
 
     @Override
     public void start() {
         rootElem = new Element("Representation");
     }
 
     @Override
     public void ok() {
         rootElem.addContent(new Element("Status").setText("OK"));
     }
 
     @Override
     public void newProfileItem(ProfileItem profileItem) {
         this.profileItem = profileItem;
         profileItemElem = new Element("Item");
         if (rootElem != null) {
             rootElem.addContent(profileItemElem);
         }
     }
 
     @Override
     public void addBasic() {
         profileItemElem.setAttribute("uid", profileItem.getUid());
     }
 
     @Override
     public void addAudit() {
         profileItemElem.setAttribute("status", profileItem.getStatus().getName());
         profileItemElem.setAttribute("created", DATE_FORMAT.print(profileItem.getCreated().getTime()));
         profileItemElem.setAttribute("modified", DATE_FORMAT.print(profileItem.getModified().getTime()));
     }
 
     @Override
 	public void addNote() {
 		profileItemElem.addContent(new Element("Note").setText(profileItem.getNote()));
 	}
 
 	@Override
     public void addName() {
         profileItemElem.addContent(new Element("Name").setText(profileItem.getName()));
     }
 
     @Override
     public void addDates(TimeZone timeZone) {
         String startDate = StartEndDate.getLocalStartEndDate(profileItem.getStartDate(), timeZone).toString();
         profileItemElem.addContent(new Element("StartDate").setText(startDate));
         if (profileItem.getEndDate() != null) {
             String endDate = StartEndDate.getLocalStartEndDate(profileItem.getEndDate(), timeZone).toString();
             profileItemElem.addContent(new Element("EndDate").setText(endDate));
         } else {
             profileItemElem.addContent(new Element("EndDate"));
         }
     }
 
     @Override
     public void addCategory() {
         DataCategory dataCategory = profileItem.getDataItem().getDataCategory();
         profileItemElem.addContent(new Element("CategoryUid").setText(dataCategory.getUid()));
         profileItemElem.addContent(new Element("CategoryWikiName").setText(dataCategory.getWikiName()));
     }
 
     @Override
     public void addReturnValues(ReturnValues returnValues) {
 
         // Create the Amounts element.
         amountsElem = new Element("Amounts");
 
         // Add the return values to it.
         for (Map.Entry<String, ReturnValue> entry : returnValues.getReturnValues().entrySet()) {
             Element amountElem = new Element("Amount");
             amountElem.setAttribute("type", entry.getKey());
 
             ReturnValue returnValue = entry.getValue();
 
             // If there was a problem in the calculation, returnValue may be null. (PL-11105)
             if (returnValue != null) {
                 amountElem.setAttribute("unit", returnValue.getUnit());
                 amountElem.setAttribute("perUnit", returnValue.getPerUnit());
                 amountElem.setText(Double.toString(returnValue.getValue()));
             } else {
 
                 // Deal with an 'empty' value
                 amountElem.setAttribute("unit", "");
                 amountElem.setAttribute("perUnit", "");
                 // Set no value.
             }
 
             if (entry.getKey().equals(returnValues.getDefaultType())) {
                 amountElem.setAttribute("default", "true");
             }
             amountsElem.addContent(amountElem);
         }
 
         // TODO: check this. Will the returnvalues ever be empty?
         if (amountsElem.getChildren().size() > 0) {
             profileItemElem.addContent(amountsElem);
         }
 
         // Create the Notes element.
         notesElem = new Element("Notes");
 
         // Add the notes to it.
         for (Note note : returnValues.getNotes()) {
             Element noteElem = new Element("Note");
             noteElem.setAttribute("type", note.getType());
             noteElem.setText(note.getValue());
             notesElem.addContent(noteElem);
         }
 
         // TODO: This should be part of the returnValues element (Amounts) not at the same level.
         if (notesElem.getChildren().size() > 0) {
             amountsElem.addContent(notesElem);
         }
     }
 
     public String getMediaType() {
         return "application/xml";
     }
 
     public Object getObject() {
         return new Document(rootElem);
     }
 }
