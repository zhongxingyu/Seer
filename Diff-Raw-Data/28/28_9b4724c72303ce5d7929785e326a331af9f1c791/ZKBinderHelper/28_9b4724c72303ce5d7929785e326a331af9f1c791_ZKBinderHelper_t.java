 package cz.datalite.zk.bind;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.zk.ui.AbstractComponent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zkplus.databind.AnnotateDataBinder;
 
 /**
  * Facade to helper utils to work with databinding. This internaly contains the
  * dispatched based on binding version to provide proper utils implemenation.
  *
  * @author Karel Cemus
  */
 public abstract class ZKBinderHelper {
 
     private static final Logger LOGGER = LoggerFactory.getLogger( ZKBinderHelper.class );
 
     private ZKBinderHelper() {
         // library class, facade
     }
 
     /** returns proper instance of binder helper based on binding version */
     public static BinderHelper helper( Component component ) {
         final Object binder = component.getAttribute( "binder", true );
         
         final boolean hasBinderId = component.getRoot().getAttribute( "$BINDER_ID$" ) != null;
         
         final boolean hasComposer = component.getRoot().getAttribute( "$composer" ) != null;
 
        // if binder is not assigned and neither the composer it means that the binding is not initialized yet.
        if ( binder == null && !hasComposer )
             throw new IllegalArgumentException( "Component doesn't have assigned any binder." );
 
        if ( binder != null && binder instanceof AnnotateDataBinder )
             return BinderHelperV1.INSTANCE;
 
        if ( binder != null && binder instanceof Binder )
             return BinderHelperV2.INSTANCE;
        
        return hasBinderId ? BinderHelperV2.INSTANCE : BinderHelperV1.INSTANCE;
     }
     
     /** Returns wheather or not the binder is detectable and is supposed to be
      * attached in the future */
     public static boolean hasDetachedBinder( Component component ) {
         if ( component == null ) return false;
 
         final Object binder = component.getAttribute( "binder", true );
 
         final boolean hasComposer = component.getRoot().getAttribute( "$composer" ) != null;
 
        return binder != null || hasComposer;
     }
 
     /** Returns wheather or not the component has attached any binder instance */
     public static boolean hasBinder( Component component ) {
         if ( component == null ) return false;
         
         final Object binder = component.getAttribute( "binder", true );
         
         return binder != null; // || ( hasComposer ^ hasBinderId );
     }
     
     /** @see BinderHelper#loadComponent */
     public static void loadComponent( Component comp ) {
         if ( hasBinder( comp ) ) helper( comp ).loadComponent( comp );
         else
             LOGGER.warn( "Attempt to load a component but it is null or has not attached any binder." );
     }
 
     /** @see BinderHelper#loadComponentAttribute */
     @Deprecated
     public static void loadComponentAttribute( Component comp, String attribute ) {
         if ( hasBinder( comp ) )
             helper( comp ).loadComponentAttribute( comp, attribute );
         else
             LOGGER.warn( "Attempt to load a component's attribute but it is null or has not attached any binder." );
     }
 
     /** @see BinderHelper#saveComponent */
     public static boolean saveComponent( Component comp ) {
         if ( hasBinder( comp ) ) return helper( comp ).saveComponent( comp );
         else
             LOGGER.warn( "Attempt to save a component but it is null or has not attached any binder." );
         return false;
     }
 
     /** @see BinderHelper#saveComponentAttribute */
     @Deprecated
     public static boolean saveComponentAttribute( Component comp, String attribute ) {
         if ( hasBinder( comp ) )
             return helper( comp ).saveComponentAttribute( comp, attribute );
         else
             LOGGER.warn( "Attempt to save a component's attribute but it is null or has not attached any binder." );
         return false;
     }
 
     /** @see BinderHelper#loadComponentAttributes */
     @Deprecated
     public static void loadComponentAttributes( Component comp, String[] attributes ) {
         if ( hasBinder( comp ) )
             helper( comp ).loadComponentAttributes( comp, attributes );
         else
             LOGGER.warn( "Attempt to load a component's attributes but it is null or has not attached any binder." );
     }
 
     /**
      * @param component Component with a registered binder to determine version of
      * binding and also the component to be annotated
      * @see BinderHelper#registerAnnotation */
     public static void registerAnnotation( final AbstractComponent component, final String property, final String annotName, final String value ) {
         if ( hasDetachedBinder( component ) )
             helper( component ).registerAnnotation( component, property, annotName, value );
         else
             LOGGER.warn( "Attempt to register an annotation on the component but it has not attached any binder." );
     }
 
     /**
      * @param comp Component with a registered binder to determine version of
      * binding
      * @see BinderHelper#notifyChange
      */
     public static void notifyChange( Component comp, Object bean, String model ) {
         if ( hasDetachedBinder( comp ) ) helper( comp ).notifyChange( bean, model );
         else
             LOGGER.warn( "Attempt to load a component but it is null or has not attached any binder." );
     }
     
     /** unrecognized version number */
     public static final int NOT_DEFINED = -1;
 
     /** @see BinderHelper#version */
     public static int version( Component comp ) {
         if ( hasDetachedBinder( comp ) ) return helper( comp ).version();
         else return NOT_DEFINED;
     }
 }
