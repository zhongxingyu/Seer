 package cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.dn;
 
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.IDataProvider;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.EntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.Role;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNFilterTemplateInstance;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRenameTemplateInstance;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNReplaceTemplateInstance;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRule;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRulesGroup;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNTemplateInstance;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.AuthorizedDeleteButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.AuthorizedDeleteTemplateInstanceButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.AuthorizedRedirectButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.BooleanLabel;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.CommitChangesButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.DeleteConfirmationMessage;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.LimitedEditingForm;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.RedirectWithParamButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.TruncatedLabel;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.UnobtrusivePagingNavigator;
 import cz.cuni.mff.odcleanstore.webfrontend.core.models.DependentDataProvider;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.CompiledDNRuleDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNFilterTemplateInstanceDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRenameTemplateInstanceDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNReplaceTemplateInstanceDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRulesGroupDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNTemplateInstanceDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.exceptions.DaoException;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.LimitedEditingPage;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.pipelines.TransformerAssignmentDetailPage;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.RulesGroupHelpPanel;
 
 @AuthorizeInstantiation({ Role.PIC })
 public class DNGroupDetailPage extends LimitedEditingPage
 {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(DNGroupDetailPage.class);
 	
 	private DNRulesGroupDao dnRulesGroupDao;
 	private DNRuleDao dnRuleDao;
 	private DNReplaceTemplateInstanceDao dnReplaceTemplateInstanceDao;
 	private DNRenameTemplateInstanceDao dnRenameTemplateInstanceDao;
 	private DNFilterTemplateInstanceDao dnFilterTemplateInstanceDao;
 	private CompiledDNRuleDao compiledDnRuleDao;
 
 	public DNGroupDetailPage(final Integer groupId) 
 	{
 		this(groupId, null);
 	}
 	
 	public DNGroupDetailPage(final Integer groupId, final Integer transformerInstanceId) 
 	{
 		super(
 			"Home > Backend > DN > Groups > Edit", 
 			"Edit DN rule group",
 			DNRulesGroupDao.class,
 			groupId
 		);
 		
 		// prepare DAO objects
 		//
 		dnRulesGroupDao = daoLookupFactory.getDao(DNRulesGroupDao.class);
 		dnRuleDao = daoLookupFactory.getDao(DNRuleDao.class, isEditable());
 		dnReplaceTemplateInstanceDao = daoLookupFactory.getDao(DNReplaceTemplateInstanceDao.class);
 		dnRenameTemplateInstanceDao = daoLookupFactory.getDao(DNRenameTemplateInstanceDao.class);
 		dnFilterTemplateInstanceDao = daoLookupFactory.getDao(DNFilterTemplateInstanceDao.class);
 		compiledDnRuleDao = daoLookupFactory.getDao(CompiledDNRuleDao.class);
 		
 		// register page components
 		//
 		addBackToPipelineLink(groupId, transformerInstanceId);
 		addHelpWindow("rulesGroupHelpWindow", "openRulesGroupHelpWindow", new RulesGroupHelpPanel("content"));
 		addHelpWindow("dnRuleHelpWindow", "openDNRuleHelpWindow", new DNRuleHelpPanel("content"));
 		addHelpWindow("dnReplaceTemplateInstanceHelpWindow", "openDNReplaceTemplateInstanceHelpWindow", new DNReplaceTemplateInstanceHelpPanel("content"));
 		addHelpWindow("dnRenameTemplateInstanceHelpWindow", "openDNRenameTemplateInstanceHelpWindow", new DNRenameTemplateInstanceHelpPanel("content"));
 		addHelpWindow("dnFilterTemplateInstanceHelpWindow", "openDNFilterTemplateInstanceHelpWindow", new DNFilterTemplateInstanceHelpPanel("content"));
 		
 		DNRulesGroup group = dnRulesGroupDao.load(groupId);
 		addEditDNRulesGroupForm(group);
 		addCommitChangesButton(group);
 		addDNRawRulesSection(groupId);
 		addDNReplaceTemplateInstancesSection(groupId);
 		addDNRenameTemplateInstancesSection(groupId);
 		addDNFilterTemplateInstancesSection(groupId);
 	}
 	
 	private void addBackToPipelineLink(Integer groupId, Integer transformerInstanceId) 
 	{
 		Map<Integer, Integer> navigationMap = getODCSSession().getDnPipelineRulesNavigationMap();
 		Integer linkTransformerId = null;
 		if (transformerInstanceId != null) 
 		{
 			linkTransformerId = transformerInstanceId;
 			navigationMap.put(groupId, transformerInstanceId);
 		}
 		else if (navigationMap.containsKey(groupId))
 		{
 			linkTransformerId = navigationMap.get(groupId);
 		}
 		
 		add(new AuthorizedRedirectButton(
 			TransformerAssignmentDetailPage.class,
 			linkTransformerId, 
 			linkTransformerId != null,
 			"backToPipelineLink"
 		));
 	}
 	
 	/*
 	 	=======================================================================
 	 	Implementace qaRulesTable
 	 	=======================================================================
 	*/
 	
 	private void addEditDNRulesGroupForm(final DNRulesGroup group)
 	{
 		IModel<DNRulesGroup> formModel = new CompoundPropertyModel<DNRulesGroup>(group);
 		
 		Form<DNRulesGroup> form = new LimitedEditingForm<DNRulesGroup>("editDNGroupForm", formModel, isEditable())
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmitImpl()
 			{
 				DNRulesGroup group = this.getModelObject();
 				
 				try {
 					dnRulesGroupDao.update(group);
 				}
 				catch (DaoException ex)
 				{	
 					logger.error(ex.getMessage(), ex);
 					getSession().error(ex.getMessage());
 					return;
 				}
 				catch (Exception ex)
 				{
 					logger.error(ex.getMessage(), ex);				
 					getSession().error(
 						"The group could not be updated due to an unexpected error."
 					);
 					
 					return;
 				}
 				
 				getSession().info("The group was successfuly updated.");
 			}
 		};
 		
 		form.add(createTextfield("label"));
 		form.add(createTextarea("description", false));
 		
 		add(form);
 	}
 	
 	private void addDNRawRulesSection(final Integer groupId) 
 	{
 		add(
 			new AuthorizedRedirectButton(
 				NewDNRulePage.class,
 				groupId, 
 				isEditable(),
 				"addNewRuleLink"
 			)
 		);
 		
 		addDNRawRulesTable(groupId);
 	}
 
 	private void addDNRawRulesTable(final Integer groupId)
 	{
 		IDataProvider<DNRule> data = new DependentDataProvider<DNRule>(dnRuleDao, "groupId", groupId);
 		
 		DataView<DNRule> dataView = new DataView<DNRule>("dnRawRulesTable", data)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(Item<DNRule> item) 
 			{
 				DNRule rule = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<DNRule>(rule));
 				
 				item.add(new TruncatedLabel("description", MAX_LIST_COLUMN_TEXT_LENGTH));
 				
 				item.add(createAuthorizedDeleteButton(
 					dnRuleDao,
 					rule,
 					"rule",
 					new DeleteConfirmationMessage("rule")
 					));
 				
 				
 				item.add(
 					new RedirectWithParamButton
 					(
 						DNRuleDetailPage.class,
 						rule.getId(),
 						"showEditDNRulePage"
 					)
 				);
 			}
 		};
 		
 		dataView.setItemsPerPage(ITEMS_PER_PAGE);
 		
 		add(dataView);
 		
 		add(new UnobtrusivePagingNavigator("rawRulesNavigator", dataView));
 	}
 	
 	private void addDNReplaceTemplateInstancesSection(final Integer groupId) 
 	{
 		add(
 			new AuthorizedRedirectButton(
 				NewDNReplaceTemplateInstancePage.class,
 				groupId, 
 				isEditable(),
 				"addNewReplaceTemplateInstanceLink"
 			)
 		);
 		
 		addDNReplaceTemplateInstancesTable(groupId);
 	}
 	
 	private void addDNReplaceTemplateInstancesTable(final Integer groupId)
 	{
 		IDataProvider<DNReplaceTemplateInstance> data = new DependentDataProvider<DNReplaceTemplateInstance>
 		(
 			dnReplaceTemplateInstanceDao, 
 			"groupId", 
 			groupId
 		);
 	
 		DataView<DNReplaceTemplateInstance> dataView = new DataView<DNReplaceTemplateInstance>("dnReplaceTemplateInstancesTable", data)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(Item<DNReplaceTemplateInstance> item) 
 			{
 				DNReplaceTemplateInstance instance = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<DNReplaceTemplateInstance>(instance));
 	
 				item.add(new TruncatedLabel("propertyName", MAX_LIST_COLUMN_TEXT_LENGTH));
 				item.add(new TruncatedLabel("pattern", MAX_LIST_COLUMN_TEXT_LENGTH));
 				item.add(new TruncatedLabel("replacement", MAX_LIST_COLUMN_TEXT_LENGTH));
 				
 				item.add(
					createAuthorizedDeleteTemplateInstanceButton(
 						dnReplaceTemplateInstanceDao,
 						compiledDnRuleDao,
 						instance,
 						"replaceTemplateInstance",
						new DeleteConfirmationMessage("replace template instance")
 					)
 				);
 				
 				item.add(
 					new RedirectWithParamButton
 					(
 						EditDNReplaceTemplateInstancePage.class,
 						instance.getId(),
 						"showEditDNReplaceTemplateInstancePage"
 					)
 				);
 			}
 		};
 		
 		dataView.setItemsPerPage(ITEMS_PER_PAGE);
 		
 		add(dataView);
 		
 		add(new UnobtrusivePagingNavigator("replaceTemplateInstancesNavigator", dataView));
 	}
 	
 	private void addDNRenameTemplateInstancesSection(final Integer groupId) 
 	{
 		add(
 			new AuthorizedRedirectButton(
 				NewDNRenameTemplateInstancePage.class,
 				groupId, 
 				isEditable(),
 				"addNewRenameTemplateInstanceLink"
 			)
 		);
 		
 		addDNRenameTemplateInstancesTable(groupId);
 	}
 	
 	private void addDNRenameTemplateInstancesTable(final Integer groupId)
 	{
 		IDataProvider<DNRenameTemplateInstance> data = new DependentDataProvider<DNRenameTemplateInstance>
 		(
 			dnRenameTemplateInstanceDao, 
 			"groupId", 
 			groupId
 		);
 	
 		DataView<DNRenameTemplateInstance> dataView = new DataView<DNRenameTemplateInstance>("dnRenameTemplateInstancesTable", data)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(Item<DNRenameTemplateInstance> item) 
 			{
 				DNRenameTemplateInstance instance = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<DNRenameTemplateInstance>(instance));
 	
 				item.add(new TruncatedLabel("sourcePropertyName", MAX_LIST_COLUMN_TEXT_LENGTH));
 				item.add(new TruncatedLabel("targetPropertyName", MAX_LIST_COLUMN_TEXT_LENGTH));
 				
 				item.add(
					createAuthorizedDeleteTemplateInstanceButton(
 						dnRenameTemplateInstanceDao,
 						compiledDnRuleDao,
 						instance,
 						"renameTemplateInstance",
						new DeleteConfirmationMessage("rename template instance")
 					)
 				);
 				
 				item.add(
 					new RedirectWithParamButton
 					(
 						EditDNRenameTemplateInstancePage.class,
 						instance.getId(),
 						"showEditDNRenameTemplateInstancePage"
 					)
 				);
 			}
 		};
 		
 		dataView.setItemsPerPage(ITEMS_PER_PAGE);
 		
 		add(dataView);
 		
 		add(new UnobtrusivePagingNavigator("renameTemplateInstancesNavigator", dataView));
 	}
 	
 	private void addDNFilterTemplateInstancesSection(final Integer groupId) 
 	{
 		add(
 			new AuthorizedRedirectButton(
 				NewDNFilterTemplateInstancePage.class,
 				groupId, 
 				isEditable(),
 				"addNewFilterTemplateInstanceLink"
 			)
 		);
 		
 		addDNFilterTemplateInstancesTable(groupId);
 	}
 	
 	private void addDNFilterTemplateInstancesTable(final Integer groupId)
 	{
 		IDataProvider<DNFilterTemplateInstance> data = new DependentDataProvider<DNFilterTemplateInstance>
 		(
 			dnFilterTemplateInstanceDao, 
 			"groupId", 
 			groupId
 		);
 	
 		DataView<DNFilterTemplateInstance> dataView = new DataView<DNFilterTemplateInstance>("dnFilterTemplateInstancesTable", data)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(Item<DNFilterTemplateInstance> item) 
 			{
 				DNFilterTemplateInstance instance = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<DNFilterTemplateInstance>(instance));
 	
 				item.add(new TruncatedLabel("propertyName", MAX_LIST_COLUMN_TEXT_LENGTH));
 				item.add(new TruncatedLabel("pattern", MAX_LIST_COLUMN_TEXT_LENGTH));
 				item.add(new BooleanLabel("keep"));
 				
 				item.add(
 					createAuthorizedDeleteTemplateInstanceButton
 					(
 						dnFilterTemplateInstanceDao,
 						compiledDnRuleDao,
 						instance,
 						"filterTemplateInstance",
 						new DeleteConfirmationMessage("filter template instance")
 					)
 				);
 				
 				item.add(
 					new RedirectWithParamButton
 					(
 						EditDNFilterTemplateInstancePage.class,
 						instance.getId(),
 						"showEditDNFilterTemplateInstancePage"
 					)
 				);
 			}
 		};
 		
 		dataView.setItemsPerPage(ITEMS_PER_PAGE);
 		
 		add(dataView);
 		
 		add(new UnobtrusivePagingNavigator("filterTemplateInstancesNavigator", dataView));
 	}
 	
 	private void addCommitChangesButton(final DNRulesGroup group)
 	{
 		add(new CommitChangesButton("commitChanges", group, dnRulesGroupDao)
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick()
 			{
 				super.onClick();
 				setResponsePage(new DNGroupDetailPage(group.getId()));
 			}
 		});
 	}
 	
 	private <BO extends EntityWithSurrogateKey> Component createAuthorizedDeleteButton(DaoForEntityWithSurrogateKey<BO> dao, final DNRule rule,
 		String objName, DeleteConfirmationMessage message)
 	{
 		return new AuthorizedDeleteButton<BO>(dao, rule.getId(), isEditable(), objName, message, null)
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick()
 			{
 				super.onClick();
 				setResponsePage(new DNGroupDetailPage(rule.getGroupId()));
 			}
 		};
 	}
 	
 	private <BO extends DNTemplateInstance> Component createAuthorizedDeleteTemplateInstanceButton(
 		DNTemplateInstanceDao<BO> templateInstanceDao, CompiledDNRuleDao compiledRuleDao, final BO instance, String objName, DeleteConfirmationMessage message)
 	{
 		return 
 			new AuthorizedDeleteTemplateInstanceButton<BO>(
 				templateInstanceDao, 
 				compiledRuleDao,
 				instance, 
 				isEditable(), 
 				objName, 
 				message, 
 				null
 			)
 			{
 				private static final long serialVersionUID = 1L;
 	
 				@Override
 				public void onClick()
 				{
 					super.onClick();
 					setResponsePage(new DNGroupDetailPage(instance.getGroupId()));
 				}
 			};
 	}
 }
