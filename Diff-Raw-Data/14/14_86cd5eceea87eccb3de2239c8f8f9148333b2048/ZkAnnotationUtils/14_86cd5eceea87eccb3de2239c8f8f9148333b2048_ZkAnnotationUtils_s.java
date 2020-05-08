 package cz.datalite.zk.composer;
 
 import cz.datalite.helpers.ReflectionHelper;
 import cz.datalite.zk.annotation.*;
 import cz.datalite.zk.annotation.processor.AnnotationProcessor;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Map;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.lang.SystemException;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.UiException;
 
 /**
  * Util class for ZK-DL annotations like {@link ZkModel} and {@link ZkController}.
  *
  * @author Karel Čemus <cemuskar@fel.cvut.cz>
  */
 public final class ZkAnnotationUtils {
 
     private static final Logger LOGGER = LoggerFactory.getLogger( ZkAnnotationUtils.class );
 
     private ZkAnnotationUtils() {
         // library class
     }
 
     /**
      * Map interface is used to simulate @ZkModel property as it is a real
      * public propert of controller class.
      *
      * If no @ZkModel annotaton is used at all, it asumes "old" behaviour - set
      * field value directly.
      *
      * @param key name of property marked with @ZkModel
      * @param value new value
      */
     public static Object put( final String key, final Object value, final Object controller, Map<String, Field> zkModels, Map<String, Field> zkControllers ) {
         if ( zkModels.containsKey( key ) )
             put( key, value, zkModels, controller );
         else if ( zkModels.isEmpty() )
             putDefault( key, value, controller );
         else
             LOGGER.error( "Unknown model for key '{}'", key );
         return value;
     }
 
     private static void putDefault( final String key, final Object value, final Object controller ) {
         try {
             org.zkoss.lang.reflect.Fields.set( controller, key, value, true );
         } catch ( NoSuchMethodException ex ) {
             throw SystemException.Aide.wrap( ex );
         }
     }
 
     private static void put( final String key, final Object value, final Map<String, Field> fieldMap, final Object controller ) {
         try {
             try {
                 org.zkoss.lang.reflect.Fields.set( controller, key, value, true );
             } catch ( NoSuchMethodException ex ) {
                 final Field field = fieldMap.get( key );
                 if ( field != null ) {
                     field.setAccessible( true );
                     field.set( controller, value );
                     field.setAccessible( false );
                 } else
                     throw new NoSuchMethodException( "No such setter or field for key \"" + key + "\"." );
             }
         } catch ( Exception ex ) {
             throw SystemException.Aide.wrap( ex );
         }
     }
 
     /**
      * Map interface is used to simulate @ZkModel and @ZkController property as
      * it is a real public property of controller class.
      *
      * If no @ZkModel and @ZkController annotaton is used at all, it asumes
      * "old" behaviour - get field value directly.
      *
      * @param key name of property marked with @ZkModel or @ZkController
      * @return value of the property
      */
     public static Object get( Object key, Object controller, Map<String, Field> zkModels, Map<String, Field> zkControllers ) {
         if ( zkModels.containsKey( ( String ) key ) )
             return get( ( String ) key, controller, zkModels );
         else if ( zkControllers.containsKey( ( String ) key ) )
             return get( ( String ) key, controller, zkControllers );
         else if ( zkModels.isEmpty() && zkControllers.isEmpty() )
             return getDefault( ( String ) key, controller );
         else
             throw new UiException( new NoSuchFieldException( "Composer \"" + controller.getClass().getName()
                     + "\"doesn't contain @ZkModel or @ZkController property \"" + key + "\"." ) );
     }
 
     private static Object getDefault( final String key, Object instance ) {
         try {
             return org.zkoss.lang.reflect.Fields.get( instance, key );
         } catch ( NoSuchMethodException ex ) {
             LOGGER.error( "Something went wrong", ex );
             return null;
         }
     }
 
     private static Object get( final String key, final Object instance, final Map<String, Field> fieldMap ) {
         try {
             try {
                 return org.zkoss.lang.reflect.Fields.get( instance, key );
             } catch ( NoSuchMethodException ex ) {
                 final Field field = fieldMap.get( key );
                 if ( field != null ) {
                     field.setAccessible( true );
                     final Object value = field.get( instance );
                     field.setAccessible( false );
                     return value;
                 } else
                     throw new NoSuchMethodException( "No such getter or field for key \"" + key + "\"." );
             }
         } catch ( Exception ex ) {
             LOGGER.error( "Something went wrong", ex );
             return null;
         }
     }
 
     public static void init( final Map<String, Field> models, final Map<String, Field> controllers, final Object controller, final Component self ) {
         // initialize models
         initZkController( controllers, controller, self );
         initZkModel( models, controller, self );
     }
 
     private static void initZkModel( final Map<String, Field> models, final Object controller, final Component self ) {
         // publishes the view model into UI. This is 
         // for backward compatibility and to allow same usage as it was
         // before ZK 6.0
         // the same for model. The default value is same as controller as well. It is more convenient to have only one variable to access controller
         // however, you can change the value with @ZkModel annotation on class level
         if ( !loadControllerClass( controller.getClass() ).equals( loadModelClass( controller.getClass() ) ) )
             self.setAttribute( loadModelClass( controller.getClass() ), controller, Component.COMPONENT_SCOPE );
 
         // setup model and controller fields and methods.
         models.putAll( loadModelFields( controller.getClass() ) );
         models.putAll( loadModelMethod( controller.getClass() ) );
     }
 
     private static void initZkController( final Map<String, Field> controllers, final Object controller, final Component self ) {
         // publishes the view model into UI. This is 
         // for backward compatibility and to allow same usage as it was
         // before ZK 6.0
         // publish controller into component namespace. It can be than accessed from ZUL as "ctl.xxx".
         // defualt value is ctl, but it can be changed with @ZkController annotation on class level
         self.setAttribute( loadControllerClass( controller.getClass() ), controller, Component.COMPONENT_SCOPE );
 
         // setup model and controller fields and methods.
         controllers.putAll( loadControllerFields( controller.getClass() ) );
         controllers.putAll( loadControllerMethod( controller.getClass() ) );
     }
 
     private static Map<String, Field> loadModelFields( final Class cls ) {
         final Map<String, Field> fields = new java.util.HashMap<String, Field>();
         for ( Field field : ReflectionHelper.getAllFields( cls ) ) {
             for ( Annotation annotation : field.getDeclaredAnnotations() ) {
                 if ( annotation instanceof ZkModel ) {
                     fields.put( getId( ( ZkModel ) annotation, field ), field );
                     break;
                 }
             }
         }
         return fields;
     }
 
     public static String loadModelClass( final Class cls ) {
         for ( Annotation annotation : cls.getDeclaredAnnotations() ) {
             if ( annotation instanceof ZkModel )
                 return ( ( ZkModel ) annotation ).name().length() == 0 ? "ctl" : ( ( ZkModel ) annotation ).name();
         }
         return loadControllerClass( cls );
     }
 
     private static Map<String, Field> loadModelMethod( final Class cls ) {
         final Map<String, Field> fields = new java.util.HashMap<String, Field>();
         for ( Method method : ReflectionHelper.getAllMethods( cls ) ) {
             for ( Annotation annotation : method.getDeclaredAnnotations() ) {
                 if ( annotation instanceof ZkModel ) {
                     fields.put( getId( ( ZkModel ) annotation, method ), null );
                     break;
                 }
             }
         }
         return fields;
     }
 
     private static Map<String, Field> loadControllerFields( final Class cls ) {
         final Map<String, Field> fields = new java.util.HashMap<String, Field>();
         for ( Field field : ReflectionHelper.getAllFields( cls ) ) {
             for ( Annotation annotation : field.getDeclaredAnnotations() ) {
                 if ( annotation instanceof ZkController ) {
                     fields.put( getId( ( ZkController ) annotation, field ), field );
                     break;
                 }
             }
         }
         return fields;
     }
 
     public static String loadControllerClass( final Class cls ) {
         for ( Annotation annotation : cls.getDeclaredAnnotations() ) {
             if ( annotation instanceof ZkController )
                 return ( ( ZkController ) annotation ).name().length() == 0 ? "ctl" : ( ( ZkController ) annotation ).name();
         }
         return "ctl";
     }
 
     private static Map<String, Field> loadControllerMethod( final Class cls ) {
         final Map<String, Field> fields = new java.util.HashMap<String, Field>();
         for ( Method method : ReflectionHelper.getAllMethods( cls ) ) {
             for ( Annotation annotation : method.getDeclaredAnnotations() ) {
                 if ( annotation instanceof ZkController ) {
                     fields.put( getId( ( ZkController ) annotation, method ), null );
                     break;
                 }
             }
         }
         return fields;
     }
 
     private static String getId( final ZkModel annotation, final Field field ) {
         return "".equals( annotation.name() ) ? field.getName() : annotation.name();
     }
 
     private static String getId( final ZkController annotation, final Field field ) {
         return "".equals( annotation.name() ) ? field.getName() : annotation.name();
     }
 
     private static String getId( final ZkComponent annotation, final Field field ) {
         return "".equals( annotation.id() ) ? field.getName() : annotation.id();
     }
 
     private static String getId( final ZkModel annotation, final Method method ) {
         return "".equals( annotation.name() ) ? getMethodNameWithoutGetSet( method.getName() ) : annotation.name();
     }
 
     private static String getId( final ZkController annotation, final Method method ) {
         return "".equals( annotation.name() ) ? getMethodNameWithoutGetSet( method.getName() ) : annotation.name();
     }
 
     private static String getMethodNameWithoutGetSet( final String name ) {
         if ( name.startsWith( "get" ) )
             return getMethodLowerCase( name.substring( 3 ) );
         else if ( name.startsWith( "set" ) )
             return getMethodLowerCase( name.substring( 3 ) );
         else if ( name.startsWith( "is" ) )
             return getMethodLowerCase( name.substring( 2 ) );
         else
             return name;
     }
 
     protected static String getMethodLowerCase( final String name ) {
         return name.substring( 0, 1 ).toLowerCase() + name.substring( 1 );
     }
 
     /**
      * Checks each method for @ZkEvent(s) annotation and registers all events
      * with DLZKEvent class.
      *
      * @param ctl controller
      * @param component master component
      */
     public static void registerZkEvents( final Object ctl, final Component component ) {
         AnnotationProcessor processor = AnnotationProcessor.getProcessor( ctl.getClass() );
         processor.registerZkEventsTo( component, ctl );
     }
 
     /**
      * Check all fields in ctl object for annotation @ZkComponent. For each
      * property get id from annotation (or property name if not set) and find
      * fellow component of component by this id. If not found or not correct
      * type, throws exception. If everythnig is ok, set the property to new
      * value of the fellow component
      *
      * @param controller controller with @ZkComponent annotations
      * @param component master component to find fellow components in
      */
     public static void registerZkComponents( final Object controller, final Component component ) {
         for ( final Field field : ReflectionHelper.getAllFields( controller.getClass() ) ) {
             for ( Annotation annot : field.getDeclaredAnnotations() ) {
                 if ( annot instanceof ZkComponent ) {
                     String id = getId( ( ZkComponent ) annot, field );
                     Component target = component.getFellowIfAny( id );
 
                     if ( target == null ) {
                         if ( ( ( ZkComponent ) annot ).mandatory() )
                             throw new IllegalArgumentException( "@ZkComponent injection: Unable to inject Component with ID '" + id + "'. "
                                     + "Component not found in idspace of composer self component '" + component.getId() + "'." );
                     } else
                         try {
                             field.setAccessible( true );
                             field.set( controller, target );
                             field.setAccessible( false );
                         } catch ( IllegalArgumentException ex ) {
                             throw new IllegalArgumentException( "@ZkComponent injection: Unable to inject Component with ID '" + id + "'. "
                                     + "Component is of different class: '" + target.getClass().getName() + "'." );
                         } catch ( IllegalAccessException ex ) {
                             throw SystemException.Aide.wrap( ex );
                         }
                 }
             }
         }
     }
 
     /**
      * It is possible to add @ZkConfirm or @ZkBinding annotations to any method
      * in composer. However they may be processed only together with @ZkEvent
      * annotation, while in event invocation phase. It is similar to Spring
      * proxy invocation - it handles only calls from outside.
      *
      * It thorws exception if invalid annotation combination is met.
      *
      * @param clazz class to check.
      */
     public static void validMethodAnnotations( final Class clazz ) {
         for ( Method method : ReflectionHelper.getAllMethods( clazz ) ) {
             boolean zkEvent = false;
             boolean zkConfirm = false;
             boolean zkBinding = false;
             for ( Annotation annot : method.getDeclaredAnnotations() ) {
                if ( annot instanceof ZkEvent || annot instanceof ZkEvents ) {
                     zkEvent = true;
                     continue;
                 } else if ( annot instanceof ZkConfirm ) {
                     zkConfirm = true;
                     continue;
                 } else if ( annot instanceof ZkBinding || annot instanceof ZkBindings ) {
                     zkBinding = true;
                     continue;
                 }
             }
 
             if ( !zkEvent && ( zkConfirm || zkBinding ) )
                 throw new IllegalArgumentException( "Unsupported annotation combination on method \"" + method.getName() + "\" ZkEvent is required." );
         }
     }
 }
