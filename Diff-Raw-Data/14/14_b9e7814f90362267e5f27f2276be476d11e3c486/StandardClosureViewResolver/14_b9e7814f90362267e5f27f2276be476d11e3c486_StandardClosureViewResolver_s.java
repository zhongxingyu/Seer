 package com.mattwhipple.sproogle.closure.impl;
 
 import java.lang.invoke.MethodHandles;
 import java.util.Locale;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
 import org.springframework.web.servlet.view.AbstractUrlBasedView;
 
 import com.google.template.soy.tofu.SoyTofu.Renderer;
 import com.mattwhipple.sproogle.closure.ClosureTemplateConfiguration;
 
 public class StandardClosureViewResolver extends AbstractTemplateViewResolver {
 
 	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
 	
 	private ClosureTemplateConfiguration closureTemplateConfiguration;
 	
 	public StandardClosureViewResolver() {
 		super();
         setExposeSpringMacroHelpers(false);
 	}
 
 	@Override
 	protected Class<?> getViewClass() {
 		return StandardClosureTemplateView.class;
 	}
 
 	@Override
 	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
 	}
 	
 	@Override
 	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
 		StandardClosureTemplateView view = (StandardClosureTemplateView) super.buildView(viewName);
 		
 		if (viewName == null) throw new NullPointerException("viewName cannot be null");
 		
 		ClosureTemplateConfiguration closureTemplateConfiguration = this.closureTemplateConfiguration;
 		if (closureTemplateConfiguration == null) throw new IllegalStateException("ClosureTemplateConfiguration must be set before a view can be built");
 		
 		view.setClosureTemplateConfiguration(closureTemplateConfiguration);
 		view.setRequestContextAttribute(closureTemplateConfiguration.getRequestContextAttribute());
 		viewName = closureTemplateConfiguration.getNamespacingStrategy().applyNamespace(viewName);
 		Set<String> activePackageNames = closureTemplateConfiguration.getActiveDelegatePackageProvider().getPackageNames();
 		logger.debug("Active Delegate Packages: {}", activePackageNames);
 		view.setActiveDelegatePackageNames(activePackageNames);
 		logger.debug("Rendering View {}", viewName);
 		
 		Renderer viewRenderer = closureTemplateConfiguration.getSoyTofuStore().getTemplates().newRenderer(viewName);
 		if (viewRenderer == null) throw new NullPointerException("Renderer retrieved from store templates should not be null");
 		view.setSoyTofuRenderer(viewRenderer);
 		return view;
 	}
 	
 	public void setClosureTemplateConfiguration(
 			ClosureTemplateConfiguration closureTemplateConfiguration) {
 		this.closureTemplateConfiguration = closureTemplateConfiguration;
 	}
 
 }
