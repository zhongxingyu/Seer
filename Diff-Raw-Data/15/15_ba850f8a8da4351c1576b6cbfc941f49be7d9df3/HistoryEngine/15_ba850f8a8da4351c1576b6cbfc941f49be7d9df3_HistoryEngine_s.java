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
 import com.trollsahead.qcumberless.gui.Button;
 import com.trollsahead.qcumberless.gui.elements.Element;
 import com.trollsahead.qcumberless.gui.elements.RootElement;
 import com.trollsahead.qcumberless.plugins.HistoryPlugin;
 import com.trollsahead.qcumberless.util.ElementHelper;
 import com.trollsahead.qcumberless.util.FileUtil;
 import com.trollsahead.qcumberless.util.HistoryHelper;
 import com.trollsahead.qcumberless.util.Util;
 
 import static com.trollsahead.qcumberless.gui.Images.ThumbnailState;
 import static com.trollsahead.qcumberless.engine.Engine.AnimationState;
 import static com.trollsahead.qcumberless.engine.Engine.animationBackground;
 import static com.trollsahead.qcumberless.gui.elements.Element.ColorScheme;
 import static com.trollsahead.qcumberless.gui.elements.Element.ROOT_FEATURE_EDITOR;
 import static com.trollsahead.qcumberless.gui.elements.Element.ROOT_STEP_DEFINITIONS;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 
 public class HistoryEngine implements CucumberlessEngine {
     private static final Color COLOR_HISTORY_DATE_NORMAL = new Color(1.0f, 1.0f, 1.0f, 1.0f);
     private static final Color COLOR_HISTORY_DATE_CURRENT = new Color(1.0f, 1.0f, 0.0f, 1.0f);
     private static final Color COLOR_HISTORY_DATE_HIGHLIGHT = new Color(1.0f, 1.0f, 0.0f, 1.0f);
     private static final Color COLOR_HISTORY_DATE_ERROR = new Color(1.0f, 0.0f, 0.0f, 1.0f);
     private static final Color COLOR_HISTORY_DATE_SUCCESS = new Color(0.0f, 1.0f, 0.0f, 1.0f);
 
     private static final int DATES_PADDING_HORIZONTAL = 40;
     private static final int DATES_PADDING_VERTICAL = 10;
     private static final int BUTTON_PADDING = 40;
     private static final int PLUGIN_BUTTON_PADDING = 16;
 
     private static final String NO_HISTORY = "NO HISTORY";
     private static final String HISTORY_VIEW = "HISTORY VIEW";
 
     private static RootElement originalCucumberRoot = null;
     private static RootElement originalFeaturesRoot = null;
     private static RootElement originalStepsRoot = null;
     private static ColorScheme originalColorScheme;
 
     private static AnimationState animationState;
     private static float animationProgress;
 
     private static List<HistoryEntry> historyEntries;
     private static int currentHistoryEntryIndex;
     private static HistoryEntry highlightedEntry = null;
 
     private static Properties historyProperties;
     private static String currentHistoryDate;
 
     private static Button leftArrowButton;
     private static Button rightArrowButton;
     private static String tags = null;
 
     private static List<Button> buttons;
 
     public static List<HistoryPlugin> plugins = new LinkedList<HistoryPlugin>();
 
     public void initialize() {
         leftArrowButton = new Button(
                 0, 0,
                 Images.getImage(Images.IMAGE_ARROW_LEFT, Images.ThumbnailState.NORMAL.ordinal()),
                 Images.getImage(Images.IMAGE_ARROW_LEFT, Images.ThumbnailState.HIGHLIGHTED.ordinal()),
                 Images.getImage(Images.IMAGE_ARROW_LEFT, Images.ThumbnailState.PRESSED.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.ButtonNotification() {
                     public void onClick() {
                         prevDate();
                     }
                 },
                 null);
         rightArrowButton = new Button(
                 0, 0,
                 Images.getImage(Images.IMAGE_ARROW_RIGHT, Images.ThumbnailState.NORMAL.ordinal()),
                 Images.getImage(Images.IMAGE_ARROW_RIGHT, Images.ThumbnailState.HIGHLIGHTED.ordinal()),
                 Images.getImage(Images.IMAGE_ARROW_RIGHT, Images.ThumbnailState.PRESSED.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.ButtonNotification() {
                     public void onClick() {
                         nextDate();
                     }
                 },
                 null);
     }
 
     public void show() {
         reset();
         backupCucumberRoot();
         List<String> dirs = Util.restrictListSize(HistoryHelper.filterByTags(HistoryHelper.sortDirs(HistoryHelper.findHistoryDirs()), tags), 20);
         historyEntries = new LinkedList<HistoryEntry>();
         for (String dir : dirs) {
             historyEntries.add(new HistoryEntry(dir, getHistoryDate(dir)));
         }
         if (!Util.isEmpty(historyEntries)) {
             showHistory(AnimationState.NONE);
             FlashingMessageManager.addMessage(new FlashingMessage(HISTORY_VIEW));
         } else {
             showNoHistory();
         }
     }
 
     public void hide() {
         restoreCucumberRoot();
         FlashingMessageManager.removeAllMessages();
     }
 
     private void reset() {
         historyEntries = null;
         historyProperties = null;
         currentHistoryEntryIndex = 0;
         buttons = new LinkedList<Button>();
         for (final HistoryPlugin plugin : plugins) {
             Button button = new Button(
                     0,
                     0,
                     plugin.getThumbnail(ThumbnailState.NORMAL),
                     plugin.getThumbnail(ThumbnailState.HIGHLIGHTED),
                     plugin.getThumbnail(ThumbnailState.PRESSED),
                     Button.ALIGN_HORIZONTAL_LEFT | Button.ALIGN_VERTICAL_CENTER,
                     new Button.ButtonNotification() {
                         public void onClick() {
                             HistoryEntry entry = historyEntries.get(currentHistoryEntryIndex);
                             plugin.trigger(HistoryHelper.featuresRootToFeatureList(), entry.date, (String) historyProperties.get("tags"));
                         }
                     }
                     ,null
             );
             button.setHint(plugin.getTooltip());
             buttons.add(button);
         }
     }
 
     private void backupCucumberRoot() {
         originalCucumberRoot = DesignerEngine.cucumberRoot;
         originalFeaturesRoot = DesignerEngine.featuresRoot;
         originalStepsRoot = DesignerEngine.stepsRoot;
         originalColorScheme = DesignerEngine.colorScheme;
     }
 
     private void restoreCucumberRoot() {
         DesignerEngine.cucumberRoot = originalCucumberRoot;
         DesignerEngine.featuresRoot = originalFeaturesRoot;
         DesignerEngine.stepsRoot = originalStepsRoot;
         DesignerEngine.setColorScheme(originalColorScheme);
     }
 
     private void showHistory(AnimationState newAnimationState) {
         synchronized (Engine.DATA_LOCK) {
             animationState = newAnimationState;
             if (animationState != AnimationState.NONE) {
                 renderCurrentRootToBackground();
             }
             createNewRoot();
             loadFeatures(historyEntries.get(currentHistoryEntryIndex));
             historyProperties = HistoryHelper.getRunProperties(historyEntries.get(currentHistoryEntryIndex).directory);
             currentHistoryDate = formatDate(Long.parseLong((String) historyProperties.get("date")));
             DesignerEngine.setColorScheme(ColorScheme.PLAY);
             animationProgress = animationState != AnimationState.NONE ? 0.0f : 1.0f;
         }
     }
 
     private void showNoHistory() {
         animationState = AnimationState.NONE;
         animationProgress = 1.0f;
         createNewRoot();
         FlashingMessageManager.addMessage(new FlashingMessage(NO_HISTORY));
     }
 
     private Date getHistoryDate(String dir) {
         return HistoryHelper.extractDateFromDir(dir);
     }
 
     private String formatDate(long date) {
         return formatDate(new Date(date));
     }
 
     private String formatDate(Date date) {
         return new SimpleDateFormat("MM-dd-yyyy HH:mm").format(date);
     }
 
     private void renderCurrentRootToBackground() {
         render(Engine.animationGraphics, false);
     }
 
     private void loadFeatures(HistoryEntry entry) {
         List<String> features = FileUtil.getFeatureFiles(entry.directory);
         FeatureLoader.parseFeatureFilesAndPushToDesignerRoot(features.toArray(new String[0]), Element.ADD_STATE_RUN_OUTCOME);
         if (entry.hasErrors) {
             ElementHelper.unfoldOnlyErrors();
         } else {
             ElementHelper.unfoldAllScenariosIfNotTooMany();
         }
         if (!Util.isEmpty(tags)) {
             ElementHelper.filterFeaturesAndScenariosByTags(tags);
         }
     }
 
     private void createNewRoot() {
         DesignerEngine.cucumberRoot = new RootElement();
         DesignerEngine.cucumberRoot.setBounds(0, 0, 0, 0);
 
         DesignerEngine.featuresRoot = new RootElement();
         DesignerEngine.featuresRoot.rootType = ROOT_FEATURE_EDITOR;
         DesignerEngine.cucumberRoot.addChild(DesignerEngine.featuresRoot, 0);
 
         DesignerEngine.stepsRoot = new RootElement();
         DesignerEngine.stepsRoot.rootType = ROOT_STEP_DEFINITIONS;
         DesignerEngine.cucumberRoot.addChild(DesignerEngine.stepsRoot, 1);
     }
 
     public void update() {
         if (!Util.isEmpty(historyEntries)) {
             leftArrowButton.update();
             rightArrowButton.update();
             Engine.designerEngine.update();
             for (Button button : buttons) {
                 button.update();
             }
         }
         FlashingMessageManager.update();
         updateAnimation();
     }
 
     private void updateAnimation() {
         if (animationProgress < 1.0f) {
             animationProgress = Math.min(1.0f, animationProgress + GuiUtil.DISAPPEAR_SPEED);
         }
     }
 
     public void render(Graphics2D g) {
         render(g, true);
     }
     
     public void render(Graphics2D g, boolean renderOverlayContent) {
         Engine.drawBackgroundPicture(g);
         Engine.designerEngine.renderOnlyElements(g);
         if (animationState != AnimationState.NONE) {
             GuiUtil.renderAppearAnimation(g, animationBackground, animationState, animationProgress);
         }
         if (renderOverlayContent) {
             renderDates(g);
             renderButtonbar(g);
             renderPlugins(g);
         }
     }
 
     private void renderPlugins(Graphics2D g) {
         int width = 0;
         int maxHeight = 0;
         for (Button button : buttons) {
             width += button.getImageWidth() + PLUGIN_BUTTON_PADDING;
             if (button.getImageHeight() > maxHeight) {
                 maxHeight = button.getImageHeight();
             }
         }
         width -= PLUGIN_BUTTON_PADDING;
 
         int x = (Engine.windowWidth - width) / 2;
         int y = Engine.windowHeight - ButtonBar.BUTTONBAR_HEIGHT - PLUGIN_BUTTON_PADDING - (maxHeight / 2);
         for (Button button : buttons) {
             button.setPosition(x, y);
             button.render(g);
             x += button.getImageWidth() + PLUGIN_BUTTON_PADDING;
         }
     }
 
     private void renderDates(Graphics2D g) {
         if (Util.isEmpty(historyEntries)) {
             return;
         }
         highlightedEntry = null;
         int x = Engine.windowWidth - Engine.fontMetrics.stringWidth(historyEntries.get(0).formattedDate) - DATES_PADDING_HORIZONTAL;
         int y = DATES_PADDING_VERTICAL + Engine.fontMetrics.getHeight() - 3;
         int i = 0;
 
         for (HistoryEntry entry : historyEntries) {
             if (CumberlessMouseListener.mouseX >= x - 10 && CumberlessMouseListener.mouseY >= y - Engine.fontMetrics.getHeight() + 3 && CumberlessMouseListener.mouseY <= y + 3) {
                 highlightedEntry = entry;
             }
 
             g.setColor(entry == highlightedEntry ? COLOR_HISTORY_DATE_HIGHLIGHT : (i != currentHistoryEntryIndex ? COLOR_HISTORY_DATE_NORMAL : COLOR_HISTORY_DATE_CURRENT));
             g.drawString(entry.formattedDate, x, y);
 
             if (entry.hasErrors) {
                 g.setColor(COLOR_HISTORY_DATE_ERROR);
                 g.fillOval(x - 12, y - (Engine.fontMetrics.getHeight() / 2) + 1, 6, 6);
             } else {
                 g.setColor(COLOR_HISTORY_DATE_SUCCESS);
                 g.fillOval(x - 11, y - (Engine.fontMetrics.getHeight() / 2) + 2, 4, 4);
             }
 
             y += Engine.fontMetrics.getHeight() + 5;
             i++;
         }
     }
 
     private void renderButtonbar(Graphics2D g) {
         if (Util.isEmpty(historyEntries)) {
             return;
         }
         g.setColor(ButtonBar.COLOR_BACKGROUND_NORMAL);
         g.fillRect(0, Engine.windowHeight - ButtonBar.BUTTONBAR_HEIGHT, Engine.windowWidth, ButtonBar.BUTTONBAR_HEIGHT);
 
         int dateWidth = Engine.fontMetrics.stringWidth(currentHistoryDate);
         int dateX = (Engine.windowWidth - dateWidth) / 2;
         int textY = Engine.windowHeight - (ButtonBar.BUTTONBAR_HEIGHT + Engine.fontMetrics.getHeight()) / 2 + Engine.fontMetrics.getHeight() - 3;
 
         g.setColor(Color.WHITE);
         g.drawString(currentHistoryDate, dateX, textY);
 
         String runTags = (String) historyProperties.get("tags");
         if (!Util.isEmpty(runTags)) {
             g.drawString(runTags, 5, textY);
         }
 
         int buttonY = Engine.windowHeight - (ButtonBar.BUTTONBAR_HEIGHT / 2);
         leftArrowButton.setPosition(dateX - BUTTON_PADDING, buttonY);
         rightArrowButton.setPosition(dateX + dateWidth + BUTTON_PADDING, buttonY);
 
         leftArrowButton.render(g);
         rightArrowButton.render(g);
     }
 
     public void postRender() {
         Engine.designerEngine.postRender();
     }
 
     public void resize() {
         Engine.designerEngine.resize();
     }
 
     public void mouseMoved() {
         Engine.designerEngine.mouseMoved();
     }
 
     public void mouseWheelMoved(int unitsToScroll) {
         Engine.designerEngine.mouseWheelMoved(unitsToScroll);
     }
 
     public void click(int clickCount) {
         if (leftArrowButton.click()) {
             return;
         }
         if (rightArrowButton.click()) {
             return;
         }
         if (highlightedEntry != null) {
             gotoDate(highlightedEntry);
             return;
         }
         for (Button button : buttons) {
             if (button.click()) {
                 return;
             }
         }
         Engine.designerEngine.click(clickCount);
     }
 
     public void keyPressed(KeyEvent keyEvent) {
         if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT || keyEvent.getKeyCode() == KeyEvent.VK_UP) {
             prevDate();
         }
         if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT || keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
             nextDate();
         }
         if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
             Engine.prevEngine();
         }
     }
 
     private void gotoDate(HistoryEntry entry) {
         if (animationProgress < 1.0f) {
             return;
         }
         if (Util.isEmpty(historyEntries)) {
             return;
         }
         int i = 0;
         for (HistoryEntry historyEntry : historyEntries) {
             if (historyEntry == entry) {
                 currentHistoryEntryIndex = i;
                 showHistory(AnimationState.FORWARD);
                 return;
             }
             i++;
         }
     }
 
     public void nextDate() {
         if (animationProgress < 1.0f) {
             return;
         }
         if (!Util.isEmpty(historyEntries) && currentHistoryEntryIndex < historyEntries.size() - 1) {
             currentHistoryEntryIndex++;
             showHistory(AnimationState.FORWARD);
         }
     }
 
     public void prevDate() {
         if (animationProgress < 1.0f) {
             return;
         }
         if (!Util.isEmpty(historyEntries) && currentHistoryEntryIndex > 0) {
             currentHistoryEntryIndex--;
             showHistory(AnimationState.BACKWARD);
         }
     }
 
     public void startDrag(boolean isControlDown) {
         Engine.designerEngine.startDrag(isControlDown);
     }
 
     public void endDrag() {
         Engine.designerEngine.endDrag();
     }
 
     public void updateDrag() {
         Engine.designerEngine.updateDrag();
     }
 
     public void updateDevices(Set<Device> devices) {
         Engine.designerEngine.updateDevices(devices);
     }
 
     public static void removeTagFilter() {
         tags = null;
     }
 
     public static void setTagFilter(String tags) {
         HistoryEngine.tags = tags;
     }
 
     public static void initializePlugins() {
         for (HistoryPlugin plugin : plugins) {
             plugin.initialize();
         }
     }
 
     private class HistoryEntry {
         public String directory;
         public Date date;
         public String formattedDate;
         public boolean hasErrors;
 
         public HistoryEntry(String directory, Date date) {
             this.directory = directory;
             this.date = date;
             this.formattedDate = formatDate(date);
             updateErrorState();
         }
 
         private void updateErrorState() {
             hasErrors = false;
             List<String> features = FileUtil.getFeatureFiles(directory);
             for (String feature : features) {
                 if (HistoryHelper.hasErrorInFeatureFile(feature)) {
                     hasErrors = true;
                     break;
                 }
             }
         }
     }
 }
