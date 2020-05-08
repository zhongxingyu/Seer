 package ch.cern.atlas.apvs.ptu.server;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public interface PtuConstants {
 	public static final DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
 	public static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 }
