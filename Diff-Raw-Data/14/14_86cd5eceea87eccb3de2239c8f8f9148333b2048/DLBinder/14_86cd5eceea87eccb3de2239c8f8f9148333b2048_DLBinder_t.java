 package cz.datalite.zk.composer;
 
 import cz.datalite.zk.composer.listener.DLDetailController;
 import cz.datalite.zk.composer.listener.DLMainModel;
 import cz.datalite.zk.composer.listener.DLMasterController;
 import java.lang.reflect.Field;
 import java.util.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.bind.BindComposer;
 import org.zkoss.zk.ui.*;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.metainfo.ComponentInfo;
 import org.zkoss.zk.ui.sys.ComponentCtrl;
 
 /**
  * <p>Composer for MVC development.</p> <p>It is like ZK's
  * GenericForwardComposer, but you have to explicitly bound properties and
  * methods with annotations: <ul> <li>@ZkEvent - register event on component and
  * call annotated method when event fires</li> <li>@ZkComponent - attach ZUL
  * component to this propery <li>@ZkController - is accessible from ZUL page
  * whith the {controller}.property name. Default value for {controller} is "ctl"
  * unless you set it to other value with @Zkcontroller(name="controller") on
  * class level. Example: @ZkController ListControll listCtl; ZUL:
  * controller="${ctl.listCtl}" sets controller. Controller is read only. </li>
  * <li>@ZkModel - is accessible from ZUL page whith the {model}.property name.
  * Default value for {model} is "ctl" unless you set it to other value with
  * @ZkModel(name="model") on class level. Example: @ZkModel String modelProperty
  * = "xx"; ZUL: value="@{ctl.modelProperty}" sets value to "xx". Model is
  * read/write. </li> <li>@ZkParameter - checks ZK's parameter maps for parameter
  * name and set field to this value in doBeforeCompose(). It checks these maps:
  * execution.getArg(), execution.getAttributes(), execution.getParameters() and
  * uses first map with this parameter set. </li> <li>@ZkBinding - save binding
  * before method invocation and/or load after method invocation.</li>
  * <li>@ZkConfirm - ask user with question dialog befor method is invoked</li> *
  * </ul> Check those annotations documentation for more information. Although it
  * extends GenericAutowireComposer it <b>does not</b> wires custom varibles. It
  * is used solely for implicit objects. <p>
  *
  * <p>This class implements map interface. This is usefull for map access from
  * ZUL or with binding. In doBeforeComposeChildren, instance of controller is
  * set to component variable as "ctl" (or custom name) and is immediately from
  * ZUL processing - ctl.property will access this map, which will return
  * property mapped by @ZkController or @ZkModel.</p>
  *
  * <p>Similar to GenericAutowireComposer it has basic protected properties like
  * session, requestScope, arg etc., but it doesn't wire all properties and
  * methods autmatically (because it can be performance bottleneck if you use
  * multiple composers with many methods.)</p>
  *
  * <p>Notice that since this composer kept references to the components, single
  * instance object cannot be shared by multiple components.</p>
  *
  * <p>It implements DLMasterController interface, while each controller can be
  * master. Defualt implementation only constructs list of child controllers and
  * resend each message to all children that implements DLDetailController
  * interface.</p> <p>Usage for automatic master/detail setup:<br/> &lt;include
  * src="xx" masterController="${ctl}"&gt;<br/> Include child page into master
  * page and set masterController to this variable. DLComposer will connect
  * master/detail automatically. </p>
  *
  * @author Jiri Bubnik - DLComposer
  * @author simonpai - SelectorComposer
  * @author Karel Cemus <cemus@datalite.cz>
  */
 public class DLBinder<T extends Component, S extends DLMainModel> extends BindComposer<T> implements java.util.Map<String, Object>, DLMasterController<S> {
 
     protected final Logger LOGGER = LoggerFactory.getLogger( this.getClass() );
 
     /**
      * The view component managed by this view-model provide component reference
      * to allow to work with UI in inherited controllers. This can be understood
      * as a feature of backward compatibility. It preserves old usage setup self
      * in advance - it might be used in composition. Implicit Object; the
      * applied component itself.
      *
      * @since 3.0.7
      */
     protected transient T self;
 
     /** Implicit Object; the space owner of the applied component.
      *
      * @since 3.0.7
      */
     protected transient IdSpace spaceOwner;
 
     /** Implicit Object; the page.
      *
      * @since 3.0.7
      */
     protected transient Page page;
 
     /** Implicit Object; the desktop.
      *
      * @since 3.0.7
      */
     protected transient Desktop desktop;
 
     /** Implicit Object; the session.
      *
      * @since 3.0.7
      */
     protected transient Session session;
 
     /** Implicit Object; the web application.
      *
      * @since 3.0.7
      */
     protected transient WebApp application;
 
     /** Implicit Object; a map of attributes defined in the applied component.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> componentScope;
 
     /** Implicit Object; a map of attributes defined in the ID space contains the
      * applied component.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> spaceScope;
 
     /** Implicit Object; a map of attributes defined in the page.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> pageScope;
 
     /** Implicit Object; a map of attributes defined in the desktop.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> desktopScope;
 
     /** Implicit Object; a map of attributes defined in the session.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> sessionScope;
 
     /** Implicit Object; a map of attributes defined in the web application.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> applicationScope;
 
     /** Implicit Object; a map of attributes defined in the request.
      *
      * @since 3.0.7
      */
     protected transient Map<String, Object> requestScope;
 
     /** Implicit Object; the current execution.
      *
      * @since 3.0.7
      */
     protected transient Execution execution;
 
     /** Implicit Object; the arg argument passed to the createComponents method. It is
      * never null.
      *
      * @since 3.0.8
      */
     protected transient Map<?, ?> arg;
 
     /** Implicit Object; the param argument passed from the http request.
      *
      * @since 3.6.1
      */
     protected transient Map<String, String[]> param;
 
     /** Map model name to field representing this model */
     private final Map<String, Field> zkModels = new java.util.HashMap<String, Field>();
 
     /** Map controller name to field representing this model */
     private final Map<String, Field> zkControllers = new java.util.HashMap<String, Field>();
 
     /** Model in master/detail. * */
     private S masterControllerModel;
 
     /** List of child controller for master / detail page composition. * */
     private List<DLDetailController> detailControllers = new LinkedList<DLDetailController>();
 
     //    protected final List<VariableResolver> _resolvers;
     public DLBinder() {
         super();
 //        _resolvers = Selectors.newVariableResolvers( getClass(), SelectorComposer.class );
     }
 
     @Override
     public void doAfterCompose( T comp ) throws Exception {
         // Process ZK annotations
         //      check that annotations are correct
         ZkAnnotationUtils.validMethodAnnotations( this.getClass() );
         //      @ZkEvent
         ZkAnnotationUtils.registerZkEvents( this, self );
         //      @ZkComponent
         ZkAnnotationUtils.registerZkComponents( this, self );
         //      ZKOSS Selector wiring @Wire and @Listen
         SelectorUtils.wire( this, self );
        // wire components and then let it call on the children
        super.doAfterCompose( comp );
     }
 
     @Override
     public ComponentInfo doBeforeCompose( Page page, Component parent, ComponentInfo compInfo ) throws Exception {
 //        Selectors.wireVariables( page, this, _resolvers );
 //        getUtilityHandler().subscribeEventQueues( this );
 //        return compInfo;
         return super.doBeforeCompose( page, parent, compInfo );
     }
 
     /**
      * <p>Before main component children are created, setup master/detail and
      * publish controller variables into component namespace. You can than use
      * value="${ctl.property}" in child components. This method is called by ZK
      * framework.</p>
      *
      * DLComposer extension here: <ul> <li>sets "self" variable to master
      * component</li> <li>calls setupMasterController() - if </li> <li>validates
      * @ZkXXX annotations - e.g. @ZkBinding can be used only with @ZkEvent</li>
      * <li>loads all @ZkXXX annotations</li> </ul>
      *
      * @param comp master component
      * @throws Exception
      */
     @Override
     public void doBeforeComposeChildren( Component comp ) throws Exception {
         // initialize the binder and its properties
         init( comp );
 
         // initialize the parent (as well as the @init in children)
         super.doBeforeComposeChildren( comp );
     }
 
     /**
      * Initializes the DLBinder to fill stateful attributes available in derived
      * classes like arguments, parameter, desktop, ...
      */
     private void init( final Component comp ) {
         // this is not availible since ZK 6 because GenericAutowiredComposer
         // is no more parent of current class. Defined properties are not present
         // wire all implicit variables
         WireUtils.wireImplicit( this, comp );
 
         // load ZK annotations        
         //      @ZkModel
         //      @ZkController
         ZkAnnotationUtils.init( zkModels, zkControllers, this, self );
 
         // register modified DLBinderImpl to provide the support to Zk annotations
         //  do it only if another binder is not already defined
         final ComponentCtrl extendedComponent = ( ( ComponentCtrl ) comp );
         if ( extendedComponent.getAnnotations( "binder" ).isEmpty() )
             extendedComponent.addAnnotation( "binder", "init", Collections.singletonMap( "value", new String[]{ "'cz.datalite.zk.bind.AnnotationBinder'" } ) );
 
         // setup Master / detail relationship
         MasterDetailUtils.setupMasterController( this, comp );
 
         // setup parameters @ZkParameter
         ZkParameterUtils.setupZkParameters( this );
     }
 
     /** ************************ Master / Detail *************************** */
     public void addChildController( DLDetailController detailController ) {
         detailControllers.add( detailController );
     }
 
     public void removeChildController( DLDetailController detailController ) {
         detailControllers.remove( detailController );
     }
 
     public S getMasterControllerModel() {
         return masterControllerModel;
     }
 
     public void postEvent( String eventName, Object data ) {
         Events.postEvent( eventName, self, data );
     }
 
     public void onDetailChanged( S model ) {
         masterControllerModel = model;
         MasterDetailUtils.onDetailChanged( detailControllers, model );
     }
 
     /** ***************** @ZkModel and @ZkController ********************** */
     public Object get( final Object key ) {
         return ZkAnnotationUtils.get( key, this, zkModels, zkControllers );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public boolean containsKey( final Object key ) {
         // ZK's databinding check, if the key is available in a map.
         // We can skip this here, because get() method covers all cases.
         return true;
     }
 
     public Object put( final String key, final Object value ) {
         return ZkAnnotationUtils.put( key, value, this, zkModels, zkControllers );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public Object remove( final Object key ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public void putAll( final java.util.Map<? extends String, ? extends Object> m ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public void clear() {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public java.util.Set<String> keySet() {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public Collection<Object> values() {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public java.util.Set<java.util.Map.Entry<String, Object>> entrySet() {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public int size() {
         //throw new UnsupportedOperationException( "Not supported yet." );
         // unsupport exception causes problems while debuging in netbeans, 
         // which tries to show in variables window this as Map()
         return 0;
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public boolean isEmpty() {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     /** Only to implement all Map interface methods. Don't use. */
     public boolean containsValue( final Object value ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 }
