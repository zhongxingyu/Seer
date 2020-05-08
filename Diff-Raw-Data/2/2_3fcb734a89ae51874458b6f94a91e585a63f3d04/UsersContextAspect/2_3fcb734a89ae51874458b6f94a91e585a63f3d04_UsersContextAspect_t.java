 package fr.cg95.cvq.service.users.aspect;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.reflect.MethodSignature;
 import org.springframework.core.Ordered;
 
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.UserSecurityProfile;
 import fr.cg95.cvq.business.users.UserSecurityRule;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.GenericAccessManager;
 import fr.cg95.cvq.security.PermissionException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.security.annotation.IsUser;
 
 @Aspect
 public class UsersContextAspect implements Ordered {
 
     private IHomeFolderDAO homeFolderDAO;
 
     private IIndividualDAO individualDAO;
 
     private IGenericDAO genericDAO;
 
     @Before("fr.cg95.cvq.SystemArchitecture.businessService() && @annotation(context) && within(fr.cg95.cvq.service.users..*)")
     public void contextAnnotatedMethod(JoinPoint joinPoint, Context context) {
         
         if (!ArrayUtils.contains(context.types(), ContextType.ECITIZEN)
             && !ArrayUtils.contains(context.types(), ContextType.AGENT))
             return;
 
         if (SecurityContext.isFrontOfficeContext() && SecurityContext.getCurrentEcitizen() == null
             && ArrayUtils.contains(context.types(), ContextType.UNAUTH_ECITIZEN))
             return;
 
         MethodSignature signature = (MethodSignature) joinPoint.getSignature();
         
         Method method = signature.getMethod();
         Annotation[][] parametersAnnotations = method.getParameterAnnotations();
         Object[] arguments = joinPoint.getArgs();
         Long homeFolderId = null;
         HomeFolder homeFolder = null;
         Long individualId = null;
         Individual individual = null;
         int i = 0;
         for (Object argument : arguments) {
             if (parametersAnnotations[i] != null && parametersAnnotations[i].length > 0) {
                 Annotation parameterAnnotation = parametersAnnotations[i][0];
                 if (parameterAnnotation.annotationType().equals(IsUser.class)) {
                     if (argument instanceof Long) {
                         Long id = (Long)argument;
                         try {
                             individual = (Individual)individualDAO.findById(Individual.class, id);
                             individualId = id;
                         } catch (CvqObjectNotFoundException e1) {
                             try {
                                 homeFolder = (HomeFolder)homeFolderDAO.findById(HomeFolder.class, id);
                                 homeFolderId = id;
                             } catch (CvqObjectNotFoundException e2) {
                                 // no user with this id
                             }
                         }
                     } else if (argument instanceof Individual) {
                         individual = (Individual)argument;
                         individualId = individual.getId();
                     } else if (argument instanceof HomeFolder) {
                         homeFolder = (HomeFolder)argument;
                         homeFolderId = homeFolder.getId();
                     }
                 } 
             }
             i++;
         }
        if (homeFolder == null && individual != null && individual.getHomeFolder() != null) {
             homeFolder = individual.getHomeFolder();
             homeFolderId = homeFolder.getId();
         }
         if (!GenericAccessManager.performPermissionCheck(homeFolderId, individualId, context))
             throw new PermissionException(joinPoint.getSignature().getDeclaringType(), 
                     joinPoint.getSignature().getName(), context.types(), context.privilege(),
                     "access denied on home folder " + homeFolderId 
                         + " / individual " + individualId);
 
         if (SecurityContext.isBackOfficeContext()) {
             UserSecurityRule rule = genericDAO.simpleSelect(UserSecurityRule.class).and("agentId", SecurityContext.getCurrentUserId()).unique();
             if (!can(rule, context.privilege()))
                 throw new PermissionException(joinPoint.getSignature().getDeclaringType(), joinPoint.getSignature().getName(), context.types(), context.privilege(),
                     "current agent does not have "+ context.privilege() +" prvilege on user referential");
 
         }
     }
 
     private boolean can(UserSecurityRule rule, ContextPrivilege privilege) {
         switch(privilege) {
         case MANAGE:
             if (rule != null && UserSecurityProfile.MANAGE.equals(rule.getProfile()))
                 return true;
             break;
         case WRITE:
             if (rule != null && UserSecurityProfile.writer.contains(rule.getProfile()))
                 return true;
             break;
         default:
             return true;
         }
         return false;
     }
 
     @Override
     public int getOrder() {
         return 1;
     }
 
     public void setHomeFolderDAO(IHomeFolderDAO homeFolderDAO) {
         this.homeFolderDAO = homeFolderDAO;
     }
 
     public void setIndividualDAO(IIndividualDAO individualDAO) {
         this.individualDAO = individualDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
 }
