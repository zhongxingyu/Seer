 /*
  * Copyright (C) 2011 Klaus Reimer <k@ailis.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.ailis.maven.plugin.javascript;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.codehaus.plexus.util.StringUtils;
 
 import com.google.javascript.jscomp.JSSourceFile;
 
 /**
  * Parses dependencies from a source file.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public class DependencyManager
 {
     /** Regular expression to match @require annotation */
     private static final Pattern REQUIRE_REGEX = Pattern
         .compile("(?m)^\\s*\\*\\s*[%@]require\\s+([a-zA-Z0-9\\/\\-\\.]+)\\s*$");
 
     /** Regular expression to match @use annotation */
     private static final Pattern USE_REGEX = Pattern
         .compile("(?m)^\\s*\\*\\s*[%@]use\\s+([a-zA-Z0-9\\/\\-\\.]+)\\s*$");
 
     /** List of dependencies per script filename. */
     private final Map<String, List<Dependency>> scriptDependencies =
         new HashMap<String, List<Dependency>>();
 
     /** The absolute source directory as string with trailing slash. */
     private final String sourceDirectory;
 
     /** The order of the resolved dependencies. */
     private final List<String> order = new ArrayList<String>();
 
     /** The included dependencies. */
     private final Set<String> included = new HashSet<String>();
 
     /** The resolve stack. */
     private final Stack<String> stack = new Stack<String>();
 
     /** The used dependencies. */
     private final Set<String> used = new HashSet<String>();
 
     /**
      * Constructor.
      *
      * @param sourceDirectory
      *            The source directory.
      */
     public DependencyManager(final File sourceDirectory)
     {
         this.sourceDirectory =
             sourceDirectory.getAbsolutePath() + File.separatorChar;
     }
 
     /**
      * Adds the specified JavaScript source file
      *
      * @param file
      *            The JavaScript source file to add. Must not be null.
      * @throws IOException
      *             If JavaScript source file was not found.
      */
     public void addScript(final JSSourceFile file) throws IOException
     {
         final List<Dependency> dependencies = new ArrayList<Dependency>();
         final String code = file.getCode();
         Matcher matcher = REQUIRE_REGEX.matcher(code);
         while (matcher.find())
         {
             dependencies.add(new Dependency(matcher.group(1), true));
         }
         matcher = USE_REGEX.matcher(code);
         while (matcher.find())
         {
             dependencies.add(new Dependency(matcher.group(1), false));
         }
        this.scriptDependencies.put(file.getName().replace(
            this.sourceDirectory, "").replace("\\", "/"), dependencies);
     }
 
     /**
      * Adds the specified JavaScript source files.
      *
      * @param files
      *            The JavaScript source files to add. Must not be null.
      * @throws IOException
      *             If JavaScript source file was not found.
      */
     public void addScripts(final List<JSSourceFile> files)
         throws IOException
     {
         for (final JSSourceFile file : files)
             addScript(file);
     }
 
     /**
      * Returns the dependencies of the specified file.
      *
      * @param file
      *            The JavaScript source file.
      * @return The dependencies. Never null. May be empty.
      * @throws IllegalArgumentException
      *             When specified file was not added to the dependency manager
      *             first.
      */
     public List<Dependency> getDependencies(final JSSourceFile file)
     {
         final List<Dependency> dependencies =
             this.scriptDependencies.get(file.getName().replace(
                this.sourceDirectory, "").replace("\\", "/"));
         if (dependencies == null)
             throw new IllegalArgumentException("File " + file
                 + " is unknown to script dependency manager");
         return dependencies;
     }
 
     /**
      * Returns the external dependencies.
      *
      * @return The external dependencies. Never null. May be empty.
      */
     public Set<Dependency> getExternalDependencies()
     {
         final Set<Dependency> externalDependencies =
             new HashSet<Dependency>();
         for (final Map.Entry<String, List<Dependency>> entry : this.scriptDependencies
             .entrySet())
         {
             final List<Dependency> dependencies = entry.getValue();
             for (final Dependency dependency : dependencies)
             {
                 if (!this.scriptDependencies.containsKey(dependency
                     .getFilename()))
                     externalDependencies.add(dependency);
             }
         }
         return externalDependencies;
     }
 
     /**
      * Returns the provided dependencies.
      *
      * @return The provided dependencies. Never null. May be empty.
      */
     public Set<String> getProvidedDependencies()
     {
         return Collections.unmodifiableSet(this.scriptDependencies.keySet());
     }
 
     /**
      * Resolves the dependencies and returns a list with the correctly ordered
      * files.
      *
      * @return The list with ordered files.
      * @throws CircularDependencyException
      *             When a circular dependency was found.
      */
     public List<String> resolve()
     {
         this.order.clear();
         this.stack.clear();
         this.used.clear();
         this.included.clear();
         for (final String name : this.scriptDependencies.keySet())
         {
             includeScript(name);
         }
         return this.order;
     }
 
     /**
      * Dependency resolving: Includes the specified script.
      *
      * @param filename
      *            The script to include
      */
     private void includeScript(final String filename)
     {
         // Ignore script if already included
         if (this.included.contains(filename)) return;
 
         // Check for circular dependencies
         if (this.stack.contains(filename))
             throw new CircularDependencyException(
                 "Circular dependency detected: "
                     + StringUtils.join(this.stack.iterator(), " > ") + " > "
                     + filename, this.sourceDirectory + filename);
         this.stack.push(filename);
 
         // Process the script
         processScript(filename);
        this.order.add((this.sourceDirectory + filename).replace("\\", "/"));
 
         // Mark script as included
         this.included.add(filename);
         this.stack.pop();
 
         if (this.stack.isEmpty()) finish();
     }
 
     /**
      * Dependency resolving: Uses the specified script.
      *
      * @param filename
      *            The script to use
      */
     private void useScript(final String filename)
     {
         if (this.included.contains(filename)) return;
         if (this.used.contains(filename)) return;
         this.used.add(filename);
     }
 
     /**
      * Dependency resolving: Finishes the resolving.
      */
     private void finish()
     {
         if (this.used.isEmpty()) return;
         final Set<String> used = new HashSet<String>(this.used);
         this.used.clear();
         for (final String use : used)
             includeScript(use);
         finish();
     }
 
     /**
      * Dependency resolving: processes a script and its dependencies.
      *
      * @param filename
      *            The script to process
      */
     private void processScript(final String filename)
     {
         final List<Dependency> dependencies =
             this.scriptDependencies.get(filename);
 
         if (dependencies != null)
         {
             for (final Dependency dependency : dependencies)
                 if (dependency.isRequired())
                     includeScript(dependency.getFilename());
             for (final Dependency dependency : dependencies)
                 if (!dependency.isRequired())
                     useScript(dependency.getFilename());
         }
     }
 }
