 package team.GunsPlus.Gui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericComboBox;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericListWidget;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericRadioButton;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.ListWidgetItem;
 import org.getspout.spoutapi.gui.Screen;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.Block.TripodData;
 import team.GunsPlus.Enum.Target;
 import team.GunsPlus.Enum.Target.Player;
 import team.GunsPlus.Enum.Target.Animal;
 import team.GunsPlus.Enum.Target.Monster;
 import team.GunsPlus.Util.Task;
 
 public class TripodPopup extends GenericPopup{
 	
 	private Map<String, UUID> ids = new HashMap<String, UUID>(); 
 	private Task update;
 	private TripodData data;
 	private GenericLabel title = new GenericLabel("Tripod Menu");
 	private GenericRadioButton auto = new GenericRadioButton("automatic");
 	private GenericRadioButton manu = new GenericRadioButton("manual");
 	private GenericButton apply = new GenericButton("apply");
 	private GenericLabel targets = new GenericLabel("Targets:");
 	private GenericLabel general = new GenericLabel("General:");
 	private GenericListWidget list = new GenericListWidget();
 	private GenericComboBox combo_type = new GenericComboBox();
 	private GenericComboBox combo_name = new GenericComboBox();
 	private GenericTextField nfield = new GenericTextField();
 	private GenericButton tok = new GenericButton("OK ?");
 	private GenericButton del = new GenericButton("delete");
 	private GenericButton edit = new GenericButton("edit");
 	private GenericButton add = new GenericButton("add");
 	private GenericLabel working = new GenericLabel("WORKING");
 	private GenericLabel entered = new GenericLabel("ENTERED");
 	
 	private int mode = -1;
 	
 	public TripodPopup(GunsPlus plugin, TripodData td){
 		setData(td);
 		this.setEverythingVisible();
 		
 		title.setAnchor(WidgetAnchor.SCALE);
 		title.setX(100).setY(20).setWidth(50).setHeight(15);
 		addId("TITLE", title.getId());
 		
 		working.setAnchor(WidgetAnchor.SCALE);
 		working.setX(200).setY(140).setWidth(70).setHeight(50);
 		working.setVisible(false);
 		working.setTooltip("Punch the tripod to stop it working.");
 		addId("WORKING", working.getId());
 		
 		entered.setAnchor(WidgetAnchor.SCALE);
 		entered.setX(200).setY(140).setWidth(70).setHeight(50);
 		entered.setVisible(false);
 		entered.setTooltip("Punch the tripod to exit.");
 		addId("ENTERED", entered.getId());
 		
 		targets.setAlign(WidgetAnchor.SCALE);
 		targets.setX(120).setY(35).setWidth(50).setHeight(15);
 		addId("TITLE", title.getId());
 		
 		general.setAlign(WidgetAnchor.SCALE);
 		general.setX(20).setY(40).setWidth(50).setHeight(15);
 		addId("GENERAL", general.getId());
 		
 		auto.setAnchor(WidgetAnchor.SCALE);
 		auto.setX(20).setY(60).setWidth(30).setHeight(15);
 		auto.setTooltip("If you select this, you can start the tripod working by punching it.");
 		addId("AUTO", auto.getId());
 		
 		manu.setAnchor(WidgetAnchor.SCALE);
 		manu.setX(20).setY(80).setWidth(30).setHeight(15);
 		manu.setTooltip("If you select this, you can enter the tripod by punching it.");
 		addId("MANU", manu.getId());
 		
 		auto.setGroup(1);
 		manu.setGroup(1);
 		
 		apply.setAnchor(WidgetAnchor.SCALE);
 		apply.setX(350).setY(100).setWidth(30).setHeight(15);
 		apply.setTooltip("Press to confirm and save changes.");
 		addId("APPLY", apply.getId());
 		
 		add.setAnchor(WidgetAnchor.SCALE);
 		add.setX(390).setY(55).setWidth(40).setHeight(15);
 		add.setTooltip("Add a target to the list.");
 		addId("ADD", add.getId());
 		
 		del.setAnchor(WidgetAnchor.SCALE);
 		del.setX(390).setY(70).setWidth(40).setHeight(14);
 		del.setTooltip("Delete the currently selected target.");
 		addId("DEL", del.getId());
 		
 		edit.setAnchor(WidgetAnchor.SCALE);
 		edit.setX(390).setY(90).setWidth(40).setHeight(14);
 		edit.setTooltip("Edit the currently selected target.");
 		addId("EDIT", edit.getId());
 		
 		list.setAnchor(WidgetAnchor.SCALE);
 		list.setX(300).setY(55).setWidth(80).setHeight(80);
 		list.setVisible(false).setDirty(true);
 		list.setTooltip("The tripod will fire at all targets in this list.");
 		addId("LIST", list.getId());
 		
 		combo_type.setAnchor(WidgetAnchor.SCALE).setX(300).setY(65).setWidth(70).setHeight(15);
 		combo_type.setText("Type");
 		combo_type.setVisible(false);
 		addId("COMBOTYPE", combo_type.getId());
 		
 		nfield.setAnchor(WidgetAnchor.SCALE).setX(300).setY(80).setWidth(70).setHeight(15);
 		nfield.setText("Name");
 		nfield.setVisible(false);
 		nfield.setTooltip("Type the name of the player here.");
 		addId("NAMEFIELD", nfield.getId());
 		
 		combo_name.setAnchor(WidgetAnchor.SCALE).setX(300).setY(80).setWidth(70).setHeight(15);
 		combo_name.setText("Name");
 		combo_name.setVisible(false);
 		addId("COMBONAME", combo_name.getId());
 		
 		tok.setAnchor(WidgetAnchor.SCALE);
 		tok.setX(300).setY(95).setWidth(70).setHeight(15);
 		tok.setVisible(false);	
 		tok.setTooltip("Ok ?");
 		addId("OK", tok.getId());
 		
 		this.attachWidgets(plugin, title, auto, manu, apply, add, targets, list,  nfield ,tok,del,edit, combo_type, combo_name, general,  working, entered);
 		
 		if(data.isAutomatic()){
 			auto.setSelected(true);
 			setChooserEnabled();
 		}else{
 			manu.setSelected(true);
 			setChooserDisabled();
 		}
 		
 		List<String> ls = new ArrayList<String>();
 		for(Target t : Target.values()){
 			ls.add(t.name());
 		}
 		combo_type.setItems(ls);
 		setComboNameAnimal();
 		
 		for(Target t : data.getTargets()){
 			list.addItem(new ListWidgetItem(t.toString(), t.equals(Target.PLAYER)?((Player)t.getEntity()).getName():t.getEntity().toString()));
 		}
 		update = new Task(GunsPlus.plugin){
 			public void run(){
 				if(!(combo_type.getSelectedItem()==null||combo_name.getSelectedItem()==null)&&(mode==0||mode==1)){
 					if(combo_type.getSelectedItem().equalsIgnoreCase("PLAYER")){
 						setPlayerChooser();
 					}else if(combo_type.getSelectedItem().equalsIgnoreCase("MONSTER")){
 						setComboNameMonster();
 						removePlayerChooser();
 						setTargetChooser();
 					}else if(combo_type.getSelectedItem().equalsIgnoreCase("ANIMAL")){
 						setComboNameAnimal();
 						removePlayerChooser();
 						setTargetChooser();
 					}
 					combo_type.setText(combo_type.getSelectedItem()!=null?combo_type.getSelectedItem():combo_type.getText());
 					combo_name.setText(combo_name.getSelectedItem()!=null?combo_name.getSelectedItem():combo_name.getText());
 					combo_name.setDirty(true);
 					combo_type.setDirty(true);
 				}
 				if(!(list.getItems().length<=0)){
 					list.setVisible(true);
 				}else{
 					list.setVisible(false);
 				}
 				if(data.isWorking()){
 					setEverythingInvisible();
 					working.setVisible(true);
 				}else if(data.isEntered()){
 					setEverythingInvisible();
 					entered.setVisible(true);
 				}
 			}
 		};
 		update.startTaskRepeating(5);
 	}
 	
 	public void resize(Screen s){
 		int w = s.getWidth();
 		int h = s.getHeight();
 		title.setX(w/2-20);
 		targets.setX((w/3)*2);
 		apply.setX(w-40);
 		apply.setY(h-25);
 		list.setX((w/3)*2).setY(targets.getY()+targets.getHeight()+5);
 		add.setX(list.getX()+list.getWidth()+5).setY(list.getY());
 		del.setX(list.getX()+list.getWidth()+5).setY(add.getY()+add.getHeight()+5);
 		edit.setX(list.getX()+list.getWidth()+5).setY(del.getY()+del.getHeight()+5);
 		combo_type.setX((w/3)*2).setY(list.getY()+list.getHeight()+5);
 		combo_name.setX((w/3)*2).setY(combo_type.getY()+combo_type.getHeight()+5);
 		nfield.setX((w/3)*2).setY(combo_type.getY()+combo_type.getHeight()+5);
 		tok.setX((w/3)*2).setY(nfield.getY()+nfield.getHeight()+5);
 	}
 	
 	public void setTargetChooser(){
 		if(mode==0){
 			combo_type.setVisible(true);
 			combo_name.setVisible(true);
 			tok.setVisible(true);
 		}else if(mode==1){
 			if(list.getSelectedItem()!=null){
 				String t = list.getSelectedItem().getTitle();
 				for(String s : combo_type.getItems()){
 					if(s.equalsIgnoreCase(t)){
 						combo_type.setSelection(combo_type.getItems().indexOf(s));
 					}
 				}
 			}
 			combo_type.setVisible(true);
 			combo_name.setVisible(true);
 			tok.setVisible(true);
 		}else if(mode==2){
 			tok.setVisible(true);
 		}else{}
 	}
 	
 	public void applyData(){
		ArrayList<Target> tars = new ArrayList<Target>();
 		for(ListWidgetItem i : list.getItems()){
 			Target t = parseTarget(i.getTitle(), i.getText(), i.getText());
 			tars.add(t);
 		}
 		data.setTargets(tars);
 		if(auto.isSelected()){
 			data.setAutomatic(true);
 		}else{
 			data.setAutomatic(false);
 		}
 	}
 	
 	public void setEverythingInvisible(){
 		for(Widget w : this.getAttachedWidgets()){
 			w.setVisible(false);
 		}
 	}
 	
 	public void setEverythingVisible(){
 		for(Widget w : this.getAttachedWidgets()){
 			w.setVisible(true);
 		}
 	}
 	
 	public void setChooserDisabled(){
 		combo_type.setEnabled(false);
 		combo_name.setEnabled(false);
 		nfield.setEnabled(false);
 		tok.setEnabled(false);
 		list.setEnabled(false);
 		add.setEnabled(false);
 		del.setEnabled(false);
 		edit.setEnabled(false);
 	}
 	
 	public void setChooserEnabled(){
 		combo_type.setEnabled(true);
 		combo_name.setEnabled(true);
 		nfield.setEnabled(true);
 		tok.setEnabled(true);
 		list.setEnabled(true);
 		add.setEnabled(true);
 		del.setEnabled(true);
 		edit.setEnabled(true);
 	}
 
 	public void removeTargetChooser(){
 		combo_type.setVisible(false);
 		combo_name.setVisible(false);
 		tok.setVisible(false);
 	}
 	
 	public void setPlayerChooser(){
 		combo_type.setVisible(true);
 		combo_name.setVisible(false);
 		nfield.setVisible(true);
 	
 	}
 	
 	public void removePlayerChooser(){
 		nfield.setVisible(false);
 	}
 	
 	public void performListAction(){
 		if(mode==0){
 			if(combo_type.getSelectedItem()!=null&&combo_name.getSelectedItem()!=null){
 				Target t = parseTarget(combo_type.getSelectedItem(), combo_name.getSelectedItem(), nfield.getText());
 				if(t!=null){
 					list.addItem(new ListWidgetItem(t.toString(), t.equals(Target.PLAYER)?((Player)t.getEntity()).getName():t.getEntity().toString()));
 				}
 			}
 		}else if(mode==1){
 			if(combo_type.getSelectedItem()!=null&&combo_name.getSelectedItem()!=null){
 				Target t = parseTarget(combo_type.getSelectedItem(), combo_name.getSelectedItem(), nfield.getText());
 				if(t!=null&&list.getSelectedItem()!=null){
 					list.getSelectedItem().setTitle(t.toString());
 					list.getSelectedItem().setText(t.equals(Target.PLAYER)?((Player)t.getEntity()).getName():t.getEntity().toString());
 				}
 			}
 		}else if(mode==2){
 			if(list.getSelectedItem()!=null)
 				list.removeItem(list.getSelectedItem());
 		}else{return;}
 		setNoMode();
 	}
 	
 	public Target parseTarget(String type, String name, String extra){
 		Target t = null;
 		 t = Target.valueOf(type.toUpperCase());
 		switch(t){
 			case PLAYER:
 				Player p = Player.Player;
 				p.setName(extra);
 				t.setEntity(p);
 				t.setPriority(getListRowByListItem(getListItemByTypeAndName(type, extra)));
 				break;
 			case ANIMAL:
 				try{
 					t.setEntity(Animal.valueOf(name.toUpperCase()));
 					t.setPriority(getListRowByListItem(getListItemByTypeAndName(type, name)));
 				}catch(Exception e){}
 				break;
 			case MONSTER:
 				try{
 					t.setEntity(Monster.valueOf(name.toUpperCase()));
 					t.setPriority(getListRowByListItem(getListItemByTypeAndName(type, name)));
 				}catch(Exception e){}
 				break;
 		}
 		return t;
 	}
 	
 	public int getListRowByListItem(ListWidgetItem lw){
 		for(int i=0;i<list.getItems().length;i++){
 			if(list.getItems()[i].equals(lw)){
 				return i;
 			}
 		}
 		return 0;
 	}
 	
 	public ListWidgetItem getListItemByTypeAndName(String t, String n){
 		ListWidgetItem w = null;
 		for(ListWidgetItem l : list.getItems()){
 			if(l.getTitle().equalsIgnoreCase(t)&&l.getText().equalsIgnoreCase(n)){
 				w = l;
 			}
 		}
 		return w;
 	}
 		
 	
 	public void attach(SpoutPlayer sp){
 		resize(sp.getMainScreen());
 		sp.getMainScreen().attachPopupScreen(this);
 	}
 	
 	public void close(SpoutPlayer sp){
 		if(sp.getMainScreen().getActivePopup().equals(this)){
 			if(update.isTaskRunning())
 				update.stopTask();
 			sp.getMainScreen().getActivePopup().close();
 		}
 	}
 
 	public TripodData getData() {
 		return data;
 	}
 
 	public void setData(TripodData data) {
 		this.data = data;
 	}
 	
 	public void setAddMode(){
 		mode = 0;
 	}
 	
 	public void setEditMode(){
 		mode = 1;
 	}
 	
 	public void setDelMode(){
 		mode = 2;
 	}
 	
 	public void setNoMode(){
 		mode = -1;
 	}
 	
 	public int getMode(){
 		return mode;
 	}
 	
 	public void setComboNameMonster(){
 		List<String> ls = new ArrayList<String>();
 		for(Monster m: Monster.values()){
 			ls.add(m.toString());
 		}
 		combo_name.setItems(ls);
 	}
 	
 	public void setComboNameAnimal(){
 		List<String> ls = new ArrayList<String>();
 		for(Animal m: Animal.values()){
 			ls.add(m.toString());
 		}
 		combo_name.setItems(ls);
 	}
 
 	public Map<String, UUID> getIds() {
 		return ids;
 	}
 
 	public void setIds(Map<String, UUID> ids) {
 		this.ids = ids;
 	}
 	
 	public void addId(String w, UUID i){
 		this.ids.put(w, i);
 	}
 	
 	public void removeId(String w){
 		this.ids.remove(w);
 	}
 	
 	public UUID getId(String n){
 		return ids.containsKey(n)?ids.get(n):null;
 	}
 }
