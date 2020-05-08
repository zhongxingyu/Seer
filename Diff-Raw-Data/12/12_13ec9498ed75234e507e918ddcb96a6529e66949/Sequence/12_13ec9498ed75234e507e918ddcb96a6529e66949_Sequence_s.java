 package org.qnot.pwrecta;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Sequence {
    public static String DEFAULT_SEQUENCE = "4-S";
     
     private List<SequenceItem> items;
     
     public Sequence() {
         this.items = new ArrayList<SequenceItem>();
     }
     
     public void addItem(SequenceItem item) {
         this.items.add(item);
     }
     
     public List<SequenceItem> getItemList() {
         return this.items;
     }
     
     // 1-up;2-down;3-diag;4-left;
     public static Sequence fromString(String seq) throws SequenceParseException {
         Sequence sequence = new Sequence();
         if(seq == null || seq.length() == 0) {
             throw new SequenceParseException("Empty sequence string");
         }
         
         String[] items = seq.split(";");
         if (items == null || items.length == 0) {
             throw new SequenceParseException("No sequence items found. Please be sure to separate items with a ';'");
         }
         
         for(String i : items) {
             String[] spec = i.split("-");
             if (spec == null || spec.length != 2) {
                 throw new SequenceParseException("Invalid sequence item. Format should be [length]-[direction]");
             }
 
             Direction direction = Direction.fromString(spec[1]);
             Integer len = null;
             try {
                 len = Integer.valueOf(spec[0]);
             } catch(NumberFormatException e) {
                 throw new SequenceParseException("Invalid sequence length. Please provide an integer");
             }
             
             sequence.addItem(new SequenceItem(direction, len.intValue()));
         }
         
         return sequence;
     }
 }
