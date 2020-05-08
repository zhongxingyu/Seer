 /*
  * @(#)DefineParticipantAuthorizationChainActivityInformation.java
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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import module.mission.domain.Mission;
 import module.mission.domain.MissionFinancer;
 import module.mission.domain.MissionProcess;
 import module.mission.domain.util.AuthorizationChain;
 import module.mission.domain.util.ParticipantAuthorizationChain;
 import module.organization.domain.Person;
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public class DefineParticipantAuthorizationChainActivityInformation extends ParticipantActivityInformation {
 
     private AuthorizationChain authorizationChain;
 
     public DefineParticipantAuthorizationChainActivityInformation(final MissionProcess missionProcess,
             final WorkflowActivity<MissionProcess, ? extends ActivityInformation<MissionProcess>> activity) {
         super(missionProcess, activity);
     }
 
     @Override
     public boolean hasAllneededInfo() {
         return super.hasAllneededInfo() && false;
     }
 
     public AuthorizationChain getAuthorizationChain() {
         return authorizationChain;
     }
 
     public void setAuthorizationChain(AuthorizationChain authorizationChain) {
         this.authorizationChain = authorizationChain;
     }
 
     public SortedMap<Person, Collection<ParticipantAuthorizationChain>> getPossibleParticipantAuthorizationChains() {
         final Person selected = getPerson();
         final SortedMap<Person, Collection<ParticipantAuthorizationChain>> participantAuthorizationChainss =
                 new TreeMap<Person, Collection<ParticipantAuthorizationChain>>(Person.COMPARATOR_BY_NAME);
         for (final Person person : getProcess().getMission().getParticipantesSet()) {
             if (selected == null || selected == person) {
                 final Collection<ParticipantAuthorizationChain> participantAuthorizationChain =
                         ParticipantAuthorizationChain.getParticipantAuthorizationChains(person);
                 if (participantAuthorizationChain.isEmpty()) {
                     // this case if for students and other ad-hoc cases.
                     final MissionProcess missionProcess = getProcess();
                     final Mission mission = missionProcess.getMission();
                     final Collection<ParticipantAuthorizationChain> temp = new ArrayList<ParticipantAuthorizationChain>();
                     for (final MissionFinancer missionFinancer : mission.getFinancerSet()) {
                         final Unit unit = getFirstValidUnit(missionFinancer.getUnit(), person);
                         temp.addAll(ParticipantAuthorizationChain.getParticipantAuthorizationChains(person, unit.getUnit()));
                     }
                     participantAuthorizationChainss.put(person, temp);
                 } else {
                     // teachers, employees, researchers and grant owners should go here.
                     participantAuthorizationChainss.put(person, participantAuthorizationChain);
                 }
             }
         }
 
         return participantAuthorizationChainss;
     }
 
     private Unit getFirstValidUnit(final Unit unit, final Person person) {
        return unit.hasResponsiblesInUnit() && unit.hasUnit() && !unit.isResponsible(person.getUser().getExpenditurePerson()) ? unit : getFirstValidUnit(
                 unit.getParentUnit(), person);
     }
 
 }
