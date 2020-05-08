 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.validator;
 
 import org.jtalks.common.service.exceptions.NotFoundException;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.service.UserService;
 import org.zkoss.bind.ValidationContext;
 import org.zkoss.bind.validator.AbstractValidator;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.WrongValueException;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import java.util.Set;
 
 /**
  * ZK's validator of User's email.
  * It checks email's uniqueness, length, and validity.
  *
  * @author Nickolay Polyarniy
  */
 public class EmailValidator extends AbstractValidator {
 
     private final UserService userService;
     private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
     private final Validator validator = factory.getValidator();
 
     public EmailValidator(UserService userService) {
         this.userService = userService;
     }
 
     @Override
     public void validate(ValidationContext validationContext) throws WrongValueException {
         String email = (String) validationContext.getProperty().getValue();
         PoulpeUser user = (PoulpeUser) validationContext.getBindContext().getValidatorArg("user");
        user.setEmail(email);
 
         //validate by pattern and length
         Set<ConstraintViolation<PoulpeUser>> set = validator.validateProperty(user, "email");
         if (!set.isEmpty()) {
             addInvalidMessage(validationContext, set.iterator().next().getMessage());
             return;
         }
 
         //uniqueness validation
         if (userService.isEmailAlreadyUsed(email)) {
             try {
                 if (!userService.getByEmail(email).equals(user)) {
                     //this "if" is in case user email was A, than in edit_user it was changed to B and than to A again
                     addInvalidMessage(validationContext, Labels.getLabel("err.users.edit.dublicate_email"));
                 }
             } catch (NotFoundException e) {
                 //it should not happend. Because email already used(see root if).
                 e.printStackTrace();
             }
         }
     }
 
 }
