 package org.sf.javabdd;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
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
 * @version $Id: BuDDyFactory.java,v 1.13 2003/04/18 22:30:31 joewhaley Exp $
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
        return this.buildCube(value, width, (BuDDyBDD[]) var.toArray(new BuDDyBDD[var.size()]));
     }
    private native BuDDyBDD buildCube(int value, int width, BuDDyBDD[] var);
 
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
     public void printTable(BDD b) { printTable((BuDDyBDD) b); }
     private native void printTable(BuDDyBDD b);
 
     /**
      * @see org.sf.javabdd.BDDFactory#load(java.lang.String)
      */
     public native BDD load(String filename);
 
     /**
      * @see org.sf.javabdd.BDDFactory#save(java.lang.String, org.sf.javabdd.BDD)
      */
     public void save(String filename, BDD v) { save(filename, (BuDDyBDD) v); }
     public native void save(String filename, BuDDyBDD v);
 
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
     public void addVarBlock(BDD var, boolean fixed) { addVarBlock((BuDDyBDD) var, fixed); }
     private native void addVarBlock(BuDDyBDD var, boolean fixed);
 
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
     public BDDDomain overlapDomain(BDDDomain d1, BDDDomain d2) {
         return overlapDomain((BuDDyBDDDomain) d1, (BuDDyBDDDomain) d2);
     }
     private native BuDDyBDDDomain overlapDomain(BuDDyBDDDomain d1, BuDDyBDDDomain d2);
     
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
      * @see org.sf.javabdd.BDDFactory#getDomain()
      */
     public BDDDomain getDomain(int i) {
         if (i < 0 || i >= numberOfDomains())
             throw new IndexOutOfBoundsException(i+" is out of range");
         return new BuDDyBDDDomain(i);
     }
 
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
         public BDD ite(BDD thenBDD, BDD elseBDD) {
             return ite((BuDDyBDD) thenBDD, (BuDDyBDD) elseBDD);
         }
         private native BuDDyBDD ite(BuDDyBDD thenBDD, BuDDyBDD elseBDD);
         
         /**
          * @see org.sf.javabdd.BDD#relprod(org.sf.javabdd.BDD, org.sf.javabdd.BDD)
          */
         public BDD relprod(BDD that, BDD var) {
             return relprod((BuDDyBDD) that, var);
         }
         public native BuDDyBDD relprod(BuDDyBDD that, BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#compose(org.sf.javabdd.BDD, int)
          */
         public BDD compose(BDD that, int var) {
             return compose((BuDDyBDD) that, var);
         }
         public native BuDDyBDD compose(BuDDyBDD that, int var);
         
         /**
          * @see org.sf.javabdd.BDD#constrain(org.sf.javabdd.BDD)
          */
         public BDD constrain(BDD that) {
             return constrain((BuDDyBDD) that);
         }
         public native BuDDyBDD constrain(BuDDyBDD that);
         
         /**
          * @see org.sf.javabdd.BDD#exist(org.sf.javabdd.BDD)
          */
         public BDD exist(BDD var) {
             return exist((BuDDyBDD) var);
         }
         public native BuDDyBDD exist(BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#forAll(org.sf.javabdd.BDD)
          */
         public BDD forAll(BDD var) {
             return forAll((BuDDyBDD) var);
         }
         public native BuDDyBDD forAll(BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#unique(org.sf.javabdd.BDD)
          */
         public BDD unique(BDD var) {
             return unique((BuDDyBDD) var);
         }
         public native BuDDyBDD unique(BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#restrict(org.sf.javabdd.BDD)
          */
         public BDD restrict(BDD var) {
             return restrict((BuDDyBDD) var);
         }
         public native BuDDyBDD restrict(BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#simplify(org.sf.javabdd.BDD)
          */
         public BDD simplify(BDD d) {
             return simplify((BuDDyBDD) d);
         }
         public native BuDDyBDD simplify(BuDDyBDD d);
         
         /**
          * @see org.sf.javabdd.BDD#support()
          */
         public native BDD support();
         
         /**
          * @see org.sf.javabdd.BDD#apply(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp)
          */
         public BDD apply(BDD that, BDDFactory.BDDOp opr) {
             return apply((BuDDyBDD) that, opr);
         }
         public native BuDDyBDD apply(BuDDyBDD that, BDDFactory.BDDOp opr);
         
         /**
          * @see org.sf.javabdd.BDD#applyWith(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp)
          */
         public void applyWith(BDD that, BDDFactory.BDDOp opr) {
             applyWith((BuDDyBDD) that, opr);
         }
         public native void applyWith(BuDDyBDD that, BDDFactory.BDDOp opr);
         
         /**
          * @see org.sf.javabdd.BDD#applyAll(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp, org.sf.javabdd.BDD)
          */
         public BDD applyAll(BDD that, BDDFactory.BDDOp opr, BDD var) {
             return applyAll((BuDDyBDD) that, opr, (BuDDyBDD) var);
         }
         public native BuDDyBDD applyAll(BuDDyBDD that, BDDFactory.BDDOp opr, BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#applyEx(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp, org.sf.javabdd.BDD)
          */
         public BDD applyEx(BDD that, BDDFactory.BDDOp opr, BDD var) {
             return applyEx((BuDDyBDD) that, opr, (BuDDyBDD) var);
         }
         public native BuDDyBDD applyEx(BuDDyBDD that, BDDFactory.BDDOp opr, BuDDyBDD var);
         
         /**
          * @see org.sf.javabdd.BDD#applyUni(org.sf.javabdd.BDD, org.sf.javabdd.BDDFactory.BDDOp, org.sf.javabdd.BDD)
          */
         public BDD applyUni(BDD that, BDDFactory.BDDOp opr, BDD var) {
             return applyUni((BuDDyBDD) that, opr, (BuDDyBDD) var);
         }
         public native BuDDyBDD applyUni(BuDDyBDD that, BDDFactory.BDDOp opr, BuDDyBDD var);
         
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
         public BDD satOneSet(BDD var, BDD pol) {
             return satOneSet((BuDDyBDD) var, (BuDDyBDD) pol);
         }
         public native BuDDyBDD satOneSet(BuDDyBDD var, BuDDyBDD pol);
         
         /**
          * @see org.sf.javabdd.BDD#allsat()
          */
         public List allsat() {
             return Arrays.asList(_allsat());
         }
         
         protected native byte[][] _allsat();
         
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
         public double satCount(BDD varset) {
             return satCount((BuDDyBDD) varset);
         }
         public native double satCount(BuDDyBDD varset);
         
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
         public double logSatCount(BDD varset) {
             return logSatCount((BuDDyBDD) varset);
         }
         public native double logSatCount(BuDDyBDD varset);
         
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
         public BDD veccompose(BDDPairing pair) {
             return veccompose((BuDDyBDDPairing) pair);
         }
         public native BuDDyBDD veccompose(BuDDyBDDPairing pair);
         
         /**
          * @see org.sf.javabdd.BDD#scanSet()
          */
         public native int[] scanSet();
         
         /**
          * @see org.sf.javabdd.BDD#scanSetDomains()
          */
         public native int[] scanSetDomains();
         
         /**
          * @see org.sf.javabdd.BDD#scanVar(org.sf.javabdd.BDDDomain)
          */
         public int scanVar(BDDDomain var) {
             return scanVar((BuDDyBDDDomain) var);
         }
         public native int scanVar(BuDDyBDDDomain var);
         
         /**
          * @see org.sf.javabdd.BDD#scanAllVar()
          */
         public native int[] scanAllVar();
         
         /**
          * @see org.sf.javabdd.BDD#replace(org.sf.javabdd.BDDPairing)
          */
         public BDD replace(BDDPairing pair) {
             return replace((BuDDyBDDPairing) pair);
         }
         public native BuDDyBDD replace(BuDDyBDDPairing pair);
         
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
          * @see org.sf.javabdd.BDDDomain#getIndex()
          */
         public int getIndex() { return _id; }
         
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
        public BDD buildEquals(BDDDomain that) {
            return buildEquals((BuDDyBDDDomain) that);
        }
         public native BuDDyBDD buildEquals(BuDDyBDDDomain that);
         
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
         
         /**
          * @see java.lang.Object#equals()
          */
         public boolean equals(Object o) {
             try {
                 return equals((BuDDyBDDDomain) o);
             } catch (ClassCastException _) {
                 return false;
             }
         }
         
         public boolean equals(BuDDyBDDDomain that) {
             return this._id == that._id;
         }
         
         /**
          * @see java.lang.Object#hashCode()
          */
         public int hashCode() {
             return this._id;
         }
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
        public void set(BDD oldvar, BDD newvar) {
            set((BuDDyBDD) oldvar, (BuDDyBDD) newvar);
        }
         public native void set(BuDDyBDD oldvar, BuDDyBDD newvar);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDD[], org.sf.javabdd.BDD[])
          */
         public native void set(BDD[] oldvar, BDD[] newvar);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDDDomain, org.sf.javabdd.BDDDomain)
          */
        public void set(BDDDomain p1, BDDDomain p2) {
            set((BuDDyBDDDomain) p1, (BuDDyBDDDomain) p2);
        }
         public native void set(BuDDyBDDDomain p1, BuDDyBDDDomain p2);
         
         /**
          * @see org.sf.javabdd.BDDPairing#set(org.sf.javabdd.BDDDomain[], org.sf.javabdd.BDDDomain[])
          */
         public native void set(BDDDomain[] p1, BDDDomain[] p2);
         
         /**
          * @see org.sf.javabdd.BDDPairing#reset()
          */
         public native void reset();
         
         /**
          * @see java.lang.Object#finalize()
          */
         protected void finalize() throws Throwable {
             super.finalize();
             this.free();
         }
 
         /**
          * Free the memory allocated for this pair.
          */
         private native void free();
         
     }
     
 }
