 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.mediation.task;
 
 import java.util.Vector;
 
 public class SeparatorFileReader extends AbstractFileReader {
     
     private String fieldSeparator;
 
     public SeparatorFileReader() {
     }
     
     @Override
     public boolean validate(Vector<String> messages) {
         boolean retValue = super.validate(messages); 
         
         // optionals
         fieldSeparator = ((String) parameters.get("separator") == null) 
                 ? "," : (String) parameters.get("separator");
        
         return retValue;
     }
     
     @Override
     protected String[] splitFields(String line) {
        return line.split(fieldSeparator);
     }
 }
