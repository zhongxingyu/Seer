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
 
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.service.UserService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.bind.ValidationContext;
 import org.zkoss.bind.validator.AbstractValidator;
 import org.zkoss.util.resource.Labels;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import java.util.Set;
 
 /**
  * ZK's validator of User's email. It checks email's uniqueness, length, and validity. It wasn't possible to set up a
  * Bean Validator to check mail for pattern because we had to check for uniqueness and there is no way you can pass
  * several validators to ZK via {@code @validator()}.
  *
  * @author Nickolay Polyarniy
  */
 public class EmailValidator extends AbstractValidator {
     private static final String DUPLICATED_MAIL_MESSAGE = "err.users.edit.dublicate_email";
    private final Logger logger = LoggerFactory.getLogger(getClass());
     private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
     private final UserService userService;
 
     /**
      * @param userService to have access to database and check whether a mail already exists in DB
      */
     public EmailValidator(UserService userService) {
         this.userService = userService;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate(ValidationContext validationContext) {
         String email = (String) validationContext.getProperty().getValue();
         PoulpeUser user = (PoulpeUser) validationContext.getBindContext().getValidatorArg("user");
         String oldEmail = user.getEmail();
         user.setEmail(email);
         if (!beanValidationFails(validationContext, user)) {
             user.setEmail(oldEmail);
             checkForUniqueness(validationContext, email, user);
         }
     }
 
     private void checkForUniqueness(ValidationContext validationContext, String email, PoulpeUser user) {
         PoulpeUser userWithSuchEmail = userService.getByEmail(email);
         if (userWithSuchEmail != null && userWithSuchEmail.getId() != user.getId()) {
             //the second part of "if" is in case user email was A, than in edit_user it was
             //changed to B and than to A again
             addInvalidMessage(validationContext, Labels.getLabel(DUPLICATED_MAIL_MESSAGE));
         }
     }
 
     private boolean beanValidationFails(ValidationContext validationContext, PoulpeUser user) {
         Set<ConstraintViolation<PoulpeUser>> set = validator.validateProperty(user, "email");
         if (!set.isEmpty()) {
             addInvalidMessage(validationContext, set.iterator().next().getMessage());
             return true;
         }
         return false;
     }
 
 }
