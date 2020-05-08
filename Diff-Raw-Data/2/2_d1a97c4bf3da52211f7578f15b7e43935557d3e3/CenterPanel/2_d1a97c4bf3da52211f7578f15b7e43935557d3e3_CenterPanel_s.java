 /*
   Copyright (c) 2009-2012 Malte Mader <mader@zbh.uni-hamburg.de>
   Copyright (c) 2009-2012 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
 
 package de.unihamburg.zbh.fishoracle.client;
 
 import gwtupload.client.IUploadStatus.Status;
 import gwtupload.client.IUploader;
 import gwtupload.client.IUploader.OnFinishUploaderHandler;
 import gwtupload.client.IUploader.UploadedInfo;
 import gwtupload.client.MultiUploader;
 
 import java.util.LinkedHashMap;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 import com.smartgwt.client.data.Criteria;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.Cursor;
 import com.smartgwt.client.types.GroupStartOpen;
 import com.smartgwt.client.types.ListGridEditEvent;
 import com.smartgwt.client.types.MultipleAppearance;
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.types.SelectionStyle;
 import com.smartgwt.client.types.SelectionType;
 import com.smartgwt.client.types.Side;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.util.BooleanCallback;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.Img;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.Progressbar;
 import com.smartgwt.client.widgets.Window;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.events.CloseClickEvent;
 import com.smartgwt.client.widgets.events.MouseOverEvent;
 import com.smartgwt.client.widgets.events.MouseOverHandler;
 
 import com.smartgwt.client.widgets.events.ResizedEvent;
 import com.smartgwt.client.widgets.events.ResizedHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.ButtonItem;
 import com.smartgwt.client.widgets.form.fields.CheckboxItem;
 import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
 import com.smartgwt.client.widgets.form.fields.HeaderItem;
 import com.smartgwt.client.widgets.form.fields.LinkItem;
 import com.smartgwt.client.widgets.form.fields.PasswordItem;
 import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
 import com.smartgwt.client.widgets.form.fields.SelectItem;
 import com.smartgwt.client.widgets.form.fields.TextAreaItem;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 import com.smartgwt.client.widgets.form.fields.events.DataArrivedEvent;
 import com.smartgwt.client.widgets.form.fields.events.DataArrivedHandler;
 import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
 import com.smartgwt.client.widgets.form.validator.MatchesFieldValidator;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
 import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.menu.Menu;
 import com.smartgwt.client.widgets.menu.MenuItem;
 import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
 import com.smartgwt.client.widgets.tab.Tab;
 import com.smartgwt.client.widgets.tab.TabSet;
 import com.smartgwt.client.widgets.toolbar.ToolStrip;
 import com.smartgwt.client.widgets.toolbar.ToolStripButton;
 import com.smartgwt.client.widgets.toolbar.ToolStripMenuButton;
 
 import de.unihamburg.zbh.fishoracle.client.data.DBConfigData;
 import de.unihamburg.zbh.fishoracle.client.data.FoConfigData;
 import de.unihamburg.zbh.fishoracle.client.data.FoConstants;
 import de.unihamburg.zbh.fishoracle.client.data.FoSegment;
 import de.unihamburg.zbh.fishoracle.client.data.FoGroup;
 import de.unihamburg.zbh.fishoracle.client.data.FoProject;
 import de.unihamburg.zbh.fishoracle.client.data.FoStudy;
 import de.unihamburg.zbh.fishoracle.client.data.GWTImageInfo;
 import de.unihamburg.zbh.fishoracle.client.data.EnsemblGene;
 import de.unihamburg.zbh.fishoracle.client.data.QueryInfo;
 import de.unihamburg.zbh.fishoracle.client.data.RecMapInfo;
 import de.unihamburg.zbh.fishoracle.client.data.FoUser;
 import de.unihamburg.zbh.fishoracle.client.datasource.FeatureDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.FileImportDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.PlatformDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.SNPMutationDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.SegmentDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.EnsemblDBDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.StudyDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.OperationId;
 import de.unihamburg.zbh.fishoracle.client.datasource.OrganDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.ProjectAccessDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.ProjectDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.PropertyDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.TranslocationDS;
 import de.unihamburg.zbh.fishoracle.client.datasource.UserDS;
 import de.unihamburg.zbh.fishoracle.client.ImgCanvas;
 import de.unihamburg.zbh.fishoracle.client.rpc.Admin;
 import de.unihamburg.zbh.fishoracle.client.rpc.AdminAsync;
 import de.unihamburg.zbh.fishoracle.client.rpc.Search;
 import de.unihamburg.zbh.fishoracle.client.rpc.SearchAsync;
 import de.unihamburg.zbh.fishoracle.client.rpc.UserService;
 import de.unihamburg.zbh.fishoracle.client.rpc.UserServiceAsync;
 
 public class CenterPanel extends VLayout {
 
 	private ToolStripButton selectButton;
 	
 	private ListGrid userGrid;
 	private ListGrid organGrid;
 	private ListGrid propertyGrid;
 	private ListGrid studyGrid;
 	private ListGrid platformGrid;
 	private ListGrid groupGrid;
 	private ListGrid groupUserGrid;
 	private ListGrid dataTypeGrid;
 	private SelectItem userSelectItem;
 	private SelectItem projectSelectItem;
 	private ListGrid ensemblGrid;
 	
 	private DynamicForm userProfileForm;
 	private DynamicForm pwForm;
 	private DynamicForm userPwForm;
 	private TextItem userIdTextItem;
 	private TextItem userNameTextItem;
 	private TextItem userFirstNameTextItem;
 	private TextItem userLastNameTextItem;
 	private TextItem userEmailTextItem;
 	private PasswordItem userPwItem;
 	private PasswordItem userPwConfirmItem;
 	private PasswordItem newPwPasswordItem;
 	
 	private ListGrid projectGrid;
 	private ListGrid projectStudyGrid;
 	private TextItem projectNameTextItem;
 	private TextAreaItem projectDescriptionItem;
 	private ListGrid projectAccessGrid;
 	private SelectItem accessRightSelectItem;
 	private SelectItem groupSelectItem;
 	private SelectItem studySelectItem;
 	
 	private TextItem groupNameTextItem;
 	private TextItem organLabelTextItem;
 	private ComboBoxItem organTypeCbItem;
 	private TextItem propertyLabelTextItem;
 	private ComboBoxItem propertyTypeCbItem;
 	private TextItem platformLabelTextItem;
 	private ComboBoxItem platformTypeCbItem;
 	private TextItem dbNameTextItem;
 	private TextItem dbLabelTextItem;
 	private TextItem dbVersionTextItem;
 	
 	private TabSet centerTabSet = null;
 	private TextItem chrTextItem;
 	private TextItem startTextItem;
 	private TextItem endTextItem;
 	
 	private ListGrid fileGrid;
 	private RadioGroupItem createStudyItem;
 	private ComboBoxItem cbItemFilterType;
 	private SelectItem selectItemProjects;
 	private SelectItem selectItemTissues;
 	private SelectItem selectItemPlatform;
 	private SelectItem selectItemGenomeAssembly;
 	private CheckboxItem batchCheckbox;
 	
 	private TextItem ensemblHost;
     private TextItem ensemblPort;
     private TextItem ensemblDatabase;
     private TextItem ensemblUser;
     private TextItem ensemblPW;
     private TextItem fishoracleHost;
     private TextItem fishoracleDatabase;
     private TextItem fishoracleUser;
     private TextItem fishoraclePW;
 	
     private Window window;
     
 	private MainPanel mp = null;
 	private CenterPanel cp = null;
 	
 	public CenterPanel(MainPanel mainPanel) {
 		
 		mp = mainPanel;
 		cp = this;
 		
 		centerTabSet = new TabSet();
 		centerTabSet.setTabBarPosition(Side.TOP);
 		centerTabSet.setTabBarAlign(Side.LEFT);
 		
 		Tab welcomeTab = new Tab("Welcome"); 
 		VLayout welcomeLayout = new VLayout();
 		welcomeLayout.setContents("" +
         		"<center><h1>FISH Oracle</h1></center>" +
         		"<p id=\"welcome\">You can search for genomic regions" +
         		" or for a gene specified by a gene name e.g. 'kras'" +
         		" or a karyoband." +
         		" By clicking on an element a window opens that shows additional information." +
         		"</p><p id=\"welcome\"><br>FISH Oracle uses:<br> " +
         		"<ul id=\"welcome\">" +
         		"<li> the Google Web Toolkit <a href=\"http://code.google.com/webtoolkit/\" target=_blank>http://code.google.com/webtoolkit/</a></li>" +
         		"<li> the SmartGWT <a href=\"http://code.google.com/p/smartgwt/\" target=_blank>http://code.google.com/p/smartgwt/</a></li>" +
         		"<li> the Ensembl human core database <a href=\"http://www.ensembl.org\" target=_blank>http://www.ensembl.org</a></li>" +
         		"<li> AnnotationSketch of the GenomeTools <a href=\"http://www.genometools.org\" target=_blank>http://www.genometools.org</a></li>" +
         		"</ul></p>");
 		
 		welcomeTab.setPane(welcomeLayout);
 		
 		centerTabSet.addTab(welcomeTab);
 		
 		this.addResizedHandler(new ImageFrameResizedHandler(cp));
 		
 		this.addMember(centerTabSet);
 	}
 
 	public TabSet getCenterTabSet() {
 		return centerTabSet;
 	}
 
 	public MainPanel getMainPanel() {
 		return mp;
 	}
 	
 	public ToolStripButton getSelectButtion() {
 		return selectButton;
 	}
 	
 	public void refreshRange(){
 		
 		GWTImageInfo imgInfo;
 		
 		ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 		imgInfo = imgLayer.getImageInfo();
 		
 		String newChr;
 	    int newStart;
 	    int newEnd;
 	    
 		newChr = chrTextItem.getDisplayValue();
 	    
 	    newStart = Integer.parseInt(startTextItem.getDisplayValue());
 	    
 	    newEnd = Integer.parseInt(endTextItem.getDisplayValue());
 	    
 	    if(newStart >= newEnd || newEnd - newStart <= 10){
 	    	
 	    	SC.say("The end value must at least be 10 base pairs greater than the start value!");
 	    	
 	    } else {
 	    
 	    	imgInfo.setChromosome(newChr);
 	    	
 	    	imgInfo.setStart(newStart);
 	    
 	    	imgInfo.setEnd(newEnd);
 	    	
 	    	cp.imageRedraw(imgInfo);
 	    }
 	}
 	
 	public ImgCanvas createImageLayer(GWTImageInfo imgInfo){
 		
 		ImgCanvas image = new ImgCanvas(imgInfo, this);
 		image.setOverflow(Overflow.HIDDEN);
         image.setWidth(imgInfo.getWidth());
         image.setHeight(imgInfo.getHeight());
         image.setAppImgDir("/");
 		
         image.addMouseOverHandler(new MouseOverHandler(){
 
         	@Override
         	public void onMouseOver(MouseOverEvent event) {
         		ImgCanvas image = (ImgCanvas) event.getSource();
 
         		if(!image.isSetChildren()){
 
         			int rmc;
 
         			GWTImageInfo imgInfo = image.getImageInfo();
 
         			for(rmc=0; rmc < imgInfo.getRecmapinfo().size(); rmc++){
         				
         				if((imgInfo.getRecmapinfo().get(rmc).getSoutheastX() - imgInfo.getRecmapinfo().get(rmc).getNorthwestX()) < 3 
         						&& !imgInfo.getRecmapinfo().get(rmc).getType().equals("gene")
         						&& !imgInfo.getRecmapinfo().get(rmc).getType().equals("translocation")
         						&& !imgInfo.getRecmapinfo().get(rmc).getType().equals("mutation")){
         					continue;
         				}
         				
         				final Img spaceImg = new Img("1pximg.gif");
 
         				spaceImg.addClickHandler(new RecMapClickHandler(imgInfo.getRecmapinfo().get(rmc), imgInfo, cp));
 
         				int southeast_x = (int) imgInfo.getRecmapinfo().get(rmc).getSoutheastX();
 
         				int northwest_x = (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestX();
 
         				if(southeast_x > imgInfo.getWidth()){
         					spaceImg.setWidth(imgInfo.getWidth() - northwest_x);
         				} else {
         					spaceImg.setWidth(southeast_x - northwest_x);
         				}
 
         				if(spaceImg.getWidth() <= 0){
         					if(!imgInfo.getRecmapinfo().get(rmc).getType().equals("translocation")  ){
         						spaceImg.setWidth(1);
             				} else {
             					spaceImg.setWidth(10);
             				}
         				}
         				if(imgInfo.getRecmapinfo().get(rmc).getType().equals("translocation")  ){
         					
         				}
 
         				int southeast_y = (int) imgInfo.getRecmapinfo().get(rmc).getSoutheastY();
 
         				int northwest_y = (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestY();
 
         				spaceImg.setHeight(southeast_y - northwest_y);
 
         				spaceImg.setLeft( (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestX());
 
         				spaceImg.setTop( (int) imgInfo.getRecmapinfo().get(rmc).getNorthwestY());
 
         				spaceImg.setCursor(Cursor.HAND);
 
         				image.addChild(spaceImg);
 
         			}
         			
         			image.setSetChildren(true);
         		}
 
         	}
 
         });
         
 		return image;
 	}
 	
 	public void newImageTab(final GWTImageInfo imgInfo){
 		
 		Tab imgTab = new Tab(imgInfo.getQuery().getQueryString() + " (" + imgInfo.getQuery().getConfig().getStrArray("ensemblDBLabel")[0] + ")"); 
 		imgTab.setCanClose(true);
 		
 		VLayout presentationLayer = new VLayout();
 		presentationLayer.setDefaultLayoutAlign(VerticalAlignment.TOP);
 		
 		/*Toolbar*/
 		ToolStrip presentationToolStrip = new ToolStrip();
 		
 		presentationToolStrip.setWidth100();
 		
 		/*scrolling to the left and the right on the chromosome*/
 		ToolStripButton scrollLeftButton = new ToolStripButton();
 		scrollLeftButton.setTooltip("Scroll left");
 		scrollLeftButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 				GWTImageInfo imgInfo = imgLayer.getImageInfo();
 				
 				int range;
 				int percRange;
 				int perc = 10;
 				int newStart;
 				int newEnd;
 		
 				range = imgInfo.getEnd() - imgInfo.getStart(); 
 		
 				percRange = range * perc / 100;
 	    
 				newStart = imgInfo.getStart() - percRange;
 	    
 				newEnd = imgInfo.getEnd() - percRange;
 	    
 				if(newStart > 0){
 	    
 					imgInfo.setStart(newStart);
 	    
 					imgInfo.setEnd(newEnd);
 		
 					cp.imageRedraw(imgInfo);
 	    	
 				} else {
 					SC.say("You have reached the chromsomes end ...");
 				}
 				
 			}
 			
 		});
 		scrollLeftButton.setIcon("[APP]/icons/arrow_left.png");
 		scrollLeftButton.setAppImgDir("/");
 		presentationToolStrip.addButton(scrollLeftButton);
 		
 		Label scrollLabel = new Label("Scroll");
 		scrollLabel.setWidth(20);
 		presentationToolStrip.addMember(scrollLabel);
 		
 		ToolStripButton scrollRightButton = new ToolStripButton();
 		scrollRightButton.setTooltip("Scroll right");
 		scrollRightButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 				GWTImageInfo imgInfo = imgLayer.getImageInfo();
 				
 				int range;
     			int percRange;
     			int perc = 10;
     			int newStart;
     			int newEnd;
     			
     			range = imgInfo.getEnd() - imgInfo.getStart(); 
     			
     		    percRange = range * perc / 100;
     			
     		    
     		    newStart = imgInfo.getStart() + percRange;
     		    
     		    newEnd = imgInfo.getEnd() + percRange;
     		    
     		    imgInfo.setStart(newStart);
     		    
     		    imgInfo.setEnd(newEnd);
     		    
     		    cp.imageRedraw(imgInfo);
 			}
 		});
 		
 		scrollRightButton.setIcon("[APP]/icons/arrow_right.png");
 		scrollRightButton.setAppImgDir("/");
 		presentationToolStrip.addButton(scrollRightButton);
 		presentationToolStrip.addSeparator();
 		
 		/*zoomin in and out*/
 		ToolStripButton zoomInButton = new ToolStripButton();
 		zoomInButton.setTooltip("Zoom in");
 		zoomInButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 				GWTImageInfo imgInfo = imgLayer.getImageInfo();
 				
 				int range;
     			int percRange;
     			int perc = 10;
     			int newStart;
     			int newEnd;
     			
     			range = imgInfo.getEnd() - imgInfo.getStart(); 
     			
     		    percRange = range * perc / 100;
     			
     		    
     		    newStart = imgInfo.getStart() + percRange;
     		    
     		    newEnd = imgInfo.getEnd() - percRange;
     		    
     		    if(newEnd - newStart > 10){
     		    
     		    	imgInfo.setStart(newStart);
     		    
     		    	imgInfo.setEnd(newEnd);
     			
     		    	cp.imageRedraw(imgInfo);
     		    	
     		    } else {
     		    	SC.say("You have reached the highest zoom level ...");
     		    }
 			}
 		});
 		
 		zoomInButton.setIcon("[APP]/icons/zoom_in.png");
 		zoomInButton.setAppImgDir("/");
 		presentationToolStrip.addButton(zoomInButton);
 		
 		Label zoomLabel = new Label("Zoom");
 		zoomLabel.setWidth(20);
 		presentationToolStrip.addMember(zoomLabel);
 		
 		ToolStripButton zoomOutButton = new ToolStripButton();
 		zoomOutButton.setTooltip("Zoom out");
 		zoomOutButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 				GWTImageInfo imgInfo = imgLayer.getImageInfo();
 				
 				int range;
     			int percRange;
     			int perc = 10;
     			int newStart;
     			int newEnd;
     			
     			range = imgInfo.getEnd() - imgInfo.getStart(); 
     			
     		    percRange = range * perc / 100;
     			
     		    
     		    newStart = imgInfo.getStart() - percRange;
     		    
     		    newEnd = imgInfo.getEnd() + percRange;
     		    
     		    
     		    if(newStart < 0){
     		    	
     		    	newEnd = newEnd - newStart;
     		    	newStart = 0;
     		    }
     		    
     		    imgInfo.setStart(newStart);
     		    
     		    imgInfo.setEnd(newEnd);
     			
     		    cp.imageRedraw(imgInfo);
 			}
 		});
 		zoomOutButton.setIcon("[APP]/icons/zoom_out.png");
 		zoomOutButton.setAppImgDir("/");
 		presentationToolStrip.addButton(zoomOutButton);
 		
 		presentationToolStrip.addSeparator();
 		
 		/*display exact chromosome range*/
 		
 		chrTextItem = new TextItem();
 		chrTextItem.setTitle("Chr");
 		chrTextItem.setTooltip("Set the chromsome");
 		chrTextItem.setWidth(25);
 		chrTextItem.setValue(imgInfo.getChromosome());
 		chrTextItem.addKeyPressHandler(new KeyPressHandler(){
 
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				if(event.getKeyName().equals("Enter")){
 					refreshRange();
 				}
 			}
 		});
 		presentationToolStrip.addFormItem(chrTextItem);
 		
 		startTextItem = new TextItem();
 		startTextItem.setTitle("Start");
 		startTextItem.setTooltip("Set start position");
 		startTextItem.setWidth(70);
 		startTextItem.setValue(imgInfo.getStart());
 		startTextItem.addKeyPressHandler(new KeyPressHandler(){
 
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				if(event.getKeyName().equals("Enter")){
 					refreshRange();
 				}
 			}
 		});
 		presentationToolStrip.addFormItem(startTextItem);
 		
 		endTextItem = new TextItem();
 		endTextItem.setTitle("End");
 		endTextItem.setTooltip("Set end position");
 		endTextItem.setWidth(70);
 		endTextItem.setValue(imgInfo.getEnd());
 		
 		endTextItem.addKeyPressHandler(new KeyPressHandler(){
 
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				if(event.getKeyName().equals("Enter")){
 					refreshRange();
 				}
 			}
 		});
 		presentationToolStrip.addFormItem(endTextItem);
 		
 		presentationToolStrip.addSeparator();
 		
 		ToolStripButton configButton = new ToolStripButton();
 		configButton.setTitle("Configure");
 		configButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 				GWTImageInfo imgInfo = imgLayer.getImageInfo();
 				
 				Window w = new Window();
 				w.setTitle("Configuration");
 				//w.setAutoCenter(true);
 				w.setLeft(500);
 				w.setWidth(300);
 				w.setHeight(cp.getInnerHeight() - 300);
 				w.setIsModal(true);
 				w.setShowModalMask(true);
 				ConfigLayout cl = new ConfigLayout(mp, false);
 				cl.setImgInfo(imgInfo);
 				cl.setWin(w);
 				cl.loadConfig(imgInfo.getQuery().getConfig(), false);
 				cl.getSearchTextItem().setValue(imgInfo.getQuery().getQueryString());
 				cl.getSearchRadioGroupItem().setValue(imgInfo.getQuery().getSearchType());
 				if(imgInfo.getQuery().getSearchType().equals("Region")){
 					cl.getChrTextItem().setValue(chrTextItem.getDisplayValue());
 					cl.getStartTextItem().setValue(startTextItem.getDisplayValue());
 					cl.getEndTextItem().setValue(endTextItem.getDisplayValue());
 					cl.getSearchTextItem().hide();
 					cl.getChrTextItem().show();
 					cl.getStartTextItem().show();
 					cl.getEndTextItem().show();
 				}
 				w.addItem(cl);
 				w.show();
 			}
 		});
 		
 		presentationToolStrip.addButton(configButton);
 		
 		ToolStripButton refreshButton = new ToolStripButton();
 		refreshButton.setTooltip("Refresh image");
 		refreshButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				refreshRange();
 			}
 		});
 		refreshButton.setIcon("[APP]/icons/arrow_refresh.png");
 		refreshButton.setAppImgDir("/");
 		presentationToolStrip.addButton(refreshButton);
 		
 		presentationToolStrip.addSeparator();
 		
 		/*menu for more actions*/
 		Menu exportMenu = new Menu();
 		
 		MenuItem pdfExportItem = new MenuItem("Export image as pdf document");
 		MenuItem psExportItem = new MenuItem("Export image as ps document");
 		MenuItem svgExportItem = new MenuItem("Export image as svg document");
 		MenuItem pngExportItem = new MenuItem("Export image as png document");
 		
 		pdfExportItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 
 			public void onClick(MenuItemClickEvent event) {
 				GWTImageInfo imgInfo = ((ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1]).getImageInfo();
 				
 				GWTImageInfo newImgInfo = imgInfo.clone();
 				
 				newImgInfo.getQuery().setImageType("pdf");
 				
 				cp.exportImage(newImgInfo);
 			}
 		});
 		
 		psExportItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 
 			public void onClick(MenuItemClickEvent event) {
 				GWTImageInfo imgInfo = ((ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1]).getImageInfo();
 				
 
 				GWTImageInfo newImgInfo = imgInfo.clone();
 				
 				newImgInfo.getQuery().setImageType("ps");
 				
 				cp.exportImage(newImgInfo);
 			}
 		});
 		
 		svgExportItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 
 			public void onClick(MenuItemClickEvent event) {
 				GWTImageInfo imgInfo = ((ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1]).getImageInfo();
 				
 				GWTImageInfo newImgInfo = imgInfo.clone();
 				
 				newImgInfo.getQuery().setImageType("svg");
 				
 				cp.exportImage(newImgInfo);
 			}
 		});
 		
 		pngExportItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 
 			public void onClick(MenuItemClickEvent event) {
 				GWTImageInfo imgInfo = ((ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1]).getImageInfo();
 				
 				GWTImageInfo newImgInfo = imgInfo.clone();
 				
 				newImgInfo.getQuery().setImageType("png");
 								
 				cp.exportImage(newImgInfo);
 			}
 		});
 		
 		exportMenu.setItems(pdfExportItem, psExportItem, svgExportItem, pngExportItem);
 		
 		final ImgCanvas image = createImageLayer(imgInfo);
 		
 		ToolStripMenuButton exportMenuButton = new ToolStripMenuButton("Export", exportMenu);
 		
 		selectButton = new ToolStripButton();
 		selectButton.setTitle("Select");
 		selectButton.setActionType(SelectionType.CHECKBOX);
 		selectButton.setSelected(false);
 		selectButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				ToolStripButton b = (ToolStripButton) event.getSource();
 				
 				if(!b.isSelected()){
 					ImgCanvas imgLayer = (ImgCanvas) cp.getCenterTabSet().getSelectedTab().getPane().getChildren()[1];
 					imgLayer.hideRec();
 				}
 			}
 			
 		});
 		
 		presentationToolStrip.addButton(selectButton);
 		
 		presentationToolStrip.addMenuButton(exportMenuButton);
 		
 		presentationLayer.addMember(presentationToolStrip);
 		
 		image.setChromosome(chrTextItem);
 		image.setStart(startTextItem);
 		image.setEnd(endTextItem);
 		
 		presentationLayer.addMember(image);
 		
 		imgTab.setPane(presentationLayer);
 		
 		centerTabSet.addTab(imgTab);
 	
 		centerTabSet.selectTab(imgTab);
 		
 	}
 	
 	public void loadMutationWindow(String geneId, int trackId, FoConfigData cd){
 		//TODO
 		Window window = new Window();
 		window.setTitle("SNVs");
 		window.setWidth(700);
 		window.setHeight(330);
 		window.setAutoCenter(true);
 		window.setCanDragResize(true);
 		
 		ListGrid mutGrid = new ListGrid();
 		mutGrid.setWidth100();
 		mutGrid.setHeight100();
 		mutGrid.setShowAllRecords(true);
 		mutGrid.setAlternateRecordStyles(true);
 		mutGrid.setShowHeader(true);
 		mutGrid.setWrapCells(true);
 		mutGrid.setFixedRecordHeights(false);
 		
 		mutGrid.setShowAllRecords(false);
 		mutGrid.setAutoFetchData(false);
 		
 		SNPMutationDS mDS = new SNPMutationDS();
 		mDS.addConfigData(cd);
 		
 		Criteria c = new Criteria("geneId", geneId);
 		c.setAttribute("trackId", trackId);
 		
 		mutGrid.setDataSource(mDS);
 		mutGrid.setFetchOperation(OperationId.MUTATION_FETCH_FOR_ATTRIBS);
 		mutGrid.fetchData(c);
 		
 		mutGrid.setGroupStartOpen(GroupStartOpen.ALL);
 		mutGrid.groupBy("studyName");
 		
 		window.addItem(mutGrid);
 		
 		window.show();
 	}
 	
 	public void loadWindow(FoSegment segmentData){
 		
 		Window window = new Window();
 		window.setTitle("Segment " + segmentData.getId());
 		window.setWidth(500);
 		window.setHeight(330);
 		window.setAutoCenter(true);
 		window.setCanDragResize(true);
 		
 		final ListGrid cncGrid = new ListGrid();
 		cncGrid.setWidth100();
 		cncGrid.setHeight100();
 		cncGrid.setShowAllRecords(true);
 		cncGrid.setAlternateRecordStyles(true);
 		cncGrid.setShowHeader(false);
 		cncGrid.setWrapCells(true);
 		cncGrid.setCellHeight(40);
 		cncGrid.setCanEdit(true);
 		cncGrid.setEditEvent(ListGridEditEvent.CLICK);
 		
 		
 		ListGridField key = new ListGridField("key", "key");
 		ListGridField val = new ListGridField("val", "val");
 		
 		cncGrid.setFields(key, val);
 		
 		ListGridRecord[] lgr = new ListGridRecord[15];
 		
 		lgr[0] = new ListGridRecord();
 		lgr[0].setAttribute("key", "Segment ID ");
 		lgr[0].setAttribute("val", segmentData.getId());
 		
 		lgr[1] = new ListGridRecord();
 		lgr[1].setAttribute("key", "Chromosome");
 		lgr[1].setAttribute("val", segmentData.getLocation().getChromosome());
 		
 		lgr[2] = new ListGridRecord();
 		lgr[2].setAttribute("key", "Start");
 		lgr[2].setAttribute("val", segmentData.getLocation().getStart());
 		
 		lgr[3] = new ListGridRecord();
 		lgr[3].setAttribute("key", "End");
 		lgr[3].setAttribute("val", segmentData.getLocation().getEnd());
 		
 		lgr[4] = new ListGridRecord();
 		lgr[4].setAttribute("key", "Segment Mean");
 		lgr[4].setAttribute("val", segmentData.getMean());
 		
 		lgr[5] = new ListGridRecord();
 		lgr[5].setAttribute("key", "Markers");
 		lgr[5].setAttribute("val", segmentData.getNumberOfMarkers());
 		
 		lgr[6] = new ListGridRecord();
 		lgr[6].setAttribute("key", "Status");
 		lgr[6].setAttribute("val", segmentData.getStatus());
 		
 		lgr[7] = new ListGridRecord();
 		lgr[7].setAttribute("key", "Status score");
 		lgr[7].setAttribute("val", segmentData.getStatusScore());
 		
 		lgr[8] = new ListGridRecord();
 		lgr[8].setAttribute("key", "Study");
 		lgr[8].setAttribute("val", segmentData.getStudyName());
 		
 		cncGrid.setData(lgr);
 		
 		window.addItem(cncGrid);
 		
 		window.show();
 	}
 	
 	public void loadWindow(EnsemblGene gene){
 		
 		Window window = new Window();
 
 		window.setTitle("Gene " + gene.getGenName());
 		window.setAutoSize(true);
 		window.setAutoCenter(true);
 		
 		final ListGrid geneGrid = new ListGrid();
 		geneGrid.setWidth(450);
 		geneGrid.setHeight(270);  
 		geneGrid.setShowAllRecords(true);  
 		geneGrid.setAlternateRecordStyles(true);
 		geneGrid.setShowHeader(false);
 		geneGrid.setWrapCells(true);
 		geneGrid.setFixedRecordHeights(false);
 		geneGrid.setCanEdit(true);
 		geneGrid.setEditEvent(ListGridEditEvent.CLICK);
 		
 		ListGridField key = new ListGridField("key", "key");
 		ListGridField val = new ListGridField("val", "val");
 		
 		geneGrid.setFields(key, val);
 		
 		ListGridRecord[] lgr = new ListGridRecord[9];
 		
 		lgr[0] = new ListGridRecord();
 		lgr[0].setAttribute("key", "Ensembl Stable ID");
 		lgr[0].setAttribute("val", gene.getAccessionID());
 		
 		lgr[1] = new ListGridRecord();
 		lgr[1].setAttribute("key", "Name");
 		lgr[1].setAttribute("val", gene.getGenName());
 		
 		lgr[2] = new ListGridRecord();
 		lgr[2].setAttribute("key", "Chromosome");
 		lgr[2].setAttribute("val", gene.getChr());
 		
 		lgr[3] = new ListGridRecord();
 		lgr[3].setAttribute("key", "Start");
 		lgr[3].setAttribute("val", gene.getStart());
 		
 		lgr[4] = new ListGridRecord();
 		lgr[4].setAttribute("key", "End");
 		lgr[4].setAttribute("val", gene.getEnd());
 		
 		lgr[5] = new ListGridRecord();
 		lgr[5].setAttribute("key", "Length");
 		lgr[5].setAttribute("val", gene.getLength());
 		
 		lgr[6] = new ListGridRecord();
 		lgr[6].setAttribute("key", "Strand");
 		lgr[6].setAttribute("val", gene.getStrand());
 		
 		lgr[7] = new ListGridRecord();
 		lgr[7].setAttribute("key", "Bio Type");
 		lgr[7].setAttribute("val", gene.getBioType());
 		
 		lgr[8] = new ListGridRecord();
 		lgr[8].setAttribute("key", "Description");
 		lgr[8].setAttribute("val", gene.getDescription());
 		
 		geneGrid.setData(lgr);
 		
 		window.addItem(geneGrid);
 		
 		window.show();
 	}
 	
 	public void loadSetPwWindow(final int userId, String username, UserDS uDS){
 		
 		final Window window = new Window();
 
 		window.setTitle("Set password for " + username);
 		window.setWidth(250);
 		window.setHeight(100);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		pwForm = new DynamicForm();
 		pwForm.setUseAllDataSourceFields(true);
 		pwForm.setDataSource(uDS);
 		
 		uDS.getField("userId").setHidden(true);
 		uDS.getField("userName").setHidden(true);
 		uDS.getField("firstName").setHidden(true);
 		uDS.getField("lastName").setHidden(true);
 		uDS.getField("email").setHidden(true);
 		
 		
 		pwForm.editSelectedData(userGrid);
 		
 		newPwPasswordItem = new PasswordItem();
 		newPwPasswordItem.setName("pw");
 		
 		ButtonItem submitPwButton = new ButtonItem("submit");
 		submitPwButton.setWidth(50);
 		
 		submitPwButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				pwForm.setUpdateOperation(OperationId.USER_UPDATE_PASSWORD_ADMIN);
 				pwForm.saveData();
 				
 				window.hide();
 			}
 			
 		});
 
 		pwForm.setItems(newPwPasswordItem, submitPwButton);
 	
 		window.addItem(pwForm);
 		
 		window.show();
 		
 	}
 	
 	public void openUserAdminTab(){
 	
 		Tab usersAdminTab = new Tab("Users");
 		usersAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip userToolStrip = new ToolStrip();
 		userToolStrip.setWidth100();
 		
 		UserDS uDS = new UserDS();
 		
 		ToolStripButton changePwButton = new ToolStripButton();  
 		changePwButton.setTitle("Change Password");
 		changePwButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ListGridRecord lgr = userGrid.getSelectedRecord();
 				
 				UserDS uDS = (UserDS) userGrid.getDataSource();
 				
 				if (lgr != null){
 				
 					loadSetPwWindow(Integer.parseInt(userGrid.getSelectedRecord().getAttribute("userId")),
 						userGrid.getSelectedRecord().getAttribute("userName"), uDS);
 					
 				} else {
 					SC.say("Select a user.");
 				}
 			}});
 		
 		userToolStrip.addButton(changePwButton);
 		
 		controlsPanel.addMember(userToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		userGrid = new ListGrid();
 		userGrid.setWidth100();
 		userGrid.setHeight100();
 		userGrid.setAlternateRecordStyles(true);
 		userGrid.setWrapCells(true);
 		userGrid.setFixedRecordHeights(false);
 		userGrid.setEditByCell(true);
 		userGrid.setAutoSaveEdits(false);
 		
 		userGrid.setShowAllRecords(false);
 		userGrid.setAutoFetchData(false);
 		
 		userGrid.setDataSource(uDS);
 		userGrid.setFetchOperation(OperationId.USER_FETCH_ALL);
 		userGrid.fetchData();
 		
 		ListGridField lgfId = new ListGridField("userId", "User ID");
 		lgfId.setCanEdit(false);
 		ListGridField lgfFirstName = new ListGridField("firstName", "First Name");
 		lgfFirstName.setCanEdit(false);
 		ListGridField lgfLastName = new ListGridField("lastName", "Last Name");
 		lgfFirstName.setCanEdit(false);
 		ListGridField lgfUserName = new ListGridField("userName", "Username");
 		lgfUserName.setCanEdit(false);
 		ListGridField lgfEmail = new ListGridField("email", "E-Mail");
 		lgfEmail.setCanEdit(false);
 		ListGridField lgfIsActive = new ListGridField("isActive", "Activated");
 		lgfIsActive.setCanEdit(true);
 		
 		lgfIsActive.addChangedHandler(new com.smartgwt.client.widgets.grid.events.ChangedHandler(){
 
 			@Override
 			public void onChanged(
 					com.smartgwt.client.widgets.grid.events.ChangedEvent event) {
 				
 				ListGridRecord lgr = userGrid.getSelectedRecord();
 				userGrid.setUpdateOperation(OperationId.USER_UPDATE_ISACTIVE);
 				userGrid.updateData(lgr);
 			}
 		});
 		
 		ListGridField lgfisAdmin = new ListGridField("isAdmin", "Administator");
 		lgfisAdmin.setCanEdit(true);
 		lgfisAdmin.addChangedHandler(new com.smartgwt.client.widgets.grid.events.ChangedHandler(){
 
 			@Override
 			public void onChanged(
 					com.smartgwt.client.widgets.grid.events.ChangedEvent event) {
 				
 				ListGridRecord lgr = userGrid.getSelectedRecord();
 				userGrid.setUpdateOperation(OperationId.USER_UPDATE_ISADMIN);
 				userGrid.updateData(lgr);
 			}
 		});
 		
 		userGrid.setFields(lgfId, lgfFirstName, lgfLastName, lgfUserName, lgfEmail, lgfIsActive, lgfisAdmin);
 		
 		gridContainer.addMember(userGrid);
 		
 		pane.addMember(gridContainer);
 		
 		usersAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(usersAdminTab);
 		
 		centerTabSet.selectTab(usersAdminTab);
 		
 	}
 	
 	public void openUserProfileTab(){
 		
 		Tab userProfileTab = new Tab("Profile");
 		userProfileTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		HLayout header = new HLayout();
 		header.setAutoWidth();
 		header.setAutoHeight();
 		
 		Label headerLbl = new Label("<h2>Profile</h2>");
 		headerLbl.setWrap(false);
 		header.addMember(headerLbl);
 		
 		pane.addMember(header);
 		
 		UserDS uDS = new UserDS();
 		uDS.getField("userId").setHidden(false);
 		
 		userProfileForm = new DynamicForm();
 		userProfileForm.setWidth(250);
 		userProfileForm.setHeight(260);
 		userProfileForm.setAlign(Alignment.CENTER);
 
 		userProfileForm.setDataSource(uDS);
 		
 		userIdTextItem = new TextItem();
 		userIdTextItem.setName("userId");
 		userIdTextItem.setRequired(false);
 		userIdTextItem.setDisabled(true);
 		
 		userNameTextItem = new TextItem();
 		userNameTextItem.setName("userName");
 		userNameTextItem.setRequired(false);
 		userNameTextItem.setDisabled(true);
 		
 		userFirstNameTextItem = new TextItem();
 		userFirstNameTextItem.setName("firstName");
 		
 		userLastNameTextItem = new TextItem();
 		userLastNameTextItem.setName("lastName");
 		
 		userEmailTextItem = new TextItem();
 		userEmailTextItem.setName("email");
 		
 		
 		userProfileForm.setFetchOperation(OperationId.USER_FETCH_PROFILE);
 		userProfileForm.fetchData();
 		
 		ButtonItem updateProfile = new ButtonItem();
 		updateProfile.setTitle("Update Profile");
 		updateProfile.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				if(userFirstNameTextItem.validate() && userLastNameTextItem.validate() && userEmailTextItem.validate()){
 					
 					userProfileForm.setUpdateOperation(OperationId.USER_UPDATE_PROFILE);
 					userProfileForm.saveData();
 				}
 			}});
 		
 		
 		userProfileForm.setItems(userIdTextItem,
 								userNameTextItem,
 								userFirstNameTextItem,
 								userLastNameTextItem,
 								userEmailTextItem,
 								updateProfile);
 		
 		userPwForm = new DynamicForm();
 		userPwForm.setWidth(250);
 		userPwForm.setHeight(260);
 		userPwForm.setAlign(Alignment.CENTER);
 		userPwForm.setUseAllDataSourceFields(true);
 		userPwForm.setDataSource(uDS);
 		uDS.getField("userId").setHidden(true);
 		uDS.getField("userName").setHidden(true);
 		uDS.getField("firstName").setHidden(true);
 		uDS.getField("lastName").setHidden(true);
 		uDS.getField("email").setHidden(true);
 		
 		userPwItem = new PasswordItem();
 		userPwItem.setName("pw");
 		
 		userPwConfirmItem = new PasswordItem();
 		userPwConfirmItem.setTitle("Confirm Password");
 		userPwConfirmItem.setRequired(true);
 		
 		MatchesFieldValidator matchesValidator = new MatchesFieldValidator();  
 		matchesValidator.setOtherField("pw");
 		matchesValidator.setErrorMessage("Passwords do not match!");
 		userPwConfirmItem.setValidators(matchesValidator);  
 		
 		userPwForm.setFetchOperation(OperationId.USER_FETCH_PROFILE);
 		userPwForm.fetchData();
 		
 		ButtonItem updatePassword = new ButtonItem();
 		updatePassword.setTitle("Set Password");
 		updatePassword.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				
 				if(userPwConfirmItem.validate()){
 					
 					userPwForm.setUpdateOperation(OperationId.USER_UPDATE_PASSWORD);
 					userPwForm.saveData();
 				}
 			}});
 		
 		userPwForm.setItems(userPwItem,
 							userPwConfirmItem,
 							updatePassword);
 		
 		pane.addMember(userProfileForm);
 		
 		pane.addMember(userPwForm);
 		
 		userProfileTab.setPane(pane);
 		
 		centerTabSet.addTab(userProfileTab);
 		
 		centerTabSet.selectTab(userProfileTab);
 		
 	}
 	
 	public void loadUserToGroupWindow(final FoGroup foGroup){
 		
 		final Window window = new Window();
 		
 		window.setTitle("Add User to Group: " + foGroup.getName());
 		window.setWidth(250);
 		window.setHeight(100);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		DynamicForm userToGroupForm = new DynamicForm();
 		
 		userSelectItem = new SelectItem();  
         userSelectItem.setTitle("User");
         
         getAllUsersExceptGroup(foGroup); 
         
         ButtonItem addUserToGroupButton = new ButtonItem("Add");
 		addUserToGroupButton.setWidth(50);
 		
 		addUserToGroupButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 
 				addUserToGroup(foGroup, Integer.parseInt(userSelectItem.getValueAsString()));
 				window.hide();
 			}
 			
 		});
 		
 		userToGroupForm.setItems(userSelectItem, addUserToGroupButton);
 		
 		window.addItem(userToGroupForm);
 		
 		window.show();
 	}
 	
 	public void loadGroupManageWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add Group");
 		window.setWidth(250);
 		window.setHeight(100);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true); 
 		
 		DynamicForm groupForm = new DynamicForm();
 		groupNameTextItem = new TextItem();
 		groupNameTextItem.setTitle("Group Name");
 		
 		ButtonItem addGroupButton = new ButtonItem("Add");
 		addGroupButton.setWidth(50);
 		
 		addGroupButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				addGroup(new FoGroup(0, groupNameTextItem.getDisplayValue(), true));
 				window.hide();
 			}
 			
 		});
 
 		groupForm.setItems(groupNameTextItem, addGroupButton);
 	
 		window.addItem(groupForm);
 		
 		window.show();
 	}
 	
 	public void openGroupAdminTab(){
 		Tab groupAdminTab;
 		groupAdminTab = new Tab("Manage Groups");
 		
 		groupAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip groupToolStrip = new ToolStrip();
 		groupToolStrip.setWidth100();
 		
 		ToolStripButton addGroupButton = new ToolStripButton();  
 		addGroupButton.setTitle("add Group");
 		addGroupButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadGroupManageWindow();
 			}});
 		
 		groupToolStrip.addButton(addGroupButton);
 		
 		ToolStripButton deleteGroupButton = new ToolStripButton();  
 		deleteGroupButton.setTitle("delete Group");
 		deleteGroupButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				final ListGridRecord lgr = groupGrid.getSelectedRecord();
 				
 				SC.confirm("Do you really want to delete " + lgr.getAttribute("groupName") + "?", new BooleanCallback(){
 
 					@Override
 					public void execute(Boolean value) {
 						if(value != null && value){
 							FoGroup group = new FoGroup(Integer.parseInt(lgr.getAttribute("groupId")),
 											lgr.getAttribute("groupName"),
 											Boolean.parseBoolean(lgr.getAttribute("isactive")));
 				
 							deleteGroup(group);
 						}
 					}
 				});
 				
 			}});
 		
 		groupToolStrip.addButton(deleteGroupButton);
 		
 		ToolStripButton addUserGroupButton = new ToolStripButton();  
 		addUserGroupButton.setTitle("add User to Group");
 		addUserGroupButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				ListGridRecord lgr = groupGrid.getSelectedRecord();
 				
 				FoGroup group = new FoGroup(Integer.parseInt(lgr.getAttribute("groupId")),
 															lgr.getAttribute("groupName"),
 															Boolean.parseBoolean(lgr.getAttribute("isactive")));
 				
 				loadUserToGroupWindow(group);
 				
 		}});
 		
 		groupToolStrip.addButton(addUserGroupButton);
 		
 		ToolStripButton removeUserGroupButton = new ToolStripButton();
 		removeUserGroupButton.setTitle("remove User from Group");
 		removeUserGroupButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				ListGridRecord userLgr = groupUserGrid.getSelectedRecord();
 				ListGridRecord groupLgr = groupGrid.getSelectedRecord();
 				
 				int groupId = groupLgr.getAttributeAsInt("groupId");
 				int userId = userLgr.getAttributeAsInt("userId");
 				
 				removeUserFromGroup(groupId, userId);
 				
 		}});
 		
 		groupToolStrip.addButton(removeUserGroupButton);
 		
 		controlsPanel.addMember(groupToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		groupGrid = new ListGrid();
 		groupGrid.setWidth("50%");
 		groupGrid.setHeight100();
 		groupGrid.setShowAllRecords(true);
 		groupGrid.setAlternateRecordStyles(true);
 		groupGrid.setWrapCells(true);
 		groupGrid.setFixedRecordHeights(false);
 		groupGrid.markForRedraw();
 		
 		ListGridField lgfGroupId = new ListGridField("groupId", "group ID");
 		ListGridField lgfGroupName = new ListGridField("groupName", "Group Name");
 		ListGridField lgfGroupActivated = new ListGridField("isactive", "Activated");
 		
 		groupGrid.setFields(lgfGroupId, lgfGroupName, lgfGroupActivated);
 		
 		showAllGroups();
 		
 		gridContainer.addMember(groupGrid);
 		
 		groupUserGrid = new ListGrid();
 		groupUserGrid.setWidth("50%");
 		groupUserGrid.setHeight100();
 		groupUserGrid.setShowAllRecords(true);  
 		groupUserGrid.setAlternateRecordStyles(true);
 		groupUserGrid.setWrapCells(true);
 		groupUserGrid.setFixedRecordHeights(false);
 		groupUserGrid.markForRedraw();
 		
 		ListGridField lgfGroupUserId = new ListGridField("userId", "User ID");
 		ListGridField lgfGroupUserName = new ListGridField("userName", "Username");
 		
 		groupUserGrid.setFields(lgfGroupUserId, lgfGroupUserName);
 		
 		gridContainer.addMember(groupUserGrid);
 		
 		pane.addMember(gridContainer);
 		
 		groupAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(groupAdminTab);
 		
 		centerTabSet.selectTab(groupAdminTab);
 		
 	}
 	
 	public void loadOrganManageWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add Organ");
 		window.setWidth(250);
 		window.setHeight(120);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		DynamicForm organForm = new DynamicForm();
 		organLabelTextItem = new TextItem();
 		organLabelTextItem.setTitle("Property Label");
 		
 		organTypeCbItem = new ComboBoxItem(); 
 		organTypeCbItem.setTitle("Type");
 		organTypeCbItem.setType("comboBox");
 		
 		organTypeCbItem.setAutoFetchData(false);
 		
 		OrganDS oDS = new OrganDS();
 		
 		organTypeCbItem.setOptionDataSource(oDS);
 		organTypeCbItem.setOptionOperationId(OperationId.ORGAN_FETCH_TYPES);
 		organTypeCbItem.setDisplayField("typeName");
 		organTypeCbItem.setValueField("typeId");
 		
 		ButtonItem addOrganButton = new ButtonItem("Add");
 		addOrganButton.setWidth(50);
 		
 		addOrganButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("organName", organLabelTextItem.getDisplayValue());
 				lgr.setAttribute("organType", organTypeCbItem.getDisplayValue());
 				
 				organGrid.addData(lgr);
 				
 				window.hide();
 			}
 		});
 
 		organForm.setItems(organLabelTextItem, organTypeCbItem, addOrganButton);
 	
 		window.addItem(organForm);
 		
 		window.show();
 	}
 	
 	public void openOrganAdminTab(){
 		
 		Tab organsAdminTab = new Tab("Manage Organs");
 		organsAdminTab.setCanClose(true);
 
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip organToolStrip = new ToolStrip();
 		organToolStrip.setWidth100();
 		
 		ToolStripButton addOrganButton = new ToolStripButton();
 		addOrganButton.setTitle("add Organ");
 		addOrganButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadOrganManageWindow();
 			}});
 		
 		organToolStrip.addButton(addOrganButton);
 		
 		controlsPanel.addMember(organToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		organGrid = new ListGrid();
 		organGrid.setWidth100();
 		organGrid.setHeight100();
 		organGrid.setAlternateRecordStyles(true);
 		organGrid.setWrapCells(true);
 		organGrid.setFixedRecordHeights(false);
 		organGrid.setShowAllRecords(false);
 		organGrid.setAutoFetchData(false);
 		
 		ListGridField lgfId = new ListGridField("organId", "Organ ID");
 		ListGridField lgfLabel = new ListGridField("organName", "Organ Name");
 		ListGridField lgfType = new ListGridField("organType", "Organ Type");
 		ListGridField lgfActivity = new ListGridField("organActivity", "enabled");
 		
 		organGrid.setFields(lgfId, lgfLabel, lgfType, lgfActivity);
 		
 		OrganDS oDS = new OrganDS();
 		
 		organGrid.setDataSource(oDS);
 		organGrid.setFetchOperation(OperationId.ORGAN_FETCH_ALL);
 		
 		organGrid.fetchData();
 		
 		gridContainer.addMember(organGrid);
 		
 		pane.addMember(gridContainer);
 		
 		organsAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(organsAdminTab);
 		
 		centerTabSet.selectTab(organsAdminTab);
 	}
 	
 	public void loadPropertyManageWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add Property");
 		window.setWidth(250);
 		window.setHeight(120);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		DynamicForm propertyForm = new DynamicForm();
 		propertyLabelTextItem = new TextItem();
 		propertyLabelTextItem.setTitle("Property Label");
 		
 		propertyTypeCbItem = new ComboBoxItem(); 
 		propertyTypeCbItem.setTitle("Type");
 		propertyTypeCbItem.setType("comboBox");
 		
 		propertyTypeCbItem.setDisplayField("typeName");
 		propertyTypeCbItem.setValueField("typeId");
 		
 		propertyTypeCbItem.setAutoFetchData(false);
 		
 		PropertyDS pDS = new PropertyDS();
 		
 		propertyTypeCbItem.setOptionDataSource(pDS);
 		propertyTypeCbItem.setOptionOperationId(OperationId.PROPERTY_FETCH_TYPES);
 		
 		ButtonItem addPropertyButton = new ButtonItem("Add");
 		addPropertyButton.setWidth(50);
 		
 		addPropertyButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("propertyName", propertyLabelTextItem.getDisplayValue());
 				lgr.setAttribute("propertyType", propertyTypeCbItem.getDisplayValue());
 				
 				propertyGrid.addData(lgr);
 				
 				window.hide();
 				
 				window.hide();
 			}
 		});
 
 		propertyForm.setItems(propertyLabelTextItem, propertyTypeCbItem, addPropertyButton);
 	
 		window.addItem(propertyForm);
 		
 		window.show();
 	}
 	
 	public void openPropertyAdminTab(){
 		
 		Tab propertiesAdminTab = new Tab("Manage Properties");
 		propertiesAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip propertyToolStrip = new ToolStrip();
 		propertyToolStrip.setWidth100();
 		
 		ToolStripButton addPropertyButton = new ToolStripButton();
 		addPropertyButton.setTitle("add Property");
 		addPropertyButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadPropertyManageWindow();
 			}});
 		
 		propertyToolStrip.addButton(addPropertyButton);
 		
 		controlsPanel.addMember(propertyToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 				
 		propertyGrid = new ListGrid();
 		propertyGrid.setWidth100();
 		propertyGrid.setHeight100();
 		propertyGrid.setAlternateRecordStyles(true);
 		propertyGrid.setWrapCells(true);
 		propertyGrid.setFixedRecordHeights(false);
 		propertyGrid.setShowAllRecords(false);
 		propertyGrid.setAutoFetchData(false);
 		
 		ListGridField lgfId = new ListGridField("propertyId", "Property ID");
 		ListGridField lgfLabel = new ListGridField("propertyName", "Property Name");
 		ListGridField lgfType = new ListGridField("propertyType", "Property Type");
 		ListGridField lgfActivity = new ListGridField("propertyActivity", "enabled");
 		
 		propertyGrid.setFields(lgfId, lgfLabel, lgfType, lgfActivity);
 		
 		propertyGrid.setFields(lgfId, lgfLabel, lgfType, lgfActivity);
 		
 		PropertyDS pDS = new PropertyDS();
 		
 		propertyGrid.setDataSource(pDS);
 		propertyGrid.setFetchOperation(OperationId.PROPERTY_FETCH_ALL);
 		
 		propertyGrid.fetchData();
 		
 		gridContainer.addMember(propertyGrid);
 		
 		pane.addMember(gridContainer);
 		
 		propertiesAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(propertiesAdminTab);
 		
 		centerTabSet.selectTab(propertiesAdminTab);
 	}
 	
 	public void loadPlatformManageWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add Platform");
 		window.setWidth(250);
 		window.setHeight(120);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		DynamicForm chipForm = new DynamicForm();
 		platformLabelTextItem = new TextItem();
 		platformLabelTextItem.setTitle("Platform Label");
 		
 		platformTypeCbItem = new ComboBoxItem(); 
 		platformTypeCbItem.setTitle("Type");
 		platformTypeCbItem.setType("comboBox");
 		
 		platformTypeCbItem.setAutoFetchData(false);
 		
 		PlatformDS pDS = new PlatformDS();
 		
 		platformTypeCbItem.setOptionDataSource(pDS);
 		platformTypeCbItem.setOptionOperationId(OperationId.PLATFORM_FETCH_TYPES);
 		platformTypeCbItem.setDisplayField("typeName");
 		platformTypeCbItem.setValueField("typeId");
 		
 		ButtonItem addPlatformButton = new ButtonItem("Add");
 		addPlatformButton.setWidth(50);
 		
 		addPlatformButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("platformName", platformLabelTextItem.getDisplayValue());
 				lgr.setAttribute("platformType", platformTypeCbItem.getDisplayValue());
 				
 				platformGrid.addData(lgr);
 				
 				window.hide();
 			}
 		});
 
 		chipForm.setItems(platformLabelTextItem, platformTypeCbItem, addPlatformButton);
 	
 		window.addItem(chipForm);
 		
 		window.show();
 	}
 	
 	public void openPlatformAdminTab(){
 		
 		Tab platformsAdminTab = new Tab("Manage Platforms");
 		platformsAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip platformToolStrip = new ToolStrip();
 		platformToolStrip.setWidth100();
 		
 		ToolStripButton addPlatformButton = new ToolStripButton();
 		addPlatformButton.setTitle("add Platform");
 		addPlatformButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadPlatformManageWindow();
 			}});
 		
 		platformToolStrip.addButton(addPlatformButton);
 		
 		controlsPanel.addMember(platformToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		platformGrid = new ListGrid();
 		platformGrid.setWidth100();
 		platformGrid.setHeight100();
 		platformGrid.setAlternateRecordStyles(true);
 		platformGrid.setWrapCells(true);
 		platformGrid.setFixedRecordHeights(false);
 		platformGrid.setShowAllRecords(false);
 		platformGrid.setAutoFetchData(false);
 		
 		ListGridField lgfId = new ListGridField("platformId", "Platform ID");
 		ListGridField lgfLabel = new ListGridField("platformName", "Platform Name");
 		ListGridField lgfType = new ListGridField("platformType", "Platform Type");
 		
 		platformGrid.setFields(lgfId, lgfLabel, lgfType);
 		
 		PlatformDS pDS = new PlatformDS();
 		
 		platformGrid.setDataSource(pDS);
 		platformGrid.setFetchOperation(OperationId.PLATFORM_FETCH_ALL);
 		platformGrid.fetchData();
 		
 		gridContainer.addMember(platformGrid);
 		
 		pane.addMember(gridContainer);
 		
 		platformsAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(platformsAdminTab);
 		
 		centerTabSet.selectTab(platformsAdminTab);
 	}
 	
 	public void openDataTab(String type, String studyId){
 		Tab segmentAdminTab = new Tab(type);
 		segmentAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		dataTypeGrid = new ListGrid();
 		dataTypeGrid.setWidth100();
 		dataTypeGrid.setHeight100();
 		dataTypeGrid.setShowAllRecords(true);
 		dataTypeGrid.setAlternateRecordStyles(true);
 		dataTypeGrid.setWrapCells(true);
 		dataTypeGrid.setFixedRecordHeights(false);
 		dataTypeGrid.setAutoFetchData(false);
 		dataTypeGrid.setShowAllRecords(false);
 		
 		
 		if(type.equals("Segments")){
 			SegmentDS sDS = new SegmentDS();
 		
 			dataTypeGrid.setDataSource(sDS);
 		}
 		
 		if(type.equals(FoConstants.SNV)){
 			SNPMutationDS mDS = new SNPMutationDS();
 		
 			dataTypeGrid.setDataSource(mDS);
 			dataTypeGrid.setFetchOperation(OperationId.MUTATION_FETCH_FOR_STUDY_ID);
 		}
 		
 		if(type.equals("Translocations")){
 			TranslocationDS tDS = new TranslocationDS();
 		
 			dataTypeGrid.setDataSource(tDS);
 		}
 		
 		if(type.equals("Features")){
 			FeatureDS fDS = new FeatureDS();
 			
 			dataTypeGrid.setDataSource(fDS);
 			
 			dataTypeGrid.setFetchOperation(OperationId.FEATURE_FETCH_FOR_STUDY_ID);
 		}
 		
 		dataTypeGrid.fetchData(new Criteria("studyId", studyId));
 		
 		gridContainer.addMember(dataTypeGrid);
 		
 		pane.addMember(gridContainer);
 		
 		segmentAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(segmentAdminTab);
 		
 		centerTabSet.selectTab(segmentAdminTab);
 	}
 	
 	public void openStudyAdminTab(FoUser user){
 		
 		Tab studyAdminTab = new Tab("Manage Studies");
 		studyAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip sToolStrip = new ToolStrip();
 		sToolStrip.setWidth100();
 		
 		projectSelectItem = new SelectItem();
 		projectSelectItem.setTitle("Project");
 		
 		projectSelectItem.setDisplayField("projectName");
 		projectSelectItem.setValueField("projectId");		
 		
 		projectSelectItem.setAutoFetchData(false);
 		
 		ProjectDS pDS = new ProjectDS();
 		
 		projectSelectItem.setOptionDataSource(pDS);
 		projectSelectItem.setOptionOperationId(OperationId.PROJECT_FETCH_ALL);
 		
 		projectSelectItem.setDefaultToFirstOption(true);
 		projectSelectItem.addChangedHandler(new ChangedHandler() {
 
 			@Override
 			public void onChanged(ChangedEvent event) {
 				
 				String projectId = projectSelectItem.getValueAsString();
 				
 				studyGrid.fetchData(new Criteria("projectId", projectId));
 			}
 		});
 		
 		sToolStrip.addFormItem(projectSelectItem);
 		
 		ToolStripButton showSegmentsButton = new ToolStripButton();
 		showSegmentsButton.setTitle("show segments");
 		showSegmentsButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ListGridRecord lgr = studyGrid.getSelectedRecord();
 				
 				if (lgr != null){
 				
 					openDataTab("Segments", lgr.getAttributeAsString("studyId"));
 					
 				} else {
 					SC.say("Select a study.");
 				}
 				
 			}});
 		
 		sToolStrip.addButton(showSegmentsButton);
 		
 		ToolStripButton showMutationsButton = new ToolStripButton();
 		showMutationsButton.setTitle("show SNVs");
 		showMutationsButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ListGridRecord lgr = studyGrid.getSelectedRecord();
 				
 				if (lgr != null){
 				
					openDataTab("SNVs", lgr.getAttributeAsString("studyId"));
 					
 				} else {
 					SC.say("Select a study.");
 				}
 				
 			}});
 		
 		sToolStrip.addButton(showMutationsButton);
 		
 		ToolStripButton showTranslocationsButton = new ToolStripButton();
 		showTranslocationsButton.setTitle("show translocations");
 		showTranslocationsButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ListGridRecord lgr = studyGrid.getSelectedRecord();
 				
 				if (lgr != null){
 				
 					openDataTab("Translocations", lgr.getAttributeAsString("studyId"));
 					
 				} else {
 					SC.say("Select a study.");
 				}
 				
 			}});
 		
 		sToolStrip.addButton(showTranslocationsButton);
 		
 		ToolStripButton showFeaturesButton = new ToolStripButton();
 		showFeaturesButton.setTitle("show features");
 		showFeaturesButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ListGridRecord lgr = studyGrid.getSelectedRecord();
 				
 				if (lgr != null){
 				
 					openDataTab("Features", lgr.getAttributeAsString("studyId"));
 					
 				} else {
 					SC.say("Select a study.");
 				}
 				
 			}});
 		
 		sToolStrip.addButton(showFeaturesButton);
 		
 		ToolStripButton removeStudyButton = new ToolStripButton();
 		removeStudyButton.setTitle("remove study");
 		if(!user.getIsAdmin()){
 			removeStudyButton.setDisabled(true);
 		}
 		removeStudyButton.addClickHandler(new ClickHandler(){
 		
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				final ListGridRecord lgr = studyGrid.getSelectedRecord();
 				
 				if(lgr != null) {
 				
 					SC.confirm("Do you really want to delete " + lgr.getAttribute("studyName") + "?", new BooleanCallback(){
 
 						@Override
 						public void execute(Boolean value) {
 							if(value != null && value){
 								
 								lgr.setAttribute("projectId", projectSelectItem.getValueAsString());
 								
 								studyGrid.removeData(lgr);
 								
 							}
 						}
 					});
 					
 				} else {
 					SC.say("Select a study.");
 				}
 				
 			}});
 		
 		sToolStrip.addButton(removeStudyButton);
 		
 		controlsPanel.addMember(sToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		studyGrid = new ListGrid();
 		studyGrid.setWidth100();
 		studyGrid.setHeight100();
 		studyGrid.setShowAllRecords(true);
 		studyGrid.setAlternateRecordStyles(true);
 		studyGrid.setWrapCells(true);
 		studyGrid.setShowAllRecords(false);
 		studyGrid.setAutoFetchData(true);
 		studyGrid.setFixedRecordHeights(false);
 		
 		StudyDS mDS = new StudyDS();
 		
 		studyGrid.setDataSource(mDS);
 		studyGrid.setFetchOperation(OperationId.STUDY_FETCH_FOR_PROJECT);
 		
 		gridContainer.addMember(studyGrid);
 		
 		pane.addMember(gridContainer);
 		
 		studyAdminTab.setPane(pane);
 		
 		centerTabSet.addTab(studyAdminTab);
 		
 		centerTabSet.selectTab(studyAdminTab);
 	}
 	
 	public void loadProjectManageWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add Project");
 		window.setWidth(250);
 		window.setHeight(200);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		DynamicForm projectForm = new DynamicForm();
 		projectNameTextItem = new TextItem();
 		projectNameTextItem.setTitle("Project Name");
 		
 		projectDescriptionItem = new TextAreaItem();
 		projectDescriptionItem.setTitle("Description");
 		projectDescriptionItem.setLength(5000);
 		
 		ButtonItem addProjectButton = new ButtonItem("Add");
 		addProjectButton.setWidth(50);
 
 		addProjectButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("projectId", 0);
 				lgr.setAttribute("projectName",projectNameTextItem.getDisplayValue());
 				lgr.setAttribute("projectDescription", projectDescriptionItem.getDisplayValue());
 				
 				projectGrid.addData(lgr);
 				
 				window.hide();
 			}
 			
 		});
 		
 		projectForm.setItems(projectNameTextItem, projectDescriptionItem, addProjectButton);
 	
 		window.addItem(projectForm);
 		
 		window.show();
 	}
 	
 	public void loadProjectAccessManageWindow(final FoProject project){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add project access to project " + project.getName());
 		window.setWidth(250);
 		window.setHeight(200);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		final DynamicForm projectAccessForm = new DynamicForm();
 		
 		groupSelectItem = new SelectItem();
         groupSelectItem.setTitle("Group");
         
         getAllGroupsExceptProject(project);
 		
 		accessRightSelectItem = new SelectItem();
 		accessRightSelectItem.setTitle("Access right");
 		
 		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
         valueMap.put("r", "r");
         valueMap.put("rw", "rw");
 		
         accessRightSelectItem.setValueMap(valueMap);
         
 		ButtonItem addProjectAccessButton = new ButtonItem("Add");
 		addProjectAccessButton.setWidth(50);
 
 		addProjectAccessButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("projectId", project.getId());
 				lgr.setAttribute("groupId", groupSelectItem.getValueAsString());
 				lgr.setAttribute("accessRight", accessRightSelectItem.getDisplayValue());
 				
 				projectAccessGrid.addData(lgr);
 				
 				window.hide();
 			}
 			
 		});
 		
 		projectAccessForm.setItems(groupSelectItem, accessRightSelectItem, addProjectAccessButton);
 	
 		window.addItem(projectAccessForm);
 		
 		window.show();
 	}
 	
 	public void loadAddStudyProjectWindow(final FoProject project){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add study to project " + project.getName());
 		window.setWidth(250);
 		window.setHeight(200);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		final DynamicForm addStudyForm = new DynamicForm();
 		
 		projectSelectItem = new SelectItem();
         projectSelectItem.setTitle("Project");
         
         projectSelectItem.setDisplayField("projectName");
 		projectSelectItem.setValueField("projectId");		
 		
 		projectSelectItem.setAutoFetchData(false);
 		
 		ProjectDS pDS = new ProjectDS();
 		
 		projectSelectItem.setOptionDataSource(pDS);
 		projectSelectItem.setOptionOperationId(OperationId.PROJECT_FETCH_ALL);
 		
 		projectSelectItem.setDefaultToFirstOption(true);
 		projectSelectItem.addChangedHandler(new ChangedHandler() {
 
 			@Override
 			public void onChanged(ChangedEvent event) {
 				
 				String projectId = projectSelectItem.getValueAsString();
 				String notInProjectId = "" + project.getId();
 				
 				Criteria c = new Criteria("projectId", projectId);
 				c.addCriteria("notInProjectId", notInProjectId);
 				
 				studySelectItem.setPickListCriteria(c);
 				
 				studySelectItem.setOptionOperationId(OperationId.STUDY_FETCH_NOT_IN_PROJECT);
 				
 				studySelectItem.fetchData();
 			}
 		});
 		
 		projectSelectItem.addDataArrivedHandler(new DataArrivedHandler(){
 
 			@Override
 			public void onDataArrived(DataArrivedEvent event) {
 				
 				String projectId = projectSelectItem.getValueAsString();
 				String notInProjectId = "" + project.getId();
 				
 				Criteria c = new Criteria("projectId", projectId);
 				c.addCriteria("notInProjectId", notInProjectId);
 				
 				studySelectItem.setPickListCriteria(c);
 				
 				studySelectItem.setOptionOperationId(OperationId.STUDY_FETCH_NOT_IN_PROJECT);
 				
 				studySelectItem.fetchData();
 				
 			}
 		});
 		
 		studySelectItem = new SelectItem();
 		studySelectItem.setTitle("Study");
 		studySelectItem.setDisplayField("studyName");
 		studySelectItem.setValueField("studyId");
 		studySelectItem.setMultiple(true);
 		studySelectItem.setMultipleAppearance(MultipleAppearance.PICKLIST);
 		studySelectItem.setAutoFetchData(false);
 		
 		StudyDS mDS = new StudyDS();
 		
 		studySelectItem.setOptionDataSource(mDS);
 		studySelectItem.setOptionOperationId(OperationId.STUDY_FETCH_NOT_IN_PROJECT);
 		
 		ButtonItem addStudyButton = new ButtonItem("Add");
 		addStudyButton.setWidth(50);
 
 		addStudyButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord[] lgr = studySelectItem.getSelectedRecords();
 				
 				String projectId = "" + project.getId();
 				
 				projectStudyGrid.setAddOperation(OperationId.STUDY_ADD_TO_PROJECT);
 				
 				for(int i = 0; i < lgr.length; i++){
 				
 					
 					lgr[i].setAttribute("projectId", projectId);
 					projectStudyGrid.addData(lgr[i]);
 				
 				}
 				
 				window.hide();
 			}
 			
 		});
 		
 		addStudyForm.setItems(projectSelectItem, studySelectItem, addStudyButton);
 	
 		window.addItem(addStudyForm);
 		
 		window.show();
 		
 	}
 	
 	public void openProjectAdminTab(FoUser user){
 		Tab projectAdminTab;
 		projectAdminTab = new Tab("Manage Projects");
 		
 		projectAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip projectToolStrip = new ToolStrip();
 		projectToolStrip.setWidth100();
 		
 		ToolStripButton addProjectButton = new ToolStripButton();  
 		addProjectButton.setTitle("add Project");
 		if(user.getIsAdmin() == false){
 			addProjectButton.setDisabled(true);
 		}
 		addProjectButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadProjectManageWindow();
 			}});
 		
 		projectToolStrip.addButton(addProjectButton);
 		
 		ToolStripButton deleteProjectButton = new ToolStripButton();  
 		deleteProjectButton.setTitle("delete Project");
 		if(user.getIsAdmin() == false){
 			deleteProjectButton.setDisabled(true);
 		}
 		deleteProjectButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				final ListGridRecord lgr = projectGrid.getSelectedRecord();
 				
 				if (lgr != null) {
 				
 					SC.confirm("Do you really want to delete " + lgr.getAttribute("projectName") + "?", new BooleanCallback(){
 
 						@Override
 						public void execute(Boolean value) {
 							if(value != null && value){
 								
 								String projectId = lgr.getAttribute("projectId");
 								
 								projectAccessGrid.selectAllRecords();
 								projectAccessGrid.removeSelectedData();
 								
 								projectStudyGrid.selectAllRecords();
 								ListGridRecord[] lgrStudy = projectStudyGrid.getSelectedRecords();
 								
 								for(int i = 0; i < lgrStudy.length; i++){
 								
 									lgrStudy[i].setAttribute("projectId", projectId);
 								
 								}
 								
 								projectStudyGrid.removeSelectedData();
 								
 								projectGrid.removeData(lgr);
 							}
 						}
 					});
 				
 				} else {
 					SC.say("Select a project.");
 				}
 				
 			}});
 		
 		projectToolStrip.addButton(deleteProjectButton);
 		
 		
 		ToolStripButton addProjectAccessButton = new ToolStripButton();  
 		addProjectAccessButton.setTitle("add Project Access");
 		if(user.getIsAdmin() == false){
 			addProjectAccessButton.setDisabled(true);
 		}
 		addProjectAccessButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				ListGridRecord lgr = projectGrid.getSelectedRecord();
 				
 				if (lgr != null){
 				
 					FoProject project = new FoProject(Integer.parseInt(lgr.getAttribute("projectId")),
 																		lgr.getAttribute("projectName"),
 																		lgr.getAttribute("projectDescription"));
 					loadProjectAccessManageWindow(project);
 					
 				} else {
 					SC.say("Select a project.");
 				}
 			}});
 		
 		projectToolStrip.addButton(addProjectAccessButton);
 		
 		ToolStripButton removeProjectAccessButton = new ToolStripButton();  
 		removeProjectAccessButton.setTitle("remove Project Access");
 		if(user.getIsAdmin() == false){
 			removeProjectAccessButton.setDisabled(true);
 		}
 		removeProjectAccessButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				ListGridRecord projectAccessLgr = projectAccessGrid.getSelectedRecord();
 
 				if (projectAccessLgr != null){
 					
 					projectAccessGrid.removeData(projectAccessLgr);
 					
 				} else {
 					SC.say("Select a group.");
 				}
 				
 			}});
 		
 		projectToolStrip.addButton(removeProjectAccessButton);
 		
 		ToolStripButton addStudyToProjectButton = new ToolStripButton();  
 		addStudyToProjectButton.setTitle("add study to project");
 		if(user.getIsAdmin() == false){
 			addStudyToProjectButton.setDisabled(true);
 		}
 		addStudyToProjectButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				ListGridRecord lgr = projectGrid.getSelectedRecord();
 				
 				if (lgr != null){
 				
 					FoProject project = new FoProject(Integer.parseInt(lgr.getAttribute("projectId")),
 																		lgr.getAttribute("projectName"),
 																		lgr.getAttribute("projectDescription"));
 					loadAddStudyProjectWindow(project);
 					
 				} else {
 					SC.say("Select a project.");
 				}
 				
 			}});
 		
 		projectToolStrip.addButton(addStudyToProjectButton);
 		
 		controlsPanel.addMember(projectToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		projectGrid = new ListGrid();
 		projectGrid.setWidth("50%");
 		projectGrid.setHeight100();  
 		projectGrid.setAlternateRecordStyles(true);
 		projectGrid.setWrapCells(true);
 		projectGrid.setFixedRecordHeights(false);
 		projectGrid.setAutoFetchData(false);
 		projectGrid.setShowAllRecords(false);
 		projectGrid.markForRedraw();
 		
 		ListGridField lgfProjectId = new ListGridField("projectId", "Project ID");
 		ListGridField lgfProjectName = new ListGridField("projectName", "Project Name");
 		ListGridField lgfProjectActivated = new ListGridField("projectDescription", "Description");
 		
 		projectGrid.setFields(lgfProjectId, lgfProjectName, lgfProjectActivated);
 		
 		ProjectDS pDS = new ProjectDS();
 		
 		projectGrid.setDataSource(pDS);
 		projectGrid.setFetchOperation(OperationId.PROJECT_FETCH_ALL);
 		
 		projectGrid.fetchData();
 		
 		gridContainer.addMember(projectGrid);
 		
 		projectStudyGrid = new ListGrid();
 		projectStudyGrid.setWidth("50%");
 		projectStudyGrid.setHeight100();
 		projectStudyGrid.setAlternateRecordStyles(true);
 		projectStudyGrid.setWrapCells(true);
 		projectStudyGrid.setFixedRecordHeights(false);
 		projectStudyGrid.setShowAllRecords(false);
 		projectStudyGrid.setAutoFetchData(false);
 		projectStudyGrid.markForRedraw();
 		
 		StudyDS mDS = new StudyDS();
 		mDS.getField("cnv").setCanView(false);
 		mDS.getField("snp").setCanView(false);
 		mDS.getField("transloc").setCanView(false);
 		mDS.getField("generic").setCanView(false);
 		
 		projectStudyGrid.setDataSource(mDS);
 		projectStudyGrid.setFetchOperation(OperationId.STUDY_FETCH_FOR_PROJECT);
 		
 		gridContainer.addMember(projectStudyGrid);
 		
 		pane.addMember(gridContainer);
 		
 		if(user.getIsAdmin()){
 			projectAccessGrid = new ListGrid();
 			projectAccessGrid.setWidth100();
 			projectAccessGrid.setHeight("50%");
 			projectAccessGrid.setAlternateRecordStyles(true);
 			projectAccessGrid.setWrapCells(true);
 			projectAccessGrid.setFixedRecordHeights(false);
 			projectAccessGrid.setAutoFetchData(false);
 			projectAccessGrid.setShowAllRecords(false);
 			projectAccessGrid.markForRedraw();
 		
 			ListGridField lgfProjectAccessId = new ListGridField("projectAccessId", "ID");
 			ListGridField lgfProjectAccessGroup = new ListGridField("groupName", "Group");
 			ListGridField lgfProjectAccessRight = new ListGridField("accessRight", "Access Right");
 		
 			projectAccessGrid.setFields(lgfProjectAccessId, lgfProjectAccessGroup, lgfProjectAccessRight);
 		
 			ProjectAccessDS paDS = new ProjectAccessDS();
 			
 			projectAccessGrid.setDataSource(paDS);
 			
 			pane.addMember(projectAccessGrid);
 		}
 		
 		projectGrid.addRecordClickHandler(new MyProjectRecordClickHandler(projectStudyGrid, projectAccessGrid, user, cp));
 		
 		projectAdminTab.setPane(pane);
 		
 		/*
 		projectAdminTab.addTabSelectedHandler(new TabSelectedHandler(){
 
 			@Override
 			public void onTabSelected(TabSelectedEvent event) {
 				
 				if(projectGrid.getSelectedRecord() != null){
 					
 					String projectId = projectGrid.getSelectedRecord().getAttribute("projectId");
 				
 					projectStudyGrid.setFetchOperation(OperationId.STUDY_FETCH_FOR_PROJECT);
 					
 					//TODO Find oput why this does not work.
 					
 					projectStudyGrid.fetchData(new Criteria("projectId", projectId));
 				}
 			}
 		});
 		*/
 		centerTabSet.addTab(projectAdminTab);
 		
 		centerTabSet.selectTab(projectAdminTab);
 		
 	}
 	
 	public void loadUploadWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Upload Files");
 		window.setWidth(400);
 		window.setHeight(300);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 			
 		window.addCloseClickHandler(new com.smartgwt.client.widgets.events.CloseClickHandler(){
 
 			@Override
 			public void onCloseClick(CloseClickEvent event) {
 				fileGrid.invalidateCache();
 				fileGrid.fetchData();
 				Window w = (Window) event.getSource();
 				w.clear();
 			}
 		});
 		
 		
 		MultiUploader defaultUploader = new MultiUploader();
 		defaultUploader.setHeight("100%");
 		
 		defaultUploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
 		    public void onFinish(IUploader uploader) {
 		      if (uploader.getStatus() == Status.SUCCESS) {
 		        UploadedInfo info = uploader.getServerInfo();
 		      }
 		    }
 		  });
 	
 		window.addItem(defaultUploader);
 		
 		window.show();
 	}
 	
 	public void openDataAdminTab(){
 		Tab dataAdminTab;
 		
 		dataAdminTab = new Tab("Data Import");
 		dataAdminTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		HLayout header = new HLayout();
 		header.setWidth100();
 		header.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip importToolStrip = new ToolStrip();
 		importToolStrip.setWidth100();
 		
 		ToolStripButton addUploadButton = new ToolStripButton();  
 		addUploadButton.setTitle("Upload Files");
 		addUploadButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadUploadWindow();
 			}});
 		
 		importToolStrip.addButton(addUploadButton);
 		
 		ToolStripButton deleteUploadButton = new ToolStripButton();  
 		deleteUploadButton.setTitle("Delete Files");
 		deleteUploadButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				if(fileGrid.getSelectedRecords().length > 0){
 					fileGrid.removeSelectedData();
 				} else {
 					SC.say("Select at least one file for deletion.");
 				}
 				
 			}});
 		
 		importToolStrip.addButton(deleteUploadButton);
 		
 		controlsPanel.addMember(importToolStrip);
 		
 		VLayout body = new VLayout();
 		body.setWidth100();
 		body.setHeight100();
 		body.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		//Grid
 		fileGrid = new ListGrid();
 		fileGrid.setWidth100();
 		fileGrid.setHeight("50%");  
 		fileGrid.setShowRowNumbers(true);
 		fileGrid.setAlternateRecordStyles(true);
 		fileGrid.setEditByCell(true);
 		fileGrid.setEditEvent(ListGridEditEvent.CLICK);
 		fileGrid.setWrapCells(true);
 		fileGrid.setFixedRecordHeights(false);
 		fileGrid.setSelectionType(SelectionStyle.MULTIPLE);
 		fileGrid.setCanDragSelect(true);
 		fileGrid.setShowAllRecords(false);
 		
 		/*
 		fileGrid.setEditorCustomizer(new ListGridEditorCustomizer(){
 			
 			@Override
 			public FormItem getEditor(final ListGridEditorContext context) {
 				
 				ListGridField field = context.getEditField();  
 				
 				//field.setDisplayField("studyName");
 				//field.setValueField("studyId");
 				
 				SelectItem selectItemExperiments = null;
 				
 				if (field.getName().equals("studyName")) {  
 				
 				selectItemExperiments = new SelectItem();
 				selectItemExperiments.setTitle("");
 				selectItemExperiments.setDisplayField("studyName");
 				selectItemExperiments.setValueField("studyId");
 				selectItemExperiments.setAutoFetchData(false);
 				StudyDS mDS = new StudyDS();
 				
 				selectItemExperiments.setOptionDataSource(mDS);
 				selectItemExperiments.setOptionOperationId(OperationId.STUDY_FETCH_FOR_PROJECT);
 				
 				selectItemExperiments.setDefaultToFirstOption(true);
 				
 				selectItemExperiments.setPickListCriteria(new Criteria("projectId", selectItemProjects.getValue().toString()));
 				
 				}
 				
 				return selectItemExperiments;
 			}
 		});
 		*/
 		//ListGridField fileField = new ListGridField("file", "File");
 		//ListGridField nameField = new ListGridField("studyName", "Study Name");
 		//fileGrid.setFields(fileField, nameField);
 
 		FileImportDS fiDS = new FileImportDS();
 		
 		fileGrid.setDataSource(fiDS);
 		
 		fileGrid.fetchData();
 		
 		HLayout importOptions = new HLayout();
 		importOptions.setWidth100();
 		importOptions.setHeight("50%");
 		importOptions.setAlign(Alignment.CENTER);
 		
 		//dynamic form
 		DynamicForm importOptionsForm = new DynamicForm();
 		importOptionsForm.setWidth("250px");
 		importOptionsForm.setAlign(Alignment.CENTER);
 		
 		cbItemFilterType = new ComboBoxItem();
 		cbItemFilterType.setTitle("Data Type");
 		
 		cbItemFilterType.setDisplayField("featureType");
 		cbItemFilterType.setValueField("featureType");
 		cbItemFilterType.setAutoFetchData(false);
 		FeatureDS fDS = new FeatureDS();
 		
 		cbItemFilterType.setOptionDataSource(fDS);
 		cbItemFilterType.setOptionOperationId(OperationId.FEATURE_FETCH_TYPES);
 		
 		cbItemFilterType.setDefaultToFirstOption(true);
 		
 		createStudyItem = new RadioGroupItem();
 		createStudyItem.setTitle("");
 		createStudyItem.setValueMap("Create new study", "Import to existing study");
 		createStudyItem.setDefaultValue("Create new study");
 		createStudyItem.addChangedHandler(new ChangedHandler(){
 
 			@Override
 			public void onChanged(ChangedEvent event) {
 				String val = event.getValue().toString();
 				
 				if(val.equals("Create new study")) {
 					selectItemProjects.show();
 					selectItemTissues.show();
 					selectItemPlatform.show();
 					selectItemGenomeAssembly.show();
 				}
 				if(val.equals("Import to existing study")) {
 					selectItemProjects.hide();
 					selectItemTissues.hide();
 					selectItemPlatform.hide();
 					selectItemGenomeAssembly.hide();
 				}
 			}
 		});
 		
 		selectItemProjects = new SelectItem();
 		selectItemProjects.setTitle("Project");
 		selectItemProjects.setDisplayField("projectName");
 		selectItemProjects.setValueField("projectId");
 		selectItemProjects.setAutoFetchData(false);
 		ProjectDS pDS = new ProjectDS();
 		
 		selectItemProjects.setOptionDataSource(pDS);
 		selectItemProjects.setOptionOperationId(OperationId.PROJECT_FETCH_READ_WRITE);
 		
 		selectItemProjects.setDefaultToFirstOption(true);
 		
 		selectItemTissues = new SelectItem();
 		selectItemTissues.setTitle("Tissue");
 		selectItemTissues.setDisplayField("organNamePlusType");
 		selectItemTissues.setValueField("organId");
 		selectItemTissues.setAutoFetchData(false);
 		OrganDS oDS = new OrganDS();
 		
 		selectItemTissues.setOptionDataSource(oDS);
 		selectItemTissues.setOptionOperationId(OperationId.ORGAN_FETCH_ENABLED);
 		
 		selectItemTissues.setDefaultToFirstOption(true);
 		
 		selectItemPlatform = new SelectItem();
 		selectItemPlatform.setTitle("Platform");
 		selectItemPlatform.setDisplayField("platformName");
 		selectItemPlatform.setValueField("platformId");
 		selectItemPlatform.setAutoFetchData(false);
 		
 		PlatformDS plDS = new PlatformDS();
 		
 		selectItemPlatform.setOptionDataSource(plDS);
 		selectItemPlatform.setOptionOperationId(OperationId.PLATFORM_FETCH_ALL);
 		selectItemPlatform.setDefaultToFirstOption(true);
 		
 		selectItemGenomeAssembly = new SelectItem();
 		selectItemGenomeAssembly.setTitle("Genome Assembly");
 		selectItemGenomeAssembly.setValueMap("GrCh37", "ncbi36");
 		selectItemGenomeAssembly.setDefaultToFirstOption(true);
 		
 		batchCheckbox = new CheckboxItem();
 		batchCheckbox.setTitle("Batch import");
 		batchCheckbox.setValue(false);
 		
 		ButtonItem importButton = new ButtonItem("Import");
 		importButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord[] lgrs = fileGrid.getRecords();
 				
 				FoStudy[] studies = new FoStudy[lgrs.length];
 				
 				for(int i = 0; i < lgrs.length; i++){
 					studies[i] = new FoStudy();
 					studies[i].setName(lgrs[i].getAttributeAsString("studyName"));
 					studies[i].setFiles(new String[]{lgrs[i].getAttributeAsString("fileName")});
 					
 					if(createStudyItem.getValueAsString().equals("Create new study")){
 						studies[i].setAssembly(selectItemGenomeAssembly.getValueAsString());
 						studies[i].setDescription("");
 						studies[i].setOrganId(Integer.parseInt(selectItemTissues.getValue().toString()));
 						studies[i].setPlatformId(Integer.parseInt(selectItemPlatform.getValue().toString()));
 						studies[i].setPropertyIds(new int[]{});
 					}
 				}
 				
 				//TODO serach for solution to async import with progressbar.
 				if(batchCheckbox.getValueAsBoolean()) {
 					// Import automatically
 					//for(int i = 0; i < studies.length; i++) {
 						if(createStudyItem.getValueAsString().equals("Create new study")) {
 							// create study and import data
 							
 							
 							
 							importData(studies,
 										cbItemFilterType.getValueAsString(),
 										cbItemFilterType.getSelectedRecord().getAttribute("type"),
 										true,
 										Integer.parseInt(selectItemProjects.getValue().toString()),
 										"",
 										studies.length, 
 										studies.length);
 						}
 						if(createStudyItem.getValueAsString().equals("Import to existing study")) {
 							// import data into existing study
 							importData(studies,
 									cbItemFilterType.getValueAsString(), 
 									cbItemFilterType.getSelectedRecord().getAttribute("type"),
 									false,
 									Integer.parseInt(selectItemProjects.getValue().toString()),
 									"",
 									studies.length, 
 									studies.length);
 						}
 					//}
 					
 				} else {
 					// Manual import
 					if(studies.length > 0){
 						ManualImportWindow miw = null;
 						if(createStudyItem.getValueAsString().equals("Create new study")) {
 							miw = new ManualImportWindow(studies,
 										cbItemFilterType.getValueAsString(),
 										cbItemFilterType.getSelectedRecord().getAttribute("type"),
 										Integer.parseInt(selectItemProjects.getValue().toString()),
 										true,
 										fileGrid,
 										cp);
 							miw.show();
 					}
 						if(createStudyItem.getValueAsString().equals("Import to existing study")) {
 							miw = new ManualImportWindow(studies,
 										cbItemFilterType.getValueAsString(),
 										cbItemFilterType.getSelectedRecord().getAttribute("type"),
 										Integer.parseInt(selectItemProjects.getValue().toString()),
 										false,
 										fileGrid,
 										cp);
 							miw.show();
 						}
 					} else {
 						SC.say("You need at least one file to import.");
 					}
 				}
 			}
 		});
 		
 		importOptionsForm.setFields(cbItemFilterType,
 									createStudyItem,
 									selectItemProjects,
 									selectItemTissues,
 									selectItemPlatform,
 									selectItemGenomeAssembly,
 									batchCheckbox,
 									importButton);
 		
 		importOptions.addMember(importOptionsForm);
 		
 		body.addMember(fileGrid);
 		body.addMember(importOptions);
 		
 		header.addMember(controlsPanel);
 		
 		pane.addMember(header);	
 		
 		pane.addMember(body);
 		
 		dataAdminTab.setPane(pane);
 				
 		centerTabSet.addTab(dataAdminTab);
 		
 		centerTabSet.selectTab(dataAdminTab);
 	}
 	
 	public void loadEnsemblManageWindow(){
 		
 		final Window window = new Window();
 
 		window.setTitle("Add Ensembl Database");
 		window.setWidth(250);
 		window.setHeight(150);
 		window.setAlign(Alignment.CENTER);
 		
 		window.setAutoCenter(true);
 		window.setIsModal(true);
 		window.setShowModalMask(true);
 		
 		DynamicForm ensemblForm = new DynamicForm();
 		
 		dbNameTextItem = new TextItem();
 		dbNameTextItem.setTitle("Database Name");
 		
 		dbLabelTextItem = new TextItem();
 		dbLabelTextItem.setTitle("Database Label");
 		
 		dbVersionTextItem = new TextItem();
 		dbVersionTextItem.setTitle("Database Version");
 		
 		ButtonItem addDBButton = new ButtonItem("Add");
 		addDBButton.setWidth(50);
 		
 		addDBButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("ensemblDBName", dbNameTextItem.getDisplayValue());
 				lgr.setAttribute("ensemblDBLabel", dbLabelTextItem.getDisplayValue());
 				lgr.setAttribute("ensemblDBVersion", dbVersionTextItem.getDisplayValue());
 				
 				ensemblGrid.addData(lgr);
 				
 				window.hide();
 			}
 		});
 
 		ensemblForm.setItems(dbNameTextItem, dbLabelTextItem, dbVersionTextItem, addDBButton);
 	
 		window.addItem(ensemblForm);
 		
 		window.show();
 	}
 	
 	public void openEnsemblConfigTab(){
 		
 		Tab ensemblConfigTab = new Tab("Ensembl Databases");
 		ensemblConfigTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout headerContainer = new VLayout();
 		headerContainer.setDefaultLayoutAlign(Alignment.CENTER);
 		headerContainer.setWidth100();
 		headerContainer.setAutoHeight();
 		
 		HLayout controlsPanel = new HLayout();
 		controlsPanel.setWidth100();
 		controlsPanel.setAutoHeight();
 		
 		ToolStrip ensemblToolStrip = new ToolStrip();
 		ensemblToolStrip.setWidth100();
 		
 		ToolStripButton addEDBButton = new ToolStripButton();
 		addEDBButton.setTitle("add Database");
 		addEDBButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loadEnsemblManageWindow();
 			}});
 		
 		ensemblToolStrip.addButton(addEDBButton);
 		
 		ToolStripButton deleteEDBButton = new ToolStripButton();  
 		deleteEDBButton.setTitle("delete Database");
 		deleteEDBButton.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				final ListGridRecord lgr = ensemblGrid.getSelectedRecord();
 				
 				if (lgr != null) {
 				
 					SC.confirm("Do you really want to delete " + lgr.getAttribute("ensemblDBLabel") + "?", new BooleanCallback(){
 
 						@Override
 						public void execute(Boolean value) {
 							if(value != null && value){
 						
 								ensemblGrid.removeData(lgr);
 							}
 						}
 					});
 				
 				} else {
 					SC.say("Select a database.");
 				}
 			}});
 		
 		ensemblToolStrip.addButton(deleteEDBButton);
 		
 		
 		controlsPanel.addMember(ensemblToolStrip);
 		
 		headerContainer.addMember(controlsPanel);
 		
 		pane.addMember(headerContainer);
 		
 		HLayout gridContainer = new HLayout();
 		gridContainer.setWidth100();
 		gridContainer.setHeight100();
 		
 		ensemblGrid = new ListGrid();
 		ensemblGrid.setWidth100();
 		ensemblGrid.setHeight100();
 		ensemblGrid.setAlternateRecordStyles(true);
 		ensemblGrid.setWrapCells(true);
 		ensemblGrid.setFixedRecordHeights(false);
 		ensemblGrid.setShowAllRecords(false);
 		ensemblGrid.setAutoFetchData(false);
 		
 		EnsemblDBDS edbDS = new EnsemblDBDS();
 		
 		ensemblGrid.setDataSource(edbDS);
 		ensemblGrid.fetchData();
 		
 		gridContainer.addMember(ensemblGrid);
 		
 		pane.addMember(gridContainer);
 		
 		ensemblConfigTab.setPane(pane);
 		
 		centerTabSet.addTab(ensemblConfigTab);
 		
 		centerTabSet.selectTab(ensemblConfigTab);
 		
 	}
 	
 	public void openDatabaseConfigTab(DBConfigData dbdata){
 		Tab DatabaseConfigTab;
 		DatabaseConfigTab = new Tab("Database Configuration");
 		DatabaseConfigTab.setCanClose(true);
 		
 		VLayout pane = new VLayout();
 		pane.setWidth100();
 		pane.setHeight100();
 		pane.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		HLayout header = new HLayout();
 		header.setAutoWidth();
 		header.setAutoHeight();
 		
 		Label headerLbl = new Label("<h2>Configure Database Connections</h2>");
 		headerLbl.setWidth("300");
 		header.addMember(headerLbl);
 		
 		pane.addMember(header);
 	    
 	    DynamicForm dataForm = new DynamicForm();
 	    dataForm.setWidth("100");
 	    dataForm.setHeight("25");
 	    
 	    HeaderItem ensemblHeaderItem =  new HeaderItem();
 	    ensemblHeaderItem.setDefaultValue("Ensembl Connection Data");
 	    
 	    ensemblHost = new TextItem();
 	    ensemblHost.setTitle("Host");
 	    ensemblHost.setValue(dbdata.getEhost());
 	    
 	    ensemblPort = new TextItem();
 	    ensemblPort.setTitle("Port");
 	    ensemblPort.setValue(dbdata.getEport());
 	    
 	    ensemblDatabase = new TextItem();
 	    ensemblDatabase.setTitle("Database");
 	    ensemblDatabase.setValue(dbdata.getEdb());
 	    
 	    ensemblUser = new TextItem();
 	    ensemblUser.setTitle("User");
 	    ensemblUser.setValue(dbdata.getEuser());
 	    
 	    ensemblPW = new PasswordItem();
 	    ensemblPW.setTitle("Password");
 	    ensemblPW.setValue(dbdata.getEpw());
 		
 	    HeaderItem fishoracleHeaderItem =  new HeaderItem();
 	    fishoracleHeaderItem.setDefaultValue("Fish Oracle Connection Data"); 
 	    
 	    fishoracleHost = new TextItem();
 	    fishoracleHost.setTitle("Host");
 	    fishoracleHost.setValue(dbdata.getFhost());
 	    
 	    fishoracleDatabase = new TextItem();
 	    fishoracleDatabase.setTitle("Database");
 	    fishoracleDatabase.setValue(dbdata.getFdb());
 	    
 	    fishoracleUser = new TextItem();
 	    fishoracleUser.setTitle("User");
 	    fishoracleUser.setValue(dbdata.getFuser());
 	    
 	    fishoraclePW = new PasswordItem();
 	    fishoraclePW.setTitle("Password");
 	    fishoraclePW.setValue(dbdata.getFpw());
 	    
 	    ButtonItem submitButton = new ButtonItem("submit");
 		submitButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				DBConfigData dbcd = new DBConfigData(ensemblHost.getDisplayValue(),
 														Integer.parseInt(ensemblPort.getDisplayValue()),
 														ensemblDatabase.getDisplayValue(),
 														ensemblUser.getDisplayValue(),
 														ensemblPW.getDisplayValue(),
 														fishoracleHost.getDisplayValue(),
 														fishoracleDatabase.getDisplayValue(),
 														fishoracleUser.getDisplayValue(),
 														fishoraclePW.getDisplayValue());
 				storedbConfigData(dbcd);
 			}
 			
 		});
 	    
 	    dataForm.setItems(ensemblHeaderItem,
 	    					ensemblHost,
 	    					ensemblPort,
 	    					ensemblDatabase,
 	    					ensemblUser,
 	    					ensemblPW,
 	    					fishoracleHeaderItem,
 	    					fishoracleHost,
 	    					fishoracleDatabase,
 	    					fishoracleUser,
 	    					fishoraclePW,
 	    					submitButton);
 	    
 	    pane.addMember(dataForm);
 	    
 	    DatabaseConfigTab.setPane(pane);
 	    
 	    centerTabSet.addTab(DatabaseConfigTab);
 		
 		centerTabSet.selectTab(DatabaseConfigTab);
 	}
 	
 	/*=============================================================================
 	 *||                              RPC Calls                                  ||
 	 *=============================================================================
 	 * */
 		
 	public void imageRedraw(GWTImageInfo imgInfo){
 			
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<GWTImageInfo> callback = new AsyncCallback<GWTImageInfo>(){
 			public void onSuccess(GWTImageInfo result){
 				
 				Canvas[] tabContents = cp.getCenterTabSet().getSelectedTab().getPane().getChildren();
 				ImgCanvas imgLayer = (ImgCanvas) tabContents[1];
 				
 				TextItem chromosome = imgLayer.getChromosome();
 				TextItem start = imgLayer.getStart();
 				TextItem end = imgLayer.getEnd();
 				
 				imgLayer.removeFromParent();
 				imgLayer.destroy();
 				
 				chromosome.setValue(result.getChromosome());
 				start.setValue(result.getStart());
 				end.setValue(result.getEnd());
 				
 				ImgCanvas newImgLayer = cp.createImageLayer(result);
 				
 				newImgLayer.setChromosome(chromosome);
 				newImgLayer.setStart(start);
 				newImgLayer.setEnd(end);
 				
 				VLayout presentationLayer = (VLayout) cp.getCenterTabSet().getSelectedTab().getPane();
 				
 				presentationLayer.addMember(newImgLayer);
 
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say(caught.getMessage());
 			}
 		};
 		req.redrawImage(imgInfo, callback);
 	}
 	
 	public void exportImage(GWTImageInfo imgInfo){
 		
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<GWTImageInfo> callback = new AsyncCallback<GWTImageInfo>(){
 			public void onSuccess(GWTImageInfo result){
 				
 				Window window = new Window();
 				window.setTitle("export image as " + result.getQuery().getImageType() +  " document");
 				window.setAutoCenter(true);
 				window.setWidth(160);
 				window.setHeight(100);
 				
 				DynamicForm downloadForm = new DynamicForm();
 				downloadForm.setPadding(25);
 				
 				LinkItem link = new LinkItem();
 				link.setValue(result.getImgUrl());
 				link.setLinkTitle("download");
 				link.setAlign(Alignment.CENTER);
 				link.setShowTitle(false);
 				
 				downloadForm.setItems(link);
 				
 				window.addItem(downloadForm);
 				
 				window.show();
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say("Nothing found!");
 			}
 		};
 		req.redrawImage(imgInfo, callback);
 	}
 	
 	public void storedbConfigData(DBConfigData data){
 	
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>(){
 			@Override
 			public void onSuccess(Boolean result){
 				
 				SC.say("Database connection parameters stored.");
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say(caught.getMessage());
 			}
 		};
 		req.writeConfigData(data, callback);
 	}
 	
 	public void showAllGroups(){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoGroup[]> callback = new AsyncCallback<FoGroup[]>(){
 			
 			public void onSuccess(FoGroup[] result){
 				
 				FoGroup[] groups = result;
 				
 				groupGrid.addRecordClickHandler(new MyGroupRecordClickHandler(groupUserGrid, cp));
 								
 				ListGridRecord[] lgr = new ListGridRecord[groups.length];
 				
 				for(int i=0; i < groups.length; i++){
 					lgr[i] = new ListGridRecord();
 					lgr[i].setAttribute("groupId", groups[i].getId());
 					lgr[i].setAttribute("groupName", groups[i].getName());
 					lgr[i].setAttribute("isactive", groups[i].isIsactive());
 				}
 
 				groupGrid.setData(lgr);
 				
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 
 		};
 		req.getAllFoGroups(callback);
 	}
 	
 	public void deleteGroup(FoGroup foGroup){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<Void> callback = new AsyncCallback<Void>(){
 			
 			public void onSuccess(Void result){
 				
 				groupGrid.removeData(groupGrid.getSelectedRecord());
 				groupGrid.getRecords();
 				
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 
 		};
 		req.deleteGroup(foGroup, callback);
 	}
 	
 	public void addGroup(FoGroup foGroup){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoGroup> callback = new AsyncCallback<FoGroup>(){
 			
 			public void onSuccess(FoGroup result){
 				FoGroup group = result;
 				
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("groupId", group.getId());
 				lgr.setAttribute("groupName", group.getName());
 				lgr.setAttribute("isactive", group.isIsactive());
 				
 				groupGrid.addData(lgr);
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 
 		};
 		req.addGroup(foGroup, callback);
 	}
 	
 	public void getUserObject(final String forWhat){
 		
 		final UserServiceAsync req = (UserServiceAsync) GWT.create(UserService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "UserService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoUser[]> callback = new AsyncCallback<FoUser[]>(){
 			
 			public void onSuccess(FoUser[] result){
 				
 				if(forWhat.equals("ProjectAdminTab")){
 					openProjectAdminTab(result[0]);
 				}
 				if(forWhat.equals("StudyAdminTab")){
 					openStudyAdminTab(result[0]);
 				}
 				
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 		};
 		req.getSessionUserObject(callback);
 	}
 	
 	
 	public void getAllUsersExceptGroup(FoGroup foGroup){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoUser[]> callback = new AsyncCallback<FoUser[]>(){
 			
 			public void onSuccess(FoUser[] result){
 				
 				 LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
 				 
 				 for(int i=0; i< result.length; i++){
 					 valueMap.put(new Integer(result[i].getId()).toString(), result[i].getUserName());
 				 }
 			     
 				 userSelectItem.setValueMap(valueMap);
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 		};
 		req.getAllUsersExceptFoGroup(foGroup, callback);
 	}
 	
 	public void getUsersForGroup(int groupId){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoUser[]> callback = new AsyncCallback<FoUser[]>(){
 			
 			public void onSuccess(FoUser[] result){
 				
 				FoUser[] users = result;
 				ListGridRecord[] userLgr = null;
 				
 				if(users != null){
 					userLgr = new ListGridRecord[users.length];
 				
 					for(int i=0; i < users.length; i++){
 						userLgr[i] = new ListGridRecord();
 						userLgr[i].setAttribute("userId", users[i].getId());
 						userLgr[i].setAttribute("userName", users[i].getUserName());
 					}
 				}
 				
 				groupUserGrid.setData(userLgr);
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 		};
 		req.getUsersForGroup(groupId, callback);
 	}
 	
 	public void addUserToGroup(FoGroup foGroup, int userId){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoUser> callback = new AsyncCallback<FoUser>(){
 			
 			public void onSuccess(FoUser result){
 
 				ListGridRecord lgr = new ListGridRecord();
 				lgr.setAttribute("userId", result.getId());
 				lgr.setAttribute("userName", result.getUserName());
 				
 				groupUserGrid.addData(lgr);
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 		};
 		req.addUserToFoGroup(foGroup, userId, callback);
 	}
 	
 	public void removeUserFromGroup(int groupId, int userId){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>(){
 			
 			public void onSuccess(Boolean result){
 				groupUserGrid.removeData(groupUserGrid.getSelectedRecord());
 				groupUserGrid.getRecords();
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 		};
 		req.removeUserFromFoGroup(groupId, userId, callback);
 	}
 	
 	public void getAllGroupsExceptProject(FoProject foProject){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoGroup[]> callback = new AsyncCallback<FoGroup[]>(){
 			
 			public void onSuccess(FoGroup[] result){
 				
 				 LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
 				 
 				 for(int i=0; i< result.length; i++){
 					 valueMap.put(new Integer(result[i].getId()).toString(), result[i].getName());
 				 }
 				 
 				 groupSelectItem.setValueMap(valueMap);
 			}
 			public void onFailure(Throwable caught){
 				SC.say(caught.getMessage());
 			}
 		};
 		req.getAllGroupsExceptFoProject(foProject, callback);
 	}
 	
 	public void importData(FoStudy[] foStudy,
 							String importType,
 							String importSubType,
 							boolean createStudy,
 							int projectId,
 							String tool,
 							int importNumber,
 							int nofImports){
 		
 		final AdminAsync req = (AdminAsync) GWT.create(Admin.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "AdminService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<int[]> callback = new AsyncCallback<int[]>(){
 			@Override
 			public void onSuccess(int[] result){
 				
 				/*
 				if(window != null){
 					window.updateValues(result[0], result[1]);
 				}
 				*/
 				
 				window.hide();
 				fileGrid.invalidateCache();
 				fileGrid.fetchData();
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say(caught.getMessage());
 			}
 		};
 		
 		if(batchCheckbox.getValueAsBoolean()){
 			/*
 			window = new ProgressWindow(0, nofImports);
 			window.addCloseClickHandler(new com.smartgwt.client.widgets.events.CloseClickHandler(){
 
 						@Override
 						public void onCloseClick(CloseClickEvent event) {
 							fileGrid.invalidateCache();
 							fileGrid.fetchData();
 							Window w = (Window) event.getSource();
 							w.clear();
 						}
 					});
 		*/
 			window = new Window();
 			window.setTitle("BatchImport");
 			window.setAlign(Alignment.CENTER);
 			
 			window.setAutoCenter(true);
 			window.setIsModal(true);
 			window.setShowModalMask(true);
 			Label lbl = new Label("Import Data. This may take upto several minutes.");
 			window.addItem(lbl);
 			window.show();
 		}
 		
 		req.importData(foStudy,
 						importType,
 						importSubType,
 						createStudy,
 						projectId,
 						tool,
 						importNumber,
 						nofImports,
 						callback);
 	}
 }
 
 class ManualImportWindow extends Window {
 	
 	private TextItem textItemStudyName;
 	private SelectItem selectItemProjects;
 	private SelectItem selectItemTissues;
 	private SelectItem selectItemPlatform;
 	private SelectItem selectItemGenomeAssembly;
 	private TextAreaItem textItemDescription;
 	private SelectItem selectItemSNPTool;
 	private SelectItem selectItemProperty;
 	private ButtonItem submitButton;
 	private int fileNumber;
 	private int nofFiles;
 	private ManualImportWindow self;
 	private ListGrid listGrid;
 	private CenterPanel cp;
 	
 	public ManualImportWindow(final FoStudy[] studies,
 							final String importType,
 							final String dataSubType,
 							final int projectId,
 							final boolean createStudy,
 							ListGrid lg,
 							CenterPanel scp){
 		super();
 		self = this;
 		this.listGrid = lg;
 		this.cp = scp;
 		
 		fileNumber = 0;
 		nofFiles = studies.length;
 
 		this.setTitle("Import file " + (fileNumber + 1) + " of " + studies.length);
 		this.setWidth(300);
 		this.setHeight(400);
 		this.setAlign(Alignment.CENTER);
 		
 		this.setAutoCenter(true);
 		this.setIsModal(true);
 		this.setShowModalMask(true);
 		
 		DynamicForm importOptionsForm = new DynamicForm();
 		importOptionsForm.setWidth100();
 		importOptionsForm.setHeight100();
 		importOptionsForm.setAlign(Alignment.CENTER);
 		
 		textItemStudyName = new TextItem();
 		textItemStudyName.setTitle("Study Name");
 		textItemStudyName.setValue(studies[0].getName());
 		
 		
 		selectItemProjects = new SelectItem();
 		selectItemProjects.setTitle("Project");
 		selectItemProjects.setDisplayField("projectName");
 		selectItemProjects.setValueField("projectId");
 		selectItemProjects.setAutoFetchData(false);
 		ProjectDS pDS = new ProjectDS();
 		
 		selectItemProjects.setOptionDataSource(pDS);
 		selectItemProjects.setOptionOperationId(OperationId.PROJECT_FETCH_ALL);
 		
 		selectItemProjects.setDefaultValue(projectId);
 		
 		if(!createStudy){
 			selectItemProjects.setVisible(false);
 		}
 		
 		selectItemTissues = new SelectItem();
 		selectItemTissues.setTitle("Tissue");
 		selectItemTissues.setDisplayField("organNamePlusType");
 		selectItemTissues.setValueField("organId");
 		selectItemTissues.setAutoFetchData(false);
 		OrganDS oDS = new OrganDS();
 		
 		selectItemTissues.setOptionDataSource(oDS);
 		selectItemTissues.setOptionOperationId(OperationId.ORGAN_FETCH_ENABLED);
 		
 		selectItemTissues.setDefaultValue(studies[0].getOrganId());
 		
 		if(!createStudy){
 			selectItemTissues.setVisible(false);
 		}
 		
 		selectItemPlatform = new SelectItem();
 		selectItemPlatform.setTitle("Platform");
 		selectItemPlatform.setDisplayField("platformName");
 		selectItemPlatform.setValueField("platformId");
 		selectItemPlatform.setAutoFetchData(false);
 		
 		PlatformDS plDS = new PlatformDS();
 		
 		selectItemPlatform.setOptionDataSource(plDS);
 		selectItemPlatform.setOptionOperationId(OperationId.PLATFORM_FETCH_ALL);
 		selectItemPlatform.setDefaultValue(studies[0].getPlatformId());
 		
 		if(!createStudy){
 			selectItemPlatform.setVisible(false);
 		}
 		
 		selectItemGenomeAssembly = new SelectItem();
 		selectItemGenomeAssembly.setTitle("Genome Assembly");
 		selectItemGenomeAssembly.setValueMap("GrCh37", "ncbi36");
 		selectItemGenomeAssembly.setDefaultValue(studies[0].getAssembly());
 		
 		if(!createStudy){
 			selectItemGenomeAssembly.setVisible(false);
 		}
 		
 		textItemDescription = new TextAreaItem();
 		textItemDescription.setTitle("Description");
 		textItemDescription.setDefaultValue("");
 		
 		if(!createStudy){
 			textItemDescription.setVisible(false);
 		}
 		
 		selectItemSNPTool = new SelectItem();
 		selectItemSNPTool.setTitle("SNP Tool");
 		selectItemSNPTool.setValueMap("gatk", "varscan", "SNVMix", "samtools");
 		if(!importType.equals(FoConstants.SNV)){
 			selectItemSNPTool.setVisible(false);
 		}
 		
 		selectItemProperty = new SelectItem();
 		selectItemProperty.setTitle("Property");
 		selectItemProperty.setDisplayField("propertyNamePlusType");
 		selectItemProperty.setValueField("propertyId");
 		selectItemProperty.setAutoFetchData(false);
 		
 		PropertyDS prlDS = new PropertyDS();
 		
 		selectItemProperty.setOptionDataSource(prlDS);
 		selectItemProperty.setOptionOperationId(OperationId.PROPERTY_FETCH_ENABLED);
 		
 		ButtonItem cancelButton = new ButtonItem("cancel");
 		cancelButton.setEndRow(false);
 		cancelButton.setAlign(Alignment.RIGHT);
 		cancelButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 			
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				if(fileNumber + 1 < nofFiles){
 					
 					fileNumber++;
 					
 					self.setTitle("Import file " + (fileNumber + 1) + " of " + studies.length);
 					
 					textItemStudyName.setValue(studies[fileNumber].getName());
 					selectItemProjects.setValue(projectId);
 					selectItemTissues.setValue(studies[fileNumber].getOrganId());
 					selectItemPlatform.setValue(studies[fileNumber].getPlatformId());
 					selectItemGenomeAssembly.setValue(studies[fileNumber].getAssembly());
 					textItemDescription.setValue("");
 					selectItemSNPTool.setValue("");
 					selectItemProperty.clearValue();
 					
 					if(fileNumber + 1 == nofFiles){
 						submitButton.setTitle("finish");
 					}
 					
 				} else {
 					listGrid.invalidateCache();
 					listGrid.fetchData();
 					self.clear();
 				}	
 			}
 		});
 		
 		submitButton = new ButtonItem("next");
 		submitButton.setStartRow(false);
 		
 		submitButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
 			
 			@Override
 			public void onClick(
 					com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
 				
 				if(fileNumber < nofFiles){
 					
 					FoStudy s = new FoStudy();
 					s.setName(textItemStudyName.getValueAsString());
 					s.setFiles(studies[fileNumber].getFiles());
 					
 					if(createStudy){
 						s.setAssembly(selectItemGenomeAssembly.getValueAsString());
 						s.setDescription(textItemDescription.getValueAsString());
 						s.setOrganId(Integer.parseInt(selectItemTissues.getValue().toString()));
 						s.setPlatformId(Integer.parseInt(selectItemPlatform.getValue().toString()));
 						String[] strPIds = selectItemProperty.getValues();
 						int[] intPIds = new int[strPIds.length];
 						for(int k=0; k < strPIds.length; k++){
 							intPIds[k] = Integer.parseInt(strPIds[k]);
 						}
 						s.setPropertyIds(intPIds);
 					}
 					
 					cp.importData(new FoStudy[]{s},
 									importType,
 									dataSubType,
 									createStudy,
 									Integer.parseInt(selectItemProjects.getValue().toString()),
 									selectItemSNPTool.getValueAsString(),
 									fileNumber + 1,
 									nofFiles);
 					
 					if(fileNumber + 1 == nofFiles){
 						listGrid.invalidateCache();
 						listGrid.fetchData();
 						self.clear();
 					}
 					
 					if(fileNumber + 1 < nofFiles) {
 						
 						fileNumber++;
 					
 						self.setTitle("Import file " + (fileNumber + 1) + " of " + studies.length);
 					
 						textItemStudyName.setValue(studies[fileNumber].getName());
 						selectItemProjects.setValue(projectId);
 						selectItemTissues.setValue(studies[fileNumber].getOrganId());
 						selectItemPlatform.setValue(studies[fileNumber].getPlatformId());
 						selectItemGenomeAssembly.setValue(studies[fileNumber].getAssembly());
 						textItemDescription.setValue("");
 						selectItemSNPTool.setValue("");
 						selectItemProperty.clearValue();
 					
 					}
 					
 					if(fileNumber + 1 == nofFiles){
 						submitButton.setTitle("finish");
 					}
 				}
 			}
 		});
 		
 		importOptionsForm.setFields(
 				textItemStudyName,
 				selectItemProjects,
 				selectItemTissues,
 				selectItemPlatform,
 				selectItemGenomeAssembly,
 				selectItemSNPTool,
 				textItemDescription,
 				selectItemProperty,
 				cancelButton,
 				submitButton);
 		
 		this.addItem(importOptionsForm);
 		
 	}
 }
 
 class ProgressWindow extends Window {
 	
 	private Progressbar bar;
 	
 	public ProgressWindow(int imp, int nofi){
 		super();
 		
 		int per = getPercentage(imp, nofi);
 		
 		this.setTitle("Upload Files "+ per + "%");
 		this.setWidth(400);
 		this.setHeight(60);
 		this.setAlign(Alignment.CENTER);
 	
 		this.setAutoCenter(true);
 		this.setIsModal(true);
 		this.setShowModalMask(true);
 		
 		bar = new Progressbar(); 
 		bar.setVertical(false); 
 		bar.setHeight(24);
 		
 		this.addItem(bar);
 	
 		bar.setPercentDone(per);
 	}
 	
 	private int getPercentage(int x, int y){
 		return (x/y)*100;
 	}
 	
 	public void updateValues(int imp, int nofi){
 		
 		int per = getPercentage(imp, nofi);
 		this.setTitle("Upload Files "+ per + "%");
 		bar.setPercentDone(per);	
 	}
 }
 
 class MyProjectRecordClickHandler implements RecordClickHandler {
 
 	private ListGrid projectStudyGrid;
 	private ListGrid projectAccessGrid;
 	private FoUser user;
 	
 	public MyProjectRecordClickHandler(ListGrid projectStudyGrid, ListGrid projectAccessGrid, FoUser user, CenterPanel cp){
 		this.projectStudyGrid = projectStudyGrid;
 		this.projectAccessGrid = projectAccessGrid;
 		this.user = user;
 	}
 	
 	@Override
 	public void onRecordClick(RecordClickEvent event) {
 		
 		String projectId = event.getRecord().getAttribute("projectId");
 		
 		projectStudyGrid.fetchData(new Criteria("projectId", projectId));
 		
 		if(user.getIsAdmin()){
 			
 			projectAccessGrid.fetchData(new Criteria("projectId", projectId));
 		}
 	}
 }
 
 class MyGroupRecordClickHandler implements RecordClickHandler {
 
 	private ListGrid groupUserGrid;
 	private CenterPanel cp;
 	
 	public MyGroupRecordClickHandler(ListGrid groupUserGrid, CenterPanel cp){
 		this.groupUserGrid = groupUserGrid;
 		this.cp = cp;
 	}
 	
 	@Override
 	public void onRecordClick(RecordClickEvent event) {
 		ListGridRecord[] oldRecords = groupUserGrid.getRecords();
 		
 		for (int i= 0; i < oldRecords.length; i++){
 			groupUserGrid.removeData(oldRecords[i]);
 		}
 		
 		int groupId = Integer.parseInt(event.getRecord().getAttribute("groupId"));
 		
 		cp.getUsersForGroup(groupId);
 	}
 }
 
 class RecMapClickHandler implements ClickHandler{
 	
 	private RecMapInfo recInfo;
 	private CenterPanel cp;
 	private GWTImageInfo imgInfo;
 	
 	
 	public RecMapClickHandler(RecMapInfo recmapinfo, GWTImageInfo imgInfo, CenterPanel centerPanel){
 		this.recInfo = recmapinfo;
 		this.imgInfo = imgInfo;
 		this.cp = centerPanel;
 	}
 	
 	public void onClick(ClickEvent event) {
 		
 		if(recInfo.getType().equals("gene")){
 			
 			geneDetails(recInfo.getElementName(), imgInfo.getQuery().getConfig().getStrArray("ensemblDBName")[0]);
 		}
 		
 		if(recInfo.getType().equals("segment")){
 			
 			segmentDetails(recInfo.getElementName());
 		}
 		
 		if(recInfo.getType().equals("translocation")){
 			
 			updateImgInfoForTranslocationId(recInfo.getElementName(), imgInfo);
 		}
 		if(recInfo.getType().equals("mutation_root")){
 			FoConfigData cd = imgInfo.getQuery().getConfig();
 			
 			cp.loadMutationWindow(recInfo.getElementName(), recInfo.getTrackNumber(), cd);
 			
 			//TODO
 		}
 	}
 	
 	public void segmentDetails(String query){
 		
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<FoSegment> callback = new AsyncCallback<FoSegment>(){
 			public void onSuccess(FoSegment segmentData){
 				
 				cp.loadWindow(segmentData);
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say(caught.getMessage());
 			}
 		};
 		req.getSegmentInfo(Integer.parseInt(query), callback);
 	}
 	
 	public void geneDetails(String query, String ensemblDB){
 		
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<EnsemblGene> callback = new AsyncCallback<EnsemblGene>(){
 			public void onSuccess(EnsemblGene gene){
 				
 				cp.loadWindow(gene);
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say(caught.getMessage());
 			}
 		};
 		req.getGeneInfo(query, ensemblDB, callback);
 	}
 	
 	public void updateImgInfoForTranslocationId(String query, GWTImageInfo imgInfo){
 		
 		final SearchAsync req = (SearchAsync) GWT.create(Search.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "Search";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<QueryInfo> callback = new AsyncCallback<QueryInfo>(){
 			public void onSuccess(QueryInfo query){
 				
 				cp.getMainPanel().getWestPanel().getSearchContent().search(query);
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				SC.say(caught.getMessage());
 			}
 		};
 		req.updateImgInfoForTranslocationId(Integer.parseInt(query), imgInfo, callback);
 	}
 }
 
 class ImageFrameResizedHandler implements ResizedHandler{
 
 	private CenterPanel cp;
 	
 	public ImageFrameResizedHandler(CenterPanel centerPanel){
 		cp = centerPanel;
 	}
 	
 	@Override
 	public void onResized(ResizedEvent event) {
 		if(cp.getCenterTabSet().getTabs().length > 1){
 			Canvas[] tabContents = cp.getCenterTabSet().getSelectedTab().getPane().getChildren();
 			Canvas presentationLayer = cp.getCenterTabSet().getSelectedTab().getPane();
 			ImgCanvas imgLayer = (ImgCanvas) tabContents[1];
 			imgLayer.getImageInfo().setWidth(presentationLayer.getInnerWidth());
 			cp.imageRedraw(imgLayer.getImageInfo());
 		}
 	}
 }
