 package org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Map;
 
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.ext.mvp.IConfigurablePresenter;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.ext.mvp.IContainerItemPresenter;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences.view.IPreferencesEditorView;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences.view.PreferencesEditorView;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences.view.PreferencesEditorView.ICreatePreferenceListener;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences.view.PreferencesEditorView.IDeletePreferenceListener;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences.view.PreferencesEditorView.IEditPreferenceListener;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.preferences.view.PreferencesEditorView.ISavePreferenceListener;
 import org.flowframe.kernel.common.mdm.dao.services.preferences.IEntityPreferenceDAOService;
 import org.flowframe.kernel.common.mdm.domain.BaseEntity;
 import org.flowframe.kernel.common.mdm.domain.preferences.EntityPreferenceItem;
import org.flowframe.ui.services.contribution.IMainApplication;
 import org.flowframe.ui.services.factory.IComponentFactory;
 import org.flowframe.ui.services.transaction.ITransactionCompletionListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vaadin.mvp.presenter.BasePresenter;
 import org.vaadin.mvp.presenter.annotation.Presenter;
 
 import com.vaadin.addon.jpacontainer.EntityItem;
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.addon.jpacontainer.JPAContainerItem;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.data.util.filter.Compare.Equal;
 
 @Presenter(view = PreferencesEditorView.class)
 public class PreferencesEditorPresenter extends BasePresenter<IPreferencesEditorView, PreferencesEditorEventBus> implements IContainerItemPresenter, IConfigurablePresenter {
 	private static final String TRANSACTION_ID = "ff.editor.preferences";
 
 	protected Logger logger = LoggerFactory.getLogger(this.getClass());
 	private boolean initialized = false;
 	private Item newEntityItem;
 	private BaseEntity bean;
 	private IMainApplication mainApplication;
 	private JPAContainer<EntityPreferenceItem> container;
 
 	@SuppressWarnings("unchecked")
 	private void initialize() {
 		this.container = (JPAContainer<EntityPreferenceItem>) this.mainApplication.createPersistenceContainer(EntityPreferenceItem.class);
 
 		ICreatePreferenceListener createListener = new ICreatePreferenceListener() {
 
 			@Override
 			public void onCreatePreference() {
 				PreferencesEditorPresenter.this.newEntityItem = PreferencesEditorPresenter.this.container.createEntityItem(new EntityPreferenceItem());
 				PreferencesEditorPresenter.this.getView().create(PreferencesEditorPresenter.this.newEntityItem);
 			}
 		};
 		IEditPreferenceListener editListener = new IEditPreferenceListener() {
 
 			@Override
 			public void onEditPreference(Item item) {
 				BeanItem<EntityPreferenceItem> beanItem = new BeanItem<EntityPreferenceItem>(((JPAContainerItem<EntityPreferenceItem>) item).getEntity());
 				PreferencesEditorPresenter.this.getView().edit(beanItem);
 			}
 		};
 		ISavePreferenceListener saveListener = new ISavePreferenceListener() {
 
 			@Override
 			public void onSavePreference(final EntityPreferenceItem preferenceItem) {
 				try {
 					PreferencesEditorPresenter.this.mainApplication.runInTransaction(TRANSACTION_ID, new SavePreferenceDelegate(preferenceItem), new ITransactionCompletionListener() {
 
 						@Override
 						public void onTransactionCompleted() {
 							PreferencesEditorPresenter.this.container.refreshItem(preferenceItem.getId());
 						}
 
 					});
 				} catch (Exception e) {
 					StringWriter sw = new StringWriter();
 					e.printStackTrace(new PrintWriter(sw));
 					String stackTrace = sw.toString();
 
 					PreferencesEditorPresenter.this.mainApplication.showError("Could not save Preference", e.getMessage(), stackTrace);
 				}
 			}
 		};
 		IDeletePreferenceListener deleteListener = new IDeletePreferenceListener() {
 
 			@Override
 			public void onDeletePreference(EntityPreferenceItem item) {
 				try {
 					PreferencesEditorPresenter.this.mainApplication.runInTransaction(TRANSACTION_ID, new DeletePreferenceDelegate(item));
 				} catch (Exception e) {
 					StringWriter sw = new StringWriter();
 					e.printStackTrace(new PrintWriter(sw));
 					String stackTrace = sw.toString();
 
 					PreferencesEditorPresenter.this.mainApplication.showError("Could not delete Preference", e.getMessage(), stackTrace);
 				}
 			}
 		};
 
 		this.getView().addCreatePreferenceListener(createListener);
 		this.getView().addDeletePreferenceListener(deleteListener);
 		this.getView().addSavePreferenceListener(saveListener);
 		this.getView().addEditPreferenceListener(editListener);
 
 		String[] columnPropertyIds = new String[] { "preferenceKey", "preferenceValue", "dateCreated" };
 		String[] columnNames = new String[] { "Name", "Value", "Date Created" };
 
 		this.getView().showContent();
 		this.getView().setContainerDataSource(this.container, columnPropertyIds, columnNames);
 
 		this.initialized = true;
 	}
 
 	@Override
 	public void onSetItemDataSource(Item item, Container... container) throws Exception {
 		Object bean = getBean(item);
 		assert (item instanceof BaseEntity) : "The item bean was not of type BaseEntity.";
 		this.bean = (BaseEntity) bean;
 		assert (this.bean.getId() != null) : "The item data source entity id was null";
 
 		if (!this.initialized) {
 			initialize();
 		}
 
 		clearFilters();
 		applyFilter();
 	}
 
 	public void onPreferenceAdded(EntityPreferenceItem preference) {
 		this.container.addEntity(preference);
 	}
 	
 	@Override
 	public void onConfigure(Map<String, Object> params) throws Exception {
 		this.mainApplication = (IMainApplication) params.get(IComponentFactory.FACTORY_PARAM_MAIN_APP);
 	}
 
 
 	private void clearFilters() {
 		this.container.removeAllContainerFilters();
 	}
 
 	private void applyFilter() {
 		if (this.bean.getPreferences() != null) {
 			Equal filter = new Equal("parentEntityPreference.id", this.bean.getPreferences().getId());
 			this.container.addContainerFilter(filter);
 			this.container.applyFilters();
 		} else {
 			Equal filter = new Equal("parentEntityPreference.id", -1);
 			this.container.addContainerFilter(filter);
 			this.container.applyFilters();
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	private Object getBean(Item item) {
 		if (item instanceof EntityItem) {
 			return ((EntityItem) item).getEntity();
 		} else if (item instanceof BeanItem) {
 			return ((BeanItem) item).getBean();
 		} else {
 			return null;
 		}
 	}
 
 	private class SavePreferenceDelegate implements Runnable {
 		private EntityPreferenceItem preferenceItem;
 
 		public SavePreferenceDelegate(EntityPreferenceItem preferenceItem) {
 			this.preferenceItem = preferenceItem;
 		}
 
 		@Override
 		public void run() {
 			IEntityPreferenceDAOService preferenceDao = PreferencesEditorPresenter.this.mainApplication.findDAOByClass(IEntityPreferenceDAOService.class);
 			assert (preferenceDao != null) : "Preferences DAO was null.";
 			preferenceDao.updateItem(this.preferenceItem);
 			PreferencesEditorPresenter.this.getView().save();
 		}
 
 	}
 
 	private class DeletePreferenceDelegate implements Runnable {
 		private EntityPreferenceItem preferenceItem;
 
 		public DeletePreferenceDelegate(EntityPreferenceItem preferenceItem) {
 			this.preferenceItem = preferenceItem;
 		}
 
 		@Override
 		public void run() {
 			IEntityPreferenceDAOService preferenceDao = PreferencesEditorPresenter.this.mainApplication.findDAOByClass(IEntityPreferenceDAOService.class);
 			assert (preferenceDao != null) : "Preferences DAO was null.";
 			preferenceDao.deleteItem(this.preferenceItem);
 			PreferencesEditorPresenter.this.getView().delete();
 		}
 
 	}
 }
