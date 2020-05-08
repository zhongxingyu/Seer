 /*
  * @(#)AuthoriseParticipantActivity.java
  *
  * Copyright 2011 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.mission.domain.activity;
 
 import module.mission.domain.Mission;
 import module.mission.domain.MissionProcess;
 import module.mission.domain.PersonMissionAuthorization;
 import module.workflow.activities.ActivityInformation;
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.util.BundleUtil;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public class AuthoriseParticipantActivity extends MissionProcessActivity<MissionProcess, AuthoriseParticipantActivityInformation> {
 
     @Override
     public String getLocalizedName() {
         return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(MissionProcess missionProcess, User user) {
        return super.isActive(missionProcess, user)
                && !missionProcess.getIsCanceled()
                && missionProcess.isApproved()
                && missionProcess.canAuthoriseParticipantActivity()
                && (!missionProcess.getMission().hasAnyFinancer() || (missionProcess.hasAllAllocatedFunds() && missionProcess
                        .hasAllCommitmentNumbers()));
     }
 
     @Override
     protected void process(AuthoriseParticipantActivityInformation authoriseParticipantActivityInformation) {
         PersonMissionAuthorization personMissionAuthorization =
                 authoriseParticipantActivityInformation.getPersonMissionAuthorization();
         personMissionAuthorization.setAuthority(UserView.getCurrentUser().getPerson());
 
         MissionProcess missionProcess = authoriseParticipantActivityInformation.getProcess();
         Mission mission = missionProcess.getMission();
         if (mission.allParticipantsAreAuthorized()) {
             missionProcess.notifyAllParticipants();
             if (!mission.hasAnyMissionItems()) {
                 missionProcess.addToProcessParticipantInformationQueues();
             }
         }
     }
 
     @Override
     public ActivityInformation<MissionProcess> getActivityInformation(MissionProcess process) {
         return new AuthoriseParticipantActivityInformation(process, this);
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
         return true;
     }
 
     @Override
     public boolean isVisible() {
         return false;
     }
 
 }
