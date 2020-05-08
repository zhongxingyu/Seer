 package org.fightteam.join.samples.filter;
 
 import org.fightteam.join.samples.filter.security.RestAuthenticationEntryPoint;
 import org.fightteam.join.samples.filter.security.RestUserDetailsService;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
 import org.springframework.security.access.AccessDecisionManager;
 import org.springframework.security.access.AccessDecisionVoter;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.access.SecurityConfig;
 import org.springframework.security.access.expression.SecurityExpressionHandler;
 import org.springframework.security.access.vote.AffirmativeBased;
 import org.springframework.security.access.vote.AuthenticatedVoter;
 import org.springframework.security.access.vote.RoleVoter;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.ProviderManager;
 import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.web.*;
 import org.springframework.security.web.access.ExceptionTranslationFilter;
 import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
 import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
 import org.springframework.security.web.access.expression.WebExpressionVoter;
 import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
 import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
 import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
 import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
 import org.springframework.security.web.context.SecurityContextPersistenceFilter;
 import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
 import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
 import org.springframework.security.web.util.matcher.RequestMatcher;
 
 import javax.servlet.Filter;
 import java.util.*;
 
 /**
  * @author excalibur
  * @since 0.0.1
  */
 @Configuration
 public class SecurityApplicationConfig {
 
     /**
      * 代理过滤器
      *
      * 具体实现由过滤链来完成
      *
      * 如果有多个过滤链就在这定义
     * @return
      */
     @Bean(name = "springSecurityFilterChain")
     public FilterChainProxy portalFilterChainProxy() {
 
         List<SecurityFilterChain> securityFilterChains = new ArrayList<>();
 
         //只需要过滤 REST服务的
         securityFilterChains.add(restSecurityFilterChain());
 
         FilterChainProxy filterChainProxy = new FilterChainProxy(securityFilterChains);
         return filterChainProxy;
 
     }
 
     /**
      * 过滤链
      *
      * 根据自己的需要定义出一个过滤链
      *
      * 最好参照官方的默认实现
      *
     * @return
      */
     @Bean
     public SecurityFilterChain restSecurityFilterChain() {
         // 过滤链的路径
         RequestMatcher requestMatcher = new AntPathRequestMatcher("/**");
 
         // 过滤链
         List<Filter> filters = new ArrayList<>();
 
         // 根据顺序和需要功能添加过滤器
 
 
         // 1.ChannelProcessingFilter
         // 协议过滤器,用于协议之间的切换
 //        filters.add(channelProcessingFilter());
         //filters.add(new CORSAwareFilter());
 
         // 2.SecurityContextPersistenceFilter 必须
         // 权限上下文持久化过滤器,常见是把SecurityContext信息对应到session中
         //filters.add(new SecurityContextPersistenceFilter());
 
         // 3.ConcurrentSessionFilter
         // SecurityContextHolder中更新SessionRegistry，以反映当前的请求中对应的principal
        // filters.add(securityContextHolderAwareRequestFilter());
 
         // 4.UsernamePasswordAuthenticationFilter, CasAuthenticationFilter, BasicAuthenticationFilter etc
         // 认证方式过滤器，选择其中需要的就
         filters.add(tokenAuthenticationFilter());
 
 
         // 5.SecurityContextHolderAwareRequestFilter
         // 注入过滤器，如果你要在HttpServletRequestWrapper中使用，就要配置
         //filters.add(securityContextHolderAwareRequestFilter());
 
         // 6.JaasApiIntegrationFilter
         // 如果JaasAuthenticationToken在SecurityContextHolder中，Subject将会在JaasAuthenticationToken中贯穿整个过滤链
 
 
 
         // 7.RememberMeAuthenticationFilter
         // 提供“记住我”功能的过滤器
 
         // 8.AnonymousAuthenticationFilter
         // 提供匿名登录的过滤器
 
         // 9.ExceptionTranslationFilter
         // 异常处理过滤器
         filters.add(exceptionTranslationFilter());
         // 10.FilterSecurityInterceptor 必须
         // 当需要保护时候，切入点
         filters.add(filterSecurityInterceptor());
 
         return new DefaultSecurityFilterChain(requestMatcher, filters);
     }
 
     /**
      * 协议过滤器
      *
      * 常见协议:http、https等
      *
     * @return
      */
 //    @Bean
 //    public ChannelProcessingFilter channelProcessingFilter(){
 //        ChannelProcessingFilter channelProcessingFilter = new ChannelProcessingFilter();
 //
 //        return channelProcessingFilter;
 //    }
     @Bean
     public SecurityContextPersistenceFilter securityContextPersistenceFilter() {
 
         SecurityContextPersistenceFilter filter = new SecurityContextPersistenceFilter();
         return filter;
     }
 
     @Bean
     public SecurityContextHolderAwareRequestFilter securityContextHolderAwareRequestFilter() {
         SecurityContextHolderAwareRequestFilter f = new SecurityContextHolderAwareRequestFilter();
 
 
         return f;
     }
 
 //    public AbstractAuthenticationProcessingFilter tokenAuthenticationFilter(){
 //        Authentication
 //    }
     /**
      * 权限接入
      *
      * 必须
      *
      * 自定义权限，权限载入等等都由这关联
     * @return
      */
     @Bean
     public FilterSecurityInterceptor filterSecurityInterceptor() {
 
 
         FilterSecurityInterceptor f = new FilterSecurityInterceptor();
         f.setAccessDecisionManager(accessDecisionManager());
         f.setAuthenticationManager(authenticationManager());
         f.setSecurityMetadataSource(filterInvocationSecurityMetadataSource());
 
         try {
             f.afterPropertiesSet();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return f;
     }
 
     /**
      * 资源管理
      *
     * @return
      */
     @Bean
     public FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource() {
 
         SecurityExpressionHandler<FilterInvocation> securityExpressionHandler = new DefaultWebSecurityExpressionHandler();
 
         LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
         map.put(new AntPathRequestMatcher("/**"), Arrays.<ConfigAttribute>asList(new SecurityConfig("hasRole('ROLE_SUPER')")));
 
         ExpressionBasedFilterInvocationSecurityMetadataSource ms = new ExpressionBasedFilterInvocationSecurityMetadataSource(map, securityExpressionHandler);
 
         return ms;
     }
 
     /**
      *
     * @return
      */
     @Bean
     public AuthenticationManager authenticationManager() {
         List<AuthenticationProvider> providers = new ArrayList<>();
         DaoAuthenticationProvider daoAP = new DaoAuthenticationProvider();
         providers.add(daoAP);
         AuthenticationManager authenticationManager = new ProviderManager(providers);
 
         return authenticationManager;
     }
 
 
 
     @Bean
     public AuthenticationProvider daoAuthenticationProvider() {
 
         DaoAuthenticationProvider p = new DaoAuthenticationProvider();
         p.setUserDetailsService(userDetailsService());
         return p;
 
     }
     @Bean
     public UserDetailsService userDetailsService() {
 
         return new RestUserDetailsService();
     }
     /**
      *
     * @return
      */
     @Bean
     public AccessDecisionManager accessDecisionManager() {
 
         List<AccessDecisionVoter> voters = new ArrayList<>();
         voters.add(new RoleVoter());
         voters.add(new WebExpressionVoter());
         voters.add(new AuthenticatedVoter());
 
         AffirmativeBased adm = new AffirmativeBased(voters);
 
         return adm;
 
     }
 
     /**
      * 异常处理
     * @return
      */
     @Bean
     public ExceptionTranslationFilter exceptionTranslationFilter() {
         ExceptionTranslationFilter exceptionTranslationFilter = new ExceptionTranslationFilter(authenticationEntryPoint());
         return exceptionTranslationFilter;
     }
 
     @Bean
     public AuthenticationEntryPoint authenticationEntryPoint(){
         AuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
         return entryPoint;
     }
 
     @Bean
     public UsernamePasswordAuthenticationFilter tokenAuthenticationFilter(){
         UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
         filter.setAuthenticationManager(authenticationManager());
         filter.setPostOnly(false);
         filter.afterPropertiesSet();
         return filter;
     }
 
 }
