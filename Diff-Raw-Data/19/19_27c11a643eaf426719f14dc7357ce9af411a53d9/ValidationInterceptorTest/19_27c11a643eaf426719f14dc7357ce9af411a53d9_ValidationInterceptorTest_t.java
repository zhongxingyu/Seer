 package com.teoware.refapp.service.interceptor;
 
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 import java.lang.reflect.Method;
 
 import javax.interceptor.InvocationContext;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 
 import com.teoware.refapp.service.ServiceException;
 import com.teoware.refapp.service.validation.Validate;
 import com.teoware.refapp.service.validation.group.ValidationGroup;
 import com.teoware.refapp.service.validation.util.ServiceFacade;
 
 public class ValidationInterceptorTest {
 
 	@InjectMocks
 	private ValidationInterceptor validationInterceptor = new ValidationInterceptor();
 
 	@Mock
 	private ServiceFacade serviceFacade;
 
 	@Mock
 	private InvocationContext context;
 
 	@Before
 	public void setUp() throws Exception {
 		initMocks(this);
 	}
 
 	@Test
 	public void testValidationOfNonValidatedMethod() throws Exception {
 		String method = "nonValidatedMethod";
 
 		when(context.getMethod()).thenReturn(getMethod(method, null));
 
 		Object object = validationInterceptor.validate(context);
 
 		assertNull(object);
 	}
 
 	@Test(expected = ServiceException.class)
 	public void testValidationOfMethodWithoutParams() throws Exception {
 		String method = "methodWithoutParams";
 
 		when(context.getMethod()).thenReturn(getMethod(method, null));
 
 		validationInterceptor.validate(context);
 	}
 
 	@Test(expected = ServiceException.class)
 	public void testValidationOfMethodWithParamsButNoValidationGroup() throws Exception {
 		String method = "methodWithParamsButNoValidationGroup";
 		Class<?>[] params = { Object.class };
 		Object[] value = { new Object() };
 
 		when(context.getMethod()).thenReturn(getMethod(method, params));
 		when(context.getParameters()).thenReturn(value);
 
 		validationInterceptor.validate(context);
 	}
 
 	@Test(expected = ServiceException.class)
 	public void testValidationOfMethodWithParamsAndSuperValidationGroup() throws Exception {
 		String method = "methodWithParamsAndSuperValidationGroup";
 		Class<?>[] params = { Object.class };
 		Object[] value = { new Object() };
 
 		when(context.getMethod()).thenReturn(getMethod(method, params));
 		when(context.getParameters()).thenReturn(value);
 
 		validationInterceptor.validate(context);
 	}
 
 	@Test
 	public void testValidationOfMethodWithParamsAndValidationGroup() throws Exception {
 		String method = "methodWithParamsAndValidationGroup";
 		Class<?>[] params = { Object.class };
 		Object[] value = { new Object() };
 
 		when(context.getMethod()).thenReturn(getMethod(method, params));
 		when(context.getParameters()).thenReturn(value);
 
 		Object object = validationInterceptor.validate(context);
 
 		assertNull(object);
 	}
 
	@Test
 	public void testValidationOfMethodWithAnnotatedParamsAndValidationGroup() throws Exception {
 		String method = "methodWithAnnotatedParamsAndValidationGroup";
 		Class<?>[] params = { Object.class };
 		Object[] value = { new Object() };
 
 		when(context.getMethod()).thenReturn(getMethod(method, params));
 		when(context.getParameters()).thenReturn(value);
 
 		validationInterceptor.validate(context);
 	}
 
 	public Method getMethod(String name, Class<?>[] params) throws SecurityException, NoSuchMethodException {
 		return DummyValidationClass.class.getMethod(name, params);
 	}
 
 	public class DummyValidationClass {
 
 		public void nonValidatedMethod() {
 		}
 
 		@Validate
 		public void methodWithoutParams() {
 		}
 
 		@Validate
 		public void methodWithParamsButNoValidationGroup(Object param) {
 		}
 
 		@Validate(ValidationGroup.class)
 		public void methodWithParamsAndSuperValidationGroup(Object param) {
 		}
 
 		@Validate(DummyValidationGroup.class)
 		public void methodWithParamsAndValidationGroup(Object param) {
 		}
 
 		@Validate(DummyValidationGroup.class)
 		public void methodWithAnnotatedParamsAndValidationGroup(@Validate Object param) {
 		}
 	}
 
 	public interface DummyValidationGroup extends ValidationGroup {
 	}
 }
