 package knapsack;
 
 public class Item implements Comparable {
     private int price;
     private int weight;
     private float value;
 
     public Item() {
         this(1, 1);
     }
 
     public Item(int price, int weight) {
         this.price = price;
         this.weight = weight;
         calculateValue();
     }
 
     private void calculateValue() {
        value = new Float(price) / new Float(weight);
     }
 
     public boolean isMoreValuableThan(Item otherItem) {
         return getValue() > otherItem.getValue();
     }
 
     public float getValue() {
         return value;
     }
 
     public void setPrice(int price) {
         this.price = price;
         calculateValue();
     }
 
     public void setWeight(int weight) {
         this.weight = weight;
         calculateValue();
     }
 
     public int getWeight() {
         return weight;
     }
 
     public int getPrice() {
         return price;
     }
 
     @Override
     public int compareTo(Object o) {
         if (o instanceof Item) {
             Item otherItem = (Item) o;
             return new Float(getValue()).compareTo(otherItem.getValue());
         } else {
             return 0;
         }
     }
 
     public String toString() {
         return "Value:" + getValue() + " Price:" + getPrice() + " Weight:" + getWeight();
     }
 }
