 package com.atanor.smanager.client.ui;
 
 import com.atanor.smanager.rpc.dto.HardwareDto;
 import com.atanor.smanager.rpc.dto.PresetDto;
 import com.atanor.smanager.rpc.dto.WindowDto;
 import com.atanor.smanager.rpc.services.ConfigService;
 import com.atanor.smanager.rpc.services.ConfigServiceAsync;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.types.VisibilityMode;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.SectionStack;
 
 public class NavigationArea extends HLayout {
 
 	private final ConfigServiceAsync configService = GWT
 			.create(ConfigService.class);
 	private String namePreset = "name", windowN = "window";
 
 	public NavigationArea(final SimpleEventBus bus) {
 
 		super();
 
 		this.setMembersMargin(10);
 		this.setOverflow(Overflow.HIDDEN);
 		this.setShowResizeBar(true);
 
 		final SectionStack sectionStack = new SectionStack();
 		sectionStack.setShowExpandControls(true);
 		sectionStack.setAnimateSections(true);
 		sectionStack.setVisibilityMode(VisibilityMode.MUTEX);
 		sectionStack.setOverflow(Overflow.HIDDEN);
 		sectionStack.setMembersMargin(5);
 		sectionStack.setContents("<H2>Presets</H2>");
 
 		// final SectionStackSection section1 = new
 		// SectionStackSection("Presets");
 		// section1.setMembersMargin(20);
 		// section1.setExpanded(true);
 
 		configService
 				.getHardwareConfiguration(new AsyncCallback<HardwareDto>() {
 
 					@Override
 					public void onSuccess(final HardwareDto result) {
 						int pres = 0, win = 0;
 
 						for (PresetDto preset : result.getPresets()) {
 							namePreset = namePreset + pres;
 							final Label $namePreset = new Label();
 							$namePreset.setTop((pres * 164) + 2);
 							$namePreset.setLeft(7);
 							$namePreset.setWidth(164);
 							$namePreset.setHeight(62);
 							$namePreset.setShowEdges(true);
 							$namePreset.setBorder("2px solid black");
 							$namePreset.setMaxWidth(164);
 							$namePreset.setMaxHeight(62);
 							$namePreset.setBackgroundColor("lightgrey");
//							$namePreset.setProperty("preset", pres);
 							$namePreset.draw();
 //							$namePreset
 //									.addDoubleClickHandler(new DoubleClickHandler() {
 //										@Override
 //										public void onDoubleClick(
 //												DoubleClickEvent event) {
 //											event.getSource();
 //											bus.fireEvent(new AnimateEvent(0));
 //											for (int count = 0; count <= result
 //													.getPresets().size(); count++) {
 //												$namePreset.animateFade(30,
 //														null, 1000);
 ////												$namePreset.animateFade(100,
 ////														null, 1000);
 //											}
 //										}
 //									});
 							pres++;
 
 							for (WindowDto window : preset.getWindows()) {
 								windowN = windowN + win;
 								final Label $windowN = new Label();
 								$windowN.setTop(window.getYTopLeft() / 20);
 								$windowN.setLeft(window.getXTopLeft() / 10);
 								$windowN.setWidth((window.getXBottomRight() - window
 										.getXTopLeft()) / 10);
 								$windowN.setHeight((window.getYBottomRight() - window
 										.getYTopLeft()) / 20);
 								$windowN.setBorder("1px solid black");
 								$windowN.setBackgroundColor("darkgrey");
 								$windowN.draw();
 								$namePreset.addChild($windowN);
 								win++;
 							}
 							;
 							sectionStack.addChild($namePreset);
 
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						SC.say("Configurations are not available!");
 						caught.printStackTrace();
 					}
 				});
 
 		
 //		drawPane2.addDoubleClickHandler(new DoubleClickHandler() {
 //			@Override
 //			public void onDoubleClick(DoubleClickEvent event) {
 //				drawPane2.animateFade(30, null, 1000);
 //				bus.fireEvent(new AnimateEvent(1));
 //				drawPane.animateFade(100, null, 1000);
 //			}
 //		});
 //
 //		DrawLabel name1 = new DrawLabel();
 //		name1.setDrawPane(drawPane);
 //		name1.setLeft(33);
 //		name1.setTop(24);
 //		name1.setContents("One to All");
 //		name1.setLineWidth(1);
 //		name1.setFontSize(11);
 //		name1.draw();
 
 		
 		// sectionStack.addSection(section1);
 
 		this.addMember(sectionStack);
 
 	}
 }
