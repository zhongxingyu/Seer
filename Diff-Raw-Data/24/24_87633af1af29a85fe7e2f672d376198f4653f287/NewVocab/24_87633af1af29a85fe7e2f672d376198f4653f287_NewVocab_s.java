 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.pages.contribution.vocab;
 
 import com.preppa.web.data.VocabDAO;
 import com.preppa.web.data.DictionaryWordDAO;
 import com.preppa.web.data.TagDAO;
 import com.preppa.web.entities.ExampleSentence;
 import com.preppa.web.entities.Vocab;
 import com.preppa.web.entities.DictionaryWord;
 import com.preppa.web.entities.Tag;
 import com.preppa.web.entities.User;
 import java.sql.Timestamp;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.FieldTranslator;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.ValidationException;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.TextField;
 import org.apache.tapestry5.json.JSONObject;
 import org.chenillekit.tapestry.core.components.prototype_ui.AutoComplete;
 import org.springframework.security.annotation.Secured;
 
 /**
  *
  * @author newtonik
  */
 @Secured("ROLE_USER")
 public class NewVocab {
     @ApplicationState
     private User user;
     @Property
     private Vocab vocab;
     @Inject
     private VocabDAO vocabDAO;
     @InjectPage
     private ShowVocab showvocab;
     @Property
    @Persist
     private String fWord;
     @Property
    @Persist
     private String partofspch;
     @Property
    @Persist
     private String fDefinition;
     @Property
     private String fSentence;
     /*@Property
     private String fTag;*/
 	@Component(id = "vocabform")
 	private Form _form;
     @Inject
     private DictionaryWordDAO dictionarywordDAO;
     @Component
     private AutoComplete autoCompleteTag;
     @Inject
     private TagDAO tagDAO;
     @Property
     private List<Tag> addedTags = new LinkedList<Tag>();
     @Inject
     @Property
     private Block newtagblock;
     @Property
     private Tag tag;
     @Component
     private TextField tagTextfield;
     @Property
     private String fname;
     @Component
     private Form tagform;
     @Property
     private DictionaryWord dWord;
 
     void onValidateFormFromVocabForm() {
     }
 
     List<Tag> onProvideCompletionsFromAutocompleteTag(String partial) {
         List<Tag> matches = tagDAO.findByPartialName(partial);
         return matches;
 
     }
 
     /*void onActivate(Vocab word) {
         this.vocab = word;
     }*/
 
     public Block getNewTagBlock() {
         return newtagblock;
     }
 
     public boolean getTitleFilled() {
         return (fWord != null);
     }
 
     public boolean getPOSFilled() {
         return (partofspch != null);
     }
 
     Object onActivate(int id) {
         if (this.dWord == null) {
             this.dWord = dictionarywordDAO.findById(id);
         }
         //this.vocab = vocabDAO.findById(id);
        System.out.println("***");
        if(dWord != null) {
             System.out.print("Here dWord is not null is is " + dWord);
             fWord = dWord.getName();
             partofspch = dWord.getPartofspeech();
             fDefinition = dWord.getDefinition();
         }
         return null;
     }
     Integer onPassivate() {
         if (dWord == null) {
             return null;
         }
         return this.dWord.getId();
     }
     List<String> onProvideCompletionsFromfWord(String partial)
     {
         List<DictionaryWord> matches = dictionarywordDAO.findByPartialName(partial);
 
         List<String> result = new ArrayList<String>();
 
             for (DictionaryWord a : matches)
             {
                 result.add(a.getName());
             }
        
         return result;
     }
 
     /*Object onPassivate() {
         if (vocab == null) {
             return vocab;
         }
         else {
             return vocab.getId();
         }
     }*/
 
     void cleanupRender() {
         System.out.println("*** I'm in cleanuprender!");
     }
 
     @CommitAfter
     Object onSuccessFromVocabForm() {
          this.vocab = new Vocab();
          vocab.setName(fWord);
          vocab.setPartofspeech(partofspch);
          vocab.setDefinition(fDefinition);
          vocab.setUser(user);
          Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
          vocab.setCreatedAt(now);
          vocab.setUpdatedAt(now);
          ExampleSentence sent = new ExampleSentence();
          if (fSentence != null) {
             sent.setSentence(fSentence);
             vocab.setSentence(sent);
          }
 
           for(Tag t: addedTags) {
             if(!(vocab.getTaglist().contains(t)))
             {
                 vocab.getTaglist().add(t);
             }
           }
 
          vocabDAO.doSave(vocab);
 
          System.out.println("Setting dWord, it is "+ dWord);
          if (dWord != null) {
             System.out.println("dWord set");
             dWord.setSubmitted(true);
             dictionarywordDAO.doSave(dWord);
          }
          showvocab.setvocab(vocab);
          addedTags.clear();
          fWord = null;
          partofspch = null;
          return showvocab;
     }
 
 public FieldTranslator getTagTranslator()
     {
         return new FieldTranslator<Tag>()
         {
             @Override
           public String toClient(Tag value)
           {
                 String clientValue = "0";
                 if (value != null)
                 clientValue = String.valueOf(value.getName());
 
                 return clientValue;
           }
 
           @Override
           public void render(MarkupWriter writer) { }
 
           @Override
           public Class<Tag> getType() { return Tag.class; }
 
           @Override
           public Tag parse(String clientValue) throws ValidationException
           {
             Tag serverValue = null;
             if(clientValue == null) {
                 Tag t = new Tag();
                 t.setName(clientValue);
             }
             System.out.println(clientValue);
 
             if (clientValue != null && clientValue.length() > 0 && !clientValue.equals("0")) {
                 System.out.println(clientValue);
                 serverValue = tagDAO.findByName(clientValue).get(0);
             }
             return serverValue;
           }
 
     };
    }
 
        Block onActionFromAddTag() {
             return newtagblock;
        }
          Block onActionFromCloseTag() {
             return newtagblock;
         }
 
        Block onActionFromCancelTag() {
             return null;
        }
              //Funtions for adding new tags and topics
         @CommitAfter
         JSONObject onSuccessFromTagForm() {
             List<Tag> tolist =  tagDAO.findByName(fname);
             JSONObject json = new JSONObject();
             System.out.print(tolist);
             if(tolist.size() > 0) {
                 String markup = "<p>  <b>" + fname +
                     "</b> already exists. <p>";
                 json.put("content", markup);
 
             }
             else
             {
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
 
 
 }
