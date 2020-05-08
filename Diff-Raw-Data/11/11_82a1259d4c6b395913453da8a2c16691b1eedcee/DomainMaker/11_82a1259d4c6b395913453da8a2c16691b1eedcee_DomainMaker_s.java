 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.dns.editor;
 
 import static org.ref_send.promise.eventual.Eventual.near;
 import static org.ref_send.var.Variable.var;
 
 import java.io.Serializable;
 
 import org.joe_e.Powerless;
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.ref_send.var.Factory;
 import org.ref_send.var.Variable;
 import org.waterken.dns.Domain;
 import org.waterken.dns.Resource;
 import org.waterken.menu.Menu;
 import org.waterken.menu.MenuMaker;
 import org.web_send.graph.Framework;
 
 /**
  * A {@link Domain} implementation.
  */
 public final class
 DomainMaker {
     
     private
     DomainMaker() {}
     
     /**
      * Constructs an instance.
      * @param framework model framework
      */
    static public DomainMaster
     build(final Framework framework) {
         final Menu<Resource> answers = MenuMaker.make(8, new RVF());
         class DomainX extends Struct implements Domain, Serializable {
             static private final long serialVersionUID = 1L;
 
             public ConstArray<Resource>
             getAnswers() { return near(answers.getSnapshot()); }
         }
         framework.publisher.bind(Domain.name, new DomainX());
        return new DomainMaster(framework.destruct, answers,
                                new ExtensionX(framework, answers));
     }
     
     static final class
     RVF extends Factory<Variable<Resource>> implements Powerless, Serializable {
         static private final long serialVersionUID = 1L;
 
         public Variable<Resource>
         run() {
             final Resource initial = new Resource(Resource.A, Resource.IN,
                 ResourceGuard.minTTL, ByteArray.array(new byte[]{ 127,0,0,1 })); 
             return var(initial, new ResourceGuard());
         }
     }
 }
