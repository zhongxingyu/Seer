 package com.cluedoassist;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import java.io.Serializable;
 
 public final class Accusation implements LogEntry {
 
     public final String asker;
 
     public final List<Card> cards;
 
     public Accusation(String asker, List<Card> cards) {
        if (asker == null || "".equals(asker)) {
            throw new IllegalArgumentException("asker can't be null or empty");
        }
        if (cards == null) {
            throw new IllegalArgumentException("cards can't be null");
        }
        if (cards.size() != 3) {
            throw new IllegalArgumentException(
                                        "there should be exactly three cards");
        }
         this.asker = asker;
 
         this.cards = Collections.unmodifiableList(new ArrayList<Card>(cards));
     }
 
     @Override
     public String toString() {
         return "Accusation: " + asker + " " + cards;
     }
 }
