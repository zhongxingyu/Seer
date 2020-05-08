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
		if(currentIdx() >= pages.size()) {
 			pages.add(new MatrixImpl(5,7));
			setCurrentPage(currentIdx() + 1);
 		}
		return pages.get(currentIdx());
 	}
 
 	public Matrix prevPage() {
		if (currentIdx() > 1) {
			setCurrentPage(currentIdx() - 1);
 		}
			return pages.get(currentIdx());
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
	
	public int currentIdx() {
		return currentPage - 1;
	}
 }
