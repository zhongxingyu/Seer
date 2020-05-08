 package org.agilewiki.jid.collection.vlenc;
 
 import junit.framework.TestCase;
 
 public class ListTest extends TestCase {
     public void test() throws Exception {
        /*
         JAFactoryLocator factoryLocator = JidFactories.createNoOsgiFactoryLocator(1);
         JABundleContext jaBundleContext = JABundleContext.getJABundleContext(factoryLocator);
         try {
             JAFuture future = new JAFuture();
             Actor l0 = factoryLocator.newActor(JidFactories.STRING_LIST_JID_TYPE);
             int l0sl = GetSerializedLength.req.send(future, l0);
             assertEquals(8, l0sl);
             Actor l1 = (new CopyJID()).send(future, l0);
             int l1sl = GetSerializedLength.req.send(future, l1);
             assertEquals(8, l1sl);
             (new IAdd(0)).send(future, l1);
             l1sl = GetSerializedLength.req.send(future, l1);
             assertEquals(12, l1sl);
             Actor l2 = (new CopyJID()).send(future, l1);
             int l2sl = GetSerializedLength.req.send(future, l2);
             assertEquals(12, l2sl);
            Actor s0 = factoryLocator.newActor(JidFactories.STRING_LIST_JID_TYPE);
             (new SetString("Hi")).send(future, s0);
             int s0sl = GetSerializedLength.req.send(future, s0);
             assertEquals(8, s0sl);
             byte[] s0bs = GetSerializedBytes.req.send(future, s0);
             assertEquals(8, s0bs.length);
             (new IAddBytes(-1, s0bs)).send(future, l2);
             l2sl = GetSerializedLength.req.send(future, l2);
             assertEquals(20, l2sl);
             (new ISetBytes(0, s0bs)).send(future, l2);
             l2sl = GetSerializedLength.req.send(future, l2);
             assertEquals(24, l2sl);
             (new IRemove(0)).send(future, l2);
             l2sl = GetSerializedLength.req.send(future, l2);
             assertEquals(16, l2sl);
             Empty.req.send(future, l2);
             l2sl = GetSerializedLength.req.send(future, l2);
             assertEquals(8, l2sl);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             jaBundleContext.stop(0);
         }
        */
     }
 }
