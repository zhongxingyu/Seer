 package org.aptrust.client.utilities;
 
 import java.util.Properties;
 
 import org.aptrust.client.api.AptrustClient;
 import org.aptrust.client.api.IngestProcessSummary;
 import org.aptrust.client.api.IngestStatus;
 import org.aptrust.client.api.InstitutionInfo;
 import org.aptrust.client.api.Summary;
 import org.aptrust.client.impl.AptrustClientImpl;
 import org.aptrust.client.impl.PropertiesClientConfig;
 import org.aptrust.common.exception.AptrustException;
 
 public class ClientUtil {
 
     public static void main(String [] args) throws Exception {
         Properties p = new Properties();
         p.load(ClientUtil.class.getClassLoader().getResourceAsStream("client-config.properties"));
         PropertiesClientConfig config = new PropertiesClientConfig(p);
         AptrustClientImpl client = new AptrustClientImpl(config);
        System.out.println("Solr URL: " + p.getProperty("solr-url"));
         System.out.println("Institutions:");
         for (InstitutionInfo i : client.getInstitutions()) {
             summarize(i, client);
         }
         
     }
     
     public static void summarize(InstitutionInfo i, AptrustClient client) throws AptrustException {
         System.out.println("  " + i.getId() + " - \"" + i.getFullName() + "\"");
         Summary s = client.getSummary(i.getId());
         System.out.println("    Usage:      " + s.getBytesUsed() + " bytes");
         System.out.println("    Packages:   " + s.getPackageCount());
         System.out.println("     DPN-bound: " + s.getDpnBoundPackageCount());
         System.out.println("     Failed:    " + s.getFailedPackageCount());
         System.out.println("     Public:    " + s.getPublicPackageCount());
        System.out.println("     Private:   " + s.getPrivatePackageCount());
         System.out.println("    Objects:    " + s.getObjectCount());
         System.out.println();
         System.out.println("Current Operations:");
         for (IngestProcessSummary ips : client.findIngestProcesses(i.getId(), null, null, IngestStatus.IN_PROGRESS)) {
             summarize(ips);
         }
         
         System.out.println();
         System.out.println("Failed Operations:");
         for (IngestProcessSummary ips : client.findIngestProcesses(i.getId(), null, null, IngestStatus.FAILED)) {
             summarize(ips);
         }
     }
     
     public static void summarize(IngestProcessSummary ips) {
         System.out.println("      " + ips.getName() + " (" + ips.getInitiatingUser() + ") " + ips.getProgress() + "%");
     }
 
 }
