 /**
  * Copyright (c) 2009-2011, netBout.com
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are PROHIBITED without prior written permission from
  * the author. This product may NOT be used anywhere and on any computer
  * except the server platform of netBout Inc. located at www.netbout.com.
  * Federal copyright law prohibits unauthorized reproduction by any means
  * and imposes fines up to $25,000 for violation. If you received
  * this code occasionally and without intent to use it, please report this
  * incident to the author by email.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package com.netbout.inf.predicates;
 
 import com.netbout.inf.Meta;
 import com.netbout.inf.Msg;
 import com.netbout.inf.Predicate;
 import com.netbout.spi.Message;
 import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
import java.util.TreeSet;
 
 /**
  * Allows only bundled messages.
  *
  * @author Yegor Bugayenko (yegor@netbout.com)
  * @version $Id$
  */
 @Meta(name = "bundled", extracts = true)
 public final class BundledPred extends AbstractVarargPred {
 
     /**
      * Bundle marker.
      */
     public static final String BUNDLE = "bundle";
 
     /**
      * List of already passed bundles.
      */
     private final transient Set<String> passed = new HashSet<String>();
 
     /**
      * Public ctor.
      * @param args The arguments
      */
     public BundledPred(final List<Predicate> args) {
         super(args);
     }
 
     /**
      * Extracts necessary data from message.
      * @param from The message to extract from
      * @param msg Where to extract
      */
     public static void extract(final Message from, final Msg msg) {
        final Set<Urn> names = new TreeSet<Urn>();
         for (Participant dude : from.bout().participants()) {
            names.add(dude.identity().name());
         }
        msg.put(BundledPred.BUNDLE, Logger.format("%[list]s", names));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object evaluate(final Msg msg, final int pos) {
         final String bundle = msg.<String>get(this.BUNDLE);
         boolean allow;
         if (this.passed.contains(bundle)) {
             allow = false;
         } else {
             this.passed.add(bundle);
             allow = true;
         }
         return allow;
     }
 
 }
