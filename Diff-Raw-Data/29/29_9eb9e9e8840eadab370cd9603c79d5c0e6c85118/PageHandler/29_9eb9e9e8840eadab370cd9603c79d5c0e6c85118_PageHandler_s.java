 package sw.app_250grice;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PageHandler {
 	
 	private static PageHandler singletonPageHandler;
 	private List<Page> pages;
 		
 	private PageHandler(){
 		pages = new ArrayList<Page>();		
 	}
 	
 	public static PageHandler getPageHandler() {
 		if(singletonPageHandler == null)
 			singletonPageHandler = new PageHandler();
 		return singletonPageHandler;
 	}
 	
 	public void addPageBlank(String name) throws PageNameAlreadyExistsException{
 		if(containsPageByName(name))
 			throw new PageNameAlreadyExistsException();
 		else
 			pages.add(new Page(name));			
 	}
 	
 	public void addPageExisting(Page page) throws PageNameAlreadyExistsException{
 		if(containsPageByName(page.name))
 			throw new PageNameAlreadyExistsException();
 		else
 			pages.add(page);			
 	}
 	
 	public boolean containsPageByName(String name) {
 		try {
 		getPageByName(name);
 		}
 		catch (PageNotFoundException ex) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public List<Page> getPages() {
 		List<Page> ret = new ArrayList<Page>();
 		
 		for (Page page : pages) {
 			ret.add(page.clone());
 		}
 		
 		return ret;
 	}
 	
 	public void deletePageByName(String name) throws PageNotFoundException{
 	//	try {
 			Page toSearch = getPageByName(name);
 			this.pages.remove(toSearch);
 		/*}
 		catch (PageNotFoundException ex) {
 			throw(ex);
 		}*/
 	}
 	
 	public void dispose() {
 		pages.clear();
 		pages = null;
 		singletonPageHandler = null;
 	}
 	
 	
 	private Page getPageByName(String name) throws PageNotFoundException{
 		for(Page page : pages)
 			if(page.getName() == name)
 				return page;
 			
 		throw(new PageNotFoundException());
 	}
 	
 	public void renamePageByName(String oldName, String newName) throws PageNotFoundException, PageNameAlreadyExistsException {
 		Page p = this.getPageByName(oldName);
 		/*
 		try {
 			p = this.getPageByName(oldName);
 		}
 		catch (PageNotFoundException ex) {
 			throw(ex);
 		}*/
 
 		if (this.containsPageByName(newName))
 			throw(new PageNameAlreadyExistsException());
 		
 		p.setName(newName);

 	}
 	
 }
