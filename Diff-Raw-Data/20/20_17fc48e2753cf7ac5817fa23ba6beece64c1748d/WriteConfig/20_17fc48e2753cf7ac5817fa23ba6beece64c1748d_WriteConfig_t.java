 package com.rom.server.configuration;
 
 import com.rom.common.logging.Logger;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 import java.io.FileOutputStream;
 
 public class WriteConfig {
 	private XStream xs = null;
 	private FileOutputStream fs = null;
 		
 	public boolean updateConfiguration(Config eintrag) {
 		boolean ret = false;
 		
 		try {
 			this.fs = new FileOutputStream(Config.file);
 			this.xs = new XStream(new StaxDriver());
 			this.xs.toXML(eintrag, fs);
 			this.fs.close();
 		} catch (Exception ex) {
			Logger.logException("Exception in updateConfiguration", ex);
 		}
 		
 		return ret;
 	}
 }
 
 
