 /* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
  *
  * Copyright (c) 2012-2013 Edugility LLC.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  * The original copy of this license is available at
  * http://www.opensource.org/license/mit-license.html.
  */
 package com.edugility.objexj.engine;
 
 import java.util.Arrays;
 import java.util.Collection; // for javadoc only
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A virtual thread for running {@link Instruction}s from a regular
  * expression {@link Program}.
  *
  * A {@link Thread} may be in several states:
  *
  * <ul>
  *
  * <li><strong>{@linkplain State#VIABLE Viable}</strong>.  A {@link
  * Thread} is <em>viable</em> when its {@linkplain #getState() state}
  * is {@link State#VIABLE VIABLE} and when it {@linkplain
 * #getProgramCounter() has a valid program counter}.</li>
  *
  * <li><strong>{@linkplain State#DEAD Dead}</strong>.  A {@link
  * Thread} is <em>{@linkplain State#DEAD dead}</em> when its
  * {@linkplain #getState() state} is {@link State#DEAD DEAD}.  Death
  * occurs when the {@link #die()} method is invoked.  Internally,
  * {@link #die()} is invoked in some cases by the {@link #step()}
  * method and the {@link #advanceProgramCounter()} method.  A dead
  * {@link Thread} will return {@code false} from the following
  * methods:
  *
  * <ul>
  *
  * <li>{@link #canRead()}</li>
  *
  * <li>{@link #isViable()}</li>
  *
  * </ul>
  *
  * Additionally, a {@linkplain State#DEAD dead} {@link Thread} will
  * throw an {@link IllegalStateException} from any other method that
  * is invoked.</li>
  *
  * <li><strong>{@linkplain State#MATCH Match}</strong>.  A {@link
  * Thread} is a <em>match</em> when its {@link #getState()} method
  * returns {@link State#MATCH MATCH}.  A {@link Thread} is placed in
  * the {@link State#MATCH MATCH} state by invoking the {@link
  * #match()} method.  A {@link Thread} in the {@link State#MATCH
  * MATCH} state effectively cannot do anything other than {@linkplain
  * #getSubmatches() report its submatches}.</li>
  *
  * </ul>
  *
  * @param <T> the type of items this {@link Thread} will {@linkplain
  * #read() read}
  *
  * @author <a href="http://about.me/lairdnelson" target="_parent">Laird Nelson</a>
  *
  * @see <a
  * href="http://swtch.com/~rsc/regexp/regexp2.html" target="_parent">R. S. Cox. (2009,
  * December). <span style="font-style: italic;">Regular Expression Matching: the Virtual Machine
  * Approach</span> [Online]. Available:
  * http://swtch.com/~rsc/regexp/regexp2.html</a>
  */
 public class Thread<T> implements Cloneable, Runnable, ThreadScheduler<T> {
 
   /**
    * A legal value for a {@link Thread}'s {@linkplain
    * #advanceItemPointer() item pointer}, provided that there is no
    * input to read from.
    */
   public static final int VALID_NO_INPUT_POINTER = Integer.MAX_VALUE;
 
   /**
    * A constant representing an {@linkplain #advanceItemPointer() item
    * pointer} that is invalid in all cases.
    */
   private static final int INVALID_INPUT_POINTER = -1;
 
   /**
    * An {@code enum} representing the possible states a {@link Thread}
    * can be in.
    *
    * @author <a href="http://about.me/lairdnelson"
    * target="_parent">Laird Nelson</a>
    */
   public enum State {
 
     /**
      * A {@link State} indicating the associated {@link Thread} is
      * completely non-viable and is essentially useless.
      */
     DEAD, 
 
     /**
      * A {@link State} indicating that a {@link Thread} has fulfilled
      * its purpose and is no longer viable.
      */
     MATCH, 
 
     /**
      * A {@link State} indicating that a {@link Thread} is in a normal
      * state and is fully viable.
      */
     VIABLE
   }
 
   /**
    * The {@link ThreadScheduler} used by this {@link Thread} to
    * {@linkplain ThreadScheduler#newThread(Object, ProgramCounter,
    * List, int, Map, Map) spawn} and {@linkplain
    * ThreadScheduler#schedule(Thread) schedule} new {@link Thread}s.
    * This field is never {@code null}.
    * 
    * <p>This field is not deep cloned by the {@link #clone()} method.
    * {@link Thread} clones will share {@link ThreadScheduler}
    * references.</p>
    */
   private final ThreadScheduler<T> threadScheduler;
 
   /**
    * A {@link List} of items that constitutes the <em>input</em> that
    * can be {@linkplain #read() read} by this {@link Thread}.
    *
    * <p>This field may be {@code null} or {@linkplain
    * Collection#isEmpty() empty}.</p>
    *
    * <p>This field is not deep cloned by the {@link #clone()} method.
    * {@link Thread} clones will share {@code items} references.</p>
    *
    * @see #advanceItemPointer()
    *
    * @see #read()
    */
   private final List<? extends T> items;
 
   /**
    * The index used by the {@link #read()} method (and incremented by
    * the {@link #advanceItemPointer()} method) to select a particular
    * item from this {@link Thread}'s {@linkplain #items affiliated
    * <tt>List</tt> of items}.
    *
    * <p>This field must have a minimum value of {@code 0}.</p>
    *
    * @see #advanceItemPointer()
    *
    * @see #read()
    */
   private int itemPointer;
 
   /**
    * This {@link Thread}'s affiliated {@link ProgramCounter}.  This
    * field must not be {@code null}.
    *
    * <p>This field is {@linkplain ProgramCounter#clone() deep cloned}
    * by the {@link #clone()} method.
    *
    * @see #advanceProgramCounter()
    *
    * @see #getProgramCounter()
    */
   private ProgramCounter<T> programCounter;
 
   /**
    * The {@link State} this {@link Thread} is in.  This field is never
    * {@code null}.
    *
    * <p>{@link Thread}s created by the {@link #clone()} method always
    * start with a {@link State} of {@link State#VIABLE}.  In other
    * words, this field is not cloned.</p>
    */
   private State state;
 
   /**
    * The identifier of this {@link Thread}.
    *
    * <p>This field may be {@code null}.</p>
    *
    * <p>This field is used simply for its {@link Object#toString()}
    * output.</p>
    *
    * <p>This field is not deep cloned by the {@link #clone()} method.
    * {@link Thread} clones will share {@code id} references.</p>
    *
    * @see #toString()
    *
    * @see #clone()
    */
   private Object id;
 
   /**
    * The {@link CaptureGroup}s affiliated with this {@link Thread}.
    *
    * <p>This field may be {@code null}.</p>
    *
    * <p>This field is {@linkplain #deepClone(Map) deep cloned}
    * by the {@link #clone()} method.</p>
    *
    * @see #deepClone(Map)
    *
    * @see #clone()
    *
    * @see #getSubmatches()
    *
    * @see CaptureGroup
    */
   private Map<Object, CaptureGroup<T>> captureGroups;
 
   /**
    * A {@link Map} of global variables maintained by this {@link
    * Thread}.
    *
    * <p>This field may be {@code null}.</p>
    *
    * @see #getVariables()
    */
   private Map<Object, Object> variables;
 
   /**
    * An {@link InstructionContext} that wraps this {@link Thread}.
    *
    * <p>This field may be {@code null}.</p>
    */
   private InstructionContext<T> ic;
 
   /**
    * Creates a new {@link Thread} without any {@link CaptureGroup}s or
    * variables.
    *
    * @param id the identifier for the new {@link Thread}; used only
    * for its {@link Object#toString()} output; may be {@code null}
    *
    * @param programCounter the {@link ProgramCounter} that {@linkplain
    * ProgramCounter#getProgram() houses} the {@link Program} that will
    * be run by this {@link Thread}; must not be {@code null}
    *
    * @param items the input to read from; may be {@code null} in which
    * case the supplied {@code itemPointer} must be equal to {@code 0}
    * or {@link #VALID_NO_INPUT_POINTER}
    *
    * @param itemPointer the index within the supplied {@code items}
    * {@link List} from which to begin {@linkplain #read() reading};
    * must be zero or a positive integer less than the supplied {@code
    * items} {@linkplain Collection#size() size}, or must be equal to
    * {@link #VALID_NO_INPUT_POINTER} provided that the {@code items}
    * {@link List} is {@code null} or {@linkplain Collection#isEmpty()
    * empty}
    *
    * @param threadScheduler the {@link ThreadScheduler} this {@link
    * Thread} will use to {@linkplain ThreadScheduler#newThread(Object,
    * ProgramCounter, List, int, Map, Map) spawn} and {@linkplain
    * ThreadScheduler#schedule(Thread) schedule} new {@link Thread}s;
    * must not be {@code null}
    *
    * @exception IllegalArgumentException if any of the preconditions
    * outlined as part of the parameter descriptions is not fulfilled
    */
   public Thread(final Object id, final ProgramCounter<T> programCounter, final List<? extends T> items, final int itemPointer, final ThreadScheduler<T> threadScheduler) {
     this(id, programCounter, items, itemPointer, null, null, threadScheduler);
   }
 
   /**
    * Creates a new {@link Thread}.
    *
    * @param id the identifier for the new {@link Thread}; used only
    * for its {@link Object#toString()} output; may be {@code null}
    *
    * @param programCounter the {@link ProgramCounter} that {@linkplain
    * ProgramCounter#getProgram() houses} the {@link Program} that will
    * be run by this {@link Thread}; must not be {@code null}
    *
    * @param items the input to read from; may be {@code null} in which
    * case the supplied {@code itemPointer} must be equal to {@code 0}
    * or {@link #VALID_NO_INPUT_POINTER}
    *
    * @param itemPointer the index within the supplied {@code items}
    * {@link List} from which to begin {@linkplain #read() reading};
    * must be zero or a positive integer less than the supplied {@code
    * items} {@linkplain Collection#size() size}, or must be equal to
    * {@link #VALID_NO_INPUT_POINTER} provided that the {@code items}
    * {@link List} is {@code null} or {@linkplain Collection#isEmpty()
    * empty}
    *
    * @param captureGroups a {@link Map} of {@link CaptureGroup}s
    * indexed by key that this new {@link Thread} will use to store
    * {@linkplain #getSubmatches() submatches}; may be {@code null}
    *
    * @param variables a {@link Map} of variables this {@link Thread}
    * will be initialized with; may be {@code null}
    *
    * @param threadScheduler the {@link ThreadScheduler} this {@link
    * Thread} will use to {@linkplain ThreadScheduler#newThread(Object,
    * ProgramCounter, List, int, Map, Map) spawn} and {@linkplain
    * ThreadScheduler#schedule(Thread) schedule} new {@link Thread}s;
    * must not be {@code null}
    *
    * @exception IllegalArgumentException if any of the preconditions
    * outlined as part of the parameter descriptions is not fulfilled
    *
    * @see #getSubmatches()
    *
    * @see #getVariables()
    */
   public Thread(final Object id, final ProgramCounter<T> programCounter, final List<? extends T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final Map<Object, Object> variables, final ThreadScheduler<T> threadScheduler) {
     super();
     this.state = State.VIABLE;
 
     this.id = id;
 
     if (captureGroups != null) {
       this.captureGroups = captureGroups;
     }
 
     if (variables != null) {
       this.variables = variables;
     }
 
     this.items = items;
 
     if (programCounter == null) {
       throw new IllegalArgumentException("programCounter == null");
     }
     this.programCounter = programCounter;
 
     if (threadScheduler == null) {
       throw new IllegalArgumentException("threadScheduler == null");
     }
     this.threadScheduler = threadScheduler;
 
     /*
      * For checking the incoming itemPointer, we reject truly insane
      * values (like huge integers when a small items List is passed
      * in), but we accept an itemPointer that is "just off the end" of
      * the incoming List (e.g. itemPointer == items.size()).  This
      * state of affairs might come about when a Thread has just read
      * the last item, but the subsequent instruction it's supposed to
      * run doesn't actually need any input.
      *
      * It is certainly the case that if the caller decided to try to
      * read something with, say, an itemPointer of 0 from an empty
      * list, the read would fail.  But not all Threads run
      * Instructions that perform reads.
      */
 
     if (this.items == null || this.items.isEmpty()) {
       if (itemPointer != VALID_NO_INPUT_POINTER && itemPointer != 0) {
         throw new IllegalArgumentException("An empty or null input list requires an inputPointer of 0 or VALID_NO_INPUT_POINTER");
       }
     } else if (itemPointer < 0) {
       throw new IllegalArgumentException("itemPointer < 0: " + itemPointer);
     } else if (itemPointer == this.items.size()) {
       // WARN: the next read will fail
     } else if (itemPointer > this.items.size()) {
       throw new IllegalArgumentException("itemPointer > items.size(): " + itemPointer + " > " + this.items.size());
     }
     this.itemPointer = itemPointer;
   }
 
   /**
    * Returns the identifier of this {@link Thread}.
    *
    * <p>This method may return {@code null}.</p>
    *
    * @return the identifier of this {@link Thread}, or {@code null}
    *
    * @see #setId(Object)
    *
    * @see #toString()
    */
   public Object getId() {
     return this.id;
   }
 
   /**
    * Sets the identifier of this {@link Thread}.  The supplied {@code
    * id} may be {@code null} and is used only for its {@link
    * Object#toString() toString()} return value, which must be
    * non-{@code null}.
    *
    * @param id the new identifier; may be {@code null}
    *
    * @see #getId()
    *
    * @see #toString()
    */
   public void setId(final Object id) {
     this.id = id;
   }
 
   /**
    * Returns an {@linkplain Collections#unmodifiableMap(Map)
    * unmodifiable <tt>Map</tt>} of submatches found by this {@link
    * Thread}.
    *
    * <p>This method never returns {@code null}.</p>
    *
    * <p>The {@linkplain Map#values() values} in the {@link Map} that
    * is returned are guaranteed not to be {@code null} and are
    * {@linkplain Collections#unmodifiableList(List) unmodifiable}.</p>
    *
    * @return a non-{@code null} {@link Map} of submatches indexed by
    * the keys they were stored under as a result of the {@link
    * #save(Object)} and {@link #stop(Object)} operations
    */
   public final Map<Object, List<? extends T>> getSubmatches() {
     if (State.MATCH != this.getState()) {
       throw new IllegalStateException();
     }
     Map<Object, List<? extends T>> returnValue = null;
     if (this.captureGroups != null && !this.captureGroups.isEmpty()) {
       final Set<Entry<Object, CaptureGroup<T>>> entries = this.captureGroups.entrySet();
       if (entries != null) {
         for (final Entry<Object, CaptureGroup<T>> entry : entries) {
           assert entry != null;
           final CaptureGroup<T> cg = entry.getValue();
           if (cg != null) {
             final Object key = entry.getKey();
             if (returnValue == null) {
               returnValue = new HashMap<Object, List<? extends T>>();
             }
             returnValue.put(key, cg.getItems());
           }
         }
       }
     }
     if (returnValue == null) {
       returnValue = Collections.emptyMap();
     } else {
       returnValue = Collections.unmodifiableMap(returnValue);
     }
     return returnValue;
   }
 
   /**
    * Returns the item pointer associated with this {@link Thread}.
    *
    * @return the item pointer associated with this {@link Thread}
    */
   private final int getItemPointer() {
     return this.itemPointer;
   }
 
   /**
    * Returns a non-{@code null} {@link Map} of variables associated
    * with this {@link Thread}.
    *
    * <p>The {@link Map} that is returned is returned by reference, and
    * modifications to it are visible to other consumers of this
    * method.  If subclasses choose to override this method, they must
    * preserve these semantics.</p>
    *
    * @return a non-{@code null} {@link Map} of variable values indexed
    * by arbitrary keys
    *
    * @see MVELFilter
    */
   public Map<Object, Object> getVariables() {
     if (this.variables == null) {
       this.variables = new HashMap<Object, Object>();
     }
     return this.variables;
   }
 
   /**
    * Marks a position in the input such that corresponding items
    * {@linkplain #getSubmatches() can be retrieved later}.
    *
    * @param key the {@link Object} under which to index the position;
    * may be {@code null}
    *
    * @see #getSubmatches()
    */
   public final void save(final Object key) {
     if (key == null) {
       throw new IllegalArgumentException("key", new NullPointerException("key"));
     }
     this.ensureViable();
     if (this.captureGroups == null) {
       this.captureGroups = new HashMap<Object, CaptureGroup<T>>();
       this.captureGroups.put(key, new CaptureGroup<T>(this.items, this.getItemPointer()));
     } else {
       final CaptureGroup<T> cg = this.captureGroups.get(key);
       if (cg == null) {
         this.captureGroups.put(key, new CaptureGroup<T>(this.items, this.getItemPointer()));
       } else {
         cg.setEndIndex(this.getItemPointer());
       }
     }
   }
 
   /**
    * Marks the end of a region in the input such that corresponding
    * items {@linkplain #getSubmatches() can be retrieved later}.
    *
    * @param key the {@link Object} that was passed to a previous
    * {@link #save(Object)} call; may be {@code null}
    */
   public final void stop(final Object key) {
     if (key == null) {
       throw new IllegalArgumentException("key", new NullPointerException("key"));
     }
     this.ensureViable();
     if (this.captureGroups != null) {
       final CaptureGroup<T> cg = this.captureGroups.get(key);
       if (cg != null) {
         cg.setEndIndex(this.getItemPointer());
       }
     }
   }
 
   /**
    * Creates a new {@link Thread} and returns it.
    *
    * @param id the id for the new {@link Thread}; may be {@code null}
    * in which case a generated identifier will be used instead; only
    * its {@link Object#toString()} method will be called
    *
    * @param programCounterIndex the position within the associaged
    * {@link #getProgramCounter() ProgramCounter} from which to begin
    * execution; may be relative or absolute depending on the value of
    * the {@code relative} parameter
    *
    * @param relative whether the {@code programCounterIndex} parameter
    * represents a relative position or an absolute index
    *
    * @return a non-{@code null} new {@link Thread} not identical to
    * this one
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   public final Thread<T> newThread(final Object id, final int programCounterIndex, final boolean relative) {
     this.ensureViable();
     ProgramCounter<T> programCounter = this.getProgramCounter();
     if (programCounter != null) {
       final int absoluteProgramCounterIndex;
       if (relative) {
         absoluteProgramCounterIndex = programCounter.getIndex() + programCounterIndex;
       } else {
         absoluteProgramCounterIndex = programCounterIndex;
       }
       programCounter = programCounter.clone(absoluteProgramCounterIndex);
     }
     // TODO: we're not cloning the variables; is that OK?
     return this.newThread(id, programCounter, this.items, this.getItemPointer(), deepClone(this.captureGroups), this.variables);
   }
 
   /**
    * {@inheritDoc}
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   @Override
   public final Thread<T> newThread(final Object id, final ProgramCounter<T> programCounter, final List<? extends T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final Map<Object, Object> variables) {
     this.ensureViable();
     if (this.threadScheduler == null) {
       throw new IllegalStateException("threadScheduler == null");
     }
     return this.threadScheduler.newThread(id, programCounter, items, itemPointer, captureGroups, variables);
   }
 
   /**
    * Schedules the supplied {@link Thread} for execution.
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * <p>This method performs no validity checking of any kind on the
    * supplied {@link Thread}, relying instead upon the underlying
    * {@link ThreadScheduler}'s {@link
    * ThreadScheduler#schedule(Thread)} method to perform any
    * checks.</p>
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   @Override
   public final boolean schedule(final Thread<T> t) {
     this.ensureViable();
     if (this.threadScheduler == null) {
       throw new IllegalStateException("threadScheduler == null");
     }
     return this.threadScheduler.schedule(t);
   }
 
   /**
    * Returns {@code true} if this {@link Thread}'s {@linkplain
    * #getItemPointer() item pointer} is {@code 0} (in the case of
    * there being input) or {@link #VALID_NO_INPUT_POINTER} (in the case
    * of there being null or {@linkplain Collection#isEmpty() empty}
    * input).
    *
    * @return {@code true} if this {@link Thread} is positioned at the
    * beginning of the input; {@code false} otherwise
    *
    * @exception IllegalStateException if this {@link Thread}'s
    * {@linkplain #getState() state} is {@link State#DEAD}
    */
   public final boolean atStart() {
     if (Thread.State.DEAD == this.getState()) {
       throw new IllegalStateException(Thread.State.DEAD.toString());
     }
     if (this.items == null || this.items.isEmpty()) {
       return this.getItemPointer() == VALID_NO_INPUT_POINTER;
     } else {
       return this.getItemPointer() == 0;
     }
   }
 
   /**
    * Returns {@code true} if this {@link Thread}'s input pointer is
    * equal to this {@link Thread}'s input size.
    *
    * <p><strong>Note:</strong> This method may be invoked in any
    * {@linkplain State state} except {@link State#DEAD}.</p>
    *
    * @return {@code true} if this {@link Thread}'s input pointer is
    * equal to this {@link Thread}'s input size; {@code false}
    * otherwise
    */
   public final boolean atEnd() {
     if (Thread.State.DEAD == this.getState()) {
       throw new IllegalStateException(Thread.State.DEAD.toString());
     }
     if (this.items == null || this.items.isEmpty()) {
       return this.getItemPointer() == VALID_NO_INPUT_POINTER;
     } else {
       return this.getItemPointer() == this.items.size();
     }
   }
 
   /**
    * Returns {@code true} if this {@link Thread} {@linkplain
    * #isViable() is viable}, and has more items to read.
    *
    * <p>This method may be invoked in all states.</p>
    *
    * @return {@code true} if this {@link Thread} can read an item
    */
   public final boolean canRead() {
     return this.isViable() && this.isValidItemPointer(this.getItemPointer());
   }
 
   /**
    * Reads (but does not "consume") the current item.
    *
    * <p>This method may be invoked only when {@link #canRead()}
    * returns {@code true}.</p>
    *
    * @return the current item which may be {@code null}
    *
    * @exception IllegalStateException if the {@link #canRead()} method
    * returns {@code false}
    *
    * @see #canRead()
    */
   public final T read() {
     if (!this.canRead()) {
       throw new IllegalStateException("Thread cannot read");
     }
     if (this.items == null || this.items.isEmpty()) {
       // This is an edge case.  If we are originally supplied with no
       // input, we need to be able to use an itemPointer that is both
       // valid, but doesn't point into the (non-existent) input list.
       assert this.getItemPointer() == VALID_NO_INPUT_POINTER;
       return null;
     }
     assert this.getItemPointer() >= 0 && this.getItemPointer() < this.items.size();
     return this.items.get(this.getItemPointer());
   }
 
   /**
    * Returns {@code true} if the supplied {@code itemPointer}
    * parameter is valid.
    *
    * @param itemPointer the item pointer to consider
    *
    * @return {@code true} if the supplied {@code itemPointer}
    * parameter is valid; {@code false} otherwise
    */
   private final boolean isValidItemPointer(final int itemPointer) {
     if (this.items == null || this.items.isEmpty()) {
       // We can have null or empty input.  In such a case, a value
       // equal to VALID_NO_INPUT_POINTER is OK; all others are not.
       return itemPointer == VALID_NO_INPUT_POINTER;
     }
     return itemPointer >= 0 && itemPointer < this.items.size();
   }
 
   /**
    * Advances this {@link Thread}'s item pointer, provided it was
    * valid to begin with.  Returns {@code true} if the resulting item
    * pointer is valid.
    *
    * <p>Please note that a return value of {@code false} does
    * <em>not</em> indicate that this {@link Thread} is dead.</p>
    *
    * @return {@code true} if this {@link Thread}'s item pointer was
    * successfully advanced; {@code false} otherwise
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   public final boolean advanceItemPointer() {
     this.ensureViable();
     final boolean returnValue;
     int itemPointer = this.getItemPointer();
     if (itemPointer == INVALID_INPUT_POINTER || itemPointer == VALID_NO_INPUT_POINTER) {
       returnValue = false;
     } else {
       ++this.itemPointer;
       returnValue = this.isValidItemPointer(this.getItemPointer());
     }
     return returnValue;
   }
 
   /**
    * Returns this {@link Thread}'s associated {@link ProgramCounter}.
   *
   * <p>This method never returns {@code null}.</p>
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @return this {@link Thread}'s associated {@link ProgramCounter};
    * never {@code null}
    */
   public final ProgramCounter<T> getProgramCounter() {
     this.ensureViable();
     return this.programCounter;
   }
 
   /**
    * Advances the program counter to point at the next {@link
    * Instruction} in the {@link Program}.  If this cannot
    * happen&mdash;as in the case where there are no more {@link
    * Instruction}s in the {@link Program}&mdash;then the {@link
    * #die()} method is invoked.
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @return {@code true} if the program counter was successfully
    * advanced and therefore if there is another {@link Instruction}
    * ready to be executed; {@code false} if this {@link Thread} has
    * been killed via the {@link #die()} method since there are no more
    * {@link Instruction}s in the {@link Program} to be executed
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   public final boolean advanceProgramCounter() {
     this.ensureViable();
     boolean returnValue = false;
     final ProgramCounter<T> programCounter = this.getProgramCounter();
     if (programCounter != null) {
       returnValue = programCounter.advance();
     }
     if (!returnValue) {
       // The program counter could not be incremented, so there are no
       // further instructions to run, so we're dead.
       this.die();
     }
     return returnValue;
   }
 
   /**
    * Sets this {@link Thread}'s program counter to the supplied value,
    * provided it is valid.  If the supplied value is <em>not</em>
    * valid, then this {@link Thread} {@linkplain #die() dies}.
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @param programCounter
    *
    * @param relative
    *
    * @return {@code true} if the program counter was actually set;
    * {@code false} if it was not (thus indicating that this {@link
    * Thread} is now dead)
    *
    * @exception IllegalArgumentException if the supplied {@code
    * programCounter} is not valid
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   public final boolean jump(final int programCounter, final boolean relative) {
     this.ensureViable();
     final boolean returnValue;
     final ProgramCounter<T> pc = this.getProgramCounter();
     final int absoluteProgramCounter;
     if (relative) {
       absoluteProgramCounter = pc.getIndex() + programCounter;
     } else {
       absoluteProgramCounter = programCounter;
     }
     if (pc != null && pc.isValid(absoluteProgramCounter)) {
       pc.setIndex(absoluteProgramCounter);
       returnValue = true;
     } else {
       this.die();
       returnValue = false;
     }
     return returnValue;
   }
 
   /**
    * Returns the current {@link Instruction}.
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @return the current {@link Instruction}, or {@code null} if the
    * {@link #isViable()} method returns {@code false}
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    *
    * @see #isViable()
    */
   private final Instruction<T> getInstruction() {
     this.ensureViable();
     final Instruction<T> returnValue;
     final ProgramCounter<T> programCounter = this.getProgramCounter();
     if (programCounter != null && this.isViable()) {
       returnValue = programCounter.getInstruction();
     } else {
       returnValue = null;
     }
     return returnValue;
   }
 
   /**
    * Retrieves the current {@link Instruction} and {@linkplain
    * Instruction#execute(InstructionContext) executes} it.
    *
    * <p>If the {@link #isViable()} method returns {@code false},
    * then invoking this method will cause the {@link #die()} method to
    * be invoked and no further action will take place.</p>
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   public final void step() {
     this.ensureViable();
     final Instruction<T> instruction = this.getInstruction();
     assert instruction != null;
     if (this.ic == null) {
       this.ic = new InstructionContext<T>(this);
     }
     instruction.execute(this.ic);
   }
 
   /**
    * Repeatedly calls the {@link #step()} method until this {@link
    * Thread} has completed execution.
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   @Override
   public final void run() {
     this.ensureViable();
     final Logger logger = Logger.getLogger(this.getClass().getName());
     assert logger != null;
     final boolean finer = logger.isLoggable(Level.FINER);
     final ProgramCounter<T> pc = this.getProgramCounter();
     assert pc != null;
     while (this.isViable()) {
       final Instruction<T> currentInstruction = pc.getInstruction();
       assert currentInstruction != null;
       if (finer) {
         final int index = pc.getIndex();
         logger.logp(Level.FINER, this.getClass().getName(), "run", "Before running {0} {1} ({2}) at input position {3}", new Object[] { this, currentInstruction, Integer.valueOf(index), Integer.valueOf(this.getItemPointer()) });
       }
       this.step();
       if (finer) {
         if (this.isViable()) {
           logger.logp(Level.FINER, this.getClass().getName(), "run", "After running {0} {1}; input position is now {2}", new Object[] { this, currentInstruction, Integer.valueOf(this.getItemPointer()) });
         } else {
           logger.logp(Level.FINER, this.getClass().getName(), "run", "Thread {0} is no longer viable", this);
         }
       }
     }
   }
 
   /**
    * Returns {@code true} if and only if this {@link Thread}'s
    * {@linkplain #getState() associated state} is {@link State#VIABLE
    * VIABLE} and if its associated {@link ProgramCounter}'s {@link
    * ProgramCounter#isValid()} method returns {@code true}.
    *
    * <p>This method may be invoked in all states.</p>
    *
    * @return {@code true} if and only if this {@link Thread} is viable
    */
   public final boolean isViable() {
     // Do not call getProgramCounter() here; just access the field
     // directly.  This avoids an infinite loop.
     return this.getState() == State.VIABLE && this.programCounter != null && this.programCounter.isValid();
   }
 
   /**
    * Causes this {@link Thread} to irrevocably enter the {@link
    * State#MATCH MATCH} state.
    *
    * <p>This method may be invoked only when {@link #isViable()}
    * returns {@code true}.</p>
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   public final void match() {
     this.ensureViable();
     this.state = State.MATCH;
   }
 
   /**
    * Causes this {@link Thread} to irrevocably enter the {@link
    * State#DEAD DEAD} state.
    *
    * <p>This method may be invoked in all states except {@link
    * State#MATCH MATCH}.</p>
    *
    * @exception IllegalStateException if this {@link Thread} is
    * currently in the {@link State#MATCH MATCH} state
    */
   public final void die() {
     if (State.MATCH == this.getState()) {
       throw new IllegalStateException();
     }
     this.state = State.DEAD;
     this.itemPointer = INVALID_INPUT_POINTER;
     if (this.captureGroups != null) {
       this.captureGroups.clear();
     }
   }
 
   /**
    * Returns the {@link State} of this {@link Thread}.  This method
    * never returns {@code null}.
    *
    * @return a non-{@code null} {@link State}
    */
   public final State getState() {
     assert this.state != null;
     return this.state;
   }
 
   /**
    * Returns a non-{@code null} {@link Set} of {@link Object}s
    * representing the keys under which {@link CaptureGroup}s are
    * indexed.
    *
    * @return a non-{@code null} {@link Set} of {@link Object}s
    * representing the keys under which {@link CaptureGroup}s are
    * indexed
    *
    * @see #getSubmatches()
    */
   public final Set<?> getGroupKeySet() {
     final Set<?> returnValue;
     if (this.captureGroups == null || this.captureGroups.isEmpty()) {
       returnValue = Collections.emptySet();
     } else {
       returnValue = Collections.unmodifiableSet(this.captureGroups.keySet());
     }
     return returnValue;
   }
 
   /**
    * Returns the number of {@link CaptureGroup}s this {@link Thread}
    * is currently tracking.
    *
    * @return the number of {@link CaptureGroup}s this {@link Thread}
    * is currently tracking
    */
   public final int getGroupCount() {
     final int result;
     if (this.captureGroups == null || this.captureGroups.isEmpty()) {
       result = 0;
     } else {
       result = this.captureGroups.size();
     }
     return result;
   }
 
   /**
    * Returns the {@link List} of items captured under the supplied
    * {@link Object} key.
    *
    * <p>This method may return {@code null}.</p>
    *
    * @param key the key in question; may be {@code null}
    *
    * @return a {@link List} of items; may be {@code null}
    */
   public final List<? extends T> getGroup(final Object key) {
     List<? extends T> result = null;
     if (this.captureGroups != null) {
       final CaptureGroup<T> cg = this.captureGroups.get(key);
       if (cg != null) {
         result = cg.getItems();
       }
     }
     return result;
   }
 
   /**
    * Does nothing if this {@link Thread} {@linkplain #isViable() is viable}.
    *
    * @exception IllegalStateException if this {@link Thread}
    * {@linkplain #isViable() is not viable}
    */
   private final void ensureViable() {
     if (!this.isViable()) {
       throw new IllegalStateException("Thread is not viable");
     }
   }
 
   /**
    * {@linkplain Object#clone() Clones} this {@link Thread} and
    * returns the clone.
    *
    * <p>This method never returns {@code null}.</p>
    *
    * <h2>Design Notes</h2>
    *
    * <p>{@link Thread}s internally store {@link Map}s of {@link
    * CaptureGroup}s.  These {@link Map}s are {@linkplain
    * #deepClone(Map) deeply cloned}&mdash;that is, the {@link Map}
    * itself is {@link Object#clone() cloned} as well as the {@link
    * CaptureGroup#clone() CaptureGroup}s inside them.</p>
    *
    * <p>As of this writing, the internal {@link Map} of variables is
    * not deeply cloned.  This means that two {@link Thread}s might
    * share variable map keys and values.</p>
    *
    * @return a non-{@code null} clone of this {@link Thread}
    */
   @Override
   public Thread<T> clone() {
     Thread<T> clone = null;
     try {
       @SuppressWarnings("unchecked")
       final Thread<T> temp = (Thread<T>)super.clone();
       clone = temp;
     } catch (final CloneNotSupportedException cannotHappen) {
       throw (InternalError)new InternalError().initCause(cannotHappen);
     }
     assert clone != null;
     
     // Clone our ProgramCounter.
     final ProgramCounter<T> programCounter = this.getProgramCounter();
     if (programCounter != null) {
       clone.programCounter = programCounter.clone();
     }
 
     // TODO: Clone our variables?
 
     // Clone our capture groups.
     if (this.captureGroups != null) {
       clone.captureGroups = deepClone(this.captureGroups);
     }
 
     // With all of his state set, make sure that he's VIABLE.
     assert clone.isViable();
 
     return clone;
   }
 
   /**
    * Returns a hashcode for this {@link Thread}.
    *
    * @return a hashcode for this {@link Thread}
    */
   @Override
   public int hashCode() {
     int result = 17;
     
     final Object id = this.getId();
     int c = id == null ? 0 : id.hashCode();
     result = result * 37 + c;
 
     c = this.getItemPointer();
     result = result * 37 + c;
 
     c = this.getState().hashCode();
     result = result * 37 + c;
 
     c = this.items == null ? 0 : this.items.hashCode();
     result = result * 37 + c;
 
     // We don't call getProgramCounter() because hashCode() must be a
     // method that is permissible to call in any state.
     c = this.programCounter.hashCode();
     result = result * 37 + c;
 
     c = this.captureGroups == null ? 0 : this.captureGroups.hashCode();
     result = result * 37 + c;
 
     return result;
   }
 
   /**
    * Returns {@code true} if the supplied {@link Object} is equal to
    * this {@link Thread}.
    *
    * @return {@code true} if the supplied {@link Object} is equal to
    * this {@link Thread}.
    */
   @Override
   public boolean equals(final Object other) {
     if (other == this) {
       return true;
     } else if (this.getClass().equals(other.getClass())) {
       final Thread him = (Thread)other;
 
       final int myItemPointer = this.getItemPointer();
       if (him.getItemPointer() != myItemPointer) {
         return false;
       }
 
       final Object id = this.getId();
       if (id == null) {
         if (him.getId() != null) {
           return false;
         }
       }
 
       final State state = this.getState();
       assert state != null;
       if (state != him.getState()) {
         return false;
       }
 
       if (this.items == null) {
         if (him.items != null) {
           return false;
         }
       } else if (!this.items.equals(him.items)) {
         return false;
       }
 
       // We don't call getProgramCounter() because hashCode() must be
       // a method that is permissible to call in any state.
       if (this.programCounter == null) {
         if (him.programCounter != null) {
           return false;
         }
       } else if (!this.programCounter.equals(him.programCounter)) {
         return false;
       }
 
       if (this.captureGroups == null) {
         if (him.captureGroups != null) {
           return false;
         }
       } else if (!this.captureGroups.equals(him.captureGroups)) {
         return false;
       }
 
       return true;
 
     } else {
       return false;
     }
   }
 
   /**
    * Returns a non-{@code null} {@link String} representation of this
    * {@link Thread}.
    *
    * @return a non-{@code null} {@link String} representation of this
    * {@link Thread}
    */
   @Override
   public String toString() {
     final Object id = this.getId();
     if (id == null) {
       return super.toString();
     } else {
       return id.toString();
     }
   }
 
 
   /*
    * Static methods.
    */
 
 
   /**
    * {@linkplain Object#clone() Clones} the supplied {@link Map} of
    * {@link CaptureGroups} by creating a new {@link HashMap} using its
    * {@linkplain HashMap#HashMap(Map) copy constructor} supplied with
    * the value of the {@code suppliedCaptureGroups} parameter.
    *
    * <p>In addition, each {@link CaptureGroup} in the supplied {@link
    * Map} is {@linkplain CaptureGroup#clone() cloned}.</p>
    *
    * <p>The end result is a {@link Map} of {@link CaptureGroup}s
    * indexed by {@link Object}s that is {@linkplain
    * Object#equals(Object) equal to} the supplied {@link
    * suppliedCaptureGroups} parameter, but not identical to it, and
    * containing {@linkplain Map#values() values} that are not
    * identical to the {@linkplain Map#values() values} in the supplied
    * {@code suppliedCaptureGroups} parameter.</p>
    *
    * @param suppliedCaptureGroups a {@link Map} of {@link
    * CaptureGroup}s indexed by arbitrary, possibly {@code null}, keys;
    * may be {@code null} in which case {@code null} will be returned
    *
    * @return a {@link Map} equal to the supplied {@link
    * suppliedCaptureGroups} parameter, but guaranteed not to be
    * identical to it; or {@code null} if the supplied {@code
    * suppliedCaptureGroups} parameter is {@code null}
    */
   private static final <T> Map<Object, CaptureGroup<T>> deepClone(final Map<Object, CaptureGroup<T>> suppliedCaptureGroups) {
     final Map<Object, CaptureGroup<T>> captureGroups;
     if (suppliedCaptureGroups != null) {
       captureGroups = new HashMap<Object, CaptureGroup<T>>(suppliedCaptureGroups.size());
       final Set<Entry<Object, CaptureGroup<T>>> entries = suppliedCaptureGroups.entrySet();
       if (entries != null && !entries.isEmpty()) {
         for (final Entry<Object, CaptureGroup<T>> entry : entries) {
           if (entry != null) {
             final CaptureGroup<T> cg = entry.getValue();
             if (cg == null) {
               captureGroups.put(entry.getKey(), null);
             } else {
               captureGroups.put(entry.getKey(), cg.clone());
             }
           }
         }
       }
     } else {
       captureGroups = null;
     }
     return captureGroups;
   }
 
 }
