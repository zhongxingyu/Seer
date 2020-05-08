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
 
 import org.apache.commons.lang3.RandomStringUtils;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.service.UserService;
 import org.jtalks.poulpe.test.fixtures.TestFixtures;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import org.zkoss.bind.BindContext;
 import org.zkoss.bind.Property;
 import org.zkoss.bind.ValidationContext;
 import org.zkoss.bind.impl.BinderImpl;
 import org.zkoss.bind.impl.PropertyImpl;
 
 import static org.mockito.Mockito.*;
 
 /**
  * @author Nickolay Polyarniy
  */
 public class EmailValidatorTest {
     EmailValidator validator;
 
     @Mock
     UserService userService;
     @Mock
     ValidationContext context;
 
     @BeforeMethod
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         validator = new EmailValidator(userService);
     }
 
     private void givenBindContextReturnsMailAndUserId(String email, long userId) {
         BinderImpl binder = mock(BinderImpl.class);
         BindContext bindContext = mock(BindContext.class);
         Property property = new PropertyImpl("", "email", email);
 
         when(context.getProperty()).thenReturn(property);
         when(context.getBindContext()).thenReturn(bindContext);
         when(bindContext.getValidatorArg("user")).thenReturn(PoulpeUser.withId(userId));
         when(bindContext.getBinder()).thenReturn(binder);
     }
 
     @Test
     public void tooLongMailShouldFailValidation() {
         givenBindContextReturnsMailAndUserId(RandomStringUtils.randomAlphanumeric(255) + "@rambler.ru", 1);
         validator.validate(context);
         verify(context).setInvalid();
     }
 
     @Test
     public void wrongEmailPatternShouldFailValidation() {
         givenBindContextReturnsMailAndUserId("asdasd", 1);
         validator.validate(context);
         verify(context).setInvalid();
     }
 
     @Test
     public void duplicatedMailShouldFailValidation() throws Exception {
         PoulpeUser user1 = PoulpeUser.withId(1);
         PoulpeUser user2 = PoulpeUser.withId(2);
         //user2 wants to save email which already used by user1
         givenBindContextReturnsMailAndUserId(user1.getEmail(), user2.getId());
         storeUsersInMockedDb(user1, user2);
         validator.validate(context);
         verify(context).setInvalid();
     }
 
     /**
      * Checks the next situation: user A was with email E1, in edit_user E1 was changed to E2, then it was changed back
      * to E1. So user A trying to save email E1 while he already have saved email E1.
      */
     @Test
     public void testTheSameEmailValidation() throws Exception {
         PoulpeUser user = TestFixtures.user();
         givenBindContextReturnsMailAndUserId(user.getEmail(), user.getId());
         storeUsersInMockedDb(user);
         validator.validate(context);
         verify(context, never()).setInvalid();
     }
 
     private void storeUsersInMockedDb(PoulpeUser... users) throws Exception {
         for (PoulpeUser user : users) {
             when(userService.getByEmail(user.getEmail())).thenReturn(user);
         }
     }
 
 }
