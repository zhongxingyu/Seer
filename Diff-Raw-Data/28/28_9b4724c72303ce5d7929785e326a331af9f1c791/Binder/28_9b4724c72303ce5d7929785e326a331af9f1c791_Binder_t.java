 package cz.datalite.zk.bind;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.bind.AnnotateBinder;
 import org.zkoss.bind.Converter;
 import org.zkoss.bind.Validator;
 import org.zkoss.bind.impl.BindingKey;
 import org.zkoss.zel.impl.util.Classes;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.UiException;
 import org.zkoss.zkplus.databind.TypeConverter;
 
 /**
  * DataLite's implementation of ZK Binder to add the support for annotations
  * brought by this part of ZK-DL library. To preserve the correct ZK command
  * life-cycle and to be as close as possible to the ZK framework's intended
  * usage there is needed to intercept the method with execution to attach
  * another logic.
  *
  * @author Karel Cemus <cemus@datalite.cz>
  */
 public class Binder extends AnnotateBinder {
 
     /** instance of looger */
     protected static final Logger LOGGER = LoggerFactory.getLogger( Binder.class );
 
     public boolean saveComponent( Component comp ) {
         LOGGER.trace( "Saving the component '{}'.", comp.getClass() );
         return saveAllBindings( comp );
     }
 
     public void loadComponent( Component comp ) {
         LOGGER.trace( "Loading the component '{}'.", comp.getClass() );
         super.loadComponent( comp, false );
     }
 
     @Deprecated
     public void loadComponentAttribute( Component comp, String attribute ) {
         LOGGER.trace( "Loading the attribute '{}' on the component '{}'.", attribute, comp.getClass() );
         loadBinding( comp, new BindingKey( comp, attribute ) );
     }
 
     @Deprecated
     public boolean saveComponentAttribute( Component comp, String attribute ) {
         LOGGER.trace( "Saving the attribute '{}' on the component '{}'.", attribute, comp.getClass() );
         return saveBinding( comp, new BindingKey( comp, attribute ) );
     }
 
     public static Binder getBinder( Component comp ) {
        return ( Binder ) comp.getAttribute( "binder", true );
     }
 
     @Override
     public Converter getConverter( String name ) {
         try {
             return super.getConverter( name );
         } catch ( ClassCastException ex ) {
             // converter found but class doesn't meet requirements
             // try to find adapter
             return resolveConverterAdapter( name );
         } catch ( UiException ex ) {
             // this allows to use method converters
             // syntax is then like 'ctl.coerceToValue'
             return new MethodConverter( name, ex.getMessage() );
         }
     }
 
     /** Support for TypeConverter and MethodConverter */
     private Converter resolveConverterAdapter( String name ) {
         try {
             Class converter = Classes.forNameByThread( name );
             if ( TypeConverter.class.isAssignableFrom( converter ) ) {
                 LOGGER.debug( "Converter '{}' is not directly support in ZK 6 and later. You should consider conversion to 'org.zkoss.bind.Converter'.", converter.getClass() );
                 return new TypeConverterAdapter( ( TypeConverter ) converter.newInstance() );
             } else
                 throw new ClassNotFoundException( "Convertor has to implement 'Converter' or 'TypeConverter' interface." );
         } catch ( InstantiationException ex ) {
             LOGGER.error( "Converter adapter couldn't be created.", ex );
             throw new UiException( ex );
         } catch ( IllegalAccessException ex ) {
             LOGGER.error( "Converter adapter couldn't be created.", ex );
             throw new UiException( ex );
         } catch ( ClassNotFoundException ex ) {
             throw new UiException( ex );
         }
     }
 
     @Override
     public Validator getValidator( String name ) {
         try {
             return super.getValidator( name );
         } catch ( org.zkoss.zk.ui.UiException ex ) {
             // this allows to use method converters
             // syntax is then like 'ctl.coerceToValue'
             return new MethodValidator( name, ex.getMessage() );
         }
     }
 }
