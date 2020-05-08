 package org.cyclopsgroup.jcli.jline;
 
 import java.beans.IntrospectionException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import jline.Completor;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.cyclopsgroup.jcli.AutoCompletable;
 import org.cyclopsgroup.jcli.QuotedStringTokenizer;
 import org.cyclopsgroup.jcli.spi.CliDefinition;
 import org.cyclopsgroup.jcli.spi.CliUtils;
 import org.cyclopsgroup.jcli.spi.OptionDefinition;
 
 /**
  * JLine completor implemented with JCli
  * 
  * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
  */
 public class CliCompletor
     implements Completor
 {
     private static List<String> filterList( List<String> list, String prefix )
     {
         if ( StringUtils.isEmpty( prefix ) || list == null )
         {
             return list;
         }
         List<String> results = new ArrayList<String>();
         for ( String item : list )
         {
             if ( item.startsWith( prefix ) )
             {
                 results.add( item );
             }
         }
         return results;
     }
 
     private final CliDefinition cli;
 
     private final AutoCompletable completable;
 
     private final QuotedStringTokenizer tokenizer;
 
     /**
      * @param cliBean Entyped AutoCompletable implementation or an normal bean
      * @param tokenizer Tokenizer for argument parsing
      * @throws IntrospectionException
      */
     public CliCompletor( final Object cliBean, final QuotedStringTokenizer tokenizer )
         throws IntrospectionException
     {
         Validate.notNull( cliBean, "Cli bean can't be NULL" );
         Validate.notNull( tokenizer, "String tokenizer can't be NULL" );
         cli = CliUtils.defineCli( cliBean.getClass() );
         if ( cliBean instanceof AutoCompletable )
         {
             this.completable = (AutoCompletable) cliBean;
         }
         else
         {
             this.completable = new AutoCompletable()
             {
                 public List<String> suggestArgument( String partialArgument )
                 {
                     return Collections.emptyList();
                 }
 
                 public List<String> suggestOption( String optionName, String partialOption )
                 {
                     return Collections.emptyList();
                 }
             };
         }
         this.tokenizer = tokenizer;
     }
 
     /**
      * @inheritDoc
      */
     @SuppressWarnings( "unchecked" )
     public int complete( final String command, final int cursor, final List suggestions )
     {
         ArgumentsInspector inspector = new ArgumentsInspector( cli );
         if ( StringUtils.isNotEmpty( command ) )
         {
             List<String> args = tokenizer.parse( command );
             for ( String arg : args )
             {
                 inspector.consume( arg );
             }
             if ( command.charAt( command.length() - 1 ) == ' ' )
             {
                 inspector.end();
             }
         }
         // System.err.println( "command=[" + command + "], cursor=" + cursor + ", state=" + inspector.getState().name()
         // + ", value=" + inspector.getCurrentValue() );
         List<String> candidates = new ArrayList<String>();
         switch ( inspector.getState() )
         {
             case READY:
                 for ( OptionDefinition o : inspector.getRemainingOptions() )
                 {
                     candidates.add( "-" + o.getName() );
                 }
                 Collections.sort( candidates );
                 candidates.addAll( suggestArguments( null ) );
                 break;
             case OPTION:
             case LONG_OPTION:
                 candidates.addAll( suggestOptionNames( inspector, inspector.getCurrentValue() ) );
                 break;
             case OPTION_VALUE:
                 candidates.addAll( suggestOptionValue( inspector.getCurrentOption(), inspector.getCurrentValue() ) );
                 break;
             case ARGUMENT:
                 candidates.addAll( suggestArguments( inspector.getCurrentValue() ) );
         }
         for ( String candidate : candidates )
         {
             suggestions.add( tokenizer.escape( candidate ) );
         }
         if ( StringUtils.isEmpty( command ) )
         {
             return 0;
         }
         if ( command.endsWith( " " ) )
         {
             return cursor;
         }
         return command.length() - inspector.getCurrentValue().length();
     }
 
     private List<String> suggestArguments( String partialArgument )
     {
         List<String> results;
         if ( StringUtils.isEmpty( partialArgument ) )
         {
             results = completable.suggestArgument( null );
         }
         else
         {
             results = completable.suggestArgument( partialArgument );
             if ( results == null )
             {
                return filterList( completable.suggestArgument( null ), partialArgument );
             }
         }
         if ( results == null )
         {
             results = Collections.emptyList();
         }
         else
         {
             Collections.sort( results );
         }
         return results;
     }
 
     private List<String> suggestOptionNames( ArgumentsInspector inspector, String value )
     {
         List<String> results = new ArrayList<String>();
         for ( OptionDefinition o : inspector.getRemainingOptions() )
         {
             if ( value.startsWith( "--" ) && o.getOption().longName() != null
                 && ( "--" + o.getOption().longName() ).startsWith( value ) )
             {
                 results.add( "--" + o.getOption().longName() );
             }
             else if ( value.startsWith( "-" ) && ( "-" + o.getOption().name() ).startsWith( value ) )
             {
                 results.add( "-" + o.getOption().name() );
             }
         }
         Collections.sort( results );
         return results;
     }
 
     private List<String> suggestOptionValue( OptionDefinition option, String partialValue )
     {
         List<String> results;
         if ( StringUtils.isEmpty( partialValue ) )
         {
             results = completable.suggestOption( option.getName(), null );
         }
         else
         {
             results = completable.suggestOption( option.getName(), partialValue );
             if ( results == null )
             {
                 results = filterList( completable.suggestOption( option.getName(), null ), partialValue );
             }
         }
         if ( results == null )
         {
             results = Collections.emptyList();
         }
         else
         {
             Collections.sort( results );
         }
         return results;
     }
 }
