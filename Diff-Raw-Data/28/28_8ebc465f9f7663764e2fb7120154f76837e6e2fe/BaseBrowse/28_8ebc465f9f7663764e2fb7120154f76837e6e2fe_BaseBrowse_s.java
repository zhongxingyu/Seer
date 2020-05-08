 package sgit.action;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import sgit.dto.Breadcrumb;
 import sgit.dto.GitRepository;
 
 public abstract class BaseBrowse extends Base {	
 	private String path = "";
 	public void setPath(String path) {		
 		this.path = path==null?"":path;
 	}
 	public String getPath() {return path;}
 	private GitRepository repository;
 	public void setRepository(GitRepository repository) {this.repository = repository;}	
 	public GitRepository getRepository() {return repository;}
 	private String id;
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getId(){return id;}
 	private String branch;
 	public void setBranch(String branch) {
 		this.branch = branch;
 	}
 	public String getBranch() {
 		if(this.branch == null)
 			this.branch = repository.getDefaultBranch();
 		return this.branch;
 	}
 	public List<String> getBranchList() {
 		return repository.getBranchList(getBranch());
 	}
 	private static String join(List<String> chunks) {
 		StringBuilder b = new StringBuilder();
 		int len = chunks.size();
 		for(int i = 0; i < len; i++) {
 			if(i!=0)
 				b.append("/");
 			b.append(chunks.get(i));			
 		}
 		return b.toString();
 	}
 	public List<Breadcrumb> getBreadcrumbs() {
		if(path.isEmpty())
			return Collections.emptyList();
 		List<Breadcrumb> result = new ArrayList<Breadcrumb>();
		List<String> acc = new ArrayList<String>();
		String chunks[] = path.split("/");
		int len = chunks.length;
		for(int i = 0; i < len; i++) {
			acc.add(chunks[i]);
			String path = (i==len-1 && getId() == null)?null:join(acc); 
			result.add(new Breadcrumb(chunks[i], path));
		}		
 		if(getId() != null)
 			result.add(new Breadcrumb(getId(), null));
 		return result;
 	}
 }
