 package ch.epfl.bbcf.psd;
 
 import java.sql.SQLException;
 
 
 public class Node {
 
 	protected static final int TAB_WIDTH = Main.TAB_WIDTH;
 	protected float[] tab;
 	protected int previousPosition;
 	protected int imageNumber;
 	protected int zoom;
 	protected Tree tree;
 	protected boolean first;
 	protected int previousStart;
 
 
 
 	private Node one;
 	private Node two;
 	private Node five;
 
 	private boolean last;
 	private int prevIndex;
 
 
 
 	protected Node(Tree tree, int zoom, Node parent_one, Node parent_two, Node parent_five, boolean last){
 		this.first = true;
 		this.zoom = zoom;
 		tab = new float[TAB_WIDTH];
 		for(int i=0;i<tab.length;i++){
 			tab[i]=0;
 		}
 		this.previousPosition = -1;
 		this.one = parent_one;
 		this.two = parent_two;
 		this.five = parent_five;
 		this.last = last;
 		this.tree = tree;
 	}
 
 	/**
 	 * Re-init the Node
 	 */
 	protected void reset(){
 		for(int i=0;i<tab.length;i++){
 			tab[i]=0;
 		}
 	}
 
 	/**
 	 * fill the scores in the output database.
 	 * @param position
 	 * @param score
 	 * @param currentImageNumber
 	 * @throws SQLException 
 	 */
 	protected void fill(int position, float score,int currentImageNumber) throws SQLException{
 		if(first){
 			this.imageNumber = currentImageNumber;
 			this.first = false;
 			this.previousPosition = position;
 			this.prevIndex = 0;
 		}
 
 		int index = getTabIndex(position);
 		
 		if(currentImageNumber == this.imageNumber){//fill the tab
 			for (int i=this.prevIndex + 1; i < index;i++){
				tab[i] = score;
 			}
 			tab[index] = score;
 			this.prevIndex = index;
 		} else {//draw the tab then reset it then fill it
			if (currentImageNumber == 26742 && this.zoom == 2){
				System.out.println("WRITEEEE");
			}
 			tree.writeValues(this);
 			if(!last){
 				this.updateParents();
 			}
 			this.reset();
 			tab[index] = score;
 			this.previousStart = index;
 			this.previousPosition = position;
 			imageNumber = currentImageNumber;
 		}
 
 	}
 
 
 	/**
 	 * Propagate score to the parents
 	 * @throws SQLException 
 	 */
 	protected void updateParents() throws SQLException{
 		int position = previousPosition;
 		int ind = getTabIndex(position);
 		int startPosition = getStartPosition(position);
 		if(one != null){
 			float max1 = tab[ind];
 			for(int i=ind;i<tab.length;i++){
 				ind++;
 				startPosition += zoom;
 				max1 = Math.max(max1, tab[i]);
 				if(i%2==1){//update parent 1
 					one.fill(startPosition, max1, (int)Math.ceil((double)imageNumber / 2));
 					if(i<tab.length-1){
 						max1=tab[i+1];
 					}
 				}
 			}
 
 
 
 
 		} else if(this.two != null){
 			float max2 = tab[ind];
 			float max5 = tab[ind];
 			for(int i=ind;i<tab.length;i++){
 				ind++;
 				startPosition += zoom;
 				max2 = Math.max(max2, tab[i]);
 				max5 = Math.max(max5, tab[i]);
 
 				if(i%2==1){//update parent 2
 					two.fill(startPosition, max2, (int)Math.ceil((double)imageNumber / 2));
 					if(i<tab.length-1){
 						max2=tab[i+1];
 					}
 				}
 
 
 				if(i%5==4){//update parent 5
 					five.fill(startPosition, max5, (int)Math.ceil((double)imageNumber / 5));
 					if(i<tab.length-1){
 						max5=tab[i+1];
 					}
 				}
 			}
 		}
 	}
 
 
 	protected void endWriting() throws SQLException{
 		tree.writeValues(this, true);
 		if(!this.last){
 			this.updateParents();
 			if(null != this.one){
 				this.one.endWriting();
 			} else if(this.two != null){
 				this.two.endWriting();
 				this.five.endWriting();
 			}
 		}
 	}
 	/**
 	 * @return the zoom
 	 */
 	protected int getZoom() {
 		return zoom;
 	}
 	protected float[] getTab(){
 		return tab;
 	}
 
 	protected void setImageNumber(int imageNumber) {
 		this.imageNumber = imageNumber;
 
 	}
 	protected int getImageNumber(){
 		return this.imageNumber;
 	}
 
 	protected int getStartPosition(int position){
 
 		int index = this.getTabIndex(position);
 		int result = position - (index * zoom);
 
 		if(result <= 0){
 			return 0;
 		}
 		return result;
 	}
 
 	protected int getTabIndex(int position) {
 		int ind = (int)Math.ceil((double)(position / zoom) % TAB_WIDTH);
 		return ind;
 	}
 }
