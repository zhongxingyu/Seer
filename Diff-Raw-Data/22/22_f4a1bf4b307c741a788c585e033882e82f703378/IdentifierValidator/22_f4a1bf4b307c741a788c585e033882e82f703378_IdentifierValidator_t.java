 /*
  * Copyright (C) 2003-2007 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.webui.form.validator;
 
 import org.exoplatform.web.application.ApplicationMessage;
 import org.exoplatform.webui.exception.MessageException;
 import org.exoplatform.webui.form.UIFormInput;
 
 /**
  * Created by The eXo Platform SARL
  * Author : Dang Van Minh
  *          minhdv81@yahoo.com
  * Jun 7, 2006
  * 
  * Validates whether the value is composed of letters, numbers or '_'
  */
 public class IdentifierValidator implements Validator {
     
   public void validate(UIFormInput uiInput) throws Exception {
     String s = (String)uiInput.getValue();
    if(s==null) return ;
     if(Character.isDigit(s.charAt(0))) {
       Object[] args = { uiInput.getName(), uiInput.getBindingField() };
       throw new MessageException(new ApplicationMessage("FirstCharacterNameValidator.msg", args, ApplicationMessage.WARNING)) ;
     }    
     for(int i = 0; i < s.length(); i ++){
       char c = s.charAt(i);
       if (Character.isLetter(c) || Character.isDigit(c) || c=='_' || c=='-'){
         continue;
       }
       Object[] args = { uiInput.getName(), uiInput.getBindingField() };
       throw new MessageException(new ApplicationMessage("IdentifierValidator.msg.Invalid-char", args)) ;
     }
   }
 }
