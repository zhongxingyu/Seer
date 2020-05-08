 package com.redshape.servlet.core.controllers.registry;
 
 import com.redshape.servlet.core.controllers.Action;
 import com.redshape.servlet.core.controllers.IAction;
 import com.redshape.servlet.core.controllers.loaders.IActionsLoader;
 import com.redshape.utils.Commons;
 import org.apache.commons.collections.FastHashMap;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Registry to handle action objects and their mappings on income
  * requests path.
  *
  * @author Cyril A. Karpenko <self@nikelin.ru>
  */
 public class ControllersRegistry implements IControllersRegistry, ApplicationContextAware{
     private Map<Integer, Class<? extends IAction>> actions = new FastHashMap();
 	private Map<Integer, IAction> registry = new FastHashMap();
 
     private ApplicationContext context;
 	private IActionsLoader loader;
 
     public ControllersRegistry() {
     	this( (Set<Class<? extends IAction>>) null);
     }
 
 	public ControllersRegistry( Set<Class<? extends IAction>> actions ) {
 		super();
 
 		this.addActions(actions);
 	}
 
 
     public ControllersRegistry( IActionsLoader loader ) {
     	super();
 
 		this.loader = loader;
 		this.init();
     }
 
 	protected void init() {
 		if ( this.getLoader() == null ) {
 			return;
 		}
 
 		this.addActions(this.getLoader().load());
 	}
 
 	protected void addActions( Set<Class<? extends IAction>> actions ) {
 		for ( Class<? extends IAction> action : actions ) {
 			this.addAction( action );
 		}
 	}
 
 	private int generateHashId( Class<? extends IAction> action ) {
 		Action actionMeta = action.getAnnotation(Action.class);
 		if ( actionMeta == null ) {
 			return -1;
 		}
 
 		return generateHashId( actionMeta.controller(), actionMeta.name() );
 	}
 
 	private int generateHashId( String controllerName, String actionName ) {
 		return ( this.normalize( controllerName + "/" + actionName ) ).hashCode();
 	}
 
 	protected IActionsLoader getLoader() {
 		return loader;
 	}
 
 	@Override
     public String getViewPath( IAction action ) {
         Action actionMeta = action.getClass().getAnnotation( Action.class );
         if ( actionMeta == null ) {
             return null;
         }
 
         String viewPath = actionMeta.view();
         if ( viewPath == null || viewPath.isEmpty() ) {
             return null;
         }
 
         return viewPath;
     }
 
     @Override
     public void setApplicationContext( ApplicationContext context ) {
     	this.context = context;
     }
     
     protected ApplicationContext getContext() {
     	return this.context;
     }
     
     @Override
 	public void addAction( Class<? extends IAction> actionClazz ) {
         int id = this.generateHashId(actionClazz);
 		if ( id == -1 ) {
 			return;
 		}
 
 		this.actions.put(id, actionClazz);
     }
 
     @Override
 	public Collection<Class<? extends IAction> > getActions() {
         return this.actions.values();
     }
 
     @Override
     public IAction getInstance( String controller, String action ) throws InstantiationException {
 		int hashCode = this.generateHashId( controller, action );
 		if ( hashCode == -1 ) {
 			return null;
 		}
 
        	if ( !this.registry.containsKey( hashCode ) ) {
 			this.registry.put( hashCode, this.createAction( controller, action ) );
 		}
 
         return this.registry.get(hashCode);
     }
 
 	protected IAction createAction( String controller, String action ) throws InstantiationException {
 		int actionHashCode = this.generateHashId(controller, action);
         IAction actionInstance = null;
 		Class<? extends IAction> actionClazz = this.actions.get( actionHashCode );
 		if ( actionClazz != null ) {
 			actionInstance = _createInstance(actionClazz);
 			this.registry.put( actionHashCode, actionInstance );
 		}
 
 		int controllerHashCode = this.generateHashId(controller, "");
         IAction controllerInstance = null;
 		Class<? extends IAction> controllerClazz = this.actions.get( controllerHashCode );
 		if ( controllerClazz != null ) {
 			this.registry.put( controllerHashCode, controllerInstance = _createInstance(controllerClazz) );
 		}
 
         return Commons.select( actionInstance, controllerInstance );
     }
 
 	protected String normalize( String value ) {
 		value = value.replace("//", "/");
 
 		if ( value.startsWith("/") ) {
 			value = value.substring(1);
 		}
 
 		if ( value.endsWith("/") ) {
 			value = value.substring( 0, value.length() - 1 );
 		}
 
 		return value;
 	}
 
     private IAction _createInstance( Class<? extends IAction> action ) throws InstantiationException {
         try {
             IAction actionInstance = action.newInstance();
 			this.getContext().getAutowireCapableBeanFactory()
 				.autowireBean( actionInstance );
 
 			return actionInstance;
         } catch ( InstantiationException e ) {
        	throw new InstantiationException("Unable to create action instance");
         } catch ( IllegalAccessException e ) {
             throw new InstantiationException("Action constructor not available");
         }
     }
 
 }
