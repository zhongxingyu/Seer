 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.engine;
 
 import com.trollsahead.qcumberless.device.Device;
 import com.trollsahead.qcumberless.gui.*;
 import com.trollsahead.qcumberless.gui.elements.*;
 import com.trollsahead.qcumberless.model.*;
 import com.trollsahead.qcumberless.plugins.Plugin;
 import com.trollsahead.qcumberless.util.ConfigurationManager;
 import com.trollsahead.qcumberless.util.ElementHelper;
 import com.trollsahead.qcumberless.util.FileUtil;
 import com.trollsahead.qcumberless.util.Util;
 
 import static com.trollsahead.qcumberless.gui.elements.Element.ColorScheme;
 import static com.trollsahead.qcumberless.gui.elements.Element.ROOT_STEP_DEFINITIONS;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.*;
 import java.util.List;
 
 public class DesignerEngine implements CucumberlessEngine {
     private static final int SCROLL_WHEEL_IMPACT_CANVAS = BaseBarElement.RENDER_HEIGHT_MINIMUM;
 
     public static RootElement cucumberRoot = null;
     public static RootElement featuresRoot = null;
     public static RootElement stepsRoot = null;
 
     private static enum DragMode {NOT_DRAGGING, DRAGGING_CANVAS, DRAGGING_TERMINAL}
     private static DragMode dragMode = DragMode.NOT_DRAGGING;
 
     private static Element oldTouchedElement = null;
     private static Element touchedElement = null;
     private static RootElement touchedRootElement = null;
 
     public static List<Step> stepDefinitions = null;
 
     public static Element lastAddedElement = null;
 
     public static int dragSplitterX = 0;
 
     private static boolean canvasHasMouseFocus = true;
     public static int canvasHeight;
 
     public static ButtonBar buttonBar;
     public static Spotlight spotlight;
 
     public static String featuresBaseDir = null;
     public static List<File> featuresToDeleteWhenSaving = null;
 
     public static Set<String> runTags = new HashSet<String>();
     public static String tagsFilter = null;
 
     public static ColorScheme colorScheme = ColorScheme.DESIGN;
 
     public static DesignerEngine instance = null;
 
     public DesignerEngine() {
         RunHistory.initialize();
 
         buttonBar = new ButtonBar();
         spotlight = new Spotlight();
         instance = this;
 
         cucumberRoot = new RootElement();
         scratchFeatures(false);
         resetStepDefinitions(false);
 
         Terminal.initialize();
         FlashingMessageManager.initialize();
     }
 
     public void initialize() {
         if (!Util.isEmpty(ConfigurationManager.get("importFeaturesOnStartup"))) {
             importFeatures(new File[]{new File(ConfigurationManager.get("featuresPath"))});
         } else {
             scratchFeatures(true);
         }
         if (!Util.isEmpty(ConfigurationManager.get("importStepDefinitionsOnStartup"))) {
             importStepDefinitions(Engine.plugins.get(0));
         } else {
             resetStepDefinitions(true);
         }
         if (!Util.isEmpty(ConfigurationManager.get("enableDeviceOnStartup"))) {
             DeviceManager.enableDeviceWithName(ConfigurationManager.get("enableDeviceOnStartup"));
         }
     }
 
     public void show() {
         resize();
     }
 
     public void hide() {
     }
 
     public void update() {
         Terminal.update();
         updateHighlight();
         buttonBar.update();
         spotlight.update();
         cucumberRoot.update(System.currentTimeMillis());
         EasterEgg.update();
         FlashingMessageManager.update();
     }
 
     public void render(Graphics2D g) {
         calculateCanvasHeight();
         clear(g);
         cucumberRoot.render(g);
         buttonBar.render(g);
         spotlight.render(g);
         Player.render(g);
         FlashingMessageManager.render(g);
         cucumberRoot.renderHints(g);
         renderFps(g);
         Terminal.render(g);
         if (DropDown.isVisible) {
             DropDown.render(g);
         }
     }
 
     public void postRender() {
         cucumberRoot.stickToParentRenderPosition(false);
     }
 
     public void renderOnlyElements(Graphics2D g) {
         canvasHeight = Engine.windowHeight;
         cucumberRoot.render(g);
         cucumberRoot.renderHints(g);
         FlashingMessageManager.render(g);
     }
 
     public void resize() {
         synchronized (Engine.DATA_LOCK) {
             buttonBar.resize();
             Terminal.resize();
             calculateCanvasHeight();
             updateRootPositions();
         }
     }
 
     public void mouseMoved() {
         if (DropDown.mouseMoved()) {
             canvasHasMouseFocus = false;
             return;
         }
         if (EditBox.mouseMoved()) {
             canvasHasMouseFocus = false;
             return;
         }
         canvasHasMouseFocus = true;
     }
 
     public void mouseWheelMoved(int unitsToScroll) {
         if (isMouseInsideCanvasArea()) {
             if (CumberlessMouseListener.mouseX < dragSplitterX) {
                 featuresRoot.scroll(-unitsToScroll * SCROLL_WHEEL_IMPACT_CANVAS);
             } else {
                 stepsRoot.scroll(-unitsToScroll * SCROLL_WHEEL_IMPACT_CANVAS);
             }
         } else if (isMouseInsideTerminalArea()) {
             Terminal.scroll(unitsToScroll);
         }
     }
 
     public void click(int clickCount) {
         synchronized (Engine.DATA_LOCK) {
             if (EditBox.click()) {
                 return;
             }
             if (buttonBar.click()) {
                 return;
             }
             if (Terminal.click()) {
                 return;
             }
             if (touchedElement != null) {
                 touchedElement.click(clickCount);
             }
         }
     }
 
     public void keyPressed(KeyEvent keyEvent) {
         if (!EditBox.isVisible) {
             synchronized (Engine.DATA_LOCK) {
                 spotlight.searchKeyPressed(keyEvent);
             }
             if (keyEvent.getKeyChar() == '!') {
                 Engine.fpsShow = !Engine.fpsShow;
             }
             if (Util.isEmpty(spotlight.searchString)) {
                 if (keyEvent.getKeyChar() == ' ') {
                     switchColorScheme();
                 }
                 if (keyEvent.getKeyChar() == '+') {
                     stepsRoot.unfoldAll();
                     featuresRoot.unfoldAll();
                 }
                 if (keyEvent.getKeyChar() == '-') {
                     stepsRoot.foldAll();
                     featuresRoot.foldAll();
                 }
             }
         }
     }
 
     public void startDrag(boolean isControlDown) {
         if (isMouseInsideCanvasArea()) {
             dragMode = DragMode.DRAGGING_CANVAS;
             synchronized (Engine.DATA_LOCK) {
                 if (touchedElement != null) {
                     if (touchedElement.isDragable()) {
                         touchedElement.startDrag(isControlDown);
                     }
                 } else if (touchedRootElement != null) {
                     touchedRootElement.startDrag(isControlDown);
                 }
             }
         } else if (isMouseInsideTerminalArea()) {
             dragMode = DragMode.DRAGGING_TERMINAL;
             Terminal.mouseDragged();
         } else {
             dragMode = DragMode.NOT_DRAGGING;
         }
     }
 
     public void endDrag() {
         if (dragMode == DragMode.NOT_DRAGGING) {
             return;
         }
         if (dragMode == DragMode.DRAGGING_CANVAS) {
             synchronized (Engine.DATA_LOCK) {
                 if (touchedElement != null) {
                     touchedElement.endDrag();
                 } else if (touchedRootElement != null) {
                     touchedRootElement.endDrag();
                 }
             }
         } else if (dragMode == DragMode.DRAGGING_TERMINAL) {
             Terminal.mouseDragged();
         }
         dragMode = DragMode.NOT_DRAGGING;
     }
 
     public void updateDrag() {
         if (dragMode == DragMode.NOT_DRAGGING || !CumberlessMouseListener.isButtonPressed) {
             return;
         }
         if (dragMode == DragMode.DRAGGING_CANVAS) {
             synchronized (Engine.DATA_LOCK) {
                 if (touchedElement != null) {
                     if (touchedElement.isBeingDragged()) {
                         touchedElement.applyDragOffset();
                     }
                 } else if (touchedRootElement != null && touchedRootElement.isDragable()) {
                     touchedRootElement.scroll(CumberlessMouseListener.mouseY - CumberlessMouseListener.oldMouseY);
                 }
             }
         } else if (dragMode == DragMode.DRAGGING_TERMINAL) {
             Terminal.mouseDragged();
         }
     }
 
     public void clear(Graphics2D g) {
         if (!EasterEgg.isBackgroundCoveringCanvas()) {
             Engine.drawBackgroundPicture(g);
         }
         if (EasterEgg.animation > 0) {
             EasterEgg.render(g);
         }
     }
 
     private void renderFps(Graphics g) {
         if (!Engine.fpsShow) {
             return;
         }
         String str = "FPS: " + Engine.fpsLastCount;
         int x = (Engine.windowWidth - Engine.fontMetrics.stringWidth(str)) / 2;
         int y = canvasHeight - 5 - ButtonBar.BUTTONBAR_HEIGHT;
         g.setColor(Color.BLACK);
         g.drawString(str, x + 1, y + 1);
         g.setColor(Color.WHITE);
         g.drawString(str, x, y);
     }
 
     public void updateDevices(Set<Device> devices) {
         if (Player.isRunning()) {
             return;
         }
         buttonBar.updateDevices(devices);
         Terminal.updateDevices(devices);
     }
 
     public static void runTests(BaseBarElement cucumberTextElement) {
         runTests(cucumberTextElement, !Util.isEmpty(tagsFilter) ? Util.stringToTagSet(tagsFilter) : runTags);
     }
 
     public static void runTests(BaseBarElement cucumberTextElement, Set<String> tags) {
         List<BaseBarElement> features = new LinkedList<BaseBarElement>();
         features.add(cucumberTextElement);
         runTests(features, tags);
     }
 
     public static void runTests() {
         runTests(!Util.isEmpty(tagsFilter) ? Util.stringToTagSet(tagsFilter) : runTags);
     }
 
     public static void runTests(Set<String> tags) {
         List<BaseBarElement> features = new LinkedList<BaseBarElement>();
         for (int i = 0; i < featuresRoot.children.size(); i++) {
             features.add((BaseBarElement) featuresRoot.children.get(i));
         }
         runTests(features, tags);
     }
 
     public static void runTests(List<BaseBarElement> features, Set<String> tags) {
         Player.prepareRun(ColorScheme.PLAY);
         for (final Device device : Engine.devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.PLAY)) {
                 new Player().play(features, device, tags);
             }
         }
     }
 
     public static void runInSingleStepMode(List<StringBuilder> features) {
         Player.prepareSingleStepMode();
         for (final Device device : Engine.devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.PLAY)) {
                 new Player().playInStepMode(features, device, new HashSet<String>());
             }
         }
     }
 
     public static void runInStepMode(StepElement stepElement) {
         if (Player.isAtStepBreakpoint()) {
             for (Player player : Player.players) {
                 player.runStep(stepElement);
             }
         } else {
             Player.prepareStepMode();
             for (final Device device : Engine.devices) {
                 if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.STEP)) {
                     new Player().playInStepMode(stepElement, device, new HashSet<String>());
                 }
             }
         }
     }
 
     private static void updateHighlight() {
         if (!canvasHasMouseFocus) {
             return;
         }
         if (touchedElement != null && touchedElement.isBeingDragged()) {
             return;
         }
         findTouchedElement();
         if (touchedElement != oldTouchedElement) {
             toggleHighlight(oldTouchedElement, false);
             oldTouchedElement = touchedElement;
         }
         if (!isMouseInsideCanvasArea()) {
             return;
         }
         toggleHighlight(touchedElement, true);
     }
 
     private static void findTouchedElement() {
         if (touchedElement != null) {
             touchedElement = touchedElement.findElement(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         }
         if (touchedElement == null) {
             touchedElement = cucumberRoot.findElement(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         }
         touchedRootElement = CumberlessMouseListener.mouseX < dragSplitterX ? featuresRoot : stepsRoot;
     }
 
     private static void toggleHighlight(Element element, boolean highlight) {
         if (element != null) {
             element.highlight(highlight);
         }
     }
 
     public static boolean isMouseInsideCanvasArea() {
         return CumberlessMouseListener.mouseY < canvasHeight - ButtonBar.BUTTONBAR_HEIGHT;
     }
 
     public static boolean isMouseInsideButtonBarArea() {
         return CumberlessMouseListener.mouseY >= canvasHeight - ButtonBar.BUTTONBAR_HEIGHT && CumberlessMouseListener.mouseY <= canvasHeight;
     }
 
     public static boolean isMouseInsideTerminalArea() {
         return CumberlessMouseListener.mouseY > Engine.windowHeight - Terminal.getHeight();
     }
 
     public static void updateRootPositions() {
         dragSplitterX = BaseBarElement.RENDER_WIDTH_MAX_FEATURE_EDITOR + ((Engine.windowWidth - BaseBarElement.RENDER_WIDTH_MAX_STEP_DEFINITIONS - BaseBarElement.RENDER_WIDTH_MAX_FEATURE_EDITOR) / 2);
         int divider = Math.max(dragSplitterX, Engine.windowWidth - BaseBarElement.RENDER_WIDTH_MAX_STEP_DEFINITIONS - RootElement.PADDING_HORIZONTAL * 2);
         cucumberRoot.setBounds(0, 0, 0, 0);
         if (featuresRoot != null) {
             featuresRoot.setBounds(0, 10, divider - 20, canvasHeight);
         }
         if (stepsRoot != null) {
             stepsRoot.setBounds(divider, 10, Engine.windowWidth - divider, canvasHeight);
         }
     }
 
     private static void calculateCanvasHeight() {
         canvasHeight = Engine.windowHeight - Terminal.getHeight();
     }
 
     public static void importStepDefinitions(final Plugin plugin) {
         new Thread(new Runnable() {
             public void run() {
                 Map<String, List<StepDefinition>> stepDefinitionMap = plugin.getStepDefinitions();
                 synchronized (Engine.DATA_LOCK) {
                     CucumberStepDefinitionLoader.parseStepDefinitions(stepDefinitionMap);
                     featuresRoot.updateSteps();
                 }
             }
         }).start();
     }
 
     public static void importFeatures(File[] files) {
         if (files == null || files.length == 0) {
             Engine.resetFps();
             return;
         }
         synchronized (Engine.DATA_LOCK) {
             try {
                 FeatureLoader.parseFeatureFilesAndPushToDesignerRoot(FileUtil.getFeatureFiles(files));
             } catch (Exception e) {
                 e.printStackTrace();
             }
             featuresBaseDir = (files.length == 1 && files[0].isDirectory()) ? files[0].getAbsolutePath() : null;
             featuresRoot.isLoaded = true;
             UndoManager.reset();
             Engine.resetFps();
         }
     }
 
     public static void exportFeatures(File directory) {
         featuresRoot.export(directory);
         FlashingMessageManager.addMessage(new FlashingMessage("Features exported!", FlashingMessage.STANDARD_TIMEOUT));
         Engine.resetFps();
     }
 
     public static void saveFeatures() {
         if (featuresToDeleteWhenSaving != null) {
             for (File file : featuresToDeleteWhenSaving) {
                 file.delete();
             }
         }
        featuresRoot.save();
         FlashingMessageManager.addMessage(new FlashingMessage("Features saved!", FlashingMessage.STANDARD_TIMEOUT));
         Engine.resetFps();
     }
 
     public static void scratchFeatures(boolean addTemplate) {
         synchronized (Engine.DATA_LOCK) {
             resetFeatures();
             if (addTemplate) {
                 addTemplateFeature();
             }
         }
         Engine.resetFps();
     }
 
     public static void resetStepDefinitions(boolean addTemplate) {
         synchronized (Engine.DATA_LOCK) {
             stepDefinitions = new ArrayList<Step>();
 
             cucumberRoot.removeChild(stepsRoot);
             stepsRoot = new RootElement();
             stepsRoot.rootType = ROOT_STEP_DEFINITIONS;
             cucumberRoot.addChild(stepsRoot, 1);
 
             if (addTemplate) {
                 stepsRoot.addChild(new FeatureElement(BaseBarElement.ROOT_STEP_DEFINITIONS, com.trollsahead.qcumberless.model.Locale.getString("feature")));
                 stepsRoot.addChild(new ScenarioElement(BaseBarElement.ROOT_STEP_DEFINITIONS, com.trollsahead.qcumberless.model.Locale.getString("scenario")));
                 stepsRoot.addChild(new ScenarioOutlineElement(BaseBarElement.ROOT_STEP_DEFINITIONS, com.trollsahead.qcumberless.model.Locale.getString("scenario outline")));
                 stepsRoot.addChild(new BackgroundElement(BaseBarElement.ROOT_STEP_DEFINITIONS, com.trollsahead.qcumberless.model.Locale.getString("background")));
                 stepsRoot.addChild(new CommentElement(BaseBarElement.ROOT_STEP_DEFINITIONS, com.trollsahead.qcumberless.model.Locale.getString("comment")));
                 BaseBarElement stepElement = new StepElement(BaseBarElement.ROOT_STEP_DEFINITIONS, com.trollsahead.qcumberless.model.Locale.getString("new step"));
                 stepElement.step.setMatchedByStepDefinition(false);
                 stepsRoot.addChild(stepElement);
             }
 
             updateRootPositions();
         }
         Engine.resetFps();
     }
 
     public static void resetFeatures() {
         featuresBaseDir = null;
         featuresToDeleteWhenSaving = null;
         cucumberRoot.removeChild(featuresRoot);
         featuresRoot = new RootElement();
         cucumberRoot.addChild(featuresRoot, 0);
         featuresRoot.isLoaded = false;
         updateRootPositions();
         UndoManager.reset();
         Engine.resetFps();
     }
 
     private static void addTemplateFeature() {
         BaseBarElement scenario = new ScenarioElement(BaseBarElement.ROOT_FEATURE_EDITOR, "New Scenario");
         scenario.unfold();
         BaseBarElement feature = new FeatureElement(BaseBarElement.ROOT_FEATURE_EDITOR, "New Feature");
         feature.setFilename("noname_" + System.currentTimeMillis() + ".feature");
         feature.addChild(scenario);
         feature.unfold();
         featuresRoot.addChild(feature);
         updateLastAddedElement(scenario);
     }
 
     public static void updateLastAddedElement(Element element) {
         if (element.rootType != ROOT_STEP_DEFINITIONS) {
             lastAddedElement = element;
             UndoManager.takeSnapshot(featuresRoot);
         }
     }
 
     public static void deleteFeatureFromFilesystemWhenSaving(BaseBarElement feature) {
         if (feature.type != BaseBarElement.TYPE_FEATURE) {
             return;
         }
         if (Util.isEmpty(feature.getFilename())) {
             return;
         }
         if (featuresToDeleteWhenSaving == null) {
             featuresToDeleteWhenSaving = new LinkedList<File>();
         }
         featuresToDeleteWhenSaving.add(new File(feature.getFilename()));
     }
 
     public static List<String> getDefinedTags() {
         return Arrays.asList(featuresRoot.getTags().toArray(new String[0]));
     }
 
     public static List<String> getDefinedTags(int ... typeFilter) {
         return Arrays.asList(featuresRoot.getTags(typeFilter).toArray(new String[0]));
     }
 
     public static void toggleRunTag(String tag) {
         String negatedTag = Util.negatedTag(tag);
         if (runTags.contains(tag)) {
             runTags.remove(tag);
             runTags.add(negatedTag);
         } else if (runTags.contains(negatedTag)) {
             runTags.remove(negatedTag);
         } else {
             runTags.add(tag);
         }
     }
 
     public static boolean isRunTagEnabled(String tag) {
         return runTags.contains(tag);
     }
 
     public static void setColorScheme(ColorScheme colorScheme) {
         DesignerEngine.colorScheme = colorScheme;
         cucumberRoot.toggleColorScheme();
     }
 
     public static void switchColorScheme() {
         if (colorScheme == ColorScheme.DESIGN) {
             if (Player.getStepMode() == Player.STEP_MODE_RUNNING_SINGLESTEP) {
                 setColorScheme(ColorScheme.STEP_MODE);
             } else {
                 setColorScheme(ColorScheme.PLAY);
             }
         } else {
             setColorScheme(ColorScheme.DESIGN);
         }
     }
 
     public static void toggleTerminal() {
         Terminal.toggleTerminal();
     }
 
     public static void filterFeaturesByTags(String tags) {
         ElementHelper.filterFeaturesByTags(tags);
         featuresRoot.scrollToTop();
         tagsFilter = tags;
     }
 
     public static void filterScenariosByTags(String tags) {
         ElementHelper.filterScenariosByTags(tags);
         featuresRoot.scrollToTop();
         tagsFilter = tags;
     }
 
     public static void removeTagsFilter() {
         ElementHelper.removeFilter();
         featuresRoot.scrollToTop();
         tagsFilter = null;
     }
 
     public static void undo() {
         UndoManager.UndoElement undoElement = UndoManager.pop(featuresRoot);
         if (undoElement == null) {
             return;
         }
         synchronized (Engine.DATA_LOCK) {
             DesignerEngine.featuresRoot.children = new LinkedList<Element>();
             for (FeatureElement feature : undoElement.features) {
                 featuresRoot.children.add(feature);
                 feature.groupParent = featuresRoot;
             }
             Engine.resetFps();
         }
     }
 
     public static void initializeStepMode() {
         for (final Device device : Engine.devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.STEP)) {
                 device.initializeStepMode();
             }
         }
     }
 
     public static void addFeature(BaseBarElement element) {
         if (Util.isEmpty(featuresBaseDir)) {
             featuresBaseDir = FileUtil.getPath(element.getFilename());
         }
     }
 }
