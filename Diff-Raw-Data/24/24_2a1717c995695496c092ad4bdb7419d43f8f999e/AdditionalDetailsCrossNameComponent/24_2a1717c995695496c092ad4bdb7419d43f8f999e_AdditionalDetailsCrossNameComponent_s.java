 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.breeding.manager.crossingmanager;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.generationcp.breeding.manager.application.Message;
 import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
 import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.GermplasmDataManager;
 import org.generationcp.middleware.pojos.Germplasm;
 import org.generationcp.middleware.pojos.Name;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.ui.AbsoluteLayout;
 import com.vaadin.ui.AbstractComponent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.OptionGroup;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.Notification;
 
 /**
  * This class contains the absolute layout of UI elements in Cross Name section
  * in "Enter Additional Details..." tab in Crossing Manager application
  * 
  * @author Darla Ani
  *
  */
 @Configurable
 public class AdditionalDetailsCrossNameComponent extends AbsoluteLayout 
 		implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener{
 	
 	public static final String GENERATE_BUTTON_ID = "Generate Next Name Id";
 
 	private static final long serialVersionUID = -1197900610042529900L;
 	private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);
 	private static final Integer MAX_LEADING_ZEROS = 10;
 	
 	@Autowired
 	private SimpleResourceBundleMessageSource messageSource;
 	
 	@Autowired
 	private GermplasmDataManager germplasmManager;
 	
     private Label specifyPrefixLabel;
     private Label specifySuffixLabel;
     private Label howManyDigitsLabel; 
     private Label nextNameInSequenceLabel;
     private Label generatedNameLabel;
     
     private OptionGroup crossNameOptionGroup;
     private TextField prefixTextField;
     private TextField suffixTextField;
     private CheckBox sequenceNumCheckBox;
     private Select leadingZerosSelect;
     private Button generateButton;
     
     private AbstractComponent[] digitsToggableComponents = new AbstractComponent[2];
     private AbstractComponent[] otherToggableComponents = new AbstractComponent[8];
 
     private String lastPrefixUsed; //store prefix used for MW method including zeros, if any
     private Integer nextNumberInSequence;
     
     private CrossesMadeContainer container;
         
     
 	@Override
 	public void setCrossesMadeContainer(CrossesMadeContainer container) {
 		this.container = container;
 	}
 	
 	@SuppressWarnings("serial")
 	@Override
 	public void afterPropertiesSet() throws Exception {  
 		setHeight("200px");
         setWidth("700px");
         	
 		crossNameOptionGroup = new OptionGroup();
 		sequenceNumCheckBox = new CheckBox();
 		sequenceNumCheckBox.setImmediate(true);
 		sequenceNumCheckBox.addListener(new Property.ValueChangeListener() {
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
 			}
 		});
 	
 		specifyPrefixLabel = new Label();
 		prefixTextField = new TextField();
 		prefixTextField.setWidth("500px");
 		
 		howManyDigitsLabel = new Label();
 		leadingZerosSelect = new Select();
 		for (int i = 1; i <= MAX_LEADING_ZEROS; i++){
 			leadingZerosSelect.addItem(Integer.valueOf(i));
 		}
 		leadingZerosSelect.setNullSelectionAllowed(false);
 		leadingZerosSelect.select(Integer.valueOf(1));
 		leadingZerosSelect.setWidth("50px");
 		
 		
 		specifySuffixLabel = new Label();
 		suffixTextField = new TextField();
 		suffixTextField.setWidth("500px");
 		
 		nextNameInSequenceLabel = new Label();
 		generatedNameLabel = new Label();
 		
 		generateButton = new Button();
 		generateButton.setData(GENERATE_BUTTON_ID);
 		generateButton.addListener(new CrossingManagerImportButtonClickListener(this));
 
 		layoutComponents();
 		initializeToggableComponents();
 	}
 
 
     @Override
     public void attach() {
         super.attach();
         updateLabels();
     }
     
     @Override
 	public void updateLabels() {
 		messageSource.setCaption(specifyPrefixLabel, Message.SPECIFY_PREFIX_REQUIRED);
 		messageSource.setCaption(specifySuffixLabel, Message.SPECIFY_SUFFIX_OPTIONAL);
 		messageSource.setCaption(sequenceNumCheckBox, Message.SEQUENCE_NUMBER_SHOULD_HAVE_LEADING_ZEROS);
 		messageSource.setCaption(howManyDigitsLabel, Message.HOW_MANY_DIGITS);
 		messageSource.setCaption(nextNameInSequenceLabel, Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE);
 		messageSource.setCaption(generateButton, Message.GENERATE);
 	}
     
     private void layoutComponents() {
 		addComponent(crossNameOptionGroup, "top:10px;left:20px");
 		addComponent(specifyPrefixLabel, "top:40px;left:20px");
 		addComponent(prefixTextField, "top:25px;left:180px");
 		addComponent(sequenceNumCheckBox, "top:57px;left:20px");
 		addComponent(howManyDigitsLabel, "top:73px;left:400px");
 		addComponent(leadingZerosSelect, "top:55px;left:520px");
 		addComponent(specifySuffixLabel, "top:110px;left:20px");
 		addComponent(suffixTextField, "top:90px;left:180px");
 		addComponent(nextNameInSequenceLabel, "top:145px;left:20px");
 		addComponent(generatedNameLabel, "top:145px;left:237px");
 		addComponent(generateButton, "top:155px;left:20px");
 	}
     
     private void initializeToggableComponents(){
     	digitsToggableComponents[0] = howManyDigitsLabel;
     	digitsToggableComponents[1] = leadingZerosSelect;
     	
     	otherToggableComponents[0] = specifyPrefixLabel;
     	otherToggableComponents[1] = specifySuffixLabel;
     	otherToggableComponents[2] = nextNameInSequenceLabel;
     	otherToggableComponents[3] = prefixTextField;
     	otherToggableComponents[4] = suffixTextField;
     	otherToggableComponents[5] = sequenceNumCheckBox;
     	otherToggableComponents[6] = generatedNameLabel;
     	otherToggableComponents[7] = generateButton;
     	
     	enableSpecifyCrossNameComponents(true);
     }
     
     // Enables / disables UI elements for specifying Cross Name details
 	private void enableSpecifyCrossNameComponents(boolean enabled){
 		for (AbstractComponent component : otherToggableComponents){
 			component.setEnabled(enabled);
 		}
 		enableSpecifyLeadingZerosComponents(enabled && sequenceNumCheckBox.booleanValue());
 	}
 	
 	private void enableSpecifyLeadingZerosComponents(boolean enabled){
 		for (AbstractComponent component : digitsToggableComponents){
 			component.setEnabled(enabled);
 		}
 	}
     
 	// Action handler for Generation button
     public void generateNextNameButtonAction(){
     	if (validateCrossNameFields()) {
     		String suffix = ((String) suffixTextField.getValue()).trim();
     		
     		try {
     			lastPrefixUsed = buildPrefixString();
     			String nextSequenceNumberString = this.germplasmManager.getNextSequenceNumberForCrossName(lastPrefixUsed);
     			
     			nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
     			generatedNameLabel.setCaption(buildNextNameInSequence(lastPrefixUsed, suffix, nextNumberInSequence));
     			
     		} catch (MiddlewareQueryException e) {
     			LOG.error(e.toString() + "\n" + e.getStackTrace());
     			e.printStackTrace();
     			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
     					messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE), Notification.POSITION_CENTERED);
     		}
     	}
     	
     	
     }
     
     private boolean validateCrossNameFields(){
     	Window window = getWindow();
     	String prefix = ((String) prefixTextField.getValue()).trim();
     	
     	if (StringUtils.isEmpty(prefix)){
     		MessageNotifier.showError(window, "Error with Cross Code", messageSource.getMessage(Message.ERROR_ENTER_PREFIX_FIRST), Notification.POSITION_CENTERED);
     		return false;
     	
     	} else if (prefix.contains(" ")){
     		MessageNotifier.showError(window, "Error with Cross Code", messageSource.getMessage(Message.ERROR_PREFIX_HAS_WHITESPACE), Notification.POSITION_CENTERED);
         	return false;
     	} 
     	
     	return true;
     }
     
     private boolean validateGeneratedName(){
     	
     	// if Generate button never pressed
     	if (nextNumberInSequence == null ){
     		MessageNotifier.showError(getWindow(), "Error with Cross Code", MessageFormat.format(
     				messageSource.getMessage(Message.ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST), ""
     				), Notification.POSITION_CENTERED);
     		return false;
 
     	// if prefix specifications were changed and next name in sequence not generated first
     	} else {
     		String currentPrefixString = buildPrefixString();
 			if (!currentPrefixString.equals(lastPrefixUsed)){
     			MessageNotifier.showError(getWindow(), "Error with Cross Code", MessageFormat.format(
     					messageSource.getMessage(Message.ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST), " (" + currentPrefixString +")"
     					), Notification.POSITION_CENTERED);
     			return false;
     		}
     	}
     	
     	return true;
     }
     
     private String buildPrefixString(){
    	return ((String) prefixTextField.getValue()).trim() + getLeadingZeroesAsString();
     }
 
 	private String buildNextNameInSequence(String prefix, String suffix,
 			Integer number) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(prefix);
		if(!(prefix.charAt(prefix.length()-1) == '0')){
		    sb.append(" ");
		}
		sb.append(number);
 		if (!StringUtils.isEmpty(suffix)){
 			sb.append(" ");
 			sb.append(suffix);
 		}
 		return sb.toString();
 	}
     
     
    private String getLeadingZeroesAsString(){
     	StringBuilder sb = new StringBuilder();
    	sb.append(" ");
     	if (sequenceNumCheckBox.booleanValue()){
     		Integer numOfZeros = (Integer) leadingZerosSelect.getValue();
    		for (int i = 0; i < numOfZeros; i++){
    			sb.append("0");
     		}
     	}
     	return sb.toString();
     }
 
     
 	@Override
 	public boolean updateCrossesMadeContainer() {
 		
 		if (this.container != null && this.container.getCrossesMade() != null && 
 				this.container.getCrossesMade().getCrossesMap()!= null && validateCrossNameFields() 
 					&& validateGeneratedName()) { 
 			
 			int ctr = nextNumberInSequence;
 			String suffix = (String) suffixTextField.getValue();
 			
 			Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
 			List<GermplasmListEntry> oldCrossNames = new ArrayList<GermplasmListEntry>();
 			
 			// Store old cross name and generate new names based on prefix, suffix specifications
 			for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()){
 				Name nameObject = entry.getValue();
 				String oldCrossName = nameObject.getNval();
 				nameObject.setNval(buildNextNameInSequence(lastPrefixUsed, suffix, ctr++));
 				
 				Germplasm germplasm = entry.getKey();
 				Integer tempGid = germplasm.getGid();
 				GermplasmListEntry oldNameEntry = new GermplasmListEntry(tempGid, tempGid, tempGid, oldCrossName);
 				
 				oldCrossNames.add(oldNameEntry);
 			}
 			// Only store the "original" cross names, would not store previous names on 2nd, 3rd, ... change
 			if (this.container.getCrossesMade().getOldCrossNames()== null ||
 					this.container.getCrossesMade().getOldCrossNames().isEmpty()){
 				this.container.getCrossesMade().setOldCrossNames(oldCrossNames);				
 			}
 			
 			return true;
 				
 		}
 		
 		return false;
 	}
 
 	
 }
