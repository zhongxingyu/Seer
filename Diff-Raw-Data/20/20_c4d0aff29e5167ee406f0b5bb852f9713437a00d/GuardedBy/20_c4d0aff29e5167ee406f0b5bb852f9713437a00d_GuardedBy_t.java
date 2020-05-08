 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
/**
GuardedBy can be applied to individual fields of a class to specify that they can
only be accessed when the current Thread holds a certain lock. For example:

<pre>
@GuardedBy("this") public int sharedData;

…
synchronized(this){ sharedData = 7; } // OK
…
sharedData = 13; // throws IllegalAccessError
</pre>

Only "this", static and non-static members of the form "full.package.and.Class.field",
and class objects of the form "full.package.and.Class.class" are accepted as guard locks.
<p>
When these compile-time guards are insufficient, use the Checker.guardBy API.

@see edu.unh.cs.tact.Checker#guardBy(Object, Object)
*/
 public @interface GuardedBy{
 	String value();
 }
