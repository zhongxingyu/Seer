 /*
  * Copyright (c) 2013, Kyle Mulleady
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the organization nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY KYLE MULLEADY ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL KYLE MULLEADY BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ics311km;
 
 /** 
  * RedBlackDynamicSet is the red-black tree implementation of DynamicSet.
  * 
  * @author Kyle Mulleady
  * @version 1.0
  */
 public class RedBlackDynamicSet extends BST {
 
     public RedBlackDynamicSet() {
         this.root = null;
         this.size = 0;
     }
 
     public void insert(KeyType k, Object e) {
         Node n = this.nodeInsert(k, true);
         n.isRed(true);
         this.insertFix(n);
     }
 
     public void delete(KeyType k) {
         Node n = this.nodeDelete(k, true);
         if (n != null)
             this.deleteFix(n);
     }
 
     // Very similar to RB-INSERT-FIXUP from the book.
     private void insertFix(Node n) {
         while (n != this.root && n.getP() != this.root && n.getP().isRed()) {
             if (n.getP() == n.getP().getP().getLeft()) {
                 Node y = n.getP().getP().getRight();
                 if (y != null && y.isRed()) {
                     n.getP().isRed(false);
                     y.isRed(false);
                     n.getP().getP().isRed(true);
                     n = n.getP().getP();
                 }
                 else if (n == n.getP().getRight()) {
                     n = n.getP();
                     this.leftRotate(n);
                 }
                 if (n.getP() != null) {
                     n.getP().isRed(false);
                     if (n.getP().getP() != null) {
                         n.getP().getP().isRed(true);
                         this.rightRotate(n.getP().getP());
                     }
                 }
             }
             else {
                 Node y = n.getP().getP().getLeft();
                 if (y != null && y.isRed()) {
                     n.getP().isRed(false);
                     y.isRed(false);
                     n.getP().getP().isRed(true);
                     n = n.getP().getP();
                 }
                 else if (n == n.getP().getLeft()) {
                     n = n.getP();
                     this.rightRotate(n);
                 }
                 if (n.getP() != null) {
                     n.getP().isRed(false);
                     if (n.getP().getP() != null) {
                         n.getP().getP().isRed(true);
                         this.leftRotate(n.getP().getP());
                     }
                 }
             }
         }
         if (this.root != null)
             this.root.isRed(false);
     }
 
     private void leftRotate(Node n) {
         if (n.getRight() != null) {
             Node y = n.getRight();
             n.setRight(y.getLeft());
             if (y.getLeft() != null)
                 y.getLeft().setP(n);
             y.setP(n.getP());
             if (n.getP() == null)
                 this.root = y;
             else if (n == n.getP().getLeft())
                 n.getP().setLeft(y);
             else
                 n.getP().setRight(y);
             y.setLeft(n);
             n.setP(y);
         }
     }
 
     private void rightRotate(Node n) {
         if (n.getLeft() != null) {
             Node y = n.getLeft();
             n.setLeft(y.getRight());
             if (y.getRight() != null)
                 y.getRight().setP(n);
             y.setP(n.getP());
             if (n.getP() == null)
                 this.root = y;
             else if (n == n.getP().getRight())
                 n.getP().setRight(y);
             else
                 n.getP().setLeft(y);
             y.setRight(n);
             n.setP(y);
         }
     }
 
     private void deleteFix(Node n) {
         while (n != this.root && !n.isRed()) {
             if (n == n.getP().getLeft()) {
                 Node w = n.getP().getRight();
                 if (w.isRed()) {
                     w.isRed(false);
                     n.getP().isRed(true);
                     this.leftRotate(n.getP());
                     w = n.getP().getRight();
                 }
                 if (!w.getLeft().isRed() && !w.getRight().isRed()) {
                     w.isRed(true);
                     n = n.getP();
                 }
                 else if (!w.getRight().isRed()) {
                     w.getLeft().isRed(false);
                     w.isRed(true);
                     this.rightRotate(w);
                     w = n.getP().getRight();
                 }
                w.isRed(n.getP().isRed());
                n.getP().isRed(false);
                w.getRight().isRed(false);
                this.leftRotate(n.getP());
                n = this.root;
             }
             else {
                 Node w = n.getP().getLeft();
                 if (w.isRed()) {
                     w.isRed(false);
                     n.getP().isRed(true);
                     this.rightRotate(n.getP());
                     w = n.getP().getLeft();
                 }
                 if (!w.getRight().isRed() && !w.getLeft().isRed()) {
                     w.isRed(true);
                     n = n.getP();
                 }
                 else if (!w.getLeft().isRed()) {
                     w.getRight().isRed(false);
                     w.isRed(true);
                     this.leftRotate(w);
                     w = n.getP().getLeft();
                 }
                w.isRed(n.getP().isRed());
                n.getP().isRed(false);
                w.getLeft().isRed(false);
                this.rightRotate(n.getP());
                n = this.root;
             }
         }
         n.isRed(false);
     }
 
 }
 
 
 
