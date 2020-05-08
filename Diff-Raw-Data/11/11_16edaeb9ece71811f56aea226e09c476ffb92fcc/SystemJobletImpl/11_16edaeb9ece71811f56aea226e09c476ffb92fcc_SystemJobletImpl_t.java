 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.module;
 
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.orm.DatasourceProperties;
 import org.web4thejob.orm.ORMUtil;
 import org.web4thejob.orm.PanelDefinition;
 import org.web4thejob.orm.Path;
 import org.web4thejob.orm.parameter.*;
 import org.web4thejob.orm.query.Condition;
 import org.web4thejob.orm.query.Criterion;
 import org.web4thejob.orm.query.OrderBy;
 import org.web4thejob.orm.query.Query;
 import org.web4thejob.orm.scheme.RenderElement;
 import org.web4thejob.orm.scheme.RenderScheme;
 import org.web4thejob.orm.scheme.RenderSchemeUtil;
 import org.web4thejob.orm.scheme.SchemeType;
 import org.web4thejob.security.*;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.util.L10nMessages;
 import org.web4thejob.web.panel.*;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author Veniamin Isaias
  * @since 3.4.0
  */
 
 @Component
 public class SystemJobletImpl extends AbstractJoblet implements SystemJoblet {
 
 
     @Override
     protected String getPropertiesName() {
         return H4Module.class.getSimpleName() + ".properties";
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Exception> List<E> setup() {
         UserIdentity userIdentity = ContextUtil.getSecurityService().getAdministratorIdentity();
 
         Map<Panel, String> beanIds = new HashMap<Panel, String>();
 
         ListViewPanel panels = ContextUtil.getDefaultPanel(ListViewPanel.class);
         panels.setTargetType(PanelDefinition.class);
         panels.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(PanelDefinition
                 .class).getFriendlyName());
         beanIds.put(panels, ORMUtil.persistPanel(panels));
 
         ListViewPanel roles = ContextUtil.getDefaultPanel(ListViewPanel.class);
         roles.setTargetType(RoleIdentity.class);
         roles.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(RoleIdentity
                 .class).getFriendlyName());
         beanIds.put(roles, ORMUtil.persistPanel(roles));
 
         ListViewPanel users = ContextUtil.getDefaultPanel(ListViewPanel.class);
         users.setTargetType(UserIdentity.class);
         users.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(UserIdentity
                 .class).getFriendlyName());
         beanIds.put(users, ORMUtil.persistPanel(users));
 
         ListViewPanel policies = ContextUtil.getDefaultPanel(ListViewPanel.class);
         policies.setTargetType(AuthorizationPolicy.class);
         policies.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(AuthorizationPolicy
                 .class).getFriendlyName());
         beanIds.put(policies, ORMUtil.persistPanel(policies));
 
         FramePanel dashboard = ContextUtil.getDefaultPanel(FramePanel.class);
         dashboard.setSettingValue(SettingEnum.TARGET_URL, "http://web4thejob.sourceforge.net/dashboard/index.php");
         dashboard.setSettingValue(SettingEnum.PANEL_NAME, "My Dashboard");
         beanIds.put(dashboard, ORMUtil.persistPanel(dashboard));
 
         //----------------------------------------------------------------------------------------------------------
         //Parameters
         //----------------------------------------------------------------------------------------------------------
         ListViewPanel entityViewParameters = ContextUtil.getDefaultPanel(ListViewPanel.class);
         entityViewParameters.setTargetType(EntityTypeEntityViewParameter.class);
         entityViewParameters.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                 (EntityTypeEntityViewParameter.class).getFriendlyName());
         beanIds.put(entityViewParameters, ORMUtil.persistPanel(entityViewParameters));
 
         ListViewPanel listViewParameters = ContextUtil.getDefaultPanel(ListViewPanel.class);
         listViewParameters.setTargetType(EntityTypeListViewParameter.class);
         listViewParameters.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                 (EntityTypeListViewParameter.class).getFriendlyName());
         beanIds.put(listViewParameters, ORMUtil.persistPanel(listViewParameters));
 
         ListViewPanel queryParameters = ContextUtil.getDefaultPanel(ListViewPanel.class);
         queryParameters.setTargetType(EntityTypeQueryParameter.class);
         queryParameters.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                 (EntityTypeQueryParameter.class).getFriendlyName());
         beanIds.put(queryParameters, ORMUtil.persistPanel(queryParameters));
 
         ListViewPanel charsetParameters = ContextUtil.getDefaultPanel(ListViewPanel.class);
         charsetParameters.setTargetType(PrinterCharsetParameter.class);
         charsetParameters.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                 (PrinterCharsetParameter.class).getFriendlyName());
         beanIds.put(charsetParameters, ORMUtil.persistPanel(charsetParameters));
 
         ListViewPanel imageRepoParameters = ContextUtil.getDefaultPanel(ListViewPanel.class);
         imageRepoParameters.setTargetType(LocationImagesRepoParameter.class);
         imageRepoParameters.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                 (LocationImagesRepoParameter.class).getFriendlyName());
         beanIds.put(imageRepoParameters, ORMUtil.persistPanel(imageRepoParameters));
         //----------------------------------------------------------------------------------------------------------
 
         ListViewPanel renderSchemes = ContextUtil.getDefaultPanel(ListViewPanel.class);
         renderSchemes.setTargetType(RenderScheme.class);
         renderSchemes.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(RenderScheme
                 .class).getFriendlyName());
         beanIds.put(renderSchemes, ORMUtil.persistPanel(renderSchemes));
 
         ListViewPanel queries = ContextUtil.getDefaultPanel(ListViewPanel.class);
         queries.setTargetType(Query.class);
         queries.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(Query
                 .class).getFriendlyName());
         beanIds.put(queries, ORMUtil.persistPanel(queries));
 
         ModuleInfoPanel moduleInfoPanel = ContextUtil.getDefaultPanel(ModuleInfoPanel.class);
         moduleInfoPanel.setSettingValue(SettingEnum.PANEL_NAME, moduleInfoPanel.toString());
         beanIds.put(moduleInfoPanel, ORMUtil.persistPanel(moduleInfoPanel));
 
         ListViewPanel members = ContextUtil.getDefaultPanel(ListViewPanel.class);
         members.setTargetType(RoleMembers.class);
         members.setMasterType(UserIdentity.class);
         members.setBindProperty(RoleMembers.FLD_USER);
         members.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(RoleMembers
                 .class).getFriendlyName());
         RenderScheme scheme = RenderSchemeUtil.createDefaultRenderScheme(RoleMembers.class,
                 SchemeType.LIST_SCHEME);
         scheme.setName(scheme.getName() + "\\user bound");
         scheme.getElements().clear();
         RenderElement renderElement = scheme.addElement(ContextUtil.getMRS().getPropertyPath(RoleMembers
                 .class, new Path(RoleMembers.FLD_ROLE)));
         renderElement.setFriendlyName(ContextUtil.getMRS().getEntityMetadata(RoleIdentity.class).getFriendlyName());
         renderElement = scheme.addElement(ContextUtil.getMRS().getPropertyPath(RoleMembers.class,
                 new String[]{RoleMembers.FLD_ROLE, RoleIdentity.FLD_AUTHORIZATION_POLICY}));
         renderElement.setFriendlyName(ContextUtil.getMRS().getEntityMetadata(AuthorizationPolicy.class)
                 .getFriendlyName());
         ContextUtil.getDWS().save(scheme);
         members.setSettingValue(SettingEnum.RENDER_SCHEME_FOR_VIEW, scheme.getName());
 
 
         ListViewPanel elements = ContextUtil.getDefaultPanel(ListViewPanel.class);
         elements.setTargetType(RenderElement.class);
         elements.setMasterType(RenderScheme.class);
         elements.setBindProperty(RenderElement.FLD_RENDER_SCHEME);
         elements.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(RenderElement
                 .class).getFriendlyName());
         scheme = RenderSchemeUtil.createDefaultRenderScheme(RenderElement.class,
                 SchemeType.LIST_SCHEME, new String[]{RenderElement.FLD_RENDER_SCHEME});
         scheme.setName(scheme.getName() + "\\scheme bound");
         ContextUtil.getDWS().save(scheme);
         elements.setSettingValue(SettingEnum.RENDER_SCHEME_FOR_VIEW, scheme.getName());
 
         ListViewPanel criteria = ContextUtil.getDefaultPanel(ListViewPanel.class);
         criteria.setTargetType(Criterion.class);
         criteria.setMasterType(Query.class);
         criteria.setBindProperty(Criterion.FLD_QUERY);
         criteria.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(Criterion
                 .class).getFriendlyName());
         scheme = RenderSchemeUtil.createDefaultRenderScheme(Criterion.class,
                 SchemeType.LIST_SCHEME, new String[]{Criterion.FLD_QUERY});
         scheme.setName(scheme.getName() + "\\query bound");
         ContextUtil.getDWS().save(scheme);
         criteria.setSettingValue(SettingEnum.RENDER_SCHEME_FOR_VIEW, scheme.getName());
 
         ListViewPanel orderings = ContextUtil.getDefaultPanel(ListViewPanel.class);
         orderings.setTargetType(OrderBy.class);
         orderings.setMasterType(Query.class);
         orderings.setBindProperty(OrderBy.FLD_QUERY);
         orderings.setSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata(OrderBy
                 .class).getFriendlyName());
         scheme = RenderSchemeUtil.createDefaultRenderScheme(OrderBy.class,
                 SchemeType.LIST_SCHEME, new String[]{OrderBy.FLD_QUERY});
         scheme.setName(scheme.getName() + "\\query bound");
         ContextUtil.getDWS().save(scheme);
         orderings.setSettingValue(SettingEnum.RENDER_SCHEME_FOR_VIEW, scheme.getName());
 
         ContextUtil.getSessionContext().refresh();
 
         BorderedLayoutPanel userRoles = ContextUtil.getDefaultPanel(BorderedLayoutPanel.class);
         userRoles.setCenter(ContextUtil.getPanel(beanIds.get(users)));
         userRoles.setSouth(members);
         userRoles.setSettingValue(SettingEnum.NORTH_ENABLED, false);
         userRoles.setSettingValue(SettingEnum.EAST_ENABLED, false);
         userRoles.setSettingValue(SettingEnum.WEST_ENABLED, false);
         userRoles.setSettingValue(SettingEnum.CENTER_ENABLED, true);
         userRoles.setSettingValue(SettingEnum.SOUTH_ENABLED, true);
         userRoles.setSettingValue(SettingEnum.SOUTH_COLLAPSIBLE, true);
         userRoles.setSettingValue(SettingEnum.SOUTH_SPLITTABLE, true);
         userRoles.setSettingValue(SettingEnum.SOUTH_MERGE_COMMANDS, false);
         userRoles.setSettingValue(SettingEnum.SOUTH_HEIGHT, "50%");
         userRoles.setSettingValue(SettingEnum.PANEL_NAME, users.toString() + " & " + members.toString());
         userRoles.render();
         beanIds.put(userRoles, ORMUtil.persistPanel(userRoles));
 
         BorderedLayoutPanel schemeElements = ContextUtil.getDefaultPanel(BorderedLayoutPanel.class);
         schemeElements.setCenter(ContextUtil.getPanel(beanIds.get(renderSchemes)));
         schemeElements.setSouth(elements);
         schemeElements.setSettingValue(SettingEnum.NORTH_ENABLED, false);
         schemeElements.setSettingValue(SettingEnum.EAST_ENABLED, false);
         schemeElements.setSettingValue(SettingEnum.WEST_ENABLED, false);
         schemeElements.setSettingValue(SettingEnum.CENTER_ENABLED, true);
         schemeElements.setSettingValue(SettingEnum.SOUTH_ENABLED, true);
         schemeElements.setSettingValue(SettingEnum.SOUTH_COLLAPSIBLE, true);
         schemeElements.setSettingValue(SettingEnum.SOUTH_SPLITTABLE, true);
         schemeElements.setSettingValue(SettingEnum.SOUTH_MERGE_COMMANDS, false);
         schemeElements.setSettingValue(SettingEnum.SOUTH_HEIGHT, "50%");
         schemeElements.setSettingValue(SettingEnum.PANEL_NAME, renderSchemes.toString());
         schemeElements.render();
         beanIds.put(schemeElements, ORMUtil.persistPanel(schemeElements));
 
         BorderedLayoutPanel queriesElements = ContextUtil.getDefaultPanel(BorderedLayoutPanel.class);
         queriesElements.setCenter(ContextUtil.getPanel(beanIds.get(queries)));
         TabbedLayoutPanel tabbedLayoutPanel = ContextUtil.getDefaultPanel(TabbedLayoutPanel.class);
         tabbedLayoutPanel.setSettingValue(SettingEnum.DISABLE_DYNAMIC_TAB_TITLE, true);
         tabbedLayoutPanel.getSubpanels().add(criteria);
         tabbedLayoutPanel.getSubpanels().add(orderings);
         tabbedLayoutPanel.setSelectedIndex(0);
         queriesElements.setSouth(tabbedLayoutPanel);
         queriesElements.setSettingValue(SettingEnum.NORTH_ENABLED, false);
         queriesElements.setSettingValue(SettingEnum.EAST_ENABLED, false);
         queriesElements.setSettingValue(SettingEnum.WEST_ENABLED, false);
         queriesElements.setSettingValue(SettingEnum.CENTER_ENABLED, true);
         queriesElements.setSettingValue(SettingEnum.SOUTH_ENABLED, true);
         queriesElements.setSettingValue(SettingEnum.SOUTH_COLLAPSIBLE, true);
         queriesElements.setSettingValue(SettingEnum.SOUTH_SPLITTABLE, true);
         queriesElements.setSettingValue(SettingEnum.SOUTH_MERGE_COMMANDS, false);
         queriesElements.setSettingValue(SettingEnum.SOUTH_HEIGHT, "50%");
         queriesElements.setSettingValue(SettingEnum.PANEL_NAME, queries.toString());
         queriesElements.render();
         beanIds.put(queriesElements, ORMUtil.persistPanel(queriesElements));
 
         // The default desktop
        DesktopLayoutPanel desktop = ContextUtil.getBean(DesktopLayoutPanel.class);
         desktop.addTab(dashboard);
         desktop.render();
         Query adminRoleQuery = ContextUtil.getEntityFactory().buildQuery(RoleIdentity.class);
         adminRoleQuery.addCriterion(new Path(RoleIdentity.FLD_CODE), Condition.EQ, RoleIdentity.ROLE_ADMINISTRATOR);
         beanIds.put(desktop, ORMUtil.persistPanel(desktop, "Administrator's default Desktop", null,
                 (Identity) ContextUtil.getDRS()
                         .findUniqueByQuery(adminRoleQuery)));
 
         ContextUtil.getSessionContext().refresh();
 
         // ---------------------------------------------------------------------------------------------------
         // Authorization Menu
         // ---------------------------------------------------------------------------------------------------
         AuthorizationPolicyPanel policyPanel = ContextUtil.getDefaultPanel(AuthorizationPolicyPanel.class);
         policyPanel.render();
         MenuAuthorizationPanel menuAuthorizationPanel = policyPanel
                 .getMenuAuthorizationPanel();
         Object rootItem = menuAuthorizationPanel.getRootItem();
 
         Object panelsMenu = menuAuthorizationPanel.renderAddedMenu(rootItem,
                 L10nMessages.L10N_NAME_DEFAULT_PANELS_MENU.toString());
         menuAuthorizationPanel.renderAddedPanel(panelsMenu, ContextUtil.getPanel(beanIds.get(dashboard)));
         menuAuthorizationPanel.renderAddedPanel(panelsMenu, ContextUtil.getPanel(beanIds.get(panels)));
         menuAuthorizationPanel.renderAddedPanel(panelsMenu, ContextUtil.getPanel(beanIds.get(schemeElements)));
         menuAuthorizationPanel.renderAddedPanel(panelsMenu, ContextUtil.getPanel(beanIds.get(queriesElements)));
         menuAuthorizationPanel.renderAddedPanel(panelsMenu, ContextUtil.getPanel(beanIds.get(moduleInfoPanel)));
 
         Object securityMenu = menuAuthorizationPanel.renderAddedMenu(rootItem,
                 L10nMessages.L10N_NAME_DEFAULT_SECURITY_MENU.toString());
         menuAuthorizationPanel.renderAddedPanel(securityMenu, ContextUtil.getPanel(beanIds.get(userRoles)));
         menuAuthorizationPanel.renderAddedPanel(securityMenu, ContextUtil.getPanel(beanIds.get(policies)));
         menuAuthorizationPanel.renderAddedPanel(securityMenu, ContextUtil.getPanel(beanIds.get(roles)));
 
         Object paramsMenu = menuAuthorizationPanel.renderAddedMenu(rootItem,
                 L10nMessages.L10N_NAME_DEFAULT_PARAMETERS_MENU.toString());
         menuAuthorizationPanel.renderAddedPanel(paramsMenu, ContextUtil.getPanel(beanIds.get
                 (entityViewParameters)));
         menuAuthorizationPanel.renderAddedPanel(paramsMenu, ContextUtil.getPanel(beanIds.get(listViewParameters)));
         menuAuthorizationPanel.renderAddedPanel(paramsMenu, ContextUtil.getPanel(beanIds.get(queryParameters)));
         menuAuthorizationPanel.renderAddedPanel(paramsMenu, ContextUtil.getPanel(beanIds.get(charsetParameters)));
         menuAuthorizationPanel.renderAddedPanel(paramsMenu, ContextUtil.getPanel(beanIds.get(imageRepoParameters)));
 
 
         AuthorizationPolicy authorizationPolicy = ContextUtil.getEntityFactory().buildAuthorizationPolicy();
         authorizationPolicy.setName(L10nMessages.L10N_NAME_DEFAULT_ADMINISTRATORS_MENU.toString());
         authorizationPolicy.setDefinition(policyPanel.getDefinition());
         ContextUtil.getDWS().save(authorizationPolicy);
 
         RoleIdentity role = userIdentity.getRoles().iterator().next().getRole();
         role.setAuthorizationPolicy(authorizationPolicy);
         ContextUtil.getDWS().save(role);
 
         //--------------------------------------------------------------------------------------------------------
         // ANYONE role and default query for sorting identities
         //--------------------------------------------------------------------------------------------------------
         role = ContextUtil.getEntityFactory().buildRoleIdentity();
         role.setCode("ANYONE");
         role.setIndex(1000);
         ContextUtil.getDWS().save(role);
 
         RoleMembers roleMembers = ContextUtil.getEntityFactory().buildRoleMembers();
         roleMembers.setRole(role);
         roleMembers.setUser(userIdentity);
         ContextUtil.getDWS().save(roleMembers);
 
         Query query = ContextUtil.getEntityFactory().buildQuery(Identity.class);
         query.setName("by code");
         query.setCached(true);
         query.addOrderBy(new Path(Identity.FLD_CODE));
         ContextUtil.getDWS().save(query);
 
         EntityTypeQueryParameter entityTypeQueryParameter = ContextUtil.getEntityFactory().buildParameter
                 (EntityTypeQueryParameter.class);
         entityTypeQueryParameter.setOwner(role);
         entityTypeQueryParameter.setKey(Identity.class.getCanonicalName());
         entityTypeQueryParameter.setValue(Long.valueOf(query.getId()).toString());
         ContextUtil.getDWS().save(entityTypeQueryParameter);
 
 
         return super.setup();
     }
 
     @Override
     public String getName() {
         return "System Joblet";
     }
 
     @Override
     public String getProjectUrl() {
         return "http://wiki.web4thejob.org/miscel/glossary/system_joblet";
     }
 
     @Override
     public int getOrdinal() {
         return 5;
     }
 
     @Override
     public List<Resource> getResources() {
         List<Resource> resources = new ArrayList<Resource>();
 
         PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
         try {
             for (Resource resource : resolver.getResources("classpath*:org/web4thejob/orm/**/*.hbm.xml")) {
 
                 if (resource.getFilename().equals("AuxiliaryDatabaseObjects.hbm.xml"))
                     continue;
 
                 resources.add(resource);
             }
         } catch (IOException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
 
         return resources;
     }
 
     @Override
     public String getBasePackage() {
         return "org.web4thejob.orm";
     }
 
     @Override
     public boolean isInstalled() {
 
         Properties datasource = new Properties();
         try {
             datasource.load(new ClassPathResource(DatasourceProperties.PATH).getInputStream());
         } catch (IOException e) {
             return false;
         }
 
         return StringUtils.hasText(datasource.getProperty(DatasourceProperties.INSTALLED));
     }
 
     @Override
     public String[] getSchemas() {
         return new String[]{"w4tj"};
     }
 }
