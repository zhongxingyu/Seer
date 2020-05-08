 package de.echox.hacklace.pix0lat0r.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.echox.hacklace.pix0lat0r.data.Matrix;
 import de.echox.hacklace.pix0lat0r.data.MatrixImpl;
 
 public class App {
 	
 	List<Matrix> pages = new ArrayList<Matrix>();
 	
 	private int currentPage;
 
 	public void quit() {
 		System.exit(0);
 	}
 
 	App() {
 		this.setCurrentPage(1);
 		pages.add(new MatrixImpl(5,7));
 	}
 	
 	public Matrix nextPage() {
		if(getCurrentPage() >= pages.size()) {
 			pages.add(new MatrixImpl(5,7));
			setCurrentPage(getCurrentPage() + 1);
 		}
		return pages.get(getCurrentPage());
 	}
 
 	public Matrix prevPage() {
		if (getCurrentPage() > 1) {
			setCurrentPage(getCurrentPage() - 1);
 		}
			return pages.get(getCurrentPage());
 	}
 
 	public int getCurrentPage() {
 		return currentPage;
 	}
 
 	public void setCurrentPage(int currentPage) {
 		this.currentPage = currentPage;
 	}
 
 	public int getPageSize() {
 		return this.pages.size();
 	}
 }
