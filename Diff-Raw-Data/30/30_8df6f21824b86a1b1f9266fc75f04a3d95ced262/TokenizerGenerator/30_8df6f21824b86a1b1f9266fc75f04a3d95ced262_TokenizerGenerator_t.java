 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.httpparser;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.jboss.classfilewriter.AccessFlag;
 import org.jboss.classfilewriter.ClassFile;
 import org.jboss.classfilewriter.ClassMethod;
 import org.jboss.classfilewriter.code.BranchEnd;
 import org.jboss.classfilewriter.code.CodeAttribute;
 import org.jboss.classfilewriter.code.LookupSwitchBuilder;
 import org.jboss.classfilewriter.code.TableSwitchBuilder;
 import org.jboss.classfilewriter.util.DescriptorUtils;
 
 /**
  * @author Stuart Douglas
  */
 public class TokenizerGenerator {
 
     private static final AtomicInteger nameCounter = new AtomicInteger();
 
     public static Tokenizer createTokenizer(final String[] values) {
         final String className = Tokenizer.class.getName() + "$$" + nameCounter.incrementAndGet();
         final ClassFile file = new ClassFile(className, "java.lang.Object", Tokenizer.class.getName());
 
         final ClassMethod ctor = file.addMethod(AccessFlag.PUBLIC, "<init>", "V");
         ctor.getCodeAttribute().aload(0);
         ctor.getCodeAttribute().invokespecial(Object.class.getName(), "<init>", "()V");
         ctor.getCodeAttribute().returnInstruction();
 
         //list of all states except the initial
         final List<State> allStates = new ArrayList<State>();
         final State initial = new State((byte) 0, "");
         for (String value : values) {
             addStates(initial, value, allStates);
         }
         //we want initial to be number 0
         final AtomicInteger stateCounter = new AtomicInteger(-1);
         setupStateNo(initial, stateCounter);
         for (State state : allStates) {
             setupStateNo(state, stateCounter);
         }
         final int noStates = stateCounter.get();
 
         final ClassMethod handle = file.addMethod(Modifier.PUBLIC, "handle", "V", "B", DescriptorUtils.makeDescriptor(TokenContext.class));
         writeHandle(className, handle.getCodeAttribute(), initial, allStates, noStates);
 
         writeStateMethod(file, initial);
         for (State state : allStates) {
             if (state.stateno > 0) {
                 writeStateMethod(file, state);
             }
         }
 
         final Class<Tokenizer> cls = (Class<Tokenizer>) file.define(TokenizerGenerator.class.getClassLoader());
         try {
             return cls.newInstance();
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static void writeStateMethod(final ClassFile file, final State currentState) {
         final ClassMethod handle = file.addMethod(Modifier.STATIC, "state" + currentState.stateno, "V", "B", DescriptorUtils.makeDescriptor(TokenContext.class));
         final CodeAttribute c = handle.getCodeAttribute();
         c.iload(0);
         final LookupSwitchBuilder s = new LookupSwitchBuilder();
         final AtomicReference<BranchEnd> tokenEnd = s.add((byte) ' ');
         final Map<State, AtomicReference<BranchEnd>> ends = new IdentityHashMap<State, AtomicReference<BranchEnd>>();
         for (final State state : currentState.next.values()) {
                 ends.put(state, s.add(state.value));
         }
         c.lookupswitch(s);
 
         //now we write out tokenEnd
         c.branchEnd(tokenEnd.get());
         if (!currentState.soFar.equals("")) {
             c.aload(1);
             c.ldc(currentState.soFar);
             c.invokestatic(TokenizerGenerator.class.getName(), "tokenEnd", "(" + DescriptorUtils.makeDescriptor(TokenContext.class) + "Ljava/lang/String;)V");
         }
         c.returnInstruction();
 
         final BranchEnd defaultSetup = s.getDefaultBranchEnd().get();
         c.branchEnd(defaultSetup);
         c.setupFrame("B", DescriptorUtils.makeDescriptor(TokenContext.class));
         c.aload(1);
         c.invokevirtual(TokenContext.class.getName(), "getState", DescriptorUtils.makeDescriptor(TokenState.class), new String[0]);
         c.dup();
         c.iconst(TokenState.NO_STATE);
         c.invokevirtual(TokenState.class.getName(), "setState", "(I)V");
         c.newInstruction(StringBuilder.class);
         c.dup();
         c.ldc(currentState.soFar);
         c.invokespecial(StringBuilder.class.getName(), "<init>", "(Ljava/lang/String;)V");
         c.iload(0);
         c.invokevirtual(StringBuilder.class.getName(), "append", "(C)Ljava/lang/StringBuilder;");
         c.invokevirtual(TokenState.class.getName(), "setStringBuilder", "(Ljava/lang/StringBuilder;)V");
         c.returnInstruction();
 
         for(Map.Entry<State, AtomicReference<BranchEnd>> e : ends.entrySet()) {
             c.branchEnd(e.getValue().get());
             final State state = e.getKey();
             if(state.stateno < 0 ) {
                 //prefix match
                 c.aload(1);
                 c.invokevirtual(TokenContext.class.getName(), "getState", DescriptorUtils.makeDescriptor(TokenState.class), new String[0]);
                 c.dup();
                 c.dup();
                 c.iconst(state.stateno);
                 c.invokevirtual(TokenState.class.getName(), "setState", "(I)V");
                 c.ldc(state.terminalState);
                 c.invokevirtual(TokenState.class.getName(), "setCurrent", "(Ljava/lang/String;)V");
                 c.iconst(state.soFar.length());
                 c.invokevirtual(TokenState.class.getName(), "setPos", "(I)V");
                 c.returnInstruction();
             } else {
                 c.aload(1);
                 c.invokevirtual(TokenContext.class.getName(), "getState", DescriptorUtils.makeDescriptor(TokenState.class), new String[0]);
                 c.iconst(state.stateno);
                 c.invokevirtual(TokenState.class.getName(), "setState", "(I)V");
                 c.returnInstruction();
             }
         }
 
     }
 
     private static void setupStateNo(final State state, final AtomicInteger stateCounter) {
         if (state.next.isEmpty()) {
             state.stateno = TokenState.PREFIX_MATCH;
             state.terminalState = state.soFar;
         } else if (state.next.size() == 1) {
             String terminal = null;
             State s = state.next.values().iterator().next();
             while (true) {
                 if (s.next.size() > 1) {
                     break;
                 } else if (s.next.isEmpty()) {
                     terminal = s.soFar;
                     break;
                 }
                 s = s.next.values().iterator().next();
             }
             if (terminal != null) {
                 state.stateno = TokenState.PREFIX_MATCH;
                 state.terminalState = terminal;
             } else {
                 state.stateno = stateCounter.incrementAndGet();
             }
         } else {
             state.stateno = stateCounter.incrementAndGet();
         }
     }
 
     private static void writeHandle(final String className, final CodeAttribute c, final State initial, final List<State> allStates, int noStates) {
         final List<State> states = new ArrayList<State>();
         states.add(initial);
         states.addAll(allStates);
         Collections.sort(states);
         c.iload(1);
         c.aload(2);
         c.dup();
         c.invokevirtual(TokenContext.class.getName(), "getState", DescriptorUtils.makeDescriptor(TokenState.class), new String[0]);
         c.invokevirtual(TokenState.class.getName(), "getState", "()I");
         TableSwitchBuilder builder = new TableSwitchBuilder(-2, noStates);
         final IdentityHashMap<State, AtomicReference<BranchEnd>> ends = new IdentityHashMap<State, AtomicReference<BranchEnd>>();
         final AtomicReference<BranchEnd> prefixMatch = builder.add();
         final AtomicReference<BranchEnd> noState = builder.add();
 
         ends.put(initial, builder.add());
         for (final State s : states) {
             if (s.stateno > 0) {
                 ends.put(s, builder.add());
             }
         }
         c.tableswitch(builder);
         c.branchEnd(builder.getDefaultBranchEnd().get());
         c.newInstruction(RuntimeException.class);
         c.dup();
         c.ldc("Could not find state");
         c.invokespecial(RuntimeException.class.getName(), "<init>", "(Ljava/lang/String;)V");
         c.athrow();
 
         //prefix
         c.branchEnd(prefixMatch.get());
         c.invokestatic(TokenizerGenerator.class.getName(), "prefix", "(B" + DescriptorUtils.makeDescriptor(TokenContext.class) + ")V");
         c.returnInstruction();
 
         //none
         c.branchEnd(noState.get());
         c.invokestatic(TokenizerGenerator.class.getName(), "nostate", "(B" + DescriptorUtils.makeDescriptor(TokenContext.class) + ")V");
         c.returnInstruction();
 
         invokeState(className, c, ends, initial);
         for (final State s : states) {
             if (s.stateno >= 0) {
                 invokeState(className, c, ends, s);
             }
         }
     }
 
     //These methods are invoked by generated bytecode
     @SuppressWarnings("unused")
     static void prefix(byte b, TokenContext context) {
         final TokenState state = context.getState();
        final String current = state.getCurrent();
         if(b == ' ') {
            if(current.length() == state.getPos()) {
                tokenEnd(context, current);
             } else {
                tokenEnd(context, current.substring(0, state.getPos()));
             }
         } else {
            if(state.getPos() != current.length() && b == current.charAt(state.getPos())) {
                 state.setPos(state.getPos()+1);
             } else {
                 state.setState(TokenState.NO_STATE);
                final StringBuilder builder = new StringBuilder(current.substring(0, state.getPos()));
                 builder.append((char)b);
                 state.setStringBuilder(builder);
             }
         }
     }
 
     @SuppressWarnings("unused")
     static void nostate(byte b, TokenContext context) {
         if(b == ' ') {
             tokenEnd(context, context.getState().getStringBuilder().toString());
         } else {
             context.getState().getStringBuilder().append((char)b);
         }
     }
 
     @SuppressWarnings("unused")
     static void tokenEnd(TokenContext context, String token) {
         final TokenState state = context.getState();
         context.getTokenHandler().handleToken(token, context);
         state.setState(0);
         state.setStringBuilder(null);
     }
 
     private static void invokeState(final String className, final CodeAttribute c, final IdentityHashMap<State, AtomicReference<BranchEnd>> ends, final State s) {
         c.branchEnd(ends.get(s).get());
         c.invokestatic(className, "state" + s.stateno, "(B" + DescriptorUtils.makeDescriptor(TokenContext.class) + ")V");
         c.returnInstruction();
     }
 
     private static void addStates(final State initial, final String value, final List<State> allStates) {
         addStates(initial, value, 0, allStates);
     }
 
     private static void addStates(final State current, final String value, final int i, final List<State> allStates) {
         if (i == value.length()) {
             return;
         }
         byte[] bytes = value.getBytes();
         final byte currentByte = bytes[i];
         State newState = current.next.get(currentByte);
         if (newState == null) {
             current.next.put(currentByte, newState = new State(currentByte, value.substring(0, i + 1)));
             allStates.add(newState);
         }
         addStates(newState, value, i + 1, allStates);
     }
 
     private static class State implements Comparable<State> {
 
         Integer stateno;
         String terminalState;
         final byte value;
         final String soFar;
         final Map<Byte, State> next = new HashMap<Byte, State>();
 
         private State(final byte value, final String soFar) {
             this.value = value;
             this.soFar = soFar;
         }
 
         @Override
         public int compareTo(final State o) {
             return stateno.compareTo(o.stateno);
         }
     }
 }
