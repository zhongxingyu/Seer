 package fi.vincit.jmobster.util.groups;
 
 /*
  * Copyright 2012 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import fi.vincit.jmobster.processor.GroupMode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
 
 /**
  * Default implementation of group manager
  */
 public class GenericGroupManager<T> implements GroupManager<T> {
 
     private static final Logger LOG = LoggerFactory.getLogger( GenericGroupManager.class );
 
     final private Collection<T> groups;
     private GroupMode groupMode;
     private boolean includeValidatorsWithoutGroup = true;
 
     /**
      * Constructs a group manager
      * @param groupMode Group mode
      * @param requiredGroups Required groups
      */
     public GenericGroupManager( GroupMode groupMode, T... requiredGroups ) {
         this(groupMode, Arrays.asList(requiredGroups));
     }
 
     /**
      * Constructs a group manager
      * @param groupMode Group mode
      * @param requiredGroups Required groups
      */
     public GenericGroupManager( GroupMode groupMode, Collection<T> requiredGroups ) {
         this.groupMode = groupMode;
         this.groups = new HashSet<T>(requiredGroups.size());
         for( T group : requiredGroups ) {
             this.groups.add(group);
         }
     }
 
     /**
      * If set to true, the validators without group will be included in accepted groups.
      * Default value is true.
      * @param includeValidatorsWithoutGroup Value
      */
     public void setIncludeValidatorsWithoutGroup( boolean includeValidatorsWithoutGroup ) {
         this.includeValidatorsWithoutGroup = includeValidatorsWithoutGroup;
     }
 
     @Override
     public boolean match( HasGroups<T> groupObject ) {
         if( groupObject.hasGroups() && checkGroups( groupObject ) ) {
             return true;
         } else if( !groupObject.hasGroups() && includeValidatorsWithoutGroup ) {
             return true;
         }
         return false;
     }
 
     @Override
     public void setGroups( GroupMode groupMode, Collection<T> groups ) {
         this.groupMode = groupMode;
         this.groups.clear();
         this.groups.addAll(groups);
     }
 
     /**
      * Checks validator groups using the current groupMode.
      * @param groupObject Validator
      * @return True if groups match according to groupMode, otherwise false.
      */
     private boolean checkGroups(HasGroups<T> groupObject) {
         T[] groupsGiven = groupObject.getGroups();
 
         switch( groupMode ) {
             case ANY_OF_REQUIRED: return checkAnyRequiredGroups( groupsGiven );
             case EXACTLY_REQUIRED: return checkExactlyRequiredGroups( groupsGiven );
             case AT_LEAST_REQUIRED: return checkAtLeastRequiredGroups( groupsGiven );
             default: LOG.error("Invalid group mode: {}", groupMode); return false;
         }
     }
 
     private boolean checkAnyRequiredGroups( T[] groupsGiven ) {
         // If no groups configured, this always passes
         if( groups.size() == 0 ) {
             return true;
         }
 
         for( T group : groupsGiven ) {
             for( T myGroup : this.groups ) {
                 if( group.equals(myGroup) ) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean checkAtLeastRequiredGroups( T[] groupsGiven ) {
         final Set<T> configuredGroups = new HashSet<T>(groups.size());
         for( T group : groups ) {
             configuredGroups.add( group );
         }
         final int groupsNeededCount = configuredGroups.size();
 
         final Set<T> givenGroups = new HashSet<T>(groupsGiven.length);
        for( T group : groupsGiven ) {
            givenGroups.add( group );
        }
         final int groupsGivenCount = givenGroups.size();
 
         if( groupsGivenCount < groupsNeededCount ) {
             return false;
         }
 
         int groupsFoundCount = 0;
         for( T givenGroup : givenGroups ) {
             if( configuredGroups.contains(givenGroup) ) {
                 ++groupsFoundCount;
             }
         }
         return groupsFoundCount == groupsNeededCount;
     }
 
     private boolean checkExactlyRequiredGroups( T[] groupsGiven ) {
         // If no groups configured, this always passes
         if( groups.size() == 0 && groupsGiven.length == 0 ) {
             return true;
         }
 
         final int groupsNeededCount = this.groups.size();
         if( groupsGiven.length != groupsNeededCount ) {
             return false;
         }
         int groupsFoundCount = 0;
         for( T aGroup : groupsGiven ) {
             for( T vGroup : this.groups ) {
                 if( aGroup.equals(vGroup) ) {
                     ++groupsFoundCount;
                 }
             }
         }
         return groupsFoundCount == groupsNeededCount;
     }
 }
