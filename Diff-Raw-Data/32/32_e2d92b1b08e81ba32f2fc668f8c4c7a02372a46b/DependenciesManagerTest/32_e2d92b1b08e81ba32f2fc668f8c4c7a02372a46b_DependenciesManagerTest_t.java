 package com.codingcrayons.tnt.core;
 
 import java.util.HashMap;
 import java.util.Map;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 public class DependenciesManagerTest {
 
 	protected DependenciesManager manager;
 	protected FacesContext context;
 	protected ExternalContext externalContext;
 	protected Map<Object, Object> attrs;
 	protected UIViewRoot viewRoot;
 
 	@BeforeMethod
 	public void setUp() {
 		context = mock(FacesContext.class);
 		viewRoot = mock(UIViewRoot.class);
 		when(context.getViewRoot()).thenReturn(viewRoot);
 		externalContext = mock(ExternalContext.class);
 		when(context.getExternalContext()).thenReturn(externalContext);
 		attrs = new HashMap<Object, Object>();
 		when(context.getAttributes()).thenReturn(attrs);
 		manager = new DependenciesManager(context);
 	}
 
 	@Test
 	public void autoLoadShouldBeEnabledByDefault() {
 		when(externalContext.getInitParameter(DependenciesManager.TNT_AUTO_LOAD)).thenReturn(null);
 		assertTrue(manager.isAutoLoad());
 	}
 
 	@Test
 	public void shouldOmitEmptyAutoLoadParam() {
 		when(externalContext.getInitParameter(DependenciesManager.TNT_AUTO_LOAD)).thenReturn("");
 		assertTrue(manager.isAutoLoad());
 	}
 
 	@Test
 	public void autoLoadParamShouldBeCached() {
 		when(externalContext.getInitParameter(DependenciesManager.TNT_AUTO_LOAD)).thenReturn("false");
 		assertFalse(manager.isAutoLoad());
 		assertEquals(Boolean.FALSE, context.getAttributes().get(DependenciesManager.TNT_AUTO_LOAD));
 	}
 
 	@Test
 	public void autoLoadParamShouldBeInCache() {
 		attrs.put(DependenciesManager.TNT_AUTO_LOAD, false);
 		assertFalse(manager.isAutoLoad());
 		verify(externalContext, never()).getInitParameter(anyString());
 	}
 
 	@Test
 	public void shouldCreateNewInstanceAndPutIntoFacesContext() {
 		DependenciesManager newManager = DependenciesManager.getInstance(context);
 		assertEquals(newManager, attrs.get(DependenciesManager.TNT_DEPENDENCIES_MANAGER));
 	}
 
 	@Test
 	public void shouldReturnCachedInstance() {
 		DependenciesManager newManager = new DependenciesManager(context);
 		attrs.put(DependenciesManager.TNT_DEPENDENCIES_MANAGER, newManager);
 		assertEquals(DependenciesManager.getInstance(context), newManager);
 	}
 
 	@Test
 	public void shouldInsertTNTCSS() {
 		manager.insertTNTCSS();
 		verify(viewRoot).addComponentResource(eq(context), any(UIOutput.class), eq("head"));
 		assertTrue(attrs.containsKey("tnt.css.css"));
 	}
 
 	@Test
 	public void doNotUseAutoLoadShouldNotInsertCSS() {
 		when(externalContext.getInitParameter(DependenciesManager.TNT_AUTO_LOAD)).thenReturn("false");
 		manager.insertTNTCSS();
 		verify(viewRoot, never()).addComponentResource(eq(context), any(UIOutput.class), eq("head"));
 	}
 
 	@Test
 	public void shouldInsertMetaViewport() {
 		manager.insertMetaViewport();
 		verify(viewRoot).addComponentResource(eq(context), any(UIOutput.class), eq("head"));
 		assertTrue(attrs.containsKey("tnt.viewport.meta"));
 	}
 
 	@Test
 	public void metaViewportAlreadyInsertedDoNotInsertAgain() {
 		attrs.put("tnt.viewport.meta", true);
 		manager.insertMetaViewport();
 		verify(viewRoot, never()).addComponentResource(eq(context), any(UIOutput.class), eq("head"));
 	}

	@Test
	public void metaViewportShouldWorksWithPrimeFaces() {
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				Object[] args = invocationOnMock.getArguments();
				UIOutput viewport = (UIOutput)args[1];
				assertTrue(viewport.getAttributes().get("name").toString().endsWith(".js"), "Name must ends with .js, it's a workaround for PrimeFaces");
				return null;
			}
		}).when(viewRoot).addComponentResource(eq(context), any(UIOutput.class), eq("head"));

		manager.insertMetaViewport();

		verify(viewRoot).addComponentResource(eq(context), any(UIOutput.class), eq("head"));
		assertTrue(attrs.containsKey("tnt.viewport.meta"));
	}
 }
