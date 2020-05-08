 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 /**
  * TODO Put here a description of what this class does.
  * 
  * @author schepedw. Created Apr 10, 2013.
  */
 public class AFSCommand extends Command {
 	private String outputFileName;
 	private String VCFName;
 	private DatabaseConnector conn;
 	private TreeMap<Integer, Integer> spectra;
 	private String filterName;
 
 	public AFSCommand(String VCFName, String outputFilename, String filterName)
 			throws ClassNotFoundException, SQLException {
 		this.VCFName = VCFName;
 		this.outputFileName = outputFilename;
 		this.filterName = filterName;
 		this.conn = new DatabaseConnector();
 		this.spectra = new TreeMap<Integer, Integer>();
 	}
 	
 
 	@Override
 	public String execute() {
 
 		try {
 			ArrayList<String> vcfIDs = getVcfIDs();
 
 			for (int i = 0; i < vcfIDs.size(); i++) {
 				ArrayList<String> entryIDs = getEntryIDs(vcfIDs.get(i));
 				for (int j = 0; j < entryIDs.size(); j++) {
 					String filterEntryPassFlag = filterEntryPasses(entryIDs
 							.get(j));
 
 					if (filterEntryPassFlag.equals("0")) {
 						continue;
 					}
 
 					int weirdThingsInEntry = 0;
 					ArrayList<String> individualIDs = getIndividualIDs(entryIDs
 							.get(j));
 					for (int k = 0; k < individualIDs.size(); k++) {
 						ResultSet individuals = getIndividuals(
 								individualIDs.get(k), filterEntryPassFlag);
 						weirdThingsInEntry = countIndividuals(
 								weirdThingsInEntry, individuals);
 					}
 					updateSpectra(weirdThingsInEntry);
 				}
 			}
 		} catch (Exception e) {

 			e.printStackTrace();
 		}
 		String toRet=printSpectra();
 		return toRet;
 
 	}
 
 	private int countIndividuals(int weirdThingsInEntry, ResultSet individuals)
 			throws SQLException {
 		while (individuals.next()) {
 			weirdThingsInEntry += countWeirdThings(
 					individuals.getString("Allele1"),
 					individuals.getString("Allele2"),
 					individuals.getString("Allele3"));
 
 		}
 		return weirdThingsInEntry;
 	}
 
 	private String filterEntryPasses(String entryID)
 			throws ClassNotFoundException, SQLException {
 		if (this.filterName.equals("")) {
 			return "1";
 		}
 		String sql = "Select `FilterEntryPass`.`Pass` from `FilterEntryPass`,`Filter`  where `FilterEntryPass`.`EntryId`="
 				+ entryID
 				+ " and `Filter`.`FilName`= '"
 				+ this.filterName
 				+ "' and `Filter`.`FilId` = `FilterEntryPass`.`FilId`";
 		ResultSet pass = this.conn.executeQuery(sql);
 		if (!pass.first()) {
 			System.out.println("Entry " + entryID + " not in filter. Aborting");
			return "error";
 		}
 
 		return pass.getString(1);
 
 	}
 
 	private void updateSpectra(int weirdThingsInEntry) {
 		if (!this.spectra.containsKey(weirdThingsInEntry)) {
 
 			this.spectra.put(weirdThingsInEntry, 1);
 		} else {
 			int update = this.spectra.get(weirdThingsInEntry) + 1;
 
 			this.spectra.put(weirdThingsInEntry, update);
 		}
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
 
 	private ResultSet getIndividuals(String indId, String checkIndividualFlag)
 			throws ClassNotFoundException, SQLException {
 		String sql;
 		if (checkIndividualFlag.equals("2")) {
 			sql = "Select * from `GT`, `FilterIndividualPass` where `GT`.`IndID`= '"
 					+ indId
 					+ "' and `FilterIndividualPass`.`IndID`='"
 					+ indId
 					+ "' and `FilterIndividualPass`.`IndID`=1";
 		} else {
 			sql = "Select * from `GT` where `IndID`= '" + indId + "'";
 		}
 
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
 
 	private int countWeirdThings(String allele1, String allele2, String allele3) {
 		int count = 0;
 
 		if (allele1 == null) {
 			return count;
 		} else if (allele1.equals("1")) {
 
 			count++;
 		}
 		if (allele2 == null) {
 			return count;
 		} else if (allele2.equals("1")) {
 
 			count++;
 		}
 		if (allele3 == null) {
 			return count;
 		} else if (allele3.equals("1")) {
 			count++;
 		}
 		return count;
 	}
 
 	private String printSpectra() {
 		String output = getOutputContent();
 		if (this.outputFileName.equals("")) {
 			return output;
 		} else {
 			FileWriter fstream;
 			try {
 				fstream = new FileWriter(this.outputFileName);
 				BufferedWriter out = new BufferedWriter(fstream);
 				out.write(output);
 				out.close();
 			} catch (IOException exception) {
 				exception.printStackTrace();
 			}
 			return "Output located in "+this.outputFileName;
 		}
 	}
 
 	private String getOutputContent() {
 		String output = "";
 		int endOfSpectra = this.spectra.lastKey();
 		for (int i = 0; i <= endOfSpectra; i++) {
 			if (!this.spectra.containsKey(i)) {
 				output += String.format("%d\t", 0);
 			} else
 				output += String.format("%d\t", this.spectra.get(i));
 		}
 		output += String.format("\n");
 		for (int i = 0; i <= endOfSpectra; i++) {
 			output += String.format("%d\t", i);
 		}
 		output += String.format("\n");
 		return output;
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 	}
 
 }
