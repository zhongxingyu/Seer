 package ch.cern.atlas.apvs.ptu.server;
 
 import java.util.Date;
 
 import ch.cern.atlas.apvs.domain.Measurement;
 
 @SuppressWarnings("serial")
 public class DoseAccum extends Measurement {
 
 	public DoseAccum(String ptuId, double value, Date d) {
 		super(ptuId, "DoseAccum", value, "mSv", d);
 	}
 }
