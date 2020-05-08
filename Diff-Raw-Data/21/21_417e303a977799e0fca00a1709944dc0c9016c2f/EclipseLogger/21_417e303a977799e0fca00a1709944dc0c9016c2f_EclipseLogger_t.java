 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.tools.common.logging;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.scribble.common.logging.Journal;
 import org.scribble.protocol.model.ModelProperties;
 
 /**
  * The Eclipse implementation of the journal.
  *
  */
 public class EclipseLogger implements Journal {
     
     private IFile _file=null;
     private boolean _finished=false;
     private boolean _errorOccurred=false;
     private java.util.Vector<ReportEntry> _entries=new java.util.Vector<ReportEntry>();
     private java.util.Vector<ReportEntry> _reported=new java.util.Vector<ReportEntry>();
     
     /**
      * The constructor.
      * 
      * @param file The file
      */
     public EclipseLogger(IFile file) {
         _file = file;
     }
 
     /**
      * {@inheritDoc}
      */
     public void error(String issue, java.util.Map<String,Object> props) {
         reportIssue(issue, ReportEntry.ERROR_TYPE, props);
         _errorOccurred = true;
     }
 
     /**
      * Has an error occurred.
      * 
      * @return Whether an error has occurred
      */
     public boolean hasErrorOccurred() {
         return (_errorOccurred);
     }
     
     /**
      * {@inheritDoc}
      */
     public void info(String issue, java.util.Map<String,Object> props) {
         reportIssue(issue, ReportEntry.INFORMATION_TYPE, props);
     }
 
     /**
      * {@inheritDoc}
      */
     public void warning(String issue, java.util.Map<String,Object> props) {
         reportIssue(issue, ReportEntry.WARNING_TYPE, props);
     }
     
     /**
      * This method reports an issue.
      * 
      * @param issue The issue
      * @param issueType The issue type
      * @param props The properties
      */
     protected void reportIssue(String issue, int issueType, java.util.Map<String,Object> props) {
         
         if (_file != null) {
     
             synchronized (_entries) {
                 _entries.add(new ReportEntry(issue, issueType, props));
             }
             
             if (_finished) {
                 // Publish immediately
                 finished();
             }
         }
     }            
     
     /**
      * {@inheritDoc}
      */
     public void finished() {
         org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
             public void run() {
                 
                 if (_file != null && _file.exists()) {
                     
                     // Clear current markers
                     try {
                         synchronized (_entries) {
                             
                             if (!_finished) {
                                 _file.deleteMarkers(SavaraMarker.SAVARA_PROBLEM, true,
                                         IFile.DEPTH_INFINITE);
                                 _finished = true;
                             }
                         
                             // Update the markers
                             for (int i=0; i < _entries.size(); i++) {
                                 ReportEntry re=(ReportEntry)_entries.get(i);
                                 
                                 if (!_reported.contains(re)) {
                                     createMarker(re.getIssue(), re.getType(),
                                         re.getProperties());
                                     
                                     _reported.add(re);
                                 }
                             }
                             
                             _entries.clear();
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
     }
     
     /**
      * This method creates a marker.
      * 
      * @param mesg The message
      * @param type The type
      * @param props The properties
      */
     protected void createMarker(String mesg, int type,
                     java.util.Map<String,Object> props) {
                     
         // Create marker for message
         try {
             IMarker marker=_file.createMarker(SavaraMarker.SAVARA_PROBLEM);
             
             // Initialize the attributes on the marker
             marker.setAttribute(IMarker.MESSAGE, mesg);
             
             if (type == ReportEntry.ERROR_TYPE) {
                 marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
             } else if (type == ReportEntry.WARNING_TYPE) {
                 marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
             } else if (type == ReportEntry.INFORMATION_TYPE) {
                 marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
             }
             
             if (props != null &&
             			props.containsKey(ModelProperties.URI)) {
             	Object srcComp=props.get(ModelProperties.URI);
             	
             	marker.setAttribute("uri", srcComp);
             }
             
         } catch (Exception e) {
             
             // TODO: report error
             e.printStackTrace();
         }
     }
     
     /**
      * This is a simple data container class to hold the
      * information reported during validation.
      *
      */
     public class ReportEntry {
         
         /**
          * Error type.
          */
         public static final int ERROR_TYPE=0;
 
         /**
          * Warning type.
          */
         public static final int WARNING_TYPE=1;
 
         /**
          * Information type.
          */
         public static final int INFORMATION_TYPE=2;
         
         private String _issue=null;
         private int _type=0;
         private java.util.Map<String, Object> _properties=null;
 
         /**
          * Constructor.
          * 
          * @param issue The issue
          * @param type The type
          * @param props The properties
          */
         public ReportEntry(String issue, int type,
                     java.util.Map<String, Object> props) {
             _issue = issue;
             _type = type;
             _properties = props;
         }
         
         /**
          * This method returns the issue.
          * 
          * @return The issue
          */
         public String getIssue() {
             return (_issue);
         }
         
         /**
          * This method returns the type.
          * 
          * @return The type
          */
         public int getType() {
             return (_type);
         }
         
         /**
          * This method returns the properties.
          * 
          * @return The properties
          */
         public java.util.Map<String,Object> getProperties() {
             return (_properties);
         }
        
        protected String getKey() {
        	Object uri=getProperties().get("uri");
        	return (_issue+":"+_type+":"+uri);
        }
        
        public int hashCode() {
        	return (getKey().hashCode());
        }
        
        public boolean equals(Object obj) {
        	boolean ret=false;
        	
        	if (obj instanceof ReportEntry) {
        		ret = ((ReportEntry)obj).getKey().equals(getKey());
        	}
        	
        	return (ret);
        }
     }
 }
