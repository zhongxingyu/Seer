 package com.technophobia.substeps.model.structure;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DefaultSubstepsTestParentElement extends AbstractSubstepsTestElement implements SubstepsTestParentElement {
 
     private final List<SubstepsTestElement> children;
     private Status childrenStatus;
     private final int testCount;
 
 
     public DefaultSubstepsTestParentElement(final SubstepsTestParentElement parent, final String id,
             final String testName, final int testCount) {
         super(parent, id, testName);
         this.testCount = testCount;
         this.children = new ArrayList<SubstepsTestElement>(testCount);
         this.childrenStatus = Status.NOT_RUN;
     }
 
 
     @Override
     public Result getTestResult(final boolean includeChildren) {
         if (includeChildren) {
             return getStatus().asResult();
         }
         return super.getStatus().asResult();
     }
 
 
     @Override
     public int getChildCount() {
         return testCount;
     }
 
 
     @Override
     public SubstepsTestElement[] getChildren() {
         return children.toArray(new SubstepsTestElement[children.size()]);
     }
 
 
     @Override
     public void addChild(final SubstepsTestElement child) {
         children.add(child);
     }
 
 
     @Override
     public Status getStatus() {
         final Status suiteStatus = getSuiteStatus();
         if (!childrenStatus.equals(Status.NOT_RUN)) {
             return Status.combineStatus(childrenStatus, suiteStatus);
         }
         return suiteStatus;
     }
 
 
     @Override
     public void childChangedStatus(final SubstepsTestElement child, final Status childStatus) {
         if (isFirstChild(child) && childStatus.isRunning()) {
             // is 1st child, and is running, so copy status
             updateTimeAndChildrenStatus(childStatus);
             return;
         }
 
        final SubstepsTestElement lastChild = children.get(getChildCount() - 1);
         if (child == lastChild) {
             if (childStatus.isComplete()) {
                 // all children done, collect cumulative status
                 updateTimeAndChildrenStatus(getCumulatedStatus());
                 return;
             }
         } else if (!lastChild.getStatus().isNotRun()) {
             // child is not last, but last child has been run - child has been
             // rerun or is rerunning
             updateTimeAndChildrenStatus(getCumulatedStatus());
             return;
         }
 
         // finally, set RUNNING_FAILURE/ERROR if child has failed but suite has
         // not failed
         if (childStatus.isFailure()) {
             if (!childrenStatus.isErrorOrFailure()) {
                 updateTimeAndChildrenStatus(Status.RUNNING_FAILURE);
                 return;
             }
         } else if (childStatus.isError()) {
             if (!childrenStatus.isError()) {
                 updateTimeAndChildrenStatus(Status.RUNNING_ERROR);
                 return;
             }
         }
     }
 
 
     private boolean isFirstChild(final SubstepsTestElement child) {
         return !children.isEmpty() && children.get(0) == child;
     }
 
 
     private void updateTimeAndChildrenStatus(final Status status) {
         if (childrenStatus.equals(status)) {
             return;
         }
 
         if (status.equals(Status.RUNNING)) {
             if (time >= 0.0d) {
                 // re-running child - ignore
             } else {
                 time = -System.currentTimeMillis() / 1000.0d;
             }
         } else if (status.isComplete()) {
             if (time < 0) {
                 final double endTime = System.currentTimeMillis() / 1000.0d;
                 time = endTime + time;
             }
         }
 
         this.childrenStatus = status;
         final SubstepsTestParentElement parent = getParent();
         if (parent != null) {
             parent.childChangedStatus(this, getStatus());
         }
     }
 
 
     private Status getCumulatedStatus() {
         // copy list to avoid concurrency problems
         final SubstepsTestElement[] children = this.children.toArray(new SubstepsTestElement[this.children.size()]);
         if (children.length == 0)
             return getSuiteStatus();
 
         Status cumulated = children[0].getStatus();
 
         for (int i = 1; i < children.length; i++) {
             final Status childStatus = children[i].getStatus();
             cumulated = Status.combineStatus(cumulated, childStatus);
         }
         // not necessary, see special code in Status.combineProgress()
         // if (suiteStatus.isErrorOrFailure() && cumulated.isNotRun())
         // return suiteStatus; //progress is Done if error in Suite and no
         // children run
         return cumulated;
     }
 
 
     private Status getSuiteStatus() {
         return super.getStatus();
     }
 }
