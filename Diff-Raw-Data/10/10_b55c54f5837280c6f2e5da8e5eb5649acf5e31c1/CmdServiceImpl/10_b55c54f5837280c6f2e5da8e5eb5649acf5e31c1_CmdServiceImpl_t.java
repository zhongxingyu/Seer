 package com.eucalyptus.webui.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.eucalyptus.webui.client.service.CmdService;
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.shared.aws.ImageType;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class CmdServiceImpl extends RemoteServiceServlet implements CmdService {
 	static final String EC2_ACCESS_KEY="5VPWK0CGBEORB4ITOOMLL";
 	static final String EC2_SECRET_KEY="xHj6hTmtKgGzCEIOAtOc6iUCkuyFBXBQhWOdiSZU";
	static final String EC2_URL="http://192.168.0.50:8773/services/Eucalyptus";
 	static final String SSH_HOST="root@59.66.104.184";
 	//FIXME !! howto get certs? 
 	static final String EC2_CERT="/home/eucalyptus/admin/euca2-admin-bf8e80b9-cert.pem";
 	static final String EC2_PRIVATE_KEY="/home/eucalyptus/admin/euca2-admin-bf8e80b9-pk.pem";
 	static final String EC2_USER_ID="491317658036";
 	static final String EUCALYPTUS_CERT="/home/eucalyptus/admin/cloud-cert.pem";
	static final String AWS_CREDENTIAL_FILE="/home/eucalyptus/admin/iamrc";
 	static final String IMAGE_PATH = "/home/images/";
 	
 	public static final ArrayList<SearchResultFieldDesc> CTRL_COMMON_FIELD_DESCS = Lists.newArrayList();
 	static {
     CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "名称", true, "10%") );
 		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "分区", true, "10%") );
 		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "地址", true, "10%") );
 		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "状态", true, "10%") );
 	}
 
 	@Override
 	public String run(Session session, String[] cmd) {
 		String ret = "";
 		try {
 			ProcessBuilder b = new ProcessBuilder(cmd);
 			Map<String, String> env = b.environment();
 			env.put("EC2_URL", EC2_URL);
 			env.put("EC2_ACCESS_KEY", EC2_ACCESS_KEY);
 			env.put("EC2_SECRET_KEY", EC2_SECRET_KEY);
 			env.put("EC2_CERT", EC2_CERT);
 			env.put("EC2_PRIVATE_KEY", EC2_PRIVATE_KEY);
 			env.put("EC2_USER_ID", EC2_USER_ID);
 			env.put("EUCALYPTUS_CERT", EUCALYPTUS_CERT);
			env.put("AWS_CREDENTIAL_FILE", AWS_CREDENTIAL_FILE);
 			Process p = b.start();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String s = reader.readLine();
 			while (s != null) {
 				ret = ret.concat(s + "\n");
 				s = reader.readLine();
 			}
 			p.waitFor(); 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	@Override
 	public String sshRun(Session session, String[] cmd) {
 	    final String[] _cmd = {"ssh", SSH_HOST, //"-p", "22220",  //TODO
 	    		"EC2_CERT=" + EC2_CERT + " EC2_ACCESS_KEY=" + EC2_ACCESS_KEY + " EC2_SECRET_KEY=" +  EC2_SECRET_KEY + " " +  " EC2_PRIVATE_KEY=" + EC2_PRIVATE_KEY +  
	    		" EC2_USER_ID=" + EC2_USER_ID + " EUCALYPTUS_CERT=" + EUCALYPTUS_CERT + " AWS_CREDENTIAL_FILE=" +  AWS_CREDENTIAL_FILE + " " + 
 	    		StringUtils.join(cmd, " ")};
 	    System.out.println("sshRun: " + _cmd[2]);
 	    return run(session, _cmd);
 	}
 
 	@Override
 	public SearchResult lookupNodeCtrl(Session session, String search,
 			SearchRange range) {
 		final String[] cmd = {"euca-describe-nodes"};
 		String ret = sshRun(session, cmd);
 		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
 		for (String s : ret.split("\n")) {
       if (s != null) {
         String[] i = s.split("\\s+");
         if (i.length < 3)
           break;
         data.add(new SearchResultRow(Arrays.asList("", i[2], i[1], "")));
       }
 		}
 		SearchResult result = new SearchResult(data.size(), range);
 		result.setDescs(CTRL_COMMON_FIELD_DESCS);
 		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
 		return result;
 	}
 	
 	@Override
 	public SearchResult lookupStorageCtrl(Session session, String search,
 			SearchRange range) {
 		final String[] cmd = {"euca-describe-storage-controllers"};
 		String ret = sshRun(session, cmd);
 		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
 		for (String s : ret.split("\n")) {
 			if (s != null) {
 				String[] i = s.split("\\s+");
         if (i.length < 5)
           break;
 				data.add(new SearchResultRow(Arrays.asList(i[2], i[1], i[3], i[4])));
 			}
 		}
 		SearchResult result = new SearchResult(data.size(), range);
 		result.setDescs(CTRL_COMMON_FIELD_DESCS);
 		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
 		return result;
 	}
 	@Override
 	public SearchResult lookupClusterCtrl(Session session, String search,
 			SearchRange range) {
 		final String[] cmd = {"euca-describe-clusters"};
 		String ret = sshRun(session, cmd);
 		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
 		for (String s : ret.split("\n")) {
 			if (s != null) {
 				String[] i = s.split("\\s+");
         if (i.length < 5)
           break;
 				data.add(new SearchResultRow(Arrays.asList(i[2], i[1], i[3], i[4])));
 			}
 				
 		}
 		SearchResult result = new SearchResult(data.size(), range);
 		result.setDescs(CTRL_COMMON_FIELD_DESCS);
 		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
 		return result;
 	}
 	@Override
 	public SearchResult lookupWalrusCtrl(Session session, String search,
 			SearchRange range) {
 		final String[] cmd = {"euca-describe-walruses"};
 		String ret = sshRun(session, cmd);
 		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
 		for (String s : ret.split("\n")) {
 			if (s != null) {
 				String[] i = s.split("\\s+");
 				if (i.length < 5)
 				  break;
 				data.add(new SearchResultRow(Arrays.asList(i[2], i[1], i[3], i[4])));
 			}
 		}
 		SearchResult result = new SearchResult(data.size(), range);
 		result.setDescs(CTRL_COMMON_FIELD_DESCS);
 		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
 		return result;
 	}
 
   @Override
   public String uploadImage(Session session, String file, ImageType type, String bucket,
       String name, String kernel, String ramdisk) {
     //won't need if run on server    
     final String[] cmd0 = {"scp", file, SSH_HOST + ":" + IMAGE_PATH};
     String ret = run(session, cmd0);
     System.out.println("ret: " + ret);
     
     file = file.replace("/tmp/", IMAGE_PATH);
     //rename
     String _file = IMAGE_PATH + name;
     String s = "mv " + file + " " + _file;
     final String[] cmd = {s};
     ret = sshRun(session, cmd);
     System.out.println("ret: " + ret);
     
     //gunzip if needed
     if (name.endsWith(".gz")) {
       final String[] _cmd = {"gunzip", _file};
       ret = sshRun(session, _cmd);
       System.out.println("ret: " + ret);
       _file = _file.substring(0, _file.length() - 3);
     }
     
     //bundle
     String[] cmd1 = null;
     switch (type) {
     case KERNEL:
       String[] k = {"euca-bundle-image", "-i", _file, "--kernel", "true"};
       cmd1 = k;
       break;
     case RAMDISK:
       String[] ra = {"euca-bundle-image", "-i", _file, "--ramdisk", "true"};
       cmd1 = ra;
       break;
     case ROOTFS:
       String[] ro = {"euca-bundle-image", "-i", _file, "--kernel", kernel, "--ramdisk", ramdisk};
       cmd1 = ro;
       break;
     }
     ret = sshRun(session, cmd1);
     System.out.println("ret: " + ret);
     String manifest = "";
     {     
       String[] tmp  = ret.split("\n");
       for (String t : tmp) {
         if (t.contains("manifest")) {
           manifest = t.split("\\s")[2];
         }
       }
     }
     System.out.println("manifest: " + manifest);
     
     //upload
     final String[] cmd2 = {"euca-upload-bundle", "-b", bucket, "-m", manifest};
     ret = sshRun(session, cmd2);
     System.out.println("ret: " + ret);
     
     //register
     File m = new File(manifest);
     final String[] cmd3 = {"euca-register", bucket + "/" + m.getName()};
     ret = sshRun(session, cmd3);
     System.out.println("ret: " + ret);
     {
       String[] tmp  = ret.split("\n");
       for (String t : tmp) {
         if (t.contains("IMAGE")) {
           ret = t.split("\\s")[1];
         }
       }    
     }
     return ret; 
   }
 
   @Override
   public String runInstance(Session session, String image, String keypair,
       String vmtype, String group) {
     List<String> cmd = new ArrayList<String>();
     cmd.add("euca-run-instances");
     if (!keypair.equals("")) {
       cmd.add("-k");
       cmd.add(keypair);
     }
     if (!vmtype.equals("")) {
       cmd.add("-t");
       cmd.add(vmtype);
     }
     if (!group.equals("")) {
       cmd.add("-g");
       cmd.add(group);
     }
     cmd.add(image);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     System.out.println("ret: " + ret);
     {
       String[] tmp  = ret.split("\n");
       for (String t : tmp) {
         if (t.contains("INSTANCE")) {
           ret = t.split("\\s")[1];
         }
       }    
     }
     return ret;
   }
 
   @Override
   public void registerCluster(Session session, String part, String host,
       String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-register-cluster", "--partition", part, "--host", host, "--component", name);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void deregisterCluster(Session session, String part, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-deregister-cluster", "--partition", part, "--component", name);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
   }
 
 
   @Override
   public void registerStorage(Session session, String part, String host,
       String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-register-storage-controller", "--partition", part, "--host", host, "--component", name);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
   }
 
   @Override
   public void deregisterStorage(Session session, String part, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-deregister-storage-controller", "--partition", part, "--component", name);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void registerWalrus(Session session, String host,
       String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-register-walrus", "--partition", "walrus", "--host", host, "--component", name);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
   }
 
   @Override
   public void deregisterWalrus(Session session, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-deregister-walrus", "--partition", "walrus", "--component", name);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void registerNode(Session session, String host) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca_conf", "--register-nodes", host);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void deregisterNode(Session session, String host) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca_conf", "--deregister-nodes", host);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 }
