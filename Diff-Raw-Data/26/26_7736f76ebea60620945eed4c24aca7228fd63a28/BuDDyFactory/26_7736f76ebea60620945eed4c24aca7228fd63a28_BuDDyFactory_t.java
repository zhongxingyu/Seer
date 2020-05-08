 package org.sf.javabdd;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * An implementation of BDDFactory that relies on the BuDDy library through a
  * native interface.  You can use this by calling the "BuDDyFactory.init()"
  * method with the desired arguments.  This will return you an instance of the
  * BDDFactory class that you can use.  Call "done()" on that instance when you
  * are finished.
  * 
  * This class (and the BuDDy library) do NOT support multithreading.
  * Furthermore, there can be only one instance active at a time.  You can only
  * call "init()" again after you have called "done()" on the original instance.
  * 
  * @see org.sf.javabdd.BDDFactory
  * 
  * @author John Whaley
 * @version $Id: BuDDyFactory.java,v 1.8 2003/02/02 11:30:41 joewhaley Exp $
  */
 public class BuDDyFactory extends BDDFactory {
 
     public static BDDFactory init(int nodenum, int cachesize) {
         if (INSTANCE != null) {
             throw new InternalError("Error: BDDFactory already initialized.");
         }
         INSTANCE = new BuDDyFactory();
         INSTANCE.initialize(nodenum, cachesize);
         return INSTANCE;
     }
     
     private static BuDDyFactory INSTANCE;
     
     static {
         System.loadLibrary("buddy");
         registerNatives();
     }
     
     private static native void registerNatives();
     
     private BuDDyFactory() {}
 
     /**
      * @see org.sf.javabdd.BDDFactory#zero()
      */
     public BDD zero() { return _getZero(); }
 
     protected native BuDDyBDD _getZero();
 
     /**
      * @see org.sf.javabdd.BDDFactory#one()
      */
     public BDD one() { return _getOne(); }
 
     protected native BuDDyBDD _getOne();
     
     /**
      * @see org.sf.javabdd.BDDFactory#buildCube(int, int, java.util.Collection)
      */
     public BDD buildCube(int value, int width, Collection var) {
         return this.buildCube(value, width, (BDD[]) var.toArray(new BDD[var.size()]));
     }
     
     private native BDD buildCube(int value, int width, BDD[] var);
 
     /**
      * @see org.sf.javabdd.BDDFactory#buildCube(int, int, int[])
      */
     public native BDD buildCube(int value, int width, int[] var);
 
     /**
      * @see org.sf.javabdd.BDDFactory#makeSet(int[])
      */
     public native BDD makeSet(int[] v);
     
     /**
      * @see org.sf.javabdd.BDDFactory#initialize(int, int)
      */
     protected native void initialize(int nodenum, int cachesize);
 
     /**
      * @see org.sf.javabdd.BDDFactory#isInitialized()
      */
     public native boolean isInitialized();
 
     /**
      * @see org.sf.javabdd.BDDFactory#done()
      */
     public void done() {
         INSTANCE = null;
         _done();
     }
 
     protected native void _done();
 
     /**
      * @see org.sf.javabdd.BDDFactory#setMaxNodeNum(int)
      */
     public native int setMaxNodeNum(int size);
 
     /**
      * @see org.sf.javabdd.BDDFactory#setMinFreeNodes(int)
      */
     public native void setMinFreeNodes(int x);
 
     /**
      * @see org.sf.javabdd.BDDFactory#setMaxIncrease(int)
      */
     public native int setMaxIncrease(int x);
 
     /**
      * @see org.sf.javabdd.BDDFactory#setCacheRatio(int)
      */
     public native int setCacheRatio(int x);
 
     /**
      * @see org.sf.javabdd.BDDFactory#varNum()
      */
     public native int varNum();
 
     /**
      * @see org.sf.javabdd.BDDFactory#setVarNum(int)
      */
     public native int setVarNum(int num);
 
     /**
      * @see org.sf.javabdd.BDDFactory#extVarNum(int)
      */
     public native int extVarNum(int num);
 
     /**
      * @see org.sf.javabdd.BDDFactory#ithVar(int)
      */
     public native BDD ithVar(int var);
 
     /**
      * @see org.sf.javabdd.BDDFactory#nithVar(int)
      */
     public native BDD nithVar(int var);
 
     /**
      * @see org.sf.javabdd.BDDFactory#swapVar(int, int)
      */
     public native void swapVar(int v1, int v2);
 
     /**
      * @see org.sf.javabdd.BDDFactory#makePair()
      */
     public native BDDPairing makePair();
     
     /**
      * @see org.sf.javabdd.BDDFactory#printAll()
      */
     public native void printAll();
 
     /**
      * @see org.sf.javabdd.BDDFactory#printTable(org.sf.javabdd.BDD)
      */
     public native void printTable(BDD b);
 
     /**
      * @see org.sf.javabdd.BDDFactory#load(java.lang.String)
      */
     public native BDD load(String filename);
 
     /**
      * @see org.sf.javabdd.BDDFactory#save(java.lang.String, org.sf.javabdd.BDD)
      */
     public native void save(String filename, BDD v);
 
     /**
      * @see org.sf.javabdd.BDDFactory#level2Var(int)
      */
     public native int level2Var(int level);
 
     /**
      * @see org.sf.javabdd.BDDFactory#var2level(int)
      */
     public native int var2level(int var);
 
     /**
      * @see org.sf.javabdd.BDDFactory#reorder(org.sf.javabdd.BDDFactory.ReorderMethod)
      */
     public native void reorder(BDDFactory.ReorderMethod m);
 
     /**
      * @see org.sf.javabdd.BDDFactory#autoReorder(org.sf.javabdd.BDDFactory.ReorderMethod)
      */
     public native void autoReorder(BDDFactory.ReorderMethod method);
 
     /**
      * @see org.sf.javabdd.BDDFactory#autoReorder(org.sf.javabdd.BDDFactory.ReorderMethod, int)
      */
     public native void autoReorder(BDDFactory.ReorderMethod method, int max);
 
     /**
      * @see org.sf.javabdd.BDDFactory#getReorderMethod()
      */
     public native BDDFactory.ReorderMethod getReorderMethod();
 
     /**
      * @see org.sf.javabdd.BDDFactory#getReorderTimes()
      */
     public native int getReorderTimes();
 
     /**
      * @see org.sf.javabdd.BDDFactory#disableReorder()
      */
     public native void disableReorder();
 
     /**
      * @see org.sf.javabdd.BDDFactory#enableReorder()
      */
     public native void enableReorder();
 
     /**
      * @see org.sf.javabdd.BDDFactory#reorderVerbose(int)
      */
     public native int reorderVerbose(int v);
 
     /**
      * @see org.sf.javabdd.BDDFactory#setVarOrder(int[])
      */
     public native void setVarOrder(int[] neworder);
     
     /**
      * @see org.sf.javabdd.BDDFactory#addVarBlock(org.sf.javabdd.BDD, boolean)
      */
     public native void addVarBlock(BDD var, boolean fixed);
 
     /**
      * @see org.sf.javabdd.BDDFactory#addVarBlock(int, int, boolean)
      */
     public native void addVarBlock(int first, int last, boolean fixed);
 
     /**
      * @see org.sf.javabdd.BDDFactory#varBlockAll()
      */
     public native void varBlockAll();
 
     /**
      * @see org.sf.javabdd.BDDFactory#clearVarBlocks()
      */
     public native void clearVarBlocks();
 
     /**
      * @see org.sf.javabdd.BDDFactory#printOrder()
      */
     public native void printOrder();
 
     /**
      * @see org.sf.javabdd.BDDFactory#nodeCount(java.util.Collection)
      */
     public native int nodeCount(Collection r);
 
     /**
      * @see org.sf.javabdd.BDDFactory#getAllocNum()
      */
     public native int getAllocNum();
 
     /**
      * @see org.sf.javabdd.BDDFactory#getNodeNum()
      */
     public native int getNodeNum();
 
     /**
      * @see org.sf.javabdd.BDDFactory#reorderGain()
      */
     public native int reorderGain();
 
     /**
      * @see org.sf.javabdd.BDDFactory#printStat()
      */
     public native void printStat();
 
     /**
      * @see org.sf.javabdd.BDDFactory#extDomain(int[])
      */
    public native BDDDomain[] extDomain(int[] domainSizes);
     
     /**
      * @see org.sf.javabdd.BDDFactory#overlapDomain(org.sf.javabdd.BDDDomain, org.sf.javabdd.BDDDomain)
      */
    public native BDDDomain overlapDomain(BDDDomain d1, BDDDomain d2);
     
     /**
      * @see org.sf.javabdd.BDDFactory#makeSet(org.sf.javabdd.BDDDomain[])
      */
    public native BDD makeSet(BDDDomain[] v);
     
     /**
      * @see org.sf.javabdd.BDDFactory#clearAllDomains()
      */
    public native void clearAllDomains();
     
     /**
      * @see org.sf.javabdd.BDDFactory#numberOfDomains()
      */
    public native int numberOfDomains();
 
     /**
      * An implementation of a BDD class, used by the BuDDy interface.
      */
     public static class BuDDyBDD extends BDD {
     
         private int _id;
         
         private BuDDyBDD(int id) {
             this._id = id;
             this.addRef();
         }
         
         /**
          * @see org.sf.javabdd.BDD#var()
          */
         public native int var();
         
         /**
          * @see org.sf.javabdd.BDD#high()
          */
         public native BDD high();
         
         /**
          * @see org.sf.javabdd.BDD#low()
          */
         public native BDD low();
         
         /**
          * @see org.sf.javabdd.BDD#id()
          */
         public native BDD id();
         
         /**
          * @see org.sf.javabdd.BDD#not()
          */
         public native BDD not();
         
         /**
          * @see org.sf.javabdd.BDD#ite(org.sf.javabdd.BDD, org.sf.javabdd.BDD)
          */
         public native BDD ite(BDD thenBDD, BDD elseBDD);
         
         /**
          * @see org.sf.javabdd.BDD#relprod(org.sf.javabdd.BDD, org.sf.javabdd.BDD)
          */
         public native BDD relprod(BDD that, BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#compose(org.sf.javabdd.BDD, int)
          */
         public native BDD compose(BDD that, int var);
         
         /**
          * @see org.sf.javabdd.BDD#constrain(org.sf.javabdd.BDD)
          */
         public native BDD constrain(BDD that);
         
         /**
          * @see org.sf.javabdd.BDD#exist(org.sf.javabdd.BDD)
          */
         public native BDD exist(BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#forAll(org.sf.javabdd.BDD)
          */
         public native BDD forAll(BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#unique(org.sf.javabdd.BDD)
          */
         public native BDD unique(BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#restrict(org.sf.javabdd.BDD)
          */
         public native BDD restrict(BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#simplify(org.sf.javabdd.BDD)
          */
         public native BDD simplify(BDD d);
         
         /**
          * @see org.sf.javabdd.BDD#support()
          */
         public native BDD support();
         
         /**
          * @see org.sf.javabdd.BDD#apply(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp)
          */
         public native BDD apply(BDD that, BDDFactory.BDDOp opr);
         
         /**
          * @see org.sf.javabdd.BDD#applyWith(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp)
          */
         public native void applyWith(BDD that, BDDFactory.BDDOp opr);
         
         /**
          * @see org.sf.javabdd.BDD#applyAll(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp, org.sf.javabdd.BDD)
          */
         public native BDD applyAll(BDD that, BDDFactory.BDDOp opr, BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#applyEx(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp, org.sf.javabdd.BDD)
          */
         public native BDD applyEx(BDD that, BDDFactory.BDDOp opr, BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#applyUni(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp, org.sf.javabdd.BDD)
          */
         public native BDD applyUni(BDD that, BDDFactory.BDDOp opr, BDD var);
         
         /**
          * @see org.sf.javabdd.BDD#satOne()
          */
         public native BDD satOne();
         
         /**
          * @see org.sf.javabdd.BDD#fullSatOne()
          */
         public native BDD fullSatOne();
         
         /**
          * @see org.sf.javabdd.BDD#satOneSet(org.sf.javabdd.BDD, org.sf.javabdd.BDD)
          */
         public native BDD satOneSet(BDD var, BDD pol);
         
         /**
          * @see org.sf.javabdd.BDD#allsat()
          */
         public Iterator allsat() {
             return null;
         }
         
         /**
          * @see org.sf.javabdd.BDD#printSet()
          */
         public native void printSet();
         
         /**
          * @see org.sf.javabdd.BDD#printDot()
          */
         public native void printDot();
 
         /**
          * @see org.sf.javabdd.BDD#nodeCount()
          */
         public native int nodeCount();
         
         /**
          * @see org.sf.javabdd.BDD#pathCount()
          */
         public native double pathCount();
         
         /**
          * @see org.sf.javabdd.BDD#satCount()
          */
         public native double satCount();
         
         /**
          * @see org.sf.javabdd.BDD#satCount(org.sf.javabdd.BDD)
          */
         public native double satCount(BDD varset);
         
         /**
          * @see org.sf.javabdd.BDD#logSatCount()
          */
         public native double logSatCount();
         
         /**
          * @see org.sf.javabdd.BDD#varProfile()
          */
         public native int[] varProfile();
         
         /**
          * @see org.sf.javabdd.BDD#logSatCount(org.sf.javabdd.BDD)
          */
         public native double logSatCount(BDD varset);
         
         /**
          * @see org.sf.javabdd.BDD#addRef()
          */
         protected native void addRef();
         
         /**
          * @see org.sf.javabdd.BDD#delRef()
          */
         protected native void delRef();
         
         /**
          * @see java.lang.Object#finalize()
          */
         protected void finalize() throws Throwable {
             super.finalize();
             this.delRef();
         }
         
         /**
          * @see org.sf.javabdd.BDD#veccompose(org.sf.javabdd.BDDPairing)
          */
         public native BDD veccompose(BDDPairing pair);
         
         /**
          * @see org.sf.javabdd.BDD#scanSet()
          */
         public native int[] scanSet();
         
         /**
          * @see org.sf.javabdd.BDD#scanVar(int)
          */
         public native int scanVar(int var);
         
         /**
          * @see org.sf.javabdd.BDD#scanAllVar()
          */
         public native int[] scanAllVar();
         
         /**
          * @see org.sf.javabdd.BDD#replace(org.sf.javabdd.BDDPairing)
          */
         public native BDD replace(BDDPairing pair);
         
         /**
          * @see org.sf.javabdd.BDD#printSetWithDomains()
          */
         public native void printSetWithDomains();
         
         /**
          * @see org.sf.javabdd.BDD#equals(org.sf.javabdd.BDD)
          */
         public boolean equals(BDD that) {
             return this._id == ((BuDDyBDD) that)._id;
         }
         
         /**
          * @see org.sf.javabdd.BDD#hashCode()
          */
         public int hashCode() {
             return this._id;
         }
 
     }
     
     /**
      * An implementation of a BDDDomain, used by the BuDDy interface.
      */
     public static class BuDDyBDDDomain extends BDDDomain {
         private int _id;
         
         private BuDDyBDDDomain(int id) {
             this._id = id;
         }
             
         /**
          * @see org.sf.javabdd.BDDDomain#domain()
          */
         public native BDD domain();
         
         /**
          * @see org.sf.javabdd.BDDDomain#size()
          */
         public native int size();
         
         /**
          * @see org.sf.javabdd.BDDDomain#buildEquals(org.sf.javabdd.BDDDomain)
          */
         public native BDD buildEquals(BDDDomain that);
         
         /**
          * @see org.sf.javabdd.BDDDomain#set()
          */
         public native BDD set();
         
         /**
          * @see org.sf.javabdd.BDDDomain#ithVar(int)
          */
         public native BDD ithVar(int val);
         
         /**
          * @see org.sf.javabdd.BDDDomain#varNum()
          */
         public native int varNum();
         
         /**
          * @see org.sf.javabdd.BDDDomain#vars()
          */
         public native int[] vars();
         
     }
     
     /**
      * An implementation of a BDDPairing, used by the BuDDy interface.
      */
     public static class BuDDyBDDPairing extends BDDPairing {
         
         private long _ptr;
         
         private BuDDyBDDPairing(long ptr) {
                 this._ptr = ptr;
         }
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(int, int)
          */
         public native void set(int oldvar, int newvar);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(int[], int[])
          */
         public native void set(int[] oldvar, int[] newvar);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDD, org.sf.javabdd.BDD)
          */
         public native void set(BDD oldvar, BDD newvar);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDD[], org.sf.javabdd.BDD[])
          */
         public native void set(BDD[] oldvar, BDD[] newvar);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDDDomain, org.sf.javabdd.BDDDomain)
          */
         public native void set(BDDDomain p1, BDDDomain p2);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDDDomain[], org.sf.javabdd.BDDDomain[])
          */
         public native void set(BDDDomain[] p1, BDDDomain[] p2);
         
         /**
          * @see org.sf.javabdd.BDDPairing#reset()
          */
         public native void reset();
         
     }
     
 }
