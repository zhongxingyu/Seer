 /*
  * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 2 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 2 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
  * CA 95054 USA or visit www.sun.com if you need additional information or
  * have any questions.
  */
 
 package com.sun.javafx.runtime.location;
 
 import com.sun.javafx.runtime.sequence.Sequence;
 import com.sun.javafx.runtime.sequence.SequenceMutator;
 import com.sun.javafx.runtime.sequence.SequencePredicate;
 import com.sun.javafx.runtime.sequence.Sequences;
 
 /**
  * SequenceVar represents a sequence-valued variable as a Location.
  * New SequenceVars are constructed with the make() factory
  * method.  SequenceVar values are always valid; it is an error to invalidate a SequenceVar.
  *
  * @author Brian Goetz
  */
 public class SequenceVar<T> extends AbstractSequenceLocation<T> implements SequenceLocation<T>, MutableLocation {
 
     private final SequenceMutator.Listener<T> mutationListener = new MutationListener();
 
     public static <T> SequenceVar<T> make(Sequence<? extends T> value) {
         return new SequenceVar<T>(value);
     }
 
     public static <T> SequenceLocation<? extends T> makeUnmodifiable(Sequence<? extends T> value) {
         return Locations.unmodifiableLocation(new SequenceVar<T>(value));
     }
 
     private SequenceVar(Sequence<? extends T> value) {
         super(true, false);
         if (value == null)
             throw new NullPointerException();
         this.value = value;
     }
 
     @Override
     public void invalidate() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Sequence<? extends T> set(Sequence<? extends T> value) {
         replaceValue(value);
         return value;
     }
 
     @Override
     public void set(int position, T newValue) {
         SequenceMutator.set(value, mutationListener, position, newValue);
     }
 
     @Override
     public void delete(int position) {
         SequenceMutator.delete(value, mutationListener, position);
     }
 
     @Override
     public void delete(SequencePredicate<T> sequencePredicate) {
         SequenceMutator.delete(value, mutationListener, sequencePredicate);
     }
 
     @Override
     public void deleteAll() {
        set(Sequences.emptySequence((Class<? extends T>) value.getElementType()));
     }
 
     @Override
     public void deleteValue(final T targetValue) {
         delete(new SequencePredicate<T>() {
             public boolean matches(Sequence<? extends T> sequence, int index, T value) {
                 return ((value == null && targetValue == null || value.equals(targetValue)));
             }
         });
     }
 
     @Override
     public void insert(T value) {
         SequenceMutator.insert(this.value, mutationListener, value);
     }
 
     @Override
     public void insert(Sequence<? extends T> values) {
         SequenceMutator.insert(value, mutationListener, values);
     }
 
     public void insertFirst(T value) {
         SequenceMutator.insertFirst(this.value, mutationListener, value);
     }
 
     @Override
     public void insertFirst(Sequence<? extends T> values) {
         SequenceMutator.insertFirst(value, mutationListener, values);
     }
 
     @Override
     public void insertBefore(T value, int position) {
         SequenceMutator.insertBefore(this.value, mutationListener, value, position);
     }
 
     @Override
     public void insertBefore(T value, SequencePredicate<T> sequencePredicate) {
         SequenceMutator.insertBefore(this.value, mutationListener, value, sequencePredicate);
     }
 
     @Override
     public void insertBefore(Sequence<? extends T> values, int position) {
         SequenceMutator.insertBefore(value, mutationListener, values, position);
     }
 
     @Override
     public void insertBefore(Sequence<? extends T> values, SequencePredicate<T> sequencePredicate) {
         SequenceMutator.insertBefore(value, mutationListener, values, sequencePredicate);
     }
 
     @Override
     public void insertAfter(T value, int position) {
         SequenceMutator.insertAfter(this.value, mutationListener, value, position);
     }
 
     @Override
     public void insertAfter(T value, SequencePredicate<T> sequencePredicate) {
         SequenceMutator.insertAfter(this.value, mutationListener, value, sequencePredicate);
     }
 
     @Override
     public void insertAfter(Sequence<? extends T> values, int position) {
         SequenceMutator.insertAfter(value, mutationListener, values, position);
     }
 
     @Override
     public void insertAfter(Sequence<? extends T> values, SequencePredicate<T> sequencePredicate) {
         SequenceMutator.insertAfter(value, mutationListener, values, sequencePredicate);
     }
 
     private class MutationListener implements SequenceMutator.Listener<T> {
         public void onReplaceSequence(Sequence<? extends T> newSeq) {
             value = newSeq;
             valueChanged();
         }
 
         public void onInsert(int position, T element) {
             SequenceVar.this.onInsert(position, element);
         }
 
         public void onDelete(int position, T element) {
             SequenceVar.this.onDelete(position, element);
         }
 
         public void onReplaceElement(int position, T oldValue, T newValue) {
             SequenceVar.this.onReplaceElement(position, oldValue, newValue);
         }
 
     }
 }
