 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package test;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 /**
  *
  * @author Coevtmw
  */
 public class InstrumentTest {
     private InstrumentFacadeLocal instrumentFacade = null;
     private Instrument instrument = null;
     private HashMap<String,Item> itemHash = new HashMap<String,Item>();
     private boolean initialized = false;
     private int itemSequence = 0;
     
     /**
      * Use JNDI to retrieve EJB reference
      * @return
      */
     private InstrumentFacadeLocal lookupInstrumentFacade() {
         try {
             Context c = new InitialContext();
             instrumentFacade = (InstrumentFacadeLocal) c.lookup("java:comp/env/InstrumentFacade_ejbref");
             return instrumentFacade;
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Create new InstrumentTest
      * @param name
      * @param description
      */
     public InstrumentTest(String name) {
         instrumentFacade = lookupInstrumentFacade();
         
         instrument = new Instrument();
         instrument.setInstrumentName(name);
         instrument.setDisplayCount(0);
         instrument.setItemCollection(new ArrayList<Item>());
         
         instrumentFacade.create(instrument);
         initialized = true;
     }
     
     /**
      * Call immediately after start of Servlet session
      */
     public void beginServerProcessing() {
         instrument.setDisplayCount(instrument.getDisplayCount() + 1);
     }
     
     /**
      * Create a new item and write the question which will be posed to the user
      * @param varName
      * @param question
      */
     public void writeQuestion(String varName, String question) {
         if (!initialized) {
             return;
         }
         
         Item item;
         if (itemHash.containsKey(varName)) {
             item = itemHash.get(varName);
         }
         else {
             item = new Item();
             item.setVarName(varName);
         }
         item.setQuestion(question);
         item.setInstrumentID(instrument);
         item.setDisplayCount(instrument.getDisplayCount());
         item.setItemSequence(++itemSequence);
         instrument.getItemCollection().add(item);
         itemHash.put(varName, item);
     }
     
     /**
      * Based upon response from user, set answer for a given variable
      * @param varName
      * @param answer
      */
     public void writeAnswer(String varName, String answer) {
         if (!initialized) {
             return;
         }
         
         if (itemHash.containsKey(varName)) {
            Item item = itemHash.get(varName);
             item.setAnswer(answer);
         }
     }
     
     /**
      * After settting all answers, and creating new questions, update instrument
      */
     public void finishServerProcessing() {
         if (!initialized) {
             return;
         }
         
         instrumentFacade.edit(instrument);
     }
 }
