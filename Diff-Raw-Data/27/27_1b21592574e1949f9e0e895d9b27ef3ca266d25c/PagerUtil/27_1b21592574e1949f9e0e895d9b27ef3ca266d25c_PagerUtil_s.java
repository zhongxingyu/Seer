 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.pager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * y[W[eBeB
  * 
  * @author Toshitaka Agata(Nulab,inc.)
  */
 public class PagerUtil {
 
     public static boolean isPrev(PagerCondition condition) {
         boolean prev = condition.getOffset() > 0;
         return prev;
     }
 
     public static boolean isNext(PagerCondition condition) {
         boolean next = condition.getCount() > 0
                 && condition.getOffset() + condition.getLimit() < condition
                         .getCount();
         return next;
     }
 
     public static int getCurrentLastOffset(PagerCondition condition) {
         int nextOffset = getNextOffset(condition);
        return nextOffset < condition.getCount() ? nextOffset - 1: condition
                .getCount() - 1;
     }
 
     public static int getNextOffset(PagerCondition condition) {
         return condition.getOffset() + condition.getLimit();
     }
 
     public static int getPrevOffset(PagerCondition condition) {
         int prevOffset = condition.getOffset() - condition.getLimit();
         return prevOffset < 0 ? 0 : prevOffset;
     }
 
     public static int getPageIndex(PagerCondition condition) {
         if (condition.getLimit() == 0) {
             return 1;
         } else {
             return condition.getOffset() / condition.getLimit();
         }
     }
 
     public static int getPageCount(PagerCondition condition) {
         return getPageIndex(condition) + 1;
     }
 
     public static int getLastPageIndex(PagerCondition condition) {
         if (condition.getLimit() == 0) {
             return 0;
         } else {
             return (condition.getCount() - 1) / condition.getLimit();
         }
     }
 
     public static int getDisplayPageIndexBegin(PagerCondition condition,
             int displayPageMax) {
         int lastPageIndex = getLastPageIndex(condition);
         if (lastPageIndex < displayPageMax) {
             return 0;
         } else {
             int currentPageIndex = getPageIndex(condition);
             int displayPageIndexBegin = currentPageIndex
                     - ((int) Math.floor(displayPageMax / 2));
             return displayPageIndexBegin < 0 ? 0 : displayPageIndexBegin;
         }
     }
 
     public static int getDisplayPageIndexEnd(PagerCondition condition,
             int displayPageMax) {
         int lastPageIndex = getLastPageIndex(condition);
         int displayPageIndexBegin = getDisplayPageIndexBegin(condition,
                 displayPageMax);
         int displayPageRange = lastPageIndex - displayPageIndexBegin;
         if (displayPageRange < displayPageMax) {
             return lastPageIndex;
         } else {
             return displayPageIndexBegin + displayPageMax - 1;
         }
     }
 
     /**
      * List̓ePagerCondition̏ŃtB^O܂B
      * @param list List
      * @param condition 
      * @return tB^OList
      */
     public static List filter(List list, PagerCondition condition) {
         condition.setCount(list.size());
         if (condition.getLimit() == PagerCondition.NONE_LIMIT) {
             return list;
         } else {
             List result = new ArrayList();
             for (int i = 0; i < list.size(); i++) {
                 if (i >= condition.getOffset()
                         && i < condition.getOffset() + condition.getLimit()) {
                     result.add(list.get(i));
                 }
             }
             return result;
         }
     }
 }
