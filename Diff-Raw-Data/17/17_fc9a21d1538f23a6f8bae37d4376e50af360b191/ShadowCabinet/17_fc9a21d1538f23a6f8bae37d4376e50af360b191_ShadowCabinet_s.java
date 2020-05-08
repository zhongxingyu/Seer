 package com.thoughtworks.shadow;
 
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 
 import java.util.*;
 
 public class ShadowCabinet implements Cabinet {
     private final List listeners = new ArrayList();
     private final LinkedList shadows = new LinkedList();
     private int maxSize = 5;
 
     public void add(ComparableTestShadow testShadow) {
         if (maxSize == 0) {
             return;
         }
        removeMaxPassTimesTests(maxSize - 1);
         addIntoSet(testShadow);
     }
 
     public void addListener(ShadowCabinetListener listener) {
         listeners.add(listener);
     }
 
     public void removeListener(ShadowCabinetListener listener) {
         listeners.remove(listener);
     }
 
     public void action(TestResult result) {
         fireStartActionEvent();
         TestSuite suite = new TestSuite();
         for (Iterator iter = shadows.iterator(); iter.hasNext();) {
             suite.addTest((ComparableTestShadow) iter.next());
         }
         suite.run(result);
         fireEndActionEvent();
     }
 
     public int maxSize() {
         return maxSize;
     }
 
     public int size() {
         return shadows.size();
     }
 
     public void setMaxSize(int maxSize) {
         this.maxSize = maxSize;
         removeMaxPassTimesTests(maxSize);
     }
 
     private void removeMaxPassTimesTests(int maxSize) {
         Collections.sort(shadows);
         while (size() > maxSize) {
             fireAfterRemoveTestEvent((ComparableTestShadow) shadows.removeLast());
         }
     }
 
     public void remove(ComparableTestShadow test) {
         if (shadows.remove(test)) {
             fireAfterRemoveTestEvent(test);
         }
     }
 
     private void addIntoSet(ComparableTestShadow testShadow) {
         if (shadows.contains(testShadow)) {
             return;
         }
         testShadow.setContainer(this);
         shadows.add(testShadow);
         fireAfterAddTestEvent(testShadow);
     }
 
     private void fireAfterAddTestEvent(ComparableTestShadow test) {
         for (int i = 0; i < listeners.size(); i++) {
             ((ShadowCabinetListener) listeners.get(i)).afterAddTest(test);
         }
     }
 
     private void fireAfterRemoveTestEvent(ComparableTestShadow test) {
         for (int i = 0; i < listeners.size(); i++) {
             ((ShadowCabinetListener) listeners.get(i)).afterRemoveTest(test);
         }
     }
 
     private void fireEndActionEvent() {
         for (int i = 0; i < listeners.size(); i++) {
             ((ShadowCabinetListener) listeners.get(i)).endAction();
         }
     }
 
     private void fireStartActionEvent() {
         for (int i = 0; i < listeners.size(); i++) {
             ((ShadowCabinetListener) listeners.get(i)).startAction();
         }
     }
 }
