 package sslhoff;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class SiteChecker {
 	private List<String> siteUrls;
 	private ArrayList<ProxyDefinition> proxyDefs;
 	private X509CertificateLogger logger;
 	
 	public SiteChecker(List<String> siteUrls, ArrayList<ProxyDefinition> proxyDefs, X509CertificateLogger logger)
 	{
 		this.siteUrls = siteUrls;
 		this.proxyDefs = proxyDefs;
		this.logger = logger;
 	}
 	
 	public void check() throws Exception
 	{
 		Iterator<ProxyDefinition> it = proxyDefs.iterator();
 		while(it.hasNext())
 		{
 			Thread th = new Thread(new SiteCheckerRunnable(it.next(), this.siteUrls, logger));
 			th.start();
 		}
 	}
 }
