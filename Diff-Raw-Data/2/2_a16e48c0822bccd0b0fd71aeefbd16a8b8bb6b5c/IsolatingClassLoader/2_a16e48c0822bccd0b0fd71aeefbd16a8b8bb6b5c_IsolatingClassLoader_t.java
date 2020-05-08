 // Copyright (C) 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.util;
 
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * Keen <code>URLClassLoader</code> that ensures it loads specified
  * packages rather than the usual work-shy Java 2 approach of letting
  * the parent do it. The result is that classes loaded from these
  * packages are isolated from other class loaders.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class IsolatingClassLoader extends URLClassLoader {
 
   /**
    * Array of prefixes that identifies packages or classes that
    * shouldn't be isolated.
    */
   private final String[] m_sharedPrefixes;
 
   /**
    * Set of class names that identifies classes that shouldn't be isolated.
    */
   private final Set m_sharedClassNames = new HashSet();
 
   private final boolean m_isolateParentOnly;
 
   /**
    * Constructor.
    *
    * @param parent
    *          Parent classloader. We use its class path to load our classes.
    * @param shared
    *          Array of fully qualified class names, or fully qualified prefixes
   *          ending in "*", that identify the packages or classes to share.
    * @param isolateParentOnly
    *          If <code>true</code>, automatically share classes from
    *          <code>parent</code>'s parent classloaders.
    */
   public IsolatingClassLoader(URLClassLoader parent, String[] shared,
                               boolean isolateParentOnly) {
     super(parent.getURLs(), parent);
 
     final List prefixes = new ArrayList();
 
     for (int i = 0; i < shared.length; i++) {
       final int index = shared[i].indexOf('*');
 
       if (index >= 0) {
         prefixes.add(shared[i].substring(0, index));
       }
       else {
         m_sharedClassNames.add(shared[i]);
       }
     }
 
     m_sharedPrefixes = (String[])prefixes.toArray(new String[0]);
 
     m_isolateParentOnly = isolateParentOnly;
   }
 
   /**
    * Override to only check parent ClassLoader if the class name matches our
    * list of shared classes.
    *
    * @param name The name of the class to load.
    * @param resolve Whether the class should be initialised.
    * @return The class.
    * @throws ClassNotFoundException If the class couldn't be found.
    */
   protected Class loadClass(String name, boolean resolve)
     throws ClassNotFoundException  {
 
     if (m_isolateParentOnly) {
       try {
         // We always have a parent classloader.
         // Null grandparent => use boot classloader.
         return Class.forName(name, resolve, getParent().getParent());
       }
       catch (ClassNotFoundException e) {
         // Grandparent knows nothing.
       }
     }
 
     synchronized (this) {
       boolean shared = m_sharedClassNames.contains(name);
 
       for (int i = 0; !shared && i < m_sharedPrefixes.length; i++) {
         if (name.startsWith(m_sharedPrefixes[i])) {
           shared = true;
         }
       }
 
       if (!shared) {
         Class c = findLoadedClass(name);
 
         if (c == null) {
           c = findClass(name);
         }
 
         if (resolve) {
           resolveClass(c);
         }
 
         return c;
       }
       else {
         return super.loadClass(name, resolve);
       }
     }
   }
 }
