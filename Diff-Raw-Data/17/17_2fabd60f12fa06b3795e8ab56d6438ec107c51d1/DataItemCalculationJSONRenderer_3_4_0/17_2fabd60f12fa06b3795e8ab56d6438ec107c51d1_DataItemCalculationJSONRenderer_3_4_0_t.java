 package com.amee.platform.resource.dataitem.v_3_4;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.ResponseHelper;
 import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.item.BaseItemValue;
 import com.amee.domain.sheet.Choice;
 import com.amee.domain.sheet.Choices;
 import com.amee.platform.resource.dataitem.v_3_6.DataItemCalculationJSONRenderer_3_6_0;
 import com.amee.platform.science.Note;
 import com.amee.platform.science.ReturnValue;
 import com.amee.platform.science.ReturnValues;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
import java.util.List;
 import java.util.Map;
 
 @Service
 @Scope("prototype")
 @Since("3.4.0")
 public class DataItemCalculationJSONRenderer_3_4_0 extends DataItemCalculationJSONRenderer_3_6_0 {
 
     /**
      * v3.4.0 did not handle non-finite numbers in json.
      * 
      * @param returnValues
      */
     @Override
     public void addReturnValues(ReturnValues returnValues) {
 
         // Create an array of multiple amount objects.
         JSONArray multipleAmountsArr = new JSONArray();
         for (Map.Entry<String, ReturnValue> entry : returnValues.getReturnValues().entrySet()) {
 
             // Create a multiple amount object.
             JSONObject multipleAmountObj = new JSONObject();
 
             String type = entry.getKey();
             ReturnValue returnValue = entry.getValue();
 
             ResponseHelper.put(multipleAmountObj, "type", type);
             ResponseHelper.put(multipleAmountObj, "unit", returnValue != null ? returnValue.getUnit() : "");
             ResponseHelper.put(multipleAmountObj, "perUnit", returnValue != null ? returnValue.getPerUnit() : "");
 
             // Flag for default type.
             ResponseHelper.put(multipleAmountObj, "default", type.equals(returnValues.getDefaultType()));
 
             if (returnValue == null) {
                 ResponseHelper.put(multipleAmountObj, "value", JSONObject.NULL);
             } else if (Double.isInfinite(returnValue.getValue())) {
                 ResponseHelper.put(multipleAmountObj, "value", "Infinity");
             } else if (Double.isNaN(returnValue.getValue())) {
                 ResponseHelper.put(multipleAmountObj, "value", "NaN");
             } else {
                 ResponseHelper.put(multipleAmountObj, "value", returnValue.getValue());
             }
             
             // Add the object to the amounts array
             multipleAmountsArr.put(multipleAmountObj);
         }
 
         // Add the multiple amounts to the result object, if there are some.
         if (multipleAmountsArr.length() > 0) {
             ResponseHelper.put(rootObj, "amounts", multipleAmountsArr);
         }
 
         // Create an array of note objects.
         JSONArray notesArr = new JSONArray();
         for (Note note : returnValues.getNotes()) {
             // Create the note object.
             JSONObject noteObj = new JSONObject();
             ResponseHelper.put(noteObj, "type", note.getType());
             ResponseHelper.put(noteObj, "value", note.getValue());
             // Add the note object to the notes array
             notesArr.put(noteObj);
         }
 
         // Add the notes array to the the result object, if there are some.
         if (notesArr.length() > 0) {
             ResponseHelper.put(rootObj, "notes", notesArr);
         }
     }
 
     @Override
    public void addValues(Choices userValues, Map<String, List<BaseItemValue>> dataItemValues) {
 
         // Create an array of value objects.
         JSONArray valuesArr = new JSONArray();
         Map<String, ItemValueDefinition> itemValueDefinitions = dataItem.getItemDefinition().getItemValueDefinitionsMap();
         for (Choice choice : userValues.getChoices()) {
             if (!choice.getName().startsWith("units.") && !choice.getName().startsWith("perUnits.")) {
 
                 // Create a value object.
                 JSONObject valueObj = new JSONObject();
                 ResponseHelper.put(valueObj, "name", choice.getName());
                 ResponseHelper.put(valueObj, "value", choice.getValue());
 
                 // Add details from the ItemValueDefinition.
                 ItemValueDefinition itemValueDefinition = itemValueDefinitions.get(choice.getName());
                 if (itemValueDefinition != null) {
                     if (itemValueDefinition.hasUnit()) {
                         if (userValues.containsKey("units." + choice.getName())) {
                             ResponseHelper.put(valueObj, "unit", userValues.get("units." + choice.getName()).getValue());
                         } else {
                             ResponseHelper.put(valueObj, "unit", itemValueDefinition.getUnit());
                         }
                         if (itemValueDefinition.hasPerUnit()) {
                             if (userValues.containsKey("perUnits." + choice.getName())) {
                                 ResponseHelper.put(valueObj, "perUnit", userValues.get("perUnits." + choice.getName()).getValue());
                             } else {
                                 ResponseHelper.put(valueObj, "perUnit", itemValueDefinition.getPerUnit());
                             }
                         }
                     }
                 }
 
                 // Add the object to the values array
                 valuesArr.put(valueObj);
             }
         }
 
         // Add the values array to the input object, if there are some.
         if (valuesArr.length() > 0) {
             ResponseHelper.put(rootObj, "values", valuesArr);
         }
     }
 
 }
