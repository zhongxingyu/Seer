 /* Jedd - A language for implementing relations using BDDs
  * Copyright (C) 2003 Ondrej Lhotak
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package jedd.internal;
 
 import java.util.*;
 import jedd.internal.buddy.*;
 
 public class BuddyBackend extends Backend {
     protected int bdd( RelationInstance in ) {
         return ((BuddyInstance)in).bdd;
     }
     protected RelationInstance bdd( int in ) {
         return new BuddyInstance( in );
     }
 
     synchronized void init() {
         init(0);
     }
     synchronized void init(int numNodes) {
         System.loadLibrary("jeddbuddy");
         if(numNodes == 0) {
             Buddy.bdd_init( 1*1000*1000, 100*1000 );
         } else {
             Buddy.bdd_init( numNodes, numNodes/10 );
            Buddy.bdd_setmaxnodenum( numNodes );
         }
         //Buddy.bdd_init( 41*1000*1000, 100*1000 );
         Buddy.setuperrorhandler();
         Buddy.bdd_disable_reorder();
         Buddy.bdd_setcacheratio(4);
         Buddy.bdd_setmaxincrease(10*1000*1000);
         //int nodes = Buddy.bdd_getallocnum();
         //System.out.println("currently alloced: "+nodes);
         //Buddy.bdd_setmaxnodenum(nodes+1);
         //Buddy.bdd_setminfreenodes(1);
     }
 
     synchronized void verboseGC() {
         Buddy.verbose_gc();
     }
 
     protected int totalBits = 0;
     int numBits() {
         return totalBits;
     }
     synchronized void addBits( int bits ) {
         Buddy.bdd_extvarnum(bits);
         totalBits += bits;
     }
 
     synchronized void addRef( RelationInstance bdd ) {
         Buddy.bdd_addref( bdd(bdd) );
     }
     synchronized void delRef( RelationInstance bdd ) {
         Buddy.bdd_delref( bdd(bdd) );
     }
 
     // return value of following functions *is* refed
     synchronized RelationInstance falseBDD() {
         return bdd( Buddy.bdd_addref( Buddy.bdd_false() ) );
     }
     synchronized RelationInstance trueBDD() {
         return bdd( Buddy.bdd_addref( Buddy.bdd_true() ) );
     }
     synchronized protected RelationInstance ithVar( int i ) {
         return bdd( Buddy.bdd_addref( Buddy.bdd_ithvar(i) ) );
     }
     synchronized protected RelationInstance nithVar( int i ) {
         return bdd( Buddy.bdd_addref( Buddy.bdd_nithvar(i) ) );
     }
     RelationInstance literal( int bits[] ) {
         int i = 0;
         RelationInstance ret = trueBDD();
         RelationInstance tmp;
         RelationInstance var;
         for( i = 0; i < bits.length; i++ ) {
             if( bits[i] == 0 ) {
                 tmp = ret;
                 ret = and( tmp, var = nithVar(i) );
                 addRef(ret);
                 delRef(var);
                 delRef(tmp);
             } else if( bits[i] == 1 ) {
                 tmp = ret;
                 ret = and( tmp, var = ithVar(i) );
                 addRef(ret);
                 delRef(var);
                 delRef(tmp);
             }
         }
         return ret;
     }
 
 
     // return value of following functions is *not* refed
     synchronized RelationInstance replace( RelationInstance r, Replacer pair ) {
         return bdd( Buddy.bdd_replace( bdd(r), pair(pair) ) );
     }
 
     RelationInstance copy( RelationInstance r, Copier copier ) {
         BuddyCopier bc = (BuddyCopier) copier;
         RelationInstance ret = r;
         addRef(ret);
         for( int i=0; i<bc.rels.length; i++ ) {
             RelationInstance newRet = and(ret, bc.rels[i]);
             addRef(newRet);
             delRef(ret);
             ret = newRet;
         }
         delRef(ret);
         return ret;
     }
     static class BuddyCopier implements Copier {
         public RelationInstance[] rels;
     }
     Copier makeCopier( int from[], int to[] ) {
         int i;
         RelationInstance[] rels = new RelationInstance[from.length];
         for( i=0; i < from.length; i++ ) {
             RelationInstance b1, b2;
             RelationInstance ivf = ithVar(from[i]);
             RelationInstance ivt = ithVar(to[i]);
             b1 = biimp( ivf, ivt );
             addRef( b1 );
             delRef( ivf );
             delRef( ivt );
             rels[i] = b1;
         }
         BuddyCopier ret = new BuddyCopier();
         ret.rels = rels;
         return ret;
     }
     static class BuddyAdder implements Adder {
         public int from[];
         public int to[];
     }
     Adder makeAdder( int[] from, int[] to ) {
         BuddyAdder ret = new BuddyAdder();
         ret.from = from;
         ret.to = to;
         return ret;
     }
 
     synchronized RelationInstance relprod( RelationInstance r1, RelationInstance r2, Projector proj ) {
         RelationInstance ret = bdd( Buddy.bdd_appex(
                     bdd(r1), bdd(r2), Buddy.bddop_and, bdd(relpc(proj)) ) );
         return ret;
     }
     synchronized RelationInstance project( RelationInstance r, Projector proj ) {
         RelationInstance ret = bdd( Buddy.bdd_exist( bdd(r), bdd(relpc(proj)) ) );
         return ret;
     }
     synchronized RelationInstance or( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_or( bdd(r1), bdd(r2) ) );
     }
     synchronized RelationInstance and( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_and( bdd(r1), bdd(r2) ) );
     }
     synchronized RelationInstance xor( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_xor( bdd(r1), bdd(r2) ) );
     }
     synchronized RelationInstance biimp( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_biimp( bdd(r1), bdd(r2) ) );
     }
     synchronized RelationInstance minus( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_apply( bdd(r1), bdd(r2), Buddy.bddop_diff ) );
     }
 
     boolean equals( RelationInstance r1, RelationInstance r2 ) {
         return bdd(r1) == bdd(r2);
     }
 
 
     synchronized void setOrder( int level2var[] ) {
         Buddy.bdd_setvarorder( level2var );
     }
     synchronized void allowReorder(boolean setting) {
         if(setting) {
             Buddy.bdd_autoreorder(Buddy.BDD_REORDER_SIFTITE);
             Buddy.bdd_reorder_verbose(1);
             Buddy.bdd_enable_reorder();
         } else {
             Buddy.bdd_disable_reorder();
         }
     }
 
     synchronized Iterator cubeIterator( final RelationInstance r ) {
         return new Iterator() {
             int[] cubes = new int[totalBits];
             boolean done = (0 == Buddy.firstCube(bdd(r), cubes.length, cubes));
             public boolean hasNext() { return !done; }
             synchronized public Object next() {
                 int[] ret = new int[totalBits];
                 System.arraycopy( cubes, 0, ret, 0, totalBits );
                 done = (0 == Buddy.nextCube(bdd(r), cubes.length, cubes));
                 return ret;
             }
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     /*
     void allCubes( RelationInstance r, int cubes[] ) {
         Buddy.allCubes( bdd(r), cubes );
     }
     */
 
     synchronized int numNodes( RelationInstance r ) {
         return Buddy.bdd_nodecount(bdd(r));
     }
     synchronized int numPaths( RelationInstance r ) {
         return (int) Buddy.bdd_pathcount(bdd(r));
     }
 
     synchronized double fSatCount( RelationInstance r, int vars ) {
         double s = Buddy.bdd_satcount(bdd(r));
         s /= Math.pow(2,totalBits-vars);
         return s;
     }
 
     synchronized long satCount( RelationInstance r, int vars ) {
         return (long) fSatCount(r, vars);
     }
 
     synchronized void gbc() {
         Buddy.bdd_gbc();
     }
 
     synchronized void getShape( RelationInstance bdd, int shape[] ) {
         Buddy.getShape( bdd(bdd), shape );
     }
 
     synchronized Projector makeProjector( int domains[] ) {
         return relpc( bdd( Buddy.bdd_addref( Buddy.bdd_makeset( domains, domains.length ) ) ) );
     }
     synchronized Replacer makeReplacer( int from[], int to[] ) {
         bddPair pair = Buddy.bdd_newpair();
         Buddy.bdd_setpairs( pair, from, to, from.length );
         return pair( pair );
     }
     private static class BuddyReplacer implements Replacer {
         final bddPair pair;
         BuddyReplacer( bddPair pair ) {
             this.pair = pair;
         }
     }
     private bddPair pair( Replacer in ) {
         return ((BuddyReplacer)in).pair;
     }
     private Replacer pair( bddPair in ) {
         return new BuddyReplacer( in );
     }
     RelationInstance add( RelationInstance r, Adder adder, long offset ) {
         BuddyAdder ba = (BuddyAdder) adder;
 
         int[] from = ba.from;
         int[] to = ba.to;
 
         RelationInstance output = trueBDD();
         RelationInstance carry = falseBDD();
 
         for( int i = 0; i < from.length; i++ ) {
             RelationInstance itf = ithVar(from[i]);
             RelationInstance itt = ithVar(to[i]);
             if( (offset & 1L) == 0L ) {
                 // out &= to <=> from ^ c
                 // newc = from & c
                 RelationInstance bdd1 = xor(itf, carry);
                 addRef(bdd1);
 
                 RelationInstance bdd2 = biimp(itt, bdd1);
                 addRef(bdd2);
                 delRef(bdd1);
 
                 RelationInstance bdd3 = and(output, bdd2);
                 addRef(bdd3);
                 delRef(bdd2);
                 delRef(output);
 
                 output = bdd3;
 
                 RelationInstance oldCarry = carry;
                 carry = and(itf, oldCarry);
                 addRef(carry);
                 delRef(oldCarry);
             } else {
                 // out &= to <=> 1 ^ from ^ c
                 // newc = in | c
                 RelationInstance bdd1 = biimp(itf, carry);
                 addRef(bdd1);
 
                 RelationInstance bdd2 = biimp(itt, bdd1);
                 addRef(bdd2);
                 delRef(bdd1);
 
                 RelationInstance bdd3 = and(output, bdd2);
                 addRef(bdd3);
                 delRef(bdd2);
                 delRef(output);
 
                 output = bdd3;
 
                 RelationInstance oldCarry = carry;
                 carry = or(itf, oldCarry);
                 addRef(carry);
                 delRef(oldCarry);
             }
             delRef(itf);
             delRef(itt);
             offset >>>= 1;
         }
 
         RelationInstance ret = and(r, output);
         delRef(carry);
         delRef(output);
         return ret;
     }
     synchronized protected int width(RelationInstance bdd, int bit1, int bit2) {
         return Buddy.bdd_markwidth(bdd(bdd), bit1, bit2);
     }
 }
