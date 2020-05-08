 /*******************************************************************************
  * 2011 Ivan Shubin http://mindengine.net
  * 
  * This file is part of MindEngine.net Oculus Grid.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Oculus Experior.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package net.mindengine.oculus.grid.domain.task;
 
 import java.util.List;
 
 import net.mindengine.oculus.experior.test.descriptors.TestInformation;
 
 public class SuiteInformation {
     
     private List<TestStatus> tests;
     /**
      * Id of suite in Database
      */
     private Long suiteId;
     
     public SuiteStatistic calculateStatistics() {
         SuiteStatistic statistic = new SuiteStatistic();
         if(tests!=null) {
             statistic.setTotal(tests.size());
             for(TestStatus test : tests) {
                 if(test.getPhase()==TestInformation.PHASE_DONE) {
                     
                     if(test.getStatus()==TestInformation.STATUS_FAILED) {
                         statistic.setFailed(statistic.getFailed()+1);
                     }
                     else if(test.getStatus()==TestInformation.STATUS_PASSED) {
                         statistic.setPassed(statistic.getPassed()+1);
                     }
                     else if(test.getStatus()==TestInformation.STATUS_WARNING) {
                         statistic.setWarning(statistic.getWarning()+1);
                     }
                     else if(test.getStatus()==TestInformation.STATUS_POSTPONED) {
                         statistic.setPostponed(statistic.getPostponed()+1);
                     }
                 }
             }
         }
         return statistic;
     }
     
     public void changeTestStatus(Long customId, TestStatus testStatus) {
         int i=0;
         for(TestStatus ts : tests) {
             if(customId == ts.getCustomId()) {
                 tests.set(i, testStatus);
                 return;
             }
             i++;
         }
     }
 
     public void setTests(List<TestStatus> tests) {
         this.tests = tests;
     }
 
     public List<TestStatus> getTests() {
         return tests;
     }
 
     public void setSuiteId(Long suiteId) {
         this.suiteId = suiteId;
     }
 
     public Long getSuiteId() {
         return suiteId;
     }
 
 }
