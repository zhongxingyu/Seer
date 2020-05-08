 package gr.uoi.cs.daintiness.hecate.io;
 
 import gr.uoi.cs.daintiness.hecate.diff.DiffResult;
 import gr.uoi.cs.daintiness.hecate.transitions.Deletion;
 import gr.uoi.cs.daintiness.hecate.transitions.Insersion;
 import gr.uoi.cs.daintiness.hecate.transitions.TransitionList;
 import gr.uoi.cs.daintiness.hecate.transitions.Transitions;
 import gr.uoi.cs.daintiness.hecate.transitions.Update;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 
 public class Export {
 	
 	static private int id;
 	
 	public static void xml(Transitions trs, String path) {
 		try {
 			JAXBContext jaxbContext = JAXBContext.newInstance(Update.class, Deletion.class, Insersion.class, TransitionList.class, Transitions.class);
 			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			String filePath = getDir(path) + File.separator + "transitions.xml";
 			jaxbMarshaller.marshal(trs, new FileOutputStream(filePath));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void metrics(DiffResult res, String path) throws IOException {
 		String filePath = getDir(path) + File.separator + "metrics.csv";
 		BufferedWriter metrics = new BufferedWriter(new FileWriter(filePath, true));
 		String name = res.met.getVersionNames()[1];
 		String time = name.substring(0, name.length()-4);
 		metrics.write(
 				(id++) + "\t" +
 				time + "\t" +
 				res.met.getVersionNames()[0] + "\t" +
 				res.met.getVersionNames()[1] + "\t" +
 				res.met.getOldSizes()[0] + "\t" +
 				res.met.getNewSizes()[0] + "\t" +
 				res.met.getOldSizes()[1] + "\t" +
 				res.met.getNewSizes()[1] + "\t" +
 				res.met.getTableMetrics()[0] + "\t" +
 				res.met.getTableMetrics()[1] + "\t" +
 				res.met.getAttributeMetrics()[0] + "\t" +
 				res.met.getAttributeMetrics()[1] + "\t" +
 				res.met.getAttributeMetrics()[2] + "\t" +
 				res.met.getAttributeMetrics()[3] + "\t" +
 				res.met.getTotalMetrics()[2] + "\t" +
 				res.met.getAttributeMetrics()[4] + "\t" +
				res.met.getAttributeMetrics()[5] + "\n"
 			);
 		metrics.close();
 	}
 	
 	public static void initMetrics(String path) throws IOException {
		id = 1;
 		String filePath = getDir(path) + File.separator + "metrics.csv";
 		BufferedWriter metrics = new BufferedWriter(new FileWriter(filePath));
 		metrics.write("# trID\ttime\toldVer\tnewVer\t#oldT\t#newT\t#oldA"
 				+ "\tnewA\ttIns\ttDel\taIns\taDel"
 				+ "\taTypeAlt\tkeyAlt\ttotAlt\taTabIns\taTabDel\n");
 		metrics.close();
 	}
 	
 	private static String getDir(String path) {
 		String parrent = (new File(path)).getParent();
 		File dir = new File(parrent + File.separator + "results");
 		if (!dir.exists()) {
 			dir.mkdir();
 		}
 		return dir.getPath();
 	}


 }
