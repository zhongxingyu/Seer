 package org.aldeon.dht.interest;
 
 import org.aldeon.dht.interest.orders.Order;
 import org.aldeon.dht.interest.orders.TopicOrderLine;
 import org.aldeon.dht.interest.orders.TopicOrderLineImpl;
 import org.aldeon.model.Identifier;
 import org.aldeon.networking.common.AddressType;
 import org.aldeon.networking.common.PeerAddress;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class AddressTypeIgnoringInterestTracker implements InterestTracker {
 
     private Map<Identifier, TopicOrderLine> lines = new ConcurrentHashMap<>();
 
     @Override
     public void addAddress(PeerAddress address, Identifier topic) {
        System.out.println("INTADD: " + address + " TOPIC: " + topic);
         line(topic).addAddress(address);
     }
 
     @Override
     public void delAddress(PeerAddress address, Identifier topic) {
        System.out.println("INTDEL: " + address + " TOPIC: " + topic);
         line(topic).delAddress(address);
         clean(topic);
     }
 
     @Override
     public void delAddress(PeerAddress address) {
        System.out.println("INTDEL: " + address);
         Iterator<TopicOrderLine> it = lines.values().iterator();
         while(it.hasNext()) {
             TopicOrderLine line = it.next();
             line.delAddress(address);
             if(line.isEmpty()) {
                 it.remove();
             }
         }
     }
 
     @Override
     public Set<PeerAddress> getInterested(AddressType addressType, Identifier topic, int maxResults) {
         Set<PeerAddress> result = line(topic).getAddresses(maxResults);
        System.out.println("INTSET: " + result.size());
         clean(topic);
         return result;
     }
 
     @Override
     public void placeOrder(Order order) {
         line(order.topic()).addOrder(order);
     }
 
     @Override
     public void revokeOrder(Order order) {
         line(order.topic()).delOrder(order);
         clean(order.topic());
     }
 
     private TopicOrderLine line(Identifier topic) {
         TopicOrderLine line = lines.get(topic);
         if(line == null) {
             line = new TopicOrderLineImpl();
             lines.put(topic, line);
         }
         return line;
     }
 
     private void clean(Identifier topic) {
         TopicOrderLine line = lines.get(topic);
         if(line != null) {
             if(line.isEmpty()) {
                 lines.remove(topic);
             }
         }
     }
 
     @Override
     public int getDemand(AddressType addressType, Identifier topic) {
         TopicOrderLine line = lines.get(topic);
         return (line == null) ? 0 : line.getDemand();
     }
 }
