 /**
  * 
  */
 package fhdw.ipscrum.shared.model;
 
 import fhdw.ipscrum.shared.model.visitor.IProductBacklogItemVisitor;
 
 /**
  * @author Niklas
  * 
  */
public class Bug extends ProductBacklogItem {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fhdw.ipscrum.shared.model.ProductBacklogItem#accept(fhdw.ipscrum.shared
 	 * .model.visitor.IProductBacklogItemVisitor)
 	 */
 	@Override
 	public void accept(final IProductBacklogItemVisitor visitor) { //
 		visitor.handleBug(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fhdw.ipscrum.shared.model.ProductBacklogItem#initialize()
 	 */
 	@Override
 	protected void initialize() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
