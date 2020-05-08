 package org.otherobjects.cms.jcr;
 
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.model.User;
 import org.otherobjects.cms.security.SecurityUtil;
 import org.otherobjects.cms.types.AnnotationBasedTypeDefBuilder;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeDefBuilder;
 import org.otherobjects.cms.types.TypeService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.GrantedAuthorityImpl;
 import org.springframework.security.MockAuthenticationManager;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationProvider;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit38.AbstractTransactionalJUnit38SpringContextTests;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 import org.springmodules.jcr.jackrabbit.ocm.JcrMappingTemplate;
 
@ContextConfiguration(locations = {"file:src/test/java/org/otherobjects/cms/jcr/jcr-test-context.xml"})
 @TransactionConfiguration(transactionManager = "jcrTransactionManager", defaultRollback = true)
 @Transactional
 public abstract class BaseJcrTestCase extends AbstractTransactionalJUnit38SpringContextTests
 {
     @Autowired
     protected JcrMappingTemplate jcrMappingTemplate;
 
     @Autowired
     protected TypeService typeService;
 
     @Autowired
     protected UniversalJcrDao universalJcrDao;
 
     @Autowired
     protected TypeDefBuilder typeDefBuilder;
 
     @Autowired
     protected OtherObjectsConfigurator configurator;
 
     @Autowired
     protected DaoService daoService;
 
     protected void registerType(Class<?> cls) throws Exception
     {
         AnnotationBasedTypeDefBuilder b = new AnnotationBasedTypeDefBuilder();
         TypeDef typeDef = b.getTypeDef(cls);
         typeService.registerType(typeDef);
     }
 
     protected void registerType(TypeDef typeDef)
     {
         typeService.registerType(typeDef);
     }
 
     protected void adminLogin()
     {
         // pretend to be an editor
         // fake admin
         User admin = new User("admin");
         admin.setId(new Long(1));
 
         SecurityContextHolder.getContext().setAuthentication(
                 new MockAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(admin, "admin", new GrantedAuthority[]{new GrantedAuthorityImpl(SecurityUtil.EDITOR_ROLE_NAME)})));
     }
 
     protected void anoymousLogin()
     {
         // pretend to be an anonymous user
         AnonymousAuthenticationProvider anonymousAuthenticationProvider = new AnonymousAuthenticationProvider();
         anonymousAuthenticationProvider.setKey("testkey");
         AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken("testkey", "anonymous", new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_ANONYMOUS")});
         SecurityContextHolder.getContext().setAuthentication(anonymousAuthenticationProvider.authenticate(anonymousAuthenticationToken));
     }
 
     protected void logout()
     {
         SecurityContextHolder.clearContext();
     }
 
 }
