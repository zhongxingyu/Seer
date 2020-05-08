 package org.jims.modules.crossbow.infrastructure.worker;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 import org.jims.modules.crossbow.objectmodel.policy.Policy;
 import org.jims.modules.crossbow.objectmodel.resources.Appliance;
 import org.jims.modules.crossbow.objectmodel.resources.ApplianceType;
 import org.jims.modules.crossbow.objectmodel.resources.Interface;
 import org.jims.modules.crossbow.objectmodel.resources.Switch;
 
 
 /**
  *
  * @author cieplik
  */
 public class NameHelper {
 
 	public static String interfaceName( Interface iface ) {
 		return ( ApplianceType.ROUTER.equals( iface.getAppliance().getType() )
 		         ? routerName( iface.getAppliance() )
 		         : machineName( iface.getAppliance() ) )
 		       + SEP + iface.getResourceId();
 	}
 
 	public static String policyName( Policy p ) {
 		return interfaceName( p.getInterface() ) + SEP + p.getName();
 	}
 
 	public static String switchName( Switch s ) {
 		return s.getProjectId() + SEP + SWITCH + s.getResourceId();
 	}
 
 	public static String machineName( Appliance a ) {
 		return a.getProjectId() + SEP + MACHINE + a.getResourceId();
 	}
 
 	public static String routerName( Appliance a ) {
 		return a.getProjectId() + SEP + ROUTER + a.getResourceId();
 	}
 
 
 	public static String extractAppliance( String s ) {
 
 		logger.debug( "Extracting appliance (input string: " + s + ")" );
 
 		Pattern p = Pattern.compile( capgroup( REG_APPLIANCE_NAME ) + REG_SEP + REG_ANY );
 
 		Matcher m = p.matcher( s );
 
 		return ( m.matches() ? m.group( 1 ) : "" );
 
 	}
 
 
 	public static Object extractInterface( String s ) {
 
 		logger.debug( "Extracting interface (input string: " + s + ")" );
 
 		Pattern p = Pattern.compile( capgroup( REG_INTERFACE_NAME ) + REG_SEP + REG_ANY );
 		Matcher m = p.matcher( s );
 
 		return ( m.matches() ? m.group( 1 ) : "" );
 
 	}
 
 
 	private static String capgroup( String s ) {
 		return "(" + s + ")";
 	}
 
 	private static String group( String s ) {
 		return "(?:" + s + ")";
 	}
 
 
 	public final static String SEP = "..";
 	public final static String ROUTER = "R";
 	private final static String MACHINE = "M";
 	private final static String SWITCH = "S";
 
 	private final static String REG_SEP = Pattern.quote( SEP );
 	private final static String REG_PROJECT_ID = "[a-zA-Z](?:(?:\\.[a-zA-Z])|(?:[a-zA-Z]))*";  // TODO
 	private final static String REG_RESOURCE_ID = "[a-zA-Z]+[0-9]+";  // TODO
 	private final static String REG_ANY = ".*";  // TODO
 
 	public final static String REG_SWITCH_NAME
 		= group( REG_PROJECT_ID + REG_SEP + SWITCH + REG_RESOURCE_ID );
 	public final static String REG_SWITCH_NAME_CG
 		= group( capgroup( REG_PROJECT_ID ) + REG_SEP + SWITCH + capgroup( REG_RESOURCE_ID ) );
 
 	public final static String REG_MACHINE_NAME
		= group( REG_PROJECT_ID + REG_SEP + MACHINE + REG_PROJECT_ID );
 	public final static String REG_MACHINE_NAME_CG
		= group( capgroup( REG_PROJECT_ID ) + REG_SEP + capgroup( MACHINE ) + capgroup( REG_PROJECT_ID ) );
 
 	public final static String REG_ROUTER_NAME
		= group( REG_PROJECT_ID + REG_SEP + ROUTER + REG_PROJECT_ID );
 	public final static String REG_ROUTER_NAME_CG
		= group( capgroup( REG_PROJECT_ID ) + REG_SEP + capgroup( ROUTER ) + capgroup( REG_PROJECT_ID ) );
 
 	public final static String REG_APPLIANCE_NAME
 		= group( REG_MACHINE_NAME + "|" + REG_ROUTER_NAME );
 	public final static String REG_APPLIANCE_NAME_CG
 		= group( capgroup( REG_PROJECT_ID ) + REG_SEP
 		  + capgroup( ROUTER + "|" + MACHINE ) + capgroup( REG_PROJECT_ID ) );
 
 	public final static String REG_INTERFACE_NAME
 		= group( REG_APPLIANCE_NAME + REG_SEP + REG_RESOURCE_ID );
 	public final static String REG_INTERFACE_NAME_CG
 		= group( capgroup( REG_PROJECT_ID ) + REG_SEP + capgroup( ROUTER + "|" + MACHINE )
		  + capgroup( REG_PROJECT_ID ) + REG_SEP + capgroup( REG_RESOURCE_ID ) );
 
 	public final static String REG_POLICY_NAME_CG
 		= group( REG_INTERFACE_NAME_CG + REG_SEP + capgroup( REG_PROJECT_ID ) );
 
 
 	private static final Logger logger = Logger.getLogger( NameHelper.class );
 
 }
