 package admin.bll;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import admin.gui.ManageKeywordsPanel;
 import admin.gui.ManageKeywordsPanelHandler;
 import common.dal.IImageStore;
 import common.dal.IKeywordsStore;
 
 
 public class ManageKeywordsController implements ManageKeywordsPanelHandler {
     public ManageKeywordsPanel view;
 	public IKeywordsStore keywordsStore;
 	public IImageStore imageStore;
 	
 	private ArrayList<String> keywords;
 
 
     public ManageKeywordsController(IKeywordsStore keywordsStore, IImageStore imageStore) {
 		this.keywordsStore = keywordsStore;
 		this.imageStore = imageStore;
     	keywords = keywordsStore.getKeywords();
 		view = new ManageKeywordsPanel(this);
     }
 
     @Override
     public boolean addKeyword(String keyword) {
 		if (keywordsStore.addKeyword(keyword)) {
 			keywords.add(keyword);
 			return true;
 		}
 		return false;
     }
 
     @Override
     public boolean deleteKeyword(String keyword) {
		if (keywordsStore.deleteKeyword(keyword) && imageStore.deleteAllWithKeyword(keyword)) {
 			keywords.remove(keyword);
 			return true;
 		}
 		return false;
     }
 	
     public ManageKeywordsPanel getView() {
 		return view;
     }
 
 	@Override
 	public List<String> getKeywords() {
 		return keywords;
 	}
 }
