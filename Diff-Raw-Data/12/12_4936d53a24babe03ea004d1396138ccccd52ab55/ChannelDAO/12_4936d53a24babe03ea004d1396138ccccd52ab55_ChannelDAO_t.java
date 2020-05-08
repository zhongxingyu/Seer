 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVACAO, SA (PTIN), IBM Corp.,
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package si.setcce.societies.crowdtasking.model.dao;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.NotFoundException;
 import com.googlecode.objectify.Ref;
 import si.setcce.societies.crowdtasking.model.Channel;
 
 import static si.setcce.societies.crowdtasking.model.dao.OfyService.ofy;
 
 /**
  * Describe your class here...
  *
  * @author Simon Jureša
  */
 public final class ChannelDAO {
     private ChannelDAO() {
     }
 
     public static Channel load(Long id) {
         Channel cs = null;
         try {
             Object obj = ofy().load().type(Channel.class).id(id).get();
            if (obj != null) {
                 cs = (Channel) obj;
             } else {
                 ofy().clear();
                 cs = ofy().load().type(Channel.class).id(id).get();
             }
        } catch (NotFoundException ignored) {
         }
         return cs;
     }
 
     public static Channel loadChannelByNumber(Long channelNumber) {
         return ofy().load().type(Channel.class).filter("channelNumber", channelNumber).first().get();
     }
 
     public static Channel load(Ref<Channel> csRef) {
         return ofy().load().ref(csRef).get();
     }
 
     public static void delete(Long id) {
         ofy().delete().type(Channel.class).id(id).now();
     }
 
/*
     public static List<Channel> loadChannels() {
         return ofy().load().type(Channel.class).list();
     }
 
     public static List<Channel> loadChannels(int limit) {
         return ofy().load().type(Channel.class).limit(limit).list();
     }
*/
 
     public static Channel save(Channel cs) {
         Key<Channel> key = ofy().save().entity(cs).now();
         return ofy().load().key(key).get();
     }
 }
