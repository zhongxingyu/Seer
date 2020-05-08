 /**
  * 
  */
 package gr.uoi.cs.daintiness.hecate.diff;
 
 /**
  * 
  * @author iskoulis
  *
  */
 public class Metrics {
 
 	private String oldVersion;
 	private String newVersion;
 	private int insertions, deletions, alterations;
 	private int tableIns, tableDel, tableAlt;
 	private int attrIns, attrDel, attrAlt;
 	private int attrTabIns, attrTabDel;
 	private int keyAlt;
 	private int numOfTables, numOfAttributes;
 	private int numOfNewTables, numOfNewAttributes;
 	private static int versions = 0;
 	/**
 	 * 
 	 */
 	public Metrics(String oldVersion, String newVersion) {
 		versions++;
 		this.oldVersion = oldVersion;
 		this.newVersion = newVersion;
 		reset();
 	}
 
 	public void reset() {
 		insertions = deletions = alterations = 0;
 		tableIns = tableDel = 0;
 		attrIns = attrDel = 0;
 		tableAlt = attrAlt = keyAlt = 0;
 		numOfTables = numOfAttributes = 0;
 	}
 
 	public void resetVersionNum() {
 		versions = 0;
 	}
 
 	protected void insertAttr () {
 		attrIns++; insertions++;
 	}
 	protected void insertTabAttr () {
 		attrTabIns++; insertions++;
 	}
 	protected void insetTable() {
 		tableIns++; insertions++;
 	}
 
 	protected void deleteAttr() {
 		attrDel++; deletions++;
 	}
 	protected void deleteTabAttr() {
 		attrTabDel++; deletions++;
 	}
 	protected void deleteTable() {
 		tableDel++; deletions++;
 	}
 
 	protected void alterAttr() {
 		attrAlt++;
 		alterations++;
 	}
 	protected void alterTable() {
 		tableAlt++;
		// XXX this is wrong, must check before calling this
 	}
 	protected void alterKey() {
 		keyAlt++;
 		alterations++;
 	}
 
 	protected void setOrigTables(int n) {
 		numOfTables = n;
 	}
 	protected void setOrigAttrs(int n) {
 		numOfAttributes = n;
 	}
 
 	protected void setNewTables(int n) {
 		numOfNewTables = n;
 	}
 	protected void setNewAttrs(int n) {
 		numOfNewAttributes = n;
 	}
 
 	public int[] getTotalMetrics() {
 		int i[] = {this.insertions, this.deletions, this.alterations};
 		return i;
 	}
 
 	public int[] getTableMetrics() {
 		int i[] = {this.tableIns, this.tableDel, this.tableAlt};
 		return i;
 	}
 
 	public int[] getAttributeMetrics() {
 		int i[] = {this.attrIns, this.attrDel, this.attrAlt ,this.keyAlt, this.attrTabIns, this.attrTabDel};
 		return i;
 	}
 
 	public int[] getOldSizes() {
 		int i[] = {this.numOfTables, this.numOfAttributes};
 		return i;
 	}
 
 	public int[] getNewSizes() {
 		int i[] = {this.numOfNewTables, this.numOfNewAttributes};
 		return i;
 	}
 
 	public String[] getVersionNames() {
 		String[] ret = {oldVersion, newVersion};
 		return ret;
 	}
 
 	public int getNumVersions() {
 		return versions;
 	}
 
 	public void sanityCheck() throws Exception {
 		if(insertions != tableIns + attrIns + attrTabIns) throw new Exception("BIV!!");
 		if(deletions != tableDel + attrDel + attrTabDel) throw new Exception("BIV!!");
 	}
 }
