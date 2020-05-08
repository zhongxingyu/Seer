 package com.sharkhunter.dbgpack;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import com.sun.jna.Platform;
 
 import net.pms.PMS;
 import net.pms.external.ExternalListener;
 
 public class DbgPack_plugin implements ExternalListener,ActionListener, ItemListener {
 	
 	private boolean pms;
 	private boolean web;
 	
 	private JCheckBox pmsbox;
 	private JCheckBox webbox;
 	
 	public DbgPack_plugin() {
 		pms=true;
 		web=true;
 	}
 
 	public void shutdown() {
 	}
 
 	public String name() {
 		return "Pack Debug Info";
 	}
 	//@Override
 	public JComponent config() {
 		JPanel top=new JPanel(new GridBagLayout());
 		JButton debugPack=new JButton("Pack debug info");
 		pmsbox=new JCheckBox("Include PMS.conf",pms);
 		webbox=new JCheckBox("Include WEB.conf",web);
 		debugPack.setActionCommand("action");
 		debugPack.addActionListener(this);
 		pmsbox.addItemListener(this);
 		webbox.addItemListener(this);
 		GridBagConstraints c = new GridBagConstraints();
 		// 1st the channels path
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx=1.0;
 		top.add(pmsbox,c);
 		c.gridy++;
 		c.weightx=1.0;
 		top.add(webbox,c);
 		c.gridy++;
 		c.weightx=2.0;
 		top.add(debugPack,c);
 		return top;
 	}
 	private void writeToZip(ZipOutputStream out,File f) throws Exception {
 		byte[] buf = new byte[1024];
 		int len;
 		if(!f.exists()) {
			PMS.debug("DbgPack file "+f.getAbsolutePath()+" does not exists, Ignore.");
 			return;
 		}
 		FileInputStream in = new FileInputStream(f);
 		out.putNextEntry(new ZipEntry(f.getName()));
 		while ((len = in.read(buf)) > 0) 
 			out.write(buf, 0, len);
 		out.closeEntry();
 		in.close();
 	}
 	private static final String PMSDIR = "\\PMS\\";
 	private String conf(String file) {
 		String strAppData = System.getenv("APPDATA");
 		if (Platform.isWindows() && strAppData != null) {
                 return strAppData + PMSDIR + file+ ".conf";
         } 
         else {
         	return file+".conf";
         }
 	}
 	private void packDbg(String[] files) {
 		String fName="pms_dbg.zip";
 		try {
 			ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(fName));
 			// PMS.conf
 			if(pms) {
 				File f=new File(PMS.getConfiguration().getPmsConfPath());
 				//File f=new File(conf("PMS"));
 				writeToZip(zos,f);
 			}
 			if(web) {
 				File f=new File(conf("WEB"));
 				writeToZip(zos,f);
 			}
 			// Now the rest
 			if(files!=null) {
 				for(int i=0;i<files.length;i++) {
 					File f=new File(files[i]);
 					writeToZip(zos,f);
 				}
 			}
 			// Last PMS log
 			File pms_file=new File("debug.log");
 			writeToZip(zos,pms_file);
 			zos.close();
 
 		} catch (Exception e) {
 			PMS.debug("error packing dbg info "+e);
 		}
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		Object source = e.getItemSelectable();
 		boolean val=true;
 		if(e.getStateChange() == ItemEvent.DESELECTED)
 			val=false;
 		if(source==pmsbox)
 			pms=val;
 		if(source==webbox)
 			web=val;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String f=(String)PMS.getConfiguration().getCustomProperty("dbgpack");
 		if(f==null)
 			packDbg(null);
 		else {
 			String files[]=f.split(",");
 			packDbg(files);
 		}
 	}
 }
