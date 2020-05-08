 package com.wickedspiral.jacss;
 
 import org.kohsuke.args4j.Option;
 
 /**
 * @author wasche
 * @since 10/1/13
 */
 public class Options
 {
     @Option( name = "-v", aliases = { "--verbose" }, required = false, metaVar = "VERBOSE",
         usage = "Print debugging information" )
     protected boolean verbose = false;
 
     @Option( name = "-d", aliases = { "--debug" }, required = false, metaVar = "DEBUG",
         usage = "Print additional debugging information" )
     protected boolean debug = false;
 
     @Option( name = "-f", aliases = { "--force" }, required = false, metaVar = "FORCE",
         usage = "Force re-compression" )
     protected boolean force = false;
 
     @Option( name = "--keep-trailing-semicolons", required = false,
         usage = "Do not strip semicolons on last style of a rule" )
     protected boolean keepTailingSemicolons = false;
     
     @Option( name = "--add-trailing-semicolons", required = false,
         usage = "Add trailing semicolons on last style of a rule" )
     protected boolean addTrailingSemicolons = false;
 
     @Option( name = "--no-collapse-zeroes", required = false, usage = "Do not drop leading zeroes inside rgba()" )
     protected boolean noCollapseZeroes = false;
 
     @Option( name = "--no-collapse-none", required = false, usage = "Do not collapse none to 0" )
     protected boolean noCollapseNone = false;
     
     @Option( name = "--no-lowercasify-rgb", required = false, usage = "Do not lowercasify RGB hex constants" )
     protected boolean noLowercasifyRgb = false;
     
     // This is too silly to be a public option; fix #24.
     protected boolean keepUnitsInColorStop = false;
     
    // This is too silly to be a public option; fix #25.
    protected boolean noLowercasifyKeywords = false;
    
     @Option( name = "--compat-yui242", required = false, usage = "Match compatibility with YUI 2.4.2" )
     protected boolean yui242 = false;
 
     public void imply()
     {
         if ( yui242 )
         {
             keepTailingSemicolons = true;
             addTrailingSemicolons = true;
             noCollapseZeroes = true;
             noCollapseNone = true;
             noLowercasifyRgb = true;
            noLowercasifyKeywords = true;
             keepUnitsInColorStop = true;
         }
     }
     
     public boolean isDebug()
     {
         return debug;
     }
 
     public boolean shouldCollapseNone()
     {
         return !noCollapseNone;
     }
     
     public boolean shouldCollapseZeroes()
     {
         return !noCollapseZeroes;
     }
 
     public boolean shouldLowercasifyRgb()
     {
         return !noLowercasifyRgb;
     }
 
    public boolean shouldLowercasifyKeywords()
    {
        return !noLowercasifyKeywords;
    }

     public boolean keepTailingSemicolons()
     {
         return keepTailingSemicolons || addTrailingSemicolons;
     }
     
     public boolean addTrailingSemicolons()
     {
         return addTrailingSemicolons;
     }
     
     public boolean keepUnitsInColorStop()
     {
         return keepUnitsInColorStop;
     }
 }
