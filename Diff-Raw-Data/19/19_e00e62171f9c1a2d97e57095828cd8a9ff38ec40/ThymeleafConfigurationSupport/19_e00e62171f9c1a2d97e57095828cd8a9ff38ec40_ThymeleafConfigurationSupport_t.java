 package org.sevensource.thymeleaf4spring;
 
 import java.util.Set;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.web.servlet.ViewResolver;
 import org.thymeleaf.dialect.IDialect;
 import org.thymeleaf.extras.tiles2.spring.web.view.ThymeleafTilesView;
 import org.thymeleaf.spring3.SpringTemplateEngine;
 import org.thymeleaf.spring3.view.AbstractThymeleafView;
 import org.thymeleaf.spring3.view.ThymeleafView;
 import org.thymeleaf.spring3.view.ThymeleafViewResolver;
 import org.thymeleaf.templateresolver.ITemplateResolver;
 import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 
 public abstract class ThymeleafConfigurationSupport {
 	
 	public final static String DEFAULT_TEMPLATE_CHARSET = "UTF-8";
 	public final static String DEFAULT_TEMPLATE_MODE = "HTML5";
 	public final static String DEFAULT_TEMPLATE_SUFFIX = ".html";
 	
 	
 	/**
 	 * Creates a Thymeleaf {@link ViewResolver}<br>
 	 * Disables cache if {@link #isDevelopment()} is true
 	 * @return
 	 */
 	@Bean
 	public ThymeleafViewResolver thymeleafViewResolver() {
 		ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
		thymeleafViewResolver.setCharacterEncoding(getCharacterEncoding());
 		thymeleafViewResolver.setCache( isCaching() );
 		thymeleafViewResolver.setViewClass(getViewClass());
 		thymeleafViewResolver.setTemplateEngine(templateEngine());
 		
 		return configureViewResolver( thymeleafViewResolver );
 	}
 	
 	/**
 	 * create a {@link SpringTemplateEngine}
 	 * @return
 	 */
 	protected SpringTemplateEngine templateEngine() {
 		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
 		templateEngine.setAdditionalDialects( getAdditionalDialects() );
 		templateEngine.setTemplateResolvers( getTemplateResolvers() );
 		
 		return configureTemplateEngine( templateEngine );
 	}
 	
 	/**
	 * Helper to instantiate new {@link ServletContextTemplateResolver} and set
	 * some common options
 	 */
 	protected ServletContextTemplateResolver createServletContextTemplateResolver() {
 		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
 		
		templateResolver.setCharacterEncoding( getCharacterEncoding() );
 		templateResolver.setSuffix( DEFAULT_TEMPLATE_SUFFIX );
 		templateResolver.setTemplateMode( DEFAULT_TEMPLATE_MODE );
 		
 		if(isCaching()) {
 			templateResolver.setCacheTTLMs( TemplateResolver.DEFAULT_CACHE_TTL_MS );
 			templateResolver.setCacheable( TemplateResolver.DEFAULT_CACHEABLE );
 		} else {
 			templateResolver.setCacheTTLMs(0L);
 			templateResolver.setCacheable( false );
 		}
 		
 		return templateResolver;
 	}
 	
 	
 	/**
	 * 
	 * @return the Character Encoding to be used for {@link TemplateResolver}
	 *         and {@link ViewResolver} (default:
	 *         {@link ThymeleafConfigurationSupport#DEFAULT_TEMPLATE_CHARSET}
	 */
	protected String getCharacterEncoding() {
		return DEFAULT_TEMPLATE_CHARSET;
	}
	
	/**
 	 * should caching of Templates be enabled?
 	 * @return
 	 */
 	protected boolean isCaching() {
 		return isDevelopment() ? false:true;
 	}
 	
 	/**
 	 * if true, Thymeleaf caching is disabled and DefaultAbstractThymeleafTilesConfiguration definition refresh is enabled
 	 * @return
 	 */
 	protected abstract boolean isDevelopment();
 	
 	
 	/**
 	 * Get the actual View implementation class, usually {@link ThymeleafTilesView} or {@link ThymeleafView}
 	 * @return
 	 */
 	protected abstract Class<? extends AbstractThymeleafView> getViewClass();
 	
 	
 	/**
 	 * Create additional Thymeleaf Dialects
 	 * @return can return null
 	 */
 	protected abstract Set<IDialect> getAdditionalDialects();
 	
 	/**
 	 * @return {@link ITemplateResolver}s that should be used by Thymeleaf
 	 */
 	protected abstract Set<ITemplateResolver> getTemplateResolvers();
 	
 	/**
 	 * can be overridden by subclasses to further configure the {@link ThymeleafViewResolver}
 	 * @param viewResolver
 	 * @return
 	 */
 	protected ThymeleafViewResolver configureViewResolver(ThymeleafViewResolver viewResolver) {
 		return viewResolver;
 	}
 	
 	/**
 	 * can be overridden by subclasses to further configure the {@link SpringTemplateEngine}
 	 * @param templateEngine
 	 * @return
 	 */
 	protected SpringTemplateEngine configureTemplateEngine(SpringTemplateEngine templateEngine) {
 		return templateEngine;
 	}
 }
