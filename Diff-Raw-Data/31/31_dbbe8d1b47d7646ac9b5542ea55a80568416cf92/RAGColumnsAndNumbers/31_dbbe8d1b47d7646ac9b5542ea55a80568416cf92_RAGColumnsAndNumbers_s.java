 package org.paules.geckoboard.api.widget;
 
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.paules.geckoboard.api.Push;
 import org.paules.geckoboard.api.error.ValidationException;
 import org.paules.geckoboard.api.json.GraphType;
 
 public class RAGColumnsAndNumbers extends Push {
     private static final class Item {
         private final String text;
 
         private final int    value;
 
         private final String prefix;
 
         public Item( String text, int value ) {
             super();
             this.text = text;
             this.value = value;
             prefix = null;
         }
 
         public Item( String text, int value, String prefix ) {
             super();
             this.text = text;
             this.value = value;
             this.prefix = prefix;
         }
 
         public String getPrefix() {
             return prefix;
         }
 
         public String getText() {
             return text;
         }
 
         public int getValue() {
             return value;
         }
     }
 
     private Item      red;
 
     private Item      amber;
 
     private Item      green;
 
     private GraphType graphType;
 
     public RAGColumnsAndNumbers( String widgetKey, GraphType graphType ) {
         super( widgetKey );
         this.graphType = graphType;
     }
    
     @Override
     protected void validate() throws ValidationException {
     }
 
     @Override
     protected void getData( ObjectNode data ) {
         data.put( "type", graphType.toString().toLowerCase() );
         ArrayNode items = data.arrayNode();
         data.put( "item", items );
        ObjectNode red = data.objectNode();
        red.put( "value", this.red.getValue() );
        red.put( "text", this.red.getText() );
        if ( this.red.getPrefix() != null ) {
            red.put( "prefix", this.red.getPrefix() );
         }
        items.add( red );
 
         ObjectNode amber = data.objectNode();
         amber.put( "value", this.amber.getValue() );
         amber.put( "text", this.amber.getText() );
         if ( this.amber.getPrefix() != null ) {
             amber.put( "prefix", this.amber.getPrefix() );
         }
         items.add( amber );
 
        ObjectNode green = data.objectNode();
        green.put( "value", this.green.getValue() );
        green.put( "text", this.green.getText() );
        if ( this.green.getPrefix() != null ) {
            green.put( "prefix", this.green.getPrefix() );
         }
        items.add( green );
     }
 
     public void setAmber( String label, int value ) {
         amber = new Item( label, value );
     }
 
     public void setAmber( String label, int value, String prefix ) {
         amber = new Item( label, value, prefix );
     }
 
     public void setGreen( String label, int value ) {
         green = new Item( label, value );
     }
 
     public void setGreen( String label, int value, String prefix ) {
         green = new Item( label, value, prefix );
     }
 
     public void setRed( String label, int value ) {
         red = new Item( label, value );
     }
 
     public void setRed( String label, int value, String prefix ) {
         red = new Item( label, value, prefix );
     }
 }
