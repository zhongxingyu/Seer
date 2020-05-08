 package net.thucidides.fragments;
 
 import com.google.inject.Injector;
 import net.thucidides.fragments.elements.Fragment;
 import net.thucidides.fragments.locators.DynamicLocatorFactory;
 import net.thucidides.fragments.proxy.ElementLoader;
 import net.thucidides.fragments.proxy.FrameLoader;
 import net.thucidides.fragments.proxy.ListLoader;
 import net.thucidides.fragments.utils.PageUtils;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.internal.WrapsElement;
 import org.openqa.selenium.support.pagefactory.ElementLocator;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Proxy;
 import java.util.List;
 
 /**
  * Creates page fragments
  * 
  * @author eger
  */
 public class FragmentFactory {
 	
	private static final String FRAGMENT_FIELD_LOCATOR = "locators";
 	private static final String FRAGMENT_FIELD_PARENT = "parent";
 	private static final String FRAGMENT_FIELD_WRAPPED = "wrapped";
	private static final String FRAGMENT_FIELD_FACTORY = "";
	private static final String FRAGMENT_FIELD_NAME = "";
 	
 	private static final String PAGE_FIELD_FACTORY = "fragmentFactory";
 	private static final String PAGE_FIELD_NAME = "contextName";
 	
 	private final IFragmentContext parentContext;
 	
 	private final Injector injector; 
 	
 	/**
 	 * Create factory for given parent context and injector.
 	 * 
 	 * @param parentContext parent context (page or fragment)
 	 */
 	public FragmentFactory(IFragmentContext parentContext, Injector injector) {
 		this.parentContext = parentContext;
 		this.injector = injector;
 	}
 	
 	/**
 	 * Creates frame which automatically switches search context.
 	 * 
 	 * @param type fragment type
 	 * @param locator IFrame locators
 	 * @param name frame name
 	 * 
 	 * @return fragment instance
 	 */
 	public <F extends Fragment<?>> F createFrame(Class<F> type, ElementLocator locator, String name){
 		return initFragment(type, createFrameProxy(createWebElementProxy(locator)), name, locator.toString());
 	}
 	
 	/**
 	 * Creates fragment given type.
 	 * 
 	 * @param type fragment type
 	 * @param locator fragment locators
 	 * @param name fragment name
 	 * 
 	 * @return fragment instance
 	 */
 	public <F extends Fragment<?>> F createFragment(Class<F> type, ElementLocator locator, String name){
 		return initFragment(type, createWebElementProxy(locator), name, locator.toString());
 	}
 	
 	/**
 	 * Creates list of fragments.
 	 * 
 	 * @param type fragment type
 	 * @param locator fragment locators
 	 * @param name fragment name
 	 * 
 	 * @return
 	 */
 	public <F extends Fragment<?>> List<F> createList(Class<F> type, ElementLocator locator, String name){
 		return (List<F>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), 
 				new Class[]{List.class}, new ListLoader<>(type, locator, this, name));
 	}
 	
 	public static void initPageFragments(PageWithFragments page, Injector injector){
 		FragmentFactory fragmentFactory = new FragmentFactory(page, injector);
 		
 		try {
 			new ReflectionObjectBuilder<PageWithFragments>()
 				.set(PageWithFragments.class, PAGE_FIELD_FACTORY, fragmentFactory)
 				.set(PageWithFragments.class, PAGE_FIELD_NAME, page.getClass().getSimpleName())
 			.assign(page);
 		} catch (IllegalAccessException | NoSuchFieldException | SecurityException ex) {
 			throw new RuntimeException("Failed to inject fields into frgament context.", ex);
 		}
 		
 		decorateFields(page, PageWithFragments.class, 
 				new FragmentDecorator(new DynamicLocatorFactory(page), fragmentFactory));
 		
 		if(injector != null){
 			injector.injectMembers(page);
 		}
 	}
 	
 	private <F extends Fragment<?>> F initFragment(Class<F> klass, WebElement wrapped, String name, String locator){
 		F fragment = initFragmentObject(klass);
 		
 		FragmentFactory fragmentFactory = new FragmentFactory(fragment, injector);
 		
 		try{
 			new ReflectionObjectBuilder<Fragment>()
 				.set(Fragment.class, FRAGMENT_FIELD_NAME, name)
 				.set(Fragment.class, FRAGMENT_FIELD_LOCATOR, locator)
 				.set(Fragment.class, FRAGMENT_FIELD_PARENT, parentContext)
 				.set(Fragment.class, FRAGMENT_FIELD_WRAPPED, wrapped)
 				.set(Fragment.class, FRAGMENT_FIELD_FACTORY, fragmentFactory)
 			.assign(fragment);
 		} catch (IllegalAccessException | NoSuchFieldException | SecurityException ex){
 			throw new RuntimeException(String.format("Failed to inject fields into [%s]", name), ex);
 		}
 		
 		decorateFields(fragment, Fragment.class, 
 				new FragmentDecorator(new DynamicLocatorFactory(wrapped), fragmentFactory));
 		
 		if(injector != null){
 			injector.injectMembers(fragment);
 		}
 		
 		return fragment;
 	}
 	
 	private WebElement createFrameProxy(WebElement frame){
 		return (WebElement) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), 
 				new Class[]{WebElement.class, WrapsElement.class }, new FrameLoader(PageUtils.getParentPage(parentContext), frame));
 	}
 
 	private static WebElement createWebElementProxy(ElementLocator locator){
 		return (WebElement) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), 
 				new Class[]{WebElement.class, WrapsElement.class}, new ElementLoader(locator));
 	}
 	
 	private static <T> void decorateFields(T fragmentContext, Class<? super T> archetype, FragmentDecorator decorator) {
 		Class currentType = fragmentContext.getClass();
 		
 		try{
 			while(currentType != archetype){
 				for(Field field: currentType.getDeclaredFields()){
 					field.set(fragmentContext, decorator.decorate(ClassLoader.getSystemClassLoader(), field));
 				}
 				currentType = currentType.getSuperclass();
 			}
 		} catch (IllegalArgumentException | IllegalAccessException ex){
 			throw new RuntimeException(String.format("Failed to decorate [%s] fields", fragmentContext), ex);
 		}
 	}
 	
 	private static <T extends Fragment> T initFragmentObject(Class<T> type){
 		try{
 			return type.getConstructor().newInstance();
 		} catch (Exception ex){
 			throw new IllegalArgumentException(String.format("Failed to create [%s]!", type), ex);
 		}
 	}
 }
