 // This file is part of Q-Cumberless Testing.
 //
 // Q-Cumberless Testing is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // Q-Cumberless Testing is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with Q-Cumberless Testing.  If not, see <http://www.gnu.org/licenses/>.
 //
 // Copyright 2012
 
 // Daniel Andersen (dani_ande@yahoo.dk)
 
 package com.trollsahead.qcumberless.gui;
 
 import com.trollsahead.qcumberless.engine.Engine;
 import com.trollsahead.qcumberless.engine.FeatureLoader;
 import com.trollsahead.qcumberless.engine.Player;
 import com.trollsahead.qcumberless.model.Step;
 import com.trollsahead.qcumberless.model.Tag;
 import com.trollsahead.qcumberless.util.Util;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
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
             {-1},
             {TYPE_FEATURE},
             {TYPE_FEATURE},
             {TYPE_SCENARIO, TYPE_BACKGROUND},
             {TYPE_SCENARIO, TYPE_BACKGROUND}
     };
 
     public static final Color COLOR_TEXT_NORMAL        = new Color(0x000000);
     public static final Color COLOR_TEXT_PARAMETER     = new Color(0xDDDD88);
     public static final Color COLOR_TEXT_ERROR_MESSAGE = new Color(0x000000);
     public static final Color COLOR_TEXT_TAGS          = new Color(0x000000);
 
     public static final Color COLOR_BG_FAILED            = new Color(0xFF0000);
     public static final Color COLOR_BG_ERROR_MESSAGE     = new Color(0.8f, 0.8f, 0.4f, 0.8f);
     public static final Color COLOR_BG_TAGS              = new Color(0.0f, 0.0f, 0.0f, 0.05f);
 
     public static final Color[] COLOR_BG_UNRECOGNIZED_STEP = {new Color(0xFF66FF), new Color(0xDD99DD)};
 
     public static final Color[][] COLOR_BG_FILL = new Color[][] {
             {new Color(0xAAAAFF), new Color(0xBBBBEE)},
             {new Color(0x888888), new Color(0xAAAAAA)},
             {new Color(0x88FF88), new Color(0xAADDAA)},
             {new Color(0xFF6666), new Color(0xDD9999)},
             {new Color(0xAAAAAA), new Color(0xBBBBBB)},
     };
 
     public static final int RENDER_WIDTH_MAX_FEATURE_EDITOR = 600;
     public static final int RENDER_WIDTH_MAX_STEP_DEFINITIONS = 400;
     public static final int RENDER_HEIGHT_MINIMUM = 20;
 
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
     public static final int TAGS_BUTTON_PADDING_HORIZONTAL = 6;
 
     public static final int BUTTON_WIDTH  = 15;
 
     public static final float PLAY_ANIMATION_SPEED = 30.0f;
     public static final float PLAY_ANIMATION_DASH_LENGTH = 10.0f;
 
     private int tagsWidth = 0;
     private int tagsHeight = 0;
 
     public Step step;
 
     public String title;
     private String filename = null;
     private String comment = null;
     public Tag tags;
 
     private static final int IMAGE_BUFFER_COUNT = 20;
     private static final int IMAGE_BUFFER_WIDTH = 620;
     private static final int[] IMAGE_BUFFER_HEIGHT = new int[IMAGE_BUFFER_COUNT];
 
     private static BufferedImage[] image = new BufferedImage[IMAGE_BUFFER_COUNT];
     private static Graphics2D[] imageGraphics = new Graphics2D[IMAGE_BUFFER_COUNT];
 
     private List<Button> buttons;
     private Button trashcanButton;
     private Button playButton;
     private Button editButton;
     private Button tagsAddButton;
     private Button tagsRemoveButton;
 
     private static final int DRAG_HISTORY_LENGTH = 5;
     private static final long DRAG_HISTORY_UPDATE_INTERVAL = 5;
     private static final float DRAG_HISTORY_THROW_LIMIT = 10.0f;
 
     private int[] dragHistoryX = new int[DRAG_HISTORY_LENGTH];
     private int[] dragHistoryY = new int[DRAG_HISTORY_LENGTH];
     private long[] dragHistoryTime = new long[DRAG_HISTORY_LENGTH];
     private int dragHistoryIndex = 0;
 
     private Element lastBubbledElement = null;
    private Element lastBubbledPosition = null;
     
     private int buttonbarX = 0;
 
     static {
         for (int i = 0; i < IMAGE_BUFFER_COUNT; i++) {
             IMAGE_BUFFER_HEIGHT[i] = (i + 1) * 10;
             image[i] = new BufferedImage(IMAGE_BUFFER_WIDTH, IMAGE_BUFFER_HEIGHT[i], BufferedImage.TYPE_INT_ARGB);
             imageGraphics[i] = image[i].createGraphics();
             imageGraphics[i].setFont(Engine.FONT_DEFAULT);
         }
     }
 
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
         trashcanButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_TRASHCAN, Images.TYPE_NORMAL), Images.getImage(Images.IMAGE_TRASHCAN, Images.TYPE_HIGHLIGHT), Images.getImage(Images.IMAGE_TRASHCAN, Images.TYPE_NORMAL),
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
                 Images.getImage(Images.IMAGE_PLAY, Images.TYPE_NORMAL), Images.getImage(Images.IMAGE_PLAY, Images.TYPE_HIGHLIGHT), Images.getImage(Images.IMAGE_PLAY, Images.TYPE_NORMAL),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER_OF_PARENT,
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
                 Images.getImage(Images.IMAGE_EDIT, Images.TYPE_NORMAL), Images.getImage(Images.IMAGE_EDIT, Images.TYPE_HIGHLIGHT), Images.getImage(Images.IMAGE_EDIT, Images.TYPE_NORMAL),
                 Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER_OF_PARENT,
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
                 Images.getImage(Images.IMAGE_ADD, Images.TYPE_NORMAL), Images.getImage(Images.IMAGE_ADD, Images.TYPE_HIGHLIGHT), Images.getImage(Images.IMAGE_ADD, Images.TYPE_NORMAL),
                 Button.ALIGN_HORIZONTAL_LEFT | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
                         List<String> nonUsedTags = new ArrayList<String>(Engine.getDefinedTags());
                         nonUsedTags.removeAll(tags.toSet());
                         if (!nonUsedTags.isEmpty()) {
                             nonUsedTags.add("*");
                             DropDown.show(
                                     tagsAddButton.renderX + TEXT_PADDING_HORIZONTAL,
                                     tagsAddButton.renderY + TEXT_PADDING_VERTICAL,
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
                 },
                 this);
         buttons.add(tagsAddButton);
         tagsRemoveButton = new Button(
                 0,
                 0,
                 null,
                 Images.getImage(Images.IMAGE_MINUS, Images.TYPE_NORMAL), Images.getImage(Images.IMAGE_MINUS, Images.TYPE_HIGHLIGHT), Images.getImage(Images.IMAGE_MINUS, Images.TYPE_NORMAL),
                 Button.ALIGN_HORIZONTAL_LEFT | Button.ALIGN_VERTICAL_CENTER,
                 new Button.CucumberButtonNotification() {
                     public void onClick() {
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
                 },
                 this);
         buttons.add(tagsRemoveButton);
         updateButtonPositions();
         for (Button button : buttons) {
             button.setVisible(false);
         }
     }
 
     private void updateButtonPositions() {
         trashcanButton.setPosition(BUTTON_PADDING_HORIZONTAL, 0);
         buttonbarX = BUTTON_PADDING_HORIZONTAL;
         if (hasPlayButton()) {
             playButton.setPosition(buttonbarX, 0);
             buttonbarX += BUTTON_WIDTH + 4;
         }
         if (hasEditButton()) {
             editButton.setPosition(buttonbarX, 0);
             buttonbarX += BUTTON_WIDTH + 4;
         }
         if (hasTagsAddButton()) {
             if (tags.hasTags()) {
                 tagsAddButton.setAlignment(Button.ALIGN_HORIZONTAL_LEFT | Button.ALIGN_VERTICAL_CENTER);
                 tagsAddButton.setPosition((renderWidth + tagsWidth) / 2 + TAGS_BUTTON_PADDING_HORIZONTAL, TAGS_PADDING_VERTICAL + (tagsHeight / 2));
                 tagsRemoveButton.setPosition((renderWidth + tagsWidth) / 2 + (TAGS_BUTTON_PADDING_HORIZONTAL * 2) + BUTTON_WIDTH, TAGS_PADDING_VERTICAL + (tagsHeight / 2));
             } else {
                 tagsAddButton.setAlignment(Button.ALIGN_HORIZONTAL_CENTER | Button.ALIGN_VERTICAL_CENTER_OF_PARENT);
                 tagsAddButton.setPosition(buttonbarX, 0);
                 buttonbarX += BUTTON_WIDTH + 4;
             }
         }
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
 
     public void updateSelf(long time) {
         updateButtons();
     }
 
     public String getTitle() {
         return step.toString();
     }
 
     private void updateButtons() {
         trashcanButton.setVisible(hasTrashcanButton() && isHighlighted());
         playButton.setVisible(hasPlayButton() && isHighlighted());
         editButton.setVisible(hasEditButton() && isHighlighted());
         tagsAddButton.setVisible(hasTagsAddButton() && isHighlighted());
         tagsRemoveButton.setVisible(hasTagsAddButton() && isHighlighted() && tags.hasTags());
         for (Button button : buttons) {
             button.update();
         }
     }
 
     protected void calculateRenderPosition(Graphics g) {
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
         calculateTagsPosition(g);
         int elementHeight = calculatePartPositions(g);
         renderWidth = calculateRenderWidth();
         renderHeight = Math.max(RENDER_HEIGHT_MINIMUM, elementHeight + (TEXT_PADDING_VERTICAL * 2));
         paddingHeight = PADDING_VERTICAL[type];
         groupHeight = renderHeight + paddingHeight;
         updateButtonPositions();
     }
 
     private void calculateTagsPosition(Graphics g) {
         if (!shouldRenderTags()) {
             tagsHeight = 0;
             return;
         }
         FontMetrics fontMetrics = g.getFontMetrics();
         tagsWidth = fontMetrics.stringWidth(getTagsString()) + (HINT_PADDING_HORIZONTAL * 2);
         tagsHeight = fontMetrics.getHeight() + (HINT_PADDING_VERTICAL * 2);
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
 
     private int calculatePartPositions(Graphics g) {
         FontMetrics fontMetrics = g.getFontMetrics();
         int x = 0;
         int y = tagsHeight;
         for (CucumberStepPart part : step.getParts()) {
             part.wrapText(fontMetrics, x, y, renderWidth - (getTextPaddingLeft() + getTextPaddingRight()));
             x = part.endX;
             y = part.endY;
         }
         return y + fontMetrics.getHeight();
     }
 
     public void click() {
         for (Button button : buttons) {
             if (button.click()) {
                 return;
             }
         }
         final CucumberStepPart part = getTouchedPart();
         if (part != null && part.type == CucumberStepPart.PartType.ARGUMENT) {
             DropDown.show(
                     (int) animation.moveAnimation.renderX + part.startX + TEXT_PADDING_HORIZONTAL,
                     (int) animation.moveAnimation.renderY + part.startY + TEXT_PADDING_VERTICAL,
                     new DropDown.DropDownCallback() {
                         public void chooseItem(String item) {
                             part.text = item;
                             if (isEditableParameter(part.text)) {
                                 EditBox.showEditPart(part);
                             }
                         }
                     },
                     Arrays.asList(part.validParameters));
         } else {
             foldToggle();
         }
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
        lastBubbledPosition = null;
     }
 
     public void endDrag() {
         if (isButtonTouched()) {
             return;
         }
         super.endDrag();
         dragFadeAnimation();
         if (groupParent == Engine.stepsRoot) {
             throwElementToFeaturesGroup();
         }
         Engine.updateLastAddedElement(this);
     }
 
     public boolean isDragable() {
         return type != TYPE_FEATURE && !animation.moveAnimation.isMoving() && !isParentFolded() && visible;
     }
 
     protected void applyDrag() {
         updateDragPositionHistory();
         Element touchedGroup = Engine.cucumberRoot.findGroup((int) animation.moveAnimation.renderX, (int) animation.moveAnimation.renderY, type);
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
 
     private void throwElementToFeaturesGroup() {
         if (Engine.lastAddedElement == null) {
             return;
         }
         int oldestIndex = (dragHistoryIndex + 1) % DRAG_HISTORY_LENGTH;
         long deltaTime = dragHistoryTime[dragHistoryIndex] - dragHistoryTime[oldestIndex];
         if (deltaTime == 0) {
             return;
         }
         int deltaX = Math.abs(dragHistoryX[dragHistoryIndex] - dragHistoryX[oldestIndex]);
         int deltaY = Math.abs(dragHistoryY[dragHistoryIndex] - dragHistoryY[oldestIndex]);
         float squaredDist = (float) ((deltaX * deltaX) + (deltaY * deltaY));
         if (squaredDist / deltaTime > DRAG_HISTORY_THROW_LIMIT) {
             groupParent.removeChild(this);
             if (type == TYPE_FEATURE) {
                 Engine.featuresRoot.addChild(this);
             } else if (type == TYPE_SCENARIO) {
                 if (Engine.lastAddedElement.type == TYPE_FEATURE) {
                     Engine.featuresRoot.addChild(this);
                 } else if (Engine.lastAddedElement.type == TYPE_SCENARIO) {
                     Engine.lastAddedElement.groupParent.addChild(this);
                 } else {
                     Engine.lastAddedElement.groupParent.groupParent.addChild(this);
                 }
             } else {
                 if (Engine.lastAddedElement.type == TYPE_FEATURE) {
                     return;
                 }
                 if (Engine.lastAddedElement.type == TYPE_SCENARIO) {
                     Engine.lastAddedElement.addChild(this);
                 } else {
                     int index = Engine.lastAddedElement.groupParent.findChildIndex(Engine.lastAddedElement);
                     Engine.lastAddedElement.groupParent.addChild(this, index + 1);
                 }
             }
         }
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
 
     protected void renderBefore(Graphics g) {
         if (!animation.colorAnimation.isVisible()) {
             return;
         }
         if (animation.moveAnimation.renderX > Engine.canvasWidth || animation.moveAnimation.renderY > Engine.canvasHeight ||
             animation.moveAnimation.renderX + renderWidth < 0 || animation.moveAnimation.renderY + renderHeight < 0) {
             return;
         }
         renderElement(g);
     }
 
     protected void renderAfter(Graphics g) {
     }
 
     private void renderElement(Graphics canvas) {
         int index = Math.min(renderHeight / 10, IMAGE_BUFFER_COUNT - 1);
         Graphics2D g = imageGraphics[index];
         if (Engine.fpsDetails != Engine.DETAILS_NONE) {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         } else {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         }
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, animation.colorAnimation.getAlpha()));
         clear(index);
         drawShadow(index);
         drawBar(index);
         drawText(index);
         drawTags(index);
         canvas.drawImage(image[index], (int) animation.moveAnimation.renderX, (int) animation.moveAnimation.renderY, null);
         drawButtons(canvas);
     }
     
     protected void renderHintsInternal(Graphics g) {
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
 
     private String getTagsString() {
         return tags.hasTags() ? tags.toString() : "@";
     }
 
     private void drawButtons(Graphics canvas) {
         for (Button button : buttons) {
             button.render(canvas);
         }
     }
 
     private void clear(int index) {
         Graphics2D g = imageGraphics[index];
         g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
         g.fillRect(0, 0, IMAGE_BUFFER_WIDTH, IMAGE_BUFFER_HEIGHT[index]);
     }
 
     private void drawShadow(int index) {
         if (Engine.fpsDetails == Engine.DETAILS_FEWER) {
             return;
         }
         Graphics2D g = imageGraphics[index];
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.2f));
         g.fillRoundRect(3, 3, renderWidth - 1, renderHeight - 1, BAR_ROUNDING, BAR_ROUNDING);
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.4f));
         g.fillRoundRect(2, 2, renderWidth - 1, renderHeight - 1, BAR_ROUNDING, BAR_ROUNDING);
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.6f));
         g.fillRoundRect(1, 1, renderWidth - 1, renderHeight - 1, BAR_ROUNDING, BAR_ROUNDING);
     }
 
     private void drawBar(int index) {
         Graphics2D g = imageGraphics[index];
 
         g.setColor(getBackgroundColor());
         g.fillRoundRect(0, 0, renderWidth - 1, renderHeight - 1, BAR_ROUNDING, BAR_ROUNDING);
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.8f));
         g.drawRoundRect(0, 0, renderWidth - 1, renderHeight - 1, BAR_ROUNDING, BAR_ROUNDING);
 
         if (!renderPlaying(index) && !renderEditing(index)) {
             renderPlayFailures(index);
         }
     }
 
     private Color getBackgroundColor() {
         int highlightToIndex = isHighlighted() ? 1 : 0;
         if (type == TYPE_STEP && isFailed) {
             return COLOR_BG_FAILED;
         } else if (type != TYPE_STEP || step.matchedByStepDefinition() || groupParent == Engine.stepsRoot) {
             return COLOR_BG_FILL[type][highlightToIndex];
         } else {
             return COLOR_BG_UNRECOGNIZED_STEP[highlightToIndex];
         }
     }
     
     private boolean renderPlaying(int index) {
         if (!Player.isRunning()) {
             return false;
         }
         if (!isRunnable() || !isRunning()) {
             return false;
         }
         renderBorder(index, new Color(0.0f, 0.0f, 0.0f, 0.4f), 1.5f);
         renderAnimatedBorder(index, new Color(1.0f, 1.0f, 1.0f, 0.8f), 1.5f);
         return true;
     }
 
     private boolean renderPlayFailures(int index) {
         if (!isRunnable() || !isFailed) {
             return false;
         }
         renderBorder(index, new Color(1.0f, 0.0f, 0.0f, 0.8f), 1.5f);
         return true;
     }
 
     private boolean renderEditing(int index) {
         if (!EditBox.isEditing(this)) {
             return false;
         }
         renderBorder(index, new Color(1.0f, 1.0f, 0.5f, 0.8f), 1.5f);
         return true;
     }
 
     private void renderBorder(int index, Color color, float width) {
         Graphics2D g = imageGraphics[index];
         Stroke stroke = g.getStroke();
 
         g.setColor(color);
         g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
         g.drawRoundRect(1, 1, renderWidth - 3, renderHeight - 3, BAR_ROUNDING, BAR_ROUNDING);
 
         g.setStroke(stroke);
     }
 
     private void renderAnimatedBorder(int index, Color color, float width) {
         float dashPosition = (PLAY_ANIMATION_DASH_LENGTH * 2.0f) - (float) (System.currentTimeMillis() % (int) (PLAY_ANIMATION_SPEED * PLAY_ANIMATION_DASH_LENGTH * 2.0f)) / PLAY_ANIMATION_SPEED;
 
         Graphics2D g = imageGraphics[index];
         Stroke stroke = g.getStroke();
 
         g.setColor(color);
         g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, PLAY_ANIMATION_DASH_LENGTH, new float[] {PLAY_ANIMATION_DASH_LENGTH}, dashPosition));
         g.drawRoundRect(1, 1, renderWidth - 3, renderHeight - 3, BAR_ROUNDING, BAR_ROUNDING);
 
         g.setStroke(stroke);
     }
 
     private boolean isRunning() {
         return (type == TYPE_FEATURE && Player.isCurrentFeature(this)) ||
                //(type == TYPE_BACKGROUND && CucumberPlayer.currentScenario == null && CucumberPlayer.isCurrentFeature(groupParent)) ||
                (type == TYPE_SCENARIO && Player.isCurrentScenario(this)) ||
                (type == TYPE_STEP && Player.isCurrentStep(this));
     }
     
     private boolean isRunnable() {
         return type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_BACKGROUND || type == TYPE_STEP;
     }
 
     private void drawText(int index) {
         Graphics2D g = imageGraphics[index];
         FontMetrics fontMetrics = g.getFontMetrics();
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
                 g.drawString(text, drawX, drawY + fontMetrics.getHeight());
                 updatePartTouchState(fontMetrics, part, text, (int) animation.moveAnimation.renderX + drawX, (int) animation.moveAnimation.renderY + drawY);
                 x = 0;
                 y += fontMetrics.getHeight();
             }
         }
         g.setFont(Engine.FONT_DEFAULT);
     }
 
     private void drawTags(int index) {
         if (!shouldRenderTags() || (!isHighlighted() && type == TYPE_FEATURE)) {
             return;
         }
         Graphics2D g = imageGraphics[index];
         FontMetrics fontMetrics = g.getFontMetrics();
 
         int width = fontMetrics.stringWidth(tags.toString()) + (HINT_PADDING_HORIZONTAL * 2);
         int height = fontMetrics.getHeight() + (HINT_PADDING_VERTICAL * 2);
 
         int x = (renderWidth - width) / 2;
         int y = TAGS_PADDING_VERTICAL;
 
         g.setColor(Util.blendColor(getBackgroundColor(), COLOR_BG_TAGS));
         g.fillRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
         g.drawRoundRect(x, y, width - 1, height - 1, HINT_ROUNDING, HINT_ROUNDING);
 
         g.setColor(COLOR_TEXT_TAGS);
         g.drawString(tags.toString(), x + HINT_PADDING_HORIZONTAL, y + fontMetrics.getHeight() - 3 + HINT_PADDING_VERTICAL);
     }
 
     private void drawHint(Graphics g, String hint, int x, int y, Color textColor, Color bgColor) {
         drawHint(g, hint, x, y, textColor, bgColor, new Color(0.0f, 0.0f, 0.0f, 0.8f), false, true, true);
     }
     
     private void drawHint(Graphics g, String hint, int x, int y, Color textColor, Color bgColor, Color borderColor, boolean centerAligned, boolean bottomAligned, boolean forceOnScreen) {
         FontMetrics fontMetrics = g.getFontMetrics();
 
         int width = fontMetrics.stringWidth(hint) + (HINT_PADDING_HORIZONTAL * 2);
         int height = fontMetrics.getHeight() + (HINT_PADDING_VERTICAL * 2);
 
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
         g.drawString(hint, x + HINT_PADDING_HORIZONTAL, y + fontMetrics.getHeight() - 3 + HINT_PADDING_VERTICAL);
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
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 1.0f));
 
         g.fillRect(x - 1, y - 1, errorScreenshots[0].getWidth(null) + 2, errorScreenshots[0].getHeight(null) + 2);
         g.drawImage(errorScreenshots[0], x, y, null);
 
         if (errorScreenshots.length < 2 || errorScreenshots[1] == null) {
             return;
         }
         x += errorScreenshots[0].getWidth(null) + 5;
         g.fillRect(x - 1, y - 1, errorScreenshots[1].getWidth(null) + 2, errorScreenshots[1].getHeight(null) + 2);
         g.drawImage(errorScreenshots[1], x, y, null);
     }
 
     private void updatePartTouchState(FontMetrics fontMetrics, CucumberStepPart part, String text, int x, int y) {
         part.isTouched =
                 CumberlessMouseListener.mouseX >= x && CumberlessMouseListener.mouseX <= x + fontMetrics.stringWidth(text) &&
                 CumberlessMouseListener.mouseY >= y && CumberlessMouseListener.mouseY <= y + fontMetrics.getHeight();
     }
 
     private int getTextPaddingLeft() {
         return buttonbarX;
     }
 
     private int getTextPaddingRight() {
         if (hasTrashcanButton()) {
             return TEXT_PADDING_HORIZONTAL + BUTTON_WIDTH;
         } else {
             return TEXT_PADDING_HORIZONTAL;
         }
     }
 
     private boolean isButtonTouched() {
         for (Button button : buttons) {
             if (button.isTouched()) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean hasTrashcanButton() {
         return groupParent != Engine.stepsRoot;
     }
 
     private boolean hasPlayButton() {
         return (type == TYPE_FEATURE || type == TYPE_SCENARIO || type == TYPE_BACKGROUND) && groupParent != Engine.stepsRoot && Engine.isPlayableDeviceEnabled();
     }
 
     private boolean hasEditButton() {
         return (type == TYPE_SCENARIO || type == TYPE_BACKGROUND || type == TYPE_COMMENT || (type == TYPE_STEP && !step.matchedByStepDefinition())) && groupParent != Engine.stepsRoot;
     }
 
     private boolean hasTagsAddButton() {
         return (type == TYPE_FEATURE || type == TYPE_SCENARIO) && groupParent != Engine.stepsRoot;
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
             sb.append(tags.toString()).append("\n");
             if (type == TYPE_FEATURE) {
                 sb.append("\n");
             }
         }
         if (type == TYPE_FEATURE) {
             sb.append("Feature: ").append(convertNewlines(title)).append("\n\n");
         }
         if (type == TYPE_BACKGROUND) {
             sb.append("Background:\n");
         }
         if (type == TYPE_SCENARIO) {
             sb.append("Scenario: ").append(title).append("\n\n");
         }
         if (type == TYPE_STEP) {
             sb.append("\t").append(step.toString()).append("\n");
         }
         if (type == TYPE_COMMENT) {
             sb.append("\t").append("# ").append(title).append("\n");
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
         String relativePath = getRelativePath(filename);
         File file = new File(directory, relativePath);
         File path = new File(Util.getPath(file.getAbsolutePath()));
         if (!path.exists() && !path.mkdirs()) {
             return false;
         }
         return export(file.getAbsolutePath());
     }
 
     private String getRelativePath(String filename) {
         if (!Util.isEmpty(Engine.featuresBaseDir) && filename.startsWith(Engine.featuresBaseDir)) {
             return Util.stripLeadingSlash(filename.substring(Engine.featuresBaseDir.length()));
         }
         return new File(filename).getName();
     }
 
     public boolean export(String filename) {
         if (type != TYPE_FEATURE) {
             return false;
         }
         if (Util.isEmpty(filename)) {
             return false;
         }
         BufferedWriter bufferedWriter = null;
         try {
             bufferedWriter = new BufferedWriter(new FileWriter(new File(filename)));
             bufferedWriter.append(buildFeature().toString());
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         } finally {
             Util.close(bufferedWriter);
         }
         return true;
     }
 
     private static boolean isEditableParameter(String text) {
         return "ResourceKey".equals(text) || "*".equals(text);
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
