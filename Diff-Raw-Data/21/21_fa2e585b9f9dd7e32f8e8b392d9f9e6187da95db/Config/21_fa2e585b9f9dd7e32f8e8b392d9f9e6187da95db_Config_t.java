 package ngmep.config;
 
 /*
 	ngmep2osm - importador de datos de ngmep a openstreetmap
 	
 	Copyright (C) 2011-2012 Alberto Fernández <infjaf@gmail.com>
 	
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 	
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 
 import ngmep.osm.log.Log;
 
 public class Config extends Properties{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5287776933222886816L;
 	private static Config instance =  new Config();
 	private String fecha="";
 	
 	public static Config getInstance(){
 		return instance;
 	}	
 	private Config(){
 		this (new File(System.getProperty("user.home") + File.separator + ".osm"+ File.separator + "ngmep2osm.cfg"));
 	}
 	private Config(File configFile)  {
 		if (configFile.exists() && configFile.canRead()){
 			loadConfigFile(configFile);
 		}
 		else {
 			loadDefaults();
 		}
 		init();
 	}
 	private void loadConfigFile(File configFile){		
 		try (FileInputStream fis = new FileInputStream(configFile)){
 			this.load(fis);
 		}
 		catch (IOException ioe) {
 			loadDefaults();
 		}
 		init();
 	}
 	private void loadDefaults(){
 		setProperty("output.dir", System.getProperty("user.home")+ File.separator + "osm" + File.separator + "ine"+ File.separator);
 		setProperty("database.config.file", getProperty("output.dir") + "osmc.auth");
 		
 	}
 	private void init(){
        this.fecha =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());        
         File osm = new File(getOutputDir());
         if (!osm.exists()){
         	if (!osm.mkdirs()){
         		Log.log("Error creando el directorio:" + osm.getAbsolutePath());
         	}
         }
         File log = new File (osm, "log");
         if (!log.exists()){
         	if (!log.mkdir()){
         		Log.log("Error creando el directorio:" + log.getAbsolutePath());
         	}
         }
 	}    
 
 
     public Properties getDatabaseCredentials () throws IOException{
     	if (this.containsKey("database")){
     		return this;
     	}
         Properties config = new Properties();
         try (FileInputStream fis = new FileInputStream(getProperty("database.config.file"));) {
         	config.load(fis);
         }       
         return config;
     }
     public boolean isLogConsola() {
     	return Boolean.parseBoolean(getProperty("logConsola"));
     }    
     
     public String getOutputDir() {
     	return getProperty("output.dir");
     }    
     
     public String getOsmOutputFile (String baseName) {
    	return getOutputDir() +  File.separator + baseName + "."+ this.fecha + ".osm.gz";
     }
 	public String getLogFile() {
		return getOutputDir() + File.separator + "log"+File.separator + "log_"+ this.fecha+ ".log";
 	}
 	@Override
 	public synchronized boolean equals(Object o) {	
 		return super.equals(o);
 	}
 	@Override
 	public synchronized int hashCode() {
 		return super.hashCode();
 	}
 
 }
