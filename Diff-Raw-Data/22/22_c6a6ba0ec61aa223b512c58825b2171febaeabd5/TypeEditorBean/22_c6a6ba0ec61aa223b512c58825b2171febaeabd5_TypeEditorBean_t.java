 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.structure;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.MessageBean;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.ReferencedContent;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.model.SelectItem;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Bean behind typeEditor.xhtml to
  * edit FxType objects
  *
  * @author Gerhard Glos (gerhard.glos@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 public class TypeEditorBean {
 
     private static final Log LOG = LogFactory.getLog(TypeEditorBean.class);
     private FxTypeEdit type = null;
     private List<WrappedRelation> wrappedRelations = null;
     private long defaultSelectListTypeId = CacheAdmin.getEnvironment().getTypes(true, true, true, false).get(0).getId();
     private long relSourceIdFiler = defaultSelectListTypeId;
     private long relDestIdFiler = defaultSelectListTypeId;
     private long relMaxSourceFiler = DEFAULT_REL_MAX;
     private long relMaxDestFiler = DEFAULT_REL_MAX;
     private boolean relSourceUnlimitedFiler = true;
     private boolean relDestUnlimitedFiler = true;
 
     private WrappedRelation wrappedRelationFiler = null;
     private boolean unlimitedVersions = true;
     private List<SelectItem> timeRanges = null;
     private int timeRange = -1;
     private long historyAge = -1;
     private FxReference icon = new FxReference(false, FxReference.EMPTY);
     private boolean maxRelSourceUnlimited = false;
     private boolean maxRelDestUnlimited = false;
     private static final int DEFAULT_REL_MAX = 100;
     //checker if current user may edit the property
     boolean structureManagement = false;
     //checker for the editMode: if not in edit mode,
     // save and delete buttons are not rendered by the gui
     private boolean editMode = false;
 
     //quick fix for reloading the content tree context menu after creating a new type
     private boolean reloadContentTree = false;
 
     //type script editor tab
     private ScriptListWrapper scriptWrapper = null;
     private int scriptListFiler = -1;
     private FxScriptInfo selectedScriptInfo = null;
     private long selectedScriptEventId = -1;
     private boolean selectedDerivedUsage = false;
     private boolean selectedActive = true;
 
     public String getParseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 return null;
             } else if ("openInstance".equals(action)) {
                 editMode = false;
                 long propId = FxJsfUtils.getLongParameter("id", -1);
                 editType(propId);
             } else if ("editInstance".equals(action)) {
                 editMode = true;
                 long propId = FxJsfUtils.getLongParameter("id", -1);
                 editType(propId);
             } else if ("createType".equals(action)) {
                 editMode = true;
                 type = FxTypeEdit.createNew("NEWTYPE", new FxString(""), CacheAdmin.getEnvironment().getDefaultACL(ACL.Category.STRUCTURE));
                 initEditing();
                 setTypeMode(TypeMode.Content);
             } else if ("createTypeRelation".equals(action)) {
                 editMode = true;
                 type = FxTypeEdit.createNew("NEWTYPE", new FxString(""), CacheAdmin.getEnvironment().getDefaultACL(ACL.Category.STRUCTURE));
                 initEditing();
                 setTypeMode(TypeMode.Relation);
             }
 
         } catch (Throwable t) {
             LOG.error("Failed to parse request parameters: " + t.getMessage(), t);
             new FxFacesMsgErr(t).addToContext();
             return "structureContent";
         }
 
         return null;
     }
 
     public long getTypeId() {
         return type != null ? type.getId() : -1;
     }
 
    //necessarry only to prevent JSF errors because of value binding
    public void setTypeId(long typeId) {
    }

     /**
      * Load a type for editing
      *
      * @param typeId id of the type
      */
     public void editType(long typeId) {
         this.type = CacheAdmin.getEnvironment().getType(typeId).asEditable();
         initEditing();
     }
 
     public boolean isReloadContentTree() {
         return reloadContentTree;
     }
 
     public void toggleEditMode() {
         editMode = !editMode;
     }
 
     public boolean getEditMode() {
         return editMode;
     }
 
     public boolean isStructureManagement() {
         return structureManagement;
     }
 
     /**
      * Initializes variables and does workarounds so editing
      * of an existing group and group assignment is possible via the webinterface.
      */
     private void initEditing() {
         structureManagement = FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement);
         reloadContentTree = false;
         if (!type.isNew())
             scriptWrapper = new ScriptListWrapper(type.getId(), true);
         wrappedRelations = new ArrayList<WrappedRelation>(type.getRelations().size());
         for (FxTypeRelation r : type.getRelations()) {
             wrappedRelations.add(new WrappedRelation(r));
         }
         unlimitedVersions = type.getMaxVersions() == -1;
         maxRelSourceUnlimited = type.getMaxRelSource() == 0;
         maxRelDestUnlimited = type.getMaxRelDestination() == 0;
         icon = type.getIcon();
         timeRange = initTimeRange();
     }
 
     public boolean isMaxRelSourceUnlimited() {
         return maxRelSourceUnlimited;
     }
 
     public void setMaxRelSourceUnlimited(boolean maxRelSourceUnlimited) {
         if (this.maxRelSourceUnlimited && !maxRelSourceUnlimited)
             setMaxRelSource(DEFAULT_REL_MAX);
         if (!this.maxRelSourceUnlimited && maxRelSourceUnlimited)
             setMaxRelSource(0);
         this.maxRelSourceUnlimited = maxRelSourceUnlimited;
     }
 
     public boolean isMaxRelDestUnlimited() {
         return maxRelDestUnlimited;
     }
 
     public void setMaxRelDestUnlimited(boolean maxRelDestUnlimited) {
         if (this.maxRelDestUnlimited && !maxRelDestUnlimited)
             setMaxRelDestination(DEFAULT_REL_MAX);
         if (!this.maxRelDestUnlimited && maxRelDestUnlimited)
             setMaxRelDestination(0);
         this.maxRelDestUnlimited = maxRelDestUnlimited;
     }
 
     public FxReference getIcon() {
         if( !icon.isEmpty() && !icon.getDefaultTranslation().hasContent() ) {
             //load the content to be able to display the caption
             try {
                 icon.getDefaultTranslation().setContent(EJBLookup.getContentEngine().load(new FxPK(icon.getDefaultTranslation().getId())));
             } catch (FxApplicationException e) {
                 LOG.warn(e);
             }
         }
         return icon;
     }
 
     public void setIcon(FxReference icon) {
         this.icon = icon;
         this.type.setIcon(icon);
     }
 
     /**
      * Converts milliseconds to a more user-friendly time format.
      * 0=forever, 1=ms, 2=sec, 3=min, 4=day
      *
      * @return the maximum appliable time format
      */
     private int initTimeRange() {
         //0=forever, 1=ms, 2=sec, 3=min, 4=day
         historyAge = type.getHistoryAge();
         if (historyAge <= 0)
             return 0;
         if ((historyAge / 1000) > 1 && historyAge % 1000 == 0) {
             historyAge = historyAge / 1000;
         } else return 1;
         if ((historyAge / 60) > 1 && historyAge % 60 == 0) {
             historyAge = historyAge / 60;
         } else return 2;
         if ((historyAge / (60 * 24)) > 1 && historyAge % (60 * 24) == 0) {
             historyAge = historyAge / (60 * 24);
             return 4;
         } else return 3;
     }
 
     /**
      * Converts the currently used time format back to milliseconds.
      *
      * @return the current time format converted to milliseconds
      */
     private long historyAgeToMilis() {
         if (timeRange == 0)
             return 0;
         if (timeRange == 1)
             return historyAge;
         else if (timeRange == 2)
             return historyAge * 1000;
         else if (timeRange == 3)
             return historyAge * 1000 * 60;
         else if (timeRange == 4)
             return historyAge * 1000 * 60 * 60 * 24;
 
             //should never happen
         else return -1;
     }
 
     /**
      * Propagates changes to the DB.
      */
     public void saveChanges() {
         if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.ScriptManagement)) {
             try {
                 if (!type.isNew())
                     saveScriptChanges();
             }
             catch (Throwable t) {
                 new FxFacesMsgErr(t).addToContext();
             }
         } else
             new FxFacesMsgInfo("StructureEditor.info.notInRole.scriptManagement").addToContext();
 
         if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement)) {
             try {
                 if (unlimitedVersions) {
                     type.setMaxVersions(-1);
                 }
                 updateRelations();
 
                 type.setHistoryAge(historyAgeToMilis());
                 long id = EJBLookup.getTypeEngine().save(type);
                 StructureTreeControllerBean s = (StructureTreeControllerBean) FxJsfUtils.getManagedBean("structureTreeControllerBean");
                 if (type.isNew()) {
                     s.addAction(StructureTreeControllerBean.ACTION_RELOAD_SELECT_TYPE, id, "");
                 } else
                     s.addAction(StructureTreeControllerBean.ACTION_RENAME_SELECT_TYPE, id, type.getDisplayName());
 
                 new FxFacesMsgInfo("TypeEditor.message.info." + (type.isNew() ? "createdType" : "savedChanges"),
                         type.getLabel()).addToContext();
                 reloadContentTree = true;
             }
             catch (Throwable t) {
                 new FxFacesMsgErr(t).addToContext();
             }
         } else
             new FxFacesMsgInfo("StructureEditor.info.notInRole.structureManagement").addToContext();
     }
 
     /**
      * Remove all existing relations and add newly created relations,
      * created from wrappedRelations
      */
     private void updateRelations() {
         while (!type.getRelations().isEmpty()) {
             type.removeRelation(type.getRelations().get(0));
         }
 
         for (WrappedRelation r : wrappedRelations) {
             try {
                 type.updateRelation(r.asRelation());
             }
             catch (Throwable t) {
                 new FxFacesMsgErr(t).addToContext();
             }
         }
     }
 
     public int getTimeRange() {
         return timeRange;
     }
 
     public void setTimeRange(int timeRange) {
         this.timeRange = timeRange;
     }
 
     public List<SelectItem> getTimeRanges() {
         if (timeRanges == null) {
             timeRanges = new ArrayList<SelectItem>();
             MessageBean messageBean = (MessageBean) FxJsfUtils.getManagedBean("fxMessageBean");
             timeRanges.add(new SelectItem(0, messageBean.getMessage("TypeEditor.selectlist.timeRanges.label.forever")));
             timeRanges.add(new SelectItem(1, messageBean.getMessage("TypeEditor.selectlist.timeRanges.label.ms")));
             timeRanges.add(new SelectItem(2, messageBean.getMessage("TypeEditor.selectlist.timeRanges.label.sec")));
             timeRanges.add(new SelectItem(3, messageBean.getMessage("TypeEditor.selectlist.timeRanges.label.min")));
             timeRanges.add(new SelectItem(4, messageBean.getMessage("TypeEditor.selectlist.timeRanges.label.days")));
         }
         return timeRanges;
     }
 
     public boolean isUnlimitedVersions() {
         return unlimitedVersions;
     }
 
     public void setUnlimitedVersions(boolean unlimitedVersions) {
         if (!this.unlimitedVersions && unlimitedVersions)
             type.setMaxVersions(-1);
         if (this.unlimitedVersions && !unlimitedVersions)
             type.setMaxVersions(0);
         this.unlimitedVersions = unlimitedVersions;
     }
 
     public List<WrappedRelation> getWrappedRelations() {
         return wrappedRelations;
     }
 
     public long getRelSourceIdFiler() {
         return relSourceIdFiler;
     }
 
     public void setRelSourceIdFiler(long relSourceIdFiler) {
         this.relSourceIdFiler = relSourceIdFiler;
     }
 
     public long getRelDestIdFiler() {
         return relDestIdFiler;
     }
 
     public void setRelDestIdFiler(long relDestIdFiler) {
         this.relDestIdFiler = relDestIdFiler;
     }
 
     public long getRelMaxDestFiler() {
         return relMaxDestFiler;
     }
 
     public void setRelMaxDestFiler(long relMaxDestFiler) {
         this.relMaxDestFiler = relMaxDestFiler;
     }
 
     public long getRelMaxSourceFiler() {
         return relMaxSourceFiler;
     }
 
     public void setRelMaxSourceFiler(long relMaxSourceFiler) {
         this.relMaxSourceFiler = relMaxSourceFiler;
     }
 
     public boolean isRelSourceUnlimitedFiler() {
         return relSourceUnlimitedFiler;
     }
 
     public void setRelSourceUnlimitedFiler(boolean relSourceUnlimitedFiler) {
         if (this.relSourceUnlimitedFiler && !relSourceUnlimitedFiler)
             this.relMaxSourceFiler = DEFAULT_REL_MAX;
         if (!this.relSourceUnlimitedFiler && relSourceUnlimitedFiler)
             this.relMaxSourceFiler = 0;
         this.relSourceUnlimitedFiler = relSourceUnlimitedFiler;
     }
 
     public boolean isRelDestUnlimitedFiler() {
         return relDestUnlimitedFiler;
     }
 
     public void setRelDestUnlimitedFiler(boolean relDestUnlimitedFiler) {
         if (this.relDestUnlimitedFiler && !relDestUnlimitedFiler)
             this.relMaxDestFiler = DEFAULT_REL_MAX;
         if (!this.relDestUnlimitedFiler && relDestUnlimitedFiler)
             this.relMaxDestFiler = 0;
         this.relDestUnlimitedFiler = relDestUnlimitedFiler;
     }
 
     public void addRelation() {
         WrappedRelation relationToAdd = new WrappedRelation(getRelSourceIdFiler(), getRelDestIdFiler(),
                 isRelSourceUnlimitedFiler() ? 0 : getRelMaxSourceFiler(), isRelDestUnlimitedFiler() ? 0 : getRelMaxDestFiler());
         if (!wrappedRelations.contains(relationToAdd)) {
             wrappedRelations.add(relationToAdd);
             setRelSourceIdFiler(defaultSelectListTypeId);
             setRelDestIdFiler(defaultSelectListTypeId);
             setRelMaxSourceFiler(DEFAULT_REL_MAX);
             setRelMaxDestFiler(DEFAULT_REL_MAX);
         } else
             new FxFacesMsgErr("TypeEditor.message.error.relationAlreadyExists").addToContext();
     }
 
     public WrappedRelation getWrappedRelationFiler() {
         return wrappedRelationFiler;
     }
 
     public void setWrappedRelationFiler(WrappedRelation wrappedRelationFiler) {
         this.wrappedRelationFiler = wrappedRelationFiler;
     }
 
     public void removeRelation() {
         WrappedRelation wrappedRelToRemove = getWrappedRelationFiler();
         int removeIndex = -1;
         for (int i = 0; i < wrappedRelations.size(); i++) {
             if (wrappedRelations.get(i).equals(wrappedRelToRemove)) {
                 removeIndex = i;
                 break;
             }
         }
         if (removeIndex != -1) {
             wrappedRelations.remove(removeIndex);
         }
     }
 
     public FxTypeEdit getType() {
         return type;
     }
 
     public void setType(FxTypeEdit type) {
         this.type = type;
         this.icon = type.getIcon();
     }
 
     public void setCategory(TypeCategory tg) {
         this.type.setCategory(tg);
     }
 
     public TypeCategory getCategory() {
         return this.type.getCategory();
     }
 
     public void setLanguageMode(LanguageMode lm) {
         this.type.setLanguage(lm);
     }
 
     public LanguageMode getLanguageMode() {
         return this.type.getLanguage();
     }
 
     public void setTypeMode(TypeMode tm) {
         this.type.setMode(tm);
     }
 
     public TypeMode getTypeMode() {
         return this.type.getMode();
     }
 
     public void setTypeState(TypeState ts) {
         this.type.setState(ts);
     }
 
     public TypeState getTypeState() {
         return this.type.getState();
     }
 
     public void setWorkflow(long id) {
         this.type.setWorkflow(CacheAdmin.getEnvironment().getWorkflow(id));
     }
 
     public long getWorkflow() {
         return this.type.getWorkflow().getId();
     }
 
     public void setACL(long id) {
         this.type.setACL(CacheAdmin.getEnvironment().getACL(id));
     }
 
     public long getACL() {
         return this.type.getACL().getId();
     }
 
     public void setDescription(FxString desc) {
         this.type.setDescription(desc);
     }
 
     public FxString getDescription() {
         return this.type.getDescription();
     }
 
     public void setHistoryAge(long age) {
         this.historyAge = age;
     }
 
     public long getHistoryAge() {
         return this.historyAge;
     }
 
     public void setMaxRelDestination(int dest) {
         this.type.setMaxRelDestination(dest);
     }
 
     public int getMaxRelDestination() {
         return this.type.getMaxRelDestination();
     }
 
     public void setMaxRelSource(int source) {
         this.type.setMaxRelSource(source);
     }
 
     public int getMaxRelSource() {
         return this.type.getMaxRelSource();
     }
 
     public void setMaxVersions(long versions) {
         this.type.setMaxVersions(versions);
     }
 
     public long getMaxVersions() {
         return this.type.getMaxVersions();
     }
 
     public void setName(String name) {
         this.type.setName(name);
     }
 
     public String getName() {
         return this.type.getName();
     }
 
     public void setRemoveInstancesWithRelationTypes(boolean rel) {
         this.type.setRemoveInstancesWithRelationTypes(rel);
     }
 
     public boolean isRemoveInstancesWithRelationTypes() {
         return this.type.isRemoveInstancesWithRelationTypes();
     }
 
     public void setTrackHistory(boolean hist) {
         this.type.setTrackHistory(hist);
     }
 
     public boolean isTrackHistory() {
         return this.type.isTrackHistory();
     }
 
     public void setUseInstancePermissions(boolean perm) {
         this.type.setUseInstancePermissions(perm);
     }
 
     public boolean isUseInstancePermissions() {
         return this.type.useInstancePermissions();
     }
 
     public void setUsePropertyPermissions(boolean perm) {
         this.type.setUsePropertyPermissions(perm);
     }
 
     public boolean isUsePropertyPermissions() {
         return this.type.usePropertyPermissions();
     }
 
     public void setUseStepPermissions(boolean perm) {
         this.type.setUseStepPermissions(perm);
     }
 
     public boolean isUseStepPermissions() {
         return this.type.useStepPermissions();
     }
 
     public void setUseTypePermissions(boolean perm) {
         this.type.setUseTypePermissions(perm);
     }
 
     public boolean isUseTypePermissions() {
         return this.type.useTypePermissions();
     }
 
     /*
      *  Simple wrapper class for FxTypeRelation with convenient getters and setters
      * for GUI operations
      */
     public class WrappedRelation {
         private long sourceId;
         private long destId;
         protected long maxSource;
         protected long maxDest;
         private int id;
 
         WrappedRelation(FxTypeRelation r) {
             this.sourceId = r.getSource().getId();
             this.destId = r.getDestination().getId();
             this.maxSource = r.getMaxSource();
             this.maxDest = r.getMaxDestination();
         }
 
         public WrappedRelation(long sourceId, long destId, long maxSource, long maxDest) {
             this.sourceId = sourceId;
             this.destId = destId;
             this.maxSource = maxSource;
             this.maxDest = maxDest;
         }
 
         public FxTypeRelation asRelation() {
             return new FxTypeRelation(CacheAdmin.getEnvironment().getType(sourceId),
                     CacheAdmin.getEnvironment().getType(destId),
                     maxSource, maxDest);
         }
 
         public long getSourceId() {
             return sourceId;
         }
 
         public void setSourceId(long sourceId) {
             this.sourceId = sourceId;
         }
 
         public long getDestId() {
             return destId;
         }
 
         public void setDestId(long destinationId) {
             this.destId = destinationId;
         }
 
         public long getMaxSource() {
             return maxSource;
         }
 
         public void setMaxSource(long maxSource) {
             this.maxSource = maxSource;
         }
 
         public long getMaxDest() {
             return maxDest;
         }
 
         public void setMaxDest(long maxDestination) {
             this.maxDest = maxDestination;
         }
 
         public int getId() {
             return id;
         }
 
         public void setId(int id) {
             this.id = id;
         }
 
         @Override
         public boolean equals(Object o) {
             if (o instanceof FxTypeRelation)
                 return this.asRelation().equals(o);
             else if (o instanceof WrappedRelation)
                 return (((WrappedRelation) o).getSourceId() == this.getSourceId() &&
                         ((WrappedRelation) o).getDestId() == this.getDestId());
             else return false;
         }
 
         @Override
         public int hashCode() {
             int result;
             result = (int) (sourceId ^ (sourceId >>> 32));
             result = 31 * result + (int) (destId ^ (destId >>> 32));
             return result;
         }
 
         public boolean equalsCompletely(Object o) {
             if (o instanceof FxTypeRelation)
                 return this.asRelation().equalsCompletely(o);
             else if (o instanceof WrappedRelation)
                 return (((WrappedRelation) o).getSourceId() == this.getSourceId() &&
                         ((WrappedRelation) o).getDestId() == this.getDestId() &&
                         ((WrappedRelation) o).getMaxSource() == this.getMaxSource() &&
                         ((WrappedRelation) o).getMaxDest() == this.getMaxDest());
             else return false;
         }
 
         public boolean isMaxSourceUnlimited() {
             return maxSource == 0;
         }
 
         public void setMaxSourceUnlimited(boolean maxSourceUnlimited) {
             if (this.isMaxSourceUnlimited() && !maxSourceUnlimited)
                 setMaxSource(DEFAULT_REL_MAX);
             if (!this.isMaxSourceUnlimited() && maxSourceUnlimited)
                 setMaxRelSource(0);
         }
 
         public boolean isMaxDestUnlimited() {
             return maxDest == 0;
         }
 
         public void setMaxDestUnlimited(boolean maxDestUnlimited) {
             if (this.isMaxDestUnlimited() && !maxDestUnlimited)
                 setMaxDest(DEFAULT_REL_MAX);
             if (!this.isMaxDestUnlimited() && maxDestUnlimited)
                 setMaxDest(0);
         }
     }
 
     public String showTypeEditor() {
         return "typeEditor";
     }
 
     /**
      * called from the script editor; to open an instance where the script is assigned to
      *
      * @return type editor page
      */
     public String gotoTypeScriptEditor() {
         editMode = false;
         long propId = FxJsfUtils.getLongParameter("oid", -1);
         editType(propId);
         return showTypeScriptEditor();
     }
 
     /**
      * computes a list of relation types that contain relations of
      * which this type is source or destination
      *
      * @return the ids of referencing relation types
      */
     public List<FxType> getReferencingRelations() {
         return CacheAdmin.getEnvironment().getReferencingRelationTypes(type.getId());
     }
 
     /**
      * ************** script editor tab begin ***********************
      */
 
     public String showTypeScriptEditor() {
         return "typeScriptEditor";
     }
 
     public ScriptListWrapper getScriptWrapper() {
         return scriptWrapper;
     }
 
     public int getScriptCount() {
         return scriptWrapper == null ? 0 : scriptWrapper.getScriptList().size();
     }
 
     public int getScriptListFiler() {
         return scriptListFiler;
     }
 
     public void setScriptListFiler(int scriptListFiler) {
         this.scriptListFiler = scriptListFiler;
     }
 
     public void removeScript() {
         scriptWrapper.remove(scriptListFiler);
     }
 
     public FxScriptInfo getSelectedScriptInfo() {
         return selectedScriptInfo;
     }
 
     public void setSelectedScriptInfo(FxScriptInfo selectedScriptInfo) {
         this.selectedScriptInfo = selectedScriptInfo;
     }
 
     public long getSelectedScriptEventId() {
         if (selectedScriptInfo != null)
             return selectedScriptInfo.getEvent().getId();
         else return -1;
     }
 
     public void setSelectedScriptEventId(long selectedScriptEventId) {
         this.selectedScriptEventId = selectedScriptEventId;
     }
 
     public boolean isSelectedDerivedUsage() {
         return selectedDerivedUsage;
     }
 
     public void setSelectedDerivedUsage(boolean selectedDerivedUsage) {
         this.selectedDerivedUsage = selectedDerivedUsage;
     }
 
     public boolean isSelectedActive() {
         return selectedActive;
     }
 
     public void setSelectedActive(boolean selectedActive) {
         this.selectedActive = selectedActive;
     }
 
     public void addScript() {
         try {
             scriptWrapper.add(selectedScriptInfo.getId(), selectedScriptEventId, selectedDerivedUsage, selectedActive);
             this.selectedScriptInfo = CacheAdmin.getFilteredEnvironment().getScripts().get(0);
             this.selectedScriptInfo.getEvent().getId();
         }
         catch (Throwable t) {
             //TODO: print error message, a4j tags do not support faces message erros
             //new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public Map<Long, String> getTypeNameForId() {
         return new HashMap<Long, String>() {
             public String get(Object key) {
                 return CacheAdmin.getFilteredEnvironment().getType((Long) key).getDisplayName();
             }
         };
     }
 
     /**
      * Saves script assignment changes to DB.
      *
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on errors
      */
     public void saveScriptChanges() throws FxApplicationException {
         for (ScriptListWrapper.ScriptListEntry e : scriptWrapper.getDelta(type.getId(), true)) {
             if (e.getId() == ScriptListWrapper.ID_SCRIPT_ADDED)
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(e.getScriptEvent(), e.getScriptInfo().getId(), type.getId(), e.isActive(), e.isDerivedUsage());
             else if (e.getId() == ScriptListWrapper.ID_SCRIPT_REMOVED)
                 EJBLookup.getScriptingEngine().removeTypeScriptMappingForEvent(e.getScriptInfo().getId(), type.getId(), e.getScriptEvent());
             else if (e.getId() == ScriptListWrapper.ID_SCRIPT_UPDATED)
                 EJBLookup.getScriptingEngine().updateTypeScriptMappingForEvent(e.getScriptInfo().getId(), type.getId(), e.getScriptEvent(), e.isActive(), e.isDerivedUsage());
         }
     }
 
     /****script editor tab end*********/
 
 }
