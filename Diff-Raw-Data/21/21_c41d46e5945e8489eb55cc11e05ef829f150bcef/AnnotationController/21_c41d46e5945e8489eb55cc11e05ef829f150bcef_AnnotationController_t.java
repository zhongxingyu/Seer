 package ccnmtl.vital3.controllers;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.TreeMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.time.DateUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 
 import ccnmtl.utils.OmniComparator;
 import ccnmtl.utils.TastyClient;
 import ccnmtl.vital3.*;
 import ccnmtl.vital3.ucm.UserContextInfo;
 import ccnmtl.vital3.ucm.UserCourseManager;
 import ccnmtl.vital3.utils.*;
 import ccnmtl.vital3.commands.*;
 
 /**
  * Annotation controller is in charge of everything notes-related. This will be used to
  * display the "Manage my Notes" page in the regular Spring MVC fashion, but it will also
  * be used to receive and respond to AJAX requests from the three places notes are displayed in Vital.
  *
  * IMPORTANT: The parameters provided in the request will very likely be manipulated by the validator!
  * This is done in order to clear up some ambiguities in the semantics of the request. After validation, the limitBy
  * and recent parameters may have changed to the following form:
  * "limitBy" represents which materials we want notes on. It's value is either "none" "unit" "assignment" or "material".
  * If limitBy is "unit" we want only notes on materials related to a particular unit (via unitMaterialAssoc and via assignmentMaterialAssoc)
  * If limitBy is "assignment" we want only notes on materials related to a particular assignment (via assignmentMaterialAssoc)
  * If limitBy is "material" we want only notes on a particular material
  * "recent" represents a time period used to limit which annotations are returned. It's value is either null, "today" or "week"
  * The validator will find and set up any objects that were referred to in the request. For example, if this request indicated limitBy
  * a particular assignment, that assignment will be set on the command object.
  *
  * Note about tag-storage in Vital3: We use the Tasty server to store and retrieve tags. We use a user's
  * participant id as the Tasty "user" parameter, the annotation id as the Tasty "item" parameter, and the
  * tag string remains unchanged (except for trimming of leading and trailing whitespace).
  */
 public class AnnotationController extends Vital3CommandController {
     
     // set via dependency-injection, tasty is used for tagging
     private TastyClient tastyClient;
     
     /**
      * Protect this controller
      */
     protected Integer getMinAccessLevel(Vital3Command commandObj) {
         return UserCourseManager.STUDENT_ACCESS;
     }
     
     /**
      * Derive worksite from parameters... different in many cases
      */
     protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
         
         VitalWorksite worksite = null;
         
         AnnotationCommand command = (AnnotationCommand) commandObj;
         String action = command.getAction();
         
         if (action == null) {
             // validator decorated worksite already
             worksite = command.getWorksite();
             if (worksite == null) {
                 worksite = super.getRequestedWorksite(command);   
             }
                      
         } else if (action.equals("noteQuery")) {
             String limitBy = command.getLimitBy();
             if (limitBy.equals("none")) {
                 // validator decorated worksite already
                 worksite = command.getWorksite();
             } else {
                 if (limitBy.equals("material"))
                     worksite = command.getMaterial().getWorksite();
                 else if (limitBy.equals("unit"))
                     worksite = command.getUnit().getWorksite();
                 else
                     worksite = command.getAssignment().getRelatedWorksite();
                 // decorate worksite:
                 ucm.decorateWorksite(worksite, false, false);
             }
         } else if (action.equals("submitNote")) {
             Long id = command.getId();
             if (id == null) worksite = command.getMaterial().getWorksite();
             else worksite = command.getAnnotation().getRelatedWorksite();
             // decorate worksite:
             ucm.decorateWorksite(worksite, false, false);
         
         } else if (action.equals("deleteNote")) {
             // action = delete note
             worksite = command.getAnnotation().getRelatedWorksite();
             // decorate worksite:
             ucm.decorateWorksite(worksite, false, false);
        
         } else if (action.equals("deleteNotes")) {
             
             worksite = super.getRequestedWorksite(command);
             ucm.decorateWorksite(worksite, false, false);
         } 
         
         if (worksite == null) throw new RuntimeException("worksite was null. something went wrong.");
         
         return worksite;
     }
     
     public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
 
         // get common parameters:
         AnnotationCommand command = (AnnotationCommand) commandObj;
         UserContextInfo userInfo = getUserContextInfo(request);
         VitalParticipant participant = userInfo.getParticipant();
         String action = command.getAction();
 
         ModelAndView mav = null;
         
         try {
             // handle errors in a uniform manner... TO DO: split ajax requests away from this standard controller flow...
             if (errors.hasErrors()) mav = Vital3Utils.createErrorMAV(errors, this.messageSourceAccessor);
             else {
                 logger.debug("handle request");
                 // branch on action:
                 if (action == null) {
                     // this is a request for "manage my notes" page
                     
                     command.setLimitBy("none");
                     command.setGroupBy("materialTitle");
                     command.setAction("none");
                     command.setRecent(null);
                     Map model = noteQuery(participant, command);
                     
                     // add all units and inside them, assignments related to those units
                     VitalWorksite worksite = userInfo.getWorksite();
                     Set units = worksite.getUnits();
                     Vital3Utils.initCollections(Unit.class, units, "assignments", Assignment.class);
                     model.put("units", units);
                     
                     mav = new ModelAndView("manageMyNotes", model);
                     
                 } else if (action.equals("noteQuery")) {
                     // returns the view for whichever template is specified
                     String template = command.getTemplate();
                     String templateName = "notesMini_" + template;
                     Map model = noteQuery(participant, command);
                     mav = new ModelAndView(templateName, model);
                     
                     // prevent caching. Sky noticed IE seemed to be caching noteQuery.
                     // this method is defined in WebContentGenerator
                     this.preventCaching(response);
                     
                 } else if (action.equals("submitNote")) {
                     // returns ajaxResponse view
                     mav = submitNote(participant, command);
                     
                 } else if (action.equals("deleteNotes")) {
                     // returns redirectview
                     mav = deleteNotes(participant, command);
                     
                 } else if (action.equals("deleteNote")) {
                     // returns ajaxResponse view
                     mav = deleteNote(participant, command);
                     
                 }
             }
             
         } catch (Exception e) {
             logger.error("Error during annotation controller operation:", e);
             mav = Vital3Utils.createErrorMAV(e);
         }
         
         // put "currentUser", "worksite", and "admin" into the model if available:
         if (!(mav.getView() instanceof RedirectView))
             Vital3Utils.putUserInfoIntoModel(userInfo, mav.getModel());
         
         
         return mav;
     }
     
     
     // submitNote is for inserting and updating notes
     private ModelAndView submitNote(VitalParticipant participant, AnnotationCommand command) throws Exception {
         
         Long id = command.getId();
         String type = command.getType();
         String title = command.getTitle();
         Set tags = command.getTags();
         
         logger.debug("annotationController beginning submitNote...");
         
         // distinguish between insert/update by checking for an id parameter
         
         if (id != null) {
             // if we have id, this is an update:
             
             Annotation note = command.getAnnotation();
             // transfer the properties
             command.transferToAnnotation(note);
             // save
             vital3DAO.save(Annotation.class, note);
             
             
         } else {
             // no id param means this is an insert:
             
             Material material = command.getMaterial();
             
             // construct the Annotation using the Material and Participant, transfer the user-entered properties to the new Annotation
             Annotation note = new Annotation(material, participant, null, null, null, null, null, null);
             command.transferToAnnotation(note);
             // insert the annotation
             vital3DAO.save(Annotation.class, note);
             // need that annotation's id... Hibernate makes it available immediately!
             id = note.getId();
             if (id == null) throw new RuntimeException("Id was still null after saving new note!");
         
         }
         
         
         if (tags != null && tags.size() > 0) {
             logger.debug("There are " + tags.size() + " tags");
             // need to trim whitespace on tags?
             Iterator tagIter = tags.iterator();
             while (tagIter.hasNext()) {
                 String tag = (String) tagIter.next();
                 logger.debug("tag: >>>" + tag + "<<<");
             }
         } else {
             logger.debug("No tags");
         }
         // TastyClient will handle deleting the old unused tags and adding new ones:
         tastyClient.setTagsForItem(participant.getId().toString(), id.toString(), tags);
         
         
         logger.debug("returning from submitNote");
         return new ModelAndView("ajaxResponse", "body", "{id:\""+id+"\"}");
         
     }
     
     
     private ModelAndView deleteNote(VitalParticipant participant, AnnotationCommand command) throws Exception {
         
         Annotation note = command.getAnnotation();
         
         // delete tags from the annotation
         tastyClient.setTagsForItem(participant.getId().toString(), note.getId().toString(), null);
         
         // delete the annotation.
         vital3DAO.delete(Annotation.class, note);
         
         return new ModelAndView("ajaxResponse", null);
     }
     
 
     private ModelAndView deleteNotes(VitalParticipant participant, AnnotationCommand command) throws Exception {
         
         ArrayList notes = command.getEntityList();
         Iterator notesIter = notes.iterator();
         while (notesIter.hasNext()) {
             Annotation note = (Annotation)notesIter.next(); 
             tastyClient.setTagsForItem(participant.getId().toString(), note.getId().toString(), null);
             vital3DAO.delete(Annotation.class, note);  
         }
         
         // redirect to the "manage my notes" page
         String successUrl = "annotations.smvc?worksiteId=" + command.getWorksiteId();
         return new ModelAndView(new RedirectView(successUrl, true));
         
     }
     
     
     /**
      * If you want to call this method directly, your command object will need to match the form which
      * would be produced by the AnnotationValidator. This is DIFFERENT than the form of request that the
      * validator expects! See the documentation for this class (AnnotationController) for details.
      */
     public Map noteQuery(VitalParticipant participant, AnnotationCommand command) throws Exception {
         
         String groupBy = command.getGroupBy();
         String recent = command.getRecent();
         String limitBy = command.getLimitBy();
         
         logger.debug("noteQuery: groupBy = " + groupBy + " recent = " + recent + " limitBy = " + limitBy);
         
         Set materials = null;
         
         // If there is a limitBy parameter, use it to find the materials which fit that criteria:
         if (limitBy.equals("material") || limitBy.equals("unit") || limitBy.equals("assignment")) {
             
             // find the collection of materials related to the unit/assignment:
             
             if (limitBy.equals("material")) {
                 Material material = command.getMaterial();
                 materials = new HashSet();
                 materials.add(material);
                 
             } else if (limitBy.equals("assignment")) {
                 Assignment assignment = command.getAssignment();
                 materials = Vital3Utils.initM2MCollection(Assignment.class, assignment, Material.class);
                 
             } else {
                 Unit unit = command.getUnit();
                 // all materials for all child assignments...
                 Set unitAssignments = unit.getAssignments();
                 materials = Vital3Utils.initM2MCollections(Assignment.class, unitAssignments, Material.class);
                 // ...and all unit materials:
                 materials.addAll(Vital3Utils.initM2MCollection(Unit.class, unit, Material.class));
             }
             
         }
         
         // if there is a recent parameter, use it to set up a Date which will be used as search criteria:
         Date minDate = null;
         if (recent != null) {
             // truncate (round down) today's date to represent the very beginning of the day (midnight):
             Date today = minDate = DateUtils.truncate(new Date(), Calendar.DATE);
             if (recent.equals("week")) {
                 // create a new Date to represent a week ago
                 minDate = new Date(today.getTime() - DateUtils.MILLIS_PER_DAY * 7);
             } else {
                 minDate = today;
             }
             logger.debug("Prepared minDate criteria: " + minDate);
         }
         if (materials != null) logger.debug("Annotation controller prepared " + materials.size() + " materials for possible criteria match.");
         
         // Retrieve all annotations from the user for the material which meet the criteria (i.e. limitBy & recent):
         // See documentation of participant.getAnnotationsForMaterials for details on this method's behavior:
         //List annotations = participant.getAnnotationsForMaterials(materials, minDate);
         List annotations = vital3DAO.getAnnotations(participant, materials, minDate);
         logger.debug("Annotation controller found " + annotations.size() + " annotations meeting the criteria.");
         
         // Next we need to fetch tags for each annotation
         
         // retrieve all tagged items for this participant:
         Map tagMap;
         try {
             String idString = participant.getId().toString();
             tagMap = tastyClient.getAllItemsAndTagsForUser(idString);
             logger.debug("AnnotationController retrieved tagMap.");
         } catch (Exception e) {
             
             logger.error("Error during tag retrieval:", e);
             throw new Exception("Error during tag retrieval", e);
         }
         // that returns a Map with keys for the item id (String) which are linked to Sets of tags (Strings).
         // we can use a method Annotation.getTags(TreeMap tagMap) which will pull its tag Set out of the Map according to its Stringified id.
         
         // Next we need to group the annotations...
         
         // each element of the groupList must be a hashMap which contains a 'groupTitle', 'groupId', and a 'notes' key. 'notes' is an ArrayList.
         // groupId must be unique and contain safe characters.
         
         LinkedList groupList = new LinkedList();
         Map groupMap = null;
         Set notes = null;
         
         if (annotations.size() > 0) {
             
             if (groupBy.equals("noteTitle")) {
 
                 OmniComparator noteTitleComp = new OmniComparator(Annotation.class, "getTitle");
                 // sort annotations by its title:
                 Collections.sort(annotations, noteTitleComp);
                 
                 Material noteMaterial = command.getMaterial();                    
                 // create new groupMap and add it:
                 groupMap = createGroupMap(noteMaterial.getTitle(), "noteTitleGroup", annotations);                             
                 groupList.add(groupMap);
             
             } else if (groupBy.equals("materialTitle")) {
                 // -- primary (group) sort: note.material.title
                 
                 OmniComparator materialTitleComp = new OmniComparator(Material.class, "getTitle");
                 OmniComparator noteMaterialComp = new OmniComparator(Annotation.class, "getMaterial", materialTitleComp);
                 
                 //replace title by date.
                 
                 // secondary sort: if two annotations have the same material.title, sort them by reverse date modified.
                 
                 /*
                 ///secondary sort: sort materials by date modified beneath the material title.
                 logger.debug("Adding a tiebreaker sort: ");
                 OmniComparator subSort = new OmniComparator(Annotation.class, "getDateModified");
                 subSort.reverseSortOrder();
                 noteMaterialComp.setSecondaryComparator(subSort);
                 */
                 
                 // sort annotations by material title:
                 Collections.sort(annotations, noteMaterialComp);
                 
                //Long idMin = new Long(170162);
                //Long idMax = new Long(170270);
                
                
                
                 Material currentMaterial = null;
                 // iterate through annotations:
                 Iterator annoIter = annotations.iterator();
                 while (annoIter.hasNext()) {
                     
                     Annotation note = (Annotation) annoIter.next();
                     logger.debug (note);
                     
                     // see if this note's material is equal to the "current" material
                     Material noteMaterial = note.getMaterial();
                     if (!noteMaterial.equals(currentMaterial)) {
                        //logger.debug("AnnotationController: creating new group");
                         // if this is not our current material, we're done with the current group (remember annotations are grouped by material title)
                         // we need to create a new groupMap, add it to the groupList, and update our references,
                         notes = new TreeSet();
                         currentMaterial = noteMaterial;
                         // create new groupMap and add it:
                         groupMap = createGroupMap(noteMaterial.getTitle(), "mId-" + noteMaterial.getId(), notes);
                         groupList.add(groupMap);
                     }
                     // put this annotation into the groupMap.notes set:
                    //if (note.getId().compareTo(the_id_min) > 0  && note.getId().compareTo(idMax) < 0) {
                    if (1 == 1) {
                        notes.add(note);
                    }
                    else {
                        logger.debug ("rejected");
                    }
                 }
                 
             } else if (groupBy.equals("modificationDate")) {
                 
                 // -- primary (group) sort: note.dateModified
                 
                 OmniComparator noteDateComp = new OmniComparator(Annotation.class, "getDateModified");
                 // sort annotations by date modified:
                 Collections.sort(annotations, noteDateComp);
                 
                 Date currentModDate = null;
                 // iterate through annotations:
                 Iterator annoIter = annotations.iterator();
                 while (annoIter.hasNext()) {
                     
                     Annotation note = (Annotation) annoIter.next();
                     // see if this note's modDate is ON THE SAME DAY as the "current" modDate
                     Date noteModDate = note.getDateModified();
                     if (currentModDate == null || !DateUtils.isSameDay(noteModDate, currentModDate)) {
                         logger.debug("AnnotationController: creating new group");
                         // if this is not on the same day, we're done with the current group (remember annotations are grouped by material title)
                         // create new notes list, update reference to currentModDate:
                         notes = new TreeSet();
                         currentModDate = noteModDate;
                         // create new groupMap and add it:
                         String modDateString = textFormatter.dateToDateOnlyString(noteModDate);
                         groupMap = createGroupMap(modDateString, "modDate-" + textFormatter.deleteNonAlphaNumeric(modDateString), notes);
                         groupList.add(groupMap);
                     }
                     // put this annotation into the groupMap.notes list:
                     notes.add(note);
                 }
                 
                 
             } else {
                 // groupBy is "tag"
                 // -- primary (group) sort: tag.title
                 
                 // noTagNotes stores all notes which have no tag.
                 Set noTagNotes = new TreeSet();
                 // groupMaps will store each groupMap keyed by tag. Because it is a TreeMap, its keys are kept in tag-order (alphabetical).
                 Map groupMaps = new TreeMap();
                 
                 Iterator annoIter = annotations.iterator();
                 while (annoIter.hasNext()) {
                     
                     Annotation note = (Annotation) annoIter.next();
                     
                     Set noteTags = note.getTags(tagMap);
                     if (noteTags.isEmpty()) {
                         logger.debug("note " + note.getTitle() + " has no tags");
                         noTagNotes.add(note);
                     } else {
                         
                         Iterator tagIter = noteTags.iterator();
                         while (tagIter.hasNext()) {
                             String tag = (String) tagIter.next();
                             groupMap = (Map) groupMaps.get(tag);
                             if (groupMap == null) {
                                 notes = new TreeSet();
                                 notes.add(note);
                                 // create a new groupMap and add it to temp map:
                                 groupMap = createGroupMap(tag, "tag-"+URLEncoder.encode(tag, "UTF-8"), notes);
                                 groupMaps.put(tag, groupMap);
                             } else {
                                 notes = (Set) groupMap.get("notes");
                                 notes.add(note);
                             }
                         }
                     }
                 }
                 // transfer groupMaps to groupList. They are already ordered by tag.
                 Iterator groupIter = groupMaps.values().iterator();
                 while (groupIter.hasNext()) {
                     groupList.add(groupIter.next());
                 }
                 // add noTagNotes at the end of the list.
                 if (noTagNotes.size() > 0) {
                     groupMap = createGroupMap("(no tags)", "tag-notags", noTagNotes);
                     groupList.add(groupMap);
                 }
                 
             }
             
         }
         // We now have a List of Hashmaps, each containing a key "groupTitle" (holds a String), "groupId" (unique String) and a key "notes" (holding a List of Annotations).
         // We also have a Map keyed by (String) annotation id, which holds ordered Sets of (String) tags for each annotation.
         // in the template, we can call $annotation.getTags($tagMap) to get the set of tags for an annotation.
         
         HashMap model = new HashMap();
         
         model.put("action", command.getAction());
         model.put("tagMap", tagMap);
         model.put("groupBy", groupBy);
         model.put("groupList", groupList);
         model.put("textFormatter", this.getTextFormatter());
         
         return model;
         
     }
     
     
     // creates a groupMap which holds all the notes in the group.
     private Map createGroupMap(String groupTitle, Object groupId, Object notes) {
         Map groupMap = new HashMap();
         groupMap.put("groupTitle", groupTitle);
         groupMap.put("groupId", groupId);
         groupMap.put("notes", notes);
         return groupMap;
     }
     
     public void setTastyClient(TastyClient tc) {
         this.tastyClient = tc;
     }
     public TastyClient getTastyClient() {
         return this.tastyClient;
     }
     
 }
