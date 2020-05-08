 package si.fri.iskalnikOseb.dataHelpers;
 
 import java.util.ArrayList;
 
 public class FinanceStructure {
 	private static ArrayList<FinanceStructure> vsePovezave= new ArrayList<FinanceStructure>();
 	
 	private String imePovezave;
	private ArrayList<String> clanki;
 	
 	public FinanceStructure(String imePovezave, String clanek){
 		this.imePovezave = imePovezave;
		clanki = new ArrayList<String>();
 		clanki.add(clanek);
 		vsePovezave.add(this);
 	}
 	public void addClanek(String clanek) {
 		clanki.add(clanek);
 	}
 	
 	public ArrayList<String> getClanki(){
 		return clanki;
 	}
 	public String getImePovezave(){
 		return imePovezave;
 	}
 	public int getStClankov() {
 		return clanki.size();
 	}
 	
 	public static FinanceStructure getFinanceStructure(String imePovezave){
 		if (imePovezave == null)
 			return null;
 		for (FinanceStructure fs : vsePovezave) {
 			if (imePovezave.equals(fs.getImePovezave()))
 				return fs;
 		}
 		return 
 			null;
 	}
	
	public static void clearVsePovezave() {
		vsePovezave = new ArrayList<FinanceStructure>();
	}
 
 }
