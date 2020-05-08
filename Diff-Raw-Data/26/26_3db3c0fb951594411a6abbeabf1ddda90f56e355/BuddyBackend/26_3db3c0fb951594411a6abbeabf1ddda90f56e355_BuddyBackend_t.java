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
 
     void init() {
         System.loadLibrary("jeddbuddy");
         Buddy.bdd_init( 1*1000*1000, 100*1000 );
        Buddy.setuperrorhandler();
         Buddy.bdd_disable_reorder();
         Buddy.bdd_setcacheratio(4);
         Buddy.bdd_setmaxincrease(100*1000);
     }
 
     protected int totalBits = 0;
     int numBits() {
         return totalBits;
     }
     void addBits( int bits ) {
         Buddy.bdd_extvarnum(bits);
         totalBits += bits;
     }
 
     void addRef( RelationInstance bdd ) {
         Buddy.bdd_addref( bdd(bdd) );
     }
     void delRef( RelationInstance bdd ) {
         Buddy.bdd_delref( bdd(bdd) );
     }
 
     // return value of following functions *is* refed
     RelationInstance falseBDD() {
         return bdd( Buddy.bdd_addref( Buddy.bdd_false() ) );
     }
     RelationInstance trueBDD() {
         return bdd( Buddy.bdd_addref( Buddy.bdd_true() ) );
     }
     protected RelationInstance ithVar( int i ) {
         return bdd( Buddy.bdd_addref( Buddy.bdd_ithvar(i) ) );
     }
     protected RelationInstance nithVar( int i ) {
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
     RelationInstance replace( RelationInstance r, Replacer pair ) {
         return bdd( Buddy.bdd_replace( bdd(r), pair(pair) ) );
     }
 
     RelationInstance copy( RelationInstance r, Copier copier ) {
         return and( r, relpc( copier ) );
     }
     Copier makeCopier( int from[], int to[] ) {
         int i;
         RelationInstance ret = trueBDD();
         for( i=0; i < from.length; i++ ) {
             RelationInstance b1, b2;
             RelationInstance ivf = ithVar(from[i]);
             RelationInstance ivt = ithVar(to[i]);
             b1 = biimp( ivf, ivt );
             addRef( b1 );
             delRef( ivf );
             delRef( ivt );
             b2 = and( b1, ret );
             addRef( b2 );
             delRef( b1 );
             delRef( ret );
             ret = b2;
         }
         return relpc(ret);
     }
 
     RelationInstance relprod( RelationInstance r1, RelationInstance r2, Projector proj ) {
         RelationInstance ret = bdd( Buddy.bdd_appex(
                     bdd(r1), bdd(r2), Buddy.bddop_and, bdd(relpc(proj)) ) );
         return ret;
     }
     RelationInstance project( RelationInstance r, Projector proj ) {
         RelationInstance ret = bdd( Buddy.bdd_exist( bdd(r), bdd(relpc(proj)) ) );
         return ret;
     }
     RelationInstance or( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_or( bdd(r1), bdd(r2) ) );
     }
     RelationInstance and( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_and( bdd(r1), bdd(r2) ) );
     }
     RelationInstance biimp( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_biimp( bdd(r1), bdd(r2) ) );
     }
     RelationInstance minus( RelationInstance r1, RelationInstance r2 ) {
         return bdd( Buddy.bdd_apply( bdd(r1), bdd(r2), Buddy.bddop_diff ) );
     }
 
     boolean equals( RelationInstance r1, RelationInstance r2 ) {
         return bdd(r1) == bdd(r2);
     }
 
 
     void setOrder( int level2var[] ) {
         Buddy.bdd_setvarorder( level2var );
     }
 
     void allCubes( RelationInstance r, int cubes[] ) {
         Buddy.allCubes( bdd(r), cubes );
     }
 
     int numNodes( RelationInstance r ) {
         return Buddy.bdd_nodecount(bdd(r));
     }
     int numPaths( RelationInstance r ) {
         return (int) Buddy.bdd_pathcount(bdd(r));
     }
 
     int satCount( RelationInstance r, int vars ) {
         double s = Buddy.bdd_satcount(bdd(r));
         s /= Math.pow(2,totalBits-vars);
         return (int) s;
     }
 
     void gbc() {
         Buddy.bdd_gbc();
     }
 
     void getShape( RelationInstance bdd, int shape[] ) {
         Buddy.getShape( bdd(bdd), shape );
     }
 
     Projector makeProjector( int domains[] ) {
         return relpc( bdd( Buddy.bdd_addref( Buddy.bdd_makeset( domains, domains.length ) ) ) );
     }
     Replacer makeReplacer( int from[], int to[] ) {
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
 }
