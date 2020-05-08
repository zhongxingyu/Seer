 package test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.net.ConfigurationException;
 import org.dcm4che2.tool.dcmqr.DcmQR;
 
 import daemon.tools.nifti.Nifti_Reader;
 import ij.ImagePlus;
 
 public class Test {
 	public static void main(String[] args){
 		try {
			DcmQR dcmQR = new DcmQR("GDC_RECHPASS2");
 
 			dcmQR.setCalledAET("GEPACS28", true);
 			dcmQR.setRemoteHost("120.50.150.28");
 			dcmQR.setRemotePort(4100);
 
 			dcmQR.setPackPDV(true);
 			dcmQR.setTcpNoDelay(false);
 			dcmQR.setCGet(false);
 			dcmQR.setQueryLevel(DcmQR.QueryRetrieveLevel.PATIENT);
 
 			dcmQR.addMatchingKey(new int[]{Tag.PatientName},"MARTIN" );
 
 			// dcmQR.addMatchingKey(new int[]{Tag.AccessionNumber}, "999");
 			// dcmQR.addMatchingKey(new int[]{Tag.PatientName},"TEST^STUDY");
 
 			dcmQR.addReturnKey(new int[]{Tag.PatientName});
 
 			dcmQR.configureTransferCapability(false);
 
 			dcmQR.start();
 			dcmQR.open();
 
 			List<DicomObject> result = dcmQR.query();
 
 			System.out.println(result.toString());
 
 			System.out.println("Query Matches: "+ result.size());
 			dcmQR.close();
 
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
 
