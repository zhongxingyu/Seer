 package org.jims.modules.crossbow.infrastructure.gatherer;
 
 import javax.management.JMX;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 import java.util.LinkedList;
 import org.jims.modules.crossbow.enums.LinkStatistics;
 import org.jims.modules.crossbow.exception.LinkException;
 import org.jims.modules.crossbow.flow.FlowManagerMBean;
 import org.jims.modules.crossbow.flow.enums.FlowStatistics;
 import org.jims.modules.crossbow.infrastructure.worker.NameHelper;
 import org.jims.modules.crossbow.link.VNicManagerMBean;
 import org.jims.modules.crossbow.objectmodel.policy.Policy;
 import org.jims.modules.crossbow.objectmodel.resources.Appliance;
 import org.jims.modules.crossbow.objectmodel.resources.ApplianceType;
 import org.jims.modules.crossbow.objectmodel.resources.Interface;
 import org.jims.modules.crossbow.objectmodel.Assignments;
 import org.jims.modules.sg.service.wnservice.WNDelegateMBean;
 
 import org.jims.modules.crossbow.enums.LinkStatisticTimePeriod;
 
 
 /**
  *
  * @author cieplik
  */
 public class StatisticsGatherer implements StatisticsGathererMBean {
 
 	private Assignments assignments = null;
 	private WNDelegateMBean wnDelegate;
 
 	private static final Logger logger = Logger.getLogger( StatisticsGatherer.class );
 
 	public StatisticsGatherer( WNDelegateMBean wnDelegate ) {
 		this.wnDelegate = wnDelegate;
 	}
 
 	@Override
 	public Map< LinkStatistics, Long > getInterfaceStatistics( Interface iface ) {
 
		String url = assignments.getAssignment(iface);
 		VNicManagerMBean vNicManager = getVNicManager( url );
 
 		Map< LinkStatistics, Long > res = new HashMap< LinkStatistics, Long >();
 		Map< LinkStatistics, String > stats = null;
 
 		try {
 			stats = vNicManager.getByName( NameHelper.interfaceName( iface ) ).getStatistics();
 		} catch ( LinkException ex ) {
 			// TODO-DAWID
 		}
 
 		if ( null != stats ) {
 			for ( Map.Entry< LinkStatistics, String > s : stats.entrySet() ) {
 				res.put( s.getKey(), Long.valueOf( s.getValue() ) );
 			}
 		}
 
 		return res;
 
 	}
 
 	@Override
 	public List< Map<LinkStatistics, Long> > getInterfaceStatistics( Interface iface, LinkStatisticTimePeriod period ) {
 
		String url = assignments.getAssignment(iface);
 		VNicManagerMBean vNicManager = getVNicManager( url );
 
 		try {
 			return vNicManager.getByName( NameHelper.interfaceName( iface ) ).getStatistics(period);
 		} catch ( LinkException ex ) {
 			// TODO-DAWID
 			ex.printStackTrace();
 		}
 
 		return null;
 
 	}
 
 	@Override
 	public List< Map<LinkStatistics, Long> > getPolicyStatistics( Policy policy, LinkStatisticTimePeriod period ) {
 
 		Map< LinkStatistics, Long > res = new HashMap< LinkStatistics, Long >();
 
		String url = assignments.getAssignment(policy);
 		FlowManagerMBean flowManager = getFlowManager( url );
 
 		List< Map<LinkStatistics, Long> > list = new LinkedList< Map<LinkStatistics, Long> >();
 		List<Map< FlowStatistics, Long >> flowList = flowManager.getByName( NameHelper.policyName( policy ) ).getStatistics(period);
 
 		for( Map<FlowStatistics, Long> map : flowList ) {
 			Map<LinkStatistics, Long> linkMap = new HashMap<LinkStatistics, Long>();
 			for ( Map.Entry< FlowStatistics, Long > s : map.entrySet() ) {
 				linkMap.put( convert( s.getKey() ), s.getValue() );
 			}
 			list.add(linkMap);
 		}
 		return list;
 
 	}
 
 	@Override
 	public Map< LinkStatistics, Long > getPolicyStatistics( Policy policy ) {
 
 		Map< LinkStatistics, Long > res = new HashMap< LinkStatistics, Long >();
 
		String url = assignments.getAssignment(policy);
 		FlowManagerMBean flowManager = getFlowManager( url );
 
 		for ( Map.Entry< FlowStatistics, Long > s : flowManager.getByName( NameHelper.policyName( policy ) ).getStatistics().entrySet() ) {
 			res.put( convert( s.getKey() ), s.getValue() );
 		}
 
 		return res;
 
 	}
 
 	public FlowManagerMBean getFlowManager( String url ) {
 
 		FlowManagerMBean flowManager = null;
 		try {
 			MBeanServerConnection mbsc = JMXConnectorFactory.connect(
 				new JMXServiceURL( url )
 			).getMBeanServerConnection();
 
 			flowManager = JMX.newMBeanProxy(
 				mbsc, new ObjectName( "Crossbow:type=FlowManager" ), FlowManagerMBean.class
 			);
 		} catch ( Exception ex ) {
 			logger.error( "Error while querying MBean server (url: " + url + ")", ex );
 		}
 
 		return flowManager;
 	}
 
 	public VNicManagerMBean getVNicManager( String url ) {
 
 		VNicManagerMBean vnicManager = null;
 
 		try {
 			MBeanServerConnection mbsc = JMXConnectorFactory.connect(
 				new JMXServiceURL( url )
 			).getMBeanServerConnection();
 
 			vnicManager = JMX.newMBeanProxy(
 				mbsc, new ObjectName( "Crossbow:type=VNicManager" ), VNicManagerMBean.class
 		);
 		} catch ( Exception ex ) {
 			logger.error( "Error while querying MBean server (url: " + url + ")", ex );
 		}
 
 		return vnicManager;
 	}
 
 
 	/*
 	 * JConsole only
 	 */
 
 	@Override
 	public Map< String, String > get_InterfaceStatistics( String projectId, String appliance, String resourceId ) {
 
 		Map< String, String > res = new HashMap< String, String >();
 
 		Interface iface = new Interface( resourceId, projectId );
 
 		Appliance app = new Appliance( appliance, projectId, ApplianceType.MACHINE );
 		app.addInterface( iface );
 
 		for ( Map.Entry< LinkStatistics, Long > s : getInterfaceStatistics( iface ).entrySet() ) {
 			res.put( s.getKey().toString(), s.getValue().toString() );
 		}
 
 		return res;
 
 	}
 
 
 	private LinkStatistics convert( FlowStatistics flowStatistics ) {
 
 		switch ( flowStatistics ) {
 			case IPACKETS: return LinkStatistics.IPACKETS;
 			case OBYTES:   return LinkStatistics.OBYTES;
 			case OPACKETS: return LinkStatistics.OPACKETS;
 			default:       return LinkStatistics.RBYTES;
 		}
 
 	}
 
 }
