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
 
 package com.trollsahead.qcumberless.gui.elements;
 
 import com.trollsahead.qcumberless.engine.*;
 import com.trollsahead.qcumberless.gui.*;
 import com.trollsahead.qcumberless.gui.Button;
 import com.trollsahead.qcumberless.model.*;
 import com.trollsahead.qcumberless.util.ElementHelper;
 import com.trollsahead.qcumberless.util.FileUtil;
 import com.trollsahead.qcumberless.util.HistoryHelper;
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
 
 public abstract class BaseBarElement extends Element {
     public static final int TYPE_FEATURE          = 0;
     public static final int TYPE_BACKGROUND       = 1;
     public static final int TYPE_SCENARIO         = 2;
     public static final int TYPE_STEP             = 3;
     public static final int TYPE_COMMENT          = 4;
     public static final int TYPE_EXAMPLES         = 5;
     public static final int TYPE_SCENARIO_OUTLINE = 6;
     public static final int TYPE_GROUPING         = 7;
 
     public static final float BAR_TRANSPARENCY = 0.7f;
 
     public static final Color COLOR_TEXT_NORMAL        = new Color(0x000000);
     public static final Color COLOR_TEXT_PARAMETER     = new Color(0xDDDD88);
     public static final Color COLOR_TEXT_ERROR_MESSAGE = new Color(0x000000);
     public static final Color COLOR_TEXT_TAGS          = new Color(0x000000);
     public static final Color COLOR_TEXT_COMMENT       = new Color(0.0f, 0.0f, 0.0f, 0.7f);
 
     public static final Color BAR_COLOR_NOT_YET_PLAYED  = new Color(0.5f, 0.5f, 0.5f, 0.5f);
     public static final Color BAR_COLOR_SUCCESS         = new Color(0.2f, 0.9f, 0.2f, 0.5f);
     public static final Color BAR_COLOR_FAILURE         = new Color(1.0f, 0.2f, 0.2f, 0.8f);
     public static final Color BAR_COLOR_PLAYING_COMMENT = new Color(0.1f, 0.1f, 0.1f, 0.5f);
 
     public static final Color COLOR_BG_ERROR_MESSAGE   = new Color(0.8f, 0.8f, 0.4f, 0.8f);
     public static final Color COLOR_BG_TAGS            = new Color(0.0f, 0.0f, 0.0f, 0.05f);
     public static final Color COLOR_BG_CLEAR           = new Color(1.0f, 1.0f, 1.0f, 0.0f);
     public static final Color COLOR_BG_HINT            = new Color(0.0f, 0.0f, 0.0f, 0.8f);
 
     public static final Color COLOR_PARAM_MISMATCH     = new Color(1.0f, 0.0f, 0.0f);
 
     protected static final Color COLOR_BORDER_SHADOW  = new Color(0.0f, 0.0f, 0.0f, 0.8f);
     protected static final Color COLOR_BORDER_PLAYING = new Color(0.0f, 0.0f, 0.0f, 0.4f);
     protected static final Color COLOR_BORDER_EDITING = new Color(1.0f, 1.0f, 0.5f, 0.8f);
 
     public static final float BORDER_STROKE_WIDTH = 1.5f;
 
     public static final int RENDER_WIDTH_MAX_FEATURE_EDITOR = 600;
     public static final int RENDER_WIDTH_MAX_STEP_DEFINITIONS = 400;
     public static final int RENDER_HEIGHT_MINIMUM = 20;
     public static final int SHADOW_SIZE = 5;
 
     private static final long BREAKPOINT_ANIMATION_BLINK_SPEED = 500;
 
     public static final int[] PADDING_HORIZONTAL = new int[] {50, 50, 50, 50, 50, 50, 50,  0};
     public static final int[] PADDING_VERTICAL   = new int[] {10, 10, 10,  5,  5, 10,  5, 10};
 
     public static final int BAR_ROUNDING = 20;
     private static final int HINT_ROUNDING = 12;
 
     public static final int TEXT_PADDING_HORIZONTAL = 15;
     public static final int TEXT_PADDING_VERTICAL   = 5;
 
     protected static final int HINT_PADDING_HORIZONTAL = 7;
     protected static final int HINT_PADDING_VERTICAL = 2;
 
     protected static final int TAGS_PADDING_VERTICAL = 4;
 
     public static final int BUTTON_PADDING_HORIZONTAL = 15;
     public static final int BUTTON_SPACE_HORIZONTAL = 6;
     public static final int BUTTON_SPACE_VERTICAL = 4;
 
     public static final int BUTTON_WIDTH  = 15;
     public static final int BUTTON_HEIGHT = 15;
 
     protected static final int BUTTON_GROUP_HEIGHT = BUTTON_HEIGHT + BUTTON_SPACE_VERTICAL;
 
     protected static final int COMMENT_PADDING_LEFT = BUTTON_WIDTH + BUTTON_PADDING_HORIZONTAL + 15;
 
     protected static final long UNFOLD_GROUP_DELAY = 500L;
 
     public static final float PLAY_ANIMATION_SPEED = 30.0f;
     public static final float PLAY_ANIMATION_DASH_LENGTH = 10.0f;
 
     private static final int DRAG_SCROLL_AREA_HEIGHT = 100;
     private static final int DRAG_SCROLL_AMOUNT = 10;
 
     protected int tagsWidth = 0;
     protected int tagsHeight = 0;
 
     protected int commentHeight = 0;
 
     protected int oldRenderWidth = -1;
 
     public Step step;
 
    protected String title;
     protected String filename = null;
     public Tag tags;
 
     protected String comment = null;
     protected List<String> commentWrapped = null;
 
     protected List<Button> buttons;
     protected List<ElementPluginButton> pluginButtons;
     protected Button expandButton;
     protected Button trashcanButton;
     protected Button playButton;
     protected Button stepButton;
     protected Button editButton;
     protected Button interactiveDesignerButton;
     protected Button tagsAddButton;
     protected Button tagsNewButton;
     protected Button tagsRemoveButton;
 
     protected static final int DRAG_HISTORY_LENGTH = 5;
     protected static final long DRAG_HISTORY_UPDATE_INTERVAL = 5;
     protected static final float DRAG_HISTORY_THROW_LIMIT = 10.0f;
 
     protected int[] dragHistoryX = new int[DRAG_HISTORY_LENGTH];
     protected int[] dragHistoryY = new int[DRAG_HISTORY_LENGTH];
     protected long[] dragHistoryTime = new long[DRAG_HISTORY_LENGTH];
     protected int dragHistoryIndex = 0;
 
     protected int buttonGroupHeight = 0;
     protected boolean buttonGroupVisible = false;
     protected boolean buttonGroupVisibleOld = false;
     protected int buttonGroupWidth = 0;
     protected int buttonGroupCount = 0;
     protected Button buttonGroupSingleButton = null;
 
     protected Element lastBubbledElement = null;
 
     protected long lastRenderCount = 0;
 
     protected PlayResult playResult;
 
     protected BaseBarElement(int type, int rootType) {
         this(type, rootType, "Untitled");
     }
 
     protected BaseBarElement(int type, int rootType, String title) {
         this(type, rootType, calculateRenderWidthFromRoot(rootType), title, new Step(title));
     }
 
     protected BaseBarElement(int type, int rootType, String title, Step step) {
         this(type, rootType, calculateRenderWidthFromRoot(rootType), title, step, "");
     }
 
     protected BaseBarElement(int type, int rootType, String title, int width) {
         this(type, rootType, width, title, new Step(title), "");
     }
 
     protected BaseBarElement(int type, int rootType, int width, String title, Step step) {
         this(type, rootType, width, title, step, "");
     }
 
     protected BaseBarElement(int type, int rootType, int width, String title, Step step, String tags) {
         super();
         this.type = type;
         this.rootType = rootType;
         this.renderWidth = width;
         this.title = title;
         this.step = step != null ? step : new Step(title);
         this.tags = new Tag(tags);
         this.playResult = new PlayResult();
         folded = false;
         animation.colorAnimation.setColor(getNormalBackgroundColor());
         animation.sizeAnimation.currentWidth = this.renderWidth;
         animation.sizeAnimation.currentHeight = this.renderHeight;
         addButtons();
     }
 
     protected void addButtons() {
         buttons = new ArrayList<Button>();
         expandButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_EXPAND, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_EXPAND, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_EXPAND, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_TOP,
                 new Button.ButtonNotification() {
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
                 new Button.ButtonNotification() {
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
                 new Button.ButtonNotification() {
                     public void onClick() {
                         play();
                     }
                 },
                 this);
         buttons.add(playButton);
         stepButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_STEP_ELEMENT, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_STEP_ELEMENT, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_STEP_ELEMENT, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.ButtonNotification() {
                     public void onClick() {
                         step();
                     }
                 },
                 this);
         buttons.add(stepButton);
         editButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_EDIT, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_EDIT, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_EDIT, ThumbnailState.NORMAL.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.ButtonNotification() {
                     public void onClick() {
                         if (BaseBarElement.this.type == TYPE_FEATURE || BaseBarElement.this.type == TYPE_SCENARIO || BaseBarElement.this.type == TYPE_SCENARIO_OUTLINE) {
                             EditBox.showMultilineEditElement(BaseBarElement.this);
                         } else {
                             EditBox.showSinglelineEditElement(BaseBarElement.this);
                         }
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
                 new Button.ButtonNotification() {
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
                 new Button.ButtonNotification() {
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
                 new Button.ButtonNotification() {
                     public void onClick() {
                         removeTags();
                     }
                 },
                 this);
         buttons.add(tagsRemoveButton);
         interactiveDesignerButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_INTERACTIVE_DESIGNER, ThumbnailState.NORMAL.ordinal()), Images.getImage(Images.IMAGE_INTERACTIVE_DESIGNER, ThumbnailState.HIGHLIGHTED.ordinal()), Images.getImage(Images.IMAGE_INTERACTIVE_DESIGNER, ThumbnailState.PRESSED.ordinal()),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER,
                 new Button.ButtonNotification() {
                     public void onClick() {
                         Engine.interactiveDesignerEngine.setElement(BaseBarElement.this);
                         Engine.showEngine(Engine.interactiveDesignerEngine);
                     }
                 },
                 this);
         buttons.add(interactiveDesignerButton);
         addAdditionalButtons();
         pluginButtons = GuiUtil.getPluginButtonsForElement(this);
         updateButtonPositions();
         for (Button button : buttons) {
             button.setVisible(false);
         }
         for (ElementPluginButton button : pluginButtons) {
             button.setVisible(false);
         }
     }
 
     protected abstract void addAdditionalButtons();
 
     protected void updateButtonPositions() {
         buttonGroupCount = 0;
         buttonGroupSingleButton = null;
         trashcanButton.setPosition(BUTTON_PADDING_HORIZONTAL, buttonGroupHeight / 2);
         expandButton.setPosition(BUTTON_PADDING_HORIZONTAL, buttonGroupHeight + TEXT_PADDING_VERTICAL);
         if (!isHighlighted()) {
             return;
         }
         buttonGroupWidth = BUTTON_PADDING_HORIZONTAL;
         if (hasPlayButton()) {
             buttonGroupWidth = addGroupButton(playButton, buttonGroupWidth);
         }
         if (hasStepButton()) {
             buttonGroupWidth = addGroupButton(stepButton, buttonGroupWidth);
         }
         if (hasTagsAddButton()) {
             if (tags.hasTags()) {
                 tagsAddButton.setPosition((renderWidth + tagsWidth) / 2 + BUTTON_SPACE_HORIZONTAL, TAGS_PADDING_VERTICAL + (tagsHeight / 2) + buttonGroupHeight);
                 tagsRemoveButton.setPosition((renderWidth + tagsWidth) / 2 + (BUTTON_SPACE_HORIZONTAL * 2) + BUTTON_WIDTH, TAGS_PADDING_VERTICAL + (tagsHeight / 2) + buttonGroupHeight);
             } else {
                 buttonGroupWidth = addGroupButton(tagsNewButton, buttonGroupWidth);
             }
         }
         if (hasEditButton()) {
             buttonGroupWidth = addGroupButton(editButton, buttonGroupWidth);
         }
         if (hasInteractiveDesignerButton()) {
             buttonGroupWidth = addGroupButton(interactiveDesignerButton, buttonGroupWidth);
         }
         updateAdditionalButtonPositions();
         for (ElementPluginButton button : pluginButtons) {
             if (!button.getCallback().isVisibleForElement(this)) {
                 continue;
             }
             buttonGroupWidth = addGroupButton(button, buttonGroupWidth);
         }
         buttonGroupWidth -= BUTTON_SPACE_HORIZONTAL;
         if (buttonGroupCount == 1) {
             buttonGroupSingleButton.setPosition(BUTTON_PADDING_HORIZONTAL, renderHeight / 2);
         }
     }
 
     protected abstract void updateAdditionalButtonPositions();
 
     protected int addGroupButton(Button button, int x) {
         button.setPosition(x, BUTTON_SPACE_VERTICAL + (BUTTON_HEIGHT / 2));
         buttonGroupCount++;
         buttonGroupSingleButton = button;
         return x + BUTTON_WIDTH + BUTTON_SPACE_HORIZONTAL;
     }
 
     public void setTitleAndComment(String title) {
         if (type == TYPE_STEP || type == TYPE_COMMENT) {
             this.title = Util.isEmpty(title) ? "-" : title;
         } else {
             if (type != TYPE_FEATURE) {
                 title = ElementHelper.ensureOnlyOneTitleLine(title);
             }
             this.comment = ElementHelper.extractCommentFromTitle(title);
             this.title = ElementHelper.removeCommentFromTitle(title);
            commentWrapped = null;
         }
         step = FeatureLoader.findMatchingStep(this.title);
     }
 
     public void setTitle(String title) {
         this.title = Util.isEmpty(title) ? "-" : title;
         step = FeatureLoader.findMatchingStep(this.title);
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public void setTags(String tags) {
         this.tags = new Tag(tags);
     }
 
     public void setComment(String comment) {
         this.comment = comment;
         this.commentWrapped = null;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void updateSelf(long time) {
         updateButtons();
         lastRenderCount = Engine.renderCounter;
     }
 
     public String getTitle() {
         return step.toString();
     }
 
     private void updateButtons() {
         if (groupParent == null) {
             return;
         }
         for (Button button : buttons) {
             button.setVisible(false);
         }
         for (ElementPluginButton button : pluginButtons) {
             button.setVisible(false);
         }
         if (!isHighlighted()) {
             expandButton.setVisible(false);
             toggleButtonGroup(false);
             return;
         }
         boolean hasButtonGroupButtons = buttonGroupVisible || buttonGroupCount == 1;
         expandButton.setVisible(buttonGroupCount > 1);
         updateButtonGroupState();
         trashcanButton.setVisible(hasTrashcanButton());
         tagsRemoveButton.setVisible(hasTagsAddButton() && tags.hasTags());
         tagsAddButton.setVisible(hasTagsAddButton() && tags.hasTags());
         tagsNewButton.setVisible(hasTagsAddButton() && hasButtonGroupButtons && !tags.hasTags());
         playButton.setVisible(hasPlayButton() && hasButtonGroupButtons);
         stepButton.setVisible(hasStepButton() && hasButtonGroupButtons);
         editButton.setVisible(hasEditButton() && hasButtonGroupButtons);
         interactiveDesignerButton.setVisible(hasInteractiveDesignerButton() && hasButtonGroupButtons);
         updateAdditionalButtonsVisibleState();
         for (Button button : buttons) {
             button.update();
         }
         for (ElementPluginButton button : pluginButtons) {
             button.setVisible(button.getCallback().isVisibleForElement(this) && hasButtonGroupButtons);
             button.update();
         }
     }
 
     protected abstract void updateAdditionalButtonsVisibleState();
 
     private void updateButtonGroupState() {
         if (Engine.renderCounter <= lastRenderCount) {
             return;
         }
         if (expandButton.isVisible() && CumberlessMouseListener.mouseX < expandButton.renderX + expandButton.renderWidth) {
             toggleButtonGroup(true);
         } else if (buttonGroupCount <= 1 || !expandAreaIsTouched()) {
             toggleButtonGroup(false);
         }
     }
 
     private boolean expandAreaIsTouched() {
         return CumberlessMouseListener.mouseX < animation.moveAnimation.renderX + buttonGroupWidth;
     }
 
     private void toggleButtonGroup(boolean visible) {
         buttonGroupVisibleOld = buttonGroupVisible;
         buttonGroupVisible = visible;
         if (buttonGroupVisible != buttonGroupVisibleOld) {
             step.setTextDirty(true);
             shouldStickToParentRenderPosition = true;
         }
     }
 
     protected void calculateRenderPosition(Graphics2D g) {
         if (groupParent != null) {
             animation.moveAnimation.setRealPosition(groupParent.animation.moveAnimation, !shouldStickToParentRenderPosition);
             animation.moveAnimation.realY += groupParent.groupHeight;
             if (groupParent instanceof BaseBarElement) {
                 animation.moveAnimation.realX += PADDING_HORIZONTAL[((BaseBarElement) groupParent).type];
             }
         }
         if (shouldStickToParentRenderPosition || animation.alphaAnimation.justBecameVisible()) {
             animation.moveAnimation.setRenderPosition(animation.moveAnimation, false);
         }
         oldRenderWidth = renderWidth;
         renderWidth = calculateRenderWidth();
         wrapComment();
         calculateButtonGroupHeight();
         calculateCommentHeight();
         calculateTagsSize();
         int elementHeight = calculatePartPositions();
         renderHeight = Math.max(RENDER_HEIGHT_MINIMUM, elementHeight + (TEXT_PADDING_VERTICAL * 2)) + getAdditionalRenderHeight();
         paddingHeight = PADDING_VERTICAL[type];
         groupHeight = renderHeight + paddingHeight;
         updateButtonPositions();
     }
 
     private void wrapComment() {
         if (renderWidth != oldRenderWidth) {
             commentWrapped = null;
         }
         if (Util.isEmpty(comment) || !Util.isEmpty(commentWrapped)) {
             return;
         }
         commentWrapped = Util.wrapText(comment, renderWidth - (COMMENT_PADDING_LEFT + getTextPaddingRight()), Engine.fontMetrics);
     }
 
     protected abstract int getAdditionalRenderHeight();
     
     private void calculateButtonGroupHeight() {
         buttonGroupHeight = buttonGroupVisible ? BUTTON_GROUP_HEIGHT : 0;
         animation.moveAnimation.realY -= buttonGroupHeight;
         if (buttonGroupVisible != buttonGroupVisibleOld) {
             animation.moveAnimation.renderY = animation.moveAnimation.realY;
             if (buttonGroupVisible && animation.moveAnimation.realY < 0) {
                 DesignerEngine.featuresRoot.push(-animation.moveAnimation.realY);
             }
         }
     }
 
     private void calculateCommentHeight() {
         if (Util.isEmpty(comment)) {
             commentHeight = 0;
         } else {
             commentHeight = Engine.fontMetrics.getHeight() * commentWrapped.size() + TEXT_PADDING_VERTICAL;
         }
     }
 
     private void calculateTagsSize() {
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
         int maxWidth = Engine.windowWidth;
         if (rootType == ROOT_FEATURE_EDITOR) {
             wantedWidth = RENDER_WIDTH_MAX_FEATURE_EDITOR;
             maxWidth = DesignerEngine.featuresRoot.renderWidth - (maxPadding - (int) DesignerEngine.featuresRoot.animation.moveAnimation.renderX);
         } else if (rootType == ROOT_STEP_DEFINITIONS) {
             wantedWidth = RENDER_WIDTH_MAX_STEP_DEFINITIONS;
             maxWidth = DesignerEngine.stepsRoot.renderWidth - (RootElement.PADDING_HORIZONTAL * 2);
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
         step.setRenderWidth(renderWidth - (getTextPaddingLeft() + getTextPaddingRight()));
         if (!step.isTextDirty()) {
             return step.getLastPartBottom() + Engine.fontMetrics.getHeight();
         }
         int x = 0;
         int y = commentHeight + tagsHeight + buttonGroupHeight;
         for (CucumberStepPart part : step.getParts()) {
             part.wrapText(x, y);
             x = part.endX;
             y = part.endY;
         }
         step.setTextDirty(false);
         return y + Engine.fontMetrics.getHeight();
     }
 
     public void click(int clickCount) {
         if (clickCount > 1 && canDoubleClick()) {
             doubleClick();
             return;
         }
         if (!canEdit() && canSelectPart()) {
             if (clickPart()) {
                 return;
             }
         }
         if (!canEdit()) {
             if (stepButton.click()) {
                 return;
             }
             foldToggle();
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
         if (clickPart()) {
             return;
         }
         foldToggle();
     }
 
     private boolean clickPart() {
         final CucumberStepPart part = getTouchedPart();
         if (part == null || part.type != CucumberStepPart.PartType.ARGUMENT) {
             return false;
         }
         if (part.hook.getValidParameters().length == 1 && Constants.isStringParameter(part.hook.getValidParameters()[0])) {
             EditBox.showEditPart(part);
         } else {
             DropDown.show(
                     (int) animation.moveAnimation.renderX + part.startX + TEXT_PADDING_HORIZONTAL,
                     (int) animation.moveAnimation.renderY + part.startY + TEXT_PADDING_VERTICAL,
                     new DropDown.DropDownCallback() {
                             public void chooseItem(String item) {
                                 if (!Constants.isStringParameter(item)) {
                                     part.setText(item);
                                 }
                                 if (Constants.isStringParameter(item)) {
                                     EditBox.showEditPart(part);
                                 }
                             }
                     },
                     Arrays.asList(part.getValidParameters().getValidParameters()));
         }
         return true;
     }
 
     private void doubleClick() {
         if (groupParent.rootType != ROOT_STEP_DEFINITIONS) {
             return;
         }
         new Thread(new Runnable() {
             public void run() {
                 if (type == TYPE_FEATURE) {
                     if (Locale.isLocaleSet()) {
                         comment = "# language: " + Locale.getLocale().toString().toLowerCase();
                     }
                     filename = CucumberlessDialog.instance.askFeatureFilename();
                     if (filename == null) {
                         return;
                     }
                 }
                 synchronized (Engine.DATA_LOCK) {
                     throwElementToFeaturesGroup();
                     DesignerEngine.addFeature(BaseBarElement.this);
                 }
             }
         }).start();
     }
 
     private void play() {
         DesignerEngine.runTests(this);
     }
 
     private void step() {
         if (type != TYPE_STEP) {
             return;
         }
         DesignerEngine.runInStepMode((StepElement) this);
     }
 
     public void trashElement() {
         if (!canEdit()) {
             return;
         }
         synchronized (Engine.DATA_LOCK) {
             if (groupParent != null) {
                 groupParent.updateElementIndex(this, -1);
                 DesignerEngine.cucumberRoot.removeChild(this);
                 UndoManager.takeSnapshot(DesignerEngine.featuresRoot);
             }
             if (type == TYPE_FEATURE) {
                 DesignerEngine.deleteFeatureFromFilesystemWhenSaving(this);
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
         if (!isFoldableByClicking() || !visible) {
             return;
         }
         if (children.size() == 0 || isParentFolded()) {
             return;
         }
         folded = !folded;
         foldFadeAnimation(folded ? 0.0f : BAR_TRANSPARENCY, true);
         UndoManager.takeSnapshot(DesignerEngine.featuresRoot);
     }
 
     public void fold() {
         fold(true);
     }
 
     public void unfold() {
         unfold(true);
     }
 
     public void fold(boolean animate) {
         if (!isFoldable() || !visible) {
             return;
         }
         folded = true;
         foldFadeAnimation(0.0f, animate);
     }
 
     public void unfold(boolean animate) {
         if (!isFoldable() || !visible) {
             return;
         }
         folded = false;
         foldFadeAnimation(BAR_TRANSPARENCY, animate);
     }
 
     protected boolean isFoldableByClicking() {
         return isFoldable();
     }
 
     public abstract boolean isFoldable();
 
     public void foldFadeAnimation(float alpha, boolean animate) {
         for (Element child : children) {
             if (!child.visible) {
                 child.animation.alphaAnimation.setAlpha(0.0f);
                 continue;
             }
             if (animate) {
                 child.animation.alphaAnimation.setAlpha(alpha, Animation.FADE_SPEED_FOLD);
             } else {
                 child.animation.alphaAnimation.setAlpha(alpha);
             }
             if (!child.folded) {
                 child.foldFadeAnimation(alpha, animate);
             }
         }
     }
 
     private void dragFadeAnimation() {
         highlight(false);
         float alpha = isDragged ? Animation.FADE_ALPHA_DRAG : BAR_TRANSPARENCY;
         animation.alphaAnimation.setAlpha(alpha, Animation.FADE_SPEED_DRAG);
         if (!folded) {
             for (Element child : children) {
                 child.animation.alphaAnimation.setAlpha(alpha, Animation.FADE_SPEED_FOLD);
             }
         }
     }
 
     public void startDrag(boolean isControlDown) {
         if (isButtonTouched()) {
             return;
         }
         super.startDrag(isControlDown);
         dragFadeAnimation();
         resetDragPositionHistory();
         lastBubbledElement = null;
         DesignerEngine.touchedGroup = null;
         DesignerEngine.lastTouchedGroup = null;
         DesignerEngine.dragHighlightGroupStartTime = 0;
         if (isControlDown && type != TYPE_FEATURE && type != TYPE_BACKGROUND) {
             ElementHelper.deepCopyElement(this);
             applyDrag();
         }
     }
 
     public void endDrag() {
         if (isButtonTouched()) {
             return;
         }
         super.endDrag();
         DesignerEngine.touchedGroup = null;
         DesignerEngine.lastTouchedGroup = null;
         DesignerEngine.dragHighlightGroupStartTime = 0;
         dragFadeAnimation();
         if (isThrowingElementToFeaturesGroup()) {
             throwElementToFeaturesGroup();
         }
         DesignerEngine.updateLastAddedElement(this);
     }
 
     public boolean isDragable() {
         return !animation.moveAnimation.isMoving() && !isParentFolded() && visible && canDrag();
     }
 
     protected void applyDrag() {
         updateDragPositionHistory();
         DesignerEngine.touchedGroup = DesignerEngine.cucumberRoot.findGroup(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY, type);
         if (type == TYPE_BACKGROUND && DesignerEngine.touchedGroup != null && DesignerEngine.touchedGroup.type == TYPE_FEATURE) {
             BaseBarElement backgroundElement = ElementHelper.findBackgroundElement(DesignerEngine.touchedGroup);
             if (backgroundElement != null && backgroundElement != this) {
                 DesignerEngine.touchedGroup = null;
             }
         }
         if (DesignerEngine.touchedGroup == null && type == TYPE_FEATURE) {
             DesignerEngine.touchedGroup = DesignerEngine.featuresRoot;
         }
         if (DesignerEngine.touchedGroup == null || DesignerEngine.touchedGroup instanceof RootElement) {
             lastBubbledElement = null;
             return;
         }
         Element touchedElement = DesignerEngine.cucumberRoot.findElementRealPosition(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         if (touchedElement == lastBubbledElement) {
             return;
         }
         lastBubbledElement = touchedElement;
         int index = calculateIndexInList(DesignerEngine.touchedGroup);
         DesignerEngine.touchedGroup.updateElementIndex(this, index);
     }
 
     private void updateUnfoldWhenDraggingState() {
         if (!isDragged) {
             return;
         }
         if (DesignerEngine.touchedGroup != DesignerEngine.lastTouchedGroup) {
             DesignerEngine.dragHighlightGroupStartTime = System.currentTimeMillis();
         }
         DesignerEngine.lastTouchedGroup = DesignerEngine.touchedGroup;
         if (DesignerEngine.touchedGroup == null || !(DesignerEngine.touchedGroup instanceof BaseBarElement)) {
             return;
         }
         if (DesignerEngine.touchedGroup.isFolded() && System.currentTimeMillis() > DesignerEngine.dragHighlightGroupStartTime + UNFOLD_GROUP_DELAY) {
             ((BaseBarElement) DesignerEngine.touchedGroup).foldToggle();
         }
     }
 
     private void scrollCanvasWhenDragging() {
         if (!isDragged) {
             return;
         }
         if (CumberlessMouseListener.mouseY < DRAG_SCROLL_AREA_HEIGHT) {
             DesignerEngine.featuresRoot.scroll(DRAG_SCROLL_AMOUNT);
         }
         if (CumberlessMouseListener.mouseY > DesignerEngine.canvasHeight - DRAG_SCROLL_AREA_HEIGHT) {
             DesignerEngine.featuresRoot.scroll(-DRAG_SCROLL_AMOUNT);
         }
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
         if (groupParent.rootType != ROOT_STEP_DEFINITIONS || DesignerEngine.lastAddedElement == null) {
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
         if (type == TYPE_GROUPING) {
             return;
         }
         groupParent.removeChild(this);
         if (type == TYPE_FEATURE) {
             DesignerEngine.featuresRoot.addChild(this);
         } else if (type == TYPE_SCENARIO || type == TYPE_BACKGROUND || type == TYPE_SCENARIO_OUTLINE) {
             Element elementToAddChildTo;
             if (DesignerEngine.lastAddedElement.type == TYPE_FEATURE) {
                 elementToAddChildTo = DesignerEngine.lastAddedElement;
             } else if (DesignerEngine.lastAddedElement.type == TYPE_SCENARIO || DesignerEngine.lastAddedElement.type == TYPE_BACKGROUND || DesignerEngine.lastAddedElement.type == TYPE_SCENARIO_OUTLINE) {
                 elementToAddChildTo = DesignerEngine.lastAddedElement.groupParent;
             } else {
                 elementToAddChildTo = DesignerEngine.lastAddedElement.groupParent.groupParent;
             }
             if (type == TYPE_SCENARIO || type == TYPE_SCENARIO_OUTLINE || (type == TYPE_BACKGROUND && !hasBackgroundElement(elementToAddChildTo))) {
                 elementToAddChildTo.addChild(this);
                 elementToAddChildTo.unfold();
             } else {
                 return;
             }
         } else {
             if (DesignerEngine.lastAddedElement.type == TYPE_FEATURE) {
                 return;
             }
             if (DesignerEngine.lastAddedElement.type == TYPE_SCENARIO || DesignerEngine.lastAddedElement.type == TYPE_BACKGROUND || DesignerEngine.lastAddedElement.type == TYPE_SCENARIO_OUTLINE) {
                 DesignerEngine.lastAddedElement.addChild(this);
                 DesignerEngine.lastAddedElement.unfold();
             } else {
                 int index = DesignerEngine.lastAddedElement.groupParent.findChildIndex(DesignerEngine.lastAddedElement);
                 DesignerEngine.lastAddedElement.groupParent.addChild(this, index + 1);
                 DesignerEngine.lastAddedElement.groupParent.unfold();
             }
         }
         DesignerEngine.updateLastAddedElement(this);
     }
 
     public void addChild(Element element, int index) {
         super.addChild(element, index);
         ElementHelper.bubbleStaticElementsIntoPlace(this);
     }
 
     public void addChild(Element element) {
         super.addChild(element);
         ElementHelper.bubbleStaticElementsIntoPlace(this);
     }
     
     private boolean hasBackgroundElement(Element feature) {
         return ElementHelper.findBackgroundElement(feature) != null;
     }
 
     public Element findGroup(int x, int y, int type) {
         if (isInsideGroupRect(x, y) && isAttachable(type)) {
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
 
     protected abstract boolean isAttachable(int type);
 
     public void updateElementIndex(Element element, int index) {
         int currentIndex = findChildIndex(element);
         if (currentIndex == index) {
             return;
         }
         if (index == -1) {
             return;
         }
         DesignerEngine.cucumberRoot.removeChild(element);
         addChild(element, index);
     }
 
     public abstract BaseBarElement duplicate();
 
     protected void duplicatePropertiesTo(BaseBarElement element) {
         element.animation.alphaAnimation.setAlpha(BAR_TRANSPARENCY, Animation.FADE_SPEED_REENTRANCE);
         element.animation.moveAnimation.setRealPosition(animation.moveAnimation, true);
         element.animation.moveAnimation.setRenderPosition(animation.moveAnimation, true);
         element.renderWidth = renderWidth;
         element.renderHeight = renderHeight;
         element.animation.sizeAnimation.currentWidth = renderWidth;
         element.animation.sizeAnimation.currentHeight = renderHeight;
         element.setPlayResult(playResult.getState());
         element.animation.colorAnimation.setColor(animation.colorAnimation.getColor());
         element.folded = folded;
         element.comment = comment;
         element.tags = tags != null ? tags.duplicate() : null;
         element.step = step.duplicate();
     }
 
     private int calculateIndexInList(Element touchedGroup) {
         Element touchedElement = DesignerEngine.cucumberRoot.findElementRealPosition(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         if (touchedElement == touchedGroup) {
             return touchedGroup.folded ? -1 : 0;
         }
         if (touchedElement == null) {
             return -1;
         }
         return touchedGroup.findChildIndex(touchedElement);
     }
 
     public void update(long time) {
         scrollCanvasWhenDragging();
         super.update(time);
     }
 
     protected void renderBefore(Graphics2D g) {
         if (!animation.alphaAnimation.isVisible()) {
             return;
         }
         if (animation.moveAnimation.renderX > Engine.windowWidth || animation.moveAnimation.renderY > DesignerEngine.canvasHeight ||
             animation.moveAnimation.renderX + renderWidth < 0 || animation.moveAnimation.renderY + renderHeight < 0) {
             return;
         }
         updateUnfoldWhenDraggingState();
         renderElement(g);
     }
 
     protected void renderAfter(Graphics2D g) {
         groupHeight -= buttonGroupHeight;
     }
 
     private void renderElement(Graphics2D canvas) {
         ImageTemplate imageTemplate = RenderOptimizer.getImageTemplate(renderWidth + SHADOW_SIZE, renderHeight + SHADOW_SIZE);
         BufferedImage image = imageTemplate.image;
         Graphics2D g = imageTemplate.graphics;
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, animation.alphaAnimation.getAlpha()));
         setBackgroundColorAccordingToState();
         clear(g);
         drawBar(g);
         drawComment(g);
         drawText(g);
         drawTags(g);
         drawAdditionals(g);
         canvas.drawImage(image, (int) animation.moveAnimation.renderX, (int) animation.moveAnimation.renderY, null);
         drawButtons(canvas);
     }
 
     protected abstract void drawAdditionals(Graphics2D g);
 
     protected void renderHintsInternal(Graphics2D g) {
         if (rootType != ROOT_FEATURE_EDITOR) {
             return;
         }
        if (!isHighlighted()) {
            return;
        }
        if (DesignerEngine.colorScheme == ColorScheme.DESIGN) {
             drawPartRegExpHint(g);
             return;
         }
         if (playResult.hasScreenshots()) {
             drawScreenshot(g);
         }
         if (playResult.hasErrorMessage()) {
             drawHint(g, playResult.getErrorMessage(), CumberlessMouseListener.mouseX + 15, CumberlessMouseListener.mouseY, COLOR_TEXT_ERROR_MESSAGE, COLOR_BG_ERROR_MESSAGE);
         }
     }
 
     private boolean drawPartRegExpHint(Graphics2D g) {
         CucumberStepPart touchedPart = getTouchedPart();
         if (touchedPart == null || touchedPart.type != CucumberStepPart.PartType.ARGUMENT) {
             return false;
         }
         if (touchedPart.matchesRegExp()) {
             drawHint(g, touchedPart.getRegExp(), CumberlessMouseListener.mouseX + 15, CumberlessMouseListener.mouseY, COLOR_TEXT_ERROR_MESSAGE, COLOR_BG_ERROR_MESSAGE);
         } else {
             drawHint(g, "Text does not match regexp: " + touchedPart.getRegExp(), CumberlessMouseListener.mouseX + 15, CumberlessMouseListener.mouseY, COLOR_TEXT_ERROR_MESSAGE, COLOR_BG_ERROR_MESSAGE);
         }
         return true;
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
             GuiUtil.drawBorder(g, x, 0, buttonGroupWidth, renderHeight - 5, BAR_ROUNDING, COLOR_BORDER_SHADOW, BORDER_STROKE_WIDTH);
         }
         GuiUtil.drawShadow(g, x, y, width + 1, height + 1, BAR_ROUNDING);
         GuiUtil.drawBarBorder(g, x, y, width, height, BAR_ROUNDING, COLOR_BORDER_SHADOW);
         boolean renderedBorder = false;
         renderedBorder |= renderPlaying(g);
         renderedBorder |= renderEditing(g);
         int space = renderedBorder ? 1 : 0;
         GuiUtil.drawBarFilling(g, x + 1 + space, y + 1 + space, width - 1 - (space * 2), height - 1 - (space * 2), BAR_ROUNDING, animation.colorAnimation.getColor());
         if (buttonGroupVisible) {
             GuiUtil.drawBarFilling(g, x + 1 + space, 1 + space, buttonGroupWidth - 1 - (space * 2), renderHeight - 8, BAR_ROUNDING, animation.colorAnimation.getColor());
         }
     }
 
     public void clearRunStatus() {
         playResult = new PlayResult();
         super.clearRunStatus();
     }
 
     public void setPlayResult(PlayResult.State state) {
         setPlayState(new PlayResult(state));
     }
 
     public void setPlayState(PlayResult playResult) {
         if (playResult == null) {
             return;
         }
         this.playResult = playResult;
         animation.colorAnimation.setColor(getBackgroundColorAccordingToState(), Animation.FADE_SPEED_CHANGE_PLAY_COLOR_STATE);
     }
 
     public void setErrorMessage(String message) {
         playResult.setErrorMessage(message);
     }
 
     public void setErrorScreenshots(Screenshot[] screenshots) {
         playResult.addScreenshots(screenshots);
     }
 
     public void toggleColorSchemeInternal() {
         animation.colorAnimation.setColor(getBackgroundColorAccordingToState(), Animation.FADE_SPEED_CHANGE_COLOR_SCHEME);
     }
 
     private void setBackgroundColorAccordingToState() {
         animation.colorAnimation.setColorKeepProgress(getBackgroundColorAccordingToState());
     }
 
     private Color getBackgroundColorAccordingToState() {
         if (DesignerEngine.colorScheme == ColorScheme.DESIGN || (groupParent != null && groupParent.rootType == ROOT_STEP_DEFINITIONS)) {
             return getNormalBackgroundColor();
         }
         if (this instanceof CommentElement) {
             return BAR_COLOR_PLAYING_COMMENT;
         } else if (playResult.isSuccess()) {
             return BAR_COLOR_SUCCESS;
         } else if (playResult.isFailed()) {
             return BAR_COLOR_FAILURE;
         } else {
             return BAR_COLOR_NOT_YET_PLAYED;
         }
     }
 
     public abstract Color getNormalBackgroundColor();
 
     protected Color getTextColor() {
         return COLOR_TEXT_NORMAL;
     }
 
     protected Color getParameterColor() {
         return COLOR_TEXT_PARAMETER;
     }
 
     protected int highlightToColorIndex() {
         return isHighlighted() ? 1 : 0;
     }
 
     private boolean renderPlaying(Graphics2D g) {
         if (!Player.isRunning()) {
             return false;
         }
         if (!isRunnable() || !isRunning()) {
             return false;
         }
         renderBorder(g, COLOR_BORDER_PLAYING, BORDER_STROKE_WIDTH);
         if (Player.isAtStepBreakpoint()) {
             renderBreakpointBorder(g);
         } else {
             renderAnimatedBorder(g);
         }
         return true;
     }
 
     private boolean renderEditing(Graphics2D g) {
         if (!EditBox.isEditing(this)) {
             return false;
         }
         renderBorder(g, COLOR_BORDER_EDITING, BORDER_STROKE_WIDTH);
         return true;
     }
 
     private void renderBorder(Graphics2D g, Color color, float width) {
         GuiUtil.drawBorder(g, 1, 1 + buttonGroupHeight, renderWidth - 3, renderHeight - 3 - buttonGroupHeight, BAR_ROUNDING, color, width);
         if (buttonGroupVisible) {
             GuiUtil.drawBorder(g, 1, 1, buttonGroupWidth - 2, renderHeight - 10, BAR_ROUNDING, color, width);
         }
     }
 
     private void renderAnimatedBorder(Graphics2D g) {
         Stroke oldStroke = Animation.setStrokeAnimation(g, PLAY_ANIMATION_DASH_LENGTH, BORDER_STROKE_WIDTH, PLAY_ANIMATION_SPEED);
         g.setColor(Player.getPlayingColor(this));
         g.drawRoundRect(1, 1 + buttonGroupHeight, renderWidth - 3, renderHeight - 3 - buttonGroupHeight, BAR_ROUNDING, BAR_ROUNDING);
         if (buttonGroupVisible) {
             g.drawRoundRect(1, 1, buttonGroupWidth - 2, renderHeight - 10, BAR_ROUNDING, BAR_ROUNDING);
         }
         g.setStroke(oldStroke);
     }
 
     private void renderBreakpointBorder(Graphics2D g) {
         if (type == TYPE_STEP && (System.currentTimeMillis() % BREAKPOINT_ANIMATION_BLINK_SPEED) < (BREAKPOINT_ANIMATION_BLINK_SPEED / 2)) {
             return;
         }
         Stroke oldStroke = Animation.setStroke(g, PLAY_ANIMATION_DASH_LENGTH, BORDER_STROKE_WIDTH, PLAY_ANIMATION_SPEED);
         g.setColor(Player.getPlayingColor(this));
         g.drawRoundRect(1, 1 + buttonGroupHeight, renderWidth - 3, renderHeight - 3 - buttonGroupHeight, BAR_ROUNDING, BAR_ROUNDING);
         if (buttonGroupVisible) {
             g.drawRoundRect(1, 1, buttonGroupWidth - 2, renderHeight - 10, BAR_ROUNDING, BAR_ROUNDING);
         }
         g.setStroke(oldStroke);
     }
 
     private boolean isRunning() {
         return Player.isElementRunning(this);
     }
     
     private boolean isRunnable() {
         return type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_SCENARIO_OUTLINE || type == TYPE_BACKGROUND || type == TYPE_STEP;
     }
 
     private void drawComment(Graphics2D g) {
         if (Util.isEmpty(comment)) {
             return;
         }
         g.setColor(COLOR_TEXT_COMMENT);
         int y = tagsHeight + buttonGroupHeight + TEXT_PADDING_VERTICAL;
         for (String line : commentWrapped) {
             y += Engine.fontMetrics.getHeight();
             g.drawString(line, getTextPaddingLeft(), y - 3);
         }
     }
 
     private void drawText(Graphics2D g) {
         for (CucumberStepPart part : step.getParts()) {
             if (!part.render || part.wrappedText == null) {
                 continue;
             }
             int x = part.startX;
             int y = part.startY;
             for (String text : part.wrappedText) {
                 int drawX = x + getTextPaddingLeft();
                 int drawY = y + TEXT_PADDING_VERTICAL - 3;
                 if (part.type == CucumberStepPart.PartType.ARGUMENT && !part.matchesRegExp() && rootType == ROOT_FEATURE_EDITOR && DesignerEngine.colorScheme == ColorScheme.DESIGN) {
                     g.setColor(Color.BLACK);
                     g.fillRect(drawX - 3, drawY + Engine.fontMetrics.getHeight() + 2, Engine.fontMetrics.stringWidth(text) + 6, 3);
                     g.setColor(COLOR_PARAM_MISMATCH);
                     g.fillRect(drawX - 2, drawY + Engine.fontMetrics.getHeight() + 3, Engine.fontMetrics.stringWidth(text) + 4, 1);
                 }
                 g.setColor(part.type == CucumberStepPart.PartType.TEXT ? getTextColor() : getParameterColor());
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
 
         g.setColor(Util.blendColor(animation.colorAnimation.getColor(), COLOR_BG_TAGS));
         g.fillRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
         g.drawRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
 
         g.setColor(COLOR_TEXT_TAGS);
         g.drawString(tags.toString(), x + HINT_PADDING_HORIZONTAL, y + Engine.fontMetrics.getHeight() - 3 + HINT_PADDING_VERTICAL);
     }
 
     protected void drawHint(Graphics g, String hint, int x, int y, Color textColor, Color bgColor) {
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
 
     private void drawScreenshot(Graphics g) {
         if (!playResult.hasScreenshots()) {
             return;
         }
         int x = CumberlessMouseListener.mouseX + 15;
         for (Screenshot screenshot : playResult.getScreenshots()) {
             if (screenshot == null || screenshot.getImage() == null) {
                 continue;
             }
             int y = Math.min(CumberlessMouseListener.mouseY + 10, DesignerEngine.canvasHeight - screenshot.getImage().getHeight(null));
         
             g.setColor(Color.BLACK);
         
             g.fillRect(x - 1, y - 1, screenshot.getImage().getWidth(null) + 2, screenshot.getImage().getHeight(null) + 2);
             g.drawImage(screenshot.getImage(), x, y, null);
 
             x += screenshot.getImage().getWidth(null) + 5;
         }
     }
 
     private void updatePartTouchState(CucumberStepPart part, String text, int x, int y) {
         part.isTouched =
                 CumberlessMouseListener.mouseX >= x && CumberlessMouseListener.mouseX <= x + Engine.fontMetrics.stringWidth(text) &&
                 CumberlessMouseListener.mouseY >= y && CumberlessMouseListener.mouseY <= y + Engine.fontMetrics.getHeight();
     }
 
     protected int getTextPaddingLeft() {
         return BUTTON_WIDTH + BUTTON_PADDING_HORIZONTAL;
     }
 
     protected int getTextPaddingRight() {
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
 
     protected boolean hasTrashcanButton() {
         return canEdit();
     }
 
     protected boolean hasPlayButton() {
         return (type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_SCENARIO_OUTLINE || type == TYPE_BACKGROUND)
                 && Engine.isPlayableDeviceEnabled()
                 && !Player.isStarted()
                 && canEdit();
     }
 
     protected boolean hasStepButton() {
         return type == TYPE_STEP
                && Engine.isStepableDeviceEnabled()
                && (!Player.isStarted() || (Player.isAtStepBreakpoint() && Player.getStepMode() == Player.STEP_MODE_RUNNING_SINGLESTEP))
                && canEdit();
     }
 
     protected boolean hasEditButton() {
         return (type == TYPE_FEATURE ||
                 type == TYPE_SCENARIO ||
                 type == TYPE_SCENARIO_OUTLINE ||
                 type == TYPE_BACKGROUND ||
                 type == TYPE_COMMENT ||
                (type == TYPE_STEP && !step.matchedByStepDefinition()))
                 && canEdit();
     }
 
     protected boolean hasInteractiveDesignerButton() {
         return (type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_SCENARIO_OUTLINE || type == TYPE_BACKGROUND)
                 && Engine.isInteractiveDesignerDeviceEnabled()
                 && !Player.isStarted()
                 && canEdit();
     }
 
     protected boolean hasTagsAddButton() {
         return (type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_SCENARIO_OUTLINE) && canEdit();
     }
 
     private boolean shouldRenderTags() {
         return !isParentFolded() && tags.hasTags();
     }
 
     public StringBuilder buildFeatureInternal(FeatureBuildState buildState) {
         String indent = type == TYPE_SCENARIO || type == TYPE_SCENARIO_OUTLINE ? ElementHelper.EXPORT_INDENT : "";
         StringBuilder sb = new StringBuilder();
         if (!Util.isEmpty(comment)) {
             sb.append(Util.indentAllLines(comment, indent)).append("\n");
         }
         if (!Util.isEmpty(tags.toString())) {
             sb.append(indent).append(tags.toString()).append("\n");
         }
         if (buildState.hasState(FeatureBuildState.ADD_STATE_RUN_OUTCOME)) {
             sb.append(HistoryHelper.getRunOutcomeComment(this, buildState.getTime()));
         }
         if (buildState.hasState(FeatureBuildState.ADD_STATE_VIEW)) {
             if (isFoldable()) {
                 sb.append("# foldstate: ").append(isFolded()).append("\n");
             }
             if (DesignerEngine.lastAddedElement == this) {
                 sb.append("# lastAddedElement").append("\n");
             }
         }
         if (buildState.hasState(FeatureBuildState.ADD_STATE_STEP_MODE) && !Util.isEmpty(buildState.getStepPauseDefinition())) {
             sb.append(ElementHelper.EXPORT_INDENT).append(ElementHelper.EXPORT_INDENT)
                     .append(Locale.getString("And")).append(" ")
                     .append(buildState.getStepPauseDefinition())
                     .append("\n");
         }
         return sb;
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
         File path = new File(FileUtil.getPath(file.getAbsolutePath()));
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
             out.append(buildFeature(new FeatureBuildState()).toString());
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         } finally {
             FileUtil.close(out);
         }
         return true;
     }
 
     private void addTags(boolean isNewButton) {
         List<String> nonUsedTags = new ArrayList<String>(DesignerEngine.getDefinedTags());
         nonUsedTags.removeAll(tags.toSet());
         if (!nonUsedTags.isEmpty()) {
             nonUsedTags.add(Constants.TAG_NEW);
             DropDown.show(
                     (isNewButton ? tagsNewButton.renderX : tagsAddButton.renderX),
                     (isNewButton ? tagsNewButton.renderY : tagsAddButton.renderY) + TEXT_PADDING_VERTICAL,
                     new DropDown.DropDownCallback() {
                         public void chooseItem(String item) {
                             if (Constants.TAG_NEW.equals(item)) {
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
             step.setTextDirty(true);
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
             step.setTextDirty(true);
             tags = new Tag("@");
         }
         EditBox.showEditTags(this);
     }
 
     public Set<String> getTagsInternal() {
         return tags.toSet();
     }
 
     public PlayResult getPlayResult() {
         return playResult;
     }
     
     protected boolean canEdit() {
         return Engine.currentEngine == Engine.designerEngine && DesignerEngine.colorScheme != ColorScheme.PLAY &&
                groupParent != DesignerEngine.stepsRoot && groupParent.rootType != ROOT_STEP_DEFINITIONS;
     }
 
     protected boolean canSelectPart() {
         return groupParent.rootType == ROOT_STEP_DEFINITIONS;
     }
 
     protected boolean canDoubleClick() {
         return Engine.currentEngine == Engine.designerEngine && DesignerEngine.colorScheme != ColorScheme.PLAY;
     }
 
     private boolean canDrag() {
         return Engine.currentEngine == Engine.designerEngine && DesignerEngine.colorScheme != ColorScheme.PLAY;
     }
 
     public void setAlphaOnAll(float alpha) {
         animation.alphaAnimation.setAlpha(alpha);
         for (Element child : children) {
             ((BaseBarElement) child).setAlphaOnAll(alpha);
         }
     }
 }
