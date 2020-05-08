 package com.google.gwt.forum.client;
 
 import com.allen_sauer.gwt.dnd.client.DragContext;
 import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class FlexTableRowDropController extends AbstractDropController{
 
 	Forum parent;
 	
 	public FlexTableRowDropController(Widget dropTarget, Forum parent) {
 		super(dropTarget);
 		this.parent = parent;
 	}
 
 	@Override
 	public void onDrop(DragContext context){
 		super.onDrop(context);
 		AbsolutePanel target = (AbsolutePanel) getDropTarget();
 		
		//int factor = 6200000; // Thats the biggest amount / 100
		//int table_size = 490;
 		
 		// Aqui se define lo que quieres que haga la el target widget cuando le
 		// sueltes lo que sea
 		//target.setTitle(parent.currentCity.getCity()+" "+parent.currentCity.getAmmount());
 		//target.add(new Label(parent.currentCity.getCity()+parent.currentCity.getAmmount()));
 		Label temp = new Label();
 	//	temp.setWidth(String.valueOf( (parent.currentCity.getAmmount()*table_size)/factor)+"px");
 		temp.setStyleName("graphic_bars");
 		temp.setText(" .");
 		target.add(temp);
 		
 	}
 
 	
 }
