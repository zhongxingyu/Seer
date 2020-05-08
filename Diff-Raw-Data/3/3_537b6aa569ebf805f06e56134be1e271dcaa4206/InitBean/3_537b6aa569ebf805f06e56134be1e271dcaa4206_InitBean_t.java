 package com.pms.service.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
 import org.springframework.core.type.filter.AnnotationTypeFilter;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.pms.service.PackageRole;
 import com.pms.service.cfg.ConfigurationManager;
 import com.pms.service.dao.ICommonDao;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.RoleBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.util.DataEncrypt;
 
 public class InitBean {
 
     public static final Set<String> loginPath = new HashSet<String>();
     public static final Map<String, String> rolesValidationMap = new HashMap<String, String>();
     private static final Logger logger = LogManager.getLogger(InitBean.class);
 
     public static final String ADMIN_USER_NAME = "admin";
 
     /**
      * 初始化数据库
      * 
      * @param dao
      * @throws SecurityException
      * @throws ClassNotFoundException
      */
     public static void initUserRoleDB(ICommonDao dao) throws SecurityException, ClassNotFoundException {
         initRoleItems(dao);
         setLoginPathValidation();
 
         createAdminGroup(dao);
         createSystemDefaultGroups(dao);
         createAdminUser(dao);
     }
 
     private static void createSystemDefaultGroups(ICommonDao dao) {
         String[] groupNames = new String[] { GroupBean.PROJECT_MANAGER_VALUE, GroupBean.PROJECT_ASSISTANT_VALUE, GroupBean.SALES_ASSISTANT_VALUE, GroupBean.PM, GroupBean.FINANCE,
                 GroupBean.SALES_MANAGER_VALUE, GroupBean.COO_VALUE, GroupBean.DEPOT_MANAGER_VALUE, GroupBean.PURCHASE_VALUE };
 
         Map<String, String[]> groupRoles = new HashMap<String, String[]>();
         groupRoles.put(GroupBean.PROJECT_MANAGER_VALUE, new String[] {
         	RoleValidConstants.PAY_INVOICE_MANAGER_PROCESS,
         	RoleValidConstants.PURCHASE_REQUEST_PROCESS,  
         	RoleValidConstants.PURCHASE_CONTRACT_PROCESS
         });
         groupRoles.put(GroupBean.PROJECT_ASSISTANT_VALUE, new String[] {
         	RoleValidConstants.PROJECT_UPDATE, 
         	RoleValidConstants.SALES_CONTRACT_UPDATE,
         	RoleValidConstants.PURCHASE_ALLOCATE_MANAGEMENT,
             RoleValidConstants.PURCHASE_REQUEST_MANAGEMENT
         });
         groupRoles.put(GroupBean.SALES_ASSISTANT_VALUE, new String[] {
         	RoleValidConstants.PROJECT_ADD, 
         	RoleValidConstants.PROJECT_UPDATE,
         	RoleValidConstants.SALES_CONTRACT_ADD, 
         	RoleValidConstants.SALES_CONTRACT_UPDATE,
         	RoleValidConstants.PURCHASE_ALLOCATE_MANAGEMENT,
         	RoleValidConstants.PURCHASE_REQUEST_MANAGEMENT
         });
         
         groupRoles.put(GroupBean.PM, new String[] {
         	RoleValidConstants.SHIP_MANAGEMENT, 
         	RoleValidConstants.BORROWING_MANAGEMENT,
         	RoleValidConstants.PAY_INVOICE_ADD,
         	RoleValidConstants.PURCHASE_BACK_MANAGEMENT
         });
         groupRoles.put(GroupBean.FINANCE, new String[] {
         	RoleValidConstants.PAY_INVOICE_FIN_PROCESS,
        	RoleValidConstants.PAY_INVOICE_DONE,
        	RoleValidConstants.FINANCE_MANAGEMENT
         });
         groupRoles.put(GroupBean.SALES_MANAGER_VALUE, new String[] {
         	RoleValidConstants.PAY_INVOICE_MANAGER_PROCESS,
         	RoleValidConstants.PURCHASE_REQUEST_PROCESS,  
             RoleValidConstants.PURCHASE_CONTRACT_PROCESS
         });
         groupRoles.put(GroupBean.DEPOT_MANAGER_VALUE, new String[] {
         	RoleValidConstants.SHIP_MANAGEMENT_PROCESS, 
         	RoleValidConstants.BORROWING_MANAGEMENT_PROCESS,
         	RoleValidConstants.PURCHASE_ALLOCATE_PROCESS,
         	RoleValidConstants.REPOSITORY_MANAGEMENT_PROCESS
         });
         groupRoles.put(GroupBean.PURCHASE_VALUE, new String[] {
         	RoleValidConstants.PURCHASE_CONTRACT_MANAGEMENT, 
             RoleValidConstants.PURCHASE_ORDER_MANAGEMENT,
             RoleValidConstants.PURCHASE_ORDER_PROCESS,
             RoleValidConstants.REPOSITORY_MANAGEMENT,
             RoleValidConstants.PURCHASE_REQUEST_PROCESS
 
         });
 
         for (String name : groupNames) {
             Map<String, Object> newGroup = new HashMap<String, Object>();
             newGroup.put(GroupBean.GROUP_NAME, name);
 
             // 查找是否角色已经初始化
             Map<String, Object> group = dao.findOne(GroupBean.GROUP_NAME, name, DBBean.USER_GROUP);
 
             Map<String, Object> roleQuery = new HashMap<String, Object>();
             roleQuery.put(RoleBean.ROLE_ID, new DBQuery(DBQueryOpertion.IN, groupRoles.get(name)));
             roleQuery.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
 
             List<Object> roles = dao.listLimitKeyValues(roleQuery, DBBean.ROLE_ITEM);
 
             if (group == null) {
                 // 系统角色不允许删除
                 newGroup.put(GroupBean.IS_SYSTEM_GROUP, true);
                 newGroup.put(GroupBean.ROLES, roles);
                 dao.add(newGroup, DBBean.USER_GROUP);
             } else {
                 group.put(GroupBean.ROLES, roles);
                 group.put(GroupBean.IS_SYSTEM_GROUP, true);
                 dao.updateById(group, DBBean.USER_GROUP);
             }
         }
 
     }
 
     private static void createAdminGroup(ICommonDao dao) {
         logger.info("Init admin group");
         Map<String, Object> adminGroup = new HashMap<String, Object>();
         adminGroup.put(GroupBean.GROUP_NAME, GroupBean.GROUP_ADMIN_VALUE);
 
         // 查找是否admin角色已经初始化
         Map<String, Object> group = dao.findOne(GroupBean.GROUP_NAME, GroupBean.GROUP_ADMIN_VALUE, DBBean.USER_GROUP);
 
         // 查询所有的权限赋值给admin
         Map<String, Object> roleItemQuery = new HashMap<String, Object>();
         roleItemQuery.put(ApiConstants.LIMIT_KEYS, new String[] { ApiConstants.MONGO_ID });
         List<Object> list = dao.listLimitKeyValues(roleItemQuery, DBBean.ROLE_ITEM);
 
         if (group == null) {
             adminGroup.put(GroupBean.ROLES, list);
             dao.add(adminGroup, DBBean.USER_GROUP);
         } else {
             group.put(GroupBean.ROLES, list);
             dao.updateById(group, DBBean.USER_GROUP);
         }
 
     }
 
     private static void createAdminUser(ICommonDao dao) {
         Map<String, Object> adminUser = new HashMap<String, Object>();
         adminUser.put(UserBean.USER_NAME, ADMIN_USER_NAME);
         Map<String, Object> user = dao.findOne(UserBean.USER_NAME, ADMIN_USER_NAME, DBBean.USER);
 
         // 查找admin角色的_id
         Map<String, Object> groupQuery = new HashMap<String, Object>();
         groupQuery.put(GroupBean.GROUP_NAME, GroupBean.GROUP_ADMIN_VALUE);
         groupQuery.put(ApiConstants.LIMIT_KEYS, new String[] { ApiConstants.MONGO_ID });
         List<Object> list = dao.listLimitKeyValues(groupQuery, DBBean.USER_GROUP);
 
         if (user == null) {
             adminUser.put(UserBean.GROUPS, list);
             adminUser.put(UserBean.PASSWORD, DataEncrypt.generatePassword("123456"));
             dao.add(adminUser, DBBean.USER);
         } else {
             user.put(UserBean.GROUPS, list);
             dao.updateById(user, DBBean.USER);
         }
     }
 
     /**
      * 初始化那些path需要登录验证，数据放到内存中
      * 
      * 
      * @throws ClassNotFoundException
      */
     private static void setLoginPathValidation() throws ClassNotFoundException {
         ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
         scanner.resetFilters(true);
         scanner.addIncludeFilter(new AnnotationTypeFilter(LoginRequired.class));
         for (BeanDefinition bd : scanner.findCandidateComponents(PackageRole.class.getPackage().getName())) {
             Class<?> classzz = Class.forName(bd.getBeanClassName());
             Method metods[] = classzz.getMethods();
 
             RequestMapping parent = classzz.getAnnotation(RequestMapping.class);
             String path = "";
             if (parent != null) {
                 path = parent.value()[0];
             }
 
             for (Method m : metods) {
                 LoginRequired rv = m.getAnnotation(LoginRequired.class);
                 if (rv != null) {
                     RequestMapping mapping = m.getAnnotation(RequestMapping.class);
 
                     if (mapping != null) {
                         loginPath.add(path + mapping.value()[0]);
 
                     }
                 }
             }
 
         }
     }
 
     /**
      * 
      * 出事化权限表，权限来至于 @RoleValidate
      * 
      * @param dao
      * @throws ClassNotFoundException
      */
     private static void initRoleItems(ICommonDao dao) throws ClassNotFoundException {
         ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
         scanner.addIncludeFilter(new AnnotationTypeFilter(RoleValidate.class));
         List<String> roleIds = new ArrayList<String>();
 
         // FIXME: 删除不存在的role，但是也需要删除group， user中相关联的数据
         for (BeanDefinition bd : scanner.findCandidateComponents(PackageRole.class.getPackage().getName())) {
             Class<?> classzz = Class.forName(bd.getBeanClassName());
             Method metods[] = classzz.getMethods();
 
             RequestMapping parent = classzz.getAnnotation(RequestMapping.class);
             String path = "";
             if (parent != null) {
                 path = parent.value()[0];
             }
 
             for (Method m : metods) {
                 Annotation annotations[] = m.getAnnotations();
 
                 for (Annotation anno : annotations) {
 
                     if (anno instanceof RoleValidate) {
                         RoleValidate rv = (RoleValidate) anno;
 
                         RequestMapping mapping = m.getAnnotation(RequestMapping.class);
 
                         if (!roleIds.contains(rv.roleID())) {
                             roleIds.add(rv.roleID());
                         }
                         @SuppressWarnings("unchecked")
                         Map<String, Object> role = dao.findOne(RoleBean.ROLE_ID, rv.roleID(), DBBean.ROLE_ITEM);
                         if (role != null) {
                             role.put(RoleBean.ROLE_DESC, rv.desc());
                             dao.updateById(role, DBBean.ROLE_ITEM);
                         } else {
                             Map<String, Object> roleMap = new HashMap<String, Object>();
                             roleMap.put(RoleBean.ROLE_ID, rv.roleID());
                             roleMap.put(RoleBean.ROLE_DESC, rv.desc());
                             dao.add(roleMap, DBBean.ROLE_ITEM);
                         }
 
                         String validPath = path + mapping.value()[0];
                         if (rolesValidationMap.get(validPath) != null) {
                             rolesValidationMap.put(validPath, rv.roleID() + "," + rolesValidationMap.get(validPath));
                         } else {
                             rolesValidationMap.put(validPath, rv.roleID());
                         }
 
                     }
                 }
             }
         }
     }
 
 }
