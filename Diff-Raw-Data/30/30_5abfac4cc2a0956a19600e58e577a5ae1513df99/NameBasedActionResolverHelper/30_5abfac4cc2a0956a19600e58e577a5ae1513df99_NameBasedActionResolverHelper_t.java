 package net.sourceforge.stripes.controller;
 
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.sourceforge.stripes.action.ActionBean;
 import net.sourceforge.stripes.action.ActionBeanContext;
 import net.sourceforge.stripes.action.SessionScope;
 import net.sourceforge.stripes.exception.ActionBeanNotFoundException;
 import net.sourceforge.stripes.exception.StripesServletException;
 import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.ParseException;
 
 /**
  * This is a helper class for the <code>NameBasedActionResolver</code>.
  * All overriding implementations are located in this helper class instead of writing them as Strings in the 
  * <code>NameBasedActionResolverBytecodeProcessor</code>.
  * 
  * @author Andreas Knifh
  */
 public class NameBasedActionResolverHelper {
 
 	/**
 	 * The <code>NameBasedActionResolver</code>.
 	 */
 	private NameBasedActionResolver nameBasedActionResolver;
 	
 	/**
 	 * The <code>Log</code> from the <code>NameBasedActionResolver</code>;
 	 */
 	private Log log;
 
 	/**
 	 * @param nameBasedActionResolver The calling <code>NameBasedActionResolver</code>.
 	 * @param log The <code>Log</code> of calling <code>NameBasedActionResolver</code> .
 	 */
 	public NameBasedActionResolverHelper(NameBasedActionResolver nameBasedActionResolver, Log log) {
 		this.nameBasedActionResolver = nameBasedActionResolver;
 		this.log = log;
 	}
 
 	/**
 	 * Overrides the <code>AnnotatedClassActionResolver.makeNewActionBean()</code> to make sure no actionBeans are created
 	 * from a cached version of the <code>ActionBean</code> class. 
 	 */
 	public ActionBean makeNewActionBean(Class<? extends ActionBean> type, ActionBeanContext ctx)
 			throws Exception, InstantiationException, IllegalAccessException {
 		String className = type.getName();
 		log.debug("makeNewActionBean: ", className);
 		ActionBean actionBean = (ActionBean) type.newInstance();
 		nameBasedActionResolver.addActionBean(actionBean.getClass());
 		return actionBean;
 	}
 
 	/**
 	 * Overrides the <code>NameBasedActionResolver.getActionBean()</code> to ensure that new ActionBeans are discovered
 	 * dynamically.
 	 */
 	public ActionBean getActionBean(ActionBeanContext context, String urlBinding) throws StripesServletException {
 		log.debug("getActionBean: ", urlBinding);
         try {
         	try {
         		Class<? extends ActionBean> beanClass = getActionBeanType(urlBinding);
                 ActionBean bean;
 
                 if (beanClass == null) {
                     throw new ActionBeanNotFoundException(
                     		urlBinding, UrlBindingFactory.getInstance().getPathMap());
                 }
                 
                 if(!nameBasedActionResolver.getUrlBinding(beanClass).equals(urlBinding)) {
                 	log.warn(beanClass.getName() + " is no longer bound to " + urlBinding);
                 	throw new ActionBeanNotFoundException(beanClass.getName() + " is no longer bound to " + urlBinding);
                 }
 
                 String bindingPath = nameBasedActionResolver.getUrlBinding(beanClass);
                 try {
                     HttpServletRequest request = context.getRequest();
 
                     if (beanClass.isAnnotationPresent(SessionScope.class)) {
                         bean = (ActionBean) request.getSession().getAttribute(bindingPath);
 
                         if (bean == null) {
                             bean = makeNewActionBean(beanClass, context);
                             request.getSession().setAttribute(bindingPath, bean);
                         }
                     }
                     else {
                         bean = (ActionBean) request.getAttribute(bindingPath);
                         if (bean == null) {
                             bean = makeNewActionBean(beanClass, context);
                             request.setAttribute(bindingPath, bean);
                         }
                     }
 
                     nameBasedActionResolver.setActionBeanContext(bean, context);
                 }
                 catch (Exception e) {
                     StripesServletException sse = new StripesServletException(
                         "Could not create instance of ActionBean type [" + beanClass.getName() + "].", e);
                     log.error(sse);
                     throw sse;
                 }
 
                 nameBasedActionResolver.assertGetContextWorks(bean);
                 return bean;
             }
             catch (StripesServletException sse) {
                 ActionBean bean = nameBasedActionResolver.handleActionBeanNotFound(context, urlBinding);
                 if (bean != null) {
                 	nameBasedActionResolver.setActionBeanContext(bean, context);
                 	nameBasedActionResolver.assertGetContextWorks(bean);
                     return bean;
                 }
                 else {
                     throw sse;
                 }
             }
         }
         catch (StripesServletException exc) {
             rescanFor(urlBinding);
             try {
             	Class<? extends ActionBean> beanClass = getActionBeanType(urlBinding);
                 ActionBean bean;
 
                 if (beanClass == null) {
                     throw new ActionBeanNotFoundException(
                     		urlBinding, UrlBindingFactory.getInstance().getPathMap());
                 }
 
                 String bindingPath = nameBasedActionResolver.getUrlBinding(beanClass);
                 try {
                     HttpServletRequest request = context.getRequest();
 
                     if (beanClass.isAnnotationPresent(SessionScope.class)) {
                         bean = (ActionBean) request.getSession().getAttribute(bindingPath);
 
                         if (bean == null) {
                             bean = makeNewActionBean(beanClass, context);
                             request.getSession().setAttribute(bindingPath, bean);
                         }
                     }
                     else {
                         bean = (ActionBean) request.getAttribute(bindingPath);
                         if (bean == null) {
                             bean = makeNewActionBean(beanClass, context);
                             request.setAttribute(bindingPath, bean);
                         }
                     }
 
                     nameBasedActionResolver.setActionBeanContext(bean, context);
                 }
                 catch (Exception e) {
                     StripesServletException sse = new StripesServletException(
                         "Could not create instance of ActionBean type [" + beanClass.getName() + "].", e);
                     log.error(sse);
                     throw sse;
                 }
 
                 nameBasedActionResolver.assertGetContextWorks(bean);
                 return bean;
             }
             catch (StripesServletException sse) {
                 ActionBean bean = nameBasedActionResolver.handleActionBeanNotFound(context, urlBinding);
                 if (bean != null) {
                 	nameBasedActionResolver.setActionBeanContext(bean, context);
                 	nameBasedActionResolver.assertGetContextWorks(bean);
                     return bean;
                 }
                 else {
                     throw sse;
                 }
             }
         }
 	}
 
 	/**
 	 * Overrides the <code>AnnotatedClassActionResolver.getActionBeanType()</code> to ensure that the correct
 	 * <code>ActionBeanType</code> is returned for the urlBinding, even if to ActionBeans has swapped urlBindings with each
 	 * other. 
 	 */
 	public Class<? extends ActionBean> getActionBeanType(String urlBinding) {
         log.debug("getActionBeanType for urlBinding: ", urlBinding);
 
         UrlBinding binding = UrlBindingFactory.getInstance().getBindingPrototype(urlBinding);
         Class<? extends ActionBean> cls = binding == null ? null : binding.getBeanType();
         if (cls == null && !urlBinding.endsWith(".jsp")) {
             cls = rescanFor(urlBinding);
         }
         return cls;
 	}
 	
 	/**
 	 * Redoes the scan that Stripes normally does at startup to find Action
 	 * Beans, and looks for an Action Bean that is bound to the given URL.
 	 * 
 	 * This method comes directly from stripes-reload.
 	 * 
 	 * @param nameBasedActionResolver
 	 * 
 	 * @param urlBinding
 	 *            the URL binding of the Action Bean.
 	 * @return the Action Bean that is bound to the URL, or {@code null} if none
 	 *         was found.
 	 */
 	private Class<? extends ActionBean> rescanFor(String urlBinding) {
 		log.debug("rescanFor: ", urlBinding);
 		Set<Class<? extends ActionBean>> classes = nameBasedActionResolver.findClasses();
 		for (Class<? extends ActionBean> cls : classes) {
			//FIXME Note this causes a problem when rescanning during tests and there are classes with bad urlBindings.
			try {
				if (urlBinding.equals(nameBasedActionResolver.getUrlBinding(cls))) {
					nameBasedActionResolver.addActionBean(cls);
					return cls;
				}
			} catch (ParseException e) {
				log.debug("Ignoring ParseException");
 			}
 		}
 		return null;
 	}
 }
