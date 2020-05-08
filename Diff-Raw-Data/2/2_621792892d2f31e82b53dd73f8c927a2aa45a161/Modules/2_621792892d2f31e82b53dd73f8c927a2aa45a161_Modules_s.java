 package org.mosaic.shell.impl.command.std;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.net.URL;
 import java.util.*;
 import javax.annotation.Nonnull;
 import org.mosaic.lifecycle.Module;
 import org.mosaic.lifecycle.ModuleManager;
 import org.mosaic.lifecycle.annotation.Bean;
 import org.mosaic.lifecycle.annotation.ServiceRef;
 import org.mosaic.shell.Console;
 import org.mosaic.shell.annotation.*;
 
 import static com.google.common.base.Strings.padStart;
 import static org.joda.time.Duration.standardSeconds;
 
 /**
  * @author arik
  */
 @Bean
 public class Modules
 {
     @Nonnull
     private ModuleManager moduleManager;
 
     @ServiceRef
     public void setModuleManager( @Nonnull ModuleManager moduleManager )
     {
         this.moduleManager = moduleManager;
     }
 
     @Command( name = "install-module", label = "Install module", desc = "Install a new module from a given location." )
     public int installModule( @Nonnull Console console, @Arguments @Nonnull String... locations )
             throws IOException
     {
         int exitCode = 0;
         for( String location : locations )
         {
             URL url = new URL( location );
             console.println( "Installing module from '" + url + "'..." );
             try
             {
                 this.moduleManager.installModule( url ).waitForActivation( standardSeconds( 30 ) );
             }
             catch( Exception e )
             {
                 console.printStackTrace( "Could not install module from '" + location + "': " + e.getMessage(), e );
                 exitCode = 1;
             }
         }
         return exitCode;
     }
 
     @Command( name = "list-modules", label = "List modules", desc = "List installed modules." )
     public void listModules( @Nonnull Console console ) throws IOException
     {
         Console.TableHeaders table = console.createTable();
         table.addHeader( "ID", 4 );
         table.addHeader( "Name" );
         table.addHeader( "Version", 20 );
         table.addHeader( "State", 13 );
         table.addHeader( "Last Update", 30 );
         table.addHeader( "Services", 7 );
 
         Console.TablePrinter printer = table.start();
         for( Module module : this.moduleManager.getModules() )
         {
             printer.print( module.getId(),
                            module.getName(),
                            module.getVersion(),
                            module.getState(),
                            new Date( module.getLastModified() ),
                            module.getExportedServices().size() );
         }
         printer.done();
     }
 
     @Command( name = "inspect-module", label = "Inspect module(s)", desc = "Inspects and show information about installed modules." )
     public void inspectModule( @Nonnull Console console,
 
                                @Option @Alias( "e" ) @Desc( "use exact matching of module names" )
                                boolean exact,
 
                                @Option @Alias( "h" ) @Desc( "show module headers" )
                                boolean showHeaders,
 
                                @Option @Alias( "s" ) @Desc( "show module service declarations and usages" )
                                boolean showServices,
 
                                @Option @Alias( "p" ) @Desc( "show module package imports and exports" )
                                boolean showPackages,
 
                                @Option @Alias( "c" ) @Desc( "show module content" )
                                boolean showContents,
 
                                @Option @Alias( "a" ) @Desc( "show all" )
                                boolean all,
 
                                @Nonnull @Arguments String... moduleNames ) throws IOException
     {
         if( all )
         {
             showHeaders = !showHeaders;
             showServices = !showServices;
             showPackages = !showPackages;
             showContents = !showContents;
         }
 
         Collection<Module> knownModules = this.moduleManager.getModules();
         for( String moduleName : moduleNames )
         {
             for( Module module : knownModules )
             {
                 if( matches( module, moduleName, exact ) )
                 {
                     displayModuleInfo( console, module );
 
                     if( showHeaders )
                     {
                         displayModuleHeaders( console, module );
                     }
 
                     if( showServices )
                     {
                         displayModuleServices( console, module );
                     }
 
                     if( showPackages )
                     {
                         displayModulePackages( console, module );
                     }
 
                     if( showContents )
                     {
                         displayModuleContents( console, module );
                     }
                 }
             }
         }
     }
 
     private void displayModuleInfo( @Nonnull Console console, @Nonnull Module module ) throws IOException
     {
         console.println();
         console.println( "GENERAL INFORMATION" );
         console.print( 8, padStart( "ID", 30, ' ' ) ).print( ": " ).println( module.getId() );
         console.print( 8, padStart( "Name", 30, ' ' ) ).print( ": " ).println( module.getName() );
         console.print( 8, padStart( "Version", 30, ' ' ) ).print( ": " ).println( module.getVersion() );
         console.print( 8, padStart( "Location", 30, ' ' ) ).print( ": " ).println( module.getPath() );
         console.print( 8, padStart( "State", 30, ' ' ) ).print( ": " ).println( module.getState().name() );
         console.print( 8, padStart( "Modification time", 30, ' ' ) ).print( ": " ).println( new Date( module.getLastModified() ) );
     }
 
     private void displayModuleHeaders( @Nonnull Console console, @Nonnull Module module ) throws IOException
     {
         console.println();
         console.println( "HEADERS" );
         for( Map.Entry<String, String> entry : module.getHeaders().entrySet() )
         {
             String headerName = entry.getKey();
             if( !"Import-Package".equals( headerName ) && !"Ignore-Package".equals( headerName ) )
             {
                 console.print( 8, padStart( headerName, 30, ' ' ) ).print( ": " ).println( entry.getValue() );
             }
         }
     }
 
     private void displayModuleServices( @Nonnull Console console, @Nonnull Module module ) throws IOException
     {
         console.println();
         console.println( "EXPORTED SERVICES" );
         Collection<Module.ServiceExport> exportedServices = module.getExportedServices();
         for( Module.ServiceExport export : exportedServices )
         {
             console.println( 8, "----------------------------------------------------------------------------------" );
             console.println( 8, export.getType() );
 
             printServiceExportProperties( console, export );
 
             Collection<Module> consumers = export.getConsumers();
             if( consumers.isEmpty() )
             {
                 console.println( 8, "Not used by any module" );
             }
             else
             {
                 console.println( 8, "Used by:" );
                 for( Module consumer : consumers )
                 {
                     console.println( 30, consumer );
                 }
             }
         }
         if( !exportedServices.isEmpty() )
         {
             console.println( 8, "----------------------------------------------------------------------------------" );
         }
 
 
         console.println();
         console.println( "IMPORTED SERVICES" );
        Collection<Module.ServiceExport> importedServices = module.getExportedServices();
         for( Module.ServiceExport export : importedServices )
         {
             console.println( 8, "----------------------------------------------------------------------------------" );
             console.println( 8, export.getType() );
             console.println( 12, "provided by: " + export.getProvider() );
 
             printServiceExportProperties( console, export );
         }
         if( !importedServices.isEmpty() )
         {
             console.println( 8, "----------------------------------------------------------------------------------" );
         }
     }
 
     private void printServiceExportProperties( Console console, Module.ServiceExport export ) throws IOException
     {
         console.println( 8, "Properties:" );
         for( Map.Entry<String, Object> entry : export.getProperties().entrySet() )
         {
             String propertyName = entry.getKey();
             Object value = entry.getValue();
             if( value != null )
             {
                 Class<?> type = value.getClass();
                 if( type.isArray() )
                 {
                     int length = Array.getLength( value );
                     List<Object> list = new ArrayList<>( length );
                     for( int i = 0; i < length; i++ )
                     {
                         list.add( Array.get( value, i ) );
                     }
                     value = list.toString();
                 }
             }
             console.print( 10, padStart( propertyName, 30, ' ' ) ).print( ": " ).println( value );
         }
     }
 
     private void displayModulePackages( @Nonnull Console console, @Nonnull Module module ) throws IOException
     {
         // TODO arik: implement displayModulePackages([console, module])
     }
 
     private void displayModuleContents( @Nonnull Console console, @Nonnull Module module ) throws IOException
     {
         console.println();
         console.println( "CONTENTS" );
         for( String resource : module.getResources() )
         {
             console.println( 8, resource );
         }
     }
 
     private boolean matches( Module module, String moduleName, boolean exact )
     {
         if( exact && module.getName().equalsIgnoreCase( moduleName ) )
         {
             return true;
         }
         else if( !exact && module.getName().toLowerCase().contains( moduleName.toLowerCase() ) )
         {
             return true;
         }
         else
         {
             try
             {
                 int moduleId = Integer.parseInt( moduleName );
                 if( moduleId == module.getId() )
                 {
                     return true;
                 }
             }
             catch( NumberFormatException ignore )
             {
                 // module name is not a module ID - ignore it
             }
         }
         return false;
     }
 }
