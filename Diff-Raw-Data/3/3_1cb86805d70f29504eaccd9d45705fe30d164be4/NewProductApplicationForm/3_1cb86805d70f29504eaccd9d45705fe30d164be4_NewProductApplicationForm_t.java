 /*
  * $Id:$
  *
  * Copyright (C) 2002 Idega hf. All Rights Reserved.
  *
  * This software is the proprietary information of Idega hf.
  * Use is subject to license terms.
  *
  */
 package is.idega.idegaweb.atvr.supplier.application.presentation;
 
 import com.idega.presentation.Block;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.PresentationObject;
 import com.idega.presentation.Table;
 import com.idega.presentation.ui.DropdownMenu;
 import com.idega.presentation.ui.Form;
 import com.idega.presentation.ui.SubmitButton;
 import com.idega.presentation.ui.TextInput;
 import com.idega.util.IWTimestamp;
 
 import is.idega.idegaweb.atvr.supplier.application.business.NewProductApplicationBusiness;
 import is.idega.idegaweb.atvr.supplier.application.data.NewProductApplication;
 
 import java.rmi.RemoteException;
 
 /**
  * This class does something very clever.....
  * 
  * @author <a href="palli@idega.is">Pall Helgason</a>
  * @version 1.0
  */
 public class NewProductApplicationForm extends Block {
 	private final static String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.atvr";
 
 	private final static int ACTION_VIEW_FORM = 0;
 	private final static int ACTION_SUBMIT_FORM = 1;
 
 	private final static int TYPE_TRIAL = 0;
 	private final static int TYPE_SPECIAL = 1;
 	private final static int TYPE_MONTH = 2;
 	private final static int TYPE_TOBACCO = 3;
 
 	private final static String PARAM_FORM_TYPE = "npa_type";
 	private final static String PARAM_FORM_CATEGORY = "npa_cat";
 	private final static String PARAM_FORM_SUBMIT = "npa_submit";
 
 	private final static String PARAM_DESC = "npa_desc";
 	private final static String PARAM_DESC2 = "npa_desc2";
 	private final static String PARAM_QUANTITY = "npa_qty";
 	private final static String PARAM_STRENGTH = "npa_str";
 	private final static String PARAM_PRODUCER = "npa_prdc";
 	private final static String PARAM_COUNTRY = "npa_ctr";
 	private final static String PARAM_BAR_CODE = "npa_bar";
 	private final static String PARAM_SUB_CATEGORY = "npa_sub_cat";
 	private final static String PARAM_AMOUNT = "npa_amount";
 	private final static String PARAM_WEIGHT = "npa_weigth";
 	private final static String PARAM_MONOXIDE = "npa_monoxide";
 
 	private String _type = "0";
 
 	public String getBundleIdentifier() {
 		return IW_BUNDLE_IDENTIFIER;
 	}
 
 	private void control(IWContext iwc) {
 		if (iwc.getParameter(PARAM_FORM_SUBMIT) == null) 
 			showForm(iwc);
 		else
 			submitForm(iwc);
 	}
 
 	private void submitForm(IWContext iwc) {
 		try {
 			NewProductApplication appl = getApplicationBusiness(iwc).getNewApplication();
 
 			String desc = iwc.getParameter(PARAM_DESC);
 			String desc2 = iwc.getParameter(PARAM_DESC2);
 			String qty = iwc.getParameter(PARAM_QUANTITY);
 			String str = iwc.getParameter(PARAM_STRENGTH);
 			String prod = iwc.getParameter(PARAM_PRODUCER);
 			String ctry = iwc.getParameter(PARAM_COUNTRY);
 			String bar = iwc.getParameter(PARAM_BAR_CODE);
 			String amount = iwc.getParameter(PARAM_AMOUNT);
 			String cat = iwc.getParameter(PARAM_FORM_CATEGORY);
 			String sub = iwc.getParameter(PARAM_SUB_CATEGORY);
 			String weight = iwc.getParameter(PARAM_WEIGHT);
 			String monoxide = iwc.getParameter(PARAM_MONOXIDE);
 			
 			appl.setApplicationType(_type);
 			appl.setDescription(desc);
 			appl.setDescription2(desc2);
 			appl.setQuantity(qty);
 			appl.setStrength(str);
 			appl.setProducer(prod);
 			appl.setCountryOfOrigin(ctry);
 			appl.setBarCode(bar);
 			appl.setAmount(amount);
 			appl.setWeigth(weight);
 //			appl.setProductCategoryId();
 			appl.setSupplierId(iwc.getUserId());
 			appl.setApplicationSent(IWTimestamp.getTimestampRightNow());
			if (monoxide != null)
				appl.setCarbonMonoxide(Float.parseFloat(monoxide));
 			
 			getApplicationBusiness(iwc).insertApplication(appl);
 			
 			add("Umskn send");
 			
 			return;
 		}
 		catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		add("Villa vi sendingu umsknar");
 	}
 
 	private void showForm(IWContext iwc) {
 		int typeId = parseType(_type);
 
 		Form form = new Form();
 
 		if (typeId == TYPE_TRIAL || typeId == TYPE_SPECIAL || typeId == TYPE_MONTH) {
 			Table t = new Table(2, 12);
 			t.add("Lsing", 1, 1);
 			t.add("Lsing 2", 1, 2);
 			t.add("Milliltrar", 1, 3);
 			t.add("Vnstyrkur", 1, 4);
 			t.add("Framleiandi", 1, 5);
 			t.add("Upprunaland", 1, 6);
 			t.add("Strikamerki", 1, 7);
 			t.add("Vruflokkur", 1, 8);
 			t.add("Flokksdeild", 1, 9);
 			t.add("Flskur pr. ks.", 1, 10);
 
 			TextInput desc = new TextInput(PARAM_DESC);
 			TextInput desc2 = new TextInput(PARAM_DESC2);
 			TextInput qty = new TextInput(PARAM_QUANTITY);
 			TextInput str = new TextInput(PARAM_STRENGTH);
 			TextInput prod = new TextInput(PARAM_PRODUCER);
 			TextInput ctry = new TextInput(PARAM_COUNTRY);
 			TextInput bar = new TextInput(PARAM_BAR_CODE);
 			TextInput bottles = new TextInput(PARAM_AMOUNT);
 
 			t.add(desc, 2, 1);
 			t.add(desc2, 2, 2);
 			t.add(qty, 2, 3);
 			t.add(str, 2, 4);
 			t.add(prod, 2, 5);
 			t.add(ctry, 2, 6);
 			t.add(bar, 2, 7);
 			t.add(bottles, 2, 10);
 
 			int selectedCategory = 0;
 			String selected = iwc.getParameter(PARAM_FORM_CATEGORY);
 			if (selected != null) {
 				selectedCategory = Integer.parseInt(selected);
 			}
 
 			DropdownMenu category = (DropdownMenu) getCategoryDropdown(iwc, selectedCategory);
 //			category.setToSubmit();
 			t.add(category, 2, 8);
 			t.add(getSubCategoryDropdown(iwc, selectedCategory), 2, 9);
 
 			SubmitButton submit = new SubmitButton(PARAM_FORM_SUBMIT, "Senda");
 			submit.setAsImageButton(true);
 			t.setAlignment(2, 12, "Right");
 			t.add(submit, 2, 12);
 
 			form.add(t);
 		}
 		else if (typeId == TYPE_TOBACCO) {
 			Table t = new Table(2, 10);
 			t.add("Lsing", 1, 1);
 			t.add("Vruflokkur", 1, 2);
 			t.add("Flokksdeild", 1, 3);
 			t.add("Framleiandi", 1, 4);
 			t.add("Upprunaland", 1, 5);
 			t.add("Tjrumagn", 1, 6);
 			t.add("yngd tbaks", 1, 7);
 			t.add("Magn kolmnoxs",1,8);
 
 			TextInput desc = new TextInput(PARAM_DESC);
 			TextInput prod = new TextInput(PARAM_PRODUCER);
 			TextInput ctry = new TextInput(PARAM_COUNTRY);
 			TextInput amount = new TextInput(PARAM_AMOUNT);
 			TextInput weight = new TextInput(PARAM_WEIGHT);
 			TextInput monoxide = new TextInput(PARAM_MONOXIDE);
 			monoxide.setAsFloat();
 
 			t.add(desc, 2, 1);
 			t.add(prod, 2, 4);
 			t.add(ctry, 2, 5);
 			t.add(amount, 2, 6);
 			t.add(weight, 2, 7);
 			t.add(monoxide,2,8);
 
 			int selectedCategory = 0;
 			String selected = iwc.getParameter(PARAM_FORM_CATEGORY);
 			if (selected != null) {
 				selectedCategory = Integer.parseInt(selected);
 			}
 
 			DropdownMenu category = (DropdownMenu) getCategoryDropdown(iwc, selectedCategory);
 //			category.setToSubmit();
 			t.add(category, 2, 2);
 			t.add(getSubCategoryDropdown(iwc, selectedCategory), 2, 3);
 
 			SubmitButton submit = new SubmitButton(PARAM_FORM_SUBMIT, "Senda");
 			submit.setAsImageButton(true);
 			t.setAlignment(2, 10, "Right");
 			t.add(submit, 2, 10);
 
 			form.add(t);
 		}
 
 		add(form);
 	}
 
 	private int parseType(String type) {
 		if (type == null)
 			return TYPE_TRIAL;
 
 		return Integer.parseInt(type);
 	}
 
 	public void main(IWContext iwc) {
 		control(iwc);
 	}
 
 	private PresentationObject getTypeDropdown(IWContext iwc, int selected) {
 		DropdownMenu menu = new DropdownMenu(PARAM_FORM_TYPE);
 		menu.addMenuElement(TYPE_TRIAL, "Reynsla");
 		menu.addMenuElement(TYPE_SPECIAL, "Srlisti");
 		menu.addMenuElement(TYPE_MONTH, "Mnaarfl.");
 		menu.addMenuElement(TYPE_TOBACCO, "Tbak");
 
 		menu.setSelectedElement(selected);
 
 		return menu;
 	}
 
 	private PresentationObject getCategoryDropdown(IWContext iwc, int selected) {
 		DropdownMenu menu = new DropdownMenu(PARAM_FORM_CATEGORY);
 		menu.addMenuElement(0, "(01) Rauvn");
 		menu.addMenuElement(1, "(02) Hvtvn");
 		menu.addMenuElement(2, "(03) Hver veit");
 		menu.addMenuElement(3, "(33) Rettur");
 
 		menu.setSelectedElement(selected);
 
 		return menu;
 	}
 
 	private PresentationObject getSubCategoryDropdown(IWContext iwc, int category) {
 		DropdownMenu menu = new DropdownMenu(PARAM_SUB_CATEGORY);
 		menu.addMenuElement(0, "Frakkland");
 		menu.addMenuElement(1, "England");
 		menu.addMenuElement(2, "Svj");
 		menu.addMenuElement(3, "Finnland");
 
 		return menu;
 	}
 	
 	private NewProductApplicationBusiness getApplicationBusiness(IWContext iwc) throws Exception {
 		return (NewProductApplicationBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, NewProductApplicationBusiness.class);
 	}	
 	
 	public void setApplicationType(String type) {
 		_type = type;	
 	}
 }
