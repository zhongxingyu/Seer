 package org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums;
 
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 public enum RESTCSNodeRelationshipModeV1 {
     ID, TARGET;
 
    public static RESTCSNodeRelationshipModeV1 getRelationshipMode(final int id) {
         switch (id) {
             case CommonConstants.CS_RELATIONSHIP_MODE_ID:
                 return ID;
             case CommonConstants.CS_RELATIONSHIP_MODE_TARGET:
                 return TARGET;
             default:
                 return null;
         }
     }
 
    public static Integer getRelationshipModeId(final RESTCSNodeRelationshipModeV1 relationshipMode) {
        if (relationshipMode == null) return null;
 
        switch (relationshipMode) {
             case ID:
                 return CommonConstants.CS_RELATIONSHIP_MODE_ID;
             case TARGET:
                 return CommonConstants.CS_RELATIONSHIP_MODE_TARGET;
             default:
                 return null;
         }
     }
 }
