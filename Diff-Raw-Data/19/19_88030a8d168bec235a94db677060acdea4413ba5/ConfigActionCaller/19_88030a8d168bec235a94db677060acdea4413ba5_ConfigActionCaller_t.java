 package de.zib.gndms.infra.system;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Sets;
import com.google.common.collect.MapMaker;
 import com.google.inject.Binder;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import de.zib.gndms.infra.action.*;
 import de.zib.gndms.kit.config.ParameterTools;
 import de.zib.gndms.logic.action.Action;
 import de.zib.gndms.logic.action.SkipActionInitializationException;
 import de.zib.gndms.logic.model.BatchUpdateAction;
 import de.zib.gndms.logic.model.DefaultBatchUpdateAction;
 import de.zib.gndms.logic.model.DelegatingEntityUpdateListener;
 import de.zib.gndms.logic.model.EntityUpdateListener;
 import de.zib.gndms.logic.model.config.ConfigAction;
 import de.zib.gndms.logic.model.config.EchoOptionsAction;
 import de.zib.gndms.logic.model.config.HelpOverviewAction;
 import de.zib.gndms.logic.model.dspace.AssignSliceKindAction;
 import de.zib.gndms.logic.model.dspace.SetupSliceKindAction;
 import de.zib.gndms.logic.model.dspace.SetupSubspaceAction;
 import de.zib.gndms.logic.model.gorfx.ConfigOfferTypeAction;
 import de.zib.gndms.logic.model.gorfx.SetupOfferTypeAction;
 import de.zib.gndms.model.common.GridResource;
 import de.zib.gndms.model.common.ModelUUIDGen;
 import org.apache.axis.components.uuid.UUIDGen;
 import org.apache.axis.components.uuid.UUIDGenFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.io.PrintWriter;
 import java.util.Set;
import java.util.Map;
 
 
 /**
  * Default implementation of the {@code WSActionCaller} interface.
  *
  * A ConfigActionCaller is used to dynamically instantiate and call new {@code ConfigAction}s.
  * The main method of this class is {@code  callPublicAction(String, String, java.io.PrintWriter)} which is used for
  * this purpose. See {@link WSActionCaller#callPublicAction(String, String, java.io.PrintWriter)}
  * for a description.
  *
  * @see ConfigAction
  * @see WSActionCaller
 * @author Stefan Plantikow<plantikow@zib.de>
 * @version $Id$
 *
 *          User: stepn Date: 03.09.2008 Time: 16:43:46
 */
 public final class ConfigActionCaller implements WSActionCaller, Module {
     private @NotNull final Log logger = LogFactory.getLog(ConfigActionCaller.class);
 
     private @NotNull final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
     private final @NotNull ModelUUIDGen actionUUIDGen = new ModelUUIDGen() {
             public @NotNull String nextUUID() {
                 return uuidGen.nextUUID();
             }
     };
 
	  private final Map<Class<? extends ConfigAction<?>>, Boolean> configActionMap = new MapMaker().makeMap();
     /**
      * A set of ConfigAction.{@link HelpOverviewAction} is excluded, as this set used in the {@code HelpOverviewAction}
      * to print help about all available ConfigActions.
      */
     private final Set<Class<? extends ConfigAction<?>>> configActions =
            Sets.newSetFromMap(configActionMap);
 
     private final Function<String, String> classToActionNameMapper =
         new Function<String, String>() {
            public String apply(final String s) {
                 if (s.startsWith("de.zib.gndms.infra.action") && s.endsWith("Action"))
                     return ".sys" + s.substring("de.zib.gndms.infra.action".length(), s.length()-6);
                 if (s.startsWith("de.zib.gndms.logic.model") && s.endsWith("Action"))
                     return s.substring("de.zib.gndms.logic.model".length(), s.length()-6);
                 return s;
             }
         };
 
     private final GNDMSystem system;
 
 	private final Injector injector;
 
 
     /**
      * Fills {@link #configActions} with all implemented {@link ConfigAction}s, except the {@link HelpOverviewAction}.
      * Load the GNDMSystem's injector into {@link #injector} as a new {@code ChildInjector} of this.
      *
      * @param systemParam the GNDMSystem
      */
 	@SuppressWarnings({ "ThisEscapedInObjectConstruction", "OverlyCoupledMethod" })
 	public ConfigActionCaller(final @NotNull GNDMSystem systemParam) {
         system = systemParam;
         configActions.add(SetupSubspaceAction.class);
         configActions.add(EchoOptionsAction.class);
         configActions.add(GetHomeInfoAction.class);
         configActions.add(SetupOfferTypeAction.class);
         configActions.add(ConfigOfferTypeAction.class);
         configActions.add(SetupSliceKindAction.class);
         configActions.add(AssignSliceKindAction.class);
         configActions.add(RefreshSystemAction.class);
         configActions.add(SetupDefaultConfigletAction.class);
         configActions.add(ReadC3CatalogAction.class);
         configActions.add(ReadGNDMSVersionAction.class);
 
 		injector = system.getInstanceDir().getSystemAccessInjector().createChildInjector(this);
     }
 
     /**
      * Instantiates a new config action or a specific subclass of it according to the String <tt>name</tt>,
      * sets its configurations map to the values set in <tt>params</tt> and returns the action.
      *
      <p>If the instance should be public accessible (see {@link de.zib.gndms.infra.action.PublicAccessible}) set <tt>pub</tt>
      * to <tt>true</tt>.
      *
      *
      * @param name A String giving a description about the action class or a help request.
      *        See {@link #findActionClass(String)} )
      * @param params A String containing the configuration for the Config Action.
      *        See {@link ConfigAction#parseLocalOptions(String)}
      * @param pub a boolean to flag that the instance is public accessible or not.
      *        See {@link PublicAccessible}.
      *
      * @return a ConfigAction instance with a given configuration
      *
      * @throws ClassNotFoundException if the class given by <tt>name></tt> could not be found.
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws ParameterTools.ParameterParseException if <tt>params</tt> is not valid according to defined Syntax.
      */
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     public ConfigAction<?> instantiateConfigAction(final @NotNull String name,
                                                    final @NotNull String params,
                                                    final boolean pub
                                                   )
             throws ClassNotFoundException, IllegalAccessException, InstantiationException,
             ParameterTools.ParameterParseException {
         final Class<? extends ConfigAction<?>> clazz = findActionClass(name);
         
         if( pub && !PublicAccessible.class.isAssignableFrom(clazz) )
                 throw new IllegalArgumentException( name + " is not public accessible." );
 
         if (ConfigAction.class.isAssignableFrom(clazz)) {
             final ConfigAction configAction = clazz.newInstance();
             configAction.parseLocalOptions(params);
             return configAction;
         }
         else
             throw new IllegalArgumentException("Not a ConfigAction");
     }
 
     /**
      * Finds a specific ConfigAction class.
      *
      * If <tt>name</tt>
      * <ul>
      *      <li>
      *      is denoted as "help", "-help" or "--help" a {@link HelpOverviewAction} class instance will be returned.
      *      </li>
      *      <li>
      *      starts with ".sys", the class 'de.zib.gndms.infra.action'+name.substring(4)+'Action' will be returned.
      *      </li>
      * *      <li>
      *      starts with ".", the class 'de.zib.gndms.logic.model'+name+'Action' will be returned.
      *      </li>
      * </ul>
      * 
      * @param name a String containing the name of a config action class or a help request.
      * @return a ConfigAction class corresponding the given String.
      * @throws ClassNotFoundException if <tt>name</tt> is null or the class could not be found
      */
     @SuppressWarnings({ "MethodMayBeStatic" })
     private Class<? extends ConfigAction<?>> findActionClass(
             final @NotNull String name) throws ClassNotFoundException
     {
         if (name.length() > 0) {
             final String nameToLower = name.toLowerCase();
             if ("help".equals(nameToLower) || "-help".equals(nameToLower)
                     || "--help".equals(nameToLower)) {
                 return toConfigActionClass(HelpOverviewAction.class);
             }
             try {
                 if (name.startsWith(".sys"))
                     return toConfigActionClass(Class.forName(
                             "de.zib.gndms.infra.action" + name.substring(4) + "Action"));
             }
             catch (ClassNotFoundException e) {
                 // continue trying
             }
             if (name.charAt(0) == '.')
                 return toConfigActionClass(
                         Class.forName("de.zib.gndms.logic.model" + name + "Action"));
         }
         return toConfigActionClass(Class.forName(name));
     }
 
     /**
      * Checks if <tt>helpOverviewActionClassParam</tt> is a <tt>ConfigAction</tt> class or a subclass of it.
      * In this case the class object will be returned with a cast, as denoted in the signature.
      * Otherwise an IllegalArgumentException will be thrown.
      * 
      * @param helpOverviewActionClassParam the class which will be casted and returned
      * @return  <tt>helpOverviewActionClassParam</tt> if it is a <tt>ConfigAction</tt> class or a subclass of it.
      */
     @SuppressWarnings({ "unchecked" })
     private static Class<? extends ConfigAction<?>> toConfigActionClass(
             final Class<?> helpOverviewActionClassParam) {
          if (ConfigAction.class.isAssignableFrom(helpOverviewActionClassParam))
                 return (Class<? extends ConfigAction<?>>) helpOverviewActionClassParam;
         else
              throw new IllegalArgumentException("Given class is not a ConfigAction");
     }
 
 
     @SuppressWarnings({ "FeatureEnvy" })
     public Object callAction(
             final @NotNull String className, final @NotNull String opts,
             final @NotNull PrintWriter writer) throws Exception {
         ConfigAction<?> action = instantiateConfigAction(className, opts.trim(), false);
         return realCallAction( action, className, opts, writer );
     }
 
         
     public Object callPublicAction( final @NotNull String className, final @NotNull String opts, final @NotNull PrintWriter writer ) throws Exception {
 
         ConfigAction<?> action = instantiateConfigAction(className, opts.trim(), true);
         return realCallAction( action, className, opts, writer );
     }
 
 
     /**
      * Sets all necessary fields of the newly created ConfigAction instance,
      * invokes {@code call()} on the ConfigAction instance and its postponed actions
      * and returns the result of the ConfigAction's computation.
      *
      * Note: the options of the action ({@code opts}) must be already be set before this method is invoked.
      *
      * @param action the ConfigAction which shall be invoked
      * @param className the classname of the ConfigAction which shall be created
      * @param opts A String containing the configuration for the ConfigAction
      * @param writer a printwriter where all output, generated by the ConfigAction, will be written to
      * @return the result of the ConfigAction's computation
      * @throws Exception
      */
     protected Object realCallAction( ConfigAction<?> action, final @NotNull String className, final @NotNull String opts, final @NotNull PrintWriter writer ) throws Exception {
         action.setOwnEntityManager(system.getEntityManagerFactory().createEntityManager());
         action.setPrintWriter(writer);
         action.setClosingWriterOnCleanUp(false);
         action.setWriteResult(true);
         action.setUUIDGen(actionUUIDGen);
         action.setOwnPostponedActions(new DefaultBatchUpdateAction<GridResource>());
         final DelegatingEntityUpdateListener<GridResource> updateListener =
             DelegatingEntityUpdateListener.getInstance(system);
         action.getPostponedActions().setListener(updateListener);
         if (action instanceof SystemAction)
             ((SystemAction<?>)action).setSystem(system);
         // Help Action required dynamic casting due to compiler bug in older 1.5 javac
         // regarding generics and instanceof
         if (HelpOverviewAction.class.isInstance(action)) {
             HelpOverviewAction helpAction = HelpOverviewAction.class.cast(action);
             helpAction.setConfigActions(configActions);
             helpAction.setNameMapper(classToActionNameMapper);
         }
         injector.injectMembers(action);
 
         logger.info("Running " + className + ' ' + opts);
         try {
             Object retVal = action.call();
             Action<?> postAction = action.getPostponedActions();
             postAction.call();
             return retVal;
         }
         catch (SkipActionInitializationException sa) {
             // Intentionally ignored
             return null;
         }
         catch (Exception e) {
             logger.warn("Failure during " + className + ' ' + opts, e);
             throw e;
         }
     }
 
 
     public void configure(final @NotNull Binder binder) {
 		binder.skipSources(GNDMSystem.class,
 		                   EntityManager.class,
 		                   ModelUUIDGen.class,
 		                   UUIDGen.class,
 		                   EntityUpdateListener.class,
 		                   BatchUpdateAction.class);
 		binder.bind(ConfigActionCaller.class).toInstance(this);
 	}
 }
