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
 
 import junit.framework.TestCase;
 
 /**
  * @author Toshitaka Agata
  */
 public class PagerConditionBaseTest extends TestCase {
 
     DefaultPagerCondition condition;
 
     protected void setUp() throws Exception {
         condition = new DefaultPagerCondition();
     }
 
     public void testFirstPage() {
         setCondtion(0, 10, 95);
        assertCondtion(false, true, 0, 1, 0, 10, 9, 9);
     }
 
     public void testSecondPage() {
         setCondtion(10, 10, 95);
        assertCondtion(true, true, 1, 2, 0, 20, 19, 9);
     }
 
     public void testLastPage() {
         setCondtion(90, 10, 95);
        assertCondtion(true, false, 9, 10, 80, 100, 94, 9);
     }
 
     public void testEmptyResult() {
         setCondtion(0, 10, 0);
         assertCondtion(false, false, 0, 1, 0, 10, 0, 0);
     }
 
     public void test9Result() {
         setCondtion(0, 10, 9);
        assertCondtion(false, false, 0, 1, 0, 10, 8, 0);
     }
 
     public void test10Result() {
         setCondtion(0, 10, 10);
        assertCondtion(false, false, 0, 1, 0, 10, 9, 0);
     }
 
     private void assertCondtion(boolean isPrev, boolean isNext, int pageIndex,
             int pageCount, int prevOffset, int nextOffset,
             int currentLastOffset, int lastPageIndex) {
         PagerViewHelper helper = new PagerViewHelper(condition);
         assertEquals(isPrev, helper.isPrev());
         assertEquals(isNext, helper.isNext());
         assertEquals(pageIndex, helper.getPageIndex());
         assertEquals(pageCount, helper.getPageCount());
         assertEquals(prevOffset, helper.getPrevOffset());
         assertEquals(nextOffset, helper.getNextOffset());
         assertEquals(currentLastOffset, helper.getCurrentLastOffset());
         assertEquals(lastPageIndex, helper.getLastPageIndex());
     }
 
     private void setCondtion(int offset, int limit, int count) {
         condition.setOffset(offset);
         condition.setLimit(limit);
         condition.setCount(count);
     }
 }
