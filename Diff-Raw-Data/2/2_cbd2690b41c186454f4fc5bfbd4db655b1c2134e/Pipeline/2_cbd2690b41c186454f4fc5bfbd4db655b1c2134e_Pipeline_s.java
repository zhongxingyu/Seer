 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import static org.ref_send.promise.Fulfilled.ref;
 import static org.web_send.Entity.maxContentSize;
 
 import java.io.Serializable;
 
 import org.joe_e.Struct;
 import org.ref_send.list.List;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Loop;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.io.limited.Limited;
 import org.waterken.io.snapshot.Snapshot;
 import org.waterken.remote.Remoting;
 import org.waterken.vat.Effect;
 import org.waterken.vat.Root;
 import org.waterken.vat.Service;
 import org.waterken.vat.Transaction;
 import org.waterken.vat.Vat;
 import org.web_send.Failure;
 
 /**
  * Manages a pending request queue for a specific host.
  */
 final class
 Pipeline implements Serializable {
     static private final long serialVersionUID = 1L;
 
     static private final class
     Entry extends Struct implements Serializable {
         static private final long serialVersionUID = 1L;
 
         final int id;       // serial number
         final Message msg;  // pending message
         
         Entry(final int id, final Message message) {
             this.id = id;
             this.msg = message;
         }
     }
     
     private final String peer;
     private final Loop<Effect> effect;
     private final Vat model;
     private final Fulfilled<Outbound> outbound;
     
     private final List<Entry> pending = List.list();
     private       int serialMID = 0;
     private       int halts = 0;    // number of pending pipeline flushes
     private       int queries = 0;  // number of queries after the last flush
     
     Pipeline(final String peer, final Root local) {
         this.peer = peer;
         effect = (Loop<Effect>)local.fetch(null, Root.effect);
         model = (Vat)local.fetch(null, Root.vat);
         outbound = Fulfilled.detach((Outbound)local.fetch(null, AMP.outbound));
     }
 
     void
     resend() { effect.run(restart(model, peer, pending.getSize(), false, 0)); }
     
     /*
      * Message sending is halted when an Update follows a Query. Sending resumes
      * once the response to the preceeding Query has been received.
      */
     
     void
     enqueue(final Message message) {
         if (pending.isEmpty()) {
             outbound.cast().add(peer, this);
         }
         final int mid = serialMID++;
         pending.append(new Entry(mid, message));
         if (message instanceof Update) {
             if (0 != queries) {
                 ++halts;
                 queries = 0;
             }
         }
         if (message instanceof Query) { ++queries; }
         if (0 == halts) { effect.run(restart(model, peer, 1, true, mid)); }
     }
     
     private Message
     dequeue(final int mid) {
         if (pending.getFront().id != mid) { throw new RuntimeException(); }
         
         final Entry front = pending.pop();
         if (pending.isEmpty()) {
             outbound.cast().remove(peer);
         }
         if (front.msg instanceof Query) {
             if (0 == halts) {
                 --queries;
             } else {
                 int max = pending.getSize();
                 for (final Entry x : pending) {
                     if (x.msg instanceof Update) {
                         --halts;
                         effect.run(restart(model, peer, max, true, x.id));
                         break;
                     }
                     if (x.msg instanceof Query) { break; }
                     --max;
                 }
             }
         }
         return front.msg;
     }
     
     static private Effect
     restart(final Vat vat, final String peer,
             final int max, final boolean skipTo, final int mid) {
         // Create a transaction effect that will schedule a new extend
         // transaction that actually puts the messages on the wire.
         return new Effect() {
            public void
            run() throws Exception {
                vat.service.run(new Service() {
                    public void
                    run() throws Exception {
                        vat.enter(Vat.extend, new Transaction<Void>() {
                            public Void
                            run(final Root local) throws Exception {
                                final Server client =
                                    (Server)local.fetch(null, Remoting.client);
                                final Loop<Effect> effect =
                                   (Loop)local.fetch(null, Root.effect);
                                final Outbound outbound =
                                    (Outbound)local.fetch(null, AMP.outbound);
                                boolean found = !skipTo;
                                boolean q = false;
                                int n = max;
                                for (final Entry x: outbound.find(peer).pending){
                                    if (!found) {
                                        if (mid == x.id) {
                                            found = true;
                                        } else {
                                            continue;
                                        }
                                    }
                                    if (0 == n--) { break; }
                                    if (q && x.msg instanceof Update) { break; }
                                    if (x.msg instanceof Query) { q = true; }
                                    effect.run(send(vat, client, peer, x));
                                }
                                return null;
                            }
                        });
                    }
                });
            }
         };
     }
     
     static private Effect
     send(final Vat vat, final Server client, final String peer, final Entry x) {
         Promise<Request> rendered;
         try {
             rendered = ref(x.msg.send());
         } catch (final Exception reason) {
             rendered = new Rejected<Request>(reason);
         }
         final Promise<Request> request = rendered;
         final int mid = x.id;
         return new Effect() {
            public void
            run() throws Exception {
                client.serve(peer, request, new Receive(vat, peer, mid));
            }
         };
     }
     
     static private final class
     Receive extends Do<Response,Void> {
         
         private final Vat vat;
         private final String peer;
         private final int mid;
         
         Receive(final Vat vat, final String peer, final int mid) {
             this.vat = vat;
             this.peer = peer;
             this.mid = mid;
         }
 
         public Void
         fulfill(Response r) throws Exception {
             if (null != r.body) {
                 try {
                     final int length = r.getContentLength();
                     if (length > maxContentSize) { throw Failure.tooBig(); }
                     r = new Response(r.version, r.status, r.phrase, r.header,
                         Snapshot.snapshot(length < 0 ? 1024 : length,
                             Limited.limit(maxContentSize, r.body)));
                 } catch (final Failure e) { return reject(e); }
             }
             return resolve(ref(r));
         }
         
         public Void
         reject(final Exception reason) {
             return resolve(new Rejected<Response>(reason));
         }
         
         private Void
         resolve(final Promise<Response> response) {
             vat.service.run(new Service() {
                 public void
                 run() throws Exception {
                     vat.enter(Vat.change, new Transaction<Void>() {
                         public Void
                         run(final Root local) throws Exception {
                             final Outbound outbound =
                                 (Outbound)local.fetch(null, AMP.outbound);
                             final Pipeline msgs = outbound.find(peer);
                             final Message respond = msgs.dequeue(mid);
                             Response value;
                             try {
                                 value = response.cast();
                             } catch (final Exception reason) {
                                 return respond.reject(reason);
                             }
                             return respond.fulfill(value);
                         }
                     });
                 }
             });
             return null;
         }
     }
 }
