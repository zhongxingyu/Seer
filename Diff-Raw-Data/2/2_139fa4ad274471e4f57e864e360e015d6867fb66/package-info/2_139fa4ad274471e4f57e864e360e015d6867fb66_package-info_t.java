 /*
  * Copyright 2012-2013 Raffael Herzog
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * <p>Annotations and runtime classes for contracts.</p>
  *
  * <h2>Concepts</h2>
  *
  * <p>There are three main annotations:</p>
  *
  * <dl>
  * <dt>{@link ch.raffael.contracts.Require Require}</dt>
  * <dd><em>Preconditions</em>: Conditions that must be true on method entry. This can
  * also be used to check the method parameters. <strong>Inheritance</strong>:
  * Inheriting classes may <em>extend</em> the contract, i.e. inherited preconditions
  * will be <em>OR</em>-associated.</dd>
  *
  * <dt style="margin-top:1ex">{@link ch.raffael.contracts.Ensure Ensure}</dt>
  * <dd><em>Postconditions</em>: Conditions that must be true on method entry. This can
  * also be used to check the method's return value. <strong>Inheritance</strong>:
  * Inheriting classes may further <em>constrain</em> the contract, i.e. inherited
  * postconditions will be <em>AND</em>-associated.</dd>
  *
  * <dt style="margin-top:1ex">{@link ch.raffael.contracts.Invariant Invariant}</dt>
  * <dd>Conditions that must be true both on method entry and exit for each method of
  * the class. <strong>Inheritance</strong>: Inheriting classes may further
  * <em>constrain</em> the contract, i.e. inherited invariants will be
  * <em>AND</em>-associated.</dd>
  * </dl>
  *
  * <p>The conditions are expressed using Cel expressions.</p>
  *
  * <h2>The Contract Expression Language Cel</h2>
  *
  * <p>Generally, Cel expressions are just Java expressions evaluated in the scope of the
  * class they are defined in. Conditions that are defined for a method
  * (Require and Ensure) additionally may access the
  * method parameters (but not local variables). See also the full
 * <a href="processor/cel/Cel.g">ANTLR3 grammar</a>.</p>
  *
  * <p>There are, however, some limitations in these expressions:</p>
  *
  * <ul>
  * <li><strong>New instances</strong>: Cel expressions cannot create new instances. The
  * <code>new</code> operator is disabled.</li>
  *
  * <li><strong>Assignments</strong>: Cel expressions must be free of any side-effects,
  * therefore they cannot change any values. All assignment operators, including pre-
  * and post-increment and -decrement.</li>
  *
  * <li><strong>Generics</strong>: For now, Cel expressions operate on the erasure,
  * there's no support for generics.</li>
  *
  * <li><strong>Autoboxing</strong>: There is no support for autoboxing/-unboxing.</li>
  * </ul>
  *
  * <p>On the other side, there are a few extensions to Java expressions described
  * below.</p>
  *
  *
  * <h3>String and character literals</h3>
  *
  * <p>Contract expressions are embedded in the Java code as annotations. This means that
  * whenever you're writing a string literal, you'd have to escape the quotes:</p>
  *
  * <pre>{@literal @Require("foo.startsWith(\"bar\")")}
  *{@literal @Ensure("@result().startsWith(\"\\\"\")") // ouch!}</pre>
  *
  * <p>To work around this issue, Cel also supports singe quotes for string literals:</p>
  *
  * <pre>{@literal @Require("foo.startsWith('bar')")}
  *{@literal @Ensure("@result().startsWith('\"')")}</pre>
  *
  * This, however, conflicts with char literals, which is why Cel introduces a new syntax
  * for them. Use single quotes as usual, but append a 'c' (case-insensitive):
  *
  * <pre>{@literal @Require("foo=='X'c")}</pre>
  *
  *
  * <h3>Conditional conditions ;)</h3>
  *
  * <p>Basically, this is just another way of writing logical OR. The advantage is that
  * it's more natural and intuitively understandable. An example:</p>
  *
  * <pre>if ( myObject != null ) myObject.isValid()</pre>
  *
  * <p>Read: "If myObject is not null, it must be valid". This is a more readable way of
  * writing:</p>
  *
  * <pre>myObject == null || myObject.isValid()</pre>
  *
  *
  * <h3>Finally</h3>
  *
  * Postconditions, if not otherwise specified, will only be checked upon normal method
  * exit. To check them also when the method is throwing an exception, use {@code finally}:
  *
  * <pre>@Ensure("finally a==b")</pre>
  *
  * To check a post-condition only when throwing, use the {@code @throw} function (see
  * below):
  *
  * <pre>@Ensure("finally if(@thrown(Throwable)) a==b</pre>
  *
  *
  * <h3>Functions</h3>
  *
  * <p>Functions provide some extended functionality needed to express contracts. All
  * functions start with an '@' character. The following functions are recognised:</p>
  *
  * <dl>
  * <dt><code>@old(&lt;expression&gt;)</code></dt>
  * <dd>
  * [<code>Ensure, Invariant</code>]
  * Evaluate the expression on method-entry but refer to its value on
  * method-exit.
  * <p><strong>Example</strong></p>
  * <pre>size() == @old(size()) + 1</pre>
  * <p>(e.g. postcondition for the add() method of a list)</p>
  * </dd>
  *
  * <dt style="margin-top:1ex"><code>@result()</code></dt>
  * <dd>
  * [Ensure</code>] Refer to the return value of the method.
  * </dd>
  *
  * <dt style="margin-top:1ex"><code>@thrown(&lt;[exception-class]&gt;)</code></dt>
  * <dd>
  * [<code>Ensure, Invariant</code>] Check that
  * an exception has been thrown. If the exception is not specified,
  * {@link java.lang.Throwable Throwable} is assumed. There are two variants:
  * <ul>
  * <li><code>thrown(IllegalStateException)</code> is true, if the method
  * threw an IllegalStateException.</li>
  * <li><code>thrown(IllegalStateException).getMessage().equals("Foo")</code>
  * is true, if the method threw an IllegalStateException and the message
  * equals "foo".</li>
  * </ul>
  * </dd>
  *
  * <dt style="margin-top:1ex"><code>@param([&lt;index&gt;])</code></dt>
  * <dd>
  * [<code>Require, Ensure</code>] Usually, you
  * can (and should) refer to parameters by their name. This allows to access
  * parameters by their index. If the annotation is on the method, the index is
  * required and absolute (<code>@param(0)</code> refers to the first parameter).
  * If the annotation is on a parameter, the index can be omitted and is relative:
  * <code>@param(0)</code> refers to the annotated parameter,
  * <code>@param(-1)</code> and <code>@param(+1)</code> refer to the parameter to
  * the left and right of the annotated parameter. In this context, the index can
  * be omitted: <code>@param()</code> refers to the annotated parameter.
  * </dd>
  *
  * <dt style="margin-top:1ex"><code>@each(&lt;expression&gt;, &lt;identifier&gt; -&gt; &lt;expression&gt;)</code></dt>
  * <dd>
  * [<code>Require, Ensure, Invariant</code>]
  * Make sure an expression is true for each element of an <code>Iterable</code>
  * or array.
  *
  * <p><strong>Example</strong></p>
  * <pre>@each(getChildren(), child -&gt; child.isValid())</pre>
  * <p>Ensure that each element of <code>getChildren()</code> is valid.</p>
  * </dd>
  *
  * <dt style="margin-top:1ex"><code>@equal(&lt;expression&gt;, &lt;expression&gt;)</code></dt>
  * <dd>
  * [<code>Require, Ensure, Invariant</code>]
  * Null-safe equals. <code>@equal(a, b)</code> is the same as
  * <code>a==null ? b==null : a.equals(b)</code>
  * </dd>
  *
  * <dt style="margin-top:1ex"><code>@regex(&lt;regular-expression&gt; [, &lt;flags&gt;)</code></dt>
  * <dd>
  * [<code>Require, Ensure, Invariant</code>]
  * Shorthand to <code>java.util.regex.Pattern</code>. Specify flags as a comma
  * separated list of the constants defined in <code>Pattern</code> (e.g.
  * <code>@regex("ab+c.*", CASE_INSENSITIVE, DOTALL)</code>). The result of this
  * function is a {@link ch.raffael.contracts.internal.Regex}.
  * </dd>
  * </dl>
  */
 package ch.raffael.contracts;
