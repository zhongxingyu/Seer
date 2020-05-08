 package krasa.laboratory.beans.generator;
 
 import krasa.laboratory.beans.AppContextExtendingBean;
 import krasa.laboratory.beans.ChildBean;
 import krasa.laboratory.beans.Parent;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.config.ConstructorArgumentValues;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.beans.factory.support.GenericBeanDefinition;
 import org.springframework.context.EnvironmentAware;
 import org.springframework.core.PriorityOrdered;
 import org.springframework.core.env.Environment;
 
 /*generates beans according to loaded properties*/
 public class BeanGenerator2 implements BeanFactoryPostProcessor, PriorityOrdered, EnvironmentAware {
 	public static final Log logger = LogFactory.getLog(BeanGenerator2.class);
 	private Environment environment;
 
 	@Override
 	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		String beanInstances = environment.getRequiredProperty("beanInstances");
		String[] split = beanInstances.split(",");
 		for (String key : split) {
 			generateBean(beanFactory, key);
 		}
 	}
 
 	@Override
 	public int getOrder() {
 		return 0;
 	}
 
 	@Override
 	public void setEnvironment(Environment environment) {
 		this.environment = environment;
 	}
 
 	// create bean definition so that fields can be autowired
 	private void generateBean(AutowireCapableBeanFactory beanFactory, String key) {
 		GenericBeanDefinition appContextExtendingBean = getAppContextExtendingBean(key);
 		GenericBeanDefinition child = getChild(key);
 		GenericBeanDefinition parent = getParent(key, child);
 
 		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
 		defaultListableBeanFactory.registerBeanDefinition("parent" + key.toUpperCase(), parent);
 		defaultListableBeanFactory.registerBeanDefinition("child" + key.toUpperCase(), parent);
 		defaultListableBeanFactory.registerBeanDefinition("appContextExtendingBean" + key.toUpperCase(),
 				appContextExtendingBean);
 	}
 
 	private GenericBeanDefinition getAppContextExtendingBean(String key) {
 		ConstructorArgumentValues cas = new ConstructorArgumentValues();
 		cas.addGenericArgumentValue(key);
 
 		GenericBeanDefinition definition = new GenericBeanDefinition();
 		definition.setBeanClass(AppContextExtendingBean.class);
 		definition.setConstructorArgumentValues(cas);
 		definition.setResourceDescription(this.toString());
 		return definition;
 	}
 
 	private GenericBeanDefinition getChild(String key) {
 		ConstructorArgumentValues cas = new ConstructorArgumentValues();
 		cas.addGenericArgumentValue(key);
 
 		GenericBeanDefinition child = new GenericBeanDefinition();
 		child.setBeanClass(ChildBean.class);
 		child.setConstructorArgumentValues(cas);
 		return child;
 	}
 
 	private GenericBeanDefinition getParent(String key, GenericBeanDefinition child) {
 		ConstructorArgumentValues cas = new ConstructorArgumentValues();
 		cas.addGenericArgumentValue(key);
 
 		GenericBeanDefinition def = new GenericBeanDefinition();
 		def.setBeanClass(Parent.class);
 		def.setDescription(toString());
 		def.setConstructorArgumentValues(cas);
 		MutablePropertyValues propertyValues = new MutablePropertyValues();
 		propertyValues.addPropertyValue("childBean", child);
 		def.setPropertyValues(propertyValues);
 		return def;
 	}
 
 }
