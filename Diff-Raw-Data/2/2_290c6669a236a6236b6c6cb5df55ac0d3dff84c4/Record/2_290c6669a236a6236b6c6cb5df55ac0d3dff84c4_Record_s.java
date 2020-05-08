 package cmput391;
 
 public class Record {
 	
 	private int recordID;
 	private int imgID;
 
 	public Record(int record) {
 		this.recordID = record;
		imgID = 0;
 	}
 
 	public void setRecordID(int record) {
 		this.recordID = record;
 	}
 	
 	public void setImgID(int img) {
 		this.imgID = img;
 	}
 
 	public int getRecordID() {
 		return recordID;
 	}
 	
 	public int getImgID() {
 		return imgID;
 	}
 }
