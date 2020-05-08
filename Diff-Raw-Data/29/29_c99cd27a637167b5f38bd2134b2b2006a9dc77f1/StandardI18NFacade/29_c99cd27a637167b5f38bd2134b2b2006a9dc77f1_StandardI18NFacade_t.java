 package com.redshape.i18n.impl;
 
 import com.redshape.i18n.I18NFacade;
 import com.redshape.i18n.I18NManager;
 import com.redshape.utils.ResourcesLoader;
 import com.redshape.utils.config.ConfigException;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 public class StandardI18NFacade implements com.redshape.i18n.I18NFacade {
 	private static final Logger log = Logger.getLogger( StandardI18NFacade.class );
 	
 	private static final I18NFacade instance = new StandardI18NFacade();
     private static Locale defaultLocale;
 	private Map<String, I18NManager> managers = new HashMap<String, I18NManager>();
 	
 	public static I18NFacade instance() {
 		return StandardI18NFacade.instance;
 	}
 	
 	public static String _( String orig ) {
 		return _( orig, Locale.getDefault() );
 	}
 	
 	public static String _( String orig, Locale locale ) {
 		return getDefaultManager( locale )._(orig);
 	}
 
     public static Locale getDefaultLocale() {
         return StandardI18NFacade.defaultLocale;
     }
 	
 	public static void setLocale( Locale locale ) {
 		log.info("Changing locale to" + instance().normalizeLocale(locale) );
 		Locale.setDefault( new Locale( instance().normalizeLocale( locale ) ) );
 	}
 	
 	public static boolean hasManager( Locale locale ) {
 		return instance().getManager(locale) != null;
 	}
 	
 	public static I18NManager getDefaultManager() {
 		return getDefaultManager( Locale.getDefault() );
 	}
 	
 	public static I18NManager getDefaultManager( Locale locale ) {
 		I18NManager manager = instance().getManager( locale );
 		if ( manager == null ) {
			throw new IllegalArgumentException("Locale " + locale.toString() + " not associated with any manager");
 		}
 		
 		return manager;
 	}
 	
 	@Override
 	public String normalizeLocale( Locale locale ) {
 		String normalized = locale.toString().toLowerCase();
 		normalized = normalized.replaceAll("[_\\-]", "");
 		return normalized;
 	}
 	
 	protected String findLocale( Locale locale ) {
 		String normalized = this.normalizeLocale( locale );
 		
 		for ( String key : this.managers.keySet() ) {
 			if ( key.contains( normalized ) ) {
 				return key;
 			}
 		}
 		
 		for ( String key : this.managers.keySet() ) {
 			if ( normalized.contains( key  ) ) {
 				return key;
 			}
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public I18NManager getManager( Locale locale ) {
 		String localeId = this.findLocale( locale );
 		if ( localeId == null ) {
 			return null;
 		}
 		
 		return this.managers.get( localeId );
 	}
 	
 	public static Set<I18NManager> createManagers(ResourcesLoader loader,
 			String[] resources, Locale[] locales, Locale defaultLocale ) throws ConfigException {
         StandardI18NFacade.defaultLocale = new Locale( instance().normalizeLocale( defaultLocale ) );
 
 		Set<I18NManager> managers = new HashSet<I18NManager>();
 		for ( int i = 0; i < resources.length; i++ ) {
 			managers.add( instance().createManager( loader, resources[i], locales[i] ) );
 		}
 		
 		return managers;
 	}	
 	
 	public static I18NManager createDefaultManager(ResourcesLoader loader,
 			String resources, Locale locale) throws ConfigException {
 		return instance().createManager( loader, resources, locale );
 	}	
 	
 	@Override
 	public I18NManager createManager(ResourcesLoader loader,
 			String resource, Locale locale ) throws ConfigException {
 		I18NManager manager = new StandardI18N(loader, resource, locale);
 		this.managers.put( this.normalizeLocale( locale ), manager );
 		return manager;
 	}
 
 }
