 package net.sue445.s3tiger;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sue445.s3tiger.internal.AppEngineWebConfigUtil;
 import net.sue445.s3tiger.internal.IgnoreType;
 
 import org.junit.internal.runners.model.EachTestNotifier;
 import org.junit.runner.Description;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.slim3.util.AppEngineUtil;
 
/**
 * Slim3 runner
 * <ul>
 * <li>using {@link IgnoreProduction}, {@link IgnoreDevelopment}, {@link IgnoreServer}, {@link IgnoreNotServer} </li>
 * <li>when run on not server, read &lt;system-properties&gt; from appengine-web.xml </li>
 * </ul>
 * @author sue445
 *
 */
 public class Slim3 extends BlockJUnit4ClassRunner {
 	private static final Logger log = Logger.getLogger(Slim3.class.getName());
 
 	private final boolean isClassIgnore;
 
 
 	/**
 	 *
 	 * @param klass
 	 * @throws InitializationError
 	 */
 	public Slim3(Class<?> klass) throws InitializationError {
 		super(klass);
 		this.isClassIgnore = isClassIgnore();
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	protected boolean isClassIgnore(){
 		Class<?> javaClass = super.getTestClass().getJavaClass();
 		List<IgnoreType> ignoreTypes = IgnoreType.toEnum(javaClass);
 		return hasCurrentEnvironment(ignoreTypes);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
 		if(isClassIgnore || isMethodIgnore(method)){
 			skipTestMethod(method, notifier);
 			return;
 		}
 
 		Properties originalSystemProperties = setUpSystemProperties();
 
 		try{
 			super.runChild(method, notifier);
 
 		} finally {
 			tearDownSystemProperties(originalSystemProperties);
 		}
 	}
 
 	/**
 	 *
 	 * @param method
 	 * @param notifier
 	 */
 	protected void skipTestMethod(FrameworkMethod method, RunNotifier notifier) {
 		EachTestNotifier eachNotifier= makeNotifier(method, notifier);
 		eachNotifier.fireTestStarted();
 		eachNotifier.fireTestFinished();
 		log.log(Level.FINE, "ignored");
 	}
 
 	/**
 	 * read system properties from appengine-web.xml
 	 * @return original system properties
 	 */
 	protected Properties setUpSystemProperties() {
 		if(AppEngineUtil.isServer()){
 			return null;
 
 		} else{
 			Properties originalSystemProperties = (Properties) System.getProperties().clone();
 			System.getProperties().putAll(AppEngineWebConfigUtil.getSystemProperties());
 			return originalSystemProperties;
 		}
 	}
 
 	/**
 	 *
 	 * @param originalSystemProperties
 	 */
 	protected void tearDownSystemProperties(Properties originalSystemProperties) {
 		if(originalSystemProperties != null){
 			System.getProperties().clear();
 			System.getProperties().putAll(originalSystemProperties);
 		}
 	}
 
 	/**
 	 *
 	 * @param method
 	 * @return
 	 */
 	protected boolean isMethodIgnore(FrameworkMethod method){
 		List<IgnoreType> ignoreTypes = IgnoreType.toEnum(method.getMethod());
 		return hasCurrentEnvironment(ignoreTypes);
 	}
 
 	/**
 	 *
 	 * @param method
 	 * @param notifier
 	 * @return
 	 */
 	protected EachTestNotifier makeNotifier(FrameworkMethod method, RunNotifier notifier) {
 		Description description= describeChild(method);
 		return new EachTestNotifier(notifier, description);
 	}
 
 	/**
 	 *
 	 * @param ignoreTypes
 	 * @return
 	 */
 	protected static boolean hasCurrentEnvironment(List<IgnoreType> ignoreTypes){
 		for(IgnoreType ignore : ignoreTypes){
 			if(ignore.isCurrentEnvironment()){
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
