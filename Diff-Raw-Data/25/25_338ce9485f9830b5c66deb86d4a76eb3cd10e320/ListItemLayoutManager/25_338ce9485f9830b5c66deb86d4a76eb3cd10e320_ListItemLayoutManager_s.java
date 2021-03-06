 /*
  * Copyright 1999-2005 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /* $Id$ */
 
 package org.apache.fop.layoutmgr.list;
 
 import org.apache.fop.fo.flow.ListItem;
 import org.apache.fop.fo.flow.ListItemBody;
 import org.apache.fop.fo.flow.ListItemLabel;
 import org.apache.fop.layoutmgr.BlockLevelLayoutManager;
 import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
 import org.apache.fop.layoutmgr.LayoutManager;
 import org.apache.fop.layoutmgr.LayoutContext;
 import org.apache.fop.layoutmgr.PositionIterator;
 import org.apache.fop.layoutmgr.Position;
 import org.apache.fop.layoutmgr.NonLeafPosition;
 import org.apache.fop.layoutmgr.TraitSetter;
 import org.apache.fop.layoutmgr.KnuthElement;
 import org.apache.fop.layoutmgr.KnuthBox;
 import org.apache.fop.layoutmgr.KnuthPenalty;
 import org.apache.fop.layoutmgr.KnuthPossPosIter;
 import org.apache.fop.area.Area;
 import org.apache.fop.area.Block;
 import org.apache.fop.traits.MinOptMax;
 import org.apache.fop.traits.SpaceVal;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 /**
  * LayoutManager for a list-item FO.
  * The list item contains a list item label and a list item body.
  */
 public class ListItemLayoutManager extends BlockStackingLayoutManager {
     private Item label;
     private Item body;
 
     private Block curBlockArea = null;
 
     private LinkedList labelList = null;
     private LinkedList bodyList = null;
 
     private int listItemHeight;
 
     //TODO space-before|after: handle space-resolution rules
     private MinOptMax spaceBefore;
     private MinOptMax spaceAfter;
     
     private boolean keepWithNextPendingOnLabel;
     private boolean keepWithNextPendingOnBody;
     
     /*
     private class ItemPosition extends LeafPosition {
         protected List cellBreaks;
         protected ItemPosition(LayoutManager lm, int pos, List l) {
             super(lm, pos);
             cellBreaks = l;
         }
     }*/
 
     private class ListItemPosition extends Position {
         private int iLabelFirstIndex;
         private int iLabelLastIndex;
         private int iBodyFirstIndex;
         private int iBodyLastIndex;
 
         public ListItemPosition(LayoutManager lm, int labelFirst, int labelLast, 
                 int bodyFirst, int bodyLast) {
             super(lm);
             iLabelFirstIndex = labelFirst;
             iLabelLastIndex = labelLast;
             iBodyFirstIndex = bodyFirst;
             iBodyLastIndex = bodyLast;
         }
         
         public int getLabelFirstIndex() {
             return iLabelFirstIndex;
         }
         
         public int getLabelLastIndex() {
             return iLabelLastIndex;
         }
 
         public int getBodyFirstIndex() {
             return iBodyFirstIndex;
         }
         
         public int getBodyLastIndex() {
             return iBodyLastIndex;
         }
     }
 
     /**
      * Create a new list item layout manager.
      * @param node list-item to create the layout manager for
      */
     public ListItemLayoutManager(ListItem node) {
         super(node);
         setLabel(node.getLabel());
         setBody(node.getBody());
     }
 
     /**
      * Convenience method.
      * @return the ListBlock node
      */
     protected ListItem getListItemFO() {
         return (ListItem)fobj;
     }
 
     /**
      * Create a LM for the fo:list-item-label object
      * @param node the fo:list-item-label FO
      */
     public void setLabel(ListItemLabel node) {
         label = new Item(node);
         label.setParent(this);
     }
 
     /**
      * Create a LM for the fo:list-item-body object
      * @param node the fo:list-item-body FO
      */
     public void setBody(ListItemBody node) {
         body = new Item(node); 
         body.setParent(this);
     }
 
     /** @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties() */
     protected void initProperties() {
         super.initProperties();
         spaceBefore = new SpaceVal(getListItemFO().getCommonMarginBlock().spaceBefore).getSpace();
         spaceAfter = new SpaceVal(getListItemFO().getCommonMarginBlock().spaceAfter).getSpace();
     }
 
     private int getIPIndents() {
         int iIndents = 0;
         iIndents += getListItemFO().getCommonMarginBlock().startIndent.getValue();
         iIndents += getListItemFO().getCommonMarginBlock().endIndent.getValue();
         return iIndents;
     }
     
     /** @see org.apache.fop.layoutmgr.LayoutManager */
     public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
         referenceIPD = context.getRefIPD();
         LayoutContext childLC;
         
         // label
         childLC = new LayoutContext(0);
         childLC.setRefIPD(context.getRefIPD());
         labelList = label.getNextKnuthElements(childLC, alignment);
         if (childLC.isKeepWithPreviousPending()) {
             context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
         }
         this.keepWithNextPendingOnLabel = childLC.isKeepWithNextPending();
 
         // body
         childLC = new LayoutContext(0);
         childLC.setRefIPD(context.getRefIPD());
         bodyList = body.getNextKnuthElements(childLC, alignment);
         if (childLC.isKeepWithPreviousPending()) {
             context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
         }
         this.keepWithNextPendingOnBody = childLC.isKeepWithNextPending();
 
         // create a combined list
         LinkedList returnedList = getCombinedKnuthElementsForListItem(labelList, bodyList);
 
         // "wrap" the Position inside each element
         LinkedList tempList = returnedList;
         KnuthElement tempElement;
         returnedList = new LinkedList();
         ListIterator listIter = tempList.listIterator();
         while (listIter.hasNext()) {
             tempElement = (KnuthElement)listIter.next();
             tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
             returnedList.add(tempElement);
         }
         
         if (keepWithNextPendingOnLabel || keepWithNextPendingOnBody || mustKeepWithNext()) {
             context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
         }
         if (mustKeepWithPrevious()) {
             context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
         }
 
         setFinished(true);
         return returnedList;
     }
 
     private LinkedList getCombinedKnuthElementsForListItem(LinkedList labelElements,
                                                            LinkedList bodyElements) {
         //Copy elements to array lists to improve element access performance
         List[] elementLists = {new ArrayList(labelElements),
                                new ArrayList(bodyElements)};
         int[] fullHeights = {calcItemHeightFromContents(elementLists[0]),
                             calcItemHeightFromContents(elementLists[1])};
         int[] partialHeights = {0, 0};
         int[] start = {-1, -1};
         int[] end = {-1, -1};
 
         int totalHeight = Math.max(fullHeights[0], fullHeights[1]);
         int step;
         int addedBoxHeight = 0;
         boolean keepWithNextActive = false;
 
         LinkedList returnList = new LinkedList();
         while ((step = getNextStep(elementLists, start, end, partialHeights))
                > 0) {
             
             if (end[0] + 1 == elementLists[0].size()) {
                 if (keepWithNextPendingOnLabel) {
                     keepWithNextActive = true;
                 }
             }
             if (end[1] + 1 == elementLists[1].size()) {
                 if (keepWithNextPendingOnBody) {
                     keepWithNextActive = true;
                 }
             }
             
             // compute penalty height and box height
             int penaltyHeight = step 
                 + getMaxRemainingHeight(fullHeights, partialHeights) 
                 - totalHeight;
             int boxHeight = step - addedBoxHeight - penaltyHeight;
 
             // add the new elements
             addedBoxHeight += boxHeight;
             ListItemPosition stepPosition = new ListItemPosition(this, 
                     start[0], end[0], start[1], end[1]);
             returnList.add(new KnuthBox(boxHeight, stepPosition, false));
             if (addedBoxHeight < totalHeight) {
                 int p = 0;
                 if (keepWithNextActive || mustKeepTogether()) {
                     p = KnuthPenalty.INFINITE;
                 }
                 returnList.add(new KnuthPenalty(penaltyHeight, p, false, stepPosition, false));
             }
         }
 
         return returnList;
     }
 
     private int calcItemHeightFromContents(List elements, int start, int end) {
         ListIterator iter = elements.listIterator(start);
         int count = end - start + 1;
         int len = 0;
         while (iter.hasNext()) {
             KnuthElement el = (KnuthElement)iter.next();
             if (el.isBox()) {
                 len += el.getW();
             } else if (el.isGlue()) {
                 len += el.getW();
             } else {
                 log.debug("Ignoring penalty: " + el);
                 //ignore penalties
             }
             count--;
             if (count == 0) {
                 break;
             }
         }
         return len;
     }
     
     private int calcItemHeightFromContents(List elements) {
         return calcItemHeightFromContents(elements, 0, elements.size() - 1);
     }
 
     private int getNextStep(List[] elementLists, int[] start, int[] end, int[] partialHeights) {
         // backup of partial heights
         int[] backupHeights = {partialHeights[0], partialHeights[1]};
 
         // set starting points
         start[0] = end[0] + 1;
         start[1] = end[1] + 1;
 
         // get next possible sequence for label and body
         int seqCount = 0;
         for (int i = 0; i < start.length; i++) {
             while (end[i] + 1 < elementLists[i].size()) {
                 end[i]++;
                 KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                 if (el.isPenalty()) {
                     if (el.getP() < KnuthElement.INFINITE) {
                         //First legal break point
                         break;
                     }
                 } else if (el.isGlue()) {
                    KnuthElement prev = (KnuthElement)elementLists[i].get(end[i] - 1);
                    if (prev.isBox()) {
                        //Second legal break point
                        break;
                     }
                     partialHeights[i] += el.getW();
                 } else {
                     partialHeights[i] += el.getW();
                 }
             }
             if (end[i] < start[i]) {
                 partialHeights[i] = backupHeights[i];
             } else {
                 seqCount++;
             }
         }
         if (seqCount == 0) {
             return 0;
         }
         
         // determine next step
         int step;
         if (backupHeights[0] == 0 && backupHeights[1] == 0) {
             // this is the first step: choose the maximum increase, so that
             // the smallest area in the first page will contain at least
             // a label area and a body area
             step = Math.max((end[0] >= start[0] ? partialHeights[0] : Integer.MIN_VALUE),
                             (end[1] >= start[1] ? partialHeights[1] : Integer.MIN_VALUE));
         } else {
             // this is not the first step: choose the minimum increase
             step = Math.min((end[0] >= start[0] ? partialHeights[0] : Integer.MAX_VALUE),
                             (end[1] >= start[1] ? partialHeights[1] : Integer.MAX_VALUE));
         }
 
         // reset bigger-than-step sequences
         for (int i = 0; i < partialHeights.length; i++) {
             if (partialHeights[i] > step) {
                 partialHeights[i] = backupHeights[i];
                 end[i] = start[i] - 1;
             }
         }
 
         return step;
     }
 
     private int getMaxRemainingHeight(int[] fullHeights, int[] partialHeights) {
         return Math.max(fullHeights[0] - partialHeights[0],
                         fullHeights[1] - partialHeights[1]);
     }
 
     /**
      * @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(java.util.List, int)
      */
     public LinkedList getChangedKnuthElements(List oldList, int alignment) {
         //log.debug(" LILM.getChanged> label");
         // label
         labelList = label.getChangedKnuthElements(labelList, alignment);
 
         //log.debug(" LILM.getChanged> body");
         // body
         // "unwrap" the Positions stored in the elements
         ListIterator oldListIterator = oldList.listIterator();
         KnuthElement oldElement = null;
         while (oldListIterator.hasNext()) {
             oldElement = (KnuthElement)oldListIterator.next();
             Position innerPosition = ((NonLeafPosition) oldElement.getPosition()).getPosition();
             //System.out.println(" BLM> unwrapping: " + (oldElement.isBox() 
             //  ? "box    " : (oldElement.isGlue() ? "glue   " : "penalty")) 
             //  + " creato da " + oldElement.getLayoutManager().getClass().getName());
             //System.out.println(" BLM> unwrapping:         " 
             //  + oldElement.getPosition().getClass().getName());
             if (innerPosition != null) {
                 // oldElement was created by a descendant of this BlockLM
                 oldElement.setPosition(innerPosition);
             } else {
                 // thisElement was created by this BlockLM
                 // modify its position in order to recognize it was not created
                 // by a child
                 oldElement.setPosition(new Position(this));
             }
         }
 
         LinkedList returnedList = body.getChangedKnuthElements(oldList, alignment);
         // "wrap" the Position inside each element
         LinkedList tempList = returnedList;
         KnuthElement tempElement;
         returnedList = new LinkedList();
         ListIterator listIter = tempList.listIterator();
         while (listIter.hasNext()) {
             tempElement = (KnuthElement)listIter.next();
             tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
             returnedList.add(tempElement);
         }
 
         return returnedList;
     }
 
     /**
      * Add the areas for the break points.
      * This sets the offset of each cell as it is added.
      *
      * @param parentIter the position iterator
      * @param layoutContext the layout context for adding areas
      */
     public void addAreas(PositionIterator parentIter,
                          LayoutContext layoutContext) {
         getParentArea(null);
 
         // if adjusted space before
         double adjust = layoutContext.getSpaceAdjust();
         addBlockSpacing(adjust, spaceBefore);
         spaceBefore = null;
 
         getPSLM().addIDToPage(getListItemFO().getId());
 
         LayoutContext lc = new LayoutContext(0);
 
         // "unwrap" the NonLeafPositions stored in parentIter
         LinkedList positionList = new LinkedList();
         Position pos;
         while (parentIter.hasNext()) {
             pos = (Position) parentIter.next();
             if (pos instanceof NonLeafPosition) {
                 // pos contains a ListItemPosition created by this ListBlockLM
                 positionList.add(((NonLeafPosition) pos).getPosition());
             }
         }
 
         // use the first and the last ListItemPosition to determine the 
         // corresponding indexes in the original labelList and bodyList
         int labelFirstIndex = ((ListItemPosition) positionList.getFirst()).getLabelFirstIndex();
         int labelLastIndex = ((ListItemPosition) positionList.getLast()).getLabelLastIndex();
         int bodyFirstIndex = ((ListItemPosition) positionList.getFirst()).getBodyFirstIndex();
         int bodyLastIndex = ((ListItemPosition) positionList.getLast()).getBodyLastIndex();
 
         // add label areas
         if (labelFirstIndex <= labelLastIndex) {
             KnuthPossPosIter labelIter = new KnuthPossPosIter(labelList, 
                     labelFirstIndex, labelLastIndex + 1);
             lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
             lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
             // TO DO: use the right stack limit for the label
             lc.setStackLimit(layoutContext.getStackLimit());
             label.addAreas(labelIter, lc);
         }
 
         // reset the area bpd after adding the label areas and before adding the body areas
         int savedBPD = 0;
         if (labelFirstIndex <= labelLastIndex
             && bodyFirstIndex <= bodyLastIndex) {
             savedBPD = curBlockArea.getBPD();
             curBlockArea.setBPD(0);
         }
 
         // add body areas
         if (bodyFirstIndex <= bodyLastIndex) {
             KnuthPossPosIter bodyIter = new KnuthPossPosIter(bodyList, 
                     bodyFirstIndex, bodyLastIndex + 1);
             lc.setFlags(LayoutContext.FIRST_AREA, layoutContext.isFirstArea());
             lc.setFlags(LayoutContext.LAST_AREA, layoutContext.isLastArea());
             // TO DO: use the right stack limit for the body
             lc.setStackLimit(layoutContext.getStackLimit());
             body.addAreas(bodyIter, lc);
         }
 
         // after adding body areas, set the maximum area bpd
         if (curBlockArea.getBPD() < savedBPD) {
             curBlockArea.setBPD(savedBPD);
         }
 
         flush();
 
         // if adjusted space after
         addBlockSpacing(adjust, spaceAfter);
         
         curBlockArea = null;
     }
 
     /**
      * Get the height of the list item after adjusting.
      * Should only be called after adding the list item areas.
      *
      * @return the height of this list item after adjustment
      */
     public int getListItemHeight() {
         return listItemHeight;
     }
 
     /**
      * Return an Area which can contain the passed childArea. The childArea
      * may not yet have any content, but it has essential traits set.
      * In general, if the LayoutManager already has an Area it simply returns
      * it. Otherwise, it makes a new Area of the appropriate class.
      * It gets a parent area for its area by calling its parent LM.
      * Finally, based on the dimensions of the parent area, it initializes
      * its own area. This includes setting the content IPD and the maximum
      * BPD.
      *
      * @param childArea the child area
      * @return the parent are for the child
      */
     public Area getParentArea(Area childArea) {
         if (curBlockArea == null) {
             curBlockArea = new Block();
 
             // Set up dimensions
             /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);
             
             // set traits
             TraitSetter.addBorders(curBlockArea, 
                     getListItemFO().getCommonBorderPaddingBackground());
             TraitSetter.addBackground(curBlockArea, 
                     getListItemFO().getCommonBorderPaddingBackground());
             TraitSetter.addMargins(curBlockArea,
                     getListItemFO().getCommonBorderPaddingBackground(), 
                     getListItemFO().getCommonMarginBlock());
             TraitSetter.addBreaks(curBlockArea, 
                     getListItemFO().getBreakBefore(), 
                     getListItemFO().getBreakAfter());
             
             int contentIPD = referenceIPD - getIPIndents();
             curBlockArea.setIPD(contentIPD);
 
             setCurrentArea(curBlockArea);
         }
         return curBlockArea;
     }
 
     /**
      * Add the child.
      * Rows return the areas returned by the child elements.
      * This simply adds the area to the parent layout manager.
      *
      * @param childArea the child area
      */
     public void addChildArea(Area childArea) {
         if (curBlockArea != null) {
             curBlockArea.addBlock((Block) childArea);
         }
     }
 
     /**
      * Reset the position of this layout manager.
      *
      * @param resetPos the position to reset to
      */
     public void resetPosition(Position resetPos) {
         if (resetPos == null) {
             reset(null);
         }
     }
     
     /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether() */
     public boolean mustKeepTogether() {
         //TODO Keeps will have to be more sophisticated sooner or later
         return ((BlockLevelLayoutManager)getParent()).mustKeepTogether() 
                 || !getListItemFO().getKeepTogether().getWithinPage().isAuto()
                 || !getListItemFO().getKeepTogether().getWithinColumn().isAuto();
     }
 
     /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious() */
     public boolean mustKeepWithPrevious() {
         return !getListItemFO().getKeepWithPrevious().getWithinPage().isAuto()
             || !getListItemFO().getKeepWithPrevious().getWithinColumn().isAuto();
     }
 
     /** @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext() */
     public boolean mustKeepWithNext() {
         return !getListItemFO().getKeepWithNext().getWithinPage().isAuto()
                 || !getListItemFO().getKeepWithNext().getWithinColumn().isAuto();
     }
 
 }
 
