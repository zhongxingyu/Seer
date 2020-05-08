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
 package org.jtalks.poulpe.web.controller.branch;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.jtalks.poulpe.model.entity.Branch;
 import org.jtalks.poulpe.model.entity.Section;
 import org.jtalks.poulpe.service.BranchService;
 import org.jtalks.poulpe.service.SectionService;
 import org.jtalks.poulpe.validation.EntityValidator;
 import org.jtalks.poulpe.validation.ValidationError;
 import org.jtalks.poulpe.validation.ValidationResult;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author Bekrenev Dmitry
  * */
 public class BranchPresenterTest {
     @Mock
     SectionService service;
     @Mock
     BranchDialogView view;
     @Mock
     BranchService branchService;
     @Mock 
     EntityValidator entityValidator;
     private Section section = new Section("sectionName", "sectionDescription");
     BranchPresenter presenter = new BranchPresenter();
     
     private ValidationResult resultWithErrors = resultWithErrors();
 
     private ValidationResult resultWithErrors() {
         ValidationError error = new ValidationError("name", Branch.BRANCH_ALREADY_EXISTS);
         Set<ValidationError> errors = Collections.singleton(error);
         return new ValidationResult(errors);
     }
 
     @BeforeMethod
     public void setUp() {
         MockitoAnnotations.initMocks(this);
         presenter.setSectionService(service);
         presenter.setView(view);
         presenter.setBranchService(branchService);
         presenter.setEntityValidator(entityValidator);
     }
 
     @Test
     public void testInitView() {
         List<Section> sections = Collections.nCopies(4, section);
         when(service.getAll()).thenReturn(sections);
 
         presenter.initView();
 
         verify(view).initSectionList(sections);
     }
 
     @Test
     public void testSaveBranch() {
     	givenNoConstraintsViolated();
     	
         Branch branch = new Branch();
        branch.setSection(new Section());
         presenter.saveBranch(branch);
         verify(view, never()).validationFailure(any(ValidationResult.class));
        verify(service).saveSection(any(Section.class));
     }
     
     private void givenNoConstraintsViolated() {
         when(entityValidator.validate(any(Branch.class))).thenReturn(ValidationResult.EMPTY);
     }
 
   @Test
     public void testSaveBranchWhenExceptionHappen()  {
 	   Branch branch = new Branch();
 	   givenConstraintViolated();
 	   
         presenter.saveBranch(branch);
         verify(branchService, never()).saveBranch(any(Branch.class));
     }
 
 	private void givenConstraintViolated() {
 		when(entityValidator.validate(any(Branch.class))).thenReturn(resultWithErrors);
 	}
 
 }
