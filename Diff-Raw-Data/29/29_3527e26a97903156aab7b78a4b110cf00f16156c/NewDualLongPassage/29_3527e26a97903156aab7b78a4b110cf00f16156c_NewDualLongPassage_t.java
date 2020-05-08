 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.components.questiontypes.longpassage;
 
 import com.preppa.web.data.LongDualPassageDAO;
 import com.preppa.web.data.PassageDAO;
 import com.preppa.web.data.TagDAO;
 import com.preppa.web.data.TestsubjectDAO;
 import com.preppa.web.entities.LongDualPassage;
 import com.preppa.web.entities.Tag;
 import com.preppa.web.entities.Testsubject;
 import com.preppa.web.entities.User;
 import com.preppa.web.services.PassageService;
 import com.preppa.web.utils.PassageType;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.FieldTranslator;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.ValidationException;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONObject;
 import org.chenillekit.tapestry.core.components.Editor;
 import org.chenillekit.tapestry.core.components.prototype_ui.AutoComplete;
 import org.springframework.security.annotation.Secured;
 
 /**
  *
  * @author nwt
  */
//@Secured("ROLE_USER")
 public class NewDualLongPassage {
 
     @Property
     private LongDualPassage longDualpassage;
     @ApplicationState
     private User user;
     @Inject
     private LongDualPassageDAO longDualpassageDAO;
     @Inject
     private PassageDAO passageDAO;
     @Component(parameters = {"value=fbodyone"})
     private Editor passeditorone;
     @Component(parameters = {"value=fbodytwo"})
     private Editor passeditortwo;
     private int size;
     @Property
     @Persist
     private Testsubject testsubject;
     private List<Testsubject> testsubjects;
     @Inject
     private TestsubjectDAO testsubjectDAO;
     @Property
     private String fTitle;
     @Property
     private String fTitle2;
     @Property
     private String fBodyone;
     @Property
     private String fBodytwo;
     @Property
     private String fSource;
     @Property
     private String fTag;
     @Property
     private String fSummary;
     @InjectPage
     private com.preppa.web.pages.contribution.longpassage.ShowDualLongPassage showdualpasage;
     @Component
     private AutoComplete autoCompleteTag;
     @Property
     private List<Tag> addedTags = new LinkedList<Tag>();
     @Inject
     private TagDAO tagDAO;
     @Inject
     private PassageService passageService;
     @Inject
     @Property
     private Block newtagblock;
     @Property
     private String fname;
     @Property
     private Tag tag;
     @Component
     private Form dualpassageform;
    private boolean showpage = false;


    
      public void setPageTrue() {
        showpage = true;
    }

    public void setPageFalse() {
        showpage = false;
    }
 
     void onActivate() {
         this.longDualpassage = new LongDualPassage();
     }
 
     @CommitAfter
     Object onSuccessFromDualPassageForm() {
 
 
         longDualpassage.setPassageone(fBodyone);
         longDualpassage.setPassagetwo(fBodytwo);
         longDualpassage.setTitle(fTitle);
         longDualpassage.setSource(fSource);
         longDualpassage.setSummary(fSummary);
         if (fBodyone.length() > 100) {
             longDualpassage.setPassagetype(PassageType.LONG_DUAL);
         } else {
             longDualpassage.setPassagetype(PassageType.SHORT_DUAL);
         }
 
 
 
 
         for (Tag t : addedTags) {
             if (!(longDualpassage.getTaglist().contains(t))) {
                 longDualpassage.getTaglist().add(t);
             }
         }
 
         passageService.checkLongDualPassage(longDualpassage);
         Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
         longDualpassage.setCreatedAt(now);
         longDualpassage.setUpdatedAt(now);
        longDualpassage.setUser(user);
        longDualpassage.setUpdatedBy(user);
 
 
         longDualpassageDAO.doSave(longDualpassage);
         showdualpasage.setLongDualPassage(longDualpassage);
         return showdualpasage;
     }
 
     public static String sanitize(String string) {
         return string.replaceAll("(?i)<script.*?>.*?</script.*?>", "") // case 1
                 .replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "") // case 2
                 .replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "");     // case 3
     }
 
     /**
      * @return the testsubjects
      */
     public List<Testsubject> getTestsubjects() {
         this.setTestsubjects(testsubjectDAO.findAll());
         return testsubjects;
     }
 
     /**
      * @param testsubjects the testsubjects to set
      */
     public void setTestsubjects(List<Testsubject> testsubjects) {
         this.testsubjects = testsubjects;
     }
 
     List<String> onProvideCompletionsFromTags(String partial) {
         List<Tag> matches = tagDAO.findByPartialName(partial);
 
         List<String> result = new ArrayList<String>();
         for (Tag t : matches) {
             result.add(t.getName());
         }
         return result;
     }
 
     List<Tag> onProvideCompletionsFromAutocompleteTag(String partial) {
         List<Tag> matches = tagDAO.findByPartialName(partial);
         return matches;
 
     }
 
     public FieldTranslator getTagTranslator() {
         return new FieldTranslator<Tag>() {
 
             @Override
             public String toClient(Tag value) {
                 String clientValue = "0";
                 if (value != null) {
                     clientValue = String.valueOf(value.getName());
                 }
 
                 return clientValue;
             }
 
             @Override
             public void render(MarkupWriter writer) {
             }
 
             @Override
             public Class<Tag> getType() {
                 return Tag.class;
             }
 
             @Override
             public Tag parse(String clientValue) throws ValidationException {
                 Tag serverValue = null;
 //            if(clientValue == null) {
 //                Tag t = new Tag();
 //                t.setName(clientValue);
 //            }
                 System.out.println(clientValue);
 
                 if (clientValue != null && clientValue.length() > 0 && !clientValue.equals("0")) {
                     System.out.println(clientValue);
                     serverValue = tagDAO.findByName(clientValue).get(0);
                 }
                 return serverValue;
             }
         };
     }
     //Funtions for adding new tags and topics
 
     @CommitAfter
     JSONObject onSuccessFromTagForm() {
         List<Tag> tolist = tagDAO.findByName(fname);
         JSONObject json = new JSONObject();
         if (tolist.size() > 0) {
             String markup = "<p>  <b>" + fname +
                     "</b> already exists. <p>";
             json.put("content", markup);
 
         } else {
             tag = new Tag();
             tag.setName(fname);
 
             tagDAO.doSave(tag);
             String markup = "<p> You just submitted <b>" + tag.getName() +
                     "</b>. Please add it using the dropdown <p>";
             json.put("content", markup);
 
         }
 
 
         // return new TextStreamResponse("text/json", json.toString());
         return json;
     }
 
     Block onActionFromCloseTag() {
         return newtagblock;
     }
 }
