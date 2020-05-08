 package com.amee.platform.resource.profileitem.v_3_6;
 
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.ResponseHelper;
 import com.amee.domain.ProfileItemService;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.item.profile.ProfileItem;
 import com.amee.platform.resource.ResourceService;
 import com.amee.platform.resource.profileitem.ProfileItemResource;
 import com.amee.platform.science.Note;
 import com.amee.platform.science.ReturnValue;
 import com.amee.platform.science.ReturnValues;
 import com.amee.platform.science.StartEndDate;
 
 @Service
 @Scope("prototype")
 @Since("3.6.0")
 public class ProfileItemJSONRenderer_3_6_0 implements ProfileItemResource.Renderer {
 
     @Autowired
     ProfileItemService profileItemService;
 
     @Autowired
     ResourceService resourceService;
 
     protected ProfileItem profileItem;
     protected JSONObject rootObj;
     protected JSONObject profileItemObj;
     protected JSONObject amountsObj;
 
     @Override
     public void start() {
         rootObj = new JSONObject();
     }
 
     @Override
     public void ok() {
         ResponseHelper.put(rootObj, "status", "OK");
     }
 
     @Override
     public void newProfileItem(ProfileItem profileItem) {
         this.profileItem = profileItem;
         profileItemObj = new JSONObject();
         if (rootObj != null) {
             ResponseHelper.put(rootObj, "item", profileItemObj);
         }
     }
 
     @Override
     public void addBasic() {
         ResponseHelper.put(profileItemObj, "uid", profileItem.getUid());
     }
 
     @Override
     public void addAudit() {
         ResponseHelper.put(profileItemObj, "status", profileItem.getStatus().getName());
         ResponseHelper.put(profileItemObj, "created", DATE_FORMAT.print(profileItem.getCreated().getTime()));
         ResponseHelper.put(profileItemObj, "modified", DATE_FORMAT.print(profileItem.getModified().getTime()));
     }
 
     @Override
 	public void addNote() {
 		ResponseHelper.put(profileItemObj, "note", profileItem.getNote());
 	}
 
 	@Override
     public void addName() {
         ResponseHelper.put(profileItemObj, "name", profileItem.getName());
     }
 
     @Override
     public void addDates(TimeZone timeZone) {
         String startDate = StartEndDate.getLocalStartEndDate(profileItem.getStartDate(), timeZone).toString();
         ResponseHelper.put(profileItemObj, "startDate", startDate);
         String endDate = "";
         if (profileItem.getEndDate() != null) {
             endDate = StartEndDate.getLocalStartEndDate(profileItem.getEndDate(), timeZone).toString();
         }
         ResponseHelper.put(profileItemObj, "endDate", endDate);
     }
 
     @Override
     public void addCategory() {
         DataCategory dataCategory = profileItem.getDataItem().getDataCategory();
         ResponseHelper.put(profileItemObj, "categoryUid", dataCategory.getUid());
         ResponseHelper.put(profileItemObj, "categoryWikiName", dataCategory.getWikiName());
     }
 
     /**
      * Amounts object contains an array of Amount objects and and array of Note objects.
      * 
      * @param returnValues
      */
     @Override
     public void addReturnValues(ReturnValues returnValues) {
         amountsObj = new JSONObject();
 
         // Create an array of amount objects
         JSONArray amountArr = new JSONArray();
         ResponseHelper.put(amountsObj, "amount", amountArr);
 
         // Add the return values
         for (Map.Entry<String, ReturnValue> entry : returnValues.getReturnValues().entrySet()) {
 
             // Create an Amount object
             JSONObject amountObj = new JSONObject();
             double returnValue = entry.getValue().getValue();
 
             if (Double.isInfinite(returnValue)) {
                 ResponseHelper.put(amountObj, "value", "Infinity");
             } else if (Double.isNaN(returnValue)) {
                 ResponseHelper.put(amountObj, "value", "NaN");
             } else {
                 ResponseHelper.put(amountObj, "value", returnValue);
             }
             ResponseHelper.put(amountObj, "type", entry.getKey());
             ResponseHelper.put(amountObj, "unit", entry.getValue().getUnit());
             ResponseHelper.put(amountObj, "perUnit", entry.getValue().getPerUnit());
             if (entry.getKey().equals(returnValues.getDefaultType())) {
                 ResponseHelper.put(amountObj, "default", true);
             }
 
             // Add the object to the amounts array
             amountArr.put(amountObj);
         }
 
         // Only add the amounts if we have some.
         // TODO: confirm this is correct behaviour? I think we should add it anyway. Also see DOMRenderer.
         if (amountArr.length() > 0) {
             ResponseHelper.put(profileItemObj, "amounts", amountsObj);
         }
 
         JSONArray notesArr = new JSONArray();
 
         // Add the notes
         for (Note note : returnValues.getNotes()) {
             JSONObject noteObj = new JSONObject();
             ResponseHelper.put(noteObj, "type", note.getType());
             ResponseHelper.put(noteObj, "value", note.getValue());
 
             // Add the note to the notes array.
             notesArr.put(noteObj);
         }
 
         // TODO: Check this condition
         if (notesArr.length() > 0) {
             ResponseHelper.put(amountsObj, "notes", notesArr);
         }
     }
 
     @Override
     public String getMediaType() {
         return "application/json";
     }
 
     @Override
     public Object getObject() {
         return rootObj;
     }
    
    @Override
    public void addNote() {
        ResponseHelper.put(profileItemObj, "note", profileItem.getNote());
    }
 }
