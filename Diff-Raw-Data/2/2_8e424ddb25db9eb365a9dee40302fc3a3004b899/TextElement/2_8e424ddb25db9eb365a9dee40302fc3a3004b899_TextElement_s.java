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
 
 package com.trollsahead.qcumberless.gui;
 
 import com.trollsahead.qcumberless.engine.Engine;
 import com.trollsahead.qcumberless.engine.FeatureLoader;
 import com.trollsahead.qcumberless.engine.Player;
 import com.trollsahead.qcumberless.model.Locale;
 import com.trollsahead.qcumberless.model.Step;
 import com.trollsahead.qcumberless.model.Tag;
 import com.trollsahead.qcumberless.util.ElementHelper;
 import com.trollsahead.qcumberless.util.Util;
 
 import static com.trollsahead.qcumberless.gui.ExtendedButtons.*;
 import static com.trollsahead.qcumberless.gui.Images.ThumbnailState;
 import static com.trollsahead.qcumberless.gui.RenderOptimizer.ImageTemplate;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 
 import static com.trollsahead.qcumberless.model.Step.CucumberStepPart;
 
 public class TextElement extends Element {
     public static final int TYPE_FEATURE         = 0;
     public static final int TYPE_BACKGROUND      = 1;
     public static final int TYPE_SCENARIO        = 2;
     public static final int TYPE_STEP            = 3;
     public static final int TYPE_COMMENT         = 4;
 
     public static final int[][] TYPE_ATTACHABLE_TO = {
             {RootElement.TYPE_ROOT},
             {TYPE_FEATURE},
             {TYPE_FEATURE},
             {TYPE_SCENARIO, TYPE_BACKGROUND},
             {TYPE_SCENARIO, TYPE_BACKGROUND}
     };
 
     public static final Color COLOR_TEXT_NORMAL        = new Color(0x000000);
     public static final Color COLOR_TEXT_PARAMETER     = new Color(0xDDDD88);
     public static final Color COLOR_TEXT_ERROR_MESSAGE = new Color(0x000000);
     public static final Color COLOR_TEXT_TAGS          = new Color(0x000000);
 
     public static final Color COLOR_BG_FAILED          = new Color(0xFF0000);
     public static final Color COLOR_BG_ERROR_MESSAGE   = new Color(0.8f, 0.8f, 0.4f, 0.8f);
     public static final Color COLOR_BG_TAGS            = new Color(0.0f, 0.0f, 0.0f, 0.05f);
     public static final Color COLOR_BG_CLEAR           = new Color(1.0f, 1.0f, 1.0f, 0.0f);
     public static final Color COLOR_BG_HINT            = new Color(0.0f, 0.0f, 0.0f, 0.8f);
 
     public static final Color[] COLOR_BG_UNRECOGNIZED_STEP = {new Color(0xFF66FF), new Color(0xDD99DD)};
 
     public static final Color[][] COLOR_BG_FILL = new Color[][] {
             {new Color(0xAAAAFF), new Color(0xBBBBEE)},
             {new Color(0x888888), new Color(0xAAAAAA)},
             {new Color(0x88FF88), new Color(0xAADDAA)},
             {new Color(0xFF6666), new Color(0xDD9999)},
             {new Color(0xAAAAAA), new Color(0xBBBBBB)},
     };
 
     private static final Color COLOR_BORDER_SHADOW = new Color(0.0f, 0.0f, 0.0f, 0.8f);
     private static final Color COLOR_BORDER_PLAYING = new Color(0.0f, 0.0f, 0.0f, 0.4f);
     private static final Color COLOR_BORDER_FAILURE = new Color(1.0f, 0.0f, 0.0f, 0.8f);
     private static final Color COLOR_BORDER_EDITING = new Color(1.0f, 1.0f, 0.5f, 0.8f);
 
     public static final int RENDER_WIDTH_MAX_FEATURE_EDITOR = 600;
     public static final int RENDER_WIDTH_MAX_STEP_DEFINITIONS = 400;
     public static final int RENDER_HEIGHT_MINIMUM = 20;
     public static final int SHADOW_SIZE = 5;
 
     public static final int[] PADDING_HORIZONTAL = new int[] {50, 50, 50, 50, 50, 50};
     public static final int[] PADDING_VERTICAL   = new int[] {10, 10, 10, 5, 5, 10};
 
     public static final int BAR_ROUNDING = 20;
     private static final int HINT_ROUNDING = 12;
 
     public static final int TEXT_PADDING_HORIZONTAL = 15;
     public static final int TEXT_PADDING_VERTICAL   = 5;
 
     private static final int HINT_PADDING_HORIZONTAL = 7;
     private static final int HINT_PADDING_VERTICAL = 2;
 
     private static final int TAGS_PADDING_VERTICAL = 4;
 
     public static final int BUTTON_PADDING_HORIZONTAL = 15;
     public static final int BUTTON_SPACE_HORIZONTAL = 6;
     public static final int BUTTON_SPACE_VERTICAL = 4;
 
     public static final int BUTTON_WIDTH  = 15;
     public static final int BUTTON_HEIGHT = 15;
 
     private static final int BUTTON_GROUP_HEIGHT = BUTTON_HEIGHT + BUTTON_SPACE_VERTICAL;
 
     public static final float PLAY_ANIMATION_SPEED = 30.0f;
     public static final float PLAY_ANIMATION_DASH_LENGTH = 10.0f;
     public static final float PLAY_ANIMATION_DASH_WIDTH = 1.5f;
 
     private int tagsWidth = 0;
     private int tagsHeight = 0;
 
     public Step step;
 
     public String title;
     private String filename = null;
     private String comment = null;
     public Tag tags;
 
     private static final String EXPORT_INDENT = "    ";
 
     private List<Button> buttons;
     private List<ElementPluginButton> pluginButtons;
     private Button expandButton;
     private Button trashcanButton;
     private Button playButton;
     private Button editButton;
     private Button tagsAddButton;
     private Button tagsNewButton;
     private Button tagsRemoveButton;
 
     private static final int DRAG_HISTORY_LENGTH = 5;
     private static final long DRAG_HISTORY_UPDATE_INTERVAL = 5;
     private static final float DRAG_HISTORY_THROW_LIMIT = 10.0f;
 
     private int[] dragHistoryX = new int[DRAG_HISTORY_LENGTH];
     private int[] dragHistoryY = new int[DRAG_HISTORY_LENGTH];
     private long[] dragHistoryTime = new long[DRAG_HISTORY_LENGTH];
     private int dragHistoryIndex = 0;
 
     private int buttonGroupHeight = 0;
     private boolean buttonGroupVisible = false;
     private boolean buttonGroupVisibleOld = false;
     private int buttonGroupWidth = 0;
     private boolean buttonGroupHasButtons = false;
 
     private Element lastBubbledElement = null;
 
     public TextElement(int type, int rootType) {
         this(type, rootType, "Untitled");
     }
 
     public TextElement(int type, int rootType, String title) {
         this(type, rootType, calculateRenderWidthFromRoot(rootType), title, new Step(title));
     }
 
     public TextElement(int type, int rootType, String title, Step step) {
         this(type, rootType, calculateRenderWidthFromRoot(rootType), title, step, "");
     }
 
     public TextElement(int type, int rootType, int width, String title, Step step) {
         this(type, rootType, width, title, step, "");
     }
     
     public TextElement(int type, int rootType, int width, String title, Step step, String tags) {
         super();
         this.type = type;
         this.rootType = rootType;
         this.renderWidth = width;
         this.title = title;
         this.step = step != null ? step : new Step(title);
         this.tags = new Tag(tags);
         folded = type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_BACKGROUND;
         if (type == TYPE_FEATURE) {
             animation.colorAnimation.setAlpha(Animation.FADE_ALPHA_DEFAULT, Animation.FADE_SPEED_ENTRANCE);
         }
         animation.sizeAnimation.currentWidth = this.renderWidth;
         animation.sizeAnimation.currentHeight = this.renderHeight;
         addButtons();
     }
 
     private void addButtons() {
         buttons = new ArrayList<Button>();
         expandButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_EXPAND, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_EXPAND, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_EXPAND, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER_OF_PARENT,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                     }
                 },
                 this);
         buttons.add(expandButton);
         trashcanButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_TRASHCAN, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_TRASHCAN, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_TRASHCAN, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_RIGHT_OF_PARENT | Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER_OF_PARENT,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         trashElement();
                     }
                 },
                 this);
         buttons.add(trashcanButton);
         playButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_PLAY, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_PLAY, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_PLAY, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         play();
                     }
                 },
                 this);
         buttons.add(playButton);
         editButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_EDIT, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_EDIT, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_EDIT, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         EditBox.showEditElement(TextElement.this);
                     }
                 },
                 this);
         buttons.add(editButton);
         tagsAddButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_ADD, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_ADD, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_ADD, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_LEFT | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         addTags(false);
                     }
                 },
                 this);
         buttons.add(tagsAddButton);
         tagsNewButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_AT, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_AT, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_AT, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         addTags(true);
                     }
                 },
                 this);
         buttons.add(tagsNewButton);
         tagsRemoveButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_MINUS, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_MINUS, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_MINUS, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_LEFT | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         removeTags();
                     }
                 },
                 this);
         buttons.add(tagsRemoveButton);
         pluginButtons = GuiUtil.getPluginButtonsForElement(this);
         updateButtonPositions();
         for (Button button : buttons) {
             button.setVisible(false);
         }
         for (ElementPluginButton button : pluginButtons) {
             button.setVisible(false);
         }
     }
 
     private void updateButtonPositions() {
         buttonGroupHasButtons = false;
         trashcanButton.setPosition(BUTTON_PADDING_HORIZONTAL, buttonGroupHeight / 2);
         expandButton.setPosition(BUTTON_PADDING_HORIZONTAL, buttonGroupHeight / 2);
         if (!isHighlighted()) {
             return;
         }
         buttonGroupWidth = BUTTON_PADDING_HORIZONTAL;
         if (hasPlayButton()) {
             buttonGroupWidth = addGroupButton(playButton, buttonGroupWidth);
         }
         if (hasEditButton()) {
             buttonGroupWidth = addGroupButton(editButton, buttonGroupWidth);
         }
         if (hasTagsAddButton()) {
             if (tags.hasTags()) {
                 tagsAddButton.setPosition((renderWidth + tagsWidth) / 2 + BUTTON_SPACE_HORIZONTAL, TAGS_PADDING_VERTICAL + (tagsHeight / 2) + buttonGroupHeight);
                 tagsRemoveButton.setPosition((renderWidth + tagsWidth) / 2 + (BUTTON_SPACE_HORIZONTAL * 2) + BUTTON_WIDTH, TAGS_PADDING_VERTICAL + (tagsHeight / 2) + buttonGroupHeight);
             } else {
                 buttonGroupWidth = addGroupButton(tagsNewButton, buttonGroupWidth);
             }
         }
         for (ElementPluginButton button : pluginButtons) {
             if (!button.getCallback().isVisibleForElement(this)) {
                 continue;
             }
             buttonGroupWidth = addGroupButton(button, buttonGroupWidth);
         }
         buttonGroupWidth -= BUTTON_SPACE_HORIZONTAL;
     }
     
     private int addGroupButton(Button button, int x) {
         button.setPosition(x, BUTTON_SPACE_VERTICAL + (BUTTON_HEIGHT / 2));
         buttonGroupHasButtons = true;
         return x + BUTTON_WIDTH + BUTTON_SPACE_HORIZONTAL;
     }
 
     public void setTitle(String title) {
         this.title = title;
         step = FeatureLoader.findMatchingStep(title);
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public void setTags(String tags) {
         this.tags = new Tag(tags);
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void updateSelf(long time) {
         updateButtons();
     }
 
     public String getTitle() {
         return step.toString();
     }
 
     private void updateButtons() {
         if (groupParent == Engine.stepsRoot) {
             return;
         }
         for (Button button : buttons) {
             button.setVisible(false);
         }
         for (ElementPluginButton button : pluginButtons) {
             button.setVisible(false);
         }
         if (!isHighlighted()) {
             toggleButtonGroup(false);
             return;
         }
         expandButton.setVisible(buttonGroupHasButtons);
         updateButtonGroupState();
         trashcanButton.setVisible(true);
         tagsRemoveButton.setVisible(hasTagsAddButton() && tags.hasTags());
         tagsAddButton.setVisible(hasTagsAddButton() && tags.hasTags());
         tagsNewButton.setVisible(hasTagsAddButton() && buttonGroupVisible && !tags.hasTags());
         playButton.setVisible(hasPlayButton() && buttonGroupVisible);
         editButton.setVisible(hasEditButton() && buttonGroupVisible);
         for (Button button : buttons) {
             button.update();
         }
         for (ElementPluginButton button : pluginButtons) {
             button.setVisible(button.getCallback().isVisibleForElement(this) && buttonGroupVisible);
             button.update();
         }
     }
 
     private void updateButtonGroupState() {
         if (expandButton.isTouched()) {
             toggleButtonGroup(true);
         } else if (!buttonGroupHasButtons || !expandAreaIsTouched()) {
             toggleButtonGroup(false);
         }
     }
 
     private boolean expandAreaIsTouched() {
         return CumberlessMouseListener.mouseX < animation.moveAnimation.renderX + buttonGroupWidth;
     }
 
     private void toggleButtonGroup(boolean visible) {
         buttonGroupVisibleOld = buttonGroupVisible;
         buttonGroupVisible = visible;
     }
 
     protected void calculateRenderPosition(Graphics2D g) {
         if (groupParent != null) {
             animation.moveAnimation.setRealPosition(groupParent.animation.moveAnimation, !shouldStickToParentRenderPosition);
             animation.moveAnimation.realY += groupParent.groupHeight;
             if (groupParent instanceof TextElement) {
                 animation.moveAnimation.realX += PADDING_HORIZONTAL[((TextElement) groupParent).type];
             }
         }
         if (shouldStickToParentRenderPosition || animation.colorAnimation.justBecameVisible) {
             animation.moveAnimation.setRenderPosition(animation.moveAnimation, false);
         }
         calculateButtonGroupHeight();
         calculateTagsPosition();
         int elementHeight = calculatePartPositions();
         renderWidth = calculateRenderWidth();
         renderHeight = Math.max(RENDER_HEIGHT_MINIMUM, elementHeight + (TEXT_PADDING_VERTICAL * 2));
         paddingHeight = PADDING_VERTICAL[type];
         groupHeight = renderHeight + paddingHeight;
         updateButtonPositions();
     }
 
     private void calculateButtonGroupHeight() {
         buttonGroupHeight = buttonGroupVisible ? BUTTON_GROUP_HEIGHT : 0;
         animation.moveAnimation.realY -= buttonGroupHeight;
         if (buttonGroupVisible != buttonGroupVisibleOld) {
             animation.moveAnimation.renderY = animation.moveAnimation.realY;
             if (buttonGroupVisible && animation.moveAnimation.realY < 0) {
                 Engine.featuresRoot.scroll(-animation.moveAnimation.realY);
             }
         }
     }
 
     private void calculateTagsPosition() {
         if (!shouldRenderTags()) {
             tagsHeight = 0;
             return;
         }
         tagsWidth = Engine.fontMetrics.stringWidth(getTagsStringOrAtIfEmpty()) + (HINT_PADDING_HORIZONTAL * 2);
         tagsHeight = Engine.fontMetrics.getHeight() + (HINT_PADDING_VERTICAL * 2);
     }
 
     public static int calculateRenderWidthFromRoot(int rootType) {
         int maxPadding = PADDING_HORIZONTAL[TYPE_FEATURE] + PADDING_HORIZONTAL[TYPE_SCENARIO] + PADDING_HORIZONTAL[TYPE_STEP];
         int wantedWidth = RENDER_WIDTH_MAX_FEATURE_EDITOR;
         int maxWidth = Engine.canvasWidth;
         if (rootType == ROOT_FEATURE_EDITOR) {
             wantedWidth = RENDER_WIDTH_MAX_FEATURE_EDITOR;
             maxWidth = Engine.featuresRoot.renderWidth - (maxPadding - (int) Engine.featuresRoot.animation.moveAnimation.renderX);
         } else if (rootType == ROOT_STEP_DEFINITIONS) {
             wantedWidth = RENDER_WIDTH_MAX_STEP_DEFINITIONS;
             maxWidth = Engine.stepsRoot.renderWidth - (RootElement.PADDING_HORIZONTAL * 2);
         }
         return Math.min(maxWidth, wantedWidth);
     }
 
     private int calculateRenderWidth() {
         if (animation.sizeAnimation.isResizing) {
             return (int) animation.sizeAnimation.currentWidth;
         }
         if (rootType != ROOT_NONE) {
             return calculateRenderWidthFromRoot(rootType);
         }
         return renderWidth;
     }
 
     private int calculatePartPositions() {
         int x = 0;
         int y = tagsHeight + buttonGroupHeight;
         for (CucumberStepPart part : step.getParts()) {
             part.wrapText(x, y, renderWidth - (getTextPaddingLeft() + getTextPaddingRight()));
             x = part.endX;
             y = part.endY;
         }
         return y + Engine.fontMetrics.getHeight();
     }
 
     public void click(int clickCount) {
         if (clickCount > 1) {
             doubleClick();
             return;
         }
         for (Button button : buttons) {
             if (button.click()) {
                 return;
             }
         }
         for (ElementPluginButton button : pluginButtons) {
             if (button.click()) {
                 return;
             }
         }
         final CucumberStepPart part = getTouchedPart();
         if (part != null && part.type == CucumberStepPart.PartType.ARGUMENT) {
             if (part.validParameters.length == 1 && isEditableParameter(part.validParameters[0])) {
                 EditBox.showEditPart(part);
             } else {
                 DropDown.show(
                         (int) animation.moveAnimation.renderX + part.startX + TEXT_PADDING_HORIZONTAL,
                         (int) animation.moveAnimation.renderY + part.startY + TEXT_PADDING_VERTICAL,
                         new DropDown.DropDownCallback() {
                             public void chooseItem(String item) {
                                 part.setText(item);
                                 if (isEditableParameter(part.getText())) {
                                     EditBox.showEditPart(part);
                                 }
                             }
                         },
                         Arrays.asList(part.validParameters));
             }
         } else {
             foldToggle();
         }
     }
 
     private void doubleClick() {
         if (groupParent != Engine.stepsRoot) {
             return;
         }
         new Thread(new Runnable() {
             public void run() {
                 if (type == TYPE_FEATURE) {
                     filename = CucumberlessDialog.instance.askFeatureFilename();
                     if (filename == null) {
                         return;
                     }
                 }
                 synchronized (Engine.LOCK) {
                     throwElementToFeaturesGroup();
                 }
             }
         }).start();
     }
 
     private void play() {
         Engine.runTests(this);
     }
 
     public void trashElement() {
         synchronized (Engine.LOCK) {
             if (groupParent != null) {
                 groupParent.updateElementIndex(this, -1);
                 Engine.cucumberRoot.removeChild(this);
             }
         }
     }
 
     private CucumberStepPart getTouchedPart() {
         for (CucumberStepPart part : step.getParts()) {
             if (part.isTouched) {
                 return part;
             }
         }
         return null;
     }
 
     private void foldToggle() {
         if (children.size() == 0 || isParentFolded()) {
             return;
         }
         if (type != TYPE_FEATURE && type != TYPE_SCENARIO && type != TYPE_BACKGROUND) {
             return;
         }
         folded = !folded;
         foldFadeAnimation(folded ? 0.0f : Animation.FADE_ALPHA_DEFAULT);
     }
 
     public void unfold() {
         folded = false;
         foldFadeAnimation(Animation.FADE_ALPHA_DEFAULT);
     }
 
     protected void foldFadeAnimation(float alpha) {
         for (Element child : children) {
             child.animation.colorAnimation.setAlpha(alpha, Animation.FADE_SPEED_FOLD);
             if (!child.folded) {
                 child.foldFadeAnimation(alpha);
             }
         }
     }
 
     private void dragFadeAnimation() {
         highlight(false);
         float alpha = isDragged ? Animation.FADE_ALPHA_DRAG : Animation.FADE_ALPHA_DEFAULT;
         animation.colorAnimation.setAlpha(alpha, Animation.FADE_SPEED_DRAG);
         if (!folded) {
             for (Element child : children) {
                 child.animation.colorAnimation.setAlpha(alpha, Animation.FADE_SPEED_FOLD);
             }
         }
     }
 
     public void startDrag() {
         if (isButtonTouched()) {
             return;
         }
         super.startDrag();
         dragFadeAnimation();
         resetDragPositionHistory();
         lastBubbledElement = null;
     }
 
     public void endDrag() {
         if (isButtonTouched()) {
             return;
         }
         super.endDrag();
         dragFadeAnimation();
         if (isThrowingElementToFeaturesGroup()) {
             throwElementToFeaturesGroup();
         }
         Engine.updateLastAddedElement(this);
     }
 
     public boolean isDragable() {
         return type != TYPE_FEATURE && !animation.moveAnimation.isMoving() && !isParentFolded() && visible;
     }
 
     protected void applyDrag() {
         updateDragPositionHistory();
         Element touchedGroup = Engine.cucumberRoot.findGroup(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY, type);
         if (type == TYPE_BACKGROUND && touchedGroup != null && touchedGroup.type == TYPE_FEATURE) {
             TextElement backgroundElement = Engine.findBackgroundElement(touchedGroup);
             if (backgroundElement != null && backgroundElement != this) {
                 touchedGroup = null;
             }
         }
         if (touchedGroup == null && type == TYPE_FEATURE) {
             touchedGroup = Engine.featuresRoot;
         }
         if (touchedGroup == null || touchedGroup instanceof RootElement) {
             lastBubbledElement = null;
             return;
         }
         Element touchedElement = Engine.cucumberRoot.findElementRealPosition(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         if (touchedElement == lastBubbledElement) {
             return;
         }
         lastBubbledElement = touchedElement;
         int index = calculateIndexInList(touchedGroup);
         touchedGroup.updateElementIndex(this, index);
     }
 
     private void resetDragPositionHistory() {
         dragHistoryIndex = 0;
         for (int i = 0; i < DRAG_HISTORY_LENGTH; i++) {
             dragHistoryX[i] = CumberlessMouseListener.mouseX;
             dragHistoryY[i] = CumberlessMouseListener.mouseY;
             dragHistoryTime[i] = System.currentTimeMillis();
         }
     }
 
     private void updateDragPositionHistory() {
         if (System.currentTimeMillis() < dragHistoryTime[dragHistoryIndex] + DRAG_HISTORY_UPDATE_INTERVAL) {
             return;
         }
         dragHistoryIndex = (dragHistoryIndex + 1) % DRAG_HISTORY_LENGTH;
         dragHistoryX[dragHistoryIndex] = CumberlessMouseListener.mouseX;
         dragHistoryY[dragHistoryIndex] = CumberlessMouseListener.mouseY;
         dragHistoryTime[dragHistoryIndex] = System.currentTimeMillis();
     }
 
     private boolean isThrowingElementToFeaturesGroup() {
         if (groupParent != Engine.stepsRoot || Engine.lastAddedElement == null) {
             return false;
         }
         int oldestIndex = (dragHistoryIndex + 1) % DRAG_HISTORY_LENGTH;
         long deltaTime = dragHistoryTime[dragHistoryIndex] - dragHistoryTime[oldestIndex];
         if (deltaTime == 0) {
             return false;
         }
         int deltaX = Math.abs(dragHistoryX[dragHistoryIndex] - dragHistoryX[oldestIndex]);
         int deltaY = Math.abs(dragHistoryY[dragHistoryIndex] - dragHistoryY[oldestIndex]);
         float squaredDist = (float) ((deltaX * deltaX) + (deltaY * deltaY));
         return squaredDist / deltaTime > DRAG_HISTORY_THROW_LIMIT;
     }
 
     private void throwElementToFeaturesGroup() {
         groupParent.removeChild(this);
         if (type == TYPE_FEATURE) {
             Engine.featuresRoot.addChild(this);
         } else if (type == TYPE_SCENARIO || type == TYPE_BACKGROUND) {
             Element elementToAddChildTo;
             if (Engine.lastAddedElement.type == TYPE_FEATURE) {
                elementToAddChildTo = Engine.featuresRoot;
             } else if (Engine.lastAddedElement.type == TYPE_SCENARIO || Engine.lastAddedElement.type == TYPE_BACKGROUND) {
                 elementToAddChildTo = Engine.lastAddedElement.groupParent;
             } else {
                 elementToAddChildTo = Engine.lastAddedElement.groupParent.groupParent;
             }
             if (type == TYPE_SCENARIO || (type == TYPE_BACKGROUND && !hasBackgroundElement(elementToAddChildTo))) {
                 elementToAddChildTo.addChild(this);
             } else {
                 return;
             }
         } else {
             if (Engine.lastAddedElement.type == TYPE_FEATURE) {
                 return;
             }
             if (Engine.lastAddedElement.type == TYPE_SCENARIO || Engine.lastAddedElement.type == TYPE_BACKGROUND) {
                 Engine.lastAddedElement.addChild(this);
             } else {
                 int index = Engine.lastAddedElement.groupParent.findChildIndex(Engine.lastAddedElement);
                 Engine.lastAddedElement.groupParent.addChild(this, index + 1);
             }
         }
         Engine.updateLastAddedElement(this);
     }
 
     public void addChild(Element element, int index) {
         super.addChild(element, index);
         if (element.type == TYPE_BACKGROUND) {
             updateElementIndex(element, 0);
         }
     }
 
     public void addChild(Element element) {
         super.addChild(element);
         if (element.type == TYPE_BACKGROUND) {
             updateElementIndex(element, 0);
         }
     }
     
     private boolean hasBackgroundElement(Element feature) {
         return Engine.findBackgroundElement(feature) != null;
     }
 
     public Element findGroup(int x, int y, int type) {
         if (isInsideGroupRect(x, y) && isAttachableTo(type)) {
             return this;
         } else {
             for (Element child : children) {
                 Element foundElement = child.findGroup(x, y, type);
                 if (foundElement != null) {
                     return foundElement;
                 }
             }
             return null;
         }
     }
 
     private boolean isAttachableTo(int type) {
         for (int attachType : TYPE_ATTACHABLE_TO[type]) {
             if (attachType == this.type) {
                 return true;
             }
         }
         return false;
     }
 
     public void updateElementIndex(Element element, int index) {
         int currentIndex = findChildIndex(element);
         if (currentIndex == index) {
             return;
         }
         if (index == -1) {
             return;
         }
         Engine.cucumberRoot.removeChild(element);
         if (currentIndex == -1) {
             addChild(element, index);
         } else {
             addChild(element, index);
         }
     }
 
     protected Element duplicate() {
         TextElement element = new TextElement(type, rootType, calculateRenderWidthFromRoot(rootType), title, step.duplicate());
         element.animation.colorAnimation.setAlpha(Animation.FADE_ALPHA_DEFAULT, Animation.FADE_SPEED_REENTRANCE);
         element.animation.moveAnimation.setRealPosition(animation.moveAnimation, true);
         element.animation.moveAnimation.setRenderPosition(animation.moveAnimation, true);
         element.renderWidth = renderWidth;
         element.renderHeight = renderHeight;
         element.animation.sizeAnimation.currentWidth = this.renderWidth;
         element.animation.sizeAnimation.currentHeight = this.renderHeight;
         return element;
     }
 
     private int calculateIndexInList(Element touchedGroup) {
         Element touchedElement = Engine.cucumberRoot.findElementRealPosition(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         if (touchedElement == touchedGroup) {
             return touchedGroup.folded ? -1 : 0;
         }
         if (touchedElement == null) {
             return -1;
         }
         return touchedGroup.findChildIndex(touchedElement);
     }
 
     protected void renderBefore(Graphics2D g) {
         if (!animation.colorAnimation.isVisible()) {
             return;
         }
         if (animation.moveAnimation.renderX > Engine.canvasWidth || animation.moveAnimation.renderY > Engine.canvasHeight ||
             animation.moveAnimation.renderX + renderWidth < 0 || animation.moveAnimation.renderY + renderHeight < 0) {
             return;
         }
         renderElement(g);
     }
 
     protected void renderAfter(Graphics2D g) {
         groupHeight -= buttonGroupHeight;
     }
 
     private void renderElement(Graphics2D canvas) {
         ImageTemplate imageTemplate = RenderOptimizer.getImageTemplate(renderWidth + SHADOW_SIZE, renderHeight + SHADOW_SIZE);
         BufferedImage image = imageTemplate.image;
         Graphics2D g = imageTemplate.graphics;
         if (Engine.fpsDetails != Engine.DETAILS_NONE) {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         } else {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         }
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, animation.colorAnimation.getAlpha()));
         clear(g);
         drawBar(g);
         drawText(g);
         drawTags(g);
         canvas.drawImage(image, (int) animation.moveAnimation.renderX, (int) animation.moveAnimation.renderY, null);
         drawButtons(canvas);
     }
 
     protected void renderHintsInternal(Graphics2D g) {
         if (!isHighlighted()) {
             return;
         }
         if (hasScreenshot()) {
             drawScreenshot(g);
         }
         if (!Util.isEmpty(errorMessage)) {
             drawHint(g, errorMessage, CumberlessMouseListener.mouseX + 15, CumberlessMouseListener.mouseY, COLOR_TEXT_ERROR_MESSAGE, COLOR_BG_ERROR_MESSAGE);
         }
     }
 
     public String getTagsString() {
         return tags.hasTags() ? tags.toString() : "";
     }
 
     private String getTagsStringOrAtIfEmpty() {
         return tags.hasTags() ? tags.toString() : "@";
     }
 
     private void drawButtons(Graphics2D canvas) {
         for (Button button : buttons) {
             button.render(canvas);
         }
         for (ElementPluginButton button : pluginButtons) {
             button.render(canvas);
         }
     }
 
     private void clear(Graphics2D g) {
         g.setColor(COLOR_BG_CLEAR);
         g.fillRect(0, 0, renderWidth + SHADOW_SIZE, renderHeight + SHADOW_SIZE);
     }
 
     private void drawBar(Graphics2D g) {
         int x = 0;
         int y = buttonGroupHeight;
         int width = renderWidth - 1;
         int height = renderHeight - 1 - buttonGroupHeight;
         if (buttonGroupVisible) {
             GuiUtil.drawShadow(g, x, 0, buttonGroupWidth, renderHeight - 5, BAR_ROUNDING);
             GuiUtil.drawBorder(g, x, 0, buttonGroupWidth, renderHeight - 5, BAR_ROUNDING, COLOR_BORDER_SHADOW, 1.5f);
         }
         GuiUtil.drawShadow(g, x, y, width + 1, height + 1, BAR_ROUNDING);
         GuiUtil.drawBarFilling(g, x, y, width + 1, height + 1, BAR_ROUNDING, COLOR_BORDER_SHADOW); // Outline - for some reason fillRoundRect is faster than drawRoundRect
         boolean renderedBorder = false;
         renderedBorder |= renderPlaying(g);
         renderedBorder |= renderEditing(g);
         renderedBorder |= renderPlayFailures(g);
         int space = renderedBorder ? 1 : 0;
         GuiUtil.drawBarFilling(g, x + 1 + space, y + 1 + space, width - 1 - (space * 2), height - 1 - (space * 2), BAR_ROUNDING, getBackgroundColor());
         if (buttonGroupVisible) {
             GuiUtil.drawBarFilling(g, x + 1 + space, 1 + space, buttonGroupWidth - 1 - (space * 2), renderHeight - 8, BAR_ROUNDING, getBackgroundColor());
         }
     }
 
     public Color getBackgroundColor() {
         int highlightToIndex = isHighlighted() ? 1 : 0;
         if (type == TYPE_STEP && isFailed) {
             return COLOR_BG_FAILED;
         } else if (type != TYPE_STEP || step.matchedByStepDefinition() || groupParent == Engine.stepsRoot) {
             return COLOR_BG_FILL[type][highlightToIndex];
         } else {
             return COLOR_BG_UNRECOGNIZED_STEP[highlightToIndex];
         }
     }
 
     private boolean renderPlaying(Graphics2D g) {
         if (!Player.isRunning()) {
             return false;
         }
         if (!isRunnable() || !isRunning()) {
             return false;
         }
         renderBorder(g, COLOR_BORDER_PLAYING, 1.5f);
         renderAnimatedBorder(g);
         return true;
     }
 
     private boolean renderPlayFailures(Graphics2D g) {
         if (!isRunnable() || !isFailed) {
             return false;
         }
         renderBorder(g, COLOR_BORDER_FAILURE, 1.5f);
         return true;
     }
 
     private boolean renderEditing(Graphics2D g) {
         if (!EditBox.isEditing(this)) {
             return false;
         }
         renderBorder(g, COLOR_BORDER_EDITING, 1.5f);
         return true;
     }
 
     private void renderBorder(Graphics2D g, Color color, float width) {
         GuiUtil.drawBorder(g, 1, 1 + buttonGroupHeight, renderWidth - 3, renderHeight - 3 - buttonGroupHeight, BAR_ROUNDING, color, width);
         if (buttonGroupVisible) {
             GuiUtil.drawBorder(g, 1, 1, buttonGroupWidth - 2, renderHeight - 10, BAR_ROUNDING, color, width);
         }
     }
 
     private void renderAnimatedBorder(Graphics2D g) {
         Stroke oldStroke = Animation.setStrokeAnimation(g, PLAY_ANIMATION_DASH_LENGTH, PLAY_ANIMATION_DASH_WIDTH, PLAY_ANIMATION_SPEED);
         g.setColor(Player.getPlayingColor(this));
         g.drawRoundRect(1, 1 + buttonGroupHeight, renderWidth - 3, renderHeight - 3 - buttonGroupHeight, BAR_ROUNDING, BAR_ROUNDING);
         if (buttonGroupVisible) {
             g.drawRoundRect(1, 1, buttonGroupWidth - 2, renderHeight - 10, BAR_ROUNDING, BAR_ROUNDING);
         }
         g.setStroke(oldStroke);
     }
 
     private boolean isRunning() {
         return (type == TYPE_FEATURE && Player.isCurrentFeature(this)) ||
                (type == TYPE_BACKGROUND && Player.isCurrentBackground(this)) ||
                (type == TYPE_SCENARIO && Player.isCurrentScenario(this)) ||
                (type == TYPE_STEP && Player.isCurrentStep(this));
     }
     
     private boolean isRunnable() {
         return type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_BACKGROUND || type == TYPE_STEP;
     }
 
     private void drawText(Graphics2D g) {
         for (CucumberStepPart part : step.getParts()) {
             if (!part.render) {
                 continue;
             }
             if (part.type == CucumberStepPart.PartType.TEXT) {
                 g.setColor(COLOR_TEXT_NORMAL);
             } else {
                 g.setColor(COLOR_TEXT_PARAMETER);
             }
             int x = part.startX;
             int y = part.startY;
             for (String text : part.wrappedText) {
                 int drawX = x + getTextPaddingLeft();
                 int drawY = y + TEXT_PADDING_VERTICAL - 3;
                 g.drawString(text, drawX, drawY + Engine.fontMetrics.getHeight());
                 updatePartTouchState(part, text, (int) animation.moveAnimation.renderX + drawX, (int) animation.moveAnimation.renderY + drawY);
                 x = 0;
                 y += Engine.fontMetrics.getHeight();
             }
         }
         g.setFont(Engine.FONT_DEFAULT);
     }
 
     private void drawTags(Graphics2D g) {
         if (!shouldRenderTags() || (!isHighlighted() && type == TYPE_FEATURE)) {
             return;
         }
 
         int width = Engine.fontMetrics.stringWidth(tags.toString()) + (HINT_PADDING_HORIZONTAL * 2);
         int height = Engine.fontMetrics.getHeight() + (HINT_PADDING_VERTICAL * 2);
 
         int x = (renderWidth - width) / 2;
         int y = TAGS_PADDING_VERTICAL + buttonGroupHeight;
 
         g.setColor(Util.blendColor(getBackgroundColor(), COLOR_BG_TAGS));
         g.fillRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
         g.drawRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
 
         g.setColor(COLOR_TEXT_TAGS);
         g.drawString(tags.toString(), x + HINT_PADDING_HORIZONTAL, y + Engine.fontMetrics.getHeight() - 3 + HINT_PADDING_VERTICAL);
     }
 
     private void drawHint(Graphics g, String hint, int x, int y, Color textColor, Color bgColor) {
         drawHint(g, hint, x, y, textColor, bgColor, COLOR_BG_HINT, false, true, true);
     }
     
     private void drawHint(Graphics g, String hint, int x, int y, Color textColor, Color bgColor, Color borderColor, boolean centerAligned, boolean bottomAligned, boolean forceOnScreen) {
         int width = Engine.fontMetrics.stringWidth(hint) + (HINT_PADDING_HORIZONTAL * 2);
         int height = Engine.fontMetrics.getHeight() + (HINT_PADDING_VERTICAL * 2);
 
         if (bottomAligned) {
             y -= height;
         }
         if (centerAligned) {
             x -= width / 2;
         }
         if (forceOnScreen) {
             x = Math.max(x, 0);
             y = Math.max(y, 0);
         }
         
         g.setColor(bgColor);
         g.fillRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
         g.setColor(borderColor);
         g.drawRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
 
         g.setColor(textColor);
         g.drawString(hint, x + HINT_PADDING_HORIZONTAL, y + Engine.fontMetrics.getHeight() - 3 + HINT_PADDING_VERTICAL);
     }
 
     private boolean hasScreenshot() {
         return errorScreenshots != null && errorScreenshots.length > 0;
     }
 
     private void drawScreenshot(Graphics g) {
         if (errorScreenshots == null || errorScreenshots.length == 0 || errorScreenshots[0] == null) {
             return;
         }
         int x = CumberlessMouseListener.mouseX + 15;
         int y = Math.min(CumberlessMouseListener.mouseY + 10, Engine.canvasHeight - errorScreenshots[0].getHeight(null));
 
         g.setColor(Color.BLACK);
 
         g.fillRect(x - 1, y - 1, errorScreenshots[0].getWidth(null) + 2, errorScreenshots[0].getHeight(null) + 2);
         g.drawImage(errorScreenshots[0], x, y, null);
 
         if (errorScreenshots.length < 2 || errorScreenshots[1] == null) {
             return;
         }
         x += errorScreenshots[0].getWidth(null) + 5;
         g.fillRect(x - 1, y - 1, errorScreenshots[1].getWidth(null) + 2, errorScreenshots[1].getHeight(null) + 2);
         g.drawImage(errorScreenshots[1], x, y, null);
     }
 
     private void updatePartTouchState(CucumberStepPart part, String text, int x, int y) {
         part.isTouched =
                 CumberlessMouseListener.mouseX >= x && CumberlessMouseListener.mouseX <= x + Engine.fontMetrics.stringWidth(text) &&
                 CumberlessMouseListener.mouseY >= y && CumberlessMouseListener.mouseY <= y + Engine.fontMetrics.getHeight();
     }
 
     private int getTextPaddingLeft() {
         return BUTTON_WIDTH + BUTTON_PADDING_HORIZONTAL;
     }
 
     private int getTextPaddingRight() {
         return TEXT_PADDING_HORIZONTAL + BUTTON_WIDTH;
     }
 
     private boolean isButtonTouched() {
         for (Button button : buttons) {
             if (button.isTouched()) {
                 return true;
             }
         }
         for (ElementPluginButton button : pluginButtons) {
             if (button.isTouched()) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean hasPlayButton() {
         return (type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_BACKGROUND) && Engine.isPlayableDeviceEnabled();
     }
 
     private boolean hasEditButton() {
         return type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_BACKGROUND || type == TYPE_COMMENT || (type == TYPE_STEP && !step.matchedByStepDefinition());
     }
 
     private boolean hasTagsAddButton() {
         return type == TYPE_FEATURE || type == TYPE_SCENARIO;
     }
 
     private boolean shouldRenderTags() {
         return !isParentFolded() && tags.hasTags();
     }
 
     public StringBuilder buildFeatureInternal() {
         StringBuilder sb = new StringBuilder();
         if (!Util.isEmpty(comment)) {
             sb.append(comment).append("\n");
         }
         if (!Util.isEmpty(tags.toString())) {
             if (type == TYPE_SCENARIO) {
                 sb.append(EXPORT_INDENT);
             }
             sb.append(tags.toString()).append("\n");
         }
         if (type == TYPE_FEATURE) {
             sb.append(Locale.getString("feature")).append(": ").append(convertNewlines(title)).append("\n\n");
         }
         if (type == TYPE_BACKGROUND) {
             sb.append(EXPORT_INDENT).append(Locale.getString("background")).append(":\n");
         }
         if (type == TYPE_SCENARIO) {
             sb.append(EXPORT_INDENT).append(Locale.getString("scenario")).append(": ").append(title).append("\n");
         }
         if (type == TYPE_STEP) {
             sb.append(EXPORT_INDENT).append(EXPORT_INDENT).append(step.toString()).append("\n");
         }
         if (type == TYPE_COMMENT) {
             sb.append(EXPORT_INDENT).append(EXPORT_INDENT).append("# ").append(title).append("\n");
         }
         return sb;
     }
 
     private String convertNewlines(String title) {
         return title.replaceAll("\\s\\*\\s", "\n");
     }
 
     protected void updateStepsInternal() {
         if (type == TYPE_STEP) {
             step = FeatureLoader.findMatchingStep(step.toString());
         }
     }
 
     public boolean save() {
         return export(filename);
     }
 
     public boolean export(File directory) {
         String relativePath = ElementHelper.getRelativePath(filename);
         File file = new File(directory, relativePath);
         File path = new File(Util.getPath(file.getAbsolutePath()));
         if (!path.exists() && !path.mkdirs()) {
             return false;
         }
         return export(file.getAbsolutePath());
     }
 
     public boolean export(String filename) {
         if (type != TYPE_FEATURE) {
             return false;
         }
         if (Util.isEmpty(filename)) {
             return false;
         }
         BufferedWriter out = null;
         try {
             out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
             out.append(buildFeature().toString());
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         } finally {
             Util.close(out);
         }
         return true;
     }
 
     private static boolean isEditableParameter(String text) {
         return "ResourceKey".equals(text) || "*".equals(text);
     }
 
     private void addTags(boolean isNewButton) {
         List<String> nonUsedTags = new ArrayList<String>(Engine.getDefinedTags());
         nonUsedTags.removeAll(tags.toSet());
         if (!nonUsedTags.isEmpty()) {
             nonUsedTags.add("*");
             DropDown.show(
                     (isNewButton ? tagsNewButton.renderX : tagsAddButton.renderX),
                     (isNewButton ? tagsNewButton.renderY : tagsAddButton.renderY) + TEXT_PADDING_VERTICAL,
                     new DropDown.DropDownCallback() {
                         public void chooseItem(String item) {
                             if ("*".equals(item)) {
                                 editTags();
                             } else {
                                 tags.add(item);
                             }
                         }
                     },
                     Util.sortedTagList(nonUsedTags));
         } else {
             editTags();
         }
     }
 
     private void removeTags() {
         if (!tags.hasTags()) {
             return;
         } else if (tags.toList().size() <= 1) {
             tags = new Tag("");
         } else {
             DropDown.show(
                     tagsAddButton.renderX + TEXT_PADDING_HORIZONTAL,
                     tagsAddButton.renderY + TEXT_PADDING_VERTICAL,
                     new DropDown.DropDownCallback() {
                         public void chooseItem(String item) {
                             tags.remove(item);
                         }
                     },
                     Util.sortedTagList(tags.toList()));
         }
     }
 
     private void editTags() {
         if (!tags.hasTags()) {
             tags = new Tag("@");
         }
         EditBox.showEditTags(this);
     }
 
     public Set<String> getTagsInternal() {
         return tags.toSet();
     }
 }
