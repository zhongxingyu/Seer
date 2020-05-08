 package com.oreilly.common.text;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.oreilly.common.io.Yaml;
 
 
 public class Translater {
 	
 	// raw translation data - [name], [value map]
 	HashMap< String, TranslationRecord > translations = new HashMap< String, TranslationRecord >();
 	
 	
 	public Translater( File translationDirectory, Logger errorLog ) {
 		// load the contents of the translation files..
 		List< YamlConfiguration > files = Yaml.loadYamlDirectory( translationDirectory, errorLog );
 		for ( YamlConfiguration config : files ) {
 			String name = config.getString( TranslationFileConstants.name );
 			List< String > inheritsFrom = config.getStringList( TranslationFileConstants.inherits );
 			ConfigurationSection content = config.getConfigurationSection( TranslationFileConstants.content );
 			if ( ( name != null ) & ( content != null ) ) {
 				TranslationRecord record = new TranslationRecord( name );
				translations.put( name, record );
 				if ( inheritsFrom != null )
 					record.inheritsFrom.addAll( inheritsFrom );
 				for ( String key : content.getKeys( true ) ) {
 					Object item = content.get( key );
 						if (( item instanceof String ) | ( item instanceof Double ) | ( item instanceof Integer ))
 							record.rawTranslations.put( key, item.toString());
 				}
 			}
 		}
 		// Compile translations...
 		LinkedList< TranslationRecord > toCompile = new LinkedList< TranslationRecord >();
		toCompile.addAll( translations.values() );
 		boolean progress = true;
 		TranslationRecord parent = null;
 		while ( progress ) {
 			progress = false;
 			LinkedList< TranslationRecord > toRemove = new LinkedList< TranslationRecord >();
 			for ( TranslationRecord record : toCompile ) {
 				// if all the parent records are compiled (or if there are no parent records)..
 				//  .. then we can compile this one.
 				boolean parentsCompiled = true;
 				if ( record.inheritsFrom != null )
 					if ( record.inheritsFrom.size() > 0 )
 						for ( String parentName : record.inheritsFrom ) {
 							parent = translations.get( parentName );
 							if ( parent == null ) {
 								// TODO: Some form of error based on "parent doesn't exist!"
 								parentsCompiled = false;
 								break;
 							}
 							if ( parent.compiled == false ) {
 								parentsCompiled = false;
 								break;
 							}
 						}
 				if ( parentsCompiled ) {
 					progress = true;
 					// 'import' the data from the parent records
 					for ( String parentName : record.inheritsFrom ) {
 						parent = translations.get( parentName );
 						record.compiledTranslations.putAll( parent.compiledTranslations );
 					}
 					// apply the data in the this record
 					record.compiledTranslations.putAll( record.rawTranslations );
 					// mark as compiled
 					record.compiled = true;
 					// put this record in the remove queue
 					toRemove.add( record );
 				}
 			}
 			// now we are outside the loop, remove any records that have been compiled this round
 			for ( TranslationRecord record : toRemove )
 				toCompile.remove( record );
 		}
 		// if no changes have happened this round, then we have unresolvable dependencies - throw up an error.
 		// TODO:
 	}
 	
 	
 	public HashMap< String, String > getTranslation( String name ) {
 		TranslationRecord record = translations.get( name );
 		if ( record == null )
 			return null; //TODO: Throw an error
 		return record.compiledTranslations;
 	}
 	
 }
 
 
 class TranslationRecord {
 	
 	public String name;
 	public ArrayList< String > inheritsFrom = new ArrayList< String >();
 	public HashMap< String, String > rawTranslations = new HashMap< String, String >();
 	public HashMap< String, String > compiledTranslations = new HashMap< String, String >();
 	boolean compiled = false;
 	
 	
 	public TranslationRecord( String name ) {
 		this.name = name;
 	}
 	
 }
 
 
 class TranslationFileConstants {
 	
 	public static final String name = "header.name";
 	public static final String inherits = "header.inheritsFrom";
 	public static final String content = "translations";
 }
