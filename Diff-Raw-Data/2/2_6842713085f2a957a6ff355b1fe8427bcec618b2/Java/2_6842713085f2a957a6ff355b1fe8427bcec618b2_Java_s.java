 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin.distgen;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class Java {
 
     //~ Instance fields --------------------------------------------------------
 
     private transient String version = "1.6";
 
     private transient String initialHeapSize = "128m";
 
     private transient String maximalHeapSize = "256m";
 
    private transient String jvmArgs = "";
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getJvmArgs() {
         return jvmArgs;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  jvmArgs  DOCUMENT ME!
      */
     public void setJvmArgs(final String jvmArgs) {
         this.jvmArgs = jvmArgs;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getInitialHeapSize() {
         return initialHeapSize;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  initialHeapSize  DOCUMENT ME!
      */
     public void setInitialHeapSize(final String initialHeapSize) {
         this.initialHeapSize = initialHeapSize;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getMaximalHeapSize() {
         return maximalHeapSize;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  maximalHeapSize  DOCUMENT ME!
      */
     public void setMaximalHeapSize(final String maximalHeapSize) {
         this.maximalHeapSize = maximalHeapSize;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getVersion() {
         return version;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  version  DOCUMENT ME!
      */
     public void setVersion(final String version) {
         this.version = version;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("Java ["); // NOI18N
 
         sb.append("version=").append(version);                   // NOI18N
         sb.append(", initialHeapSize=").append(initialHeapSize); // NOI18N
         sb.append(", maximalHeapSize=").append(maximalHeapSize); // NOI18N
         sb.append(", jvmArgs=").append(jvmArgs);                 // NOI18N
         sb.append(']');
 
         return sb.toString();
     }
 }
