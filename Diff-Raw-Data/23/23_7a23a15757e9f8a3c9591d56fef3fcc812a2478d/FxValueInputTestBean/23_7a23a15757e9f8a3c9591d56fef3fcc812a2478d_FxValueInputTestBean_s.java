 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.test;
 
 import com.flexive.shared.CacheAdmin;
 import static com.flexive.shared.FxLanguage.ENGLISH;
 import static com.flexive.shared.FxLanguage.GERMAN;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.value.*;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Miscellanous test methods for the fxValueInput test page (test_fxvalueinput.xhtml).
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxValueInputTestBean {
     /**
      * ui:repeat update workaround
      */
     public static class ValueHolder {
         private FxValue value;
 
         public ValueHolder(FxValue value) {
             this.value = value;
         }
 
         public FxValue getValue() {
             return value;
         }
 
         public void setValue(FxValue value) {
             this.value = value;
         }
     }
 
     private List<ValueHolder> basicValues;
     private List<FxValue> basicValues2;
 
     public void submit() {
         // TODO: evaluate input values
     }
 
     public List<ValueHolder> getBasicValues() {
         if (basicValues == null) {
             final List<FxValue> values = createFxValues();
             basicValues = new ArrayList<ValueHolder>(values.size());
             for (FxValue value : values) {
                 basicValues.add(new ValueHolder(value));
             }
         }
         return basicValues;
     }
 
     public List<FxValue> getBasicValues2() {
         if (basicValues2 == null) {
             basicValues2 = createFxValues();
         }
         return basicValues2;
     }
 
     @SuppressWarnings({"RedundantArrayCreation"})
     private List<FxValue> createFxValues() {
         final FxSelectList countriesList = CacheAdmin.getEnvironment().getSelectList(FxSelectList.COUNTRIES);
         final List<FxSelectListItem> countries = countriesList.getItems();
         return Arrays.asList(new FxValue[]{
                 new FxString(false, "string singlelanguage"),
                 new FxString(ENGLISH, "english").setTranslation(GERMAN, "deutsch"),
                 new FxNumber(false, 21),
                 new FxNumber(ENGLISH, 1).setTranslation(GERMAN, 2),
                 new FxLargeNumber(false, 22L),
                 new FxLargeNumber(ENGLISH, 3L).setTranslation(GERMAN, 4L),
                 new FxFloat(false, 3.14f),
                 new FxFloat(ENGLISH, 3.14f).setTranslation(GERMAN, 3.15f),
                 new FxDouble(false, Math.PI),
                 new FxDouble(ENGLISH, Math.PI).setTranslation(GERMAN, Math.PI - 0.1),
                 new FxDate(false, new Date()),
                 new FxDate(ENGLISH, new Date()).setTranslation(GERMAN, new Date(new Date().getTime() - 24*3600*1000)),
                 new FxHTML(false, "HTML value"),
                 new FxHTML(ENGLISH, "english HTML").setTranslation(GERMAN, "deutsches HTML"),
                 new FxSelectOne(false, countries.get(0)),
                 new FxSelectOne(ENGLISH, countries.get(1)).setTranslation(GERMAN, countries.get(2)),
                 new FxSelectMany(false, new SelectMany(countriesList)),
                 new FxSelectMany(ENGLISH, new SelectMany(countriesList)).setTranslation(GERMAN, new SelectMany(countriesList)),
         });
     }
 }
