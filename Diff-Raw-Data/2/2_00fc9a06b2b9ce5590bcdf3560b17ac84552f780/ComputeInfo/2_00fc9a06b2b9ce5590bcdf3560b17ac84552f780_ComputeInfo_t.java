 package org.megam.chef.parser;
 
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.megam.chef.Constants;
 import org.megam.chef.cloudformatters.AmazonCloudFormatter;
 import org.megam.chef.cloudformatters.GoogleCloudFormatter;
 import org.megam.chef.cloudformatters.HPCloudFormatter;
 import org.megam.chef.cloudformatters.ProfitBricksCloudFormatter;
 import org.megam.chef.cloudformatters.OutputCloudFormatter;
 import org.megam.chef.core.Condition;
 import org.megam.chef.core.ScriptFeeder;
 import org.megam.chef.shell.FedInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author rajthilak
  * 
  */
 public class ComputeInfo implements DataMap, ScriptFeeder, Condition {
 
 	public static final String GROUPS = "groups";
 	public static final String IMAGE = "image";
 	public static final String FLAVOR = "flavor";
 	public static final String CPUS = "cpus";
 	public static final String RAM = "ram";
	public static final String HDD = "hdd-size";
 	public static final String TENANTID = "tenant_id";
 	public static final String SSHKEY = "ssh_key";
 	public static final String IDENTITYFILE = "identity_file";
 	public static final String SSHUSER = "ssh_user";
 	public static final String VAULTLOCATION = "vault_location";
 	public static final String SSHPUBLOCATION = "sshpub_location";
 	public static final String CREDENTIALFILE = "credential_file";
 	public static final String ZONE = "zone";
 	public static final String REGION = "region";
 
 	/**
 	 * create Map name as cc (cross cloud) from config.json file
 	 */
 	private String cctype;
 	private Map<String, String> cc = new HashMap<String, String>();
 	private Map<String, String> access = new HashMap<String, String>();
 
 	private OutputCloudFormatter ocf = null;
 
 	public ComputeInfo() {
 		// tricky, gson populated your private vars (map) yet ?
 	}
 
 	private void createOCF() {
 		switch (getCCType()) {
 		case "ec2":
 			ocf = new AmazonCloudFormatter(map());
 			break;
 		case "google":
 			ocf = new GoogleCloudFormatter(map());
 			break;
 		case "hp":
 			ocf = new HPCloudFormatter(map());
 			break;
 		case "profitbricks":
 			ocf = new ProfitBricksCloudFormatter(map());
 			break;
 		default:
 			throw new IllegalArgumentException(
 					getCCType()
 							+ ": configuration not supported yet. We are working on it.\n"
 							+ Constants.HELP_GITHUB);
 		}
 	}
 
 	private String getCCType() {
 		return cctype;
 	}
 
 	public String getVaultLocation() {
 		return map().get(VAULTLOCATION);
 	}
 
 	public String getSshPubLocation() {
 		return map().get(SSHPUBLOCATION);
 	}
 
 	/**
 	 * @return ec2 map
 	 */
 	public Map<String, String> map() {
 		if (!cc.keySet().containsAll(access.keySet())) {
 			cc.putAll(access);
 		}
 		return cc;
 	}
 
 	public boolean canFeed() {
 		return true;
 	}
 
 	public FedInfo feed() {
 		Map<String, String> ocfout = ocf.format();
 		if (ocfout != null) {
 			StringBuilder sb = new StringBuilder();
 			for (Map.Entry<String, String> entry : ocfout.entrySet()) {
 				if (entry.getValue().length() > 0) {
 					sb.append(" ");
 					sb.append(entry.getKey());
 					sb.append(" ");
 					sb.append(entry.getValue());
 					sb.append(" ");
 				}
 			}
 			return (new FedInfo(name(), sb.toString()));
 		} else {
 			throw new IllegalArgumentException(getCCType()
 					+ ": Can't proceed with arguments missing \n"
 					+ ocf.toString() + "\n" + Constants.HELP_GITHUB);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.megam.chef.core.Condition#name()
 	 */
 	public String name() {
 		return "ComputeInfo";
 	}
 
 	@Override
 	public boolean ok() {
 		return ocf.ok();
 	}
 
 	@Override
 	public boolean inputAvailable() {
 		createOCF();
 		return ocf.inputAvailable();
 	}
 
 	public List<String> getReason() {
 		return ocf.getReason();
 	}
 
 	public String toString() {
 		StringBuilder strbd = new StringBuilder();
 		final Formatter formatter = new Formatter(strbd);
 		for (Map.Entry<String, String> entry : map().entrySet()) {
 			formatter.format("%10s = %s%n", entry.getKey(), entry.getValue());
 		}
 		formatter.close();
 		return strbd.toString();
 	}
 
 }
