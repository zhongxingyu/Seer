 /*******************************************************************************
  *  Copyright 2013 Jason Sipula                                                *
  *                                                                             *
  *  Licensed under the Apache License, Version 2.0 (the "License");            *
  *  you may not use this file except in compliance with the License.           *
  *  You may obtain a copy of the License at                                    *
  *                                                                             *
  *      http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                             *
  *  Unless required by applicable law or agreed to in writing, software        *
  *  distributed under the License is distributed on an "AS IS" BASIS,          *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
  *  See the License for the specific language governing permissions and        *
  *  limitations under the License.                                             *
  *******************************************************************************/
 
 package com.vanomaly.superd.view;
 
 import javafx.geometry.Pos;
 import javafx.scene.control.OverrunStyle;
 import javafx.scene.control.TableCell;
 
 import com.vanomaly.superd.core.SimpleFileProperty;
 
 /**
  * @author Jason Sipula
  *
  */
 public class BaselineLeftStringCenterOverrunTableCell extends TableCell<SimpleFileProperty, String> {
     public BaselineLeftStringCenterOverrunTableCell() {
         this(null);
         this.setStyle("-fx-padding: 5px");
         this.setAlignment(Pos.BASELINE_LEFT);
     }
 
     public BaselineLeftStringCenterOverrunTableCell(String ellipsisString) {
         super();
        setTextOverrun(OverrunStyle.CENTER_WORD_ELLIPSIS);
         if (ellipsisString != null && !"".equals(ellipsisString)) {
             setEllipsisString(ellipsisString);
         }  
     }
 
     @Override 
     protected void updateItem(String item, boolean empty) {
         super.updateItem(item, empty);
         setText(item == null ? "" : item);
     }
 }
