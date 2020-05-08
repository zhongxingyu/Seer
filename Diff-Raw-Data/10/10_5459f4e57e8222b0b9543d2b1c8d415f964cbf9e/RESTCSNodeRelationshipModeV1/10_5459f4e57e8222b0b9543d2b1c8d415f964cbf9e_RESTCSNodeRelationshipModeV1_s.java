 package org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums;
 
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 public enum RESTCSNodeRelationshipModeV1 {
     ID, TARGET;
 
    public static RESTCSNodeRelationshipModeV1 getRelationshipType(final int id) {
         switch (id) {
             case CommonConstants.CS_RELATIONSHIP_MODE_ID:
                 return ID;
             case CommonConstants.CS_RELATIONSHIP_MODE_TARGET:
                 return TARGET;
             default:
                 return null;
         }
     }
 
    public static Integer getRelationshipTypeId(final RESTCSNodeRelationshipModeV1 relationshipType) {
        if (relationshipType == null) return null;
 
        switch (relationshipType) {
             case ID:
                 return CommonConstants.CS_RELATIONSHIP_MODE_ID;
             case TARGET:
                 return CommonConstants.CS_RELATIONSHIP_MODE_TARGET;
             default:
                 return null;
         }
     }
 }
