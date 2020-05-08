 package com.akjava.gwt.webappmaker.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.html5.client.file.ui.FileNameAndText;
 import com.akjava.gwt.html5.client.file.ui.FileNameAndTextCell;
 import com.akjava.gwt.jdomaker.client.JDOBuilder;
 import com.akjava.gwt.lib.client.GWTHTMLUtils;
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.gwt.lib.client.StorageControler;
 import com.akjava.gwt.lib.client.StorageException;
 import com.akjava.gwt.lib.client.widget.PasteValueReceiveArea;
 import com.akjava.gwt.lib.client.widget.TabInputableTextArea;
 import com.akjava.gwt.webappmaker.client.ServletDataDto.FormDataToAdminServletDataFunction;
 import com.akjava.gwt.webappmaker.client.ServletDataDto.FormDataToMainServletDataFunction;
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.gwt.webtestmaker.client.InvalidCsvException;
 import com.akjava.gwt.webtestmaker.client.TestCommand;
 import com.akjava.gwt.webtestmaker.client.TestCommandDto;
 import com.akjava.gwt.webtestmaker.client.WebTestUtils;
 import com.akjava.gwt.webtestmaker.client.command.ClickLinkCommand;
 import com.akjava.gwt.webtestmaker.client.command.CompareTextCommand;
 import com.akjava.gwt.webtestmaker.client.command.CompareTitleCommand;
 import com.akjava.gwt.webtestmaker.client.command.OpenUrlCommand;
 import com.akjava.gwt.webtestmaker.client.command.SubmitCommand;
 import com.akjava.gwt.webtestmaker.client.command.TestInfoCommand;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormDataDto;
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.functions.MapToAdvancedTemplatedTextFunction;
 import com.akjava.lib.common.tag.Tag;
 import com.akjava.lib.common.tag.TagToStringConverter;
 import com.akjava.lib.common.utils.TagUtil;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.akjava.lib.common.utils.ValuesUtils;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SubmitButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent.Handler;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class WebAppsMaker implements EntryPoint {
 
 	private TabInputableTextArea input;
 	private TextArea output;
 	private TextBox packageBox;
 	private CellList<FileNameAndText> cellList;
 	private TextArea fileTextArea;
 	private VerticalPanel downloadLinks;
 
 	private StorageControler storageControler=new StorageControler();
 	private SingleSelectionModel<FileNameAndText> selectionModel;
 	private VerticalPanel downloadLinkContainer=new VerticalPanel();
 	private TextArea jdoCsv;
 	public void onModuleLoad() {
 		
 		String lang=GWTHTMLUtils.getInputValueById("gwtlang", "en");
 		Internationals.lang=lang;
 		
 		HorizontalPanel root=new HorizontalPanel();
 		VerticalPanel leftVertical=new VerticalPanel();
 		root.add(leftVertical);
 		GWTHTMLUtils.getPanelIfExist("gwtapp").add(root);
 		
 		PasteValueReceiveArea test=new PasteValueReceiveArea();
 		 test.setStylePrimaryName("readonly");
 		 test.setText("Click(Focus) & Paste Here");
 		 leftVertical.add(test);
 		 test.setSize("600px", "60px");
 		 test.setFocus(true);
 		 test.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				input.setText(event.getValue());
 				doConvert();
 			}
 			 
 		});
 		 
 		 
 		 HorizontalPanel hpanel=new HorizontalPanel();
 		 hpanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 		 leftVertical.add(hpanel);
 		 packageBox = new TextBox();
 		 packageBox.setWidth("200px");
 		 packageBox.setText(storageControler.getValue("packageValue", "com.akjava.gae.app1."));
 		
 		 hpanel.add(new Label("package"));
 		 hpanel.add(packageBox);
 		 
 		 leftVertical.add(new Label("Csv(tab only)"));
 		 input = new TabInputableTextArea();
 		 input.setText(storageControler.getValue("inputCsv", ""));
 		 //GWTHTMLUtils.setPlaceHolder(input, "className,package,servletName,path");
 		 input.setSize("600px","200px");
 		 leftVertical.add(input);
 		 Button convert=new Button("Convert",new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				doConvert();
 			}
 		});
 		 
 		 HorizontalPanel left=new HorizontalPanel();
 		 leftVertical.add(left);
 		 left.add(convert);
 		 left.add(downloadLinkContainer);
 		 
 		 output=new TextArea();
 		 output.setSize("600px","200px");
 		 output.setReadOnly(true);
 		 leftVertical.add(output);
 		 
 		 VerticalPanel centerVertical=new VerticalPanel();
 		 root.add(centerVertical);
 		 
 		 ScrollPanel scroll=new ScrollPanel();
 		 scroll.setSize("300px", "800px");
 		
 		 FileNameAndTextCell cell=new FileNameAndTextCell();
 		 cellList = new CellList<FileNameAndText>(cell);
 		 cellList.setPageSize(100);
 		 scroll.setWidget(cellList);
 		 centerVertical.add(scroll);
 		 
 		 selectionModel = new SingleSelectionModel<FileNameAndText>();
 		 cellList.setSelectionModel(selectionModel);
 		 selectionModel.addSelectionChangeHandler(new Handler() {
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				FileNameAndText select=selectionModel.getSelectedObject();
 				if(select!=null){
 				fileTextArea.setText(select.getText());
 				downloadLinks.clear();
 				downloadLinks.add(select.createDownloadLink("Download"));
 				}else{
 					fileTextArea.setText("");
 					downloadLinks.clear();
 				}
 			}
 		});
 		 VerticalPanel rightVertical=new VerticalPanel();
 		 root.add(rightVertical);
 		 downloadLinks = new VerticalPanel();
 		 rightVertical.add(downloadLinks);
 		 fileTextArea = new TextArea();
 		 fileTextArea.setReadOnly(true);
 		 fileTextArea.setSize("800px", "800px");
 		 rightVertical.add(fileTextArea);
 		 
 		 jdoCsv = new TextArea();
 		 jdoCsv.setSize("600px","200px");
 		 jdoCsv.setReadOnly(true);
 		 leftVertical.add(jdoCsv);
 		 
 	}
 	public String getFixedPackage(){
 		String packageText=packageBox.getText();
 		if(packageText.endsWith(".")){
 			packageText=ValuesUtils.chomp(packageText);
 		}
 		return packageText;
 	}
 
 	
 	
 	protected void doConvert() {
 		String packageValue=getFixedPackage();
 		cellList.getSelectionModel().setSelected(selectionModel.getSelectedObject(), false);
 		List<FormData> datas=FormDataDto.linesToFormData(input.getText());
 		//debug text
 		String out="";
 		for(FormData data:datas){
 			out+=data.toString();
 			out+="\n";
 		}
 		output.setText(out);
 		
 		List<FileNameAndText> files=new ArrayList<FileNameAndText>();
 		List<ServletData> sdatas=Lists.newArrayList();
 		
 		//create jdo classes
 		FileNameAndText pmfFile=new FileNameAndText("PMF.java",JDOBuilder.generatePackageLine(packageValue)+com.akjava.gwt.jdomaker.client.resources.Bundles.INSTANCE.pmf().getText());
 		files.add(pmfFile);
 		
 		for(FormData data:datas){
 		String text=JDOCsvConverter.convert(data.getFormFieldDatas());
 		JDOBuilder jdoBuilder=new JDOBuilder();
 		//TODO support more settings
 		List<FileNameAndText> jdoFiles=jdoBuilder.createJdoFilesByCsv(new JDOBuilder.Settings().className(data.getClassName()+"Entity").packageValue(packageValue), text);
 		for(FileNameAndText file:jdoFiles){
 			files.add(file);
 		}
 		}
 		
 		for(FormData fdata:datas){
 			//Tools.java
 			ToolsGenerator toolsGenerator=new ToolsGenerator(fdata,packageValue);
 			files.add(toolsGenerator.createFileNameAndText());
 			//Modifier.java
 			ModifierGenerator modifierGenerator=new ModifierGenerator(fdata,packageValue);
 			files.add(modifierGenerator.createFileNameAndText());
 			//Validator.java
 			ValidatorGenerator validatorGenerator=new ValidatorGenerator(fdata,packageValue);
 			files.add(validatorGenerator.createFileNameAndText());
 			
 			if(fdata.isAdminOnly()){//some data dont need to show users.
 				continue;
 			}
 			
 			List<ServletData> sdata=new FormDataToMainServletDataFunction(packageValue+".").apply(fdata);
 			Iterables.addAll(sdatas, sdata);
 			
 			List<FileNameAndText> mainServletFiles=Lists.transform(sdata, new ServletDataDto.ServletDataToServletFileFunction());
 			Iterables.addAll(files, mainServletFiles);
 			
 			
 			List<List<FileNameAndText>> templateFiles=Lists.transform(sdata, new ServletDataDto.ServletDataToTemplateFileFunction());
 			for(List<FileNameAndText> templates:templateFiles){
 				Iterables.addAll(files, templates);
 			}
 			//TODO list always contain add link,however usually not need it.
 			//TODO option allow add,edit delete
 				
 		}
 		
 		
 		//admin use another mainbase
 		for(FormData fdata:datas){
 			List<ServletData> sdata=new FormDataToAdminServletDataFunction(packageValue+".").apply(fdata);
 			Iterables.addAll(sdatas, sdata);
 			
 			List<FileNameAndText> mainServletFiles=Lists.transform(sdata, new ServletDataDto.ServletDataToServletFileFunction());
 			Iterables.addAll(files, mainServletFiles);
 			
 			
 			List<List<FileNameAndText>> templateFiles=Lists.transform(sdata, new ServletDataDto.ServletDataToTemplateFileFunction());
 			for(List<FileNameAndText> templates:templateFiles){
 				Iterables.addAll(files, templates);
 			}
 		}
 		//sharedutils.java
 		files.add(FileNameAndTextGenerator.generateSharedUtils(packageValue+"."));
 		
 		//Top
 		files.add(FileNameAndTextGenerator.generateTopServlet(packageValue+"."));
 		//template
 		files.add(FileNameAndTextGenerator.generateTopTemplate(datas));
 		
 		//AdminTop
 		files.add(FileNameAndTextGenerator.generateAdminTopServlet(packageValue+"."));
 		//template
 		files.add(FileNameAndTextGenerator.generateAdminTopTemplate(datas));
 		
 		
 		//mainBase
 		files.add(FileNameAndTextGenerator.generateMainBase(datas));
 		//adminBase
 		files.add(FileNameAndTextGenerator.generateAdminBase(datas));
 		
 		
 		//web.xml
 		String template=Bundles.INSTANCE.servlet().getText();
 		
 		//create list for add
 		List<ServletWebXmlData> xmlDatas=Lists.newArrayList(Collections2.transform(sdatas, ServletDataDto.getServletDataToServletWebXmlFunction()));
 		
 		//add top
 		ServletWebXmlData topData=new ServletWebXmlData();
 		topData.setName("Top");
 		topData.setFullClassName(packageValue+"."+"main.TopServlet");
 		topData.setPath("/index.html");
 		xmlDatas.add(topData);
 		
 		//add admin top
 		ServletWebXmlData adminTopData=new ServletWebXmlData();
 		adminTopData.setName("AdminTop");
 		adminTopData.setFullClassName(packageValue+"."+"admin.AdminTopServlet");
 		adminTopData.setPath("/admin/index.html");
 		xmlDatas.add(adminTopData);
 		
 		List<Map<String,String>> xmlTextMaps=Lists.transform(xmlDatas, ServletWebXmlDataDto.getServletWebXmlDataToMapFunction());
 		List<String> webXmls=Lists.transform(xmlTextMaps, new MapToAdvancedTemplatedTextFunction(template));
 		String webXmlTemplate=Bundles.INSTANCE.web().getText();
 		
 		Map<String,String> map=new HashMap<String, String>();
 		map.put("welcome", "index.html");
 		map.put("servlets", Joiner.on("\n").join(webXmls));
 		files.add(new FileNameAndText("web.xml",TemplateUtils.createAdvancedText(webXmlTemplate, map)));
 		
 		
 		//create webtest xml files 
 		Tag project=new Tag("project").attr("default", "test");
 		Tag target=new Tag("target").attr("name", "test");
 		project.addChild(target);
 		try{
 			for(FormData data:datas){
 				
 				String xmlText=createTestXmlFile(data);
 				String name="test_"+data.getClassName().toLowerCase()+".xml";
 				files.add(new FileNameAndText(name, xmlText));
				target.addChild(new Tag("ant").attr("antfile", name));
 			}
 			}catch (Exception e) {
 				LogUtils.log(e.getMessage());
 			}
 			files.add(new FileNameAndText("allTests.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+TagToStringConverter.convert(project)));
 		
 		
 		cellList.setRowData(0, files);
 		try {
 			storageControler.setValue("packageValue",packageValue);
 			packageBox.setText(packageValue);
 			storageControler.setValue("inputCsv", input.getText());
 		} catch (StorageException e) {
 			LogUtils.log(e.getMessage());
 			e.printStackTrace();
 		}
 		//create
 		
 		
 		//create submit
 		downloadLinkContainer.clear();
 		FormPanel form=new FormPanel();
 		
 		form.setAction("/tozip");
 		form.setMethod(FormPanel.METHOD_POST);
 		VerticalPanel container=new VerticalPanel();
 		form.add(container);
 		container.add(new Hidden("filenumber", ""+files.size()));
 		for(int i=0;i<files.size();i++){
 			int number=i+1;
 			FileNameAndText file=files.get(i);
 			container.add(new Hidden("path"+number, DirectoryDetector.detectDirectory(packageBox.getText(), file)));
 			container.add(new Hidden("text"+number, file.getText()));
 		}
 		
 		container.add(new SubmitButton("Download"));
 		downloadLinkContainer.add(form);
 		
 		String jdoText="";
 		for(FormData data:datas){
 		String text=JDOCsvConverter.convert(data.getFormFieldDatas());
 		jdoText+=text+"\n";
 		}
 		jdoCsv.setText(jdoText);
 		
 	}
 	
 	private String createTestXmlFile(FormData data) throws InvalidCsvException{
 		
 		//TODO get base host info
 			String baseUrl="http://localhost:8888/admin/"+data.getClassName().toLowerCase();
 			List<List<TestCommand>> testCommands=createStandardWebTest(data.getClassName().toLowerCase(),baseUrl,data);
 			
 		
 		String xmlText=WebTestUtils.testToXmlText(WebTestUtils.testToTag(testCommands, baseUrl));
 		return xmlText;
 	}
 	private List<List<TestCommand>> createStandardWebTest(String keyName,String baseUrl,
 			FormData formData) {
 		List<FormFieldData> formFieldDatas=formData.getFormFieldDatas();
 		List<List<TestCommand>> testCommandsList=new ArrayList<List<TestCommand>>();
 		//add test
 		List<TestCommand> testCommands=new ArrayList<TestCommand>();
 		testCommandsList.add(testCommands);
 		testCommands.add(new TestInfoCommand(keyName+"_add test",""));
 		testCommands.add(new OpenUrlCommand(ServletDataDto.URL_ADD));
 		//TODO think how to international
 		
 		Collection<TestCommand> commands=Collections2.transform(
 				Collections2.filter(formFieldDatas, TestCommandDto.getAvaiableSetFormFieldDataFilter()),
 				TestCommandDto.getFormFieldDataToSimpleSetWebTest());
 		for(TestCommand command:commands){
 			testCommands.add(command);
 		}
 		
 		testCommands.add(new SubmitCommand());
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 追加確認"));
 		testCommands.add(new CompareTextCommand("エラーがあります",true));
 		
 		testCommands.add(new SubmitCommand());
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 追加実行"));
 		testCommands.add(new CompareTextCommand("追加完了"));
 		//show test
 		testCommands=new ArrayList<TestCommand>();
 		testCommandsList.add(testCommands);
 		testCommands.add(new TestInfoCommand(keyName+"_show test","depdnds on passed add-test"));
 		testCommands.add(new OpenUrlCommand(""));
 		testCommands.add(new ClickLinkCommand("表示"));
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 表示"));
 		
 		//edit test
 		testCommands=new ArrayList<TestCommand>();
 		testCommandsList.add(testCommands);
 		testCommands.add(new TestInfoCommand(keyName+"_edit test","depdnds on passed add-test"));
 		testCommands.add(new OpenUrlCommand(""));
 		testCommands.add(new ClickLinkCommand("編集"));
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 編集"));
 		
 		commands=Collections2.transform(
 				Collections2.filter(formFieldDatas, TestCommandDto.getAvaiableSetFormFieldDataFilter()),
 				TestCommandDto.getFormFieldDataToSimpleEditWebTest());
 		for(TestCommand command:commands){
 			testCommands.add(command);
 		}
 		testCommands.add(new SubmitCommand());
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 編集確認"));
 		testCommands.add(new CompareTextCommand("エラーがあります",true));
 		
 		testCommands.add(new SubmitCommand());
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 編集実行"));
 		testCommands.add(new CompareTextCommand("編集完了"));
 		
 		//delete test
 		testCommands=new ArrayList<TestCommand>();
 		testCommandsList.add(testCommands);
 		testCommands.add(new TestInfoCommand(keyName+"_delete test","depdnds on passed add-test"));
 		testCommands.add(new OpenUrlCommand(""));
 		testCommands.add(new ClickLinkCommand("削除"));
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 削除確認"));
 		
 		
 		testCommands.add(new SubmitCommand());
 		testCommands.add(new CompareTitleCommand(formData.getName()+" 削除実行"));
 		testCommands.add(new CompareTextCommand("削除完了"));
 		
 		return testCommandsList;
 	}
 }
