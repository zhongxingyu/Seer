 package fr.cg95.cvq.service.request.aspect;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import org.apache.log4j.Logger;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.reflect.MethodSignature;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.core.Ordered;
 
 import fr.cg95.cvq.business.authority.Category;
 import fr.cg95.cvq.business.authority.CategoryProfile;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.dao.authority.ICategoryDAO;
import fr.cg95.cvq.dao.request.IRequestActionDAO;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.dao.request.IRequestTypeDAO;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.GenericAccessManager;
 import fr.cg95.cvq.security.PermissionException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.security.annotation.IsHomeFolder;
 import fr.cg95.cvq.security.annotation.IsIndividual;
 import fr.cg95.cvq.security.annotation.IsRequester;
 import fr.cg95.cvq.security.annotation.IsSubject;
 import fr.cg95.cvq.service.request.IRequestActionService;
 import fr.cg95.cvq.service.request.annotation.IsCategory;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.service.request.annotation.IsRequestAction;
 import fr.cg95.cvq.service.request.annotation.IsRequestType;
 
 @Aspect
 public class RequestContextCheckAspect implements Ordered, BeanFactoryAware {
     
     private Logger logger = Logger.getLogger(RequestContextCheckAspect.class);
     
     private IRequestDAO requestDAO;
     private IRequestTypeDAO requestTypeDAO;
     private ICategoryDAO categoryDAO;
    private IRequestActionDAO requestActionDAO;
     private BeanFactory beanFactory;
 
     @Before("fr.cg95.cvq.SystemArchitecture.businessService() && @annotation(context) && within(fr.cg95.cvq.service.request..*)")
     public void contextAnnotatedMethod(JoinPoint joinPoint, Context context) {
         
         MethodSignature signature = (MethodSignature) joinPoint.getSignature();
 
         if (!context.type().equals(ContextType.ECITIZEN) 
                 && !context.type().equals(ContextType.ECITIZEN_AGENT)
                 && !context.type().equals(ContextType.AGENT)) {
             logger.debug("contextAnnotatedMethod() unhandled context type ("
                     + context.type() + ") on method " + signature.getMethod().getName()
                     + ", ignoring");
             return;
         }
         
         if (context.privilege().equals(ContextPrivilege.NONE)) {
             logger.debug("contextAnnotatedMethod() no special privilege asked"
                     + " on method " + signature.getMethod().getName() + ", returning");
             return;
         }
         
         Method method = signature.getMethod();
         Annotation[][] parametersAnnotations = method.getParameterAnnotations();
         Object[] arguments = joinPoint.getArgs();
         Long homeFolderId = null;
         Long individualId = null;
         Long categoryId = null;
         int i = 0;
         for (Object argument : arguments) {
             if (parametersAnnotations[i] != null && parametersAnnotations[i].length > 0) {
                 Annotation parameterAnnotation = parametersAnnotations[i][0];
                 if (parameterAnnotation.annotationType().equals(IsHomeFolder.class)) {
                     homeFolderId = (Long) argument;
                 } else if (parameterAnnotation.annotationType().equals(IsIndividual.class)) {
                     individualId = (Long) argument;
                 } else if (parameterAnnotation.annotationType().equals(IsSubject.class)) {
                     individualId = (Long) argument;
                 } else if (parameterAnnotation.annotationType().equals(IsRequester.class)) {
                     individualId = (Long) argument;
                 } else if (parameterAnnotation.annotationType().equals(IsRequest.class)
                     || parameterAnnotation.annotationType().equals(IsRequestAction.class)) {
                     Request request = null;
                     if (parameterAnnotation.annotationType().equals(IsRequestAction.class)) {
                         RequestAction requestAction = null;
                         if (argument instanceof Long) {
                             try {
                                requestAction = 
                                    (RequestAction) requestActionDAO.findById(RequestAction.class,(Long)argument);
                             } catch (CvqObjectNotFoundException e) {
                                 throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
                                     joinPoint.getSignature().getName(), context.type(),
                                     context.privilege(), "unknown resource type : " + argument);
                             }
                         } else if (argument instanceof RequestAction) {
                             requestAction = (RequestAction) argument;
                         }
                         if (requestAction != null)
                             request = requestAction.getRequest();
                     } else if (argument instanceof Long) {
                         try {
                             request = (Request) requestDAO.findById(Request.class, (Long) argument);
                         } catch (CvqObjectNotFoundException confe) {
                             throw new PermissionException(joinPoint.getSignature().getDeclaringType(), 
                                     joinPoint.getSignature().getName(), context.type(),
                                     context.privilege(), "unknown resource type : " + argument);
                         }
                     } else if (argument instanceof Request) {
                         request = (Request) argument;
                     }
                     if (SecurityContext.isBackOfficeContext()) {
                         if (request.getRequestType() != null 
                                 && request.getRequestType().getCategory() != null)
                             categoryId = request.getRequestType().getCategory().getId();
                         else
                             throw new PermissionException(joinPoint.getSignature().getDeclaringType(), 
                                 joinPoint.getSignature().getName(), context.type(),
                                 context.privilege(), 
                                 "no category associated to request type : " 
                                     + request.getRequestType().getLabel());
                     }
                     homeFolderId = request.getHomeFolderId();
                     individualId = request.getSubjectId();
                 } else if (parameterAnnotation.annotationType().equals(IsRequestType.class)) {
 
                     // no restrictions on request type services opened to e-citizens
                     if (SecurityContext.isFrontOfficeContext())
                         return;
 
                     RequestType requestType = null;
                     if (argument instanceof Long) {
                         try {
                             requestType = (RequestType) requestTypeDAO.findById(RequestType.class, (Long) argument);
                         } catch (CvqObjectNotFoundException confe) {
                             throw new PermissionException(joinPoint.getSignature().getDeclaringType(), 
                                     joinPoint.getSignature().getName(), context.type(), context.privilege(), 
                                     "unknown resource type : " + argument);
                         }                        
                     } else if (argument instanceof RequestType) {
                         requestType = (RequestType) argument;
                     }
 
                     if (requestType == null) {
                         throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
                             joinPoint.getSignature().getName(), context.type(), context.privilege(),
                             "no request type specified");
                     }
 
                     categoryId = requestType.getCategory().getId();
                     
                     // TODO : mutualize
 //                    CategoryRoles[] categoryRoles = 
 //                        SecurityContext.getCurrentCredentialBean().getCategoryRoles();
 //                    for (CategoryRoles categoryRole : categoryRoles) {
 //                        Set<RequestType> categoryRequests = 
 //                            categoryRole.getCategory().getRequestTypes();
 //                        if (categoryRequests == null)
 //                            continue;
 //                        for (RequestType requestTypeToCheck : categoryRequests) {
 //                            if (requestTypeToCheck.getId().equals(requestType.getId())) {
 //                                // we found the request type we are interested in
 //                                if (context.privilege().equals(ContextPrivilege.READ)
 //                                        || (context.privilege().equals(ContextPrivilege.WRITE)
 //                                                && (categoryRole.getProfile().equals(CategoryProfile.READ_WRITE)
 //                                                        || categoryRole.getProfile().equals(CategoryProfile.MANAGER)))
 //                                                        || (context.privilege().equals(ContextPrivilege.MANAGE)
 //                                                                && categoryRole.getProfile().equals(CategoryProfile.MANAGER))) {
 //                                    // that's ok, let's return
 //                                    return;
 //                                } else {
 //                                    break;
 //                                }
 //                            }
 //                        }
 //                    }
                     
                     // if we are here, that means agent is not authorized
 //                    throw new PermissionException(joinPoint.getSignature().getDeclaringType(), 
 //                            joinPoint.getSignature().getName(), context.type(), context.privilege(),
 //                            "request type " + requestType.getLabel());
                     
                 } else if (parameterAnnotation.annotationType().equals(IsCategory.class)) {
                     Category categoryToCheck = null;
                     if (argument instanceof Long) {
                         try {
                             categoryToCheck = (Category) categoryDAO.findById(Category.class, (Long) argument);
                         } catch (CvqObjectNotFoundException confe) {
                             throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
                                     joinPoint.getSignature().getName(), context.type(),
                                     context.privilege(), "unknown resource type : " + argument);
                         }
                     } else if (argument instanceof Category) {
                         categoryToCheck = (Category) argument;
                     }
 
                     if (categoryToCheck == null) {
                         throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
                             joinPoint.getSignature().getName(), context.type(), context.privilege(),
                             "no category specified");
                     }
 
                     categoryId = categoryToCheck.getId();
                     
                     // TODO : mutualize
 //                    CategoryRoles[] categoryRoles =
 //                        SecurityContext.getCurrentCredentialBean().getCategoryRoles();
 //                    for (CategoryRoles categoryRole : categoryRoles) {
 //                        Category category = categoryRole.getCategory();
 //                        if (categoryToCheck.getId().equals(category.getId())) {
 //                            // we found the category we are interested in
 //                            if (context.privilege().equals(ContextPrivilege.READ)
 //                                || (context.privilege().equals(ContextPrivilege.WRITE)
 //                                    && (categoryRole.getProfile().equals(CategoryProfile.READ_WRITE)
 //                                        || categoryRole.getProfile().equals(CategoryProfile.MANAGER)))
 //                                || (context.privilege().equals(ContextPrivilege.MANAGE)
 //                                    && categoryRole.getProfile().equals(CategoryProfile.MANAGER))) {
 //                                // that's ok, let's return
 //                                return;
 //                            } else {
 //                                break;
 //                            }
 //                        }
 //                    }
 //
 //                    // if we are here, that means agent is not authorized
 //                    throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
 //                            joinPoint.getSignature().getName(), context.type(), context.privilege(),
 //                            "category " + categoryToCheck.getName());
                 }
             }
             i++;
         }
 
         if (!GenericAccessManager.performPermissionCheck(homeFolderId, individualId,
             context.privilege()))
             throw new PermissionException(joinPoint.getSignature().getDeclaringType(), 
                     joinPoint.getSignature().getName(), context.type(), context.privilege(), 
                     "access denied on home folder " + homeFolderId +
                     " / individual " + individualId);
         
         if (SecurityContext.isBackOfficeContext()) {
 
             if (context.privilege().equals(ContextPrivilege.MANAGE)) {
                 CategoryRoles[] categoryRoles =
                     SecurityContext.getCurrentCredentialBean().getCategoryRoles();
                 if (categoryRoles != null) {
                     for (CategoryRoles categoryRole : categoryRoles) {
                         if (categoryRole.getProfile().equals(CategoryProfile.MANAGER))
                             return;
                     }
                 }
                 throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
                     joinPoint.getSignature().getName(), context.type(), context.privilege(),
                     "access denied on home folder " + homeFolderId);
             }
 
             // TODO ACMF : to be completed
             if (categoryId == null) {
                 logger.debug("contextAnnotatedMethod() no category or request type provided, "
                     + "not performing any more special permission checks");
                 return;
             }
             
             Category categoryToCheck = null;
             try {
                 categoryToCheck = (Category) categoryDAO.findById(Category.class, categoryId);
             } catch (CvqObjectNotFoundException confe) {
                 // this has been checked before
             }
 
             CategoryRoles[] categoryRoles =
                 SecurityContext.getCurrentCredentialBean().getCategoryRoles();
             for (CategoryRoles categoryRole : categoryRoles) {
                 Category category = categoryRole.getCategory();
                 if (categoryToCheck.getId().equals(category.getId())) {
                     // we found the category we are interested in
                     if (context.privilege().equals(ContextPrivilege.READ)
                         || (context.privilege().equals(ContextPrivilege.WRITE)
                             && (categoryRole.getProfile().equals(CategoryProfile.READ_WRITE)
                                 || categoryRole.getProfile().equals(CategoryProfile.MANAGER)))
                         || (context.privilege().equals(ContextPrivilege.MANAGE)
                             && categoryRole.getProfile().equals(CategoryProfile.MANAGER))) {
                         // that's ok, let's return
                         return;
                     } else {
                         break;
                     }
                 }
             }
 
             // if we are here, that means agent is not authorized
             throw new PermissionException(joinPoint.getSignature().getDeclaringType(),
                     joinPoint.getSignature().getName(), context.type(), context.privilege(),
                     "category " + categoryToCheck.getName());
         }
     }
     
     @Override
     public int getOrder() {
         return 1;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setRequestTypeDAO(IRequestTypeDAO requestTypeDAO) {
         this.requestTypeDAO = requestTypeDAO;
     }
 
     public void setCategoryDAO(ICategoryDAO categoryDAO) {
         this.categoryDAO = categoryDAO;
     }
    
    public void setRequestActionDAO(IRequestActionDAO requestActionDAO) {
        this.requestActionDAO = requestActionDAO;
    }
 
     @Override
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 }
