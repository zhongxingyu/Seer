 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 
 package de.ailis.jasdoc.doc;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Documentation parser configuration.
  * 
  * @author Klaus Reimer (k@ailis.de)
  */
 public class Configuration
 {
     /** If private symbols are to be included in the documentation. */
     private boolean privateIncluded = false;
 
     /** If protected symbols are to be included in the documentation. */
     private boolean protectedIncluded = true;
 
     /** The output directory. */
     private File outputDirectory = new File("out");
 
     /** The theme for the output. */
     private String theme = "default";
 
     /** The documentation title. */
     private String title = "API Documentation";
 
     /** List of excluded symbols. */
     private List<String> excludes = new ArrayList<String>();
 
     /** List of included symbols. */
     private List<String> includes = new ArrayList<String>();
 
     /**
      * The documentation title. *& private String title = "API Documentation";
      * 
      * /** Enables or disables inclusion of private symbols.
      * 
      * @param privateIncluded
      *            True to include private symbols, false to exclude them
      *            (Default).
      */
     public void setPrivateIncluded(final boolean privateIncluded)
     {
         this.privateIncluded = privateIncluded;
     }
 
     /**
      * Checks if private symbols are to be included in the documentation.
      * 
      * @return True if private symbols are to be included, false if not
      *         (Default).
      */
     public boolean isPrivateIncluded()
     {
         return this.privateIncluded;
     }
 
     /**
      * Enables or disables inclusion of protected symbols.
      * 
      * @param protectedIncluded
      *            True to include protected symbols (Default), false to exclude
      *            them.
      */
     public void setProtectedIncluded(final boolean protectedIncluded)
     {
         this.protectedIncluded = protectedIncluded;
     }
 
     /**
      * Checks if protected symbols are to be included in the documentation.
      * 
      * @return True if protected symbols are to be included (Default), false if
      *         not.
      */
     public boolean isProtectedIncluded()
     {
         return this.protectedIncluded;
     }
 
     /**
      * Sets the output directory.
      * 
      * @param outputDirectory
      *            The output directory to set. Must not be null.
      */
     public void setOutputDirectory(final File outputDirectory)
     {
         if (outputDirectory == null)
             throw new IllegalArgumentException("outputDirectory is null");
         this.outputDirectory = outputDirectory;
     }
 
     /**
      * Returns the output directory.
      * 
      * @return The output directory. Never null.
      */
     public File getOutputDirectory()
     {
         return this.outputDirectory;
     }
 
     /**
      * Sets the theme.
      * 
      * @param theme
      *            The theme to set. Must not be null.
      */
     public void setTheme(final String theme)
     {
         if (theme == null)
             throw new IllegalArgumentException("theme is null");
         this.theme = theme;
     }
 
     /**
      * Returns the theme.
      * 
      * @return The theme.
      */
     public String getTheme()
     {
         return this.theme;
     }
 
     /**
      * Sets the documentation title.
      * 
      * @param title
      *            The documentation title.
      */
     public void setTitle(final String title)
     {
         this.title = title;
     }
 
     /**
      * Returns the documentation title.
      * 
      * @return The documentation title.
      */
     public String getTitle()
     {
         return this.title;
     }
 
     /**
      * Sets the includes.
      * 
      * @param includes
      *            The includes as a comma-separated list.
      */
     public void setIncludes(final String includes)
     {
         for (final String include: includes.split("\\s*,\\s*"))
             this.includes.add(include);
     }
 
     /**
      * Sets the excludes.
      * 
      * @param excludes
      *            The excludes as a comma-separated list.
      */
     public void setExludes(final String excludes)
     {
         for (final String exclude: excludes.split("\\s*,\\s*"))
             this.excludes.add(exclude);
     }
 
     /**
      * Checks if the specified symbol is excluded.
      * 
      * @param name
      *            The qualified symbol name.
      * @return True if symbol is excluded, false if not.
      */
     public boolean isExcluded(String name)
     {
         // Check if symbol is excluded
         for (String exclude: this.excludes)
         {
             if (name.equals(exclude)) return true;
             if (name.startsWith(exclude + ".")) return true;
         }
 
         // Symbol is included if include list is empty
         if (this.includes.isEmpty()) return false;
        
         for (String include: this.includes)
         {
             if (name.equals(include)) return false;
             if (name.startsWith(include + ".")) return false;
         }
        
         // Excluded by default
         return true;
     }
 }
