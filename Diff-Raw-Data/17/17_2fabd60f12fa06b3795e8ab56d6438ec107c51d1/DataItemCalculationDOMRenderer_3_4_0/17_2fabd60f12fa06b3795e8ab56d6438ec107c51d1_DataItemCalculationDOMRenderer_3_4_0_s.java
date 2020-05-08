 package com.amee.platform.resource.dataitem.v_3_4;
 
 import com.amee.base.domain.Since;
 import com.amee.domain.data.ItemValueDefinition;
 import com.amee.domain.sheet.Choice;
 import com.amee.domain.sheet.Choices;
 import com.amee.platform.resource.dataitem.v_3_6.DataItemCalculationDOMRenderer_3_6_0;
 import com.amee.platform.science.Note;
 import com.amee.platform.science.ReturnValue;
 import com.amee.platform.science.ReturnValues;
 import org.jdom.Element;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import java.util.Map;
 
 @Service
 @Scope("prototype")
 @Since("3.4.0")
 public class DataItemCalculationDOMRenderer_3_4_0 extends DataItemCalculationDOMRenderer_3_6_0 {
 
     /**
      * ReturnValues < 3.6.0 were not wrapped in an Output element.
      *
      * @param returnValues
      */
     @Override
     public void addReturnValues(ReturnValues returnValues) {
         Element returnValuesElem = new Element("Amounts");
 
         // Add the return values
         for (Map.Entry<String, ReturnValue> entry : returnValues.getReturnValues().entrySet()) {
             String type = entry.getKey();
             ReturnValue value = entry.getValue();
 
             Element amountElem = new Element("Amount");
             amountElem.setAttribute("type", type);
             amountElem.setAttribute("unit", value != null ? value.getUnit() : "");
             amountElem.setAttribute("perUnit", value != null ? value.getPerUnit() : "");
             if (type.equals(returnValues.getDefaultType())) {
                 amountElem.setAttribute("default", "true");
             }
             amountElem.setText(value != null ? value.getValue() + "" : "");
             returnValuesElem.addContent(amountElem);
         }
         if (returnValuesElem.getChildren().size() > 0) {
             rootElem.addContent(returnValuesElem);
         }
 
         Element notesElem = new Element("Notes");
 
         // Add the notes
         for (Note note : returnValues.getNotes()) {
             Element noteElem = new Element("Note");
             noteElem.setAttribute("type", note.getType());
             noteElem.setText(note.getValue());
             notesElem.addContent(noteElem);
         }
         if (notesElem.getChildren().size() > 0) {
             rootElem.addContent(notesElem);
         }
     }
 
     @Override
    public void addValues(Choices userValues) {
 
         // Add the supplied values
         Element valuesElem = new Element("Values");
         Map<String, ItemValueDefinition> itemValueDefinitions = dataItem.getItemDefinition().getItemValueDefinitionsMap();
         for (Choice choice : userValues.getChoices()) {
             if (!choice.getName().startsWith("units.") && !choice.getName().startsWith("perUnits.")) {
                 Element valueElem = new Element("Value");
                 valueElem.setAttribute("name", choice.getName());
                 valueElem.setText(choice.getValue());
 
                 // Add details from the ItemValueDefinition.
                 ItemValueDefinition itemValueDefinition = itemValueDefinitions.get(choice.getName());
                 if (itemValueDefinition != null) {
                     if (itemValueDefinition.hasUnit()) {
                         if (userValues.containsKey("units." + choice.getName())) {
                             valueElem.setAttribute("unit", userValues.get("units." + choice.getName()).getValue());
                         } else {
                             valueElem.setAttribute("unit", itemValueDefinition.getUnit().toString());
                         }
                     }
                     if (itemValueDefinition.hasPerUnit()) {
                         if (userValues.containsKey("perUnits." + choice.getName())) {
                             valueElem.setAttribute("perUnit", userValues.get("perUnits." + choice.getName()).getValue());
                         } else {
                             valueElem.setAttribute("perUnit", itemValueDefinition.getPerUnit().toString());
                         }
                     }
                 }
                 valuesElem.addContent(valueElem);
             }
         }
 
         // Only add the element if we have values.
         if (valuesElem.getChildren().size() > 0) {
             rootElem.addContent(valuesElem);
         }
     }
 }
