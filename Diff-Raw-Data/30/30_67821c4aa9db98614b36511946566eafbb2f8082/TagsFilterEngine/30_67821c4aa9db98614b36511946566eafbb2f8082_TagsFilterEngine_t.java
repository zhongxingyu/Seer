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
 import com.trollsahead.qcumberless.gui.CumberlessMouseListener;
 import com.trollsahead.qcumberless.gui.TagFilterButton;
 import com.trollsahead.qcumberless.gui.elements.BaseBarElement;
 import com.trollsahead.qcumberless.gui.Button;
 import com.trollsahead.qcumberless.gui.Scrollbar;
 import com.trollsahead.qcumberless.util.Util;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 public class TagsFilterEngine implements CucumberlessEngine {
     private static final Color TEXT_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
     private static final Color TITLE_UNDERLINE_COLOR = new Color(0.3f, 0.3f, 0.3f, 1.0f);
 
     private static final String NO_FEATURE_TAGS = "NO FEATURE TAGS DEFINED";
     private static final String NO_SCENARIO_TAGS = "NO SCENARIO TAGS DEFINED";
 
     private static final String FEATURE_TAGS_TITLE = "FEATURE TAGS";
     private static final String SCENARIO_TAGS_TITLE = "SCENARIO TAGS";
 
     public static final int TAG_WIDTH = 120;
     public static final int TAG_HEIGHT = 50;
 
     public static final int TAG_PADDING_HORIZONTAL = 50;
     public static final int TAG_PADDING_VERTICAL = 50;
     public static final int TAGS_TITLE_PADDING = 10;
 
     private static final int TAG_BUTTON_OFFSET_HORIZONTAL = 5;
     private static final int TAG_BUTTON_OFFSET_VERTICAL = 5;
 
     private static final int SCROLLBAR_PADDING_HORIZONTAL = 4;
     private static final int SCROLLBAR_PADDING_VERTICAL = 4;
 
     private static final Color BACKGROUND_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.5f);
 
     private List<String> featureTags;
     private List<String> scenarioTags;
 
     private Scrollbar featureScrollbar;
     private Scrollbar scenarioScrollbar;
 
     private int featureTagsX;
     private int featureTagsY;
     private int featureTagsWidth;
     private int featureTagsHeight;
     private int featureTagsAreaHeight;
     private int featureTagsOffset;
 
     private int scenarioTagsX;
     private int scenarioTagsY;
     private int scenarioTagsWidth;
     private int scenarioTagsHeight;
     private int scenarioTagsAreaHeight;
     private int scenarioTagsOffset;
 
     private List<TagFilterButton> featureTagsButtons;
     private List<TagFilterButton> scenarioTagsButtons;
 
     public void initialize() {
         featureScrollbar = new Scrollbar();
         scenarioScrollbar = new Scrollbar();
     }
 
     public void show() {
         featureTags = DesignerEngine.getDefinedTags(BaseBarElement.TYPE_FEATURE);
         scenarioTags = DesignerEngine.getDefinedTags(BaseBarElement.TYPE_SCENARIO, BaseBarElement.TYPE_SCENARIO_OUTLINE);
         featureTagsOffset = 0;
         scenarioTagsOffset = 0;
         setupTagsButtons();
         resize();
     }
 
     public void hide() {
     }
 
     public void update() {
         for (TagFilterButton button : featureTagsButtons) {
             button.update();
         }
         for (TagFilterButton button : scenarioTagsButtons) {
             button.update();
         }
     }
 
     private void setupTagsButtons() {
         featureTagsButtons = new LinkedList<TagFilterButton>();
         for (final String tag : featureTags) {
             featureTagsButtons.add(
                     new TagFilterButton(
                             tag,
                             0,
                             0,
                             TAG_WIDTH,
                             TAG_HEIGHT,
                             new Button.ButtonNotification() {
                                 public void onClick() {
                                     DesignerEngine.runTests(Util.stringToTagSet("@" + tag));
                                     Engine.showEngine(Engine.designerEngine);
                                 }
                             },
                             new Button.ButtonNotification() {
                                 public void onClick() {
                                     DesignerEngine.filterFeaturesByTags("@" + tag);
                                     Engine.showEngine(Engine.designerEngine);
                                 }
                             },
                             new Button.ButtonNotification() {
                                 public void onClick() {
                                     HistoryEngine.setTagFilter("@" + tag);
                                     Engine.showEngine(Engine.historyEngine);
                                 }
                             }));
         }
         scenarioTagsButtons = new LinkedList<TagFilterButton>();
         for (final String tag : scenarioTags) {
             scenarioTagsButtons.add(
                     new TagFilterButton(
                             tag,
                             0,
                             0,
                             TAG_WIDTH,
                             TAG_HEIGHT,
                             new Button.ButtonNotification() {
                                 public void onClick() {
                                     DesignerEngine.runTests(Util.stringToTagSet("@" + tag));
                                     Engine.showEngine(Engine.designerEngine);
                                 }
                             },
                             new Button.ButtonNotification() {
                                 public void onClick() {
                                     DesignerEngine.filterScenariosByTags("@" + tag);
                                     Engine.showEngine(Engine.designerEngine);
                                 }
                             },
                             new Button.ButtonNotification() {
                                 public void onClick() {
                                     HistoryEngine.setTagFilter("@" + tag);
                                     Engine.showEngine(Engine.historyEngine);
                                 }
                             }));
         }
     }
 
     public void render(Graphics2D g) {
         calculatePosition();
         renderBackground(g);
         renderFeatureTags(g);
         renderTags(g);
     }
 
     private void renderBackground(Graphics2D g) {
         Engine.drawBackgroundPicture(g);
 
         g.setColor(BACKGROUND_COLOR);
         g.fillRect(featureTagsX, featureTagsY, featureTagsWidth, featureTagsHeight);
         g.fillRect(scenarioTagsX, scenarioTagsY, scenarioTagsWidth, scenarioTagsHeight);
     }
 
     private void calculatePosition() {
         featureTagsWidth = (int) (Engine.windowWidth * 0.8f);
         featureTagsHeight = (int) (Engine.windowHeight * 0.45f);
         featureTagsX = (Engine.windowWidth - featureTagsWidth) / 2;
         featureTagsY = ((Engine.windowHeight / 2) - featureTagsHeight) / 2;
 
         scenarioTagsWidth = (int) (Engine.windowWidth * 0.8f);
         scenarioTagsHeight = (int) (Engine.windowHeight * 0.45f);
         scenarioTagsX = (Engine.windowWidth - scenarioTagsWidth) / 2;
         scenarioTagsY = (((Engine.windowHeight / 2) - scenarioTagsHeight) / 2) + (Engine.windowHeight / 2);
 
         updateButtonPositions();
         updateScrollbarBounds();
     }
 
     private void updateButtonPositions() {
         featureTagsOffset = featureScrollbar.getScroll();
         int i = 0;
         for (TagFilterButton button : featureTagsButtons) {
             setButtonPosition(button, i, featureTagsX + TAG_BUTTON_OFFSET_HORIZONTAL, featureTagsY + TAG_BUTTON_OFFSET_VERTICAL - featureTagsOffset, featureTagsWidth, featureTagsHeight);
             button.setClipRect(featureTagsX, featureTagsY + TAG_PADDING_VERTICAL - TAG_BUTTON_OFFSET_VERTICAL, featureTagsWidth, featureTagsHeight - TAG_PADDING_VERTICAL + TAG_BUTTON_OFFSET_VERTICAL);
             featureTagsAreaHeight = button.topY + button.height - featureTagsY - TAG_PADDING_VERTICAL + TAG_BUTTON_OFFSET_VERTICAL;
             i++;
         }
         scenarioTagsOffset = scenarioScrollbar.getScroll();
         i = 0;
         for (TagFilterButton button : scenarioTagsButtons) {
             setButtonPosition(button, i, scenarioTagsX + TAG_BUTTON_OFFSET_HORIZONTAL, scenarioTagsY + TAG_BUTTON_OFFSET_VERTICAL - scenarioTagsOffset, scenarioTagsWidth, scenarioTagsHeight);
             button.setClipRect(scenarioTagsX, scenarioTagsY + TAG_PADDING_VERTICAL - TAG_BUTTON_OFFSET_VERTICAL, scenarioTagsWidth, scenarioTagsHeight - TAG_PADDING_VERTICAL + TAG_BUTTON_OFFSET_VERTICAL);
             scenarioTagsAreaHeight = button.topY + button.height - scenarioTagsY - TAG_PADDING_VERTICAL + TAG_BUTTON_OFFSET_VERTICAL;
             i++;
         }
     }
 
     private void setButtonPosition(TagFilterButton button, int index, int x, int y, int width, int height) {
         int tagWidthPlusPadding = TAG_WIDTH + TAG_PADDING_HORIZONTAL;
         int tagHeightPlusPadding = TAG_HEIGHT + TAG_PADDING_VERTICAL;
         int countX = width / tagWidthPlusPadding;
         int countY = height / tagHeightPlusPadding;
         int offsetX = x + (width - ((countX * tagWidthPlusPadding) - TAG_PADDING_HORIZONTAL)) / 2;
         int offsetY = y + (height - ((countY * tagHeightPlusPadding) - TAG_PADDING_VERTICAL)) / 2;
         button.setCenterPosition(offsetX + (index % countX) * tagWidthPlusPadding, offsetY + (index / countX) * tagHeightPlusPadding);
     }
 
     private void renderFeatureTags(Graphics2D g) {
         if (Util.isEmpty(featureTags)) {
             g.setColor(TEXT_COLOR);
             g.drawString(NO_FEATURE_TAGS, (Engine.windowWidth - Engine.fontMetrics.stringWidth(NO_FEATURE_TAGS)) / 2, featureTagsY + ((featureTagsHeight - Engine.fontMetrics.getHeight()) / 2));
             return;
         }
         drawTitle(g, FEATURE_TAGS_TITLE, featureTagsY);
         for (TagFilterButton button : featureTagsButtons) {
            button.render(g);
         }
         featureScrollbar.render(g);
     }
 
     private void renderTags(Graphics2D g) {
         if (Util.isEmpty(scenarioTags)) {
             g.setColor(TEXT_COLOR);
             g.drawString(NO_SCENARIO_TAGS, (Engine.windowWidth - Engine.fontMetrics.stringWidth(NO_SCENARIO_TAGS)) / 2, scenarioTagsY + ((scenarioTagsHeight - Engine.fontMetrics.getHeight()) / 2));
             return;
         }
         drawTitle(g, SCENARIO_TAGS_TITLE, scenarioTagsY);
         for (TagFilterButton button : scenarioTagsButtons) {
            button.render(g);
         }
         scenarioScrollbar.render(g);
     }
     
     private void drawTitle(Graphics g, String title, int y) {
         int titleWidth = Engine.fontMetrics.stringWidth(title);
         int titleX = (Engine.windowWidth - titleWidth) / 2;
         int titleY = y + Engine.fontMetrics.getHeight() + TAGS_TITLE_PADDING;
         g.setColor(Color.BLACK);
         g.drawString(title, titleX + 1, titleY + 1);
         g.setColor(TEXT_COLOR);
         g.drawString(title, titleX, titleY);
         g.setColor(TITLE_UNDERLINE_COLOR);
         g.drawLine(titleX - 25, titleY + 5, titleX + titleWidth + 25, titleY + 5);
     }
 
     public void postRender() {
     }
 
     public void resize() {
         updateScrollbarBounds();
     }
 
     public void mouseMoved() {
     }
 
     public void mouseWheelMoved(int unitsToScroll) {
         if (CumberlessMouseListener.mouseX >= featureTagsX && CumberlessMouseListener.mouseY >= featureTagsY &&
             CumberlessMouseListener.mouseX <= featureTagsX + featureTagsWidth && CumberlessMouseListener.mouseY <= featureTagsY + featureTagsHeight) {
             featureScrollbar.scroll(unitsToScroll);
         }
         if (CumberlessMouseListener.mouseX >= scenarioTagsX && CumberlessMouseListener.mouseY >= scenarioTagsY &&
             CumberlessMouseListener.mouseX <= scenarioTagsX + scenarioTagsWidth && CumberlessMouseListener.mouseY <= scenarioTagsY + scenarioTagsHeight) {
             scenarioScrollbar.scroll(unitsToScroll);
         }
     }
 
     public void click(int clickCount) {
         if (featureScrollbar.isScrollbarTouched() || scenarioScrollbar.isScrollbarTouched()) {
             return;
         }
         for (TagFilterButton button : featureTagsButtons) {
             if (button.click()) {
                 return;
             }
         }
         for (TagFilterButton button : scenarioTagsButtons) {
             if (button.click()) {
                 return;
             }
         }
     }
 
     public void keyPressed(KeyEvent keyEvent) {
         if (keyEvent.getKeyChar() == KeyEvent.VK_ESCAPE) {
             DesignerEngine.removeTagsFilter();
             Engine.prevEngine();
         }
     }
 
     public void startDrag(boolean isControlDown) {
         featureScrollbar.startDrag();
         scenarioScrollbar.startDrag();
     }
 
     public void endDrag() {
         featureScrollbar.endDrag();
         scenarioScrollbar.endDrag();
     }
 
     public void updateDrag() {
         featureScrollbar.updateDrag();
         scenarioScrollbar.updateDrag();
     }
 
     public void updateDevices(Set<Device> devices) {
     }
 
     private void updateScrollbarBounds() {
         if (!Util.isEmpty(featureTagsButtons)) {
             featureScrollbar.setBounds(featureTagsX + SCROLLBAR_PADDING_HORIZONTAL, featureTagsY + TAG_PADDING_VERTICAL, featureTagsHeight - TAG_PADDING_VERTICAL - SCROLLBAR_PADDING_VERTICAL, featureTagsAreaHeight);
         }
         if (!Util.isEmpty(scenarioTagsButtons)) {
             scenarioScrollbar.setBounds(scenarioTagsX + SCROLLBAR_PADDING_HORIZONTAL, scenarioTagsY + TAG_PADDING_VERTICAL, scenarioTagsHeight - TAG_PADDING_VERTICAL - SCROLLBAR_PADDING_VERTICAL, scenarioTagsAreaHeight);
         }
     }
 }
