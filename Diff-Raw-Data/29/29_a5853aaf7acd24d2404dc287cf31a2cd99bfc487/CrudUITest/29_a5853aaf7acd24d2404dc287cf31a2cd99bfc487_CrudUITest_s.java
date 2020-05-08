 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.isA;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.net.MalformedURLException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.portlet.PortletMode;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.mock.web.portlet.MockPortalContext;
 import org.springframework.mock.web.portlet.MockPortletURL;
 
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.vaadin.server.Page;
 import com.vaadin.server.WebBrowser;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 
 import de.unioninvestment.eai.portal.portlet.crud.config.PortletConfig;
 import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.ShowPopupEvent;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelBuilder;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelFactory;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.presenters.PortletPresenter;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.presenters.PresenterFactory;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.presenters.configuration.PortletConfigurationPresenter;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.PortletView;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.configuration.PortletConfigurationView;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.BusinessExceptionMessage;
 import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptModelBuilder;
 import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptModelFactory;
 import de.unioninvestment.eai.portal.portlet.crud.services.ConfigurationService;
 import de.unioninvestment.eai.portal.portlet.test.commons.SpringPortletContextTest;
 import de.unioninvestment.eai.portal.support.vaadin.junit.LiferayContext;
 import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
 
 public class CrudUITest extends SpringPortletContextTest {
 
 	@InjectMocks
	private CrudUI app = new CrudUI();
 
 	@Mock
 	private Settings settingsMock;
 	@Mock
 	private Portlet portletMock;
 	@Mock
 	private PortletPresenter portletPresenterMock;
 	@Mock
 	private PortletView portletViewMock;
 	@Mock
 	private ConfigurationService configurationServiceMock;
 	@Mock
 	private GuiBuilder guiBuilderMock;
 
 	@Mock
 	private EventBus eventBusMock;
 
 	@Mock
 	private ModelBuilder modelBuilderMock;
 	@Mock
 	private PresenterFactory presenterFactoryMock;
 	@Mock
 	private PortletConfigurationPresenter portletConfigurationPresenterMock;
 	@Mock
 	private PortletConfigurationView portletConfigurationViewMock;
 
 	@Mock(name = "viewPage")
 	private VerticalLayout viewPageMock;
 
 	@Mock(name = "editPage")
 	private ComponentContainer editPageMock;
 
 	@Mock
 	private transient ScriptModelFactory scriptModelFactoryMock;
 
 	@Mock
 	ScriptModelBuilder scriptModelBuilderMock;
 
 	@Mock
 	private ModelFactory modelFactoryMock;
 
 	@Mock
 	private Config configMock;
 
 	@Mock
 	private ThemeDisplay themeDisplayMock;
 
 	@Mock
 	private RenderRequest renderRequestMock;
 
 	@Mock
 	private RenderResponse renderResponseMock;
 
 	@Mock
 	private WebBrowser browserMock;
 
 	@Rule
 	public LiferayContext liferayContext = new LiferayContext("4711", 14008L);
 
 	@Before
 	public void setUp() throws MalformedURLException {
 		MockitoAnnotations.initMocks(this);
 
 		when(settingsMock.getHelpUrl()).thenReturn("http://help.us");
 		when(
 				((ResourceResponse) liferayContext.getPortletResponseMock())
 						.createRenderURL()).thenReturn(
 				new MockPortletURL(new MockPortalContext(), "myurl"));
 		when(
 				modelFactoryMock.getBuilder(isA(EventBus.class),
 						isA(Config.class))).thenReturn(modelBuilderMock);
 		when(modelBuilderMock.build()).thenReturn(portletMock);
 
 		when(presenterFactoryMock.portletConfigurationPresenter()).thenReturn(
 				portletConfigurationPresenterMock);
 		when(portletConfigurationPresenterMock.getView()).thenReturn(
 				portletConfigurationViewMock);
 		when(portletPresenterMock.getView()).thenReturn(portletViewMock);
 		when(configurationServiceMock.getPortletConfig("4711", 14008))
 				.thenReturn(configMock);
 
 		when(
 				liferayContext.getPortletRequestMock().getAttribute(
 						WebKeys.PORTLET_ID)).thenReturn("4711");
 		when(
 				liferayContext.getPortletRequestMock().getAttribute(
 						WebKeys.THEME_DISPLAY)).thenReturn(themeDisplayMock);
 		when(themeDisplayMock.getScopeGroupId()).thenReturn(14008L);
 
 		when(Page.getCurrent().getWebBrowser()).thenReturn(browserMock);
 		when(browserMock.getLocale()).thenReturn(Locale.GERMANY);
 
 	}
 
 	/**
 	 * Bereitet die UserPrincipals und das Userrepository für die Tests vor,
 	 * sodass ein NamedUser mit den übergebenen Rollen ausgestattet ist.
 	 * 
 	 * @param roles
 	 */
 	private void provideUserWithRoles(String... roles) {
 
 		when(liferayContext.getVaadinPortletRequestMock().getRemoteUser())
 				.thenReturn("carsten");
 
 		final Set<String> roleSet = roles == null ? null : new HashSet<String>(
 				Arrays.asList(roles));
 		when(
 				liferayContext.getVaadinPortletRequestMock().isUserInRole(
 						isA(String.class))).thenAnswer(new Answer<Boolean>() {
 			@Override
 			public Boolean answer(InvocationOnMock invocation) throws Throwable {
 				return roleSet != null
 						&& roleSet.contains(invocation.getArguments()[0]);
 			}
 		});
 	}
 
 	@Test
 	public void shouldApplyPreferredBrowserLocale() {
 		app.init(liferayContext.getVaadinPortletRequestMock());
 
 		assertThat(app.getLocale(), is(Locale.GERMANY));
 	}
 
 	@Test
 	public void shouldRegisterFallbackErrorHandler() {
 		provideUserWithRoles();
 		initializeUI();
 
 		verify(liferayContext.getVaadinSessionMock()).setErrorHandler(isA(CrudErrorHandler.class));
 	}
 
 	private void initializeUI() {
 		app.init(liferayContext.getVaadinPortletRequestMock());
 	}
 
 	@Test
 	@Ignore
 	public void shouldDisplayUnconfiguredMessage() {
 		initializeUI();
 
 	}
 
 	@Test
 	public void shouldDisplayMessageOnConfigurationError() {
 		PortletConfig portletConfig = new PortletConfig();
 
 		when(configMock.getPortletConfig()).thenReturn(portletConfig);
 		when(
 				configurationServiceMock.isConfigured(configMock,
 						liferayContext.getPortletPreferencesMock()))
 				.thenReturn(true);
 
 		when(modelBuilderMock.build()).thenThrow(new RuntimeException("bla"));
 
 		ArgumentCaptor<Component> components = ArgumentCaptor
 				.forClass(Component.class);
 
 		initializeUI();
 
 		verify(viewPageMock, times(3)).addComponent(components.capture());
 		// PortletId
 		assertThat(components.getAllValues().get(0), instanceOf(Label.class));
 		// CommunityId
 		assertThat(components.getAllValues().get(1), instanceOf(Label.class));
 		assertThat(components.getAllValues().get(2),
 				instanceOf(BusinessExceptionMessage.class));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void shouldDisplayPortletViewForExistingConfiguration() {
 		initializeUI();
 
 		PortletConfig portletConfig = new PortletConfig();
 		when(
 				configurationServiceMock.isConfigured(configMock,
 						liferayContext.getPortletPreferencesMock()))
 				.thenReturn(true);
 
 		when(configMock.getPortletConfig()).thenReturn(portletConfig);
 		when(modelBuilderMock.build()).thenReturn(portletMock);
 		when(guiBuilderMock.build(portletMock))
 				.thenReturn(portletPresenterMock);
 		when(portletPresenterMock.getView()).thenReturn(portletViewMock);
 
 		when(
 				scriptModelFactoryMock.getBuilder(any(EventBus.class),
 						any(Portlet.class),
 						any((new HashMap<Object, Object>()).getClass())))
 				.thenReturn(scriptModelBuilderMock);
 
 		app.refreshViews();
 
 		verify(viewPageMock).addComponent(portletViewMock);
 	}
 
 	@Test
 	public void testHandleResourceRequest() {
 		initializeUI();
 		expectWindowContentChange(PortletMode.EDIT, app.getEditContent());
 		expectWindowContentChange(PortletMode.VIEW, app.getViewContent());
 		expectWindowContentChange(PortletMode.HELP, app.getHelpContent());
 	}
 
 	private void expectWindowContentChange(PortletMode targetMode,
 			ComponentContainer expectedContent) {
 		when(liferayContext.getPortletRequestMock().getPortletMode())
 				.thenReturn(targetMode);
 		callHandleResourceRequest();
 		assertThat(app.getContent(), is((Component) expectedContent));
 	}
 
 	private void callHandleResourceRequest() {
 		app.handleResourceRequest(
 				(ResourceRequest) liferayContext.getPortletRequestMock(),
 				(ResourceResponse) liferayContext.getPortletResponseMock(), app);
 	}
 
 	@Test
 	public void shouldRefreshEditPageBeforeEntering() {
 		initializeUI();
 		app.setPortletDomain(portletMock);
 
 		when(liferayContext.getPortletRequestMock().getPortletMode())
 				.thenReturn(PortletMode.VIEW);
 		callHandleResourceRequest();
 		reset(portletConfigurationPresenterMock);
 
 		when(liferayContext.getPortletRequestMock().getPortletMode())
 				.thenReturn(PortletMode.EDIT);
 		callHandleResourceRequest();
 		verify(portletConfigurationPresenterMock).refresh(configMock);
 	}
 
 	@Test
 	public void shouldNotRefreshIfAlreadyOnEditPage() {
 		initializeUI();
 		app.setPortletDomain(portletMock);
 
 		when(liferayContext.getPortletRequestMock().getPortletMode())
 				.thenReturn(PortletMode.EDIT);
 		callHandleResourceRequest();
 		reset(portletConfigurationPresenterMock);
 
 		callHandleResourceRequest();
 		verify(portletConfigurationPresenterMock, never()).refresh(
 				any(Config.class));
 	}
 
 	@Test
 	public void shouldRegisterPopupEventHandler() {
 		initializeUI();
 
 		verify(eventBusMock).addHandler(ShowPopupEvent.class, app);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void shouldProvideInformationIfInitializationIsInProgress() {
 
 		assertThat(app.isInitializing(), is(true));
 
 		initializeUI();
 
 		final PortletConfig portletConfig = new PortletConfig();
 		when(
 				configurationServiceMock.isConfigured(configMock,
 						liferayContext.getPortletPreferencesMock()))
 				.thenReturn(true);
 
 		when(configMock.getPortletConfig()).thenAnswer(
 				new Answer<PortletConfig>() {
 					@Override
 					public PortletConfig answer(InvocationOnMock invocation)
 							throws Throwable {
 						assertThat(app.isInitializing(), is(true));
 						return portletConfig;
 					}
 				});
 		when(modelBuilderMock.build()).thenAnswer(new Answer<Portlet>() {
 			@Override
 			public Portlet answer(InvocationOnMock invocation) throws Throwable {
 				assertThat(app.isInitializing(), is(true));
 				return portletMock;
 			}
 		});
 
 		when(guiBuilderMock.build(portletMock)).thenAnswer(
 				new Answer<PortletPresenter>() {
 					@Override
 					public PortletPresenter answer(InvocationOnMock invocation)
 							throws Throwable {
 						assertThat(app.isInitializing(), is(true));
 						return portletPresenterMock;
 					}
 
 				});
 		when(portletPresenterMock.getView()).thenReturn(portletViewMock);
 
 		when(
 				scriptModelFactoryMock.getBuilder(any(EventBus.class),
 						any(Portlet.class),
 						any((Map.class))))
 				.thenAnswer(new Answer<ScriptModelBuilder>() {
 					@Override
 					public ScriptModelBuilder answer(InvocationOnMock invocation)
 							throws Throwable {
 						assertThat(app.isInitializing(), is(true));
 						return scriptModelBuilderMock;
 					}
 				});
 
 		app.refreshViews();
 
 		verify(viewPageMock).addComponent(portletViewMock);
 		assertThat(app.isInitializing(), is(false));
 
 	}
 
 	@Test
 	public void shouldNotInformPortletDomainAboutReloadOnFirstRenderRequest() {
 		initializeUI();
 		app.setPortletDomain(portletMock);
 		app.initializing = false;
 
 		app.handleRenderRequest(renderRequestMock, renderResponseMock, app);
 
 		verify(portletMock, never()).handleReload();
 	}
 
 	@Test
 	public void shouldInformPortletDomainAboutReloadOnSecondRenderRequest() {
 		initializeUI();
 		app.setPortletDomain(portletMock);
 		app.initializing = false;
 		when(renderRequestMock.getPortletMode()).thenReturn(PortletMode.VIEW);
 		app.handleRenderRequest(renderRequestMock, renderResponseMock, app);
 
 		app.handleRenderRequest(renderRequestMock, renderResponseMock, app);
 
 		verify(portletMock, times(1)).handleReload();
 	}
 
 	@Test
 	public void shouldNotInformPortletDomainAboutReloadIfPortletModeIsNotVIEW() {
 		initializeUI();
 		app.setPortletDomain(portletMock);
 		app.initializing = false;
 		when(renderRequestMock.getPortletMode()).thenReturn(PortletMode.EDIT);
 		app.handleRenderRequest(renderRequestMock, renderResponseMock, app);
 
 		app.handleRenderRequest(renderRequestMock, renderResponseMock, app);
 
 		verify(portletMock, never()).handleReload();
 	}
 
 	@Test
 	public void shouldNotInformPortletDomainAboutReloadOnInitializingRenderRequest() {
 		app.handleRenderRequest(renderRequestMock, renderResponseMock, app);
 		verify(portletMock, never()).handleReload();
 	}
 
 }
