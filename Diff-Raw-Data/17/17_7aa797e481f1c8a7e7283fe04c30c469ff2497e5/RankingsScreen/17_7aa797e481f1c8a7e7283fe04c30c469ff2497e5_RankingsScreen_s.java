 package net.mytcg.topcar.ui;
 
 import java.util.Vector;
 
 import net.mytcg.topcar.ui.custom.ColorLabelField;
 import net.mytcg.topcar.ui.custom.FixedButtonField;
 import net.mytcg.topcar.ui.custom.ListItemField;
 import net.mytcg.topcar.ui.custom.PageNumberField;
 import net.mytcg.topcar.ui.custom.SexyEditField;
 import net.mytcg.topcar.util.Const;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 
 public class RankingsScreen extends AppScreen implements FieldChangeListener
 {
 	FixedButtonField exit = new FixedButtonField(Const.back);
 	PageNumberField pageNumber = new PageNumberField("Page 1/1");
 	SexyEditField amount = new SexyEditField("");
 	ColorLabelField lblType = new ColorLabelField("No users found.");
 	ListItemField lblRanking = null;
 	Vector pages = new Vector();
 	Vector tempList = new Vector();
 	int currentPage = 0;
 	
 	public RankingsScreen(String category, int categoryId, boolean friends)
 	{
 		super(null);
 		add(new ColorLabelField(""));
 		bgManager.setStatusHeight(exit.getContentHeight());
 		bgManager.setArrowMode(true);
 		
 		lblRanking = new ListItemField(category, -1, false, 0);
 		tempList.addElement(lblRanking);
 		lblRanking.setFocusable(false);
 		
 		if(!friends){
 			doConnect(Const.leaderboard+categoryId);
 		}else{
 			doConnect(Const.leaderboard+categoryId+"&"+Const.friends);
 		}
 		
 		exit.setChangeListener(this);
 		
 		addButton(new FixedButtonField(""));
 		addButton(pageNumber);
 		addButton(exit);
 	}
 	
 	public void process(String val) {
 		int listSize = (Const.getUsableHeight()) / (Const.getButtonHeight()+12);
 		int listCounter = 1;
 		pages = new Vector();
 		if (!(isDisplaying())) {
 			int fromIndex;
 	    	if ((fromIndex = val.indexOf(Const.xml_result)) != -1) {
 	    		//setText(val.substring(fromIndex+Const.xml_result_length, val.indexOf(Const.xml_result_end, fromIndex)));
 	    	} if (((fromIndex = val.indexOf(Const.xml_leaderboard)) != -1)) {
 	    		int value = -1;
 	    		String usr = "";
 	    		
 	    		int endIndex = -1;
 	    		String ranking = "";
 	    		while ((fromIndex = val.indexOf(Const.xml_valt)) != -1){
 	    			if(listCounter >= listSize){
 	    				pages.addElement(tempList);
 	    				tempList = new Vector();
 	    				listCounter=0;
 	    			}
 	    			endIndex = val.indexOf(Const.xml_leader_end);
 	    			try{
 	    				ranking = val.substring(fromIndex, endIndex+Const.xml_leader_end_length);
 	    			}catch(Exception e){};
 	    			fromIndex = ranking.indexOf(Const.xml_valt);
 	    			
 	    			try {
 	    				value = Integer.parseInt(ranking.substring(fromIndex+Const.xml_valt_length, ranking.indexOf(Const.xml_valt_end, fromIndex)));
 	    			} catch (Exception e) {
 	    				value = -1;
 	    			}
 	    			if ((fromIndex = ranking.indexOf(Const.xml_usr)) != -1) {
 	    				usr = ranking.substring(fromIndex+Const.xml_usr_length, ranking.indexOf(Const.xml_usr_end, fromIndex));
 	    			}
 	    			val = val.substring(val.indexOf(Const.xml_leader_end)+Const.xml_leader_end_length);
 	    			if(value != -1){
 	    				try{
 	    					lblType = new ColorLabelField("        " + usr);
 			    			synchronized(UiApplication.getEventLock()) {
 			    				tempList.addElement(lblType);
			    				amount = new SexyEditField(""+value);
 			    				amount.setEdit(false);
 			    				tempList.addElement(amount);
 			        			listCounter++;
 			    			}
 	    				}catch(Exception e){};
 	    			}
 	    		} if(value == -1){
     				synchronized(UiApplication.getEventLock()) {
     					tempList.addElement(lblType);
 	    			}
     			}
 	    		pages.addElement(tempList);
 		        synchronized(UiApplication.getEventLock()) {
 		        	System.out.println("SIZE "+((Vector)pages.elementAt(0)).size());
 		        	if(pages.size()<=1){
 	    				bgManager.setArrowMode(false);
 	    			}
 		        	pageNumber.setLabel("Page 1/"+pages.size());
 		        	Field[] temp = new Field[((Vector)pages.elementAt(0)).size()];
 		        	((Vector)pages.elementAt(0)).copyInto(temp);
 		        	bgManager.deleteAll();
 		    	   	bgManager.addAll(temp);
 		    	}
 	    	}
 	    	invalidate();
 	    	//setDisplaying(true);
 		}		
 	}
 	
 	public boolean navigationMovement(int dx, int dy, int status, int time) {
 		if(dy == 0 && dx == -1){
 			if(pages.size() >1){
 				if((currentPage-1)<0){
 					currentPage = pages.size()-1;
 				}else{
 					currentPage--;
 				}
 				synchronized(UiApplication.getEventLock()) {
 					pageNumber.setLabel("Page "+(currentPage+1)+"/"+pages.size());
 					Field[] temp = new Field[((Vector)pages.elementAt(currentPage)).size()];
 	    			((Vector)pages.elementAt(currentPage)).copyInto(temp);
 	    			bgManager.deleteAll();
 		    		bgManager.addAll(temp);
 		    	}
 			}
 			return true;
 		}else if(dy == 0 && dx == 1){
 			if(pages.size() >1){
 				if((currentPage+1)>=pages.size()){
 					currentPage = 0;
 				}else{
 					currentPage++;
 				}
 				synchronized(UiApplication.getEventLock()) {
 					pageNumber.setLabel("Page "+(currentPage+1)+"/"+pages.size());
 					Field[] temp = new Field[((Vector)pages.elementAt(currentPage)).size()];
 	    			((Vector)pages.elementAt(currentPage)).copyInto(temp);
 	    			bgManager.deleteAll();
 		    		bgManager.addAll(temp);
 		    	}
 			}
 			return true;
 		}else{
 			return super.navigationMovement(dx, dy, status, time);
 		}
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		if (f == exit) {
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		} 
 	}
 }
