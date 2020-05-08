 package org.romaframework.module.users;
 
 import org.romaframework.aspect.authentication.UserObjectPermissionListener;
 import org.romaframework.aspect.persistence.PersistenceAspect;
 import org.romaframework.aspect.persistence.QueryByFilter;
 import org.romaframework.core.Roma;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.module.users.domain.BaseAccount;
 import org.romaframework.module.users.domain.CustomFunction;
 import org.romaframework.module.users.domain.CustomProfiling;
 import org.romaframework.module.users.repository.CustomProfilingRepository;
 
 public class CustomProfiler implements UserObjectPermissionListener {
 
 	private static final String	SESSION_KEY							= "SESSION_CUSTOM_PROFILING";
 	private static final String	NONEXISTENT_SESSION_KEY	= "NOTEXIST_SESSION_CUSTOM_PROFILING";
 
 	public CustomProfiler() {
 		Controller.getInstance().registerListener(UserObjectPermissionListener.class, this);
 	}
 
 	public CustomProfiling getCustomProfiling() {
 		CustomProfiling customProfiling = (CustomProfiling) Roma.session().getProperty(SESSION_KEY);
 		if (Roma.session().getActiveSessionInfo().getAccount() == null) {
 			return null;
 		}
 		if (customProfiling == null) {
 			if (Roma.session().getProperty(NONEXISTENT_SESSION_KEY) != null)
 				return null;
 			QueryByFilter filter = new QueryByFilter(CustomProfiling.class);
 			filter.setStrategy(PersistenceAspect.STRATEGY_DETACHING);
 			filter.addItem("account", QueryByFilter.FIELD_EQUALS, Roma.session().getActiveSessionInfo().getAccount());
 			customProfiling = Roma.component(CustomProfilingRepository.class).findFirstByCriteria(filter);
 			if (customProfiling == null) {
 				Roma.session().setProperty(NONEXISTENT_SESSION_KEY, new Object());
 			} else {
 				Roma.session().setProperty(SESSION_KEY, customProfiling);
 			}
 		}
 		return customProfiling;
 	}
 
 	public boolean allow(String function) {
 		CustomProfiling customProfiling = getCustomProfiling();
 		if (customProfiling != null) {
 			CustomFunction customFunction = customProfiling.getFunctions().get(function);
 			if (customFunction != null) {
 				if (Boolean.FALSE.equals(customFunction.isAllow()))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	public CustomProfiling createCustomProfiling() {
 		CustomProfiling customProfiling = new CustomProfiling();
 		customProfiling.setAccount((BaseAccount) Roma.session().getActiveSessionInfo().getAccount());
 		return customProfiling;
 	}
 
 	public boolean allowAction(SchemaAction action) {
		return allow(action.getEntity().getName() + "." + action.getName());
 	}
 
 	public boolean allowClass(SchemaClass clazz) {
 		return allow(clazz.getName());
 	}
 
 	public boolean allowEvent(SchemaEvent event) {
		return allow(event.getEntity().getName() + "." + event.getName());
 	}
 
 	public boolean allowField(SchemaField field) {
		return allow(field.getEntity().getName() + "." + field.getName());
 	}
 
 	public void setCustomProfiling(CustomProfiling profiling) {
 		Roma.session().setProperty(SESSION_KEY, profiling);
 		Roma.session().setProperty(NONEXISTENT_SESSION_KEY, null);
 	}
 }
