 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 
 import org.joe_e.Immutable;
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.list.List;
 import org.ref_send.promise.Eventual;
 import org.ref_send.promise.Log;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 import org.waterken.db.Creator;
 import org.waterken.db.Database;
 import org.waterken.db.Effect;
 import org.waterken.db.Root;
 import org.waterken.db.Transaction;
 import org.waterken.http.Server;
 import org.waterken.syntax.Exporter;
 import org.waterken.syntax.json.JSONDeserializer;
 import org.waterken.syntax.json.JSONSerializer;
 
 /**
  * The vat initialization transaction.
  */
 public final class
 VatInitializer extends Struct implements Transaction<PowerlessArray<String>> {
 
     private final Method make;      // maker method
     private final String base;      // base URL for JSON serialization
     private final ByteArray body;   // JSON serialized arguments
     
     protected
     VatInitializer(final Method make, final String base, final ByteArray body) {
         this.make = make;
         this.base = base;
         this.body = body;
     }
     
     public PowerlessArray<String>
     run(final Root local) throws Exception {
         final Log log = local.fetch(null, Database.log);
         final Receiver<Effect<Server>> effect=local.fetch(null,Database.effect);
         
         final List<Promise<?>> tasks = List.list();
         final Receiver<?> destruct = local.fetch(null, Database.destruct);
         final Outbound outbound = new Outbound();
         final HTTP.Exports exports = HTTP.make(enqueue(effect,tasks), local,
                 log, destruct, Eventual.ref(outbound));
        log.got(exports.getHere() + "#make", null, make);
         local.link(VatInitializer.tasks, tasks);
         local.link(VatInitializer.outbound, outbound);
         local.link(VatInitializer.exports, exports);
         local.link(VatInitializer.sessions, new SessionMaker(local));
         local.link(Database.wake, wake(tasks, outbound, effect));
         final ConstArray<Type> signature =
             ConstArray.array(make.getGenericParameterTypes());
         final Object[] argv = new Object[signature.length()];
         ConstArray<Type> parameters = signature;
         if (0 != parameters.length() && Eventual.class == parameters.get(0)) {
             parameters = parameters.without(0);
             argv[0] = exports._;
         }
         final ConstArray<?> optional = new JSONDeserializer().run(base,
             exports.connect(), parameters, exports.getCodebase(),
             body.asInputStream());
         for (int i = optional.length(), j = argv.length; 0 != i;) {
             argv[--j] = optional.get(--i);
         }
         final Object top = Reflection.invoke(make, null, argv);
         local.link(Database.top, top);
         final Exporter export =
             HTTP.changeBase(exports.getHere(), exports.export(), base);
        return PowerlessArray.array(export.run(top), export.run(destruct));
     }
     
     static public String
     create(final Database<Server> parent, final String project,
            final String base, final String label,
            final Class<?> maker, final Object... argv) throws Exception {
         final Method make = HTTP.dispatch(maker, "make");
         final ByteArray body =
             new JSONSerializer().run(null, ConstArray.array(argv)); 
         return parent.enter(Transaction.update,
                             new Transaction<PowerlessArray<String>>() {
             public PowerlessArray<String>
             run(final Root local) throws Exception {
                 final Creator creator = local.fetch(null, Database.creator);
                 return creator.run(project, base, label,
                                    new VatInitializer(make, null, body)).call();
             }
        }).call().get(0);
     }
     
     static private Receiver<Promise<?>>
     enqueue(final Receiver<Effect<Server>> effect,final List<Promise<?>> tasks){
         class Enqueue extends Struct
                       implements Receiver<Promise<?>>, Serializable {
             static private final long serialVersionUID = 1L;
 
             public void
             run(final Promise<?> task) {
                 if (tasks.isEmpty()) { effect.run(runTask()); }
                 tasks.append(task);
             }
         }
         return new Enqueue();
     }
     
     static private Receiver<?>
     wake(final List<Promise<?>> tasks, final Outbound outbound,
          final Receiver<Effect<Server>> effect) {
         class Wake extends Struct implements Receiver<Object>, Serializable {
             static private final long serialVersionUID = 1L;
             
             public void
             run(final Object ignored) {
                 if (!tasks.isEmpty()) { effect.run(runTask()); }
                 for (final Pipeline x : outbound.getPending()) { x.resend(); }
             }
         }
         return new Wake();
     }
     
     static private Effect<Server>
     runTask() {
         return new Effect<Server>() {
             public void
             run(final Database<Server> vat) throws Exception {
                 vat.enter(Transaction.update, new Transaction<Immutable>() {
                     public Immutable
                     run(final Root local) throws Exception {
                         final List<Promise<?>> tasks =
                             local.fetch(null, VatInitializer.tasks);
                         final Promise<?> task = tasks.pop();
                         if (!tasks.isEmpty()) {
                             final Receiver<Effect<Server>> effect =
                                 local.fetch(null, Database.effect);
                             effect.run(runTask());
                         }
                         task.call();
                         return new Immutable() {};
                     }
                 }).call();
             }
         };
     }
 
     /**
      * key bound to the session maker in all vats
      */
     static private   final String sessions = "sessions";
     
     static protected final String outbound = ".outbound";
     static private   final String tasks = ".tasks";
     static public    final String exports = ".exports";
 }
