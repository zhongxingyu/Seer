 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import com.xerox.amazonws.sdb.Domain;
 import com.xerox.amazonws.sdb.DomainMetadataResult;
 import com.xerox.amazonws.sdb.Item;
 import com.xerox.amazonws.sdb.ItemAttribute;
 import com.xerox.amazonws.sdb.ItemListener;
 import com.xerox.amazonws.sdb.ListDomainsResult;
 import com.xerox.amazonws.sdb.QueryResult;
 import com.xerox.amazonws.sdb.QueryWithAttributesResult;
 import com.xerox.amazonws.sdb.SDBException;
 import com.xerox.amazonws.sdb.SimpleDB;
 
 /**
  * Sample application demonstrating various operations against SDS.  
  *
  */
 public class sdbShell {
 	static int itemCount;
 
 	/**
 	 * Executes specified query against given domain while demonstrating pagination.
 	 *
 	 * @param domain	    query domain 
 	 * @param queryString	query string
 	 * @param maxResults	maximum number of values to return per page of results
 	 */
 	private static void executeQuery(Domain domain, String queryString, int maxResults) {
 
         String nextToken = "";
         do {
             try {
                 QueryResult result = domain.listItems(queryString, nextToken, maxResults);
 				List<Item> items = result.getItemList();
                 for (Item i : items) {
                     System.out.println(i.getIdentifier());
                 }
                 nextToken = result.getNextToken();
             }
             catch (SDBException ex) {
                 System.out.println("Query '" + queryString + "' Failure: ");
                 ex.printStackTrace();
             }
         } while (nextToken != null && nextToken.trim().length() > 0);
         System.out.println("Done.");
 	}
 
 	/**
 	 * Executes specified query against given domain.
 	 *
 	 * @param domain	    query domain 
 	 * @param queryString	query string
 	 */
 	private static void executeQuery(Domain domain, String queryString) {
         executeQuery(domain, queryString, 0);
     }
 
 	/**
 	 * Main execution body.
 	 *
 	 * @param args	    command line arguments (none required or processed)
 	 */
     public static void main(String [] args) {
 		if (args.length < 2) {
 			System.err.println("usage: sdbShell <access key> <secret key> [command file]");
 			System.exit(1);
 		}
 		try {
 			String awsAccessId = args[0];
 			String awsSecretKey = args[1];
 
 			if (awsAccessId == null || awsAccessId.trim().length() == 0)
 			{
 				System.out.println("Access key not set");
 				return;
 			}
 
 			if (awsSecretKey == null || awsSecretKey.trim().length() == 0)
 			{
 				System.out.println("Secret key not set");
 				return;
 			}
 			SimpleDB sds = new SimpleDB(awsAccessId, awsSecretKey, true);
 			sds.setSignatureVersion(2);
 
 			InputStream iStr = System.in;
 			if (args.length > 2) {
 				iStr = new FileInputStream(args[2]);
 			}
 			BufferedReader rdr = new BufferedReader(new InputStreamReader(iStr));
 			boolean done = false;
 			Domain dom = null;
 			while (!done) {
 				System.out.print("sdbShell> ");
 				String line = rdr.readLine();
 				if (line == null) { // exit, if end of input
 					System.exit(0);
 				}
 				StringTokenizer st = new StringTokenizer(line);
 				if (st.countTokens() == 0) {
 					continue;
 				}
 				String cmd = st.nextToken().toLowerCase();
 				if (cmd.equals("q") || cmd.equals("quit")) {
 					done = true;
 				}
 				else if (cmd.equals("h") || cmd.equals("?") || cmd.equals("help")) {
 					showHelp();
 				}
 				else if (cmd.equals("d") || cmd.equals("domains")) {
 					ListDomainsResult result = sds.listDomains();
 					List<Domain> domains = result.getDomainList();
 					for (Domain d : domains) {
 						System.out.println(d.getName());
 					}
 				}
 				else if (cmd.equals("ad") || cmd.equals("adddomain")) {
 					if (st.countTokens() != 1) {
 						System.out.println("Error: need domain name.");
 						continue;
 					}
 					Domain d = sds.createDomain(st.nextToken());
 				}
 				else if (cmd.equals("dd") || cmd.equals("deletedomain")) {
 					if (st.countTokens() != 1) {
 						System.out.println("Error: need domain name.");
 						continue;
 					}
 					sds.deleteDomain(st.nextToken());
 				}
 				else if (cmd.equals("sd") || cmd.equals("setdomain")) {
 					if (st.countTokens() != 1) {
 						System.out.println("Error: need domain name.");
 						continue;
 					}
 					dom = sds.getDomain(st.nextToken());
 				}
 				else if (cmd.equals("dm") || cmd.equals("domainmetadata")) {
 					if (checkDomain(dom)) {
 						DomainMetadataResult metadata = dom.getMetadata();
 						System.out.println("Domain Metadata for : "+dom.getName());
 						System.out.println(" ItemCount: "+metadata.getItemCount());
 						System.out.println(" AttributeNameCount: "+metadata.getAttributeNameCount());
 						System.out.println(" AttributeValueCount: "+metadata.getAttributeValueCount());
 						System.out.println(" ItemNamesSizeBytes: "+metadata.getItemNamesSizeBytes());
 						System.out.println(" AttributeNamesSizeBytes: "+metadata.getAttributeNamesSizeBytes());
 						System.out.println(" AttributeValuesSizeBytes: "+metadata.getAttributeValuesSizeBytes());
 						System.out.println(" Timestamp: "+metadata.getTimestamp());
 					}
 				}
 				else if (cmd.equals("aa") || cmd.equals("addattr")) {
 					if (checkDomain(dom)) {
 						if (st.countTokens() < 3) {
 							System.out.println("Error: need item id, attribute name and value.");
 							continue;
 						}
 						Item item = dom.getItem(st.nextToken());
 						List<ItemAttribute> list = new ArrayList<ItemAttribute>();
 						String key = st.nextToken();
 						String value = st.nextToken();
 						if (line.indexOf('"') > -1) {
 							value = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
 						}
 						list.add(new ItemAttribute(key, value, false));
 						item.putAttributes(list);
 					}
 				}
 				else if (cmd.equals("ra") || cmd.equals("replaceattr")) {
 					if (checkDomain(dom)) {
 						if (st.countTokens() < 3) {
 							System.out.println("Error: need item id, attribute name and value.");
 							continue;
 						}
 						Item item = dom.getItem(st.nextToken());
 						List<ItemAttribute> list = new ArrayList<ItemAttribute>();
 						String key = st.nextToken();
 						String value = st.nextToken();
 						if (line.indexOf('"') > -1) {
 							value = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
 						}
 						list.add(new ItemAttribute(key, value, true));
 						item.putAttributes(list);
 					}
 				}
 				else if (cmd.equals("da") || cmd.equals("deleteattr")) {
 					if (checkDomain(dom)) {
 						if (st.countTokens() != 2) {
 							System.out.println("Error: need item id and attribute name.");
 							continue;
 						}
 						Item item = dom.getItem(st.nextToken());
 						List<ItemAttribute> list = new ArrayList<ItemAttribute>();
 						list.add(new ItemAttribute(st.nextToken(), null, true));
 						item.deleteAttributes(list);
 					}
 				}
 				else if (cmd.equals("di") || cmd.equals("deleteitem")) {
 					if (checkDomain(dom)) {
 						if (st.countTokens() != 1) {
 							System.out.println("Error: need item id.");
 							continue;
 						}
 						dom.deleteItem(st.nextToken());
 					}
 				}
 				else if (cmd.equals("i") || cmd.equals("item")) {
 					if (checkDomain(dom)) {
 						if (st.countTokens() != 1) {
 							System.out.println("Error: need item id.");
 							continue;
 						}
 						Item item = dom.getItem(st.nextToken());
 						List<ItemAttribute> attrs = item.getAttributes(new ArrayList<String>());
 						System.out.println("Item : "+item.getIdentifier());
 						for (ItemAttribute attr : attrs) {
 							System.out.println(" "+attr.getName()+" = "+attr.getValue());
 						}
 					}
 				}
 				else if (cmd.equals("gi") || cmd.equals("getitems")) {
 					if (checkDomain(dom)) {
 						itemCount = 0;
 						dom.setMaxThreads(20);
 //						long start = System.currentTimeMillis();
 						//dom.listItemsAttributes("", new ItemListener() {
 						dom.listItemsWithAttributes("", null, new ItemListener() {
 								public synchronized void itemAvailable(String id, List<ItemAttribute> attrs) {
 									System.out.println("Item : "+id);
 									for (ItemAttribute attr : attrs) {
 										System.out.println("  "+attr.getName()+" = "+filter(attr.getValue()));
 									}
 									itemCount++;
 								}
 							});
 //						long end = System.currentTimeMillis();
 //						System.out.println("Time : "+((int)(end-start)/1000.0));
 //						System.out.println("Number of items returned : "+itemCount);
 					}
 				}
 				else if (cmd.equals("ga") || cmd.equals("getattributes")) {
 					if (checkDomain(dom)) {
 						itemCount = 0;
 //						long start = System.currentTimeMillis();
 						String nextToken = null;
 						do {
 							QueryWithAttributesResult qwar = dom.listItemsWithAttributes("", null, nextToken, 250);
 							Map<String, List<ItemAttribute>> items = qwar.getItems();
 							for (String id : items.keySet()) {
 								System.out.println("Item : "+id);
 								for (ItemAttribute attr : items.get(id)) {
 									System.out.println("  "+attr.getName()+" = "+filter(attr.getValue()));
 								}
 								itemCount++;
 							}
 							nextToken = qwar.getNextToken();
 						} while (nextToken != null && !nextToken.trim().equals(""));
 //						long end = System.currentTimeMillis();
 //						System.out.println("Time : "+((int)(end-start)/1000.0));
 //						System.out.println("Number of items returned : "+itemCount);
 					}
 				}
 				else if (cmd.equals("select")) {
 					if (checkDomain(dom)) {
 						itemCount = 0;
 //						long start = System.currentTimeMillis();
 						String nextToken = null;
 						do {
 							QueryWithAttributesResult qwar = dom.selectItems(line, nextToken);
 							Map<String, List<ItemAttribute>> items = qwar.getItems();
 							for (String id : items.keySet()) {
 								System.out.println("Item : "+id);
 								for (ItemAttribute attr : items.get(id)) {
 									System.out.println("  "+attr.getName()+" = "+filter(attr.getValue()));
 								}
 								itemCount++;
 							}
 							nextToken = qwar.getNextToken();
							System.out.println("Box Usage :"+qwar.getBoxUsage());
 						} while (nextToken != null && !nextToken.trim().equals(""));
 //						long end = System.currentTimeMillis();
 //						System.out.println("Time : "+((int)(end-start)/1000.0));
 //						System.out.println("Number of items returned : "+itemCount);
 					}
 				}
 				else if (cmd.equals("l") || cmd.equals("list")) {
 					if (checkDomain(dom)) executeQuery(dom, null);
 				}
 				else {
 					if (checkDomain(dom)) executeQuery(dom, line);
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (ex.getCause() != null) {
 				System.err.println("caused by : ");
 				ex.getCause().printStackTrace();
 			}
 		}
     }
 
 	private static boolean checkDomain(Domain dom) {
 		if (dom == null) {
 			System.out.println("domain must be set!");
 			return false;
 		}
 		return true;
 	}
 
 	private static void showHelp() {
 		System.out.println("SimpleDB Shell Commands:");
 		System.out.println("adddomain(ad) <domain name> : add new domain");
 		System.out.println("deletedomain(dd) <domain name> : delete domain (not functional in SDS yet)");
 		System.out.println("domains(d) : list domains");
 		System.out.println("setdomain(sd) <domain name> : set current domain");
 		System.out.println("domainmetadata(dm) : show current domain metadata");
 		System.out.println("addattr(aa) <item id> <attr name> <attr value> : add attribute to item in current domain");
 		System.out.println("replaceattr(ra) <item id> <attr name> <attr value> : replace attribute to item in current domain");
 		System.out.println("deleteattr(da) <item id> <attr name> : delete attribute of item in current domain");
 		System.out.println("deleteitem(di) <item id> : delete item in current domain");
 		System.out.println("list(l) or <filter string> : lists items matching filter in current domain");
 		System.out.println("item(i) <item id> : shows item attributes");
 		System.out.println("select <expression> : runs a SQL like query against the domain specified");
 		System.out.println("getitems(gi) : shows attributes for multiple items");
 		System.out.println("help(h,?) : show help");
 		System.out.println("quit(q) : exit the shell");
 	}
 
 	private static String filter(String val) {
 		if (val.length() == 0) return val;	// fast exit
 		StringBuilder ret = new StringBuilder();
 		char [] chars = new char[val.length()];
 		val.getChars(0, val.length(), chars, 0);
 		for (int i=0; i<chars.length; i++) {
 			if (!(chars[i]>0 && chars[i]<128)) {
 				ret.append("\\u");
 				ret.append(Integer.toHexString(chars[i]));
 			}
 			else {
 				ret.append(chars[i]);
 			}
 		}
 		return ret.toString();
 	}
 }
