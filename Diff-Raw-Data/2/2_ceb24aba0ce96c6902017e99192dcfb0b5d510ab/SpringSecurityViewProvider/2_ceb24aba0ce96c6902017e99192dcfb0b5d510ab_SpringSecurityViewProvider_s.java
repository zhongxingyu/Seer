 /*
  * Copyright 2013 ENERKO Informatik GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.vaadin.addons.springsecurityviewprovider;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.context.ApplicationContext;
 import org.springframework.expression.EvaluationContext;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.security.access.expression.ExpressionUtils;
 import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
 import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.util.SimpleMethodInvocation;
 
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewProvider;
 
 /**
  * <p>
  * This is a specialized ViewProvider that takes care of evaluating {@link ViewDescription} 
  * especially {@link ViewDescription#requiredPermissions()}. Those expressions are evaluated
  * within the current Spring Security Context. If they evaluate to false, the view in concern
  * is not even instantiated.
  * </p>
  * <p>
  * This ViewProvider must be retrieved through a factory method as the application context
  * will be transiently stored and thus cannot be autowired through constructor injection.
  * </p>
  * <p>
  * This also requires a the spring-instrumentation agent. See pom.xml or read more about
  * Configurable objects here
  * <a href="http://info.michael-simons.eu/2013/03/12/vaadin-spring-using-configurable-in-vaadin-components/">
  * Vaadin & Spring: Using @Configurable in Vaadin Components
  * </a>
  * </p>
  * @author Michael J. Simons, 2013-03-04
  */
 @Configurable
 public class SpringSecurityViewProvider implements ViewProvider {
 	private static final Logger logger = Logger.getLogger(SpringSecurityViewProvider.class.getName());
 	private static final long serialVersionUID = -8555986824827085073L;
 	/** Will be injected through AspectJ upon new and deserialization */
 	@Autowired
 	transient ApplicationContext applicationContext;
 	/** The viewname -> view class mapping */
 	final Map<String, Class<? extends View>> views = new HashMap<>();
 	/** Cached instances of views */
 	final Map<String, View> cachedInstances = new HashMap<>();
 	private Boolean enableCaching;
 
 	public final static ViewProvider createViewProvider(final Authentication authentication) {
 		return createViewProvider(authentication, null);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public final static ViewProvider createViewProvider(final Authentication authentication, Boolean enableCaching) {
 		final SpringSecurityViewProvider springViewProvider = new SpringSecurityViewProvider();
 		springViewProvider.enableCaching = enableCaching;
 		
 		try {				
 			final ApplicationContext applicationContext = springViewProvider.applicationContext;
 			
 			// Retrieve the default SecurityExpressionHandler 
 			final MethodSecurityExpressionHandler securityExpressionHandler = applicationContext.getBean(DefaultMethodSecurityExpressionHandler.class);
 			// The method that is protected in the end
 			final Method getViewMethod = SpringSecurityViewProvider.class.getMethod("getView", String.class);
 			// A parser to evaluate parse the permissions.
 			final SpelExpressionParser parser = new SpelExpressionParser();
 	
 			// Although beans can be retrieved by annotation they must be retrieved by name
 			// to avoid instanciating them
 			for(String beanName : applicationContext.getBeanDefinitionNames()) {
 				final Class<?> beanClass = applicationContext.getType(beanName);
 				// only work with Views that are described by our specialed Description
 				if (beanClass.isAnnotationPresent(ViewDescription.class) && View.class.isAssignableFrom(beanClass)) {
 					final ViewDescription viewDescription = beanClass.getAnnotation(ViewDescription.class);
 					// requires no special permissions and can be immediatly added
 					if(StringUtils.isBlank(viewDescription.requiredPermissions())) {
 						springViewProvider.views.put(viewDescription.name(), (Class<? extends View>) beanClass);
 					} 
 					// requires permissions
 					else {
 						// this is actually borrowed from the code in org.springframework.security.access.prepost.PreAuthorize
 						final EvaluationContext evaluationContext = securityExpressionHandler.createEvaluationContext(authentication, new SimpleMethodInvocation(springViewProvider, getViewMethod, viewDescription.name()));
 						// only add the view to my provider if the permissions evaluate to true						
 						if(ExpressionUtils.evaluateAsBoolean(parser.parseExpression(viewDescription.requiredPermissions()), evaluationContext))
 							springViewProvider.views.put(viewDescription.name(), (Class<? extends View>) beanClass);							
 					}
 				}        
 	        }						
 		} catch (NoSuchMethodException | SecurityException e) {
 			// Won't happen
 		} 
 				
 		return springViewProvider;
 	}
 	
 	private SpringSecurityViewProvider() {		
 	}
 	
 	/**
 	 * Returns true if this provider supports the given view
 	 * @param clazz
 	 * @return
 	 */
 	public boolean hasView(final Class<? extends View> clazz) {
 		return this.views.containsValue(clazz);
 	}
 	
 	@Override
 	public String getViewName(String viewAndParameters) {
 		String rv = null;
 		if(viewAndParameters != null) {
 			if(views.containsKey(viewAndParameters))
 				rv = viewAndParameters;
 			else {
 				// Find the best (longest) match
 				String bestMatch = null;
 				for(String viewName : views.keySet()) {
					if(viewAndParameters.startsWith(viewName + "/") && (bestMatch == null || bestMatch.length() < viewAndParameters.length())) {
 						bestMatch = viewName;						
 					}
 				}
 				rv = bestMatch;
 			}
 		}
 		return rv;
 	}
 
 	@Override
 	public View getView(String viewName) {			
 		View rv = null;
 
 		// Retrieve the implementing class
 		Class<? extends View> clazz = this.views.get(viewName);
 		if(clazz != null) {
 			// Try to find cached instance of caching is enabled and the view is cacheable
 			if(isCachingEnabled() && clazz.getAnnotation(ViewDescription.class).cacheable()) {
 				rv = this.cachedInstances.get(viewName);
 				// retrieve the new instance and cache it if it's not already cached.
 				if(rv == null)
 					this.cachedInstances.put(viewName, rv = applicationContext.getBean(clazz));
 			} else {
 				rv = applicationContext.getBean(clazz);
 			}
 		}
 		return rv;
 	}		
 
 	/**
 	 * Caching should be enabled only in "prod" environment
 	 * @return true if caching is enabled
 	 */
 	boolean isCachingEnabled() {		
 		final boolean forceViewCaching = enableCaching != null && enableCaching;
 		logger.log(Level.FINE, forceViewCaching ? "Forcing view caching..." : "Letting profile decide about view caching...");
 		return forceViewCaching || this.applicationContext.getEnvironment().acceptsProfiles("prod");
 	}
 }
