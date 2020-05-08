 /**
  * DistributionBarDemoApplication.java (VaadinDistributionBar)
  * 
  * Copyright 2012 Vaadin Ltd, Sami Viitanen <alump@vaadin.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.vaadin.alump.distributionbar;
 
 import java.util.Random;
 
 import com.vaadin.Application;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 public class DistributionBarDemoApplication extends Application {
 	
 	private static final long serialVersionUID = -2473123168493326044L;
 	private DistributionBar barOne;
 	private DistributionBar barTwo;
 	private DistributionBar barThree;
 	private DistributionBar barFour;
 	private DistributionBar barFive;
 	private DistributionBar barSix;
 	
 	final static private int BAR_ONE_PARTS = 2;
 	final static private int BAR_TWO_PARTS = 3;
 	final static private int BAR_THREE_PARTS = 6;
 	final static private int BAR_FOUR_PARTS = 10;
 	final static private int BAR_FIVE_PARTS = 2;
 	final static private int BAR_SIX_PARTS = 11;
 	
 	@Override
 	public void init() {
 		
 		setTheme ("distributionbardemo");
 		
 		Window mainWindow = new Window("Vaadin Distribution Bar Demo");
 		mainWindow.addComponent(buildView());
 		setMainWindow(mainWindow);
 	}
 	
 	private Component buildView() {
 		
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSizeFull();
 		layout.setSpacing(true);
 		
 		Label header = new Label ("Distribution Bar Demo");
 		layout.addComponent(header);
 		
 		Button randomButton = new Button("Click here to update the bars values");
 		randomButton.addListener(randomButtonListener);
 		layout.addComponent(randomButton);
 		
 		barOne = new DistributionBar(BAR_ONE_PARTS);
 		barOne.setCaption("Senate:");
 		barOne.setWidth("100%");
 		barOne.addStyleName("my-bar-one");
 		barOne.setPartTitle(0, "Democratic Party");
 		barOne.setPartTitle(1, "Republican Party");
 		layout.addComponent(barOne);
 		layout.setComponentAlignment(barOne, Alignment.MIDDLE_CENTER);
 		
 		barTwo = new DistributionBar(BAR_TWO_PARTS);
 		barTwo.setCaption("Do people like nicer backgrounds?");
 		barTwo.setWidth("100%");
 		barTwo.addStyleName("my-bar-two");
 		layout.addComponent(barTwo);
 		layout.setComponentAlignment(barTwo, Alignment.MIDDLE_CENTER);
 		
 		barThree = new DistributionBar(BAR_THREE_PARTS);
 		barThree.setCaption("Maaaany parts with default styling");
 		barThree.setWidth("100%");
 		barThree.addStyleName("my-bar-three");
 		layout.addComponent(barThree);
 		layout.setComponentAlignment(barThree, Alignment.MIDDLE_CENTER);
 		
 		barFour = new DistributionBar(BAR_FOUR_PARTS);
 		barFour.setCaption("CSS tricks");
 		barFour.setWidth("100%");
 		barFour.addStyleName("my-bar-four");
 		layout.addComponent(barFour);
 		layout.setComponentAlignment(barFour, Alignment.MIDDLE_CENTER);
 		
 		barFive = new DistributionBar(BAR_FIVE_PARTS);
 		barFive.setCaption("Vote results:");
 		barFive.setWidth("100%");
 		barFive.addStyleName("my-bar-five");
 		barFive.setPartTitle(0, "YES!");
 		barFive.setPartTitle(1, "NO!");
 		layout.addComponent(barFive);
 		layout.setComponentAlignment(barFive, Alignment.MIDDLE_CENTER);
 		
 		barSix = new DistributionBar(BAR_SIX_PARTS);
 		barSix.setCaption("Change in part count:");
 		barSix.setWidth("100%");
 		barSix.addStyleName("my-bar-six");
 		layout.addComponent(barSix);
 		layout.setComponentAlignment(barSix, Alignment.MIDDLE_CENTER);
 
 		
 		return layout;
 		
 	}
 	
 	private final Button.ClickListener randomButtonListener = 
 		new Button.ClickListener() {
 
			private static final long serialVersionUID = -4454445076337898590L;

 			public void buttonClick(ClickEvent event) {
 				
 				Random random = new Random();
 				
 
 				int chairs = 100;
 				int groupA = random.nextInt(chairs);
 				
 				barOne.setPartSize(0, groupA);
 				barOne.setPartSize(1, chairs - groupA);
 
 				
 				for (int i = 0; i < BAR_TWO_PARTS; ++i) {
 					barTwo.setPartSize(i, random.nextInt(20));
 				}
 				
 				for (int i = 0; i < BAR_THREE_PARTS; ++i) {
 					barThree.setPartSize(i, random.nextInt(50));
 				}
 				
 				for (int i = 0; i < BAR_FOUR_PARTS; ++i) {
 					barFour.setPartSize(i, random.nextInt(10));
 				}
 				
 				for (int i = 0; i < BAR_FIVE_PARTS; ++i) {
 					barFive.setPartSize(i, random.nextInt(10000000));
 				}
 				
 				int newSize = 2 + random.nextInt(9);
 				barSix.setNumberOfParts(newSize);
 				for (int i = 0; i < newSize; ++i) {
 					barSix.setPartSize(i, random.nextInt(5));
 				}
 				
 				
 			}
 	};
 	
 	
 
 }
