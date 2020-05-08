 package org.synyx.minos.skillz.web.validation;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import org.synyx.minos.skillz.domain.Project;
 import org.synyx.minos.skillz.service.SkillManagement;
 
 /**
  * Validator for {@code Project}. Checks the following criteria:
  * <ul>
  * <li>Projectname is set</li>
  * <li>Projectname is not 'form'</li>
  * <li>Projectname does not already exist (only for new projects)</li>
  * </ul>
  * 
  * @author Michael Herbold - herbold@synyx.de
  */
 public class ProjectValidator implements Validator {
 
     private final SkillManagement skillManagement;
 
     /** error message for empty project name. */
     public static final String PROJECT_NAME_EMPTY =
             "skillz.project.name.error.empty";
 
     /** error message for already used project name. */
     public static final String PROJECT_NAME_ALREADY_EXISTS =
             "skillz.project.name.error.alreadyexists";
 
     /** error message for 'form' as project name. */
     public static final String PROJECT_NAME_INVALID =
             "skillz.project.name.error.invalid";
     
     /** invalid project name value. */
     public static final String INVALID_NAME_VALUE = "form";
 
     
     /**
      * Creates a new {@link ProjectValidator} instance.
      */
     @Autowired
     public ProjectValidator(SkillManagement skillManagement) {
 
         this.skillManagement = skillManagement;
     }
 
     
     /*
      * (non-Javadoc)
      * 
      * @see org.springframework.validation.Validator#supports(java.lang.Class)
      */
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
 
         return Project.class.equals(clazz);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.springframework.validation.Validator#validate(java.lang.Object,
      * org.springframework.validation.Errors)
      */
     public void validate(Object target, Errors errors) {
 
         Project project = (Project) target;
 
         // validate project name - not empty
         if (StringUtils.isBlank(project.getName())) {
             errors.rejectValue("name", PROJECT_NAME_EMPTY);
         } else {
             // validate project name - not used already
             if (project.isNew()
                     && null != skillManagement.getProject(project.getName())) {
                 errors.rejectValue("name", PROJECT_NAME_ALREADY_EXISTS);
             }
 
             // Prevent "form" as project name, as it is being used in the URL
             if (StringUtils.equalsIgnoreCase("form", project.getName())) {
                 errors.rejectValue("name", PROJECT_NAME_INVALID);
             }
         }
 
     }
 
 }
