 package org.motechproject.ananya.kilkari.domain;
 
import java.io.Serializable;

public class SubscriptionActivationRequest implements Serializable {
     private final String msisdn;
     private final SubscriptionPack pack;
     private final Channel channel;
 
     public SubscriptionActivationRequest(String msisdn, SubscriptionPack pack, Channel channel) {
         this.msisdn = msisdn;
         this.pack = pack;
         this.channel = channel;
     }
 
     public String getMsisdn() {
         return msisdn;
     }
 
     public SubscriptionPack getPack() {
         return pack;
     }
 
     public Channel getChannel() {
         return channel;
     }
 }
