 package net.praqma.hudson.nametemplates;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.praqma.hudson.exception.TemplateException;
 import net.praqma.hudson.scm.CCUCMState.State;
 
 public class NameTemplate {
 	
 	private static Map<String, Template> templates = new HashMap<String, Template>();
 	
 	static {
 		templates.put( "date", new DateTemplate() );
 		templates.put( "time", new TimeTemplate() );
 		templates.put( "stream", new StreamTemplate() );
 		templates.put( "component", new ComponentTemplate() );
 		templates.put( "baseline", new BaselineTemplate() );
 		templates.put( "project", new ProjectTemplate() );
 		templates.put( "ccversion", new ClearCaseBuildNumberTemplate() );
 		templates.put( "number", new NumberTemplate() );
 		templates.put( "user", new UserTemplate() );
 	}
 	
 	private static Pattern rx_ = Pattern.compile( "(\\[.*?\\])" );
	private static Pattern rx_checkFinal = Pattern.compile( "^[\\w-]*$" );
 	
 	public static void validateTemplates( State state ) {
 		
 		Set<String> keys = templates.keySet();
 		for( String key : keys ) {
 			String r;
 			try {
 				r = templates.get( key ).parse( state, "" );
 				System.out.println( key + " = " + r );
 			} catch (TemplateException e) {
 				System.out.println( "Could not validate " + key );
 			}
 		}
 	}
 	
 	public static String trim( String template ) {
         if( template.matches( "^\".+\"$" ) ) {
         	template = template.substring( 1, template.length()-1 );
         }
         
         return template;
 	}
 	
 	public static boolean testTemplate( String template ) throws TemplateException {
 		Matcher m = rx_.matcher( template );
 		String result = template;
 		
 		while( m.find() ) {
 			String replace = m.group(1);
 			String templateName = replace.toLowerCase().substring( 1, replace.length()-1 );
 			
 			/*  Pre-process template */
 			if( templateName.contains( "=" ) ) {
 				String[] s = templateName.split( "=" );
 				templateName = s[0];
 			}
 			
 			if( !templates.containsKey( templateName ) ) {
 				throw new TemplateException( "The template " + templateName + " does not exist" );
 			} else {
 				result = result.replace( replace, "" );
 			}
 		}
 		
 		Matcher f = rx_checkFinal.matcher( result );
 		if( !f.find() ) {
 			throw new TemplateException( "The template is not correct" );
 		}
 		
 		return true;
 	}
 	
 	public static String parseTemplate( String template, State state ) throws TemplateException {
 		
 		Matcher m = rx_.matcher( template );
 		String result = template;
 		
 		while( m.find() ) {
 			String replace = m.group(1);
 			String templateName = replace.toLowerCase().substring( 1, replace.length()-1 );
 			String args = null;
 			
 			/*  Pre-process template */
 			if( templateName.contains( "=" ) ) {
 				String[] s = templateName.split( "=" );
 				templateName = s[0];
 				args = s[1];
 			}
 			
 			if( !templates.containsKey( templateName ) ) {
 				throw new TemplateException( "The template " + templateName + " does not exist" );
 			} else {
 				String r = templates.get( templateName ).parse( state, args );
 				result = result.replace( replace, r );
 			}
 		}
 		
 		Matcher f = rx_checkFinal.matcher( result );
 		if( !f.find() ) {
 			throw new TemplateException( "The template is not correct" );
 		}
 		
 		return result;
 	}
 }
