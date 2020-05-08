 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * TODO Put here a description of what this class does.
  * 
  * @author schepedw. Created Apr 10, 2013.
  */
 public class AFSCommand extends Command {
 	private String options;
 	private String VCFName;
 	private DatabaseConnector conn;
 	private int[] spectra = { 0, 0, 0 };
 
 	public AFSCommand(String VCFName, String options)
 			throws ClassNotFoundException, SQLException {
 		this.VCFName = VCFName;
 		this.options = options;
 		this.conn = new DatabaseConnector();
 	}
 
 	@Override
 	public String execute() {
 		try {
 			ArrayList<String> vcfIDs = getVcfIDs();
 			for (int i = 0; i < vcfIDs.size(); i++) {
 				ArrayList<String> entryIDs = getEntryIDs(vcfIDs.get(i));
 				
 				for (int j = 0; j < entryIDs.size(); j++) {
 					ArrayList<String> individualIDs = getIndividualIDs(entryIDs
 							.get(j));
 					
 					for (int k = 0; k < individualIDs.size(); k++) {
 						ResultSet individuals = getIndividuals(individualIDs
 								.get(k));
 						while (individuals.next()) {
 
 							updateSpectra(individuals.getString("Allele1"),
 									individuals.getString("Allele2"),
 									individuals.getString("Allele3"));
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.printf("%d %d %d\n", this.spectra[0], this.spectra[1],
 				this.spectra[2]);
 		return this.spectra.toString();
 
 	}
 
 	private ArrayList<String> getVcfIDs() throws SQLException,
 			ClassNotFoundException {
 		String sql = "Select `VcfId` from `Vcf` where `VcfName`='"
 				+ this.VCFName + "'";
 		ResultSet vcf = this.conn.executeQuery(sql);
 		return convertToArrayList(vcf);
 
 	}
 
 	private ArrayList<String> getEntryIDs(String VcfId) throws SQLException,
 			ClassNotFoundException {
 		String sql = "Select `EntryId` from `VcfEntry` where `VcfId`= '"
 				+ VcfId + "'";
 		ResultSet entries = this.conn.executeQuery(sql);
 		return convertToArrayList(entries);
 	}
 
 	private ArrayList<String> getIndividualIDs(String entryId)
 			throws SQLException, ClassNotFoundException {
 		String sql = "Select `IndID` from `IndividualEntry` where `EntryId`= '"
 				+ entryId + "'";
 		ResultSet individualIds = this.conn.executeQuery(sql);
 		return convertToArrayList(individualIds);
 	}
 
 	private ResultSet getIndividuals(String indId)
 			throws ClassNotFoundException, SQLException {
 		String sql = "Select * from `GT` where `IndID`= '" + indId + "'";
 		return this.conn.executeQuery(sql);
 	}
 
 	private ArrayList<String> convertToArrayList(ResultSet r)
 			throws SQLException {
 		ArrayList<String> result = new ArrayList<String>();
 		while (r.next()) {
 			result.add(r.getString(1));
 		}
 		r.close();
 		return result;
 	}
 
 	private void updateSpectra(String allele1, String allele2, String allele3) {
 
 		if (allele1 == null) {
 			return;
 		} else if (allele1.charAt(0) == '1') {
 			this.spectra[0]++;
 		}
 		if (allele2 == null) {
 			return;
 		}
 		else if (allele2.charAt(0) == '1') {
 			this.spectra[1]++;
 		}
 		if (allele3 == null) {
 			return;
 		}
 		else if (allele3.charAt(0) == '1') {
 			this.spectra[2]++;
 		}
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 	}
 }
