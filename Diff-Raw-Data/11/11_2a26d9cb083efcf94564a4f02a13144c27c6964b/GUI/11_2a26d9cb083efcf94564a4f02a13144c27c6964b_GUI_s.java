 /*
  * Copyright (C) 2010 The Halal Certification Project 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package com.mui.certificate.gui;
 import com.cloudgarden.resource.SWTResourceManager;
 
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class GUI extends org.eclipse.swt.widgets.Composite {
 
 	{
 		//Register as a resource user - SWTResourceManager will
 		//handle the obtaining and disposing of resources
 		SWTResourceManager.registerResourceUser(this);
 	}
 	
 	private Menu menuBar;
 	private Group details;
 	private MenuItem menuItemCertificateAdd;
 	private ToolItem toolItem4;
 	private ToolItem toolItem3;
 	private ToolItem toolItem2;
 	private ToolItem toolItem1;
 	private ToolBar toolBar;
 	private MenuItem menuItemHelpManual;
 	private MenuItem menuItemHelpAbout;
 	private Menu menu4;
 	private MenuItem menuItemFile;
 	private MenuItem menuItemCertificate;
 	private MenuItem menuItemHelp;
 	private MenuItem menuItem1;
 	private MenuItem menuItemCertificateSearch;
 	private Menu menu3;
 	private MenuItem menuItemSettingChangePasswd;
 	private Menu menu2;
 	private Text text6;
 	private Text text5;
 	private Text text4;
 	private CLabel cLabel25;
 	private CLabel cLabel24;
 	private CLabel cLabel23;
 	private CLabel cLabel22;
 	private Button buttonCancel;
 	private Button buttonSubmit;
 	private Group group6;
 	private CLabel cLabel21;
 	private CLabel cLabel20;
 	private CLabel cLabel19;
 	private CLabel cLabel18;
 	private CLabel cLabel17;
 	private Group group5;
 	private Canvas canvas2;
 	private Canvas canvas1;
 	private Group group4;
 	private CLabel cLabel16;
 	private CLabel cLabel15;
 	private CLabel cLabel14;
 	private CCombo cCombo6;
 	private CLabel cLabel13;
 	private CCombo cCombo5;
 	private CLabel cLabel12;
 	private CCombo cCombo4;
 	private CLabel cLabel11;
 	private Text textNomorCertificate;
 	private CCombo cCombo3;
 	private CCombo cCombo2;
 	private CLabel cLabel10;
 	private CCombo cCombo1;
 	private CLabel cLabel9;
 	private CLabel cLabel8;
 	private Text text3;
 	private Text text2;
 	private Text text1;
 	private CCombo cComboJenisProduk;
 	private CLabel cLabel7;
 	private CLabel cLabel6;
 	private Group group3;
 	private CLabel cLabel5;
 	private Group group2;
 	private Group group1;
 	private CLabel cLabel4;
 	private CLabel cLabel3;
 	private CLabel cLabel2;
 	private CLabel cLabel1;
 	private CTabItem cTabItem1;
 	private CTabFolder cTabFolder1;
 	private MenuItem menuItemSetting;
 	private MenuItem menuItemFileExit;
 	private MenuItem menuItemFileOpen;
 	private Menu menu1;
 
 	/**
 	* Auto-generated main method to display this 
 	* org.eclipse.swt.widgets.Composite inside a new Shell.
 	*/
 	public static void main(String[] args) {
 		showGUI();
 	}
 	
 	/**
 	* Overriding checkSubclass allows this class to extend org.eclipse.swt.widgets.Composite
 	*/	
 	protected void checkSubclass() {
 	}
 	
 	/**
 	* Auto-generated method to display this 
 	* org.eclipse.swt.widgets.Composite inside a new Shell.
 	*/
 	public static void showGUI() {
 		Display display = Display.getDefault();
 		Shell shell = new Shell(display);
 		GUI inst = new GUI(shell, SWT.NULL);
 		Point size = inst.getSize();
 		shell.setLayout(new FillLayout());
 		shell.layout();
 		if(size.x == 0 && size.y == 0) {
 			inst.pack();
 			shell.pack();
 		} else {
 			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
 			shell.setSize(shellBounds.width, shellBounds.height);
 		}
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 	}
 
 	public GUI(org.eclipse.swt.widgets.Composite parent, int style) {
 		super(parent, style);
 		initGUI();
 	}
 
 	private void initGUI() {
 		try {
 			FormLayout thisLayout = new FormLayout();
 			this.setLayout(thisLayout);
 			this.setSize(695, 639);
 			{
 				FormData cLabel15LData = new FormData();
 				cLabel15LData.left =  new FormAttachment(0, 1000, 173);
 				cLabel15LData.top =  new FormAttachment(0, 1000, 119);
 				cLabel15LData.width = 6;
 				cLabel15LData.height = 19;
 				cLabel15 = new CLabel(this, SWT.NONE);
 				cLabel15.setLayoutData(cLabel15LData);
 			}
 			{
 				cTabFolder1 = new CTabFolder(this, SWT.NONE);
 				{
 					cTabItem1 = new CTabItem(cTabFolder1, SWT.NONE);
 					cTabItem1.setText("Certificate Info");
 					{
 						details = new Group(cTabFolder1, SWT.NONE);
 						cTabItem1.setControl(details);
 						{
 							group1 = new Group(details, SWT.NONE);
 							group1.setText("Details");
 							group1.setBounds(12, 116, 435, 185);
 							{
 								cLabel2 = new CLabel(group1, SWT.NONE);
 								cLabel2.setText("Jenis Produk");
 								cLabel2.setBounds(5, 13, 122, 30);
 							}
 							{
 								cLabel3 = new CLabel(group1, SWT.NONE);
 								cLabel3.setText("Nama Produk");
 								cLabel3.setBounds(5, 44, 96, 30);
 							}
 							{
 								cLabel4 = new CLabel(group1, SWT.NONE);
 								cLabel4.setText("Nama Perusahaan");
 								cLabel4.setBounds(6, 86, 140, 30);
 							}
 							{
 								cLabel5 = new CLabel(group1, SWT.NONE);
 								cLabel5.setText("Alamat Perusahaan");
 								cLabel5.setBounds(5, 129, 147, 30);
 							}
 							{
 								cComboJenisProduk = new CCombo(group1, SWT.BORDER);
 								cComboJenisProduk.setText("Makanan");
 								cComboJenisProduk.setBounds(152, 16, 205, 19);
 								cComboJenisProduk.setItems(new java.lang.String[] {"Makanan","Minuman","Obat-obatan","Snack","Daging"});
 							}
 							{
 								text1 = new Text(group1, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
 								text1.setBounds(152, 129, 271, 40);
 							}
 							{
 								text2 = new Text(group1, SWT.BORDER);
 								text2.setBounds(152, 91, 271, 21);
 							}
 							{
 								text3 = new Text(group1, SWT.BORDER);
 								text3.setBounds(152, 50, 271, 21);
 							}
 						}
 						{
 							group2 = new Group(details, SWT.NONE);
 							group2.setText("Certificate Details");
 							group2.setBounds(12, 12, 435, 83);
 							{
 								cLabel1 = new CLabel(group2, SWT.NONE);
 								cLabel1.setText("Nomor Sertifikat");
 								cLabel1.setBounds(6, 14, 127, 30);
 							}
 							{
 								textNomorCertificate = new Text(group2, SWT.MULTI | SWT.WRAP | SWT.BORDER);
 								textNomorCertificate.setText("00-00000-00");
 								textNomorCertificate.setBounds(154, 18, 101, 20);
 							}
 							{
 								cLabel14 = new CLabel(group2, SWT.NONE);
 								cLabel14.setText("URL  Link");
 								cLabel14.setBounds(7, 43, 60, 30);
 							}
 							{
 								cLabel16 = new CLabel(group2, SWT.BORDER);
 								cLabel16.setBounds(154, 50, 269, 21);
 							}
 						}
 						{
 							group3 = new Group(details, SWT.NONE);
 							group3.setText("Date");
 							group3.setBounds(12, 312, 435, 87);
 							{
 								cLabel6 = new CLabel(group3, SWT.NONE);
 								cLabel6.setText("Tanggal dikeluarkan");
 								cLabel6.setBounds(6, 14, 131, 30);
 							}
 							{
 								cLabel7 = new CLabel(group3, SWT.NONE);
 								cLabel7.setText("Berlaku sampai dengan");
 								cLabel7.setBounds(6, 45, 122, 30);
 							}
 							{
 								cLabel8 = new CLabel(group3, SWT.NONE);
 								cLabel8.setText("Tahun");
 								cLabel8.setBounds(326, 15, 33, 30);
 							}
 							{
 								cLabel9 = new CLabel(group3, SWT.NONE);
 								cLabel9.setText("tgl");
 								cLabel9.setBounds(152, 14, 16, 30);
 							}
 							{
 								cCombo1 = new CCombo(group3, SWT.BORDER);
 								cCombo1.setText("01");
 								cCombo1.setBounds(178, 20, 38, 19);
 								cCombo1.setItems(new java.lang.String[] {"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"});
 							}
 							{
 								cLabel10 = new CLabel(group3, SWT.NONE);
 								cLabel10.setText("bln");
 								cLabel10.setBounds(233, 20, 19, 19);
 							}
 							{
 								cCombo2 = new CCombo(group3, SWT.BORDER);
 								cCombo2.setText("Jan");
 								cCombo2.setBounds(259, 20, 61, 19);
 								cCombo2.setItems(new java.lang.String[] {"Jan","Feb","Mar","Aprl","Mei","Jun","Jul","Agust","Sep","Oct","Nov","Des"});
 							}
 							{
 								cCombo3 = new CCombo(group3, SWT.BORDER);
 								cCombo3.setText("1990");
 								cCombo3.setBounds(370, 20, 51, 19);
 								cCombo3.setItems(new java.lang.String[] {"2000","2001","2002","2003","2004","2005","2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016","2017","2018","2019","2020"});
 							}
 							{
 								cLabel11 = new CLabel(group3, SWT.NONE);
 								cLabel11.setText("tgl");
 								cLabel11.setBounds(152, 44, 16, 30);
 							}
 							{
 								cCombo4 = new CCombo(group3, SWT.BORDER);
 								cCombo4.setText("01");
 								cCombo4.setBounds(177, 51, 39, 17);
 								cCombo4.setItems(new java.lang.String[] {"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"});
 							}
 							{
 								cLabel12 = new CLabel(group3, SWT.NONE);
 								cLabel12.setText("bln");
 								cLabel12.setBounds(233, 50, 19, 19);
 							}
 							{
 								cCombo5 = new CCombo(group3, SWT.BORDER);
 								cCombo5.setText("Jan");
 								cCombo5.setBounds(258, 50, 62, 18);
 								cCombo5.setItems(new java.lang.String[] {"Jan","Feb","Mar","Aprl","Mei","Jun","Jul","Agust","Sep","Oct","Nov","Des"});
 							}
 							{
 								cLabel13 = new CLabel(group3, SWT.NONE);
 								cLabel13.setText("Tahun");
 								cLabel13.setBounds(326, 49, 33, 18);
 							}
 							{
 								cCombo6 = new CCombo(group3, SWT.BORDER);
 								cCombo6.setText("1990");
 								cCombo6.setBounds(370, 49, 50, 18);
 								cCombo6.setItems(new java.lang.String[] {"2000","2001","2002","2003","2004","2005","2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016","2017","2018","2019","2020"});
 							}
 						}
 						{
 							group4 = new Group(details, SWT.NONE);
 							group4.setText("QR Code Image");
 							group4.setBounds(475, 13, 188, 230);
 							{
 								canvas1 = new Canvas(group4, SWT.NONE);
 								canvas1.setBounds(12, 33, 164, 185);
 								{
 									canvas2 = new Canvas(canvas1, SWT.BORDER);
 									canvas2.setBounds(-5, 0, 174, 187);
 								}
 							}
 						}
 						{
 							group5 = new Group(details, SWT.NONE);
 							group5.setText("Additional Information");
 							group5.setBounds(475, 271, 187, 128);
 							{
 								cLabel17 = new CLabel(group5, SWT.NONE);
 								cLabel17.setText("This Certificate was generated by :");
 								cLabel17.setBounds(5, 18, 180, 30);
 							}
 							{
 								cLabel18 = new CLabel(group5, SWT.NONE);
 								cLabel18.setText("ID User");
 								cLabel18.setBounds(7, 47, 60, 21);
 							}
 							{
 								cLabel19 = new CLabel(group5, SWT.BORDER);
 								cLabel19.setBounds(79, 50, 96, 19);
 							}
 							{
 								cLabel20 = new CLabel(group5, SWT.NONE);
 								cLabel20.setText("Details");
 								cLabel20.setBounds(7, 70, 60, 30);
 							}
 							{
 								cLabel21 = new CLabel(group5, SWT.BORDER);
 								cLabel21.setBounds(79, 81, 96, 35);
 							}
 						}
 						{
 							group6 = new Group(details, SWT.NONE);
 							group6.setText("MUI Board of Director");
 							group6.setBounds(12, 420, 547, 140);
 							{
 								cLabel22 = new CLabel(group6, SWT.NONE);
 								cLabel22.setText("Ketua Komisi Fatwa MUI");
 								cLabel22.setBounds(6, 21, 200, 19);
 							}
 							{
 								cLabel23 = new CLabel(group6, SWT.NONE);
 								cLabel23.setText("Direktur Lembaga Pengkajian Pangan,");
 								cLabel23.setBounds(6, 48, 191, 20);
 							}
 							{
 								cLabel24 = new CLabel(group6, SWT.NONE);
 								cLabel24.setText("Obat-obatan, dan Kosmetika");
 								cLabel24.setBounds(6, 58, 156, 30);
 							}
 							{
 								cLabel25 = new CLabel(group6, SWT.NONE);
 								cLabel25.setText("Ketua Umum MUI");
 								cLabel25.setBounds(6, 86, 156, 30);
 							}
 							{
 								text4 = new Text(group6, SWT.MULTI | SWT.WRAP | SWT.BORDER);
 								text4.setBounds(218, 21, 309, 19);
 							}
 							{
 								text5 = new Text(group6, SWT.MULTI | SWT.WRAP | SWT.BORDER);
 								text5.setBounds(218, 55, 309, 19);
 							}
 							{
 								text6 = new Text(group6, SWT.MULTI | SWT.WRAP | SWT.BORDER);
 								text6.setBounds(218, 90, 309, 20);
 							}
 						}
 						{
 							buttonSubmit = new Button(details, SWT.PUSH | SWT.CENTER);
 							buttonSubmit.setText("Submit");
 							buttonSubmit.setBounds(588, 452, 60, 30);
 						}
 						{
 							buttonCancel = new Button(details, SWT.PUSH | SWT.CENTER);
 							buttonCancel.setText("Cancel");
 							buttonCancel.setBounds(588, 494, 60, 30);
 						}
 					}
 				}
 				FormData cTabFolder1LData = new FormData();
 				cTabFolder1LData.left =  new FormAttachment(0, 1000, 5);
 				cTabFolder1LData.top =  new FormAttachment(0, 1000, 36);
 				cTabFolder1LData.width = 674;
 				cTabFolder1LData.height = 574;
 				cTabFolder1.setLayoutData(cTabFolder1LData);
 				cTabFolder1.setSelection(0);
 			}
 			{
 				FormData toolBarLData = new FormData();
 				toolBarLData.left =  new FormAttachment(0, 1000, 0);
 				toolBarLData.top =  new FormAttachment(0, 1000, 0);
 				toolBarLData.width = 259;
 				toolBarLData.height = 30;
 				toolBar = new ToolBar(this, SWT.HORIZONTAL);
 				toolBar.setLayoutData(toolBarLData);
 				{
 					toolItem1 = new ToolItem(toolBar, SWT.PUSH);
					toolItem1.setImage(SWTResourceManager.getImage("home.jpg"));
 					toolItem1.setToolTipText("Home Directory");
 				}
 				{
 					toolItem2 = new ToolItem(toolBar, SWT.PUSH);
					toolItem2.setImage(SWTResourceManager.getImage("hide.jpg"));
 					toolItem2.setToolTipText("Hide");
 				}
 				{
 					toolItem3 = new ToolItem(toolBar, SWT.PUSH);
					toolItem3.setImage(SWTResourceManager.getImage("print.jpg"));
 					toolItem3.setToolTipText("Printing Certificate");
 				}
 				{
 					toolItem4 = new ToolItem(toolBar, SWT.NONE);
					toolItem4.setImage(SWTResourceManager.getImage("pdf.jpg"));
 					toolItem4.setToolTipText("Export Directly to PDF");
 				}
 			}
 			{
 				menuBar = new Menu(getShell(), SWT.BAR);
 				getShell().setMenuBar(menuBar);
 				{
 					menuItemFile = new MenuItem(menuBar, SWT.CASCADE);
 					menuItemFile.setText("File");
 					{
 						menu1 = new Menu(menuBar);
 						menuItemFile.setMenu(menu1);
 						{
 							menuItemFileOpen = new MenuItem(menu1, 0);
 							menuItemFileOpen.setText("Open");
 						}
 						{
 							menuItemFileExit = new MenuItem(menu1, 0);
 							menuItemFileExit.setText("Exit");
 						}
 					}
 				}
 				{
 					menuItemSetting = new MenuItem(menuBar, SWT.CASCADE);
 					menuItemSetting.setText("User Setting");
 					{
 						menu2 = new Menu(menuBar);
 						menuItemSetting.setMenu(menu2);
 						{
 							menuItemSettingChangePasswd = new MenuItem(menu2, 0);
 							menuItemSettingChangePasswd.setText("Change Password");
 						}
 					}
 				}
 				{
 					menuItemCertificate = new MenuItem(menuBar, SWT.CASCADE);
 					menuItemCertificate.setText("Certificate");
 					{
 						menu3 = new Menu(menuBar);
 						menuItemCertificate.setMenu(menu3);
 						{
 							menuItemCertificateSearch = new MenuItem(menu3, 0);
 							menuItemCertificateSearch.setText("Search");
 						}
 						{
 							menuItem1 = new MenuItem(menu3, 0);
 							menuItem1.setText("Remove");
 						}
 						{
 							menuItemCertificateAdd = new MenuItem(menu3, 0);
 							menuItemCertificateAdd.setText("Add New");
 						}
 					}
 				}
 				{
 					menuItemHelp = new MenuItem(menuBar, SWT.CASCADE);
 					menuItemHelp.setText("Help");
 					{
 						menu4 = new Menu(menuBar);
 						menuItemHelp.setMenu(menu4);
 						{
 							menuItemHelpAbout = new MenuItem(menu4, 0);
 							menuItemHelpAbout.setText("About");
 						}
 						{
 							menuItemHelpManual = new MenuItem(menu4, 0);
 							menuItemHelpManual.setText("Manual");
 						}
 					}
 				}
 			}
 			this.layout();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
