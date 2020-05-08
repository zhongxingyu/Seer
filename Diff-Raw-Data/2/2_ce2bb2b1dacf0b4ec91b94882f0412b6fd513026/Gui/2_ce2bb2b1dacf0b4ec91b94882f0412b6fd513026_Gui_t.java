 /*******************************************************************************
  * <eCharacter> is a research project of the <e-UCM>
  *          research group.
  *
  *    Developed by: Alejandro Muñoz del Rey, Sergio de Luis Nieto and David González
  *    Ledesma.
  *    Under the supervision of Baltasar Fernández-Manjón and Javier Torrente
  * 
  *    Copyright 2012-2013 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://echaracter.e-ucm.es>, 
  *          <http://e-adventure.e-ucm.es> or <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  *      <eCharacter> is free software: you can 
  *      redistribute it and/or modify it under the terms of the GNU Lesser 
  *      General Public License as published by the Free Software Foundation, 
  *      either version 3 of the License, or (at your option) any later version.
  *  
  *      <eCharacter> is distributed in the hope that it 
  *      will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  *      warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
  *      See the GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <eCharacter>. If not, 
  *      see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package es.eucm.echaracter.gui;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.material.RenderState;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Sphere;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.NiftyEventSubscriber;
 import de.lessvoid.nifty.builder.ImageBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
 import de.lessvoid.nifty.controls.SliderChangedEvent;
 import es.eucm.echaracter.repository.RepositoryReader;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import es.eucm.echaracter.api.Callback;
 import es.eucm.echaracter.control.Control;
 import es.eucm.echaracter.gui.progressbar.ProgressbarThread;
 import es.eucm.echaracter.i18n.I18N;
 import es.eucm.echaracter.loader.Configuration;
 import es.eucm.echaracter.types.StageType;
 import java.awt.Desktop;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 public class Gui extends AbstractAppState implements ScreenController {
     
     private static final int BONES_PAGE = 5;
     private Nifty nifty;
     private I18N i18nGui, i18nModel, i18nFamily; 
     private Control control;
     private Application app;
     private Configuration config;
     private String selection, modelSelection,subStageSelected, textureOrSubMeshSelected;
     private int page;
     private ArrayList<String> stages,idBones,idPhysicalBuild;
     private ArrayList<String> families;
     private int index, languagePage, repositoryPage;
     private String language;
     private FamilyStageBuilder modelsb;
     private ScaleStageBuilder scalesb;
     private SingleStageBuilder singlesb;
     private MultiStageBuilder multisb;
     private AnimationStageBuilder animationsb;
     private PopUpBuilder popUp;
     private RepositoryReader repository;
     private Callback callback;
     
     /**
      * The mustLoad and loading params are used as a quick&dirty state machine implementation.
      * Their only purpose is to allow Nifty update and show the loading progress components
      * before start loading the selected model. 
      * 
      * The state transition is as follows:
      * 1) The user clicks the button to start character customization.
      * 2) The method loadFirstScreen() is called. This method setsUp mustLoad=true
      * 3) In the next update() cycle, mustLoad:=false and loading:=true. 
      * 4) In the next update() cycle, the method doLoad() is invoked which actually triggers the loading process
      */
     private boolean mustLoad=false;
     private boolean loading=false;
     
     public Gui(Control control,Configuration config,Callback callback){
         this.control = control;
         this.config = config;
         this.callback = callback;
         families = this.control.getFamiliesID();
     }
     
     public void startGame(String nextScreen) {
         nifty.gotoScreen(nextScreen);  // switch to another screen
     }
     
     public void loadFamilyScreen(){
         String types[] = {StageType.singleStage.toString(),
                           StageType.scaleStage.toString(),
                           StageType.multiStage.toString(),
                           StageType.animationStage.toString()};
         for(String type : types){
             nifty.gotoScreen(type);
             List<Element> listmenu = nifty.getScreen(type).findElementByName("panel_options").getElements();
             Iterator<Element> it = listmenu.iterator();
             while(it.hasNext()){
                 Element element = it.next();
                 element.markForRemoval();
             }
         }
         nifty.gotoScreen("start");
         modelsb.initModels();
     }
 
     public void quitGame() {
         app.stop();
     }
 
     public void bind(Nifty nifty, Screen screen) {
         this.nifty = nifty;
     }
 
     public void onStartScreen() {
         
     }
 
     public void onEndScreen() {}
     
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         //Native application
         if(callback == null){
             selection = "";
             this.app = app;
             page = 0;
             index = 0;
             popUp = null;
             String systemLanguagePrefix = getSystemLanguage();
             String systemLanguage = I18N.getLanguage(config.getProperty(Configuration.LOCALE_PATH), systemLanguagePrefix);
             if (systemLanguage != null){
                 language = systemLanguage;
                 ArrayList<String> listLanguages = I18N.getListLanguage(config.getProperty(Configuration.LOCALE_PATH));
                 languagePage = listLanguages.indexOf(language);
                 hideLanguagePopup();
             }
             else{
                 ArrayList<String> listLanguages = I18N.getListLanguage(config.getProperty(Configuration.LOCALE_PATH));
                 if (listLanguages.size()>0){
                     language = listLanguages.get(0);
                 }
                 languagePage = 0;
             }
             i18nGui = new I18N(config.getProperty(Configuration.LOCALE_PATH),language);
             modelsb = new FamilyStageBuilder(nifty,control,i18nGui,getTexture("family-button"),language);
             changeLocale("");
             
             
         }
         //Called by API
         else{
             
         }
     }
     
     public void buildMenu(){
         String types[] = {StageType.singleStage.toString(),
                           StageType.scaleStage.toString(),
                           StageType.multiStage.toString(),
                           StageType.animationStage.toString()};
         for(final String type : types){
             for(int i = 0; i<stages.size(); i++){
                 final String pant = stages.get(i);
                 PanelBuilder menu;
                 menu = new PanelBuilder(){{
                     height("80%");
                     childLayoutVertical();
                     valign(VAlign.Bottom);
                     image(new ImageBuilder(){{
                         filename(control.getIconPathStage(pant));
                         align(Align.Center);
                     }});
                     image(new ImageBuilder(){{
                         id(pant+"menu-selection");
                         align(Align.Center);
                         filename(getMenu("menu-selection"));
                     }});
                 }};
                 menu.id(stages.get(i)+"Menu");
                 menu.interactOnClick("changeScreen("+stages.get(i)+")");
                 menu.build(nifty, nifty.getScreen(type), nifty.getScreen(type).findElementByName("panel_options"));
                 if(i<stages.size()-1){
                     new ImageBuilder(){{
                         valign(VAlign.Center);
                         filename(getMenu("header-leftseparator"));
                     }}.build(nifty, nifty.getScreen(type), nifty.getScreen(type).findElementByName("panel_options"));
                 }
                 nifty.getScreen(type).findElementByName(stages.get(i)+"menu-selection").setVisible(false);
             }
         }          
     }
     
     public void changeCharacterPage(String steep){
         modelSelection = modelsb.changeCharacterPage(i18nFamily,steep);    
     }
     
     /**
      * This method is invoked once the user clicks the right-pointed arrow to start personalizing the 
      * model. 
      */
     public void loadFirstScreen(){
         
         // First, iterate layers to make loading indicators visible
        List<Element> layers = this.nifty.getCurrentScreen().getLayerElements();
         for (Element e:layers){
            if (e.getId().equals("progressLayer") || e.getId().equals("Loading")){
                e.setVisible(true);
 
            }
         }
    
         // Set mustLoad to true. This will trigger the model loading process in the next update cycle.
         // (A full cycle is skipped to ensure Nifty has time to get updated as to reflect the changes
         // done to the visibility of the elements used to convey loading progress).
       mustLoad=true;
     }
     
     public void changeScalePage(String steep){
         changePage(steep);
         scalesb.changeScalePage(page, selection);
     }
     
     public void changeTab(String param){
         if(selection.equals("")){
             animationsb.changeTab(param, selection);
         }
         else{
             if(control.getStageTypes(selection).toString().equals(StageType.scaleStage.toString())){
                 changePage("0");
                 scalesb.changeTab(param, selection);
             }
         }
     }
     
     public void changeScreen(String param){
         int oldIndex = index;
         selection = param;
         if(param.equals("+")){
             index++;
             if(index==stages.size()){
                 selection = "";
             }
             else{
                 selection = stages.get(index);
             }
             
         }else{
             if(param.equals("-")){
                 index--;
                 if(index>=0){
                     selection = stages.get(index);
                 }
                 else{
                     selection= "";
                 }
             }else{
                 if(param.equals("animationStage")){
                     selection ="";
                     index=stages.size();
                 }
                 else{
                     index = stages.lastIndexOf(selection);
                 }
             }
         }
         if(!selection.equals("")){
             String stage = control.getStageTypes(selection).toString();
             String oldStage;
             if(oldIndex==stages.size()){
                 oldStage = "animationStage";
             }
             else{
                 oldStage = control.getStageTypes(stages.get(oldIndex)).toString();
             }
             if(!oldStage.equals(stage)){
                 nifty.gotoScreen(stage);
                 /*if(!stage.equals("animationStage")){
                     control.defaultCameraView();
                 }*/
             }
             loadScreen(stage);
         }
         else{
             if(index < 0){
                 control.removeModel();
                 loadFamilyScreen();
             }
             else{
                 nifty.gotoScreen("animationStage");
                 loadScreen("animationStage");
             }
         }
         
     }
     
     public void loadScreen(String type){
         loadMenu(type);
         if(type.equals(StageType.singleStage.toString())){
             changeTexturePage("0");
         }
         if(type.equals(StageType.scaleStage.toString())||type.equals(StageType.animationStage.toString())){
             changeTab("basic");
         }
         if(type.equals(StageType.multiStage.toString())){
             changeMultiTexturePage("0");
         }
     }
     
     public void loadMenu(String type){
         Iterator<String> it = stages.iterator();
         while(it.hasNext()){
             String auxStage = it.next();
                 nifty.getScreen(type).findElementByName(auxStage+"menu-selection").setVisible(false);
                 if(auxStage.equals(selection)){
                     nifty.getScreen(type).findElementByName(auxStage+"menu-selection").setVisible(true);
                 }
         }
     }
     
     /******************************ChangePageImages*****************************/
     
     public void changeTexturePage(String steep){
         changePage(steep);
         singlesb.showTexturePage(selection,page);
     }
     
     public void changeMultiTexturePage(String steep){
         changePage(steep);
         multisb.showTexturePage(selection, page);
     }
     
     public void changePageSubStage(String t, String steep){
         int h = Integer.valueOf(t);
         multisb.showSubTexturePage(selection, h, page, steep);
     }
     
     //Change page of textures or bones
     
     public void changePage(String steep){
         if(steep.equals("+")){
             page++;
         }
         if(steep.equals("-")){
             page--;
         }
         if(steep.equals("0")){
             page = 0;
         }
     }
     
     /******************************LoadGuiImages*****************************/
     
     public String getMenu(String param){
         if(param.equals("s1-header")){
             return Resources.s1_header;
         }
         if(param.equals("logo")){
             return Resources.logo;
         }
         if(param.equals("s1-settings")){
             return Resources.s1_settings;
         }
         if(param.equals("s1-settings-over")){
             return Resources.s1_settings_over;
         }
         if(param.equals("s2-header-left")){
             return Resources.s2_header_left;
         }
         if(param.equals("header-leftseparator")){
             return Resources.header_left_separator;
         }
         if(param.equals("s2-header-right")){
             return Resources.s2_header_right;
         }
         if(param.equals("header-rightseparator")){
             return Resources.header_right_separator;
         }
         if(param.equals("export")){
             return Resources.export;
         }
         if(param.equals("export-over")){
             return Resources.export_over;
         }
         if(param.equals("export-selection")){
             return Resources.export_selection;
         }
         if(param.equals("s2-settings")){
             return Resources.s2_settings;
         }
         if(param.equals("s2-settings-over")){
             return Resources.s2_settings_over;
         }
         if(param.equals("menu-selection")){
             return Resources.menu_selection;
         }
         return null;
     }
     
     public String getFont(String size){
         
         if(size.equals("15")){
             return Resources.font_15;
         }
         if(size.equals("20")){
             return Resources.font_20;
         }
         if(size.equals("30")){
             return Resources.font_30;
         }
         return null;
     }
     
     public String getButton(String param){
         if(param.equals("left")){
             return Resources.button_left;
         }
         if(param.equals("left-over")){
             return Resources.button_left_over;
         }
         if(param.equals("right")){
             return Resources.button_right;
         }
         if(param.equals("right-over")){
             return Resources.button_right_over;
         }
         if(param.equals("up")){
             return Resources.button_up;
         }
         if(param.equals("up-over")){
             return Resources.button_up_over;
         }
         if(param.equals("down")){
             return Resources.button_down;
         }
         if(param.equals("down-over")){
             return Resources.button_down_over;
         }
         if(param.equals("info")){
             return Resources.button_info;
         }
         if(param.equals("info-over")){
             return Resources.button_info_over;
         }
         if(param.equals("color")){
             return Resources.button_color;
         }
         if(param.equals("color-over")){
             return Resources.button_color_over;
         }
         if(param.equals("next")){
             return Resources.button_next;
         }
         if(param.equals("next-over")){
             return Resources.button_next_over;
         }
         if(param.equals("previous")){
             return Resources.button_previous;
         }
         if(param.equals("previous-over")){
             return Resources.button_previous_over;
         }
         if(param.equals("cancel")){
             return Resources.button_cancel;
         }
         if(param.equals("cancel-over")){
             return Resources.button_cancel_over;
         }
         if(param.equals("accept")){
             return Resources.button_accept;
         }
         if(param.equals("accept-over")){
             return Resources.button_accept_over;
         }
         if(param.equals("more")){
             return Resources.button_more;
         }
         if(param.equals("more-over")){
             return Resources.button_more_over;
         }
         if(param.equals("button")){
             return Resources.button;
         }
         if(param.equals("button-over")){
             return Resources.button_over;
         }
         if(param.equals("selector")){
             return Resources.selector;
         }
         if(param.equals("up-selector")){
             return Resources.selector_up;
         }
         if(param.equals("up-selector-over")){
             return Resources.selector_up_over;
         }
         if(param.equals("down-selector")){
             return Resources.selector_down;
         }
         if(param.equals("down-selector-over")){
             return Resources.selector_down_over;
         }
         if(param.equals("close-language")){
             return Resources.close_language;
         }
         if(param.equals("close-language-over")){
             return Resources.close_language_over;
         }
         return null;
     }
     
     public String getTexture(String param){
         if(param.equals("flag")){
             return Resources.flag;
         }
         if(param.equals("s1-background")){
             return Resources.s1_background;
         }
         if(param.equals("s1-leftpanel")){
             return Resources.s1_left_panel;
         }
         if(param.equals("family-button")){
             return Resources.button_family;
         }
         if(param.equals("family-button-over")){
             return Resources.button_family_over;
         }
         if(param.equals("s1-rightpanel")){
             return Resources.s1_right_panel;
         }
         if(param.equals("s2-right-panel")){
             return Resources.s2_right_panel;
         }
         if(param.equals("model")){
             return Resources.model;
         }
         if(param.equals("s2-separator")){
             return Resources.s2_separator;
         }
         if(param.equals("red")){
             return Resources.red;
         }
         if(param.equals("green")){
             return Resources.green;
         }
         if(param.equals("blue")){
             return Resources.blue;
         }
         if(param.equals("background-popup")){
             return Resources.background_popup;
         }
         if(param.equals("background-popup-custom")){
             return Resources.background_popup_custom;
         }
         if(param.equals("background-popup-dialog")){
             return Resources.background_popup_dialog;
         }
         if(param.equals("background-popup-language")){
             return Resources.background_popup_language;
         }
         if(param.equals("tab-l")){
             return Resources.tab_left;
         }
         if(param.equals("tab-r")){
             return Resources.tab_rigth;
         }
         if(param.equals("eCharacter")){
             return Resources.eCharacter;
         }
         if(param.equals("x")){
             return Resources.x;
         }
         return null;
     }
     
     public String getTick(){
         return Resources.tick;
     }
     
     public void changeTextureOrSubMesh(String substage, String idTextureOrSubMesh){
         if(control.getStageTypes(selection) == StageType.singleStage){
             singlesb.changeTextureOrSubMesh(selection,page,substage,idTextureOrSubMesh);
             textureOrSubMeshSelected = singlesb.getTextureOrSubMesh();
             subStageSelected = singlesb.getSubStage(selection, page, substage);
         }
         if(control.getStageTypes(selection) == StageType.multiStage){
             multisb.changeTextureOrSubMesh(selection,page,substage,idTextureOrSubMesh);
             textureOrSubMeshSelected = multisb.getTextureOrSubMesh(substage);
             subStageSelected = multisb.getSubStage(selection, page, substage);
         }
     }
     
   /******************************FamilyControler*****************************/
   
   public void changeFamilyPage(String steep){
       modelsb.showFamilyPage(steep);
   } 
   
   public void selectFamily(String id){
       int i = modelsb.selectFamily(id,getTexture("family-button"),getTexture("family-button-over"));
       control.selectFamily(families.get(i));
       i18nFamily = new I18N(control.getLanguageFamilyPath(),language);
       changeCharacterPage("0");
   }
     
   /******************************LocaleDropDownControler*****************************/  
   
   public void changeLocale(String steep) {
         if(steep.equals("+")){
             languagePage++;
         }
         if(steep.equals("-")){
             languagePage--;
         }
         ArrayList<String> listLanguages = I18N.getListLanguage(config.getProperty(Configuration.LOCALE_PATH));
         language = listLanguages.get(languagePage);
         //config.setProperty(Configuration.LANGUAGE, language);
         i18nGui = new I18N(config.getProperty(Configuration.LOCALE_PATH),language);
         modelsb.changeLocale(i18nGui,language);
         if(languagePage > 0){
             nifty.getScreen("start").findElementByName("uplanguage").setVisible(true);
         }
         else{
             nifty.getScreen("start").findElementByName("uplanguage").setVisible(false);
         }
         if((listLanguages.size() - languagePage) > 1){
             nifty.getScreen("start").findElementByName("downlanguage").setVisible(true);
         }
         else{
             nifty.getScreen("start").findElementByName("downlanguage").setVisible(false);
         }
   }
     
     /******************************FinishButtonControler*****************************/
     
     public void export() {
         popUp.export();
     }
     
     /******************************PopUpsControler*****************************/
     
     public void popUpButtonClicked(String id) {
         int sel = popUp.popUpButtonClicked(id);
         if(sel == 1){
                 index = 0;
                 selection = stages.get(index);
                 loadScreen(control.getStageTypes(selection).toString());
                 nifty.gotoScreen(control.getStageTypes(selection).toString());
         }
         if(sel==2){
             loadFamilyScreen();
         }
         
         if(sel==4){
             quitGame();
         }
     }
     
     /******************************PopUpColorControler*****************************/
     
     public void showWindowChangeColor(String h) throws InterruptedException{
         if(control.getStageTypes(selection) == StageType.singleStage){
             textureOrSubMeshSelected = singlesb.getTextureOrSubMesh();
             subStageSelected = singlesb.getSubStage();
         }
         if(control.getStageTypes(selection) == StageType.multiStage){
             textureOrSubMeshSelected = multisb.getTextureOrSubMesh(h);
             subStageSelected = multisb.getSubStage(h);
         }
         popUp.showWindowChangeColor(subStageSelected,textureOrSubMeshSelected);
     }
     
     public void changePopUpPage(String steep){
         popUp.changePopUpPage(steep,subStageSelected,textureOrSubMeshSelected);
     }
     
     public void changePopUpColor(String im){
         popUp.changePopUpColor(im,subStageSelected,textureOrSubMeshSelected);
     }
     
     public void changeSliderColor(String color, String id){
         popUp.changeSliderColor(color);
         popUp.unCheck();
         popUp.check(id);
     }
     
     @NiftyEventSubscriber(id="sliderR")
     public void onRedSliderChange(final String id, final SliderChangedEvent event) {
         popUp.onRedSliderChange(event.getValue());
     }
 
     @NiftyEventSubscriber(id="sliderG")
     public void onGreenSliderChange(final String id, final SliderChangedEvent event) {
         popUp.onGreenSliderChange(event.getValue());
     }
 
     @NiftyEventSubscriber(id="sliderB")
     public void onBlueSliderChange(final String id, final SliderChangedEvent event) {
         popUp.onBlueSliderChange(event.getValue());
     }
     
     public void acceptButtonClicked() throws InterruptedException, IOException {
         popUp.accept(textureOrSubMeshSelected,subStageSelected);
     }
     
     public void cancelButtonClicked() {
         popUp.cancel(); 
     }
     
     public void showMoreColors(){
         popUp.showMoreColors();
     }
     
     public void changeTabColor(String option){
         popUp.changeTabColor(option);
     }
     
     /******************************SliderControler*****************************/
     
     @NiftyEventSubscriber(id="slider0")
     public void onSlider0Change(final String id, final SliderChangedEvent event) {
         float inc = 1.0f + event.getValue() * 0.01f;
         control.setBoneControllerValue(idBones.get(page*BONES_PAGE), inc);
         control.setDefaultValueBoneController(idBones.get(page*BONES_PAGE),event.getValue());
     }
     
     @NiftyEventSubscriber(id="slider1")
     public void onSlider1Change(final String id, final SliderChangedEvent event) {
         float inc = 1.0f + event.getValue() * 0.01f;
         control.setBoneControllerValue(idBones.get(page*BONES_PAGE+1), inc);
         control.setDefaultValueBoneController(idBones.get(page*BONES_PAGE+1),event.getValue());
 
     }
     
     @NiftyEventSubscriber(id="slider2")
     public void onSlider2Change(final String id, final SliderChangedEvent event) {
         float inc = 1.0f + event.getValue() * 0.01f;
         control.setBoneControllerValue(idBones.get(page*BONES_PAGE+2), inc);
         control.setDefaultValueBoneController(idBones.get(page*BONES_PAGE+2),event.getValue());
     }
     
     @NiftyEventSubscriber(id="slider3")
     public void onSlider3Change(final String id, final SliderChangedEvent event) {
         float inc = 1.0f + event.getValue() * 0.01f;
         control.setBoneControllerValue(idBones.get(page*BONES_PAGE+3), inc);
         control.setDefaultValueBoneController(idBones.get(page*BONES_PAGE+3),event.getValue());
     }
     
     @NiftyEventSubscriber(id="slider4")
     public void onSlider4Change(final String id, final SliderChangedEvent event) {
         float inc = 1.0f + event.getValue() * 0.01f;
         control.setBoneControllerValue(idBones.get(page*BONES_PAGE+4), inc);
         control.setDefaultValueBoneController(idBones.get(page*BONES_PAGE+4),event.getValue());
     }
     
     /******************************PhysicalBuildControler*****************************/
     
     public void changeBodyType(String bodyType){
         control.setPhysicalBuild(idPhysicalBuild.get(page*BONES_PAGE+Integer.parseInt(bodyType)));
     }
     
     /******************************AnimationsControler*****************************/
     
     public void preview(String animation){
         animationsb.showAnimation(animation);
     }
     
     public void cameraPreview(String camera){
         animationsb.showCamera(camera);
     }
     
     public void changePageAnimations(String selection, String steep){
         animationsb.showPage(selection, steep);
     }
     
     @NiftyEventSubscriber(id="aCheckBox0")
     public void aCheckBox0Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("a",0,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="aCheckBox1")
     public void aCheckBox1Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("a",1,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="aCheckBox2")
     public void aCheckBox2Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("a",2,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="qCheckBox0")
     public void qCheckBox0Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("q",0,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="qCheckBox1")
     public void qCheckBox1Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("q",1,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="qCheckBox2")
     public void qCheckBox2Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("q",2,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="cCheckBox0")
     public void cCheckBox0Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("c",0,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="cCheckBox1")
     public void cCheckBox1Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("c",1,event.isChecked());
     }
     
     @NiftyEventSubscriber(id="cCheckBox2")
     public void cCheckBox2Changed(final String id, final CheckBoxStateChangedEvent event){
         animationsb.checkOrUncheck("c",2,event.isChecked());
     }
     
     public void checkAll(String id, String bool){
         boolean b = false;
         if(bool.equals("true")){
             b = true;
         }
         animationsb.checkAll(id,b);
     }
     
     public void showWeb(){
         try {
             if (Desktop.isDesktopSupported()) {
                 Desktop desktop = Desktop.getDesktop();
                 if (desktop.isSupported(Desktop.Action.BROWSE)) {
                     desktop.browse(new URI("http://character.e-ucm.es"));
                 }
             }
         } catch (Exception e) {
                 e.printStackTrace();
         }
     }
     
     private String getSystemLanguage(){
         Locale locale = Locale.getDefault();
         return locale.getLanguage();
     }
     
     public void showInfo(String family){
         modelsb.showInfo(family);
     }
     public void closeInfo(){
         nifty.getScreen("start").getLayerElements().get(2).setVisible(false);
     }
     public void hideLanguagePopup(){
         nifty.getScreen("start").getLayerElements().get(3).setVisible(false);
     }
     public void showRepository(){
         repository = new RepositoryReader();
         String stageType = "start";
         nifty.getScreen(stageType).findElementByName("repoWelcome").getRenderer(TextRenderer.class).setText(i18nGui.getString("idWelcome"));
         nifty.getScreen(stageType).findElementByName("welcomeRepoPanel").layoutElements();
         nifty.getScreen(stageType).findElementByName("repoDescText").getRenderer(TextRenderer.class).setText(i18nGui.getString("idRepoText"));
         nifty.getScreen(stageType).findElementByName("repoDescPanel").layoutElements();
         nifty.getScreen(stageType).findElementByName("downloadText").getRenderer(TextRenderer.class).setText(i18nGui.getString("idDownload"));
         nifty.getScreen(stageType).findElementByName("download").layoutElements();
         nifty.getScreen(stageType).getLayerElements().get(4).setVisible(true);
         changeRepository("0");
     }
     public void hideRepository(){
         nifty.getScreen("start").getLayerElements().get(4).setVisible(false);
     }
     public void changeRepository(String steep){
         if(steep.equals("+")){
             repositoryPage++;
         }
         if(steep.equals("-")){
             repositoryPage--;
         }
         if(steep.equals("0")){
             repositoryPage=0;
         }
         ArrayList<String> listFamilies = repository.getFamiliesID();
         String family = listFamilies.get(repositoryPage);
         nifty.getScreen("start").findElementByName("RepoDescFamText").getRenderer(TextRenderer.class).setText(repository.getDescriptionFamily(family));
         nifty.getScreen("start").findElementByName("RepoDescFamPanel").layoutElements();
         i18nGui = new I18N(config.getProperty(Configuration.LOCALE_PATH),language);
         Element image = nifty.getScreen("start").findElementByName("familyRepo");
         ImageRenderer imager = image.getRenderer(ImageRenderer.class);
         String imagePath = repository.getIconPathFamily(family);
         if(imagePath!=null){
             imager.setImage(nifty.getRenderEngine().createImage(imagePath, false));
         }
         else{
             imager.setImage(nifty.getRenderEngine().createImage(getTexture("x"), false));
         }
         if(repositoryPage > 0){
             nifty.getScreen("start").findElementByName("familyRepoLeft").setVisible(true);
         }
         else{
             nifty.getScreen("start").findElementByName("familyRepoLeft").setVisible(false);
         }
         if((repository.getNumFamilies() - repositoryPage) > 1){
             nifty.getScreen("start").findElementByName("familyRepoRight").setVisible(true);
         }
         else{
             nifty.getScreen("start").findElementByName("familyRepoRight").setVisible(false);
         }
     }
     public void downloadFamily(){
         ArrayList<String> listFamilies = repository.getFamiliesID();
         String idFamily = listFamilies.get(repositoryPage);
         repository.downloadFamily(idFamily);
         control.refreshFamilies();
         modelsb.initModels();
     }
     
     /**
      * Only checks if the loading progress must be started
      * @param tpf 
      */
     public void update(float tpf) {
         if (mustLoad){
             loading = true;
             mustLoad=false;
         } else if (loading){
             mustLoad=loading=false;
             doLoad();
         }
     //System.out.println("UPDATING");
     }
 
     /**
      * Starts loading the model for customization. This method is only invoked by update() after method
      * loadFirstScreen() sets mustLoad:=true.
      */
     private void doLoad() {
                 index = 0;
         control.selectModel(modelSelection);
         i18nModel = new I18N(control.getLanguageModelPath(),language);
         stages = control.getStagesLabels();
         selection = stages.get(index);
         scalesb = new ScaleStageBuilder(nifty,control,i18nFamily,i18nModel,i18nGui);
         singlesb = new SingleStageBuilder(nifty,control,i18nGui,i18nModel);
         multisb = new MultiStageBuilder(nifty, control, i18nGui, i18nFamily, i18nModel);
         animationsb = new AnimationStageBuilder(nifty, control, i18nGui, i18nFamily);
         if(popUp == null){
             popUp = new PopUpBuilder(nifty, control, i18nGui,i18nModel);
         }
         nifty.gotoScreen("scaleStage");
         ArrayList<String> idPanel = control.getIdsSubStages(selection);
         idPhysicalBuild = control.getIdsPhysicalBuild(idPanel.get(0));
         idBones = control.getIdBonesController(selection);
         buildMenu();
         
         loadScreen(control.getStageTypes(selection).toString());
         nifty.gotoScreen(control.getStageTypes(selection).toString());
 
     }
 }
