 package com.dsa.pcapneo.graph.repositories;
 
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.repository.GraphRepository;
 
 import com.dsa.pcapneo.domain.graph.Device;
 import com.dsa.pcapneo.domain.graph.DeviceType;
 import com.dsa.pcapneo.domain.graph.IpAddress;
 import com.dsa.pcapneo.domain.graph.User;
 import com.dsa.pcapneo.domain.graph.WebSite;
 
 public interface DeviceRepository extends GraphRepository<Device> {
 	Iterable<Device> findByDeviceTypeName(String name);
 
 	@Query ("START device=node:Device(hostName={0}) MATCH device-[:IS_A]->dt return dt")
 	DeviceType getDeviceType(String hostName);
 
 	@Query ("START device=node:Device(hostName={0}) MATCH device-[:USED_BY]->user return user")
 	Iterable<User> getUsersOfDevice(String hostName);
 	
 	@Query ("START device=node:Device(hostName={0}) " + 
			"MATCH device<-[:CONNECTS_FROM_DEVICE]-httpsession-[:CONNECTS_TO_WEBSITE]->site " +
 			"RETURN site")
 	Iterable<WebSite> getAllWebSitesVisitedByDevice(String hostName);
 	
 	@Query ("START ip=node:IpAddress({0}) " +
 			"MATCH ip<-[:CONNECTS_USING]-device " +
 			"RETURN device")
 	Iterable<Device> getDevicesUsingIpAddress(IpAddress ipaddr);
 
 	@Query ("START ip=node:IpAddress(ipAddr={0}) " +
 			"MATCH ip<-[:CONNECTS_USING]-device " +
 			"RETURN device")
 	Iterable<Device> getDevicesUsingIpAddress(String ipaddr);
 }
