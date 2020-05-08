 package de.taimos.aws.route53update;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 
 import com.amazonaws.regions.Region;
 import com.amazonaws.regions.Regions;
 import com.amazonaws.services.route53.AmazonRoute53Client;
 import com.amazonaws.services.route53.model.Change;
 import com.amazonaws.services.route53.model.ChangeBatch;
 import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
 import com.amazonaws.services.route53.model.HostedZone;
 import com.amazonaws.services.route53.model.ListHostedZonesResult;
 import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
 import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
 import com.amazonaws.services.route53.model.RRType;
 import com.amazonaws.services.route53.model.ResourceRecord;
 import com.amazonaws.services.route53.model.ResourceRecordSet;
 
 import de.taimos.httputils.WS;
 
 public class Starter {
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length != 2) {
			System.out.println("usage: java -jar route53-updater.jar <domain> <host>");
 			System.exit(1);
 		}
 		String domain = args[0] + ".";
 		System.out.println("Domain: " + domain);
 		String host = args[1] + "." + domain;
 		System.out.println("Host: " + host);
 		String zoneId = Starter.findZoneId(domain);
 		
 		String publicHostname = Starter.getPublicHostname();
 		ResourceRecordSet set = Starter.findCurrentSet(zoneId, host);
 		
 		List<Change> changes = new ArrayList<>();
 		if (set != null) {
 			System.out.println("Deleting current set: " + set);
 			changes.add(new Change("DELETE", set));
 		}
 		ResourceRecordSet rrs = new ResourceRecordSet(host, RRType.CNAME).withTTL(60L).withResourceRecords(new ResourceRecord(publicHostname));
 		System.out.println("Creating new set: " + rrs);
 		changes.add(new Change("CREATE", rrs));
 		
 		try {
 			AmazonRoute53Client cl = Starter.createClient();
 			ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest(zoneId, new ChangeBatch(changes));
 			cl.changeResourceRecordSets(req);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static ResourceRecordSet findCurrentSet(String zoneId, String host) {
 		AmazonRoute53Client cl = Starter.createClient();
 		ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest(zoneId);
 		ListResourceRecordSetsResult sets = cl.listResourceRecordSets(req);
 		List<ResourceRecordSet> recordSets = sets.getResourceRecordSets();
 		for (ResourceRecordSet rrs : recordSets) {
 			if (rrs.getName().equals(host)) {
 				return rrs;
 			}
 		}
 		return null;
 	}
 	
 	private static String findZoneId(String domain) {
 		AmazonRoute53Client cl = Starter.createClient();
 		ListHostedZonesResult zones = cl.listHostedZones();
 		List<HostedZone> hostedZones = zones.getHostedZones();
 		for (HostedZone hz : hostedZones) {
 			if (hz.getName().equals(domain)) {
 				return hz.getId();
 			}
 		}
 		throw new RuntimeException("Cannot find zone: " + domain);
 	}
 	
 	private static String getPublicHostname() {
 		HttpResponse res = WS.url("http://169.254.169.254/latest/meta-data/public-hostname").get();
 		return WS.getResponseAsString(res);
 	}
 	
 	private static AmazonRoute53Client createClient() {
 		AmazonRoute53Client cl = new AmazonRoute53Client();
 		cl.setRegion(Region.getRegion(Regions.EU_WEST_1));
 		return cl;
 	}
 }
