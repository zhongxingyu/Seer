 /* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
  *
  * Copyright (c) 2013 Edugility LLC.
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
 
 import java.io.Serializable;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * An instruction {@linkplain #execute(InstructionContext) run} in the
  * {@linkplain InstructionContext context} of a {@linkplain
  * com.edugility.objexj.engine.Thread thread} of execution in an
  * {@code objexj} {@linkplain Engine virtual machine}.
  *
  * @param <T> the type of {@link Object} that {@link
  * InstructionContext} instances passed to the {@link
  * #execute(InstructionContext)} method can work with
  *
  * @author <a href="http://about.me/lairdnelson">Laird Nelson</a>
  *
  * @see Engine
  *
  * @see InstructionContext
  *
  * @see Thread
  */
 public abstract class Instruction<T> implements Serializable {
 
   /**
    * The version of this class for serialization purposes.
    *
    * @see Serializable
    */
   private static final long serialVersionUID = 1L;
 
   /**
    * A {@link Pattern} useful in parsing an {@link Instruction} from
    * its textual representation.  This field is never {@code null}.
    *
    * @see #valueOf(String)
    */
   private static final Pattern LINE_PATTERN = Pattern.compile("^([^\\s]+)(?:\\s+?(.*))?$");
 
   /**
    * Creates a new {@link Instruction}.
    */
   protected Instruction() {
     super();
   }
 
   /**
    * Executes this {@link Instruction} in the context of the supplied
    * {@link InstructionContext}.
    *
    * @param context the {@link InstructionContext} in effect; must not
    * be {@code null}
    *
    * @exception IllegalArgumentException if {@code context} is {@code
    * null}
    */
   public abstract void execute(final InstructionContext<T> context);
 
   /**
    * Loads a {@link Class}.  This implementation uses the {@link
    * Class#forName(String, boolean, ClassLoader)} method, passing the
    * supplied {@code className}, {@code true} and the return value of
   * the {@link Thread#getContextClassLoader()} method as arguments.
    *
    * @param className the name of the class to load; must not be
    * {@code null}
    *
    * @return the loaded {@link Class}; never {@code null}
    *
    * @exception IllegalArgumentException if {@code className} is
    * {@code null}
    *
    * @exception ClassNotFoundException if a {@link Class}
    * corresponding to the supplied {@code className} parameter could
    * not be found
    */
   protected Class<?> loadClass(final String className) throws ClassNotFoundException {
     if (className == null) {
       throw new IllegalArgumentException("className", new NullPointerException("className"));
     }
     return Class.forName(className, true, java.lang.Thread.currentThread().getContextClassLoader());
   }
 
   /**
    * Checks to see if the supplied {@link Object} is equal to this
    * {@link Instruction}.  This implementation returns {@code true} if
    * and only if the supplied {@link Object} is non-{@code null} and
    * has a {@link Object#getClass() Class} that is {@linkplain
    * Class#equals(Object) equal to} this {@link Instruction}'s {@link
    * Object#getClass() Class}.
    *
    * @param other the {@link Object} to test; may be {@code null}
    *
    * @return {@code true} if the supplied {@link Object} is equal to
    * this {@link Instruction}; {@code false} otherwise
    */
   @Override
   public boolean equals(final Object other) {
     return other == this || this.getClass().equals(other.getClass());
   }
 
   /**
    * Returns a hashcode for this {@link Instruction}.  This
    * implementation returns the hashcode of this {@link Instruction}'s
    * {@link Object#getClass() Class}.
    *
    * @return a hashcode for this {@link Instruction}
    */
   @Override
   public int hashCode() {
     return this.getClass().hashCode();
   }
 
   /**
    * Returns a non-{@code null} {@link String} representation of this
    * {@link Instruction}.
    *
    * @return a non-{@code null} {@link String} representation of this
    * {@link Instruction}
    */
   @Override
   public String toString() {
     final String simpleName = this.getClass().getSimpleName();
     return String.format("%s%s", simpleName.substring(0, 1).toLowerCase(), simpleName.substring(1));
   }
 
   
   /*
    * Static methods.
    */
 
 
   /**
    * Parses the supplied {@code line} and returns a new {@link
    * Instruction} instance appropriate for this input.
    *
    * @param <T> the type of {@link Object} the resulting {@link
    * Instruction} will work with
    *
    * @param line the {@link String} to parse; must not be {@code null}
    *
    * @return a new {@link Instruction} instance; never {@code null}
    *
    * @exception IllegalArgumentException if {@code line} is {@code
    * null} or unparseable
    *
    * @exception ClassNotFoundException if a class corresponding to a
    * portion of the supplied {@code line} could not be found
    *
    * @exception IllegalAccessException if a new {@link Instruction}
    * instance could not be created because its constructor was found
    * but could not be called due to access restrictions
    *
    * @exception InstantiationException if a new {@link Instruction}
    * instance could not be created because the constructor could not
    * be called
    *
    * @exception InvocationTargetException if a new {@link Instruction}
    * instance could not be created becase the invocation of its
    * constructor threw an {@link Exception}
    *
    * @exception NoSuchMethodException if a new {@link Instruction}
    * instance could not be created because its constructor could not
    * be found
    */
   public static final <T> Instruction<T> valueOf(final String line) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
     if (line == null) {
       throw new IllegalArgumentException("line", new NullPointerException("line"));
     }
     final Matcher m = LINE_PATTERN.matcher(line);
     assert m != null;
     if (!m.matches()) {
       throw new IllegalArgumentException("Bad instruction line: " + line);
     }
 
     final String rawCommand = m.group(1);
     assert rawCommand != null;
     assert rawCommand.length() >= 2;
 
     final String command = String.format("com.edugility.objexj.engine.%s%s", rawCommand.substring(0, 1).toUpperCase(), rawCommand.substring(1));
     assert command != null;
     
     final Class<?> instructionClass = Class.forName(command, true, java.lang.Thread.currentThread().getContextClassLoader());
     assert instructionClass != null;
     if (!Instruction.class.isAssignableFrom(instructionClass)) {
       throw new IllegalArgumentException("bad instruction: " + command);
     }
     
     String operands = m.group(2);
     if (operands == null) {
       operands = "";
     } else {
       operands = operands.trim();
     }
 
     Object instruction = null;
     final Constructor<?> c;
     if (operands.isEmpty()) {
       c = null;
       instruction = instructionClass.newInstance();
     } else {
       c = instructionClass.getConstructor(String.class);
       assert c != null;
       instruction = c.newInstance(operands);
     }
     @SuppressWarnings("unchecked")
     final Instruction<T> temp = (Instruction<T>)instruction;
     return temp;
   }
 
 }
