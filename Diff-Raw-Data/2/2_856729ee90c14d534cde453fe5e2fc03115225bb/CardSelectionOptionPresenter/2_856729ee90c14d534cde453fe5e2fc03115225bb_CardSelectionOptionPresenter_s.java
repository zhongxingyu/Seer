 package be.betty.gwtp.client.presenters;
 
 import java.util.LinkedHashMap;
 
 import be.betty.gwtp.client.ClientUtils;
 import be.betty.gwtp.client.Storage_access;
 import be.betty.gwtp.client.UiConstants;
 
 import be.betty.gwtp.client.event.SetViewEvent;
 import be.betty.gwtp.client.event.ShowPlacedCardEvent;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.smartgwt.client.types.MultipleAppearance;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
 import com.smartgwt.client.widgets.form.fields.FormItem;
 import com.smartgwt.client.widgets.form.fields.SelectItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 
 public class CardSelectionOptionPresenter extends PresenterWidget<CardSelectionOptionPresenter.MyView> {
 
 	public interface MyView extends View {
 
 		SimplePanel getSimplePanel();
 		SimplePanel getSimplePanelFirstFilter();
 		CheckBox getDoShowPlacedCard();
 		CheckBox getDoSwitchView();
 	}
 
 	private EventBus myEventBus;
 
 	private SelectItem selectItemMultiplePickList;
 
 	private String current = "init";
 
 	private DynamicForm multiSelectComboForm;
 	private DynamicForm selectComboForm;
 
 	private String[] checkBoxTab;
 
 	private String indexFirstComboBox;
 	private FormItem firstComboBox;
 	private LinkedHashMap<String, String> valueMap;
 	private boolean showPlacedCard = true;
 
 
 	@Inject
 	public CardSelectionOptionPresenter(final EventBus eventBus, final MyView view) {
 		super(eventBus, view);
 		myEventBus = eventBus;
 
 
 		selectComboForm = new DynamicForm();
 		firstComboBox = new ComboBoxItem();   
 
 
 		multiSelectComboForm = new DynamicForm();
 		selectItemMultiplePickList = new SelectItem();
 
 	}
 
 
 	@Override protected void onBind() {
 		super.onBind();
 
 		getView().getSimplePanel().clear();
 		getView().getSimplePanel().add(multiSelectComboForm);
 
 		getView().getDoShowPlacedCard().setTitle("Show the placed card of the curent filter");
 		getView().getDoSwitchView().setTitle("Switch automatically to corresponding view");
 		
 		getView().getDoShowPlacedCard().setValue(true);
 		getView().getDoShowPlacedCard().addClickHandler( new ClickHandler() {
 			@Override public void onClick(ClickEvent event) {
 				getEventBus().fireEvent(new ShowPlacedCardEvent(getView().getDoShowPlacedCard().getValue()));
 			}
 		});
 
 		firstComboBox.addChangedHandler(new ChangedHandler() {
 			@Override public void onChanged(ChangedEvent event) {
 				try {
 					//System.out.println("x"+event.getValue().toString());
 					//System.out.println(event.getSource().toString());
 					indexFirstComboBox = event.getValue().toString();
 
 					//ClientUtils.notifyUser(indexFirstComboBox, myEventBus);
 					if (!valueMap.containsKey(indexFirstComboBox))
 						return;
 
 					if (!indexFirstComboBox.equals("0")) {
 						printSecondComboBxView(Integer.parseInt(indexFirstComboBox));
 						for (int i = 0; i < MainPresenter.allCards.size(); i++) {
 							MainPresenter.allCards.get(""+i).setVisible(false);
 						}
 					} else {
 						//getView().getGroup_choice().setVisible(false);
 						for (int i = 0; i < MainPresenter.allCards.size(); i++) 
 							MainPresenter.allCards.get(""+i).setVisible(true);
 						if(!showPlacedCard)
 							for (int k = 0; k < MainPresenter.allCards.size(); k++)
 								if (MainPresenter.allCards.get(""+k).isPlaced())
 									MainPresenter.allCards.get(""+k).setVisible(false);
 							
 						
 						multiSelectComboForm.hide();
 					}
 				} catch (Exception e) {
 					//ClientUtils.notifyUser("exception: "+e, myEventBus);
 				}
 			}
 		});
 
 		selectItemMultiplePickList.addChangedHandler(new ChangedHandler() {
 
 			@Override public void onChanged(ChangedEvent event) {
 				redrawAllCardsFromSelectionPanel();
 				//ClientUtils.notifyUser("on changed (second combobox)", myEventBus);
 				//ClientUtils.notifyUser("size = "+MainPresenter.allCards.size(), myEventBus);
 				
 			}
 
 	
 		});
 	}
 
 	@Override protected void onReset() {
 		super.onReset();
 	}
 
 	public void redrawAllCardsFromSelectionPanel() {
 		for (int i = 0; i < MainPresenter.allCards.size(); i++) {
 
 			//ClientUtils.notifyUser("accession card num "+i+"and its tostring value is: "+MainPresenter.allCards.get(i), myEventBus);
 			MainPresenter.allCards.get(""+i).setVisible(false);
 		}
 		
 		if(firstComboBox.getDisplayValue().equalsIgnoreCase("All Card")){
 			for (int i = 0; i < MainPresenter.allCards.size(); i++) 
 				MainPresenter.allCards.get(""+i).setVisible(true);
 			if(!showPlacedCard)
 				for (int k = 0; k < MainPresenter.allCards.size(); k++)
 					if (MainPresenter.allCards.get(""+k).isPlaced())
 						MainPresenter.allCards.get(""+k).setVisible(false);
 		}
 		
 		try{
 			
 			checkBoxTab = selectItemMultiplePickList.getValues();
 			if(checkBoxTab.length!=0){
 			if (getView().getDoSwitchView().getValue()) {
 				myEventBus.fireEvent(new SetViewEvent(indexFirstComboBox,checkBoxTab[0]));
 				if (!current.equalsIgnoreCase(checkBoxTab[0])){
 					String[] text = selectItemMultiplePickList.getDisplayValue().split(",");
 					String notif = "The view of "+firstComboBox.getDisplayValue()+" "+text[0]+" is selected";
 					current = checkBoxTab[0];
					if(!current.equalsIgnoreCase("&nbsp"));
 					ClientUtils.notifyUser(notif, UiConstants.getNotifCss(), getEventBus());
 				}
 			}
 			for(int i=0; i< checkBoxTab.length; i++){
 				String str = checkBoxTab[i];
 				for (int j = 0; j < MainPresenter.allCards.size(); j++){
 					//ClientUtils.notifyUser("i= "+i+"j ="+j, myEventBus);
 					if (indexFirstComboBox.equalsIgnoreCase("1")) {
 						if (str.equals(""+MainPresenter.allCards.get(""+j).getTeacherId()))
 							MainPresenter.allCards.get(""+j).setVisible(true);
 					}else if (indexFirstComboBox.equalsIgnoreCase("2")) {
 							for (int k = 0; k < +MainPresenter.allCards.get(""+j).getGroupsId().length; k++)
 								if (str.equals(""+MainPresenter.allCards.get(""+j).getGroupsId()[k]))
 									MainPresenter.allCards.get(""+j).setVisible(true);
 					}
 				}
 			}
 			if(!showPlacedCard)
 				for (int k = 0; k < MainPresenter.allCards.size(); k++)
 					if (MainPresenter.allCards.get(""+k).isPlaced())
 						MainPresenter.allCards.get(""+k).setVisible(false);
 			}
 		}catch (Exception E){
 			//ClientUtils.notifyUser("exeption (second combobox) ==> "+E, myEventBus);
 			System.out.println(E);
 		}
 		
 	}
 	
 	/**
 	 * PRE: the local storage must be filled
 	 */
 	public void init() {
 		System.out.println("init!!");
 		setStaticFirstComboBox();
 		multiSelectComboForm.setWidth(200);
 	}
 
 
 	private void setStaticFirstComboBox(){
 
 
 		//getView().getSimplePanel().clear();
 		getView().getSimplePanelFirstFilter().clear();
 
 		getView().getSimplePanelFirstFilter().add(selectComboForm);
 
 
 		selectComboForm.setWidth(200); 
 		firstComboBox.setTitle("Option");
 		firstComboBox.setType("comboBox");
 		valueMap = new LinkedHashMap<String, String>();
 		valueMap.put("0", "All card");
 		valueMap.put("1", "Professor");
 		valueMap.put("2","Group");
 		valueMap.put("3", "Type");
 		firstComboBox.setValueMap(valueMap);
 		firstComboBox.setDefaultValue("All Card");
 		selectComboForm.setItems(firstComboBox);
 		selectComboForm.redraw();
 
 	}
 
 	//TODO Changer le nom de cette fonction pour que ca soit plus adapte a la nouvelle configuration (comboBox)
 	public void printSecondComboBxView(int selectedIndex) {
 		assert selectedIndex >= 1 && selectedIndex <= 3;
 
 		multiSelectComboForm.setWidth(200);
 		multiSelectComboForm.clearValues();
 		multiSelectComboForm.clear();
 
 		getView().getSimplePanel().clear();
 
 		switch (selectedIndex) {
 
 		case 1:
 			LinkedHashMap<String, String> valueMapTeach = new LinkedHashMap<String, String>();
 			for (int i = 0; i < Storage_access.getNumberOfTeacher(); i++) {
 				valueMapTeach.put(""+i, Storage_access.getTeacher(i));
 			}
 			//ClientUtils.notifyUser(valueMapTeach.toString(), this.myEventBus);
 			selectItemMultiplePickList.setTitle("prof");
 			//selectItemMultiplePickList.setTitleStyle(titleStyle);
 			selectItemMultiplePickList.setMultiple(true);  
 			selectItemMultiplePickList.setMultipleAppearance(MultipleAppearance.PICKLIST);  
 			selectItemMultiplePickList.setValueMap(valueMapTeach);
 			multiSelectComboForm.setItems(selectItemMultiplePickList);
 			getView().getSimplePanel().add(multiSelectComboForm);
 			multiSelectComboForm.show();
 			break;
 
 		case 2:
 			LinkedHashMap<String, String> valueMapGroup = new LinkedHashMap<String, String>();
 			for (int i = 0; i < Storage_access.getNumberOfGroup(); i++) {
 				valueMapGroup.put(""+i, Storage_access.getGroup(i));
 			}
 			selectItemMultiplePickList.setTitle("Group");
 			selectItemMultiplePickList.setMultiple(true);
 			selectItemMultiplePickList.setMultipleAppearance(MultipleAppearance.PICKLIST);
 			selectItemMultiplePickList.setValueMap(valueMapGroup);
 			multiSelectComboForm.setItems(selectItemMultiplePickList);
 			getView().getSimplePanel().add(multiSelectComboForm);
 			multiSelectComboForm.show();
 			break;
 
 		case 3:
 			selectItemMultiplePickList.setTitle("Type");
 			selectItemMultiplePickList.setMultiple(true);
 			selectItemMultiplePickList.setMultipleAppearance(MultipleAppearance.PICKLIST);
 			selectItemMultiplePickList.setValueMap("Informatic", "group", "class");
 			multiSelectComboForm.setItems(selectItemMultiplePickList);
 			getView().getSimplePanel().add(multiSelectComboForm);
 			multiSelectComboForm.show();
 			break;
 
 		default:
 
 			return;
 		}
 	}
 
 
 	public void setShowPlacedCard(boolean showPlacedCard) {
 		this.showPlacedCard = showPlacedCard;
 	}
 
 }
