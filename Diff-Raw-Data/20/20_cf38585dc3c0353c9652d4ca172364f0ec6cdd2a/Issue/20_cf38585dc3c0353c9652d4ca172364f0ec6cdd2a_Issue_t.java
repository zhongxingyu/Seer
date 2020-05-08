 package mlproject.models;
 
 
 
 public class Issue{
 	
	HashMap<String, String> issueData;
	String mImagePath;
	
	public Issue(String csvFields, String csv, String imagePath) throws ArgumentException{
		this.mImagePath = imagePath;
		String[] titles = csvFields.split(",\\s*")
		String[] data = csv.split(",\\s*")
		if(titles.size != data.size) throw new ArgumentException("# titles doesn't match # of fields");
 		
		for(int i = 0; i < titles.size; i++){
			String title = titles[i];
			String d = data[i];
			issueData.put(title, d);
		}
	}
	
	public String get(String t){
		return issueData.get(t);
 	}
 	
 	
 	
 	
 	
 	
 }
